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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * Class to handle idempotency related operations.
 */
public class IdempotencyValidator {

    private static final Log log = LogFactory.getLog(IdempotencyValidator.class);
    private static final Map<String, Object> configs = OpenBankingConfigParser.getInstance().getConfiguration();
    private static final ConsentCoreService consentCoreService = ConsentExtensionsDataHolder.getInstance()
            .getConsentCoreService();

    /**
     * Method to check whether the request is idempotent.
     * This method will first check whether idempotency validation is enabled. Then it will check whether the
     * idempotency key exists in the database and whether the request is received within the allowed time.
     *
     * @param idempotencyKeyName    Idempotency Key Name
     * @param idempotencyKeyValue    Idempotency Key Value
     * @param request                Request Payload
     * @return  IdempotencyValidationResult
     */
    public static IdempotencyValidationResult validateIdempotency(String idempotencyKeyName, String idempotencyKeyValue,
                                                                  String request, String clientId)
            throws ConsentManagementException {

        if (Boolean.parseBoolean((configs.get(IdempotencyConstants.IDEMPOTENCY_IS_ENABLED)).toString())) {
            // If idempotency key name, value or request is empty then cannot proceed with idempotency validation
            if (idempotencyKeyName == null || idempotencyKeyValue == null || request.isEmpty() || clientId == null) {
                log.debug("Idempotency Key , NameValue, Client ID or Request is empty. Hence cannot proceed with " +
                        "idempotency validation");
                return new IdempotencyValidationResult(false, false, null, null);
            }
            try {
                // Retrieve consent ids that have the idempotency key name and value as attribute
                ArrayList<String> consentIds = getConsentIdsFromIdempotencyKey(idempotencyKeyName,
                        idempotencyKeyValue);
                // Check whether the consent id list is not empty
                if (isListNotEmpty(consentIds)) {
                    log.debug(String.format("Idempotency Key  %s exists in the database. Hence this is an idempotent" +
                            " request", idempotencyKeyValue));
                    for (String consentId : consentIds) {
                        DetailedConsentResource consentRequest = consentCoreService.getDetailedConsent(consentId);
                        if (consentRequest != null) {
                            // Compare the client ID sent in the request and client id retrieved from the database
                            // to validate whether the request is received from the same client
                            if (isClientIdsMatching(clientId, consentRequest.getClientID())) {
                                // Check whether difference between two dates is less than the configured time
                                if (isRequestReceivedWithinAllowedTime(consentRequest.getCreatedTime())) {
                                    // Compare whether JSON payloads are equal
                                    if (isJSONPayloadSimilar(consentRequest.getReceipt(), request)) {
                                        log.debug("Payloads are similar and request received within allowed time." +
                                                " Hence this is a valid idempotent request");
                                        return new IdempotencyValidationResult(true, true,
                                                consentRequest, consentId);
                                    } else {
                                        log.debug(IdempotencyConstants.ERROR_PAYLOAD_NOT_SIMILAR);
                                        throw new ConsentManagementException(IdempotencyConstants
                                                .ERROR_PAYLOAD_NOT_SIMILAR);
                                    }
                                } else {
                                    log.debug(IdempotencyConstants.ERROR_AFTER_ALLOWED_TIME);
                                    throw new ConsentManagementException(IdempotencyConstants
                                            .ERROR_AFTER_ALLOWED_TIME);
                                }
                            } else {
                                log.debug(IdempotencyConstants.ERROR_MISMATCHING_CLIENT_ID);
                                throw new ConsentManagementException(IdempotencyConstants.ERROR_MISMATCHING_CLIENT_ID);
                            }
                        } else {
                            log.debug(IdempotencyConstants.ERROR_NO_CONSENT_DETAILS);
                            throw new ConsentManagementException(IdempotencyConstants.ERROR_NO_CONSENT_DETAILS);
                        }
                    }
                }
            } catch (IOException e) {
                log.error(IdempotencyConstants.JSON_COMPARING_ERROR, e);
                throw new ConsentManagementException(IdempotencyConstants.JSON_COMPARING_ERROR);
            }
        }
        return new IdempotencyValidationResult(false, false, null, null);
    }

    /**
     * Method to retrieve the consent ids that have the idempotency key name and value as attribute.
     *
     * @param idempotencyKeyName     Idempotency Key Name
     * @param idempotencyKeyValue    Idempotency Key Value
     * @return   List of consent ids
     */
    private static ArrayList<String> getConsentIdsFromIdempotencyKey(String idempotencyKeyName,
                                                                     String idempotencyKeyValue) {
        try {
            return consentCoreService.getConsentIdByConsentAttributeNameAndValue(
                    idempotencyKeyName, idempotencyKeyValue);
        } catch (ConsentManagementException e) {
            log.debug("No consent ids found for the idempotency key value");
            return new ArrayList<>();
        }
    }

    /**
     * Method to check whether the consent ID list is not empty. If idempotency key exists in the database then
     * the consent Id list will be not empty.
     *
     * @param consentIds   List of consentIds
     * @return    Whether the list is not empty
     */
    private static boolean isListNotEmpty(ArrayList<String> consentIds) {
        return !consentIds.isEmpty();
    }

    /**
     * Method to compare the client ID sent in the request and client id retrieved from the database.
     *
     * @param requestClientID     Client ID sent in the request
     * @param dbClientId          client ID retrieved from the database
     * @return   Whether JSON client Ids are equal
     */
    private static boolean isClientIdsMatching(String requestClientID, String dbClientId) {

        return requestClientID.equals(dbClientId);
    }

    /**
     * Method to compare whether JSON payloads are equal.
     *
     * @param jsonString1     JSON payload retrieved from database
     * @param jsonString2     JSON payload received from current request
     * @return   Whether JSON payloads are equal
     * @throws IOException If an error occurs while comparing JSON payloads
     */
    private static boolean isJSONPayloadSimilar(String jsonString1, String jsonString2) throws IOException {

        JsonNode expectedNode = new ObjectMapper().readTree(jsonString1);
        JsonNode actualNode = new ObjectMapper().readTree(jsonString2);
        return expectedNode.equals(actualNode);
    }

    /**
     * Method to check whether difference between two dates is less than the configured time.
     *
     * @param createdTime Created Time of the request
     * @return  Whether the request is received within allowed time
     */
    protected static boolean isRequestReceivedWithinAllowedTime(long createdTime) {

        if (createdTime == 0L) {
            return false;
        }
        String allowedTimeDuration = (String) configs.get(IdempotencyConstants.IDEMPOTENCY_ALLOWED_TIME);
        if (allowedTimeDuration != null) {
            OffsetDateTime createdDate = OffsetDateTime.parse(convertToISO8601(createdTime));
            OffsetDateTime currDate = OffsetDateTime.now(createdDate.getOffset());

            long diffInHours = Duration.between(createdDate, currDate).toMinutes();
            return diffInHours <= Long.parseLong(allowedTimeDuration);
        } else {
            log.error("Idempotency Allowed duration is null");
            return false;
        }
    }

    /**
     * Convert long date values to ISO 8601 format.
     * @param dateValue     Date value
     * @return ISO 8601 formatted date
     */
    public static String convertToISO8601(long dateValue) {

        DateFormat simple = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        Date simpleDateVal = new Date(dateValue * 1000);
        return simple.format(simpleDateVal);
    }
}
