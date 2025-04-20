/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.identity.extensions.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.openidconnect.model.RequestObject;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.StatusEnum;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;
import org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.request.validator.models.FSRequestObject;
import org.wso2.financial.services.accelerator.identity.extensions.auth.extensions.request.validator.models.ValidationResponse;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for IdentityServiceExtensionUtils.
 */
public class IdentityServiceExtensionUtilsTest {

    private static MockedStatic<ServiceExtensionUtils> mockedServiceExtensionUtils;
    private static MockedStatic<FinancialServicesConfigParser> mockedConfigParser;
    private static MockedStatic<IdentityCommonUtils> mockedIdentityCommonUtils;

    @BeforeClass
    public void beforeClass() throws ConsentManagementException {
        mockedConfigParser = Mockito.mockStatic(FinancialServicesConfigParser.class);
        mockedServiceExtensionUtils = Mockito.mockStatic(ServiceExtensionUtils.class);
        mockedIdentityCommonUtils = Mockito.mockStatic(IdentityCommonUtils.class);

        mockedConfigParser.when(FinancialServicesConfigParser::getInstance)
                .thenReturn(mock(FinancialServicesConfigParser.class));

        ConsentResource mockConsentResource = new ConsentResource();
        mockConsentResource.setCreatedTime(3600L);
        mockConsentResource.setValidityPeriod(3600L);

        ConsentCoreService mockConsentCoreService = mock(ConsentCoreService.class);
        when(mockConsentCoreService.getConsent(Mockito.anyString(), Mockito.anyBoolean()))
                .thenReturn(mockConsentResource);

        IdentityExtensionsDataHolder.getInstance().setConsentCoreService(mockConsentCoreService);
    }

    @AfterClass
    public static void afterClass() {
        mockedConfigParser.close();
        mockedServiceExtensionUtils.close();
        mockedIdentityCommonUtils.close();
    }

    @Test
    public void testServiceExtensionActionStatusValidation_Success() throws IdentityOAuth2Exception {
        ExternalServiceResponse response = mock(ExternalServiceResponse.class);
        when(response.getStatus()).thenReturn(StatusEnum.SUCCESS);

        // No exception should be thrown for a successful response
        IdentityServiceExtensionUtils.serviceExtensionActionStatusValidation(response);
    }

    @Test(expectedExceptions = IdentityOAuth2Exception.class)
    public void testServiceExtensionActionStatusValidation_Failure_NoData() throws IdentityOAuth2Exception {
        ExternalServiceResponse response = mock(ExternalServiceResponse.class);
        when(response.getStatus()).thenReturn(StatusEnum.ERROR);
        when(response.getData()).thenReturn(null);

        // Should throw an exception due to missing data
        IdentityServiceExtensionUtils.serviceExtensionActionStatusValidation(response);
    }

    @Test(expectedExceptions = IdentityOAuth2Exception.class)
    public void testServiceExtensionActionStatusValidation_Failure_WithErrorData() throws IdentityOAuth2Exception {
        ExternalServiceResponse response = mock(ExternalServiceResponse.class);
        when(response.getStatus()).thenReturn(StatusEnum.ERROR);

        JsonNode errorData = mock(JsonNode.class);
        when(errorData.path(FinancialServicesConstants.ERROR_MESSAGE))
                .thenReturn(mock(JsonNode.class));
        when(errorData.path(FinancialServicesConstants.ERROR_MESSAGE)
                .asText(FinancialServicesConstants.DEFAULT_ERROR_MESSAGE))
                .thenReturn("Custom error message");
        when(errorData.path(FinancialServicesConstants.ERROR_DESCRIPTION))
                .thenReturn(mock(JsonNode.class));
        when(errorData.path(FinancialServicesConstants.ERROR_DESCRIPTION)
                .asText(FinancialServicesConstants.DEFAULT_ERROR_DESCRIPTION))
                .thenReturn("Custom error description");

        when(response.getData()).thenReturn(errorData);

        // Should throw an exception with the custom error message and description
        IdentityServiceExtensionUtils.serviceExtensionActionStatusValidation(response);
    }

