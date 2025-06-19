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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
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
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.impl.ExternalAPIConsentRetrievalStep;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.AccountDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsumerAccountDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ExternalAPIPreConsentAuthorizeResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.PermissionDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.util.ConsentAuthorizeUtil;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.util.TestConstants;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit test class for ExternalAPIConsentRetrievalStep.
 */
public class ExternalAPIConsentRetrievalStepTest {

    private ExternalAPIConsentRetrievalStep consentRetrievalStep;
    private ConsentData consentData;
    private ConsentCoreService consentCoreService;
    private static MockedStatic<FinancialServicesConfigParser> configParser;
    private MockedStatic<ConsentExtensionsDataHolder> dataHolderMockedStatic;
    private MockedStatic<ServiceExtensionUtils> serviceUtilsMockedStatic;
    private MockedStatic<ConsentAuthorizeUtil> authorizeUtilMockedStatic;

    @BeforeClass
    public void setUp() throws Exception {

        consentData = mock(ConsentData.class);
        consentCoreService = mock(ConsentCoreService.class);
        configParser = Mockito.mockStatic(FinancialServicesConfigParser.class);
        FinancialServicesConfigParser configParserMock = Mockito.mock(FinancialServicesConfigParser.class);
        Map<String, Object> configs = new HashMap<String, Object>();
        Mockito.doReturn(configs).when(configParserMock).getConfiguration();
        Mockito.doReturn(FinancialServicesConstants.BASIC_AUTH).when(configParserMock)
                .getServiceExtensionsEndpointSecurityType();
        Mockito.doReturn("test").when(configParserMock)
                .getServiceExtensionsEndpointSecurityBasicAuthUsername();
        Mockito.doReturn("test").when(configParserMock)
                .getServiceExtensionsEndpointSecurityBasicAuthPassword();
        Mockito.doReturn(true).when(configParserMock).isServiceExtensionsEndpointEnabled();
        List<ServiceExtensionTypeEnum> serviceExtensionTypes = new ArrayList<>();
        serviceExtensionTypes.add(ServiceExtensionTypeEnum.POPULATE_CONSENT_AUTHORIZE_SCREEN);
        Mockito.doReturn(serviceExtensionTypes).when(configParserMock).getServiceExtensionTypes();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        // Mock ConsentExtensionsDataHolder
        ConsentExtensionsDataHolder dataHolder = mock(ConsentExtensionsDataHolder.class);
        dataHolderMockedStatic = mockStatic(ConsentExtensionsDataHolder.class);
        dataHolderMockedStatic.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolder);
        when(dataHolder.getConsentCoreService()).thenReturn(consentCoreService);

        // Mock ConsentAuthorizeUtil
        authorizeUtilMockedStatic = mockStatic(ConsentAuthorizeUtil.class);
        authorizeUtilMockedStatic.when(() -> ConsentAuthorizeUtil.extractRequestObject(anyString()))
                .thenReturn("dummyJWT");
        authorizeUtilMockedStatic.when(() -> ConsentAuthorizeUtil.extractConsentId(anyString()))
                .thenReturn("consent123");
        authorizeUtilMockedStatic.when(() -> ConsentAuthorizeUtil.buildConsentDataJSON(any()))
                .thenCallRealMethod();
        authorizeUtilMockedStatic.when(() -> ConsentAuthorizeUtil.buildConsumerDataJSON(any()))
                .thenCallRealMethod();
        authorizeUtilMockedStatic.when(() -> ConsentAuthorizeUtil.addAuthorizedDataObject(any(), any()))
                .thenCallRealMethod();

        // ConsentResource
        ConsentResource consentResource = new ConsentResource();
        consentResource.setConsentType("accounts");
        when(consentCoreService.getConsent(anyString(), anyBoolean())).thenReturn(consentResource);

        // AuthorizationResource
        AuthorizationResource authResource = new AuthorizationResource();
        authResource.setAuthorizationStatus("Created");
        ArrayList<AuthorizationResource> authList = new ArrayList<>();
        authList.add(authResource);
        when(consentCoreService.searchAuthorizations(anyString())).thenReturn(authList);

