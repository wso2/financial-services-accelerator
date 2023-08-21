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
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;

/**
 * This class defines unit tests for DefaultRealtimeEventNotificationPayloadGenerator.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
public class DefaultRealtimeEventNotificationPayloadGeneratorTests {

    @Test
    public void testGetRealtimeEventNotificationPayload() {
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setClientId(EventNotificationTestConstants.SAMPLE_CLIENT_ID);
        notificationDTO.setNotificationId(EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);


        RealtimeEventNotificationRequestGenerator defaultRealtimeEventNotificationRequestGenerator
                = new DefaultRealtimeEventNotificationRequestGenerator();
        String result = defaultRealtimeEventNotificationRequestGenerator
                .getRealtimeEventNotificationPayload(notificationDTO,
                        EventNotificationTestConstants.SAMPLE_SET);

        Assert.assertEquals(result, EventNotificationTestConstants.SAMPLE_NOTIFICATION_PAYLOAD);
    }

    @Test
    public void testGetAdditionalHeaders() {
        RealtimeEventNotificationRequestGenerator defaultRealtimeEventNotificationRequestGenerator
                = new DefaultRealtimeEventNotificationRequestGenerator();
        HashMap<String, String> result = (HashMap<String, String>)
                defaultRealtimeEventNotificationRequestGenerator.getAdditionalHeaders();

        Assert.assertEquals(0, result.size());
    }

}
