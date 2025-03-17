/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model;

import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;

import java.util.Map;

/**
 * ConsentManageExternalRequestDTO
 */
public class ConsentManageExternalRequestDTO {

    private Map<String, String> headers;
    //Payload can either be a JSONObject or a JSONArray
    private Object payload;
    private Map queryParams;
    private String requestPath;
    private String clientId;
    private ConsentResource consentResource;

    public ConsentManageExternalRequestDTO(ConsentManageData consentManageData) {
        this.headers = consentManageData.getHeaders();
        this.payload = consentManageData.getPayload();
        this.queryParams = consentManageData.getQueryParams();
        this.requestPath = consentManageData.getRequestPath();
        this.clientId = consentManageData.getClientId();
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public Map getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Map queryParams) {
        this.queryParams = queryParams;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public ConsentResource getConsentResource() {
        return consentResource;
    }

    public void setConsentResource(
            ConsentResource consentResource) {
        this.consentResource = consentResource;
    }
}
