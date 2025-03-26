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
package org.wso2.financial.services.accelerator.consent.mgt.extensions.manage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.extension.model.StatusEnum;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentAttributes;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.impl.ExternalAPIConsentManageHandler;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ConsentManageData;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExternalAPIConsentManageHandlerTest {

    private ExternalAPIConsentManageHandler handler;
    private ConsentCoreService consentCoreServiceMock;
    private ConsentManageData consentManageData;
    private static MockedStatic<FinancialServicesConfigParser> configParser;
    private MockedStatic<ConsentExtensionsDataHolder> staticDataHolderMock;
    private MockedStatic<ServiceExtensionUtils> serviceUtilsMockedStatic;
    private ObjectMapper mapper;

    @BeforeClass
    public void setUp() {

        // Mock configurations
        configParser = Mockito.mockStatic(FinancialServicesConfigParser.class);
        FinancialServicesConfigParser configParserMock = Mockito.mock(FinancialServicesConfigParser.class);
        Map<String, Object> configs = new HashMap<>();
        Mockito.doReturn(configs).when(configParserMock).getConfiguration();
        Mockito.doReturn(FinancialServicesConstants.BASIC_AUTH).when(configParserMock)
                .getServiceExtensionsEndpointSecurityType();
        Mockito.doReturn("test").when(configParserMock)
                .getServiceExtensionsEndpointSecurityBasicAuthUsername();
        Mockito.doReturn("test").when(configParserMock)
                .getServiceExtensionsEndpointSecurityBasicAuthPassword();
        Mockito.doReturn(true).when(configParserMock).isServiceExtensionsEndpointEnabled();
        List<ServiceExtensionTypeEnum> serviceExtensionTypes = new ArrayList<>();
        serviceExtensionTypes.add(ServiceExtensionTypeEnum.VALIDATE_DCR_CREATE_REQUEST);
        Mockito.doReturn(serviceExtensionTypes).when(configParserMock).getServiceExtensionTypes();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        consentCoreServiceMock = mock(ConsentCoreService.class);
        ConsentExtensionsDataHolder dataHolderMock = mock(ConsentExtensionsDataHolder.class);

        staticDataHolderMock = mockStatic(ConsentExtensionsDataHolder.class);
        staticDataHolderMock.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolderMock);
        when(dataHolderMock.getConsentCoreService()).thenReturn(consentCoreServiceMock);

        serviceUtilsMockedStatic = mockStatic(ServiceExtensionUtils.class);

        handler = new ExternalAPIConsentManageHandler();
        consentManageData = mock(ConsentManageData.class);
        mapper = new ObjectMapper();
    }

    @AfterClass
    public void tearDown() {
        staticDataHolderMock.close();
        serviceUtilsMockedStatic.close();
        configParser.close();
    }

    @Test
    public void testHandleGet_Success() throws Exception {

        String validConsentId = "123e4567-e89b-12d3-a456-426614174000";
        when(consentManageData.getClientId()).thenReturn("client123");
        when(consentManageData.getRequestPath()).thenReturn("consents/" + validConsentId);

        // Populate ConsentResource
        ConsentResource consentResource = new ConsentResource();
        consentResource.setConsentID(validConsentId);
        consentResource.setClientID("client123");
        consentResource.setConsentType("ACCOUNTS");
        consentResource.setReceipt("{\"consent\": \"sample\"}");
        when(consentCoreServiceMock.getConsent(eq(validConsentId), anyBoolean()))
                .thenReturn(consentResource);

        // Populate ConsentAttributes
        ConsentAttributes attributes = new ConsentAttributes();
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("scope", "openid accounts");
        attributes.setConsentAttributes(attrMap);
        when(consentCoreServiceMock.getConsentAttributes(validConsentId)).thenReturn(attributes);

        // Create external API response payload
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("responseData", Map.of("field", "value"));
        JsonNode responseJson = mapper.valueToTree(responseMap);

        ExternalServiceResponse serviceResponse = new ExternalServiceResponse();
        serviceResponse.setStatus(StatusEnum.SUCCESS);
        serviceResponse.setData(responseJson);

        serviceUtilsMockedStatic.when(() ->
                ServiceExtensionUtils.invokeExternalServiceCall(any(),
                        eq(ServiceExtensionTypeEnum.PRE_CONSENT_RETRIEVAL))).thenReturn(serviceResponse);

        handler.handleGet(consentManageData);
        verify(consentManageData).setResponsePayload(any());
        verify(consentManageData).setResponseStatus(ResponseStatus.OK);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleGet_ConsentNotFound() throws Exception {

        String validConsentId = "123e4567-e89b-12d3-a456-426614174000";
        when(consentManageData.getClientId()).thenReturn("client123");
        when(consentManageData.getRequestPath()).thenReturn("consents/" + validConsentId);
        when(consentCoreServiceMock.getConsent(eq(validConsentId), anyBoolean())).thenReturn(null);

        handler.handleGet(consentManageData);
    }


    @Test(expectedExceptions = ConsentException.class)
    public void testHandleGet_InvalidConsentId() {

        when(consentManageData.getClientId()).thenReturn("client123");
        when(consentManageData.getRequestPath()).thenReturn("invalid_path");

        handler.handleGet(consentManageData);
    }

    @Test
    public void testHandlePost_Success() throws Exception {

        ConsentManageData localConsentManageData = mock(ConsentManageData.class);
        when(localConsentManageData.getClientId()).thenReturn("client123");
        when(localConsentManageData.getRequestPath()).thenReturn("accounts");

        // Pre-consent generation mock response
        Map<String, Object> preResp = new HashMap<>();
        preResp.put("consentType", "accounts");
        preResp.put("consentStatus", "created");
        preResp.put("consentPayload", Map.of("account", "456"));
        preResp.put("validityTime", 3600);
        preResp.put("consentFrequency", 0);
        preResp.put("recurringIndicator", false);

        JsonNode preConsentJson = mapper.valueToTree(preResp);
        ExternalServiceResponse preServiceResponse = new ExternalServiceResponse();
        preServiceResponse.setStatus(StatusEnum.SUCCESS);
        preServiceResponse.setData(preConsentJson);

        serviceUtilsMockedStatic.when(() ->
                ServiceExtensionUtils.invokeExternalServiceCall(any(),
                        eq(ServiceExtensionTypeEnum.PRE_CONSENT_GENERATION))).thenReturn(preServiceResponse);

        // Created consent
        DetailedConsentResource detailed = new DetailedConsentResource();
        detailed.setConsentID("consent789");
        detailed.setConsentAttributes(Map.of("scope", "accounts"));
        when(consentCoreServiceMock.createAuthorizableConsent(any(), any(), anyString(), anyString(), anyBoolean()))
                .thenReturn(detailed);

        ConsentResource created = new ConsentResource();
        created.setConsentID("consent789");
        created.setClientID("client123");
        created.setConsentType("accounts");
        created.setCurrentStatus("created");
        created.setReceipt("{\"account\":\"456\"}");
        when(consentCoreServiceMock.getConsent(eq("consent789"), anyBoolean()))
                .thenReturn(created);

        JsonNode postConsentJson = mapper.valueToTree(Map.of("responseData", Map.of("result", "done")));
        ExternalServiceResponse postServiceResponse = new ExternalServiceResponse();
        postServiceResponse.setStatus(StatusEnum.SUCCESS);
        postServiceResponse.setData(postConsentJson);

        serviceUtilsMockedStatic.when(() ->
                ServiceExtensionUtils.invokeExternalServiceCall(any(),
                        eq(ServiceExtensionTypeEnum.POST_CONSENT_GENERATION))).thenReturn(postServiceResponse);

        handler.handlePost(localConsentManageData);
        verify(localConsentManageData).setResponsePayload(any());
        verify(localConsentManageData).setResponseStatus(ResponseStatus.CREATED);
    }


    @Test
    public void testHandleDelete_Success() throws Exception {

        String validConsentId = "123e4567-e89b-12d3-a456-426614174000";
        String requestPath = "consents/" + validConsentId;

        when(consentManageData.getClientId()).thenReturn("client123");
        when(consentManageData.getRequestPath()).thenReturn(requestPath);

        ConsentResource resource = new ConsentResource();
        resource.setConsentID(validConsentId);
        resource.setClientID("client123");
        resource.setConsentType("accounts");
        resource.setCurrentStatus("authorized");
        resource.setReceipt("{\"accounts\": [\"acc-1\"]}");

        when(consentCoreServiceMock.getConsent(eq(validConsentId), anyBoolean())).thenReturn(resource);

        // ConsentAttributes (populated)
        ConsentAttributes attributes = new ConsentAttributes();
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("scope", "accounts");
        attrMap.put("frequency", "daily");
        attributes.setConsentAttributes(attrMap);
        when(consentCoreServiceMock.getConsentAttributes(validConsentId)).thenReturn(attributes);

        // External service response payload for revoke
        Map<String, Object> revokeData = new HashMap<>();
        revokeData.put("revocationStatusName", "Revoked");
        revokeData.put("requireTokenRevocation", false);
        JsonNode revokeJson = mapper.valueToTree(revokeData);

        ExternalServiceResponse revokeServiceResponse = new ExternalServiceResponse();
        revokeServiceResponse.setStatus(StatusEnum.SUCCESS);
        revokeServiceResponse.setData(revokeJson);

        // Mock external service
        serviceUtilsMockedStatic.when(() ->
                ServiceExtensionUtils.invokeExternalServiceCall(any(),
                        eq(ServiceExtensionTypeEnum.PRE_CONSENT_REVOCATION))).thenReturn(revokeServiceResponse);

        // Mock revokeConsent success
        when(consentCoreServiceMock.revokeConsent(eq(validConsentId), eq("Revoked"), any(), eq(false)))
                .thenReturn(true);

        handler.handleDelete(consentManageData);
        verify(consentManageData).setResponseStatus(ResponseStatus.NO_CONTENT);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleDelete_RevokeConsentFails() throws Exception {

        String validConsentId = "123e4567-e89b-12d3-a456-426614174000";
        String requestPath = "consents/" + validConsentId;

        when(consentManageData.getClientId()).thenReturn("client123");
        when(consentManageData.getRequestPath()).thenReturn(requestPath);

        // Mock ConsentResource
        ConsentResource resource = new ConsentResource();
        resource.setConsentID(validConsentId);
        resource.setClientID("client123");
        resource.setConsentType("accounts");
        resource.setCurrentStatus("Authorized"); // Should allow revocation
        resource.setReceipt("{\"accounts\": [\"acc-1\"]}");
        when(consentCoreServiceMock.getConsent(eq(validConsentId), anyBoolean())).thenReturn(resource);

        // ConsentAttributes
        ConsentAttributes attributes = new ConsentAttributes();
        attributes.setConsentAttributes(Map.of("scope", "accounts"));
        when(consentCoreServiceMock.getConsentAttributes(validConsentId)).thenReturn(attributes);

        // External service returns REVOKED response
        Map<String, Object> revokeData = new HashMap<>();
        revokeData.put("revocationStatusName", "Revoked");
        revokeData.put("requireTokenRevocation", true);
        JsonNode revokeJson = mapper.valueToTree(revokeData);

        ExternalServiceResponse revokeServiceResponse = new ExternalServiceResponse();
        revokeServiceResponse.setStatus(StatusEnum.SUCCESS);
        revokeServiceResponse.setData(revokeJson);
        serviceUtilsMockedStatic.when(() ->
                ServiceExtensionUtils.invokeExternalServiceCall(any(),
                        eq(ServiceExtensionTypeEnum.PRE_CONSENT_REVOCATION))).thenReturn(revokeServiceResponse);

        // Simulate revoke failure
        when(consentCoreServiceMock.revokeConsent(eq(validConsentId), eq("Revoked"), any(), eq(true)))
                .thenReturn(false);
        handler.handleDelete(consentManageData);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleDelete_MissingClientId() {

        when(consentManageData.getClientId()).thenReturn(null);
        when(consentManageData.getRequestPath()).thenReturn("consents/123e4567-e89b-12d3-a456-426614174000");

        handler.handleDelete(consentManageData);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleDelete_MissingResourcePath() {

        when(consentManageData.getClientId()).thenReturn("client123");
        when(consentManageData.getRequestPath()).thenReturn(null);  // Simulates missing path

        handler.handleDelete(consentManageData);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleDelete_InvalidRequestPath() {

        when(consentManageData.getClientId()).thenReturn("client123");
        when(consentManageData.getRequestPath()).thenReturn("invalid"); // Less than 2 parts

        handler.handleDelete(consentManageData);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleDelete_InvalidConsentId() {

        when(consentManageData.getClientId()).thenReturn("client123");
        when(consentManageData.getRequestPath()).thenReturn("consents/not-a-uuid");

        handler.handleDelete(consentManageData);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleDelete_ConsentNotFound() throws Exception {
        String validConsentId = "123e4567-e89b-12d3-a456-426614174000";

        when(consentManageData.getClientId()).thenReturn("client123");
        when(consentManageData.getRequestPath()).thenReturn("consents/" + validConsentId);
        when(consentCoreServiceMock.getConsent(eq(validConsentId), anyBoolean())).thenReturn(null);

        handler.handleDelete(consentManageData);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandlePut_Unsupported() {
        // Add tests when method is implemented.
        handler.handlePut(consentManageData);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandlePatch_Unsupported() {

        // Add tests when method is implemented.
        handler.handlePatch(consentManageData);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleFileUploadPost_Unsupported() {

        // Add tests when method is implemented.
        handler.handleFileUploadPost(consentManageData);
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testHandleFileGet_Unsupported() {

        // Add tests when method is implemented.
        handler.handleFileGet(consentManageData);
    }
}
