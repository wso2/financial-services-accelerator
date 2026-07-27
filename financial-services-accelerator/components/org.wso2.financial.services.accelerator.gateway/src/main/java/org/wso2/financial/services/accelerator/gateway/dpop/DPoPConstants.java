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

import static org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants.BEARER_TAG;

/**
 * RFC 9449 claim names and {@code financial-services.xml} configuration keys
 * consumed by the DPoP module.
 */
public final class DPoPConstants {

    private DPoPConstants() {
    }

    public static final String DPOP_HEADER = "DPoP";
    public static final String DPOP_SCHEME = "DPoP";
    public static final String DPOP_NONCE_HEADER = "DPoP-Nonce";

    public static final String DPOP_JWT_TYPE = "dpop+jwt";
    public static final String SHA256_HASH_ALG = "SHA-256";

    public static final int DPOP_SCHEME_LEN = DPOP_SCHEME.length() + 1;
    public static final int BEARER_SCHEME_LEN = BEARER_TAG.trim().length() + 1;

    public static final String DPOP_BOUND_PROPERTY = "dpop.bound";
    /** Synapse message property used to pass a rotated nonce from request to response handling. */
    public static final String DPOP_RESPONSE_NONCE_PROPERTY = "dpop.response.nonce";
    public static final String CACHE_CONTROL_NO_STORE = "no-store";

    /**
     * RFC 9449 §4.2 claim names.
     */
    public static final class Claims {

        private Claims() {
        }

        public static final String CNF_CLAIM = "cnf";
        public static final String JKT_CLAIM = "jkt";
        public static final String JTI_CLAIM = "jti";
        public static final String HTM_CLAIM = "htm";
        public static final String HTU_CLAIM = "htu";
        public static final String IAT_CLAIM = "iat";
        public static final String ATH_CLAIM = "ath";
        public static final String NONCE_CLAIM = "nonce";
    }

    /**
     * Flattened keys under {@code <FinancialServices><Gateway><DPoP>...</DPoP></Gateway></FinancialServices>}
     * as produced by {@code FinancialServicesConfigParser}.
     */
    public static final class ConfigKeys {

        private ConfigKeys() {
        }

        public static final String GATEWAY_DPOP_PREFIX = "Gateway.DPoP.";
        public static final String ACCEPTED_ALGORITHMS = GATEWAY_DPOP_PREFIX + "AcceptedAlgorithms";
        public static final String IAT_SKEW_SECONDS = GATEWAY_DPOP_PREFIX + "IatSkewSeconds";
        public static final String JTI_CACHE_TTL_SECONDS = GATEWAY_DPOP_PREFIX + "JtiCacheTtlSeconds";
        public static final String NONCE_STRATEGY = GATEWAY_DPOP_PREFIX + "NonceStrategy";
        public static final String NONCE_TTL_SECONDS = GATEWAY_DPOP_PREFIX + "NonceTtlSeconds";
        public static final String NONCE_ROTATE_AFTER_USES = GATEWAY_DPOP_PREFIX + "NonceRotateAfterUses";
        public static final String NONCE_MAX_TRACKED_CLIENTS = GATEWAY_DPOP_PREFIX + "NonceMaxTrackedClients";
        public static final String HTTPS_REQUIRED = GATEWAY_DPOP_PREFIX + "HttpsRequired";
        public static final String STRIP_DPOP_HEADER = GATEWAY_DPOP_PREFIX + "StripDpopHeader";
        public static final String INTROSPECTION_CACHE_TTL_SECONDS =
                GATEWAY_DPOP_PREFIX + "IntrospectionCacheTtlSeconds";
        public static final String CACHE_PROVIDER_CLASS = GATEWAY_DPOP_PREFIX + "CacheProviderClass";
    }

    /**
     * Default values that apply when a key is absent from {@code financial-services.xml}.
     */
    public static final class Defaults {

        private Defaults() {
        }

        public static final String ACCEPTED_ALGORITHMS = "ES256,ES384,ES512,PS256,PS384,PS512";
        public static final long IAT_SKEW_SECONDS = 60L;
        public static final long JTI_CACHE_TTL_SECONDS = 120L;
        public static final String NONCE_STRATEGY = "never";
        public static final long NONCE_TTL_SECONDS = 300L;
        public static final int NONCE_ROTATE_AFTER_USES = 10;
        public static final int NONCE_MAX_TRACKED_CLIENTS = 10_000;
        public static final boolean HTTPS_REQUIRED = true;
        public static final boolean STRIP_DPOP_HEADER = false;
        public static final long INTROSPECTION_CACHE_TTL_SECONDS = 60L;
    }
}
