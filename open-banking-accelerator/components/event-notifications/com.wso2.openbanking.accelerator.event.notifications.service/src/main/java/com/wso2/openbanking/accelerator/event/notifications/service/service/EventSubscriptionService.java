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

package com.wso2.openbanking.accelerator.event.notifications.service.service;

import com.wso2.openbanking.accelerator.common.util.DatabaseUtil;
import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.dao.EventSubscriptionDAO;
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.model.EventSubscription;
import com.wso2.openbanking.accelerator.event.notifications.service.persistence.EventSubscriptionStoreInitializer;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.util.List;

/**
 * This is the event subscription service class.
 */
public class EventSubscriptionService {
    private static Log log = LogFactory.getLog(EventSubscriptionService.class);

    /**
     * This method will call the dao layer to persist the event subscription.
     *
     * @param eventSubscription event subscription object that needs to be persisted
     * @throws OBEventNotificationException
     */
    public EventSubscription createEventSubscription(EventSubscription eventSubscription)
            throws OBEventNotificationException {

        EventSubscriptionDAO eventSubscriptionDao = EventSubscriptionStoreInitializer.getEventSubscriptionDao();

        Connection connection = DatabaseUtil.getDBConnection();

        try {
            //store event subscription data in the database
            EventSubscription storeEventSubscriptionResult = eventSubscriptionDao.
                    storeEventSubscription(connection, eventSubscription);
            //store subscribed event types in the database
            if (eventSubscription.getEventTypes() != null && !eventSubscription.getEventTypes().isEmpty()) {
                List<String> storedEventTypes = eventSubscriptionDao.storeSubscribedEventTypes(connection,
                        storeEventSubscriptionResult.getSubscriptionId(), eventSubscription.getEventTypes());
                storeEventSubscriptionResult.setEventTypes(storedEventTypes);
            }
            log.debug("Event subscription created successfully.");
            DatabaseUtil.commitTransaction(connection);
            return storeEventSubscriptionResult;
        } catch (OBEventNotificationException e) {
            log.error("Error while creating event subscription.", e);
            DatabaseUtil.rollbackTransaction(connection);
            throw new OBEventNotificationException(EventNotificationConstants.ERROR_STORING_EVENT_SUBSCRIPTION, e);
        } finally {
            DatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * This method will call the dao layer to retrieve a single event subscription.
     *
     * @param subscriptionId subscription id of the event subscription
     * @throws OBEventNotificationException
     */
    public EventSubscription getEventSubscriptionBySubscriptionId(String subscriptionId)
            throws OBEventNotificationException {

        EventSubscriptionDAO eventSubscriptionDao = EventSubscriptionStoreInitializer.getEventSubscriptionDao();

        Connection connection = DatabaseUtil.getDBConnection();

        try {
            return eventSubscriptionDao.getEventSubscriptionBySubscriptionId(connection, subscriptionId);
        } catch (OBEventNotificationException e) {
            log.error("Error while retrieving event subscription.", e);
            throw new OBEventNotificationException(e.getMessage(), e);
        } finally {
            DatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * This method will call the dao layer to retrieve all event subscriptions of a client.
     *
     * @param clientId client id of the event subscription
     * @throws OBEventNotificationException
     */
    public List<EventSubscription> getEventSubscriptionsByClientId(String clientId)
            throws OBEventNotificationException {

        Connection connection = DatabaseUtil.getDBConnection();
        try {
            EventSubscriptionDAO eventSubscriptionDao = EventSubscriptionStoreInitializer.getEventSubscriptionDao();
            return eventSubscriptionDao.getEventSubscriptionsByClientId(connection, clientId);
        } catch (OBEventNotificationException e) {
            log.error("Error while retrieving event subscriptions.", e);
            throw new OBEventNotificationException(e.getMessage(), e);
        } finally {
            DatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * This method will call the dao layer to retrieve all event subscriptions by event type.
     *
     * @param eventType event type that needs to be subscribed by the retrieving event subscriptions.
     * @throws OBEventNotificationException
     */
    public List<EventSubscription> getEventSubscriptionsByClientIdAndEventType(String eventType)
            throws OBEventNotificationException {


        Connection connection = DatabaseUtil.getDBConnection();
        try {
            EventSubscriptionDAO eventSubscriptionDao = EventSubscriptionStoreInitializer.getEventSubscriptionDao();
            return eventSubscriptionDao.getEventSubscriptionsByEventType(connection, eventType);
        } catch (OBEventNotificationException e) {
            log.error("Error while retrieving event subscriptions.", e);
            throw new OBEventNotificationException(e.getMessage(), e);
        } finally {
            DatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * This method will call the dao layer to update an event subscription.
     *
     * @param eventSubscription event subscription object that needs to be updated
     * @throws OBEventNotificationException
     */
    public Boolean updateEventSubscription(EventSubscription eventSubscription)
            throws OBEventNotificationException {

        Connection connection = DatabaseUtil.getDBConnection();

        EventSubscriptionDAO eventSubscriptionDao = EventSubscriptionStoreInitializer.getEventSubscriptionDao();

        //get the stored event subscription
        EventSubscription retrievedEventSubscription = eventSubscriptionDao.
                getEventSubscriptionBySubscriptionId(connection, eventSubscription.getSubscriptionId());

        //update request data column
        try {
            JSONParser parser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
            JSONObject storedRequestData = (JSONObject) parser.parse(retrievedEventSubscription.getRequestData());
            JSONObject receivedRequestData = (JSONObject) parser.parse(eventSubscription.getRequestData());
            for (String key : storedRequestData.keySet()) {
                if (receivedRequestData.containsKey(key)) {
                    storedRequestData.put(key, receivedRequestData.get(key));
                }
            }
            eventSubscription.setRequestData(storedRequestData.toJSONString());
        } catch (ParseException e) {
            log.error("Error while Parsing the stored request Object", e);
            throw new OBEventNotificationException("Error while Parsing the stored request Object", e);
        }

        //update event subscription
        try {
            boolean isUpdated = eventSubscriptionDao.updateEventSubscription(connection, eventSubscription);

            //update subscribed event types
            if (isUpdated && eventSubscription.getEventTypes() != null &&
                    !eventSubscription.getEventTypes().isEmpty()) {
                //delete the existing subscribed event types
                eventSubscriptionDao.deleteSubscribedEventTypes(connection, eventSubscription.getSubscriptionId());
                //store the updated subscribed event types
                List<String> storedEventTypes = eventSubscriptionDao.storeSubscribedEventTypes(connection,
                        eventSubscription.getSubscriptionId(), eventSubscription.getEventTypes());
                eventSubscription.setEventTypes(storedEventTypes);
            } else if (!isUpdated) {
                log.debug("Event subscription update failed.");
                DatabaseUtil.rollbackTransaction(connection);
            }
            log.debug("Event subscription updated successfully.");
            DatabaseUtil.commitTransaction(connection);
            return isUpdated;
        } catch (OBEventNotificationException e) {
            log.error("Error while updating event subscription.", e);
            DatabaseUtil.rollbackTransaction(connection);
            throw new OBEventNotificationException(e.getMessage(), e);
        } finally {
            DatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * This method will call the dao layer to delete an event subscription.
     *
     * @param subscriptionId subscription id of the event subscription
     * @throws OBEventNotificationException
     */
    public Boolean deleteEventSubscription(String subscriptionId) throws OBEventNotificationException {

        Connection connection = DatabaseUtil.getDBConnection();

        try {
            EventSubscriptionDAO eventSubscriptionDao = EventSubscriptionStoreInitializer.getEventSubscriptionDao();
            boolean isDeleted = eventSubscriptionDao.deleteEventSubscription(connection, subscriptionId);
            if (isDeleted) {
                log.debug("Event subscription deleted successfully.");
                DatabaseUtil.commitTransaction(connection);
            } else {
                log.debug("Event subscription deletion failed.");
                DatabaseUtil.rollbackTransaction(connection);
            }
            return isDeleted;
        } catch (OBEventNotificationException e) {
            log.error("Error while deleting event subscription.", e);
            throw new OBEventNotificationException(e.getMessage(), e);
        } finally {
            DatabaseUtil.closeConnection(connection);
        }
    }


}
