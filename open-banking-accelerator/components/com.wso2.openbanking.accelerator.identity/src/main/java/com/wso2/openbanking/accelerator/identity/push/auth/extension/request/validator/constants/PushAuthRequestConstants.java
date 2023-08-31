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

package com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.constants;

/**
 * Constant class for push auth request.
 */
public class PushAuthRequestConstants {

    // PAR request parameters
    public static final String REQUEST = "request";
    public static final String REQUEST_URI = "request_uri";
    public static final String RESPONSE_TYPE = "response_type";
    public static final String NONCE = "nonce";
    public static final String SCOPE = "scope";
    public static final String CODE_CHALLENGE = "code_challenge";
    public static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
    public static final String CLIENT_ID = "client_id";
    public static final String REDIRECT_URI = "redirect_uri";

    // error constants
    public static final String SERVER_ERROR = "server_error";
    public static final String INVALID_REQUEST = "invalid_request";
    public static final String INVALID_REQUEST_OBJECT = "invalid_request_object";

    // custom jwt body constants
    public static final String DECODED_JWT_BODY = "decodedJWTBody";
    public static final String DECODED_JWT_HEADER = "decodedJWTHeader";

    // jwt constants
    public static final String ALG_HEADER = "alg";
    public static final String AUDIENCE = "aud";
    public static final String ISSUER = "iss";
    public static final String EXPIRY = "exp";
    public static final long ONE_HOUR_IN_MILLIS = 3600000;
    public static final String NOT_BEFORE = "nbf";
    public static final String ALG_HEADER_NONE = "none";

    // jwt parts
    public static final String BODY = "body";
    public static final String HEADER = "head";
}
