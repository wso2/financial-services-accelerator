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

import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.constant.AcceleratorTestConstants
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil
import org.wso2.financial.services.accelerator.test.gateway.integration.accounts.util.AbstractAccountsFlow

/**
 * Get Consent Request Header Validation Tests
 */
class GetConsentRequestHeaderValidationTests extends AbstractAccountsFlow {

    @Test
    void "TC0202004_Get Accounts Initiation With Authorization Code Type Access Token"() {

        if (userAccessToken == null) {
            doDefaultAccountInitiation()
            doAccountConsentAuthorisation()
        }
        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "Bearer ${userAccessToken}")
                .accept(AcceleratorTestConstants.CONTENT_TYPE_JSON)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .get(consentPath + "/${consentId}")

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_401)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.ERROR_ERRORS_DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Permission mismatch. Consent does not contain necessary permissions"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_CODE),
                AcceleratorTestConstants.ERROR_CODE_FORBIDDEN)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_MSG),
                "Consent Enforcement Error")
    }

    @Test
    void "TC0202005_Get Accounts Initiation With Client Credentials Type Access Token"() {

        doDefaultAccountInitiation()

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .accept(AcceleratorTestConstants.CONTENT_TYPE_JSON)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .get(consentPath + "/${consentId}")

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_200)
    }

    @Test
    void "TC0202006_Get Accounts Initiation With Access Token Not Bounding To Accounts Scope"() {

        List<AcceleratorTestConstants.ApiScope> scopes = ConsentMgtTestUtils.getApiScopesForConsentType(
                AcceleratorTestConstants.PAYMENTS_TYPE)
        String accessToken = getApplicationAccessToken(AcceleratorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), scopes)

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "Bearer ${accessToken}")
                .accept(AcceleratorTestConstants.CONTENT_TYPE_JSON)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .get(consentPath + "/${consentId}")

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_403)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("User is NOT authorized to access the Resource: " +
                "/account-access-consents/{ConsentId}. Scope validation failed."))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_CODE),
                "900910")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_MSG),
                "The access token does not allow you to access the requested resource")
    }

    @Test
    void "TC0202011_Get Accounts Initiation Without Authorization Header"() {

        doDefaultAccountInitiation()
        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .accept(AcceleratorTestConstants.CONTENT_TYPE_JSON)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .get(consentPath + "/${consentId}")

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_401)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Make sure your API invocation call has a header: 'Authorization :" +
                " Bearer ACCESS_TOKEN' or 'Authorization : Basic ACCESS_TOKEN' or 'ApiKey : API_KEY'"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_CODE),
               "900902")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_MSG),
                "Missing Credentials")
    }

    @Test
    void "TC0202012_Get Accounts Initiation With Invalid Authorization Header"() {

        doDefaultAccountInitiation()
        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "Bearer invalid_header")
                .accept(AcceleratorTestConstants.CONTENT_TYPE_JSON)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .get(consentPath + "/${consentId}")

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_401)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Access failure for API: /open-banking/v3.1/aisp, version" +
                ": v3.1 status: (900901)"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_CODE),
                "900901")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_MSG),
                "Invalid Credentials")
    }

    @Test
    void "TC0202013_Get Accounts Initiation Without Specifying Accept Header"() {

        doDefaultAccountInitiation()
        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .get(consentPath + "/${consentId}")

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_200)
    }

    //TODO: Uncomment after fixing: https://github.com/wso2-enterprise/financial-open-banking/issues/7965
//    @Test
    void "TC0202014_Get Accounts Initiation With Invalid Accept Header"() {

        doDefaultAccountInitiation()
        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .accept("application")
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .get(consentPath + "/${consentId}")

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_406)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Permission mismatch. Consent does not contain necessary permissions"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_CODE),
                AcceleratorTestConstants.ERROR_CODE_FORBIDDEN)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_MSG),
                "Consent Enforcement Error")
    }

    @Test
    void "TC0202015_Get Accounts Initiation With Empty Consent Id"() {

        doDefaultAccountInitiation()

        def consentid = ""

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .accept(AcceleratorTestConstants.CONTENT_TYPE_JSON)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .get(consentPath + "/${consentid}")

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_405)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Method not allowed for given API resource"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_CODE),
                "405")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_MSG),
                "Runtime Error")
    }

    @Test
    void "TC0202016_Get Accounts Initiation Without Consent Id Parameter"() {

        doDefaultAccountInitiation()

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .accept(AcceleratorTestConstants.CONTENT_TYPE_JSON)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .get(consentPath + "/")

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_405)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Method not allowed for given API resource"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_CODE),
                "405")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_MSG),
                "Runtime Error")
    }
}
