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

/**
 * Used for creating runtime exceptions for Open-banking modules.
 */
public class OpenBankingRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -5686395831712095972L;
    private String errorCode;

    public OpenBankingRuntimeException(String errorCode, Throwable cause) {

        super(cause);
        this.errorCode = errorCode;
    }

    public OpenBankingRuntimeException(String errorCode) {
        super();
        this.errorCode = errorCode;
    }

    public String getErrorCode() {

        return errorCode;
    }

    public void setErrorCode(String errorCode) {

        this.errorCode = errorCode;
    }

}
