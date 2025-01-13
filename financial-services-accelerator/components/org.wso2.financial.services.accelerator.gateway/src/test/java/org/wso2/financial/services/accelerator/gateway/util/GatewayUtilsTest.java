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

package org.wso2.financial.services.accelerator.gateway.util;

import com.nimbusds.jwt.JWTClaimsSet;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigurationService;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.gateway.GatewayTestConstants;
import org.wso2.financial.services.accelerator.gateway.internal.GatewayDataHolder;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class for Gateway utility methods.
 */
public class GatewayUtilsTest {

    @BeforeClass
    public void initTest() {
        GatewayDataHolder dataHolder = GatewayDataHolder.getInstance();

        Map<String, Object> configs = new HashMap<>();
        configs.put(FinancialServicesConstants.VALIDATE_JWT, true);
        configs.put(FinancialServicesConstants.DCR_USE_SOFTWAREID_AS_APPNAME, true);
        configs.put(FinancialServicesConstants.JWKS_ENDPOINT_NAME, "software_jwks_endpoint");
        configs.put(FinancialServicesConstants.GATEWAY_CACHE_EXPIRY, "60");
        configs.put(FinancialServicesConstants.GATEWAY_CACHE_MODIFIED_EXPIRY, "60");
        configs.put(FinancialServicesConstants.REQUEST_ROUTER,
                "org.wso2.financial.services.accelerator.gateway.executor.core.DefaultRequestRouter");
        configs.put(FinancialServicesConstants.SSA_CLIENT_NAME, "software_client_name");

        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.getFirstProperty(Mockito.anyString())).thenReturn("admin");
        APIManagerConfigurationService apimConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(apimConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        dataHolder.setApiManagerConfiguration(apimConfigurationService);

        FinancialServicesConfigurationService financialServicesConfigurationService =
                Mockito.mock(FinancialServicesConfigurationService.class);
        Mockito.when(financialServicesConfigurationService.getConfigurations()).thenReturn(configs);
        dataHolder.setFinancialServicesConfigurationService(financialServicesConfigurationService);
    }
    @Test
//            (priority = 1)
    public void testIsValidJWTToken() {

        Assert.assertTrue(GatewayUtils.isValidJWTToken(GatewayTestConstants.TEST_JWT));
    }

    @Test
//            (priority = 2)
    public void testB64Encode() throws UnsupportedEncodingException {

        JSONObject payload = GatewayUtils.decodeBase64(GatewayTestConstants.B64_PAYLOAD);
        Assert.assertEquals(payload.getString("sub"), "1234567890");
        Assert.assertEquals(payload.getString("name"), "John Doe");
        Assert.assertEquals(payload.getInt("iat"), 1516239022);
    }

    @Test
//            (priority = 3)
    public void testBasicAuthHeader() {

        Assert.assertEquals(GatewayUtils.getBasicAuthHeader("admin", "admin"),
                "Basic YWRtaW46YWRtaW4=");
    }

    @Test
//            (priority = 4)
    public void testGetTextPayload() {

        Assert.assertEquals(GatewayUtils.getTextPayload(GatewayTestConstants.XML_PAYLOAD), "Test Content");
    }

    @Test
//            (priority = 5)
    public void testIsEligibleRequest() {

        Assert.assertTrue(GatewayUtils.isEligibleRequest(GatewayConstants.JSON_CONTENT_TYPE,
                GatewayConstants.POST_HTTP_METHOD));
        Assert.assertTrue(GatewayUtils.isEligibleRequest(GatewayConstants.APPLICATION_XML_CONTENT_TYPE,
                GatewayConstants.POST_HTTP_METHOD));
        Assert.assertTrue(GatewayUtils.isEligibleRequest(GatewayConstants.TEXT_XML_CONTENT_TYPE,
                GatewayConstants.POST_HTTP_METHOD));
        Assert.assertTrue(GatewayUtils.isEligibleRequest(GatewayConstants.JSON_CONTENT_TYPE,
                GatewayConstants.PUT_HTTP_METHOD));
        Assert.assertTrue(GatewayUtils.isEligibleRequest(GatewayConstants.APPLICATION_XML_CONTENT_TYPE,
                GatewayConstants.PUT_HTTP_METHOD));
        Assert.assertTrue(GatewayUtils.isEligibleRequest(GatewayConstants.TEXT_XML_CONTENT_TYPE,
                GatewayConstants.PUT_HTTP_METHOD));
    }

