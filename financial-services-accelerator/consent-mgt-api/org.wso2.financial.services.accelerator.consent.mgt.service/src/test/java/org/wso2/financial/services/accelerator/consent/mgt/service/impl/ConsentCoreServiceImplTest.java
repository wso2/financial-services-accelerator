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

package org.wso2.financial.services.accelerator.consent.mgt.service.impl;

import net.minidev.json.JSONObject;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.consent.mgt.dao.ConsentCoreDAO;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentDataDeletionException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentDataInsertionException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentDataRetrievalException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentDataUpdationException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentMgtException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentAttributes;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentHistoryResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentStatusAuditRecord;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.persistence.ConsentStoreInitializer;
import org.wso2.financial.services.accelerator.consent.mgt.dao.util.DatabaseUtils;
import org.wso2.financial.services.accelerator.consent.mgt.service.constants.ConsentCoreServiceConstants;
import org.wso2.financial.services.accelerator.consent.mgt.service.util.ConsentCoreServiceUtil;
import org.wso2.financial.services.accelerator.consent.mgt.service.util.ConsentMgtServiceTestData;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;

/**
 * Test for FS consent management core service.
 */
public class ConsentCoreServiceImplTest {

    private static final Logger log = LoggerFactory.getLogger(ConsentCoreServiceImplTest.class);
    private ConsentCoreServiceImpl consentCoreServiceImpl;
    @Mock
    private ConsentCoreDAO mockedConsentCoreDAO;
    @Mock
    private ConsentCoreServiceImpl consentServiceMock;
    private String sampleID;
    @Mock
    Connection connectionMock;
    @Mock
    ConsentResource consentResourceMock;
    MockedStatic<DatabaseUtils> databaseUtilMockedStatic;
    MockedStatic<ConsentStoreInitializer> consentStoreInitializerMockedStatic;

    @BeforeClass
    public void initTest() {

        connectionMock = Mockito.mock(Connection.class);
        consentCoreServiceImpl = new ConsentCoreServiceImpl();
        mockedConsentCoreDAO = Mockito.mock(ConsentCoreDAO.class);
        consentResourceMock = Mockito.mock(ConsentResource.class);
        consentServiceMock = Mockito.mock(ConsentCoreServiceImpl.class);
    }

    @BeforeMethod
    public void mock() {

        sampleID = UUID.randomUUID().toString();
    }

    @BeforeMethod
    private void mockStaticClasses() {

        databaseUtilMockedStatic = mockStatic(DatabaseUtils.class);
        databaseUtilMockedStatic.when(DatabaseUtils::getDBConnection).thenReturn(connectionMock);

        consentStoreInitializerMockedStatic = mockStatic(ConsentStoreInitializer.class);
        consentStoreInitializerMockedStatic.when(ConsentStoreInitializer::getInitializedConsentCoreDAOImpl)
                .thenReturn(mockedConsentCoreDAO);

    }

    @AfterMethod
    public void tearDown() {
        // Closing the mockStatic after each test
        Mockito.reset(mockedConsentCoreDAO);

        databaseUtilMockedStatic.close();

        consentStoreInitializerMockedStatic.close();

    }

    @Test
    public void testCreateAuthorizableConsentWithBulkAuth() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(any(), any());
        doReturn(ConsentMgtServiceTestData.getSampleTestAuthorizationResource(null, null))
                .when(mockedConsentCoreDAO).storeAuthorizationResource(any(), any());
        doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));

        DetailedConsentResource detailedConsentResource =
                consentCoreServiceImpl.createConsent(ConsentMgtServiceTestData
                        .getSampleDetailedConsentResource());

        Assert.assertNotNull(detailedConsentResource);
        Assert.assertNotNull(detailedConsentResource.getConsentId());
        Assert.assertNotNull(detailedConsentResource.getClientId());
        Assert.assertNotNull(detailedConsentResource.getReceipt());
        Assert.assertNotNull(detailedConsentResource.getConsentType());
        Assert.assertNotNull(detailedConsentResource.getCurrentStatus());
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testCreateAuthorizableConsentWithBulkAuthWithDatabaseConnectionExpection() throws
            Exception {

        databaseUtilMockedStatic.when(DatabaseUtils::getDBConnection)
                .thenThrow(new SQLException("DB connection failed"));

        DetailedConsentResource detailedConsentResource =
                consentCoreServiceImpl.createConsent(ConsentMgtServiceTestData
                        .getSampleDetailedConsentResource());

    }

    @Test
    public void testCreateAuthorizableConsentWithBulkAuthWithAttributes() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(any(), any());
        doReturn(ConsentMgtServiceTestData.getSampleTestAuthorizationResource(null, null))
                .when(mockedConsentCoreDAO).storeAuthorizationResource(any(), any());
        doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));
        doReturn(true).when(mockedConsentCoreDAO).storeConsentAttributes(any(),
                any());

        DetailedConsentResource detailedConsentResource =
                consentCoreServiceImpl.createConsent(ConsentMgtServiceTestData
                        .getSampleDetailedConsentResource());

        Assert.assertNotNull(detailedConsentResource);
        Assert.assertNotNull(detailedConsentResource.getConsentId());
        Assert.assertNotNull(detailedConsentResource.getClientId());
        Assert.assertNotNull(detailedConsentResource.getReceipt());
        Assert.assertNotNull(detailedConsentResource.getConsentType());
        Assert.assertNotNull(detailedConsentResource.getCurrentStatus());
        Assert.assertNotNull(detailedConsentResource.getConsentAttributes());
    }

    @Test
    public void testCreateAuthorizableConsentWithBulkAuthWithoutUserID() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(any(), any());
        doReturn(ConsentMgtServiceTestData.getSampleTestAuthorizationResource(null, null))
                .when(mockedConsentCoreDAO).storeAuthorizationResource(any(), any());
        doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));

        DetailedConsentResource detailedConsentResource =
                consentCoreServiceImpl.createConsent(ConsentMgtServiceTestData
                        .getSampleDetailedConsentResource());

        Assert.assertNotNull(detailedConsentResource);
        Assert.assertNotNull(detailedConsentResource.getConsentId());
        Assert.assertNotNull(detailedConsentResource.getClientId());
        Assert.assertNotNull(detailedConsentResource.getReceipt());
        Assert.assertNotNull(detailedConsentResource.getConsentType());
        Assert.assertNotNull(detailedConsentResource.getCurrentStatus());
    }

    // unit test for ImplicitAndNoAuthType

    // unit tests for createConsent with exceptions
    @Test(expectedExceptions = ConsentMgtException.class)
    public void testCreateAuthorizableConsentWithBulkAuthRollback() throws
            Exception {

        doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeConsentResource(any(), any());

        DetailedConsentResource detailedConsentResource =
                consentCoreServiceImpl.createConsent(ConsentMgtServiceTestData
                        .getSampleDetailedConsentResource());
    }
