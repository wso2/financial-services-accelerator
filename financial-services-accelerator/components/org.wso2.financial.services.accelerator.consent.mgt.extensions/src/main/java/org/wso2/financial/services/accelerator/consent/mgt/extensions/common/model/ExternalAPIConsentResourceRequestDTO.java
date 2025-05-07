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
package org.wso2.financial.services.accelerator.consent.mgt.extensions.common.model;

import org.json.JSONObject;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Consent resource model for the API extension consent management.
 */
public class ExternalAPIConsentResourceRequestDTO {

    private String id;
    private String clientId;
    private String type;
    private String status;
    private int frequency;
    private long validityTime;
    private long createdTime;
    private long updatedTime;
    private boolean recurringIndicator;
    private Map<String, Object> receipt;
    private Map<String, String> attributes;
    private List<Authorization> authorizations;
    private String fileContent;

    public ExternalAPIConsentResourceRequestDTO(ConsentResource consentResource) {

        this.id = consentResource.getConsentID();
        this.clientId = consentResource.getClientID();
        this.type = consentResource.getConsentType();
        this.status = consentResource.getCurrentStatus();
        this.frequency = consentResource.getConsentFrequency();
        this.validityTime = consentResource.getValidityPeriod();
        this.createdTime = consentResource.getCreatedTime();
        this.updatedTime = consentResource.getUpdatedTime();
        this.recurringIndicator = consentResource.isRecurringIndicator();

        if (consentResource.getReceipt() != null && !consentResource.getReceipt().isEmpty()) {
            JSONObject receiptJson = new JSONObject(consentResource.getReceipt());
            this.receipt = receiptJson.toMap();
        } else {
            this.receipt = Collections.emptyMap();
        }

        this.attributes = consentResource.getConsentAttributes();

    }

    public ExternalAPIConsentResourceRequestDTO(DetailedConsentResource detailedConsent) {

        this.id = detailedConsent.getConsentID();
        this.clientId = detailedConsent.getClientID();
        this.type = detailedConsent.getConsentType();
        this.status = detailedConsent.getCurrentStatus();
        this.frequency = detailedConsent.getConsentFrequency();
        this.validityTime = detailedConsent.getValidityPeriod();
        this.createdTime = detailedConsent.getCreatedTime();
        this.updatedTime = detailedConsent.getUpdatedTime();
        this.recurringIndicator = detailedConsent.isRecurringIndicator();

        // Convert receipt String to Map<String, Object>
        if (detailedConsent.getReceipt() != null && !detailedConsent.getReceipt().isEmpty()) {
            JSONObject receiptJson = new JSONObject(detailedConsent.getReceipt());
            this.receipt = receiptJson.toMap();
        } else {
            this.receipt = Collections.emptyMap();
        }

        this.attributes = detailedConsent.getConsentAttributes();

        // Group ConsentMappingResources by AuthorizationID
        Map<String, List<ConsentMappingResource>> mappingsByAuthId = Optional.ofNullable(
                        detailedConsent.getConsentMappingResources())
                .orElse(new ArrayList<>())
                .stream()
                .collect(Collectors.groupingBy(ConsentMappingResource::getAuthorizationID));

        // Build authorizations list
        this.authorizations = Optional.ofNullable(detailedConsent.getAuthorizationResources())
                .orElse(new ArrayList<>())
                .stream()
                .map(auth -> {
                    Authorization authorization = new Authorization();
                    authorization.setId(auth.getAuthorizationID());
                    authorization.setUserId(auth.getUserID());
                    authorization.setType(auth.getAuthorizationType());
                    authorization.setStatus(auth.getAuthorizationStatus());

                    List<Resource> resources = mappingsByAuthId
                            .getOrDefault(auth.getAuthorizationID(), Collections.emptyList())
                            .stream()
                            .map(mapping -> {
                                Resource resource = new Resource();
                                resource.setId(mapping.getMappingID());
                                resource.setAccountId(mapping.getAccountID());
                                resource.setPermission(mapping.getPermission());
                                resource.setStatus(mapping.getMappingStatus());
                                return resource;
                            })
                            .collect(Collectors.toList());

                    authorization.setResources(resources);
                    return authorization;
                })
                .collect(Collectors.toList());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public long getValidityTime() {
        return validityTime;
    }

    public void setValidityTime(long validityTime) {
        this.validityTime = validityTime;
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

    public boolean getRecurringIndicator() {
        return recurringIndicator;
    }

    public void setRecurringIndicator(boolean recurringIndicator) {
        this.recurringIndicator = recurringIndicator;
    }

    public Map<String, Object> getReceipt() {
        return receipt;
    }

    public void setReceipt(Map<String, Object> receipt) {
        this.receipt = receipt;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public List<Authorization> getAuthorizations() {
        return authorizations;
    }

    public void setAuthorizations(
            List<Authorization> authorizations) {
        this.authorizations = authorizations;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    /**
     * Authorization
     */
    public static class Authorization {

        private String id;
        private String userId;
        private String type;
        private String status;
        private List<Resource> resources;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public List<Resource> getResources() {
            return resources;
        }

        public void setResources(List<Resource> resources) {
            this.resources = resources;
        }
    }

    /**
     * Consent Mapping Resource
     */
    public static class Resource {

        private String id;
        private String accountId;
        private String permission;
        private String status;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
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
