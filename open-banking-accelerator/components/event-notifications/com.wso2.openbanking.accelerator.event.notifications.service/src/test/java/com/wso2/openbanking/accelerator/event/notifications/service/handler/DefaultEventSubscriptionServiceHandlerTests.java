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

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationTestConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.response.EventSubscriptionResponse;
import com.wso2.openbanking.accelerator.event.notifications.service.service.EventSubscriptionService;
import com.wso2.openbanking.accelerator.event.notifications.service.util.EventNotificationServiceUtil;
import com.wso2.openbanking.accelerator.event.notifications.service.utils.EventNotificationTestUtils;
import net.minidev.json.JSONObject;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Test class for DefaultEventSubscriptionServiceHandler.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({OpenBankingConfigParser.class, EventNotificationServiceUtil.class})
public class DefaultEventSubscriptionServiceHandlerTests extends PowerMockTestCase {
    @BeforeMethod
    public void mock() {
        EventNotificationTestUtils.mockConfigParser();
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    DefaultEventSubscriptionServiceHandler defaultEventSubscriptionServiceHandler =
            new DefaultEventSubscriptionServiceHandler();

    @Test
    public void testCreateEventSubscription() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.createEventSubscription(Mockito.anyObject()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscription());

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        mockStatic(EventNotificationServiceUtil.class);
        doNothing().when(EventNotificationServiceUtil.class, "validateClientId", anyString());

        EventSubscriptionResponse eventSubscriptionCreationResponse = defaultEventSubscriptionServiceHandler
                .createEventSubscription(EventNotificationTestUtils.getSampleEventSubscriptionDTO());

        Assert.assertEquals(eventSubscriptionCreationResponse.getStatus(), EventNotificationConstants.CREATED);
    }

