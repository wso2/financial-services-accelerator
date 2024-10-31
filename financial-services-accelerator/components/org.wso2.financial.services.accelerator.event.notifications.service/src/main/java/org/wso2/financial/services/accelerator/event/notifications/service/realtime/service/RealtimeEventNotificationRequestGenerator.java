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

import net.minidev.json.JSONObject;
import org.wso2.financial.services.accelerator.event.notifications.service.model.Notification;

import java.util.Map;

/**
 * Interface for event notification request metadata generation. For custom class extensions the class name
 * is to be referred from the realtime_event_notification_request_generator in deployment.toml
 */
public interface RealtimeEventNotificationRequestGenerator {
    /**
     * This method is to generate realtime event notification payload. To generate custom values
     * for the body this method should be extended.
     *
     * @return String payload
     */
    JSONObject getRealtimeEventNotificationPayload(Notification notificationDTO, String eventSET);

    /**
     * This method is to generate realtime event notification request headers. To generate custom values
     * for the body this method should be extended.
     *
     * @return Map<String, String> headers
     */
    Map<String, String> getAdditionalHeaders();
}
