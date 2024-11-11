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

package com.wso2.openbanking.accelerator.identity.claims;

import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheKey;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeRespDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default OB specific claim provider implementation.
 */
public class OBDefaultClaimProvider extends OBClaimProvider {

    private static final Log log = LogFactory.getLog(OBDefaultClaimProvider.class);
    private static final String S_HASH_CLAIM = "s_hash";

    @Override
    public Map<String, Object> getAdditionalClaims(OAuthAuthzReqMessageContext authAuthzReqMessageContext,
                                                   OAuth2AuthorizeRespDTO authorizeRespDTO)
            throws IdentityOAuth2Exception {

        Map<String, Object> claims = new HashMap<>();
        final String sessionDataKey = authAuthzReqMessageContext.getAuthorizationReqDTO().getSessionDataKey();

        /* State is an optional parameter, so the authorization server must successfully authenticate and
         * must NOT return state nor s_hash. (FAPI1-ADV-5.2.2.1-5)
         */
        String stateValue = SessionDataCache.getInstance().getValueFromCache(new SessionDataCacheKey(sessionDataKey))
                .getoAuth2Parameters().getState();

        if (stateValue != null) {
            claims.put(S_HASH_CLAIM, IdentityCommonUtil.getHashValue(stateValue, null));
            if (log.isDebugEnabled()) {
                log.debug("S_HASH value created using given algorithm for state value:" + stateValue);
            }
        }

        final String responseType = authAuthzReqMessageContext.getAuthorizationReqDTO().getResponseType();
        avoidSettingATHash(responseType, authorizeRespDTO, claims);

        return claims;

    }

    @Override
    public Map<String, Object> getAdditionalClaims(OAuthTokenReqMessageContext tokenReqMessageContext,
                                                   OAuth2AccessTokenRespDTO tokenRespDTO)
            throws IdentityOAuth2Exception {

        return new HashMap<>();
    }

    /**
     * If response_type value is not 'code id_token token', avoid setting at_hash claim to the authorization
     * endpoint id_token as it is OPTIONAL (OIDCC-3.3.2.11).
     *
     * @param responseType     requested auth response_type
     * @param authorizeRespDTO authorizeRespDTO
     * @param claims           returning claims map
     */
    private void avoidSettingATHash(String responseType, OAuth2AuthorizeRespDTO authorizeRespDTO,
                                    Map<String, Object> claims) {

        if (StringUtils.isNotBlank(responseType)) {
            List<String> responseTypes = Arrays.asList(responseType.trim().split("\\s+"));
            if (!(responseTypes.contains(IdentityCommonConstants.CODE)
                    && responseTypes.contains(OAuthConstants.ID_TOKEN)
                    && responseTypes.contains(OAuthConstants.TOKEN))) {
                if (StringUtils.isNotBlank(authorizeRespDTO.getAccessToken())) {
                    authorizeRespDTO.setAccessToken(null);
                }
                claims.put(OAuthConstants.OIDCClaims.AT_HASH, null);
            }
        }
    }

    private JSONObject getRequestBodyFromCache(String[] cachedRequests) {

        try {
            if (cachedRequests.length > 0) {
                return JWTUtils.decodeRequestJWT(cachedRequests[0], "body");
            }
        } catch (ParseException e) {
            log.error("Exception occurred when decoding request. Caused by, ", e);
        }

        return new JSONObject();
    }

    /**
     * If request object state value is empty, ignore the session cache state value, as FAPI-RW says only parameters
     * inside the request object should be used (FAPI1-ADV-5.2.2-10).
     *
     * @param sessionDataKey key used to store session cache
     */
    private void removeStateFromCache(String sessionDataKey) {

        final SessionDataCacheKey sessionDataCacheKey = new SessionDataCacheKey(sessionDataKey);
        SessionDataCacheEntry sessionDataCacheEntry = SessionDataCache.getInstance()
                .getValueFromCache(sessionDataCacheKey);

        if (sessionDataCacheEntry != null) {
            sessionDataCacheEntry.getoAuth2Parameters().setState(null);
            sessionDataCacheEntry.getParamMap().put(OAuthConstants.OAuth20Params.STATE, new String[]{});

            SessionDataCache.getInstance().addToCache(sessionDataCacheKey, sessionDataCacheEntry);
        }
    }
}
