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

package org.wso2.financial.services.apim.mediation.policies.dynamic.client.registration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.json.JSONObject;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.apim.mediation.policies.dynamic.client.registration.util.DCRConstants;
import org.wso2.financial.services.apim.mediation.policies.dynamic.client.registration.util.DCRHandlingException;
import org.wso2.financial.services.apim.mediation.policies.dynamic.client.registration.util.DCRUtil;
import org.wso2.financial.services.apim.mediation.policies.dynamic.client.registration.utils.DCRTestConstants;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;

/**
 * Test class for DynamicClientRegistrationRequestMediator.
 */
public class DynamicClientRegistrationRequestMediatorTest {

    private static final Log log = LogFactory.getLog(DynamicClientRegistrationRequestMediatorTest.class);
    private DynamicClientRegistrationRequestMediator mediator;
    private Axis2MessageContext axis2Ctx;
    private MessageContext synapseCtx;
    private Map<String, Object> headers;

    MockedStatic<DCRUtil> dcrUtilMockedStatic;
    MockedStatic<JsonUtil> jsonUtilMockedStatic;

    @BeforeClass
    public void setup() {
        mediator = new DynamicClientRegistrationRequestMediator();

        synapseCtx = Mockito.mock(MessageContext.class);
        axis2Ctx = Mockito.mock(Axis2MessageContext.class);
        headers = new HashMap<>();

        Mockito.when(axis2Ctx.getAxis2MessageContext()).thenReturn(synapseCtx);

        dcrUtilMockedStatic = Mockito.mockStatic(DCRUtil.class);
        jsonUtilMockedStatic = Mockito.mockStatic(JsonUtil.class);

        mediator.setClientNameAttributeName("software_client_name");
        mediator.setJwksEndpointName("software_jwks_endpoint");
        mediator.setValidateRequestJWT(true);
        mediator.setUseSoftwareIdAsAppName(true);
        mediator.setJwksEndpointTimeout(3000);
    }

    @AfterClass
    public void tearDown() {
        dcrUtilMockedStatic.close();
        jsonUtilMockedStatic.close();
    }

    @Test
    public void testMediateMethodForPOST() throws JsonProcessingException {

        Mockito.when(axis2Ctx.getProperty(DCRConstants.HTTP_METHOD)).thenReturn("POST");
        headers.put(DCRConstants.CONTENT_TYPE_TAG, "application/jwt");
        Mockito.when(synapseCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);

        dcrUtilMockedStatic.when(() -> DCRUtil.buildMessagePayloadFromMessageContext(any(), anyString()))
                .thenReturn(Optional.of(DCRTestConstants.DCR_REQUEST));
        dcrUtilMockedStatic.when(() -> DCRUtil.decodeRequestJWT(anyString(), anyString()))
                .thenReturn(DCRTestConstants.DECODED_DCR_REQUEST);
        dcrUtilMockedStatic.when(() -> DCRUtil.validateRequestSignature(anyString(), any(), anyString(), anyInt()))
                .thenAnswer(invocation -> {
                    log.info("Mocked Log: " + invocation.getArgument(0));
                    return null;
                });

        dcrUtilMockedStatic.when(() -> DCRUtil.appendISDcrRequestPayloadAttributes(
                any(), any(), any(), anyBoolean(), anyString(), anyString()))
                .thenAnswer(invocation -> {
                    JSONObject req = invocation.getArgument(1);
                    return null;
                });

        ArgumentCaptor<String> jsonPayloadCaptor = ArgumentCaptor.forClass(String.class);
        jsonUtilMockedStatic.when(() -> JsonUtil.getNewJsonPayload(eq(synapseCtx), jsonPayloadCaptor.capture(),
                eq(true), eq(true))).thenReturn(null);

        mediator.mediate(axis2Ctx);

        String jsonPassedToJsonUtil = jsonPayloadCaptor.getValue();
        JsonNode expectedNode = new ObjectMapper().readTree(DCRTestConstants.DECODED_DCR_REQUEST);
        JsonNode actualNode = new ObjectMapper().readTree(jsonPassedToJsonUtil);
        Assert.assertTrue(expectedNode.equals(actualNode));
    }

    @Test
    public void testMediateMethodForPOSTForJson() throws JsonProcessingException {

        Mockito.when(axis2Ctx.getProperty(DCRConstants.HTTP_METHOD)).thenReturn("POST");
        headers.put(DCRConstants.CONTENT_TYPE_TAG, "application/json");
        Mockito.when(synapseCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);

        dcrUtilMockedStatic.when(() -> DCRUtil.buildMessagePayloadFromMessageContext(any(), anyString()))
                .thenReturn(Optional.of(DCRTestConstants.DECODED_DCR_REQUEST));

        dcrUtilMockedStatic.when(() -> DCRUtil.appendISDcrRequestPayloadAttributes(
                        any(), any(), any(), anyBoolean(), anyString(), anyString()))
                .thenAnswer(invocation -> {
                    return null;
                });

        ArgumentCaptor<String> jsonPayloadCaptor = ArgumentCaptor.forClass(String.class);
        jsonUtilMockedStatic.when(() -> JsonUtil.getNewJsonPayload(eq(synapseCtx), jsonPayloadCaptor.capture(),
                eq(true), eq(true))).thenReturn(null);

        mediator.mediate(axis2Ctx);

        String jsonPassedToJsonUtil = jsonPayloadCaptor.getValue();
        JsonNode expectedNode = new ObjectMapper().readTree(DCRTestConstants.DECODED_DCR_REQUEST);
        JsonNode actualNode = new ObjectMapper().readTree(jsonPassedToJsonUtil);
        Assert.assertTrue(expectedNode.equals(actualNode));
    }

