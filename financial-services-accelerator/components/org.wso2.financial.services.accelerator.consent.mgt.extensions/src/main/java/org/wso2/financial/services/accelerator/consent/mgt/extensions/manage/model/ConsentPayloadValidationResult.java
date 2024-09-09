/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model;

import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;

/**
 * Data wrapper for the result of consent payload validation.
 */
public class ConsentPayloadValidationResult {

    boolean isValid;
    ResponseStatus httpCode;
    String errorCode;
    String errorMessage;

    public ConsentPayloadValidationResult(boolean isValid) {
        this.isValid = isValid;
    }

    public ConsentPayloadValidationResult(boolean isValid, ResponseStatus httpCode, String errorCode,
                                          String errorMessage) {
        this.isValid = isValid;
        this.httpCode = httpCode;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public ResponseStatus getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(ResponseStatus httpCode) {
        this.httpCode = httpCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
