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
package com.wso2.openbanking.accelerator.gateway.executor.dcr;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigurationService;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.gateway.cache.GatewayCache;
import com.wso2.openbanking.accelerator.gateway.cache.GatewayCacheKey;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.internal.GatewayDataHolder;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import org.apache.http.HttpStatus;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.internal.WhiteboxImpl;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.common.gateway.dto.APIRequestInfoDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.RequestContextDTO;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test for DCR executor.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({IdentityUtil.class, GatewayDataHolder.class})
public class DCRExecutorTest {

    @Mock
    Map<String, Object> urlMap = new HashMap<>();

    @Mock
    OpenBankingConfigurationService openBankingConfigurationService;

    @Mock
    APIManagerConfigurationService apiManagerConfigurationService;

    @Mock
    APIManagerConfiguration apiManagerConfiguration;

    @InjectMocks
    DCRExecutor dcrExecutor = new DCRExecutor();

    Map<String, Object> configMap = new HashMap<>();
    Map<String, List<String>> configuredAPIList = new HashMap<>();
    JsonParser jsonParser = new JsonParser();

    @BeforeTest
    public void init() {

        MockitoAnnotations.initMocks(this);

        urlMap = new HashMap<>();
        urlMap.put("userName", "admin");
        urlMap.put("password", "admin".toCharArray());
        urlMap.put(GatewayConstants.IAM_HOSTNAME, "localhost");
        urlMap.put(GatewayConstants.IAM_DCR_URL, "dcr/register");
        urlMap.put(GatewayConstants.TOKEN_URL, "/token");
        urlMap.put(GatewayConstants.APP_CREATE_URL, "/appCreate");
        urlMap.put(GatewayConstants.KEY_MAP_URL, "/keyMap/application-id");
        urlMap.put(GatewayConstants.API_RETRIEVE_URL, "/apis");
        urlMap.put(GatewayConstants.API_SUBSCRIBE_URL, "/subscriptions/multiple");
        urlMap.put(GatewayConstants.API_GET_SUBSCRIBED, "/subscriptions");

        configMap.put(GatewayConstants.REQUEST_ROUTER,
                "com.wso2.openbanking.accelerator.gateway.executor.core.DefaultRequestRouter");
        configMap.put(GatewayConstants.GATEWAY_CACHE_EXPIRY, "1");
        configMap.put(GatewayConstants.GATEWAY_CACHE_MODIFIEDEXPIRY, "1");
        configMap.put(OpenBankingConstants.STORE_HOSTNAME, "localhost");
        configMap.put(OpenBankingConstants.TOKEN_ENDPOINT, "/token");
        configMap.put(OpenBankingConstants.APIM_APPCREATION, "/appCreation");
        configMap.put(OpenBankingConstants.APIM_KEYGENERATION, "/keygeneration");
        configMap.put(OpenBankingConstants.APIM_GETAPIS, "/getAPIs");
        configMap.put(OpenBankingConstants.APIM_SUBSCRIBEAPIS, "/subscribe");
        configMap.put(OpenBankingConstants.APIM_GETSUBSCRIPTIONS, "/getSubscriptions");
        configMap.put(OpenBankingConstants.OB_KM_NAME, "OBKM");
        Mockito.when(openBankingConfigurationService.getConfigurations()).thenReturn(configMap);

        List<String> dcrRoles = new ArrayList<>();
        dcrRoles.add("AISP");
        dcrRoles.add("PISP");
        List<String> accountRoles = new ArrayList<>();
        accountRoles.add("AISP");
        configuredAPIList.put("DynamicClientRegistration", dcrRoles);
        configuredAPIList.put("AccountandTransactionAPI", accountRoles);
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    String isDcrResponse = "{\"client_name\":\"application_test\",\"client_id\":\"provided_client_id0001\"," +
            "\"client_secret\":\"provided_client_secret0001\",\"redirect_uris\":[\"\"]}";

    String tokenResponse = "{\n" +
            "    \"access_token\": \"sdsdfcdvvfvfvfv\",\n" +
            "    \"scope\": \"accounts cdr-register\",\n" +
            "    \"token_type\": \"Bearer\",\n" +
            "    \"expires_in\": 3600\n" +
            "}";
    String publishedAPIResponse = "{\n" +
            "   \"count\":2,\n" +
            "   \"list\":[\n" +
            "      {\n" +
            "         \"id\":\"01234567-0123-0123-0123-012345678901\",\n" +
            "         \"name\":\"DynamicClientRegistration\"\n" +
            "      },\n" +
            "      {\n" +
            "         \"id\":\"2962f3bb-8330-438e-baee-0ee1d6434ba4\",\n" +
            "         \"name\":\"AccountandTransactionAPI\"\n" +
            "      }\n" +
            "   ],\n" +
            "   \"pagination\":{\n" +
            "      \"offset\":2,\n" +
            "      \"limit\":2,\n" +
            "      \"total\":10,\n" +
            "      \"next\":\"/apis?limit=2&offset=4\",\n" +
            "      \"previous\":\"/apis?limit=2&offset=0\"\n" +
            "   }\n" +
            "}";

    String subscriptionResponse = "[\n" +
            "   {\n" +
            "      \"subscriptionId\":\"faae5fcc-cbae-40c4-bf43-89931630d313\",\n" +
            "      \"applicationId\":\"b3ade481-30b0-4b38-9a67-498a40873a6d\",\n" +
            "      \"apiId\":\"2962f3bb-8330-438e-baee-0ee1d6434ba4\",\n" +
            "      \"apiInfo\":{\n" +
            "         \"id\":\"01234567-0123-0123-0123-012345678901\",\n" +
            "         \"name\":\"DynamicClientRegistration\"\n" +
            "      },\n" +
            "      \"applicationInfo\":{\n" +
            "         \"applicationId\":\"01234567-0123-0123-0123-012345678901\",\n" +
            "         \"name\":\"DynamicClientRegistration\",\n" +
            "         \"throttlingPolicy\":\"Unlimited\",\n" +
            "         \"description\":\"Sample calculator application\",\n" +
            "         \"status\":\"APPROVED\",\n" +
            "         \"groups\":\"\",\n" +
            "         \"subscriptionCount\":0,\n" +
            "         \"attributes\":\"External Reference ID, Billing Tier\",\n" +
            "         \"owner\":\"admin\"\n" +
            "      },\n" +
            "      \"throttlingPolicy\":\"Unlimited\",\n" +
            "      \"requestedThrottlingPolicy\":\"Unlimited\",\n" +
            "      \"status\":\"UNBLOCKED\",\n" +
            "      \"redirectionParams\":\"\"\n" +
            "   }\n" +
            "]";

    String applicationSearchResponse = "{ \"count\": 1, \"list\": " +
            "[ { \"applicationId\": \"01234567-0123-0123-0123-012345678901\", " +
            "\"name\": \"CalculatorApp\", \"throttlingPolicy\": \"Unlimited\", " +
            "\"description\": \"Sample calculator application\", \"status\": \"APPROVED\", " +
            "\"groups\": \"\", \"subscriptionCount\": 0," +
            " \"attributes\": \"External Reference ID, Billing Tier\", " +
            "\"owner\": \"admin\" } ], " +
            "\"pagination\": { \"offset\": 0, \"limit\": 1, \"total\": 10, \"next\": \"\", \"previous\": \"\" } }";

    String createdApplicationResponse = "{\n" +
            "  \"applicationId\": \"01234567-0123-0123-0123-012345678901\",\n" +
            "  \"name\": \"CalculatorApp\"\n" +
            "}";
    String keyMapResponse = "{\n" +
            "  \"keyMappingId\": \"92ab520c-8847-427a-a921-3ed19b15aad7\",\n" +
            "  \"keyManager\": \"Resident Key Manager\",\n" +
            "  \"consumerKey\": \"vYDoc9s7IgAFdkSyNDaswBX7ejoa\",\n" +
            "  \"consumerSecret\": \"TIDlOFkpzB7WjufO3OJUhy1fsvAa\"\n" +
            "}";

    String softwareStatement = "eyJhbGciOiJQUzI1NiIsImtpZCI6IkR3TUtkV01tajdQV2ludm9xZlF5WFZ6eVo2USIsInR5cCI6IkpXVCJ9." +
            "eyJpc3MiOiJjZHItcmVnaXN0ZXIiLCJpYXQiOjE1NzE4MDgxNjcsImV4cCI6MjE0NzQ4MzY0NiwianRpIjoiM2JjMjA1YTFlYmM5NDNm" +
            "YmI2MjRiMTRmY2IyNDExOTYiLCJvcmdfaWQiOiIzQjBCMEE3Qi0zRTdCLTRBMkMtOTQ5Ny1FMzU3QTcxRDA3QzgiLCJvcmdfbmFtZSI6" +
            "Ik1vY2sgQ29tcGFueSBJbmMuIiwiY2xpZW50X25hbWUiOiJNb2NrIFNvZnR3YXJlIE5ldyIsImNsaWVudF9kZXNjcmlwdGlvbiI6IkEg" +
            "bW9jayBzb2Z0d2FyZSBwcm9kdWN0IGZvciB0ZXN0aW5nIFNTQSIsImNsaWVudF91cmkiOiJodHRwczovL3d3dy5tb2NrY29tcGFueS5j" +
            "b20uYXUiLCJyZWRpcmVjdF91cmlzIjpbImh0dHBzOi8vd3d3Lmdvb2dsZS5jb20vcmVkaXJlY3RzL3JlZGlyZWN0MSIsImh0dHBzOi8v" +
            "d3d3Lmdvb2dsZS5jb20vcmVkaXJlY3RzL3JlZGlyZWN0MiJdLCJsb2dvX3VyaSI6Imh0dHBzOi8vd3d3Lm1vY2tjb21wYW55LmNvbS5h" +
            "dS9sb2dvcy9sb2dvMS5wbmciLCJ0b3NfdXJpIjoiaHR0cHM6Ly93d3cubW9ja2NvbXBhbnkuY29tLmF1L3Rvcy5odG1sIiwicG9saWN5" +
            "X3VyaSI6Imh0dHBzOi8vd3d3Lm1vY2tjb21wYW55LmNvbS5hdS9wb2xpY3kuaHRtbCIsImp3a3NfdXJpIjoiaHR0cHM6Ly9rZXlzdG9y" +
            "ZS5vcGVuYmFua2luZ3Rlc3Qub3JnLnVrLzAwMTU4MDAwMDFIUVFyWkFBWC85YjV1c0RwYk50bXhEY1R6czdHektwLmp3a3MiLCJyZXZv" +
            "Y2F0aW9uX3VyaSI6Imh0dHBzOi8vZ2lzdC5naXRodWJ1c2VyY29udGVudC5jb20vaW1lc2g5NC8zMTcyZTJlNDU3NTdjZGEwOGVjMjcy" +
            "N2Y5MGI3MmNlZC9yYXcvZmYwZDNlYWJlNGNkZGNlNDdlZWMwMjI4ZjU5MjE3NTIyM2RkOTJiMi93c28yLWF1LWRjci1kZW1vLmp3a3Mi" +
            "LCJyZWNpcGllbnRfYmFzZV91cmkiOiJodHRwczovL3d3dy5tb2NrY29tcGFueS5jb20uYXUiLCJzb2Z0d2FyZV9pZCI6InRlc3QxMjM0" +
            "Iiwic29mdHdhcmVfcm9sZXMiOiJkYXRhLXJlY2lwaWVudC1zb2Z0d2FyZS1wcm9kdWN0IEFJU1AiLCJzY29wZSI6ImJhbms6YWNjb3Vu" +
            "dHMuYmFzaWM6cmVhZCBiYW5rOmFjY291bnRzLmRldGFpbDpyZWFkIGJhbms6dHJhbnNhY3Rpb25zOnJlYWQgYmFuazpwYXllZXM6cmVh" +
            "ZCBiYW5rOnJlZ3VsYXJfcGF5bWVudHM6cmVhZCBjb21tb246Y3VzdG9tZXIuYmFzaWM6cmVhZCBjb21tb246Y3VzdG9tZXIuZGV0YWls" +
            "OnJlYWQgY2RyOnJlZ2lzdHJhdGlvbiJ9.O5xHyhgOyAcTyXLqaUD9O2Iz-Dv5i3_P-ADw1A7PrMZV9j8JdrvY0n0QfhV0YKhmiSTYtII" +
            "RCFB_9EchBpnfPeVW4AJ9wt-JpQ2_TWCDSnGIlKb0fmepQkbcQmSRvecFpuECFWUIab6rDOz8IOMMuRXZrwghn3LaP5gKbbDT2NhCp0C" +
            "GjBZ2RwriIEx4NZjLBXP4RIw7ZhicOdXL3_544vFs6rOs6IjEkK1z9pHaBfyU0j7BRNcCwPL0Y9_zo4VpZ81Bd8IB_AxIpRNOLcpsa5c" +
            "c9oD5B-bqqTeWAkI_INjTlDXf-Rq5bBs7ldkuHh0fRNbI0gIyrpT_VyRL3IKIlw";

    String dcrResponsePayload = "{\n" +
            "    \"software_id\": \"test1234\",\n" +
            "    \"software_statement\": \"eyJhbGciOiJQUzI1NiIsImtpZCI6IkR3TUtkV01tajdQV2ludm9xZlF5WFZ6eVo2U" +
            "SIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJjZHItcmVnaXN0ZXIiLCJpYXQiOjE1NzE4MDgxNjcsImV4cCI6MjE0NzQ4MzY0Ni" +
            "wianRpIjoiM2JjMjA1YTFlYmM5NDNmYmI2MjRiMTRmY2IyNDExOTYiLCJvcmdfaWQiOiIzQjBCMEE3Qi0zRTdCLTRBMkMtO" +
            "TQ5Ny1FMzU3QTcxRDA3QzgiLCJvcmdfbmFtZSI6Ik1vY2sgQ29tcGFueSBJbmMuIiwiY2xpZW50X25hbWUiOiJNb2NrIFNv" +
            "ZnR3YXJlIE5ldyIsImNsaWVudF9kZXNjcmlwdGlvbiI6IkEgbW9jayBzb2Z0d2FyZSBwcm9kdWN0IGZvciB0ZXN0aW5nIFNT" +
            "QSIsImNsaWVudF91cmkiOiJodHRwczovL3d3dy5tb2NrY29tcGFueS5jb20uYXUiLCJyZWRpcmVjdF91cmlzIjpbImh0dHB" +
            "zOi8vd3d3Lmdvb2dsZS5jb20vcmVkaXJlY3RzL3JlZGlyZWN0MSIsImh0dHBzOi8vd3d3Lmdvb2dsZS5jb20vcmVkaXJlY3" +
            "RzL3JlZGlyZWN0MiJdLCJsb2dvX3VyaSI6Imh0dHBzOi8vd3d3Lm1vY2tjb21wYW55LmNvbS5hdS9sb2dvcy9sb2dvMS5wb" +
            "mciLCJ0b3NfdXJpIjoiaHR0cHM6Ly93d3cubW9ja2NvbXBhbnkuY29tLmF1L3Rvcy5odG1sIiwicG9saWN5X3VyaSI6Imh0" +
            "dHBzOi8vd3d3Lm1vY2tjb21wYW55LmNvbS5hdS9wb2xpY3kuaHRtbCIsImp3a3NfdXJpIjoiaHR0cHM6Ly9rZXlzdG9yZS5" +
            "vcGVuYmFua2luZ3Rlc3Qub3JnLnVrLzAwMTU4MDAwMDFIUVFyWkFBWC85YjV1c0RwYk50bXhEY1R6czdHektwLmp3a3MiLC" +
            "JyZXZvY2F0aW9uX3VyaSI6Imh0dHBzOi8vZ2lzdC5naXRodWJ1c2VyY29udGVudC5jb20vaW1lc2g5NC8zMTcyZTJlNDU3N" +
            "TdjZGEwOGVjMjcyN2Y5MGI3MmNlZC9yYXcvZmYwZDNlYWJlNGNkZGNlNDdlZWMwMjI4ZjU5MjE3NTIyM2RkOTJiMi93c28y" +
            "LWF1LWRjci1kZW1vLmp3a3MiLCJyZWNpcGllbnRfYmFzZV91cmkiOiJodHRwczovL3d3dy5tb2NrY29tcGFueS5jb20uYXU" +
            "iLCJzb2Z0d2FyZV9pZCI6InRlc3QxMjM0Iiwic29mdHdhcmVfcm9sZXMiOiJkYXRhLXJlY2lwaWVudC1zb2Z0d2FyZS1wcm" +
            "9kdWN0IEFJU1AiLCJzY29wZSI6ImJhbms6YWNjb3VudHMuYmFzaWM6cmVhZCBiYW5rOmFjY291bnRzLmRldGFpbDpyZWFkI" +
            "GJhbms6dHJhbnNhY3Rpb25zOnJlYWQgYmFuazpwYXllZXM6cmVhZCBiYW5rOnJlZ3VsYXJfcGF5bWVudHM6cmVhZCBjb21t" +
            "b246Y3VzdG9tZXIuYmFzaWM6cmVhZCBjb21tb246Y3VzdG9tZXIuZGV0YWlsOnJlYWQgY2RyOnJlZ2lzdHJhdGlvbiJ9.O5" +
            "xHyhgOyAcTyXLqaUD9O2Iz-Dv5i3_P-ADw1A7PrMZV9j8JdrvY0n0QfhV0YKhmiSTYtIIRCFB_9EchBpnfPeVW4AJ9wt-Jp" +
            "Q2_TWCDSnGIlKb0fmepQkbcQmSRvecFpuECFWUIab6rDOz8IOMMuRXZrwghn3LaP5gKbbDT2NhCp0CGjBZ2RwriIEx4NZjL" +
            "BXP4RIw7ZhicOdXL3_544vFs6rOs6IjEkK1z9pHaBfyU0j7BRNcCwPL0Y9_zo4VpZ81Bd8IB_AxIpRNOLcpsa5cc9oD5B-b" +
            "qqTeWAkI_INjTlDXf-Rq5bBs7ldkuHh0fRNbI0gIyrpT_VyRL3IKIlw\",\n" +
            "    \"grant_types\": [\n" +
            "        \"client_credentials\",\n" +
            "        \"authorization_code\",\n" +
            "        \"refresh_token\",\n" +
            "        \"urn:ietf:params:oauth:grant-type:jwt-bearer\"\n" +
            "    ],\n" +
            "    \"application_type\": \"web\",\n" +
            "    \"scope\": \"bank:accounts.basic:read bank:accounts.detail:read bank:transactions:read " +
            "bank:payees:read bank:regular_payments:read common:customer.basic:read common:customer.detail:read" +
            " cdr:registration\",\n" +
            "    \"client_id_issued_at\": 1619150285,\n" +
            "    \"redirect_uris\": [\n" +
            "        \"https://www.google.com/redirects/redirect1\",\n" +
            "        \"https://www.google.com/redirects/redirect2\"\n" +
            "    ],\n" +
            "    \"request_object_signing_alg\": \"PS256\",\n" +
            "    \"client_id\": \"uagAipmOU5quayzoznU1ddWg6tca\",\n" +
            "    \"token_endpoint_auth_method\": \"private_key_jwt\",\n" +
            "    \"response_types\": \"code id_token\",\n" +
            "    \"id_token_signed_response_alg\": \"PS256\"\n" +
            "}";

    @Test
    public void testFilterRegulatorAPIs() {

        Map<String, List<String>> configuredAPIList = new HashMap<>();
        List<String> dcrRoles = new ArrayList<>();
        dcrRoles.add("AISP");
        dcrRoles.add("PISP");
        List<String> accountRoles = new ArrayList<>();
        accountRoles.add("AISP");
        configuredAPIList.put("DynamicClientRegistration", dcrRoles);
        configuredAPIList.put("AccountandTransactionAPI", accountRoles);

        JsonArray publishedAPIs = new JsonArray();
        JsonObject dcrApi = new JsonObject();
        dcrApi.addProperty("id", "1");
        dcrApi.addProperty("name", "DynamicClientRegistration");
        publishedAPIs.add(dcrApi);

        DCRExecutor dcrExecutor = new DCRExecutor();
        List<String> filteredAPIList = dcrExecutor.filterRegulatorAPIs(configuredAPIList, publishedAPIs, dcrRoles);
        Assert.assertEquals(filteredAPIList.get(0), "1");
    }

    @Test
    public void testExtractApplicationName() throws ParseException {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(OpenBankingConstants.DCR_USE_SOFTWAREID_AS_APPNAME, "true");
        configMap.put(OpenBankingConstants.DCR_APPLICATION_NAME_KEY, "software_client_name");
        String applicationName = dcrExecutor.getApplicationName(dcrResponsePayload, configMap);
        Assert.assertEquals("test1234", applicationName);
    }

    @Test
    public void testExtractApplicationNameWithSoftwateIDEnabledFalse() throws ParseException {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put(OpenBankingConstants.DCR_USE_SOFTWAREID_AS_APPNAME, "false");
        configMap.put(OpenBankingConstants.DCR_APPLICATION_NAME_KEY, "client_name");
        String applicationName = dcrExecutor.getApplicationName(dcrResponsePayload, configMap);
        Assert.assertEquals("Mock Software New", applicationName);
    }

    @Test
    public void testExtractSoftwareRoles() throws ParseException {

        List<String> roles = dcrExecutor.getRolesFromSSA(softwareStatement);
        Assert.assertTrue(roles.contains("data-recipient-software-product"));
        Assert.assertTrue(roles.contains("AISP"));

    }

    @Test
    public void testGetUnauthorizedAPIs() {

        Map<String, List<String>> configuredAPIList = new HashMap<>();
        List<String> dcrRoles = new ArrayList<>();
        dcrRoles.add("AISP");
        dcrRoles.add("PISP");
        List<String> accountRoles = new ArrayList<>();
        accountRoles.add("AISP");
        configuredAPIList.put("DynamicClientRegistration", dcrRoles);
        configuredAPIList.put("AccountandTransactionAPI", accountRoles);

        JsonArray subscribedAPIs = new JsonArray();
        JsonObject dcrApi = new JsonObject();
        JsonObject apiInfo = new JsonObject();
        apiInfo.addProperty("name", "DynamicClientRegistration");
        dcrApi.add("apiInfo", apiInfo);
        dcrApi.addProperty("subscriptionId", "1");
        subscribedAPIs.add(dcrApi);

        List<String> allowedRoles = new ArrayList<>();
        allowedRoles.add("data-recipient-software-product");
        List<String> unAuthorizedAPIs = dcrExecutor
                .getUnAuthorizedAPIs(subscribedAPIs, configuredAPIList, allowedRoles);
        Assert.assertTrue(unAuthorizedAPIs.contains("1"));

    }

    @Test
    public void testNewAPIsToSubscribe() {

        List<String> allowedAPIs = new ArrayList<>();
        allowedAPIs.add("1");
        allowedAPIs.add("2");

        List<String> subscribedAPIs = new ArrayList<>();
        subscribedAPIs.add("1");

        List<String> apisToSubscribe = dcrExecutor.getNewAPIsToSubscribe(allowedAPIs, subscribedAPIs);
        Assert.assertTrue(apisToSubscribe.contains("2"));

        allowedAPIs.remove("2");
        apisToSubscribe = dcrExecutor.getNewAPIsToSubscribe(allowedAPIs, subscribedAPIs);
        Assert.assertTrue(apisToSubscribe.isEmpty());

        subscribedAPIs.remove("1");
        apisToSubscribe = dcrExecutor.getNewAPIsToSubscribe(allowedAPIs, subscribedAPIs);
        Assert.assertTrue(apisToSubscribe.contains("1"));

        allowedAPIs.add("2");
        apisToSubscribe = dcrExecutor.getNewAPIsToSubscribe(allowedAPIs, subscribedAPIs);
        Assert.assertTrue(apisToSubscribe.contains("1"));
        Assert.assertTrue(apisToSubscribe.contains("2"));
    }

    @Test
    public void testPostProcessResponseForRegister() throws Exception {

        OBAPIResponseContext obapiResponseContext = Mockito.mock(OBAPIResponseContext.class);
        MsgInfoDTO msgInfoDTO = Mockito.mock(MsgInfoDTO.class);
        DCRExecutor dcrExecutor = Mockito.spy(DCRExecutor.class);
        Mockito.doReturn(msgInfoDTO).when(obapiResponseContext).getMsgInfo();
        Mockito.doReturn(HttpMethod.POST).when(msgInfoDTO).getHttpMethod();
        Mockito.doReturn(HttpStatus.SC_CREATED).when(obapiResponseContext).getStatusCode();
        Mockito.doReturn(dcrResponsePayload).when(obapiResponseContext).getResponsePayload();

        Mockito.when(openBankingConfigurationService.getAllowedAPIs()).thenReturn(configuredAPIList);
        GatewayDataHolder.getInstance().setApiManagerConfiguration(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.doReturn("admin").when(apiManagerConfiguration).getFirstProperty(anyString());
        Mockito.doReturn("localhost/services").when(apiManagerConfiguration)
                .getFirstProperty("APIKeyValidator.ServerURL");

        PowerMockito.mockStatic(IdentityUtil.class);
        Mockito.when(IdentityUtil.getProperty("OAuth.OAuth2DCREPUrl")).thenReturn("localhost/api/dcr/register");
        Mockito.doReturn(jsonParser.parse(isDcrResponse)).when(dcrExecutor)
                .callPost(eq(urlMap.get(GatewayConstants.IAM_DCR_URL).toString()), anyString(), anyString());
        Mockito.doReturn(jsonParser.parse(tokenResponse)).when(dcrExecutor)
                .getToken(anyString(), eq(urlMap.get(GatewayConstants.TOKEN_URL).toString()), anyString());
        Mockito.doReturn(jsonParser.parse(isDcrResponse)).when(dcrExecutor)
                .callGet(eq("dcr/register/uagAipmOU5quayzoznU1ddWg6tca"),
                        anyString(), anyString(), anyString());
        Mockito.doReturn(jsonParser.parse(createdApplicationResponse)).when(dcrExecutor)
                .callPost(eq(urlMap.get(GatewayConstants.APP_CREATE_URL).toString()), anyString(), anyString());
        Mockito.doReturn(jsonParser.parse(keyMapResponse)).when(dcrExecutor)
                .callPost(eq("/keyMap/01234567-0123-0123-0123-012345678901"), anyString(), anyString());
        Mockito.doReturn(jsonParser.parse(publishedAPIResponse)).when(dcrExecutor)
                .callGet(eq(urlMap.get(GatewayConstants.API_RETRIEVE_URL).toString()), anyString(), anyString(),
                        anyString());
        Mockito.doReturn(jsonParser.parse(subscriptionResponse)).when(dcrExecutor)
                .callPost(eq(urlMap.get(GatewayConstants.API_SUBSCRIBE_URL).toString()), anyString(), anyString());
        Mockito.doReturn(true).when(dcrExecutor)
                .callDelete(eq("dcr/register/provided_client_id0001"), anyString());
        GatewayDataHolder.getInstance().setOpenBankingConfigurationService(openBankingConfigurationService);
        DCRExecutor.setUrlMap(urlMap);
        dcrExecutor.postProcessResponse(obapiResponseContext);
        verify(obapiResponseContext, times(0)).setError(true);
    }

    @Test
    public void testPostProcessResponseForUpdate() throws IOException, OpenBankingException, URISyntaxException {

        String subscribedAPIResponse = "{ \"count\": 1, \"list\": " +
                "[ { \"subscriptionId\": \"faae5fcc-cbae-40c4-bf43-89931630d313\", " +
                "\"applicationId\": \"b3ade481-30b0-4b38-9a67-498a40873a6d\", " +
                "\"apiId\": \"2962f3bb-8330-438e-baee-0ee1d6434ba4\", " +
                "\"apiInfo\": { \"id\": \"01234567-0123-0123-0123-012345678901\", " +
                "\"name\": \"AccountandTransactionAPI\"," +
                " \"description\": \"A calculator API that supports basic operations\", " +
                "\"context\": \"CalculatorAPI\", \"version\": \"1.0.0\", \"type\": \"WS\", " +
                "\"provider\": \"admin\", \"lifeCycleStatus\": \"PUBLISHED\", " +
                "\"thumbnailUri\": \"/apis/01234567-0123-0123-0123-012345678901/thumbnail\", " +
                "\"avgRating\": 4.5, \"throttlingPolicies\": [ \"Unlimited\", \"Bronze\" ], " +
                "\"advertiseInfo\": { \"advertised\": true, \"originalStoreUrl\": \"https://localhost:9443/store\", " +
                "\"apiOwner\": \"admin\" }, " +
                "\"businessInformation\": { \"businessOwner\": \"businessowner\", \"businessOwnerEmail\": " +
                "\"businessowner@wso2.com\", \"technicalOwner\": \"technicalowner\"," +
                " \"technicalOwnerEmail\": \"technicalowner@wso2.com\" }, \"isSubscriptionAvailable\": false, " +
                "\"monetizationLabel\": \"Free\" }, " +
                "\"applicationInfo\": { \"applicationId\": \"01234567-0123-0123-0123-012345678901\"," +
                " \"name\": \"CalculatorApp\", \"throttlingPolicy\": \"Unlimited\", " +
                "\"description\": \"Sample calculator application\", \"status\": \"APPROVED\", " +
                "\"groups\": \"\", \"subscriptionCount\": 0, \"attributes\": \"External Reference ID, Billing Tier\"," +
                " \"owner\": \"admin\" }, \"throttlingPolicy\": \"Unlimited\", \"requestedThrottlingPolicy\":" +
                " \"Unlimited\", \"status\": \"UNBLOCKED\", \"redirectionParams\": \"\" } ], " +
                "\"pagination\": { \"offset\": 0, \"limit\": 1, \"total\": 10, \"next\": \"\", \"previous\": \"\" } }";

        OBAPIResponseContext obapiResponseContext = Mockito.mock(OBAPIResponseContext.class);
        MsgInfoDTO msgInfoDTO = Mockito.mock(MsgInfoDTO.class);
        DCRExecutor dcrExecutor = Mockito.spy(DCRExecutor.class);
        Mockito.doReturn(msgInfoDTO).when(obapiResponseContext).getMsgInfo();
        Mockito.doReturn(HttpMethod.PUT).when(msgInfoDTO).getHttpMethod();
        Mockito.doReturn(HttpStatus.SC_OK).when(obapiResponseContext).getStatusCode();
        Mockito.doReturn(dcrResponsePayload).when(obapiResponseContext).getResponsePayload();

        PowerMockito.mockStatic(IdentityUtil.class);
        Mockito.when(IdentityUtil.getProperty("OAuth.OAuth2DCREPUrl")).thenReturn("localhost/api/dcr/register");
        GatewayDataHolder.getInstance().setOpenBankingConfigurationService(openBankingConfigurationService);
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(OpenBankingConstants.DCR_USE_SOFTWAREID_AS_APPNAME, true);
        configMap.put(OpenBankingConstants.DCR_APPLICATION_NAME_KEY, "software_name");
        Mockito.when(openBankingConfigurationService.getConfigurations()).thenReturn(configMap);
        Mockito.when(openBankingConfigurationService.getAllowedAPIs()).thenReturn(configuredAPIList);

        Mockito.doReturn(jsonParser.parse(isDcrResponse)).when(dcrExecutor)
                .callPost(eq(urlMap.get(GatewayConstants.IAM_DCR_URL).toString()), anyString(), anyString());
        Mockito.doReturn(jsonParser.parse(tokenResponse)).when(dcrExecutor)
                .getToken(anyString(), eq(urlMap.get(GatewayConstants.TOKEN_URL).toString()), anyString());
        Mockito.doReturn(jsonParser.parse(applicationSearchResponse)).when(dcrExecutor)
                .callGet(eq(urlMap.get(GatewayConstants.APP_CREATE_URL).toString()),
                        anyString(), anyString(), anyString());
        Mockito.doReturn(jsonParser.parse(subscribedAPIResponse)).when(dcrExecutor)
                .callGet(eq(urlMap.get(GatewayConstants.API_GET_SUBSCRIBED).toString()),
                        anyString(), anyString(), anyString());
        Mockito.doReturn(jsonParser.parse(publishedAPIResponse)).when(dcrExecutor)
                .callGet(eq(urlMap.get(GatewayConstants.API_RETRIEVE_URL).toString()), anyString(), anyString(),
                        anyString());
        Mockito.doReturn(jsonParser.parse(subscriptionResponse)).when(dcrExecutor)
                .callPost(eq(urlMap.get(GatewayConstants.API_SUBSCRIBE_URL).toString()), anyString(), anyString());
        Mockito.doReturn(true).when(dcrExecutor)
                .callDelete(eq("dcr/register/provided_client_id0001"), anyString());
        DCRExecutor.setUrlMap(urlMap);
        dcrExecutor.postProcessResponse(obapiResponseContext);
        verify(obapiResponseContext, times(0)).setError(true);
    }

    @Test
    public void testPostProcessResponseForDelete() throws Exception {

        OBAPIResponseContext obapiResponseContext = Mockito.mock(OBAPIResponseContext.class);
        APIRequestInfoDTO apiRequestInfoDTO = new APIRequestInfoDTO();
        apiRequestInfoDTO.setConsumerKey("clientId");
        MsgInfoDTO msgInfoDTO = Mockito.mock(MsgInfoDTO.class);
        DCRExecutor dcrExecutor = Mockito.spy(DCRExecutor.class);
        GatewayCache gatewayCache = Mockito.mock(GatewayCache.class);

        PowerMockito.mockStatic(IdentityUtil.class);

        Mockito.when(IdentityUtil.getProperty("OAuth.OAuth2DCREPUrl")).thenReturn("localhost/api/dcr/register");
        Mockito.doReturn(msgInfoDTO).when(obapiResponseContext).getMsgInfo();
        Mockito.doReturn(HttpMethod.DELETE).when(msgInfoDTO).getHttpMethod();
        Mockito.doReturn(HttpStatus.SC_NO_CONTENT).when(obapiResponseContext).getStatusCode();
        Mockito.doReturn(dcrResponsePayload).when(obapiResponseContext).getResponsePayload();
        Mockito.doReturn(apiRequestInfoDTO).when(obapiResponseContext).getApiRequestInfo();
        Mockito.when(openBankingConfigurationService.getAllowedAPIs()).thenReturn(configuredAPIList);
        GatewayDataHolder.getInstance().setApiManagerConfiguration(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.doReturn("admin").when(apiManagerConfiguration).getFirstProperty(anyString());
        Mockito.doReturn("localhost/services").when(apiManagerConfiguration)
                .getFirstProperty("APIKeyValidator.ServerURL");
        GatewayDataHolder.getInstance().setOpenBankingConfigurationService(openBankingConfigurationService);
        WhiteboxImpl.invokeMethod(GatewayDataHolder.getInstance(), "setGatewayCache", gatewayCache);
        //GatewayDataHolder.setGatewayCache(gatewayCache);

        Mockito.doReturn("application").when(gatewayCache).getFromCache(GatewayCacheKey.of(anyString()));
        Mockito.doReturn(jsonParser.parse(isDcrResponse)).when(dcrExecutor)
                .callPost(eq(urlMap.get(GatewayConstants.IAM_DCR_URL).toString()), anyString(), anyString());
        Mockito.doReturn(jsonParser.parse(tokenResponse)).when(dcrExecutor)
                .getToken(anyString(), eq(urlMap.get(GatewayConstants.TOKEN_URL).toString()), anyString());
        Mockito.doReturn(jsonParser.parse(applicationSearchResponse)).when(dcrExecutor)
                .callGet(eq(urlMap.get(GatewayConstants.APP_CREATE_URL).toString()),
                        anyString(), anyString(), anyString());
        Mockito.doReturn(true).when(dcrExecutor).callDelete(anyString(), anyString());
        DCRExecutor.setUrlMap(urlMap);
        dcrExecutor.postProcessResponse(obapiResponseContext);
        verify(obapiResponseContext, times(0)).setError(true);
    }

    @Test
    public void testPostProcessRequestInvalidAuthenticationDCR() {

        OBAPIRequestContext obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        APIRequestInfoDTO apiRequestInfoDTO = new APIRequestInfoDTO();
        apiRequestInfoDTO.setConsumerKey("clientId");
        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        DCRExecutor dcrExecutor = Mockito.spy(DCRExecutor.class);

        msgInfoDTO.setHttpMethod(HttpMethod.GET);
        msgInfoDTO.setResource("/register/1234");
        apiRequestInfoDTO.setConsumerKey("123");
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put(GatewayConstants.CONTENT_TYPE_TAG, "application/jwt");
        requestHeaders.put(GatewayConstants.CONTENT_LENGTH, "1000");
        msgInfoDTO.setHeaders(requestHeaders);
        RequestContextDTO requestContextDTO = new RequestContextDTO();
        requestContextDTO.setApiRequestInfo(apiRequestInfoDTO);
        requestContextDTO.setMsgInfo(msgInfoDTO);

        Mockito.doReturn(msgInfoDTO).when(obapiRequestContext).getMsgInfo();
        Mockito.doReturn(apiRequestInfoDTO).when(obapiRequestContext).getApiRequestInfo();

        dcrExecutor.postProcessRequest(obapiRequestContext);
        verify(obapiRequestContext).setError(true);
    }

    @Test
    public void testPostProcessWithInvalidAuthenticationDCRApplicationAccessToken() throws Exception {

        OBAPIRequestContext obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        GatewayCache gatewayCache = Mockito.mock(GatewayCache.class);
        DCRExecutor dcrExecutor = Mockito.spy(DCRExecutor.class);
        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        Map<String, String> headers = new HashMap<>();
        headers.put(GatewayConstants.AUTH_HEADER, "");
        msgInfoDTO.setHeaders(headers);
        msgInfoDTO.setResource("/register/123");
        msgInfoDTO.setHttpMethod(HttpMethod.DELETE);
        APIRequestInfoDTO apiRequestInfoDTO = new APIRequestInfoDTO();
        apiRequestInfoDTO.setConsumerKey("123");
        DCRExecutor.setUrlMap(urlMap);
        WhiteboxImpl.invokeMethod(GatewayDataHolder.getInstance(), "setGatewayCache", gatewayCache);

        Mockito.doReturn(msgInfoDTO).when(obapiRequestContext).getMsgInfo();
        Mockito.doReturn(apiRequestInfoDTO).when(obapiRequestContext).getApiRequestInfo();
        Mockito.doReturn(jsonParser.parse(isDcrResponse)).when(dcrExecutor)
                .callGet(anyString(), anyString(), anyString(), anyString());
        Mockito.doNothing().when(gatewayCache).addToCache(anyObject(), anyObject());

        dcrExecutor.postProcessRequest(obapiRequestContext);
        verify(gatewayCache).addToCache(anyObject(), anyObject());
    }

    @Test
    public void testAddApplicationNameToCacheInDelete() throws Exception {

        OBAPIRequestContext obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        GatewayCache gatewayCache = Mockito.mock(GatewayCache.class);
        DCRExecutor dcrExecutor = Mockito.spy(DCRExecutor.class);
        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setResource("/register/123");
        msgInfoDTO.setHttpMethod(HttpMethod.DELETE);
        APIRequestInfoDTO apiRequestInfoDTO = new APIRequestInfoDTO();
        apiRequestInfoDTO.setConsumerKey("123");
        DCRExecutor.setUrlMap(urlMap);
        WhiteboxImpl.invokeMethod(GatewayDataHolder.getInstance(), "setGatewayCache", gatewayCache);
        //GatewayDataHolder.setGatewayCache(gatewayCache);

        Mockito.doReturn(msgInfoDTO).when(obapiRequestContext).getMsgInfo();
        Mockito.doReturn(apiRequestInfoDTO).when(obapiRequestContext).getApiRequestInfo();
        Mockito.doReturn(jsonParser.parse(isDcrResponse)).when(dcrExecutor)
                .callGet(anyString(), anyString(), anyString(), anyString());
        Mockito.doNothing().when(gatewayCache).addToCache(anyObject(), anyObject());

        dcrExecutor.postProcessRequest(obapiRequestContext);
        verify(gatewayCache).addToCache(anyObject(), anyObject());
    }

    @Test
    public void testErrorScenarios() throws IOException, OpenBankingException, URISyntaxException {

        OBAPIResponseContext obapiResponseContext = Mockito.mock(OBAPIResponseContext.class);
        MsgInfoDTO msgInfoDTO = Mockito.mock(MsgInfoDTO.class);
        DCRExecutor dcrExecutor = Mockito.spy(DCRExecutor.class);
        Mockito.doReturn(msgInfoDTO).when(obapiResponseContext).getMsgInfo();
        Mockito.doReturn(HttpMethod.POST).when(msgInfoDTO).getHttpMethod();
        Mockito.doReturn(HttpStatus.SC_CREATED).when(obapiResponseContext).getStatusCode();
        Mockito.doReturn(dcrResponsePayload).when(obapiResponseContext).getResponsePayload();

        Mockito.when(openBankingConfigurationService.getAllowedAPIs()).thenReturn(configuredAPIList);
        GatewayDataHolder.getInstance().setApiManagerConfiguration(apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.doReturn("admin").when(apiManagerConfiguration).getFirstProperty(anyString());
        Mockito.doReturn("localhost/services").when(apiManagerConfiguration)
                .getFirstProperty("APIKeyValidator.ServerURL");
        PowerMockito.mockStatic(IdentityUtil.class);
        Mockito.when(IdentityUtil.getProperty("OAuth.OAuth2DCREPUrl")).thenReturn("localhost/api/dcr/register");

        //when sp creation fails for token generation
        Mockito.doReturn(null).when(dcrExecutor)
                .callPost(eq(urlMap.get(GatewayConstants.IAM_DCR_URL).toString()), anyString(), anyString());
        Mockito.doReturn(true).when(dcrExecutor)
                .callDelete(eq("localhost/api/openbanking/dynamic-client-registration/register/" +
                        "uagAipmOU5quayzoznU1ddWg6tca"), anyString());

        GatewayDataHolder.getInstance().setOpenBankingConfigurationService(openBankingConfigurationService);
        DCRExecutor.setUrlMap(urlMap);
        dcrExecutor.postProcessResponse(obapiResponseContext);
        verify(obapiResponseContext).setError(true);

        //when token call fails
        Mockito.doReturn(jsonParser.parse(isDcrResponse)).when(dcrExecutor)
                .callPost(eq(urlMap.get(GatewayConstants.IAM_DCR_URL).toString()), anyString(), anyString());
        Mockito.doReturn(null).when(dcrExecutor)
                .getToken(anyString(), eq(urlMap.get(GatewayConstants.TOKEN_URL).toString()), anyString());
        Mockito.doReturn(true).when(dcrExecutor)
                .callDelete(eq("dcr/register/provided_client_id0001"), anyString());
        dcrExecutor.postProcessResponse(obapiResponseContext);
        verify(obapiResponseContext, times(2)).setError(true);

        //when retrieving client id and secret fails
        Mockito.doReturn(jsonParser.parse(isDcrResponse)).when(dcrExecutor)
                .callPost(eq(urlMap.get(GatewayConstants.IAM_DCR_URL).toString()), anyString(), anyString());
        Mockito.doReturn(jsonParser.parse(tokenResponse)).when(dcrExecutor)
                .getToken(anyString(), eq(urlMap.get(GatewayConstants.TOKEN_URL).toString()), anyString());
        Mockito.doReturn(null).when(dcrExecutor)
                .callGet(eq("dcr/register/uagAipmOU5quayzoznU1ddWg6tca"),
                        anyString(), anyString(), anyString());
        dcrExecutor.postProcessResponse(obapiResponseContext);
        verify(obapiResponseContext, times(3)).setError(true);

        //when AM application creation fails
        Mockito.doReturn(jsonParser.parse(isDcrResponse)).when(dcrExecutor)
                .callPost(eq(urlMap.get(GatewayConstants.IAM_DCR_URL).toString()), anyString(), anyString());
        Mockito.doReturn(jsonParser.parse(tokenResponse)).when(dcrExecutor)
                .getToken(anyString(), eq(urlMap.get(GatewayConstants.TOKEN_URL).toString()), anyString());
        Mockito.doReturn(jsonParser.parse(isDcrResponse)).when(dcrExecutor)
                .callGet(eq("dcr/register/uagAipmOU5quayzoznU1ddWg6tca"),
                        anyString(), anyString(), anyString());
        Mockito.doReturn(null).when(dcrExecutor)
                .callPost(eq(urlMap.get(GatewayConstants.APP_CREATE_URL).toString()), anyString(), anyString());
        dcrExecutor.postProcessResponse(obapiResponseContext);
        verify(obapiResponseContext, times(4)).setError(true);

        //when key map response is null
        Mockito.doReturn(jsonParser.parse(isDcrResponse)).when(dcrExecutor)
                .callPost(eq(urlMap.get(GatewayConstants.IAM_DCR_URL).toString()), anyString(), anyString());
        Mockito.doReturn(jsonParser.parse(tokenResponse)).when(dcrExecutor)
                .getToken(anyString(), eq(urlMap.get(GatewayConstants.TOKEN_URL).toString()), anyString());
        Mockito.doReturn(jsonParser.parse(isDcrResponse)).when(dcrExecutor)
                .callGet(eq("dcr/register/uagAipmOU5quayzoznU1ddWg6tca"),
                        anyString(), anyString(), anyString());
        Mockito.doReturn(jsonParser.parse(createdApplicationResponse)).when(dcrExecutor)
                .callPost(eq(urlMap.get(GatewayConstants.APP_CREATE_URL).toString()), anyString(), anyString());
        Mockito.doReturn(null).when(dcrExecutor)
                .callPost(eq("/keyMap/01234567-0123-0123-0123-012345678901"), anyString(), anyString());
        ///appCreate/01234567-0123-0123-0123-012345678901
        Mockito.doReturn(true).when(dcrExecutor)
                .callDelete(eq("/appCreate/01234567-0123-0123-0123-012345678901"), anyString());
        dcrExecutor.postProcessResponse(obapiResponseContext);
        verify(obapiResponseContext, times(5)).setError(true);

        //when retrieving published apis get a null response
        Mockito.doReturn(jsonParser.parse(isDcrResponse)).when(dcrExecutor)
                .callPost(eq(urlMap.get(GatewayConstants.IAM_DCR_URL).toString()), anyString(), anyString());
        Mockito.doReturn(jsonParser.parse(tokenResponse)).when(dcrExecutor)
                .getToken(anyString(), eq(urlMap.get(GatewayConstants.TOKEN_URL).toString()), anyString());
        Mockito.doReturn(jsonParser.parse(isDcrResponse)).when(dcrExecutor)
                .callGet(eq("dcr/register/uagAipmOU5quayzoznU1ddWg6tca"),
                        anyString(), anyString(), anyString());
        Mockito.doReturn(jsonParser.parse(createdApplicationResponse)).when(dcrExecutor)
                .callPost(eq(urlMap.get(GatewayConstants.APP_CREATE_URL).toString()), anyString(), anyString());
        Mockito.doReturn(jsonParser.parse(keyMapResponse)).when(dcrExecutor)
                .callPost(eq("/keyMap/01234567-0123-0123-0123-012345678901"), anyString(), anyString());
        Mockito.doReturn(null).when(dcrExecutor)
                .callGet(eq(urlMap.get(GatewayConstants.API_RETRIEVE_URL).toString()), anyString(), anyString(),
                        anyString());
        dcrExecutor.postProcessResponse(obapiResponseContext);
        verify(obapiResponseContext, times(6)).setError(true);


        //when subscribing to APIs get null response
        Mockito.doReturn(jsonParser.parse(isDcrResponse)).when(dcrExecutor)
                .callPost(eq(urlMap.get(GatewayConstants.IAM_DCR_URL).toString()), anyString(), anyString());
        Mockito.doReturn(jsonParser.parse(tokenResponse)).when(dcrExecutor)
                .getToken(anyString(), eq(urlMap.get(GatewayConstants.TOKEN_URL).toString()), anyString());
        Mockito.doReturn(jsonParser.parse(isDcrResponse)).when(dcrExecutor)
                .callGet(eq("dcr/register/uagAipmOU5quayzoznU1ddWg6tca"),
                        anyString(), anyString(), anyString());
        Mockito.doReturn(jsonParser.parse(createdApplicationResponse)).when(dcrExecutor)
                .callPost(eq(urlMap.get(GatewayConstants.APP_CREATE_URL).toString()), anyString(), anyString());
        Mockito.doReturn(jsonParser.parse(keyMapResponse)).when(dcrExecutor)
                .callPost(eq("/keyMap/01234567-0123-0123-0123-012345678901"), anyString(), anyString());
        Mockito.doReturn(jsonParser.parse(publishedAPIResponse)).when(dcrExecutor)
                .callGet(eq(urlMap.get(GatewayConstants.API_RETRIEVE_URL).toString()), anyString(), anyString(),
                        anyString());
        Mockito.doReturn(null).when(dcrExecutor)
                .callPost(eq(urlMap.get(GatewayConstants.API_SUBSCRIBE_URL).toString()), anyString(), anyString());

        dcrExecutor.postProcessResponse(obapiResponseContext);
        verify(obapiResponseContext, times(7)).setError(true);

        //when deleting internal SP get false
        Mockito.doReturn(jsonParser.parse(isDcrResponse)).when(dcrExecutor)
                .callPost(eq(urlMap.get(GatewayConstants.IAM_DCR_URL).toString()), anyString(), anyString());
        Mockito.doReturn(jsonParser.parse(tokenResponse)).when(dcrExecutor)
                .getToken(anyString(), eq(urlMap.get(GatewayConstants.TOKEN_URL).toString()), anyString());
        Mockito.doReturn(jsonParser.parse(isDcrResponse)).when(dcrExecutor)
                .callGet(eq("dcr/register/uagAipmOU5quayzoznU1ddWg6tca"),
                        anyString(), anyString(), anyString());
        Mockito.doReturn(jsonParser.parse(createdApplicationResponse)).when(dcrExecutor)
                .callPost(eq(urlMap.get(GatewayConstants.APP_CREATE_URL).toString()), anyString(), anyString());
        Mockito.doReturn(jsonParser.parse(keyMapResponse)).when(dcrExecutor)
                .callPost(eq("/keyMap/01234567-0123-0123-0123-012345678901"), anyString(), anyString());
        Mockito.doReturn(jsonParser.parse(publishedAPIResponse)).when(dcrExecutor)
                .callGet(eq(urlMap.get(GatewayConstants.API_RETRIEVE_URL).toString()), anyString(), anyString(),
                        anyString());
        Mockito.doReturn(jsonParser.parse(subscriptionResponse)).when(dcrExecutor)
                .callPost(eq(urlMap.get(GatewayConstants.API_SUBSCRIBE_URL).toString()), anyString(), anyString());
        Mockito.doReturn(false).when(dcrExecutor)
                .callDelete(eq("dcr/register/provided_client_id0001"), anyString());
        dcrExecutor.postProcessResponse(obapiResponseContext);
        verify(obapiResponseContext, times(8)).setError(true);
    }
}