    @Test
    public void testMediateMethodForPOSTForJsonWithInvalidSSA() {

        Mockito.when(axis2Ctx.getProperty(DCRConstants.HTTP_METHOD)).thenReturn("POST");
        headers.put(DCRConstants.CONTENT_TYPE_TAG, "application/json");
        Mockito.when(synapseCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);

        dcrUtilMockedStatic.when(() -> DCRUtil.buildMessagePayloadFromMessageContext(any(), anyString()))
                .thenReturn(Optional.of(DCRTestConstants.DECODED_DCR_REQUEST));
        dcrUtilMockedStatic.when(() -> DCRUtil.decodeRequestJWT(anyString(), anyString()))
                .thenThrow(ParseException.class);

        dcrUtilMockedStatic.when(() -> DCRUtil.returnSynapseHandlerJSONError(any(), any(), any()))
                .thenAnswer(invocation -> {
                    Object payload = invocation.getArgument(2);
                    log.info(">>> Payload passed to returnSynapseHandlerJSONError: " + payload);
                    return null;
                });

        dcrUtilMockedStatic.when(() -> DCRUtil.getErrorResponse(anyString(), anyString()))
                .thenReturn(getErrorResponse("invalid_request", "Malformed Software Statement JWT found"));

        boolean result = mediator.mediate(axis2Ctx);

        Assert.assertFalse(result);

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        dcrUtilMockedStatic.verify(() -> DCRUtil.returnSynapseHandlerJSONError(eq(axis2Ctx), eq("400"),
                jsonCaptor.capture()), atLeastOnce());

        String capturedPayload = jsonCaptor.getValue();
        Assert.assertNotNull(capturedPayload);
        Assert.assertTrue(capturedPayload.contains(DCRConstants.INVALID_REQUEST));
        Assert.assertTrue(capturedPayload.contains("Malformed Software Statement JWT found"));
    }

    @Test
    public void testMediateMethodWithoutPayload() {

        Mockito.when(axis2Ctx.getProperty(DCRConstants.HTTP_METHOD)).thenReturn("POST");
        headers.put(DCRConstants.CONTENT_TYPE_TAG, "application/jwt");
        Mockito.when(synapseCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);

        dcrUtilMockedStatic.when(() -> DCRUtil.buildMessagePayloadFromMessageContext(any(), anyString()))
                .thenReturn(Optional.empty());
        dcrUtilMockedStatic.when(() -> DCRUtil.appendISDcrRequestPayloadAttributes(
                        any(), any(), any(), anyBoolean(), anyString(), anyString()))
                .thenAnswer(invocation -> {
                    JSONObject req = invocation.getArgument(1);
                    return null;
                });

        ArgumentCaptor<String> jsonPayloadCaptor = ArgumentCaptor.forClass(String.class);
        jsonUtilMockedStatic.when(() -> JsonUtil.getNewJsonPayload(eq(synapseCtx), jsonPayloadCaptor.capture(),
                eq(true), eq(true))).thenReturn(null);
        dcrUtilMockedStatic.when(() -> DCRUtil.returnSynapseHandlerJSONError(any(), any(), any()))
                .thenAnswer(invocation -> {
                    Object payload = invocation.getArgument(2);
                    log.info(">>> Payload passed to returnSynapseHandlerJSONError: " + payload);
                    return null;
                });

        dcrUtilMockedStatic.when(() -> DCRUtil.getErrorResponse(anyString(), anyString()))
                .thenReturn(getErrorResponse("invalid_request", "Malformed Software Statement JWT found"));

        boolean result = mediator.mediate(axis2Ctx);

        Assert.assertFalse(result);

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        dcrUtilMockedStatic.verify(() -> DCRUtil.returnSynapseHandlerJSONError(eq(axis2Ctx), eq("400"),
                jsonCaptor.capture()), atLeastOnce());

        String capturedPayload = jsonCaptor.getValue();
        Assert.assertNotNull(capturedPayload);
        Assert.assertTrue(capturedPayload.contains(DCRConstants.INVALID_REQUEST));
        Assert.assertTrue(capturedPayload.contains("Malformed Software Statement JWT found"));
    }

