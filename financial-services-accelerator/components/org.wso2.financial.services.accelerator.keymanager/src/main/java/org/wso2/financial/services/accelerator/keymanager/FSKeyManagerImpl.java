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

package org.wso2.financial.services.accelerator.keymanager;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
import org.wso2.carbon.apimgt.api.model.ConfigurationDto;
import org.wso2.carbon.apimgt.api.model.KeyManagerConnectorConfiguration;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.keymanager.internal.KeyManagerDataHolder;
import org.wso2.financial.services.accelerator.keymanager.utils.FSKeyManagerConstants;
import org.wso2.financial.services.accelerator.keymanager.utils.FSKeyManagerUtil;
import org.wso2.financial.services.accelerator.keymanager.utils.IdentityServerUtils;
import org.wso2.is7.client.WSO2IS7KeyManager;
import org.wso2.is7.client.WSO2IS7KeyManagerConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FS key manager client impl class.
 */
public class FSKeyManagerImpl extends WSO2IS7KeyManager {

    private static final Log log = LogFactory.getLog(FSKeyManagerImpl.class);

    public static final String OAUTH2 = "oauth2";

    @Override
    public AccessTokenInfo getNewApplicationAccessToken(AccessTokenRequest tokenRequest) throws APIManagementException {

        try {
            JSONObject spAppData = IdentityServerUtils.getSPApplicationFromClientId(tokenRequest.getClientId());
            String regulatoryProperty = IdentityServerUtils.getRegulatoryPropertyFromSPMetadata(spAppData);
            if (Boolean.parseBoolean(regulatoryProperty)) {
                return null;
            }
        } catch (FinancialServicesException e) {
            log.error("Error while generating keys. ", e);
        }
        return super.getNewApplicationAccessToken(tokenRequest);
    }

