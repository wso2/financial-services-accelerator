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

package com.wso2.openbanking.accelerator.consent.mgt.service.impl;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.event.executor.OBEventQueue;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.DatabaseUtil;
import com.wso2.openbanking.accelerator.consent.mgt.dao.ConsentCoreDAO;
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
import com.wso2.openbanking.accelerator.consent.mgt.dao.persistence.ConsentStoreInitializer;
import com.wso2.openbanking.accelerator.consent.mgt.service.constants.ConsentCoreServiceConstants;
import com.wso2.openbanking.accelerator.consent.mgt.service.internal.ConsentManagementDataHolder;
import com.wso2.openbanking.accelerator.consent.mgt.service.util.ConsentMgtServiceTestData;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.OAuth2Service;
import org.wso2.carbon.identity.oauth2.dao.AccessTokenDAOImpl;
import org.wso2.carbon.identity.oauth2.dto.OAuthRevocationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuthRevocationResponseDTO;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Matchers.any;

/**
 * Test for Open Banking consent management core service.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({DatabaseUtil.class, ConsentStoreInitializer.class, ConsentManagementDataHolder.class,
        OpenBankingConfigParser.class})
public class OBConsentMgtCoreServiceTests {

    private ConsentCoreServiceImpl consentCoreServiceImpl;
    private ConsentCoreDAO mockedConsentCoreDAO;
    private String sampleID;
    private ConsentManagementDataHolder consentManagementDataHolderMock;
    private OBEventQueue obEventQueueMock;

    @BeforeClass
    public void initTest() {

        consentCoreServiceImpl = new ConsentCoreServiceImpl();
        mockedConsentCoreDAO = Mockito.mock(ConsentCoreDAO.class);
        consentManagementDataHolderMock = Mockito.mock(ConsentManagementDataHolder.class);
        obEventQueueMock = Mockito.mock(OBEventQueue.class);
    }

    @BeforeMethod
    public void mock() throws ConsentManagementException, IdentityOAuth2Exception {

        sampleID = UUID.randomUUID().toString();
        mockStaticClasses();
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @Test (priority = 2)
    public void testCreateAuthorizableConsent() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(Mockito.any(), Mockito.any());
        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleStoredTestAuthorizationResource()).when(mockedConsentCoreDAO)
                .storeAuthorizationResource(Mockito.any(), Mockito.any());

        DetailedConsentResource detailedConsentResource =
                consentCoreServiceImpl.createAuthorizableConsent(ConsentMgtServiceTestData
                                .getSampleTestConsentResource(), ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_TYPE, true);

        Assert.assertNotNull(detailedConsentResource);
        Assert.assertNotNull(detailedConsentResource.getConsentID());
        Assert.assertNotNull(detailedConsentResource.getClientID());
        Assert.assertNotNull(detailedConsentResource.getReceipt());
        Assert.assertNotNull(detailedConsentResource.getConsentType());
        Assert.assertNotNull(detailedConsentResource.getCurrentStatus());
    }

    @Test (priority = 2)
    public void testCreateAuthorizableConsentWithIsImplicitAuthFalse() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(Mockito.any(), Mockito.any());
        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleStoredTestAuthorizationResource()).when(mockedConsentCoreDAO)
                .storeAuthorizationResource(Mockito.any(), Mockito.any());

        DetailedConsentResource detailedConsentResource =
                consentCoreServiceImpl.createAuthorizableConsent(ConsentMgtServiceTestData
                                .getSampleTestConsentResource(), ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_TYPE, false);

        Assert.assertNotNull(detailedConsentResource);
        Assert.assertNotNull(detailedConsentResource.getConsentID());
        Assert.assertNotNull(detailedConsentResource.getClientID());
        Assert.assertNotNull(detailedConsentResource.getReceipt());
        Assert.assertNotNull(detailedConsentResource.getConsentType());
        Assert.assertNotNull(detailedConsentResource.getCurrentStatus());
    }

    @Test (priority = 2)
    public void testCreateAuthorizableConsentWithAttributes() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(Mockito.any(), Mockito.any());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource())
                .when(mockedConsentCoreDAO).storeAuthorizationResource(Mockito.any(), Mockito.any());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).storeConsentAttributes(Mockito.any(),
                Mockito.any());

        DetailedConsentResource detailedConsentResource =
                consentCoreServiceImpl.createAuthorizableConsent(ConsentMgtServiceTestData
                                .getSampleStoredTestConsentResourceWithAttributes(),
                        ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_TYPE, true);

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

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(Mockito.any(), Mockito.any());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource())
                .when(mockedConsentCoreDAO).storeAuthorizationResource(Mockito.any(), Mockito.any());

        DetailedConsentResource detailedConsentResource =
                consentCoreServiceImpl.createAuthorizableConsent(ConsentMgtServiceTestData
                                .getSampleStoredTestConsentResourceWithAttributes(), null,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_TYPE, true);

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

        consentCoreServiceImpl.createAuthorizableConsent(consentResource, Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyBoolean());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateAuthorizableConsentWithoutReceipt() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleTestConsentResource();
        consentResource.setReceipt(null);

        consentCoreServiceImpl.createAuthorizableConsent(consentResource, Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyBoolean());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateAuthorizableConsentWithoutConsentType() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleTestConsentResource();
        consentResource.setConsentType(null);

        consentCoreServiceImpl.createAuthorizableConsent(consentResource, Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyBoolean());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateAuthorizableConsentWithoutCurrentStatus() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleTestConsentResource();
        consentResource.setCurrentStatus(null);

        consentCoreServiceImpl.createAuthorizableConsent(consentResource, Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyBoolean());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateAuthorizableConsentWithImplicitAndNoAuthStatus() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(Mockito.any(), Mockito.any());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource())
                .when(mockedConsentCoreDAO).storeAuthorizationResource(Mockito.any(), Mockito.any());

        consentCoreServiceImpl.createAuthorizableConsent(ConsentMgtServiceTestData
                        .getSampleStoredTestConsentResourceWithAttributes(), null, null,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_TYPE, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateAuthorizableConsentWithImplicitAndNoAuthType() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(Mockito.any(), Mockito.any());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource())
                .when(mockedConsentCoreDAO).storeAuthorizationResource(Mockito.any(), Mockito.any());

        consentCoreServiceImpl.createAuthorizableConsent(ConsentMgtServiceTestData
                        .getSampleStoredTestConsentResourceWithAttributes(), null,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS, null, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateAuthorizableConsentRollback() throws Exception {

        Mockito.doThrow(OBConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeConsentResource(Mockito.any(), Mockito.any());

        consentCoreServiceImpl.createAuthorizableConsent(ConsentMgtServiceTestData
                        .getSampleTestConsentResource(), ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_TYPE, true);
    }

    @Test
    public void testCreateExclusiveConsent() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourcesList())
                .when(mockedConsentCoreDAO).searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(),
                Mockito.anyInt());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestConsentResource())
                .when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(), Mockito.anyObject());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.anyObject(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestConsentResource())
                .when(mockedConsentCoreDAO).storeConsentResource(Mockito.any(), Mockito.anyObject());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).storeConsentAttributes(Mockito.any(),
                Mockito.anyObject());
        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestAuthorizationResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).storeAuthorizationResource(Mockito.any(), Mockito.anyObject());

        DetailedConsentResource exclusiveConsent =
                consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData
                                .getSampleStoredTestConsentResource(),
                        ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
        Assert.assertNotNull(exclusiveConsent);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentDataRetrieveError() throws Exception {

        Mockito.doThrow(OBConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(),
                Mockito.anyInt());

        consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentDataUpdateError() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourcesList())
                .when(mockedConsentCoreDAO).searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(),
                Mockito.anyInt());
        Mockito.doThrow(OBConsentDataUpdationException.class)
                .when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());

        consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentDataInsertError() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourcesList())
                .when(mockedConsentCoreDAO).searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(),
                Mockito.anyInt());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestConsentResource())
                .when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doThrow(OBConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(), Mockito.anyObject());

        consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutClientID() throws Exception {

        ConsentResource sampleConsentResource = ConsentMgtServiceTestData.getSampleStoredTestConsentResource();
        sampleConsentResource.setClientID(null);

        consentCoreServiceImpl.createExclusiveConsent(sampleConsentResource,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutReceipt() throws Exception {

        ConsentResource sampleConsentResource = ConsentMgtServiceTestData.getSampleStoredTestConsentResource();
        sampleConsentResource.setReceipt(null);

        consentCoreServiceImpl.createExclusiveConsent(sampleConsentResource,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutConsentType() throws Exception {

        ConsentResource sampleConsentResource = ConsentMgtServiceTestData.getSampleStoredTestConsentResource();
        sampleConsentResource.setConsentType(null);

        consentCoreServiceImpl.createExclusiveConsent(sampleConsentResource,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutConsentStatus() throws Exception {

        ConsentResource sampleConsentResource = ConsentMgtServiceTestData.getSampleStoredTestConsentResource();
        sampleConsentResource.setCurrentStatus(null);

        consentCoreServiceImpl.createExclusiveConsent(sampleConsentResource,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutUserID() throws Exception {

        consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                null, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithouAuthStatus() throws Exception {

        consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, null,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithouAuthType() throws Exception {

        consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                null, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithImplicitAuthFalse() throws Exception {

        consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                null, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithouApplicableExistingConsentStatus() throws Exception {

        consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, null,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithouNewExistingConsentStatus() throws Exception {

        consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                null, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithouNewCurrentConsentStatus() throws Exception {

        consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                null, true);
    }

    @Test
    public void testRevokeConsent() throws Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        Mockito.doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentResource()).when(mockedConsentCoreDAO)
                .updateConsentStatus(Mockito.any(), Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                retrievedDetailedConsentResource.getConsentID(), retrievedDetailedConsentResource.getCurrentStatus()))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(), Mockito.anyObject());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.anyString());

        boolean isConsentRevoked = new MockConsentCoreServiceImpl()
                .revokeConsentWithReason(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                false, ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);

        Assert.assertTrue(isConsentRevoked);
    }

    @Test
    public void testRevokeConsentAndTokens() throws Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        Mockito.doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentResource()).when(mockedConsentCoreDAO)
                .updateConsentStatus(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                retrievedDetailedConsentResource.getConsentID(), retrievedDetailedConsentResource.getCurrentStatus()))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(), Mockito.anyObject());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.anyString());

        boolean isConsentRevoked = new MockConsentCoreServiceImpl()
                .revokeConsent(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        true);


        Assert.assertTrue(isConsentRevoked);
    }

    @Test
    public void testRevokeConsentAndTokensTokenRevokeError() throws Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        Mockito.doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentResource()).when(mockedConsentCoreDAO)
                .updateConsentStatus(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                retrievedDetailedConsentResource.getConsentID(), retrievedDetailedConsentResource.getCurrentStatus()))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(), Mockito.anyObject());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.anyString());
        try {
            boolean isConsentRevoked = new MockConsentCoreServiceImplTokenError()
                    .revokeConsentWithReason(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                            ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                            true, ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);
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

        Mockito.doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentResource()).when(mockedConsentCoreDAO)
                .updateConsentStatus(Mockito.any(), Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                retrievedDetailedConsentResource.getConsentID(), retrievedDetailedConsentResource.getCurrentStatus()))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(), Mockito.anyObject());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.anyString());

        boolean isConsentRevoked = new MockConsentCoreServiceImpl()
                .revokeConsent(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
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
    public void testRevokeConsentWithoutApplicableStatusToRevoke() throws Exception {

        consentCoreServiceImpl.revokeConsent(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeConsentWithInvalidApplicableStatus() throws Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        Mockito.doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.any(), Mockito.anyBoolean());

        consentCoreServiceImpl.revokeConsent(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeConsentDataRetrievalError() throws Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        Mockito.doThrow(OBConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.any(), Mockito.anyBoolean());

        consentCoreServiceImpl.revokeConsentWithReason(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                false, ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeConsentDataInsertionError() throws Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        Mockito.doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentResource()).when(mockedConsentCoreDAO)
                .updateConsentStatus(Mockito.any(), Mockito.anyString(), Mockito.anyString());
        Mockito.doThrow(OBConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(), Mockito.anyObject());

        new MockConsentCoreServiceImpl().revokeConsent(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeConsentDataUpdationError() throws Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        Mockito.doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        Mockito.doThrow(OBConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateConsentStatus(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        consentCoreServiceImpl.revokeConsentWithReason(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                false, ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);
    }

    @Test
    public void testRevokeExistingApplicableConsents() throws Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
        detailedConsentResources.add(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());

        Mockito.doReturn(detailedConsentResources).when(mockedConsentCoreDAO)
                .searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                        Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource()).when(mockedConsentCoreDAO)
                .updateConsentStatus(Mockito.any(), Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS)).when(mockedConsentCoreDAO)
                .storeConsentStatusAuditRecord(Mockito.any(), Mockito.anyObject());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.any());

        Assert.assertTrue(consentCoreServiceImpl.revokeExistingApplicableConsents(sampleID,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                false));
    }

    @Test
    public void testRevokeExistingApplicableConsentsWithTokens() throws Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
        detailedConsentResources.add(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());

        Mockito.doReturn(detailedConsentResources).when(mockedConsentCoreDAO)
                .searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                        Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource()).when(mockedConsentCoreDAO)
                .updateConsentStatus(Mockito.any(), Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS)).when(mockedConsentCoreDAO)
                .storeConsentStatusAuditRecord(Mockito.any(), Mockito.anyObject());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.any());

        Assert.assertTrue(new MockConsentCoreServiceImpl().revokeExistingApplicableConsents(sampleID,
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

        Mockito.doReturn(detailedConsentResources).when(mockedConsentCoreDAO)
                .searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                        Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource()).when(mockedConsentCoreDAO)
                .updateConsentStatus(Mockito.any(), Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS)).when(mockedConsentCoreDAO)
                .storeConsentStatusAuditRecord(Mockito.any(), Mockito.anyObject());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.any());

        consentCoreServiceImpl.revokeExistingApplicableConsents(sampleID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeExistingApplicableConsentsRetrieveError() throws Exception {

        Mockito.doThrow(OBConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                        Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());

        consentCoreServiceImpl.revokeExistingApplicableConsents(sampleID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeExistingApplicableConsentsUpdateError() throws Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
        detailedConsentResources.add(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());

        Mockito.doReturn(detailedConsentResources).when(mockedConsentCoreDAO)
                .searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                        Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());
        Mockito.doThrow(OBConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateConsentStatus(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        consentCoreServiceImpl.revokeExistingApplicableConsents(sampleID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeExistingApplicableConsentsInsertionError() throws Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
        detailedConsentResources.add(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());

        Mockito.doReturn(detailedConsentResources).when(mockedConsentCoreDAO)
                .searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                        Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource()).when(mockedConsentCoreDAO)
                .updateConsentStatus(Mockito.any(), Mockito.anyString(), Mockito.anyString());
        Mockito.doThrow(OBConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeConsentStatusAuditRecord(Mockito.any(), Mockito.anyObject());

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

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentFileErrorWhenRetrieval() throws Exception {

        Mockito.doThrow(OBConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getConsentResource(Mockito.any(), Mockito.anyString());

        consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                        .getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentFileRollBackWhenCreation() throws Exception {

        ConsentResource storedConsentResource = ConsentMgtServiceTestData.getSampleStoredTestConsentResource();

        Mockito.doReturn(storedConsentResource).when(mockedConsentCoreDAO)
                .getConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doThrow(OBConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeConsentFile(Mockito.any(), Mockito.any());

        consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                        .getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                Mockito.anyString());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentFileRollBackWhenUpdating() throws Exception {

        ConsentResource storedConsentResource = ConsentMgtServiceTestData.getSampleStoredTestConsentResource();

        Mockito.doReturn(storedConsentResource).when(mockedConsentCoreDAO)
                .getConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doThrow(OBConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateConsentStatus(Mockito.any(), Mockito.anyString(), Mockito.anyString());

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

        Mockito.doReturn(consentResource).when(mockedConsentCoreDAO).getConsentResource(Mockito.any(),
                Mockito.anyString());

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
        consentCoreServiceImpl.createConsentFile(sampleConsentFile,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);
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
    public void testGetConsent() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource()).when(mockedConsentCoreDAO)
                .getConsentResource(Mockito.any(), Mockito.anyString());

        // Get consent
        ConsentResource retrievedConsentResource = consentCoreServiceImpl.getConsent(ConsentMgtServiceTestData
                .getSampleStoredTestConsentResource().getConsentID(), false);

        Assert.assertNotNull(retrievedConsentResource);
    }

    @Test
    public void testGetConsentWithAttributes() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResourceWithAttributes())
                .when(mockedConsentCoreDAO).getConsentResourceWithAttributes(Mockito.any(), Mockito.anyString());

        // Get consent
        ConsentResource retrievedConsentResource = consentCoreServiceImpl.getConsent(ConsentMgtServiceTestData
                .getSampleStoredTestConsentResource().getConsentID(), true);

        Assert.assertNotNull(retrievedConsentResource);
        Assert.assertNotNull(retrievedConsentResource.getConsentAttributes());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentRollBackWhenRetrieve() throws Exception {

        Mockito.doThrow(OBConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getConsentResource(Mockito.any(), Mockito.anyString());

        // Get consent
        consentCoreServiceImpl.getConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource().getConsentID(),
                false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentWithoutConsentID() throws Exception {

        consentCoreServiceImpl.getConsent(null, false);
    }

    @Test
    public void testGetDetailedConsent() throws Exception {

        DetailedConsentResource detailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        Mockito.doReturn(detailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());

        // Get consent
        DetailedConsentResource retrievedConsentResource =
                consentCoreServiceImpl.getDetailedConsent(detailedConsentResource.getConsentID());

        Assert.assertNotNull(retrievedConsentResource);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetDetailedConsentWithoutConsentID() throws Exception {

        consentCoreServiceImpl.getDetailedConsent(null);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetDetailedConsentRetrieveError() throws Exception {

        Mockito.doThrow(OBConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());
        consentCoreServiceImpl.getDetailedConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource()
                .getConsentID());
    }

    @Test
    public void testCreateConsentAuthorization() throws Exception {

        AuthorizationResource sampleAuthorizationResource =
                ConsentMgtServiceTestData.getSampleTestAuthorizationResource(sampleID);

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource())
                .when(mockedConsentCoreDAO).storeAuthorizationResource(Mockito.any(), Mockito.any());

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
                ConsentMgtServiceTestData.getSampleTestAuthorizationResource(sampleID);

        Mockito.doThrow(OBConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeAuthorizationResource(Mockito.any(), Mockito.any());

        // Get consent
        consentCoreServiceImpl.createConsentAuthorization(sampleAuthorizationResource);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentAuthorizationWithoutConsentID() throws Exception {

        AuthorizationResource sampleAuthorizationResource =
                ConsentMgtServiceTestData.getSampleTestAuthorizationResource(null);

        //Create a consent authorization resource
        consentCoreServiceImpl.createConsentAuthorization(sampleAuthorizationResource);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentAuthorizationWithoutAuthorizationStatus() throws Exception {

        AuthorizationResource sampleAuthorizationResource =
                ConsentMgtServiceTestData.getSampleTestAuthorizationResource(sampleID);

        // Explicitly setting authorization status to null
        sampleAuthorizationResource.setAuthorizationStatus(null);

        //Create a consent authorization resource
        consentCoreServiceImpl.createConsentAuthorization(sampleAuthorizationResource);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentAuthorizationWithoutAuthorizationType() throws Exception {

        AuthorizationResource sampleAuthorizationResource =
                ConsentMgtServiceTestData.getSampleTestAuthorizationResource(sampleID);
        sampleAuthorizationResource.setAuthorizationType(null);

        //Create a consent authorization resource
        consentCoreServiceImpl.createConsentAuthorization(sampleAuthorizationResource);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentAuthorizationWithoutAuthorizationUserID() throws Exception {

        AuthorizationResource sampleAuthorizationResource =
                ConsentMgtServiceTestData.getSampleTestAuthorizationResource(sampleID);
        sampleAuthorizationResource.setUserID(null);

        //Create a consent authorization resource
        consentCoreServiceImpl.createConsentAuthorization(sampleAuthorizationResource);
    }

    @Test
    public void testCreateConsentAccountMapping() throws Exception {

        AuthorizationResource storedAuthorizationResource =
                ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource();

        ConsentMappingResource storedConsentMappingResource =
                ConsentMgtServiceTestData.getSampleStoredTestConsentMappingResource(sampleID);

        Mockito.doReturn(storedConsentMappingResource).when(mockedConsentCoreDAO)
                .storeConsentMappingResource(Mockito.any(), Mockito.any());

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

        Mockito.doThrow(OBConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeConsentMappingResource(Mockito.any(), Mockito.any());

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

        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.any());
        Assert.assertTrue(consentCoreServiceImpl
                .deactivateAccountMappings(ConsentMgtServiceTestData.UNMATCHED_MAPPING_IDS));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testDeactivateAccountMappingsWithEmptyMappingIDList() throws Exception {

        consentCoreServiceImpl.deactivateAccountMappings(new ArrayList<>());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testDeactivateAccountMappingsRollback() throws Exception {

        Mockito.doThrow(OBConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateConsentMappingStatus(Mockito.any(), Mockito.any(), Mockito.any());
        consentCoreServiceImpl.deactivateAccountMappings(ConsentMgtServiceTestData.UNMATCHED_MAPPING_IDS);
    }

    @Test
    public void testUpdateAccountMappingPermissionWithEmptyMap() {

        try {
            consentCoreServiceImpl.updateAccountMappingPermission(new HashMap<>());
            Assert.fail("Expected ConsentManagementException to be thrown");
        } catch (ConsentManagementException e) {
            Assert.assertEquals(e.getMessage(), "Cannot proceed since account mapping IDs are not provided");
        }
    }

    @Test
    public void testUpdateAccountMappingPermission() throws Exception {

        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentMappingPermission(Mockito.any(),
                Mockito.any());
        Assert.assertTrue(consentCoreServiceImpl
                .updateAccountMappingPermission(ConsentMgtServiceTestData.SAMPLE_MAPPING_ID_PERMISSION_MAP));
    }

    @Test
    public void testSearchConsents() throws Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
        detailedConsentResources.add(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());

        Mockito.doReturn(detailedConsentResources)
                .when(mockedConsentCoreDAO).searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());

        consentCoreServiceImpl.searchDetailedConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST,
                ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPES_LIST,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_STATUSES_LIST, ConsentMgtServiceTestData.SAMPLE_USER_IDS_LIST,
                12345L, 23456L, null, null);
    }

    @Test
    public void testSearchConsentsInRetention() throws Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
        detailedConsentResources.add(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());

        Mockito.doReturn(detailedConsentResources)
                .when(mockedConsentCoreDAO).searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(),
                Mockito.anyInt());

        consentCoreServiceImpl.searchDetailedConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST,
                ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPES_LIST,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_STATUSES_LIST, ConsentMgtServiceTestData.SAMPLE_USER_IDS_LIST,
                12345L, 23456L, null, null, false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testSearchConsentsRetrieveError() throws Exception {

        Mockito.doThrow(OBConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());

        consentCoreServiceImpl.searchDetailedConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST,
                ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPES_LIST,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_STATUSES_LIST, ConsentMgtServiceTestData.SAMPLE_USER_IDS_LIST,
                12345L, 23456L, null, null);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testSearchConsentsRetrieveErrorInRetention() throws Exception {

        Mockito.doThrow(OBConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(),
                Mockito.anyInt());

        consentCoreServiceImpl.searchDetailedConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST,
                ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPES_LIST,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_STATUSES_LIST, ConsentMgtServiceTestData.SAMPLE_USER_IDS_LIST,
                12345L, 23456L, null, null, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testSearchConsentsWithLimits() throws Exception {

        Mockito.doThrow(OBConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());

        consentCoreServiceImpl.searchDetailedConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST,
                ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPES_LIST,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_STATUSES_LIST, ConsentMgtServiceTestData.SAMPLE_USER_IDS_LIST,
                12345L, 23456L, 1, 0);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testSearchConsentsInRetentionDBWithLimits() throws Exception {

        Mockito.doThrow(OBConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(),
                Mockito.anyInt());

        consentCoreServiceImpl.searchDetailedConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST,
                ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPES_LIST,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_STATUSES_LIST, ConsentMgtServiceTestData.SAMPLE_USER_IDS_LIST,
                12345L, 23456L, 1, 0, true);
    }

    @Test
    public void testBindUserAccountsToConsentWithAccountIdList() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestAuthorizationResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).updateAuthorizationUser(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestAuthorizationResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).updateAuthorizationStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(Mockito.any(), Mockito.anyObject());
        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentResource())
                .when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(), Mockito.anyObject());

        Assert.assertTrue(consentCoreServiceImpl
                .bindUserAccountsToConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                        ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_ID_LIST,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
    }

    @Test
    public void testBindUserAccountsToConsent() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestAuthorizationResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).updateAuthorizationUser(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestAuthorizationResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).updateAuthorizationStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(Mockito.any(), Mockito.anyObject());
        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentResource())
                .when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(), Mockito.anyObject());

        Assert.assertTrue(consentCoreServiceImpl
                .bindUserAccountsToConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                        ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithoutNewCurrentConsentStatus() throws Exception {

        consentCoreServiceImpl.bindUserAccountsToConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS, null);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithoutConsentID() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredTestConsentResource();
        consentResource.setConsentID(null);

        consentCoreServiceImpl.bindUserAccountsToConsent(consentResource,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithoutClientID() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredTestConsentResource();
        consentResource.setClientID(null);

        consentCoreServiceImpl.bindUserAccountsToConsent(consentResource,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithotConsentType() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredTestConsentResource();
        consentResource.setConsentType(null);

        consentCoreServiceImpl.bindUserAccountsToConsent(consentResource,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithoutUserID() throws Exception {

        consentCoreServiceImpl.bindUserAccountsToConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                null, "authID", ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithoutAuthID() throws Exception {

        consentCoreServiceImpl.bindUserAccountsToConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, null,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithoutNewAuthStatus() throws Exception {

        consentCoreServiceImpl.bindUserAccountsToConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP, null,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithEmptyAccountsAndPermissionsMap() throws Exception {

        Assert.assertTrue(consentCoreServiceImpl
                .bindUserAccountsToConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                        ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID", new HashMap<>(),
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentDataRetrieveError() throws Exception {

        Mockito.doThrow(OBConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());
        consentCoreServiceImpl.bindUserAccountsToConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentDataUpdateError() throws Exception {

        Mockito.doThrow(OBConsentDataUpdationException.class)
                .when(mockedConsentCoreDAO).updateAuthorizationUser(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        consentCoreServiceImpl.bindUserAccountsToConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentDataInsertError() throws Exception {

        Mockito.doThrow(OBConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(), Mockito.anyObject());
        consentCoreServiceImpl.bindUserAccountsToConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test
    public void testReAuthorizeExistingAuthResources() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredTestConsentResource();
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString(),
                Mockito.anyBoolean());
        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(Mockito.any(), Mockito.anyObject());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.anyObject(), Mockito.anyString());
        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
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

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredTestConsentResource();
        Mockito.doReturn(detailedConsentResource)
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString(),
                Mockito.anyBoolean());
        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(Mockito.any(), Mockito.anyObject());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.anyObject(), Mockito.anyString());
        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, consentResource.getCurrentStatus()));
    }

    @Test
    public void testReAuthorizeExistingAuthResourceNoAccountsRemoveOrAddScenario() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredTestConsentResource();
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString(),
                Mockito.anyBoolean());
        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(Mockito.any(), Mockito.anyObject());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.anyObject(), Mockito.anyString());
        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP2,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, consentResource.getCurrentStatus()));
    }

    @Test
    public void testReAuthorizeExistingAuthResourceAccountsRemoveScenario() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredTestConsentResource();
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourceWithMultipleAccountIDs())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString(),
                Mockito.anyBoolean());
        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(Mockito.any(), Mockito.anyObject());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.anyObject(), Mockito.anyString());
        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP3,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, consentResource.getCurrentStatus()));
    }

    @Test
    public void testReAuthorizeExistingAuthResourcesWithoutMatchingStatuses() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredTestConsentResource();
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString(),
                Mockito.anyBoolean());
        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(Mockito.any(), Mockito.anyObject());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.anyObject(), Mockito.anyString());
        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
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
    public void testReAuthorizeExistingAuthResourcesWithoutApplicableConsentStatus() throws Exception {

        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesWithoutNewConsentStatus() throws Exception {

        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, null));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesWithoutAccountsAndPermissionsMap() throws Exception {

        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        new HashMap<>(), ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesDataRetrieveError() throws Exception {

        Mockito.doThrow(OBConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString(),
                Mockito.anyBoolean());
        consentCoreServiceImpl.reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesDataInsertError() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleConsentMappingResourcesList(ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST))
                .when(mockedConsentCoreDAO).getConsentMappingResources(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doThrow(OBConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentMappingResource(Mockito.any(), Mockito.anyObject());
        consentCoreServiceImpl.reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesDataUpdateError() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleConsentMappingResourcesList(ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST))
                .when(mockedConsentCoreDAO).getConsentMappingResources(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doThrow(OBConsentDataUpdationException.class)
                .when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(), Mockito.anyObject(),
                Mockito.anyString());
        consentCoreServiceImpl.reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
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

        Mockito.doThrow(OBConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentAttributes(Mockito.any(), Mockito.any());

        consentCoreServiceImpl.storeConsentAttributes(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    @Test
    public void testGetConsentAttributesWithAttributeKeys() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentAttributesObject(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).getConsentAttributes(Mockito.any(), Mockito.anyString(),
                Mockito.anyObject());
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
    public void testGetConsentAttributesDataRetrieveError() throws Exception {

        Mockito.doThrow(OBConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentAttributes(Mockito.any(), Mockito.anyString());
        consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID, new ArrayList<>());
    }

    @Test
    public void testGetConsentAttributes() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentAttributesObject(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).getConsentAttributes(Mockito.any(), Mockito.anyString());
        ConsentAttributes consentAttributes =
                consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
        Assert.assertNotNull(consentAttributes);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesWithoutAttributeKeys() throws Exception {

        Mockito.doThrow(OBConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentAttributes(Mockito.any(), Mockito.anyString());
        consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesWithoutAttributesWithoutConsentID() throws Exception {

        consentCoreServiceImpl.getConsentAttributes(null);
    }

    @Test
    public void testGetConsentAttributesByName() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP)
                .when(mockedConsentCoreDAO).getConsentAttributesByName(Mockito.any(), Mockito.anyString());
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

        Mockito.doThrow(OBConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentAttributesByName(Mockito.any(), Mockito.anyString());
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

        Mockito.doThrow(OBConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentIdByConsentAttributeNameAndValue(Mockito.any(),
                Mockito.anyString(), Mockito.anyString());
        consentCoreServiceImpl.getConsentIdByConsentAttributeNameAndValue("payment-type",
                "domestic-payments");
    }

    @Test
    public void testGetConsentIdByConsentAttributeNameAndValue() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.SAMPLE_CONSENT_IS_ARRAY)
                .when(mockedConsentCoreDAO).getConsentIdByConsentAttributeNameAndValue(Mockito.any(),
                Mockito.anyString(), Mockito.anyString());
        ArrayList<String> consentIdList = consentCoreServiceImpl.getConsentIdByConsentAttributeNameAndValue(
                "payment-type", "domestic-payments");
        Assert.assertFalse(consentIdList.isEmpty());
    }

    @Test
    public void testGetConsentFile() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT))
                .when(mockedConsentCoreDAO).getConsentFile(Mockito.any(), Mockito.anyString(),
                Mockito.anyBoolean());
        ConsentFile consentFile = consentCoreServiceImpl
                .getConsentFile(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
        Assert.assertNotNull(consentFile);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentFileWithoutConsentID() throws Exception {

        consentCoreServiceImpl.getConsentFile(null);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentFileDataRetrieveError() throws Exception {

        Mockito.doThrow(OBConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentFile(Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());
        consentCoreServiceImpl.getConsentFile(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
    }

    @Test
    public void testGetAuthorizationResource() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource())
                .when(mockedConsentCoreDAO).getAuthorizationResource(Mockito.any(), Mockito.anyString());
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

        Mockito.doThrow(OBConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getAuthorizationResource(Mockito.any(), Mockito.anyString());
        consentCoreServiceImpl.getAuthorizationResource(ConsentMgtServiceTestData
                .getSampleStoredTestAuthorizationResource().getAuthorizationID());
    }

    @Test
    public void testSearchConsentStatusAuditRecords() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleStoredTestConsentStatusAuditRecordsList(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).getConsentStatusAuditRecords(Mockito.any(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString(),
                Mockito.anyBoolean());
        ArrayList<ConsentStatusAuditRecord> consentStatusAuditRecords =
                consentCoreServiceImpl.searchConsentStatusAuditRecords(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_ACTION_BY,
                        ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                        ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                        ConsentMgtServiceTestData.SAMPLE_AUDIT_ID);
        Assert.assertNotNull(consentStatusAuditRecords);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testSearchConsentStatusAuditRecordsDataRetrieveError() throws Exception {

        Mockito.doThrow(OBConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getConsentStatusAuditRecords(Mockito.any(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString(),
                        Mockito.anyBoolean());
        consentCoreServiceImpl.searchConsentStatusAuditRecords(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_ACTION_BY,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD, ConsentMgtServiceTestData.SAMPLE_AUDIT_ID);
    }

    @Test
    public void testSearchAuthorizations() throws Exception {

        ArrayList<String> consentIDs = new ArrayList<>();
        consentIDs.add(UUID.randomUUID().toString());

        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleAuthorizationResourcesList(consentIDs))
                .when(mockedConsentCoreDAO).searchConsentAuthorizations(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        ArrayList<AuthorizationResource> retrievedAuthorizations =
                consentCoreServiceImpl.searchAuthorizations(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID);
        Assert.assertNotNull(retrievedAuthorizations);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testSearchAuthorizationsDataRetrieveError() throws Exception {

        Mockito.doThrow(OBConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).searchConsentAuthorizations(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());

        consentCoreServiceImpl.searchAuthorizations(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_USER_ID);
    }

    @Test
    public void testDeleteConsentAttributes() throws Exception {

        Mockito.doReturn(true).when(mockedConsentCoreDAO).deleteConsentAttributes(Mockito.any(),
                Mockito.anyString(), Mockito.anyObject());
        consentCoreServiceImpl.deleteConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testDeleteConsentAttributesDeleteError() throws Exception {

        Mockito.doThrow(OBConsentDataDeletionException.class)
                .when(mockedConsentCoreDAO).deleteConsentAttributes(Mockito.any(), Mockito.anyString(),
                Mockito.anyObject());
        consentCoreServiceImpl.deleteConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testDeleteConsentAttributesWithoutConsentID() throws Exception {

        Mockito.doReturn(true).when(mockedConsentCoreDAO).deleteConsentAttributes(Mockito.any(),
                Mockito.anyString(), Mockito.anyObject());
        consentCoreServiceImpl.deleteConsentAttributes(null,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testDeleteConsentAttributesWithoutAttributeKeysList() throws Exception {

        Mockito.doReturn(true).when(mockedConsentCoreDAO).deleteConsentAttributes(Mockito.any(),
                Mockito.anyString(), Mockito.anyObject());
        consentCoreServiceImpl.deleteConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                null);
    }

    @Test
    public void testReAuthorizeConsentWithNewAuthResource() throws Exception {

        AuthorizationResource authorizationResource = ConsentMgtServiceTestData
                .getSampleTestAuthorizationResource(sampleID);
        ArrayList<String> consentIDs = new ArrayList<>();
        consentIDs.add(sampleID);

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleAuthorizationResourcesList(consentIDs))
                .when(mockedConsentCoreDAO).searchConsentAuthorizations(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(authorizationResource)
                .when(mockedConsentCoreDAO).updateAuthorizationStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(authorizationResource).when(mockedConsentCoreDAO).storeAuthorizationResource(Mockito.any(),
                Mockito.anyObject());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString(),
                Mockito.anyBoolean());
        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentMappingResource(sampleID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(Mockito.any(), Mockito.anyObject());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.anyObject(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestConsentResource())
                .when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentStatusAuditRecord(sampleID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(), Mockito.anyObject());

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceDataRetrieveError() throws Exception {

        Mockito.doThrow(OBConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).searchConsentAuthorizations(Mockito.any(), Mockito.any(), Mockito.any());

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
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleAuthorizationResourcesList(consentIDs))
                .when(mockedConsentCoreDAO).searchConsentAuthorizations(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doThrow(OBConsentDataUpdationException.class)
                .when(mockedConsentCoreDAO).updateAuthorizationStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());

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
                .getSampleTestAuthorizationResource(sampleID);

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourcesList())
                .when(mockedConsentCoreDAO).searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());
        Mockito.doReturn(authorizationResource)
                .when(mockedConsentCoreDAO).updateAuthorizationStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(authorizationResource).when(mockedConsentCoreDAO).storeAuthorizationResource(Mockito.any(),
                Mockito.anyObject());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString(),
                Mockito.anyBoolean());
        Mockito.doThrow(OBConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentMappingResource(Mockito.any(), Mockito.anyObject());

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

    private void mockStaticClasses() throws ConsentManagementException, IdentityOAuth2Exception {

        PowerMockito.mockStatic(DatabaseUtil.class);
        PowerMockito.when(DatabaseUtil.getDBConnection()).thenReturn(Mockito.mock(Connection.class));
        PowerMockito.when(DatabaseUtil.getRetentionDBConnection()).thenReturn(Mockito.mock(Connection.class));

        PowerMockito.mockStatic(ConsentStoreInitializer.class);
        PowerMockito.when(ConsentStoreInitializer.getInitializedConsentCoreDAOImpl()).thenReturn(mockedConsentCoreDAO);
        PowerMockito.when(ConsentStoreInitializer
                .getInitializedConsentRetentionDAOImpl()).thenReturn(mockedConsentCoreDAO);

        PowerMockito.mockStatic(ConsentManagementDataHolder.class);
        PowerMockito.when(ConsentManagementDataHolder.getInstance()).thenReturn(consentManagementDataHolderMock);

        PowerMockito.when(consentManagementDataHolderMock.getOBEventQueue()).thenReturn(obEventQueueMock);

        AccessTokenDAOImpl accessTokenDAOMock = Mockito.mock(AccessTokenDAOImpl.class);
        Mockito.doNothing().when(accessTokenDAOMock).revokeAccessTokens(Mockito.any(String[].class));
        PowerMockito.when(consentManagementDataHolderMock.getAccessTokenDAO()).thenReturn(accessTokenDAOMock);

        OpenBankingConfigParser openBankingConfigParserMock = Mockito.mock(OpenBankingConfigParser.class);
        Map<String, Object> configuration = new HashMap<>();
        configuration.put(OpenBankingConstants.CONSENT_ID_CLAIM_NAME, "OB_CONSENT_ID");
        Mockito.when(openBankingConfigParserMock.getConfiguration()).thenReturn(configuration);
        Mockito.when(openBankingConfigParserMock.isConsentDataRetentionEnabled()).thenReturn(true);
        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);
    }

    @Test
    public void testAmendConsentData() throws Exception {

        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentReceipt(Mockito.any(),
                Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentValidityTime(Mockito.any(),
                Mockito.anyString(), Mockito.anyLong());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(), Mockito.anyObject());

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

        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentValidityTime(Mockito.any(),
                Mockito.anyString(), Mockito.anyLong());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(), Mockito.anyObject());

        ConsentResource consentResource =
                consentCoreServiceImpl.amendConsentData(sampleID, null,
                        ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID);

        Assert.assertNotNull(consentResource);
    }

    @Test
    public void testAmendConsentReceipt() throws Exception {

        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentReceipt(Mockito.any(),
                Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(), Mockito.anyObject());

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

        Mockito.doThrow(OBConsentDataUpdationException.class)
                .when(mockedConsentCoreDAO).updateConsentReceipt(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());

        consentCoreServiceImpl.amendConsentData(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
            ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
            ConsentMgtServiceTestData.SAMPLE_USER_ID);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendConsentDataRetrieveError() throws Exception {

        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentReceipt(Mockito.any(),
                Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentValidityTime(Mockito.any(),
                Mockito.anyString(), Mockito.anyLong());
        Mockito.doThrow(OBConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentResource(Mockito.any(), Mockito.anyString());

        consentCoreServiceImpl.amendConsentData(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
            ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
            ConsentMgtServiceTestData.SAMPLE_USER_ID);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendConsentDataInsertError() throws Exception {

        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentReceipt(Mockito.any(),
                Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentValidityTime(Mockito.any(),
                Mockito.anyString(), Mockito.anyLong());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doThrow(OBConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(), Mockito.anyObject());

        consentCoreServiceImpl.amendConsentData(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.SAMPLE_USER_ID);
    }

    @Test
    public void testUpdateConsentStatus() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString(),
                Mockito.anyBoolean());

        consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentStatusDataRetrievalError() throws Exception {

        Mockito.doThrow(OBConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentResource(Mockito.any(), Mockito.anyObject());

        consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentStatusDataInsertError() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doThrow(OBConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyObject(),
                Mockito.anyObject());

        consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentStatusDataUpdateError() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doThrow(OBConsentDataUpdationException.class)
                .when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyObject(),
                Mockito.anyObject());

        consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentStatusWithoutConsentId() throws Exception {

        consentCoreServiceImpl.updateConsentStatus(null,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentStatusWithoutUserId() throws Exception {

        consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentStatusWithoutConsentStatus() throws Exception {

        consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData.CONSENT_ID,
                null);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentsEligibleForExpirationDataRetrievalError() throws Exception {

        Mockito.doThrow(new OBConsentDataRetrievalException("Error"))
                .when(mockedConsentCoreDAO).getExpiringConsents(Mockito.any(), Mockito.anyString());
        ArrayList<DetailedConsentResource> consentsEligibleForExpiration =
                consentCoreServiceImpl.getConsentsEligibleForExpiration("authorised");

        Assert.assertTrue(!consentsEligibleForExpiration.isEmpty());
        Assert.assertEquals(consentsEligibleForExpiration.get(0).getConsentID(),
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourcesList().get(0).getConsentID());
    }

    @Test
    public void testGetConsentsEligibleForExpiration() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourcesList())
                .when(mockedConsentCoreDAO).getExpiringConsents(Mockito.any(), Mockito.anyString());
        ArrayList<DetailedConsentResource> consentsEligibleForExpiration =
                consentCoreServiceImpl.getConsentsEligibleForExpiration("authorised");

        Assert.assertTrue(!consentsEligibleForExpiration.isEmpty());
        Assert.assertEquals(consentsEligibleForExpiration.get(0).getConsentID(),
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourcesList().get(0).getConsentID());
    }

    @Test
    public void testRevokeConsentWithoutReason() throws Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        Mockito.doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentResource()).when(mockedConsentCoreDAO)
                .updateConsentStatus(Mockito.any(), Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                        retrievedDetailedConsentResource.getConsentID(),
                        retrievedDetailedConsentResource.getCurrentStatus()))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(), Mockito.anyObject());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.anyString());

        boolean isConsentRevoked = new MockConsentCoreServiceImpl()
                .revokeConsent(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);

        Assert.assertTrue(isConsentRevoked);
    }

    @Test (priority = 1)
    public void testRevokeConsentWithUserIDWithoutReason() throws Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        Mockito.doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentResource()).when(mockedConsentCoreDAO)
                .updateConsentStatus(Mockito.any(), Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                        retrievedDetailedConsentResource.getConsentID(),
                        retrievedDetailedConsentResource.getCurrentStatus()))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(), Mockito.anyObject());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.anyString());

        boolean isConsentRevoked = new MockConsentCoreServiceImpl()
                .revokeConsent(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID);

        Assert.assertTrue(isConsentRevoked);
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

        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestAuthorizationResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).updateAuthorizationStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());

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

    @Test
    public void testUpdateAuthorizationUser() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestAuthorizationResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).updateAuthorizationUser(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());

        consentCoreServiceImpl.updateAuthorizationUser(ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_1,
                ConsentMgtServiceTestData.SAMPLE_USER_ID);
    }

    private void setInitialDataForAmendDetailedConsentSuccessFlow() throws Exception {

        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentReceipt(Mockito.any(),
                Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentValidityTime(Mockito.any(),
                Mockito.anyString(), Mockito.anyLong());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(), Mockito.anyObject());

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString(),
                Mockito.anyBoolean());
        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(Mockito.any(), Mockito.anyObject());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.anyObject(), Mockito.anyString());

        Mockito.doReturn(true).when(mockedConsentCoreDAO).deleteConsentAttributes(Mockito.any(),
                Mockito.anyString(), Mockito.anyObject());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).storeConsentAttributes(Mockito.any(),
                Mockito.anyObject());
    }

    @Test
    public void testAmendDetailedConsentData() throws Exception {

        setInitialDataForAmendDetailedConsentSuccessFlow();
        DetailedConsentResource detailedConsentResource =
                consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
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
                consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
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
        Mockito.doReturn(new AuthorizationResource()).when(mockedConsentCoreDAO)
                .storeAuthorizationResource(Mockito.any(), Mockito.anyObject());
        Mockito.doReturn(new ConsentMappingResource()).when(mockedConsentCoreDAO)
                .storeConsentMappingResource(Mockito.any(), Mockito.anyObject());

        DetailedConsentResource detailedConsentResource =
                consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
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
        consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
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
        consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
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

        Mockito.doThrow(OBConsentDataUpdationException.class)
                .when(mockedConsentCoreDAO).updateConsentReceipt(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());

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
    public void testAmendDetailedConsentDataRetrieveError() throws Exception {

        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentReceipt(Mockito.any(),
                Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentValidityTime(Mockito.any(),
                Mockito.anyString(), Mockito.anyLong());
        Mockito.doThrow(OBConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString(),
                Mockito.anyBoolean());

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

        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentReceipt(Mockito.any(),
                Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentValidityTime(Mockito.any(),
                Mockito.anyString(), Mockito.anyLong());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doThrow(OBConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(), Mockito.anyObject());

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

        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentReceipt(Mockito.any(),
                Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentValidityTime(Mockito.any(),
                Mockito.anyString(), Mockito.anyLong());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(), Mockito.anyObject());

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString(),
                Mockito.anyBoolean());
        Mockito.doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(Mockito.any(), Mockito.anyObject());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.anyObject(), Mockito.anyString());

        Mockito.doThrow(OBConsentDataDeletionException.class).when(mockedConsentCoreDAO)
                .deleteConsentAttributes(Mockito.any(), Mockito.anyString(), Mockito.anyObject());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).storeConsentAttributes(Mockito.any(),
                Mockito.anyObject());
        consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                new HashMap<>());
    }

    @Test
    public void testStoreConsentAmendmentHistory() throws Exception {

        boolean result = consentCoreServiceImpl.storeConsentAmendmentHistory(sampleID,
                ConsentMgtServiceTestData.getSampleTestConsentHistoryResource(),
                ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource());

        Assert.assertNotNull(result);
    }

    @Test
    public void testStoreConsentAmendmentHistoryWithoutPassingCurrentConsent() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString(),
                Mockito.anyBoolean());

        ConsentHistoryResource consentHistoryResource =
                new ConsentHistoryResource();
        consentHistoryResource.setTimestamp(ConsentMgtServiceTestData.SAMPLE_CONSENT_AMENDMENT_TIMESTAMP);
        consentHistoryResource.setReason(ConsentMgtServiceTestData.SAMPLE_AMENDMENT_REASON);
        consentHistoryResource.setDetailedConsentResource(ConsentMgtServiceTestData
                .getSampleDetailedStoredTestCurrentConsentResource());

        boolean result = consentCoreServiceImpl.storeConsentAmendmentHistory(
                ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                consentHistoryResource, null);

        Assert.assertNotNull(result);
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

        ConsentHistoryResource consentHistoryResource = ConsentMgtServiceTestData.getSampleTestConsentHistoryResource();
        consentHistoryResource.setTimestamp(0);
        consentCoreServiceImpl.storeConsentAmendmentHistory(sampleID, consentHistoryResource,
                ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testStoreConsentAmendmentHistoryWithoutConsentAmendedReason() throws Exception {

        ConsentHistoryResource consentHistoryResource = ConsentMgtServiceTestData.getSampleTestConsentHistoryResource();
        consentHistoryResource.setReason(null);
        consentCoreServiceImpl.storeConsentAmendmentHistory(sampleID,
                consentHistoryResource,
                ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testStoreConsentAmendmentHistoryDataInsertError() throws Exception {

        Mockito.doThrow(OBConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentAmendmentHistory(Mockito.any(), Mockito.anyString(),
                Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(), Mockito.anyObject(), Mockito.anyString());

        consentCoreServiceImpl.storeConsentAmendmentHistory(sampleID,
                ConsentMgtServiceTestData.getSampleTestConsentHistoryResource(),
                ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testStoreConsentAmendmentHistoryDataRetrievalError() throws Exception {

        Mockito.doThrow(OBConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString(),
                Mockito.anyBoolean());

        consentCoreServiceImpl.storeConsentAmendmentHistory(sampleID,
                ConsentMgtServiceTestData.getSampleTestConsentHistoryResource(), null);
    }

    @Test ()
    public void testGetConsentAmendmentHistoryData() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentHistoryDataMap())
                .when(mockedConsentCoreDAO).retrieveConsentAmendmentHistory(Mockito.any(), any(ArrayList.class));
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString(),
                Mockito.anyBoolean());

        Map<String, ConsentHistoryResource>  consentAmendmentHistory =
                consentCoreServiceImpl.getConsentAmendmentHistoryData(sampleID);

        Assert.assertTrue(consentAmendmentHistory.containsKey(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
        Assert.assertNotNull(consentAmendmentHistory.get(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
    }

    @Test
    public void testGetConsentAmendmentHistoryDataWithOnlyBasicConsentData() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleConsentHistoryBasicConsentDataMap())
                .when(mockedConsentCoreDAO).retrieveConsentAmendmentHistory(Mockito.any(), any(ArrayList.class));
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString(),
                Mockito.anyBoolean());

        Map<String, ConsentHistoryResource>  consentAmendmentHistory =
                consentCoreServiceImpl.getConsentAmendmentHistoryData(sampleID);

        Assert.assertTrue(consentAmendmentHistory.containsKey(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
        Assert.assertNotNull(consentAmendmentHistory.get(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
    }

    @Test
    public void testGetConsentAmendmentHistoryDataWithOnlyConsentAttributesData() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleConsentHistoryConsentAttributesDataMap())
                .when(mockedConsentCoreDAO).retrieveConsentAmendmentHistory(Mockito.any(), any(ArrayList.class));
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString(),
                Mockito.anyBoolean());

        Map<String, ConsentHistoryResource>  consentAmendmentHistory =
                consentCoreServiceImpl.getConsentAmendmentHistoryData(sampleID);

        Assert.assertTrue(consentAmendmentHistory.containsKey(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
        Assert.assertNotNull(consentAmendmentHistory.get(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
    }

    @Test
    public void testGetConsentAmendmentHistoryDataWithOnlyConsentMappingsData() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleConsentHistoryConsentMappingsDataMap())
                .when(mockedConsentCoreDAO).retrieveConsentAmendmentHistory(Mockito.any(), any(ArrayList.class));
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString(),
                Mockito.anyBoolean());

        Map<String, ConsentHistoryResource>  consentAmendmentHistory =
                consentCoreServiceImpl.getConsentAmendmentHistoryData(sampleID);

        Assert.assertTrue(consentAmendmentHistory.containsKey(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
        Assert.assertNotNull(consentAmendmentHistory.get(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
    }

    @Test
    public void testGetConsentAmendmentHistoryDataWithNoConsentHistoryEntries() throws Exception {

        Mockito.doReturn(new HashMap<>())
                .when(mockedConsentCoreDAO).retrieveConsentAmendmentHistory(Mockito.any(), any(ArrayList.class));
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString(),
                Mockito.anyBoolean());

        Map<String, ConsentHistoryResource>  consentAmendmentHistory =
                consentCoreServiceImpl.getConsentAmendmentHistoryData(sampleID);

        Assert.assertEquals(0, consentAmendmentHistory.size());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAmendmentHistoryDataWithoutConsentID() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentHistoryDataMap())
                .when(mockedConsentCoreDAO).retrieveConsentAmendmentHistory(Mockito.any(), any(ArrayList.class));
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString(),
                Mockito.anyBoolean());

        consentCoreServiceImpl.getConsentAmendmentHistoryData(null);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAmendmentHistoryDataRetrieveError() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentHistoryDataMap())
                .when(mockedConsentCoreDAO).retrieveConsentAmendmentHistory(Mockito.any(), any(ArrayList.class));
        Mockito.doThrow(OBConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString(),
                Mockito.anyBoolean());

       consentCoreServiceImpl.getConsentAmendmentHistoryData(sampleID);
    }

    @Test
    public void testRevokeTokensByClientId() throws Exception {
        MockConsentCoreServiceImpl mockConsentCoreService = new MockConsentCoreServiceImpl();
        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        mockConsentCoreService.revokeTokens(
                retrievedDetailedConsentResource, ConsentMgtServiceTestData.SAMPLE_USER_ID);
    }

    @Test
    public void testRevokeTokensByClientIdError() {
        MockConsentCoreServiceImplTokenError mockedConsentCoreServiceImplTokenError =
                new MockConsentCoreServiceImplTokenError();
        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        try {
            mockedConsentCoreServiceImplTokenError.revokeTokens(
                    retrievedDetailedConsentResource, ConsentMgtServiceTestData.SAMPLE_USER_ID);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IdentityOAuth2Exception);
        }
    }

    @Test
    public void testSyncRetentionDatabaseWithPurgedConsent() throws Exception {

        DetailedConsentResource detailedConsent =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        ArrayList<String> consentIds = new ArrayList<>();
        ArrayList<ConsentStatusAuditRecord> consentStatusAuditRecords = new ArrayList<>();
        consentStatusAuditRecords.add(new ConsentStatusAuditRecord());
        consentIds.add(detailedConsent.getConsentID());

        Mockito.doReturn(consentIds).when(mockedConsentCoreDAO)
                .getListOfConsentIds(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(detailedConsent).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());
        Mockito.doReturn(null).when(mockedConsentCoreDAO)
                .getConsentFile(Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());
        Mockito.doReturn(consentStatusAuditRecords).when(mockedConsentCoreDAO)
                .getConsentStatusAuditRecordsByConsentId(Mockito.any(), any(ArrayList.class), Mockito.anyInt(),
                        Mockito.anyInt(), Mockito.anyBoolean());

        Mockito.doReturn(new AuthorizationResource()).when(mockedConsentCoreDAO)
                .storeAuthorizationResource(Mockito.any(), any(AuthorizationResource.class));
        Mockito.doReturn(true).when(mockedConsentCoreDAO)
                .storeConsentAttributes(Mockito.any(), any(ConsentAttributes.class));
        Mockito.doReturn(true).when(mockedConsentCoreDAO)
                .storeConsentFile(Mockito.any(), any(ConsentFile.class));
        Mockito.doReturn(new ConsentMappingResource()).when(mockedConsentCoreDAO)
                .storeConsentMappingResource(Mockito.any(), any(ConsentMappingResource.class));
        Mockito.doReturn(new ConsentStatusAuditRecord()).when(mockedConsentCoreDAO)
                .storeConsentStatusAuditRecord(Mockito.any(), any(ConsentStatusAuditRecord.class));
        Mockito.doReturn(new ConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(Mockito.any(), any(ConsentResource.class));
        Mockito.doReturn(true).when(mockedConsentCoreDAO)
                .deleteConsentData(Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());

        Assert.assertTrue(consentCoreServiceImpl.syncRetentionDatabaseWithPurgedConsent());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testSyncRetentionDatabaseWithPurgedConsentRetentionDisabled() throws Exception {

        OpenBankingConfigParser openBankingConfigParserMock = Mockito.mock(OpenBankingConfigParser.class);
        Mockito.when(openBankingConfigParserMock.isConsentDataRetentionEnabled()).thenReturn(false);
        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);
        consentCoreServiceImpl.syncRetentionDatabaseWithPurgedConsent();
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testSyncRetentionDatabaseWithPurgedConsentConsentListError() throws Exception {

        Mockito.doThrow(OBConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getListOfConsentIds(Mockito.any(), Mockito.anyBoolean());
        consentCoreServiceImpl.syncRetentionDatabaseWithPurgedConsent();
    }

    @Test
    public void testSyncRetentionDatabaseWithPurgedConsentFileGetError() throws Exception {

        DetailedConsentResource detailedConsent =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        ArrayList<String> consentIds = new ArrayList<>();
        consentIds.add(detailedConsent.getConsentID());

        Mockito.doReturn(consentIds).when(mockedConsentCoreDAO)
                .getListOfConsentIds(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(detailedConsent).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());
        Mockito.doThrow(OBConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentFile(Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());
        Mockito.doThrow(OBConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getConsentStatusAuditRecordsByConsentId(Mockito.any(), any(ArrayList.class), Mockito.anyInt(),
                        Mockito.anyInt(), Mockito.anyBoolean());

        Mockito.doReturn(new AuthorizationResource()).when(mockedConsentCoreDAO)
                .storeAuthorizationResource(Mockito.any(), any(AuthorizationResource.class));
        Mockito.doReturn(true).when(mockedConsentCoreDAO)
                .storeConsentAttributes(Mockito.any(), any(ConsentAttributes.class));
        Mockito.doReturn(true).when(mockedConsentCoreDAO)
                .storeConsentFile(Mockito.any(), any(ConsentFile.class));
        Mockito.doReturn(new ConsentMappingResource()).when(mockedConsentCoreDAO)
                .storeConsentMappingResource(Mockito.any(), any(ConsentMappingResource.class));
        Mockito.doReturn(new ConsentStatusAuditRecord()).when(mockedConsentCoreDAO)
                .storeConsentStatusAuditRecord(Mockito.any(), any(ConsentStatusAuditRecord.class));
        Mockito.doReturn(new ConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(Mockito.any(), any(ConsentResource.class));
        Mockito.doReturn(true).when(mockedConsentCoreDAO)
                .deleteConsentData(Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());
        Assert.assertTrue(consentCoreServiceImpl.syncRetentionDatabaseWithPurgedConsent());
    }

    @Test
    public void testSyncRetentionDatabaseWithPurgedConsentFileStoreError() throws Exception {

        DetailedConsentResource detailedConsent =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        ArrayList<String> consentIds = new ArrayList<>();
        ArrayList<ConsentStatusAuditRecord> consentStatusAuditRecords = new ArrayList<>();
        consentStatusAuditRecords.add(new ConsentStatusAuditRecord());
        consentIds.add(detailedConsent.getConsentID());

        Mockito.doReturn(consentIds).when(mockedConsentCoreDAO)
                .getListOfConsentIds(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(detailedConsent).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());
        Mockito.doReturn(new ConsentFile()).when(mockedConsentCoreDAO)
                .getConsentFile(Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());
        Mockito.doReturn(consentStatusAuditRecords).when(mockedConsentCoreDAO)
                .getConsentStatusAuditRecordsByConsentId(Mockito.any(), any(ArrayList.class), Mockito.anyInt(),
                        Mockito.anyInt(), Mockito.anyBoolean());

        Mockito.doReturn(new ConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(Mockito.any(), any(ConsentResource.class));
        Mockito.doReturn(new AuthorizationResource()).when(mockedConsentCoreDAO)
                .storeAuthorizationResource(Mockito.any(), any(AuthorizationResource.class));
        Mockito.doReturn(new ConsentMappingResource()).when(mockedConsentCoreDAO)
                .storeConsentMappingResource(Mockito.any(), any(ConsentMappingResource.class));
        Mockito.doReturn(true).when(mockedConsentCoreDAO)
                .storeConsentAttributes(Mockito.any(), any(ConsentAttributes.class));
        Mockito.doReturn(false).when(mockedConsentCoreDAO)
                .storeConsentFile(Mockito.any(), any(ConsentFile.class));

        Mockito.doReturn(true).when(mockedConsentCoreDAO)
                .deleteConsentData(Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());
        Assert.assertTrue(consentCoreServiceImpl.syncRetentionDatabaseWithPurgedConsent());
    }

    @Test
    public void testSyncRetentionDatabaseWithPurgedConsentAuditStoreError() throws Exception {

        DetailedConsentResource detailedConsent =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        ArrayList<String> consentIds = new ArrayList<>();
        ArrayList<ConsentStatusAuditRecord> consentStatusAuditRecords = new ArrayList<>();
        consentStatusAuditRecords.add(new ConsentStatusAuditRecord());
        consentIds.add(detailedConsent.getConsentID());

        Mockito.doReturn(consentIds).when(mockedConsentCoreDAO)
                .getListOfConsentIds(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(detailedConsent).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());
        Mockito.doReturn(new ConsentFile()).when(mockedConsentCoreDAO)
                .getConsentFile(Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());
        Mockito.doReturn(consentStatusAuditRecords).when(mockedConsentCoreDAO)
                .getConsentStatusAuditRecordsByConsentId(Mockito.any(), any(ArrayList.class), Mockito.anyInt(),
                        Mockito.anyInt(), Mockito.anyBoolean());

        Mockito.doReturn(new ConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(Mockito.any(), any(ConsentResource.class));
        Mockito.doReturn(new AuthorizationResource()).when(mockedConsentCoreDAO)
                .storeAuthorizationResource(Mockito.any(), any(AuthorizationResource.class));
        Mockito.doReturn(new ConsentMappingResource()).when(mockedConsentCoreDAO)
                .storeConsentMappingResource(Mockito.any(), any(ConsentMappingResource.class));
        Mockito.doReturn(true).when(mockedConsentCoreDAO)
                .storeConsentAttributes(Mockito.any(), any(ConsentAttributes.class));
        Mockito.doReturn(true).when(mockedConsentCoreDAO)
                .storeConsentFile(Mockito.any(), any(ConsentFile.class));
        Mockito.doReturn(null).when(mockedConsentCoreDAO)
                .storeConsentStatusAuditRecord(Mockito.any(), any(ConsentStatusAuditRecord.class));

        Mockito.doReturn(true).when(mockedConsentCoreDAO)
                .deleteConsentData(Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());
        Assert.assertTrue(consentCoreServiceImpl.syncRetentionDatabaseWithPurgedConsent());
    }

    @Test
    public void testSyncRetentionDatabaseWithPurgedConsentAttributeStoreError() throws Exception {

        DetailedConsentResource detailedConsent =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        ArrayList<String> consentIds = new ArrayList<>();
        ArrayList<ConsentStatusAuditRecord> consentStatusAuditRecords = new ArrayList<>();
        consentStatusAuditRecords.add(new ConsentStatusAuditRecord());
        consentIds.add(detailedConsent.getConsentID());

        Mockito.doReturn(consentIds).when(mockedConsentCoreDAO)
                .getListOfConsentIds(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(detailedConsent).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());
        Mockito.doReturn(new ConsentFile()).when(mockedConsentCoreDAO)
                .getConsentFile(Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());
        Mockito.doReturn(consentStatusAuditRecords).when(mockedConsentCoreDAO)
                .getConsentStatusAuditRecordsByConsentId(Mockito.any(), any(ArrayList.class), Mockito.anyInt(),
                        Mockito.anyInt(), Mockito.anyBoolean());

        Mockito.doReturn(new ConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(Mockito.any(), any(ConsentResource.class));
        Mockito.doReturn(new AuthorizationResource()).when(mockedConsentCoreDAO)
                .storeAuthorizationResource(Mockito.any(), any(AuthorizationResource.class));
        Mockito.doReturn(new ConsentMappingResource()).when(mockedConsentCoreDAO)
                .storeConsentMappingResource(Mockito.any(), any(ConsentMappingResource.class));
        Mockito.doReturn(false).when(mockedConsentCoreDAO)
                .storeConsentAttributes(Mockito.any(), any(ConsentAttributes.class));

        Assert.assertTrue(consentCoreServiceImpl.syncRetentionDatabaseWithPurgedConsent());
    }

    @Test
    public void testSyncRetentionDatabaseWithPurgedConsentMappingStoreError() throws Exception {

        DetailedConsentResource detailedConsent =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        ArrayList<String> consentIds = new ArrayList<>();
        ArrayList<ConsentStatusAuditRecord> consentStatusAuditRecords = new ArrayList<>();
        consentStatusAuditRecords.add(new ConsentStatusAuditRecord());
        consentIds.add(detailedConsent.getConsentID());

        Mockito.doReturn(consentIds).when(mockedConsentCoreDAO)
                .getListOfConsentIds(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(detailedConsent).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());
        Mockito.doReturn(new ConsentFile()).when(mockedConsentCoreDAO)
                .getConsentFile(Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());
        Mockito.doReturn(consentStatusAuditRecords).when(mockedConsentCoreDAO)
                .getConsentStatusAuditRecordsByConsentId(Mockito.any(), any(ArrayList.class), Mockito.anyInt(),
                        Mockito.anyInt(), Mockito.anyBoolean());

        Mockito.doReturn(new ConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(Mockito.any(), any(ConsentResource.class));
        Mockito.doReturn(new AuthorizationResource()).when(mockedConsentCoreDAO)
                .storeAuthorizationResource(Mockito.any(), any(AuthorizationResource.class));
        Mockito.doReturn(null).when(mockedConsentCoreDAO)
                .storeConsentMappingResource(Mockito.any(), any(ConsentMappingResource.class));

        Mockito.doReturn(true).when(mockedConsentCoreDAO)
                .deleteConsentData(Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());
        Assert.assertTrue(consentCoreServiceImpl.syncRetentionDatabaseWithPurgedConsent());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentFileError() throws Exception {
        OpenBankingConfigParser openBankingConfigParserMock = Mockito.mock(OpenBankingConfigParser.class);
        Mockito.when(openBankingConfigParserMock.isConsentDataRetentionEnabled()).thenReturn(false);
        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);
        consentCoreServiceImpl.getConsentFile("3d22259e-942c-46b8-8f75-a608c677a6e6", true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentFileErrorRetentionData() throws Exception {
        consentCoreServiceImpl.getConsentFile("", true);
    }

    @Test
    public void testGetConsentFileRetentionDataSuccess() throws Exception {

        Mockito.doReturn(new ConsentFile()).when(mockedConsentCoreDAO)
                .getConsentFile(Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());
        ConsentFile consentFile = consentCoreServiceImpl.getConsentFile("3d22259e-942c-46b8-8f75-a608c677a6e6",
                true);
        Assert.assertNotNull(consentFile);
    }

    @Test
    public void testGetConsentFileConsentData() throws Exception {

        Mockito.doReturn(new ConsentFile()).when(mockedConsentCoreDAO)
                .getConsentFile(Mockito.any(), Mockito.anyString(), Mockito.anyBoolean());
        ConsentFile consentFile = consentCoreServiceImpl.getConsentFile("3d22259e-942c-46b8-8f75-a608c677a6e6",
                false);
        Assert.assertNotNull(consentFile);
    }

    @Test
    public void testGetConsentStatusAuditRecords() throws Exception {

        ArrayList<ConsentStatusAuditRecord> consentStatusAuditRecords = new ArrayList<>();
        ArrayList<String> consentIds = new ArrayList<>();

        Mockito.doReturn(consentStatusAuditRecords).when(mockedConsentCoreDAO)
                .getConsentStatusAuditRecordsByConsentId(Mockito.any(), any(ArrayList.class), Mockito.anyInt(),
                        Mockito.anyInt(), Mockito.anyBoolean());
        ArrayList<ConsentStatusAuditRecord> statusAuditRecords =
                consentCoreServiceImpl.getConsentStatusAuditRecords(consentIds, null, null, false);
        Assert.assertNotNull(statusAuditRecords);
    }

    @Test
    public void testGetConsentStatusAuditRecordsInRetention() throws Exception {

        ArrayList<ConsentStatusAuditRecord> consentStatusAuditRecords = new ArrayList<>();
        ArrayList<String> consentIds = new ArrayList<>();

        Mockito.doReturn(consentStatusAuditRecords).when(mockedConsentCoreDAO)
                .getConsentStatusAuditRecordsByConsentId(Mockito.any(), any(ArrayList.class), Mockito.anyInt(),
                        Mockito.anyInt(), Mockito.anyBoolean());
        ArrayList<ConsentStatusAuditRecord> statusAuditRecords =
                consentCoreServiceImpl.getConsentStatusAuditRecords(consentIds, null, null, true);
        Assert.assertNotNull(statusAuditRecords);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentStatusAuditRecordsInRetentionError() throws Exception {
        ArrayList<String> consentIds = new ArrayList<>();
        OpenBankingConfigParser openBankingConfigParserMock = Mockito.mock(OpenBankingConfigParser.class);
        Mockito.when(openBankingConfigParserMock.isConsentDataRetentionEnabled()).thenReturn(false);
        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);
        consentCoreServiceImpl.getConsentStatusAuditRecords(consentIds, null, null, true);
    }
}

class MockConsentCoreServiceImpl extends ConsentCoreServiceImpl {

    @Override
    OAuth2Service getOAuth2Service() {

        return Mockito.mock(OAuth2Service.class);
    }

    @Override
    AuthenticatedUser getAuthenticatedUser(String userID) {

        return Mockito.mock(AuthenticatedUser.class);
    }

    @Override
    Set<AccessTokenDO> getAccessTokenDOSet(DetailedConsentResource detailedConsentResource,
                                           AuthenticatedUser authenticatedUser) {

        String[] scopes = {"OB_CONSENT_ID" + detailedConsentResource.getConsentID()};
        AccessTokenDO sampleAccessTokenDO = new AccessTokenDO();
        sampleAccessTokenDO.setScope(scopes);
        sampleAccessTokenDO.setAccessToken("sample_token");

        Set<AccessTokenDO> accessTokenDOS = new HashSet<>();
        accessTokenDOS.add(sampleAccessTokenDO);

        return accessTokenDOS;
    }

    @Override
    OAuthRevocationResponseDTO revokeTokenByClient(OAuth2Service oAuth2Service,
                                                   OAuthRevocationRequestDTO revocationRequestDTO) {

        return Mockito.mock(OAuthRevocationResponseDTO.class);
    }
}

class MockConsentCoreServiceImplTokenError extends ConsentCoreServiceImpl {

    @Override
    OAuth2Service getOAuth2Service() {

        return Mockito.mock(OAuth2Service.class);
    }

    @Override
    AuthenticatedUser getAuthenticatedUser(String userID) {

        return Mockito.mock(AuthenticatedUser.class);
    }

    @Override
    Set<AccessTokenDO> getAccessTokenDOSet(DetailedConsentResource detailedConsentResource,
                                           AuthenticatedUser authenticatedUser) {

        String[] scopes = {"OB_CONSENT_ID" + detailedConsentResource.getConsentID()};
        AccessTokenDO sampleAccessTokenDO = new AccessTokenDO();
        sampleAccessTokenDO.setScope(scopes);
        sampleAccessTokenDO.setAccessToken("sample_token");

        Set<AccessTokenDO> accessTokenDOS = new HashSet<>();
        accessTokenDOS.add(sampleAccessTokenDO);

        return accessTokenDOS;
    }

    @Override
    OAuthRevocationResponseDTO revokeTokenByClient(OAuth2Service oAuth2Service,
                                                   OAuthRevocationRequestDTO revocationRequestDTO) {

        OAuthRevocationResponseDTO errorResponse = new OAuthRevocationResponseDTO();
        errorResponse.setError(true);
        errorResponse.setErrorMsg("Error occurred while revoking authorization grant for applications");
        return errorResponse;
    }
}
