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

package com.wso2.openbanking.accelerator.identity.idtoken;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonHelper;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeRespDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.openidconnect.DefaultIDTokenBuilder;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * OB specific IDToken builder.
 */
public class OBIDTokenBuilder extends DefaultIDTokenBuilder {

    private static final Log log = LogFactory.getLog(OBIDTokenBuilder.class);

    public OBIDTokenBuilder() throws IdentityOAuth2Exception {

    }

    Map<String, Object> identityConfigurations = IdentityExtensionsDataHolder.getInstance().getConfigurationMap();
    Object ppidProperty = identityConfigurations.get(IdentityCommonConstants.ENABLE_SUBJECT_AS_PPID);
    Object removeTenantDomainConfig = identityConfigurations.
            get(IdentityCommonConstants.REMOVE_TENANT_DOMAIN_FROM_SUBJECT);
    Boolean removeTenantDomain = removeTenantDomainConfig != null
            && Boolean.parseBoolean(removeTenantDomainConfig.toString());
    Object removeUserStoreDomainConfig = identityConfigurations.
            get(IdentityCommonConstants.REMOVE_USER_STORE_DOMAIN_FROM_SUBJECT);
    Boolean removeUserStoreDomain = removeUserStoreDomainConfig != null
            && Boolean.parseBoolean(removeUserStoreDomainConfig.toString());

    // method to set the subject claim in id token returned in authorization as a pairwise pseudonymous ID
    @Override
    protected String getSubjectClaim(OAuthAuthzReqMessageContext authzReqMessageContext,
                                     OAuth2AuthorizeRespDTO authorizeRespDTO,
                                     String clientId,
                                     String spTenantDomain,
                                     AuthenticatedUser authorizedUser) throws IdentityOAuth2Exception {

        String callBackUri = authzReqMessageContext.getAuthorizationReqDTO().getCallbackUrl();
        String userId = StringUtils.EMPTY;
        String subject = StringUtils.EMPTY;
        String sectorIdentifierUri = null;
        boolean setSubjectAsPPID = false;
        if (ppidProperty != null) {
            setSubjectAsPPID = Boolean.parseBoolean(ppidProperty.toString());
        }
        try {
            // for non regulatory scenarios, need to return the user id as the subject
            if (!IdentityCommonUtil.getRegulatoryFromSPMetaData(clientId)) {
                return super.getSubjectClaim(authzReqMessageContext, authorizeRespDTO, clientId, spTenantDomain,
                        authorizedUser);
            }
            sectorIdentifierUri = getSectorIdentifierUri(clientId);
        } catch (OpenBankingException e) {
            log.error("Error occurred while retrieving service provider data", e);
            throw new IdentityOAuth2Exception("Error occurred while retrieving service provider data");
        }
        if (setSubjectAsPPID) {
            if (authzReqMessageContext.getAuthorizationReqDTO().getUser() != null) {
                userId = authzReqMessageContext.getAuthorizationReqDTO().getUser()
                        .getUsernameAsSubjectIdentifier(false, false);
            }
            subject = getSubjectClaimValue(sectorIdentifierUri, userId, callBackUri);
            if (StringUtils.isNotBlank(subject)) {
                return subject;
            } else {
                log.error("Subject claim cannot be empty");
                throw new IdentityOAuth2Exception("Subject claim cannot be empty");
            }
        } else if (removeTenantDomain || removeUserStoreDomain) {
            /* Update the subject claim of the JWT claims set if any of the following configurations are true
            and if PPID is as the subject claim is not enabled.
                1. open_banking.identity.token.remove_user_store_domain_from_subject
                2. open_banking.identity.token.remove_tenant_domain_from_subject */
           return authorizedUser.getUsernameAsSubjectIdentifier(!removeUserStoreDomain, !removeTenantDomain);
        } else {
            return MultitenantUtils.getTenantAwareUsername(super.getSubjectClaim(authzReqMessageContext,
                    authorizeRespDTO, clientId, spTenantDomain, authorizedUser));
        }
    }

