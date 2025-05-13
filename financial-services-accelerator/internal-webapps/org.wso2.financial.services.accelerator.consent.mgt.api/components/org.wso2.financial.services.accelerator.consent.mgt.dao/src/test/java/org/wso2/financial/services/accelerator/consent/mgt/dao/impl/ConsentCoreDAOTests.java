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
import java.util.HashMap;
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
        consentResource.setClientId(UUID.randomUUID().toString());
        consentResource.setConsentType(ConsentMgtDAOTestData.SAMPLE_CONSENT_TYPE);
        consentResource.setCurrentStatus(ConsentMgtDAOTestData.SAMPLE_CURRENT_STATUS);
        consentResource.setExpiryTime(ConsentMgtDAOTestData.SAMPLE_CONSENT_VALIDITY_PERIOD);
        consentResource.setConsentId(UUID.randomUUID().toString());
        consentResource.setRecurringIndicator(true);

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
        }
        Assert.assertNotNull(storedConsentResource);
        Assert.assertNotNull(storedConsentResource.getConsentId());
        Assert.assertNotNull(storedConsentResource.getClientId());
        Assert.assertNotNull(storedConsentResource.getConsentType());

        Assert.assertTrue(storedConsentResource.getExpiryTime() > 0);
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
                    storedConsentResource.getConsentId());
        }

        Assert.assertNotNull(retrievedConsentResource);
        Assert.assertEquals(retrievedConsentResource.getConsentId(), storedConsentResource.getConsentId());
        Assert.assertNotNull(retrievedConsentResource.getConsentId());
        Assert.assertNotNull(retrievedConsentResource.getClientId());
        Assert.assertNotNull(retrievedConsentResource.getConsentType());
        Assert.assertTrue(retrievedConsentResource.getExpiryTime() > 0L);
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
                    ConsentMgtDAOTestData.getSampleTestConsentAttributesObject(storedConsentResource.getConsentId()));
            storedAuthorizationResource = consentCoreDAO.storeAuthorizationResource(connection,
                    ConsentMgtDAOTestData.getSampleTestAuthorizationResource(storedConsentResource.getConsentId()));

            retrievedDetailedConsentResource = consentCoreDAO.getDetailedConsentResource(connection,
                    storedConsentResource.getConsentId(), storedConsentResource.getOrgInfo());
        }

        Assert.assertNotNull(retrievedDetailedConsentResource);
        Assert.assertEquals(retrievedDetailedConsentResource.getConsentId(), storedConsentResource.getConsentId());
        Assert.assertEquals(retrievedDetailedConsentResource.getConsentId(), storedConsentResource.getConsentId());
        Assert.assertEquals(retrievedDetailedConsentResource.getClientId(), storedConsentResource.getClientId());
        Assert.assertEquals(retrievedDetailedConsentResource.getReceipt(), storedConsentResource.getReceipt());
        Assert.assertEquals(retrievedDetailedConsentResource.getConsentType(),
                storedConsentResource.getConsentType());
        Assert.assertEquals(retrievedDetailedConsentResource.getCurrentStatus(),
                storedConsentResource.getCurrentStatus());

        Assert.assertEquals(retrievedDetailedConsentResource.getExpiryTime(),
                storedConsentResource.getExpiryTime());
        Assert.assertEquals(retrievedDetailedConsentResource.isRecurringIndicator(),
                storedConsentResource.isRecurringIndicator());
        Assert.assertNotNull(retrievedDetailedConsentResource.getConsentAttributes());
        Assert.assertEquals(retrievedDetailedConsentResource.getAuthorizationResources().get(0).getAuthorizationId(),
                storedAuthorizationResource.getAuthorizationId());

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
                    ConsentMgtDAOTestData.getSampleTestAuthorizationResource(storedConsentResource.getConsentId()));

            retrievedDetailedConsentResource = consentCoreDAO.getDetailedConsentResource(connection,
                    storedAuthorizationResource.getConsentId()
                    , storedConsentResource.getOrgInfo());
        }

        Assert.assertNotNull(retrievedDetailedConsentResource);
        Assert.assertEquals(retrievedDetailedConsentResource.getConsentId(), storedConsentResource.getConsentId());
        Assert.assertEquals(retrievedDetailedConsentResource.getConsentId(), storedConsentResource.getConsentId());
        Assert.assertEquals(retrievedDetailedConsentResource.getClientId(), storedConsentResource.getClientId());
        Assert.assertEquals(retrievedDetailedConsentResource.getReceipt(), storedConsentResource.getReceipt());
        Assert.assertEquals(retrievedDetailedConsentResource.getConsentType(),
                storedConsentResource.getConsentType());
        Assert.assertEquals(retrievedDetailedConsentResource.getCurrentStatus(),
                storedConsentResource.getCurrentStatus());

        Assert.assertEquals(retrievedDetailedConsentResource.getExpiryTime(),
                storedConsentResource.getExpiryTime());
        Assert.assertEquals(retrievedDetailedConsentResource.isRecurringIndicator(),
                storedConsentResource.isRecurringIndicator());
        Assert.assertNotNull(retrievedDetailedConsentResource.getConsentAttributes());
        Assert.assertEquals(retrievedDetailedConsentResource.getAuthorizationResources().get(0).getAuthorizationId(),
                storedAuthorizationResource.getAuthorizationId());

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
                    ConsentMgtDAOTestData.getSampleTestConsentAttributesObject(storedConsentResource.getConsentId()));
            // create two auth resources for same consent id
            storedAuthorizationResourceOne = consentCoreDAO.storeAuthorizationResource(connection,
                    ConsentMgtDAOTestData.getSampleTestAuthorizationResource(storedConsentResource.getConsentId()));
            storedAuthorizationResourceTwo = consentCoreDAO.storeAuthorizationResource(connection,
                    ConsentMgtDAOTestData.getSampleTestAuthorizationResource(storedConsentResource.getConsentId()));
            retrievedDetailedConsentResource = consentCoreDAO.getDetailedConsentResource(connection,
                    storedConsentResource.getConsentId(), storedConsentResource.getOrgInfo());
        }

        Assert.assertNotNull(retrievedDetailedConsentResource);
        Assert.assertEquals(retrievedDetailedConsentResource.getConsentId(), storedConsentResource.getConsentId());
        Assert.assertEquals(retrievedDetailedConsentResource.getAuthorizationResources().get(0).getAuthorizationId(),
                storedAuthorizationResourceOne.getAuthorizationId());
        Assert.assertEquals(retrievedDetailedConsentResource.getAuthorizationResources().get(1).getAuthorizationId(),
                storedAuthorizationResourceTwo.getAuthorizationId());
        /* according to the created consent resource,  retrieved consent resource should contain two auth
        resources and
        three mapping resources
         */
        Assert.assertEquals(retrievedDetailedConsentResource.getAuthorizationResources().size(), 2);
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
        consentCoreDAO.getDetailedConsentResource(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                ConsentMgtDAOTestData.SAMPLE_ORG_ID);
    }

    @Test(expectedExceptions = ConsentDataRetrievalException.class)
    public void testRetrieveDetailedConsentResourceResultSetError() throws
            Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doThrow(SQLException.class).when(mockedPreparedStatement).executeQuery();
        consentCoreDAO.getDetailedConsentResource(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                ConsentMgtDAOTestData.SAMPLE_ORG_ID);
    }

    @Test
    public void testUpdateConsentStatus() throws
            Exception {

        ConsentResource storedConsentResource;
        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
            consentCoreDAO.updateConsentStatus(connection, storedConsentResource.getConsentId(),
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
    public void testUpdateConsentExpiryTime() throws
            Exception {

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();
        long newConsentExpiryTime = 12345;

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            ConsentResource storedConsentResource = consentCoreDAO.storeConsentResource(connection,
                    consentResource);
            consentCoreDAO.updateConsentExpiryTime(connection, storedConsentResource.getConsentId(),
                    newConsentExpiryTime);
        }
    }

    @Test(expectedExceptions = ConsentDataUpdationException.class)
    public void testUpdateConsentExpiryTimeSQLError() throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.updateConsentExpiryTime(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
                ConsentMgtDAOTestData.SAMPLE_CONSENT_VALIDITY_PERIOD);
    }

    @Test(expectedExceptions = ConsentDataUpdationException.class)
    public void testUpdateConsentExpiryTimeUpdateError() throws
            Exception {

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection).prepareStatement(Mockito.anyString());
        Mockito.doReturn(0).when(mockedPreparedStatement).executeUpdate();
        consentCoreDAO.updateConsentExpiryTime(mockedConnection, ConsentMgtDAOTestData.SAMPLE_CONSENT_ID,
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
            authorizationResource.setConsentId(storedConsentResource.getConsentId());
            authorizationResource.setAuthorizationType(ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_TYPE);
            authorizationResource.setUserId(ConsentMgtDAOTestData.SAMPLE_USER_ID);
            authorizationResource.setAuthorizationStatus(ConsentMgtDAOTestData.SAMPLE_AUTHORIZATION_STATUS);

            storedAuthorizationResource = consentCoreDAO.storeAuthorizationResource(connection,
                    authorizationResource);
        }
        Assert.assertNotNull(storedAuthorizationResource.getConsentId());
        Assert.assertNotNull(storedAuthorizationResource.getAuthorizationType());
        Assert.assertNotNull(storedAuthorizationResource.getUserId());
        Assert.assertNotNull(storedAuthorizationResource.getAuthorizationStatus());
        Assert.assertTrue(storedAuthorizationResource.getUpdatedTime() > 0L);
        Assert.assertNotNull(storedAuthorizationResource.getAuthorizationId());
    }

    @Test(expectedExceptions = ConsentDataInsertionException.class)
    public void testStoreAuthorizationResourceInsertionError() throws
            Exception {

        ConsentResource storedConsentResource = ConsentMgtDAOTestData.getSampleStoredTestConsentResource();

        Mockito.doReturn(mockedPreparedStatement).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(0).when(mockedPreparedStatement).executeUpdate();

        consentCoreDAO.storeAuthorizationResource(mockedConnection, ConsentMgtDAOTestData.
                getSampleTestAuthorizationResource(storedConsentResource.getConsentId()));
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
                    .getSampleTestAuthorizationResource(storedConsentResource.getConsentId());

            storedAuthorizationResource = consentCoreDAO.storeAuthorizationResource(connection,
                    authorizationResource);

            retrievedAuthorizationResource = consentCoreDAO.getAuthorizationResource(connection,
                    storedAuthorizationResource.getAuthorizationId(), storedConsentResource.getOrgInfo());
        }
        Assert.assertTrue(retrievedAuthorizationResource.getUpdatedTime() > 0L);
