/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.wso2.openbanking.accelerator.consent.extensions.common.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Class to handle idempotency related operations.
 */
public class IdempotencyValidator {

    private static final Log log = LogFactory.getLog(IdempotencyValidator.class);
    private static final ConsentCoreService consentCoreService = ConsentExtensionsDataHolder.getInstance()
            .getConsentCoreService();

    /**
     * Method to check whether the request is idempotent.
     * This method will first check whether idempotency validation is enabled. After that it will validate whether
     * required parameters for validation is present.
     * For validation, need to check whether the idempotency key values is present as a consent attribute, if present
     * the consent will be retrieved. Finally following conditions will be validated.
     *  - Whether the client id sent in the request and client id retrieved from the database are equal
     *  - Whether the difference between two dates is less than the configured time
     *  - Whether payloads are equal
     *
     * @param consentManageData            Consent Manage Data
     * @return  IdempotencyValidationResult
     */
    public static IdempotencyValidationResult validateIdempotency(ConsentManageData consentManageData)
            throws IdempotencyValidationException {

        if (IdempotencyValidationUtils.isIdempotencyEnabledFromConfig()) {
            // If idempotency key value, client id or request is empty then cannot proceed with idempotency validation
            if (!IdempotencyValidationUtils.isMandatoryParamsPresent(consentManageData, getIdempotencyHeaderName())) {
                log.error("Idempotency Key Value, Client ID or Request is empty. Hence cannot proceed with " +
                        "idempotency validation");
                return new IdempotencyValidationResult(false, false, null, null);
            }
            try {
                String idempotencyKeyName = getIdempotencyAttributeName(consentManageData.getRequestPath());
                String idempotencyKeyValue = consentManageData.getHeaders().get(getIdempotencyHeaderName());
                // Retrieve consent ids that have the idempotency key name and value as attribute
                ArrayList<String> consentIds = IdempotencyValidationUtils
                        .getConsentIdsFromIdempotencyKey(idempotencyKeyName, idempotencyKeyValue);
                // Check whether the consent id list is not empty. If idempotency key exists in the database then
                // the consent Id list will be not empty.
                if (!consentIds.isEmpty()) {
                    log.debug(String.format("Idempotency Key  %s exists in the database. Hence this is an idempotent" +
                            " request", idempotencyKeyValue));
                    for (String consentId : consentIds) {
                        DetailedConsentResource consentRequest = consentCoreService.getDetailedConsent(consentId);
                        if (consentRequest != null) {
                            return validateIdempotencyConditions(consentManageData, consentRequest);
                        } else {
                            log.error(IdempotencyConstants.ERROR_NO_CONSENT_DETAILS);
                            throw new IdempotencyValidationException(IdempotencyConstants.ERROR_NO_CONSENT_DETAILS);
                        }
                    }
                }
            } catch (IOException e) {
                log.error(IdempotencyConstants.JSON_COMPARING_ERROR, e);
                throw new IdempotencyValidationException(IdempotencyConstants.JSON_COMPARING_ERROR);
            } catch (ConsentManagementException e) {
                log.error(IdempotencyConstants.CONSENT_RETRIEVAL_ERROR, e);
                return new IdempotencyValidationResult(true, false, null, null);
            }
        }
        return new IdempotencyValidationResult(false, false, null, null);
    }

