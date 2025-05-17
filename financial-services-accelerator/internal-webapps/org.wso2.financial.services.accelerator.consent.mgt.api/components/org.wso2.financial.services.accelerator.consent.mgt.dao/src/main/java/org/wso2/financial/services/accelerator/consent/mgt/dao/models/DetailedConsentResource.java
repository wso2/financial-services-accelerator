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
public class DetailedConsentResource extends ConsentResource {


    private Map<String, String> consentAttributes;
    private ArrayList<AuthorizationResource> authorizationResources;
    private ArrayList<ConsentMappingResource> consentMappingResources;

    public DetailedConsentResource() {

    }

    public DetailedConsentResource(String orgId, String consentId, String clientId, String receipt,
                                   String consentType,
                                   String currentStatus, long expiryTime, long createdTime,
                                   long updatedTime, boolean recurringIndicator,
                                   Map<String, String> consentAttributes,
                                   ArrayList<AuthorizationResource> authorizationResources,
                                   ArrayList<ConsentMappingResource> consentMappingResources) {
        super(orgId, consentId, clientId, receipt, consentType, expiryTime, recurringIndicator, currentStatus,
                createdTime, updatedTime);
        this.consentAttributes = consentAttributes;
        this.authorizationResources = authorizationResources;
        this.consentMappingResources = consentMappingResources;

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
                ConsentMappingResource mappingClone = new ConsentMappingResource();
                mappingClone.setMappingId(mapping.getMappingId());
                mappingClone.setResource(mapping.getResource());
                mappingClone.setMappingStatus(mapping.getMappingStatus());
                copiedMappingResources.add(mappingClone); // assuming .clone() exists
            }
        }

        // Create new instance
        return new DetailedConsentResource(
                getOrgId(),
                getConsentId(),
                getClientId(),
                getReceipt(),
                getConsentType(),
                getCurrentStatus(),
                getExpiryTime(),
                getCreatedTime(),
                getUpdatedTime(),
                isRecurringIndicator(),
                copiedConsentAttributes,
                copiedAuthResources,
                copiedMappingResources
        );
    }
}
