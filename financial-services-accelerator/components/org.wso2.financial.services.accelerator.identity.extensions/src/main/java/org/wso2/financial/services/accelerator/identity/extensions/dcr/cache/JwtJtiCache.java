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

package org.wso2.financial.services.accelerator.identity.extensions.dcr.cache;

import org.wso2.financial.services.accelerator.common.caching.FinancialServicesBaseCache;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;

/**
 * Cache definition to store JTI values of Request JWTs.
 */
public class JwtJtiCache extends FinancialServicesBaseCache<JwtJtiCacheKey, String> {

    private static final String CACHE_NAME = "DCR_JTI_CACHE";
    private Integer accessExpiryMinutes;
    private Integer modifiedExpiryMinutes;
    private static JwtJtiCache jwtJtiCache;

    /**
     * Initialize With unique cache name.
     */
    public JwtJtiCache() {

        super(CACHE_NAME);
        this.accessExpiryMinutes = setAccessExpiryMinutes();
        this.modifiedExpiryMinutes = setModifiedExpiryMinutes();
    }

    /**
     * Singleton getInstance method to create only one object.
     *
     * @return JwtJtiCache object
     */
    public static synchronized JwtJtiCache getInstance() {
        if (jwtJtiCache == null) {
            jwtJtiCache = new JwtJtiCache();
        }
        return jwtJtiCache;
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

        String cacheAccessExpiry = (String) FinancialServicesConfigParser.getInstance().getConfiguration()
                .get(FinancialServicesConstants.JTI_CACHE_ACCESS_EXPIRY);

        return cacheAccessExpiry == null ? 3600 : Integer.parseInt(cacheAccessExpiry);
    }

    public int setModifiedExpiryMinutes() {

        String cacheAccessExpiry = (String) FinancialServicesConfigParser.getInstance().getConfiguration()
                .get(FinancialServicesConstants.JTI_CACHE_MODIFY_EXPIRY);

        return cacheAccessExpiry == null ? 3600 : Integer.parseInt(cacheAccessExpiry);
    }

}
