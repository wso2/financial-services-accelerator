/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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
package com.wso2.openbanking.accelerator.consent.extensions.validate;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.CarbonUtils;
import com.wso2.openbanking.accelerator.common.util.ErrorConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentServiceUtil;
import com.wso2.openbanking.accelerator.consent.extensions.utils.ConsentExtensionDataProvider;
import com.wso2.openbanking.accelerator.consent.extensions.utils.ConsentExtensionTestConstants;
import com.wso2.openbanking.accelerator.consent.extensions.utils.ConsentExtensionTestUtils;
import com.wso2.openbanking.accelerator.consent.extensions.utils.ConsentValidateTestConstants;
import com.wso2.openbanking.accelerator.consent.extensions.validate.impl.DefaultConsentValidator;
import com.wso2.openbanking.accelerator.consent.extensions.validate.impl.VRPSubmissionPayloadValidator;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidateData;
import com.wso2.openbanking.accelerator.consent.extensions.validate.model.ConsentValidationResult;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.joda.time.Instant;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Test class for validating Variable Recurring Payment submission requests.
 */
@PrepareForTest({OpenBankingConfigParser.class, OpenBankingConfigParser.class, ConsentServiceUtil.class})
@PowerMockIgnore({"com.wso2.openbanking.accelerator.consent.extensions.common.*", "net.minidev.*",
        "jdk.internal.reflect.*"})
public class VRPSubmissionTest {
    VRPSubmissionPayloadValidator validator = new VRPSubmissionPayloadValidator();
    DefaultConsentValidator consentValidator;
    @Mock
    ConsentValidateData consentValidateDataMock;
    @Mock
    DetailedConsentResource detailedConsentResourceMock;
    @Mock
    ConsentCoreServiceImpl consentCoreServiceMock;
    @Mock
    ConsentValidationResult consentValidationResultMock;
    Map<String, String> resourceParams = new HashMap<>();
    JSONObject headers = new JSONObject();
    private static Map<String, String> configMap;
    Map<String, String> consentAttributes = new HashMap<>();
    ArrayList<AuthorizationResource> authorizationResources = new ArrayList<AuthorizationResource>();

    @BeforeClass
    public void initClass() throws ReflectiveOperationException {
        MockitoAnnotations.initMocks(this);

        //to execute util class initialization
        new CarbonUtils();
        System.setProperty("some.property", "property.value");
        System.setProperty("carbon.home", ".");
        ConsentExtensionTestUtils.injectEnvironmentVariable("CARBON_HOME", ".");

        configMap = new HashMap<>();
        configMap.put("ErrorURL", "https://localhost:8243/error");

        consentValidator = new DefaultConsentValidator();
        consentValidateDataMock = mock(ConsentValidateData.class);
        authorizationResources.add(getAuthorizationResource());
        detailedConsentResourceMock = mock(DetailedConsentResource.class);
        consentCoreServiceMock = mock(ConsentCoreServiceImpl.class);
    }

    @BeforeMethod
    public void initMethod() {

        OpenBankingConfigParser openBankingConfigParserMock =  mock(OpenBankingConfigParser.class);
        doReturn(configMap).when(openBankingConfigParserMock).getConfiguration();

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }
    @Test
    public void testValidateInitiation() throws ParseException {

        JSONObject initPayload = ConsentExtensionTestUtils.getJsonPayload(
                ConsentValidateTestConstants.VRP_INITIATION);
        JSONObject subPayload = ConsentExtensionTestUtils.getJsonPayload(
                ConsentValidateTestConstants.VRP_SUBMISSION);

        JSONObject validationResult = validator.validateInitiation(
                ConsentExtensionTestUtils.getInitiationPayload(subPayload),
                ConsentExtensionTestUtils.getInitiationPayload(initPayload));

        Assert.assertTrue((Boolean) validationResult.get(ConsentExtensionConstants.IS_VALID_PAYLOAD));
    }

    @Test
    public void testCreditorAccInInstruction() throws ParseException {

        JSONObject initPayload = ConsentExtensionTestUtils.getJsonPayload(
                ConsentValidateTestConstants.VRP_INITIATION_WITHOUT_CREDITOR_ACC);
        JSONObject subPayload = ConsentExtensionTestUtils.getJsonPayload(
                ConsentValidateTestConstants.VRP_SUBMISSION);

        JSONObject validationResult = validator.validateCreditorAcc(
                ConsentExtensionTestUtils.getInitiationPayload(subPayload),
                ConsentExtensionTestUtils.getInitiationPayload(initPayload));

        Assert.assertTrue((Boolean) validationResult.get(ConsentExtensionConstants.IS_VALID_PAYLOAD));
    }

