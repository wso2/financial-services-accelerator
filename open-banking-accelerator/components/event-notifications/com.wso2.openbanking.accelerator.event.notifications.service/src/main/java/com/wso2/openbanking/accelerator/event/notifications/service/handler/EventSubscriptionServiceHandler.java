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

package com.wso2.openbanking.accelerator.event.notifications.service.handler;

import com.wso2.openbanking.accelerator.event.notifications.service.dto.EventSubscriptionDTO;
import com.wso2.openbanking.accelerator.event.notifications.service.model.EventSubscription;
import com.wso2.openbanking.accelerator.event.notifications.service.response.EventSubscriptionResponse;
import net.minidev.json.JSONObject;

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
     */
    EventSubscriptionResponse createEventSubscription(EventSubscriptionDTO eventSubscriptionRequestDto);

    /**
     * This method is used to retrieve an event subscription by its subscription ID.
     *
     * @param clientId The client ID of the subscription.
     * @param subscriptionId The subscription ID of the subscription.
     * @return For successful request the API will return a JSON with the retrieved Subscription.
     */
    EventSubscriptionResponse getEventSubscription(String clientId, String subscriptionId);

    /**
     * This method is used to retrieve all event subscriptions of a client.
     *
     * @param clientId The client ID of the subscription.
     * @return For successful request the API will return a JSON with the retrieved Subscriptions.
     */
    EventSubscriptionResponse getAllEventSubscriptions(String clientId);

    /**
     * This method is used to retrieve all event subscriptions by event type.
     *
     * @param clientId The client ID of the subscription.
     * @param eventType The event type that needs to be subscribed by the retrieving subscriptions.
     * @return For successful request the API will return a JSON with the retrieved Subscriptions.
     */
    EventSubscriptionResponse getEventSubscriptionsByEventType(String clientId, String eventType);

    /**
     * This method is used to update an event subscription.
     *
     * @param eventSubscriptionUpdateRequestDto The request DTO that contains the updating subscription details.
     * @return For successful request the API will return a JSON with the updated Subscription.
     */
    EventSubscriptionResponse updateEventSubscription(EventSubscriptionDTO eventSubscriptionUpdateRequestDto);

    /**
     * This method is used to delete an event subscription.
     *
     * @param clientId The client ID of the subscription.
     * @param subscriptionId The subscription ID of the subscription.
     * @return For successful request the API will an OK response.
     */
    EventSubscriptionResponse deleteEventSubscription(String clientId, String subscriptionId);

    /**
     * This method is used to create the response JSON object from the event subscription model.
     *
     * @param eventSubscription The event subscription model.
     * @return JSONObject
     */
    JSONObject mapSubscriptionModelToResponseJson(EventSubscription eventSubscription);

}
