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

package com.wso2.openbanking.accelerator.identity.grant.type.handlers;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.RefreshGrantHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * OB specific refresh grant handler.
 */
public class OBRefreshGrantHandler extends RefreshGrantHandler {

    private static final Log log = LogFactory.getLog(OBRefreshGrantHandler.class);

    @Override
    public OAuth2AccessTokenRespDTO issue(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {

        try {
            String clientId = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId();
            if (IdentityCommonUtil.getRegulatoryFromSPMetaData(clientId)) {
                // Apply application scope restrictions if enabled
                if (IdentityCommonUtil.isAppScopeRestrictionEnabledForGrant(IdentityCommonConstants.REFRESH_TOKEN)) {
                    tokReqMsgCtx.setScope(IdentityCommonUtil.retainAllowedScopesForApplication(
                            tokReqMsgCtx.getScope(), clientId));
                }
                OAuth2AccessTokenRespDTO oAuth2AccessTokenRespDTO = super.issue(tokReqMsgCtx);
                executeInitialStep(oAuth2AccessTokenRespDTO, tokReqMsgCtx);
                tokReqMsgCtx.setScope(IdentityCommonUtil.removeInternalScopes(tokReqMsgCtx.getScope()));
                publishUserAccessTokenData(oAuth2AccessTokenRespDTO);
                if (tokReqMsgCtx.getScope().length == 0) {
                    oAuth2AccessTokenRespDTO.setAuthorizedScopes("");
                }
                return oAuth2AccessTokenRespDTO;
            }
        } catch (OpenBankingException e) {
            throw new IdentityOAuth2Exception(e.getMessage());
        }
        return super.issue(tokReqMsgCtx);
    }

    /**
     * Extend this method to publish access token related data.
     *
     * @param oAuth2AccessTokenRespDTO
     */

    public void publishUserAccessTokenData(OAuth2AccessTokenRespDTO oAuth2AccessTokenRespDTO)
            throws IdentityOAuth2Exception {

    }

    /**
     * Extend this method to perform any actions which requires internal scopes.
     *
     * @param oAuth2AccessTokenRespDTO
     * @param tokReqMsgCtx
     */
    public void executeInitialStep(OAuth2AccessTokenRespDTO oAuth2AccessTokenRespDTO,
                                   OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {

    }

    @Override
    public boolean validateScope(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {

        String[] grantedScopes = tokReqMsgCtx.getScope();
        if (!super.validateScope(tokReqMsgCtx)) {
            return false;
        }

        String[] requestedScopes = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getScope();
        if (ArrayUtils.isNotEmpty(requestedScopes)) {
            //Adding internal scopes.
            ArrayList<String> requestedScopeList = new ArrayList<>(Arrays.asList(requestedScopes));
            String consentIdClaim = IdentityExtensionsDataHolder.getInstance().getConfigurationMap()
                    .get(IdentityCommonConstants.CONSENT_ID_CLAIM_NAME).toString();
            for (String scope : grantedScopes) {
                if (scope.startsWith(consentIdClaim)) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Adding custom scope %s to the requested scopes", scope));
                    }
                    requestedScopeList.add(scope);
                }
            }

            // remove duplicates in requestedScopeList
            requestedScopeList = new ArrayList<>(new HashSet<>(requestedScopeList));

            String[] modifiedScopes = requestedScopeList.toArray(new String[0]);
            if (modifiedScopes.length != 0) {
                tokReqMsgCtx.setScope(modifiedScopes);
            }
        }
        return true;
    }
}
