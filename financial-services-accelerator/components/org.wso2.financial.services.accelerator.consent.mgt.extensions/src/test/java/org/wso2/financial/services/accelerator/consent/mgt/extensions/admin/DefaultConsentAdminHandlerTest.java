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

import org.json.JSONObject;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentStatusAuditRecord;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.builder.ConsentAdminBuilder;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.impl.DefaultConsentAdminHandler;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.model.ConsentAdminData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.model.ExternalAPIAdminConsentRevokeRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.model.ExternalAPIAdminConsentRevokeResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.model.ExternalAPIAdminConsentSearchRequestDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.model.ExternalAPIAdminConsentSearchResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.utils.ExternalAPIConsentAdminUtils;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionExporter;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.util.TestConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.util.TestUtil;
import org.wso2.financial.services.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;

import java.lang.reflect.Field;
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

    DefaultConsentAdminHandler defaultConsentAdminHandler;
    private DefaultConsentAdminHandler adminHandler;
    private MockedStatic<ConsentExtensionsDataHolder> consentExtensionsDataHolder;

    @Mock
    ConsentAdminData consentAdminDataMock;
    @Mock
    ConsentCoreServiceImpl consentCoreServiceMock;
    private static Map<String, String> headers;
    private static Map<String, Object> configs;

    private MockedStatic<FinancialServicesConfigParser> configParser;


    @BeforeClass
    public void initTest() throws ConsentManagementException {

        consentAdminDataMock = mock(ConsentAdminData.class);
        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
        consentExtensionsDataHolder = mockStatic(ConsentExtensionsDataHolder.class);
        configParser = mockStatic(FinancialServicesConfigParser.class);

        ConsentExtensionsDataHolder dataHolderMock = mock(ConsentExtensionsDataHolder.class);
        doReturn(consentCoreServiceMock).when(dataHolderMock).getConsentCoreService();
        consentExtensionsDataHolder.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);


        ArrayList<DetailedConsentResource> resources = new ArrayList<>();
        resources.add(TestUtil.getSampleDetailedConsentResource());
        doReturn(resources).when(consentCoreServiceMock).searchDetailedConsents(any(ArrayList.class),
                any(ArrayList.class), any(ArrayList.class), any(ArrayList.class), any(ArrayList.class),
                anyLong(), anyLong(), anyInt(), anyInt());
        doReturn(TestUtil.getSampleConsentResource(TestConstants.AUTHORISED_STATUS)).when(consentCoreServiceMock)
                .getConsent(anyString(), anyBoolean());
        doReturn(TestUtil.getSampleAuthorizedDetailedConsentResource()).when(consentCoreServiceMock)
                .getDetailedConsent(anyString());
        doReturn(true).when(consentCoreServiceMock).revokeConsentWithReason(anyString(), anyString(),
                anyString(), anyString());
        doReturn(Map.of("HistoryId", TestUtil.getSampleConsentHistoryResource())).when(consentCoreServiceMock)
                .getConsentAmendmentHistoryData(anyString());
        ArrayList<ConsentStatusAuditRecord> auditRecords = new ArrayList<>();
        auditRecords.add(TestUtil.getSampleConsentStatusAuditRecord(TestConstants.SAMPLE_CONSENT_ID,
                TestConstants.AUTHORISED_STATUS));
        doReturn(auditRecords).when(consentCoreServiceMock).getConsentStatusAuditRecords(any(ArrayList.class),
                anyInt(), anyInt());
        doReturn(TestUtil.getSampleConsentFileObject(TestConstants.SAMPLE_CONSENT_FILE)).when(consentCoreServiceMock)
                .getConsentFile(anyString());

        configs = new HashMap<String, Object>();
        configs.put(FinancialServicesConstants.MAX_INSTRUCTED_AMOUNT, "1000");

        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(configs).when(configParserMock).getConfiguration();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        headers = new HashMap<>();

        ConsentAdminBuilder consentAdminBuilder = Mockito.mock(ConsentAdminBuilder.class);
        Mockito.when(consentAdminBuilder.getConsentAdminHandler()).thenReturn(defaultConsentAdminHandler);
        ConsentExtensionExporter.setConsentAdminBuilder(consentAdminBuilder);

        defaultConsentAdminHandler = new DefaultConsentAdminHandler();
        adminHandler = new DefaultConsentAdminHandler();
        setPrivateBoolean(adminHandler, "isExtensionsEnabled", true);
        setPrivateBoolean(adminHandler, "isExternalPreConsentRevocationEnabled", true);
        setPrivateBoolean(adminHandler, "isExternalEnrichConsentSearchResponseEnabled", true);

    }

    @AfterClass
    public void tearDown() {
        // Closing the mockStatic after each test
        consentExtensionsDataHolder.close();
        configParser.close();
    }

    @Test
    public void testHandleSearch() {
        ConsentAdminData consentAdminDataMock = mock(ConsentAdminData.class);
        doReturn(getQueryParams()).when(consentAdminDataMock).getQueryParams();
        defaultConsentAdminHandler.handleSearch(consentAdminDataMock);
        verify(consentAdminDataMock).setResponseStatus(ResponseStatus.OK);
    }

    @Test
    public void testHandleSearchWithExtensionsEnabled() {
        setConsentAdminBuilder();
        JSONObject payload = new JSONObject();
        doReturn(payload).when(consentAdminDataMock).getPayload();
        doReturn(getQueryParams()).when(consentAdminDataMock).getQueryParams();

        try (MockedStatic<ExternalAPIConsentAdminUtils> mockedStatic =
                     mockStatic(ExternalAPIConsentAdminUtils.class)) {
            ExternalAPIAdminConsentSearchResponseDTO mockResponse = new ExternalAPIAdminConsentSearchResponseDTO();

            mockedStatic.when(() ->
                    ExternalAPIConsentAdminUtils.callExternalService(any(ExternalAPIAdminConsentSearchRequestDTO.class))
            ).thenReturn(mockResponse);

            adminHandler.handleSearch(consentAdminDataMock);
        }
    }

    @Test
    public void testHandleRevoke() {
        ConsentAdminData consentAdminDataMock = mock(ConsentAdminData.class);
        doReturn(getQueryParams()).when(consentAdminDataMock).getQueryParams();
        defaultConsentAdminHandler.handleRevoke(consentAdminDataMock);
        verify(consentAdminDataMock).setResponseStatus(ResponseStatus.NO_CONTENT);
    }

    @Test
    public void testHandleRevokeWithExtensionsEnabled() {
        setConsentAdminBuilder();
        ConsentAdminData consentAdminDataMock = mock(ConsentAdminData.class);
        JSONObject payload = new JSONObject();
        doReturn(payload).when(consentAdminDataMock).getPayload();
        doReturn(TestConstants.ACCOUNT_CONSENT_GET_PATH).when(consentAdminDataMock).getAbsolutePath();

        doReturn(getQueryParams()).when(consentAdminDataMock).getQueryParams();

        try (MockedStatic<ExternalAPIConsentAdminUtils> mockedStatic =
                     mockStatic(ExternalAPIConsentAdminUtils.class)) {
            ExternalAPIAdminConsentRevokeResponseDTO mockResponse = new ExternalAPIAdminConsentRevokeResponseDTO();

            mockedStatic.when(() ->
                    ExternalAPIConsentAdminUtils.callExternalService(any(ExternalAPIAdminConsentRevokeRequestDTO.class))
            ).thenReturn(mockResponse);

            adminHandler.handleRevoke(consentAdminDataMock);
        }


    }

    @Test
    public void testHandleConsentAmendmentHistoryRetrieval() {
        setConsentAdminBuilder();
        ConsentAdminData consentAdminDataMock = mock(ConsentAdminData.class);
        doReturn(getQueryParams()).when(consentAdminDataMock).getQueryParams();

        defaultConsentAdminHandler.handleConsentAmendmentHistoryRetrieval(consentAdminDataMock);
        verify(consentAdminDataMock).setResponseStatus(ResponseStatus.OK);
    }

    @Test
    public void testHandleConsentStatusAuditSearch() {
        ConsentAdminData consentAdminDataMock = mock(ConsentAdminData.class);
        doReturn(getQueryParams()).when(consentAdminDataMock).getQueryParams();

        defaultConsentAdminHandler.handleConsentStatusAuditSearch(consentAdminDataMock);
        verify(consentAdminDataMock).setResponseStatus(ResponseStatus.OK);
    }

    @Test
    public void testHandleConsentFileSearch() {
        ConsentAdminData consentAdminDataMock = mock(ConsentAdminData.class);
        doReturn(getQueryParams()).when(consentAdminDataMock).getQueryParams();

        defaultConsentAdminHandler.handleConsentFileSearch(consentAdminDataMock);
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

    private void setPrivateBoolean(Object target, String fieldName, boolean value) {

        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.setBoolean(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set flag: " + fieldName, e);
        }
    }

    private static void setConsentAdminBuilder() {

        ConsentAdminBuilder consentAdminBuilder = Mockito.mock(ConsentAdminBuilder.class);
        ConsentExtensionExporter.setConsentAdminBuilder(consentAdminBuilder);
    }
}
