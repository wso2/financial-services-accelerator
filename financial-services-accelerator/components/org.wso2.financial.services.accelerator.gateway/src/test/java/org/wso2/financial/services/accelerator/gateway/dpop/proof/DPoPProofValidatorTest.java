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

package org.wso2.financial.services.accelerator.gateway.dpop.proof;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.gateway.dpop.DPoPConstants;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import static org.wso2.financial.services.accelerator.gateway.dpop.DPoPConstants.DPOP_JWT_TYPE;
import static org.wso2.financial.services.accelerator.gateway.dpop.DPoPConstants.SHA256_HASH_ALG;
import static org.wso2.financial.services.accelerator.gateway.util.GatewayConstants.GET_HTTP_METHOD;

/**
 * Unit tests for {@link DPoPProofValidator}. Exercises each RFC 9449 §4.3
 * validation rule independently with both valid and invalid inputs.
 */
public class DPoPProofValidatorTest {

    private static final Set<String> ACCEPTED_ALGS = new HashSet<>(Arrays.asList("ES256", "PS256"));
    private static final long IAT_SKEW_SECONDS = 60L;
    private static final String HTU = "https://api.example.com/resource";

    private DPoPProofValidator validator;
    private ECKey ecKey;
    private String accessToken;
    private String correctAth;

    @BeforeMethod
    public void setUp() throws Exception {
        validator = new DPoPProofValidator(ACCEPTED_ALGS, IAT_SKEW_SECONDS);
        ecKey = new ECKeyGenerator(Curve.P_256).generate();
        accessToken = "test.access.token";
        correctAth = computeAth(accessToken);
    }

    @Test
    public void validProofShouldSucceed() throws Exception {
        String proof = buildProof(ecKey, GET_HTTP_METHOD, HTU,
                new Date(), UUID.randomUUID().toString(), correctAth, null);

        DPoPProofValidator.ParsedProof parsed = validator.parseDPoPProof(proof);
        DPoPProofValidator.ValidationResult result = validator.validate(parsed, GET_HTTP_METHOD,
                HTU, accessToken, null);
        assertNotNull(result.getProofJkt());
        assertNotNull(result.getJti());
    }

    @Test
    public void validProofWithNonceShouldSucceed() throws Exception {
        String nonce = "server-issued-nonce-123";
        String proof = buildProof(ecKey, "POST", "https://api.example.com/data",
                new Date(), UUID.randomUUID().toString(), correctAth, nonce);

        DPoPProofValidator.ParsedProof parsed = validator.parseDPoPProof(proof);
        DPoPProofValidator.ValidationResult result = validator.validate(parsed, "POST",
                "https://api.example.com/data", accessToken, nonce);
        assertNotNull(result.getProofJkt());
    }

    @Test
    public void missingProofShouldFail() {
        assertErrorCode(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                () -> validator.parseDPoPProof(null));
    }

    @Test
    public void wrongTypShouldFail() throws Exception {
        ECKey key = new ECKeyGenerator(Curve.P_256).generate();
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256)
                .type(new JOSEObjectType("JWT"))
                .jwk(key.toPublicJWK())
                .build();
        String proof = signProof(key, header,
                buildBasicClaims(GET_HTTP_METHOD, "https://example.com", new Date(),
                        UUID.randomUUID().toString(), correctAth, null));

