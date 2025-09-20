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

package org.wso2.financial.services.apim.mediation.policies.consent.enforcement.constants;

/**
 * Constants class for the Consent Enforcement Policy.
 */
public class ConsentEnforcementConstants {

    public static final String AUTH_HEADER = "Authorization";
    public static final String BEARER_TAG = "Bearer ";
    public static final String BASIC_TAG = "Basic ";
    public static final String COLON = ":";
    public static final String CONTENT_TYPE_TAG = "Content-Type";
    public static final String JWT_CONTENT_TYPE = "application/jwt";
    public static final String API_KEY_VALIDATOR_USERNAME = "APIKeyValidator.Username";
    public static final String API_KEY_VALIDATOR_PASSWORD = "APIKeyValidator.Password";
    public static final String INFO_HEADER_TAG = "Account-Request-Information";
    public static final String IS_VALID = "isValid";
    public static final String MODIFIED_PAYLOAD = "modifiedPayload";
    public static final String CONSENT_INFO = "consentInformation";
    public static final String CONSENT_VALIDATE_ENDPOINT = "/api/fs/consent/validate/validate";

    // Configs
    public static final String KEYSTORE_LOCATION_TAG = "Security.InternalKeyStore.Location";
    public static final String KEYSTORE_PASSWORD_TAG = "Security.InternalKeyStore.Password";
    public static final String SIGNING_ALIAS_TAG = "Security.InternalKeyStore.KeyAlias";
    public static final String SIGNING_KEY_PASSWORD = "Security.InternalKeyStore.KeyPassword";

    // Additional param keys
    public static final String ELECTED_RESOURCE_TAG = "electedResource";
    public static final String CONSENT_ID_TAG = "consentId";
    public static final String USER_ID_TAG = "userId";
    public static final String CLIENT_ID_TAG = "clientId";
    public static final String RESOURCE_PARAMS_TAG = "resourceParams";
    public static final String CONTEXT_TAG = "context";
    public static final String RESOURCE_TAG = "resource";
    public static final String HTTP_METHOD_TAG = "httpMethod";
    public static final String HEADERS_TAG = "headers";
    public static final String BODY_TAG = "body";

    // Message context properties
    public static final String API_ELECTED_RESOURCE = "API_ELECTED_RESOURCE";
    public static final String REST_FULL_REQUEST_PATH = "REST_FULL_REQUEST_PATH";
    public static final String REST_METHOD = "REST_METHOD";
    public static final String REST_API_CONTEXT = "REST_API_CONTEXT";
    public static final String CONSUMER_KEY = "api.ut.consumerKey";
    public static final String USER_ID = "api.ut.userId";

    // Error constants
    public static final String ERROR_CODE = "ERROR_CODE";
    public static final String ERROR_TITLE = "ERROR_TITLE";
    public static final String ERROR_DESCRIPTION = "ERROR_DESCRIPTION";
    public static final String CUSTOM_HTTP_SC = "CUSTOM_HTTP_SC";

}
