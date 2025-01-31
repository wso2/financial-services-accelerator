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
package com.wso2.openbanking.accelerator.keymanager;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.common.util.ServiceProviderUtils;
import com.wso2.openbanking.accelerator.keymanager.internal.KeyManagerDataHolder;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.ConfigurationDto;
import org.wso2.carbon.apimgt.api.model.KeyManagerConnectorConfiguration;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.impl.AMDefaultKeyManagerImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementServiceImpl;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.OAuthAdminService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OB key manager client impl class.
 */
public class OBKeyManagerImpl extends AMDefaultKeyManagerImpl implements OBKeyManagerExtensionInterface {

    private static final Log log = LogFactory.getLog(OBKeyManagerImpl.class);

    public static final String OAUTH2 = "oauth2";

    @Override
    public AccessTokenInfo getNewApplicationAccessToken(AccessTokenRequest tokenRequest) throws APIManagementException {

        try {
            ApplicationManagementServiceImpl applicationManagementService = getApplicationMgmtServiceImpl();
            ServiceProvider serviceProvider = applicationManagementService.getServiceProviderByClientId(
                    tokenRequest.getClientId(), IdentityApplicationConstants.OAuth2.NAME, tenantDomain);
            if (serviceProvider != null) {
                ServiceProviderProperty regulatoryProperty = Arrays.stream(serviceProvider.getSpProperties())
                        .filter(serviceProviderProperty -> serviceProviderProperty.getName()
                                .equalsIgnoreCase(OpenBankingConstants.REGULATORY)).findAny().orElse(null);
                if (regulatoryProperty != null && "true".equalsIgnoreCase(regulatoryProperty.getValue())) {
                    return null;
                }
            }
        } catch (IdentityApplicationManagementException e) {
            log.error("Error while generating keys. ", e);
        }
        return super.getNewApplicationAccessToken(tokenRequest);
    }

    @Override
    public String getType() {

        return OBKeyManagerConstants.CUSTOM_KEYMANAGER_TYPE;
    }

