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

package com.wso2.openbanking.accelerator.gateway.util;

/**
 * Gateway common constants class.
 */
public class GatewayConstants {

    public static final String CONTENT_TYPE_TAG = "Content-Type";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String JWT_CONTENT_TYPE = "application/jwt";
    public static final String JSON_CONTENT_TYPE = "application/json";
    public static final String JOSE_CONTENT_TYPE = "application/jose";
    public static final String APPLICATION_XML_CONTENT_TYPE = "application/xml";
    public static final String TEXT_XML_CONTENT_TYPE = "text/xml";
    public static final String GET_HTTP_METHOD = "GET";
    public static final String POST_HTTP_METHOD = "POST";
    public static final String PUT_HTTP_METHOD = "PUT";
    public static final String PATCH_HTTP_METHOD = "PATCH";
    public static final String DELETE_HTTP_METHOD = "DELETE";
    public static final String ACCEPT = "Accept";
    public static final String AUTH_HEADER = "Authorization";
    public static final String BASIC_TAG = "Basic ";
    public static final String BEARER_TAG = "Bearer ";
    public static final String PUBLISHER_API_PATH = "api/am/publisher/apis/";
    public static final String SWAGGER_ENDPOINT = "/swagger";
    public static final String REGULATORY_CUSTOM_PROP = "x-wso2-regulatory-api";
    public static final String API_TYPE_CUSTOM_PROP = "x-wso2-api-type";
    public static final String IS_RETURN_RESPONSE = "isReturnResponse";
    public static final String MODIFIED_STATUS = "ModifiedStatus";
    public static final String APPLICATION = "application";
    public static final String APPLICATION_USER = "application_user";

    //dcr related configs
    public static final String AM_APP_NAME_CACHEKEY = "APP_NAME";
    public static final String APP_CREATE_URL = "APP_CREATION_URL";
    public static final String KEY_MAP_URL = "KEY_MAPPING_URL";
    public static final String API_RETRIEVE_URL = "API_RETRIEVAL_URL";
    public static final String API_SUBSCRIBE_URL = "API_SUBSCRIBE_URL";
    public static final String API_GET_SUBSCRIBED = "API_GET_SUBSCRIPTIONS";
    public static final String TOKEN_URL = "TOKEN_URL";
    public static final String USERNAME = "userName";
    public static final String IAM_DCR_URL = "DCR_Endpoint";
    public static final String PASSWORD = "password";
    public static final String IAM_HOSTNAME = "IAM_Hostname";
    public static final String VALIDATE_JWT = "DCR.RequestJWTValidation";


    //Config elements
    public static final String CONSENT_VALIDATION_ENDPOINT_TAG = "Gateway.ConsentValidationEndpoint";
    public static final String KEYSTORE_LOCATION_TAG = "Security.InternalKeyStore.Location";
    public static final String KEYSTORE_PASSWORD_TAG = "Security.InternalKeyStore.Password";
    public static final String SIGNING_ALIAS_TAG = "Security.InternalKeyStore.KeyAlias";
    public static final String SIGNING_KEY_PASSWORD = "Security.InternalKeyStore.KeyPassword";
    public static final String API_KEY_VALIDATOR_USERNAME = "APIKeyValidator.Username";
    public static final String API_KEY_VALIDATOR_PASSWORD = "APIKeyValidator.Password";
    public static final String PUBLISHER_HOSTNAME = "PublisherURL";
    public static final String CONTEXT_PROP_CACHE_KEY = "_contextProp";
    public static final String ANALYTICS_PROP_CACHE_KEY = "_analyticsData";
    public static final String API_DATA_STREAM = "APIInputStream";
    public static final String API_DATA_VERSION = "1.0.0";
    public static final String ERROR_STATUS_PROP = "errorStatusCode";
    public static final String CONSENT_ID_CLAIM_NAME = "Identity.ConsentIDClaimName";
    public static final String REQUEST_ROUTER = "Gateway.RequestRouter";
    public static final String GATEWAY_CACHE_EXPIRY = "Gateway.Cache.GatewayCache.CacheAccessExpiry";
    public static final String GATEWAY_CACHE_MODIFIEDEXPIRY = "Gateway.Cache.GatewayCache.CacheModifiedExpiry";
    public static final String GATEWAY_THROTTLE_DATAPUBLISHER = "Gateway.CustomThrottleDataPublisher";

    public static final String CUSTOMER_CARE_OFFICER_SCOPE = "consents:read_all";

    public static final String API_TYPE_CONSENT = "consent";
    public static final String API_TYPE_NON_REGULATORY = "non-regulatory";

    // Idempotency
    public static final String REQUEST_CACHE_KEY = "Request";
    public static final String CREATED_TIME_CACHE_KEY = "Created_Time";
    public static final String RESPONSE_CACHE_KEY = "Response";
    public static final String TRUE = "true";
    public static final String IDEMPOTENCY_KEY_CACHE_KEY = "Idempotency_Key";

    // Error constants
    public static final String INVALID_CLIENT = "invalid_client";
    public static final String CLIENT_CERTIFICATE_MISSING = "Invalid mutual TLS request. Client certificate is missing";
    public static final String CLIENT_CERTIFICATE_INVALID = "Invalid mutual TLS request. Client certificate is invalid";
    public static final String INVALID_GRANT_TYPE = "Access failure for API: grant type validation failed.";
    public static final String INVALID_CREDENTIALS = "Invalid Credentials. Make sure you have provided the " +
            "correct security credentials ";
    public static final String MISSING_CREDENTIALS = "Invalid Credentials. Make sure your API invocation call " +
            "has a header - 'Authorization'";

    // Error codes
    public static final int API_AUTH_INVALID_CREDENTIALS = 900901;
    public static final int API_AUTH_MISSING_CREDENTIALS = 900902;

    // Oauth2 constants
    public static final String AUTHORIZED_USER_TYPE_CLAIM_NAME = "aut";
    public static final String DEFAULT = "default";
    public static final String OPENID = "openid";

    // HTTP methods
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String PATCH = "PATCH";
    public static final String DELETE = "DELETE";

    // Grant types
    public static final String CLIENT_CREDENTIALS = "client_credentials";
    public static final String AUTHORIZATION_CODE = "authorization_code";
    public static final String IMPLICIT = "Implicit";
    public static final String PASSWORD_GRANT = "Password";

    //Dispute resolution constants
    public static final String UNKNOWN = "Unknown";
    public static final String ERROR_CODE = "ERROR_CODE";
    public static final String HTTP_RESPONSE_STATUS_CODE = "HTTP_RESPONSE_STATUS_CODE";
    public static final String CUSTOM_HTTP_SC = "CUSTOM_HTTP_SC";
    public static final String HTTP_SC = "HTTP_SC";
    public static final String HTTP_METHOD = "api.ut.HTTP_METHOD";
    public static final String API_BODY = "API";

}
