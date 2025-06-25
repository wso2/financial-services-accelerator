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

package org.wso2.financial.services.accelerator.gateway.test.accounts.Consent_Revocation_Tests

import io.restassured.http.ContentType
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.AccountsRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

/**
 * Consent Revocation Request Header Validation Tests
 */
class DeleteConsentRequestHeaderValidationTests extends FSAPIMConnectorTest {

    void initialization() {
        consentPath = ConnectorTestConstants.AISP_CONSENT_PATH
        initiationPayload = AccountsRequestPayloads.initiationPayload
        scopeList = ConsentMgtTestUtils.getApiScopesForConsentType(ConnectorTestConstants.ACCOUNTS_TYPE)

        //Get application access token
        applicationAccessToken = getApplicationAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), scopeList)
    }

    @Test
    void "Delete Consent With Authorization Code Type Access Token"() {

        initialization()
        doDefaultAccountInitiation()
        doAccountConsentAuthorisation()

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${userAccessToken}")
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .delete(consentPath + "/" + consentId)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_0_DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Incorrect Access Token Type provided"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_0_CODE),
                "200001")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_0_MSG),
                "Access failure for API: grant type validation failed.")
    }

    @Test
    void "Delete Consent With Client Credentials Type Access Token"() {

        initialization()
        doDefaultAccountInitiation()
        doAccountConsentAuthorisation()

        consentResponse = doAccountConsentRevocation(consentId)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_204)
    }

    @Test
    void "Delete Consent With Access Token Not Bounding To Accounts Scope"() {

        initialization()
        List<ConnectorTestConstants.ApiScope> scopes = ConsentMgtTestUtils.getApiScopesForConsentType(
                ConnectorTestConstants.PAYMENTS_TYPE)
        String accessToken = getApplicationAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), scopes)

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${accessToken}")
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .delete(consentPath + "/" + consentId)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_403)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("User is NOT authorized to access the Resource: " +
                "/account-access-consents/{ConsentId}. Scope validation failed."))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.CODE),
                "900910")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.MESSAGE),
                "The access token does not allow you to access the requested resource")
    }

    @Test
    void "Delete Consent With Invalid Authorization Header"() {

        initialization()
        doDefaultAccountInitiation()

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer invalid_token")
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .delete(consentPath + "/" + consentId)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Access failure for API: /open-banking/v3.1/aisp, version:" +
                " v3.1 status: (900901)"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.CODE),
                "900901")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.MESSAGE),
                "Invalid Credentials")
    }

    @Test
    void "OB-1723_Verify Consent Revoke for valid consent without Authorization Header"() {

        initialization()

        //Consent Initiation
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Consent Authorisation
        doAccountConsentAuthorisation()

        //Consent Revocation
        consentRevocationResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .delete(consentPath + "/${consentId}")

        Assert.assertEquals(consentRevocationResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_401)
        Assert.assertTrue(consentRevocationResponse.jsonPath().get("message").toString()
                .contains(ConnectorTestConstants.MISSING_CREDENTIALS))
    }

    //TODO: Issue https://github.com/wso2/financial-services-accelerator/issues/709
    @Test
    void "OB-1725_Verify Consent Revoke for valid consent with Incorrect Consent ID"() {

        initialization()

        //Consent Initiation
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Consent Authorisation
        doAccountConsentAuthorisation()

        //Consent Revocation
        String incorrectConsentID = "2171e7f0-641c-4f4e-9a9d-cfbbdd02b85b99"

        consentRevocationResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .baseUri(configuration.getServerBaseURL())
                .delete(consentPath + "/${incorrectConsentID}")

        Assert.assertEquals(consentRevocationResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
                ConnectorTestConstants.CONSENT_ID_INVALID_ERROR)
    }

    @Test
    void "OB-1726_Verify Consent Revoke for valid consent with Incorrect Consent Path"() {

        initialization()

        //Consent Initiation
        doDefaultInitiation()
        Assert.assertNotNull(consentId)
        Assert.assertEquals(consentResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_201)

        //Consent Authorisation
        doAccountConsentAuthorisation()

        //Consent Revocation
        consentRevocationResponse = FSRestAsRequestBuilder.buildRequest()
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .contentType(ContentType.JSON)
                .accept(ConnectorTestConstants.CONTENT_TYPE_JSON)
                .baseUri(configuration.getServerBaseURL())
                .delete(incorrectConsentPath + "/${consentId}")

        Assert.assertEquals(consentRevocationResponse.getStatusCode(), ConnectorTestConstants.STATUS_CODE_404)
        Assert.assertTrue(consentRevocationResponse.jsonPath().get("description").toString()
                .contains(ConnectorTestConstants.API_REQUEST_NOT_FOUND))
    }
}
