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

package com.wso2.openbanking.accelerator.event.notifications.service.constants;

/**
 * Event Notification Constants.
 */
public class EventNotificationConstants {

    //Service level constants
    public static final String X_WSO2_CLIENT_ID = "x-wso2-client_id";

    //Event Notification Status
    public static final String ACK = "ACK";
    public static final String ERROR = "ERR";
    public static final String OPEN = "OPEN";

    //Response Status
    public static final String NOT_FOUND = "NOTFOUND";
    public static final String OK = "OK";
    public static final String CREATED = "CREATED";
    public static final String BAD_REQUEST = "BADREQUEST";
    public static final String NO_CONTENT = "NO_CONTENT";
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
    //Database columns
    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    public static final String CLIENT_ID = "CLIENT_ID";
    public static final String RESOURCE_ID = "RESOURCE_ID";
    public static final String STATUS = "STATUS";
    public static final String UPDATED_TIMESTAMP = "UPDATED_TIMESTAMP";
    public static final String EVENT_INFO = "EVENT_INFO";
    public static final String EVENT_TYPE = "EVENT_TYPE";
    public static final String SUBSCRIPTION_ID = "SUBSCRIPTION_ID";
    public static final String CALLBACK_URL = "CALLBACK_URL";
    public static final String TIME_STAMP = "TIMESTAMP";
    public static final String SPEC_VERSION = "SPEC_VERSION";
    public static final String REQUEST = "REQUEST";

    //Error Constants
    public static final String INVALID_REQUEST = "invalid_request";
    public static final String EVENT_NOTIFICATION_CREATION_ERROR = "Error occurred while saving event " +
            "notifications in the  database";
    public static final String MISSING_REQ_PAYLOAD = "No request payload found";
    public static final String MISSING_HEADER_PARAM_CLIENT_ID = "Missing header x-wso2-client_id";
    public static final String MISSING_HEADER_PARAM_RESOURCE_ID = "Missing header x-wso2-resource_id";
    public static final String ERROR_IN_EVENT_POLLING_REQUEST = "Error in event polling request";
    public static final String INVALID_CHARS_IN_HEADER_ERROR = "Invalid characters found in the request headers";

    //Polling request params
    public static final String SET_ERRORS = "setErrs";
    public static final String MAX_EVENTS = "maxEvents";
    public static final String DESCRIPTION = "description";
    public static final String RETURN_IMMEDIATELY = "returnImmediately";

    //Polling response params
    public static final String SETS = "sets";
    public static final String MORE_AVAILABLE = "moreAvailable";
    public static final String NOTIFICATIONS_ID = "notificationsID";

    // Event Subscription Request Params
    public static final String SUBSCRIPTION_ID_PARAM = "subscriptionId";
    public static final String CALLBACK_URL_PARAM = "callbackUrl";
    public static final String VERSION_PARAM = "version";
    public static final String EVENT_TYPE_PARAM = "eventTypes";
    public static final String DATA_PARAM = "data";

    public static final String DB_ERROR_UPDATING = "Database error while updating notification with ID : " +
            "'%s' in the database. ";
    public static final String DB_ERROR_NOTIFICATION_RETRIEVE = "Error occurred while retrieving" +
            " notifications for client ID : '%s'.";
    public static final String DB_FAILED_ERROR_NOTIFICATION_STORING = "Failed to store error notification with ID : ";
    public static final String DB_ERROR_STORING_ERROR_NOTIFICATION = "Error occurred while closing the " +
            "event-notification database connection";
    public static final String DB_ERROR_EVENTS_RETRIEVE = "Error occurred while retrieving events for" +
            " notifications ID : '%s'.";
    public static final String PARSE_ERROR_NOTIFICATION_ID = "Error occurred while parsing events for" +
            " notifications ID : '%s'.";
    public static final String DB_CONN_ESTABLISHED = "Database connection is established to get notification " +
            "for client ID : '%s' in the database. ";
    public static final String  RETRIEVED_NOTIFICATION_CLIENT = "Retrieved notification for client ID: '%s'. ";

    public static final String RETRIEVED_EVENTS_NOTIFICATION = "Retrieved events for notification ID: '%s'. ";
    public static final String NO_NOTIFICATIONS_FOUND_CLIENT = "No notifications found for client ID - '%s'";
    public static final String NO_EVENTS_NOTIFICATION_ID = "No events found for notification ID - '%s'";
    public static final String INVALID_CLIENT_ID = "Invalid mandatory parameter x-wso2-client-id.";
    public static final String DATABASE_CONNECTION_CLOSE_LOG_MSG = "Closing database connection";

    public static final String ERROR_STORING_EVENT_SUBSCRIPTION = "Error occurred while storing event " +
            "subscription in the database. ";
    public static final String ERROR_UPDATING_EVENT_SUBSCRIPTION = "Error occurred while updating event " +
            "subscription in the database. ";
    public static final String ERROR_RETRIEVING_EVENT_SUBSCRIPTION = "Error occurred while retrieving event " +
            "subscription in the database. ";
    public static final String ERROR_RETRIEVING_EVENT_SUBSCRIPTIONS = "Error occurred while retrieving event " +
            "subscriptions in the database.";
    public static final String ERROR_DELETING_EVENT_SUBSCRIPTION = "Error occurred while deleting event " +
            "subscription in the database. ";
    public static final String EVENT_SUBSCRIPTION_NOT_FOUND = "Event subscription not found.";
    public static final String EVENT_SUBSCRIPTIONS_NOT_FOUND = "Event subscriptions not found for the given client id.";
    public static final String ERROR_HANDLING_EVENT_SUBSCRIPTION = "Error occurred while handling the event " +
            "subscription  request";
}
