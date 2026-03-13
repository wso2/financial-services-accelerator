/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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

import java.util.List;
import java.util.Map;

/**
 * Internal Consent Update Request DTO.
 */
public class InternalConsentUpdateRequestDTO {

    private String consentID;
    private String receipt;
    private String status;
    private int consentFrequency;
    private long validityPeriod;
    private boolean recurringIndicator;
    private Map<String, String> consentAttributes;
    private List<Authorization> authorizationResources;

    public String getConsentID() {
        return consentID;
    }

    public void setConsentID(String consentID) {
        this.consentID = consentID;
    }

    public String getReceipt() {
        return receipt;
    }

    public void setReceipt(String receipt) {
        this.receipt = receipt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getConsentFrequency() {
        return consentFrequency;
    }

    public void setConsentFrequency(int consentFrequency) {
        this.consentFrequency = consentFrequency;
    }

    public long getValidityPeriod() {
        return validityPeriod;
    }

    public void setValidityPeriod(long validityPeriod) {
        this.validityPeriod = validityPeriod;
    }

    public boolean isRecurringIndicator() {
        return recurringIndicator;
    }

    public void setRecurringIndicator(boolean recurringIndicator) {
        this.recurringIndicator = recurringIndicator;
    }

    public Map<String, String> getConsentAttributes() {
        return consentAttributes;
    }

    public void setConsentAttributes(Map<String, String> consentAttributes) {
        this.consentAttributes = consentAttributes;
    }

    public List<Authorization> getAuthorizationResources() {
        return authorizationResources;
    }

    public void setAuthorizationResources(List<Authorization> authorizationResources) {
        this.authorizationResources = authorizationResources;
    }

    /**
     * Authorization.
     */
    public static class Authorization {

        private String authorizationID;
        private String userID;
        private String authorizationType;
        private String authorizationStatus;
        private List<Resource> resources;

        public String getAuthorizationID() {
            return authorizationID;
        }

        public void setAuthorizationID(String authorizationID) {
            this.authorizationID = authorizationID;
        }

        public String getUserID() {
            return userID;
        }

        public void setUserID(String userID) {
            this.userID = userID;
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

        public List<Resource> getResources() {
            return resources;
        }

        public void setResources(List<Resource> resources) {
            this.resources = resources;
        }
    }

    /**
     * Consent Mapping Resource.
     */
    public static class Resource {

        private String mappingID;
        private String accountID;
        private String permission;
        private String mappingStatus;

        public String getMappingID() {
            return mappingID;
        }

        public void setMappingID(String mappingID) {
            this.mappingID = mappingID;
        }

        public String getAccountID() {
            return accountID;
        }

        public void setAccountID(String accountID) {
            this.accountID = accountID;
        }

        public String getPermission() {
            return permission;
        }

        public void setPermission(String permission) {
            this.permission = permission;
        }

        public String getMappingStatus() {
            return mappingStatus;
        }

        public void setMappingStatus(String mappingStatus) {
            this.mappingStatus = mappingStatus;
        }
    }
}
