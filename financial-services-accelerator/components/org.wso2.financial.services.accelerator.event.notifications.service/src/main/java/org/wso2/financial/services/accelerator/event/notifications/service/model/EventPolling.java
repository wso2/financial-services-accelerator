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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Event Polling model.
 */
public class EventPolling {

    private Boolean returnImmediately = true;
    private String clientId = null;
    private int maxEvents = 0;
    private int setsToReturn = 0;
    private List<String> ack = new ArrayList<String>();
    private Map<String, NotificationError> errors = new HashMap<String, NotificationError>();

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Boolean getReturnImmediately() {
        return returnImmediately;
    }

    public void setReturnImmediately(Boolean returnImmediately) {
        this.returnImmediately = returnImmediately;
    }

    public int getMaxEvents() {
        return maxEvents;
    }

    public void setMaxEvents(int maxEvents) {
        this.maxEvents = maxEvents;
    }

    public int getSetsToReturn() {
        return setsToReturn;
    }

    public void setSetsToReturn(int setsToReturn) {
        this.setsToReturn = setsToReturn;
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