    @Override
    public String getType() {

        return FSKeyManagerConstants.CUSTOM_KEYMANAGER_TYPE;
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
        Map<String, ConfigurationDto> fsAdditionalProperties = new HashMap<>();

        KeyManagerConnectorConfiguration keyManagerConnectorConfiguration = KeyManagerDataHolder.getInstance()
                .getKeyManagerConnectorConfiguration(type);
        // Obtain additional key manager configurations defined in config
        Map<String, Map<String, String>> keyManagerAdditionalProperties = FinancialServicesConfigParser.getInstance()
                .getKeyManagerAdditionalProperties();

        if (keyManagerConnectorConfiguration != null) {
            List<ConfigurationDto> applicationConfigurationDtoList = keyManagerConnectorConfiguration
                    .getApplicationConfigurations();
            Object additionalProperties = oAuthApplicationInfo.getParameter(APIConstants.JSON_ADDITIONAL_PROPERTIES);
            if (additionalProperties != null) {
                JSONObject additionalPropertiesJson;
                try {
                    additionalPropertiesJson = new JSONObject(additionalProperties.toString());
                } catch (JSONException e) {
                    String errMsg = "Additional properties is not a valid JSON string";
                    throw new APIManagementException(errMsg, e, ExceptionCodes
                            .from(ExceptionCodes.INVALID_APPLICATION_ADDITIONAL_PROPERTIES,
                                    errMsg));
                }

                for (ConfigurationDto configurationDto : applicationConfigurationDtoList) {
                    String key = configurationDto.getName();
                    String values = null;
                    if (additionalPropertiesJson.has(key)) {
                        values = additionalPropertiesJson.get(key).toString();
                    }

                    if (values == null) {
                        // AbstractKeyManager Validations
                        // Check if mandatory parameters are missing
                        if (configurationDto.isRequired()) {
                            missedRequiredValues.add(configurationDto.getName());
                        }
                    } else {
                        // FSKeyManager Validations
                        if (keyManagerAdditionalProperties.containsKey(key)) {
                            configurationDto.setValues(Collections.singletonList(values));
                            fsAdditionalProperties.put(key, configurationDto);
                        } else {
                            // AMDefaultKeyManager validations
                            // Check for invalid time periods
                            if (StringUtils.isNotBlank(values) && !StringUtils
                                    .equals(values, APIConstants.KeyManager.NOT_APPLICABLE_VALUE)) {
                                try {
                                    if (WSO2IS7KeyManagerConstants.PKCE_MANDATORY.equals(key) ||
                                            WSO2IS7KeyManagerConstants.PKCE_SUPPORT_PLAIN.equals(key) ||
                                            WSO2IS7KeyManagerConstants.PUBLIC_CLIENT.equals(key)) {

                                        if (!(values.equals(Boolean.TRUE.toString()) ||
                                                values.equals(Boolean.FALSE.toString()))) {
                                            String errMsg = "Application configuration values cannot have negative " +
                                                    "values.";
                                            throw new APIManagementException(errMsg, ExceptionCodes
                                                    .from(ExceptionCodes.INVALID_APPLICATION_ADDITIONAL_PROPERTIES,
                                                            errMsg));
                                        }
                                    } else {
                                        long longValue = Long.parseLong(values);
                                        if (longValue < 0) {
                                            String errMsg = "Application configuration values cannot have negative " +
                                                    "values.";
                                            throw new APIManagementException(errMsg, ExceptionCodes
                                                    .from(ExceptionCodes.INVALID_APPLICATION_ADDITIONAL_PROPERTIES,
                                                            errMsg));
                                        }
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
                if (fsAdditionalProperties.size() != 0) {
                    validateAdditionalProperties(fsAdditionalProperties);
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

        HashMap<String, String> additionalProperties = FSKeyManagerUtil
                .getValuesForAdditionalProperties(oauthAppRequest);
        if (Boolean.parseBoolean(additionalProperties.get(FinancialServicesConstants.REGULATORY))) {
            // Adding SP property to identify create request. Will be removed when setting up authenticators.
            additionalProperties.put("AppCreateRequest", "true");
        }
        doPreCreateApplication(oauthAppRequest, additionalProperties);
        OAuthApplicationInfo oAuthApplicationInfo = oauthAppRequest.getOAuthApplicationInfo();
        oauthAppRequest.setOAuthApplicationInfo(oAuthApplicationInfo);
        oAuthApplicationInfo = super.createApplication(oauthAppRequest);
        // Need to get the application name after creating the application to obtain the generated app name
        String appName = oAuthApplicationInfo.getClientName();
        // Admin needs to have application role to retrieve and edit the app
        FSKeyManagerUtil.addApplicationRoleToAdmin(appName);

        try {
            JSONObject serviceProviderAppData = IdentityServerUtils.getSPApplicationFromClientId(
                    oAuthApplicationInfo.getClientId());

            updateSpProperties(appName, oAuthApplicationInfo, serviceProviderAppData, additionalProperties, true);

            if (Boolean.parseBoolean(additionalProperties.get("regulatory"))) {
                String appNameProperty = IdentityServerUtils.getSpPropertyFromSPMetaData("DisplayName",
                        IdentityServerUtils.getSPMetadataFromSPApp(serviceProviderAppData));
                if (appNameProperty != null) {
                    oauthAppRequest.getOAuthApplicationInfo().setClientName(appNameProperty);
                }
                // Assigning null as it is how the tokenScope parameter is used in the updateApplication method
                oauthAppRequest.getOAuthApplicationInfo().addParameter("tokenScope", null);
                super.updateApplication(oauthAppRequest);
            }
            return oAuthApplicationInfo;

        } catch (FinancialServicesException e) {
            throw new APIManagementException(ExceptionCodes.OAUTH2_APP_CREATION_FAILED.getErrorMessage(), e,
                    ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
        }
    }

    @Override
    @Generated(message = "Excluding from code coverage since it is covered from other method")
    public OAuthApplicationInfo updateApplication(OAuthAppRequest oAuthAppRequest) throws APIManagementException {

        HashMap<String, String> additionalProperties = FSKeyManagerUtil
                .getValuesForAdditionalProperties(oAuthAppRequest);
        // Adding SP property to identify update request. Will be removed when updating authenticators.
        additionalProperties.put("AppCreateRequest", "false");
        OAuthApplicationInfo oAuthApplicationInfo = oAuthAppRequest.getOAuthApplicationInfo();
        try {
            JSONObject appData = IdentityServerUtils.getSPApplicationFromClientId(oAuthApplicationInfo.getClientId());

            doPreUpdateApplication(oAuthAppRequest, additionalProperties, appData);
            String appName = appData.getString("name");
            updateSpProperties(appName, oAuthApplicationInfo, appData, additionalProperties, false);
        } catch (FinancialServicesException e) {
            throw new RuntimeException(e);
        }

        oAuthApplicationInfo = super.updateApplication(oAuthAppRequest);
        return oAuthApplicationInfo;
    }

    @Override
    @Generated(message = "Excluding from code coverage since it is covered from other method")
    public OAuthApplicationInfo retrieveApplication(String consumerKey) throws APIManagementException {

        OAuthApplicationInfo oAuthApplicationInfo = super.retrieveApplication(consumerKey);
        try {
            JSONObject appData = IdentityServerUtils.getSPApplicationFromClientId(oAuthApplicationInfo.getClientId());
            return updateAdditionalProperties(oAuthApplicationInfo,
                    IdentityServerUtils.getSPMetadataFromSPApp(appData));
        } catch (FinancialServicesException e) {
            throw new APIManagementException(ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED.getErrorMessage(),
                    e, ExceptionCodes.OAUTH2_APP_RETRIEVAL_FAILED);
        }
    }

    /**
     * @param spAppName               Generate service provider application name
     * @param oAuthApplicationInfo    OAuthApplicationInfo from the request
     * @param serviceProviderAppData  Service provider application data
     * @param additionalProperties    new Service provider property map
     * @param isCreateApp             Whether this function is called at app creation
     * @throws APIManagementException when failed to update the application properties
     */
    protected void updateSpProperties(String spAppName, OAuthApplicationInfo oAuthApplicationInfo ,
                                      JSONObject serviceProviderAppData,
                                      HashMap<String, String> additionalProperties, boolean isCreateApp)
            throws APIManagementException {

        try {
            doPreUpdateSpApp(oAuthApplicationInfo, serviceProviderAppData, additionalProperties, isCreateApp);
            // Iterate FS specific additional properties to check whether they override the value of any predefined
            // sp properties in application management listeners
            Map<String, Object> spProperties = IdentityServerUtils.constructSPPropertiesList(
                    IdentityServerUtils.getSPMetadataFromSPApp(serviceProviderAppData), additionalProperties);

            // Update the DCR application
            IdentityServerUtils.updateDCRApplication(serviceProviderAppData.getString("clientId"), spAppName,
                    spProperties);

            boolean isAppCreateRequest = Boolean.parseBoolean(additionalProperties.get("AppCreateRequest"));
            boolean isRegulatory = Boolean.parseBoolean(additionalProperties
                    .get(FinancialServicesConstants.REGULATORY));
            if (isAppCreateRequest && isRegulatory) {
                String certificate = additionalProperties.get(FSKeyManagerConstants.SP_CERTIFICATE);
                IdentityServerUtils.updateSPApplication(serviceProviderAppData.getString("clientId"), certificate);
            }
        } catch (FinancialServicesException e) {
            log.error("Error while updating service provider application properties", e);
            throw new APIManagementException("Error while updating service provider application properties", e);
        }
    }

    /**
     * Extract values for additional properties defined in the config from database and add to oAuthApplicationInfo.
     *
     * @return oAuth application Info
     */
    protected OAuthApplicationInfo updateAdditionalProperties(OAuthApplicationInfo oAuthApplicationInfo,
                                                              JSONArray spProperties) {

        Map<String, Map<String, String>> keyManagerAdditionalProperties = FinancialServicesConfigParser.getInstance()
                .getKeyManagerAdditionalProperties();
        for (String key : keyManagerAdditionalProperties.keySet()) {
            for (int i = 0; i < spProperties.length(); i++) {
                JSONObject spPropertyObj = spProperties.getJSONObject(i);
                if (spPropertyObj.get("name").equals(key)) {
                    ((HashMap<String, String>) oAuthApplicationInfo.getParameter(
                            APIConstants.JSON_ADDITIONAL_PROPERTIES)).put(key, spPropertyObj.getString("value"));
                }
            }
        }
        return oAuthApplicationInfo;
    }

    /**
     * Validate additional properties at toolkit level.
     *
     * @param fsAdditionalProperties Values for additional property list defined in the config
     * @throws APIManagementException when failed to validate a given property
     */
    @Generated(message = "Excluding from code coverage since the method body is at toolkit")
    public void validateAdditionalProperties(Map<String, ConfigurationDto> fsAdditionalProperties)
            throws APIManagementException {

        String regulatory = FSKeyManagerUtil.getValueForAdditionalProperty(fsAdditionalProperties,
                FinancialServicesConstants.REGULATORY);
        if (Boolean.parseBoolean(regulatory)) {
            String spCertificate = FSKeyManagerUtil.getValueForAdditionalProperty(fsAdditionalProperties,
                    FSKeyManagerConstants.SP_CERTIFICATE);
            FSKeyManagerUtil.validateCertificate(spCertificate);
        }

        FSKeyManagerExtensionInterface keyManagerExtensionImpl = FSKeyManagerUtil.getKeyManagerExtensionImpl();
        if (keyManagerExtensionImpl != null) {
            keyManagerExtensionImpl.validateAdditionalProperties(fsAdditionalProperties);
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
        FSKeyManagerExtensionInterface keyManagerExtensionImpl = FSKeyManagerUtil.getKeyManagerExtensionImpl();
        if (keyManagerExtensionImpl != null) {
            keyManagerExtensionImpl.doPreCreateApplication(oAuthAppRequest, additionalProperties);
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
                                       JSONObject serviceProvider) throws APIManagementException {

        FSKeyManagerExtensionInterface keyManagerExtensionImpl = FSKeyManagerUtil.getKeyManagerExtensionImpl();
        if (keyManagerExtensionImpl != null) {
            keyManagerExtensionImpl.doPreUpdateApplication(oAuthAppRequest, additionalProperties, serviceProvider);
        }
    }

    /**
     * Do changes to service provider before updating the service provider properties.
     *
     * @param oAuthApplicationInfo  OAuthApplicationInfo from the request
     * @param spAppData             Service provider application data
     * @param additionalProperties  AdditionalProperties
     * @param isCreateApp           Whether this function is called at app creation
     * @throws APIManagementException when failed to validate a given property
     */
    @Generated(message = "Excluding from code coverage since the method body is at toolkit")
    public void doPreUpdateSpApp(OAuthApplicationInfo oAuthApplicationInfo, JSONObject spAppData, HashMap<String, String> additionalProperties,
                                 boolean isCreateApp)
            throws APIManagementException {

        FSKeyManagerExtensionInterface keyManagerExtensionImpl = FSKeyManagerUtil.getKeyManagerExtensionImpl();
        if (keyManagerExtensionImpl != null) {
            keyManagerExtensionImpl.doPreUpdateSpApp(oAuthApplicationInfo, spAppData, additionalProperties, isCreateApp);
        }
    }

}
