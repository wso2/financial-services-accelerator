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

package com.wso2.openbanking.accelerator.gateway.dpop.util;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.wso2.openbanking.accelerator.gateway.dpop.DPoPConstants;
import com.wso2.openbanking.accelerator.gateway.dpop.proof.DPoPProofException;
import org.apache.axis2.context.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Unit tests for {@link DPoPUtils} pure helpers and Synapse-context-dependent methods.
 */
public class DPoPUtilsTest {

    private Axis2MessageContext mockSynCtx;
    private MessageContext mockAxis2MC;

    @BeforeMethod
    public void setUp() {
        mockSynCtx = Mockito.mock(Axis2MessageContext.class);
        mockAxis2MC = Mockito.mock(MessageContext.class);
        Mockito.when(mockSynCtx.getAxis2MessageContext()).thenReturn(mockAxis2MC);
    }

    @Test
    public void parseBoolNullShouldReturnDefault() {
        assertTrue(DPoPUtils.parseBool(null, true));
        assertFalse(DPoPUtils.parseBool(null, false));
    }

    @Test
    public void parseBoolLowercaseTrueShouldReturnTrue() {
        assertTrue(DPoPUtils.parseBool("true", false));
    }

    @Test
    public void parseBoolUppercaseTrueShouldReturnTrue() {
        assertTrue(DPoPUtils.parseBool("TRUE", false));
    }

    @Test
    public void parseBoolFalseShouldReturnFalse() {
        assertFalse(DPoPUtils.parseBool("false", true));
    }

    @Test
    public void parseBoolInvalidStringShouldReturnFalse() {
        assertFalse(DPoPUtils.parseBool("invalid", false));
        assertFalse(DPoPUtils.parseBool("yes", false));
    }

    @Test
    public void parseLongNullShouldReturnDefault() {
        assertEquals(DPoPUtils.parseLong(null, 42L), 42L);
    }

    @Test
    public void parseLongValidNumberShouldReturnParsed() {
        assertEquals(DPoPUtils.parseLong("120", 0L), 120L);
    }

    @Test
    public void parseLongWhitespaceTrimmingNumber() {
        assertEquals(DPoPUtils.parseLong("  300  ", 0L), 300L);
    }

    @Test
    public void parseLongInvalidStringShouldReturnDefault() {
        assertEquals(DPoPUtils.parseLong("abc", 99L), 99L);
    }

    @Test
    public void parseStringNullShouldReturnDefault() {
        assertEquals(DPoPUtils.parseString(null, "default"), "default");
    }

    @Test
    public void parseStringBlankShouldReturnDefault() {
        assertEquals(DPoPUtils.parseString("   ", "default"), "default");
    }

    @Test
    public void parseStringValueShouldBeTrimmed() {
        assertEquals(DPoPUtils.parseString("  value  ", "default"), "value");
    }

    @Test
    public void parseAlgorithmsNullShouldReturnDefaultContainingES256() {
        Set<String> algs = DPoPUtils.parseAlgorithms(null);
        assertNotNull(algs);
        assertTrue(algs.contains("ES256"), "Default algorithm set should contain ES256");
    }

    @Test
    public void parseAlgorithmsBlankShouldReturnDefaultContainingES256() {
        Set<String> algs = DPoPUtils.parseAlgorithms("  ");
        assertTrue(algs.contains("ES256"));
    }

    @Test
    public void parseAlgorithmsCommaSeparatedShouldReturnSetOfTwo() {
        Set<String> algs = DPoPUtils.parseAlgorithms("ES256,PS256");
        assertEquals(algs.size(), 2);
        assertTrue(algs.contains("ES256"));
        assertTrue(algs.contains("PS256"));
    }

    @Test
    public void extractTokenNullHeaderShouldReturnNull() {
        assertNull(DPoPUtils.extractToken(null, "Bearer"));
    }

    @Test
    public void extractTokenBearerHeaderShouldReturnToken() {
        assertEquals(DPoPUtils.extractToken("Bearer abc123", "Bearer"), "abc123");
    }

    @Test
    public void extractTokenDPoPSchemeShouldReturnToken() {
        assertEquals(DPoPUtils.extractToken("DPoP mytoken", "DPoP"), "mytoken");
    }

    @Test
    public void extractTokenCaseInsensitiveSchemeShouldWork() {
        assertEquals(DPoPUtils.extractToken("BEARER mytoken", "Bearer"), "mytoken");
    }

    @Test
    public void extractTokenWrongSchemeShouldReturnNull() {
        assertNull(DPoPUtils.extractToken("Basic dXNlcjpwYXNz", "Bearer"));
    }

    @Test
    public void removeHeaderShouldRemoveExactMatch() {
        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        headers.put("Authorization", "Bearer token");
        DPoPUtils.removeHeader(headers, "Authorization");
        assertFalse(headers.containsKey("Authorization"));
    }