        // External service success response
        ObjectMapper mapper = new ObjectMapper();
        ExternalAPIPreConsentAuthorizeResponseDTO responseDTO;

        responseDTO = mapper.readValue(TestConstants.ACCOUNT_AUTH_SERVLET_DATA,
                ExternalAPIPreConsentAuthorizeResponseDTO.class);

        JsonNode jsonNode = mapper.valueToTree(responseDTO);

        ExternalServiceResponse externalServiceResponse = new ExternalServiceResponse();
        externalServiceResponse.setStatus(StatusEnum.SUCCESS);
        externalServiceResponse.setData(jsonNode);

        serviceUtilsMockedStatic = mockStatic(ServiceExtensionUtils.class);
        serviceUtilsMockedStatic.when(() -> ServiceExtensionUtils.invokeExternalServiceCall(any(), any()))
                .thenReturn(externalServiceResponse);

        // ConsentData default behavior
        when(consentData.isRegulatory()).thenReturn(true);
        when(consentData.getSpQueryParams()).thenReturn("request=dummy");

        consentRetrievalStep = new ExternalAPIConsentRetrievalStep();
    }

    @AfterClass
    public void tearDown() {
        dataHolderMockedStatic.close();
        authorizeUtilMockedStatic.close();
        serviceUtilsMockedStatic.close();
        configParser.close();
    }

    @Test
    public void testExecute_Success() throws Exception {

        Map<String, String> headers = new HashMap<>();
        ConsentData realConsentData = new ConsentData(
                "sessionKey123",
                "user123",
                "request=dummy",
                "openid accounts",
                "testApp",
                headers
        );
        realConsentData.setRegulatory(true);
        realConsentData.setRedirectURI(URI.create("https://localhost:9443/redirect"));
        realConsentData.setState("dummyState");

        // Mock ConsentAuthorizeUtil statics
        authorizeUtilMockedStatic.when(() -> ConsentAuthorizeUtil.extractRequestObject(anyString()))
                .thenReturn("dummyJWT");
        authorizeUtilMockedStatic.when(() -> ConsentAuthorizeUtil.extractConsentId(anyString()))
                .thenReturn("consent123");

        ConsentResource mockConsentResource = getMockConsentResource();
        when(consentCoreService.getConsent(anyString(), anyBoolean()))
                .thenReturn(mockConsentResource);

        // Mock AuthorizationResource
        AuthorizationResource mockAuthResource = new AuthorizationResource();
        mockAuthResource.setAuthorizationStatus("Created");

        ArrayList<AuthorizationResource> authList = new ArrayList<>();
        authList.add(mockAuthResource);

        when(consentCoreService.searchAuthorizations(anyString()))
                .thenReturn(authList);

        ExternalServiceResponse externalServiceResponse = getExternalServiceResponse();

        serviceUtilsMockedStatic.when(() -> ServiceExtensionUtils.invokeExternalServiceCall(any(), any()))
                .thenReturn(externalServiceResponse);

        JSONObject jsonObject = new JSONObject();
        consentRetrievalStep.execute(realConsentData, jsonObject);

        assertTrue(jsonObject.has("consentData"));
        assertTrue(jsonObject.has("consumerData"));
        assertEquals(jsonObject.getJSONObject("consentData").getString("type"), "accounts");
        assertTrue(jsonObject.getJSONObject("consentData").getBoolean("allowMultipleAccounts"));
    }


    @Test
    public void testExecute_NonRegulatory() {
        when(consentData.isRegulatory()).thenReturn(false);
        JSONObject jsonObject = new JSONObject();
        consentRetrievalStep.execute(consentData, jsonObject);
        assertTrue(jsonObject.isEmpty());
    }

    @Test(expectedExceptions = ConsentException.class)
    public void testExecute_ExternalServiceFailure() throws Exception {
        // Prepare ConsentData object with dummy fields
        Map<String, String> headers = new HashMap<>();
        ConsentData realConsentData = new ConsentData(
                "sessionKey123",
                "user123",
                "request=dummy",
                "openid accounts",
                "sampleApp",
                headers
        );
        realConsentData.setRegulatory(true);
        realConsentData.setRedirectURI(URI.create("https://localhost:9443/redirect"));
        realConsentData.setState("dummyState");

        ConsentResource mockConsentResource = getMockConsentResource();
        when(consentCoreService.getConsent(anyString(), anyBoolean()))
                .thenReturn(mockConsentResource);

        // Mock searchAuthorizations() to return a valid AuthorizationResource
        AuthorizationResource mockAuthResource = new AuthorizationResource();
        mockAuthResource.setAuthorizationStatus("Created");
        ArrayList<AuthorizationResource> authList = new ArrayList<>();
        authList.add(mockAuthResource);
        when(consentCoreService.searchAuthorizations(anyString()))
                .thenReturn(authList);

        // Simulate external service failure
        ExternalServiceResponse errorResponse = new ExternalServiceResponse();
        errorResponse.setStatus(StatusEnum.ERROR);
        ObjectMapper mapper = new ObjectMapper();
        errorResponse.setData(mapper.readTree("{\"data\": { \"errorMessage\" : \"Something went wrong\" } }"));

        serviceUtilsMockedStatic.when(() -> ServiceExtensionUtils.invokeExternalServiceCall(any(), any()))
                .thenReturn(errorResponse);

        // Execute and expect ConsentException due to error response
        JSONObject jsonObject = new JSONObject();
        consentRetrievalStep.execute(realConsentData, jsonObject);
    }

    private ConsentResource getMockConsentResource() {
        ConsentResource resource = new ConsentResource();
        resource.setConsentID("consent123");
        resource.setClientID("client123");
        resource.setConsentType("accounts");
        resource.setReceipt("{\"data\":\"sample\"}");
        resource.setCurrentStatus("awaitingAuthorization");
        return resource;
    }

    private static ExternalServiceResponse getExternalServiceResponse() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ExternalAPIPreConsentAuthorizeResponseDTO responseDTO;
        responseDTO = mapper.readValue(TestConstants.ACCOUNT_AUTH_SERVLET_DATA,
                ExternalAPIPreConsentAuthorizeResponseDTO.class);
        JsonNode jsonNode = mapper.valueToTree(responseDTO);

        ExternalServiceResponse externalServiceResponse = new ExternalServiceResponse();
        externalServiceResponse.setStatus(StatusEnum.SUCCESS);
        externalServiceResponse.setData(jsonNode);
        return externalServiceResponse;
    }

    @Test
    public void testSetMandatoryConsentData_WithAuthorization() throws Exception {

        // Make sure isPreInitiatedConsent = true
        configParser.close();
        configParser = mockStatic(FinancialServicesConfigParser.class);
        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        when(configParserMock.isPreInitiatedConsent()).thenReturn(true);
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        dataHolderMockedStatic.close();
        dataHolderMockedStatic = mockStatic(ConsentExtensionsDataHolder.class);
        ConsentExtensionsDataHolder dataHolder = mock(ConsentExtensionsDataHolder.class);
        dataHolderMockedStatic.when(ConsentExtensionsDataHolder::getInstance).thenReturn(dataHolder);

        when(dataHolder.getConsentCoreService()).thenReturn(consentCoreService);
        when(consentCoreService.getConsent(anyString(), anyBoolean()))
                .thenReturn(getMockConsentResource());

        AuthorizationResource authResource = new AuthorizationResource();
        authResource.setAuthorizationStatus("Created");
        when(consentCoreService.searchAuthorizations(anyString()))
                .thenReturn(new ArrayList<>(List.of(authResource)));

        when(consentCoreService.getDetailedConsent(anyString())).thenReturn(null);

        serviceUtilsMockedStatic.when(() -> ServiceExtensionUtils.invokeExternalServiceCall(any(), any()))
                .thenReturn(getExternalServiceResponse());

        // Spy ConsentData
        Map<String, String> headers = new HashMap<>();
        ConsentData spyConsentData = Mockito.spy(new ConsentData("sessionKey123", "user123", "request=dummy",
                "openid accounts", "testApp", headers
        ));
        spyConsentData.setRegulatory(true);
        spyConsentData.setRedirectURI(URI.create("https://localhost:9443/redirect"));
        spyConsentData.setState("dummyState");

        // Mock static methods to return valid consent ID
        authorizeUtilMockedStatic.when(() -> ConsentAuthorizeUtil.extractRequestObject(anyString()))
                .thenReturn("dummyJWT");
        authorizeUtilMockedStatic.when(() -> ConsentAuthorizeUtil.getRequestObjectJson(anyString()))
                .thenReturn(new JSONObject());
        authorizeUtilMockedStatic.when(() -> ConsentAuthorizeUtil.extractConsentId(anyString()))
                .thenReturn("consent123");

        // Execute
        consentRetrievalStep = new ExternalAPIConsentRetrievalStep();
        JSONObject jsonObject = new JSONObject();
        consentRetrievalStep.execute(spyConsentData, jsonObject);

        Mockito.verify(spyConsentData).setConsentId("consent123");
        Mockito.verify(spyConsentData).setType("accounts");
        Mockito.verify(spyConsentData).setConsentResource(any(ConsentResource.class));
        Mockito.verify(spyConsentData).setAuthResource(any(AuthorizationResource.class));

        assertTrue(jsonObject.has("consentData"));
        assertTrue(jsonObject.has("consumerData"));
    }

    @Test
    public void testAddAuthorizedDataObject_withPermissionsAndSelectedConsumerAccounts() throws Exception {
        // Setup permissions
        PermissionDTO permission = new PermissionDTO();
        permission.setDisplayValues(Collections.singletonList("ReadAccounts"));

        // Setup consumer account
        ConsumerAccountDTO consumerAcc = new ConsumerAccountDTO();
        consumerAcc.setAccountId("acc-user-1");

        // Build hashes
        String permissionHash = UUID.nameUUIDFromBytes(
                new ObjectMapper().writeValueAsString(permission).getBytes(StandardCharsets.UTF_8)).toString();
        String accountHash = UUID.nameUUIDFromBytes(
                new ObjectMapper().writeValueAsString(consumerAcc).getBytes(StandardCharsets.UTF_8)).toString();

        // Build metadata map
        Map<String, Object> retrieved = new HashMap<>();
        retrieved.put("permissions", Collections.singletonList(permission));
        retrieved.put("consumerAccounts", Collections.singletonList(consumerAcc));

        Map<String, Object> metaDataMap = new HashMap<>();
        metaDataMap.put("retrievedAccountsAndPermissions", retrieved);

        // Build input payload with hashed permission and account
        JSONObject inputPayload = new JSONObject();
        JSONObject accountPermissionParams = new JSONObject();
        accountPermissionParams.put("permission-0", permissionHash);
        accountPermissionParams.put("accounts-0", new JSONArray(Collections.singletonList(accountHash)));
        inputPayload.put("requestAccountPermissionParameters", accountPermissionParams);

        ConsentAuthorizeUtil.addAuthorizedDataObject(inputPayload, metaDataMap);

        // Assertions
        assertTrue(inputPayload.has("authorizedData"));
        JSONArray authorizedData = inputPayload.getJSONArray("authorizedData");
        assertEquals(authorizedData.length(), 1);

        JSONObject authEntry = authorizedData.getJSONObject(0);
        assertTrue(authEntry.has("permissions"));
        assertEquals(authEntry.getJSONArray("permissions").getString(0), "ReadAccounts");
        assertTrue(authEntry.has("accounts"));
        assertEquals(authEntry.getJSONArray("accounts").length(), 1); // 1 selected
    }

    @Test
    public void testAddAuthorizedDataObject_withPermissionsAndPermissionInitiatedAccounts() throws Exception {
        // Setup permissions
        PermissionDTO permission = new PermissionDTO();
        permission.setDisplayValues(Collections.singletonList("ReadAccounts"));
        AccountDTO initiatedAcc = new AccountDTO();
        initiatedAcc.setAccountId("acc-init-1");
        permission.setInitiatedAccounts(Collections.singletonList(initiatedAcc));

        // Build hashes
        String permissionHash = UUID.nameUUIDFromBytes(
                new ObjectMapper().writeValueAsString(permission).getBytes(StandardCharsets.UTF_8)).toString();

        // Build metadata map
        Map<String, Object> retrieved = new HashMap<>();
        retrieved.put("permissions", Collections.singletonList(permission));

        Map<String, Object> metaDataMap = new HashMap<>();
        metaDataMap.put("retrievedAccountsAndPermissions", retrieved);

        // Build input payload with hashed permission and account
        JSONObject inputPayload = new JSONObject();
        JSONObject accountPermissionParams = new JSONObject();
        accountPermissionParams.put("permission-0", permissionHash);
        inputPayload.put("requestAccountPermissionParameters", accountPermissionParams);

        ConsentAuthorizeUtil.addAuthorizedDataObject(inputPayload, metaDataMap);

        // Assertions
        assertTrue(inputPayload.has("authorizedData"));
        JSONArray authorizedData = inputPayload.getJSONArray("authorizedData");
        assertEquals(authorizedData.length(), 1);

        JSONObject authEntry = authorizedData.getJSONObject(0);
        assertTrue(authEntry.has("permissions"));
        assertEquals(authEntry.getJSONArray("permissions").getString(0), "ReadAccounts");
        assertTrue(authEntry.has("accounts"));
        assertEquals(authEntry.getJSONArray("accounts").length(), 1); // 1 permission initiated
    }

    @Test
    public void testAddAuthorizedDataObject_withPermissionsAndConsentInitiatedAccounts() throws Exception {
        // Setup permissions
        PermissionDTO permission = new PermissionDTO();
        permission.setDisplayValues(Collections.singletonList("ReadAccounts"));

        // Setup consent initiated accounts
        AccountDTO initiatedAcc = new AccountDTO();
        initiatedAcc.setAccountId("acc-init-1");

        // Build hashes
        String permissionHash = UUID.nameUUIDFromBytes(
                new ObjectMapper().writeValueAsString(permission).getBytes(StandardCharsets.UTF_8)).toString();

        // Build metadata map
        Map<String, Object> retrieved = new HashMap<>();
        retrieved.put("permissions", Collections.singletonList(permission));
        retrieved.put("initiatedAccountsForConsent", Collections.singletonList(initiatedAcc));

        Map<String, Object> metaDataMap = new HashMap<>();
        metaDataMap.put("retrievedAccountsAndPermissions", retrieved);

        // Build input payload with hashed permission and account
        JSONObject inputPayload = new JSONObject();
        JSONObject accountPermissionParams = new JSONObject();
        accountPermissionParams.put("permission-0", permissionHash);
        inputPayload.put("requestAccountPermissionParameters", accountPermissionParams);

        ConsentAuthorizeUtil.addAuthorizedDataObject(inputPayload, metaDataMap);

        // Assertions
        assertTrue(inputPayload.has("authorizedData"));
        JSONArray authorizedData = inputPayload.getJSONArray("authorizedData");
        assertEquals(authorizedData.length(), 1);

        JSONObject authEntry = authorizedData.getJSONObject(0);
        assertTrue(authEntry.has("permissions"));
        assertEquals(authEntry.getJSONArray("permissions").getString(0), "ReadAccounts");
        assertTrue(authEntry.has("accounts"));
        assertEquals(authEntry.getJSONArray("accounts").length(), 1); // 1 consent initiated
    }

    @Test
    public void testAddAuthorizedDataObject_withoutPermissionsWithConsumerAccounts() throws Exception {
        // Setup consumer account
        ConsumerAccountDTO consumerAcc = new ConsumerAccountDTO();
        consumerAcc.setAccountId("acc-user-1");

        // Build hashes
        String accountHash = UUID.nameUUIDFromBytes(
                new ObjectMapper().writeValueAsString(consumerAcc).getBytes(StandardCharsets.UTF_8)).toString();

        // Build metadata map
        Map<String, Object> retrieved = new HashMap<>();
        retrieved.put("consumerAccounts", Collections.singletonList(consumerAcc));

        Map<String, Object> metaDataMap = new HashMap<>();
        metaDataMap.put("retrievedAccountsAndPermissions", retrieved);

        // Build input payload with hashed permission and account
        JSONObject inputPayload = new JSONObject();
        JSONObject accountPermissionParams = new JSONObject();
        accountPermissionParams.put("accounts", new JSONArray(Collections.singletonList(accountHash)));
        inputPayload.put("requestAccountPermissionParameters", accountPermissionParams);

        ConsentAuthorizeUtil.addAuthorizedDataObject(inputPayload, metaDataMap);

        // Assertions
        assertTrue(inputPayload.has("authorizedData"));
        JSONArray authorizedData = inputPayload.getJSONArray("authorizedData");
        assertEquals(authorizedData.length(), 1);

        JSONObject authEntry = authorizedData.getJSONObject(0);
        assertFalse(authEntry.has("permissions"));
        assertTrue(authEntry.has("accounts"));
        assertEquals(authEntry.getJSONArray("accounts").length(), 1); // 1 selected
    }

    @Test
    public void testAddAuthorizedDataObject_withoutPermissionsWithConsentInitiatedAccounts() throws Exception {
        // Setup consent initiated accounts
        AccountDTO initiatedAcc = new AccountDTO();
        initiatedAcc.setAccountId("acc-init-1");

        // Build metadata map
        Map<String, Object> retrieved = new HashMap<>();
        retrieved.put("initiatedAccountsForConsent", Collections.singletonList(initiatedAcc));

        Map<String, Object> metaDataMap = new HashMap<>();
        metaDataMap.put("retrievedAccountsAndPermissions", retrieved);

        // Build input payload with hashed permission and account
        JSONObject inputPayload = new JSONObject();
        JSONObject accountPermissionParams = new JSONObject();
        inputPayload.put("requestAccountPermissionParameters", accountPermissionParams);

        ConsentAuthorizeUtil.addAuthorizedDataObject(inputPayload, metaDataMap);

        // Assertions
        assertTrue(inputPayload.has("authorizedData"));
        JSONArray authorizedData = inputPayload.getJSONArray("authorizedData");
        assertEquals(authorizedData.length(), 1);

        JSONObject authEntry = authorizedData.getJSONObject(0);
        assertFalse(authEntry.has("permissions"));
        assertTrue(authEntry.has("accounts"));
        assertEquals(authEntry.getJSONArray("accounts").length(), 1); // 1 consent initiated
    }

    @Test
    public void testAddAuthorizedDataObject_withoutPermissionsWithoutAccounts() throws Exception {

        // Build metadata map
        Map<String, Object> retrieved = new HashMap<>();
        Map<String, Object> metaDataMap = new HashMap<>();
        metaDataMap.put("retrievedAccountsAndPermissions", retrieved);

        // Build input payload with hashed permission and account
        JSONObject inputPayload = new JSONObject();
        JSONObject accountPermissionParams = new JSONObject();
        inputPayload.put("requestAccountPermissionParameters", accountPermissionParams);

        ConsentAuthorizeUtil.addAuthorizedDataObject(inputPayload, metaDataMap);

        // Assertions
        assertTrue(inputPayload.has("authorizedData"));
        assertTrue(inputPayload.getJSONArray("authorizedData").isEmpty());
    }
}
