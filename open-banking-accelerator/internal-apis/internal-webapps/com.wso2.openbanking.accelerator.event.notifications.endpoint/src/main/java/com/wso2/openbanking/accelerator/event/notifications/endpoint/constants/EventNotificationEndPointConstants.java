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

package com.wso2.openbanking.accelerator.event.notifications.endpoint.constants;

/**
 * Constants in Endpoint.
 */
public class EventNotificationEndPointConstants {
    public static final String X_WSO2_CLIENT_ID = "x-wso2-client_id";
    public static final String X_WSO2_RESOURCE_ID = "x-wso2-resource_id";
    public static final String REQUEST = "request";
    public static final String NOT_FOUND_RESPONSE = "No OPEN notifications founds for the given clientID";
    public static final String POLLING_ERROR_RESPONSE = "OB Event Notification Polling error";
    public static final String EVENT_CREATION_ERROR_RESPONSE = "OB Event Notification Creation error";
    public static final String REQUEST_PAYLOAD_ERROR = "Error in the request payload";
    public static final String EMPTY_REQ_PAYLOAD = "Request payload cannot be empty";
    public static final String INVALID_REQUEST = "invalid_request";
    public static final String INVALID_REQUEST_PAYLOAD = "invalid_request_payload";
    public static final String MISSING_REQUEST_PAYLOAD = "missing_request_payload";
    public static final String MISSING_JSON_REQUEST_PAYLOAD = "missing_Json_request_payload";
    public static final String INVALID_REQUEST_HEADER = "invalid_request_header";
    public static final String MISSING_REQUEST_HEADER = "missing_request_header";
    public static final String ERROR_PAYLOAD_PARSE = "Error while parsing payload";
    public static final String NOTIFICATIONS_NOT_FOUND = "notification_not_found";
}