    @Test
    public void testMediateMethodPayloadBuildThrowingException() {

        Mockito.when(axis2Ctx.getProperty(DCRConstants.HTTP_METHOD)).thenReturn("POST");
        headers.put(DCRConstants.CONTENT_TYPE_TAG, "application/jwt");
        Mockito.when(synapseCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);

        dcrUtilMockedStatic.when(() -> DCRUtil.buildMessagePayloadFromMessageContext(any(), anyString()))
                .thenThrow(DCRHandlingException.class);

        dcrUtilMockedStatic.when(() -> DCRUtil.returnSynapseHandlerJSONError(any(), any(), any()))
                .thenAnswer(invocation -> {
                    Object payload = invocation.getArgument(2);
                    log.info(">>> Payload passed to returnSynapseHandlerJSONError: " + payload);
                    return null;
                });

        dcrUtilMockedStatic.when(() -> DCRUtil.getErrorResponse(anyString(), anyString()))
                .thenReturn(getErrorResponse("invalid_request", "invalid request found"));

        boolean result = mediator.mediate(axis2Ctx);

        Assert.assertFalse(result);

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        dcrUtilMockedStatic.verify(() -> DCRUtil.returnSynapseHandlerJSONError(eq(axis2Ctx), eq("400"),
                jsonCaptor.capture()), atLeastOnce());

        String capturedPayload = jsonCaptor.getValue();
        Assert.assertNotNull(capturedPayload);
        Assert.assertTrue(capturedPayload.contains(DCRConstants.INVALID_REQUEST));
        Assert.assertTrue(capturedPayload.contains("invalid request found"));
    }

    @Test
    public void testMediateMethodWithNullPayload() {

        Mockito.when(axis2Ctx.getProperty(DCRConstants.HTTP_METHOD)).thenReturn("POST");
        headers.put(DCRConstants.CONTENT_TYPE_TAG, "application/jwt");
        Mockito.when(synapseCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);

        dcrUtilMockedStatic.when(() -> DCRUtil.buildMessagePayloadFromMessageContext(any(), anyString()))
                .thenReturn(Optional.empty());

        dcrUtilMockedStatic.when(() -> DCRUtil.returnSynapseHandlerJSONError(any(), any(), any()))
                .thenAnswer(invocation -> {
                    Object payload = invocation.getArgument(2);
                    log.info(">>> Payload passed to returnSynapseHandlerJSONError: " + payload);
                    return null;
                });

        dcrUtilMockedStatic.when(() -> DCRUtil.getErrorResponse(anyString(), anyString()))
                .thenReturn(getErrorResponse("invalid_request", "invalid request found"));

        boolean result = mediator.mediate(axis2Ctx);

        Assert.assertFalse(result);

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        dcrUtilMockedStatic.verify(() -> DCRUtil.returnSynapseHandlerJSONError(eq(axis2Ctx), eq("400"),
                jsonCaptor.capture()), atLeastOnce());

        String capturedPayload = jsonCaptor.getValue();
        Assert.assertNotNull(capturedPayload);
        Assert.assertTrue(capturedPayload.contains(DCRConstants.INVALID_REQUEST));
        Assert.assertTrue(capturedPayload.contains("invalid request found"));
    }

    @Test
    public void testMediateMethodForPOSTWithInvalidJWTPayload() {

        Mockito.when(axis2Ctx.getProperty(DCRConstants.HTTP_METHOD)).thenReturn("POST");
        headers.put(DCRConstants.CONTENT_TYPE_TAG, "application/jwt");
        Mockito.when(synapseCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);

        dcrUtilMockedStatic.when(() -> DCRUtil.buildMessagePayloadFromMessageContext(any(), anyString()))
                .thenReturn(Optional.of("jwbvfuegjbwbfuhdvgwgvydycdcbvywvcydcydbycwvuvyvwyevy"));
        dcrUtilMockedStatic.when(() -> DCRUtil.decodeRequestJWT(anyString(), anyString()))
                .thenThrow(ParseException.class);

        dcrUtilMockedStatic.when(() -> DCRUtil.returnSynapseHandlerJSONError(any(), any(), any()))
                .thenAnswer(invocation -> {
                    Object payload = invocation.getArgument(2);
                    log.info(">>> Payload passed to returnSynapseHandlerJSONError: " + payload);
                    return null;
                });

        dcrUtilMockedStatic.when(() -> DCRUtil.getErrorResponse(anyString(), anyString()))
                .thenReturn(getErrorResponse("invalid_request", "invalid request found"));

        boolean result = mediator.mediate(axis2Ctx);

        Assert.assertFalse(result);

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        dcrUtilMockedStatic.verify(() -> DCRUtil.returnSynapseHandlerJSONError(eq(axis2Ctx), eq("400"),
                jsonCaptor.capture()), atLeastOnce());

        String capturedPayload = jsonCaptor.getValue();
        Assert.assertNotNull(capturedPayload);
        Assert.assertTrue(capturedPayload.contains(DCRConstants.INVALID_REQUEST));
        Assert.assertTrue(capturedPayload.contains("invalid request found"));
    }

