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

package org.wso2.financial.services.accelerator.event.notifications.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.model.Notification;
import org.wso2.financial.services.accelerator.event.notifications.service.model.NotificationEvent;
import org.wso2.financial.services.accelerator.event.notifications.service.model.NotificationResponse;

import java.util.List;

/**
 * Interface for event notification generation. For custom class extensions the class name
 * is to be referred from the event_notification_generator in deployment.toml
 */
public interface EventNotificationGenerator {

    /**
     * This method is to generate event notification body. To generate custom values
     * for the body this method should be extended.
     *
     * @param notification           Notification details
     * @param notificationEventList  List of notification events
     * @return Event Notification Response Body
     * @throws FSEventNotificationException  Exception when generating event notification body
     */
    NotificationResponse generateEventNotificationBody(Notification notification, List<NotificationEvent>
            notificationEventList) throws FSEventNotificationException;

    String generateEventNotification(JsonNode jsonNode) throws FSEventNotificationException;
}
