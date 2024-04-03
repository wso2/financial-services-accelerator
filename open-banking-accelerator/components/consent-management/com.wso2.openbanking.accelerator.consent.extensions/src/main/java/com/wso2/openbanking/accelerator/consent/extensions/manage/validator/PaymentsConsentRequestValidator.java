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
package com.wso2.openbanking.accelerator.consent.extensions.manage.validator;

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.util.ConsentManageUtil;
import com.wso2.openbanking.accelerator.consent.extensions.util.PaymentPayloadValidator;
import net.minidev.json.JSONObject;

/**
 * Consent Manage validator class for Payment Request Validation.
 */
public class PaymentsConsentRequestValidator {

    /**
     * Method to validate payment initiation request.
     *
     * @param requestPath     Request Path of the request
     * @param initiation      Initiation Object
     * @return JSONObject     Validation Response
     */
    public static JSONObject validatePaymentInitiation(String requestPath, JSONObject initiation) {

        JSONObject validationResponse = new JSONObject();

        //Check request body is valid and not empty
        JSONObject dataValidationResult = ConsentManageUtil.validateInitiationDataBody(initiation);
        if (!(boolean) dataValidationResult.get(ConsentExtensionConstants.IS_VALID)) {
            return dataValidationResult;
        }

        JSONObject data = (JSONObject) initiation.get(ConsentExtensionConstants.DATA);

        if (data.containsKey(ConsentExtensionConstants.INITIATION)) {
            JSONObject initiationValidationResult = PaymentPayloadValidator
                    .validatePaymentInitiationPayload(requestPath,
                            (JSONObject) data.get(ConsentExtensionConstants.INITIATION));
            if (!(boolean) initiationValidationResult.get(ConsentExtensionConstants.IS_VALID)) {
                return initiationValidationResult;
            }
        }

        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
        return validationResponse;
    }
}
