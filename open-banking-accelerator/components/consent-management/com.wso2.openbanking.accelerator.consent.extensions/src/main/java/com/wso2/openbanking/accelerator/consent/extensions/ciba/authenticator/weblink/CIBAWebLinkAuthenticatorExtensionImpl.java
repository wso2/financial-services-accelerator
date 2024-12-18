/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */
package com.wso2.openbanking.accelerator.consent.extensions.ciba.authenticator.weblink;

import com.nimbusds.jwt.SignedJWT;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.CarbonUtils;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

/**
 * This class represents the implementation of CIBA web link authenticator extensions.
 */
public class CIBAWebLinkAuthenticatorExtensionImpl implements CIBAWebLinkAuthenticatorExtensionInterface {

    private static final Log log = LogFactory.getLog(CIBAWebLinkAuthenticatorExtensionImpl.class);
    private static final ConsentCoreService consentCoreService =
            ConsentExtensionsDataHolder.getInstance().getConsentCoreService();
    public static final String LOGIN_HINT = "login_hint";

    /**
     * Method to identify the user/users involved in the authentication.
     *
     * @param request HttpServletRequest
     * @return list of users
     */
    @Override
    public List<AuthenticatedUser> getAuthenticatedUsers(HttpServletRequest request) {

        return Arrays.stream(request.getParameter(LOGIN_HINT)
                        .split(","))
                .map(String::trim)
                .map(AuthenticatedUser::createLocalAuthenticatedUserFromSubjectIdentifier)
                .collect(Collectors.toList());
    }

    /**
     * Method to generate web auth links for given user.
     *
     * @param context authentication context.
     * @param user    authenticated user.
     * @return Auth web link for authenticated user.
     */
    @Override
    public String generateWebAuthLink(AuthenticationContext context, AuthenticatedUser user) {
        List<String> allowedParams = OpenBankingConfigParser.getInstance().getCibaWebLinkAllowedParams();
        List<String> paramList = Arrays.stream(context.getQueryParams().split("&")).filter(e -> {
            for (String allowedParam : allowedParams) {
                if (e.startsWith(allowedParam)) {
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList());

        // Rename `request_object` query params to `request` param.
        List<String> requestObjectList = Arrays.stream(context.getQueryParams().split("&"))
                .filter(e -> e.startsWith("request_object")).collect(Collectors.toList());
        String requestObject = requestObjectList.get(0).split("=")[1];
        paramList.add("request=" + requestObject);
        paramList.add(OpenBankingConstants.CIBA_WEB_AUTH_LINK_PARAM + "=true");
        paramList.add("login_hint=" + user.getUserName());

        StringBuilder builder = new StringBuilder();
        builder.append(CarbonUtils.getCarbonServerUrl()).append(CIBAWebLinkAuthenticatorConstants.AUTHORIZE_URL_PATH);
        for (String param : paramList) {
            builder.append(param).append("&");
        }
        if (log.isDebugEnabled()) {
            log.debug(builder.toString());
        }
        return builder.toString();
    }

    /**
     * Method to create authorisation resources for given users.
     *
     * @param authenticatedUsers list of users involved for the authorisation
     * @param context            authenticationContext
     */
    public void createAuthResourcesForUsers(List<AuthenticatedUser> authenticatedUsers,
                                            AuthenticationContext context) throws AuthenticationFailedException {

        try {
            // Extract consentId
            Optional<String> requestObject = Arrays.stream(context.getQueryParams().split("&"))
                    .filter(e -> e.startsWith(CIBAWebLinkAuthenticatorConstants.REQUEST_OBJECT)).findFirst()
                    .map(e -> e.split("=")[1]);

            if (requestObject.isPresent()) {
                SignedJWT signedJWT = SignedJWT.parse(requestObject.get());
                JSONObject claims = (JSONObject) signedJWT.getJWTClaimsSet()
                        .getClaim(CIBAWebLinkAuthenticatorConstants.CLAIMS);
                JSONObject userinfo = (JSONObject) claims.get(CIBAWebLinkAuthenticatorConstants.USER_INFO);
                JSONObject openBankingIntentId =
                        (JSONObject) userinfo.get(CIBAWebLinkAuthenticatorConstants.OPEN_BANKING_INTENT_ID);
                String consentId = (String) openBankingIntentId.get(CIBAWebLinkAuthenticatorConstants.VALUE);

                // Extract usernames
                List<String> usernames = authenticatedUsers.stream()
                        .map(AuthenticatedUser::getUserName)
                        .map(username -> username.endsWith(OpenBankingConstants.CARBON_SUPER_TENANT_DOMAIN) ?
                                username : username + OpenBankingConstants.CARBON_SUPER_TENANT_DOMAIN)
                        .collect(Collectors.toList());

                List<AuthorizationResource> consentAuthResources =
                        consentCoreService.searchAuthorizations(consentId);
                if (consentAuthResources.size() == 1 && consentAuthResources.get(0).getAuthorizationStatus()
                        .equals(OpenBankingConstants.CREATED_AUTHORISATION_RESOURCE_STATE)) {
                    // Scenario : Initiated consent with default auth resources binding.
                    consentCoreService.updateAuthorizationUser(consentAuthResources.get(0).getAuthorizationID(),
                            usernames.get(0));
                    for (int i = 1; i < usernames.size(); i++) {
                        AuthorizationResource userAuthResource = new AuthorizationResource(consentId,
                                usernames.get(i), OpenBankingConstants.CREATED_AUTHORISATION_RESOURCE_STATE,
                                OpenBankingConstants.MULTI_AUTH_AUTHORISATION_TYPE, System.currentTimeMillis());
                        consentCoreService.createConsentAuthorization(userAuthResource);
                    }
                } else if (consentAuthResources.size() == authenticatedUsers.size()) {
                    for (AuthorizationResource authorizationResource : consentAuthResources) {
                        if (!usernames.contains(authorizationResource.getUserID())) {
                            log.error("No matching authorisation resources found for the given consent.");
                            throw new AuthenticationFailedException("No matching authorisation resources found for the "
                                    + "given consent.");
                        }
                    }
                } else {
                    log.error("Authorisation resources partially exists for the given consent.");
                    throw new AuthenticationFailedException("Authorisation resources partially exists for the " +
                            "given consent.");
                }
            } else {
                throw new AuthenticationFailedException("Could not extract request object from the request.");
            }
        } catch (ConsentManagementException | ParseException e) {
            log.error("Error occurred while persisting authorisation resources", e);
            throw new AuthenticationFailedException("Error occurred while persisting authorisation resources", e);
        }
    }
}
