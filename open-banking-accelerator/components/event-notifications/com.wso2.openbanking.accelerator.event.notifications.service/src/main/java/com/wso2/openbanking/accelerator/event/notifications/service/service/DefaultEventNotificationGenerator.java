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

package com.wso2.openbanking.accelerator.event.notifications.service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.event.notifications.service.dto.NotificationDTO;
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.model.Notification;
import com.wso2.openbanking.accelerator.event.notifications.service.model.NotificationEvent;
import com.wso2.openbanking.accelerator.event.notifications.service.util.EventNotificationServiceUtil;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;

import java.time.Instant;
import java.util.List;

/**
 * Default Event Notification Response Generator Class.
 */
public class DefaultEventNotificationGenerator implements EventNotificationGenerator {

    private static Log log = LogFactory.getLog(DefaultEventNotificationGenerator.class);

    @Override
    public Notification generateEventNotificationBody(NotificationDTO notificationDTO,
                                                      List<NotificationEvent> notificationEventList)
            throws OBEventNotificationException {

        Notification notification = new Notification();
        //get current time in milliseconds
        Long currentTime = Instant.now().getEpochSecond();

        //generate transaction Identifier
        String transactionIdentifier = UUIDGenerator.generateUUID();

        notification.setIss(OpenBankingConfigParser.getInstance().getEventNotificationTokenIssuer());
        notification.setIat(currentTime);
        notification.setAud(notificationDTO.getClientId());
        notification.setJti(notificationDTO.getNotificationId());
        notification.setTxn(transactionIdentifier);
        notification.setToe(notificationDTO.getUpdatedTimeStamp());
        notification.setSub(generateSubClaim(notificationDTO));
        notification.setEvents(notificationEventList);
        return notification;
    }

    @Generated(message = "Excluded from tests as using a util method from a different package")
    public String generateEventNotification(JsonNode jsonNode)
            throws OBEventNotificationException {

        String payload = EventNotificationServiceUtil.getCustomNotificationPayload(jsonNode);
        try {
            return IdentityCommonUtil.signJWTWithDefaultKey(payload);
        } catch (Exception e) {
            log.error("Error while signing the JWT token", e);
            throw new OBEventNotificationException("Error while signing the JWT token", e);
        }

    }

    @Generated(message = "Private method tested when the used method is tested")
    private String generateSubClaim(NotificationDTO notificationDTO) {
        String sub = notificationDTO.getClientId();
        return sub;
    }

}
