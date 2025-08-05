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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.authz.handlers.CodeResponseTypeHandler;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeRespDTO;
import org.wso2.financial.services.accelerator.common.constant.ErrorConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.util.Generated;
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
     * Extension point to get updated scopes.
     *
     * @param oauthAuthzMsgCtx OAuthAuthzReqMessageContext
     * @return OAuth2AuthorizeRespDTO
     * @throws IdentityOAuth2Exception If an error occurred while issuing the code.
     */
    @Override
    @Generated(message = "Ignoring since main logics in util methods are tested")
    public OAuth2AuthorizeRespDTO issue(OAuthAuthzReqMessageContext oauthAuthzMsgCtx) throws IdentityOAuth2Exception {

        try {
            if (!IdentityCommonUtils.isRegulatoryApp(oauthAuthzMsgCtx.getAuthorizationReqDTO().getConsumerKey())) {
                return issueCode(oauthAuthzMsgCtx);
            }

            String[] updatedApprovedScopes;
            if (fsResponseTypeHandler != null) {

                // Perform FS customized behaviour
                updatedApprovedScopes = fsResponseTypeHandler.getApprovedScopes(oauthAuthzMsgCtx);
            } else {

                // Perform FS default behaviour
                updatedApprovedScopes = IdentityCommonUtils.updateApprovedScopes(oauthAuthzMsgCtx);
            }

            if (updatedApprovedScopes != null) {
                oauthAuthzMsgCtx.setApprovedScope(updatedApprovedScopes);
            } else {
                throw new IdentityOAuth2Exception("Error while updating scopes");
            }
            return issueCode(oauthAuthzMsgCtx);
        } catch (FinancialServicesException e) {
            log.error(e.getMessage().replaceAll("[\r\n]", ""), e);
            throw new IdentityOAuth2Exception(e.getMessage());
        } catch (JsonProcessingException e) {
            log.error(ErrorConstants.JSON_PROCESSING_ERROR, e);
            throw new IdentityOAuth2Exception(ErrorConstants.JSON_PROCESSING_ERROR);
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
}
