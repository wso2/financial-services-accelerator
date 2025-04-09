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
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import org.wso2.financial.services.accelerator.event.notifications.service.EventCreationService;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationTestConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.model.EventCreationResponse;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationServiceUtil;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Test class for DefaultEventCreationServiceHandler.
 */
public class DefaultEventCreationServiceHandlerTest {

    private MockedStatic<FinancialServicesConfigParser> configParserMockedStatic;
    @Mock
    private ConsentCoreServiceImpl consentCoreServiceMock;

    DefaultEventCreationServiceHandler defaultEventCreationServiceHandler;
    private MockedStatic<ServiceExtensionUtils> serviceExtensionUtilsMockedStatic;

    @BeforeClass
    public void initClass() {

        configParserMockedStatic = Mockito.mockStatic(FinancialServicesConfigParser.class);
        consentCoreServiceMock = Mockito.mock(ConsentCoreServiceImpl.class);
        defaultEventCreationServiceHandler = new DefaultEventCreationServiceHandler();
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

    @Test
    public void testPublishEvents() throws Exception {

        try (MockedStatic<EventNotificationServiceUtil> eventNotificationUtilMockedStatic =
                     Mockito.mockStatic(EventNotificationServiceUtil.class)) {

            ConsentResource consentResource = new ConsentResource();
            consentResource.setConsentID("0ba972a9-08cd-4cad-b7e2-20655bcbd9e0");
            Mockito.doReturn(consentResource).when(consentCoreServiceMock).getConsent(anyString(), anyBoolean());
            eventNotificationUtilMockedStatic.when(EventNotificationServiceUtil::getConsentCoreServiceImpl)
                    .thenReturn(consentCoreServiceMock);

            eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                    .thenAnswer((Answer<Void>) invocation -> null);

            EventCreationService eventCreationService = Mockito.mock(EventCreationService.class);
            Mockito.when(eventCreationService.publishEventNotification(any())).
                    thenReturn(EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);

            defaultEventCreationServiceHandler.setEventCreationService(eventCreationService);

            EventCreationResponse eventCreationResponse = defaultEventCreationServiceHandler.publishEvent(
                    EventNotificationTestUtils.getNotificationCreationDTO());

            Assert.assertEquals(eventCreationResponse.getStatus(), HttpStatus.SC_CREATED);
            Assert.assertEquals(eventCreationResponse.getResponseBody()
                            .get(EventNotificationConstants.NOTIFICATIONS_ID),
                    EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);
        }

    }

    @Test
    public void testPublishEventConsentException() throws Exception {

        try (MockedStatic<EventNotificationServiceUtil> eventNotificationUtilMockedStatic =
                     Mockito.mockStatic(EventNotificationServiceUtil.class)) {
            EventCreationService eventCreationService = Mockito.mock(EventCreationService.class);
            Mockito.when(eventCreationService.publishEventNotification(any())).
                    thenReturn(EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);

            defaultEventCreationServiceHandler.setEventCreationService(eventCreationService);

            Mockito.doThrow(ConsentManagementException.class).when(consentCoreServiceMock).getConsent(anyString(),
                    anyBoolean());
            eventNotificationUtilMockedStatic.when(EventNotificationServiceUtil::getConsentCoreServiceImpl)
                    .thenReturn(consentCoreServiceMock);
            eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                    .thenAnswer((Answer<Void>) invocation -> null);

            EventCreationResponse eventCreationResponse = defaultEventCreationServiceHandler.publishEvent(
                    EventNotificationTestUtils.getNotificationCreationDTO());

            Assert.assertEquals(eventCreationResponse.getStatus(), HttpStatus.SC_BAD_REQUEST);
        }
    }

    @Test
    public void testPublishFSEventInvalidClient() throws Exception {

        try (MockedStatic<EventNotificationServiceUtil> eventNotificationUtilMockedStatic =
                     Mockito.mockStatic(EventNotificationServiceUtil.class)) {

            EventCreationService eventCreationService = Mockito.mock(EventCreationService.class);
            Mockito.when(eventCreationService.publishEventNotification(any())).
                    thenReturn(EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);

            ConsentResource consentResource = new ConsentResource();
            consentResource.setConsentID("0ba972a9-08cd-4cad-b7e2-20655bcbd9e0");
            Mockito.doReturn(consentResource).when(consentCoreServiceMock).getConsent(anyString(), anyBoolean());
            eventNotificationUtilMockedStatic.when(EventNotificationServiceUtil::getConsentCoreServiceImpl)
                    .thenReturn(consentCoreServiceMock);

            eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                    .thenThrow(FSEventNotificationException.class);

            EventCreationResponse eventCreationResponse =
                    defaultEventCreationServiceHandler.publishEvent(
                            EventNotificationTestUtils.getNotificationCreationDTO());

            Assert.assertEquals(eventCreationResponse.getStatus(), HttpStatus.SC_BAD_REQUEST);
        }
    }

    @Test
    public void testPublishFSEventsServiceException() throws Exception {

        try (MockedStatic<EventNotificationServiceUtil> eventNotificationUtilMockedStatic =
                     Mockito.mockStatic(EventNotificationServiceUtil.class)) {

            EventCreationService eventCreationService = Mockito.mock(EventCreationService.class);

            Mockito.when(eventCreationService.publishEventNotification(any())).thenThrow(new
                    FSEventNotificationException("Error when persisting events"));

            defaultEventCreationServiceHandler.setEventCreationService(eventCreationService);

            ConsentResource consentResource = new ConsentResource();
            consentResource.setConsentID("0ba972a9-08cd-4cad-b7e2-20655bcbd9e0");
            Mockito.doReturn(consentResource).when(consentCoreServiceMock).getConsent(anyString(), anyBoolean());
            eventNotificationUtilMockedStatic.when(EventNotificationServiceUtil::getConsentCoreServiceImpl)
                    .thenReturn(consentCoreServiceMock);
            eventNotificationUtilMockedStatic.when(() -> EventNotificationServiceUtil.validateClientId(anyString()))
                    .thenAnswer((Answer<Void>) invocation -> null);

            EventCreationResponse eventCreationResponse = defaultEventCreationServiceHandler.publishEvent(
                    EventNotificationTestUtils.getNotificationCreationDTO());

            Assert.assertEquals(eventCreationResponse.getStatus(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
