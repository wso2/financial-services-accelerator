/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package org.wso2.financial.services.accelerator.event.notifications.service.realtime.service;

import com.nimbusds.jose.JOSEException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.financial.services.accelerator.common.util.DatabaseUtils;
import org.wso2.financial.services.accelerator.event.notifications.service.EventNotificationGenerator;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.dao.EventNotificationDAO;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.internal.EventNotificationDataHolder;
import org.wso2.financial.services.accelerator.event.notifications.service.model.EventSubscription;
import org.wso2.financial.services.accelerator.event.notifications.service.model.Notification;
import org.wso2.financial.services.accelerator.event.notifications.service.model.NotificationEvent;
import org.wso2.financial.services.accelerator.event.notifications.service.model.NotificationResponse;
import org.wso2.financial.services.accelerator.event.notifications.service.model.RealtimeEventNotification;
import org.wso2.financial.services.accelerator.event.notifications.service.persistence.EventNotificationStoreInitializer;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationServiceUtil;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This service is used to add open state event notifications to the realtime event notification queue.
 * This service is called whenever the server starts.
 */
public class RealtimeEventNotificationLoaderService implements Runnable {
    private static final Log log = LogFactory.getLog(RealtimeEventNotificationLoaderService.class);

    @Override
    public void run() {
        // Get all open state event notifications from the database and add them to the queue
        Connection connection = DatabaseUtils.getDBConnection();
        try {
            LinkedBlockingQueue<RealtimeEventNotification> queue = EventNotificationDataHolder.getInstance().
                    getRealtimeEventNotificationQueue();
            EventNotificationDAO pollingDAO = EventNotificationStoreInitializer.getEventNotificationDAO();
            EventNotificationGenerator eventNotificationGenerator = EventNotificationServiceUtil.
                    getEventNotificationGenerator();
            List<Notification> openNotifications = pollingDAO.getNotificationsByStatus(connection,
                    EventNotificationConstants.OPEN);

            for (Notification notification : openNotifications) {
                //Get events by notificationId
                List<NotificationEvent> notificationEvents = pollingDAO.
                        getEventsByNotificationID(connection, notification.getNotificationId());
                DatabaseUtils.commitTransaction(connection);

                List<EventSubscription> subscriptionList = EventNotificationServiceUtil.getEventSubscriptionService()
                        .getEventSubscriptionsByClientId(notification.getClientId());
                if (subscriptionList.isEmpty()) {
                    throw new FSEventNotificationException("No subscriptions found for the client ID: " +
                            notification.getClientId());
                }

                for (EventSubscription subscription : subscriptionList) {
                    List<NotificationEvent> allowedEvents = new ArrayList<>();
                    notificationEvents.forEach(notificationEvent -> {
                        if (subscription.getEventTypes().contains(notificationEvent.getEventType())) {
                            allowedEvents.add(notificationEvent);
                        }
                    });

                    NotificationResponse responseNotification = eventNotificationGenerator.
                            generateEventNotificationBody(notification, allowedEvents);
                    RealtimeEventNotification realtimeEventNotification = new RealtimeEventNotification();
                    realtimeEventNotification.setCallbackUrl(subscription.getCallbackUrl());
                    realtimeEventNotification.setSecurityEventToken(eventNotificationGenerator.
                            generateEventNotification(NotificationResponse.getJsonNode(responseNotification)));
                    realtimeEventNotification.setNotification(notification);
                    queue.put(realtimeEventNotification); // put the notification into the queue
                }
            }
        } catch (InterruptedException e) {
            DatabaseUtils.rollbackTransaction(connection);
            log.error("Error when adding the Realtime Notification into the RealtimeEventNotification Queue", e);
        } catch (FSEventNotificationException e) {
            DatabaseUtils.rollbackTransaction(connection);
            log.error("Error when generating the event notification", e);
        } catch (IOException | JOSEException | IdentityOAuth2Exception e) {
            DatabaseUtils.rollbackTransaction(connection);
            log.error("Error while processing event notification JSON object", e);
        } finally {
            DatabaseUtils.closeConnection(connection);
        }
    }
}
