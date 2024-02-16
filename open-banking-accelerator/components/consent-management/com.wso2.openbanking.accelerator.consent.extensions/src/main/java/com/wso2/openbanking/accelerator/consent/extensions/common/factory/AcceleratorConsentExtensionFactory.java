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

import com.wso2.openbanking.accelerator.consent.extensions.authorize.impl.handler.persist.AccountConsentPersistenceHandler;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.impl.handler.persist.CofConsentPersistenceHandler;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.impl.handler.persist.ConsentPersistenceHandler;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.impl.handler.persist.PaymentConsentPersistenceHandler;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.impl.handler.retrieval.AccountConsentRetrievalHandler;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.impl.handler.retrieval.CofConsentRetrievalHandler;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.impl.handler.retrieval.ConsentRetrievalHandler;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.impl.handler.retrieval.PaymentConsentRetrievalHandler;
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

    /**
     * Method to get the Consent Authorize Handler.
     *
     * @param type  Type of the request
     * @return ConsentAuthorizeHandler
     */
    public static ConsentRetrievalHandler getConsentRetrievalHandler(String type) {

        ConsentRetrievalHandler consentRetrieveHandler = null;

        if (type.equalsIgnoreCase(ConsentExtensionConstants.ACCOUNTS)) {
            consentRetrieveHandler = new AccountConsentRetrievalHandler();
        } else if (type.equalsIgnoreCase(ConsentExtensionConstants.PAYMENTS)) {
            consentRetrieveHandler = new PaymentConsentRetrievalHandler();
        } else if (type.equalsIgnoreCase(ConsentExtensionConstants.FUNDSCONFIRMATIONS)) {
            consentRetrieveHandler = new CofConsentRetrievalHandler();
        }
        return consentRetrieveHandler;

    }

    /**
     * Method to get the Consent Persistence Handler.
     *
     * @param type  Type of the request
     * @return ConsentPersistenceHandler
     */
    public static ConsentPersistenceHandler getConsentPersistenceHandler(String type) {

        ConsentPersistenceHandler consentPersistenceHandler = null;

        if (ConsentExtensionConstants.ACCOUNTS.equalsIgnoreCase(type)) {
            consentPersistenceHandler = new AccountConsentPersistenceHandler();
        } else if (ConsentExtensionConstants.PAYMENTS.equalsIgnoreCase(type)) {
            consentPersistenceHandler = new PaymentConsentPersistenceHandler();
        } else if (ConsentExtensionConstants.FUNDSCONFIRMATIONS.equalsIgnoreCase(type)) {
            consentPersistenceHandler = new CofConsentPersistenceHandler();
        }
        return consentPersistenceHandler;

    }

}
