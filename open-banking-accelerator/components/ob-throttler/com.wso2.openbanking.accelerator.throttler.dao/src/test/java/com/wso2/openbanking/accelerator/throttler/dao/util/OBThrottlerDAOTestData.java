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
package com.wso2.openbanking.accelerator.throttler.dao.util;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Test data for Open Banking throttler DAO.
 */
public class OBThrottlerDAOTestData {

    public static final String THROTTLE_GROUP = "OBIdentifierAuthenticator";

    public static final String THROTTLE_PARAM = "user-ip-192.168.1.1";

    public static final Timestamp CURRENT_TIMESTAMP = new Timestamp(new Date().getTime());

    public static final Timestamp UNLOCK_TIMESTAMP = new Timestamp(CURRENT_TIMESTAMP.getTime() + (1000L * 180));

    public static final int OCCURRENCES = 1;

    /**
     * Data provider.
     */
    public static final class DataProviders {

        /*
         * throttleGroup
         * throttleParam
         * currentTimestamp
         * unlockTimestamp
         * occurrences
         */
        public static final Object[][] OB_THROTTLER_DATA_HOLDER = new Object[][]{

                {
                        THROTTLE_GROUP,
                        THROTTLE_PARAM,
                        CURRENT_TIMESTAMP,
                        UNLOCK_TIMESTAMP,
                        OCCURRENCES
                }
        };
    }
}
