/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.openbanking.accelerator.consent.extensions.validate.impl;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.ErrorConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionUtils;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidateData;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidationResult;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidator;
import com.wso2.openbanking.accelerator.consent.extensions.validate.util.ConsentValidatorUtil;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

/**
 * Consent validator default implementation.
 */
public class DefaultConsentValidator implements ConsentValidator {

    private static final Log log = LogFactory.getLog(DefaultConsentValidator.class);
    private static final String ACCOUNTS_REGEX = "/accounts/[^/?]*";
    private static final String TRANSACTIONS_REGEX = "/accounts/[^/?]*/transactions";
    private static final String BALANCES_REGEX = "/accounts/[^/?]*/balances";
    private static final String PERMISSION_MISMATCH_ERROR = "Permission mismatch. Consent does not contain necessary " +
            "permissions";
    private static final String INVALID_URI_ERROR = "Path requested is invalid";
    private static final String CONSENT_EXPIRED_ERROR = "Provided consent is expired";
    private static final String CONSENT_STATE_ERROR = "Provided consent not in authorised state";
    private static final String AUTHORISED_STATUS = "authorised";

    @Override
    public void validate(ConsentValidateData consentValidateData, ConsentValidationResult consentValidationResult)
            throws ConsentException {

        String uri = consentValidateData.getRequestPath();
        JSONObject receiptJSON;
        try {
            receiptJSON = (JSONObject) (new JSONParser(JSONParser.MODE_PERMISSIVE)).
                    parse(consentValidateData.getComprehensiveConsent().getReceipt());

        } catch (ParseException e) {
            log.error(e.getMessage());
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Exception occurred while validating" +
                    " permissions");
        }

        //User Validation
        String userIdFromToken = consentValidateData.getUserId();
        boolean userIdMatching = false;
        ArrayList<AuthorizationResource> authResources = consentValidateData.getComprehensiveConsent()
                .getAuthorizationResources();
        for (AuthorizationResource resource : authResources) {
            if (userIdFromToken.contains(resource.getUserID())) {
                userIdMatching = true;
                break;
            }
        }

