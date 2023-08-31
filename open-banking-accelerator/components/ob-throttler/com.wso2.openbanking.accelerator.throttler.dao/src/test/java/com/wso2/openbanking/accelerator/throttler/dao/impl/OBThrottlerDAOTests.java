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
import com.wso2.openbanking.accelerator.throttler.dao.exception.OBThrottlerDataDeletionException;
import com.wso2.openbanking.accelerator.throttler.dao.exception.OBThrottlerDataInsertionException;
import com.wso2.openbanking.accelerator.throttler.dao.exception.OBThrottlerDataRetrievalException;
import com.wso2.openbanking.accelerator.throttler.dao.exception.OBThrottlerDataUpdationException;
import com.wso2.openbanking.accelerator.throttler.dao.model.ThrottleDataModel;
import com.wso2.openbanking.accelerator.throttler.dao.queries.OBThrottlerSQLStatements;
import com.wso2.openbanking.accelerator.throttler.dao.util.OBThrottlerDAOTestData;
import com.wso2.openbanking.accelerator.throttler.dao.util.OBThrottlerDAOUtils;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Test for Open Banking throttler DAO.
 */
public class OBThrottlerDAOTests {

    private static final String DB_NAME = "OB_THROTTLE_DB";

    private OBThrottlerDAO obThrottlerDAO;
    private Connection mockedConnection;
    private PreparedStatement mockedPreparedStatement;
    private ResultSet mockedResultSet;
    private ThrottleDataModel storedThrottleDataModel;

    @BeforeClass
    public void initTest() throws Exception {

        OBThrottlerDAOUtils.initializeDataSource(DB_NAME, OBThrottlerDAOUtils.getFilePath("dbScripts/h2.sql"));
        obThrottlerDAO = new OBThrottlerDAOImpl(new OBThrottlerSQLStatements());
        mockedConnection = Mockito.mock(Connection.class);
        mockedPreparedStatement = Mockito.mock(PreparedStatement.class);
        mockedResultSet = Mockito.mock(ResultSet.class);
    }

    @DataProvider(name = "sampleOBThrottleDataProvider")
    public Object[][] provideOBThrottleData() {

        /*
         * throttleGroup
         * throttleParam
         * currentTimestamp
         * unlockTimestamp
         * occurrences
         */
        return OBThrottlerDAOTestData.DataProviders.OB_THROTTLER_DATA_HOLDER;
    }

    // data insertion tests
    @Test(dataProvider = "sampleOBThrottleDataProvider")
    public void testStoreThrottleData(String throttleGroup, String throttleParam, Timestamp currentTimestamp,
                                         Timestamp unlockTimestamp, int occurrences) throws Exception {

        try (Connection connection = OBThrottlerDAOUtils.getConnection(DB_NAME)) {

            storedThrottleDataModel = obThrottlerDAO.storeThrottleData(connection, throttleGroup, throttleParam,
                    currentTimestamp, unlockTimestamp);
        }
        Assert.assertNotNull(storedThrottleDataModel);
        Assert.assertNotNull(storedThrottleDataModel.getThrottleGroup());
        Assert.assertNotNull(storedThrottleDataModel.getThrottleParam());
        Assert.assertNotNull(storedThrottleDataModel.getLastUpdateTimestamp());
        Assert.assertNotNull(storedThrottleDataModel.getUnlockTimestamp());
    }

    @Test(expectedExceptions = OBThrottlerDataInsertionException.class)
    public void testStoreThrottleDataInsertionError() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(0).when(mockedPreparedStatement).executeUpdate();

