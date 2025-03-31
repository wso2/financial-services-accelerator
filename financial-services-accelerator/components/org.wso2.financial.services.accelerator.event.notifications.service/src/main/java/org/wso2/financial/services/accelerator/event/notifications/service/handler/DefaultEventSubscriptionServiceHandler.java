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

package org.wso2.financial.services.accelerator.event.notifications.service.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.extension.model.StatusEnum;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.event.notifications.service.EventSubscriptionService;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.dto.EventSubscriptionDTO;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.model.EventSubscription;
import org.wso2.financial.services.accelerator.event.notifications.service.model.EventSubscriptionResponse;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationServiceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This is the default service handler for event notification subscription.
 */
public class DefaultEventSubscriptionServiceHandler implements EventSubscriptionServiceHandler {
    private static final Log log = LogFactory.getLog(DefaultEventSubscriptionServiceHandler.class);

    private EventSubscriptionService eventSubscriptionService = new EventSubscriptionService();

    public void setEventSubscriptionService(EventSubscriptionService eventSubscriptionService) {
        this.eventSubscriptionService = eventSubscriptionService;
    }

   @Override
    public EventSubscriptionResponse createEventSubscription(EventSubscriptionDTO eventSubscriptionRequestDto)
            throws FSEventNotificationException {

       try {
           EventNotificationServiceUtil.validateClientId(eventSubscriptionRequestDto.getClientId());

       } catch (FSEventNotificationException e) {
           String errorMsg = String.format("A client was not found" + " for the client id : '%s' in the database. ",
                   eventSubscriptionRequestDto.getClientId().replaceAll("[\r\n]", ""));
           log.error(errorMsg, e);
           throw new FSEventNotificationException(HttpStatus.SC_BAD_REQUEST, errorMsg, e);
       }

       try {

           EventSubscription eventSubscription = mapEventSubscriptionDtoToModel(eventSubscriptionRequestDto);
           handleValidation(new JSONObject(eventSubscription),
                   EventNotificationConstants.EventSubscriptionOperationEnum.SubscriptionCreation);
           EventSubscription eventSubscriptionCreateResponse = eventSubscriptionService.
                   createEventSubscription(eventSubscription);

           EventSubscriptionResponse eventSubscriptionResponse = new EventSubscriptionResponse();
           eventSubscriptionResponse.setResponseStatus(HttpStatus.SC_CREATED);
           eventSubscriptionResponse.setResponseBody(handleResponseGeneration(eventSubscriptionCreateResponse,
                   EventNotificationConstants.EventSubscriptionOperationEnum.SubscriptionCreation));
           return eventSubscriptionResponse;
       } catch (FSEventNotificationException e) {
           log.error("Error occurred while creating event subscription", e);
           throw new FSEventNotificationException(e.getStatus(), e.getMessage(), e);
       }
   }

    @Override
    public EventSubscriptionResponse getEventSubscription(String clientId, String subscriptionId)
            throws FSEventNotificationException {

        try {
            EventNotificationServiceUtil.validateClientId(clientId);

        } catch (FSEventNotificationException e) {
            String errorMsg = String.format("A client was not found" + " for the client id : '%s' in the database. ",
                    clientId.replaceAll("[\r\n]", ""));
            log.error(errorMsg, e);
            throw new FSEventNotificationException(HttpStatus.SC_BAD_REQUEST, errorMsg, e);
        }

        try {
            EventSubscription eventSubscription = eventSubscriptionService.
                    getEventSubscriptionBySubscriptionId(subscriptionId);

            handleValidation(new JSONObject(eventSubscription),
                    EventNotificationConstants.EventSubscriptionOperationEnum.SingleSubscriptionRetrieval);

            EventSubscriptionResponse eventSubscriptionResponse = new EventSubscriptionResponse();
            eventSubscriptionResponse.setResponseStatus(HttpStatus.SC_OK);
            eventSubscriptionResponse.setResponseBody(handleResponseGeneration(eventSubscription,
                    EventNotificationConstants.EventSubscriptionOperationEnum.SingleSubscriptionRetrieval));
            return eventSubscriptionResponse;
        } catch (FSEventNotificationException e) {
            log.error("Error occurred while retrieving event subscription", e);
            if (e.getMessage().equals(EventNotificationConstants.EVENT_SUBSCRIPTION_NOT_FOUND)) {
                throw new FSEventNotificationException(HttpStatus.SC_BAD_REQUEST, e.getMessage(), e);
            } else {
                throw new FSEventNotificationException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
            }
        }
    }

