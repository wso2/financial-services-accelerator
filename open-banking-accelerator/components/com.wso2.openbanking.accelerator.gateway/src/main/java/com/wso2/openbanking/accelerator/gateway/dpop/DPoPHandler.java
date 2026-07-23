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

package com.wso2.openbanking.accelerator.gateway.dpop;

import com.wso2.openbanking.accelerator.common.util.OpenBankingUtils;
import com.wso2.openbanking.accelerator.gateway.dpop.binding.AccessTokenBinder;
import com.wso2.openbanking.accelerator.gateway.dpop.binding.IntrospectionClient;
import com.wso2.openbanking.accelerator.gateway.dpop.cache.DPoPCacheProvider;
import com.wso2.openbanking.accelerator.gateway.dpop.cache.LocalDPoPCacheProvider;
import com.wso2.openbanking.accelerator.gateway.dpop.nonce.NonceStrategy;
import com.wso2.openbanking.accelerator.gateway.dpop.nonce.NonceStrategyType;
import com.wso2.openbanking.accelerator.gateway.dpop.proof.DPoPProofException;
import com.wso2.openbanking.accelerator.gateway.dpop.proof.DPoPProofValidator;
import com.wso2.openbanking.accelerator.gateway.dpop.util.Challenge;
import com.wso2.openbanking.accelerator.gateway.dpop.util.DPoPUtils;
import com.wso2.openbanking.accelerator.gateway.internal.GatewayDataHolder;
import org.apache.axis2.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.wso2.openbanking.accelerator.gateway.dpop.DPoPConstants.BEARER_SCHEME_LEN;
import static com.wso2.openbanking.accelerator.gateway.dpop.DPoPConstants.DPOP_BOUND_PROPERTY;
import static com.wso2.openbanking.accelerator.gateway.dpop.DPoPConstants.DPOP_HEADER;
import static com.wso2.openbanking.accelerator.gateway.dpop.DPoPConstants.DPOP_SCHEME;
import static com.wso2.openbanking.accelerator.gateway.dpop.DPoPConstants.DPOP_SCHEME_LEN;
import static com.wso2.openbanking.accelerator.gateway.dpop.util.DPoPUtils.parseBool;
import static com.wso2.openbanking.accelerator.gateway.util.GatewayConstants.AUTH_HEADER;
import static com.wso2.openbanking.accelerator.gateway.util.GatewayConstants.BEARER_TAG;


/**
 * Custom Synapse handler that validates DPoP-bound access tokens per RFC 9449.
 * <p>
 * Must be positioned BEFORE the built-in {@code APIAuthenticationHandler} in
 * {@code velocity_template.xml}. All tuning is read from {@code open-banking.xml}
 * under {@code <Gateway><DPoP>...</DPoP></Gateway>}. DPoP enforcement is opt-in per API:
 * set {@code apiDPoPEnabled=true} on each handler element that requires it.
 *
 * <pre>
 * &lt;handler class="org.wso2.financial.services.accelerator.gateway.dpop.DPoPHandler"&gt;
 *     &lt;property name="apiDPoPEnabled" value="true"/&gt;
 * &lt;/handler&gt;
 * </pre>
 */
public class DPoPHandler extends AbstractHandler implements ManagedLifecycle {

    private static final Log log = LogFactory.getLog(DPoPHandler.class);

    // DPoP enforcement is opt-in per API; set value="true" on the handler element to enable.
    private boolean apiDPoPEnabled = false;

    // Resolved at init() from open-banking.xml
    private long iatSkewSeconds;
    private long jtiCacheTtlSeconds;
    private boolean stripDpopHeader;
    private boolean httpsRequired;

