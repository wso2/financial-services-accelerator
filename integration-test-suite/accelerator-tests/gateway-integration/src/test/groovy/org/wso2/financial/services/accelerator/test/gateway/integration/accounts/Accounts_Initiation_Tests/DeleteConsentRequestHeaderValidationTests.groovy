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
 * Consent Revocation Request Header Validation Tests
 */
class DeleteConsentRequestHeaderValidationTests extends AbstractAccountsFlow {

    @Test
    void "TC0203004_Delete Consent With Authorization Code Type Access Token"() {

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
                .delete(consentPath + "/" + consentId)

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_401)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.ERROR_ERRORS_DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Permission mismatch. Consent does not contain necessary permissions"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_ERRORS_CODE),
                AcceleratorTestConstants.ERROR_CODE_FORBIDDEN)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_ERRORS_MSG),
                "Consent Enforcement Error")
    }

    @Test
    void "TC0203005_Delete Consent With Client Credentials Type Access Token"() {

        doDefaultAccountInitiation()
        doAccountConsentAuthorisation()

        consentResponse = doAccountConsentRevocation(consentId)

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_204)
    }

    @Test
    void "TC0203006_Delete Consent With Access Token Not Bounding To Accounts Scope"() {

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
                .delete(consentPath + "/" + consentId)

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
    void "TC0203016_Delete Consent With Invalid Authorization Header"() {

        doDefaultAccountInitiation()

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "Bearer invalid_token")
                .accept(AcceleratorTestConstants.CONTENT_TYPE_JSON)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .delete(consentPath + "/" + consentId)

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_401)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Access failure for API: /open-banking/v3.1/aisp, version:" +
                " v3.1 status: (900901)"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_CODE),
                "900901")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_MSG),
                "Invalid Credentials")
    }
}
