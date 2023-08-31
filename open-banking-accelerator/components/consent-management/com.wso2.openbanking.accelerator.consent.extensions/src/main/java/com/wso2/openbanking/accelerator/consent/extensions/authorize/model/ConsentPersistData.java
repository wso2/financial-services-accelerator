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

package com.wso2.openbanking.accelerator.consent.extensions.authorize.model;

import net.minidev.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Data wrapper for consent persist flow data.
 */
public class ConsentPersistData {

    //Payload of the persist request
    private JSONObject payload;
    //Request headers of the persist request
    private Map<String, String> headers;
    //Consent data object used in the retrieval flow populated via a cache
    private ConsentData consentData;
    //Boolean value representing whether the approval was granted by the user
    private boolean approval;
    //Additional metadata
    private Map<String, Object> metadata;
    //Browser cookies
    private Map<String, String> browserCookies;

    public ConsentPersistData(JSONObject payload, Map<String, String> headers, boolean approval,
                              ConsentData consentData) {
        this.payload = payload;
        this.headers = headers;
        this.approval = approval;
        this.consentData = consentData;
        metadata = new HashMap<>();
        browserCookies = new HashMap<>();
    }

    public JSONObject getPayload() {
        return payload;
    }

    public void setPayload(JSONObject payload) {
        this.payload = payload;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public boolean getApproval() {
        return approval;
    }

    public void setApproval(boolean approval) {
        this.approval = approval;
    }

    public ConsentData getConsentData() {
        return consentData;
    }

    public void setConsentData(ConsentData consentData) {
        this.consentData = consentData;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void addMetadata(String key, Object value) {

        this.metadata.put(key, value);
    }

    public void addMultipleMetadata(Map<String, Object> data) {

        this.metadata.putAll(data);
    }

    public Map<String, String> getBrowserCookies() {

        return browserCookies;
    }

    public void setBrowserCookies(Map<String, String> cookies) {

        this.browserCookies.putAll(cookies);
    }
}
