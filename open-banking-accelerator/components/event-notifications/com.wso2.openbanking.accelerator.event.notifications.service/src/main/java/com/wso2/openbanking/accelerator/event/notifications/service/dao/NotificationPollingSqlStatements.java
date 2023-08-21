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
 * SQL queries to store and retrieve event notifications.
 */
public class NotificationPollingSqlStatements {

    public String getEventsByNotificationIdQuery() {

        return "SELECT * FROM OB_NOTIFICATION_EVENT WHERE NOTIFICATION_ID = ?";
    }

    public String getMaxNotificationsQuery() {

        return "SELECT * FROM OB_NOTIFICATION WHERE CLIENT_ID = ? AND STATUS = ? LIMIT ?";
    }

    public String getNotificationsCountQuery() {

        return "SELECT COUNT(*) AS NOTIFICATION_COUNT FROM OB_NOTIFICATION WHERE CLIENT_ID = ? AND STATUS = ?";
    }

    public String storeErrorNotificationQuery() {

        return "INSERT INTO OB_NOTIFICATION_ERROR (NOTIFICATION_ID, ERROR_CODE, DESCRIPTION) VALUES (?,?,?)";
    }

    public String updateNotificationStatusQueryById() {

        return "UPDATE OB_NOTIFICATION SET STATUS = ?, UPDATED_TIMESTAMP= ? WHERE NOTIFICATION_ID = ?";
    }

    public String getNotificationByNotificationId() {

        return "SELECT NOTIFICATION_ID, STATUS FROM OB_NOTIFICATION WHERE NOTIFICATION_ID = ?";
    }

    public String getNotificationsByState() {

        return "SELECT * FROM OB_NOTIFICATION WHERE STATUS = ?";
    }
}
