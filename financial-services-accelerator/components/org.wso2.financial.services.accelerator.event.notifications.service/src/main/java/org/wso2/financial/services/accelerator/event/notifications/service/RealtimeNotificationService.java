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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.wso2.financial.services.accelerator.common.util.DatabaseUtils;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.dao.EventNotificationDAO;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.model.Notification;
import org.wso2.financial.services.accelerator.event.notifications.service.model.NotificationEvent;
import org.wso2.financial.services.accelerator.event.notifications.service.persistence.EventNotificationStoreInitializer;

import java.sql.Connection;
import java.util.List;

/**
 * Realtime Notification service class.
 */
public class RealtimeNotificationService {

    private static final Log log = LogFactory.getLog(RealtimeNotificationService.class);

    /**
     * Method to retrieve notification by status.
     *
     * @param status      Notification status to retrieve
     * @return List of notifications by status
     * @throws FSEventNotificationException  Exception when retrieving notifications by status
     */
    public List<Notification> getNotificationsByStatus(String status)
            throws FSEventNotificationException {

        EventNotificationDAO eventNotificationDAO = EventNotificationStoreInitializer.getEventNotificationDAO();

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            List<Notification> events = eventNotificationDAO.
                    getNotificationsByStatus(connection, status);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Event Notifications with %s status retrieved successfully.",
                        status.replaceAll("[\r\n]", "")));
            }
            DatabaseUtils.commitTransaction(connection);
            return events;
        } catch (FSEventNotificationException e) {
            log.error("Error while retrieving event notification.", e);
            DatabaseUtils.rollbackTransaction(connection);
            throw new FSEventNotificationException(EventNotificationConstants.ERROR_STORING_EVENT_SUBSCRIPTION, e);
        } finally {
            DatabaseUtils.closeConnection(connection);
        }
    }

    /**
     * Method to retrieve notifications by NotificationID.
     *
     * @param notificationId  Notification ID to retrieve
     * @return List of notifications by notification ID
     * @throws FSEventNotificationException  Exception when retrieving notifications by notification ID
     */
    public List<NotificationEvent> getEventsByNotificationID(String notificationId)
            throws FSEventNotificationException {

        EventNotificationDAO eventNotificationDAO = EventNotificationStoreInitializer.getEventNotificationDAO();

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            //store event subscription data in the database
            List<NotificationEvent> notificationEvents = eventNotificationDAO.
                    getEventsByNotificationID(connection, notificationId);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Event Notifications with notification id %s retrieved successfully.",
                        notificationId.replaceAll("[\r\n]", "")));
            }
            DatabaseUtils.commitTransaction(connection);
            return notificationEvents;
        } catch (FSEventNotificationException e) {
            log.error("Error while retrieving event notification.", e);
            DatabaseUtils.rollbackTransaction(connection);
            throw new FSEventNotificationException(EventNotificationConstants.ERROR_STORING_EVENT_SUBSCRIPTION, e);
        } finally {
            DatabaseUtils.closeConnection(connection);
        }
    }

    /**
     * Method to update the notification status by ID, allowed values are.
     * OPEN,ACK and ERR
     *
     * @param notificationId     Notification ID to update
     * @param notificationStatus Notification status to update
     * @throws FSEventNotificationException  Exception when updating notification status by ID
     */
    public void updateNotificationStatusById(String notificationId, String notificationStatus)
            throws FSEventNotificationException {

        Connection connection = DatabaseUtils.getDBConnection();

        EventNotificationDAO eventNotificationDAO = EventNotificationStoreInitializer.getEventNotificationDAO();

        try {
            //update the stored event notification
            eventNotificationDAO.updateNotificationStatusById(connection, notificationId, notificationStatus);

            log.debug("Event Notification updated successfully.");
            DatabaseUtils.commitTransaction(connection);
        } catch (JSONException e) {
            log.error("Error while Parsing the stored request Object", e);
            throw new FSEventNotificationException("Error while Parsing the stored request Object", e);
        } catch (FSEventNotificationException e) {
            log.error("Error while updating event notification.", e);
            DatabaseUtils.rollbackTransaction(connection);
            throw new FSEventNotificationException(e.getMessage(), e);
        } finally {
            DatabaseUtils.closeConnection(connection);
        }
    }
}
