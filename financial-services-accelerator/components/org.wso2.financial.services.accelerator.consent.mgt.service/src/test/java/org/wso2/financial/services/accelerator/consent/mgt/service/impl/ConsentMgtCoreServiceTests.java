/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth2.dto.OAuthRevocationResponseDTO;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.util.DatabaseUtils;
import org.wso2.financial.services.accelerator.consent.mgt.dao.ConsentCoreDAO;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentDataDeletionException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentDataInsertionException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentDataRetrievalException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentDataUpdationException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentAttributes;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentFile;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentHistoryResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentStatusAuditRecord;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.persistence.ConsentStoreInitializer;
import org.wso2.financial.services.accelerator.consent.mgt.service.constants.ConsentCoreServiceConstants;
import org.wso2.financial.services.accelerator.consent.mgt.service.internal.ConsentMgtDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.service.util.ConsentMgtServiceTestData;
import org.wso2.financial.services.accelerator.consent.mgt.service.util.TokenRevocationUtil;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for FS consent management core service.
 */
public class ConsentMgtCoreServiceTests {

    private ConsentCoreServiceImpl consentCoreServiceImpl;
    @Mock
    private ConsentCoreDAO mockedConsentCoreDAO;
    private String sampleID;
    @Mock
    private ConsentMgtDataHolder consentManagementDataHolderMock;
    @Mock
    Connection connectionMock;
    @Mock
    ConsentResource consentResourceMock;
    MockedStatic<DatabaseUtils> databaseUtilMockedStatic;
    MockedStatic<ConsentStoreInitializer> consentStoreInitializerMockedStatic;
    MockedStatic<ConsentMgtDataHolder> consentManagementDataHolderMockedStatic;
    MockedStatic<TokenRevocationUtil> tokenRevocationUtilMockedStatic;

    @BeforeClass
    public void initTest() {

        connectionMock = Mockito.mock(Connection.class);
        consentCoreServiceImpl = new ConsentCoreServiceImpl();
        mockedConsentCoreDAO = Mockito.mock(ConsentCoreDAO.class);
        consentManagementDataHolderMock = Mockito.mock(ConsentMgtDataHolder.class);
        consentResourceMock = Mockito.mock(ConsentResource.class);
    }

    @BeforeMethod
    public void mock() {

        sampleID = UUID.randomUUID().toString();
    }

    @BeforeClass
    private void mockStaticClasses() {

        databaseUtilMockedStatic = mockStatic(DatabaseUtils.class);
        databaseUtilMockedStatic.when(DatabaseUtils::getDBConnection).thenReturn(connectionMock);

        consentStoreInitializerMockedStatic = mockStatic(ConsentStoreInitializer.class);
        consentStoreInitializerMockedStatic.when(ConsentStoreInitializer::getInitializedConsentCoreDAOImpl)
                .thenReturn(mockedConsentCoreDAO);

        consentManagementDataHolderMockedStatic = mockStatic(ConsentMgtDataHolder.class);
        consentManagementDataHolderMockedStatic.when(ConsentMgtDataHolder::getInstance)
                .thenReturn(consentManagementDataHolderMock);

        tokenRevocationUtilMockedStatic = mockStatic(TokenRevocationUtil.class);
        tokenRevocationUtilMockedStatic.when(() -> TokenRevocationUtil.getAuthenticatedUser(anyString()))
                .thenReturn(new AuthenticatedUser());
        tokenRevocationUtilMockedStatic.when(() -> TokenRevocationUtil.getAccessTokenDOSet(any(), any()))
                .thenReturn(new HashSet<AccessTokenDO>());
        tokenRevocationUtilMockedStatic.when(() -> TokenRevocationUtil.revokeTokenByClient(any(), any()))
                .thenReturn(new OAuthRevocationResponseDTO());
    }

    @AfterClass
    public void tearDown() {
        // Closing the mockStatic after each test
        databaseUtilMockedStatic.close();
        consentStoreInitializerMockedStatic.close();
        consentManagementDataHolderMockedStatic.close();
        tokenRevocationUtilMockedStatic.close();
    }

    @Test(priority = 2)
    public void testCreateAuthorizableConsent() throws Exception {

        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(any(), any());
        doReturn(ConsentMgtServiceTestData.getSampleTestAuthorizationResource(null, null))
                .when(mockedConsentCoreDAO).storeAuthorizationResource(any(), any());

        DetailedConsentResource detailedConsentResource =
                consentCoreServiceImpl.createAuthorizableConsent(ConsentMgtServiceTestData
                                .getSampleTestConsentResource(), ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_AUTH_TYPE, true);

        Assert.assertNotNull(detailedConsentResource);
        Assert.assertNotNull(detailedConsentResource.getConsentID());
        Assert.assertNotNull(detailedConsentResource.getClientID());
        Assert.assertNotNull(detailedConsentResource.getReceipt());
        Assert.assertNotNull(detailedConsentResource.getConsentType());
        Assert.assertNotNull(detailedConsentResource.getCurrentStatus());
    }

    @Test (priority = 2)
    public void testCreateAuthorizableConsentWithAttributes() throws Exception {

        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(any(), any());
        doReturn(ConsentMgtServiceTestData.getSampleTestAuthorizationResource(null, null))
                .when(mockedConsentCoreDAO).storeAuthorizationResource(any(), any());
        doReturn(true).when(mockedConsentCoreDAO).storeConsentAttributes(any(),
                any());

        DetailedConsentResource detailedConsentResource =
                consentCoreServiceImpl.createAuthorizableConsent(ConsentMgtServiceTestData
                                .getSampleStoredTestConsentResourceWithAttributes(),
                        ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_AUTH_TYPE, true);

        Assert.assertNotNull(detailedConsentResource);
        Assert.assertNotNull(detailedConsentResource.getConsentID());
        Assert.assertNotNull(detailedConsentResource.getClientID());
        Assert.assertNotNull(detailedConsentResource.getReceipt());
        Assert.assertNotNull(detailedConsentResource.getConsentType());
        Assert.assertNotNull(detailedConsentResource.getCurrentStatus());
        Assert.assertNotNull(detailedConsentResource.getConsentAttributes());
    }

