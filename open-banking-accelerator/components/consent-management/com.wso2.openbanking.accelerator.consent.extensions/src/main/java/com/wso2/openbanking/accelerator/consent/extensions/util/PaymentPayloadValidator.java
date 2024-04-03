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
package com.wso2.openbanking.accelerator.consent.extensions.util;

import com.wso2.openbanking.accelerator.common.util.ErrorConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Validator class to validate payment initiation payload.
 */
public class PaymentPayloadValidator {

    private static final Log log = LogFactory.getLog(PaymentPayloadValidator.class);

    /**
     * Method to validate payment initiation payload.
     *
     * @param requestPath Request Path of the request
     * @param initiation  Initiation Object of the request
     * @return JSONObject Validation Response
     */
    public static JSONObject validatePaymentInitiationPayload(String requestPath, JSONObject initiation) {

        JSONObject validationResponse = new JSONObject();

        //Validate DebtorAccount
        if (initiation.containsKey(ConsentExtensionConstants.DEBTOR_ACC)) {
            JSONObject debtorAccount = (JSONObject) initiation.get(ConsentExtensionConstants.DEBTOR_ACC);
            JSONObject validationResult = ConsentManageUtil.validateDebtorAccount(debtorAccount);
            if (!(boolean) validationResult.get(ConsentExtensionConstants.IS_VALID)) {
                return validationResult;
            }
        }

        //Validate CreditorAccount
        if (initiation.containsKey(ConsentExtensionConstants.CREDITOR_ACC)) {
            JSONObject creditorAccount = (JSONObject) initiation.get(ConsentExtensionConstants.CREDITOR_ACC);
            JSONObject validationResult = ConsentManageUtil.validateCreditorAccount(creditorAccount);

            if (!(boolean) validationResult.get(ConsentExtensionConstants.IS_VALID)) {
                return validationResult;
            }
        } else {
            if (!requestPath.contains(ConsentExtensionConstants.PAYMENTS)) {
                log.error(ErrorConstants.MSG_MISSING_CREDITOR_ACC);
                return ConsentManageUtil.getValidationResponse(ErrorConstants.FIELD_MISSING,
                        ErrorConstants.MSG_MISSING_CREDITOR_ACC, ErrorConstants.PATH_CREDIT_ACCOUNT);
            }
        }

        //Validate Local Instrument
        if (initiation.containsKey(ConsentExtensionConstants.LOCAL_INSTRUMENT) && !ConsentManageUtil
                .validateLocalInstrument(initiation.getAsString(ConsentExtensionConstants.LOCAL_INSTRUMENT))) {
            log.error(ErrorConstants.INVALID_LOCAL_INSTRUMENT);
            return ConsentManageUtil.getValidationResponse(ErrorConstants.UNSUPPORTED_LOCAL_INSTRUMENTS,
                    ErrorConstants.INVALID_LOCAL_INSTRUMENT, ErrorConstants.PATH_LOCAL_INSTRUMENT);
        }


        if (!requestPath.contains(ConsentExtensionConstants.PAYMENTS)) {
            JSONObject instructedAmount = (JSONObject) initiation.get(ConsentExtensionConstants.INSTRUCTED_AMOUNT);
            if (Double.parseDouble(instructedAmount.getAsString(ConsentExtensionConstants.AMOUNT)) < 1) {
                log.error(ErrorConstants.INVALID_INSTRUCTED_AMOUNT);
                return ConsentManageUtil.getValidationResponse(ErrorConstants.FIELD_INVALID,
                        ErrorConstants.INVALID_INSTRUCTED_AMOUNT, ErrorConstants.PATH_INSTRUCTED_AMOUNT);
            }

            if (!ConsentManageUtil
                    .validateMaxInstructedAmount(instructedAmount.getAsString(ConsentExtensionConstants.AMOUNT))) {
                log.error(ErrorConstants.MAX_INSTRUCTED_AMOUNT);
                return ConsentManageUtil.getValidationResponse(ErrorConstants.FIELD_INVALID,
                        ErrorConstants.MAX_INSTRUCTED_AMOUNT, ErrorConstants.PATH_INSTRUCTED_AMOUNT);
            }

        }
        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
        return validationResponse;
    }
}
