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
import org.codehaus.groovy.runtime.InvokerInvocationException
import org.testng.Assert
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.constant.AcceleratorTestConstants
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil
import org.wso2.financial.services.accelerator.test.gateway.integration.accounts.util.AbstractAccountsFlow

import java.nio.file.Paths

/**
 * Accounts Flow Initiation Tests.
 */
class AccountsInitiationRequestHeaderValidationTests extends AbstractAccountsFlow {

//    @Test
    void "TC0201005_Initiation Request With Authorization Code Type Access Token"() {

        if (userAccessToken == null) {
            doDefaultAccountInitiation()
            doAccountConsentAuthorisation()
        }

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID,AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "Bearer ${userAccessToken}")
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .header(AcceleratorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .accept(ContentType.JSON)
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_401)
        def errorMessage = TestUtil.parseResponseBody(retrievalResponse, AcceleratorTestConstants.ERROR_DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Permission mismatch. Consent does not contain necessary permissions"))
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,AcceleratorTestConstants.ERROR_ERRORS_CODE),
                AcceleratorTestConstants.ERROR_CODE_FORBIDDEN)
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,AcceleratorTestConstants.ERROR_MSG),
                "Consent Enforcement Error")
    }

    @Test
    void "TC0201007_Initiation Request With Access Token Not Bounding To Accounts Scope"() {

        List<AcceleratorTestConstants.ApiScope> scopes = ConsentMgtTestUtils.getApiScopesForConsentType(
                AcceleratorTestConstants.PAYMENTS_TYPE)
        String accessToken = getApplicationAccessToken(AcceleratorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), scopes)

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID,AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "Bearer ${accessToken}")
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .header(AcceleratorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .accept(ContentType.JSON)
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_403)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("User is NOT authorized to access the Resource: " +
                "/account-access-consents. Scope validation failed."))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_CODE),
                "900910")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_MSG),
                "The access token does not allow you to access the requested resource")
    }

    @Test
    void "TC0201013_Initiation Request Without Authorization Header"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID,AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .header(AcceleratorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .accept(ContentType.JSON)
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_401)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Make sure your API invocation call has a header: 'Authorization"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_CODE),
                "900902")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_MSG),
                AcceleratorTestConstants.MISSING_CREDENTIALS_ERROR)
    }

    @Test
    void "TC0201014_Initiation Request With Invalid Authorization Header"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID,AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "Bearer invalid-header")
                .header(AcceleratorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .accept(ContentType.JSON)
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_401)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Access failure for API: /open-banking/v3.1/aisp, version: v3.1 status: (900901)"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_CODE),
                "900901")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_MSG),
                AcceleratorTestConstants.INVALID_CREDENTIALS_ERROR)
    }

    @Test
    void "TC0201015_Initiation Request Without Content Type"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID,AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .header(AcceleratorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .accept(ContentType.JSON)
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(),AcceleratorTestConstants.STATUS_CODE_400)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Schema validation failed in the Request: Request Content-Type header" +
                " '[text/plain; charset=ISO-8859-1]' does not match any allowed types. Must be one of: " +
                "[application/json; charset=utf-8]"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_CODE),
                AcceleratorTestConstants.ERROR_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_MSG),
                AcceleratorTestConstants.ERROR_CODE_BAD_REQUEST)
    }

    @Test
    void "TC0201016_Initiation Request With Invalid Content Type"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.XML)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID,AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .header(AcceleratorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .accept(ContentType.JSON)
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_400)

        def errorMessage = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.ERROR_ERRORS_DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Request Content-Type header does not match any allowed types"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_ERRORS_CODE),
                "200012")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_ERRORS_MSG),
                "Request Content-Type header does not match any allowed types")
    }

    @Test
    void "TC0201017_Initiation Request Without Specifying Accept Header"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID,AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .header(AcceleratorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(),AcceleratorTestConstants.STATUS_CODE_201)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DATA_STATUS)
        Assert.assertEquals(errorMessage, "AwaitingAuthorisation")
    }

    @Test
    void "TC0201018_Initiation Request With Invalid Accept Header"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID,AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .header(AcceleratorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .accept("application")
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_415)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, AcceleratorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Permission mismatch. Consent does not contain necessary permissions"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_CODE),
                AcceleratorTestConstants.ERROR_CODE_FORBIDDEN)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,AcceleratorTestConstants.ERROR_MSG),
                AcceleratorTestConstants.CONSENT_ENFORCEMENT_ERROR)
    }

    @Test
    void "TC0201038_Initiation Request With Headers with Capital Case"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_CAPS, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .header(AcceleratorTestConstants.X_FAPI_INTERACTION_ID_CAPS, TestUtil.generateUUID())
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_201)
    }

    @Test(expectedExceptions = InvokerInvocationException.class, expectedExceptionsMessageRegExp = ".*Remote host terminated the handshake.*")
    void "TC0201038_Initiation Request Without TLS certificate"() {

        FSRestAsRequestBuilder.buildBasicRequestWithoutTlsContext()
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .header(AcceleratorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)
    }

    //TODO: Need to uncomment when the issue from APIM is fixed
//    @Test(expectedExceptions = InvokerInvocationException.class, expectedExceptionsMessageRegExp = ".*Remote host terminated the handshake.*")
    void "Validate Account Initiation request with expired cert in the context" () {

        String keystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "expired-certs", "signing-keystore", "signing.jks")
        String alias = "tpp4-sig"
        String password = "wso2carbon"

       FSRestAsRequestBuilder.buildRequest(keystoreLocation, password)
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .header(AcceleratorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)
    }

    @Test(expectedExceptions = InvokerInvocationException.class, expectedExceptionsMessageRegExp = ".*Remote host terminated the handshake.*")
    void "Validate Account Initiation request with revoked cert in the context" () {

        String keystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "revoked-certs", "transport.jks")
        String password = "wso2carbon"

        FSRestAsRequestBuilder.buildRequest(keystoreLocation, password)
                .contentType(ContentType.JSON)
                .header(AcceleratorTestConstants.X_FAPI_FINANCIAL_ID, AcceleratorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(AcceleratorTestConstants.CHARSET, AcceleratorTestConstants.CHARSET_TYPE)
                .header(AcceleratorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .header(AcceleratorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

    }
}
