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
import com.wso2.openbanking.accelerator.event.notifications.service.dto.EventPollingDTO;
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.response.EventPollingResponse;
import com.wso2.openbanking.accelerator.event.notifications.service.service.EventPollingService;
import com.wso2.openbanking.accelerator.event.notifications.service.util.EventNotificationServiceUtil;
import com.wso2.openbanking.accelerator.event.notifications.service.utils.EventNotificationTestUtils;
import net.minidev.json.JSONObject;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
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
 * Test class for DefaultEventPollingServiceHandler.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({OpenBankingConfigParser.class, EventNotificationServiceUtil.class})
public class DefaultEventPollingServiceHandlerTests extends PowerMockTestCase {

    @BeforeMethod
    public void mock() {

        EventNotificationTestUtils.mockConfigParser();

    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    DefaultEventPollingServiceHandler defaultEventPollingServiceHandler = new DefaultEventPollingServiceHandler();


    @Test
    public void testMapPollingRequest() {

        EventPollingDTO eventPollingDTO = defaultEventPollingServiceHandler.mapPollingRequest(
                EventNotificationTestUtils.getEventRequest());

        Assert.assertEquals(eventPollingDTO.getReturnImmediately(),
                EventNotificationTestConstants.SAMPLE_RETURN_IMMEDIATETLY);
        Assert.assertEquals(eventPollingDTO.getMaxEvents(), EventNotificationTestConstants.SAMPLE_MAX_EVENTS);
    }

//    @Test
//    public void mapPollingForEmptyRequest() {
//        JSONObject eventPollingRequest = new JSONObject();
//        eventPollingRequest.put(EventNotificationConstants.X_WSO2_CLIENT_ID,
//                EventNotificationTestConstants.SAMPLE_CLIENT_ID);
//        EventPollingDTO eventPollingDTO = defaultEventPollingServiceHandler.mapPollingRequest(eventPollingRequest);
//
//        Assert.assertEquals(eventPollingDTO., 0);
//    }

    @Test
    public void testMapPollingRequestWithError() {

        JSONObject eventRequest = EventNotificationTestUtils.getEventRequest();
        eventRequest.put(EventNotificationConstants.SET_ERRORS, EventNotificationTestUtils.getPollingError());

        EventPollingDTO eventPollingDTO = defaultEventPollingServiceHandler.mapPollingRequest(eventRequest);

        Assert.assertEquals(eventPollingDTO.getMaxEvents(), EventNotificationTestConstants.SAMPLE_MAX_EVENTS);
    }


    @Test
    public void testPollEvents() throws Exception {

        JSONObject eventPollingRequest = new JSONObject();
        eventPollingRequest.put(EventNotificationConstants.X_WSO2_CLIENT_ID,
                 EventNotificationTestConstants.SAMPLE_CLIENT_ID);
        eventPollingRequest.put(EventNotificationConstants.MAX_EVENTS, 5);

        EventPollingService eventPollingService = Mockito.mock(EventPollingService.class);
        Mockito.when(eventPollingService.pollEvents(Mockito.anyObject())).thenReturn(EventNotificationTestUtils.
                getAggregatedPollingResponse());

        defaultEventPollingServiceHandler.setEventPollingService(eventPollingService);

        mockStatic(EventNotificationServiceUtil.class);
        doNothing().when(EventNotificationServiceUtil.class, "validateClientId", anyString());

        EventPollingResponse eventPollingResponse =
               defaultEventPollingServiceHandler.pollEvents(eventPollingRequest);

        Assert.assertEquals(eventPollingResponse.getStatus(), "OK");
        Assert.assertTrue(eventPollingResponse.getResponseBody().containsKey("moreAvailable"));
        Assert.assertTrue(eventPollingResponse.getResponseBody().containsKey("sets"));
    }

    @Test
    public void testPollEventsInvalidClient() throws Exception {
        JSONObject eventPollingRequest = new JSONObject();
        eventPollingRequest.put(EventNotificationConstants.X_WSO2_CLIENT_ID,
                EventNotificationTestConstants.SAMPLE_CLIENT_ID);
        eventPollingRequest.put(EventNotificationConstants.MAX_EVENTS, 5);

        EventPollingService eventPollingService = Mockito.mock(EventPollingService.class);
        Mockito.when(eventPollingService.pollEvents(Mockito.anyObject())).thenReturn(EventNotificationTestUtils.
                getAggregatedPollingResponse());

        defaultEventPollingServiceHandler.setEventPollingService(eventPollingService);

        mockStatic(EventNotificationServiceUtil.class);
        PowerMockito.doThrow(new OBEventNotificationException("Invalid client ID")).when(
                EventNotificationServiceUtil.class);
        EventNotificationServiceUtil.validateClientId(anyString());

        EventPollingResponse eventPollingResponse =
                defaultEventPollingServiceHandler.pollEvents(eventPollingRequest);

        Assert.assertEquals(eventPollingResponse.getStatus(), EventNotificationConstants.BAD_REQUEST);
    }

    @Test
    public void testPollEventsServiceError() throws Exception {

        JSONObject eventPollingRequest = new JSONObject();
        eventPollingRequest.put(EventNotificationConstants.X_WSO2_CLIENT_ID,
                EventNotificationTestConstants.SAMPLE_CLIENT_ID);
        eventPollingRequest.put(EventNotificationConstants.MAX_EVENTS, 5);

        EventPollingService eventPollingService = Mockito.mock(EventPollingService.class);
        Mockito.when(eventPollingService.pollEvents(Mockito.anyObject())).thenThrow(new
                OBEventNotificationException("Error when polling events"));

        defaultEventPollingServiceHandler.setEventPollingService(eventPollingService);

        mockStatic(EventNotificationServiceUtil.class);
        doNothing().when(EventNotificationServiceUtil.class, "validateClientId", anyString());

        EventPollingResponse eventPollingResponse =
                defaultEventPollingServiceHandler.pollEvents(eventPollingRequest);

        Assert.assertEquals(eventPollingResponse.getStatus(), EventNotificationConstants.BAD_REQUEST);

    }
}
