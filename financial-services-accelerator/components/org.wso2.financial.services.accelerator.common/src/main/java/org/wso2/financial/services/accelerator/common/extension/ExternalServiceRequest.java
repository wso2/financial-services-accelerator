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

package org.wso2.financial.services.accelerator.common.extension;

import org.json.JSONObject;

import java.util.List;

/**
 * Model class to represent the external service request
 */
public class ExternalServiceRequest {

    String eventId;
    Event event;
    AllowedOperation allowedOperation;

    public ExternalServiceRequest(String eventId, Event event, AllowedOperation allowedOperation) {
        this.eventId = eventId;
        this.event = event;
        this.allowedOperation = allowedOperation;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public AllowedOperation getAllowedOperation() {
        return allowedOperation;
    }

    public void setAllowedOperation(AllowedOperation allowedOperation) {
        this.allowedOperation = allowedOperation;
    }

    /**
     * Event.
     */
    public static class Event {

        EventRequest request;

        public Event(EventRequest request) {
            this.request = request;
        }

        public EventRequest getRequest() {
            return request;
        }

        public void setRequest(EventRequest request) {
            this.request = request;
        }
    }

    /**
     * Event Request.
     */
    public static class EventRequest {

        JSONObject payload;
        List<String> additionalParams;

        public EventRequest(JSONObject payload, List<String> additionalHeaders, List<String> additionalParams) {
            this.payload = payload;
            this.additionalParams = additionalParams;
        }

        public JSONObject getPayload() {
            return payload;
        }

        public void setPayload(JSONObject payload) {
            this.payload = payload;
        }

        public List<String> getAdditionalParams() {
            return additionalParams;
        }

        public void setAdditionalParams(List<String> additionalParams) {
            this.additionalParams = additionalParams;
        }
    }

    /**
     * AllowedOperation.
     */
    public static class AllowedOperation {
        String op;

        public AllowedOperation(String op) {
            this.op = op;
        }

        public String getOp() {
            return op;
        }

        public void setOp(String op) {
            this.op = op;
        }
    }

}
