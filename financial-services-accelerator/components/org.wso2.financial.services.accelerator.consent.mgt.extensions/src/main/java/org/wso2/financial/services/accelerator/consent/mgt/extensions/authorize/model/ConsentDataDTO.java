/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public class ConsentDataDTO {
    @NotEmpty
    private String type;

    @Valid
    @NotNull
    private Map<String, Object> basicConsentData;

    @Valid
    private List<PermissionsDTO> permissions;

    @Valid
    private List<AccountDTO> initiatedAccountsForConsent;
    private Boolean allowMultipleAccounts;
    private Boolean isReauthorization;

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

    public List<PermissionsDTO> getPermissions() {
        return permissions;
    }

    public void setPermissions(
            List<PermissionsDTO> permissions) {
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

    public Boolean getReauthorization() {
        return isReauthorization;
    }

    public void setReauthorization(Boolean reauthorization) {
        isReauthorization = reauthorization;
    }
}
