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

package org.wso2.financial.services.accelerator.event.notifications.endpoint.constants;

/**
 * Constants in Endpoint.
 */
public class EventNotificationEndPointConstants {
    public static final String X_WSO2_CLIENT_ID = "x-wso2-client-id";
    public static final String X_WSO2_RESOURCE_ID = "x-wso2-resource-id";
    public static final String REQUEST = "request";
    public static final String EVENT_CREATION_ERROR_RESPONSE = "Event Notification Creation error";
    public static final String REQUEST_PAYLOAD_ERROR = "Error in the request payload";
    public static final String EMPTY_REQ_PAYLOAD = "Request payload cannot be empty";
    public static final String INVALID_REQUEST = "invalid_request";
    public static final String INVALID_REQUEST_PAYLOAD = "invalid_request_payload";
    public static final String MISSING_REQUEST_PAYLOAD = "missing_request_payload";
    public static final String INVALID_REQUEST_HEADER = "invalid_request_header";
    public static final String MISSING_REQUEST_HEADER = "missing_request_header";
}
