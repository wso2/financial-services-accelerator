/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.identity.extensions.cache;

import org.wso2.financial.services.accelerator.common.caching.FinancialServicesBaseCache;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;

/**
 * Cache definition to store objects in open banking iam component implementations.
 */
public class IdentityCache extends FinancialServicesBaseCache<IdentityCacheKey, Object> {

    private static final String cacheName = "FS_IDENTITY_CACHE";

    private final Integer accessExpiryMinutes;
    private final Integer modifiedExpiryMinutes;

    /**
     * Initialize with unique cache name.
     */
    public IdentityCache() {

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

        return FinancialServicesConfigParser.getInstance().getCommonCacheAccessExpiryTime();
    }

    public int setModifiedExpiryMinutes() {

        return FinancialServicesConfigParser.getInstance().getCommonCacheModifiedExpiryTime();
    }
}
