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

package com.wso2.openbanking.accelerator.gateway.cache;

import com.wso2.openbanking.accelerator.common.caching.OpenBankingBaseCache;
import com.wso2.openbanking.accelerator.gateway.internal.GatewayDataHolder;

/**
 * Cache definition to store API Resource Security Schemes.
 */
public class GatewayCache extends OpenBankingBaseCache<GatewayCacheKey, Object> {

    private static final String cacheName = "OPEN_BANKING_GATEWAY_CACHE";

    private Integer accessExpiryMinutes;
    private Integer modifiedExpiryMinutes;

    /**
     * Initialize with unique cache name.
     */
    public GatewayCache() {

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

        return GatewayDataHolder.getInstance().getGatewayCacheAccessExpiry();
    }

    public int setModifiedExpiryMinutes() {

        return GatewayDataHolder.getInstance().getGatewayCacheModifiedExpiry();
    }
}