    /**
     * Validate OAuth Application Properties.
     *
     * @param oAuthApplicationInfo OAuthApplication Information
     * @throws APIManagementException when failed to validate the OAuth application properties
     */
    @Override
    protected void validateOAuthAppCreationProperties(OAuthApplicationInfo oAuthApplicationInfo)
            throws APIManagementException {

        String type = getType();
        List<String> missedRequiredValues = new ArrayList<>();
        Map<String, ConfigurationDto> obAdditionalProperties = new HashMap<>();

        KeyManagerConnectorConfiguration oBKeyManagerConnectorConfiguration = KeyManagerDataHolder.getInstance()
                .getKeyManagerConnectorConfiguration(type);
        // Obtain additional key manager configurations defined in config
        Map<String, Map<String, String>> keyManagerAdditionalProperties = OpenBankingConfigParser.getInstance()
                .getKeyManagerAdditionalProperties();

        if (oBKeyManagerConnectorConfiguration != null) {
            List<ConfigurationDto> applicationConfigurationDtoList = oBKeyManagerConnectorConfiguration
                    .getApplicationConfigurations();
            Object additionalProperties = oAuthApplicationInfo.getParameter(APIConstants.JSON_ADDITIONAL_PROPERTIES);
            if (additionalProperties != null) {
                Object additionalPropertiesJson;
                try {
                    additionalPropertiesJson = new JSONParser(JSONParser.MODE_PERMISSIVE)
                            .parse(additionalProperties.toString());
                    if (!(additionalPropertiesJson instanceof JSONObject)) {
                        String errMsg = "Additional properties is not a valid json object";
                        log.error(errMsg);
                        throw new APIManagementException(errMsg, ExceptionCodes
                                .from(ExceptionCodes.INVALID_APPLICATION_ADDITIONAL_PROPERTIES,
                                        errMsg));
                    }
                } catch (ParseException e) {
                    String errMsg = "Additional properties is not a valid JSON string";
                    throw new APIManagementException(errMsg, e, ExceptionCodes
                            .from(ExceptionCodes.INVALID_APPLICATION_ADDITIONAL_PROPERTIES,
                                    errMsg));
                }

                for (ConfigurationDto configurationDto : applicationConfigurationDtoList) {
                    String key = configurationDto.getName();
                    String values = ((JSONObject) additionalPropertiesJson).getAsString(key);

                    if (values == null) {
                        // AbstractKeyManager Validations
                        // Check if mandatory parameters are missing
                        if (configurationDto.isRequired()) {
                            missedRequiredValues.add(configurationDto.getName());
                        }
                    } else {
                        // OBKeyManager Validations
                        if (keyManagerAdditionalProperties.containsKey(key)) {
                            configurationDto.setValues(Arrays.asList(values));
                            obAdditionalProperties.put(key, configurationDto);
                        } else {
                            // AMDefaultKeyManager validations
                            // Check for invalid time periods
                            if (StringUtils.isNotBlank(values) && !StringUtils
                                    .equals(values, APIConstants.KeyManager.NOT_APPLICABLE_VALUE)) {
                                try {
                                    Long longValue = Long.parseLong(values);
                                    if (longValue < 0) {
                                        String errMsg = "Application configuration values cannot have negative values.";
                                        throw new APIManagementException(errMsg, ExceptionCodes
                                                .from(ExceptionCodes.INVALID_APPLICATION_ADDITIONAL_PROPERTIES,
                                                        errMsg));
                                    }
                                } catch (NumberFormatException e) {
                                    String errMsg = "Application configuration values cannot have string values.";
                                    throw new APIManagementException(errMsg, e, ExceptionCodes
                                            .from(ExceptionCodes.INVALID_APPLICATION_ADDITIONAL_PROPERTIES, errMsg));
                                }
                            }
                        }
                    }
                }
                if (!missedRequiredValues.isEmpty()) {
                    throw new APIManagementException(
                            "Missing required properties to create/update oauth " + "application",
                            ExceptionCodes.KEY_MANAGER_MISSING_REQUIRED_PROPERTIES_IN_APPLICATION);
                }
                // Call external method to validate additional properties
                if (obAdditionalProperties.size() != 0) {
                    validateAdditionalProperties(obAdditionalProperties);
                }
            }
        } else {
            throw new APIManagementException("Invalid Key Manager Type " + type, ExceptionCodes.KEY_MANAGER_NOT_FOUND);
        }
    }

    /**
     * Overriding the default create application method with Open Banking requirements.
     *
     * @param oauthAppRequest OAuthApplicationRequest object
     * @return OAuthApplicationInfo object
     * @throws APIManagementException when failed to create the application properly in Key Manager
     */
    @Override
    @Generated(message = "Excluding from code coverage since it is covered from other method")
    public OAuthApplicationInfo createApplication(OAuthAppRequest oauthAppRequest) throws APIManagementException {

        HashMap<String, String> additionalProperties = KeyManagerUtil.getValuesForAdditionalProperties(oauthAppRequest);
        if (Boolean.parseBoolean(additionalProperties.get(OpenBankingConstants.REGULATORY))) {
            // Adding SP property to identify create request. Will be removed when setting up authenticators.
            additionalProperties.put("AppCreateRequest", "true");
        }
        // This SP property makes the username the default subject identifier.
        additionalProperties.put(OBKeyManagerConstants.USE_USER_ID_FOR_DEFAULT_SUBJECT, "false");
        doPreCreateApplication(oauthAppRequest, additionalProperties);
        OAuthApplicationInfo oAuthApplicationInfo = oauthAppRequest.getOAuthApplicationInfo();
        String username = (String) oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_CLIENT_USERNAME);
        oAuthApplicationInfo = super.createApplication(oauthAppRequest);
        // Need to get the application name after creating the application to obtain the generated app name
        String appName = oAuthApplicationInfo.getClientName();
        // Admin needs to have application role to retrieve and edit the app
        KeyManagerUtil.addApplicationRoleToAdmin(appName);

