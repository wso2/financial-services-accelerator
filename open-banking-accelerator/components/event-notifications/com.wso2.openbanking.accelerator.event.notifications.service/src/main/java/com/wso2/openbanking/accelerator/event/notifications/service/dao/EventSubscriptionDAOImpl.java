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
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.model.EventSubscription;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.sql.Statement.EXECUTE_FAILED;

/**
 * Default EventSubscriptionDAO Impl.
 */
public class EventSubscriptionDAOImpl implements EventSubscriptionDAO {
    private static Log log = LogFactory.getLog(EventSubscriptionDAOImpl.class);

    protected EventSubscriptionSqlStatements sqlStatements;

    public EventSubscriptionDAOImpl(EventSubscriptionSqlStatements sqlStatements) {
        this.sqlStatements = sqlStatements;
    }

    public EventSubscription storeEventSubscription(Connection connection, EventSubscription eventSubscription)
            throws OBEventNotificationException {

        int storeSubscriptionAffectedRows;

        UUID subscriptionId = UUID.randomUUID();
        long unixTime = Instant.now().getEpochSecond();
        eventSubscription.setSubscriptionId(subscriptionId.toString());
        eventSubscription.setTimeStamp(unixTime);
        eventSubscription.setStatus(EventNotificationConstants.CREATED);

        final String sql = sqlStatements.storeEventSubscriptionQuery();
        try (PreparedStatement storeEventSubscriptionStatement = connection.prepareStatement(sql)) {
            String driverName = connection.getMetaData().getDriverName();
            storeEventSubscriptionStatement.setString(1, eventSubscription.getSubscriptionId());
            storeEventSubscriptionStatement.setString(2, eventSubscription.getClientId());
            storeEventSubscriptionStatement.setString(3, eventSubscription.getCallbackUrl());
            storeEventSubscriptionStatement.setLong(4, eventSubscription.getTimeStamp());
            storeEventSubscriptionStatement.setString(5, eventSubscription.getSpecVersion());
            storeEventSubscriptionStatement.setString(6, eventSubscription.getStatus());
            if (driverName.contains(EventNotificationConstants.POSTGRE_SQL)) {
                storeEventSubscriptionStatement.setObject(7, eventSubscription.getRequestData(),
                        java.sql.Types.OTHER);
            } else {
                storeEventSubscriptionStatement.setString(7, eventSubscription.getRequestData());
            }
            storeSubscriptionAffectedRows = storeEventSubscriptionStatement.executeUpdate();
            if (storeSubscriptionAffectedRows == 0) {
                log.error("Failed to store the event notification subscription.");
                throw new OBEventNotificationException(EventNotificationConstants.ERROR_STORING_EVENT_SUBSCRIPTION);
            }
        } catch (SQLException e) {
            log.error("SQL exception when storing the event types of the subscription", e);
            throw new OBEventNotificationException(EventNotificationConstants.ERROR_STORING_EVENT_SUBSCRIPTION);
        }
        return eventSubscription;
    }

    @Override
    public List<String> storeSubscribedEventTypes(Connection connection, String subscriptionId, List<String> eventTypes)
            throws OBEventNotificationException {

        final String sql = sqlStatements.storeSubscribedEventTypesQuery();
        try (PreparedStatement storeSubscribedEventTypesStatement = connection.prepareStatement(sql)) {
            for (String eventType : eventTypes) {
                storeSubscribedEventTypesStatement.setString(1, subscriptionId);
                storeSubscribedEventTypesStatement.setString(2, eventType);
                storeSubscribedEventTypesStatement.addBatch();
            }
            int[] storeSubscribedEventTypesAffectedRows = storeSubscribedEventTypesStatement.executeBatch();
            for (int affectedRows : storeSubscribedEventTypesAffectedRows) {
                if (affectedRows == 0 || affectedRows == EXECUTE_FAILED) {
                    log.error("Failed to store the subscribed event types.");
                    throw new OBEventNotificationException(EventNotificationConstants.ERROR_STORING_EVENT_SUBSCRIPTION);
                }
            }
        } catch (SQLException e) {
            log.error("SQL exception when storing the subscribed event types.", e);
            throw new OBEventNotificationException(EventNotificationConstants.ERROR_STORING_EVENT_SUBSCRIPTION);
        }
        log.debug("Stored the subscribed event types successfully.");
        return eventTypes;

    }

