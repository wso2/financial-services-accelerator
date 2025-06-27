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

package org.wso2.financial.services.accelerator.gateway.test.accounts.Accounts_Retrieval_Tests

import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.AccountsRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.PaymentRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.utility.AccountsDataProviders
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Accounts Flow Retrieval Tests.
 */
class AccountsRetrievalRequestHeaderValidationTests extends FSAPIMConnectorTest {

    void initialization() {
        consentPath = ConnectorTestConstants.AISP_CONSENT_PATH
        initiationPayload = AccountsRequestPayloads.initiationPayload
        scopeList = ConsentMgtTestUtils.getApiScopesForConsentType(ConnectorTestConstants.ACCOUNTS_TYPE)
        accountsPath = ConnectorTestConstants.ACCOUNTS_PATH

        //Get application access token
        applicationAccessToken = getApplicationAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), scopeList)
    }

    @Test
    void "Accounts Retrieval with client credentials access token"() {

        initialization()

        def retrievalResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .get(accountsPath)

        Assert.assertEquals(retrievalResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_403)
        def errorMessage = TestUtil.parseResponseBody(retrievalResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("The claim configured in the system and the claim provided in the token " +
                "do not align. Please ensure the claims match."))
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                "900912")
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,ConnectorTestConstants.ERROR_ERRORS_MSG),
                "Claim Mismatch")
    }

    @Test
    void "Accounts Retrieval with access token bound to a different scope"() {

        consentPath = ConnectorTestConstants.PAYMENT_CONSENT_API_PATH
        initiationPayload = PaymentRequestPayloads.initiationPaymentPayload
        scopeList = ConsentMgtTestUtils.getApiScopesForConsentType(ConnectorTestConstants.PAYMENTS_TYPE)

        applicationAccessToken = getApplicationAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), scopeList)

        //Payment Consent Initiation
        doDefaultInitiationForPayments(applicationAccessToken, initiationPayload)
        Assert.assertNotNull(consentId)

        //Payment Consent Authorisation
        doPaymentConsentAuthorisation(scopeList)
        Assert.assertNotNull(code)
        Assert.assertNotNull(userAccessToken)

        //Send Account Retrieval Request with Payment Consent Access Token
        accountsPath = ConnectorTestConstants.AISP_PATH + "accounts"

        retrievalResponse = consentRequestBuilder.buildBasicRequest(userAccessToken)
                .baseUri(configuration.getServerBaseURL())
                .get(accountsPath)

        Assert.assertEquals(retrievalResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_403)
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
                "The access token does not allow you to access the requested resource")
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
                "User is NOT authorized to access the Resource: /accounts. Scope validation failed.")
    }

    /*
     * This test case calls all the bulk account endpoints and submits without authorization header
     * Asserts for 900902 error code
     * */

    @Test(dataProvider = "AccountsResources", dataProviderClass = AccountsDataProviders.class)
    void "Accounts Retrieval without authorisation header"(String resource) {

        initialization()

        doDefaultAccountInitiation()
        doAccountConsentAuthorisation()

        def retrievalResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .get(resource)

        Assert.assertEquals(retrievalResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        def errorMessage = TestUtil.parseResponseBody(retrievalResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Invalid Credentials. Make sure your API invocation call has a header:" +
                " 'Authorization"))
        Assert.assertTrue(errorMessage.contains("Bearer ACCESS_TOKEN' or 'Authorization : Basic ACCESS_TOKEN' or " +
                "'ApiKey : API_KEY'"))
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                "900902")
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,ConnectorTestConstants.ERROR_ERRORS_MSG),
                "Missing Credentials")
    }

    /*
     * This test case calls all the bulk account endpoints and submits with an invalid authorization header
     * Asserts for 900901 error code with a 401 status code
     * */
    @Test(dataProvider = "AccountsResources", dataProviderClass = AccountsDataProviders.class)
    void "Accounts Retrieval with an invalid authorisation header"(String resource) {

        initialization()
        doDefaultAccountInitiation()
        doAccountConsentAuthorisation()

        def retrievalResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer invalid_token")
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .get(resource)

        Assert.assertEquals(retrievalResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        Assert.assertTrue(TestUtil.parseResponseBody(retrievalResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
                .contains("Access failure for API: /open-banking/v3.1/aisp, version: v3.1" +
                " status: (900901) - Invalid Credentials. Make sure you have provided the correct security credentials"))
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                "900901")
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,ConnectorTestConstants.ERROR_ERRORS_MSG),
                "Invalid Credentials")
    }

    /*
     * This test case calls all the bulk account endpoints and submits without the accept header
     * Asserts for 200 status code
     */
    @Test
    void "Accounts Retrieval without accept header"() {

        initialization()
        doDefaultAccountInitiation()
        doAccountConsentAuthorisation()

        def retrievalResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${userAccessToken}")
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .get(ConnectorTestConstants.ACCOUNTS_PATH)

        Assert.assertEquals(retrievalResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
    }

    /*
     * This test case calls all the bulk account endpoints and submits with an invalid unsupported accept header
     * Asserts for 406 status code
     */
    @Test
    void "Accounts Retrieval with an unsupported accept header"() {

        initialization()
        doDefaultAccountInitiation()
        doAccountConsentAuthorisation()

        def retrievalResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${userAccessToken}")
                .accept(ContentType.XML)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .get(ConnectorTestConstants.ACCOUNTS_PATH)

        Assert.assertEquals(retrievalResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_406)
    }
}
