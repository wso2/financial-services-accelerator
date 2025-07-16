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

package mtls_enforcement_validator

import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import org.wso2.bfsi.test.framework.keystore.KeyStore
import org.wso2.financial.services.accelerator.test.framework.FSConnectorTest
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService

import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.request_builder.JWTGenerator
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

import javax.net.ssl.SSLHandshakeException
import java.nio.file.Paths

/**
 * MTLS Enforcement Validation Test Cases.
 */
class MtlsEnforcementValidationTest extends FSConnectorTest {

    String clientId
    String idToken
    ConnectorTestConstants.ApiScope scope = ConnectorTestConstants.ApiScope.ACCOUNTS
    private ConfigurationService configuration = new ConfigurationService()

    @BeforeClass
    void init() {
        //Create Regulatory Application with tls_client_auth method
        clientId = createApplication(configuration.getAppDCRSoftwareId(), ConnectorTestConstants.TLS_AUTH_METHOD)
    }

    @Test
    void "Validate access token generation with valid MTLS certificate in the header"() {

        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
                .header(ConnectorTestConstants.X_WSO2_MUTUAL_CERT, TestUtil.getPublicKeyFromTransportKeyStore())
                .body(generator.getAppAccessTokenJwt(ConnectorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(ConnectorTestConstants.TOKEN_ENDPOINT_URL)

        def accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(accessToken)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "expires_in").toString(),
                ConnectorTestConstants.TOKEN_EXPIRY_TIME)
        Assert.assertTrue(TestUtil.parseResponseBody(tokenResponse, "scope").contains(scope.scopeString))
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "token_type"), ConnectorTestConstants.BEARER)
    }

    @Test
    void "Validate access token generation with valid MTLS certificate in the context"() {

        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
                .body(generator.getAppAccessTokenJwt(ConnectorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(ConnectorTestConstants.TOKEN_ENDPOINT_URL)

        def accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(accessToken)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "expires_in").toString(),
                ConnectorTestConstants.TOKEN_EXPIRY_TIME)
        Assert.assertNotNull(TestUtil.parseResponseBody(tokenResponse, "scope"))
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "token_type"), ConnectorTestConstants.BEARER)
    }

    //TODO: Enable after fixing the issue : https://github.com/wso2/financial-services-accelerator/issues/118
    @Test(enabled = false)
    void "Validate token request without MTLS certificate in the header and context"() {

        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = getAccessTokenRequestWithoutCertInContext()
                .body(generator.getAppAccessTokenJwt(ConnectorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(ConnectorTestConstants.TOKEN_ENDPOINT_URL)

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR),
                ConnectorTestConstants.INVALID_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Transport certificate not found in the request")
    }

    //TODO: Enable after fixing the issue: https://github.com/wso2/financial-services-accelerator/issues/119
    @Test(enabled = false)
    void "Validate token request with invalid MTLS certificate in the header"() {

        String keystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "berlin-certs","eidas-qwac.jks")
        String alias = "1"
        String password = "wso2carbon"

        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = FSRestAsRequestBuilder.buildBasicRequestWithoutTlsContext()
                .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
                .header(ConnectorTestConstants.X_WSO2_MUTUAL_CERT,
                        KeyStore.getPublicKeyFromKeyStore(keystoreLocation, password, alias))
                .body(generator.getAppAccessTokenJwt(ConnectorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(ConnectorTestConstants.TOKEN_ENDPOINT_URL)

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR),
                ConnectorTestConstants.INVALID_CLIENT)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid transport certificate. Certificate passed through the request not valid")
    }

    //TODO: Enable after fixing the issue: https://github.com/wso2/financial-services-accelerator/issues/119
    @Test(enabled = false)
    void "Validate token request with expired MTLS certificate in the header"() {

        String keystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "expired-certs", "signing-keystore", "signing.jks")
        String alias = "tpp4-sig"
        String password = "wso2carbon"

        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = FSRestAsRequestBuilder.buildBasicRequestWithoutTlsContext()
                .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
                .header(ConnectorTestConstants.X_WSO2_MUTUAL_CERT,
                        KeyStore.getPublicKeyFromKeyStore(keystoreLocation, password, alias))
                .body(generator.getAppAccessTokenJwt(ConnectorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(ConnectorTestConstants.TOKEN_ENDPOINT_URL)

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR),
                ConnectorTestConstants.INVALID_CLIENT)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Invalid transport certificate. Certificate passed through the request not valid")
    }

    //TODO: Enable after fixing the issue : https://github.com/wso2/financial-services-accelerator/issues/118
    @Test(enabled = false)
    void "Validate token request when the request header and client_certificate_header configs are differ"() {

        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)

        Response tokenResponse = FSRestAsRequestBuilder.buildBasicRequestWithoutTlsContext()
                .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
                .header("mutual-auth-cert", TestUtil.getPublicKeyFromTransportKeyStore())
                .body(generator.getAppAccessTokenJwt(ConnectorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(ConnectorTestConstants.TOKEN_ENDPOINT_URL)

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR),
                ConnectorTestConstants.INVALID_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Transport certificate not found in the request")
    }

    @Test
    void "Validate token request with valid MTLS certificate in the header and the context"() {

        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
                .header(ConnectorTestConstants.X_WSO2_MUTUAL_CERT, TestUtil.getPublicKeyFromTransportKeyStore())
                .body(generator.getAppAccessTokenJwt(ConnectorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(ConnectorTestConstants.TOKEN_ENDPOINT_URL)

        accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(accessToken)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "expires_in").toString(),
                ConnectorTestConstants.TOKEN_EXPIRY_TIME)
        Assert.assertNotNull(TestUtil.parseResponseBody(tokenResponse, "scope"))
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "token_type"), ConnectorTestConstants.BEARER)
    }

    //TODO: Enable after fixing the issue : https://github.com/wso2/financial-services-accelerator/issues/118
    @Test(enabled = false)
    void "Validate token request with invalid TLS cert in the header and valid cert in the context" () {

        String keystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "expired-certs", "signing-keystore", "signing.jks")
        String alias = "tpp4-sig"
        String password = "wso2carbon"

        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
                .header(ConnectorTestConstants.X_WSO2_MUTUAL_CERT,
                        KeyStore.getPublicKeyFromKeyStore(keystoreLocation, password, alias))
                .body(generator.getAppAccessTokenJwt(ConnectorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(ConnectorTestConstants.TOKEN_ENDPOINT_URL)

        def accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(accessToken)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "expires_in").toString(),
                ConnectorTestConstants.TOKEN_EXPIRY_TIME)
        Assert.assertNotNull(TestUtil.parseResponseBody(tokenResponse, "scope"))
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "token_type"), ConnectorTestConstants.BEARER)
    }

    //TODO: Enable after fixing issue
    @Test (enabled = false, expectedExceptions = SSLHandshakeException.class)
    void "Validate token request with valid TLS cert in the header and expired cert in the context" () {

        String keystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "expired-certs", "signing-keystore", "signing.jks")
        String alias = "tpp4-sig"
        String password = "wso2carbon"

        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = FSRestAsRequestBuilder.buildRequest(keystoreLocation, password)
                .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
                .header(ConnectorTestConstants.X_WSO2_MUTUAL_CERT, TestUtil.getPublicKeyFromTransportKeyStore())
                .body(generator.getAppAccessTokenJwt(ConnectorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(ConnectorTestConstants.TOKEN_ENDPOINT_URL)

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
    }

    //Test Scenarios for Token Request when client_transport_cert_as_header_enabled=false
//    enable the below test case after configuring client_transport_cert_as_header_enabled=false in deployment.toml
    @Test (enabled = false)
    void "Token request with valid MTLS certificate in header when client_transport_cert_as_header disabled"() {

        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = FSRestAsRequestBuilder.buildBasicRequestWithoutTlsContext()
                .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
                .header(ConnectorTestConstants.X_WSO2_MUTUAL_CERT, TestUtil.getPublicKeyFromTransportKeyStore())
                .body(generator.getAppAccessTokenJwt(ConnectorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(ConnectorTestConstants.TOKEN_ENDPOINT_URL)

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR),
                ConnectorTestConstants.CERTIFICATE_NOT_FOUND)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ERROR_DESCRIPTION),
                "Transport certificate not found in the request")
    }

    //enable the below test case after configuring client_transport_cert_as_header_enabled=false in deployment.toml
    @Test (enabled = false)
    void "Validate token request with valid MTLS certificate in the context when client_transport_cert_as_header disabled"() {

        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
                .body(generator.getAppAccessTokenJwt(ConnectorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(ConnectorTestConstants.TOKEN_ENDPOINT_URL)

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(accessToken)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "expires_in").toString(),
                ConnectorTestConstants.TOKEN_EXPIRY_TIME)
        Assert.assertNotNull(TestUtil.parseResponseBody(tokenResponse, "scope"))
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "token_type"), ConnectorTestConstants.BEARER)
    }

    //enable the below test case after configuring client_transport_cert_as_header_enabled=false in deployment.toml
    @Test (enabled = false)
    void "Validate token request with valid MTLS cert in header and context when server config is disabled"() {

        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
                .header(ConnectorTestConstants.X_WSO2_MUTUAL_CERT, TestUtil.getPublicKeyFromTransportKeyStore())
                .body(generator.getAppAccessTokenJwt(ConnectorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(ConnectorTestConstants.TOKEN_ENDPOINT_URL)

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(tokenResponse)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "expires_in").toString(),
                ConnectorTestConstants.TOKEN_EXPIRY_TIME)
        Assert.assertNotNull(TestUtil.parseResponseBody(tokenResponse, "scope"))
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "token_type"), ConnectorTestConstants.BEARER)
    }

    //enable the below test case after configuring client_transport_cert_as_header_enabled=false in deployment.toml
    @Test (enabled = false)
    void "Token request with invalid TLS cert in header and valid cert in context when server config disabled"() {

        String keystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "expired-certs", "signing-keystore", "signing.jks")
        String alias = "tpp4-sig"
        String password = "wso2carbon"

        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
                .header(ConnectorTestConstants.X_WSO2_MUTUAL_CERT,
                        KeyStore.getPublicKeyFromKeyStore(keystoreLocation, password, alias))
                .body(generator.getAppAccessTokenJwt(ConnectorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(ConnectorTestConstants.TOKEN_ENDPOINT_URL)

        def accessToken = TestUtil.parseResponseBody(tokenResponse, ConnectorTestConstants.ACCESS_TOKEN)
        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(accessToken)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "expires_in").toString(),
                ConnectorTestConstants.TOKEN_EXPIRY_TIME)
        Assert.assertNotNull(TestUtil.parseResponseBody(tokenResponse, "scope"))
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "token_type"), ConnectorTestConstants.BEARER)
    }

    //enable the below test case after configuring client_transport_cert_as_header_enabled=false in deployment.toml
    @Test (enabled = false, expectedExceptions = SSLHandshakeException.class)
    void "Token request with valid TLS cert in header and invalid cert in context when server config disabled" () {

        String keystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "expired-certs", "signing-keystore", "signing.jks")
        String alias = "tpp4-sig"
        String password = "wso2carbon"

        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = FSRestAsRequestBuilder.buildRequest(keystoreLocation, password)
                .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
                .header(ConnectorTestConstants.X_WSO2_MUTUAL_CERT, TestUtil.getPublicKeyFromTransportKeyStore())
                .body(generator.getAppAccessTokenJwt(ConnectorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(ConnectorTestConstants.TOKEN_ENDPOINT_URL)
    }

    //TODO: Enable after fixing issue
    @Test (enabled = false)
    void "Validate token request with valid TLS cert in the header and revoked cert in the context" () {

        String keystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "revoked-certs", "transport.jks")
        String password = "wso2carbon"

        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = FSRestAsRequestBuilder.buildRequest(keystoreLocation, password)
                .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
                .header(ConnectorTestConstants.X_WSO2_MUTUAL_CERT, TestUtil.getPublicKeyFromTransportKeyStore())
                .body(generator.getAppAccessTokenJwt(ConnectorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(ConnectorTestConstants.TOKEN_ENDPOINT_URL)

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
    }

    @Test (enabled = false)
    void "Validate token request with cert not bound to application in context when client_transport_cert_as_header disabled" () {

        String keystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "DynamicClientRegistration", "uk", "tpp2", "transport-keystore", "transport.jks")
        String password = "wso2carbon"

        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = FSRestAsRequestBuilder.buildRequest(keystoreLocation, password)
                .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
                .body(generator.getAppAccessTokenJwt(ConnectorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(ConnectorTestConstants.TOKEN_ENDPOINT_URL)

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
    }

    @Test (enabled = false)
    void "Validate token request with cert not bound to application in header when client_transport_cert_as_header enabled" () {

        String keystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "DynamicClientRegistration", "uk", "tpp2", "transport-keystore", "transport.jks")
        String password = "wso2carbon"
        String alias = "transport"

        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = FSRestAsRequestBuilder.buildBasicRequestWithoutTlsContext()
                .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
                .header(ConnectorTestConstants.X_WSO2_MUTUAL_CERT,
                        KeyStore.getPublicKeyFromKeyStore(keystoreLocation, password, alias))
                .body(generator.getAppAccessTokenJwt(ConnectorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(ConnectorTestConstants.TOKEN_ENDPOINT_URL)

        Assert.assertEquals(tokenResponse.statusCode(), ConnectorTestConstants.STATUS_CODE_400)
    }

    @AfterClass
    void cleanup() {
        //Delete the application created for the test
//        deleteApplication(clientId, ConnectorTestConstants.TLS_AUTH_METHOD)
    }
}
