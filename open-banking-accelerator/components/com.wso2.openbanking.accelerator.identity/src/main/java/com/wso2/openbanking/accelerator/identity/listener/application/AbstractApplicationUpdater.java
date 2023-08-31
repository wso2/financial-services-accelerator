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
package com.wso2.openbanking.accelerator.identity.listener.application;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;

import java.util.Map;

/**
 * Abstract class for extending methods to be invoked by the application listener.
 */
public abstract class AbstractApplicationUpdater {

    public abstract void setOauthAppProperties(boolean isRegulatoryApp, OAuthConsumerAppDTO oauthApplication,
                                               Map<String, Object> spMetaData) throws OpenBankingException;

    public abstract void setServiceProviderProperties(boolean isRegulatoryApp, ServiceProvider serviceProvider,
                                                      ServiceProviderProperty[] serviceProvideProperties)
            throws OpenBankingException;

    public abstract void setAuthenticators(boolean isRegulatoryApp, String tenantDomain,
                                           ServiceProvider serviceProvider,
                                           LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig)
            throws OpenBankingException;

    public abstract void setConditionalAuthScript (boolean isRegulatoryApp, ServiceProvider serviceProvider,
             LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig)
            throws OpenBankingException;

    public abstract void publishData(Map<String, Object> spMetaData, OAuthConsumerAppDTO oAuthConsumerAppDTO)
            throws OpenBankingException;

    public abstract void doPreCreateApplication(boolean isRegulatoryApp, ServiceProvider serviceProvider,
                                                 LocalAndOutboundAuthenticationConfig
                                                         localAndOutboundAuthenticationConfig,
                                                 String tenantDomain, String userName) throws OpenBankingException;

    public abstract void doPostGetApplication(ServiceProvider serviceProvider, String applicationName,
                                              String tenantDomain) throws OpenBankingException;

    public abstract void doPreUpdateApplication(boolean isRegulatoryApp, OAuthConsumerAppDTO oauthApplication,
                                                ServiceProvider serviceProvider, LocalAndOutboundAuthenticationConfig
                                                         localAndOutboundAuthenticationConfig, String tenantDomain,
                                                String userName)
            throws OpenBankingException;

    public abstract void doPreDeleteApplication(String applicationName, String tenantDomain, String userName)
            throws OpenBankingException;

    public abstract void doPostDeleteApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws OpenBankingException;

}
