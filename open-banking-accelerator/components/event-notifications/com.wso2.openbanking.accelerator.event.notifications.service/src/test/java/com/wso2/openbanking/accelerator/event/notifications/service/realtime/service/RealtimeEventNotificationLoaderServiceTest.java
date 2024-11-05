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

package com.wso2.openbanking.accelerator.event.notifications.service.realtime.service;

import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationTestConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.dao.AggregatedPollingDAOImpl;
import com.wso2.openbanking.accelerator.event.notifications.service.dto.NotificationDTO;
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.internal.EventNotificationDataHolder;
import com.wso2.openbanking.accelerator.event.notifications.service.model.Notification;
import com.wso2.openbanking.accelerator.event.notifications.service.persistence.EventPollingStoreInitializer;
import com.wso2.openbanking.accelerator.event.notifications.service.realtime.model.RealtimeEventNotification;
import com.wso2.openbanking.accelerator.event.notifications.service.service.DefaultEventNotificationGenerator;
import com.wso2.openbanking.accelerator.event.notifications.service.service.EventSubscriptionService;
import com.wso2.openbanking.accelerator.event.notifications.service.util.EventNotificationServiceUtil;
import com.wso2.openbanking.accelerator.event.notifications.service.utils.EventNotificationTestUtils;
import net.minidev.json.parser.ParseException;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;

/**
 * Test class for RealtimeEventNotificationLoaderService.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({EventNotificationDataHolder.class, DefaultEventNotificationGenerator.class,
        EventNotificationServiceUtil.class, EventPollingStoreInitializer.class, AggregatedPollingDAOImpl.class,
        DefaultRealtimeEventNotificationRequestGenerator.class})
public class RealtimeEventNotificationLoaderServiceTest extends PowerMockTestCase {
    @Test
    public void testRun() throws OBEventNotificationException, InterruptedException, ParseException {
        LinkedBlockingQueue<RealtimeEventNotification> eventQueue = new LinkedBlockingQueue<>();

        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.getEventSubscriptionsByClientId(any()))
                .thenReturn(EventNotificationTestUtils.getEventSubscrptionList());

        DefaultEventNotificationGenerator mockedEventNotificationGenerator =
                Mockito.mock(DefaultEventNotificationGenerator.class);
        DefaultRealtimeEventNotificationRequestGenerator mockedRealtimeEventNotificationRequestGenerator =
                Mockito.mock(DefaultRealtimeEventNotificationRequestGenerator.class);

        PowerMockito.mockStatic(EventNotificationServiceUtil.class);
        PowerMockito.when(EventNotificationServiceUtil.getEventNotificationGenerator()).thenReturn(
                mockedEventNotificationGenerator);
        PowerMockito.when(EventNotificationServiceUtil.getRealtimeEventNotificationRequestGenerator()).thenReturn(
                mockedRealtimeEventNotificationRequestGenerator);
        PowerMockito.when(EventNotificationServiceUtil.getEventSubscriptionService())
                .thenReturn(eventSubscriptionService);

        EventNotificationDataHolder eventNotificationDataHolderMock = Mockito.mock(EventNotificationDataHolder.class);
        Mockito.when(eventNotificationDataHolderMock.getRealtimeEventNotificationQueue()).thenReturn(eventQueue);
        PowerMockito.mockStatic(EventNotificationDataHolder.class);
        PowerMockito.when(EventNotificationDataHolder.getInstance()).thenReturn(eventNotificationDataHolderMock);

        NotificationDTO notificationDTO1 = new NotificationDTO();
        notificationDTO1.setClientId(EventNotificationTestConstants.SAMPLE_CLIENT_ID);
        notificationDTO1.setNotificationId(EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);

        NotificationDTO notificationDTO2 = new NotificationDTO();
        notificationDTO2.setClientId(EventNotificationTestConstants.SAMPLE_CLIENT_ID_2);
        notificationDTO2.setNotificationId(EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID_2);

        List<NotificationDTO> notifications = new ArrayList<>();
        notifications.add(notificationDTO1);
        notifications.add(notificationDTO2);

        AggregatedPollingDAOImpl mockAggregatedPollingDAOImpl = Mockito.mock(AggregatedPollingDAOImpl.class);
        doReturn(notifications).when(mockAggregatedPollingDAOImpl).getNotificationsByStatus(
                EventNotificationConstants.OPEN);
        doReturn(EventNotificationTestUtils.getSampleNotificationsList())
                .when(mockAggregatedPollingDAOImpl).getEventsByNotificationID(any());
        PowerMockito.mockStatic(EventPollingStoreInitializer.class);
        PowerMockito.when(EventPollingStoreInitializer.getAggregatedPollingDAO())
                .thenReturn(mockAggregatedPollingDAOImpl);


        Notification testNotification = new Notification();
        String testEventSET = EventNotificationTestConstants.SAMPLE_SET;
        String testPayload =
                "{\"notificationId\": " + notificationDTO1.getNotificationId() + ", \"SET\": " + testEventSET + "}";

        doReturn(testNotification).when(mockedEventNotificationGenerator).generateEventNotificationBody(any(), any());
        doReturn(testEventSET).when(mockedEventNotificationGenerator).generateEventNotification(any());
        doReturn(testPayload).when(mockedRealtimeEventNotificationRequestGenerator)
                .getRealtimeEventNotificationPayload(any(), any());

        new Thread(new RealtimeEventNotificationLoaderService()).start();

        Thread.sleep(5000);
        RealtimeEventNotification notification1 = eventQueue.take();
        RealtimeEventNotification notification2 = eventQueue.take();

        Assert.assertEquals(notification1.getNotificationId(), EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);
        Assert.assertEquals(notification2.getNotificationId(), EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID_2);
        Assert.assertEquals(notification1.getJsonPayload(), testPayload);
        Assert.assertEquals(notification2.getJsonPayload(), testPayload);
    }
}
