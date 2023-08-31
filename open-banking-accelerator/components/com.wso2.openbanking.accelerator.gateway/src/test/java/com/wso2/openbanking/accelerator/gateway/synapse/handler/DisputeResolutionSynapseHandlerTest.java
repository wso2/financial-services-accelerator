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

package com.wso2.openbanking.accelerator.gateway.synapse.handler;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.data.publisher.common.util.OBDataPublisherUtil;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import com.wso2.openbanking.accelerator.gateway.util.GatewayUtils;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;


/**
 * Test for Dispute Resolution Synapse Handler.
 */
@PrepareForTest({OpenBankingConfigParser.class, OBDataPublisherUtil.class, JsonUtil.class, GatewayUtils.class})
@PowerMockIgnore({"jdk.internal.reflect.*", "javax.management.*"})
public class DisputeResolutionSynapseHandlerTest extends PowerMockTestCase {
    private static final Log log = LogFactory.getLog(DisputeResolutionSynapseHandlerTest.class);
    private static final String REQUEST_BODY = "requestBody";
    private Axis2MessageContext axis2MessageContextMock;

    Map<String, Object> sampleRequestHeaders = new HashMap<>();

    DisputeResolutionSynapseHandler disputeResolutionSynapseHandler = new DisputeResolutionSynapseHandler();
    public DisputeResolutionSynapseHandlerTest() throws ParserConfigurationException, IOException {

        sampleRequestHeaders.put("Accept-Encoding", "gzip, deflate, br");
        sampleRequestHeaders.put("Access-Control-Allow-Headers", "authorization, Access-Control-Allow-Origin," +
                " Content-Type, SOAPAction, apikey, Authorization");
        sampleRequestHeaders.put("Access-Control-Allow-Methods", "POST");
        sampleRequestHeaders.put("Access-Control-Allow-Origin", "*");
        sampleRequestHeaders.put("Access-Control-Expose-Headers", "");
        sampleRequestHeaders.put("Content-Type", "application/json");
        sampleRequestHeaders.put("WWW-Authenticate", "Internal API Key realm=\"WSO2 API Manager\"," +
                " Bearer realm=\"WSO2 API Manager\", error=\"invalid_token\", error_description=\"The " +
                "provided token is invalid\"");
       }

    String sampleRequestBody = "{\"Data\":{\"Permissions\":[\"ReadAccountsDetail\",\"ReadTransactionsDetail\","
            + "\"ReadBalances\"],"
            + "\"Risk\":{}}";
    String sampleResponseBody = "{\"code\": \"900902\"," +
            "\"message\": \"Missing Credentials\"," +
            "\"description\": \"Invalid Credentials."
            + "Make sure your API invocation call has a header: "
            + "'Authorization : Bearer ACCESS_TOKEN' or 'Authorization :"
            + " Basic ACCESS_TOKEN' or 'apikey: API_KEY'\"}";

    @BeforeClass
    public void initClass() {

        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void testSynapseHandlerForResponseFlow() throws Exception {

        MessageContext messageContext = getResponseData();
        Assert.assertTrue(disputeResolutionSynapseHandler.handleResponseOutFlow(messageContext));
    }
    @Test
    public void testSynapseHandlerForRequestFlow() throws Exception {

        MessageContext messageContext = getRequestData();
        Assert.assertTrue(disputeResolutionSynapseHandler.handleRequestInFlow(messageContext));
    }


    private MessageContext getResponseData() throws Exception {

        PowerMockito.mockStatic(GatewayUtils.class);
        mockStatic(OpenBankingConfigParser.class);
        OpenBankingConfigParser openBankingConfigParserMock = mock(OpenBankingConfigParser.class);
        when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);
        Mockito.doReturn(true).when(openBankingConfigParserMock).isDisputeResolutionEnabled();
        Mockito.doReturn(true).when(openBankingConfigParserMock)
                .isNonErrorDisputeDataPublishingEnabled();

        SynapseConfiguration synapseConfigurationMock = mock(SynapseConfiguration.class);
        SynapseEnvironment synapseEnvironmentMock = mock(SynapseEnvironment.class);
        org.apache.axis2.context.MessageContext messageContextMock =
                mock(org.apache.axis2.context.MessageContext.class);
        MessageContext messageContext = new Axis2MessageContext(messageContextMock, synapseConfigurationMock,
                synapseEnvironmentMock);

        messageContext.setProperty(APIConstants.API_ELECTED_RESOURCE, "/register");
        messageContext.setProperty(GatewayConstants.HTTP_METHOD, "POST");
        messageContext.setProperty(GatewayConstants.UNKNOWN, "unknown");

        when(GatewayUtils.buildMessagePayloadFromMessageContext(Mockito.anyObject(), Mockito.anyMap()))
                .thenReturn(Optional.of(sampleResponseBody));

        Map<String, Object> contextEntries = new HashMap<>();
        contextEntries.put(REQUEST_BODY, sampleRequestBody);
        messageContext.setContextEntries(contextEntries);

        org.apache.axis2.context.MessageContext axis2MessageContext = new org.apache.axis2.context.MessageContext();
        axis2MessageContext.setProperty(GatewayConstants.HTTP_SC, 401);
        axis2MessageContext.setProperty(org.apache.axis2.context.MessageContext
                .TRANSPORT_HEADERS, sampleRequestHeaders);
        ((Axis2MessageContext) messageContext).setAxis2MessageContext(axis2MessageContext);

        mockStatic(OBDataPublisherUtil.class);
        doNothing().when(OBDataPublisherUtil.class, "publishData", Mockito.anyString(), Mockito.anyString(),
                Mockito.anyObject());

        mockStatic(JsonUtil.class);
        OMElement omElementMock = mock(OMElement.class);
        when(JsonUtil.getNewJsonPayload(Mockito.anyObject(), Mockito.anyString(), Mockito.anyBoolean(),
                Mockito.anyBoolean())).thenReturn(omElementMock);

        return messageContext;
    }

    private MessageContext getRequestData() throws Exception {

        PowerMockito.mockStatic(GatewayUtils.class);
        mockStatic(OpenBankingConfigParser.class);
        OpenBankingConfigParser openBankingConfigParserMock = mock(OpenBankingConfigParser.class);
        when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);
        Mockito.doReturn(true).when(openBankingConfigParserMock).isDisputeResolutionEnabled();

        SynapseConfiguration synapseConfigurationMock = mock(SynapseConfiguration.class);
        SynapseEnvironment synapseEnvironmentMock = mock(SynapseEnvironment.class);
        org.apache.axis2.context.MessageContext messageContextMock =
                mock(org.apache.axis2.context.MessageContext.class);
        MessageContext messageContext = new Axis2MessageContext(messageContextMock, synapseConfigurationMock,
                synapseEnvironmentMock);

        org.apache.axis2.context.MessageContext axis2MessageContext = new org.apache.axis2.context.MessageContext();
       ((Axis2MessageContext) messageContext).setAxis2MessageContext(axis2MessageContext);
        axis2MessageContext.setProperty(org.apache.axis2.context.MessageContext
                .TRANSPORT_HEADERS, sampleRequestHeaders);

        mockStatic(JsonUtil.class);
        OMElement omElementMock = mock(OMElement.class);
        when(JsonUtil.getNewJsonPayload(Mockito.anyObject(), Mockito.anyString(), Mockito.anyBoolean(),
                Mockito.anyBoolean())).thenReturn(omElementMock);

        when(GatewayUtils.buildMessagePayloadFromMessageContext(Mockito.anyObject(), Mockito.anyMap()))
                .thenReturn(Optional.of(sampleRequestBody));
       return messageContext;
    }


}

