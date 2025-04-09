/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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
import org.json.JSONObject;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.event.notifications.service.EventSubscriptionService;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationTestConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.model.EventSubscriptionResponse;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationServiceUtil;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Test class for DefaultEventSubscriptionServiceHandler.
 */
public class DefaultEventSubscriptionServiceHandlerTests {

    private MockedStatic<FinancialServicesConfigParser> configParserMockedStatic;
    private MockedStatic<EventNotificationServiceUtil> eventNotificationUtilMockedStatic;
    private MockedStatic<ServiceExtensionUtils> serviceExtensionUtilsMockedStatic;
    DefaultEventSubscriptionServiceHandler defaultEventSubscriptionServiceHandler;

    @BeforeClass
    public void initTest() {
        configParserMockedStatic = Mockito.mockStatic(FinancialServicesConfigParser.class);
        eventNotificationUtilMockedStatic = Mockito.mockStatic(EventNotificationServiceUtil.class);
        serviceExtensionUtilsMockedStatic = Mockito.mockStatic(ServiceExtensionUtils.class);

        Map<String, Object> configs = new HashMap<String, Object>();
        configs.put(FinancialServicesConstants.REALTIME_EVENT_NOTIFICATION_ENABLED, false);
        configs.put(FinancialServicesConstants.ALLOW_MULTIPLE_SUBSCRIPTION, true);
        FinancialServicesConfigParser configParserMock = Mockito.mock(FinancialServicesConfigParser.class);
        Mockito.doReturn(configs).when(configParserMock).getConfiguration();
        configParserMockedStatic.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.isInvokeExternalService(any()))
                .thenReturn(false);

