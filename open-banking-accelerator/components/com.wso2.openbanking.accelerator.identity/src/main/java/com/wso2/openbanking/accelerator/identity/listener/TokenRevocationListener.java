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
package com.wso2.openbanking.accelerator.identity.listener;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth.event.AbstractOAuthEventInterceptor;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuthRevocationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuthRevocationResponseDTO;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.identity.oauth2.model.RefreshTokenValidationDataDO;

import java.util.Map;

/**
 * Event listener to revoke consents when access token is revoked.
 */
public class TokenRevocationListener extends AbstractOAuthEventInterceptor {

    private static final Log log = LogFactory.getLog(TokenRevocationListener.class);
    private static final ConsentCoreServiceImpl consentCoreService = new ConsentCoreServiceImpl();

    /**
     * Revoke the consent bound to the access token after revoking the access token.
     *
     * @param revokeRequestDTO
     * @param revokeResponseDTO
     * @param accessTokenDO
     * @param refreshTokenDO
     * @param params
     * @throws IdentityOAuth2Exception
     */
    @Override
    @Generated(message = "Excluding from code coverage since it requires a service call")
    public void onPostTokenRevocationByClient(OAuthRevocationRequestDTO revokeRequestDTO,
                                              OAuthRevocationResponseDTO revokeResponseDTO,
                                              AccessTokenDO accessTokenDO,
                                              RefreshTokenValidationDataDO refreshTokenDO,
                                              Map<String, Object> params) throws IdentityOAuth2Exception {

        if (!revokeRequestDTO.getoAuthClientAuthnContext().isAuthenticated()) {
            return;
        }
        String consentId = "";
        if (accessTokenDO != null) {
            consentId = getConsentIdFromScopes(accessTokenDO.getScope());
        } else if (refreshTokenDO != null) {
            consentId = getConsentIdFromScopes(refreshTokenDO.getScope());
        }
        if (StringUtils.isNotEmpty(consentId)) {
            try {
                // Skip consent revocation if the request is from consent revocation flow.
                boolean isConsentRevocationFlow = revokeRequestDTO.getoAuthClientAuthnContext().getParameters().
                        containsKey(OpenBankingConstants.IS_CONSENT_REVOCATION_FLOW) && (boolean) revokeRequestDTO.
                        getoAuthClientAuthnContext().getParameter(OpenBankingConstants.IS_CONSENT_REVOCATION_FLOW);
                if (!isConsentRevocationFlow) {
                    consentCoreService.revokeConsentWithReason(consentId, OpenBankingConstants.
                            DEFAULT_STATUS_FOR_REVOKED_CONSENTS, null, false, "Revoked by token revocation");
                }
            } catch (ConsentManagementException e) {
                log.error(String.format("Error occurred while revoking consent on token revocation. %s",
                        e.getMessage().replaceAll("[\r\n]", "")));
            }
        }
    }

    /**
     * Return consent-id when a string array of scopes is given.
     *
     * @param scopes
     * @return
     */
    public String getConsentIdFromScopes(String[] scopes) {

        String consentIdClaim = OpenBankingConfigParser.getInstance().getConfiguration()
                .get(OpenBankingConstants.CONSENT_ID_CLAIM_NAME).toString();
        if (scopes != null) {
            for (String scope : scopes) {
                if (scope.contains(consentIdClaim)) {
                    return scope.split(consentIdClaim)[1];
                }
            }
        }
        return null;
    }
}