    @Test
    public void testMediateMethodForPOSTWithNullJWTPayload() {

        Mockito.when(axis2Ctx.getProperty(DCRConstants.HTTP_METHOD)).thenReturn("POST");
        headers.put(DCRConstants.CONTENT_TYPE_TAG, "application/jwt");
        Mockito.when(synapseCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);

        dcrUtilMockedStatic.when(() -> DCRUtil.buildMessagePayloadFromMessageContext(any(), anyString()))
                .thenReturn(Optional.of("jwbvfuegjbwbfuhdvgwgvydycdcbvywvcydcydbycwvuvyvwyevy"));
        dcrUtilMockedStatic.when(() -> DCRUtil.decodeRequestJWT(anyString(), anyString()))
                .thenReturn(null);

        dcrUtilMockedStatic.when(() -> DCRUtil.returnSynapseHandlerJSONError(any(), any(), any()))
                .thenAnswer(invocation -> {
                    Object payload = invocation.getArgument(2);
                    log.info(">>> Payload passed to returnSynapseHandlerJSONError: " + payload);
                    return null;
                });

        dcrUtilMockedStatic.when(() -> DCRUtil.getErrorResponse(anyString(), anyString()))
                .thenReturn(getErrorResponse("invalid_request", "invalid request found"));

        boolean result = mediator.mediate(axis2Ctx);

        Assert.assertFalse(result);

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        dcrUtilMockedStatic.verify(() -> DCRUtil.returnSynapseHandlerJSONError(eq(axis2Ctx), eq("400"),
                jsonCaptor.capture()), atLeastOnce());

        String capturedPayload = jsonCaptor.getValue();
        Assert.assertNotNull(capturedPayload);
        Assert.assertTrue(capturedPayload.contains(DCRConstants.INVALID_REQUEST));
        Assert.assertTrue(capturedPayload.contains("invalid request found"));
    }

    @Test
    public void testMediateMethodWithInvalidSignature() {

        Mockito.when(axis2Ctx.getProperty(DCRConstants.HTTP_METHOD)).thenReturn("POST");
        headers.put(DCRConstants.CONTENT_TYPE_TAG, "application/jwt");
        Mockito.when(synapseCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);

        dcrUtilMockedStatic.when(() -> DCRUtil.buildMessagePayloadFromMessageContext(any(), anyString()))
                .thenReturn(Optional.of(DCRTestConstants.DCR_REQUEST));
        dcrUtilMockedStatic.when(() -> DCRUtil.decodeRequestJWT(anyString(), anyString()))
                .thenReturn(DCRTestConstants.DECODED_DCR_REQUEST);
        dcrUtilMockedStatic.when(() -> DCRUtil.validateRequestSignature(anyString(), any(), anyString(), anyInt()))
                .thenThrow(ParseException.class);
        dcrUtilMockedStatic.when(() -> DCRUtil.validateJWTSignature(anyString(), anyString(), anyString(), anyInt()))
                .thenThrow(ParseException.class);

        dcrUtilMockedStatic.when(() -> DCRUtil.returnSynapseHandlerJSONError(any(), any(), any()))
                .thenAnswer(invocation -> {
                    Object payload = invocation.getArgument(2);
                    log.info(">>> Payload passed to returnSynapseHandlerJSONError: " + payload);
                    return null;
                });

        dcrUtilMockedStatic.when(() -> DCRUtil.getErrorResponse(anyString(), anyString()))
                .thenReturn(getErrorResponse("invalid_request", "invalid request found"));

        boolean result = mediator.mediate(axis2Ctx);

        Assert.assertFalse(result);

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        dcrUtilMockedStatic.verify(() -> DCRUtil.returnSynapseHandlerJSONError(eq(axis2Ctx), eq("400"),
                jsonCaptor.capture()), atLeastOnce());

        String capturedPayload = jsonCaptor.getValue();
        Assert.assertNotNull(capturedPayload);
        Assert.assertTrue(capturedPayload.contains(DCRConstants.INVALID_REQUEST));
        Assert.assertTrue(capturedPayload.contains("invalid request found"));
    }

