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

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
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
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentStatusAuditRecord;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.persistence.ConsentStoreInitializer;
import org.wso2.financial.services.accelerator.consent.mgt.dao.util.DatabaseUtils;
import org.wso2.financial.services.accelerator.consent.mgt.service.constants.ConsentCoreServiceConstants;
import org.wso2.financial.services.accelerator.consent.mgt.service.util.ConsentMgtServiceTestData;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
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




//    @Test
//    public void testUpdateConsentStatus() throws
//            Exception {
//
//        DetailedConsentResource retrievedDetailedConsentResource =
//                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();
//
//        doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
//                retrievedDetailedConsentResource.getConsentId(),
//                retrievedDetailedConsentResource.getCurrentStatus()))
//                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
//                        any(ConsentStatusAuditRecord.class));
//
//        consentCoreServiceImpl.updateConsentStatus(retrievedDetailedConsentResource.getConsentId(),
//                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
//    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateConsentStatusDataRetrievalError() throws
            Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

        consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateConsentStatusDataUpdateError() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
        doThrow(ConsentDataUpdationException.class)
                .when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                        anyString());

        consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateConsentStatusDataInsertError() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                anyString());
        doThrow(ConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));

        consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateConsentStatusWithoutConsentId() throws
            Exception {

        consentCoreServiceImpl.updateConsentStatus(null,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateConsentStatusWithoutConsentStatus() throws
            Exception {

        consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData.CONSENT_ID,
                null);
    }



    @Test
    public void testDeactivateAccountMappings() throws
            Exception {

        doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                any(), any());
        Assert.assertTrue(consentCoreServiceImpl
                .deactivateAccountMappings(ConsentMgtServiceTestData.UNMATCHED_MAPPING_IDS));
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testDeactivateAccountMappingsWithEmptyMappingIDList() throws
            Exception {

        consentCoreServiceImpl.deactivateAccountMappings(new ArrayList<>());
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testDeactivateAccountMappingsRollback() throws
            Exception {

        doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateConsentMappingStatus(any(), any(), any());
        consentCoreServiceImpl.deactivateAccountMappings(ConsentMgtServiceTestData.UNMATCHED_MAPPING_IDS);
    }




    @Test
    public void testRevokeConsent() throws
            Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), any());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                anyString());
        doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                retrievedDetailedConsentResource.getConsentId(),
                retrievedDetailedConsentResource.getCurrentStatus()))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));
        doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                any(), anyString());

        boolean isConsentRevoked = consentCoreServiceImpl.revokeConsentWithReason(
                ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, false,
                ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);

        Assert.assertTrue(isConsentRevoked);
    }

    @Test
    public void testRevokeConsentWithReason() throws
            Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), any());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                anyString());
        doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                retrievedDetailedConsentResource.getConsentId(),
                retrievedDetailedConsentResource.getCurrentStatus()))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));
        doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                any(), anyString());

        boolean isConsentRevoked = consentCoreServiceImpl.revokeConsentWithReason(
                ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);

        Assert.assertTrue(isConsentRevoked);
    }

    @Test
    public void testRevokeConsentWithUserId() throws
            Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), any());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                anyString());
        doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                retrievedDetailedConsentResource.getConsentId(),
                retrievedDetailedConsentResource.getCurrentStatus()))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));
        doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                any(), anyString());

        boolean isConsentRevoked = consentCoreServiceImpl.revokeConsentWithReason(
                ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);

        Assert.assertTrue(isConsentRevoked);
    }

    @Test
    public void testRevokeConsentAndTokens() throws
            Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), any());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                anyString());

        doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                retrievedDetailedConsentResource.getConsentId(),
                retrievedDetailedConsentResource.getCurrentStatus()))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));
        doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                any(), anyString());

        boolean isConsentRevoked = consentCoreServiceImpl.revokeConsent(
                ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                true);

        Assert.assertTrue(isConsentRevoked);
    }

    @Test
    public void testRevokeConsentAndTokensTokenRevokeError() throws
            Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), any());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                anyString());

        doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                retrievedDetailedConsentResource.getConsentId(),
                retrievedDetailedConsentResource.getCurrentStatus()))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));
        doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                any(), anyString());
        try {
            boolean isConsentRevoked = consentCoreServiceImpl.revokeConsentWithReason(
                    ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, true,
                    ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);
            Assert.assertTrue(isConsentRevoked);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ConsentMgtException);
        }

    }

    @Test
    public void testRevokeConsentWithoutConsentAttributes() throws
            Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();
        retrievedDetailedConsentResource.setConsentAttributes(null);

        doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), any());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                anyString());
        doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                retrievedDetailedConsentResource.getConsentId(),
                retrievedDetailedConsentResource.getCurrentStatus()))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));
        doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                any(), anyString());

        boolean isConsentRevoked = consentCoreServiceImpl.revokeConsent(
                ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                false);

        Assert.assertTrue(isConsentRevoked);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testRevokeConsentWithoutConsentID() throws
            Exception {

        consentCoreServiceImpl.revokeConsentWithReason(null,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                false, ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testRevokeConsentWithoutNewConsentStatus() throws
            Exception {

        consentCoreServiceImpl.revokeConsent(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                null, ConsentMgtServiceTestData.SAMPLE_USER_ID, false);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testRevokeConsentDataRetrievalError() throws
            Exception {

        doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), any());

        consentCoreServiceImpl.revokeConsentWithReason(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                false, ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testRevokeConsentDataInsertionError() throws
            Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), any());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                anyString());
        doThrow(ConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));

        consentCoreServiceImpl.revokeConsent(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                false);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testRevokeConsentDataUpdateError() throws
            Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), any());
        doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateConsentStatus(any(), anyString(), anyString());

        consentCoreServiceImpl.revokeConsentWithReason(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                false, ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);
    }


    @Test
    public void testRevokeConsentWithoutReason() throws
            Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), any());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                anyString());
        doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                retrievedDetailedConsentResource.getConsentId(),
                retrievedDetailedConsentResource.getCurrentStatus()))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));
        doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                any(), anyString());

        boolean isConsentRevoked = consentCoreServiceImpl.revokeConsent(
                ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);

        Assert.assertTrue(isConsentRevoked);
    }

    @Test(priority = 1)
    public void testRevokeConsentWithUserIDWithoutReason() throws
            Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), any());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                anyString());
        doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                retrievedDetailedConsentResource.getConsentId(),
                retrievedDetailedConsentResource.getCurrentStatus()))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));
        doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                any(), anyString());

        boolean isConsentRevoked = consentCoreServiceImpl.revokeConsent(
                ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID);

        Assert.assertTrue(isConsentRevoked);
    }

    @Test
    public void testRevokeExistingApplicableConsents() throws
            Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
        detailedConsentResources.add(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());

        doReturn(detailedConsentResources).when(mockedConsentCoreDAO)
                .searchConsents(any(), any(), any(), any(), any(), any(),
                        any(), anyLong(), anyLong(), anyInt(), anyInt());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                anyString());
        doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS)).when(mockedConsentCoreDAO)
                .storeConsentStatusAuditRecord(any(), any(ConsentStatusAuditRecord.class));
        doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                any(), any());

        Assert.assertTrue(consentCoreServiceImpl.revokeExistingApplicableConsents(sampleID,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                false));
    }

    @Test
    public void testRevokeExistingApplicableConsentsWithTokens() throws
            Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
        detailedConsentResources.add(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());

        doReturn(detailedConsentResources).when(mockedConsentCoreDAO)
                .searchConsents(any(), any(), any(), any(), any(), any(),
                        any(), anyLong(), anyLong(), anyInt(), anyInt());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                anyString());
        doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS)).when(mockedConsentCoreDAO)
                .storeConsentStatusAuditRecord(any(), any(ConsentStatusAuditRecord.class));
        doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                any(), any());

        Assert.assertTrue(consentCoreServiceImpl.revokeExistingApplicableConsents(sampleID,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                true));
    }

    @Test
    public void testRevokeExistingApplicableConsentsWithConsentsWithNoAttributes() throws
            Exception {

        DetailedConsentResource detailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();
        detailedConsentResource.setConsentAttributes(null);

        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
        detailedConsentResources.add(detailedConsentResource);

        doReturn(detailedConsentResources).when(mockedConsentCoreDAO)
                .searchConsents(any(), any(), any(), any(), any(), any(),
                        any(), anyLong(), anyLong(), anyInt(), anyInt());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                anyString());
        doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS)).when(mockedConsentCoreDAO)
                .storeConsentStatusAuditRecord(any(), any(ConsentStatusAuditRecord.class));
        doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                any(), any());

        consentCoreServiceImpl.revokeExistingApplicableConsents(sampleID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testRevokeExistingApplicableConsentsRetrieveError() throws
            Exception {

        doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .searchConsents(any(), any(), any(), any(), any(), any(),
                        any(), any(), any(), any(), any());

        consentCoreServiceImpl.revokeExistingApplicableConsents(sampleID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testRevokeExistingApplicableConsentsUpdateError() throws
            Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
        detailedConsentResources.add(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());

        doReturn(detailedConsentResources).when(mockedConsentCoreDAO)
                .searchConsents(any(), any(), any(), any(), any(), any(),
                        any(), any(), any(), any(), any());
        doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateConsentStatus(any(), anyString(), anyString());

        consentCoreServiceImpl.revokeExistingApplicableConsents(sampleID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
    }

//    @Test(expectedExceptions = ConsentMgtException.class)
//    public void testRevokeExistingApplicableConsentsInsertionError() throws
//            Exception {
//
//        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
//        detailedConsentResources.add(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());
//
//        doReturn(detailedConsentResources).when(mockedConsentCoreDAO)
//                .searchConsents(any(), any(), any(), any(), any(), any(),
//                        any(), any(), any(), any(), any());
//        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
//                anyString());
//        doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
//                .storeConsentStatusAuditRecord(any(), any());
//
//        consentCoreServiceImpl.revokeExistingApplicableConsents(sampleID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
//                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
//                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
//    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testRevokeExistingApplicableConsentsWithoutClientID() throws
            Exception {

        consentCoreServiceImpl.revokeExistingApplicableConsents(null, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testRevokeExistingApplicableConsentsWithoutRevokedConsentStatus() throws
            Exception {

        consentCoreServiceImpl.revokeExistingApplicableConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_ID,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, null, false);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testRevokeExistingApplicableConsentsWithoutUserID() throws
            Exception {

        consentCoreServiceImpl.revokeExistingApplicableConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_ID,
                null, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS
                , false);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testRevokeExistingApplicableConsentsWithoutConsentType() throws
            Exception {

        consentCoreServiceImpl.revokeExistingApplicableConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_ID,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, null,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS
                , false);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testRevokeExistingApplicableConsentsWithoutApplicableStatusToRevoke() throws
            Exception {

        consentCoreServiceImpl.revokeExistingApplicableConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_ID,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE,
                null, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS
                , false);
    }

    @Test
    public void testReAuthorizeExistingAuthResources() throws
            Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredConsentResource();
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

        doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                any(), anyString());
        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, consentResource.getCurrentStatus()));
    }

    @Test
    public void testReAuthorizeExistingAuthResourceAccountsAddScenario() throws
            Exception {

        ConsentMappingResource consentMappingResource =
                ConsentMgtServiceTestData.getSampleTestConsentMappingResource(sampleID);
        consentMappingResource.setAccountID("accountID1");
        ArrayList<ConsentMappingResource> mappingResources = new ArrayList<>();
        mappingResources.add(consentMappingResource);

        DetailedConsentResource detailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();
        detailedConsentResource.setConsentMappingResources(mappingResources);

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredConsentResource();
        doReturn(detailedConsentResource)
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

        doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                any(), anyString());
        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, consentResource.getCurrentStatus()));
    }

    @Test
    public void testReAuthorizeExistingAuthResourceNoAccountsRemoveOrAddScenario() throws
            Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredConsentResource();
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

        doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                any(), anyString());
        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP2,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, consentResource.getCurrentStatus()));
    }

    @Test
    public void testReAuthorizeExistingAuthResourceAccountsRemoveScenario() throws
            Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredConsentResource();
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourceWithMultipleAccountIDs())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

        doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                any(), anyString());
        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP3,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, consentResource.getCurrentStatus()));
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testReAuthorizeExistingAuthResourcesWithoutConsentID() throws
            Exception {

        Assert.assertTrue(consentCoreServiceImpl.reAuthorizeExistingAuthResource(null,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testReAuthorizeExistingAuthResourcesWithoutAuthID() throws
            Exception {

        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        null, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testReAuthorizeExistingAuthResourcesWithoutUserID() throws
            Exception {

        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        sampleID, null, ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testReAuthorizeExistingAuthResourcesWithoutCurrentConsentStatus() throws
            Exception {

        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        sampleID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                        null, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testReAuthorizeExistingAuthResourcesWithoutNewConsentStatus() throws
            Exception {

        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, null));
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testReAuthorizeExistingAuthResourcesWithoutAccountsAndPermissionsMap() throws
            Exception {

        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        new HashMap<>(), ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testReAuthorizeExistingAuthResourcesDataRetrieveError() throws
            Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
        consentCoreServiceImpl.reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }
//
//    @Test(expectedExceptions = ConsentMgtException.class)
//    public void testReAuthorizeExistingAuthResourcesDataInsertError() throws
//            Exception {
//
//        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
//                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
//        doReturn(ConsentMgtServiceTestData
//                .getSampleConsentMappingResourcesList(ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST))
//                .when(mockedConsentCoreDAO).getConsentMappingResources(any(), anyString());
//        doThrow(ConsentDataInsertionException.class)
//                .when(mockedConsentCoreDAO).storeAuthorizationResource(any(),
//                        any());
//        consentCoreServiceImpl.reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
//                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
//                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
//                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
//    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testReAuthorizeExistingAuthResourcesDataUpdateError() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
        doReturn(ConsentMgtServiceTestData
                .getSampleConsentMappingResourcesList(ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST))
                .when(mockedConsentCoreDAO).getConsentMappingResources(any(), anyString());
        doThrow(ConsentDataUpdationException.class)
                .when(mockedConsentCoreDAO).updateConsentStatus(any(), any(),
                        anyString());
        consentCoreServiceImpl.reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test
    public void testReAuthorizeConsentWithNewAuthResource() throws
            Exception {

        AuthorizationResource authorizationResource = ConsentMgtServiceTestData
                .getSampleTestAuthorizationResource(sampleID, null);
        ArrayList<String> consentIDs = new ArrayList<>();
        consentIDs.add(sampleID);

        doReturn(ConsentMgtServiceTestData.getSampleAuthorizationResourcesList(consentIDs))
                .when(mockedConsentCoreDAO).searchConsentAuthorizations(any(), anyString(),
                        anyString());
        doNothing().when(mockedConsentCoreDAO).updateAuthorizationStatus(any(), anyString(),
                anyString());
        doReturn(authorizationResource).when(mockedConsentCoreDAO).storeAuthorizationResource(any(),
                any(AuthorizationResource.class));
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
        doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                any(), anyString());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                anyString());
        doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentStatusAuditRecord(sampleID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testReAuthorizeConsentWithNewAuthResourceDataRetrieveError() throws
            Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).searchConsentAuthorizations(any(), any(), any());

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testReAuthorizeConsentWithNewAuthResourceDataUpdateError() throws
            Exception {

        ArrayList<String> consentIDs = new ArrayList<>();
        consentIDs.add(sampleID);
        doReturn(ConsentMgtServiceTestData.getSampleAuthorizationResourcesList(consentIDs))
                .when(mockedConsentCoreDAO).searchConsentAuthorizations(any(), any(), any());
        doThrow(ConsentDataUpdationException.class)
                .when(mockedConsentCoreDAO).updateAuthorizationStatus(any(), anyString(),
                        anyString());

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testReAuthorizeConsentWithNewAuthResourceDataInsertError() throws
            Exception {

        AuthorizationResource authorizationResource = ConsentMgtServiceTestData
                .getSampleTestAuthorizationResource(sampleID, null);

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourcesList())
                .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(), any(),
                        any(), any(), anyLong(), anyLong(), anyInt(),
                        anyInt());
        doNothing().when(mockedConsentCoreDAO).updateAuthorizationStatus(any(), anyString(),
                anyString());
        doReturn(authorizationResource).when(mockedConsentCoreDAO).storeAuthorizationResource(any(),
                any(AuthorizationResource.class));
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
        doThrow(ConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeAuthorizationResource(any(),
                        any());

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutConsentID() throws
            Exception {

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(null, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutUserID() throws
            Exception {

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, null,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutAccountsMap() throws
            Exception {

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                null, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutCurrentConsentStatus() throws
            Exception {

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP, null,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutNewConsentStatus() throws
            Exception {

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                null, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutNewExistingAuthStatus() throws
            Exception {

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, null,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutNewAuthStatus() throws
            Exception {

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                null, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutNewAuthType() throws
            Exception {

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS, null);
    }

    @Test
    public void storeConsentAttributes() throws
            Exception {

        consentCoreServiceImpl.storeConsentAttributes(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void storeConsentAttributesWithoutParameters() throws
            Exception {

        consentCoreServiceImpl.storeConsentAttributes(null, null);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void storeConsentAttributesWithoutConsentId() throws
            Exception {

        consentCoreServiceImpl.storeConsentAttributes(null,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void storeConsentAttributesWithoutAttributeMap() throws
            Exception {

        consentCoreServiceImpl.storeConsentAttributes(ConsentMgtServiceTestData.CONSENT_ID,
                null);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void storeConsentAttributesEmptyAttributeMap() throws
            Exception {

        consentCoreServiceImpl.storeConsentAttributes(ConsentMgtServiceTestData.CONSENT_ID,
                new HashMap<>());
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
                .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
        doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentAttributesObject(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).getConsentAttributes(any(), anyString(),
                        any());
        ConsentAttributes consentAttributes =
                consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
        Assert.assertNotNull(consentAttributes);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetConsentAttributesWithoutConsentID() throws
            Exception {

        consentCoreServiceImpl.getConsentAttributes(null,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetConsentAttributesWithEmptyAttributeKeys() throws
            Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentAttributes(any(), anyString());
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
                .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentAttributes(any(), anyString(), any());
        consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    @Test
    public void testGetConsentAttributes() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleTestConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
        doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentAttributesObject(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).getConsentAttributes(any(), anyString());
        ConsentAttributes consentAttributes =
                consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
        Assert.assertNotNull(consentAttributes);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetConsentAttributesWithoutAttributeKeys() throws
            Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentAttributes(any(), anyString());
        consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetConsentAttributesWithoutAttributesWithoutConsentID() throws
            Exception {

        consentCoreServiceImpl.getConsentAttributes(null);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetConsentAttributesConsentResourceRetrieveError() throws
            Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
        doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentAttributesObject(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).getConsentAttributes(any(), anyString());
        ConsentAttributes consentAttributes =
                consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
        Assert.assertNotNull(consentAttributes);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetConsentAttributesWithDataRetrieveError() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.getSampleTestConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
        doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getConsentAttributes(any(), anyString());
        ConsentAttributes consentAttributes =
                consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
        Assert.assertNotNull(consentAttributes);
    }

    @Test
    public void testGetConsentAttributesByName() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP)
                .when(mockedConsentCoreDAO).getConsentAttributesByName(any(), anyString());
        Map<String, String> retrievedAttributesMap =
                consentCoreServiceImpl.getConsentAttributesByName("x-request-id");
        Assert.assertTrue(retrievedAttributesMap.containsKey("x-request-id"));
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetConsentAttributesByNameWithoutAttributeName() throws
            Exception {

        consentCoreServiceImpl.getConsentAttributesByName(null);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetConsentAttributesByNameDataRetrieveError() throws
            Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentAttributesByName(any(), anyString());
        consentCoreServiceImpl.getConsentAttributesByName("x-request-id");
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetConsentIdByConsentAttributeNameAndValueWithoutAttributeName() throws
            Exception {

        consentCoreServiceImpl.getConsentIdByConsentAttributeNameAndValue(null,
                "domestic-payments");
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetConsentIdByConsentAttributeNameAndValueWithoutAttributeValues() throws
            Exception {

        consentCoreServiceImpl.getConsentIdByConsentAttributeNameAndValue("payment-type",
                null);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetConsentIdByConsentAttributeNameAndValueDataRetrieveError() throws
            Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentIdByConsentAttributeNameAndValue(any(),
                        anyString(), anyString());
        consentCoreServiceImpl.getConsentIdByConsentAttributeNameAndValue("payment-type",
                "domestic-payments");
    }

    @Test
    public void testGetConsentIdByConsentAttributeNameAndValue() throws
            Exception {

        doReturn(ConsentMgtServiceTestData.SAMPLE_CONSENT_IS_ARRAY)
                .when(mockedConsentCoreDAO).getConsentIdByConsentAttributeNameAndValue(any(),
                        anyString(), anyString());
        ArrayList<String> consentIdList = consentCoreServiceImpl.getConsentIdByConsentAttributeNameAndValue(
                "payment-type", "domestic-payments");
        Assert.assertFalse(consentIdList.isEmpty());
    }

    @Test
    public void testUpdateConsentAttributes() throws
            Exception {

        doNothing().when(mockedConsentCoreDAO).updateConsentAttributes(any(),
                anyString(), anyMap());
        doReturn(ConsentMgtServiceTestData.getSampleTestConsentAttributesObject(
                ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).getConsentAttributes(any(), anyString());
        consentCoreServiceImpl.updateConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateConsentAttributesWithoutConsentId() throws
            Exception {

        consentCoreServiceImpl.updateConsentAttributes(null,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateConsentAttributesWithoutAttributes() throws
            Exception {

        consentCoreServiceImpl.updateConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID, null);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateConsentAttributesWithEmptyAttributes() throws
            Exception {

        consentCoreServiceImpl.updateConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                new HashMap<>());
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateConsentAttributesWithDataUpdateError() throws
            Exception {

        doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateConsentAttributes(any(), anyString(), anyMap());
        doReturn(ConsentMgtServiceTestData.getSampleTestConsentAttributesObject(
                ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).getConsentAttributes(any(), anyString());
        consentCoreServiceImpl.updateConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testUpdateConsentAttributesWithDataRetrieveError() throws
            Exception {

        doNothing().when(mockedConsentCoreDAO).updateConsentAttributes(any(),
                anyString(), anyMap());
        doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getConsentAttributes(any(), anyString());
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
    public void testDeleteConsentAttributesWithoutConsentID() throws
            Exception {

        doReturn(true).when(mockedConsentCoreDAO).deleteConsentAttributes(any(),
                anyString(), any());
        consentCoreServiceImpl.deleteConsentAttributes(null,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testDeleteConsentAttributesWithoutAttributeKeysList() throws
            Exception {

        doReturn(true).when(mockedConsentCoreDAO).deleteConsentAttributes(any(),
                anyString(), any());
        consentCoreServiceImpl.deleteConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                null);
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
    public void testAmendDetailedConsentData() throws
            Exception {

        setInitialDataForAmendDetailedConsentSuccessFlow();
        DetailedConsentResource detailedConsentResource =
                consentCoreServiceImpl.amendDetailedConsent(sampleID,
                        ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                        ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                        ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        new HashMap<>());

        Assert.assertNotNull(detailedConsentResource);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testAmendDetailedConsentDataWithoutConsentID() throws
            Exception {

        consentCoreServiceImpl.amendDetailedConsent(null, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                new HashMap<>());
    }

    @Test
    public void testAmendDetailedConsentDataWithoutExpiryTimeOnly() throws
            Exception {

        setInitialDataForAmendDetailedConsentSuccessFlow();
        DetailedConsentResource detailedConsentResource =
                consentCoreServiceImpl.amendDetailedConsent(sampleID, null,
                        ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                        ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        new HashMap<>());

        Assert.assertNotNull(detailedConsentResource);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testAmendDetailedConsentDataWithoutReceiptAndExpiryTime() throws
            Exception {

        consentCoreServiceImpl.amendDetailedConsent(sampleID, null, null,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                new HashMap<>());
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testAmendDetailedConsentDataWithoutUserId() throws
            Exception {

        consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP, null,
                new HashMap<>());
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testAmendDetailedConsentDataWithoutAuthId() throws
            Exception {

        consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD, null,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                new HashMap<>());
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testAmendDetailedConsentDataWithoutNewConsentStatus() throws
            Exception {

        consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP, null,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                new HashMap<>());
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testAmendDetailedConsentDataWithoutNewConsentAttributes() throws
            Exception {

        consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, null,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                new HashMap<>());
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testAmendDetailedConsentDataWithoutAccountIdMapWithPermissions() throws
            Exception {

        consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                null,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                new HashMap<>());
    }

    private void setInitialDataForAmendDetailedConsentSuccessFlow() throws
            Exception {

        doNothing().when(mockedConsentCoreDAO).updateConsentReceipt(any(),
                anyString(), anyString());
        doNothing().when(mockedConsentCoreDAO).updateConsentExpiryTime(any(),
                anyString(), anyLong());
        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
        doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

        doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                any(), anyString());

        doReturn(true).when(mockedConsentCoreDAO).deleteConsentAttributes(any(),
                anyString(), any());
        doReturn(true).when(mockedConsentCoreDAO).storeConsentAttributes(any(),
                any(ConsentAttributes.class));
    }

    private void setInitialDataForAmendDetailedConsentWithBulkAuthResourceSuccessFlow() throws
            Exception {

        // return authResource
        doReturn(ConsentMgtServiceTestData.getSampleTestAuthorizationResource(sampleID, null))
                .when(mockedConsentCoreDAO).getAuthorizationResource(any(),
                        any(), any());
        doNothing().when(mockedConsentCoreDAO).updateConsentReceipt(any(),
                anyString(), anyString());
        doNothing().when(mockedConsentCoreDAO).updateConsentExpiryTime(any(),
                anyString(), anyLong());
        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
        doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

        doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                any(), anyString());

        doReturn(true).when(mockedConsentCoreDAO).deleteConsentAttributes(any(),
                anyString(), any());
        doReturn(true).when(mockedConsentCoreDAO).storeConsentAttributes(any(),
                any(ConsentAttributes.class));

        //storeAuthorizationResource
        doReturn(ConsentMgtServiceTestData.getSampleTestAuthorizationResource(sampleID, null))
                .when(mockedConsentCoreDAO).storeAuthorizationResource(any(),
                        any(AuthorizationResource.class));
        // mocked DetailedConsentResource
        DetailedConsentResource detailedConsentResource = Mockito.mock(DetailedConsentResource.class);
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
        // getOrgId
        doReturn(ConsentMgtServiceTestData.ORG_INFO).when(detailedConsentResource).getOrgID();

    }


    // uint tests for amendDetailedConsentWithBulkAuthResource
    @Test
    public void testAmendDetailedConsentWithBulkAuthResource() throws
            Exception {

        setInitialDataForAmendDetailedConsentWithBulkAuthResourceSuccessFlow();
        DetailedConsentResource detailedConsentResource =
                consentCoreServiceImpl.amendDetailedConsentWithBulkAuthResource(
                        ConsentMgtServiceTestData.ORG_INFO,
                        sampleID,
                        ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                        ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                        ConsentMgtServiceTestData.getSampleTestAuthorizationResourceListWithAuthIdAndConsentMappingId(),
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.getSampleTestAuthorizationResourcesList(sampleID, null)
                                                                               );

        Assert.assertNotNull(detailedConsentResource);
    }

    @Test(expectedExceptions = ConsentMgtException.class)
    public void testAmendDetailedConsentWithBulkAuthResourceWithoutConsentID() throws
            Exception {

        consentCoreServiceImpl.amendDetailedConsentWithBulkAuthResource(
                ConsentMgtServiceTestData.ORG_INFO,
                null,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.getSampleTestAuthorizationResourceListWithAuthIdAndConsentMappingId(),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.getSampleTestAuthorizationResourcesList(sampleID, null)
                                                                       );
    }

    @Test()
    public void testAmendDetailedConsentWithBulkAuthResourceWithoutAuthorizationResource() throws
            Exception {

        setInitialDataForAmendDetailedConsentWithBulkAuthResourceSuccessFlow();
        consentCoreServiceImpl.amendDetailedConsentWithBulkAuthResource(
                ConsentMgtServiceTestData.ORG_INFO,
                sampleID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                new ArrayList<>(),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.getSampleTestAuthorizationResourcesList(sampleID, null)
                                                                       );

    }


    @Test
    public void testAmendDetailedConsentWithBulkAuthResourceWithoutUserId() throws
            Exception {
        setInitialDataForAmendDetailedConsentWithBulkAuthResourceSuccessFlow();

        consentCoreServiceImpl.amendDetailedConsentWithBulkAuthResource(
                ConsentMgtServiceTestData.ORG_INFO,
                sampleID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.getSampleTestAuthorizationResourceListWithAuthIdAndConsentMappingId(),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                null,
                ConsentMgtServiceTestData.getSampleTestAuthorizationResourcesList(sampleID, null)
                                                                       );

    }

    // unit tests for getConsentAmendmentHistoryData
    @Test
    public void testGetConsentAmendmentHistoryData() throws
            Exception {


        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

        //retrieveConsentAmendmentHistory
        doReturn(ConsentMgtServiceTestData.getSampleConsentHistoryDataMap()).when(mockedConsentCoreDAO).
                retrieveConsentAmendmentHistory(any(), any(), any());

        Map<String, ConsentHistoryResource> consentHistoryResource =
                consentCoreServiceImpl.getConsentAmendmentHistoryData(
                        ConsentMgtServiceTestData.getSampleConsentStatusAuditRecordIds(), sampleID);
        Assert.assertNotNull(consentHistoryResource);
    }

    // empty consentStatusAuditRecordIds
    @Test
    public void testGetConsentAmendmentHistoryDataWithEmptyConsentStatusAuditRecordIds() throws
            Exception {

        Map<String, ConsentHistoryResource> consentHistoryResource =
                consentCoreServiceImpl.getConsentAmendmentHistoryData(
                        new ArrayList<>(), sampleID);
        Assert.assertNotNull(consentHistoryResource);
    }

    // empty consetId
    @Test(expectedExceptions = ConsentMgtException.class)
    public void testGetConsentAmendmentHistoryDataWithEmptyConsentId() throws
            Exception {

        consentCoreServiceImpl.getConsentAmendmentHistoryData(
                ConsentMgtServiceTestData.getSampleConsentStatusAuditRecordIds(), null);
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


}