        if (!userIdMatching) {
            log.error(ErrorConstants.INVALID_USER_ID);
            consentValidationResult.setErrorMessage(ErrorConstants.INVALID_USER_ID);
            consentValidationResult.setErrorCode(ErrorConstants.RESOURCE_CONSENT_MISMATCH);
            consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        String clientIdFromToken = consentValidateData.getClientId();
        String clientIdFromConsent = consentValidateData.getComprehensiveConsent().getClientID();
        if (clientIdFromToken == null || clientIdFromConsent == null ||
                !clientIdFromToken.equals(clientIdFromConsent)) {
            log.error(ErrorConstants.MSG_INVALID_CLIENT_ID);
            consentValidationResult.setErrorMessage(ErrorConstants.MSG_INVALID_CLIENT_ID);
            consentValidationResult.setErrorCode(ErrorConstants.RESOURCE_CONSENT_MISMATCH);
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
            case ConsentExtensionConstants.FUNDSCONFIRMATIONS:
                validateFundsConfirmationSubmission(consentValidateData, receiptJSON, consentValidationResult);
                break;
            case ConsentExtensionConstants.VRP:
                validateVRPSubmission(consentValidateData, receiptJSON, consentValidationResult);
                break;
            default:
                log.error(ErrorConstants.INVALID_CONSENT_TYPE);
                consentValidationResult.setErrorMessage(ErrorConstants.INVALID_CONSENT_TYPE);
                consentValidationResult.setErrorCode(ErrorConstants.UNEXPECTED_ERROR);
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

        JSONArray permissions = (JSONArray) ((JSONObject) receiptJSON.get("Data")).get("Permissions");

        // Perform URI Validation.
        String uri = consentValidateData.getRequestPath();
        if (!(uri.matches(ACCOUNTS_REGEX) || uri.matches(TRANSACTIONS_REGEX) || uri.matches(BALANCES_REGEX))) {
            consentValidationResult.setErrorMessage(INVALID_URI_ERROR);
            consentValidationResult.setErrorCode("00013");
            consentValidationResult.setHttpCode(401);
            return;
        }
        if ((uri.matches(ACCOUNTS_REGEX) && !permissions.contains("ReadAccountsDetail")) ||
                (uri.matches(TRANSACTIONS_REGEX) && !permissions.contains("ReadTransactionsDetail")) ||
                (uri.matches(BALANCES_REGEX)) && !permissions.contains("ReadBalances")) {
            consentValidationResult.setErrorMessage(PERMISSION_MISMATCH_ERROR);
            consentValidationResult.setErrorCode("00010");
            consentValidationResult.setHttpCode(401);
            return;
        }

        //Consent Status Validation
        if (!ConsentExtensionConstants.AUTHORIZED_STATUS
                .equalsIgnoreCase(consentValidateData.getComprehensiveConsent().getCurrentStatus())) {
            consentValidationResult.setErrorMessage(ErrorConstants.ACCOUNT_CONSENT_STATE_INVALID);
            consentValidationResult.setErrorCode(ErrorConstants.RESOURCE_INVALID_CONSENT_STATUS);
            consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        if (isConsentExpired(((JSONObject) receiptJSON.get("Data")).getAsString("ExpirationDateTime"))) {
            consentValidationResult.setErrorMessage(CONSENT_EXPIRED_ERROR);
            consentValidationResult.setErrorCode("00011");
            consentValidationResult.setHttpCode(401);
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

        try {
            // Rejecting consent if cut off time is elapsed and the policy is REJECT
            // Updating the consent status to "Reject" if the above condition is true
            if (ConsentExtensionUtils.shouldSubmissionRequestBeRejected(ConsentExtensionUtils
                    .convertToISO8601(detailedConsentResource.getCreatedTime()))) {
                boolean success = ConsentExtensionUtils.getConsentService().revokeConsent(
                        detailedConsentResource.getConsentID(), ConsentExtensionConstants.REJECTED_STATUS);
                if (!success) {
                    log.error(ErrorConstants.TOKEN_REVOKE_ERROR);
                    consentValidationResult.setErrorMessage(ErrorConstants.TOKEN_REVOKE_ERROR);
                    consentValidationResult.setErrorCode(ErrorConstants.UNEXPECTED_ERROR);
                    consentValidationResult.setHttpCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                    return;
                }
                log.error(ErrorConstants.CUT_OFF_DATE_ELAPSED);
                consentValidationResult.setErrorMessage(ErrorConstants.CUT_OFF_DATE_ELAPSED);
                consentValidationResult.setErrorCode(ErrorConstants.RULES_CUTOFF);
                consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
                return;
            }

            // Check if requested consent ID matches to initiation consent ID.
            if (consentValidateData.getConsentId() == null || detailedConsentResource.getConsentID() == null ||
                    !consentValidateData.getConsentId().equals(detailedConsentResource.getConsentID())) {
                log.error(ErrorConstants.MSG_INVALID_CONSENT_ID);
                consentValidationResult.setErrorMessage(ErrorConstants.MSG_INVALID_CONSENT_ID);
                consentValidationResult.setErrorCode(ErrorConstants.RESOURCE_CONSENT_MISMATCH);
                consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
                return;
            }

            if (consentValidateData.getRequestPath().contains(ConsentExtensionConstants.PAYMENT_COF_PATH)) {
                PaymentFundsConfirmationPayloadValidator paymentFundsConfirmationValidator =
                        new PaymentFundsConfirmationPayloadValidator();
                paymentFundsConfirmationValidator.validatePaymentFundsConfirmationRequest(consentValidateData,
                        consentValidationResult, detailedConsentResource);
                return;
            } else {
                if (!ConsentExtensionConstants.AUTHORIZED_STATUS
                        .equalsIgnoreCase(consentValidateData.getComprehensiveConsent().getCurrentStatus())) {
                    log.error(ErrorConstants.PAYMENT_CONSENT_STATE_INVALID);
                    consentValidationResult.setErrorMessage(ErrorConstants.PAYMENT_CONSENT_STATE_INVALID);
                    consentValidationResult.setErrorCode(ErrorConstants.RESOURCE_INVALID_CONSENT_STATUS);
                    consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
                    return;
                }

                JSONObject submissionJson = consentValidateData.getPayload();
                JSONObject submissionData = new JSONObject();
                JSONObject submissionInitiation = new JSONObject();

                JSONObject requestInitiation = (JSONObject) ((JSONObject) initiationJson
                        .get(ConsentExtensionConstants.DATA)).get(ConsentExtensionConstants.INITIATION);

                if (submissionJson.containsKey(ConsentExtensionConstants.DATA) &&
                        submissionJson.get(ConsentExtensionConstants.DATA) instanceof JSONObject) {
                    submissionData = (JSONObject) submissionJson.get(ConsentExtensionConstants.DATA);
                } else {
                    log.error(ErrorConstants.DATA_NOT_FOUND);
                    consentValidationResult.setErrorMessage(ErrorConstants.DATA_NOT_FOUND);
                    consentValidationResult.setErrorCode(ErrorConstants.FIELD_MISSING);
                    consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
                    return;
                }

                if (submissionData.containsKey(ConsentExtensionConstants.INITIATION) &&
                        submissionData.get(ConsentExtensionConstants.INITIATION) instanceof JSONObject) {
                    submissionInitiation = (JSONObject) submissionData.get(ConsentExtensionConstants.INITIATION);
                } else {
                    log.error(ErrorConstants.INITIATION_NOT_FOUND);
                    consentValidationResult.setErrorMessage(ErrorConstants.INITIATION_NOT_FOUND);
                    consentValidationResult.setErrorCode(ErrorConstants.FIELD_MISSING);
                    consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
                    return;
                }

                // Check if requested consent ID in the body to initiation consent ID.
                if (!submissionData.containsKey(ConsentExtensionConstants.CONSENT_ID_VALIDATION) ||
                        submissionData.get(ConsentExtensionConstants.CONSENT_ID_VALIDATION) == null ||
                        !submissionData.get(ConsentExtensionConstants.CONSENT_ID_VALIDATION)
                                .equals(detailedConsentResource.getConsentID())) {
                    log.error(ErrorConstants.MSG_INVALID_CONSENT_ID);
                    consentValidationResult.setErrorMessage(ErrorConstants.MSG_INVALID_CONSENT_ID);
                    consentValidationResult.setErrorCode(ErrorConstants.RESOURCE_CONSENT_MISMATCH);
                    consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
                    return;
                }

                PaymentSubmissionPayloadValidator validator = new PaymentSubmissionPayloadValidator();
                JSONObject initiationValidationResult = validator
                        .validateInitiation(submissionInitiation, requestInitiation);

                if (!(boolean) initiationValidationResult.get(ConsentExtensionConstants.IS_VALID_PAYLOAD)) {
                    log.error(initiationValidationResult.getAsString(ConsentExtensionConstants.ERROR_MESSAGE));
                    consentValidationResult.setErrorMessage(initiationValidationResult
                            .getAsString(ConsentExtensionConstants.ERROR_MESSAGE));
                    consentValidationResult.setErrorCode(initiationValidationResult
                            .getAsString(ConsentExtensionConstants.ERROR_CODE));
                    consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
                    return;
                }
            }

        } catch (ConsentManagementException e) {
            log.error(e.getMessage());
            consentValidationResult.setErrorMessage(e.getMessage());
            consentValidationResult.setErrorCode(ErrorConstants.UNEXPECTED_ERROR);
            consentValidationResult.setHttpCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        consentValidationResult.setValid(true);
    }

    private boolean isConsentExpired(String expDateVal) throws ConsentException {

        if (expDateVal != null && !expDateVal.isEmpty()) {
            try {
                OffsetDateTime expDate = OffsetDateTime.parse(expDateVal);
                return OffsetDateTime.now().isAfter(expDate);
            } catch (DateTimeParseException e) {
                log.error("Error occurred while parsing the expiration date : " + expDateVal);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        "Error occurred while parsing the expiration date");
            }
        } else {
            return false;
        }

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
        if (uri == null || !ConsentValidatorUtil.isCOFURIValid(uri)) {
            consentValidationResult.setErrorMessage(ErrorConstants.INVALID_URI_ERROR);
            consentValidationResult.setErrorCode(ErrorConstants.RESOURCE_INVALID_FORMAT);
            consentValidationResult.setHttpCode(401);
            return;
        }

        //Consent Status Validation
        if (!ConsentExtensionConstants.AUTHORIZED_STATUS
                .equalsIgnoreCase(consentValidateData.getComprehensiveConsent().getCurrentStatus())) {
            consentValidationResult.setErrorMessage(ErrorConstants.COF_CONSENT_STATE_INVALID);
            consentValidationResult.setErrorCode(ErrorConstants.RESOURCE_INVALID_CONSENT_STATUS);
            consentValidationResult.setHttpCode(400);
            return;
        }

        //Validate whether the consent is expired
        if (ConsentValidatorUtil
                .isConsentExpired(((JSONObject) receiptJSON.get(ConsentExtensionConstants.DATA))
                        .getAsString(ConsentExtensionConstants.EXPIRATION_DATE))) {
            consentValidationResult.setErrorMessage(ErrorConstants.CONSENT_EXPIRED_ERROR);
            consentValidationResult.setErrorCode(ErrorConstants.FIELD_INVALID);
            consentValidationResult.setHttpCode(400);
            return;
        }

        // Check if requested consent ID in the token to initiation consent ID.
        if (consentValidateData.getConsentId() == null ||
                consentValidateData.getComprehensiveConsent().getConsentID() == null ||
                !consentValidateData.getConsentId()
                        .equals(consentValidateData.getComprehensiveConsent().getConsentID())) {
            log.error(ErrorConstants.MSG_INVALID_CONSENT_ID);
            consentValidationResult.setErrorMessage(ErrorConstants.MSG_INVALID_CONSENT_ID);
            consentValidationResult.setErrorCode(ErrorConstants.RESOURCE_CONSENT_MISMATCH);
            consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        JSONObject data = (JSONObject) consentValidateData.getPayload().get(ConsentExtensionConstants.DATA);
        // Check if requested consent ID in the body to initiation consent ID.
        if (!data.containsKey(ConsentExtensionConstants.CONSENT_ID_VALIDATION) ||
                data.get(ConsentExtensionConstants.CONSENT_ID_VALIDATION) == null ||
                !data.get(ConsentExtensionConstants.CONSENT_ID_VALIDATION)
                        .equals(consentValidateData.getComprehensiveConsent().getConsentID())) {
            log.error(ErrorConstants.MSG_INVALID_CONSENT_ID);
            consentValidationResult.setErrorMessage(ErrorConstants.MSG_INVALID_CONSENT_ID);
            consentValidationResult.setErrorCode(ErrorConstants.RESOURCE_CONSENT_MISMATCH);
            consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        consentValidationResult.setValid(true);

    }

    /**
     * Validate VRP Submission Request.
     *
     * @param consentValidateData     Object with request data
     * @param consentValidationResult Validation result object to return
     */
    private void validateVRPSubmission(ConsentValidateData consentValidateData, JSONObject initiationJson,
                                       ConsentValidationResult consentValidationResult) {

        DetailedConsentResource detailedConsentResource = consentValidateData.getComprehensiveConsent();

        if (!ConsentExtensionConstants.AUTHORIZED_STATUS
                .equals(consentValidateData.getComprehensiveConsent().getCurrentStatus())) {
            log.error(ErrorConstants.PAYMENT_CONSENT_STATE_INVALID);
            consentValidationResult.setErrorMessage(ErrorConstants.PAYMENT_CONSENT_STATE_INVALID);
            consentValidationResult.setErrorCode(ErrorConstants.RESOURCE_INVALID_CONSENT_STATUS);
            consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        // Check if requested consent ID matches to initiation consent ID.
        if (consentValidateData.getConsentId() == null || detailedConsentResource.getConsentID() == null ||
                !consentValidateData.getConsentId().equals(detailedConsentResource.getConsentID())) {
            log.error(ErrorConstants.MSG_INVALID_CONSENT_ID);
            ConsentValidatorUtil.getValidationResult(ErrorConstants.RESOURCE_CONSENT_MISMATCH,
                    ErrorConstants.MSG_INVALID_CONSENT_ID);
            return;
        }

        JSONObject submissionJson = consentValidateData.getPayload();

        JSONObject dataValidationResults = VRPSubmissionPayloadValidator.validateSubmissionData(submissionJson);
        if (!Boolean.parseBoolean(dataValidationResults.
                getAsString(ConsentExtensionConstants.IS_VALID_PAYLOAD))) {
            ConsentValidatorUtil.setErrorMessageForConsentValidationResult(dataValidationResults,
                    consentValidationResult);
            return;
        }

        JSONObject submissionData = (JSONObject) submissionJson.get(ConsentExtensionConstants.DATA);

        JSONObject initiationParameterValidationResults = VRPSubmissionPayloadValidator.
                validateInitiationParameter(submissionData);
        if (!Boolean.parseBoolean(initiationParameterValidationResults.
                getAsString(ConsentExtensionConstants.IS_VALID_PAYLOAD))) {
            log.error(initiationParameterValidationResults.getAsString(ConsentExtensionConstants.ERROR_MESSAGE));
            ConsentValidatorUtil.setErrorMessageForConsentValidationResult(initiationParameterValidationResults,
                    consentValidationResult);
            return;
        }

        JSONObject instructionParameterValidationResults = VRPSubmissionPayloadValidator.
                validateInstructionParameter(submissionData);
        if (!Boolean.parseBoolean(instructionParameterValidationResults.
                getAsString(ConsentExtensionConstants.IS_VALID_PAYLOAD))) {
            log.error(instructionParameterValidationResults.getAsString(ConsentExtensionConstants.ERROR_MESSAGE));
            ConsentValidatorUtil.setErrorMessageForConsentValidationResult(instructionParameterValidationResults,
                    consentValidationResult);
            return;
        }

        // Check if requested consent ID in the body to initiation consent ID.
        if (!submissionData.containsKey(ConsentExtensionConstants.CONSENT_ID) ||
                !(submissionData.get(ConsentExtensionConstants.CONSENT_ID) instanceof String) ||
                !submissionData.get(ConsentExtensionConstants.CONSENT_ID)
                        .equals(detailedConsentResource.getConsentID())) {
            log.error(ErrorConstants.INVALID_REQUEST_CONSENT_ID);
            consentValidationResult.setErrorMessage(ErrorConstants.INVALID_REQUEST_CONSENT_ID);
            consentValidationResult.setErrorCode(ErrorConstants.RESOURCE_CONSENT_MISMATCH);
            consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        JSONObject dataObject = (JSONObject) initiationJson.get(ConsentExtensionConstants.DATA);
        JSONObject requestInitiation = (JSONObject) dataObject.get(ConsentExtensionConstants.INITIATION);
        JSONObject submissionInitiation = (JSONObject) submissionData.get(ConsentExtensionConstants.INITIATION);
        JSONObject submissionInstruction = (JSONObject) submissionData.get(ConsentExtensionConstants.INSTRUCTION);

        JSONObject initiationValidationResult = VRPSubmissionPayloadValidator
                .validateInitiation(submissionInitiation, requestInitiation);

        if (!Boolean.parseBoolean(initiationValidationResult.
                getAsString(ConsentExtensionConstants.IS_VALID_PAYLOAD))) {
            ConsentValidatorUtil.setErrorMessageForConsentValidationResult(initiationValidationResult,
                    consentValidationResult);
            return;
        }

        JSONObject instructionValidationResult = VRPSubmissionPayloadValidator.
                validateInstruction(submissionInstruction, requestInitiation);

        if (!Boolean.parseBoolean(instructionValidationResult.
                getAsString(ConsentExtensionConstants.IS_VALID_PAYLOAD))) {
            ConsentValidatorUtil.setErrorMessageForConsentValidationResult(instructionValidationResult,
                    consentValidationResult);
            return;
        }

        JSONObject riskParameterValidationResults = VRPSubmissionPayloadValidator.validateRiskParameter(submissionJson);
        if (!Boolean.parseBoolean(riskParameterValidationResults.
                getAsString(ConsentExtensionConstants.IS_VALID_PAYLOAD))) {
            ConsentValidatorUtil.setErrorMessageForConsentValidationResult(riskParameterValidationResults,
                    consentValidationResult);
            return;
        }

        JSONObject initiationRisk = (JSONObject) initiationJson.get(ConsentExtensionConstants.RISK);
        JSONObject submissionRisk = (JSONObject) submissionJson.get(ConsentExtensionConstants.RISK);
        JSONObject riskValidationResult = VRPSubmissionPayloadValidator.validateRisk(submissionRisk,
                initiationRisk);

        if (!Boolean.parseBoolean(riskValidationResult.
                getAsString(ConsentExtensionConstants.IS_VALID_PAYLOAD))) {
            ConsentValidatorUtil.setErrorMessageForConsentValidationResult(riskValidationResult,
                    consentValidationResult);
            return;
        }
        consentValidationResult.setValid(true);
    }
}
