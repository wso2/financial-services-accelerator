/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.ConsentValidator;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.model.ConsentValidateData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.model.ConsentValidationResult;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

/**
 * Consent validator default implementation.
 */
public class DefaultConsentValidator implements ConsentValidator {

    private static final Log log = LogFactory.getLog(DefaultConsentValidator.class);
    private static final String ACCOUNTS_BULK_REGEX = "/accounts";
    private static final String ACCOUNTS_REGEX = "/accounts/[^/?]*";
    private static final String TRANSACTIONS_REGEX = "/accounts/[^/?]*/transactions";
    private static final String BALANCES_REGEX = "/accounts/[^/?]*/balances";
    private static final String COF_SUBMISSION_PATH = "/funds-confirmations";
    private static final String PERMISSION_MISMATCH_ERROR = "Permission mismatch. Consent does not contain necessary " +
            "permissions";
    private static final String INVALID_URI_ERROR = "Path requested is invalid";
    private static final String CONSENT_EXPIRED_ERROR = "Provided consent is expired";
    @Override
    public void validate(ConsentValidateData consentValidateData, ConsentValidationResult consentValidationResult)
            throws ConsentException {

        if (consentValidateData.getComprehensiveConsent() == null ||
                consentValidateData.getComprehensiveConsent().getReceipt() == null) {
            log.error("Consent Details cannot be found");
            consentValidationResult.setValid(false);
            consentValidationResult.setErrorMessage("Consent Details cannot be found");
            consentValidationResult.setErrorCode(ResponseStatus.BAD_REQUEST.getReasonPhrase());
            consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        JSONObject receiptJSON;
        try {
            receiptJSON = new JSONObject(consentValidateData.getComprehensiveConsent().getReceipt());
        } catch (JSONException e) {
            log.error(e.getMessage().replaceAll("[\n\r]", ""));
            consentValidationResult.setValid(false);
            consentValidationResult.setErrorMessage(e.getMessage().replaceAll("[\n\r]", ""));
            consentValidationResult.setErrorCode(ResponseStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
            consentValidationResult.setHttpCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        //User Validation
        String userIdFromToken = FinancialServicesUtils.resolveUsernameFromUserId(consentValidateData.getUserId());
        boolean userIdMatching = false;
        ArrayList<AuthorizationResource> authResources = consentValidateData.getComprehensiveConsent()
                .getAuthorizationResources();
        for (AuthorizationResource resource : authResources) {
            if (userIdFromToken.equals(resource.getUserID())) {
                userIdMatching = true;
                break;
            }
        }

        if (!userIdMatching) {
            log.error("Invalid User Id");
            consentValidationResult.setValid(false);
            consentValidationResult.setErrorMessage("Invalid User Id");
            consentValidationResult.setErrorCode(ResponseStatus.BAD_REQUEST.getReasonPhrase());
            consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        String clientIdFromToken = consentValidateData.getClientId();
        String clientIdFromConsent = consentValidateData.getComprehensiveConsent().getClientID();
        if (clientIdFromToken == null || !clientIdFromToken.equals(clientIdFromConsent)) {
            log.error("Invalid Client Id");
            consentValidationResult.setValid(false);
            consentValidationResult.setErrorMessage("Invalid Client Id");
            consentValidationResult.setErrorCode(ResponseStatus.FORBIDDEN.getReasonPhrase());
            consentValidationResult.setHttpCode(HttpStatus.SC_FORBIDDEN);
            return;
        }

        String requestType = consentValidateData.getComprehensiveConsent().getConsentType();

        switch (requestType) {
            case ConsentExtensionConstants.ACCOUNTS:
                validateAccountSubmission(consentValidateData, receiptJSON, consentValidationResult);
                break;
            case ConsentExtensionConstants.PAYMENTS:
                validatePaymentSubmission(consentValidateData, receiptJSON, consentValidationResult);
                break;
            case ConsentExtensionConstants.FUNDS_CONFIRMATIONS:
                validateFundsConfirmationSubmission(consentValidateData, receiptJSON, consentValidationResult);
                break;
            default:
                log.error("Invalid consent type");
                consentValidationResult.setValid(false);
                consentValidationResult.setErrorMessage("Invalid consent type");
                consentValidationResult.setErrorCode(ResponseStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
                consentValidationResult.setHttpCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                return;
        }
    }

    /**
     * Validate Account Retrieval Request.
     *
     * @param consentValidateData     Object with request data
     * @param consentValidationResult Validation result object to return
     */
    private void validateAccountSubmission(ConsentValidateData consentValidateData, JSONObject receiptJSON,
                                           ConsentValidationResult consentValidationResult) {

        JSONArray permissions = receiptJSON.getJSONObject("Data").getJSONArray("Permissions");

        // Perform URI Validation.
        String uri = consentValidateData.getRequestPath();
        if (!(uri.matches(ACCOUNTS_BULK_REGEX) || uri.matches(ACCOUNTS_REGEX) || uri.matches(TRANSACTIONS_REGEX) ||
                uri.matches(BALANCES_REGEX))) {
            log.error(INVALID_URI_ERROR);
            consentValidationResult.setValid(false);
            consentValidationResult.setErrorMessage(INVALID_URI_ERROR);
            consentValidationResult.setErrorCode(ResponseStatus.UNAUTHORIZED.getReasonPhrase());
            consentValidationResult.setHttpCode(401);
            return;
        }
        String persmissionString = permissions.toString();
        if ((uri.matches(ACCOUNTS_BULK_REGEX) && !persmissionString.contains("ReadAccountsBasic")) ||
                (uri.matches(ACCOUNTS_REGEX) && !persmissionString.contains("ReadAccountsDetail")) ||
                (uri.matches(TRANSACTIONS_REGEX) && !persmissionString.contains("ReadTransactionsDetail")) ||
                (uri.matches(BALANCES_REGEX)) && !persmissionString.contains("ReadBalances")) {
            log.error(PERMISSION_MISMATCH_ERROR);
            consentValidationResult.setValid(false);
            consentValidationResult.setErrorMessage(PERMISSION_MISMATCH_ERROR);
            consentValidationResult.setErrorCode(ResponseStatus.FORBIDDEN.getReasonPhrase());
            consentValidationResult.setHttpCode(HttpStatus.SC_FORBIDDEN);
            return;
        }

        //Consent Status Validation
        if (!ConsentExtensionConstants.AUTHORIZED_STATUS
                .equals(consentValidateData.getComprehensiveConsent().getCurrentStatus())) {
            log.error("Consent is not in the correct state");
            consentValidationResult.setValid(false);
            consentValidationResult.setErrorMessage("Consent is not in the correct state");
            consentValidationResult.setErrorCode(ResponseStatus.BAD_REQUEST.getReasonPhrase());
            consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        if (isConsentExpired(receiptJSON.getJSONObject("Data").getString("ExpirationDateTime"))) {
            log.error(CONSENT_EXPIRED_ERROR);
            consentValidationResult.setValid(false);
            consentValidationResult.setErrorMessage(CONSENT_EXPIRED_ERROR);
            consentValidationResult.setErrorCode(ResponseStatus.BAD_REQUEST.getReasonPhrase());
            consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }
        consentValidationResult.setValid(true);
    }

    /**
     * Validate Funds Confirmation Retrieval Request.
     *
     * @param consentValidateData     Object with request data
     * @param consentValidationResult Validation result object to return
     */
    private static void validateFundsConfirmationSubmission(ConsentValidateData consentValidateData,
                                                            JSONObject receiptJSON,
                                                            ConsentValidationResult consentValidationResult) {

        // Perform URI Validation.
        String uri = consentValidateData.getRequestPath();
        if (!isCOFURIValid(uri)) {
            log.error("Invalid request URI");
            consentValidationResult.setValid(false);
            consentValidationResult.setErrorMessage("Invalid request URI");
            consentValidationResult.setErrorCode(ResponseStatus.UNAUTHORIZED.getReasonPhrase());
            consentValidationResult.setHttpCode(HttpStatus.SC_UNAUTHORIZED);
            return;
        }

        //Consent Status Validation
        if (!ConsentExtensionConstants.AUTHORIZED_STATUS
                .equals(consentValidateData.getComprehensiveConsent().getCurrentStatus())) {
            log.error("Consent is not in the correct state");
            consentValidationResult.setValid(false);
            consentValidationResult.setErrorMessage("Consent is not in the correct state");
            consentValidationResult.setErrorCode(ResponseStatus.BAD_REQUEST.getReasonPhrase());
            consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        //Validate whether the consent is expired
        if (isConsentExpired(receiptJSON.getJSONObject(ConsentExtensionConstants.DATA)
                .getString(ConsentExtensionConstants.EXPIRATION_DATE))) {
            log.error(CONSENT_EXPIRED_ERROR);
            consentValidationResult.setValid(false);
            consentValidationResult.setErrorMessage(CONSENT_EXPIRED_ERROR);
            consentValidationResult.setErrorCode(ResponseStatus.UNAUTHORIZED.getReasonPhrase());
            consentValidationResult.setHttpCode(HttpStatus.SC_UNAUTHORIZED);
            return;
        }


        // Check if requested consent ID in the token to initiation consent ID.
        if (consentValidateData.getConsentId() == null ||
                consentValidateData.getComprehensiveConsent().getConsentID() == null ||
                !consentValidateData.getConsentId()
                        .equals(consentValidateData.getComprehensiveConsent().getConsentID())) {
            log.error("Consent ID mismatch");
            consentValidationResult.setValid(false);
            consentValidationResult.setErrorMessage("Consent ID mismatch");
            consentValidationResult.setErrorCode(ResponseStatus.BAD_REQUEST.getReasonPhrase());
            consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        JSONObject data = consentValidateData.getPayload().getJSONObject(ConsentExtensionConstants.DATA);
        // Check if requested consent ID in the body to initiation consent ID.
        if (!data.has(ConsentExtensionConstants.CONSENT_ID) ||
                data.get(ConsentExtensionConstants.CONSENT_ID) == null ||
                !data.get(ConsentExtensionConstants.CONSENT_ID)
                        .equals(consentValidateData.getComprehensiveConsent().getConsentID())) {
            log.error("Invalid consent ID");
            consentValidationResult.setValid(false);
            consentValidationResult.setErrorMessage("Invalid consent ID");
            consentValidationResult.setErrorCode(ResponseStatus.BAD_REQUEST.getReasonPhrase());
            consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        consentValidationResult.setValid(true);

    }

    /**
     * Validate Payment Retrieval Request.
     *
     * @param consentValidateData     Object with request data
     * @param consentValidationResult Validation result object to return
     */
    private void validatePaymentSubmission(ConsentValidateData consentValidateData, JSONObject initiationJson,
                                           ConsentValidationResult consentValidationResult) {

        DetailedConsentResource detailedConsentResource = consentValidateData.getComprehensiveConsent();
        // Check if requested consent ID matches to initiation consent ID.
        if (consentValidateData.getConsentId() == null || detailedConsentResource.getConsentID() == null ||
                !consentValidateData.getConsentId().equals(detailedConsentResource.getConsentID())) {
            log.error("Consent ID mismatch");
            consentValidationResult.setErrorMessage("Consent ID mismatch");
            consentValidationResult.setErrorCode(ResponseStatus.BAD_REQUEST.getReasonPhrase());
            consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        if (!ConsentExtensionConstants.AUTHORIZED_STATUS
                .equals(consentValidateData.getComprehensiveConsent().getCurrentStatus())) {
            consentValidationResult.setErrorMessage("Consent is not in the correct state");
            consentValidationResult.setErrorCode(ResponseStatus.BAD_REQUEST.getReasonPhrase());
            consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        JSONObject submissionJson = consentValidateData.getPayload();
        JSONObject submissionData = new JSONObject();
        JSONObject submissionInitiation = new JSONObject();

        JSONObject requestInitiation = initiationJson.getJSONObject(ConsentExtensionConstants.DATA)
                .getJSONObject(ConsentExtensionConstants.INITIATION);

        if (submissionJson.has(ConsentExtensionConstants.DATA) &&
                submissionJson.get(ConsentExtensionConstants.DATA) instanceof JSONObject) {
            submissionData = submissionJson.getJSONObject(ConsentExtensionConstants.DATA);
        } else {
            log.error("Invalid Submission payload Data Object found");
            consentValidationResult.setErrorMessage("Invalid Submission payload Data Object found");
            consentValidationResult.setErrorCode(ResponseStatus.BAD_REQUEST.getReasonPhrase());
            consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        // Check if requested consent ID in the body to initiation consent ID.
        if (!submissionData.has(ConsentExtensionConstants.CONSENT_ID) ||
                submissionData.get(ConsentExtensionConstants.CONSENT_ID) == null ||
                !submissionData.get(ConsentExtensionConstants.CONSENT_ID)
                        .equals(detailedConsentResource.getConsentID())) {
            log.error("Invalid consent ID");
            consentValidationResult.setErrorMessage("Invalid consent ID");
            consentValidationResult.setErrorCode(ResponseStatus.BAD_REQUEST.getReasonPhrase());
            consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        if (submissionData.has(ConsentExtensionConstants.INITIATION) &&
                submissionData.get(ConsentExtensionConstants.INITIATION) instanceof JSONObject) {
            submissionInitiation = submissionData.getJSONObject(ConsentExtensionConstants.INITIATION);
        } else {
            log.error("Invalid Submission payload Initiation Object found");
            consentValidationResult.setErrorMessage("Invalid Submission payload Initiation Object found");
            consentValidationResult.setErrorCode(ResponseStatus.BAD_REQUEST.getReasonPhrase());
            consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }


        if (!isJsonObjectsSimilar(requestInitiation, submissionInitiation)) {
            log.error("Initiation payloads does not match");
            consentValidationResult.setErrorMessage("Initiation payloads does not match");
            consentValidationResult.setErrorCode(ResponseStatus.BAD_REQUEST.getReasonPhrase());
            consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }
        consentValidationResult.setValid(true);
    }

    /**
     * Method to check the consent expiration.
     *
     * @param expDateVal   Expiration date value
     * @return boolean     True if consent is expired
     * @throws ConsentException Consent Exception with error details
     */
    private static boolean isConsentExpired(String expDateVal) throws ConsentException {

        if (expDateVal != null && !expDateVal.isEmpty()) {
            try {
                OffsetDateTime expDate = OffsetDateTime.parse(expDateVal);
                return OffsetDateTime.now().isAfter(expDate);
            } catch (DateTimeParseException e) {
                log.error(String.format("Error occurred while parsing the expiration date : %s",
                        expDateVal.replaceAll("[\n\r]", "")));
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        "Error occurred while parsing the expiration date");
            }
        } else {
            return false;
        }
    }

    /**
     * Util method to validate the Confirmation of Funds request URI.
     *
     * @param uri  Request URI
     * @return Whether URI is valid
     */
    public static boolean isCOFURIValid(String uri) {
        return COF_SUBMISSION_PATH.equals(uri);
    }
    /**
     * Method to compare whether JSON Objects are equal.
     *
     * @param object1   First Object to compare
     * @param object2   Second Object to compare
     * @return   Whether JSON Objects are equal
     */
    public static boolean isJsonObjectsSimilar(JSONObject object1, JSONObject object2) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            return (mapper.readTree(object1.toString())).equals(mapper.readTree(object2.toString()));
        } catch (JsonProcessingException e) {
            log.error("Error occurred while comparing the JSON Objects", e);
            return false;
        }
    }
}