    /**
     * Method to check whether the idempotency conditions are met.
     * This method will validate the following conditions.
     *  - Whether the client id sent in the request and client id retrieved from the database are equal
     *  - Whether the difference between two dates is less than the configured time
     *  - Whether payloads are equal
     *
     * @param consentManageData        Consent Manage Data
     * @param consentRequest           Detailed Consent Resource
     * @return  IdempotencyValidationResult
     */
    private static IdempotencyValidationResult validateIdempotencyConditions(ConsentManageData consentManageData,
                                                                             DetailedConsentResource consentRequest)
            throws IdempotencyValidationException, IOException {
        // Compare the client ID sent in the request and client id retrieved from the database
        // to validate whether the request is received from the same client
        if (IdempotencyValidationUtils.isClientIdsMatching(consentManageData.getClientId(),
                consentRequest.getClientID())) {
            // Check whether difference between two dates is less than the configured time
            if (IdempotencyValidationUtils.isRequestReceivedWithinAllowedTime(getCreatedTimeOfPreviousRequest(
                    consentManageData.getRequestPath(), consentRequest.getConsentID()))) {
                // Compare whether JSON payloads are equal
                if (isPayloadSimilar(consentManageData, getPayloadOfPreviousRequest(
                        consentManageData.getRequestPath(), consentRequest.getConsentID()))) {
                    log.debug("Payloads are similar and request received within allowed" +
                            " time. Hence this is a valid idempotent request");
                    return new IdempotencyValidationResult(true, true,
                            consentRequest, consentRequest.getConsentID());
                } else {
                    log.error(IdempotencyConstants.ERROR_PAYLOAD_NOT_SIMILAR);
                    throw new IdempotencyValidationException(IdempotencyConstants
                            .ERROR_PAYLOAD_NOT_SIMILAR);
                }
            } else {
                log.error(IdempotencyConstants.ERROR_AFTER_ALLOWED_TIME);
                throw new IdempotencyValidationException(IdempotencyConstants
                        .ERROR_AFTER_ALLOWED_TIME);
            }
        } else {
            log.error(IdempotencyConstants.ERROR_MISMATCHING_CLIENT_ID);
            throw new IdempotencyValidationException(IdempotencyConstants.ERROR_MISMATCHING_CLIENT_ID);
        }
    }

    /**
     * Method to get the Idempotency Attribute Name store in consent Attributes.
     *
     * @param resourcePath     Resource Path
     * @return idempotency Attribute Name.
     */
    public static String getIdempotencyAttributeName(String resourcePath) {
        return "IdempotencyKey";
    }

    /**
     * Method to get the Idempotency Header Name according to the request.
     *
     * @return idempotency Header Name.
     */
    public static String getIdempotencyHeaderName() {
        return "x-idempotency-key";
    }

    /**
     * Method to get created time from the Detailed Consent Resource.
     *
     * @param resourcePath     Resource Path
     * @param consentId             ConsentId
     * @return Created Time.
     */
    public static long getCreatedTimeOfPreviousRequest(String resourcePath, String consentId) {
        DetailedConsentResource consentRequest = null;
        try {
            consentRequest = consentCoreService.getDetailedConsent(consentId);
        } catch (ConsentManagementException e) {
            log.error(IdempotencyConstants.CONSENT_RETRIEVAL_ERROR, e);
            return 0L;
        }

        return consentRequest.getCreatedTime();
    }

    /**
     * Method to get payload from previous request.
     *
     * @param resourcePath     Resource Path
     * @param consentId             ConsentId
     * @return Map containing the payload.
     */
    public static String getPayloadOfPreviousRequest(String resourcePath, String consentId) {
        DetailedConsentResource consentRequest = null;
        try {
            consentRequest = consentCoreService.getDetailedConsent(consentId);
        } catch (ConsentManagementException e) {
            log.error(IdempotencyConstants.CONSENT_RETRIEVAL_ERROR, e);
            return null;
        }
        return consentRequest.getReceipt();
    }

    /**
     * Method to compare whether payloads are equal.
     *
     * @param consentManageData   Consent Manage Data Object
     * @param consentReceipt      Payload received from database
     * @return   Whether payloads are equal
     */
    public static boolean isPayloadSimilar(ConsentManageData consentManageData, String consentReceipt) {

        if (consentManageData.getPayload() == null || consentReceipt == null) {
            return false;
        }

        JsonNode expectedNode = null;
        JsonNode actualNode = null;
        try {
            expectedNode = new ObjectMapper().readTree(consentManageData.getPayload().toString());
            actualNode = new ObjectMapper().readTree(consentReceipt);
        } catch (JsonProcessingException e) {
            log.error(IdempotencyConstants.JSON_COMPARING_ERROR, e);
            return false;
        }
        return expectedNode.equals(actualNode);
    }
}
