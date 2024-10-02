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

package org.wso2.financial.services.accelerator.gateway.executor.core;


import org.wso2.financial.services.accelerator.gateway.executor.model.FSAPIRequestContext;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSAPIResponseContext;

/**
 * Mock Open banking executor for testing.
 */
public class MockOBExecutor implements FinancialServicesGatewayExecutor {

    @Override
    public void preProcessRequest(FSAPIRequestContext fsapiRequestContext) {

        fsapiRequestContext.setModifiedPayload("{}");
    }

    /**
     * Method to handle post request.
     *
     * @param fsapiRequestContext FS request context object
     */
    @Override
    public void postProcessRequest(FSAPIRequestContext fsapiRequestContext) {

    }


    /**
     * Method to handle pre response.
     *
     * @param fsapiResponseContext FS response context object
     */
    @Override
    public void preProcessResponse(FSAPIResponseContext fsapiResponseContext) {

    }

    /**
     * Method to handle post response.
     *
     * @param fsapiResponseContext FS response context object
     */
    @Override
    public void postProcessResponse(FSAPIResponseContext fsapiResponseContext) {

    }

}
