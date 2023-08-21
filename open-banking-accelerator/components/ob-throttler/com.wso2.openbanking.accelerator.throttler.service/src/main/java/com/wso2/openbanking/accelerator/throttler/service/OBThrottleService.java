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
package com.wso2.openbanking.accelerator.throttler.service;

import com.wso2.openbanking.accelerator.common.exception.OBThrottlerException;
import com.wso2.openbanking.accelerator.common.util.DatabaseUtil;
import com.wso2.openbanking.accelerator.throttler.dao.OBThrottlerDAO;
import com.wso2.openbanking.accelerator.throttler.dao.exception.OBThrottlerDataDeletionException;
import com.wso2.openbanking.accelerator.throttler.dao.exception.OBThrottlerDataInsertionException;
import com.wso2.openbanking.accelerator.throttler.dao.exception.OBThrottlerDataRetrievalException;
import com.wso2.openbanking.accelerator.throttler.dao.exception.OBThrottlerDataUpdationException;
import com.wso2.openbanking.accelerator.throttler.dao.model.ThrottleDataModel;
import com.wso2.openbanking.accelerator.throttler.dao.persistence.DataStoreInitializer;
import com.wso2.openbanking.accelerator.throttler.service.constants.OBThrottlerServiceConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Service class for OB Throttler.
 * <p>
 * Contains methods required to throttle the occurrence of a given parameter.
 * The parameters can be separated into groups by 'throttleGroup' attribute, which will
 * allow throttling same parameter values in different groups.
 */
public class OBThrottleService {

    private static Log log = LogFactory.getLog(OBThrottleService.class);
    protected Map<String, Map<String, Timestamp>> throttleDataMap = new HashMap<>();
    private static OBThrottleService instance = null;

    // private constructor
    private OBThrottleService() {
    }

    /**
     * @return OBThrottleService instance
     */
    public static synchronized OBThrottleService getInstance() {

        if (instance == null) {
            instance = new OBThrottleService();
        }
        return instance;
    }

