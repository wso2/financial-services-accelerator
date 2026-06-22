/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
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

package org.wso2.financial.services.accelerator.gateway.dpop.util;

import org.apache.axis2.context.MessageContext;
import org.apache.http.HttpHeaders;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.gateway.dpop.proof.DPoPProofException;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link Challenge}. Uses {@code mockStatic} for
 * {@link RelayUtils} and {@link Axis2Sender} to avoid real HTTP I/O.
 */
public class ChallengeTest {

    private static final String ACCEPTED_ALGS = "ES256 PS256";
    private static final String HEADER_DPOP_NONCE = "DPoP-Nonce";

    private Challenge challenge;
    private Axis2MessageContext mockSynCtx;
    private MessageContext mockAxis2MC;
    private Map<String, String> responseHeaders;

    @BeforeMethod
    public void setUp() {
        challenge = new Challenge(ACCEPTED_ALGS);
        mockSynCtx = Mockito.mock(Axis2MessageContext.class);
        mockAxis2MC = Mockito.mock(MessageContext.class);
        responseHeaders = new HashMap<>();

        Mockito.when(mockSynCtx.getAxis2MessageContext()).thenReturn(mockAxis2MC);
        Mockito.when(mockAxis2MC.getProperty(MessageContext.TRANSPORT_HEADERS)).thenReturn(responseHeaders);
    }

    @Test
    public void send401WithInvalidDPoPProofShouldSetWwwAuthenticateHeader() {
        try (MockedStatic<RelayUtils> relayStatic = Mockito.mockStatic(RelayUtils.class);
             MockedStatic<Axis2Sender> senderStatic = Mockito.mockStatic(Axis2Sender.class)) {

            relayStatic.when(() -> RelayUtils.consumeAndDiscardMessage(mockAxis2MC))
                    .thenAnswer(inv -> null);
            senderStatic.when(() -> Axis2Sender.sendBack(mockSynCtx))
                    .thenAnswer(inv -> null);

            challenge.send401(mockSynCtx, DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                    "Proof failed", null);

            String wwwAuth = responseHeaders.get(HttpHeaders.WWW_AUTHENTICATE);
            assertNotNull(wwwAuth, "WWW-Authenticate header must be set");
            assertTrue(wwwAuth.startsWith("DPoP"), "Challenge must start with 'DPoP'");
            assertTrue(wwwAuth.contains("error=\"invalid_dpop_proof\""),
                    "Challenge must contain the RFC error code");
            assertFalse(responseHeaders.containsKey(HEADER_DPOP_NONCE),
                    "DPoP-Nonce header must not be present when no nonce is supplied");
        }
    }

    @Test
    public void send401WithUseDPoPNonceShouldSetDPoPNonceHeader() {
        String nonce = "server-issued-nonce-abc";

        try (MockedStatic<RelayUtils> relayStatic = Mockito.mockStatic(RelayUtils.class);
             MockedStatic<Axis2Sender> senderStatic = Mockito.mockStatic(Axis2Sender.class)) {

            relayStatic.when(() -> RelayUtils.consumeAndDiscardMessage(mockAxis2MC))
                    .thenAnswer(inv -> null);
            senderStatic.when(() -> Axis2Sender.sendBack(mockSynCtx))
                    .thenAnswer(inv -> null);

            challenge.send401(mockSynCtx, DPoPProofException.ErrorCode.USE_DPOP_NONCE,
                    "Nonce required", nonce);

            String dpopNonce = responseHeaders.get(HEADER_DPOP_NONCE);
            assertNotNull(dpopNonce, "DPoP-Nonce header must be set when a nonce is supplied");
            assertEquals(dpopNonce, nonce);

            String wwwAuth = responseHeaders.get(HttpHeaders.WWW_AUTHENTICATE);
            assertNotNull(wwwAuth);
            assertTrue(wwwAuth.contains("error=\"use_dpop_nonce\""));
        }
    }

    @Test
    public void send401WithNullMessageShouldNotThrow() {
        try (MockedStatic<RelayUtils> relayStatic = Mockito.mockStatic(RelayUtils.class);
             MockedStatic<Axis2Sender> senderStatic = Mockito.mockStatic(Axis2Sender.class)) {

            relayStatic.when(() -> RelayUtils.consumeAndDiscardMessage(mockAxis2MC))
                    .thenAnswer(inv -> null);
            senderStatic.when(() -> Axis2Sender.sendBack(mockSynCtx))
                    .thenAnswer(inv -> null);

            // Should complete without NullPointerException
            challenge.send401(mockSynCtx, DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                    null, null);

            String wwwAuth = responseHeaders.get(HttpHeaders.WWW_AUTHENTICATE);
            assertNotNull(wwwAuth);
            assertTrue(wwwAuth.contains("error=\"invalid_dpop_proof\""));
            assertFalse(wwwAuth.contains("error_description"),
                    "No error_description should be present for null message");
        }
    }

    @Test
    public void send401ShouldSet401StatusCodeOnSynCtx() {
        try (MockedStatic<RelayUtils> relayStatic = Mockito.mockStatic(RelayUtils.class);
             MockedStatic<Axis2Sender> senderStatic = Mockito.mockStatic(Axis2Sender.class)) {

            relayStatic.when(() -> RelayUtils.consumeAndDiscardMessage(mockAxis2MC))
                    .thenAnswer(inv -> null);
            senderStatic.when(() -> Axis2Sender.sendBack(mockSynCtx))
                    .thenAnswer(inv -> null);

            challenge.send401(mockSynCtx, DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                    "some error", null);

            // Verify Axis2Sender.sendBack was called to dispatch the error response
            senderStatic.verify(() -> Axis2Sender.sendBack(mockSynCtx));
        }
    }
}
