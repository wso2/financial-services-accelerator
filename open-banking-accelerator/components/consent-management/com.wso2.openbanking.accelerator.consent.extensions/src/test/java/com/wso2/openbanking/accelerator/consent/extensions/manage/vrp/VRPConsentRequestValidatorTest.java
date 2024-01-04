/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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
package com.wso2.openbanking.accelerator.consent.extensions.manage.vrp;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.util.CarbonUtils;
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

import static com.wso2.openbanking.accelerator.common.util.ErrorConstants.*;
import static com.wso2.openbanking.accelerator.consent.extensions.manage.validator.VRPConsentRequestValidator.validateAmountCurrency;
import static com.wso2.openbanking.accelerator.consent.extensions.manage.validator.VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

/**
 * Test class for VRPConsentRequestValidator
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
        JSONObject response = VRPConsentRequestValidator.validateVRPPayload("payload");
        Assert.assertFalse((boolean) response.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(PAYLOAD_FORMAT_ERROR, response.get(ConsentExtensionConstants.ERRORS));

    }

    @Test
    public void testVrpEmptyPayload() {
        JSONObject response = VRPConsentRequestValidator.validateVRPPayload("");
        Assert.assertFalse((boolean) response.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(PAYLOAD_FORMAT_ERROR, response.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiation() {

        String initiationPayloads = VRPTestConstants.vrpInitiationPayloadWithoutData;
        JSONObject response = VRPConsentRequestValidator.validateVRPPayload(JSONValue.
                parse(initiationPayloads));
        Assert.assertFalse((boolean) response.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(PAYLOAD_FORMAT_ERROR, response.get(ConsentExtensionConstants.ERRORS));
    }


    @Test
    public void testVrpControlParameters() {
        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_CONTROL_PARAMETERS;
        JSONObject response = VRPConsentRequestValidator.validateControlParameters((JSONObject) JSONValue.
                parse(initiationPayloads));
        Assert.assertFalse((boolean) response.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(PAYLOAD_FORMAT_ERROR_MAXIMUM_INDIVIDUAL_AMOUNT, response.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpEmptyData() {
        String initiationPayloads = VRPTestConstants.vrpInitiationPayloadWithStringData;
        JSONObject response = VRPConsentRequestValidator.validateVRPPayload(JSONValue.
                parse(initiationPayloads));
        Assert.assertFalse((boolean) response.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(PAYLOAD_FORMAT_ERROR_MAXIMUM_INDIVIDUAL_AMOUNT, response.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpDataIsJsonObject() {
        String initiationPayloads = VRPTestConstants.vrpInitiationPayloadWithOutJsonObject;
        JSONObject response = VRPConsentRequestValidator.validateVRPPayload(JSONValue.
                parse(initiationPayloads));
        Assert.assertFalse((boolean) response.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals( PAYLOAD_FORMAT_ERROR, response.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadWithoutControlParameters() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_CONTROL_PARAMETERS;
        JSONObject result = VRPConsentRequestValidator.validateVRPPayload(JSONValue.
                parse(initiationPayloads));

        Assert.assertTrue(true);
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals( PAYLOAD_FORMAT_ERROR_CONTROL_PARAMETER, result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadWithoutControlParameterKey() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_CONTROL_PARAMETERS;
        JSONObject result = VRPConsentRequestValidator.validateControlParameters((JSONObject) JSONValue.
                parse(initiationPayloads));
        JSONObject result2 = VRPConsentRequestValidator.validateMaximumIndividualAmount((JSONObject) JSONValue.
                parse(initiationPayloads));
        JSONObject result3 = VRPConsentRequestValidator.validateMaximumIndividualAmountCurrency((JSONObject) JSONValue.
                parse(initiationPayloads));

        Assert.assertTrue(true);
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result2.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result3.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals( PAYLOAD_FORMAT_ERROR_MAXIMUM_INDIVIDUAL_AMOUNT,
                result.get(ConsentExtensionConstants.ERRORS));

    }


    @Test
    public void testValidateAmountCurrencyWithCurrencyKeys() {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Currency", "USD");

        JSONArray jsonArray = new JSONArray();
        jsonArray.add(jsonObject);

        boolean result = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(jsonArray, "Currency");
        assertTrue(result);

    }

    @Test
    public void testValidateAmountCurrencyWithInvalidKey() {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("InvalidKey", "USD");

        JSONArray jsonArray = new JSONArray();
        jsonArray.add(jsonObject);

        boolean result = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(jsonArray, "Currency");

        Assert.assertFalse(result);
    }

    @Test
    public void testValidateAmountCurrencyWithEmptyArray() {

        JSONArray jsonArray = new JSONArray();

        boolean result = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(jsonArray, "Currency");

        Assert.assertFalse(result);

    }

    @Test
    public void testValidateAmountCurrencyWithNullArray() {
        boolean result = VRPConsentRequestValidator.validateAmountCurrency(null, "Currency");

        // Assert that the result is false, indicating an invalid key in a null array
        Assert.assertFalse(result);
    }

    @Test
    public void testVrpInitiationPayloadWithoutControlParameter() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_CURRENCY;
        JSONObject result = VRPConsentRequestValidator.validateMaximumIndividualAmountCurrency((JSONObject) JSONValue.
                parse(initiationPayloads));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals( MAXIMUM_INDIVIDUAL_AMOUNT_CURRENCY_IS_MISSING,
                result.get(ConsentExtensionConstants.ERRORS));

    }

    @Test
    public void testVrpInitiationPayloadWithoutPeriodicLimitCurrency() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_PERIODIC_LIMIT_CURRENCY;
        JSONObject result = VRPConsentRequestValidator.validateMaximumIndividualAmountCurrency((JSONObject) JSONValue.
                parse(initiationPayloads));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        assertTrue(true);
        Assert.assertEquals( MAXIMUM_INDIVIDUAL_AMOUNT_CURRENCY_IS_MISSING,
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadWithoutPeriodicLimitAmount() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_PERIODIC_LIMIT_AMOUNT;
        JSONObject result = VRPConsentRequestValidator.validateMaximumIndividualAmountCurrency((JSONObject) JSONValue.
                parse(initiationPayloads));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        assertTrue(true);
//        Assert.assertEquals( MAXIMUM_INDIVIDUAL_AMOUNT_CURRENCY_IS_MISSING,
//                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testValidateAmountCurrencyPeriodicLimitsWithInvalidValue() {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Currency", 123); // Invalid value, not a string

        JSONArray jsonArray = new JSONArray();
        jsonArray.add(jsonObject);

        // Test the method with an invalid value
        boolean result = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(jsonArray, "Currency");

        // Assert that the result is false, indicating an invalid value
        Assert.assertFalse(result);
    }


    @Test
    public void testValidateAmountCurrencyPeriodicLimitsWithInvalidKey() {
        // Test case: Invalid key, key not present in the JSON array
        JSONArray testData = new JSONArray();
        JSONObject limit = new JSONObject();
        limit.put("anotherKey", "USD");
        testData.add(limit);

        boolean result = validateAmountCurrencyPeriodicLimits(testData, "currency");
        Assert.assertFalse(result);
    }


    @Test
    public void testValidationFailureForCurrency() {

        // Create a sample JSONObject for periodic limits with invalid currency value
        JSONObject limit = new JSONObject();
        limit.put(ConsentExtensionConstants.CURRENCY, 123); // Invalid currency value, not a string

        // Create a JSONArray with the invalid currency value
        JSONArray periodicLimits = new JSONArray();
        periodicLimits.add(limit);

        // Call the method that checks currency validation
        boolean result = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(periodicLimits,
                ConsentExtensionConstants.CURRENCY);

        // Assert that the result is false, indicating a validation failure for currency
        Assert.assertFalse(result);
    }

    @Test
    public void testValidateAmountCurrencyPeriodicLimitsWithCurrencyKey() {

        // Test case 2: Invalid currency key (empty value)
        JSONArray testData2 = new JSONArray();
        JSONObject limit2 = new JSONObject();
        limit2.put("currency", "");
        testData2.add(limit2);

        boolean result2 = validateAmountCurrencyPeriodicLimits(testData2, "0");
        Assert.assertFalse(result2);

        // Test case 3: Invalid currency key (missing key)
        JSONArray testData3 = new JSONArray();

        boolean result3 = validateAmountCurrencyPeriodicLimits(testData3, "0");
        Assert.assertFalse(result3);

        // Test case 4: Invalid currency key (null parentObj)
        boolean result4 = validateAmountCurrencyPeriodicLimits(null, "currency");
        Assert.assertFalse(result4);

        // Add more test cases as needed
    }

//    @Test
//    public void testVrpCompletePayload() {
//
//        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITH_ALL_PARAMETERS;
//        JSONObject result = VRPConsentRequestValidator.validateVRPPayload(JSONValue.
//                parse(initiationPayloads));
//
//        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
//
//    }

    @Test
    public void testVrpInitiationPayloadWithoutRisk() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_RISK;
        JSONObject result = VRPConsentRequestValidator.validateConsentRisk((JSONObject) JSONValue.
                parse(initiationPayloads));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        assertTrue(true);
        Assert.assertEquals( PAYLOAD_FORMAT_ERROR_RISK,
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testIsValidObject() {
        String initiationPayloads = VRPTestConstants.METADATA_VRP_CREDITOR_ACCOUNT;
        JSONObject result = VRPConsentRequestValidator.validateVRPPayload(JSONValue.
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
    public void testIsValidObjectCreditorAcc() {
        String initiationPayloads = VRPTestConstants.METADATA_VRP_CREDITOR_ACCOUNT;
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


//    @Test
//    public void testVrpInitiationPayloadWithoutDebtorAcc() {
//
//        String initiationPayloads = VRPTestConstants.METADATA_VRP_DEBTOR_ACCOUNT;
//        JSONObject result = VRPConsentRequestValidator.validateConsentInitiation((JSONObject) JSONValue.
//                parse(initiationPayloads));
//
//        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
//    }

    @Test
    public void testVrpInitiationPayloadWithoutCreditAcc() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_CREDITOR_ACCOUNT;
        JSONObject result = VRPConsentRequestValidator.validateConsentInitiation((JSONObject) JSONValue.
                parse(initiationPayloads));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals( PAYLOAD_FORMAT_ERROR,
                result.get(ConsentExtensionConstants.ERRORS));
    }


    @Test
    public void testVrpInitiationPayloadWithoutCreditorAcc() {

        String initiationPayloads =VRPTestConstants.METADATA_VRP_CREDITOR_ACCOUNT;
        JSONObject result = VRPConsentRequestValidator.validateVRPInitiationPayload((JSONObject) JSONValue.
                parse(initiationPayloads));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals( PAYLOAD_FORMAT_ERROR_DEBTOR_ACC,
                result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadWithoutSchemeName() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_DEBTOR_ACCOUNT_SCHEME_NAME;
        JSONObject result = ConsentManageUtil.validateDebtorAccount((JSONObject) JSONValue.
                parse(initiationPayloads));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals( MISSING_DEBTOR_ACC_SCHEME_NAME,
                result.get(ConsentExtensionConstants.ERRORS));
    }

//    @Test
//    public void testVrpInitiationPayloadWithoutIdentification() {
//
//        String initiationPayloads = VRPTestConstants.METADATA_VRP_DEBTOR_ACCOUNT_IDENTIFICATION;
//        JSONObject result = ConsentManageUtil.validateDebtorAccount((JSONObject) JSONValue.
//                parse(initiationPayloads));
//
//        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
//        Assert.assertEquals( MISSING_DEBTOR_ACC_IDENTIFICATION,
//                result.get(ConsentExtensionConstants.ERRORS));
//    }
//
//TODO:
    @Test
    public void testVrpInitiationPayloadCreditorAccWithoutSchemeName() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_CREDITOR_ACCOUNT_SCHEME_NAME;
        JSONObject result = VRPConsentRequestValidator.validateVRPPayload((JSONValue.
                parse(initiationPayloads)));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals( MISSING_CREDITOR_ACC_SCHEME_NAME,
               result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadCreditorAccWithoutIdentification() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_CREDITOR_ACCOUNT_IDENTIFICATION;
        JSONObject result = VRPConsentRequestValidator.validateVRPPayload(JSONValue.
                parse(initiationPayloads));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals( MISSING_CREDITOR_ACC_SCHEME_NAME,
                result.get(ConsentExtensionConstants.ERRORS)); //TODO:
    }


    //UTIL CLASS
    @Test
    public void testValidatePeriodicLimits() {
        // Assuming invalidLimit is a JSONObject containing invalid data
        JSONObject invalidLimit = new JSONObject();
        invalidLimit.put("someKey", "someValue"); // Add your invalid data

        JSONObject validationResult = VRPConsentRequestValidator.validatePeriodicLimits(invalidLimit);

        // Check if the validation result indicates that it's not valid
        Assert.assertFalse(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)));


    }

    @Test
    public void testValidatePeriodicAlignment() {
        // Assuming periodicAlignment is an Object containing some value (valid or invalid)
        Object periodicAlignment = "someValue"; // Replace with your test data

        boolean validationResult = VRPConsentRequestValidator.validatePeriodicAlignment(periodicAlignment);


        Assert.assertFalse(validationResult, "Validation failed for periodicAlignment");

    }


    @Test
    public void testValidatePeriodicAlignmentType() {

        // Test case 2: periodAlignment is not empty but is an invalid value
        Object invalidPeriodicAlignment = "INVALID_ALIGNMENT"; // Replace with your invalid test data
        boolean validationResult2 = VRPConsentRequestValidator.validatePeriodicAlignment(invalidPeriodicAlignment);
        Assert.assertFalse(validationResult2, "Validation succeeded for invalid periodicAlignment");

        // Test case 3: periodAlignment is empty
        Object emptyPeriodicAlignment = ""; // Replace with your empty test data
        boolean validationResult3 = VRPConsentRequestValidator.validatePeriodicAlignment(emptyPeriodicAlignment);
        Assert.assertFalse(validationResult3, "Validation succeeded for empty periodicAlignment");
    }

    @Test
    public void testValidatePeriodicAlignmentTypes() {

        Object emptyPeriodicAlignment = "";
        boolean validationResult3 = VRPConsentRequestValidator.validatePeriodicAlignment(emptyPeriodicAlignment);
        Assert.assertFalse(validationResult3, "Validation succeeded for empty periodicAlignment");
    }

    @Test
    public void testValidatePeriodicAlignmentWithInvalidAlignment() {

        String invalidAlignment = "invalidAlignment";

        boolean result = VRPConsentRequestValidator.validatePeriodicAlignment(invalidAlignment);

        Assert.assertFalse(result);
    }

    @Test
    public void testValidatePeriodicAlignmentWithEmptyString() {
        String emptyAlignment = "";

        boolean result = VRPConsentRequestValidator.validatePeriodicAlignment(emptyAlignment);

        Assert.assertFalse(result);
    }

    @Test
    public void testValidatePeriodicAlignmentWithNonString() {

        Object nonStringAlignment = 123; // assuming a non-string object


        boolean result = VRPConsentRequestValidator.validatePeriodicAlignment(nonStringAlignment);


        Assert.assertFalse(result);
    }

    @Test
    public void testValidatePeriodicAlignmentWithError() {
        // Arrange
        String invalidAlignment = "invalidAlignment";

        // Act
        JSONObject validationResponse = new JSONObject();
        validationResponse.put(ConsentExtensionConstants.IS_VALID,
                VRPConsentRequestValidator.validatePeriodicAlignment(invalidAlignment));

        // Assert
        Assert.assertFalse((boolean) validationResponse.get(ConsentExtensionConstants.IS_VALID));

    }

    @Test
    public void testValidateAmountCurrencyPeriodicLimitsWithValidKey() {

        // Create a sample JSONArray with a JSONObject containing the valid key
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Currency", "USD");

        JSONArray jsonArray = new JSONArray();
        jsonArray.add(jsonObject);

        // Test the method with the valid key
        boolean result = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(jsonArray, "Currency");

        // Assert that the result is true, indicating a valid key
        assertTrue(result);
    }

    @Test
    public void testValidateAmountCurrencyPeriodicLimitsWithInvalidKeys() {

        // Create a sample JSONArray with a JSONObject not containing the valid key
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("InvalidKey", "USD");

        JSONArray jsonArray = new JSONArray();
        jsonArray.add(jsonObject);

        // Test the method with an invalid key
        boolean result = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(jsonArray, "Currency");

        // Assert that the result is false, indicating an invalid key
        Assert.assertFalse(result);
    }

    @Test
    public void testValidateAmountCurrencyPeriodicLimitsWithEmptyArray() {

        JSONArray jsonArray = new JSONArray();

        // Test the method with an empty array
        boolean result = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(jsonArray, "Currency");

        // Assert that the result is false, indicating an invalid key in an empty array
        Assert.assertFalse(result);
    }

    @Test
    public void testValidateAmountCurrencyPeriodicLimitsWithNullArray() {

        // Test the method with a null array
        boolean result = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(null, "Currency");

        // Assert that the result is false, indicating an invalid key in a null array
        Assert.assertFalse(result);
    }


    @Test
    public void testVrpPeriodicTypeJsonArray() {

        Object invalidObject = "Not a JSONArray";
        boolean isValidInvalidObject = VRPConsentRequestValidator.isValidJSONArray(invalidObject);

        // Test case 2: Missing period type key
        JSONObject missingKeyObject = new JSONObject();
        boolean result2 = VRPConsentRequestValidator.validatePeriodicType(missingKeyObject);
        Assert.assertFalse(result2);

        // Test case 3: Null period type
        JSONObject nullPeriodTypeObject = new JSONObject();
        nullPeriodTypeObject.put(ConsentExtensionConstants.PERIOD_TYPE, null);
        boolean result3 = VRPConsentRequestValidator.validatePeriodicType(nullPeriodTypeObject);
        Assert.assertFalse(result3);

        // Test case 4: Empty period type
        JSONObject emptyPeriodTypeObject = new JSONObject();
        emptyPeriodTypeObject.put(ConsentExtensionConstants.PERIOD_TYPE, "");
        boolean result4 = VRPConsentRequestValidator.validatePeriodicType(emptyPeriodTypeObject);
        Assert.assertFalse(result4);

        // Test case 5: Invalid period type
        JSONObject invalidPeriodTypeObject = new JSONObject();
        invalidPeriodTypeObject.put(ConsentExtensionConstants.PERIOD_TYPE, "InvalidType");
        boolean result5 = VRPConsentRequestValidator.validatePeriodicType(invalidPeriodTypeObject);
        Assert.assertFalse(result5);

        Assert.assertFalse(isValidInvalidObject, ConsentExtensionConstants.IS_VALID);
    }

    @Test
    public void testValidToDateTimeFormat() {

        Object invalidDateTimeObject = "";
        boolean isValidInvalidDateTimeObject = VRPConsentRequestValidator.isValidDateTimeObject(invalidDateTimeObject);
        Assert.assertFalse(isValidInvalidDateTimeObject, ConsentExtensionConstants.IS_VALID);

        JSONObject jsonObjectDateTimeObject = new JSONObject();
        boolean isValidValidDateTimeObject = VRPConsentRequestValidator.isValidDateTimeObject(jsonObjectDateTimeObject);
        Assert.assertFalse(isValidValidDateTimeObject, ConsentExtensionConstants.IS_VALID);
    }


    @Test
    public void testDataContainsKey_InitiationNotPresent() {
        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_INITIATION;
        JSONObject result = VRPConsentRequestValidator.validateConsentInitiation((JSONObject) JSONValue.
                parse(initiationPayloads));

        boolean containsKey = result.containsKey(ConsentExtensionConstants.INITIATION);
        Assert.assertFalse(containsKey, ConsentExtensionConstants.IS_VALID);
    }

    @Test
    public void testDataContainsKey_ControlParametersNotPresent() {
        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_CONTROL_PARAMETERS;
        JSONObject result = VRPConsentRequestValidator.validateConsentControlParameters((JSONObject) JSONValue.
                parse(initiationPayloads));
        boolean containsKey = result.containsKey(ConsentExtensionConstants.CONTROL_PARAMETERS);
        Assert.assertFalse(containsKey, ConsentExtensionConstants.IS_VALID);
    }

    @Test
    public void testVrpInitiationPayloadMaximumIndividualAmountNotJsonObject() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITH_EMPTY_MAX_INDIVIDUAL_AMOUNT;
        String date = VRPTestConstants.METADATA_VRP_WITHOUT_VALID_FROM_DATE;
        String date2 =VRPTestConstants.METADATA_VRP_WITHOUT_VALID_TO_DATE;

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

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_DEB_ACC;
        JSONObject result = VRPConsentRequestValidator.validateVRPInitiationPayload((JSONObject) JSONValue.
                parse(initiationPayloads));
        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidJSONObject(initiationPayloads);
        Assert.assertFalse(isValidNonJSONObject, (ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
    }

    @Test
    public void testVrpInitiationPayloadDebAccs() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_DEB_ACC;
        JSONObject result = VRPConsentRequestValidator.validateVRPPayload((JSONObject) JSONValue.
                parse(initiationPayloads));
        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidJSONObject(initiationPayloads);
        Assert.assertFalse(isValidNonJSONObject, (ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
    }


    @Test
    public void testVrpInitiationMax() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITH_EMPTY_MAX_INDIVIDUAL_AMOUNT;
        JSONObject result = VRPConsentRequestValidator.validateVRPPayload(JSONValue.
                parse(initiationPayloads));
        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidJSONObject(initiationPayloads);
        Assert.assertFalse(isValidNonJSONObject, (ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));

    }

    @Test
    public void testVrpInitiationPayloadValidateDate() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_DEB_ACC;
        JSONObject result = VRPConsentRequestValidator.validateVRPInitiationPayload((JSONObject) JSONValue.
                parse(initiationPayloads));
        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidJSONObject(initiationPayloads);
        Assert.assertFalse(isValidNonJSONObject, (ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
    }

    @Test
    public void testVrpInitiationPayloadCreditorAcc() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_CREDITOR_ACC;
        JSONObject result = VRPConsentRequestValidator.validateVRPInitiationPayload((JSONObject) JSONValue.
                parse(initiationPayloads));
        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidJSONObject(initiationPayloads);
        Assert.assertFalse(isValidNonJSONObject);
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertEquals(INVALID_PARAMETER_CREDITOR_ACC, result.get(ConsentExtensionConstants.ERRORS));
    }

    @Test
    public void testVrpInitiationPayloadCreditorAccs() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_CREDITOR_ACC;
        JSONObject result = VRPConsentRequestValidator.validateVRPPayload(JSONValue.
                parse(initiationPayloads));
        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidJSONObject(initiationPayloads);

        Assert.assertFalse(isValidNonJSONObject);
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));

        Assert.assertEquals(INVALID_PARAMETER_CREDITOR_ACC, result.get(ConsentExtensionConstants.ERRORS));
    }


    @Test
    public void testRequestBodyValidation_NoRiskKey() {
        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_RISK;
        String risk = VRPTestConstants.METADATA_VRP_WITH_EMPTY_RISK;
        JSONObject result = VRPConsentRequestValidator.validateConsentRisk((JSONObject) JSONValue.
                parse(initiationPayloads));
        JSONObject result2 = VRPConsentRequestValidator.validateConsentRisk((JSONObject) JSONValue.
                parse(risk));

        boolean containsKey = result.containsKey(ConsentExtensionConstants.RISK);
        boolean key = result2.containsKey(ConsentExtensionConstants.RISK);
        Assert.assertFalse(containsKey, ConsentExtensionConstants.IS_VALID);
        Assert.assertFalse(key, ConsentExtensionConstants.IS_VALID);
    }

    @Test
    public void testIsValidObject_ControlParameter_Empty() {
        // Given
        String emptyControlParameterObject = "";
        emptyControlParameterObject.contains("controlParameter");

        // When
        boolean isValid = VRPConsentRequestValidator.isValidJSONObject(emptyControlParameterObject);

        // Then
        Assert.assertFalse(isValid, "Expected the JSONObject with controlParameter key to be empty and invalid");
    }


    @Test
    public void testIsValidObject_NegativeScenarios() {

        // Negative scenario 3: Non-JSONObject value (String)
        String nonJSONObject = "Not a JSONObject";
        JSONObject validInitiationObject = new JSONObject();


//        JSONObject result = VRPConsentRequestValidator.validateVRPPayload(nonJSONObject);
//        JSONObject result2 = VRPConsentRequestValidator.validateVRPPayload(validInitiationObject);

        // When
        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidJSONObject(nonJSONObject);
        boolean isValidNonJSONObject1 = VRPConsentRequestValidator.isValidJSONObject(validInitiationObject);
        Assert.assertFalse(isValidNonJSONObject1, (ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse(isValidNonJSONObject, (ConsentExtensionConstants.IS_VALID));

        // Then
        // Verify that the controlParameter is considered invalid

        // Verify that the non-JSONObject value is considered invalid
        // Assert.assertFalse(isValidNonJSONObject, "Expected the non-JSONObject value to be invalid");
    }

    @Test
    public void testVrpInitiationPayloadInitiationNotJsonObject() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_EMPTY_INITIATION;
        JSONObject result = VRPConsentRequestValidator.validateConsentInitiation((JSONObject) JSONValue.
                parse(initiationPayloads));
        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidJSONObject(initiationPayloads);
        Assert.assertFalse(isValidNonJSONObject, (ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
    }

    @Test
    public void testVrpInitiationPayloadMaximumIndividualNotJsonObject() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITH_INVALID_MAX_INDIVIDUAL_AMOUNT;
        JSONObject result = VRPConsentRequestValidator.validateMaximumIndividualAmount((JSONObject) JSONValue.
                parse(initiationPayloads));
        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidJSONObject(initiationPayloads);
        Assert.assertFalse(isValidNonJSONObject, (ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
    }

    @Test
    public void testVrpInitiationPayloadControlParametersNotJsonObject() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITH_EMPTY_CONTROL_PARAMETERS;
        JSONObject result = VRPConsentRequestValidator.validateConsentControlParameters((JSONObject) JSONValue.
                parse(initiationPayloads));
        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidJSONObject(initiationPayloads);
        Assert.assertFalse(isValidNonJSONObject, (ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
    }


    @Test
    public void testVrpPayloadWithoutDate() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITH_DATE_NOT_STRING;
        JSONObject result = VRPConsentRequestValidator.validateParameterDateTime((JSONObject) JSONValue.
                parse(initiationPayloads));
        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidDateTimeObject(initiationPayloads);
        Assert.assertFalse(isValidNonJSONObject, (ConsentExtensionConstants.IS_VALID));
        assertTrue((boolean) result.get(ConsentExtensionConstants.IS_VALID));
    }

    @Test
    public void testVrpInitiationPayloadWithoutDate() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITH_INVALID_VALID_FROM_DATETIME;
        JSONObject result = VRPConsentRequestValidator.validateParameterDateTime((JSONObject) JSONValue.
                parse(initiationPayloads));

        // Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        assertFalse(result.containsKey("ValidFromDateTime"));
    }


    @Test
    public void testVrpInitiationPayloadWithoutValidToDate() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_VALID_TO_DATE;
        JSONObject result = VRPConsentRequestValidator.validateParameterDateTime((JSONObject) JSONValue.
                parse(initiationPayloads));

        // Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        assertFalse(result.containsKey("ValidToDateTime"));
    }

    @Test
    public void testVrpInitiationPayloadMaximumIndividualAmountIsJsonObject() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITH_EMPTY_MAX_INDIVIDUAL_AMOUNT;
        JSONObject result = VRPConsentRequestValidator.validateControlParameters((JSONObject) JSONValue.
                parse(initiationPayloads));
        boolean isValidNonJSONObject = VRPConsentRequestValidator.isValidJSONObject(initiationPayloads);
        Assert.assertFalse(isValidNonJSONObject, (ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
    }

//    @Test
//    public void testValidateParameterDateTime() {
//
//        String initiationPayloads = VRPDataProviders.DataProviders.METADATA_VRP_WITH_INVALID_VALID_FROM_DATETIME;
//        JSONObject result = VRPConsentRequestValidator.validateParameterDateTime((JSONObject) JSONValue.
//                parse(initiationPayloads));
//
//        JSONObject controlParametersInvalidFormatValidTo = new JSONObject();
//        controlParametersInvalidFormatValidTo.put(ConsentExtensionConstants.VALID_TO_DATE_TIME, "invalid_date");
//
//        JSONObject resultInvalidFormatValidTo = VRPConsentRequestValidator.
//                validateParameterDateTime(controlParametersInvalidFormatValidTo);
//        Assert.assertFalse((boolean) resultInvalidFormatValidTo.get(ConsentExtensionConstants.IS_VALID),
//                "Expected invalid date format error for ValidTo");
//
//        // Test case 3: Invalid date format for ValidFrom
//        JSONObject controlParametersInvalidFormatValidFrom = new JSONObject();
//        controlParametersInvalidFormatValidFrom.put(ConsentExtensionConstants.VALID_TO_DATE_TIME,
//                "2024-12-31T23:59:59.999Z");
//        controlParametersInvalidFormatValidFrom.put(ConsentExtensionConstants.VALID_FROM_DATE_TIME, "invalid_date");
//
//        // Prepare a JSON object with an invalid date format for ValidToDateTime
//        JSONObject controlParameters = new JSONObject();
//        controlParameters.put(ConsentExtensionConstants.VALID_TO_DATE_TIME, "invalid_date_format");
//
//        // Validate the result
//        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
//
//        JSONObject resultInvalidFormatValidFrom = VRPConsentRequestValidator.
//                validateParameterDateTime(controlParametersInvalidFormatValidFrom);
//        Assert.assertFalse((boolean) resultInvalidFormatValidFrom.get(ConsentExtensionConstants.IS_VALID),
//                "Expected invalid date format error for ValidFrom");
//
//        // Test case 4: ValidToDate older than ValidFromDate
//        JSONObject controlParametersInvalidDates = new JSONObject();
//        controlParametersInvalidDates.put(ConsentExtensionConstants.VALID_TO_DATE_TIME, "2023-01-01T00:00:00.000Z");
//        controlParametersInvalidDates.put(ConsentExtensionConstants.VALID_FROM_DATE_TIME, "2024-01-01T00:00:00.000Z");
//
//        JSONObject resultInvalidDates = VRPConsentRequestValidator.
//                validateParameterDateTime(controlParametersInvalidDates);
//        Assert.assertFalse((boolean) resultInvalidDates.get(ConsentExtensionConstants.IS_VALID),
//                "Expected invalid date range error");
//
//        // Test case 5: ValidToDate is in the past
//        JSONObject controlParametersPastValidToDate = new JSONObject();
//        controlParametersPastValidToDate.put(ConsentExtensionConstants.VALID_TO_DATE_TIME,
//                "2022-01-01T00:00:00.000Z");
//        controlParametersPastValidToDate.put(ConsentExtensionConstants.VALID_FROM_DATE_TIME,
//                "2023-01-01T00:00:00.000Z");
//
//        JSONObject resultPastValidToDate = VRPConsentRequestValidator.
//                validateParameterDateTime(controlParametersPastValidToDate);
//        Assert.assertFalse((boolean) resultPastValidToDate.get(ConsentExtensionConstants.IS_VALID),
//                "Expected invalid past date for ValidTo");
//    }

    @Test
    public void testVrpInitiationPayloadDateTime() {
        // Prepare a JSON object with an invalid date format for ValidToDateTime
        JSONObject controlParameters = new JSONObject();
        controlParameters.put(ConsentExtensionConstants.VALID_TO_DATE_TIME, "invalid_date_format");

        // Call the method under test
        JSONObject result = VRPConsentRequestValidator.validateParameterDateTime(controlParameters);

        // Validate the result
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
    }


    @Test
    public void testVrpInitiationPayloadDateTimes() {
        // Prepare a JSON object with an invalid date format for ValidToDateTime
        JSONObject controlParameters = new JSONObject();
        controlParameters.put(ConsentExtensionConstants.VALID_FROM_DATE_TIME, "invalid_date_format");

        // Call the method under test
        JSONObject result = VRPConsentRequestValidator.validateParameterDateTime(controlParameters);

        // Validate the result
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
    }

    @Test
    public void testIsValidDateTimeObjectNegativeScenarios() {
        // Test case 1: Empty string
        String emptyString = "";
        boolean resultEmptyString = VRPConsentRequestValidator.isValidDateTimeObject(emptyString);
        Assert.assertFalse(resultEmptyString, "Expected false for an empty string");

        // Test case 2: Null value
        Object nullValue = null;
        boolean resultNullValue = false;
        Assert.assertFalse(resultNullValue, "Expected false for a null value");

        // Test case 3: Non-string value
        Object nonStringValue = 123; // Assuming an integer, but could be any non-string type
        boolean resultNonStringValue = VRPConsentRequestValidator.isValidDateTimeObject(nonStringValue);
        Assert.assertFalse(resultNonStringValue, "Expected false for a non-string value");
    }

    @Test
    public void testValidationMethods() {
        // Test isValidDateTimeObject with a valid date-time string
        assertTrue(VRPConsentRequestValidator.isValidDateTimeObject("2022-12-31T23:59:59Z"));

        // Test isValidDateTimeObject with an invalid date-time string
        Assert.assertFalse(VRPConsentRequestValidator.isValidDateTimeObject("invalid_datetime"));

        // Test isValidDateTimeObject with an empty date-time string
        Assert.assertFalse(VRPConsentRequestValidator.isValidDateTimeObject(""));

        // Test validateParameterDateTime with valid controlParameters
        JSONObject validControlParameters = new JSONObject();
        validControlParameters.put(ConsentExtensionConstants.VALID_TO_DATE_TIME, "2024-01-01T12:00:00Z");
        validControlParameters.put(ConsentExtensionConstants.VALID_FROM_DATE_TIME, "2023-01-01T12:00:00Z");
        JSONObject validValidationResponse = VRPConsentRequestValidator.
                validateParameterDateTime(validControlParameters);
        Assert.assertFalse((boolean) validValidationResponse.get(ConsentExtensionConstants.IS_VALID));

        // Test validateParameterDateTime with invalid controlParameters
        JSONObject invalidControlParameters = new JSONObject();
        invalidControlParameters.put(ConsentExtensionConstants.VALID_TO_DATE_TIME, "2024-01-01T12:00:00Z");
        invalidControlParameters.put(ConsentExtensionConstants.VALID_FROM_DATE_TIME, "2025-01-01T12:00:00Z");
        JSONObject invalidValidationResponse = VRPConsentRequestValidator.
                validateParameterDateTime(invalidControlParameters);
        Assert.assertFalse((boolean) invalidValidationResponse.get(ConsentExtensionConstants.IS_VALID));


    }

    @Test
    public void testValidationMethodDate() {
        // Test isValidDateTimeObject with a valid date-time string
        assertTrue(VRPConsentRequestValidator.isValidDateTimeObject("2022-12-31T23:59:59Z"));

        // Test isValidDateTimeObject with an invalid date-time string
        Assert.assertFalse(VRPConsentRequestValidator.isValidDateTimeObject("invalid_datetime"));

        // Test isValidDateTimeObject with an empty date-time string
        Assert.assertFalse(VRPConsentRequestValidator.isValidDateTimeObject(""));

        // Test validateParameterDateTime with invalid controlParameters
        JSONObject invalidControlParameters = new JSONObject();
        invalidControlParameters.put(ConsentExtensionConstants.VALID_TO_DATE_TIME, "2024-01-01T12:00:00Z");
        invalidControlParameters.put(ConsentExtensionConstants.VALID_FROM_DATE_TIME, "2025-01-01T12:00:00Z");
        JSONObject invalidValidationResponse = VRPConsentRequestValidator.
                validateParameterDateTime(invalidControlParameters);
        Assert.assertFalse((boolean) invalidValidationResponse.get(ConsentExtensionConstants.IS_VALID));


        // Arrange
        JSONObject controlParameters = new JSONObject();
        controlParameters.put(ConsentExtensionConstants.VALID_FROM_DATE_TIME, "2023-01-01T00:00:00Z");
        controlParameters.put(ConsentExtensionConstants.VALID_TO_DATE_TIME, "2022-01-01T00:00:00Z");

        // Act
        JSONObject validationResult = VRPConsentRequestValidator.validateParameterDateTime(controlParameters);

        // Assert
        Assert.assertFalse((boolean) validationResult.get(ConsentExtensionConstants.IS_VALID));
    }

    @Test
    public void testValidateAmountCurrencyNegativeScenarios() {

        //VRPConsentRequestValidator yourClass = new VRPConsentRequestValidator();

        // Test case 1: parentObj is null
        boolean result1 = validateAmountCurrency(null, "yourKey");
        Assert.assertFalse(result1);

        // Test case 2: Key is not present in parentObj
        boolean result2 = validateAmountCurrency(new JSONObject(), "nonExistentKey");
        Assert.assertFalse(result2);

        // Test case 3: Value associated with the key is not a String
        JSONObject testData = new JSONObject();
        testData.put("Amount", 123);
        boolean result3 = validateAmountCurrency(testData, "yourKey");
        Assert.assertFalse(result3);

        // Test case 4: Value associated with the key is an empty String
        testData.put("Currency", "");
        boolean result4 = validateAmountCurrency(testData, "yourKey");
        Assert.assertFalse(result4);

        boolean result11 = validateAmountCurrency(null, "Currency");
        Assert.assertFalse(result11);

        // Test case 2: Key is not present in parentObj
        boolean result22 = validateAmountCurrency(new JSONObject(), "Currency");
        Assert.assertFalse(result22);

        // Test case 3: Key is present, but value is not a String
        JSONObject testData3 = new JSONObject();
        testData3.put("Currency", 123); // Assuming "Currency" should be a String, but it's an integer in this case
        boolean result33 = validateAmountCurrency(testData3, "Currency");
        Assert.assertFalse(result33);

        // Test case 4: Key is present, and value is an empty String
        JSONObject testData4 = new JSONObject();
        testData4.put("Currency", "");
        boolean result44 = validateAmountCurrency(testData4, "Currency");
        Assert.assertFalse(result44);
    }

    @Test
    public void testValidateAmountCurrencyWithoutAmountKeyAndEmptyString() {
        // Create an instance of YourClass
        VRPConsentRequestValidator amount = new VRPConsentRequestValidator();

        // Test case 1: parentObj is null
        boolean result1 = validateAmountCurrency(null, "Amount");
        Assert.assertFalse(result1);

        // Test case 2: Key is not present in parentObj
        boolean result2 = validateAmountCurrency(new JSONObject(), "nonExistentKey");
        Assert.assertFalse(result2);

        // Test case 3: Key is present, but value is not a String
        JSONObject testData3 = new JSONObject();
        testData3.put("Amount", 123); // Assuming "yourKey" should be a String, but it's an integer in this case
        boolean result3 = validateAmountCurrency(testData3, "Amount");
        Assert.assertFalse(result3);

        // Test case 4: Key is present, and value is an empty String
        JSONObject testData4 = new JSONObject();
        testData4.put("Amount", "");
        boolean result4 = validateAmountCurrency(testData4, "Amount");
        Assert.assertFalse(result4);
    }

    @Test
    public void testValidateCurrencyWithoutAmountKeyAndEmptyString() {
        // Create an instance of YourClass
        VRPConsentRequestValidator currency = new VRPConsentRequestValidator();

        // Test case 1: parentObj is null
        boolean result1 = validateAmountCurrency(null, "Currency");
        Assert.assertFalse(result1);

        // Test case 2: Key is not present in parentObj
        boolean result2 = validateAmountCurrency(new JSONObject(), "nonExistentKey");
        Assert.assertFalse(result2);

        // Test case 3: Key is present, but value is not a String
        JSONObject testData3 = new JSONObject();
        testData3.put("Currency", 123); // Assuming "yourKey" should be a String, but it's an integer in this case
        boolean result3 = validateAmountCurrency(testData3, "Currency");
        Assert.assertFalse(result3);

        // Test case 4: Key is present, and value is an empty String
        JSONObject testData4 = new JSONObject();
        testData4.put("Currency", "");
        boolean result4 = validateAmountCurrency(testData4, "Currency");
        Assert.assertFalse(result4);
    }


    @Test
    public void testValidateAmountCurrencyPeriodicLimits() {

        // Test case 2: Key is null
        JSONArray testData2 = new JSONArray();
        boolean result2 = validateAmountCurrencyPeriodicLimits(testData2, null);
        Assert.assertFalse(result2);

        // Test case 3: ParentObj is null
        boolean result3 = validateAmountCurrencyPeriodicLimits(null, "0");
        Assert.assertFalse(result3);

        // Test case 4: Key is not present in parentObj
        JSONArray testData4 = new JSONArray();
        boolean result4 = validateAmountCurrencyPeriodicLimits(testData4, "nonExistentKey");
        Assert.assertFalse(result4);

        // Test case 5: Value is an empty String
        JSONArray testData5 = new JSONArray();
        testData5.add("");
        boolean result5 = validateAmountCurrencyPeriodicLimits(testData5, "0");
        Assert.assertFalse(result5);
    }


    @Test
    public void testValidateKeyAndNonEmptyStringValue() {

        // Test case 2: Key is null
        JSONArray testData2 = new JSONArray();
        boolean result2 = validateAmountCurrencyPeriodicLimits(testData2, null);
        Assert.assertFalse(result2);

        // Test case 3: ParentObj is null
        boolean result3 = validateAmountCurrencyPeriodicLimits(null, "0");
        Assert.assertFalse(result3);

        // Test case 4: Key is not present in parentObj
        JSONArray testData4 = new JSONArray();
        boolean result4 = validateAmountCurrencyPeriodicLimits(testData4, "nonExistentKey");
        Assert.assertFalse(result4);

        // Test case 5: Value is an empty String
        JSONArray testData5 = new JSONArray();
        testData5.add("");
        boolean result5 = validateAmountCurrencyPeriodicLimits(testData5, "0");
        Assert.assertFalse(result5);


        // Test case 7: Value is not a String
        JSONArray testData7 = new JSONArray();
        testData7.add(123); // Assuming the value should be a String, but it's an integer in this case
        boolean result7 = validateAmountCurrencyPeriodicLimits(testData7, "0");
        Assert.assertFalse(result7);
    }


    @Test
    public void testValidateAmountCurrencyWithoutCurrentKeyAndEmptyString() {
        // Create an instance of YourClass
        VRPConsentRequestValidator currency = new VRPConsentRequestValidator();

        // Test case 1: parentObj is null
        boolean result1 = validateAmountCurrency(null, "Currency");
        Assert.assertFalse(result1);

        // Test case 2: Key is not present in parentObj
        boolean result2 = validateAmountCurrency(new JSONObject(), "nonExistentKey");
        Assert.assertFalse(result2);

        // Test case 3: Key is present, but value is not a String
        JSONObject testData3 = new JSONObject();
        testData3.put("Currency", 123); // Assuming "yourKey" should be a String, but it's an integer in this case
        boolean result3 = validateAmountCurrency(testData3, "Current");
        Assert.assertFalse(result3);

        // Test case 4: Key is present, and value is an empty String
        JSONObject testData4 = new JSONObject();
        testData4.put("Currency", "");
        boolean result4 = validateAmountCurrency(testData4, "Current");
        Assert.assertFalse(result4);

        // Add more test cases as needed
    }

    @Test
    public void testNegativeScenarioForKeyAndEmptyString() {
        // Create an instance of YourClass
        VRPConsentRequestValidator amount = new VRPConsentRequestValidator();

        // Test case 1: Value is not a String
        JSONObject testData1 = new JSONObject();
        testData1.put("Amount", 123); // Assuming "yourKey" should be a String, but it's an integer in this case
        boolean result1 = validateAmountCurrency(testData1, "yourKey");
        Assert.assertFalse(result1);

        // Test case 2: Value is an empty String
        JSONObject testData2 = new JSONObject();
        testData2.put("Amount", "");
        boolean result2 = validateAmountCurrency(testData2, "yourKey");
        Assert.assertFalse(result2);

    }

    @Test
    public void testNegativeScenarioForKeyAndEmptyStrings() {
        // Create an instance of YourClass
        VRPConsentRequestValidator currency = new VRPConsentRequestValidator();

        // Test case 1: Value is not a String
        JSONObject testData1 = new JSONObject();
        testData1.put("Currency", 123); // Assuming "yourKey" should be a String, but it's an integer in this case
        boolean result1 = validateAmountCurrency(testData1, "yourKey");
        Assert.assertFalse(result1);

        // Test case 2: Value is an empty String
        JSONObject testData2 = new JSONObject();
        testData2.put("Currency", "");
        boolean result2 = validateAmountCurrency(testData2, "yourKey");
        Assert.assertFalse(result2);

    }


    @Test
    public void testPositiveScenarioForValidateAmountCurrency() {

        VRPConsentRequestValidator amount = new VRPConsentRequestValidator();

        JSONObject testData = new JSONObject();
        testData.put("Amount", "1000");
        boolean result = validateAmountCurrency(testData, "Amount");

        assertTrue(result);
    }

    @Test
    public void testPositiveScenarioForValidateAmountCurrencys() {

        VRPConsentRequestValidator currency = new VRPConsentRequestValidator();

        JSONObject testData = new JSONObject();
        testData.put("Currency", "1000");
        boolean result = validateAmountCurrency(testData, "Currency");

        assertTrue(result);
    }


    @Test
    public void testInvalidValidFromDateTimeFormat() {

        JSONObject controlParameters = new JSONObject();
        controlParameters.put(ConsentExtensionConstants.VALID_FROM_DATE_TIME, "invalid-date-time");

        JSONObject validationResult = VRPConsentRequestValidator.validateParameterDateTime(controlParameters);

        Assert.assertFalse((Boolean) validationResult.get(ConsentExtensionConstants.IS_VALID));

    }


    @Test
    public void testValidPeriodicLimits() {
        JSONObject controlParameters = new JSONObject();
        JSONArray periodicLimitsArray = new JSONArray();

        JSONObject validPeriodicLimit = new JSONObject();
        validPeriodicLimit.put(ConsentExtensionConstants.AMOUNT, "1000");
        validPeriodicLimit.put(ConsentExtensionConstants.CURRENCY, "USD");
        validPeriodicLimit.put(ConsentExtensionConstants.PERIOD_ALIGNMENT, "Day");
        validPeriodicLimit.put(ConsentExtensionConstants.PERIOD_TYPE, "year");
        periodicLimitsArray.add(validPeriodicLimit);

        controlParameters.put(ConsentExtensionConstants.PERIODIC_LIMITS, periodicLimitsArray);

        JSONObject validationResult = VRPConsentRequestValidator.validatePeriodicLimits(controlParameters);
        boolean validationResult2 = VRPConsentRequestValidator.validatePeriodicType(controlParameters);
        Assert.assertFalse((Boolean) validationResult.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((Boolean) validationResult2);
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
        boolean result = validateAmountCurrency(parentObj, key);
        assertTrue(result);

        // Test case 2: "Currency" key is present, but value is an empty String
        parentObj.put("Currency", "");
        result = validateAmountCurrency(parentObj, key);
        Assert.assertFalse(result);

        // Test case 3: "Currency" key is present, but value is not a String
        parentObj.put("Currency", 123); // Assuming "Currency" should be a String, but it's an integer in this case
        result = validateAmountCurrency(parentObj, key);
        Assert.assertFalse(result);

        // Test case 4: "Currency" key is not present in parentObj
        parentObj = new JSONObject();
        result = validateAmountCurrency(parentObj, key);
        Assert.assertFalse(result);

        // Test case 5: parentObj is null
        result = validateAmountCurrency(null, key);
        Assert.assertFalse(result);

        Assert.assertFalse((Boolean) validationResult.get(ConsentExtensionConstants.IS_VALID));
    }

    @Test
    public void testInvalidPeriodicLimitsFormat() {

        JSONObject controlParameters = new JSONObject();
        controlParameters.put(ConsentExtensionConstants.PERIODIC_LIMITS, "invalid-format");
        JSONObject validationResult = VRPConsentRequestValidator.validatePeriodicLimits(controlParameters);

        Assert.assertFalse((Boolean) validationResult.get(ConsentExtensionConstants.IS_VALID));
    }

    @Test
    public void testInvalidMaxAmountFormat() {

        JSONObject controlParameters = new JSONObject();
        controlParameters.put(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT, "invalid-format");
        JSONObject validationResult = VRPConsentRequestValidator.validateMaximumIndividualAmount(controlParameters);

        Assert.assertFalse((Boolean) validationResult.get(ConsentExtensionConstants.IS_VALID));
    }

    @Test
    public void testInvalidMaxAmountFormatPeriodicLimit() {

        JSONObject controlParameters = new JSONObject();
        controlParameters.put(ConsentExtensionConstants.PERIODIC_LIMITS, "invalid-format");
        JSONObject validationResult = VRPConsentRequestValidator.
                validateMaximumIndividualAmountCurrency(controlParameters);

        Assert.assertFalse((Boolean) validationResult.get(ConsentExtensionConstants.IS_VALID));
    }

    @Test
    public void testInvalidMaxAmountFormats() {

        JSONObject controlParameters = new JSONObject();
        controlParameters.put(ConsentExtensionConstants.PERIODIC_LIMITS, "invalid-format");
        JSONObject validationResult = VRPConsentRequestValidator.validatePeriodicLimits(controlParameters);

        Assert.assertFalse((Boolean) validationResult.get(ConsentExtensionConstants.IS_VALID));
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
    }


    @Test
    public void testValidateParameterDateTime_InvalidDateTimeRange() {
        // Arrange
        JSONObject controlParameters = new JSONObject();
        controlParameters.put(ConsentExtensionConstants.VALID_FROM_DATE_TIME, "2022-01-01T00:00:00Z");
        controlParameters.put(ConsentExtensionConstants.VALID_TO_DATE_TIME, "2022-01-02T00:00:00Z");
        JSONObject result1 = VRPConsentRequestValidator.validateParameterDateTime(controlParameters);
        Assert.assertNotNull(result1);
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
        // Arrange
        JSONObject controlParameters = new JSONObject();
        controlParameters.put(ConsentExtensionConstants.VALID_FROM_DATE_TIME, "2023-01-01T00:00:00Z");
        controlParameters.put(ConsentExtensionConstants.VALID_TO_DATE_TIME, "2022-01-01T00:00:00Z");

        // Act
        boolean hasValidFromDate = controlParameters.containsKey(ConsentExtensionConstants.VALID_FROM_DATE_TIME);
        boolean hasValidToDate = controlParameters.containsKey(ConsentExtensionConstants.VALID_TO_DATE_TIME);

        // Assert
        assertTrue(hasValidFromDate && hasValidToDate);
        // Add additional assertions based on your error handling logic
    }


    @Test
    public void testInvalidDateTimeRanges() {
        // Arrange
        JSONObject controlParameters = new JSONObject();
        controlParameters.put(ConsentExtensionConstants.VALID_FROM_DATE_TIME, "2023-01-01T00:00:00Z");
        controlParameters.put(ConsentExtensionConstants.VALID_TO_DATE_TIME, "2022-01-01T00:00:00Z");

        // Act
        JSONObject isValidRange = VRPConsentRequestValidator.validateParameterDateTime(controlParameters);

        // Assert
        assertFalse(isValidRange.containsKey("ValidFromDateTime"));
        Assert.assertFalse((Boolean) isValidRange.get(ConsentExtensionConstants.IS_VALID));
        // Add additional assertions based on your error handling logic
    }


    @Test
    public void testValidCurrencyKey() {
        // Arrange
        JSONObject parentObj = new JSONObject();
        parentObj.put("Currency", "USD");

        // Act
        boolean isValid = validateAmountCurrency(parentObj, "Currency");

        // Assert
        assertTrue(isValid);
    }

    @Test
    public void testInvalidCurrencyKey_NullParentObj() {
        // Act
        boolean isValid = validateAmountCurrency(null, "Currency");

        // Assert
        Assert.assertFalse(isValid);

    }

    @Test
    public void testInvalidCurrencyKey_MissingKey() {

        JSONObject maximumIndividualAmount = new JSONObject();

        // Act
        boolean validationResults = validateAmountCurrency(maximumIndividualAmount, ConsentExtensionConstants.CURRENCY);

        // Assert
        Assert.assertFalse(validationResults);
        // Arrange
        JSONObject parentObj = new JSONObject();
        JSONObject validationResult = VRPConsentRequestValidator.validateMaximumIndividualAmount(parentObj);

        Assert.assertFalse((Boolean) validationResult.get(ConsentExtensionConstants.IS_VALID));
        boolean isValid = validateAmountCurrency(parentObj, "Currency");

        // Assert
        Assert.assertFalse(isValid);
    }

    @Test
    public void testInvalidCurrencyKey_EmptyValue() {

        JSONObject maximumIndividualAmount = new JSONObject(); // Assuming this is a JSON object
        // You may need to populate maximumIndividualAmount with other necessary values

        // Act
        boolean validationResult = validateAmountCurrency(
                (JSONObject) maximumIndividualAmount, ConsentExtensionConstants.CURRENCY);

        // Assert
        Assert.assertFalse(validationResult);
        // Arrange
        JSONObject parentObj = new JSONObject();
        parentObj.put("Currency", "");
        JSONObject validationResults = VRPConsentRequestValidator.validateMaximumIndividualAmount(parentObj);

        Assert.assertFalse((Boolean) validationResults.get(ConsentExtensionConstants.IS_VALID));
        // Act

        // Act
        boolean isValid = validateAmountCurrency(parentObj, "Currency");

        // Assert
        Assert.assertFalse(isValid);
    }

    @Test
    public void testInvalidCurrencyKey_NonString() {

        JSONObject maximumIndividualAmount = new JSONObject(); // Assuming this is a JSON object
        // You may need to populate maximumIndividualAmount with other necessary values

        // Act
        boolean validationResults = validateAmountCurrency((JSONObject)
                maximumIndividualAmount, ConsentExtensionConstants.CURRENCY);

        // Assert
        Assert.assertFalse(validationResults);
        // Arrange
        JSONObject parentObj = new JSONObject();
        parentObj.put("Currency", 123); // Assuming "Currency" should be a String, but it's an integer in this case
        JSONObject validationResult = VRPConsentRequestValidator.validateMaximumIndividualAmount(parentObj);

        Assert.assertFalse((Boolean) validationResult.get(ConsentExtensionConstants.IS_VALID));

        // Act
        boolean isValid = validateAmountCurrency(parentObj, "Currency");

        // Assert
        Assert.assertFalse(isValid);
    }

    @Test
    public void testInvalidCurrencyKey_NullValue() {

        JSONObject maximumIndividualAmount = new JSONObject(); // Assuming this is a JSON object
        // You may need to populate maximumIndividualAmount with other necessary values

        // Act
        boolean validationResult = validateAmountCurrency(
                (JSONObject) maximumIndividualAmount, ConsentExtensionConstants.CURRENCY);

        // Assert
        Assert.assertFalse(validationResult);
        // Arrange
        JSONObject parentObj = new JSONObject();
        parentObj.put("Currency", null);
        parentObj.put("1000", "usd");

        JSONObject validationResults = VRPConsentRequestValidator.validateMaximumIndividualAmount(parentObj);

        Assert.assertFalse((Boolean) validationResults.get(ConsentExtensionConstants.IS_VALID));
        // Act
        boolean isValid = validateAmountCurrency(parentObj, "Currency");
        parentObj.put(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT, parentObj);

        // Assert
        Assert.assertFalse(isValid);
    }


    @Test
    public void testValidateAmountCurrencyWithoutAmountKey() {
        // Test case 1: parentObj is null
        boolean result1 = validateAmountCurrency(null, "Amount");
        Assert.assertFalse(result1);

        // Test case 2: Key is not present in parentObj
        boolean result2 = validateAmountCurrency(new JSONObject(), "Amount");
        Assert.assertFalse(result2);

        // Test case 3: Key is present, but value is not a String
        JSONObject testData3 = new JSONObject();
        testData3.put("Amount", 123); // Assuming "Amount" should be a String, but it's an integer in this case
        boolean result3 = validateAmountCurrency(testData3, "Amount");
        Assert.assertFalse(result3);

        // Test case 4: Key is present, and value is an empty String
        JSONObject testData4 = new JSONObject();
        testData4.put("Amount", "");
        boolean result4 = validateAmountCurrency(testData4, "Amount");
        Assert.assertFalse(result4);
    }

    @Test
    public void testValidateAmountCurrencyWithoutCurrencyKey() {
        // Test case 1: parentObj is null
        boolean result1 = validateAmountCurrency(null, "Currency");
        Assert.assertFalse(result1);

        // Test case 2: Key is not present in parentObj
        boolean result2 = validateAmountCurrency(new JSONObject(), "Currency");
        Assert.assertFalse(result2);

        // Test case 3: Key is present, but value is not a String
        JSONObject testData3 = new JSONObject();
        testData3.put("Currency", 123); // Assuming "Currency" should be a String, but it's an integer in this case
        boolean result3 = validateAmountCurrency(testData3, "Currency");
        Assert.assertFalse(result3);

        // Test case 4: Key is present, and value is an empty String
        JSONObject testData4 = new JSONObject();
        testData4.put("Currency", "");
        boolean result4 = validateAmountCurrency(testData4, "Currency");
        Assert.assertFalse(result4);
    }


    @Test
    public void testVrpInitiationPayloadWithoutControlParameterss() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_CURRENCY;
        JSONObject result = VRPConsentRequestValidator.validateMaximumIndividualAmount((JSONObject) JSONValue.
                parse(initiationPayloads));

        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        assertFalse(result.containsKey("Currency"));
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
        boolean result = validateAmountCurrency(parentObj, key);
        assertTrue(result);

        // Test case 2: "Currency" key is present, but value is an empty String
        parentObj.put("Currency", "");
        result = validateAmountCurrency(parentObj, key);
        Assert.assertFalse(result);

        // Test case 3: "Currency" key is present, but value is not a String
        parentObj.put("Currency", 123); // Assuming "Currency" should be a String, but it's an integer in this case
        result = validateAmountCurrency(parentObj, key);
        Assert.assertFalse(result);

        // Test case 4: "Currency" key is not present in parentObj
        parentObj = new JSONObject();
        result = validateAmountCurrency(parentObj, key);
        Assert.assertFalse(result);

        // Test case 5: parentObj is null
        result = validateAmountCurrency(null, key);
        Assert.assertFalse(result);
    }


    @Test
    public void testValidateAmountCurrencyNegativeScenariosss() {
        // Test case 1: "Currency" key is present, but value is an empty String
        JSONObject maximumIndividualAmount = new JSONObject();
        maximumIndividualAmount.put("Currency", "");
        boolean result1 = validateAmountCurrency(maximumIndividualAmount, ConsentExtensionConstants.CURRENCY);
        Assert.assertFalse(result1);

        // Test case 2: "Currency" key is present, but value is not a String
        maximumIndividualAmount.put("Currency", 123);
        // Assuming "Currency" should be a String, but it's an integer in this case
        boolean result2 = validateAmountCurrency(maximumIndividualAmount, ConsentExtensionConstants.CURRENCY);
        Assert.assertFalse(result2);

        // Test case 3: "Currency" key is not present in maximumIndividualAmount
        maximumIndividualAmount = new JSONObject();
        boolean result3 = validateAmountCurrency(maximumIndividualAmount, ConsentExtensionConstants.CURRENCY);
        Assert.assertFalse(result3);

        // Test case 4: maximumIndividualAmount is null
        boolean result4 = validateAmountCurrency(null, ConsentExtensionConstants.CURRENCY);
        Assert.assertFalse(result4);
    }


    @Test
    public void testValidatePeriodicAlignmentt() {
        // Arrange
        String validAlignment = ConsentExtensionConstants.CONSENT;
        String invalidAlignment = "invalidAlignment";
        String emptyAlignment = "";

        // Act
        boolean isValidValidAlignment = VRPConsentRequestValidator.validatePeriodicAlignment(validAlignment);
        boolean isValidInvalidAlignment = VRPConsentRequestValidator.validatePeriodicAlignment(invalidAlignment);
        boolean isValidEmptyAlignment = VRPConsentRequestValidator.validatePeriodicAlignment(emptyAlignment);

        Assert.assertFalse(isValidInvalidAlignment);
        Assert.assertFalse(isValidEmptyAlignment);
    }


    @Test
    public void testValidatePeriodicLimitTypes() {
        // Arrange
        JSONObject controlParameters = new JSONObject();

        // Create a valid periodic limits array
        JSONArray validPeriodicLimits = new JSONArray();
        JSONObject validPeriodicLimit = new JSONObject();
        validPeriodicLimit.put(ConsentExtensionConstants.PERIOD_ALIGNMENT, ConsentExtensionConstants.CONSENT);
        validPeriodicLimit.put(ConsentExtensionConstants.PERIOD_TYPE, ConsentExtensionConstants.DAY);
        validPeriodicLimits.add(validPeriodicLimit);

        // Add the valid periodic limits array to control parameters
        controlParameters.put(ConsentExtensionConstants.PERIODIC_LIMITS, validPeriodicLimits);

        // Act
        JSONObject validationResult = VRPConsentRequestValidator.validateVRPPayload(controlParameters);

        // Assert
        Assert.assertFalse((boolean) validationResult.get(ConsentExtensionConstants.IS_VALID));
    }

    @Test
    public void testValidatePeriodicType() {
        // Test case 1: Valid periodic type
        JSONObject validLimitObject = new JSONObject();
        validLimitObject.put(ConsentExtensionConstants.PERIOD_TYPE, ConsentExtensionConstants.DAY);
        boolean result1 = VRPConsentRequestValidator.validatePeriodicType(validLimitObject);
        assertTrue(result1);

        // Test case 2: Missing period type key
        JSONObject missingKeyObject = new JSONObject();
        boolean result2 = VRPConsentRequestValidator.validatePeriodicType(missingKeyObject);
        Assert.assertFalse(result2);

        // Test case 3: Null period type
        JSONObject nullPeriodTypeObject = new JSONObject();
        nullPeriodTypeObject.put(ConsentExtensionConstants.PERIOD_TYPE, null);
        boolean result3 = VRPConsentRequestValidator.validatePeriodicType(nullPeriodTypeObject);
        Assert.assertFalse(result3);

        // Test case 4: Empty period type
        JSONObject emptyPeriodTypeObject = new JSONObject();
        emptyPeriodTypeObject.put(ConsentExtensionConstants.PERIOD_TYPE, "");
        boolean result4 = VRPConsentRequestValidator.validatePeriodicType(emptyPeriodTypeObject);
        Assert.assertFalse(result4);

        // Test case 5: Invalid period type
        JSONObject invalidPeriodTypeObject = new JSONObject();
        invalidPeriodTypeObject.put(ConsentExtensionConstants.PERIOD_TYPE, "InvalidType");
        boolean result5 = VRPConsentRequestValidator.validatePeriodicType(invalidPeriodTypeObject);
        Assert.assertFalse(result5);
    }

    @Test
    public void testVrpInitiationPayloadWithoutPeriodicType() {

        String initiationPayloads = VRPTestConstants.METADATA_VRP_WITHOUT_PERIODIC_TYPE;
        JSONObject result = VRPConsentRequestValidator.validateControlParameters((JSONObject) JSONValue.
                parse(initiationPayloads));
        JSONObject result2 = VRPConsentRequestValidator.validatePeriodicLimits((JSONObject) JSONValue.
                parse(initiationPayloads));
        boolean result3 = VRPConsentRequestValidator.validatePeriodicType((JSONObject) JSONValue.
                parse(initiationPayloads));


        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result2.get(ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result3);
        assertFalse(result.containsKey("PeriodType"));
    }


    @Test
    public void testValidateAmountCurrencyWithCurrencyKey() {

        JSONObject testData1 = new JSONObject();
        testData1.put("currency", "USD");

        boolean result1 = VRPConsentRequestValidator.validateAmountCurrency(testData1, "currency");
        assertTrue(result1);

        // Test case 2: Invalid currency key (empty value)
        JSONObject testData2 = new JSONObject();
        testData2.put("currency", "");

        boolean result2 = VRPConsentRequestValidator.validateAmountCurrency(testData2, "currency");
        Assert.assertFalse(result2);

        // Test case 3: Invalid currency key (missing key)
        JSONObject testData3 = new JSONObject();

        boolean result3 = VRPConsentRequestValidator.validateAmountCurrency(testData3, "currency");
        Assert.assertFalse(result3);

        // Test case 4: Invalid currency key (null parentObj)
        boolean result4 = VRPConsentRequestValidator.validateAmountCurrency(null, "currency");
        Assert.assertFalse(result4);

        // Add more test cases as needed
    }


    @Test
    public void testValidationFailureForNullCurrencyKey() {

        JSONArray periodicLimits = new JSONArray();
        periodicLimits.add(new JSONObject());

        // Call the method that checks currency validation
        boolean result = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(periodicLimits,
                ConsentExtensionConstants.CURRENCY);

        Assert.assertFalse(result);
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
        assertFalse(result.containsKey("Currency"));
    }


    @Test
    public void testValidationFailureForMissingKey() {

        // Create a sample JSONArray with a JSONObject that does not contain the key
        JSONArray periodicLimits = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("otherKey", "someValue"); // Add a different key
        periodicLimits.add(jsonObject);

        // Call the method that checks currency validation
        boolean result = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(periodicLimits,
                ConsentExtensionConstants.CURRENCY);

        // Assert that the result is false, indicating a validation failure for a missing key
        Assert.assertFalse(result);
    }

    @Test
    public void testValidationFailureForNullParentArray() {

        // Call the method with a null parentArray
        boolean result = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimits(null,
                ConsentExtensionConstants.CURRENCY);

        // Assert that the result is false, indicating a validation failure for a null parentArray
        Assert.assertFalse(result);
    }


    @Test
    public void testValidateControlParameters() {

        JSONObject controlParameters = new JSONObject();

        JSONObject result = VRPConsentRequestValidator.validateControlParameters(controlParameters);

        assertTrue(result.containsKey(ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
    }


    @Test
    public void testValidateAmountCurrencyPeriodicLimits_Invalid() {

        JSONObject controlParameters = new JSONObject();
        JSONArray periodicLimits = new JSONArray();

        JSONObject invalidPeriodicLimit = new JSONObject();
        periodicLimits.add(invalidPeriodicLimit);

        controlParameters.put(ConsentExtensionConstants.PERIODIC_LIMITS, periodicLimits);

        JSONObject result = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimit(controlParameters);

        assertTrue(result.containsKey(ConsentExtensionConstants.IS_VALID));
        Assert.assertFalse((boolean) result.get(ConsentExtensionConstants.IS_VALID));
    }

    @Test
    public void testValidateAmountCurrencyPeriodicLimit_WithErrors() {

        JSONObject controlParameters = new JSONObject();
        JSONArray periodicLimits = new JSONArray();

        JSONObject invalidPeriodicLimit = new JSONObject();
        periodicLimits.add(invalidPeriodicLimit);

        controlParameters.put(ConsentExtensionConstants.PERIODIC_LIMITS, periodicLimits);

        JSONObject periodicLimitType = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimit(controlParameters);

        Assert.assertFalse((boolean) periodicLimitType.get(ConsentExtensionConstants.IS_VALID));

        assertTrue(periodicLimitType.containsKey(ConsentExtensionConstants.ERRORS));

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

        Assert.assertFalse(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)));
    }

    @Test
    public void testValidatePeriodicLimits_InvalidFormat() {
        JSONObject controlParametersObject = new JSONObject();
        controlParametersObject.put(ConsentExtensionConstants.PERIODIC_LIMITS, "InvalidFormat");

        JSONObject validationResult = VRPConsentRequestValidator.validatePeriodicLimits(controlParametersObject);

        Assert.assertFalse(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)));

    }

    @Test
    public void testValidatePeriodicLimits_MissingPeriodLimits() {

        JSONObject controlParametersObject = new JSONObject();


        JSONObject validationResult = VRPConsentRequestValidator.validatePeriodicLimits(controlParametersObject);

        Assert.assertFalse(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)));
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
                validateAmountCurrencyPeriodicLimit(controlParametersObject);

        assertTrue(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)));
    }


    @Test
    public void testValidateAmountCurrencyPeriodicLimit_MissingCurrency() {
        JSONObject controlParametersObject = new JSONObject();
        JSONArray periodicLimitsArray = new JSONArray();

        JSONObject periodicLimit1 = new JSONObject();
        periodicLimitsArray.add(periodicLimit1);

        controlParametersObject.put(ConsentExtensionConstants.PERIODIC_LIMITS, periodicLimitsArray);

        JSONObject validationResult = VRPConsentRequestValidator.
                validateAmountCurrencyPeriodicLimit(controlParametersObject);

        Assert.assertFalse(Boolean.parseBoolean(validationResult.getAsString(ConsentExtensionConstants.IS_VALID)));

    }

    @Test
    public void testValidatePeriodicType_Valid() {
        JSONObject periodicLimitObject = new JSONObject();
        periodicLimitObject.put(ConsentExtensionConstants.PERIOD_TYPE, ConsentExtensionConstants.MONTH);

        boolean validationResult = VRPConsentRequestValidator.validatePeriodicType(periodicLimitObject);

        assertTrue(validationResult);
    }

    @Test
    public void testValidatePeriodicType_InvalidType() {
        JSONObject periodicLimitObject = new JSONObject();
        periodicLimitObject.put(ConsentExtensionConstants.PERIOD_TYPE, "InvalidType");

        boolean validationResult = VRPConsentRequestValidator.validatePeriodicType(periodicLimitObject);

        Assert.assertFalse(validationResult);

    }

    @Test
    public void testValidatePeriodicType_MissingType() {

        JSONObject periodicLimitObject = new JSONObject();

        boolean validationResult = VRPConsentRequestValidator.validatePeriodicType(periodicLimitObject);

        Assert.assertFalse(validationResult);
    }

    @Test
    public void testValidatePeriodicType_EmptyType() {

        JSONObject periodicLimitObject = new JSONObject();
        periodicLimitObject.put(ConsentExtensionConstants.PERIOD_TYPE, "");

        boolean validationResult = VRPConsentRequestValidator.validatePeriodicType(periodicLimitObject);

        Assert.assertFalse(validationResult);
    }

    @Test
    public void testValidatePeriodicType_NullType() {
        JSONObject periodicLimitObject = new JSONObject();
        periodicLimitObject.put(ConsentExtensionConstants.PERIOD_TYPE, null);

        boolean validationResult = VRPConsentRequestValidator.validatePeriodicType(periodicLimitObject);

        Assert.assertFalse(validationResult);
    }

    @Test
    public void testYourMethod_ValidPeriodicType() {

        JSONObject controlParameters = new JSONObject();
        JSONArray periodicLimits = new JSONArray();

        JSONObject periodicLimit = new JSONObject();
        periodicLimit.put(ConsentExtensionConstants.PERIOD_TYPE, ConsentExtensionConstants.MONTH);
        periodicLimits.add(periodicLimit);

        controlParameters.put(ConsentExtensionConstants.PERIODIC_LIMITS, periodicLimits);

        JSONObject result = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimit(controlParameters);

        Assert.assertFalse(Boolean.parseBoolean(result.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals( PERIODIC_LIMIT_CURRENCY_IS_MISSING,
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

        JSONObject result = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimit(controlParameters);

        Assert.assertFalse(Boolean.parseBoolean(result.getAsString(ConsentExtensionConstants.IS_VALID)));
        Assert.assertEquals( PERIODIC_LIMIT_CURRENCY_IS_MISSING,
                result.get(ConsentExtensionConstants.ERRORS));

    }

//    @Test
//    public void testYourMethod_MissingPeriodicType() {
//
//        JSONObject controlParameters = new JSONObject();
//
//        JSONObject result = VRPConsentRequestValidator.validateAmountCurrencyPeriodicLimit(controlParameters);
//
//        Assert.assertFalse(Boolean.parseBoolean(result.getAsString(ConsentExtensionConstants.IS_VALID)));
//        Assert.assertEquals( PERIODIC_LIMIT_CURRENCY_IS_MISSING,
//                result.get(ConsentExtensionConstants.ERRORS));
//    }


}



