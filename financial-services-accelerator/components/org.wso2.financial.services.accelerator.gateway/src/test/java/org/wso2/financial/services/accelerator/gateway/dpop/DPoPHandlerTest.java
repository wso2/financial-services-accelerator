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

package org.wso2.financial.services.accelerator.gateway.dpop;

import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.gateway.dpop.binding.AccessTokenBinder;
import org.wso2.financial.services.accelerator.gateway.dpop.cache.DPoPCacheProvider;
import org.wso2.financial.services.accelerator.gateway.dpop.cache.DPoPJtiCache;
import org.wso2.financial.services.accelerator.gateway.dpop.cache.DPoPNonceCache;
import org.wso2.financial.services.accelerator.gateway.dpop.nonce.NonceStrategy;
import org.wso2.financial.services.accelerator.gateway.dpop.proof.DPoPProofException;
import org.wso2.financial.services.accelerator.gateway.dpop.proof.DPoPProofValidator;
import org.wso2.financial.services.accelerator.gateway.dpop.util.Challenge;
import org.wso2.financial.services.accelerator.gateway.dpop.util.DPoPUtils;
import org.wso2.financial.services.accelerator.gateway.internal.GatewayDataHolder;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link DPoPHandler}. All private collaborator fields are injected
 * via reflection so that {@code init()} — which requires Carbon/APIM runtime — is
 * never called.
 */
public class DPoPHandlerTest {

    private static final String AUTH_HEADER = "Authorization";
    private static final String HEADER_DPOP = "DPoP";
    private static final String BEARER_TAG = "Bearer ";
    private static final String DPOP_SCHEME = "DPoP";
    private static final String ACCESS_TOKEN = "test-access-token";
    private static final String DPOP_PROOF = "dpop.proof.jwt";
    private static final String JWK_THUMBPRINT = "some-jwk-thumbprint";
    private static final String JTI = "unique-jti";
    private static final String HTU = "https://api.example.com/resource";
    private static final String HTTP_METHOD = "GET";
    private static final long NONCE_TTL = 300L;
    private static final long JTI_TTL = 120L;

    private DPoPHandler handler;
    private DPoPProofValidator mockValidator;
    private DPoPCacheProvider mockCache;
    private AccessTokenBinder mockBinder;
    private Challenge mockChallenge;
    private NonceStrategy mockNonceStrategy;

    private org.apache.synapse.core.axis2.Axis2MessageContext mockSynCtx;
    private MessageContext mockAxis2MC;

    @BeforeClass
    public void beforeClass() {
        System.setProperty("carbon.home", "/");
    }

    @BeforeMethod
    public void setUp() throws Exception {
        handler = new DPoPHandler();
        mockValidator = Mockito.mock(DPoPProofValidator.class);
        mockCache = Mockito.mock(DPoPCacheProvider.class);
        mockBinder = Mockito.mock(AccessTokenBinder.class);
        mockChallenge = Mockito.mock(Challenge.class);
        mockNonceStrategy = Mockito.mock(NonceStrategy.class);

        // Inject all fields without calling init()
        setField("apiDPoPEnabled", true);
        setField("iatSkewSeconds", 60L);
        setField("jtiCacheTtlSeconds", JTI_TTL);
        setField("stripDpopHeader", false);
        setField("httpsRequired", false);
        setField("proofValidator", mockValidator);
        setField("cacheProvider", mockCache);
        setField("accessTokenBinder", mockBinder);
        setField("challenge", mockChallenge);
        setField("nonceStrategy", mockNonceStrategy);

        // Default: nonce not required, no rotation
        Mockito.when(mockNonceStrategy.requiresNonce(Mockito.anyString())).thenReturn(false);
        Mockito.when(mockNonceStrategy.shouldRotate(Mockito.anyString())).thenReturn(false);

        mockSynCtx = Mockito.mock(org.apache.synapse.core.axis2.Axis2MessageContext.class);
        mockAxis2MC = Mockito.mock(MessageContext.class);
        Mockito.when(mockSynCtx.getAxis2MessageContext()).thenReturn(mockAxis2MC);
    }

    @Test
    public void apiDPoPDisabledShouldPassThrough() throws Exception {
        setField("apiDPoPEnabled", false);

        boolean result = handler.handleRequest(mockSynCtx);

        assertTrue(result);
        Mockito.verifyNoInteractions(mockValidator, mockCache, mockBinder, mockChallenge);
    }

