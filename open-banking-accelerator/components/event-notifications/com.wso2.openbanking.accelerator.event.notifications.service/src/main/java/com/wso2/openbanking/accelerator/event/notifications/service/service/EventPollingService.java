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

import com.nimbusds.jose.JOSEException;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.dao.AggregatedPollingDAO;
import com.wso2.openbanking.accelerator.event.notifications.service.dto.EventPollingDTO;
import com.wso2.openbanking.accelerator.event.notifications.service.dto.NotificationDTO;
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.model.AggregatedPollingResponse;
import com.wso2.openbanking.accelerator.event.notifications.service.model.Notification;
import com.wso2.openbanking.accelerator.event.notifications.service.model.NotificationError;
import com.wso2.openbanking.accelerator.event.notifications.service.model.NotificationEvent;
import com.wso2.openbanking.accelerator.event.notifications.service.persistence.EventPollingStoreInitializer;
import com.wso2.openbanking.accelerator.event.notifications.service.util.EventNotificationServiceUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the event polling service.
 */
public class EventPollingService {

    private static Log log = LogFactory.getLog(EventPollingService.class);

    /**
     * The pollEvents methods will return the Aggregated Polling Response for
     * event polling request.
     * @param eventPollingDTO Event polling request DTO
     * @return AggregatedPollingResponse Aggregated Polling Response
     * @throws OBEventNotificationException Exception when polling events
     */
    public AggregatedPollingResponse pollEvents(EventPollingDTO eventPollingDTO)
            throws OBEventNotificationException {

        AggregatedPollingResponse aggregatedPollingResponse = new AggregatedPollingResponse();
        AggregatedPollingDAO aggregatedPollingDAO = EventPollingStoreInitializer.getAggregatedPollingDAO();

        EventNotificationGenerator eventNotificationGenerator = EventNotificationServiceUtil.
                getEventNotificationGenerator();

        Map<String, String> sets = new HashMap<>();

        //Short polling
        if (eventPollingDTO.getReturnImmediately()) {

            //Update notifications with ack
            for (String notificationId : eventPollingDTO.getAck()) {
                aggregatedPollingDAO.updateNotificationStatusById(notificationId, EventNotificationConstants.ACK);
            }

            //Update notifications with err
            for (Map.Entry<String, NotificationError> entry: eventPollingDTO.getErrors().entrySet()) {
                //Check if the notification is in OPEN status
                if (aggregatedPollingDAO.getNotificationStatus(entry.getKey())) {
                    aggregatedPollingDAO.updateNotificationStatusById(
                            entry.getKey(), EventNotificationConstants.ERROR);
                    aggregatedPollingDAO.storeErrorNotification(entry.getValue());
                }
            }

            //Retrieve notifications
            int maxEvents = eventPollingDTO.getMaxEvents();

            if (maxEvents == 0) {
                aggregatedPollingResponse.setSets(sets);
                aggregatedPollingResponse.setStatus(EventNotificationConstants.OK);
            } else {

                int setsToReturn = OpenBankingConfigParser.getInstance().getNumberOfSetsToReturn();

                List<NotificationDTO> notificationList;

                if (maxEvents < setsToReturn) {
                    notificationList = aggregatedPollingDAO.getNotificationsByClientIdAndStatus(
                            eventPollingDTO.getClientId(), EventNotificationConstants.OPEN, maxEvents);

                } else {
                    notificationList = aggregatedPollingDAO.getNotificationsByClientIdAndStatus(
                            eventPollingDTO.getClientId(), EventNotificationConstants.OPEN, setsToReturn);
                }

                if (notificationList.isEmpty()) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("No OB Event Notifications available for for the client " +
                                "with ID : '%s'.", eventPollingDTO.getClientId().replaceAll("[\r\n]", "")));
                    }
                    aggregatedPollingResponse.setStatus(EventNotificationConstants.NOT_FOUND);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("OB Event Notifications available for the client " +
                                "with ID : '%s'.", eventPollingDTO.getClientId().replaceAll("[\r\n]", "")));
                    }
                    aggregatedPollingResponse.setStatus(EventNotificationConstants.OK);

                    for (NotificationDTO notificationDTO : notificationList) {

                        try {
                            //Get events by notificationId
                            List<NotificationEvent> notificationEvents = aggregatedPollingDAO.
                                    getEventsByNotificationID(notificationDTO.getNotificationId());

                            Notification responseNotification = eventNotificationGenerator.
                                    generateEventNotificationBody(notificationDTO, notificationEvents);
                            sets.put(notificationDTO.getNotificationId(),
                                    eventNotificationGenerator.generateEventNotification(Notification.getJsonNode(
                                            responseNotification)));
                            log.info("Retrieved OB event notifications");
                        } catch (OBEventNotificationException |
                                 IOException | JOSEException | IdentityOAuth2Exception e) {
                            log.debug("Error when retrieving OB event notifications.", e);
                            throw new OBEventNotificationException("Error when retrieving OB event notifications.", e);
                        }
                    }
                    aggregatedPollingResponse.setSets(sets);
                }
            }

            int count = aggregatedPollingDAO.getNotificationCountByClientIdAndStatus(eventPollingDTO.getClientId(),
                    EventNotificationConstants.OPEN) - aggregatedPollingResponse.getSets().size();

            aggregatedPollingResponse.setCount(count);

            return aggregatedPollingResponse;
        }

        return null;
    }
}
