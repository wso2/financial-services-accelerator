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

package org.wso2.financial.services.accelerator.consent.mgt.api.dao.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model for Detailed Consent Resource.
 */
public class DetailedConsentResource extends ConsentResource {

    private Map<String, Object> consentAttributes;
    private List<AuthorizationResource> authorizationResources;

    public DetailedConsentResource() {

    }

    public DetailedConsentResource(String orgId, String consentId, String clientId, String receipt,
                                   String consentType,
                                   String currentStatus, long expiryTime, long createdTime,
                                   long updatedTime, boolean recurringIndicator,
                                   Map<String, Object> consentAttributes,
                                   List<AuthorizationResource> authorizationResources) {
        super(orgId, consentId, clientId, receipt, consentType, expiryTime, recurringIndicator, currentStatus,
                createdTime, updatedTime);
        this.consentAttributes = consentAttributes;
        this.authorizationResources = authorizationResources;

    }

    public Map<String, Object> getConsentAttributes() {

        return consentAttributes;
    }

    public void setConsentAttributes(Map<String, Object> consentAttributes) {

        this.consentAttributes = consentAttributes;
    }

    public List<AuthorizationResource> getAuthorizationResources() {

        return authorizationResources;
    }

    public void setAuthorizationResources(List<AuthorizationResource> authorizationResources) {

        this.authorizationResources = authorizationResources;
    }

    public DetailedConsentResource clone() {
        Map<String, Object> copiedConsentAttributes = new HashMap<>();
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
                copiedAuthResources
        );
    }
}
