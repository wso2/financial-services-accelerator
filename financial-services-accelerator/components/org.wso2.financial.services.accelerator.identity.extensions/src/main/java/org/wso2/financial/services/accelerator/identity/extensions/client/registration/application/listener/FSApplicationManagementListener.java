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

package org.wso2.financial.services.accelerator.identity.extensions.client.registration.application.listener;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceManager;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.AssociatedRolesConfig;
import org.wso2.carbon.identity.application.common.model.AuthorizedAPI;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.listener.AbstractApplicationMgtListener;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.identity.extensions.client.registration.dcr.util.DCRUtils;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonConstants;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Application listener.
 */
public class FSApplicationManagementListener extends AbstractApplicationMgtListener {

    private static final Log log = LogFactory.getLog(FSApplicationManagementListener.class);
    private final IdentityExtensionsDataHolder identityDataHolder = IdentityExtensionsDataHolder.getInstance();
    private static final Gson gson = new Gson();

    @Override
    public int getDefaultOrderId() {

        return 1000;
    }

    @Override
    public boolean doPreCreateApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        try {
            // Set the allowed audience to ORGANIZATION to map the roles created while
            // publishing the APIs to the application.
            AssociatedRolesConfig rolesConfig = new AssociatedRolesConfig();
            rolesConfig.setAllowedAudience(IdentityCommonConstants.ORGANIZATION);
            serviceProvider.setAssociatedRolesConfig(rolesConfig);

            LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig = serviceProvider
                    .getLocalAndOutBoundAuthenticationConfig();

            identityDataHolder.getAbstractApplicationUpdater()
                    .doPreCreateApplication(serviceProvider, localAndOutboundAuthenticationConfig, tenantDomain,
                            userName);

        } catch (FinancialServicesException e) {
            log.error("Error occurred while Creating application.", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean doPostCreateApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        try {
            ServiceProviderProperty[] spProperties = serviceProvider.getSpProperties();
            Optional<ServiceProviderProperty> scopeProperty = Arrays.stream(spProperties)
                    .filter(spProperty -> FinancialServicesConstants.SCOPE.equals(spProperty.getName()))
                    .findFirst();

            // In IS 7.0 and upwards, scopes should be bind to the application via API Resources. When IS as a
            // Key Manager is configured it will automatically create API resource binding the scopes in IS when
            // publishing APIs in API Manager.

            // Retrieve the API resource created while publishing the APIs in API Manager
            APIResourceManager resourceManager = IdentityExtensionsDataHolder.getInstance().getApiResourceManager();
            APIResource apiResource = resourceManager
                    .getAPIResourceByIdentifier(IdentityCommonConstants.USER_DEFINED_RESOURCE, tenantDomain);
            if (apiResource != null) {
                List<Scope> scopes = apiResource.getScopes();
                if (scopeProperty.isPresent()) {
                    String scopesStringFromRequest = scopeProperty.get().getValue();
                    if (scopesStringFromRequest != null) {
                        List<String> scopesListFromRequest = List.of(scopesStringFromRequest.split(" "));
                        scopes.removeIf(scope -> !scopesListFromRequest.contains(scope.getName()));
                    }
                }
                // Created authorized API object to store the API resource details
                AuthorizedAPI authorizedAPI = new AuthorizedAPI();
                authorizedAPI.setAPIId(apiResource.getId());
                authorizedAPI.setScopes(scopes);
                authorizedAPI.setPolicyId(IdentityCommonConstants.RBAC_POLICY);

                // Add the authorized API to the application
                IdentityExtensionsDataHolder.getInstance().getAuthorizedAPIManagementService()
                        .addAuthorizedAPI(serviceProvider.getApplicationResourceId(), authorizedAPI, tenantDomain);
            }

            identityDataHolder.getAbstractApplicationUpdater().doPostCreateApplication(serviceProvider,
                    serviceProvider.getLocalAndOutBoundAuthenticationConfig(), tenantDomain, userName);

        } catch (APIResourceMgtException e) {
            log.error("Error occurred while retrieving API resource.", e);
            return false;
        } catch (IdentityApplicationManagementException e) {
            log.error("Error occurred while adding authorized API.", e);
        } catch (FinancialServicesException e) {
            log.error("Error occurred while creating application.", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean doPreUpdateApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        try {
            OAuthConsumerAppDTO oAuthConsumerAppDTO = DCRUtils
                    .getOAuthConsumerAppDTO(serviceProvider.getApplicationName());
            LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig = serviceProvider
                    .getLocalAndOutBoundAuthenticationConfig();

            identityDataHolder.getAbstractApplicationUpdater()
                    .doPreUpdateApplication(oAuthConsumerAppDTO, serviceProvider,
                            localAndOutboundAuthenticationConfig, tenantDomain, userName);

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
            if (!gson.toJson(localAndOutboundAuthenticationConfig).equals(gson.toJson(existingSP
                    .getLocalAndOutBoundAuthenticationConfig()))) {
                updateAuthenticator = true;
            }

            if (updateAuthenticator) {
                localAndOutboundAuthenticationConfig.setAuthenticationType("flow");
                identityDataHolder.getAbstractApplicationUpdater().setAuthenticators(tenantDomain,
                        serviceProvider, localAndOutboundAuthenticationConfig);
                identityDataHolder.getAbstractApplicationUpdater().setConditionalAuthScript(serviceProvider,
                        localAndOutboundAuthenticationConfig);
            }
            //update service provider Properties
            identityDataHolder.getAbstractApplicationUpdater().setServiceProviderProperties(serviceProvider,
                    serviceProvider.getSpProperties());
            serviceProvider.setLocalAndOutBoundAuthenticationConfig(localAndOutboundAuthenticationConfig);
            IdentityExtensionsDataHolder identityExtensionsDataHolder = IdentityExtensionsDataHolder.getInstance();
            Map<String, Object> spMetaData = IdentityCommonUtils.getSpMetaData(serviceProvider);
            //update oauth application
            identityDataHolder.getAbstractApplicationUpdater().setOauthAppProperties(oAuthConsumerAppDTO, spMetaData);
            if (StringUtils.isNotBlank(CarbonContext.getThreadLocalCarbonContext().getUsername())) {
                identityExtensionsDataHolder.getOauthAdminService().updateConsumerApplication(oAuthConsumerAppDTO);
            }

        } catch (FinancialServicesException e) {
            log.error("Error occurred while updating application.", e);
            return false;
        } catch (IdentityOAuthAdminException e) {
            //returning true here because this error code is returned when there is no oauth app created
            //when running integration tests of IS, test cases fail since apps are update before key generation in tests
            if ("OAUTH-60002".equals(e.getErrorCode())) {
                return true;
            }
            log.error("Error while retrieving oauth application", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean doPostGetServiceProvider(ServiceProvider serviceProvider, String applicationName,
                                            String tenantDomain) throws IdentityApplicationManagementException {

        try {
            identityDataHolder.getAbstractApplicationUpdater()
                    .doPostGetApplication(serviceProvider, applicationName, tenantDomain);
        } catch (FinancialServicesException e) {
            log.error("Error occurred while retrieving application.", e);
            return false;
        }
        return true;

    }

    @Override
    public boolean doPreDeleteApplication(String applicationName, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        try {
            identityDataHolder.getAbstractApplicationUpdater()
                    .doPreDeleteApplication(applicationName, tenantDomain, userName);
        } catch (FinancialServicesException e) {
            log.error("Error occurred while deleting application.", e);
            return false;
        }
        return true;

    }

    @Override
    public boolean doPostDeleteApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        try {
            identityDataHolder.getAbstractApplicationUpdater()
                    .doPostDeleteApplication(serviceProvider, tenantDomain, userName);
        } catch (FinancialServicesException e) {
            log.error("Error occurred while deleting application.", e);
            return false;
        }
        return true;

    }
}
