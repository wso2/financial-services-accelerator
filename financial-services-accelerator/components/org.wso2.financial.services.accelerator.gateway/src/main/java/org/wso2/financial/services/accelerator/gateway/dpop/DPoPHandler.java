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
import org.apache.commons.lang3.StringUtils;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.financial.services.accelerator.common.logging.Log;
import org.wso2.financial.services.accelerator.common.logging.LogFactory;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.gateway.dpop.binding.AccessTokenBinder;
import org.wso2.financial.services.accelerator.gateway.dpop.binding.IntrospectionClient;
import org.wso2.financial.services.accelerator.gateway.dpop.cache.DPoPCacheProvider;
import org.wso2.financial.services.accelerator.gateway.dpop.cache.LocalDPoPCacheProvider;
import org.wso2.financial.services.accelerator.gateway.dpop.nonce.AlwaysNonceStrategy;
import org.wso2.financial.services.accelerator.gateway.dpop.nonce.NeverNonceStrategy;
import org.wso2.financial.services.accelerator.gateway.dpop.nonce.NonceStrategy;
import org.wso2.financial.services.accelerator.gateway.dpop.nonce.RotatingNonceStrategy;
import org.wso2.financial.services.accelerator.gateway.dpop.proof.DPoPProofException;
import org.wso2.financial.services.accelerator.gateway.dpop.proof.DPoPProofValidator;
import org.wso2.financial.services.accelerator.gateway.dpop.util.Challenge;
import org.wso2.financial.services.accelerator.gateway.dpop.util.DPoPUtils;
import org.wso2.financial.services.accelerator.gateway.internal.GatewayDataHolder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants.AUTH_HEADER;
import static org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants.BEARER_TAG;
import static org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils.equalsIgnoreCase;
import static org.wso2.financial.services.accelerator.gateway.dpop.DPoPConstants.BEARER_SCHEME_LEN;
import static org.wso2.financial.services.accelerator.gateway.dpop.DPoPConstants.DPOP_BOUND_PROPERTY;
import static org.wso2.financial.services.accelerator.gateway.dpop.DPoPConstants.DPOP_HEADER;
import static org.wso2.financial.services.accelerator.gateway.dpop.DPoPConstants.DPOP_SCHEME;
import static org.wso2.financial.services.accelerator.gateway.dpop.DPoPConstants.DPOP_SCHEME_LEN;

/**
 * Custom Synapse handler that validates DPoP-bound access tokens per RFC 9449.
 * <p>
 * Must be positioned BEFORE the built-in {@code APIAuthenticationHandler} in
 * {@code velocity_template.xml}. All tuning is read from {@code financial-services.xml}
 * under {@code <Gateway><DPoP>...</DPoP></Gateway>}; the only Synapse property
 * the handler accepts is {@code apiDPoPEnabled}, which lets an API owner opt a single
 * API out of DPoP enforcement without touching the global config.
 *
 * <pre>
 * &lt;handler class="org.wso2.financial.services.accelerator.gateway.dpop.DPoPHandler"&gt;
 *     &lt;property name="apiDPoPEnabled" value="true"/&gt;
 * &lt;/handler&gt;
 * </pre>
 */
public class DPoPHandler extends AbstractHandler implements ManagedLifecycle {

    private static final Log log = LogFactory.getLog(DPoPHandler.class);

    // Per-API DPoP enforcement flag — overrides the global setting for a single API
    private boolean apiDPoPEnabled = true;

    // Resolved at init() from financial-services.xml
    private boolean globalDPoPEnabled;
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

        this.globalDPoPEnabled = DPoPUtils.parseBool(cfg.get(DPoPConstants.ConfigKeys.ENABLED),
                DPoPConstants.Defaults.ENABLED);
        this.iatSkewSeconds = DPoPUtils.parseLong(cfg.get(DPoPConstants.ConfigKeys.IAT_SKEW_SECONDS),
                DPoPConstants.Defaults.IAT_SKEW_SECONDS);
        this.jtiCacheTtlSeconds = DPoPUtils.parseLong(cfg.get(DPoPConstants.ConfigKeys.JTI_CACHE_TTL_SECONDS),
                DPoPConstants.Defaults.JTI_CACHE_TTL_SECONDS);
        this.stripDpopHeader = DPoPUtils.parseBool(cfg.get(DPoPConstants.ConfigKeys.STRIP_DPOP_HEADER),
                DPoPConstants.Defaults.STRIP_DPOP_HEADER);
        this.httpsRequired = DPoPUtils.parseBool(cfg.get(DPoPConstants.ConfigKeys.HTTPS_REQUIRED),
                DPoPConstants.Defaults.HTTPS_REQUIRED);

        Set<String> algorithms =
                DPoPUtils.parseAlgorithms((String) cfg.get(DPoPConstants.ConfigKeys.ACCEPTED_ALGORITHMS));
        String nonceStrategyName = DPoPUtils.parseString(cfg.get(DPoPConstants.ConfigKeys.NONCE_STRATEGY),
                DPoPConstants.Defaults.NONCE_STRATEGY);
        int nonceRotateAfterUses = (int) DPoPUtils.parseLong(cfg.get(DPoPConstants.ConfigKeys.NONCE_ROTATE_AFTER_USES),
                DPoPConstants.Defaults.NONCE_ROTATE_AFTER_USES);
        long introspectionTtl = DPoPUtils.parseLong(cfg.get(DPoPConstants.ConfigKeys.INTROSPECTION_CACHE_TTL_SECONDS),
                DPoPConstants.Defaults.INTROSPECTION_CACHE_TTL_SECONDS);

