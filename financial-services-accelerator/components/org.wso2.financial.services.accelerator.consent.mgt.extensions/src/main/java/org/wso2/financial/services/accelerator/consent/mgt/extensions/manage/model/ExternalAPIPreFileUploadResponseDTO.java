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
package org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model;

/**
 * ExternalAPIValidateFileUploadResponseDTO
 */
public class ExternalAPIPreFileUploadResponseDTO {

    private String applicableConsentStatus;
    private String newConsentStatus;
    private String userId;

    public String getApplicableConsentStatus() {
        return applicableConsentStatus;
    }

    public void setApplicableConsentStatus(String applicableConsentStatus) {
        this.applicableConsentStatus = applicableConsentStatus;
    }

    public String getNewConsentStatus() {
        return newConsentStatus;
    }

    public void setNewConsentStatus(String newConsentStatus) {
        this.newConsentStatus = newConsentStatus;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
