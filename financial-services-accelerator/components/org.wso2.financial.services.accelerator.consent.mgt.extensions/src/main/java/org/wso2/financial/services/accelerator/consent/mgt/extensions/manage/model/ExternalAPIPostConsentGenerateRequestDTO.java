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
package org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model;

import org.json.JSONObject;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;

/**
 * ExternalAPIPostConsentGenerateRequestDTO
 */
public class ExternalAPIPostConsentGenerateRequestDTO {

    private String consentId;
    private ConsentResource consentResource;
    private String resourcePath;


    public ExternalAPIPostConsentGenerateRequestDTO(ConsentResource consentResource, String resourcePath) {

        this.consentId = consentResource.getConsentID();
        this.consentResource = consentResource;
        this.resourcePath = resourcePath;

    }

    public String getConsentId() {
        return consentId;
    }

    public void setConsentId(String consentId) {
        this.consentId = consentId;
    }

    public ConsentResource getConsentResource() {
        return consentResource;
    }

    public void setConsentResource(
            ConsentResource consentResource) {
        this.consentResource = consentResource;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    /**
     * Convert the dto to a JSON object with correct consent resource format.
     *
     * @return JSON object
     */
    public JSONObject toJson() {

        JSONObject dtoJson = new JSONObject(this);
        JSONObject consentResourceJson = this.consentResource.toJson();
        dtoJson.put("consentResource", consentResourceJson);
        return dtoJson;
    }
}
