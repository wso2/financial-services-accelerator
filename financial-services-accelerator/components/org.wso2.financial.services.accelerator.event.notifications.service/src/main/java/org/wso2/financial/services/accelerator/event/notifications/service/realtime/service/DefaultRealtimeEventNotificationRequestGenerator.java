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

package org.wso2.financial.services.accelerator.event.notifications.service.realtime.service;

import org.json.JSONObject;
import org.wso2.financial.services.accelerator.event.notifications.service.model.Notification;

import java.util.HashMap;
import java.util.Map;

/**
 * Default class for realtime event notification request generation.
 * This is to generate the realtime event notification request payload and headers.
 */
public class DefaultRealtimeEventNotificationRequestGenerator implements RealtimeEventNotificationRequestGenerator {

    @Override
    public JSONObject getRealtimeEventNotificationPayload(Notification notificationDTO, String eventSET) {
        return new JSONObject("{\"notificationId\": " + notificationDTO.getNotificationId() + ", \"SET\": "
                + eventSET + "}");
    }

    @Override
    public Map<String, String> getAdditionalHeaders() {
        return new HashMap<>();
    }
}
