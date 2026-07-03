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

import com.wso2.openbanking.accelerator.common.caching.OpenBankingBaseCache;

/**
 * Cache for DPoP nonce storage (RFC 9449 §8).
 * Maintains the active nonce per client; nonces are reusable until the server rotates them.
 */
public class DPoPNonceCache extends OpenBankingBaseCache<DPoPCacheKey, String> {

    private static final String CACHE_NAME = "OPEN_BANKING_DPOP_NONCE_CACHE";

    private final Integer accessExpiryMinutes;
    private final Integer modifiedExpiryMinutes;

    public DPoPNonceCache(long ttlSeconds) {

        super(CACHE_NAME);
        int minutes = Math.max(1, (int) Math.ceil((double) ttlSeconds / 60));
        this.accessExpiryMinutes = minutes;
        this.modifiedExpiryMinutes = minutes;
    }

    /**
     * Stores the nonce for the given key, replacing any previously active nonce.
     *
     * @param proofKeyId the nonce cache key, typically the JWK thumbprint of the proof's public key.
     * @param nonce      the nonce value to store.
     */
    public void storeNonce(String proofKeyId, String nonce) {

        addToCache(DPoPCacheKey.of(proofKeyId), nonce);
    }

    /**
     * Returns the currently active nonce for the given key, or {@code null} if none exists.
     *
     * @param proofKeyId the nonce cache key, typically the JWK thumbprint of the proof's public key.
     * @return the active nonce, or {@code null}.
     */
    public String getActiveNonce(String proofKeyId) {

        return getFromCache(DPoPCacheKey.of(proofKeyId));
    }

    @Override
    public int getCacheAccessExpiryMinutes() {

        return accessExpiryMinutes;
    }

    @Override
    public int getCacheModifiedExpiryMinutes() {

        return modifiedExpiryMinutes;
    }
}
