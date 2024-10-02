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
}
