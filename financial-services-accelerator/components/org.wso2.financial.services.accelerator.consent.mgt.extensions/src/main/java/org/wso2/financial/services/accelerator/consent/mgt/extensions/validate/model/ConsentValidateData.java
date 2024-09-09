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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.model;

import org.json.JSONObject;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;

import java.util.Map;
import java.util.TreeMap;

/**
 * Data wrapper for consent validate data.
 */
public class ConsentValidateData {

    private JSONObject headers;
    private JSONObject payload;
    private String requestPath;
    private String consentId;
    private String userId;
    private String clientId;
    private Map<String, String> resourceParams;
    private DetailedConsentResource comprehensiveConsent;
    private TreeMap<String, String> headersMap;

    public ConsentValidateData(JSONObject headers, JSONObject payload, String requestPath, String consentId,
                               String userId, String clientId, Map<String, String> resourceParams) {
        this.headers = headers;
        this.payload = payload;
        this.requestPath = requestPath;
        this.consentId = consentId;
        this.userId = userId;
        this.clientId = clientId;
        this.resourceParams = resourceParams;
    }

    public ConsentValidateData(JSONObject headers, JSONObject payload, String requestPath, String consentId,
                               String userId, String clientId, Map<String, String> resourceParams,
                               TreeMap<String, String> headersMap) {
        this.headers = headers;
        this.payload = payload;
        this.requestPath = requestPath;
        this.consentId = consentId;
        this.userId = userId;
        this.clientId = clientId;
        this.resourceParams = resourceParams;
        this.headersMap = headersMap;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public JSONObject getPayload() {
        return payload;
    }

    public JSONObject getHeaders() {
        return headers;
    }

    public DetailedConsentResource getComprehensiveConsent() {
        return comprehensiveConsent;
    }

    public void setComprehensiveConsent(DetailedConsentResource comprehensiveConsent) {
        this.comprehensiveConsent = comprehensiveConsent;
    }

    public String getConsentId() {
        return consentId;
    }

    public void setConsentId(String consentId) {
        this.consentId = consentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Map<String, String> getResourceParams() {
        return resourceParams;
    }

    public void setResourceParams(Map<String, String> resourceParams) {
        this.resourceParams = resourceParams;
    }

    public TreeMap<String, String> getHeadersMap() {
        return headersMap;
    }

    private void setHeadersMap(TreeMap<String, String> headersMap) {
        this.headersMap = headersMap;
    }
}
