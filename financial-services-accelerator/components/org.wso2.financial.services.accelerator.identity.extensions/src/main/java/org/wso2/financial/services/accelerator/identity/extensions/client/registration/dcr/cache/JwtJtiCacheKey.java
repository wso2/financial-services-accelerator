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


package org.wso2.financial.services.accelerator.identity.extensions.client.registration.dcr.cache;

import org.wso2.financial.services.accelerator.common.caching.FinancialServicesBaseCacheKey;

import java.io.Serializable;
import java.util.Objects;

/**
 * The definition of Cache Key to create JWT JTI Cache.
 */
public class JwtJtiCacheKey extends FinancialServicesBaseCacheKey implements Serializable {

    static final long serialVersionUID = 1382340306L;
    private String jtiValue;

    public JwtJtiCacheKey(String jtiCacheKey) {
        this.jtiValue = jtiCacheKey;
    }

    public static JwtJtiCacheKey of(String jtiCacheKey) {

        return new JwtJtiCacheKey(jtiCacheKey);
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JwtJtiCacheKey that = (JwtJtiCacheKey) o;
        return Objects.equals(jtiValue, that.jtiValue);
    }

    @Override
    public int hashCode() {

        return Objects.hash(jtiValue);
    }
}
