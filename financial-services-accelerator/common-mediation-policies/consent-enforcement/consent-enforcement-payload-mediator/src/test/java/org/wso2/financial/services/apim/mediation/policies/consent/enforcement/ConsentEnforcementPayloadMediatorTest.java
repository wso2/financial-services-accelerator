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

package org.wso2.financial.services.apim.mediation.policies.consent.enforcement;

import org.apache.axis2.context.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.apim.mediation.policies.consent.enforcement.utils.ConsentEnforcementUtils;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class for ConsentEnforcementPayloadMediator.
 */
public class ConsentEnforcementPayloadMediatorTest {

    private ConsentEnforcementPayloadMediator mediator;
    private Axis2MessageContext axis2Ctx;
    private MessageContext synapseCtx;
    private Map<String, String> headers;

    MockedStatic<ConsentEnforcementUtils> consentEnforcementUtilsMockedStatic;
    MockedStatic<JsonUtil> jsonUtilMockedStatic;

    @BeforeClass
    public void setup() {

        mediator = new ConsentEnforcementPayloadMediator();

        synapseCtx = Mockito.mock(MessageContext.class);
        axis2Ctx = Mockito.mock(Axis2MessageContext.class);
        headers = new HashMap<>();

        Mockito.when(axis2Ctx.getAxis2MessageContext()).thenReturn(synapseCtx);

        consentEnforcementUtilsMockedStatic = Mockito.mockStatic(ConsentEnforcementUtils.class);
        jsonUtilMockedStatic = Mockito.mockStatic(JsonUtil.class);
    }

    @AfterClass
    public void tearDown() {

        consentEnforcementUtilsMockedStatic.close();
        jsonUtilMockedStatic.close();
    }

    @Test
    public void testConsentEnforcementPayloadMediatorValidScenario() {

        consentEnforcementUtilsMockedStatic.when(() -> ConsentEnforcementUtils
                .extractConsentIdFromJwtToken(Mockito.any(), Mockito.any()))
                .thenReturn("123");
        consentEnforcementUtilsMockedStatic.when(() -> ConsentEnforcementUtils
                .createValidationRequestPayload(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(new JSONObject());
        consentEnforcementUtilsMockedStatic.when(() -> ConsentEnforcementUtils
                .generateJWT(Mockito.any()))
                .thenReturn("enforcementJwtPayload");
        jsonUtilMockedStatic.when(() -> JsonUtil
                        .jsonPayloadToString(Mockito.any()))
                .thenReturn("jsonRequestPayload");

        mediator.setConsentIdClaimName("consentId");
        Assert.assertTrue(mediator.mediate(axis2Ctx));
    }

    @Test(expectedExceptions = SynapseException.class)
    public void testConsentEnforcementPayloadMediatorNullConsentIdScenario() {

        mediator.setConsentIdClaimName("consentId");
        mediator.mediate(axis2Ctx);
    }

    @Test(expectedExceptions = SynapseException.class)
    public void testConsentEnforcementPayloadMediatorInvalidJwtTokenScenario() {

        consentEnforcementUtilsMockedStatic.when(() -> ConsentEnforcementUtils
                        .extractConsentIdFromJwtToken(Mockito.any(), Mockito.any()))
                .thenThrow(UnsupportedEncodingException.class);

        mediator.setConsentIdClaimName("consentId");
        mediator.mediate(axis2Ctx);
    }

    @Test(expectedExceptions = SynapseException.class)
    public void testConsentEnforcementPayloadMediatorInvalidParsingScenario() {

        consentEnforcementUtilsMockedStatic.when(() -> ConsentEnforcementUtils
                        .extractConsentIdFromJwtToken(Mockito.any(), Mockito.any()))
                .thenReturn("123");
        consentEnforcementUtilsMockedStatic.when(() -> ConsentEnforcementUtils
                        .createValidationRequestPayload(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(new JSONObject());
        consentEnforcementUtilsMockedStatic.when(() -> ConsentEnforcementUtils
                        .generateJWT(Mockito.any()))
                .thenThrow(ParseException.class);

        mediator.setConsentIdClaimName("consentId");
        mediator.mediate(axis2Ctx);
    }

    @Test(expectedExceptions = SynapseException.class)
    public void testConsentEnforcementPayloadMediatorInvalidRequestPayloadScenario() {

        consentEnforcementUtilsMockedStatic.when(() -> ConsentEnforcementUtils
                        .extractConsentIdFromJwtToken(Mockito.any(), Mockito.any()))
                .thenReturn("123");
        consentEnforcementUtilsMockedStatic.when(() -> ConsentEnforcementUtils
                        .createValidationRequestPayload(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(new JSONObject());
        jsonUtilMockedStatic.when(() -> JsonUtil
                        .jsonPayloadToString(Mockito.any()))
                .thenThrow(JSONException.class);

        mediator.setConsentIdClaimName("consentId");
        mediator.mediate(axis2Ctx);
    }

}
