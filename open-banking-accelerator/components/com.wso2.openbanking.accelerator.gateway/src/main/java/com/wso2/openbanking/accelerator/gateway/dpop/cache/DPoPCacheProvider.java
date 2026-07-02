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

package com.wso2.openbanking.accelerator.gateway.dpop.cache;

import java.util.Map;

/**
 * SPI for the two pieces of state the DPoP handler maintains: the JTI replay-defense
 * set (per RFC 9449 §11.1) and the per-client nonce store (per §8).
 * <p>
 * The default implementation {@link LocalDPoPCacheProvider} uses the gateway's
 * node-local Carbon javax.cache and is sufficient for single-node deployments.
 * Distributed deployments must provide their own implementation (e.g. Redis-backed)
 * via {@code Gateway.DPoP.CacheProviderClass} in {@code open-banking.xml},
 * because a node-local seen-set is racy across cluster members — a replayed proof
 * routed to a different node would not be detected.
 * <p>
 * Implementations must be safe for concurrent use by multiple gateway worker threads.
 * {@link #isJtiFirstUse} must be atomic; a partial overlap between the read and write
 * is not acceptable.
 */
public interface DPoPCacheProvider {

    /**
     * Called once at handler init. The map contains every {@code Gateway.DPoP.*} key
     * declared in {@code open-banking.xml}, stripped of the {@code Gateway.DPoP.}
     * prefix, so an implementation can read its own backend-specific keys (e.g.
     * {@code Cache.Redis.Host}) without coupling the SPI to any particular backend.
     */
    void initialize(Map<String, Object> properties);

    /**
     * Atomically checks whether the {@code (jti, jkt)} pair has been seen before and
     * records it if not. Returns {@code true} if this is the first occurrence of the pair
     * (the proof is fresh and has been recorded), {@code false} if it was already present
     * (the proof is a replay and must be rejected).
     *
     * @param jti        the {@code jti} claim from the DPoP proof
     * @param jkt        the SHA-256 JWK thumbprint of the proof's public key (sender scope)
     * @param ttlSeconds how long the entry must remain in the cache
     */
    boolean isJtiFirstUse(String jti, String jkt, long ttlSeconds);

    /**
     * Generates a fresh nonce, stores it against {@code proofKeyId} (replacing any prior
     * value), and returns it. The TTL is determined by the implementation's configuration
     * set at {@link #initialize} time.
     *
     * @param proofKeyId the nonce cache key, typically the JWK thumbprint of the proof's public key
     */
    String issueNonce(String proofKeyId);

    /**
     * Returns the currently active nonce for {@code proofKeyId}, or {@code null} if none.
     *
     * @param proofKeyId the nonce cache key, typically the JWK thumbprint of the proof's public key
     */
    String getActiveNonce(String proofKeyId);

}
