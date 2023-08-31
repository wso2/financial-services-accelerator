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

package com.wso2.openbanking.accelerator.authentication.data.publisher.service;

import com.wso2.openbanking.accelerator.authentication.data.publisher.constant.AuthPublisherConstants;
import com.wso2.openbanking.accelerator.authentication.data.publisher.extension.AbstractAuthDataPublisher;
import com.wso2.openbanking.accelerator.authentication.data.publisher.internal.AuthenticationDataPublisherDataHolder;
import com.wso2.openbanking.accelerator.authentication.data.publisher.internal.AuthenticationDataPublisherServiceComponent;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.data.publisher.common.util.OBDataPublisherUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * This class implements the custom adaptive authentication function for publishing authentication data.
 */
public class AuthenticationDataPublisherServiceImpl implements AuthenticationDataPublisherService {

    private static final Log log = LogFactory.getLog(AuthenticationDataPublisherServiceComponent.class);

    @Override
    public void authDataExtractor(JsAuthenticationContext context, String authenticationStatus,
                                  Map<String, String> parameterMap) {

        HashMap<String, Object> authenticationData = new HashMap<>();
        String userName = null;
        String authenticationStep = null;
        long unixTimestamp = Instant.now().getEpochSecond();

        //Retrieves the user ID from context
        if (AuthPublisherConstants.AUTHENTICATION_SUCCESSFUL.equals(authenticationStatus)) {
            AuthenticatedUser authenticatedUser = context.getWrapped().getLastAuthenticatedUser();
            userName = authenticatedUser.getAuthenticatedSubjectIdentifier();
        } else if (AuthPublisherConstants.AUTHENTICATION_FAILED.equals(authenticationStatus)) {
            if ((context.getWrapped()).getParameters().get(AuthPublisherConstants.AUTHENTICATED_USER) != null) {
                userName = ((AuthenticatedUser) context.getWrapped().getParameters()
                        .get(AuthPublisherConstants.AUTHENTICATED_USER)).getUserName();
            } else if ((context.getWrapped()).getParameters()
                    .get(AuthPublisherConstants.LAST_LOGIN_FAILED_USER) != null) {
                userName = ((AuthenticatedUser) context.getWrapped().getParameters()
                        .get(AuthPublisherConstants.LAST_LOGIN_FAILED_USER)).getUserName();
            } else {
                log.error("Failed to retrieve the user name relating to the authentication");
            }
        }

        //Retrieves authentication step from context
        if (context.getWrapped().getCurrentAuthenticator() != null) {
            authenticationStep = context.getWrapped().getCurrentAuthenticator();
        } else if (context.getWrapped().getCurrentAuthenticator() == null &&
                AuthPublisherConstants.AUTHENTICATION_ATTEMPTED.equalsIgnoreCase(authenticationStatus)) {
            authenticationStep = null;
        } else {
            log.error("Failed to retrieve the authentication step relating to the authentication");
        }

        //Collects additional data from toolkit level depending on the configurations
        AuthenticationDataPublisherDataHolder authenticationDataPublisherDataHolder
                = getAuthenticationDataPublisherDataHolder();
        AbstractAuthDataPublisher authDataPublisher = authenticationDataPublisherDataHolder.getAuthDataPublisher();
        Map<String, Object> additionalData = authDataPublisher.getAdditionalData(context, authenticationStatus);
        for (Map.Entry<String, Object> dataElement : additionalData.entrySet()) {
            authenticationData.put(dataElement.getKey(), dataElement.getValue());
        }

        //Collect data from the map sent by adaptive authentication function
        if (parameterMap != null) {
            HashMap<String, Object> authProperties = new HashMap<>(parameterMap); //write a config
            for (Map.Entry<String, Object> element : authProperties.entrySet()) {
                authenticationData.put(element.getKey(), element.getValue());
            }
        }
        authenticationData.put(AuthPublisherConstants.USER_ID, userName);
        authenticationData.put(AuthPublisherConstants.AUTHENTICATION_STATUS, authenticationStatus);
        authenticationData.put(AuthPublisherConstants.AUTHENTICATION_STEP, authenticationStep);
        authenticationData.put(AuthPublisherConstants.TIMESTAMP, unixTimestamp);
        authenticationData.put(AuthPublisherConstants.AUTHENTICATION_APPROACH, AuthPublisherConstants.REDIRECT);

        //Publish Data
        OBDataPublisherUtil.publishData(AuthPublisherConstants.AUTHENTICATION_INPUT_STREAM,
                AuthPublisherConstants.STREAM_VERSION, authenticationData);
    }

    @Generated(message = "Added for testing purposes")
    protected AuthenticationDataPublisherDataHolder getAuthenticationDataPublisherDataHolder() {

        return AuthenticationDataPublisherDataHolder.getInstance();
    }
}