//        Assert.assertNotNull(retrievedAuthorizationResource.getAuthorizationId());
        Assert.assertNotNull(retrievedAuthorizationResource.getAuthorizationStatus());
        Assert.assertNotNull(retrievedAuthorizationResource.getUserId());
        Assert.assertNotNull(retrievedAuthorizationResource.getAuthorizationType());
        Assert.assertEquals(retrievedAuthorizationResource.getConsentId(),
                storedAuthorizationResource.getConsentId());
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
    public void testStoreConsentAttributes() throws
            Exception {

        ConsentResource storedConsentResource;
        ConsentAttributes consentAttributesResource;
        boolean isConsentAttributesStored;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
            consentAttributesResource = new ConsentAttributes();
            consentAttributesResource.setConsentId(storedConsentResource.getConsentId());
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
                        .getConsentId()));
    }

    @Test
    public void testRetrieveConsentAttributes() throws
            Exception {

        ConsentAttributes retrievedConsentAttributesResource;
        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            consentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
            ConsentResource retrievedConsentResource = consentCoreDAO.getConsentResource(connection,
                    consentResource.getConsentId());

            ConsentAttributes consentAttributesResource = ConsentMgtDAOTestData
                    .getSampleTestConsentAttributesObject(retrievedConsentResource.getConsentId());

            consentCoreDAO.storeConsentAttributes(connection, consentAttributesResource);

            retrievedConsentAttributesResource = consentCoreDAO.getConsentAttributes(connection,
                    retrievedConsentResource.getConsentId());
        }
        Assert.assertNotNull(retrievedConsentAttributesResource.getConsentId());
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
                    consentResource.getConsentId());

            retrievedConsentAttributesResource = consentCoreDAO.getConsentAttributes(connection,
                    retrievedConsentResource.getConsentId());
        }
        Assert.assertNotNull(retrievedConsentAttributesResource.getConsentId());
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
//                    consentResource.getConsentId());
//
//            ConsentAttributes consentAttributesResource = ConsentMgtDAOTestData
//                    .getSampleTestConsentAttributesObject(retrievedConsentResource.getConsentId());
//
//            consentCoreDAO.storeConsentAttributes(connection, consentAttributesResource);
//
//            retrievedConsentAttributesResource = consentCoreDAO.getConsentAttributes(connection,
//                    retrievedConsentResource.getConsentId(), ConsentMgtDAOTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
//        }
//        Assert.assertNotNull(retrievedConsentAttributesResource.getConsentId());
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
//                    consentResource.getConsentId());
//
//            retrievedConsentAttributesResource = consentCoreDAO.getConsentAttributes(connection,
//                    retrievedConsentResource.getConsentId(), ConsentMgtDAOTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
//        }
//        Assert.assertNotNull(retrievedConsentAttributesResource.getConsentId());
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
            retrievedConsentResource = consentCoreDAO.getConsentResource(connection, consentResource.getConsentId());

            consentAttributesResource = ConsentMgtDAOTestData
                    .getSampleTestConsentAttributesObject(retrievedConsentResource.getConsentId());

            consentCoreDAO.storeConsentAttributes(connection, consentAttributesResource);

            retrievedValuesMap = consentCoreDAO.getConsentAttributesByName(connection,
                    "x-request-id");

        }
        Assert.assertTrue(retrievedValuesMap.containsKey(consentAttributesResource.getConsentId()));
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
                    consentResource.getConsentId());
            ConsentAttributes consentAttributesResource = ConsentMgtDAOTestData
                    .getSampleTestConsentAttributesObject(retrievedConsentResource.getConsentId());
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
            consentAttributesResource.setConsentId(storedConsentResource.getConsentId());
            consentAttributesResource.setConsentAttributes(ConsentMgtDAOTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
            consentCoreDAO.storeConsentAttributes(connection, consentAttributesResource);
            createdConsentResource = consentCoreDAO.getDetailedConsentResource(connection,
                    storedConsentResource.getConsentId(), storedConsentResource.getOrgInfo());

            consentCoreDAO.updateConsentAttributes(connection, storedConsentResource.getConsentId(),
                    ConsentMgtDAOTestData.CONSENT_ATTRIBUTES_MAP_FOR_UPDATE);
            updatedConsentResource = consentCoreDAO.getDetailedConsentResource(connection,
                    storedConsentResource.getConsentId(), storedConsentResource.getOrgInfo());
        }
        Assert.assertEquals(createdConsentResource.getConsentId(), updatedConsentResource.getConsentId());
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
                    .getSampleTestConsentAttributesObject(storedConsentResource.getConsentId());
            consentCoreDAO.storeConsentAttributes(connection, consentAttributesResource);

            isDeleted = consentCoreDAO.deleteConsentAttributes(connection, storedConsentResource.getConsentId(),
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
                Assert.assertEquals(resource.getConsentId(), authResource.getConsentId());
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
                Assert.assertEquals(resource.getConsentId(), authResource.getConsentId());
            }
        }
    }

