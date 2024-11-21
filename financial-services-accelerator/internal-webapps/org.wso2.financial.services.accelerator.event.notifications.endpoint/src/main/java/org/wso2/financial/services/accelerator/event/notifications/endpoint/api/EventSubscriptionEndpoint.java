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
import org.wso2.financial.services.accelerator.event.notifications.endpoint.constants.EventNotificationEndPointConstants;
import org.wso2.financial.services.accelerator.event.notifications.endpoint.util.EventSubscriptionUtils;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.dto.EventSubscriptionDTO;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.handler.EventSubscriptionServiceHandler;
import org.wso2.financial.services.accelerator.event.notifications.service.model.EventSubscriptionResponse;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationServiceUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

        //check if the client id is present in the header
        String clientId = request.getHeader(EventNotificationEndPointConstants.X_WSO2_CLIENT_ID);
        if (StringUtils.isBlank(clientId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationServiceUtil.
                    getErrorDTO(EventNotificationEndPointConstants.MISSING_REQUEST_HEADER,
                            EventNotificationConstants.MISSING_HEADER_PARAM_CLIENT_ID)).build();
        }
        // extract the payload from the request
        try {
            JSONObject requestPayload = EventSubscriptionUtils.getJSONObjectPayload(request);
            EventSubscriptionDTO eventSubscriptionDTO = mapSubscriptionRequestToDTo(requestPayload);
            eventSubscriptionDTO.setClientId(request.getHeader(EventNotificationEndPointConstants.X_WSO2_CLIENT_ID));

            EventSubscriptionResponse eventSubscriptionResponse = eventSubscriptionServiceHandler.
                    createEventSubscription(eventSubscriptionDTO);
            return EventSubscriptionUtils.mapEventSubscriptionServiceResponse(eventSubscriptionResponse);
        } catch (IOException e) {
            log.error("Invalid Payload received", e);
            return Response.status(Response.Status.BAD_REQUEST).
                    entity(EventNotificationServiceUtil.
                            getErrorDTO(EventNotificationEndPointConstants.INVALID_REQUEST_PAYLOAD,
                                    EventNotificationEndPointConstants.REQUEST_PAYLOAD_ERROR)).build();
        } catch (FSEventNotificationException e) {
            return Response.status(e.getStatus()).entity(EventNotificationServiceUtil.
                    getErrorDTO(EventNotificationEndPointConstants.INVALID_REQUEST, e.getMessage())).build();
        }
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
            return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationServiceUtil.
                    getErrorDTO(EventNotificationEndPointConstants.MISSING_REQUEST_HEADER,
                            EventNotificationConstants.MISSING_HEADER_PARAM_CLIENT_ID)).build();
        }

        EventSubscriptionResponse eventSubscriptionResponse = null;
        try {
            eventSubscriptionResponse = eventSubscriptionServiceHandler.
                    getEventSubscription(clientId, uriInfo.getPathParameters()
                            .getFirst(EventNotificationConstants.SUBSCRIPTION_ID_PARAM));
            return EventSubscriptionUtils.mapEventSubscriptionServiceResponse(eventSubscriptionResponse);
        } catch (FSEventNotificationException e) {
            return Response.status(e.getStatus()).entity(EventNotificationServiceUtil.
                    getErrorDTO(EventNotificationEndPointConstants.INVALID_REQUEST, e.getMessage())).build();
        }
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
            return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationServiceUtil.
                    getErrorDTO(EventNotificationEndPointConstants.MISSING_REQUEST_HEADER,
                            EventNotificationConstants.MISSING_HEADER_PARAM_CLIENT_ID)).build();
        }

        EventSubscriptionResponse eventSubscriptionResponse = null;
        try {
            eventSubscriptionResponse = eventSubscriptionServiceHandler.
                    getAllEventSubscriptions(clientId);

            return EventSubscriptionUtils.mapEventSubscriptionServiceResponse(eventSubscriptionResponse);
        } catch (FSEventNotificationException e) {
            return Response.status(e.getStatus()).entity(EventNotificationServiceUtil.
                    getErrorDTO(EventNotificationEndPointConstants.INVALID_REQUEST, e.getMessage())).build();
        }
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
            return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationServiceUtil.
                    getErrorDTO(EventNotificationEndPointConstants.MISSING_REQUEST_HEADER,
                            EventNotificationConstants.MISSING_HEADER_PARAM_CLIENT_ID)).build();
        }

        EventSubscriptionResponse eventSubscriptionResponse = null;
        try {
            eventSubscriptionResponse = eventSubscriptionServiceHandler.getEventSubscriptionsByEventType(clientId,
                    uriInfo.getPathParameters().getFirst(EventNotificationConstants.EVENT_TYPE_PARAM));
            return EventSubscriptionUtils.mapEventSubscriptionServiceResponse(eventSubscriptionResponse);
        } catch (FSEventNotificationException e) {
            return Response.status(e.getStatus()).entity(EventNotificationServiceUtil.
                    getErrorDTO(EventNotificationEndPointConstants.INVALID_REQUEST, e.getMessage())).build();
        }
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

        //check if the client id is present in the header
        String clientId = request.getHeader(EventNotificationEndPointConstants.X_WSO2_CLIENT_ID);
        if (StringUtils.isBlank(clientId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationServiceUtil.
                    getErrorDTO(EventNotificationEndPointConstants.MISSING_REQUEST_HEADER,
                            EventNotificationConstants.MISSING_HEADER_PARAM_CLIENT_ID)).build();
        }

        // extract the payload from the request
        try {
            JSONObject requestPayload = EventSubscriptionUtils.getJSONObjectPayload(request);
            EventSubscriptionDTO eventSubscriptionDTO = mapSubscriptionRequestToDTo(requestPayload);
            eventSubscriptionDTO.setClientId(request.getHeader(EventNotificationEndPointConstants.X_WSO2_CLIENT_ID));

            eventSubscriptionDTO.setSubscriptionId(uriInfo.getPathParameters()
                    .getFirst(EventNotificationConstants.SUBSCRIPTION_ID_PARAM));
            EventSubscriptionResponse eventSubscriptionResponse = eventSubscriptionServiceHandler.
                    updateEventSubscription(eventSubscriptionDTO);
            return EventSubscriptionUtils.mapEventSubscriptionServiceResponse(eventSubscriptionResponse);
        } catch (IOException e) {
            log.error("Invalid Payload received", e);
            return Response.status(Response.Status.BAD_REQUEST).
                    entity(EventNotificationServiceUtil.
                            getErrorDTO(EventNotificationEndPointConstants.INVALID_REQUEST_PAYLOAD,
                                    EventNotificationEndPointConstants.REQUEST_PAYLOAD_ERROR)).build();
        } catch (FSEventNotificationException e) {
            return Response.status(e.getStatus()).entity(EventNotificationServiceUtil.
                    getErrorDTO(EventNotificationEndPointConstants.INVALID_REQUEST, e.getMessage())).build();
        }
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
            return Response.status(Response.Status.BAD_REQUEST).entity(EventNotificationServiceUtil.
                    getErrorDTO(EventNotificationEndPointConstants.MISSING_REQUEST_HEADER,
                            EventNotificationConstants.MISSING_HEADER_PARAM_CLIENT_ID)).build();
        }

        EventSubscriptionResponse eventSubscriptionResponse = null;
        try {
            eventSubscriptionResponse = eventSubscriptionServiceHandler.deleteEventSubscription(clientId,
                    uriInfo.getPathParameters().getFirst(EventNotificationConstants.SUBSCRIPTION_ID_PARAM));
            return EventSubscriptionUtils.mapEventSubscriptionServiceResponse(eventSubscriptionResponse);
        } catch (FSEventNotificationException e) {
            return Response.status(e.getStatus()).entity(EventNotificationServiceUtil.
                    getErrorDTO(EventNotificationEndPointConstants.INVALID_REQUEST, e.getMessage())).build();
        }
    }

    /**
     * Method to map the Subscription Request to DTO.
     * @param requestPayload   Subscription Request Payload
     * @return EventSubscriptionDTO
     */
    private EventSubscriptionDTO mapSubscriptionRequestToDTo(JSONObject requestPayload) {
        EventSubscriptionDTO eventSubscriptionDTO = new EventSubscriptionDTO();
        eventSubscriptionDTO.setCallbackUrl(requestPayload.has(EventNotificationConstants.CALLBACK_URL_PARAM) ?
                requestPayload.getString(EventNotificationConstants.CALLBACK_URL_PARAM) : null);
        eventSubscriptionDTO.setSpecVersion(requestPayload.has(EventNotificationConstants.VERSION_PARAM) ?
                requestPayload.getString(EventNotificationConstants.VERSION_PARAM) : null);
        eventSubscriptionDTO.setRequestData(requestPayload.toString());

        List<String> eventTypesList = new ArrayList<>();
        if (requestPayload.has(EventNotificationConstants.EVENT_TYPES_PARAM)) {
            JSONArray eventTypes = requestPayload.getJSONArray(EventNotificationConstants.EVENT_TYPES_PARAM);
            eventTypes.iterator().forEachRemaining(eventType -> {
                eventTypesList.add(eventType.toString());
            });
            eventSubscriptionDTO.setEventTypes(eventTypesList);
        }
        return eventSubscriptionDTO;
    }

}
