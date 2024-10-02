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

import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.gateway.GatewayTestConstants;

import java.io.UnsupportedEncodingException;

/**
 * Test class for Gateway utility methods.
 */
public class GatewayUtilsTest {

    @Test(priority = 1)
    public void testIsValidJWTToken() {

        Assert.assertTrue(GatewayUtils.isValidJWTToken(GatewayTestConstants.TEST_JWT));
    }

    @Test(priority = 2)
    public void testB64Encode() throws UnsupportedEncodingException {

        JSONObject payload = GatewayUtils.decodeBase64(GatewayTestConstants.B64_PAYLOAD);
        Assert.assertEquals(payload.getString("sub"), "1234567890");
        Assert.assertEquals(payload.getString("name"), "John Doe");
        Assert.assertEquals(payload.getInt("iat"), 1516239022);
    }

    @Test(priority = 3)
    public void testBasicAuthHeader() {

        Assert.assertEquals(GatewayUtils.getBasicAuthHeader("admin", "admin"),
                "Basic YWRtaW46YWRtaW4=");
    }

    @Test(priority = 4)
    public void testGetTextPayload() {

        Assert.assertEquals(GatewayUtils.getTextPayload(GatewayTestConstants.XML_PAYLOAD), "Test Content");
    }

    @Test (priority = 5)
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

    @Test (priority = 6)
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

    @Test(priority = 7)
    public void testJWTPayloadLoad() {

        Assert.assertEquals(GatewayUtils.getPayloadFromJWT(GatewayTestConstants.TEST_JWT),
                GatewayTestConstants.B64_PAYLOAD);
    }

}
