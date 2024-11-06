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

package com.wso2.openbanking.accelerator.consent.extensions.ciba.authenticator.weblink;


import com.nimbusds.jwt.SignedJWT;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.CarbonUtils;
import com.wso2.openbanking.accelerator.consent.extensions.ciba.authenticator.CIBAPushAuthenticatorConstants;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.extension.identity.helper.FederatedAuthenticatorUtil;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.InboundConstants;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.user.api.UserStoreException;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * CIBA Web Link Authenticator for sending auth web links to authentication device / devices
 */
public class CIBAWebLinkAuthenticator extends AbstractApplicationAuthenticator
        implements FederatedApplicationAuthenticator {

    private static final Log log = LogFactory.getLog(CIBAWebLinkAuthenticator.class);
    private static final ConsentCoreService consentCoreService =
            ConsentExtensionsDataHolder.getInstance().getConsentCoreService();
    private static final String AUTHORIZE_URL_PATH = "/oauth2/authorize?";

    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationContext context) throws AuthenticationFailedException {

        List<AuthenticatedUser> authenticatedUsers = getAuthenticatedUsers(request);
        for (AuthenticatedUser user : authenticatedUsers) {
            try {
                if (!FederatedAuthenticatorUtil.isUserExistInUserStore(user.getUserName())) {
                    log.error(String.format("User does not exist in the User store : %s", user.getUserName()));
                    throw new AuthenticationFailedException("User does not exist in the User store");
                }
            } catch (UserStoreException e) {
                log.error(String.format("Cannot find the user in User store : %s", user.getUserName()));
                throw new AuthenticationFailedException("Cannot find the user in User store", e);
            }
        }
        // Handle creating auth resources for users.
        createAuthResourcesForUsers(authenticatedUsers, context);
        HashMap<String, String> webAuthLinksMap = new HashMap<>();
        for (AuthenticatedUser user : authenticatedUsers) {
            webAuthLinksMap.put(user.getUserName(), generateWebAuthLink(context, user));
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s no. of users has been resolved for web auth links",
                    authenticatedUsers.size()));
        }

        // Triggering notification event for web links
        for (Map.Entry<String, String> webLinkEntry : webAuthLinksMap.entrySet()) {
            String webAuthLink = webAuthLinksMap.get(webLinkEntry.getKey());
            String username = webLinkEntry.getKey();
            triggerNotificationEvent(username, webAuthLink);
        }

    }

    /**
     * Method to create authorisation resources for given users.
     *
     * @param authenticatedUsers list of users involved for the authorisation
     * @param context            authenticationContext
     */
    protected void createAuthResourcesForUsers(List<AuthenticatedUser> authenticatedUsers,
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

    /**
     * Method to trigger the notification event in IS.
     */
    protected void triggerNotificationEvent(String userName, String webLink) throws AuthenticationFailedException {

        HashMap<String, Object> properties = new HashMap<>();
        properties.put(IdentityEventConstants.EventProperty.USER_NAME, userName);
        properties.put(OpenBankingConstants.CIBA_WEB_AUTH_LINK_PARAM, webLink);
        Event identityMgtEvent = new Event(CIBAWebLinkAuthenticatorConstants.NOTIFICATION_TRIGGER_EVENT, properties);
        try {
            ConsentExtensionsDataHolder.getInstance().getIdentityEventService().handleEvent(identityMgtEvent);
        } catch (IdentityEventException e) {
            throw new AuthenticationFailedException("Error occurred while calling triggerNotificationEvent", e);
        }

    }

    /**
     * Method to identify the user/users involved in the authentication.
     *
     * @param request HttpServletRequest
     * @return list of users
     */
    protected List<AuthenticatedUser> getAuthenticatedUsers(HttpServletRequest request) {

        List<AuthenticatedUser> users = Arrays.stream(request.getParameter(CIBAPushAuthenticatorConstants.LOGIN_HINT)
                        .split(","))
                .map(String::trim)
                .map(AuthenticatedUser::createLocalAuthenticatedUserFromSubjectIdentifier)
                .collect(Collectors.toList());
        return users;
    }

    /**
     * Method to generate web auth links for given user.
     *
     * @param context authentication context.
     * @param user    authenticated user.
     * @return Auth web link for authenticated user.
     */
    protected String generateWebAuthLink(AuthenticationContext context, AuthenticatedUser user) {

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
        builder.append(CarbonUtils.getCarbonServerUrl()).append(AUTHORIZE_URL_PATH);
        for (String param : paramList) {
            builder.append(param).append("&");
        }
        if (log.isDebugEnabled()) {
            log.debug(builder.toString());
        }
        return builder.toString();
    }


    @Override
    protected void processAuthenticationResponse(HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationContext context) throws AuthenticationFailedException {
        // This authenticator is used only to send the web-auth links, And it does not expect to process the response.
    }

    @Override
    public boolean canHandle(HttpServletRequest request) {
        // CIBA web link Authenticator is used only to send the web-auth links, And it does not expect to handle it.
        return false;
    }

    @Override
    public String getContextIdentifier(HttpServletRequest request) {
        return request.getParameter(InboundConstants.RequestProcessor.CONTEXT_KEY);
    }

    @Override
    public String getName() {
        return CIBAWebLinkAuthenticatorConstants.AUTHENTICATOR_NAME;
    }

    @Override
    public String getFriendlyName() {
        return CIBAWebLinkAuthenticatorConstants.AUTHENTICATOR_FRIENDLY_NAME;
    }
}
