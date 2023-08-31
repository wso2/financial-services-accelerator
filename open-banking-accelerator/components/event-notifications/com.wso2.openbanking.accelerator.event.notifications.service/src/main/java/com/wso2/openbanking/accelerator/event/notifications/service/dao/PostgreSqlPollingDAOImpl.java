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

import com.wso2.openbanking.accelerator.common.util.DatabaseUtil;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.dto.NotificationDTO;
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.model.NotificationEvent;
import com.wso2.openbanking.accelerator.event.notifications.service.util.EventNotificationServiceUtil;
import net.minidev.json.parser.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSql event polling dao class.
 */
@Generated(message = "Postgres Implementation")
public class PostgreSqlPollingDAOImpl extends AggregatedPollingDAOImpl {

    private static Log log = LogFactory.getLog(PostgreSqlPollingDAOImpl.class);

    public PostgreSqlPollingDAOImpl(NotificationPollingSqlStatements notificationPollingSqlStatements) {
        super(notificationPollingSqlStatements);
    }

    @Override
    public List<NotificationDTO> getNotificationsByClientIdAndStatus(String clientId, String status, int max)
            throws OBEventNotificationException {

        List<NotificationDTO> notificationList;
        Connection connection = DatabaseUtil.getDBConnection();
        try {

            notificationList = new ArrayList<>();

            if (log.isDebugEnabled()) {
                log.debug(String.format(EventNotificationConstants.DB_CONN_ESTABLISHED, clientId));
            }

            final String sql = sqlStatements.getMaxNotificationsQuery();
            try (PreparedStatement getNotificationsPreparedStatement = connection.prepareStatement(sql,
                    ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                getNotificationsPreparedStatement.setString(1, clientId);
                getNotificationsPreparedStatement.setString(2, status);
                getNotificationsPreparedStatement.setInt(3, max);

                try (ResultSet notificationResultSet = getNotificationsPreparedStatement.executeQuery()) {
                    if (notificationResultSet.next()) {

                        //bring pointer back to the top of the result set if not on the top
                        if (!notificationResultSet.isBeforeFirst()) {
                            notificationResultSet.beforeFirst();
                        }

                        //read event notifications from the result set
                        while (notificationResultSet.next()) {
                            NotificationDTO notification = new NotificationDTO();

                            notification.setNotificationId(notificationResultSet.getString
                                    (EventNotificationConstants.NOTIFICATION_ID));
                            notification.setClientId(notificationResultSet.getString
                                    (EventNotificationConstants.CLIENT_ID));
                            notification.setResourceId(notificationResultSet.getString
                                    (EventNotificationConstants.RESOURCE_ID));
                            notification.setStatus(notificationResultSet.getString
                                    (EventNotificationConstants.STATUS));
                            notification.setUpdatedTimeStamp((notificationResultSet.getTimestamp(
                                    (EventNotificationConstants.UPDATED_TIMESTAMP)).getTime()));

                            notificationList.add(notification);
                        }
                        notificationResultSet.close();
                        getNotificationsPreparedStatement.close();

                        if (log.isDebugEnabled()) {
                            log.debug(String.format(EventNotificationConstants.RETRIEVED_NOTIFICATION_CLIENT,
                                    clientId));
                        }

                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug(String.format(EventNotificationConstants.NO_NOTIFICATIONS_FOUND_CLIENT,
                                    clientId));
                        }
                    }
                }
            } catch (SQLException e) {
                throw new OBEventNotificationException(String.format
                        (EventNotificationConstants.DB_ERROR_NOTIFICATION_RETRIEVE,
                                clientId), e);
            }
        } finally {
            log.debug(EventNotificationConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
        return notificationList;
    }

    @Override
    public List<NotificationEvent> getEventsByNotificationID(String notificationId)
            throws OBEventNotificationException {

        List<NotificationEvent> eventList = new ArrayList<>();

        Connection connection = DatabaseUtil.getDBConnection();
        try {

            final String sql = sqlStatements.getEventsByNotificationIdQuery();

            try (PreparedStatement getEventsPreparedStatement = connection.prepareStatement(sql,
                    ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {

                getEventsPreparedStatement.setString(1, notificationId);

                try (ResultSet eventsResultSet = getEventsPreparedStatement.executeQuery()) {
                    if (eventsResultSet.next()) {

                        //bring pointer back to the top of the result set if not on the top
                        if (!eventsResultSet.isBeforeFirst()) {
                            eventsResultSet.beforeFirst();
                        }

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
                        eventsResultSet.close();
                        getEventsPreparedStatement.close();

                        if (log.isDebugEnabled()) {
                            log.debug(String.format(EventNotificationConstants.RETRIEVED_EVENTS_NOTIFICATION,
                                    notificationId));
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug(String.format(EventNotificationConstants.NO_EVENTS_NOTIFICATION_ID,
                                    notificationId));
                        }
                    }
                } catch (ParseException e) {
                    log.error(String.format(EventNotificationConstants.PARSE_ERROR_NOTIFICATION_ID, notificationId), e);
                    throw new OBEventNotificationException(String.format (
                            EventNotificationConstants.PARSE_ERROR_NOTIFICATION_ID, notificationId), e);
                }
            } catch (SQLException e) {
                log.error(String.format(EventNotificationConstants.DB_ERROR_EVENTS_RETRIEVE, notificationId), e);
                throw new OBEventNotificationException(String.format
                        (EventNotificationConstants.DB_ERROR_EVENTS_RETRIEVE, notificationId), e);
            }

        } finally {
            log.debug(EventNotificationConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }

        return eventList;
    }

    @Override
    public List<NotificationDTO> getNotificationsByStatus(String status)
            throws OBEventNotificationException {
        List<NotificationDTO> notificationList;
        Connection connection = DatabaseUtil.getDBConnection();
        try {
            notificationList = new ArrayList<>();
            final String sql = sqlStatements.getMaxNotificationsQuery();
            try (PreparedStatement getNotificationsPreparedStatement = connection.prepareStatement(sql,
                    ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                getNotificationsPreparedStatement.setString(1, status);
                try (ResultSet notificationResultSet = getNotificationsPreparedStatement.executeQuery()) {
                    if (notificationResultSet.next()) {
                        //bring pointer back to the top of the result set if not on the top
                        if (!notificationResultSet.isBeforeFirst()) {
                            notificationResultSet.beforeFirst();
                        }
                        //read event notifications from the result set
                        while (notificationResultSet.next()) {
                            NotificationDTO notification = new NotificationDTO();
                            notification.setNotificationId(notificationResultSet.getString
                                    (EventNotificationConstants.NOTIFICATION_ID));
                            notification.setClientId(notificationResultSet.getString
                                    (EventNotificationConstants.CLIENT_ID));
                            notification.setResourceId(notificationResultSet.getString
                                    (EventNotificationConstants.RESOURCE_ID));
                            notification.setStatus(notificationResultSet.getString
                                    (EventNotificationConstants.STATUS));
                            notification.setUpdatedTimeStamp((notificationResultSet.getTimestamp(
                                    (EventNotificationConstants.UPDATED_TIMESTAMP)).getTime()));
                            notificationList.add(notification);
                        }
                        notificationResultSet.close();
                        getNotificationsPreparedStatement.close();
                        if (log.isDebugEnabled()) {
                            log.debug(
                                    EventNotificationConstants.RETRIEVED_NOTIFICATION_CLIENT);
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug(
                                    EventNotificationConstants.NO_NOTIFICATIONS_FOUND_CLIENT);
                        }
                    }
                }
            } catch (SQLException e) {
                throw new OBEventNotificationException(
                        EventNotificationConstants.DB_ERROR_NOTIFICATION_RETRIEVE, e);
            }
        } finally {
            log.debug(EventNotificationConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
        return notificationList;
    }
}
