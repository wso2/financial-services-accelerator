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
import java.util.Map;

/**
 * Model for Detailed Consent Resource.
 */
public class DetailedConsentResource {

    private String orgID;
    private String consentID;
    private String clientID;
    private String receipt;
    private String consentType;
    private String currentStatus;
    private int consentFrequency;
    private long validityPeriod;
    private long createdTime;
    private long updatedTime;
    private boolean recurringIndicator;
    private Map<String, String> consentAttributes;
    private ArrayList<AuthorizationResource> authorizationResources;
    private ArrayList<ConsentMappingResource> consentMappingResources;

    public DetailedConsentResource() {

    }

    public DetailedConsentResource(String consentID, String clientID, String receipt,
                                   String consentType,
                                   String currentStatus, int consentFrequency, long validityPeriod, long createdTime,
                                   long updatedTime, boolean recurringIndicator,
                                   Map<String, String> consentAttributes,
                                   ArrayList<AuthorizationResource> authorizationResources,
                                   ArrayList<ConsentMappingResource> consentMappingResources) {
        this.consentID = consentID;
        this.clientID = clientID;
        this.receipt = receipt;
        this.consentType = consentType;
        this.currentStatus = currentStatus;
        this.consentFrequency = consentFrequency;
        this.validityPeriod = validityPeriod;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
        this.recurringIndicator = recurringIndicator;
        this.consentAttributes = consentAttributes;
        this.authorizationResources = authorizationResources;
        this.consentMappingResources = consentMappingResources;

    }

    public DetailedConsentResource(String orgID, String consentID, String clientID, String receipt,
                                   String consentType,
                                   String currentStatus, int consentFrequency, long validityPeriod, long createdTime,
                                   long updatedTime, boolean recurringIndicator,
                                   Map<String, String> consentAttributes,
                                   ArrayList<AuthorizationResource> authorizationResources,
                                   ArrayList<ConsentMappingResource> consentMappingResources) {
        this.orgID = orgID;
        this.consentID = consentID;
        this.clientID = clientID;
        this.receipt = receipt;
        this.consentType = consentType;
        this.currentStatus = currentStatus;
        this.consentFrequency = consentFrequency;
        this.validityPeriod = validityPeriod;
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

    public String getConsentID() {

        return consentID;
    }

    public String getOrgID() {
        return orgID;
    }

    public void setOrgID(String orgInfo) {
        this.orgID = orgInfo;
    }

    public void setConsentID(String consentID) {

        this.consentID = consentID;
    }

    public String getClientID() {

        return clientID;
    }


    public void setClientID(String clientID) {

        this.clientID = clientID;
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

    public int getConsentFrequency() {

        return consentFrequency;
    }

    public void setConsentFrequency(int consentFrequency) {

        this.consentFrequency = consentFrequency;
    }

    public long getValidityPeriod() {

        return validityPeriod;
    }

    public void setValidityPeriod(long validityPeriod) {

        this.validityPeriod = validityPeriod;
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
        return new DetailedConsentResource(this.orgID, this.consentID, this.clientID, this.receipt, this.consentType,
                this.currentStatus, this.consentFrequency, this.validityPeriod, this.createdTime, this.updatedTime,
                this.recurringIndicator, this.consentAttributes, this.authorizationResources,
                this.consentMappingResources);


    }
}
