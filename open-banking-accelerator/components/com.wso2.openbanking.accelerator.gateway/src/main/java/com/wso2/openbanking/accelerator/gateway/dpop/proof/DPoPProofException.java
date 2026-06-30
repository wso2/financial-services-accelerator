/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
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

package com.wso2.openbanking.accelerator.gateway.dpop.proof;

/**
 * Signals a DPoP proof validation failure per RFC 9449. Carries the RFC-defined
 * error code used in the {@code WWW-Authenticate} challenge sent back to clients.
 */
public class DPoPProofException extends Exception {

    private static final long serialVersionUID = 8473629184756L;

    /**
     * RFC 9449 §7.1 error codes returned in the challenge.
     */
    public enum ErrorCode {

        /**
         * The DPoP proof JWT itself failed validation (§4.3).
         */
        INVALID_DPOP_PROOF("invalid_dpop_proof"),

        /**
         * The access token is invalid or the DPoP binding check failed.
         */
        INVALID_TOKEN("invalid_token"),

        /**
         * The server requires a DPoP-Nonce and none was supplied or it was stale.
         */
        USE_DPOP_NONCE("use_dpop_nonce");

        private final String value;

        ErrorCode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private final ErrorCode errorCode;

    public DPoPProofException(ErrorCode errorCode, String message) {

        super(message);
        this.errorCode = errorCode;
    }

    public DPoPProofException(ErrorCode errorCode, String message, Throwable cause) {

        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {

        return errorCode;
    }
}
