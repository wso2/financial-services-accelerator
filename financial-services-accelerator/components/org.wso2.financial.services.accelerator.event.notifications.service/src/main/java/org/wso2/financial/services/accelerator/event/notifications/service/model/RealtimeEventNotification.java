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

package org.wso2.financial.services.accelerator.event.notifications.service.model;

import net.minidev.json.JSONObject;
import org.wso2.financial.services.accelerator.event.notifications.service.realtime.service.RealtimeEventNotificationRequestGenerator;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationServiceUtil;

/**
 * Model class for real time event notifications.
 */
public class RealtimeEventNotification {
    private String callbackUrl = null;
    private String securityEventToken = null; // Security Event Token to hold the Event Notification Data
    private Notification notification = null;

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public void setSecurityEventToken(String notification) {
        this.securityEventToken = notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public JSONObject getJsonPayload() {
        RealtimeEventNotificationRequestGenerator eventNotificationRequestGenerator =
                EventNotificationServiceUtil.getRealtimeEventNotificationRequestGenerator();
        return eventNotificationRequestGenerator
                .getRealtimeEventNotificationPayload(notification, securityEventToken);
    }

    public String getNotificationId() {
        return notification.getNotificationId();
    }

}
