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

import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;

/**
 * ExternalAPIPostConsentGenerateRequestDTO
 */
public class ExternalAPIPostConsentGenerateRequestDTO {

    private String consentId;
    private String status;
    private long updateTime;
    private long createdTime;
    private Object consentPayload;

    public ExternalAPIPostConsentGenerateRequestDTO(DetailedConsentResource consentResource) {

        this.consentId = consentResource.getConsentID();
        this.status = consentResource.getCurrentStatus();
        this.updateTime = consentResource.getUpdatedTime();
        this.createdTime = consentResource.getCreatedTime();
        this.consentPayload = consentResource.getReceipt();
    }

    public String getConsentId() {
        return consentId;
    }

    public void setConsentId(String consentId) {
        this.consentId = consentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public Object getConsentPayload() {
        return consentPayload;
    }

    public void setConsentPayload(Object consentPayload) {
        this.consentPayload = consentPayload;
    }
}
