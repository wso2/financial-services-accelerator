/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.identity.extensions.client.registration.attribute.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.oauth.dcr.bean.ApplicationRegistrationRequest;
import org.wso2.carbon.identity.oauth.dcr.bean.ApplicationUpdateRequest;
import org.wso2.carbon.identity.oauth.dcr.exception.DCRMClientException;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigurationService;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.StatusEnum;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.identity.extensions.client.registration.dcr.attribute.filter.FSAdditionalAttributeFilter;
import org.wso2.financial.services.accelerator.identity.extensions.client.registration.dcr.cache.JwtJtiCache;
import org.wso2.financial.services.accelerator.identity.extensions.client.registration.dcr.cache.JwtJtiCacheKey;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonConstants;
import org.wso2.financial.services.accelerator.identity.extensions.util.TestConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Additional attribute filter tests.
 */
public class FSAdditionalAttributeFilterTest {

    FSAdditionalAttributeFilter fsAdditionalAttributeFilter;
    IdentityExtensionsDataHolder identityExtensionsDataHolder;
    private static MockedStatic<IdentityExtensionsDataHolder> identityExtensionsDataHolderMockedStatic;
    private MockedStatic<FinancialServicesConfigParser> configParserMockedStatic;
    private static MockedStatic<JwtJtiCache> jwtJtiCacheMockedStatic;
    MockedStatic<ServiceExtensionUtils> serviceExtensionUtilsMockedStatic;
    private static final Gson gson = new Gson();

