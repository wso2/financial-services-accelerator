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

package signature_algorithm_validation

import org.wso2.openbanking.test.framework.constant.OBConstants
import io.restassured.RestAssured
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.constant.RequestPayloads
import org.wso2.financial.services.accelerator.test.framework.request_builder.JWTGenerator
import org.wso2.financial.services.accelerator.test.framework.request_builder.TokenRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

import java.nio.file.Paths

/**
 * Signature Algorithm Validation Test Cases.
 */
class SignatureAlgorithmValidationTest extends FSConnectorTest {

    String clientId
    String idToken
    ConnectorTestConstants.ApiScope scope = ConnectorTestConstants.ApiScope.ACCOUNTS
    private ConfigurationService configuration = new ConfigurationService()

    void authoriseConsent() {

        clientId = configuration.getAppInfoClientID()
        consentPath = ConnectorTestConstants.ACCOUNT_CONSENT_PATH
        initiationPayload = RequestPayloads.initiationPayload
        //Consent initiation
        doDefaultInitiation(initiationPayload)
        Assert.assertNotNull(consentId)

        //Consent Authorisation
        doConsentAuthorisation(configuration.getAppInfoClientID(), true, consentScopes)
        Assert.assertNotNull(code)
    }

    @Test
    void "Validate token request contains client assertion signed with configured signature algorithm"() {

        authoriseConsent()

        Response tokenResponse = getUserAccessTokenResponse(ConnectorTestConstants.PKJWT_AUTH_METHOD, clientId,
                code, consentScopes)

        accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(accessToken)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "expires_in").toString(),
                ConnectorTestConstants.TOKEN_EXPIRY_TIME)
        Assert.assertNotNull(TestUtil.parseResponseBody(tokenResponse, "scope"))
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "token_type"), ConnectorTestConstants.BEARER)
    }

    @Test
    void "Validate token request contains client assertion signed with unsupported signature algorithm"() {

        JWTGenerator acceleratorJWTGenerator = new JWTGenerator()
        acceleratorJWTGenerator.setScopes(consentScopes)
        acceleratorJWTGenerator.setSigningAlgorithm("RS256")

        String jwt = acceleratorJWTGenerator.getUserAccessTokenJwt(ConnectorTestConstants.PKJWT_AUTH_METHOD, clientId, code)

        RestAssured.baseURI = configuration.getISServerUrl()
        Response tokenResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(ConnectorTestConstants.X_WSO2_MUTUAL_CERT, TestUtil.getPublicKeyFromTransportKeyStore())
                .body(jwt)
                .post(OBConstants.TOKEN_ENDPOINT)

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR),
                ConnectorTestConstants.INVALID_CLIENT)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Signature algorithm used in the request is invalid.")
    }

    @Test
    void "Validate token request contains client assertion signed with invalid certificate"() {

        String keystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "berlin-certs","eidas-qwac.jks")
        String alias = "1"
        String password = "wso2carbon"

        def tokenResponse = TokenRequestBuilder.getUserAccessTokenWithDefinedCert(configuration.getCommonSigningAlgorithm(),
                keystoreLocation, password, alias, consentScopes, clientId, code)

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
    }

    @Test
    void "Validate token request contains client assertion signed with expired certificate"() {

        String keystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "expired-certs", "signing-keystore", "signing.jks")
        String alias = "tpp4-sig"
        String password = "wso2carbon"

        def tokenResponse = TokenRequestBuilder.getUserAccessTokenWithDefinedCert(configuration.getCommonSigningAlgorithm(),
                keystoreLocation, password, alias, consentScopes, clientId, code)

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
    }
}
