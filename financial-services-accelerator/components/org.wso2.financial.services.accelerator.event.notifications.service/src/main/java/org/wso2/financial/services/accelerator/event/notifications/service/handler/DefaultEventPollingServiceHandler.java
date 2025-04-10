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
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.dto.EventPollingDTO;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.model.AggregatedPollingResponse;
import org.wso2.financial.services.accelerator.event.notifications.service.model.EventPolling;
import org.wso2.financial.services.accelerator.event.notifications.service.model.EventPollingResponse;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationServiceUtil;

import java.util.UUID;

/**
 * This is the service handler for event polling.
 */
public class DefaultEventPollingServiceHandler implements EventPollingServiceHandler {

    private static final Log log = LogFactory.getLog(DefaultEventPollingServiceHandler.class);

    public void setEventPollingService(EventPollingService eventPollingService) {
        this.eventPollingService = eventPollingService;
    }

    private EventPollingService eventPollingService = new EventPollingService();


    /**
     * This method is used to Poll Events as per request params.
     * @param eventPollingDTO Event polling DTO
     * @return  EventPollingResponse
     */
    public EventPollingResponse pollEvents(EventPollingDTO eventPollingDTO) throws FSEventNotificationException {

        //Validate clientID of the polling request
        try {
            EventNotificationServiceUtil.validateClientId(eventPollingDTO.getClientId());
        } catch (FSEventNotificationException e) {
            String errorMessage = String.format("A client was not found for the client id : '%s' in the database. ",
                    eventPollingDTO.getClientId().replaceAll("[\r\n]", ""));
            log.error(errorMessage, e);
            throw new FSEventNotificationException(HttpStatus.SC_BAD_REQUEST, errorMessage, e);
        }

        EventPolling eventPolling = mapEventPollingDtoToModel(eventPollingDTO);
        //Poll events
        try {
            handleValidation(new JSONObject(eventPolling));
            AggregatedPollingResponse aggregatedPollingResponse = eventPollingService.pollEvents(eventPolling);

            EventPollingResponse eventPollingResponse = new EventPollingResponse();
            eventPollingResponse.setStatus(aggregatedPollingResponse.getStatus());
            eventPollingResponse.setResponseBody(handleResponseGeneration(aggregatedPollingResponse));
            return eventPollingResponse;
        } catch (FSEventNotificationException e) {
            log.error("Error occurred while polling events" , e);
            throw new FSEventNotificationException(e.getStatus(), e.getMessage(), e);
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
    private static void handleValidation(JSONObject eventPolling) throws FSEventNotificationException {

        JSONObject data = new JSONObject();
        data.put(EventNotificationConstants.EVENT_POLLING_PAYLOAD, eventPolling);

        if (ServiceExtensionUtils.isInvokeExternalService(ServiceExtensionTypeEnum.PRE_EVENT_POLLING)) {
            ExternalServiceRequest request = new ExternalServiceRequest(UUID.randomUUID().toString(),
                    data);
            try {
                ExternalServiceResponse response = ServiceExtensionUtils.invokeExternalServiceCall(request,
                        ServiceExtensionTypeEnum.PRE_EVENT_POLLING);
                if (StatusEnum.ERROR.equals(response.getStatus())) {
                    JSONObject dataObj = new JSONObject(response.getData().toString());
                    throw new FSEventNotificationException(dataObj.getInt(FinancialServicesConstants.ERROR_CODE),
                            dataObj.getString(FinancialServicesConstants.ERROR_MESSAGE));
                }
            } catch (FinancialServicesException e) {
                throw new FSEventNotificationException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }
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
        data.put(EventNotificationConstants.EVENT_POLLING, new JSONObject(aggregatedPollingResponse));

        if (ServiceExtensionUtils.isInvokeExternalService(ServiceExtensionTypeEnum.POST_EVENT_POLLING)) {
            ExternalServiceRequest request = new ExternalServiceRequest(UUID.randomUUID().toString(), data);
            try {
                ExternalServiceResponse response = ServiceExtensionUtils.invokeExternalServiceCall(request,
                        ServiceExtensionTypeEnum.POST_EVENT_POLLING);
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
        return getPollingResponseJSON(aggregatedPollingResponse);
    }
}
