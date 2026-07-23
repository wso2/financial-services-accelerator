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

import org.apache.commons.lang3.StringUtils;
import org.wso2.financial.services.accelerator.common.logging.Log;
import org.wso2.financial.services.accelerator.common.logging.LogFactory;
import org.wso2.financial.services.accelerator.gateway.dpop.proof.DPoPProofException;
import org.wso2.financial.services.accelerator.gateway.dpop.util.DPoPUtils;

/**
 * Resolves {@code cnf.jkt} from the access token and verifies it against the
 * computed JWK thumbprint from the DPoP proof.
 * <ul>
 *   <li>JWT access tokens: {@code cnf.jkt} is read directly from the JWT claims
 *       (no signature verification — that is the downstream OAuth authenticator's job).</li>
 *   <li>Opaque access tokens: {@code cnf.jkt} is obtained via Key Manager introspection;
 *       results are cached by {@link IntrospectionClient} for the configured TTL so
 *       repeated calls for the same token within that window avoid extra network calls.</li>
 * </ul>
 */
public class AccessTokenBinder {

    private static final Log log = LogFactory.getLog(AccessTokenBinder.class);

    private final IntrospectionClient introspectionClient;

    public AccessTokenBinder(IntrospectionClient introspectionClient) {

        this.introspectionClient = introspectionClient;
    }

    /**
     * Resolves {@code cnf.jkt} from the access token regardless of its format.
     * <p>
     * Callers should invoke this once per request and pass the result to
     * {@link #verifyBinding(String, String)}, so that resolution — whether a
     * local JWT parse or a remote introspection call.
     *
     * @param accessToken raw access token string; {@code null} or blank returns {@code null}
     * @return the JWK thumbprint, or {@code null} if the token carries no DPoP binding
     * @throws DPoPProofException if the token is malformed or introspection fails
     */
    public String resolveJkt(String accessToken) throws DPoPProofException {

        if (StringUtils.isBlank(accessToken)) {
            return null;
        }
        final boolean isJwt = DPoPUtils.isJwtToken(accessToken);
        log.debug(() -> "Resolving cnf.jkt from " + (isJwt ? "JWT" : "opaque") + " access token");

        final String jkt = isJwt ? DPoPUtils.extractJktFromJwt(accessToken) : extractJktViaIntrospection(accessToken);
        log.debug(() -> "Resolved cnf.jkt: " + (StringUtils.isNotBlank(jkt) ? jkt : "<none: token is not DPoP-bound>"));

        return jkt;
    }

    /**
     * Verifies that the pre-resolved JWK thumbprint from the access token matches
     * the thumbprint computed from the DPoP proof's public key.
     * <p>
     * Accepts the already-resolved {@code tokenJkt} (from {@link #resolveJkt}) so
     * that JWT parsing and introspection are never repeated for the same request.
     *
     * @param tokenJkt JWK thumbprint resolved from the access token (may be {@code null})
     * @param proofJkt JWK thumbprint computed from the validated DPoP proof
     * @throws DPoPProofException if the token has no DPoP binding or the thumbprints mismatch
     */
    public void verifyBinding(String tokenJkt, String proofJkt) throws DPoPProofException {
        if (StringUtils.isBlank(tokenJkt)) {
            // Per RFC 9449 §7.1: a token presented under the DPoP scheme must have cnf.jkt.
            throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_TOKEN,
                    "Access token presented under DPoP scheme has no cnf.jkt binding");
        }
        if (!tokenJkt.equals(proofJkt)) {

            log.debug(() -> "cnf.jkt mismatch: token=" + tokenJkt + ", proof=" + proofJkt);
            throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_TOKEN,
                    "DPoP proof public key thumbprint does not match the access token's cnf.jkt");
        }
        log.debug(() -> "DPoP token binding verified: cnf.jkt=" + tokenJkt);

    }

    /**
     * Resolves {@code cnf.jkt} for an opaque access token by calling the Key Manager
     * introspection endpoint. Results are cached by {@link IntrospectionClient} for the
     * configured TTL to avoid repeated round-trips for the same token.
     *
     * @param accessToken raw opaque access token
     * @return the {@code cnf.jkt} JWK thumbprint, or {@code null} if the token has no DPoP binding
     * @throws DPoPProofException if the introspection call fails
     */
    private String extractJktViaIntrospection(String accessToken) throws DPoPProofException {
        try {
            return introspectionClient.getJwkThumbprint(accessToken);
        } catch (IntrospectionClient.IntrospectionException e) {
            throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_TOKEN,
                    "Failed to obtain cnf.jkt from token introspection: " + e.getMessage(), e);
        }
    }
}