    @Test (priority = 2)
    public void testCreateAuthorizableConsentWithoutUserID() throws Exception {

        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(any(), any());
        doReturn(ConsentMgtServiceTestData.getSampleTestAuthorizationResource(null, null))
                .when(mockedConsentCoreDAO).storeAuthorizationResource(any(), any());

        DetailedConsentResource detailedConsentResource =
                consentCoreServiceImpl.createAuthorizableConsent(ConsentMgtServiceTestData
                                .getSampleStoredTestConsentResourceWithAttributes(), null,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_AUTH_TYPE, true);

        Assert.assertNotNull(detailedConsentResource);
        Assert.assertNotNull(detailedConsentResource.getConsentID());
        Assert.assertNotNull(detailedConsentResource.getClientID());
        Assert.assertNotNull(detailedConsentResource.getReceipt());
        Assert.assertNotNull(detailedConsentResource.getConsentType());
        Assert.assertNotNull(detailedConsentResource.getCurrentStatus());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateAuthorizableConsentWithoutClientID() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleTestConsentResource();
        consentResource.setClientID(null);

        consentCoreServiceImpl.createAuthorizableConsent(consentResource, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_AUTH_TYPE,
                false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateAuthorizableConsentWithoutReceipt() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleTestConsentResource();
        consentResource.setReceipt(null);

        consentCoreServiceImpl.createAuthorizableConsent(consentResource, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_AUTH_TYPE,
                false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateAuthorizableConsentWithoutConsentType() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleTestConsentResource();
        consentResource.setConsentType(null);

        consentCoreServiceImpl.createAuthorizableConsent(consentResource, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_AUTH_TYPE,
                false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateAuthorizableConsentWithoutCurrentStatus() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleTestConsentResource();
        consentResource.setCurrentStatus(null);

        consentCoreServiceImpl.createAuthorizableConsent(consentResource, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_AUTH_TYPE,
                false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateAuthorizableConsentWithImplicitAndNoAuthStatus() throws Exception {

        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(any(), any());
        doReturn(ConsentMgtServiceTestData.getSampleTestAuthorizationResource(null, null))
                .when(mockedConsentCoreDAO).storeAuthorizationResource(any(), any());

        consentCoreServiceImpl.createAuthorizableConsent(ConsentMgtServiceTestData
                        .getSampleStoredTestConsentResourceWithAttributes(), null, null,
                ConsentMgtServiceTestData.SAMPLE_AUTH_TYPE, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateAuthorizableConsentWithImplicitAndNoAuthType() throws Exception {

        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(any(), any());
        doReturn(ConsentMgtServiceTestData.getSampleTestAuthorizationResource(null, null))
                .when(mockedConsentCoreDAO).storeAuthorizationResource(any(), any());

        consentCoreServiceImpl.createAuthorizableConsent(ConsentMgtServiceTestData
                        .getSampleStoredTestConsentResourceWithAttributes(), null,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS, null, true);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testCreateAuthorizableConsentRollback() throws Exception {

        doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeConsentResource(any(), any());

        consentCoreServiceImpl.createAuthorizableConsent(ConsentMgtServiceTestData
                        .getSampleTestConsentResource(), ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTH_TYPE, true);
    }

    @Test
    public void testCreateExclusiveConsent() throws Exception {

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourcesList())
                .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(),
                        any(), any(), anyLong(), anyLong(), anyInt(),
                        anyInt());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                anyString());
        doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));
        doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                any(ArrayList.class), anyString());
        doReturn(ConsentMgtServiceTestData.getSampleTestConsentResource())
                .when(mockedConsentCoreDAO).storeConsentResource(any(), any(ConsentResource.class));
        doReturn(true).when(mockedConsentCoreDAO).storeConsentAttributes(any(),
                any(ConsentAttributes.class));
        doReturn(ConsentMgtServiceTestData
                        .getSampleTestAuthorizationResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID, null))
                .when(mockedConsentCoreDAO).storeAuthorizationResource(any(),
                        any(AuthorizationResource.class));

        DetailedConsentResource exclusiveConsent =
                consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData
                                .getSampleStoredConsentResource(),
                        ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
        Assert.assertNotNull(exclusiveConsent);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentDataRetrieveError() throws Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(),
                        any(), any(), any(), any(), any(), any());
        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(any(), any());

        consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentDataUpdateError() throws Exception {

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourcesList())
                .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(),
                        any(), any(), any(), any(), any(), any());
        doThrow(ConsentDataUpdationException.class)
                .when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                        anyString());
        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(any(), any());

        consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentDataInsertError() throws Exception {

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourcesList())
                .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(),
                        any(), any(), anyLong(), anyLong(), anyInt(),
                        anyInt());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                anyString());
        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(any(), any());
        doThrow(ConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));

        consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutClientID() throws Exception {

        ConsentResource sampleConsentResource = ConsentMgtServiceTestData.getSampleStoredConsentResource();
        sampleConsentResource.setClientID(null);

        consentCoreServiceImpl.createExclusiveConsent(sampleConsentResource,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutReceipt() throws Exception {

        ConsentResource sampleConsentResource = ConsentMgtServiceTestData.getSampleStoredConsentResource();
        sampleConsentResource.setReceipt(null);

        consentCoreServiceImpl.createExclusiveConsent(sampleConsentResource,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutConsentType() throws Exception {

        ConsentResource sampleConsentResource = ConsentMgtServiceTestData.getSampleStoredConsentResource();
        sampleConsentResource.setConsentType(null);

        consentCoreServiceImpl.createExclusiveConsent(sampleConsentResource,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutConsentStatus() throws Exception {

        ConsentResource sampleConsentResource = ConsentMgtServiceTestData.getSampleStoredConsentResource();
        sampleConsentResource.setCurrentStatus(null);

        consentCoreServiceImpl.createExclusiveConsent(sampleConsentResource,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutUserID() throws Exception {

        consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource(),
                null, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutAuthStatus() throws Exception {

        consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, null,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutAuthType() throws Exception {

        consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                null, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test
    public void testCreateExclusiveConsentWithImplicitAuthFalse() throws Exception {

        doReturn(ConsentMgtServiceTestData.getSampleTestConsentResource())
                .when(mockedConsentCoreDAO).storeConsentResource(any(), any());
        doReturn(true).when(mockedConsentCoreDAO).storeConsentAttributes(any(), any());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(), anyString());
        doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(), any(), anyString());
        doReturn(ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource())
                .when(mockedConsentCoreDAO).storeAuthorizationResource(any(), any());
        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
        detailedConsentResources.add(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());

        doReturn(detailedConsentResources).when(mockedConsentCoreDAO)
                .searchConsents(any(), any(), any(), any(), any(),
                        any(), anyLong(), anyLong(), anyInt(), anyInt());
        doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));

        DetailedConsentResource consentResource = consentCoreServiceImpl
                .createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource(),
                        ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                        null, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
        Assert.assertNotNull(consentResource);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutApplicableExistingConsentStatus() throws Exception {

        consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, null,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutNewExistingConsentStatus() throws Exception {

        consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                null, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutNewCurrentConsentStatus() throws Exception {

        consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                null, true);
    }

    @Test
    public void testStoreDetailedConsentResource() throws ConsentManagementException, ConsentDataInsertionException {

        doReturn(ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource())
                .when(mockedConsentCoreDAO).storeAuthorizationResource(any(), any());
        doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentMappingResource("sample-auth-id"))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(any(), any());

        DetailedConsentResource result = consentCoreServiceImpl.storeDetailedConsentResource(
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());
        Assert.assertNotNull(result);

    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testStoreDetailedConsentResourceWithMissingConsentID() throws ConsentManagementException {

        DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
        detailedConsentResource.setConsentID(null);
        // Should throw an exception
        consentCoreServiceImpl.storeDetailedConsentResource(detailedConsentResource);
    }

    @Test
    public void testUpdateConsentAndCreateAuthResources() throws ConsentManagementException,
            ConsentDataRetrievalException, ConsentDataInsertionException {

        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
                .getConsentResource(any(), anyString());
        doReturn(ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource())
                .when(mockedConsentCoreDAO).storeAuthorizationResource(any(), any());
        doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentMappingResource("sample-auth-id"))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(any(), any());

        DetailedConsentResource result = consentCoreServiceImpl.updateConsentAndCreateAuthResources(
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());
        Assert.assertNotNull(result);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentAndCreateAuthResourcesWithMissingConsentID() throws ConsentManagementException {

        DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
        detailedConsentResource.setConsentID(null);
        // Should throw an exception
        consentCoreServiceImpl.updateConsentAndCreateAuthResources(detailedConsentResource);
    }


    @Test
    public void testGetConsent() throws Exception {

        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
                .getConsentResource(any(), anyString());

        // Get consent
        ConsentResource retrievedConsentResource = consentCoreServiceImpl.getConsent(ConsentMgtServiceTestData
                .getSampleStoredConsentResource().getConsentID(), false);

        Assert.assertNotNull(retrievedConsentResource);
    }

    @Test
    public void testGetConsentWithAttributes() throws Exception {

        doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResourceWithAttributes())
                .when(mockedConsentCoreDAO).getConsentResourceWithAttributes(any(), anyString());

        // Get consent
        ConsentResource retrievedConsentResource = consentCoreServiceImpl.getConsent(ConsentMgtServiceTestData
                .getSampleStoredConsentResource().getConsentID(), true);

        Assert.assertNotNull(retrievedConsentResource);
        Assert.assertNotNull(retrievedConsentResource.getConsentAttributes());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentRollBackWhenRetrieve() throws Exception {

        doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getConsentResource(any(), anyString());

        // Get consent
        consentCoreServiceImpl.getConsent(ConsentMgtServiceTestData
                        .getSampleStoredConsentResource().getConsentID(),
                false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentWithoutConsentID() throws Exception {

        consentCoreServiceImpl.getConsent(null, false);
    }

    @Test
    public void testGetDetailedConsent() throws Exception {

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

        // Get consent
        DetailedConsentResource retrievedConsentResource = consentCoreServiceImpl
                .getDetailedConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource().getConsentID());

        Assert.assertNotNull(retrievedConsentResource);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testGetDetailedConsentWithoutConsentID() throws Exception {

        // Get consent
        consentCoreServiceImpl.getDetailedConsent(null);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testGetDetailedConsentWithDataRetrievalException() throws Exception {

        doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), anyString());

        // Get consent
        consentCoreServiceImpl.getDetailedConsent(ConsentMgtServiceTestData
                .getSampleStoredConsentResource().getConsentID());
    }

    @Test
    public void testCreateConsentFile() throws Exception {

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

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentFileErrorWhenRetrieval() throws Exception {

        doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getConsentResource(any(), anyString());

        consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                        .getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentFileRollBackWhenCreation() throws Exception {

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

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentFileRollBackWhenUpdating() throws Exception {

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

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentFileWithInvalidStatus()
            throws Exception {

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

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentFileWithoutFileContent() throws Exception {

        consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                        .getSampleConsentFileObject(null), ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentFileWithoutConsentID() throws Exception {

        ConsentFile sampleConsentFile =
                ConsentMgtServiceTestData.getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE);

        sampleConsentFile.setConsentID(null);
        consentCoreServiceImpl.createConsentFile(sampleConsentFile, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentFileWithoutNewConsentStatus()
            throws Exception {

        consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                        .getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE),
                null, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentFileWithoutApplicableStatusForFileUpload()
            throws Exception {

        consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                        .getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                null);
    }

    @Test
    public void testGetConsentFileConsentData() throws Exception {

        doReturn(new ConsentFile()).when(mockedConsentCoreDAO)
                .getConsentFile(any(), anyString());
        ConsentFile consentFile = consentCoreServiceImpl
                .getConsentFile("3d22259e-942c-46b8-8f75-a608c677a6e6");
        Assert.assertNotNull(consentFile);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testGetConsentFileWithoutConsentId() throws Exception {

        consentCoreServiceImpl.getConsentFile(null);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testGetConsentFileWithDataRetrievalError() throws Exception {

        doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getConsentFile(any(), anyString());
        ConsentFile consentFile = consentCoreServiceImpl
                .getConsentFile("3d22259e-942c-46b8-8f75-a608c677a6e6");
        Assert.assertNotNull(consentFile);
    }

    @Test
    public void testCreateConsentAuthorization() throws Exception {

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

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentAuthorizationRollbackWhenCreation() throws Exception {

        AuthorizationResource sampleAuthorizationResource =
                ConsentMgtServiceTestData.getSampleTestAuthorizationResource(sampleID, null);

        doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeAuthorizationResource(any(), any());

        // Get consent
        consentCoreServiceImpl.createConsentAuthorization(sampleAuthorizationResource);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentAuthorizationWithoutConsentID() throws Exception {

        AuthorizationResource sampleAuthorizationResource =
                ConsentMgtServiceTestData.getSampleTestAuthorizationResource(null, null);
        sampleAuthorizationResource.setConsentID(null);

        //Create a consent authorization resource
        consentCoreServiceImpl.createConsentAuthorization(sampleAuthorizationResource);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentAuthorizationWithoutAuthorizationStatus() throws Exception {

        AuthorizationResource sampleAuthorizationResource =
                ConsentMgtServiceTestData.getSampleTestAuthorizationResource(sampleID, null);

        // Explicitly setting authorization status to null
        sampleAuthorizationResource.setAuthorizationStatus(null);

        //Create a consent authorization resource
        consentCoreServiceImpl.createConsentAuthorization(sampleAuthorizationResource);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentAuthorizationWithoutAuthorizationType() throws Exception {

        AuthorizationResource sampleAuthorizationResource =
                ConsentMgtServiceTestData.getSampleTestAuthorizationResource(sampleID, null);
        sampleAuthorizationResource.setAuthorizationType(null);

        //Create a consent authorization resource
        consentCoreServiceImpl.createConsentAuthorization(sampleAuthorizationResource);
    }

    @Test
    public void testGetAuthorizationResource() throws Exception {

        doReturn(ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource())
                .when(mockedConsentCoreDAO).getAuthorizationResource(any(), anyString());
        AuthorizationResource authorizationResource =
                consentCoreServiceImpl.getAuthorizationResource(ConsentMgtServiceTestData
                        .getSampleStoredTestAuthorizationResource().getAuthorizationID());
        Assert.assertNotNull(authorizationResource);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetAuthorizationResourceWithoutAuthID() throws Exception {

        consentCoreServiceImpl.getAuthorizationResource(null);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetAuthorizationResourceDataRetrieveError() throws Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getAuthorizationResource(any(), anyString());
        consentCoreServiceImpl.getAuthorizationResource(ConsentMgtServiceTestData
                .getSampleStoredTestAuthorizationResource().getAuthorizationID());
    }

    @Test
    public void testSearchAuthorizationsWithConsentID() throws Exception {

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
    public void testSearchAuthorizationsWithUserID() throws Exception {

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
    public void testSearchAuthorizations() throws Exception {

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

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testSearchAuthorizationsDataRetrieveError() throws Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).searchConsentAuthorizations(any(), anyString(),
                        anyString());

        consentCoreServiceImpl.searchAuthorizations(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_USER_ID);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateAuthorizationStatusWithoutAuthId() throws Exception {

        consentCoreServiceImpl.updateAuthorizationStatus(null,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateAuthorizationStatusWithoutNewAuthStatus() throws Exception {

        consentCoreServiceImpl.updateAuthorizationStatus(ConsentMgtServiceTestData.CONSENT_ID,
                null);
    }

    @Test
    public void testUpdateAuthorizationStatus() throws Exception {

        doNothing().when(mockedConsentCoreDAO).updateAuthorizationStatus(any(), anyString(),
                anyString());
        doReturn(ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource())
                .when(mockedConsentCoreDAO).getAuthorizationResource(any(), anyString());

        consentCoreServiceImpl.updateAuthorizationStatus(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateAuthorizationStatusWithDataUpdateError() throws Exception {

        doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateAuthorizationStatus(any(), anyString(), anyString());

        consentCoreServiceImpl.updateAuthorizationStatus(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateAuthorizationStatusWithDataRetrievalError() throws Exception {

        doNothing().when(mockedConsentCoreDAO).updateAuthorizationStatus(any(), anyString(),
                anyString());
        doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getAuthorizationResource(any(), anyString());

        consentCoreServiceImpl.updateAuthorizationStatus(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateAuthorizationUserWithoutAuthorizationID() throws Exception {

        consentCoreServiceImpl.updateAuthorizationUser(null,
                ConsentMgtServiceTestData.SAMPLE_USER_ID);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateAuthorizationUserWithoutUserID() throws Exception {

        consentCoreServiceImpl.updateAuthorizationUser(ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_1,
                null);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateAuthorizationUserWithDataUpdateError() throws Exception {

        doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateAuthorizationUser(any(), anyString(), anyString());

        consentCoreServiceImpl.updateAuthorizationUser(ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_1,
                ConsentMgtServiceTestData.SAMPLE_USER_ID);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateAuthorizationUserWithDataRetrieveError() throws Exception {

        doNothing().when(mockedConsentCoreDAO).updateAuthorizationUser(any(),
                anyString(), anyString());
        doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getAuthorizationResource(any(), anyString());

        consentCoreServiceImpl.updateAuthorizationUser(ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_1,
                ConsentMgtServiceTestData.SAMPLE_USER_ID);
    }

    @Test
    public void testUpdateAuthorizationUser() throws Exception {

        doNothing().when(mockedConsentCoreDAO).updateAuthorizationUser(any(), anyString(),
                anyString());
        doReturn(ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource())
                .when(mockedConsentCoreDAO).getAuthorizationResource(any(), anyString());


        consentCoreServiceImpl.updateAuthorizationUser(ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_1,
                ConsentMgtServiceTestData.SAMPLE_USER_ID);
    }

    @Test
    public void testBindUserAccountsToConsentWithAccountIdList() throws Exception {

        doNothing().when(mockedConsentCoreDAO).updateAuthorizationUser(any(), anyString(),
                anyString());
        doNothing().when(mockedConsentCoreDAO).updateAuthorizationStatus(any(), anyString(),
                anyString());
        doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(any(),
                        any(ConsentMappingResource.class));
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                anyString());
        doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));

        Assert.assertTrue(consentCoreServiceImpl
                .bindUserAccountsToConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource(),
                        ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_ID_LIST,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
    }

    @Test
    public void testBindUserAccountsToConsent() throws Exception {

        doNothing().when(mockedConsentCoreDAO).updateAuthorizationUser(any(), anyString(),
                anyString());
        doNothing().when(mockedConsentCoreDAO).updateAuthorizationStatus(any(), anyString(),
                anyString());
        doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(any(),
                        any(ConsentMappingResource.class));
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                anyString());
        doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));

        Assert.assertTrue(consentCoreServiceImpl
                .bindUserAccountsToConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource(),
                        ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithoutNewCurrentConsentStatus() throws Exception {

        consentCoreServiceImpl.bindUserAccountsToConsent(ConsentMgtServiceTestData
                        .getSampleStoredConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS, null);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithoutConsentID() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredConsentResource();
        consentResource.setConsentID(null);

        consentCoreServiceImpl.bindUserAccountsToConsent(consentResource,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithoutClientID() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredConsentResource();
        consentResource.setClientID(null);

        consentCoreServiceImpl.bindUserAccountsToConsent(consentResource,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithoutConsentType() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredConsentResource();
        consentResource.setConsentType(null);

        consentCoreServiceImpl.bindUserAccountsToConsent(consentResource,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithoutUserID() throws Exception {

        consentCoreServiceImpl.bindUserAccountsToConsent(ConsentMgtServiceTestData
                        .getSampleStoredConsentResource(),
                null, "authID", ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithoutAuthID() throws Exception {

        consentCoreServiceImpl.bindUserAccountsToConsent(ConsentMgtServiceTestData
                        .getSampleStoredConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, null,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithoutNewAuthStatus() throws Exception {

        consentCoreServiceImpl.bindUserAccountsToConsent(ConsentMgtServiceTestData
                        .getSampleStoredConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP, null,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithEmptyAccountsAndPermissionsMap() throws Exception {

        Assert.assertTrue(consentCoreServiceImpl
                .bindUserAccountsToConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource(),
                        ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID", new HashMap<>(),
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentDataUpdateError() throws Exception {

        doThrow(ConsentDataUpdationException.class)
                .when(mockedConsentCoreDAO).updateAuthorizationUser(any(), anyString(),
                        anyString());
        consentCoreServiceImpl.bindUserAccountsToConsent(ConsentMgtServiceTestData
                        .getSampleStoredConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentDataInsertError() throws Exception {

        doThrow(ConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));
        consentCoreServiceImpl.bindUserAccountsToConsent(ConsentMgtServiceTestData
                        .getSampleStoredConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test
    public void testUpdateConsentStatus() throws Exception {

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

        consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentStatusDataRetrievalError() throws Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

        consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentStatusDataUpdateError() throws Exception {

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
        doThrow(ConsentDataUpdationException.class)
                .when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                        anyString());

        consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentStatusDataInsertError() throws Exception {

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

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentStatusWithoutConsentId() throws Exception {

        consentCoreServiceImpl.updateConsentStatus(null,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentStatusWithoutConsentStatus() throws Exception {

        consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData.CONSENT_ID,
                null);
    }

    @Test
    public void testCreateConsentAccountMapping() throws Exception {

        AuthorizationResource storedAuthorizationResource =
                ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource();

        ConsentMappingResource storedConsentMappingResource =
                ConsentMgtServiceTestData.getSampleStoredTestConsentMappingResource(sampleID);

        doReturn(storedConsentMappingResource).when(mockedConsentCoreDAO)
                .storeConsentMappingResource(any(), any());

        ArrayList<ConsentMappingResource> storedConsentMappingResources =
                consentCoreServiceImpl.createConsentAccountMappings(storedAuthorizationResource.getAuthorizationID(),
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP);

        Assert.assertNotNull(storedConsentMappingResources);
        for (ConsentMappingResource resource : storedConsentMappingResources) {
            Assert.assertNotNull(resource.getAccountID());
            Assert.assertNotNull(resource.getPermission());
            Assert.assertNotNull(resource.getAuthorizationID());
            Assert.assertEquals(resource.getMappingStatus(), ConsentCoreServiceConstants.ACTIVE_MAPPING_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentAccountMappingRollBackWhenCreation() throws Exception {

        AuthorizationResource storedAuthorizationResource =
                ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource();

        doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeConsentMappingResource(any(), any());

        consentCoreServiceImpl.createConsentAccountMappings(storedAuthorizationResource.getAuthorizationID(),
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentAccountMappingWithoutAuthID() throws Exception {

        consentCoreServiceImpl.createConsentAccountMappings(null,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentAccountMappingWithoutAccountAndPermissionsMap() throws Exception {

        consentCoreServiceImpl.createConsentAccountMappings(sampleID, new HashMap<>());
    }

    @Test
    public void testDeactivateAccountMappings() throws Exception {

        doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                any(), any());
        Assert.assertTrue(consentCoreServiceImpl
                .deactivateAccountMappings(ConsentMgtServiceTestData.UNMATCHED_MAPPING_IDS));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testDeactivateAccountMappingsWithEmptyMappingIDList() throws Exception {

        consentCoreServiceImpl.deactivateAccountMappings(new ArrayList<>());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testDeactivateAccountMappingsRollback() throws Exception {

        doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateConsentMappingStatus(any(), any(), any());
        consentCoreServiceImpl.deactivateAccountMappings(ConsentMgtServiceTestData.UNMATCHED_MAPPING_IDS);
    }

    @Test
    public void testUpdateAccountMappingStatus() throws Exception {

        doNothing().when(mockedConsentCoreDAO)
                .updateConsentMappingStatus(any(), any(), any());

        consentCoreServiceImpl.updateAccountMappingStatus(ConsentMgtServiceTestData.MAPPING_IDS_LIST,
                ConsentMgtServiceTestData.SAMPLE_MAPPING_STATUS);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateAccountMappingStatusWithoutMappingIds() throws Exception {

        doNothing().when(mockedConsentCoreDAO)
                .updateConsentMappingStatus(any(), any(), any());

        consentCoreServiceImpl.updateAccountMappingStatus(new ArrayList<>(),
                ConsentMgtServiceTestData.SAMPLE_MAPPING_STATUS);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateAccountMappingStatusDataUpdateError() throws Exception {

        doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateConsentMappingStatus(any(), any(), any());

        consentCoreServiceImpl.updateAccountMappingStatus(ConsentMgtServiceTestData.MAPPING_IDS_LIST,
                ConsentMgtServiceTestData.SAMPLE_MAPPING_STATUS);
    }

    @Test
    public void testRevokeConsent() throws Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), any());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                anyString());
        doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                        retrievedDetailedConsentResource.getConsentID(),
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
    public void testRevokeConsentWithReason() throws Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), any());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                anyString());
        doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                        retrievedDetailedConsentResource.getConsentID(),
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
    public void testRevokeConsentWithUserId() throws Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), any());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                anyString());
        doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                        retrievedDetailedConsentResource.getConsentID(),
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
    public void testRevokeConsentAndTokens() throws Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), any());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                anyString());

        doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                        retrievedDetailedConsentResource.getConsentID(),
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
    public void testRevokeConsentAndTokensTokenRevokeError() throws Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), any());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                anyString());

        doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                        retrievedDetailedConsentResource.getConsentID(),
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
            Assert.assertTrue(e instanceof ConsentManagementException);
        }

    }

    @Test
    public void testRevokeConsentWithoutConsentAttributes() throws Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();
        retrievedDetailedConsentResource.setConsentAttributes(null);

        doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), any());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                anyString());
        doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                        retrievedDetailedConsentResource.getConsentID(),
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

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeConsentWithoutConsentID() throws Exception {

        consentCoreServiceImpl.revokeConsentWithReason(null,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                false, ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeConsentWithoutNewConsentStatus() throws Exception {

        consentCoreServiceImpl.revokeConsent(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                null, ConsentMgtServiceTestData.SAMPLE_USER_ID, false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeConsentDataRetrievalError() throws Exception {

        doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), any());

        consentCoreServiceImpl.revokeConsentWithReason(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                false, ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeConsentDataInsertionError() throws Exception {

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

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeConsentDataUpdateError() throws Exception {

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
    public void testRevokeConsentWithoutReason() throws Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), any());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                anyString());
        doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                        retrievedDetailedConsentResource.getConsentID(),
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

    @Test (priority = 1)
    public void testRevokeConsentWithUserIDWithoutReason() throws Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(any(), any());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                anyString());
        doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                        retrievedDetailedConsentResource.getConsentID(),
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
    public void testRevokeExistingApplicableConsents() throws Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
        detailedConsentResources.add(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());

        doReturn(detailedConsentResources).when(mockedConsentCoreDAO)
                .searchConsents(any(), any(), any(), any(), any(),
                        any(), anyLong(), anyLong(),  anyInt(), anyInt());
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
    public void testRevokeExistingApplicableConsentsWithTokens() throws Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
        detailedConsentResources.add(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());

        doReturn(detailedConsentResources).when(mockedConsentCoreDAO)
                .searchConsents(any(), any(), any(), any(), any(),
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
    public void testRevokeExistingApplicableConsentsWithConsentsWithNoAttributes() throws Exception {

        DetailedConsentResource detailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();
        detailedConsentResource.setConsentAttributes(null);

        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
        detailedConsentResources.add(detailedConsentResource);

        doReturn(detailedConsentResources).when(mockedConsentCoreDAO)
                .searchConsents(any(), any(), any(), any(), any(),
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

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeExistingApplicableConsentsRetrieveError() throws Exception {

        doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .searchConsents(any(), any(), any(), any(), any(),
                        any(), any(), any(), any(), any());

        consentCoreServiceImpl.revokeExistingApplicableConsents(sampleID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeExistingApplicableConsentsUpdateError() throws Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
        detailedConsentResources.add(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());

        doReturn(detailedConsentResources).when(mockedConsentCoreDAO)
                .searchConsents(any(), any(), any(), any(), any(),
                        any(), any(), any(), any(), any());
        doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateConsentStatus(any(), anyString(), anyString());

        consentCoreServiceImpl.revokeExistingApplicableConsents(sampleID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeExistingApplicableConsentsInsertionError() throws Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
        detailedConsentResources.add(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());

        doReturn(detailedConsentResources).when(mockedConsentCoreDAO)
                .searchConsents(any(), any(), any(), any(), any(),
                        any(), any(), any(), any(), any());
        doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                anyString());
        doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeConsentStatusAuditRecord(any(), any());

        consentCoreServiceImpl.revokeExistingApplicableConsents(sampleID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeExistingApplicableConsentsWithoutClientID() throws Exception {

        consentCoreServiceImpl.revokeExistingApplicableConsents(null, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeExistingApplicableConsentsWithoutRevokedConsentStatus() throws Exception {

        consentCoreServiceImpl.revokeExistingApplicableConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_ID,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, null, false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeExistingApplicableConsentsWithoutUserID() throws Exception {

        consentCoreServiceImpl.revokeExistingApplicableConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_ID,
                null, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS
                , false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeExistingApplicableConsentsWithoutConsentType() throws Exception {

        consentCoreServiceImpl.revokeExistingApplicableConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_ID,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, null,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS
                , false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeExistingApplicableConsentsWithoutApplicableStatusToRevoke() throws Exception {

        consentCoreServiceImpl.revokeExistingApplicableConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_ID,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE,
                null, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS
                , false);
    }

    @Test
    public void testReAuthorizeExistingAuthResources() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredConsentResource();
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
        doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(any(),
                        any(ConsentMappingResource.class));
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
    public void testReAuthorizeExistingAuthResourceAccountsAddScenario() throws Exception {

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
        doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(any(),
                        any(ConsentMappingResource.class));
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
    public void testReAuthorizeExistingAuthResourceNoAccountsRemoveOrAddScenario() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredConsentResource();
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
        doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(any(),
                        any(ConsentMappingResource.class));
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
    public void testReAuthorizeExistingAuthResourceAccountsRemoveScenario() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredConsentResource();
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourceWithMultipleAccountIDs())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
        doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(any(),
                        any(ConsentMappingResource.class));
        doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                any(), anyString());
        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP3,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, consentResource.getCurrentStatus()));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesWithoutConsentID() throws Exception {

        Assert.assertTrue(consentCoreServiceImpl.reAuthorizeExistingAuthResource(null,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesWithoutAuthID() throws Exception {

        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        null, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesWithoutUserID() throws Exception {

        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        sampleID, null, ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesWithoutCurrentConsentStatus() throws Exception {

        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        sampleID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                        null, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesWithoutNewConsentStatus() throws Exception {

        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, null));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesWithoutAccountsAndPermissionsMap() throws Exception {

        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        new HashMap<>(), ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesDataRetrieveError() throws Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
        consentCoreServiceImpl.reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesDataInsertError() throws Exception {

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
        doReturn(ConsentMgtServiceTestData
                        .getSampleConsentMappingResourcesList(ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST))
                .when(mockedConsentCoreDAO).getConsentMappingResources(any(), anyString());
        doThrow(ConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentMappingResource(any(),
                        any(ConsentMappingResource.class));
        consentCoreServiceImpl.reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesDataUpdateError() throws Exception {

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
        doReturn(ConsentMgtServiceTestData
                        .getSampleConsentMappingResourcesList(ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST))
                .when(mockedConsentCoreDAO).getConsentMappingResources(any(), anyString());
        doThrow(ConsentDataUpdationException.class)
                .when(mockedConsentCoreDAO).updateConsentMappingStatus(any(), any(),
                        anyString());
        consentCoreServiceImpl.reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test
    public void testReAuthorizeConsentWithNewAuthResource() throws Exception {

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
        doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentMappingResource(sampleID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(any(),
                        any(ConsentMappingResource.class));
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

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceDataRetrieveError() throws Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).searchConsentAuthorizations(any(), any(), any());

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceDataUpdateError() throws Exception {

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

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceDataInsertError() throws Exception {

        AuthorizationResource authorizationResource = ConsentMgtServiceTestData
                .getSampleTestAuthorizationResource(sampleID, null);

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourcesList())
                .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(),
                        any(), any(), anyLong(), anyLong(), anyInt(),
                        anyInt());
        doNothing().when(mockedConsentCoreDAO).updateAuthorizationStatus(any(), anyString(),
                anyString());
        doReturn(authorizationResource).when(mockedConsentCoreDAO).storeAuthorizationResource(any(),
                any(AuthorizationResource.class));
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
        doThrow(ConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentMappingResource(any(),
                        any(ConsentMappingResource.class));

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutConsentID() throws Exception {

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(null, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutUserID() throws Exception {

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, null,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutAccountsMap() throws Exception {

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                null, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutCurrentConsentStatus() throws Exception {

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP, null,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutNewConsentStatus() throws Exception {

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                null, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutNewExistingAuthStatus() throws Exception {

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, null,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutNewAuthStatus() throws Exception {

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                null, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutNewAuthType() throws Exception {

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS, null);
    }

    @Test
    public void storeConsentAttributes() throws Exception {

        consentCoreServiceImpl.storeConsentAttributes(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void storeConsentAttributesWithoutParameters() throws Exception {

        consentCoreServiceImpl.storeConsentAttributes(null, null);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void storeConsentAttributesWithoutConsentId() throws Exception {

        consentCoreServiceImpl.storeConsentAttributes(null,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void storeConsentAttributesWithoutAttributeMap() throws Exception {

        consentCoreServiceImpl.storeConsentAttributes(ConsentMgtServiceTestData.CONSENT_ID,
                null);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void storeConsentAttributesEmptyAttributeMap() throws Exception {

        consentCoreServiceImpl.storeConsentAttributes(ConsentMgtServiceTestData.CONSENT_ID,
                new HashMap<>());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void storeConsentAttributesDataInsertError() throws Exception {

        doThrow(ConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentAttributes(any(), any());

        consentCoreServiceImpl.storeConsentAttributes(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    @Test
    public void testGetConsentAttributesWithAttributeKeys() throws Exception {

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

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesWithoutConsentID() throws Exception {

        consentCoreServiceImpl.getConsentAttributes(null,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesWithEmptyAttributeKeys() throws Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentAttributes(any(), anyString());
        consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                new ArrayList<>());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesConsentResourceReteivealError() throws Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentAttributes(any(), anyString(), any());
        consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesDataRetrieveError() throws Exception {

        doReturn(ConsentMgtServiceTestData.getSampleTestConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentAttributes(any(), anyString(), any());
        consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    @Test
    public void testGetConsentAttributes() throws Exception {

        doReturn(ConsentMgtServiceTestData.getSampleTestConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
        doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentAttributesObject(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).getConsentAttributes(any(), anyString());
        ConsentAttributes consentAttributes =
                consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
        Assert.assertNotNull(consentAttributes);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesWithoutAttributeKeys() throws Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentAttributes(any(), anyString());
        consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesWithoutAttributesWithoutConsentID() throws Exception {

        consentCoreServiceImpl.getConsentAttributes(null);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesConsentResourceRetrieveError() throws Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
        doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentAttributesObject(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).getConsentAttributes(any(), anyString());
        ConsentAttributes consentAttributes =
                consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
        Assert.assertNotNull(consentAttributes);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesWithDataRetrieveError() throws Exception {

        doReturn(ConsentMgtServiceTestData.getSampleTestConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
        doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getConsentAttributes(any(), anyString());
        ConsentAttributes consentAttributes =
                consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
        Assert.assertNotNull(consentAttributes);
    }

    @Test
    public void testGetConsentAttributesByName() throws Exception {

        doReturn(ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP)
                .when(mockedConsentCoreDAO).getConsentAttributesByName(any(), anyString());
        Map<String, String> retrievedAttributesMap =
                consentCoreServiceImpl.getConsentAttributesByName("x-request-id");
        Assert.assertTrue(retrievedAttributesMap.containsKey("x-request-id"));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesByNameWithoutAttributeName() throws Exception {

        consentCoreServiceImpl.getConsentAttributesByName(null);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesByNameDataRetrieveError() throws Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentAttributesByName(any(), anyString());
        consentCoreServiceImpl.getConsentAttributesByName("x-request-id");
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentIdByConsentAttributeNameAndValueWithoutAttributeName() throws Exception {

        consentCoreServiceImpl.getConsentIdByConsentAttributeNameAndValue(null,
                "domestic-payments");
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentIdByConsentAttributeNameAndValueWithoutAttributeValues() throws Exception {

        consentCoreServiceImpl.getConsentIdByConsentAttributeNameAndValue("payment-type",
                null);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentIdByConsentAttributeNameAndValueDataRetrieveError() throws Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentIdByConsentAttributeNameAndValue(any(),
                        anyString(), anyString());
        consentCoreServiceImpl.getConsentIdByConsentAttributeNameAndValue("payment-type",
                "domestic-payments");
    }

    @Test
    public void testGetConsentIdByConsentAttributeNameAndValue() throws Exception {

        doReturn(ConsentMgtServiceTestData.SAMPLE_CONSENT_IS_ARRAY)
                .when(mockedConsentCoreDAO).getConsentIdByConsentAttributeNameAndValue(any(),
                        anyString(), anyString());
        ArrayList<String> consentIdList = consentCoreServiceImpl.getConsentIdByConsentAttributeNameAndValue(
                "payment-type", "domestic-payments");
        Assert.assertFalse(consentIdList.isEmpty());
    }

    @Test
    public void testUpdateConsentAttributes() throws Exception {

        doNothing().when(mockedConsentCoreDAO).updateConsentAttributes(any(),
                anyString(), anyMap());
        doReturn(ConsentMgtServiceTestData.getSampleTestConsentAttributesObject(
                        ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).getConsentAttributes(any(), anyString());
        consentCoreServiceImpl.updateConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentAttributesWithoutConsentId() throws Exception {

        consentCoreServiceImpl.updateConsentAttributes(null,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentAttributesWithoutAttributes() throws Exception {

        consentCoreServiceImpl.updateConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID, null);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentAttributesWithEmptyAttributes() throws Exception {

        consentCoreServiceImpl.updateConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                new HashMap<>());
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentAttributesWithDataUpdateError() throws Exception {

        doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateConsentAttributes(any(), anyString(), anyMap());
        doReturn(ConsentMgtServiceTestData.getSampleTestConsentAttributesObject(
                        ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).getConsentAttributes(any(), anyString());
        consentCoreServiceImpl.updateConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentAttributesWithDataRetrieveError() throws Exception {

        doNothing().when(mockedConsentCoreDAO).updateConsentAttributes(any(),
                anyString(), anyMap());
        doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getConsentAttributes(any(), anyString());
        consentCoreServiceImpl.updateConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    @Test
    public void testDeleteConsentAttributes() throws Exception {

        doReturn(true).when(mockedConsentCoreDAO).deleteConsentAttributes(any(),
                anyString(), any());
        consentCoreServiceImpl.deleteConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testDeleteConsentAttributesDeleteError() throws Exception {

        doThrow(ConsentDataDeletionException.class)
                .when(mockedConsentCoreDAO).deleteConsentAttributes(any(), anyString(),
                        any());
        consentCoreServiceImpl.deleteConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testDeleteConsentAttributesWithoutConsentID() throws Exception {

        doReturn(true).when(mockedConsentCoreDAO).deleteConsentAttributes(any(),
                anyString(), any());
        consentCoreServiceImpl.deleteConsentAttributes(null,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testDeleteConsentAttributesWithoutAttributeKeysList() throws Exception {

        doReturn(true).when(mockedConsentCoreDAO).deleteConsentAttributes(any(),
                anyString(), any());
        consentCoreServiceImpl.deleteConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                null);
    }

    @Test
    public void testSearchConsentStatusAuditRecords() throws Exception {

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

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testSearchConsentStatusAuditRecordsWithDataRetrievalError() throws Exception {

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
    public void testGetConsentStatusAuditRecords() throws Exception {

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

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testGetConsentStatusAuditRecordsWithDataRetrievalError() throws Exception {
        ArrayList<String> consentIds = new ArrayList<>();

        doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getConsentStatusAuditRecordsByConsentId(any(), any(ArrayList.class),
                        any(), any());
        ArrayList<ConsentStatusAuditRecord> statusAuditRecords =
                consentCoreServiceImpl.getConsentStatusAuditRecords(consentIds, null, null);
        Assert.assertNotNull(statusAuditRecords);
    }

    @Test
    public void testStoreConsentAmendmentHistory() throws Exception {

        boolean result = consentCoreServiceImpl.storeConsentAmendmentHistory(sampleID,
                ConsentMgtServiceTestData.getSampleTestConsentHistoryResource(),
                ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource());

        Assert.assertTrue(result);
    }

    @Test
    public void testStoreConsentAmendmentHistoryWithoutPassingCurrentConsent() throws Exception {

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

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testStoreConsentAmendmentHistoryWithoutConsentID() throws Exception {

        consentCoreServiceImpl.storeConsentAmendmentHistory(null,
                ConsentMgtServiceTestData.getSampleTestConsentHistoryResource(),
                ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testStoreConsentAmendmentHistoryWithoutConsentHistoryResource() throws Exception {

        consentCoreServiceImpl.storeConsentAmendmentHistory(sampleID,
                null,
                ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testStoreConsentAmendmentHistoryWithZeroAsConsentAmendedTimestamp() throws Exception {

        ConsentHistoryResource consentHistoryResource = ConsentMgtServiceTestData
                .getSampleTestConsentHistoryResource();
        consentHistoryResource.setTimestamp(0);
        consentCoreServiceImpl.storeConsentAmendmentHistory(sampleID, consentHistoryResource,
                ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testStoreConsentAmendmentHistoryWithoutConsentAmendedReason() throws Exception {

        ConsentHistoryResource consentHistoryResource = ConsentMgtServiceTestData
                .getSampleTestConsentHistoryResource();
        consentHistoryResource.setReason(null);
        consentCoreServiceImpl.storeConsentAmendmentHistory(sampleID,
                consentHistoryResource,
                ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testStoreConsentAmendmentHistoryDataInsertError() throws Exception {

        doThrow(ConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentAmendmentHistory(any(), anyString(),
                        anyLong(), anyString(), anyString(), anyString(),
                        anyString());

        consentCoreServiceImpl.storeConsentAmendmentHistory(sampleID,
                ConsentMgtServiceTestData.getSampleTestConsentHistoryResource(),
                ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testStoreConsentAmendmentHistoryDataRetrievalError() throws Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

        consentCoreServiceImpl.storeConsentAmendmentHistory(sampleID,
                ConsentMgtServiceTestData.getSampleTestConsentHistoryResource(), null);
    }

    @Test ()
    public void testGetConsentAmendmentHistoryData() throws Exception {

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentHistoryDataMap())
                .when(mockedConsentCoreDAO).retrieveConsentAmendmentHistory(any(), any());
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

        Map<String, ConsentHistoryResource>  consentAmendmentHistory =
                consentCoreServiceImpl.getConsentAmendmentHistoryData(sampleID);

        Assert.assertTrue(consentAmendmentHistory.containsKey(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
        Assert.assertNotNull(consentAmendmentHistory.get(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
    }

    @Test
    public void testGetConsentAmendmentHistoryDataWithOnlyBasicConsentData() throws Exception {

        doReturn(ConsentMgtServiceTestData.getSampleConsentHistoryBasicConsentDataMap())
                .when(mockedConsentCoreDAO).retrieveConsentAmendmentHistory(any(), any());
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

        Map<String, ConsentHistoryResource>  consentAmendmentHistory =
                consentCoreServiceImpl.getConsentAmendmentHistoryData(sampleID);

        Assert.assertTrue(consentAmendmentHistory.containsKey(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
        Assert.assertNotNull(consentAmendmentHistory.get(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
    }

    @Test
    public void testGetConsentAmendmentHistoryDataWithOnlyConsentAttributesData() throws Exception {

        doReturn(ConsentMgtServiceTestData.getSampleConsentHistoryConsentAttributesDataMap())
                .when(mockedConsentCoreDAO).retrieveConsentAmendmentHistory(any(), any());
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

        Map<String, ConsentHistoryResource>  consentAmendmentHistory =
                consentCoreServiceImpl.getConsentAmendmentHistoryData(sampleID);

        Assert.assertTrue(consentAmendmentHistory.containsKey(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
        Assert.assertNotNull(consentAmendmentHistory.get(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
    }

    @Test
    public void testGetConsentAmendmentHistoryDataWithOnlyConsentMappingsData() throws Exception {

        doReturn(ConsentMgtServiceTestData.getSampleConsentHistoryConsentMappingsDataMap())
                .when(mockedConsentCoreDAO).retrieveConsentAmendmentHistory(any(), any());
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

        Map<String, ConsentHistoryResource>  consentAmendmentHistory =
                consentCoreServiceImpl.getConsentAmendmentHistoryData(sampleID);

        Assert.assertTrue(consentAmendmentHistory.containsKey(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
        Assert.assertNotNull(consentAmendmentHistory.get(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
    }

    @Test
    public void testGetConsentAmendmentHistoryDataWithNoConsentHistoryEntries() throws Exception {

        doReturn(new HashMap<>())
                .when(mockedConsentCoreDAO).retrieveConsentAmendmentHistory(any(), any());
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

        Map<String, ConsentHistoryResource>  consentAmendmentHistory =
                consentCoreServiceImpl.getConsentAmendmentHistoryData(sampleID);

        Assert.assertEquals(0, consentAmendmentHistory.size());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAmendmentHistoryDataWithoutConsentID() throws Exception {

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentHistoryDataMap())
                .when(mockedConsentCoreDAO).retrieveConsentAmendmentHistory(any(), any());
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

        consentCoreServiceImpl.getConsentAmendmentHistoryData(null);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAmendmentHistoryDataRetrieveError() throws Exception {

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentHistoryDataMap())
                .when(mockedConsentCoreDAO).retrieveConsentAmendmentHistory(any(), any());
        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

        consentCoreServiceImpl.getConsentAmendmentHistoryData(sampleID);
    }

    @Test
    public void testSearchConsents() throws Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
        detailedConsentResources.add(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());

        doReturn(detailedConsentResources)
                .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(),
                        any(), any(), anyLong(), anyLong(), anyInt(),
                        anyInt());

        consentCoreServiceImpl.searchDetailedConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST,
                ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPES_LIST,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_STATUSES_LIST,
                ConsentMgtServiceTestData.SAMPLE_USER_IDS_LIST,
                12345L, 23456L, null, null);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testSearchConsentsRetrieveError() throws Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(),
                        any(), any(), any(), any(), any(), any());

        consentCoreServiceImpl.searchDetailedConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST,
                ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPES_LIST,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_STATUSES_LIST,
                ConsentMgtServiceTestData.SAMPLE_USER_IDS_LIST,
                12345L, 23456L, null, null);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testSearchConsentsWithLimits() throws Exception {

        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(),
                        any(), any(), anyLong(), anyLong(), anyInt(),
                        anyInt());

        consentCoreServiceImpl.searchDetailedConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST,
                ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPES_LIST,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_STATUSES_LIST,
                ConsentMgtServiceTestData.SAMPLE_USER_IDS_LIST,
                12345L, 23456L, 1, 0);
    }

    @Test
    public void testAmendConsentData() throws Exception {

        doNothing().when(mockedConsentCoreDAO).updateConsentReceipt(any(),
                anyString(), anyString());
        doNothing().when(mockedConsentCoreDAO).updateConsentValidityTime(any(),
                anyString(), anyLong());
        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
        doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));

        ConsentResource consentResource =
                consentCoreServiceImpl.amendConsentData(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                        ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID);

        Assert.assertNotNull(consentResource);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendConsentDataWithoutConsentID() throws Exception {

        consentCoreServiceImpl.amendConsentData(null, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.SAMPLE_USER_ID);

    }

    @Test
    public void testAmendConsentValidityPeriod() throws Exception {

        doNothing().when(mockedConsentCoreDAO).updateConsentValidityTime(any(),
                anyString(), anyLong());
        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
        doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));

        ConsentResource consentResource =
                consentCoreServiceImpl.amendConsentData(sampleID, null,
                        ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID);

        Assert.assertNotNull(consentResource);
    }

    @Test
    public void testAmendConsentReceipt() throws Exception {

        doNothing().when(mockedConsentCoreDAO).updateConsentReceipt(any(),
                anyString(), anyString());
        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
        doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));

        ConsentResource consentResource =
                consentCoreServiceImpl.amendConsentData(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                        null, ConsentMgtServiceTestData.SAMPLE_USER_ID);

        Assert.assertNotNull(consentResource);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendConsentDataWithoutReceiptAndValidityTime() throws Exception {

        consentCoreServiceImpl.amendConsentData(sampleID, null, null,
                ConsentMgtServiceTestData.SAMPLE_USER_ID);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendConsentDataUpdateError() throws Exception {

        doThrow(ConsentDataUpdationException.class)
                .when(mockedConsentCoreDAO).updateConsentReceipt(any(), anyString(),
                        anyString());

        consentCoreServiceImpl.amendConsentData(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.SAMPLE_USER_ID);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendConsentDataRetrieveError() throws Exception {

        doNothing().when(mockedConsentCoreDAO).updateConsentReceipt(any(),
                anyString(), anyString());
        doNothing().when(mockedConsentCoreDAO).updateConsentValidityTime(any(),
                anyString(), anyLong());
        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());

        consentCoreServiceImpl.amendConsentData(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.SAMPLE_USER_ID);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendConsentDataInsertError() throws Exception {

        doNothing().when(mockedConsentCoreDAO).updateConsentReceipt(any(),
                anyString(), anyString());
        doNothing().when(mockedConsentCoreDAO).updateConsentValidityTime(any(),
                anyString(), anyLong());
        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
        doThrow(ConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));

        consentCoreServiceImpl.amendConsentData(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.SAMPLE_USER_ID);
    }

    @Test
    public void testAmendDetailedConsentData() throws Exception {

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

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataWithoutConsentID() throws Exception {

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
    public void testAmendDetailedConsentDataWithoutReceiptOnly() throws Exception {

        setInitialDataForAmendDetailedConsentSuccessFlow();
        DetailedConsentResource detailedConsentResource =
                consentCoreServiceImpl.amendDetailedConsent(sampleID,
                        ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                        null,
                        ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        new HashMap<>());

        Assert.assertNotNull(detailedConsentResource);
    }

    @Test
    public void testAmendDetailedConsentDataWithoutValidityTimeOnly() throws Exception {

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

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataWithoutReceiptAndValidityTime() throws Exception {

        consentCoreServiceImpl.amendDetailedConsent(sampleID, null, null,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                new HashMap<>());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataWithoutUserId() throws Exception {

        consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP, null,
                new HashMap<>());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataWithoutAuthId() throws Exception {

        consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD, null,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                new HashMap<>());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataWithoutNewConsentStatus() throws Exception {

        consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP, null,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                new HashMap<>());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataWithoutNewConsentAttributes() throws Exception {

        consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, null,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                new HashMap<>());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataWithoutAccountIdMapWithPermissions() throws Exception {

        consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                null,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                new HashMap<>());
    }

    @Test
    public void testAmendDetailedConsentDataWithAdditionalAmendmentData() throws Exception {

        setInitialDataForAmendDetailedConsentSuccessFlow();
        doReturn(new AuthorizationResource()).when(mockedConsentCoreDAO)
                .storeAuthorizationResource(any(), any(AuthorizationResource.class));
        doReturn(new ConsentMappingResource()).when(mockedConsentCoreDAO)
                .storeConsentMappingResource(any(), any(ConsentMappingResource.class));

        DetailedConsentResource detailedConsentResource =
                consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData
                                .SAMPLE_CONSENT_RECEIPT,
                        ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                        ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.getSampleAdditionalConsentAmendmentDataMap());

        Assert.assertNotNull(detailedConsentResource);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataWithAdditionalAmendmentDataWithoutConsentIdInAuthResources()
            throws Exception {

        setInitialDataForAmendDetailedConsentSuccessFlow();
        consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData
                        .SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.getSampleAdditionalConsentAmendmentDataMapWithoutConsentId());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataWithAdditionalAmendmentDataWithoutAccountIdInMappingResources()
            throws Exception {

        setInitialDataForAmendDetailedConsentSuccessFlow();
        consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData
                        .SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.getSampleAdditionalConsentAmendmentDataMapWithoutAccountId());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataUpdateError() throws Exception {

        doThrow(ConsentDataUpdationException.class)
                .when(mockedConsentCoreDAO).updateConsentReceipt(any(), anyString(),
                        anyString());

        consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData
                        .SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                new HashMap<>());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataRetrieveError() throws Exception {

        doNothing().when(mockedConsentCoreDAO).updateConsentReceipt(any(),
                anyString(), anyString());
        doNothing().when(mockedConsentCoreDAO).updateConsentValidityTime(any(),
                anyString(), anyLong());
        doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

        consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                new HashMap<>());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataInsertError() throws Exception {

        doNothing().when(mockedConsentCoreDAO).updateConsentReceipt(any(),
                anyString(), anyString());
        doNothing().when(mockedConsentCoreDAO).updateConsentValidityTime(any(),
                anyString(), anyLong());
        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
        doThrow(ConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));

        consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                new HashMap<>());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataDeletionError() throws Exception {

        doNothing().when(mockedConsentCoreDAO).updateConsentReceipt(any(),
                anyString(), anyString());
        doNothing().when(mockedConsentCoreDAO).updateConsentValidityTime(any(),
                anyString(), anyLong());
        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
        doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
        doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(any(),
                        any(ConsentMappingResource.class));
        doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                any(), anyString());

        doThrow(ConsentDataDeletionException.class).when(mockedConsentCoreDAO)
                .deleteConsentAttributes(any(), anyString(), any());
        doReturn(true).when(mockedConsentCoreDAO).storeConsentAttributes(any(),
                any(ConsentAttributes.class));
        consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                new HashMap<>());
    }

    private void setInitialDataForAmendDetailedConsentSuccessFlow() throws Exception {

        doNothing().when(mockedConsentCoreDAO).updateConsentReceipt(any(),
                anyString(), anyString());
        doNothing().when(mockedConsentCoreDAO).updateConsentValidityTime(any(),
                anyString(), anyLong());
        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
        doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
        doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(any(),
                        any(ConsentMappingResource.class));
        doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                any(), anyString());

        doReturn(true).when(mockedConsentCoreDAO).deleteConsentAttributes(any(),
                anyString(), any());
        doReturn(true).when(mockedConsentCoreDAO).storeConsentAttributes(any(),
                any(ConsentAttributes.class));
    }
}
