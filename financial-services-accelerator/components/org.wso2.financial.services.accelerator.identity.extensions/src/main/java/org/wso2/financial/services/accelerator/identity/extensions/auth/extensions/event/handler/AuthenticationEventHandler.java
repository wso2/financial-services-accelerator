/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.event.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.bean.context.MessageContext;
import org.wso2.carbon.identity.core.handler.InitConfig;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.financial.services.accelerator.data.publisher.util.FSDataPublisherUtil;

import java.time.Instant;
import java.util.HashMap;

/**
 * IS Authentication Events Handler
 */
public class AuthenticationEventHandler extends AbstractEventHandler {

    private static final Log log = LogFactory.getLog(AuthenticationEventHandler.class);
    public static final String EVENT_AUTHENTICATION_STEP_SUCCESS = "AUTHENTICATION_STEP_SUCCESS";
    public static final String EVENT_AUTHENTICATION_STEP_FAILURE = "AUTHENTICATION_STEP_FAILURE";
    public static final String EVENT_POST_ISSUE_ACCESS_TOKEN_V2 = "POST_ISSUE_ACCESS_TOKEN_V2";
    public static final String AUTHENTICATION_SUCCESSFUL = "AuthenticationSuccessful";
    public static final String AUTHENTICATION_FAILED = "AuthenticationFailed";
    public static final String AUTHENTICATION_ATTEMPTED = "AuthenticationAttempted";
    public static final String TOKEN_EXCHANGED = "TokenExchanged";
    public static final String AUTHENTICATED_USER = "authenticatedUser";
    public static final String BASIC_AUTHENTICATOR = "BasicAuthenticator";
    public static final String LAST_LOGIN_FAILED_USER = "lastLoginFailedUser";
    public static final String USER_ID = "userId";
    public static final String AUTHENTICATION_STATUS = "authenticationStatus";
    public static final String AUTHENTICATION_STEP = "authenticationStep";
    public static final String TIMESTAMP = "timestamp";
    public static final String AUTHENTICATION_APPROACH = "authenticationApproach";
    public static final String AUTHENTICATION_INPUT_STREAM = "UserAuthenticationEventStream";
    public static final String REDIRECT = "redirect";
    public static final String STREAM_VERSION = "1.0.0";

    @Override
    public String getName() {

        return "customUserAuthentication";
    }

    public void handleEvent(Event event) {
        String eventName = event.getEventName();
        if (EVENT_AUTHENTICATION_STEP_SUCCESS.equals(eventName) ||
                EVENT_AUTHENTICATION_STEP_FAILURE.equals(eventName) ||
                EVENT_POST_ISSUE_ACCESS_TOKEN_V2.equals(eventName)) {
            Object contextObject = event.getEventProperties().get("context");
            AuthenticationContext authenticationContext = (AuthenticationContext) contextObject;
            String authStatus = null;
            if (EVENT_AUTHENTICATION_STEP_SUCCESS.equals(eventName)) {
                authStatus = AUTHENTICATION_SUCCESSFUL;
            } else if (EVENT_AUTHENTICATION_STEP_FAILURE.equals(eventName)) {
                authStatus = AUTHENTICATION_FAILED;
            } else if (EVENT_POST_ISSUE_ACCESS_TOKEN_V2.equals(eventName)) {
                authStatus = TOKEN_EXCHANGED;
            }
            extractAuthDataToPublish(authenticationContext, authStatus);
        }

    }

    @Override
    public void init(InitConfig configuration) throws IdentityRuntimeException {

        super.init(configuration);
    }

    @Override
    public int getPriority(MessageContext messageContext) {

        return 250;
    }


    protected void extractAuthDataToPublish(AuthenticationContext context, String authenticationStatus) {

        HashMap<String, Object> authenticationData = new HashMap<>();
        String userName = null;
        String authenticationStep = null;
        long unixTimestamp = Instant.now().getEpochSecond();

        //Retrieves the user ID from context
        if (AUTHENTICATION_SUCCESSFUL.equals(authenticationStatus)) {
            AuthenticatedUser authenticatedUser = context.getLastAuthenticatedUser();
            userName = authenticatedUser.getAuthenticatedSubjectIdentifier();
        } else if (AUTHENTICATION_FAILED.equals(authenticationStatus)) {
            if (context.getParameters().get(AUTHENTICATED_USER) != null) {
                userName = ((AuthenticatedUser) context.getParameters()
                        .get(AUTHENTICATED_USER)).getUserName();
            } else if (context.getParameters()
                    .get(LAST_LOGIN_FAILED_USER) != null) {
                userName = ((AuthenticatedUser) context.getParameters()
                        .get(LAST_LOGIN_FAILED_USER)).getUserName();
            } else {
                log.error("Failed to retrieve the user name relating to the authentication");
            }
        }

        //Retrieves authentication step from context
        if (context.getCurrentAuthenticator() != null) {
            authenticationStep = context.getCurrentAuthenticator();
        } else if (context.getCurrentAuthenticator() == null &&
                AUTHENTICATION_ATTEMPTED.equals(authenticationStatus)) {
            authenticationStep = null;
        } else {
            log.error("Failed to retrieve the authentication step relating to the authentication");
        }

        authenticationData.put(USER_ID, userName);
        authenticationData.put(AUTHENTICATION_STATUS, authenticationStatus);
        authenticationData.put(AUTHENTICATION_STEP, authenticationStep);
        authenticationData.put(TIMESTAMP, unixTimestamp);
        authenticationData.put(AUTHENTICATION_APPROACH, REDIRECT);

        //Publish Data
        FSDataPublisherUtil.publishData(AUTHENTICATION_INPUT_STREAM,
                STREAM_VERSION, authenticationData);
    }
}
