/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.apim.mediation.policies.dynamic.client.registration.util;

/**
 * Exception class for handling errors in Dynamic Client Registration (DCR) operations.
 */
public class DCRHandlingException extends Exception {

    private final String errorCode;
    private final String httpCode;

    public DCRHandlingException(String errorCode, String message, String httpCode) {
        super(message);
        this.errorCode = errorCode;
        this.httpCode = httpCode;
    }

    public DCRHandlingException(String errorCode, String message, String httpCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpCode = httpCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getHttpCode() {
        return httpCode;
    }
}