    @Test
    public void testValidateVRPSubmission() throws ParseException, ConsentManagementException {

        doReturn(authorizationResources).when(detailedConsentResourceMock).getAuthorizationResources();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(detailedConsentResourceMock).getClientID();
        doReturn(detailedConsentResourceMock).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(ConsentExtensionConstants.VRP).when(detailedConsentResourceMock).getConsentType();
        doReturn(ConsentValidateTestConstants.VRP_INITIATION).when(detailedConsentResourceMock).getReceipt();
        doReturn(ConsentExtensionConstants.AUTHORIZED_STATUS).when(detailedConsentResourceMock).getCurrentStatus();

        doReturn(getVRPConsentAttributes()).when(detailedConsentResourceMock).getConsentAttributes();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(detailedConsentResourceMock).getConsentID();
        doReturn(ConsentValidateTestConstants.USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(consentValidateDataMock).getClientId();

        doReturn(ConsentValidateTestConstants.VRP_PATH).when(consentValidateDataMock).getRequestPath();
        doReturn(resourceParams).when(consentValidateDataMock).getResourceParams();
        doReturn(headers).when(consentValidateDataMock).getHeaders();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(consentValidateDataMock).getConsentId();
        JSONObject submissionPayload = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                .parse(ConsentValidateTestConstants.VRP_SUBMISSION);
        doReturn(submissionPayload).when(consentValidateDataMock).getPayload();

        doReturn(ConsentExtensionTestUtils.getConsentAttributes("vrp"))
                .when(consentCoreServiceMock).getConsentAttributes(Mockito.anyString());
        doReturn(true).when(consentCoreServiceMock).deleteConsentAttributes(Mockito.anyString(),
                Mockito.<ArrayList<String>>anyObject());
        doReturn(true).when(consentCoreServiceMock).storeConsentAttributes(Mockito.anyString(),
                Mockito.<Map<String, String>>anyObject());

        PowerMockito.mockStatic(ConsentServiceUtil.class);
        PowerMockito.when(ConsentServiceUtil.getConsentService()).thenReturn(consentCoreServiceMock);

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();
        consentValidator.validate(consentValidateDataMock, consentValidationResult);

        Assert.assertTrue(consentValidationResult.isValid());
    }
    @Test
    public void testValidateVRPSubmissionWithoutRisk() throws ParseException, ConsentManagementException {

        doReturn(authorizationResources).when(detailedConsentResourceMock).getAuthorizationResources();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(detailedConsentResourceMock).getClientID();
        doReturn(detailedConsentResourceMock).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(ConsentExtensionConstants.VRP).when(detailedConsentResourceMock).getConsentType();
        doReturn(ConsentValidateTestConstants.VRP_INITIATION).when(detailedConsentResourceMock).getReceipt();
        doReturn(ConsentExtensionConstants.AUTHORIZED_STATUS).when(detailedConsentResourceMock).getCurrentStatus();

        doReturn(getVRPConsentAttributes()).when(detailedConsentResourceMock).getConsentAttributes();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(detailedConsentResourceMock).getConsentID();
        doReturn(ConsentValidateTestConstants.USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(consentValidateDataMock).getClientId();

        doReturn(ConsentValidateTestConstants.VRP_PATH).when(consentValidateDataMock).getRequestPath();
        doReturn(resourceParams).when(consentValidateDataMock).getResourceParams();
        doReturn(headers).when(consentValidateDataMock).getHeaders();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(consentValidateDataMock).getConsentId();
        JSONObject submissionPayload = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                .parse(ConsentValidateTestConstants.VRP_SUBMISSION_WITHOUT_RISK);
        doReturn(submissionPayload).when(consentValidateDataMock).getPayload();

        doReturn(ConsentExtensionTestUtils.getConsentAttributes("vrp"))
                .when(consentCoreServiceMock).getConsentAttributes(Mockito.anyString());
        doReturn(true).when(consentCoreServiceMock).deleteConsentAttributes(Mockito.anyString(),
                Mockito.<ArrayList<String>>anyObject());
        doReturn(true).when(consentCoreServiceMock).storeConsentAttributes(Mockito.anyString(),
                Mockito.<Map<String, String>>anyObject());

        PowerMockito.mockStatic(ConsentServiceUtil.class);
        PowerMockito.when(ConsentServiceUtil.getConsentService()).thenReturn(consentCoreServiceMock);

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();
        consentValidator.validate(consentValidateDataMock, consentValidationResult);

        Assert.assertFalse(consentValidationResult.isValid());
        Assert.assertEquals(consentValidationResult.getErrorMessage(), ErrorConstants.RISK_NOT_FOUND);
        Assert.assertEquals(consentValidationResult.getErrorCode(), ErrorConstants.FIELD_MISSING);
        Assert.assertEquals(consentValidationResult.getHttpCode(), 400);
    }

    @Test
    public void testConsentValidateWithUserIdMismatch() {

        doReturn(authorizationResources).when(detailedConsentResourceMock).getAuthorizationResources();
        doReturn(detailedConsentResourceMock).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(ConsentExtensionTestConstants.VALID_INITIATION_OBJECT).when(detailedConsentResourceMock)
                .getReceipt();
        doReturn(resourceParams).when(consentValidateDataMock).getResourceParams();
        doReturn(headers).when(consentValidateDataMock).getHeaders();
        doReturn("psu1@wso2.com").when(consentValidateDataMock).getUserId();

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();
        consentValidator.validate(consentValidateDataMock, consentValidationResult);

        Assert.assertFalse(consentValidationResult.isValid());
        Assert.assertEquals(consentValidationResult.getErrorMessage(), ErrorConstants.INVALID_USER_ID);;
        Assert.assertEquals(consentValidationResult.getErrorCode(), ErrorConstants.RESOURCE_CONSENT_MISMATCH);
        Assert.assertEquals(consentValidationResult.getHttpCode(), 400);
    }

    @Test
    public void testValidateVRPSubmissionWithInvalidStatus() {

        doReturn(authorizationResources).when(detailedConsentResourceMock).getAuthorizationResources();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(detailedConsentResourceMock).getClientID();
        doReturn(authorizationResources).when(detailedConsentResourceMock).getAuthorizationResources();
        doReturn(detailedConsentResourceMock).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(ConsentExtensionConstants.VRP).when(detailedConsentResourceMock).getConsentType();
        doReturn(ConsentExtensionTestConstants.VALID_INITIATION_OBJECT).when(detailedConsentResourceMock)
                .getReceipt();
        doReturn(ConsentExtensionConstants.AWAITING_AUTH_STATUS).when(detailedConsentResourceMock).getCurrentStatus();
        doReturn(ConsentValidateTestConstants.VRP_PATH).when(consentValidateDataMock).getRequestPath();
        doReturn(ConsentValidateTestConstants.USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(consentValidateDataMock).getClientId();

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();
        consentValidator.validate(consentValidateDataMock, consentValidationResult);

        Assert.assertFalse(consentValidationResult.isValid());
        Assert.assertEquals(consentValidationResult.getErrorMessage(), ErrorConstants.VRP_CONSENT_STATUS_INVALID);
        Assert.assertEquals(consentValidationResult.getErrorCode(), ErrorConstants.RESOURCE_INVALID_CONSENT_STATUS);
        Assert.assertEquals(consentValidationResult.getHttpCode(), 400);
    }

    @Test
    public void testValidateVRPSubmissionWithInvalidInstruction() throws ParseException, ConsentManagementException {

        doReturn(authorizationResources).when(detailedConsentResourceMock).getAuthorizationResources();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(detailedConsentResourceMock).getClientID();
        doReturn(detailedConsentResourceMock).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(ConsentExtensionConstants.VRP).when(detailedConsentResourceMock).getConsentType();
        doReturn(ConsentValidateTestConstants.VRP_INITIATION).when(detailedConsentResourceMock).getReceipt();
        doReturn(ConsentExtensionConstants.AUTHORIZED_STATUS).when(detailedConsentResourceMock).getCurrentStatus();

        doReturn(getVRPConsentAttributes()).when(detailedConsentResourceMock).getConsentAttributes();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(detailedConsentResourceMock).getConsentID();
        doReturn(ConsentValidateTestConstants.USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(consentValidateDataMock).getClientId();

        doReturn(ConsentValidateTestConstants.VRP_PATH).when(consentValidateDataMock).getRequestPath();
        doReturn(resourceParams).when(consentValidateDataMock).getResourceParams();
        doReturn(headers).when(consentValidateDataMock).getHeaders();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(consentValidateDataMock).getConsentId();
        JSONObject submissionPayload = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                .parse(ConsentValidateTestConstants.VRP_SUBMISSION_WITH_INVALID_INSTRUCTION);
        doReturn(submissionPayload).when(consentValidateDataMock).getPayload();

        doReturn(ConsentExtensionTestUtils.getConsentAttributes("vrp"))
                .when(consentCoreServiceMock).getConsentAttributes(Mockito.anyString());
        doReturn(true).when(consentCoreServiceMock).deleteConsentAttributes(Mockito.anyString(),
                Mockito.<ArrayList<String>>anyObject());
        doReturn(true).when(consentCoreServiceMock).storeConsentAttributes(Mockito.anyString(),
                Mockito.<Map<String, String>>anyObject());

        PowerMockito.mockStatic(ConsentServiceUtil.class);
        PowerMockito.when(ConsentServiceUtil.getConsentService()).thenReturn(consentCoreServiceMock);

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();
        consentValidator.validate(consentValidateDataMock, consentValidationResult);

        Assert.assertFalse(consentValidationResult.isValid());
        Assert.assertEquals(consentValidationResult.getErrorMessage(),
                ErrorConstants.CREDITOR_ACC_SCHEME_NAME_MISMATCH);
        Assert.assertEquals(consentValidationResult.getErrorCode(), ErrorConstants.RESOURCE_CONSENT_MISMATCH);
        Assert.assertEquals(consentValidationResult.getHttpCode(), 400);
    }

    @Test
    public void testValidateVRPSubmissionWithInvalidRisk() throws ParseException, ConsentManagementException {

        doReturn(authorizationResources).when(detailedConsentResourceMock).getAuthorizationResources();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(detailedConsentResourceMock).getClientID();
        doReturn(detailedConsentResourceMock).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(ConsentExtensionConstants.VRP).when(detailedConsentResourceMock).getConsentType();
        doReturn(ConsentValidateTestConstants.VRP_INITIATION).when(detailedConsentResourceMock).getReceipt();
        doReturn(ConsentExtensionConstants.AUTHORIZED_STATUS).when(detailedConsentResourceMock).getCurrentStatus();

        doReturn(getVRPConsentAttributes()).when(detailedConsentResourceMock).getConsentAttributes();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(detailedConsentResourceMock).getConsentID();
        doReturn(ConsentValidateTestConstants.USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(consentValidateDataMock).getClientId();

        doReturn(ConsentValidateTestConstants.VRP_PATH).when(consentValidateDataMock).getRequestPath();
        doReturn(resourceParams).when(consentValidateDataMock).getResourceParams();
        doReturn(headers).when(consentValidateDataMock).getHeaders();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(consentValidateDataMock).getConsentId();
        JSONObject submissionPayload = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                .parse(ConsentValidateTestConstants.VRP_SUBMISSION_WITH_INVALID_RISK);
        doReturn(submissionPayload).when(consentValidateDataMock).getPayload();

        doReturn(ConsentExtensionTestUtils.getConsentAttributes("vrp"))
                .when(consentCoreServiceMock).getConsentAttributes(Mockito.anyString());
        doReturn(true).when(consentCoreServiceMock).deleteConsentAttributes(Mockito.anyString(),
                Mockito.<ArrayList<String>>anyObject());
        doReturn(true).when(consentCoreServiceMock).storeConsentAttributes(Mockito.anyString(),
                Mockito.<Map<String, String>>anyObject());

        PowerMockito.mockStatic(ConsentServiceUtil.class);
        PowerMockito.when(ConsentServiceUtil.getConsentService()).thenReturn(consentCoreServiceMock);

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();
        consentValidator.validate(consentValidateDataMock, consentValidationResult);

        Assert.assertFalse(consentValidationResult.isValid());
        Assert.assertEquals(consentValidationResult.getErrorMessage(), ErrorConstants.RISK_PARAMETER_MISMATCH);
        Assert.assertEquals(consentValidationResult.getErrorCode(), ErrorConstants.RESOURCE_CONSENT_MISMATCH);
        Assert.assertEquals(consentValidationResult.getHttpCode(), 400);
    }

    @Test
    public void testValidateVRPSubmissionWithoutInstruction() throws ParseException {

        doReturn(authorizationResources).when(detailedConsentResourceMock).getAuthorizationResources();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(detailedConsentResourceMock).getClientID();
        doReturn(detailedConsentResourceMock).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(ConsentExtensionConstants.VRP).when(detailedConsentResourceMock).getConsentType();
        doReturn(ConsentValidateTestConstants.VRP_INITIATION).when(detailedConsentResourceMock).getReceipt();
        doReturn(ConsentExtensionConstants.AUTHORIZED_STATUS).when(detailedConsentResourceMock).getCurrentStatus();

        doReturn(getVRPConsentAttributes()).when(detailedConsentResourceMock).getConsentAttributes();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(detailedConsentResourceMock).getConsentID();
        doReturn(ConsentValidateTestConstants.USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(consentValidateDataMock).getClientId();

        doReturn(ConsentValidateTestConstants.VRP_PATH).when(consentValidateDataMock).getRequestPath();
        doReturn(resourceParams).when(consentValidateDataMock).getResourceParams();
        doReturn(headers).when(consentValidateDataMock).getHeaders();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(consentValidateDataMock).getConsentId();
        JSONObject submissionPayload = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                .parse(ConsentValidateTestConstants.VRP_SUBMISSION_WITHOUT_INSTRUCTION);
        doReturn(submissionPayload).when(consentValidateDataMock).getPayload();

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();
        consentValidator.validate(consentValidateDataMock, consentValidationResult);

        Assert.assertFalse(consentValidationResult.isValid());
        Assert.assertEquals(consentValidationResult.getErrorMessage(), ErrorConstants.INSTRUCTION_NOT_FOUND);
        Assert.assertEquals(consentValidationResult.getErrorCode(), ErrorConstants.FIELD_MISSING);
        Assert.assertEquals(consentValidationResult.getHttpCode(), 400);
    }

    private Map<String, String> getVRPConsentAttributes() {
        consentAttributes.put(ConsentExtensionConstants.PAYMENT_TYPE, "domestic-vrp-consents");
        consentAttributes.put(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT, "100.00");
        consentAttributes.put(ConsentExtensionConstants.PERIOD_ALIGNMENT, "Consent");
        consentAttributes.put(ConsentExtensionConstants.PERIOD_TYPE, "Week");
        consentAttributes.put(ConsentExtensionConstants.LAST_PAYMENT_DATE,
                Long.toString(ConsentValidateTestConstants.EXPIRATION_DATE.toEpochSecond()));
        consentAttributes.put(ConsentExtensionConstants.AMOUNT, "100.00");

        return consentAttributes;
    }

    private AuthorizationResource getAuthorizationResource() {
        return new AuthorizationResource(ConsentValidateTestConstants.CONSENT_ID,
                ConsentValidateTestConstants.USER_ID, "awaitingAuthorization",
                ConsentValidateTestConstants.SAMPLE_AUTHORIZATION_TYPE, Instant.now().getMillis());
    }

    @Test
    public void testValidateVRPSubmissionWithoutCreditorAccount() throws ParseException, ConsentManagementException {

        doReturn(authorizationResources).when(detailedConsentResourceMock).getAuthorizationResources();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(detailedConsentResourceMock).getClientID();
        doReturn(detailedConsentResourceMock).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(ConsentExtensionConstants.VRP).when(detailedConsentResourceMock).getConsentType();
        doReturn(ConsentValidateTestConstants.VRP_INITIATION).when(detailedConsentResourceMock).getReceipt();
        doReturn(ConsentExtensionConstants.AUTHORIZED_STATUS).when(detailedConsentResourceMock).getCurrentStatus();

        doReturn(getVRPConsentAttributes()).when(detailedConsentResourceMock).getConsentAttributes();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(detailedConsentResourceMock).getConsentID();
        doReturn(ConsentValidateTestConstants.USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(consentValidateDataMock).getClientId();

        doReturn(ConsentValidateTestConstants.VRP_PATH).when(consentValidateDataMock).getRequestPath();
        doReturn(resourceParams).when(consentValidateDataMock).getResourceParams();
        doReturn(headers).when(consentValidateDataMock).getHeaders();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(consentValidateDataMock).getConsentId();
        JSONObject submissionPayload = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                .parse(ConsentValidateTestConstants.VRP_SUBMISSION_WITHOUT_CREDITOR_ACC);
        doReturn(submissionPayload).when(consentValidateDataMock).getPayload();

        doReturn(ConsentExtensionTestUtils.getConsentAttributes("vrp"))
                .when(consentCoreServiceMock).getConsentAttributes(Mockito.anyString());
        doReturn(true).when(consentCoreServiceMock).deleteConsentAttributes(Mockito.anyString(),
                Mockito.<ArrayList<String>>anyObject());
        doReturn(true).when(consentCoreServiceMock).storeConsentAttributes(Mockito.anyString(),
                Mockito.<Map<String, String>>anyObject());

        PowerMockito.mockStatic(ConsentServiceUtil.class);
        PowerMockito.when(ConsentServiceUtil.getConsentService()).thenReturn(consentCoreServiceMock);

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();
        consentValidator.validate(consentValidateDataMock, consentValidationResult);

        Assert.assertFalse(consentValidationResult.isValid());
        Assert.assertEquals(consentValidationResult.getErrorMessage(), ErrorConstants.CREDITOR_ACC_NOT_FOUND);
        Assert.assertEquals(consentValidationResult.getErrorCode(), ErrorConstants.FIELD_MISSING);
        Assert.assertEquals(consentValidationResult.getHttpCode(), 400);
    }

    @Test
    public void testValidateVRPSubmissionWithDebtorAccountMisMatch() throws ParseException,
            ConsentManagementException {

        doReturn(authorizationResources).when(detailedConsentResourceMock).getAuthorizationResources();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(detailedConsentResourceMock).getClientID();
        doReturn(detailedConsentResourceMock).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(ConsentExtensionConstants.VRP).when(detailedConsentResourceMock).getConsentType();
        doReturn(ConsentValidateTestConstants.VRP_INITIATION).when(detailedConsentResourceMock).getReceipt();
        doReturn(ConsentExtensionConstants.AUTHORIZED_STATUS).when(detailedConsentResourceMock).getCurrentStatus();

        doReturn(getVRPConsentAttributes()).when(detailedConsentResourceMock).getConsentAttributes();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(detailedConsentResourceMock).getConsentID();
        doReturn(ConsentValidateTestConstants.USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(consentValidateDataMock).getClientId();

        doReturn(ConsentValidateTestConstants.VRP_PATH).when(consentValidateDataMock).getRequestPath();
        doReturn(resourceParams).when(consentValidateDataMock).getResourceParams();
        doReturn(headers).when(consentValidateDataMock).getHeaders();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(consentValidateDataMock).getConsentId();
        JSONObject submissionPayload = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                .parse(ConsentValidateTestConstants.VRP_SUBMISSION_DEBTOR_ACC_MISMATCH);
        doReturn(submissionPayload).when(consentValidateDataMock).getPayload();

        doReturn(ConsentExtensionTestUtils.getConsentAttributes("vrp"))
                .when(consentCoreServiceMock).getConsentAttributes(Mockito.anyString());
        doReturn(true).when(consentCoreServiceMock).deleteConsentAttributes(Mockito.anyString(),
                Mockito.<ArrayList<String>>anyObject());
        doReturn(true).when(consentCoreServiceMock).storeConsentAttributes(Mockito.anyString(),
                Mockito.<Map<String, String>>anyObject());

        PowerMockito.mockStatic(ConsentServiceUtil.class);
        PowerMockito.when(ConsentServiceUtil.getConsentService()).thenReturn(consentCoreServiceMock);

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();
        consentValidator.validate(consentValidateDataMock, consentValidationResult);

        Assert.assertFalse(consentValidationResult.isValid());
        Assert.assertEquals(consentValidationResult.getErrorMessage(), ErrorConstants.DEBTOR_ACC_NOT_FOUND);
        Assert.assertEquals(consentValidationResult.getErrorCode(), ErrorConstants.FIELD_MISSING);
        Assert.assertEquals(consentValidationResult.getHttpCode(), 400);
    }

    @Test
    public void testValidateVRPSubmissionWithoutRemittanceInfo() throws ParseException,
            ConsentManagementException {

        doReturn(authorizationResources).when(detailedConsentResourceMock).getAuthorizationResources();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(detailedConsentResourceMock).getClientID();
        doReturn(detailedConsentResourceMock).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(ConsentExtensionConstants.VRP).when(detailedConsentResourceMock).getConsentType();
        doReturn(ConsentValidateTestConstants.VRP_INITIATION).when(detailedConsentResourceMock).getReceipt();
        doReturn(ConsentExtensionConstants.AUTHORIZED_STATUS).when(detailedConsentResourceMock).getCurrentStatus();

        doReturn(getVRPConsentAttributes()).when(detailedConsentResourceMock).getConsentAttributes();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(detailedConsentResourceMock).getConsentID();
        doReturn(ConsentValidateTestConstants.USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(consentValidateDataMock).getClientId();

        doReturn(ConsentValidateTestConstants.VRP_PATH).when(consentValidateDataMock).getRequestPath();
        doReturn(resourceParams).when(consentValidateDataMock).getResourceParams();
        doReturn(headers).when(consentValidateDataMock).getHeaders();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(consentValidateDataMock).getConsentId();
        JSONObject submissionPayload = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                .parse(ConsentValidateTestConstants.VRP_SUBMISSION_WITHOUT_REMITTANCE_INFO);
        doReturn(submissionPayload).when(consentValidateDataMock).getPayload();

        doReturn(ConsentExtensionTestUtils.getConsentAttributes("vrp"))
                .when(consentCoreServiceMock).getConsentAttributes(Mockito.anyString());
        doReturn(true).when(consentCoreServiceMock).deleteConsentAttributes(Mockito.anyString(),
                Mockito.<ArrayList<String>>anyObject());
        doReturn(true).when(consentCoreServiceMock).storeConsentAttributes(Mockito.anyString(),
                Mockito.<Map<String, String>>anyObject());

        PowerMockito.mockStatic(ConsentServiceUtil.class);
        PowerMockito.when(ConsentServiceUtil.getConsentService()).thenReturn(consentCoreServiceMock);

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();
        consentValidator.validate(consentValidateDataMock, consentValidationResult);

        Assert.assertFalse(consentValidationResult.isValid());
        Assert.assertEquals(consentValidationResult.getErrorMessage(),
                ErrorConstants.REMITTANCE_INFO_NOT_FOUND);
        Assert.assertEquals(consentValidationResult.getErrorCode(), ErrorConstants.FIELD_MISSING);
        Assert.assertEquals(consentValidationResult.getHttpCode(), 400);
    }

    @Test
    public void testValidateVRPSubmissionWithRemittanceInfoMisMatch() throws ParseException,
            ConsentManagementException {

        doReturn(authorizationResources).when(detailedConsentResourceMock).getAuthorizationResources();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(detailedConsentResourceMock).getClientID();
        doReturn(detailedConsentResourceMock).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(ConsentExtensionConstants.VRP).when(detailedConsentResourceMock).getConsentType();
        doReturn(ConsentValidateTestConstants.VRP_INITIATION).when(detailedConsentResourceMock).getReceipt();
        doReturn(ConsentExtensionConstants.AUTHORIZED_STATUS).when(detailedConsentResourceMock).getCurrentStatus();

        doReturn(getVRPConsentAttributes()).when(detailedConsentResourceMock).getConsentAttributes();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(detailedConsentResourceMock).getConsentID();
        doReturn(ConsentValidateTestConstants.USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(consentValidateDataMock).getClientId();

        doReturn(ConsentValidateTestConstants.VRP_PATH).when(consentValidateDataMock).getRequestPath();
        doReturn(resourceParams).when(consentValidateDataMock).getResourceParams();
        doReturn(headers).when(consentValidateDataMock).getHeaders();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(consentValidateDataMock).getConsentId();
        JSONObject submissionPayload = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                .parse(ConsentValidateTestConstants.VRP_SUBMISSION_WITHOUT_REMITTANCE_INFO_MISMATCH);
        doReturn(submissionPayload).when(consentValidateDataMock).getPayload();

        doReturn(ConsentExtensionTestUtils.getConsentAttributes("vrp"))
                .when(consentCoreServiceMock).getConsentAttributes(Mockito.anyString());
        doReturn(true).when(consentCoreServiceMock).deleteConsentAttributes(Mockito.anyString(),
                Mockito.<ArrayList<String>>anyObject());
        doReturn(true).when(consentCoreServiceMock).storeConsentAttributes(Mockito.anyString(),
                Mockito.<Map<String, String>>anyObject());

        PowerMockito.mockStatic(ConsentServiceUtil.class);
        PowerMockito.when(ConsentServiceUtil.getConsentService()).thenReturn(consentCoreServiceMock);

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();
        consentValidator.validate(consentValidateDataMock, consentValidationResult);

        Assert.assertFalse(consentValidationResult.isValid());
        Assert.assertEquals(consentValidationResult.getErrorMessage(),
                ErrorConstants.REMITTANCE_INFO_MISMATCH);
        Assert.assertEquals(consentValidationResult.getErrorCode(), ErrorConstants.RESOURCE_CONSENT_MISMATCH);
        Assert.assertEquals(consentValidationResult.getHttpCode(), 400);
    }

    @Test(dataProvider = "VRPInvalidInitiationSubmissionPayloadsDataProvider",
            dataProviderClass = ConsentExtensionDataProvider.class)
    public void testValidateVRPSubmissionForInvalidInitiation(String payload) throws ParseException {

        doReturn(authorizationResources).when(detailedConsentResourceMock).getAuthorizationResources();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(detailedConsentResourceMock).getClientID();
        doReturn(detailedConsentResourceMock).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(ConsentExtensionConstants.VRP).when(detailedConsentResourceMock).getConsentType();
        doReturn(ConsentValidateTestConstants.VRP_INITIATION).when(detailedConsentResourceMock).getReceipt();
        doReturn(ConsentExtensionConstants.AUTHORIZED_STATUS).when(detailedConsentResourceMock).getCurrentStatus();

        doReturn(getVRPConsentAttributes()).when(detailedConsentResourceMock).getConsentAttributes();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(detailedConsentResourceMock).getConsentID();
        doReturn(ConsentValidateTestConstants.USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(consentValidateDataMock).getClientId();

        doReturn(ConsentValidateTestConstants.VRP_PATH).when(consentValidateDataMock).getRequestPath();
        doReturn(resourceParams).when(consentValidateDataMock).getResourceParams();
        doReturn(headers).when(consentValidateDataMock).getHeaders();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(consentValidateDataMock).getConsentId();
        JSONObject submissionPayload = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(payload);
        doReturn(submissionPayload).when(consentValidateDataMock).getPayload();

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();
        consentValidator.validate(consentValidateDataMock, consentValidationResult);

        Assert.assertFalse(consentValidationResult.isValid());
        // Using VRPInvalidInitiationSubmissionPayloadsDataProvider dataProvider three test scenarios are been tested.
        // Relevant error messages will be returned respectively.
    }

    @Test
    public void testValidateVRPSubmissionWithIntegerInstructionIdentification() throws ParseException,
            ConsentManagementException {

        doReturn(authorizationResources).when(detailedConsentResourceMock).getAuthorizationResources();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(detailedConsentResourceMock).getClientID();
        doReturn(detailedConsentResourceMock).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(ConsentExtensionConstants.VRP).when(detailedConsentResourceMock).getConsentType();
        doReturn(ConsentValidateTestConstants.VRP_INITIATION).when(detailedConsentResourceMock).getReceipt();
        doReturn(ConsentExtensionConstants.AUTHORIZED_STATUS).when(detailedConsentResourceMock).getCurrentStatus();

        doReturn(getVRPConsentAttributes()).when(detailedConsentResourceMock).getConsentAttributes();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(detailedConsentResourceMock).getConsentID();
        doReturn(ConsentValidateTestConstants.USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(consentValidateDataMock).getClientId();

        doReturn(ConsentValidateTestConstants.VRP_PATH).when(consentValidateDataMock).getRequestPath();
        doReturn(resourceParams).when(consentValidateDataMock).getResourceParams();
        doReturn(headers).when(consentValidateDataMock).getHeaders();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(consentValidateDataMock).getConsentId();
        JSONObject submissionPayload = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                .parse(ConsentValidateTestConstants.VRP_SUBMISSION_WITH_INTEGER_INSTRUCTION_IDENTIFICATION);
        doReturn(submissionPayload).when(consentValidateDataMock).getPayload();

        doReturn(ConsentExtensionTestUtils.getConsentAttributes("vrp"))
                .when(consentCoreServiceMock).getConsentAttributes(Mockito.anyString());
        doReturn(true).when(consentCoreServiceMock).deleteConsentAttributes(Mockito.anyString(),
                Mockito.<ArrayList<String>>anyObject());
        doReturn(true).when(consentCoreServiceMock).storeConsentAttributes(Mockito.anyString(),
                Mockito.<Map<String, String>>anyObject());

        PowerMockito.mockStatic(ConsentServiceUtil.class);
        PowerMockito.when(ConsentServiceUtil.getConsentService()).thenReturn(consentCoreServiceMock);

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();
        consentValidator.validate(consentValidateDataMock, consentValidationResult);

        Assert.assertFalse(consentValidationResult.isValid());
        Assert.assertEquals(consentValidationResult.getErrorMessage(), ErrorConstants.INVALID_SUBMISSION_TYPE);
        Assert.assertEquals(consentValidationResult.getErrorCode(), ErrorConstants.FIELD_INVALID);
        Assert.assertEquals(consentValidationResult.getHttpCode(), 400);
    }

   @Test
    public void testValidateVRPSubmissionWithIntegerEndToEndIdentification() throws ParseException,
            ConsentManagementException {

        doReturn(authorizationResources).when(detailedConsentResourceMock).getAuthorizationResources();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(detailedConsentResourceMock).getClientID();
        doReturn(detailedConsentResourceMock).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(ConsentExtensionConstants.VRP).when(detailedConsentResourceMock).getConsentType();
        doReturn(ConsentValidateTestConstants.VRP_INITIATION).when(detailedConsentResourceMock).getReceipt();
        doReturn(ConsentExtensionConstants.AUTHORIZED_STATUS).when(detailedConsentResourceMock).getCurrentStatus();

        doReturn(getVRPConsentAttributes()).when(detailedConsentResourceMock).getConsentAttributes();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(detailedConsentResourceMock).getConsentID();
        doReturn(ConsentValidateTestConstants.USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(consentValidateDataMock).getClientId();

        doReturn(ConsentValidateTestConstants.VRP_PATH).when(consentValidateDataMock).getRequestPath();
        doReturn(resourceParams).when(consentValidateDataMock).getResourceParams();
        doReturn(headers).when(consentValidateDataMock).getHeaders();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(consentValidateDataMock).getConsentId();
        JSONObject submissionPayload = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                .parse(ConsentValidateTestConstants.VRP_SUBMISSION_WITH_INTEGER_END_TO_IDENTIFICATION);
        doReturn(submissionPayload).when(consentValidateDataMock).getPayload();

        doReturn(ConsentExtensionTestUtils.getConsentAttributes("vrp"))
                .when(consentCoreServiceMock).getConsentAttributes(Mockito.anyString());
        doReturn(true).when(consentCoreServiceMock).deleteConsentAttributes(Mockito.anyString(),
                Mockito.<ArrayList<String>>anyObject());
        doReturn(true).when(consentCoreServiceMock).storeConsentAttributes(Mockito.anyString(),
                Mockito.<Map<String, String>>anyObject());

        PowerMockito.mockStatic(ConsentServiceUtil.class);
        PowerMockito.when(ConsentServiceUtil.getConsentService()).thenReturn(consentCoreServiceMock);

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();
        consentValidator.validate(consentValidateDataMock, consentValidationResult);

        Assert.assertFalse(consentValidationResult.isValid());
        Assert.assertEquals(consentValidationResult.getErrorMessage(),
                ErrorConstants.INVALID_END_TO_END_IDENTIFICATION_TYPE);
        Assert.assertEquals(consentValidationResult.getErrorCode(), ErrorConstants.FIELD_INVALID);
        Assert.assertEquals(consentValidationResult.getHttpCode(), 400);
    }

    @Test
    public void testValidateVRPSubmissionWithoutDebtorAccInSubmission() throws ParseException,
            ConsentManagementException {

        doReturn(authorizationResources).when(detailedConsentResourceMock).getAuthorizationResources();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(detailedConsentResourceMock).getClientID();
        doReturn(detailedConsentResourceMock).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(ConsentExtensionConstants.VRP).when(detailedConsentResourceMock).getConsentType();
        doReturn(ConsentValidateTestConstants.VRP_INITIATION_WITHOUT_DEBTOR_ACC).when(detailedConsentResourceMock)
                .getReceipt();
        doReturn(ConsentExtensionConstants.AUTHORIZED_STATUS).when(detailedConsentResourceMock).getCurrentStatus();

        doReturn(getVRPConsentAttributes()).when(detailedConsentResourceMock).getConsentAttributes();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(detailedConsentResourceMock).getConsentID();
        doReturn(ConsentValidateTestConstants.USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(consentValidateDataMock).getClientId();

        doReturn(ConsentValidateTestConstants.VRP_PATH).when(consentValidateDataMock).getRequestPath();
        doReturn(resourceParams).when(consentValidateDataMock).getResourceParams();
        doReturn(headers).when(consentValidateDataMock).getHeaders();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(consentValidateDataMock).getConsentId();
        JSONObject submissionPayload = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                .parse(ConsentValidateTestConstants.VRP_SUBMISSION_WITH_DEBTOR_ACC);
        doReturn(submissionPayload).when(consentValidateDataMock).getPayload();

        doReturn(ConsentExtensionTestUtils.getConsentAttributes("vrp"))
                .when(consentCoreServiceMock).getConsentAttributes(Mockito.anyString());
        doReturn(true).when(consentCoreServiceMock).deleteConsentAttributes(Mockito.anyString(),
                Mockito.<ArrayList<String>>anyObject());
        doReturn(true).when(consentCoreServiceMock).storeConsentAttributes(Mockito.anyString(),
                Mockito.<Map<String, String>>anyObject());

        PowerMockito.mockStatic(ConsentServiceUtil.class);
        PowerMockito.when(ConsentServiceUtil.getConsentService()).thenReturn(consentCoreServiceMock);

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();
        consentValidator.validate(consentValidateDataMock, consentValidationResult);

        Assert.assertFalse(consentValidationResult.isValid());
        Assert.assertEquals(consentValidationResult.getErrorMessage(), ErrorConstants.DEBTOR_ACC_NOT_FOUND);
        Assert.assertEquals(consentValidationResult.getErrorCode(), ErrorConstants.FIELD_MISSING);
        Assert.assertEquals(consentValidationResult.getHttpCode(), 400);
    }

    @Test
    public void testValidateVRPSubmissionWithoutCreditorAccInInitiation() throws ParseException,
            ConsentManagementException {

        doReturn(authorizationResources).when(detailedConsentResourceMock).getAuthorizationResources();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(detailedConsentResourceMock).getClientID();
        doReturn(detailedConsentResourceMock).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(ConsentExtensionConstants.VRP).when(detailedConsentResourceMock).getConsentType();
        doReturn(ConsentValidateTestConstants.VRP_INITIATION_WITHOUT_CREDITOR_ACC).when(detailedConsentResourceMock).
                getReceipt();
        doReturn(ConsentExtensionConstants.AUTHORIZED_STATUS).when(detailedConsentResourceMock).getCurrentStatus();

        doReturn(getVRPConsentAttributes()).when(detailedConsentResourceMock).getConsentAttributes();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(detailedConsentResourceMock).getConsentID();
        doReturn(ConsentValidateTestConstants.USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(consentValidateDataMock).getClientId();

        doReturn(ConsentValidateTestConstants.VRP_PATH).when(consentValidateDataMock).getRequestPath();
        doReturn(resourceParams).when(consentValidateDataMock).getResourceParams();
        doReturn(headers).when(consentValidateDataMock).getHeaders();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(consentValidateDataMock).getConsentId();
        JSONObject submissionPayload = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                .parse(ConsentValidateTestConstants.VRP_SUBMISSION_WITH_INSTRUCTION_CREDITOR_ACC);
        doReturn(submissionPayload).when(consentValidateDataMock).getPayload();

        doReturn(ConsentExtensionTestUtils.getConsentAttributes("vrp"))
                .when(consentCoreServiceMock).getConsentAttributes(Mockito.anyString());
        doReturn(true).when(consentCoreServiceMock).deleteConsentAttributes(Mockito.anyString(),
                Mockito.<ArrayList<String>>anyObject());
        doReturn(true).when(consentCoreServiceMock).storeConsentAttributes(Mockito.anyString(),
                Mockito.<Map<String, String>>anyObject());

        PowerMockito.mockStatic(ConsentServiceUtil.class);
        PowerMockito.when(ConsentServiceUtil.getConsentService()).thenReturn(consentCoreServiceMock);

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();
        consentValidator.validate(consentValidateDataMock, consentValidationResult);

        Assert.assertFalse(consentValidationResult.isValid());
        Assert.assertEquals(consentValidationResult.getErrorMessage(), ErrorConstants.CREDITOR_ACC_NOT_FOUND);
        Assert.assertEquals(consentValidationResult.getErrorCode(), ErrorConstants.FIELD_MISSING);
        Assert.assertEquals(consentValidationResult.getHttpCode(), 400);
    }

    @Test(dataProvider = "VRPInvalidSubmissionPayloadsDataProvider",
            dataProviderClass = ConsentExtensionDataProvider.class)
    public void testValidateVRPSubmissionForInvalidInstruction(String payload) throws ParseException {

        doReturn(authorizationResources).when(detailedConsentResourceMock).getAuthorizationResources();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(detailedConsentResourceMock).getClientID();
        doReturn(detailedConsentResourceMock).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(ConsentExtensionConstants.VRP).when(detailedConsentResourceMock).getConsentType();
        doReturn(ConsentValidateTestConstants.VRP_INITIATION).when(detailedConsentResourceMock).getReceipt();
        doReturn(ConsentExtensionConstants.AUTHORIZED_STATUS).when(detailedConsentResourceMock).getCurrentStatus();

        doReturn(getVRPConsentAttributes()).when(detailedConsentResourceMock).getConsentAttributes();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(detailedConsentResourceMock).getConsentID();
        doReturn(ConsentValidateTestConstants.USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(consentValidateDataMock).getClientId();

        doReturn(ConsentValidateTestConstants.VRP_PATH).when(consentValidateDataMock).getRequestPath();
        doReturn(resourceParams).when(consentValidateDataMock).getResourceParams();
        doReturn(headers).when(consentValidateDataMock).getHeaders();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(consentValidateDataMock).getConsentId();
        JSONObject submissionPayload = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(payload);
        doReturn(submissionPayload).when(consentValidateDataMock).getPayload();

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();
        consentValidator.validate(consentValidateDataMock, consentValidationResult);

        Assert.assertFalse(consentValidationResult.isValid());
        // Using the VRPInvalidSubmissionPayloadsDataProvider dataProvider five test scenarios are been tested.
        // Relevant error messages will be returned respectively.
    }

    @Test
    public void testValidateVRPSubmissionWithInstructionRemittanceMismatch() throws ParseException,
            ConsentManagementException {

        doReturn(authorizationResources).when(detailedConsentResourceMock).getAuthorizationResources();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(detailedConsentResourceMock).getClientID();
        doReturn(detailedConsentResourceMock).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(ConsentExtensionConstants.VRP).when(detailedConsentResourceMock).getConsentType();
        doReturn(ConsentValidateTestConstants.VRP_INITIATION).when(detailedConsentResourceMock).getReceipt();
        doReturn(ConsentExtensionConstants.AUTHORIZED_STATUS).when(detailedConsentResourceMock).getCurrentStatus();

        doReturn(getVRPConsentAttributes()).when(detailedConsentResourceMock).getConsentAttributes();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(detailedConsentResourceMock).getConsentID();
        doReturn(ConsentValidateTestConstants.USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(consentValidateDataMock).getClientId();

        doReturn(ConsentValidateTestConstants.VRP_PATH).when(consentValidateDataMock).getRequestPath();
        doReturn(resourceParams).when(consentValidateDataMock).getResourceParams();
        doReturn(headers).when(consentValidateDataMock).getHeaders();
        doReturn(ConsentValidateTestConstants.CONSENT_ID).when(consentValidateDataMock).getConsentId();
        JSONObject submissionPayload = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                .parse(ConsentValidateTestConstants.VRP_SUBMISSION_WITHOUT_REMITTANCE_INFO_MISMATCH);
        doReturn(submissionPayload).when(consentValidateDataMock).getPayload();

        doReturn(ConsentExtensionTestUtils.getConsentAttributes("vrp"))
                .when(consentCoreServiceMock).getConsentAttributes(Mockito.anyString());
        doReturn(true).when(consentCoreServiceMock).deleteConsentAttributes(Mockito.anyString(),
                Mockito.<ArrayList<String>>anyObject());
        doReturn(true).when(consentCoreServiceMock).storeConsentAttributes(Mockito.anyString(),
                Mockito.<Map<String, String>>anyObject());

        PowerMockito.mockStatic(ConsentServiceUtil.class);
        PowerMockito.when(ConsentServiceUtil.getConsentService()).thenReturn(consentCoreServiceMock);

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();
        consentValidator.validate(consentValidateDataMock, consentValidationResult);

        Assert.assertFalse(consentValidationResult.isValid());
        Assert.assertEquals(consentValidationResult.getErrorMessage(), ErrorConstants.REMITTANCE_INFO_MISMATCH);
        Assert.assertEquals(consentValidationResult.getErrorCode(), ErrorConstants.RESOURCE_CONSENT_MISMATCH);
        Assert.assertEquals(consentValidationResult.getHttpCode(), 400);
    }

    @Test
    public void testConsentValidateVRPvWithInvalidConsentId() {

        doReturn(authorizationResources).when(detailedConsentResourceMock).getAuthorizationResources();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(detailedConsentResourceMock).getClientID();
        doReturn(detailedConsentResourceMock).when(consentValidateDataMock).getComprehensiveConsent();
        doReturn(ConsentExtensionConstants.VRP).when(detailedConsentResourceMock).getConsentType();
        doReturn(ConsentExtensionConstants.AUTHORIZED_STATUS).when(detailedConsentResourceMock).getCurrentStatus();
        doReturn(ConsentValidateTestConstants.INVALID_CONSENT_ID).when(detailedConsentResourceMock).getConsentID();
        doReturn(ConsentExtensionTestConstants.VALID_INITIATION_OBJECT).when(detailedConsentResourceMock)
                .getReceipt();
        doReturn(resourceParams).when(consentValidateDataMock).getResourceParams();
        doReturn(headers).when(consentValidateDataMock).getHeaders();
        doReturn(ConsentValidateTestConstants.USER_ID).when(consentValidateDataMock).getUserId();
        doReturn(ConsentValidateTestConstants.CLIENT_ID).when(consentValidateDataMock).getClientId();

        ConsentValidationResult consentValidationResult = new ConsentValidationResult();
        consentValidator.validate(consentValidateDataMock, consentValidationResult);

        Assert.assertFalse(consentValidationResult.isValid());
        Assert.assertEquals(consentValidationResult.getErrorMessage(), ErrorConstants.MSG_INVALID_CONSENT_ID);;
        Assert.assertEquals(consentValidationResult.getErrorCode(), ErrorConstants.RESOURCE_CONSENT_MISMATCH);
        Assert.assertEquals(consentValidationResult.getHttpCode(), 400);
    }

}

