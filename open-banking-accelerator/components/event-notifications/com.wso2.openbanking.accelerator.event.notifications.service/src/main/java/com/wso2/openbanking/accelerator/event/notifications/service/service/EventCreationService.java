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

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.util.DatabaseUtil;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.dao.EventPublisherDAO;
import com.wso2.openbanking.accelerator.event.notifications.service.dto.NotificationCreationDTO;
import com.wso2.openbanking.accelerator.event.notifications.service.dto.NotificationDTO;
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.model.NotificationEvent;
import com.wso2.openbanking.accelerator.event.notifications.service.persistence.EventPublisherStoreInitializer;
import com.wso2.openbanking.accelerator.event.notifications.service.realtime.service.EventNotificationProducerService;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;


import java.sql.Connection;
import java.util.ArrayList;
import java.util.Map;

/**
 * This is the event creation service class.
 */
public class EventCreationService {

    private static Log log = LogFactory.getLog(EventCreationService.class);

    /**
     * The publishOBEventNotification methods will call the dao layer to persist the event
     * notifications
     * event polling request.
     * @param notificationCreationDTO
     *
     * @throws OBEventNotificationException
     */
    public String publishOBEventNotification(NotificationCreationDTO notificationCreationDTO)
            throws OBEventNotificationException {

        Connection connection = DatabaseUtil.getDBConnection();
        NotificationDTO notification = getNotification(notificationCreationDTO);
        ArrayList<NotificationEvent> eventsList =  getEvents(notificationCreationDTO.getEventPayload());

        EventPublisherDAO eventPublisherDAO = EventPublisherStoreInitializer.getEventCreationDao();
        String eventResponse = null;

        try {
            eventResponse = eventPublisherDAO.persistEventNotification(connection, notification, eventsList);
            DatabaseUtil.commitTransaction(connection);

            // Check whether the real time event notification is enabled.
            if (OpenBankingConfigParser.getInstance().isRealtimeEventNotificationEnabled()) {
                new Thread(new EventNotificationProducerService(notification, eventsList)).start();
            }
            return eventResponse;
        } catch (OBEventNotificationException e) {
            throw new OBEventNotificationException("Error when persisting event notification data", e);
        } finally {
            log.debug(EventNotificationConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * The getEvents method is used to get the NotificationEvents Array list from payload.
     *
     * @param notificationEvents
     */
    @Generated(message = "Private methods invoked when calling referred method")
    private ArrayList<NotificationEvent> getEvents(Map<String, JSONObject> notificationEvents) {

        ArrayList<NotificationEvent> eventsList = new ArrayList<>();
        notificationEvents.keySet().forEach(key -> {
            Object eventInfo = notificationEvents.get(key);
            NotificationEvent notificationEvent = new NotificationEvent();
            notificationEvent.setEventType(key);
            notificationEvent.setEventInformation((JSONObject) eventInfo);
            eventsList.add(notificationEvent);
        });

        return eventsList;
    }

    /**
     * The getNotification method is used to get the NotificationDAO from payload.
     *
     * @param notificationCreationDTO
     */
    @Generated(message = "Private methods invoked when calling referred method")
    private NotificationDTO getNotification(NotificationCreationDTO notificationCreationDTO) {

        NotificationDTO notification = new NotificationDTO();
        notification.setNotificationId(UUIDGenerator.generateUUID());
        notification.setClientId(notificationCreationDTO.getClientId());
        notification.setResourceId(notificationCreationDTO.getResourceId());
        notification.setStatus(EventNotificationConstants.OPEN);

        return notification;
    }
}
