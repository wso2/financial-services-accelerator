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

package com.wso2.openbanking.accelerator.gateway.executor.test.executor;

import com.wso2.openbanking.accelerator.gateway.executor.core.AbstractRequestRouter;
import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;

import java.util.List;

/**
 * Router for testing.
 */
public class TestRouter extends AbstractRequestRouter {

    @Override
    public List<OpenBankingGatewayExecutor> getExecutorsForRequest(OBAPIRequestContext requestContext) {

        return super.getExecutorMap().get(requestContext.getMsgInfo().getHeaders().get("test-prop"));
    }

    @Override
    public List<OpenBankingGatewayExecutor> getExecutorsForResponse(OBAPIResponseContext requestContext) {

        return super.getExecutorMap().get("VALID");
    }
}
