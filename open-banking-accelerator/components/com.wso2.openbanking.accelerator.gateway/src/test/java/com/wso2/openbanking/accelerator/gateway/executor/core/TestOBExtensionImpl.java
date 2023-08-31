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
import com.wso2.openbanking.accelerator.gateway.executor.test.TestConstants;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.common.gateway.dto.ExtensionResponseDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.ExtensionResponseStatus;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;

import java.util.HashMap;
import java.util.Map;

/**
 * Test for open Banking extension implementation.
 */
public class TestOBExtensionImpl {

    private static OBAPIRequestContext obapiRequestContext;
    private static OBAPIResponseContext obapiResponseContext;
    private static OBExtensionListenerImpl obExtensionListener = new OBExtensionListenerImpl();

    @BeforeClass
    public static void beforeClass() {

    }

    @Test(priority = 1)
    public void testMinimumFlow() {

        obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        Mockito.when(obapiRequestContext.isError()).thenReturn(false);
        Mockito.when(obapiRequestContext.getModifiedPayload()).thenReturn(null);
        Mockito.when(obapiRequestContext.getAddedHeaders()).thenReturn(new HashMap<>());

        obapiResponseContext = Mockito.mock(OBAPIResponseContext.class);
        Mockito.when(obapiResponseContext.isError()).thenReturn(false);
        Mockito.when(obapiResponseContext.getModifiedPayload()).thenReturn(null);
        Mockito.when(obapiResponseContext.getAddedHeaders()).thenReturn(new HashMap<>());

        ExtensionResponseDTO responseDTOForRequest = obExtensionListener.getResponseDTOForRequest(obapiRequestContext);
        Assert.assertEquals(responseDTOForRequest.getResponseStatus(), ExtensionResponseStatus.CONTINUE.toString());
        Assert.assertNull(responseDTOForRequest.getHeaders());
        Assert.assertNull(responseDTOForRequest.getPayload());
        Assert.assertNull(responseDTOForRequest.getCustomProperty());
        Assert.assertEquals(responseDTOForRequest.getStatusCode(), 0);

        ExtensionResponseDTO responseDTOForResponse =
                obExtensionListener.getResponseDTOForResponse(obapiResponseContext);

        Assert.assertEquals(responseDTOForResponse.getResponseStatus(), ExtensionResponseStatus.CONTINUE.toString());
        Assert.assertNull(responseDTOForResponse.getHeaders());
        Assert.assertNull(responseDTOForResponse.getPayload());
        Assert.assertNull(responseDTOForResponse.getCustomProperty());
        Assert.assertEquals(responseDTOForResponse.getStatusCode(), 0);

    }

    @Test(priority = 1)
    public void testAddedHeaders() {

        obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        obapiResponseContext = Mockito.mock(OBAPIResponseContext.class);
        Mockito.when(obapiRequestContext.isError()).thenReturn(false);
        Mockito.when(obapiRequestContext.getModifiedPayload()).thenReturn(null);
        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setHeaders(new HashMap<>());
        Mockito.when(obapiRequestContext.getMsgInfo()).thenReturn(msgInfoDTO);
        Map<String, String> addedHeaders = new HashMap<>();
        addedHeaders.put("custom", "header");
        Mockito.when(obapiRequestContext.getAddedHeaders()).thenReturn(addedHeaders);

        ExtensionResponseDTO responseDTOForRequest = obExtensionListener.getResponseDTOForRequest(obapiRequestContext);
        Assert.assertEquals(responseDTOForRequest.getResponseStatus(), ExtensionResponseStatus.CONTINUE.toString());
        Assert.assertEquals(responseDTOForRequest.getHeaders().get("custom"), "header");
        Assert.assertNull(responseDTOForRequest.getPayload());
        Assert.assertNull(responseDTOForRequest.getCustomProperty());
        Assert.assertEquals(responseDTOForRequest.getStatusCode(), 0);

        Mockito.when(obapiResponseContext.isError()).thenReturn(false);
        Mockito.when(obapiResponseContext.getModifiedPayload()).thenReturn(null);
        Mockito.when(obapiResponseContext.getMsgInfo()).thenReturn(msgInfoDTO);
        Mockito.when(obapiResponseContext.getAddedHeaders()).thenReturn(addedHeaders);

        ExtensionResponseDTO responseDTOForResponse =
                obExtensionListener.getResponseDTOForResponse(obapiResponseContext);
        Assert.assertEquals(responseDTOForResponse.getResponseStatus(), ExtensionResponseStatus.CONTINUE.toString());
        Assert.assertEquals(responseDTOForResponse.getHeaders().get("custom"), "header");
        Assert.assertNull(responseDTOForResponse.getPayload());
        Assert.assertNull(responseDTOForResponse.getCustomProperty());
        Assert.assertEquals(responseDTOForResponse.getStatusCode(), 0);

    }

