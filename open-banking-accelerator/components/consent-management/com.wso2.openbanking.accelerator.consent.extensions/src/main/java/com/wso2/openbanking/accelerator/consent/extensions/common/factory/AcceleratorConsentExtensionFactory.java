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
package com.wso2.openbanking.accelerator.consent.extensions.common.factory;

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.manage.impl.AccountConsentManageRequestHandler;
import com.wso2.openbanking.accelerator.consent.extensions.manage.impl.CofConsentRequestHandler;
import com.wso2.openbanking.accelerator.consent.extensions.manage.impl.ConsentManageRequestHandler;
import com.wso2.openbanking.accelerator.consent.extensions.manage.impl.PaymentConsentRequestHandler;
import com.wso2.openbanking.accelerator.consent.extensions.manage.impl.VRPConsentRequestHandler;

/**
 * Factory class to get the class based in request type.
 */
public class AcceleratorConsentExtensionFactory {
    /**
     * Method to get the Consent Manage Request Validator.
     *
     * @param requestPath  Request path of the request
     * @return ConsentManageRequestValidator
     */
    public static ConsentManageRequestHandler getConsentManageRequestValidator(String requestPath) {

        ConsentManageRequestHandler consentManageRequestHandler = null;

        switch (requestPath) {
            case ConsentExtensionConstants.ACCOUNT_CONSENT_GET_PATH:
                consentManageRequestHandler = new AccountConsentManageRequestHandler();
                break;
            case ConsentExtensionConstants.COF_CONSENT_PATH:
                consentManageRequestHandler = new CofConsentRequestHandler();
                break;
            case ConsentExtensionConstants.PAYMENT_CONSENT_PATH:
                consentManageRequestHandler = new PaymentConsentRequestHandler();
                break;
            case ConsentExtensionConstants.VRP_CONSENT_PATH:
                consentManageRequestHandler = new VRPConsentRequestHandler();
                break;
            default:
                return null;
        }
        return consentManageRequestHandler;
    }
}
