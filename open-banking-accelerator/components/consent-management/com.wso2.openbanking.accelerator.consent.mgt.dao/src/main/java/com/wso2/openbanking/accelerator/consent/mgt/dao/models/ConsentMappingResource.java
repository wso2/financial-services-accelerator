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
 * Model for consent mapping resource.
 */
public class ConsentMappingResource {

    private String mappingID;
    private String authorizationID;
    private String accountID;
    private String permission;
    private String mappingStatus;

    public ConsentMappingResource() {

    }

    @Generated(message = "Excluding constructor because setter methods are explicitly called")
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
}
