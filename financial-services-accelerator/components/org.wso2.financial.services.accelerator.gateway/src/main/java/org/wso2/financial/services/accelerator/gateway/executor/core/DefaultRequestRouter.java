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
import org.wso2.financial.services.accelerator.gateway.util.GatewayConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Financial Services Default Request Router.
 */
public class DefaultRequestRouter extends AbstractRequestRouter {

    private static final List<FinancialServicesGatewayExecutor> EMPTY_LIST = new ArrayList<>();

    @Override
    public List<FinancialServicesGatewayExecutor> getExecutorsForRequest(FSAPIRequestContext requestContext) {
        if (GatewayConstants.API_TYPE_NON_REGULATORY
                .equals(requestContext.getOpenAPI().getExtensions().get(GatewayConstants.API_TYPE_CUSTOM_PROP))) {
            requestContext.addContextProperty(GatewayConstants.API_TYPE_CUSTOM_PROP,
                    GatewayConstants.API_TYPE_NON_REGULATORY);
            return EMPTY_LIST;
        } else if (GatewayConstants.API_TYPE_CONSENT
                .equals(requestContext.getOpenAPI().getExtensions().get(GatewayConstants.API_TYPE_CUSTOM_PROP))) {
            //add support for consent management portal APIs
            requestContext.addContextProperty(GatewayConstants.API_TYPE_CUSTOM_PROP,
                    GatewayConstants.API_TYPE_CONSENT);
            return this.getExecutorMap().get(GatewayConstants.EXECUTOR_TYPE_CONSENT);
        } else if (requestContext.getMsgInfo().getResource().contains(GatewayConstants.DCR_PATH)) {
            return this.getExecutorMap().get(GatewayConstants.EXECUTOR_TYPE_DCR);
        } else {
            return this.getExecutorMap().get(GatewayConstants.EXECUTOR_TYPE_DEFAULT);
        }
    }

    @Override
    public List<FinancialServicesGatewayExecutor> getExecutorsForResponse(FSAPIResponseContext responseContext) {

        if (responseContext.getContextProps().containsKey(GatewayConstants.API_TYPE_CUSTOM_PROP)) {
            if (GatewayConstants.API_TYPE_NON_REGULATORY
                    .equals(responseContext.getContextProps().get(GatewayConstants.API_TYPE_CUSTOM_PROP))) {
                return EMPTY_LIST;
            } else if (GatewayConstants.API_TYPE_CONSENT
                    .equals(responseContext.getContextProps().get(GatewayConstants.API_TYPE_CUSTOM_PROP))) {
                return this.getExecutorMap().get(GatewayConstants.EXECUTOR_TYPE_CONSENT);
            }
        }

        if (responseContext.getMsgInfo().getResource().contains(GatewayConstants.DCR_PATH)) {
            return this.getExecutorMap().get(GatewayConstants.EXECUTOR_TYPE_DCR);
        } else {
            return this.getExecutorMap().get(GatewayConstants.EXECUTOR_TYPE_DEFAULT);
        }
    }
}
