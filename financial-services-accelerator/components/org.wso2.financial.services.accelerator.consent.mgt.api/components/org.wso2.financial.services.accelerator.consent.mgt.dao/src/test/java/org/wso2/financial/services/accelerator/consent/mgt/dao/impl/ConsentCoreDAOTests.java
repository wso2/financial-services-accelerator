/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.consent.mgt.dao.impl;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.consent.mgt.dao.ConsentCoreDAO;
import org.wso2.financial.services.accelerator.consent.mgt.dao.constants.ConsentMgtDAOConstants;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentDataDeletionException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentDataInsertionException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentDataRetrievalException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentDataUpdationException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentAttributes;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentFile;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentStatusAuditRecord;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.queries.ConsentMgtCommonDBQueries;
import org.wso2.financial.services.accelerator.consent.mgt.dao.util.ConsentManagementDAOTestDataProvider;
import org.wso2.financial.services.accelerator.consent.mgt.dao.util.ConsentMgtDAOTestData;
import org.wso2.financial.services.accelerator.consent.mgt.dao.util.DAOUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * Consent management DAO tests.
 */
public class ConsentCoreDAOTests {

    private static final String DB_NAME = "CONSENT_DB";

    private ConsentCoreDAO consentCoreDAO;
    private Connection mockedConnection;
    private PreparedStatement mockedPreparedStatement;
    private ResultSet mockedResultSet;

    @BeforeClass
    public void initTest() throws
            Exception {

        DAOUtils.initializeDataSource(DB_NAME, DAOUtils.getFilePath("dbScripts/h2.sql"));
        consentCoreDAO = new ConsentCoreDAOImpl(new ConsentMgtCommonDBQueries());
        mockedConnection = Mockito.mock(Connection.class);
        mockedPreparedStatement = Mockito.mock(PreparedStatement.class);
        mockedResultSet = Mockito.mock(ResultSet.class);
    }

