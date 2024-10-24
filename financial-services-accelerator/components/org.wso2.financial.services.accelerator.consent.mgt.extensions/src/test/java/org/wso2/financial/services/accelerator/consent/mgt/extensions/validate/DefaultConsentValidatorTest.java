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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.validate;

import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.util.TestConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.util.TestUtil;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.impl.DefaultConsentValidator;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.model.ConsentValidateData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.model.ConsentValidationResult;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * Default Consent Validator Tests.
 */
public class DefaultConsentValidatorTest {

    DefaultConsentValidator validator = new DefaultConsentValidator();
    ConsentValidateData consentValidateDataMock;
    ConsentValidationResult consentValidationResultMock;
    MockedStatic<ConsentExtensionUtils> extensionsUtilMockedStatic;

    @BeforeClass
    public void beforeTest() {
        consentValidateDataMock = mock(ConsentValidateData.class);
        consentValidationResultMock = spy(ConsentValidationResult.class);

        extensionsUtilMockedStatic = Mockito.mockStatic(ConsentExtensionUtils.class);
        extensionsUtilMockedStatic.when(() -> ConsentExtensionUtils.resolveUsernameFromUserId(anyString()))
                .thenReturn(TestConstants.SAMPLE_USER_ID);
    }

    @AfterClass
    public void afterTest() {
        extensionsUtilMockedStatic.close();
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
    public void testValidateForInvalidConsentType() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(TestConstants.VALID_INITIATION).when(consentResource).getReceipt();
        ArrayList<AuthorizationResource> authResources = TestUtil.getSampleAuthorizationResourceArray(
                TestConstants.SAMPLE_CONSENT_ID, TestConstants.SAMPLE_AUTH_ID);
        doReturn(authResources).when(consentResource).getAuthorizationResources();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentResource).getClientID();
        doReturn("vrp").when(consentResource).getConsentType();
        doReturn(TestConstants.AUTHORISED_STATUS).when(consentResource).getCurrentStatus();

        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(TestConstants.SAMPLE_USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentValidateDataMock).getClientId();
        doReturn("/accounts").when(consentValidateDataMock).getRequestPath();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertFalse(consentValidationResultMock.isValid());
        Assert.assertEquals(consentValidationResultMock.getErrorCode(),
                ResponseStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        Assert.assertEquals(consentValidationResultMock.getErrorMessage(), "Invalid consent type");
        Assert.assertEquals(consentValidationResultMock.getHttpCode(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testValidateWithInvalidAccountURI() {
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
        doReturn("/payments").when(consentValidateDataMock).getRequestPath();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertFalse(consentValidationResultMock.isValid());
        Assert.assertEquals(consentValidationResultMock.getErrorCode(), ResponseStatus.UNAUTHORIZED.getReasonPhrase());
        Assert.assertEquals(consentValidationResultMock.getErrorMessage(), "Path requested is invalid");
        Assert.assertEquals(consentValidationResultMock.getHttpCode(), HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void testValidateWithInvalidPermissionsForAccounts() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(TestConstants.ACC_INITIATION_WITH_LIMITED_PERMISSIONS).when(consentResource).getReceipt();
        ArrayList<AuthorizationResource> authResources = TestUtil.getSampleAuthorizationResourceArray(
                TestConstants.SAMPLE_CONSENT_ID, TestConstants.SAMPLE_AUTH_ID);
        doReturn(authResources).when(consentResource).getAuthorizationResources();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentResource).getClientID();
        doReturn(TestConstants.ACCOUNTS).when(consentResource).getConsentType();

        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(TestConstants.SAMPLE_USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentValidateDataMock).getClientId();
        doReturn("/accounts").when(consentValidateDataMock).getRequestPath();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertFalse(consentValidationResultMock.isValid());
        Assert.assertEquals(consentValidationResultMock.getErrorCode(), ResponseStatus.FORBIDDEN.getReasonPhrase());
        Assert.assertEquals(consentValidationResultMock.getErrorMessage(), "Permission mismatch. " +
                "Consent does not contain necessary permissions");
        Assert.assertEquals(consentValidationResultMock.getHttpCode(), HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void testValidateWithInvalidStatusForAccounts() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(TestConstants.VALID_INITIATION).when(consentResource).getReceipt();
        ArrayList<AuthorizationResource> authResources = TestUtil.getSampleAuthorizationResourceArray(
                TestConstants.SAMPLE_CONSENT_ID, TestConstants.SAMPLE_AUTH_ID);
        doReturn(authResources).when(consentResource).getAuthorizationResources();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentResource).getClientID();
        doReturn(TestConstants.ACCOUNTS).when(consentResource).getConsentType();
        doReturn(TestConstants.SAMPLE_AUTHORIZATION_STATUS).when(consentResource).getCurrentStatus();

        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(TestConstants.SAMPLE_USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentValidateDataMock).getClientId();
        doReturn("/accounts").when(consentValidateDataMock).getRequestPath();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertFalse(consentValidationResultMock.isValid());
        Assert.assertEquals(consentValidationResultMock.getErrorCode(), ResponseStatus.BAD_REQUEST.getReasonPhrase());
        Assert.assertEquals(consentValidationResultMock.getErrorMessage(), "Consent is not in the correct" +
                " state");
        Assert.assertEquals(consentValidationResultMock.getHttpCode(), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testValidateWithExpiredInitiationForAccounts() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(TestConstants.ACC_INITIATION_EXPIRED).when(consentResource).getReceipt();
        ArrayList<AuthorizationResource> authResources = TestUtil.getSampleAuthorizationResourceArray(
                TestConstants.SAMPLE_CONSENT_ID, TestConstants.SAMPLE_AUTH_ID);
        doReturn(authResources).when(consentResource).getAuthorizationResources();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentResource).getClientID();
        doReturn(TestConstants.ACCOUNTS).when(consentResource).getConsentType();
        doReturn(TestConstants.AUTHORISED_STATUS).when(consentResource).getCurrentStatus();

        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(TestConstants.SAMPLE_USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentValidateDataMock).getClientId();
        doReturn("/accounts").when(consentValidateDataMock).getRequestPath();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertFalse(consentValidationResultMock.isValid());
        Assert.assertEquals(consentValidationResultMock.getErrorCode(), ResponseStatus.BAD_REQUEST.getReasonPhrase());
        Assert.assertEquals(consentValidationResultMock.getErrorMessage(), "Provided consent is expired");
        Assert.assertEquals(consentValidationResultMock.getHttpCode(), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testValidateForAccounts() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(TestConstants.VALID_INITIATION).when(consentResource).getReceipt();
        ArrayList<AuthorizationResource> authResources = TestUtil.getSampleAuthorizationResourceArray(
                TestConstants.SAMPLE_CONSENT_ID, TestConstants.SAMPLE_AUTH_ID);
        doReturn(authResources).when(consentResource).getAuthorizationResources();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentResource).getClientID();
        doReturn(TestConstants.ACCOUNTS).when(consentResource).getConsentType();
        doReturn(TestConstants.AUTHORISED_STATUS).when(consentResource).getCurrentStatus();

        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(TestConstants.SAMPLE_USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentValidateDataMock).getClientId();
        doReturn("/accounts").when(consentValidateDataMock).getRequestPath();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertTrue(consentValidationResultMock.isValid());
    }

    @Test
    public void testValidateWithInvalidCOFURI() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(TestConstants.COF_RECEIPT).when(consentResource).getReceipt();
        ArrayList<AuthorizationResource> authResources = TestUtil.getSampleAuthorizationResourceArray(
                TestConstants.SAMPLE_CONSENT_ID, TestConstants.SAMPLE_AUTH_ID);
        doReturn(authResources).when(consentResource).getAuthorizationResources();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentResource).getClientID();
        doReturn(TestConstants.FUNDS_CONFIRMATIONS).when(consentResource).getConsentType();

        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(TestConstants.SAMPLE_USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentValidateDataMock).getClientId();
        doReturn("/payments").when(consentValidateDataMock).getRequestPath();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertFalse(consentValidationResultMock.isValid());
        Assert.assertEquals(consentValidationResultMock.getErrorCode(), ResponseStatus.UNAUTHORIZED.getReasonPhrase());
        Assert.assertEquals(consentValidationResultMock.getErrorMessage(), "Invalid request URI");
        Assert.assertEquals(consentValidationResultMock.getHttpCode(), HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void testValidateWithInvalidStatusForCOF() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(TestConstants.COF_RECEIPT).when(consentResource).getReceipt();
        ArrayList<AuthorizationResource> authResources = TestUtil.getSampleAuthorizationResourceArray(
                TestConstants.SAMPLE_CONSENT_ID, TestConstants.SAMPLE_AUTH_ID);
        doReturn(authResources).when(consentResource).getAuthorizationResources();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentResource).getClientID();
        doReturn(TestConstants.FUNDS_CONFIRMATIONS).when(consentResource).getConsentType();
        doReturn(TestConstants.SAMPLE_AUTHORIZATION_STATUS).when(consentResource).getCurrentStatus();

        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(TestConstants.SAMPLE_USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentValidateDataMock).getClientId();
        doReturn(TestConstants.COF_PATH).when(consentValidateDataMock).getRequestPath();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertFalse(consentValidationResultMock.isValid());
        Assert.assertEquals(consentValidationResultMock.getErrorCode(), ResponseStatus.BAD_REQUEST.getReasonPhrase());
        Assert.assertEquals(consentValidationResultMock.getErrorMessage(), "Consent is not in the correct" +
                " state");
        Assert.assertEquals(consentValidationResultMock.getHttpCode(), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testValidateWithExpiredInitiationForCOF() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(TestConstants.COF_RECEIPT_EXPIRED).when(consentResource).getReceipt();
        ArrayList<AuthorizationResource> authResources = TestUtil.getSampleAuthorizationResourceArray(
                TestConstants.SAMPLE_CONSENT_ID, TestConstants.SAMPLE_AUTH_ID);
        doReturn(authResources).when(consentResource).getAuthorizationResources();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentResource).getClientID();
        doReturn(TestConstants.FUNDS_CONFIRMATIONS).when(consentResource).getConsentType();
        doReturn(TestConstants.AUTHORISED_STATUS).when(consentResource).getCurrentStatus();

        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(TestConstants.SAMPLE_USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentValidateDataMock).getClientId();
        doReturn(TestConstants.COF_PATH).when(consentValidateDataMock).getRequestPath();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertFalse(consentValidationResultMock.isValid());
        Assert.assertEquals(consentValidationResultMock.getErrorCode(), ResponseStatus.UNAUTHORIZED.getReasonPhrase());
        Assert.assertEquals(consentValidationResultMock.getErrorMessage(), "Provided consent is expired");
        Assert.assertEquals(consentValidationResultMock.getHttpCode(), HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void testValidateForCOFWithoutConsentIdFromToken() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(TestConstants.COF_RECEIPT).when(consentResource).getReceipt();
        ArrayList<AuthorizationResource> authResources = TestUtil.getSampleAuthorizationResourceArray(
                TestConstants.SAMPLE_CONSENT_ID, TestConstants.SAMPLE_AUTH_ID);
        doReturn(authResources).when(consentResource).getAuthorizationResources();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentResource).getClientID();
        doReturn(TestConstants.FUNDS_CONFIRMATIONS).when(consentResource).getConsentType();
        doReturn(TestConstants.AUTHORISED_STATUS).when(consentResource).getCurrentStatus();

        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(TestConstants.SAMPLE_USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentValidateDataMock).getClientId();
        doReturn(TestConstants.COF_PATH).when(consentValidateDataMock).getRequestPath();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertFalse(consentValidationResultMock.isValid());
        Assert.assertEquals(consentValidationResultMock.getErrorCode(), ResponseStatus.BAD_REQUEST.getReasonPhrase());
        Assert.assertEquals(consentValidationResultMock.getErrorMessage(), "Consent ID mismatch");
        Assert.assertEquals(consentValidationResultMock.getHttpCode(), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testValidateForCOFWithoutConsentIdFromConsent() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(TestConstants.COF_RECEIPT).when(consentResource).getReceipt();
        ArrayList<AuthorizationResource> authResources = TestUtil.getSampleAuthorizationResourceArray(
                TestConstants.SAMPLE_CONSENT_ID, TestConstants.SAMPLE_AUTH_ID);
        doReturn(authResources).when(consentResource).getAuthorizationResources();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentResource).getClientID();
        doReturn(TestConstants.FUNDS_CONFIRMATIONS).when(consentResource).getConsentType();
        doReturn(TestConstants.AUTHORISED_STATUS).when(consentResource).getCurrentStatus();
        doReturn(null).when(consentResource).getConsentID();

        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(TestConstants.SAMPLE_USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentValidateDataMock).getClientId();
        doReturn(TestConstants.COF_PATH).when(consentValidateDataMock).getRequestPath();
        doReturn(TestConstants.SAMPLE_CONSENT_ID).when(consentValidateDataMock).getConsentId();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertFalse(consentValidationResultMock.isValid());
        Assert.assertEquals(consentValidationResultMock.getErrorCode(), ResponseStatus.BAD_REQUEST.getReasonPhrase());
        Assert.assertEquals(consentValidationResultMock.getErrorMessage(), "Consent ID mismatch");
        Assert.assertEquals(consentValidationResultMock.getHttpCode(), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testValidateForCOFConsentIdMismatch() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(TestConstants.COF_RECEIPT).when(consentResource).getReceipt();
        ArrayList<AuthorizationResource> authResources = TestUtil.getSampleAuthorizationResourceArray(
                TestConstants.SAMPLE_CONSENT_ID, TestConstants.SAMPLE_AUTH_ID);
        doReturn(authResources).when(consentResource).getAuthorizationResources();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentResource).getClientID();
        doReturn(TestConstants.FUNDS_CONFIRMATIONS).when(consentResource).getConsentType();
        doReturn(TestConstants.AUTHORISED_STATUS).when(consentResource).getCurrentStatus();
        doReturn("1234567890").when(consentResource).getConsentID();

        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(TestConstants.SAMPLE_USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentValidateDataMock).getClientId();
        doReturn(TestConstants.COF_PATH).when(consentValidateDataMock).getRequestPath();
        doReturn(TestConstants.SAMPLE_CONSENT_ID).when(consentValidateDataMock).getConsentId();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertFalse(consentValidationResultMock.isValid());
        Assert.assertEquals(consentValidationResultMock.getErrorCode(), ResponseStatus.BAD_REQUEST.getReasonPhrase());
        Assert.assertEquals(consentValidationResultMock.getErrorMessage(), "Consent ID mismatch");
        Assert.assertEquals(consentValidationResultMock.getHttpCode(), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testValidateForCOFConsentIdPayloadMismatch() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(TestConstants.COF_RECEIPT).when(consentResource).getReceipt();
        ArrayList<AuthorizationResource> authResources = TestUtil.getSampleAuthorizationResourceArray(
                TestConstants.SAMPLE_CONSENT_ID, TestConstants.SAMPLE_AUTH_ID);
        doReturn(authResources).when(consentResource).getAuthorizationResources();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentResource).getClientID();
        doReturn(TestConstants.FUNDS_CONFIRMATIONS).when(consentResource).getConsentType();
        doReturn(TestConstants.AUTHORISED_STATUS).when(consentResource).getCurrentStatus();
        doReturn("1234567890").when(consentResource).getConsentID();

        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(TestConstants.SAMPLE_USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentValidateDataMock).getClientId();
        doReturn(TestConstants.COF_PATH).when(consentValidateDataMock).getRequestPath();
        doReturn("1234567890").when(consentValidateDataMock).getConsentId();
        doReturn(new JSONObject(TestConstants.COF_SUBMISSION)).when(consentValidateDataMock).getPayload();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertFalse(consentValidationResultMock.isValid());
        Assert.assertEquals(consentValidationResultMock.getErrorCode(), ResponseStatus.BAD_REQUEST.getReasonPhrase());
        Assert.assertEquals(consentValidationResultMock.getErrorMessage(), "Invalid consent ID");
        Assert.assertEquals(consentValidationResultMock.getHttpCode(), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testValidateForCOF() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(TestConstants.COF_RECEIPT).when(consentResource).getReceipt();
        ArrayList<AuthorizationResource> authResources = TestUtil.getSampleAuthorizationResourceArray(
                TestConstants.SAMPLE_CONSENT_ID, TestConstants.SAMPLE_AUTH_ID);
        doReturn(authResources).when(consentResource).getAuthorizationResources();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentResource).getClientID();
        doReturn(TestConstants.FUNDS_CONFIRMATIONS).when(consentResource).getConsentType();
        doReturn(TestConstants.AUTHORISED_STATUS).when(consentResource).getCurrentStatus();
        doReturn(TestConstants.SAMPLE_CONSENT_ID).when(consentResource).getConsentID();

        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(TestConstants.SAMPLE_USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentValidateDataMock).getClientId();
        doReturn(TestConstants.COF_PATH).when(consentValidateDataMock).getRequestPath();
        doReturn(TestConstants.SAMPLE_CONSENT_ID).when(consentValidateDataMock).getConsentId();
        doReturn(new JSONObject(TestConstants.COF_SUBMISSION)).when(consentValidateDataMock).getPayload();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertTrue(consentValidationResultMock.isValid());
    }

    @Test
    public void testValidateForPaymentWithoutConsentIDFromToken() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(TestConstants.PAYMENT_INITIATION).when(consentResource).getReceipt();
        ArrayList<AuthorizationResource> authResources = TestUtil.getSampleAuthorizationResourceArray(
                TestConstants.SAMPLE_CONSENT_ID, TestConstants.SAMPLE_AUTH_ID);
        doReturn(authResources).when(consentResource).getAuthorizationResources();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentResource).getClientID();
        doReturn(TestConstants.PAYMENTS).when(consentResource).getConsentType();
        doReturn(TestConstants.AUTHORISED_STATUS).when(consentResource).getCurrentStatus();

        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(TestConstants.SAMPLE_USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentValidateDataMock).getClientId();
        doReturn(null).when(consentValidateDataMock).getConsentId();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertFalse(consentValidationResultMock.isValid());
        Assert.assertEquals(consentValidationResultMock.getErrorCode(), ResponseStatus.BAD_REQUEST.getReasonPhrase());
        Assert.assertEquals(consentValidationResultMock.getErrorMessage(), "Consent ID mismatch");
        Assert.assertEquals(consentValidationResultMock.getHttpCode(), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testValidateForPaymentWithoutConsentIDFromConsent() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(TestConstants.PAYMENT_INITIATION).when(consentResource).getReceipt();
        ArrayList<AuthorizationResource> authResources = TestUtil.getSampleAuthorizationResourceArray(
                TestConstants.SAMPLE_CONSENT_ID, TestConstants.SAMPLE_AUTH_ID);
        doReturn(authResources).when(consentResource).getAuthorizationResources();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentResource).getClientID();
        doReturn(TestConstants.PAYMENTS).when(consentResource).getConsentType();
        doReturn(TestConstants.AUTHORISED_STATUS).when(consentResource).getCurrentStatus();
        doReturn(null).when(consentResource).getConsentID();

        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(TestConstants.SAMPLE_USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentValidateDataMock).getClientId();
        doReturn(TestConstants.SAMPLE_CONSENT_ID).when(consentValidateDataMock).getConsentId();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertFalse(consentValidationResultMock.isValid());
        Assert.assertEquals(consentValidationResultMock.getErrorCode(), ResponseStatus.BAD_REQUEST.getReasonPhrase());
        Assert.assertEquals(consentValidationResultMock.getErrorMessage(), "Consent ID mismatch");
        Assert.assertEquals(consentValidationResultMock.getHttpCode(), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testValidateForPaymentConsentIDMismatch() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(TestConstants.PAYMENT_INITIATION).when(consentResource).getReceipt();
        ArrayList<AuthorizationResource> authResources = TestUtil.getSampleAuthorizationResourceArray(
                TestConstants.SAMPLE_CONSENT_ID, TestConstants.SAMPLE_AUTH_ID);
        doReturn(authResources).when(consentResource).getAuthorizationResources();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentResource).getClientID();
        doReturn(TestConstants.PAYMENTS).when(consentResource).getConsentType();
        doReturn(TestConstants.AUTHORISED_STATUS).when(consentResource).getCurrentStatus();
        doReturn("1234567890").when(consentResource).getConsentID();

        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(TestConstants.SAMPLE_USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentValidateDataMock).getClientId();
        doReturn(TestConstants.SAMPLE_CONSENT_ID).when(consentValidateDataMock).getConsentId();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertFalse(consentValidationResultMock.isValid());
        Assert.assertEquals(consentValidationResultMock.getErrorCode(), ResponseStatus.BAD_REQUEST.getReasonPhrase());
        Assert.assertEquals(consentValidationResultMock.getErrorMessage(), "Consent ID mismatch");
        Assert.assertEquals(consentValidationResultMock.getHttpCode(), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testValidateForPaymentInvalidStatus() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(TestConstants.PAYMENT_INITIATION).when(consentResource).getReceipt();
        ArrayList<AuthorizationResource> authResources = TestUtil.getSampleAuthorizationResourceArray(
                TestConstants.SAMPLE_CONSENT_ID, TestConstants.SAMPLE_AUTH_ID);
        doReturn(authResources).when(consentResource).getAuthorizationResources();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentResource).getClientID();
        doReturn(TestConstants.PAYMENTS).when(consentResource).getConsentType();
        doReturn(TestConstants.SAMPLE_AUTHORIZATION_STATUS).when(consentResource).getCurrentStatus();
        doReturn(TestConstants.SAMPLE_CONSENT_ID).when(consentResource).getConsentID();

        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(TestConstants.SAMPLE_USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentValidateDataMock).getClientId();
        doReturn(TestConstants.SAMPLE_CONSENT_ID).when(consentValidateDataMock).getConsentId();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertFalse(consentValidationResultMock.isValid());
        Assert.assertEquals(consentValidationResultMock.getErrorCode(), ResponseStatus.BAD_REQUEST.getReasonPhrase());
        Assert.assertEquals(consentValidationResultMock.getErrorMessage(), "Consent is not in the correct" +
                " state");
        Assert.assertEquals(consentValidationResultMock.getHttpCode(), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testValidateForPaymentWithoutDataPayload() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(TestConstants.PAYMENT_INITIATION).when(consentResource).getReceipt();
        ArrayList<AuthorizationResource> authResources = TestUtil.getSampleAuthorizationResourceArray(
                TestConstants.SAMPLE_CONSENT_ID, TestConstants.SAMPLE_AUTH_ID);
        doReturn(authResources).when(consentResource).getAuthorizationResources();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentResource).getClientID();
        doReturn(TestConstants.PAYMENTS).when(consentResource).getConsentType();
        doReturn(TestConstants.AUTHORISED_STATUS).when(consentResource).getCurrentStatus();
        doReturn(TestConstants.SAMPLE_CONSENT_ID).when(consentResource).getConsentID();

        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(TestConstants.SAMPLE_USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentValidateDataMock).getClientId();
        doReturn(TestConstants.SAMPLE_CONSENT_ID).when(consentValidateDataMock).getConsentId();
        doReturn(new JSONObject(TestConstants.PAYMENT_SUBMISSION_WITHOUT_DATA)).when(consentValidateDataMock)
                .getPayload();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertFalse(consentValidationResultMock.isValid());
        Assert.assertEquals(consentValidationResultMock.getErrorCode(), ResponseStatus.BAD_REQUEST.getReasonPhrase());
        Assert.assertEquals(consentValidationResultMock.getErrorMessage(), "Invalid Submission payload Data " +
                        "Object found");
        Assert.assertEquals(consentValidationResultMock.getHttpCode(), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testValidateForPaymentMismatchConsentIDPayload() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(TestConstants.PAYMENT_INITIATION).when(consentResource).getReceipt();
        ArrayList<AuthorizationResource> authResources = TestUtil.getSampleAuthorizationResourceArray(
                TestConstants.SAMPLE_CONSENT_ID, TestConstants.SAMPLE_AUTH_ID);
        doReturn(authResources).when(consentResource).getAuthorizationResources();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentResource).getClientID();
        doReturn(TestConstants.PAYMENTS).when(consentResource).getConsentType();
        doReturn(TestConstants.AUTHORISED_STATUS).when(consentResource).getCurrentStatus();
        doReturn("123456790088").when(consentResource).getConsentID();

        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(TestConstants.SAMPLE_USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentValidateDataMock).getClientId();
        doReturn("123456790088").when(consentValidateDataMock).getConsentId();
        doReturn(new JSONObject(TestConstants.PAYMENT_SUBMISSION)).when(consentValidateDataMock)
                .getPayload();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertFalse(consentValidationResultMock.isValid());
        Assert.assertEquals(consentValidationResultMock.getErrorCode(), ResponseStatus.BAD_REQUEST.getReasonPhrase());
        Assert.assertEquals(consentValidationResultMock.getErrorMessage(), "Invalid consent ID");
        Assert.assertEquals(consentValidationResultMock.getHttpCode(), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testValidateForPaymentWithoutInitiationPayload() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(TestConstants.PAYMENT_INITIATION).when(consentResource).getReceipt();
        ArrayList<AuthorizationResource> authResources = TestUtil.getSampleAuthorizationResourceArray(
                TestConstants.SAMPLE_CONSENT_ID, TestConstants.SAMPLE_AUTH_ID);
        doReturn(authResources).when(consentResource).getAuthorizationResources();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentResource).getClientID();
        doReturn(TestConstants.PAYMENTS).when(consentResource).getConsentType();
        doReturn(TestConstants.AUTHORISED_STATUS).when(consentResource).getCurrentStatus();
        doReturn(TestConstants.SAMPLE_CONSENT_ID).when(consentResource).getConsentID();

        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(TestConstants.SAMPLE_USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentValidateDataMock).getClientId();
        doReturn(TestConstants.SAMPLE_CONSENT_ID).when(consentValidateDataMock).getConsentId();
        doReturn(new JSONObject(TestConstants.PAYMENT_SUBMISSION_WITHOUT_INITIATION)).when(consentValidateDataMock)
                .getPayload();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertFalse(consentValidationResultMock.isValid());
        Assert.assertEquals(consentValidationResultMock.getErrorCode(), ResponseStatus.BAD_REQUEST.getReasonPhrase());
        Assert.assertEquals(consentValidationResultMock.getErrorMessage(), "Invalid Submission payload " +
                "Initiation Object found");
        Assert.assertEquals(consentValidationResultMock.getHttpCode(), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testValidateForPaymentWithMismatchInitiationPayload() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(TestConstants.PAYMENT_INITIATION).when(consentResource).getReceipt();
        ArrayList<AuthorizationResource> authResources = TestUtil.getSampleAuthorizationResourceArray(
                TestConstants.SAMPLE_CONSENT_ID, TestConstants.SAMPLE_AUTH_ID);
        doReturn(authResources).when(consentResource).getAuthorizationResources();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentResource).getClientID();
        doReturn(TestConstants.PAYMENTS).when(consentResource).getConsentType();
        doReturn(TestConstants.AUTHORISED_STATUS).when(consentResource).getCurrentStatus();
        doReturn(TestConstants.SAMPLE_CONSENT_ID).when(consentResource).getConsentID();

        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(TestConstants.SAMPLE_USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentValidateDataMock).getClientId();
        doReturn(TestConstants.SAMPLE_CONSENT_ID).when(consentValidateDataMock).getConsentId();
        doReturn(new JSONObject(TestConstants.PAYMENT_SUBMISSION_WITH_DIFFERENT_INITIATION))
                .when(consentValidateDataMock).getPayload();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertFalse(consentValidationResultMock.isValid());
        Assert.assertEquals(consentValidationResultMock.getErrorCode(), ResponseStatus.BAD_REQUEST.getReasonPhrase());
        Assert.assertEquals(consentValidationResultMock.getErrorMessage(), "Initiation payloads does" +
                " not match");
        Assert.assertEquals(consentValidationResultMock.getHttpCode(), HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testValidateForPayments() {
        DetailedConsentResource consentResource = mock(DetailedConsentResource.class);
        doReturn(TestConstants.PAYMENT_INITIATION).when(consentResource).getReceipt();
        ArrayList<AuthorizationResource> authResources = TestUtil.getSampleAuthorizationResourceArray(
                TestConstants.SAMPLE_CONSENT_ID, TestConstants.SAMPLE_AUTH_ID);
        doReturn(authResources).when(consentResource).getAuthorizationResources();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentResource).getClientID();
        doReturn(TestConstants.PAYMENTS).when(consentResource).getConsentType();
        doReturn(TestConstants.AUTHORISED_STATUS).when(consentResource).getCurrentStatus();
        doReturn(TestConstants.SAMPLE_CONSENT_ID).when(consentResource).getConsentID();

        doReturn(consentResource).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(TestConstants.SAMPLE_USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(TestConstants.SAMPLE_CLIENT_ID).when(consentValidateDataMock).getClientId();
        doReturn(TestConstants.SAMPLE_CONSENT_ID).when(consentValidateDataMock).getConsentId();
        doReturn(new JSONObject(TestConstants.PAYMENT_SUBMISSION)).when(consentValidateDataMock).getPayload();

        validator.validate(consentValidateDataMock, consentValidationResultMock);

        Assert.assertTrue(consentValidationResultMock.isValid());
    }
}
