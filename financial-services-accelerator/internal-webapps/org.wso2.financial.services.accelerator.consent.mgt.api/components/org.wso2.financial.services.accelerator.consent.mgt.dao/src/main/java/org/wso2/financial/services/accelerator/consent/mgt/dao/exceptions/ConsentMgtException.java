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

package org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions;

import org.wso2.financial.services.accelerator.consent.mgt.dao.constants.ConsentError;

import javax.ws.rs.core.Response;

/**
 * Used for creating runtime exceptions for financial services modules.
 */
public class ConsentMgtException extends Exception {

    private static final long serialVersionUID = -5686395831712095972L;
    private Response.Status statusCode;
    private ConsentError error;

    // TODO : have to remove unwanted constructors after implementing consent attributes ( still some of the
    //  constructors are in use in the code used for consent attributes)
    public ConsentMgtException(Response.Status statusCode, Throwable cause) {

        super(cause);
        this.statusCode = statusCode;
    }


    public ConsentMgtException(Response.Status statusCode, ConsentError error) {
        this.statusCode = statusCode;
        this.error = error;
    }

    public ConsentMgtException(Response.Status statusCode, ConsentError error, Throwable e) {
        super(e);
        this.statusCode = statusCode;
        this.error = error;
    }



    public ConsentMgtException(String message) {

        super(message);
    }


    public ConsentMgtException(ConsentError e) {
        this.error = e;
    }

    public ConsentMgtException(Response.Status statusCode, String message) {

        super(message);
        this.statusCode = statusCode;
    }


    public ConsentMgtException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConsentMgtException(Response.Status statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;

    }
    public ConsentError getError() {

        return error;
    }

    public Response.Status getStatusCode() {
        return statusCode;
    }


}
