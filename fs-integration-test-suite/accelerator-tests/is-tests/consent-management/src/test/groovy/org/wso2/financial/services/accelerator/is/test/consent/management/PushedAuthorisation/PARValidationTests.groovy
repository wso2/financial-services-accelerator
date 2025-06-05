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

package org.wso2.financial.services.accelerator.is.test.consent.management.PushedAuthorisation

import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.SignedJWT
import io.restassured.RestAssured
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.bfsi.test.framework.automation.NavigationAutomationStep
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.automation.consent.BasicAuthAutomationStep
import org.wso2.financial.services.accelerator.test.framework.constant.AccountsRequestPayloads
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.request_builder.AuthorisationBuilder
import org.wso2.financial.services.accelerator.test.framework.request_builder.JWTGenerator
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil
import javax.net.ssl.SSLHandshakeException
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * This class contains the test cases for PAR validation.
 */
class PARValidationTests extends FSConnectorTest{

    String basicHeader

    @BeforeClass
    void init() {
        consentPath = ConnectorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = AccountsRequestPayloads.initiationPayload
        basicHeader = getBasicAuthHeader(configuration.getUserKeyManagerAdminName(),
                configuration.getUserKeyManagerAdminPWD())
        generator = new JWTGenerator()
    }

    @Test (groups = "SmokeTest")
    void "Initiate authorisation request using PAR"() {

        def clientId = configuration.getAppInfoClientID()

        //Consent Initiation
        doDefaultInitiation()

        //Send PAR request
        def response = doPushAuthorisationRequest(consentScopes, consentId,
                clientId, configuration.getCommonSigningAlgorithm())
        def requestUri = TestUtil.parseResponseBody(response, ConnectorTestConstants.REQUEST_URI)

        Assert.assertEquals(response.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        Assert.assertNotNull(requestUri)
        Assert.assertNotNull(TestUtil.parseResponseBody(response, ConnectorTestConstants.RESPONSE_EXPIRES_IN))

        //Authorise the consent
        doConsentAuthorisationViaRequestUri(requestUri.toURI(), clientId)
        Assert.assertNotNull(code)

        //Generate User Access Token
        Response tokenResponse = getUserAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD, clientId,
                code.toString(), consentScopes, true)
        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")

        HashMap<String, String> mapPayload = TestUtil.getJwtTokenPayload(accessToken)
        Assert.assertEquals(mapPayload.get("consent_id"), consentId)
    }

    @Test
    void "Initiate authorisation request using PAR without MTLS security"() {

        try {
            RestAssured.given()

            def clientId = configuration.getAppInfoClientID()

            //Consent Initiation
            doDefaultInitiation()

            //Send PAR request
            generator = new JWTGenerator()

            //Generate Client Assertion
            String assertionString = generator.getClientAssertionJwt(clientId)
            String scopes = consentScopes.collect { it.scopeString }.join(' ')

            //Generate Request Object Claim
            JWT getRequestObjectClaim = generator.getRequestObjectClaim(scopes, consentId,
                    configuration.getAppDCRRedirectUri(), clientId, ConnectorTestConstants.RESPONSE_TYPE_CODE_ID_TOKEN,
                    true, UUID.randomUUID().toString())

            def bodyContent = [
                    (ConnectorTestConstants.CLIENT_ID_KEY)            : (clientId),
                    (ConnectorTestConstants.CLIENT_ASSERTION_TYPE_KEY): (ConnectorTestConstants.CLIENT_ASSERTION_TYPE),
                    (ConnectorTestConstants.CLIENT_ASSERTION_KEY)     : assertionString,
                    (ConnectorTestConstants.REQUEST_KEY)         : getRequestObjectClaim.getParsedString()
            ]

            def parResponse = FSRestAsRequestBuilder.buildBasicRequest()
                    .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                    .formParams(bodyContent)
                    .baseUri(configuration.getISServerUrl())
                    .post(ConnectorTestConstants.PAR_ENDPOINT)

        } catch (SSLHandshakeException e) {
            Assert.assertTrue(e.getMessage().contains("PKIX path building failed: " +
                    "sun.security.provider.certpath.SunCertPathBuilderException: " +
                    "unable to find valid certification path to requested target"))
        }
    }

