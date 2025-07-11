/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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

import org.wso2.financial.services.accelerator.common.caching.FinancialServicesBaseCacheKey;

import java.io.Serializable;
import java.util.Objects;

/**
 * Cache Key for Open Banking Identity cache.
 */
public class IdentityCacheKey extends FinancialServicesBaseCacheKey implements Serializable {

    private static final long serialVersionUID = 143057970021542120L;
    public String identityCacheKey;

    public IdentityCacheKey(String identityCacheKey) {

        this.identityCacheKey = identityCacheKey;
    }

    public static IdentityCacheKey of(String identityCacheKey) {

        return new IdentityCacheKey(identityCacheKey);
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IdentityCacheKey that = (IdentityCacheKey) o;
        return Objects.equals(identityCacheKey, that.identityCacheKey);
    }

    @Override
    public int hashCode() {

        return Objects.hash(identityCacheKey);
    }
}
