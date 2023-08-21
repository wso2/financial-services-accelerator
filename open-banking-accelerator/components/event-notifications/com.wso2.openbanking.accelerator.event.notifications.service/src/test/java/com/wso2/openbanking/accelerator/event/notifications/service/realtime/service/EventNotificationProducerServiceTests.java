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

import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationTestConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.dto.NotificationDTO;
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.internal.EventNotificationDataHolder;
import com.wso2.openbanking.accelerator.event.notifications.service.model.Notification;
import com.wso2.openbanking.accelerator.event.notifications.service.realtime.model.RealtimeEventNotification;
import com.wso2.openbanking.accelerator.event.notifications.service.service.DefaultEventNotificationGenerator;
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

import java.util.concurrent.LinkedBlockingQueue;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;

/**
 * Test class for EventNotificationProducerService.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({EventNotificationDataHolder.class, DefaultEventNotificationGenerator.class,
        EventNotificationServiceUtil.class, DefaultRealtimeEventNotificationRequestGenerator.class})
public class EventNotificationProducerServiceTests extends PowerMockTestCase {
    @Test
    public void testRun() throws OBEventNotificationException, ParseException, InterruptedException {
        LinkedBlockingQueue<RealtimeEventNotification> eventQueue = new LinkedBlockingQueue<>();
        String callbackUrl = EventNotificationTestConstants.SAMPLE_CALLBACK_URL;

        DefaultEventNotificationGenerator mockedEventNotificationGenerator =
                Mockito.mock(DefaultEventNotificationGenerator.class);
        DefaultRealtimeEventNotificationRequestGenerator mockedRealtimeEventNotificationRequestGenerator =
                Mockito.mock(DefaultRealtimeEventNotificationRequestGenerator.class);

        PowerMockito.mockStatic(EventNotificationServiceUtil.class);
        PowerMockito.when(EventNotificationServiceUtil.getEventNotificationGenerator()).thenReturn(
                mockedEventNotificationGenerator);
        PowerMockito.when(EventNotificationServiceUtil.getRealtimeEventNotificationRequestGenerator())
                .thenReturn(mockedRealtimeEventNotificationRequestGenerator);
        PowerMockito.when(EventNotificationServiceUtil.getCallbackURL(Mockito.any())).thenReturn(callbackUrl);

        EventNotificationDataHolder eventNotificationDataHolderMock = Mockito.mock(EventNotificationDataHolder.class);
        Mockito.when(eventNotificationDataHolderMock.getRealtimeEventNotificationQueue()).thenReturn(eventQueue);
        PowerMockito.mockStatic(EventNotificationDataHolder.class);
        PowerMockito.when(EventNotificationDataHolder.getInstance()).thenReturn(eventNotificationDataHolderMock);

        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setClientId(EventNotificationTestConstants.SAMPLE_CLIENT_ID);
        notificationDTO.setNotificationId(EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);

        Notification testNotification = new Notification();
        String testEventSET = EventNotificationTestConstants.SAMPLE_SET;
        String testPayload = EventNotificationTestConstants.SAMPLE_NOTIFICATION_PAYLOAD;

        doReturn(testNotification).when(mockedEventNotificationGenerator).generateEventNotificationBody(any(), any());
        doReturn(testEventSET).when(mockedEventNotificationGenerator).generateEventNotification(any());
        doReturn(testPayload).when(mockedRealtimeEventNotificationRequestGenerator)
                .getRealtimeEventNotificationPayload(any(), any());

        EventNotificationProducerService eventNotificationProducerService =
                new EventNotificationProducerService(notificationDTO,
                        EventNotificationTestUtils.getSampleNotificationsList());

        new Thread(eventNotificationProducerService).start();

        Thread.sleep(5000);
        RealtimeEventNotification notification = eventQueue.take();

        Assert.assertEquals(notification.getJsonPayload(), testPayload);
        Assert.assertEquals(notification.getNotificationId(), EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);
        Assert.assertEquals(notification.getCallbackUrl(), callbackUrl);
    }
}
