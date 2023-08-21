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

import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationTestConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.response.EventCreationResponse;
import com.wso2.openbanking.accelerator.event.notifications.service.util.EventNotificationServiceUtil;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;

/**
 * Test class for EventNotificationPersistenceServiceHandler.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({EventNotificationServiceUtil.class, DefaultEventCreationServiceHandler.class})
public class EventNotificationPersistenceServiceHandlerTests {

    @Test
    public void testPersistRevokeEvent() {
        DefaultEventCreationServiceHandler defaultEventCreationServiceHandlerMock =
                Mockito.mock(DefaultEventCreationServiceHandler.class);

        PowerMockito.mockStatic(EventNotificationServiceUtil.class);
        PowerMockito.when(EventNotificationServiceUtil.getDefaultEventCreationServiceHandler())
                .thenReturn(defaultEventCreationServiceHandlerMock);

        EventCreationResponse eventCreationResponse = new EventCreationResponse();
        eventCreationResponse.setStatus(EventNotificationConstants.OK);
        doReturn(eventCreationResponse).when(defaultEventCreationServiceHandlerMock).publishOBEvent(any());



        EventNotificationPersistenceServiceHandler revokeEventPersistenceServiceHandler =
                EventNotificationPersistenceServiceHandler.getInstance();

        EventCreationResponse response =
                revokeEventPersistenceServiceHandler.persistRevokeEvent(
                        EventNotificationTestConstants.SAMPLE_CLIENT_ID,
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID,
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1, any());

        Assert.assertEquals(response.getStatus(), EventNotificationConstants.OK);
    }
}
