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

package org.wso2.financial.services.accelerator.event.notifications.service.realtime;

import org.json.JSONObject;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.event.notifications.service.DefaultEventNotificationGenerator;
import org.wso2.financial.services.accelerator.event.notifications.service.EventSubscriptionService;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationTestConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.internal.EventNotificationDataHolder;
import org.wso2.financial.services.accelerator.event.notifications.service.model.Notification;
import org.wso2.financial.services.accelerator.event.notifications.service.model.NotificationResponse;
import org.wso2.financial.services.accelerator.event.notifications.service.model.RealtimeEventNotification;
import org.wso2.financial.services.accelerator.event.notifications.service.realtime.service.DefaultRealtimeEventNotificationRequestGenerator;
import org.wso2.financial.services.accelerator.event.notifications.service.realtime.service.EventNotificationProducerService;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationServiceUtil;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationTestUtils;

import java.util.concurrent.LinkedBlockingQueue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

/**
 * Test class for EventNotificationProducerService.
 */
public class EventNotificationProducerServiceTests {

    private MockedStatic<EventNotificationServiceUtil> notificationServiceUtilMockedStatic;
    private MockedStatic<EventNotificationDataHolder> eventNotificationDataHolderMockedStatic;
        DefaultRealtimeEventNotificationRequestGenerator mockedRealtimeEventNotificationRequestGenerator;
private LinkedBlockingQueue<RealtimeEventNotification> eventQueue;

    @BeforeClass
    public void initTest() throws FSEventNotificationException {

        NotificationResponse testNotification = new NotificationResponse();
        String testEventSET = EventNotificationTestConstants.SAMPLE_SET;
        JSONObject testPayload = EventNotificationTestConstants.SAMPLE_NOTIFICATION_PAYLOAD;
//
        DefaultEventNotificationGenerator mockedEventNotificationGenerator =
                Mockito.mock(DefaultEventNotificationGenerator.class);
        mockedRealtimeEventNotificationRequestGenerator =
                Mockito.mock(DefaultRealtimeEventNotificationRequestGenerator.class);
////
        doReturn(testNotification).when(mockedEventNotificationGenerator).generateEventNotificationBody(any(), any());
        doReturn(testEventSET).when(mockedEventNotificationGenerator).generateEventNotification(any());
        doReturn(testPayload).when(mockedRealtimeEventNotificationRequestGenerator)
                .getRealtimeEventNotificationPayload(any(), any());
//
        notificationServiceUtilMockedStatic = Mockito.mockStatic(EventNotificationServiceUtil.class);
        notificationServiceUtilMockedStatic.when(EventNotificationServiceUtil::getEventNotificationGenerator)
                .thenReturn(mockedEventNotificationGenerator);
        notificationServiceUtilMockedStatic
                .when(EventNotificationServiceUtil::getRealtimeEventNotificationRequestGenerator)
                .thenReturn(mockedRealtimeEventNotificationRequestGenerator);

        eventQueue = new LinkedBlockingQueue<>();
        EventNotificationDataHolder eventNotificationDataHolder = Mockito.mock(EventNotificationDataHolder.class);
        eventNotificationDataHolder.setRealtimeEventNotificationQueue(eventQueue);

        eventNotificationDataHolderMockedStatic = Mockito.mockStatic(EventNotificationDataHolder.class);
        eventNotificationDataHolderMockedStatic.when(EventNotificationDataHolder::getInstance)
                .thenReturn(eventNotificationDataHolder);
    }

    @AfterClass
    public void tearDown() {
        notificationServiceUtilMockedStatic.close();
        eventNotificationDataHolderMockedStatic.close();
    }

    @Test
    public void testRun() throws FSEventNotificationException, InterruptedException {

        try (MockedStatic<EventNotificationServiceUtil> notificationStatic =
                     Mockito.mockStatic(EventNotificationServiceUtil.class);
                     MockedStatic<EventNotificationDataHolder> eventStatic =
                             Mockito.mockStatic(EventNotificationDataHolder.class)) {

            NotificationResponse testNotification = new NotificationResponse();
            String testEventSET = EventNotificationTestConstants.SAMPLE_SET;
            JSONObject testPayload = EventNotificationTestConstants.SAMPLE_NOTIFICATION_PAYLOAD;

            DefaultEventNotificationGenerator mockedEventNotificationGenerator =
                    Mockito.mock(DefaultEventNotificationGenerator.class);
            mockedRealtimeEventNotificationRequestGenerator =
                Mockito.mock(DefaultRealtimeEventNotificationRequestGenerator.class);

            doReturn(testNotification).when(mockedEventNotificationGenerator)
                    .generateEventNotificationBody(any(), any());
            doReturn(testEventSET).when(mockedEventNotificationGenerator).generateEventNotification(any());
            doReturn(testPayload).when(mockedRealtimeEventNotificationRequestGenerator)
                .getRealtimeEventNotificationPayload(any(), any());

            notificationStatic.when(EventNotificationServiceUtil::getEventNotificationGenerator)
                    .thenReturn(mockedEventNotificationGenerator);
            notificationServiceUtilMockedStatic
                .when(EventNotificationServiceUtil::getRealtimeEventNotificationRequestGenerator)
                .thenReturn(mockedRealtimeEventNotificationRequestGenerator);

            LinkedBlockingQueue<RealtimeEventNotification> eventQueue = new LinkedBlockingQueue<>();
            EventNotificationDataHolder eventNotificationDataHolder = Mockito.mock(EventNotificationDataHolder.class);
            eventNotificationDataHolder.setRealtimeEventNotificationQueue(eventQueue);

            eventStatic.when(EventNotificationDataHolder::getInstance)
                    .thenReturn(eventNotificationDataHolder);

            Notification notificationDTO = new Notification();
            notificationDTO.setClientId(EventNotificationTestConstants.SAMPLE_CLIENT_ID);
            notificationDTO.setNotificationId(EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);

            EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
            doReturn(EventNotificationTestUtils.getEventSubscrptionList()).when(eventSubscriptionService)
                    .getEventSubscriptionsByClientId(anyString());

            EventNotificationProducerService eventNotificationProducerService =
                    new EventNotificationProducerService(notificationDTO,
                            EventNotificationTestUtils.getSampleNotificationsList());
            eventNotificationProducerService.setEventSubscriptionService(eventSubscriptionService);

            new Thread(eventNotificationProducerService).start();

            Thread.sleep(5000);
            RealtimeEventNotification notification = eventQueue.take();

            Assert.assertEquals(notification.getJsonPayload(), testPayload);
            Assert.assertEquals(notification.getNotificationId(),
                    EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);
            Assert.assertNotNull(notification.getCallbackUrl());
        }
    }
}
