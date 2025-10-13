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

package org.wso2.financial.services.apim.mediation.policies.consent.enforcement.utils;

import org.apache.synapse.MessageContext;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.financial.services.apim.mediation.policies.consent.enforcement.constants.ConsentEnforcementConstants;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class for ConsentEnforcementUtils.
 */
public class ConsentEnforcementUtilsTest {

    @Test
    public void testGetResourceParamMap() {
        MessageContext messageContext = Mockito.mock(MessageContext.class);
        Mockito.when(messageContext.getProperty(ConsentEnforcementConstants.REST_FULL_REQUEST_PATH))
                .thenReturn("/aisp/accounts/123");
        Mockito.when(messageContext.getProperty(ConsentEnforcementConstants.REST_METHOD))
                .thenReturn("GET");
        Mockito.when(messageContext.getProperty(ConsentEnforcementConstants.REST_API_CONTEXT))
                .thenReturn("/open-banking/v3.1/aisp");

        Map<String, String> resourceMap = ConsentEnforcementUtils.getResourceParamMap(messageContext);

        Assert.assertEquals(resourceMap.get(ConsentEnforcementConstants.RESOURCE_TAG), "/aisp/accounts/123");
        Assert.assertEquals(resourceMap.get(ConsentEnforcementConstants.HTTP_METHOD_TAG), "GET");
        Assert.assertEquals(resourceMap.get(ConsentEnforcementConstants.CONTEXT_TAG), "/open-banking/v3.1/aisp");
    }

    @Test
    public void testExtractConsentIdFromJwtToken() throws UnsupportedEncodingException {
        String token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJjb25zZW50SWQiOiIxMjM0NTYifQ.sflKxwRJSMeKKF2QT4fw" +
                "pMeJf36POk6yJV_adQssw5c";

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", token);

        String consentId = ConsentEnforcementUtils.extractConsentIdFromJwtToken(headers, "consentId");

        Assert.assertEquals(consentId, "123456");
    }

    @Test
    public void testCreateValidationRequestPayload() {
        String jsonPayload = "{\"key\":\"value\"}";
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Header1", "Value1");
        Map<String, Object> additionalParams = new HashMap<>();
        additionalParams.put("param1", "value1");

        JSONObject validationRequest = ConsentEnforcementUtils
                .createValidationRequestPayload(jsonPayload, requestHeaders, additionalParams);

        Assert.assertEquals(validationRequest.getJSONObject("headers").getString("Header1"), "Value1");
        Assert.assertEquals(validationRequest.getJSONObject("body").getString("key"), "value");
        Assert.assertEquals(validationRequest.getString("param1"), "value1");
    }

}
