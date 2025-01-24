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

import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jose.util.X509CertUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.carbon.identity.oauth2.model.HttpRequestHeader;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.openidconnect.DefaultOIDCClaimsCallbackHandler;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonConstants;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonUtils;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This call back handler adds ob specific additional claims to self contained JWT access token.
 */
public class FSDefaultOIDCClaimsCallbackHandler extends DefaultOIDCClaimsCallbackHandler {

    private static Log log = LogFactory.getLog(FSDefaultOIDCClaimsCallbackHandler.class);
    Map<String, Object> identityConfigurations = IdentityExtensionsDataHolder.getInstance().getConfigurationMap();

    @Override
    public JWTClaimsSet handleCustomClaims(JWTClaimsSet.Builder jwtClaimsSetBuilder, OAuthTokenReqMessageContext
            tokenReqMessageContext) throws IdentityOAuth2Exception {

        /*  accessToken property check is done to omit the following claims getting bound to id_token
             The access token property is added to the ID token message context before this method is invoked. */
        try {
            if (FinancialServicesUtils.isFapiConformantApp(tokenReqMessageContext.getOauth2AccessTokenReqDTO()
                    .getClientId())
                    && (tokenReqMessageContext.getProperty(IdentityCommonConstants.ACCESS_TOKEN) == null)) {

                Map<String, Object> userClaimsInOIDCDialect = new HashMap<>();
                JWTClaimsSet jwtClaimsSet = getJwtClaimsFromSuperClass(jwtClaimsSetBuilder, tokenReqMessageContext);
                if (jwtClaimsSet != null) {
                    userClaimsInOIDCDialect.putAll(jwtClaimsSet.getClaims());
                }
                addCnfClaimToOIDCDialect(tokenReqMessageContext, userClaimsInOIDCDialect);
                addConsentIDClaimToOIDCDialect(tokenReqMessageContext, userClaimsInOIDCDialect);
                updateSubClaim(tokenReqMessageContext, userClaimsInOIDCDialect);

                for (Map.Entry<String, Object> claimEntry : userClaimsInOIDCDialect.entrySet()) {
                    if (IdentityCommonConstants.SCOPE.equals(claimEntry.getKey())) {
                        String[] nonInternalScopes = IdentityCommonUtils
                                .removeInternalScopes(claimEntry.getValue().toString()
                                        .split(IdentityCommonConstants.SPACE_SEPARATOR));
                        jwtClaimsSetBuilder.claim(IdentityCommonConstants.SCOPE, StringUtils.join(nonInternalScopes,
                                IdentityCommonConstants.SPACE_SEPARATOR));
                    } else {
                        jwtClaimsSetBuilder.claim(claimEntry.getKey(), claimEntry.getValue());
                    }
                }
                return jwtClaimsSetBuilder.build();
            }
        } catch (RequestObjectException e) {
            log.error("Error while handling custom claims", e);
            throw new IdentityOAuth2Exception(e.getMessage(), e);
        }
        return super.handleCustomClaims(jwtClaimsSetBuilder, tokenReqMessageContext);
    }

    @Generated(message = "Excluding from code coverage since it makes is used to return claims from the super class")
    public JWTClaimsSet getJwtClaimsFromSuperClass(JWTClaimsSet.Builder jwtClaimsSetBuilder,
                                                   OAuthTokenReqMessageContext tokenReqMessageContext)
            throws IdentityOAuth2Exception {

        return super.handleCustomClaims(jwtClaimsSetBuilder, tokenReqMessageContext);
    }

    private void addCnfClaimToOIDCDialect(OAuthTokenReqMessageContext tokenReqMessageContext,
                                          Map<String, Object> userClaimsInOIDCDialect) {
        Base64URL certThumbprint;
        X509Certificate certificate;
        String headerName = IdentityCommonUtils.getMTLSAuthHeader();

        HttpRequestHeader[] requestHeaders = tokenReqMessageContext.getOauth2AccessTokenReqDTO()
                .getHttpRequestHeaders();
        Optional<HttpRequestHeader> certHeader =
                Arrays.stream(requestHeaders).filter(h -> headerName.equals(h.getName())).findFirst();
        if (certHeader.isPresent()) {
            try {
                certificate = IdentityCommonUtils.parseCertificate(certHeader.get().getValue()[0]);
                certThumbprint = X509CertUtils.computeSHA256Thumbprint(certificate);
                userClaimsInOIDCDialect.put(IdentityCommonConstants.CNF_CLAIM,
                        Collections.singletonMap("x5t#S256", certThumbprint));
            } catch (FinancialServicesException e) {
                log.error("Error while extracting the certificate", e);
            }
        }
    }

    private void addConsentIDClaimToOIDCDialect(OAuthTokenReqMessageContext tokenReqMessageContext,
                                                Map<String, Object> userClaimsInOIDCDialect) {

        String consentIdClaimName =
                identityConfigurations.get(FinancialServicesConstants.CONSENT_ID_CLAIM_NAME).toString();
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
            userClaimsInOIDCDialect.put(consentIdClaimName, consentID);
        }
    }

    /**
     * Update the subject claim of the JWT claims set if any of the following configurations are true
     *  1. Remove tenant domain from subject (fs.identity.token.remove_tenant_domain_from_subject)
     *  2. Remove user store domain from subject (fs.identity.token.remove_user_store_domain_from_subject)
     * @param tokenReqMessageContext token request message context
     * @param userClaimsInOIDCDialect user claims in OIDC dialect as a map
     */
    private void updateSubClaim(OAuthTokenReqMessageContext tokenReqMessageContext,
                                Map<String, Object> userClaimsInOIDCDialect) {

        Object removeTenantDomainConfig =
                identityConfigurations.get(FinancialServicesConstants.REMOVE_TENANT_DOMAIN_FROM_SUBJECT);
        Boolean removeTenantDomain = removeTenantDomainConfig != null
                && Boolean.parseBoolean(removeTenantDomainConfig.toString());

        Object removeUserStoreDomainConfig =
                identityConfigurations.get(FinancialServicesConstants.REMOVE_USER_STORE_DOMAIN_FROM_SUBJECT);
        Boolean removeUserStoreDomain = removeUserStoreDomainConfig != null
                && Boolean.parseBoolean(removeUserStoreDomainConfig.toString());

        if (removeTenantDomain || removeUserStoreDomain) {
            String subClaim = tokenReqMessageContext.getAuthorizedUser()
                    .getUsernameAsSubjectIdentifier(!removeUserStoreDomain, !removeTenantDomain);
            userClaimsInOIDCDialect.put(IdentityCommonConstants.SUBJECT_CLAIM, subClaim);
        }
    }
}
