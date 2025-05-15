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

/**
 * Model for the Authorization resource.
 */
public class AuthorizationResource {

    private String authorizationId;
    private String consentId;
    private String userId;
    private String authorizationStatus;
    private String authorizationType;
    private long updatedTime;
    private Object resource;

    public AuthorizationResource() {

    }

    public AuthorizationResource(String consentId, String userId, String authorizationStatus,
                                 String authorizationType, Object resource, long updatedTime) {
        this.consentId = consentId;
        this.userId = userId;
        this.authorizationStatus = authorizationStatus;
        this.authorizationType = authorizationType;
        this.updatedTime = updatedTime;
        this.resource = resource;

    }

    public String getAuthorizationId() {

        return authorizationId;
    }

    public String getAuthorizationType() {

        return authorizationType;
    }

    public void setAuthorizationType(String authorizationType) {

        this.authorizationType = authorizationType;
    }

    public void setAuthorizationId(String authorizationId) {

        this.authorizationId = authorizationId;
    }

    public String getConsentId() {

        return consentId;
    }

    public void setConsentId(String consentId) {

        this.consentId = consentId;
    }

    public String getUserId() {

        return userId;
    }

    public void setUserId(String userId) {

        this.userId = userId;
    }

    public String getAuthorizationStatus() {

        return authorizationStatus;
    }

    public void setAuthorizationStatus(String authorizationStatus) {

        this.authorizationStatus = authorizationStatus;
    }

    public long getUpdatedTime() {

        return updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {

        this.updatedTime = updatedTime;
    }

    public Object getResource() {

        return resource;
    }

    public void setResource(Object resource) {

        this.resource = resource;
    }

}
