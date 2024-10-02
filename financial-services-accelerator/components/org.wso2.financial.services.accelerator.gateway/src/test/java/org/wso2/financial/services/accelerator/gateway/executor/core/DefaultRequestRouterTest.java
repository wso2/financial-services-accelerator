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

import io.swagger.v3.oas.models.OpenAPI;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.gateway.GatewayTestConstants;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSAPIRequestContext;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSAPIResponseContext;
import org.wso2.financial.services.accelerator.gateway.util.GatewayConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test for default request router.
 */
public class DefaultRequestRouterTest {

    DefaultRequestRouter defaultRequestRouter;
    OpenAPI openAPI;

    @BeforeClass
    public void beforeClass() {

        defaultRequestRouter = new DefaultRequestRouter();
        defaultRequestRouter.setExecutorMap(initExecutors());
        openAPI = new OpenAPI();
        openAPI.setExtensions(new HashMap<>());
    }

    @Test(priority = 1)
    public void testDCRRequestsForRouter() {

        FSAPIRequestContext obapiRequestContext = Mockito.mock(FSAPIRequestContext.class);
        FSAPIResponseContext obapiResponseContext = Mockito.mock(FSAPIResponseContext.class);
        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setResource("/anyAPIcall/register");
        Mockito.when(obapiRequestContext.getMsgInfo()).thenReturn(msgInfoDTO);
        Mockito.when(obapiRequestContext.getOpenAPI()).thenReturn(openAPI);
        Mockito.when(obapiResponseContext.getMsgInfo()).thenReturn(msgInfoDTO);
        Assert.assertNotNull(defaultRequestRouter.getExecutorsForRequest(obapiRequestContext));
        Assert.assertNotNull(defaultRequestRouter.getExecutorsForResponse(obapiResponseContext));

    }

    @Test(priority = 1)
    public void testAccountRequestsForRouter() {

        FSAPIRequestContext obapiRequestContext = Mockito.mock(FSAPIRequestContext.class);
        FSAPIResponseContext obapiResponseContext = Mockito.mock(FSAPIResponseContext.class);
        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setResource("/anyAPIcall");
        Mockito.when(obapiRequestContext.getOpenAPI()).thenReturn(openAPI);
        Mockito.when(obapiRequestContext.getMsgInfo()).thenReturn(msgInfoDTO);
        Mockito.when(obapiResponseContext.getMsgInfo()).thenReturn(msgInfoDTO);
        Assert.assertNotNull(defaultRequestRouter.getExecutorsForRequest(obapiRequestContext));
        Assert.assertNotNull(defaultRequestRouter.getExecutorsForResponse(obapiResponseContext));
    }

    @Test(priority = 2)
    public void testNonRegulatoryAPIcall() {

        FSAPIRequestContext obapiRequestContext = Mockito.mock(FSAPIRequestContext.class);
        FSAPIResponseContext obapiResponseContext = Mockito.mock(FSAPIResponseContext.class);
        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setResource("/anyAPIcall");
        Map<String, Object> extensions = new HashMap<>();
        Map<String, Object> contextProps = new HashMap<>();
        extensions.put(GatewayConstants.API_TYPE_CUSTOM_PROP, GatewayConstants.API_TYPE_NON_REGULATORY);
        contextProps.put(GatewayConstants.API_TYPE_CUSTOM_PROP, GatewayConstants.API_TYPE_NON_REGULATORY);
        openAPI.setExtensions(extensions);
        Mockito.when(obapiRequestContext.getOpenAPI()).thenReturn(openAPI);
        Mockito.when(obapiRequestContext.getMsgInfo()).thenReturn(msgInfoDTO);
        Mockito.when(obapiResponseContext.getMsgInfo()).thenReturn(msgInfoDTO);
        Mockito.when(obapiResponseContext.getContextProps()).thenReturn(contextProps);
        Assert.assertEquals(defaultRequestRouter.getExecutorsForRequest(obapiRequestContext).size(), 0);
        Assert.assertEquals(defaultRequestRouter.getExecutorsForResponse(obapiResponseContext).size(), 0);
    }

    @Test(priority = 2)
    public void testRegulatoryAPIcall() {

        FSAPIRequestContext obapiRequestContext = Mockito.mock(FSAPIRequestContext.class);
        FSAPIResponseContext obapiResponseContext = Mockito.mock(FSAPIResponseContext.class);
        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setResource("/anyAPIcall");
        Map<String, Object> extensions = new HashMap<>();
        Map<String, Object> contextProps = new HashMap<>();
        extensions.put(GatewayConstants.API_TYPE_CUSTOM_PROP, GatewayConstants.API_TYPE_NON_REGULATORY);
        contextProps.put(GatewayConstants.API_TYPE_CUSTOM_PROP, "regulatory");
        openAPI.setExtensions(extensions);
        Mockito.when(obapiRequestContext.getOpenAPI()).thenReturn(openAPI);
        Mockito.when(obapiRequestContext.getMsgInfo()).thenReturn(msgInfoDTO);
        Mockito.when(obapiResponseContext.getMsgInfo()).thenReturn(msgInfoDTO);
        Mockito.when(obapiResponseContext.getContextProps()).thenReturn(contextProps);
        Assert.assertNotNull(defaultRequestRouter.getExecutorsForRequest(obapiRequestContext));
        Assert.assertNotNull(defaultRequestRouter.getExecutorsForResponse(obapiResponseContext));
    }

    public static Map<String, List<FinancialServicesGatewayExecutor>> initExecutors() {

        Map<String, List<FinancialServicesGatewayExecutor>> executors = new HashMap<>();
        Map<String, Map<Integer, String>> fullValidatorMap = GatewayTestConstants.FULL_VALIDATOR_MAP;
        for (Map.Entry<String, Map<Integer, String>> stringMapEntry : fullValidatorMap.entrySet()) {
            List<FinancialServicesGatewayExecutor> executorList = new ArrayList<>();
            Map<Integer, String> executorNames = stringMapEntry.getValue();
            for (Map.Entry<Integer, String> executorEntity : executorNames.entrySet()) {
                FinancialServicesGatewayExecutor object = (FinancialServicesGatewayExecutor)
                        FinancialServicesUtils.getClassInstanceFromFQN(executorEntity.getValue());
                executorList.add(object);
            }
            executors.put(stringMapEntry.getKey(), executorList);
        }
        return executors;

    }
}
