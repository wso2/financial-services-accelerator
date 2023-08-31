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

package com.wso2.openbanking.accelerator.authentication.data.publisher.internal;

import com.wso2.openbanking.accelerator.authentication.data.publisher.extension.AbstractAuthDataPublisher;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigurationService;
import com.wso2.openbanking.accelerator.common.util.OpenBankingUtils;

/**
 * Data holder for Open Banking Authentication Data Publisher.
 */
public class AuthenticationDataPublisherDataHolder {

    private static volatile AuthenticationDataPublisherDataHolder instance;
    private OpenBankingConfigurationService openBankingConfigurationService;
    private AbstractAuthDataPublisher authDataPublisher;

    public static AuthenticationDataPublisherDataHolder getInstance() {

        if (instance == null) {
            synchronized (AuthenticationDataPublisherDataHolder.class) {
                if (instance == null) {
                    instance = new AuthenticationDataPublisherDataHolder();
                }
            }
        }
        return instance;
    }

    public void setOpenBankingConfigurationService(
            OpenBankingConfigurationService openBankingConfigurationService) {

        this.openBankingConfigurationService = openBankingConfigurationService;
        AbstractAuthDataPublisher abstractAuthDataPublisher =
                (AbstractAuthDataPublisher) OpenBankingUtils.getClassInstanceFromFQN(openBankingConfigurationService
                        .getConfigurations().get("DataPublishing.AuthDataPublisher").toString());
        this.setAuthDataPublisher(abstractAuthDataPublisher);
    }

    public AbstractAuthDataPublisher getAuthDataPublisher() {
        return authDataPublisher;
    }

    public void setAuthDataPublisher(AbstractAuthDataPublisher authDataPublisher) {

        this.authDataPublisher = authDataPublisher;
    }
}
