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

package org.wso2.financial.services.accelerator.gateway.executor.impl.dcr;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.http.HttpStatus;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.common.gateway.dto.APIRequestInfoDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.RequestContextDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.ResponseContextDTO;
import org.wso2.carbon.apimgt.common.gateway.extensionlistener.PayloadHandler;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigurationService;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.util.JWTUtils;
import org.wso2.financial.services.accelerator.gateway.GatewayTestConstants;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSAPIRequestContext;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSAPIResponseContext;
import org.wso2.financial.services.accelerator.gateway.internal.GatewayDataHolder;
import org.wso2.financial.services.accelerator.gateway.util.GatewayConstants;
import org.wso2.financial.services.accelerator.gateway.util.GatewayUtils;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.HttpMethod;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test for DCR executor.
 */
public class DCRExecutorTest {

    private static DCRExecutor dcrExecutor;
    private static MockedStatic<JWTUtils> jwtUtilsMockedStatic;
    private static MockedStatic<GatewayUtils> gatewayUtilsMockedStatic;

    @BeforeClass
    public static void beforeClass() {
        GatewayDataHolder dataHolder = GatewayDataHolder.getInstance();

        Map<String, Object> configs = new HashMap<>();
        configs.put(FinancialServicesConstants.VALIDATE_JWT, true);
        configs.put(FinancialServicesConstants.GATEWAY_CACHE_EXPIRY, "60");
        configs.put(FinancialServicesConstants.GATEWAY_CACHE_MODIFIED_EXPIRY, "60");
        configs.put(FinancialServicesConstants.REQUEST_ROUTER,
                "org.wso2.financial.services.accelerator.gateway.executor.core.DefaultRequestRouter");
        configs.put(FinancialServicesConstants.SSA_CLIENT_NAME, "software_client_name");
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.getFirstProperty(Mockito.anyString())).thenReturn("admin");
        APIManagerConfigurationService apiManagerConfigurationService =
                Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        dataHolder.setApiManagerConfiguration(apiManagerConfigurationService);
        FinancialServicesConfigurationService financialServicesConfigurationService =
                Mockito.mock(FinancialServicesConfigurationService.class);
        Mockito.when(financialServicesConfigurationService.getConfigurations()).thenReturn(configs);
        dataHolder.setFinancialServicesConfigurationService(financialServicesConfigurationService);

        jwtUtilsMockedStatic = Mockito.mockStatic(JWTUtils.class);
        jwtUtilsMockedStatic.when(() -> JWTUtils.decodeRequestJWT(anyString(), anyString()))
                .thenReturn(GatewayTestConstants.DECODED_DCR_PAYLOAD);

        JWTClaimsSet jwtClaimsSet = Mockito.mock(JWTClaimsSet.class);
        Mockito.doReturn(GatewayTestConstants.DCR_CLAIMS).when(jwtClaimsSet).getClaims();

