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
import org.wso2.financial.services.accelerator.event.notifications.service.model.EventSubscription;

import java.sql.Connection;
import java.util.List;

/**
 * Event Notification Subscription DAO interface.
 */
public interface EventSubscriptionDAO {

    /**
     * This method is used to store event notification subscription in the database.
     *
     * @param connection Database connection.
     * @param eventSubscription EventSubscription object.
     * @return EventSubscription object.
     * @throws FSEventNotificationException  Exception when storing event subscription
     */
    EventSubscription storeEventSubscription(Connection connection, EventSubscription eventSubscription)
            throws FSEventNotificationException;

    /**
     * This method is used to store subscribed event types in the database.
     *
     * @param connection Database connection.
     * @param subscriptionId Subscription ID.
     * @param eventTypes Event types to be stored.
     * @return List of strings with subscribed event types.
     * @throws FSEventNotificationException  Exception when storing subscribed event types
     */
    List<String> storeSubscribedEventTypes(Connection connection, String subscriptionId, List<String> eventTypes)
            throws FSEventNotificationException;

    /**
     * This method is used to retrieve an event subscription by a subscription ID.
     *
     * @param connection Database connection.
     * @param subscriptionId Subscription ID.
     * @return EventSubscription model.
     * @throws FSEventNotificationException  Exception when retrieving event subscription by subscription ID
     */
    EventSubscription getEventSubscriptionBySubscriptionId(Connection connection, String subscriptionId)
            throws FSEventNotificationException;

    /**
     * This method is used to retrieve all event subscriptions a client.
     *
     * @param connection Database connection.
     * @param clientId Client ID.
     * @return List of EventSubscription models.
     * @throws FSEventNotificationException  Exception when retrieving event subscriptions by client ID
     */
    List<EventSubscription> getEventSubscriptionsByClientId(Connection connection, String clientId)
            throws FSEventNotificationException;

    /**
     * This method is used to retrieve all event subscriptions by event type.
     *
     * @param connection Database connection.
     * @param eventType Event type that need to be subscribed by the retrieving subscriptions.
     * @return List of EventSubscription models.
     * @throws FSEventNotificationException   Exception when retrieving event subscriptions by event type
     */
    List<EventSubscription> getEventSubscriptionsByEventType(Connection connection, String eventType)
            throws FSEventNotificationException;

    /**
     * This method is used to update an event subscription.
     *
     * @param connection Database connection.
     * @param eventSubscription eventSubscription object.
     * @return true if update was successful.
     * @throws FSEventNotificationException  Exception when updating event subscription
     */
    Boolean updateEventSubscription(Connection connection, EventSubscription eventSubscription)
            throws FSEventNotificationException;

    /**
     * This method is used to delete an event subscription.
     *
     * @param connection Database connection.
     * @param subscriptionId Subscription ID.
     * @return true if deletion was successful.
     * @throws FSEventNotificationException  Exception when deleting event subscription
     */
    Boolean deleteEventSubscription(Connection connection, String subscriptionId) throws FSEventNotificationException;

    /**
     * This method is used to delete subscribed event types of a subscription.
     *
     * @param connection Database connection.
     * @param subscriptionId subscription ID.
     * @return true if deletion was successful.
     * @throws FSEventNotificationException  Exception when deleting subscribed event types
     */
    Boolean deleteSubscribedEventTypes(Connection connection, String subscriptionId)
            throws FSEventNotificationException;

}
