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

package org.wso2.financial.services.accelerator.event.notifications.endpoint.api;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.event.notifications.endpoint.constants.EventNotificationEndPointConstants;
import org.wso2.financial.services.accelerator.event.notifications.endpoint.util.EventNotificationUtils;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.dto.EventPollingDTO;
import org.wso2.financial.services.accelerator.event.notifications.service.handler.EventPollingServiceHandler;
import org.wso2.financial.services.accelerator.event.notifications.service.model.EventPollingResponse;
import org.wso2.financial.services.accelerator.event.notifications.service.model.NotificationError;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationServiceUtil;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * Aggregated Event Polling API Specification.
 *
 * <p>Swagger for Aggregated Event Polling API Specification
 */
@Path("/events")
public class EventPollingEndpoint {

    private static final Log log = LogFactory.getLog(EventPollingEndpoint.class);
    private EventPollingServiceHandler eventPollingServiceHandler;

    public EventPollingEndpoint() {

        eventPollingServiceHandler = EventNotificationUtils.getEventPollingServiceHandler();
    }

    public EventPollingEndpoint(EventPollingServiceHandler handler) {

        eventPollingServiceHandler = handler;
    }

    /**
     * Retrieve Event Notifications Using Aggregated Polling.
     */
    @SuppressFBWarnings({"JAXRS_ENDPOINT", "SERVLET_HEADER"})
    // Suppressed content - Endpoint
    // Suppression reason - False Positive : This endpoint is secured with access control lists in the configuration
    // Suppressed content - request.getHeader()
    // Suppression reason - False Positive : Header is properly validated to ensure no special characters are passed
    // Suppressed warning count - 4
    @POST
    @Path("/{s:.*}")
    @Consumes({"application/x-www-form-urlencoded"})
    @Produces({"application/json; charset=utf-8", "application/jose+jwe"})
    @ApiOperation(value = "Retrieve Events", tags = {"Events"})

    public Response pollEvents(@Context HttpServletRequest request, @Context HttpServletResponse response,
                               MultivaluedMap parameterMap) {

        String eventPollingData;
        JSONObject eventPollingRequest;

        if (!parameterMap.isEmpty() && parameterMap.containsKey(EventNotificationEndPointConstants.REQUEST)) {

            eventPollingData = parameterMap.get(EventNotificationEndPointConstants.REQUEST).
                    toString().replaceAll("\\\\r|\\\\n|\\r|\\n|\\[|]| ", StringUtils.EMPTY);

            if (StringUtils.isNotBlank(eventPollingData)) {
                byte[] decodedBytes = Base64.getDecoder().decode(eventPollingData);
                String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
                try {
                    eventPollingRequest = new JSONObject(decodedString);

                    //check if the client id is present in the header
                    String clientId = request.getHeader(EventNotificationEndPointConstants.X_WSO2_CLIENT_ID);
                    if (!StringUtils.isBlank(clientId)) {
                        eventPollingRequest.put(EventNotificationEndPointConstants.X_WSO2_CLIENT_ID, request.
                                getHeader(EventNotificationEndPointConstants.X_WSO2_CLIENT_ID));
                    } else {
                        return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationServiceUtil
                                .getErrorDTO(EventNotificationEndPointConstants.MISSING_REQUEST_HEADER,
                                EventNotificationConstants.MISSING_HEADER_PARAM_CLIENT_ID).toString()).build();
                    }

                    EventPollingDTO pollingDTO = mapPollingRequestToDTO(eventPollingRequest);
                    EventPollingResponse eventPollingResponse = eventPollingServiceHandler.
                            pollEvents(pollingDTO);

                    return EventNotificationUtils.mapEventPollingServiceResponse(eventPollingResponse);

                } catch (ClassCastException e) {
                    log.error(EventNotificationEndPointConstants.REQUEST_PAYLOAD_ERROR, e);
                    return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationServiceUtil
                            .getErrorDTO(EventNotificationEndPointConstants.INVALID_REQUEST_PAYLOAD,
                            EventNotificationEndPointConstants.REQUEST_PAYLOAD_ERROR).toString()).build();
                }
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationServiceUtil
                        .getErrorDTO(EventNotificationEndPointConstants.INVALID_REQUEST_PAYLOAD,
                        EventNotificationEndPointConstants.EMPTY_REQ_PAYLOAD).toString()).build();
            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationServiceUtil
                    .getErrorDTO(EventNotificationEndPointConstants.MISSING_REQUEST_PAYLOAD,
                    EventNotificationConstants.MISSING_REQ_PAYLOAD).toString()).build();
        }
    }

    /**
     * This method will map the eventPollingRequest JSON to EventPollingDTO.
     * @param eventPollingRequest JSON request for event polling
     * @return EventPollingDTO
     */
    public EventPollingDTO mapPollingRequestToDTO(JSONObject eventPollingRequest) {

        EventPollingDTO eventPollingDTO = new EventPollingDTO();
        eventPollingDTO.setClientId(eventPollingRequest.get(EventNotificationConstants.X_WSO2_CLIENT_ID).toString());

        if (eventPollingRequest.length() == 0) {

            eventPollingDTO.setMaxEvents(FinancialServicesConfigParser.getInstance().getNumberOfSetsToReturn());

            return eventPollingDTO;
        }

        //Set acknowledged events to DTO
        if (eventPollingRequest.has(EventNotificationConstants.ACK.toLowerCase(Locale.ROOT))) {
            JSONArray acknowledgedEvents = (JSONArray) eventPollingRequest.
                    get(EventNotificationConstants.ACK.toLowerCase(Locale.ROOT));
            acknowledgedEvents.forEach((event -> {
                eventPollingDTO.setAck(event.toString());
            }));
        }

        //Set error events to DTO
        if (eventPollingRequest.has(EventNotificationConstants.SET_ERRORS)) {
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
        if (eventPollingRequest.has(EventNotificationConstants.MAX_EVENTS)) {
            eventPollingDTO.setMaxEvents(Integer.parseInt(eventPollingRequest.
                    get(EventNotificationConstants.MAX_EVENTS).toString()));
        } else {
            eventPollingDTO.setMaxEvents(FinancialServicesConfigParser.getInstance().getNumberOfSetsToReturn());
        }

        return eventPollingDTO;
    }

    private NotificationError getNotificationError(JSONObject errorEvent) {

        NotificationError notificationError = new NotificationError();
        notificationError.setErrorCode(errorEvent.get(
                EventNotificationConstants.ERROR.toLowerCase(Locale.ROOT)).toString());
        notificationError.setErrorDescription(
                errorEvent.get(EventNotificationConstants.DESCRIPTION).toString());
        return notificationError;
    }
}