        defaultEventSubscriptionServiceHandler = new DefaultEventSubscriptionServiceHandler();
    }

    @AfterClass
    public void tearDown() {
        configParserMockedStatic.close();
        eventNotificationUtilMockedStatic.close();
        serviceExtensionUtilsMockedStatic.close();
    }

    @Test
    public void testCreateEventSubscription() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.createEventSubscription(any()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscription());

        eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                .thenAnswer((Answer<Void>) invocation -> null);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.isInvokeExternalService(any()))
                .thenReturn(false);

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        EventSubscriptionResponse eventSubscriptionCreationResponse = defaultEventSubscriptionServiceHandler
                .createEventSubscription(EventNotificationTestUtils.getSampleEventSubscriptionDTO());

        Assert.assertEquals(eventSubscriptionCreationResponse.getResponseStatus(), HttpStatus.SC_CREATED);
    }

    @Test
    public void testCreateEventSubscriptionWithExternalService() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.createEventSubscription(any()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscription());

        eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                .thenAnswer((Answer<Void>) invocation -> null);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.isInvokeExternalService(any()))
                .thenReturn(true);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.invokeExternalServiceCall(any(), any()))
                .thenReturn(EventNotificationTestUtils.getExternalServiceResponseWithData());

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        EventSubscriptionResponse eventSubscriptionCreationResponse = defaultEventSubscriptionServiceHandler
                .createEventSubscription(EventNotificationTestUtils.getSampleEventSubscriptionDTO());

        Assert.assertEquals(eventSubscriptionCreationResponse.getResponseStatus(), HttpStatus.SC_CREATED);
    }

    @Test
    public void testCreateEventSubscriptionWithInvalidClientID() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.createEventSubscription(any()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscription());

        eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                .thenThrow(FSEventNotificationException.class);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.isInvokeExternalService(any()))
                .thenReturn(false);

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        EventSubscriptionResponse eventSubscriptionCreationResponse = defaultEventSubscriptionServiceHandler
                .createEventSubscription(EventNotificationTestUtils.getSampleEventSubscriptionDTO());

        Assert.assertEquals(eventSubscriptionCreationResponse.getResponseStatus(), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testCreateEventSubscriptionServiceError() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.createEventSubscription(any()))
                .thenThrow(new FSEventNotificationException(EventNotificationConstants.
                        ERROR_STORING_EVENT_SUBSCRIPTION));

        eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                .thenAnswer((Answer<Void>) invocation -> null);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.isInvokeExternalService(any()))
                .thenReturn(false);

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        EventSubscriptionResponse eventSubscriptionCreationResponse = defaultEventSubscriptionServiceHandler
                .createEventSubscription(EventNotificationTestUtils.getSampleEventSubscriptionDTO());

        Assert.assertEquals(eventSubscriptionCreationResponse.getResponseStatus(),
                HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testGetEventSubscription() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.getEventSubscriptionBySubscriptionId(any()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscription());

        eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                .thenAnswer((Answer<Void>) invocation -> null);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.isInvokeExternalService(any()))
                .thenReturn(false);

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        EventSubscriptionResponse eventSubscriptionRetrieveResponse = defaultEventSubscriptionServiceHandler
                .getEventSubscription(EventNotificationTestConstants.SAMPLE_CLIENT_ID,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);

        Assert.assertEquals(eventSubscriptionRetrieveResponse.getResponseStatus(), HttpStatus.SC_OK);
    }

    @Test
    public void testGetEventSubscriptionWithExternalService() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.getEventSubscriptionBySubscriptionId(any()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscription());

        eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                .thenAnswer((Answer<Void>) invocation -> null);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.isInvokeExternalService(any()))
                .thenReturn(true);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.invokeExternalServiceCall(any(), any()))
                .thenReturn(EventNotificationTestUtils.getExternalServiceResponse());

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        EventSubscriptionResponse eventSubscriptionRetrieveResponse = defaultEventSubscriptionServiceHandler
                .getEventSubscription(EventNotificationTestConstants.SAMPLE_CLIENT_ID,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);

        Assert.assertEquals(eventSubscriptionRetrieveResponse.getResponseStatus(), HttpStatus.SC_OK);
    }

    @Test
    public void testGetEventSubscriptionWithInvalidClientID() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.getEventSubscriptionBySubscriptionId(any()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscription());

        eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                .thenThrow(FSEventNotificationException.class);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.isInvokeExternalService(any()))
                .thenReturn(false);

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        EventSubscriptionResponse eventSubscriptionRetrieveResponse = defaultEventSubscriptionServiceHandler
                .getEventSubscription(EventNotificationTestConstants.SAMPLE_CLIENT_ID,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);

        Assert.assertEquals(eventSubscriptionRetrieveResponse.getResponseStatus(), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testGetEventSubscriptionServiceError() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.getEventSubscriptionBySubscriptionId(any()))
                .thenThrow(new FSEventNotificationException(EventNotificationConstants.
                        ERROR_RETRIEVING_EVENT_SUBSCRIPTIONS));

        eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                .thenAnswer((Answer<Void>) invocation -> null);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.isInvokeExternalService(any()))
                .thenReturn(false);

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        EventSubscriptionResponse eventSubscriptionRetrieveResponse = defaultEventSubscriptionServiceHandler
                .getEventSubscription(EventNotificationTestConstants.SAMPLE_CLIENT_ID,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);

        Assert.assertEquals(eventSubscriptionRetrieveResponse.getResponseStatus(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testGetAllEventSubscriptions() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.getEventSubscriptionsByClientId(any()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscriptions());

        eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                .thenAnswer((Answer<Void>) invocation -> null);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.isInvokeExternalService(any()))
                .thenReturn(false);

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        EventSubscriptionResponse eventSubscriptionRetrieveResponse = defaultEventSubscriptionServiceHandler
                .getAllEventSubscriptions(EventNotificationTestConstants.SAMPLE_CLIENT_ID);

        Assert.assertEquals(eventSubscriptionRetrieveResponse.getResponseStatus(), HttpStatus.SC_OK);
    }

    @Test
    public void testGetAllEventSubscriptionsWithExternalService() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.getEventSubscriptionsByClientId(any()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscriptions());

        eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                .thenAnswer((Answer<Void>) invocation -> null);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.isInvokeExternalService(any()))
                .thenReturn(true);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.invokeExternalServiceCall(any(), any()))
                .thenReturn(EventNotificationTestUtils.getExternalServiceResponse());

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        EventSubscriptionResponse eventSubscriptionRetrieveResponse = defaultEventSubscriptionServiceHandler
                .getAllEventSubscriptions(EventNotificationTestConstants.SAMPLE_CLIENT_ID);

        Assert.assertEquals(eventSubscriptionRetrieveResponse.getResponseStatus(), HttpStatus.SC_OK);
    }

    @Test
    public void testGetAllEventSubscriptionsWithInvalidClientID() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.getEventSubscriptionsByClientId(any()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscriptions());

        eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                .thenThrow(FSEventNotificationException.class);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.isInvokeExternalService(any()))
                .thenReturn(false);

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        EventSubscriptionResponse eventSubscriptionRetrieveResponse = defaultEventSubscriptionServiceHandler
                .getAllEventSubscriptions(EventNotificationTestConstants.SAMPLE_CLIENT_ID);

        Assert.assertEquals(eventSubscriptionRetrieveResponse.getResponseStatus(), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testGetAllEventSubscriptionsServiceError() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.getEventSubscriptionsByClientId(any()))
                .thenThrow(new FSEventNotificationException(EventNotificationConstants.
                        ERROR_RETRIEVING_EVENT_SUBSCRIPTIONS));

        eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                .thenAnswer((Answer<Void>) invocation -> null);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.isInvokeExternalService(any()))
                .thenReturn(false);

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        EventSubscriptionResponse eventSubscriptionRetrieveResponse = defaultEventSubscriptionServiceHandler
                .getAllEventSubscriptions(EventNotificationTestConstants.SAMPLE_CLIENT_ID);

        Assert.assertEquals(eventSubscriptionRetrieveResponse.getResponseStatus(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testGetEventSubscriptionsByEventType() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.getEventSubscriptionsByEventType(anyString()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscriptions());

        eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                .thenAnswer((Answer<Void>) invocation -> null);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.isInvokeExternalService(any()))
                .thenReturn(false);

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        EventSubscriptionResponse eventSubscriptionRetrieveResponse = defaultEventSubscriptionServiceHandler
                .getEventSubscriptionsByEventType(EventNotificationTestConstants.SAMPLE_CLIENT_ID,
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1);

        Assert.assertEquals(eventSubscriptionRetrieveResponse.getResponseStatus(), HttpStatus.SC_OK);
    }

    @Test
    public void testGetEventSubscriptionsByEventTypeWithExternalService() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.getEventSubscriptionsByEventType(anyString()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscriptions());

        eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                .thenAnswer((Answer<Void>) invocation -> null);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.isInvokeExternalService(any()))
                .thenReturn(true);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.invokeExternalServiceCall(any(), any()))
                .thenReturn(EventNotificationTestUtils.getExternalServiceResponse());

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        EventSubscriptionResponse eventSubscriptionRetrieveResponse = defaultEventSubscriptionServiceHandler
                .getEventSubscriptionsByEventType(EventNotificationTestConstants.SAMPLE_CLIENT_ID,
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1);

        Assert.assertEquals(eventSubscriptionRetrieveResponse.getResponseStatus(), HttpStatus.SC_OK);
    }

    @Test
    public void testGetEventSubscriptionsByEventTypeWithInvalidClientID() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.getEventSubscriptionsByEventType(anyString()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscriptions());

        eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                .thenThrow(FSEventNotificationException.class);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.isInvokeExternalService(any()))
                .thenReturn(false);

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        EventSubscriptionResponse eventSubscriptionRetrieveResponse = defaultEventSubscriptionServiceHandler
                .getEventSubscriptionsByEventType(EventNotificationTestConstants.SAMPLE_CLIENT_ID,
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1);

        Assert.assertEquals(eventSubscriptionRetrieveResponse.getResponseStatus(), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testGetEventSubscriptionsByEventTypeServiceError() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.getEventSubscriptionsByEventType(anyString()))
                .thenThrow(new FSEventNotificationException(EventNotificationConstants.
                        ERROR_RETRIEVING_EVENT_SUBSCRIPTIONS));

        eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                .thenAnswer((Answer<Void>) invocation -> null);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.isInvokeExternalService(any()))
                .thenReturn(false);

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        EventSubscriptionResponse eventSubscriptionRetrieveResponse = defaultEventSubscriptionServiceHandler
                .getEventSubscriptionsByEventType(EventNotificationTestConstants.SAMPLE_CLIENT_ID,
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1);

        Assert.assertEquals(eventSubscriptionRetrieveResponse.getResponseStatus(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testUpdateEventSubscription() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.updateEventSubscription(any())).thenReturn(true);
        Mockito.when(eventSubscriptionService.getEventSubscriptionBySubscriptionId(any()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscription());

        eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                .thenAnswer((Answer<Void>) invocation -> null);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.isInvokeExternalService(any()))
                .thenReturn(false);

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        EventSubscriptionResponse eventSubscriptionUpdateResponse = defaultEventSubscriptionServiceHandler
                .updateEventSubscription(EventNotificationTestUtils.getSampleEventSubscriptionUpdateDTO());

        Assert.assertEquals(eventSubscriptionUpdateResponse.getResponseStatus(), HttpStatus.SC_OK);
    }

    @Test
    public void testUpdateEventSubscriptionWithExternalService() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.updateEventSubscription(any())).thenReturn(true);
        Mockito.when(eventSubscriptionService.getEventSubscriptionBySubscriptionId(any()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscription());

        eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                .thenAnswer((Answer<Void>) invocation -> null);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.isInvokeExternalService(any()))
                .thenReturn(true);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.invokeExternalServiceCall(any(), any()))
                .thenReturn(EventNotificationTestUtils.getExternalServiceResponseWithData());

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        EventSubscriptionResponse eventSubscriptionUpdateResponse = defaultEventSubscriptionServiceHandler
                .updateEventSubscription(EventNotificationTestUtils.getSampleEventSubscriptionUpdateDTO());

        Assert.assertEquals(eventSubscriptionUpdateResponse.getResponseStatus(), HttpStatus.SC_OK);
    }

    @Test
    public void testUpdateEventSubscriptionWithInvalidClientID() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.updateEventSubscription(any())).thenReturn(true);
        Mockito.when(eventSubscriptionService.getEventSubscriptionBySubscriptionId(any()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscription());

        eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                .thenThrow(FSEventNotificationException.class);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.isInvokeExternalService(any()))
                .thenReturn(false);

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        EventSubscriptionResponse eventSubscriptionUpdateResponse = defaultEventSubscriptionServiceHandler
                .updateEventSubscription(EventNotificationTestUtils.getSampleEventSubscriptionUpdateDTO());

        Assert.assertEquals(eventSubscriptionUpdateResponse.getResponseStatus(), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testUpdateEventSubscriptionServiceError() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.updateEventSubscription(any())).thenReturn(true);
        Mockito.when(eventSubscriptionService.getEventSubscriptionBySubscriptionId(any()))
                .thenThrow(new FSEventNotificationException(EventNotificationConstants.
                        ERROR_UPDATING_EVENT_SUBSCRIPTION));

        eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                .thenAnswer((Answer<Void>) invocation -> null);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.isInvokeExternalService(any()))
                .thenReturn(false);

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        EventSubscriptionResponse eventSubscriptionUpdateResponse = defaultEventSubscriptionServiceHandler
                .updateEventSubscription(EventNotificationTestUtils.getSampleEventSubscriptionUpdateDTO());

        Assert.assertEquals(eventSubscriptionUpdateResponse.getResponseStatus(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testDeleteEventSubscription() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.deleteEventSubscription(any())).thenReturn(true);
        Mockito.when(eventSubscriptionService.getEventSubscriptionBySubscriptionId(any()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscription());

        eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                .thenAnswer((Answer<Void>) invocation -> null);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.isInvokeExternalService(any()))
                .thenReturn(false);

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        EventSubscriptionResponse eventSubscriptionDeletionResponse = defaultEventSubscriptionServiceHandler
                .deleteEventSubscription(EventNotificationTestConstants.SAMPLE_CLIENT_ID,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);

        Assert.assertEquals(eventSubscriptionDeletionResponse.getResponseStatus(), HttpStatus.SC_NO_CONTENT);
    }

    @Test
    public void testDeleteEventSubscriptionWithExternalService() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.deleteEventSubscription(any())).thenReturn(true);
        Mockito.when(eventSubscriptionService.getEventSubscriptionBySubscriptionId(any()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscription());

        eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                .thenAnswer((Answer<Void>) invocation -> null);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.isInvokeExternalService(any()))
                .thenReturn(true);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.invokeExternalServiceCall(any(), any()))
                .thenReturn(EventNotificationTestUtils.getExternalServiceResponse());

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        EventSubscriptionResponse eventSubscriptionDeletionResponse = defaultEventSubscriptionServiceHandler
                .deleteEventSubscription(EventNotificationTestConstants.SAMPLE_CLIENT_ID,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);

        Assert.assertEquals(eventSubscriptionDeletionResponse.getResponseStatus(), HttpStatus.SC_NO_CONTENT);
    }

    @Test
    public void testDeleteEventSubscriptionWithInvalidClientID() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.deleteEventSubscription(any())).thenReturn(true);

        eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                .thenThrow(FSEventNotificationException.class);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.isInvokeExternalService(any()))
                .thenReturn(false);

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        EventSubscriptionResponse eventSubscriptionDeletionResponse = defaultEventSubscriptionServiceHandler
                .deleteEventSubscription(EventNotificationTestConstants.SAMPLE_CLIENT_ID,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);

        Assert.assertEquals(eventSubscriptionDeletionResponse.getResponseStatus(), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testDeleteEventSubscriptionServiceError() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.deleteEventSubscription(any()))
                .thenThrow(new FSEventNotificationException(EventNotificationConstants.
                        ERROR_DELETING_EVENT_SUBSCRIPTION));
        Mockito.when(eventSubscriptionService.getEventSubscriptionBySubscriptionId(any()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscription());

        eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                .thenAnswer((Answer<Void>) invocation -> null);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.isInvokeExternalService(any()))
                .thenReturn(false);

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        EventSubscriptionResponse eventSubscriptionDeletionResponse = defaultEventSubscriptionServiceHandler
                .deleteEventSubscription(EventNotificationTestConstants.SAMPLE_CLIENT_ID,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);

        Assert.assertEquals(eventSubscriptionDeletionResponse.getResponseStatus(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testMapSubscriptionModelToResponseJson() {

        JSONObject eventSubscriptionCreationResponse = defaultEventSubscriptionServiceHandler
                .mapSubscriptionModelToResponseJson(EventNotificationTestUtils.getSampleStoredEventSubscription());

        Assert.assertEquals(eventSubscriptionCreationResponse.get(EventNotificationConstants.SUBSCRIPTION_ID_PARAM),
                EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);
        Assert.assertEquals(eventSubscriptionCreationResponse.get(EventNotificationConstants.CALLBACK_URL_PARAM),
                EventNotificationTestConstants.SAMPLE_CALLBACK_URL);
        Assert.assertEquals(eventSubscriptionCreationResponse.get(EventNotificationConstants.VERSION_PARAM),
                EventNotificationTestConstants.SAMPLE_SPEC_VERSION);
        Assert.assertNotNull(eventSubscriptionCreationResponse.get(EventNotificationConstants.EVENT_TYPES_PARAM));
    }
}
