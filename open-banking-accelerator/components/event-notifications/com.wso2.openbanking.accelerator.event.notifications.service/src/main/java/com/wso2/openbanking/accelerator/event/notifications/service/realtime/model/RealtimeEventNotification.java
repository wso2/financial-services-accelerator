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

package com.wso2.openbanking.accelerator.event.notifications.service.realtime.model;

import com.wso2.openbanking.accelerator.event.notifications.service.dto.NotificationDTO;
import com.wso2.openbanking.accelerator.event.notifications.service.realtime.service.RealtimeEventNotificationRequestGenerator;
import com.wso2.openbanking.accelerator.event.notifications.service.util.EventNotificationServiceUtil;

/**
 * Model class for real time event notifications.
 */
public class RealtimeEventNotification {
    private String callbackUrl = null;
    private String eventSET = null; // Security Event Token to hold the Event Notification Data
    private NotificationDTO notificationDTO = null;

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public void setEventSET(String notification) {
        this.eventSET = notification;
    }

    public void setNotificationDTO(NotificationDTO notificationDTO) {
        this.notificationDTO = notificationDTO;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public String getJsonPayload() {
        RealtimeEventNotificationRequestGenerator eventNotificationRequestGenerator =
                EventNotificationServiceUtil.getRealtimeEventNotificationRequestGenerator();
        return eventNotificationRequestGenerator.getRealtimeEventNotificationPayload(notificationDTO, eventSET);
    }

    public String getNotificationId() {
        return notificationDTO.getNotificationId();
    }

}
