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

package org.wso2.financial.services.accelerator.gateway.test.dcr

import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.gateway.test.dcr.util.DCRConstants
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.request_builder.ClientRegistrationRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

import java.time.Instant

/**
 * Dynamic Client Registration Create Flow Tests.
 */
class DynamicClientRegistrationCreateTest extends FSConnectorTest {

    ConfigurationService configuration = new ConfigurationService()
    private String registrationPath
    String SSA

    @BeforeClass(alwaysRun = true)
    void setup() {

        registrationPath = configuration.getServerBaseURL() + DCRConstants.REGISTRATION_ENDPOINT
        configuration.setTppNumber(1)
        SSA = new File(configuration.getAppDCRSSAPath()).text
    }

    @Test
    void "Invoke registration request with a valid request payload not structured as JWS"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildPlainRequest(ClientRegistrationRequestBuilder.getRegularClaims(SSA))
                .post(registrationPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Malformed request JWT")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_request")
    }

    @Test
    void "Invoke registration request with invalid redirectURI"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder
                        .getRegularClaimsWithInvalidRedirectURI(SSA, "invalid_redirect_uri"))
                .post(registrationPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Redirect URIs do not match with the software statement")
    }

    @Test
    void "Invoke registration request with null value for redirectURI"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder
                        .getRegularClaimsWithInvalidRedirectURI(SSA, null))
                .post(registrationPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter redirectUris cannot be empty")
    }

    @Test
    void "Invoke registration request without redirectURI"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder
                        .getRegularClaimsWithoutRedirectURI(SSA))
                .post(registrationPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter redirectUris cannot be empty")
    }

    @Test
    void "Invoke registration request with an invalid token_endpoint_auth_method"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder.getRegularClaims(SSA,
                        configuration.getAppDCRSoftwareId(), "invalid"))
                .post(registrationPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid token endpoint authentication method requested.")
    }

    @Test
    void "Invoke registration request with null value for token_endpoint_auth_method"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder.getRegularClaims(SSA,
                        configuration.getAppDCRSoftwareId(), null))
                .post(registrationPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid token endpoint authentication method requested.")
    }

    @Test
    void "Invoke registration request without token_endpoint_auth_method"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder
                        .getRegularClaimsWithoutTokenEndpointAuthMethod(SSA))
                .post(registrationPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter tokenEndpointAuthMethod not found in the request")
    }

    @Test
    void "Invoke registration request with an invalid grant_type"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder
                        .getRegularClaimsWithInvalidGrantTypes(SSA, "invalid_grant_type"))
                .post(registrationPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid grantTypes provided")
    }

    @Test
    void "Invoke registration request with null value for grant_type"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder
                        .getRegularClaimsWithInvalidGrantTypes(SSA, null))
                .post(registrationPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter grantTypes cannot be empty")
    }

    @Test
    void "Invoke registration request without grant_type"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder.getRegularClaimsWithoutGrantType(SSA))
                .post(registrationPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter grantTypes cannot be empty")
    }

    @Test
    void "Invoke registration request with invalid SSA"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder.getRegularClaims("invalid_ssa"))
                .post(registrationPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_software_statement")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Malformed Software Statement JWT found")
    }

    @Test
    void "Invoke registration request with unverifiable SSA"() {

        String SELF_SIGNED_SSA = new File(configuration.getAppDCRSelfSignedSSAPath()).text
        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder.getRegularClaims(SELF_SIGNED_SSA))
                .post(registrationPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_request")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid request signature. Signed JWT rejected: Another algorithm expected, or no matching key(s) found")
    }

    @Test
    void "Invoke registration request with null value for SSA"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder.getRegularClaims(null))
                .post(registrationPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_software_statement")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Malformed Software Statement JWT found")
    }

    @Test
    void "Invoke registration request without SSA"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder.getRegularClaimsWithoutSSA())
                .post(registrationPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_software_statement")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter software statement cannot be null")
    }

    @Test
    void "Invoke registration request with an invalid id_token_signed_response_alg"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder.getRegularClaims(SSA,
                        configuration.getAppDCRSoftwareId(), ConnectorTestConstants.PKJWT_AUTH_METHOD,
                        Instant.now().toEpochMilli(), ConnectorTestConstants.ALG_PS256,
                        "invalid-algorithm"))
                .post(registrationPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid signature algorithm requested")
    }

    @Test
    void "Invoke registration request with null value for id_token_signed_response_alg"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder.getRegularClaims(SSA,
                        configuration.getAppDCRSoftwareId(), ConnectorTestConstants.PKJWT_AUTH_METHOD,
                        Instant.now().toEpochMilli(), ConnectorTestConstants.ALG_PS256,
                        null))
                .post(registrationPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid signature algorithm requested")
    }

    @Test
    void "Invoke registration request without id_token_signed_response_alg"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder
                        .getRegularClaimsWithoutIdTokenSignedResponseAlg(SSA))
                .post(registrationPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter idTokenSignatureAlgorithm not found in the request")
    }

