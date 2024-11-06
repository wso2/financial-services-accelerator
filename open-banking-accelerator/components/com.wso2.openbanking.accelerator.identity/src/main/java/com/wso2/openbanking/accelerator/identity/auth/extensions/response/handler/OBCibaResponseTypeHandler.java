/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth.ciba.dao.CibaDAOFactory;
import org.wso2.carbon.identity.oauth.ciba.exceptions.CibaCoreException;
import org.wso2.carbon.identity.oauth.ciba.handlers.CibaResponseTypeHandler;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeRespDTO;

import java.util.ArrayList;

/**
 * Handles authorize requests with CibaAuthCode as response type.
 */
public class OBCibaResponseTypeHandler extends CibaResponseTypeHandler {

    @Override
    public OAuth2AuthorizeRespDTO issue(OAuthAuthzReqMessageContext oauthAuthzMsgCtx) throws IdentityOAuth2Exception {

        OAuth2AuthorizeReqDTO authorizationReqDTO = oauthAuthzMsgCtx.getAuthorizationReqDTO();
        try {
            // Assigning authenticated user for the request that to be persisted.
            AuthenticatedUser cibaAuthenticatedUser = authorizationReqDTO.getUser();
            // Assigning the authentication status that to be persisted.

            ArrayList<String> consentIds = IdentityExtensionsDataHolder.getInstance().getConsentCoreService()
                    .getConsentIdByConsentAttributeNameAndValue(OpenBankingConstants.AUTH_REQ_ID,
                            authorizationReqDTO.getNonce());

            if (!consentIds.isEmpty()) {
                if (IdentityExtensionsDataHolder.getInstance().getConsentCoreService().getDetailedConsent(
                        consentIds.get(0)).getCurrentStatus().equals(OpenBankingConstants.AUTHORISED_STATUS)) {
                    // Update successful authentication.
                    String authCodeKey = CibaDAOFactory.getInstance().getCibaAuthMgtDAO()
                            .getCibaAuthCodeKey(authorizationReqDTO.getNonce());
                    CibaDAOFactory.getInstance().getCibaAuthMgtDAO()
                            .persistAuthenticationSuccess(authCodeKey, cibaAuthenticatedUser);
                }
            }
            String callbackURL = OpenBankingConfigParser.getInstance().getCibaAuthenticationRedirectEndpoint();
            if (StringUtils.isNotEmpty(callbackURL)) {
                OAuth2AuthorizeRespDTO respDTO = new OAuth2AuthorizeRespDTO();
                respDTO.setCallbackURI(callbackURL);
                return respDTO;
            } else {
                throw new IdentityOAuth2Exception("Error occurred while retrieving CIBA redirect endpoint.");
            }
        } catch (CibaCoreException e) {
            throw new IdentityOAuth2Exception("Error occurred in persisting authenticated user and authentication " +
                    "status for the request made by client: " + authorizationReqDTO.getConsumerKey(), e);
        } catch (ConsentManagementException e) {
            throw new IdentityOAuth2Exception("Error occurred in retrieving auth_req_id ", e);
        }
    }
}
