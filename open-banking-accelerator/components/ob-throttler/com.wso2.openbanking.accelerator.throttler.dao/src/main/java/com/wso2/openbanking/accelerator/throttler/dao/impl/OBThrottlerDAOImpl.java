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
package com.wso2.openbanking.accelerator.throttler.dao.impl;

import com.wso2.openbanking.accelerator.throttler.dao.OBThrottlerDAO;
import com.wso2.openbanking.accelerator.throttler.dao.constants.OBThrottlerDAOConstants;
import com.wso2.openbanking.accelerator.throttler.dao.exception.OBThrottlerDataDeletionException;
import com.wso2.openbanking.accelerator.throttler.dao.exception.OBThrottlerDataInsertionException;
import com.wso2.openbanking.accelerator.throttler.dao.exception.OBThrottlerDataRetrievalException;
import com.wso2.openbanking.accelerator.throttler.dao.exception.OBThrottlerDataUpdationException;
import com.wso2.openbanking.accelerator.throttler.dao.model.ThrottleDataModel;
import com.wso2.openbanking.accelerator.throttler.dao.queries.OBThrottlerSQLStatements;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Implementation of OBThrottlerDAO.
 */
public class OBThrottlerDAOImpl implements OBThrottlerDAO {

    private static final Log log = LogFactory.getLog(OBThrottlerDAOImpl.class);
    private OBThrottlerSQLStatements sqlStatements;

