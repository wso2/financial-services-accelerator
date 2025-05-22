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

package org.wso2.financial.services.accelerator.keymanager.utils;

import java.util.Map;

/**
 * FS Key Manager Constants.
 */
public class FSKeyManagerConstants {

    public static final String CUSTOM_KEYMANAGER_TYPE = "fsKeyManager";
    public static final String API_KEY_VALIDATOR_USERNAME = "APIKeyValidator.Username";
    public static final String API_KEY_VALIDATOR_PASSWORD = "APIKeyValidator.Password";
    public static final String API_KEY_VALIDATOR_URL = "APIKeyValidator.ServerURL";
    public static final String SERVICE = "/services";

    public static final String APP_RETRIEVAL_URL = "/api/server/v1/applications/";
    public static final String DCR_EP = "/api/identity/oauth2/dcr/v1.1/register/";

    public static final Map<String, String> APP_CONFIG_MAPPING = Map.of(
            "ext_refresh_token_lifetime", "refresh_token_expiry_time",
            "ext_id_token_lifetime", "id_token_expiry_time",
            "ext_application_token_lifetime", "application_access_token_expiry_time",
            "ext_user_token_lifetime", "user_access_token_expiry_time");

}
