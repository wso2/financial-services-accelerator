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

package org.wso2.financial.services.accelerator.gateway.executor.exception;

/**
 * Financial Services executor exception class.
 */
public class FSExecutorException extends Exception {

    private String errorCode;
    private String errorPayload;

    public FSExecutorException(String message, String errorCode, String errorPayload) {

        super(message);
        this.errorCode = errorCode;
        this.errorPayload = errorPayload;
    }

    public FSExecutorException(String message, Throwable cause) {

        super(message, cause);
    }

    public FSExecutorException(String message) {
        super(message);
    }

    public FSExecutorException(Throwable cause, String errorCode, String errorPayload) {

        super(cause);
        this.errorCode = errorCode;
        this.errorPayload = errorPayload;
    }

    public FSExecutorException(String message, Throwable cause, String errorCode, String errorPayload) {

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