    @BeforeClass
    public void beforeClass() throws JsonProcessingException {

        identityExtensionsDataHolder = IdentityExtensionsDataHolder.getInstance();
        Map<String, Object> confMap = new HashMap<>();
        confMap.put(FinancialServicesConstants.PRIMARY_AUTHENTICATOR_DISPLAY_NAME, "basic");
        confMap.put(FinancialServicesConstants.PRIMARY_AUTHENTICATOR_NAME, "BasicAuthenticator");
        confMap.put(FinancialServicesConstants.IDENTITY_PROVIDER_NAME, "SMSAuthentication");
        confMap.put(FinancialServicesConstants.IDENTITY_PROVIDER_STEP, "2");
        confMap.put(FinancialServicesConstants.POST_APPLICATION_LISTENER, "org.wso2.financial.services.accelerator" +
                ".identity.extensions.client.registration.application.listener.ApplicationUpdaterImpl");
        confMap.put(FinancialServicesConstants.DCR_SERVICE_EXTENSION, "org.wso2.financial.services.accelerator" +
                ".identity.extensions.client.registration.dcr.extension.FSDefaultDCRExtension");
        confMap.put(FinancialServicesConstants.REQUEST_VALIDATOR, "org.wso2.financial.services.accelerator.identity" +
                ".extensions.auth.extensions.request.validator.DefaultFSRequestObjectValidator");
        confMap.put(FinancialServicesConstants.RESPONSE_HANDLER, "org.wso2.financial.services.accelerator.identity" +
                ".extensions.auth.extensions.response.handler.impl.FSDefaultResponseTypeHandlerImpl");
        confMap.put(FinancialServicesConstants.CLAIM_PROVIDER, "org.wso2.financial.services.accelerator.identity" +
                ".extensions.claims.RoleClaimProviderImpl");
        confMap.put(FinancialServicesConstants.GRANT_HANDLER, "org.wso2.financial.services.accelerator.identity" +
                ".extensions.grant.type.handlers.FSDefaultGrantHandler");
        FinancialServicesConfigurationService configurationService =
                Mockito.mock(FinancialServicesConfigurationService.class);
        Mockito.when(configurationService.getConfigurations()).thenReturn(confMap);
        Mockito.when(configurationService.getDCRParamsConfig()).thenReturn(getDcrParamConfigs());
        Mockito.when(configurationService.getDCRValidatorsConfig()).thenReturn(getDcrValidatorConfigs());

        identityExtensionsDataHolderMockedStatic = Mockito.mockStatic(IdentityExtensionsDataHolder.class);
        identityExtensionsDataHolderMockedStatic.when(IdentityExtensionsDataHolder::getInstance)
                .thenReturn(identityExtensionsDataHolder);

        configParserMockedStatic = Mockito.mockStatic(FinancialServicesConfigParser.class);
        FinancialServicesConfigParser configParserMock = mock(FinancialServicesConfigParser.class);
        doReturn(true).when(configParserMock).isServiceExtensionsEndpointEnabled();
        doReturn(new ArrayList<>()).when(configParserMock).getServiceExtensionTypes();
        configParserMockedStatic.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        identityExtensionsDataHolder.setConfigurationService(configurationService);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree("{" +
            "   \"clientData\": {" +
            "       \"software_id\": \"testSoftwareId\"" +
            "   }" +
            "}");
        serviceExtensionUtilsMockedStatic = Mockito.mockStatic(ServiceExtensionUtils.class);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils
                .isInvokeExternalService(any())).thenReturn(true);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils
                .invokeExternalServiceCall(any(), any())).thenReturn(new ExternalServiceResponse("testId",
                StatusEnum.SUCCESS, rootNode));

        JwtJtiCache jwtJtiCache = Mockito.mock(JwtJtiCache.class);
        Mockito.doReturn(null).when(jwtJtiCache).getFromCache(JwtJtiCacheKey.of(anyString()));


        jwtJtiCacheMockedStatic = Mockito.mockStatic(JwtJtiCache.class);
        jwtJtiCacheMockedStatic.when(JwtJtiCache::getInstance).thenReturn(jwtJtiCache);

        fsAdditionalAttributeFilter = new FSAdditionalAttributeFilter();

    }

    @AfterClass
    public void afterClass() {
        identityExtensionsDataHolderMockedStatic.close();
        configParserMockedStatic.close();
        jwtJtiCacheMockedStatic.close();
        serviceExtensionUtilsMockedStatic.close();
    }

    @Test
    public void testFilterDCRRegisterAttributes() throws DCRMClientException {

        ApplicationRegistrationRequest appRegistrationRequest = gson.fromJson(TestConstants.DCR_APP_REQUEST,
                ApplicationRegistrationRequest.class);
        Map<String, Object> resultMap = fsAdditionalAttributeFilter.filterDCRRegisterAttributes(appRegistrationRequest,
                TestConstants.getSSAParamMap());

        Assert.assertNotNull(resultMap);
        Assert.assertNotNull(resultMap.get(IdentityCommonConstants.SOFTWARE_ID));
        Assert.assertNotNull(resultMap.get(IdentityCommonConstants.APPLICATION_TYPE));
    }

    @Test
    public void testFilterDCRRegisterAttributesWithoutExtension() throws DCRMClientException {

        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils
                .isInvokeExternalService(any())).thenReturn(false);
        ApplicationRegistrationRequest appRegistrationRequest = gson.fromJson(TestConstants.DCR_APP_REQUEST,
                ApplicationRegistrationRequest.class);
        Map<String, Object> resultMap = fsAdditionalAttributeFilter.filterDCRRegisterAttributes(appRegistrationRequest,
                TestConstants.getSSAParamMap());

        Assert.assertNotNull(resultMap);
        Assert.assertFalse(resultMap.isEmpty());

        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils
                .isInvokeExternalService(any())).thenReturn(true);
    }

    @Test
    public void testFilterDCRRegisterAttributesForMCRApps() throws DCRMClientException {

        ApplicationRegistrationRequest appRegistrationRequest = gson.fromJson(TestConstants.DCR_APP_REQUEST_FOR_MCR,
                ApplicationRegistrationRequest.class);
        Map<String, Object> resultMap = fsAdditionalAttributeFilter.filterDCRRegisterAttributes(appRegistrationRequest,
                TestConstants.getSSAParamMap());

        Assert.assertNotNull(resultMap);
        Assert.assertNotNull(resultMap.get(IdentityCommonConstants.SOFTWARE_ID));
        Assert.assertNotNull(resultMap.get(IdentityCommonConstants.APPLICATION_TYPE));
    }

    @Test
    public void testFilterDCRRegisterAttributesWithoutMandatoryFields() {

        ApplicationRegistrationRequest appRegistrationRequest = gson.fromJson(
                TestConstants.DCR_APP_REQUEST_WITHOUT_MANDATORY_FIELDS, ApplicationRegistrationRequest.class);
        try {
            fsAdditionalAttributeFilter.filterDCRRegisterAttributes(appRegistrationRequest,
                    TestConstants.getSSAParamMap());
        } catch (DCRMClientException e) {
            Assert.assertEquals(e.getMessage(), "Required parameter softwareId not found in the request");
        }
    }

    @Test
    public void testFilterDCRRegisterAttributesWithDisallowedValues() {

        ApplicationRegistrationRequest appRegistrationRequest = gson.fromJson(
                TestConstants.DCR_APP_REQUEST_WITH_DISALLOWED_VALUES, ApplicationRegistrationRequest.class);
        try {
            fsAdditionalAttributeFilter.filterDCRRegisterAttributes(appRegistrationRequest,
                    TestConstants.getSSAParamMap());
        } catch (DCRMClientException e) {
            Assert.assertEquals(e.getMessage(), "Invalid scope provided");
        }
    }

    @Test
    public void testFilterDCRUpdateAttributes() throws DCRMClientException {

        ApplicationUpdateRequest applicationUpdateRequest = gson.fromJson(TestConstants.DCR_APP_REQUEST,
                ApplicationUpdateRequest.class);
        ServiceProviderProperty[] serviceProviderProperties = new ServiceProviderProperty[0];
        Map<String, Object> resultMap = fsAdditionalAttributeFilter.filterDCRUpdateAttributes(applicationUpdateRequest,
                TestConstants.getSSAParamMap(), serviceProviderProperties);

        Assert.assertNotNull(resultMap);
        Assert.assertNotNull(resultMap.get(IdentityCommonConstants.SOFTWARE_ID));
        Assert.assertNotNull(resultMap.get(IdentityCommonConstants.APPLICATION_TYPE));
    }

    @Test
    public void testFilterDCRUpdateAttributesWithoutExtension() throws DCRMClientException {

        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils
                .isInvokeExternalService(any())).thenReturn(false);
        ApplicationUpdateRequest applicationUpdateRequest = gson.fromJson(TestConstants.DCR_APP_REQUEST,
                ApplicationUpdateRequest.class);
        ServiceProviderProperty[] serviceProviderProperties = new ServiceProviderProperty[0];
        Map<String, Object> resultMap = fsAdditionalAttributeFilter.filterDCRUpdateAttributes(applicationUpdateRequest,
                TestConstants.getSSAParamMap(), serviceProviderProperties);

        Assert.assertNotNull(resultMap);
        Assert.assertFalse(resultMap.isEmpty());

        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils
                .isInvokeExternalService(any())).thenReturn(true);
    }

    @Test
    public void testFilterDCRUpdateAttributesForMCRApps() throws DCRMClientException {

        ApplicationUpdateRequest applicationUpdateRequest = gson.fromJson(TestConstants.DCR_APP_REQUEST_FOR_MCR,
                ApplicationUpdateRequest.class);
        ServiceProviderProperty[] serviceProviderProperties = new ServiceProviderProperty[0];
        Map<String, Object> resultMap = fsAdditionalAttributeFilter.filterDCRUpdateAttributes(applicationUpdateRequest,
                TestConstants.getSSAParamMap(), serviceProviderProperties);

        Assert.assertNotNull(resultMap);
        Assert.assertNotNull(resultMap.get(IdentityCommonConstants.SOFTWARE_ID));
        Assert.assertNotNull(resultMap.get(IdentityCommonConstants.APPLICATION_TYPE));
    }

    @Test
    public void testProcessDCRGetAttributes() throws DCRMClientException {

        Map<String, Object> resultMap = fsAdditionalAttributeFilter
                .processDCRGetAttributes(TestConstants.getSSAParamMapForRetrieval());

        Assert.assertNotNull(resultMap);
    }

    @Test
    public void testGetResponseAttributeKeys() {

        List<String> resultMap = fsAdditionalAttributeFilter.getResponseAttributeKeys();

        Assert.assertNotNull(resultMap);
        Assert.assertTrue(resultMap.contains(IdentityCommonConstants.SOFTWARE_ID));
        Assert.assertTrue(resultMap.contains(IdentityCommonConstants.APPLICATION_TYPE));
        Assert.assertTrue(resultMap.contains(FinancialServicesConstants.SCOPE));
    }

    private static Map<String, Map<String, Object>> getDcrParamConfigs() {

        Map<String, Map<String, Object>> dcrParamConfigs = new HashMap<>();
        dcrParamConfigs.put("SoftwareId", Map.of("Required", true, "IncludeInResponse", true,
                "Key", "software_id", "Name", "SoftwareId", "AllowedValues", new ArrayList<>()));
        dcrParamConfigs.put("SoftwareStatement", Map.of("Required", true, "IncludeInResponse", true,
            "Key", "software_statement", "Name", "SoftwareStatement", "AllowedValues",
                new ArrayList<>()));

        List<String> allowedScopes = new ArrayList<>();
        allowedScopes.add("accounts");
        allowedScopes.add("payments");
        dcrParamConfigs.put("Scope", Map.of("Required", true, "IncludeInResponse", true,
                "Key", "scope", "Name", "Scope", "AllowedValues", allowedScopes));
        return dcrParamConfigs;
    }

    private static Map<String, Map<String, Object>> getDcrValidatorConfigs() {

        Map<String, Map<String, Object>> dcrValidatorConfigs = new HashMap<>();
        dcrValidatorConfigs.put("RequiredParamsValidator", Map.of("Enable", true, "Priority", "1",
                "Class", "org.wso2.financial.services.accelerator.identity.extensions.client.registration" +
                        ".dcr.validators.RequiredParamsValidator", "Name", "RequiredParamsValidator",
                "AllowedValues", new ArrayList<>()));
        dcrValidatorConfigs.put("IssuerValidator", Map.of("Enable", true, "Priority", "2", "Class",
                "org.wso2.financial.services.accelerator.identity.extensions.client.registration.dcr." +
                        "validators.IssuerValidator",
                "Name", "IssuerValidator", "AllowedValues", new ArrayList<>()));
        dcrValidatorConfigs.put("RedirectUriFormatValidator", Map.of("Enable", true, "Priority", "3",
                "Class", "org.wso2.financial.services.accelerator.identity.extensions.client.registration." +
                        "dcr.validators.RedirectUriFormatValidator", "Name", "RedirectUriFormatValidator",
                "AllowedValues", new ArrayList<>()));
        dcrValidatorConfigs.put("RedirectUriMatchValidator", Map.of("Enable", true, "Priority", "4",
                "Class", "org.wso2.financial.services.accelerator.identity.extensions.client.registration." +
                        "dcr.validators.RedirectUriMatchValidator", "Name", "RedirectUriMatchValidator",
                "AllowedValues", new ArrayList<>()));
        dcrValidatorConfigs.put("UriHostnameValidator", Map.of("Enable", true, "Priority", "5",
                "Class", "org.wso2.financial.services.accelerator.identity.extensions.client.registration" +
                        ".dcr.validators.UriHostnameValidator", "Name", "UriHostnameValidator",
                "AllowedValues", new ArrayList<>()));

        List<String> allowedIssuers = new ArrayList<>();
        allowedIssuers.add("OpenBanking Ltd");
        dcrValidatorConfigs.put("SSAIssuerValidator", Map.of("Enable", true, "Priority", "6",
                "Class", "org.wso2.financial.services.accelerator.identity.extensions.client.registration" +
                        ".dcr.validators.SSAIssuerValidator", "Name", "SSAIssuerValidator",
                "AllowedValues", allowedIssuers));
        dcrValidatorConfigs.put("RequestJTIValidator", Map.of("Enable", true, "Priority", "7",
                "Class", "org.wso2.financial.services.accelerator.identity.extensions.client.registration" +
                        ".dcr.validators.RequestJTIValidator", "Name", "RequestJTIValidator", "AllowedValues",
                new ArrayList<>()));
        dcrValidatorConfigs.put("SSAJTIValidator", Map.of("Enable", true, "Priority", "8", "Class",
                "org.wso2.financial.services.accelerator.identity.extensions.client.registration.dcr" +
                        ".validators.SSAJTIValidator",
                "Name", "SSAJTIValidator", "AllowedValues", new ArrayList<>()));
        dcrValidatorConfigs.put("TokenEndpointAuthSigningAlgValidator", Map.of("Enable", true, "Priority",
                "9", "Class", "org.wso2.financial.services.accelerator.identity.extensions.client." +
                        "registration.dcr.validators.TokenEndpointAuthSigningAlgValidator",
                "Name", "TokenEndpointAuthSigningAlgValidator", "AllowedValues", new ArrayList<>()));
        return dcrValidatorConfigs;
    }
}
