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

package org.wso2.financial.services.accelerator.gateway.executor.impl.consent;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONObject;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.common.gateway.dto.APIRequestInfoDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigurationService;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.util.HTTPClientUtils;
import org.wso2.financial.services.accelerator.gateway.GatewayTestConstants;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSAPIRequestContext;
import org.wso2.financial.services.accelerator.gateway.executor.model.FSExecutorError;
import org.wso2.financial.services.accelerator.gateway.internal.GatewayDataHolder;
import org.wso2.financial.services.accelerator.gateway.util.GatewayUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Test for enforcement executor.
 */
public class ConsentEnforcementExecutorTest {

    private static ConsentEnforcementExecutor consentEnforcementExecutor;
    private static MockedStatic<HTTPClientUtils> httpClientUtilsMockedStatic;

    @BeforeClass
    public static void beforeClass() throws IOException {

        GatewayDataHolder dataHolder = GatewayDataHolder.getInstance();
        String path = "src/test/resources";
        File file = new File(path);
        String absolutePathForTestResources = file.getAbsolutePath();
        dataHolder.setKeyStoreLocation(absolutePathForTestResources + "/wso2carbon.jks");
        dataHolder.setKeyAlias("wso2carbon");
        dataHolder.setKeyPassword("wso2carbon");
        dataHolder.setKeyStorePassword("wso2carbon".toCharArray());

        Map<String, Object> configs = new HashMap<>();
        configs.put(FinancialServicesConstants.CONSENT_VALIDATION_ENDPOINT, "http://localhost:8080");
        configs.put(FinancialServicesConstants.REQUEST_ROUTER,
                "org.wso2.financial.services.accelerator.gateway.executor.core.DefaultRequestRouter");
        FinancialServicesConfigurationService financialServicesConfigurationService =
                Mockito.mock(FinancialServicesConfigurationService.class);
        Mockito.when(financialServicesConfigurationService.getConfigurations()).thenReturn(configs);
        dataHolder.setFinancialServicesConfigurationService(financialServicesConfigurationService);

        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.getFirstProperty(Mockito.anyString())).thenReturn("admin");
        APIManagerConfigurationService apimConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(apimConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        dataHolder.setApiManagerConfiguration(apimConfigurationService);

        File responseFile = new File("src/test/resources/test-validation-response.json");
        byte[] crlBytes = FileUtils.readFileToString(responseFile, String.valueOf(StandardCharsets.UTF_8))
                .getBytes(StandardCharsets.UTF_8);
        InputStream inStream = new ByteArrayInputStream(crlBytes);

        HttpEntity httpEntityMock = Mockito.mock(HttpEntity.class);
        Mockito.doReturn(inStream).when(httpEntityMock).getContent();

        CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);
        CloseableHttpResponse httpResponse = Mockito.mock(CloseableHttpResponse.class);
        Mockito.doReturn(httpEntityMock).when(httpResponse).getEntity();
        Mockito.doReturn(httpResponse).when(httpClient).execute(Mockito.any());

        httpClientUtilsMockedStatic = Mockito.mockStatic(HTTPClientUtils.class);
        httpClientUtilsMockedStatic.when(() -> HTTPClientUtils.getHttpsClient()).thenReturn(httpClient);

        consentEnforcementExecutor = new ConsentEnforcementExecutor();
    }

    @AfterClass
    public static void afterClass() {
        httpClientUtilsMockedStatic.close();
    }

    @Test(priority = 1)
    public void testSigningKeyRetrieval() {

        Assert.assertNotNull(consentEnforcementExecutor.getJWTSigningKey());
    }

    @Test(priority = 2)
    public void testJWTGeneration() {

        String jwtToken = consentEnforcementExecutor.generateJWT(GatewayTestConstants.CUSTOM_PAYLOAD);
        Assert.assertNotNull(jwtToken);
        String[] parts = jwtToken.split("\\.");
        Assert.assertEquals(parts.length, 3);
    }

    @Test(priority = 2)
    public void testValidationPayloadCreation() {

        Map<String, String> headers = new HashMap<>();
        headers.put("customHeader", "headerValue");
        headers.put("customHeader2", "headerValue2");
        JSONObject jsonObject =
                consentEnforcementExecutor.createValidationRequestPayload(headers,
                        GatewayTestConstants.CUSTOM_PAYLOAD, new HashMap<>());
        Assert.assertNotNull(jsonObject);
        Assert.assertEquals(((JSONObject) jsonObject.get(ConsentEnforcementExecutor.HEADERS_TAG)).get("customHeader"),
                "headerValue");
        Assert.assertEquals(((JSONObject) jsonObject.get(ConsentEnforcementExecutor.BODY_TAG)).get("custom"),
                "payload");
    }

    @Test(priority = 3)
    public void testB64Decoder() throws UnsupportedEncodingException {

        String jwtToken = "eyJjdXN0b20iOiJwYXlsb2FkIn0";
        JSONObject jsonObject = GatewayUtils.decodeBase64(jwtToken);
        Assert.assertEquals(jsonObject.get("custom").toString(), "payload");
    }

    @Test
    public void testHandlerError() {
        FSAPIRequestContext fsapiRequestContext = Mockito.mock(FSAPIRequestContext.class);
        ArrayList<FSExecutorError> errors = new ArrayList<>();
        Mockito.when(fsapiRequestContext.getErrors()).thenReturn(errors);
        consentEnforcementExecutor.handleError(fsapiRequestContext, "Error", "Error",
                "400");
    }

    @Test
    public void testPostProcessRequest() {
        String consentID = String.valueOf(UUID.randomUUID());

        MsgInfoDTO msgInfoDTOMock = Mockito.mock(MsgInfoDTO.class);
        Mockito.doReturn("/accounts").when(msgInfoDTOMock).getElectedResource();
        Mockito.doReturn("/accounts").when(msgInfoDTOMock).getResource();
        Mockito.doReturn("GET").when(msgInfoDTOMock).getHttpMethod();

        APIRequestInfoDTO apiRequestInfoDTOMock = Mockito.mock(APIRequestInfoDTO.class);
        Mockito.doReturn("admin@wso2.com").when(apiRequestInfoDTOMock).getUsername();
        Mockito.doReturn("test-client-id").when(apiRequestInfoDTOMock).getConsumerKey();
        Mockito.doReturn("/open-banking/v3.1/aisp").when(apiRequestInfoDTOMock).getContext();

        FSAPIRequestContext fsapiRequestContext = Mockito.mock(FSAPIRequestContext.class);
        Mockito.doReturn(false).when(fsapiRequestContext).isError();
        Mockito.doReturn(consentID).when(fsapiRequestContext).getConsentId();
        Mockito.doReturn(apiRequestInfoDTOMock).when(fsapiRequestContext).getApiRequestInfo();
        Mockito.doReturn(msgInfoDTOMock).when(fsapiRequestContext).getMsgInfo();

        consentEnforcementExecutor.postProcessRequest(fsapiRequestContext);
    }

}