    @Test
    public void testMediateMethodForGET() {

        Mockito.when(axis2Ctx.getProperty(DCRConstants.HTTP_METHOD)).thenReturn("GET");
        headers.put(DCRConstants.AUTHORIZATION, "Bearer eyJ4NXQiOiIxbEhzaTRCM0VRbWNsVXFQZjdES0l0QXlxWlUiLCJraWQi" +
                "OiJNMkl6TkRFMllUWXdPV0kzWTJNMk1tVmtORFV5WkRkaU9HTmhaVGN5TWpCaFpHWmpZbUV4TldJMU0yTmxNV0l3TTJSak0" +
                "yRXdNalkyWldabFpEVXlZZ19SUzI1NiIsInR5cCI6ImF0K2p3dCIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJqNEJ6aW4xb2" +
                "syRDR3b1FSbzJ4UGNDYndCRlVhIiwiYXV0IjoiQVBQTElDQVRJT04iLCJiaW5kaW5nX3R5cGUiOiJjZXJ0aWZpY2F0ZSIsI" +
                "mlzcyI6Imh0dHBzOlwvXC9sb2NhbGhvc3Q6OTQ0Nlwvb2F1dGgyXC90b2tlbiIsImNsaWVudF9pZCI6Imo0QnppbjFvazJE" +
                "NHdvUVJvMnhQY0Nid0JGVWEiLCJhdWQiOiJqNEJ6aW4xb2syRDR3b1FSbzJ4UGNDYndCRlVhIiwibmJmIjoxNzQ5Nzk1ODE" +
                "zLCJhenAiOiJqNEJ6aW4xb2syRDR3b1FSbzJ4UGNDYndCRlVhIiwib3JnX2lkIjoiMTAwODRhOGQtMTEzZi00MjExLWEwZD" +
                "UtZWZlMzZiMDgyMjExIiwic2NvcGUiOiJhY2NvdW50cyIsImNuZiI6eyJ4NXQjUzI1NiI6InNOMmVRaTdqejkxNGVnOGVxY" +
                "Xl2VVhYNEx6YVlzTEQ4amtQRUpGYWpyem8ifSwiZXhwIjoxNzQ5Nzk5NDEzLCJvcmdfbmFtZSI6IlN1cGVyIiwiaWF0Ijox" +
                "NzQ5Nzk1ODEzLCJiaW5kaW5nX3JlZiI6ImE4ZTRkN2U1NDg1MWFkZDQ3N2U0ODQ2ODNhMTIyMjNmIiwianRpIjoiODE4NTA" +
                "xMjEtMTYzOS00YjVkLTg0ZWYtOTMyYTg4YmZmZjgzIn0.DsvpUQvDFBMw6z0_fOvBDNasgL4wpL8nq9YlfNdGcgSWyYT-cW" +
                "8BBUFhdfAPjF1_VsygfHFyvEbcbiTz_wmflfgZ8nx8PLR-aI2btoPm5Alymp2KnGIIb6j4h2uByRAwafs3_CFTdQesvFwMh" +
                "uiV0fUvMuyHfgzwKj3syqQIykFiFGzhQAII-lELMlZ7B8PrWgF4hiG7pFtZf56m9wEGYAqkYcejufojwxVzC79C5emBELaL" +
                "O3WaUETirFhM-UukKO2Nqey8Y31P_47nxa3X8Tbv_LksUT4A7qAoFhqzUvQNU2PTfCHtIPwPdeQ-yY1IIEzpy0g39rZ5Gia" +
                "snuuyQw");
        Mockito.when(axis2Ctx.getProperty(DCRConstants.API_UT_RESOURCE))
                .thenReturn("/register/j4Bzin1ok2D4woQRo2xPcCbwBFUa");
        Mockito.when(synapseCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);

        dcrUtilMockedStatic.when(() -> DCRUtil.decodeRequestJWT(anyString(), anyString()))
                .thenReturn(DCRTestConstants.DECODED_ACCESS_TOKEN);

        boolean result = mediator.mediate(axis2Ctx);

        Assert.assertTrue(result);
    }

