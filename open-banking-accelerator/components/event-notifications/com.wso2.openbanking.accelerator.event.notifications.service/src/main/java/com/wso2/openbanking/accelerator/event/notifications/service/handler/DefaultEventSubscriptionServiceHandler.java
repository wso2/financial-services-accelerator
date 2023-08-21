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

import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.dto.EventSubscriptionDTO;
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.model.EventSubscription;
import com.wso2.openbanking.accelerator.event.notifications.service.response.EventSubscriptionResponse;
import com.wso2.openbanking.accelerator.event.notifications.service.service.EventSubscriptionService;
import com.wso2.openbanking.accelerator.event.notifications.service.util.EventNotificationServiceUtil;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the default service handler for event notification subscription.
 */
public class DefaultEventSubscriptionServiceHandler implements EventSubscriptionServiceHandler {
    private static final Log log = LogFactory.getLog(DefaultEventSubscriptionServiceHandler.class);

    private EventSubscriptionService eventSubscriptionService = new EventSubscriptionService();

    public void setEventSubscriptionService(EventSubscriptionService eventSubscriptionService) {
        this.eventSubscriptionService = eventSubscriptionService;
    }

    /**
     * This method is used to create event subscriptions.
     *
     * @param eventSubscriptionRequestDto
     * @return EventSubscriptionResponse
     */
    public EventSubscriptionResponse createEventSubscription(EventSubscriptionDTO eventSubscriptionRequestDto) {
        EventSubscriptionResponse eventSubscriptionResponse = new EventSubscriptionResponse();

        EventSubscriptionResponse clientIdValidation = validateClientId(eventSubscriptionRequestDto.getClientId());
        // check whether clientIdValidation is not null, then return the error response
        if (clientIdValidation != null) {
            return clientIdValidation;
        }

        EventSubscription eventSubscription = mapEventSubscriptionDtoToModel(eventSubscriptionRequestDto);

        try {
            EventSubscription createEventSubscriptionResponse = eventSubscriptionService.
                    createEventSubscription(eventSubscription);
            eventSubscriptionResponse.setStatus(EventNotificationConstants.CREATED);
            eventSubscriptionResponse.
                    setResponseBody(mapSubscriptionModelToResponseJson(createEventSubscriptionResponse));
            return eventSubscriptionResponse;
        } catch (OBEventNotificationException e) {
            log.error("Error occurred while creating event subscription", e);
            eventSubscriptionResponse.setStatus(EventNotificationConstants.INTERNAL_SERVER_ERROR);
            eventSubscriptionResponse.setErrorResponse("Error occurred while creating event subscription");
            return eventSubscriptionResponse;
        }

    }

    /**
     * This method is used to retrieve a single event subscription.
     *
     * @param clientId
     * @param subscriptionId
     * @return EventSubscriptionResponse
     */
    public EventSubscriptionResponse getEventSubscription(String clientId, String subscriptionId) {
        EventSubscriptionResponse eventSubscriptionResponse = new EventSubscriptionResponse();

        EventSubscriptionResponse clientIdValidation = validateClientId(clientId);
        // check whether clientIdValidation is not null, then return the error response
        if (clientIdValidation != null) {
            return clientIdValidation;
        }

        try {
            EventSubscription eventSubscription = eventSubscriptionService.
                    getEventSubscriptionBySubscriptionId(subscriptionId);
            eventSubscriptionResponse.setStatus(EventNotificationConstants.OK);
            eventSubscriptionResponse.setResponseBody(mapSubscriptionModelToResponseJson(eventSubscription));
            return eventSubscriptionResponse;
        } catch (OBEventNotificationException e) {
            log.error("Error occurred while retrieving event subscription", e);
            if (e.getMessage().equals(EventNotificationConstants.EVENT_SUBSCRIPTION_NOT_FOUND)) {
                eventSubscriptionResponse.setStatus(EventNotificationConstants.BAD_REQUEST);
                eventSubscriptionResponse.setErrorResponse("Event subscription not found");
            } else {
                eventSubscriptionResponse.setStatus(EventNotificationConstants.INTERNAL_SERVER_ERROR);
                eventSubscriptionResponse.setErrorResponse("Error occurred while retrieving event subscription");
            }
            return eventSubscriptionResponse;
        }
    }