    @Test
    public void removeHeaderShouldRemoveCaseVariants() {
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "Bearer token1");
        headers.put("AUTHORIZATION", "Bearer token2");
        DPoPUtils.removeHeader(headers, "Authorization");
        assertFalse(headers.containsKey("authorization"));
        assertFalse(headers.containsKey("AUTHORIZATION"));
    }

    @Test
    public void isJwtTokenNullShouldReturnFalse() {
        assertFalse(DPoPUtils.isJwtToken(null));
    }

    @Test
    public void isJwtTokenBlankShouldReturnFalse() {
        assertFalse(DPoPUtils.isJwtToken(""));
        assertFalse(DPoPUtils.isJwtToken("   "));
    }

    @Test
    public void isJwtTokenThreePartShouldReturnTrue() {
        assertTrue(DPoPUtils.isJwtToken("a.b.c"));
    }

    @Test
    public void isJwtTokenTwoPartShouldReturnFalse() {
        assertFalse(DPoPUtils.isJwtToken("a.b"));
    }

    @Test
    public void isJwtTokenFourPartShouldReturnFalse() {
        assertFalse(DPoPUtils.isJwtToken("a.b.c.d"));
    }

    @Test
    public void extractJktFromJwtShouldReturnJktWhenPresent() throws Exception {
        ECKey key = new ECKeyGenerator(Curve.P_256).generate();
        String jkt = "expected-jkt-value";

        Map<String, Object> cnf = new HashMap<>();
        cnf.put(DPoPConstants.Claims.JKT_CLAIM, jkt);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .claim(DPoPConstants.Claims.CNF_CLAIM, cnf)
                .subject("client")
                .build();
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256).build();
        SignedJWT jwt = new SignedJWT(header, claims);
        jwt.sign(new ECDSASigner(key));

        String result = DPoPUtils.extractJktFromJwt(jwt.serialize());
        assertEquals(result, jkt);
    }

    @Test
    public void extractJktFromJwtWithoutCnfShouldReturnNull() throws Exception {
        ECKey key = new ECKeyGenerator(Curve.P_256).generate();
        JWTClaimsSet claims = new JWTClaimsSet.Builder().subject("client").build();
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256).build();
        SignedJWT jwt = new SignedJWT(header, claims);
        jwt.sign(new ECDSASigner(key));

        String result = DPoPUtils.extractJktFromJwt(jwt.serialize());
        assertNull(result);
    }

    @Test
    public void extractJktFromJwtNotAJwtShouldThrowDPoPProofException() {
        try {
            DPoPUtils.extractJktFromJwt("not.a.jwt");
            fail("Expected DPoPProofException");
        } catch (DPoPProofException e) {
            assertEquals(e.getErrorCode(), DPoPProofException.ErrorCode.INVALID_TOKEN);
        }
    }

    @Test
    public void checkHttpsForHttpsUrlShouldNotThrow() throws DPoPProofException {
        DPoPUtils.checkHttps("https://api.example.com/resource");
    }

    @Test
    public void checkHttpsForHttpUrlShouldThrow() {
        try {
            DPoPUtils.checkHttps("http://api.example.com/resource");
            fail("Expected DPoPProofException");
        } catch (DPoPProofException e) {
            assertEquals(e.getErrorCode(), DPoPProofException.ErrorCode.INVALID_DPOP_PROOF);
        }
    }

    @Test
    public void checkHttpsForNullShouldNotThrow() throws DPoPProofException {
        DPoPUtils.checkHttps(null);
    }

    @Test
    public void getTransportHeadersShouldReturnCaseInsensitiveMap() {
        Map<String, String> rawHeaders = new HashMap<>();
        rawHeaders.put("Authorization", "Bearer token");
        rawHeaders.put("dpop", "proof-value");
        Mockito.when(mockAxis2MC.getProperty(MessageContext.TRANSPORT_HEADERS)).thenReturn(rawHeaders);

        Map<String, String> result = DPoPUtils.getTransportHeaders(mockSynCtx);

        assertNotNull(result);
        assertEquals(result.get("authorization"), "Bearer token");
        assertEquals(result.get("AUTHORIZATION"), "Bearer token");
        assertEquals(result.get("DPoP"), "proof-value");
    }

    @Test
    public void getTransportHeadersNullPropertyShouldReturnEmptyMap() {
        Mockito.when(mockAxis2MC.getProperty(MessageContext.TRANSPORT_HEADERS)).thenReturn(null);

        Map<String, String> result = DPoPUtils.getTransportHeaders(mockSynCtx);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void normalizeHtuShouldBuildCorrectUri() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Host", "api.example.com");

        Mockito.when(mockSynCtx.getProperty("TRANSPORT_IN_NAME")).thenReturn("https");
        Mockito.when(mockSynCtx.getProperty("REST_FULL_REQUEST_PATH")).thenReturn("/resource");

        String htu = DPoPUtils.normalizeHtu(mockSynCtx, headers);

        assertEquals(htu, "https://api.example.com/resource");
    }

    @Test
    public void hasMultipleDPopHeadersShouldReturnTrueWhenExcessContainsDPoP() {
        Map<String, String> excessHeaders = new HashMap<>();
        excessHeaders.put("DPoP", "second-proof-value");
        Mockito.when(mockAxis2MC.getProperty(NhttpConstants.EXCESS_TRANSPORT_HEADERS))
                .thenReturn(excessHeaders);

        assertTrue(DPoPUtils.hasMultipleDPopHeaders(mockSynCtx));
    }

    @Test
    public void hasMultipleDPopHeadersShouldReturnFalseWhenExcessEmpty() {
        Mockito.when(mockAxis2MC.getProperty(NhttpConstants.EXCESS_TRANSPORT_HEADERS))
                .thenReturn(new HashMap<>());

        assertFalse(DPoPUtils.hasMultipleDPopHeaders(mockSynCtx));
    }
}
