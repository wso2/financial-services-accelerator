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

package org.wso2.financial.services.accelerator.identity.extensions.claims;

import com.nimbusds.jwt.JWTClaimsSet;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.openidconnect.DefaultOIDCClaimsCallbackHandler;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonConstants;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This call back handler adds FS specific additional claims to the self-contained JWT access token.
 */
@SuppressFBWarnings(value = {"CRLF_INJECTION_LOGS", "REPLACE_STR_LINE_TERMINATORS"},
        justification = "Log messages are sanitized for CRLF injection.")
public class FSDefaultOIDCClaimsCallbackHandler extends DefaultOIDCClaimsCallbackHandler {

    private static Log log = LogFactory.getLog(FSDefaultOIDCClaimsCallbackHandler.class);

    @Override
    public JWTClaimsSet handleCustomClaims(JWTClaimsSet.Builder jwtClaimsSetBuilder, OAuthTokenReqMessageContext
            tokenReqMessageContext) throws IdentityOAuth2Exception {

        /*
        accessToken property check is done to omit the following claims getting bound to id_token
        The access token property is added to the ID token message context before this method is invoked.
        */
        try {
            if (IdentityCommonUtils.isRegulatoryApp(tokenReqMessageContext.getOauth2AccessTokenReqDTO()
                    .getClientId())
                    && (tokenReqMessageContext.getProperty(IdentityCommonConstants.ACCESS_TOKEN) == null)) {

                Map<String, Object> claimsInJwtToken = new HashMap<>();
                JWTClaimsSet jwtClaimsSet = super.handleCustomClaims(jwtClaimsSetBuilder, tokenReqMessageContext);
                if (jwtClaimsSet != null) {
                    claimsInJwtToken.putAll(jwtClaimsSet.getClaims());
                }

                addConsentIDClaim(tokenReqMessageContext, claimsInJwtToken);

                /*
                Removes the consent ID scope from the JWT token's scopes claim before returning the token.
                The consent ID scope was added during the authorization process in the response type extension
                for internal use.
                */
                removeConsentIdScope(jwtClaimsSetBuilder, claimsInJwtToken);
                return jwtClaimsSetBuilder.build();
            }
        } catch (FinancialServicesException e) {
            log.error("Error while handling custom claims", e);
            throw new IdentityOAuth2Exception(e.getMessage(), e);
        }
        return super.handleCustomClaims(jwtClaimsSetBuilder, tokenReqMessageContext);
    }

    void removeConsentIdScope(JWTClaimsSet.Builder jwtClaimsSetBuilder,
                                             Map<String, Object> claimsInJwtToken) {

        for (Map.Entry<String, Object> claimEntry : claimsInJwtToken.entrySet()) {
            if (FinancialServicesConstants.SCOPE.equals(claimEntry.getKey())) {
                String[] nonInternalScopes = IdentityCommonUtils
                        .removeInternalScopes(claimEntry.getValue().toString()
                                .split(FinancialServicesConstants.SPACE_SEPARATOR));
                jwtClaimsSetBuilder.claim(FinancialServicesConstants.SCOPE, StringUtils.join(nonInternalScopes,
                        FinancialServicesConstants.SPACE_SEPARATOR));
            } else {
                jwtClaimsSetBuilder.claim(claimEntry.getKey(), claimEntry.getValue());
            }
        }
    }

    /**
     * This method adds the consent ID claim to the JWT token.
     * @param tokenReqMessageContext OAuthTokenReqMessageContext
     * @param claimsInJwtToken Claims in the JWT token
     */
    void addConsentIDClaim(OAuthTokenReqMessageContext tokenReqMessageContext,
                                   Map<String, Object> claimsInJwtToken) {

        String consentIdClaimName = IdentityCommonUtils.getConfiguredConsentIdClaimName();
        String consentID = Arrays.stream(tokenReqMessageContext.getScope())
                .filter(scope -> scope.contains(IdentityCommonConstants.FS_PREFIX)).findFirst().orElse(null);
        if (StringUtils.isEmpty(consentID)) {
            consentID = Arrays.stream(tokenReqMessageContext.getScope())
                    .filter(scope -> scope.contains(consentIdClaimName))
                    .findFirst().orElse(StringUtils.EMPTY)
                    .replaceAll(consentIdClaimName, StringUtils.EMPTY);
        } else {
            consentID = consentID.replace(IdentityCommonConstants.FS_PREFIX, StringUtils.EMPTY);
        }

        if (StringUtils.isNotEmpty(consentID)) {
            claimsInJwtToken.put(consentIdClaimName, consentID);
        }
    }
}
