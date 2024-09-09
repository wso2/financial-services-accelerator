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

package org.wso2.financial.services.accelerator.consent.mgt.service.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.OAuth2Service;
import org.wso2.carbon.identity.oauth2.bean.OAuthClientAuthnContext;
import org.wso2.carbon.identity.oauth2.dao.OAuthTokenPersistenceFactory;
import org.wso2.carbon.identity.oauth2.dto.OAuthRevocationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuthRevocationResponseDTO;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.service.internal.ConsentMgtDataHolder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Util class containing utility methods to revoke tokens.
 */
public class TokenRevocationUtil {

    private static final Log log = LogFactory.getLog(TokenRevocationUtil.class);

    public static void revokeTokens(DetailedConsentResource detailedConsentResource, String userID)
            throws IdentityOAuth2Exception {

        OAuth2Service oAuth2Service = getOAuth2Service();
        String clientId = detailedConsentResource.getClientID();
        String consentId = detailedConsentResource.getConsentID();
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(userID);
        Set<AccessTokenDO> accessTokenDOSet = getAccessTokenDOSet(detailedConsentResource, authenticatedUser);

        //TODO : Get consent id claim name from configuration
        String consentIdClaim = "consent_id";

        if (!accessTokenDOSet.isEmpty()) {
            Set<String> activeTokens = new HashSet<>();
            // Get tokens to revoke to an array
            for (AccessTokenDO accessTokenDO : accessTokenDOSet) {
                // Filter tokens by consent ID claim
                if (Arrays.asList(accessTokenDO.getScope()).contains(consentIdClaim +
                        detailedConsentResource.getConsentID())) {
                    activeTokens.add(accessTokenDO.getAccessToken());
                }
            }

            if (!activeTokens.isEmpty()) {
                // set authorization context details for the given user
                OAuthClientAuthnContext oAuthClientAuthnContext = new OAuthClientAuthnContext();
                oAuthClientAuthnContext.setAuthenticated(true);
                oAuthClientAuthnContext.setClientId(clientId);
                oAuthClientAuthnContext.addParameter("IS_CONSENT_REVOCATION_FLOW", true);

                // set common properties of token revocation request
                OAuthRevocationRequestDTO revokeRequestDTO = new OAuthRevocationRequestDTO();
                revokeRequestDTO.setOauthClientAuthnContext(oAuthClientAuthnContext);
                revokeRequestDTO.setConsumerKey(clientId);
                revokeRequestDTO.setTokenType(GrantType.REFRESH_TOKEN.toString());

                for (String activeToken : activeTokens) {
                    // set access token to be revoked
                    revokeRequestDTO.setToken(activeToken);
                    OAuthRevocationResponseDTO oAuthRevocationResponseDTO =
                            revokeTokenByClient(oAuth2Service, revokeRequestDTO);

                    if (oAuthRevocationResponseDTO.isError()) {
                        log.error(String.format("Error while revoking access token for consent ID: %s",
                                consentId.replaceAll("[\r\n]", "")));
                        throw new IdentityOAuth2Exception(
                                String.format("Error while revoking access token for consent ID: %s. Caused by, %s",
                                        consentId, oAuthRevocationResponseDTO.getErrorMsg()));
                    }
                }
            }
        }
    }

    @Generated(message = "Excluded from code coverage since used for testing purposes")
    static OAuth2Service getOAuth2Service() {

        return ConsentMgtDataHolder.getInstance().getOAuth2Service();
    }

    @Generated(message = "Excluded from code coverage since used for testing purposes")
    public static AuthenticatedUser getAuthenticatedUser(String userID) throws IdentityOAuth2Exception {
        // set domain name
        if (UserCoreUtil.getDomainFromThreadLocal() == null) {
            UserCoreUtil.setDomainInThreadLocal(UserCoreUtil.extractDomainFromName(userID));
        }
        // TODO: Set federated user details
        return AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier(userID);
    }

    @Generated(message = "Excluded from code coverage since used for testing purposes")
    public static Set<AccessTokenDO> getAccessTokenDOSet(DetailedConsentResource detailedConsentResource,
                                                  AuthenticatedUser authenticatedUser) throws IdentityOAuth2Exception {

        return OAuthTokenPersistenceFactory.getInstance().getAccessTokenDAO()
                .getAccessTokens(detailedConsentResource.getClientID(), authenticatedUser,
                        authenticatedUser.getUserStoreDomain(), false);
    }

    @Generated(message = "Excluded from code coverage since used for testing purposes")
    public static OAuthRevocationResponseDTO revokeTokenByClient(OAuth2Service oAuth2Service,
                                                          OAuthRevocationRequestDTO revocationRequestDTO) {

        return oAuth2Service.revokeTokenByOAuthClient(revocationRequestDTO);
    }
}
