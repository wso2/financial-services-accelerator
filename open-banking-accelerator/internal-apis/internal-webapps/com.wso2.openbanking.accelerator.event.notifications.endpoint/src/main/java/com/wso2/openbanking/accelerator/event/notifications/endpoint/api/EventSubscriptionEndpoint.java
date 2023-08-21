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

package com.wso2.openbanking.accelerator.event.notifications.endpoint.api;

import com.wso2.openbanking.accelerator.event.notifications.endpoint.constants.EventNotificationEndPointConstants;
import com.wso2.openbanking.accelerator.event.notifications.endpoint.util.EventNotificationUtils;
import com.wso2.openbanking.accelerator.event.notifications.endpoint.util.EventSubscriptionUtils;
import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.dto.EventSubscriptionDTO;
import com.wso2.openbanking.accelerator.event.notifications.service.handler.EventSubscriptionServiceHandler;
import com.wso2.openbanking.accelerator.event.notifications.service.response.EventSubscriptionResponse;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.annotations.ApiOperation;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Events Notification Subscription API.
 */
@Path("/subscription")
public class EventSubscriptionEndpoint {
    private static final Log log = LogFactory.getLog(EventSubscriptionEndpoint.class);

    private static final EventSubscriptionServiceHandler eventSubscriptionServiceHandler = EventSubscriptionUtils.
            getEventSubscriptionServiceHandler();

    /**
     * Register an Event Notification Subscription.
     */
    @SuppressFBWarnings({"JAXRS_ENDPOINT", "SERVLET_HEADER"})
    @POST
    @Path("/")
    @Consumes({"application/json; charset=utf-8"})
    @Produces({"application/json; charset=utf-8"})
    @ApiOperation(value = "Create Subscriptions", tags = {" Create Subscriptions"})
    public Response registerSubscription(@Context HttpServletRequest request, @Context HttpServletResponse response) {

        EventSubscriptionDTO eventSubscriptionDTO = new EventSubscriptionDTO();

        //check if the client id is present in the header
        String clientId = request.getHeader(EventNotificationEndPointConstants.X_WSO2_CLIENT_ID);
        if (StringUtils.isBlank(clientId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationUtils.
                    getErrorDTO(EventNotificationEndPointConstants.MISSING_REQUEST_HEADER,
                            EventNotificationConstants.MISSING_HEADER_PARAM_CLIENT_ID)).build();
        }
        eventSubscriptionDTO.setClientId(request.getHeader(EventNotificationEndPointConstants.X_WSO2_CLIENT_ID));

        // extract the payload from the request
        try {
            JSONObject requestData = EventSubscriptionUtils.getJSONObjectPayload(request);
            if (requestData != null) {
                eventSubscriptionDTO.setRequestData(requestData);
            } else {
                log.error("Subscription request payload is missing");
                return Response.status(Response.Status.BAD_REQUEST).
                        entity(EventNotificationUtils.
                                getErrorDTO(EventNotificationEndPointConstants.MISSING_REQUEST_PAYLOAD,
                                        EventNotificationEndPointConstants.MISSING_JSON_REQUEST_PAYLOAD)).build();
            }
        } catch (IOException e) {
            log.error("Invalid Payload received", e);
            return Response.status(Response.Status.BAD_REQUEST).
                    entity(EventNotificationUtils.
                            getErrorDTO(EventNotificationEndPointConstants.INVALID_REQUEST_PAYLOAD,
                                    EventNotificationEndPointConstants.REQUEST_PAYLOAD_ERROR)).build();
        } catch (ParseException e) {
            return Response.status(Response.Status.BAD_REQUEST).
                    entity(EventNotificationUtils.
                            getErrorDTO(EventNotificationEndPointConstants.INVALID_REQUEST_PAYLOAD,
                                    EventNotificationEndPointConstants.ERROR_PAYLOAD_PARSE)).build();
        }
        EventSubscriptionResponse eventSubscriptionResponse = eventSubscriptionServiceHandler.
                createEventSubscription(eventSubscriptionDTO);
        return EventSubscriptionUtils.mapEventSubscriptionServiceResponse(eventSubscriptionResponse);
    }

