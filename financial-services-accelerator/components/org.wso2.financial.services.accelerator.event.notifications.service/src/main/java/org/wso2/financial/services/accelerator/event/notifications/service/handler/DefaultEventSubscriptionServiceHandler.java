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
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationConstants.EventSubscriptionOperationEnum;
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

           JSONObject externalServiceResponse = handleValidation(new JSONObject(eventSubscriptionRequestDto),
                   EventSubscriptionOperationEnum.SubscriptionCreation);

           EventSubscription eventSubscription = mapEventSubscriptionDtoToModel(
                   ServiceExtensionTypeEnum.PRE_EVENT_SUBSCRIPTION, eventSubscriptionRequestDto,
                   externalServiceResponse);

           EventSubscription eventSubscriptionCreateResponse = eventSubscriptionService.
                   createEventSubscription(eventSubscription);

           EventSubscriptionResponse eventSubscriptionResponse = new EventSubscriptionResponse();
           eventSubscriptionResponse.setResponseStatus(HttpStatus.SC_CREATED);
           eventSubscriptionResponse.setResponseBody(handleResponseGeneration(eventSubscriptionCreateResponse,
                   EventSubscriptionOperationEnum.SubscriptionCreation));
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
                    EventSubscriptionOperationEnum.SingleSubscriptionRetrieval);

            EventSubscriptionResponse eventSubscriptionResponse = new EventSubscriptionResponse();
            eventSubscriptionResponse.setResponseStatus(HttpStatus.SC_OK);
            eventSubscriptionResponse.setResponseBody(handleResponseGeneration(eventSubscription,
                    EventSubscriptionOperationEnum.SingleSubscriptionRetrieval));
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
                    EventSubscriptionOperationEnum.BulkSubscriptionRetrieval);

            EventSubscriptionResponse eventSubscriptionResponse = new EventSubscriptionResponse();
            eventSubscriptionResponse.setResponseStatus(HttpStatus.SC_OK);
            eventSubscriptionResponse.setResponseBody(handleResponseGeneration(eventSubscriptionResponseList,
                    EventSubscriptionOperationEnum.BulkSubscriptionRetrieval));
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
                    EventSubscriptionOperationEnum.SubscriptionRetrievalForEventTypes);

            EventSubscriptionResponse eventSubscriptionResponse = new EventSubscriptionResponse();
            eventSubscriptionResponse.setResponseStatus(HttpStatus.SC_OK);
            eventSubscriptionResponse.setResponseBody(handleResponseGeneration(eventSubscriptionResponseList,
                    EventSubscriptionOperationEnum.SubscriptionRetrievalForEventTypes));
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

        try {

            JSONObject externalServiceResponse = handleValidation(new JSONObject(eventSubscriptionUpdateRequestDto),
                    EventSubscriptionOperationEnum.SubscriptionCreation);

            EventSubscription eventSubscription = mapEventSubscriptionDtoToModel(
                    ServiceExtensionTypeEnum.PRE_EVENT_SUBSCRIPTION, eventSubscriptionUpdateRequestDto,
                    externalServiceResponse);
            Boolean isUpdated = eventSubscriptionService.updateEventSubscription(eventSubscription);
            if (!isUpdated) {
                log.error("Event subscription not found.");
                throw new FSEventNotificationException(HttpStatus.SC_BAD_REQUEST, "Event subscription not found.");
            }
            eventSubscriptionResponse.setResponseStatus(HttpStatus.SC_OK);
            EventSubscription eventSubscriptionUpdateResponse = eventSubscriptionService.
                    getEventSubscriptionBySubscriptionId(eventSubscriptionUpdateRequestDto.getSubscriptionId());
            eventSubscriptionResponse.setResponseBody(handleResponseGeneration(eventSubscriptionUpdateResponse,
                    EventSubscriptionOperationEnum.SubscriptionUpdate));
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

            handleValidation(new JSONObject(eventSubscription), EventSubscriptionOperationEnum.SubscriptionDelete);

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

    private EventSubscription mapEventSubscriptionDtoToModel(ServiceExtensionTypeEnum extensionType,
                                                             EventSubscriptionDTO eventSubscriptionDTO,
                                                             JSONObject externalServiceResponse) {
        if (ServiceExtensionUtils.isInvokeExternalService(extensionType)) {
            return mapEventSubscriptionDtoToModel(eventSubscriptionDTO, externalServiceResponse);
        }
        return mapEventSubscriptionDtoToModel(eventSubscriptionDTO);
    }

    /**
     * This method will map the event subscription DTO to event subscription model
     * to be passed to the dao layer.
     *
     * @param externalServiceResponse      Event Subscription DTO
     * @return EventSubscription        Event Subscription Model mapped
     */
    private EventSubscription mapEventSubscriptionDtoToModel(EventSubscriptionDTO eventSubscriptionDTO,
                                                             JSONObject externalServiceResponse) {
        EventSubscription eventSubscription = new EventSubscription();

        eventSubscription.setSubscriptionId(eventSubscriptionDTO.getSubscriptionId());
        if (externalServiceResponse.has(EventNotificationConstants.EVENT_TYPES_PARAM)) {
            eventSubscription.setEventTypes(constructEventList(externalServiceResponse
                    .getJSONArray(EventNotificationConstants.EVENT_TYPES_PARAM)));
        }
        if (externalServiceResponse.has(EventNotificationConstants.CALLBACK_URL_PARAM) &&
                !externalServiceResponse.isNull(EventNotificationConstants.CALLBACK_URL_PARAM)) {
            eventSubscription.setCallbackUrl(
                    externalServiceResponse.getString(EventNotificationConstants.CALLBACK_URL_PARAM));
        }
        if (externalServiceResponse.has(EventNotificationConstants.VERSION_PARAM) &&
                externalServiceResponse.get(EventNotificationConstants.VERSION_PARAM) != null) {
            eventSubscription.setSpecVersion(externalServiceResponse.
                    getString(EventNotificationConstants.VERSION_PARAM));
        }
        eventSubscription.setClientId(eventSubscriptionDTO.getClientId());
        eventSubscription.setRequestData(eventSubscriptionDTO.getRequestData().toString());
        return eventSubscription;
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

        JSONObject payload = eventSubscriptionDTO.getRequestData();
        List<String> eventTypes = new ArrayList<>();
        Object eventTypesObj = payload.get(EventNotificationConstants.EVENT_TYPES_PARAM);
        if (eventTypesObj instanceof List) {
            List<?> eventTypesList = (List<?>) eventTypesObj;
            for (Object item : eventTypesList) {
                if (item instanceof String) {
                    eventTypes.add((String) item);
                }
            }
        }
        eventSubscription.setEventTypes(eventTypes);
        eventSubscription.setCallbackUrl(payload.has(EventNotificationConstants.CALLBACK_URL_PARAM) ?
                payload.get(EventNotificationConstants.CALLBACK_URL_PARAM).toString() : null);
        eventSubscription.setSpecVersion(payload.get(EventNotificationConstants.SPEC_VERSION_PARAM) != null ?
                payload.get(EventNotificationConstants.SPEC_VERSION_PARAM).toString() : null);
        eventSubscription.setClientId(eventSubscriptionDTO.getClientId());
        eventSubscription.setRequestData(payload.toString());
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
            responsePayload.put(EventNotificationConstants.SPEC_VERSION_PARAM, eventSubscription.getSpecVersion());
        }
        if (eventSubscription.getEventTypes() != null) {
            responsePayload.put(EventNotificationConstants.EVENT_TYPES_PARAM, eventSubscription.getEventTypes());
        }
        return responsePayload;
    }

    /**
     * This method is used to create a string from a JSONArray.
     * @param eventArray   JSONArray of events
     * @return
     */
    private List<String> constructEventList(JSONArray eventArray) {
        return IntStream.range(0, eventArray.length())
                .mapToObj(eventArray::getString)
                .collect(Collectors.toList());
    }

    /**
     * Method to invoke the external service for validation.
     *
     * @param eventSubscription   Event subscription JSON payload
     * @param operation           Operation to be performed
     * @throws FSEventNotificationException  Exception when handling validation
     */
    private static JSONObject handleValidation(JSONObject eventSubscription, EventSubscriptionOperationEnum operation)
            throws FSEventNotificationException {

        JSONObject data = new JSONObject();
        data.put(EventNotificationConstants.EVENT_SUBSCRIPTION_PAYLOAD, eventSubscription);
        data.put(EventNotificationConstants.API_OPERATION, operation);

        if (ServiceExtensionUtils.isInvokeExternalService(ServiceExtensionTypeEnum.PRE_EVENT_SUBSCRIPTION)) {
            ExternalServiceRequest request = new ExternalServiceRequest(UUID.randomUUID().toString(),
                    data);
            try {
                ExternalServiceResponse response = ServiceExtensionUtils.invokeExternalServiceCall(request,
                        ServiceExtensionTypeEnum.PRE_EVENT_SUBSCRIPTION);
                if (StatusEnum.ERROR.equals(response.getStatus())) {
                    JSONObject dataObj = new JSONObject(response.getData().toString());
                    throw new FSEventNotificationException(dataObj.getInt(FinancialServicesConstants.ERROR_CODE),
                            dataObj.getString(FinancialServicesConstants.ERROR_MESSAGE));
                }
                if (EventSubscriptionOperationEnum.SubscriptionCreation.equals(operation) ||
                        EventSubscriptionOperationEnum.SubscriptionUpdate.equals(operation)) {
                    return new JSONObject(response.getData().toString());
                }
            } catch (FinancialServicesException e) {
                throw new FSEventNotificationException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }
        return null;
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
                                                EventSubscriptionOperationEnum operation)
            throws FSEventNotificationException {

        JSONObject data = new JSONObject();
        data.put(EventNotificationConstants.EVENT_SUBSCRIPTION, new JSONObject(eventSubscription));
        data.put(EventNotificationConstants.API_OPERATION, operation);

        if (ServiceExtensionUtils.isInvokeExternalService(ServiceExtensionTypeEnum.POST_EVENT_SUBSCRIPTION)) {
            ExternalServiceRequest request = new ExternalServiceRequest(UUID.randomUUID().toString(), data);
            try {
                ExternalServiceResponse response = ServiceExtensionUtils.invokeExternalServiceCall(request,
                        ServiceExtensionTypeEnum.POST_EVENT_SUBSCRIPTION);
                if (StatusEnum.ERROR.equals(response.getStatus())) {
                    JSONObject dataObj = new JSONObject(response.getData().toString());
                    throw new FSEventNotificationException(dataObj.getInt(FinancialServicesConstants.ERROR_CODE),
                            dataObj.getString(FinancialServicesConstants.ERROR_MESSAGE));
                }

                return new JSONObject(response.getData().get(FinancialServicesConstants.RESPONSE_DATA).toString());
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
    private JSONObject handleResponseGeneration(List<JSONObject> eventSubscriptionList,
                                                EventSubscriptionOperationEnum operation)
            throws FSEventNotificationException {

        JSONObject data = new JSONObject();
        data.put(EventNotificationConstants.EVENT_SUBSCRIPTION, eventSubscriptionList);
        data.put(EventNotificationConstants.API_OPERATION, operation);

        if (ServiceExtensionUtils.isInvokeExternalService(ServiceExtensionTypeEnum.POST_EVENT_SUBSCRIPTION)) {
            ExternalServiceRequest request = new ExternalServiceRequest(UUID.randomUUID().toString(), data);
            try {
                ExternalServiceResponse response = ServiceExtensionUtils.invokeExternalServiceCall(request,
                        ServiceExtensionTypeEnum.POST_EVENT_SUBSCRIPTION);
                if (StatusEnum.ERROR.equals(response.getStatus())) {
                    JSONObject dataObj = new JSONObject(response.getData().toString());
                    throw new FSEventNotificationException(dataObj.getInt(FinancialServicesConstants.ERROR_CODE),
                            dataObj.getString(FinancialServicesConstants.ERROR_MESSAGE));
                }

                return new JSONObject(response.getData().get(FinancialServicesConstants.RESPONSE_DATA).toString());
            } catch (FinancialServicesException e) {
                throw new FSEventNotificationException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }
        return new JSONObject(eventSubscriptionList);
    }
}
