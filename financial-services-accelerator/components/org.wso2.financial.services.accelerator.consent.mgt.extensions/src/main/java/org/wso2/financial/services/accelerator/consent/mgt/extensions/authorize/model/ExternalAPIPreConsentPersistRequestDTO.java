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

import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.model.ExternalAPIConsentResourceRequestDTO;

import java.util.Map;

/**
 * Wrapper for pre consent persist flow external request data.
 */
public class ExternalAPIPreConsentPersistRequestDTO {

    private String consentId;
    private ExternalAPIConsentResourceRequestDTO consentResource;
    private Map<String, Object> userGrantedData;
    private boolean isApproved;

    public ExternalAPIPreConsentPersistRequestDTO(String consentId,
                                                  ExternalAPIConsentResourceRequestDTO consentResource,
                                                  Map<String, Object> userGrantedData, boolean isApproved) {
        this.consentId = consentId;
        this.consentResource = consentResource;
        this.userGrantedData = userGrantedData;
        this.isApproved = isApproved;
    }

    public String getConsentId() {
        return consentId;
    }

    public void setConsentId(String consentId) {
        this.consentId = consentId;
    }

    public ExternalAPIConsentResourceRequestDTO getConsentResource() {
        return consentResource;
    }

    public void setConsentResource(
            ExternalAPIConsentResourceRequestDTO consentResource) {
        this.consentResource = consentResource;
    }

    public Map<String, Object> getUserGrantedData() {
        return userGrantedData;
    }

    public void setUserGrantedData(Map<String, Object> userGrantedData) {
        this.userGrantedData = userGrantedData;
    }

    public boolean getIsApproved() {
        return isApproved;
    }

    public void setIsApproved(boolean isApproved) {
        this.isApproved = isApproved;
    }
}
