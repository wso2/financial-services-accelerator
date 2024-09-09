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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model;

import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Data wrapper for consent manage data.
 */
public class ConsentManageData {

    private Map<String, String> headers;
    //Payload can either be a JSONObject or a JSONArray
    private Object payload;
    private Map queryParams;
    private String requestPath;
    private String clientId;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ResponseStatus responseStatus;
    private Object responsePayload;

    public ConsentManageData(Map<String, String> headers, Object payload, Map queryParams,
                             String requestPath, HttpServletRequest request, HttpServletResponse response) {
        this.headers = headers;
        this.payload = payload;
        this.queryParams = queryParams;
        this.requestPath = requestPath;
        this.request = request;
        this.response = response;
    }

    public ConsentManageData(Map<String, String> headers, Map queryParams,
                             String requestPath, HttpServletRequest request, HttpServletResponse response) {
        this.headers = headers;
        this.requestPath = requestPath;
        payload = null;
        this.queryParams = queryParams;
        this.request = request;
        this.response = response;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public Map getQueryParams() {
        return queryParams;
    }

    public Object getPayload() {
        return payload;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setResponseStatus(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    public void setResponsePayload(Object responsePayload) {
        this.responsePayload = responsePayload;
    }

    public Object getResponsePayload() {
        return responsePayload;
    }

    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public void setResponseHeader(String key, String value) {
        response.setHeader(key.replaceAll("\n\r", ""), value.replaceAll("\n\r", ""));
    }

    public void setResponseHeaders(Map<String, String> headers) {
        for (Map.Entry<String, String> header : headers.entrySet()) {
            setResponseHeader(header.getKey().replaceAll("\n\r", ""), header.getValue().replaceAll("\n\r", ""));
        }
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
}