        DPoPProofValidator.ParsedProof parsed = validator.parseDPoPProof(proof);
        assertErrorCode(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                () -> validator.validate(parsed, GET_HTTP_METHOD, "https://example.com", accessToken, null));
    }

    @Test
    public void unparseableProofShouldFail() {
        assertErrorCode(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                () -> validator.parseDPoPProof("notajwt"));
    }

    @Test
    public void unknownAlgorithmShouldFail() throws Exception {
        RSAKey rsaKey = new RSAKeyGenerator(2048).generate();
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(new JOSEObjectType(DPOP_JWT_TYPE))
                .jwk(rsaKey.toPublicJWK())
                .build();
        JWTClaimsSet claims = buildBasicClaims(GET_HTTP_METHOD, "https://example.com",
                new Date(), UUID.randomUUID().toString(), correctAth, null);
        SignedJWT jwt = new SignedJWT(header, claims);
        jwt.sign(new RSASSASigner(rsaKey));

        DPoPProofValidator.ParsedProof parsed = validator.parseDPoPProof(jwt.serialize());
        assertErrorCode(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                () -> validator.validate(parsed, GET_HTTP_METHOD, "https://example.com", accessToken, null));
    }

    @Test
    public void wrongHtmShouldFail() throws Exception {
        String proof = buildProof(ecKey, GET_HTTP_METHOD, HTU,
                new Date(), UUID.randomUUID().toString(), correctAth, null);

        DPoPProofValidator.ParsedProof parsed = validator.parseDPoPProof(proof);
        assertErrorCode(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                () -> validator.validate(parsed, "POST", HTU,
                        accessToken, null));
    }

    @Test
    public void wrongHtuShouldFail() throws Exception {
        String proof = buildProof(ecKey, GET_HTTP_METHOD, HTU,
                new Date(), UUID.randomUUID().toString(), correctAth, null);

        DPoPProofValidator.ParsedProof parsed = validator.parseDPoPProof(proof);
        assertErrorCode(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                () -> validator.validate(parsed, GET_HTTP_METHOD, "https://api.example.com/other",
                        accessToken, null));
    }

    @Test
    public void expiredIatShouldFail() throws Exception {
        Date expiredIat = new Date(System.currentTimeMillis() - (IAT_SKEW_SECONDS + 5) * 1000L);
        String proof = buildProof(ecKey, GET_HTTP_METHOD, HTU,
                expiredIat, UUID.randomUUID().toString(), correctAth, null);

        DPoPProofValidator.ParsedProof parsed = validator.parseDPoPProof(proof);
        assertErrorCode(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                () -> validator.validate(parsed, GET_HTTP_METHOD, HTU,
                        accessToken, null));
    }

    @Test
    public void futureIatBeyondSkewShouldFail() throws Exception {
        Date futureIat = new Date(System.currentTimeMillis() + (IAT_SKEW_SECONDS + 5) * 1000L);
        String proof = buildProof(ecKey, GET_HTTP_METHOD, HTU,
                futureIat, UUID.randomUUID().toString(), correctAth, null);

        DPoPProofValidator.ParsedProof parsed = validator.parseDPoPProof(proof);
        assertErrorCode(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                () -> validator.validate(parsed, GET_HTTP_METHOD, HTU,
                        accessToken, null));
    }

    @Test
    public void wrongAthShouldFail() throws Exception {
        String wrongAth = computeAth("different.access.token");
        String proof = buildProof(ecKey, GET_HTTP_METHOD, HTU,
                new Date(), UUID.randomUUID().toString(), wrongAth, null);

        DPoPProofValidator.ParsedProof parsed = validator.parseDPoPProof(proof);
        assertErrorCode(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                () -> validator.validate(parsed, GET_HTTP_METHOD, HTU,
                        accessToken, null));
    }

    @Test
    public void missingAthWhenTokenPresentShouldFail() throws Exception {
        String proof = buildProof(ecKey, GET_HTTP_METHOD, HTU,
                new Date(), UUID.randomUUID().toString(), null, null);

        DPoPProofValidator.ParsedProof parsed = validator.parseDPoPProof(proof);
        assertErrorCode(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                () -> validator.validate(parsed, GET_HTTP_METHOD, HTU,
                        accessToken, null));
    }

    @Test
    public void wrongNonceShouldReturnUseNonceError() throws Exception {
        String proof = buildProof(ecKey, GET_HTTP_METHOD, HTU,
                new Date(), UUID.randomUUID().toString(), correctAth, "wrong-nonce");

        DPoPProofValidator.ParsedProof parsed = validator.parseDPoPProof(proof);
        assertErrorCode(DPoPProofException.ErrorCode.USE_DPOP_NONCE,
                () -> validator.validate(parsed, GET_HTTP_METHOD, HTU,
                        accessToken, "correct-nonce"));
    }

    @Test
    public void missingNonceWhenRequiredShouldReturnUseNonceError() throws Exception {
        String proof = buildProof(ecKey, GET_HTTP_METHOD, HTU,
                new Date(), UUID.randomUUID().toString(), correctAth, null);

        DPoPProofValidator.ParsedProof parsed = validator.parseDPoPProof(proof);
        assertErrorCode(DPoPProofException.ErrorCode.USE_DPOP_NONCE,
                () -> validator.validate(parsed, GET_HTTP_METHOD, HTU,
                        accessToken, "required-nonce"));
    }

    @Test
    public void htuTrailingSlashShouldBeNormalized() throws Exception {
        String proof = buildProof(ecKey, GET_HTTP_METHOD, "https://api.example.com/resource/",
                new Date(), UUID.randomUUID().toString(), correctAth, null);

        DPoPProofValidator.ParsedProof parsed = validator.parseDPoPProof(proof);
        DPoPProofValidator.ValidationResult result = validator.validate(parsed, GET_HTTP_METHOD,
                HTU, accessToken, null);
        assertNotNull(result.getProofJkt());
    }

    @Test
    public void proofWithNullHtuShouldFail() throws Exception {
        // Passing null as htu omits the claim, triggering the null branch in normalizedUriEquals
        String proof = buildProof(ecKey, GET_HTTP_METHOD, null,
                new Date(), UUID.randomUUID().toString(), correctAth, null);

        DPoPProofValidator.ParsedProof parsed = validator.parseDPoPProof(proof);
        assertErrorCode(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                () -> validator.validate(parsed, GET_HTTP_METHOD,
                        HTU, accessToken, null));
    }

    @Test
    public void proofWithMissingJtiShouldFail() throws Exception {
        // Pass null jti so the jti claim is absent from the proof
        String proof = buildProof(ecKey, GET_HTTP_METHOD, HTU,
                new Date(), null, correctAth, null);

        DPoPProofValidator.ParsedProof parsed = validator.parseDPoPProof(proof);
        assertErrorCode(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                () -> validator.validate(parsed, GET_HTTP_METHOD,
                        HTU, accessToken, null));
    }

    @Test
    public void parseDPoPProofShouldPopulateKidWhenJwkHasKid() throws Exception {
        ECKey keyWithKid = new ECKeyGenerator(Curve.P_256).keyID("my-key-id").generate();
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256)
                .type(new JOSEObjectType(DPOP_JWT_TYPE))
                .jwk(keyWithKid.toPublicJWK())
                .build();
        JWTClaimsSet claims = buildBasicClaims(GET_HTTP_METHOD, HTU,
                new Date(), UUID.randomUUID().toString(), correctAth, null);
        SignedJWT jwt = new SignedJWT(header, claims);
        jwt.sign(new ECDSASigner(keyWithKid));

        DPoPProofValidator.ParsedProof parsed = validator.parseDPoPProof(jwt.serialize());
        assertEquals(parsed.getKid(), "my-key-id");
    }

    @Test
    public void parseDPoPProofShouldReturnNullKidWhenJwkHasNoKid() throws Exception {
        // ecKey (from setUp) was generated without a kid
        String proof = buildProof(ecKey, GET_HTTP_METHOD, HTU,
                new Date(), UUID.randomUUID().toString(), correctAth, null);

        DPoPProofValidator.ParsedProof parsed = validator.parseDPoPProof(proof);
        org.testng.Assert.assertNull(parsed.getKid());
    }

    @Test
    public void parsedProofShouldHaveNonNullJwkThumbprint() throws Exception {
        String proof = buildProof(ecKey, GET_HTTP_METHOD, HTU,
                new Date(), UUID.randomUUID().toString(), correctAth, null);

        DPoPProofValidator.ParsedProof parsed = validator.parseDPoPProof(proof);
        assertNotNull(parsed.getJwkThumbprint(),
                "JWK thumbprint must be computed during parseDPoPProof");
    }

    private String buildProof(ECKey key, String htm, String htu, Date iat,
                              String jti, String ath, String nonce) throws Exception {
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256)
                .type(new JOSEObjectType(DPOP_JWT_TYPE))
                .jwk(key.toPublicJWK())
                .build();
        return signProof(key, header, buildBasicClaims(htm, htu, iat, jti, ath, nonce));
    }

    private JWTClaimsSet buildBasicClaims(String htm, String htu, Date iat,
                                          String jti, String ath, String nonce) {
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .jwtID(jti)
                .claim(DPoPConstants.Claims.HTM_CLAIM, htm)
                .claim(DPoPConstants.Claims.HTU_CLAIM, htu)
                .issueTime(iat);
        if (ath != null) {
            builder.claim(DPoPConstants.Claims.ATH_CLAIM, ath);
        }
        if (nonce != null) {
            builder.claim(DPoPConstants.Claims.NONCE_CLAIM, nonce);
        }
        return builder.build();
    }

    private String signProof(ECKey key, JWSHeader header, JWTClaimsSet claims) throws Exception {
        SignedJWT jwt = new SignedJWT(header, claims);
        jwt.sign(new ECDSASigner(key));
        return jwt.serialize();
    }

    private String computeAth(String token) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(SHA256_HASH_ALG);
        byte[] hash = digest.digest(token.getBytes(StandardCharsets.US_ASCII));
        return Base64URL.encode(hash).toString();
    }

    private void assertErrorCode(DPoPProofException.ErrorCode expected, ValidatorCall call) {
        try {
            call.execute();
            fail("Expected DPoPProofException with error code " + expected);
        } catch (DPoPProofException e) {
            assertEquals(e.getErrorCode(), expected);
        } catch (Exception e) {
            fail("Unexpected exception type: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    @FunctionalInterface
    interface ValidatorCall {
        void execute() throws Exception;
    }
}
