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

package org.wso2.financial.services.apim.mediation.policies.dynamic.client.registration.util;

/**
 * Constants for Dynamic Client Registration (DCR) Mediator.
 */
public class DCRConstants {

    public static final String CONTENT_TYPE_TAG = "Content-Type";
    public static final String JWT_CONTENT_TYPE = "application/jwt";
    public static final String JSON_CONTENT_TYPE = "application/json";
    public static final String HTTP_METHOD = "api.ut.HTTP_METHOD";
    public static final String JWT_HEAD = "head";
    public static final String JWT_BODY = "body";
    public static final String SOFTWARE_STATEMENT = "software_statement";
    public static final String SOFTWARE_ID = "software_id";
    public static final String CLIENT_NAME = "client_name";
    public static final String JWKS_URI = "jwks_uri";
    public static final String TOKEN_EP_ALLOW_REUSE_PVT_KEY_JWT = "token_endpoint_allow_reuse_pvt_key_jwt";
    public static final String PRIVATE_KEY_JWT = "private_key_jwt";
    public static final String APP_DISPLAY_NAME = "ext_application_display_name";
    public static final String TOKEN_EP_AUTH_METHOD = "token_endpoint_auth_method";
    public static final String DISALLOWED_CHARS_PATTERN = "([~!#$;%^&*+={}\\s\\|\\\\<>\\\"\\'\\/,\\]\\[\\(\\)])";
    public static final String SUBSTITUTE_STRING = "_";
    public static final int ABBREVIATED_STRING_LENGTH = 70;
    public static final String ERROR = "error";
    public static final String ERROR_DESCRIPTION = "error_description";
    public static final String API_UT_RESOURCE = "api.ut.resource";
    public static final String SLASH = "/";
    public static final String AUTHORIZATION = "Authorization";
    public static final String CLIENT_ID = "client_id";
    public static final String DISABLE_CHUNKING = "DISABLE_CHUNKING";
    public static final String RESPONSE = "RESPONSE";

    //HTTP Codes
    public static final String BAD_REQUEST_CODE = "400";
    public static final String SERVER_ERROR_CODE = "500";
    public static final String UNAUTHORIZED_CODE = "401";
    //Error Codes
    public static final String INVALID_REQUEST = "invalid_request";
    public static final String INVALID_SSA = "invalid_software_statement";
    public static final String SERVER_ERROR = "server_error";
    public static final String UNAUTHORIZED = "unauthorized";
}