        this.proofValidator = new DPoPProofValidator(algorithms, iatSkewSeconds);
        this.nonceStrategy = buildNonceStrategy(nonceStrategyName, nonceRotateAfterUses);
        this.accessTokenBinder = new AccessTokenBinder(new IntrospectionClient(introspectionTtl));
        this.challenge = new Challenge(String.join(" ", algorithms));

        this.cacheProvider = buildCacheProvider(cfg);

        log.info("DPoP handler initialized. globalDPoPEnabled=" + globalDPoPEnabled
                + ", algorithms=" + algorithms
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
            log.debug("Handling request in DPoPHandler. " +
                    "apiDPoPEnabled=" + apiDPoPEnabled + ", globalDPoPEnabled=" + globalDPoPEnabled);
        }
        if (!apiDPoPEnabled || !globalDPoPEnabled) {
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

            log.debug(() -> "DPoP handler: scheme=" + (isDPoPScheme ? "DPoP" : "Bearer")
                    + ", hasDPoPProof=" + (dPoPHeader != null));

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

                log.debug(() -> "Rejecting DPoP-bound token presented without a proof: cnf.jkt=" + tokenJkt);
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

        log.debug(() -> "Validating DPoP proof: method=" + httpMethod + ", htu=" + htu
                + ", kid=" + parsedProof.getKid() + ", proofKeyId=" + proofJwkThumbprint);

        // 1. Nonce check — if nonce is required but absent or expired, reject immediately.
        //    The catch block in handleRequest issues the nonce and includes it in the 401
        //    response for both the "no active nonce" and "nonce mismatch" cases, so no
        //    issueNonce call is made here.
        String expectedNonce = null;
        if (proofJwkThumbprint != null && nonceStrategy.requiresNonce(proofJwkThumbprint)) {
            expectedNonce = cacheProvider.getActiveNonce(proofJwkThumbprint);
            if (expectedNonce == null) {

                log.debug(() -> "No active nonce for proofKeyId=" + proofJwkThumbprint + "; challenging client");
                throw new DPoPProofException(DPoPProofException.ErrorCode.USE_DPOP_NONCE,
                        "DPoP-Nonce required; retry with the issued nonce");
            }
            log.debug(() -> "Nonce check: active nonce found for proofKeyId=" + proofJwkThumbprint);
        }

        // 2. Validate proof JWT (signature, header, claims, ath)
        DPoPProofValidator.ValidationResult result =
                proofValidator.validate(parsedProof, httpMethod, htu, accessToken, expectedNonce);
        final String proofJkt = result.getProofJkt();

        // 3. Replay defense — jti comes from the validation result.
        final String jti = result.getJti();
        log.debug(() -> "Checking jti replay: jti=" + jti);

        if (jti == null || !cacheProvider.isJtiFirstUse(jti, proofJkt, jtiCacheTtlSeconds)) {
            throw new DPoPProofException(DPoPProofException.ErrorCode.INVALID_DPOP_PROOF,
                    "DPoP proof jti has already been used (replay detected)");
        }

        // 4. Token binding — use the resolved tokenJkt
        accessTokenBinder.verifyBinding(tokenJkt, proofJkt);

        // 5. Consume the nonce (if used)
        if (expectedNonce != null) {
            cacheProvider.isNonceValidAndConsumed(proofJwkThumbprint, expectedNonce);
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
        log.debug(() -> "DPoP validation successful for request: " + htu);

        return true;
    }

    private Map<String, Object> readDPopConfig() {
        if (GatewayDataHolder.getInstance().getFinancialServicesConfigurationService() == null) {
            log.warn("FinancialServicesConfigurationService not available; DPoP handler will use defaults");
            return Collections.emptyMap();
        }
        Map<String, Object> all = GatewayDataHolder.getInstance()
                .getFinancialServicesConfigurationService().getConfigurations();
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
            provider = FinancialServicesUtils.getClassInstanceFromFQN(fqn, DPoPCacheProvider.class);
            if (provider == null) {
                throw new IllegalStateException("Configured " + DPoPConstants.ConfigKeys.CACHE_PROVIDER_CLASS
                        + " = '" + fqn + "' could not be instantiated as a " + DPoPCacheProvider.class.getName());
            }
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

    private NonceStrategy buildNonceStrategy(String name, int rotateAfterUses) {
        if (equalsIgnoreCase("always", name)) {
            return new AlwaysNonceStrategy();
        } else if (equalsIgnoreCase("rotating", name)) {
            return new RotatingNonceStrategy(rotateAfterUses);
        }
        return new NeverNonceStrategy();
    }

    /**
     * Synapse property setter for the per-API DPoP enforcement toggle.
     * Set {@code value="false"} on the handler element to opt a single API out of
     * DPoP enforcement without touching the global configuration.
     */
    public void setApiDPoPEnabled(String apiDPoPEnabled) {
        this.apiDPoPEnabled = Boolean.parseBoolean(apiDPoPEnabled);
    }
}
