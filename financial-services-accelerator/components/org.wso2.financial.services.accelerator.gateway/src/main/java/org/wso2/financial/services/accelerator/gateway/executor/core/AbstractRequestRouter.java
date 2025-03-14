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


import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSAPIRequestContext;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSAPIResponseContext;
import org.wso2.financial.services.accelerator.gateway.internal.GatewayDataHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Financial Services abstract Request Router.
 */
public abstract class AbstractRequestRouter {

    private Map<String, List<FinancialServicesGatewayExecutor>> executorMap = new HashMap<>();

    /**
     * Initiation method of the Router
     */
    @Generated(message = "Ignoring since the method require OSGi services to function. This functionality is tested " +
            "in other services")
    public void build() {

        Map<String, Map<Integer, String>> executorConfig =
                GatewayDataHolder.getInstance().getFinancialServicesConfigurationService().getExecutors();
        executorConfig.keySet().forEach(consentType -> {
            Map<Integer, String> integerStringMap = executorConfig.get(consentType);
            List<FinancialServicesGatewayExecutor> executorList = integerStringMap.keySet().stream()
                    .map(integer -> FinancialServicesUtils.getClassInstanceFromFQN(integerStringMap.get(integer),
                            FinancialServicesGatewayExecutor.class)).collect(Collectors.toList());
            executorMap.put(consentType, executorList);
        });
    }

    /**
     * Method to obtain correct executors for the given request context. ( Expected to be implemented at toolkit)
     *
     * @param requestContext FS Request context
     * @return List of executors
     */
    public abstract List<FinancialServicesGatewayExecutor> getExecutorsForRequest(FSAPIRequestContext requestContext);

    /**
     * Method to obtain correct executors for the given response context. ( Expected to be implemented at toolkit)
     *
     * @param requestContext FS Response context
     * @return List of executors
     */
    public abstract List<FinancialServicesGatewayExecutor> getExecutorsForResponse(FSAPIResponseContext requestContext);

    public Map<String, List<FinancialServicesGatewayExecutor>> getExecutorMap() {

        return executorMap;
    }

    public void setExecutorMap(
            Map<String, List<FinancialServicesGatewayExecutor>> executorMap) {

        this.executorMap = executorMap;
    }

}
