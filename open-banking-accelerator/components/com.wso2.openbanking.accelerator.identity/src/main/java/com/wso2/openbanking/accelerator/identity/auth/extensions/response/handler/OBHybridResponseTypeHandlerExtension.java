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

package com.wso2.openbanking.accelerator.identity.auth.extensions.response.handler;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.authz.handlers.HybridResponseTypeHandler;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeRespDTO;

/**
 * Extension to append scope with OB_ prefix at the end of auth flow, before offering auth code.
 */
public class OBHybridResponseTypeHandlerExtension extends HybridResponseTypeHandler {

    static OBResponseTypeHandler obResponseTypeHandler =
            IdentityExtensionsDataHolder.getInstance().getObResponseTypeHandler();

    /**
     * Extension point to get updated scope and refresh token validity period.
     *
     * @param oauthAuthzMsgCtx
     * @return
     * @throws IdentityOAuth2Exception
     */
    @Override
    public OAuth2AuthorizeRespDTO issue(OAuthAuthzReqMessageContext oauthAuthzMsgCtx) throws IdentityOAuth2Exception {

        try {
            if (!isRegulatory(oauthAuthzMsgCtx.getAuthorizationReqDTO().getConsumerKey())) {
                return issueCode(oauthAuthzMsgCtx);
            }
        } catch (OpenBankingException e) {
            throw  new IdentityOAuth2Exception("Error while reading regulatory property");
        }

        oauthAuthzMsgCtx.setRefreshTokenvalidityPeriod(
                obResponseTypeHandler.updateRefreshTokenValidityPeriod(oauthAuthzMsgCtx));
        if (obResponseTypeHandler.updateApprovedScopes(oauthAuthzMsgCtx) != null) {
            oauthAuthzMsgCtx.setApprovedScope(obResponseTypeHandler.updateApprovedScopes(oauthAuthzMsgCtx));
        } else {
            throw new IdentityOAuth2Exception("Error while updating scopes");
        }
        return issueCode(oauthAuthzMsgCtx);
    }

    /**
     * Separated method to call parent issue.
     *
     * @param oAuthAuthzReqMessageContext
     * @return
     * @throws IdentityOAuth2Exception
     */
    @Generated(message = "cant unit test super calls")
    OAuth2AuthorizeRespDTO issueCode(
            OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext) throws IdentityOAuth2Exception {

        return super.issue(oAuthAuthzReqMessageContext);
    }

    @Generated(message = "Ignoring because it requires a service call")
    boolean isRegulatory(String clientId) throws OpenBankingException {

        return IdentityCommonUtil.getRegulatoryFromSPMetaData(clientId);
    }
}
