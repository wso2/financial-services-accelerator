/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.identity.extensions.claims;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeRespDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.openidconnect.ClaimProvider;
import org.wso2.financial.services.accelerator.common.constant.ErrorConstants;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * FS specific claim provider.
 */
public class FSClaimProvider implements ClaimProvider {

    private static final Log log = LogFactory.getLog(FSClaimProvider.class);
    private static ClaimProvider claimProvider;

    @Override
    public Map<String, Object> getAdditionalClaims(OAuthAuthzReqMessageContext authAuthzReqMessageContext,
                                                   OAuth2AuthorizeRespDTO authorizeRespDTO)
            throws IdentityOAuth2Exception {

        Map<String, Object> additionalClaims;

        // Perform FS default behaviour
        try {
            additionalClaims = new HashMap<>(getDefaultAdditionalIdTokenClaims(
                    authAuthzReqMessageContext, authorizeRespDTO));
        } catch (ConsentManagementException e) {
            log.error("Error while getting consent ID claim.", e);
            throw new IdentityOAuth2Exception("Error while getting consent ID claim.", e);
        } catch (JsonProcessingException e) {
            log.error(ErrorConstants.JSON_PROCESSING_ERROR, e);
            throw new IdentityOAuth2Exception(ErrorConstants.JSON_PROCESSING_ERROR, e);
        }

        if (getClaimProvider() != null) {
            // Perform FS customized behaviour
            additionalClaims.putAll(getClaimProvider()
                    .getAdditionalClaims(authAuthzReqMessageContext, authorizeRespDTO));
        }

        return additionalClaims;
    }

    @Override
    public Map<String, Object> getAdditionalClaims(OAuthTokenReqMessageContext tokenReqMessageContext,
                                                   OAuth2AccessTokenRespDTO tokenRespDTO)
            throws IdentityOAuth2Exception {

        // Perform FS default behaviour
        Map<String, Object> additionalClaims = new HashMap<>(getDefaultAdditionalIdTokenClaims(
                tokenReqMessageContext, tokenRespDTO));

        if (getClaimProvider() != null) {
            // Perform FS customized behaviour
            additionalClaims.putAll(getClaimProvider()
                    .getAdditionalClaims(tokenReqMessageContext, tokenRespDTO));
        }

        tokenRespDTO.setAuthorizedScopes(updateScopeInTokenResponseBody(tokenRespDTO.getAuthorizedScopes()));
        return additionalClaims;
    }

    /**
     * Update the scope attribute in the token response body by removing internal scopes.
     *
     * @param scopes
     * @return updated scopes
     */
    private String updateScopeInTokenResponseBody(String scopes) {

        String[] updatedScopesArray = IdentityCommonUtils.removeInternalScopes(scopes.split(" "));

        StringBuilder scopesString = new StringBuilder();
        for (String scope : updatedScopesArray) {
            scopesString.append(scope).append(" ");
        }

        return scopesString.toString().trim();
    }

    private Map<String, Object> getDefaultAdditionalIdTokenClaims(
            OAuthAuthzReqMessageContext authAuthzReqMessageContext, OAuth2AuthorizeRespDTO authorizeRespDTO)
            throws ConsentManagementException, JsonProcessingException {

        Map<String, Object> additionalClaims = new HashMap<>();

        // TODO: read from config
        boolean shouldAddConsentIdClaimToIdToken = true;
        if (shouldAddConsentIdClaimToIdToken) {
            // TODO: read from config
            String consentIdClaimName = "consent_id";
            additionalClaims.put(consentIdClaimName, IdentityCommonUtils.getConsentId(authAuthzReqMessageContext));
        }

        return additionalClaims;
    }

    private Map<String, Object> getDefaultAdditionalIdTokenClaims(
            OAuthTokenReqMessageContext tokenReqMessageContext, OAuth2AccessTokenRespDTO tokenRespDTO) {

        Map<String, Object> additionalClaims = new HashMap<>();

        // TODO: read from config
        boolean shouldAddConsentIdClaimToIdToken = true;
        if (shouldAddConsentIdClaimToIdToken) {
            // TODO: read from config
            String consentIdClaimName = "consent_id";
            additionalClaims.put(consentIdClaimName, IdentityCommonUtils
                    .getConsentId(tokenReqMessageContext.getScope()));
        }

        return additionalClaims;
    }

    public static void setClaimProvider(ClaimProvider claimProvider) {

        FSClaimProvider.claimProvider = claimProvider;
    }

    public static ClaimProvider getClaimProvider() {

        return claimProvider;
    }

}
