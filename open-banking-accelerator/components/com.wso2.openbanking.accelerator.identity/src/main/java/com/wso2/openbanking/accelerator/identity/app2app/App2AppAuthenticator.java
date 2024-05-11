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
package com.wso2.openbanking.accelerator.identity.app2app;


import com.nimbusds.jwt.SignedJWT;
import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.identity.app2app.exception.SecretValidationException;
import com.wso2.openbanking.accelerator.identity.app2app.model.Secret;
import com.wso2.openbanking.accelerator.identity.app2app.utils.App2AppAuthUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authenticator.push.PushAuthenticator;
import org.wso2.carbon.identity.application.common.model.Property;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



/**
 * App2App authenticator for authenticating users from native auth attempt.
 */
public class App2AppAuthenticator extends PushAuthenticator {

    private static final Log log = LogFactory.getLog(App2AppAuthenticator.class);
    private static final long serialVersionUID = -5439464372188473141L;

    @Override
    public String getName() {

        return App2AppAuthenticatorConstants.AUTHENTICATOR_NAME;

    }

    @Override
    public String getFriendlyName() {

        return App2AppAuthenticatorConstants.AUTHENTICATOR_FRIENDLY_NAME;

    }
    @Override
    protected void processAuthenticationResponse(HttpServletRequest httpServletRequest,
                                                 HttpServletResponse httpServletResponse,
                                                 AuthenticationContext authenticationContext)
            throws AuthenticationFailedException {

        String jwtString = httpServletRequest.getParameter(App2AppAuthenticatorConstants.SECRET);
        try {
            SignedJWT signedJWT = JWTUtils.getSignedJWT(jwtString);
            Secret secret = new Secret(signedJWT);
            String loginHint = secret.getLoginHint();
            AuthenticatedUser authenticatedUser = App2AppAuthUtils.getAuthenticatedUserFromSubjectIdentifier(loginHint);
            secret.setAuthenticatedUser(authenticatedUser);
            App2AppAuthUtils.validateSecret(secret);
            AuthenticatedUser user = secret.getAuthenticatedUser();
            authenticationContext.setSubject(user);
        } catch (SecretValidationException e) {
            throw new AuthenticationFailedException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new AuthenticationFailedException("Illegal Argument exception: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new AuthenticationFailedException("Run Time exception: " + e.getMessage(), e);
        } catch (ParseException e) {
            throw new AuthenticationFailedException("Provided JWT for AppValidationJWT is not parsable: " + e.getMessage(), e);
        }

    }

    @Override
    public boolean canHandle(HttpServletRequest httpServletRequest) {

        return !StringUtils.isBlank(httpServletRequest.getParameter(App2AppAuthenticatorConstants.SECRET));

    }

    @Override
    public String getContextIdentifier(HttpServletRequest request) {

        return request.getParameter(App2AppAuthenticatorConstants.SESSION_DATA_KEY);

    }

    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationContext context) throws AuthenticationFailedException {
        log.error("Initializing App2App authenticator is not supported.");
        throw new AuthenticationFailedException("Mandatory parameter secret null or empty in request.");

    }

    //TODO : remove this configuration properties.
    @Override
    public List<Property> getConfigurationProperties() {

        List<Property> configProperties = new ArrayList<>();
        String firebaseServerKey = "Firebase Server Key";
        Property serverKeyProperty = new Property();
        serverKeyProperty.setName("ServerKey");
        serverKeyProperty.setDisplayName(firebaseServerKey);
        serverKeyProperty.setDescription("Enter the firebase server key ");
        serverKeyProperty.setDisplayOrder(0);
        serverKeyProperty.setRequired(true);
        configProperties.add(serverKeyProperty);
        return configProperties;

    }
}

