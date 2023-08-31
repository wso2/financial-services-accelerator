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

package com.wso2.openbanking.accelerator.consent.endpoint.util;

/**
 * Constant class for consent authorize endpoints.
 */
public class ConsentConstants {

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String DEFAULT_RESPONSE_CONTENT_TYPE = APPLICATION_JSON;

    public static final String ERROR_PAYLOAD_READ = "Error while reading payload";
    public static final String ERROR_PAYLOAD_PARSE = "Error while parsing payload";
    public static final String RESOURCE_PATH = "ResourcePath";
    public static final String HTTP_METHOD = "HttpMethod";
    public static final String RESOURCE_CONTEXT = "ResourceContext";
    public static final String PRESERVE_CONSENT = "Consent.PreserveConsentLink";
    public static final String SENSITIVE_DATA_MAP = "sensitiveDataMap";
    public static final String LOGGED_IN_USER = "loggedInUser";
    public static final String SP_QUERY_PARAMS = "spQueryParams";
    public static final String SCOPES = "scopeString";
    public static final String APPLICATION = "application";
    public static final String REQUEST_HEADERS = "requestHeaders";
    public static final String REQUEST_URI = "redirectURI";
    public static final String USERID = "userId";
    public static final String CONSENT_ID = "consentId";
    public static final String CLIENT_ID = "clientId";
    public static final String REGULATORY = "regulatory";
    public static final String CONSENT_RESOURCE = "consentResource";
    public static final String AUTH_RESOURCE = "authResource";
    public static final String META_DATA = "metaDataMap";
    public static final String TYPE = "type";

}
