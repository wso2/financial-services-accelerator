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

package com.wso2.openbanking.accelerator.ciba.authentication.endpoint.impl.api;

/**
 * Constants for CIBA authentication endpoint.
 */
public class CIBAAuthenticationEndpointConstants {

    // request related constants
    public static final String AUTH_RESPONSE = "authResponse";
    public static final String TOKEN_DEVICE_ID = "did";
    public static final String TOKEN_SESSION_DATA_KEY = "sid";
    public static final String CONTEXT_AUTH_DATA = "authData";
    public static final String TOKEN_RESPONSE = "res";
    public static final String METADATA = "mta";
    public static final String AUTH_REQUEST_STATUS_SUCCESS = "SUCCESSFUL";
    public static final String AUTH_REQUEST_STATUS_DENIED = "DENIED";

    // device registration related constants
    public static final String DEVICE_REGISTRATION_URL = "/api/users/v1/me/push-auth/discovery-data";
    public static final String AUTHENTICATION_ENDPOINT_URL_PREFIX = "/api/openbanking/ciba";
    public static final String AUTHENTICATION_ENDPOINT = "ae";
    public static final String AUTH_HEADER_NAME = "Authorization";

    // consent related constants
    public static final String APPROVAL = "approval";
    public static final String AUTHORIZE = "authorize";
    public static final String ACCOUNT_IDS = "accountIds";
    public static final String METADATA_ACCOUNT_IDS = "approvedAccountIds";

    /**
     * Enum which contains error codes and corresponding error messages.
     */
    public enum ErrorMessages {

        ERROR_CODE_AUTH_RESPONSE_TOKEN_NOT_FOUND(
                "PBA-15001",
                "The request did not contain an authentication response token"
        ),
        ERROR_CODE_SESSION_DATA_KEY_NOT_FOUND(
                "PBA-15002",
                "Session data key is not present in the authentication response token received from device: "
        ),
        ERROR_CODE_GET_DEVICE_ID_FAILED(
                "PBA-15003",
                "Error occurred when extracting the auth response token."
        ),
        ERROR_CODE_GET_PUBLIC_KEY_FAILED(
                "PBA-15004",
                "Error occurred when trying to get the public key from device: "
        ),
        ERROR_CODE_TOKEN_VALIDATION_FAILED(
                "PBA-15005",
                "Error occurred when validating auth response token from device: "
        ),
        ERROR_CODE_PARSE_JWT_FAILED(
                "PBA-15006",
                "Error occurred when parsing auth response token to JWT."
        ),
        ERROR_PERSIST_INVALID_AUTHORIZE(
            "400", "Invalid value for authorize. Should be true/false"
        ),
        ERROR_PERSIST_APPROVAL_MANDATORY(
                "400", "Mandatory body parameter approval is unavailable"
        ),
        ERROR_CODE_SERVER_ERROR(
                "500", "internal server error"
        ),
        ERROR_CODE_BAD_REQUEST(
                "400", "Bad Request"
        ),
        ERROR_CODE_UNAUTHORIZED(
                "401", "Unauthorized"
        );

        private final String code;
        private final String message;

        ErrorMessages(String code, String message) {

            this.code = code;
            this.message = message;
        }

        public String getCode() {

            return code;
        }

        public String getMessage() {

            return message;
        }

        @Override
        public String toString() {

            return code + " - " + message;
        }
    }
}
