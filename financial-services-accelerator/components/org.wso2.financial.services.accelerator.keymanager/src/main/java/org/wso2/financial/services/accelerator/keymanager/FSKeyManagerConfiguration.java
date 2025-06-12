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

import org.apache.commons.lang.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.model.ConfigurationDto;
import org.wso2.carbon.apimgt.api.model.KeyManagerConnectorConfiguration;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.jwt.JWTValidatorImpl;
import org.wso2.financial.services.accelerator.keymanager.internal.KeyManagerDataHolder;
import org.wso2.financial.services.accelerator.keymanager.utils.FSKeyManagerConstants;
import org.wso2.is7.client.WSO2IS7KeyManagerConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Key manager configuration class to override the default key manager interface implementation.
 */
@Component(
        name = "org.wso2.financial.services.accelerator.keymanager.config",
        immediate = true,
        service = KeyManagerConnectorConfiguration.class
)
public class FSKeyManagerConfiguration implements KeyManagerConnectorConfiguration {

    @Override
    public String getImplementation() {

        return FSKeyManagerImpl.class.getName();
    }

    @Override
    public String getJWTValidator() {

        return JWTValidatorImpl.class.getName();
    }

    @Override
    public List<ConfigurationDto> getConnectionConfigurations() {

        List<ConfigurationDto> configurationDtoList = new ArrayList<>();
        configurationDtoList
                .add(new ConfigurationDto("Username", "Username", "input", "Username of admin user", "",
                        true, false, Collections.emptyList(), false));
        configurationDtoList
                .add(new ConfigurationDto("Password", "Password", "input",
                        "Password of Admin user", "", true, true, Collections.emptyList(), false));
        configurationDtoList.add(new ConfigurationDto("api_resource_management_endpoint",
                "WSO2 Identity Server 7 API Resource Management Endpoint", "input",
                String.format("E.g., %s/api/server/v1/api-resources",
                        org.wso2.carbon.apimgt.api.APIConstants.DEFAULT_KEY_MANAGER_HOST), "", true, false,
                Collections.emptyList(), false));
        configurationDtoList.add(new ConfigurationDto("is7_roles_endpoint",
                "WSO2 Identity Server 7 Roles Endpoint", "input",
                String.format("E.g., %s/scim2/v2/Roles",
                        org.wso2.carbon.apimgt.api.APIConstants.DEFAULT_KEY_MANAGER_HOST), "", true, false,
                Collections.emptyList(), false));
        configurationDtoList.add(new ConfigurationDto("enable_roles_creation",
                "Create roles in WSO2 Identity Server 7", "checkbox",
                "Create roles in WSO2 Identity Server 7, corresponding to the roles used in WSO2 API Manager.",
                "Enable", false, false, Collections.singletonList("Enable"), false));
        return configurationDtoList;

    }

    @Override
    public List<ConfigurationDto> getApplicationConfigurations() {

        List<ConfigurationDto> applicationConfigurationsList = new ArrayList();
        applicationConfigurationsList
                .add(new ConfigurationDto(WSO2IS7KeyManagerConstants.APPLICATION_TOKEN_LIFETIME,
                        "Lifetime of the Application Token ", "input", "Type Lifetime of the Application Token " +
                        "in seconds ", APIConstants.KeyManager.NOT_APPLICABLE_VALUE, false, false,
                        Collections.EMPTY_LIST, false));
        applicationConfigurationsList
                .add(new ConfigurationDto(WSO2IS7KeyManagerConstants.USER_TOKEN_LIFETIME,
                        "Lifetime of the User Token ", "input", "Type Lifetime of the User Token " +
                        "in seconds ", APIConstants.KeyManager.NOT_APPLICABLE_VALUE, false, false,
                        Collections.EMPTY_LIST, false));
        applicationConfigurationsList
                .add(new ConfigurationDto(WSO2IS7KeyManagerConstants.REFRESH_TOKEN_LIFETIME,
                        "Lifetime of the Refresh Token ", "input", "Type Lifetime of the Refresh Token " +
                        "in seconds ", APIConstants.KeyManager.NOT_APPLICABLE_VALUE, false, false,
                        Collections.EMPTY_LIST, false));
        applicationConfigurationsList
                .add(new ConfigurationDto(WSO2IS7KeyManagerConstants.ID_TOKEN_LIFETIME,
                        "Lifetime of the ID Token", "input", "Type Lifetime of the ID Token " +
                        "in seconds ", APIConstants.KeyManager.NOT_APPLICABLE_VALUE, false, false,
                        Collections.EMPTY_LIST, false));

        ConfigurationDto configurationDtoPkceMandatory = new ConfigurationDto(WSO2IS7KeyManagerConstants.PKCE_MANDATORY,
                "Enable PKCE", "checkbox", "Enable PKCE", String.valueOf(false), false, false,
                Collections.EMPTY_LIST, false);
        applicationConfigurationsList.add(configurationDtoPkceMandatory);

        ConfigurationDto configurationDtoPkcePlainText =
                new ConfigurationDto(WSO2IS7KeyManagerConstants.PKCE_SUPPORT_PLAIN,
                        "Support PKCE Plain text", "checkbox", "S256 is recommended, plain text too can be used.",
                        String.valueOf(false), false, false, Collections.EMPTY_LIST, false);
        applicationConfigurationsList.add(configurationDtoPkcePlainText);

        ConfigurationDto configurationDtoBypassClientCredentials =
                new ConfigurationDto(WSO2IS7KeyManagerConstants.PUBLIC_CLIENT,
                        "Public client", "checkbox", "Allow authentication without the client secret.",
                        String.valueOf(false), false, false, Collections.EMPTY_LIST, false);
        applicationConfigurationsList.add(configurationDtoBypassClientCredentials);

        Map<String, Map<String, String>> keyManagerAdditionalProperties = KeyManagerDataHolder.getInstance()
                .getConfigurationService().getKeyManagerConfigs();

        for (Map.Entry<String, Map<String, String>> propertyElement : keyManagerAdditionalProperties.entrySet()) {
            String propertyName = propertyElement.getKey();
            Map<String, String> property = propertyElement.getValue();
            boolean required = !StringUtils.isEmpty(property.get("required"))
                    && Boolean.parseBoolean(property.get("required"));
            boolean mask = !StringUtils.isEmpty(property.get("mask"))
                    && Boolean.parseBoolean(property.get("mask"));
            boolean multiple = !StringUtils.isEmpty(property.get("multiple"))
                    && Boolean.parseBoolean(property.get("multiple"));
            List<String> values = StringUtils.isEmpty(property.get("values")) ? Collections.EMPTY_LIST
                    : Arrays.asList(property.get("values").split(","));

            applicationConfigurationsList.add(new ConfigurationDto(propertyName, property.get("label"),
                    property.get("type"), property.get("tooltip"), property.get("default"), required , mask
                    , values, multiple));
        }

        return applicationConfigurationsList;
    }

    @Override
    public String getType() {

        return FSKeyManagerConstants.CUSTOM_KEYMANAGER_TYPE;
    }

    @Override
    public String getDefaultScopesClaim() {

        return APIConstants.JwtTokenConstants.SCOPE;
    }

    @Override
    public String getDefaultConsumerKeyClaim() {

        return APIConstants.JwtTokenConstants.AUTHORIZED_PARTY;
    }

}
