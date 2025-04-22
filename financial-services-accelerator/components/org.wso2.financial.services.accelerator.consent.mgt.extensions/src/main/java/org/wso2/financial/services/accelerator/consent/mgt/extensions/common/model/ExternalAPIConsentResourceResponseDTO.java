package org.wso2.financial.services.accelerator.consent.mgt.extensions.common.model;

import java.util.List;
import java.util.Map;

/**
 * Consent resource response model for the API extension consent management.
 */
public class ExternalAPIConsentResourceResponseDTO {

    private String type;
    private String status;
    private Integer frequency;
    private Long validityTime;
    private Boolean recurringIndicator;
    private Map<String, Object> receipt;
    private Map<String, String> attributes;
    private List<Authorization> authorizations;
    private List<AmendedAuthorization> amendments;

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

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
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
        private String type;
        private String status;
        private List<Resource> resources;

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

        private String accountId;
        private String permission;
        private String status;

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

    /**
     * Amended Authorization
     */
    public static class AmendedAuthorization {

        private String id;
        private String type;
        private String status;
        private List<Resource> resources;
        private List<AmendedResource> amendedResources;

        public String getId() {
            return id;
        }

        public void setId(String userId) {
            this.id = userId;
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

        public List<AmendedResource> getAmendedResources() {
            return amendedResources;
        }

        public void setAmendedResources(
                List<AmendedResource> amendedResources) {
            this.amendedResources = amendedResources;
        }
    }

    /**
     * Amended Consent Mapping Resource
     */
    public static class AmendedResource {

        private String id;
        private String permission;
        private String status;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
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
