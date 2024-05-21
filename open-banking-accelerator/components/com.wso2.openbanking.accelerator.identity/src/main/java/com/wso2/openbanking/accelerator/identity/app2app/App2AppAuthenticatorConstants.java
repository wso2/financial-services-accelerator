/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.openbanking.accelerator.identity.app2app;

/**
 * Constants related with App2App Authenticator process.
 */
public class App2AppAuthenticatorConstants {

    public static final String AUTHENTICATOR_NAME = "app2app";
    public static final String AUTHENTICATOR_FRIENDLY_NAME = "App2App Authenticator";
    public static final String REQUEST = "request";
    public static final String DEVICE_VERIFICATION_TOKEN_IDENTIFIER = "deviceVerificationToken";
    public static final String SESSION_DATA_KEY = "sessionDataKey";
    public static final String APP_AUTH_IDENTIFIER_VALIDATION_EXCEPTION_MESSAGE
            = "Error while validating App Auth Identifier.";
    public static final String ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE
            = "Error while creating user for provided loginHint.";
    public static final String PARSE_EXCEPTION_MESSAGE = "Error while parsing the provided JWT.";
    public static final String PUSH_DEVICE_HANDLER_SERVER_EXCEPTION_MESSAGE
            = "Error occurred in push device handler service: ";
    public static final String USER_STORE_EXCEPTION_MESSAGE = "Error while creating authenticated user.";
    public static final String PUSH_DEVICE_HANDLER_CLIENT_EXCEPTION_MESSAGE
            = "Error occurred in Push Device handler client.";
    public static final String INITIALIZATION_ERROR_MESSAGE = "Initializing App2App authenticator is not supported.";
    public static final String DEVICE_VERIFICATION_TOKEN_MISSING_ERROR_MESSAGE
            = "Device verification token null or empty in request.";
    public static final String USER_AUTHENTICATED_MSG
            = "User {%s} authenticated by app2app authenticator successfully.";
    public static final String OPEN_BANKING_EXCEPTION_MESSAGE
            = "Error while retrieving user.";
    public static final String REQUIRED_PARAMS_MISSING_MESSAGE
            = "Required Parameters did or loginHint null or empty.";
}

