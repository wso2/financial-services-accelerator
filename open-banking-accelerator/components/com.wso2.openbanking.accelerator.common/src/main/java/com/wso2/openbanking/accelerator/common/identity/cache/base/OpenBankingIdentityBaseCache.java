/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.openbanking.accelerator.common.identity.cache.base;

import com.wso2.openbanking.accelerator.common.caching.OpenBankingBaseCache;
import com.wso2.openbanking.accelerator.common.caching.OpenBankingBaseCacheKey;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.internal.OpenBankingCommonDataHolder;

/**
 * Cache definition to store objects in open banking iam component implementations.
 * @param <K>
 * @param <V>
 */
public class OpenBankingIdentityBaseCache<K extends OpenBankingBaseCacheKey, V> extends OpenBankingBaseCache<K, V> {

    private static final String cacheName = "OPEN_BANKING_IDENTITY_CACHE";

    private Integer accessExpiryMinutes;
    private Integer modifiedExpiryMinutes;

    /**
     * Initialize with unique cache name.
     */
    public OpenBankingIdentityBaseCache(String cacheName) {

        super(cacheName);
        this.accessExpiryMinutes = setAccessExpiryMinutes();
        this.modifiedExpiryMinutes = setModifiedExpiryMinutes();
    }

    @Override
    public int getCacheAccessExpiryMinutes() {
        return accessExpiryMinutes;
    }

    @Override
    public int getCacheModifiedExpiryMinutes() {
        return modifiedExpiryMinutes;
    }

    public int setAccessExpiryMinutes() {

        return OpenBankingCommonDataHolder.getInstance().getCommonCacheAccessExpiry();
    }

    public int setModifiedExpiryMinutes() {

        return OpenBankingCommonDataHolder.getInstance().getCommonCacheModifiedExpiry();
    }

    public V getFromCacheOrRetrieve(K key, OnDemandRetriever onDemandRetriever) throws  OpenBankingException {

        try {
            return super.getFromCacheOrRetrieve(key, onDemandRetriever);
        } catch (OpenBankingException e) {
            throw new OpenBankingException("Unable to retrieve from cache", e);
        }
    }
}
