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
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonUtils;

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

            // Perform FS default behaviour
            String sessionDataKey = oauthAuthzMsgCtx.getAuthorizationReqDTO().getSessionDataKey();
            String consentId = IdentityCommonUtils.getConsentIDFromSessionData(sessionDataKey);
            String[] updatedApprovedScopes = IdentityCommonUtils.updateApprovedScopes(oauthAuthzMsgCtx, consentId);
            long refreshTokenValidityPeriod = oauthAuthzMsgCtx.getRefreshTokenvalidityPeriod();

            if (ServiceExtensionUtils.isInvokeExternalService(ServiceExtensionTypeEnum.POST_USER_AUTHORIZATION)) {
                // Perform FS customized behaviour with service extension
                updatedApprovedScopes = IdentityCommonUtils
                        .getApprovedScopesWithServiceExtension(oauthAuthzMsgCtx, consentId);
            } else if (fsResponseTypeHandler != null) {
                // Perform FS customized behaviour
                updatedApprovedScopes = fsResponseTypeHandler.getApprovedScopes(oauthAuthzMsgCtx);
            }

            if (ServiceExtensionUtils.isInvokeExternalService(ServiceExtensionTypeEnum.POST_USER_AUTHORIZATION)) {
                // Perform FS customized behaviour with service extension
                refreshTokenValidityPeriod = IdentityCommonUtils
                        .getRefreshTokenValidityPeriodWithServiceExtension(oauthAuthzMsgCtx, consentId);
            } else if (fsResponseTypeHandler != null) {
                // Perform FS customized behaviour
                refreshTokenValidityPeriod = fsResponseTypeHandler.getRefreshTokenValidityPeriod(oauthAuthzMsgCtx);
            }

            oauthAuthzMsgCtx.setRefreshTokenvalidityPeriod(refreshTokenValidityPeriod);
            if (updatedApprovedScopes != null) {
                oauthAuthzMsgCtx.setApprovedScope(updatedApprovedScopes);
            } else {
                throw new IdentityOAuth2Exception("Error while updating scopes");
            }
            return issueCode(oauthAuthzMsgCtx);
        } catch (RequestObjectException e) {
            throw  new IdentityOAuth2Exception("Error while reading regulatory property");
        } catch (FinancialServicesException e) {
            log.error("Error while invoking external service extension", e);
            throw new IdentityOAuth2Exception("Error while invoking external service extension");
        }
    }

    /**
     * Separated method to call parent issue.
     *
     * @param oAuthAuthzReqMessageContext OAuthAuthzReqMessageContext
     * @return OAuth2AuthorizeRespDTO
     * @throws IdentityOAuth2Exception If an error occurred while issuing the code.
     */
    @Generated(message = "Cannot test super calls")
    OAuth2AuthorizeRespDTO issueCode(OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext)
            throws IdentityOAuth2Exception {

        return super.issue(oAuthAuthzReqMessageContext);
    }

    @Generated(message = "Ignoring because it requires a service call")
    boolean isRegulatory(String clientId) throws RequestObjectException {

        return FinancialServicesUtils.isRegulatoryApp(clientId);
    }
}
