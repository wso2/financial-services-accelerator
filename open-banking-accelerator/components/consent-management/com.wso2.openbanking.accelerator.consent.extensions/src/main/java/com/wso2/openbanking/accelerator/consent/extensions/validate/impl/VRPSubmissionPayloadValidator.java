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
package com.wso2.openbanking.accelerator.consent.extensions.validate.impl;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.ErrorConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.PeriodicLimit;
import com.wso2.openbanking.accelerator.consent.extensions.validate.util.ConsentValidatorUtil;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Class for validating VRP submission request.
 */
public class VRPSubmissionPayloadValidator {
    private static final Log log = LogFactory.getLog(VRPSubmissionPayloadValidator.class);

    /**
     * Validates the initiation parameters between the initiation of submission request and the initiation parameters
     * of consent initiation request.
     *
     * @param initiationOfSubmission                 The initiation parameters from the submission request.
     * @param initiationParameterOfConsentInitiation The initiation parameters from the consent initiation request.
     * @return A JSONObject indicating the validation result. It contains a boolean value under the key
     * ConsentExtensionConstants.IS_VALID_PAYLOAD, indicating whether the payload is valid. If the
     * validation fails, it returns a JSONObject containing error details with keys defined in ErrorConstants.
     */
    public static JSONObject validateInitiation(JSONObject initiationOfSubmission,
                                                JSONObject initiationParameterOfConsentInitiation) {

        if (initiationOfSubmission != null && initiationParameterOfConsentInitiation != null) {



            //Validate Creditor Account
            if ((!initiationOfSubmission.containsKey(ConsentExtensionConstants.CREDITOR_ACC) &&
                    initiationParameterOfConsentInitiation.containsKey(ConsentExtensionConstants.CREDITOR_ACC)) ||
                    (initiationOfSubmission.containsKey(ConsentExtensionConstants.CREDITOR_ACC) &&
                            !initiationParameterOfConsentInitiation.
                                    containsKey(ConsentExtensionConstants.CREDITOR_ACC))) {

                return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                        ErrorConstants.CREDITOR_ACC_NOT_FOUND);
            } else if (initiationOfSubmission.containsKey(ConsentExtensionConstants.CREDITOR_ACC) &&
                    initiationParameterOfConsentInitiation.containsKey(ConsentExtensionConstants.CREDITOR_ACC)) {

                Object submissionCreditorAccounts = initiationOfSubmission.
                        get(ConsentExtensionConstants.CREDITOR_ACC);
                Object consentInitiationCreditorAccounts = initiationParameterOfConsentInitiation.
                        get(ConsentExtensionConstants.CREDITOR_ACC);

                if (submissionCreditorAccounts instanceof JSONObject &&
                        consentInitiationCreditorAccounts instanceof JSONObject) {
                    JSONObject submissionCreditorAccount = (JSONObject) initiationOfSubmission.
                            get(ConsentExtensionConstants.CREDITOR_ACC);
                    JSONObject consentInitiationCreditorAccount = (JSONObject)
                            initiationParameterOfConsentInitiation.get(ConsentExtensionConstants.CREDITOR_ACC);

                    JSONObject creditorAccValidationResult = ConsentValidatorUtil.
                            validateCreditorAcc(submissionCreditorAccount, consentInitiationCreditorAccount);
                    if (!Boolean.parseBoolean(creditorAccValidationResult.
                            getAsString(ConsentExtensionConstants.IS_VALID_PAYLOAD))) {
                        return creditorAccValidationResult;
                    }
                } else {
                    return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                            ErrorConstants.INITIATION_CREDITOR_ACC_NOT_JSON_ERROR);
                }
            }

