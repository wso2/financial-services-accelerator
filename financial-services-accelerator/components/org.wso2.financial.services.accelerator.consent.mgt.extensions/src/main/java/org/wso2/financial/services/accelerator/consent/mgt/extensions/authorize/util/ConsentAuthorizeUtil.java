/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Locale;

/**
 * Util class for consent authorize operations.
 */
public class ConsentAuthorizeUtil {

    private static final Log log = LogFactory.getLog(ConsentAuthorizeUtil.class);
    private static final FinancialServicesConfigParser configParser = FinancialServicesConfigParser.getInstance();

    /**
     * Method to extract request object from query params.
     *
     * @param spQueryParams Query params
     * @return requestObject
     * @throws ConsentException Consent Exception
     */
    public static String extractRequestObject(String spQueryParams) throws ConsentException {

        if (StringUtils.isNotBlank(spQueryParams)) {
            String requestObject = null;
            String[] spQueries = spQueryParams.split("&");
            for (String param : spQueries) {
                if (param.contains("request=")) {
                    requestObject = (param.substring("request=".length())).replaceAll(
                            "\\r\\n|\\r|\\n|%20", "");
                    break;
                }
            }
            if (requestObject != null) {
                return requestObject;
            }
        }
        throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Error while parsing the request object.");
    }

    /**
     * Method to extract the consent id from the request.
     * @param requestObject Request object
     * @return consentId
     * @throws ConsentException Consent Exception
     */
    public static String extractConsentId(String requestObject) throws ConsentException {

        String authFlowConsentIdSource = configParser.getAuthFlowConsentIdSource();

        try {
            if (FinancialServicesConstants.REQUEST_OBJECT.equals(authFlowConsentIdSource)) {

                // validate request object and get the payload
                String requestObjectPayload = decodeRequestObjectPayload(requestObject);
                JSONObject payload = new JSONObject(requestObjectPayload);

                // get consent id from the request object
                if (payload.has(ConsentExtensionConstants.CLAIMS)) {
                    JSONObject claims = payload.getJSONObject(ConsentExtensionConstants.CLAIMS);
                    return FinancialServicesUtils.getConsentIdFromEssentialClaims(claims.toString());
                }
            }
        } catch (JSONException | JsonProcessingException e) {
            log.error("Payload is not a JSON object", e);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Payload is not a JSON object");
        }

        if (FinancialServicesConstants.REQUEST_PARAM.equals(authFlowConsentIdSource)) {
            return getConsentIdFromRequestParam(ConsentAuthorizeUtil.getRequestObjectJson(requestObject));
        }

        return null;
    }

    /**
     * Method to extract the consent id from the request parameters.
     *
     * @param requestParameters Request parameters
     * @return consentId
     */
    public static String getConsentIdFromRequestParam(JSONObject requestParameters) {

        String key = configParser.getConsentIdExtractionKey();

        // TODO: need to support other request parameters based on requirements
        if (key.equals(FinancialServicesConstants.SCOPE)) {
            String scope = (String) requestParameters.get(FinancialServicesConstants.SCOPE);
            if (StringUtils.isNotBlank(scope)) {
                return FinancialServicesUtils.getConsentIdFromScopesRequestParam(
                        scope.split(FinancialServicesConstants.SPACE_SEPARATOR));
            }
        }
        return null;
    }

    /**
     * Extracts a top-level string field from the request object payload.
     * Returns an empty string if the field is not found or if the payload is invalid.
     *
     * @param requestObject The signed JWT request object (in format header.payload.signature)
     * @param fieldName     The name of the field to extract
     * @return The string value of the field, or an empty string if the field is not found
     */
    public static String extractField(String requestObject, String fieldName) {
        try {
            String payloadJson = decodeRequestObjectPayload(requestObject);
            JSONObject payload = new JSONObject(payloadJson);
            return payload.optString(fieldName, "");
        } catch (JSONException e) {
            log.warn("Failed to parse JWT payload as JSON or extract field: " +
                    fieldName.replaceAll("[\r\n]", ""), e);
            return "";
        }
    }