    // Collaborators built once at init()
    private DPoPProofValidator proofValidator;
    private NonceStrategy nonceStrategy;
    private AccessTokenBinder accessTokenBinder;
    private Challenge challenge;
    private DPoPCacheProvider cacheProvider;

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {

        Map<String, Object> cfg = this.readDPopConfig();

        this.iatSkewSeconds = DPoPUtils.parseLong(cfg.get(DPoPConstants.ConfigKeys.IAT_SKEW_SECONDS),
                DPoPConstants.Defaults.IAT_SKEW_SECONDS);
        this.jtiCacheTtlSeconds = DPoPUtils.parseLong(cfg.get(DPoPConstants.ConfigKeys.JTI_CACHE_TTL_SECONDS),
                DPoPConstants.Defaults.JTI_CACHE_TTL_SECONDS);
        this.stripDpopHeader = parseBool(cfg.get(DPoPConstants.ConfigKeys.STRIP_DPOP_HEADER),
                DPoPConstants.Defaults.STRIP_DPOP_HEADER);
        this.httpsRequired = parseBool(cfg.get(DPoPConstants.ConfigKeys.HTTPS_REQUIRED),
                DPoPConstants.Defaults.HTTPS_REQUIRED);

        Set<String> algorithms =
                DPoPUtils.parseAlgorithms((String) cfg.get(DPoPConstants.ConfigKeys.ACCEPTED_ALGORITHMS));
        String nonceStrategyName = DPoPUtils.parseString(cfg.get(DPoPConstants.ConfigKeys.NONCE_STRATEGY),
                DPoPConstants.Defaults.NONCE_STRATEGY);
        int nonceRotateAfterUses = (int) DPoPUtils.parseLong(cfg.get(DPoPConstants.ConfigKeys.NONCE_ROTATE_AFTER_USES),
                DPoPConstants.Defaults.NONCE_ROTATE_AFTER_USES);
        int nonceMaxTrackedClients = (int) DPoPUtils.parseLong(
                cfg.get(DPoPConstants.ConfigKeys.NONCE_MAX_TRACKED_CLIENTS),
                DPoPConstants.Defaults.NONCE_MAX_TRACKED_CLIENTS);
        long introspectionTtl = DPoPUtils.parseLong(cfg.get(DPoPConstants.ConfigKeys.INTROSPECTION_CACHE_TTL_SECONDS),
                DPoPConstants.Defaults.INTROSPECTION_CACHE_TTL_SECONDS);

        this.proofValidator = new DPoPProofValidator(algorithms, iatSkewSeconds);
        this.nonceStrategy = this.buildNonceStrategy(nonceStrategyName, nonceRotateAfterUses, nonceMaxTrackedClients);
        this.accessTokenBinder = new AccessTokenBinder(new IntrospectionClient(introspectionTtl));
        this.challenge = new Challenge(String.join(" ", algorithms));

        this.cacheProvider = buildCacheProvider(cfg);

        log.info("DPoP handler initialized. algorithms=" + algorithms
                + ", nonceStrategy=" + nonceStrategy.getName()
                + ", cacheProvider=" + cacheProvider.getClass().getName());
    }

    @Override
    public void destroy() {
        log.info("DPoP handler destroyed.");
    }

    @Override
    public boolean handleRequest(MessageContext synCtx) {

        if (log.isDebugEnabled()) {
            log.debug("Handling request in DPoPHandler. apiDPoPEnabled=" + apiDPoPEnabled);
        }
        if (!apiDPoPEnabled) {
            return true;
        }

        final Map<String, String> headers = DPoPUtils.getTransportHeaders(synCtx);
        if (headers.isEmpty()) {
            log.debug("No transport headers found; skipping DPoP validation");
            return true;
        }

        final String authHeader = headers.get(AUTH_HEADER);
        final String dPoPHeader = headers.get(DPOP_HEADER);

        // Declared before try so the catch block can use it without re-parsing the proof JWT.
        DPoPProofValidator.ParsedProof parsedProof = null;

        try {
            final boolean isDPoPScheme = authHeader != null
                    && authHeader.regionMatches(true, 0, DPOP_SCHEME + " ", 0, DPOP_SCHEME_LEN);
            final boolean isBearerWithDPoP = dPoPHeader != null && authHeader != null
                    && authHeader.regionMatches(true, 0, BEARER_TAG, 0, BEARER_SCHEME_LEN);
            String accessToken = DPoPUtils.extractToken(authHeader, isDPoPScheme ? DPOP_SCHEME : BEARER_TAG.trim());

            if (log.isDebugEnabled()) {
                log.debug("DPoP handler: scheme=" + (isDPoPScheme ? "DPoP" : "Bearer")
                        + ", hasDPoPProof=" + (dPoPHeader != null));
            }

            // Parse the proof JWT — kid and JWK thumbprint extracted here are reused
            // throughout: nonce-cache lookup, full validation, and replay check.
            if (StringUtils.isNotBlank(dPoPHeader)) {
                parsedProof = proofValidator.parseDPoPProof(dPoPHeader);
            }

            // Resolve cnf.jkt — reused for both the early-rejection check below and
            // the binding verification inside validateDPoP.
            final String tokenJkt = accessTokenBinder.resolveJkt(accessToken);

            // RFC 9449 §7.1: a DPoP-bound token presented without a proof must be rejected.
            // For JWT tokens this is detected from claims; for opaque tokens from introspection.
            if (parsedProof == null && StringUtils.isNotEmpty(tokenJkt)) {

                if (log.isDebugEnabled()) {
                    log.debug("Rejecting DPoP-bound token presented without a proof: cnf.jkt=" + tokenJkt);
                }
                throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_TOKEN,
                        "DPoP proof required for DPoP-bound access token; retry with the proof");
            }

            if (!isDPoPScheme && !isBearerWithDPoP) {
                // No DPoP involvement — downstream APIAuthenticationHandler handles the Bearer case
                log.debug("No DPoP involvement detected; passing request to downstream handler");
                return true;
            }

            // RFC 9449 §4.3: exactly one DPoP header field is required
            if (DPoPUtils.hasMultipleDPopHeaders(synCtx)) {
                throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                        "Multiple DPoP headers found");
            }

