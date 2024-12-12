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

package org.wso2.financial.services.accelerator.event.notifications.endpoint.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.event.notifications.endpoint.constants.EventNotificationEndPointConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.handler.EventCreationServiceHandler;
import org.wso2.financial.services.accelerator.event.notifications.service.handler.EventPollingServiceHandler;
import org.wso2.financial.services.accelerator.event.notifications.service.model.EventCreationResponse;
import org.wso2.financial.services.accelerator.event.notifications.service.model.EventPollingResponse;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationServiceUtil;

import javax.ws.rs.core.Response;

/**
 * This class will have util methods needed for event notifications.
 */
public class EventNotificationUtils {

    private static final Log log = LogFactory.getLog(EventNotificationUtils.class);

    private EventNotificationUtils() {

    }

    /**
     * This method is to get the event creation service handler as per the config.
     * @return EventCreationServiceHandler
     */
    public static EventCreationServiceHandler getEventNotificationCreationServiceHandler() {

        return (EventCreationServiceHandler)
                FinancialServicesUtils.getClassInstanceFromFQN(FinancialServicesConfigParser.getInstance().
                        getConfiguration().get(FinancialServicesConstants.EVENT_CREATION_HANDLER).toString());
    }

    /**
     * This method is to get the event polling service handler as per the config.
     * @return EventPollingServiceHandler
     */
    public static EventPollingServiceHandler getEventPollingServiceHandler() {

        return (EventPollingServiceHandler)
                FinancialServicesUtils.getClassInstanceFromFQN(FinancialServicesConfigParser.getInstance().
                        getConfiguration().get(FinancialServicesConstants.EVENT_POLLING_HANDLER).toString());
    }

    /**
     * Method to map the Event Creation Service Response to API response.
     * @param eventCreationResponse  EventCreationResponse
     * @return Response
     */
    public static Response mapEventCreationServiceResponse(EventCreationResponse eventCreationResponse) {

        if (EventNotificationConstants.CREATED.equals(eventCreationResponse.getStatus())) {

            return Response.status(Response.Status.CREATED)
                    .entity(eventCreationResponse.getResponseBody().toString()).build();

        }

        return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationServiceUtil.getErrorDTO(
                EventNotificationEndPointConstants.INVALID_REQUEST,
                EventNotificationEndPointConstants.EVENT_CREATION_ERROR_RESPONSE)).build();
    }

    /**
     * Method to map Event Polling Service to API response.
     * @param eventPollingResponse EventPollingResponse
     * @return Response
     */
    public static Response mapEventPollingServiceResponse(EventPollingResponse eventPollingResponse) {

        String responseBody = eventPollingResponse.getResponseBody().toString();
        if (EventNotificationConstants.OK.equals(eventPollingResponse.getStatus())) {
            return Response.status(Response.Status.OK).entity(responseBody).build();
        } else if (EventNotificationConstants.NOT_FOUND.equals(eventPollingResponse.getStatus())) {
            return Response.status(Response.Status.NOT_FOUND).entity(responseBody).build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * Get mapped Response.Status for the given status value.
     * @param status status value
     * @return Mapped Response.Status
     */
    private static Response.Status getErrorResponseStatus(String status) {

        if (EventNotificationConstants.NOT_FOUND.equals(status)) {
            return Response.Status.NOT_FOUND;
        } else if (EventNotificationConstants.BAD_REQUEST.equals(status)) {
            return Response.Status.BAD_REQUEST;
        } else {
            return Response.Status.INTERNAL_SERVER_ERROR;
        }
    }
}
