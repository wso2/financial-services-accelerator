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

package org.wso2.financial.services.accelerator.event.notifications.service.util;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationTestConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.dto.EventSubscriptionDTO;
import org.wso2.financial.services.accelerator.event.notifications.service.dto.NotificationCreationDTO;
import org.wso2.financial.services.accelerator.event.notifications.service.model.AggregatedPollingResponse;
import org.wso2.financial.services.accelerator.event.notifications.service.model.EventPolling;
import org.wso2.financial.services.accelerator.event.notifications.service.model.EventSubscription;
import org.wso2.financial.services.accelerator.event.notifications.service.model.Notification;
import org.wso2.financial.services.accelerator.event.notifications.service.model.NotificationError;
import org.wso2.financial.services.accelerator.event.notifications.service.model.NotificationEvent;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * EventNotification Test Utils class.
 */
public class EventNotificationTestUtils {

    private static final Map<String, BasicDataSource> dataSourceMap = new HashMap<>();

    public static List<Notification> getSampleSavedTestNotification() {

        List<Notification> notificationList = new ArrayList<>();
        notificationList.add(getSampleNotificationDTO(UUID.randomUUID().toString()));

        return notificationList;
    }
    public static Notification getSampleNotificationDTO(String notificationId) {

        Notification notification = new Notification();
        notification.setResourceId(EventNotificationTestConstants.SAMPLE_RESOURCE_ID);
        notification.setNotificationId(notificationId);
        notification.setStatus(EventNotificationConstants.OPEN);
        notification.setUpdatedTimeStamp(EventNotificationTestConstants.UPDATED_TIME);
        notification.setClientId(EventNotificationTestConstants.SAMPLE_CLIENT_ID);

        return notification;
    }
    public static List<NotificationEvent> getSampleNotificationsList() {
        List<NotificationEvent> eventsList = new ArrayList<NotificationEvent>();
        NotificationEvent notificationEvent = new NotificationEvent();
        notificationEvent.setEventType(EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1);
        notificationEvent.setEventInformation(getSampleEventInformation());
        eventsList.add(notificationEvent);

        return eventsList;
    }

    public static List<EventSubscription> getEventSubscrptionList() {
        EventSubscription eventSubscription = new EventSubscription();
        eventSubscription.setCallbackUrl(EventNotificationTestConstants.SAMPLE_CALLBACK_URL);
        eventSubscription.setEventTypes(getSampleEventTypeList());
        List<EventSubscription> eventSubscriptions = new ArrayList<>();
        eventSubscriptions.add(eventSubscription);
        return eventSubscriptions;
    }

    public static List<String> getSampleEventTypeList() {
        List<String> eventsTypes = new ArrayList<>();
        eventsTypes.add(EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1);

        return eventsTypes;
    }

    public static JSONObject getEventRequest() {

        JSONObject sampleEventPollingRequest = new JSONObject();
        sampleEventPollingRequest.put(EventNotificationConstants.X_WSO2_CLIENT_ID,
                EventNotificationTestConstants.SAMPLE_CLIENT_ID);
        sampleEventPollingRequest.put(EventNotificationConstants.RETURN_IMMEDIATELY, true);
        sampleEventPollingRequest.put(EventNotificationConstants.MAX_EVENTS, 5);
        sampleEventPollingRequest.put(EventNotificationConstants.ACK,
                EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);
        return sampleEventPollingRequest;
    }

    public static JSONObject getSampleEventInformation() {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key1", "value1");
        jsonObject.put("key2", "value2");
        jsonObject.put("key3", "value3");

        return jsonObject;
    }

    public static AggregatedPollingResponse getAggregatedPollingResponse() {

        Map<String, String> sets = new HashMap<>();
        sets.put("4f312007-4d3f-40e4-a525-0f6ee8bb54d9", EventNotificationTestConstants.SAMPLE_SET);
        AggregatedPollingResponse aggregatedPollingResponse = new AggregatedPollingResponse();
        aggregatedPollingResponse.setCount(0);
        aggregatedPollingResponse.setStatus("OK");
        aggregatedPollingResponse.setSets(sets);
        return aggregatedPollingResponse;
    }

