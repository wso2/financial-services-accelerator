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

        consentResponse = doDefaultInitiationWithUpdatedPayload(RequestPayloads.initiationPayloadWithoutPermissions)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.MESSAGE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.CODE),
                ConnectorTestConstants.BAD_REQUEST.toString())
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.DESCRIPTION)
                .contains("Schema validation failed in the Request: [Path '/Data'] Object has missing required " +
                        "properties ([\"Permissions\"]), "))
    }

    @Test(dataProvider = "InvalidAccountsPermissionsForInitiation",
            dataProviderClass = AccountsDataProviders.class)
    void "Accounts Initiation With Invalid Permissions"(permissions) {

        setInitiationPayload(RequestPayloads.generateConsentInitiationPayload(permissions))
        doDefaultAccountInitiation()

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.CODE),
                ConnectorTestConstants.STATUS_CODE_400.toString())
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.MESSAGE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.DESCRIPTION)
                .contains("not found in enum (possible values: [\"ReadAccountsBasic\",\"ReadAccountsDetail\"," +
                        "\"ReadBalances\",\"ReadTransactionsDetail\"]), "))
    }

    @Test
    void "Accounts Initiation Without Specifying Expiration Date Time"() {

        consentResponse = doDefaultInitiationWithUpdatedPayload(AccountsRequestPayloads.initiationPayloadWithoutExpirationDate)

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

        consentResponse = doDefaultInitiationWithUpdatedPayload(AccountsRequestPayloads.initiationPayloadWithInvalidExpirationDate)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.CODE),
                ConnectorTestConstants.STATUS_CODE_400.toString())
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.MESSAGE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.DESCRIPTION)
                .contains("invalid against requested date format(s) [yyyy-MM-dd'T'HH:mm:ssZ, " +
                        "yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,12}Z]"))
    }

    @Test
    void "Accounts Initiation With Past Expiration Date Time"() {

        consentResponse = doDefaultInitiationWithUpdatedPayload(AccountsRequestPayloads.initiationPayloadWithPastExpirationDate)

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

        consentResponse = doDefaultInitiationWithUpdatedPayload(AccountsRequestPayloads.initiationPayloadWithInvalidTransactionToDate)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.CODE),
                ConnectorTestConstants.STATUS_CODE_400.toString())
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.MESSAGE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.DESCRIPTION)
                .contains("invalid against requested date format(s) [yyyy-MM-dd'T'HH:mm:ssZ, yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,12}Z], "))
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

        consentResponse = doDefaultInitiationWithUpdatedPayload(AccountsRequestPayloads.initiationPayloadWithInvalidTransactionFromDate)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.CODE),
                ConnectorTestConstants.STATUS_CODE_400.toString())
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.MESSAGE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.DESCRIPTION)
                .contains("invalid against requested date format(s) [yyyy-MM-dd'T'HH:mm:ssZ, yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,12}Z], "))
    }

    @Test
    void "Accounts Initiation With Past TransactionToDateTime than TransactionFromDateTime"() {

        consentResponse = doDefaultInitiationWithUpdatedPayload(AccountsRequestPayloads.initiationPayloadWithPastTransactionToDateTime)

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

        consentResponse = doDefaultInitiationWithUpdatedPayload(AccountsRequestPayloads.initiationPayloadEmptyPayload)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.CODE),
                ConnectorTestConstants.STATUS_CODE_400.toString())
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.MESSAGE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.DESCRIPTION)
                .contains("Schema validation failed in the Request: A request body is required but none found., "))
    }

    /*
     * Test the initiation flow with empty json as the  payload
     * Expected response code is 400 and OBIE Error Code of UK.OBIE.Field.Missing
     */
    @Test
    void "Accounts Initiation With Empty Json Payload"() {

        consentResponse = doDefaultInitiationWithUpdatedPayload(AccountsRequestPayloads.initiationPayloadEmptyJsonPayload)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.CODE),
                ConnectorTestConstants.STATUS_CODE_400.toString())
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.MESSAGE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.DESCRIPTION)
                .contains("Schema validation failed in the Request: Object has missing required properties ([\"Data\",\"Risk\"]), "))
    }

    /*
     * Test the initiation flow with empty string as the  payload
     * Expected response code is 400 and OBIE Error Code of UK.OBIE.Resource.InvalidFormat
     */
    @Test
    void "Accounts Initiation With Empty String Payload"() {

        consentResponse = doDefaultInitiationWithUpdatedPayload(AccountsRequestPayloads.initiationPayloadWithEmptyString)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.CODE),
                ConnectorTestConstants.STATUS_CODE_400.toString())
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.MESSAGE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.DESCRIPTION)
                .contains("Schema validation failed in the Request: Instance type (string) does not match any allowed " +
                        "primitive type (allowed: [\"object\"]), "))
    }

    @Test
    void "Initiation Request With same TransactionFromDate and TransactionToDate"() {

        consentResponse = doDefaultInitiationWithUpdatedPayload(AccountsRequestPayloads.initiationPayloadWithSameTransactionFromToDates)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_201)
    }

    @Test
    void "Accounts Initiation with permissions not in array format"() {

        doDefaultInitiationWithUpdatedPayload(AccountsRequestPayloads.initiationPayloadWithoutArrayFormat)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.CODE),
                ConnectorTestConstants.STATUS_CODE_400.toString())
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.MESSAGE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
        Assert.assertTrue(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.DESCRIPTION)
                .contains("Schema validation failed in the Request: [Path '/Data/Permissions'] Instance type (string) " +
                        "does not match any allowed primitive type (allowed: [\"array\"]), "))
    }

    @Test(dataProvider = "ValidAccountsPermissionsForInitiation", dataProviderClass = AccountsDataProviders.class)
    void "Accounts Initiation With valid combined permissions"(permissions) {

        setInitiationPayload(AccountsRequestPayloads.getUpdatedInitiationPayload(permissions))
        doDefaultInitiation()

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
    }
}
