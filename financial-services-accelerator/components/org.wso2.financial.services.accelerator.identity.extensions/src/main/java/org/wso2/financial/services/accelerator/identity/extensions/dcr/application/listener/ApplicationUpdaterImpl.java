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

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
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
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.financial.services.accelerator.common.config.TextFileReader;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.identity.extensions.dcr.util.DCRUtils;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonConstants;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Default implementation class for AbstractApplicationUpdater which should be extended for spec specific.
 * tasks
 */
public class ApplicationUpdaterImpl extends AbstractApplicationUpdater {

    private static final Log logger = LogFactory.getLog(ApplicationUpdaterImpl.class);

    public void setOauthAppProperties(OAuthConsumerAppDTO oauthApplication, Map<String, Object> spMetaData)
            throws FinancialServicesException {

    }

    public void setServiceProviderProperties(ServiceProvider serviceProvider,
                                             ServiceProviderProperty[] serviceProvideProperties)
            throws FinancialServicesException {

    }

    public void setAuthenticators(String tenantDomain, ServiceProvider serviceProvider,
                                   LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig)
            throws FinancialServicesException {

        IdentityExtensionsDataHolder identityExtensionsDataHolder = IdentityExtensionsDataHolder.getInstance();

        try {
            if (DCRUtils.getFapiCompliantPropertyFromSP(serviceProvider)) {

                Map<String, Object> configMap = identityExtensionsDataHolder.getConfigurationMap();
                List<AuthenticationStep> authSteps = new ArrayList<>();

                //Read the identity provider from open-banking.xml
                String idpName = configMap.get(FinancialServicesConstants.IDENTITY_PROVIDER_NAME) == null
                        ? null : configMap.get(FinancialServicesConstants.IDENTITY_PROVIDER_NAME).toString();
                String idpStep = configMap.get(FinancialServicesConstants.IDENTITY_PROVIDER_STEP) == null
                        ? null : configMap.get(FinancialServicesConstants.IDENTITY_PROVIDER_STEP).toString();

                IdentityProvider configuredIdentityProvider = null;

                if (StringUtils.isNotEmpty(idpName)) {
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
                            logger.debug(String.format("Authentication step 1 added: %s",
                                    idpName.replaceAll("[\r\n]", "")));
                        }
                        localAndOutboundAuthenticationConfig.setAuthenticationSteps(
                                authSteps.toArray(new AuthenticationStep[0]));
                    } else {
                        throw new FinancialServicesException("Error! An Identity Provider has not been configured.");
                    }
                } else {
                    //Step 1 - Default basic authentication
                    LocalAuthenticatorConfig localAuthenticatorConfig = new LocalAuthenticatorConfig();
                    LocalAuthenticatorConfig[] localAuthenticatorConfigs = new LocalAuthenticatorConfig[1];
                    AuthenticationStep basicAuthenticationStep = new AuthenticationStep();

                    String authenticatorDisplayName = configMap.
                            get(FinancialServicesConstants.PRIMARY_AUTHENTICATOR_DISPLAY_NAME).toString();
                    String authenticatorName = configMap.get(FinancialServicesConstants.PRIMARY_AUTHENTICATOR_NAME)
                            .toString();

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
                        logger.debug(String.format("Authentication step 1 added: %s",
                                authenticatorName.replaceAll("[\r\n]", "")));
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
                            logger.debug(String.format("Authentication step 2 added: %s",
                                    idpName.replaceAll("[\r\n]", "")));
                        }
                    }
                    localAndOutboundAuthenticationConfig.setAuthenticationSteps(authSteps.toArray(
                            new AuthenticationStep[0]));
                }
            }
        } catch (IdentityOAuthAdminException e) {
            throw new FinancialServicesException("Error while retrieving the consumer application", e);
        } catch (RequestObjectException e) {
            throw new FinancialServicesException("Error while reading the service provider metadata", e);
        } catch (IdentityApplicationManagementException e) {
            throw new FinancialServicesException("Error while reading configured Identity providers", e);
        }
    }

    public void setConditionalAuthScript(ServiceProvider serviceProvider,
                                         LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig)
            throws FinancialServicesException {

        try {
            if (DCRUtils.getFapiCompliantPropertyFromSP(serviceProvider)) {
                if (localAndOutboundAuthenticationConfig.getAuthenticationScriptConfig() == null) {
                    TextFileReader textFileReader = TextFileReader.getInstance();
                    String authScript = textFileReader.readFile
                            (IdentityCommonConstants.CONDITIONAL_COMMON_AUTH_SCRIPT_FILE_NAME);
                    if (StringUtils.isNotEmpty(authScript)) {
                        AuthenticationScriptConfig scriptConfig = new AuthenticationScriptConfig();
                        scriptConfig.setContent(authScript);
                        scriptConfig.setEnabled(true);
                        localAndOutboundAuthenticationConfig.setAuthenticationScriptConfig(scriptConfig);
                    }
                }
            }
        } catch (IdentityOAuthAdminException e) {
            throw new FinancialServicesException("Error while retrieving the consumer application", e);
        } catch (RequestObjectException e) {
            throw new FinancialServicesException("Error while reading the service provider metadata", e);
        } catch (IOException e) {
            throw new FinancialServicesException("Error occurred while reading file", e);
        }
    }

    public void doPreCreateApplication(ServiceProvider serviceProvider,
                                       LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig,
                                       String tenantDomain, String userName) throws FinancialServicesException {

    }

    public void doPostCreateApplication(ServiceProvider serviceProvider,
                                       LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig,
                                       String tenantDomain, String userName) throws FinancialServicesException {

    }

    public void doPostGetApplication(ServiceProvider serviceProvider, String applicationName,
                                     String tenantDomain) throws FinancialServicesException {

    }

    public void doPreUpdateApplication(OAuthConsumerAppDTO oauthApplication,
                                       ServiceProvider serviceProvider, LocalAndOutboundAuthenticationConfig
                                               localAndOutboundAuthenticationConfig, String tenantDomain,
                                       String userName) throws FinancialServicesException {

        try {
            boolean updateAuthenticator = false;

            if (localAndOutboundAuthenticationConfig == null) {
                localAndOutboundAuthenticationConfig = new LocalAndOutboundAuthenticationConfig();
            }

            localAndOutboundAuthenticationConfig.setUseTenantDomainInLocalSubjectIdentifier(true);
            localAndOutboundAuthenticationConfig.setUseUserstoreDomainInLocalSubjectIdentifier(true);

            ApplicationManagementService applicationManagementService = IdentityExtensionsDataHolder.getInstance()
                    .getApplicationManagementService();
            ServiceProvider existingSP = applicationManagementService
                    .getServiceProvider(serviceProvider.getApplicationID());

            // Authenticators are updated only when creating the app or when an authenticator change
            // is made from the IS carbon console

            //If authentication steps are not set then it is a create request
            if (existingSP.getLocalAndOutBoundAuthenticationConfig() == null ||
                    existingSP.getLocalAndOutBoundAuthenticationConfig().getAuthenticationSteps() == null ||
                    existingSP.getLocalAndOutBoundAuthenticationConfig().getAuthenticationSteps().length == 0) {
                updateAuthenticator = true;
            }
            // Checking whether any change have been made in the Local & Outbound Configs of the SP
            if (!new Gson().toJson(localAndOutboundAuthenticationConfig).equals(new Gson().toJson(existingSP
                    .getLocalAndOutBoundAuthenticationConfig()))) {
                updateAuthenticator = true;
            }

            if (updateAuthenticator) {
                localAndOutboundAuthenticationConfig.setAuthenticationType("flow");
                setAuthenticators(tenantDomain, serviceProvider, localAndOutboundAuthenticationConfig);
                setConditionalAuthScript(serviceProvider, localAndOutboundAuthenticationConfig);
            }
            //update service provider Properties
            setServiceProviderProperties(serviceProvider, serviceProvider.getSpProperties());
            serviceProvider.setLocalAndOutBoundAuthenticationConfig(localAndOutboundAuthenticationConfig);
            IdentityExtensionsDataHolder identityExtensionsDataHolder = IdentityExtensionsDataHolder.getInstance();
            Map<String, Object> spMetaData = IdentityCommonUtils.getSpMetaData(serviceProvider);
            //update oauth application
            setOauthAppProperties(oauthApplication, spMetaData);
            if (StringUtils.isNotBlank(CarbonContext.getThreadLocalCarbonContext().getUsername())) {
                identityExtensionsDataHolder.getOauthAdminService().updateConsumerApplication(oauthApplication);
            }
        } catch (IdentityOAuthAdminException e) {
            throw new FinancialServicesException("Error occurred while updating application ", e);
        } catch (IdentityApplicationManagementException e) {
            throw new FinancialServicesException("Error occurred while retrieving service provider ", e);
        }
    }

    public void doPreDeleteApplication(String applicationName, String tenantDomain, String userName)
            throws FinancialServicesException {

    }

    public void doPostDeleteApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws FinancialServicesException {

    }
}
