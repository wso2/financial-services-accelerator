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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.admin;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentStatusAuditRecord;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.impl.DefaultConsentAdminHandler;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.model.ConsentAdminData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.util.TestConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.util.TestUtil;
import org.wso2.financial.services.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

/**
 * Default Consent admin handler test.
 */
public class DefaultConsentAdminHandlerTest {

    private DefaultConsentAdminHandler adminHandler = new DefaultConsentAdminHandler();
    private ConsentCoreServiceImpl consentCoreServiceImpl;
    private MockedStatic<ConsentExtensionsDataHolder> consentExtensionsDataHolder;

    @BeforeClass
    public void initTest() throws ConsentManagementException {
        consentCoreServiceImpl = mock(ConsentCoreServiceImpl.class);
        consentExtensionsDataHolder = mockStatic(ConsentExtensionsDataHolder.class);

        ConsentExtensionsDataHolder dataHolderMock = mock(ConsentExtensionsDataHolder.class);
        doReturn(consentCoreServiceImpl).when(dataHolderMock).getConsentCoreService();
        consentExtensionsDataHolder.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);

        ArrayList<DetailedConsentResource> resources = new ArrayList<>();
        resources.add(TestUtil.getSampleDetailedConsentResource());
        doReturn(resources).when(consentCoreServiceImpl).searchDetailedConsents(any(ArrayList.class),
                any(ArrayList.class), any(ArrayList.class), any(ArrayList.class), any(ArrayList.class),
                anyLong(), anyLong(), anyInt(), anyInt());

        doReturn(TestUtil.getSampleConsentResource(TestConstants.AUTHORISED_STATUS)).when(consentCoreServiceImpl)
                .getConsent(anyString(), anyBoolean());
        doReturn(TestUtil.getSampleDetailedConsentResource()).when(consentCoreServiceImpl)
                .getDetailedConsent(anyString());
        doReturn(true).when(consentCoreServiceImpl).revokeConsentWithReason(anyString(), anyString(),
                anyString(), anyString());
        doReturn(Map.of("HistoryId", TestUtil.getSampleConsentHistoryResource())).when(consentCoreServiceImpl)
                .getConsentAmendmentHistoryData(anyString());
        ArrayList<ConsentStatusAuditRecord> auditRecords = new ArrayList<>();
        auditRecords.add(TestUtil.getSampleConsentStatusAuditRecord(TestConstants.SAMPLE_CONSENT_ID,
                TestConstants.AUTHORISED_STATUS));
        doReturn(auditRecords).when(consentCoreServiceImpl).getConsentStatusAuditRecords(any(ArrayList.class),
                anyInt(), anyInt());
        doReturn(TestUtil.getSampleConsentFileObject(TestConstants.SAMPLE_CONSENT_FILE)).when(consentCoreServiceImpl)
                .getConsentFile(anyString());
    }

    @AfterClass
    public void tearDown() {
        // Closing the mockStatic after each test
        consentExtensionsDataHolder.close();
    }

    @Test
    public void testHandleSearch() {

        ConsentAdminData consentAdminDataMock = mock(ConsentAdminData.class);
        doReturn(getQueryParams()).when(consentAdminDataMock).getQueryParams();

        adminHandler.handleSearch(consentAdminDataMock);
        verify(consentAdminDataMock).setResponseStatus(ResponseStatus.OK);
    }

    @Test
    public void testHandleRevoke() {

        ConsentAdminData consentAdminDataMock = mock(ConsentAdminData.class);
        doReturn(getQueryParams()).when(consentAdminDataMock).getQueryParams();

       // adminHandler.handleRevoke(consentAdminDataMock);
       // verify(consentAdminDataMock).setResponseStatus(ResponseStatus.NO_CONTENT);
    }

    @Test
    public void testHandleConsentAmendmentHistoryRetrieval() {

        ConsentAdminData consentAdminDataMock = mock(ConsentAdminData.class);
        doReturn(getQueryParams()).when(consentAdminDataMock).getQueryParams();

        adminHandler.handleConsentAmendmentHistoryRetrieval(consentAdminDataMock);
        verify(consentAdminDataMock).setResponseStatus(ResponseStatus.OK);
    }

    @Test
    public void testHandleConsentStatusAuditSearch() {

        ConsentAdminData consentAdminDataMock = mock(ConsentAdminData.class);
        doReturn(getQueryParams()).when(consentAdminDataMock).getQueryParams();

        adminHandler.handleConsentStatusAuditSearch(consentAdminDataMock);
        verify(consentAdminDataMock).setResponseStatus(ResponseStatus.OK);
    }

    @Test
    public void testHandleConsentFileSearch() {

        ConsentAdminData consentAdminDataMock = mock(ConsentAdminData.class);
        doReturn(getQueryParams()).when(consentAdminDataMock).getQueryParams();

        adminHandler.handleConsentFileSearch(consentAdminDataMock);
        verify(consentAdminDataMock).setResponseStatus(ResponseStatus.OK);
    }

    private Map getQueryParams() {
        Map queryParams = new HashMap();
        queryParams.put(ConsentExtensionConstants.CC_CONSENT_ID, new ArrayList<>(Collections
                .singletonList(TestConstants.SAMPLE_CONSENT_ID)));
        queryParams.put(ConsentExtensionConstants.CONSENT_IDS, new ArrayList<>(Collections
                .singletonList(TestConstants.SAMPLE_CONSENT_ID)));
        queryParams.put(ConsentExtensionConstants.CLIENT_IDS, new ArrayList<>(Collections
                .singletonList(TestConstants.SAMPLE_CLIENT_ID)));
        queryParams.put(ConsentExtensionConstants.CONSENT_TYPES, new ArrayList<>(Collections
                .singletonList(TestConstants.ACCOUNTS)));
        queryParams.put(ConsentExtensionConstants.CONSENT_STATUSES, new ArrayList<>(Collections
                .singletonList(TestConstants.AUTHORISED_STATUS)));
        queryParams.put(ConsentExtensionConstants.USER_IDS, new ArrayList<>(Collections
                .singletonList(TestConstants.SAMPLE_USER_ID)));

        return queryParams;
    }
}
