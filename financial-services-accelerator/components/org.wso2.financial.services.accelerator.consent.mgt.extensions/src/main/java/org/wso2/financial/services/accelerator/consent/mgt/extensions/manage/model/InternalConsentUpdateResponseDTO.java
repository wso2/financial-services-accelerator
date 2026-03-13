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

import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Internal Consent Update Request DTO.
 */
public class InternalConsentUpdateResponseDTO {

    private String consentID;
    private String clientID;
    private String receipt;
    private String status;
    private String consentType;
    private int consentFrequency;
    private long validityPeriod;
    private boolean recurringIndicator;
    private long createdTime;
    private long updatedTime;
    private Map<String, String> consentAttributes;
    private List<Authorization> authorizationResources;

    public InternalConsentUpdateResponseDTO(DetailedConsentResource detailedConsent) {

        this.consentID = detailedConsent.getConsentID();
        this.clientID = detailedConsent.getClientID();
        this.consentType = detailedConsent.getConsentType();
        this.status = detailedConsent.getCurrentStatus();
        this.consentFrequency = detailedConsent.getConsentFrequency();
        this.validityPeriod = detailedConsent.getValidityPeriod();
        this.createdTime = detailedConsent.getCreatedTime();
        this.updatedTime = detailedConsent.getUpdatedTime();
        this.recurringIndicator = detailedConsent.isRecurringIndicator();

        // Convert receipt String to Map<String, Object>
        if (detailedConsent.getReceipt() != null && !detailedConsent.getReceipt().isEmpty()) {
            this.receipt = detailedConsent.getReceipt();
        } else {
            this.receipt = null;
        }

        this.consentAttributes = detailedConsent.getConsentAttributes();

        // Group ConsentMappingResources by AuthorizationID
        Map<String, List<ConsentMappingResource>> mappingsByAuthId = Optional.ofNullable(
                        detailedConsent.getConsentMappingResources())
                .orElse(new ArrayList<>())
                .stream()
                .collect(Collectors.groupingBy(ConsentMappingResource::getAuthorizationID));

        // Build authorizations list
        this.authorizationResources = Optional.ofNullable(detailedConsent.getAuthorizationResources())
                .orElse(new ArrayList<>())
                .stream()
                .map(auth -> {
                    InternalConsentUpdateResponseDTO.Authorization authorization =
                            new InternalConsentUpdateResponseDTO.Authorization();
                    authorization.setAuthorizationID(auth.getAuthorizationID());
                    authorization.setUserID(auth.getUserID());
                    authorization.setAuthorizationType(auth.getAuthorizationType());
                    authorization.setAuthorizationStatus(auth.getAuthorizationStatus());

                    List<InternalConsentUpdateResponseDTO.Resource> resources = mappingsByAuthId
                            .getOrDefault(auth.getAuthorizationID(), Collections.emptyList())
                            .stream()
                            .map(mapping -> {
                                InternalConsentUpdateResponseDTO.Resource resource =
                                        new InternalConsentUpdateResponseDTO.Resource();
                                resource.setMappingID(mapping.getMappingID());
                                resource.setAccountID(mapping.getAccountID());
                                resource.setPermission(mapping.getPermission());
                                resource.setMappingStatus(mapping.getMappingStatus());
                                return resource;
                            })
                            .collect(Collectors.toList());

                    authorization.setResources(resources);
                    return authorization;
                })
                .collect(Collectors.toList());
    }

    public String getConsentID() {
        return consentID;
    }

    public void setConsentID(String consentID) {
        this.consentID = consentID;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
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

    public String getConsentType() {
        return consentType;
    }

    public void setConsentType(String consentType) {
        this.consentType = consentType;
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

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
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
