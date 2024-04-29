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
package com.wso2.openbanking.accelerator.consent.extensions.validate.impl;

import com.wso2.openbanking.accelerator.common.util.ErrorConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.validate.util.ConsentValidatorUtil;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
     *         ConsentExtensionConstants.IS_VALID_PAYLOAD, indicating whether the payload is valid. If the
     *         validation fails, it returns a JSONObject containing error details with keys defined in ErrorConstants.
     */
    public static JSONObject validateInitiation(JSONObject initiationOfSubmission,
                                                 JSONObject initiationParameterOfConsentInitiation) {

        JSONObject validationResult = new JSONObject();
        validationResult.put(ConsentExtensionConstants.IS_VALID_PAYLOAD, true);
        validationResult.put(ConsentExtensionConstants.ERROR_CODE, "");
        validationResult.put(ConsentExtensionConstants.ERROR_MESSAGE, "");

        if (initiationOfSubmission != null && initiationParameterOfConsentInitiation != null) {
                //Validate Creditor Account
                if (initiationOfSubmission.containsKey(ConsentExtensionConstants.CREDITOR_ACC) &&
                        initiationParameterOfConsentInitiation.containsKey(ConsentExtensionConstants.CREDITOR_ACC)) {

                    Object submissionCreditorAccounts = initiationOfSubmission.
                            get(ConsentExtensionConstants.CREDITOR_ACC);
                    Object consentInitiationCreditorAccounts = initiationParameterOfConsentInitiation.
                            get(ConsentExtensionConstants.CREDITOR_ACC);

                    if (areJSONObjects(submissionCreditorAccounts, consentInitiationCreditorAccounts)) {
                        JSONObject submissionCreditorAccount = (JSONObject) initiationOfSubmission.
                                get(ConsentExtensionConstants.CREDITOR_ACC);
                        JSONObject consentInitiationCreditorAccount = (JSONObject)
                                initiationParameterOfConsentInitiation.get(ConsentExtensionConstants.CREDITOR_ACC);

                        JSONObject creditorAccValidationResult = ConsentValidatorUtil.
                                validateCreditorAcc(submissionCreditorAccount, consentInitiationCreditorAccount);
                        if (!(boolean) creditorAccValidationResult.get(ConsentExtensionConstants.IS_VALID_PAYLOAD)) {
                            return creditorAccValidationResult;
                        }
                    } else {
                        return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                                ErrorConstants.INITIATION_CREDITOR_ACC_NOT_JSON_ERROR);
                    }
                } else {
                    return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                            ErrorConstants.CREDITOR_ACC_NOT_FOUND);
                }

                //Validate Debtor Account
                if ((!initiationOfSubmission.containsKey(ConsentExtensionConstants.DEBTOR_ACC) &&
                        initiationParameterOfConsentInitiation.containsKey(ConsentExtensionConstants.DEBTOR_ACC)) ||
                        (initiationOfSubmission.containsKey(ConsentExtensionConstants.DEBTOR_ACC) &&
                                !initiationParameterOfConsentInitiation.
                                        containsKey(ConsentExtensionConstants.DEBTOR_ACC))) {

                    return ConsentValidatorUtil.getValidationResult(ErrorConstants.RESOURCE_CONSENT_MISMATCH,
                            ErrorConstants.DEBTOR_ACC_MISMATCH);
                } else if (initiationOfSubmission.containsKey(ConsentExtensionConstants.DEBTOR_ACC) &&
                        initiationParameterOfConsentInitiation.containsKey(ConsentExtensionConstants.DEBTOR_ACC)) {

                    Object submissionDebtorAccounts = initiationOfSubmission
                            .get(ConsentExtensionConstants.DEBTOR_ACC);
                    Object consentInitiationDebtorAccounts = initiationParameterOfConsentInitiation
                            .get(ConsentExtensionConstants.DEBTOR_ACC);

                    if (areJSONObjects(submissionDebtorAccounts, consentInitiationDebtorAccounts)) {
                        JSONObject submissionDebtorAccount = (JSONObject) initiationOfSubmission
                                .get(ConsentExtensionConstants.DEBTOR_ACC);
                        JSONObject consentInitiationDebtorAccount = (JSONObject) initiationParameterOfConsentInitiation
                                .get(ConsentExtensionConstants.DEBTOR_ACC);

                        JSONObject debtorAccValidationResult = ConsentValidatorUtil.
                                validateDebtorAcc(submissionDebtorAccount, consentInitiationDebtorAccount);
                        if (!(boolean) debtorAccValidationResult.get(ConsentExtensionConstants.IS_VALID_PAYLOAD)) {
                            return debtorAccValidationResult;
                        }
                    } else {
                        return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                                ErrorConstants.DEBTOR_ACC_NOT_JSON_ERROR);
                    }
                }

                if (initiationOfSubmission.containsKey(ConsentExtensionConstants.REMITTANCE_INFO)
                        && initiationParameterOfConsentInitiation.
                        containsKey(ConsentExtensionConstants.REMITTANCE_INFO)) {

                    Object remittanceInformationSubmission = initiationOfSubmission
                            .get(ConsentExtensionConstants.REMITTANCE_INFO);
                    Object remittanceInformationInitiation = initiationParameterOfConsentInitiation
                            .get(ConsentExtensionConstants.REMITTANCE_INFO);

                    if (areJSONObjects(remittanceInformationSubmission, remittanceInformationInitiation)) {
                        JSONObject remittanceInformationSub = (JSONObject) initiationOfSubmission
                                .get(ConsentExtensionConstants.REMITTANCE_INFO);
                        JSONObject remittanceInformationInit = (JSONObject) initiationParameterOfConsentInitiation
                                .get(ConsentExtensionConstants.REMITTANCE_INFO);

                        validationResult = VRPSubmissionPayloadValidator.validateRemittanceInfo
                                (remittanceInformationSub, remittanceInformationInit);
                        if (!((boolean) validationResult.get(ConsentExtensionConstants.IS_VALID_PAYLOAD))) {
                            return validationResult;
                        }
                    } else {
                        return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                                ErrorConstants.INITIATION_REMITTANCE_INFO_NOT_JSON_ERROR);
                    }

                } else {
                    return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                            ErrorConstants.INITIATION_REMITTANCE_INFO_PARAMETER_NOT_FOUND);
                }
            }
        return validationResult;
    }

    /**
     * Validates the instruction between submission and initiation JSONObjects.
     *
     * @param submission The instruction submission JSONObject from submission request.
     * @param initiation The instruction initiation JSONObject from initiation request.
     * @return A JSONObject indicating the validation result. It contains a boolean value under the key
     *         ConsentExtensionConstants.IS_VALID_PAYLOAD, indicating whether the payload is valid. If the
     *         validation fails, it returns a JSONObject containing error details with keys defined in ErrorConstants.
     */
    public static JSONObject validateInstruction(JSONObject submission,
                                                JSONObject initiation) {

        JSONObject validationResult = new JSONObject();
        validationResult.put(ConsentExtensionConstants.IS_VALID_PAYLOAD, true);
        validationResult.put(ConsentExtensionConstants.ERROR_CODE, "");
        validationResult.put(ConsentExtensionConstants.ERROR_MESSAGE, "");

        if (submission != null && initiation != null) {

            if (submission.containsKey(ConsentExtensionConstants.INSTRUCTION_IDENTIFICATION)) {
                Object value = submission.get(ConsentExtensionConstants.INSTRUCTION_IDENTIFICATION);
                if (!(value instanceof String)) {
                    return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                            ErrorConstants.INVALID_SUBMISSION_TYPE);
                }
            } else {
                //log.error(ErrorConstants.INSTRUCTION_IDENTIFICATION_NOT_FOUND);
                return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                        ErrorConstants.INSTRUCTION_IDENTIFICATION_NOT_FOUND);
            }

            if (submission.containsKey(ConsentExtensionConstants.END_TO_END_IDENTIFICATION)) {
                Object value = submission.get(ConsentExtensionConstants.END_TO_END_IDENTIFICATION);
                if (!(value instanceof String)) {
                   // log.error(ErrorConstants.INVALID_TYPE);
                    return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                            ErrorConstants.INVALID_TYPE);
                }
            } else {
                return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                        ErrorConstants.END_TO_END_IDENTIFICATION_PARAMETER_NOT_FOUND);
            }

            //Validate Creditor Account
            if (submission.containsKey(ConsentExtensionConstants.CREDITOR_ACC) &&
                    initiation.containsKey(ConsentExtensionConstants.CREDITOR_ACC)) {

                Object submissionCreditorAccounts = submission.get(ConsentExtensionConstants.CREDITOR_ACC);
                Object consentInitiationCreditorAccounts = initiation.get(ConsentExtensionConstants.CREDITOR_ACC);

                if (areJSONObjects(submissionCreditorAccounts, consentInitiationCreditorAccounts)) {
                    JSONObject submissionCreditorAccount = (JSONObject) submission.
                            get(ConsentExtensionConstants.CREDITOR_ACC);
                    JSONObject consentInitiationCreditorAccount = (JSONObject) initiation.
                            get(ConsentExtensionConstants.CREDITOR_ACC);

                    JSONObject creditorAccValidationResult = ConsentValidatorUtil.
                            validateCreditorAcc(submissionCreditorAccount, consentInitiationCreditorAccount);
                    if (!(boolean) creditorAccValidationResult.get(ConsentExtensionConstants.IS_VALID_PAYLOAD)) {
                        return creditorAccValidationResult;
                    }
                } else {
                    return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                            ErrorConstants.INSTRUCTION_CREDITOR_ACC_NOT_JSON_ERROR);
                }
            } else {
                return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                        ErrorConstants.CREDITOR_ACC_NOT_FOUND);
            }

            if (!submission.containsKey(ConsentExtensionConstants.INSTRUCTED_AMOUNT)) {

                return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                        ErrorConstants.INSTRUCTED_AMOUNT_PARAMETER_NOT_FOUND);
            }

            if (submission.containsKey(ConsentExtensionConstants.REMITTANCE_INFO) && initiation.
                    containsKey(ConsentExtensionConstants.REMITTANCE_INFO)) {

                Object remittanceInformationSubmission = submission.get(ConsentExtensionConstants.REMITTANCE_INFO);
                Object remittanceInformationInitiation = initiation.get(ConsentExtensionConstants.REMITTANCE_INFO);

                if (areJSONObjects(remittanceInformationSubmission, remittanceInformationInitiation)) {
                    JSONObject remittanceInformationSub = (JSONObject) submission
                            .get(ConsentExtensionConstants.REMITTANCE_INFO);
                    JSONObject remittanceInformationInit = (JSONObject) initiation
                            .get(ConsentExtensionConstants.REMITTANCE_INFO);

                    validationResult = VRPSubmissionPayloadValidator.validateRemittanceInfo(remittanceInformationSub,
                            remittanceInformationInit);
                    if (!((boolean) validationResult.get(ConsentExtensionConstants.IS_VALID_PAYLOAD))) {
                        return validationResult;
                    }
                } else {
                    return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                            ErrorConstants.INSTRUCTION_REMITTANCE_INFO_NOT_JSON_ERROR);
                }
            } else {
                return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                        ErrorConstants.INSTRUCTION_REMITTANCE_INFO_PARAMETER_NOT_FOUND);
            }
        }
        return validationResult;
    }

    /**
     * Validates the remittance information between two remittance information JSONObjects.
     *
     * @param remittanceInformationSub  The remittance information from the submission request.
     * @param remittanceInformationInit The remittance information from the initiation request.
     * @return A JSONObject indicating the validation result. It contains a boolean value under the key
     *         ConsentExtensionConstants.IS_VALID_PAYLOAD, indicating whether the payload is valid. If the
     *         validation fails, it returns a JSONObject containing error details with keys defined in ErrorConstants.
     */
    public static JSONObject validateRemittanceInfo(JSONObject remittanceInformationSub,
                                                    JSONObject remittanceInformationInit) {

        JSONObject validationResult = new JSONObject();
        validationResult.put(ConsentExtensionConstants.IS_VALID_PAYLOAD, true);

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

        return validationResult;

    }

    /**
     * Validates the risk parameters between the risk of submission and the risk of initiation JSONObjects.
     *
     * @param riskOfSubmission The risk parameters from the submission.
     * @param riskOfInitiation The risk parameters from the initiation.
     * @return A JSONObject indicating the validation result. It contains a boolean value under the key
     *         ConsentExtensionConstants.IS_VALID_PAYLOAD, indicating whether the payload is valid. If the
     *         validation fails, it returns a JSONObject containing error details with keys defined in ErrorConstants.
     */
    public static JSONObject validateRisk(JSONObject riskOfSubmission,
                                          JSONObject riskOfInitiation) {

        JSONObject validationResult = new JSONObject();
        validationResult.put(ConsentExtensionConstants.IS_VALID_PAYLOAD, true);

        if (!ConsentValidatorUtil.compareOptionalParameter(
                riskOfSubmission.getAsString(ConsentExtensionConstants.CONTEXT_CODE),
                riskOfInitiation.getAsString(ConsentExtensionConstants.CONTEXT_CODE))) {

            return ConsentValidatorUtil.getValidationResult(ErrorConstants.RESOURCE_CONSENT_MISMATCH,
                    ErrorConstants.RISK_PARAMETER_MISMATCH);
        }

        return validationResult;

    }

    /**
     This method checks if the given objects are instances of JSONObject.
     @param obj1 The first object to compare.
     @param obj2 The second object to compare.
     @return true if both objects are instances of JSONObject, false otherwise.
     */
    public static boolean areJSONObjects(Object obj1, Object obj2) {
        return (obj1 instanceof JSONObject) && (obj2 instanceof JSONObject);
    }

}
