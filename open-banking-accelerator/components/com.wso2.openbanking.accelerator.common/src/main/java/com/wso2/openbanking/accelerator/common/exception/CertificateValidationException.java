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

package com.wso2.openbanking.accelerator.common.exception;

import com.wso2.openbanking.accelerator.common.error.OpenBankingErrorCodes;

/**
 * Certificate Validation exception class.
 */
public class CertificateValidationException extends Exception {

    private String errorCode;
    private String errorPayload;

    public CertificateValidationException(String message) {

        super(message);
        this.errorCode = OpenBankingErrorCodes.INVALID_MTLS_CERT_CODE;
        this.errorPayload = "";
    }

    public CertificateValidationException(String message, String errorCode, String errorPayload) {

        super(message);
        this.errorCode = errorCode;
        this.errorPayload = errorPayload;
    }

    public CertificateValidationException(String message, Throwable cause) {

        super(message, cause);
    }

    public CertificateValidationException(Throwable cause, String errorCode, String errorPayload) {

        super(cause);
        this.errorCode = errorCode;
        this.errorPayload = errorPayload;
    }

    public CertificateValidationException(String message, Throwable cause, String errorCode, String errorPayload) {

        super(message, cause);
        this.errorCode = errorCode;
        this.errorPayload = errorPayload;
    }

    public String getErrorCode() {

        return errorCode;
    }

    public void setErrorCode(String errorCode) {

        this.errorCode = errorCode;
    }

    public String getErrorPayload() {

        return errorPayload;
    }

    public void setErrorPayload(String errorPayload) {

        this.errorPayload = errorPayload;
    }
}
