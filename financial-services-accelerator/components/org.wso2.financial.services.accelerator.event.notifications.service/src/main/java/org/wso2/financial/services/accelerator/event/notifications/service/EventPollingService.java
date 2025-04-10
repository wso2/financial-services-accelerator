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

import com.nimbusds.jose.JOSEException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.financial.services.accelerator.common.util.DatabaseUtils;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.dao.EventNotificationDAO;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.model.AggregatedPollingResponse;
import org.wso2.financial.services.accelerator.event.notifications.service.model.EventPolling;
import org.wso2.financial.services.accelerator.event.notifications.service.model.Notification;
import org.wso2.financial.services.accelerator.event.notifications.service.model.NotificationError;
import org.wso2.financial.services.accelerator.event.notifications.service.model.NotificationEvent;
import org.wso2.financial.services.accelerator.event.notifications.service.model.NotificationResponse;
import org.wso2.financial.services.accelerator.event.notifications.service.persistence.EventNotificationStoreInitializer;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationServiceUtil;

import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Event polling service.
 */
public class EventPollingService {

    private static Log log = LogFactory.getLog(EventPollingService.class);

    /**
     * The pollEvents methods will return the Aggregated Polling Response for
     * event polling request.
     * @param eventPolling Event polling request
     * @return AggregatedPollingResponse Aggregated Polling Response
     * @throws FSEventNotificationException Exception when polling events
     */
    public AggregatedPollingResponse pollEvents(EventPolling eventPolling)
            throws FSEventNotificationException {

        Connection connection = DatabaseUtils.getDBConnection();
        AggregatedPollingResponse aggregatedPollingResponse = new AggregatedPollingResponse();
        EventNotificationDAO eventNotificationDAO = EventNotificationStoreInitializer.getEventNotificationDAO();

        EventNotificationGenerator eventNotificationGenerator = EventNotificationServiceUtil.
                getEventNotificationGenerator();

        Map<String, String> sets = new HashMap<>();

        //Short polling
        if (eventPolling.getReturnImmediately()) {

            try {
                //Update notifications with ack
                for (String notificationId : eventPolling.getAck()) {
                    eventNotificationDAO.updateNotificationStatusById(connection, notificationId,
                            EventNotificationConstants.ACK);
                }

                //Update notifications with err
                for (Map.Entry<String, NotificationError> entry: eventPolling.getErrors().entrySet()) {
                    //Check if the notification is in OPEN status
                    if (eventNotificationDAO.getNotificationStatus(connection, entry.getKey())) {
                        eventNotificationDAO.updateNotificationStatusById(connection, entry.getKey(),
                                EventNotificationConstants.ERROR);
                        eventNotificationDAO.storeErrorNotification(connection, entry.getValue());
                    }
                }

                //Retrieve notifications
                int maxEvents = eventPolling.getMaxEvents();

                if (maxEvents == 0) {
                    aggregatedPollingResponse.setSets(sets);
                    aggregatedPollingResponse.setStatus(HttpStatus.SC_OK);
                } else {

                    int setsToReturn = eventPolling.getSetsToReturn();

                    List<Notification> notificationList;

                    if (maxEvents < setsToReturn) {
                        notificationList = eventNotificationDAO.getNotificationsByClientIdAndStatus(connection,
                                eventPolling.getClientId(), EventNotificationConstants.OPEN, maxEvents);

                    } else {
                        notificationList = eventNotificationDAO.getNotificationsByClientIdAndStatus(connection,
                                eventPolling.getClientId(), EventNotificationConstants.OPEN, setsToReturn);
                    }

                    if (notificationList.isEmpty()) {
                        if (log.isDebugEnabled()) {
                            log.debug(String.format("No FS Event Notifications available for for the client " +
                                    "with ID : '%s'.", eventPolling.getClientId().replaceAll("[\r\n]", "")));
                        }
                        aggregatedPollingResponse.setStatus(HttpStatus.SC_NOT_FOUND);
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug(String.format("FS Event Notifications available for the client " +
                                    "with ID : '%s'.", eventPolling.getClientId().replaceAll("[\r\n]", "")));
                        }
                        aggregatedPollingResponse.setStatus(HttpStatus.SC_OK);

                        for (Notification notification : notificationList) {

                            //Get events by notificationId
                            List<NotificationEvent> notificationEvents = eventNotificationDAO.
                                    getEventsByNotificationID(connection, notification.getNotificationId());

                            NotificationResponse responseNotification = eventNotificationGenerator.
                                    generateEventNotificationBody(notification, notificationEvents);
                            sets.put(notification.getNotificationId(), eventNotificationGenerator
                                    .generateEventNotification(NotificationResponse.getJsonNode(responseNotification)));
                            log.info("Retrieved FS event notifications");
                        }
                        aggregatedPollingResponse.setSets(sets);
                    }
                }

                int count = eventNotificationDAO.getNotificationCountByClientIdAndStatus(connection,
                        eventPolling.getClientId(), EventNotificationConstants.OPEN)
                        - aggregatedPollingResponse.getSets().size();

                aggregatedPollingResponse.setCount(count);
                DatabaseUtils.commitTransaction(connection);

                return aggregatedPollingResponse;
            } catch (FSEventNotificationException |
                     IOException | JOSEException | IdentityOAuth2Exception e) {
                log.debug("Error when retrieving FS event notifications.", e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new FSEventNotificationException("Error when retrieving FS event notifications.", e);
            } finally {
                log.debug(EventNotificationConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
                DatabaseUtils.closeConnection(connection);
            }
        }

        return null;
    }
}
