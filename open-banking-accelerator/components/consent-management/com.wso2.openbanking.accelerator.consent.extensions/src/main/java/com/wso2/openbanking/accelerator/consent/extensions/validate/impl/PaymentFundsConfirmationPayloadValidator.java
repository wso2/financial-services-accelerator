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
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidateData;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidationResult;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

/**
 * Class for validating Payments funds confirmation requests.
 */
public class PaymentFundsConfirmationPayloadValidator {

    private static Log log = LogFactory.getLog(PaymentFundsConfirmationPayloadValidator.class);

    /**
     * MEthod to validate Payment Funds Confirmation requests.
     *
     * @param consentValidateData         Object with request data
     * @param consentValidationResult     Validation result object to return
     * @param detailedConsentResource     detailed consent resource retrieved from database
     */
    public void validatePaymentFundsConfirmationRequest(ConsentValidateData consentValidateData,
                                                        ConsentValidationResult consentValidationResult,
                                                        DetailedConsentResource detailedConsentResource) {

        //Consent Status Validation
        if (!ConsentExtensionConstants.AUTHORISED_STATUS
                .equalsIgnoreCase(consentValidateData.getComprehensiveConsent().getCurrentStatus())) {
            consentValidationResult.setErrorMessage(ErrorConstants.PAYMENT_CONSENT_STATE_INVALID);
            consentValidationResult.setErrorCode(ErrorConstants.RESOURCE_INVALID_CONSENT_STATUS);
            consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        //Validate Consent Id From path
        //ResourcePath comes in format /pisp/domestic-scheduled-consents/{consentId}/funds-confirmation
        String[] requestPathArray = ((String) consentValidateData.getResourceParams().get("ResourcePath"))
                .trim().split("/");

        if (requestPathArray.length != 5  || StringUtils.isEmpty(requestPathArray[3])) {
            log.error(ErrorConstants.CONSENT_ID_NOT_FOUND);
            consentValidationResult.setErrorMessage(ErrorConstants.CONSENT_ID_NOT_FOUND);
            consentValidationResult.setErrorCode(ErrorConstants.RESOURCE_CONSENT_MISMATCH);
            consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        //Validate whether consentId from path matches
        String consentIdFromPath = requestPathArray[3];
        if (consentIdFromPath == null || !consentIdFromPath.equals(detailedConsentResource.getConsentID())) {
            log.error(ErrorConstants.MSG_INVALID_CONSENT_ID);
            consentValidationResult.setErrorMessage(ErrorConstants.MSG_INVALID_CONSENT_ID);
            consentValidationResult.setErrorCode(ErrorConstants.RESOURCE_CONSENT_MISMATCH);
            consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        consentValidationResult.setValid(true);
    }
}

