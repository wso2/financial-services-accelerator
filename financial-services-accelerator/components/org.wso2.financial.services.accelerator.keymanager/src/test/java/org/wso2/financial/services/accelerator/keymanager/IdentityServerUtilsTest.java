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

package org.wso2.financial.services.accelerator.keymanager;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.util.HTTPClientUtils;
import org.wso2.financial.services.accelerator.keymanager.internal.KeyManagerDataHolder;
import org.wso2.financial.services.accelerator.keymanager.util.KeyManagerTestConstants;
import org.wso2.financial.services.accelerator.keymanager.utils.IdentityServerUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

/**
 * Test class for IdentityServerUtils.
 */
public class IdentityServerUtilsTest {

    MockedStatic<KeyManagerDataHolder> keyManagerDataHolderMockedStatic;
    KeyManagerDataHolder keyManagerDataHolder;
    HttpEntity httpEntityMock;
    KeyManagerConfiguration keyManagerConfiguration;

    @BeforeClass
    public void setUp() throws URISyntaxException {

        keyManagerDataHolder = KeyManagerDataHolder.getInstance();

        APIManagerConfiguration apiManagerConfiguration = mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.getFirstProperty(Mockito.anyString()))
                .thenReturn("https://localhost:9446/services/");
        APIManagerConfigurationService apimConfigurationService = mock(APIManagerConfigurationService.class);
        Mockito.when(apimConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        keyManagerDataHolder.setApiManagerConfiguration(apimConfigurationService);

        keyManagerDataHolderMockedStatic = Mockito.mockStatic(KeyManagerDataHolder.class);
        keyManagerDataHolderMockedStatic.when(KeyManagerDataHolder::getInstance)
                .thenReturn(keyManagerDataHolder);

        URIBuilder mockBuilder = Mockito.mock(URIBuilder.class);

        // Mock method chaining
        Mockito.when(mockBuilder.setScheme("https")).thenReturn(mockBuilder);
        Mockito.when(mockBuilder.setHost("example.com")).thenReturn(mockBuilder);
        Mockito.when(mockBuilder.setPath("/api")).thenReturn(mockBuilder);
        Mockito.when(mockBuilder.build()).thenReturn(new URI("https://example.com/api"));

        keyManagerConfiguration = new KeyManagerConfiguration();
        keyManagerConfiguration
                .addParameter(APIConstants.KeyManager.AUTHORIZE_ENDPOINT, "https://localhost:9446/oauth2/authorize");
        keyManagerConfiguration.addParameter(APIConstants.KEY_MANAGER_USERNAME, "test-username");
        keyManagerConfiguration.addParameter(APIConstants.KEY_MANAGER_PASSWORD, "test-password");
    }

    @AfterClass
    public void tearDown() {

        keyManagerDataHolderMockedStatic.close();
    }

    @Test
    public void testGetAppIdFromClientId() throws IOException, FinancialServicesException {

        try (MockedStatic<HTTPClientUtils> httpClientUtilsMockedStatic = Mockito.mockStatic(HTTPClientUtils.class)) {
            byte[] crlBytes = KeyManagerTestConstants.APP_DATA_RESPONSE.getBytes(StandardCharsets.UTF_8);
            InputStream inStream = new ByteArrayInputStream(crlBytes);

            httpEntityMock = mock(HttpEntity.class);
            Mockito.doReturn(inStream).when(httpEntityMock).getContent();

            StatusLine statusLine = mock(StatusLine.class);
            Mockito.doReturn(200).when(statusLine).getStatusCode();

            CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
            CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
            Mockito.doReturn(httpEntityMock).when(httpResponse).getEntity();
            Mockito.doReturn(statusLine).when(httpResponse).getStatusLine();
            Mockito.doReturn(httpResponse).when(httpClient).execute(Mockito.any());

            httpClientUtilsMockedStatic.when(HTTPClientUtils::getHttpsClient).thenReturn(httpClient);

            String appID = IdentityServerUtils
                    .getAppIdFromClientId(keyManagerConfiguration, "rgyZpk8uMCBXqolzjWQyLnmPVd0a");
            Assert.assertNotNull(appID);
        }
    }

//    @Test
    public void testGetSPApplicationFromClientId() throws IOException, FinancialServicesException {

        try (MockedStatic<HTTPClientUtils> httpClientUtilsMockedStatic = Mockito.mockStatic(HTTPClientUtils.class)) {
            byte[] responseBytes = KeyManagerTestConstants.APP_DATA_RESPONSE.getBytes(StandardCharsets.UTF_8);
            InputStream inStream = new ByteArrayInputStream(responseBytes);

            HttpEntity httpEntity = mock(HttpEntity.class);
            Mockito.doReturn(inStream).when(httpEntity).getContent();

            StatusLine statusLine = mock(StatusLine.class);
            Mockito.doReturn(200).when(statusLine).getStatusCode();

            CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
            CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
            Mockito.doReturn(httpEntity).when(httpResponse).getEntity();
            Mockito.doReturn(statusLine).when(httpResponse).getStatusLine();
            Mockito.doReturn(httpResponse).when(httpClient).execute(Mockito.any());

            httpClientUtilsMockedStatic.when(HTTPClientUtils::getHttpsClient).thenReturn(httpClient);

            JSONObject spApp = IdentityServerUtils
                    .getSPApplicationFromClientId(keyManagerConfiguration, "rgyZpk8uMCBXqolzjWQyLnmPVd0a");
            Assert.assertNotNull(spApp);
        }
    }