        try {
            String tenantDomain = ServiceProviderUtils.getSpTenantDomain(oAuthApplicationInfo.getClientId());
            updateSpProperties(appName, tenantDomain, username, additionalProperties, true);

            ServiceProvider appServiceProvider = getApplicationMgmtServiceImpl()
                    .getServiceProvider(appName, tenantDomain);
            ServiceProviderProperty regulatoryProperty = getSpPropertyFromSPMetaData(
                    OpenBankingConstants.REGULATORY, appServiceProvider.getSpProperties());

            if (regulatoryProperty != null) {
                if (Boolean.parseBoolean(regulatoryProperty.getValue())) {
                    OAuthAppRequest updatedOauthAppRequest = oauthAppRequest;
                    ServiceProviderProperty appNameProperty = getSpPropertyFromSPMetaData("DisplayName",
                            appServiceProvider.getSpProperties());
                    if (appNameProperty != null) {
                        updatedOauthAppRequest.getOAuthApplicationInfo().setClientName(appNameProperty.getValue());
                    }
                    // Assigning null as it is how the tokenScope parameter is used in the updateApplication method
                    updatedOauthAppRequest.getOAuthApplicationInfo().addParameter("tokenScope", null);
                    super.updateApplication(updatedOauthAppRequest);
                }
            }
            return oAuthApplicationInfo;

        } catch (OpenBankingException | APIManagementException e) {
            throw new APIManagementException(ExceptionCodes.OAUTH2_APP_CREATION_FAILED.getErrorMessage(),
                    e, ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
        } catch (IdentityApplicationManagementException e) {
            String errMsg = "error occurred in retrieving service provider for app " + appName;
            log.error(errMsg);
            throw new APIManagementException(errMsg, e, ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
        }
    }

    @Override
    @Generated(message = "Excluding from code coverage since it is covered from other method")
    public OAuthApplicationInfo updateApplication(OAuthAppRequest oAuthAppRequest) throws APIManagementException {

        HashMap<String, String> additionalProperties = KeyManagerUtil.getValuesForAdditionalProperties(oAuthAppRequest);
        // Adding SP property to identify update request. Will be removed when updating authenticators.
        additionalProperties.put("AppCreateRequest", "false");
        OAuthApplicationInfo oAuthApplicationInfo = oAuthAppRequest.getOAuthApplicationInfo();
        String clientId = oAuthApplicationInfo.getClientId();
        // There is no way to identify the client type in here. So we have to hardcode "oauth2" as the client type
        try {
            ServiceProvider serviceProvider = getApplicationMgmtServiceImpl()
                    .getServiceProviderByClientId(clientId, OAUTH2, tenantDomain);
            doPreUpdateApplication(oAuthAppRequest, additionalProperties, serviceProvider);
            String appName = serviceProvider.getApplicationName();
            String username = (String) oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_CLIENT_USERNAME);
            updateSpProperties(appName, tenantDomain, username, additionalProperties, false);
        } catch (IdentityApplicationManagementException e) {
            String errMsg = "Cannot find Service provider application for client Id " + clientId;
            log.error(errMsg);
            throw new APIManagementException(errMsg, ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
        }

        oAuthApplicationInfo = super.updateApplication(oAuthAppRequest);
        return oAuthApplicationInfo;
    }

    @Override
    @Generated(message = "Excluding from code coverage since it is covered from other method")
    public OAuthApplicationInfo retrieveApplication(String consumerKey) throws APIManagementException {

        OAuthApplicationInfo oAuthApplicationInfo = super.retrieveApplication(consumerKey);
        String name = oAuthApplicationInfo.getClientName();
        try {
            String tenantDomain = ServiceProviderUtils.getSpTenantDomain(consumerKey);
            org.wso2.carbon.identity.application.common.model.ServiceProvider appServiceProvider =
                    getApplicationMgmtServiceImpl().getServiceProvider(name, tenantDomain);
            // Iterate OB specific additional properties to check whether they override the value of any predefined
            // sp properties in application management listeners
            List<ServiceProviderProperty> spProperties =
                    new ArrayList<>(Arrays.asList(appServiceProvider.getSpProperties()));
            return updateAdditionalProperties(oAuthApplicationInfo, spProperties);
        } catch (IdentityApplicationManagementException | OpenBankingException e) {
            throw new APIManagementException(ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED.getErrorMessage(),
                    e, ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
        }
    }

    /**
     * @param spAppName            Generate service provider application name
     * @param tenantDomain         Tenant domain of the service provider application
     * @param username             Application owner
     * @param additionalProperties new Service provider property map
     * @param isCreateApp           Whether this function is called at app creation
     * @throws APIManagementException
     */
    protected void updateSpProperties(String spAppName, String tenantDomain, String username,
                                      HashMap<String, String> additionalProperties, boolean isCreateApp)
            throws APIManagementException {

        try {
            org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO oAuthConsumerAppDTO = getOAuthAdminService().
                    getOAuthApplicationDataByAppName(spAppName);
            ServiceProvider serviceProvider = getApplicationMgmtServiceImpl()
                    .getServiceProvider(spAppName, tenantDomain);
            doPreUpdateSpApp(oAuthConsumerAppDTO, serviceProvider, additionalProperties, isCreateApp);
            // Iterate OB specific additional properties to check whether they override the value of any predefined
            // sp properties in application management listeners
            List<ServiceProviderProperty> spProperties =
                    new ArrayList<>(Arrays.asList(serviceProvider.getSpProperties()));
            for (Map.Entry<String, String> propertyElement : additionalProperties.entrySet()) {
                ServiceProviderProperty overridenSPproperty = spProperties.stream().filter(
                        serviceProviderProperty -> serviceProviderProperty.getName()
                                .equalsIgnoreCase(propertyElement.getKey())).findAny().orElse(null);
                // If SP property is overridden, remove old SP property and add the new one
                if (overridenSPproperty != null) {
                    spProperties.remove(overridenSPproperty);
                    overridenSPproperty.setValue(propertyElement.getValue());
                    spProperties.add(overridenSPproperty);
                } else {
                    ServiceProviderProperty additionalProperty = new ServiceProviderProperty();
                    additionalProperty.setName(propertyElement.getKey());
                    additionalProperty.setValue(propertyElement.getValue());
                    spProperties.add(additionalProperty);
                }
            }
            serviceProvider.setSpProperties(spProperties.toArray(new ServiceProviderProperty[0]));
            try {
                getApplicationMgmtServiceImpl().updateApplication(serviceProvider, tenantDomain, username);
                if (log.isDebugEnabled()) {
                    log.debug("Successfully updated service provider properties for app " + spAppName);
                }
            } catch (IdentityApplicationManagementException e) {
                String errMsg = "error occurred while updating service provider " + spAppName;
                log.error(errMsg);
                throw new APIManagementException(errMsg, e, ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
            }

            try {
                getOAuthAdminService().updateConsumerApplication(oAuthConsumerAppDTO);
                if (log.isDebugEnabled()) {
                    log.debug("Successfully updated oAuth application DTO for app " + spAppName);
                }
            } catch (IdentityOAuthAdminException e) {
                String errMsg = "error occurred while updating oAuth Application data for app " + spAppName;
                log.error(errMsg);
                throw new APIManagementException(errMsg, e, ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
            }

        } catch (IdentityApplicationManagementException | IdentityOAuthAdminException e) {
            String errMsg = "error occurred in retrieving service provider or oAuth app " + spAppName;
            log.error(errMsg);
            throw new APIManagementException(errMsg, e, ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
        }

    }

    /**
     * Extract values for additional properties defined in the config from database and add to oAuthApplicationInfo.
     *
     * @return oAuth application Info
     */
    protected OAuthApplicationInfo updateAdditionalProperties(OAuthApplicationInfo oAuthApplicationInfo,
                                                              List<ServiceProviderProperty> spProperties) {

        Map<String, Map<String, String>> keyManagerAdditionalProperties = OpenBankingConfigParser.getInstance()
                .getKeyManagerAdditionalProperties();
        for (String key : keyManagerAdditionalProperties.keySet()) {
            for (ServiceProviderProperty spProperty : spProperties) {
                if (spProperty.getName().equalsIgnoreCase(key)) {
                    ((HashMap<String, String>) oAuthApplicationInfo.getParameter(
                            APIConstants.JSON_ADDITIONAL_PROPERTIES)).put(key, spProperty.getValue());
                }
            }
        }
        return oAuthApplicationInfo;
    }

    /**
     * Validate additional properties at toolkit level.
     *
     * @param obAdditionalProperties Values for additional property list defined in the config
     * @throws APIManagementException when failed to validate a given property
     */
    @Generated(message = "Excluding from code coverage since the method body is at toolkit")
    public void validateAdditionalProperties(Map<String, ConfigurationDto> obAdditionalProperties)
            throws APIManagementException {

        OBKeyManagerExtensionInterface obKeyManagerExtensionImpl = KeyManagerUtil.getOBKeyManagerExtensionImpl();
        if (obKeyManagerExtensionImpl != null) {
            obKeyManagerExtensionImpl.validateAdditionalProperties(obAdditionalProperties);
        }
    }

    /**
     * Do changes to app request before creating the app at toolkit level.
     *
     * @param additionalProperties Values for additional property list defined in the config
     * @throws APIManagementException when failed to validate a given property
     */
    @Generated(message = "Excluding from code coverage since the method body is at toolkit")
    public void doPreCreateApplication(OAuthAppRequest oAuthAppRequest, HashMap<String, String> additionalProperties)
            throws APIManagementException {
        OBKeyManagerExtensionInterface obKeyManagerExtensionImpl = KeyManagerUtil.getOBKeyManagerExtensionImpl();
        if (obKeyManagerExtensionImpl != null) {
            obKeyManagerExtensionImpl.doPreCreateApplication(oAuthAppRequest, additionalProperties);
        }
    }

    /**
     * Do changes to app request before updating the app at toolkit level.
     *
     * @param additionalProperties Values for additional property list defined in the config
     * @throws APIManagementException when failed to validate a given property
     */
    @Generated(message = "Excluding from code coverage since the method body is at toolkit")
    public void doPreUpdateApplication(OAuthAppRequest oAuthAppRequest, HashMap<String, String> additionalProperties,
                                       ServiceProvider serviceProvider) throws APIManagementException {
        OBKeyManagerExtensionInterface obKeyManagerExtensionImpl = KeyManagerUtil.getOBKeyManagerExtensionImpl();
        if (obKeyManagerExtensionImpl != null) {
            obKeyManagerExtensionImpl.doPreUpdateApplication(oAuthAppRequest, additionalProperties, serviceProvider);
        }
    }

    /**
     * Do changes to service provider before updating the service provider properties.
     *
     * @param oAuthConsumerAppDTO oAuth application DTO
     * @param serviceProvider Service provider application
     * @param isCreateApp           Whether this function is called at app creation
     * @throws APIManagementException when failed to validate a given property
     */
    @Generated(message = "Excluding from code coverage since the method body is at toolkit")
    public void doPreUpdateSpApp(org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO oAuthConsumerAppDTO,
                                 ServiceProvider serviceProvider,
                                 HashMap<String, String> additionalProperties, boolean isCreateApp)
            throws APIManagementException {

        OBKeyManagerExtensionInterface obKeyManagerExtensionImpl = KeyManagerUtil.getOBKeyManagerExtensionImpl();
        if (obKeyManagerExtensionImpl != null) {
            obKeyManagerExtensionImpl.doPreUpdateSpApp(oAuthConsumerAppDTO, serviceProvider, additionalProperties,
                    isCreateApp);
        }
    }

    @Generated(message = "Added for unit testing purposes")
    protected ApplicationManagementServiceImpl getApplicationMgmtServiceImpl() {

        return ApplicationManagementServiceImpl.getInstance();
    }

    @Generated(message = "Added for unit testing purposes")
    protected OAuthAdminService getOAuthAdminService() {
        return new OAuthAdminService();
    }

    protected ServiceProviderProperty getSpPropertyFromSPMetaData(String propertyName,
                                                                  ServiceProviderProperty[] spProperties) {

        return Arrays.asList(spProperties).stream().filter(serviceProviderProperty -> serviceProviderProperty.getName()
                .equalsIgnoreCase(propertyName)).findAny().orElse(null);
    }

}
