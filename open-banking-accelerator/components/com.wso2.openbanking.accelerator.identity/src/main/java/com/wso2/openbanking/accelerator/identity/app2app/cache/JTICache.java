/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.openbanking.accelerator.identity.app2app.cache;

import com.wso2.openbanking.accelerator.identity.cache.IdentityCache;
import com.wso2.openbanking.accelerator.identity.cache.IdentityCacheKey;

/**
 * Class for maintaining JTI cache.
 */
public class JTICache {

    private static volatile IdentityCache jtiCacheInstance;

    /**
     * Get JTI cache instance.
     *
     * @return IdentityCache instance as JTICache
     */
    public static IdentityCache getInstance() {

        //Outer null check avoids entering synchronized block when jtiCache is not null.
        if (jtiCacheInstance == null) {
            // Synchronize access to ensure thread safety
            synchronized (JTICache.class) {
                // Avoids race condition within threads
                if (jtiCacheInstance == null) {
                    jtiCacheInstance = new IdentityCache();
                }
            }
        }

        return jtiCacheInstance;
    }

    /**
     * Adds the provided JTI (JSON Web Token ID) to the cache for efficient retrieval and management.
     *
     * @param jti The JTI (JSON Web Token ID) to be added to the cache.
     */
    public static void addJtiDataToCache(String jti) {

        JTICache.getInstance().addToCache(IdentityCacheKey.of(jti), jti);
    }

    /**
     * Retrieves the data associated with the provided JTI (JSON Web Token ID) from the cache.
     *
     * @param jti The JTI (JSON Web Token ID) for which data is to be retrieved from the cache.
     * @return The data associated with the provided JTI if found in the cache, otherwise null.
     */
    public static Object getJtiDataFromCache(String jti) {

        return JTICache.getInstance().getFromCache(IdentityCacheKey.of(jti));
    }
}