    @Test
    public void testStoreConsentResource() throws
            Exception {

        ConsentResource storedConsentResource;
        ConsentResource consentResource = new ConsentResource();
        consentResource.setReceipt(ConsentMgtDAOTestData.SAMPLE_CONSENT_RECEIPT);
        consentResource.setClientID(UUID.randomUUID().toString());
        consentResource.setConsentType(ConsentMgtDAOTestData.SAMPLE_CONSENT_TYPE);
        consentResource.setCurrentStatus(ConsentMgtDAOTestData.SAMPLE_CURRENT_STATUS);
        consentResource.setConsentFrequency(ConsentMgtDAOTestData.SAMPLE_CONSENT_FREQUENCY);
        consentResource.setValidityPeriod(ConsentMgtDAOTestData.SAMPLE_CONSENT_VALIDITY_PERIOD);
        consentResource.setConsentID(UUID.randomUUID().toString());
        consentResource.setRecurringIndicator(true);

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
        }
        Assert.assertNotNull(storedConsentResource);
        Assert.assertNotNull(storedConsentResource.getConsentID());
        Assert.assertNotNull(storedConsentResource.getClientID());
        Assert.assertNotNull(storedConsentResource.getConsentType());
        Assert.assertEquals(ConsentMgtDAOTestData.SAMPLE_CONSENT_FREQUENCY,
                storedConsentResource.getConsentFrequency());
        Assert.assertTrue(storedConsentResource.getValidityPeriod() > 0);
        Assert.assertTrue(storedConsentResource.isRecurringIndicator());
        Assert.assertTrue(storedConsentResource.getCreatedTime() > 0L);
        Assert.assertNotNull(storedConsentResource.getCurrentStatus());
    }

    @Test(expectedExceptions = ConsentDataInsertionException.class)
    public void testStoreConsentResourceInsertionError() throws
            Exception {

        Mockito.doReturn(Mockito.mock(PreparedStatement.class)).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(0).when(Mockito.mock(PreparedStatement.class)).executeUpdate();

        consentCoreDAO.storeConsentResource(mockedConnection, ConsentMgtDAOTestData.getSampleTestConsentResource());
    }

    @Test(expectedExceptions = ConsentDataInsertionException.class)
    public void testStoreConsentResourceSQLError() throws
            Exception {


        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.storeConsentResource(mockedConnection, ConsentMgtDAOTestData.getSampleTestConsentResource());
    }

    @Test
    public void testRetrieveConsentResource() throws
            Exception {

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
        Assert.assertTrue(retrievedConsentResource.getValidityPeriod() > 0L);
        Assert.assertTrue(retrievedConsentResource.isRecurringIndicator());
        Assert.assertTrue(retrievedConsentResource.getCreatedTime() > 0L);
        Assert.assertNotNull(retrievedConsentResource.getCurrentStatus());
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveConsentResourceResultSetError() throws
            Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getConsentResource(mockedConnection, Mockito.anyString());
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveConsentResourceSQLError() throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.getConsentResource(mockedConnection, Mockito.any());
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveConsentResourceWithUnmatchedConsentID() throws
            Exception {

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            consentCoreDAO.getConsentResource(connection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID);
        }
    }

    @Test
    public void testRetrieveConsentWithAttributesResource() throws
            Exception {

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
        Assert.assertTrue(retrievedConsentResource.getValidityPeriod() > 0L);
        Assert.assertTrue(retrievedConsentResource.isRecurringIndicator());
        Assert.assertTrue(retrievedConsentResource.getCreatedTime() > 0L);
        Assert.assertNotNull(retrievedConsentResource.getCurrentStatus());
        Assert.assertNotNull(retrievedConsentResource.getConsentAttributes());

    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveConsentWithAttributesResourceResultRetrieveError() throws
            Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());
        Mockito.doReturn(mockedResultSet).when(mockedPreparedStatement).executeQuery();
        Mockito.doReturn(false).when(mockedResultSet).next();
        consentCoreDAO.getConsentResourceWithAttributes(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID);
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveConsentWithAttributesResourceResultSetError() throws
            Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getConsentResourceWithAttributes(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID);
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveConsentWithAttributesResourceSQLError() throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString(),
                Mockito.anyInt(), Mockito.anyInt());
        consentCoreDAO.getConsentResourceWithAttributes(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID);
    }

    @Test
    public void testRetrieveDetailedConsentResource() throws
            Exception {

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
                    storedConsentResource.getConsentID());
        }

        Assert.assertNotNull(retrievedDetailedConsentResource);
        Assert.assertEquals(retrievedDetailedConsentResource.getConsentID(), storedConsentResource.getConsentID());
        Assert.assertEquals(retrievedDetailedConsentResource.getConsentID(), storedConsentResource.getConsentID());
        Assert.assertEquals(retrievedDetailedConsentResource.getClientID(), storedConsentResource.getClientID());
        Assert.assertEquals(retrievedDetailedConsentResource.getReceipt(), storedConsentResource.getReceipt());
        Assert.assertEquals(retrievedDetailedConsentResource.getConsentType(),
                storedConsentResource.getConsentType());
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
    public void testRetrieveDetailedConsentResourceWithoutAttributes() throws
            Exception {

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
                    storedConsentResource.getConsentID());
        }

        Assert.assertNotNull(retrievedDetailedConsentResource);
        Assert.assertEquals(retrievedDetailedConsentResource.getConsentID(), storedConsentResource.getConsentID());
        Assert.assertEquals(retrievedDetailedConsentResource.getConsentID(), storedConsentResource.getConsentID());
        Assert.assertEquals(retrievedDetailedConsentResource.getClientID(), storedConsentResource.getClientID());
        Assert.assertEquals(retrievedDetailedConsentResource.getReceipt(), storedConsentResource.getReceipt());
        Assert.assertEquals(retrievedDetailedConsentResource.getConsentType(),
                storedConsentResource.getConsentType());
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
    public void testRetrieveDetailedConsentResourceWithMultipleConsentAttributeKeys() throws
            Exception {

        ConsentResource storedConsentResource;
        AuthorizationResource storedAuthorizationResourceOne;
        AuthorizationResource storedAuthorizationResourceTwo;
        DetailedConsentResource retrievedDetailedConsentResource;
        String resourceOne = "{\"accountID\": \"111\",\"permission\": \"read\"}";
        String resourceTwo = "{\"accountID\": \"222\",\"permission\": \"read\"}";

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
                            .getSampleTestConsentMappingResourceWithResource(storedAuthorizationResourceOne
                                    .getAuthorizationID(), resourceOne));
            consentCoreDAO.storeConsentMappingResource(connection,
                    ConsentMgtDAOTestData
                            .getSampleTestConsentMappingResourceWithResource(storedAuthorizationResourceOne
                                    .getAuthorizationID(), resourceTwo));
            // mapping resource for second auth resource with a single account id
            consentCoreDAO.storeConsentMappingResource(connection,
                    ConsentMgtDAOTestData
                            .getSampleTestConsentMappingResourceWithResource(storedAuthorizationResourceTwo
                                    .getAuthorizationID(), resourceOne));
            retrievedDetailedConsentResource = consentCoreDAO.getDetailedConsentResource(connection,
                    storedConsentResource.getConsentID());
        }

        Assert.assertNotNull(retrievedDetailedConsentResource);
        Assert.assertEquals(retrievedDetailedConsentResource.getConsentID(), storedConsentResource.getConsentID());
        Assert.assertEquals(retrievedDetailedConsentResource.getAuthorizationResources().get(0).getAuthorizationID(),
                storedAuthorizationResourceOne.getAuthorizationID());
        Assert.assertEquals(retrievedDetailedConsentResource.getAuthorizationResources().get(1).getAuthorizationID(),
                storedAuthorizationResourceTwo.getAuthorizationID());
        /* according to the created consent resource,  retrieved consent resource should contain two auth
        resources and
        three mapping resources
         */
        Assert.assertEquals(retrievedDetailedConsentResource.getAuthorizationResources().size(), 2);
        Assert.assertEquals(retrievedDetailedConsentResource.getConsentMappingResources().size(), 3);
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveDetailedConsentResourceError() throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.getDetailedConsentResource(mockedConnection, Mockito.anyString());
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveDetailedConsentResourceRetrieveError() throws
            Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(mockedResultSet).when(mockedPreparedStatement).executeQuery();
        Mockito.doReturn(false).when(mockedResultSet).next();
        consentCoreDAO.getDetailedConsentResource(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID);
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveDetailedConsentResourceResultSetError() throws
            Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getDetailedConsentResource(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID);
    }

    @Test
    public void testUpdateConsentStatus() throws
            Exception {

        ConsentResource storedConsentResource;
        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
            consentCoreDAO.updateConsentStatus(connection, storedConsentResource.getConsentID(),
                    ConsentMgtDAOTestData.SAMPLE_CURRENT_STATUS);
        }
    }

    @Test(expectedExceptions = ConsentDataUpdationException.class)
    public void testUpdateConsentStatusSQLError() throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.updateConsentStatus(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                ConsentMgtDAOTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test(expectedExceptions = ConsentDataUpdationException.class)
    public void testUpdateConsentStatusWithUnmatchedConsentID() throws
            Exception {

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            consentCoreDAO.updateConsentStatus(connection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                    ConsentMgtDAOTestData.SAMPLE_CURRENT_STATUS);
        }
    }

    @Test
    public void testUpdateConsentReceipt() throws
            Exception {

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();
        String newConsentReceipt = "{\"amendedReceipt\":\"amendedData\"}";

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            ConsentResource storedConsentResource = consentCoreDAO.storeConsentResource(connection,
                    consentResource);
            consentCoreDAO.updateConsentReceipt(connection, storedConsentResource.getConsentID(), newConsentReceipt);
        }
    }

    @Test(expectedExceptions = ConsentDataUpdationException.class)
    public void testUpdateConsentReceiptSQLError() throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.updateConsentReceipt(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                ConsentMgtDAOTestData.SAMPLE_CONSENT_RECEIPT);
    }

    @Test(expectedExceptions = ConsentDataUpdationException.class)
    public void testUpdateConsentReceiptUpdateError() throws
            Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection).prepareStatement(Mockito.anyString());
        Mockito.doReturn(0).when(mockedPreparedStatement).executeUpdate();
        consentCoreDAO.updateConsentReceipt(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                ConsentMgtDAOTestData.SAMPLE_CONSENT_RECEIPT);
    }

    @Test
    public void testUpdateConsentValidityTime() throws
            Exception {

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();
        long newConsentValidityTime = 12345;

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            ConsentResource storedConsentResource = consentCoreDAO.storeConsentResource(connection,
                    consentResource);
            consentCoreDAO.updateConsentValidityTime(connection, storedConsentResource.getConsentID(),
                    newConsentValidityTime);
        }
    }

    @Test(expectedExceptions = ConsentDataUpdationException.class)
    public void testUpdateConsentValidityTimeSQLError() throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.updateConsentValidityTime(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                ConsentMgtDAOTestData.SAMPLE_CONSENT_VALIDITY_PERIOD);
    }

    @Test(expectedExceptions = ConsentDataUpdationException.class)
    public void testUpdateConsentValidityTimeUpdateError() throws
            Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection).prepareStatement(Mockito.anyString());
        Mockito.doReturn(0).when(mockedPreparedStatement).executeUpdate();
        consentCoreDAO.updateConsentValidityTime(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                ConsentMgtDAOTestData.SAMPLE_CONSENT_VALIDITY_PERIOD);
    }

    @Test
    public void testStoreAuthorizationResource() throws
            Exception {

        ConsentResource storedConsentResource;
        AuthorizationResource storedAuthorizationResource;
        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            AuthorizationResource authorizationResource = new AuthorizationResource();
            authorizationResource.setConsentID(storedConsentResource.getConsentID());
            authorizationResource.setAuthorizationType(ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_TYPE);
            authorizationResource.setUserID(ConsentMgtDAOTestData.SAMPLE_USER_ID);
            authorizationResource.setAuthorizationStatus(ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_STATUS);

            storedAuthorizationResource = consentCoreDAO.storeAuthorizationResource(connection,
                    authorizationResource);
        }
        Assert.assertNotNull(storedAuthorizationResource.getConsentID());
        Assert.assertNotNull(storedAuthorizationResource.getAuthorizationType());
        Assert.assertNotNull(storedAuthorizationResource.getUserID());
        Assert.assertNotNull(storedAuthorizationResource.getAuthorizationStatus());
        Assert.assertTrue(storedAuthorizationResource.getUpdatedTime() > 0L);
        Assert.assertNotNull(storedAuthorizationResource.getAuthorizationID());
    }

    @Test(expectedExceptions = ConsentDataInsertionException.class)
    public void testStoreAuthorizationResourceInsertionError() throws
            Exception {

        ConsentResource storedConsentResource = ConsentMgtDAOTestData.getSampleStoredTestConsentResource();

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(0).when(mockedPreparedStatement).executeUpdate();

        consentCoreDAO.storeAuthorizationResource(mockedConnection, ConsentMgtDAOTestData.
                getSampleTestAuthorizationResource(storedConsentResource.getConsentID()));
    }

    @Test(expectedExceptions = ConsentDataInsertionException.class)
    public void testStoreAuthorizationResourceSQLError() throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());

        consentCoreDAO.storeAuthorizationResource(mockedConnection, ConsentMgtDAOTestData.
                getSampleTestAuthorizationResource(Mockito.anyString()));
    }

    @Test
    public void testRetrieveAuthorizationResource() throws
            Exception {

        ConsentResource storedConsentResource;
        AuthorizationResource authorizationResource;
        AuthorizationResource storedAuthorizationResource;
        AuthorizationResource retrievedAuthorizationResource;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            authorizationResource = ConsentMgtDAOTestData
                    .getSampleTestAuthorizationResource(storedConsentResource.getConsentID());

            storedAuthorizationResource = consentCoreDAO.storeAuthorizationResource(connection,
                    authorizationResource);

            retrievedAuthorizationResource = consentCoreDAO.getAuthorizationResource(connection,
                    storedAuthorizationResource.getAuthorizationID(), storedConsentResource.getOrgID());
        }
        Assert.assertTrue(retrievedAuthorizationResource.getUpdatedTime() > 0L);
