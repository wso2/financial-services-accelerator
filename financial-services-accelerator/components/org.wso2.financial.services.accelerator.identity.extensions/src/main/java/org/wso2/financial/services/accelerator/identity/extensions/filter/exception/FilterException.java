/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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
package org.wso2.financial.services.accelerator.identity.extensions.filter.exception;

import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;

/**
 * CDS filter exception.
 */
public class FilterException extends FinancialServicesException {

    private String errorDescription;
    private int errorCode;

    public FilterException(int errorCode, String error, String errorDescription, Throwable e) {

        super(error, e);
        this.errorDescription = errorDescription;
        this.errorCode = errorCode;
    }

    public FilterException(int errorCode, String error, String errorDescription) {

        super(error);
        this.errorDescription = errorDescription;
        this.errorCode = errorCode;
    }

    public FilterException(String message, Throwable e) {

        super(message, e);
    }

    public String getErrorDescription() {

        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {

        this.errorDescription = errorDescription;
    }

    public int getErrorCode() {

        return errorCode;
    }

    public void setErrorCode(int errorCode) {

        this.errorCode = errorCode;
    }
}
