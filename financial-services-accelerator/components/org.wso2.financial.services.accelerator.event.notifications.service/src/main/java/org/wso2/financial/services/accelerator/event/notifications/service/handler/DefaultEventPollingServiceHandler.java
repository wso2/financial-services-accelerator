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

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.extension.model.StatusEnum;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.event.notifications.service.EventPollingService;
import org.wso2.financial.services.accelerator.event.notifications.service.EventSubscriptionService;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.dto.EventPollingDTO;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.model.AggregatedPollingResponse;
import org.wso2.financial.services.accelerator.event.notifications.service.model.EventPolling;
import org.wso2.financial.services.accelerator.event.notifications.service.model.EventPollingResponse;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationServiceUtil;

import java.util.Map;
import java.util.UUID;

/**
 * This is the service handler for event polling.
 */
public class DefaultEventPollingServiceHandler implements EventPollingServiceHandler {

    private static final Log log = LogFactory.getLog(DefaultEventPollingServiceHandler.class);
    Map<String, Object> configs = FinancialServicesConfigParser.getInstance().getConfiguration();

    public void setEventPollingService(EventPollingService eventPollingService) {
        this.eventPollingService = eventPollingService;
    }

    private EventPollingService eventPollingService = new EventPollingService();


    /**
     * This method is used to Poll Events as per request params.
     * @param eventPollingDTO Event polling DTO
     * @return  EventPollingResponse
     */
    public EventPollingResponse pollEvents(EventPollingDTO eventPollingDTO) {

        EventPollingResponse eventPollingResponse = new EventPollingResponse();

        EventPollingResponse clientIdValidation = validateClientId(eventPollingDTO.getClientId());
        // check whether clientIdValidation is not null, then return the error response
        if (clientIdValidation != null) {
            return clientIdValidation;
        }

        EventPolling eventPolling = mapEventPollingDtoToModel(eventPollingDTO);
        //Poll events
        try {

            if (Boolean.parseBoolean(configs.get(FinancialServicesConstants.REQUIRE_SUBSCRIPTION_TO_POLL).toString()) &&
                    !EventNotificationServiceUtil.isSubscriptionExist(new EventSubscriptionService(),
                            eventPollingDTO.getClientId())) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("'%s' with clientId '%s'.", EventNotificationConstants.SUBSCRIPTION_EXISTS,
                            eventPollingDTO.getClientId().replaceAll("[\r\n]", "")));
                }
                eventPollingResponse.setStatus(HttpStatus.SC_CONFLICT);
                eventPollingResponse.setResponseBody(EventNotificationServiceUtil.getErrorDTO(
                        EventNotificationConstants.INVALID_REQUEST,
                        EventNotificationConstants.SUBSCRIPTION_RESOURCE_NOT_FOUND));
                return eventPollingResponse;
            }

            EventPollingResponse validationResponse = handleValidation(new JSONObject(eventPolling));
            if (validationResponse != null) {
                return validationResponse;
            }
            AggregatedPollingResponse aggregatedPollingResponse = eventPollingService.pollEvents(eventPolling);

            eventPollingResponse.setStatus(aggregatedPollingResponse.getStatus());
            eventPollingResponse.setResponseBody(handleResponseGeneration(aggregatedPollingResponse));
            return eventPollingResponse;
        } catch (FSEventNotificationException e) {
            log.error("Error occurred while polling events" , e);
            eventPollingResponse.setStatus(e.getStatus() == 0 ? HttpStatus.SC_INTERNAL_SERVER_ERROR : e.getStatus());
            eventPollingResponse.setErrorResponse(EventNotificationServiceUtil.getErrorDTO(
                    EventNotificationConstants.INVALID_REQUEST, e.getMessage()));
            return eventPollingResponse;
        }
    }

    /**
     * This method will map the event subscription DTO to event subscription model
     * to be passed to the dao layer.
     *
     * @param eventPollingDTO      Event polling DTO
     * @return EventPolling        Event polling Model mapped
     */
    private EventPolling mapEventPollingDtoToModel(EventPollingDTO eventPollingDTO) {

        EventPolling eventPolling = new EventPolling();
        eventPolling.setClientId(eventPollingDTO.getClientId());
        eventPolling.setMaxEvents(eventPollingDTO.getMaxEvents());
        eventPolling.setReturnImmediately(eventPollingDTO.getReturnImmediately());
        eventPolling.setSetsToReturn(FinancialServicesConfigParser.getInstance().getNumberOfSetsToReturn());
        eventPollingDTO.getAck().forEach(eventPolling::setAck);
        eventPollingDTO.getErrors().forEach(eventPolling::setErrors);

        return eventPolling;
    }

    @Generated(message = "Private method tested when testing the invoked method")
    private JSONObject getPollingResponseJSON(AggregatedPollingResponse aggregatedPollingResponse) {

        JSONObject responseJSON = new JSONObject();
        responseJSON.put(EventNotificationConstants.SETS, aggregatedPollingResponse.getSets());
        responseJSON.put(EventNotificationConstants.MORE_AVAILABLE,
                aggregatedPollingResponse.isMoreAvailable());
        return responseJSON;
    }

    /**
     * Method to invoke the external service for validation.
     *
     * @param eventPolling     Event polling JSON payload
     * @throws FSEventNotificationException  Exception when handling validation
     */
    private static EventPollingResponse handleValidation(JSONObject eventPolling) throws FSEventNotificationException {

        JSONObject data = new JSONObject();
        data.put(EventNotificationConstants.EVENT_POLLING_DATA, eventPolling);

        if (ServiceExtensionUtils.isInvokeExternalService(ServiceExtensionTypeEnum.VALIDATE_EVENT_POLLING)) {
            ExternalServiceRequest request = new ExternalServiceRequest(UUID.randomUUID().toString(),
                    data);
            try {
                ExternalServiceResponse response = ServiceExtensionUtils.invokeExternalServiceCall(request,
                        ServiceExtensionTypeEnum.VALIDATE_EVENT_POLLING);
                if (StatusEnum.ERROR.equals(response.getStatus())) {
                    EventPollingResponse eventPollingResponse = new EventPollingResponse();
                    eventPollingResponse.setStatus(response.getErrorCode());
                    eventPollingResponse.setErrorResponse(new JSONObject(response.getData().toString()));
                    return eventPollingResponse;
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
     * @param aggregatedPollingResponse   Aggregated polling response
     * @return     Generated response JSON
     * @throws FSEventNotificationException Exception when handling response generation
     */
    private JSONObject handleResponseGeneration(AggregatedPollingResponse aggregatedPollingResponse)
            throws FSEventNotificationException {

        JSONObject data = new JSONObject();
        data.put(EventNotificationConstants.EVENT_POLLING_DATA, new JSONObject(aggregatedPollingResponse));

        if (ServiceExtensionUtils.isInvokeExternalService(ServiceExtensionTypeEnum.ENRICH_EVENT_POLLING_RESPONSE)) {
            ExternalServiceRequest request = new ExternalServiceRequest(UUID.randomUUID().toString(), data);
            try {
                ExternalServiceResponse response = ServiceExtensionUtils.invokeExternalServiceCall(request,
                        ServiceExtensionTypeEnum.ENRICH_EVENT_POLLING_RESPONSE);
                if (StatusEnum.ERROR.equals(response.getStatus())) {
                    JSONObject dataObj = new JSONObject(response.getData().toString());
                    throw new FSEventNotificationException(dataObj.getInt(FinancialServicesConstants.ERROR_CODE),
                            dataObj.getString(FinancialServicesConstants.ERROR_MESSAGE));
                }

                return new JSONObject(response.getData().get(EventNotificationConstants.EVENT_POLLING_RESPONSE)
                        .toString());
            } catch (FinancialServicesException e) {
                throw new FSEventNotificationException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }
        return getPollingResponseJSON(aggregatedPollingResponse);
    }

    /**
     * This method is used to validate the client ID.
     *
     * @param clientId                      Client ID
     * @return EventPollingResponse    Return EventPollingResponse if the client ID is
     *                                      invalid, if the client ID is valid, null will be returned.
     */
    private EventPollingResponse validateClientId(String clientId) {
        try {
            EventNotificationServiceUtil.validateClientId(clientId);
        } catch (FSEventNotificationException e) {
            log.error("Invalid client ID", e);
            EventPollingResponse eventPollingResponse = new EventPollingResponse();
            eventPollingResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
            eventPollingResponse.setResponseBody(EventNotificationServiceUtil.getErrorDTO(
                    EventNotificationConstants.INVALID_REQUEST, e.getMessage()));
            return eventPollingResponse;
        }
        return null;
    }
}