    @Test
    public void testUpdateSPApplication() throws IOException, FinancialServicesException {

        try (MockedStatic<HTTPClientUtils> httpClientUtilsMockedStatic = Mockito.mockStatic(HTTPClientUtils.class)) {
            byte[] crlBytes = KeyManagerTestConstants.APP_DATA_RESPONSE.getBytes(StandardCharsets.UTF_8);
            InputStream inStream = new ByteArrayInputStream(crlBytes);

            httpEntityMock = mock(HttpEntity.class);
            Mockito.doReturn(inStream).when(httpEntityMock).getContent();

            StatusLine statusLine = mock(StatusLine.class);
            Mockito.doReturn(200).when(statusLine).getStatusCode();

            CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
            CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
            Mockito.doReturn(httpEntityMock).when(httpResponse).getEntity();
            Mockito.doReturn(statusLine).when(httpResponse).getStatusLine();
            Mockito.doReturn(httpResponse).when(httpClient).execute(Mockito.any());

            httpClientUtilsMockedStatic.when(HTTPClientUtils::getHttpsClient).thenReturn(httpClient);

            IdentityServerUtils.updateSPApplication(keyManagerConfiguration, "rgyZpk8uMCBXqolzjWQyLnmPVd0a",
                    KeyManagerTestConstants.SP_CERT);
        }
    }

    @Test
    public void testUpdateDCRApplication() throws IOException, FinancialServicesException {

        try (MockedStatic<HTTPClientUtils> httpClientUtilsMockedStatic = Mockito.mockStatic(HTTPClientUtils.class)) {
            byte[] crlBytes = KeyManagerTestConstants.APP_DATA_RESPONSE.getBytes(StandardCharsets.UTF_8);
            InputStream inStream = new ByteArrayInputStream(crlBytes);

            httpEntityMock = mock(HttpEntity.class);
            Mockito.doReturn(inStream).when(httpEntityMock).getContent();

            StatusLine statusLine = mock(StatusLine.class);
            Mockito.doReturn(200).when(statusLine).getStatusCode();

            CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
            CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
            Mockito.doReturn(httpEntityMock).when(httpResponse).getEntity();
            Mockito.doReturn(statusLine).when(httpResponse).getStatusLine();
            Mockito.doReturn(httpResponse).when(httpClient).execute(Mockito.any());

            httpClientUtilsMockedStatic.when(HTTPClientUtils::getHttpsClient).thenReturn(httpClient);

            IdentityServerUtils.updateDCRApplication(keyManagerConfiguration, "rgyZpk8uMCBXqolzjWQyLnmPVd0a",
                    "test", Map.of("testkey1", "testValue1, testKey2, testValue2"));
        }
    }

    @Test
    public void testConstructSPPropertiesList() {

        HashMap<String, String> spProperties = new HashMap<>();
        spProperties.put("sp_certificate", KeyManagerTestConstants.SP_CERT);
        spProperties.put("regulatory", "true");
        Map<String, Object> spList = IdentityServerUtils.constructSPPropertiesList(
                new JSONArray(KeyManagerTestConstants.SP_PROPERTY_ARRAY), spProperties);

        Assert.assertFalse(spList.isEmpty());
    }

    @Test
    public void testGetSPMetadataFromSPApp() {

        JSONArray spMetadata = IdentityServerUtils.getSPMetadataFromSPApp(
                new JSONObject(KeyManagerTestConstants.SP_APP_RETRIEVAL_RESPONSE));

        Assert.assertFalse(spMetadata.isEmpty());
    }

    @Test
    public void testGetRegulatoryPropertyFromSPMetadata() {

        String regulatory = IdentityServerUtils.getRegulatoryPropertyFromSPMetadata(
                new JSONObject(KeyManagerTestConstants.SP_APP_RETRIEVAL_RESPONSE));

        Assert.assertTrue(Boolean.parseBoolean(regulatory));
    }

    @Test
    public void testGetSpPropertyFromSPMetaData() {

        String propertyValue = IdentityServerUtils.getSpPropertyFromSPMetaData("isB2BSelfServiceApp",
                new JSONArray(KeyManagerTestConstants.SP_PROPERTY_ARRAY));

        Assert.assertFalse(Boolean.parseBoolean(propertyValue));
    }

}