    @Test
    void "Initiate consent authorisation flow with expired request uri"() {

        def clientId = configuration.getAppInfoClientID()

        //Consent Initiation
        doDefaultInitiation()

        //Send PAR request
        def response = doPushAuthorisationRequest(consentScopes, consentId,
                clientId, configuration.getCommonSigningAlgorithm())
        def requestUri = TestUtil.parseResponseBody(response, ConnectorTestConstants.REQUEST_URI)

        Assert.assertEquals(response.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        Assert.assertNotNull(requestUri)
        Assert.assertNotNull(TestUtil.parseResponseBody(response, ConnectorTestConstants.RESPONSE_EXPIRES_IN))

        println "\nWaiting for request uri to expire..."
        sleep(65000)

        //Authorise the consent
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()

        authoriseUrl = acceleratorAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                    .toURI().toString()

        automation = getBrowserAutomation(ConnectorTestConstants.DEFAULT_DELAY)
                .addStep(new NavigationAutomationStep(authoriseUrl, 10))
                .execute()

        def url = automation.currentUrl.get()
        Assert.assertTrue(TestUtil.getErrorDescriptionFromUrl(url).contains("par.request.uri.expired"))
    }

    @Test
    void "Unable to initiate authorisation if the redirect uri mismatch with the application redirect uri"() {

        def clientId = configuration.getAppInfoClientID()
        def incorrectRedirectUrl = "https://www.google.com"

        //Consent Initiation
        doDefaultInitiation()

        //Send PAR request
        def response = doPushAuthorisationRequest(consentScopes, consentId,
                clientId, configuration.getCommonSigningAlgorithm(), incorrectRedirectUrl)

        Assert.assertEquals(response.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, ConnectorTestConstants.ERROR_DESCRIPTION),
        "callback.not.match")
    }

