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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.validate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.StatusEnum;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.util.TestConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.util.TestUtil;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.impl.ConsentValidatorServiceExtension;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.model.ConsentValidateData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.model.ConsentValidationResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Consent validator service extension tests.
 */
public class ConsentValidatorServiceExtensionTest {

    ConsentValidatorServiceExtension validator = new ConsentValidatorServiceExtension();
    ConsentValidateData consentValidateDataMock;
    ConsentValidationResult consentValidationResultMock;
    MockedStatic<FinancialServicesUtils> financialServicesUtilsMockedStatic;
    MockedStatic<ServiceExtensionUtils> serviceExtensionUtilsMockedStatic;
    private static MockedStatic<FinancialServicesConfigParser> configParser;

    @BeforeClass
    public void beforeTest() throws JsonProcessingException {
        consentValidateDataMock = Mockito.mock(ConsentValidateData.class);
        consentValidationResultMock = Mockito.spy(ConsentValidationResult.class);

        configParser = Mockito.mockStatic(FinancialServicesConfigParser.class);
        Map<String, Object> configs = new HashMap<String, Object>();
        configs.put(FinancialServicesConstants.MAX_INSTRUCTED_AMOUNT, "1000");
        FinancialServicesConfigParser configParserMock = Mockito.mock(FinancialServicesConfigParser.class);
        Mockito.doReturn(configs).when(configParserMock).getConfiguration();
        configParser.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);