    @Test
    public void testCreateEventSubscriptionServiceError() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.createEventSubscription(Mockito.anyObject()))
                .thenThrow(new OBEventNotificationException(EventNotificationConstants.
                        ERROR_STORING_EVENT_SUBSCRIPTION));

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        mockStatic(EventNotificationServiceUtil.class);
        doNothing().when(EventNotificationServiceUtil.class, "validateClientId", anyString());

        EventSubscriptionResponse eventSubscriptionCreationResponse = defaultEventSubscriptionServiceHandler
                .createEventSubscription(EventNotificationTestUtils.getSampleEventSubscriptionDTO());

        Assert.assertEquals(eventSubscriptionCreationResponse.getStatus(),
                EventNotificationConstants.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testGetEventSubscription() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.getEventSubscriptionBySubscriptionId(Mockito.anyObject()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscription());

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        mockStatic(EventNotificationServiceUtil.class);
        doNothing().when(EventNotificationServiceUtil.class, "validateClientId", anyString());

        EventSubscriptionResponse eventSubscriptionRetrieveResponse = defaultEventSubscriptionServiceHandler
                .getEventSubscription(EventNotificationTestConstants.SAMPLE_CLIENT_ID,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);

        Assert.assertEquals(EventNotificationConstants.OK, eventSubscriptionRetrieveResponse.getStatus());
    }

    @Test
    public void testGetEventSubscriptionServiceError() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.getEventSubscriptionBySubscriptionId(Mockito.anyObject()))
                .thenThrow(new OBEventNotificationException(EventNotificationConstants.
                        ERROR_RETRIEVING_EVENT_SUBSCRIPTIONS));

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        mockStatic(EventNotificationServiceUtil.class);
        doNothing().when(EventNotificationServiceUtil.class, "validateClientId", anyString());

        EventSubscriptionResponse eventSubscriptionRetrieveResponse = defaultEventSubscriptionServiceHandler
                .getEventSubscription(EventNotificationTestConstants.SAMPLE_CLIENT_ID,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);

        Assert.assertEquals(EventNotificationConstants.INTERNAL_SERVER_ERROR,
                eventSubscriptionRetrieveResponse.getStatus());
    }

    @Test
    public void testGetAllEventSubscriptions() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.getEventSubscriptionsByClientId(Mockito.anyObject()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscriptions());

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        mockStatic(EventNotificationServiceUtil.class);
        doNothing().when(EventNotificationServiceUtil.class, "validateClientId", anyString());

        EventSubscriptionResponse eventSubscriptionRetrieveResponse = defaultEventSubscriptionServiceHandler
                .getAllEventSubscriptions(EventNotificationTestConstants.SAMPLE_CLIENT_ID);

        Assert.assertEquals(EventNotificationConstants.OK, eventSubscriptionRetrieveResponse.getStatus());
    }

    @Test
    public void testGetAllEventSubscriptionsServiceError() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.getEventSubscriptionsByClientId(Mockito.anyObject()))
                .thenThrow(new OBEventNotificationException(EventNotificationConstants.
                        ERROR_RETRIEVING_EVENT_SUBSCRIPTIONS));

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        mockStatic(EventNotificationServiceUtil.class);
        doNothing().when(EventNotificationServiceUtil.class, "validateClientId", anyString());

        EventSubscriptionResponse eventSubscriptionRetrieveResponse = defaultEventSubscriptionServiceHandler
                .getAllEventSubscriptions(EventNotificationTestConstants.SAMPLE_CLIENT_ID);

        Assert.assertEquals(EventNotificationConstants.INTERNAL_SERVER_ERROR,
                eventSubscriptionRetrieveResponse.getStatus());
    }

    @Test
    public void testGetEventSubscriptionsByEventType() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.getEventSubscriptionsByClientIdAndEventType(Mockito.anyString()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscriptions());

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        mockStatic(EventNotificationServiceUtil.class);
        doNothing().when(EventNotificationServiceUtil.class, "validateClientId", anyString());

        EventSubscriptionResponse eventSubscriptionRetrieveResponse = defaultEventSubscriptionServiceHandler
                .getEventSubscriptionsByEventType(EventNotificationTestConstants.SAMPLE_CLIENT_ID,
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1);

        Assert.assertEquals(EventNotificationConstants.OK, eventSubscriptionRetrieveResponse.getStatus());
    }

    @Test
    public void testGetEventSubscriptionsByEventTypeServiceError() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.getEventSubscriptionsByClientIdAndEventType(Mockito.anyString()))
                .thenThrow(new OBEventNotificationException(EventNotificationConstants.
                        ERROR_RETRIEVING_EVENT_SUBSCRIPTIONS));

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        mockStatic(EventNotificationServiceUtil.class);
        doNothing().when(EventNotificationServiceUtil.class, "validateClientId", anyString());

        EventSubscriptionResponse eventSubscriptionRetrieveResponse = defaultEventSubscriptionServiceHandler
                .getEventSubscriptionsByEventType(EventNotificationTestConstants.SAMPLE_CLIENT_ID,
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1);

        Assert.assertEquals(EventNotificationConstants.INTERNAL_SERVER_ERROR,
                eventSubscriptionRetrieveResponse.getStatus());
    }

    @Test
    public void testUpdateEventSubscription() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.updateEventSubscription(Mockito.anyObject()))
                .thenReturn(true);
        Mockito.when(eventSubscriptionService.getEventSubscriptionBySubscriptionId(Mockito.anyObject()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscription());

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        mockStatic(EventNotificationServiceUtil.class);
        doNothing().when(EventNotificationServiceUtil.class, "validateClientId", anyString());

        EventSubscriptionResponse eventSubscriptionUpdateResponse = defaultEventSubscriptionServiceHandler
                .updateEventSubscription(EventNotificationTestUtils.getSampleEventSubscriptionUpdateDTO());

        Assert.assertEquals(EventNotificationConstants.OK, eventSubscriptionUpdateResponse.getStatus());
    }

    @Test
    public void testUpdateEventSubscriptionServiceError() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.updateEventSubscription(Mockito.anyObject()))
                .thenReturn(true);
        Mockito.when(eventSubscriptionService.getEventSubscriptionBySubscriptionId(Mockito.anyObject()))
                .thenThrow(new OBEventNotificationException(EventNotificationConstants.
                        ERROR_UPDATING_EVENT_SUBSCRIPTION));

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        mockStatic(EventNotificationServiceUtil.class);
        doNothing().when(EventNotificationServiceUtil.class, "validateClientId", anyString());

        EventSubscriptionResponse eventSubscriptionUpdateResponse = defaultEventSubscriptionServiceHandler
                .updateEventSubscription(EventNotificationTestUtils.getSampleEventSubscriptionUpdateDTO());

        Assert.assertEquals(EventNotificationConstants.INTERNAL_SERVER_ERROR,
                eventSubscriptionUpdateResponse.getStatus());
    }

    @Test
    public void testDeleteEventSubscription() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.deleteEventSubscription(Mockito.anyObject()))
                .thenReturn(true);

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        mockStatic(EventNotificationServiceUtil.class);
        doNothing().when(EventNotificationServiceUtil.class, "validateClientId", anyString());

        EventSubscriptionResponse eventSubscriptionDeletionResponse = defaultEventSubscriptionServiceHandler
                .deleteEventSubscription(EventNotificationTestConstants.SAMPLE_CLIENT_ID,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);

        Assert.assertEquals(EventNotificationConstants.NO_CONTENT, eventSubscriptionDeletionResponse.getStatus());
    }

    @Test
    public void testDeleteEventSubscriptionServiceError() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.deleteEventSubscription(Mockito.anyObject()))
                .thenThrow(new OBEventNotificationException(EventNotificationConstants.
                        ERROR_DELETING_EVENT_SUBSCRIPTION));

        defaultEventSubscriptionServiceHandler.setEventSubscriptionService(eventSubscriptionService);

        mockStatic(EventNotificationServiceUtil.class);
        doNothing().when(EventNotificationServiceUtil.class, "validateClientId", anyString());

        EventSubscriptionResponse eventSubscriptionDeletionResponse = defaultEventSubscriptionServiceHandler
                .deleteEventSubscription(EventNotificationTestConstants.SAMPLE_CLIENT_ID,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);

        Assert.assertEquals(EventNotificationConstants.INTERNAL_SERVER_ERROR,
                eventSubscriptionDeletionResponse.getStatus());
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
        Assert.assertNotNull(eventSubscriptionCreationResponse.get(EventNotificationConstants.EVENT_TYPE_PARAM));
    }
}