    @Override
    public EventSubscription getEventSubscriptionBySubscriptionId(Connection connection, String subscriptionId)
            throws OBEventNotificationException {
        EventSubscription retrievedSubscription = new EventSubscription();
        List<String> eventTypes = new ArrayList<>();

        final String sql = sqlStatements.getEventSubscriptionBySubscriptionIdQuery();
        try (PreparedStatement getEventSubscriptionBySubscriptionIdStatement = connection.prepareStatement(sql)) {
            getEventSubscriptionBySubscriptionIdStatement.setString(1, subscriptionId);
            try (ResultSet resultSet = getEventSubscriptionBySubscriptionIdStatement.executeQuery()) {
                if (resultSet.next()) {
                    mapResultSetToEventSubscription(retrievedSubscription, resultSet);
                    resultSet.beforeFirst(); // Reset the cursor position to the beginning of the result set.
                    while (resultSet.next()) {
                        String eventType = resultSet.getString(EventNotificationConstants.EVENT_TYPE);
                        if (eventType != null) {
                            eventTypes.add(eventType);
                        }
                    }
                    if (!eventTypes.isEmpty()) {
                        retrievedSubscription.setEventTypes(eventTypes);
                    }
                } else {
                    log.error("No event notification subscription found for the given subscription id.");
                    throw new OBEventNotificationException(
                            EventNotificationConstants.EVENT_SUBSCRIPTION_NOT_FOUND);
                }
            } catch (SQLException e) {
                log.error("SQL exception when retrieving the event notification subscription.", e);
                throw new OBEventNotificationException(EventNotificationConstants.ERROR_RETRIEVING_EVENT_SUBSCRIPTION);
            }
        } catch (SQLException e) {
            log.error("SQL exception when retrieving the event notification subscription.", e);
            throw new OBEventNotificationException(EventNotificationConstants.ERROR_RETRIEVING_EVENT_SUBSCRIPTION);
        }
        return retrievedSubscription;
    }

    @Override
    public List<EventSubscription> getEventSubscriptionsByClientId(Connection connection, String clientId)
            throws OBEventNotificationException {
        List<EventSubscription> retrievedSubscriptions = new ArrayList<>();

        final String sql = sqlStatements.getEventSubscriptionsByClientIdQuery();
        try (PreparedStatement getEventSubscriptionsByClientIdStatement = connection.prepareStatement(sql)) {
            getEventSubscriptionsByClientIdStatement.setString(1, clientId);
            try (ResultSet resultSet = getEventSubscriptionsByClientIdStatement.executeQuery()) {
                if (resultSet.isBeforeFirst()) {
                    while (resultSet.next()) {
                        EventSubscription eventSubscription = new EventSubscription();
                        List<String> eventTypes = new ArrayList<>();
                        mapResultSetToEventSubscription(eventSubscription, resultSet);
                        resultSet.previous();
                        while (resultSet.next()) {
                            if (eventSubscription.getSubscriptionId().equals(resultSet.
                                    getString(EventNotificationConstants.SUBSCRIPTION_ID))) {
                                if (resultSet.getString(EventNotificationConstants.EVENT_TYPE) != null) {
                                    eventTypes.add(resultSet.getString(EventNotificationConstants.EVENT_TYPE));
                                }
                            } else {
                                resultSet.previous();
                                break;
                            }
                        }
                        if (!eventTypes.isEmpty()) {
                            eventSubscription.setEventTypes(eventTypes);
                        }
                        retrievedSubscriptions.add(eventSubscription);
                    }
                    log.debug("Retrieved the event notification subscriptions successfully.");
                }
                return retrievedSubscriptions;
            } catch (SQLException e) {
                log.error("SQL exception when retrieving the event notification subscriptions.", e);
                throw new OBEventNotificationException(EventNotificationConstants.ERROR_RETRIEVING_EVENT_SUBSCRIPTION);
            }
        } catch (SQLException e) {
            log.error("SQL exception when retrieving the event notification subscriptions.", e);
            throw new OBEventNotificationException(EventNotificationConstants.ERROR_RETRIEVING_EVENT_SUBSCRIPTIONS);
        }
    }

    @Override
    public List<EventSubscription> getEventSubscriptionsByEventType(Connection connection, String eventType)
            throws OBEventNotificationException {
        List<EventSubscription> retrievedSubscriptions = new ArrayList<>();

        final String sql = sqlStatements.getEventSubscriptionsByEventTypeQuery();
        try (PreparedStatement getEventSubscriptionsByClientIdAndEventTypeStatement =
                     connection.prepareStatement(sql)) {
            getEventSubscriptionsByClientIdAndEventTypeStatement.setString(1, eventType);
            try (ResultSet resultSet = getEventSubscriptionsByClientIdAndEventTypeStatement.executeQuery()) {
                if (resultSet.isBeforeFirst()) {
                    while (resultSet.next()) {
                        EventSubscription eventSubscription = new EventSubscription();
                        List<String> eventTypes = new ArrayList<>();
                        mapResultSetToEventSubscription(eventSubscription, resultSet);
                        resultSet.previous();
                        while (resultSet.next()) {
                            if (eventSubscription.getSubscriptionId().equals(resultSet.
                                    getString(EventNotificationConstants.SUBSCRIPTION_ID))) {
                                if (resultSet.getString(EventNotificationConstants.EVENT_TYPE) != null) {
                                    eventTypes.add(resultSet.getString(EventNotificationConstants.EVENT_TYPE));
                                }
                            } else {
                                resultSet.previous();
                                break;
                            }
                        }
                        if (!eventTypes.isEmpty()) {
                            eventSubscription.setEventTypes(eventTypes);
                        }
                        retrievedSubscriptions.add(eventSubscription);
                    }
                    log.debug("Retrieved the event notification subscriptions successfully.");
                }
                return retrievedSubscriptions;
            } catch (SQLException e) {
                log.error("SQL exception when retrieving the event notification subscriptions.", e);
                throw new OBEventNotificationException(EventNotificationConstants.ERROR_RETRIEVING_EVENT_SUBSCRIPTION);
            }
        } catch (SQLException e) {
            log.error("SQL exception when retrieving the event notification subscriptions.", e);
            throw new OBEventNotificationException(EventNotificationConstants.ERROR_RETRIEVING_EVENT_SUBSCRIPTIONS);
        }
    }

