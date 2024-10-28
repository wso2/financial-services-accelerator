/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.event.notifications.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.common.util.JWTUtils;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.model.Notification;
import org.wso2.financial.services.accelerator.event.notifications.service.model.NotificationEvent;
import org.wso2.financial.services.accelerator.event.notifications.service.model.NotificationResponse;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationServiceUtil;

import java.time.Instant;
import java.util.List;

/**
 * Default Event Notification Response Generator Class.
 */
public class DefaultEventNotificationGenerator implements EventNotificationGenerator {

    private static Log log = LogFactory.getLog(DefaultEventNotificationGenerator.class);

    @Override
    public NotificationResponse generateEventNotificationBody(Notification notification,
                                                              List<NotificationEvent> notificationEventList)
            throws FSEventNotificationException {

        NotificationResponse notificationResponse = new NotificationResponse();
        //get current time in milliseconds
        Long currentTime = Instant.now().getEpochSecond();

        //generate transaction Identifier
        String transactionIdentifier = UUIDGenerator.generateUUID();

        notificationResponse.setIss(FinancialServicesConfigParser.getInstance().getEventNotificationTokenIssuer());
        notificationResponse.setIat(currentTime);
        notificationResponse.setAud(notification.getClientId());
        notificationResponse.setJti(notification.getNotificationId());
        notificationResponse.setTxn(transactionIdentifier);
        notificationResponse.setToe(notification.getUpdatedTimeStamp());
        notificationResponse.setSub(generateSubClaim(notification));
        notificationResponse.setEvents(notificationEventList);
        return notificationResponse;
    }

    @Generated(message = "Excluded from tests as using a util method from a different package")
    public String generateEventNotification(JsonNode jsonNode) throws FSEventNotificationException {

        String payload = EventNotificationServiceUtil.getCustomNotificationPayload(jsonNode);
        try {
            return JWTUtils.signJWTWithDefaultKey(payload);
        } catch (Exception e) {
            log.error("Error while signing the JWT token", e);
            throw new FSEventNotificationException("Error while signing the JWT token", e);
        }

    }

    @Generated(message = "Private method tested when the used method is tested")
    private String generateSubClaim(Notification notification) {
        return notification.getClientId();
    }

}
