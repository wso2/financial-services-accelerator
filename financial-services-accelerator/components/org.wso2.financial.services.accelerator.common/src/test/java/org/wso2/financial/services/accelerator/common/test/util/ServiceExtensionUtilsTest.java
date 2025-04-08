/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.common.test.util;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONObject;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.OperationEnum;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.util.HTTPClientUtils;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Test class for ServiceExtensionUtils.
 */
public class ServiceExtensionUtilsTest {

    private static MockedStatic<FinancialServicesConfigParser> configParser;
    private static MockedStatic<HTTPClientUtils> httpClientUtilsMockedStatic;
    HttpEntity httpEntityMock;

    @BeforeClass
    public void setUp() throws IOException {

        configParser = Mockito.mockStatic(FinancialServicesConfigParser.class);
        Map<String, Object> configs = new HashMap<String, Object>();
        configs.put(FinancialServicesConstants.MAX_INSTRUCTED_AMOUNT, "1000");
        FinancialServicesConfigParser configParserMock = Mockito.mock(FinancialServicesConfigParser.class);
        Mockito.doReturn(configs).when(configParserMock).getConfiguration();
        Mockito.doReturn(FinancialServicesConstants.BASIC_AUTH).when(configParserMock)
                .getServiceExtensionsEndpointSecurityType();
        Mockito.doReturn(3).when(configParserMock)
                .getServiceExtensionsEndpointRetryCount();
        Mockito.doReturn(5).when(configParserMock)
                .getServiceExtensionsEndpointConnectTimeoutInSeconds();
        Mockito.doReturn(5).when(configParserMock)
                .getServiceExtensionsEndpointReadTimeoutInSeconds();
        Mockito.doReturn("test").when(configParserMock)
                .getServiceExtensionsEndpointSecurityBasicAuthUsername();
        Mockito.doReturn("test").when(configParserMock)
                .getServiceExtensionsEndpointSecurityBasicAuthPassword();
        Mockito.doReturn(true).when(configParserMock).isServiceExtensionsEndpointEnabled();
        List<ServiceExtensionTypeEnum> serviceExtensionTypes = new ArrayList<>();
        serviceExtensionTypes.add(ServiceExtensionTypeEnum.VALIDATE_DCR_CREATE_REQUEST);
        Mockito.doReturn(serviceExtensionTypes).when(configParserMock).getServiceExtensionTypes();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        String serviceResponse = "{\n" +
                "  \"responseId\": \"Ec1wMjmiG8\",\n" +
                "  \"status\": \"SUCCESS\"\n" +
                "}";
        byte[] crlBytes = serviceResponse.getBytes(StandardCharsets.UTF_8);
        InputStream inStream = new ByteArrayInputStream(crlBytes);

        httpEntityMock = Mockito.mock(HttpEntity.class);
        Mockito.doReturn(inStream).when(httpEntityMock).getContent();

        StatusLine statusLine = Mockito.mock(StatusLine.class);
        Mockito.doReturn(200).when(statusLine).getStatusCode();

        CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);
        CloseableHttpResponse httpResponse = Mockito.mock(CloseableHttpResponse.class);
        Mockito.doReturn(httpEntityMock).when(httpResponse).getEntity();
        Mockito.doReturn(statusLine).when(httpResponse).getStatusLine();
        Mockito.doReturn(httpResponse).when(httpClient).execute(Mockito.any());

        httpClientUtilsMockedStatic = Mockito.mockStatic(HTTPClientUtils.class);
        httpClientUtilsMockedStatic.when(() -> HTTPClientUtils.getHttpsClient()).thenReturn(httpClient);
    }

    @AfterClass
    public static void afterClass() {
        configParser.close();
        httpClientUtilsMockedStatic.close();
    }

    @Test
    public void testIsInvokeExternalService() {
        Assert.assertTrue(ServiceExtensionUtils
                .isInvokeExternalService(ServiceExtensionTypeEnum.VALIDATE_DCR_CREATE_REQUEST));
    }

    @Test
    public void testInvokeExternalServiceCall() throws FinancialServicesException {
        ExternalServiceResponse response = ServiceExtensionUtils.invokeExternalServiceCall(getDCRCreateServiceRequest(),
                ServiceExtensionTypeEnum.VALIDATE_DCR_CREATE_REQUEST);

        Assert.assertNotNull(response);
    }

    @Test(expectedExceptions = FinancialServicesException.class)
    public void testInvokeExternalServiceCallForError() throws IOException, FinancialServicesException {

        StatusLine statusLine = Mockito.mock(StatusLine.class);
        Mockito.doReturn(400).when(statusLine).getStatusCode();

        CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);
        CloseableHttpResponse httpResponse = Mockito.mock(CloseableHttpResponse.class);
        Mockito.doReturn(httpEntityMock).when(httpResponse).getEntity();
        Mockito.doReturn(statusLine).when(httpResponse).getStatusLine();
        Mockito.doReturn(httpResponse).when(httpClient).execute(Mockito.any());

        httpClientUtilsMockedStatic.when(() -> HTTPClientUtils.getHttpClient()).thenReturn(httpClient);
        ServiceExtensionUtils.invokeExternalServiceCall(getDCRCreateServiceRequest(),
                ServiceExtensionTypeEnum.VALIDATE_DCR_CREATE_REQUEST);
    }

    @Test
    public void testPathExists() {

        String json = "{\n" +
                "   \"data\":{\n" +
                "      \"responseId\":\"Ec1wMjmiG8\",\n" +
                "      \"actionStatus\":\"SUCCESS\"\n" +
                "   }\n" +
                "}";
        Assert.assertTrue(ServiceExtensionUtils.pathExists(new JSONObject(json), "data.actionStatus"));
    }

    @Test
    public void testRetrieveValueFromJSONObject() {

        String json = "{\n" +
                "   \"data\":{\n" +
                "      \"responseId\":\"Ec1wMjmiG8\",\n" +
                "      \"actionStatus\":\"SUCCESS\"\n" +
                "   }\n" +
                "}";
        Object value = ServiceExtensionUtils.retrieveValueFromJSONObject(new JSONObject(json), "data.actionStatus");
        Assert.assertEquals(value, "SUCCESS");
    }

    private ExternalServiceRequest getDCRCreateServiceRequest() {
        JSONObject appRegistrationRequest = new JSONObject();
        appRegistrationRequest.put("appRegistrationRequest", new HashMap<>());
        appRegistrationRequest.put("ssaParams", new HashMap<>());
        return new ExternalServiceRequest(UUID.randomUUID().toString(), appRegistrationRequest,
                OperationEnum.ADDITIONAL_ID_TOKEN_CLAIMS_FOR_AUTHZ_RESPONSE);
    }
}
