/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
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

package org.wso2.financial.services.accelerator.gateway.dpop.binding;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.impl.dto.KeyManagerDto;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.financial.services.accelerator.common.logging.Log;
import org.wso2.financial.services.accelerator.common.logging.LogFactory;
import org.wso2.financial.services.accelerator.gateway.dpop.DPoPConstants;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.wso2.financial.services.accelerator.gateway.dpop.DPoPConstants.SHA256_HASH_ALG;

/**
 * Wraps the APIM Key Manager introspection endpoint to retrieve {@code cnf.jkt}
 * for opaque tokens. Results are cached keyed by a SHA-256 hash of the raw token
 * to avoid repeated round-trips for the same token within its validity window.
 */
public class IntrospectionClient {

    private static final Log log = LogFactory.getLog(IntrospectionClient.class);

    private final long cacheTtlMs;
    private final ConcurrentHashMap<String, CachedEntry> introspectionCache = new ConcurrentHashMap<>();

    public IntrospectionClient(long cacheTtlSeconds) {
        this.cacheTtlMs = cacheTtlSeconds * 1000L;
    }

    /**
     * Returns the {@code cnf.jkt} from token introspection, or {@code null} if the
     * token has no DPoP binding.
     *
     * @param accessToken raw opaque access token
     * @throws IntrospectionException if the introspection call fails
     */
    public String getJwkThumbprint(String accessToken) throws IntrospectionException {
        String cacheKey = hashToken(accessToken);
        CachedEntry cached = introspectionCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return cached.jkt;
        }
        final String jkt = introspect(accessToken);
        if (StringUtils.isNotBlank(jkt)) {
            // putIfAbsent ensures that if two threads both see a cache miss and both call
            // introspect(), only the first result is stored. The second thread's result is
            // discarded silently — both return the same jkt since introspection is deterministic.
            introspectionCache.putIfAbsent(cacheKey,
                    new CachedEntry(jkt, System.currentTimeMillis() + cacheTtlMs));
        }
        return jkt;
    }

    private String introspect(String accessToken) throws IntrospectionException {
        try {
            final String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .getTenantDomain(true);
            Map<String, KeyManagerDto> keyManagerDtoMap = KeyManagerHolder.getTenantKeyManagers(tenantDomain);
            if (keyManagerDtoMap == null || keyManagerDtoMap.isEmpty()) {
                throw new IntrospectionException("No key manager configured for tenant: " + tenantDomain);
            }
            for (Map.Entry<String, KeyManagerDto> entry : keyManagerDtoMap.entrySet()) {
                KeyManager keyManager = entry.getValue().getKeyManager();
                if (keyManager == null) {
                    continue;
                }
                try {
                    AccessTokenInfo tokenInfo = keyManager.getTokenMetaData(accessToken);
                    if (tokenInfo == null || !tokenInfo.isTokenValid()) {
                        continue;
                    }
                    Object cnf = tokenInfo.getParameter(DPoPConstants.Claims.CNF_CLAIM);
                    if (cnf instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Object jkt = ((Map<String, Object>) cnf).get(DPoPConstants.Claims.JKT_CLAIM);
                        return jkt != null ? jkt.toString() : null;
                    }
                    return null;
                } catch (APIManagementException e) {
                    log.debug("Key manager " + entry.getKey()
                            + " failed to introspect token, trying next: " + e.getMessage());
                }
            }
            throw new IntrospectionException("Token introspection failed for all configured key managers");
        } catch (IntrospectionException e) {
            throw e;
        } catch (Exception e) {
            throw new IntrospectionException("Unexpected error during token introspection: " + e.getMessage(), e);
        }
    }

    private String hashToken(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance(SHA256_HASH_ALG);
            byte[] hash = md.digest(token.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            log.debug("SHA-256 algorithm not available for token hashing, falling back to hashCode", e);
            return String.valueOf(token.hashCode());
        }
    }

    private static final class CachedEntry {

        final String jkt;
        final long expiresAt;

        CachedEntry(String jkt, long expiresAt) {
            this.jkt = jkt;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    /**
     * Thrown when the upstream introspection endpoint cannot resolve a token.
     */
    public static class IntrospectionException extends Exception {

        private static final long serialVersionUID = 87452349823745L;

        public IntrospectionException(String message) {
            super(message);
        }

        public IntrospectionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
