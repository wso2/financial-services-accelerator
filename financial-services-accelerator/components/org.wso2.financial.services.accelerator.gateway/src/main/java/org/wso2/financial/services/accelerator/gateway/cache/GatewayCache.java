/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.gateway.cache;

import org.wso2.financial.services.accelerator.common.caching.FinancialServicesBaseCache;
import org.wso2.financial.services.accelerator.gateway.internal.GatewayDataHolder;

/**
 * Cache definition to store API Resource Security Schemes
 */
public class GatewayCache extends FinancialServicesBaseCache<GatewayCacheKey, Object> {

    private static final String cacheName = "FINANCIAL_SERVICES_GATEWAY_CACHE";

    private final Integer accessExpiryMinutes;
    private final Integer modifiedExpiryMinutes;

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
