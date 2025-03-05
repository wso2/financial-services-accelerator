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
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.AuthErrorCode;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;

import java.util.Map;

/**
 * Financial Services Consent Retrieval Step Policy for Consent Status Validation.
 */
public class ConsentStatusValidationPolicy extends ConsentRetrievalStepPolicy {

    @Override
    public void execute(ConsentData consentData, JSONObject jsonObject, Map<String, Object> propertyMap,
                        Map<String, Object> retrievalContext) throws ConsentException {

        // Validate consent status.
        ConsentResource consentResource = (ConsentResource) retrievalContext.get("consentResource");
        if (!ConsentExtensionConstants.AWAIT_AUTHORISE_STATUS.equals(consentResource.getCurrentStatus())) {
            throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.INVALID_REQUEST,
                    "Consent not in authorizable state", consentData.getState());
        }

        // Validate authorization status.
        AuthorizationResource authorizationResource = (AuthorizationResource) retrievalContext.get(
                "authorizationResource");
        if (!authorizationResource.getAuthorizationStatus().equals(ConsentExtensionConstants.CREATED_STATUS)) {
            throw new ConsentException(consentData.getRedirectURI(), AuthErrorCode.INVALID_REQUEST,
                    "Authorisation not in authorizable state", consentData.getState());
        }
    }

}
