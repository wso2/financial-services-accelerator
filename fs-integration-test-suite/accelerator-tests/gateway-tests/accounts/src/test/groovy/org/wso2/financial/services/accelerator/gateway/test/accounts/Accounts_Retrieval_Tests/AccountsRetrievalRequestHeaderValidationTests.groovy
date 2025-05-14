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
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil
import org.wso2.financial.services.accelerator.gateway.test.accounts.util.AbstractAccountsFlow
import org.wso2.financial.services.accelerator.gateway.test.accounts.util.AccountConstants
import org.wso2.financial.services.accelerator.gateway.test.accounts.util.AccountsDataProviders

/**
 * Accounts Flow Retrieval Tests.
 */
class AccountsRetrievalRequestHeaderValidationTests extends AbstractAccountsFlow {

    String payload

    //TODO
//    @Test
    void "Accounts Retrieval with client credentials access token"() {

        def retrievalResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .get(accountsPath)

        Assert.assertEquals(retrievalResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_403)
        def errorMessage = TestUtil.parseResponseBody(retrievalResponse, ConnectorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("The claim configured in the system and the claim provided in the " +
                "token do not align"))
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,ConnectorTestConstants.ERROR_CODE),
                "900912")
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,ConnectorTestConstants.MESSAGE),
                "Claim Mismatch")
    }

//    @Test
//    void "Accounts Retrieval with access token bound to a different scope"() {
//
//        List<ConnectorTestConstants.ApiScope> scopeList = ConsentMgtTestUtils.getApiScopesForConsentType(ConnectorTestConstants.PAYMENTS_TYPE)
//        String accessTokenBoundToPaymentScope = getApplicationAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
//                configuration.getAppInfoClientID(), scopeList)
//
//        paymentInitiation(accessTokenBoundToPaymentScope)
//        def paymentConsentId = TestUtil.parseResponseBody(consentResponse, "Data.ConsentId")
//        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, UKConstants.DATA_STATUS),
//                "AwaitingAuthorisation")
//
//        paymentAuthorization(paymentConsentId)
//        def paymentUserAccessToken = UKRequestBuilder.getUserToken(code)
//
//        def retrievalResponse = TestSuite.buildRequest()
//                .header(UKConstants.X_FAPI_FINANCIAL_ID, UKConstants.X_FAPI_FINANCIAL_ID_VALUE)
//                .header(UKConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${paymentUserAccessToken}")
//                .accept(UKConstants.CONTENT_TYPE)
//                .header(UKConstants.CHARSET, UKConstants.CHARSET_TYPE)
//                .get(AccountsConstants.ACCOUNTS_PATH)
//
//        Assert.assertEquals(retrievalResponse.statusCode(), 403)
//    }

    /*
     * This test case calls all the bulk account endpoints and submits without authorization header
     * Asserts for 900902 error code
     * */

    @Test(dataProvider = "AccountsResources", dataProviderClass = AccountsDataProviders.class)
    void "Accounts Retrieval without authorisation header"(String resource) {

        if (userAccessToken == null) {
            doDefaultAccountInitiation()
            doAccountConsentAuthorisation()
        }
        def retrievalResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .get(resource)

        Assert.assertEquals(retrievalResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        def errorMessage = TestUtil.parseResponseBody(retrievalResponse, ConnectorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Invalid Credentials. Make sure your API invocation call has a header:" +
                " 'Authorization"))
        Assert.assertTrue(errorMessage.contains("Bearer ACCESS_TOKEN' or 'Authorization : Basic ACCESS_TOKEN' or " +
                "'ApiKey : API_KEY'"))
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,ConnectorTestConstants.ERROR_CODE),
                "900902")
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,ConnectorTestConstants.MESSAGE),
                "Missing Credentials")
    }

    /*
     * This test case calls all the bulk account endpoints and submits with an invalid authorization header
     * Asserts for 900901 error code with a 401 status code
     * */
    @Test(dataProvider = "AccountsResources", dataProviderClass = AccountsDataProviders.class)
    void "Accounts Retrieval with an invalid authorisation header"(String resource) {

        if (userAccessToken == null) {
            doDefaultAccountInitiation()
            doAccountConsentAuthorisation()
        }
        def retrievalResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer invalid_token")
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .get(resource)

        Assert.assertEquals(retrievalResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        def errorMessage = TestUtil.parseResponseBody(retrievalResponse, ConnectorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Access failure for API: /open-banking/v3.1/aisp, version: v3.1" +
                " status: (900901) - Invalid Credentials. Make sure you have provided the correct security credentials"))
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,ConnectorTestConstants.ERROR_CODE),
                "900901")
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,ConnectorTestConstants.MESSAGE),
                "Invalid Credentials")
    }

    /*
     * This test case calls all the bulk account endpoints and submits without the accept header
     * Asserts for 200 status code
     */
    @Test
    void "Accounts Retrieval without accept header"() {

        if (userAccessToken == null) {
            doDefaultAccountInitiation()
            doAccountConsentAuthorisation()
        }
        def retrievalResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${userAccessToken}")
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .get(AccountConstants.ACCOUNTS_PATH)

        Assert.assertEquals(retrievalResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
    }

    /*
     * This test case calls all the bulk account endpoints and submits with an invalid unsupported accept header
     * Asserts for 406 status code
     */
    @Test
    void "Accounts Retrieval with an unsupported accept header"() {
        if (userAccessToken == null) {
            doDefaultAccountInitiation()
            doAccountConsentAuthorisation()
        }
        def retrievalResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${userAccessToken}")
                .accept("application")
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .get(AccountConstants.ACCOUNTS_PATH)

        def errorMessage = TestUtil.parseResponseBody(retrievalResponse, ConnectorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("The claim configured in the system and the claim provided in the " +
                "token do not align"))
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,ConnectorTestConstants.ERROR_CODE),
                "900912")
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,ConnectorTestConstants.MESSAGE),
                "Claim Mismatch")
    }
}