//    @Test
//    public void testConsentSearchWithConsentStatusesList() throws
//            Exception {
//
//        ArrayList<DetailedConsentResource> detailedConsentResources;
//        ArrayList<String> consentIDs = new ArrayList<>();
//
//        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
//            storeDataForConsentSearchTest(consentIDs, connection);
//            detailedConsentResources = consentCoreDAO.searchConsents(connection, null, null, null,
//                    null, ConsentMgtDAOTestData.SAMPLE_CONSENT_STATUSES_LIST, null, null,
//                    null, 10, 0);
//        }
//
//        Assert.assertNotNull(detailedConsentResources);
//        for (DetailedConsentResource resource : detailedConsentResources) {
//            Assert.assertNotNull(resource.getAuthorizationResources());
//            Assert.assertNotNull(resource.getConsentMappingResources());
//            Assert.assertNotNull(resource.getConsentAttributes());
//
//            for (AuthorizationResource authResource : resource.getAuthorizationResources()) {
//                Assert.assertEquals(resource.getConsentId(), authResource.getConsentId());
//            }
//        }
//    }

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
                Assert.assertEquals(resource.getConsentId(), authResource.getConsentId());
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
                Assert.assertEquals(resource.getConsentId(), authResource.getConsentId());
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
            Assert.assertNotNull(resource.getConsentAttributes());

            for (AuthorizationResource authResource : resource.getAuthorizationResources()) {
                Assert.assertEquals(resource.getConsentId(), authResource.getConsentId());
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
                Assert.assertEquals(resource.getConsentId(), authResource.getConsentId());
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
        Mockito.doReturn("test").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.AUTH_TYPE);
        Mockito.doReturn("test").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.AUTH_STATUS);
        Mockito.doReturn("test").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.RESOURCE);

        Mockito.doReturn("123456").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.UPDATED_TIME);
        Mockito.doReturn("test").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.USER_ID);
        ConsentCoreDAOImpl dao = new ConsentCoreDAOImpl(new ConsentMgtCommonDBQueries());
        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        dao.setAuthorizationDataInResponseForGroupedQuery(authorizationResources,
                mockedResultSetTemp, "");

        Assert.assertTrue(authorizationResources.size() != 0);
    }

    @Test
    public void testConsentSearchForNoneNullValuesNegativeCase() throws
            Exception {

        ResultSet mockedResultSetTemp = Mockito.mock(ResultSet.class);
        Mockito.doReturn("test").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.AUTH_ID);
        Mockito.doReturn("test").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.MAPPING_ID);
        Mockito.doReturn("test").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.AUTH_TYPE);
        Mockito.doReturn("test").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.AUTH_STATUS);
        Mockito.doReturn("test").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.RESOURCE);

        Mockito.doReturn("123456").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.UPDATED_TIME);
        Mockito.doReturn("test,test2").when(mockedResultSetTemp).getString(ConsentMgtDAOConstants.USER_ID);
        ConsentCoreDAOImpl dao = new ConsentCoreDAOImpl(new ConsentMgtCommonDBQueries());
        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        dao.setAuthorizationDataInResponseForGroupedQuery(authorizationResources,
                mockedResultSetTemp, "");

        Assert.assertTrue(!authorizationResources.isEmpty());
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
                Assert.assertEquals(resource.getConsentId(), authResource.getConsentId());
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
                Assert.assertEquals(resource.getConsentId(), authResource.getConsentId());
            }
        }
    }

