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

package org.wso2.financial.services.accelerator.test.dcr

import io.restassured.RestAssured
import org.testng.Assert
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.dcr.util.DCRConstants
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.request_builder.ClientRegistrationRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

import java.time.Instant

/**
 * Dynamic Client Registration Update Flow Tests.
 */
class DynamicClientRegistrationUpdateTest extends FSConnectorTest {

    ConfigurationService configuration = new ConfigurationService()
    private String accessToken
    private String clientId
    private String configClientId
    private List<ConnectorTestConstants.ApiScope> scopes = [ConnectorTestConstants.ApiScope.ACCOUNTS]
    private String registrationPath
    String SSA
    private String invalidClientId = "invalid_client_id"
    private String invalidAccessToken = "invalid_token"

    @BeforeClass(alwaysRun = true)
    void setup() {

        configClientId = configuration.getAppInfoClientID(0)
        registrationPath = configuration.getServerBaseURL() + DCRConstants.REGISTRATION_ENDPOINT
        configuration.setTppNumber(1)
        SSA = new File(configuration.getAppDCRSSAPath()).text

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestWithClaims(ClientRegistrationRequestBuilder.getRegularClaims(SSA))
                .when()
                .post(registrationPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")

        accessToken = getApplicationAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD, clientId, scopes)
        Assert.assertNotNull(accessToken)
    }

    @Test
    void "Update client request with an invalid clientId"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForUpdate(accessToken, ClientRegistrationRequestBuilder.getUpdateRegularClaims(SSA))
                .put(registrationPath + "/" + invalidClientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Token is not bound to the client id sent in the request")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "unauthorized_request")
    }

