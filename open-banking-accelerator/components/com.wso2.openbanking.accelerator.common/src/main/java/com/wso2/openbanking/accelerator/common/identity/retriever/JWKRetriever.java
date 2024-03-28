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

package com.wso2.openbanking.accelerator.common.identity.retriever;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.Resource;
import com.nimbusds.jose.util.ResourceRetriever;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.identity.ApplicationIdentityService;
import com.wso2.openbanking.accelerator.common.identity.cache.JWKSetCache;
import com.wso2.openbanking.accelerator.common.identity.cache.JWKSetCacheKey;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;

/**
 * Retrieve JWK set using nimbus retriever.
 */
public class JWKRetriever {

    private volatile JWKRetriever instance = null;

    private static final int jwksSizeLimit;
    private static final int jwksConnectionTimeout;
    private static final int jwksReadTimeout;

    /**
     * The JWK set retriever.
     */
    private static final ResourceRetriever resourceRetriever;

    static {

        jwksSizeLimit = Integer.parseInt(OpenBankingConfigParser.getInstance().getJwksRetrieverSizeLimit());
        jwksConnectionTimeout = Integer.parseInt(OpenBankingConfigParser.getInstance()
                .getJwksRetrieverConnectionTimeout());
        jwksReadTimeout = Integer.parseInt(OpenBankingConfigParser.getInstance().getJwksRetrieverReadTimeout());
        resourceRetriever = new DefaultResourceRetriever(jwksReadTimeout, jwksConnectionTimeout, jwksSizeLimit);
    }

    /**
     * Get instance of JWKRetriever.
     *
     * @return JWKRetriever instance
     */
    public JWKRetriever getInstance() {

        if (instance == null) {
            synchronized (this) {
                if (instance == null) {
                    instance = new JWKRetriever();
                }
            }
        }
        return instance;
    }

    /**
     * Get JWK Set from remote resource retriever.
     *
     * @param jwksURL   jwksURL in URL format
     * @return  JWKSet
     * @throws OpenBankingException  if an error occurs while retrieving resource
     */
    public JWKSet updateJWKSetFromURL(URL jwksURL) throws OpenBankingException {

        JWKSet jwkSet;
        Resource res;
        try {
            res = resourceRetriever.retrieveResource(jwksURL);
        } catch (IOException e) {
            throw new OpenBankingException("Couldn't retrieve remote JWK set: " + e.getMessage(), e);
        }
        try {
            jwkSet = JWKSet.parse(res.getContent());
        } catch (ParseException e) {
            throw new OpenBankingException("Couldn't parse remote JWK set: " + e.getMessage(), e);
        }

        return jwkSet;
    }

    /**
     * Get JWK Set from cache or retrieve from onDemand retriever.
     *
     * @param jwksURL jwksURL in URL format
     * @param applicationName application name as a string
     * @return jwkSet
     * @throws OpenBankingException  if an error occurs while getting JWK set
     */
    public JWKSet getJWKSet(URL jwksURL , String applicationName) throws OpenBankingException {

        try {
            JWKSetCache jwkSetCache = new JWKSetCache();
            ApplicationIdentityService applicationIdentityService = new ApplicationIdentityService();
            JWKSet jwkSet = jwkSetCache.getFromCacheOrRetrieve(JWKSetCacheKey.of(applicationName), ()
                    ->applicationIdentityService.getPublicJWKSet(
                    applicationName, jwksURL, true));

            if (jwkSet == null) {
                jwkSet = updateJWKSetFromURL(jwksURL);
            }
            return  jwkSet;
        } catch (OpenBankingException e) {
            throw new OpenBankingException("Couldn't get remote JWK set: " + e.getMessage(), e);
        }
    }
}
