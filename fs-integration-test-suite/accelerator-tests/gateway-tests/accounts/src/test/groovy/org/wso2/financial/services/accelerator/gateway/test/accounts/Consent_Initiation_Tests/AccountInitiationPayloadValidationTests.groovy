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

package org.wso2.financial.services.accelerator.gateway.test.accounts.Consent_Initiation_Tests


import jdk.internal.net.http.Response
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.AccountsRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.utility.AccountsDataProviders
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Account Initiation Payload Validation Tests
 */
class AccountInitiationPayloadValidationTests extends FSAPIMConnectorTest {

    @BeforeClass
    void init() {
        consentPath = ConnectorTestConstants.AISP_CONSENT_PATH
        initiationPayload = AccountsRequestPayloads.initiationPayload
        scopeList = ConsentMgtTestUtils.getApiScopesForConsentType(ConnectorTestConstants.ACCOUNTS_TYPE)

        //Get application access token
        applicationAccessToken = getApplicationAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), scopeList)
    }

    @Test()
    void "Accounts Initiation Without Permissions"() {

        consentResponse = doDefaultAccountInitiationWithUpdatedPayload(RequestPayloads.initiationPayloadWithoutPermissions)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
                "consent_default")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
                .contains("Permissions are not in correct format"))
    }

    @Test(dataProvider = "InvalidAccountsPermissionsForInitiation",
            dataProviderClass = AccountsDataProviders.class)
    void "Accounts Initiation With Invalid Permissions"(permissions) {

        setInitiationPayload(RequestPayloads.generateConsentInitiationPayload(permissions))
        doDefaultAccountInitiation()

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
                "consent_default")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
                .contains("Permissions are invalid"))
    }

    @Test
    void "Accounts Initiation Without Specifying Expiration Date Time"() {

        consentResponse = doDefaultAccountInitiationWithUpdatedPayload(AccountsRequestPayloads.initiationPayloadWithoutExpirationDate)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_MSG),
                "consent_default")
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
                .contains("ExpirationDateTime is invalid"))
    }

    @Test
    void "Accounts Initiation With Invalid Expiration Date Time Format"() {

        consentResponse = doDefaultAccountInitiationWithUpdatedPayload(AccountsRequestPayloads.initiationPayloadWithInvalidExpirationDate)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
                "consent_default")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
                .contains("ExpirationDateTime is invalid"))
    }

    @Test
    void "Accounts Initiation With Past Expiration Date Time"() {

        consentResponse = doDefaultAccountInitiationWithUpdatedPayload(AccountsRequestPayloads.initiationPayloadWithPastExpirationDate)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
                "consent_default")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
                .contains("ExpirationDateTime should be a future date"))
    }

//    @Test
//    void "Accounts Initiation Without Specifying Transaction To Date Time"() {
//
//        consentResponse = doDefaultAccountInitiationWithUpdatedPayload(AccountsRequestPayloads.initiationPayloadWithoutTransactionToDate)
//
//        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
//        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
//                ConnectorTestConstants.ERROR_CODE_400)
//        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
//                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
//        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
//                .contains("Schema validation failed in the Request:"))
//    }
//
    @Test
    void "Accounts Initiation With Invalid Transaction To Date Time Format"() {

        consentResponse = doDefaultAccountInitiationWithUpdatedPayload(AccountsRequestPayloads.initiationPayloadWithInvalidTransactionToDate)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
                "consent_default")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
                .contains("TransactionToDateTime is invalid"))
    }

//    @Test
//    void "Accounts Initiation Without Specifying Transaction From Date Time"() {
//
//        consentResponse = doDefaultAccountInitiationWithUpdatedPayload(AccountsRequestPayloads.initiationPayloadWithoutTransactionFromDate)
//
//        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
//        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
//                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
//        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_MSG),
//                ConnectorTestConstants.CONSENT_MGT_ERROR)
//        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
//                .contains("TransactionFromDateTime is invalid"))
//    }

    @Test
    void "Accounts Initiation With Invalid Transaction From Date Time Format"() {

        consentResponse = doDefaultAccountInitiationWithUpdatedPayload(AccountsRequestPayloads.initiationPayloadWithInvalidTransactionFromDate)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
                "consent_default")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
                .contains("TransactionFromDateTime is invalid"))
    }

    @Test
    void "Accounts Initiation With Past TransactionToDateTime than TransactionFromDateTime"() {

        consentResponse = doDefaultAccountInitiationWithUpdatedPayload(AccountsRequestPayloads.initiationPayloadWithPastTransactionToDateTime)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
                "consent_default")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
                .contains("TransactionToDateTime should be after TransactionFromDateTime"))
    }

    /*
     * Test the initiation flow without the payload
     * Expected response code is 400 and OBIE Error Code of UK.OBIE.Field.Invalid
     */
    @Test
    void "Accounts Initiation With Empty Payload"() {

        consentResponse = doDefaultAccountInitiationWithUpdatedPayload(AccountsRequestPayloads.initiationPayloadEmptyPayload)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
                "consent_default")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
                .contains("Payload is not in the correct format"))
    }

    /*
     * Test the initiation flow with empty json as the  payload
     * Expected response code is 400 and OBIE Error Code of UK.OBIE.Field.Missing
     */
    @Test
    void "Accounts Initiation With Empty Json Payload"() {

        consentResponse = doDefaultAccountInitiationWithUpdatedPayload(AccountsRequestPayloads.initiationPayloadEmptyJsonPayload)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
                "consent_default")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
                .contains("Invalid request payload"))
    }

    /*
     * Test the initiation flow with empty string as the  payload
     * Expected response code is 400 and OBIE Error Code of UK.OBIE.Resource.InvalidFormat
     */
    @Test
    void "Accounts Initiation With Empty String Payload"() {

        consentResponse = doDefaultAccountInitiationWithUpdatedPayload(AccountsRequestPayloads.initiationPayloadWithEmptyString)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
                "consent_default")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
                .contains("Payload is not in the correct format"))
    }

    @Test
    void "Initiation Request With same TransactionFromDate and TransactionToDate"() {

        consentResponse = doDefaultAccountInitiationWithUpdatedPayload(AccountsRequestPayloads.initiationPayloadWithSameTransactionFromToDates)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_201)
    }

    @Test
    void "Accounts Initiation with permissions not in array format"() {

        doDefaultAccountInitiationWithUpdatedPayload(AccountsRequestPayloads.initiationPayloadWithoutArrayFormat)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION).contains(
                "Permissions are not in correct format"))
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG).contains(
                "consent_default"))
    }

    @Test(dataProvider = "ValidAccountsPermissionsForInitiation", dataProviderClass = AccountsDataProviders.class)
    void "Accounts Initiation With valid combined permissions"(permissions) {

        setInitiationPayload(AccountsRequestPayloads.getUpdatedInitiationPayload(permissions))
        doDefaultInitiation()

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
    }
}
