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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.model.Notification;
import org.wso2.financial.services.accelerator.event.notifications.service.model.NotificationError;
import org.wso2.financial.services.accelerator.event.notifications.service.model.NotificationEvent;
import org.wso2.financial.services.accelerator.event.notifications.service.queries.EventNotificationSqlStatements;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationServiceUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Persisting event notifications to database.
 */
public class EventNotificationDAOImpl implements EventNotificationDAO {

    private static Log log = LogFactory.getLog(EventNotificationDAOImpl.class);
    protected EventNotificationSqlStatements sqlStatements;

    public EventNotificationDAOImpl(EventNotificationSqlStatements eventNotificationSqlStatements) {
        this.sqlStatements = eventNotificationSqlStatements;
    }

    @Override
    public String persistEventNotification(Connection connection, Notification notification,
                                       ArrayList<NotificationEvent> eventsList)
            throws FSEventNotificationException {

        int result;
        int[] noOfRows;
        String persistEventNotification = sqlStatements.getStoreNotification();
        String persistEvents = sqlStatements.getStoreNotificationEvents();

        long currentTimestamp = System.currentTimeMillis() / 1000;

        try (PreparedStatement persistEventNotificationStmnt =
                     connection.prepareStatement(persistEventNotification);
             PreparedStatement persistEventsStmt = connection.prepareStatement(persistEvents)) {

            log.debug("Setting parameters to prepared statement to add event notification ");

            persistEventNotificationStmnt.setString(1, notification.getNotificationId());
            persistEventNotificationStmnt.setString(2, notification.getClientId());
            persistEventNotificationStmnt.setString(3, notification.getResourceId());
            persistEventNotificationStmnt.setString(4, notification.getStatus());
            persistEventNotificationStmnt.setLong(5, currentTimestamp);

            // with result, we can determine whether the insertion was successful or not
            result = persistEventNotificationStmnt.executeUpdate();

            // to insert notification events
            for (NotificationEvent event : eventsList) {
                persistEventsStmt.setString(1, notification.getNotificationId());
                persistEventsStmt.setString(2, event.getEventType());
                persistEventsStmt.setString(3, event.getEventInformation().toString());
                persistEventsStmt.addBatch();
            }
            noOfRows = persistEventsStmt.executeBatch();
        } catch (SQLException e) {
            log.error(EventNotificationConstants.EVENT_NOTIFICATION_CREATION_ERROR, e);
            throw new FSEventNotificationException(EventNotificationConstants.
                    EVENT_NOTIFICATION_CREATION_ERROR, e);
        }
        // Confirm that the data are updated successfully
        if (result > 0 && noOfRows.length != 0) {
            log.info("Created the event notification successfully");
            return notification.getNotificationId();
        } else {
            throw new FSEventNotificationException("Failed to create the event notification.");
        }
    }