//    @Test
//    void "TC0101019_Invoke registration request with an invalid application_type"() {
//
//        def registrationResponse = ClientRegistrationRequestBuilder
//                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder.getClaimsWithInvalidApplication_type())
//                .post(registrationPath)
//
//        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
//        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
//                "invalid_client_metadata")
//    }

//    @Test
//    void "TC0101020_Invoke registration request with null value for application_type"() {
//
//        def registrationResponse = ClientRegistrationRequestBuilder
//                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder.getClaimsWithNullApplication_type())
//                .post(registrationPath)
//
//        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
//        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
//                "invalid_client_metadata")
//    }

//    @Test
//    void "TC0101021_Invoke registration request without application_type"() {
//
//        def registrationResponse = ClientRegistrationRequestBuilder
//                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder.getClaimsWithoutApplication_type())
//                .post(registrationPath)
//
//        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
//        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
//                "invalid_client_metadata")
//    }

    @Test
    void "Invoke registration request with an invalid request_object_signing_alg"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder.getRegularClaims(SSA, configuration.getAppDCRSoftwareId(),
                        ConnectorTestConstants.PKJWT_AUTH_METHOD, Instant.now().toEpochMilli(),
                        ConnectorTestConstants.ALG_PS256, ConnectorTestConstants.ALG_PS256, "invalid_algorithm"))
                .post(registrationPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid signature algorithm requested")
    }

    @Test
    void "Invoke registration request with null value for request_object_signing_alg"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder.getRegularClaims(SSA,
                        configuration.getAppDCRSoftwareId(), ConnectorTestConstants.PKJWT_AUTH_METHOD,
                        Instant.now().toEpochMilli(), ConnectorTestConstants.ALG_PS256,
                        ConnectorTestConstants.ALG_PS256, null))
                .post(registrationPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid signature algorithm requested")
    }

    @Test
    void "Invoke registration request without request_object_signing_alg"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder
                        .getRegularClaimsWithoutRequestObjSigningAlg(SSA))
                .post(registrationPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter requestObjectSignatureAlgorithm not found in the request")
    }

    @Test
    void "Invoke registration request with invalid iss claim"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder.getRegularClaims(SSA,
                        "invalid_iss"))
                .post(registrationPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid issuer, issuer should be the same as the software id")
    }

    @Test
    void "Invoke registration request with an invalid token_endpoint_auth_signing_alg" (){

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder.getRegularClaims(SSA,
                        configuration.getAppDCRSoftwareId(), ConnectorTestConstants.PKJWT_AUTH_METHOD,
                        Instant.now().toEpochMilli(), "invalid_token_endpoint_auth_signing_alg"))
                .post(registrationPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid signature algorithm requested")
    }

    @Test
    void "Invoke registration request with an null token_endpoint_auth_signing_alg" (){

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder
                        .getRegularClaims(SSA, configuration.getAppDCRSoftwareId(),
                        ConnectorTestConstants.PKJWT_AUTH_METHOD, Instant.now().toEpochMilli(),
                        null))
                .post(registrationPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid signature algorithm requested")
    }

//    @Test
//    void "TC0101042_Invoke registration request with a replayed JTI value in JWT request"() {
//
//        String jti = String.valueOf(System.currentTimeMillis())
//
//        def appAccessToken = UKRequestBuilder.getApplicationToken(scopes, clientId)
//        //delete the created application
//        ClientRegistrationRequestBuilder.buildBasicRequest(appAccessToken, false)
//                .delete(registrationPath + clientId)
//
//        //create an application with given jti
//        def registrationResponse = ClientRegistrationRequestBuilder
//                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder.getRegularClaimsWithGivenJti(jti))
//                .post(registrationPath)
//
//        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.CREATED)
//        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")
//
//        accessToken = UKRequestBuilder.getApplicationToken(scopes, clientId)
//        Assert.assertNotNull(accessToken)
//
//        //creating an application with the same jti
//        registrationResponse = ClientRegistrationRequestBuilder
//                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder.getRegularClaimsWithGivenJti(jti))
//                .post(registrationPath)
//
//        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
//        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
//                "invalid_client_metadata")
//        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, "error_description"),
//                "JTI value of the registration request has been replayed")
//    }
}
