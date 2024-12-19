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

package org.wso2.financial.services.accelerator.event.notifications.service.realtime.service;

import com.nimbusds.jose.JOSEException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.financial.services.accelerator.event.notifications.service.EventNotificationGenerator;
import org.wso2.financial.services.accelerator.event.notifications.service.EventSubscriptionService;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.internal.EventNotificationDataHolder;
import org.wso2.financial.services.accelerator.event.notifications.service.model.EventSubscription;
import org.wso2.financial.services.accelerator.event.notifications.service.model.Notification;
import org.wso2.financial.services.accelerator.event.notifications.service.model.NotificationEvent;
import org.wso2.financial.services.accelerator.event.notifications.service.model.NotificationResponse;
import org.wso2.financial.services.accelerator.event.notifications.service.model.RealtimeEventNotification;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationServiceUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This thread is used to produce the event notification and put it into the realtime event notification queue.
 */
public class EventNotificationProducerService implements Runnable {
    private static final Log log = LogFactory.getLog(EventNotificationProducerService.class);
    private final Notification notification;
    private final List<NotificationEvent> notificationEvents;
    private EventSubscriptionService eventSubscriptionService;

    public EventNotificationProducerService(
            Notification notification, List<NotificationEvent> notificationEvents) {

        this.notification = notification;
        this.notificationEvents = notificationEvents;

        this.eventSubscriptionService = new EventSubscriptionService();
    }

    public void setEventSubscriptionService(EventSubscriptionService eventSubscriptionService) {
        this.eventSubscriptionService = eventSubscriptionService;
    }

    @Override
    public void run() {

        try {
            List<EventSubscription> subscriptionList = eventSubscriptionService.getEventSubscriptionsByClientId(
                    notification.getClientId());
            if (CollectionUtils.isEmpty(subscriptionList)) {
                throw new FSEventNotificationException("No subscriptions found for the client ID: " +
                        notification.getClientId());
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

                if (!allowedEvents.isEmpty() && StringUtils.isNotEmpty(subscription.getCallbackUrl())) {
                    RealtimeEventNotification realtimeEventNotification = new RealtimeEventNotification();
                    realtimeEventNotification.setNotification(notification);
                    realtimeEventNotification.setCallbackUrl(subscription.getCallbackUrl());

                    NotificationResponse notificationResponse = eventNotificationGenerator
                            .generateEventNotificationBody(notification, allowedEvents);
                    realtimeEventNotification.setSecurityEventToken(eventNotificationGenerator
                            .generateEventNotification(NotificationResponse.getJsonNode(notificationResponse)));

                    queue.put(realtimeEventNotification); // put the notification into the queue
                }
            }
        } catch (InterruptedException e) {
            log.error("Error when adding the Realtime Notification with notification ID " +
                    notification.getNotificationId().replaceAll("[\r\n]", "") +
                    " into the RealtimeEventNotification Queue", e);
        } catch (FSEventNotificationException e) {
            log.error("Error when generating the event notification", e);
        } catch (IOException | JOSEException | IdentityOAuth2Exception e) {
            log.error("Error while processing event notification JSON object", e);
        }
    }
}
