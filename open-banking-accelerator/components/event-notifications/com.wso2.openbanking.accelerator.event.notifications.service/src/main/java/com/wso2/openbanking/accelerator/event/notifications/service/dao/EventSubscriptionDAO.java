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

import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.model.EventSubscription;

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
     * @throws OBEventNotificationException
     */
    EventSubscription storeEventSubscription(Connection connection, EventSubscription eventSubscription)
            throws OBEventNotificationException;

    /**
     * This method is used to store subscribed event types in the database.
     *
     * @param connection Database connection.
     * @param subscriptionId Subscription ID.
     * @param eventTypes Event types to be stored.
     * @return List of strings with subscribed event types.
     * @throws OBEventNotificationException
     */
    List<String> storeSubscribedEventTypes(Connection connection, String subscriptionId, List<String> eventTypes)
            throws OBEventNotificationException;

    /**
     * This method is used to retrieve an event subscription by a subscription ID.
     *
     * @param connection Database connection.
     * @param subscriptionId Subscription ID.
     * @return EventSubscription model.
     * @throws OBEventNotificationException
     */
    EventSubscription getEventSubscriptionBySubscriptionId(Connection connection, String subscriptionId)
            throws OBEventNotificationException;

    /**
     * This method is used to retrieve all event subscriptions a client.
     *
     * @param connection Database connection.
     * @param clientId Client ID.
     * @return List of EventSubscription models.
     * @throws OBEventNotificationException
     */
    List<EventSubscription> getEventSubscriptionsByClientId(Connection connection, String clientId)
            throws OBEventNotificationException;

    /**
     * This method is used to retrieve all event subscriptions by event type.
     *
     * @param connection Database connection.
     * @param eventType Event type that need to be subscribed by the retrieving subscriptions.
     * @return List of EventSubscription models.
     * @throws OBEventNotificationException
     */
    List<EventSubscription> getEventSubscriptionsByEventType(Connection connection, String eventType)
            throws OBEventNotificationException;

    /**
     * This method is used to update an event subscription.
     *
     * @param connection Database connection.
     * @param eventSubscription eventSubscription object.
     * @return true if update was successful.
     * @throws OBEventNotificationException
     */
    Boolean updateEventSubscription(Connection connection, EventSubscription eventSubscription)
            throws OBEventNotificationException;

    /**
     * This method is used to delete an event subscription.
     *
     * @param connection Database connection.
     * @param subscriptionId Subscription ID.
     * @return true if deletion was successful.
     * @throws OBEventNotificationException
     */
    Boolean deleteEventSubscription(Connection connection, String subscriptionId) throws OBEventNotificationException;

    /**
     * This method is used to delete subscribed event types of a subscription.
     *
     * @param connection Database connection.
     * @param subscriptionId subscription ID.
     * @return true if deletion was successful.
     * @throws OBEventNotificationException
     */
    Boolean deleteSubscribedEventTypes(Connection connection, String subscriptionId)
            throws OBEventNotificationException;

}
