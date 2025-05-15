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

package org.wso2.financial.services.accelerator.consent.mgt.dao.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Model for Detailed Consent Resource.
 */
public class DetailedConsentResource {

    private String orgInfo;
    private String consentId;
    private String clientId;
    private String receipt;
    private String consentType;
    private String currentStatus;
    private long expiryTime;
    private long createdTime;
    private long updatedTime;
    private boolean recurringIndicator;
    private Map<String, String> consentAttributes;
    private ArrayList<AuthorizationResource> authorizationResources;
    private ArrayList<ConsentMappingResource> consentMappingResources;

    public DetailedConsentResource() {

    }

    public DetailedConsentResource(String consentId, String clientId, String receipt,
                                   String consentType,
                                   String currentStatus, long expiryTime, long createdTime,
                                   long updatedTime, boolean recurringIndicator,
                                   Map<String, String> consentAttributes,
                                   ArrayList<AuthorizationResource> authorizationResources,
                                   ArrayList<ConsentMappingResource> consentMappingResources) {
        this.consentId = consentId;
        this.clientId = clientId;
        this.receipt = receipt;
        this.consentType = consentType;
        this.currentStatus = currentStatus;
        this.expiryTime = expiryTime;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
        this.recurringIndicator = recurringIndicator;
        this.consentAttributes = consentAttributes;
        this.authorizationResources = authorizationResources;
        this.consentMappingResources = consentMappingResources;

    }

    public DetailedConsentResource(String orgInfo, String consentId, String clientId, String receipt,
                                   String consentType,
                                   String currentStatus, long expiryTime, long createdTime,
                                   long updatedTime, boolean recurringIndicator,
                                   Map<String, String> consentAttributes,
                                   ArrayList<AuthorizationResource> authorizationResources,
                                   ArrayList<ConsentMappingResource> consentMappingResources) {
        this.orgInfo = orgInfo;
        this.consentId = consentId;
        this.clientId = clientId;
        this.receipt = receipt;
        this.consentType = consentType;
        this.currentStatus = currentStatus;
        this.expiryTime = expiryTime;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
        this.recurringIndicator = recurringIndicator;
        this.consentAttributes = consentAttributes;
        this.authorizationResources = authorizationResources;
        this.consentMappingResources = consentMappingResources;

    }

    public long getUpdatedTime() {

        return updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {

        this.updatedTime = updatedTime;
    }

    public String getConsentId() {

        return consentId;
    }

    public String getOrgInfo() {
        return orgInfo;
    }

    public void setOrgID(String orgInfo) {
        this.orgInfo = orgInfo;
    }

    public void setConsentId(String consentId) {

        this.consentId = consentId;
    }

    public String getClientId() {

        return clientId;
    }


    public void setClientId(String clientId) {

        this.clientId = clientId;
    }

    public String getReceipt() {

        return receipt;
    }

    public void setReceipt(String receipt) {

        this.receipt = receipt;
    }

    public String getConsentType() {

        return consentType;
    }

    public void setConsentType(String consentType) {

        this.consentType = consentType;
    }

    public long getExpiryTime() {

        return expiryTime;
    }

    public void setExpiryTime(long expiryTime) {

        this.expiryTime = expiryTime;
    }

    public boolean isRecurringIndicator() {

        return recurringIndicator;
    }

    public void setRecurringIndicator(boolean recurringIndicator) {

        this.recurringIndicator = recurringIndicator;
    }

    public boolean getRecurringIndicator() {

        return recurringIndicator;
    }

    public String getCurrentStatus() {

        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {

        this.currentStatus = currentStatus;
    }

    public long getCreatedTime() {

        return createdTime;
    }

    public void setCreatedTime(long createdTime) {

        this.createdTime = createdTime;
    }

    public Map<String, String> getConsentAttributes() {

        return consentAttributes;
    }

    public void setConsentAttributes(Map<String, String> consentAttributes) {

        this.consentAttributes = consentAttributes;
    }

    public ArrayList<AuthorizationResource> getAuthorizationResources() {

        return authorizationResources;
    }

    public void setAuthorizationResources(ArrayList<AuthorizationResource> authorizationResources) {

        this.authorizationResources = authorizationResources;
    }

    public ArrayList<ConsentMappingResource> getConsentMappingResources() {

        return consentMappingResources;
    }

    public void setConsentMappingResources(ArrayList<ConsentMappingResource> consentMappingResources) {

        this.consentMappingResources = consentMappingResources;
    }

    public DetailedConsentResource clone() {
        Map<String, String> copiedConsentAttributes = new HashMap<>();
        if (this.consentAttributes != null) {
            copiedConsentAttributes.putAll(this.consentAttributes);
        }

        // Deep copy of authorizationResources
        ArrayList<AuthorizationResource> copiedAuthResources = new ArrayList<>();
        if (this.authorizationResources != null) {
            for (AuthorizationResource auth : this.authorizationResources) {
                AuthorizationResource authClone = new AuthorizationResource();
                authClone.setAuthorizationId(auth.getAuthorizationId());
                authClone.setUserId(auth.getUserId());
                authClone.setAuthorizationType(auth.getAuthorizationType());
                authClone.setAuthorizationStatus(auth.getAuthorizationStatus());
                // Deep copy of consentMappingResources
                ArrayList<ConsentMappingResource> copiedConsentMappingResources = new ArrayList<>();

                copiedAuthResources.add(authClone);
            }
        }

        // Deep copy of consentMappingResources
        ArrayList<ConsentMappingResource> copiedMappingResources = new ArrayList<>();
        if (this.consentMappingResources != null) {
            for (ConsentMappingResource mapping : this.consentMappingResources) {
                ConsentMappingResource mappingClone =  new ConsentMappingResource();
                mappingClone.setMappingId(mapping.getMappingId());
                mappingClone.setResource(mapping.getResource());
                mappingClone.setMappingStatus(mapping.getMappingStatus());
                copiedMappingResources.add(mappingClone); // assuming .clone() exists
            }
        }

        // Create new instance
        return new DetailedConsentResource(
                this.orgInfo,
                this.consentId,
                this.clientId,
                this.receipt,
                this.consentType,
                this.currentStatus,
                this.expiryTime,
                this.createdTime,
                this.updatedTime,
                this.recurringIndicator,
                copiedConsentAttributes,
                copiedAuthResources,
                copiedMappingResources
        );
    }
}
