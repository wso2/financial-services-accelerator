/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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
package org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model;

import org.json.JSONObject;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;

/**
 * Wrapper for pre consent authorize flow external request data.
 */
public class ExternalAPIPreConsentAuthorizeRequestDTO {

    private String consentId;
    private String userId;
    private String scope;
    private DetailedConsentResource consentResource;

    public ExternalAPIPreConsentAuthorizeRequestDTO(ConsentData consentData,
                                                    DetailedConsentResource detailedConsentResource, String scope) {

        this.consentId = consentData.getConsentId();
        this.userId = consentData.getUserId();
        this.consentResource = detailedConsentResource;
        this.scope = scope;
    }

    public String getConsentId() {
        return consentId;
    }

    public void setConsentId(String consentId) {
        this.consentId = consentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public DetailedConsentResource getConsentResource() {
        return consentResource;
    }

    public void setConsentResource(
            DetailedConsentResource consentResource) {
        this.consentResource = consentResource;
    }

    /**
     * Convert the dto to a JSON object with correct consent resource format.
     *
     * @return JSON object
     */
    public JSONObject toJson() {

        JSONObject dtoJson = new JSONObject(this);
        JSONObject consentResourceJson = this.consentResource != null ? this.consentResource.toJson() :
                new JSONObject();
        dtoJson.put("consentResource", consentResourceJson);
        return dtoJson;
    }
}
