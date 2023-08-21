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
package com.wso2.openbanking.accelerator.identity.authenticator.constants;

/**
 * Constants used by the OBIdentifierAuthenticator.
 */
public class IdentifierHandlerConstants {

    public static final String CONTEXT_PROP_INVALID_EMAIL_USERNAME = "InvalidEmailUsername";
    public static final String HANDLER_NAME = "IdentifierExecutor";
    public static final String HANDLER_FRIENDLY_NAME = "ob-identifier-first";
    public static final String USER_NAME = "username";
    public static final String FAILED_USERNAME = "&failedUsername=";
    public static final String ERROR_CODE = "&errorCode=";
    public static final String AUTHENTICATORS = "&authenticators=";
    public static final String LOCAL = "LOCAL";
    public static final String UTF_8 = "UTF-8";

    //auth request params
    public static final String AUTH_REQ_URL = "authRequestURL";
    public static final String ACCEPT_HEADER = "accept";
    public static final String ACCEPT_HEADER_VALUE = "application/json";
    public static final String AUTH_HEADER = "Authorization";
}
