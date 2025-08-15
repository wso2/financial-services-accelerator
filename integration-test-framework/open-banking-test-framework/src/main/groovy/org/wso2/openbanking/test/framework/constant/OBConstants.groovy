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

package org.wso2.openbanking.test.framework.constant

/**
 * Common constant class for Open banking framework
 */
class OBConstants {

    // JWT Claim keys & payload constants
    public static final String ISSUER_KEY = "iss"
    public static final String SUBJECT_KEY = "sub"
    public static final String AUDIENCE_KEY = "aud"
    public static final String EXPIRE_DATE_KEY = "exp"
    public static final String ISSUED_AT_KEY = "iat"
    public static final String JTI_KEY = "jti"
    public static final String CLIENT_SECRET = "client_secret"
    public static final String AUTH_RESPONSE_TYPE_KEY = "response_type"
    public static final String STATE_KEY = "state"
    public static final String NONCE_KEY = "nonce"
    public static final String CLAIMS_KEY = "claims"
    public static final String ID_TOKEN_KEY = "id_token"
    public static final String ACR_KEY = "acr"
    public static final String ESSENTIAL_KEY = "claims"
    public static final String VALUES_KEY = "values"
    public static final String USERINFO_KEY = "userinfo"
    public static final String REQUEST_KEY = "request";
    public static final String INTEGRATION_DATE = "integrationDate"
    public static final String ALGORITHM_KEY = "alg"

    public static final String CODE_VERIFIER_KEY = "code_verifier"
    public static final String GRANT_TYPE_KEY = "grant_type"
    public static final String SCOPE_KEY = "scope"
    public static final String CLIENT_ASSERTION_TYPE_KEY = "client_assertion_type"
    public static final String CLIENT_ASSERTION_KEY = "client_assertion"
    public static final String REDIRECT_URI_KEY = "redirect_uri"
    public static final String CODE_KEY = "code"
    public static final String CLIENT_ID_KEY = "client_id"
    public static final String USER_NAME = "username"
    public static final String PASSWORD = "password"
    public static final String CLIENT_CREDENTIALS = "client_credentials"
    public static final String AUTH_CODE = "authorization_code"
    public static final String REFRESH_TOKEN = "refresh_token"
    public static final String PASSWORD_GRANT = "password"
    public static final String ACCESS_TOKEN = "access_token"
    public static final String TOKEN = "token"
    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json"
    public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data"
    public static final String AUTH_RESPONSE_TYPE = "code id_token"
    public static final ArrayList<String> ACCOUNTS_DEFAULT_SCOPES = new ArrayList<>(Arrays.asList("accounts", "openid"))
    public static final String AUTHORIZATION_HEADER_KEY = "Authorization"
    public static final String CLIENT_ASSERTION_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer"
    public static final String TLS_AUTH_METHOD = "tls_client_auth"
    public static final String ACCESS_TOKEN_CONTENT_TYPE = "application/x-www-form-urlencoded"
    public static final String X_FAPI_AUTH_DATE_HEADER_KEY = "x-fapi-auth-date"

    // DCR
    public static final String REDIRECT_URIS_KEY = "redirect_uris"
    public static final String TOKEN_ENDPOINT_AUTH_SIGNING_ALG_KEY = "token_endpoint_auth_signing_alg"
    public static final String TOKEN_ENDPOINT_AUTH_METHOD_KEY = "token_endpoint_auth_method"
    public static final String GRANT_TYPES_KEY = "grant_types"
    public static final String RESPONSE_TYPES_KEY = "response_types"
    public static final String APPLICATION_TYPE_KEY = "application_type"
    public static final String ID_TOKEN_SIGNED_RESPONSE_ALG_KEY = "id_token_signed_response_alg"
    public static final String ID_TOKEN_ENCRYPTED_RESPONSE_ALG_KEY = "id_token_encrypted_response_alg"
    public static final String ID_TOKEN_ENCRYPTED_RESPONSE_ENC_KEY = "id_token_encrypted_response_enc"
    public static final String REQUEST_OBJECT_SIGNING_ALG_KEY = "request_object_signing_alg"
    public static final String SOFTWARE_STATEMENT_KEY = "software_statement"

    //Rest Assured Request
    public static final String CONTENT_TYPE_APPLICATION_JWT = "application/jwt";

    // Algorithm
    public static final String ALGORITHM_SHA_1 = "SHA-1"
    public static final String ALGORITHM_SHA_256 = "SHA-256"

    // Endpoints
    public static final String TOKEN_ENDPOINT = "/oauth2/token";
    public static final String AUTHORIZE_ENDPOINT = "/authorize/?";
    public static final String INTROSPECTION_ENDPOINT = "/oauth2/introspect";
    public static final String OAUTH2_TOKEN_ENDPOINT = "/oauth2/token";
    public static final String REGISTER_SCOPE_ENDPOINT = "/api/identity/oauth2/v1.0/scopes";
    public static final String OAUTH2_REVOKE_ENDPOINT = "/oauth2/revoke";
    public static final String REST_API_CLIENT_REGISTRATION_ENDPOINT = "/client-registration/v0.16/register";
    public static final String REST_API_PUBLISHER_ENDPOINT = "/api/am/publisher/v1.1/apis/";
    public static final String REST_API_STORE_ENDPOINT = "/api/am/store/v1/";

    //HTTP Status Codes
    public static final int OK = 200
    public static final int BAD_REQUEST = 400
    public static final int FORBIDDEN = 403
    public static final int CREATED = 201
    public static final int UNAUTHORIZED = 401
    public static final int CONFLICT = 409
    public static final int NO_CONTENT = 204
    public static final int STATUS_CODE_200 = 200
    public static final int STATUS_CODE_201 = 201
    public static final int STATUS_CODE_400 = 400
    public static final int STATUS_CODE_401 = 401
    public static final int STATUS_CODE_404 = 404
    public static final int STATUS_CODE_405 = 405
    public static final int STATUS_CODE_406 = 406
    public static final int STATUS_CODE_422 = 422
    public static final int STATUS_CODE_409 = 409
    public static final int STATUS_CODE_204 = 204
    public static final int STATUS_CODE_403 = 403
    public static final int STATUS_CODE_500 = 500

    //Automation
    public static final String BROWSER_CHROME = "chrome"
    public static final String BROWSER_FIREFOX = "firefox"
    public static final String HEADLESS_TAG = "--headless"
    public static final String ERROR = "error"
    public static final String ERROR_DESCRIPTION = "error_description"
    public static final String OTP_CODE = "123456"

    //Configuration
    public static final String CARBON_HOME = "carbon.home"
    public static final String BASIC_HEADER_KEY = "Basic"
}

