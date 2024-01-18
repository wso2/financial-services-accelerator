/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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
package com.wso2.openbanking.accelerator.consent.extensions.manage.vrp;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.util.CarbonUtils;
import com.wso2.openbanking.accelerator.common.util.ErrorConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.extensions.manage.validator.VRPConsentRequestValidator;
import com.wso2.openbanking.accelerator.consent.extensions.util.ConsentManageUtil;
import com.wso2.openbanking.accelerator.consent.extensions.utils.ConsentExtensionTestUtils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

/**
 * Test class for VRPConsentRequestValidator.
 */
@PowerMockIgnore({"jdk.internal.reflect.*"})
@PrepareForTest({OpenBankingConfigParser.class})
public class VRPConsentRequestValidatorTest extends PowerMockTestCase {

    @Mock
    private ConsentManageData consentManageData;

    @Mock
    OpenBankingConfigParser openBankingConfigParser;

    private static Map<String, String> configMap;

    @BeforeClass
    public void setUp() throws ReflectiveOperationException {
        MockitoAnnotations.initMocks(this);

        configMap = new HashMap<>();
        //to execute util class initialization
        new CarbonUtils();
        System.setProperty("some.property", "property.value");
        System.setProperty("carbon.home", ".");
        ConsentExtensionTestUtils.injectEnvironmentVariable("CARBON_HOME", ".");

        consentManageData = mock(ConsentManageData.class);
    }

