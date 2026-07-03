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

package com.wso2.openbanking.accelerator.gateway.dpop.proof;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.Ed25519Verifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.ThumbprintUtils;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.wso2.openbanking.accelerator.gateway.dpop.DPoPConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Date;
import java.util.Set;

import static com.wso2.openbanking.accelerator.gateway.dpop.DPoPConstants.DPOP_JWT_TYPE;
import static com.wso2.openbanking.accelerator.gateway.dpop.DPoPConstants.SHA256_HASH_ALG;
import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;


/**
 * Pure JOSE proof validation per RFC 9449 §4.2 and §4.3. Has no Synapse or APIM
 * runtime dependencies; safe to unit-test in isolation.
 * <p>
 * Usage: construct once with the desired algorithm allow-list and clock skew,
 * then call {@link #validate} once per request.
 */
public class DPoPProofValidator {

    private static final Log log = LogFactory.getLog(DPoPProofValidator.class);

    private final Set<String> acceptedAlgorithms;
    private final long iatSkewMillis;

    public DPoPProofValidator(Set<String> acceptedAlgorithms, long iatSkewSeconds) {

        this.acceptedAlgorithms = acceptedAlgorithms;
        this.iatSkewMillis = iatSkewSeconds * 1000L;
    }

    /**
     * Lightweight result of the initial proof JWT parse. Holds the data needed before
     * full validation — the JWK thumbprint for nonce-cache lookup and the {@code kid}
     * for logging. Pass to {@link #validate} to complete the cryptographic checks.
     */
    public static final class ParsedProof {

        private final SignedJWT signedJWT;
        private final String kid;
        private final String jwkThumbprint;

        private ParsedProof(SignedJWT signedJWT, String kid, String jwkThumbprint) {

            this.signedJWT = signedJWT;
            this.kid = kid;
            this.jwkThumbprint = jwkThumbprint;
        }

        /**
         * The {@code kid} from the embedded JWK in the proof header, or {@code null} if absent.
         */
        public String getKid() {

            return kid;
        }

        /**
         * SHA-256 JWK thumbprint computed from the proof header's {@code jwk} claim before full
         * validation. Use this as the nonce cache key — it is derived from the actual public key
         * material and is independent of the client-controlled {@code kid} value. Returns
         * {@code null} if the {@code jwk} claim is absent (header validation will reject the proof
         * when {@link DPoPProofValidator#validate} is called).
         */
        public String getJwkThumbprint() {

            return jwkThumbprint;
        }
    }

    /**
     * The verified outputs of a successful DPoP proof validation.
     */
    public static final class ValidationResult {

        private final String proofJkt;
        private final String jti;

        private ValidationResult(String proofJkt, String jti) {

            this.proofJkt = proofJkt;
            this.jti = jti;
        }

        /**
         * Base64url SHA-256 JWK thumbprint of the proof's public key (from the validated header).
         */
        public String getProofJkt() {

            return proofJkt;
        }

        /**
         * The {@code jti} (JWT ID) from the proof claims — use for replay detection.
         */
        public String getJti() {

            return jti;
        }
    }

