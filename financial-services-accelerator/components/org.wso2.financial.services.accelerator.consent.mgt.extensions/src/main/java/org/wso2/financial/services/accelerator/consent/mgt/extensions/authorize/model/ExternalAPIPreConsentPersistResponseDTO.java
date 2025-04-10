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

import java.util.List;
import java.util.Map;

/**
 * ExternalAPIPreConsentPersistResponseDTO
 */
public class ExternalAPIPreConsentPersistResponseDTO {

    private Map<String, Object> consentPayload;
    private Integer consentFrequency;
    private Long validityTime;
    private Boolean recurringIndicator;
    private String consentStatus;
    private String consentType;
    private Map<String, String> consentAttributes;
    private List<Authorization> authorizations;
    private List<AmendedAuthorization> amendments;

    public Map<String, Object> getConsentPayload() {
        return consentPayload;
    }

    public void setConsentPayload(Map<String, Object> consentPayload) {
        this.consentPayload = consentPayload;
    }

    public Integer getConsentFrequency() {
        return consentFrequency;
    }

    public void setConsentFrequency(Integer consentFrequency) {
        this.consentFrequency = consentFrequency;
    }

    public Long getValidityTime() {
        return validityTime;
    }

    public void setValidityTime(Long validityTime) {
        this.validityTime = validityTime;
    }

    public Boolean getRecurringIndicator() {
        return recurringIndicator;
    }

    public void setRecurringIndicator(Boolean recurringIndicator) {
        this.recurringIndicator = recurringIndicator;
    }

    public String getConsentStatus() {
        return consentStatus;
    }

    public void setConsentStatus(String consentStatus) {
        this.consentStatus = consentStatus;
    }

    public String getConsentType() {
        return consentType;
    }

    public void setConsentType(String consentType) {
        this.consentType = consentType;
    }

    public Map<String, String> getConsentAttributes() {
        return consentAttributes;
    }

    public void setConsentAttributes(Map<String, String> consentAttributes) {
        this.consentAttributes = consentAttributes;
    }

    public List<Authorization> getAuthorizations() {
        return authorizations;
    }

    public void setAuthorizations(
            List<Authorization> authorizations) {
        this.authorizations = authorizations;
    }

    public List<AmendedAuthorization> getAmendments() {
        return amendments;
    }

    public void setAmendments(
            List<AmendedAuthorization> amendments) {
        this.amendments = amendments;
    }

    /**
     * Authorization
     */
    public static class Authorization {

        private String userId;
        private String authorizationType;
        private String authorizationStatus;
        private List<Resource> consentedResources;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getAuthorizationType() {
            return authorizationType;
        }

        public void setAuthorizationType(String authorizationType) {
            this.authorizationType = authorizationType;
        }

        public String getAuthorizationStatus() {
            return authorizationStatus;
        }

        public void setAuthorizationStatus(String authorizationStatus) {
            this.authorizationStatus = authorizationStatus;
        }

        public List<Resource> getConsentedResources() {
            return consentedResources;
        }

        public void setConsentedResources(List<Resource> consentedResources) {
            this.consentedResources = consentedResources;
        }
    }

    /**
     * Amended Authorization
     */
    public static class AmendedAuthorization {

        private String authorizationId;
        private String authorizationType;
        private String authorizationStatus;
        private List<Resource> consentedResources;
        private List<AmendedResource> amendedResources;

        public String getAuthorizationId() {
            return authorizationId;
        }

        public void setAuthorizationId(String userId) {
            this.authorizationId = userId;
        }

        public String getAuthorizationType() {
            return authorizationType;
        }

        public void setAuthorizationType(String authorizationType) {
            this.authorizationType = authorizationType;
        }

        public String getAuthorizationStatus() {
            return authorizationStatus;
        }

        public void setAuthorizationStatus(String authorizationStatus) {
            this.authorizationStatus = authorizationStatus;
        }

        public List<Resource> getConsentedResources() {
            return consentedResources;
        }

        public void setConsentedResources(List<Resource> consentedResources) {
            this.consentedResources = consentedResources;
        }

        public List<AmendedResource> getAmendedResources() {
            return amendedResources;
        }

        public void setAmendedResources(
                List<AmendedResource> amendedResources) {
            this.amendedResources = amendedResources;
        }
    }

    /**
     * Resource
     */
    public static class Resource {

        private String resourceId;
        private String permission;
        private String status;

        public String getResourceId() {
            return resourceId;
        }

        public void setResourceId(String resourceId) {
            this.resourceId = resourceId;
        }

        public String getPermission() {
            return permission;
        }

        public void setPermission(String permission) {
            this.permission = permission;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    /**
     * Amended Resource
     */
    public static class AmendedResource {

        private String mappingId;
        private String permission;
        private String status;

        public String getMappingId() {
            return mappingId;
        }

        public void setMappingId(String mappingId) {
            this.mappingId = mappingId;
        }

        public String getPermission() {
            return permission;
        }

        public void setPermission(String permission) {
            this.permission = permission;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}