    @Test
//            (priority = 6)
    public void testIsEligibleResponse() {

        Assert.assertTrue(GatewayUtils.isEligibleResponse(GatewayConstants.JSON_CONTENT_TYPE,
                GatewayConstants.POST_HTTP_METHOD));
        Assert.assertTrue(GatewayUtils.isEligibleResponse(GatewayConstants.APPLICATION_XML_CONTENT_TYPE,
                GatewayConstants.POST_HTTP_METHOD));
        Assert.assertTrue(GatewayUtils.isEligibleResponse(GatewayConstants.TEXT_XML_CONTENT_TYPE,
                GatewayConstants.POST_HTTP_METHOD));
        Assert.assertTrue(GatewayUtils.isEligibleResponse(GatewayConstants.JSON_CONTENT_TYPE,
                GatewayConstants.PUT_HTTP_METHOD));
        Assert.assertTrue(GatewayUtils.isEligibleResponse(GatewayConstants.APPLICATION_XML_CONTENT_TYPE,
                GatewayConstants.PUT_HTTP_METHOD));
        Assert.assertTrue(GatewayUtils.isEligibleResponse(GatewayConstants.TEXT_XML_CONTENT_TYPE,
                GatewayConstants.PUT_HTTP_METHOD));
        Assert.assertTrue(GatewayUtils.isEligibleResponse(GatewayConstants.JSON_CONTENT_TYPE,
                GatewayConstants.GET_HTTP_METHOD));
        Assert.assertTrue(GatewayUtils.isEligibleResponse(GatewayConstants.APPLICATION_XML_CONTENT_TYPE,
                GatewayConstants.GET_HTTP_METHOD));
        Assert.assertTrue(GatewayUtils.isEligibleResponse(GatewayConstants.TEXT_XML_CONTENT_TYPE,
                GatewayConstants.GET_HTTP_METHOD));
    }

    @Test
//            (priority = 7)
    public void testJWTPayloadLoad() {

        Assert.assertEquals(GatewayUtils.getPayloadFromJWT(GatewayTestConstants.TEST_JWT),
                GatewayTestConstants.B64_PAYLOAD);
    }

    @Test
    public void testConstructIsDcrPayload() {

        JWTClaimsSet jwtClaimsSet = Mockito.mock(JWTClaimsSet.class);
        Mockito.doReturn(GatewayTestConstants.DCR_CLAIMS).when(jwtClaimsSet).getClaims();

        String payload = GatewayUtils.constructIsDcrPayload(jwtClaimsSet,
                new JSONObject(GatewayTestConstants.DECODED_DCR_PAYLOAD));
        JSONObject payloadObj = new JSONObject(payload);

        Assert.assertTrue(payloadObj.has(GatewayConstants.CLIENT_NAME));
        Assert.assertTrue(payloadObj.has(GatewayConstants.JWKS_URI));
        Assert.assertTrue(payloadObj.has(GatewayConstants.TOKEN_TYPE));
        Assert.assertTrue(payloadObj.has(GatewayConstants.REQUIRE_SIGNED_OBJ));
        Assert.assertTrue(payloadObj.has(GatewayConstants.TLS_CLIENT_CERT_ACCESS_TOKENS));
    }

    @Test
    public void testGetApplicationNameFromSSA() {

        JWTClaimsSet jwtClaimsSet = Mockito.mock(JWTClaimsSet.class);
        Mockito.doReturn(GatewayTestConstants.DCR_CLAIMS).when(jwtClaimsSet).getClaims();
        String appName = GatewayUtils.getApplicationName(jwtClaimsSet,
                new JSONObject(GatewayTestConstants.DECODED_DCR_PAYLOAD));
        Assert.assertNotNull(appName);
    }

    @Test
    public void testGetApplicationNameFromRequest() {

        JWTClaimsSet jwtClaimsSet = Mockito.mock(JWTClaimsSet.class);
        Mockito.doReturn(GatewayTestConstants.DCR_CLAIMS_WITHOUT_SSA).when(jwtClaimsSet).getClaims();
        String appName = GatewayUtils.getApplicationName(jwtClaimsSet,
                new JSONObject(GatewayTestConstants.DECODED_DCR_PAYLOAD));
        Assert.assertNotNull(appName);
    }

    @Test
    public void testGetSafeApplicationName() {

        String appName = GatewayUtils.getSafeApplicationName("WSO2 Open Banking TPP2 (Sandbox)");
        Assert.assertNotNull(appName);
        Assert.assertEquals(appName, "WSO2_Open_Banking_TPP2__Sandbox_");
    }
}
