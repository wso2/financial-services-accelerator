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
package org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.StatusEnum;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.impl.ExternalAPIConsentPersistStep;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.AmendedResources;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentPersistData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ExternalAPIPreConsentPersistResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ExternalAPIUtil;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.model.ExternalAPIConsentResourceResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class ExternalAPIConsentPersistStepTest {

    private static class MockContext {
        MockedStatic<FinancialServicesConfigParser> configStatic;
        MockedStatic<ConsentExtensionsDataHolder> dataHolderStatic;
    }

    private static ConsentData createConsentData(String consentId, String userId, boolean withAuthResource) throws
            Exception {
        ConsentData data = new ConsentData("sessionKey", userId, "req", "scope", "myApp", Map.of());
        data.setConsentId(consentId);
        data.setRedirectURI(new URI("https://localhost/redirect"));
        data.setState("xyz");
        if (withAuthResource) {
            AuthorizationResource auth = new AuthorizationResource();
            auth.setAuthorizationID("auth-123");
            auth.setUserID(userId);
            data.setAuthResource(auth);
        }
        return data;
    }

    private static ConsentPersistData createPersistData(ConsentData consentData) {
        JSONObject payload = new JSONObject().put("foo", "bar");
        ConsentPersistData persistData = new ConsentPersistData(payload, new HashMap<>(), true, consentData);
        persistData.setBrowserCookies(Map.of("commonAuthId", "auth-cookie-id"));
        return persistData;
    }

    private MockContext mockPreInitiatedMode(boolean preInitiated, FinancialServicesConfigParser configMock,
                                             ConsentCoreService consentCoreService) {
        MockContext context = new MockContext();

        context.configStatic = mockStatic(FinancialServicesConfigParser.class);
        context.configStatic.when(FinancialServicesConfigParser::getInstance).thenReturn(configMock);
        when(configMock.isPreInitiatedConsent()).thenReturn(preInitiated);

        context.dataHolderStatic = mockStatic(ConsentExtensionsDataHolder.class);
        ConsentExtensionsDataHolder dataHolder = mock(ConsentExtensionsDataHolder.class);
        context.dataHolderStatic.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolder);
        when(dataHolder.getConsentCoreService()).thenReturn(consentCoreService);

        return context;
    }

    @Test
    public void testExecute_withPreInitiatedConsent_success() throws Exception {
        FinancialServicesConfigParser configMock = mock(FinancialServicesConfigParser.class);
        ConsentCoreService consentCoreService = mock(ConsentCoreService.class);
        MockContext mocks = mockPreInitiatedMode(true, configMock, consentCoreService);

        ConsentData consentData = createConsentData("cid-123", "user1", true);
        ConsentPersistData persistData = createPersistData(consentData);

        DetailedConsentResource detailed = new DetailedConsentResource();
        detailed.setConsentID("cid-123");

        when(consentCoreService.getDetailedConsent("cid-123")).thenReturn(detailed);
        when(consentCoreService.searchAuthorizations("cid-123"))
                .thenReturn(new ArrayList<>(List.of(consentData.getAuthResource())));

        ExternalAPIPreConsentPersistResponseDTO dto = new ExternalAPIPreConsentPersistResponseDTO();
        dto.setConsentResource(new ExternalAPIConsentResourceResponseDTO());

        try (MockedStatic<ServiceExtensionUtils> staticMock = mockStatic(ServiceExtensionUtils.class);
             MockedStatic<ExternalAPIUtil> utilStatic = mockStatic(ExternalAPIUtil.class)) {

            ExternalServiceResponse response = new ExternalServiceResponse();
            response.setStatus(StatusEnum.SUCCESS);
            response.setData(new ObjectMapper().valueToTree(dto));

            staticMock.when(() -> ServiceExtensionUtils.invokeExternalServiceCall(any(), any()))
                    .thenReturn(response);

            utilStatic.when(() -> ExternalAPIUtil.constructDetailedConsentResource(any(), any(), any(), any()))
                    .thenReturn(detailed);

            ExternalAPIConsentPersistStep persistStep = new ExternalAPIConsentPersistStep();
            persistStep.execute(persistData);

            ArgumentCaptor<DetailedConsentResource> captor = ArgumentCaptor.forClass(DetailedConsentResource.class);
            verify(consentCoreService).updateConsentAndCreateAuthResources(captor.capture(), eq("user1"));
            assertEquals(captor.getValue().getConsentID(), "cid-123");
        } finally {
            mocks.configStatic.close();
            mocks.dataHolderStatic.close();
        }
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testExecute_nullConsentData_shouldThrowException() throws Exception {
        JSONObject payload = new JSONObject().put("foo", "bar");
        ConsentPersistData persistData = new ConsentPersistData(payload, new HashMap<>(), true, null);

        FinancialServicesConfigParser configMock = mock(FinancialServicesConfigParser.class);
        ConsentCoreService consentCoreService = mock(ConsentCoreService.class);
        MockContext mocks = mockPreInitiatedMode(true, configMock, consentCoreService);

        try {
            new ExternalAPIConsentPersistStep().execute(persistData);
        } finally {
            mocks.configStatic.close();
            mocks.dataHolderStatic.close();
        }
    }

    @Test
    public void testExecute_nonPreInitiated_withConsentId_shouldPersist() throws Exception {
        FinancialServicesConfigParser configMock = mock(FinancialServicesConfigParser.class);
        ConsentCoreService consentCoreService = mock(ConsentCoreService.class);
        MockContext mocks = mockPreInitiatedMode(false, configMock, consentCoreService);

        ConsentData consentData = createConsentData("cid-nonpre", "userZ", false);
        ConsentPersistData persistData = createPersistData(consentData);

        DetailedConsentResource dummyConsent = new DetailedConsentResource();
        dummyConsent.setConsentID("cid-nonpre");
        when(consentCoreService.getDetailedConsent("cid-nonpre")).thenReturn(dummyConsent);

        ExternalAPIConsentResourceResponseDTO responseConsent = new ExternalAPIConsentResourceResponseDTO();
        ExternalAPIPreConsentPersistResponseDTO dto = new ExternalAPIPreConsentPersistResponseDTO();
        dto.setConsentResource(responseConsent);

        try (MockedStatic<ServiceExtensionUtils> staticMock = mockStatic(ServiceExtensionUtils.class);
             MockedStatic<ExternalAPIUtil> utilStatic = mockStatic(ExternalAPIUtil.class)) {

            ExternalServiceResponse response = new ExternalServiceResponse();
            response.setStatus(StatusEnum.SUCCESS);
            response.setData(new ObjectMapper().valueToTree(dto));

            staticMock.when(() -> ServiceExtensionUtils.invokeExternalServiceCall(any(), any()))
                    .thenReturn(response);

            DetailedConsentResource constructed = new DetailedConsentResource();
            constructed.setConsentID("cid-nonpre");
            utilStatic.when(() -> ExternalAPIUtil.constructDetailedConsentResource(any(), eq(consentData)))
                    .thenReturn(constructed);

            new ExternalAPIConsentPersistStep().execute(persistData);
            verify(consentCoreService).storeDetailedConsentResource(constructed);
        } finally {
            mocks.configStatic.close();
            mocks.dataHolderStatic.close();
        }
    }

    @Test
    public void testExecute_nonPreInitiated_withoutConsentId_shouldGenerateAndPersist() throws Exception {
        FinancialServicesConfigParser configMock = mock(FinancialServicesConfigParser.class);
        ConsentCoreService consentCoreService = mock(ConsentCoreService.class);
        MockContext mocks = mockPreInitiatedMode(false, configMock, consentCoreService);

        ConsentData consentData = new ConsentData("sessionKey", "user2", "req", "scope", "appX", new HashMap<>());
        consentData.setRedirectURI(new URI("https://localhost/return"));
        consentData.setState("state-x");
        ConsentPersistData persistData = createPersistData(consentData);

        ExternalAPIConsentResourceResponseDTO responseConsent = new ExternalAPIConsentResourceResponseDTO();
        ExternalAPIPreConsentPersistResponseDTO dto = new ExternalAPIPreConsentPersistResponseDTO();
        dto.setConsentResource(responseConsent);

        try (MockedStatic<ServiceExtensionUtils> staticMock = mockStatic(ServiceExtensionUtils.class);
             MockedStatic<ExternalAPIUtil> utilStatic = mockStatic(ExternalAPIUtil.class)) {

            ExternalServiceResponse response = new ExternalServiceResponse();
            response.setStatus(StatusEnum.SUCCESS);
            response.setData(new ObjectMapper().valueToTree(dto));

            staticMock.when(() -> ServiceExtensionUtils.invokeExternalServiceCall(any(), any()))
                    .thenReturn(response);

            DetailedConsentResource constructed = new DetailedConsentResource();
            constructed.setConsentID("generated-id-123");
            utilStatic.when(() -> ExternalAPIUtil.constructDetailedConsentResource(any(), eq(consentData)))
                    .thenReturn(constructed);

            new ExternalAPIConsentPersistStep().execute(persistData);

            assert consentData.getConsentId() != null;
            assertEquals(consentData.getMetaDataMap().get("commonAuthId"), "auth-cookie-id");

            verify(consentCoreService).storeDetailedConsentResource(constructed);
        } finally {
            mocks.configStatic.close();
            mocks.dataHolderStatic.close();
        }
    }

    @Test
    public void testExecute_withAmendments_shouldPersistAmendedResources() throws Exception {
        FinancialServicesConfigParser configMock = mock(FinancialServicesConfigParser.class);
        ConsentCoreService consentCoreService = mock(ConsentCoreService.class);
        MockContext mocks = mockPreInitiatedMode(true, configMock, consentCoreService);

        ConsentData consentData = createConsentData("cid-amend", "userA", true);
        ConsentPersistData persistData = createPersistData(consentData);

        DetailedConsentResource detailed = new DetailedConsentResource();
        detailed.setConsentID("cid-amend");

        when(consentCoreService.getDetailedConsent("cid-amend")).thenReturn(detailed);
        when(consentCoreService.searchAuthorizations("cid-amend"))
                .thenReturn(new ArrayList<>(List.of(consentData.getAuthResource())));

        // Setup amendments
        ExternalAPIConsentResourceResponseDTO.AmendedAuthorization amendedAuth =
                new ExternalAPIConsentResourceResponseDTO.AmendedAuthorization();
        List<ExternalAPIConsentResourceResponseDTO.AmendedAuthorization> amendments = List.of(amendedAuth);
        ExternalAPIConsentResourceResponseDTO responseConsent = new ExternalAPIConsentResourceResponseDTO();
        responseConsent.setAmendments(amendments);

        ExternalAPIPreConsentPersistResponseDTO dto = new ExternalAPIPreConsentPersistResponseDTO();
        dto.setConsentResource(responseConsent);

        try (MockedStatic<ServiceExtensionUtils> staticMock = mockStatic(ServiceExtensionUtils.class);
             MockedStatic<ExternalAPIUtil> utilStatic = mockStatic(ExternalAPIUtil.class)) {

            ExternalServiceResponse response = new ExternalServiceResponse();
            response.setStatus(StatusEnum.SUCCESS);
            response.setData(new ObjectMapper().valueToTree(dto));

            staticMock.when(() -> ServiceExtensionUtils.invokeExternalServiceCall(any(), any()))
                    .thenReturn(response);

            utilStatic.when(() -> ExternalAPIUtil.constructDetailedConsentResource(any(), any(), any(), any()))
                    .thenReturn(detailed);

            // Mock amended resource handling
            AmendedResources amendedResources = mock(AmendedResources.class);
            when(amendedResources.getAmendedAuthResources()).thenReturn(List.of());
            when(amendedResources.getNewMappingResources()).thenReturn(List.of());
            when(amendedResources.getAmendedMappingResources()).thenReturn(List.of());

            utilStatic.when(() -> ExternalAPIUtil.constructAmendedResources(any())).thenReturn(amendedResources);

            ExternalAPIConsentPersistStep persistStep = new ExternalAPIConsentPersistStep();
            persistStep.execute(persistData);

            // Verify amended resources are persisted
            verify(consentCoreService).updateAuthorizationResources(List.of());
            verify(consentCoreService).createConsentMappingResources(List.of());
            verify(consentCoreService).updateConsentMappingResources(List.of());

        } finally {
            mocks.configStatic.close();
            mocks.dataHolderStatic.close();
        }
    }

}
