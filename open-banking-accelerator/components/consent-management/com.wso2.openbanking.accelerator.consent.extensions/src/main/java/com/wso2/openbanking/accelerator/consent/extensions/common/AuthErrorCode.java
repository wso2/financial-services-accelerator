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

package com.wso2.openbanking.accelerator.consent.extensions.common;

/**
 * Enum of the error redirect codes from both OAuth 2.0 and OIDC Core 1.0 specifications.
 */
public enum AuthErrorCode {

    /**
     * invalid_request, see {@link <a href="https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2.1">
     * OAuth 2.0</a>}.
     */
    INVALID_REQUEST("invalid_request"),
    /**
     * unauthorized_client, see {@link <a href="https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2.1">
     * OAuth 2.0</a>}.
     */
    UNAUTHORIZED_CLIENT("unauthorized_client"),
    /**
     * access_denied, see {@link <a href="https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2.1">
     * OAuth 2.0</a>}.
     */
    ACCESS_DENIED("access_denied"),
    /**
     * unsupported_response_type, see {@link <a href="https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2.1">
     * OAuth 2.0</a>}.
     */
    UNSUPPORTED_RESPONSE_TYPE("unsupported_response_type"),
    /**
     * invalid_scope, see {@link <a href="https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2.1">
     * OAuth 2.0</a>}.
     */
    INVALID_SCOPE("invalid_scope"),
    /**
     * server_error, see {@link <a href="https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2.1">
     * OAuth 2.0</a>}.
     */
    SERVER_ERROR("server_error"),
    /**
     * temporarily_unavailable, see {@link <a href="https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2.1">
     * OAuth 2.0</a>}.
     */
    TEMPORARILY_UNAVAILABLE("temporarily_unavailable"),
    /**
     * interaction_required, see {@link <a href="https://openid.net/specs/openid-connect-core-1_0.html#AuthError">
     * OpenID Connect Core 1.0</a>}.
     */
    INTERACTION_REQUIRED("interaction_required"),
    /**
     * login_required, see {@link <a href="https://openid.net/specs/openid-connect-core-1_0.html#AuthError">
     * OpenID Connect Core 1.0</a>}.
     */
    LOGIN_REQUIRED("login_required"),
    /**
     * account_selection_required, see {@link <a href="https://openid.net/specs/openid-connect-core-1_0.html#AuthError">
     * OpenID Connect Core 1.0</a>}.
     */
    ACCOUNT_SELECTION_REQUIRED("account_selection_required"),
    /**
     * consent_required, see {@link <a href="https://openid.net/specs/openid-connect-core-1_0.html#AuthError">
     * OpenID Connect Core 1.0</a>}.
     */
    CONSENT_REQUIRED("consent_required"),
    /**
     * invalid_request_uri, see {@link <a href="https://openid.net/specs/openid-connect-core-1_0.html#AuthError">
     * OpenID Connect Core 1.0</a>}.
     */
    INVALID_REQUEST_URI("invalid_request_uri"),
    /**
     * invalid_request_object, see {@link <a href="https://openid.net/specs/openid-connect-core-1_0.html#AuthError">
     * OpenID Connect Core 1.0</a>}.
     */
    INVALID_REQUEST_OBJECT("invalid_request_object"),
    /**
     * request_not_supported, see {@link <a href="https://openid.net/specs/openid-connect-core-1_0.html#AuthError">
     * OpenID Connect Core 1.0</a>}.
     */
    REQUEST_NOT_SUPPORTED("request_not_supported"),
    /**
     * request_uri_not_supported, see {@link <a href="https://openid.net/specs/openid-connect-core-1_0.html#AuthError">
     * OpenID Connect Core 1.0</a>}.
     */
    REQUEST_URI_NOT_SUPPORTED("request_uri_not_supported"),
    /**
     * registration_not_supported, see {@link <a href="https://openid.net/specs/openid-connect-core-1_0.html#AuthError">
     * OpenID Connect Core 1.0</a>}.
     */
    REGISTRATION_NOT_SUPPORTED("registration_not_supported");

    private final String errorCode;

    AuthErrorCode(String errorCode) {

        this.errorCode = errorCode;
    }

    @Override
    public String toString() {

        return errorCode;
    }
}
