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
     * @param notificationId
     * @param notificationStatus
     * @return
     * @throws OBEventNotificationException
     */
    Boolean updateNotificationStatusById(String notificationId, String notificationStatus)
            throws OBEventNotificationException;

    /**
     * This method is to store event notifications in the OB_NOTIFICATION table.
     * @param notificationError
     * @return
     * @throws OBEventNotificationException
     */
    Map<String, NotificationError> storeErrorNotification(NotificationError notificationError)
            throws OBEventNotificationException;

    /**
     * This method is to retrieve given number of notifications in the OB_NOTIFICATION table by client and status.
     * @param clientId
     * @param status
     * @param max
     * @return
     * @throws OBEventNotificationException
     */
    List<NotificationDTO> getNotificationsByClientIdAndStatus(String clientId, String
            status, int max) throws OBEventNotificationException;

    /**
     * This method is to retrieve notifications by NotificationID.
     * @param notificationId
     *
     * @return
     * @throws OBEventNotificationException
     */
    List<NotificationEvent> getEventsByNotificationID(String notificationId) throws OBEventNotificationException;

    /**
     * This method is to retrieve notifications in the OB_NOTIFICATION table by status.
     * @param status
     *
     * @return List<NotificationDTO>
     * @throws OBEventNotificationException
     */
    List<NotificationDTO> getNotificationsByStatus(String status) throws OBEventNotificationException;

    /**
     * This method is to retrieve notificationsCount by ClientId and Status.
     * @param clientId
     * @param eventStatus
     *
     * @return
     * @throws OBEventNotificationException
     */
    int getNotificationCountByClientIdAndStatus(String clientId, String eventStatus)
            throws OBEventNotificationException;

    /**
     * This method is to retrieve the notification status.
     * @param notificationId
     *
     * @return
     * @throws OBEventNotificationException
     */
    boolean getNotificationStatus(String notificationId) throws OBEventNotificationException;
}
