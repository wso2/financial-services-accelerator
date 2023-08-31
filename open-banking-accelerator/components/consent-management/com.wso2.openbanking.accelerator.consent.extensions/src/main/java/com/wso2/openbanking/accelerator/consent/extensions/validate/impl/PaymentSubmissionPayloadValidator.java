/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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
import com.wso2.openbanking.accelerator.consent.extensions.validate.util.PaymentSubmissionValidationUtil;
import net.minidev.json.JSONObject;

/**
 * Class for validating Payment submission requests.
 */
public class PaymentSubmissionPayloadValidator {

    /**
     * Method to validate payment submission initiation payload.
     *
     * @param submission Submission Request
     * @param initiation Initiation Request
     * @return
     */
    public JSONObject validateInitiation(JSONObject submission, JSONObject initiation) {

        JSONObject validationResult = new JSONObject();
        validationResult.put(ConsentExtensionConstants.IS_VALID_PAYLOAD, true);
        validationResult.put(ConsentExtensionConstants.ERROR_CODE, "");
        validationResult.put(ConsentExtensionConstants.ERROR_MESSAGE, "");

        if (submission != null && initiation != null) {

            //Validate Instruction Identification
            JSONObject instructionIdentificationResult = PaymentSubmissionValidationUtil
                    .validateInstructionIdentification(submission, initiation);
            if (!((boolean) instructionIdentificationResult.get(ConsentExtensionConstants.IS_VALID_PAYLOAD))) {
                return instructionIdentificationResult;
            }

            //Validate End to End Identification
            JSONObject endToEndIdentificationResult = PaymentSubmissionValidationUtil
                    .validateEndToEndIdentification(submission, initiation);
            if (!((boolean) endToEndIdentificationResult.get(ConsentExtensionConstants.IS_VALID_PAYLOAD))) {
                return endToEndIdentificationResult;
            }

            //Validate Instructed Amount
            JSONObject instructedAmountResult = PaymentSubmissionValidationUtil
                    .validateInstructedAmount(submission, initiation);
            if (!((boolean) instructedAmountResult.get(ConsentExtensionConstants.IS_VALID_PAYLOAD))) {
                return instructedAmountResult;
            }


            //Validate Creditor Account
            if (submission.containsKey(ConsentExtensionConstants.CREDITOR_ACC) &&
                    initiation.containsKey(ConsentExtensionConstants.CREDITOR_ACC)) {

                JSONObject subCreditorAccount = (JSONObject) submission.get(ConsentExtensionConstants.CREDITOR_ACC);
                JSONObject initCreditorAccount = (JSONObject) initiation.get(ConsentExtensionConstants.CREDITOR_ACC);

                JSONObject creditorAccValidationResult = ConsentValidatorUtil.validateCreditorAcc(subCreditorAccount,
                        initCreditorAccount);
                if (!(boolean) creditorAccValidationResult.get(ConsentExtensionConstants.IS_VALID_PAYLOAD)) {
                    return creditorAccValidationResult;
                }
            } else {
                return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                        ErrorConstants.CREDITOR_ACC_NOT_FOUND);
            }

            //Validate Debtor Account
            if ((!submission.containsKey(ConsentExtensionConstants.DEBTOR_ACC) &&
                    initiation.containsKey(ConsentExtensionConstants.DEBTOR_ACC)) ||
                    (submission.containsKey(ConsentExtensionConstants.DEBTOR_ACC) &&
                            !initiation.containsKey(ConsentExtensionConstants.DEBTOR_ACC))) {

                return ConsentValidatorUtil.getValidationResult(ErrorConstants.RESOURCE_CONSENT_MISMATCH,
                        ErrorConstants.DEBTOR_ACC_MISMATCH);
            } else if (submission.containsKey(ConsentExtensionConstants.DEBTOR_ACC) &&
                    initiation.containsKey(ConsentExtensionConstants.DEBTOR_ACC)) {

                JSONObject subDebtorAccount = (JSONObject) submission.get(ConsentExtensionConstants.DEBTOR_ACC);
                JSONObject initDebtorAccount = (JSONObject) initiation.get(ConsentExtensionConstants.DEBTOR_ACC);

                JSONObject debtorAccValidationResult = ConsentValidatorUtil.validateDebtorAcc(subDebtorAccount,
                        initDebtorAccount);
                if (!(boolean) debtorAccValidationResult.get(ConsentExtensionConstants.IS_VALID_PAYLOAD)) {
                    return debtorAccValidationResult;
                }
            }

            //Validate Local Instrument
            if (!ConsentValidatorUtil.compareOptionalParameter(
                    submission.getAsString(ConsentExtensionConstants.LOCAL_INSTRUMENT),
                    initiation.getAsString(ConsentExtensionConstants.LOCAL_INSTRUMENT))) {

                return ConsentValidatorUtil.getValidationResult(ErrorConstants.RESOURCE_CONSENT_MISMATCH,
                        ErrorConstants.LOCAL_INSTRUMENT_MISMATCH);
            }
        }

        return validationResult;

    }
}

