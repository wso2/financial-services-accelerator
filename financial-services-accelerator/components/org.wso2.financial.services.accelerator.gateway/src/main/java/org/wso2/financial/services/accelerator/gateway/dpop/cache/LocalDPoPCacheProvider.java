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

package org.wso2.financial.services.accelerator.gateway.dpop.cache;

import org.wso2.financial.services.accelerator.common.logging.Log;
import org.wso2.financial.services.accelerator.common.logging.LogFactory;
import org.wso2.financial.services.accelerator.gateway.dpop.DPoPConstants;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

import static org.wso2.financial.services.accelerator.gateway.dpop.util.DPoPUtils.parseLong;

/**
 * Default {@link DPoPCacheProvider} backed by the gateway's node-local
 * {@code FinancialServicesBaseCache} infrastructure. Suitable for single-node deployments.
 * Distributed gateway deployments require a cluster-aware implementation since the JTI
 * seen-set and nonce store are not replicated across nodes — a replayed proof or a reused
 * nonce routed to a different node would not be detected.
 */
public class LocalDPoPCacheProvider implements DPoPCacheProvider {

    private static final Log log = LogFactory.getLog(LocalDPoPCacheProvider.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private DPoPJtiCache jtiCache;
    private DPoPNonceCache nonceCache;

    @Override
    public void initialize(Map<String, Object> properties) {

        long jtiTtl = parseLong(properties.get("JtiCacheTtlSeconds"),
                DPoPConstants.Defaults.JTI_CACHE_TTL_SECONDS);
        long nonceTtl = parseLong(properties.get("NonceTtlSeconds"),
                DPoPConstants.Defaults.NONCE_TTL_SECONDS);

        this.jtiCache = new DPoPJtiCache(jtiTtl);
        this.nonceCache = new DPoPNonceCache(nonceTtl);

        if (log.isDebugEnabled()) {
            log.debug("LocalDPoPCacheProvider initialized. jtiTtlSeconds=" + jtiTtl
                    + ", nonceTtlSeconds=" + nonceTtl);
        }
    }

    /**
     * {@code ttlSeconds} is consumed at initialization; this parameter is ignored per call.
     */
    @Override
    public boolean isJtiFirstUse(String jti, String jkt, long ttlSeconds) {

        return jtiCache.isJtiFirstUse(jti, jkt);
    }

    /**
     * Generates a cryptographically random nonce, stores it against {@code proofKeyId}
     * replacing any prior value, and returns it to be sent in the {@code DPoP-Nonce}
     * response header.
     */
    @Override
    public String issueNonce(String proofKeyId) {

        String nonce = generateNonce();
        nonceCache.storeNonce(proofKeyId, nonce);
        return nonce;
    }

    /**
     * Returns the currently active nonce for {@code proofKeyId}, or {@code null} if none
     * has been issued yet.
     */
    @Override
    public String getActiveNonce(String proofKeyId) {

        return nonceCache.getActiveNonce(proofKeyId);
    }

    private String generateNonce() {

        byte[] bytes = new byte[16];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