    @Test
    void "Reject consent authorisation flow when the consent_id define is not related to the authenticated user"() {

        def invalidConsentId = "db638818-be86-42fc-bdb8-1e2a1011866d"
        def clientId = configuration.getAppInfoClientID()

        //Send PAR request
        def response = doPushAuthorisationRequest(consentScopes, invalidConsentId,
                clientId, configuration.getCommonSigningAlgorithm())
        def requestUri = TestUtil.parseResponseBody(response, ConnectorTestConstants.REQUEST_URI)

        Assert.assertEquals(response.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        Assert.assertNotNull(requestUri)

        //Authorise the consent
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()

        authoriseUrl = acceleratorAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                    .toURI().toString()

        automation = getBrowserAutomation(ConnectorTestConstants.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(authoriseUrl))
                .execute()

        // Get Code From URL
        def errorMessage = URLDecoder.decode(TestUtil.getStatusMsgFromUrl(automation.currentUrl.get()),
                StandardCharsets.UTF_8)
        Assert.assertTrue(errorMessage.contains("Error while parsing the response"))
    }

    @Test
    void "Reject consent authorisation flow when the consent_id is unrecognized by the Data Holder"() {

        def invalidConsentId = "abcd1234"

        def clientId = configuration.getAppInfoClientID()

        //Send PAR request
        def response = doPushAuthorisationRequest(consentScopes, invalidConsentId,
                clientId, configuration.getCommonSigningAlgorithm())
        def requestUri = TestUtil.parseResponseBody(response, ConnectorTestConstants.REQUEST_URI)

        Assert.assertEquals(response.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        Assert.assertNotNull(requestUri)
        Assert.assertNotNull(TestUtil.parseResponseBody(response, ConnectorTestConstants.RESPONSE_EXPIRES_IN))
    }

    @Test
    void "Unable to extract request uri if the client id mismatch with the application client id"() {

        def incorrectClientId = "YwSmCUteklf0T3MJdW8IQeM1kLga"

        //Consent Initiation
        doDefaultInitiation()

        //Send PAR request
        def response = doPushAuthorisationRequest(consentScopes, consentId,
                incorrectClientId, configuration.getCommonSigningAlgorithm())

        Assert.assertEquals(response.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertTrue(TestUtil.parseResponseBody(response, ConnectorTestConstants.ERROR_DESCRIPTION).contains(
                "Error while retrieving app information for client_id"))
    }

    @Test
    void "Unable to initiate authorisation if the scope not available"() {

        def clientId = configuration.getAppInfoClientID()

        //Consent Initiation
        doDefaultInitiation()

        //Send PAR request
        def response = doPushAuthorisationRequest(null, consentId,
                clientId, configuration.getCommonSigningAlgorithm())

        Assert.assertEquals(response.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Request with 'client_id' = '$clientId' has 'response_type' for 'hybrid flow'; " +
                        "but 'openid' scope not found.")
    }

    @Test
    void "Consent authorisation request by passing previously used request_uri"() {

        def clientId = configuration.getAppInfoClientID()

        //Consent Initiation
        doDefaultInitiation()

        //Send PAR request
        def response = doPushAuthorisationRequest(consentScopes, consentId,
                clientId, configuration.getCommonSigningAlgorithm())
        def requestUri = TestUtil.parseResponseBody(response, ConnectorTestConstants.REQUEST_URI)

        Assert.assertEquals(response.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        Assert.assertNotNull(requestUri)
        Assert.assertNotNull(TestUtil.parseResponseBody(response, ConnectorTestConstants.RESPONSE_EXPIRES_IN))

        //Authorise the consent
        doConsentAuthorisationViaRequestUri(requestUri.toURI(), clientId)
        Assert.assertNotNull(code)

        //Authorise Consent Using same request_uri
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()
        authoriseUrl = acceleratorAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                .toURI().toString()

        automation = getBrowserAutomation(ConnectorTestConstants.DEFAULT_DELAY)
                .addStep(new NavigationAutomationStep(authoriseUrl, 10))
                .execute()

        String url = automation.currentUrl.get()
        Assert.assertTrue(TestUtil.getErrorDescriptionFromUrl(url).contains("par.invalid.request.uri"))
    }

    @Test
    void "Validate PAR Request when Null consent-id sent in the request"() {

        def clientId = configuration.getAppInfoClientID()

        //Consent Initiation
        doDefaultInitiation()

        //Send PAR request
        def response = doPushAuthorisationRequest(consentScopes, null,
                clientId, configuration.getCommonSigningAlgorithm())
        def requestUri = TestUtil.parseResponseBody(response, ConnectorTestConstants.REQUEST_URI)

        Assert.assertEquals(response.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        Assert.assertNotNull(requestUri)

        //Authorise the consent
        AuthorisationBuilder acceleratorAuthorisationBuilder = new AuthorisationBuilder()

        authoriseUrl = acceleratorAuthorisationBuilder.getAuthorizationRequest(requestUri.toURI(), clientId)
                .toURI().toString()

        automation = getBrowserAutomation(ConnectorTestConstants.DEFAULT_DELAY)
                .addStep(new BasicAuthAutomationStep(authoriseUrl))
                .execute()

        // Get Code From URL
        def errorMessage = URLDecoder.decode(TestUtil.getStatusMsgFromUrl(automation.currentUrl.get()),
                StandardCharsets.UTF_8)
        Assert.assertTrue(errorMessage.contains("Error while parsing the response"))
    }

    @Test
    void "PAR Request without client id param in the request body"() {

        def clientId = configuration.getAppInfoClientID()

        //Consent Initiation
        doDefaultInitiation()

        //Send PAR request
        def response = doPushAuthorisationRequestWithoutClientId(consentScopes, consentId,
                clientId, configuration.getCommonSigningAlgorithm())
        def requestUri = TestUtil.parseResponseBody(response, ConnectorTestConstants.REQUEST_URI)

        Assert.assertEquals(response.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        Assert.assertNotNull(requestUri)
    }

    @Test
    void "Initiate push authorisation flow without request object parameter"() {

        def clientId = configuration.getAppInfoClientID()

        //Consent Initiation
        doDefaultInitiation()

        //Send PAR request
        def response = doPushAuthorisationRequestWithoutRequestObject(consentScopes, consentId,
                clientId, configuration.getCommonSigningAlgorithm())
        def requestUri = TestUtil.parseResponseBody(response, ConnectorTestConstants.REQUEST_URI)

        Assert.assertEquals(response.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
    }

    @Test
    void "Initiate authorisation consent flow without 'aud' claim in request object"() {

        def clientId = configuration.getAppInfoClientID()
        String scopes = consentScopes.collect { it.scopeString }.join(' ')

        //Consent Initiation
        doDefaultInitiation()

        String getRequestObjectClaim = generator.getRequestObjectClaimString(scopes, consentId,
                configuration.getAppInfoRedirectURL(), clientId,
                ConnectorTestConstants.RESPONSE_TYPE_CODE_ID_TOKEN.toString(), true,
                UUID.randomUUID().toString())

        String modifiedClaimSet = generator.removeClaimsFromRequestObject(getRequestObjectClaim, "aud")
        String payload = generator.getSignedRequestObject(modifiedClaimSet)
        JWT jwt = SignedJWT.parse(payload)

        def response = doPushAuthorisationRequest(jwt)

        Assert.assertEquals(response.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, ConnectorTestConstants.ERROR_DESCRIPTION),
                ConnectorTestConstants.MISSING_AUD_VALUE)
        Assert.assertEquals(TestUtil.parseResponseBody(response, ConnectorTestConstants.ERROR),
                ConnectorTestConstants.INVALID_REQUEST_OBJECT)
    }

    @Test
    void "Initiate authorisation consent flow without 'iss' claim in request object"() {

        def clientId = configuration.getAppInfoClientID()
        String scopes = consentScopes.collect { it.scopeString }.join(' ')

        //Consent Initiation
        doDefaultInitiation()

        String getRequestObjectClaim = generator.getRequestObjectClaimString(scopes, consentId,
                configuration.getAppInfoRedirectURL(), clientId,
                ConnectorTestConstants.RESPONSE_TYPE_CODE_ID_TOKEN.toString(), true,
                UUID.randomUUID().toString())

        String modifiedClaimSet = generator.removeClaimsFromRequestObject(getRequestObjectClaim, "iss")
        String payload = generator.getSignedRequestObject(modifiedClaimSet)
        JWT jwt = SignedJWT.parse(payload)

        def response = doPushAuthorisationRequest(jwt)

        Assert.assertEquals(response.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, ConnectorTestConstants.ERROR_DESCRIPTION),
                ConnectorTestConstants.MISSING_ISS_VALUE)
        Assert.assertEquals(TestUtil.parseResponseBody(response, ConnectorTestConstants.ERROR),
                ConnectorTestConstants.INVALID_REQUEST_OBJECT)
    }

    @Test
    void "Initiate authorisation consent flow without 'exp' claim in request object"() {

        def clientId = configuration.getAppInfoClientID()
        String scopes = consentScopes.collect { it.scopeString }.join(' ')

        //Consent Initiation
        doDefaultInitiation()

        String getRequestObjectClaim = generator.getRequestObjectClaimString(scopes, consentId,
                configuration.getAppInfoRedirectURL(), clientId,
                ConnectorTestConstants.RESPONSE_TYPE_CODE_ID_TOKEN.toString(), true,
                UUID.randomUUID().toString())

        String modifiedClaimSet = generator.removeClaimsFromRequestObject(getRequestObjectClaim, "exp")
        String payload = generator.getSignedRequestObject(modifiedClaimSet)
        JWT jwt = SignedJWT.parse(payload)

        def response = doPushAuthorisationRequest(jwt)

        Assert.assertEquals(response.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, ConnectorTestConstants.ERROR_DESCRIPTION),
                ConnectorTestConstants.MISSING_EXP_VALUE)
        Assert.assertEquals(TestUtil.parseResponseBody(response, ConnectorTestConstants.ERROR),
                ConnectorTestConstants.INVALID_REQUEST_OBJECT)
    }

    @Test
    void "OB-1244_Initiate authorisation consent flow without 'nbf' claim in request object"() {

        def clientId = configuration.getAppInfoClientID()
        String scopes = consentScopes.collect { it.scopeString }.join(' ')

        //Consent Initiation
        doDefaultInitiation()

        String getRequestObjectClaim = generator.getRequestObjectClaimString(scopes, consentId,
                configuration.getAppInfoRedirectURL(), clientId,
                ConnectorTestConstants.RESPONSE_TYPE_CODE_ID_TOKEN.toString(), true,
                UUID.randomUUID().toString())

        String modifiedClaimSet = generator.removeClaimsFromRequestObject(getRequestObjectClaim, "nbf")
        String payload = generator.getSignedRequestObject(modifiedClaimSet)
        JWT jwt = SignedJWT.parse(payload)

        def response = doPushAuthorisationRequest(jwt)

        Assert.assertEquals(response.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, ConnectorTestConstants.ERROR_DESCRIPTION),
                ConnectorTestConstants.MISSING_NBF_VALUE)
        Assert.assertEquals(TestUtil.parseResponseBody(response, ConnectorTestConstants.ERROR),
                ConnectorTestConstants.INVALID_REQUEST_OBJECT)
    }

    @Test
    void "Initiate authorisation consent flow with expired 'exp' claim in request object"() {

        def clientId = configuration.getAppInfoClientID()
        String scopes = consentScopes.collect { it.scopeString }.join(' ')

        //Consent Initiation
        doDefaultInitiation()

        String getRequestObjectClaim = generator.getRequestObjectClaimString(scopes, consentId,
                configuration.getAppInfoRedirectURL(), clientId,
                ConnectorTestConstants.RESPONSE_TYPE_CODE_ID_TOKEN.toString(), true,
                UUID.randomUUID().toString(), Instant.now().minus(1, ChronoUnit.HOURS))

        String payload = generator.getSignedRequestObject(getRequestObjectClaim)
        JWT jwt = SignedJWT.parse(payload)

        def response = doPushAuthorisationRequest(jwt)

        Assert.assertEquals(response.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, ConnectorTestConstants.ERROR),
                ConnectorTestConstants.INVALID_REQUEST_OBJECT)
    }

    @Test
    void "Initiate authorisation consent flow with 'nbf' claim with a future time"() {

        Instant time = Instant.now().plus(1, ChronoUnit.HOURS)

        def clientId = configuration.getAppInfoClientID()
        String scopes = consentScopes.collect { it.scopeString }.join(' ')

        //Consent Initiation
        doDefaultInitiation()

        String getRequestObjectClaim = generator.getRequestObjectClaimString(scopes, consentId,
                configuration.getAppInfoRedirectURL(), clientId,
                ConnectorTestConstants.RESPONSE_TYPE_CODE_ID_TOKEN.toString(), true,
                UUID.randomUUID().toString(), Instant.now().plus(1, ChronoUnit.HOURS), time)

        String payload = generator.getSignedRequestObject(getRequestObjectClaim)
        JWT jwt = SignedJWT.parse(payload)

        def response = doPushAuthorisationRequest(jwt)

        Assert.assertEquals(response.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, ConnectorTestConstants.ERROR),
                ConnectorTestConstants.INVALID_REQUEST_OBJECT)
    }

    @Test
    void "Initiate push authorisation flow with 'exp' having a lifetime longer than 60 minutes after 'nbf'"() {

        Instant time = Instant.now().plus(2, ChronoUnit.HOURS)

        def clientId = configuration.getAppInfoClientID()
        String scopes = consentScopes.collect { it.scopeString }.join(' ')

        //Consent Initiation
        doDefaultInitiation()

        String getRequestObjectClaim = generator.getRequestObjectClaimString(scopes, consentId,
                configuration.getAppInfoRedirectURL(), clientId,
                ConnectorTestConstants.RESPONSE_TYPE_CODE_ID_TOKEN.toString(), true,
                UUID.randomUUID().toString(), time)

        String payload = generator.getSignedRequestObject(getRequestObjectClaim)
        JWT jwt = SignedJWT.parse(payload)

        def response = doPushAuthorisationRequest(jwt)

        Assert.assertEquals(response.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, ConnectorTestConstants.ERROR_DESCRIPTION),
                ConnectorTestConstants.INVALID_EXPIRY_TIME)
        Assert.assertEquals(TestUtil.parseResponseBody(response, ConnectorTestConstants.ERROR),
                ConnectorTestConstants.INVALID_REQUEST_OBJECT)
    }

