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
package com.wso2.openbanking.accelerator.throttler.dao.constants;

/**
 * This class contains all the constants needed for the ob throttler DAO layer.
 */
public class OBThrottlerDAOConstants {

    public static final int FIRST_OCCURRENCE = 1;
    public static final String LAST_UPDATE_TIMESTAMP = "LAST_UPDATE_TIMESTAMP";
    public static final String UNLOCK_TIMESTAMP = "UNLOCK_TIMESTAMP";
    public static final String OCCURRENCES = "OCCURRENCES";

    public static final String THROTTLE_DATA_STORE_ERROR_MSG = "Error occurred while persisting throttle data in the " +
            "database";
    public static final String THROTTLE_DATA_UPDATE_ERROR_MSG = "Error occurred while updating throttle data in the " +
            "database";
    public static final String THROTTLE_DATA_RETRIEVE_ERROR_MSG = "Error occurred while retrieving throttle data " +
            "from the database";
    public static final String THROTTLE_DATA_RESULT_SET_RETRIEVE_ERROR_MSG = "Error occurred while processing the " +
            "throttle data result set retrieval";
    public static final String THROTTLE_DATA_DELETE_ERROR_MSG = "Error occurred while deleting throttle data " +
            "from the database";
    public static final String NO_RECORDS_FOUND_ERROR_MSG = "No records are found for the given inputs";
}
