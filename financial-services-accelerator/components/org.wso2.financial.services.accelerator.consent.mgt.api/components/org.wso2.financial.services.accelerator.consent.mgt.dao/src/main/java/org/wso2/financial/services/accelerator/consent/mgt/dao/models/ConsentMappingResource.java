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

import net.minidev.json.JSONObject;

/**
 * Model for consent mapping resource.
 */
public class ConsentMappingResource {

    private String mappingID;
    private String authorizationID;
    private String accountID;
    private String permission;
    private String mappingStatus;
    private JSONObject resource;

    public ConsentMappingResource() {

    }

    public ConsentMappingResource(String authorizationID, JSONObject resource, String mappingStatus) {
        this.authorizationID = authorizationID;
        this.resource = resource;
        this.mappingStatus = mappingStatus;
    }

    public ConsentMappingResource(String authorizationID, String accountID, String permission,
                                  String mappingStatus) {
        this.authorizationID = authorizationID;
        this.accountID = accountID;
        this.permission = permission;
        this.mappingStatus = mappingStatus;
    }

    public String getMappingID() {

        return mappingID;
    }

    public void setMappingID(String mappingID) {

        this.mappingID = mappingID;
    }

    public String getAuthorizationID() {

        return authorizationID;
    }

    public void setAuthorizationID(String authorizationID) {

        this.authorizationID = authorizationID;
    }

    public String getAccountID() {

        return accountID;
    }

    public void setAccountID(String accountID) {

        this.accountID = accountID;
    }

    public String getPermission() {

        return permission;
    }

    public void setPermission(String permission) {

        this.permission = permission;
    }

    public String getMappingStatus() {

        return mappingStatus;
    }

    public void setMappingStatus(String mappingStatus) {

        this.mappingStatus = mappingStatus;
    }

    public JSONObject getResource() {
        return resource;
    }

    public void setResource(JSONObject resource) {
        this.resource = resource;
    }
}
