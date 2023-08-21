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

package com.wso2.openbanking.accelerator.consent.mgt.dao.impl;

import com.wso2.openbanking.accelerator.consent.mgt.dao.ConsentCoreDAO;
import com.wso2.openbanking.accelerator.consent.mgt.dao.constants.ConsentMgtDAOConstants;
import com.wso2.openbanking.accelerator.consent.mgt.dao.exceptions.OBConsentDataDeletionException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.exceptions.OBConsentDataInsertionException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.exceptions.OBConsentDataRetrievalException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.exceptions.OBConsentDataUpdationException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentAttributes;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentFile;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentHistoryResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentStatusAuditRecord;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.queries.ConsentMgtCommonDBQueries;
import com.wso2.openbanking.accelerator.consent.mgt.dao.util.ConsentMgtDAOTestData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.util.DAOUtils;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Open banking consent management DAO tests.
 */
public class OBConsentMgtDAOTests {

    private static final String DB_NAME = "CONSENT_DB";

    private ConsentCoreDAO consentCoreDAO;
    private Connection mockedConnection;
    private PreparedStatement mockedPreparedStatement;
    private ResultSet mockedResultSet;

    @BeforeClass
    public void initTest() throws Exception {

        DAOUtils.initializeDataSource(DB_NAME, DAOUtils.getFilePath("dbScripts/h2.sql"));
        consentCoreDAO = new ConsentCoreDAOImpl(new ConsentMgtCommonDBQueries());
        mockedConnection = Mockito.mock(Connection.class);
        mockedPreparedStatement = Mockito.mock(PreparedStatement.class);
        mockedResultSet = Mockito.mock(ResultSet.class);
    }

    @DataProvider(name = "storeConsentDataProvider")
    public Object[][] storeConsentResourceData() {

        /*
         * consentID
         * clientID
         * receipt
         * consentType
         * consentFrequency
         * validityPeriod
         * recurringIndicator
         * currentStatus
         */
        return ConsentMgtDAOTestData.DataProviders.CONSENT_RESOURCE_DATA_HOLDER;
    }