//
//    // unit tests for createConsent with exceptions
//    @Test(expectedExceptions = ConsentMgtException.class)
//    public void testCreateAuthorizableConsentWithBulkAuthRollbackWhenStoringAuthResource() throws
//            Exception {
//
//        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
//                .storeConsentResource(any(), any());
//        doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
//                .storeBulkAuthorizationResources(any(), any(), any());
//
//        DetailedConsentResource detailedConsentResource =
//                consentCoreServiceImpl.createConsent(ConsentMgtServiceTestData
//                        .getSampleDetailedConsentResource());
//    }

//    // unit tests for createConsent with exceptions
//    @Test(expectedExceptions = ConsentMgtException.class)
//    public void testCreateAuthorizableConsentWithBulkAuthRollbackWhenStoringAuthResourceWithAuditRecord() throws
//            Exception {
//
//        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
//                .storeConsentResource(any(), any());
//        doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
//                .storeBulkAuthorizationResources(any(), any(), any());
//        doReturn(ConsentMgtServiceTestData
//                .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
//                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
//                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
//                        any(ConsentStatusAuditRecord.class));
//
//        DetailedConsentResource detailedConsentResource =
//                consentCoreServiceImpl.createConsent(ConsentMgtServiceTestData
//                        .getSampleDetailedConsentResource());
//    }

    // unit tests for updateConsentStatus
    @Test
    public void testUpdateConsentStatus() throws
            Exception {

        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(), anyString());
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource()).when(mockedConsentCoreDAO).
                getDetailedConsentResource(any(), any(), any());

        // handle Status Audit Record
        doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));

        consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData
                        .getSampleStoredConsentResource().getConsentId(),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_REASON,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.ORG_ID);

    }

    // set debug true
    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateRevokedConsentStatus() throws
            Exception {

        DetailedConsentResource detailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();
        detailedConsentResource.setCurrentStatus(ConsentCoreServiceConstants.CONSENT_REVOKE_STATUS);

        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(), anyString());
        doReturn(detailedConsentResource).when(mockedConsentCoreDAO).
                getDetailedConsentResource(any(), any(), any());

        consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData
                        .getSampleStoredConsentResource().getConsentId(),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_REASON,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.ORG_ID);

    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateStatusWithDatabaseConnectionExpection() throws
            Exception {

        databaseUtilMockedStatic.when(DatabaseUtils::getDBConnection)
                .thenThrow(new SQLException("DB connection failed"));

        consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData
                        .getSampleStoredConsentResource().getConsentId(),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_REASON,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.ORG_ID);

    }

    // unit tests for updateConsentStatus with exceptions
    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateConsentStatusRollback() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource()).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), any(), any());
        doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateConsentStatus(any(), anyString(), anyString());

        consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData
                        .getSampleStoredConsentResource().getConsentId(),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_REASON,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.ORG_ID);

    }

    // unit tests for updateConsentStatus with exceptions
    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateConsentStatusRetrievalRollback() throws
            Exception {

        doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), any(), any());

        consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData
                        .getSampleStoredConsentResource().getConsentId(),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_REASON,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.ORG_ID);

    }

    // unit tests for updateConsentStatus with exceptions
    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateConsentStatusRollbackWhenAuditRecord() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource()).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), any(), any());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(), anyString());
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource()).when(mockedConsentCoreDAO).
                getDetailedConsentResource(any(), any());
        doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeConsentStatusAuditRecord(any(), any(ConsentStatusAuditRecord.class));

        consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData
                        .getSampleStoredConsentResource().getConsentId(),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_REASON,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.ORG_ID);

    }

    // unit tests for updateConsentStatus with exceptions
    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateConsentStatusRollbackWhenAuditRecordWithConsentResource() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource()).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), any(), any());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(), anyString());
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource()).when(mockedConsentCoreDAO).
                getDetailedConsentResource(any(), any());
        doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeConsentStatusAuditRecord(any(), any(ConsentStatusAuditRecord.class));

        consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData
                        .getSampleStoredConsentResource().getConsentId(),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_REASON,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.ORG_ID);
    }

    // unit tests for updateConsentStatus with exceptions
    @Test(expectedExceptions = ConsentMgtException.class)
    public void
    testUpdateConsentStatusRollbackWhenAuditRecordWithConsentResourceWithConsentStatus()
            throws
            Exception {

        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(), anyString());
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource()).when(mockedConsentCoreDAO).
                getDetailedConsentResource(any(), any(), any());
        doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));
        doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeConsentStatusAuditRecord(any(), any(ConsentStatusAuditRecord.class));

        consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData
                        .getSampleStoredConsentResource().getConsentId(),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_REASON,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.ORG_ID);

    }

    // unit test for bulkUpdateConsentStatus
    @Test
    public void testBulkUpdateConsentStatus() throws
            Exception {

        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(), anyString());
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource()).when(mockedConsentCoreDAO).
                getDetailedConsentResource(any(), any(), any());
        doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourcesList())
                .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any());
        consentCoreServiceImpl.bulkUpdateConsentStatus(ConsentMgtServiceTestData.ORG_ID,
                ConsentMgtServiceTestData.SAMPLE_CLIENT_ID, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_REASON, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CONSENT_STATUSES_LIST);
    }

    // unit test for bulkUpdateConsentStatus with exceptions
    @Test(expectedExceptions = ConsentMgtException.class)
    public void testBulkUpdateConsentStatusRollback() throws
            Exception {

        doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateConsentStatus(any(), anyString(), anyString());
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource()).when(mockedConsentCoreDAO).
                getDetailedConsentResource(any(), any(), any());
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourcesList())
                .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any());
        consentCoreServiceImpl.bulkUpdateConsentStatus(ConsentMgtServiceTestData.ORG_ID,
                ConsentMgtServiceTestData.SAMPLE_CLIENT_ID, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_REASON, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CONSENT_STATUSES_LIST);
    }

    // unit test for bulkUpdateConsentStatus with exceptions
    @Test(expectedExceptions = ConsentMgtException.class)
    public void testBulkUpdateConsentStatusRollbackWhenAuditRecord() throws
            Exception {

        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(), anyString());
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource()).when(mockedConsentCoreDAO).
                getDetailedConsentResource(any(), any(), any());
        doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeConsentStatusAuditRecord(any(), any(ConsentStatusAuditRecord.class));

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourcesList())
                .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any());
        consentCoreServiceImpl.bulkUpdateConsentStatus(ConsentMgtServiceTestData.ORG_ID,
                ConsentMgtServiceTestData.SAMPLE_CLIENT_ID, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_REASON, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CONSENT_STATUSES_LIST);
    }

    // unit test for bulkUpdateConsentStatus with exceptions
    @Test(expectedExceptions = ConsentMgtException.class)
    public void testBulkUpdateConsentStatusRollbackWhenAuditRecordWithConsentResource() throws
            Exception {

        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(), anyString());
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource()).when(mockedConsentCoreDAO).
                getDetailedConsentResource(any(), any(), any());
        doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));
        doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeConsentStatusAuditRecord(any(), any(ConsentStatusAuditRecord.class));

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourcesList())
                .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any());
        consentCoreServiceImpl.bulkUpdateConsentStatus(ConsentMgtServiceTestData.ORG_ID,
                ConsentMgtServiceTestData.SAMPLE_CLIENT_ID, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_REASON, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CONSENT_STATUSES_LIST);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testBulkUpdateConsentStatusRollbackWithRetrievalException() throws
            Exception {

        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(), anyString());
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource()).when(mockedConsentCoreDAO).
                getDetailedConsentResource(any(), any(), any());
        doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));
        doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeConsentStatusAuditRecord(any(), any(ConsentStatusAuditRecord.class));

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any());
        consentCoreServiceImpl.bulkUpdateConsentStatus(ConsentMgtServiceTestData.ORG_ID,
                ConsentMgtServiceTestData.SAMPLE_CLIENT_ID, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_REASON, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CONSENT_STATUSES_LIST);
    }

    // unit test for bulkUpdateConsentStatus with exceptions
    @Test(expectedExceptions = ConsentMgtException.class)
    public void testBulkUpdateConsentStatusWithDatabaseConnectionException() throws
            Exception {

        databaseUtilMockedStatic.when(DatabaseUtils::getDBConnection)
                .thenThrow(new SQLException("DB connection failed"));
        consentCoreServiceImpl.bulkUpdateConsentStatus(ConsentMgtServiceTestData.ORG_ID,
                ConsentMgtServiceTestData.SAMPLE_CLIENT_ID, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_REASON, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CONSENT_STATUSES_LIST);
    }

    @Test
    public void testGetDetailedConsent() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString(), any());

        // Get consent
        DetailedConsentResource retrievedConsentResource = consentCoreServiceImpl
                .getDetailedConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource().getConsentId(),
                        ConsentMgtServiceTestData.ORG_ID);

        Assert.assertNotNull(retrievedConsentResource);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetDetailedConsentWithDataBaseConnectionException() throws
            Exception {

        databaseUtilMockedStatic.when(DatabaseUtils::getDBConnection)
                .thenThrow(new SQLException("DB connection failed"));
        // Get consent
        DetailedConsentResource retrievedConsentResource = consentCoreServiceImpl
                .getDetailedConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource().getConsentId(),
                        ConsentMgtServiceTestData.ORG_ID);

    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetDetailedConsentWithDataRetrievalException() throws
            Exception {

        doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), anyString(), any());

        // Get consent
        consentCoreServiceImpl.getDetailedConsent(ConsentMgtServiceTestData
                .getSampleStoredConsentResource().getConsentId(), ConsentMgtServiceTestData.ORG_ID);
    }

    @Test
    public void testCreateConsentAuthorization() throws
            Exception {

        AuthorizationResource sampleAuthorizationResource =
                ConsentMgtServiceTestData.getSampleTestAuthorizationResource(sampleID, null);

        doReturn(ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource())
                .when(mockedConsentCoreDAO).storeAuthorizationResource(any(), any());

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource()).
                when(mockedConsentCoreDAO).getDetailedConsentResource(any(), any());
        //Create a consent authorization resource
        AuthorizationResource storedAuthorizationResource =
                consentCoreServiceImpl.createConsentAuthorization(sampleAuthorizationResource);

        Assert.assertNotNull(storedAuthorizationResource);
        Assert.assertNotNull(storedAuthorizationResource.getAuthorizationId());
        Assert.assertNotNull(storedAuthorizationResource.getConsentId());
        Assert.assertNotNull(storedAuthorizationResource.getAuthorizationType());
        Assert.assertNotNull(storedAuthorizationResource.getUserId());
        Assert.assertNotNull(storedAuthorizationResource.getAuthorizationStatus());
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testCreateConsentAuthorizationRollbackWhenCreation() throws
            Exception {
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource()).
                when(mockedConsentCoreDAO).getDetailedConsentResource(any(), any());
        AuthorizationResource sampleAuthorizationResource =
                ConsentMgtServiceTestData.getSampleTestAuthorizationResource(sampleID, null);

        doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeAuthorizationResource(any(), any());

        // Get consent
        consentCoreServiceImpl.createConsentAuthorization(sampleAuthorizationResource);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testCreateConsentAuthorizationRollbackWhenCreationWithSQLException() throws
            Exception {

        databaseUtilMockedStatic.when(DatabaseUtils::getDBConnection)
                .thenThrow(new SQLException("DB connection failed"));
        AuthorizationResource sampleAuthorizationResource =
                ConsentMgtServiceTestData.getSampleTestAuthorizationResource(sampleID, null);

        // Get consent
        consentCoreServiceImpl.createConsentAuthorization(sampleAuthorizationResource);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testCreateConsentAuthorizationRevokedConsent() throws
            Exception {
        AuthorizationResource sampleAuthorizationResource =
                ConsentMgtServiceTestData.getSampleTestAuthorizationResource(sampleID, null);

        doReturn(ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource())
                .when(mockedConsentCoreDAO).storeAuthorizationResource(any(), any());

        DetailedConsentResource detailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();
        detailedConsentResource.setCurrentStatus(ConsentCoreServiceConstants.CONSENT_REVOKE_STATUS);

        doReturn(detailedConsentResource).
                when(mockedConsentCoreDAO).getDetailedConsentResource(any(), any());

        //Create a consent authorization resource
        AuthorizationResource storedAuthorizationResource =
                consentCoreServiceImpl.createConsentAuthorization(sampleAuthorizationResource);

    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testCreateConsentAuthorizationRetrievalExceptionRollbackWhenCreation() throws
            Exception {
        doThrow(ConsentDataRetrievalException.class).
                when(mockedConsentCoreDAO).getDetailedConsentResource(any(), any());
        AuthorizationResource sampleAuthorizationResource =
                ConsentMgtServiceTestData.getSampleTestAuthorizationResource(sampleID, null);

        // Get consent
        consentCoreServiceImpl.createConsentAuthorization(sampleAuthorizationResource);
    }

    @Test
    public void testGetAuthorizationResource() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource())
                .when(mockedConsentCoreDAO).getAuthorizationResource(any(), anyString(), any());
        AuthorizationResource authorizationResource =
                consentCoreServiceImpl.getAuthorizationResource(ConsentMgtServiceTestData
                        .getSampleStoredTestAuthorizationResource().getAuthorizationId(), null);
        Assert.assertNotNull(authorizationResource);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetAuthorizationResourceRetrievalException() throws
            Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getAuthorizationResource(any(), anyString(), any());
        AuthorizationResource authorizationResource =
                consentCoreServiceImpl.getAuthorizationResource(ConsentMgtServiceTestData
                        .getSampleStoredTestAuthorizationResource().getAuthorizationId(), null);
    }

    // test for ConsentCoreServiceUtil
    @Test
    public void testGetConsentStatusAuditRecord() throws
            Exception {

        List<String> consentStatusAuditRecord = ConsentCoreServiceUtil
                .getRecordIdListForConsentHistoryRetrieval(ConsentMgtServiceTestData
                        .getSampleDetailedStoredTestConsentResource());
        Assert.assertNotNull(consentStatusAuditRecord);

        // assert
        Assert.assertEquals(consentStatusAuditRecord.size(), 2);

    }

    @Test
    public void testGetConsentStatusAuditRecordWithEmptyConsent() throws
            Exception {

        List<String> consentStatusAuditRecord = ConsentCoreServiceUtil
                .getRecordIdListForConsentHistoryRetrieval(ConsentMgtServiceTestData
                        .getSampleDetailedStoredTestConsentResourceWithMultipleAccountIDs());
        Assert.assertNotNull(consentStatusAuditRecord);

        // assert
        Assert.assertEquals(consentStatusAuditRecord.size(), 2);

    }

    // test getChangedBasicConsentDataJSON
    @Test
    public void testGetChangedBasicConsentDataJSON() throws
            Exception {

        JSONObject changedBasicConsentDataJSON = ConsentCoreServiceUtil
                .getChangedBasicConsentDataJSON(
                        ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourceWithMultipleAccountIDs(),
                        ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourceWithMultipleAccountIDs());
        Assert.assertNotNull(changedBasicConsentDataJSON);
    }

    // test getChangedConsentAttributesDataJSON
    @Test
    public void testGetChangedConsentAttributesDataJSON() throws
            Exception {

        JSONObject changedConsentAttributesDataJSON = ConsentCoreServiceUtil
                .getChangedConsentAttributesDataJSON(
                        ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                        ConsentMgtServiceTestData.SAMPLE_CHANGED_CONSENT_ATTRIBUTES_MAP);
        Assert.assertNotNull(changedConsentAttributesDataJSON);
    }

    // test getChangedConsentMappingDataJSONMap
    @Test
    public void testGetChangedConsentMappingDataJSONMap() throws
            Exception {

        Map<String, JSONObject> changedConsentMappingDataJSON = ConsentCoreServiceUtil
                .getChangedConsentMappingDataJSONMap(
                        ConsentMgtServiceTestData.getSampleTestConsentMappingResourceListWithMappingId(),
                        ConsentMgtServiceTestData.getSampleTestConsentMappingResourceListWithMappingId());
        Assert.assertNotNull(changedConsentMappingDataJSON);
    }

    @Test
    public void storeConsentAttributes() throws
            Exception {

        consentCoreServiceImpl.storeConsentAttributes(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void storeConsentAttributesWithDatabaseConnectionException() throws
            Exception {

        databaseUtilMockedStatic.when(DatabaseUtils::getDBConnection)
                .thenThrow(new SQLException("DB connection failed"));

        consentCoreServiceImpl.storeConsentAttributes(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void storeConsentAttributesDataInsertError() throws
            Exception {

        doThrow(ConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentAttributes(any(), any());

        consentCoreServiceImpl.storeConsentAttributes(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    @Test
    public void testGetConsentAttributesWithAttributeKeys() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleTestConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(any(), anyString(), any());
        doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentAttributesObject(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).getConsentAttributes(any(), any(),
                        any(), any());
        ConsentAttributes consentAttributes =
                consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
        Assert.assertNotNull(consentAttributes);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetConsentAttributesWithDatabaseConnectionException() throws
            Exception {

        databaseUtilMockedStatic.when(DatabaseUtils::getDBConnection)
                .thenThrow(new SQLException("DB connection failed"));

        ConsentAttributes consentAttributes =
                consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetConsentAttributesWithEmptyAttributeKeys() throws
            Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentAttributes(any(), any(), anyString());
        consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                new ArrayList<>());
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetConsentAttributesConsentResourceReteivealError() throws
            Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentAttributes(any(), anyString(), any());
        consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetConsentAttributesDataRetrieveError() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleTestConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(any(), any(), any());
        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentAttributes(any(), anyString(), any(), any());
        consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    @Test
    public void testGetConsentAttributes() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleTestConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(any(), anyString(), any());
        doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentAttributesObject(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).getConsentAttributes(any(), any(), any());
        ConsentAttributes consentAttributes =
                consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
        Assert.assertNotNull(consentAttributes);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetConsentAttributesWithoutAttributeKeys() throws
            Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentAttributes(any(), any(), anyString());
        consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetConsentAttributesConsentResourceRetrieveError() throws
            Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentResource(any(), anyString(), any());
        doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentAttributesObject(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).getConsentAttributes(any(), any(), any(), any());
        ConsentAttributes consentAttributes =
                consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
        Assert.assertNotNull(consentAttributes);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetConsentAttributesWithDataRetrieveError() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleTestConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(any(), anyString(), any());
        doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getConsentAttributes(any(), any(), any());
        ConsentAttributes consentAttributes =
                consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
        Assert.assertNotNull(consentAttributes);
    }

    @Test
    public void testUpdateConsentAttributes() throws
            Exception {

        doNothing().when(mockedConsentCoreDAO).updateConsentAttributes(any(),
                anyString(), anyMap());
        doReturn(ConsentMgtServiceTestData.getSampleTestConsentAttributesObject(
                ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).getConsentAttributes(any(), any(), anyString());
        consentCoreServiceImpl.updateConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateConsentAttributesWithDatabaseConnectionException() throws
            Exception {

        databaseUtilMockedStatic.when(DatabaseUtils::getDBConnection)
                .thenThrow(new SQLException("DB connection failed"));
        consentCoreServiceImpl.updateConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }
//
//    @Test(expectedExceptions = ConsentMgtException.class)
//    public void testUpdateConsentAttributesWithoutConsentId() throws
//            Exception {
//
//        consentCoreServiceImpl.updateConsentAttributes(null,
//                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
//    }

//    @Test(expectedExceptions = ConsentMgtException.class)
//    public void testUpdateConsentAttributesWithoutAttributes() throws
//            Exception {
//
//        consentCoreServiceImpl.updateConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID, null);
//    }

//    @Test(expectedExceptions = ConsentMgtException.class)
//    public void testUpdateConsentAttributesWithEmptyAttributes() throws
//            Exception {
//
//        consentCoreServiceImpl.updateConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
//                new HashMap<>());
//    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateConsentAttributesWithDataUpdateError() throws
            Exception {

        doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateConsentAttributes(any(), anyString(), anyMap());
        doReturn(ConsentMgtServiceTestData.getSampleTestConsentAttributesObject(
                ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).getConsentAttributes(any(), any(), anyString());
        consentCoreServiceImpl.updateConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateConsentAttributesWithDataRetrieveError() throws
            Exception {

        doNothing().when(mockedConsentCoreDAO).updateConsentAttributes(any(),
                anyString(), anyMap());
        doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getConsentAttributes(any(), any(), any());
        consentCoreServiceImpl.updateConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    @Test
    public void testDeleteConsentAttributes() throws
            Exception {

        doReturn(true).when(mockedConsentCoreDAO).deleteConsentAttributes(any(),
                anyString(), any());
        consentCoreServiceImpl.deleteConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testDeleteConsentAttributesDeleteError() throws
            Exception {

        doThrow(ConsentDataDeletionException.class)
                .when(mockedConsentCoreDAO).deleteConsentAttributes(any(), anyString(),
                        any());
        consentCoreServiceImpl.deleteConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testDeleteConsentAttributesDatabaseConnectionException() throws
            Exception {

        databaseUtilMockedStatic.when(DatabaseUtils::getDBConnection)
                .thenThrow(new SQLException("DB connection failed"));
        consentCoreServiceImpl.deleteConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    @Test
    public void testSearchConsentStatusAuditRecords() throws
            Exception {

        ArrayList<ConsentStatusAuditRecord> consentStatusAuditRecords = new ArrayList<>();

        doReturn(consentStatusAuditRecords).when(mockedConsentCoreDAO)
                .getConsentStatusAuditRecordsByConsentId(any(), any(ArrayList.class),
                        anyInt(),
                        anyInt());
        ArrayList<ConsentStatusAuditRecord> statusAuditRecords =
                consentCoreServiceImpl.searchConsentStatusAuditRecords(ConsentMgtServiceTestData.CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_ACTION_BY,
                        0L, 0L, "1234");
        Assert.assertNotNull(statusAuditRecords);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testSearchConsentStatusAuditRecordsWithSQLException() throws
            Exception {

        // Mock the static DatabaseUtils.getDBConnection() to throw SQLException

        databaseUtilMockedStatic.when(DatabaseUtils::getDBConnection)
                .thenThrow(new SQLException("DB connection failed"));

        consentCoreServiceImpl.searchConsentStatusAuditRecords(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_ACTION_BY,
                0L, 0L, "1234");

    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testSearchConsentStatusAuditRecordsWithDataRetrievalError() throws
            Exception {

        doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getConsentStatusAuditRecords(any(), anyString(), anyString(),
                        anyString(), anyLong(), anyLong(), anyString());
        ArrayList<ConsentStatusAuditRecord> statusAuditRecords =
                consentCoreServiceImpl.searchConsentStatusAuditRecords(ConsentMgtServiceTestData.CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_ACTION_BY,
                        0L, 0L, "1234");
        Assert.assertNotNull(statusAuditRecords);
    }

    @Test
    public void testGetConsentStatusAuditRecords() throws
            Exception {

        ArrayList<ConsentStatusAuditRecord> consentStatusAuditRecords = new ArrayList<>();
        ArrayList<String> consentIds = new ArrayList<>();

        doReturn(consentStatusAuditRecords).when(mockedConsentCoreDAO)
                .getConsentStatusAuditRecordsByConsentId(any(), any(ArrayList.class),
                        anyInt(),
                        anyInt());
        ArrayList<ConsentStatusAuditRecord> statusAuditRecords =
                consentCoreServiceImpl.getConsentStatusAuditRecords(consentIds, null, null);
        Assert.assertNotNull(statusAuditRecords);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetConsentStatusAuditRecordsWithDataRetrievalError() throws
            Exception {
        ArrayList<String> consentIds = new ArrayList<>();

        doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getConsentStatusAuditRecordsByConsentId(any(), any(ArrayList.class),
                        any(), any());
        ArrayList<ConsentStatusAuditRecord> statusAuditRecords =
                consentCoreServiceImpl.getConsentStatusAuditRecords(consentIds, null, null);
        Assert.assertNotNull(statusAuditRecords);
    }

    @Test
    public void testStoreConsentAmendmentHistory() throws
            Exception {

        boolean result = consentCoreServiceImpl.storeConsentAmendmentHistory(sampleID,
                ConsentMgtServiceTestData.getSampleTestConsentHistoryResource(),
                ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource());

        Assert.assertTrue(result);
    }

    @Test
    public void testStoreConsentAmendmentHistoryWithoutPassingCurrentConsent() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

        ConsentHistoryResource consentHistoryResource = new ConsentHistoryResource();
        consentHistoryResource.setTimestamp(ConsentMgtServiceTestData.SAMPLE_CONSENT_AMENDMENT_TIMESTAMP);
        consentHistoryResource.setReason(ConsentMgtServiceTestData.SAMPLE_AMENDMENT_REASON);
        consentHistoryResource.setDetailedConsentResource(ConsentMgtServiceTestData
                .getSampleDetailedStoredTestCurrentConsentResource());

        boolean result = consentCoreServiceImpl.storeConsentAmendmentHistory(
                ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                consentHistoryResource, null);

        Assert.assertTrue(result);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testStoreConsentAmendmentHistoryWithoutConsentID() throws
            Exception {

        consentCoreServiceImpl.storeConsentAmendmentHistory(null,
                ConsentMgtServiceTestData.getSampleTestConsentHistoryResource(),
                ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource());
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testStoreConsentAmendmentHistoryWithoutConsentHistoryResource() throws
            Exception {

        consentCoreServiceImpl.storeConsentAmendmentHistory(sampleID,
                null,
                ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource());
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testStoreConsentAmendmentHistoryWithZeroAsConsentAmendedTimestamp() throws
            Exception {

        ConsentHistoryResource consentHistoryResource = ConsentMgtServiceTestData
                .getSampleTestConsentHistoryResource();
        consentHistoryResource.setTimestamp(0);
        consentCoreServiceImpl.storeConsentAmendmentHistory(sampleID, consentHistoryResource,
                ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource());
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testStoreConsentAmendmentHistoryWithoutConsentAmendedReason() throws
            Exception {

        ConsentHistoryResource consentHistoryResource = ConsentMgtServiceTestData
                .getSampleTestConsentHistoryResource();
        consentHistoryResource.setReason(null);
        consentCoreServiceImpl.storeConsentAmendmentHistory(sampleID,
                consentHistoryResource,
                ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource());
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testStoreConsentAmendmentHistoryDataInsertError() throws
            Exception {

        doThrow(ConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentAmendmentHistory(any(), anyString(),
                        anyLong(), anyString(), anyString(), anyString(), anyString(),
                        anyString());

        consentCoreServiceImpl.storeConsentAmendmentHistory(sampleID,
                ConsentMgtServiceTestData.getSampleTestConsentHistoryResource(),
                ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource());
    }

    @Test
    public void testSearchConsents() throws
            Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
        detailedConsentResources.add(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());

        doReturn(detailedConsentResources)
                .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(), any(),
                        any(), any(), anyLong(), anyLong(), anyInt(),
                        anyInt());

        consentCoreServiceImpl.searchDetailedConsents(null, ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST,
                ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPES_LIST,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_STATUSES_LIST,
                ConsentMgtServiceTestData.SAMPLE_USER_IDS_LIST,
                12345L, 23456L, null, null);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testSearchConsentsWithDatabaseConnectionException() throws
            Exception {

        databaseUtilMockedStatic.when(DatabaseUtils::getDBConnection)
                .thenThrow(new SQLException("DB connection failed"));

        consentCoreServiceImpl.searchDetailedConsents(null, ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST,
                ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPES_LIST,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_STATUSES_LIST,
                ConsentMgtServiceTestData.SAMPLE_USER_IDS_LIST,
                12345L, 23456L, null, null);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testSearchConsentsRetrieveError() throws
            Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(), any(),
                        any(), any(), any(), any(), any(), any());

        consentCoreServiceImpl.searchDetailedConsents(null, ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST,
                ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPES_LIST,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_STATUSES_LIST,
                ConsentMgtServiceTestData.SAMPLE_USER_IDS_LIST,
                12345L, 23456L, null, null);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testSearchConsentsWithLimits() throws
            Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(), any(),
                        any(), any(), anyLong(), anyLong(), anyInt(),
                        anyInt());

        consentCoreServiceImpl.searchDetailedConsents(null, ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST,
                ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPES_LIST,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_STATUSES_LIST,
                ConsentMgtServiceTestData.SAMPLE_USER_IDS_LIST,
                12345L, 23456L, 1, 0);
    }

    @Test
    public void testDeleteConsent() throws
            Exception {
        // Mock DAO behavior
        doNothing().when(mockedConsentCoreDAO).deleteConsent(any(), any());

        // Call the method
        boolean result = consentCoreServiceImpl.deleteConsent(ConsentMgtServiceTestData.CONSENT_ID);

        // Assert the result
        Assert.assertTrue(result);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testDeleteConsentWithException() throws
            Exception {

        // Mock DAO to throw an exception
        doThrow(ConsentDataDeletionException.class).when(mockedConsentCoreDAO).deleteConsent(any(), any());

        // Call the method
        consentCoreServiceImpl.deleteConsent(ConsentMgtServiceTestData.CONSENT_ID);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testDeleteConsentWithDatabaseConnectionException() throws
            Exception {

        databaseUtilMockedStatic.when(DatabaseUtils::getDBConnection)
                .thenThrow(new SQLException("DB connection failed"));

        // Call the method
        consentCoreServiceImpl.deleteConsent(ConsentMgtServiceTestData.CONSENT_ID);
    }

    @Test
    public void testRevokeConsent() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), any(), any());
        // handle Status Audit Record
        doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));

        // Mock DAO behavior
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), any(), any());

        // Call the method
        boolean result = consentCoreServiceImpl.revokeConsent(
                ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.ORG_ID,
                ConsentMgtServiceTestData.SAMPLE_ACTION_BY,
                ConsentMgtServiceTestData.SAMPLE_REASON
                                                             );

        // Assert the result
        Assert.assertTrue(result);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testRevokeRevokedConsent() throws
            Exception {

        DetailedConsentResource detailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();
        detailedConsentResource.setCurrentStatus(ConsentCoreServiceConstants.CONSENT_REVOKE_STATUS);
        doReturn(detailedConsentResource)
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), any(), any());
        // Call the method
        boolean result = consentCoreServiceImpl.revokeConsent(
                ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.ORG_ID,
                ConsentMgtServiceTestData.SAMPLE_ACTION_BY,
                ConsentMgtServiceTestData.SAMPLE_REASON
                                                             );

        // Assert the result
        Assert.assertTrue(result);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testRevokeConsentWithDatabaseConnectionexception() throws
            Exception {

        databaseUtilMockedStatic.when(DatabaseUtils::getDBConnection)
                .thenThrow(new SQLException("DB connection failed"));
        // Call the method
        boolean result = consentCoreServiceImpl.revokeConsent(
                ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.ORG_ID,
                ConsentMgtServiceTestData.SAMPLE_ACTION_BY,
                ConsentMgtServiceTestData.SAMPLE_REASON
                                                             );

    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testRevokeConsentWithException() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), any(), any());
        // Mock DAO to throw an exception
        doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateConsentStatus(any(), any(), any());

        // Call the method
        consentCoreServiceImpl.revokeConsent(
                ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.ORG_ID,
                ConsentMgtServiceTestData.SAMPLE_ACTION_BY,
                ConsentMgtServiceTestData.SAMPLE_REASON
                                            );
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testRevokeConsentWithRetrievalException() throws
            Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), any(), any());

        // Call the method
        consentCoreServiceImpl.revokeConsent(
                ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.ORG_ID,
                ConsentMgtServiceTestData.SAMPLE_ACTION_BY,
                ConsentMgtServiceTestData.SAMPLE_REASON
                                            );
    }

    // Test for successful update of consent expiry time
    @Test
    public void testUpdateConsentExpiryTime() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource()).when(
                        mockedConsentCoreDAO).
                getDetailedConsentResource(any(), any(), any());
        doNothing().when(mockedConsentCoreDAO).updateConsentExpiryTime(any(), anyString(), anyLong());

        // Call the method
        consentCoreServiceImpl.updateConsentExpiryTime(
                ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_EXPIRY_TIME,
                ConsentMgtServiceTestData.SAMPLE_ACTION_BY
                                                      );

    }

    // Test for exception during update of consent expiry time
    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateConsentExpiryTimeWithException() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource()).when(
                        mockedConsentCoreDAO).
                getDetailedConsentResource(any(), any(), any());

        doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateConsentExpiryTime(any(), anyString(), anyLong());

        // Call the method
        consentCoreServiceImpl.updateConsentExpiryTime(
                ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_EXPIRY_TIME,
                ConsentMgtServiceTestData.SAMPLE_ACTION_BY
                                                      );
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateConsentExpiryTimeWithRetrievalException() throws
            Exception {

        doThrow(ConsentDataRetrievalException.class).when(
                        mockedConsentCoreDAO).
                getDetailedConsentResource(any(), any(), any());

        // Call the method
        consentCoreServiceImpl.updateConsentExpiryTime(
                ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_EXPIRY_TIME,
                ConsentMgtServiceTestData.SAMPLE_ACTION_BY
                                                      );
    }

    // Test for exception during update of consent expiry time
    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateConsentExpiryTimeWithInvalidExpiryTime() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource()).when(
                        mockedConsentCoreDAO).
                getDetailedConsentResource(any(), any(), any());

        // Call the method
        consentCoreServiceImpl.updateConsentExpiryTime(
                ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_EXPIRY_TIME - 2000000,
                ConsentMgtServiceTestData.SAMPLE_ACTION_BY
                                                      );

    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateConsentExpiryTimeFoRevokedConsent() throws
            Exception {

        DetailedConsentResource detailedConsentResource = ConsentMgtServiceTestData.
                getSampleDetailedStoredTestCurrentConsentResource();
        detailedConsentResource.setCurrentStatus(ConsentCoreServiceConstants.CONSENT_REVOKE_STATUS);
        doReturn(detailedConsentResource).when(
                        mockedConsentCoreDAO).
                getDetailedConsentResource(any(), any(), any());

        // Call the method
        consentCoreServiceImpl.updateConsentExpiryTime(
                ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_EXPIRY_TIME,
                ConsentMgtServiceTestData.SAMPLE_ACTION_BY
                                                      );

    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateConsentExpiryTimeConnectionExpection() throws
            Exception {

        databaseUtilMockedStatic.when(DatabaseUtils::getDBConnection)
                .thenThrow(new SQLException("DB connection failed"));
        // Call the method
        consentCoreServiceImpl.updateConsentExpiryTime(
                ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_EXPIRY_TIME,
                ConsentMgtServiceTestData.SAMPLE_ACTION_BY
                                                      );

    }

    @Test
    public void testUpdateAuthorizationResource() throws
            Exception {
        doReturn(Mockito.mock(AuthorizationResource.class)).when(mockedConsentCoreDAO)
                .updateAuthorizationResource(any(),
                        anyString(), any());

        // Call the method
        consentCoreServiceImpl.updateAuthorizationResource(
                ConsentMgtServiceTestData.AUTHORIZATION_ID,
                ConsentMgtServiceTestData.getSampleTestAuthorizationResource(
                        ConsentMgtServiceTestData.AUTHORIZATION_ID, ConsentMgtServiceTestData.CONSENT_ID),
                ConsentMgtServiceTestData.SAMPLE_ACTION_BY
                                                          );

    }

    // Test for exception during update of authorization resource
    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateAuthorizationResourceWithException() throws
            Exception {
        doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateAuthorizationResource(any(), anyString(), any());

        // Call the method
        consentCoreServiceImpl.updateAuthorizationResource(
                ConsentMgtServiceTestData.AUTHORIZATION_ID,
                ConsentMgtServiceTestData.getSampleTestAuthorizationResource(
                        ConsentMgtServiceTestData.AUTHORIZATION_ID, ConsentMgtServiceTestData.CONSENT_ID),
                ConsentMgtServiceTestData.SAMPLE_ACTION_BY
                                                          );
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateAuthorizatationResource_SQLException() throws
            Exception {

        // Mock the static DatabaseUtils.getDBConnection() to throw SQLException

        databaseUtilMockedStatic.when(DatabaseUtils::getDBConnection)
                .thenThrow(new SQLException("DB connection failed"));

        consentCoreServiceImpl.updateAuthorizationResource(
                ConsentMgtServiceTestData.AUTHORIZATION_ID,
                ConsentMgtServiceTestData.getSampleTestAuthorizationResource(
                        ConsentMgtServiceTestData.AUTHORIZATION_ID, ConsentMgtServiceTestData.CONSENT_ID),
                ConsentMgtServiceTestData.SAMPLE_ACTION_BY
                                                          );
    }

    @Test
    public void testDeleteAuthorizationResource() throws
            Exception {
        // Mock DAO behavior
        doNothing().when(mockedConsentCoreDAO).deleteAuthorizationResource(any(), anyString());

        // Call the method
        boolean result = consentCoreServiceImpl.deleteAuthorizationResource(
                ConsentMgtServiceTestData.AUTHORIZATION_ID);

        // Assert the result
        Assert.assertTrue(result);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testDeleteAuthorizationResourceWithException() throws
            Exception {
        // Mock DAO to throw an exception
        doThrow(ConsentDataDeletionException.class).when(mockedConsentCoreDAO)
                .deleteAuthorizationResource(any(), anyString());

        // Call the method
        consentCoreServiceImpl.deleteAuthorizationResource(
                ConsentMgtServiceTestData.AUTHORIZATION_ID);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testDeleteAuthorizationResourceWithRetrievalException() throws
            Exception {
        // Mock DAO to throw an exception
        doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getAuthorizationResource(any(), any(), any());

        // Call the method
        consentCoreServiceImpl.deleteAuthorizationResource(
                ConsentMgtServiceTestData.AUTHORIZATION_ID);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetAuthorizationResource_SQLException() throws
            Exception {

        // Mock the static DatabaseUtils.getDBConnection() to throw SQLException

        databaseUtilMockedStatic.when(DatabaseUtils::getDBConnection)
                .thenThrow(new SQLException("DB connection failed"));

        // Call method - should throw ConsentMgtException
        consentCoreServiceImpl.getAuthorizationResource(ConsentMgtServiceTestData.AUTHORIZATION_ID
                , ConsentMgtServiceTestData.ORG_ID);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testDeleteAuthorizationResource_SQLException() throws
            Exception {

        // Mock the static DatabaseUtils.getDBConnection() to throw SQLException

        databaseUtilMockedStatic.when(DatabaseUtils::getDBConnection)
                .thenThrow(new SQLException("DB connection failed"));

        // Call method - should throw ConsentMgtException
        consentCoreServiceImpl.deleteAuthorizationResource(ConsentMgtServiceTestData.AUTHORIZATION_ID);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetConsentAttributes_SQLException() throws
            Exception {

        // Mock the static DatabaseUtils.getDBConnection() to throw SQLException

        databaseUtilMockedStatic.when(DatabaseUtils::getDBConnection)
                .thenThrow(new SQLException("DB connection failed"));

        // Call method - should throw ConsentMgtException
        consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.AUTHORIZATION_ID);
    }
}
