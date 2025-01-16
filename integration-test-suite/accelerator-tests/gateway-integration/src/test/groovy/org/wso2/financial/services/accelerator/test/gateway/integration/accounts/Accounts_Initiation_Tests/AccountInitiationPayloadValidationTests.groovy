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

package org.wso2.financial.services.accelerator.test.gateway.integration.accounts.Accounts_Initiation_Tests

import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.constant.AcceleratorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil
import org.wso2.financial.services.accelerator.test.gateway.integration.accounts.util.AbstractAccountsFlow
import org.wso2.financial.services.accelerator.test.gateway.integration.accounts.util.AccountsDataProviders
import org.wso2.financial.services.accelerator.test.gateway.integration.accounts.util.AccountPayloads

/**
 * Account Initiation Payload Validation Tests
 */
class AccountInitiationPayloadValidationTests extends AbstractAccountsFlow {

    @Test()
    void "TC0201042_Accounts Initiation Without Permissions"() {

        Response consentResponse = doDefaultAccountInitiationWithUpdatedPayload(RequestPayloads.initiationPayloadWithoutPermissions)

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.ERROR_CODE),
                AcceleratorTestConstants.ERROR_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_MSG),
                AcceleratorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.DESCRIPTION)
                .contains("Schema validation failed in the Request:"))
    }

    @Test(dataProvider = "InvalidAccountsPermissionsForInitiation",
            dataProviderClass = AccountsDataProviders.class)
    void "TC0201019_Accounts Initiation With Invalid Permissions"(permissions) {

        setInitiationPayload(RequestPayloads.generateConsentInitiationPayload(permissions))
        doDefaultAccountInitiation()

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.ERROR_CODE),
                AcceleratorTestConstants.ERROR_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_MSG),
                AcceleratorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.DESCRIPTION)
                .contains("Schema validation failed in the Request:"))
    }

    @Test
    void "TC0201027_Accounts Initiation Without Specifying Expiration Date Time"() {

        Response consentResponse = doDefaultAccountInitiationWithUpdatedPayload(AccountPayloads.initiationPayloadWithoutExpirationDate)

        Assert.assertEquals(consentResponse.statusCode(),AcceleratorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.ERROR_ERRORS_CODE),
                AcceleratorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_ERRORS_MSG),
                AcceleratorTestConstants.CONSENT_MGT_ERROR)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_ERRORS_DESCRIPTION)
                .contains("ExpirationDateTime is invalid"))
    }

    @Test
    void "TC0201028_Accounts Initiation With Invalid Expiration Date Time Format"() {

        Response consentResponse = doDefaultAccountInitiationWithUpdatedPayload(AccountPayloads.initiationPayloadWithInvalidExpirationDate)

        Assert.assertEquals(consentResponse.statusCode(),AcceleratorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.ERROR_CODE),
                AcceleratorTestConstants.ERROR_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_MSG),
                AcceleratorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.DESCRIPTION)
                .contains("Schema validation failed in the Request:"))
    }

    @Test
    void "TC0201029_Accounts Initiation With Past Expiration Date Time"() {

        Response consentResponse = doDefaultAccountInitiationWithUpdatedPayload(AccountPayloads.initiationPayloadWithPastExpirationDate)

        Assert.assertEquals(consentResponse.statusCode(),AcceleratorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.ERROR_ERRORS_CODE),
                AcceleratorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_ERRORS_MSG),
                AcceleratorTestConstants.CONSENT_MGT_ERROR)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_ERRORS_DESCRIPTION)
                .contains("ExpirationDateTime should be a future time"))
    }

    @Test
    void "TC0201030_Accounts Initiation Without Specifying Transaction To Date Time"() {

        Response consentResponse = doDefaultAccountInitiationWithUpdatedPayload(AccountPayloads.initiationPayloadWithoutTransactionToDate)

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.ERROR_CODE),
                AcceleratorTestConstants.ERROR_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_MSG),
                AcceleratorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.DESCRIPTION)
                .contains("Schema validation failed in the Request:"))
    }

    @Test
    void "TC0201031_Accounts Initiation With Invalid Transaction To Date Time Format"() {

        Response consentResponse = doDefaultAccountInitiationWithUpdatedPayload(AccountPayloads.initiationPayloadWithInvalidTransactionToDate)

        Assert.assertEquals(consentResponse.statusCode(),AcceleratorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.ERROR_CODE),
                AcceleratorTestConstants.ERROR_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_MSG),
                AcceleratorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.DESCRIPTION)
                .contains("Schema validation failed in the Request:"))
    }

    @Test
    void "TC0201032_Accounts Initiation Without Specifying Transaction From Date Time"() {

        Response consentResponse = doDefaultAccountInitiationWithUpdatedPayload(AccountPayloads.initiationPayloadWithoutTransactionFromDate)

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.ERROR_ERRORS_CODE),
                AcceleratorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_ERRORS_MSG),
                AcceleratorTestConstants.CONSENT_MGT_ERROR)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_ERRORS_DESCRIPTION)
                .contains("TransactionFromDateTime is invalid"))
    }

    @Test
    void "TC0201033_Accounts Initiation With Invalid Transaction From Date Time Format"() {

        Response consentResponse = doDefaultAccountInitiationWithUpdatedPayload(AccountPayloads.initiationPayloadWithInvalidTransactionFromDate)

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.ERROR_CODE),
                AcceleratorTestConstants.ERROR_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_MSG),
                AcceleratorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.DESCRIPTION)
                .contains("Schema validation failed in the Request:"))
    }

    @Test
    void "TC0201034_Accounts Initiation With Past TransactionToDateTime than TransactionFromDateTime"() {

        Response consentResponse = doDefaultAccountInitiationWithUpdatedPayload(AccountPayloads.initiationPayloadWithPastTransactionToDateTime)

        Assert.assertEquals(consentResponse.statusCode(),AcceleratorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.ERROR_ERRORS_CODE),
                AcceleratorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_ERRORS_MSG),
                AcceleratorTestConstants.CONSENT_MGT_ERROR)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_ERRORS_DESCRIPTION)
                .contains("TransactionToDateTime should be after TransactionFromDateTime"))
    }

    /*
     * Test the initiation flow without the payload
     * Expected response code is 400 and OBIE Error Code of UK.OBIE.Field.Invalid
     */
    @Test
    void "TC0201035_Accounts Initiation With Empty Payload"() {

        Response consentResponse = doDefaultAccountInitiationWithUpdatedPayload(AccountPayloads.initiationPayloadEmptyPayload)

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.ERROR_CODE),
                AcceleratorTestConstants.ERROR_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_MSG),
                AcceleratorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.DESCRIPTION)
                .contains("Schema validation failed in the Request:"))
    }

    /*
     * Test the initiation flow with empty json as the  payload
     * Expected response code is 400 and OBIE Error Code of UK.OBIE.Field.Missing
     */
    @Test
    void "TC0201036_Accounts Initiation With Empty Json Payload"() {

        Response consentResponse = doDefaultAccountInitiationWithUpdatedPayload(AccountPayloads.initiationPayloadEmptyJsonPayload)

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.ERROR_CODE),
                AcceleratorTestConstants.ERROR_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_MSG),
                AcceleratorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.DESCRIPTION)
                .contains("Schema validation failed in the Request:"))
    }

    /*
     * Test the initiation flow with empty string as the  payload
     * Expected response code is 400 and OBIE Error Code of UK.OBIE.Resource.InvalidFormat
     */
    @Test
    void "TC0201037_Accounts Initiation With Empty String Payload"() {

        Response consentResponse = doDefaultAccountInitiationWithUpdatedPayload(AccountPayloads.initiationPayloadWithEmptyString)

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.ERROR_CODE),
                AcceleratorTestConstants.ERROR_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_MSG),
                AcceleratorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.DESCRIPTION)
                .contains("Schema validation failed in the Request:"))
    }

    @Test
    void "TC0201039_Initiation Request With same  TransactionFromDate and TransactionToDate"() {

        Response consentResponse = doDefaultAccountInitiationWithUpdatedPayload(AccountPayloads.initiationPayloadWithSameTransactionFromToDates)

        Assert.assertEquals(consentResponse.statusCode(),AcceleratorTestConstants.STATUS_CODE_201)
    }
}
