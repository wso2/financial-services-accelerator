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
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.model.ExternalAPIConsentResourceRequestDTO;

import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper for pre consent persist flow external request data.
 */
public class ExternalAPIPreConsentPersistRequestDTO {

    private String consentId;
    private ExternalAPIConsentResourceRequestDTO consentResource;
    private UserGrantedDataDTO userGrantedData;
    private boolean isApproved;

    public ExternalAPIPreConsentPersistRequestDTO(String consentId,
                                                  ExternalAPIConsentResourceRequestDTO consentResource,
                                                  UserGrantedDataDTO userGrantedData, boolean isApproved) {
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

    public void setConsentResource(ExternalAPIConsentResourceRequestDTO consentResource) {
        this.consentResource = consentResource;
    }

    public UserGrantedDataDTO getUserGrantedData() {
        return userGrantedData;
    }

    public void setUserGrantedData(UserGrantedDataDTO userGrantedData) {
        this.userGrantedData = userGrantedData;
    }

    public boolean getIsApproved() {
        return isApproved;
    }

    public void setIsApproved(boolean isApproved) {
        this.isApproved = isApproved;
    }

    /**
     * Inner DTO class for user granted data.
     */
    public static class UserGrantedDataDTO {

        private Map<String, Object> authorizedResources;
        private JSONObject requestParameters;
        private String userId;

        public UserGrantedDataDTO() {
        }

        /**
         * Constructs the DTO from the persist payload.
         *
         * @param persistPayload    JSONObject with consent-related user input
         * @param requestParameters JSONObject of request parameters
         * @param userId            User identifier
         */
        public UserGrantedDataDTO(JSONObject persistPayload,
                                  JSONObject requestParameters,
                                  String userId) {

            this.authorizedResources = new HashMap<>();

            if (persistPayload != null) {
                for (String key : persistPayload.keySet()) {
                    this.authorizedResources.put(key, persistPayload.get(key));
                }
            }

            this.requestParameters = requestParameters;
            this.userId = userId;
        }

        public Map<String, Object> getAuthorizedResources() {
            return authorizedResources;
        }

        public void setAuthorizedResources(Map<String, Object> authorizedResources) {
            this.authorizedResources = authorizedResources;
        }

        public JSONObject getRequestParameters() {
            return requestParameters;
        }

        public void setRequestParameters(JSONObject requestParameters) {
            this.requestParameters = requestParameters;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }
}