    @Test
    void "Update client request with an invalid access token"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForUpdate(invalidAccessToken, ClientRegistrationRequestBuilder.getUpdateRegularClaims(SSA))
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.DESCRIPTION),
                "Access failure for API: /open-banking/v3.3.0, version: v3.3.0 status: (900901) - Invalid " +
                        "Credentials. Make sure you have provided the correct security credentials")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_CODE),
                "900901")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_MSG),
                "Invalid Credentials")
    }

    @Test
    void "Update client request with a clientId that does not match with the access token"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForUpdate(accessToken, ClientRegistrationRequestBuilder.getUpdateRegularClaims(SSA))
                .put(registrationPath + "/" + configClientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Token is not bound to the client id sent in the request")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "unauthorized_request")
    }

    @Test
    void "Update registration request with a valid request payload not structured as JWS"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildPlainRequest(ClientRegistrationRequestBuilder.getUpdateRegularClaims(SSA))
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Malformed request JWT")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_request")
    }

    @Test
    void "Update registration request with invalid redirectURI"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForUpdate(accessToken,  ClientRegistrationRequestBuilder
                        .getRegularClaimsWithInvalidRedirectURI(SSA, "invalid_redirect_uri"))
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Redirect URIs do not match with the software statement")
    }

    @Test
    void "Update registration request with null value for redirectURI"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForUpdate(accessToken,  ClientRegistrationRequestBuilder
                        .getRegularClaimsWithInvalidRedirectURI(SSA, null))
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter redirectUris cannot be empty")
    }

    @Test
    void "Update registration request without redirectURI"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForUpdate(accessToken,  ClientRegistrationRequestBuilder
                        .getRegularClaimsWithoutRedirectURI(SSA))
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter redirectUris cannot be empty")
    }

    @Test
    void "Update registration request with an invalid token_endpoint_auth_method"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForUpdate(accessToken,  ClientRegistrationRequestBuilder.getRegularClaims(SSA,
                        configuration.getAppDCRSoftwareId(), "invalid"))
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid token endpoint authentication method requested.")
    }

    @Test
    void "Update registration request with null value for token_endpoint_auth_method"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForUpdate(accessToken,  ClientRegistrationRequestBuilder.getRegularClaims(SSA,
                        configuration.getAppDCRSoftwareId(), null))
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid token endpoint authentication method requested.")
    }

    @Test
    void "Update registration request without token_endpoint_auth_method"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForUpdate(accessToken,  ClientRegistrationRequestBuilder
                        .getRegularClaimsWithoutTokenEndpointAuthMethod(SSA))
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter tokenEndpointAuthMethod not found in the request")
    }

    @Test
    void "Update registration request with an invalid grant_type"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForUpdate(accessToken,  ClientRegistrationRequestBuilder
                        .getRegularClaimsWithInvalidGrantTypes(SSA, "invalid_grant_type"))
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid grantTypes provided")
    }

    @Test
    void "Update registration request with null value for grant_type"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForUpdate(accessToken,  ClientRegistrationRequestBuilder
                        .getRegularClaimsWithInvalidGrantTypes(SSA, null))
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter grantTypes cannot be empty")
    }

    @Test
    void "Update registration request without grant_type"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForUpdate(accessToken,  ClientRegistrationRequestBuilder.getRegularClaimsWithoutGrantType(SSA))
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter grantTypes cannot be empty")
    }

    @Test
    void "Update registration request with invalid SSA"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForUpdate(accessToken,  ClientRegistrationRequestBuilder.getRegularClaims("invalid_ssa"))
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_software_statement")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Malformed Software Statement JWT found")
    }

    @Test
    void "Update registration request with unverifiable SSA"() {

        String SELF_SIGNED_SSA = new File(configuration.getAppDCRSelfSignedSSAPath()).text
        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForUpdate(accessToken,  ClientRegistrationRequestBuilder.getRegularClaims(SELF_SIGNED_SSA))
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_request")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid request signature. Signed JWT rejected: Another algorithm expected, or no matching key(s) found")
    }

    @Test
    void "Update registration request with null value for SSA"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForUpdate(accessToken,  ClientRegistrationRequestBuilder.getRegularClaims(null))
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_software_statement")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Malformed Software Statement JWT found")
    }

    @Test
    void "Update registration request without SSA"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForUpdate(accessToken,  ClientRegistrationRequestBuilder.getRegularClaimsWithoutSSA())
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_software_statement")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter software statement cannot be null")
    }

    @Test
    void "Update registration request with an invalid id_token_signed_response_alg"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForUpdate(accessToken,  ClientRegistrationRequestBuilder.getRegularClaims(SSA,
                        configuration.getAppDCRSoftwareId(), ConnectorTestConstants.PKJWT_AUTH_METHOD,
                        Instant.now().toEpochMilli(), ConnectorTestConstants.ALG_PS256,
                        "invalid-algorithm"))
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid signature algorithm requested")
    }

    @Test
    void "Update registration request with null value for id_token_signed_response_alg"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForUpdate(accessToken,  ClientRegistrationRequestBuilder.getRegularClaims(SSA,
                        configuration.getAppDCRSoftwareId(), ConnectorTestConstants.PKJWT_AUTH_METHOD,
                        Instant.now().toEpochMilli(), ConnectorTestConstants.ALG_PS256,
                        null))
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid signature algorithm requested")
    }

    @Test
    void "Update registration request without id_token_signed_response_alg"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForUpdate(accessToken,  ClientRegistrationRequestBuilder
                        .getRegularClaimsWithoutIdTokenSignedResponseAlg(SSA))
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter idTokenSignatureAlgorithm not found in the request")
    }

//    @Test
//    void "TC0101019_Update registration request with an invalid application_type"() {
//
//        def registrationResponse = ClientRegistrationRequestBuilder
//                .buildRegistrationRequestForUpdate(accessToken,  ClientRegistrationRequestBuilder.getClaimsWithInvalidApplication_type())
//                .put(registrationPath + "/" + clientId)
//
//        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
//        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
//                "invalid_client_metadata")
//    }

//    @Test
//    void "TC0101020_Update registration request with null value for application_type"() {
//
//        def registrationResponse = ClientRegistrationRequestBuilder
//                .buildRegistrationRequestForUpdate(accessToken,  ClientRegistrationRequestBuilder.getClaimsWithNullApplication_type())
//                .put(registrationPath + "/" + clientId)
//
//        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
//        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
//                "invalid_client_metadata")
//    }

