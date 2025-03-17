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
import org.wso2.financial.services.accelerator.test.framework.constant.AccountsRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
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
    void "Accounts Initiation Without Permissions"() {

        doDefaultInitiation(AccountsRequestPayloads.initiationPayloadWithoutPermissions)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
                "Permissions are not in correct format")
    }

    @Test(dataProvider = "InvalidAccountsPermissionsForInitiation", dataProviderClass = AccountsDataProviders.class)
    void "Accounts Initiation With Invalid Permissions"(permissions) {

        setInitiationPayload(AccountsRequestPayloads.getUpdatedInitiationPayload(permissions))
        doDefaultInitiation()

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
                "Permissions are invalid")
    }

    @Test
    void "Accounts Initiation Without Specifying Expiration Date Time"() {

        doDefaultInitiation(AccountsRequestPayloads.initiationPayloadWithoutExpirationDate)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
    }

    @Test
    void "Accounts Initiation With Invalid Expiration Date Time Format"() {

        doDefaultInitiation(AccountsRequestPayloads.initiationPayloadWithInvalidExpirationDate)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION).contains(
                "ExpirationDateTime is invalid"))
    }

    @Test
    void "Accounts Initiation With Past Expiration Date Time"() {

        doDefaultInitiation(AccountsRequestPayloads.initiationPayloadWithPastExpirationDate)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
                "ExpirationDateTime should be a future date")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
    }

    @Test
    void "Accounts Initiation Without Specifying Transaction To Date Time"() {

        doDefaultInitiation(AccountsRequestPayloads.initiationPayloadWithoutTransactionToDate)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        Assert.assertNull(TestUtil.parseResponseBody(consentResponse, "Data.TransactionToDateTime"))
    }

    @Test
    void "Accounts Initiation With Invalid Transaction To Date Time Format"() {

        doDefaultInitiation(AccountsRequestPayloads.initiationPayloadWithInvalidTransactionToDate)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION).contains(
                "TransactionToDateTime is invalid"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
    }

    @Test
    void "Accounts Initiation Without Specifying Transaction From Date Time"() {

        doDefaultInitiation(AccountsRequestPayloads.initiationPayloadWithoutTransactionFromDate)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        Assert.assertNull(TestUtil.parseResponseBody(consentResponse, "Data.TransactionFromDateTime"))
    }

    @Test
    void "Accounts Initiation With Invalid Transaction From Date Time Format"() {

        doDefaultInitiation(AccountsRequestPayloads.initiationPayloadWithInvalidTransactionFromDate)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION).contains(
                "TransactionFromDateTime is invalid"))
    }

    @Test
    void "Accounts Initiation With Past TransactionToDateTime than TransactionFromDateTime"() {

        doDefaultInitiation(AccountsRequestPayloads.initiationPayloadWithPastTransactionToDateTime)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
                "TransactionToDateTime should be after TransactionFromDateTime")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
    }

    @Test
    void "Accounts Initiation With Empty Payload"() {

        doDefaultInitiation(AccountsRequestPayloads.initiationPayloadEmptyPayload)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION).contains(
                "Payload is not in the correct format"))
    }

    @Test
    void "Accounts Initiation With Empty Json Payload"() {

        doDefaultInitiation(AccountsRequestPayloads.initiationPayloadEmptyJsonPayload)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION).contains(
                "Invalid request payload"))
    }

    @Test
    void "Accounts Initiation With Empty String Payload"() {

        doDefaultInitiation(AccountsRequestPayloads.initiationPayloadWithEmptyString)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION).contains(
                "Payload is not in the correct format"))
    }

    @Test
    void "Initiation Request With same  TransactionFromDate and TransactionToDate"() {

        doDefaultInitiation(AccountsRequestPayloads.initiationPayloadWithSameTransactionFromToDates)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_201)
    }

    @Test
    void "Accounts Initiation with permissions not in array format"() {

        doDefaultInitiation(AccountsRequestPayloads.initiationPayloadWithoutArrayFormat)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION).contains(
                "Permissions are not in correct format"))
    }

    @Test(dataProvider = "ValidAccountsPermissionsForInitiation", dataProviderClass = AccountsDataProviders.class)
    void "Accounts Initiation With valid combined permissions"(permissions) {

        setInitiationPayload(AccountsRequestPayloads.getUpdatedInitiationPayload(permissions))
        doDefaultInitiation()

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
    }
}