    @Override
    public EventSubscriptionResponse getAllEventSubscriptions(String clientId)
            throws FSEventNotificationException {

        try {
            EventNotificationServiceUtil.validateClientId(clientId);

        } catch (FSEventNotificationException e) {
            String errorMsg = String.format("A client was not found" + " for the client id : '%s' in the database. ",
                    clientId.replaceAll("[\r\n]", ""));
            log.error(errorMsg, e);
            throw new FSEventNotificationException(HttpStatus.SC_BAD_REQUEST, errorMsg, e);
        }

        try {
            List<EventSubscription> eventSubscriptionList = eventSubscriptionService.
                    getEventSubscriptionsByClientId(clientId);
            List<JSONObject> eventSubscriptionResponseList = new ArrayList<>();
            for (EventSubscription eventSubscription : eventSubscriptionList) {
                eventSubscriptionResponseList.add(mapSubscriptionModelToResponseJson(eventSubscription));
            }

            handleValidation(new JSONObject(eventSubscriptionList),
                    EventNotificationConstants.EventSubscriptionOperationEnum.BulkSubscriptionRetrieval);

            EventSubscriptionResponse eventSubscriptionResponse = new EventSubscriptionResponse();
            eventSubscriptionResponse.setResponseStatus(HttpStatus.SC_OK);
            eventSubscriptionResponse.setResponseBody(handleResponseGeneration(eventSubscriptionResponseList,
                    EventNotificationConstants.EventSubscriptionOperationEnum.BulkSubscriptionRetrieval));
            return eventSubscriptionResponse;
        } catch (FSEventNotificationException e) {
            log.error("Error occurred while retrieving event subscriptions", e);
            throw new FSEventNotificationException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);

        }
    }

    @Override
    public EventSubscriptionResponse getEventSubscriptionsByEventType(String clientId, String eventType)
            throws FSEventNotificationException {

        try {
            EventNotificationServiceUtil.validateClientId(clientId);

        } catch (FSEventNotificationException e) {
            String errorMsg = String.format("A client was not found" + " for the client id : '%s' in the database. ",
                    clientId.replaceAll("[\r\n]", ""));
            log.error(errorMsg, e);
            throw new FSEventNotificationException(HttpStatus.SC_BAD_REQUEST, errorMsg, e);
        }

        try {
            List<EventSubscription> eventSubscriptionList = eventSubscriptionService.
                    getEventSubscriptionsByEventType(eventType);
            List<JSONObject> eventSubscriptionResponseList = new ArrayList<>();
            for (EventSubscription eventSubscription : eventSubscriptionList) {
                eventSubscriptionResponseList.add(mapSubscriptionModelToResponseJson(eventSubscription));
            }

            handleValidation(new JSONObject(eventSubscriptionList),
                    EventNotificationConstants.EventSubscriptionOperationEnum.SubscriptionRetrievalForEventTypes);

            EventSubscriptionResponse eventSubscriptionResponse = new EventSubscriptionResponse();
            eventSubscriptionResponse.setResponseStatus(HttpStatus.SC_OK);
            eventSubscriptionResponse.setResponseBody(handleResponseGeneration(eventSubscriptionResponseList,
                    EventNotificationConstants.EventSubscriptionOperationEnum.SubscriptionRetrievalForEventTypes));
            return eventSubscriptionResponse;
        } catch (FSEventNotificationException e) {
            log.error("Error occurred while retrieving event subscriptions", e);
            throw new FSEventNotificationException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @Override
    public EventSubscriptionResponse updateEventSubscription(EventSubscriptionDTO eventSubscriptionUpdateRequestDto)
            throws FSEventNotificationException {

        EventSubscriptionResponse eventSubscriptionResponse = new EventSubscriptionResponse();

        try {
            EventNotificationServiceUtil.validateClientId(eventSubscriptionUpdateRequestDto.getClientId());

        } catch (FSEventNotificationException e) {
            String errorMsg = String.format("A client was not found" + " for the client id : '%s' in the database. ",
                    eventSubscriptionUpdateRequestDto.getClientId().replaceAll("[\r\n]", ""));
            log.error(errorMsg, e);
            throw new FSEventNotificationException(HttpStatus.SC_BAD_REQUEST, errorMsg, e);
        }

        EventSubscription eventSubscription = mapEventSubscriptionDtoToModel(eventSubscriptionUpdateRequestDto);

        try {

            handleValidation(new JSONObject(eventSubscription),
                    EventNotificationConstants.EventSubscriptionOperationEnum.SubscriptionUpdate);
            Boolean isUpdated = eventSubscriptionService.updateEventSubscription(eventSubscription);
            if (!isUpdated) {
                log.error("Event subscription not found.");
                throw new FSEventNotificationException(HttpStatus.SC_BAD_REQUEST, "Event subscription not found.");
            }
            eventSubscriptionResponse.setResponseStatus(HttpStatus.SC_OK);
            EventSubscription eventSubscriptionUpdateResponse = eventSubscriptionService.
                    getEventSubscriptionBySubscriptionId(eventSubscriptionUpdateRequestDto.getSubscriptionId());
            eventSubscriptionResponse.setResponseBody(handleResponseGeneration(eventSubscriptionUpdateResponse,
                    EventNotificationConstants.EventSubscriptionOperationEnum.SubscriptionUpdate));
            return eventSubscriptionResponse;
        } catch (FSEventNotificationException e) {
            log.error("Error occurred while updating event subscription", e);
            throw new FSEventNotificationException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @Override
    public EventSubscriptionResponse deleteEventSubscription(String clientId, String subscriptionId)
            throws FSEventNotificationException {

        try {
            EventNotificationServiceUtil.validateClientId(clientId);

        } catch (FSEventNotificationException e) {
            String errorMsg = String.format("A client was not found" + " for the client id : '%s' in the database. ",
                    clientId.replaceAll("[\r\n]", ""));
            log.error(errorMsg, e);
            throw new FSEventNotificationException(HttpStatus.SC_BAD_REQUEST, errorMsg, e);
        }

        try {

            EventSubscription eventSubscription = eventSubscriptionService.
                    getEventSubscriptionBySubscriptionId(subscriptionId);

            handleValidation(new JSONObject(eventSubscription),
                    EventNotificationConstants.EventSubscriptionOperationEnum.SubscriptionDelete);

            Boolean isDeleted = eventSubscriptionService.deleteEventSubscription(subscriptionId);
            if (!isDeleted) {
                log.error("Event subscription not found");
                throw new FSEventNotificationException(HttpStatus.SC_BAD_REQUEST, "Event subscription not found");
            }

            EventSubscriptionResponse eventSubscriptionResponse = new EventSubscriptionResponse();
            eventSubscriptionResponse.setResponseStatus(HttpStatus.SC_NO_CONTENT);
            return eventSubscriptionResponse;
        } catch (FSEventNotificationException e) {
            log.error("Error occurred while deleting event subscription", e);
            throw new FSEventNotificationException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    /**
     * This method will map the event subscription DTO to event subscription model
     * to be passed to the dao layer.
     *
     * @param eventSubscriptionDTO      Event Subscription DTO
     * @return EventSubscription        Event Subscription Model mapped
     */
    private EventSubscription mapEventSubscriptionDtoToModel(EventSubscriptionDTO eventSubscriptionDTO) {
        EventSubscription eventSubscription = new EventSubscription();

        eventSubscription.setSubscriptionId(eventSubscriptionDTO.getSubscriptionId());
        eventSubscription.setEventTypes(eventSubscriptionDTO.getEventTypes());
        eventSubscription.setCallbackUrl(eventSubscriptionDTO.getCallbackUrl());
        eventSubscription.setSpecVersion(eventSubscriptionDTO.getSpecVersion());
        eventSubscription.setClientId(eventSubscriptionDTO.getClientId());
        eventSubscription.setRequestData(eventSubscriptionDTO.getRequestData());
        return eventSubscription;
    }

    /**
     * This method is used to create the response model from the event subscription model.
     *
     * @param eventSubscription     Event Subscription Model
     * @return EventSubscriptionResponse containing mapped subscription
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
            responsePayload.put(EventNotificationConstants.EVENT_TYPES_PARAM, eventSubscription.getEventTypes());
        }
        return responsePayload;
    }

    /**
     * Method to invoke the external service for validation.
     *
     * @param eventSubscription   Event subscription JSON payload
     * @param operation           Operation to be performed
     * @throws FSEventNotificationException  Exception when handling validation
     */
    private static void handleValidation(JSONObject eventSubscription,
                                         EventNotificationConstants.EventSubscriptionOperationEnum operation)
            throws FSEventNotificationException {

        JSONObject data = new JSONObject();
        data.put("eventSubscription", eventSubscription);
        data.put("apiOperation", operation);

        if (ServiceExtensionUtils.isInvokeExternalService(ServiceExtensionTypeEnum.PRE_EVENT_SUBSCRIPTION)) {
            ExternalServiceRequest request = new ExternalServiceRequest(UUID.randomUUID().toString(),
                    data);
            try {
                ExternalServiceResponse response = ServiceExtensionUtils.invokeExternalServiceCall(request,
                        ServiceExtensionTypeEnum.PRE_EVENT_SUBSCRIPTION);
                if (StatusEnum.ERROR.equals(response.getStatus())) {
                    JSONObject dataObj = new JSONObject(response.getData().toString());
                    throw new FSEventNotificationException(dataObj.getInt("errorCode"),
                            dataObj.getString("errorMessage"));
                }
            } catch (FinancialServicesException e) {
                throw new FSEventNotificationException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }
    }

    /**
     * Method to handle the response generation.
     *
     * @param eventSubscription   Event subscription model
     * @param operation           Operation to be performed
     * @return                  Generated response JSON
     * @throws FSEventNotificationException  Exception when handling response generation
     */
    private JSONObject handleResponseGeneration(EventSubscription eventSubscription,
                                                EventNotificationConstants.EventSubscriptionOperationEnum operation)
            throws FSEventNotificationException {

        JSONObject data = new JSONObject();
        data.put("eventSubscription", eventSubscription);
        data.put("apiOperation", operation);
        if (ServiceExtensionUtils.isInvokeExternalService(ServiceExtensionTypeEnum.POST_EVENT_SUBSCRIPTION)) {
            ExternalServiceRequest request = new ExternalServiceRequest(UUID.randomUUID().toString(), data);
            try {
                ExternalServiceResponse response = ServiceExtensionUtils.invokeExternalServiceCall(request,
                        ServiceExtensionTypeEnum.POST_EVENT_SUBSCRIPTION);
                if (StatusEnum.ERROR.equals(response.getStatus())) {
                    JSONObject dataObj = new JSONObject(response.getData().toString());
                    throw new FSEventNotificationException(dataObj.getInt("errorCode"),
                            dataObj.getString("errorMessage"));
                }

                return new JSONObject(response.getData().get("responseData").toString());
            } catch (FinancialServicesException e) {
                throw new FSEventNotificationException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }
        return mapSubscriptionModelToResponseJson(eventSubscription);
    }

    /**
     * Method to handle the response generation.
     *
     * @param eventSubscriptionList   List of event subscription models
     * @param operation               Operation to be performed
     * @return                    Generated response JSON
     * @throws FSEventNotificationException  Exception when handling response generation
     */
    private JSONArray handleResponseGeneration(List<JSONObject> eventSubscriptionList,
                                               EventNotificationConstants.EventSubscriptionOperationEnum operation)
            throws FSEventNotificationException {

        JSONObject data = new JSONObject();
        data.put("eventSubscription", eventSubscriptionList);
        data.put("apiOperation", operation);
        if (ServiceExtensionUtils.isInvokeExternalService(ServiceExtensionTypeEnum.POST_EVENT_SUBSCRIPTION)) {
            ExternalServiceRequest request = new ExternalServiceRequest(UUID.randomUUID().toString(), data);
            try {
                ExternalServiceResponse response = ServiceExtensionUtils.invokeExternalServiceCall(request,
                        ServiceExtensionTypeEnum.POST_EVENT_SUBSCRIPTION);
                if (StatusEnum.ERROR.equals(response.getStatus())) {
                    JSONObject dataObj = new JSONObject(response.getData().toString());
                    throw new FSEventNotificationException(dataObj.getInt("errorCode"),
                            dataObj.getString("errorMessage"));
                }

                return new JSONArray(response.getData().get("responseData").toString());
            } catch (FinancialServicesException e) {
                throw new FSEventNotificationException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }
        return new JSONArray(eventSubscriptionList);
    }
}
