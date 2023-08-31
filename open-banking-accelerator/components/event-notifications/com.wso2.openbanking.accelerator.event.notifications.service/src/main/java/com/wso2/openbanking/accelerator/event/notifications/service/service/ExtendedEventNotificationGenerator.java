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

package com.wso2.openbanking.accelerator.event.notifications.service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.event.notifications.service.dto.NotificationDTO;
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.model.Notification;
import com.wso2.openbanking.accelerator.event.notifications.service.model.NotificationEvent;

import java.util.List;

/**
 * Custom Event Polling Generator Class.
 */
@Generated(message = "Extended Implementation excluded from tests")
public class ExtendedEventNotificationGenerator implements EventNotificationGenerator {

    @Override
    public Notification generateEventNotificationBody(NotificationDTO notificationDAO,
                              List<NotificationEvent> notificationEventList) throws OBEventNotificationException {
        return null;
    }

    @Override
    public String generateEventNotification(JsonNode jsonNode) {
        return null;
    }
}
