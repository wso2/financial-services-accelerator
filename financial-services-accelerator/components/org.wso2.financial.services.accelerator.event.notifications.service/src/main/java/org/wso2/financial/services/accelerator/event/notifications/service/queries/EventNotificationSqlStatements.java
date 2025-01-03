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

package org.wso2.financial.services.accelerator.event.notifications.service.queries;

/**
 * SQL queries to store and retrieve event notifications.
 */
public class EventNotificationSqlStatements {

    public String getStoreNotification() {

        return "INSERT INTO FS_NOTIFICATION (NOTIFICATION_ID, CLIENT_ID, RESOURCE_ID, STATUS, UPDATED_TIMESTAMP) " +
                "VALUES (?,?,?,?,?)";
    }

    public String getStoreNotificationEvents() {

        return "INSERT INTO FS_NOTIFICATION_EVENT (NOTIFICATION_ID, EVENT_TYPE, EVENT_INFO) VALUES (?,?,?)";
    }

    public String getEventsByNotificationIdQuery() {

        return "SELECT * FROM FS_NOTIFICATION_EVENT WHERE NOTIFICATION_ID = ?";
    }

    public String getMaxNotificationsQuery() {

        return "SELECT * FROM FS_NOTIFICATION WHERE CLIENT_ID = ? AND STATUS = ? LIMIT ?";
    }

    public String getNotificationsCountQuery() {

        return "SELECT COUNT(*) AS NOTIFICATION_COUNT FROM FS_NOTIFICATION WHERE CLIENT_ID = ? AND STATUS = ?";
    }

    public String storeErrorNotificationQuery() {

        return "INSERT INTO FS_NOTIFICATION_ERROR (NOTIFICATION_ID, ERROR_CODE, DESCRIPTION) VALUES (?,?,?)";
    }

    public String updateNotificationStatusQueryById() {

        return "UPDATE FS_NOTIFICATION SET STATUS = ?, UPDATED_TIMESTAMP= ? WHERE NOTIFICATION_ID = ?";
    }

    public String getNotificationByNotificationId() {

        return "SELECT NOTIFICATION_ID, STATUS FROM FS_NOTIFICATION WHERE NOTIFICATION_ID = ?";
    }

    public String getNotificationsByState() {

        return "SELECT * FROM FS_NOTIFICATION WHERE STATUS = ?";
    }
}
