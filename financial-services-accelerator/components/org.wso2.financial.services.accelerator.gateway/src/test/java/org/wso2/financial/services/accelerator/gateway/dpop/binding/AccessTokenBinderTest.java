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

package org.wso2.financial.services.accelerator.gateway.dpop.binding;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.ThumbprintUtils;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.gateway.dpop.DPoPConstants;
import org.wso2.financial.services.accelerator.gateway.dpop.proof.DPoPProofException;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import static org.wso2.financial.services.accelerator.gateway.dpop.DPoPConstants.SHA256_HASH_ALG;

/**
 * Unit tests for {@link AccessTokenBinder}. Covers JWT and opaque-token paths
 * and the mismatch / missing-binding error cases.
 */
public class AccessTokenBinderTest {

    private ECKey ecKey;
    private String correctJkt;

    @BeforeMethod
    public void setUp() throws Exception {
        ecKey = new ECKeyGenerator(Curve.P_256).generate();
        correctJkt = ThumbprintUtils.compute(SHA256_HASH_ALG, ecKey.toPublicJWK()).toString();
    }

    @Test
    public void jwtWithMatchingJktShouldPass() throws Exception {
        String jwtToken = buildJwtWithCnf(ecKey, correctJkt);
        AccessTokenBinder binder = new AccessTokenBinder(mockIntrospection(null));

        String tokenJkt = binder.resolveJkt(jwtToken);
        binder.verifyBinding(tokenJkt, correctJkt);
    }

    @Test
    public void jwtWithMismatchedJktShouldFail() throws Exception {
        ECKey otherKey = new ECKeyGenerator(Curve.P_256).generate();
        String otherJkt = ThumbprintUtils.compute(SHA256_HASH_ALG, otherKey.toPublicJWK()).toString();
        String jwtToken = buildJwtWithCnf(ecKey, otherJkt);
        AccessTokenBinder binder = new AccessTokenBinder(mockIntrospection(null));

        String tokenJkt = binder.resolveJkt(jwtToken);
        assertErrorCode(DPoPProofException.ErrorCode.INVALID_TOKEN, () -> binder.verifyBinding(tokenJkt, correctJkt));
    }

    @Test
    public void jwtWithoutCnfClaimShouldFail() throws Exception {
        String jwtToken = buildJwtWithoutCnf();
        AccessTokenBinder binder = new AccessTokenBinder(mockIntrospection(null));

        String tokenJkt = binder.resolveJkt(jwtToken);
        assertErrorCode(DPoPProofException.ErrorCode.INVALID_TOKEN,
                () -> binder.verifyBinding(tokenJkt, correctJkt));
    }

    @Test
    public void opaqueTokenWithMatchingJktShouldPass() throws Exception {
        String opaqueToken = "opaque_token_no_dots";
        AccessTokenBinder binder = new AccessTokenBinder(mockIntrospection(correctJkt));

        String tokenJkt = binder.resolveJkt(opaqueToken);
        binder.verifyBinding(tokenJkt, correctJkt);
    }

    @Test
    public void opaqueTokenWithMismatchedJktShouldFail() throws Exception {
        String opaqueToken = "opaque_token_no_dots";
        ECKey otherKey = new ECKeyGenerator(Curve.P_256).generate();
        String otherJkt = ThumbprintUtils.compute(SHA256_HASH_ALG, otherKey.toPublicJWK()).toString();
        AccessTokenBinder binder = new AccessTokenBinder(mockIntrospection(otherJkt));

        String tokenJkt = binder.resolveJkt(opaqueToken);
        assertErrorCode(DPoPProofException.ErrorCode.INVALID_TOKEN,
                () -> binder.verifyBinding(tokenJkt, correctJkt));
    }

    @Test
    public void opaqueTokenWithNoJktShouldFail() throws Exception {
        String opaqueToken = "opaque_token_no_dots";
        AccessTokenBinder binder = new AccessTokenBinder(mockIntrospection(null));

        String tokenJkt = binder.resolveJkt(opaqueToken);
        assertErrorCode(DPoPProofException.ErrorCode.INVALID_TOKEN,
                () -> binder.verifyBinding(tokenJkt, correctJkt));
    }

    private String buildJwtWithCnf(ECKey key, String jkt) throws Exception {
        Map<String, Object> cnf = new HashMap<>();
        cnf.put(DPoPConstants.Claims.JKT_CLAIM, jkt);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .claim(DPoPConstants.Claims.CNF_CLAIM, cnf)
                .subject("test-client")
                .build();
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256).build();
        SignedJWT jwt = new SignedJWT(header, claims);
        jwt.sign(new ECDSASigner(key));
        return jwt.serialize();
    }

    private String buildJwtWithoutCnf() throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder().subject("test-client").build();
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256).build();
        SignedJWT jwt = new SignedJWT(header, claims);
        jwt.sign(new ECDSASigner(ecKey));
        return jwt.serialize();
    }

    private IntrospectionClient mockIntrospection(String jkt) throws Exception {
        IntrospectionClient mock = Mockito.mock(IntrospectionClient.class);
        Mockito.when(mock.getJwkThumbprint(Mockito.anyString())).thenReturn(jkt);
        return mock;
    }

    private void assertErrorCode(DPoPProofException.ErrorCode expected, ThrowingRunnable call) {
        try {
            call.run();
            fail("Expected DPoPProofException with error code " + expected);
        } catch (DPoPProofException e) {
            assertEquals(e.getErrorCode(), expected);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }
}