//    @Test
//    public void testConsentSearchWithTimePeriod() throws
//            Exception {
//
//        ArrayList<DetailedConsentResource> detailedConsentResources;
//        ArrayList<String> consentIDs = new ArrayList<>();
//        long currentTime = System.currentTimeMillis() / 1000;
//
//        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
//            storeDataForConsentSearchTest(consentIDs, connection);
//            detailedConsentResources = consentCoreDAO.searchConsents(connection, null, null, null,
//                    null, null, null, currentTime,
//                    currentTime + 100, 10, 0);
//        }
//
//        Assert.assertNotNull(detailedConsentResources);
//        for (DetailedConsentResource resource : detailedConsentResources) {
//            Assert.assertNotNull(resource.getAuthorizationResources());
//            Assert.assertNotNull(resource.getConsentAttributes());
//
//            for (AuthorizationResource authResource : resource.getAuthorizationResources()) {
//                Assert.assertEquals(resource.getConsentId(), authResource.getConsentId());
//            }
//
//            Assert.assertTrue((currentTime <= resource.getUpdatedTime())
//                    && (currentTime + 100 >= resource.getUpdatedTime()));
//        }
//    }

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
    public void testStoreConsentStatusAuditRecord() throws
            Exception {

        ConsentResource storedConsentResource;
        ConsentStatusAuditRecord storedConsentStatusAuditRecord;

        ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            ConsentStatusAuditRecord consentStatusAuditRecord = new ConsentStatusAuditRecord();
            consentStatusAuditRecord.setConsentId(storedConsentResource.getConsentId());
            consentStatusAuditRecord.setCurrentStatus(ConsentMgtDAOTestData.SAMPLE_CURRENT_STATUS);
            consentStatusAuditRecord.setReason(ConsentMgtDAOTestData.SAMPLE_REASON);
            consentStatusAuditRecord.setActionBy(ConsentMgtDAOTestData.SAMPLE_ACTION_BY);
            consentStatusAuditRecord.setPreviousStatus(ConsentMgtDAOTestData.SAMPLE_CURRENT_STATUS);


            storedConsentStatusAuditRecord = consentCoreDAO.storeConsentStatusAuditRecord(connection,
                    consentStatusAuditRecord);
        }
        Assert.assertNotNull(storedConsentStatusAuditRecord.getConsentId());
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
        consentResource.setConsentId("234ba17f-c3ac-4493-9049-d71f99c36dc2");
        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {

            storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            ConsentStatusAuditRecord consentStatusAuditRecord = new ConsentStatusAuditRecord();
            consentStatusAuditRecord.setConsentId(storedConsentResource.getConsentId());
            consentStatusAuditRecord.setCurrentStatus(ConsentMgtDAOTestData.SAMPLE_CURRENT_STATUS);
            consentStatusAuditRecord.setReason(ConsentMgtDAOTestData.SAMPLE_REASON);
            consentStatusAuditRecord.setActionBy(ConsentMgtDAOTestData.SAMPLE_ACTION_BY);
            consentStatusAuditRecord.setPreviousStatus(ConsentMgtDAOTestData.SAMPLE_PREVIOUS_STATUS);
            consentStatusAuditRecord.setActionTime(1669917425);

            storedConsentStatusAuditRecord = consentCoreDAO.storeConsentStatusAuditRecord(connection,
                    consentStatusAuditRecord);
        }
        Assert.assertEquals(storedConsentStatusAuditRecord.getConsentId(), "234ba17f-c3ac-4493-9049-d71f99c36dc2");
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
                    .getSampleTestConsentStatusAuditRecord(storedConsentResource.getConsentId(),
                            storedConsentResource.getCurrentStatus());

            storedConsentStatusAuditRecord = consentCoreDAO.storeConsentStatusAuditRecord(connection,
                    consentStatusAuditRecord);

            connection.commit();

            retrievedConsentStatusAuditRecords = consentCoreDAO.getConsentStatusAuditRecords(connection,
                    storedConsentStatusAuditRecord.getConsentId(), null,
                    null, null, null, null);
        }
        Assert.assertNotNull(retrievedConsentStatusAuditRecords);
        for (ConsentStatusAuditRecord record :
                retrievedConsentStatusAuditRecords) {
            Assert.assertEquals(storedConsentStatusAuditRecord.getConsentId(), record.getConsentId());
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
                    .getSampleTestConsentStatusAuditRecord(storedConsentResource.getConsentId(),
                            storedConsentResource.getCurrentStatus());

            storedConsentStatusAuditRecord = consentCoreDAO.storeConsentStatusAuditRecord(connection,
                    consentStatusAuditRecord);

            retrievedConsentStatusAuditRecords = consentCoreDAO.getConsentStatusAuditRecords(connection,
                    storedConsentStatusAuditRecord.getConsentId(), storedConsentStatusAuditRecord.getCurrentStatus(),
                    null, null, null, null);
        }
        Assert.assertNotNull(retrievedConsentStatusAuditRecords);
        for (ConsentStatusAuditRecord record :
                retrievedConsentStatusAuditRecords) {
            Assert.assertEquals(storedConsentStatusAuditRecord.getConsentId(), record.getConsentId());
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
                    .getSampleTestConsentStatusAuditRecord(storedConsentResource.getConsentId(),
                            storedConsentResource.getCurrentStatus());

            storedConsentStatusAuditRecord = consentCoreDAO.storeConsentStatusAuditRecord(connection,
                    consentStatusAuditRecord);

            retrievedConsentStatusAuditRecords = consentCoreDAO.getConsentStatusAuditRecords(connection,
                    storedConsentStatusAuditRecord.getConsentId(), storedConsentStatusAuditRecord.getCurrentStatus(),
                    storedConsentStatusAuditRecord.getActionBy(), null, null, null);
        }
        Assert.assertNotNull(retrievedConsentStatusAuditRecords);
        for (ConsentStatusAuditRecord record :
                retrievedConsentStatusAuditRecords) {
            Assert.assertEquals(storedConsentStatusAuditRecord.getConsentId(), record.getConsentId());
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
                    .getSampleTestConsentStatusAuditRecord(storedConsentResource.getConsentId(),
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
            Assert.assertEquals(storedConsentStatusAuditRecord.getConsentId(), record.getConsentId());
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
                    .getSampleTestConsentStatusAuditRecord(storedConsentResource.getConsentId(),
                            storedConsentResource.getCurrentStatus());

            storedConsentStatusAuditRecord = consentCoreDAO.storeConsentStatusAuditRecord(connection,
                    consentStatusAuditRecord);

            fromTime = Long.sum(storedConsentStatusAuditRecord.getActionTime(), -60);
            toTime = Long.sum(storedConsentStatusAuditRecord.getActionTime(), 60);

            retrievedConsentStatusAuditRecords = consentCoreDAO.getConsentStatusAuditRecords(connection,
                    storedConsentStatusAuditRecord.getConsentId(), null,
                    null, fromTime, toTime, null);
        }
        Assert.assertNotNull(retrievedConsentStatusAuditRecords);
        for (ConsentStatusAuditRecord record :
                retrievedConsentStatusAuditRecords) {
            Assert.assertEquals(storedConsentResource.getConsentId(), record.getConsentId());
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
                    .getSampleTestConsentStatusAuditRecord(storedConsentResource.getConsentId(),
                            storedConsentResource.getCurrentStatus());

            storedConsentStatusAuditRecord = consentCoreDAO.storeConsentStatusAuditRecord(connection,
                    consentStatusAuditRecord);

            connection.commit();
            ArrayList<String> consentIds = new ArrayList<>();
            consentIds.add(storedConsentStatusAuditRecord.getConsentId());
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
            Assert.assertEquals(storedConsentStatusAuditRecord.getConsentId(), record.getConsentId());
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
    public void testStoreConsentAmendmentHistory(String historyID, String statusAuditRecordId, String recordID,
                                                 String changedAttributes,
                                                 String consentType, long amendedTimestamp, String amendmentReason)
            throws
            Exception {

        boolean result;

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            result = consentCoreDAO.storeConsentAmendmentHistory(connection, historyID, amendedTimestamp,
                    statusAuditRecordId, recordID, consentType, changedAttributes, amendmentReason);
        }
        Assert.assertTrue(result);
    }

    @Test(dataProvider = "storeConsentHistoryDataProvider",
            dataProviderClass = ConsentManagementDAOTestDataProvider.class,
            expectedExceptions = ConsentDataInsertionException.class)
    public void testStoreConsentAmendmentHistoryWithInvalidConsentType(String historyID,
                                                                       String statusAuditRecordId, String recordID,
                                                                       String changedAttributes, String consentType,
                                                                       long amendedTimestamp, String amendmentReason)
            throws
            Exception {

        boolean result;
        consentType = "sampleConsentType";
        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            result = consentCoreDAO.storeConsentAmendmentHistory(connection, historyID, amendedTimestamp,
                    statusAuditRecordId, recordID, consentType, changedAttributes, amendmentReason);
        }
        Assert.assertTrue(result);
    }

    @Test(dataProvider = "storeConsentHistoryDataProvider",
            dataProviderClass = ConsentManagementDAOTestDataProvider.class,
            expectedExceptions = ConsentDataInsertionException.class)
    public void testStoreConsentAmendmentHistoryInsertionError(String historyID, String statusAuditRecordId,
                                                               String recordID,
                                                               String changedAttributes, String consentType,
                                                               long amendedTimestamp,
                                                               String amendmentReason)
            throws
            Exception {

        Mockito.doReturn(Mockito.mock(PreparedStatement.class)).when(mockedConnection)
                .prepareStatement(Mockito.anyString());
        Mockito.doReturn(0).when(Mockito.mock(PreparedStatement.class)).executeUpdate();

        consentCoreDAO.storeConsentAmendmentHistory(mockedConnection, historyID, amendedTimestamp,
                statusAuditRecordId, recordID, consentType, changedAttributes, amendmentReason);
    }

    @Test(dataProvider = "storeConsentHistoryDataProvider",
            dataProviderClass = ConsentManagementDAOTestDataProvider.class,
            expectedExceptions = ConsentDataInsertionException.class)
    public void testStoreConsentAmendmentHistorySQLError(String historyID, String statusAuditRecordId, String recordID,
                                                         String changedAttributes, String consentType,
                                                         long amendedTimestamp, String amendmentReason)
            throws
            Exception {

        Mockito.doThrow(SQLException.class).when(mockedConnection).prepareStatement(Mockito.anyString());
        consentCoreDAO.storeConsentAmendmentHistory(mockedConnection, historyID, amendedTimestamp,
                statusAuditRecordId, recordID, consentType, changedAttributes, amendmentReason);
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


    private void storeDataForConsentSearchTest(ArrayList<String> consentIDs,
                                               Connection connection) throws
            ConsentDataInsertionException {

        ArrayList<String> authIDs = new ArrayList<>();

        // Store 3 consent resources
        ArrayList<ConsentResource> consentResources = ConsentMgtDAOTestData.getSampleConsentResourcesList();
        for (ConsentResource resource : consentResources) {
            consentIDs.add(consentCoreDAO.storeConsentResource(connection, resource).getConsentId());
        }

        // Store 2 authorization resources for each stored consent
        ArrayList<AuthorizationResource> authorizationResources =
                ConsentMgtDAOTestData.getSampleAuthorizationResourcesList(consentIDs);
        for (AuthorizationResource resource : authorizationResources) {
            authIDs.add(consentCoreDAO.storeAuthorizationResource(connection, resource).getAuthorizationId());
        }


        // Store consent attributes
        for (String consentId : consentIDs) {
            ConsentAttributes consentAttributesResource = new ConsentAttributes();
            consentAttributesResource.setConsentId(consentId);
            consentAttributesResource.setConsentAttributes(ConsentMgtDAOTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
            consentCoreDAO.storeConsentAttributes(connection, consentAttributesResource);
        }
    }


    @Test
    public void testGetConsentAttributesWithKeys() throws
            Exception {

        ConsentAttributes consentAttributes;
        ArrayList<String> keys = new ArrayList<>();
        keys.add("payment-type");

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            ConsentResource storedConsentResource = consentCoreDAO.storeConsentResource(connection,
                    ConsentMgtDAOTestData.getSampleTestConsentResource());

            consentAttributes = ConsentMgtDAOTestData
                    .getSampleTestConsentAttributesObject(storedConsentResource.getConsentId());
            consentCoreDAO.storeConsentAttributes(connection, consentAttributes);

            ConsentAttributes retrievedAttributes = ((ConsentCoreDAOImpl) consentCoreDAO)
                    .getConsentAttributes(connection, storedConsentResource.getConsentId(), keys);

            Assert.assertNotNull(retrievedAttributes);
            Assert.assertEquals(retrievedAttributes.getConsentAttributes().size(), 1);
            Assert.assertTrue(retrievedAttributes.getConsentAttributes().containsKey("payment-type"));
        }


    }

    @Test
    public void testGetConsentAttributesByName() throws
            Exception {

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            ConsentResource consentResource = ConsentMgtDAOTestData.getSampleTestConsentResource();
            ConsentResource storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);

            ConsentAttributes consentAttributes = new ConsentAttributes();
            consentAttributes.setConsentId(storedConsentResource.getConsentId());
            Map<String, String> attributes = new HashMap<>();
            attributes.put("accountID", "12345");

            consentAttributes.setConsentAttributes(attributes);
            consentCoreDAO.storeConsentAttributes(connection, consentAttributes);

            Map<String, String> results = ((ConsentCoreDAOImpl) consentCoreDAO)
                    .getConsentAttributesByName(connection, "accountID");

            Assert.assertNotNull(results);
            Assert.assertFalse(results.isEmpty());
            Assert.assertTrue(results.containsValue("12345"));
        }
    }


}
