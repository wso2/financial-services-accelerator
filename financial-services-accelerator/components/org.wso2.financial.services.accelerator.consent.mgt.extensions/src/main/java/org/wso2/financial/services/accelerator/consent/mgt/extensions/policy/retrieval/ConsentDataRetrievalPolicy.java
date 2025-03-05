/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.financial.services.accelerator.consent.mgt.extensions.policy.retrieval;

import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.util.ConsentAuthorizeUtil;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.AuthErrorCode;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;

import java.util.Map;
/**
 * Financial Services Consent Retrieval Step Policy for Consent Data Retrieval.
 */
public class ConsentDataRetrievalPolicy extends ConsentRetrievalStepPolicy {

    ConsentCoreService consentCoreService;

    public ConsentDataRetrievalPolicy() {
        consentCoreService = ConsentExtensionsDataHolder.getInstance().getConsentCoreService();
    }

    @Override
    public void execute(ConsentData consentData, JSONObject jsonObject, Map<String, Object> propertyMap,
                        Map<String, Object> retrievalContext) throws ConsentException {

        String requestObject = ConsentAuthorizeUtil.extractRequestObject(consentData.getSpQueryParams());
        String consentId = ConsentAuthorizeUtil.extractConsentId(requestObject);

        try {
            // Add data from database to the property map to be used by the policy chain.
            ConsentResource consentResource = consentCoreService.getConsent(consentId, false);
            // ToDo: Get primary user's authorization resource.
            AuthorizationResource authorizationResource = consentCoreService.searchAuthorizations(consentId).get(0);

            retrievalContext.put("consentResource", consentResource);
            retrievalContext.put("authorizationResource", authorizationResource);

            // Set the consent data to be used in consent persistence.
            consentData.setConsentId(consentId);
            consentData.setType(consentResource.getConsentType());
            consentData.setAuthResource(authorizationResource);

        } catch (ConsentManagementException e) {
            throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.SERVER_ERROR,
                    "Exception occurred while getting consent data", consentData.getState());
        }
    }

}
