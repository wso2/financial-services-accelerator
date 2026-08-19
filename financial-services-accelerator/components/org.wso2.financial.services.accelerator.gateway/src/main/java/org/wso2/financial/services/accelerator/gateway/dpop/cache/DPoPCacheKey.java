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

import org.wso2.financial.services.accelerator.common.caching.FinancialServicesBaseCacheKey;

import java.io.Serializable;
import java.util.Objects;

/**
 * Cache key for DPoP JTI replay-defense and nonce caches.
 */
public class DPoPCacheKey extends FinancialServicesBaseCacheKey implements Serializable {

    private static final long serialVersionUID = 6823941170341530021L;
    private final String dPoPCacheKey;

    public DPoPCacheKey(String dPoPCacheKey) {

        this.dPoPCacheKey = dPoPCacheKey;
    }

    public static DPoPCacheKey of(String dPoPCacheKey) {

        return new DPoPCacheKey(dPoPCacheKey);
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DPoPCacheKey that = (DPoPCacheKey) o;
        return Objects.equals(dPoPCacheKey, that.dPoPCacheKey);
    }

    @Override
    public int hashCode() {

        return Objects.hash(dPoPCacheKey);
    }
}