    @Test(priority = 1)
    public void testModifiedPayload() {

        obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        obapiResponseContext = Mockito.mock(OBAPIResponseContext.class);
        Mockito.when(obapiRequestContext.isError()).thenReturn(false);
        Mockito.when(obapiRequestContext.getModifiedPayload()).thenReturn(TestConstants.CUSTOM_PAYLOAD);
        Mockito.when(obapiRequestContext.getAddedHeaders()).thenReturn(new HashMap<>());

        ExtensionResponseDTO responseDTOForRequest = obExtensionListener.getResponseDTOForRequest(obapiRequestContext);
        Assert.assertEquals(responseDTOForRequest.getResponseStatus(), ExtensionResponseStatus.CONTINUE.toString());
        Assert.assertNull(responseDTOForRequest.getHeaders());
        Assert.assertNotNull(responseDTOForRequest.getPayload());
        Assert.assertNull(responseDTOForRequest.getCustomProperty());
        Assert.assertEquals(responseDTOForRequest.getStatusCode(), 0);

        Mockito.when(obapiResponseContext.isError()).thenReturn(false);
        Mockito.when(obapiResponseContext.getModifiedPayload()).thenReturn(TestConstants.CUSTOM_PAYLOAD);
        Mockito.when(obapiResponseContext.getAddedHeaders()).thenReturn(new HashMap<>());

        ExtensionResponseDTO responseDTOForResponse =
                obExtensionListener.getResponseDTOForResponse(obapiResponseContext);
        Assert.assertEquals(responseDTOForResponse.getResponseStatus(), ExtensionResponseStatus.CONTINUE.toString());
        Assert.assertNull(responseDTOForResponse.getHeaders());
        Assert.assertNotNull(responseDTOForResponse.getPayload());
        Assert.assertNull(responseDTOForResponse.getCustomProperty());
        Assert.assertEquals(responseDTOForResponse.getStatusCode(), 0);

    }

    @Test(priority = 1)
    public void testErrorFlow() {

        obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        Mockito.when(obapiRequestContext.isError()).thenReturn(true);
        JSONObject errorJSON = new JSONObject();
        errorJSON.put("error", true);
        Mockito.when(obapiRequestContext.getModifiedPayload()).thenReturn(errorJSON.toString());
        Mockito.when(obapiRequestContext.getAddedHeaders()).thenReturn(new HashMap<>());

        obapiResponseContext = Mockito.mock(OBAPIResponseContext.class);
        Mockito.when(obapiResponseContext.isError()).thenReturn(true);
        Mockito.when(obapiResponseContext.getModifiedPayload()).thenReturn(errorJSON.toString());
        Mockito.when(obapiResponseContext.getAddedHeaders()).thenReturn(new HashMap<>());

        ExtensionResponseDTO responseDTOForRequest = obExtensionListener.getResponseDTOForRequest(obapiRequestContext);
        Assert.assertEquals(responseDTOForRequest.getResponseStatus(), ExtensionResponseStatus.RETURN_ERROR.toString());
        Assert.assertNull(responseDTOForRequest.getHeaders());
        Assert.assertNotNull(responseDTOForRequest.getPayload());
        Assert.assertNull(responseDTOForRequest.getCustomProperty());
        Assert.assertEquals(responseDTOForRequest.getStatusCode(), 500);

        ExtensionResponseDTO responseDTOForResponse =
                obExtensionListener.getResponseDTOForResponse(obapiResponseContext);

        Assert.assertEquals(responseDTOForResponse.getResponseStatus(),
                ExtensionResponseStatus.RETURN_ERROR.toString());
        Assert.assertNull(responseDTOForResponse.getHeaders());
        Assert.assertNotNull(responseDTOForResponse.getPayload());
        Assert.assertNull(responseDTOForResponse.getCustomProperty());
        Assert.assertEquals(responseDTOForResponse.getStatusCode(), 500);

    }

    @Test(priority = 1)
    public void testFlowWithReturnResponseTrue() {

        Map<String, String> contextProps = new HashMap<>();
        contextProps.put(GatewayConstants.IS_RETURN_RESPONSE, "true");
        contextProps.put(GatewayConstants.MODIFIED_STATUS, String.valueOf(HttpStatus.SC_CREATED));
        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setHeaders(new HashMap<>());

        obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        Mockito.when(obapiRequestContext.isError()).thenReturn(false);
        Mockito.when(obapiRequestContext.getModifiedPayload()).thenReturn(null);
        Mockito.when(obapiRequestContext.getAddedHeaders()).thenReturn(new HashMap<>());
        Mockito.when(obapiRequestContext.getMsgInfo()).thenReturn(msgInfoDTO);
        Mockito.when(obapiRequestContext.getContextProps()).thenReturn(contextProps);

        obapiResponseContext = Mockito.mock(OBAPIResponseContext.class);
        Mockito.when(obapiResponseContext.isError()).thenReturn(false);
        Mockito.when(obapiResponseContext.getModifiedPayload()).thenReturn(null);
        Mockito.when(obapiResponseContext.getAddedHeaders()).thenReturn(new HashMap<>());
        Mockito.when(obapiResponseContext.getMsgInfo()).thenReturn(msgInfoDTO);
        Mockito.when(obapiResponseContext.getContextProps()).thenReturn(contextProps);

        ExtensionResponseDTO responseDTOForRequest = obExtensionListener.getResponseDTOForRequest(obapiRequestContext);
        Assert.assertEquals(responseDTOForRequest.getResponseStatus(), ExtensionResponseStatus.RETURN_ERROR.toString());
        Assert.assertEquals(responseDTOForRequest.getStatusCode(), HttpStatus.SC_CREATED);

        ExtensionResponseDTO responseDTOForResponse =
                obExtensionListener.getResponseDTOForResponse(obapiResponseContext);

        Assert.assertEquals(responseDTOForResponse.getResponseStatus(),
                ExtensionResponseStatus.RETURN_ERROR.toString());
        Assert.assertEquals(responseDTOForResponse.getStatusCode(), HttpStatus.SC_CREATED);

    }

}
