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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.RefreshGrantHandler;
import org.wso2.financial.services.accelerator.common.constant.ErrorConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonConstants;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonUtils;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityServiceExtensionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * FS specific refresh grant handler.
 */
public class FSRefreshGrantHandler extends RefreshGrantHandler {

    private static final Log log = LogFactory.getLog(FSRefreshGrantHandler.class);
    private static FSGrantHandler fsGrantHandler = IdentityExtensionsDataHolder.getInstance().getObGrantHandler();

    @Override
    public OAuth2AccessTokenRespDTO issue(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {

        try {
            if (FinancialServicesUtils.isRegulatoryApp(tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId())) {
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
        } catch (RequestObjectException e) {
            throw new IdentityOAuth2Exception(e.getMessage());
        } catch (FinancialServicesException e) {
            log.error(ErrorConstants.EXTERNAL_SERVICE_DEFAULT_ERROR, e);
            throw new IdentityOAuth2Exception(ErrorConstants.EXTERNAL_SERVICE_DEFAULT_ERROR);
        }
        return super.issue(tokReqMsgCtx);
    }

    /**
     * Override the default behaviour to set the consent ID scope to the token context
     * before issuing the token since in the default implementation the consent ID scope
     * bound to the token is removed, and it affects the FS custom token flow.
     *
     * @param tokReqMsgCtx
     * @return
     * @throws IdentityOAuth2Exception
     */
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
            String consentIdClaim = IdentityCommonUtils.getConfiguredConsentIdClaimName();
            for (String scope : grantedScopes) {
                if (scope.startsWith(consentIdClaim)) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Adding custom scope %s to the requested scopes",
                                scope.replaceAll("[\r\n]", "")));
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
