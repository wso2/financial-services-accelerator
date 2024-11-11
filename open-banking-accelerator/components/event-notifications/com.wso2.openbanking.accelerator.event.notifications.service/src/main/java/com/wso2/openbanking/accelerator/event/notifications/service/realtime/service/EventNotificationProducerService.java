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

package com.wso2.openbanking.accelerator.event.notifications.service.realtime.service;

import com.nimbusds.jose.JOSEException;
import com.wso2.openbanking.accelerator.event.notifications.service.dto.NotificationDTO;
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.internal.EventNotificationDataHolder;
import com.wso2.openbanking.accelerator.event.notifications.service.model.EventSubscription;
import com.wso2.openbanking.accelerator.event.notifications.service.model.Notification;
import com.wso2.openbanking.accelerator.event.notifications.service.model.NotificationEvent;
import com.wso2.openbanking.accelerator.event.notifications.service.realtime.model.RealtimeEventNotification;
import com.wso2.openbanking.accelerator.event.notifications.service.service.EventNotificationGenerator;
import com.wso2.openbanking.accelerator.event.notifications.service.service.EventPollingService;
import com.wso2.openbanking.accelerator.event.notifications.service.util.EventNotificationServiceUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This thread is used to produce the event notification and put it into the realtime event notification queue.
 */
public class EventNotificationProducerService implements Runnable {
    private static final Log log = LogFactory.getLog(EventPollingService.class);
    private final NotificationDTO notificationDTO;
    private final List<NotificationEvent> notificationEvents;

    public EventNotificationProducerService(
            NotificationDTO notificationDTO, List<NotificationEvent> notificationEvents) {
        this.notificationDTO = notificationDTO;
        this.notificationEvents = notificationEvents;
    }

    @Override
    public void run() {

        try {
            List<EventSubscription> subscriptionList = EventNotificationServiceUtil.getEventSubscriptionService()
                    .getEventSubscriptionsByClientId(notificationDTO.getClientId());
            if (CollectionUtils.isEmpty(subscriptionList)) {
                throw new OBEventNotificationException("No subscriptions found for the client ID: " +
                        notificationDTO.getClientId());
            }

            LinkedBlockingQueue<RealtimeEventNotification> queue = EventNotificationDataHolder.getInstance().
                    getRealtimeEventNotificationQueue();
            EventNotificationGenerator eventNotificationGenerator = EventNotificationServiceUtil.
                    getEventNotificationGenerator();

            for (EventSubscription subscription : subscriptionList) {

                List<NotificationEvent> allowedEvents = new ArrayList<>();
                notificationEvents.forEach(notificationEvent -> {
                    if (subscription.getEventTypes().contains(notificationEvent.getEventType())) {
                        allowedEvents.add(notificationEvent);
                    }
                });

                if (!allowedEvents.isEmpty()) {
                    RealtimeEventNotification realtimeEventNotification = new RealtimeEventNotification();
                    realtimeEventNotification.setNotificationDTO(notificationDTO);
                    realtimeEventNotification.setCallbackUrl(subscription.getCallbackUrl());

                    Notification notification = eventNotificationGenerator.generateEventNotificationBody(
                            notificationDTO, allowedEvents);
                    realtimeEventNotification.setEventSET(eventNotificationGenerator
                            .generateEventNotification(Notification.getJsonNode(notification)));

                    queue.put(realtimeEventNotification); // put the notification into the queue
                }
            }
        } catch (InterruptedException e) {
            log.error("Error when adding the Realtime Notification with notification ID " +
                    notificationDTO.getNotificationId() + " into the RealtimeEventNotification Queue", e);
        } catch (OBEventNotificationException e) {
            log.error("Error when generating the event notification", e);
        } catch (IOException | JOSEException | IdentityOAuth2Exception e) {
            log.error("Error while processing event notification JSON object", e);
        }
    }
}
