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
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
     * @throws IdempotencyValidationException    If an error occurs while validating idempotency
     */
    public IdempotencyValidationResult validateIdempotency(ConsentManageData consentManageData)
            throws IdempotencyValidationException {

        if (!OpenBankingConfigParser.getInstance().isIdempotencyValidationEnabled()) {
            return new IdempotencyValidationResult(false, false);
        }
        // If request is empty then cannot proceed with idempotency validation
        if (consentManageData.getPayload() == null) {
            log.error("Request payload is empty. Hence cannot proceed with idempotency validation");
            return new IdempotencyValidationResult(false, false);
        }
        // If client id is empty then cannot proceed with idempotency validation
        if (StringUtils.isBlank(consentManageData.getClientId())) {
            log.error("Client ID is empty. Hence cannot proceed with idempotency validation");
            return new IdempotencyValidationResult(false, false);
        }
        String idempotencyKeyValue = consentManageData.getHeaders().get(getIdempotencyHeaderName());
        // If idempotency key value is empty then cannot proceed with idempotency validation
        if (StringUtils.isBlank(idempotencyKeyValue)) {
            log.error("Idempotency Key Valueis empty. Hence cannot proceed with idempotency validation");
            return new IdempotencyValidationResult(false, false);
        }
        try {
            String idempotencyKeyName = getIdempotencyAttributeName(consentManageData.getRequestPath());
            if (!IdempotencyConstants.EMPTY_OBJECT.equals(consentManageData.getPayload().toString())) {
                // Retrieve consent ids that have the idempotency key name and value as attribute
                List<String> consentIds = IdempotencyValidationUtils
                        .getConsentIdsFromIdempotencyKey(idempotencyKeyName, idempotencyKeyValue);
                // Check whether the consent id list is not empty. If idempotency key exists in the database then
                // the consent Id list will be not empty.
                if (!consentIds.isEmpty()) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Idempotency Key  %s exists in the database. Hence this is an" +
                                " idempotent request", idempotencyKeyValue));
                    }
                    for (String consentId : consentIds) {
                        DetailedConsentResource consentResource = consentCoreService.getDetailedConsent(consentId);
                        if (consentResource != null) {
                            return validateIdempotencyConditions(consentManageData, consentResource);
                        } else {
                            String errorMsg = String.format(IdempotencyConstants.ERROR_NO_CONSENT_DETAILS, consentId);
                            log.error(errorMsg);
                            throw new IdempotencyValidationException(errorMsg);
                        }
                    }
                }
            } else {
                return validateIdempotencyWithoutPayload(consentManageData, idempotencyKeyName, idempotencyKeyValue);
            }
        } catch (IOException e) {
            log.error(IdempotencyConstants.JSON_COMPARING_ERROR, e);
            throw new IdempotencyValidationException(IdempotencyConstants.JSON_COMPARING_ERROR);
        } catch (ConsentManagementException e) {
            log.error(IdempotencyConstants.CONSENT_RETRIEVAL_ERROR, e);
            return new IdempotencyValidationResult(true, false);
        }
        return new IdempotencyValidationResult(false, false);
    }

    /**
     * Method to check whether the idempotency conditions are met for requests without payload.
     * This method will validate the following conditions.
     *  - Whether the idempotency key value is different for the same consent id
     *  - Whether the client id sent in the request and client id retrieved from the database are equal
     *  - Whether the difference between two dates is less than the configured time
     *  - Whether payloads are equal
     *
     * @param consentManageData        Consent Manage Data
     * @param idempotencyKeyName       Idempotency Key Name
     * @param idempotencyKeyValue      Idempotency Key value
     * @return  IdempotencyValidationResult
     */
    protected IdempotencyValidationResult validateIdempotencyWithoutPayload(ConsentManageData consentManageData,
                                                                          String idempotencyKeyName,
                                                                          String idempotencyKeyValue)
            throws IdempotencyValidationException, IOException, ConsentManagementException {

        // Retrieve consent ids and idempotency key values that have the idempotency key name
        Map<String, String> attributes = IdempotencyValidationUtils.getAttributesFromIdempotencyKey(idempotencyKeyName);
        // Check whether the attributes map is not empty. If idempotency key exists in the database then
        // the consent Id list will be not empty.
        if (!attributes.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Idempotency Key  %s exists in the database. Hence this is an" +
                        " idempotent request", idempotencyKeyValue));
            }
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                // If the idempotency key value is different for the same consent id then it is not a valid idempotent
                if (consentManageData.getRequestPath().contains(entry.getKey()) &&
                        !idempotencyKeyValue.equals(entry.getValue())) {
                    throw new IdempotencyValidationException(IdempotencyConstants.SAME_CONSENT_ID_ERROR);
                }
                DetailedConsentResource consentRequest = consentCoreService.getDetailedConsent(entry.getKey());
                if (consentRequest != null) {
                    return validateIdempotencyConditions(consentManageData, consentRequest);
                } else {
                    String errorMsg = String.format(IdempotencyConstants.ERROR_NO_CONSENT_DETAILS, entry.getKey());
                    log.error(errorMsg);
                    throw new IdempotencyValidationException(errorMsg);
                }
            }

        }
        return new IdempotencyValidationResult(false, false);
    }

    /**
     * Method to check whether the idempotency conditions are met.
     * This method will validate the following conditions.
     *  - Whether the client id sent in the request and client id retrieved from the database are equal
     *  - Whether the difference between two dates is less than the configured time
     *  - Whether payloads are equal
     *
     * @param consentManageData        Consent Manage Data
     * @param consentResource          Detailed Consent Resource
     * @return  IdempotencyValidationResult
     */
    private IdempotencyValidationResult validateIdempotencyConditions(ConsentManageData consentManageData,
                                                                      DetailedConsentResource consentResource)
            throws IdempotencyValidationException, IOException {
        // Compare the client ID sent in the request and client id retrieved from the database
        // to validate whether the request is received from the same client
        if (IdempotencyValidationUtils.isClientIDEqual(consentResource.getClientID(),
                consentManageData.getClientId())) {
            // Check whether difference between two dates is less than the configured time
            if (IdempotencyValidationUtils.isRequestReceivedWithinAllowedTime(getCreatedTimeOfPreviousRequest(
                    consentManageData.getRequestPath(), consentResource.getConsentID()))) {
                // Compare whether JSON payloads are equal
                if (isPayloadSimilar(consentManageData, getPayloadOfPreviousRequest(
                        consentManageData.getRequestPath(), consentResource.getConsentID()))) {
                    log.debug("Payloads are similar and request received within allowed" +
                            " time. Hence this is a valid idempotent request");
                    return new IdempotencyValidationResult(true, true,
                            consentResource, consentResource.getConsentID());
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
    public String getIdempotencyAttributeName(String resourcePath) {
        return IdempotencyConstants.IDEMPOTENCY_KEY_NAME;
    }

    /**
     * Method to get the Idempotency Header Name according to the request.
     *
     * @return idempotency Header Name.
     */
    public String getIdempotencyHeaderName() {
        return IdempotencyConstants.X_IDEMPOTENCY_KEY;
    }

    /**
     * Method to get created time from the Detailed Consent Resource.
     *
     * @param resourcePath     Resource Path
     * @param consentId             ConsentId
     * @return Created Time.
     */
    public long getCreatedTimeOfPreviousRequest(String resourcePath, String consentId) {
        DetailedConsentResource consentRequest = null;
        try {
            consentRequest = consentCoreService.getDetailedConsent(consentId);
        } catch (ConsentManagementException e) {
            log.error(IdempotencyConstants.CONSENT_RETRIEVAL_ERROR, e);
            return 0L;
        }
        if (consentRequest == null) {
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
    public String getPayloadOfPreviousRequest(String resourcePath, String consentId) {
        DetailedConsentResource consentRequest = null;
        try {
            consentRequest = consentCoreService.getDetailedConsent(consentId);
        } catch (ConsentManagementException e) {
            log.error(IdempotencyConstants.CONSENT_RETRIEVAL_ERROR, e);
            return null;
        }
        if (consentRequest == null) {
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
    public boolean isPayloadSimilar(ConsentManageData consentManageData, String consentReceipt) {

        if (consentManageData.getPayload() == null || consentReceipt == null) {
            return false;
        }

        JsonNode expectedNode = null;
        JsonNode actualNode = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            expectedNode = mapper.readTree(consentManageData.getPayload().toString());
            actualNode = mapper.readTree(consentReceipt);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Expected payload for idempotent request is: %s. But actual payload " +
                        "received is %s", expectedNode.toString(), actualNode.toString()));
            }
        } catch (JsonProcessingException e) {
            log.error(IdempotencyConstants.JSON_COMPARING_ERROR, e);
            return false;
        }
        return expectedNode.equals(actualNode);
    }
}