            return validateDPoP(synCtx, headers, accessToken, parsedProof, tokenJkt);
        } catch (DPoPProofException e) {

            log.error("DPoP validation failed. Caused by,", e);
            // Use the JWK thumbprint from the parsed DPoP proof as the nonce key.
            final String proofKeyId = parsedProof != null ? parsedProof.getJwkThumbprint() : null;
            final String nonce = (e.getErrorCode() == DPoPProofException.ErrorCode.USE_DPOP_NONCE && proofKeyId != null)
                    ? cacheProvider.issueNonce(proofKeyId)
                    : null;
            challenge.send401(synCtx, e.getErrorCode(), e.getMessage(), nonce);
            return false;
        }
    }

    @Override
    public boolean handleResponse(MessageContext synCtx) {

        final boolean isDPoPBound = parseBool(synCtx.getProperty(DPOP_BOUND_PROPERTY), false);
        final String rotatedNonce = (String) synCtx.getProperty(DPoPConstants.DPOP_RESPONSE_NONCE_PROPERTY);
        if (isDPoPBound && rotatedNonce != null) {
            Map<String, String> headers = DPoPUtils.getTransportHeaders(synCtx);
            headers.put(DPoPConstants.DPOP_NONCE_HEADER, rotatedNonce);
            // RFC 9449 §8.2: responses that carry a nonce must not be cached.
            headers.put(HttpHeaders.CACHE_CONTROL, DPoPConstants.CACHE_CONTROL_NO_STORE);
            ((Axis2MessageContext) synCtx).getAxis2MessageContext()
                    .setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
        }
        return true;
    }

    private boolean validateDPoP(MessageContext synCtx, Map<String, String> headers,
                                 String accessToken, DPoPProofValidator.ParsedProof parsedProof,
                                 String tokenJkt) throws DPoPProofException {

        if (parsedProof == null) {
            // Reached when Authorization: DPoP <token> is present but the DPoP header is missing.
            throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                    "DPoP proof required; retry with the proof");
        }

        final String httpMethod = (String) ((Axis2MessageContext) synCtx).getAxis2MessageContext()
                .getProperty(Constants.Configuration.HTTP_METHOD);
        final String htu = DPoPUtils.normalizeHtu(synCtx, headers);

        if (httpsRequired) {
            DPoPUtils.checkHttps(htu);
        }

        // JWK thumbprint is derived from actual key material — preferred over the
        // client-controlled kid field as the nonce cache key.
        final String proofJwkThumbprint = parsedProof.getJwkThumbprint();

        if (log.isDebugEnabled()) {
            log.debug("Validating DPoP proof: method=" + httpMethod + ", htu=" + htu
                    + ", kid=" + parsedProof.getKid() + ", proofKeyId=" + proofJwkThumbprint);
        }

        // 1. Nonce check — RFC 9449 §11.3: must reject proofs without nonce whenever one
        //    has been issued to this client, even between strategy rotation points.
        boolean strategyRequiresNonce = proofJwkThumbprint != null
                && nonceStrategy.requiresNonce(proofJwkThumbprint);
        String activeNonce = proofJwkThumbprint != null
                ? cacheProvider.getActiveNonce(proofJwkThumbprint) : null;

        String expectedNonce = null;
        if (strategyRequiresNonce || activeNonce != null) {
            if (activeNonce == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No active nonce for proofKeyId=" + proofJwkThumbprint + "; challenging client");
                }
                throw new DPoPProofException(DPoPProofException.ErrorCode.USE_DPOP_NONCE,
                        "DPoP-Nonce required; retry with the issued nonce");
            }
            expectedNonce = activeNonce;
            if (log.isDebugEnabled()) {
                log.debug("Nonce check required for proofKeyId=" + proofJwkThumbprint);
            }
        }

        // 2. Validate proof JWT (signature, header, claims, ath)
        DPoPProofValidator.ValidationResult result =
                proofValidator.validate(parsedProof, httpMethod, htu, accessToken, expectedNonce);
        final String proofJkt = result.getProofJkt();

        // 3. Replay defense — jti comes from the validation result.
        final String jti = result.getJti();
        if (log.isDebugEnabled()) {
            log.debug("Checking jti replay: jti=" + jti);
        }

        if (jti == null || !cacheProvider.isJtiFirstUse(jti, proofJkt, jtiCacheTtlSeconds)) {
            throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                    "DPoP proof jti has already been used (replay detected)");
        }

        // 4. Token binding — use the resolved tokenJkt
        accessTokenBinder.verifyBinding(tokenJkt, proofJkt);

        // 5. Rotation — if strategy says rotate, issue fresh nonce and deliver on 200 response (RFC 9449 §8.2).
        if (proofJwkThumbprint != null && nonceStrategy.shouldRotate(proofJwkThumbprint)) {
            String rotatedNonce = cacheProvider.issueNonce(proofJwkThumbprint);
            synCtx.setProperty(DPoPConstants.DPOP_RESPONSE_NONCE_PROPERTY, rotatedNonce);
            if (log.isDebugEnabled()) {
                log.debug("Nonce rotated for proofKeyId=" + proofJwkThumbprint);
            }
        }

        // 6. Rewrite Authorization: DPoP <token> → Authorization: Bearer <token>
        //    so the downstream OAuthAuthenticator works unchanged. Remove any case-variant
        //    of the key first, then write with the canonical case the authenticator expects.
        DPoPUtils.removeHeader(headers, AUTH_HEADER);
        headers.put(AUTH_HEADER, BEARER_TAG + accessToken);
        if (stripDpopHeader) {
            DPoPUtils.removeHeader(headers, DPOP_HEADER);
        }
        ((Axis2MessageContext) synCtx).getAxis2MessageContext()
                .setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);

        synCtx.setProperty(DPOP_BOUND_PROPERTY, Boolean.TRUE);
        if (log.isDebugEnabled()) {
            log.debug("DPoP validation successful for request: " + htu);
        }
        return true;
    }

    private Map<String, Object> readDPopConfig() {
        if (GatewayDataHolder.getInstance().getOpenBankingConfigurationService() == null) {
            log.warn("FinancialServicesConfigurationService not available; DPoP handler will use defaults");
            return Collections.emptyMap();
        }
        Map<String, Object> all = GatewayDataHolder.getInstance()
                .getOpenBankingConfigurationService().getConfigurations();
        return all == null ? Collections.emptyMap() : all;
    }

    /**
     * Resolves the configured {@link DPoPCacheProvider} (default {@link LocalDPoPCacheProvider})
     * and hands it every {@code Gateway.DPoP.*} key with the prefix stripped, so a custom
     * impl (e.g. Redis) can read its own backend-specific keys directly from the map
     * without coupling the SPI to any particular backend.
     */
    private DPoPCacheProvider buildCacheProvider(Map<String, Object> cfg) {
        final String fqn = (String) cfg.get(DPoPConstants.ConfigKeys.CACHE_PROVIDER_CLASS);
        DPoPCacheProvider provider;
        if (StringUtils.isBlank(fqn)) {
            provider = new LocalDPoPCacheProvider();
        } else {
            provider = (DPoPCacheProvider) OpenBankingUtils.getClassInstanceFromFQN(fqn);
        }
        provider.initialize(collectDPoPProperties(cfg));
        return provider;
    }

    private Map<String, Object> collectDPoPProperties(Map<String, Object> cfg) {
        Map<String, Object> sub = new HashMap<>();
        final String prefix = DPoPConstants.ConfigKeys.GATEWAY_DPOP_PREFIX;
        for (Map.Entry<String, Object> e : cfg.entrySet()) {
            if (e.getKey().startsWith(prefix)) {
                sub.put(e.getKey().substring(prefix.length()), e.getValue());
            }
        }
        return sub;
    }

    /**
     * Maps the configured nonce strategy name to its implementation.
     * Falls back to {@code never} for unrecognised names.
     */
    private NonceStrategy buildNonceStrategy(String name, int rotateAfterUses, int maxTrackedClients) {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Building nonce strategy: name=%s, rotateAfterUses=%s, maxTrackedClients=%s",
                    name, rotateAfterUses, maxTrackedClients));
        }
        return NonceStrategyType.fromName(name)
                .orElseGet(() -> {
                    log.warn("Unsupported DPoP nonce strategy '" + name + "' - falling back to 'never'.");
                    return NonceStrategyType.NEVER;
                }).create(rotateAfterUses, maxTrackedClients);
    }

    /**
     * Synapse property setter for the per-API DPoP enforcement toggle.
     * Set {@code value="true"} on the handler element to enable DPoP enforcement for an API.
     */
    public void setApiDPoPEnabled(String apiDPoPEnabled) {
        this.apiDPoPEnabled = Boolean.parseBoolean(apiDPoPEnabled);
    }
}
