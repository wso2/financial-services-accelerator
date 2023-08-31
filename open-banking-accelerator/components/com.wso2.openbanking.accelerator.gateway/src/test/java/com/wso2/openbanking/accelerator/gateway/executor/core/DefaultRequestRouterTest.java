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
import com.wso2.openbanking.accelerator.gateway.executor.test.util.TestUtil;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import io.swagger.v3.oas.models.OpenAPI;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;

import java.util.HashMap;
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
        defaultRequestRouter.setExecutorMap(TestUtil.initExecutors());
        openAPI = new OpenAPI();
        openAPI.setExtensions(new HashMap<>());
    }

    @Test(priority = 1)
    public void testDCRRequestsForRouter() {

        OBAPIRequestContext obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        OBAPIResponseContext obapiResponseContext = Mockito.mock(OBAPIResponseContext.class);
        MsgInfoDTO msgInfoDTO1 = new MsgInfoDTO();
        msgInfoDTO1.setResource("/anyAPIcall/register");
        Mockito.when(obapiRequestContext.getMsgInfo()).thenReturn(msgInfoDTO1);
        Mockito.when(obapiRequestContext.getOpenAPI()).thenReturn(openAPI);
        Mockito.when(obapiResponseContext.getMsgInfo()).thenReturn(msgInfoDTO1);
        Assert.assertNotNull(defaultRequestRouter.getExecutorsForRequest(obapiRequestContext));
        Assert.assertNotNull(defaultRequestRouter.getExecutorsForResponse(obapiResponseContext));

    }

    @Test(priority = 1)
    public void testAccountRequestsForRouter() {

        OBAPIRequestContext obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        OBAPIResponseContext obapiResponseContext = Mockito.mock(OBAPIResponseContext.class);
        MsgInfoDTO msgInfoDTO2 = new MsgInfoDTO();
        msgInfoDTO2.setResource("/anyAPIcall");
        Mockito.when(obapiRequestContext.getOpenAPI()).thenReturn(openAPI);
        Mockito.when(obapiRequestContext.getMsgInfo()).thenReturn(msgInfoDTO2);
        Mockito.when(obapiResponseContext.getMsgInfo()).thenReturn(msgInfoDTO2);
        Assert.assertNotNull(defaultRequestRouter.getExecutorsForRequest(obapiRequestContext));
        Assert.assertNotNull(defaultRequestRouter.getExecutorsForResponse(obapiResponseContext));
    }

    @Test(priority = 2)
    public void testNonRegulatoryAPIcall() {

        OBAPIRequestContext obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        OBAPIResponseContext obapiResponseContext = Mockito.mock(OBAPIResponseContext.class);
        MsgInfoDTO msgInfoDTO2 = new MsgInfoDTO();
        msgInfoDTO2.setResource("/anyAPIcall");
        Map<String, Object> extensions = new HashMap<>();
        Map<String, String> contextProps = new HashMap<>();
        extensions.put(GatewayConstants.API_TYPE_CUSTOM_PROP, GatewayConstants.API_TYPE_NON_REGULATORY);
        contextProps.put(GatewayConstants.API_TYPE_CUSTOM_PROP, GatewayConstants.API_TYPE_NON_REGULATORY);
        openAPI.setExtensions(extensions);
        Mockito.when(obapiRequestContext.getOpenAPI()).thenReturn(openAPI);
        Mockito.when(obapiRequestContext.getMsgInfo()).thenReturn(msgInfoDTO2);
        Mockito.when(obapiResponseContext.getMsgInfo()).thenReturn(msgInfoDTO2);
        Mockito.when(obapiResponseContext.getContextProps()).thenReturn(contextProps);
        Assert.assertEquals(defaultRequestRouter.getExecutorsForRequest(obapiRequestContext).size(), 0);
        Assert.assertEquals(defaultRequestRouter.getExecutorsForResponse(obapiResponseContext).size(), 0);
    }

    @Test(priority = 2)
    public void testRegulatoryAPIcall() {

        OBAPIRequestContext obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        OBAPIResponseContext obapiResponseContext = Mockito.mock(OBAPIResponseContext.class);
        MsgInfoDTO msgInfoDTO2 = new MsgInfoDTO();
        msgInfoDTO2.setResource("/anyAPIcall");
        Map<String, Object> extensions = new HashMap<>();
        Map<String, String> contextProps = new HashMap<>();
        extensions.put(GatewayConstants.API_TYPE_CUSTOM_PROP, GatewayConstants.API_TYPE_NON_REGULATORY);
        contextProps.put(GatewayConstants.API_TYPE_CUSTOM_PROP, "regulatory");
        openAPI.setExtensions(extensions);
        Mockito.when(obapiRequestContext.getOpenAPI()).thenReturn(openAPI);
        Mockito.when(obapiRequestContext.getMsgInfo()).thenReturn(msgInfoDTO2);
        Mockito.when(obapiResponseContext.getMsgInfo()).thenReturn(msgInfoDTO2);
        Mockito.when(obapiResponseContext.getContextProps()).thenReturn(contextProps);
        Assert.assertNotNull(defaultRequestRouter.getExecutorsForRequest(obapiRequestContext));
        Assert.assertNotNull(defaultRequestRouter.getExecutorsForResponse(obapiResponseContext));
    }
}