    @Override
    public Boolean updateEventSubscription(Connection connection, EventSubscription eventSubscription)
            throws OBEventNotificationException {
        boolean isUpdated = false;
        final String sql = sqlStatements.updateEventSubscriptionQuery();
        try (PreparedStatement updateEventSubscriptionStatement = connection.prepareStatement(sql)) {
            updateEventSubscriptionStatement.setString(1, eventSubscription.getCallbackUrl());
            updateEventSubscriptionStatement.setLong(2, Instant.now().getEpochSecond());
            updateEventSubscriptionStatement.setString(3, eventSubscription.getRequestData());
            updateEventSubscriptionStatement.setString(4, eventSubscription.getSubscriptionId());
            int affectedRows = updateEventSubscriptionStatement.executeUpdate();
            if (affectedRows > 0) {
                log.debug("Event notification subscription is successfully updated.");
                isUpdated = true;
            }
        } catch (SQLException e) {
            log.error("SQL exception when updating event notification subscription", e);
            throw new OBEventNotificationException(EventNotificationConstants.ERROR_UPDATING_EVENT_SUBSCRIPTION);
        }
        return isUpdated;
    }

    @Override
    public Boolean deleteEventSubscription(Connection connection, String subscriptionId)
            throws OBEventNotificationException {

        final String sql = sqlStatements.updateEventSubscriptionStatusQuery();
        try (PreparedStatement deleteEventSubscriptionStatement = connection.prepareStatement(sql)) {
            deleteEventSubscriptionStatement.setString(1, "DELETED");
            deleteEventSubscriptionStatement.setString(2, subscriptionId);
            int affectedRows = deleteEventSubscriptionStatement.executeUpdate();
            if (affectedRows == 0) {
                log.debug("Failed deleting event notification subscription.");
                return false;
            }
            log.debug("Event notification subscription is successfully deleted from the database.");
            return true;
        } catch (SQLException e) {
            log.error("SQL exception when deleting event notification subscription data.", e);
            throw new OBEventNotificationException(EventNotificationConstants.ERROR_DELETING_EVENT_SUBSCRIPTION);
        }
    }

    @Override
    public Boolean deleteSubscribedEventTypes(Connection connection, String subscriptionId)
            throws OBEventNotificationException {
        boolean isDeleted = false;
        int affectedRowsCount;
        final String deleteEventTypesQuery = sqlStatements.deleteSubscribedEventTypesQuery();
        try (PreparedStatement deleteEventTypesStatement = connection.prepareStatement(deleteEventTypesQuery)) {
            deleteEventTypesStatement.setString(1, subscriptionId);
            affectedRowsCount = deleteEventTypesStatement.executeUpdate();
            if (affectedRowsCount > 0) {
                log.debug("Successfully deleted the subscribed event types");
                isDeleted = true;
            }
        } catch (SQLException e) {
            log.error("SQL exception when deleting subscribed event types. ", e);
            throw new OBEventNotificationException(
                    "Error occurred while deleting the event notification subscription.");
        }
        return isDeleted;
    }

    private void mapResultSetToEventSubscription(EventSubscription response, ResultSet resultSet) throws SQLException {
        response.setSubscriptionId(resultSet.getString(EventNotificationConstants.SUBSCRIPTION_ID));
        response.setClientId(resultSet.getString(EventNotificationConstants.CLIENT_ID));
        response.setCallbackUrl(resultSet.getString(EventNotificationConstants.CALLBACK_URL));
        response.setTimeStamp(resultSet.getLong(EventNotificationConstants.TIME_STAMP));
        response.setSpecVersion(resultSet.getString(EventNotificationConstants.SPEC_VERSION));
        response.setStatus(resultSet.getString(EventNotificationConstants.STATUS));
        response.setRequestData(resultSet.getString(EventNotificationConstants.REQUEST));
    }
}
