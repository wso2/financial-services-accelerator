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
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationTestConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.dao.EventNotificationDAOImpl;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.internal.EventNotificationDataHolder;
import org.wso2.financial.services.accelerator.event.notifications.service.model.NotificationResponse;
import org.wso2.financial.services.accelerator.event.notifications.service.model.RealtimeEventNotification;
import org.wso2.financial.services.accelerator.event.notifications.service.persistence.EventNotificationStoreInitializer;
import org.wso2.financial.services.accelerator.event.notifications.service.realtime.service.DefaultRealtimeEventNotificationRequestGenerator;
import org.wso2.financial.services.accelerator.event.notifications.service.realtime.service.RealtimeEventNotificationLoaderService;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationServiceUtil;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationTestUtils;

import java.sql.Connection;
import java.util.concurrent.LinkedBlockingQueue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

/**
 * Test class for RealtimeEventNotificationLoaderService.
 */
public class RealtimeEventNotificationLoaderServiceTest {

    MockedStatic<EventNotificationServiceUtil> notificationServiceUtilMockedStatic;
    MockedStatic<EventNotificationDataHolder> eventNotificationDataHolderMockedStatic;
    MockedStatic<EventNotificationStoreInitializer> notificationStoreInitializerMockedStatic;
    DefaultEventNotificationGenerator mockedEventNotificationGenerator;
    DefaultRealtimeEventNotificationRequestGenerator mockedRealtimeEventNotificationRequestGenerator;
    LinkedBlockingQueue<RealtimeEventNotification> eventQueue;

    @BeforeClass
    public void initTest() throws FSEventNotificationException {

        mockedEventNotificationGenerator = Mockito.mock(DefaultEventNotificationGenerator.class);
        mockedRealtimeEventNotificationRequestGenerator =
                Mockito.mock(DefaultRealtimeEventNotificationRequestGenerator.class);

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

        Connection connection = Mockito.mock(Connection.class);
        EventNotificationDAOImpl mockDAOImpl = Mockito.mock(EventNotificationDAOImpl.class);
        doReturn(EventNotificationTestUtils.getNotificationList()).when(mockDAOImpl).getNotificationsByStatus(
                connection, EventNotificationConstants.OPEN);

        notificationStoreInitializerMockedStatic = Mockito.mockStatic(EventNotificationStoreInitializer.class);
        notificationStoreInitializerMockedStatic.when(EventNotificationStoreInitializer::getEventNotificationDAO)
                .thenReturn(mockDAOImpl);
    }

    @AfterClass
    public void tearDown() {
        notificationServiceUtilMockedStatic.close();
        eventNotificationDataHolderMockedStatic.close();
        notificationStoreInitializerMockedStatic.close();
    }

    @Test
    public void testRun() throws FSEventNotificationException, InterruptedException {
        LinkedBlockingQueue<RealtimeEventNotification> eventQueue = new LinkedBlockingQueue<>();

        NotificationResponse testNotification = new NotificationResponse();
        String testEventSET = EventNotificationTestConstants.SAMPLE_SET;
        JSONObject testPayload = new JSONObject("{\"notificationId\": " +
                EventNotificationTestConstants.SAMPLE_CLIENT_ID + ", \"SET\": " + testEventSET + "}");

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
