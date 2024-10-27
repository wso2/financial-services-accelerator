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

package org.wso2.financial.services.accelerator.event.notifications.service.dao;

import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.model.Notification;
import org.wso2.financial.services.accelerator.event.notifications.service.model.NotificationError;
import org.wso2.financial.services.accelerator.event.notifications.service.model.NotificationEvent;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * Event Publisher DAO interface.
 */
public interface EventNotificationDAO {

    /**
     * This method is used to persist event notifications in the database.
     *
     * @param connection       Database connection
     * @param notification     Notification details
     * @param eventsList       List of notification events
     * @return NotificationID of the saved notification.
     * @throws FSEventNotificationException  Exception when persisting event notification data
     */
    String persistEventNotification(Connection connection, Notification notification,
                                    ArrayList<NotificationEvent> eventsList)
            throws FSEventNotificationException;

    /**
     * This method is to update the notification status by ID, allowed values are.
     * OPEN,ACK and ERR
     *
     * @param connection         Database connection
     * @param notificationId     Notification ID to update
     * @param notificationStatus Notification status to update
     * @throws FSEventNotificationException  Exception when updating notification status by ID
     */
    void updateNotificationStatusById(Connection connection, String notificationId, String notificationStatus)
            throws FSEventNotificationException;

    /**
     * This method is to store event notifications error details in the FS_NOTIFICATION table.
     *
     * @param connection         Database connection
     * @param notificationError  Notification error details
     * @throws FSEventNotificationException  Exception when storing event notifications error details
     */
    void storeErrorNotification(Connection connection, NotificationError notificationError)
            throws FSEventNotificationException;

    /**
     * This method is to retrieve given number of notifications in the FS_NOTIFICATION table by client and status.
     *
     * @param connection  Database connection
     * @param clientId    Client ID to retrieve notifications
     * @param status      Notification status to retrieve
     * @param max        Maximum number of notifications to retrieve
     * @return List of notifications by client and status
     * @throws FSEventNotificationException  Exception when retrieving notifications by client ID and status
     */
    List<Notification> getNotificationsByClientIdAndStatus(Connection connection, String clientId, String
            status, int max) throws FSEventNotificationException;

    /**
     * This method is to retrieve notifications by NotificationID.
     *
     * @param connection      Database connection
     * @param notificationId  Notification ID to retrieve
     * @return List of notifications by notification ID
     * @throws FSEventNotificationException  Exception when retrieving notifications by notification ID
     */
    List<NotificationEvent> getEventsByNotificationID(Connection connection, String notificationId)
            throws FSEventNotificationException;

    /**
     * This method is to retrieve notifications in the FS_NOTIFICATION table by status.
     *
     * @param connection  Database connection
     * @param status      Notification status to retrieve
     * @return List of notifications by status
     * @throws FSEventNotificationException  Exception when retrieving notifications by status
     */
    List<Notification> getNotificationsByStatus(Connection connection, String status)
            throws FSEventNotificationException;

    /**
     * This method is to retrieve notificationsCount by ClientId and Status.
     *
     * @param connection  Database connection
     * @param clientId    Client ID to retrieve notifications
     * @param eventStatus Notification status to retrieve
     * @return Notification count by client ID and status
     * @throws FSEventNotificationException  Exception when retrieving notification count by client ID and status
     */
    int getNotificationCountByClientIdAndStatus(Connection connection, String clientId, String eventStatus)
            throws FSEventNotificationException;

    /**
     * This method is to retrieve the notification status.
     *
     * @param connection      Database connection
     * @param notificationId  Notification ID to retrieve
     * @return Notification status by notification ID
     * @throws FSEventNotificationException  Exception when retrieving notification status
     */
    boolean getNotificationStatus(Connection connection, String notificationId) throws FSEventNotificationException;

}
