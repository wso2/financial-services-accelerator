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

package org.wso2.financial.services.accelerator.event.notifications.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.util.DatabaseUtils;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.dao.EventSubscriptionDAO;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.model.EventSubscription;
import org.wso2.financial.services.accelerator.event.notifications.service.persistence.EventNotificationStoreInitializer;

import java.sql.Connection;
import java.util.List;

/**
 * This is the event subscription service class.
 */
public class EventSubscriptionService {
    private static final Log log = LogFactory.getLog(EventSubscriptionService.class);

    /**
     * This method will call the dao layer to persist the event subscription.
     *
     * @param eventSubscription event subscription object that needs to be persisted
     * @return event subscription object that is persisted
     * @throws FSEventNotificationException if an error occurred while persisting the event subscription
     */
    public EventSubscription createEventSubscription(EventSubscription eventSubscription)
            throws FSEventNotificationException {

        EventSubscriptionDAO eventSubscriptionDAO = EventNotificationStoreInitializer.getEventSubscriptionDAO();

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            //store event subscription data in the database
            EventSubscription storeEventSubscriptionResult = eventSubscriptionDAO.
                    storeEventSubscription(connection, eventSubscription);
            //store subscribed event types in the database
            if (eventSubscription.getEventTypes() != null && !eventSubscription.getEventTypes().isEmpty()) {
                List<String> storedEventTypes = eventSubscriptionDAO.storeSubscribedEventTypes(connection,
                        storeEventSubscriptionResult.getSubscriptionId(), eventSubscription.getEventTypes());
                storeEventSubscriptionResult.setEventTypes(storedEventTypes);
            }
            log.debug("Event subscription created successfully.");
            DatabaseUtils.commitTransaction(connection);
            return storeEventSubscriptionResult;
        } catch (FSEventNotificationException e) {
            log.error("Error while creating event subscription.", e);
            DatabaseUtils.rollbackTransaction(connection);
            throw new FSEventNotificationException(EventNotificationConstants.ERROR_STORING_EVENT_SUBSCRIPTION, e);
        } finally {
            DatabaseUtils.closeConnection(connection);
        }
    }

    /**
     * This method will call the dao layer to retrieve a single event subscription.
     *
     * @param subscriptionId subscription id of the event subscription
     * @return event subscription object that is retrieved
     * @throws FSEventNotificationException if an error occurred while retrieving the event subscription
     */
    public EventSubscription getEventSubscriptionBySubscriptionId(String subscriptionId)
            throws FSEventNotificationException {

        EventSubscriptionDAO eventSubscriptionDAO = EventNotificationStoreInitializer.getEventSubscriptionDAO();

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            EventSubscription eventSubscription = eventSubscriptionDAO
                    .getEventSubscriptionBySubscriptionId(connection, subscriptionId);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Event subscription for subscription Id %s retrieved successfully.",
                        subscriptionId.replaceAll("[\r\n]", "")));
            }
            DatabaseUtils.commitTransaction(connection);
            return eventSubscription;
        } catch (FSEventNotificationException e) {
            log.error("Error while retrieving event subscription.", e);
            DatabaseUtils.rollbackTransaction(connection);
            throw new FSEventNotificationException(e.getMessage(), e);
        } finally {
            DatabaseUtils.closeConnection(connection);
        }
    }

    /**
     * This method will call the dao layer to retrieve all event subscriptions of a client.
     *
     * @param clientId client id of the event subscription
     * @return list of event subscriptions that are retrieved
     * @throws FSEventNotificationException if an error occurred while retrieving the event subscriptions
     */
    public List<EventSubscription> getEventSubscriptionsByClientId(String clientId)
            throws FSEventNotificationException {

        Connection connection = DatabaseUtils.getDBConnection();
        try {
            EventSubscriptionDAO eventSubscriptionDAO = EventNotificationStoreInitializer.getEventSubscriptionDAO();
            List<EventSubscription> eventSubscriptions = eventSubscriptionDAO
                    .getEventSubscriptionsByClientId(connection, clientId);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Event subscriptions for client Id %s retrieved successfully.",
                        clientId.replaceAll("[\r\n]", "")));
            }
            DatabaseUtils.commitTransaction(connection);
            return eventSubscriptions;
        } catch (FSEventNotificationException e) {
            log.error("Error while retrieving event subscriptions.", e);
            DatabaseUtils.rollbackTransaction(connection);
            throw new FSEventNotificationException(e.getMessage(), e);
        } finally {
            DatabaseUtils.closeConnection(connection);
        }
    }

    /**
     * This method will call the dao layer to retrieve all event subscriptions by event type.
     *
     * @param eventType event type that needs to be subscribed by the retrieving event subscriptions.
     * @return list of event subscriptions that are retrieved
     * @throws FSEventNotificationException if an error occurred while retrieving the event subscriptions
     */
    public List<EventSubscription> getEventSubscriptionsByEventType(String eventType)
            throws FSEventNotificationException {


        Connection connection = DatabaseUtils.getDBConnection();
        try {
            EventSubscriptionDAO eventSubscriptionDAO = EventNotificationStoreInitializer.getEventSubscriptionDAO();
            List<EventSubscription> eventSubscriptions = eventSubscriptionDAO
                    .getEventSubscriptionsByEventType(connection, eventType);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Event subscriptions for event type %s retrieved successfully.",
                        eventType.replaceAll("[\r\n]", "")));
            }
            DatabaseUtils.commitTransaction(connection);
            return eventSubscriptions;
        } catch (FSEventNotificationException e) {
            log.error("Error while retrieving event subscriptions.", e);
            DatabaseUtils.rollbackTransaction(connection);
            throw new FSEventNotificationException(e.getMessage(), e);
        } finally {
            DatabaseUtils.closeConnection(connection);
        }
    }

    /**
     * This method will call the dao layer to update an event subscription.
     *
     * @param eventSubscription event subscription object that needs to be updated
     * @return true if the event subscription is updated successfully
     * @throws FSEventNotificationException if an error occurred while updating the event subscription
     */
    public Boolean updateEventSubscription(EventSubscription eventSubscription)
            throws FSEventNotificationException {

        Connection connection = DatabaseUtils.getDBConnection();

        EventSubscriptionDAO eventSubscriptionDAO = EventNotificationStoreInitializer.getEventSubscriptionDAO();

        try {
            //get the stored event subscription
            EventSubscription retrievedEventSubscription = eventSubscriptionDAO.
                    getEventSubscriptionBySubscriptionId(connection, eventSubscription.getSubscriptionId());

            //update request data column
            JSONObject storedRequestData = new JSONObject(retrievedEventSubscription.getRequestData());
            JSONObject receivedRequestData = new JSONObject(eventSubscription.getRequestData());
            for (String key : storedRequestData.keySet()) {
                if (receivedRequestData.has(key)) {
                    storedRequestData.put(key, receivedRequestData.get(key));
                }
            }
            eventSubscription.setRequestData(storedRequestData.toString());

            //update event subscription
            boolean isUpdated = eventSubscriptionDAO.updateEventSubscription(connection, eventSubscription);

            //update subscribed event types
            if (isUpdated && eventSubscription.getEventTypes() != null &&
                    !eventSubscription.getEventTypes().isEmpty()) {
                //delete the existing subscribed event types
                eventSubscriptionDAO.deleteSubscribedEventTypes(connection, eventSubscription.getSubscriptionId());
                //store the updated subscribed event types
                List<String> storedEventTypes = eventSubscriptionDAO.storeSubscribedEventTypes(connection,
                        eventSubscription.getSubscriptionId(), eventSubscription.getEventTypes());
                eventSubscription.setEventTypes(storedEventTypes);
            } else if (!isUpdated) {
                log.debug("Event subscription update failed.");
                DatabaseUtils.rollbackTransaction(connection);
            }
            log.debug("Event subscription updated successfully.");
            DatabaseUtils.commitTransaction(connection);
            return isUpdated;
        } catch (JSONException e) {
            log.error("Error while Parsing the stored request Object", e);
            throw new FSEventNotificationException("Error while Parsing the stored request Object", e);
        } catch (FSEventNotificationException e) {
            log.error("Error while updating event subscription.", e);
            DatabaseUtils.rollbackTransaction(connection);
            throw new FSEventNotificationException(e.getMessage(), e);
        } finally {
            DatabaseUtils.closeConnection(connection);
        }
    }

    /**
     * This method will call the dao layer to delete an event subscription.
     *
     * @param subscriptionId subscription id of the event subscription
     * @return true if the event subscription is deleted successfully
     * @throws FSEventNotificationException if an error occurred while deleting the event subscription
     */
    public Boolean deleteEventSubscription(String subscriptionId) throws FSEventNotificationException {

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            EventSubscriptionDAO eventSubscriptionDAO = EventNotificationStoreInitializer.getEventSubscriptionDAO();
            boolean isDeleted = eventSubscriptionDAO.deleteEventSubscription(connection, subscriptionId);
            if (isDeleted) {
                log.debug("Event subscription deleted successfully.");
                DatabaseUtils.commitTransaction(connection);
            } else {
                log.debug("Event subscription deletion failed.");
                DatabaseUtils.rollbackTransaction(connection);
            }
            return isDeleted;
        } catch (FSEventNotificationException e) {
            log.error("Error while deleting event subscription.", e);
            DatabaseUtils.rollbackTransaction(connection);
            throw new FSEventNotificationException(e.getMessage(), e);
        } finally {
            DatabaseUtils.closeConnection(connection);
        }
    }
}