        gatewayUtilsMockedStatic = Mockito.mockStatic(GatewayUtils.class);
        gatewayUtilsMockedStatic.when(() -> GatewayUtils.validateRequestSignature(anyString(), any()))
                .thenReturn(jwtClaimsSet);
        gatewayUtilsMockedStatic.when(() -> GatewayUtils.getSwaggerDefinition(anyString()))
                .thenReturn("");
        gatewayUtilsMockedStatic.when(() -> GatewayUtils.retrieveOpenAPI(any()))
                .thenReturn(new OpenAPI());
        gatewayUtilsMockedStatic.when(() -> GatewayUtils.constructDCRResponseForCreate(any()))
                .thenReturn(GatewayTestConstants.IS_DCR_RESPONSE);
        gatewayUtilsMockedStatic.when(() -> GatewayUtils.constructDCRResponseForRetrieval(any()))
                .thenReturn(GatewayTestConstants.IS_DCR_RESPONSE);

    }

    @AfterClass
    public void tearDown() {
        jwtUtilsMockedStatic.close();
        gatewayUtilsMockedStatic.close();
    }

    @Test
    public void testPreProcessRequest() {

        dcrExecutor = Mockito.spy(DCRExecutor.class);

        MsgInfoDTO msgInfoDTO = Mockito.mock(MsgInfoDTO.class);
        Mockito.doReturn(HttpMethod.POST).when(msgInfoDTO).getHttpMethod();
        Mockito.doReturn(new HashMap<>()).when(msgInfoDTO).getHeaders();

        FSAPIRequestContext fsapiRequestContext = Mockito.mock(FSAPIRequestContext.class);
        Mockito.doReturn(false).when(fsapiRequestContext).isError();
        Mockito.doReturn(GatewayTestConstants.DCR_PAYLOAD).when(fsapiRequestContext).getRequestPayload();
        Mockito.doReturn(msgInfoDTO).when(fsapiRequestContext).getMsgInfo();
        Mockito.doReturn(new HashMap<>()).when(fsapiRequestContext).getAddedHeaders();

        dcrExecutor.preProcessRequest(fsapiRequestContext);
        verify(fsapiRequestContext, times(0)).setError(true);
    }

    @Test
    public void testPreProcessRequestWithoutRequest() {

        dcrExecutor = Mockito.spy(DCRExecutor.class);
        HashMap<String, String> headers = new HashMap<>();
        headers.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JWT_CONTENT_TYPE);

        PayloadHandler payloadHandler = new PayloadHandler() {
            @Override
            public String consumeAsString() throws Exception {
                return GatewayTestConstants.DCR_PAYLOAD;
            }

            @Override
            public InputStream consumeAsStream() throws Exception {
                return null;
            }
        };

        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setHttpMethod(HttpMethod.POST);
        msgInfoDTO.setHeaders(headers);
        msgInfoDTO.setPayloadHandler(payloadHandler);

        APIRequestInfoDTO apiRequestInfoDTO = new APIRequestInfoDTO();
        apiRequestInfoDTO.setApiId("1234");

        RequestContextDTO requestContextDTO = new RequestContextDTO();
        requestContextDTO.setMsgInfo(msgInfoDTO);
        requestContextDTO.setApiRequestInfo(apiRequestInfoDTO);

        gatewayUtilsMockedStatic.when(() -> GatewayUtils.getTextPayload(anyString()))
                .thenReturn(null);

        FSAPIRequestContext fsapiRequestContext = new FSAPIRequestContext(requestContextDTO, new HashMap<>());
        dcrExecutor.preProcessRequest(fsapiRequestContext);
        Assert.assertFalse(fsapiRequestContext.getErrors().isEmpty());
    }

    @Test
    public void testPreProcessRequestWithNullRequest() {

        dcrExecutor = Mockito.spy(DCRExecutor.class);
        HashMap<String, String> headers = new HashMap<>();
        headers.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JWT_CONTENT_TYPE);

        PayloadHandler payloadHandler = new PayloadHandler() {
            @Override
            public String consumeAsString() throws Exception {
                return GatewayTestConstants.DCR_SOAP_PAYLOAD;
            }

            @Override
            public InputStream consumeAsStream() throws Exception {
                return null;
            }
        };

        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setHttpMethod(HttpMethod.POST);
        msgInfoDTO.setHeaders(headers);
        msgInfoDTO.setPayloadHandler(payloadHandler);

        APIRequestInfoDTO apiRequestInfoDTO = new APIRequestInfoDTO();
        apiRequestInfoDTO.setApiId("1234");

        RequestContextDTO requestContextDTO = new RequestContextDTO();
        requestContextDTO.setMsgInfo(msgInfoDTO);
        requestContextDTO.setApiRequestInfo(apiRequestInfoDTO);

        gatewayUtilsMockedStatic.when(() -> GatewayUtils.getTextPayload(anyString()))
                .thenReturn(GatewayTestConstants.DCR_SOAP_PAYLOAD);
        jwtUtilsMockedStatic.when(() -> JWTUtils.decodeRequestJWT(anyString(), anyString())).thenReturn(null);

        FSAPIRequestContext fsapiRequestContext = new FSAPIRequestContext(requestContextDTO, new HashMap<>());
        dcrExecutor.preProcessRequest(fsapiRequestContext);
        Assert.assertNotNull(fsapiRequestContext.getErrors());
        Assert.assertFalse(fsapiRequestContext.getErrors().isEmpty());
    }

    @Test
    public void testPreProcessRequestWithoutSSA() {

        dcrExecutor = Mockito.spy(DCRExecutor.class);
        HashMap<String, String> headers = new HashMap<>();
        headers.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JWT_CONTENT_TYPE);

        PayloadHandler payloadHandler = new PayloadHandler() {
            @Override
            public String consumeAsString() throws Exception {
                return GatewayTestConstants.DCR_SOAP_PAYLOAD;
            }

            @Override
            public InputStream consumeAsStream() throws Exception {
                return null;
            }
        };

        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setHttpMethod(HttpMethod.POST);
        msgInfoDTO.setHeaders(headers);
        msgInfoDTO.setPayloadHandler(payloadHandler);

        APIRequestInfoDTO apiRequestInfoDTO = new APIRequestInfoDTO();
        apiRequestInfoDTO.setApiId("1234");

        RequestContextDTO requestContextDTO = new RequestContextDTO();
        requestContextDTO.setMsgInfo(msgInfoDTO);
        requestContextDTO.setApiRequestInfo(apiRequestInfoDTO);

        gatewayUtilsMockedStatic.when(() -> GatewayUtils.getTextPayload(anyString()))
                .thenReturn(GatewayTestConstants.DCR_SOAP_PAYLOAD);
        jwtUtilsMockedStatic.when(() -> JWTUtils.decodeRequestJWT(anyString(), anyString()))
                .thenReturn(GatewayTestConstants.DECODED_DCR_PAYLOAD_WITHOUT_SSA);

        FSAPIRequestContext fsapiRequestContext = new FSAPIRequestContext(requestContextDTO, new HashMap<>());
        dcrExecutor.preProcessRequest(fsapiRequestContext);
        Assert.assertNotNull(fsapiRequestContext.getErrors());
        Assert.assertFalse(fsapiRequestContext.getErrors().isEmpty());
    }

    @Test
    public void testPreProcessRequestThrowingException() {

        dcrExecutor = Mockito.spy(DCRExecutor.class);
        HashMap<String, String> headers = new HashMap<>();
        headers.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JWT_CONTENT_TYPE);

        PayloadHandler payloadHandler = new PayloadHandler() {
            @Override
            public String consumeAsString() throws Exception {
                return GatewayTestConstants.DCR_SOAP_PAYLOAD;
            }

            @Override
            public InputStream consumeAsStream() throws Exception {
                return null;
            }
        };

        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setHttpMethod(HttpMethod.POST);
        msgInfoDTO.setHeaders(headers);
        msgInfoDTO.setPayloadHandler(payloadHandler);

        APIRequestInfoDTO apiRequestInfoDTO = new APIRequestInfoDTO();
        apiRequestInfoDTO.setApiId("1234");

        RequestContextDTO requestContextDTO = new RequestContextDTO();
        requestContextDTO.setMsgInfo(msgInfoDTO);
        requestContextDTO.setApiRequestInfo(apiRequestInfoDTO);

        gatewayUtilsMockedStatic.when(() -> GatewayUtils.getTextPayload(anyString()))
                .thenReturn(GatewayTestConstants.DCR_SOAP_PAYLOAD);
        jwtUtilsMockedStatic.when(() -> JWTUtils.decodeRequestJWT(anyString(), anyString()))
                .thenThrow(ParseException.class);

        FSAPIRequestContext fsapiRequestContext = new FSAPIRequestContext(requestContextDTO, new HashMap<>());
        dcrExecutor.preProcessRequest(fsapiRequestContext);
        Assert.assertNotNull(fsapiRequestContext.getErrors());
        Assert.assertFalse(fsapiRequestContext.getErrors().isEmpty());
    }

    @Test
    public void testPreProcessRequestThrowingMalformedUrlException() {

        dcrExecutor = Mockito.spy(DCRExecutor.class);
        HashMap<String, String> headers = new HashMap<>();
        headers.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JWT_CONTENT_TYPE);

        PayloadHandler payloadHandler = new PayloadHandler() {
            @Override
            public String consumeAsString() throws Exception {
                return GatewayTestConstants.DCR_SOAP_PAYLOAD;
            }

            @Override
            public InputStream consumeAsStream() throws Exception {
                return null;
            }
        };

        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setHttpMethod(HttpMethod.POST);
        msgInfoDTO.setHeaders(headers);
        msgInfoDTO.setPayloadHandler(payloadHandler);

        APIRequestInfoDTO apiRequestInfoDTO = new APIRequestInfoDTO();
        apiRequestInfoDTO.setApiId("1234");

        RequestContextDTO requestContextDTO = new RequestContextDTO();
        requestContextDTO.setMsgInfo(msgInfoDTO);
        requestContextDTO.setApiRequestInfo(apiRequestInfoDTO);

        gatewayUtilsMockedStatic.when(() -> GatewayUtils.getTextPayload(anyString()))
                .thenReturn(GatewayTestConstants.DCR_SOAP_PAYLOAD);
        gatewayUtilsMockedStatic.when(() -> GatewayUtils.validateRequestSignature(anyString(), any()))
                .thenThrow(MalformedURLException.class);
        jwtUtilsMockedStatic.when(() -> JWTUtils.decodeRequestJWT(anyString(), anyString()))
                .thenReturn(GatewayTestConstants.DECODED_DCR_PAYLOAD);

        FSAPIRequestContext fsapiRequestContext = new FSAPIRequestContext(requestContextDTO, new HashMap<>());
        dcrExecutor.preProcessRequest(fsapiRequestContext);
        Assert.assertNotNull(fsapiRequestContext.getErrors());
        Assert.assertFalse(fsapiRequestContext.getErrors().isEmpty());
    }

    @Test
    public void testPreProcessRequestThrowingJoseException() {

        dcrExecutor = Mockito.spy(DCRExecutor.class);
        HashMap<String, String> headers = new HashMap<>();
        headers.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JWT_CONTENT_TYPE);

        PayloadHandler payloadHandler = new PayloadHandler() {
            @Override
            public String consumeAsString() throws Exception {
                return GatewayTestConstants.DCR_SOAP_PAYLOAD;
            }

            @Override
            public InputStream consumeAsStream() throws Exception {
                return null;
            }
        };

        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setHttpMethod(HttpMethod.POST);
        msgInfoDTO.setHeaders(headers);
        msgInfoDTO.setPayloadHandler(payloadHandler);

        APIRequestInfoDTO apiRequestInfoDTO = new APIRequestInfoDTO();
        apiRequestInfoDTO.setApiId("1234");

        RequestContextDTO requestContextDTO = new RequestContextDTO();
        requestContextDTO.setMsgInfo(msgInfoDTO);
        requestContextDTO.setApiRequestInfo(apiRequestInfoDTO);

        gatewayUtilsMockedStatic.when(() -> GatewayUtils.getTextPayload(anyString()))
                .thenReturn(GatewayTestConstants.DCR_SOAP_PAYLOAD);
        gatewayUtilsMockedStatic.when(() -> GatewayUtils.validateRequestSignature(anyString(), any()))
                .thenThrow(JOSEException.class);
        jwtUtilsMockedStatic.when(() -> JWTUtils.decodeRequestJWT(anyString(), anyString()))
                .thenReturn(GatewayTestConstants.DECODED_DCR_PAYLOAD);

        FSAPIRequestContext fsapiRequestContext = new FSAPIRequestContext(requestContextDTO, new HashMap<>());
        dcrExecutor.preProcessRequest(fsapiRequestContext);
        Assert.assertNotNull(fsapiRequestContext.getErrors());
        Assert.assertFalse(fsapiRequestContext.getErrors().isEmpty());
    }

    @Test
    public void testPreProcessRequestThrowingBadJoseException() {

        dcrExecutor = Mockito.spy(DCRExecutor.class);
        HashMap<String, String> headers = new HashMap<>();
        headers.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JWT_CONTENT_TYPE);

        PayloadHandler payloadHandler = new PayloadHandler() {
            @Override
            public String consumeAsString() throws Exception {
                return GatewayTestConstants.DCR_SOAP_PAYLOAD;
            }

            @Override
            public InputStream consumeAsStream() throws Exception {
                return null;
            }
        };

        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setHttpMethod(HttpMethod.POST);
        msgInfoDTO.setHeaders(headers);
        msgInfoDTO.setPayloadHandler(payloadHandler);

        APIRequestInfoDTO apiRequestInfoDTO = new APIRequestInfoDTO();
        apiRequestInfoDTO.setApiId("1234");

        RequestContextDTO requestContextDTO = new RequestContextDTO();
        requestContextDTO.setMsgInfo(msgInfoDTO);
        requestContextDTO.setApiRequestInfo(apiRequestInfoDTO);

        gatewayUtilsMockedStatic.when(() -> GatewayUtils.getTextPayload(anyString()))
                .thenReturn(GatewayTestConstants.DCR_SOAP_PAYLOAD);
        gatewayUtilsMockedStatic.when(() -> GatewayUtils.validateRequestSignature(anyString(), any()))
                .thenThrow(BadJOSEException.class);
        jwtUtilsMockedStatic.when(() -> JWTUtils.decodeRequestJWT(anyString(), anyString()))
                .thenReturn(GatewayTestConstants.DECODED_DCR_PAYLOAD);

        FSAPIRequestContext fsapiRequestContext = new FSAPIRequestContext(requestContextDTO, new HashMap<>());
        dcrExecutor.preProcessRequest(fsapiRequestContext);
        Assert.assertNotNull(fsapiRequestContext.getErrors());
        Assert.assertFalse(fsapiRequestContext.getErrors().isEmpty());
    }

    @Test
    public void testPostProcessResponseForCreate() {

        dcrExecutor = Mockito.spy(DCRExecutor.class);
        HashMap<String, String> headers = new HashMap<>();
        headers.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JSON_CONTENT_TYPE);

        PayloadHandler payloadHandler = new PayloadHandler() {
            @Override
            public String consumeAsString() throws Exception {
                return GatewayTestConstants.IS_DCR_RESPONSE;
            }

            @Override
            public InputStream consumeAsStream() throws Exception {
                return null;
            }
        };

        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setHttpMethod(HttpMethod.POST);
        msgInfoDTO.setHeaders(headers);
        msgInfoDTO.setPayloadHandler(payloadHandler);

        APIRequestInfoDTO apiRequestInfoDTO = new APIRequestInfoDTO();
        apiRequestInfoDTO.setApiId("1234");

        ResponseContextDTO responseContextDTO = new ResponseContextDTO();
        responseContextDTO.setMsgInfo(msgInfoDTO);
        responseContextDTO.setApiRequestInfo(apiRequestInfoDTO);
        responseContextDTO.setStatusCode(HttpStatus.SC_CREATED);

        FSAPIResponseContext fsapiResponseContext = new FSAPIResponseContext(responseContextDTO, new HashMap<>());
        fsapiResponseContext.setStatusCode(HttpStatus.SC_CREATED);
        fsapiResponseContext.addContextProperty(GatewayConstants.REQUEST_PAYLOAD,
                GatewayTestConstants.DECODED_DCR_PAYLOAD);

        dcrExecutor.postProcessResponse(fsapiResponseContext);
    }

    @Test
    public void testPostProcessResponseForUpdate() {

        dcrExecutor = Mockito.spy(DCRExecutor.class);
        HashMap<String, String> headers = new HashMap<>();
        headers.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JSON_CONTENT_TYPE);

        PayloadHandler payloadHandler = new PayloadHandler() {
            @Override
            public String consumeAsString() throws Exception {
                return GatewayTestConstants.IS_DCR_RESPONSE;
            }

            @Override
            public InputStream consumeAsStream() throws Exception {
                return null;
            }
        };

        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setHttpMethod(HttpMethod.PUT);
        msgInfoDTO.setHeaders(headers);
        msgInfoDTO.setPayloadHandler(payloadHandler);

        APIRequestInfoDTO apiRequestInfoDTO = new APIRequestInfoDTO();
        apiRequestInfoDTO.setApiId("1234");

        ResponseContextDTO responseContextDTO = new ResponseContextDTO();
        responseContextDTO.setMsgInfo(msgInfoDTO);
        responseContextDTO.setApiRequestInfo(apiRequestInfoDTO);
        responseContextDTO.setStatusCode(HttpStatus.SC_OK);

        FSAPIResponseContext fsapiResponseContext = new FSAPIResponseContext(responseContextDTO, new HashMap<>());
        fsapiResponseContext.setStatusCode(HttpStatus.SC_CREATED);
        fsapiResponseContext.addContextProperty(GatewayConstants.REQUEST_PAYLOAD,
                GatewayTestConstants.DECODED_DCR_PAYLOAD);

        dcrExecutor.postProcessResponse(fsapiResponseContext);
    }

    @Test
    public void testPostProcessResponseForRetrieval() {

        dcrExecutor = Mockito.spy(DCRExecutor.class);
        HashMap<String, String> headers = new HashMap<>();
        headers.put(GatewayConstants.CONTENT_TYPE_TAG, GatewayConstants.JSON_CONTENT_TYPE);

        PayloadHandler payloadHandler = new PayloadHandler() {
            @Override
            public String consumeAsString() throws Exception {
                return GatewayTestConstants.IS_DCR_RESPONSE;
            }

            @Override
            public InputStream consumeAsStream() throws Exception {
                return null;
            }
        };

        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setHttpMethod(HttpMethod.GET);
        msgInfoDTO.setHeaders(headers);
        msgInfoDTO.setPayloadHandler(payloadHandler);

        APIRequestInfoDTO apiRequestInfoDTO = new APIRequestInfoDTO();
        apiRequestInfoDTO.setApiId("1234");

        ResponseContextDTO responseContextDTO = new ResponseContextDTO();
        responseContextDTO.setMsgInfo(msgInfoDTO);
        responseContextDTO.setApiRequestInfo(apiRequestInfoDTO);
        responseContextDTO.setStatusCode(HttpStatus.SC_OK);

        FSAPIResponseContext fsapiResponseContext = new FSAPIResponseContext(responseContextDTO, new HashMap<>());
        fsapiResponseContext.setStatusCode(HttpStatus.SC_CREATED);
        fsapiResponseContext.addContextProperty(GatewayConstants.REQUEST_PAYLOAD,
                GatewayTestConstants.DECODED_DCR_PAYLOAD);

        dcrExecutor.postProcessResponse(fsapiResponseContext);
        Assert.assertNotNull(fsapiResponseContext.getModifiedPayload());
    }
}
