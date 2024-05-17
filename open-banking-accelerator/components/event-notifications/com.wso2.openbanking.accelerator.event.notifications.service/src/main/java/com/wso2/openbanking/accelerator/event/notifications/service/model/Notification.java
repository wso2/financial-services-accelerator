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

package com.wso2.openbanking.accelerator.event.notifications.service.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import net.minidev.json.JSONObject;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the notification model.
 */
public class Notification {
    private String iss = null;
    private Long iat = null;
    private String jti = null;
    private String sub = null;
    private String aud = null;
    private String txn = null;
    private Long toe = null;
    private Map<String, JSONObject> events = new HashMap<String, JSONObject>();

    public Long getIat() {
        return iat;
    }

    public void setIat(Long iat) {
        this.iat = iat;
    }

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getAud() {
        return aud;
    }

    public void setAud(String aud) {
        this.aud = aud;
    }

    public String getTxn() {
        return txn;
    }

    public void setTxn(String txn) {
        this.txn = txn;
    }

    public Long getToe() {
        return toe;
    }

    public void setToe(Long toe) {
        this.toe = toe;
    }

    public Map<String, JSONObject> getEvents() {
        return events;
    }

    public void setEvents(List<NotificationEvent> eventsList) {

        for (NotificationEvent notificationEvent : eventsList) {
            this.events.put(notificationEvent.getEventType(), notificationEvent.getEventInformation());
        }
    }

    public String getIss() {
        return iss;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    /**
     * This method is to convert the class to a JSONObject.
     * @param notification Notification
     * @return JSONObject
     * @throws IOException IOException when converting the class to JSONObject
     * @throws JOSEException JOSEException when converting the class to JSONObject
     * @throws IdentityOAuth2Exception IdentityOAuth2Exception when converting the class to JSONObject
     */
    public static JsonNode getJsonNode(Notification notification)
            throws IOException, JOSEException, IdentityOAuth2Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(notification, JsonNode.class);
    }

}
