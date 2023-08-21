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
package com.wso2.openbanking.accelerator.throttler.dao.queries;

/**
 * SQL Statements required for OB Throttler.
 */
public class OBThrottlerSQLStatements {

    public String storeThrottleData() {

        return "INSERT INTO OB_THROTTLE_DATA (THROTTLE_GROUP, THROTTLE_PARAM, LAST_UPDATE_TIMESTAMP, " +
                "UNLOCK_TIMESTAMP, OCCURRENCES) VALUES (?, ?, ?, ?, ?)";
    }

    public String updateThrottleData() {

        return "UPDATE OB_THROTTLE_DATA SET LAST_UPDATE_TIMESTAMP = ?, UNLOCK_TIMESTAMP = ?, OCCURRENCES = ? " +
                "WHERE THROTTLE_GROUP = ? AND THROTTLE_PARAM = ?";
    }

    public String retrieveThrottleData() {

        return "SELECT * FROM OB_THROTTLE_DATA WHERE THROTTLE_GROUP = ? AND THROTTLE_PARAM = ?";
    }

    public String removeThrottleData() {

        return "DELETE FROM OB_THROTTLE_DATA WHERE THROTTLE_GROUP = ? AND THROTTLE_PARAM = ?";
    }

    public String isThrottleDataExists() {

        return "SELECT COUNT(1) FROM OB_THROTTLE_DATA WHERE THROTTLE_GROUP = ? AND THROTTLE_PARAM = ?";
    }
}
