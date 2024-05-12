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
import com.wso2.openbanking.accelerator.identity.app2app.exception.JWTValidationException;
import com.wso2.openbanking.accelerator.identity.app2app.model.AppAuthValidationJWT;
import com.wso2.openbanking.accelerator.identity.app2app.utils.App2AppAuthUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authenticator.push.PushAuthenticator;
import org.wso2.carbon.identity.application.authenticator.push.device.handler.exception.PushDeviceHandlerClientException;
import org.wso2.carbon.identity.application.authenticator.push.device.handler.exception.PushDeviceHandlerServerException;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;

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

        String jwtString = httpServletRequest.getParameter(App2AppAuthenticatorConstants.AppAuthValidationJWTIdentifier);
        try {
            SignedJWT signedJWT = JWTUtils.getSignedJWT(jwtString);
            AppAuthValidationJWT appAuthValidationJWT = new AppAuthValidationJWT(signedJWT);
            String loginHint = appAuthValidationJWT.getLoginHint();
            String deviceID = appAuthValidationJWT.getDeviceId();
            AuthenticatedUser userToBeAuthenticated = App2AppAuthUtils.getAuthenticatedUserFromSubjectIdentifier(loginHint);
            String publicKey = getPublicKeyByDeviceID(deviceID,userToBeAuthenticated);
            appAuthValidationJWT.setPublicKey(publicKey);
            appAuthValidationJWT.setSigningAlgorithm(App2AppAuthenticatorConstants.SIGNING_ALGORITHM);
            /*
                if validations are failed it will throw a JWTValidationException and flow will be interrupted.
                Hence, user Authentication will fail.
             */
            App2AppAuthUtils.validateSecret(appAuthValidationJWT);
            //If the flow is not interrupted user will be authenticated.
            authenticationContext.setSubject(userToBeAuthenticated);
        } catch (JWTValidationException e) {
            throw new AuthenticationFailedException(App2AppAuthenticatorConstants.JWT_VALIDATION_EXCEPTION_MESSAGE + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new AuthenticationFailedException(App2AppAuthenticatorConstants.ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new AuthenticationFailedException(App2AppAuthenticatorConstants.RUNTIME_EXCEPTION_MESSAGE + e.getMessage(), e);
        } catch (ParseException e) {
            throw new AuthenticationFailedException(App2AppAuthenticatorConstants.PARSE_EXCEPTION_MESSAGE + e.getMessage(), e);
        } catch (PushDeviceHandlerServerException e) {
            throw new AuthenticationFailedException(App2AppAuthenticatorConstants.PUSH_DEVICE_HANDLER_SERVER_EXCEPTION_MESSAGE, e);
        } catch (UserStoreException e) {
            throw new AuthenticationFailedException(App2AppAuthenticatorConstants.USER_STORE_EXCEPTION_MESSAGE, e);
        } catch (PushDeviceHandlerClientException e) {
            throw new AuthenticationFailedException(App2AppAuthenticatorConstants.PUSH_DEVICE_HANDLER_CLIENT_EXCEPTION_MESSAGE, e);
        }

    }

    @Override
    public boolean canHandle(HttpServletRequest httpServletRequest) {

        return !StringUtils.isBlank(httpServletRequest.getParameter(App2AppAuthenticatorConstants.AppAuthValidationJWTIdentifier));

    }

    @Override
    public String getContextIdentifier(HttpServletRequest request) {

        return request.getParameter(App2AppAuthenticatorConstants.SESSION_DATA_KEY);

    }

    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationContext context) throws AuthenticationFailedException {
        log.error(App2AppAuthenticatorConstants.INITIALIZATION_ERROR_MESSAGE);
        throw new AuthenticationFailedException(App2AppAuthenticatorConstants.MANDATORY_PARAMETER_ERROR_MESSAGE);

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

    /**
     * Retrieves the public key associated with a device and user.
     *
     * @param deviceID    The identifier of the device for which the public key is requested.
     * @param authenticatedUser  the authenticated user for this request
     * @return            The public key associated with the specified device and user.
     * @throws UserStoreException                If an error occurs while accessing user store.
     * @throws PushDeviceHandlerServerException  If an error occurs on the server side of the push device handler.
     * @throws PushDeviceHandlerClientException  If an error occurs on the client side of the push device handler.
     */
    private String getPublicKeyByDeviceID(String deviceID, AuthenticatedUser authenticatedUser) throws UserStoreException,
            PushDeviceHandlerServerException, PushDeviceHandlerClientException {

        UserRealm userRealm = App2AppAuthUtils.getUserRealm(authenticatedUser);
        String userID = App2AppAuthUtils.getUserIdFromUsername(authenticatedUser.getUserName(), userRealm);
        return App2AppAuthUtils.getPublicKey(deviceID, userID);

    }
}

