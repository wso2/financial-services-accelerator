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

package org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.response.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.authz.handlers.CodeResponseTypeHandler;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeRespDTO;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;

/**
 * Extension to append scope with FS_ prefix at the end of auth flow, before offering auth code.
 */
public class FSCodeResponseTypeHandlerExtension extends CodeResponseTypeHandler {

    private static final Log log = LogFactory.getLog(FSCodeResponseTypeHandlerExtension.class);
    static FSResponseTypeHandler fsResponseTypeHandler =
            IdentityExtensionsDataHolder.getInstance().getObResponseTypeHandler();

    /**
     * Extension point to get updated scope and refresh token validity period.
     *
     * @param oauthAuthzMsgCtx  OAuthAuthzReqMessageContext
     * @return OAuth2AuthorizeRespDTO
     * @throws IdentityOAuth2Exception If an error occurred while issuing the code.
     */
    @Override
    public OAuth2AuthorizeRespDTO issue(OAuthAuthzReqMessageContext oauthAuthzMsgCtx) throws IdentityOAuth2Exception {

        try {
            if (!isRegulatory(oauthAuthzMsgCtx.getAuthorizationReqDTO().getConsumerKey())) {
                return issueCode(oauthAuthzMsgCtx);
            }
        } catch (RequestObjectException e) {
            log.error("Error while reading regulatory property", e);
            throw new IdentityOAuth2Exception("Error while reading regulatory property");
        }

        oauthAuthzMsgCtx.setRefreshTokenvalidityPeriod(
                fsResponseTypeHandler.updateRefreshTokenValidityPeriod(oauthAuthzMsgCtx));
        String[] approvedScopes = fsResponseTypeHandler.updateApprovedScopes(oauthAuthzMsgCtx);
        if (approvedScopes != null) {
            oauthAuthzMsgCtx.setApprovedScope(approvedScopes);
        } else {
            log.error("Error while updating scopes");
            throw new IdentityOAuth2Exception("Error while updating scopes");
        }
        return issueCode(oauthAuthzMsgCtx);
    }

    /**
     * Separated method to call parent issue.
     *
     * @param oAuthAuthzReqMessageContext OAuthAuthzReqMessageContext
     * @return OAuth2AuthorizeRespDTO
     * @throws IdentityOAuth2Exception If an error occurred while issuing the code.
     */
    @Generated(message = "Cannot test super calls")
    OAuth2AuthorizeRespDTO issueCode(
            OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext) throws IdentityOAuth2Exception {

        return super.issue(oAuthAuthzReqMessageContext);
    }

    @Generated(message = "Ignoring because it requires a service call")
    boolean isRegulatory(String clientId) throws RequestObjectException {

        return FinancialServicesUtils.isRegulatoryApp(clientId);
    }
}
