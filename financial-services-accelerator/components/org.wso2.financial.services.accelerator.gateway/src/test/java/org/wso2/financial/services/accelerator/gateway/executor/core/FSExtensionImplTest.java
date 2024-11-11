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

import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.common.gateway.dto.ExtensionResponseDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.ExtensionResponseStatus;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;
import org.wso2.financial.services.accelerator.gateway.GatewayTestConstants;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSAPIRequestContext;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSAPIResponseContext;
import org.wso2.financial.services.accelerator.gateway.util.GatewayConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Test for FS extension implementation.
 */
public class FSExtensionImplTest {

    private static FSAPIRequestContext fsapiRequestContext;
    private static FSAPIResponseContext fsapiResponseContext;
    private static final FSExtensionListenerImpl fsExtensionListener = new FSExtensionListenerImpl();

    @BeforeClass
    public static void beforeClass() {

    }

    @Test(priority = 1)
    public void testMinimumFlow() {

        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setHeaders(new HashMap<>());

        fsapiRequestContext = Mockito.mock(FSAPIRequestContext.class);
        Mockito.when(fsapiRequestContext.isError()).thenReturn(false);
        Mockito.when(fsapiRequestContext.getModifiedPayload()).thenReturn(null);
        Mockito.when(fsapiRequestContext.getAddedHeaders()).thenReturn(new HashMap<>());
        Mockito.when(fsapiRequestContext.getMsgInfo()).thenReturn(msgInfoDTO);

        fsapiResponseContext = Mockito.mock(FSAPIResponseContext.class);
        Mockito.when(fsapiResponseContext.isError()).thenReturn(false);
        Mockito.when(fsapiResponseContext.getModifiedPayload()).thenReturn(null);
        Mockito.when(fsapiResponseContext.getAddedHeaders()).thenReturn(new HashMap<>());
        Mockito.when(fsapiResponseContext.getMsgInfo()).thenReturn(msgInfoDTO);

        ExtensionResponseDTO responseDTOForRequest = fsExtensionListener.getResponseDTOForRequest(fsapiRequestContext);
        Assert.assertEquals(responseDTOForRequest.getResponseStatus(), ExtensionResponseStatus.CONTINUE.toString());
        Assert.assertNull(responseDTOForRequest.getHeaders());
        Assert.assertNull(responseDTOForRequest.getPayload());
        Assert.assertNull(responseDTOForRequest.getCustomProperty());
        Assert.assertEquals(responseDTOForRequest.getStatusCode(), 0);

        ExtensionResponseDTO responseDTOForResponse =
                fsExtensionListener.getResponseDTOForResponse(fsapiResponseContext);

        Assert.assertEquals(responseDTOForResponse.getResponseStatus(), ExtensionResponseStatus.CONTINUE.toString());
        Assert.assertNull(responseDTOForResponse.getHeaders());
        Assert.assertNull(responseDTOForResponse.getPayload());
        Assert.assertNull(responseDTOForResponse.getCustomProperty());
        Assert.assertEquals(responseDTOForResponse.getStatusCode(), 0);

    }