    /**
     * Method to extract the request object payload and convert it to a JSON object.
     *
     * @param requestObject Request object
     * @return requestObjectJson
     * @throws ConsentException Consent Exception
     */
    public static JSONObject getRequestObjectJson(String requestObject) throws ConsentException {

        String payload = decodeRequestObjectPayload(requestObject);
        JSONObject requestObjectJson;
        try {
            requestObjectJson = new JSONObject(payload);
        } catch (JSONException e) {
            requestObjectJson = new JSONObject();
        }
        return requestObjectJson;
    }

    /**
     * Method to decode the request object payload.
     *
     * @param requestObject
     * @return
     */
    private static String decodeRequestObjectPayload(String requestObject) {
        String[] jwtTokenValues = requestObject.split("\\.");
        if (jwtTokenValues.length == 3) {
            return new String(Base64.getUrlDecoder().decode(jwtTokenValues[1]),
                    StandardCharsets.UTF_8);
        } else {
            log.error("request object is not signed JWT");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "request object is not signed JWT");
        }
    }

    /**
     * Method that consists the implementation for the validation of  payload and the consent,
     * this method also invokes the relevant methods to populate data for each flow.
     *
     * @param consentResource Consent Resource parameter containing consent related information retrieved
     *                        from database.
     * @return ConsentDataJson array
     */
    public static JSONObject getConsentDataForPreInitiatedConsent(ConsentResource consentResource)
            throws ConsentException {

        JSONObject consentDataJSON = new JSONObject();
        try {
            // Append consent type
            consentDataJSON.put(ConsentAuthorizeConstants.TYPE, consentResource.getConsentType());

            String receiptString = consentResource.getReceipt();
            JSONObject receipt = new JSONObject(receiptString);

            // Checks if 'data' object is present in the receipt
            if (receipt.has("Data")) {
                JSONObject data = receipt.getJSONObject("Data");

                String type = consentResource.getConsentType();
                switch (type) {
                    case ConsentExtensionConstants.ACCOUNTS:
                        populateAccountData(data, consentDataJSON);
                        break;
                    case ConsentExtensionConstants.PAYMENTS:
                        populatePaymentData(data, consentDataJSON);
                        break;
                    case ConsentExtensionConstants.FUNDS_CONFIRMATIONS:
                        populateCofData(data, consentDataJSON);
                        break;
                    default:
                        break;
                }
            } else {
                log.error("Data Object is missing");
                throw new ConsentException(ResponseStatus.BAD_REQUEST, "Data Object is missing");
            }
        } catch (JSONException e) {
            log.error("Payload is not in JSON Format", e);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Payload is not in JSON Format");
        }
        return consentDataJSON;
    }

    /**
     * Returns consent data to be displayed based on the requested scopes.
     *
     * @param scope Scopes given in the request
     * @return ConsentDataJson array
     */
    public static JSONObject getConsentDataForScope(String scope)
            throws ConsentException {

        JSONObject consentDataJSON = new JSONObject();
        JSONArray displayValues = new JSONArray();

        // Convert space-separated scopes into JSON array
        if (scope != null && !scope.trim().isEmpty()) {
            String[] scopeItems = scope.trim().split("\\s+");
            for (String item : scopeItems) {
                // Skip openid scope since it is not relevant to the consent.
                if (ConsentExtensionConstants.OPENID_SCOPE.equals(item)) {
                    continue;
                }
                displayValues.put(item);
            }
        }

        JSONArray permissions = new JSONArray();
        if (!displayValues.isEmpty()) {
            JSONObject permission = new JSONObject();
            permission.put(ConsentAuthorizeConstants.DISPLAY_VALUES, displayValues);
            permissions.put(permission);
        }

        consentDataJSON.put(ConsentAuthorizeConstants.PERMISSIONS, permissions);

        return consentDataJSON;
    }

    /**
     * Populate Domestic and international Payment Details.
     *
     * @param data            data request from the request
     * @param consentDataJSON Consent information
     */
    private static void populatePaymentData(JSONObject data, JSONObject consentDataJSON) {

        JSONObject basicConsentData = new JSONObject();

        if (data.has(ConsentExtensionConstants.INITIATION)) {
            JSONObject initiation = data.getJSONObject(ConsentExtensionConstants.INITIATION);

            //Adding InstructionIdentification
            if (initiation.has(ConsentExtensionConstants.INSTRUCTION_IDENTIFICATION)) {
                JSONArray identificationArray = new JSONArray();
                identificationArray.put(initiation.getString(ConsentExtensionConstants.INSTRUCTION_IDENTIFICATION));

                basicConsentData.put(ConsentExtensionConstants.INSTRUCTION_IDENTIFICATION_TITLE, identificationArray);
            }

            //Adding EndToEndIdentification
            if (initiation.has(ConsentExtensionConstants.END_TO_END_IDENTIFICATION)) {
                JSONArray endToEndIdentificationArray = new JSONArray();
                endToEndIdentificationArray
                        .put(initiation.getString(ConsentExtensionConstants.END_TO_END_IDENTIFICATION));

                basicConsentData.put(ConsentExtensionConstants.END_TO_END_IDENTIFICATION_TITLE,
                        endToEndIdentificationArray);
            }

            //Adding InstructedAmount
            JSONObject instructedAmount = initiation.getJSONObject(ConsentExtensionConstants.INSTRUCTED_AMOUNT);
            JSONArray instructedAmountArray = new JSONArray();

            if (instructedAmount.getString(ConsentExtensionConstants.AMOUNT_TITLE) != null) {
                instructedAmountArray.put(ConsentExtensionConstants.AMOUNT_TITLE + " : " +
                        instructedAmount.getString(ConsentExtensionConstants.AMOUNT));
            }

            if (instructedAmount.getString(ConsentExtensionConstants.CURRENCY) != null) {
                instructedAmountArray.put(ConsentExtensionConstants.CURRENCY_TITLE + " : " +
                        instructedAmount.getString(ConsentExtensionConstants.CURRENCY));
            }

            basicConsentData.put(ConsentExtensionConstants.INSTRUCTED_AMOUNT_TITLE, instructedAmountArray);

            // Adding Debtor Account
            if (initiation.has(ConsentExtensionConstants.DEBTOR_ACC)) {
                populateDebtorAccount(initiation, basicConsentData, consentDataJSON);
            }
            // Adding Creditor Account
            populateCreditorAccount(initiation, basicConsentData);

            consentDataJSON.put(ConsentAuthorizeConstants.BASIC_CONSENT_DATA, basicConsentData);
        }
    }

    /**
     * Populate account Details.
     *
     * @param data            data request from the request
     * @param consentDataJSON Consent information
     */
    private static void populateAccountData(JSONObject data, JSONObject consentDataJSON) throws ConsentException {

        JSONArray permissions = new JSONArray();
        JSONObject basicConsentData = new JSONObject();

        //Adding Permissions
        JSONArray displayValues = data.getJSONArray(ConsentExtensionConstants.PERMISSIONS);
        if (displayValues != null) {
            JSONObject permission = new JSONObject();
            permission.put(ConsentAuthorizeConstants.DISPLAY_VALUES, displayValues);
            permissions.put(permission);
        }
        consentDataJSON.put(ConsentAuthorizeConstants.PERMISSIONS, permissions);

        //Adding Expiration Date Time
        String expirationDate = data.getString(ConsentExtensionConstants.EXPIRATION_DATE);
        if (expirationDate != null) {
            if (!validateExpiryDateTime(expirationDate)) {
                log.error(ConsentAuthorizeConstants.CONSENT_EXPIRED);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ConsentAuthorizeConstants.CONSENT_EXPIRED);
            }
            JSONArray expiryArray = new JSONArray();
            expiryArray.put(expirationDate);

            basicConsentData.put(ConsentExtensionConstants.EXPIRATION_DATE_TITLE, expiryArray);
        }

        //Adding Transaction From Date Time
        String fromDateTime = data.getString(ConsentExtensionConstants.TRANSACTION_FROM_DATE);
        if (fromDateTime != null) {
            JSONArray fromDateTimeArray = new JSONArray();
            fromDateTimeArray.put(fromDateTime);

            basicConsentData.put(ConsentExtensionConstants.TRANSACTION_FROM_DATE_TITLE, fromDateTimeArray);
        }

        //Adding Transaction To Date Time
        String toDateTime = data.getString(ConsentExtensionConstants.TRANSACTION_TO_DATE);
        if (toDateTime != null) {
            JSONArray toDateTimeArray = new JSONArray();
            toDateTimeArray.put(toDateTime);

            basicConsentData.put(ConsentExtensionConstants.TRANSACTION_TO_DATE_TITLE, toDateTimeArray);
        }

        consentDataJSON.put(ConsentAuthorizeConstants.BASIC_CONSENT_DATA, basicConsentData);
        consentDataJSON.put(ConsentAuthorizeConstants.ALLOW_MULTIPLE_ACCOUNTS, true);
    }

    /**
     * Populate funds confirmation Details.
     *
     * @param initiation      data from the request
     * @param consentDataJSON Consent information
     */
    private static void populateCofData(JSONObject initiation, JSONObject consentDataJSON) throws ConsentException {

        JSONObject basicConsentData = new JSONObject();

        //Adding Expiration Date Time
        if (initiation.getString(ConsentExtensionConstants.EXPIRATION_DATE) != null) {

            if (!validateExpiryDateTime(initiation.getString(ConsentExtensionConstants.EXPIRATION_DATE))) {
                log.error(ConsentAuthorizeConstants.CONSENT_EXPIRED);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ConsentAuthorizeConstants.CONSENT_EXPIRED);
            }

            String expiry = initiation.getString(ConsentExtensionConstants.EXPIRATION_DATE);
            JSONArray expiryArray = new JSONArray();
            expiryArray.put(expiry);

            basicConsentData.put(ConsentExtensionConstants.EXPIRATION_DATE_TITLE, expiryArray);
        } else {
            JSONArray expiryArray = new JSONArray();
            expiryArray.put(ConsentExtensionConstants.OPEN_ENDED_AUTHORIZATION);

            JSONObject jsonElementExpiry = new JSONObject();

            basicConsentData.put(ConsentExtensionConstants.EXPIRATION_DATE_TITLE, expiryArray);
        }

        if (initiation.get(ConsentExtensionConstants.DEBTOR_ACC) != null) {
            //Adding Debtor Account
            populateDebtorAccount(initiation, basicConsentData, consentDataJSON);
        }

        consentDataJSON.put(ConsentAuthorizeConstants.BASIC_CONSENT_DATA, basicConsentData);
    }

    /**
     * Method to add debtor account details to consent data to send it to the consent page.
     *
     * @param initiation      Initiation object from the request
     * @param basicConsentData Consent information object
     */
    public static void populateDebtorAccount(JSONObject initiation, JSONObject basicConsentData,
                                             JSONObject consentDataJSON) {
        if (initiation.get(ConsentExtensionConstants.DEBTOR_ACC) != null) {
            JSONObject debtorAccount = initiation.getJSONObject(ConsentExtensionConstants.DEBTOR_ACC);
            JSONArray debtorAccountArray = new JSONArray();
            JSONObject initiatedAccount = new JSONObject();

            //Adding Debtor Account Scheme Name
            if (debtorAccount.getString(ConsentExtensionConstants.SCHEME_NAME) != null) {
                debtorAccountArray.put(ConsentExtensionConstants.SCHEME_NAME_TITLE + " : " +
                        debtorAccount.getString(ConsentExtensionConstants.SCHEME_NAME));
                initiatedAccount.put(ConsentExtensionConstants.SCHEME_NAME,
                        debtorAccount.getString(ConsentExtensionConstants.SCHEME_NAME));
            }

            //Adding Debtor Account Name
            if (debtorAccount.has(ConsentExtensionConstants.NAME) &&
                    debtorAccount.getString(ConsentExtensionConstants.NAME) != null) {
                debtorAccountArray.put(ConsentExtensionConstants.NAME_TITLE + " : " +
                        debtorAccount.getString(ConsentExtensionConstants.NAME));
                initiatedAccount.put(ConsentAuthorizeConstants.DISPLAY_NAME,
                        debtorAccount.getString(ConsentExtensionConstants.NAME));
            }

            //Adding Debtor Account Secondary Identification
            if (debtorAccount.has(ConsentExtensionConstants.SECONDARY_IDENTIFICATION) &&
                    debtorAccount.getString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION) != null) {
                debtorAccountArray.put(ConsentExtensionConstants.SECONDARY_IDENTIFICATION_TITLE + " : " +
                        debtorAccount.getString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION));
                initiatedAccount.put(ConsentExtensionConstants.SECONDARY_IDENTIFICATION,
                        debtorAccount.getString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION));
            }

            //Adding Debtor Account Identification
            if (debtorAccount.getString(ConsentExtensionConstants.IDENTIFICATION) != null) {
                debtorAccountArray.put(ConsentExtensionConstants.IDENTIFICATION_TITLE + " : " +
                        debtorAccount.getString(ConsentExtensionConstants.IDENTIFICATION));
                initiatedAccount.put(ConsentAuthorizeConstants.ACCOUNT_ID,
                        debtorAccount.getString(ConsentExtensionConstants.IDENTIFICATION));
            }

            basicConsentData.put(ConsentExtensionConstants.DEBTOR_ACC_TITLE, debtorAccountArray);

            // Add as initiated account
            JSONArray initiatedAccounts = new JSONArray();
            initiatedAccounts.put(initiatedAccount);
            consentDataJSON.put(ConsentAuthorizeConstants.INITIATED_ACCOUNTS_FOR_CONSENT, initiatedAccounts);
        }
    }


    /**
     * Method to add debtor account details to consent data to send it to the consent page.
     *
     * @param initiation      Initiation object from the request
     * @param basicConsentData Consent information object
     */
    public static void populateCreditorAccount(JSONObject initiation, JSONObject basicConsentData) {
        if (initiation.get(ConsentExtensionConstants.CREDITOR_ACC) != null) {
            JSONObject creditorAccount = initiation.getJSONObject(ConsentExtensionConstants.CREDITOR_ACC);
            JSONArray creditorAccountArray = new JSONArray();
            //Adding Debtor Account Scheme Name
            if (creditorAccount.getString(ConsentExtensionConstants.SCHEME_NAME) != null) {
                creditorAccountArray.put(ConsentExtensionConstants.SCHEME_NAME_TITLE + " : " +
                        creditorAccount.getString(ConsentExtensionConstants.SCHEME_NAME));
            }
            //Adding Debtor Account Identification
            if (creditorAccount.getString(ConsentExtensionConstants.IDENTIFICATION) != null) {
                creditorAccountArray.put(ConsentExtensionConstants.IDENTIFICATION_TITLE + " : " +
                        creditorAccount.getString(ConsentExtensionConstants.IDENTIFICATION));
            }
            //Adding Debtor Account Name
            if (creditorAccount.getString(ConsentExtensionConstants.NAME) != null) {
                creditorAccountArray.put(ConsentExtensionConstants.NAME_TITLE + " : " +
                        creditorAccount.getString(ConsentExtensionConstants.NAME));
            }
            //Adding Debtor Account Secondary Identification
            if (creditorAccount.getString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION) != null) {
                creditorAccountArray.put(ConsentExtensionConstants.SECONDARY_IDENTIFICATION_TITLE + " : " +
                        creditorAccount.getString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION));
            }

            basicConsentData.put(ConsentExtensionConstants.CREDITOR_ACC_TITLE, creditorAccountArray);
        }
    }

    /**
     * Method to append Dummy data for Account ID. Ideally should be separate step calling accounts service
     *
     * @return accountsJSON
     */
    public static JSONArray appendDummyAccountID() {

        JSONArray accountsJSON = new JSONArray();
        JSONObject accountOne = new JSONObject();
        accountOne.put(ConsentAuthorizeConstants.ACCOUNT_ID, "30080012343456");
        accountOne.put(ConsentAuthorizeConstants.DISPLAY_NAME, "Salary Saver Account");
        accountOne.put(ConsentAuthorizeConstants.SELECTED, false);

        accountsJSON.put(accountOne);

        JSONObject accountTwo = new JSONObject();
        accountTwo.put(ConsentAuthorizeConstants.ACCOUNT_ID, "30080098763459");
        accountTwo.put(ConsentAuthorizeConstants.DISPLAY_NAME, "Max Bonus Account");
        accountTwo.put(ConsentAuthorizeConstants.SELECTED, false);

        accountsJSON.put(accountTwo);

        return accountsJSON;

    }

    /**
     * Check if the expiry date time of the consent containsKey elapsed.
     *
     * @param expiryDate The expiry date/time of consent
     * @return boolean result of validation
     */
    public static boolean validateExpiryDateTime(String expiryDate) throws ConsentException {

        try {
            OffsetDateTime expDate = OffsetDateTime.parse(expiryDate);
            if (log.isDebugEnabled()) {
                log.debug(String.format(ConsentAuthorizeConstants.DATE_PARSE_MSG, expDate, OffsetDateTime.now()));
            }
            return OffsetDateTime.now().isBefore(expDate);
        } catch (DateTimeParseException e) {
            log.error(ConsentAuthorizeConstants.EXP_DATE_PARSE_ERROR, e);
            throw new ConsentException(ResponseStatus.BAD_REQUEST,
                    ConsentAuthorizeConstants.ACC_CONSENT_RETRIEVAL_ERROR);
        }
    }

    /**
     * Method to create the consent receipt using the request object.
     *
     * @param requestObject request object
     * @return Consent receipt
     */
    @SuppressFBWarnings("IMPROPER_UNICODE")
    public static String getReceiptFromRequestObject(String requestObject) {

        // Extract the space-separated scopes from the request
        String scope = extractField(requestObject, FinancialServicesConstants.SCOPE);

        JSONArray permissions = new JSONArray();
        if (scope != null && !scope.trim().isEmpty()) {
            String[] scopeItems = scope.trim().split("\\s+");
            for (String item : scopeItems) {
                // Skip openid scope since it is not relevant to the consent.
                if (ConsentExtensionConstants.OPENID_SCOPE.equals(item)) {
                    continue;
                }
                permissions.put(item.toUpperCase(Locale.ROOT));
            }
        }

        // Default expiration timestamp (current time + 1 hour)
        OffsetDateTime expirationTime = OffsetDateTime.now(ZoneId.systemDefault()).plusHours(1);
        String expirationDateTime = expirationTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        // Build the JSON object
        JSONObject receiptData = new JSONObject();
        receiptData.put(ConsentExtensionConstants.PERMISSIONS, permissions);
        receiptData.put(ConsentExtensionConstants.EXPIRATION_DATE, expirationDateTime);

        JSONObject receipt = new JSONObject();
        receipt.put(ConsentExtensionConstants.DATA, receiptData);

        return receipt.toString();
    }
}
