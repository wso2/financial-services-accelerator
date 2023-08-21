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

package com.wso2.openbanking.accelerator.consent.extensions.validate.util;

import com.wso2.openbanking.accelerator.common.util.ErrorConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

/**
 * Util class for Payment Submission Validation.
 */
public class PaymentSubmissionValidationUtil {

    public static JSONObject validateInstructionIdentification (JSONObject submission, JSONObject initiation) {
        if (submission.containsKey(ConsentExtensionConstants.INSTRUCTION_IDENTIFICATION)) {
            if (StringUtils.isEmpty(submission.getAsString(ConsentExtensionConstants.INSTRUCTION_IDENTIFICATION))
                    || !ConsentValidatorUtil.compareMandatoryParameter(
                    submission.getAsString(ConsentExtensionConstants.INSTRUCTION_IDENTIFICATION),
                    initiation.getAsString(ConsentExtensionConstants.INSTRUCTION_IDENTIFICATION))) {
                return ConsentValidatorUtil.getValidationResult(ErrorConstants.RESOURCE_CONSENT_MISMATCH,
                        ErrorConstants.INSTRUCTION_IDENTIFICATION_MISMATCH);
            }
        }
        return ConsentValidatorUtil.getSuccessValidationResult();
    }

    public static JSONObject validateEndToEndIdentification (JSONObject submission, JSONObject initiation) {

        if (submission.containsKey(ConsentExtensionConstants.END_TO_END_IDENTIFICATION)) {
            if (StringUtils.isEmpty(submission.getAsString(ConsentExtensionConstants.END_TO_END_IDENTIFICATION))
                    || !ConsentValidatorUtil.compareMandatoryParameter(
                    submission.getAsString(ConsentExtensionConstants.END_TO_END_IDENTIFICATION),
                    initiation.getAsString(ConsentExtensionConstants.END_TO_END_IDENTIFICATION))) {
                return ConsentValidatorUtil.getValidationResult(ErrorConstants.RESOURCE_CONSENT_MISMATCH,
                        ErrorConstants.END_TO_END_IDENTIFICATION_MISMATCH);
            }
        } else {
            return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                    ErrorConstants.END_TO_END_IDENTIFICATION_NOT_FOUND);
        }

        return ConsentValidatorUtil.getSuccessValidationResult();
    }

    public static JSONObject validateInstructedAmount (JSONObject submission, JSONObject initiation) {

        if (submission.containsKey(ConsentExtensionConstants.INSTRUCTED_AMOUNT)) {

            JSONObject subInstrAmount = (JSONObject) submission.get(ConsentExtensionConstants.INSTRUCTED_AMOUNT);
            JSONObject initInstrAmount = (JSONObject) initiation.get(ConsentExtensionConstants.INSTRUCTED_AMOUNT);

            if (subInstrAmount.containsKey(ConsentExtensionConstants.AMOUNT)) {
                if (StringUtils.isEmpty(subInstrAmount.getAsString(ConsentExtensionConstants.AMOUNT)) ||
                        !ConsentValidatorUtil.compareMandatoryParameter(
                                subInstrAmount.getAsString(ConsentExtensionConstants.AMOUNT),
                                initInstrAmount.getAsString(ConsentExtensionConstants.AMOUNT))) {
                    return ConsentValidatorUtil.getValidationResult(ErrorConstants.RESOURCE_CONSENT_MISMATCH,
                            ErrorConstants.INSTRUCTED_AMOUNT_AMOUNT_MISMATCH);
                }
            }  else {
                return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                        ErrorConstants.INSTRUCTED_AMOUNT_AMOUNT_NOT_FOUND);
            }

            if (subInstrAmount.containsKey(ConsentExtensionConstants.CURRENCY)) {
                if (StringUtils.isEmpty(subInstrAmount.getAsString(ConsentExtensionConstants.CURRENCY)) ||
                        !ConsentValidatorUtil.compareMandatoryParameter(
                                subInstrAmount.getAsString(ConsentExtensionConstants.CURRENCY),
                                initInstrAmount.getAsString(ConsentExtensionConstants.CURRENCY))) {
                    return ConsentValidatorUtil.getValidationResult(ErrorConstants.RESOURCE_CONSENT_MISMATCH,
                            ErrorConstants.INSTRUCTED_AMOUNT_CURRENCY_MISMATCH);
                }
            }  else {
                return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                        ErrorConstants.INSTRUCTED_AMOUNT_CURRENCY_NOT_FOUND);
            }
        } else {
            return ConsentValidatorUtil.getValidationResult(ErrorConstants.FIELD_MISSING,
                    ErrorConstants.INSTRUCTED_AMOUNT_NOT_FOUND);
        }

        return ConsentValidatorUtil.getSuccessValidationResult();
    }

}

