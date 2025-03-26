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
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ConsentData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model.ExternalAPIPreConsentAuthorizeResponseDTO;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.util.ConsentAuthorizeUtil;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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
        serviceExtensionTypes.add(ServiceExtensionTypeEnum.VALIDATE_DCR_CREATE_REQUEST);
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

        // ConsentResource
        ConsentResource consentResource = new ConsentResource();
        consentResource.setConsentType("accounts");
        when(consentCoreService.getConsent(anyString(), anyBoolean())).thenReturn(consentResource);

        // AuthorizationResource
        AuthorizationResource authResource = new AuthorizationResource();
        authResource.setAuthorizationStatus("created");
        ArrayList<AuthorizationResource> authList = new ArrayList<>();
        authList.add(authResource);
        when(consentCoreService.searchAuthorizations(anyString())).thenReturn(authList);

        // External service success response
        ExternalAPIPreConsentAuthorizeResponseDTO responseDTO = new ExternalAPIPreConsentAuthorizeResponseDTO();
        List<Map<String, Object>> consentDataList = new ArrayList<>();
        consentDataList.add(Map.of("field", "value"));
        List<Map<String, Object>> consumerDataList = new ArrayList<>();
        consumerDataList.add(Map.of("account", "123"));

        responseDTO.setConsentData(consentDataList);
        responseDTO.setConsumerData(consumerDataList);

        ObjectMapper mapper = new ObjectMapper();
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
        mockAuthResource.setAuthorizationStatus("created");

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
        assertTrue(jsonObject.has("accounts"));
        assertEquals(jsonObject.getJSONArray("consentData").getJSONObject(0).getString("field"), "value");
        assertEquals(jsonObject.getJSONArray("accounts").getJSONObject(0).getString("account"), "123");
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
        mockAuthResource.setAuthorizationStatus("created");
        ArrayList<AuthorizationResource> authList = new ArrayList<>();
        authList.add(mockAuthResource);
        when(consentCoreService.searchAuthorizations(anyString()))
                .thenReturn(authList);

        // Simulate external service failure
        ExternalServiceResponse errorResponse = new ExternalServiceResponse();
        errorResponse.setStatus(StatusEnum.ERROR);
        errorResponse.setErrorMessage("Something went wrong");

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

    private static ExternalServiceResponse getExternalServiceResponse() {
        ExternalAPIPreConsentAuthorizeResponseDTO responseDTO = new ExternalAPIPreConsentAuthorizeResponseDTO();

        List<Map<String, Object>> consentDataList = new ArrayList<>();
        Map<String, Object> consentEntry = new HashMap<>();
        consentEntry.put("field", "value");
        consentDataList.add(consentEntry);

        List<Map<String, Object>> consumerDataList = new ArrayList<>();
        Map<String, Object> consumerEntry = new HashMap<>();
        consumerEntry.put("account", "123");
        consumerDataList.add(consumerEntry);

        responseDTO.setConsentData(consentDataList);
        responseDTO.setConsumerData(consumerDataList);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.valueToTree(responseDTO);

        ExternalServiceResponse externalServiceResponse = new ExternalServiceResponse();
        externalServiceResponse.setStatus(StatusEnum.SUCCESS);
        externalServiceResponse.setData(jsonNode);
        return externalServiceResponse;
    }

}
