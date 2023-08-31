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
package com.wso2.openbanking.accelerator.identity.dcr.endpoint.impl.model;

import com.wso2.openbanking.accelerator.identity.dcr.endpoint.impl.util.ResponseStatus;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Model class for DCR request data.
 */
public class DCRRequestData {

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

    public HttpServletRequest getRequest() {

        return request;
    }

    public void setRequest(HttpServletRequest request) {

        this.request = request;
    }

    public HttpServletResponse getResponse() {

        return response;
    }

    public void setResponse(HttpServletResponse response) {

        this.response = response;
    }

    public ResponseStatus getResponseStatus() {

        return responseStatus;
    }

    public void setResponseStatus(ResponseStatus responseStatus) {

        this.responseStatus = responseStatus;
    }

    public Object getResponsePayload() {

        return responsePayload;
    }

    public void setResponsePayload(Object responsePayload) {

        this.responsePayload = responsePayload;
    }

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

    public DCRRequestData(Map<String, String> headers, Object payload, Map queryParams,
                          String requestPath, HttpServletRequest request, HttpServletResponse response) {

        this.headers = headers;
        this.payload = payload;
        this.queryParams = queryParams;
        this.requestPath = requestPath;
        this.request = request;
        this.response = response;
    }

}
