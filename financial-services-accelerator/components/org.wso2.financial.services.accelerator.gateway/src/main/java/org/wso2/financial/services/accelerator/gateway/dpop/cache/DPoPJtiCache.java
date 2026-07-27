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

import org.wso2.financial.services.accelerator.common.caching.FinancialServicesBaseCache;

/**
 * Cache for DPoP proof JTI replay defense (RFC 9449 §11.1).
 * Records each seen {@code (jti, jkt)} pair and rejects duplicates.
 */
public class DPoPJtiCache extends FinancialServicesBaseCache<DPoPCacheKey, String> {

    private static final String CACHE_NAME = "FINANCIAL_SERVICES_DPOP_JTI_CACHE";

    private final Integer accessExpiryMinutes;
    private final Integer modifiedExpiryMinutes;

    public DPoPJtiCache(long ttlSeconds) {

        super(CACHE_NAME);
        int minutes = Math.max(1, (int) Math.ceil((double) ttlSeconds / 60));
        this.accessExpiryMinutes = minutes;
        this.modifiedExpiryMinutes = minutes;
    }

    /**
     * Atomically checks whether the {@code (jti, jkt)} pair is new and records it if so.
     *
     * @param jti the {@code jti} claim from the DPoP proof.
     * @param jkt the SHA-256 JWK thumbprint of the proof's public key.
     * @return true if this is the first use of the pair, false if it is a replay.
     */
    public boolean isJtiFirstUse(String jti, String jkt) {

        return addToCacheIfAbsent(DPoPCacheKey.of(jti + ":" + jkt), Boolean.TRUE.toString());
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