    @Override
    public void updateNotificationStatusById(Connection connection, String notificationId, String notificationStatus)
            throws FSEventNotificationException {

        String sql = sqlStatements.updateNotificationStatusQueryById();

        try (PreparedStatement updateNotificationStatusById = connection.prepareStatement(sql)) {
            long currentTimestamp = System.currentTimeMillis() / 1000;
            updateNotificationStatusById.setString(1, notificationStatus);
            updateNotificationStatusById.setLong(2, currentTimestamp);
            updateNotificationStatusById.setString(3, notificationId);

            int affectedRows = updateNotificationStatusById.executeUpdate();

            if (affectedRows != 0) {
                connection.commit();
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Updated notification with Notification ID '%s'",
                            notificationId.replaceAll("[\r\n]", "")));
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Failed updating notification with ID : '%s'",
                            notificationId.replaceAll("[\r\n]", "")));
                }
                throw new FSEventNotificationException(String.format(EventNotificationConstants.DB_ERROR_UPDATING,
                        notificationId));
            }
        } catch (SQLException e) {
            log.error(String.format(EventNotificationConstants.DB_ERROR_UPDATING,
                    notificationId.replaceAll("[\r\n]", "")), e);
            throw new FSEventNotificationException(String.format(EventNotificationConstants.DB_ERROR_UPDATING,
                    notificationId));
        }
    }

    @Override
    public void storeErrorNotification(Connection connection, NotificationError notificationError)
            throws FSEventNotificationException {

        String storeErrorNotificationQuery = sqlStatements.storeErrorNotificationQuery();

        try (PreparedStatement storeErrorNotificationPreparedStatement =
                     connection.prepareStatement(storeErrorNotificationQuery)) {

            storeErrorNotificationPreparedStatement.setString(1, notificationError.
                    getNotificationId());
            storeErrorNotificationPreparedStatement.setString(2, notificationError.
                    getErrorCode());
            storeErrorNotificationPreparedStatement.setString(3, notificationError.
                    getErrorDescription());

            int affectedRows = storeErrorNotificationPreparedStatement.executeUpdate();
            if (affectedRows == 1) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Successfully stored error notification with ID:'%s'.",
                            notificationError.getNotificationId().replaceAll("[\r\n]", "")));
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Failed store error notification with ID:'%s'.",
                            notificationError.getNotificationId().replaceAll("[\r\n]", "")));
                }
                throw new FSEventNotificationException(EventNotificationConstants.
                        DB_FAILED_ERROR_NOTIFICATION_STORING + notificationError.getNotificationId());
            }

        } catch (SQLException e) {
            throw new FSEventNotificationException(EventNotificationConstants.
                    DB_ERROR_STORING_ERROR_NOTIFICATION, e);
        }
    }

    @Override
    public List<Notification> getNotificationsByClientIdAndStatus(Connection connection, String clientId,
                                                                  String status, int max)
            throws FSEventNotificationException {

        List<Notification> notificationList = new ArrayList<>();
        String sql = sqlStatements.getMaxNotificationsQuery();

        try (PreparedStatement getNotificationsPreparedStatement = connection.prepareStatement(sql)) {
            getNotificationsPreparedStatement.setString(1, clientId);
            getNotificationsPreparedStatement.setString(2, status);
            getNotificationsPreparedStatement.setInt(3, max);

            try (ResultSet notificationResultSet = getNotificationsPreparedStatement.executeQuery()) {
                if (notificationResultSet.isBeforeFirst()) {

                    //read event notifications from the result set
                    while (notificationResultSet.next()) {
                        Notification notification = new Notification();

                        notification.setNotificationId(notificationResultSet.getString
                                (EventNotificationConstants.NOTIFICATION_ID));
                        notification.setClientId(notificationResultSet.getString
                                (EventNotificationConstants.CLIENT_ID));
                        notification.setResourceId(notificationResultSet.getString
                                (EventNotificationConstants.RESOURCE_ID));
                        notification.setStatus(notificationResultSet.getString
                                (EventNotificationConstants.STATUS));
                        notification.setUpdatedTimeStamp((notificationResultSet.getLong(
                                (EventNotificationConstants.UPDATED_TIMESTAMP))));

                        notificationList.add(notification);
                    }

                    if (log.isDebugEnabled()) {
                        log.debug(String.format(EventNotificationConstants.RETRIEVED_NOTIFICATION_CLIENT,
                                clientId.replaceAll("[\r\n]", "")));
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format(EventNotificationConstants.NO_NOTIFICATIONS_FOUND_CLIENT,
                                clientId.replaceAll("[\r\n]", "")));
                    }
                }
            }
        } catch (SQLException e) {
            throw new FSEventNotificationException(String.format
                    (EventNotificationConstants.DB_ERROR_NOTIFICATION_RETRIEVE, clientId), e);
        }

        return notificationList;
    }

    @Override
    public List<NotificationEvent> getEventsByNotificationID(Connection connection, String notificationId)
            throws FSEventNotificationException {

        List<NotificationEvent> eventList = new ArrayList<>();
        String sql = sqlStatements.getEventsByNotificationIdQuery();

        try (PreparedStatement getEventsPreparedStatement = connection.prepareStatement(sql)) {

            getEventsPreparedStatement.setString(1, notificationId);

            try (ResultSet eventsResultSet = getEventsPreparedStatement.executeQuery()) {
                if (eventsResultSet.isBeforeFirst()) {

                    //read event notifications from the result set
                    while (eventsResultSet.next()) {
                        NotificationEvent event = new NotificationEvent();
                        event.setNotificationId(eventsResultSet.getString
                                (EventNotificationConstants.NOTIFICATION_ID));
                        event.setEventType(eventsResultSet.getString
                                (EventNotificationConstants.EVENT_TYPE));
                        event.setEventInformation(EventNotificationServiceUtil.
                                getEventJSONFromString(eventsResultSet.getString
                                        (EventNotificationConstants.EVENT_INFO)));
                        eventList.add(event);
                    }

                    if (log.isDebugEnabled()) {
                        log.debug(String.format(EventNotificationConstants.RETRIEVED_EVENTS_NOTIFICATION,
                                notificationId.replaceAll("[\r\n]", "")));
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format(EventNotificationConstants.NO_EVENTS_NOTIFICATION_ID,
                                notificationId.replaceAll("[\r\n]", "")));
                    }
                }
            } catch (SQLException e) {
                log.error(String.format(EventNotificationConstants.PARSE_ERROR_NOTIFICATION_ID,
                        notificationId.replaceAll("[\r\n]", "")), e);
                throw new FSEventNotificationException(String.format (
                        EventNotificationConstants.PARSE_ERROR_NOTIFICATION_ID, notificationId), e);
            }
        } catch (SQLException e) {
            log.error(String.format(EventNotificationConstants.DB_ERROR_EVENTS_RETRIEVE,
                    notificationId.replaceAll("[\r\n]", "")), e);
            throw new FSEventNotificationException(String.format
                    (EventNotificationConstants.DB_ERROR_EVENTS_RETRIEVE, notificationId), e);
        }


        return eventList;
    }

    @Override
    public List<Notification> getNotificationsByStatus(Connection connection, String status)
            throws FSEventNotificationException {

        List<Notification> notificationList = new ArrayList<>();
        String sql = sqlStatements.getNotificationsByState();
        try (PreparedStatement getNotificationsPreparedStatement = connection.prepareStatement(sql)) {
            getNotificationsPreparedStatement.setString(1, status);

            try (ResultSet notificationResultSet = getNotificationsPreparedStatement.executeQuery()) {
                if (notificationResultSet.isBeforeFirst()) {
                    //read event notifications from the result set
                    while (notificationResultSet.next()) {
                        Notification notification = new Notification();
                        notification.setNotificationId(notificationResultSet.getString
                                (EventNotificationConstants.NOTIFICATION_ID));
                        notification.setClientId(notificationResultSet.getString
                                (EventNotificationConstants.CLIENT_ID));
                        notification.setResourceId(notificationResultSet.getString
                                (EventNotificationConstants.RESOURCE_ID));
                        notification.setStatus(notificationResultSet.getString
                                (EventNotificationConstants.STATUS));
                        notification.setUpdatedTimeStamp((notificationResultSet.getLong(
                                (EventNotificationConstants.UPDATED_TIMESTAMP))));
                        notificationList.add(notification);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug(EventNotificationConstants.RETRIEVED_NOTIFICATION_CLIENT);
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug(EventNotificationConstants.NO_NOTIFICATIONS_FOUND_CLIENT);
                    }
                }
            }
        } catch (SQLException e) {
            throw new FSEventNotificationException(EventNotificationConstants.DB_ERROR_NOTIFICATION_RETRIEVE, e);
        }

        return notificationList;
    }

    @Override
    public int getNotificationCountByClientIdAndStatus(Connection connection, String clientId, String eventStatus)
            throws FSEventNotificationException {

        String sql = sqlStatements.getNotificationsCountQuery();

        try (PreparedStatement getNotificationCount = connection.prepareStatement(sql)) {

            getNotificationCount.setString(1, clientId);
            getNotificationCount.setString(2, eventStatus);

            try (ResultSet notificationCountResultSet = getNotificationCount.executeQuery()) {
                if (notificationCountResultSet.next()) {

                    int count = notificationCountResultSet.getInt("NOTIFICATION_COUNT");
                    notificationCountResultSet.close();
                    getNotificationCount.close();

                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Retrieved notification count for client ID: '%s'. ",
                                clientId.replaceAll("[\r\n]", "")));
                    }

                    return count;
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format(
                                EventNotificationConstants.NO_NOTIFICATIONS_FOUND_CLIENT,
                                clientId.replaceAll("[\r\n]", "")));
                    }

                    return 0;
                }
            }
        } catch (SQLException e) {
            throw new FSEventNotificationException(String.format
                    (EventNotificationConstants.DB_ERROR_NOTIFICATION_RETRIEVE, clientId), e);
        }
    }

    @Override
    public boolean getNotificationStatus(Connection connection, String notificationId)
            throws FSEventNotificationException {

        boolean isOpenStatus = false;

        String sql = sqlStatements.getNotificationByNotificationId();
        try (PreparedStatement getNotificationStatus = connection.prepareStatement(sql)) {
            getNotificationStatus.setString(1, notificationId);

            try (ResultSet notificationResultSet = getNotificationStatus.executeQuery()) {
                if (notificationResultSet.next()) {

                    if (EventNotificationConstants.OPEN.equals(notificationResultSet.
                            getString("STATUS"))) {
                        isOpenStatus = true;
                    }

                    return isOpenStatus;
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("No notifications found for notification ID - '%s'",
                                notificationId.replaceAll("[\r\n]", "")));
                    }
                }
            }
        } catch (SQLException e) {
            throw new FSEventNotificationException(String.format
                    ("Error occurred while retrieving status for the notifications ID : '%s'.",
                            notificationId), e);
        }

        return false;
    }
}
