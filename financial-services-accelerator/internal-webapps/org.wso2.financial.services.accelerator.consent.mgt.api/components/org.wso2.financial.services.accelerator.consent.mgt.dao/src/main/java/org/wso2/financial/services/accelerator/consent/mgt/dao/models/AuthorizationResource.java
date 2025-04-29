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

package org.wso2.financial.services.accelerator.consent.mgt.dao.models;

import java.util.ArrayList;

/**
 * Model for the Authorization resource.
 */
public class AuthorizationResource {

    private String authorizationID;
    private String consentID;
    private String userID;
    private String authorizationStatus;
    private String authorizationType;
    private long updatedTime;
    private ArrayList<ConsentMappingResource> consentMappingResource;

    public AuthorizationResource() {

    }

    public AuthorizationResource(String consentID, String userID, String authorizationStatus,
                                 String authorizationType, long updatedTime) {
        this.consentID = consentID;
        this.userID = userID;
        this.authorizationStatus = authorizationStatus;
        this.authorizationType = authorizationType;
        this.updatedTime = updatedTime;

    }
    public String getAuthorizationID() {

        return authorizationID;
    }

    public String getAuthorizationType() {

        return authorizationType;
    }

    public void setAuthorizationType(String authorizationType) {

        this.authorizationType = authorizationType;
    }

    public void setAuthorizationID(String authorizationID) {

        this.authorizationID = authorizationID;
    }

    public String getConsentID() {

        return consentID;
    }

    public void setConsentID(String consentID) {

        this.consentID = consentID;
    }

    public String getUserID() {

        return userID;
    }

    public void setUserID(String userID) {

        this.userID = userID;
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

    public ArrayList<ConsentMappingResource> getConsentMappingResource() {
        return consentMappingResource;
    }

    public void setConsentMappingResource(
            ArrayList<ConsentMappingResource> consentMappingResource) {
        this.consentMappingResource = consentMappingResource;
    }

}
