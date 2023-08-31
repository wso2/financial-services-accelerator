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
package com.wso2.openbanking.accelerator.account.metadata.service.internal;

import org.wso2.carbon.user.core.service.RealmService;

/**
 * AccountMetadata Data Holder.
 */
public class AccountMetadataDataHolder {

    private static AccountMetadataDataHolder instance = new AccountMetadataDataHolder();

    private RealmService realmService;

    private AccountMetadataDataHolder() {

    }

    public static AccountMetadataDataHolder getInstance() {

        return instance;
    }

    public RealmService getRealmService() {

        if (realmService == null) {
            throw new RuntimeException("Realm Service is not available. Component did not start correctly.");
        }
        return realmService;
    }

    void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }
}