        financialServicesUtilsMockedStatic = Mockito.mockStatic(FinancialServicesUtils.class);
        financialServicesUtilsMockedStatic.when(() -> FinancialServicesUtils.resolveUsernameFromUserId(anyString()))
                .thenReturn(TestConstants.SAMPLE_USER_ID);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree("{}");
        serviceExtensionUtilsMockedStatic = Mockito.mockStatic(ServiceExtensionUtils.class);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils
                .invokeExternalServiceCall(any(), any())).thenReturn(new ExternalServiceResponse("testId",
                StatusEnum.SUCCESS, rootNode));
    }

    @AfterClass
    public void afterTest() {
        financialServicesUtilsMockedStatic.close();
        serviceExtensionUtilsMockedStatic.close();
        configParser.close();
    }

    @Test
    public void testValidateWithoutComprehensiveConsent() {
        doReturn(null).when(consentValidateDataMock).getComprehensiveConsent();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertFalse(consentValidationResultMock.isValid());
        Assert.assertEquals(consentValidationResultMock.getErrorCode(), ResponseStatus.BAD_REQUEST.getReasonPhrase());
        Assert.assertEquals(consentValidationResultMock.getErrorMessage(), "Consent Details cannot be found");
        Assert.assertEquals(consentValidationResultMock.getHttpCode(), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testValidateWithoutReceipt() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(null).when(consentResource).getReceipt();
        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertFalse(consentValidationResultMock.isValid());
        Assert.assertEquals(consentValidationResultMock.getErrorCode(), ResponseStatus.BAD_REQUEST.getReasonPhrase());
        Assert.assertEquals(consentValidationResultMock.getErrorMessage(), "Consent Details cannot be found");
        Assert.assertEquals(consentValidationResultMock.getHttpCode(), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testValidateWithUserIdMismatch() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(TestConstants.VALID_INITIATION).when(consentResource).getReceipt();
        AuthorizationResource authorizationResource = new AuthorizationResource(TestConstants.SAMPLE_CONSENT_ID,
                "admin", TestConstants.SAMPLE_AUTHORIZATION_STATUS,
                TestConstants.SAMPLE_AUTH_TYPE, System.currentTimeMillis() / 1000);
        authorizationResource.setAuthorizationID(TestConstants.SAMPLE_AUTH_ID);

        ArrayList<AuthorizationResource> authResources = new ArrayList<>(List.of(authorizationResource));
        doReturn(authResources).when(consentResource).getAuthorizationResources();

        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn("admin@wso2.com").when(consentValidateDataMock).getUserId();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertFalse(consentValidationResultMock.isValid());
        Assert.assertEquals(consentValidationResultMock.getErrorCode(),
                ResponseStatus.BAD_REQUEST.getReasonPhrase());
        Assert.assertEquals(consentValidationResultMock.getErrorMessage(), "Invalid User Id");
        Assert.assertEquals(consentValidationResultMock.getHttpCode(), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testValidateWithNullClientIdFromToken() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(TestConstants.VALID_INITIATION).when(consentResource).getReceipt();
        ArrayList<AuthorizationResource> authResources = TestUtil.getSampleAuthorizationResourceArray(
                TestConstants.SAMPLE_CONSENT_ID, TestConstants.SAMPLE_AUTH_ID);
        doReturn(authResources).when(consentResource).getAuthorizationResources();

        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(TestConstants.SAMPLE_USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(null).when(consentValidateDataMock).getClientId();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertFalse(consentValidationResultMock.isValid());
        Assert.assertEquals(consentValidationResultMock.getErrorCode(), ResponseStatus.FORBIDDEN.getReasonPhrase());
        Assert.assertEquals(consentValidationResultMock.getErrorMessage(), "Invalid Client Id");
        Assert.assertEquals(consentValidationResultMock.getHttpCode(), HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void testValidateWithNullClientIdFromConsent() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(TestConstants.VALID_INITIATION).when(consentResource).getReceipt();
        ArrayList<AuthorizationResource> authResources = TestUtil.getSampleAuthorizationResourceArray(
                TestConstants.SAMPLE_CONSENT_ID, TestConstants.SAMPLE_AUTH_ID);
        doReturn(authResources).when(consentResource).getAuthorizationResources();
        doReturn(null).when(consentResource).getClientID();

        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(TestConstants.SAMPLE_USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentValidateDataMock).getClientId();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertFalse(consentValidationResultMock.isValid());
        Assert.assertEquals(consentValidationResultMock.getErrorCode(), ResponseStatus.FORBIDDEN.getReasonPhrase());
        Assert.assertEquals(consentValidationResultMock.getErrorMessage(), "Invalid Client Id");
        Assert.assertEquals(consentValidationResultMock.getHttpCode(), HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void testValidateWithClientIdMismatch() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(TestConstants.VALID_INITIATION).when(consentResource).getReceipt();
        ArrayList<AuthorizationResource> authResources = TestUtil.getSampleAuthorizationResourceArray(
                TestConstants.SAMPLE_CONSENT_ID, TestConstants.SAMPLE_AUTH_ID);
        doReturn(authResources).when(consentResource).getAuthorizationResources();
        doReturn("Invalid_Client").when(consentResource).getClientID();

        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(TestConstants.SAMPLE_USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentValidateDataMock).getClientId();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertFalse(consentValidationResultMock.isValid());
        Assert.assertEquals(consentValidationResultMock.getErrorCode(), ResponseStatus.FORBIDDEN.getReasonPhrase());
        Assert.assertEquals(consentValidationResultMock.getErrorMessage(), "Invalid Client Id");
        Assert.assertEquals(consentValidationResultMock.getHttpCode(), HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void testValidate() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(TestConstants.VALID_INITIATION).when(consentResource).getReceipt();
        ArrayList<AuthorizationResource> authResources = TestUtil.getSampleAuthorizationResourceArray(
                TestConstants.SAMPLE_CONSENT_ID, TestConstants.SAMPLE_AUTH_ID);
        doReturn(authResources).when(consentResource).getAuthorizationResources();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentResource).getClientID();
        doReturn(TestConstants.ACCOUNTS).when(consentResource).getConsentType();

        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(TestConstants.SAMPLE_USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentValidateDataMock).getClientId();
        doReturn(TestConstants.SAMPLE_CONSENT_ID).when(consentValidateDataMock).getConsentId();
        doReturn(new JSONObject()).when(consentValidateDataMock).getHeaders();
        doReturn(new HashMap<>()).when(consentValidateDataMock).getResourceParams();
        doReturn("/accounts").when(consentValidateDataMock).getRequestPath();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertTrue(consentValidationResultMock.isValid());
    }

    @Test
    public void testValidateForExternalServiceError() throws JsonProcessingException {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(TestConstants.VALID_INITIATION).when(consentResource).getReceipt();
        ArrayList<AuthorizationResource> authResources = TestUtil.getSampleAuthorizationResourceArray(
                TestConstants.SAMPLE_CONSENT_ID, TestConstants.SAMPLE_AUTH_ID);
        doReturn(authResources).when(consentResource).getAuthorizationResources();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentResource).getClientID();
        doReturn(TestConstants.ACCOUNTS).when(consentResource).getConsentType();

        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(TestConstants.SAMPLE_USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentValidateDataMock).getClientId();
        doReturn(TestConstants.SAMPLE_CONSENT_ID).when(consentValidateDataMock).getConsentId();
        doReturn(new JSONObject()).when(consentValidateDataMock).getHeaders();
        doReturn(new HashMap<>()).when(consentValidateDataMock).getResourceParams();
        doReturn("/accounts").when(consentValidateDataMock).getRequestPath();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree("{" +
                    "\"errorMessage\": \"errorMessage\"," +
                    "\"errorDescription\": \"errorDescription\"" +
                "}");

        ExternalServiceResponse externalServiceResponse = new ExternalServiceResponse("testId",
                StatusEnum.ERROR, rootNode);
        externalServiceResponse.setErrorCode("400");
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils
                .invokeExternalServiceCall(any(), any())).thenReturn(externalServiceResponse);

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertFalse(consentValidationResultMock.isValid());
    }
}
