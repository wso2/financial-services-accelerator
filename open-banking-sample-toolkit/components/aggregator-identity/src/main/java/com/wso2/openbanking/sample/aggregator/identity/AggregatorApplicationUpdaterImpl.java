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

package com.wso2.openbanking.sample.aggregator.identity;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.identity.listener.application.ApplicationUpdaterImpl;
import com.wso2.openbanking.sample.aggregator.identity.util.AggregatorConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Application Updater for Aggregator Sample.
 */
public class AggregatorApplicationUpdaterImpl extends ApplicationUpdaterImpl {

    private static final Log log = LogFactory.getLog(AggregatorApplicationUpdaterImpl.class);

    @Override
    public void setAuthenticators(boolean isRegulatoryApp, String tenantDomain, ServiceProvider serviceProvider,
                                  LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig)
            throws OpenBankingException {
        List<AuthenticationStep> authSteps = new ArrayList<AuthenticationStep>();
        ApplicationManagementService applicationManagementService = ApplicationManagementServiceImpl.getInstance();
        if (StringUtils.isNotEmpty(AggregatorConstants.BANK_IDP_NAME)) {
            IdentityProvider configuredIdentityProvider = null;
            try {
                IdentityProvider[] federatedIdPs = applicationManagementService.getAllIdentityProviders(tenantDomain);
                if (federatedIdPs != null && federatedIdPs.length > 0) {
                    for (IdentityProvider identityProvider : federatedIdPs) {
                        if (AggregatorConstants.BANK_IDP_NAME.equals(identityProvider.getIdentityProviderName())) {
                            configuredIdentityProvider = identityProvider;
                            break;
                        }
                    }
                }
                //Step 1 - federated authentication
                if (configuredIdentityProvider != null) {
                    IdentityProvider[] identityProviders = new IdentityProvider[1];
                    identityProviders[0] = configuredIdentityProvider;

                    AuthenticationStep federatedAuthStep = new AuthenticationStep();
                    federatedAuthStep.setStepOrder(1);
                    federatedAuthStep.setFederatedIdentityProviders(identityProviders);
                    //set step 1
                    authSteps.add(federatedAuthStep);
                }

                if (log.isDebugEnabled()) {
                    log.debug("Authentication step 1 added: " + AggregatorConstants.BANK_IDP_NAME);
                }
            } catch (IdentityApplicationManagementException e) {
                throw new OpenBankingException("Error while reading configured Identity providers", e);
            }

        }
        localAndOutboundAuthenticationConfig.setAuthenticationSteps(authSteps.toArray(new AuthenticationStep[0]));
    }
}
