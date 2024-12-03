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

import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistData;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.model.ConsentPersistStep;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionUtils;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentAttributes;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Consent persistence step for CIBA flow.
 */
public class CIBAConsentPersistStep implements ConsentPersistStep {

    private static final Log log = LogFactory.getLog(CIBAConsentPersistStep.class);

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
                if (sensitiveDataMap.containsKey(OpenBankingConstants.AUTH_REQ_ID)) {
                    removeExistingAuthReqIdAttribute(consentResource);
                    storeAuthReqIdAttribute((String) sensitiveDataMap.get(OpenBankingConstants.AUTH_REQ_ID),
                            consentResource);
                } else if (ConsentExtensionUtils.isCibaWebAuthLinkFlow(consentData)
                        && sensitiveDataMap.containsKey(ConsentExtensionConstants.SP_QUERY_PARAMS)) {
                    String spQueryParams = sensitiveDataMap.get(ConsentExtensionConstants.SP_QUERY_PARAMS).toString();
                    Optional<String> nonce = Arrays.stream(spQueryParams.split("&"))
                            .filter(e -> e.startsWith(ConsentExtensionConstants.NONCE)).findFirst()
                            .map(e -> e.split("=")[1]);
                    if (nonce.isPresent()) {
                        removeExistingAuthReqIdAttribute(consentResource);
                        storeAuthReqIdAttribute(nonce.get(), consentResource);
                    }

                }
            }
        } catch (ConsentManagementException e) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Exception occurred while persisting consent");
        }
    }

    private static void storeAuthReqIdAttribute(String authReqId, ConsentResource consentResource)
            throws ConsentManagementException {

        Map<String, String> consentAttributes = new HashMap<>();
        consentAttributes.put(OpenBankingConstants.AUTH_REQ_ID, authReqId);
        ConsentExtensionsDataHolder.getInstance().getConsentCoreService().storeConsentAttributes
                (consentResource.getConsentID(), consentAttributes);
    }

    private static void removeExistingAuthReqIdAttribute(ConsentResource consentResource)
            throws ConsentManagementException {

        ConsentAttributes currentConsentAttributes = null;
        try {
            currentConsentAttributes = ConsentExtensionsDataHolder.getInstance()
                    .getConsentCoreService().getConsentAttributes(consentResource.getConsentID());
        } catch (ConsentManagementException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving consent attributes.", e);
            }
        }
        // Remove if existing aut_req_id is already in attributes.
        if (currentConsentAttributes != null &&
                currentConsentAttributes.getConsentAttributes().containsKey(OpenBankingConstants.AUTH_REQ_ID)) {
            ArrayList<String> toRemoveAttributes = new ArrayList<>();
            toRemoveAttributes.add(OpenBankingConstants.AUTH_REQ_ID);
            ConsentExtensionsDataHolder.getInstance().getConsentCoreService().deleteConsentAttributes(
                    consentResource.getConsentID(), toRemoveAttributes);
        }
    }
}
