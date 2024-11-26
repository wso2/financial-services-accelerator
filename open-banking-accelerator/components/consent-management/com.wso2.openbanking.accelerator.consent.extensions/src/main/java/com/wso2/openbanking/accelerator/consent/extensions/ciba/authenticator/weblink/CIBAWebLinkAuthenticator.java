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


import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.util.OpenBankingUtils;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * CIBA Web Link Authenticator for sending auth web links to authentication device / devices
 */
public class CIBAWebLinkAuthenticator extends AbstractApplicationAuthenticator
        implements FederatedApplicationAuthenticator {

    private static final Log log = LogFactory.getLog(CIBAWebLinkAuthenticator.class);
    private static CIBAWebLinkAuthenticatorExtensionInterface authenticatorExtension;

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
        if (authenticatorExtension == null) {
            authenticatorExtension = getCIBAWebLinkAuthenticatorExtension();
        }
        authenticatorExtension.createAuthResourcesForUsers(authenticatedUsers, context);
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
    protected List<AuthenticatedUser> getAuthenticatedUsers(HttpServletRequest request)
            throws AuthenticationFailedException {
        if (authenticatorExtension == null) {
            authenticatorExtension = getCIBAWebLinkAuthenticatorExtension();
        }
        return authenticatorExtension.getAuthenticatedUsers(request);
    }

    /**
     * Method to generate web auth links for given user.
     *
     * @param context authentication context.
     * @param user    authenticated user.
     * @return Auth web link for authenticated user.
     */
    protected String generateWebAuthLink(AuthenticationContext context, AuthenticatedUser user)
            throws AuthenticationFailedException {
        if (authenticatorExtension == null) {
            authenticatorExtension = getCIBAWebLinkAuthenticatorExtension();
        }
        return authenticatorExtension.generateWebAuthLink(context, user);
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

    private static CIBAWebLinkAuthenticatorExtensionInterface getCIBAWebLinkAuthenticatorExtension() {
        return (CIBAWebLinkAuthenticatorExtensionInterface) OpenBankingUtils.getClassInstanceFromFQN(
                OpenBankingConfigParser.getInstance().getCIBAWebLinkAuthenticatorExtension());
    }
}