    /**
     * Retrieve a Single Event Subscription.
     */
    @SuppressFBWarnings({"JAXRS_ENDPOINT", "SERVLET_HEADER"})
    @GET
    @Path("/{subscriptionId}")
    @Consumes({"application/json; charset=utf-8"})
    @Produces({"application/json; charset=utf-8"})
    public Response retrieveSubscription(@Context HttpServletRequest request, @Context HttpServletResponse response,
                                         @Context UriInfo uriInfo) {
        //check if the client id is present in the header
        String clientId = request.getHeader(EventNotificationEndPointConstants.X_WSO2_CLIENT_ID);
        if (StringUtils.isBlank(clientId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationUtils.
                    getErrorDTO(EventNotificationEndPointConstants.MISSING_REQUEST_HEADER,
                            EventNotificationConstants.MISSING_HEADER_PARAM_CLIENT_ID)).build();
        }

        EventSubscriptionResponse eventSubscriptionResponse = eventSubscriptionServiceHandler.
                getEventSubscription(clientId, uriInfo.getPathParameters().getFirst("subscriptionId"));
        return EventSubscriptionUtils.mapEventSubscriptionServiceResponse(eventSubscriptionResponse);
    }

    /**
     * Retrieve All Events Subscriptions of a Client.
     */
    @SuppressFBWarnings({"JAXRS_ENDPOINT", "SERVLET_HEADER"})
    @GET
    @Path("/")
    @Produces({"application/json; charset=utf-8"})
    public Response retrieveAllSubscriptions(@Context HttpServletRequest request,
                                             @Context HttpServletResponse response) {

        //check if the client id is present in the header
        String clientId = request.getHeader(EventNotificationEndPointConstants.X_WSO2_CLIENT_ID);
        if (StringUtils.isBlank(clientId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationUtils.
                    getErrorDTO(EventNotificationEndPointConstants.MISSING_REQUEST_HEADER,
                            EventNotificationConstants.MISSING_HEADER_PARAM_CLIENT_ID)).build();
        }

        EventSubscriptionResponse eventSubscriptionResponse = eventSubscriptionServiceHandler.
                getAllEventSubscriptions(clientId);
        return EventSubscriptionUtils.mapEventSubscriptionServiceResponse(eventSubscriptionResponse);
    }

    /**
     * Retrieve All Events Subscriptions by an event type.
     */
    @SuppressFBWarnings({"JAXRS_ENDPOINT", "SERVLET_HEADER"})
    @GET
    @Path("/type/{eventType}")
    @Produces({"application/json; charset=utf-8"})
    public Response retrieveAllSubscriptionsByEventType(@Context HttpServletRequest request,
                                                        @Context HttpServletResponse response,
                                                        @Context UriInfo uriInfo) {

        //check if the client id is present in the header
        String clientId = request.getHeader(EventNotificationEndPointConstants.X_WSO2_CLIENT_ID);
        if (StringUtils.isBlank(clientId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationUtils.
                    getErrorDTO(EventNotificationEndPointConstants.MISSING_REQUEST_HEADER,
                            EventNotificationConstants.MISSING_HEADER_PARAM_CLIENT_ID)).build();
        }

        EventSubscriptionResponse eventSubscriptionResponse = eventSubscriptionServiceHandler.
                getEventSubscriptionsByEventType(clientId, uriInfo.getPathParameters().getFirst("eventType"));
        return EventSubscriptionUtils.mapEventSubscriptionServiceResponse(eventSubscriptionResponse);
    }

