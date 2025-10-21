/**
 * Copyright (c) 2023-2025, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.openbanking.accelerator.event.notifications.endpoint.api;

import com.wso2.openbanking.accelerator.event.notifications.endpoint.constants.EventNotificationEndPointConstants;
import com.wso2.openbanking.accelerator.event.notifications.endpoint.util.EventNotificationUtils;
import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.dto.NotificationCreationDTO;
import com.wso2.openbanking.accelerator.event.notifications.service.handler.EventCreationServiceHandler;
import com.wso2.openbanking.accelerator.event.notifications.service.response.EventCreationResponse;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.annotations.ApiOperation;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private static final EventCreationServiceHandler eventCreationServiceHandler = EventNotificationUtils.
            getEventNotificationCreationServiceHandler();
    private static final String specialChars = "!@#$%&*()'+,./:;<=>?[]^_`{|}";

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

    @ApiOperation(value = "Create Events", tags = {" Create Events"})
    public Response createEvents(@Context HttpServletRequest request, @Context HttpServletResponse response,
                                 MultivaluedMap parameterMap) {


        NotificationCreationDTO notificationCreationDTO = new NotificationCreationDTO();
        String requestData = StringUtils.EMPTY;
        JSONObject notificationEvents;

        try {
            //Check if the request pay load is empty
            if (!parameterMap.isEmpty() && parameterMap.containsKey(EventNotificationEndPointConstants.REQUEST)) {

                requestData = parameterMap.get(EventNotificationEndPointConstants.REQUEST).
                        toString().replaceAll("\\\\r|\\\\n|\\r|\\n|\\[|]| ", StringUtils.EMPTY);

                byte[] decodedBytes = Base64.getDecoder().decode(requestData);
                String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
                notificationEvents = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(decodedString);
                log.debug("Decoded payload string : " + decodedString.replaceAll("[\r\n]", ""));

            } else {

                return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationUtils.getErrorDTO(
                        EventNotificationEndPointConstants.MISSING_REQUEST_PAYLOAD,
                        EventNotificationConstants.MISSING_REQ_PAYLOAD)).build();
            }

            //check if the client id is present in the header
            String clientId = request.getHeader(EventNotificationEndPointConstants.X_WSO2_CLIENT_ID);
            if (!StringUtils.isBlank(clientId)) {
                notificationCreationDTO.setClientId(request.getHeader(
                        EventNotificationEndPointConstants.X_WSO2_CLIENT_ID));
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationUtils.getErrorDTO(
                        EventNotificationEndPointConstants.MISSING_REQUEST_HEADER,
                        EventNotificationConstants.MISSING_HEADER_PARAM_CLIENT_ID)).build();
            }

            //check if the resource id is present in the header
            String resourceId = request.getHeader(EventNotificationEndPointConstants.X_WSO2_RESOURCE_ID);
            if (!StringUtils.isBlank(resourceId)) {
                if (StringUtils.containsAny(resourceId, specialChars)) {
                    return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationUtils.getErrorDTO(
                            EventNotificationEndPointConstants.INVALID_REQUEST_HEADER,
                            EventNotificationConstants.INVALID_CHARS_IN_HEADER_ERROR)).build();
                }
                notificationCreationDTO.setResourceId(request.getHeader(
                        EventNotificationEndPointConstants.X_WSO2_RESOURCE_ID));;
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationUtils.getErrorDTO(
                        EventNotificationEndPointConstants.MISSING_REQUEST_HEADER,
                        EventNotificationConstants.MISSING_HEADER_PARAM_RESOURCE_ID)).build();
            }

            //set events to notificationCreationDTO
            JSONObject finalNotificationEvents = notificationEvents;
            notificationEvents.keySet().forEach(eventName -> {
                JSONObject eventInformation = (JSONObject) finalNotificationEvents.get(eventName);
                notificationCreationDTO.setEventPayload(eventName, eventInformation);
            });

        } catch (ParseException e) {
            log.error("Error while parsing the request payload", e);
            return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationUtils
                    .getErrorDTO(EventNotificationEndPointConstants.INVALID_REQUEST_PAYLOAD,
                            EventNotificationEndPointConstants.REQUEST_PAYLOAD_ERROR)).build();
        } catch (ClassCastException e) {
            log.error(EventNotificationEndPointConstants.REQUEST_PAYLOAD_ERROR, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationUtils
                    .getErrorDTO(EventNotificationEndPointConstants.INVALID_REQUEST_PAYLOAD,
                            EventNotificationEndPointConstants.REQUEST_PAYLOAD_ERROR)).build();
        }

        EventCreationResponse eventCreationResponse = eventCreationServiceHandler.
                publishOBEvent(notificationCreationDTO);

        return EventNotificationUtils.mapEventCreationServiceResponse(eventCreationResponse);
    }

}