    public static JSONObject getPollingError() {

        JSONObject errorObj = new JSONObject();
        errorObj.put("65ac7453-13b0-4d2f-9946-dff6e6089a4f", errorInfo());
        errorObj.put("78ac7453-13b0-4d2f-9946-dff6e608345f", errorInfo());
        return errorObj;
    }

    public static JSONObject errorInfo() {

        JSONObject errorInfo = new JSONObject();
        errorInfo.put("err", "authentication_failed");
        errorInfo.put("description", "The SET could not be authenticated");
        return errorInfo;
    }

    public static NotificationCreationDTO getNotificationCreationDTO() {

        NotificationCreationDTO notificationCreationDTO = new NotificationCreationDTO();
        notificationCreationDTO.setClientId(EventNotificationTestConstants.SAMPLE_CLIENT_ID);
        notificationCreationDTO.setEventPayload(EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1,
                getSampleEventInformation());
        notificationCreationDTO.setResourceId(EventNotificationTestConstants.SAMPLE_RESOURCE_ID);

        return notificationCreationDTO;
    }

    public static NotificationError getNotificationError(String notificationId) {

        NotificationError notificationError = new NotificationError();
        notificationError.setNotificationId(notificationId);
        notificationError.setErrorCode(EventNotificationTestConstants.ERROR_CODE);
        notificationError.setErrorDescription(EventNotificationTestConstants.ERROR_DESCRIPTION);

        return notificationError;
    }

    public static EventPolling getEventPollingDTO() {

        String notificationId = "d3fcb77a-274d-4851-b392-a2c0af312fd8";
        EventPolling eventPolling = new EventPolling();
        eventPolling.setMaxEvents(3);
        eventPolling.setClientId(EventNotificationTestConstants.SAMPLE_CLIENT_ID);
        eventPolling.setErrors(notificationId, getNotificationError(notificationId));
        eventPolling.setAck(EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);

        return eventPolling;
    }

    public static ArrayList<NotificationEvent> getSampleEventList() {
        ArrayList<NotificationEvent> eventsList = new ArrayList<NotificationEvent>();
        NotificationEvent notificationEvent = new NotificationEvent();
        notificationEvent.setEventType(EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1);
        notificationEvent.setEventInformation(getSampleEventInformation());
        eventsList.add(notificationEvent);

        return eventsList;
    }

    public static EventSubscription getSampleEventSubscription() {

        EventSubscription eventSubscription = new EventSubscription();
        eventSubscription.setClientId(EventNotificationTestConstants.SAMPLE_CLIENT_ID);
        eventSubscription.setCallbackUrl(EventNotificationTestConstants.SAMPLE_CALLBACK_URL);
        eventSubscription.setSpecVersion(EventNotificationTestConstants.SAMPLE_SPEC_VERSION);
        eventSubscription.setEventTypes(EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPES);
        eventSubscription.setRequestData(EventNotificationTestConstants.SUBSCRIPTION_PAYLOAD);
        eventSubscription.setStatus(EventNotificationTestConstants.SUBSCRIPTION_STATUS);
        return eventSubscription;
    }

    public static EventSubscription getSampleStoredEventSubscription() {
        EventSubscription eventSubscription = new EventSubscription();
        eventSubscription.setSubscriptionId(EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);
        eventSubscription.setClientId(EventNotificationTestConstants.SAMPLE_CLIENT_ID);
        eventSubscription.setCallbackUrl(EventNotificationTestConstants.SAMPLE_CALLBACK_URL);
        eventSubscription.setSpecVersion(EventNotificationTestConstants.SAMPLE_SPEC_VERSION);
        eventSubscription.setTimeStamp(1626480000L);
        eventSubscription.setEventTypes(EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPES);
        eventSubscription.setStatus("CREATED");

        JSONObject requestData = new JSONObject();
        requestData.put(EventNotificationConstants.SUBSCRIPTION_ID_PARAM,
                EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);
        requestData.put("clientId", EventNotificationTestConstants.SAMPLE_CLIENT_ID);
        requestData.put(EventNotificationConstants.CALLBACK_URL_PARAM,
                EventNotificationTestConstants.SAMPLE_CALLBACK_URL);
        requestData.put(EventNotificationConstants.VERSION_PARAM, EventNotificationTestConstants.SAMPLE_SPEC_VERSION);
        requestData.put(EventNotificationConstants.EVENT_TYPES_PARAM,
                EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPES);
        requestData.put("timeStamp", 1626480000L);
        requestData.put("status", "CREATED");
        eventSubscription.setRequestData(requestData.toString());
        return eventSubscription;
    }

