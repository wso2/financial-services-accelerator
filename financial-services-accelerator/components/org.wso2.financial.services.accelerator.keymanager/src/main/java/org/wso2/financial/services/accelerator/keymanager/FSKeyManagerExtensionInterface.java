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

import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.ConfigurationDto;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Interface for validation FS Key Manager Additional Properties.
 */
public interface FSKeyManagerExtensionInterface {

    /**
     * Validate additional properties.
     *
     * @param additionalProperties FS Additional Properties Map
     * @throws APIManagementException when failed to validate a given property
     */
    void validateAdditionalProperties(Map<String, ConfigurationDto> additionalProperties)
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
                                JSONObject serviceProvider)
            throws APIManagementException;

    /**
     * Do changes to service provider before updating the service provider properties.
     *
     * @param  oAuthApplicationInfo OAuth Application Info
     * @param  spAppData            Service provider data
     * @param  additionalProperties Values for additional property list defined in the config
     * @param isCreateApp           Whether this functions is called at app creation
     * @throws APIManagementException when failed to validate a given property
     */
    void doPreUpdateSpApp(OAuthApplicationInfo oAuthApplicationInfo, JSONObject spAppData, HashMap<String, String> additionalProperties, boolean isCreateApp)
            throws APIManagementException;
}
