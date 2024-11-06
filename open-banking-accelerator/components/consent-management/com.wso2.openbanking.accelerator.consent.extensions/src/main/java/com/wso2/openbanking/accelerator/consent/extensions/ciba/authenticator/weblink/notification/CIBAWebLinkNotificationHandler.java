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

package com.wso2.openbanking.accelerator.consent.extensions.ciba.authenticator.weblink.notification;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.consent.extensions.ciba.authenticator.weblink.CIBAWebLinkAuthenticatorConstants;
import com.wso2.openbanking.accelerator.consent.extensions.ciba.authenticator.weblink.notification.provider.NotificationProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.bean.context.MessageContext;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.bean.IdentityEventMessageContext;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.notification.DefaultNotificationHandler;

import java.lang.reflect.InvocationTargetException;


/**
 * This class represents the ciba web link notification handler.
 */
public class CIBAWebLinkNotificationHandler extends DefaultNotificationHandler {

    private static final Log log = LogFactory.getLog(CIBAWebLinkNotificationHandler.class);
    private static NotificationProvider notificationProvider;

    public CIBAWebLinkNotificationHandler() {
        setCIBAWebLinkNotificationProvider();
    }

    @Override
    public boolean canHandle(MessageContext messageContext) throws IdentityRuntimeException {

        Event event = ((IdentityEventMessageContext) messageContext).getEvent();
        return event.getEventName().equals(CIBAWebLinkAuthenticatorConstants.NOTIFICATION_TRIGGER_EVENT);
    }

    @Override
    public String getName() {

        return CIBAWebLinkAuthenticatorConstants.NOTIFICATION_HANDLER_NAME;
    }

    @Override
    public void handleEvent(Event event) throws IdentityEventException {

        String username = (String) event.getEventProperties().get(IdentityEventConstants.EventProperty.USER_NAME);
        String weblink = (String) event.getEventProperties().get(OpenBankingConstants.CIBA_WEB_AUTH_LINK_PARAM);

        if (log.isDebugEnabled()) {
            log.debug("Handling notification event : " + weblink);
        }

        try {
            notificationProvider.send(username, weblink);
        } catch (OpenBankingException e) {
            log.error("Error occurred while sending the notification", e);
        }

    }

    /**
     * Retrieve the config for CIBA notification provider
     */
    private static void setCIBAWebLinkNotificationProvider() {

        try {
            notificationProvider = (NotificationProvider)
                    Class.forName(OpenBankingConfigParser.getInstance()
                            .getCibaWebLinkNotificationProvider()).getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException |
                 InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            log.error("CIBA notification provider extension not found", e);
        }
    }

}
