/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.consent.mgt.api.dao.exceptions;

import org.wso2.financial.services.accelerator.consent.mgt.api.dao.constants.ConsentError;

/**
 * Used for creating runtime exceptions for financial services modules.
 */
public class ConsentMgtException extends Exception {

    private static final long serialVersionUID = -5686395831712095972L;
    private ConsentError error;

    public ConsentMgtException(String message) {

        super(message);
    }

    public ConsentMgtException(ConsentError e) {
        this.error = e;
    }

    public ConsentMgtException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConsentError getError() {

        return error;
    }



}
