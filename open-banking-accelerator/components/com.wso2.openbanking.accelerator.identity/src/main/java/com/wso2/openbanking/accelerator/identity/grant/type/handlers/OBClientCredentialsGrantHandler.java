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
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.wso2.carbon.identity.oauth.common.exception.InvalidOAuthClientException;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDO;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.ClientCredentialsGrantHandler;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

import java.util.Arrays;

/**
 * OB specific client credentials code grant handler.
 */
public class OBClientCredentialsGrantHandler extends ClientCredentialsGrantHandler {

    @Override
    public OAuth2AccessTokenRespDTO issue(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {

        try {
            if (IdentityCommonUtil.getRegulatoryFromSPMetaData(tokReqMsgCtx.getOauth2AccessTokenReqDTO()
                    .getClientId())) {
                if (IdentityCommonUtil.getDCRModifyResponseConfig() && tokReqMsgCtx.getScope().length > 0 &&
                        Arrays.asList(tokReqMsgCtx.getScope()).contains(IdentityCommonUtil.getDCRScope())) {
                    long validityPeriod = 999999999;
                    OAuthAppDO oAuthAppDO = OAuth2Util
                            .getAppInformationByClientId(tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId());
                    oAuthAppDO.setApplicationAccessTokenExpiryTime(validityPeriod);
                    tokReqMsgCtx.setValidityPeriod(validityPeriod);
                }
                OAuth2AccessTokenRespDTO oAuth2AccessTokenRespDTO = super.issue(tokReqMsgCtx);
                executeInitialStep(oAuth2AccessTokenRespDTO, tokReqMsgCtx);
                tokReqMsgCtx.setScope(IdentityCommonUtil.removeInternalScopes(tokReqMsgCtx.getScope()));
                publishUserAccessTokenData(oAuth2AccessTokenRespDTO);
                return oAuth2AccessTokenRespDTO;
            }
        } catch (OpenBankingException | InvalidOAuthClientException e) {
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
}
