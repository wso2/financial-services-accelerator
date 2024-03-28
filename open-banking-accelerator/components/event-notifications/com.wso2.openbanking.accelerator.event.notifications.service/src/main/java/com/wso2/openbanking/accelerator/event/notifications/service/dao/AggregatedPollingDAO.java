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

package com.wso2.openbanking.accelerator.event.notifications.service.dao;

import com.wso2.openbanking.accelerator.event.notifications.service.dto.NotificationDTO;
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.model.NotificationError;
import com.wso2.openbanking.accelerator.event.notifications.service.model.NotificationEvent;

import java.util.List;
import java.util.Map;

/**
 * Aggregated Polling DAO impl.
 */
public interface AggregatedPollingDAO {

    /**
     * This method is to update the notification status by ID, allowed values are.
     * OPEN,ACK and ERR
     *
     * @param notificationId     Notification ID to update
     * @param notificationStatus Notification status to update
     * @return Update is success or not
     * @throws OBEventNotificationException  Exception when updating notification status by ID
     */
    Boolean updateNotificationStatusById(String notificationId, String notificationStatus)
            throws OBEventNotificationException;

    /**
     * This method is to store event notifications error details in the OB_NOTIFICATION table.
     *
     * @param notificationError  Notification error details
     * @return Stored event notifications error details
     * @throws OBEventNotificationException  Exception when storing event notifications error details
     */
    Map<String, NotificationError> storeErrorNotification(NotificationError notificationError)
            throws OBEventNotificationException;

    /**
     * This method is to retrieve given number of notifications in the OB_NOTIFICATION table by client and status.
     *
     * @param clientId  Client ID to retrieve notifications
     * @param status    Notification status to retrieve
     * @param max       Maximum number of notifications to retrieve
     * @return List of notifications by client and status
     * @throws OBEventNotificationException  Exception when retrieving notifications by client ID and status
     */
    List<NotificationDTO> getNotificationsByClientIdAndStatus(String clientId, String
            status, int max) throws OBEventNotificationException;

    /**
     * This method is to retrieve notifications by NotificationID.
     *
     * @param notificationId  Notification ID to retrieve
     * @return List of notifications by notification ID
     * @throws OBEventNotificationException  Exception when retrieving notifications by notification ID
     */
    List<NotificationEvent> getEventsByNotificationID(String notificationId) throws OBEventNotificationException;

    /**
     * This method is to retrieve notifications in the OB_NOTIFICATION table by status.
     *
     * @param status  Notification status to retrieve
     * @return List of notifications by status
     * @throws OBEventNotificationException  Exception when retrieving notifications by status
     */
    List<NotificationDTO> getNotificationsByStatus(String status) throws OBEventNotificationException;

    /**
     * This method is to retrieve notificationsCount by ClientId and Status.
     *
     * @param clientId    Client ID to retrieve notifications
     * @param eventStatus Notification status to retrieve
     * @return List of notifications by status and client id
     * @throws OBEventNotificationException  Exception when retrieving notification count by client ID and status
     */
    int getNotificationCountByClientIdAndStatus(String clientId, String eventStatus)
            throws OBEventNotificationException;

    /**
     * This method is to retrieve the notification status.
     *
     * @param notificationId  Notification ID to retrieve
     * @return Notification status by notification ID
     * @throws OBEventNotificationException  Exception when retrieving notification status
     */
    boolean getNotificationStatus(String notificationId) throws OBEventNotificationException;
}
