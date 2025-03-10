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

/**
 * Model class to represent the external service request
 */
public class ExternalServiceRequest {

    private String requestId;
    private Request request;
    private String allowedOperation;

    public ExternalServiceRequest() {
    }

    public ExternalServiceRequest(String requestId, Request request, String allowedOperation) {
        this.requestId = requestId;
        this.request = request;
        this.allowedOperation = allowedOperation;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public String getAllowedOperation() {
        return allowedOperation;
    }

    public void setAllowedOperation(String allowedOperation) {
        this.allowedOperation = allowedOperation;
    }
}
