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

import java.util.List;

import javax.validation.constraints.NotEmpty;

/**
 * Permissions object for external API consent retrieval.
 */
public class PermissionDTO {
    @NotEmpty
    private String uid;
    @NotEmpty
    private List<String> displayValues;
    private List<AccountDTO> initiatedAccounts;

    public List<String> getDisplayValues() {
        return displayValues;
    }

    public void setDisplayValues(List<String> displayValues) {
        this.displayValues = displayValues;
    }

    public List<AccountDTO> getInitiatedAccounts() {
        return initiatedAccounts;
    }

    public void setInitiatedAccounts(
            List<AccountDTO> initiatedAccounts) {
        this.initiatedAccounts = initiatedAccounts;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
