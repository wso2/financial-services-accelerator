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

package org.wso2.financial.services.accelerator.common.extension.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Model class to represent the external service response.
 */
public class ExternalServiceResponse {

    private String responseId;
    private StatusEnum status;
    private int errorCode;
    private JsonNode data;

    // No-Args Constructor (Required for Jackson)
    public ExternalServiceResponse() {

    }

    public ExternalServiceResponse(String responseId, StatusEnum status, JsonNode data) {
        this.responseId = responseId;
        this.status = status;
        this.data = data;
    }

    public ExternalServiceResponse(String responseId, StatusEnum status, int errorCode) {
        this.responseId = responseId;
        this.status = status;
        this.errorCode = errorCode;
    }

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public JsonNode getData() {
        return data;
    }

    public void setData(JsonNode data) {
        this.data = data;
    }
}