    /**
     * Load sql statements specific to database type.
     *
     * @param sqlStatements -sqlStatements specific to db type
     */
    public OBThrottlerDAOImpl(OBThrottlerSQLStatements sqlStatements) {

        this.sqlStatements = sqlStatements;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ThrottleDataModel storeThrottleData(Connection connection, String throttleGroup, String throttleParam,
                                               Timestamp currentTimestamp, Timestamp unlockTimestamp)
            throws OBThrottlerDataInsertionException {

        String storeThrottleDataSql = sqlStatements.storeThrottleData();
        int rowCount;

        //store data
        try (PreparedStatement storePreparedStatement = connection.prepareStatement(storeThrottleDataSql)) {
            //Set prepared statement parameters
            storePreparedStatement.setString(1, throttleGroup);
            storePreparedStatement.setString(2, throttleParam);
            storePreparedStatement.setTimestamp(3, currentTimestamp);
            storePreparedStatement.setTimestamp(4, unlockTimestamp);
            storePreparedStatement.setInt(5, OBThrottlerDAOConstants.FIRST_OCCURRENCE);

            rowCount = storePreparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(OBThrottlerDAOConstants.THROTTLE_DATA_STORE_ERROR_MSG);
            throw new OBThrottlerDataInsertionException(OBThrottlerDAOConstants.THROTTLE_DATA_STORE_ERROR_MSG, e);
        }

        if (rowCount > 0) {
            if (log.isDebugEnabled()) {
                log.debug(String.format(
                        "Stored ThrottleGroup: '%s', ThrottleParam: '%s', CurrentTimestamp: '%s', " +
                                "UnlockTimestamp: '%s', Occurrences: 1", throttleGroup, throttleParam,
                        currentTimestamp, unlockTimestamp).replaceAll("[\r\n]", ""));
            }
            return new ThrottleDataModel(throttleGroup, throttleParam, currentTimestamp, unlockTimestamp, 1);
        } else {
            throw new OBThrottlerDataInsertionException("Failed to properly persist throttle data in database.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ThrottleDataModel updateThrottleData(Connection connection, String throttleGroup, String throttleParam,
                                                Timestamp currentTimestamp, Timestamp unlockTimestamp, int occurrences)
            throws OBThrottlerDataUpdationException {

        String updateThrottleDataSql = sqlStatements.updateThrottleData();
        int rowCount;

        //update database
        try (PreparedStatement updatePreparedStatement = connection.prepareStatement(updateThrottleDataSql)) {
            //Set prepared statement parameters
            updatePreparedStatement.setTimestamp(1, currentTimestamp);
            updatePreparedStatement.setTimestamp(2, unlockTimestamp);
            updatePreparedStatement.setInt(3, occurrences);
            updatePreparedStatement.setString(4, throttleGroup);
            updatePreparedStatement.setString(5, throttleParam);
            rowCount = updatePreparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(OBThrottlerDAOConstants.THROTTLE_DATA_UPDATE_ERROR_MSG);
            throw new OBThrottlerDataUpdationException(OBThrottlerDAOConstants.THROTTLE_DATA_UPDATE_ERROR_MSG, e);
        }

        if (rowCount > 0) {
            if (log.isDebugEnabled()) {
                log.debug(String.format(
                        "Updated ThrottleGroup: '%s', ThrottleParam: '%s', CurrentTimestamp: '%s', " +
                                "UnlockTimestamp: '%s', Occurrences: %d", throttleGroup, throttleParam,
                        currentTimestamp, unlockTimestamp, occurrences).replaceAll("[\r\n]", ""));
            }
            return new ThrottleDataModel(throttleGroup, throttleParam, currentTimestamp, unlockTimestamp, occurrences);
        } else {
            throw new OBThrottlerDataUpdationException("Failed to properly update throttle data in database.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ThrottleDataModel getThrottleData(Connection connection, String throttleGroup, String throttleParam)
            throws OBThrottlerDataRetrievalException {

        ThrottleDataModel throttleDataModel;
        String sql = sqlStatements.retrieveThrottleData();

        //retrieve data from database
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, throttleGroup);
            preparedStatement.setString(2, throttleParam);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Timestamp lastUpdateTimestamp = resultSet.
                            getTimestamp(OBThrottlerDAOConstants.LAST_UPDATE_TIMESTAMP);
                    Timestamp unlockTimestamp = resultSet.getTimestamp(OBThrottlerDAOConstants.UNLOCK_TIMESTAMP);
                    int occurrences = resultSet.getInt(OBThrottlerDAOConstants.OCCURRENCES);
                    throttleDataModel = new ThrottleDataModel(throttleGroup, throttleParam,
                            lastUpdateTimestamp, unlockTimestamp, occurrences);
                } else {
                    log.error(OBThrottlerDAOConstants.NO_RECORDS_FOUND_ERROR_MSG);
                    throw new OBThrottlerDataRetrievalException(OBThrottlerDAOConstants.NO_RECORDS_FOUND_ERROR_MSG);
                }
            } catch (SQLException e) {
                throw new OBThrottlerDataRetrievalException(
                        OBThrottlerDAOConstants.THROTTLE_DATA_RESULT_SET_RETRIEVE_ERROR_MSG, e);
            }
        } catch (SQLException e) {
            log.error(OBThrottlerDAOConstants.THROTTLE_DATA_RETRIEVE_ERROR_MSG);
            throw new OBThrottlerDataRetrievalException(OBThrottlerDAOConstants.THROTTLE_DATA_RETRIEVE_ERROR_MSG, e);
        }
        return throttleDataModel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteThrottleData(Connection connection, String throttleGroup, String throttleParam)
            throws OBThrottlerDataDeletionException {

        String removeThrottleDataSql = sqlStatements.removeThrottleData();
        int rowCount;

        //remove data from database
        try (PreparedStatement removePreparedStatement = connection.prepareStatement(removeThrottleDataSql)) {
            //Set prepared statement parameters
            removePreparedStatement.setString(1, throttleGroup);
            removePreparedStatement.setString(2, throttleParam);
            rowCount = removePreparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(OBThrottlerDAOConstants.THROTTLE_DATA_DELETE_ERROR_MSG);
            throw new OBThrottlerDataDeletionException(OBThrottlerDAOConstants.THROTTLE_DATA_DELETE_ERROR_MSG, e);
        }

        if (rowCount > 0) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Removed ThrottleGroup: '%s', ThrottleParam: '%s'",
                        throttleGroup, throttleParam).replaceAll("[\r\n]", ""));
            }
        } else {
            throw new OBThrottlerDataDeletionException(String.format("Throttle data for %s:%s does not exist in " +
                    "the database", throttleGroup, throttleParam));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isThrottleDataExists(Connection connection, String throttleGroup, String throttleParam)
            throws OBThrottlerDataRetrievalException {

        boolean throttleDataExists = false;
        String sql = sqlStatements.isThrottleDataExists();

        //retrieve data from database
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, throttleGroup);
            preparedStatement.setString(2, throttleParam);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    throttleDataExists = resultSet.getInt(1) > 0;
                } else {
                    log.error(OBThrottlerDAOConstants.NO_RECORDS_FOUND_ERROR_MSG);
                    throw new OBThrottlerDataRetrievalException(OBThrottlerDAOConstants.NO_RECORDS_FOUND_ERROR_MSG);
                }
            } catch (SQLException e) {
                throw new OBThrottlerDataRetrievalException(
                        OBThrottlerDAOConstants.THROTTLE_DATA_RESULT_SET_RETRIEVE_ERROR_MSG, e);
            }
        } catch (SQLException e) {
            log.error(OBThrottlerDAOConstants.THROTTLE_DATA_RETRIEVE_ERROR_MSG);
            throw new OBThrottlerDataRetrievalException(OBThrottlerDAOConstants.THROTTLE_DATA_RETRIEVE_ERROR_MSG, e);
        }
        return throttleDataExists;
    }
}
