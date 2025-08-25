/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

/**
 * Consent data object for external API consent retrieval.
 */
public class ConsentDataDTO {
    @NotEmpty(message = "Consent Type in ConsentData Object cannot be empty")
    private String type;

    @Valid
    private Map<String, Object> basicConsentData;

    @Valid
    private List<PermissionDTO> permissions;

    @Valid
    private List<AccountDTO> initiatedAccountsForConsent;
    private Boolean allowMultipleAccounts;
    private Boolean isReauthorization;
    private Map<String, Object> consentMetadata;
    private final Map<String, Object> additionalProperties = new HashMap<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getBasicConsentData() {
        return basicConsentData;
    }

    public void setBasicConsentData(Map<String, Object> basicConsentData) {
        this.basicConsentData = basicConsentData;
    }

    public List<PermissionDTO> getPermissions() {
        return permissions;
    }

    public void setPermissions(
            List<PermissionDTO> permissions) {
        this.permissions = permissions;
    }

    public List<AccountDTO> getInitiatedAccountsForConsent() {
        return initiatedAccountsForConsent;
    }

    public void setInitiatedAccountsForConsent(
            List<AccountDTO> initiatedAccountsForConsent) {
        this.initiatedAccountsForConsent = initiatedAccountsForConsent;
    }

    public Boolean getAllowMultipleAccounts() {
        return allowMultipleAccounts;
    }

    public void setAllowMultipleAccounts(Boolean allowMultipleAccounts) {
        this.allowMultipleAccounts = allowMultipleAccounts;
    }

    public Boolean getIsReauthorization() {
        return isReauthorization;
    }

    public void setIsReauthorization(Boolean reauthorization) {
        isReauthorization = reauthorization;
    }

    public Map<String, Object> getConsentMetadata() {
        return consentMetadata;
    }

    public void setConsentMetadata(Map<String, Object> consentMetadata) {
        this.consentMetadata = consentMetadata;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperties(String key, Object value) {
        this.additionalProperties.put(key, value);
    }
}
