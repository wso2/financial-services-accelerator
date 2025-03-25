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

package org.wso2.financial.services.accelerator.gateway.test.accounts.Accounts_Initiation_Tests

import io.restassured.http.ContentType
import org.codehaus.groovy.runtime.InvokerInvocationException
import org.testng.Assert
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil
import org.wso2.financial.services.accelerator.gateway.test.accounts.util.AbstractAccountsFlow

import java.nio.file.Paths

/**
 * Accounts Flow Initiation Tests.
 */
class AccountsInitiationRequestHeaderValidationTests extends AbstractAccountsFlow {

    //TODO
//    @Test
    void "Initiation Request With Authorization Code Type Access Token"() {

        if (userAccessToken == null) {
            doDefaultAccountInitiation()
            doAccountConsentAuthorisation()
        }

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID,ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${userAccessToken}")
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .accept(ContentType.JSON)
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        def errorMessage = TestUtil.parseResponseBody(retrievalResponse, ConnectorTestConstants.ERROR_DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Permission mismatch. Consent does not contain necessary permissions"))
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_FORBIDDEN)
        Assert.assertEquals(TestUtil.parseResponseBody(retrievalResponse,ConnectorTestConstants.MESSAGE),
                "Consent Enforcement Error")
    }

    @Test
    void "Initiation Request With Access Token Not Bounding To Accounts Scope"() {

        List<ConnectorTestConstants.ApiScope> scopes = ConsentMgtTestUtils.getApiScopesForConsentType(
                ConnectorTestConstants.PAYMENTS_TYPE)
        String accessToken = getApplicationAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), scopes)

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID,ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${accessToken}")
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .accept(ContentType.JSON)
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_403)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("User is NOT authorized to access the Resource: " +
                "/account-access-consents. Scope validation failed."))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_CODE),
                "900910")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.MESSAGE),
                "The access token does not allow you to access the requested resource")
    }

    @Test
    void "Initiation Request Without Authorization Header"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID,ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .accept(ContentType.JSON)
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Make sure your API invocation call has a header: 'Authorization"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_CODE),
                "900902")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.MESSAGE),
                ConnectorTestConstants.MISSING_CREDENTIALS_ERROR)
    }

    @Test
    void "Initiation Request With Invalid Authorization Header"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID,ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer invalid-header")
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .accept(ContentType.JSON)
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Access failure for API: /open-banking/v3.1/aisp, version: v3.1 status: (900901)"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_CODE),
                "900901")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.MESSAGE),
                ConnectorTestConstants.INVALID_CREDENTIALS_ERROR)
    }

    @Test
    void "Initiation Request Without Content Type"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID,ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .accept(ContentType.JSON)
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_400)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Schema validation failed in the Request: Request Content-Type header" +
                " '[text/plain; charset=ISO-8859-1]' does not match any allowed types. Must be one of: " +
                "[application/json; charset=utf-8]"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_CODE),
                ConnectorTestConstants.ERROR_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.MESSAGE),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
    }

    //TODO
//    @Test
    void "Initiation Request With Invalid Content Type"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.XML)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID,ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .accept(ContentType.JSON)
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)

        def errorMessage = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Request Content-Type header does not match any allowed types"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                "200012")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_MSG),
                "Request Content-Type header does not match any allowed types")
    }

    @Test
    void "Initiation Request Without Specifying Accept Header"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID,ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(),ConnectorTestConstants.STATUS_CODE_201)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DATA_STATUS)
        Assert.assertEquals(errorMessage, "AwaitingAuthorisation")
    }

    @Test
    void "Initiation Request With Invalid Accept Header"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID,ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .accept("application")
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_415)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Permission mismatch. Consent does not contain necessary permissions"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_CODE),
                ConnectorTestConstants.ERROR_CODE_FORBIDDEN)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.MESSAGE),
                ConnectorTestConstants.CONSENT_ENFORCEMENT_ERROR)
    }

    @Test
    void "Initiation Request With Headers with Capital Case"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID_CAPS, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID_CAPS, TestUtil.generateUUID())
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
    }

    @Test(expectedExceptions = InvokerInvocationException.class, expectedExceptionsMessageRegExp = ".*Remote host terminated the handshake.*")
    void "Initiation Request Without TLS certificate"() {

        FSRestAsRequestBuilder.buildBasicRequestWithoutTlsContext()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
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
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
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
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

    }
}
