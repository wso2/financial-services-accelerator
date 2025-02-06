/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.gateway.util;

/**
 * Class containing the constants for Financial Services Gateway module.
 */
public class GatewayConstants {

    public static final String AUTH_HEADER = "Authorization";
    public static final String BEARER_TAG = "Bearer ";
    public static final String BASIC_TAG = "Basic ";
    public static final String CONTENT_TYPE_TAG = "Content-Type";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String JWT_CONTENT_TYPE = "application/jwt";
    public static final String JSON_CONTENT_TYPE = "application/json";
    public static final String JOSE_CONTENT_TYPE = "application/jose";
    public static final String APPLICATION_XML_CONTENT_TYPE = "application/xml";
    public static final String TEXT_XML_CONTENT_TYPE = "text/xml";
    public static final String SOAP_BODY = "soapenv:Body";
    public static final String SOAP_BODY_TEXT = "text";
    public static final String SOAP_BODY_CONTENT = "content";
    public static final String SOAP_JSON_OBJECT = "jsonObject";
    public static final String COLON = ":";
    public static final String SLASH = "/";
    public static final String POST_HTTP_METHOD = "POST";
    public static final String PUT_HTTP_METHOD = "PUT";
    public static final String GET_HTTP_METHOD = "GET";
    public static final String PATCH_HTTP_METHOD = "PATCH";
    public static final String DELETE_HTTP_METHOD = "DELETE";
    public static final String PUBLISHER_API_PATH = "api/am/publisher/apis/";
    public static final String SWAGGER_ENDPOINT = "/swagger";
    public static final String API_KEY_VALIDATOR_USERNAME = "APIKeyValidator.Username";
    public static final String API_KEY_VALIDATOR_PASSWORD = "APIKeyValidator.Password";
    public static final String API_TYPE_CONSENT = "consent";
    public static final String API_TYPE_NON_REGULATORY = "non-regulatory";
    public static final String API_TYPE_CUSTOM_PROP = "x-wso2-api-type";
    public static final String EXECUTOR_TYPE_CONSENT = "Consent";
    public static final String EXECUTOR_TYPE_DCR = "DCR";
    public static final String EXECUTOR_TYPE_DEFAULT = "Default";
    public static final String DCR_PATH = "/register";
    public static final String CONTEXT_PROP_CACHE_KEY = "_contextProp";
    public static final String ANALYTICS_PROP_CACHE_KEY = "_analyticsData";
    public static final String ERROR_STATUS_PROP = "errorStatusCode";
    public static final String IS_RETURN_RESPONSE = "isReturnResponse";
    public static final String MODIFIED_STATUS = "ModifiedStatus";
    public static final String CODE = "code";
    public static final String MESSAGE = "message";
    public static final String DESCRIPTION = "description";
    public static final String ERROR = "error";
    public static final String ERROR_DESCRIPTION = "error_description";
    public static final String LINKS = "links";
    public static final String IAT = "iat";
    public static final String EXP = "exp";
    public static final String AUD = "aud";
    public static final String ISS = "iss";
    public static final String JTI = "jti";
    public static final String CLIENT_NAME = "client_name";
    public static final String JWKS_URI = "jwks_uri";
    public static final String TOKEN_TYPE = "token_type_extension";
    public static final String APP_OWNER = "ext_application_owner";
    public static final String REQUIRE_SIGNED_OBJ = "require_signed_request_object";
    public static final String TLS_CLIENT_CERT_ACCESS_TOKENS = "tls_client_certificate_bound_access_tokens";
    public static final String TOKEN_EP_ALLOW_REUSE_PVT_KEY_JWT = "token_endpoint_allow_reuse_pvt_key_jwt";
    public static final String PRIVATE_KEY_JWT = "private_key_jwt";
    public static final String APP_DISPLAY_NAME = "ext_application_display_name";
    public static final String JWT = "JWT";
    public static final String SOFTWARE_STATEMENT = "software_statement";
    public static final String SOFTWARE_ID = "software_id";
    public static final String TOKEN_EP_AUTH_SIG_ALG = "token_endpoint_auth_signing_alg";
    public static final String GRANT_TYPES = "grant_types";
    public static final String APP_TYPE = "application_type";
    public static final String REDIRECT_URIS = "redirect_uris";
    public static final String TOKEN_EP_AUTH_METHOD = "token_endpoint_auth_method";
    public static final String SCOPE = "scope";
    public static final String REQ_OBJ_SIG_ALG = "request_object_signing_alg";
    public static final String RESPONSE_TYPES = "response_types";
    public static final String ID_TOKEN_RES_ALG = "id_token_signed_response_alg";
    public static final String DISALLOWED_CHARS_PATTERN = "([~!#$;%^&*+={}\\s\\|\\\\<>\\\"\\'\\/,\\]\\[\\(\\)])";
    public static final String SUBSTITUTE_STRING = "_";
    public static final int ABBREVIATED_STRING_LENGTH = 70;
    public static final String USERNAME = "userName";
    public static final String PASSWORD = "password";
    public static final String IAM_HOSTNAME = "IAM_Hostname";

    //DCR attributes
    public static final String REQUEST_PAYLOAD = "request_payload";
    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String CLIENT_ID_ISSUED_AT = "client_id_issued_at";
    public static final String INVALID_REQUEST = "invalid_request";
    public static final String INVALID_SSA = "invalid_software_statement";
}
