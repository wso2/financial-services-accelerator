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

package com.wso2.openbanking.accelerator.event.notifications.service.dto;

import net.minidev.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Event Creation DTO.
 */
public class NotificationCreationDTO {

    private Map<String, JSONObject> events = new HashMap<String, JSONObject>();
    private String clientId = null;
    private String resourceId = null;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public Map<String, JSONObject> getEventPayload() {
        return this.events;
    }

    public void setEventPayload(String notificationType, JSONObject notificationInfo) {
        this.events.put(notificationType, notificationInfo);
    }
}

