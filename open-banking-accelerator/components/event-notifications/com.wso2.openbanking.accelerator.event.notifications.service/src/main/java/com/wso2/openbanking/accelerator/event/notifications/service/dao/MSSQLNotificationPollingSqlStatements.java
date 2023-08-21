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

import com.wso2.openbanking.accelerator.common.util.Generated;

/**
 * MSSQL Queries for Event Polling.
 */
@Generated(message = "Returns sql statements to the dao method")
public class MSSQLNotificationPollingSqlStatements extends NotificationPollingSqlStatements {

    @Override
    public String getMaxNotificationsQuery() {

        return "SELECT * FROM OB_NOTIFICATION WHERE CLIENT_ID = ? AND STATUS = ? ORDER BY NOTIFICATION_ID " +
                "OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY";
    }
}
