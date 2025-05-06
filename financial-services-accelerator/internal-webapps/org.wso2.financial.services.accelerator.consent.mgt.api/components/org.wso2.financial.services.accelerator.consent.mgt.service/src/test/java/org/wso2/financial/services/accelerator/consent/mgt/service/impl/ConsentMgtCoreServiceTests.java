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
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.consent.mgt.dao.ConsentCoreDAO;
import org.wso2.financial.services.accelerator.consent.mgt.dao.constants.ConsentMgtDAOConstants;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentDataInsertionException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentDataRetrievalException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentDataUpdationException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentMgtException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentFile;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentStatusAuditRecord;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.persistence.ConsentStoreInitializer;
import org.wso2.financial.services.accelerator.consent.mgt.dao.util.DatabaseUtils;
import org.wso2.financial.services.accelerator.consent.mgt.service.util.ConsentCoreServiceUtil;
import org.wso2.financial.services.accelerator.consent.mgt.service.util.ConsentMgtServiceTestData;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;

/**
 * Test for FS consent management core service.
 */
public class ConsentMgtCoreServiceTests {

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
                                .getSampleTestConsentResource(),
                        ConsentMgtServiceTestData.getSampleTestAuthorizationResourcesList(null, null));

        Assert.assertNotNull(detailedConsentResource);
        Assert.assertNotNull(detailedConsentResource.getConsentID());
        Assert.assertNotNull(detailedConsentResource.getClientID());
        Assert.assertNotNull(detailedConsentResource.getReceipt());
        Assert.assertNotNull(detailedConsentResource.getConsentType());
        Assert.assertNotNull(detailedConsentResource.getCurrentStatus());
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
                                .getSampleStoredTestConsentResourceWithAttributes(),
                        ConsentMgtServiceTestData.getSampleTestAuthorizationResourcesList(null, null));

        Assert.assertNotNull(detailedConsentResource);
        Assert.assertNotNull(detailedConsentResource.getConsentID());
        Assert.assertNotNull(detailedConsentResource.getClientID());
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
                                .getSampleStoredTestConsentResourceWithAttributes(),
                        ConsentMgtServiceTestData.getSampleTestAuthorizationResourcesList(null, null));

        Assert.assertNotNull(detailedConsentResource);
        Assert.assertNotNull(detailedConsentResource.getConsentID());
        Assert.assertNotNull(detailedConsentResource.getClientID());
        Assert.assertNotNull(detailedConsentResource.getReceipt());
        Assert.assertNotNull(detailedConsentResource.getConsentType());
        Assert.assertNotNull(detailedConsentResource.getCurrentStatus());
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testCreateAuthorizableConsentWithBulkAuthWithoutClientID() throws
            Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleTestConsentResource();
        consentResource.setClientID(null);

        consentCoreServiceImpl.createConsent(consentResource,
                ConsentMgtServiceTestData.getSampleTestAuthorizationResourcesList(null, null));
    }

    // unit test for ImplicitAndNoAuthType
    @Test(expectedExceptions = ConsentMgtException.class)
    public void testCreateAuthorizableConsentWithBulkAuthWithImplicitAndNoAuthType() throws
            Exception {
        doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));
        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(any(), any());
        doReturn(ConsentMgtServiceTestData.getSampleTestAuthorizationResource(null, null))
                .when(mockedConsentCoreDAO).storeAuthorizationResource(any(), any());

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        AuthorizationResource authorizationResource = new AuthorizationResource();
        authorizationResources.add(authorizationResource);
        consentCoreServiceImpl.createConsent(ConsentMgtServiceTestData
                        .getSampleStoredTestConsentResourceWithAttributes(),
                authorizationResources);
    }


    // unit tests for createConsent with exceptions
    @Test(expectedExceptions = ConsentMgtException.class)
    public void testCreateAuthorizableConsentWithBulkAuthRollback() throws
            Exception {


        doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeConsentResource(any(), any());

        consentCoreServiceImpl.createConsent(ConsentMgtServiceTestData
                        .getSampleTestConsentResource(),
                ConsentMgtServiceTestData.getSampleTestAuthorizationResourcesList(null, null));
    }


    // unit tests for createConsent with exceptions
    @Test(expectedExceptions = ConsentMgtException.class)
    public void testCreateAuthorizableConsentWithBulkAuthRollbackWhenAuditRecord() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(any(), any());
        doReturn(ConsentMgtServiceTestData.getSampleTestAuthorizationResource(null, null))
                .when(mockedConsentCoreDAO).storeAuthorizationResource(any(), any());
        doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeConsentStatusAuditRecord(any(), any(ConsentStatusAuditRecord.class));

        consentCoreServiceImpl.createConsent(ConsentMgtServiceTestData
                        .getSampleTestConsentResource(),
                ConsentMgtServiceTestData.getSampleTestAuthorizationResourcesList(null, null));
    }


    // unit tests for createConsent with exceptions
    @Test(expectedExceptions = ConsentMgtException.class)
    public void testCreateAuthorizableConsentWithBulkAuthRollbackWhenStoringAuthResource() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(any(), any());
        doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeAuthorizationResource(any(), any());

        consentCoreServiceImpl.createConsent(ConsentMgtServiceTestData
                        .getSampleTestConsentResource(),
                ConsentMgtServiceTestData.getSampleTestAuthorizationResourcesList(null, null));
    }


    // unit tests for createConsent with exceptions
    @Test(expectedExceptions = ConsentMgtException.class)
    public void testCreateAuthorizableConsentWithBulkAuthRollbackWhenStoringAuthResourceWithAuditRecord() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(any(), any());
        doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeAuthorizationResource(any(), any());
        doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));

        consentCoreServiceImpl.createConsent(ConsentMgtServiceTestData
                        .getSampleTestConsentResource(),
                ConsentMgtServiceTestData.getSampleTestAuthorizationResourcesList(null, null));
    }

    // unit tests for updateConsentStatusWithImplicitReasonAndUserId
    @Test
    public void testUpdateConsentStatusWithImplicitReasonAndUserId() throws
            Exception {


        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(), anyString());
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource()).when(mockedConsentCoreDAO).
                getDetailedConsentResource(any(), any());

        // handle Status Audit Record
        doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));

        consentCoreServiceImpl.updateConsentStatusWithImplicitReasonAndUserId(ConsentMgtServiceTestData
                        .getSampleStoredConsentResource().getConsentID(),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_REASON,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.ORG_INFO);


    }

    // unit tests for updateConsentStatusWithImplicitReasonAndUserId with exceptions
    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateConsentStatusWithImplicitReasonAndUserIdRollback() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource()).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), any());
        doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateConsentStatus(any(), anyString(), anyString());

        consentCoreServiceImpl.updateConsentStatusWithImplicitReasonAndUserId(ConsentMgtServiceTestData
                        .getSampleStoredConsentResource().getConsentID(),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_REASON,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.ORG_INFO);

    }

    // unit tests for updateConsentStatusWithImplicitReasonAndUserId with exceptions
    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateConsentStatusWithImplicitReasonAndUserIdRollbackWhenAuditRecord() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource()).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), any());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(), anyString());
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource()).when(mockedConsentCoreDAO).
                getDetailedConsentResource(any(), any());
        doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeConsentStatusAuditRecord(any(), any(ConsentStatusAuditRecord.class));

        consentCoreServiceImpl.updateConsentStatusWithImplicitReasonAndUserId(ConsentMgtServiceTestData
                        .getSampleStoredConsentResource().getConsentID(),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_REASON,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.ORG_INFO);

    }

    // unit tests for updateConsentStatusWithImplicitReasonAndUserId with exceptions
    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateConsentStatusWithImplicitReasonAndUserIdRollbackWhenAuditRecordWithConsentResource() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource()).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), any());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(), anyString());
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource()).when(mockedConsentCoreDAO).
                getDetailedConsentResource(any(), any());
        doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeConsentStatusAuditRecord(any(), any(ConsentStatusAuditRecord.class));

        consentCoreServiceImpl.updateConsentStatusWithImplicitReasonAndUserId(ConsentMgtServiceTestData
                        .getSampleStoredConsentResource().getConsentID(),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_REASON,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.ORG_INFO);
    }

    // unit tests for updateConsentStatusWithImplicitReasonAndUserId with exceptions
    @Test(expectedExceptions = ConsentMgtException.class)
    public void
    testUpdateConsentStatusWithImplicitReasonAndUserIdRollbackWhenAuditRecordWithConsentResourceWithConsentStatus()
            throws
            Exception {

        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(), anyString());
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource()).when(mockedConsentCoreDAO).
                getDetailedConsentResource(any(), any());
        doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));
        doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeConsentStatusAuditRecord(any(), any(ConsentStatusAuditRecord.class));

        consentCoreServiceImpl.updateConsentStatusWithImplicitReasonAndUserId(ConsentMgtServiceTestData
                        .getSampleStoredConsentResource().getConsentID(),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_REASON,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.ORG_INFO);

    }

    // unit test for bulkUpdateConsentStatus
    @Test
    public void testBulkUpdateConsentStatus() throws
            Exception {

        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(), anyString());
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource()).when(mockedConsentCoreDAO).
                getDetailedConsentResource(any(), any());
        doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourcesList())
                .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any());
        consentCoreServiceImpl.bulkUpdateConsentStatus(ConsentMgtServiceTestData.ORG_INFO,
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
                getDetailedConsentResource(any(), any());
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourcesList())
                .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any());
        consentCoreServiceImpl.bulkUpdateConsentStatus(ConsentMgtServiceTestData.ORG_INFO,
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
                getDetailedConsentResource(any(), any());
        doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeConsentStatusAuditRecord(any(), any(ConsentStatusAuditRecord.class));

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourcesList())
                .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any());
        consentCoreServiceImpl.bulkUpdateConsentStatus(ConsentMgtServiceTestData.ORG_INFO,
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
                getDetailedConsentResource(any(), any());
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
        consentCoreServiceImpl.bulkUpdateConsentStatus(ConsentMgtServiceTestData.ORG_INFO,
                ConsentMgtServiceTestData.SAMPLE_CLIENT_ID, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_REASON, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CONSENT_STATUSES_LIST);
    }

