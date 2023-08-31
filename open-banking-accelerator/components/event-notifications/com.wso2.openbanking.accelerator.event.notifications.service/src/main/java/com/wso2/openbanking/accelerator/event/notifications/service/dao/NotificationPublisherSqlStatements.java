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

package com.wso2.openbanking.accelerator.event.notifications.service.dao;

/**
 * SQL statements to store event notifications.
 */
public class NotificationPublisherSqlStatements {

    public String getStoreNotification() {

        final String storeNotifications = "INSERT INTO OB_NOTIFICATION (NOTIFICATION_ID, CLIENT_ID, " +
                "RESOURCE_ID, STATUS) VALUES (?,?,?,?)";
        return storeNotifications;
    }

    public String getStoreNotificationEvents() {

        final String storeNotificationEvents =
                "INSERT INTO OB_NOTIFICATION_EVENT (NOTIFICATION_ID, EVENT_TYPE, EVENT_INFO) VALUES (?,?,?)";
        return storeNotificationEvents;
    }

}
