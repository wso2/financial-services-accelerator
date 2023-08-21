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

package com.wso2.openbanking.accelerator.gateway.executor.test.util;

import com.wso2.openbanking.accelerator.common.util.OpenBankingUtils;
import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.test.TestConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Util for testing.
 */
public class TestUtil {

    public static Map<String, List<OpenBankingGatewayExecutor>> initExecutors() {

        Map<String, List<OpenBankingGatewayExecutor>> executors = new HashMap<>();
        Map<String, Map<Integer, String>> fullValidatorMap = TestConstants.FULL_VALIDATOR_MAP;
        for (Map.Entry<String, Map<Integer, String>> stringMapEntry : fullValidatorMap.entrySet()) {
            List<OpenBankingGatewayExecutor> executorList = new ArrayList<>();
            Map<Integer, String> executorNames = stringMapEntry.getValue();
            for (Map.Entry<Integer, String> executorEntity : executorNames.entrySet()) {
                OpenBankingGatewayExecutor object = (OpenBankingGatewayExecutor)
                        OpenBankingUtils.getClassInstanceFromFQN(executorEntity.getValue());
                executorList.add(object);
            }
            executors.put(stringMapEntry.getKey(), executorList);
        }
        return executors;

    }
}