    @Test
    public void testMediateMethodForGETWithClientIDNotMatchingWithToken() {

        Mockito.when(axis2Ctx.getProperty(DCRConstants.HTTP_METHOD)).thenReturn("GET");
        headers.put(DCRConstants.AUTHORIZATION, "Bearer eyJ4NXQiOiIxbEhzaTRCM0VRbWNsVXFQZjdES0l0QXlxWlUiLCJraWQi" +
                "OiJNMkl6TkRFMllUWXdPV0kzWTJNMk1tVmtORFV5WkRkaU9HTmhaVGN5TWpCaFpHWmpZbUV4TldJMU0yTmxNV0l3TTJSak0" +
                "yRXdNalkyWldabFpEVXlZZ19SUzI1NiIsInR5cCI6ImF0K2p3dCIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJqNEJ6aW4xb2" +
                "syRDR3b1FSbzJ4UGNDYndCRlVhIiwiYXV0IjoiQVBQTElDQVRJT04iLCJiaW5kaW5nX3R5cGUiOiJjZXJ0aWZpY2F0ZSIsI" +
                "mlzcyI6Imh0dHBzOlwvXC9sb2NhbGhvc3Q6OTQ0Nlwvb2F1dGgyXC90b2tlbiIsImNsaWVudF9pZCI6Imo0QnppbjFvazJE" +
                "NHdvUVJvMnhQY0Nid0JGVWEiLCJhdWQiOiJqNEJ6aW4xb2syRDR3b1FSbzJ4UGNDYndCRlVhIiwibmJmIjoxNzQ5Nzk1ODE" +
                "zLCJhenAiOiJqNEJ6aW4xb2syRDR3b1FSbzJ4UGNDYndCRlVhIiwib3JnX2lkIjoiMTAwODRhOGQtMTEzZi00MjExLWEwZD" +
                "UtZWZlMzZiMDgyMjExIiwic2NvcGUiOiJhY2NvdW50cyIsImNuZiI6eyJ4NXQjUzI1NiI6InNOMmVRaTdqejkxNGVnOGVxY" +
                "Xl2VVhYNEx6YVlzTEQ4amtQRUpGYWpyem8ifSwiZXhwIjoxNzQ5Nzk5NDEzLCJvcmdfbmFtZSI6IlN1cGVyIiwiaWF0Ijox" +
                "NzQ5Nzk1ODEzLCJiaW5kaW5nX3JlZiI6ImE4ZTRkN2U1NDg1MWFkZDQ3N2U0ODQ2ODNhMTIyMjNmIiwianRpIjoiODE4NTA" +
                "xMjEtMTYzOS00YjVkLTg0ZWYtOTMyYTg4YmZmZjgzIn0.DsvpUQvDFBMw6z0_fOvBDNasgL4wpL8nq9YlfNdGcgSWyYT-cW" +
                "8BBUFhdfAPjF1_VsygfHFyvEbcbiTz_wmflfgZ8nx8PLR-aI2btoPm5Alymp2KnGIIb6j4h2uByRAwafs3_CFTdQesvFwMh" +
                "uiV0fUvMuyHfgzwKj3syqQIykFiFGzhQAII-lELMlZ7B8PrWgF4hiG7pFtZf56m9wEGYAqkYcejufojwxVzC79C5emBELaL" +
                "O3WaUETirFhM-UukKO2Nqey8Y31P_47nxa3X8Tbv_LksUT4A7qAoFhqzUvQNU2PTfCHtIPwPdeQ-yY1IIEzpy0g39rZ5Gia" +
                "snuuyQw");
        Mockito.when(axis2Ctx.getProperty(DCRConstants.API_UT_RESOURCE))
                .thenReturn("/register/test_client_id");
        Mockito.when(synapseCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);

        dcrUtilMockedStatic.when(() -> DCRUtil.decodeRequestJWT(anyString(), anyString()))
                .thenReturn(DCRTestConstants.DECODED_ACCESS_TOKEN);

        dcrUtilMockedStatic.when(() -> DCRUtil.returnSynapseHandlerJSONError(any(), any(), any()))
                .thenAnswer(invocation -> {
                    Object payload = invocation.getArgument(2);
                    log.info(">>> Payload passed to returnSynapseHandlerJSONError: " + payload);
                    return null;
                });

        dcrUtilMockedStatic.when(() -> DCRUtil.getErrorResponse(anyString(), anyString()))
                .thenReturn(getErrorResponse("invalid_request", "invalid request found"));

        boolean result = mediator.mediate(axis2Ctx);

        Assert.assertFalse(result);

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        dcrUtilMockedStatic.verify(() -> DCRUtil.returnSynapseHandlerJSONError(eq(axis2Ctx), eq("400"),
                jsonCaptor.capture()), atLeastOnce());

        String capturedPayload = jsonCaptor.getValue();
        Assert.assertNotNull(capturedPayload);
        Assert.assertTrue(capturedPayload.contains(DCRConstants.INVALID_REQUEST));
        Assert.assertTrue(capturedPayload.contains("invalid request found"));
    }

