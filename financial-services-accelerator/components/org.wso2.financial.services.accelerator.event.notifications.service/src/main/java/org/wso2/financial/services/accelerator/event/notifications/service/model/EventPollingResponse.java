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

import org.json.JSONObject;

/**
 * This class is used to map the Event Polling service response to the API response.
 */
public class EventPollingResponse {

    private String status;
    private JSONObject responseBody;
    private JSONObject errorResponse;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public JSONObject getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(JSONObject responseBody) {
        this.responseBody = responseBody;
    }

    public JSONObject getErrorResponse() {
        return errorResponse;
    }

    public void setErrorResponse(JSONObject errorResponse) {
        this.errorResponse = errorResponse;
    }
}
