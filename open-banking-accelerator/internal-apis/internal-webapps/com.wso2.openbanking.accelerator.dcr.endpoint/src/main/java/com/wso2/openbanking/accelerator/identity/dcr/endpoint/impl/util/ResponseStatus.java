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

package com.wso2.openbanking.accelerator.identity.dcr.endpoint.impl.util;

/**
 * Enum of the supported response status in accelerator.
 */
public enum ResponseStatus {

    /**
     * 200 OK, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.1">
     *     HTTP/1.1 documentation</a>}.
     */
    OK(200, "OK"),
    /**
     * 201 Created, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.2">
     *     HTTP/1.1 documentation</a>}.
     */
    CREATED(201, "Created"),
    /**
     * 202 Accepted, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.3">
     *     HTTP/1.1 documentation</a>}.
     */
    ACCEPTED(202, "Accepted"),
    /**
     * 204 No Content, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.5">
     *     HTTP/1.1 documentation</a>}.
     */
    NO_CONTENT(204, "No Content"),
    /**
     * 205 Reset Content, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.6">
     *     HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    RESET_CONTENT(205, "Reset Content"),
    /**
     * 206 Reset Content, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.7">
     *     HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    PARTIAL_CONTENT(206, "Partial Content"),
    /**
     * 301 Moved Permanently, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.2">
     *     HTTP/1.1 documentation</a>}.
     */
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    /**
     * 302 Found, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.3">
     *     HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    FOUND(302, "Found"),
    /**
     * 303 See Other, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.4">
     *     HTTP/1.1 documentation</a>}.
     */
    SEE_OTHER(303, "See Other"),
    /**
     * 304 Not Modified, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.5">
     *     HTTP/1.1 documentation</a>}.
     */
    NOT_MODIFIED(304, "Not Modified"),
    /**
     * 305 Use Proxy, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.6">
     *     HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    USE_PROXY(305, "Use Proxy"),
    /**
     * 307 Temporary Redirect, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.8">
     *     HTTP/1.1 documentation</a>}.
     */
    TEMPORARY_REDIRECT(307, "Temporary Redirect"),
    /**
     * 400 Bad Request, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.1">
     *     HTTP/1.1 documentation</a>}.
     */
    BAD_REQUEST(400, "Bad Request"),
    /**
     * 401 Unauthorized, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.2">
     *     HTTP/1.1 documentation</a>}.
     */
    UNAUTHORIZED(401, "Unauthorized"),
    /**
     * 402 Payment Required, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.3">
     *     HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    PAYMENT_REQUIRED(402, "Payment Required"),
    /**
     * 403 Forbidden, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.4">
     *     HTTP/1.1 documentation</a>}.
     */
    FORBIDDEN(403, "Forbidden"),
    /**
     * 404 Not Found, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.5">
     *     HTTP/1.1 documentation</a>}.
     */
    NOT_FOUND(404, "Not Found"),
    /**
     * 405 Method Not Allowed, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.6">
     *     HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    /**
     * 406 Not Acceptable, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.7">
     *     HTTP/1.1 documentation</a>}.
     */
    NOT_ACCEPTABLE(406, "Not Acceptable"),
    /**
     * 409 Conflict, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.10">
     *     HTTP/1.1 documentation</a>}.
     */
    CONFLICT(409, "Conflict"),
    /**
     * 410 Gone, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.11">
     *     HTTP/1.1 documentation</a>}.
     */
    GONE(410, "Gone"),
    /**
     * 411 Length Required, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.12">
     *     HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    LENGTH_REQUIRED(411, "Length Required"),
    /**
     * 412 Precondition Failed, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.13">
     *     HTTP/1.1 documentation</a>}.
     */
    PRECONDITION_FAILED(412, "Precondition Failed"),
    /**
     * 413 Request Entity Too Large, see {@link <a
     * href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.14">HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    REQUEST_ENTITY_TOO_LARGE(413, "Request Entity Too Large"),
    /**
     * 414 Request-URI Too Long, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.15">
     *     HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    REQUEST_URI_TOO_LONG(414, "Request-URI Too Long"),
    /**
     * 415 Unsupported Media Type, see {@link <a
     * href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.16">HTTP/1.1 documentation</a>}.
     */
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    /**
     * 416 Requested Range Not Satisfiable, see {@link <a
     * href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.17">HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),
    /**
     * 417 Expectation Failed, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.18">
     *     HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    EXPECTATION_FAILED(417, "Expectation Failed"),
    /**
     * 500 Internal Server Error, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.1">
     *     HTTP/1.1 documentation</a>}.
     */
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    /**
     * 501 Not Implemented, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.2">
     *     HTTP/1.1 documentation</a>}.
     *
     * @since 2.0
     */
    NOT_IMPLEMENTED(501, "Not Implemented"),
    /**
     * 503 Service Unavailable, see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.4">
     *     HTTP/1.1 documentation</a>}.
     */
    SERVICE_UNAVAILABLE(503, "Service Unavailable");

    private final int code;
    private final String reason;

    ResponseStatus(final int statusCode, final String reasonPhrase) {
        this.code = statusCode;
        this.reason = reasonPhrase;
    }

    /**
     * Get the associated status code.
     *
     * @return the status code.
     */
    public int getStatusCode() {
        return code;
    }

    /**
     * Get the reason phrase.
     *
     * @return the reason phrase.
     */
    public String getReasonPhrase() {
        return toString();
    }

    /**
     * Get the reason phrase.
     *
     * @return the reason phrase.
     */
    @Override
    public String toString() {
        return reason;
    }

    /**
     * Convert a numerical status code into the corresponding Status.
     *
     * @param statusCode the numerical status code.
     * @return the matching Status or null is no matching Status is defined.
     */
    public static ResponseStatus fromStatusCode(final int statusCode) {
        for (ResponseStatus s : ResponseStatus.values()) {
            if (s.code == statusCode) {
                return s;
            }
        }
        return null;
    }
}