    @Test
    public void testMediateMethodForGETEmptyResource() {

        Mockito.when(axis2Ctx.getProperty(DCRConstants.HTTP_METHOD)).thenReturn("GET");
        headers.put(DCRConstants.AUTHORIZATION, "Bearer eyJ4NXQiOiIxbEhzaTRCM0VRbWNsVXFQZjdES0l0QXlxWlUiLCJraWQi" +
                "OiJNMkl6TkRFMllUWXdPV0kzWTJNMk1tVmtORFV5WkRkaU9HTmhaVGN5TWpCaFpHWmpZbUV4TldJMU0yTmxNV0l3TTJSak0" +
                "yRXdNalkyWldabFpEVXlZZ19SUzI1NiIsInR5cCI6ImF0K2p3dCIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJqNEJ6aW4xb2" +
                "syRDR3b1FSbzJ4UGNDYndCRlVhIiwiYXV0IjoiQVBQTElDQVRJT04iLCJiaW5kaW5nX3R5cGUiOiJjZXJ0aWZpY2F0ZSIsI" +
                "mlzcyI6Imh0dHBzOlwvXC9sb2NhbGhvc3Q6OTQ0Nlwvb2F1dGgyXC90b2tlbiIsImNsaWVudF9pZCI6Imo0QnppbjFvazJE" +
                "NHdvUVJvMnhQY0Nid0JGVWEiLCJhdWQiOiJqNEJ6aW4xb2syRDR3b1FSbzJ4UGNDYndCRlVhIiwibmJmIjoxNzQ5Nzk1ODE" +
                "zLCJhenAiOiJqNEJ6aW4xb2syRDR3b1FSbzJ4UGNDYndCRlVhIiwib3JnX2lkIjoiMTAwODRhOGQtMTEzZi00MjExLWEwZD" +
                "UtZWZlMzZiMDgyMjExIiwic2NvcGUiOiJhY2NvdW50cyIsImNuZiI6eyJ4NXQjUzI1NiI6InNOMmVRaTdqejkxNGVnOGVxY" +
                "Xl2VVhYNEx6YVlzTEQ4amtQRUpGYWpyem8ifSwiZXhwIjoxNzQ5Nzk5NDEzLCJvcmdfbmFtZSI6IlN1cGVyIiwiaWF0Ijox" +
                "NzQ5Nzk1ODEzLCJiaW5kaW5nX3JlZiI6ImE4ZTRkN2U1NDg1MWFkZDQ3N2U0ODQ2ODNhMTIyMjNmIiwianRpIjoiODE4NTA" +
                "xMjEtMTYzOS00YjVkLTg0ZWYtOTMyYTg4YmZmZjgzIn0.DsvpUQvDFBMw6z0_fOvBDNasgL4wpL8nq9YlfNdGcgSWyYT-cW" +
                "8BBUFhdfAPjF1_VsygfHFyvEbcbiTz_wmflfgZ8nx8PLR-aI2btoPm5Alymp2KnGIIb6j4h2uByRAwafs3_CFTdQesvFwMh" +
                "uiV0fUvMuyHfgzwKj3syqQIykFiFGzhQAII-lELMlZ7B8PrWgF4hiG7pFtZf56m9wEGYAqkYcejufojwxVzC79C5emBELaL" +
                "O3WaUETirFhM-UukKO2Nqey8Y31P_47nxa3X8Tbv_LksUT4A7qAoFhqzUvQNU2PTfCHtIPwPdeQ-yY1IIEzpy0g39rZ5Gia" +
                "snuuyQw");
        Mockito.when(axis2Ctx.getProperty(DCRConstants.API_UT_RESOURCE))
                .thenReturn("");
        Mockito.when(synapseCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);

        dcrUtilMockedStatic.when(() -> DCRUtil.decodeRequestJWT(anyString(), anyString()))
                .thenReturn(DCRTestConstants.DECODED_ACCESS_TOKEN);

        dcrUtilMockedStatic.when(() -> DCRUtil.returnSynapseHandlerJSONError(any(), any(), any()))
                .thenAnswer(invocation -> {
                    Object payload = invocation.getArgument(2);
                    log.info(">>> Payload passed to returnSynapseHandlerJSONError: " + payload);
                    return null;
                });

        dcrUtilMockedStatic.when(() -> DCRUtil.getErrorResponse(anyString(), anyString()))
                .thenReturn(getErrorResponse("invalid_request", "invalid request found"));

        boolean result = mediator.mediate(axis2Ctx);

        Assert.assertFalse(result);

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        dcrUtilMockedStatic.verify(() -> DCRUtil.returnSynapseHandlerJSONError(eq(axis2Ctx), eq("400"),
                jsonCaptor.capture()), atLeastOnce());

        String capturedPayload = jsonCaptor.getValue();
        Assert.assertNotNull(capturedPayload);
        Assert.assertTrue(capturedPayload.contains(DCRConstants.INVALID_REQUEST));
        Assert.assertTrue(capturedPayload.contains("invalid request found"));
    }

    @Test
    public void testMediateMethodForGETWithoutAuthorizationHeader() {

        Mockito.when(axis2Ctx.getProperty(DCRConstants.HTTP_METHOD)).thenReturn("GET");
        Mockito.when(axis2Ctx.getProperty(DCRConstants.API_UT_RESOURCE))
                .thenReturn("/register/j4Bzin1ok2D4woQRo2xPcCbwBFUa");
        headers.remove(DCRConstants.AUTHORIZATION);
        Mockito.when(synapseCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);

        dcrUtilMockedStatic.when(() -> DCRUtil.decodeRequestJWT(anyString(), anyString()))
                .thenReturn(DCRTestConstants.DECODED_ACCESS_TOKEN);

        dcrUtilMockedStatic.when(() -> DCRUtil.returnSynapseHandlerJSONError(any(), any(), any()))
                .thenAnswer(invocation -> {
                    Object payload = invocation.getArgument(2);
                    log.info(">>> Payload passed to returnSynapseHandlerJSONError: " + payload);
                    return null;
                });

        dcrUtilMockedStatic.when(() -> DCRUtil.getErrorResponse(anyString(), anyString()))
                .thenReturn(getErrorResponse("invalid_request", "invalid request found"));

        boolean result = mediator.mediate(axis2Ctx);

        Assert.assertFalse(result);

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        dcrUtilMockedStatic.verify(() -> DCRUtil.returnSynapseHandlerJSONError(eq(axis2Ctx), eq("400"),
                jsonCaptor.capture()), atLeastOnce());

        String capturedPayload = jsonCaptor.getValue();
        Assert.assertNotNull(capturedPayload);
        Assert.assertTrue(capturedPayload.contains(DCRConstants.INVALID_REQUEST));
        Assert.assertTrue(capturedPayload.contains("invalid request found"));
    }

