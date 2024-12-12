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

package org.wso2.financial.services.accelerator.identity.extensions.claims;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheKey;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeRespDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.financial.services.accelerator.common.util.JWTUtils;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonConstants;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonUtils;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.openidconnect.model.Constants.JWT_PART_DELIMITER;
import static org.wso2.carbon.identity.openidconnect.model.Constants.NUMBER_OF_PARTS_IN_JWE;

/**
 * Default FS specific claim provider implementation.
 */
public class FSDefaultClaimProvider extends FSClaimProvider {

    private static final Log log = LogFactory.getLog(FSDefaultClaimProvider.class);

    @Override
    public Map<String, Object> getAdditionalClaims(OAuthAuthzReqMessageContext authAuthzReqMessageContext,
                                                   OAuth2AuthorizeRespDTO authorizeRespDTO)
            throws IdentityOAuth2Exception {

        Map<String, Object> claims = new HashMap<>();
        String[] cachedRequests = null;
        final String sessionDataKey = authAuthzReqMessageContext.getAuthorizationReqDTO().getSessionDataKey();
        if (StringUtils.isNotBlank(sessionDataKey)) {
            cachedRequests = SessionDataCache.getInstance()
                    .getValueFromCache(new SessionDataCacheKey(sessionDataKey)).getParamMap().get("request");
        }
        if (cachedRequests != null && !(cachedRequests[0].split(JWT_PART_DELIMITER).length == NUMBER_OF_PARTS_IN_JWE)) {
            JSONObject requestBody = getRequestBodyFromCache(cachedRequests);

            /* State is an optional parameter, so the authorization server must successfully authenticate and
             * must NOT return state nor s_hash. (FAPI1-ADV-5.2.2.1-5)
             */
            final String state = requestBody.getString(OAuthConstants.OAuth20Params.STATE);
            if (StringUtils.isNotEmpty(state)) {
                claims.put(IdentityCommonConstants.S_HASH, IdentityCommonUtils.getHashValue(state, null));
            } else {
                // state is empty, removing state from cache too
                removeStateFromCache(sessionDataKey);
            }
        }
        final String responseType = authAuthzReqMessageContext.getAuthorizationReqDTO().getResponseType();
        setATHashAsNull(responseType, authorizeRespDTO, claims);

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
    private void setATHashAsNull(String responseType, OAuth2AuthorizeRespDTO authorizeRespDTO,
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
                return new JSONObject(JWTUtils.decodeRequestJWT(cachedRequests[0], "body"));
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
