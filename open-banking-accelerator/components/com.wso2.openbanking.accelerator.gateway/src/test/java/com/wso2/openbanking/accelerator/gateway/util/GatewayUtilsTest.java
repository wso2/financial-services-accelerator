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

package com.wso2.openbanking.accelerator.gateway.util;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
/**
 * Utility methods used in gateway.
 */
@PrepareForTest({JsonUtil.class, IOUtils.class, RelayUtils.class, Axis2Sender.class})
@PowerMockIgnore({"jdk.internal.reflect.*"})
public class GatewayUtilsTest extends PowerMockTestCase {

    private MessageContext messageContext;
    private HashMap<String, String> headers = new HashMap<>();

    @BeforeClass
    public void init() {

        messageContext = Mockito.mock(MessageContext.class);
        headers = new HashMap<>();
        doReturn(headers).when(messageContext).getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

    }

    @Test(priority = 1)
    public void testBuildMessagePayloadFromMessageContextForXML() throws OpenBankingException {

        doReturn(false).when(messageContext).getProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED);
        SOAPEnvelope soapEnvelope = Mockito.spy(SOAPEnvelope.class);
        doReturn(soapEnvelope).when(messageContext).getEnvelope();
        SOAPBody soapBody = Mockito.spy(SOAPBody.class);
        doReturn(soapBody).when(soapEnvelope).getBody();
        OMElement element = Mockito.spy(OMElement.class);
        doReturn(element).when(soapBody).getFirstElement();
        headers.put(HttpHeaders.CONTENT_TYPE, GatewayConstants.APPLICATION_XML_CONTENT_TYPE);
        Optional<String> payload = GatewayUtils.buildMessagePayloadFromMessageContext(messageContext, headers);
        Assert.assertTrue(payload.isPresent());
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testBuildMessageErrorPayloadFromMessageContextForXML() throws Exception {

        PowerMockito.mockStatic(RelayUtils.class);
        PowerMockito.doThrow(new IOException()).when(RelayUtils.class, "buildMessage", messageContext);

        doReturn(false).when(messageContext).getProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED);
        SOAPEnvelope soapEnvelope = Mockito.spy(SOAPEnvelope.class);
        doReturn(soapEnvelope).when(messageContext).getEnvelope();
        SOAPBody soapBody = Mockito.spy(SOAPBody.class);
        doReturn(soapBody).when(soapEnvelope).getBody();
        OMElement element = Mockito.spy(OMElement.class);
        doReturn(element).when(soapBody).getFirstElement();
        headers.put(HttpHeaders.CONTENT_TYPE, GatewayConstants.APPLICATION_XML_CONTENT_TYPE);
        Optional<String> payload = GatewayUtils.buildMessagePayloadFromMessageContext(messageContext, headers);
        Assert.assertTrue(payload.isPresent());
    }

    @Test(priority = 1)
    public void testBuildMessagePayloadFromMessageContextForJWT() throws OpenBankingException {

        doReturn(false).when(messageContext).getProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED);
        SOAPEnvelope soapEnvelope = Mockito.spy(SOAPEnvelope.class);
        doReturn(soapEnvelope).when(messageContext).getEnvelope();
        SOAPBody soapBody = Mockito.spy(SOAPBody.class);
        doReturn(soapBody).when(soapEnvelope).getBody();
        OMElement element = Mockito.spy(OMElement.class);
        doReturn(element).when(soapBody).getFirstElement();
        headers.put(HttpHeaders.CONTENT_TYPE, GatewayConstants.JWT_CONTENT_TYPE);
        Optional<String> payload = GatewayUtils.buildMessagePayloadFromMessageContext(messageContext, headers);
        Assert.assertTrue(payload.isPresent());
    }

    @Test(expectedExceptions = OpenBankingException.class)
    public void testNegativeBuildMessagePayloadFromMessageContextForJSON() throws Exception {

        PowerMockito.mockStatic(JsonUtil.class);
        doReturn(false).when(messageContext).getProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED);
        SOAPEnvelope soapEnvelope = Mockito.spy(SOAPEnvelope.class);
        doReturn(soapEnvelope).when(messageContext).getEnvelope();
        SOAPBody soapBody = Mockito.spy(SOAPBody.class);
        doReturn(soapBody).when(soapEnvelope).getBody();
        OMElement element = Mockito.spy(OMElement.class);
        doReturn(element).when(soapBody).getFirstElement();
        headers.put(HttpHeaders.CONTENT_TYPE, GatewayConstants.JSON_CONTENT_TYPE);
        PowerMockito.when(JsonUtil.getJsonPayload(messageContext)).thenReturn(Mockito.mock(InputStream.class));
        Optional<String> payload = GatewayUtils.buildMessagePayloadFromMessageContext(messageContext, headers);
    }

    @Test(priority = 1)
    public void testBuildMessagePayloadFromMessageContextForJSON() throws Exception {

        PowerMockito.mockStatic(JsonUtil.class);
        PowerMockito.mockStatic(IOUtils.class);
        doReturn(false).when(messageContext).getProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED);
        SOAPEnvelope soapEnvelope = Mockito.spy(SOAPEnvelope.class);
        doReturn(soapEnvelope).when(messageContext).getEnvelope();
        SOAPBody soapBody = Mockito.spy(SOAPBody.class);
        doReturn(soapBody).when(soapEnvelope).getBody();
        OMElement element = Mockito.spy(OMElement.class);
        doReturn(element).when(soapBody).getFirstElement();
        headers.put(HttpHeaders.CONTENT_TYPE, GatewayConstants.JSON_CONTENT_TYPE);
        PowerMockito.when(JsonUtil.getJsonPayload(messageContext)).thenReturn(
                new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));
        Optional<String> payload = GatewayUtils.buildMessagePayloadFromMessageContext(messageContext, headers);
        Assert.assertFalse(payload.isPresent());
    }


    @Test(priority = 1)
    public void testReturnSynapseHandlerJSONError() throws Exception {

        Axis2MessageContext msgCtx = Mockito.mock(Axis2MessageContext.class);
        PowerMockito.mockStatic(Axis2Sender.class);
        PowerMockito.mockStatic(JsonUtil.class);
        PowerMockito.doReturn(mock(OMElement.class)).when(JsonUtil.class, "getNewJsonPayload", Mockito.any(),
                Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean());
        PowerMockito.doNothing().when(Axis2Sender.class, "sendBack", msgCtx);
        doReturn(messageContext).when(msgCtx).getAxis2MessageContext();
        GatewayUtils.returnSynapseHandlerJSONError(msgCtx, "", "");
    }

    @Test(priority = 1)
    public void testReturnSynapseHandlerJSONErrorWithAxisFault() throws Exception {

        PowerMockito.mockStatic(RelayUtils.class);
        PowerMockito.doThrow(new AxisFault("")).when(RelayUtils.class, "discardRequestMessage", messageContext);
        PowerMockito.mockStatic(JsonUtil.class);
        PowerMockito.doReturn(mock(OMElement.class)).when(JsonUtil.class, "getNewJsonPayload", Mockito.any(),
                Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean());
        Axis2MessageContext msgCtx = Mockito.mock(Axis2MessageContext.class);
        PowerMockito.mockStatic(Axis2Sender.class);
        PowerMockito.doNothing().when(Axis2Sender.class, "sendBack", msgCtx);
        doReturn(messageContext).when(msgCtx).getAxis2MessageContext();
        GatewayUtils.returnSynapseHandlerJSONError(msgCtx, "", "");
    }

    @Test(priority = 1)
    public void testReturnSynapseHandlerJSONErrorWithAxisFaultForJsonPayload() throws Exception {

        PowerMockito.mockStatic(JsonUtil.class);
        PowerMockito.doThrow(new AxisFault("")).when(JsonUtil.class, "getNewJsonPayload", Mockito.any(),
                Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean());
        Axis2MessageContext msgCtx = Mockito.mock(Axis2MessageContext.class);
        PowerMockito.mockStatic(Axis2Sender.class);
        PowerMockito.doNothing().when(Axis2Sender.class, "sendBack", msgCtx);
        doReturn(messageContext).when(msgCtx).getAxis2MessageContext();
        GatewayUtils.returnSynapseHandlerJSONError(msgCtx, "", "");
    }
}