    @Test
    public void testMediateMethodForGETWithDecodingException() {

        Mockito.when(axis2Ctx.getProperty(DCRConstants.HTTP_METHOD)).thenReturn("GET");
        headers.put(DCRConstants.AUTHORIZATION, "Bearer eyJ4NXQiOiIxbEhzaTRCM0VRbWNsVXFQZjdES0l0QXlxWlUiLCJraWQi" +
                "OiJNMkl6TkRFMllUWXdPV0kzWTJNMk1tVmtORFV5WkRkaU9HTmhaVGN5TWpCaFpHWmpZbUV4TldJMU0yTmxNV0l3TTJSak0" +
                "yRXdNalkyWldabFpEVXlZZ19SUzI1NiIsInR5cCI6ImF0K2p3dCIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJqNEJ6aW4xb2" +
                "syRDR3b1FSbzJ4UGNDYndCRlVhIiwiYXV0IjoiQVBQTElDQVRJT04iLCJiaW5kaW5nX3R5cGUiOiJjZXJ0aWZpY2F0ZSIsI" +
                "mlzcyI6Imh0dHBzOlwvXC9sb2NhbGhvc3Q6OTQ0Nlwvb2F1dGgyXC90b2tlbiIsImNsaWVudF9pZCI6Imo0QnppbjFvazJE" +
                "NHdvUVJvMnhQY0Nid0JGVWEiLCJhdWQiOiJqNEJ6aW4xb2syRDR3b1FSbzJ4UGNDYndCRlVhIiwibmJmIjoxNzQ5Nzk1ODE" +
                "zLCJhenAiOiJqNEJ6aW4xb2syRDR3b1FSbzJ4UGNDYndCRlVhIiwib3JnX2lkIjoiMTAwODRhOGQtMTEzZi00MjExLWEwZD" +
                "UtZWZlMzZiMDgyMjExIiwic2NvcGUiOiJhY2NvdW50cyIsImNuZiI6eyJ4NXQjUzI1NiI6InNOMmVRaTdqejkxNGVnOGVxY" +
                "Xl2VVhYNEx6YVlzTEQ4amtQRUpGYWpyem8ifSwiZXhwIjoxNzQ5Nzk5NDEzLCJvcmdfbmFtZSI6IlN1cGVyIiwiaWF0Ijox" +
                "NzQ5Nzk1ODEzLCJiaW5kaW5nX3JlZiI6ImE4ZTRkN2U1NDg1MWFkZDQ3N2U0ODQ2ODNhMTIyMjNmIiwianRpIjoiODE4NTA" +
                "xMjEtMTYzOS00YjVkLTg0ZWYtOTMyYTg4YmZmZjgzIn0.DsvpUQvDFBMw6z0_fOvBDNasgL4wpL8nq9YlfNdGcgSWyYT-cW" +
                "8BBUFhdfAPjF1_VsygfHFyvEbcbiTz_wmflfgZ8nx8PLR-aI2btoPm5Alymp2KnGIIb6j4h2uByRAwafs3_CFTdQesvFwMh" +
                "uiV0fUvMuyHfgzwKj3syqQIykFiFGzhQAII-lELMlZ7B8PrWgF4hiG7pFtZf56m9wEGYAqkYcejufojwxVzC79C5emBELaL" +
                "O3WaUETirFhM-UukKO2Nqey8Y31P_47nxa3X8Tbv_LksUT4A7qAoFhqzUvQNU2PTfCHtIPwPdeQ-yY1IIEzpy0g39rZ5Gia" +
                "snuuyQw");
        Mockito.when(axis2Ctx.getProperty(DCRConstants.API_UT_RESOURCE))
                .thenReturn("/register/j4Bzin1ok2D4woQRo2xPcCbwBFUa");
        Mockito.when(synapseCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);

        dcrUtilMockedStatic.when(() -> DCRUtil.decodeRequestJWT(anyString(), anyString()))
                .thenThrow(ParseException.class);

        dcrUtilMockedStatic.when(() -> DCRUtil.returnSynapseHandlerJSONError(any(), any(), any()))
                .thenAnswer(invocation -> {
                    Object payload = invocation.getArgument(2);
                    log.info(">>> Payload passed to returnSynapseHandlerJSONError: " + payload);
                    return null;
                });

        dcrUtilMockedStatic.when(() -> DCRUtil.getErrorResponse(anyString(), anyString()))
                .thenReturn(getErrorResponse("invalid_request", "invalid request found"));

        boolean result = mediator.mediate(axis2Ctx);

        Assert.assertFalse(result);

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        dcrUtilMockedStatic.verify(() -> DCRUtil.returnSynapseHandlerJSONError(eq(axis2Ctx), eq("400"),
                jsonCaptor.capture()), atLeastOnce());

        String capturedPayload = jsonCaptor.getValue();
        Assert.assertNotNull(capturedPayload);
        Assert.assertTrue(capturedPayload.contains(DCRConstants.INVALID_REQUEST));
        Assert.assertTrue(capturedPayload.contains("invalid request found"));
    }

    private static String getErrorResponse(String code, String errorMessage) {

        JSONObject errorObj = new JSONObject();
        errorObj.put(DCRConstants.ERROR, code);
        errorObj.put(DCRConstants.ERROR_DESCRIPTION, errorMessage);
        return errorObj.toString();
    }
}

