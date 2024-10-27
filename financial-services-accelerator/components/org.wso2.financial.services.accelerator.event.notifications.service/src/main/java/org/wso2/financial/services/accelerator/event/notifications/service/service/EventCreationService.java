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

package org.wso2.financial.services.accelerator.event.notifications.service.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.financial.services.accelerator.common.util.DatabaseUtils;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.dao.EventNotificationDAO;
import org.wso2.financial.services.accelerator.event.notifications.service.dto.NotificationCreationDTO;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.model.Notification;
import org.wso2.financial.services.accelerator.event.notifications.service.model.NotificationEvent;
import org.wso2.financial.services.accelerator.event.notifications.service.persistence.EventNotificationStoreInitializer;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Map;

/**
 * This is the event creation service class.
 */
public class EventCreationService {

    private static Log log = LogFactory.getLog(EventCreationService.class);

    /**
     * The publishEventNotification methods will call the dao layer to persist the event
     * notifications for event polling request.
     *
     * @param notificationCreationDTO   Notification creation DTO
     * @return Event Response
     * @throws FSEventNotificationException Exception when persisting event notification data
     */
    public String publishEventNotification(NotificationCreationDTO notificationCreationDTO)
            throws FSEventNotificationException {

        Connection connection = DatabaseUtils.getDBConnection();
        Notification notification = getNotification(notificationCreationDTO);
        ArrayList<NotificationEvent> eventsList = getEvents(notificationCreationDTO.getEventPayload());

        EventNotificationDAO eventCreationDAO = EventNotificationStoreInitializer.getEventNotificationDAO();
        String eventResponse = null;

        try {
            eventResponse = eventCreationDAO.persistEventNotification(connection, notification, eventsList);
            DatabaseUtils.commitTransaction(connection);

            //TODO:
            // Check whether the real time event notification is enabled.
//            if (FinancialServicesConfigParser.getInstance().isRealtimeEventNotificationEnabled()) {
//                new Thread(new EventNotificationProducerService(notification, eventsList)).start();
//            }
            return eventResponse;
        } catch (FSEventNotificationException e) {
            DatabaseUtils.rollbackTransaction(connection);
            throw new FSEventNotificationException("Error when persisting event notification data", e);
        } finally {
            log.debug(EventNotificationConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }

    /**
     * The getEvents method is used to get the NotificationEvents Array list from payload.
     *
     * @param notificationEvents Notification Events to convert
     * @return Event notification List
     */
    private ArrayList<NotificationEvent> getEvents(Map<String, JSONObject> notificationEvents) {

        ArrayList<NotificationEvent> eventsList = new ArrayList<>();
        for (Map.Entry<String, JSONObject> entry : notificationEvents.entrySet()) {
            NotificationEvent notificationEvent = new NotificationEvent();
            notificationEvent.setEventType(entry.getKey());
            notificationEvent.setEventInformation(entry.getValue());
            eventsList.add(notificationEvent);
        }

        return eventsList;
    }

    /**
     * The getNotification method is used to get the NotificationDAO from payload.
     *
     * @param notificationCreationDTO Notification Creation DTO
     * @return Notification Details
     */
    private Notification getNotification(NotificationCreationDTO notificationCreationDTO) {

        Notification notification = new Notification();
        notification.setNotificationId(UUIDGenerator.generateUUID());
        notification.setClientId(notificationCreationDTO.getClientId());
        notification.setResourceId(notificationCreationDTO.getResourceId());
        notification.setStatus(EventNotificationConstants.OPEN);

        return notification;
    }
}
