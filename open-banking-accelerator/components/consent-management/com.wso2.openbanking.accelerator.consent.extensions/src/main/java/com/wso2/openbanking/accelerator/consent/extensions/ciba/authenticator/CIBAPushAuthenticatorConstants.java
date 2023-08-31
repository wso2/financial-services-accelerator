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

package com.wso2.openbanking.accelerator.consent.extensions.ciba.authenticator;

/**
 * CIBA Push Authenticator Constants.
 */
public class CIBAPushAuthenticatorConstants {

    public static final String AUTHENTICATOR_NAME = "ciba";
    public static final String AUTHENTICATOR_FRIENDLY_NAME = "CIBA Authenticator";
    public static final String REQUEST = "request";
    public static final String REQUEST_OBJECT = "request_object";
    public static final String BINDING_MESSAGE = "binding_message";

    //Consent Related constants
    public static final String LOGGED_IN_USER = "loggedInUser";
    public static final String USER_TENANT_DOMAIN = "userTenantDomain";
    public static final String SCOPE = "scope";
    public static final String APPLICATION = "application";
    public static final String CONSENT_PROMPTED = "consentPrompted";
    public static final String AUTH_REQ_ID = "auth_req_id";
    public static final String NONCE = "nonce";
    public static final String LOGIN_HINT = "login_hint";
    public static final String SP_QUERY_PARAMS = "spQueryParams";

    // error constants
    public static final String IS_ERROR = "isError";
    public static final String ERROR_SERVER_ERROR = "Internal server error";
    public static final String ERROR_NO_TYPE_AND_APP_DATA = "Type and application data is unavailable";
    public static final String ERROR_DESCRIPTION = "error_description";
    public static final String ERROR = "error";
}
