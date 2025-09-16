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

import com.google.gson.Gson;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.config.TextFileReader;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.common.model.script.AuthenticationScriptConfig;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Default implementation class for AbstractApplicationUpdater which should be extended for spec specific.
 * tasks
 */
public class ApplicationUpdaterImpl extends AbstractApplicationUpdater {

    private static final Log logger = LogFactory.getLog(ApplicationUpdaterImpl.class);

    public void setOauthAppProperties(boolean isRegulatoryApp, OAuthConsumerAppDTO oauthApplication,
                                      Map<String, Object> spMetaData) throws OpenBankingException {

    }

    public void setServiceProviderProperties(boolean isRegulatoryApp, ServiceProvider serviceProvider,
                                             ServiceProviderProperty[] serviceProvideProperties)
            throws OpenBankingException {

    }

    public void setAuthenticators (boolean isRegulatoryApp, String tenantDomain, ServiceProvider serviceProvider,
                                   LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig)
            throws OpenBankingException {

        IdentityExtensionsDataHolder identityExtensionsDataHolder = IdentityExtensionsDataHolder.getInstance();
        if (isRegulatoryApp) {

            Map<String, Object> configMap = identityExtensionsDataHolder.getConfigurationMap();
            List<AuthenticationStep> authSteps = new ArrayList<>();

            //Read the identity provider from open-banking.xml
            String idpName = configMap.get(IdentityCommonConstants.IDENTITY_PROVIDER_NAME) == null
                    ? null : configMap.get(IdentityCommonConstants.IDENTITY_PROVIDER_NAME).toString();
            String idpStep = configMap.get(IdentityCommonConstants.IDENTITY_PROVIDER_STEP) == null
                    ? null : configMap.get(IdentityCommonConstants.IDENTITY_PROVIDER_STEP).toString();

            IdentityProvider configuredIdentityProvider = null;

            if (StringUtils.isNotEmpty(idpName)) {
                try {
                    IdentityProvider[] federatedIdPs = identityExtensionsDataHolder
                            .getApplicationManagementService().getAllIdentityProviders(tenantDomain);
                    if (federatedIdPs != null && federatedIdPs.length > 0) {
                        for (IdentityProvider identityProvider : federatedIdPs) {
                            if (idpName.equals(identityProvider.getIdentityProviderName())) {
                                configuredIdentityProvider = identityProvider;
                                break;
                            }
                        }
                    }
                } catch (IdentityApplicationManagementException e) {
                    throw new OpenBankingException("Error while reading configured Identity providers", e);
                }

            }

            if (StringUtils.isNotEmpty(idpStep) && idpStep.equals("1")) {
                //Step 1 - Federated Authentication
                if (configuredIdentityProvider != null) {
                    IdentityProvider[] identityProviders = new IdentityProvider[1];
                    identityProviders[0] = configuredIdentityProvider;

                    AuthenticationStep federatedAuthStep = new AuthenticationStep();
                    federatedAuthStep.setStepOrder(1);
                    federatedAuthStep.setFederatedIdentityProviders(identityProviders);
                    //set step 1
                    authSteps.add(federatedAuthStep);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Authentication step 1 added: " + idpName);
                    }
                    localAndOutboundAuthenticationConfig.setAuthenticationSteps(
                            authSteps.toArray(new AuthenticationStep[0]));
                } else {
                    throw new OpenBankingException("Error! An Identity Provider has not been configured.");
                }
            } else {
                //Step 1 - Default basic authentication
                LocalAuthenticatorConfig localAuthenticatorConfig = new LocalAuthenticatorConfig();
                LocalAuthenticatorConfig[] localAuthenticatorConfigs = new LocalAuthenticatorConfig[1];
                AuthenticationStep basicAuthenticationStep = new AuthenticationStep();

                String authenticatorDisplayName = configMap.
                        get(IdentityCommonConstants.PRIMARY_AUTHENTICATOR_DISPLAYNAME).toString();
                String authenticatorName = configMap.get(IdentityCommonConstants.PRIMARY_AUTHENTICATOR_NAME).toString();

                localAuthenticatorConfig.setDisplayName(authenticatorDisplayName);
                localAuthenticatorConfig.setEnabled(true);
                localAuthenticatorConfig.setName(authenticatorName);
                localAuthenticatorConfigs[0] = localAuthenticatorConfig;

                basicAuthenticationStep.setStepOrder(1);
                basicAuthenticationStep.setLocalAuthenticatorConfigs(localAuthenticatorConfigs);
                basicAuthenticationStep.setAttributeStep(true);
                basicAuthenticationStep.setSubjectStep(true);
                //set step 1
                authSteps.add(basicAuthenticationStep);

                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Authentication step 1 added: %s", authenticatorName));
                }

                //Step 2 - federated authentication
                if (configuredIdentityProvider != null) {
                    IdentityProvider[] identityProviders = new IdentityProvider[1];
                    identityProviders[0] = configuredIdentityProvider;

                    AuthenticationStep federatedAuthStep = new AuthenticationStep();
                    federatedAuthStep.setStepOrder(2);
                    federatedAuthStep.setFederatedIdentityProviders(identityProviders);
                    //set step 2
                    authSteps.add(federatedAuthStep);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Authentication step 2 added: " + idpName);
                    }
                }
                localAndOutboundAuthenticationConfig.setAuthenticationSteps(authSteps.toArray(
                        new AuthenticationStep[0]));
            }
        }
    }

    public void setConditionalAuthScript(boolean isRegulatoryApp, ServiceProvider serviceProvider,
                                         LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig)
            throws OpenBankingException {

        if (isRegulatoryApp) {
            if (localAndOutboundAuthenticationConfig.getAuthenticationScriptConfig() == null) {
                TextFileReader textFileReader = TextFileReader.getInstance();
                try {
                    String authScript = textFileReader.readFile
                            (IdentityCommonConstants.CONDITIONAL_COMMON_AUTH_SCRIPT_FILE_NAME);
                    if (StringUtils.isNotEmpty(authScript)) {
                        AuthenticationScriptConfig scriptConfig = new AuthenticationScriptConfig();
                        scriptConfig.setContent(authScript);
                        scriptConfig.setEnabled(true);
                        localAndOutboundAuthenticationConfig.setAuthenticationScriptConfig(scriptConfig);
                    }
                } catch (IOException e) {
                    throw new OpenBankingException("Error occurred while reading file", e);
                }

            }
        }
    }

    public void publishData(Map<String, Object> spMetaData, OAuthConsumerAppDTO oAuthConsumerAppDTO)
            throws OpenBankingException {

    }

    public void doPreCreateApplication(boolean isRegulatoryApp, ServiceProvider serviceProvider,
                                       LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig,
                                       String tenantDomain, String userName) throws OpenBankingException {

    }

    public void doPostGetApplication(ServiceProvider serviceProvider, String applicationName,
                                     String tenantDomain) throws OpenBankingException {

    }

    public void doPreUpdateApplication(boolean isRegulatoryApp, OAuthConsumerAppDTO oauthApplication,
                                       ServiceProvider serviceProvider, LocalAndOutboundAuthenticationConfig
                                               localAndOutboundAuthenticationConfig, String tenantDomain,
                                       String userName) throws OpenBankingException {

        try {
            boolean updateAuthenticator = false;
            List<ServiceProviderProperty> spProperties = new ArrayList<>(Arrays.asList
                    (serviceProvider.getSpProperties()));
            ServiceProviderProperty appCreateRequest = spProperties.stream()
                    .filter(serviceProviderProperty -> serviceProviderProperty.getName()
                            .equalsIgnoreCase("AppCreateRequest")).findAny().orElse(null);
            // Authenticators are updated only when creating the app or when an authenticator change
            // is made from the IS carbon console
            if (appCreateRequest != null) {
                updateAuthenticator = Boolean.parseBoolean(appCreateRequest.getValue());
                // Removing the added additional SP property to identify create and update requests
                spProperties.remove(appCreateRequest);
                serviceProvider.setSpProperties(spProperties.toArray(new ServiceProviderProperty[0]));
            } else if (OpenBankingConfigParser.getInstance().isSetAuthenticatorsOnAppUpdateEnabled()) {
                // Executes only if the enable_setting_authenticators_on_app_update set to true.
                ApplicationManagementService applicationManagementService = IdentityExtensionsDataHolder.getInstance()
                        .getApplicationManagementService();
                ServiceProvider existingSP = applicationManagementService
                        .getServiceProvider(serviceProvider.getApplicationID());
                // Checking whether any change have been made in the Local & Outbound Configs of the SP
                if (!new Gson().toJson(localAndOutboundAuthenticationConfig).equals(new Gson().toJson(existingSP
                        .getLocalAndOutBoundAuthenticationConfig()))) {
                    updateAuthenticator = true;
                }
            }

            if (localAndOutboundAuthenticationConfig == null) {
                localAndOutboundAuthenticationConfig = new LocalAndOutboundAuthenticationConfig();
            }
            localAndOutboundAuthenticationConfig.setUseTenantDomainInLocalSubjectIdentifier(true);
            localAndOutboundAuthenticationConfig.setUseUserstoreDomainInLocalSubjectIdentifier(true);

            if (updateAuthenticator) {
                localAndOutboundAuthenticationConfig.setAuthenticationType("flow");
                setAuthenticators(isRegulatoryApp, tenantDomain, serviceProvider, localAndOutboundAuthenticationConfig);
                setConditionalAuthScript(isRegulatoryApp, serviceProvider, localAndOutboundAuthenticationConfig);
            }
            //update service provider Properties
            setServiceProviderProperties(isRegulatoryApp, serviceProvider, serviceProvider.getSpProperties());
            serviceProvider.setLocalAndOutBoundAuthenticationConfig(localAndOutboundAuthenticationConfig);
            IdentityExtensionsDataHolder identityExtensionsDataHolder = IdentityExtensionsDataHolder.getInstance();
            Map<String, Object> spMetaData = IdentityCommonUtil.getSpMetaData(serviceProvider);
            //update oauth application
            setOauthAppProperties(isRegulatoryApp, oauthApplication, spMetaData);
            if (StringUtils.isNotBlank(CarbonContext.getThreadLocalCarbonContext().getUsername())) {
                identityExtensionsDataHolder.getOauthAdminService().updateConsumerApplication(oauthApplication);
            }
            publishData(spMetaData, oauthApplication);
        } catch (IdentityOAuthAdminException e) {
            throw new OpenBankingException("Error occurred while updating application ", e);
        } catch (IdentityApplicationManagementException e) {
            throw new OpenBankingException("Error occurred while retrieving service provider ", e);
        }
    }

    public void doPreDeleteApplication(String applicationName, String tenantDomain, String userName)
            throws OpenBankingException {

    }

    public void doPostDeleteApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws OpenBankingException {

    }
}
