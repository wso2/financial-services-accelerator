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

import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.common.util.OpenBankingUtils;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.internal.GatewayDataHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Open Banking abstract Request Router.
 */
public abstract class AbstractRequestRouter {

    private Map<String, List<OpenBankingGatewayExecutor>> executorMap = new HashMap<>();

    /**
     * Initiation method of the Router.
     */
    @Generated(message = "Ignoring since the method require OSGi services to function. This functionality is tested " +
            "in other services")
    public void build() {

        Map<String, Map<Integer, String>> executorConfig =
                GatewayDataHolder.getInstance().getOpenBankingConfigurationService().getExecutors();
        executorConfig.keySet().forEach(consentType -> {
            Map<Integer, String> integerStringMap = executorConfig.get(consentType);
            List<OpenBankingGatewayExecutor> executorList = integerStringMap.keySet().stream()
                    .map(integer -> (OpenBankingGatewayExecutor) OpenBankingUtils
                            .getClassInstanceFromFQN(integerStringMap.get(integer))).collect(Collectors.toList());
            executorMap.put(consentType, executorList);
        });
    }

    /**
     * Method to obtain correct executors for the given request context. ( Expected to be implemented at toolkit)
     *
     * @param requestContext OB Request context
     * @return List of executors
     */
    public abstract List<OpenBankingGatewayExecutor> getExecutorsForRequest(OBAPIRequestContext requestContext);

    /**
     * Method to obtain correct executors for the given response context. ( Expected to be implemented at toolkit)
     *
     * @param requestContext OB Response context
     * @return List of executors
     */
    public abstract List<OpenBankingGatewayExecutor> getExecutorsForResponse(OBAPIResponseContext requestContext);

    public Map<String, List<OpenBankingGatewayExecutor>> getExecutorMap() {

        return executorMap;
    }

    public void setExecutorMap(
            Map<String, List<OpenBankingGatewayExecutor>> executorMap) {

        this.executorMap = executorMap;
    }

}
