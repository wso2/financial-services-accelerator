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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.event.notifications.endpoint.constants.EventNotificationEndPointConstants;
import org.wso2.financial.services.accelerator.event.notifications.endpoint.util.EventNotificationUtils;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.dto.NotificationCreationDTO;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.handler.EventCreationServiceHandler;
import org.wso2.financial.services.accelerator.event.notifications.service.model.EventCreationResponse;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationServiceUtil;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

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
 * Events creation API.
 */
@Path("/")
public class EventCreationEndpoint {

    private static final Log log = LogFactory.getLog(EventCreationEndpoint.class);
    private final EventCreationServiceHandler eventCreationServiceHandler;
    private static final String specialChars = "!@#$%&*()'+,./:;<=>?[]^_`{|}";
    private static final String illegalChars = "\\\\r|\\\\n|\\r|\\n|\\[|]| ";

    public EventCreationEndpoint() {

        eventCreationServiceHandler = EventNotificationUtils.getEventNotificationCreationServiceHandler();
    }

    public EventCreationEndpoint(EventCreationServiceHandler handler) {

        eventCreationServiceHandler = handler;
    }

    /**
     * This API will be used to create events.
     */
    @SuppressFBWarnings({"JAXRS_ENDPOINT", "SERVLET_HEADER"})
    // Suppressed content - Endpoint
    // Suppression reason - False Positive : This endpoint is secured with access control lists in the configuration
    // Suppressed content - request.getHeader()
    // Suppression reason - False Positive : Header is properly validated to ensure no special characters are passed
    // Suppressed warning count - 4
    @POST
    @Path("/create-events")
    @Consumes({"application/x-www-form-urlencoded"})
    @Produces({"application/json; charset=utf-8"})
    public Response createEvents(@Context HttpServletRequest request, @Context HttpServletResponse response,
                                 MultivaluedMap parameterMap) {


        NotificationCreationDTO notificationCreationDTO = new NotificationCreationDTO();
        String requestData = StringUtils.EMPTY;
        JSONObject notificationEvents;

        try {
            //Check if the request pay load is empty
            if (!parameterMap.isEmpty() && parameterMap.containsKey(EventNotificationEndPointConstants.REQUEST)) {

                requestData = parameterMap.get(EventNotificationEndPointConstants.REQUEST).
                        toString().replaceAll(illegalChars, StringUtils.EMPTY);

                byte[] decodedBytes = Base64.getDecoder().decode(requestData);
                String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
                notificationEvents = new JSONObject(decodedString);
                log.debug("Decoded payload string : " + decodedString.replaceAll("[\r\n]", ""));

            } else {
                log.error(EventNotificationEndPointConstants.MISSING_REQUEST_PAYLOAD);
                return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationServiceUtil
                        .getErrorDTO(EventNotificationEndPointConstants.MISSING_REQUEST_PAYLOAD,
                        EventNotificationConstants.MISSING_REQ_PAYLOAD)).build();
            }

            //check if the client id is present in the header
            String clientId = request.getHeader(EventNotificationEndPointConstants.X_WSO2_CLIENT_ID);
            if (!StringUtils.isBlank(clientId)) {
                notificationCreationDTO.setClientId(request.getHeader(
                        EventNotificationEndPointConstants.X_WSO2_CLIENT_ID));
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationServiceUtil
                        .getErrorDTO(EventNotificationEndPointConstants.MISSING_REQUEST_HEADER,
                        EventNotificationConstants.MISSING_HEADER_PARAM_CLIENT_ID)).build();
            }

            //check if the resource id is present in the header
            String resourceId = request.getHeader(EventNotificationEndPointConstants.X_WSO2_RESOURCE_ID);
            if (!StringUtils.isBlank(resourceId)) {
                if (StringUtils.containsAny(resourceId, specialChars)) {
                    return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationServiceUtil.
                            getErrorDTO(EventNotificationEndPointConstants.INVALID_REQUEST_HEADER,
                            EventNotificationConstants.INVALID_CHARS_IN_HEADER_ERROR)).build();
                }
                notificationCreationDTO.setResourceId(request.getHeader(
                        EventNotificationEndPointConstants.X_WSO2_RESOURCE_ID));;
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationServiceUtil.
                        getErrorDTO(EventNotificationEndPointConstants.MISSING_REQUEST_HEADER,
                        EventNotificationConstants.MISSING_HEADER_PARAM_RESOURCE_ID)).build();
            }

            //set events to notificationCreationDTO
            notificationEvents.keySet().forEach(eventName -> {
                JSONObject eventInformation = notificationEvents.getJSONObject(eventName);
                notificationCreationDTO.setEventPayload(eventName, eventInformation);
            });


            EventCreationResponse eventCreationResponse = eventCreationServiceHandler.
                    publishEvent(notificationCreationDTO);

            return EventNotificationUtils.mapEventCreationServiceResponse(eventCreationResponse);

        } catch (ClassCastException e) {
            log.error(EventNotificationEndPointConstants.REQUEST_PAYLOAD_ERROR, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationServiceUtil
                    .getErrorDTO(EventNotificationEndPointConstants.INVALID_REQUEST_PAYLOAD,
                            EventNotificationEndPointConstants.REQUEST_PAYLOAD_ERROR)).build();
        } catch (FSEventNotificationException e) {
            log.error(EventNotificationEndPointConstants.EVENT_CREATION_ERROR_RESPONSE, e);
            return Response.status(e.getStatus()).entity(EventNotificationServiceUtil
                    .getErrorDTO(EventNotificationEndPointConstants.INVALID_REQUEST,
                            e.getMessage())).build();
        }

    }

}
