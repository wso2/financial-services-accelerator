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

package org.wso2.financial.services.accelerator.integration.test.accounts.Accounts_Retrieval_Tests

import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.constant.AcceleratorTestConstants
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil
import org.wso2.financial.services.accelerator.integration.test.accounts.util.AbstractAccountsFlow
import org.wso2.financial.services.accelerator.integration.test.accounts.util.AccountsDataProviders

/**
 * Accounts Flow Retrieval Tests.
 */
class AccountsRetrievalRequestHeaderValidationTests extends AbstractAccountsFlow {

    String payload
  
    @Test
    void "TC0205005_Accounts Retrieval with client credentials access token"() {

        def retrievalResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .accept(AcceleratorTestConstants.CONTENT_TYPE_JSON)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .get(accountsPath)

        Assert.assertEquals(retrievalResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_403)
        def errorMessage = TestUtil.parseResponseBody(retrievalResponse, AcceleratorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("The claim configured in the system and the claim provided in the " +
                "token do not align"))
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,AcceleratorTestConstants.ERROR_CODE),
                "900912")
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,AcceleratorTestConstants.ERROR_MSG),
                "Claim Mismatch")
    }

    @Test
    void "TC0205006_Accounts Retrieval with access token bound to a different scope"() {

        String accessTokenBoundToPaymentScope = UKRequestBuilder.getApplicationToken(UKConstants.SCOPES.PAYMENTS)

        paymentInitiation(accessTokenBoundToPaymentScope)
        def paymentConsentId = TestUtil.parseResponseBody(consentResponse, "Data.ConsentId")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, UKConstants.DATA_STATUS),
                "AwaitingAuthorisation")

        paymentAuthorization(paymentConsentId)
        def paymentUserAccessToken = UKRequestBuilder.getUserToken(code)

        def retrievalResponse = TestSuite.buildRequest()
                .header(UKConstants.X_FAPI_FINANCIAL_ID, UKConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(UKConstants.AUTHORIZATION_HEADER_KEY, "Bearer ${paymentUserAccessToken}")
                .accept(UKConstants.CONTENT_TYPE)
                .header(UKConstants.CHARSET, UKConstants.CHARSET_TYPE)
                .get(AccountsConstants.ACCOUNTS_PATH)

        Assert.assertEquals(retrievalResponse.statusCode(), 403)
    }

    /*
     * This test case calls all the bulk account endpoints and submits without authorization header
     * Asserts for 900902 error code
     * */

    @Test(dataProvider = "AccountsResources", dataProviderClass = AccountsDataProviders.class)
    void "TC0205011_Accounts Retrieval without authorisation header"(String resource) {

        if (userAccessToken == null) {
            doDefaultAccountInitiation()
            doAccountConsentAuthorisation()
        }
        def retrievalResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .accept(AcceleratorTestConstants.CONTENT_TYPE_JSON)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .get(resource)

        Assert.assertEquals(retrievalResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_401)
        def errorMessage = TestUtil.parseResponseBody(retrievalResponse, AcceleratorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Invalid Credentials. Make sure your API invocation call has a header:" +
                " 'Authorization"))
        Assert.assertTrue(errorMessage.contains("Bearer ACCESS_TOKEN' or 'Authorization : Basic ACCESS_TOKEN' or " +
                "'ApiKey : API_KEY'"))
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,AcceleratorTestConstants.ERROR_CODE),
                "900902")
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,AcceleratorTestConstants.ERROR_MSG),
                "Missing Credentials")
    }

    /*
     * This test case calls all the bulk account endpoints and submits with an invalid authorization header
     * Asserts for 900901 error code with a 401 status code
     * */
    @Test(dataProvider = "AccountsResources", dataProviderClass = AccountsDataProviders.class)
    void "TC0205012_Accounts Retrieval with an invalid authorisation header"(String resource) {

        if (userAccessToken == null) {
            doDefaultAccountInitiation()
            doAccountConsentAuthorisation()
        }
        def retrievalResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "Bearer invalid_token")
                .accept(AcceleratorTestConstants.CONTENT_TYPE_JSON)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .get(resource)

        Assert.assertEquals(retrievalResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_401)
        def errorMessage = TestUtil.parseResponseBody(retrievalResponse, AcceleratorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Access failure for API: /open-banking/v3.1/aisp, version: v3.1" +
                " status: (900901) - Invalid Credentials. Make sure you have provided the correct security credentials"))
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,AcceleratorTestConstants.ERROR_CODE),
                "900901")
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,AcceleratorTestConstants.ERROR_MSG),
                "Invalid Credentials")
    }

    /*
     * This test case calls all the bulk account endpoints and submits without the accept header
     * Asserts for 200 status code
     */
    @Test
    void "TC0205013_Accounts Retrieval without accept header"() {

        if (userAccessToken == null) {
            doDefaultAccountInitiation()
            doAccountConsentAuthorisation()
        }
        def retrievalResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "Bearer ${userAccessToken}")
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .get(AccountConstants.ACCOUNTS_PATH)

        Assert.assertEquals(retrievalResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_200)
    }

    /*
     * This test case calls all the bulk account endpoints and submits with an invalid unsupported accept header
     * Asserts for 406 status code
     */
    @Test
    void "TC0205014_Accounts Retrieval with an unsupported accept header"() {
        if (userAccessToken == null) {
            doDefaultAccountInitiation()
            doAccountConsentAuthorisation()
        }
        def retrievalResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "Bearer ${userAccessToken}")
                .accept("application")
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .get(AccountConstants.ACCOUNTS_PATH)

        def errorMessage = TestUtil.parseResponseBody(retrievalResponse, AcceleratorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("The claim configured in the system and the claim provided in the " +
                "token do not align"))
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,AcceleratorTestConstants.ERROR_CODE),
                "900912")
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,AcceleratorTestConstants.ERROR_MSG),
                "Claim Mismatch")
    }
}
