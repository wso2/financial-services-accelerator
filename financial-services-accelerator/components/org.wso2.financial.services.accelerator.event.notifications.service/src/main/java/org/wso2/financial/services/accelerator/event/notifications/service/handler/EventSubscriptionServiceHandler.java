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

package org.wso2.financial.services.accelerator.event.notifications.service.handler;

import org.wso2.financial.services.accelerator.event.notifications.service.dto.EventSubscriptionDTO;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.model.EventSubscriptionResponse;

/**
 * EventSubscription Service handler is used to validate subscription requests before calling the
 * subscription service. For custom validations this class can be extended and the extended class
 * can be added to the deployment.toml under event_subscription_handler to execute the specific class.
 */
public interface EventSubscriptionServiceHandler {

    /**
     * This method is used to create event subscriptions in the accelerator database. The method is a generic
     * method that is used to persist data into the NOTIFICATION_SUBSCRIPTION and NOTIFICATION_SUBSCRIPTION_EVENT
     * tables.
     *
     * @param eventSubscriptionRequestDto The request DTO that contains the subscription details.
     * @return For successful request the API will return a JSON with the subscriptionId
     * @throws FSEventNotificationException Exception when creating event subscription.
     */
    EventSubscriptionResponse createEventSubscription(EventSubscriptionDTO eventSubscriptionRequestDto)
            throws FSEventNotificationException;

    /**
     * This method is used to retrieve an event subscription by its subscription ID.
     *
     * @param clientId The client ID of the subscription.
     * @param subscriptionId The subscription ID of the subscription.
     * @return For successful request the API will return a JSON with the retrieved Subscription.
     * @throws FSEventNotificationException Exception when retrieving event subscription.
     */
    EventSubscriptionResponse getEventSubscription(String clientId, String subscriptionId)
            throws FSEventNotificationException;

    /**
     * This method is used to retrieve all event subscriptions of a client.
     *
     * @param clientId The client ID of the subscription.
     * @return For successful request the API will return a JSON with the retrieved Subscriptions.
     * @throws FSEventNotificationException Exception when retrieving event subscriptions.
     */
    EventSubscriptionResponse getAllEventSubscriptions(String clientId) throws FSEventNotificationException;

    /**
     * This method is used to retrieve all event subscriptions by event type.
     *
     * @param clientId The client ID of the subscription.
     * @param eventType The event type that needs to be subscribed by the retrieving subscriptions.
     * @return For successful request the API will return a JSON with the retrieved Subscriptions.
     * @throws FSEventNotificationException Exception when retrieving event subscriptions.
     */
    EventSubscriptionResponse getEventSubscriptionsByEventType(String clientId, String eventType)
            throws FSEventNotificationException;

    /**
     * This method is used to update an event subscription.
     *
     * @param eventSubscriptionUpdateRequestDto The request DTO that contains the updating subscription details.
     * @return For successful request the API will return a JSON with the updated Subscription.
     * @throws FSEventNotificationException Exception when updating event subscription.
     */
    EventSubscriptionResponse updateEventSubscription(EventSubscriptionDTO eventSubscriptionUpdateRequestDto)
            throws FSEventNotificationException;

    /**
     * This method is used to delete an event subscription.
     *
     * @param clientId The client ID of the subscription.
     * @param subscriptionId The subscription ID of the subscription.
     * @return For successful request the API will an OK response.
     * @throws FSEventNotificationException Exception when deleting event subscription.
     */
    EventSubscriptionResponse deleteEventSubscription(String clientId, String subscriptionId)
            throws FSEventNotificationException;

}
