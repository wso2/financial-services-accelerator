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

package org.wso2.financial.services.accelerator.event.notifications.service.realtime;

import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationTestConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.model.Notification;
import org.wso2.financial.services.accelerator.event.notifications.service.realtime.service.DefaultRealtimeEventNotificationRequestGenerator;
import org.wso2.financial.services.accelerator.event.notifications.service.realtime.service.RealtimeEventNotificationRequestGenerator;

import java.util.HashMap;

/**
 * This class defines unit tests for DefaultRealtimeEventNotificationPayloadGenerator.
 */
public class DefaultRealtimeEventNotificationPayloadGeneratorTests {

    @Test
    public void testGetRealtimeEventNotificationPayload() {
        Notification notification = new Notification();
        notification.setClientId(EventNotificationTestConstants.SAMPLE_CLIENT_ID);
        notification.setNotificationId(EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);

        DefaultRealtimeEventNotificationRequestGenerator defaultRealtimeEventNotificationRequestGenerator
                = new DefaultRealtimeEventNotificationRequestGenerator();
        JSONObject result = defaultRealtimeEventNotificationRequestGenerator
                .getRealtimeEventNotificationPayload(notification,
                        EventNotificationTestConstants.SAMPLE_SET);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getString("notificationId"),
                EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);
    }

    @Test
    public void testGetAdditionalHeaders() {
        RealtimeEventNotificationRequestGenerator defaultRealtimeEventNotificationRequestGenerator
                = new DefaultRealtimeEventNotificationRequestGenerator();
        HashMap<String, String> result = (HashMap<String, String>)
                defaultRealtimeEventNotificationRequestGenerator.getAdditionalHeaders();

        Assert.assertEquals(result.size(), 0);
    }

}