    @Test(dataProvider = "storeConsentDataProvider")
    public void testStoreConsentResource(String clientID, String receipt, String consentType,
                                         int consentFrequency, long validityPeriod, boolean recurringIndicator,
                                         String currentStatus) throws Exception {

        ConsentResource storedConsentResource;
        ConsentResource consentResource = new ConsentResource();
        consentResource.setReceipt(receipt);
        consentResource.setClientID(clientID);
        consentResource.setConsentType(consentType);
        consentResource.setCurrentStatus(currentStatus);
        consentResource.setConsentFrequency(consentFrequency);
        consentResource.setValidityPeriod(validityPeriod);
        consentResource.setConsentID(UUID.randomUUID().toString());
        consentResource.setRecurringIndicator(true);

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
        }
        Assert.assertNotNull(storedConsentResource);
        Assert.assertNotNull(storedConsentResource.getConsentID());
        Assert.assertNotNull(storedConsentResource.getClientID());
        Assert.assertNotNull(storedConsentResource.getConsentType());
        Assert.assertEquals(consentFrequency, storedConsentResource.getConsentFrequency());
        Assert.assertNotNull(storedConsentResource.getValidityPeriod());
        Assert.assertTrue(storedConsentResource.isRecurringIndicator());
        Assert.assertNotNull(storedConsentResource.getCreatedTime());
        Assert.assertNotNull(storedConsentResource.getCurrentStatus());
    }

    @Test (expectedExceptions = OBConsentDataInsertionException.class)
    public void testStoreConsentResourceInsertionError() throws Exception {

        Mockito.doReturn(Mockito.mock(PreparedStatement.class)).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(0).when(Mockito.mock(PreparedStatement.class)).executeUpdate();

        consentCoreDAO.storeConsentResource(mockedConnection, ConsentMgtDAOTestData.getSampleTestConsentResource());
    }

    @Test (expectedExceptions = OBConsentDataInsertionException.class)
    public void testStoreConsentResourceSQLError() throws Exception {


        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.storeConsentResource(mockedConnection, ConsentMgtDAOTestData.getSampleTestConsentResource());
    }

    @DataProvider(name = "storeAuthorizationDataProvider")
    public Object[][] storeAuthorizationResourceData() {

        /*
         * authorizationID
         * consentID
         * authorizationType
         * userID
         * authorizationStatus
         */
        return ConsentMgtDAOTestData.DataProviders.AUTHORIZATION_RESOURCE_DATA_HOLDER;
    }

    @Test (dataProvider = "storeAuthorizationDataProvider")
    public void testStoreAuthorizationResource(String authorizationType,
                                               String userID, String authorizationStatus) throws Exception {

        ConsentResource storedConsentResource;
        AuthorizationResource storedAuthorizationResource;
        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            AuthorizationResource authorizationResource = new AuthorizationResource();
            authorizationResource.setConsentID(storedConsentResource.getConsentID());
            authorizationResource.setAuthorizationType(authorizationType);
            authorizationResource.setUserID(userID);
            authorizationResource.setAuthorizationStatus(authorizationStatus);

            storedAuthorizationResource = consentCoreDAO.storeAuthorizationResource(connection,
                    authorizationResource);
        }
        Assert.assertNotNull(storedAuthorizationResource.getConsentID());
        Assert.assertNotNull(storedAuthorizationResource.getAuthorizationType());
        Assert.assertNotNull(storedAuthorizationResource.getUserID());
        Assert.assertNotNull(storedAuthorizationResource.getAuthorizationStatus());
        Assert.assertNotNull(storedAuthorizationResource.getUpdatedTime());
        Assert.assertNotNull(storedAuthorizationResource.getAuthorizationID());
    }

    @Test (expectedExceptions = OBConsentDataInsertionException.class)
    public void testStoreAuthorizationResourceInsertionError() throws Exception {

        ConsentResource storedConsentResource = ConsentMgtDAOTestData.getSampleStoredTestConsentResource();

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(0).when(mockedPreparedStatement).executeUpdate();

        consentCoreDAO.storeAuthorizationResource(mockedConnection, ConsentMgtDAOTestData.
                getSampleTestAuthorizationResource(storedConsentResource.getConsentID()));
    }

    @Test (expectedExceptions = OBConsentDataInsertionException.class)
    public void testStoreAuthorizationResourceSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());

        consentCoreDAO.storeAuthorizationResource(mockedConnection, ConsentMgtDAOTestData.
                getSampleTestAuthorizationResource(Mockito.anyString()));
    }

    @DataProvider(name = "storeConsentMappingDataProvider")
    public Object[][] storeConsentMappingResourceData() {

        /*
         * accountID
         * permission
         * mappingStatus
         */
        return ConsentMgtDAOTestData.DataProviders.CONSENT_MAPPING_RESOURCE_DATA_HOLDER;
    }

    @Test (dataProvider = "storeConsentMappingDataProvider")
    public void testStoreConsentMappingResource(String accountID, String permission,
                                                String mappingStatus) throws Exception {

        ConsentResource consentResource;
        AuthorizationResource authorizationResource;
        ConsentMappingResource consentMappingResource;
        ConsentResource storedConsentResource;
        AuthorizationResource storedAuthorizationResource;
        ConsentMappingResource storedConsentMappingResource;

        consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            authorizationResource = ConsentMgtDAOTestData
                    .getSampleTestAuthorizationResource(storedConsentResource.getConsentID());

            storedAuthorizationResource = consentCoreDAO.storeAuthorizationResource(connection,
                    authorizationResource);

            consentMappingResource = new ConsentMappingResource();
            consentMappingResource.setAuthorizationID(storedAuthorizationResource.getAuthorizationID());
            consentMappingResource.setAccountID(accountID);
            consentMappingResource.setPermission(permission);
            consentMappingResource.setMappingStatus(mappingStatus);

            storedConsentMappingResource = consentCoreDAO.storeConsentMappingResource(connection,
                    consentMappingResource);
        }
        Assert.assertNotNull(storedConsentMappingResource.getMappingID());
        Assert.assertNotNull(storedConsentMappingResource.getAuthorizationID());
        Assert.assertNotNull(storedConsentMappingResource.getAccountID());
        Assert.assertNotNull(storedConsentMappingResource.getPermission());
        Assert.assertNotNull(storedConsentMappingResource.getMappingStatus());
    }

    @Test(dataProvider = "storeConsentMappingDataProvider")
    public void testStoreConsentMappingResourceWithID(String accountID, String permission,
                                                      String mappingStatus) throws Exception {

        ConsentResource consentResource;
        AuthorizationResource authorizationResource;
        ConsentMappingResource consentMappingResource;
        ConsentResource storedConsentResource;
        AuthorizationResource storedAuthorizationResource;
        ConsentMappingResource storedConsentMappingResource;

        consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            authorizationResource = ConsentMgtDAOTestData
                    .getSampleTestAuthorizationResource(storedConsentResource.getConsentID());
            authorizationResource.setAuthorizationID("db0b943d-38e2-47e4-bb78-8a242d279b5a");
            authorizationResource.setUpdatedTime(1669917425);
            storedAuthorizationResource = consentCoreDAO.storeAuthorizationResource(connection,
                    authorizationResource);

            consentMappingResource = new ConsentMappingResource();
            consentMappingResource.setAuthorizationID(storedAuthorizationResource.getAuthorizationID());
            consentMappingResource.setAccountID(accountID);
            consentMappingResource.setMappingID("aa4c943d-38e2-47e5-bb78-8a242d279b5a");
            consentMappingResource.setPermission(permission);
            consentMappingResource.setMappingStatus(mappingStatus);

            storedConsentMappingResource = consentCoreDAO.storeConsentMappingResource(connection,
                    consentMappingResource);
        }
        Assert.assertTrue(storedConsentMappingResource.getMappingID().equals("aa4c943d-38e2-47e5-bb78-8a242d279b5a"));
        Assert.assertTrue(
                storedConsentMappingResource.getAuthorizationID().equals("db0b943d-38e2-47e4-bb78-8a242d279b5a"));
        Assert.assertNotNull(storedConsentMappingResource.getAccountID());
        Assert.assertNotNull(storedConsentMappingResource.getPermission());
        Assert.assertNotNull(storedConsentMappingResource.getMappingStatus());
    }

    @Test(expectedExceptions = OBConsentDataInsertionException.class)
    public void testStoreConsentMappingResourceInsertionError() throws Exception {

        ConsentMappingResource sampleConsentMappingResource =
                ConsentMgtDAOTestData.getSampleTestConsentMappingResource(ConsentMgtDAOTestData
                        .getSampleStoredTestAuthorizationResource().getAuthorizationID());

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(0).when(mockedPreparedStatement).executeUpdate();

        consentCoreDAO.storeConsentMappingResource(mockedConnection, sampleConsentMappingResource);
    }

    @Test (expectedExceptions = OBConsentDataInsertionException.class)
    public void testStoreConsentMappingResourceSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.storeConsentMappingResource(mockedConnection, new ConsentMappingResource());
    }

    @Test(expectedExceptions = OBConsentDataInsertionException.class)
    public void testStoreNullConsentMappingResource() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.storeConsentMappingResource(mockedConnection, null);
    }

    @DataProvider(name = "storeConsentStatusAuditRecordDataProvider")
    public Object[][] storeConsentStatusAuditRecordData() {

        /*
         * currentStatus
         * reason
         * actionBy
         * previousStatus
         */
        return ConsentMgtDAOTestData.DataProviders.CONSENT_STATUS_AUDIT_RECORD_DATA_HOLDER;
    }

    @Test (dataProvider = "storeConsentStatusAuditRecordDataProvider")
    public void testStoreConsentStatusAuditRecord(String currentStatus, String reason,
                                                  String actionBy, String previousStatus) throws Exception {

        ConsentResource storedConsentResource;
        ConsentStatusAuditRecord storedConsentStatusAuditRecord;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            ConsentStatusAuditRecord consentStatusAuditRecord = new ConsentStatusAuditRecord();
            consentStatusAuditRecord.setConsentID(storedConsentResource.getConsentID());
            consentStatusAuditRecord.setCurrentStatus(currentStatus);
            consentStatusAuditRecord.setReason(reason);
            consentStatusAuditRecord.setActionBy(actionBy);
            consentStatusAuditRecord.setPreviousStatus(previousStatus);


            storedConsentStatusAuditRecord = consentCoreDAO.storeConsentStatusAuditRecord(connection,
                    consentStatusAuditRecord);
        }
        Assert.assertNotNull(storedConsentStatusAuditRecord.getConsentID());
        Assert.assertNotNull(storedConsentStatusAuditRecord.getCurrentStatus());
        Assert.assertNotNull(storedConsentStatusAuditRecord.getReason());
        Assert.assertNotNull(storedConsentStatusAuditRecord.getActionBy());
        Assert.assertNotNull(storedConsentStatusAuditRecord.getPreviousStatus());
        Assert.assertNotNull(storedConsentStatusAuditRecord.getActionTime());
        Assert.assertNotNull(storedConsentStatusAuditRecord.getStatusAuditID());
    }

    @Test(dataProvider = "storeConsentStatusAuditRecordDataProvider")
    public void testStoreConsentStatusAuditRecordWithConsentId(String currentStatus, String reason,
                                                               String actionBy, String previousStatus)
            throws Exception {

        ConsentResource storedConsentResource;
        ConsentStatusAuditRecord storedConsentStatusAuditRecord;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();
        consentResource.setConsentID("234ba17f-c3ac-4493-9049-d71f99c36dc2");
        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            ConsentStatusAuditRecord consentStatusAuditRecord = new ConsentStatusAuditRecord();
            consentStatusAuditRecord.setConsentID(storedConsentResource.getConsentID());
            consentStatusAuditRecord.setCurrentStatus(currentStatus);
            consentStatusAuditRecord.setReason(reason);
            consentStatusAuditRecord.setActionBy(actionBy);
            consentStatusAuditRecord.setPreviousStatus(previousStatus);
            consentStatusAuditRecord.setActionTime(1669917425);

            storedConsentStatusAuditRecord = consentCoreDAO.storeConsentStatusAuditRecord(connection,
                    consentStatusAuditRecord);
        }
        Assert.assertTrue(
                storedConsentStatusAuditRecord.getConsentID().equals("234ba17f-c3ac-4493-9049-d71f99c36dc2"));
        Assert.assertNotNull(storedConsentStatusAuditRecord.getCurrentStatus());
        Assert.assertNotNull(storedConsentStatusAuditRecord.getReason());
        Assert.assertNotNull(storedConsentStatusAuditRecord.getActionBy());
        Assert.assertNotNull(storedConsentStatusAuditRecord.getPreviousStatus());
        Assert.assertNotNull(storedConsentStatusAuditRecord.getActionTime());
        Assert.assertNotNull(storedConsentStatusAuditRecord.getStatusAuditID());
    }

    @Test (expectedExceptions = OBConsentDataInsertionException.class)
    public void testStoreConsentStatusAuditRecordInsertionError() throws Exception {

        ConsentStatusAuditRecord sampleConsentStatusAuditRecord = ConsentMgtDAOTestData
                .getSampleTestConsentStatusAuditRecord(ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                        ConsentMgtDAOTestData.SAMPLE_CURRENT_STATUS);
        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(0).when(mockedPreparedStatement).executeUpdate();
        consentCoreDAO.storeConsentStatusAuditRecord(mockedConnection, sampleConsentStatusAuditRecord);
    }

    @Test(expectedExceptions = OBConsentDataInsertionException.class)
    public void testStoreNullConsentStatusAuditRecord() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(0).when(mockedPreparedStatement).executeUpdate();
        consentCoreDAO.storeConsentStatusAuditRecord(mockedConnection, null);
    }

    @Test(expectedExceptions = OBConsentDataInsertionException.class)
    public void testStoreConsentStatusAuditRecordSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.storeConsentStatusAuditRecord(mockedConnection, new ConsentStatusAuditRecord());
    }

    @DataProvider(name = "storeConsentAttributesDataProvider")
    public Object[][] storeConsentAttributesData() {

        /*
         * consentAttributesMap
         */
        return ConsentMgtDAOTestData.DataProviders.CONSENT_ATTRIBUTES_DATA_HOLDER;
    }

    @Test (dataProvider = "storeConsentAttributesDataProvider")
    public void testStoreConsentAttributes(Map<String, String> consentAttributes) throws Exception {

        ConsentResource storedConsentResource;
        ConsentAttributes consentAttributesResource;
        boolean isConsentAttributesStored;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            consentAttributesResource = new ConsentAttributes();
            consentAttributesResource.setConsentID(storedConsentResource.getConsentID());
            consentAttributesResource.setConsentAttributes(consentAttributes);

            isConsentAttributesStored = consentCoreDAO.storeConsentAttributes(connection, consentAttributesResource);
        }
        Assert.assertTrue(isConsentAttributesStored);
    }

    @Test (expectedExceptions = OBConsentDataInsertionException.class)
    public void testStoreConsentAttributesSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.storeConsentAttributes(mockedConnection, ConsentMgtDAOTestData
                .getSampleTestConsentAttributesObject(ConsentMgtDAOTestData.getSampleStoredTestConsentResource()
                        .getConsentID()));
    }

    @DataProvider(name = "storeConsentFileDataProvider")
    public Object[][] storeConsentFileData() {

        /*
         * consentFile
         */
        return ConsentMgtDAOTestData.DataProviders.CONSENT_FILE_DATA_HOLDER;
    }

    @Test (dataProvider = "storeConsentFileDataProvider")
    public void testStoreConsentFile(String fileContent) throws Exception {

        ConsentResource storedConsentResource;
        ConsentFile consentFileResource;
        boolean isConsentFileStored;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            consentFileResource = new ConsentFile();
            consentFileResource.setConsentID(storedConsentResource.getConsentID());
            consentFileResource.setConsentFile(fileContent);

            isConsentFileStored = consentCoreDAO.storeConsentFile(connection, consentFileResource);
        }
        Assert.assertTrue(isConsentFileStored);
    }

    @Test (expectedExceptions = OBConsentDataInsertionException.class)
    public void testStoreConsentFileInsertionError() throws Exception {

        ConsentFile sampleConsentFileResource =
                ConsentMgtDAOTestData.getSampleConsentFileObject(ConsentMgtDAOTestData.SAMPLE_CONSENT_FILE);

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(0).when(mockedPreparedStatement).executeUpdate();
        consentCoreDAO.storeConsentFile(mockedConnection, sampleConsentFileResource);
    }

    @Test (expectedExceptions = OBConsentDataInsertionException.class)
    public void testStoreConsentFileSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.storeConsentFile(mockedConnection, Mockito.anyObject());
    }

    @DataProvider(name = "updateConsentStatusDataProvider")
    public Object[][] updateConsentStatusData() {

        /*
         * newConsentStatus
         */
        return ConsentMgtDAOTestData.DataProviders.CONSENT_STATUS_UPDATE_DATA_HOLDER;
    }

    @Test (dataProvider = "updateConsentStatusDataProvider")
    public void testUpdateConsentStatus(String newConsentStatus) throws Exception {

        ConsentResource storedConsentResource;
        ConsentResource updatedConsentResource;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            updatedConsentResource = consentCoreDAO.updateConsentStatus(connection,
                    storedConsentResource.getConsentID(), newConsentStatus);
        }
        Assert.assertNotNull(updatedConsentResource.getConsentID());
        Assert.assertNotNull(updatedConsentResource.getCurrentStatus());
    }

    @Test (expectedExceptions = OBConsentDataUpdationException.class)
    public void testUpdateConsentStatusSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.updateConsentStatus(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                ConsentMgtDAOTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (dataProvider = "updateConsentStatusDataProvider", expectedExceptions = OBConsentDataUpdationException.class)
    public void testUpdateConsentStatusWithUnmatchedConsentID(String newConsentStatus) throws Exception {

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            consentCoreDAO.updateConsentStatus(connection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                    newConsentStatus);
        }
    }

    @DataProvider(name = "updateConsentMappingStatusDataProvider")
    public Object[][] updateConsentMappingStatusData() {

        /*
         * newMappingStatus
         */
        return ConsentMgtDAOTestData.DataProviders.CONSENT_MAPPING_STATUS_UPDATE_DATA_HOLDER;
    }

    @Test (dataProvider = "updateConsentMappingStatusDataProvider")
    public void testUpdateConsentMappingStatus(String newMappingStatus) throws Exception {

        boolean isConsentMappingStatusUpdated;
        ConsentResource storedConsentResource;
        AuthorizationResource authorizationResource;
        AuthorizationResource storedAuthorizationResource;
        ConsentMappingResource consentMappingResource;
        ConsentMappingResource storedConsentMappingResource;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            authorizationResource = ConsentMgtDAOTestData
                    .getSampleTestAuthorizationResource(storedConsentResource.getConsentID());

            storedAuthorizationResource = consentCoreDAO.storeAuthorizationResource(connection, authorizationResource);

            consentMappingResource =
                    ConsentMgtDAOTestData
                            .getSampleTestConsentMappingResource(storedAuthorizationResource.getAuthorizationID());

            storedConsentMappingResource = consentCoreDAO.storeConsentMappingResource(connection,
                    consentMappingResource);

            ArrayList<String> mappingIDs = new ArrayList<String>() {
                {
                    add(storedConsentMappingResource.getMappingID());
                }
            };

            isConsentMappingStatusUpdated = consentCoreDAO.updateConsentMappingStatus(connection, mappingIDs,
                    newMappingStatus);
        }
        Assert.assertTrue(isConsentMappingStatusUpdated);
    }

    @Test (expectedExceptions = OBConsentDataUpdationException.class)
    public void testUpdateConsentMappingStatusSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.updateConsentMappingStatus(mockedConnection, ConsentMgtDAOTestData.UNMATCHED_MAPPING_IDS,
                ConsentMgtDAOTestData.SAMPLE_MAPPING_STATUS);
    }

    @DataProvider(name = "updateAuthorizationStatusDataProvider")
    public Object[][] updateAuthorizationStatusData() {

        /*
         * newAuthorizationStatus
         */
        return ConsentMgtDAOTestData.DataProviders.CONSENT_AUTHORIZATION_STATUS_UPDATE_DATA_HOLDER;
    }

    @Test (dataProvider = "updateAuthorizationStatusDataProvider")
    public void testUpdateAuthorizationStatus(String newAuthorizationStatus) throws Exception {

        ConsentResource storedConsentResource;
        AuthorizationResource authorizationResource;
        AuthorizationResource storedAuthorizationResource;
        AuthorizationResource updatedAuthorizationResource;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            authorizationResource = ConsentMgtDAOTestData
                    .getSampleTestAuthorizationResource(storedConsentResource.getConsentID());

            storedAuthorizationResource = consentCoreDAO.storeAuthorizationResource(connection, authorizationResource);

            updatedAuthorizationResource = consentCoreDAO.updateAuthorizationStatus(connection,
                    storedAuthorizationResource.getAuthorizationID(), newAuthorizationStatus);
        }
        Assert.assertNotNull(updatedAuthorizationResource.getUpdatedTime());
        Assert.assertNotNull(updatedAuthorizationResource.getAuthorizationID());
        Assert.assertNotNull(updatedAuthorizationResource.getAuthorizationStatus());
    }

    @Test (expectedExceptions = OBConsentDataUpdationException.class)
    public void testUpdateAuthorizationStatusSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.updateAuthorizationStatus(mockedConnection, ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_ID,
                ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test (dataProvider = "updateAuthorizationStatusDataProvider",
            expectedExceptions = OBConsentDataUpdationException.class)
    public void testUpdateAuthorizationStatusWithUnmatchedAuthID(String newAuthorizationStatus) throws Exception {

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            consentCoreDAO.updateAuthorizationStatus(connection,
                    ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_ID, newAuthorizationStatus);
        }
    }

    @DataProvider(name = "updateAuthorizationUserDataProvider")
    public Object[][] updateAuthorizationUsersData() {

        /*
         * newAuthorizationUser
         */
        return ConsentMgtDAOTestData.DataProviders.CONSENT_AUTHORIZATION_USER_UPDATE_DATA_HOLDER;
    }

    @Test (dataProvider = "updateAuthorizationUserDataProvider")
    public void testUpdateAuthorizationUser(String newUserID) throws Exception {

        ConsentResource storedConsentResource;
        AuthorizationResource authorizationResource;
        AuthorizationResource storedAuthorizationResource;
        AuthorizationResource updatedAuthorizationResource;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            authorizationResource = ConsentMgtDAOTestData
                    .getSampleTestAuthorizationResource(storedConsentResource.getConsentID());

            storedAuthorizationResource = consentCoreDAO.storeAuthorizationResource(connection, authorizationResource);

            updatedAuthorizationResource = consentCoreDAO.updateAuthorizationUser(connection,
                    storedAuthorizationResource.getAuthorizationID(), newUserID);
        }
        Assert.assertNotNull(updatedAuthorizationResource.getUserID());
        Assert.assertNotNull(updatedAuthorizationResource.getUpdatedTime());
        Assert.assertNotNull(updatedAuthorizationResource.getAuthorizationID());
    }

    @Test (expectedExceptions = OBConsentDataUpdationException.class)
    public void testUpdateAuthorizationUserSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.updateAuthorizationUser(mockedConnection, ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_ID,
                ConsentMgtDAOTestData.SAMPLE_USER_ID);
    }

    @Test (dataProvider = "updateAuthorizationUserDataProvider",
            expectedExceptions = OBConsentDataUpdationException.class)
    public void testUpdateAuthorizationUserWithUnmatchedAuthID(String newUserID) throws Exception {

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            consentCoreDAO.updateAuthorizationUser(connection, ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_ID,
                    newUserID);
        }
    }

    @Test
    public void testRetrieveConsentResource() throws Exception {

        ConsentResource storedConsentResource;
        ConsentResource retrievedConsentResource;
        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
            retrievedConsentResource = consentCoreDAO.getConsentResource(connection,
                    storedConsentResource.getConsentID());
        }

        Assert.assertNotNull(retrievedConsentResource);
        Assert.assertEquals(retrievedConsentResource.getConsentID(), storedConsentResource.getConsentID());
        Assert.assertNotNull(retrievedConsentResource.getConsentID());
        Assert.assertNotNull(retrievedConsentResource.getClientID());
        Assert.assertNotNull(retrievedConsentResource.getConsentType());
        Assert.assertEquals(consentResource.getConsentFrequency(), storedConsentResource.getConsentFrequency());
        Assert.assertNotNull(retrievedConsentResource.getValidityPeriod());
        Assert.assertTrue(retrievedConsentResource.isRecurringIndicator());
        Assert.assertNotNull(retrievedConsentResource.getCreatedTime());
        Assert.assertNotNull(retrievedConsentResource.getCurrentStatus());
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentResourceResultSetError() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getConsentResource(mockedConnection, Mockito.anyString());
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentResourceSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.getConsentResource(mockedConnection, Mockito.anyObject());
    }

    @Test(expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentResourceWithUnmatchedConsentID() throws Exception {

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
             consentCoreDAO.getConsentResource(connection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID);
        }
    }

    @Test
    public void testRetrieveDetailedConsentResource() throws Exception {

        ConsentResource storedConsentResource;
        AuthorizationResource storedAuthorizationResource;
        ConsentMappingResource storedConsentMappingResource;
        DetailedConsentResource retrievedDetailedConsentResource;

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storedConsentResource = consentCoreDAO.storeConsentResource(connection,
                    ConsentMgtDAOTestData.getSampleTestConsentResource());
             consentCoreDAO.storeConsentAttributes(connection,
                    ConsentMgtDAOTestData.getSampleTestConsentAttributesObject(storedConsentResource.getConsentID()));
            storedAuthorizationResource = consentCoreDAO.storeAuthorizationResource(connection,
                    ConsentMgtDAOTestData.getSampleTestAuthorizationResource(storedConsentResource.getConsentID()));
            storedConsentMappingResource = consentCoreDAO.storeConsentMappingResource(connection,
                    ConsentMgtDAOTestData.getSampleTestConsentMappingResource(storedAuthorizationResource
                            .getAuthorizationID()));
            retrievedDetailedConsentResource = consentCoreDAO.getDetailedConsentResource(connection,
                    storedConsentResource.getConsentID(), false);
        }

        Assert.assertNotNull(retrievedDetailedConsentResource);
        Assert.assertEquals(retrievedDetailedConsentResource.getConsentID(), storedConsentResource.getConsentID());
        Assert.assertEquals(retrievedDetailedConsentResource.getConsentID(), storedConsentResource.getConsentID());
        Assert.assertEquals(retrievedDetailedConsentResource.getClientID(), storedConsentResource.getClientID());
        Assert.assertEquals(retrievedDetailedConsentResource.getReceipt(), storedConsentResource.getReceipt());
        Assert.assertEquals(retrievedDetailedConsentResource.getConsentType(), storedConsentResource.getConsentType());
        Assert.assertEquals(retrievedDetailedConsentResource.getCurrentStatus(),
                storedConsentResource.getCurrentStatus());
        Assert.assertEquals(retrievedDetailedConsentResource.getConsentFrequency(),
                storedConsentResource.getConsentFrequency());
        Assert.assertEquals(retrievedDetailedConsentResource.getValidityPeriod(),
                storedConsentResource.getValidityPeriod());
        Assert.assertEquals(retrievedDetailedConsentResource.isRecurringIndicator(),
                storedConsentResource.isRecurringIndicator());
        Assert.assertNotNull(retrievedDetailedConsentResource.getConsentAttributes());
        Assert.assertEquals(retrievedDetailedConsentResource.getAuthorizationResources().get(0).getAuthorizationID(),
                storedAuthorizationResource.getAuthorizationID());
        Assert.assertEquals(retrievedDetailedConsentResource.getConsentMappingResources().get(0).getMappingID(),
                storedConsentMappingResource.getMappingID());
    }

    @Test
    public void testRetrieveDetailedConsentResourceWithoutAttributes() throws Exception {

        ConsentResource storedConsentResource;
        AuthorizationResource storedAuthorizationResource;
        ConsentMappingResource storedConsentMappingResource;
        DetailedConsentResource retrievedDetailedConsentResource;

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storedConsentResource = consentCoreDAO.storeConsentResource(connection,
                    ConsentMgtDAOTestData.getSampleTestConsentResource());
            storedAuthorizationResource = consentCoreDAO.storeAuthorizationResource(connection,
                    ConsentMgtDAOTestData.getSampleTestAuthorizationResource(storedConsentResource.getConsentID()));
            storedConsentMappingResource = consentCoreDAO.storeConsentMappingResource(connection,
                    ConsentMgtDAOTestData.getSampleTestConsentMappingResource(storedAuthorizationResource
                            .getAuthorizationID()));
            retrievedDetailedConsentResource = consentCoreDAO.getDetailedConsentResource(connection,
                    storedConsentResource.getConsentID(), false);
        }

        Assert.assertNotNull(retrievedDetailedConsentResource);
        Assert.assertEquals(retrievedDetailedConsentResource.getConsentID(), storedConsentResource.getConsentID());
        Assert.assertEquals(retrievedDetailedConsentResource.getConsentID(), storedConsentResource.getConsentID());
        Assert.assertEquals(retrievedDetailedConsentResource.getClientID(), storedConsentResource.getClientID());
        Assert.assertEquals(retrievedDetailedConsentResource.getReceipt(), storedConsentResource.getReceipt());
        Assert.assertEquals(retrievedDetailedConsentResource.getConsentType(), storedConsentResource.getConsentType());
        Assert.assertEquals(retrievedDetailedConsentResource.getCurrentStatus(),
                storedConsentResource.getCurrentStatus());
        Assert.assertEquals(retrievedDetailedConsentResource.getConsentFrequency(),
                storedConsentResource.getConsentFrequency());
        Assert.assertEquals(retrievedDetailedConsentResource.getValidityPeriod(),
                storedConsentResource.getValidityPeriod());
        Assert.assertEquals(retrievedDetailedConsentResource.isRecurringIndicator(),
                storedConsentResource.isRecurringIndicator());
        Assert.assertNotNull(retrievedDetailedConsentResource.getConsentAttributes());
        Assert.assertEquals(retrievedDetailedConsentResource.getAuthorizationResources().get(0).getAuthorizationID(),
                storedAuthorizationResource.getAuthorizationID());
        Assert.assertEquals(retrievedDetailedConsentResource.getConsentMappingResources().get(0).getMappingID(),
                storedConsentMappingResource.getMappingID());
    }

    @Test
    public void testRetrieveDetailedConsentResourceWithMultipleConsentAttributeKeys() throws Exception {

        ConsentResource storedConsentResource;
        AuthorizationResource storedAuthorizationResourceOne;
        AuthorizationResource storedAuthorizationResourceTwo;
        DetailedConsentResource retrievedDetailedConsentResource;
        String accountIdOne = "123456";
        String accountIdTwo = "789123";

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storedConsentResource = consentCoreDAO.storeConsentResource(connection,
                    ConsentMgtDAOTestData.getSampleTestConsentResource());
            consentCoreDAO.storeConsentAttributes(connection,
                    ConsentMgtDAOTestData.getSampleTestConsentAttributesObject(storedConsentResource.getConsentID()));
            // create two auth resources for same consent id
            storedAuthorizationResourceOne = consentCoreDAO.storeAuthorizationResource(connection,
                    ConsentMgtDAOTestData.getSampleTestAuthorizationResource(storedConsentResource.getConsentID()));
            storedAuthorizationResourceTwo = consentCoreDAO.storeAuthorizationResource(connection,
                    ConsentMgtDAOTestData.getSampleTestAuthorizationResource(storedConsentResource.getConsentID()));
            // create a total of three mapping resources for created auth resources
            // mapping resources for first auth resource with two account ids
            consentCoreDAO.storeConsentMappingResource(connection,
                    ConsentMgtDAOTestData
                            .getSampleTestConsentMappingResourceWithAccountId(storedAuthorizationResourceOne
                                    .getAuthorizationID(), accountIdOne));
            consentCoreDAO.storeConsentMappingResource(connection,
                    ConsentMgtDAOTestData
                            .getSampleTestConsentMappingResourceWithAccountId(storedAuthorizationResourceOne
                                    .getAuthorizationID(), accountIdTwo));
            // mapping resource for second auth resource with a single account id
            consentCoreDAO.storeConsentMappingResource(connection,
                    ConsentMgtDAOTestData
                            .getSampleTestConsentMappingResourceWithAccountId(storedAuthorizationResourceTwo
                                    .getAuthorizationID(), accountIdOne));
            retrievedDetailedConsentResource = consentCoreDAO.getDetailedConsentResource(connection,
                    storedConsentResource.getConsentID(), false);
        }

        Assert.assertNotNull(retrievedDetailedConsentResource);
        Assert.assertEquals(retrievedDetailedConsentResource.getConsentID(), storedConsentResource.getConsentID());
        Assert.assertEquals(retrievedDetailedConsentResource.getAuthorizationResources().get(0).getAuthorizationID(),
                storedAuthorizationResourceOne.getAuthorizationID());
        Assert.assertEquals(retrievedDetailedConsentResource.getAuthorizationResources().get(1).getAuthorizationID(),
                storedAuthorizationResourceTwo.getAuthorizationID());
        /* according to the created consent resource,  retrieved consent resource should contain two auth resources and
        three mapping resources
         */
        Assert.assertEquals(retrievedDetailedConsentResource.getAuthorizationResources().size(), 2);
        Assert.assertEquals(retrievedDetailedConsentResource.getConsentMappingResources().size(), 3);
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveDetailedConsentResourceError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.getDetailedConsentResource(mockedConnection, Mockito.anyString(), false);
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveDetailedConsentResourceRetrieveError() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(mockedResultSet).when(mockedPreparedStatement).executeQuery();
        Mockito.doReturn(false).when(mockedResultSet).next();
        consentCoreDAO.getDetailedConsentResource(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID, false);
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveDetailedConsentResourceResultSetError() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getDetailedConsentResource(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID, false);
    }

    @Test
    public void testRetrieveConsentWithAttributesResource() throws Exception {

        ConsentAttributes consentAttributesResource;
        ConsentResource retrievedConsentResource;
        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            consentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            consentAttributesResource = ConsentMgtDAOTestData
                    .getSampleTestConsentAttributesObject(consentResource.getConsentID());
            consentCoreDAO.storeConsentAttributes(connection, consentAttributesResource);

            retrievedConsentResource = consentCoreDAO.getConsentResourceWithAttributes(connection,
                    consentResource.getConsentID());
        }
        Assert.assertNotNull(retrievedConsentResource);
        Assert.assertEquals(retrievedConsentResource.getConsentID(), consentResource.getConsentID());
        Assert.assertNotNull(retrievedConsentResource.getConsentID());
        Assert.assertNotNull(retrievedConsentResource.getClientID());
        Assert.assertNotNull(retrievedConsentResource.getConsentType());
        Assert.assertEquals(consentResource.getConsentFrequency(), retrievedConsentResource.getConsentFrequency());
        Assert.assertNotNull(retrievedConsentResource.getValidityPeriod());
        Assert.assertTrue(retrievedConsentResource.isRecurringIndicator());
        Assert.assertNotNull(retrievedConsentResource.getCreatedTime());
        Assert.assertNotNull(retrievedConsentResource.getCurrentStatus());
        Assert.assertNotNull(retrievedConsentResource.getConsentAttributes());

    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentWithAttributesResourceResultRetrieveError() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());
        Mockito.doReturn(mockedResultSet).when(mockedPreparedStatement).executeQuery();
        Mockito.doReturn(false).when(mockedResultSet).next();
        consentCoreDAO.getConsentResourceWithAttributes(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID);
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentWithAttributesResourceResultSetError() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getConsentResourceWithAttributes(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID);
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentWithAttributesResourceSQLError() throws Exception {


        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString(),
                Mockito.anyInt(), Mockito.anyInt());
        consentCoreDAO.getConsentResourceWithAttributes(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID);
    }

    @Test (dataProvider = "storeConsentFileDataProvider")
    public void testRetrieveConsentFileResource(String consentFile) throws Exception {

        ConsentFile consentFileResource;
        ConsentFile retrievedConsentFileResource;
        ConsentResource retrievedConsentResource;
        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            consentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
            retrievedConsentResource = consentCoreDAO.getConsentResource(connection, consentResource.getConsentID());

            consentFileResource = new ConsentFile();
            consentFileResource.setConsentID(retrievedConsentResource.getConsentID());
            consentFileResource.setConsentFile(consentFile);

            consentCoreDAO.storeConsentFile(connection, consentFileResource);

            retrievedConsentFileResource = consentCoreDAO.getConsentFile(connection,
                    consentFileResource.getConsentID(), false);
        }
        Assert.assertNotNull(retrievedConsentFileResource.getConsentID());
        Assert.assertNotNull(retrievedConsentFileResource.getConsentFile());
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentFileResourceNoRecordsFoundError() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(mockedResultSet).when(mockedPreparedStatement).executeQuery();
        Mockito.doReturn(false).when(mockedResultSet).next();
        consentCoreDAO.getConsentFile(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID, false);
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentFileResourceSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.getConsentFile(mockedConnection, Mockito.anyObject(), false);
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentFileResourceRetrieveError() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getConsentFile(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID, false);
    }

    @Test(expectedExceptions = OBConsentDataRetrievalException.class, dataProvider = "storeConsentFileDataProvider")
    public void testRetrieveConsentFileResourceWithUnmatchedConsentID(String consentFile) throws Exception {

        ConsentFile consentFileResource;
        ConsentResource retrievedConsentResource;
        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            consentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
            retrievedConsentResource = consentCoreDAO.getConsentResource(connection, consentResource.getConsentID());

            consentFileResource = new ConsentFile();
            consentFileResource.setConsentID(retrievedConsentResource.getConsentID());
            consentFileResource.setConsentFile(consentFile);

            consentCoreDAO.storeConsentFile(connection, consentFileResource);
            consentCoreDAO.getConsentFile(connection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID, false);
        }
    }

    @DataProvider(name = "getConsentAttributesDataProvider")
    public Object[][] getConsentAttributesData() {

        /*
         * consentAttributeKeys
         */
        return ConsentMgtDAOTestData.DataProviders.CONSENT_ATTRIBUTES_GET_DATA_HOLDER;
    }

    @Test (dataProvider = "getConsentAttributesDataProvider")
    public void testRetrieveConsentAttributes(ArrayList<String> consentAttributeKeys) throws Exception {

        ConsentAttributes consentAttributesResource;
        ConsentAttributes retrievedConsentAttributesResource;
        ConsentResource retrievedConsentResource;
        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            consentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
            retrievedConsentResource = consentCoreDAO.getConsentResource(connection, consentResource.getConsentID());

            consentAttributesResource = ConsentMgtDAOTestData
                    .getSampleTestConsentAttributesObject(retrievedConsentResource.getConsentID());

            consentCoreDAO.storeConsentAttributes(connection, consentAttributesResource);

            retrievedConsentAttributesResource = consentCoreDAO.getConsentAttributes(connection,
                    retrievedConsentResource.getConsentID(), consentAttributeKeys);
        }
        Assert.assertNotNull(retrievedConsentAttributesResource.getConsentID());
        Assert.assertNotNull(retrievedConsentAttributesResource.getConsentAttributes());
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentAttributesRetrieveError() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(mockedResultSet).when(mockedPreparedStatement).executeQuery();
        Mockito.doReturn(false).when(mockedResultSet).isBeforeFirst();
        consentCoreDAO.getConsentAttributes(mockedConnection, Mockito.anyString());
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentAttributesResultSetError() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getConsentAttributes(mockedConnection, Mockito.anyString());
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentAttributesSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.getConsentAttributes(mockedConnection, Mockito.anyObject());
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentAttributesResultSetErrorOverloadedMethod() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getConsentAttributes(mockedConnection, Mockito.anyString(),
                ConsentMgtDAOTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentAttributesSQLErrorOverloadedMethod() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.getConsentAttributes(mockedConnection, Mockito.anyObject());
    }

    @Test
    public void testRetrieveConsentAttributesWithNoKeys() throws Exception {

        ConsentAttributes consentAttributesResource;
        ConsentAttributes retrievedConsentAttributesResource;
        ConsentResource retrievedConsentResource;
        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            consentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
            retrievedConsentResource = consentCoreDAO.getConsentResource(connection, consentResource.getConsentID());

            consentAttributesResource = ConsentMgtDAOTestData
                    .getSampleTestConsentAttributesObject(retrievedConsentResource.getConsentID());

            consentCoreDAO.storeConsentAttributes(connection, consentAttributesResource);

            retrievedConsentAttributesResource = consentCoreDAO.getConsentAttributes(connection,
                    retrievedConsentResource.getConsentID());
        }
        Assert.assertNotNull(retrievedConsentAttributesResource.getConsentID());
        Assert.assertNotNull(retrievedConsentAttributesResource.getConsentAttributes());
    }

    @Test (dataProvider = "getConsentAttributesDataProvider",
            expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentAttributesWithUnmatchedConsentID(ArrayList<String> consentAttributeKeys)
            throws Exception {

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            consentCoreDAO.getConsentAttributes(connection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                    consentAttributeKeys);
        }
    }

    @Test
    public void testRetrieveConsentAttributesByName() throws Exception {

        ConsentAttributes consentAttributesResource;
        Map<String, String> retrievedValuesMap;
        ConsentResource retrievedConsentResource;
        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            consentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
            retrievedConsentResource = consentCoreDAO.getConsentResource(connection, consentResource.getConsentID());

            consentAttributesResource = ConsentMgtDAOTestData
                    .getSampleTestConsentAttributesObject(retrievedConsentResource.getConsentID());

            consentCoreDAO.storeConsentAttributes(connection, consentAttributesResource);

            retrievedValuesMap = consentCoreDAO.getConsentAttributesByName(connection,
                    "x-request-id");

        }
        Assert.assertTrue(retrievedValuesMap.containsKey(consentAttributesResource.getConsentID()));
        Assert.assertTrue(retrievedValuesMap.containsValue(consentAttributesResource.getConsentAttributes().get("x" +
                "-request-id")));
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentAttributesByNameSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.getConsentAttributesByName(mockedConnection, Mockito.anyObject());
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentAttributesByNameResultSetError() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getConsentAttributesByName(mockedConnection, Mockito.anyString());
    }

    @Test
    public void testRetrieveAuthorizationResource() throws Exception {

        ConsentResource storedConsentResource;
        AuthorizationResource authorizationResource;
        AuthorizationResource storedAuthorizationResource;
        AuthorizationResource retrievedAuthorizationResource;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            authorizationResource = ConsentMgtDAOTestData
                    .getSampleTestAuthorizationResource(storedConsentResource.getConsentID());

            storedAuthorizationResource = consentCoreDAO.storeAuthorizationResource(connection, authorizationResource);

            retrievedAuthorizationResource = consentCoreDAO.getAuthorizationResource(connection,
                    storedAuthorizationResource.getAuthorizationID());
        }
        Assert.assertNotNull(retrievedAuthorizationResource.getUpdatedTime());
        Assert.assertNotNull(retrievedAuthorizationResource.getAuthorizationID());
        Assert.assertNotNull(retrievedAuthorizationResource.getAuthorizationStatus());
        Assert.assertNotNull(retrievedAuthorizationResource.getUserID());
        Assert.assertNotNull(retrievedAuthorizationResource.getAuthorizationType());
        Assert.assertEquals(retrievedAuthorizationResource.getConsentID(), storedAuthorizationResource.getConsentID());
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveAuthorizationResourceResultSetError() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getAuthorizationResource(mockedConnection, ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_ID);
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveAuthorizationResourceSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.getAuthorizationResource(mockedConnection, ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_ID);
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveAuthorizationResourceWithUnmatchedAuthID() throws Exception {

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            consentCoreDAO.getAuthorizationResource(connection,
                    ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_ID);
        }
    }

    @Test
    public void testRetrieveConsentStatusAuditRecordsWithConsentID() throws Exception {

        ConsentResource storedConsentResource;
        ConsentStatusAuditRecord consentStatusAuditRecord;
        ConsentStatusAuditRecord storedConsentStatusAuditRecord;
        ArrayList<ConsentStatusAuditRecord> retrievedConsentStatusAuditRecords;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            consentStatusAuditRecord = ConsentMgtDAOTestData
                    .getSampleTestConsentStatusAuditRecord(storedConsentResource.getConsentID(),
                    storedConsentResource.getCurrentStatus());

            storedConsentStatusAuditRecord = consentCoreDAO.storeConsentStatusAuditRecord(connection,
                    consentStatusAuditRecord);

            connection.commit();

            retrievedConsentStatusAuditRecords = consentCoreDAO.getConsentStatusAuditRecords(connection,
                    storedConsentStatusAuditRecord.getConsentID(), null,
                    null, null,
                    null, null , false);
        }
        Assert.assertNotNull(retrievedConsentStatusAuditRecords);
        for (ConsentStatusAuditRecord record:
                retrievedConsentStatusAuditRecords) {
            Assert.assertEquals(storedConsentStatusAuditRecord.getConsentID(), record.getConsentID());
        }
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentStatusAuditRecordsResultSetError() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getConsentStatusAuditRecords(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                ConsentMgtDAOTestData.SAMPLE_CURRENT_STATUS, ConsentMgtDAOTestData.SAMPLE_ACTION_BY,
                ConsentMgtDAOTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtDAOTestData.SAMPLE_CONSENT_VALIDITY_PERIOD, ConsentMgtDAOTestData.SAMPLE_AUDIT_ID, false);
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentStatusAuditRecordsSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.getConsentStatusAuditRecords(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                ConsentMgtDAOTestData.SAMPLE_CURRENT_STATUS, ConsentMgtDAOTestData.SAMPLE_ACTION_BY,
                ConsentMgtDAOTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtDAOTestData.SAMPLE_CONSENT_VALIDITY_PERIOD, ConsentMgtDAOTestData.SAMPLE_AUDIT_ID, false);
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentStatusAuditRecordByConsentIDWithUnmatchedConsentID() throws Exception {

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
             consentCoreDAO.getConsentStatusAuditRecords(connection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                     null, null, null, null, null, false);
        }
    }

    @Test
    public void testRetrieveConsentStatusAuditRecordsByConsentIDAndStatus() throws Exception {

        ConsentResource storedConsentResource;
        ConsentStatusAuditRecord consentStatusAuditRecord;
        ConsentStatusAuditRecord storedConsentStatusAuditRecord;
        ArrayList<ConsentStatusAuditRecord> retrievedConsentStatusAuditRecords;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            consentStatusAuditRecord = ConsentMgtDAOTestData
                    .getSampleTestConsentStatusAuditRecord(storedConsentResource.getConsentID(),
                    storedConsentResource.getCurrentStatus());

            storedConsentStatusAuditRecord = consentCoreDAO.storeConsentStatusAuditRecord(connection,
                    consentStatusAuditRecord);

            retrievedConsentStatusAuditRecords = consentCoreDAO.getConsentStatusAuditRecords(connection,
                    storedConsentStatusAuditRecord.getConsentID(), storedConsentStatusAuditRecord.getCurrentStatus(),
                    null, null, null, null, false);
        }
        Assert.assertNotNull(retrievedConsentStatusAuditRecords);
        for (ConsentStatusAuditRecord record:
                retrievedConsentStatusAuditRecords) {
            Assert.assertEquals(storedConsentStatusAuditRecord.getConsentID(), record.getConsentID());
            Assert.assertEquals(storedConsentStatusAuditRecord.getCurrentStatus(), record.getCurrentStatus());
        }
    }

    @Test
    public void testRetrieveConsentStatusAuditRecordsByConsentIDStatusAndActionBy() throws Exception {

        ConsentResource storedConsentResource;
        ConsentStatusAuditRecord consentStatusAuditRecord;
        ConsentStatusAuditRecord storedConsentStatusAuditRecord;
        ArrayList<ConsentStatusAuditRecord> retrievedConsentStatusAuditRecords;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            consentStatusAuditRecord = ConsentMgtDAOTestData
                    .getSampleTestConsentStatusAuditRecord(storedConsentResource.getConsentID(),
                    storedConsentResource.getCurrentStatus());

            storedConsentStatusAuditRecord = consentCoreDAO.storeConsentStatusAuditRecord(connection,
                    consentStatusAuditRecord);

            retrievedConsentStatusAuditRecords = consentCoreDAO.getConsentStatusAuditRecords(connection,
                    storedConsentStatusAuditRecord.getConsentID(), storedConsentStatusAuditRecord.getCurrentStatus(),
                    storedConsentStatusAuditRecord.getActionBy(), null,
                    null, null, false);
        }
        Assert.assertNotNull(retrievedConsentStatusAuditRecords);
        for (ConsentStatusAuditRecord record:
                retrievedConsentStatusAuditRecords) {
            Assert.assertEquals(storedConsentStatusAuditRecord.getConsentID(), record.getConsentID());
            Assert.assertEquals(storedConsentStatusAuditRecord.getCurrentStatus(), record.getCurrentStatus());
            Assert.assertEquals(storedConsentStatusAuditRecord.getActionBy(), record.getActionBy());
        }
    }

    @Test
    public void testRetrieveConsentAuditRecordByAuditRecordID() throws Exception {

        ConsentResource storedConsentResource;
        ConsentStatusAuditRecord consentStatusAuditRecord;
        ConsentStatusAuditRecord storedConsentStatusAuditRecord;
        ArrayList<ConsentStatusAuditRecord> retrievedConsentStatusAuditRecords;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            consentStatusAuditRecord = ConsentMgtDAOTestData
                    .getSampleTestConsentStatusAuditRecord(storedConsentResource.getConsentID(),
                    storedConsentResource.getCurrentStatus());

            storedConsentStatusAuditRecord = consentCoreDAO.storeConsentStatusAuditRecord(connection,
                    consentStatusAuditRecord);

            retrievedConsentStatusAuditRecords = consentCoreDAO.getConsentStatusAuditRecords(connection,
                    null, null, null, null, null,
                    storedConsentStatusAuditRecord.getStatusAuditID(), false);
        }
        Assert.assertNotNull(retrievedConsentStatusAuditRecords);
        for (ConsentStatusAuditRecord record:
                retrievedConsentStatusAuditRecords) {
            Assert.assertEquals(storedConsentStatusAuditRecord.getConsentID(), record.getConsentID());
            Assert.assertEquals(storedConsentStatusAuditRecord.getStatusAuditID(), record.getStatusAuditID());
        }
    }

    @Test
    public void testRetrieveConsentAuditRecordForGivenTime() throws Exception {

        long fromTime;
        long toTime;
        ConsentResource storedConsentResource;
        ConsentStatusAuditRecord consentStatusAuditRecord;
        ConsentStatusAuditRecord storedConsentStatusAuditRecord;
        ArrayList<ConsentStatusAuditRecord> retrievedConsentStatusAuditRecords;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            consentStatusAuditRecord = ConsentMgtDAOTestData
                    .getSampleTestConsentStatusAuditRecord(storedConsentResource.getConsentID(),
                    storedConsentResource.getCurrentStatus());

            storedConsentStatusAuditRecord = consentCoreDAO.storeConsentStatusAuditRecord(connection,
                    consentStatusAuditRecord);

            fromTime = Long.sum(storedConsentStatusAuditRecord.getActionTime(), -60);
            toTime = Long.sum(storedConsentStatusAuditRecord.getActionTime(), 60);

            retrievedConsentStatusAuditRecords = consentCoreDAO.getConsentStatusAuditRecords(connection,
                    storedConsentStatusAuditRecord.getConsentID(), null,
                    null, fromTime, toTime, null, false);
        }
        Assert.assertNotNull(retrievedConsentStatusAuditRecords);
        for (ConsentStatusAuditRecord record:
                retrievedConsentStatusAuditRecords) {
            Assert.assertEquals(storedConsentResource.getConsentID(), record.getConsentID());
            Assert.assertTrue((record.getActionTime() >= fromTime) && (record.getActionTime() <= toTime));
        }
    }

    @Test
    public void testRetrieveConsentMappingResource() throws Exception {

        ConsentResource storedConsentResource;
        AuthorizationResource authorizationResource;
        AuthorizationResource storedAuthorizationResource;
        ConsentMappingResource consentMappingResource;
        ArrayList<ConsentMappingResource> retrievedConsentMappingResources;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            authorizationResource = ConsentMgtDAOTestData
                    .getSampleTestAuthorizationResource(storedConsentResource.getConsentID());

            storedAuthorizationResource = consentCoreDAO.storeAuthorizationResource(connection,
                    authorizationResource);

            consentMappingResource =
                    ConsentMgtDAOTestData
                            .getSampleTestConsentMappingResource(storedAuthorizationResource.getAuthorizationID());

            consentCoreDAO.storeConsentMappingResource(connection,
                    consentMappingResource);

            retrievedConsentMappingResources = consentCoreDAO.getConsentMappingResources(connection,
                    storedAuthorizationResource.getAuthorizationID());
        }
        Assert.assertNotNull(retrievedConsentMappingResources);
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentMappingResourceResultSetError() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getConsentMappingResources(mockedConnection, ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_ID);
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentMappingResourceSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.getConsentMappingResources(mockedConnection, ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_ID);
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentMappingResourceRetrieveErrorOverloadedMethod() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(mockedResultSet).when(mockedPreparedStatement).executeQuery();
        Mockito.doReturn(false).when(mockedResultSet).isBeforeFirst();
        consentCoreDAO.getConsentMappingResources(mockedConnection, Mockito.anyString(),
                ConsentMgtDAOTestData.SAMPLE_MAPPING_STATUS);
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentMappingResourceResultSetErrorOverloadedMethod() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getConsentMappingResources(mockedConnection, ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_ID,
                ConsentMgtDAOTestData.SAMPLE_MAPPING_STATUS);
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentMappingResourceSQLErrorOverloadedMethod() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.getConsentMappingResources(mockedConnection, ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_ID,
                ConsentMgtDAOTestData.SAMPLE_MAPPING_STATUS);
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentMappingResourceWithUnmatchedAuthID() throws Exception {

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            consentCoreDAO.getConsentMappingResources(connection,
                    ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_ID);
        }
    }

    @Test
    public void testRetrieveConsentMappingResourceWithMappingStatus() throws Exception {

        ConsentResource storedConsentResource;
        AuthorizationResource authorizationResource;
        AuthorizationResource storedAuthorizationResource;
        ConsentMappingResource storedConsentMappingResource;
        ConsentMappingResource consentMappingResource;
        ArrayList<ConsentMappingResource> retrievedConsentMappingResources;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            authorizationResource = ConsentMgtDAOTestData
                    .getSampleTestAuthorizationResource(storedConsentResource.getConsentID());

            storedAuthorizationResource = consentCoreDAO.storeAuthorizationResource(connection,
                    authorizationResource);

            consentMappingResource =
                    ConsentMgtDAOTestData
                            .getSampleTestConsentMappingResource(storedAuthorizationResource.getAuthorizationID());

            storedConsentMappingResource = consentCoreDAO.storeConsentMappingResource(connection,
                    consentMappingResource);

            retrievedConsentMappingResources = consentCoreDAO.getConsentMappingResources(connection,
                    storedAuthorizationResource.getAuthorizationID(), storedConsentMappingResource.getMappingStatus());
        }
        Assert.assertNotNull(retrievedConsentMappingResources);
    }

    @Test
    public void testDeleteConsentAttribute() throws Exception {

        ConsentResource storedConsentResource;
        ConsentAttributes consentAttributesResource;
        boolean isDeleted;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            consentAttributesResource = ConsentMgtDAOTestData
                    .getSampleTestConsentAttributesObject(storedConsentResource.getConsentID());

            consentCoreDAO.storeConsentAttributes(connection, consentAttributesResource);

            isDeleted = consentCoreDAO.deleteConsentAttributes(connection, storedConsentResource.getConsentID(),
                    ConsentMgtDAOTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
        }
        Assert.assertTrue(isDeleted);
    }

    @Test
    public void testConsentSearchWithConsentIDsList() throws Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources;
        ArrayList<String> consentIDs = new ArrayList<>();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storeDataForConsentSearchTest(consentIDs, connection);
            detailedConsentResources = consentCoreDAO.searchConsents(connection, consentIDs, null,
                    null, null, null, null, null,
                    10, 0);
        }

        Assert.assertNotNull(detailedConsentResources);
        for (DetailedConsentResource resource : detailedConsentResources) {
            Assert.assertNotNull(resource.getAuthorizationResources());
            Assert.assertNotNull(resource.getConsentMappingResources());
            Assert.assertNotNull(resource.getConsentAttributes());

            for (AuthorizationResource authResource : resource.getAuthorizationResources()) {
                Assert.assertEquals(resource.getConsentID(), authResource.getConsentID());
            }
        }
    }

    @Test
    public void testConsentSearchWithConsentIDsListAndTime() throws Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources;
        ArrayList<String> consentIDs = new ArrayList<>();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storeDataForConsentSearchTest(consentIDs, connection);
            detailedConsentResources = consentCoreDAO.searchConsents(connection, consentIDs, null,
                    null, null, null, 1669917425L, 1669917425L,
                    10, 0);
        }

        Assert.assertNotNull(detailedConsentResources);

    }

    @Test
    public void testConsentSearchWithClientIDsList() throws Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources;
        ArrayList<String> consentIDs = new ArrayList<>();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storeDataForConsentSearchTest(consentIDs, connection);
            detailedConsentResources = consentCoreDAO.searchConsents(connection, null,
                    ConsentMgtDAOTestData.SAMPLE_CLIENT_IDS_LIST, null, null, null,
                    null, null, 10, 0);
        }

        Assert.assertNotNull(detailedConsentResources);
        for (DetailedConsentResource resource : detailedConsentResources) {
            Assert.assertNotNull(resource.getAuthorizationResources());
            Assert.assertNotNull(resource.getConsentMappingResources());
            Assert.assertNotNull(resource.getConsentAttributes());

            for (AuthorizationResource authResource : resource.getAuthorizationResources()) {
                Assert.assertEquals(resource.getConsentID(), authResource.getConsentID());
            }
        }
    }

    @Test
    public void testConsentSearchWithConsentStatusesList() throws Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources;
        ArrayList<String> consentIDs = new ArrayList<>();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storeDataForConsentSearchTest(consentIDs, connection);
            detailedConsentResources = consentCoreDAO.searchConsents(connection, null, null,
                    null, ConsentMgtDAOTestData.SAMPLE_CONSENT_STATUSES_LIST, null, null,
                    null, 10, 0);
        }

        Assert.assertNotNull(detailedConsentResources);
        for (DetailedConsentResource resource : detailedConsentResources) {
            Assert.assertNotNull(resource.getAuthorizationResources());
            Assert.assertNotNull(resource.getConsentMappingResources());
            Assert.assertNotNull(resource.getConsentAttributes());

            for (AuthorizationResource authResource : resource.getAuthorizationResources()) {
                Assert.assertEquals(resource.getConsentID(), authResource.getConsentID());
            }
        }
    }

    @Test
    public void testConsentSearchWithConsentTypesList() throws Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources;
        ArrayList<String> consentIDs = new ArrayList<>();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storeDataForConsentSearchTest(consentIDs, connection);
            detailedConsentResources = consentCoreDAO.searchConsents(connection, null, null,
                    null, ConsentMgtDAOTestData.SAMPLE_CONSENT_STATUSES_LIST, null, null,
                    null, 10, 0);
        }

        Assert.assertNotNull(detailedConsentResources);
        for (DetailedConsentResource resource : detailedConsentResources) {
            Assert.assertNotNull(resource.getAuthorizationResources());
            Assert.assertNotNull(resource.getConsentMappingResources());
            Assert.assertNotNull(resource.getConsentAttributes());

            for (AuthorizationResource authResource : resource.getAuthorizationResources()) {
                Assert.assertEquals(resource.getConsentID(), authResource.getConsentID());
            }
        }
    }

    @Test
    public void testConsentSearchWithUserIDsList() throws Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources;
        ArrayList<String> consentIDs = new ArrayList<>();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storeDataForConsentSearchTest(consentIDs, connection);
            detailedConsentResources = consentCoreDAO.searchConsents(connection, null, null,
                    null, null, ConsentMgtDAOTestData.SAMPLE_USER_IDS_LIST, null,
                    null, 10, 0);
        }

        Assert.assertNotNull(detailedConsentResources);
        for (DetailedConsentResource resource : detailedConsentResources) {
            Assert.assertNotNull(resource.getAuthorizationResources());
            Assert.assertNotNull(resource.getConsentMappingResources());
            Assert.assertNotNull(resource.getConsentAttributes());

            for (AuthorizationResource authResource : resource.getAuthorizationResources()) {
                Assert.assertEquals(resource.getConsentID(), authResource.getConsentID());
            }
        }
    }

    @Test
    public void testConsentSearchWithoutLimitAndOffset() throws Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources;
        ArrayList<String> consentIDs = new ArrayList<>();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storeDataForConsentSearchTest(consentIDs, connection);
            detailedConsentResources = consentCoreDAO.searchConsents(connection, consentIDs, null,
                    null, null, null, null, null, null,
                    null);
        }

        Assert.assertNotNull(detailedConsentResources);
        for (DetailedConsentResource resource : detailedConsentResources) {
            Assert.assertNotNull(resource.getAuthorizationResources());
            Assert.assertNotNull(resource.getConsentMappingResources());
            Assert.assertNotNull(resource.getConsentAttributes());

            for (AuthorizationResource authResource : resource.getAuthorizationResources()) {
                Assert.assertEquals(resource.getConsentID(), authResource.getConsentID());
            }
        }
    }

    @Test
    public void testConsentSearchWithoutLimitButOffset() throws Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources;
        ArrayList<String> consentIDs = new ArrayList<>();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storeDataForConsentSearchTest(consentIDs, connection);
            detailedConsentResources = consentCoreDAO.searchConsents(connection, consentIDs, null,
                    null, null, null, null, null, null,
                    1);
        }

        Assert.assertNotNull(detailedConsentResources);
        for (DetailedConsentResource resource : detailedConsentResources) {
            Assert.assertNotNull(resource.getAuthorizationResources());
            Assert.assertNotNull(resource.getConsentMappingResources());
            Assert.assertNotNull(resource.getConsentAttributes());

            for (AuthorizationResource authResource : resource.getAuthorizationResources()) {
                Assert.assertEquals(resource.getConsentID(), authResource.getConsentID());
            }
        }
    }

    @Test
    public void testConsentSearchForNullValues() throws Exception {

        ResultSet mockedResultSetTemp = Mockito.mock(ResultSet.class);
        Mockito.doReturn(null).when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.AUTH_ID);
        Mockito.doReturn(null).when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.MAPPING_ID);
        ConsentCoreDAOImpl dao = new ConsentCoreDAOImpl(new ConsentMgtCommonDBQueries());
        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();
        dao.setAuthorizationDataInResponseForGroupedQuery(authorizationResources,
                mockedResultSetTemp, "");
        dao.setAccountConsentMappingDataInResponse(consentMappingResources,
                mockedResultSetTemp);
        Assert.assertTrue(authorizationResources.size() == 0);
        Assert.assertTrue(consentMappingResources.size() == 0);
    }

    @Test
    public void testConsentSearchForNoneNullValues() throws Exception {

        ResultSet mockedResultSetTemp = Mockito.mock(ResultSet.class);
        Mockito.doReturn("test").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.AUTH_ID);
        Mockito.doReturn("test").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.MAPPING_ID);
        Mockito.doReturn("test").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.ACCOUNT_ID);
        Mockito.doReturn("test").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.MAPPING_STATUS);
        Mockito.doReturn("test").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.PERMISSION);
        Mockito.doReturn("test").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.AUTH_TYPE);
        Mockito.doReturn("test").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.AUTH_STATUS);
        Mockito.doReturn("123456").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.UPDATED_TIME);
        Mockito.doReturn("test").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.USER_ID);
        ConsentCoreDAOImpl dao = new ConsentCoreDAOImpl(new ConsentMgtCommonDBQueries());
        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();
        dao.setAuthorizationDataInResponseForGroupedQuery(authorizationResources,
                mockedResultSetTemp, "");
        dao.setAccountConsentMappingDataInResponse(consentMappingResources,
                mockedResultSetTemp);
        Assert.assertTrue(authorizationResources.size() != 0);
        Assert.assertTrue(consentMappingResources.size() != 0);
    }

    @Test
    public void testConsentSearchForNoneNullValuesNegativeCase() throws Exception {

        ResultSet mockedResultSetTemp = Mockito.mock(ResultSet.class);
        Mockito.doReturn("test").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.AUTH_ID);
        Mockito.doReturn("test").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.MAPPING_ID);
        Mockito.doReturn("1,2").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.ACCOUNT_ID);
        Mockito.doReturn("test").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.MAPPING_STATUS);
        Mockito.doReturn("test").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.PERMISSION);
        Mockito.doReturn("test").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.AUTH_TYPE);
        Mockito.doReturn("test").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.AUTH_STATUS);
        Mockito.doReturn("123456").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.UPDATED_TIME);
        Mockito.doReturn("test,test2").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.USER_ID);
        ConsentCoreDAOImpl dao = new ConsentCoreDAOImpl(new ConsentMgtCommonDBQueries());
        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();
        dao.setAuthorizationDataInResponseForGroupedQuery(authorizationResources,
                mockedResultSetTemp, "");
        dao.setAccountConsentMappingDataInResponse(consentMappingResources,
                mockedResultSetTemp);
        Assert.assertTrue(authorizationResources.size() != 0);
        Assert.assertTrue(consentMappingResources.size() != 0);
    }

    @Test
    public void testConsentSearchWithoutOffsetButLimit() throws Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources;
        ArrayList<String> consentIDs = new ArrayList<>();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storeDataForConsentSearchTest(consentIDs, connection);
            detailedConsentResources = consentCoreDAO.searchConsents(connection, consentIDs, null,
                    null, null, null, null, null, 1,
                    null);
        }

        Assert.assertNotNull(detailedConsentResources);
        for (DetailedConsentResource resource : detailedConsentResources) {
            Assert.assertNotNull(resource.getAuthorizationResources());
            Assert.assertNotNull(resource.getConsentMappingResources());
            Assert.assertNotNull(resource.getConsentAttributes());

            for (AuthorizationResource authResource : resource.getAuthorizationResources()) {
                Assert.assertEquals(resource.getConsentID(), authResource.getConsentID());
            }
        }
    }

    @Test
    public void testConsentSearchWithNoParams() throws Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources;
        ArrayList<String> consentIDs = new ArrayList<>();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storeDataForConsentSearchTest(consentIDs, connection);
            detailedConsentResources = consentCoreDAO.searchConsents(connection, null, null,
                    null, null, null, null,
                    null, 10, 0);
        }

        Assert.assertNotNull(detailedConsentResources);
        for (DetailedConsentResource resource : detailedConsentResources) {
            Assert.assertNotNull(resource.getAuthorizationResources());
            Assert.assertNotNull(resource.getConsentMappingResources());
            Assert.assertNotNull(resource.getConsentAttributes());

            for (AuthorizationResource authResource : resource.getAuthorizationResources()) {
                Assert.assertEquals(resource.getConsentID(), authResource.getConsentID());
            }
        }
    }

    @Test
    public void testConsentSearchWithTimePeriod() throws Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources;
        ArrayList<String> consentIDs = new ArrayList<>();
        long currentTime = System.currentTimeMillis() / 1000;

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storeDataForConsentSearchTest(consentIDs, connection);
            detailedConsentResources = consentCoreDAO.searchConsents(connection, null, null,
                    null, null, null, currentTime,
                    currentTime + 100, 10, 0);
        }

        Assert.assertNotNull(detailedConsentResources);
        for (DetailedConsentResource resource : detailedConsentResources) {
            Assert.assertNotNull(resource.getAuthorizationResources());
            Assert.assertNotNull(resource.getConsentMappingResources());
            Assert.assertNotNull(resource.getConsentAttributes());

            for (AuthorizationResource authResource : resource.getAuthorizationResources()) {
                Assert.assertEquals(resource.getConsentID(), authResource.getConsentID());
            }

            Assert.assertTrue((currentTime <= resource.getUpdatedTime())
                    && (currentTime + 100 >= resource.getUpdatedTime()));
        }
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testSearchConsentsSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString(),
                Mockito.anyInt(), Mockito.anyInt());
        consentCoreDAO.searchConsents(mockedConnection, null, null, null,
                null, null, null, null, null, null);
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testSearchConsentsPreparedResultSetError() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.searchConsents(mockedConnection, null, null, null,
                null, null, null, null, null, null);
    }

    @Test
    public void testSearchConsentAuthorizations() throws Exception {

        ArrayList<AuthorizationResource> authorizationResources;
        ConsentResource storedConsentResource;
        AuthorizationResource storedAuthorizationResource;

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, ConsentMgtDAOTestData
                    .getSampleTestConsentResource());
            storedAuthorizationResource = consentCoreDAO.storeAuthorizationResource(connection,
                    ConsentMgtDAOTestData.getSampleTestAuthorizationResource(storedConsentResource.getConsentID()));

            authorizationResources = consentCoreDAO.searchConsentAuthorizations(connection,
                    storedConsentResource.getConsentID(), storedAuthorizationResource.getUserID());
        }

        Assert.assertNotNull(authorizationResources);
        Assert.assertEquals(storedAuthorizationResource.getAuthorizationID(),
                authorizationResources.get(0).getAuthorizationID());
        Assert.assertEquals(storedAuthorizationResource.getConsentID(),
                authorizationResources.get(0).getConsentID());
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testSearchConsentAuthorizationsSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.searchConsentAuthorizations(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                ConsentMgtDAOTestData.SAMPLE_USER_ID);
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testSearchConsentAuthorizationsResultSetError() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.searchConsentAuthorizations(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                ConsentMgtDAOTestData.SAMPLE_USER_ID);
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testSearchConsentAuthorizationsNoRecordsFoundError() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(mockedResultSet).when(mockedPreparedStatement).executeQuery();
        Mockito.doReturn(false).when(mockedResultSet).isBeforeFirst();
        consentCoreDAO.searchConsentAuthorizations(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                ConsentMgtDAOTestData.SAMPLE_USER_ID);
    }

    @Test (expectedExceptions = OBConsentDataDeletionException.class)
    public void testDeleteConsentAttributeSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.deleteConsentAttributes(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                ConsentMgtDAOTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    private void storeDataForConsentSearchTest(ArrayList<String> consentIDs,
                                               Connection connection) throws OBConsentDataInsertionException {

        ArrayList<String> authIDs = new ArrayList<>();

        // Store 3 consent resources
        ArrayList<ConsentResource> consentResources = ConsentMgtDAOTestData.getSampleConsentResourcesList();
        for (ConsentResource resource : consentResources) {
            consentIDs.add(consentCoreDAO.storeConsentResource(connection, resource).getConsentID());
        }

        // Store 2 authorization resources for each stored consent
        ArrayList<AuthorizationResource> authorizationResources =
                ConsentMgtDAOTestData.getSampleAuthorizationResourcesList(consentIDs);
        for (AuthorizationResource resource : authorizationResources) {
            authIDs.add(consentCoreDAO.storeAuthorizationResource(connection, resource).getAuthorizationID());
        }

        // Store 2 consent mapping resources for each authorization resource
        ArrayList<ConsentMappingResource> consentMappingResources =
                ConsentMgtDAOTestData.getSampleConsentMappingResourcesList(authIDs);
        for (ConsentMappingResource resource : consentMappingResources) {
            consentCoreDAO.storeConsentMappingResource(connection, resource);
        }

        // Store consent attributes
        for (String consentID : consentIDs) {
            ConsentAttributes consentAttributesResource = new ConsentAttributes();
            consentAttributesResource.setConsentID(consentID);
            consentAttributesResource.setConsentAttributes(ConsentMgtDAOTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
            consentCoreDAO.storeConsentAttributes(connection, consentAttributesResource);
        }
    }

    @Test
    public void testUpdateConsentReceipt() throws Exception {

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();
        String newConsentReceipt = "{\"amendedReceipt\":\"amendedData\"}";

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            ConsentResource storedConsentResource = consentCoreDAO.storeConsentResource(connection,
                    consentResource);
            Assert.assertTrue(consentCoreDAO.updateConsentReceipt(connection, storedConsentResource.getConsentID(),
                    newConsentReceipt));
        }
    }

    @Test (expectedExceptions = OBConsentDataUpdationException.class)
    public void testUpdateConsentReceiptSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.updateConsentReceipt(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                ConsentMgtDAOTestData.SAMPLE_CONSENT_RECEIPT);
    }

    @Test (expectedExceptions = OBConsentDataUpdationException.class)
    public void testUpdateConsentReceiptUpdateError() throws Exception {

            Mockito.doReturn(mockedPreparedStatement).when(mockedConnection).prepareStatement(Mockito.anyString());
            Mockito.doReturn(0).when(mockedPreparedStatement).executeUpdate();
            consentCoreDAO.updateConsentReceipt(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                    ConsentMgtDAOTestData.SAMPLE_CONSENT_RECEIPT);
    }

    @Test
    public void testUpdateConsentValidityTime() throws Exception {

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();
        long newConsentValidityTime = 12345;

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            ConsentResource storedConsentResource = consentCoreDAO.storeConsentResource(connection,
                    consentResource);
            Assert.assertTrue(consentCoreDAO.updateConsentValidityTime(connection, storedConsentResource.getConsentID(),
                    newConsentValidityTime));
        }
    }

    @Test (expectedExceptions = OBConsentDataUpdationException.class)
    public void testUpdateConsentValidityTimeSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.updateConsentValidityTime(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                ConsentMgtDAOTestData.SAMPLE_CONSENT_VALIDITY_PERIOD);
    }

    @Test (expectedExceptions = OBConsentDataUpdationException.class)
    public void testUpdateConsentValidityTimeUpdateError() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection).prepareStatement(Mockito.anyString());
        Mockito.doReturn(0).when(mockedPreparedStatement).executeUpdate();
        consentCoreDAO.updateConsentValidityTime(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                ConsentMgtDAOTestData.SAMPLE_CONSENT_VALIDITY_PERIOD);
    }

    @Test
    public void testRetrieveConsentIdByConsentAttributeNameAndValue() throws Exception {

        ConsentAttributes consentAttributesResource;
        ArrayList<String> consentIdList;
        ConsentResource retrievedConsentResource;
        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            consentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
            retrievedConsentResource = consentCoreDAO.getConsentResource(connection, consentResource.getConsentID());

            consentAttributesResource = ConsentMgtDAOTestData
                    .getSampleTestConsentAttributesObject(retrievedConsentResource.getConsentID());

            consentCoreDAO.storeConsentAttributes(connection, consentAttributesResource);

            consentIdList = consentCoreDAO.getConsentIdByConsentAttributeNameAndValue(connection,
                    "payment-type", "domestic-payments");

        }
        Assert.assertFalse(consentIdList.isEmpty());
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentIdByConsentAttributeNameAndValueSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.getConsentIdByConsentAttributeNameAndValue(mockedConnection, "payment-type",
                "domestic-payments");
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentIdByConsentAttributeNameAndValueResultSetError() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getConsentIdByConsentAttributeNameAndValue(mockedConnection, "payment-type",
                "domestic-payments");
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentIdByConsentAttributeNameAndValueNoRecordsFoundError() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(mockedResultSet).when(mockedPreparedStatement).executeQuery();
        Mockito.doReturn(false).when(mockedResultSet).isBeforeFirst();
        consentCoreDAO.getConsentIdByConsentAttributeNameAndValue(mockedConnection, "payment-type",
                "domestic-payments");
    }

    @Test
    public void testRetrieveExpiringConsentsWithNoEligibility() throws Exception {

        ConsentAttributes consentAttributesResource;
        ArrayList<DetailedConsentResource> expirationEligibleConsents;
        ConsentResource retrievedConsentResource;
        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleStoredTestConsentResource();
        consentResource.setCurrentStatus(ConsentMgtDAOTestData.SAMPLE_EXPIRED_STATUS);

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            consentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
            retrievedConsentResource = consentCoreDAO.getConsentResource(connection, consentResource.getConsentID());

            consentAttributesResource = ConsentMgtDAOTestData
                    .getSampleTestConsentAttributesObject(retrievedConsentResource.getConsentID());
            consentAttributesResource.getConsentAttributes().put(
                    ConsentMgtDAOConstants.CONSENT_EXPIRY_TIME_ATTRIBUTE, "1632918113");

            consentCoreDAO.storeConsentAttributes(connection, consentAttributesResource);

            expirationEligibleConsents = consentCoreDAO.getExpiringConsents(connection,
                    "Authorized,awaitingAuthorisation");

        }
        Assert.assertTrue(expirationEligibleConsents.isEmpty());
    }

    @Test (dependsOnMethods = {"testRetrieveExpiringConsentsWithNoEligibility"})
    public void testRetrieveExpiringConsentsWithEligibility() throws Exception {

        ConsentAttributes consentAttributesResource;
        ArrayList<DetailedConsentResource> expirationEligibleConsents;
        ConsentResource retrievedConsentResource;
        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            consentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
            retrievedConsentResource = consentCoreDAO.getConsentResource(connection, consentResource.getConsentID());

            consentAttributesResource = ConsentMgtDAOTestData
                    .getSampleTestConsentAttributesObject(retrievedConsentResource.getConsentID());
            consentAttributesResource.getConsentAttributes().put(
                    ConsentMgtDAOConstants.CONSENT_EXPIRY_TIME_ATTRIBUTE, "1632918113");

            consentCoreDAO.storeConsentAttributes(connection, consentAttributesResource);

            expirationEligibleConsents = consentCoreDAO.getExpiringConsents(connection,
                    "Authorized,awaitingAuthorisation");

        }
        Assert.assertFalse(expirationEligibleConsents.isEmpty());
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveExpiringConsentsDataRetrievalError() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection).prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getExpiringConsents(mockedConnection, "Authorized,awaitingAuthorisation");
    }

    @DataProvider(name = "storeConsentHistoryDataProvider")
    public Object[][] storeConsentHistoryData() {

        /*
         * historyID
         * consentID
         * changedAttributes
         * consentType
         * amendedTimestamp
         * amendmentReason
         */
        return ConsentMgtDAOTestData.DataProviders.CONSENT_HISTORY_DATA_HOLDER;
    }

    @Test (dataProvider = "storeConsentHistoryDataProvider")
    public void testStoreConsentAmendmentHistory(String historyID, String recordID, String changedAttributes,
                              String consentType, long amendedTimestamp, String amendmentReason) throws Exception {

        boolean result;

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            result = consentCoreDAO.storeConsentAmendmentHistory(connection, historyID, amendedTimestamp,
                    recordID, consentType, changedAttributes, amendmentReason);
        }
        Assert.assertTrue(result);
    }

    @Test (dataProvider = "storeConsentHistoryDataProvider", expectedExceptions = OBConsentDataInsertionException.class)
    public void testStoreConsentAmendmentHistoryWithInvalidConsentType(String historyID, String recordID,
         String changedAttributes, String consentType, long amendedTimestamp, String amendmentReason) throws Exception {

        boolean result;
        consentType = "sampleConsentType";
        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            result = consentCoreDAO.storeConsentAmendmentHistory(connection, historyID, amendedTimestamp,
                    recordID, consentType, changedAttributes, amendmentReason);
        }
        Assert.assertTrue(result);
    }

    @Test (dataProvider = "storeConsentHistoryDataProvider", expectedExceptions = OBConsentDataInsertionException.class)
    public void testStoreConsentAmendmentHistoryInsertionError(String historyID, String recordID,
                            String changedAttributes, String consentType, long amendedTimestamp, String amendmentReason)
            throws Exception {

        Mockito.doReturn(Mockito.mock(PreparedStatement.class)).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(0).when(Mockito.mock(PreparedStatement.class)).executeUpdate();

        consentCoreDAO.storeConsentAmendmentHistory(mockedConnection, historyID, amendedTimestamp,
                recordID, consentType, changedAttributes, amendmentReason);
    }

    @Test (dataProvider = "storeConsentHistoryDataProvider", expectedExceptions = OBConsentDataInsertionException.class)
    public void testStoreConsentAmendmentHistorySQLError(String historyID, String recordID,
                         String changedAttributes, String consentType, long amendedTimestamp, String amendmentReason)
            throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.storeConsentAmendmentHistory(mockedConnection, historyID, amendedTimestamp,
                recordID, consentType, changedAttributes, amendmentReason);
    }

    @Test(dependsOnMethods = {"testStoreConsentAmendmentHistory"})
    public void testRetrieveConsentAmendmentHistory() throws Exception {

        Map<String, ConsentHistoryResource> consentHistoryResourcesDataMap;
        String expectedConsentDataTypes[] = { ConsentMgtDAOConstants.TYPE_CONSENT_BASIC_DATA,
                                              ConsentMgtDAOConstants.TYPE_CONSENT_ATTRIBUTES_DATA,
                                              ConsentMgtDAOConstants.TYPE_CONSENT_MAPPING_DATA,
                                              ConsentMgtDAOConstants.TYPE_CONSENT_AUTH_RESOURCE_DATA,
                                              "AmendedReason"};

        List<String> expectedConsentDataTypesList = Arrays.asList(expectedConsentDataTypes);

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            consentHistoryResourcesDataMap = consentCoreDAO.retrieveConsentAmendmentHistory(connection,
                    ConsentMgtDAOTestData.getRecordIDListOfSampleConsentHistory());
            Assert.assertNotNull(consentHistoryResourcesDataMap);
            for (Map.Entry<String, ConsentHistoryResource> consentHistoryDataEntry :
                    consentHistoryResourcesDataMap.entrySet()) {
                Assert.assertEquals(ConsentMgtDAOTestData.SAMPLE_HISTORY_ID, consentHistoryDataEntry.getKey());
                Map<String, Object> consentHistoryData =
                        consentHistoryDataEntry.getValue().getChangedAttributesJsonDataMap();
                for (Map.Entry<String, Object> consentHistoryDataTypeEntry :
                        consentHistoryData.entrySet()) {
                    Assert.assertNotNull(consentHistoryDataTypeEntry.getKey());
                    Assert.assertTrue(expectedConsentDataTypesList.contains((consentHistoryDataTypeEntry.getKey())));
                    Assert.assertNotNull(consentHistoryDataTypeEntry.getValue());
                }
            }
        }
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentAmendmentHistoryDataRetrievalError() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection).prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.retrieveConsentAmendmentHistory(mockedConnection,
                ConsentMgtDAOTestData.getRecordIDListOfSampleConsentHistory());
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testRetrieveConsentAmendmentHistoryPrepStmtSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.retrieveConsentAmendmentHistory(mockedConnection,
                ConsentMgtDAOTestData.getRecordIDListOfSampleConsentHistory());
    }

    @Test
    public void testRetrieveConsentAmendmentHistoryNoRecordsFound() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(mockedResultSet).when(mockedPreparedStatement).executeQuery();
        Mockito.doReturn(false).when(mockedResultSet).isBeforeFirst();

        Map<String, ConsentHistoryResource> result = consentCoreDAO.retrieveConsentAmendmentHistory(mockedConnection,
                ConsentMgtDAOTestData.getRecordIDListOfSampleConsentHistory());
        Assert.assertEquals(result.size(), 0);
    }

    @Test
    public void testDeleteConsentData() throws Exception {

        boolean isDeleted;
        boolean isDeletedOnRetentionTable;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();
        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(1).when(mockedPreparedStatement).executeUpdate();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            ConsentResource storeConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            isDeleted = consentCoreDAO.deleteConsentData(mockedConnection, storeConsentResource.getConsentID(),
                    false);
            isDeletedOnRetentionTable = consentCoreDAO.deleteConsentData(mockedConnection,
                    storeConsentResource.getConsentID(), true);
        }
        Assert.assertTrue(isDeleted);
        Assert.assertTrue(isDeletedOnRetentionTable);
    }

    @Test
    public void testDeleteConsentData2() throws Exception {

        boolean isDeleted;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();
        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(0).when(mockedPreparedStatement).executeUpdate();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            ConsentResource storeConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            isDeleted = consentCoreDAO.deleteConsentData(mockedConnection, storeConsentResource.getConsentID(),
                    false);
        }
        Assert.assertTrue(!isDeleted);
    }

    @Test (expectedExceptions = OBConsentDataDeletionException.class)
    public void testDeleteConsentDataSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.deleteConsentData(mockedConnection, "", false);
    }

    @Test
    public void testGetConsentStatusAuditRecordsByConsentId() throws Exception {

        ConsentResource storedConsentResource;
        ConsentStatusAuditRecord consentStatusAuditRecord;
        ConsentStatusAuditRecord storedConsentStatusAuditRecord;
        ArrayList<ConsentStatusAuditRecord> retrievedConsentStatusAuditRecords;
        ArrayList<ConsentStatusAuditRecord> retrievedConsentStatusAuditRecordsInRetentionTable;
        ArrayList<ConsentStatusAuditRecord> retrievedConsentStatusAuditRecordsWithLimit;
        ArrayList<ConsentStatusAuditRecord> retrievedConsentStatusAuditRecordsWithLimitAndOffset;
        ArrayList<ConsentStatusAuditRecord> retrievedConsentStatusAuditRecordsWithLimitOnly;
        ArrayList<ConsentStatusAuditRecord> retrievedConsentStatusAuditRecordsWithOffsetOnly;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            consentStatusAuditRecord = ConsentMgtDAOTestData
                    .getSampleTestConsentStatusAuditRecord(storedConsentResource.getConsentID(),
                            storedConsentResource.getCurrentStatus());

            storedConsentStatusAuditRecord = consentCoreDAO.storeConsentStatusAuditRecord(connection,
                    consentStatusAuditRecord);

            connection.commit();
            ArrayList<String> consentIds = new ArrayList<>();
            consentIds.add(storedConsentStatusAuditRecord.getConsentID());
            retrievedConsentStatusAuditRecords = consentCoreDAO.getConsentStatusAuditRecordsByConsentId(connection,
                    consentIds, null, null, false);
            retrievedConsentStatusAuditRecordsInRetentionTable =
                    consentCoreDAO.getConsentStatusAuditRecordsByConsentId(connection, consentIds, null, null, true);
            retrievedConsentStatusAuditRecordsWithLimit =
                    consentCoreDAO.getConsentStatusAuditRecordsByConsentId(connection, consentIds, 10, 0, false);
            retrievedConsentStatusAuditRecordsWithLimitAndOffset =
                    consentCoreDAO.getConsentStatusAuditRecordsByConsentId(connection, consentIds, 10, 1, false);
            retrievedConsentStatusAuditRecordsWithLimitOnly =
                    consentCoreDAO.getConsentStatusAuditRecordsByConsentId(connection, consentIds, 10, null, false);
            retrievedConsentStatusAuditRecordsWithOffsetOnly =
                    consentCoreDAO.getConsentStatusAuditRecordsByConsentId(connection, consentIds, null, 1, false);
        }
        Assert.assertNotNull(retrievedConsentStatusAuditRecords);
        Assert.assertTrue(retrievedConsentStatusAuditRecordsInRetentionTable.isEmpty());
        Assert.assertNotNull(retrievedConsentStatusAuditRecordsWithLimit);
        Assert.assertTrue(retrievedConsentStatusAuditRecordsWithLimit.size() > 0);
        Assert.assertTrue(retrievedConsentStatusAuditRecordsWithLimitAndOffset.isEmpty());
        Assert.assertTrue(!retrievedConsentStatusAuditRecordsWithLimitOnly.isEmpty());
        Assert.assertTrue(!retrievedConsentStatusAuditRecordsWithOffsetOnly.isEmpty());
        for (ConsentStatusAuditRecord record :
                retrievedConsentStatusAuditRecords) {
            Assert.assertEquals(storedConsentStatusAuditRecord.getConsentID(), record.getConsentID());
        }
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testGetConsentStatusAuditRecordsByConsentIdSQLError() throws Exception {
        ArrayList<String> consentIds = new ArrayList<>();
        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.getConsentStatusAuditRecordsByConsentId(mockedConnection,
                consentIds, null, null, false);
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testGetConsentStatusAuditRecordsByConsentIdSQLErrorForResults() throws Exception {
        ArrayList<String> consentIds = new ArrayList<>();
        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getConsentStatusAuditRecordsByConsentId(mockedConnection,
                consentIds, null, null, false);
    }

    @Test
    public void testGetListOfConsentIds() throws Exception {

        ConsentResource storedConsentResource;
        ArrayList<String> listOfConsentIds;
        ArrayList<String> listOfConsentIdsInRetentionTable;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
            listOfConsentIds = consentCoreDAO.getListOfConsentIds(connection, false);
            listOfConsentIdsInRetentionTable = consentCoreDAO.getListOfConsentIds(connection, true);
        }
        Assert.assertNotNull(listOfConsentIds);
        Assert.assertTrue(listOfConsentIdsInRetentionTable.isEmpty());
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testGetListOfConsentIdsSQLError() throws Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.getListOfConsentIds(mockedConnection, false);
    }

    @Test (expectedExceptions = OBConsentDataRetrievalException.class)
    public void testGetListOfConsentIdsSQLErrorForResults() throws Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getListOfConsentIds(mockedConnection, false);

    }
}