    public static EventSubscription getSampleStoredEventSubscription2() {
        EventSubscription eventSubscription = new EventSubscription();
        eventSubscription.setSubscriptionId(EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_2);
        eventSubscription.setClientId(EventNotificationTestConstants.SAMPLE_CLIENT_ID);
        eventSubscription.setCallbackUrl(EventNotificationTestConstants.SAMPLE_CALLBACK_URL);
        eventSubscription.setSpecVersion(EventNotificationTestConstants.SAMPLE_SPEC_VERSION);
        eventSubscription.setEventTypes(EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPES);
        eventSubscription.setStatus("CREATED");
        return eventSubscription;
    }

    public static List<EventSubscription> getSampleStoredEventSubscriptions() {
        List<EventSubscription> eventSubscriptions = new ArrayList<>();
        eventSubscriptions.add(getSampleStoredEventSubscription());
        eventSubscriptions.add(getSampleStoredEventSubscription2());
        return eventSubscriptions;
    }

    public static EventSubscription getSampleEventSubscriptionToBeUpdated(String subscriptionId) {
        EventSubscription eventSubscription = new EventSubscription();
        eventSubscription.setSubscriptionId(subscriptionId);
        eventSubscription.setCallbackUrl("test.com");
        eventSubscription.setEventTypes(EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPES);
        eventSubscription.setStatus("created");
        eventSubscription.setClientId(EventNotificationTestConstants.SAMPLE_CLIENT_ID);

        JSONObject requestData = new JSONObject();
        requestData.put(EventNotificationConstants.SUBSCRIPTION_ID_PARAM,
                EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);
        requestData.put(EventNotificationConstants.CALLBACK_URL_PARAM, "test.com");
        requestData.put(EventNotificationConstants.EVENT_TYPES_PARAM,
                EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPES);
        eventSubscription.setRequestData(requestData.toString());
        return eventSubscription;
    }

    public static List<String> getSampleStoredEventTypes() {
        return EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPES;
    }

    public static EventSubscriptionDTO getSampleEventSubscriptionDTO() {
        EventSubscriptionDTO eventSubscriptionDTO = new EventSubscriptionDTO();
        JSONObject request = new JSONObject();
        request.put("callbackUrl", EventNotificationTestConstants.SAMPLE_CALLBACK_URL);
        request.put("eventTypes", EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPES);
        request.put("version", EventNotificationTestConstants.SAMPLE_SPEC_VERSION);
        eventSubscriptionDTO.setClientId(EventNotificationTestConstants.SAMPLE_CLIENT_ID);
        eventSubscriptionDTO.setSubscriptionId(EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);
        eventSubscriptionDTO.setRequestData(request.toString());
        return eventSubscriptionDTO;
    }

    public static EventSubscriptionDTO getSampleEventSubscriptionUpdateDTO() {
        EventSubscriptionDTO eventSubscriptionDTO = new EventSubscriptionDTO();
        List<String> eventTypes = Arrays.asList("event 1", "event 2");
        JSONObject request = new JSONObject();
        request.put("callbackUrl", "updated url");
        request.put("eventTypes", eventTypes);
        eventSubscriptionDTO.setClientId(EventNotificationTestConstants.SAMPLE_CLIENT_ID);
        eventSubscriptionDTO.setSubscriptionId(EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);
        eventSubscriptionDTO.setRequestData(request.toString());
        return eventSubscriptionDTO;
    }

    public static void initializeDataSource(String databaseName, String scriptPath) throws Exception {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:h2:mem:" + databaseName);

        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().executeUpdate("RUNSCRIPT FROM '" + scriptPath + "'");
        }
        dataSourceMap.put(databaseName, dataSource);
    }

    public static Connection getConnection(String database) throws SQLException {
        if (dataSourceMap.get(database) != null) {
            return dataSourceMap.get(database).getConnection();
        }
        throw new RuntimeException("Invalid datasource.");
    }

    public static String getFilePath(String fileName) {
        if (StringUtils.isNotBlank(fileName)) {
            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", fileName)
                    .toString();
        }
        return null;
    }
}
