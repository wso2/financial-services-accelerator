/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.is.test.event.notifications.utils;

/**
 * Event Notification test Constants
 */
class EventNotificationConstants {

    final static String URL_EVENT_NOTIFICATION = "/api/fs/event-notifications"
    final static String URL_EVENT_CREATE = URL_EVENT_NOTIFICATION + "/create-events"
    final static String URL_EVENT_POLLING = URL_EVENT_NOTIFICATION + "/events"
    final static String URL_EVENT_SUBSCRIPTION = URL_EVENT_NOTIFICATION + "/subscription"
    final static String URL_EVENT_SUBSCRIPTION_BY_EVENT_TYPE = "/type/" + RESOURCE_UPDATE_EVENT_TYPE

    //Event Creation Constants
    final static String X_WSO2_RESOURCE_ID = "x-wso2-resource-id"
    final static String NOTIFICATION_ID = "notificationsID"

    // Event Polling Constants
    final static Boolean SHORT_POLLING = true
    final static Integer RETURN_MORE_EVENT_NOTIFICATIONS = 2
    final static Integer RETURN_NO_EVENT_NOTIFICATIONS = 0
    final static String SETS = "sets"
    final static String MORE_AVAILABLE = "moreAvailable"

    // Event Subscription Constants
    final static String CALLBACK_URL_CREATE = "https://www.tpp.com/v3.1/event-notifications"
    final static String CALLBACK_URL_UPDATE = "https://www.updatedtppdomain.com/v3.1/event-notifications"
    final static String VERSION = "3.1"
    final static String CONSENT_AUTHORIZATION_REVOKED_EVENT_TYPE = "urn_uk_org_openbanking_events_consent-authorization-revoked"
    final static String RESOURCE_UPDATE_EVENT_TYPE = "urn_uk_org_openbanking_events_resource-update"
    final static String CREATE_EVENT_TYPE = "urn:ietf:params:scim:event:create"
    final static String PATH_EVENT_SUBSCRIPTION_ID = "subscriptionId"
    final static String PATH_CALLBACK_URL = "callbackUrl"
    final static String PATH_VERSION = "version"
    final static String PATH_EVENT_TYPES = "eventTypes"

    // ErrorConstants
    final static String ERROR = "error"
    final static String ERROR_DESCRIPTION = "error_description"
    final static String MISSING_REQUEST_PAYLOAD = "missing_request_payload"
    final static String MISSING_REQUEST_HEADER = "missing_request_header"
    final static String INVALID_REQUEST_PAYLOAD = "invalid_request_payload"
}
