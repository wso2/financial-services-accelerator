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
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;

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
        return configurationDtoList;

    }

    @Override
    public List<ConfigurationDto> getApplicationConfigurations() {

        List<ConfigurationDto> applicationConfigurationsList = new ArrayList();
        applicationConfigurationsList
                .add(new ConfigurationDto(APIConstants.KeyManager.APPLICATION_ACCESS_TOKEN_EXPIRY_TIME,
                        "Application Access Token Expiry Time ", "input", "Type Application Access Token Expiry Time " +
                        "in seconds ", APIConstants.KeyManager.NOT_APPLICABLE_VALUE, false, false,
                        Collections.EMPTY_LIST, false));
        applicationConfigurationsList
                .add(new ConfigurationDto(APIConstants.KeyManager.USER_ACCESS_TOKEN_EXPIRY_TIME,
                        "User Access Token Expiry Time ", "input", "Type User Access Token Expiry Time " +
                        "in seconds ", APIConstants.KeyManager.NOT_APPLICABLE_VALUE, false, false,
                        Collections.EMPTY_LIST, false));
        applicationConfigurationsList
                .add(new ConfigurationDto(APIConstants.KeyManager.REFRESH_TOKEN_EXPIRY_TIME,
                        "Refresh Token Expiry Time ", "input", "Type Refresh Token Expiry Time " +
                        "in seconds ", APIConstants.KeyManager.NOT_APPLICABLE_VALUE, false, false,
                        Collections.EMPTY_LIST, false));
        applicationConfigurationsList
                .add(new ConfigurationDto(APIConstants.KeyManager.ID_TOKEN_EXPIRY_TIME,
                        "Id Token Expiry Time", "input", "Type ID Token Expiry Time " +
                        "in seconds ", APIConstants.KeyManager.NOT_APPLICABLE_VALUE, false, false,
                        Collections.EMPTY_LIST, false));

        Map<String, Map<String, String>> keyManagerAdditionalProperties = FinancialServicesConfigParser.getInstance()
                .getKeyManagerAdditionalProperties();

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
