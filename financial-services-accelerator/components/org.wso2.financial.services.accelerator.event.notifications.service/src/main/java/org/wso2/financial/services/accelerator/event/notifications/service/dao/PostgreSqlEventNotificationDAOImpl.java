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
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.model.Notification;
import org.wso2.financial.services.accelerator.event.notifications.service.model.NotificationEvent;
import org.wso2.financial.services.accelerator.event.notifications.service.queries.EventNotificationSqlStatements;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationServiceUtil;

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
public class PostgreSqlEventNotificationDAOImpl extends EventNotificationDAOImpl {

    private static Log log = LogFactory.getLog(PostgreSqlEventNotificationDAOImpl.class);

    public PostgreSqlEventNotificationDAOImpl(EventNotificationSqlStatements eventNotificationSqlStatements) {
        super(eventNotificationSqlStatements);
    }

    @Override
    public List<Notification> getNotificationsByClientIdAndStatus(Connection connection, String clientId,
                                                                  String status, int max)
            throws FSEventNotificationException {

        List<Notification> notificationList = new ArrayList<>();
        String sql = sqlStatements.getMaxNotificationsQuery();

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
                        Notification notification = new Notification();

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
                    (EventNotificationConstants.DB_ERROR_NOTIFICATION_RETRIEVE,
                            clientId), e);
        }
        return notificationList;
    }

    @Override
    public List<NotificationEvent> getEventsByNotificationID(Connection connection, String notificationId)
            throws FSEventNotificationException {

        List<NotificationEvent> eventList = new ArrayList<>();
        String sql = sqlStatements.getEventsByNotificationIdQuery();

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
                                notificationId.replaceAll("[\r\n]", "")));
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format(EventNotificationConstants.NO_EVENTS_NOTIFICATION_ID,
                                notificationId.replaceAll("[\r\n]", "")));
                    }
                }
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
        String sql = sqlStatements.getMaxNotificationsQuery();

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
                        Notification notification = new Notification();
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
            throw new FSEventNotificationException(
                    EventNotificationConstants.DB_ERROR_NOTIFICATION_RETRIEVE, e);
        }
        return notificationList;
    }
}