//    @Test
//    void "TC0101021_Update registration request without application_type"() {
//
//        def registrationResponse = ClientRegistrationRequestBuilder
//                .buildRegistrationRequestForUpdate(accessToken,  ClientRegistrationRequestBuilder.getClaimsWithoutApplication_type())
//                .put(registrationPath + "/" + clientId)
//
//        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
//        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
//                "invalid_client_metadata")
//    }

    @Test
    void "Update registration request with an invalid request_object_signing_alg"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForUpdate(accessToken,  ClientRegistrationRequestBuilder.getRegularClaims(SSA, configuration.getAppDCRSoftwareId(),
                        ConnectorTestConstants.PKJWT_AUTH_METHOD, Instant.now().toEpochMilli(),
                        ConnectorTestConstants.ALG_PS256, ConnectorTestConstants.ALG_PS256, "invalid_algorithm"))
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid signature algorithm requested")
    }

    @Test
    void "Update registration request with null value for request_object_signing_alg"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForUpdate(accessToken,  ClientRegistrationRequestBuilder.getRegularClaims(SSA,
                        configuration.getAppDCRSoftwareId(), ConnectorTestConstants.PKJWT_AUTH_METHOD,
                        Instant.now().toEpochMilli(), ConnectorTestConstants.ALG_PS256,
                        ConnectorTestConstants.ALG_PS256, null))
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid signature algorithm requested")
    }

    @Test
    void "Update registration request without request_object_signing_alg"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForUpdate(accessToken, ClientRegistrationRequestBuilder
                        .getRegularClaimsWithoutRequestObjSigningAlg(SSA))
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter requestObjectSignatureAlgorithm not found in the request")
    }

    @Test
    void "Update registration request with invalid iss claim"() {

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForUpdate(accessToken,  ClientRegistrationRequestBuilder.getRegularClaims(SSA,
                        "invalid_iss"))
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid issuer, issuer should be the same as the software id")
    }

    @Test
    void "Update registration request with an invalid token_endpoint_auth_signing_alg" (){

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForUpdate(accessToken,  ClientRegistrationRequestBuilder.getRegularClaims(SSA,
                        configuration.getAppDCRSoftwareId(), ConnectorTestConstants.PKJWT_AUTH_METHOD,
                        Instant.now().toEpochMilli(), "invalid_token_endpoint_auth_signing_alg"))
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid signature algorithm requested")
    }

    @Test
    void "Update registration request with an null token_endpoint_auth_signing_alg" (){

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForUpdate(accessToken,  ClientRegistrationRequestBuilder
                        .getRegularClaims(SSA, configuration.getAppDCRSoftwareId(),
                                ConnectorTestConstants.PKJWT_AUTH_METHOD, Instant.now().toEpochMilli(),
                                null))
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid signature algorithm requested")
    }

//    @Test
//    void "TC0101042_Update registration request with a replayed JTI value in JWT request"() {
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
//                .buildRegistrationRequestForUpdate(accessToken,  ClientRegistrationRequestBuilder.getRegularClaimsWithGivenJti(jti))
//                .put(registrationPath + "/" + clientId)
//
//        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.CREATED)
//        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")
//
//        accessToken = UKRequestBuilder.getApplicationToken(scopes, clientId)
//        Assert.assertNotNull(accessToken)
//
//        //creating an application with the same jti
//        registrationResponse = ClientRegistrationRequestBuilder
//                .buildRegistrationRequestForUpdate(accessToken,  ClientRegistrationRequestBuilder.getRegularClaimsWithGivenJti(jti))
//                .put(registrationPath + "/" + clientId)
//
//        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
//        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
//                "invalid_client_metadata")
//        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, "error_description"),
//                "JTI value of the registration request has been replayed")
//    }

    @AfterClass(alwaysRun = true)
    void tearDown() {
        def registrationResponse = ClientRegistrationRequestBuilder
                .buildRegistrationRequestForGetAndDelete(accessToken)
                .delete(registrationPath + "/" + clientId)
        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_204)
    }
}