    /**
     * This method is used to retrieve all event subscriptions of a client.
     *
     * @param clientId
     * @return EventSubscriptionResponse
     */
    public EventSubscriptionResponse getAllEventSubscriptions(String clientId) {
        EventSubscriptionResponse eventSubscriptionResponse = new EventSubscriptionResponse();

        EventSubscriptionResponse clientIdValidation = validateClientId(clientId);
        // check whether clientIdValidation is not null, then return the error response
        if (clientIdValidation != null) {
            return clientIdValidation;
        }

        try {
            List<EventSubscription> eventSubscriptionList = eventSubscriptionService.
                    getEventSubscriptionsByClientId(clientId);
            List<JSONObject> eventSubscriptionResponseList = new ArrayList<>();
            for (EventSubscription eventSubscription : eventSubscriptionList) {
                eventSubscriptionResponseList.add(mapSubscriptionModelToResponseJson(eventSubscription));
            }
            eventSubscriptionResponse.setStatus(EventNotificationConstants.OK);
            eventSubscriptionResponse.setResponseBody(eventSubscriptionResponseList);
            return eventSubscriptionResponse;
        } catch (OBEventNotificationException e) {
            log.error("Error occurred while retrieving event subscriptions", e);
            eventSubscriptionResponse.setStatus(EventNotificationConstants.INTERNAL_SERVER_ERROR);
            eventSubscriptionResponse.setErrorResponse("Error occurred while retrieving event subscription.");
            return eventSubscriptionResponse;
        }
    }

    /**
     * This method is used to retrieve all event subscriptions by event type.
     *
     * @param clientId
     * @param eventType
     * @return
     */
    public EventSubscriptionResponse getEventSubscriptionsByEventType(String clientId, String eventType) {
        EventSubscriptionResponse eventSubscriptionResponse = new EventSubscriptionResponse();

        EventSubscriptionResponse clientIdValidation = validateClientId(clientId);
        // check whether clientIdValidation is not null, then return the error response
        if (clientIdValidation != null) {
            return clientIdValidation;
        }

        try {
            List<EventSubscription> eventSubscriptionList = eventSubscriptionService.
                    getEventSubscriptionsByClientIdAndEventType(eventType);
            List<JSONObject> eventSubscriptionResponseList = new ArrayList<>();
            for (EventSubscription eventSubscription : eventSubscriptionList) {
                eventSubscriptionResponseList.add(mapSubscriptionModelToResponseJson(eventSubscription));
            }
            eventSubscriptionResponse.setStatus(EventNotificationConstants.OK);
            eventSubscriptionResponse.setResponseBody(eventSubscriptionResponseList);
            return eventSubscriptionResponse;
        } catch (OBEventNotificationException e) {
            log.error("Error occurred while retrieving event subscriptions", e);
            eventSubscriptionResponse.setStatus(EventNotificationConstants.INTERNAL_SERVER_ERROR);
            eventSubscriptionResponse.setErrorResponse("Error occurred while retrieving event subscription.");
            return eventSubscriptionResponse;
        }
    }

    /**
     * This method is used to update an event subscription.
     *
     * @param eventSubscriptionUpdateRequestDto
     * @return EventSubscriptionResponse
     */
    public EventSubscriptionResponse updateEventSubscription(EventSubscriptionDTO eventSubscriptionUpdateRequestDto) {
        EventSubscriptionResponse eventSubscriptionResponse = new EventSubscriptionResponse();

        EventSubscriptionResponse clientIdValidation = validateClientId(eventSubscriptionUpdateRequestDto.
                getClientId());
        // check whether clientIdValidation is not null, then return the error response
        if (clientIdValidation != null) {
            return clientIdValidation;
        }

        EventSubscription eventSubscription = mapEventSubscriptionDtoToModel(eventSubscriptionUpdateRequestDto);

        try {
            Boolean isUpdated = eventSubscriptionService.updateEventSubscription(eventSubscription);
            if (!isUpdated) {
                eventSubscriptionResponse.setStatus(EventNotificationConstants.BAD_REQUEST);
                eventSubscriptionResponse.setErrorResponse("Event subscription not found.");
                return eventSubscriptionResponse;
            }
            eventSubscriptionResponse.setStatus(EventNotificationConstants.OK);
            EventSubscription eventSubscriptionUpdateResponse = eventSubscriptionService.
                    getEventSubscriptionBySubscriptionId(eventSubscriptionUpdateRequestDto.getSubscriptionId());
            eventSubscriptionResponse.
                    setResponseBody(mapSubscriptionModelToResponseJson(eventSubscriptionUpdateResponse));
            return eventSubscriptionResponse;
        } catch (OBEventNotificationException e) {
            log.error("Error occurred while updating event subscription", e);
            eventSubscriptionResponse.setStatus(EventNotificationConstants.INTERNAL_SERVER_ERROR);
            eventSubscriptionResponse.setErrorResponse("Error occurred while updating event subscription.");
            return eventSubscriptionResponse;
        }
    }

