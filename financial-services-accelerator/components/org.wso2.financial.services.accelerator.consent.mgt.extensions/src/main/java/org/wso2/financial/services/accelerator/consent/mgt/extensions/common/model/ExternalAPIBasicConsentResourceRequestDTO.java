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
package org.wso2.financial.services.accelerator.consent.mgt.extensions.common.model;

import org.json.JSONObject;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;

import java.util.Collections;
import java.util.Map;

/**
 * Basic Consent resource model for the API extension consent management.
 */
public class ExternalAPIBasicConsentResourceRequestDTO {

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

    public ExternalAPIBasicConsentResourceRequestDTO(ConsentResource consentResource) {

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
}