    // method to set the subject claim in Id token returned in token response
    @Override
    protected String getSubjectClaim(OAuthTokenReqMessageContext tokenReqMessageContext,
                                     OAuth2AccessTokenRespDTO tokenRespDTO,
                                     String clientId,
                                     String spTenantDomain,
                                     AuthenticatedUser authorizedUser) throws IdentityOAuth2Exception {

        String callBackUri = tokenRespDTO.getCallbackURI();
        String userId = StringUtils.EMPTY;
        String subject = StringUtils.EMPTY;
        String sectorIdentifierUri = null;
        boolean setSubjectAsPPID = false;
        if (ppidProperty != null) {
            setSubjectAsPPID = Boolean.parseBoolean(ppidProperty.toString());
        }
        try {
            // for non regulatory scenarios, need to return the user id as the subject
            if (!IdentityCommonUtil.getRegulatoryFromSPMetaData(clientId)) {
                return super.getSubjectClaim(tokenReqMessageContext, tokenRespDTO, clientId, spTenantDomain,
                        authorizedUser);
            }
            sectorIdentifierUri = getSectorIdentifierUri(clientId);
        } catch (OpenBankingException e) {
            log.error("Error occurred while retrieving service provider data", e);
            throw new IdentityOAuth2Exception("Error occurred while retrieving service provider data");
        }
        if (setSubjectAsPPID) {
            if (tokenReqMessageContext.getAuthorizedUser() != null) {
                userId = tokenReqMessageContext.getAuthorizedUser()
                        .getUsernameAsSubjectIdentifier(false, false);
            }
            subject = getSubjectClaimValue(sectorIdentifierUri, userId, callBackUri);
            if (StringUtils.isNotBlank(subject)) {
                return subject;
            } else {
                log.error("Subject claim cannot be empty");
                throw new IdentityOAuth2Exception("Subject claim cannot be empty");
            }
        } else if (removeTenantDomain || removeUserStoreDomain) {
            /* Update the subject claim of the JWT claims set if any of the following configurations are true
            and if PPID is as the subject claim is not enabled.
                1. open_banking.identity.token.remove_user_store_domain_from_subject
                2. open_banking.identity.token.remove_tenant_domain_from_subject */
            return authorizedUser.getUsernameAsSubjectIdentifier(!removeUserStoreDomain, !removeTenantDomain);
        } else {
            return MultitenantUtils.getTenantAwareUsername(super.getSubjectClaim(tokenReqMessageContext,
                    tokenRespDTO, clientId, spTenantDomain, authorizedUser));
        }
    }

    /**
     * Get the subject claim as a UUID with userId and call back uri host name as seed.
     *
     * @param callBackUri redirect uri of the data recipient
     * @param userID      user identification of the consumer
     * @return
     */
    private String getSubjectFromCallBackUris(String callBackUri, String userID) {

        List<String> uris = unwrapURIString(callBackUri);

        if (!uris.isEmpty()) {
            URI uri;
            try {
                // assuming all URIs have the same hostname, we just take the first URI
                uri = new URI(uris.get(0));
            } catch (URISyntaxException e) {
                log.error("Error while retrieving the host name of the redirect url ", e);
                return StringUtils.EMPTY;
            }
            String hostname = uri.getHost();
            String seed = hostname.concat(userID);
            return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8)).toString();
        }
        log.error("Redirect URIs cannot be empty");
        return StringUtils.EMPTY;
    }

    /**
     * Get the subject claim as a UUID with userId and sector identifier uri host name as seed.
     *
     * @param sectorIdentifierUri sector identifier uri of the data recipient
     * @param userID              user identification of the consumer
     * @return
     */
    private String getSubjectFromSectorIdentifierUri(String sectorIdentifierUri, String userID) {

        URI uri;
        try {
            // assuming all URIs have the same hostname, we just take the first URI
            uri = new URI(sectorIdentifierUri);
        } catch (URISyntaxException e) {
            log.error("Error while retrieving the host name of the redirect url ", e);
            return StringUtils.EMPTY;
        }
        String hostname = uri.getHost();
        String seed = hostname.concat(userID);
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8)).toString();
    }

    /**
     * Given a string in regex format, unwrap and create list of seperate URIs.
     *
     * @param uriString The joined URIs in string format.
     * @return List of URIs.
     */
    private List<String> unwrapURIString(String uriString) {

        Pattern outerPattern = Pattern.compile("regexp=\\((.*?)\\)");
        Pattern innerPattern = Pattern.compile("\\^(.*?)\\$");

        Matcher matcher = outerPattern.matcher(uriString);

        String delimitedUri;

        if (matcher.find()) {
            // remove regex= part
            delimitedUri = matcher.group(1);
        } else {
            // URI is not having regex format
            return Collections.singletonList(uriString);
        }

        String[] uris = delimitedUri.split("\\|");

        // remove ^..$ part
        return Arrays.stream(uris)
                .map(uri -> {
                    Matcher m = innerPattern.matcher(uri);
                    if (m.find()) {
                        return m.group(1);
                    } else {
                        return uri;
                    }
                }).collect(Collectors.toList());
    }

    /**
     * Get the sector identifier uri from sp metadata.
     *
     * @param clientId consumer id
     * @return
     */
    @Generated(message = "Excluding from code coverage since it requires a service call")
    protected String getSectorIdentifierUri(String clientId) throws OpenBankingException {

        return (new IdentityCommonHelper()).getAppPropertyFromSPMetaData(clientId, "sector_identifier_uri");
    }

    /**
     * Validate and get subject value.
     *
     * @param sectorIdentifierUri sector identifier uri
     * @param userId              user id
     * @param callBackUri         call back uris
     * @return subject value if validated or return empty string
     */
    private String getSubjectClaimValue(String sectorIdentifierUri, String userId, String callBackUri) {

        if (StringUtils.isNotBlank(sectorIdentifierUri) && StringUtils.isNotBlank(userId)) {
            log.debug("Calculating subject claim using sector identifier uri ");
            return getSubjectFromSectorIdentifierUri(sectorIdentifierUri, userId);
        } else if (StringUtils.isNotBlank(callBackUri) && StringUtils.isNotBlank(userId)) {
            log.debug("Calculating subject claim using redirect uris ");
            return getSubjectFromCallBackUris(callBackUri, userId);
        }
        return StringUtils.EMPTY;
    }
}