//        Assert.assertNotNull(retrievedAuthorizationResource.getAuthorizationID());
        Assert.assertNotNull(retrievedAuthorizationResource.getAuthorizationStatus());
        Assert.assertNotNull(retrievedAuthorizationResource.getUserID());
        Assert.assertNotNull(retrievedAuthorizationResource.getAuthorizationType());
        Assert.assertEquals(retrievedAuthorizationResource.getConsentID(),
                storedAuthorizationResource.getConsentID());
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveAuthorizationResourceResultSetError() throws
            Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getAuthorizationResource(mockedConnection, ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_ID,
                ConsentMgtDAOTestData.SAMPLE_ORG_ID);
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveAuthorizationResourceSQLError() throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.getAuthorizationResource(mockedConnection, ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_ID,
                ConsentMgtDAOTestData.SAMPLE_ORG_ID);
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveAuthorizationResourceWithUnmatchedAuthID() throws
            Exception {

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            consentCoreDAO.getAuthorizationResource(connection,
                    ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_ID, ConsentMgtDAOTestData.SAMPLE_ORG_ID);
        }
    }

    @Test
    public void testUpdateAuthorizationStatus() throws
            Exception {

        ConsentResource storedConsentResource;
        AuthorizationResource authorizationResource;
        AuthorizationResource storedAuthorizationResource;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            authorizationResource = ConsentMgtDAOTestData
                    .getSampleTestAuthorizationResource(storedConsentResource.getConsentID());

            storedAuthorizationResource = consentCoreDAO.storeAuthorizationResource(connection,
                    authorizationResource);

            consentCoreDAO.updateAuthorizationStatus(connection, storedAuthorizationResource.getAuthorizationID(),
                    ConsentMgtDAOTestData.SAMPLE_CURRENT_STATUS);
        }
    }

    @Test(expectedExceptions = ConsentDataUpdationException.class)
    public void testUpdateAuthorizationStatusSQLError() throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.updateAuthorizationStatus(mockedConnection, ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_ID,
                ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test(expectedExceptions = ConsentDataUpdationException.class)
    public void testUpdateAuthorizationStatusWithUnmatchedAuthID() throws
            Exception {

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            consentCoreDAO.updateAuthorizationStatus(connection,
                    ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_ID, ConsentMgtDAOTestData.SAMPLE_CURRENT_STATUS);
        }
    }

    @Test
    public void testUpdateAuthorizationUser() throws
            Exception {

        ConsentResource storedConsentResource;
        AuthorizationResource authorizationResource;
        AuthorizationResource storedAuthorizationResource;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            authorizationResource = ConsentMgtDAOTestData
                    .getSampleTestAuthorizationResource(storedConsentResource.getConsentID());

            storedAuthorizationResource = consentCoreDAO.storeAuthorizationResource(connection,
                    authorizationResource);

            consentCoreDAO.updateAuthorizationUser(connection, storedAuthorizationResource.getAuthorizationID(),
                    ConsentMgtDAOTestData.SAMPLE_NEW_USER_ID);
        }
    }

    @Test(expectedExceptions = ConsentDataUpdationException.class)
    public void testUpdateAuthorizationUserSQLError() throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.updateAuthorizationUser(mockedConnection, ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_ID,
                ConsentMgtDAOTestData.SAMPLE_USER_ID);
    }

    @Test(expectedExceptions = ConsentDataUpdationException.class)
    public void testUpdateAuthorizationUserWithUnmatchedAuthID() throws
            Exception {

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            consentCoreDAO.updateAuthorizationUser(connection, ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_ID,
                    ConsentMgtDAOTestData.SAMPLE_NEW_USER_ID);
        }
    }

    @Test
    public void testStoreConsentMappingResource() throws
            Exception {

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
            consentMappingResource.setResource(ConsentMgtDAOTestData.SAMPLE_RESOURCE);
            consentMappingResource.setMappingStatus(ConsentMgtDAOTestData.SAMPLE_MAPPING_STATUS);

            storedConsentMappingResource = consentCoreDAO.storeConsentMappingResource(connection,
                    consentMappingResource);
        }
        Assert.assertNotNull(storedConsentMappingResource.getMappingID());
        Assert.assertNotNull(storedConsentMappingResource.getAuthorizationID());
        Assert.assertNotNull(storedConsentMappingResource.getResource());
        Assert.assertNotNull(storedConsentMappingResource.getMappingStatus());
    }

    @Test
    public void testStoreConsentMappingResourceWithID() throws
            Exception {

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
            consentMappingResource.setMappingID("aa4c943d-38e2-47e5-bb78-8a242d279b5a");
            consentMappingResource.setResource(ConsentMgtDAOTestData.SAMPLE_RESOURCE);
            consentMappingResource.setMappingStatus(ConsentMgtDAOTestData.SAMPLE_MAPPING_STATUS);

            storedConsentMappingResource = consentCoreDAO.storeConsentMappingResource(connection,
                    consentMappingResource);
        }
        Assert.assertEquals(storedConsentMappingResource.getMappingID(), "aa4c943d-38e2-47e5-bb78-8a242d279b5a");
        Assert.assertEquals(storedConsentMappingResource.getAuthorizationID(), "db0b943d-38e2-47e4-bb78-8a242d279b5a");
        Assert.assertNotNull(storedConsentMappingResource.getResource());
        Assert.assertNotNull(storedConsentMappingResource.getMappingStatus());
    }

    @Test(expectedExceptions = ConsentDataInsertionException.class)
    public void testStoreConsentMappingResourceInsertionError() throws
            Exception {

        ConsentMappingResource sampleConsentMappingResource =
                ConsentMgtDAOTestData.getSampleTestConsentMappingResource(ConsentMgtDAOTestData
                        .getSampleStoredTestAuthorizationResource().getAuthorizationID());

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(0).when(mockedPreparedStatement).executeUpdate();

        consentCoreDAO.storeConsentMappingResource(mockedConnection, sampleConsentMappingResource);
    }

    @Test(expectedExceptions = ConsentDataInsertionException.class)
    public void testStoreConsentMappingResourceSQLError() throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.storeConsentMappingResource(mockedConnection, new ConsentMappingResource());
    }

    @Test
    public void testRetrieveConsentMappingResource() throws
            Exception {

        ArrayList<ConsentMappingResource> retrievedConsentMappingResources;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            ConsentResource storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
            AuthorizationResource authorizationResource = ConsentMgtDAOTestData
                    .getSampleTestAuthorizationResource(storedConsentResource.getConsentID());
            AuthorizationResource storedAuthorizationResource = consentCoreDAO.storeAuthorizationResource(connection,
                    authorizationResource);
            ConsentMappingResource consentMappingResource = ConsentMgtDAOTestData
                    .getSampleTestConsentMappingResource(storedAuthorizationResource.getAuthorizationID());
            consentCoreDAO.storeConsentMappingResource(connection, consentMappingResource);

            retrievedConsentMappingResources = consentCoreDAO.getConsentMappingResources(connection,
                    storedAuthorizationResource.getAuthorizationID());
        }
        Assert.assertNotNull(retrievedConsentMappingResources);
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveConsentMappingResourceResultSetError() throws
            Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getConsentMappingResources(mockedConnection, ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_ID);
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveConsentMappingResourceSQLError() throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.getConsentMappingResources(mockedConnection, ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_ID);
    }

    @Test
    public void testUpdateConsentMappingStatus() throws
            Exception {

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            ConsentResource storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
            AuthorizationResource authorizationResource = ConsentMgtDAOTestData
                    .getSampleTestAuthorizationResource(storedConsentResource.getConsentID());
            AuthorizationResource storedAuthorizationResource = consentCoreDAO.storeAuthorizationResource(connection,
                    authorizationResource);
            ConsentMappingResource consentMappingResource = ConsentMgtDAOTestData
                    .getSampleTestConsentMappingResource(storedAuthorizationResource.getAuthorizationID());
            ConsentMappingResource storedConsentMappingResource = consentCoreDAO.storeConsentMappingResource(connection,
                    consentMappingResource);

            ArrayList<String> mappingIDs = new ArrayList<String>() {
                {
                    add(storedConsentMappingResource.getMappingID());
                }
            };

            consentCoreDAO.updateConsentMappingStatus(connection, mappingIDs,
                    ConsentMgtDAOTestData.SAMPLE_NEW_MAPPING_STATUS);
        }
    }

    @Test(expectedExceptions = ConsentDataUpdationException.class)
    public void testUpdateConsentMappingStatusSQLError() throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.updateConsentMappingStatus(mockedConnection, ConsentMgtDAOTestData.UNMATCHED_MAPPING_IDS,
                ConsentMgtDAOTestData.SAMPLE_MAPPING_STATUS);
    }

    @Test
    public void testStoreConsentAttributes() throws
            Exception {

        ConsentResource storedConsentResource;
        ConsentAttributes consentAttributesResource;
        boolean isConsentAttributesStored;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
            consentAttributesResource = new ConsentAttributes();
            consentAttributesResource.setConsentID(storedConsentResource.getConsentID());
            consentAttributesResource.setConsentAttributes(ConsentMgtDAOTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
            isConsentAttributesStored = consentCoreDAO.storeConsentAttributes(connection, consentAttributesResource);
        }
        Assert.assertTrue(isConsentAttributesStored);
    }

    @Test(expectedExceptions = ConsentDataInsertionException.class)
    public void testStoreConsentAttributesSQLError() throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.storeConsentAttributes(mockedConnection, ConsentMgtDAOTestData
                .getSampleTestConsentAttributesObject(ConsentMgtDAOTestData.getSampleStoredTestConsentResource()
                        .getConsentID()));
    }

    @Test
    public void testRetrieveConsentAttributes() throws
            Exception {

        ConsentAttributes retrievedConsentAttributesResource;
        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            consentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
            ConsentResource retrievedConsentResource = consentCoreDAO.getConsentResource(connection,
                    consentResource.getConsentID());

            ConsentAttributes consentAttributesResource = ConsentMgtDAOTestData
                    .getSampleTestConsentAttributesObject(retrievedConsentResource.getConsentID());

            consentCoreDAO.storeConsentAttributes(connection, consentAttributesResource);

            retrievedConsentAttributesResource = consentCoreDAO.getConsentAttributes(connection,
                    retrievedConsentResource.getConsentID());
        }
        Assert.assertNotNull(retrievedConsentAttributesResource.getConsentID());
        Assert.assertNotNull(retrievedConsentAttributesResource.getConsentAttributes());
    }

    @Test
    public void testRetrieveConsentAttributesForNoAttributesScenario() throws
            Exception {

        ConsentAttributes retrievedConsentAttributesResource;
        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            consentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
            ConsentResource retrievedConsentResource = consentCoreDAO.getConsentResource(connection,
                    consentResource.getConsentID());

            retrievedConsentAttributesResource = consentCoreDAO.getConsentAttributes(connection,
                    retrievedConsentResource.getConsentID());
        }
        Assert.assertNotNull(retrievedConsentAttributesResource.getConsentID());
        Assert.assertTrue(retrievedConsentAttributesResource.getConsentAttributes().isEmpty());
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveConsentAttributesResultSetError() throws
            Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getConsentAttributes(mockedConnection, Mockito.anyString());
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveConsentAttributesSQLError() throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.getConsentAttributes(mockedConnection, Mockito.any());
    }

