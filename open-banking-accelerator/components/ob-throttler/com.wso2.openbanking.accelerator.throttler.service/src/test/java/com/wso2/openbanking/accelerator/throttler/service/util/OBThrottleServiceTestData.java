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
package com.wso2.openbanking.accelerator.throttler.service.util;

import com.wso2.openbanking.accelerator.throttler.dao.model.ThrottleDataModel;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Test data for Open Banking throttle service.
 */
public class OBThrottleServiceTestData {

    public static final Timestamp CURRENT_TIMESTAMP = new Timestamp(new Date().getTime());

    public static final Timestamp UNLOCK_TIMESTAMP_GREATER_THAN_CURRENT_TIMESTAMP =
            new Timestamp(CURRENT_TIMESTAMP.getTime() + (1000L * 180));

    public static final Timestamp UNLOCK_TIMESTAMP_LESS_THAN_CURRENT_TIMESTAMP =
            new Timestamp(CURRENT_TIMESTAMP.getTime() - (1000L * 180));

    public static final String THROTTLE_GROUP = "OBIdentifierAuthenticator";

    public static final String THROTTLE_SECOND_GROUP = "OBIdentifierAuthenticator-1";

    public static final String THROTTLE_GROUP_BASIC_AUTH = "BasicAuth";

    public static final String THROTTLE_PARAM = "user-ip-192.168.1.1";

    public static final String THROTTLE_SECOND_PARAM = "user-ip-192.168.1.1";

    public static ThrottleDataModel getSampleTestThrottleData() {

        ThrottleDataModel throttleDataModel = new ThrottleDataModel(THROTTLE_GROUP, THROTTLE_PARAM, CURRENT_TIMESTAMP,
                UNLOCK_TIMESTAMP_GREATER_THAN_CURRENT_TIMESTAMP, 1);
        return throttleDataModel;
    }

    public static ThrottleDataModel getSampleUpdateTestThrottleData() {

        ThrottleDataModel throttleDataModel = new ThrottleDataModel(THROTTLE_GROUP, THROTTLE_PARAM, CURRENT_TIMESTAMP,
                UNLOCK_TIMESTAMP_GREATER_THAN_CURRENT_TIMESTAMP, 5);

        return throttleDataModel;
    }

}
