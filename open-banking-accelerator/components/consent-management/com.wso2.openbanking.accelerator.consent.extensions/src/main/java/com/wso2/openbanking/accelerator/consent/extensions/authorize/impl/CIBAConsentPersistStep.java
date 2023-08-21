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
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Consent persistence step for CIBA flow.
 */
public class CIBAConsentPersistStep implements ConsentPersistStep {

    @Override
    public void execute(ConsentPersistData consentPersistData) throws ConsentException {

        try {
            ConsentData consentData = consentPersistData.getConsentData();
            Map<String, Serializable> sensitiveDataMap = consentData.getSensitiveDataMap();

            ConsentResource consentResource;

            if (consentData.getConsentResource() == null) {
                consentResource = ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                        .getConsent(consentData.getConsentId(), false);
            } else {
                consentResource = consentData.getConsentResource();
            }

            if (sensitiveDataMap != null) {
                //Storing mapping to be used to bind consent id scope for CIBA flows
                if (sensitiveDataMap.containsKey("auth_req_id")) {
                    Map<String, String> consentAttributes = new HashMap<>();
                    consentAttributes.put("auth_req_id", (String) consentData.getSensitiveDataMap().get("auth_req_id"));
                    ConsentExtensionsDataHolder.getInstance().getConsentCoreService().storeConsentAttributes
                            (consentResource.getConsentID(), consentAttributes);
                }
            }
        } catch (ConsentManagementException e) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Exception occured while persisting consent");
        }
    }
}