    @Test
    public void testValidateRequestObjectWithServiceExtension_Success() throws Exception {
        FSRequestObject mockRequestObject = new FSRequestObject(new RequestObject());
        mockRequestObject.setClaimSet(JWTClaimsSet.parse("{}"));

        ExternalServiceResponse mockResponse = new ExternalServiceResponse();
        mockResponse.setStatus(StatusEnum.SUCCESS);

        mockedServiceExtensionUtils.when(() -> ServiceExtensionUtils
                .invokeExternalServiceCall(Mockito.any(), Mockito.any())).thenReturn(mockResponse);

        ValidationResponse validationResponse = IdentityServiceExtensionUtils
                .validateRequestObjectWithServiceExtension(mockRequestObject);
        Assert.assertTrue(validationResponse.isValid());
        Assert.assertNull(validationResponse.getViolationMessage());
    }

    @Test
    public void testValidateRequestObjectWithServiceExtension_Failure() throws Exception {
        FSRequestObject mockRequestObject = new FSRequestObject(new RequestObject());
        mockRequestObject.setClaimSet(JWTClaimsSet.parse("{}"));

        ExternalServiceResponse mockResponse = new ExternalServiceResponse();
        mockResponse.setStatus(StatusEnum.ERROR);

        ObjectMapper objectMapper = new ObjectMapper();
        String errorDataJson = "{"
                + "\"" + FinancialServicesConstants.ERROR_MESSAGE + "\": \"Custom error message\","
                + "\"" + FinancialServicesConstants.ERROR_DESCRIPTION + "\": \"Custom error description\""
                + "}";
        JsonNode mockErrorData = objectMapper.readTree(errorDataJson);

        mockResponse.setData(mockErrorData);

        mockedServiceExtensionUtils.when(() -> ServiceExtensionUtils
                .invokeExternalServiceCall(Mockito.any(), Mockito.any())).thenReturn(mockResponse);

        ValidationResponse validationResponse = IdentityServiceExtensionUtils
                .validateRequestObjectWithServiceExtension(mockRequestObject);
        Assert.assertFalse(validationResponse.isValid());
        Assert.assertEquals(validationResponse.getViolationMessage(), "Custom error description");
    }

    @Test
    public void testIssueRefreshTokenWithServiceExtension_Success() throws Exception {
        OAuthTokenReqMessageContext mockContext = new OAuthTokenReqMessageContext(new OAuth2AccessTokenReqDTO());
        mockContext.setScope(new String[]{"scope1", "scope2"});
        mockContext.getOauth2AccessTokenReqDTO().setGrantType("authorization_code");
        mockContext.setRefreshTokenvalidityPeriod(3600L);

        mockedIdentityCommonUtils.when(() -> IdentityCommonUtils.getConsentIdFromScopesArray(Mockito.any()))
                .thenReturn("123");

        ExternalServiceResponse mockResponse = new ExternalServiceResponse();
        mockResponse.setStatus(StatusEnum.SUCCESS);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode mockResponseData = objectMapper.createObjectNode()
                .put(IdentityCommonConstants.ISSUE_REFRESH_TOKEN, true);

        mockResponse.setData(mockResponseData);

        mockedServiceExtensionUtils.when(() -> ServiceExtensionUtils
                        .invokeExternalServiceCall(Mockito.any(), Mockito.any())).thenReturn(mockResponse);

        boolean result = IdentityServiceExtensionUtils.issueRefreshTokenWithServiceExtension(mockContext);
        Assert.assertTrue(result);
    }

    @Test(expectedExceptions = IdentityOAuth2Exception.class)
    public void testIssueRefreshTokenWithServiceExtension_Failure_MissingResponseData() throws Exception {
        OAuthTokenReqMessageContext mockContext = new OAuthTokenReqMessageContext(new OAuth2AccessTokenReqDTO());
        mockContext.setScope(new String[]{"scope1", "scope2"});
        mockContext.getOauth2AccessTokenReqDTO().setGrantType("authorization_code");
        mockContext.setRefreshTokenvalidityPeriod(3600L);

        mockedIdentityCommonUtils.when(() -> IdentityCommonUtils.getConsentIdFromScopesArray(Mockito.any()))
                .thenReturn("123");

        ExternalServiceResponse mockResponse = new ExternalServiceResponse();
        mockResponse.setStatus(StatusEnum.SUCCESS);

        mockedServiceExtensionUtils.when(() -> ServiceExtensionUtils
                .invokeExternalServiceCall(Mockito.any(), Mockito.any())).thenReturn(mockResponse);

        IdentityServiceExtensionUtils.issueRefreshTokenWithServiceExtension(mockContext);
    }

}
