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

import io.restassured.http.ContentType
import org.codehaus.groovy.runtime.InvokerInvocationException
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSAPIMConnectorTest
import org.wso2.financial.services.accelerator.test.framework.constant.AccountsRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.utility.ConsentMgtTestUtils
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

import java.nio.file.Paths

/**
 * Accounts Flow Initiation Tests.
 */
class AccountsInitiationRequestHeaderValidationTests extends FSAPIMConnectorTest {

    @BeforeClass
    void init() {
        consentPath = ConnectorTestConstants.AISP_CONSENT_PATH
        initiationPayload = AccountsRequestPayloads.initiationPayload
        scopeList = ConsentMgtTestUtils.getApiScopesForConsentType(ConnectorTestConstants.ACCOUNTS_TYPE)

        //Get application access token
        applicationAccessToken = getApplicationAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD,
                configuration.getAppInfoClientID(), scopeList)
    }

    @Test
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

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_403)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("The claim configured in the system and the claim provided in the token " +
                "do not align. Please ensure the claims match."))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                "900912")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_MSG),
                "Claim Mismatch")
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
        def errorMessage = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("User is NOT authorized to access the Resource: " +
                "/account-access-consents. Scope validation failed."))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                "900910")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_MSG),
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
        def errorMessage = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Make sure your API invocation call has a header: 'Authorization"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                "900902")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_MSG),
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
        def errorMessage = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Access failure for API: /open-banking/v3.1/aisp, version: v3.1 status: (900901)"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                "900901")
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_MSG),
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
        def errorMessage = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("Schema validation failed in the Request: Request Content-Type " +
                "header '[text/plain; charset=ISO-8859-1]' does not match any allowed types. Must be one of: " +
                "[application/json; charset=utf-8]., "))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_400.toString())
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_MSG),
                ConnectorTestConstants.ERROR_CODE_BAD_REQUEST)
    }

    //TODO:https://github.com/wso2/financial-services-accelerator/issues/681
    @Test
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

    //TODO: https://github.com/wso2-enterprise/financial-open-banking/issues/7965
    @Test
    void "Initiation Request With Invalid Accept Header"() {

        consentResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID,ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .accept(ContentType.XML)
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_406)
        def errorMessage = TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION)
        Assert.assertTrue(errorMessage.contains("HTTP 406 Not Acceptable"))
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.STATUS_CODE_406.toString())
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse,ConnectorTestConstants.ERROR_ERRORS_MSG),
                "consent_default")
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

//    @Test
    void "Initiation Request Without TLS certificate"() {

        def consentResponse = FSRestAsRequestBuilder.buildBasicRequestWithoutTlsContext()
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        //TODO: Enable the assertion after fixing the APIM issue even enabling certificate revocation.
        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
                ConnectorTestConstants.MTLS_ENFORCEMENT_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                ConnectorTestConstants.ERROR_CODE_UNAUTHORIZED)
        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
                "Certificate not found in the request")
    }

//    @Test(expectedExceptions = InvokerInvocationException.class, expectedExceptionsMessageRegExp = ".*Broken pipe.*")
    void "Validate Account Initiation request with expired cert in the context" () {

        String keystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "expired-certs", "signing-keystore", "signing.jks")
        String alias = "tpp4-sig"
        String password = "wso2carbon"

        def consentResponse = FSRestAsRequestBuilder.buildRequest(keystoreLocation, password)
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        //TODO: Enable the assertion after fixing the APIM issue even enabling certificate revocation.
//        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
//        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR),
//                ConnectorTestConstants.INVALID_CLIENT)
//        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
//                "Invalid transport certificate. Certificate passed through the request not valid")
    }

//    @Test(expectedExceptions = InvokerInvocationException.class, expectedExceptionsMessageRegExp = ".*Remote host terminated the handshake.*")
    void "Validate Account Initiation request with revoked cert in the context" () {

        String keystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "revoked-certs", "transport.jks")
        String password = "wso2carbon"

        def consentResponse = FSRestAsRequestBuilder.buildRequest(keystoreLocation, password)
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        //TODO: Enable the assertion after fixing the APIM issue even enabling certificate revocation.
//        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
//        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR),
//                ConnectorTestConstants.INVALID_CLIENT)
//        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
//                "Invalid transport certificate. Certificate passed through the request not valid")

    }
//
//    @Test(expectedExceptions = InvokerInvocationException.class, expectedExceptionsMessageRegExp = ".*Remote host terminated the handshake.*")
    void "Invoke accounts API with certificate not bound to current application" () {

        String keystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "DynamicClientRegistration", "uk", "tpp3", "transport-keystore", "transport.jks")
        String password = "wso2carbon"

        def consentResponse = FSRestAsRequestBuilder.buildRequest(keystoreLocation, password)
                .contentType(ContentType.JSON)
                .header(ConnectorTestConstants.X_FAPI_FINANCIAL_ID, ConnectorTestConstants.X_FAPI_FINANCIAL_ID_VALUE)
                .header(ConnectorTestConstants.CHARSET, ConnectorTestConstants.CHARSET_TYPE)
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "Bearer ${applicationAccessToken}")
                .header(ConnectorTestConstants.X_FAPI_INTERACTION_ID, TestUtil.generateUUID())
                .baseUri(configuration.getServerBaseURL())
                .body(initiationPayload)
                .post(consentPath)

        //TODO: Enable the assertion after fixing the APIM issue even enabling certificate revocation.
//        Assert.assertEquals(consentResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
//        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR),
//                ConnectorTestConstants.INVALID_CLIENT)
//        Assert.assertEquals(TestUtil.parseResponseBody(consentResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
//                "Invalid transport certificate. Certificate passed through the request not valid")

    }
}