    @Test
    void "Send PAR request without redirect URL"() {

        def clientId = configuration.getAppInfoClientID()
        String scopes = consentScopes.collect { it.scopeString }.join(' ')

        //Consent Initiation
        doDefaultInitiation()

        String getRequestObjectClaim = generator.getRequestObjectClaimString(scopes, consentId,
                configuration.getAppInfoRedirectURL(), clientId,
                ConnectorTestConstants.RESPONSE_TYPE_CODE_ID_TOKEN.toString(), true,
                UUID.randomUUID().toString())

        String modifiedClaimSet = generator.removeClaimsFromRequestObject(getRequestObjectClaim, "redirect_uri")
        String payload = generator.getSignedRequestObject(modifiedClaimSet)
        JWT jwt = SignedJWT.parse(payload)

        def response = doPushAuthorisationRequest(jwt)

        Assert.assertEquals(response.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, ConnectorTestConstants.ERROR_DESCRIPTION),
                ConnectorTestConstants.INVALID_REDIRECT_URI_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(response, ConnectorTestConstants.ERROR),
                ConnectorTestConstants.INVALID_REQUEST)
    }

    @Test
    void "PAR call with an invalid audience"() {

        def clientId = configuration.getAppInfoClientID()
        String scopes = consentScopes.collect { it.scopeString }.join(' ')

        //Consent Initiation
        doDefaultInitiation()

        String getRequestObjectClaim = generator.getRequestObjectClaimString(scopes, consentId,
                configuration.getAppInfoRedirectURL(), clientId,
                ConnectorTestConstants.RESPONSE_TYPE_CODE_ID_TOKEN.toString(), true,
                UUID.randomUUID().toString())

        String modifiedClaimSet = generator.addClaimsFromRequestObject(getRequestObjectClaim, "aud", "123")
        String payload = generator.getSignedRequestObject(modifiedClaimSet)
        JWT jwt = SignedJWT.parse(payload)

        def response = doPushAuthorisationRequest(jwt)

        Assert.assertEquals(response.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(response, ConnectorTestConstants.ERROR_DESCRIPTION),
                ConnectorTestConstants.INVALID_PARAM_ERROR)
        Assert.assertEquals(TestUtil.parseResponseBody(response, ConnectorTestConstants.ERROR),
                ConnectorTestConstants.INVALID_REQUEST_OBJECT)
    }
}
