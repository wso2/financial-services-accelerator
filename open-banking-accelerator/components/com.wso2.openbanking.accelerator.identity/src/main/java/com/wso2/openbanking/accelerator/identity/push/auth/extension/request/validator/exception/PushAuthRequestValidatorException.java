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

package com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.exception;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;

/**
 * PAR validation exception.
 */
public class PushAuthRequestValidatorException extends OpenBankingException {

    private String errorDescription;
    private String errorCode;
    private int httpStatusCode;

    public int getHttpStatusCode() {

        return httpStatusCode;
    }

    public void setHttpStatusCode(int httpStatusCode) {

        this.httpStatusCode = httpStatusCode;
    }

    public String getErrorDescription() {

        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {

        this.errorDescription = errorDescription;
    }

    public String getErrorCode() {

        return errorCode;
    }

    public void setErrorCode(String errorCode) {

        this.errorCode = errorCode;
    }

    public PushAuthRequestValidatorException(int httpStatusCode, String errorCode, String errorDescription,
                                             Throwable e) {

        super(errorDescription, e);
        this.errorDescription = errorDescription;
        this.errorCode = errorCode;
        this.httpStatusCode = httpStatusCode;

    }

    public PushAuthRequestValidatorException(int httpStatusCode, String  errorCode, String errorDescription) {

        super(errorDescription);
        this.errorDescription = errorDescription;
        this.errorCode = errorCode;
        this.httpStatusCode = httpStatusCode;
    }
}