        obThrottlerDAO.storeThrottleData(mockedConnection, OBThrottlerDAOTestData.THROTTLE_GROUP,
                OBThrottlerDAOTestData.THROTTLE_PARAM, OBThrottlerDAOTestData.CURRENT_TIMESTAMP,
                OBThrottlerDAOTestData.UNLOCK_TIMESTAMP);
    }

    @Test(expectedExceptions = OBThrottlerDataInsertionException.class)
    public void testStoreThrottleDataSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        obThrottlerDAO.storeThrottleData(mockedConnection, OBThrottlerDAOTestData.THROTTLE_GROUP,
                OBThrottlerDAOTestData.THROTTLE_PARAM, OBThrottlerDAOTestData.CURRENT_TIMESTAMP,
                OBThrottlerDAOTestData.UNLOCK_TIMESTAMP);
    }

    //data updation tests
    @Test(dataProvider = "sampleOBThrottleDataProvider", dependsOnMethods = "testStoreThrottleData")
    public void testUpdateThrottleData(String throttleGroup, String throttleParam, Timestamp currentTimestamp,
                                       Timestamp unlockTimestamp, int occurrences) throws Exception {

        ThrottleDataModel updatedThrottleDataModel;

        try (Connection connection = OBThrottlerDAOUtils.getConnection(DB_NAME)) {

            updatedThrottleDataModel = obThrottlerDAO.updateThrottleData(connection,
                    storedThrottleDataModel.getThrottleGroup(), storedThrottleDataModel.getThrottleParam(),
                    storedThrottleDataModel.getLastUpdateTimestamp(), storedThrottleDataModel.getUnlockTimestamp(),
                    occurrences + 1);
        }
        Assert.assertEquals(updatedThrottleDataModel.getOccurrences(), 2);
    }

    @Test(expectedExceptions = OBThrottlerDataUpdationException.class)
    public void testUpdateThrottleDataError() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(0).when(mockedPreparedStatement).executeUpdate();

        obThrottlerDAO.updateThrottleData(mockedConnection, OBThrottlerDAOTestData.THROTTLE_GROUP,
                OBThrottlerDAOTestData.THROTTLE_PARAM, OBThrottlerDAOTestData.CURRENT_TIMESTAMP,
                OBThrottlerDAOTestData.UNLOCK_TIMESTAMP, 1);
    }

    @Test(expectedExceptions = OBThrottlerDataUpdationException.class)
    public void testUpdateThrottleDataSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        obThrottlerDAO.updateThrottleData(mockedConnection, OBThrottlerDAOTestData.THROTTLE_GROUP,
                OBThrottlerDAOTestData.THROTTLE_PARAM, OBThrottlerDAOTestData.CURRENT_TIMESTAMP,
                OBThrottlerDAOTestData.UNLOCK_TIMESTAMP, 1);
    }

    //data retrieval tests
    @Test(dependsOnMethods = "testUpdateThrottleData")
    public void testRetrieveThrottleData() throws Exception {

        ThrottleDataModel retrievedThrottleDataModel;

        try (Connection connection = OBThrottlerDAOUtils.getConnection(DB_NAME)) {
            retrievedThrottleDataModel = obThrottlerDAO.getThrottleData(connection,
                    storedThrottleDataModel.getThrottleGroup(), storedThrottleDataModel.getThrottleParam());
        }

        Assert.assertNotNull(retrievedThrottleDataModel);
        Assert.assertEquals(retrievedThrottleDataModel.getThrottleGroup(), storedThrottleDataModel.getThrottleGroup());
        Assert.assertNotNull(retrievedThrottleDataModel.getThrottleParam());
        Assert.assertEquals(retrievedThrottleDataModel.getThrottleParam(), storedThrottleDataModel.getThrottleParam());
        Assert.assertNotNull(retrievedThrottleDataModel.getUnlockTimestamp());
        Assert.assertNotNull(retrievedThrottleDataModel.getLastUpdateTimestamp());
        Assert.assertNotNull(retrievedThrottleDataModel.getOccurrences());
    }

    @Test(dependsOnMethods = "testUpdateThrottleData")
    public void testRetrieveThrottleDataExists() throws Exception {

        Boolean isRetrieved;

        try (Connection connection = OBThrottlerDAOUtils.getConnection(DB_NAME)) {
            isRetrieved = obThrottlerDAO.isThrottleDataExists(connection,
                    storedThrottleDataModel.getThrottleGroup(), storedThrottleDataModel.getThrottleParam());
        }

        Assert.assertTrue(isRetrieved);
    }

    @Test(expectedExceptions = OBThrottlerDataRetrievalException.class)
    public void testRetrieveThrottleDataError() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(0).when(mockedPreparedStatement).executeUpdate();
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        obThrottlerDAO.getThrottleData(mockedConnection, OBThrottlerDAOTestData.THROTTLE_GROUP,
                OBThrottlerDAOTestData.THROTTLE_PARAM);
    }

    @Test (expectedExceptions = OBThrottlerDataRetrievalException.class)
    public void testRetrieveThrottleDataResultSetError() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(mockedResultSet).when(mockedPreparedStatement).executeQuery();
        Mockito.doReturn(false).when(mockedResultSet).next();
        obThrottlerDAO.getThrottleData(mockedConnection, OBThrottlerDAOTestData.THROTTLE_GROUP,
                OBThrottlerDAOTestData.THROTTLE_PARAM);
    }

    @Test(expectedExceptions = OBThrottlerDataRetrievalException.class)
    public void testRetrieveThrottleDataExistsError() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(0).when(mockedPreparedStatement).executeUpdate();
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        obThrottlerDAO.isThrottleDataExists(mockedConnection, OBThrottlerDAOTestData.THROTTLE_GROUP,
                OBThrottlerDAOTestData.THROTTLE_PARAM);
    }

    @Test (expectedExceptions = OBThrottlerDataRetrievalException.class)
    public void testRetrieveThrottleDataExistsResultSetError() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(mockedResultSet).when(mockedPreparedStatement).executeQuery();
        Mockito.doReturn(false).when(mockedResultSet).next();
        obThrottlerDAO.isThrottleDataExists(mockedConnection, OBThrottlerDAOTestData.THROTTLE_GROUP,
                OBThrottlerDAOTestData.THROTTLE_PARAM);
    }

    @Test(expectedExceptions = OBThrottlerDataRetrievalException.class)
    public void testRetrieveThrottleDataSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        obThrottlerDAO.getThrottleData(mockedConnection, OBThrottlerDAOTestData.THROTTLE_GROUP,
                OBThrottlerDAOTestData.THROTTLE_PARAM);
    }

    @Test(expectedExceptions = OBThrottlerDataRetrievalException.class)
    public void testRetrieveThrottleDataExistsSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        obThrottlerDAO.isThrottleDataExists(mockedConnection, OBThrottlerDAOTestData.THROTTLE_GROUP,
                OBThrottlerDAOTestData.THROTTLE_PARAM);
    }

    //data deletion tests
    @Test(dependsOnMethods = "testRetrieveThrottleData")
    public void testDeleteConsentAttribute() throws Exception {

        try (Connection connection = OBThrottlerDAOUtils.getConnection(DB_NAME)) {

            obThrottlerDAO.deleteThrottleData(connection, storedThrottleDataModel.getThrottleGroup(),
                    storedThrottleDataModel.getThrottleParam());
        }
    }

    @Test(expectedExceptions = OBThrottlerDataDeletionException.class)
    public void testDeleteThrottleDataError() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(0).when(mockedPreparedStatement).executeUpdate();

        obThrottlerDAO.deleteThrottleData(mockedConnection, OBThrottlerDAOTestData.THROTTLE_GROUP,
                OBThrottlerDAOTestData.THROTTLE_PARAM);
    }

    @Test(expectedExceptions = OBThrottlerDataDeletionException.class)
    public void testDeleteThrottleDataSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        obThrottlerDAO.deleteThrottleData(mockedConnection, OBThrottlerDAOTestData.THROTTLE_GROUP,
                OBThrottlerDAOTestData.THROTTLE_PARAM);
    }
}