    @Test
    public void noTransportHeadersShouldPassThrough() {
        try (MockedStatic<DPoPUtils> utilsStatic = Mockito.mockStatic(DPoPUtils.class)) {
            utilsStatic.when(() -> DPoPUtils.getTransportHeaders(mockSynCtx))
                    .thenReturn(new TreeMap<>(String.CASE_INSENSITIVE_ORDER));

            boolean result = handler.handleRequest(mockSynCtx);

            assertTrue(result);
        }
    }

    @Test
    public void bearerTokenWithNoDPoPHeaderAndNoJktShouldPassThrough() throws Exception {
        Map<String, String> headers = makeHeaders(AUTH_HEADER, BEARER_TAG + ACCESS_TOKEN);

        try (MockedStatic<DPoPUtils> utilsStatic = Mockito.mockStatic(DPoPUtils.class)) {
            utilsStatic.when(() -> DPoPUtils.getTransportHeaders(mockSynCtx)).thenReturn(headers);
            utilsStatic.when(() -> DPoPUtils.extractToken(BEARER_TAG + ACCESS_TOKEN, "Bearer"))
                    .thenReturn(ACCESS_TOKEN);
            utilsStatic.when(() -> DPoPUtils.hasMultipleDPopHeaders(mockSynCtx)).thenReturn(false);

            Mockito.when(mockBinder.resolveJkt(ACCESS_TOKEN)).thenReturn(null);

            boolean result = handler.handleRequest(mockSynCtx);

            assertTrue(result);
            Mockito.verify(mockChallenge, Mockito.never())
                    .send401(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        }
    }

    @Test
    public void dpopBoundBearerTokenWithoutProofShouldReject() throws Exception {
        // No DPoP header, but token has cnf.jkt
        Map<String, String> headers = makeHeaders(AUTH_HEADER, BEARER_TAG + ACCESS_TOKEN);

        try (MockedStatic<DPoPUtils> utilsStatic = Mockito.mockStatic(DPoPUtils.class)) {
            utilsStatic.when(() -> DPoPUtils.getTransportHeaders(mockSynCtx)).thenReturn(headers);
            utilsStatic.when(() -> DPoPUtils.extractToken(BEARER_TAG + ACCESS_TOKEN, "Bearer"))
                    .thenReturn(ACCESS_TOKEN);
            utilsStatic.when(() -> DPoPUtils.hasMultipleDPopHeaders(mockSynCtx)).thenReturn(false);

            Mockito.when(mockBinder.resolveJkt(ACCESS_TOKEN)).thenReturn(JWK_THUMBPRINT);

            boolean result = handler.handleRequest(mockSynCtx);

            assertFalse(result);
            Mockito.verify(mockChallenge).send401(
                    Mockito.eq(mockSynCtx),
                    Mockito.eq(DPoPProofException.ErrorCode.INVALID_TOKEN),
                    Mockito.any(),
                    Mockito.isNull());
        }
    }

    @Test
    public void validDPoPRequestShouldSucceedAndRewriteAuthHeader() throws Exception {
        Map<String, String> headers = makeHeaders(
                AUTH_HEADER, DPOP_SCHEME + " " + ACCESS_TOKEN,
                HEADER_DPOP, DPOP_PROOF);

        DPoPProofValidator.ParsedProof mockParsed = Mockito.mock(DPoPProofValidator.ParsedProof.class);
        Mockito.when(mockParsed.getJwkThumbprint()).thenReturn(JWK_THUMBPRINT);
        Mockito.when(mockParsed.getKid()).thenReturn("kid-1");

        DPoPProofValidator.ValidationResult mockResult = Mockito.mock(DPoPProofValidator.ValidationResult.class);
        Mockito.when(mockResult.getProofJkt()).thenReturn(JWK_THUMBPRINT);
        Mockito.when(mockResult.getJti()).thenReturn(JTI);

        try (MockedStatic<DPoPUtils> utilsStatic = Mockito.mockStatic(DPoPUtils.class)) {
            utilsStatic.when(() -> DPoPUtils.getTransportHeaders(mockSynCtx)).thenReturn(headers);
            utilsStatic.when(() -> DPoPUtils.extractToken(
                    DPOP_SCHEME + " " + ACCESS_TOKEN, DPOP_SCHEME)).thenReturn(ACCESS_TOKEN);
            utilsStatic.when(() -> DPoPUtils.hasMultipleDPopHeaders(mockSynCtx)).thenReturn(false);
            utilsStatic.when(() -> DPoPUtils.normalizeHtu(mockSynCtx, headers)).thenReturn(HTU);
            utilsStatic.when(() -> DPoPUtils.removeHeader(Mockito.any(), Mockito.any()))
                    .thenAnswer(inv -> null);

            Mockito.when(mockAxis2MC.getProperty(Constants.Configuration.HTTP_METHOD))
                    .thenReturn(HTTP_METHOD);
            Mockito.when(mockValidator.parseDPoPProof(DPOP_PROOF)).thenReturn(mockParsed);
            Mockito.when(mockBinder.resolveJkt(ACCESS_TOKEN)).thenReturn(JWK_THUMBPRINT);
            Mockito.when(mockValidator.validate(mockParsed, HTTP_METHOD, HTU, ACCESS_TOKEN, null))
                    .thenReturn(mockResult);
            Mockito.when(mockCache.isJtiFirstUse(JTI, JWK_THUMBPRINT, JTI_TTL)).thenReturn(true);
            Mockito.doNothing().when(mockBinder).verifyBinding(JWK_THUMBPRINT, JWK_THUMBPRINT);

            boolean result = handler.handleRequest(mockSynCtx);

            assertTrue(result);
            Mockito.verify(mockChallenge, Mockito.never())
                    .send401(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        }
    }

    @Test
    public void parseDPoPProofExceptionShouldSend401AndReturnFalse() throws Exception {
        Map<String, String> headers = makeHeaders(
                AUTH_HEADER, DPOP_SCHEME + " " + ACCESS_TOKEN,
                HEADER_DPOP, DPOP_PROOF);

        try (MockedStatic<DPoPUtils> utilsStatic = Mockito.mockStatic(DPoPUtils.class)) {
            utilsStatic.when(() -> DPoPUtils.getTransportHeaders(mockSynCtx)).thenReturn(headers);
            utilsStatic.when(() -> DPoPUtils.extractToken(
                    DPOP_SCHEME + " " + ACCESS_TOKEN, DPOP_SCHEME)).thenReturn(ACCESS_TOKEN);
            utilsStatic.when(() -> DPoPUtils.hasMultipleDPopHeaders(mockSynCtx)).thenReturn(false);

            Mockito.when(mockValidator.parseDPoPProof(DPOP_PROOF))
                    .thenThrow(new DPoPProofException(
                            DPoPProofException.ErrorCode.INVALID_DPOP_PROOF, "Bad proof"));
            Mockito.when(mockBinder.resolveJkt(ACCESS_TOKEN)).thenReturn(null);

            boolean result = handler.handleRequest(mockSynCtx);

            assertFalse(result);
            Mockito.verify(mockChallenge).send401(
                    Mockito.eq(mockSynCtx),
                    Mockito.eq(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF),
                    Mockito.any(),
                    Mockito.isNull());
        }
    }

    @Test
    public void nonceRequiredButAbsentShouldIssueNonceAndSend401() throws Exception {
        Map<String, String> headers = makeHeaders(
                AUTH_HEADER, DPOP_SCHEME + " " + ACCESS_TOKEN,
                HEADER_DPOP, DPOP_PROOF);

        DPoPProofValidator.ParsedProof mockParsed = Mockito.mock(DPoPProofValidator.ParsedProof.class);
        Mockito.when(mockParsed.getJwkThumbprint()).thenReturn(JWK_THUMBPRINT);
        Mockito.when(mockParsed.getKid()).thenReturn(null);

        String issuedNonce = "fresh-server-nonce";

        try (MockedStatic<DPoPUtils> utilsStatic = Mockito.mockStatic(DPoPUtils.class)) {
            utilsStatic.when(() -> DPoPUtils.getTransportHeaders(mockSynCtx)).thenReturn(headers);
            utilsStatic.when(() -> DPoPUtils.extractToken(
                    DPOP_SCHEME + " " + ACCESS_TOKEN, DPOP_SCHEME)).thenReturn(ACCESS_TOKEN);
            utilsStatic.when(() -> DPoPUtils.hasMultipleDPopHeaders(mockSynCtx)).thenReturn(false);
            utilsStatic.when(() -> DPoPUtils.normalizeHtu(mockSynCtx, headers)).thenReturn(HTU);

            Mockito.when(mockAxis2MC.getProperty(Constants.Configuration.HTTP_METHOD))
                    .thenReturn(HTTP_METHOD);
            Mockito.when(mockValidator.parseDPoPProof(DPOP_PROOF)).thenReturn(mockParsed);
            Mockito.when(mockBinder.resolveJkt(ACCESS_TOKEN)).thenReturn(JWK_THUMBPRINT);
            // Nonce is required but no active nonce exists
            Mockito.when(mockNonceStrategy.requiresNonce(JWK_THUMBPRINT)).thenReturn(true);
            Mockito.when(mockCache.getActiveNonce(JWK_THUMBPRINT)).thenReturn(null);
            Mockito.when(mockCache.issueNonce(JWK_THUMBPRINT)).thenReturn(issuedNonce);

            boolean result = handler.handleRequest(mockSynCtx);

            assertFalse(result);
            Mockito.verify(mockCache).issueNonce(JWK_THUMBPRINT);
            Mockito.verify(mockChallenge).send401(
                    Mockito.eq(mockSynCtx),
                    Mockito.eq(DPoPProofException.ErrorCode.USE_DPOP_NONCE),
                    Mockito.any(),
                    Mockito.eq(issuedNonce));
        }
    }

    @Test
    public void jtiReplayShouldRejectRequest() throws Exception {
        Map<String, String> headers = makeHeaders(
                AUTH_HEADER, DPOP_SCHEME + " " + ACCESS_TOKEN,
                HEADER_DPOP, DPOP_PROOF);

        DPoPProofValidator.ParsedProof mockParsed = Mockito.mock(DPoPProofValidator.ParsedProof.class);
        Mockito.when(mockParsed.getJwkThumbprint()).thenReturn(JWK_THUMBPRINT);
        Mockito.when(mockParsed.getKid()).thenReturn(null);

        DPoPProofValidator.ValidationResult mockResult = Mockito.mock(DPoPProofValidator.ValidationResult.class);
        Mockito.when(mockResult.getProofJkt()).thenReturn(JWK_THUMBPRINT);
        Mockito.when(mockResult.getJti()).thenReturn(JTI);

        try (MockedStatic<DPoPUtils> utilsStatic = Mockito.mockStatic(DPoPUtils.class)) {
            utilsStatic.when(() -> DPoPUtils.getTransportHeaders(mockSynCtx)).thenReturn(headers);
            utilsStatic.when(() -> DPoPUtils.extractToken(
                    DPOP_SCHEME + " " + ACCESS_TOKEN, DPOP_SCHEME)).thenReturn(ACCESS_TOKEN);
            utilsStatic.when(() -> DPoPUtils.hasMultipleDPopHeaders(mockSynCtx)).thenReturn(false);
            utilsStatic.when(() -> DPoPUtils.normalizeHtu(mockSynCtx, headers)).thenReturn(HTU);

            Mockito.when(mockAxis2MC.getProperty(Constants.Configuration.HTTP_METHOD))
                    .thenReturn(HTTP_METHOD);
            Mockito.when(mockValidator.parseDPoPProof(DPOP_PROOF)).thenReturn(mockParsed);
            Mockito.when(mockBinder.resolveJkt(ACCESS_TOKEN)).thenReturn(JWK_THUMBPRINT);
            Mockito.when(mockValidator.validate(mockParsed, HTTP_METHOD, HTU, ACCESS_TOKEN, null))
                    .thenReturn(mockResult);
            // Replay: isJtiFirstUse returns false
            Mockito.when(mockCache.isJtiFirstUse(JTI, JWK_THUMBPRINT, JTI_TTL)).thenReturn(false);

            boolean result = handler.handleRequest(mockSynCtx);

            assertFalse(result);
            Mockito.verify(mockChallenge).send401(
                    Mockito.eq(mockSynCtx),
                    Mockito.eq(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF),
                    Mockito.any(),
                    Mockito.isNull());
        }
    }

    @Test
    public void multipleDPoPHeadersShouldRejectRequest() throws Exception {
        Map<String, String> headers = makeHeaders(
                AUTH_HEADER, DPOP_SCHEME + " " + ACCESS_TOKEN,
                HEADER_DPOP, DPOP_PROOF);

        DPoPProofValidator.ParsedProof mockParsed = Mockito.mock(DPoPProofValidator.ParsedProof.class);
        Mockito.when(mockParsed.getJwkThumbprint()).thenReturn(JWK_THUMBPRINT);

        try (MockedStatic<DPoPUtils> utilsStatic = Mockito.mockStatic(DPoPUtils.class)) {
            utilsStatic.when(() -> DPoPUtils.getTransportHeaders(mockSynCtx)).thenReturn(headers);
            utilsStatic.when(() -> DPoPUtils.extractToken(
                    DPOP_SCHEME + " " + ACCESS_TOKEN, DPOP_SCHEME)).thenReturn(ACCESS_TOKEN);
            // Multiple DPoP headers
            utilsStatic.when(() -> DPoPUtils.hasMultipleDPopHeaders(mockSynCtx)).thenReturn(true);

            Mockito.when(mockValidator.parseDPoPProof(DPOP_PROOF)).thenReturn(mockParsed);
            Mockito.when(mockBinder.resolveJkt(ACCESS_TOKEN)).thenReturn(JWK_THUMBPRINT);

            boolean result = handler.handleRequest(mockSynCtx);

            assertFalse(result);
            Mockito.verify(mockChallenge).send401(
                    Mockito.eq(mockSynCtx),
                    Mockito.eq(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF),
                    Mockito.any(),
                    Mockito.isNull());
        }
    }

    @Test
    public void handleResponseShouldAlwaysReturnTrue() {
        assertTrue(handler.handleResponse(mockSynCtx));
    }

    @Test
    public void initShouldBuildCollaboratorsUsingDefaultsWhenServiceUnavailable() {
        try (MockedStatic<GatewayDataHolder> gdhStatic = Mockito.mockStatic(GatewayDataHolder.class);
             MockedConstruction<DPoPJtiCache> ignored1 = Mockito.mockConstruction(DPoPJtiCache.class);
             MockedConstruction<DPoPNonceCache> ignored2 = Mockito.mockConstruction(DPoPNonceCache.class)) {

            GatewayDataHolder mockHolder = Mockito.mock(GatewayDataHolder.class);
            Mockito.when(mockHolder.getFinancialServicesConfigurationService()).thenReturn(null);
            gdhStatic.when(GatewayDataHolder::getInstance).thenReturn(mockHolder);

            handler.init(Mockito.mock(SynapseEnvironment.class));
        }
    }

    @Test
    public void destroyShouldCompleteWithoutError() {
        handler.destroy();
    }

    @Test
    public void dpopSchemeWithoutProofHeaderShouldRejectWithInvalidProof() throws Exception {
        Map<String, String> headers = makeHeaders(AUTH_HEADER, DPOP_SCHEME + " " + ACCESS_TOKEN);

        try (MockedStatic<DPoPUtils> utilsStatic = Mockito.mockStatic(DPoPUtils.class)) {
            utilsStatic.when(() -> DPoPUtils.getTransportHeaders(mockSynCtx)).thenReturn(headers);
            utilsStatic.when(() -> DPoPUtils.extractToken(
                    DPOP_SCHEME + " " + ACCESS_TOKEN, DPOP_SCHEME)).thenReturn(ACCESS_TOKEN);
            utilsStatic.when(() -> DPoPUtils.hasMultipleDPopHeaders(mockSynCtx)).thenReturn(false);
            utilsStatic.when(() -> DPoPUtils.normalizeHtu(mockSynCtx, headers)).thenReturn(HTU);

            Mockito.when(mockAxis2MC.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn(HTTP_METHOD);
            Mockito.when(mockBinder.resolveJkt(ACCESS_TOKEN)).thenReturn(null);

            boolean result = handler.handleRequest(mockSynCtx);

            assertFalse(result);
            Mockito.verify(mockChallenge).send401(
                    Mockito.eq(mockSynCtx),
                    Mockito.eq(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF),
                    Mockito.any(), Mockito.isNull());
        }
    }

    @Test
    public void httpsRequiredShouldRejectPlainHttpRequest() throws Exception {
        setField("httpsRequired", true);
        Map<String, String> headers = makeHeaders(
                AUTH_HEADER, DPOP_SCHEME + " " + ACCESS_TOKEN,
                HEADER_DPOP, DPOP_PROOF);

        DPoPProofValidator.ParsedProof mockParsed = Mockito.mock(DPoPProofValidator.ParsedProof.class);
        Mockito.when(mockParsed.getJwkThumbprint()).thenReturn(JWK_THUMBPRINT);

        try (MockedStatic<DPoPUtils> utilsStatic = Mockito.mockStatic(DPoPUtils.class)) {
            utilsStatic.when(() -> DPoPUtils.getTransportHeaders(mockSynCtx)).thenReturn(headers);
            utilsStatic.when(() -> DPoPUtils.extractToken(
                    DPOP_SCHEME + " " + ACCESS_TOKEN, DPOP_SCHEME)).thenReturn(ACCESS_TOKEN);
            utilsStatic.when(() -> DPoPUtils.hasMultipleDPopHeaders(mockSynCtx)).thenReturn(false);
            utilsStatic.when(() -> DPoPUtils.normalizeHtu(mockSynCtx, headers))
                    .thenReturn("http://api.example.com/resource");
            utilsStatic.when(() -> DPoPUtils.checkHttps("http://api.example.com/resource"))
                    .thenThrow(new DPoPProofException(
                            DPoPProofException.ErrorCode.INVALID_DPOP_PROOF, "HTTPS required"));

            Mockito.when(mockAxis2MC.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn(HTTP_METHOD);
            Mockito.when(mockValidator.parseDPoPProof(DPOP_PROOF)).thenReturn(mockParsed);
            Mockito.when(mockBinder.resolveJkt(ACCESS_TOKEN)).thenReturn(null);

            boolean result = handler.handleRequest(mockSynCtx);

            assertFalse(result);
            Mockito.verify(mockChallenge).send401(
                    Mockito.eq(mockSynCtx),
                    Mockito.eq(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF),
                    Mockito.any(), Mockito.isNull());
        }
    }

    @Test
    public void validDPoPRequestShouldStripDPoPHeaderWhenConfigured() throws Exception {
        setField("stripDpopHeader", true);
        Map<String, String> headers = makeHeaders(
                AUTH_HEADER, DPOP_SCHEME + " " + ACCESS_TOKEN,
                HEADER_DPOP, DPOP_PROOF);

        DPoPProofValidator.ParsedProof mockParsed = Mockito.mock(DPoPProofValidator.ParsedProof.class);
        Mockito.when(mockParsed.getJwkThumbprint()).thenReturn(JWK_THUMBPRINT);
        Mockito.when(mockParsed.getKid()).thenReturn(null);

        DPoPProofValidator.ValidationResult mockResult = Mockito.mock(DPoPProofValidator.ValidationResult.class);
        Mockito.when(mockResult.getProofJkt()).thenReturn(JWK_THUMBPRINT);
        Mockito.when(mockResult.getJti()).thenReturn(JTI);

        try (MockedStatic<DPoPUtils> utilsStatic = Mockito.mockStatic(DPoPUtils.class)) {
            utilsStatic.when(() -> DPoPUtils.getTransportHeaders(mockSynCtx)).thenReturn(headers);
            utilsStatic.when(() -> DPoPUtils.extractToken(
                    DPOP_SCHEME + " " + ACCESS_TOKEN, DPOP_SCHEME)).thenReturn(ACCESS_TOKEN);
            utilsStatic.when(() -> DPoPUtils.hasMultipleDPopHeaders(mockSynCtx)).thenReturn(false);
            utilsStatic.when(() -> DPoPUtils.normalizeHtu(mockSynCtx, headers)).thenReturn(HTU);
            utilsStatic.when(() -> DPoPUtils.removeHeader(Mockito.any(), Mockito.any()))
                    .thenAnswer(inv -> null);

            Mockito.when(mockAxis2MC.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn(HTTP_METHOD);
            Mockito.when(mockValidator.parseDPoPProof(DPOP_PROOF)).thenReturn(mockParsed);
            Mockito.when(mockBinder.resolveJkt(ACCESS_TOKEN)).thenReturn(null);
            Mockito.when(mockValidator.validate(mockParsed, HTTP_METHOD, HTU, ACCESS_TOKEN, null))
                    .thenReturn(mockResult);
            Mockito.when(mockCache.isJtiFirstUse(JTI, JWK_THUMBPRINT, JTI_TTL)).thenReturn(true);
            Mockito.doNothing().when(mockBinder).verifyBinding(null, JWK_THUMBPRINT);

            boolean result = handler.handleRequest(mockSynCtx);

            assertTrue(result);
            utilsStatic.verify(() -> DPoPUtils.removeHeader(headers, HEADER_DPOP));
        }
    }

    @Test
    public void validDPoPRequestWithNonceShouldReuseNonceWhenRotationNotNeeded() throws Exception {
        // shouldRotate returns false (default from setUp) — issueNonce must never be called
        Map<String, String> headers = makeHeaders(
                AUTH_HEADER, DPOP_SCHEME + " " + ACCESS_TOKEN,
                HEADER_DPOP, DPOP_PROOF);
        String activeNonce = "server-issued-nonce";

        DPoPProofValidator.ParsedProof mockParsed = Mockito.mock(DPoPProofValidator.ParsedProof.class);
        Mockito.when(mockParsed.getJwkThumbprint()).thenReturn(JWK_THUMBPRINT);
        Mockito.when(mockParsed.getKid()).thenReturn(null);

        DPoPProofValidator.ValidationResult mockResult = Mockito.mock(DPoPProofValidator.ValidationResult.class);
        Mockito.when(mockResult.getProofJkt()).thenReturn(JWK_THUMBPRINT);
        Mockito.when(mockResult.getJti()).thenReturn(JTI);

        try (MockedStatic<DPoPUtils> utilsStatic = Mockito.mockStatic(DPoPUtils.class)) {
            utilsStatic.when(() -> DPoPUtils.getTransportHeaders(mockSynCtx)).thenReturn(headers);
            utilsStatic.when(() -> DPoPUtils.extractToken(
                    DPOP_SCHEME + " " + ACCESS_TOKEN, DPOP_SCHEME)).thenReturn(ACCESS_TOKEN);
            utilsStatic.when(() -> DPoPUtils.hasMultipleDPopHeaders(mockSynCtx)).thenReturn(false);
            utilsStatic.when(() -> DPoPUtils.normalizeHtu(mockSynCtx, headers)).thenReturn(HTU);
            utilsStatic.when(() -> DPoPUtils.removeHeader(Mockito.any(), Mockito.any()))
                    .thenAnswer(inv -> null);

            Mockito.when(mockAxis2MC.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn(HTTP_METHOD);
            Mockito.when(mockValidator.parseDPoPProof(DPOP_PROOF)).thenReturn(mockParsed);
            Mockito.when(mockBinder.resolveJkt(ACCESS_TOKEN)).thenReturn(null);
            Mockito.when(mockNonceStrategy.requiresNonce(JWK_THUMBPRINT)).thenReturn(true);
            Mockito.when(mockCache.getActiveNonce(JWK_THUMBPRINT)).thenReturn(activeNonce);
            Mockito.when(mockValidator.validate(mockParsed, HTTP_METHOD, HTU, ACCESS_TOKEN, activeNonce))
                    .thenReturn(mockResult);
            Mockito.when(mockCache.isJtiFirstUse(JTI, JWK_THUMBPRINT, JTI_TTL)).thenReturn(true);
            Mockito.doNothing().when(mockBinder).verifyBinding(null, JWK_THUMBPRINT);
            // shouldRotate already returns false (setUp default)

            boolean result = handler.handleRequest(mockSynCtx);

            assertTrue(result);
            Mockito.verify(mockCache, Mockito.never()).issueNonce(Mockito.any());
            Mockito.verify(mockSynCtx, Mockito.never())
                    .setProperty(Mockito.eq(DPoPConstants.DPOP_RESPONSE_NONCE_PROPERTY), Mockito.any());
        }
    }

    @Test
    public void validDPoPRequestShouldRotateNonceInResponseWhenStrategyDecides() throws Exception {
        // shouldRotate returns true → issueNonce is called and property is set on synCtx
        Map<String, String> headers = makeHeaders(
                AUTH_HEADER, DPOP_SCHEME + " " + ACCESS_TOKEN,
                HEADER_DPOP, DPOP_PROOF);
        String activeNonce = "server-issued-nonce";
        String rotatedNonce = "rotated-nonce";

        DPoPProofValidator.ParsedProof mockParsed = Mockito.mock(DPoPProofValidator.ParsedProof.class);
        Mockito.when(mockParsed.getJwkThumbprint()).thenReturn(JWK_THUMBPRINT);
        Mockito.when(mockParsed.getKid()).thenReturn(null);

        DPoPProofValidator.ValidationResult mockResult = Mockito.mock(DPoPProofValidator.ValidationResult.class);
        Mockito.when(mockResult.getProofJkt()).thenReturn(JWK_THUMBPRINT);
        Mockito.when(mockResult.getJti()).thenReturn(JTI);

        try (MockedStatic<DPoPUtils> utilsStatic = Mockito.mockStatic(DPoPUtils.class)) {
            utilsStatic.when(() -> DPoPUtils.getTransportHeaders(mockSynCtx)).thenReturn(headers);
            utilsStatic.when(() -> DPoPUtils.extractToken(
                    DPOP_SCHEME + " " + ACCESS_TOKEN, DPOP_SCHEME)).thenReturn(ACCESS_TOKEN);
            utilsStatic.when(() -> DPoPUtils.hasMultipleDPopHeaders(mockSynCtx)).thenReturn(false);
            utilsStatic.when(() -> DPoPUtils.normalizeHtu(mockSynCtx, headers)).thenReturn(HTU);
            utilsStatic.when(() -> DPoPUtils.removeHeader(Mockito.any(), Mockito.any()))
                    .thenAnswer(inv -> null);

            Mockito.when(mockAxis2MC.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn(HTTP_METHOD);
            Mockito.when(mockValidator.parseDPoPProof(DPOP_PROOF)).thenReturn(mockParsed);
            Mockito.when(mockBinder.resolveJkt(ACCESS_TOKEN)).thenReturn(null);
            Mockito.when(mockNonceStrategy.requiresNonce(JWK_THUMBPRINT)).thenReturn(true);
            Mockito.when(mockCache.getActiveNonce(JWK_THUMBPRINT)).thenReturn(activeNonce);
            Mockito.when(mockValidator.validate(mockParsed, HTTP_METHOD, HTU, ACCESS_TOKEN, activeNonce))
                    .thenReturn(mockResult);
            Mockito.when(mockCache.isJtiFirstUse(JTI, JWK_THUMBPRINT, JTI_TTL)).thenReturn(true);
            Mockito.doNothing().when(mockBinder).verifyBinding(null, JWK_THUMBPRINT);
            Mockito.when(mockNonceStrategy.shouldRotate(JWK_THUMBPRINT)).thenReturn(true);
            Mockito.when(mockCache.issueNonce(JWK_THUMBPRINT)).thenReturn(rotatedNonce);

            boolean result = handler.handleRequest(mockSynCtx);

            assertTrue(result);
            Mockito.verify(mockCache).issueNonce(JWK_THUMBPRINT);
            Mockito.verify(mockSynCtx).setProperty(DPoPConstants.DPOP_RESPONSE_NONCE_PROPERTY, rotatedNonce);
        }
    }

    @Test
    public void section11_3ActiveNonceWithoutStrategyRequirementShouldEnforceNonce() throws Exception {
        // RFC 9449 §11.3: once a nonce has been issued the server must require it even
        // if requiresNonce() returns false (e.g. between rotation intervals in a rotating strategy).
        Map<String, String> headers = makeHeaders(
                AUTH_HEADER, DPOP_SCHEME + " " + ACCESS_TOKEN,
                HEADER_DPOP, DPOP_PROOF);
        String activeNonce = "previously-issued-nonce";

        DPoPProofValidator.ParsedProof mockParsed = Mockito.mock(DPoPProofValidator.ParsedProof.class);
        Mockito.when(mockParsed.getJwkThumbprint()).thenReturn(JWK_THUMBPRINT);
        Mockito.when(mockParsed.getKid()).thenReturn(null);

        DPoPProofValidator.ValidationResult mockResult = Mockito.mock(DPoPProofValidator.ValidationResult.class);
        Mockito.when(mockResult.getProofJkt()).thenReturn(JWK_THUMBPRINT);
        Mockito.when(mockResult.getJti()).thenReturn(JTI);

        try (MockedStatic<DPoPUtils> utilsStatic = Mockito.mockStatic(DPoPUtils.class)) {
            utilsStatic.when(() -> DPoPUtils.getTransportHeaders(mockSynCtx)).thenReturn(headers);
            utilsStatic.when(() -> DPoPUtils.extractToken(
                    DPOP_SCHEME + " " + ACCESS_TOKEN, DPOP_SCHEME)).thenReturn(ACCESS_TOKEN);
            utilsStatic.when(() -> DPoPUtils.hasMultipleDPopHeaders(mockSynCtx)).thenReturn(false);
            utilsStatic.when(() -> DPoPUtils.normalizeHtu(mockSynCtx, headers)).thenReturn(HTU);
            utilsStatic.when(() -> DPoPUtils.removeHeader(Mockito.any(), Mockito.any()))
                    .thenAnswer(inv -> null);

            Mockito.when(mockAxis2MC.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn(HTTP_METHOD);
            Mockito.when(mockValidator.parseDPoPProof(DPOP_PROOF)).thenReturn(mockParsed);
            Mockito.when(mockBinder.resolveJkt(ACCESS_TOKEN)).thenReturn(null);
            // requiresNonce returns false (setUp default) but an active nonce exists
            Mockito.when(mockNonceStrategy.requiresNonce(JWK_THUMBPRINT)).thenReturn(false);
            Mockito.when(mockCache.getActiveNonce(JWK_THUMBPRINT)).thenReturn(activeNonce);
            // validate must be called with the active nonce — §11.3 enforcement
            Mockito.when(mockValidator.validate(mockParsed, HTTP_METHOD, HTU, ACCESS_TOKEN, activeNonce))
                    .thenReturn(mockResult);
            Mockito.when(mockCache.isJtiFirstUse(JTI, JWK_THUMBPRINT, JTI_TTL)).thenReturn(true);
            Mockito.doNothing().when(mockBinder).verifyBinding(null, JWK_THUMBPRINT);

            boolean result = handler.handleRequest(mockSynCtx);

            assertTrue(result);
            // The critical assertion: validate was called with the active nonce, not null
            Mockito.verify(mockValidator).validate(mockParsed, HTTP_METHOD, HTU, ACCESS_TOKEN, activeNonce);
        }
    }

    private Map<String, String> makeHeaders(String... keyValues) {
        Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < keyValues.length - 1; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    private void setField(String name, Object value) throws Exception {
        Field field = findField(handler.getClass(), name);
        field.setAccessible(true);
        field.set(handler, value);
    }

    private Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) {
                return findField(clazz.getSuperclass(), name);
            }
            throw e;
        }
    }
}