//    @Test
//    public void testRetrieveConsentAttributes() throws Exception {
//
//        ConsentAttributes retrievedConsentAttributesResource;
//        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();
//
//        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
//            consentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
//            ConsentResource retrievedConsentResource = consentCoreDAO.getConsentResource(connection,
//                    consentResource.getConsentID());
//
//            ConsentAttributes consentAttributesResource = ConsentMgtDAOTestData
//                    .getSampleTestConsentAttributesObject(retrievedConsentResource.getConsentID());
//
//            consentCoreDAO.storeConsentAttributes(connection, consentAttributesResource);
//
//            retrievedConsentAttributesResource = consentCoreDAO.getConsentAttributes(connection,
//                    retrievedConsentResource.getConsentID(), ConsentMgtDAOTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
//        }
//        Assert.assertNotNull(retrievedConsentAttributesResource.getConsentID());
//        Assert.assertNotNull(retrievedConsentAttributesResource.getConsentAttributes());
//    }

//    @Test
//    public void testRetrieveConsentAttributesForNoAttributesScenario() throws Exception {
//
//        ConsentAttributes retrievedConsentAttributesResource;
//        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();
//
//        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
//            consentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
//            ConsentResource retrievedConsentResource = consentCoreDAO.getConsentResource(connection,
//                    consentResource.getConsentID());
//
//            retrievedConsentAttributesResource = consentCoreDAO.getConsentAttributes(connection,
//                    retrievedConsentResource.getConsentID(), ConsentMgtDAOTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
//        }
//        Assert.assertNotNull(retrievedConsentAttributesResource.getConsentID());
//        Assert.assertTrue(retrievedConsentAttributesResource.getConsentAttributes().isEmpty());
//    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveConsentAttributesResultSetErrorOverloadedMethod() throws
            Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getConsentAttributes(mockedConnection, Mockito.anyString(),
                ConsentMgtDAOTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveConsentAttributesSQLErrorOverloadedMethod() throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.getConsentAttributes(mockedConnection, Mockito.anyString(),
                ConsentMgtDAOTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    @Test
    public void testRetrieveConsentAttributesByName() throws
            Exception {

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

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveConsentAttributesByNameSQLError() throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.getConsentAttributesByName(mockedConnection, Mockito.any());
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveConsentAttributesByNameResultSetError() throws
            Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getConsentAttributesByName(mockedConnection, Mockito.anyString());
    }

    @Test
    public void testRetrieveConsentIdByConsentAttributeNameAndValue() throws
            Exception {

        ArrayList<String> consentIdList;
        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            consentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
            ConsentResource retrievedConsentResource = consentCoreDAO.getConsentResource(connection,
                    consentResource.getConsentID());
            ConsentAttributes consentAttributesResource = ConsentMgtDAOTestData
                    .getSampleTestConsentAttributesObject(retrievedConsentResource.getConsentID());
            consentCoreDAO.storeConsentAttributes(connection, consentAttributesResource);
            consentIdList = consentCoreDAO.getConsentIdByConsentAttributeNameAndValue(connection,
                    "payment-type", "domestic-payments");

        }
        Assert.assertFalse(consentIdList.isEmpty());
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveConsentIdByConsentAttributeNameAndValueSQLError() throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.getConsentIdByConsentAttributeNameAndValue(mockedConnection, "payment-type",
                "domestic-payments");
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveConsentIdByConsentAttributeNameAndValueResultSetError() throws
            Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getConsentIdByConsentAttributeNameAndValue(mockedConnection, "payment-type",
                "domestic-payments");
    }

    @Test
    public void testRetrieveConsentIdByConsentAttributeNameAndValueNoRecordsFoundError() throws
            Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(mockedResultSet).when(mockedPreparedStatement).executeQuery();
        Mockito.doReturn(false).when(mockedResultSet).isBeforeFirst();
        ArrayList<String> consentIdList = consentCoreDAO.getConsentIdByConsentAttributeNameAndValue(mockedConnection,
                "payment-type", "domestic-payments");
        Assert.assertTrue(consentIdList.isEmpty());
    }

    @Test
    public void testUpdateConsentAttributes() throws
            Exception {

        DetailedConsentResource createdConsentResource;
        DetailedConsentResource updatedConsentResource;
        ConsentAttributes consentAttributesResource;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            ConsentResource storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
            consentAttributesResource = new ConsentAttributes();
            consentAttributesResource.setConsentID(storedConsentResource.getConsentID());
            consentAttributesResource.setConsentAttributes(ConsentMgtDAOTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
            consentCoreDAO.storeConsentAttributes(connection, consentAttributesResource);
            createdConsentResource = consentCoreDAO.getDetailedConsentResource(connection,
                    storedConsentResource.getConsentID());

            consentCoreDAO.updateConsentAttributes(connection, storedConsentResource.getConsentID(),
                    ConsentMgtDAOTestData.CONSENT_ATTRIBUTES_MAP_FOR_UPDATE);
            updatedConsentResource = consentCoreDAO.getDetailedConsentResource(connection,
                    storedConsentResource.getConsentID());
        }
        Assert.assertEquals(createdConsentResource.getConsentID(), updatedConsentResource.getConsentID());
        Assert.assertNotEquals(createdConsentResource.getConsentAttributes(),
                updatedConsentResource.getConsentAttributes());
        Assert.assertEquals(updatedConsentResource.getConsentAttributes().get("payment-type"),
                "international-payments");

    }

    @Test(expectedExceptions = ConsentDataUpdationException.class)
    public void testUpdateConsentAttributesSQLError() throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());

        consentCoreDAO.updateConsentAttributes(mockedConnection, ConsentMgtDAOConstants.CONSENT_ID,
                ConsentMgtDAOTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    @Test
    public void testDeleteConsentAttribute() throws
            Exception {

        boolean isDeleted;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            ConsentResource storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
            ConsentAttributes consentAttributesResource = ConsentMgtDAOTestData
                    .getSampleTestConsentAttributesObject(storedConsentResource.getConsentID());
            consentCoreDAO.storeConsentAttributes(connection, consentAttributesResource);

            isDeleted = consentCoreDAO.deleteConsentAttributes(connection, storedConsentResource.getConsentID(),
                    ConsentMgtDAOTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
        }
        Assert.assertTrue(isDeleted);
    }

    @Test(expectedExceptions = ConsentDataDeletionException.class)
    public void testDeleteConsentAttributeSQLError() throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.deleteConsentAttributes(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                ConsentMgtDAOTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    @Test
    public void testStoreConsentFile() throws
            Exception {

        boolean isConsentFileStored;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            ConsentResource storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            ConsentFile consentFileResource = new ConsentFile();
            consentFileResource.setConsentID(storedConsentResource.getConsentID());
            consentFileResource.setConsentFile(ConsentMgtDAOTestData.SAMPLE_CONSENT_FILE);

            isConsentFileStored = consentCoreDAO.storeConsentFile(connection, consentFileResource);
        }
        Assert.assertTrue(isConsentFileStored);
    }

    @Test(expectedExceptions = ConsentDataInsertionException.class)
    public void testStoreConsentFileInsertionError() throws
            Exception {

        ConsentFile sampleConsentFileResource =
                ConsentMgtDAOTestData.getSampleConsentFileObject(ConsentMgtDAOTestData.SAMPLE_CONSENT_FILE);

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(0).when(mockedPreparedStatement).executeUpdate();
        consentCoreDAO.storeConsentFile(mockedConnection, sampleConsentFileResource);
    }

    @Test(expectedExceptions = ConsentDataInsertionException.class)
    public void testStoreConsentFileSQLError() throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.storeConsentFile(mockedConnection, Mockito.any());
    }

    @Test
    public void testRetrieveConsentFileResource() throws
            Exception {

        ConsentFile retrievedConsentFileResource;
        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            consentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
            ConsentResource retrievedConsentResource = consentCoreDAO.getConsentResource(connection,
                    consentResource.getConsentID());

            ConsentFile consentFileResource = new ConsentFile();
            consentFileResource.setConsentID(retrievedConsentResource.getConsentID());
            consentFileResource.setConsentFile(ConsentMgtDAOTestData.SAMPLE_CONSENT_FILE);

            consentCoreDAO.storeConsentFile(connection, consentFileResource);

            retrievedConsentFileResource = consentCoreDAO.getConsentFile(connection,
                    consentFileResource.getConsentID());
        }
        Assert.assertNotNull(retrievedConsentFileResource.getConsentID());
        Assert.assertNotNull(retrievedConsentFileResource.getConsentFile());
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveConsentFileResourceNoRecordsFoundError() throws
            Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(mockedResultSet).when(mockedPreparedStatement).executeQuery();
        Mockito.doReturn(false).when(mockedResultSet).next();
        consentCoreDAO.getConsentFile(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID);
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveConsentFileResourceSQLError() throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.getConsentFile(mockedConnection, Mockito.any());
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveConsentFileResourceRetrieveError() throws
            Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getConsentFile(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID);
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveConsentFileResourceWithUnmatchedConsentID() throws
            Exception {

        ConsentFile consentFileResource;
        ConsentResource retrievedConsentResource;
        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            consentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
            retrievedConsentResource = consentCoreDAO.getConsentResource(connection, consentResource.getConsentID());

            consentFileResource = new ConsentFile();
            consentFileResource.setConsentID(retrievedConsentResource.getConsentID());
            consentFileResource.setConsentFile(ConsentMgtDAOTestData.SAMPLE_CONSENT_FILE);

            consentCoreDAO.storeConsentFile(connection, consentFileResource);
            consentCoreDAO.getConsentFile(connection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID);
        }
    }

    @Test
    public void testConsentSearchWithConsentIDsList() throws
            Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources;
        ArrayList<String> consentIDs = new ArrayList<>();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storeDataForConsentSearchTest(consentIDs, connection);
            detailedConsentResources = consentCoreDAO.searchConsents(connection, null, consentIDs, null,
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
    public void testConsentSearchWithConsentIDsListAndTime() throws
            Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources;
        ArrayList<String> consentIDs = new ArrayList<>();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storeDataForConsentSearchTest(consentIDs, connection);
            detailedConsentResources = consentCoreDAO.searchConsents(connection, null, consentIDs, null,
                    null, null, null, 1669917425L, 1669917425L,
                    10, 0);
        }

        Assert.assertNotNull(detailedConsentResources);

    }

    @Test
    public void testConsentSearchWithClientIDsList() throws
            Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources;
        ArrayList<String> consentIDs = new ArrayList<>();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storeDataForConsentSearchTest(consentIDs, connection);
            detailedConsentResources = consentCoreDAO.searchConsents(connection, null, null,
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
    public void testConsentSearchWithConsentStatusesList() throws
            Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources;
        ArrayList<String> consentIDs = new ArrayList<>();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storeDataForConsentSearchTest(consentIDs, connection);
            detailedConsentResources = consentCoreDAO.searchConsents(connection, null, null, null,
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
    public void testConsentSearchWithConsentTypesList() throws
            Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources;
        ArrayList<String> consentIDs = new ArrayList<>();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storeDataForConsentSearchTest(consentIDs, connection);
            detailedConsentResources = consentCoreDAO.searchConsents(connection, null, null, null,
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
    public void testConsentSearchWithUserIDsList() throws
            Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources;
        ArrayList<String> consentIDs = new ArrayList<>();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storeDataForConsentSearchTest(consentIDs, connection);
            detailedConsentResources = consentCoreDAO.searchConsents(connection, null, null, null,
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
    public void testConsentSearchWithoutLimitAndOffset() throws
            Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources;
        ArrayList<String> consentIDs = new ArrayList<>();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storeDataForConsentSearchTest(consentIDs, connection);
            detailedConsentResources = consentCoreDAO.searchConsents(connection, null, consentIDs, null,
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
    public void testConsentSearchWithoutLimitButOffset() throws
            Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources;
        ArrayList<String> consentIDs = new ArrayList<>();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storeDataForConsentSearchTest(consentIDs, connection);
            detailedConsentResources = consentCoreDAO.searchConsents(connection, null, consentIDs, null,
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
    public void testConsentSearchForNullValues() throws
            Exception {

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
        Assert.assertEquals(authorizationResources.size(), 0);
        Assert.assertEquals(consentMappingResources.size(), 0);
    }

    @Test
    public void testConsentSearchForNoneNullValues() throws
            Exception {

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
    public void testConsentSearchForNoneNullValuesNegativeCase() throws
            Exception {

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
    public void testConsentSearchWithoutOffsetButLimit() throws
            Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources;
        ArrayList<String> consentIDs = new ArrayList<>();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storeDataForConsentSearchTest(consentIDs, connection);
            detailedConsentResources = consentCoreDAO.searchConsents(connection, null, consentIDs, null,
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
    public void testConsentSearchWithNoParams() throws
            Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources;
        ArrayList<String> consentIDs = new ArrayList<>();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storeDataForConsentSearchTest(consentIDs, connection);
            detailedConsentResources = consentCoreDAO.searchConsents(connection, null, null, null,
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
    public void testConsentSearchWithTimePeriod() throws
            Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources;
        ArrayList<String> consentIDs = new ArrayList<>();
        long currentTime = System.currentTimeMillis() / 1000;

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storeDataForConsentSearchTest(consentIDs, connection);
            detailedConsentResources = consentCoreDAO.searchConsents(connection, null, null, null,
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

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testSearchConsentsSQLError() throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString(),
                Mockito.anyInt(), Mockito.anyInt());
        consentCoreDAO.searchConsents(mockedConnection, null, null, null, null,
                null, null, null, null, null, null);
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testSearchConsentsPreparedResultSetError() throws
            Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.searchConsents(mockedConnection, null, null, null, null,
                null, null, null, null, null, null);
    }

    @Test
    public void testSearchConsentAuthorizations() throws
            Exception {

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

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testSearchConsentAuthorizationsSQLError() throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.searchConsentAuthorizations(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                ConsentMgtDAOTestData.SAMPLE_USER_ID);
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testSearchConsentAuthorizationsResultSetError() throws
            Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.searchConsentAuthorizations(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                ConsentMgtDAOTestData.SAMPLE_USER_ID);
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testSearchConsentAuthorizationsNoRecordsFoundError() throws
            Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(mockedResultSet).when(mockedPreparedStatement).executeQuery();
        Mockito.doReturn(false).when(mockedResultSet).isBeforeFirst();
        consentCoreDAO.searchConsentAuthorizations(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                ConsentMgtDAOTestData.SAMPLE_USER_ID);
    }

    @Test
    public void testStoreConsentStatusAuditRecord() throws
            Exception {

        ConsentResource storedConsentResource;
        ConsentStatusAuditRecord storedConsentStatusAuditRecord;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            ConsentStatusAuditRecord consentStatusAuditRecord = new ConsentStatusAuditRecord();
            consentStatusAuditRecord.setConsentID(storedConsentResource.getConsentID());
            consentStatusAuditRecord.setCurrentStatus(ConsentMgtDAOTestData.SAMPLE_CURRENT_STATUS);
            consentStatusAuditRecord.setReason(ConsentMgtDAOTestData.SAMPLE_REASON);
            consentStatusAuditRecord.setActionBy(ConsentMgtDAOTestData.SAMPLE_ACTION_BY);
            consentStatusAuditRecord.setPreviousStatus(ConsentMgtDAOTestData.SAMPLE_CURRENT_STATUS);


            storedConsentStatusAuditRecord = consentCoreDAO.storeConsentStatusAuditRecord(connection,
                    consentStatusAuditRecord);
        }
        Assert.assertNotNull(storedConsentStatusAuditRecord.getConsentID());
        Assert.assertNotNull(storedConsentStatusAuditRecord.getCurrentStatus());
        Assert.assertNotNull(storedConsentStatusAuditRecord.getReason());
        Assert.assertNotNull(storedConsentStatusAuditRecord.getActionBy());
        Assert.assertNotNull(storedConsentStatusAuditRecord.getPreviousStatus());
        Assert.assertTrue(storedConsentStatusAuditRecord.getActionTime() > 0L);
        Assert.assertNotNull(storedConsentStatusAuditRecord.getStatusAuditID());
    }

    @Test
    public void testStoreConsentStatusAuditRecordWithConsentId() throws
            Exception {

        ConsentResource storedConsentResource;
        ConsentStatusAuditRecord storedConsentStatusAuditRecord;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();
        consentResource.setConsentID("234ba17f-c3ac-4493-9049-d71f99c36dc2");
        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            ConsentStatusAuditRecord consentStatusAuditRecord = new ConsentStatusAuditRecord();
            consentStatusAuditRecord.setConsentID(storedConsentResource.getConsentID());
            consentStatusAuditRecord.setCurrentStatus(ConsentMgtDAOTestData.SAMPLE_CURRENT_STATUS);
            consentStatusAuditRecord.setReason(ConsentMgtDAOTestData.SAMPLE_REASON);
            consentStatusAuditRecord.setActionBy(ConsentMgtDAOTestData.SAMPLE_ACTION_BY);
            consentStatusAuditRecord.setPreviousStatus(ConsentMgtDAOTestData.SAMPLE_PREVIOUS_STATUS);
            consentStatusAuditRecord.setActionTime(1669917425);

            storedConsentStatusAuditRecord = consentCoreDAO.storeConsentStatusAuditRecord(connection,
                    consentStatusAuditRecord);
        }
        Assert.assertEquals(storedConsentStatusAuditRecord.getConsentID(), "234ba17f-c3ac-4493-9049-d71f99c36dc2");
        Assert.assertNotNull(storedConsentStatusAuditRecord.getCurrentStatus());
        Assert.assertNotNull(storedConsentStatusAuditRecord.getReason());
        Assert.assertNotNull(storedConsentStatusAuditRecord.getActionBy());
        Assert.assertNotNull(storedConsentStatusAuditRecord.getPreviousStatus());
        Assert.assertTrue(storedConsentStatusAuditRecord.getActionTime() > 0L);
        Assert.assertNotNull(storedConsentStatusAuditRecord.getStatusAuditID());
    }

    @Test(expectedExceptions = ConsentDataInsertionException.class)
    public void testStoreConsentStatusAuditRecordInsertionError() throws
            Exception {

        ConsentStatusAuditRecord sampleConsentStatusAuditRecord = ConsentMgtDAOTestData
                .getSampleTestConsentStatusAuditRecord(ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                        ConsentMgtDAOTestData.SAMPLE_CURRENT_STATUS);
        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(0).when(mockedPreparedStatement).executeUpdate();
        consentCoreDAO.storeConsentStatusAuditRecord(mockedConnection, sampleConsentStatusAuditRecord);
    }

    @Test(expectedExceptions = ConsentDataInsertionException.class)
    public void testStoreConsentStatusAuditRecordSQLError() throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.storeConsentStatusAuditRecord(mockedConnection, new ConsentStatusAuditRecord());
    }

    @Test
    public void testRetrieveConsentStatusAuditRecordsWithConsentID() throws
            Exception {

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
                    null, null, null, null);
        }
        Assert.assertNotNull(retrievedConsentStatusAuditRecords);
        for (ConsentStatusAuditRecord record :
                retrievedConsentStatusAuditRecords) {
            Assert.assertEquals(storedConsentStatusAuditRecord.getConsentID(), record.getConsentID());
        }
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveConsentStatusAuditRecordsResultSetError() throws
            Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getConsentStatusAuditRecords(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                ConsentMgtDAOTestData.SAMPLE_CURRENT_STATUS, ConsentMgtDAOTestData.SAMPLE_ACTION_BY,
                ConsentMgtDAOTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtDAOTestData.SAMPLE_CONSENT_VALIDITY_PERIOD, ConsentMgtDAOTestData.SAMPLE_AUDIT_ID);
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveConsentStatusAuditRecordsSQLError() throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.getConsentStatusAuditRecords(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                ConsentMgtDAOTestData.SAMPLE_CURRENT_STATUS, ConsentMgtDAOTestData.SAMPLE_ACTION_BY,
                ConsentMgtDAOTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtDAOTestData.SAMPLE_CONSENT_VALIDITY_PERIOD, ConsentMgtDAOTestData.SAMPLE_AUDIT_ID);
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveConsentStatusAuditRecordByConsentIDWithUnmatchedConsentID() throws
            Exception {

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            consentCoreDAO.getConsentStatusAuditRecords(connection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                    null, null, null, null, null);
        }
    }

    @Test
    public void testRetrieveConsentStatusAuditRecordsByConsentIDAndStatus() throws
            Exception {

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
                    null, null, null, null);
        }
        Assert.assertNotNull(retrievedConsentStatusAuditRecords);
        for (ConsentStatusAuditRecord record :
                retrievedConsentStatusAuditRecords) {
            Assert.assertEquals(storedConsentStatusAuditRecord.getConsentID(), record.getConsentID());
            Assert.assertEquals(storedConsentStatusAuditRecord.getCurrentStatus(), record.getCurrentStatus());
        }
    }

    @Test
    public void testRetrieveConsentStatusAuditRecordsByConsentIDStatusAndActionBy() throws
            Exception {

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
                    storedConsentStatusAuditRecord.getActionBy(), null, null, null);
        }
        Assert.assertNotNull(retrievedConsentStatusAuditRecords);
        for (ConsentStatusAuditRecord record :
                retrievedConsentStatusAuditRecords) {
            Assert.assertEquals(storedConsentStatusAuditRecord.getConsentID(), record.getConsentID());
            Assert.assertEquals(storedConsentStatusAuditRecord.getCurrentStatus(), record.getCurrentStatus());
            Assert.assertEquals(storedConsentStatusAuditRecord.getActionBy(), record.getActionBy());
        }
    }

    @Test
    public void testRetrieveConsentAuditRecordByAuditRecordID() throws
            Exception {

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
                    storedConsentStatusAuditRecord.getStatusAuditID());
        }
        Assert.assertNotNull(retrievedConsentStatusAuditRecords);
        for (ConsentStatusAuditRecord record :
                retrievedConsentStatusAuditRecords) {
            Assert.assertEquals(storedConsentStatusAuditRecord.getConsentID(), record.getConsentID());
            Assert.assertEquals(storedConsentStatusAuditRecord.getStatusAuditID(), record.getStatusAuditID());
        }
    }

    @Test
    public void testRetrieveConsentAuditRecordForGivenTime() throws
            Exception {

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
                    null, fromTime, toTime, null);
        }
        Assert.assertNotNull(retrievedConsentStatusAuditRecords);
        for (ConsentStatusAuditRecord record :
                retrievedConsentStatusAuditRecords) {
            Assert.assertEquals(storedConsentResource.getConsentID(), record.getConsentID());
            Assert.assertTrue((record.getActionTime() >= fromTime) && (record.getActionTime() <= toTime));
        }
    }

    @Test
    public void testGetConsentStatusAuditRecordsByConsentId() throws
            Exception {

        ConsentResource storedConsentResource;
        ConsentStatusAuditRecord consentStatusAuditRecord;
        ConsentStatusAuditRecord storedConsentStatusAuditRecord;
        ArrayList<ConsentStatusAuditRecord> retrievedConsentStatusAuditRecords;
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
                    consentIds, null, null);
            retrievedConsentStatusAuditRecordsWithLimit =
                    consentCoreDAO.getConsentStatusAuditRecordsByConsentId(connection, consentIds, 10, 0);
            retrievedConsentStatusAuditRecordsWithLimitAndOffset =
                    consentCoreDAO.getConsentStatusAuditRecordsByConsentId(connection, consentIds, 10, 1);
            retrievedConsentStatusAuditRecordsWithLimitOnly =
                    consentCoreDAO.getConsentStatusAuditRecordsByConsentId(connection, consentIds, 10, null);
            retrievedConsentStatusAuditRecordsWithOffsetOnly =
                    consentCoreDAO.getConsentStatusAuditRecordsByConsentId(connection, consentIds, null, 1);
        }
        Assert.assertNotNull(retrievedConsentStatusAuditRecords);
        Assert.assertNotNull(retrievedConsentStatusAuditRecordsWithLimit);
        Assert.assertTrue(retrievedConsentStatusAuditRecordsWithLimit.size() > 0);
        Assert.assertTrue(retrievedConsentStatusAuditRecordsWithLimitAndOffset.isEmpty());
        Assert.assertFalse(retrievedConsentStatusAuditRecordsWithLimitOnly.isEmpty());
        Assert.assertFalse(retrievedConsentStatusAuditRecordsWithOffsetOnly.isEmpty());
        for (ConsentStatusAuditRecord record :
                retrievedConsentStatusAuditRecords) {
            Assert.assertEquals(storedConsentStatusAuditRecord.getConsentID(), record.getConsentID());
        }
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testGetConsentStatusAuditRecordsByConsentIdSQLError() throws
            Exception {
        ArrayList<String> consentIds = new ArrayList<>();
        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.getConsentStatusAuditRecordsByConsentId(mockedConnection,
                consentIds, null, null);
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testGetConsentStatusAuditRecordsByConsentIdSQLErrorForResults() throws
            Exception {
        ArrayList<String> consentIds = new ArrayList<>();
        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getConsentStatusAuditRecordsByConsentId(mockedConnection,
                consentIds, null, null);
    }

    @Test(dataProvider = "storeConsentHistoryDataProvider",
            dataProviderClass = ConsentManagementDAOTestDataProvider.class)
    public void testStoreConsentAmendmentHistory(String historyID, String recordID, String changedAttributes,
                                                 String consentType, long amendedTimestamp, String amendmentReason)
            throws
            Exception {

        boolean result;

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            result = consentCoreDAO.storeConsentAmendmentHistory(connection, historyID, amendedTimestamp,
                    recordID, consentType, changedAttributes, amendmentReason);
        }
        Assert.assertTrue(result);
    }

    @Test(dataProvider = "storeConsentHistoryDataProvider",
            dataProviderClass = ConsentManagementDAOTestDataProvider.class,
            expectedExceptions = ConsentDataInsertionException.class)
    public void testStoreConsentAmendmentHistoryWithInvalidConsentType(String historyID, String recordID,
                                                                       String changedAttributes, String consentType,
                                                                       long amendedTimestamp, String amendmentReason)
            throws
            Exception {

        boolean result;
        consentType = "sampleConsentType";
        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            result = consentCoreDAO.storeConsentAmendmentHistory(connection, historyID, amendedTimestamp,
                    recordID, consentType, changedAttributes, amendmentReason);
        }
        Assert.assertTrue(result);
    }

    @Test(dataProvider = "storeConsentHistoryDataProvider",
            dataProviderClass = ConsentManagementDAOTestDataProvider.class,
            expectedExceptions = ConsentDataInsertionException.class)
    public void testStoreConsentAmendmentHistoryInsertionError(String historyID, String recordID,
                                                               String changedAttributes, String consentType,
                                                               long amendedTimestamp,
                                                               String amendmentReason)
            throws
            Exception {

        Mockito.doReturn(Mockito.mock(PreparedStatement.class)).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(0).when(Mockito.mock(PreparedStatement.class)).executeUpdate();

        consentCoreDAO.storeConsentAmendmentHistory(mockedConnection, historyID, amendedTimestamp,
                recordID, consentType, changedAttributes, amendmentReason);
    }

    @Test(dataProvider = "storeConsentHistoryDataProvider",
            dataProviderClass = ConsentManagementDAOTestDataProvider.class,
            expectedExceptions = ConsentDataInsertionException.class)
    public void testStoreConsentAmendmentHistorySQLError(String historyID, String recordID,
                                                         String changedAttributes, String consentType,
                                                         long amendedTimestamp, String amendmentReason)
            throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.storeConsentAmendmentHistory(mockedConnection, historyID, amendedTimestamp,
                recordID, consentType, changedAttributes, amendmentReason);
    }

