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

package com.wso2.openbanking.accelerator.gateway.handler;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.gateway.executor.exception.OpenBankingExecutorException;
import com.wso2.openbanking.accelerator.gateway.util.GatewayUtils;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.io.IOUtils;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Optional;

import static org.mockito.Mockito.doReturn;

/**
 * Test Handler class for Signing Responses.
 */
@PrepareForTest({OpenBankingConfigParser.class, GatewayUtils.class, JsonUtil.class, IOUtils.class,
        RelayUtils.class, Axis2Sender.class})
@PowerMockIgnore({"jdk.internal.reflect.*"})
public class JwsResponseSignatureHandlerTest extends PowerMockTestCase {

    private MessageContext messageContext;
    private HashMap<String, String> headers = new HashMap<>();
    OpenBankingConfigParser openBankingConfigParserMock;

    @BeforeClass
    public void init() {

        messageContext = Mockito.mock(MessageContext.class);
        openBankingConfigParserMock = Mockito.mock(OpenBankingConfigParser.class);
        headers = new HashMap<>();
        doReturn(headers).when(messageContext).getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
    }

    @Test(priority = 1)
    public void testHandleResponseOutFlow() {

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);
        Mockito.when(openBankingConfigParserMock.isJwsResponseSigningEnabled()).thenReturn(true);
        Axis2MessageContext msgCtx = Mockito.mock(Axis2MessageContext.class);
        doReturn(messageContext).when(msgCtx).getAxis2MessageContext();
        SOAPEnvelope soapEnvelope = Mockito.spy(SOAPEnvelope.class);
        doReturn(soapEnvelope).when(msgCtx).getEnvelope();
        SOAPBody soapBody = Mockito.spy(SOAPBody.class);
        doReturn(soapBody).when(soapEnvelope).getBody();
        doReturn("Schema validation failed").when(soapBody).toString();
        Assert.assertTrue(new JwsResponseSignatureHandler().handleResponseOutFlow(msgCtx));
    }

    @Test(priority = 1)
    public void testHandleResponseOutFlowWithPayloadError() throws Exception {

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.mockStatic(GatewayUtils.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);
        Mockito.when(openBankingConfigParserMock.isJwsResponseSigningEnabled()).thenReturn(true);
        Axis2MessageContext msgCtx = Mockito.mock(Axis2MessageContext.class);
        doReturn(messageContext).when(msgCtx).getAxis2MessageContext();
        SOAPEnvelope soapEnvelope = Mockito.spy(SOAPEnvelope.class);
        doReturn(soapEnvelope).when(msgCtx).getEnvelope();
        SOAPBody soapBody = Mockito.spy(SOAPBody.class);
        doReturn(soapBody).when(soapEnvelope).getBody();
        doReturn("Schema validation failed").when(soapBody).toString();
        PowerMockito.doThrow(new OpenBankingException("")).when(GatewayUtils.class,
                "buildMessagePayloadFromMessageContext", Mockito.any(), Mockito.anyMap());
        PowerMockito.doReturn("test").when(GatewayUtils.class, "constructJWSSignature",
                "msgCtx", new HashMap<>());
        PowerMockito.doNothing().when(GatewayUtils.class, "returnSynapseHandlerJSONError",
                Mockito.any(), Mockito.anyString(), Mockito.anyString());
        Assert.assertTrue(new JwsResponseSignatureHandler().handleResponseOutFlow(msgCtx));
    }

    @Test(priority = 1)
    public void testHandleResponseOutFlowWithPayload() throws Exception {

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.mockStatic(GatewayUtils.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);
        Mockito.when(openBankingConfigParserMock.isJwsResponseSigningEnabled()).thenReturn(true);
        Axis2MessageContext msgCtx = Mockito.mock(Axis2MessageContext.class);
        doReturn(messageContext).when(msgCtx).getAxis2MessageContext();
        SOAPEnvelope soapEnvelope = Mockito.spy(SOAPEnvelope.class);
        doReturn(soapEnvelope).when(msgCtx).getEnvelope();
        SOAPBody soapBody = Mockito.spy(SOAPBody.class);
        doReturn(soapBody).when(soapEnvelope).getBody();
        doReturn("Schema validation failed").when(soapBody).toString();
        PowerMockito.doReturn(Optional.of("test")).when(GatewayUtils.class, "buildMessagePayloadFromMessageContext",
                Mockito.any(), Mockito.anyMap());
        PowerMockito.doReturn("test").when(GatewayUtils.class, "constructJWSSignature",
                "msgCtx", new HashMap<>());
        Assert.assertTrue(new JwsResponseSignatureHandler().handleResponseOutFlow(msgCtx));
    }

    @Test(priority = 1)
    public void testGenerateJWSSignature() throws Exception {

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.mockStatic(GatewayUtils.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);
        Mockito.when(openBankingConfigParserMock.isJwsResponseSigningEnabled()).thenReturn(true);
        PowerMockito.doReturn("test").when(GatewayUtils.class, "constructJWSSignature",
                "msgCtx", new HashMap<>());
        Assert.assertNotNull(new JwsResponseSignatureHandler().generateJWSSignature(Optional.of("msgCtx")));
    }

    @Test(priority = 1)
    public void testGenerateJWSSignatureWhenPayloadIsNull() throws Exception {

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.mockStatic(GatewayUtils.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);
        Mockito.when(openBankingConfigParserMock.isJwsResponseSigningEnabled()).thenReturn(true);
        PowerMockito.doReturn("test").when(GatewayUtils.class, "constructJWSSignature",
                "msgCtx", new HashMap<>());
        Assert.assertNull(new JwsResponseSignatureHandler().generateJWSSignature(Optional.of("")));
    }

    @Test(priority = 1)
    public void testGenerateJWSSignatureForErrorCase() throws Exception {

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.mockStatic(GatewayUtils.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);
        Mockito.when(openBankingConfigParserMock.isJwsResponseSigningEnabled()).thenReturn(true);
        PowerMockito.doThrow(new OpenBankingExecutorException("")).when(GatewayUtils.class, "constructJWSSignature",
                "msgCtx", new HashMap<>());
        Assert.assertNull(new JwsResponseSignatureHandler().generateJWSSignature(Optional.of("")));
    }
}
