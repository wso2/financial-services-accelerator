/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.identity.extensions.claims;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.handlers.claims.JWTAccessTokenClaimProvider;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonConstants;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This claim provider adds FS specific additional claims to the JWT access token.
 * It extracts the consent ID from scopes, adds it as a claim, and removes internal scopes.
 */
public class FSJWTAccessTokenClaimProvider implements JWTAccessTokenClaimProvider {

    private static final Log log = LogFactory.getLog(FSJWTAccessTokenClaimProvider.class);
    private static final String consentIdClaimName = IdentityCommonUtils.getConfiguredConsentIdClaimName();

    @Override
    public Map<String, Object> getAdditionalClaims(OAuthAuthzReqMessageContext context) throws IdentityOAuth2Exception {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getAdditionalClaims(OAuthTokenReqMessageContext context) throws IdentityOAuth2Exception {

        Map<String, Object> additionalClaims = new HashMap<>();

        try {
            if (IdentityCommonUtils.isRegulatoryApp(context.getOauth2AccessTokenReqDTO().getClientId()) &&
                    context != null && context.getScope() != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Processing JWT access token claims. Scopes: " +
                             Arrays.toString(context.getScope()).replaceAll("[\r\n]", ""));
                }
                addConsentIDClaim(context.getScope(), additionalClaims);
                removeConsentIdScope(context.getScope(), additionalClaims);
            }
        } catch (FinancialServicesException e) {
            log.error("Error while adding custom claims to the jwt token", e);
            throw new IdentityOAuth2Exception(e.getMessage(), e);
        }

        return additionalClaims;
    }


    /**
     * This method adds the consent ID claim to the JWT token.
     * @param scopes The scopes array
     * @param claims Claims map to add the consent ID to
     */
    void addConsentIDClaim(String[] scopes, Map<String, Object> claims) {

        String consentID = Arrays.stream(scopes)
                .filter(scope -> scope.contains(IdentityCommonConstants.FS_PREFIX)).findFirst().orElse(null);

        if (StringUtils.isEmpty(consentID)) {
            consentID = Arrays.stream(scopes)
                    .filter(scope -> scope.contains(consentIdClaimName))
                    .findFirst().orElse(StringUtils.EMPTY)
                    .replaceAll(consentIdClaimName, StringUtils.EMPTY);
        } else {
            consentID = consentID.replace(IdentityCommonConstants.FS_PREFIX, StringUtils.EMPTY);
        }

        if (StringUtils.isNotEmpty(consentID)) {
            claims.put(consentIdClaimName, consentID);

            if (log.isDebugEnabled()) {
                log.debug("Added consent ID claim: " + consentIdClaimName.replaceAll("[\r\n]", "") +
                         " : " + consentID.replaceAll("[\r\n]", ""));
            }
        }
    }

    /**
     * Removes the consent ID scope from the JWT token's scopes claim.
     * The consent ID scope was added during the authorization process for internal use.
     * @param scopes The scopes array
     * @param claims Claims map to update the scope claim in
     */
    void removeConsentIdScope(String[] scopes, Map<String, Object> claims) {

        String[] nonInternalScopes = IdentityCommonUtils.removeInternalScopes(scopes);
        String scopeString = StringUtils.join(nonInternalScopes, FinancialServicesConstants.SPACE_SEPARATOR);
        claims.put(FinancialServicesConstants.SCOPE, scopeString);
    }
}
