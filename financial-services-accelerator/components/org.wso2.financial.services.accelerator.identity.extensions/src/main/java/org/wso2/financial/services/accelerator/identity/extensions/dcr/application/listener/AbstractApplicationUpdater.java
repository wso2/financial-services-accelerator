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

package org.wso2.financial.services.accelerator.identity.extensions.dcr.application.listener;

import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;

import java.util.Map;

/**
 * Abstract class for extending methods to be invoked by the application listener.
 */
public abstract class AbstractApplicationUpdater {

    public abstract void setOauthAppProperties(OAuthConsumerAppDTO oauthApplication, Map<String, Object> spMetaData)
            throws FinancialServicesException;

    public abstract void setServiceProviderProperties(ServiceProvider serviceProvider,
                                           ServiceProviderProperty[] serviceProvideProperties)
            throws FinancialServicesException;

    public abstract void setAuthenticators(String tenantDomain, ServiceProvider serviceProvider,
                                           LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig)
            throws FinancialServicesException;

    public abstract void setConditionalAuthScript (ServiceProvider serviceProvider,
                                           LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig)
            throws FinancialServicesException;

    public abstract void doPreCreateApplication(ServiceProvider serviceProvider,
                                            LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig,
                                            String tenantDomain, String userName) throws FinancialServicesException;

    public abstract void doPostCreateApplication(ServiceProvider serviceProvider,
                                             LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig,
                                            String tenantDomain, String userName) throws FinancialServicesException;

    public abstract void doPostGetApplication(ServiceProvider serviceProvider, String applicationName,
                                            String tenantDomain) throws FinancialServicesException;

    public abstract void doPreUpdateApplication(OAuthConsumerAppDTO oauthApplication, ServiceProvider serviceProvider,
                                            LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig,
                                            String tenantDomain, String userName)
            throws FinancialServicesException;

    public abstract void doPreDeleteApplication(String applicationName, String tenantDomain, String userName)
            throws FinancialServicesException;

    public abstract void doPostDeleteApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws FinancialServicesException;

}
