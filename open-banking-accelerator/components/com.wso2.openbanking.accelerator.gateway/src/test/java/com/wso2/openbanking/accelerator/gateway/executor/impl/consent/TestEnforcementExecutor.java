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

package com.wso2.openbanking.accelerator.gateway.executor.impl.consent;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OpenBankingExecutorError;
import com.wso2.openbanking.accelerator.gateway.executor.test.TestConstants;
import com.wso2.openbanking.accelerator.gateway.internal.GatewayDataHolder;
import com.wso2.openbanking.accelerator.gateway.util.GatewayUtils;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Test for enforcement executor.
 */
public class TestEnforcementExecutor {

    private static ConsentEnforcementExecutor consentEnforcementExecutor;
    private static OBAPIRequestContext obapiRequestContext;
    private String jwtToken = "eyJjdXN0b20iOiJwYXlsb2FkIn0";

    @BeforeClass
    public static void beforeClass() throws OpenBankingException {

        GatewayDataHolder dataHolder = GatewayDataHolder.getInstance();
        String path = "src/test/resources";
        File file = new File(path);
        String absolutePathForTestResources = file.getAbsolutePath();
        dataHolder.setKeyStoreLocation(absolutePathForTestResources + "/wso2carbon.jks");
        dataHolder.setKeyAlias("wso2carbon");
        dataHolder.setKeyPassword("wso2carbon");
        dataHolder.setKeyStorePassword("wso2carbon".toCharArray());

        consentEnforcementExecutor = new ConsentEnforcementExecutor();
    }

    @Test(priority = 1)
    public void testSigningKeyRetrieval() {

        Assert.assertNotNull(consentEnforcementExecutor.getJWTSigningKey());
    }

    @Test(priority = 2)
    public void testJWTGeneration() {

        String jwtToken = consentEnforcementExecutor.generateJWT(TestConstants.CUSTOM_PAYLOAD);
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
                        TestConstants.CUSTOM_PAYLOAD, new HashMap<>());
        Assert.assertNotNull(jsonObject);
        Assert.assertEquals(((JSONObject) jsonObject.get(ConsentEnforcementExecutor.HEADERS_TAG)).get("customHeader"),
                "headerValue");
        Assert.assertEquals(((JSONObject) jsonObject.get(ConsentEnforcementExecutor.BODY_TAG)).get("custom"),
                "payload");
    }

    @Test(priority = 3)
    public void testB64Decoder() throws UnsupportedEncodingException {

        JSONObject jsonObject = GatewayUtils.decodeBase64(jwtToken);
        Assert.assertEquals(jsonObject.get("custom").toString(), "payload");
    }

    @Test
    public void testHandlerError() {
        OBAPIRequestContext obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        ArrayList<OpenBankingExecutorError> errors = new ArrayList<>();
        Mockito.when(obapiRequestContext.getErrors()).thenReturn(errors);
        consentEnforcementExecutor.handleError(obapiRequestContext, "Error", "Error",
                "400");
    }

}