    @Test(priority = 1)
    public void testAddedHeaders() {

        fsapiRequestContext = Mockito.mock(FSAPIRequestContext.class);
        fsapiResponseContext = Mockito.mock(FSAPIResponseContext.class);
        Mockito.when(fsapiRequestContext.isError()).thenReturn(false);
        Mockito.when(fsapiRequestContext.getModifiedPayload()).thenReturn(null);
        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setHeaders(new HashMap<>());
        Mockito.when(fsapiRequestContext.getMsgInfo()).thenReturn(msgInfoDTO);
        Map<String, String> addedHeaders = new HashMap<>();
        addedHeaders.put("custom", "header");
        Mockito.when(fsapiRequestContext.getAddedHeaders()).thenReturn(addedHeaders);

        ExtensionResponseDTO responseDTOForRequest = fsExtensionListener.getResponseDTOForRequest(fsapiRequestContext);
        Assert.assertEquals(responseDTOForRequest.getResponseStatus(), ExtensionResponseStatus.CONTINUE.toString());
        Assert.assertEquals(responseDTOForRequest.getHeaders().get("custom"), "header");
        Assert.assertNull(responseDTOForRequest.getPayload());
        Assert.assertNull(responseDTOForRequest.getCustomProperty());
        Assert.assertEquals(responseDTOForRequest.getStatusCode(), 0);

        Mockito.when(fsapiResponseContext.isError()).thenReturn(false);
        Mockito.when(fsapiResponseContext.getModifiedPayload()).thenReturn(null);
        Mockito.when(fsapiResponseContext.getMsgInfo()).thenReturn(msgInfoDTO);
        Mockito.when(fsapiResponseContext.getAddedHeaders()).thenReturn(addedHeaders);

        ExtensionResponseDTO responseDTOForResponse =
                fsExtensionListener.getResponseDTOForResponse(fsapiResponseContext);
        Assert.assertEquals(responseDTOForResponse.getResponseStatus(), ExtensionResponseStatus.CONTINUE.toString());
        Assert.assertEquals(responseDTOForResponse.getHeaders().get("custom"), "header");
        Assert.assertNull(responseDTOForResponse.getPayload());
        Assert.assertNull(responseDTOForResponse.getCustomProperty());
        Assert.assertEquals(responseDTOForResponse.getStatusCode(), 0);

    }

    @Test(priority = 1)
    public void testModifiedPayload() {

        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setHeaders(new HashMap<>());

        fsapiRequestContext = Mockito.mock(FSAPIRequestContext.class);
        fsapiResponseContext = Mockito.mock(FSAPIResponseContext.class);
        Mockito.when(fsapiRequestContext.isError()).thenReturn(false);
        Mockito.when(fsapiRequestContext.getModifiedPayload()).thenReturn(GatewayTestConstants.CUSTOM_PAYLOAD);
        Mockito.when(fsapiRequestContext.getAddedHeaders()).thenReturn(new HashMap<>());
        Mockito.when(fsapiRequestContext.getMsgInfo()).thenReturn(msgInfoDTO);

        ExtensionResponseDTO responseDTOForRequest = fsExtensionListener.getResponseDTOForRequest(fsapiRequestContext);
        Assert.assertEquals(responseDTOForRequest.getResponseStatus(), ExtensionResponseStatus.CONTINUE.toString());
        Assert.assertNull(responseDTOForRequest.getHeaders());
        Assert.assertNotNull(responseDTOForRequest.getPayload());
        Assert.assertNull(responseDTOForRequest.getCustomProperty());
        Assert.assertEquals(responseDTOForRequest.getStatusCode(), 0);

        Mockito.when(fsapiResponseContext.isError()).thenReturn(false);
        Mockito.when(fsapiResponseContext.getModifiedPayload()).thenReturn(GatewayTestConstants.CUSTOM_PAYLOAD);
        Mockito.when(fsapiResponseContext.getAddedHeaders()).thenReturn(new HashMap<>());
        Mockito.when(fsapiResponseContext.getMsgInfo()).thenReturn(msgInfoDTO);

        ExtensionResponseDTO responseDTOForResponse =
                fsExtensionListener.getResponseDTOForResponse(fsapiResponseContext);
        Assert.assertEquals(responseDTOForResponse.getResponseStatus(), ExtensionResponseStatus.CONTINUE.toString());
        Assert.assertNull(responseDTOForResponse.getHeaders());
        Assert.assertNotNull(responseDTOForResponse.getPayload());
        Assert.assertNull(responseDTOForResponse.getCustomProperty());
        Assert.assertEquals(responseDTOForResponse.getStatusCode(), 0);
    }