//
//    @Test
//    public void testCreateExclusiveConsent() throws
//            Exception {
//
//        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourcesList())
//                .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(), any(),
//                        any(), any(), anyLong(), anyLong(), anyInt(),
//                        anyInt());
//        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
//                anyString());
//        doReturn(ConsentMgtServiceTestData
//                .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
//                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
//                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
//                        any(ConsentStatusAuditRecord.class));
//        doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
//                any(ArrayList.class), anyString());
//        doReturn(ConsentMgtServiceTestData.getSampleTestConsentResource())
//                .when(mockedConsentCoreDAO).storeConsentResource(any(), any(ConsentResource.class));
//        doReturn(true).when(mockedConsentCoreDAO).storeConsentAttributes(any(),
//                any(ConsentAttributes.class));
//        doReturn(ConsentMgtServiceTestData
//                .getSampleTestAuthorizationResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID, null))
//                .when(mockedConsentCoreDAO).storeAuthorizationResource(any(),
//                        any(AuthorizationResource.class));
//
//        DetailedConsentResource exclusiveConsent =
//                consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData
//                                .getSampleStoredConsentResource(),
//                        ConsentMgtServiceTestData.SAMPLE_USER_ID,
//                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
//                        ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE,
//                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
//                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
//        Assert.assertNotNull(exclusiveConsent);
//    }


    @Test
    public void testGetConsent() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
                .getConsentResource(any(), anyString());

        // Get consent
        ConsentResource retrievedConsentResource = consentCoreServiceImpl.getConsent(ConsentMgtServiceTestData
                .getSampleStoredConsentResource().getConsentID(), false);

        Assert.assertNotNull(retrievedConsentResource);
    }

    @Test
    public void testGetConsentWithAttributes() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResourceWithAttributes())
                .when(mockedConsentCoreDAO).getConsentResourceWithAttributes(any(), anyString());

        // Get consent
        ConsentResource retrievedConsentResource = consentCoreServiceImpl.getConsent(ConsentMgtServiceTestData
                .getSampleStoredConsentResource().getConsentID(), true);

        Assert.assertNotNull(retrievedConsentResource);
        Assert.assertNotNull(retrievedConsentResource.getConsentAttributes());
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetConsentRollBackWhenRetrieve() throws
            Exception {


        doThrow(
                new ConsentDataRetrievalException(
                        ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG)).when(mockedConsentCoreDAO)
                .getConsentResource(any(), anyString());

        // mock getMessage


        // Get consent
        consentCoreServiceImpl.getConsent(ConsentMgtServiceTestData
                        .getSampleStoredConsentResource().getConsentID(),
                false);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetConsentWithoutConsentID() throws
            Exception {

        consentCoreServiceImpl.getConsent(null, false);
    }

    @Test
    public void testGetDetailedConsent() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

        // Get consent
        DetailedConsentResource retrievedConsentResource = consentCoreServiceImpl
                .getDetailedConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource().getConsentID(),
                        ConsentMgtServiceTestData.ORG_INFO);

        Assert.assertNotNull(retrievedConsentResource);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetDetailedConsentWithoutConsentID() throws
            Exception {

        // Get consent
        consentCoreServiceImpl.getDetailedConsent(null, ConsentMgtServiceTestData.ORG_INFO);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetDetailedConsentWithDataRetrievalException() throws
            Exception {

        doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), anyString());

        // Get consent
        consentCoreServiceImpl.getDetailedConsent(ConsentMgtServiceTestData
                .getSampleStoredConsentResource().getConsentID(), ConsentMgtServiceTestData.ORG_INFO);
    }

    @Test
    public void testCreateConsentFile() throws
            Exception {

        doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentResource(ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS))
                .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
        doReturn(true).when(mockedConsentCoreDAO).storeConsentFile(any(), any());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                anyString());

        consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                        .getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testCreateConsentFileErrorWhenRetrieval() throws
            Exception {

        doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getConsentResource(any(), anyString());

        consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                        .getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testCreateConsentFileRollBackWhenCreation() throws
            Exception {

        ConsentResource storedConsentResource = ConsentMgtServiceTestData
                .getSampleStoredConsentResource(ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);

        doReturn(storedConsentResource).when(mockedConsentCoreDAO)
                .getConsentResource(any(), anyString());
        doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeConsentFile(any(), any());

        consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                        .getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testCreateConsentFileRollBackWhenUpdating() throws
            Exception {

        ConsentResource storedConsentResource = ConsentMgtServiceTestData
                .getSampleStoredConsentResource(ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);

        doReturn(storedConsentResource).when(mockedConsentCoreDAO)
                .getConsentResource(any(), anyString());
        doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateConsentStatus(any(), anyString(), anyString());

        consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                        .getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                storedConsentResource.getCurrentStatus());
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testCreateConsentFileWithInvalidStatus()
            throws
            Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleTestConsentResource();
        consentResource.setCurrentStatus(ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);

        doReturn(consentResource).when(mockedConsentCoreDAO).getConsentResource(any(),
                anyString());

        // Create consent file
        consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                        .getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testCreateConsentFileWithoutFileContent() throws
            Exception {

        consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                        .getSampleConsentFileObject(null), ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testCreateConsentFileWithoutConsentID() throws
            Exception {

        ConsentFile sampleConsentFile =
                ConsentMgtServiceTestData.getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE);

        sampleConsentFile.setConsentID(null);
        consentCoreServiceImpl.createConsentFile(sampleConsentFile, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testCreateConsentFileWithoutNewConsentStatus()
            throws
            Exception {

        consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                        .getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE),
                null, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testCreateConsentFileWithoutApplicableStatusForFileUpload()
            throws
            Exception {

        consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                        .getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                null);
    }

    @Test
    public void testGetConsentFileConsentData() throws
            Exception {

        doReturn(new ConsentFile()).when(mockedConsentCoreDAO)
                .getConsentFile(any(), anyString());
        ConsentFile consentFile = consentCoreServiceImpl
                .getConsentFile("3d22259e-942c-46b8-8f75-a608c677a6e6");
        Assert.assertNotNull(consentFile);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetConsentFileWithoutConsentId() throws
            Exception {

        consentCoreServiceImpl.getConsentFile(null);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetConsentFileWithDataRetrievalError() throws
            Exception {

        doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getConsentFile(any(), anyString());
        ConsentFile consentFile = consentCoreServiceImpl
                .getConsentFile("3d22259e-942c-46b8-8f75-a608c677a6e6");
        Assert.assertNotNull(consentFile);
    }

    @Test
    public void testCreateConsentAuthorization() throws
            Exception {

        AuthorizationResource sampleAuthorizationResource =
                ConsentMgtServiceTestData.getSampleTestAuthorizationResource(sampleID, null);

        doReturn(ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource())
                .when(mockedConsentCoreDAO).storeAuthorizationResource(any(), any());

        //Create a consent authorization resource
        AuthorizationResource storedAuthorizationResource =
                consentCoreServiceImpl.createConsentAuthorization(sampleAuthorizationResource);

        Assert.assertNotNull(storedAuthorizationResource);
        Assert.assertNotNull(storedAuthorizationResource.getAuthorizationID());
        Assert.assertNotNull(storedAuthorizationResource.getConsentID());
        Assert.assertNotNull(storedAuthorizationResource.getAuthorizationType());
        Assert.assertNotNull(storedAuthorizationResource.getUserID());
        Assert.assertNotNull(storedAuthorizationResource.getAuthorizationStatus());
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testCreateConsentAuthorizationRollbackWhenCreation() throws
            Exception {

        AuthorizationResource sampleAuthorizationResource =
                ConsentMgtServiceTestData.getSampleTestAuthorizationResource(sampleID, null);

        doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeAuthorizationResource(any(), any());

        // Get consent
        consentCoreServiceImpl.createConsentAuthorization(sampleAuthorizationResource);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testCreateConsentAuthorizationWithoutConsentID() throws
            Exception {

        AuthorizationResource sampleAuthorizationResource =
                ConsentMgtServiceTestData.getSampleTestAuthorizationResource(null, null);
        sampleAuthorizationResource.setConsentID(null);

        //Create a consent authorization resource
        consentCoreServiceImpl.createConsentAuthorization(sampleAuthorizationResource);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testCreateConsentAuthorizationWithoutAuthorizationStatus() throws
            Exception {

        AuthorizationResource sampleAuthorizationResource =
                ConsentMgtServiceTestData.getSampleTestAuthorizationResource(sampleID, null);

        // Explicitly setting authorization status to null
        sampleAuthorizationResource.setAuthorizationStatus(null);

        //Create a consent authorization resource
        consentCoreServiceImpl.createConsentAuthorization(sampleAuthorizationResource);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testCreateConsentAuthorizationWithoutAuthorizationType() throws
            Exception {

        AuthorizationResource sampleAuthorizationResource =
                ConsentMgtServiceTestData.getSampleTestAuthorizationResource(sampleID, null);
        sampleAuthorizationResource.setAuthorizationType(null);

        //Create a consent authorization resource
        consentCoreServiceImpl.createConsentAuthorization(sampleAuthorizationResource);
    }

    @Test
    public void testGetAuthorizationResource() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource())
                .when(mockedConsentCoreDAO).getAuthorizationResource(any(), anyString(), any());
        AuthorizationResource authorizationResource =
                consentCoreServiceImpl.getAuthorizationResource(ConsentMgtServiceTestData
                        .getSampleStoredTestAuthorizationResource().getAuthorizationID(), null);
        Assert.assertNotNull(authorizationResource);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetAuthorizationResourceWithoutAuthID() throws
            Exception {

        consentCoreServiceImpl.getAuthorizationResource(null, null);
    }

//    @Test(expectedExceptions = ConsentMgtException.class)
//    public void testGetAuthorizationResourceDataRetrieveError() throws
//            Exception {
//
//        doThrow(ConsentMgtException.class)
//                .when(mockedConsentCoreDAO).getAuthorizationResource(any(), anyString(), any());
//        consentCoreServiceImpl.getAuthorizationResource(ConsentMgtServiceTestData
//                .getSampleStoredTestAuthorizationResource().getAuthorizationID(), null);
//    }

    @Test
    public void testSearchAuthorizationsWithConsentID() throws
            Exception {

        ArrayList<String> consentIDs = new ArrayList<>();
        consentIDs.add(UUID.randomUUID().toString());

        doReturn(ConsentMgtServiceTestData
                .getSampleAuthorizationResourcesList(consentIDs))
                .when(mockedConsentCoreDAO).searchConsentAuthorizations(any(), anyString(),
                        anyString());
        ArrayList<AuthorizationResource> retrievedAuthorizations =
                consentCoreServiceImpl.searchAuthorizations(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
        Assert.assertNotNull(retrievedAuthorizations);
    }

    @Test
    public void testSearchAuthorizationsWithUserID() throws
            Exception {

        ArrayList<String> consentIDs = new ArrayList<>();
        consentIDs.add(UUID.randomUUID().toString());

        doReturn(ConsentMgtServiceTestData
                .getSampleAuthorizationResourcesList(consentIDs))
                .when(mockedConsentCoreDAO).searchConsentAuthorizations(any(), anyString(),
                        anyString());
        ArrayList<AuthorizationResource> retrievedAuthorizations =
                consentCoreServiceImpl.searchAuthorizationsForUser(ConsentMgtServiceTestData.SAMPLE_USER_ID);
        Assert.assertNotNull(retrievedAuthorizations);
    }

    @Test
    public void testSearchAuthorizations() throws
            Exception {

        ArrayList<String> consentIDs = new ArrayList<>();
        consentIDs.add(UUID.randomUUID().toString());

        doReturn(ConsentMgtServiceTestData
                .getSampleAuthorizationResourcesList(consentIDs))
                .when(mockedConsentCoreDAO).searchConsentAuthorizations(any(), anyString(),
                        anyString());
        ArrayList<AuthorizationResource> retrievedAuthorizations =
                consentCoreServiceImpl.searchAuthorizations(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID);
        Assert.assertNotNull(retrievedAuthorizations);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testSearchAuthorizationsDataRetrieveError() throws
            Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).searchConsentAuthorizations(any(), anyString(),
                        anyString());

        consentCoreServiceImpl.searchAuthorizations(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_USER_ID);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateAuthorizationStatusWithoutAuthId() throws
            Exception {

        consentCoreServiceImpl.updateAuthorizationStatus(null,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS, ConsentMgtServiceTestData.ORG_INFO);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateAuthorizationStatusWithoutNewAuthStatus() throws
            Exception {

        consentCoreServiceImpl.updateAuthorizationStatus(ConsentMgtServiceTestData.CONSENT_ID,
                null, ConsentMgtServiceTestData.ORG_INFO);
    }

    @Test
    public void testUpdateAuthorizationStatus() throws
            Exception {

        doNothing().when(mockedConsentCoreDAO).updateAuthorizationStatus(any(), anyString(),
                anyString());
        doReturn(ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource())
                .when(mockedConsentCoreDAO).getAuthorizationResource(any(), anyString(), any());

        consentCoreServiceImpl.updateAuthorizationStatus(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS, ConsentMgtServiceTestData.ORG_INFO);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateAuthorizationStatusWithDataUpdateError() throws
            Exception {

        doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateAuthorizationStatus(any(), anyString(), anyString());

        consentCoreServiceImpl.updateAuthorizationStatus(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS, ConsentMgtServiceTestData.ORG_INFO);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateAuthorizationStatusWithDataRetrievalError() throws
            Exception {

        doNothing().when(mockedConsentCoreDAO).updateAuthorizationStatus(any(), anyString(),
                anyString());
        doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getAuthorizationResource(any(), anyString(), any());

        consentCoreServiceImpl.updateAuthorizationStatus(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS, ConsentMgtServiceTestData.ORG_INFO);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateAuthorizationUserWithoutAuthorizationID() throws
            Exception {

        consentCoreServiceImpl.updateAuthorizationUser(null,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.ORG_INFO);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateAuthorizationUserWithoutUserID() throws
            Exception {

        consentCoreServiceImpl.updateAuthorizationUser(ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_1,
                null, ConsentMgtServiceTestData.ORG_INFO);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateAuthorizationUserWithDataUpdateError() throws
            Exception {

        doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateAuthorizationUser(any(), anyString(), anyString());

        consentCoreServiceImpl.updateAuthorizationUser(ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_1,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.ORG_INFO);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateAuthorizationUserWithDataRetrieveError() throws
            Exception {

        doNothing().when(mockedConsentCoreDAO).updateAuthorizationUser(any(),
                anyString(), anyString());
        doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getAuthorizationResource(any(), anyString(), any());

        consentCoreServiceImpl.updateAuthorizationUser(ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_1,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.ORG_INFO);
    }

    @Test
    public void testUpdateAuthorizationUser() throws
            Exception {

        doNothing().when(mockedConsentCoreDAO).updateAuthorizationUser(any(), anyString(),
                anyString());
        doReturn(ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource())
                .when(mockedConsentCoreDAO).getAuthorizationResource(any(), anyString(), any());


        consentCoreServiceImpl.updateAuthorizationUser(ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_1,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.ORG_INFO);
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
        Assert.assertEquals(consentStatusAuditRecord.size(), 4);

    }

    @Test
    public void testGetConsentStatusAuditRecordWithEmptyConsent() throws
            Exception {

        List<String> consentStatusAuditRecord = ConsentCoreServiceUtil
                .getRecordIdListForConsentHistoryRetrieval(ConsentMgtServiceTestData
                        .getSampleDetailedStoredTestConsentResourceWithMultipleAccountIDs());
        Assert.assertNotNull(consentStatusAuditRecord);

        // assert
        Assert.assertEquals(consentStatusAuditRecord.size(), 4);

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

}

