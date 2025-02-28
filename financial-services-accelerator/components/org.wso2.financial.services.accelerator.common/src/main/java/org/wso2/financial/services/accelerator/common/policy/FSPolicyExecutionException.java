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

package org.wso2.financial.services.accelerator.common.policy;

import javax.servlet.http.HttpServletResponse;

/**
 * Exception for FS Policy Execution Errors.
 */
public class FSPolicyExecutionException extends Exception {

    private int statusCode;
    private String errorCode;

    public FSPolicyExecutionException(int statusCode, String errorCode, String errorMessage,
                                      Throwable cause) {

        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

    public FSPolicyExecutionException(String errorMessage, String errorCode, Throwable cause) {

        super(errorMessage, cause);
        this.statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        this.errorCode = errorCode;
    }

    public FSPolicyExecutionException(int statusCode, String errorCode, String errorMessage) {

        super(errorMessage);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public int getStatusCode() {

        return statusCode;
    }

    public void setStatusCode(int statusCode) {

        this.statusCode = statusCode;
    }

    public String getErrorCode() {

        return errorCode;
    }

    public void setErrorCode(String errorCode) {

        this.errorCode = errorCode;
    }
}