    @Test(priority = 1)
    public void testErrorFlow() {

        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setHeaders(new HashMap<>());

        fsapiRequestContext = Mockito.mock(FSAPIRequestContext.class);
        Mockito.when(fsapiRequestContext.isError()).thenReturn(true);
        JSONObject errorJSON = new JSONObject();
        errorJSON.put("error", true);
        Mockito.when(fsapiRequestContext.getModifiedPayload()).thenReturn(errorJSON.toString());
        Mockito.when(fsapiRequestContext.getAddedHeaders()).thenReturn(new HashMap<>());
        Mockito.when(fsapiRequestContext.getMsgInfo()).thenReturn(msgInfoDTO);

        fsapiResponseContext = Mockito.mock(FSAPIResponseContext.class);
        Mockito.when(fsapiResponseContext.isError()).thenReturn(true);
        Mockito.when(fsapiResponseContext.getModifiedPayload()).thenReturn(errorJSON.toString());
        Mockito.when(fsapiResponseContext.getAddedHeaders()).thenReturn(new HashMap<>());
        Mockito.when(fsapiResponseContext.getMsgInfo()).thenReturn(msgInfoDTO);

        ExtensionResponseDTO responseDTOForRequest = fsExtensionListener.getResponseDTOForRequest(fsapiRequestContext);
        Assert.assertEquals(responseDTOForRequest.getResponseStatus(), ExtensionResponseStatus.RETURN_ERROR.toString());
        Assert.assertNull(responseDTOForRequest.getHeaders());
        Assert.assertNotNull(responseDTOForRequest.getPayload());
        Assert.assertNull(responseDTOForRequest.getCustomProperty());
        Assert.assertEquals(responseDTOForRequest.getStatusCode(), 500);

        ExtensionResponseDTO responseDTOForResponse =
                fsExtensionListener.getResponseDTOForResponse(fsapiResponseContext);

        Assert.assertEquals(responseDTOForResponse.getResponseStatus(),
                ExtensionResponseStatus.RETURN_ERROR.toString());
        Assert.assertNull(responseDTOForResponse.getHeaders());
        Assert.assertNotNull(responseDTOForResponse.getPayload());
        Assert.assertNull(responseDTOForResponse.getCustomProperty());
        Assert.assertEquals(responseDTOForResponse.getStatusCode(), 500);

    }

    @Test(priority = 1)
    public void testFlowWithReturnResponseTrue() {

        Map<String, Object> contextProps = new HashMap<>();
        contextProps.put(GatewayConstants.IS_RETURN_RESPONSE, "true");
        contextProps.put(GatewayConstants.MODIFIED_STATUS, String.valueOf(HttpStatus.SC_CREATED));
        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setHeaders(new HashMap<>());

        fsapiRequestContext = Mockito.mock(FSAPIRequestContext.class);
        Mockito.when(fsapiRequestContext.isError()).thenReturn(false);
        Mockito.when(fsapiRequestContext.getModifiedPayload()).thenReturn(null);
        Mockito.when(fsapiRequestContext.getAddedHeaders()).thenReturn(new HashMap<>());
        Mockito.when(fsapiRequestContext.getMsgInfo()).thenReturn(msgInfoDTO);
        Mockito.when(fsapiRequestContext.getContextProps()).thenReturn(contextProps);

        fsapiResponseContext = Mockito.mock(FSAPIResponseContext.class);
        Mockito.when(fsapiResponseContext.isError()).thenReturn(false);
        Mockito.when(fsapiResponseContext.getModifiedPayload()).thenReturn(null);
        Mockito.when(fsapiResponseContext.getAddedHeaders()).thenReturn(new HashMap<>());
        Mockito.when(fsapiResponseContext.getMsgInfo()).thenReturn(msgInfoDTO);
        Mockito.when(fsapiResponseContext.getContextProps()).thenReturn(contextProps);

        ExtensionResponseDTO responseDTOForRequest = fsExtensionListener.getResponseDTOForRequest(fsapiRequestContext);
        Assert.assertEquals(responseDTOForRequest.getResponseStatus(), ExtensionResponseStatus.RETURN_ERROR.toString());
        Assert.assertEquals(responseDTOForRequest.getStatusCode(), HttpStatus.SC_CREATED);

        ExtensionResponseDTO responseDTOForResponse =
                fsExtensionListener.getResponseDTOForResponse(fsapiResponseContext);

        Assert.assertEquals(responseDTOForResponse.getResponseStatus(),
                ExtensionResponseStatus.RETURN_ERROR.toString());
        Assert.assertEquals(responseDTOForResponse.getStatusCode(), HttpStatus.SC_CREATED);

    }

}
