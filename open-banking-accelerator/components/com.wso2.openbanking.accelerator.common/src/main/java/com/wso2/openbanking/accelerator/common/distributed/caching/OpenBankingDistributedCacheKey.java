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

import java.io.Serializable;
import java.util.Objects;

/**
 * Abstract class for Open Banking Distributed Cache Key.
 */
public class OpenBankingDistributedCacheKey implements Serializable {

    private static final long serialVersionUID = -2106706990466051087L;
    private String cacheKey;


    /**
     * public constructor for OpenBankingDistributedCacheKey.
     *
     * @param cacheKey String cache key.
     */
    public OpenBankingDistributedCacheKey(String cacheKey) {
        setCacheKey(cacheKey);
    }

    /**
     * Get Instance OpenBankingDistributedCacheKey.
     *
     * @param cacheKey String cache key.
     * @return new OpenBankingDistributedCacheKey instance.
     */
    public static OpenBankingDistributedCacheKey of(String cacheKey) {
        return new OpenBankingDistributedCacheKey(cacheKey);
    }

    /**
     * Getter for cacheKey.
     *
     * @return String cacheKey.
     */
    public String getCacheKey() {
        return this.cacheKey;
    }

    /**
     * Setter for cacheKey.
     *
     * @param cacheKey String cacheKey.
     */
    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    /**
     * Equals Method for OpenBankingDistributedCacheKey objects.
     *
     * @param o Object.
     * @return True if equal, false if not-equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OpenBankingDistributedCacheKey that = (OpenBankingDistributedCacheKey) o;
        return Objects.equals(getCacheKey(), that.getCacheKey());
    }

    /**
     * hashcode for OpenBankingDistributedCacheKey.
     *
     * @return hashcode.
     */
    @Override
    public int hashCode() {
        return Objects.hash(getCacheKey());
    }
}
