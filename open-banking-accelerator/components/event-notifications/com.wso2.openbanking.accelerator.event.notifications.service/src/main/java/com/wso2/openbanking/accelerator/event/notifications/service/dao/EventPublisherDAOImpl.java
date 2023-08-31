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

import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.dto.NotificationDTO;
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.model.NotificationEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Persisting event notifications to database.
 */
public class EventPublisherDAOImpl implements EventPublisherDAO {

    private static Log log = LogFactory.getLog(EventPublisherDAOImpl.class);
    protected NotificationPublisherSqlStatements sqlStatements;

    public EventPublisherDAOImpl(NotificationPublisherSqlStatements notificationPublisherSqlStatements) {
        this.sqlStatements = notificationPublisherSqlStatements;
    }

    @Override
    public String persistEventNotification(Connection connection, NotificationDTO notificationDTO,
                                       ArrayList<NotificationEvent> eventsList) throws OBEventNotificationException {

        int result;
        int[] noOfRows;
        String persistEventNotification = sqlStatements.getStoreNotification();
        String persistEvents = sqlStatements.getStoreNotificationEvents();

        try (PreparedStatement persistEventNotificationStmnt =
                     connection.prepareStatement(persistEventNotification);
             PreparedStatement persistEventsStmt = connection.prepareStatement(persistEvents)) {

            log.debug("Setting parameters to prepared statement to add event notification ");

            persistEventNotificationStmnt.setString(1, notificationDTO.getNotificationId());
            persistEventNotificationStmnt.setString(2, notificationDTO.getClientId());
            persistEventNotificationStmnt.setString(3, notificationDTO.getResourceId());
            persistEventNotificationStmnt.setString(4, notificationDTO.getStatus());

            // with result, we can determine whether the insertion was successful or not
            result = persistEventNotificationStmnt.executeUpdate();

            // to insert notification events
            for (NotificationEvent event : eventsList) {
                persistEventsStmt.setString(1, notificationDTO.getNotificationId());
                persistEventsStmt.setString(2, event.getEventType());
                persistEventsStmt.setString(3, event.getEventInformation().toString());
                persistEventsStmt.addBatch();
            }
            noOfRows = persistEventsStmt.executeBatch();
        } catch (SQLException e) {
            log.error(EventNotificationConstants.EVENT_NOTIFICATION_CREATION_ERROR, e);
            throw new OBEventNotificationException(EventNotificationConstants.
                    EVENT_NOTIFICATION_CREATION_ERROR, e);
        }
        // Confirm that the data are updated successfully
        if (result > 0 && noOfRows.length != 0) {
            log.info("Created the event notification successfully");
            return notificationDTO.getNotificationId();
        } else {
            throw new OBEventNotificationException("Failed to create the event notification.");
        }
    }
}
