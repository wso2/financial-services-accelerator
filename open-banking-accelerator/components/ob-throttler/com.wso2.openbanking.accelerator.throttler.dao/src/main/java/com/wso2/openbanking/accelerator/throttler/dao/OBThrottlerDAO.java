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
package com.wso2.openbanking.accelerator.throttler.dao;

import com.wso2.openbanking.accelerator.throttler.dao.exception.OBThrottlerDataDeletionException;
import com.wso2.openbanking.accelerator.throttler.dao.exception.OBThrottlerDataInsertionException;
import com.wso2.openbanking.accelerator.throttler.dao.exception.OBThrottlerDataRetrievalException;
import com.wso2.openbanking.accelerator.throttler.dao.exception.OBThrottlerDataUpdationException;
import com.wso2.openbanking.accelerator.throttler.dao.model.ThrottleDataModel;

import java.sql.Connection;
import java.sql.Timestamp;

/**
 * DAO class for throttle data.
 */
public interface OBThrottlerDAO {

    /**
     * Store throttle data.
     *
     * @param connection connection object
     * @param throttleGroup - throttle group
     * @param throttleParam - throttle parameter
     * @param currentTimestamp - current timestamp
     * @param unlockTimestamp - unlock timestamp
     * @return - ThrottleDataModel
     * @throws OBThrottlerDataInsertionException - OBThrottlerDataInsertionException
     */
    ThrottleDataModel storeThrottleData(Connection connection, String throttleGroup, String throttleParam,
                                        Timestamp currentTimestamp, Timestamp unlockTimestamp)
            throws OBThrottlerDataInsertionException;

    /**
     * Update throttle data.
     *
     * @param connection connection object
     * @param throttleGroup - throttle group
     * @param throttleParam - throttle parameter
     * @param currentTimestamp - current timestamp
     * @param unlockTimestamp - unlock timestamp
     * @param occurrences - number of occurrences of the parameter
     * @return - ThrottleDataModel
     * @throws OBThrottlerDataUpdationException - OBThrottlerDataUpdationException
     */
    ThrottleDataModel updateThrottleData(Connection connection, String throttleGroup, String throttleParam,
                                         Timestamp currentTimestamp, Timestamp unlockTimestamp, int occurrences)
            throws OBThrottlerDataUpdationException;

    /**
     * Retrieve throttle data.
     *
     * @param connection connection object
     * @param throttleGroup - throttle group
     * @param throttleParam - throttle parameter
     * @return - ThrottleDataModel
     * @throws OBThrottlerDataRetrievalException - OBThrottlerDataRetrievalException
     */
    ThrottleDataModel getThrottleData(Connection connection, String throttleGroup, String throttleParam)
            throws OBThrottlerDataRetrievalException;

    /**
     * Remove throttle data from database.
     *
     * @param connection connection object
     * @param throttleGroup - throttle group
     * @param throttleParam - throttle parameter
     * @throws OBThrottlerDataDeletionException - OBThrottlerDataDeletionException
     */
    void deleteThrottleData(Connection connection, String throttleGroup, String throttleParam)
            throws OBThrottlerDataDeletionException;

    /**
     * Check if throttle data exists in the database.
     *
     * @param connection connection object
     * @param throttleGroup - throttle group
     * @param throttleParam - throttle parameter
     * @return - boolean
     * @throws OBThrottlerDataRetrievalException - OBThrottlerDataRetrievalException
     */
    boolean isThrottleDataExists(Connection connection, String throttleGroup, String throttleParam)
            throws OBThrottlerDataRetrievalException;
}
