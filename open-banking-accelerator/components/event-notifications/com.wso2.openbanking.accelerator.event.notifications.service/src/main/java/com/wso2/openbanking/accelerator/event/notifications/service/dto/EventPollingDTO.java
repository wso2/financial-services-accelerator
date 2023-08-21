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

import com.wso2.openbanking.accelerator.event.notifications.service.model.NotificationError;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Event Polling DTO.
 */
public class EventPollingDTO {

    //Set to true by default as WSO2 Open Banking don't support long polling
    private final Boolean returnImmediately = true;
    private String clientId = null;
    private int maxEvents = 0;
    private List<String> ack = new ArrayList<>();
    private Map<String, NotificationError> errors = new HashMap<>();

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Boolean getReturnImmediately() {
        return returnImmediately;
    }

    public int getMaxEvents() {
        return maxEvents;
    }

    public void setMaxEvents(int maxEvents) {
        this.maxEvents = maxEvents;
    }

    public List<String> getAck() {
        return ack;
    }

    public void setAck(String ack) {
        this.ack.add(ack);
    }

    public Map<String, NotificationError> getErrors() {
        return errors;
    }

    public void setErrors(String notificationId, NotificationError errorNotification) {
        this.errors.put(notificationId, errorNotification);
    }
}