    /**
     * Parses the raw DPoP proof string and extracts the fields needed before full validation:
     * the JWK thumbprint (preferred nonce cache key, derived from key material) and {@code kid}.
     * Pass the returned {@link ParsedProof} to {@link #validate} to complete
     * the cryptographic checks.
     *
     * @param proofJwt raw value of the {@code DPoP} HTTP header
     * @return parsed proof ready for full validation
     * @throws DPoPProofException if the JWT is blank or not parseable
     */
    public ParsedProof parseDPoPProof(String proofJwt) throws DPoPProofException {

        if (StringUtils.isBlank(proofJwt)) {
            throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                    "DPoP proof header is missing");
        }
        SignedJWT signedJWT;
        try {
            signedJWT = SignedJWT.parse(proofJwt);
        } catch (ParseException e) {
            throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                    "DPoP proof JWT is not parseable: " + e.getMessage(), e);
        }
        JWK jwk = signedJWT.getHeader().getJWK();
        String kid = null;
        String jwkThumbprint = null;
        if (jwk != null) {
            kid = jwk.getKeyID();
            try {
                jwkThumbprint = ThumbprintUtils.compute(SHA256_HASH_ALG, jwk).toString();
            } catch (JOSEException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Failed to compute preliminary JWK thumbprint in parseDPoPProof; "
                            + "header validation will reject this proof shortly", e);
                }
            }
        }
        return new ParsedProof(signedJWT, kid, jwkThumbprint);
    }

    /**
     * Validates a pre-parsed DPoP proof JWT against RFC 9449 §4.3 rules.
     * Accepts the result of {@link #parseDPoPProof} so the JWT string is parsed exactly once.
     *
     * @param parsedProof   result of {@link #parseDPoPProof}
     * @param requestHtm    HTTP method of the request
     * @param requestHtu    normalized HTTP URI of the request (no query/fragment)
     * @param accessToken   raw access token (for {@code ath} verification), or {@code null}
     * @param expectedNonce server nonce to match, or {@code null} if nonces are not required
     * @return validation result containing the JWK thumbprint and {@code jti}
     * @throws DPoPProofException if any validation step fails
     */
    public ValidationResult validate(ParsedProof parsedProof, String requestHtm, String requestHtu,
                                     String accessToken, String expectedNonce) throws DPoPProofException {

        JWSHeader header = parsedProof.signedJWT.getHeader();
        JWK jwk = validateHeader(header);

        verifySignature(parsedProof.signedJWT, jwk);

        JWTClaimsSet claims = parseClaims(parsedProof.signedJWT);
        validateClaims(claims, requestHtm, requestHtu, accessToken, expectedNonce);

        return new ValidationResult(computeJwkThumbprint(jwk), claims.getJWTID());
    }

    /**
     * Validates the DPoP proof JOSE header per RFC 9449 §4.3 rules 4–7:
     * {@code typ} must be {@code dpop+jwt}, {@code alg} must be asymmetric and in the
     * accepted list, {@code jwk} must be present and must not contain private key material.
     *
     * @param header the JWS header of the parsed proof JWT
     * @return the public JWK embedded in the header
     * @throws DPoPProofException if any header constraint is violated
     */
    private JWK validateHeader(JWSHeader header) throws DPoPProofException {
        // RFC 9449 §4.3 rule 4: typ must be "dpop+jwt"
        if (header.getType() == null || !equalsIgnoreCase(DPOP_JWT_TYPE, (header.getType().getType()))) {
            throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                    "DPoP proof typ must be \"dpop+jwt\", got: " + header.getType());
        }

        // RFC 9449 §4.3 rule 5: alg must be asymmetric and in the allow-list
        JWSAlgorithm alg = header.getAlgorithm();
        if (alg == null || JWSAlgorithm.Family.HMAC_SHA.contains(alg) || JWSAlgorithm.NONE.equals(alg)) {
            throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                    "DPoP proof alg must be an asymmetric algorithm, got: " + alg);
        }
        if (!acceptedAlgorithms.contains(alg.getName())) {
            throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                    "DPoP proof alg \"" + alg.getName() + "\" is not in the accepted algorithm list");
        }

        // RFC 9449 §4.3 rule 6: jwk must be present
        JWK jwk = header.getJWK();
        if (jwk == null) {
            throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                    "DPoP proof header must contain a jwk claim");
        }
        // RFC 9449 §4.3 rule 7: jwk must not contain private key material
        if (jwk.isPrivate()) {
            throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                    "DPoP proof jwk must not contain private key material");
        }
        return jwk;
    }

    /**
     * Verifies the DPoP proof signature against the public key embedded in its header.
     *
     * @param signedJWT the parsed proof JWT
     * @param jwk       the public key extracted from the proof header
     * @throws DPoPProofException if signature verification fails or the JWK type is unsupported
     */
    private void verifySignature(SignedJWT signedJWT, JWK jwk) throws DPoPProofException {
        try {
            JWSVerifier verifier = buildVerifier(jwk);
            if (!signedJWT.verify(verifier)) {
                throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                        "DPoP proof signature verification failed");
            }
        } catch (JOSEException e) {
            throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                    "DPoP proof signature verification error: " + e.getMessage(), e);
        }
    }

    /**
     * Constructs a {@link JWSVerifier} for the given JWK key type (RSA, EC, or OKP/Ed25519).
     *
     * @param jwk the public key from the proof header
     * @return a verifier suitable for the key type
     * @throws JOSEException      if the key material cannot be parsed
     * @throws DPoPProofException if the key type or OKP curve is unsupported
     */
    private JWSVerifier buildVerifier(JWK jwk) throws JOSEException, DPoPProofException {
        KeyType keyType = jwk.getKeyType();
        if (KeyType.RSA.equals(keyType)) {
            return new RSASSAVerifier(((RSAKey) jwk).toRSAPublicKey());
        } else if (KeyType.EC.equals(keyType)) {
            return new ECDSAVerifier(((ECKey) jwk).toECPublicKey());
        } else if (KeyType.OKP.equals(keyType)) {
            OctetKeyPair okp = (OctetKeyPair) jwk;
            if (!Curve.Ed25519.equals(okp.getCurve())) {
                throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                        "Unsupported OKP curve: " + okp.getCurve());
            }
            return new Ed25519Verifier(okp.toPublicJWK());
        }
        throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                "Unsupported JWK key type: " + keyType);
    }

    /**
     * Extracts the JWT claims set from the signed JWT.
     *
     * @param signedJWT the parsed and signature-verified proof JWT
     * @return the claims set
     * @throws DPoPProofException if the claims payload cannot be parsed
     */
    private JWTClaimsSet parseClaims(SignedJWT signedJWT) throws DPoPProofException {

        try {
            return signedJWT.getJWTClaimsSet();
        } catch (ParseException e) {
            throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                    "Failed to parse DPoP proof claims: " + e.getMessage(), e);
        }
    }

    /**
     * Validates proof claims per RFC 9449 §4.3 rules 8–12:
     * {@code htm} must match the request method, {@code htu} must match the normalized
     * request URI, {@code iat} must be within the skew window, {@code ath} must equal
     * the access token hash when a token is present, and {@code nonce} must match the
     * server-issued nonce when one is required.
     *
     * @param claims        the claims set from the proof JWT
     * @param requestHtm    HTTP method of the current request
     * @param requestHtu    normalized request URI (no query or fragment)
     * @param accessToken   raw access token for {@code ath} verification, or {@code null}
     * @param expectedNonce server-issued nonce to match, or {@code null} if not required
     * @throws DPoPProofException if any claim constraint is violated
     */
    private void validateClaims(JWTClaimsSet claims, String requestHtm, String requestHtu,
                                String accessToken, String expectedNonce) throws DPoPProofException {

        if (StringUtils.isBlank(claims.getJWTID())) {
            throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                    "DPoP proof must contain a jti claim");
        }

        // RFC 9449 §4.3 rule 8: htm must match request method
        String htm = (String) claims.getClaim(DPoPConstants.Claims.HTM_CLAIM);
        if (!equalsIgnoreCase(requestHtm, htm)) {
            throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                    "DPoP proof htm \"" + htm + "\" does not match request method \"" + requestHtm + "\"");
        }

        // RFC 9449 §4.3 rule 9: htu must match normalized request URI
        String htu = removeQueryAndFragment((String) claims.getClaim(DPoPConstants.Claims.HTU_CLAIM));
        if (!normalizedUriEquals(requestHtu, htu)) {
            throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                    "DPoP proof htu \"" + htu + "\" does not match request URI \"" + requestHtu + "\"");
        }

        // RFC 9449 §4.3 rule 11: iat must be within the acceptance window
        Date iat = claims.getIssueTime();
        if (iat == null) {
            throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                    "DPoP proof must contain an iat claim");
        }
        long now = System.currentTimeMillis();
        if (Math.abs(now - iat.getTime()) > iatSkewMillis) {
            throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                    "DPoP proof iat is outside the acceptance window");
        }

        if (accessToken != null) {
            // RFC 9449 §4.3 rule 12: ath must equal base64url(SHA-256(ASCII(accessToken)))
            String ath = (String) claims.getClaim(DPoPConstants.Claims.ATH_CLAIM);
            if (StringUtils.isBlank(ath)) {
                throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                        "DPoP proof must contain an ath claim when an access token is presented");
            }
            String expectedAth = computeAth(accessToken);
            if (!expectedAth.equals(ath)) {
                throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                        "DPoP proof ath does not match the access token hash");
            }
        }

        // RFC 9449 §4.3 rule 10: nonce claim must match the server-issued nonce when required
        if (expectedNonce != null) {
            String nonce = (String) claims.getClaim(DPoPConstants.Claims.NONCE_CLAIM);
            if (!expectedNonce.equals(nonce)) {
                throw new DPoPProofException(DPoPProofException.ErrorCode.USE_DPOP_NONCE,
                        "DPoP proof nonce is missing or does not match the server-issued nonce");
            }
        }
    }

    /**
     * Computes the base64url-encoded SHA-256 JWK thumbprint per RFC 7638.
     */
    private String computeJwkThumbprint(JWK jwk) throws DPoPProofException {

        try {
            Base64URL thumbprint = ThumbprintUtils.compute(SHA256_HASH_ALG, jwk);
            return thumbprint.toString();
        } catch (JOSEException e) {
            throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                    "Failed to compute JWK thumbprint: " + e.getMessage(), e);
        }
    }

    /**
     * Computes the {@code ath} claim value as {@code base64url(SHA-256(ASCII(accessToken)))}
     * per RFC 9449 §4.2.
     *
     * @param accessToken raw access token string
     * @return base64url-encoded SHA-256 hash of the ASCII-encoded token
     * @throws DPoPProofException if the SHA-256 algorithm is unavailable on this JVM
     */
    private String computeAth(String accessToken) throws DPoPProofException {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA256_HASH_ALG);
            byte[] hash = digest.digest(accessToken.getBytes(StandardCharsets.US_ASCII));
            return Base64URL.encode(hash).toString();
        } catch (NoSuchAlgorithmException e) {
            throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                    "SHA-256 not available", e);
        }
    }

    /**
     * Returns {@code true} if {@code a} and {@code b} are equal after stripping trailing
     * slashes and ignoring case, following the URI normalization requirements of RFC 9449 §4.3.
     *
     * @param a first URI string, may be {@code null}
     * @param b second URI string, may be {@code null}
     * @return {@code true} if the normalized URIs are equal; {@code false} if either is {@code null}
     */
    private boolean normalizedUriEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return equalsIgnoreCase(stripTrailingSlash(a), stripTrailingSlash(b));
    }

    /**
     * Removes a trailing {@code /} from {@code uri} if present.
     *
     * @param uri URI string to normalize
     * @return the URI without a trailing slash
     */
    private String stripTrailingSlash(String uri) {
        return uri.endsWith("/") ? uri.substring(0, uri.length() - 1) : uri;
    }

    /**
     * Strips the query string and fragment from {@code url}, returning only
     * {@code scheme://authority/path} for htu claim comparison per RFC 9449 §4.3 rule 9.
     *
     * @param url the raw htu claim value from the proof
     * @return normalized URI without query or fragment, or {@code null} if {@code url} is blank
     * @throws DPoPProofException if {@code url} has invalid URI syntax
     */
    private String removeQueryAndFragment(String url) throws DPoPProofException {
        try {
            if (StringUtils.isBlank(url)) {
                return null;
            }
            final URI uri = new URI(url);
            return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, null).toString();
        } catch (URISyntaxException e) {
            throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                    "Invalid URI syntax in the htu claim", e);
        }
    }
}
