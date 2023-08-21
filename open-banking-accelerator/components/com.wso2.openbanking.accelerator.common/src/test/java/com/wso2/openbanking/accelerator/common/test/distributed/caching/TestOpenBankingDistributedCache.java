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

package com.wso2.openbanking.accelerator.common.test.distributed.caching;

import com.wso2.openbanking.accelerator.common.distributed.caching.OpenBankingDistributedCache;

/**
 * TestOpenBankingDistributedCache.
 */
public class TestOpenBankingDistributedCache
        extends OpenBankingDistributedCache<TestOpenBankingDistributedCacheKey, String> {

    int cacheTimeToLiveMinutes;

    /**
     * Initialize With unique cache name.
     *
     * @param cacheName Name of the cache.
     */
    public TestOpenBankingDistributedCache(String cacheName) {
        super(cacheName);
        setCacheTimeToLiveMinutes();
    }

    @Override
    public int getCacheTimeToLiveMinutes() {
        return this.cacheTimeToLiveMinutes;
    }

    public void setCacheTimeToLiveMinutes() {
        this.cacheTimeToLiveMinutes = 2;
    }
}