    /**
     * This method is used to delete an event subscription.
     *
     * @param clientId
     * @param subscriptionId
     * @return EventSubscriptionResponse
     */
    public EventSubscriptionResponse deleteEventSubscription(String clientId, String subscriptionId) {
        EventSubscriptionResponse eventSubscriptionResponse = new EventSubscriptionResponse();

        EventSubscriptionResponse clientIdValidation = validateClientId(clientId);
        // check whether clientIdValidation is not null, then return the error response
        if (clientIdValidation != null) {
            return clientIdValidation;
        }
        try {
            Boolean isDeleted = eventSubscriptionService.deleteEventSubscription(subscriptionId);
            if (!isDeleted) {
                eventSubscriptionResponse.setStatus(EventNotificationConstants.BAD_REQUEST);
                eventSubscriptionResponse.setErrorResponse("Event subscription not found");
                return eventSubscriptionResponse;
            }
            eventSubscriptionResponse.setStatus(EventNotificationConstants.NO_CONTENT);
            return eventSubscriptionResponse;
        } catch (OBEventNotificationException e) {
            log.error("Error occurred while deleting event subscription", e);
            eventSubscriptionResponse.setStatus(EventNotificationConstants.INTERNAL_SERVER_ERROR);
            eventSubscriptionResponse.setErrorResponse("Error occurred while deleting event subscription");
            return eventSubscriptionResponse;
        }
    }

    /**
     * This method is used to validate the client ID.
     *
     * @param clientId
     * @return EventSubscriptionResponse if the client ID is invalid, if the client ID is valid, null will be returned.
     */
    private EventSubscriptionResponse validateClientId(String clientId) {
        try {
            EventNotificationServiceUtil.validateClientId(clientId);
        } catch (OBEventNotificationException e) {
            log.error("Invalid client ID", e);
            EventSubscriptionResponse eventSubscriptionResponse = new EventSubscriptionResponse();
            eventSubscriptionResponse.setStatus(EventNotificationConstants.BAD_REQUEST);
            eventSubscriptionResponse.setErrorResponse(String.format("A client was not found" +
                    " for the client id : '%s' in the database.. ", clientId));
            return eventSubscriptionResponse;
        }
        return null;
    }

    /**
     * This method will map the event subscription DTO to event subscription model
     * to be passed to the dao layer.
     *
     * @param eventSubscriptionDTO
     * @return EventSubscription
     */
    private EventSubscription mapEventSubscriptionDtoToModel(EventSubscriptionDTO eventSubscriptionDTO) {
        EventSubscription eventSubscription = new EventSubscription();

        eventSubscription.setSubscriptionId(eventSubscriptionDTO.getSubscriptionId());

        JSONObject payload = eventSubscriptionDTO.getRequestData();
        List<String> eventTypes = new ArrayList<>();
        Object eventTypesObj = payload.get(EventNotificationConstants.EVENT_TYPE_PARAM);
        if (eventTypesObj instanceof List) {
            List<?> eventTypesList = (List<?>) eventTypesObj;
            for (Object item : eventTypesList) {
                if (item instanceof String) {
                    eventTypes.add((String) item);
                }
            }
        }
        eventSubscription.setEventTypes(eventTypes);
        eventSubscription.setCallbackUrl(payload.get(EventNotificationConstants.CALLBACK_URL_PARAM) != null ?
                payload.get(EventNotificationConstants.CALLBACK_URL_PARAM).toString() : null);
        eventSubscription.setSpecVersion(payload.get(EventNotificationConstants.VERSION_PARAM) != null ?
                payload.get(EventNotificationConstants.VERSION_PARAM).toString() : null);
        eventSubscription.setClientId(eventSubscriptionDTO.getClientId());
        eventSubscription.setRequestData(payload.toJSONString());
        return eventSubscription;
    }

    /**
     * This method is used to create the response JSON object from the event subscription model.
     *
     * @param eventSubscription
     * @return JSONObject
     */
    public JSONObject mapSubscriptionModelToResponseJson(EventSubscription eventSubscription) {
        JSONObject responsePayload = new JSONObject();

        if (eventSubscription.getSubscriptionId() != null) {
            responsePayload.put(EventNotificationConstants.SUBSCRIPTION_ID_PARAM,
                    eventSubscription.getSubscriptionId());
        }
        if (eventSubscription.getCallbackUrl() != null) {
            responsePayload.put(EventNotificationConstants.CALLBACK_URL_PARAM, eventSubscription.getCallbackUrl());
        }
        if (eventSubscription.getSpecVersion() != null) {
            responsePayload.put(EventNotificationConstants.VERSION_PARAM, eventSubscription.getSpecVersion());
        }
        if (eventSubscription.getEventTypes() != null) {
            responsePayload.put(EventNotificationConstants.EVENT_TYPE_PARAM, eventSubscription.getEventTypes());
        }
        return responsePayload;
    }
}