            //Validate Debtor Account
            // This code if condition checks whether the debtor account parameter is present in both the request
            // payloads (Initiation and the submission payloads) since both the payloads as to be equal.
            if ((!initiationOfSubmission.containsKey(ConsentExtensionConstants.DEBTOR_ACC) &&
                    initiationParameterOfConsentInitiation.containsKey(ConsentExtensionConstants.DEBTOR_ACC)) ||
                    (initiationOfSubmission.containsKey(ConsentExtensionConstants.DEBTOR_ACC) &&
                            !initiationParameterOfConsentInitiation.
                                    containsKey(ConsentExtensionConstants.DEBTOR_ACC))) {

                return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                        ErrorConstants.DEBTOR_ACC_NOT_FOUND);
            } else if (initiationOfSubmission.containsKey(ConsentExtensionConstants.DEBTOR_ACC) &&
                    initiationParameterOfConsentInitiation.containsKey(ConsentExtensionConstants.DEBTOR_ACC)) {

                Object submissionDebtorAccounts = initiationOfSubmission
                        .get(ConsentExtensionConstants.DEBTOR_ACC);
                Object consentInitiationDebtorAccounts = initiationParameterOfConsentInitiation
                        .get(ConsentExtensionConstants.DEBTOR_ACC);

                if (submissionDebtorAccounts instanceof JSONObject &&
                        consentInitiationDebtorAccounts instanceof JSONObject) {
                    JSONObject submissionDebtorAccount = (JSONObject) initiationOfSubmission
                            .get(ConsentExtensionConstants.DEBTOR_ACC);
                    JSONObject consentInitiationDebtorAccount = (JSONObject) initiationParameterOfConsentInitiation
                            .get(ConsentExtensionConstants.DEBTOR_ACC);

                    JSONObject debtorAccValidationResult = ConsentValidatorUtil.
                            validateDebtorAcc(submissionDebtorAccount, consentInitiationDebtorAccount);
                    if (!Boolean.parseBoolean(debtorAccValidationResult.
                            getAsString(ConsentExtensionConstants.IS_VALID_PAYLOAD))) {
                        return debtorAccValidationResult;
                    }
                } else {
                    return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_INVALID,
                            ErrorConstants.DEBTOR_ACC_NOT_JSON_ERROR);
                }
            }

            if ((!initiationOfSubmission.containsKey(ConsentExtensionConstants.REMITTANCE_INFO)
                    && initiationParameterOfConsentInitiation.containsKey(ConsentExtensionConstants.REMITTANCE_INFO)) ||
                    (initiationOfSubmission.containsKey(ConsentExtensionConstants.REMITTANCE_INFO)
                            && !initiationParameterOfConsentInitiation.
                            containsKey(ConsentExtensionConstants.REMITTANCE_INFO))) {
                return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                        ErrorConstants.REMITTANCE_INFO_NOT_FOUND);
            } else if (initiationOfSubmission.containsKey(ConsentExtensionConstants.REMITTANCE_INFO)
                    && initiationParameterOfConsentInitiation.
                    containsKey(ConsentExtensionConstants.REMITTANCE_INFO)) {

                Object remittanceInformationSubmission = initiationOfSubmission
                        .get(ConsentExtensionConstants.REMITTANCE_INFO);
                Object remittanceInformationInitiation = initiationParameterOfConsentInitiation
                        .get(ConsentExtensionConstants.REMITTANCE_INFO);

                if (remittanceInformationSubmission instanceof JSONObject &&
                        remittanceInformationInitiation instanceof JSONObject) {
                    JSONObject remittanceInformationSub = (JSONObject) initiationOfSubmission
                            .get(ConsentExtensionConstants.REMITTANCE_INFO);
                    JSONObject remittanceInformationInit = (JSONObject) initiationParameterOfConsentInitiation
                            .get(ConsentExtensionConstants.REMITTANCE_INFO);

                    JSONObject validateRemittanceInfoResult = VRPSubmissionPayloadValidator.validateRemittanceInfo
                            (remittanceInformationSub, remittanceInformationInit);
                    if (!Boolean.parseBoolean(validateRemittanceInfoResult.
                            getAsString(ConsentExtensionConstants.IS_VALID_PAYLOAD))) {
                        return  validateRemittanceInfoResult;
                    }
                } else {
                    return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_INVALID,
                            ErrorConstants.INITIATION_REMITTANCE_INFO_NOT_JSON_ERROR);
                }
           }
        } else {
            return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                    ErrorConstants.INVALID_PARAMETER);
        }

        JSONObject validationResult = new JSONObject();
        validationResult.put(ConsentExtensionConstants.IS_VALID_PAYLOAD, true);
        return validationResult;
    }

    /**
     * Validates the instruction between submission and initiation JSONObjects.
     *
     * @param submission The submission JSONObject from submission request.
     * @param initiation The initiation JSONObject from initiation request, here we consider the initiation parameter
     *                   since the creditor account from the initiation request need to be retrieved.
     * @param consentId
     * @return A JSONObject indicating the validation result. It contains a boolean value under the key
     * ConsentExtensionConstants.IS_VALID_PAYLOAD, indicating whether the payload is valid. If the
     * validation fails, it returns a JSONObject containing error details with keys defined in ErrorConstants.
     */
    public static JSONObject validateInstruction(JSONObject submission,
                                                 JSONObject initiation, String consentId) {

        if (submission != null && initiation != null) {

            if (!submission.containsKey(ConsentExtensionConstants.INSTRUCTED_AMOUNT)) {
                return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                        ErrorConstants.INSTRUCTED_AMOUNT_NOT_FOUND);
            } else {
                Object instructedAmountObject = submission.get(ConsentExtensionConstants.INSTRUCTED_AMOUNT);

                if (isValidJSONObject(instructedAmountObject)) {
                    JSONObject instructedAmount = (JSONObject) instructedAmountObject;
                    if (!instructedAmount.containsKey(ConsentExtensionConstants.AMOUNT)) {
                        return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                                ErrorConstants.INSTRUCTED_AMOUNT_AMOUNT_NOT_FOUND);
                    } else {
                        Object amountValue = instructedAmount.get(ConsentExtensionConstants.AMOUNT);
                        if (!isValidString(amountValue)) {
                            return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_INVALID,
                                    ErrorConstants.INSTRUCTED_AMOUNT_NOT_STRING);
                        }

                        if (!instructedAmount.containsKey(ConsentExtensionConstants.CURRENCY)) {
                            return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                                    ErrorConstants.INSTRUCTED_AMOUNT_CURRENCY_NOT_FOUND);
                        } else {
                            Object currencyValue = instructedAmount.get(ConsentExtensionConstants.CURRENCY);
                            if (!isValidString(currencyValue)) {
                                return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_INVALID,
                                        ErrorConstants.INSTRUCTED_AMOUNT_CURRENCY_NOT_STRING);
                            }
                        }
                    }
                } else {
                    return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_INVALID,
                            ErrorConstants.INSTRUCTED_AMOUNT_NOT_JSON_ERROR);
                }
            }

            //Validate Creditor Account
            JSONObject validateCreditorAccResult = VRPSubmissionPayloadValidator.validateCreditorAcc
                    (submission, initiation);
            if (!Boolean.parseBoolean(validateCreditorAccResult.
                    get(ConsentExtensionConstants.IS_VALID_PAYLOAD).toString())) {
                return validateCreditorAccResult;
            }

            if (submission.containsKey(ConsentExtensionConstants.INSTRUCTION_IDENTIFICATION)) {
                Object value = submission.get(ConsentExtensionConstants.INSTRUCTION_IDENTIFICATION);

                // Check if the instruction_identification is an instance of a string
                if (!isValidString(value)) {
                    return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_INVALID,
                            ErrorConstants.INVALID_SUBMISSION_TYPE);
                }
            } else {
                return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                        ErrorConstants.INSTRUCTION_IDENTIFICATION_NOT_FOUND);
            }

            if (submission.containsKey(ConsentExtensionConstants.END_TO_END_IDENTIFICATION)) {
                Object endToEndIdentificationValue = submission.
                        get(ConsentExtensionConstants.END_TO_END_IDENTIFICATION);
                if (!isValidString(endToEndIdentificationValue)) {
                    return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_INVALID,
                            ErrorConstants.INVALID_END_TO_END_IDENTIFICATION_TYPE);
                }
            } else {
                return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                        ErrorConstants.END_TO_END_IDENTIFICATION_PARAMETER_NOT_FOUND);
            }

            if ((!submission.containsKey(ConsentExtensionConstants.REMITTANCE_INFO)
                    && initiation.containsKey(ConsentExtensionConstants.REMITTANCE_INFO)) ||
                    (submission.containsKey(ConsentExtensionConstants.REMITTANCE_INFO)
                            && !initiation.containsKey(ConsentExtensionConstants.REMITTANCE_INFO))) {
                return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                        ErrorConstants.REMITTANCE_INFO_NOT_FOUND);
            } else if (submission.containsKey(ConsentExtensionConstants.REMITTANCE_INFO)
                    && initiation.containsKey(ConsentExtensionConstants.REMITTANCE_INFO)) {
                Object remittanceInformationSubmission = submission
                        .get(ConsentExtensionConstants.REMITTANCE_INFO);
                Object remittanceInformationInitiation = initiation
                        .get(ConsentExtensionConstants.REMITTANCE_INFO);

                if (remittanceInformationSubmission instanceof JSONObject &&
                        remittanceInformationInitiation instanceof JSONObject) {
                    JSONObject remittanceInformationSub = (JSONObject) submission
                            .get(ConsentExtensionConstants.REMITTANCE_INFO);
                    JSONObject remittanceInformationInit = (JSONObject) initiation
                            .get(ConsentExtensionConstants.REMITTANCE_INFO);

                    JSONObject remittanceInfoValidationResult = VRPSubmissionPayloadValidator.validateRemittanceInfo
                            (remittanceInformationSub, remittanceInformationInit);
                    if ((!Boolean.parseBoolean(remittanceInfoValidationResult.
                            get(ConsentExtensionConstants.IS_VALID_PAYLOAD).toString()))) {
                        return remittanceInfoValidationResult;
                    }
                } else {
                    return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_INVALID,
                            ErrorConstants.INSTRUCTION_REMITTANCE_INFO_NOT_JSON_ERROR);
                }
            }
            //validate instructed amount with periodicLimits
            ConsentCoreServiceImpl consentService = new ConsentCoreServiceImpl();
        }

        JSONObject validationResult = new JSONObject();
        validationResult.put(ConsentExtensionConstants.IS_VALID_PAYLOAD, true);
        return validationResult;
    }

    /**
     * Validates the remittance information between two remittance information JSONObjects.
     *
     * @param remittanceInformationSub  The remittance information from the submission request.
     * @param remittanceInformationInit The remittance information from the initiation request.
     * @return A JSONObject indicating the validation result. It contains a boolean value under the key
     * ConsentExtensionConstants.IS_VALID_PAYLOAD, indicating whether the payload is valid. If the
     * validation fails, it returns a JSONObject containing error details with keys defined in ErrorConstants.
     */
    public static JSONObject validateRemittanceInfo(JSONObject remittanceInformationSub,
                                                    JSONObject remittanceInformationInit) {

        if (!ConsentValidatorUtil.compareOptionalParameter(
                remittanceInformationSub.getAsString(ConsentExtensionConstants.REFERENCE),
                remittanceInformationInit.getAsString(ConsentExtensionConstants.REFERENCE))) {

            return ConsentValidatorUtil.getValidationResult(ErrorConstants.RESOURCE_CONSENT_MISMATCH,
                    ErrorConstants.REMITTANCE_INFO_MISMATCH);
        }

        if (!ConsentValidatorUtil.compareOptionalParameter(
                remittanceInformationSub.getAsString(ConsentExtensionConstants.UNSTRUCTURED),
                remittanceInformationInit.getAsString(ConsentExtensionConstants.UNSTRUCTURED))) {

            return ConsentValidatorUtil.getValidationResult(ErrorConstants.RESOURCE_CONSENT_MISMATCH,
                    ErrorConstants.REMITTANCE_UNSTRUCTURED_MISMATCH);
        }

        JSONObject validationResult = new JSONObject();
        validationResult.put(ConsentExtensionConstants.IS_VALID_PAYLOAD, true);
        return validationResult;
    }

    /**
     * Validates the risk parameters between the risk of submission and the risk of initiation JSONObjects.
     *
     * @param riskOfSubmission The risk parameters from the submission.
     * @param riskOfInitiation The risk parameters from the initiation.
     * @return A JSONObject indicating the validation result. It contains a boolean value under the key
     * ConsentExtensionConstants.IS_VALID_PAYLOAD, indicating whether the payload is valid. If the
     * validation fails, it returns a JSONObject containing error details with keys defined in ErrorConstants.
     */
    public static JSONObject validateRisk(JSONObject riskOfSubmission,
                                          JSONObject riskOfInitiation) {

        if (!ConsentValidatorUtil.compareOptionalParameter(
                riskOfSubmission.getAsString(ConsentExtensionConstants.CONTEXT_CODE),
                riskOfInitiation.getAsString(ConsentExtensionConstants.CONTEXT_CODE))) {

            return ConsentValidatorUtil.getValidationResult(ErrorConstants.RESOURCE_CONSENT_MISMATCH,
                    ErrorConstants.RISK_PARAMETER_MISMATCH);
        }
        JSONObject validationResult = new JSONObject();
        validationResult.put(ConsentExtensionConstants.IS_VALID_PAYLOAD, true);
        return validationResult;
    }

    /**
     * This method validates whether the risk parameter is present in the request and validates the risk parameter is an
     * instance of JSONObject.
     *
     * @param submissionJson
     * @return validationResult
     */
    public static JSONObject validateRiskParameter(JSONObject submissionJson) {

        //Validate RISK
        if (submissionJson.containsKey(ConsentExtensionConstants.RISK)) {

            Object dataObject = submissionJson.get(ConsentExtensionConstants.RISK);
            // Check if the risk is valid JSON Object
            if (!isValidJSONObject(dataObject)) {
                return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_INVALID,
                        ErrorConstants.RISK_NOT_JSON_ERROR);
            }
        } else {
            log.error(ErrorConstants.RISK_NOT_FOUND);
            return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                    ErrorConstants.RISK_NOT_FOUND);
        }
        JSONObject validationResult = new JSONObject();
        validationResult.put(ConsentExtensionConstants.IS_VALID_PAYLOAD, true);
        return validationResult;
    }

    /**
     * Checks if the given Object is a JSONObject and the JSONObject is non-empty , and it is an instance of a string.
     *
     * @param value The Object to be validated.
     * @return true if the object is a non-null and non-empty JSONObject.
     */
    public static boolean isValidString(Object value) {
        return value instanceof String;
    }

    /**
     * Validates initiation parameter in the submission data.
     *
     * @param submissionData The JSONObject containing submission data.
     * @return A JSONObject indicating the validation result.
     */
    public static JSONObject validateInitiationParameter(JSONObject submissionData) {

        if (submissionData.containsKey(ConsentExtensionConstants.INITIATION)) {

            Object dataObject = submissionData.get(ConsentExtensionConstants.INITIATION);

            if (!isValidJSONObject(dataObject)) {
                return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_INVALID,
                        ErrorConstants.INITIATION_NOT_JSON);
            }
        } else {
            return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                    ErrorConstants.INITIATION_NOT_FOUND);
        }
        JSONObject validationResult = new JSONObject();
        validationResult.put(ConsentExtensionConstants.IS_VALID_PAYLOAD, true);
        return validationResult;
    }

    /**
     * Validates instruction parameter in the submission data.
     *
     * @param submissionData The JSONObject containing submission data.
     * @return A JSONObject indicating the validation result.
     */
    public static JSONObject validateInstructionParameter(JSONObject submissionData) {

        if (submissionData.containsKey(ConsentExtensionConstants.INSTRUCTION)) {

            Object dataObject = submissionData.get(ConsentExtensionConstants.INSTRUCTION);
            if (!isValidJSONObject(dataObject)) {
                return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_INVALID,
                        ErrorConstants.INSTRUCTION_NOT_JSON);
            }
        } else {
            return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                    ErrorConstants.INSTRUCTION_NOT_FOUND);
        }
        JSONObject validationResult = new JSONObject();
        validationResult.put(ConsentExtensionConstants.IS_VALID_PAYLOAD, true);
        return validationResult;
    }

    /**
     * Extracts submission data from a JSONObject.
     *
     * @param submissionJson The JSONObject containing submission data.
     * @return A JSONObject indicating the validation result.
     */
    public static JSONObject validateSubmissionData(JSONObject submissionJson) {

        if (!submissionJson.containsKey(ConsentExtensionConstants.DATA) &&
                !(submissionJson.get(ConsentExtensionConstants.DATA) instanceof JSONObject)) {
            log.error(ErrorConstants.DATA_NOT_FOUND);
            return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                    ErrorConstants.DATA_NOT_FOUND);
        }
        JSONObject validationResult = new JSONObject();
        validationResult.put(ConsentExtensionConstants.IS_VALID_PAYLOAD, true);
        return validationResult;
    }

    /**
     * Checks if the given object is a valid JSONObject.
     *
     * @param value The object to be checked.
     * @return true if the object is a JSONObject, otherwise false.
     */
    public static boolean isValidJSONObject(Object value) {
        return value instanceof JSONObject;
    }

    /**
     * Validates the creditor account parameter between the creditor account of submission under instruction parameter
     * and the creditor account  of initiation JSONObjects.
     *
     * @param submission The creditor account parameters from the submission.
     * @param initiation The creditor account parameters from the initiation.
     * @return A JSONObject indicating the validation result. It contains a boolean value under the key
     * ConsentExtensionConstants.IS_VALID_PAYLOAD, indicating whether the payload is valid. If the
     * validation fails, it returns a JSONObject containing error details with keys defined in ErrorConstants.
     */
    public static JSONObject validateCreditorAcc(JSONObject submission,
                                                    JSONObject initiation) {
        JSONObject validationResult = new JSONObject();

        if (submission.containsKey(ConsentExtensionConstants.CREDITOR_ACC)) {
            // If the CreditorAccount was not specified in the consent initiation,the CreditorAccount must be specified
            // in the instruction present in the submission payload.
            if (!initiation.containsKey(ConsentExtensionConstants.CREDITOR_ACC)) {
                validationResult.put(ConsentExtensionConstants.IS_VALID_PAYLOAD, true);
            } else {
                Object submissionCreditorAccounts = submission.get(ConsentExtensionConstants.CREDITOR_ACC);
                Object consentInitiationCreditorAccounts = initiation.get(ConsentExtensionConstants.CREDITOR_ACC);

                if (submissionCreditorAccounts instanceof JSONObject &&
                        consentInitiationCreditorAccounts instanceof JSONObject) {
                    JSONObject submissionCreditorAccount = (JSONObject) submission.
                            get(ConsentExtensionConstants.CREDITOR_ACC);
                    JSONObject consentInitiationCreditorAccount = (JSONObject) initiation.
                            get(ConsentExtensionConstants.CREDITOR_ACC);

                    JSONObject creditorAccValidationResult = ConsentValidatorUtil.
                            validateCreditorAcc(submissionCreditorAccount, consentInitiationCreditorAccount);
                    if (!Boolean.parseBoolean(validationResult.
                            getAsString(ConsentExtensionConstants.IS_VALID_PAYLOAD))) {
                        return creditorAccValidationResult;
                    }
                } else {
                    return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_INVALID,
                            ErrorConstants.INSTRUCTION_CREDITOR_ACC_NOT_JSON_ERROR);
                }
            }
        } else {
            // Creditor account present under the instruction in the submission request
            // is considered to be a mandatory parameter
            return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                    ErrorConstants.CREDITOR_ACC_NOT_FOUND);
        }

        validationResult.put(ConsentExtensionConstants.IS_VALID_PAYLOAD, true);
        return validationResult;
    }
}
