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

import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
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

    @BeforeClass
    public void beforeTest() {
        consentValidateDataMock = Mockito.mock(ConsentValidateData.class);
        consentValidationResultMock = Mockito.spy(ConsentValidationResult.class);

        financialServicesUtilsMockedStatic = Mockito.mockStatic(FinancialServicesUtils.class);
        financialServicesUtilsMockedStatic.when(() -> FinancialServicesUtils.resolveUsernameFromUserId(anyString()))
                .thenReturn(TestConstants.SAMPLE_USER_ID);

        serviceExtensionUtilsMockedStatic = Mockito.mockStatic(ServiceExtensionUtils.class);
        serviceExtensionUtilsMockedStatic.when(() -> ServiceExtensionUtils
                .invokeExternalServiceCall(any(), any())).thenReturn(new JSONObject("{\n" +
                "  \"responseId\": \"Ec1wMjmiG8\",\n" +
                "  \"actionStatus\": \"SUCCESS\"\n" +
                "}"));
    }

    @AfterClass
    public void afterTest() {
        financialServicesUtilsMockedStatic.close();
        serviceExtensionUtilsMockedStatic.close();
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
    public void testValidateWithStringReceipt() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn("Receipt").when(consentResource).getReceipt();
        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertFalse(consentValidationResultMock.isValid());
        Assert.assertEquals(consentValidationResultMock.getErrorCode(),
                ResponseStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        Assert.assertTrue(consentValidationResultMock.getErrorMessage().contains("A JSONObject text must begin with"));
        Assert.assertEquals(consentValidationResultMock.getHttpCode(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
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
}