    /**
     * Update an Event Subscription.
     */
    @SuppressFBWarnings({"JAXRS_ENDPOINT", "SERVLET_HEADER"})
    @PUT
    @Path("/{subscriptionId}")
    @Consumes({"application/json; charset=utf-8"})
    @Produces({"application/json; charset=utf-8"})
    public Response updateSubscription(@Context HttpServletRequest request, @Context HttpServletResponse response,
                                       @Context UriInfo uriInfo) {
        EventSubscriptionDTO eventSubscriptionDTO = new EventSubscriptionDTO();

        //check if the client id is present in the header
        String clientId = request.getHeader(EventNotificationEndPointConstants.X_WSO2_CLIENT_ID);
        if (StringUtils.isBlank(clientId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationUtils.
                    getErrorDTO(EventNotificationEndPointConstants.MISSING_REQUEST_HEADER,
                            EventNotificationConstants.MISSING_HEADER_PARAM_CLIENT_ID)).build();
        }
        eventSubscriptionDTO.setClientId(request.getHeader(EventNotificationConstants.X_WSO2_CLIENT_ID));

        // extract the payload from the request
        try {
            JSONObject requestData = EventSubscriptionUtils.getJSONObjectPayload(request);
            if (requestData != null) {
                eventSubscriptionDTO.setRequestData(requestData);
            } else {
                log.error("Subscription request payload is missing");
                return Response.status(Response.Status.BAD_REQUEST).
                        entity(EventNotificationUtils.
                                getErrorDTO(EventNotificationEndPointConstants.MISSING_REQUEST_PAYLOAD,
                                        EventNotificationEndPointConstants.MISSING_JSON_REQUEST_PAYLOAD)).build();
            }
        } catch (IOException e) {
            log.error("Invalid Payload received", e);
            return Response.status(Response.Status.BAD_REQUEST).
                    entity(EventNotificationUtils.
                            getErrorDTO(EventNotificationEndPointConstants.INVALID_REQUEST_PAYLOAD,
                                    EventNotificationEndPointConstants.REQUEST_PAYLOAD_ERROR)).build();
        } catch (ParseException e) {
            return Response.status(Response.Status.BAD_REQUEST).
                    entity(EventNotificationUtils.
                            getErrorDTO(EventNotificationEndPointConstants.INVALID_REQUEST_PAYLOAD,
                                    EventNotificationEndPointConstants.ERROR_PAYLOAD_PARSE)).build();
        }

        eventSubscriptionDTO.setSubscriptionId(uriInfo.getPathParameters().getFirst("subscriptionId"));
        EventSubscriptionResponse eventSubscriptionResponse = eventSubscriptionServiceHandler.
                updateEventSubscription(eventSubscriptionDTO);
        return EventSubscriptionUtils.mapEventSubscriptionServiceResponse(eventSubscriptionResponse);
    }

    /**
     * Delete an Event Subscription.
     */
    @SuppressFBWarnings({"JAXRS_ENDPOINT", "SERVLET_HEADER"})
    @DELETE
    @Path("/{subscriptionId}")
    @Consumes({"application/json; charset=utf-8"})
    @Produces({"application/json; charset=utf-8"})
    public Response deleteSubscription(@Context HttpServletRequest request, @Context HttpServletResponse response,
                                       @Context UriInfo uriInfo) {
        //check if the client id is present in the header
        String clientId = request.getHeader(EventNotificationEndPointConstants.X_WSO2_CLIENT_ID);
        if (StringUtils.isBlank(clientId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationUtils.
                    getErrorDTO(EventNotificationEndPointConstants.MISSING_REQUEST_HEADER,
                            EventNotificationConstants.MISSING_HEADER_PARAM_CLIENT_ID)).build();
        }

        EventSubscriptionResponse eventSubscriptionResponse = eventSubscriptionServiceHandler.
                deleteEventSubscription(clientId, uriInfo.getPathParameters().getFirst("subscriptionId"));
        return EventSubscriptionUtils.mapEventSubscriptionServiceResponse(eventSubscriptionResponse);
    }

}
