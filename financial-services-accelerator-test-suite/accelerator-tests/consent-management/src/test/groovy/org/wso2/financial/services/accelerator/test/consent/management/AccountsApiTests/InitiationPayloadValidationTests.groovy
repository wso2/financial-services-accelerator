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

package org.wso2.financial.services.accelerator.test.consent.management.AccountsApiTests

import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.AccountsRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.utility.AccountsDataProviders
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Accounts Initiation Payload Validation Tests - UK Specification
 */
class InitiationPayloadValidationTests extends FSConnectorTest {

    @BeforeClass
    void init() {
        consentPath = ConnectorTestConstants.ACCOUNT_CONSENT_PATH
    }

    @Test
    void "TC0201042_Accounts Initiation Without Permissions"() {

        consentResponse = doDefaultInitiation(AccountsRequestPayloads.initiationPayloadWithoutPermissions)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
                "Permissions are not in correct format")
    }

    @Test(dataProvider = "InvalidAccountsPermissionsForInitiation", dataProviderClass = AccountsDataProviders.class)
    void "TC0201019_Accounts Initiation With Invalid Permissions"(permissions) {

        setInitiationPayload(AccountsRequestPayloads.getUpdatedInitiationPayload(permissions))
        doDefaultInitiation()

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
                "Permissions are invalid")
    }

    @Test
    void "TC0201027_Accounts Initiation Without Specifying Expiration Date Time"() {

        consentResponse = doDefaultInitiation(AccountsRequestPayloads.initiationPayloadWithoutExpirationDate)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        Assert.assertNull(TestUtil.parseResponseBody(consentResponse, "Data.ExpirationDateTime"))
    }

    @Test
    void "TC0201028_Accounts Initiation With Invalid Expiration Date Time Format"() {

        consentResponse = doDefaultInitiation(AccountsRequestPayloads.initiationPayloadWithInvalidExpirationDate)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION).contains(
                "is invalid against requested date format(s) [yyyy-MM-dd'T'HH:mm:ssZ, yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,12}Z]"))
    }

    @Test
    void "TC0201029_Accounts Initiation With Past Expiration Date Time"() {

        consentResponse = doDefaultInitiation(AccountsRequestPayloads.initiationPayloadWithPastExpirationDate)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
                "ExpirationDateTime should be a future time")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
    }

    @Test
    void "TC0201030_Accounts Initiation Without Specifying Transaction To Date Time"() {

        consentResponse = doDefaultInitiation(AccountsRequestPayloads.initiationPayloadWithoutTransactionToDate)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        Assert.assertNull(TestUtil.parseResponseBody(consentResponse, "Data.TransactionToDateTime"))
    }

    @Test
    void "TC0201031_Accounts Initiation With Invalid Transaction To Date Time Format"() {

        consentResponse = doDefaultInitiation(AccountsRequestPayloads.initiationPayloadWithInvalidTransactionToDate)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION).contains(
                "is invalid against requested date format(s) [yyyy-MM-dd'T'HH:mm:ssZ, yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,12}Z]"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
    }

    @Test
    void "TC0201032_Accounts Initiation Without Specifying Transaction From Date Time"() {

        consentResponse = doDefaultInitiation(AccountsRequestPayloads.initiationPayloadWithoutTransactionFromDate)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        Assert.assertNull(TestUtil.parseResponseBody(consentResponse, "Data.TransactionFromDateTime"))
    }

    @Test
    void "TC0201033_Accounts Initiation With Invalid Transaction From Date Time Format"() {

        consentResponse = doDefaultInitiation(AccountsRequestPayloads.initiationPayloadWithInvalidTransactionFromDate)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION).contains(
                "is invalid against requested date format(s) [yyyy-MM-dd'T'HH:mm:ssZ, yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,12}Z]"))
    }

    @Test
    void "TC0201034_Accounts Initiation With Past TransactionToDateTime than TransactionFromDateTime"() {

        consentResponse = doDefaultInitiation(AccountsRequestPayloads.initiationPayloadWithPastTransactionToDateTime)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
                "TransactionToDateTime should be after TransactionFromDateTime")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
    }

    @Test
    void "TC0201035_Accounts Initiation With Empty Payload"() {

        consentResponse = doDefaultInitiation(AccountsRequestPayloads.initiationPayloadEmptyPayload)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION).contains(
                "Payload is not in the correct format"))
    }

    @Test
    void "TC0201036_Accounts Initiation With Empty Json Payload"() {

        consentResponse = doDefaultInitiation(AccountsRequestPayloads.initiationPayloadEmptyJsonPayload)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION).contains(
                "Object has missing required properties ([\"Data\",\"Risk\"])"))
    }

    @Test
    void "TC0201037_Accounts Initiation With Empty String Payload"() {

        consentResponse = doDefaultInitiation(AccountsRequestPayloads.initiationPayloadWithEmptyString)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION).contains(
                "Instance type (string) does not match any allowed primitive type (allowed: [\"object\"])"))
    }

    @Test
    void "TC0201039_Initiation Request With same  TransactionFromDate and TransactionToDate"() {

        consentResponse = doDefaultInitiation(AccountsRequestPayloads.initiationPayloadWithSameTransactionFromToDates)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_201)
    }

    @Test
    void "Accounts Initiation with permissions not in array format"() {

        consentResponse = doDefaultInitiation(AccountsRequestPayloads.initiationPayloadWithoutArrayFormat)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION).contains(
                "Payload is not in the correct format"))
    }

    @Test(dataProvider = "ValidAccountsPermissionsForInitiation", dataProviderClass = AccountsDataProviders.class)
    void "Accounts Initiation With valid combined permissions"(permissions) {

        setInitiationPayload(AccountsRequestPayloads.getUpdatedInitiationPayload(permissions))
        doDefaultInitiation()

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
    }
}