    /**
     * Update throttle database and throttleDataMap.
     *
     * @param throttleGroup      - throttle group
     * @param throttleParam      - throttle parameter
     * @param throttleLimit      - allowed number of occurrences
     * @param throttleTimePeriod - time period that the parameter is throttled (seconds)
     * @throws OBThrottlerException - OBThrottlerException
     */
    public void updateThrottleData(String throttleGroup, String throttleParam, int throttleLimit,
                                   int throttleTimePeriod) throws OBThrottlerException {

        ThrottleDataModel throttleDataModel;
        ThrottleDataModel existingThrottleDataModel;
        OBThrottlerDAO obThrottlerDAO = DataStoreInitializer.initializeOBThrottlerDAO();
        Timestamp currentTimestamp = new Timestamp(new Date().getTime());
        Timestamp unlockTimestamp = new Timestamp(currentTimestamp.getTime() + (1000L * throttleTimePeriod));

        Connection connection = DatabaseUtil.getDBConnection();

        try {
            //remove expired data from database by checking throttle status
            getThrottleStatus(connection, throttleGroup, throttleParam, obThrottlerDAO);
            //check if throttle group and parameter exists. Add new record if not.

            if (obThrottlerDAO.isThrottleDataExists(connection, throttleGroup, throttleParam)) {
                existingThrottleDataModel = obThrottlerDAO.getThrottleData(connection, throttleGroup, throttleParam);
                //increment Occurrences
                int updatedOccurrences = existingThrottleDataModel.getOccurrences() + 1;
                throttleDataModel = obThrottlerDAO.updateThrottleData(connection, throttleGroup, throttleParam,
                        currentTimestamp, unlockTimestamp, updatedOccurrences);
            } else {
                throttleDataModel = obThrottlerDAO.storeThrottleData(connection, throttleGroup, throttleParam,
                        currentTimestamp, unlockTimestamp);
            }
            DatabaseUtil.commitTransaction(connection);
            log.debug(OBThrottlerServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
            if (throttleDataModel.getOccurrences() > throttleLimit) {
                updateThrottleDataMap(throttleGroup, throttleParam, throttleDataModel.getUnlockTimestamp());
            }
        } catch (OBThrottlerDataInsertionException e) {
            log.error(OBThrottlerServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
            DatabaseUtil.rollbackTransaction(connection);
            throw new OBThrottlerException(OBThrottlerServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
        } catch (OBThrottlerDataUpdationException e) {
            log.error(OBThrottlerServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            DatabaseUtil.rollbackTransaction(connection);
            throw new OBThrottlerException(OBThrottlerServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
        } catch (OBThrottlerDataRetrievalException e) {
            log.error(OBThrottlerServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            DatabaseUtil.rollbackTransaction(connection);
            throw new OBThrottlerException(OBThrottlerServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
        } catch (OBThrottlerDataDeletionException e) {
            log.error(OBThrottlerServiceConstants.DATA_DELETE_ROLLBACK_ERROR_MSG, e);
            DatabaseUtil.rollbackTransaction(connection);
            throw new OBThrottlerException(OBThrottlerServiceConstants.DATA_DELETE_ROLLBACK_ERROR_MSG, e);
        } finally {
            log.debug(OBThrottlerServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Check if the given parameter is throttled.
     *
     * @param throttleGroup - throttle group
     * @param throttleParam - throttle parameter
     * @return - boolean
     * @throws OBThrottlerException - OBThrottlerDataDeletionException
     */
    public boolean isThrottled(String throttleGroup, String throttleParam) throws OBThrottlerException {

        Connection connection = DatabaseUtil.getDBConnection();
        OBThrottlerDAO obThrottlerDAO = DataStoreInitializer.initializeOBThrottlerDAO();

        try {
            boolean throttleStatus = getThrottleStatus(connection, throttleGroup, throttleParam, obThrottlerDAO);
            DatabaseUtil.commitTransaction(connection);
            log.debug(OBThrottlerServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
            return throttleStatus;
        } catch (OBThrottlerDataDeletionException e) {
            log.error(OBThrottlerServiceConstants.DATA_DELETE_ROLLBACK_ERROR_MSG, e);
            DatabaseUtil.rollbackTransaction(connection);
            throw new OBThrottlerException(OBThrottlerServiceConstants.DATA_DELETE_ROLLBACK_ERROR_MSG, e);
        } finally {
            log.debug(OBThrottlerServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Delete the throttle data record from DB on a successful attempt.
     *
     * @param throttleGroup - throttle group
     * @param throttleParam - throttle parameter
     * @throws OBThrottlerException - OBThrottlerDataDeletionException, OBThrottlerDataRetrievalException
     */
    public void deleteRecordOnSuccessAttempt(String throttleGroup, String throttleParam) throws OBThrottlerException {

        Connection connection = DatabaseUtil.getDBConnection();
        OBThrottlerDAO obThrottlerDAO = DataStoreInitializer.initializeOBThrottlerDAO();

        try {
            if (obThrottlerDAO.isThrottleDataExists(connection, throttleGroup, throttleParam)) {
                obThrottlerDAO.deleteThrottleData(connection, throttleGroup, throttleParam);
                DatabaseUtil.commitTransaction(connection);
            }
        } catch (OBThrottlerDataRetrievalException e) {
            log.error(OBThrottlerServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            DatabaseUtil.rollbackTransaction(connection);
            throw new OBThrottlerException(OBThrottlerServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
        } catch (OBThrottlerDataDeletionException e) {
            log.error(OBThrottlerServiceConstants.DATA_DELETE_ROLLBACK_ERROR_MSG, e);
            DatabaseUtil.rollbackTransaction(connection);
            throw new OBThrottlerException(OBThrottlerServiceConstants.DATA_DELETE_ROLLBACK_ERROR_MSG, e);
        } finally {
            log.debug(OBThrottlerServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Check if the given parameter is throttled. This method is overloaded.
     *
     * @param connection connection object
     * @param throttleGroup - throttle group
     * @param throttleParam - throttle parameter
     * @return - boolean
     * @throws OBThrottlerDataDeletionException - OBThrottlerDataDeletionException
     */
    private boolean getThrottleStatus(Connection connection, String throttleGroup, String throttleParam,
                               OBThrottlerDAO obThrottlerDAO) throws OBThrottlerDataDeletionException {

        Map<String, Timestamp> throttleParamMap;
        if (throttleDataMap.containsKey(throttleGroup)) {
            throttleParamMap = throttleDataMap.get(throttleGroup);
            if (throttleParamMap.containsKey(throttleParam)) {
                //check if the parameter is still locked
                Timestamp currentTimestamp = new Timestamp(new Date().getTime());
                Timestamp unlockTimestamp = throttleParamMap.get(throttleParam);
                if (unlockTimestamp.after(currentTimestamp)) {
                    return true;
                } else {
                    //remove throttle parameter from throttle data map if expired
                    throttleParamMap.remove(throttleParam);
                    throttleDataMap.put(throttleGroup, throttleParamMap);
                    //remove from database
                    obThrottlerDAO.deleteThrottleData(connection, throttleGroup, throttleParam);
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Update throttle data map.
     *
     * @param throttleGroup   - throttle group
     * @param throttleParam   - throttle parameter
     * @param unlockTimestamp - timestamp that the parameter will be unlocked.
     */
    protected void updateThrottleDataMap(String throttleGroup, String throttleParam, Timestamp unlockTimestamp) {

        Map<String, Timestamp> throttleParamMap;
        //check if throttle group already exists
        if (throttleDataMap.containsKey(throttleGroup)) {
            throttleParamMap = throttleDataMap.get(throttleGroup);
        } else {
            throttleParamMap = new HashMap<>();
        }
        //put parameter and unlockTimestamp to the throttle data map
        throttleParamMap.put(throttleParam, unlockTimestamp);
        throttleDataMap.put(throttleGroup, throttleParamMap);
    }
}
