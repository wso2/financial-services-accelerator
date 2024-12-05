/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.accelerator.consent.extensions.ciba.authenticator.weblink;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * This interface represents the CIBA web link authenticator extensions.
 */
public interface CIBAWebLinkAuthenticatorExtensionInterface {

    /**
     * Method to identify the user/users involved in the authentication.
     *
     * @param request HttpServletRequest
     * @return list of users
     */
    List<AuthenticatedUser> getAuthenticatedUsers(HttpServletRequest request) throws AuthenticationFailedException;

    /**
     * Method to generate web auth links for given user.
     *
     * @param context authentication context.
     * @param user    authenticated user.
     * @return Auth web link for authenticated user.
     */
    String generateWebAuthLink(AuthenticationContext context, AuthenticatedUser user)
            throws AuthenticationFailedException;

    /**
     * Method to create authorisation resources for given users.
     *
     * @param authenticatedUsers list of users involved for the authorisation
     * @param context            authenticationContext
     */
    void createAuthResourcesForUsers(List<AuthenticatedUser> authenticatedUsers,
                                     AuthenticationContext context) throws AuthenticationFailedException;
}
