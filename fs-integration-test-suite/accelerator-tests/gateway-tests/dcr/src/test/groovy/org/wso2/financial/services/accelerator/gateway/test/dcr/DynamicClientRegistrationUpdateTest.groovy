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
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.gateway.test.dcr.util.DCRConstants
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.request_builder.ClientRegistrationRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.request_builder.JWTGenerator
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
    JWTGenerator jwtGenerator = new JWTGenerator()

    @BeforeClass(alwaysRun = true)
    void setup() {

        configClientId = configuration.getAppInfoClientID(0)
        registrationPath = configuration.getServerBaseURL() + DCRConstants.REGISTRATION_ENDPOINT
        configuration.setTppNumber(1)
        SSA = new File(configuration.getAppDCRSSAPath()).text

        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getRegularClaimsForGateway(SSA))

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .body(payload)
                .post(registrationPath)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_201)
        clientId = TestUtil.parseResponseBody(registrationResponse, "client_id")

        accessToken = getApplicationAccessToken(ConnectorTestConstants.PKJWT_AUTH_METHOD, clientId, scopes)
        Assert.assertNotNull(accessToken)
    }

    @Test
    void "Update client request with an invalid clientId"() {

        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getUpdateRegularClaimsForGateway(SSA))

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${accessToken}")
                .body(payload)
                .put(registrationPath + "/" + invalidClientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Token is not bound to the client id sent in the request")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "unauthorized")
    }

    @Test
    void "Update client request with an invalid access token"() {

        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getUpdateRegularClaimsForGateway(SSA))

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${invalidAccessToken}")
                .body(payload)
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_ERRORS_DESCRIPTION),
                "Access failure for API: /open-banking/v3.3.0, version: v3.3.0 status: (900901) - Invalid " +
                        "Credentials. Make sure you have provided the correct security credentials")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_ERRORS_CODE),
                "900901")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_ERRORS_MSG),
                "Invalid Credentials")
    }

    @Test
    void "Update client request with a clientId that does not match with the access token"() {

        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getUpdateRegularClaimsForGateway(SSA))

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${accessToken}")
                .body(payload)
                .put(registrationPath + "/" + configClientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_401)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Token is not bound to the client id sent in the request")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "unauthorized")
    }

    @Test
    void "Update registration request with invalid redirectURI"() {

        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getRegularClaimsWithInvalidRedirectURI(SSA, "invalid_redirect_uri"))

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${accessToken}")
                .body(payload)
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_redirect_uri")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid redirect_uris found in the Request")
    }

    @Test
    void "Update registration request with null value for redirectURI"() {

        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getRegularClaimsForGatewayWithInvalidRedirectURI(SSA, null))

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${accessToken}")
                .body(payload)
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter redirectUris cannot be empty")
    }

    @Test
    void "Update registration request without redirectURI"() {

        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getRegularClaimsForGatewayWithoutRedirectURI(SSA))

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${accessToken}")
                .body(payload)
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter redirectUris cannot be empty")
    }

    @Test
    void "Update registration request with an invalid token_endpoint_auth_method"() {

        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getRegularClaimsForGateway(SSA, configuration.getAppDCRSoftwareId(), "invalid"))

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${accessToken}")
                .body(payload)
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "'invalid' Token endpoint authentication method is not allowed.")
    }

    @Test
    void "Update registration request with null value for token_endpoint_auth_method"() {

        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getRegularClaimsForGateway(SSA, configuration.getAppDCRSoftwareId(), null))

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${accessToken}")
                .body(payload)
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "'null' Token endpoint authentication method is not allowed.")
    }

    @Test
    void "Update registration request without token_endpoint_auth_method"() {

        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getRegularClaimsForGatewayWithoutTokenEndpointAuthMethod(SSA))

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${accessToken}")
                .body(payload)
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter tokenEndpointAuthMethod not found in the request")
    }

    @Test
    void "Update registration request with an invalid grant_type"() {

        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getRegularClaimsForGatewayWithInvalidGrantTypes(SSA, "invalid_grant_type"))

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${accessToken}")
                .body(payload)
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid grantTypes provided")
    }

    @Test
    void "Update registration request with null value for grant_type"() {

        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getRegularClaimsForGatewayWithInvalidGrantTypes(SSA, null))

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${accessToken}")
                .body(payload)
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid grantTypes provided")
    }

    @Test
    void "Update registration request without grant_type"() {

        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getRegularClaimsForGatewayWithoutGrantType(SSA))

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${accessToken}")
                .body(payload)
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter grantTypes cannot be empty")
    }

    @Test
    void "Update registration request with invalid SSA"() {

        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getRegularClaimsForGateway("invalid_ssa"))

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${accessToken}")
                .body(payload)
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
        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getRegularClaimsForGateway(SELF_SIGNED_SSA))

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${accessToken}")
                .body(payload)
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_request")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid request signature. Signed JWT rejected: Another algorithm expected, or no matching key(s) found")
    }

    @Test
    void "Update registration request with null value for SSA"() {

        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getRegularClaimsForGateway(null))

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${accessToken}")
                .body(payload)
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_software_statement")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Malformed Software Statement JWT found")
    }

    @Test
    void "Update registration request without SSA"() {

        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getRegularClaimsForGatewayWithoutSSA())

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${accessToken}")
                .body(payload)
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_software_statement")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter softwareStatement not found in the request")
    }

    @Test
    void "Update registration request with an invalid id_token_signed_response_alg"() {

        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getRegularClaimsForGateway(SSA, configuration.getAppDCRSoftwareId(),
                        ConnectorTestConstants.PKJWT_AUTH_METHOD,
                        Instant.now().toEpochMilli(), ConnectorTestConstants.ALG_PS256,
                        "invalid-algorithm"))

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${accessToken}")
                .body(payload)
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "'invalid-algorithm' Signing Algorithm is not allowed.")
    }

    @Test
    void "Update registration request with null value for id_token_signed_response_alg"() {

        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getRegularClaimsForGateway(SSA, configuration.getAppDCRSoftwareId(),
                        ConnectorTestConstants.PKJWT_AUTH_METHOD,
                        Instant.now().toEpochMilli(), ConnectorTestConstants.ALG_PS256,
                        null))

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${accessToken}")
                .body(payload)
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "'null' Signing Algorithm is not allowed.")
    }

    @Test
    void "Update registration request without id_token_signed_response_alg"() {

        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getRegularClaimsForGatewayWithoutIdTokenSignedResponseAlg(SSA))

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${accessToken}")
                .body(payload)
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter idTokenSignatureAlgorithm not found in the request")
    }

    @Test
    void "TC0101019_Update registration request with an invalid application_type"() {

        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getRegularClaimsForGatewayWithInvalidApplicationType(SSA, "test"))

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${accessToken}")
                .body(payload)
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid applicationType provided")
    }

    @Test
    void "TC0101020_Update registration request with null value for application_type"() {

        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getRegularClaimsForGatewayWithInvalidApplicationType(SSA, null))

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${accessToken}")
                .body(payload)
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid applicationType provided")
    }

    @Test
    void "TC0101021_Update registration request without application_type"() {

        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getRegularClaimsForGatewayWithoutApplicationType(SSA))

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${accessToken}")
                .body(payload)
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter applicationType not found in the request")
    }

    @Test
    void "Update registration request with an invalid request_object_signing_alg"() {

        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getRegularClaimsForGateway(SSA, configuration.getAppDCRSoftwareId(), ConnectorTestConstants.PKJWT_AUTH_METHOD,
                        Instant.now().toEpochMilli(), ConnectorTestConstants.ALG_PS256,
                        ConnectorTestConstants.ALG_PS256, "invalid_algorithm"))

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${accessToken}")
                .body(payload)
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "'invalid_algorithm' Signing Algorithm is not allowed.")
    }

    @Test
    void "Update registration request with null value for request_object_signing_alg"() {

        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getRegularClaimsForGateway(SSA, configuration.getAppDCRSoftwareId(), ConnectorTestConstants.PKJWT_AUTH_METHOD,
                        Instant.now().toEpochMilli(), ConnectorTestConstants.ALG_PS256,
                        ConnectorTestConstants.ALG_PS256, null))

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${accessToken}")
                .body(payload)
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "'null' Signing Algorithm is not allowed.")
    }

    @Test
    void "Update registration request without request_object_signing_alg"() {

        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getRegularClaimsForGatewayWithoutReqObjSignedAlg(SSA))

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${accessToken}")
                .body(payload)
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Required parameter requestObjectSignatureAlgorithm not found in the request")
    }

    @Test
    void "Update registration request with invalid iss claim"() {

        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getRegularClaimsForGateway(SSA, "invalid_iss"))

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${accessToken}")
                .body(payload)
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid issuer, issuer should be the same as the software id")
    }

    @Test
    void "Update registration request with an invalid token_endpoint_auth_signing_alg" (){

        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getRegularClaimsForGateway(SSA, configuration.getAppDCRSoftwareId(),
                        ConnectorTestConstants.PKJWT_AUTH_METHOD, Instant.now().toEpochMilli(),
                        "invalid_token_endpoint_auth_signing_alg"))

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${accessToken}")
                .body(payload)
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "'invalid_token_endpoint_auth_signing_alg' Signing Algorithm is not allowed.")
    }

    @Test
    void "Update registration request with an null token_endpoint_auth_signing_alg" (){

        String payload = jwtGenerator.getSignedRequestObject(ClientRegistrationRequestBuilder
                .getRegularClaimsForGateway(SSA, configuration.getAppDCRSoftwareId(),
                        ConnectorTestConstants.PKJWT_AUTH_METHOD, Instant.now().toEpochMilli(),
                        null))

        def registrationResponse = ClientRegistrationRequestBuilder
                .buildGatewayRegistrationRequest()
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${ConnectorTestConstants.BEARER} ${accessToken}")
                .body(payload)
                .put(registrationPath + "/" + clientId)

        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR),
                "invalid_client_metadata")
        Assert.assertEquals(TestUtil.parseResponseBody(registrationResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "'null' Signing Algorithm is not allowed.")
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
                .buildGatewayRegistrationRequestForRetrievalAndDelete()
                .header("Authorization", "Bearer " + accessToken)
                .delete(registrationPath + "/" + clientId)
        Assert.assertEquals(registrationResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_204)
    }
}
