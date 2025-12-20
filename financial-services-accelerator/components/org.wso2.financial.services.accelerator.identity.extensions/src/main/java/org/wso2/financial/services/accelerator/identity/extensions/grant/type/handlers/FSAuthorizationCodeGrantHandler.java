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

package org.wso2.financial.services.accelerator.identity.extensions.grant.type.handlers;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.AuthorizationCodeGrantHandler;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonConstants;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonUtils;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityServiceExtensionUtils;

/**
 * FS specific authorization code grant handler.
 * main usage of extending is to handle the refresh token issuance and setting the refresh token validity period.
 */
@SuppressFBWarnings(value = {"CRLF_INJECTION_LOGS", "REPLACE_STR_LINE_TERMINATORS"},
        justification = "Log messages are sanitized for CRLF injection.")
public class FSAuthorizationCodeGrantHandler extends AuthorizationCodeGrantHandler {

    private static final Log log = LogFactory.getLog(FSAuthorizationCodeGrantHandler.class);
    private FSGrantHandler fsGrantHandler = IdentityExtensionsDataHolder.getInstance().getObGrantHandler();

    @Override
    public OAuth2AccessTokenRespDTO issue(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {

        try {
            if (IdentityCommonUtils.isRegulatoryApp(tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId())) {
                boolean issueRefreshToken = true;
                if (ServiceExtensionUtils.isInvokeExternalService(
                        ServiceExtensionTypeEnum.ISSUE_REFRESH_TOKEN)) {
                    // Perform FS customized behaviour with service extension
                    issueRefreshToken = IdentityServiceExtensionUtils
                            .issueRefreshTokenWithServiceExtension(tokReqMsgCtx);
                } else if (fsGrantHandler != null) {
                    // Perform FS customized behaviour
                    issueRefreshToken = fsGrantHandler.issueRefreshToken(tokReqMsgCtx);
                }

                tokReqMsgCtx.addProperty(IdentityCommonConstants.ISSUE_REFRESH_TOKEN, issueRefreshToken);
                OAuth2AccessTokenRespDTO oAuth2AccessTokenRespDTO = super.issue(tokReqMsgCtx);
                IdentityCommonUtils.addConsentIdToTokenResponse(oAuth2AccessTokenRespDTO);
                return oAuth2AccessTokenRespDTO;
            }
        } catch (FinancialServicesException e) {
            log.error(e.getMessage().replaceAll("[\r\n]", ""), e);
            throw new IdentityOAuth2Exception(e.getMessage());
        }
        return super.issue(tokReqMsgCtx);
    }

    /**
     * Override the issueRefreshToken method to handle the refresh token issuance.
     *
     * @return true if refresh token is issued, false otherwise.
     */
    @Override
    public boolean issueRefreshToken() throws IdentityOAuth2Exception {

        OAuthTokenReqMessageContext tokenReqMessageContext = OAuth2Util.getTokenRequestContext();

        try {
            if (IdentityCommonUtils.isRegulatoryApp(tokenReqMessageContext.getOauth2AccessTokenReqDTO()
                    .getClientId())) {
                if (ServiceExtensionUtils.isInvokeExternalService(ServiceExtensionTypeEnum
                        .ISSUE_REFRESH_TOKEN) || fsGrantHandler != null) {
                    // Perform FS customized behaviour
                    return (Boolean) tokenReqMessageContext.getProperty(IdentityCommonConstants.ISSUE_REFRESH_TOKEN);
                } else {
                    // Perform FS default behaviour
                    return super.issueRefreshToken();
                }
            }
        } catch (FinancialServicesException e) {
            throw new IdentityOAuth2Exception("Error occurred while getting sp property from sp meta data");
        }
        return super.issueRefreshToken();
    }
}
