package org.wso2.financial.services.accelerator.common.policy.utils;

import org.json.JSONObject;

import java.util.List;

/**
 * External Service Request.
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
        List<String> additionalHeaders;
        List<String> additionalParams;

        public EventRequest(JSONObject payload, List<String> additionalHeaders, List<String> additionalParams) {
            this.payload = payload;
            this.additionalHeaders = additionalHeaders;
            this.additionalParams = additionalParams;
        }

        public JSONObject getPayload() {
            return payload;
        }

        public void setPayload(JSONObject payload) {
            this.payload = payload;
        }

        public List<String> getAdditionalHeaders() {
            return additionalHeaders;
        }

        public void setAdditionalHeaders(List<String> additionalHeaders) {
            this.additionalHeaders = additionalHeaders;
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
