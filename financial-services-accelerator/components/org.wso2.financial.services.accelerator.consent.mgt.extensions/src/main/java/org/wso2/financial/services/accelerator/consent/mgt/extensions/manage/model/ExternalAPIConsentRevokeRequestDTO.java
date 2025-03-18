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

import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;

import java.util.Map;

/**
 * ExternalAPIConsentRevokeRequestDTO
 */
public class ExternalAPIConsentRevokeRequestDTO {

    private String consentId;
    private String consentType;
    private String consentStatus;
    private String resourcePath;
    private Map<String, String> consentAttributes;

    public ExternalAPIConsentRevokeRequestDTO(ConsentResource consentResource, String resourcePath,
                                              Map<String, String> consentAttributes) {

        this.consentId = consentResource.getConsentID();
        this.consentType = consentResource.getConsentType();
        this.consentStatus = consentResource.getCurrentStatus();
        this.resourcePath = resourcePath;
        this.consentAttributes = consentAttributes;
    }

    public String getConsentId() {
        return consentId;
    }

    public void setConsentId(String consentId) {
        this.consentId = consentId;
    }

    public String getConsentType() {
        return consentType;
    }

    public void setConsentType(String consentType) {
        this.consentType = consentType;
    }

    public String getConsentStatus() {
        return consentStatus;
    }

    public void setConsentStatus(String consentStatus) {
        this.consentStatus = consentStatus;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public Map<String, String> getConsentAttributes() {
        return consentAttributes;
    }

    public void setConsentAttributes(Map<String, String> consentAttributes) {
        this.consentAttributes = consentAttributes;
    }
}
