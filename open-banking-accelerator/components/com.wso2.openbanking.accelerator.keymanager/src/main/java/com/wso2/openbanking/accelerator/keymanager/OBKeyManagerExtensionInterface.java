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

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.ConfigurationDto;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;

import java.util.HashMap;
import java.util.Map;

/**
 * Interface for validation OB Key Manager Additional Properties.
 */
public interface OBKeyManagerExtensionInterface {

    /**
     * Validate additional properties.
     *
     * @param obAdditionalProperties OB Additional Properties Map
     * @throws APIManagementException when failed to validate a given property
     */
    void validateAdditionalProperties(Map<String, ConfigurationDto> obAdditionalProperties)
            throws APIManagementException;

    /**
     * Do changes to app request before creating the app at toolkit level.
     *
     * @param oAuthAppRequest OAuth Application Request
     * @param additionalProperties Values for additional property list defined in the config
     * @throws APIManagementException when failed to validate a given property
     */
    void doPreCreateApplication(OAuthAppRequest oAuthAppRequest, HashMap<String, String> additionalProperties)
            throws APIManagementException;

    /**
     * Do changes to app request before updating the app at toolkit level.
     *
     * @param oAuthAppRequest OAuth Application Request
     * @param  additionalProperties Values for additional property list defined in the config
     * @throws APIManagementException when failed to validate a given property
     */

    void doPreUpdateApplication(OAuthAppRequest oAuthAppRequest, HashMap<String, String> additionalProperties,
                                ServiceProvider serviceProvider)
            throws APIManagementException;

    /**
     * Do changes to service provider before updating the service provider properties.
     *
     * @param oAuthConsumerAppDTO oAuth application DTO
     * @param serviceProvider Service provider application
     * @param  additionalProperties Values for additional property list defined in the config
     * @param isCreateApp           Whether this functions is called at app creation
     * @throws APIManagementException when failed to validate a given property
     */
    void doPreUpdateSpApp(OAuthConsumerAppDTO oAuthConsumerAppDTO, ServiceProvider serviceProvider,
                          HashMap<String, String> additionalProperties, boolean isCreateApp)
            throws APIManagementException;
}