//    @Test(dependsOnMethods = {"testStoreConsentAmendmentHistory"})
//    public void testRetrieveConsentAmendmentHistory() throws Exception {
//
//        Map<String, ConsentHistoryResource> consentHistoryResourcesDataMap;
//        String[] expectedConsentDataTypes = { ConsentMgtDAOConstants.TYPE_CONSENT_BASIC_DATA,
//                                              ConsentMgtDAOConstants.TYPE_CONSENT_ATTRIBUTES_DATA,
//                                              ConsentMgtDAOConstants.TYPE_CONSENT_MAPPING_DATA,
//                                              ConsentMgtDAOConstants.TYPE_CONSENT_AUTH_RESOURCE_DATA,
//                                              "AmendedReason"};
//
//        List<String> expectedConsentDataTypesList = Arrays.asList(expectedConsentDataTypes);
//
//        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
//
//            consentHistoryResourcesDataMap = consentCoreDAO.retrieveConsentAmendmentHistory(connection,
//                    ConsentMgtDAOTestData.getRecordIDListOfSampleConsentHistory(), );
//            Assert.assertNotNull(consentHistoryResourcesDataMap);
//            for (Map.Entry<String, ConsentHistoryResource> consentHistoryDataEntry :
//                    consentHistoryResourcesDataMap.entrySet()) {
//                Assert.assertEquals(ConsentMgtDAOTestData.SAMPLE_HISTORY_ID, consentHistoryDataEntry.getKey());
//                Map<String, Object> consentHistoryData =
//                        consentHistoryDataEntry.getValue().getChangedAttributesJsonDataMap();
//                for (Map.Entry<String, Object> consentHistoryDataTypeEntry :
//                        consentHistoryData.entrySet()) {
//                    Assert.assertNotNull(consentHistoryDataTypeEntry.getKey());
//                    Assert.assertTrue(expectedConsentDataTypesList.contains((consentHistoryDataTypeEntry.getKey())));
//                    Assert.assertNotNull(consentHistoryDataTypeEntry.getValue());
//                }
//            }
//        }
//    }
//
//    @Test (expectedExceptions = ConsentDataRetrievalException.class)
//    public void testRetrieveConsentAmendmentHistoryDataRetrievalError() throws Exception {
//
//        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection).prepareStatement(Mockito.anyString());
//        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
//        consentCoreDAO.retrieveConsentAmendmentHistory(mockedConnection,
//                ConsentMgtDAOTestData.getRecordIDListOfSampleConsentHistory());
//    }
//
//    @Test (expectedExceptions = ConsentDataRetrievalException.class)
//    public void testRetrieveConsentAmendmentHistoryPrepStmtSQLError() throws Exception {
//
//        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
//        consentCoreDAO.retrieveConsentAmendmentHistory(mockedConnection,
//                ConsentMgtDAOTestData.getRecordIDListOfSampleConsentHistory());
//    }
//
//    @Test
//    public void testRetrieveConsentAmendmentHistoryNoRecordsFound() throws Exception {
//
//        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
//                .prepareStatement(Mockito.anyString());
//        Mockito.doReturn(mockedResultSet).when(mockedPreparedStatement).executeQuery();
//        Mockito.doReturn(false).when(mockedResultSet).isBeforeFirst();
//
//        Map<String, ConsentHistoryResource> result = consentCoreDAO.retrieveConsentAmendmentHistory(mockedConnection,
//                ConsentMgtDAOTestData.getRecordIDListOfSampleConsentHistory());
//        Assert.assertEquals(result.size(), 0);
//    }

    @Test
    public void testRetrieveExpiringConsentsWithNoEligibility() throws
            Exception {

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

            expirationEligibleConsents = consentCoreDAO.getExpiringConsents(connection, null,
                    "authorised,awaitingAuthorisation");

        }
        Assert.assertTrue(expirationEligibleConsents.isEmpty());
    }

    @Test(dependsOnMethods = {"testRetrieveExpiringConsentsWithNoEligibility"})
    public void testRetrieveExpiringConsentsWithEligibility() throws
            Exception {

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

            expirationEligibleConsents =
                    consentCoreDAO.getExpiringConsents(connection, ConsentMgtDAOConstants.DEFAULT_ORG,
                            "authorised,awaitingAuthorisation");

        }
        Assert.assertFalse(expirationEligibleConsents.isEmpty());
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveExpiringConsentsDataRetrievalError() throws
            Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection).prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getExpiringConsents(mockedConnection, null, "authorised,awaitingAuthorisation");
    }

    private void storeDataForConsentSearchTest(ArrayList<String> consentIDs,
                                               Connection connection) throws
            ConsentDataInsertionException {

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

}
