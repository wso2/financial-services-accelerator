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
import com.wso2.openbanking.accelerator.gateway.internal.TPPCertValidatorDataHolder;

/**
 * Cache definition to store API Resource Security Schemes.
 */
public class CertificateRevocationCache extends OpenBankingBaseCache<GatewayCacheKey, Boolean> {

    private static final String CACHE_NAME = "OPEN_BANKING_CLIENT_CERTIFICATE_CACHE";

    private static CertificateRevocationCache clientCertificateCache;
    private final Integer accessExpiryMinutes;
    private final Integer modifiedExpiryMinutes;

    /**
     * Initialize with unique cache name.
     */
    private CertificateRevocationCache() {

        super(CACHE_NAME);
        this.accessExpiryMinutes = setAccessExpiryMinutes();
        this.modifiedExpiryMinutes = setModifiedExpiryMinutes();
    }

    /**
     * Singleton getInstance method to create only one object.
     *
     * @return TPPValidationCache object
     */
    public static synchronized CertificateRevocationCache getInstance() {
        if (clientCertificateCache == null) {
            clientCertificateCache = new CertificateRevocationCache();
        }
        return clientCertificateCache;
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

        return TPPCertValidatorDataHolder.getInstance().getTppCertRevocationCacheExpiry();
    }

    public int setModifiedExpiryMinutes() {

        return TPPCertValidatorDataHolder.getInstance().getTppCertRevocationCacheExpiry();
    }
}
