/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.event.notifications.service.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.model.EventSubscription;
import org.wso2.financial.services.accelerator.event.notifications.service.queries.EventSubscriptionSqlStatements;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Postgres SQL EventSubscriptionDAO Impl.
 */
public class PostgreSqlEventSubscriptionDAOImpl extends EventSubscriptionDAOImpl {

    private static final Log log = LogFactory.getLog(PostgreSqlEventSubscriptionDAOImpl.class);

    public PostgreSqlEventSubscriptionDAOImpl(EventSubscriptionSqlStatements sqlStatements) {
        super(sqlStatements);
    }

    @Override
    public EventSubscription storeEventSubscription(Connection connection, EventSubscription eventSubscription)
            throws FSEventNotificationException {

        int storeSubscriptionAffectedRows;

        UUID subscriptionId = UUID.randomUUID();
        long unixTime = Instant.now().getEpochSecond();
        eventSubscription.setSubscriptionId(subscriptionId.toString());
        eventSubscription.setTimeStamp(unixTime);
        eventSubscription.setStatus(EventNotificationConstants.CREATED);

        final String sql = sqlStatements.storeEventSubscriptionQuery();
        try (PreparedStatement storeEventSubscriptionStatement = connection.prepareStatement(sql)) {
            storeEventSubscriptionStatement.setString(1, eventSubscription.getSubscriptionId());
            storeEventSubscriptionStatement.setString(2, eventSubscription.getClientId());
            storeEventSubscriptionStatement.setString(3, eventSubscription.getCallbackUrl());
            storeEventSubscriptionStatement.setLong(4, eventSubscription.getTimeStamp());
            storeEventSubscriptionStatement.setString(5, eventSubscription.getSpecVersion());
            storeEventSubscriptionStatement.setString(6, eventSubscription.getStatus());
            storeEventSubscriptionStatement.setObject(7, eventSubscription.getRequestData(),
                    java.sql.Types.OTHER);
            storeSubscriptionAffectedRows = storeEventSubscriptionStatement.executeUpdate();
            if (storeSubscriptionAffectedRows == 0) {
                log.error("Failed to store the event notification subscription.");
                throw new FSEventNotificationException(EventNotificationConstants.ERROR_STORING_EVENT_SUBSCRIPTION);
            }
        } catch (SQLException e) {
            log.error("SQL exception when storing the event types of the subscription", e);
            throw new FSEventNotificationException(EventNotificationConstants.ERROR_STORING_EVENT_SUBSCRIPTION);
        }
        return eventSubscription;
    }

    @Override
    public List<EventSubscription> getEventSubscriptionsByClientId(Connection connection, String clientId)
            throws FSEventNotificationException {
        List<EventSubscription> retrievedSubscriptions = new ArrayList<>();

        final String sql = sqlStatements.getEventSubscriptionsByClientIdQuery();
        try (PreparedStatement getEventSubscriptionsByClientIdStatement = connection.prepareStatement(sql,
                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
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
                throw new FSEventNotificationException(EventNotificationConstants.ERROR_RETRIEVING_EVENT_SUBSCRIPTION);
            }
        } catch (SQLException e) {
            log.error("SQL exception when retrieving the event notification subscriptions.", e);
            throw new FSEventNotificationException(EventNotificationConstants.ERROR_RETRIEVING_EVENT_SUBSCRIPTIONS);
        }
    }

    @Override
    public EventSubscription getEventSubscriptionBySubscriptionId(Connection connection, String subscriptionId)
            throws FSEventNotificationException {
        EventSubscription retrievedSubscription = new EventSubscription();
        List<String> eventTypes = new ArrayList<>();

        final String sql = sqlStatements.getEventSubscriptionBySubscriptionIdQuery();
        try (PreparedStatement getEventSubscriptionBySubscriptionIdStatement = connection.prepareStatement(sql,
                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
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
                    throw new FSEventNotificationException(
                            EventNotificationConstants.EVENT_SUBSCRIPTION_NOT_FOUND);
                }
            } catch (SQLException e) {
                log.error("SQL exception when retrieving the event notification subscription.", e);
                throw new FSEventNotificationException(EventNotificationConstants.ERROR_RETRIEVING_EVENT_SUBSCRIPTION);
            }
        } catch (SQLException e) {
            log.error("SQL exception when retrieving the event notification subscription.", e);
            throw new FSEventNotificationException(EventNotificationConstants.ERROR_RETRIEVING_EVENT_SUBSCRIPTION);
        }
        return retrievedSubscription;
    }

    @Override
    public Boolean updateEventSubscription(Connection connection, EventSubscription eventSubscription)
            throws FSEventNotificationException {
        boolean isUpdated = false;
        final String sql = sqlStatements.updateEventSubscriptionQuery();
        try (PreparedStatement updateEventSubscriptionStatement = connection.prepareStatement(sql)) {
            updateEventSubscriptionStatement.setString(1, eventSubscription.getCallbackUrl());
            updateEventSubscriptionStatement.setLong(2, Instant.now().getEpochSecond());
            updateEventSubscriptionStatement.setObject(3, eventSubscription.getRequestData(),
                    java.sql.Types.OTHER);
            updateEventSubscriptionStatement.setString(4, eventSubscription.getSubscriptionId());
            int affectedRows = updateEventSubscriptionStatement.executeUpdate();
            if (affectedRows > 0) {
                log.debug("Event notification subscription is successfully updated.");
                isUpdated = true;
            }
        } catch (SQLException e) {
            log.error("SQL exception when updating event notification subscription", e);
            throw new FSEventNotificationException(EventNotificationConstants.ERROR_UPDATING_EVENT_SUBSCRIPTION);
        }
        return isUpdated;
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
