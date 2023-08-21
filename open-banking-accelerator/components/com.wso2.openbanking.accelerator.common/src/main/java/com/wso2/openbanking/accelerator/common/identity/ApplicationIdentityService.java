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

package com.wso2.openbanking.accelerator.common.identity;

import com.nimbusds.jose.jwk.JWKSet;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.identity.cache.JWKSetCache;
import com.wso2.openbanking.accelerator.common.identity.cache.JWKSetCacheKey;
import com.wso2.openbanking.accelerator.common.identity.retriever.JWKRetriever;
import org.apache.commons.lang.StringUtils;

import java.net.URL;

/**
 * Class to handle retrieving JWKSet from jwksUri.
 */
public class ApplicationIdentityService {

    /**
     * Get JWKSet for application.
     * First checks to get from cache, else retrieve the JWKSet from the URL by calling
     * a method in JWKRetriever
     * @param applicationName
     * @param jwksUrl
     * @param useCache
     * @return JWKSet
     */
    public JWKSet getPublicJWKSet(String applicationName, URL jwksUrl,
                                  boolean useCache) throws OpenBankingException {

        if (StringUtils.isEmpty(applicationName)) {
            throw new OpenBankingException("Application Name is required");
        }

        // Get JWK Set
        if (useCache) {
            JWKSetCache jwkSetCache = new JWKSetCache();
            try {
                return jwkSetCache.getFromCacheOrRetrieve(JWKSetCacheKey.of(applicationName),
                        () -> new JWKRetriever().updateJWKSetFromURL(jwksUrl));
            } catch (OpenBankingException e) {
                throw new OpenBankingException(String.format("Unable to retrieve JWKSet for %s",
                        applicationName), e);
            }
        } else {
            return new JWKRetriever().updateJWKSetFromURL(jwksUrl);
        }
    }
}
