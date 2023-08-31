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

package com.wso2.openbanking.accelerator.common.distributed.caching;

import com.hazelcast.map.IMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.TimeUnit;

/**
 * Abstract cache manager for Open Banking Distributed cache.
 *
 * @param <K> Key of the cache.
 * @param <V> Value of the cache.
 */
public abstract class OpenBankingDistributedCache<K extends OpenBankingDistributedCacheKey, V> {
    private final String cacheName;

    private static final Log log = LogFactory.getLog(OpenBankingDistributedCache.class);

    /**
     * Initialize With unique cache name.
     *
     * @param cacheName Name of the cache.
     */
    public OpenBankingDistributedCache(String cacheName) {

        this.cacheName = cacheName;
        if (log.isDebugEnabled()) {
            log.debug(String.format("Distributed Cache initialized for %s.", cacheName.replaceAll("[\r\n]", "")));
        }
    }

    /**
     * Get from cache.
     *
     * @param key cache key.
     * @return cache.
     */
    public V getFromCache(K key) {

        if (isEnabled()) {

            IMap<K, V> cache = getBaseCache();

            if (cache.containsKey(key)) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Found cache entry `%s` in cache %s.",
                            key.toString().replaceAll("[\r\n]", ""), cacheName.replaceAll("[\r\n]", "")));
                }
                return cache.get(key);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Cache entry `%s` is not Found in cache %s.",
                            key.toString().replaceAll("[\r\n]", ""), cacheName.replaceAll("[\r\n]", "")));
                }
                return null;
            }
        } else {
            log.debug("Distributed cache is Disabled.");
            return null;
        }
    }

    /**
     * Add Object to cache.
     *
     * @param key   cache key.
     * @param value object to be cached.
     */
    public void addToCache(K key, V value) {
        if (isEnabled()) {
            IMap<K, V> cache = getBaseCache();
            if (log.isDebugEnabled()) {
                log.debug(String.format("`%s` added into cache %s.", key.toString().replaceAll("[\r\n]", ""),
                        cacheName.replaceAll("[\r\n]", "")));
            }
            cache.put(key, value, getCacheTimeToLiveMinutes(), TimeUnit.MINUTES);
        } else {
            log.debug("Distributed cache is Disabled.");
        }
    }

    /**
     * Remove from cache.
     *
     * @param key cache key.
     */
    public void removeFromCache(K key) {
        if (isEnabled()) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("`%s` removed from cache %s.", key.toString().replaceAll("[\r\n]", ""),
                        cacheName.replaceAll("[\r\n]", "")));
            }
            IMap<K, V> cache = getBaseCache();
            cache.remove(key);
        } else {
            log.debug("Distributed cache is Disabled.");
        }
    }

    /**
     * Method to check if the cache is empty.
     *
     * @return true if empty, false if populated.
     */
    public boolean isEmpty() {
        return getBaseCache().isEmpty();
    }

    /**
     * Get the clustered cache.
     *
     * @return cache map.
     */
    private IMap<K, V> getBaseCache() {
        return OpenBankingDistributedMember.of().getHazelcastInstance().getMap(this.cacheName);
    }

    /**
     * Method to get if the Distributed caching is enabled.
     *
     * @return True if enabled, false if disabled.
     */
    private boolean isEnabled() {
        return OpenBankingDistributedMember.of().isEnabled();
    }

    /**
     * Get Cache expiry time upon modification in minutes.
     *
     * @return integer denoting number of minutes.
     */
    public abstract int getCacheTimeToLiveMinutes();

}
