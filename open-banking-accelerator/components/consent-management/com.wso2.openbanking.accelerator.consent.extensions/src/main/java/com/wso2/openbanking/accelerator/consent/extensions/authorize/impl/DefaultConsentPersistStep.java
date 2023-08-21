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


package com.wso2.openbanking.accelerator.consent.extensions.authorize.impl;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.impl.handler.persist.ConsentPersistenceHandler;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.common.factory.AcceleratorConsentExtensionFactory;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Consent persist step default implementation.
 */
public class DefaultConsentPersistStep implements ConsentPersistStep {

    private static final Log log = LogFactory.getLog(DefaultConsentPersistStep.class);

    @Override
    public void execute(ConsentPersistData consentPersistData) throws ConsentException {

        try {
            ConsentData consentData = consentPersistData.getConsentData();
            ConsentResource consentResource;

            if (consentData.getConsentId() == null && consentData.getConsentResource() == null) {
                log.error("Consent ID not available in consent data");
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        "Consent ID not available in consent data");
            }

            if (consentData.getConsentResource() == null) {
                consentResource = ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                        .getConsent(consentData.getConsentId(), false);
            } else {
                consentResource = consentData.getConsentResource();
            }

            if (consentData.getAuthResource() == null) {
                log.error("Auth resource not available in consent data");
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        "Auth resource not available in consent data");
            }

            //Bind the user and accounts with the consent
            String type = consentResource.getConsentType();
            ConsentPersistenceHandler consentPersistenceHandler = AcceleratorConsentExtensionFactory
                    .getConsentPersistenceHandler(type);

            consentPersistenceHandler.consentPersist(consentPersistData, consentResource);

        } catch (ConsentManagementException e) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Exception occured while persisting consent");
        }
    }
}
