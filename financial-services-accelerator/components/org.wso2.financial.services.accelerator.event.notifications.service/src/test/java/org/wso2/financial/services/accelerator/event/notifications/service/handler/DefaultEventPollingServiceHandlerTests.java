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
import org.wso2.financial.services.accelerator.event.notifications.service.EventPollingService;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationTestConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.dto.EventPollingDTO;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.model.EventPollingResponse;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationServiceUtil;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Test class for DefaultEventPollingServiceHandler.
 */
public class DefaultEventPollingServiceHandlerTests {

    private MockedStatic<FinancialServicesConfigParser> configParserMockedStatic;
    private MockedStatic<ServiceExtensionUtils> serviceExtensionUtilsMockedStatic;

    @BeforeClass
    public void initTest() {
        configParserMockedStatic = Mockito.mockStatic(FinancialServicesConfigParser.class);
        serviceExtensionUtilsMockedStatic = Mockito.mockStatic(ServiceExtensionUtils.class);

        Map<String, Object> configs = new HashMap<String, Object>();
        configs.put(FinancialServicesConstants.REALTIME_EVENT_NOTIFICATION_ENABLED, false);
        FinancialServicesConfigParser configParserMock = Mockito.mock(FinancialServicesConfigParser.class);
        Mockito.doReturn(configs).when(configParserMock).getConfiguration();
        configParserMockedStatic.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils.isInvokeExternalService(any()))
                .thenReturn(false);
    }

    @AfterClass
    public void tearDown() {
        configParserMockedStatic.close();
        serviceExtensionUtilsMockedStatic.close();
    }

    DefaultEventPollingServiceHandler defaultEventPollingServiceHandler = new DefaultEventPollingServiceHandler();

    @Test
    public void testPollEvents() throws Exception {

        try (MockedStatic<EventNotificationServiceUtil> eventNotificationUtilMockedStatic =
                     Mockito.mockStatic(EventNotificationServiceUtil.class)) {

            EventPollingDTO eventPollingRequest = new EventPollingDTO();
            eventPollingRequest.setClientId(EventNotificationTestConstants.SAMPLE_CLIENT_ID);
            eventPollingRequest.setMaxEvents(5);

            EventPollingService eventPollingService = Mockito.mock(EventPollingService.class);
            Mockito.when(eventPollingService.pollEvents(any())).thenReturn(EventNotificationTestUtils.
                    getAggregatedPollingResponse());

            defaultEventPollingServiceHandler.setEventPollingService(eventPollingService);

            eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                    .thenAnswer((Answer<Void>) invocation -> null);

            EventPollingResponse eventPollingResponse =
                    defaultEventPollingServiceHandler.pollEvents(eventPollingRequest);

            Assert.assertEquals(eventPollingResponse.getStatus(), "OK");
            Assert.assertTrue(eventPollingResponse.getResponseBody().has("moreAvailable"));
            Assert.assertTrue(eventPollingResponse.getResponseBody().has("sets"));
        }
    }

    @Test(expectedExceptions = FSEventNotificationException.class)
    public void testPollEventsInvalidClient() throws Exception {

        try (MockedStatic<EventNotificationServiceUtil> eventNotificationUtilMockedStatic =
                     Mockito.mockStatic(EventNotificationServiceUtil.class)) {

            EventPollingDTO eventPollingRequest = new EventPollingDTO();
            eventPollingRequest.setClientId(EventNotificationTestConstants.SAMPLE_CLIENT_ID);
            eventPollingRequest.setMaxEvents(5);

            EventPollingService eventPollingService = Mockito.mock(EventPollingService.class);
            Mockito.when(eventPollingService.pollEvents(any())).thenReturn(EventNotificationTestUtils.
                    getAggregatedPollingResponse());

            defaultEventPollingServiceHandler.setEventPollingService(eventPollingService);

            eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                    .thenThrow(FSEventNotificationException.class);

            EventPollingResponse eventPollingResponse =
                    defaultEventPollingServiceHandler.pollEvents(eventPollingRequest);

            Assert.assertEquals(eventPollingResponse.getStatus(), EventNotificationConstants.BAD_REQUEST);
        }
    }

    @Test(expectedExceptions = FSEventNotificationException.class)
    public void testPollEventsServiceError() throws Exception {

        try (MockedStatic<EventNotificationServiceUtil> eventNotificationUtilMockedStatic =
                     Mockito.mockStatic(EventNotificationServiceUtil.class)) {

            EventPollingDTO eventPollingRequest = new EventPollingDTO();
            eventPollingRequest.setClientId(EventNotificationTestConstants.SAMPLE_CLIENT_ID);
            eventPollingRequest.setMaxEvents(5);

            EventPollingService eventPollingService = Mockito.mock(EventPollingService.class);
            Mockito.when(eventPollingService.pollEvents(any())).thenThrow(new
                    FSEventNotificationException("Error when polling events"));

            defaultEventPollingServiceHandler.setEventPollingService(eventPollingService);

            eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                    .thenAnswer((Answer<Void>) invocation -> null);

            EventPollingResponse eventPollingResponse =
                    defaultEventPollingServiceHandler.pollEvents(eventPollingRequest);

            Assert.assertEquals(eventPollingResponse.getStatus(), EventNotificationConstants.BAD_REQUEST);
        }
    }
}