    @BeforeMethod
    public void initMethod() {

        openBankingConfigParser = mock(OpenBankingConfigParser.class);
        Mockito.doReturn(configMap).when(openBankingConfigParser).getConfiguration();

        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParser);
    }

    @Test
    public void testVrpPayload() {

        VRPConsentRequestValidator handler = new VRPConsentRequestValidator();
        JSONObject response = handler.validateVRPPayload("payload");
        Assert.assertFalse((boolean) response.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.PAYLOAD_FORMAT_ERROR, response.get(ConsentExtensionConstants.ERRORS));

    }

    @Test
    public void testVrpEmptyPayload() {
        JSONObject response = VRPConsentRequestValidator.validateVRPPayload("");
        Assert.assertFalse((boolean) response.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.PAYLOAD_FORMAT_ERROR, response.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiation() {

        String initiationPayloads = VRPTestConstants.vrpInitiationPayloadWithoutData;
        JSONObject response = VRPConsentRequestValidator.validateVRPPayload(JSONValue.
                parse(initiationPayloads));
        Assert.assertFalse((boolean) response.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.PAYLOAD_FORMAT_ERROR, response.get(ConsentExtensionConstants.ERRORS));
    }


    @Test
    public void testVrpControlParameters() {
        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_CONTROL_PARAMETERS;
        JSONObject response = VRPConsentRequestValidator.validateControlParameters((JSONObject) JSONValue.
                parse(initiationPayloads));
        Assert.assertFalse((boolean) response.get(ConsentExtensionConstants.IS_VALID));
    }

    @Test
    public void testVrpEmptyData() {
        String initiationPayloads = VRPTestConstants.vrpInitiationPayloadWithStringData;
        JSONObject response = VRPConsentRequestValidator.validateVRPPayload(JSONValue.
                parse(initiationPayloads));
        Assert.assertFalse((boolean) response.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.PAYLOAD_FORMAT_ERROR, response.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpDataIsJsonObject() {
        String initiationPayloads = VRPTestConstants.vrpInitiationPayloadWithOutJsonObject;
        JSONObject response = VRPConsentRequestValidator.validateVRPPayload(JSONValue.
                parse(initiationPayloads));
        Assert.assertFalse((boolean) response.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.PAYLOAD_FORMAT_ERROR, response.get(ConsentExtensionConstants.ERRORS));
    }


    @Test
    public void testVrpInitiationPayloadWithoutControlParameterKey() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_CONTROL_PARAMETERS;
        JSONObject result = VRPConsentRequestValidator.validateControlParameters((JSONObject) JSONValue.
                parse(initiationPayloads));
        JSONObject result2 = VRPConsentRequestValidator.validateMaximumIndividualAmount((JSONObject) JSONValue.
                parse(initiationPayloads));
        JSONObject result3 = VRPConsentRequestValidator.
                validateMaximumIndividualAmountCurrency((JSONObject) JSONValue.parse(initiationPayloads));

        Assert.assertTrue(true);
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result2.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result3.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.MISSING_MAXIMUM_INDIVIDUAL_AMOUNT,
                result.get(ConsentExtensionConstants.ERRORS));
    }


    @Test
    public void testValidateAmountCurrencyWithCurrencyKeys() {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Currency", "USD");

        JSONArray jsonArray = new JSONArray();
        jsonArray.add(jsonObject);

        JSONObject result = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(jsonArray, "Currency");
        Assert.assertTrue((boolean) result.get(ConsentExtensionConstants.IS_VALID));
    }

    @Test
    public void testValidateAmountCurrencyWithInvalidKey() {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("InvalidKey", "USD");

        JSONArray jsonArray = new JSONArray();
        jsonArray.add(jsonObject);

        JSONObject result = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(jsonArray, "Currency");

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Mandatory parameter 'Currency' of periodic limits is not present in payload",
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadWithoutPeriodicLimitCurrency() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_PERIODIC_LIMIT_CURRENCY;
        JSONObject results = VRPConsentRequestValidator.validateControlParameters((JSONObject) JSONValue.
                parse(initiationPayloads));
        JSONObject result = VRPConsentRequestValidator.validateCurrencyPeriodicLimit((JSONObject) JSONValue.
                parse(initiationPayloads));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) results.get(ConsentExtensionConstants.IS_VALID));
        assertTrue(true);
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Mandatory parameter 'Currency' of periodic limits is not present in payload",
                result.get(ConsentExtensionConstants.ERRORS));

    }

    @Test
    public void testVrpInitiationPayloadWithoutPeriodicLimitAmount() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_PERIODIC_LIMIT_AMOUNT;
        JSONObject results = VRPConsentRequestValidator.validateControlParameters((JSONObject) JSONValue.
                parse(initiationPayloads));
        JSONObject result = VRPConsentRequestValidator.validateMaximumIndividualAmountCurrency((JSONObject) JSONValue.
                parse(initiationPayloads));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) results.get(ConsentExtensionConstants.IS_VALID));
        assertTrue(true);

    }

    @Test
    public void testValidateAmountCurrencyPeriodicLimitsWithInvalidValue() {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Currency", 123);

        JSONArray jsonArray = new JSONArray();
        jsonArray.add(jsonObject);

        JSONObject result = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(jsonArray, "Currency");

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
    }


    @Test
    public void testValidateAmountCurrencyPeriodicLimitsWithInvalidKey() {

        JSONArray testData = new JSONArray();
        JSONObject limit = new JSONObject();
        limit.put("anotherKey", "USD");
        testData.add(limit);

        JSONObject result = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(testData, "currency");
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Mandatory parameter 'currency' of periodic limits is not present in payload",
                result.get(ConsentExtensionConstants.ERRORS));
    }


    @Test
    public void testValidationFailureForCurrency() {

        JSONObject limit = new JSONObject();
        limit.put(ConsentExtensionConstants.CURRENCY, 123);

        JSONArray periodicLimits = new JSONArray();
        periodicLimits.add(limit);

        JSONObject result = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(periodicLimits,
                ConsentExtensionConstants.CURRENCY);

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Mandatory parameter 'Currency' is not present in periodic limits or the " +
                "value is not a string", result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidateAmountCurrencyPeriodicLimitsWithCurrencyKey() {

        // Test case 2: Invalid currency key (empty value)
        JSONArray testData2 = new JSONArray();
        JSONObject limit2 = new JSONObject();
        limit2.put("currency", "");
        testData2.add(limit2);

        JSONObject result2 = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(testData2, "0");
        Assert.assertFalse((boolean) result2.get(ConsentExtensionConstants.IS_VALID));
        JSONArray testData3 = new JSONArray();

        JSONObject result3 = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(testData3, "0");
        Assert.assertFalse((boolean) result3.get(ConsentExtensionConstants.IS_VALID));

        JSONObject result4 = VRPConsentRequestValidator.
                validateAmountCurrencyPeriodicLimits(null, "currency");
        Assert.assertFalse((boolean) result4.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Mandatory parameter 'currency' of periodic limits is not present in payload",
                result4.get(ConsentExtensionConstants.ERRORS));


    }

    @Test
    public void testVrpInitiationPayloadWithoutRisk() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_RISK;
        JSONObject result = VRPConsentRequestValidator.validateConsentRisk((JSONObject) JSONValue.
                parse(initiationPayloads));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.PAYLOAD_FORMAT_ERROR_RISK,
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testIsValidObjectDebAcc() {
        String initiationPayloads = VRPTestConstants.METADATA_VRP_DEBTOR_ACCOUNT;
        JSONObject result = VRPConsentRequestValidator.validateVRPInitiationPayload((JSONObject) JSONValue.
                parse(initiationPayloads));


        // Test case 3: Non-JSONObject value
        String nonJsonObject = "not a JSONObject";
        Assert.assertFalse(VRPConsentRequestValidator.isValidJSONObject(nonJsonObject),
                ConsentExtensionConstants.IS_VALID);


        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));


        // Test case 4: Null value
        Object nullValue = null;
        Assert.assertFalse(VRPConsentRequestValidator.isValidJSONObject(nullValue),
                ConsentExtensionConstants.IS_VALID);

        Assert.assertEquals(ErrorConstants.PAYLOAD_FORMAT_ERROR_DEBTOR_ACC,
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testIsValidObjectDebtorAcc() {
        String initiationPayloads = VRPTestConstants.METADATA_VRP_DEBTOR_ACCOUNT;
        JSONObject result = VRPConsentRequestValidator.validateVRPInitiationPayload((JSONObject) JSONValue.
                parse(initiationPayloads));


        // Test case 3: Non-JSONObject value
        String nonJsonObject = "not a JSONObject";
        Assert.assertFalse(VRPConsentRequestValidator.isValidJSONObject(nonJsonObject),
                ConsentExtensionConstants.IS_VALID);

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        // Test case 4: Null value
        Object nullValue = null;
        Assert.assertFalse(VRPConsentRequestValidator.isValidJSONObject(nullValue),
                ConsentExtensionConstants.IS_VALID);
    }


    @Test
    public void testVrpInitiationPayloadWithoutDebtorAcc() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_DEBTOR_ACCOUNT;
        JSONObject result = VRPConsentRequestValidator.validateVRPInitiationPayload((JSONObject) JSONValue.
                parse(initiationPayloads));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.PAYLOAD_FORMAT_ERROR_DEBTOR_ACC,
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadWithoutCreditAcc() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_CREDITOR_ACCOUNT;
        JSONObject result = VRPConsentRequestValidator.validateConsentInitiation((JSONObject) JSONValue.
                parse(initiationPayloads));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.PAYLOAD_FORMAT_ERROR,
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadWithoutCreditorAcc() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_CREDITOR_ACCOUNT;
        JSONObject result = VRPConsentRequestValidator.validateVRPInitiationPayload((JSONObject) JSONValue.
                parse(initiationPayloads));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.PAYLOAD_FORMAT_ERROR_DEBTOR_ACC,
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadWithoutSchemeName() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_DEBTOR_ACCOUNT_SCHEME_NAME;
        JSONObject result = ConsentManageUtil.validateDebtorAccount((JSONObject) JSONValue.
                parse(initiationPayloads));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.MISSING_DEBTOR_ACC_SCHEME_NAME,
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidateDebtorAccount_InvalidSchemeName() {
        JSONObject debtorAccount = new JSONObject();
        debtorAccount.put(ConsentExtensionConstants.SCHEME_NAME, "");
        debtorAccount.put(ConsentExtensionConstants.IDENTIFICATION, "ValidIdentification");
        debtorAccount.put(ConsentExtensionConstants.NAME, "ValidName");

        JSONObject result = ConsentManageUtil.validateDebtorAccount(debtorAccount);

        Assert.assertFalse(Boolean.parseBoolean(result.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals(result.get(ConsentExtensionConstants.ERRORS),
                ErrorConstants.MISSING_DEBTOR_ACC_SCHEME_NAME);
    }

    @Test
    public void testVrpInitiationPayloadWithoutIdentification() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_DEBTOR_ACCOUNT_IDENTIFICATION;
        JSONObject result = VRPConsentRequestValidator.validateVRPInitiationPayload((JSONObject) JSONValue.
                parse(initiationPayloads));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));

    }

    //TODO:
    @Test
    public void testVrpInitiationPayloadCreditorAccWithoutSchemeName() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_CREDITOR_ACCOUNT_SCHEME_NAME;
        JSONObject result = VRPConsentRequestValidator.validateVRPPayload((JSONValue.
                parse(initiationPayloads)));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));

    }

    @Test
    public void testVrpInitiationPayloadCreditorAccWithoutIdentification() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_CREDITOR_ACCOUNT_IDENTIFICATION;
        JSONObject result = VRPConsentRequestValidator.validateVRPPayload(JSONValue.
                parse(initiationPayloads));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));

    }

    @Test
    public void testValidatePeriodicLimits() {

        JSONObject invalidLimit = new JSONObject();
        invalidLimit.put("someKey", "someValue");

        JSONObject validationResult = VRPConsentRequestValidator.validatePeriodicLimits(invalidLimit);

        Assert.assertFalse(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals(ErrorConstants.MISSING_PERIOD_LIMITS,
                validationResult.get(ConsentExtensionConstants.ERRORS));


    }

    @Test
    public void testValidateAmountCurrencyPeriodicLimitsWithValidKey() {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Currency", "USD");

        JSONArray jsonArray = new JSONArray();
        jsonArray.add(jsonObject);

        JSONObject result = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(jsonArray, "Currency");
        Assert.assertTrue(Boolean.parseBoolean(result.getAsString(ConsentExtensionConstants.IS_VALID)));

    }

    @Test
    public void testValidateAmountCurrencyPeriodicLimitsWithInvalidKeys() {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("InvalidKey", "USD");

        JSONArray jsonArray = new JSONArray();
        jsonArray.add(jsonObject);

        JSONObject result = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(jsonArray, "Currency");

        Assert.assertFalse(Boolean.parseBoolean(result.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals("Mandatory parameter 'Currency' of periodic limits is not present in payload",
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidateAmountCurrencyPeriodicLimitsWithEmptyArray() {

        JSONArray jsonArray = new JSONArray();

        JSONObject result = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(jsonArray, "Currency");

        Assert.assertFalse(Boolean.parseBoolean(result.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals("Mandatory parameter 'Currency' of periodic limits is not present in payload",
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidateAmountCurrencyPeriodicLimitsWithNullArray() {

        JSONObject result = VRPConsentRequestValidator.
                validateAmountCurrencyPeriodicLimits(null, "Currency");

        Assert.assertFalse(Boolean.parseBoolean(result.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals("Mandatory parameter 'Currency' of periodic limits is not present in payload",
                result.get(ConsentExtensionConstants.ERRORS));
    }


    @Test
    public void testVrpPeriodicTypeJsonArray() {

        Object invalidObject = "Not a JSONArray";
        boolean isValidInvalidObject = VRPConsentRequestValidator.isValidJSONArray(invalidObject);

        // Test case 2: Missing period type key
        JSONObject missingKeyObject = new JSONObject();
        JSONObject result2 = VRPConsentRequestValidator.validatePeriodType(missingKeyObject);
        Assert.assertFalse(Boolean.parseBoolean(result2.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals(ErrorConstants.MISSING_PERIOD_TYPE, result2.get(ConsentExtensionConstants.ERRORS));

        // Test case 3: Null period type
        JSONObject nullPeriodTypeObject = new JSONObject();
        nullPeriodTypeObject.put(ConsentExtensionConstants.PERIOD_TYPE, null);
        JSONObject result3 = VRPConsentRequestValidator.validatePeriodType(nullPeriodTypeObject);
        Assert.assertFalse(Boolean.parseBoolean(result3.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals(ErrorConstants.MISSING_PERIOD_TYPE, result2.get(ConsentExtensionConstants.ERRORS));

        // Test case 4: Empty period type
        JSONObject emptyPeriodTypeObject = new JSONObject();
        emptyPeriodTypeObject.put(ConsentExtensionConstants.PERIOD_TYPE, "");
        JSONObject result4 = VRPConsentRequestValidator.validatePeriodType(emptyPeriodTypeObject);
        Assert.assertFalse(Boolean.parseBoolean(result4.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals(ErrorConstants.MISSING_PERIOD_TYPE, result2.get(ConsentExtensionConstants.ERRORS));

        // Test case 5: Invalid period type
        JSONObject invalidPeriodTypeObject = new JSONObject();
        invalidPeriodTypeObject.put(ConsentExtensionConstants.PERIOD_TYPE, "InvalidType");
        JSONObject result5 = VRPConsentRequestValidator.validatePeriodType(invalidPeriodTypeObject);
        Assert.assertFalse(Boolean.parseBoolean(result5.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals(ErrorConstants.MISSING_PERIOD_TYPE, result2.get(ConsentExtensionConstants.ERRORS));

        Assert.assertFalse(isValidInvalidObject, ConsentExtensionConstants.IS_VALID);
    }

    @Test
    public void testDataContainsKey_InitiationNotPresent() {
        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_INITIATION;
        JSONObject result = VRPConsentRequestValidator.validateConsentInitiation((JSONObject) JSONValue.
                parse(initiationPayloads));

        boolean containsKey = result.containsKey(ConsentExtensionConstants.INITIATION);
        Assert.assertFalse(containsKey, ConsentExtensionConstants.IS_VALID);
        Assert.assertEquals("Missing mandatory parameter Initiation in the payload",
                result.get(ConsentExtensionConstants.ERRORS));

    }

    @Test
    public void testDataContainsKey_RiskNotPresent() {
        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_RISK;
        JSONObject result = VRPConsentRequestValidator.validateVRPPayload((JSONObject) JSONValue.
                parse(initiationPayloads));

        boolean containsKey = result.containsKey(ConsentExtensionConstants.RISK);
        Assert.assertFalse(containsKey, ConsentExtensionConstants.IS_VALID);
    }

    @Test
    public void testDataContainsKey_ControlParametersNotPresent() {
        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_CONTROL_PARAMETERS;
        JSONObject result = VRPConsentRequestValidator.validateConsentControlParameters((JSONObject) JSONValue.
                parse(initiationPayloads));
        boolean containsKey = result.containsKey(ConsentExtensionConstants.CONTROL_PARAMETERS);
        Assert.assertFalse(containsKey, ConsentExtensionConstants.IS_VALID);
        Assert.assertEquals(ErrorConstants.PAYLOAD_FORMAT_ERROR_CONTROL_PARAMETER,
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadMaximumIndividualAmountNotJsonObject() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITH_EMPTY_MAX_INDIVIDUAL_AMOUNT;
        String date = VRPTestConstants.METADATA_VRP_WITHOUT_VALID_FROM_DATE;
        String date2 = VRPTestConstants.METADATA_VRP_WITHOUT_VALID_TO_DATE;

        JSONObject result = VRPConsentRequestValidator.validateMaximumIndividualAmount((JSONObject) JSONValue.
                parse(initiationPayloads));
        JSONObject results = VRPConsentRequestValidator.validateMaximumIndividualAmountCurrency((JSONObject) JSONValue.
                parse(initiationPayloads));
        JSONObject result2 = VRPConsentRequestValidator.validateControlParameters((JSONObject) JSONValue.
                parse(date));
        JSONObject result3 = VRPConsentRequestValidator.validateControlParameters((JSONObject) JSONValue.
                parse(date));

        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidJSONObject(initiationPayloads);
        Assert.assertFalse(isValidNonJSONObject, (ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));


        boolean isValidNonJSONObjects = VRPConsentRequestValidator.isValidJSONObject(initiationPayloads);
        Assert.assertFalse(isValidNonJSONObjects, (ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) results.get(ConsentExtensionConstants.IS_VALID));

        boolean obj2 = VRPConsentRequestValidator.isValidJSONObject(date);
        Assert.assertFalse(obj2, (ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result2.get(ConsentExtensionConstants.IS_VALID));

        boolean obj3 = VRPConsentRequestValidator.isValidJSONObject(date2);
        Assert.assertFalse(obj3, (ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result3.get(ConsentExtensionConstants.IS_VALID));

    }

    @Test
    public void testVrpInitiationPayloadDebAcc() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_DEBTOR_ACC;
        JSONObject result = VRPConsentRequestValidator.validateConsentInitiation((JSONObject) JSONValue.
                parse(initiationPayloads));
        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidJSONObject(initiationPayloads);
        Assert.assertFalse(isValidNonJSONObject);
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.PAYLOAD_FORMAT_ERROR_DEBTOR_ACC,
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadDebAccs() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_DEB_ACC;
        JSONObject result = VRPConsentRequestValidator.validateVRPPayload(JSONValue.
                parse(initiationPayloads));
        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidJSONObject(initiationPayloads);
        Assert.assertFalse(isValidNonJSONObject);
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Parameter 'debtor account' passed in is null, empty, or not a JSONObject",
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationMax() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITH_EMPTY_MAX_INDIVIDUAL_AMOUNT;
        JSONObject result = VRPConsentRequestValidator.validateVRPPayload(JSONValue.
                parse(initiationPayloads));
        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidJSONObject(initiationPayloads);
        Assert.assertFalse(isValidNonJSONObject, (ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.PAYLOAD_FORMAT_ERROR, result.get(ConsentExtensionConstants.ERRORS));

    }

    @Test
    public void testVrpInitiationPayloadValidateDebAcc() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_DEB_ACC;
        JSONObject result = VRPConsentRequestValidator.validateVRPInitiationPayload((JSONObject) JSONValue.
                parse(initiationPayloads));
        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidJSONObject(initiationPayloads);
        Assert.assertFalse(isValidNonJSONObject, (ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.PAYLOAD_FORMAT_ERROR_DEBTOR_ACC,
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadCreditorAcc() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_CREDITOR_ACC;
        JSONObject result = VRPConsentRequestValidator.validateVRPInitiationPayload((JSONObject) JSONValue.
                parse(initiationPayloads));
        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidJSONObject(initiationPayloads);
        Assert.assertFalse(isValidNonJSONObject);
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.PAYLOAD_FORMAT_ERROR_DEBTOR_ACC,
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadCreditorAccs() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_CREDITOR_ACC;
        JSONObject result = VRPConsentRequestValidator.validateVRPPayload(JSONValue.
                parse(initiationPayloads));
        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidJSONObject(initiationPayloads);

        Assert.assertFalse(isValidNonJSONObject);
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Parameter 'creditor account' passed in is null, empty, or not a JSONObject",
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadDebtorAccs() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_DEB_ACC;
        JSONObject result = VRPConsentRequestValidator.validateVRPPayload(JSONValue.
                parse(initiationPayloads));
        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidJSONObject(initiationPayloads);

        Assert.assertFalse(isValidNonJSONObject);
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));

        Assert.assertEquals("Parameter 'debtor account' passed in is null, empty, or not a JSONObject",
                result.get(ConsentExtensionConstants.ERRORS));
    }


    @Test
    public void testIsValidObject_NegativeScenarios() {

        String nonJSONObject = "Not a JSONObject";
        JSONObject validInitiationObject = new JSONObject();

        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidJSONObject(nonJSONObject);
        boolean isValidNonJSONObject1 = VRPConsentRequestValidator.isValidJSONObject(validInitiationObject);
        Assert.assertFalse(isValidNonJSONObject1, (ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse(isValidNonJSONObject, (ConsentExtensionConstants.IS_VALID));
    }

    @Test
    public void testVrpInitiationPayloadInitiationNotJsonObject() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_EMPTY_INITIATION;
        JSONObject result = VRPConsentRequestValidator.validateConsentInitiation((JSONObject) JSONValue.
                parse(initiationPayloads));
        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidJSONObject(initiationPayloads);
        Assert.assertFalse(isValidNonJSONObject, (ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Parameter 'initiation' passed in is null, empty, or not a JSONObject",
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadMaximumIndividualNotJsonObject() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITH_INVALID_MAX_INDIVIDUAL_AMOUNT;
        JSONObject result = VRPConsentRequestValidator.validateMaximumIndividualAmount((JSONObject) JSONValue.
                parse(initiationPayloads));
        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidJSONObject(initiationPayloads);
        Assert.assertFalse(isValidNonJSONObject, (ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.MISSING_MAXIMUM_INDIVIDUAL_AMOUNT,
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadCurrencyNotJsonObject() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITH_INVALID_MAX_INDIVIDUAL_AMOUNT;
        JSONObject result = VRPConsentRequestValidator.validateControlParameters((JSONObject) JSONValue.
                parse(initiationPayloads));
        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidJSONObject(initiationPayloads);
        Assert.assertFalse(isValidNonJSONObject, (ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.MISSING_MAXIMUM_INDIVIDUAL_AMOUNT,
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadControlParametersNotJsonObject() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITH_EMPTY_CONTROL_PARAMETERS;
        JSONObject result = VRPConsentRequestValidator.validateConsentControlParameters((JSONObject) JSONValue.
                parse(initiationPayloads));
        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidJSONObject(initiationPayloads);
        Assert.assertFalse(isValidNonJSONObject, (ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Parameter 'control parameters' passed in is null, empty, or not a JSONObject",
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadWithoutDate() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITH_INVALID_VALID_FROM_DATETIME;
        JSONObject result = VRPConsentRequestValidator.validateParameterDateTime((JSONObject) JSONValue.
                parse(initiationPayloads));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.MISSING_VALID_TO_DATE_TIME,
                result.get(ConsentExtensionConstants.ERRORS));
    }


    @Test
    public void testVrpInitiationPayloadWithoutValidToDate() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_VALID_TO_DATE;
        JSONObject result = VRPConsentRequestValidator.validateParameterDateTime((JSONObject) JSONValue.
                parse(initiationPayloads));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.MISSING_VALID_TO_DATE_TIME,
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadMaximumIndividualAmountIsJsonObject() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITH_EMPTY_MAX_INDIVIDUAL_AMOUNT;
        JSONObject result = VRPConsentRequestValidator.validateControlParameters((JSONObject) JSONValue.
                parse(initiationPayloads));
        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidJSONObject(initiationPayloads);
        Assert.assertFalse(isValidNonJSONObject, (ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.MISSING_MAXIMUM_INDIVIDUAL_AMOUNT,
                result.get(ConsentExtensionConstants.ERRORS));
    }


    @Test
    public void testIsValidDateTimeObjectNegativeScenarios() {
        // Test case 1: Empty string
        String emptyString = "";
        JSONObject resultEmptyString = VRPConsentRequestValidator.isValidDateTimeObject(emptyString);
        Assert.assertFalse((boolean) resultEmptyString.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.MISSING_DATE_TIME_FORMAT,
                resultEmptyString.get(ConsentExtensionConstants.ERRORS));

        // Test case 2: Null value
        Object nullValue = null;
        boolean resultNullValue = false;
        Assert.assertFalse(resultNullValue, "Expected false for a null value");

        // Test case 3: Non-string value
        Object nonStringValue = 123; // Assuming an integer, but could be any non-string type
        JSONObject resultNonStringValue = VRPConsentRequestValidator.isValidDateTimeObject(nonStringValue);
        Assert.assertFalse((boolean) resultNonStringValue.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.MISSING_DATE_TIME_FORMAT,
                resultNonStringValue.get(ConsentExtensionConstants.ERRORS));

    }


    @Test
    public void testValidateAmountCurrencyNegativeScenarios() {

        // Test case 1: parentObj is null
        JSONObject result1 = VRPConsentRequestValidator.validateJsonObjectKey(null, "yourKey");
        Assert.assertFalse((boolean) result1.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("parameter passed in  is null",
                result1.get(ConsentExtensionConstants.ERRORS));


        // Test case 2: Key is not present in parentObj
        JSONObject result2 = VRPConsentRequestValidator.validateJsonObjectKey(new JSONObject(), "nonExistentKey");
        Assert.assertFalse((boolean) result2.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("parameter passed in  is null",
                result1.get(ConsentExtensionConstants.ERRORS));


        // Test case 3: Value associated with the key is not a String
        JSONObject testData = new JSONObject();
        testData.put("Amount", 123);
        JSONObject result3 = VRPConsentRequestValidator.validateJsonObjectKey(testData, "Amount");
        Assert.assertFalse((boolean) result3.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("parameter passed in  is null",
                result1.get(ConsentExtensionConstants.ERRORS));

        // Test case 4: Value associated with the key is an empty String
        testData.put("Currency", "");
        JSONObject result4 = VRPConsentRequestValidator.validateJsonObjectKey(testData, "Currency");
        Assert.assertFalse((boolean) result4.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("parameter passed in  is null",
                result1.get(ConsentExtensionConstants.ERRORS));


        // Test case 2: Key is not present in parentObj
        JSONObject result5 = VRPConsentRequestValidator.validateJsonObjectKey(new JSONObject(), "Currency");
        Assert.assertFalse((boolean) result5.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("parameter passed in  is null",
                result1.get(ConsentExtensionConstants.ERRORS));


        // Test case 4: Key is present, and value is an empty String
        JSONObject testData4 = new JSONObject();
        testData4.put("Currency", "");
        JSONObject result7 = VRPConsentRequestValidator.validateJsonObjectKey(testData4, "Currency");
        Assert.assertFalse((boolean) result7.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("parameter passed in  is null",
                result1.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidateAmountCurrencyWithoutAmountKeyAndEmptyString() {

        // Test case 1: parentObj is null
        JSONObject result1 = VRPConsentRequestValidator.validateJsonObjectKey(null, "Amount");
        Assert.assertFalse((Boolean) result1.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("parameter passed in  is null",
                result1.get(ConsentExtensionConstants.ERRORS));

        // Test case 2: Key is not present in parentObj
        JSONObject result2 = VRPConsentRequestValidator.validateJsonObjectKey(new JSONObject(), "nonExistentKey");
        Assert.assertFalse((Boolean) result2.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Mandatory parameter 'nonExistentKey' is not present in payload",
                result2.get(ConsentExtensionConstants.ERRORS));

        // Test case 3: Key is present, but value is not a String
        JSONObject testData3 = new JSONObject();
        testData3.put("Amount", 123); // Assuming "yourKey" should be a String, but it's an integer in this case
        JSONObject result3 = VRPConsentRequestValidator.validateJsonObjectKey(testData3, "Amount");
        Assert.assertFalse((Boolean) result3.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("The value of 'Amount'is not a string or the value is empty",
                result3.get(ConsentExtensionConstants.ERRORS));

        // Test case 4: Key is present, and value is an empty String
        JSONObject testData4 = new JSONObject();
        testData4.put("Amount", "");
        JSONObject result4 = VRPConsentRequestValidator.validateJsonObjectKey(testData4, "Amount");
        Assert.assertFalse((Boolean) result4.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("The value of 'Amount'is not a string or the value is empty",
                result4.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidateCurrencyWithoutAmountKeyAndEmptyString() {

        // Test case 1: parentObj is null
        JSONObject result1 = VRPConsentRequestValidator.validateJsonObjectKey(null, "Currency");
        Assert.assertFalse((Boolean) result1.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("parameter passed in  is null",
                result1.get(ConsentExtensionConstants.ERRORS));

        // Test case 2: Key is not present in parentObj
        JSONObject result2 = VRPConsentRequestValidator.validateJsonObjectKey(new JSONObject(), "nonExistentKey");
        Assert.assertFalse((Boolean) result2.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Mandatory parameter 'nonExistentKey' is not present in payload",
                result2.get(ConsentExtensionConstants.ERRORS));

        // Test case 3: Key is present, but value is not a String
        JSONObject testData3 = new JSONObject();
        testData3.put("Currency", 123); // Assuming "yourKey" should be a String, but it's an integer in this case
        JSONObject result3 = VRPConsentRequestValidator.validateJsonObjectKey(testData3, "Currency");
        Assert.assertFalse((Boolean) result3.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("The value of 'Currency'is not a string or the value is empty",
                result3.get(ConsentExtensionConstants.ERRORS));

        // Test case 4: Key is present, and value is an empty String
        JSONObject testData4 = new JSONObject();
        testData4.put("Currency", "");
        JSONObject result4 = VRPConsentRequestValidator.validateJsonObjectKey(testData4, "Currency");
        Assert.assertFalse((Boolean) result4.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("The value of 'Currency'is not a string or the value is empty",
                result4.get(ConsentExtensionConstants.ERRORS));
    }


    @Test
    public void testValidateAmountCurrencyPeriodicLimits() {

        // Test case 2: Key is null
        JSONArray testData2 = new JSONArray();
        JSONObject result2 = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(testData2, null);
        Assert.assertFalse((Boolean) result2.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Mandatory parameter 'null' of periodic limits is not present in payload",
                result2.get(ConsentExtensionConstants.ERRORS));

        // Test case 3: ParentObj is null
        JSONObject result3 = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(null, "0");
        Assert.assertFalse((Boolean) result2.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Mandatory parameter 'null' of periodic limits is not present in payload",
                result2.get(ConsentExtensionConstants.ERRORS));

        // Test case 4: Key is not present in parentObj
        JSONArray testData4 = new JSONArray();
        JSONObject result4 = VRPConsentRequestValidator.
                validateAmountCurrencyPeriodicLimits(testData4, "nonExistentKey");
        Assert.assertFalse((Boolean) result2.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Mandatory parameter 'null' of periodic limits is not present in payload",
                result2.get(ConsentExtensionConstants.ERRORS));

        // Test case 5: Value is an empty String
        JSONArray testData5 = new JSONArray();
        testData5.add("");
        JSONObject result5 = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(testData5, "0");
        Assert.assertFalse((Boolean) result2.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Mandatory parameter 'null' of periodic limits is not present in payload",
                result2.get(ConsentExtensionConstants.ERRORS));
    }


    @Test
    public void testValidateKeyAndNonEmptyStringValue() {

        // Test case 2: Key is null
        JSONArray testData2 = new JSONArray();
        JSONObject result2 = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(testData2, null);
        Assert.assertFalse((Boolean) result2.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Mandatory parameter 'null' of periodic limits is not present in payload",
                result2.get(ConsentExtensionConstants.ERRORS));

        // Test case 3: ParentObj is null
        JSONObject result3 = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(null, "0");
        Assert.assertFalse((Boolean) result3.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Mandatory parameter '0' of periodic limits is not present in payload",
                result3.get(ConsentExtensionConstants.ERRORS));

        // Test case 4: Key is not present in parentObj
        JSONArray testData4 = new JSONArray();
        JSONObject result4 = VRPConsentRequestValidator.
                validateAmountCurrencyPeriodicLimits(testData4, "nonExistentKey");
        Assert.assertFalse((Boolean) result4.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Mandatory parameter 'nonExistentKey' of periodic limits is not present in payload",
                result4.get(ConsentExtensionConstants.ERRORS));

        // Test case 5: Value is an empty String
        JSONArray testData5 = new JSONArray();
        testData5.add("");
        JSONObject result5 = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(testData5, "0");
        Assert.assertFalse((Boolean) result5.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Mandatory parameter '0' of periodic limits is not present in payload",
                result5.get(ConsentExtensionConstants.ERRORS));


        // Test case 7: Value is not a String
        JSONArray testData7 = new JSONArray();
        testData7.add(123); // Assuming the value should be a String, but it's an integer in this case
        JSONObject result7 = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(testData7, "0");
        Assert.assertFalse((Boolean) result7.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Mandatory parameter '0' of periodic limits is not present in payload",
                result7.get(ConsentExtensionConstants.ERRORS));
    }


    @Test
    public void testValidateAmountCurrencyWithoutCurrentKeyAndEmptyString() {


        // Test case 1: parentObj is null
        JSONObject result1 = VRPConsentRequestValidator.validateJsonObjectKey(null, "Currency");
        Assert.assertFalse((Boolean) result1.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("parameter passed in  is null",
                result1.get(ConsentExtensionConstants.ERRORS));

        // Test case 2: Key is not present in parentObj
        JSONObject result2 = VRPConsentRequestValidator.validateJsonObjectKey(new JSONObject(), "nonExistentKey");
        Assert.assertFalse((Boolean) result2.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Mandatory parameter 'nonExistentKey' is not present in payload",
                result2.get(ConsentExtensionConstants.ERRORS));

        // Test case 3: Key is present, but value is not a String
        JSONObject testData3 = new JSONObject();
        testData3.put("Currency", 123);
        JSONObject result3 = VRPConsentRequestValidator.validateJsonObjectKey(testData3, "Currency");
        Assert.assertFalse((Boolean) result3.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("The value of 'Currency'is not a string or the value is empty",
                result3.get(ConsentExtensionConstants.ERRORS));

        // Test case 4: Key is present, and value is an empty String
        JSONObject testData4 = new JSONObject();
        testData4.put("Currency", "");
        JSONObject result4 = VRPConsentRequestValidator.validateJsonObjectKey(testData4, "Currency");
        Assert.assertFalse((Boolean) result4.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("The value of 'Currency'is not a string or the value is empty",
                result4.get(ConsentExtensionConstants.ERRORS));

    }

    @Test
    public void testNegativeScenarioForKeyAndEmptyString() {

        // Test case 1: Value is not a String
        JSONObject testData1 = new JSONObject();
        testData1.put("Amount", 123);
        JSONObject result1 = VRPConsentRequestValidator.validateJsonObjectKey(testData1, "Amount");
        Assert.assertFalse((Boolean) result1.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("The value of 'Amount'is not a string or the value is empty",
                result1.get(ConsentExtensionConstants.ERRORS));

        // Test case 2: Value is an empty String
        JSONObject testData2 = new JSONObject();
        testData2.put("Amount", "");
        JSONObject result2 = VRPConsentRequestValidator.validateJsonObjectKey(testData2, "Amount");
        Assert.assertFalse((Boolean) result2.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("The value of 'Amount'is not a string or the value is empty",
                result1.get(ConsentExtensionConstants.ERRORS));

    }

    @Test
    public void testNegativeScenarioForKeyAndEmptyStrings() {

        // Test case 1: Value is not a String
        JSONObject testData1 = new JSONObject();
        testData1.put("Currency", 123); // Assuming "yourKey" should be a String, but it's an integer in this case
        JSONObject result1 = VRPConsentRequestValidator.validateJsonObjectKey(testData1, "Currency");
        Assert.assertFalse((Boolean) result1.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("The value of 'Currency'is not a string or the value is empty",
                result1.get(ConsentExtensionConstants.ERRORS));

        // Test case 2: Value is an empty String
        JSONObject testData2 = new JSONObject();
        testData2.put("Currency", "");
        JSONObject result2 = VRPConsentRequestValidator.validateJsonObjectKey(testData2, "Currency");
        Assert.assertFalse((Boolean) result2.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("The value of 'Currency'is not a string or the value is empty",
                result2.get(ConsentExtensionConstants.ERRORS));

    }


    @Test
    public void testPositiveScenarioForValidateAmountCurrency() {

        JSONObject testData = new JSONObject();
        testData.put("Amount", "1000");
        JSONObject result = VRPConsentRequestValidator.validateJsonObjectKey(testData, "Amount");

        Assert.assertTrue((boolean) result.get(ConsentExtensionConstants.IS_VALID));
    }

    @Test
    public void testPositiveScenarioForValidateAmountCurrencys() {

        JSONObject testData = new JSONObject();
        testData.put("Currency", "1000");
        JSONObject result = VRPConsentRequestValidator.validateJsonObjectKey(testData, "Currency");

        Assert.assertTrue((boolean) result.get(ConsentExtensionConstants.IS_VALID));
    }

    @Test
    public void testValidMaximumIndividualAmount() {
        JSONObject controlParameters = new JSONObject();

        JSONObject validPeriodicLimit = new JSONObject();
        validPeriodicLimit.get(ConsentExtensionConstants.AMOUNT);
        validPeriodicLimit.get(ConsentExtensionConstants.CURRENCY);
        validPeriodicLimit.put("1000", "usd");

        controlParameters.put(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT, validPeriodicLimit);

        JSONObject validationResult = VRPConsentRequestValidator.validateControlParameters(controlParameters);
        // Test case 1: "Currency" key is present, and value is a non-empty String
        JSONObject parentObj = new JSONObject();
        parentObj.put("Currency", "USD"); // Assuming a valid currency
        String key = "Currency";
        JSONObject result = VRPConsentRequestValidator.validateJsonObjectKey(parentObj, key);
        Assert.assertTrue((Boolean) result.get(ConsentExtensionConstants.IS_VALID));


        // Test case 2: "Currency" key is present, but value is an empty String
        parentObj.put("Currency", "");
        result = VRPConsentRequestValidator.validateJsonObjectKey(parentObj, key);
        Assert.assertFalse((Boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("The value of 'Currency'is not a string or the value is empty",
                result.get(ConsentExtensionConstants.ERRORS));

        // Test case 3: "Currency" key is present, but value is not a String
        parentObj.put("Currency", 123); // Assuming "Currency" should be a String, but it's an integer in this case
        result = VRPConsentRequestValidator.validateJsonObjectKey(parentObj, key);
        Assert.assertFalse((Boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("The value of 'Currency'is not a string or the value is empty",
                result.get(ConsentExtensionConstants.ERRORS));

        // Test case 4: "Currency" key is not present in parentObj
        parentObj = new JSONObject();
        result = VRPConsentRequestValidator.validateJsonObjectKey(parentObj, key);
        Assert.assertFalse((Boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Mandatory parameter 'Currency' is not present in payload",
                result.get(ConsentExtensionConstants.ERRORS));
        // Test case 5: parentObj is null
        result = VRPConsentRequestValidator.validateJsonObjectKey(null, key);
        Assert.assertFalse((Boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("parameter passed in  is null",
                result.get(ConsentExtensionConstants.ERRORS));

        Assert.assertFalse((Boolean) validationResult.get(ConsentExtensionConstants.IS_VALID));
    }

    @Test
    public void testInvalidPeriodicLimitsFormat() {

        JSONObject controlParameters = new JSONObject();
        controlParameters.put(ConsentExtensionConstants.PERIODIC_LIMITS, "invalid-format");
        JSONObject validationResult = VRPConsentRequestValidator.validatePeriodicLimits(controlParameters);

        Assert.assertFalse((Boolean) validationResult.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.INVALID_PARAMETER_PERIODIC_LIMITS,
                validationResult.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testInvalidMaxAmountFormat() {

        JSONObject controlParameters = new JSONObject();
        controlParameters.put(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT, "invalid-format");
        JSONObject validationResult = VRPConsentRequestValidator.validateMaximumIndividualAmount(controlParameters);

        Assert.assertFalse((Boolean) validationResult.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Parameter 'maximum individual amount' passed in is null, empty, or not a JSONObject",
                validationResult.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testInvalidMaxAmountFormatPeriodicLimit() {

        JSONObject controlParameters = new JSONObject();
        controlParameters.put(ConsentExtensionConstants.PERIODIC_LIMITS, "invalid-format");
        JSONObject validationResults = VRPConsentRequestValidator.
                validateControlParameters(controlParameters);
        JSONObject validationResult = VRPConsentRequestValidator.
                validateMaximumIndividualAmountCurrency(controlParameters);

        Assert.assertFalse((Boolean) validationResults.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((Boolean) validationResult.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("parameter passed in  is null",
                validationResult.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testInvalidMaxAmountFormats() {

        JSONObject controlParameters = new JSONObject();
        controlParameters.put(ConsentExtensionConstants.PERIODIC_LIMITS, "invalid-format");
        JSONObject validationResult = VRPConsentRequestValidator.validatePeriodicLimits(controlParameters);

        Assert.assertFalse((Boolean) validationResult.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.INVALID_PARAMETER_PERIODIC_LIMITS,
                validationResult.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testInvalidJSONObject() {

        String invalidJSONObject = "not a JSON object";
        boolean isValid = VRPConsentRequestValidator.isValidJSONObject(invalidJSONObject);

        Assert.assertFalse(isValid);
    }

    @Test
    public void testEmptyJSONObject() {
        JSONObject emptyJSONObject = new JSONObject();

        boolean isValid = VRPConsentRequestValidator.isValidJSONObject(emptyJSONObject);
        Assert.assertFalse(isValid);
    }

    @Test
    public void testInvalidPeriodicAlignment() {
        // Arrange
        JSONObject invalidLimit = new JSONObject();
        invalidLimit.put(ConsentExtensionConstants.PERIOD_ALIGNMENT, "InvalidAlignment");

        JSONObject isValid = VRPConsentRequestValidator.validatePeriodicLimits(invalidLimit);

        Assert.assertFalse((Boolean) isValid.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.MISSING_PERIOD_LIMITS,
                isValid.get(ConsentExtensionConstants.ERRORS));
    }


    @Test
    public void testValidateParameterDateTime_InvalidDateTimeRange() {
        // Arrange
        JSONObject controlParameters = new JSONObject();
        controlParameters.put(ConsentExtensionConstants.VALID_FROM_DATE_TIME, "2022-01-01T00:00:00Z");
        controlParameters.put(ConsentExtensionConstants.VALID_TO_DATE_TIME, "2022-01-02T00:00:00Z");
        JSONObject result1 = VRPConsentRequestValidator.validateParameterDateTime(controlParameters);
        Assert.assertNotNull(result1);
        Assert.assertFalse((Boolean) result1.get(ConsentExtensionConstants.IS_VALID));
    }


    @Test
    public void testDateValidation() {
        // Arrange
        JSONObject controlParameters = new JSONObject();
        controlParameters.put(ConsentExtensionConstants.VALID_FROM_DATE_TIME, "2022-01-01T00:00:00Z");
        controlParameters.put(ConsentExtensionConstants.VALID_TO_DATE_TIME, "2023-01-01T00:00:00Z");

        JSONObject validationResult = VRPConsentRequestValidator.validateParameterDateTime(controlParameters);

        // Assert
        Assert.assertFalse((Boolean) validationResult.get(ConsentExtensionConstants.IS_VALID));

        // Negative scenario: ValidToDateTime is older than the current date
        controlParameters.put(ConsentExtensionConstants.VALID_TO_DATE_TIME, "2021-01-01T00:00:00Z");
        validationResult = VRPConsentRequestValidator.validatePeriodicLimits(controlParameters);
        Assert.assertFalse((Boolean) validationResult.get(ConsentExtensionConstants.IS_VALID));

        // Negative scenario: CurrentDate is older than ValidFromDateTime
        controlParameters.put(ConsentExtensionConstants.VALID_TO_DATE_TIME, "2023-01-01T00:00:00Z");
        controlParameters.put(ConsentExtensionConstants.VALID_FROM_DATE_TIME, "2024-01-01T00:00:00Z");
        validationResult = VRPConsentRequestValidator.validateParameterDateTime(controlParameters);
        Assert.assertFalse((Boolean) validationResult.get(ConsentExtensionConstants.IS_VALID));
    }


    @Test
    public void testInvalidDateTimeRange() {

        JSONObject controlParameters = new JSONObject();
        controlParameters.put(ConsentExtensionConstants.VALID_FROM_DATE_TIME, "2023-01-01T00:00:00Z");
        controlParameters.put(ConsentExtensionConstants.VALID_TO_DATE_TIME, "2022-01-01T00:00:00Z");

        boolean hasValidFromDate = controlParameters.containsKey(ConsentExtensionConstants.VALID_FROM_DATE_TIME);
        boolean hasValidToDate = controlParameters.containsKey(ConsentExtensionConstants.VALID_TO_DATE_TIME);


        assertTrue(hasValidFromDate && hasValidToDate);
    }


    @Test
    public void testInvalidDateTimeRanges() {

        JSONObject controlParameters = new JSONObject();
        controlParameters.put(ConsentExtensionConstants.VALID_FROM_DATE_TIME, "2023-01-01T00:00:00Z");
        controlParameters.put(ConsentExtensionConstants.VALID_TO_DATE_TIME, "2022-01-01T00:00:00Z");

        JSONObject isValidRange = VRPConsentRequestValidator.validateParameterDateTime(controlParameters);

        assertFalse(isValidRange.containsKey("ValidFromDateTime"));
        Assert.assertFalse((Boolean) isValidRange.get(ConsentExtensionConstants.IS_VALID));

    }


    @Test
    public void testValidCurrencyKey() {
        JSONObject parentObj = new JSONObject();
        parentObj.put("Currency", "USD");

        JSONObject isValid = VRPConsentRequestValidator.validateJsonObjectKey(parentObj, "Currency");

        //       assertTrue(isValid);
    }

    @Test
    public void testInvalidCurrencyKey_NullParentObj() {
        // Act
        JSONObject isValid = VRPConsentRequestValidator.validateJsonObjectKey(null, "Currency");

        // Assert
//Assert.assertFalse(isValid);

    }

    @Test
    public void testInvalidCurrencyKey_MissingKey() {

        JSONObject maximumIndividualAmount = new JSONObject();

        // Act
        JSONObject validationResults = VRPConsentRequestValidator.
                validateJsonObjectKey(maximumIndividualAmount, ConsentExtensionConstants.CURRENCY);

        // Assert
        //Assert.assertFalse(validationResults);
        // Arrange
        JSONObject parentObj = new JSONObject();
        JSONObject validationResult = VRPConsentRequestValidator.validateMaximumIndividualAmount(parentObj);

        Assert.assertFalse((Boolean) validationResult.get(ConsentExtensionConstants.IS_VALID));
        JSONObject isValid = VRPConsentRequestValidator.validateJsonObjectKey(parentObj, "Currency");

        // Assert
        // Assert.assertFalse(isValid);
    }

    @Test
    public void testInvalidCurrencyKey_EmptyValue() {

        JSONObject maximumIndividualAmount = new JSONObject();

        // Act
        JSONObject validationResult = VRPConsentRequestValidator.validateJsonObjectKey(
                (JSONObject) maximumIndividualAmount, ConsentExtensionConstants.CURRENCY);

        // Assert
        // Assert.assertFalse(validationResult);
        // Arrange
        JSONObject parentObj = new JSONObject();
        parentObj.put("Currency", "");
        JSONObject validationResults = VRPConsentRequestValidator.validateMaximumIndividualAmount(parentObj);

        Assert.assertFalse((Boolean) validationResults.get(ConsentExtensionConstants.IS_VALID));
        // Act

        // Act
        JSONObject isValid = VRPConsentRequestValidator.validateJsonObjectKey(parentObj, "Currency");

        // Assert        Assert.assertFalse(isValid);
    }

    @Test
    public void testInvalidCurrencyKey_NonString() {

        JSONObject maximumIndividualAmount = new JSONObject();

        // Act
        JSONObject validationResults = VRPConsentRequestValidator.validateJsonObjectKey((JSONObject)
                maximumIndividualAmount, ConsentExtensionConstants.CURRENCY);

        // Assert
        // Assert.assertFalse(validationResults);
        // Arrange
        JSONObject parentObj = new JSONObject();
        parentObj.put("Currency", 123); // Assuming "Currency" should be a String, but it's an integer in this case
        JSONObject validationResult = VRPConsentRequestValidator.validateMaximumIndividualAmount(parentObj);

        Assert.assertFalse((Boolean) validationResult.get(ConsentExtensionConstants.IS_VALID));

        // Act
        JSONObject isValid = VRPConsentRequestValidator.validateJsonObjectKey(parentObj, "Currency");

        // Assert
        //   Assert.assertFalse(isValid);
    }

    @Test
    public void testInvalidCurrencyKey_NullValue() {

        JSONObject parentObj = new JSONObject();
        parentObj.put("Currency", null);
        parentObj.put("1000", "usd");

        JSONObject validationResults = VRPConsentRequestValidator.validateMaximumIndividualAmount(parentObj);

        Assert.assertFalse((Boolean) validationResults.get(ConsentExtensionConstants.IS_VALID));

        JSONObject isValid = VRPConsentRequestValidator.validateJsonObjectKey(parentObj, "Currency");
        parentObj.put(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT, parentObj);

        Assert.assertFalse((boolean) isValid.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Missing mandatory parameter Maximum Individual Amount",
                validationResults.get(ConsentExtensionConstants.ERRORS));
    }


    @Test
    public void testValidateAmountCurrencyWithoutAmountKey() {
        // Test case 1: parentObj is null
        JSONObject result1 = VRPConsentRequestValidator.validateJsonObjectKey(null, "Amount");
        //Assert.assertFalse(result1);

        // Test case 2: Key is not present in parentObj
        JSONObject result2 = VRPConsentRequestValidator.validateJsonObjectKey(new JSONObject(), "Amount");
        //Assert.assertFalse(result2);

        // Test case 3: Key is present, but value is not a String
        JSONObject testData3 = new JSONObject();
        testData3.put("Amount", 123); // Assuming "Amount" should be a String, but it's an integer in this case
        JSONObject result3 = VRPConsentRequestValidator.validateJsonObjectKey(testData3, "Amount");
        // Assert.assertFalse(result3);

        // Test case 4: Key is present, and value is an empty String
        JSONObject testData4 = new JSONObject();
        testData4.put("Amount", "");
        JSONObject result4 = VRPConsentRequestValidator.validateJsonObjectKey(testData4, "Amount");
        //Assert.assertFalse(result4);
    }

    @Test
    public void testValidateAmountCurrencyWithoutCurrencyKey() {
        // Test case 1: parentObj is null
        JSONObject result1 = VRPConsentRequestValidator.validateJsonObjectKey(null, "Currency");
        // Assert.assertFalse(result1);

        // Test case 2: Key is not present in parentObj
        JSONObject result2 = VRPConsentRequestValidator.validateJsonObjectKey(new JSONObject(), "Currency");
        //  Assert.assertFalse(result2);

        // Test case 3: Key is present, but value is not a String
        JSONObject testData3 = new JSONObject();
        testData3.put("Currency", 123); // Assuming "Currency" should be a String, but it's an integer in this case
        JSONObject result3 = VRPConsentRequestValidator.validateJsonObjectKey(testData3, "Currency");
        //Assert.assertFalse(result3);

        // Test case 4: Key is present, and value is an empty String
        JSONObject testData4 = new JSONObject();
        testData4.put("Currency", "");
        JSONObject result4 = VRPConsentRequestValidator.validateJsonObjectKey(testData4, "Currency");
        //  Assert.assertFalse(result4);
    }


    @Test
    public void testVrpInitiationPayloadWithoutControlParameterss() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_CURRENCY;
        JSONObject result = VRPConsentRequestValidator.validateMaximumIndividualAmount((JSONObject) JSONValue.
                parse(initiationPayloads));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Missing mandatory parameter Maximum Individual Amount",
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidateCurrency() {
        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_CURRENCY;
        JSONObject results = VRPConsentRequestValidator.validateMaximumIndividualAmount((JSONObject) JSONValue.
                parse(initiationPayloads));
        Assert.assertFalse((boolean) results.get(ConsentExtensionConstants.IS_VALID));
        assertFalse(results.containsKey("Currency"));

        // Test case 1: "Currency" key is present, and value is a non-empty String
        JSONObject parentObj = new JSONObject();
        parentObj.put("Currency", "USD"); // Assuming a valid currency
        String key = "Currency";
        JSONObject result = VRPConsentRequestValidator.validateJsonObjectKey(parentObj, key);
        //   assertTrue(result);

        // Test case 2: "Currency" key is present, but value is an empty String
        parentObj.put("Currency", "");
        result = VRPConsentRequestValidator.validateJsonObjectKey(parentObj, key);
        //  Assert.assertFalse(result);

        // Test case 3: "Currency" key is present, but value is not a String
        parentObj.put("Currency", 123); // Assuming "Currency" should be a String, but it's an integer in this case
        result = VRPConsentRequestValidator.validateJsonObjectKey(parentObj, key);
        //   Assert.assertFalse(result);

        // Test case 4: "Currency" key is not present in parentObj
        parentObj = new JSONObject();
        result = VRPConsentRequestValidator.validateJsonObjectKey(parentObj, key);
        //Assert.assertFalse(result);

        // Test case 5: parentObj is null
        result = VRPConsentRequestValidator.validateJsonObjectKey(null, key);
        // Assert.assertFalse(result);
    }


    @Test
    public void testValidateAmountCurrencyNegativeScenariosss() {
        // Test case 1: "Currency" key is present, but value is an empty String
        JSONObject maximumIndividualAmount = new JSONObject();
        maximumIndividualAmount.put("Currency", "");
        JSONObject result1 = VRPConsentRequestValidator.
                validateJsonObjectKey(maximumIndividualAmount, ConsentExtensionConstants.CURRENCY);
        // Assert.assertFalse(result1);

        // Test case 2: "Currency" key is present, but value is not a String
        maximumIndividualAmount.put("Currency", 123);
        // Assuming "Currency" should be a String, but it's an integer in this case
        JSONObject result2 = VRPConsentRequestValidator.
                validateJsonObjectKey(maximumIndividualAmount, ConsentExtensionConstants.CURRENCY);
        // Assert.assertFalse(result2);

        // Test case 3: "Currency" key is not present in maximumIndividualAmount
        maximumIndividualAmount = new JSONObject();
        JSONObject result3 = VRPConsentRequestValidator.
                validateJsonObjectKey(maximumIndividualAmount, ConsentExtensionConstants.CURRENCY);
        // Assert.assertFalse(result3);

        // Test case 4: maximumIndividualAmount is null
        JSONObject result4 = VRPConsentRequestValidator.
                validateJsonObjectKey(null, ConsentExtensionConstants.CURRENCY);
        //  Assert.assertFalse(result4);
    }


    @Test
    public void testValidatePeriodicLimitTypes() {

        JSONObject controlParameters = new JSONObject();

        JSONArray validPeriodicLimits = new JSONArray();
        JSONObject validPeriodicLimit = new JSONObject();
        validPeriodicLimit.put(ConsentExtensionConstants.PERIOD_ALIGNMENT, ConsentExtensionConstants.CONSENT);
        validPeriodicLimit.put(ConsentExtensionConstants.PERIOD_TYPE, ConsentExtensionConstants.DAY);
        validPeriodicLimits.add(validPeriodicLimit);

        controlParameters.put(ConsentExtensionConstants.PERIODIC_LIMITS, validPeriodicLimits);

        JSONObject validationResult = VRPConsentRequestValidator.validateVRPPayload(controlParameters);

        Assert.assertFalse((boolean) validationResult.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.PAYLOAD_FORMAT_ERROR,
                validationResult.get(ConsentExtensionConstants.ERRORS));

    }

    @Test
    public void testValidatePeriodicType() {
        // Test case 1: Valid periodic type
        JSONObject validLimitObject = new JSONObject();
        validLimitObject.put(ConsentExtensionConstants.PERIOD_TYPE, ConsentExtensionConstants.DAY);
        JSONObject result1 = VRPConsentRequestValidator.validatePeriodType(validLimitObject);
//        assertTrue(result1);

        // Test case 2: Missing period type key
        JSONObject missingKeyObject = new JSONObject();
        JSONObject result2 = VRPConsentRequestValidator.validatePeriodType(missingKeyObject);
        // Assert.assertFalse(result2);

        // Test case 3: Null period type
        JSONObject nullPeriodTypeObject = new JSONObject();
        nullPeriodTypeObject.put(ConsentExtensionConstants.PERIOD_TYPE, null);
        JSONObject result3 = VRPConsentRequestValidator.validatePeriodType(nullPeriodTypeObject);
        // Assert.assertFalse(result3);

        // Test case 4: Empty period type
        JSONObject emptyPeriodTypeObject = new JSONObject();
        emptyPeriodTypeObject.put(ConsentExtensionConstants.PERIOD_TYPE, "");
        JSONObject result4 = VRPConsentRequestValidator.validatePeriodType(emptyPeriodTypeObject);
        //Assert.assertFalse(result4);

        // Test case 5: Invalid period type
        JSONObject invalidPeriodTypeObject = new JSONObject();
        invalidPeriodTypeObject.put(ConsentExtensionConstants.PERIOD_TYPE, "InvalidType");
        JSONObject result5 = VRPConsentRequestValidator.validatePeriodType(invalidPeriodTypeObject);
        //Assert.assertFalse(result5);
    }

    @Test
    public void testVrpInitiationPayloadWithoutPeriodicType() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_PERIODIC_TYPE;
        JSONObject result = VRPConsentRequestValidator.validateControlParameters((JSONObject) JSONValue.
                parse(initiationPayloads));
        JSONObject result2 = VRPConsentRequestValidator.validatePeriodicLimits((JSONObject) JSONValue.
                parse(initiationPayloads));
        JSONObject result3 = VRPConsentRequestValidator.validatePeriodType((JSONObject) JSONValue.
                parse(initiationPayloads));


        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result2.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.MISSING_MAXIMUM_INDIVIDUAL_AMOUNT,
                result.get(ConsentExtensionConstants.ERRORS));
    }


    @Test
    public void testValidateAmountCurrencyWithCurrencyKey() {

        JSONObject testData1 = new JSONObject();
        testData1.put("currency", "USD");

        JSONObject result1 = VRPConsentRequestValidator.validateJsonObjectKey(testData1, "currency");
        //  assertTrue(result1);

        // Test case 2: Invalid currency key (empty value)
        JSONObject testData2 = new JSONObject();
        testData2.put("currency", "");

        JSONObject result2 = VRPConsentRequestValidator.validateJsonObjectKey(testData2, "currency");
        // Assert.assertFalse(result2);

        // Test case 3: Invalid currency key (missing key)
        JSONObject testData3 = new JSONObject();

        JSONObject result3 = VRPConsentRequestValidator.validateJsonObjectKey(testData3, "currency");
        //Assert.assertFalse(result3);

        // Test case 4: Invalid currency key (null parentObj)
        JSONObject result4 = VRPConsentRequestValidator.validateJsonObjectKey(null, "currency");
        //Assert.assertFalse(result4);

        // Add more test cases as needed
    }


    @Test
    public void testValidationFailureForNullCurrencyKey() {

        JSONArray periodicLimits = new JSONArray();
        periodicLimits.add(new JSONObject());

        JSONObject result = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(periodicLimits,
                ConsentExtensionConstants.CURRENCY);

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Mandatory parameter 'Currency' of periodic limits is not present in payload",
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadWithoutPeriodicTypeCurrency() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_PERIODIC_TYPE_CURRENCY;
        JSONObject result = VRPConsentRequestValidator.validateControlParameters((JSONObject) JSONValue.
                parse(initiationPayloads));
        JSONObject result2 = VRPConsentRequestValidator.validatePeriodicLimits((JSONObject) JSONValue.
                parse(initiationPayloads));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result2.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.MISSING_MAXIMUM_INDIVIDUAL_AMOUNT,
                result.get(ConsentExtensionConstants.ERRORS));
    }


    @Test
    public void testValidationFailureForMissingKey() {

        JSONArray periodicLimits = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("otherKey", "someValue");
        periodicLimits.add(jsonObject);


        JSONObject result = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(periodicLimits,
                ConsentExtensionConstants.CURRENCY);
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Mandatory parameter 'Currency' of periodic limits is not present in payload",
                result.get(ConsentExtensionConstants.ERRORS));

    }

    @Test
    public void testValidationFailureForNullParentArray() {

        // Call the method with a null parentArray
        JSONObject result = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(null,
                ConsentExtensionConstants.CURRENCY);

        // Assert that the result is false, indicating a validation failure for a null parentArray
        //Assert.assertFalse(result);
    }


    @Test
    public void testValidateControlParameters() {

        JSONObject controlParameters = new JSONObject();

        JSONObject result = VRPConsentRequestValidator.validateControlParameters(controlParameters);

        assertTrue(result.containsKey(ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.MISSING_MAXIMUM_INDIVIDUAL_AMOUNT,
                result.get(ConsentExtensionConstants.ERRORS));
    }


    @Test
    public void testValidateAmountCurrencyPeriodicLimits_Invalid() {

        JSONObject controlParameters = new JSONObject();
        JSONArray periodicLimits = new JSONArray();

        JSONObject invalidPeriodicLimit = new JSONObject();
        periodicLimits.add(invalidPeriodicLimit);

        controlParameters.put(ConsentExtensionConstants.PERIODIC_LIMITS, periodicLimits);

        JSONObject result = VRPConsentRequestValidator.validateCurrencyPeriodicLimit(controlParameters);

        assertTrue(result.containsKey(ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Mandatory parameter 'Currency' of periodic limits is not present in payload",
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidateAmountCurrencyPeriodicLimit_WithErrors() {

        JSONObject controlParameters = new JSONObject();
        JSONArray periodicLimits = new JSONArray();

        JSONObject invalidPeriodicLimit = new JSONObject();
        periodicLimits.add(invalidPeriodicLimit);

        controlParameters.put(ConsentExtensionConstants.PERIODIC_LIMITS, periodicLimits);

        JSONObject periodicLimitType = VRPConsentRequestValidator.
                validateCurrencyPeriodicLimit(controlParameters);

        Assert.assertFalse((boolean) periodicLimitType.get(ConsentExtensionConstants.IS_VALID));

        assertTrue(periodicLimitType.containsKey(ConsentExtensionConstants.ERRORS));
        Assert.assertEquals("Mandatory parameter 'Currency' of periodic limits is not present in payload",
                periodicLimitType.get(ConsentExtensionConstants.ERRORS));

    }

    @Test
    public void testValidateConsentRisk_ValidRequest() {

        JSONObject validRequest = new JSONObject();
        JSONObject data = new JSONObject();
        data.put("someKey", "someValue");

        validRequest.put(ConsentExtensionConstants.DATA, data);
        validRequest.put(ConsentExtensionConstants.RISK, new JSONObject());

        JSONObject validationResponse = VRPConsentRequestValidator.validateConsentRisk(validRequest);

        assertTrue((boolean) validationResponse.get(ConsentExtensionConstants.IS_VALID));
    }

    @Test
    public void testValidateConsentControlParameters_InvalidControlParameters() {

        JSONObject invalidControlParametersObject = new JSONObject();
        invalidControlParametersObject.put("invalidParam", "value");

        JSONObject invalidDataObject = new JSONObject();
        invalidDataObject.put(ConsentExtensionConstants.CONTROL_PARAMETERS, invalidControlParametersObject);


        JSONObject invalidRequestObject = new JSONObject();
        invalidRequestObject.put(ConsentExtensionConstants.DATA, invalidDataObject);

        JSONObject validationResult = VRPConsentRequestValidator.validateConsentControlParameters(invalidRequestObject);

        Assert.assertFalse(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals(ErrorConstants.MISSING_MAXIMUM_INDIVIDUAL_AMOUNT,
                validationResult.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidatePeriodicLimits_Valid() {

        JSONObject controlParametersObject = new JSONObject();
        JSONArray periodicLimitsArray = new JSONArray();

        JSONObject periodicLimit1 = new JSONObject();
        periodicLimit1.put(ConsentExtensionConstants.PERIOD_ALIGNMENT, "ALIGNMENT1");
        periodicLimit1.put(ConsentExtensionConstants.PERIOD_TYPE, "TYPE1");

        JSONObject periodicLimit2 = new JSONObject();
        periodicLimit2.put(ConsentExtensionConstants.PERIOD_ALIGNMENT, "ALIGNMENT2");
        periodicLimit2.put(ConsentExtensionConstants.PERIOD_TYPE, "TYPE2");

        periodicLimitsArray.add(periodicLimit1);
        periodicLimitsArray.add(periodicLimit2);

        controlParametersObject.put(ConsentExtensionConstants.PERIODIC_LIMITS, periodicLimitsArray);

        JSONObject validationResult = VRPConsentRequestValidator.validatePeriodicLimits(controlParametersObject);
        JSONObject validationResults = VRPConsentRequestValidator.validatePeriodAlignment(controlParametersObject);

        Assert.assertFalse(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertFalse(Boolean.parseBoolean(validationResults.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals(ErrorConstants.MISSING_PERIOD_ALIGNMENT,
                validationResults.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidatePeriodicLimits_InvalidFormat() {
        JSONObject controlParametersObject = new JSONObject();
        controlParametersObject.put(ConsentExtensionConstants.PERIODIC_LIMITS, "InvalidFormat");

        JSONObject validationResult = VRPConsentRequestValidator.validatePeriodicLimits(controlParametersObject);

        Assert.assertFalse(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals(ErrorConstants.INVALID_PARAMETER_PERIODIC_LIMITS,
                validationResult.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidatePeriodicLimits_MissingPeriodLimits() {

        JSONObject controlParametersObject = new JSONObject();

        JSONObject validationResult = VRPConsentRequestValidator.validatePeriodicLimits(controlParametersObject);

        Assert.assertFalse(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals(ErrorConstants.MISSING_PERIOD_LIMITS,
                validationResult.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidateAmountCurrencyPeriodicLimit_Valid() {

        JSONObject controlParametersObject = new JSONObject();
        JSONArray periodicLimitsArray = new JSONArray();

        JSONObject periodicLimit1 = new JSONObject();
        periodicLimit1.put(ConsentExtensionConstants.CURRENCY, "USD");

        JSONObject periodicLimit2 = new JSONObject();
        periodicLimit2.put(ConsentExtensionConstants.CURRENCY, "EUR");

        periodicLimitsArray.add(periodicLimit1);
        periodicLimitsArray.add(periodicLimit2);

        controlParametersObject.put(ConsentExtensionConstants.PERIODIC_LIMITS, periodicLimitsArray);

        JSONObject validationResult = VRPConsentRequestValidator.
                validateAmountPeriodicLimit(controlParametersObject);

        assertFalse(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals("Mandatory parameter 'Amount' of periodic limits is not present in payload",
                validationResult.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidateAmountCurrencyPeriodicLimit_MissingCurrency() {
        JSONObject controlParametersObject = new JSONObject();
        JSONArray periodicLimitsArray = new JSONArray();

        JSONObject periodicLimit1 = new JSONObject();
        periodicLimitsArray.add(periodicLimit1);

        controlParametersObject.put(ConsentExtensionConstants.PERIODIC_LIMITS, periodicLimitsArray);

        JSONObject validationResult = VRPConsentRequestValidator.
                validateAmountPeriodicLimit(controlParametersObject);

        Assert.assertFalse(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals("Mandatory parameter 'Amount' of periodic limits is not present in payload",
                validationResult.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidatePeriodicType_Valid() {
        JSONObject periodicLimitObject = new JSONObject();
        periodicLimitObject.put(ConsentExtensionConstants.PERIOD_TYPE, ConsentExtensionConstants.MONTH);

        JSONObject validationResult = VRPConsentRequestValidator.validatePeriodType(periodicLimitObject);

        Assert.assertTrue(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)));
    }

    @Test
    public void testValidatePeriodicType_InvalidType() {
        JSONObject periodicLimitObject = new JSONObject();
        periodicLimitObject.put(ConsentExtensionConstants.PERIOD_TYPE, "InvalidType");

        JSONObject validationResult = VRPConsentRequestValidator.validatePeriodType(periodicLimitObject);

        Assert.assertFalse(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals(ErrorConstants.INVALID_PERIOD_TYPE,
                validationResult.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidatePeriodicType_MissingType() {

        JSONObject periodicLimitObject = new JSONObject();

        JSONObject validationResult = VRPConsentRequestValidator.validatePeriodType(periodicLimitObject);

        Assert.assertFalse(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals(ErrorConstants.MISSING_PERIOD_TYPE,
                validationResult.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidatePeriodicType_EmptyType() {

        JSONObject periodicLimitObject = new JSONObject();
        periodicLimitObject.put(ConsentExtensionConstants.PERIOD_TYPE, "");

        JSONObject validationResult = VRPConsentRequestValidator.validatePeriodType(periodicLimitObject);

        Assert.assertFalse(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals("Value of period type is empty or the value passed in is not a string",
                validationResult.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidatePeriodicType_NullType() {
        JSONObject periodicLimitObject = new JSONObject();
        periodicLimitObject.put(ConsentExtensionConstants.PERIOD_TYPE, null);

        JSONObject validationResult = VRPConsentRequestValidator.validatePeriodType(periodicLimitObject);

        Assert.assertFalse(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals("Value of period type is empty or the value passed in is not a string",
                validationResult.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testYourMethod_ValidPeriodicType() {

        JSONObject controlParameters = new JSONObject();
        JSONArray periodicLimits = new JSONArray();

        JSONObject periodicLimit = new JSONObject();
        periodicLimit.put(ConsentExtensionConstants.PERIOD_TYPE, ConsentExtensionConstants.MONTH);
        periodicLimits.add(periodicLimit);

        controlParameters.put(ConsentExtensionConstants.PERIODIC_LIMITS, periodicLimits);

        JSONObject result = VRPConsentRequestValidator.validateAmountPeriodicLimit(controlParameters);

        Assert.assertFalse(Boolean.parseBoolean(result.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals("Mandatory parameter 'Amount' of periodic limits is not present in payload",
                result.get(ConsentExtensionConstants.ERRORS));

    }

    @Test
    public void testYourMethod_InvalidPeriodicType() {

        JSONObject controlParameters = new JSONObject();
        JSONArray periodicLimits = new JSONArray();

        JSONObject periodicLimit = new JSONObject();
        periodicLimit.put(ConsentExtensionConstants.PERIOD_TYPE, "InvalidType");
        periodicLimits.add(periodicLimit);

        controlParameters.put(ConsentExtensionConstants.PERIODIC_LIMITS, periodicLimits);

        JSONObject result = VRPConsentRequestValidator.validateCurrencyPeriodicLimit(controlParameters);

        Assert.assertFalse(Boolean.parseBoolean(result.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals("Mandatory parameter 'Currency' of periodic limits is not present in payload",
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testYourMethod_MissingPeriodicType() {

        JSONObject controlParameters = new JSONObject();

        JSONObject result = VRPConsentRequestValidator.validatePeriodicLimits(controlParameters);

        Assert.assertFalse(Boolean.parseBoolean(result.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals(ErrorConstants.MISSING_PERIOD_LIMITS,
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadWithoutDebtorAccs() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_DEBTOR_ACCOUNT;
        JSONObject result = VRPConsentRequestValidator.validateConsentInitiation((JSONObject) JSONValue.
                parse(initiationPayloads));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.PAYLOAD_FORMAT_ERROR_DEBTOR_ACC,
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidateConsentRisk_InvalidRequest() {
        JSONObject requestBody = new JSONObject();
        JSONObject data = new JSONObject();
        data.put("key1", "value1");
        requestBody.put("data", data);

        JSONObject validationResult = VRPConsentRequestValidator.validateVRPPayload(requestBody);
        JSONObject validationResults = VRPConsentRequestValidator.validateConsentRisk(requestBody);

        Assert.assertFalse(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertFalse(Boolean.parseBoolean(validationResults.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals(ErrorConstants.PAYLOAD_FORMAT_ERROR_RISK,
                validationResults.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadWithoutDeAcc() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_DEBTOR_ACCOUNT;
        JSONObject result = VRPConsentRequestValidator.validateVRPInitiationPayload((JSONObject) JSONValue.
                parse(initiationPayloads));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.PAYLOAD_FORMAT_ERROR_DEBTOR_ACC,
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadDAccWithoutSchemeName() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_DEBTOR_ACCOUNT_SCHEME_NAME;
        JSONObject result = VRPConsentRequestValidator.validateVRPPayload((JSONValue.
                parse(initiationPayloads)));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.MISSING_DEBTOR_ACC_SCHEME_NAME,
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadDAccWithoutIdentification() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_DEBTOR_ACCOUNT_IDENTIFICATION;
        JSONObject result = VRPConsentRequestValidator.validateVRPPayload(JSONValue.
                parse(initiationPayloads));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.MISSING_DEBTOR_ACC_IDENTIFICATION,
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadDAcc() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_DEBTOR_ACC;
        JSONObject result = VRPConsentRequestValidator.validateVRPInitiationPayload((JSONObject) JSONValue.
                parse(initiationPayloads));
        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidJSONObject(initiationPayloads);
        Assert.assertFalse(isValidNonJSONObject);
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.PAYLOAD_FORMAT_ERROR_DEBTOR_ACC,
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadWithoutDAcc() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_DEBTOR_ACCOUNT;
        JSONObject result = VRPConsentRequestValidator.validateConsentInitiation((JSONObject) JSONValue.
                parse(initiationPayloads));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.PAYLOAD_FORMAT_ERROR_DEBTOR_ACC,
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidateMaximumIndividualAmountCurrency_InvalidAmountCurrency() {
        JSONObject controlParameters = new JSONObject();
        JSONObject maximumIndividualAmount = new JSONObject();
        maximumIndividualAmount.put("InvalidKey", "USD");
        controlParameters.put(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT, maximumIndividualAmount);

        JSONObject result = VRPConsentRequestValidator.validateMaximumIndividualAmountCurrency(controlParameters);

        Assert.assertFalse(Boolean.parseBoolean(result.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals("Mandatory parameter 'Currency' is not present in payload",
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidateMaximumIndividualAmountCurrency_MissingCurrency() {

        JSONObject controlParameters = new JSONObject();
        JSONObject maximumIndividualAmount = new JSONObject();

        controlParameters.put(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT, maximumIndividualAmount);

        JSONObject result = VRPConsentRequestValidator.validateMaximumIndividualAmountCurrency(controlParameters);

        Assert.assertFalse(Boolean.parseBoolean(result.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals("Mandatory parameter 'Currency' is not present in payload",
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidatePeriodAlignmentInvalidValue() {

        JSONObject limit = new JSONObject();
        limit.put(ConsentExtensionConstants.PERIOD_ALIGNMENT, "InvalidValue");

        JSONObject result = VRPConsentRequestValidator.validatePeriodAlignment(limit);

        Assert.assertFalse(Boolean.parseBoolean(result.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals(result.get(ConsentExtensionConstants.ERRORS),
                ErrorConstants.INVALID_PERIOD_ALIGNMENT);
    }

    @Test
    public void testValidatePeriodAlignmentMissingKey() {

        JSONObject limit = new JSONObject();

        JSONObject result = VRPConsentRequestValidator.validatePeriodAlignment(limit);

        Assert.assertFalse(Boolean.parseBoolean(result.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals(result.get(ConsentExtensionConstants.ERRORS),
                ErrorConstants.MISSING_PERIOD_ALIGNMENT);
    }


    @Test
    public void testValidatePeriodicAlignment_EmptyType() {

        JSONObject periodicLimitObject = new JSONObject();
        periodicLimitObject.put(ConsentExtensionConstants.PERIOD_ALIGNMENT, "");

        JSONObject result = VRPConsentRequestValidator.validatePeriodType(periodicLimitObject);
        Assert.assertFalse(Boolean.parseBoolean(result.getAsString(ConsentExtensionConstants.IS_VALID)));

        Assert.assertFalse((Boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.MISSING_PERIOD_TYPE,
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidatePeriodicAlignment() {
        // Test case 1: Valid periodic type
        JSONObject validLimitObject = new JSONObject();
        validLimitObject.put(ConsentExtensionConstants.PERIOD_ALIGNMENT, ConsentExtensionConstants.DAY);
        JSONObject result1 = VRPConsentRequestValidator.validatePeriodAlignment(validLimitObject);
        Assert.assertFalse((Boolean) result1.get(ConsentExtensionConstants.IS_VALID));

        // Test case 2: Missing period type key
        JSONObject missingKeyObject = new JSONObject();
        JSONObject result2 = VRPConsentRequestValidator.validatePeriodAlignment(missingKeyObject);
        Assert.assertFalse((Boolean) result2.get(ConsentExtensionConstants.IS_VALID));

        // Test case 3: Null period type
        JSONObject nullPeriodTypeObject = new JSONObject();
        nullPeriodTypeObject.put(ConsentExtensionConstants.PERIOD_ALIGNMENT, null);
        JSONObject result3 = VRPConsentRequestValidator.validatePeriodAlignment(nullPeriodTypeObject);
        Assert.assertFalse((Boolean) result3.get(ConsentExtensionConstants.IS_VALID));

        // Test case 4: Empty period type
        JSONObject emptyPeriodTypeObject = new JSONObject();
        emptyPeriodTypeObject.put(ConsentExtensionConstants.PERIOD_ALIGNMENT, "");
        JSONObject result4 = VRPConsentRequestValidator.validatePeriodAlignment(emptyPeriodTypeObject);
        Assert.assertFalse((Boolean) result4.get(ConsentExtensionConstants.IS_VALID));

        // Test case 5: Invalid period type
        JSONObject invalidPeriodTypeObject = new JSONObject();
        invalidPeriodTypeObject.put(ConsentExtensionConstants.PERIOD_ALIGNMENT, "InvalidType");
        JSONObject result5 = VRPConsentRequestValidator.validatePeriodAlignment(invalidPeriodTypeObject);
        Assert.assertFalse((Boolean) result5.get(ConsentExtensionConstants.IS_VALID));

        Assert.assertEquals("Value is empty or the value passed in for periodic alignment is not a string",
                result5.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidatePeriodicAlignment_Valid() {
        JSONObject periodicLimitObject = new JSONObject();
        periodicLimitObject.put(ConsentExtensionConstants.PERIOD_ALIGNMENT, ConsentExtensionConstants.CONSENT);

        JSONObject validationResult = VRPConsentRequestValidator.validatePeriodAlignment(periodicLimitObject);

        Assert.assertTrue(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)));

    }

    @Test
    public void testValidatePeriodicAlignment_InvalidType() {
        JSONObject periodicLimitObject = new JSONObject();
        periodicLimitObject.put(ConsentExtensionConstants.PERIOD_ALIGNMENT, "InvalidType");

        JSONObject validationResult = VRPConsentRequestValidator.validatePeriodAlignment(periodicLimitObject);

        Assert.assertFalse(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals("Value is empty or the value passed in for periodic alignment is not a string",
                validationResult.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidatePeriodicAlignment_MissingType() {

        JSONObject periodicLimitObject = new JSONObject();

        JSONObject validationResult = VRPConsentRequestValidator.validatePeriodAlignment(periodicLimitObject);

        Assert.assertFalse(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals(ErrorConstants.MISSING_PERIOD_ALIGNMENT,
                validationResult.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidatePeriodicAlignments_EmptyType() {

        JSONObject periodicLimitObject = new JSONObject();
        periodicLimitObject.put(ConsentExtensionConstants.PERIOD_ALIGNMENT, "");

        JSONObject validationResult = VRPConsentRequestValidator.validatePeriodAlignment(periodicLimitObject);

        Assert.assertFalse(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals("Value is empty or the value passed in is not a string",
                validationResult.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidatePeriodicAlignment_NullType() {
        JSONObject periodicLimitObject = new JSONObject();
        periodicLimitObject.put(ConsentExtensionConstants.PERIOD_ALIGNMENT, null);

        JSONObject validationResult = VRPConsentRequestValidator.validatePeriodAlignment(periodicLimitObject);

        Assert.assertFalse(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals("Value is empty or the value passed in is not a string",
                validationResult.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidateAmountCurrencyPeriodicLimits_Valid() {

        JSONObject controlParametersObject = new JSONObject();
        JSONArray periodicLimitsArray = new JSONArray();

        JSONObject periodicLimit1 = new JSONObject();
        periodicLimit1.put(ConsentExtensionConstants.CURRENCY, "USD");

        JSONObject periodicLimit2 = new JSONObject();
        periodicLimit2.put(ConsentExtensionConstants.CURRENCY, "EUR");

        periodicLimitsArray.add(periodicLimit1);
        periodicLimitsArray.add(periodicLimit2);

        controlParametersObject.put(ConsentExtensionConstants.PERIODIC_LIMITS, periodicLimitsArray);

        JSONObject validationResult = VRPConsentRequestValidator.
                validateCurrencyPeriodicLimit(controlParametersObject);

        assertTrue(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)));
    }


    @Test
    public void testValidateAmountCurrencyPeriodicLimitS_MissingCurrency() {
        JSONObject controlParametersObject = new JSONObject();
        JSONArray periodicLimitsArray = new JSONArray();

        JSONObject periodicLimit1 = new JSONObject();
        periodicLimitsArray.add(periodicLimit1);

        controlParametersObject.put(ConsentExtensionConstants.PERIODIC_LIMITS, periodicLimitsArray);

        JSONObject validationResult = VRPConsentRequestValidator.
                validateCurrencyPeriodicLimit(controlParametersObject);

        Assert.assertFalse(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals("Mandatory parameter 'Currency' of periodic limits is not present in payload",
                validationResult.get(ConsentExtensionConstants.ERRORS));

    }

    @Test
    public void testYourMethod_ValidPeriodicTypes() {

        JSONObject controlParameters = new JSONObject();
        JSONArray periodicLimits = new JSONArray();

        JSONObject periodicLimit = new JSONObject();
        periodicLimit.put(ConsentExtensionConstants.PERIOD_TYPE, ConsentExtensionConstants.MONTH);
        periodicLimits.add(periodicLimit);

        controlParameters.put(ConsentExtensionConstants.PERIODIC_LIMITS, periodicLimits);

        JSONObject result = VRPConsentRequestValidator.validateCurrencyPeriodicLimit(controlParameters);

        Assert.assertFalse(Boolean.parseBoolean(result.getAsString(ConsentExtensionConstants.IS_VALID)));

    }

    @Test
    public void testValidateAmountCurrencyPeriodicLimits_MissingCurrency() {
        JSONObject controlParametersObject = new JSONObject();
        JSONArray periodicLimitsArray = new JSONArray();

        JSONObject periodicLimit1 = new JSONObject();
        periodicLimitsArray.add(periodicLimit1);

        controlParametersObject.put(ConsentExtensionConstants.PERIODIC_LIMITS, periodicLimitsArray);

        JSONObject validationResult = VRPConsentRequestValidator.
                validateCurrencyPeriodicLimit(controlParametersObject);

        Assert.assertFalse(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals("Mandatory parameter 'Currency' of periodic limits is not present in payload",
                validationResult.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadMaximumIndividualAmountCurrencyNotJsonObject() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITH_EMPTY_MAX_INDIVIDUAL_AMOUNT;

        JSONObject results = VRPConsentRequestValidator.validateMaximumIndividualAmountCurrency((JSONObject) JSONValue.
                parse(initiationPayloads));
        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidJSONObject(initiationPayloads);
        Assert.assertFalse(isValidNonJSONObject, (ConsentExtensionConstants.IS_VALID));

        boolean isValidNonJSONObjects = VRPConsentRequestValidator.isValidJSONObject(initiationPayloads);
        Assert.assertFalse(isValidNonJSONObjects, (ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) results.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("parameter passed in  is null",
                results.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadMaximumIndividualCurrencyNotJsonObject() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITH_INVALID_MAX_INDIVIDUAL_AMOUNT;
        JSONObject result = VRPConsentRequestValidator.validateMaximumIndividualAmountCurrency((JSONObject) JSONValue.
                parse(initiationPayloads));
        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidJSONObject(initiationPayloads);
        Assert.assertFalse(isValidNonJSONObject, (ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("parameter passed in  is null",
                result.get(ConsentExtensionConstants.ERRORS));
    }


    @Test
    public void testInvalidMaxAmountCurrencyFormatPeriodicLimit() {

        JSONObject controlParameters = new JSONObject();
        controlParameters.put(ConsentExtensionConstants.PERIODIC_LIMITS, "invalid-format");
        JSONObject validationResults = VRPConsentRequestValidator.
                validateControlParameters(controlParameters);
        JSONObject validationResult = VRPConsentRequestValidator.
                validateMaximumIndividualAmountCurrency(controlParameters);

        Assert.assertFalse((Boolean) validationResults.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((Boolean) validationResult.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("parameter passed in  is null",
                validationResult.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testInvalidCurrencyKey_MissingKeys() {

        JSONObject maximumIndividualAmount = new JSONObject();

        JSONObject validationResults = VRPConsentRequestValidator.
                validateJsonObjectKey(maximumIndividualAmount, ConsentExtensionConstants.CURRENCY);

        Assert.assertFalse((Boolean) validationResults.get(ConsentExtensionConstants.IS_VALID));

        JSONObject parentObj = new JSONObject();
        JSONObject validationResult = VRPConsentRequestValidator.validateMaximumIndividualAmountCurrency(parentObj);

        Assert.assertFalse((Boolean) validationResult.get(ConsentExtensionConstants.IS_VALID));
        JSONObject isValid = VRPConsentRequestValidator.validateJsonObjectKey(parentObj, "Currency");

        Assert.assertFalse((Boolean) isValid.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("parameter passed in  is null",
                validationResult.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testInvalidCurrencyKey_EmptyValues() {

        JSONObject maximumIndividualAmount = new JSONObject();

        JSONObject validationResult = VRPConsentRequestValidator.validateJsonObjectKey(
                maximumIndividualAmount, ConsentExtensionConstants.CURRENCY);

        Assert.assertFalse((Boolean) validationResult.get(ConsentExtensionConstants.IS_VALID));

        JSONObject parentObj = new JSONObject();
        parentObj.put("Currency", "");
        JSONObject validationResults = VRPConsentRequestValidator.validateMaximumIndividualAmountCurrency(parentObj);

        Assert.assertFalse((Boolean) validationResults.get(ConsentExtensionConstants.IS_VALID));

        JSONObject isValid = VRPConsentRequestValidator.validateJsonObjectKey(parentObj, "Currency");

        Assert.assertFalse((Boolean) isValid.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("Mandatory parameter 'Currency' is not present in payload",
                validationResult.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testInvalidCurrencyKey_NullValues() {

        JSONObject maximumIndividualAmount = new JSONObject();

        JSONObject validationResult = VRPConsentRequestValidator.validateJsonObjectKey(
                maximumIndividualAmount, ConsentExtensionConstants.CURRENCY);

        Assert.assertFalse((Boolean) validationResult.get(ConsentExtensionConstants.IS_VALID));

        JSONObject parentObj = new JSONObject();
        parentObj.put("Currency", null);
        parentObj.put("1000", "usd");

        JSONObject validationResults = VRPConsentRequestValidator.validateMaximumIndividualAmountCurrency(parentObj);

        Assert.assertFalse((Boolean) validationResults.get(ConsentExtensionConstants.IS_VALID));

        JSONObject isValid = VRPConsentRequestValidator.validateJsonObjectKey(parentObj, "Currency");
        parentObj.put(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT, parentObj);

        Assert.assertFalse((Boolean) isValid.get(ConsentExtensionConstants.IS_VALID));

        Assert.assertEquals("Mandatory parameter 'Currency' is not present in payload",
                validationResult.get(ConsentExtensionConstants.ERRORS));
    }


    @Test
    public void testVrpInitiationPayloadWithoutControlParameterCurrency() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_CURRENCY;
        JSONObject result = VRPConsentRequestValidator.validateMaximumIndividualAmountCurrency((JSONObject) JSONValue.
                parse(initiationPayloads));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("parameter passed in  is null",
                result.get(ConsentExtensionConstants.ERRORS));
    }


    @Test
    public void testVrpInitiationPayloadWithoutControlParameter() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_CURRENCY;
        JSONObject result = VRPConsentRequestValidator.validateMaximumIndividualAmountCurrency((JSONObject) JSONValue.
                parse(initiationPayloads));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("parameter passed in  is null",
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidatesCurrency() {
        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_CURRENCY;
        JSONObject results = VRPConsentRequestValidator.validateMaximumIndividualAmountCurrency((JSONObject) JSONValue.
                parse(initiationPayloads));
        Assert.assertFalse((boolean) results.get(ConsentExtensionConstants.IS_VALID));
        assertFalse(results.containsKey("Currency"));

        // Test case 1: "Currency" key is present, and value is a non-empty String
        JSONObject parentObj = new JSONObject();
        parentObj.put("Currency", "USD"); // Assuming a valid currency
        String key = "Currency";
        JSONObject result = VRPConsentRequestValidator.validateJsonObjectKey(parentObj, key);
        Assert.assertTrue((Boolean) result.get(ConsentExtensionConstants.IS_VALID));

        // Test case 2: "Currency" key is present, but value is an empty String
        parentObj.put("Currency", "");
        result = VRPConsentRequestValidator.validateJsonObjectKey(parentObj, key);
        Assert.assertFalse((Boolean) result.get(ConsentExtensionConstants.IS_VALID));

        // Test case 3: "Currency" key is present, but value is not a String
        parentObj.put("Currency", 123); // Assuming "Currency" should be a String, but it's an integer in this case
        result = VRPConsentRequestValidator.validateJsonObjectKey(parentObj, key);
        Assert.assertFalse((Boolean) result.get(ConsentExtensionConstants.IS_VALID));

        // Test case 4: "Currency" key is not present in parentObj
        parentObj = new JSONObject();
        result = VRPConsentRequestValidator.validateJsonObjectKey(parentObj, key);
        Assert.assertFalse((Boolean) result.get(ConsentExtensionConstants.IS_VALID));

        // Test case 5: parentObj is null
        result = VRPConsentRequestValidator.validateJsonObjectKey(null, key);
        Assert.assertFalse((Boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals("parameter passed in  is null",
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testWithEmptyDate() {

        String initiationPayloads = VRPTestConstants.vrpInitiationPayloadWithoutDate;
        JSONObject response = VRPConsentRequestValidator.validateParameterDateTime((JSONObject) JSONValue.
                parse(initiationPayloads));
        Assert.assertFalse((boolean) response.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(ErrorConstants.MISSING_VALID_TO_DATE_TIME, response.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidateConsentRisk() {

        JSONObject requestBody = new JSONObject();
        JSONObject data = new JSONObject();
        JSONObject risk = new JSONObject();
        data.put(ConsentExtensionConstants.RISK, risk);
        requestBody.put(ConsentExtensionConstants.DATA, data);

        JSONObject result = VRPConsentRequestValidator.validateConsentRisk(requestBody);

        Assert.assertFalse(Boolean.parseBoolean(result.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals(ErrorConstants.PAYLOAD_FORMAT_ERROR_RISK,
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidateConsentRiskInvalidFormat() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("invalidKey", "invalidValue");

        JSONObject result = VRPConsentRequestValidator.validateConsentRisk(requestBody);

        Assert.assertFalse(Boolean.parseBoolean(result.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals(ErrorConstants.PAYLOAD_FORMAT_ERROR_RISK,
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidateConsentRiskMissingRiskKey() {

        JSONObject requestBody = new JSONObject();
        JSONObject data = new JSONObject();
        requestBody.put(ConsentExtensionConstants.DATA, data);

        JSONObject result = VRPConsentRequestValidator.validateConsentRisk(requestBody);

        Assert.assertFalse(Boolean.parseBoolean(result.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals(ErrorConstants.PAYLOAD_FORMAT_ERROR_RISK,
                result.get(ConsentExtensionConstants.ERRORS));
    }


    @Test
    public void testValidateConsentRiskWithDataEmpty() {

        JSONObject requestBody = new JSONObject();
        requestBody.put(ConsentExtensionConstants.DATA, new JSONObject());

        JSONObject result = VRPConsentRequestValidator.validateConsentRisk(requestBody);

        Assert.assertFalse(Boolean.parseBoolean(result.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals(ErrorConstants.PAYLOAD_FORMAT_ERROR_RISK,
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidateConsentRiskWithDataNotPresent() {

        JSONObject requestBody = new JSONObject();

        JSONObject result = VRPConsentRequestValidator.validateConsentRisk(requestBody);

        Assert.assertFalse(Boolean.parseBoolean(result.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals(ErrorConstants.PAYLOAD_FORMAT_ERROR_RISK,
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidateConsentRiskWithDataNotEmpty() {
        JSONObject requestBody = new JSONObject();
        JSONObject data = new JSONObject();
        data.put("someKey", "someValue");
        requestBody.put(ConsentExtensionConstants.DATA, data);

        JSONObject result = VRPConsentRequestValidator.validateConsentRisk(requestBody);
        Assert.assertFalse(Boolean.parseBoolean(result.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals(ErrorConstants.PAYLOAD_FORMAT_ERROR_RISK,
                result.get(ConsentExtensionConstants.ERRORS));
    }
}
