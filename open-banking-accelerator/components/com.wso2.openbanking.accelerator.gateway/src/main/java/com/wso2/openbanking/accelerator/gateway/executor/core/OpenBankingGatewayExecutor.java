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

package com.wso2.openbanking.accelerator.gateway.executor.core;

import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;

/**
 * Open Banking executor interface.
 */
public interface OpenBankingGatewayExecutor {

    /**
     * Method to handle pre request.
     *
     * @param obapiRequestContext OB request context object
     */
    public void preProcessRequest(OBAPIRequestContext obapiRequestContext);

    /**
     * Method to handle post request.
     *
     * @param obapiRequestContext OB request context object
     */
    public void postProcessRequest(OBAPIRequestContext obapiRequestContext);

    /**
     * Method to handle pre response.
     *
     * @param obapiResponseContext OB response context object
     */
    public void preProcessResponse(OBAPIResponseContext obapiResponseContext);

    /**
     * Method to handle post response.
     *
     * @param obapiResponseContext OB response context object
     */
    public void postProcessResponse(OBAPIResponseContext obapiResponseContext);
}
