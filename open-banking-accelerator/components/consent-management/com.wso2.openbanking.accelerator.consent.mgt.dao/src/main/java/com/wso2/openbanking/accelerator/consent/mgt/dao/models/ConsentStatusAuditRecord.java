/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.wso2.openbanking.accelerator.consent.mgt.dao.models;

import com.wso2.openbanking.accelerator.common.util.Generated;

/**
 * Model for consent status audit record resource.
 */
public class ConsentStatusAuditRecord {

    private String statusAuditID;
    private String consentID;
    private String currentStatus;
    private long actionTime;
    private String reason;
    private String actionBy;
    private String previousStatus;

    public ConsentStatusAuditRecord() {

    }

    @Generated(message = "Excluding constructor because setter methods are explicitly called")
    public ConsentStatusAuditRecord(String consentID, String currentStatus, long actionTime,
                                    String reason, String actionBy, String previousStatus) {
        this.consentID = consentID;
        this.currentStatus = currentStatus;
        this.actionTime = actionTime;
        this.reason = reason;
        this.actionBy = actionBy;
        this.previousStatus = previousStatus;
    }

    public String getStatusAuditID() {

        return statusAuditID;
    }

    public void setStatusAuditID(String statusAuditID) {

        this.statusAuditID = statusAuditID;
    }

    public String getConsentID() {

        return consentID;
    }

    public void setConsentID(String consentID) {

        this.consentID = consentID;
    }

    public String getCurrentStatus() {

        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {

        this.currentStatus = currentStatus;
    }

    public long getActionTime() {

        return actionTime;
    }

    public void setActionTime(long actionTime) {

        this.actionTime = actionTime;
    }

    public String getReason() {

        return reason;
    }

    public void setReason(String reason) {

        this.reason = reason;
    }

    public String getActionBy() {

        return actionBy;
    }

    public void setActionBy(String actionBy) {

        this.actionBy = actionBy;
    }

    public String getPreviousStatus() {

        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {

        this.previousStatus = previousStatus;
    }
}
