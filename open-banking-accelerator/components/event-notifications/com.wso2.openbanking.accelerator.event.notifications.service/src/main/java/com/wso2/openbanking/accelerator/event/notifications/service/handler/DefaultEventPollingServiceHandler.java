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

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.dto.EventPollingDTO;
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.model.AggregatedPollingResponse;
import com.wso2.openbanking.accelerator.event.notifications.service.model.NotificationError;
import com.wso2.openbanking.accelerator.event.notifications.service.response.EventPollingResponse;
import com.wso2.openbanking.accelerator.event.notifications.service.service.EventPollingService;
import com.wso2.openbanking.accelerator.event.notifications.service.util.EventNotificationServiceUtil;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Locale;

/**
 * This is the service handler for event polling.
 */
public class DefaultEventPollingServiceHandler implements EventPollingServiceHandler {

    private static final Log log = LogFactory.getLog(DefaultEventPollingServiceHandler.class);

    public void setEventPollingService(EventPollingService eventPollingService) {
        this.eventPollingService = eventPollingService;
    }

    private EventPollingService eventPollingService = new EventPollingService();


    /**
     * This method is used to Poll Events as per request params.
     * @param eventPollingRequest JSON request for event polling
     * @return  EventPollingResponse
     */
    public EventPollingResponse pollEvents(JSONObject eventPollingRequest) {

        EventPollingDTO eventPollingDTO = mapPollingRequest(eventPollingRequest);
        EventPollingResponse eventPollingResponse = new EventPollingResponse();

        //Validate clientID of the polling request
        try {
            EventNotificationServiceUtil.validateClientId(eventPollingDTO.getClientId());
        } catch (OBEventNotificationException e) {
            log.error("Invalid client ID", e);
            eventPollingResponse.setStatus(EventNotificationConstants.BAD_REQUEST);
            eventPollingResponse.setErrorResponse(EventNotificationServiceUtil.getErrorDTO(
                            EventNotificationConstants.INVALID_REQUEST, String.format("A client was not found" +
                            " for the client id : '%s' in the database.. ", eventPollingDTO.getClientId())));
            return eventPollingResponse;
        }

        //Poll events
        try {
            AggregatedPollingResponse aggregatedPollingResponse = eventPollingService.pollEvents(eventPollingDTO);
            eventPollingResponse.setStatus(aggregatedPollingResponse.getStatus());
            eventPollingResponse.setResponseBody(getPollingResponseJSON(aggregatedPollingResponse));
            return eventPollingResponse;
        } catch (OBEventNotificationException e) {
            log.error("OB Event Notification error" , e);
            eventPollingResponse.setStatus(EventNotificationConstants.BAD_REQUEST);
            eventPollingResponse.setErrorResponse(EventNotificationServiceUtil.getErrorDTO(
                    EventNotificationConstants.INVALID_REQUEST, e.getMessage()));
            return eventPollingResponse;
        }

    }

    /**
     * This method will map the eventPollingRequest JSON to EventPollingDTO.
     * @param eventPollingRequest JSON request for event polling
     * @return EventPollingDTO
     */
    public EventPollingDTO mapPollingRequest(JSONObject eventPollingRequest) {

        EventPollingDTO eventPollingDTO = new EventPollingDTO();
        eventPollingDTO.setClientId(eventPollingRequest.get(EventNotificationConstants.X_WSO2_CLIENT_ID).toString());

        if (eventPollingRequest.size() == 0) {

            eventPollingDTO.setMaxEvents(OpenBankingConfigParser.getInstance().getNumberOfSetsToReturn());

            return eventPollingDTO;
        }

        //Set acknowledged events to DTO
        if (eventPollingRequest.containsKey(EventNotificationConstants.ACK.toLowerCase(Locale.ROOT))) {
            JSONArray acknowledgedEvents = (JSONArray) eventPollingRequest.
                    get(EventNotificationConstants.ACK.toLowerCase(Locale.ROOT));
            acknowledgedEvents.forEach((event -> {
                eventPollingDTO.setAck(event.toString());
            }));
        }

        //Set error events to DTO
        if (eventPollingRequest.containsKey(EventNotificationConstants.SET_ERRORS)) {
            JSONObject errorEvents = (JSONObject) eventPollingRequest.
                    get(EventNotificationConstants.SET_ERRORS);
            errorEvents.keySet().forEach(errorEvent -> {
                JSONObject errorEventInformation = (JSONObject) errorEvents.get(errorEvent);
                NotificationError notificationError = getNotificationError(errorEventInformation);
                notificationError.setNotificationId(errorEvent);
                eventPollingDTO.setErrors(errorEvent, notificationError);
            });
        }

        //Set maxEvents count to return
        if (eventPollingRequest.containsKey(EventNotificationConstants.MAX_EVENTS)) {
            eventPollingDTO.setMaxEvents(Integer.parseInt(eventPollingRequest.
                    get(EventNotificationConstants.MAX_EVENTS).toString()));
        } else {
            eventPollingDTO.setMaxEvents(OpenBankingConfigParser.getInstance().getNumberOfSetsToReturn());
        }

        return eventPollingDTO;
    }

    @Generated(message = "Private method tested when testing the invoked method")
    private NotificationError getNotificationError(JSONObject errorEvent) {

        NotificationError notificationError = new NotificationError();
        notificationError.setErrorCode(errorEvent.get(
                EventNotificationConstants.ERROR.toLowerCase(Locale.ROOT)).toString());
        notificationError.setErrorDescription(
                errorEvent.get(EventNotificationConstants.DESCRIPTION).toString());
        return notificationError;
    }

    @Generated(message = "Private method tested when testing the invoked method")
    private JSONObject getPollingResponseJSON(AggregatedPollingResponse aggregatedPollingResponse) {

        JSONObject responseJSON = new JSONObject();
        responseJSON.put(EventNotificationConstants.SETS, aggregatedPollingResponse.getSets());
        responseJSON.put(EventNotificationConstants.MORE_AVAILABLE,
                aggregatedPollingResponse.isMoreAvailable());
        return responseJSON;
    }

}
