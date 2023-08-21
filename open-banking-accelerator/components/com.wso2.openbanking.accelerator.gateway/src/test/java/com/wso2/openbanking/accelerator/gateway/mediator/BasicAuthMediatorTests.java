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

package com.wso2.openbanking.accelerator.gateway.mediator;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.synapse.ContinuationState;
import org.apache.synapse.FaultHandler;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.endpoints.Endpoint;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import static org.mockito.Mockito.doReturn;

/**
 * Unit tests for Basic Auth Mediator.
 */
public class BasicAuthMediatorTests {

    @Test
    public void testBasicAuthMediator() {

        MockitoAnnotations.initMocks(this);
        BasicAuthMediator basicAuthMediator = Mockito.spy(BasicAuthMediator.class);
        MessageContext messageContext = new MessageContextMock();
        doReturn("admin").when(basicAuthMediator).getAPIMConfigFromKey(Mockito.anyString());
        basicAuthMediator.mediate(messageContext);
        Assert.assertTrue(StringUtils.equals((CharSequence) messageContext.getProperty("basicAuthentication"),
                "Basic " + Base64.getEncoder().encodeToString("admin:admin".getBytes(StandardCharsets.UTF_8))));
    }

    // A mock class of MessageContext is created to mimic the behaviour
    static class MessageContextMock implements MessageContext {

        Map<String, Object> properties = new HashMap<>();

        @Override
        public SynapseConfiguration getConfiguration() {
            return null;
        }

        @Override
        public void setConfiguration(SynapseConfiguration synapseConfiguration) {

        }

        @Override
        public SynapseEnvironment getEnvironment() {
            return null;
        }

        @Override
        public void setEnvironment(SynapseEnvironment synapseEnvironment) {

        }

        @Override
        public Map<String, Object> getContextEntries() {
            return null;
        }

        @Override
        public void setContextEntries(Map<String, Object> map) {

        }

        @Override
        public Mediator getMainSequence() {
            return null;
        }

        @Override
        public Mediator getFaultSequence() {
            return null;
        }

        @Override
        public Mediator getSequence(String s) {
            return null;
        }

        @Override
        public OMElement getFormat(String s) {
            return null;
        }

        @Override
        public Mediator getSequenceTemplate(String s) {
            return null;
        }

        @Override
        public Endpoint getEndpoint(String s) {
            return null;
        }

        @Override
        public Object getProperty(String s) {
            return properties.get(s);
        }

        @Override
        public Object getEntry(String s) {
            return null;
        }

        @Override
        public Object getLocalEntry(String s) {
            return null;
        }

        @Override
        public void setProperty(String s, Object o) {

            this.properties.put(s, o);
        }

        @Override
        public Set getPropertyKeySet() {
            return null;
        }

        @Override
        public SOAPEnvelope getEnvelope() {
            return null;
        }

        @Override
        public void setEnvelope(SOAPEnvelope soapEnvelope) throws AxisFault {

        }

        @Override
        public EndpointReference getFaultTo() {
            return null;
        }

        @Override
        public void setFaultTo(EndpointReference endpointReference) {

        }

        @Override
        public EndpointReference getFrom() {
            return null;
        }

        @Override
        public void setFrom(EndpointReference endpointReference) {

        }

        @Override
        public String getMessageID() {
            return null;
        }

        @Override
        public void setMessageID(String s) {

        }

        @Override
        public RelatesTo getRelatesTo() {
            return null;
        }

        @Override
        public void setRelatesTo(RelatesTo[] relatesTos) {

        }

        @Override
        public EndpointReference getReplyTo() {
            return null;
        }

        @Override
        public void setReplyTo(EndpointReference endpointReference) {

        }

        @Override
        public EndpointReference getTo() {
            return null;
        }

        @Override
        public void setTo(EndpointReference endpointReference) {

        }

        @Override
        public void setWSAAction(String s) {

        }

        @Override
        public String getWSAAction() {
            return null;
        }

        @Override
        public String getSoapAction() {
            return null;
        }

        @Override
        public void setSoapAction(String s) {

        }

        @Override
        public void setWSAMessageID(String s) {

        }

        @Override
        public String getWSAMessageID() {
            return null;
        }

        @Override
        public boolean isDoingMTOM() {
            return false;
        }

        @Override
        public boolean isDoingSWA() {
            return false;
        }

        @Override
        public void setDoingMTOM(boolean b) {

        }

        @Override
        public void setDoingSWA(boolean b) {

        }

        @Override
        public boolean isDoingPOX() {
            return false;
        }

        @Override
        public void setDoingPOX(boolean b) {

        }

        @Override
        public boolean isDoingGET() {
            return false;
        }

        @Override
        public void setDoingGET(boolean b) {

        }

        @Override
        public boolean isSOAP11() {
            return false;
        }

        @Override
        public void setResponse(boolean b) {

        }

        @Override
        public boolean isResponse() {
            return false;
        }

        @Override
        public void setFaultResponse(boolean b) {

        }

        @Override
        public boolean isFaultResponse() {
            return false;
        }

        @Override
        public int getTracingState() {
            return 0;
        }

        @Override
        public void setTracingState(int i) {

        }

        @Override
        public Stack<FaultHandler> getFaultStack() {
            return null;
        }

        @Override
        public void pushFaultHandler(FaultHandler faultHandler) {

        }

        @Override
        public Stack<ContinuationState> getContinuationStateStack() {
            return null;
        }

        @Override
        public void pushContinuationState(ContinuationState continuationState) {

        }

        @Override
        public boolean isContinuationEnabled() {
            return false;
        }

        @Override
        public void setContinuationEnabled(boolean b) {

        }

        @Override
        public Log getServiceLog() {
            return null;
        }

        @Override
        public Mediator getDefaultConfiguration(String s) {
            return null;
        }

        @Override
        public String getMessageString() {
            return null;
        }

        @Override
        public int getMessageFlowTracingState() {
            return 0;
        }

        @Override
        public void setMessageFlowTracingState(int i) {

        }
    }
}
