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

package com.wso2.openbanking.accelerator.gateway.executor.service;

import com.wso2.openbanking.accelerator.gateway.executor.revocation.CRLValidator;
import com.wso2.openbanking.accelerator.gateway.executor.revocation.OCSPValidator;
import com.wso2.openbanking.accelerator.gateway.executor.revocation.RevocationValidator;

/**
 * RevocationValidatorFactory is used to get a object of type RevocationValidator.
 */
public class RevocationValidatorFactory {

    private static final String OCSP_VALIDATOR = "OCSP";
    private static final String CRL_VALIDATOR = "CRL";


    /**
     * Get a object of type RevocationValidator.
     *
     * @param validatorType        name of the required RevocationValidator type
     * @param retryCount           retry count to connect to revocation validator endpoint and get the response
     * @return RevocationValidator type object (OCSP/CRL)
     * @deprecated use {@link #getValidator(String, int, int, int, int)} instead
     */
    @Deprecated
    public RevocationValidator getValidator(String validatorType, int retryCount) {
        if (OCSP_VALIDATOR.equalsIgnoreCase(validatorType)) {
            return new OCSPValidator(retryCount);
        } else if (CRL_VALIDATOR.equalsIgnoreCase(validatorType)) {
            return new CRLValidator(retryCount);
        } else {
            return null;
        }
    }

    /**
     * Get a object of type RevocationValidator.
     *
     * @param validatorType             name of the required RevocationValidator type.
     * @param retryCount                retry count to connect to revocation validator endpoint and get the response.
     * @param connectTimeout            timeout for connecting to revocation validator endpoint.
     * @param connectionRequestTimeout       timeout for getting a connection from the connection manager.
     * @param socketTimeout             timeout for getting the response from the revocation validator endpoint.
     * @return
     */
    public RevocationValidator getValidator(String validatorType, int retryCount, int connectTimeout,
                                            int connectionRequestTimeout, int socketTimeout) {
        if (OCSP_VALIDATOR.equalsIgnoreCase(validatorType)) {
            return new OCSPValidator(retryCount, connectTimeout, connectionRequestTimeout, socketTimeout);
        } else if (CRL_VALIDATOR.equalsIgnoreCase(validatorType)) {
            return new CRLValidator(retryCount, connectTimeout, connectionRequestTimeout, socketTimeout);
        } else {
            return null;
        }
    }
}
