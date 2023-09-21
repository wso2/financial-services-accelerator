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

package com.wso2.openbanking.accelerator.event.notifications.endpoint.util;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.util.OpenBankingUtils;
import com.wso2.openbanking.accelerator.event.notifications.endpoint.constants.EventNotificationEndPointConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.dto.EventNotificationErrorDTO;
import com.wso2.openbanking.accelerator.event.notifications.service.handler.EventCreationServiceHandler;
import com.wso2.openbanking.accelerator.event.notifications.service.handler.EventPollingServiceHandler;
import com.wso2.openbanking.accelerator.event.notifications.service.response.EventCreationResponse;
import com.wso2.openbanking.accelerator.event.notifications.service.response.EventPollingResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.core.Response;

/**
 * This class will have util methods needed for event notifcations.
 */
public class EventNotificationUtils {

    private static final Log log = LogFactory.getLog(EventNotificationUtils.class);

    /**
     * This method is to get the event creation service handler as per the config.
     * @return
     */
    public static EventCreationServiceHandler getEventNotificationCreationServiceHandler() {

        EventCreationServiceHandler eventCreationServiceHandler = (EventCreationServiceHandler)
                OpenBankingUtils.getClassInstanceFromFQN(OpenBankingConfigParser.getInstance().
                        getConfiguration().get(OpenBankingConstants.EVENT_CREATION_HANDLER).toString());

        return eventCreationServiceHandler;
    }

    /**
     * This method is to get the event polling service handler as per the config.
     * @return
     */
    public static EventPollingServiceHandler getEventPollingServiceHandler() {

        EventPollingServiceHandler eventPollingServiceHandler = (EventPollingServiceHandler)
                OpenBankingUtils.getClassInstanceFromFQN(OpenBankingConfigParser.getInstance().
                        getConfiguration().get(OpenBankingConstants.EVENT_POLLING_HANDLER).toString());

        return eventPollingServiceHandler;
    }

    /**
     * Method to map the Event Creation Service Response to API response.
     * @param eventCreationResponse
     * @return
     */
    public static Response mapEventCreationServiceResponse(EventCreationResponse eventCreationResponse) {

        if (EventNotificationConstants.CREATED.equals(eventCreationResponse.getStatus())) {

            return Response.status(Response.Status.CREATED).entity(eventCreationResponse.getResponseBody()).build();

        } else if (EventNotificationConstants.BAD_REQUEST.equals(eventCreationResponse.getStatus())) {

            return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationUtils.getErrorDTO(
                    EventNotificationEndPointConstants.INVALID_REQUEST,
                    eventCreationResponse.getErrorResponse())).build();
        }

        return Response.status(Response.Status.BAD_REQUEST).entity(getErrorDTO(
                EventNotificationEndPointConstants.INVALID_REQUEST,
                EventNotificationEndPointConstants.EVENT_CREATION_ERROR_RESPONSE)).build();
    }

    /**
     * Method to map Event Polling Service to API response.
     * @param eventPollingResponse
     * @return
     */
    public static Response mapEventPollingServiceResponse(EventPollingResponse eventPollingResponse) {

        if (EventNotificationConstants.OK.equals(eventPollingResponse.getStatus())) {
            return Response.status(Response.Status.OK).entity(eventPollingResponse.getResponseBody()).build();
        } else {
            if (eventPollingResponse.getErrorResponse() instanceof String) {
                return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationUtils.getErrorDTO(
                        EventNotificationEndPointConstants.INVALID_REQUEST,
                        eventPollingResponse.getErrorResponse().toString())).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity(eventPollingResponse.getErrorResponse())
                        .build();
            }
        }
    }

    /**
     * Method to map Event Polling Service error to API response.
     * @return EventNotificationErrorDTO
     */
    public static EventNotificationErrorDTO getErrorDTO(String error, String errorDescription) {
        EventNotificationErrorDTO eventNotificationErrorDTO = new EventNotificationErrorDTO();
        eventNotificationErrorDTO.setError(error);
        eventNotificationErrorDTO.setErrorDescription(errorDescription);
        return eventNotificationErrorDTO;
    }
}
