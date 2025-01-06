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

package mtls_enforcement_validator

import org.wso2.openbanking.test.framework.keystore.OBKeyStore
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.Test
import org.wso2.financial.services.accelerator.test.framework.FSAcceleratorTest
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService

import org.wso2.financial.services.accelerator.test.framework.constant.AcceleratorTestConstants
import org.wso2.financial.services.accelerator.test.framework.request_builder.JWTGenerator
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

import javax.net.ssl.SSLHandshakeException
import java.nio.file.Paths

/**
 * MTLS Enforcement Validation Test Cases.
 */
class MtlsEnforcementValidationTest extends FSAcceleratorTest {

    String clientId
    String idToken
    AcceleratorTestConstants.ApiScope scope = AcceleratorTestConstants.ApiScope.ACCOUNTS
    private ConfigurationService configuration = new ConfigurationService()

    @Test
    void "OB-833_Validate access token generation with valid MTLS certificate in the header"() {

        clientId = configuration.getAppInfoClientID()
        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(AcceleratorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
                .header(AcceleratorTestConstants.X_WSO2_MUTUAL_CERT, TestUtil.getPublicKeyFromTransportKeyStore())
                .body(generator.getAppAccessTokenJwt(AcceleratorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(AcceleratorTestConstants.TOKEN_ENDPOINT_URL)

        def accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")

        Assert.assertEquals(tokenResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(accessToken)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "expires_in").toString(),
                AcceleratorTestConstants.TOKEN_EXPIRY_TIME)
        Assert.assertTrue(TestUtil.parseResponseBody(tokenResponse, "scope").contains(scope.scopeString))
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "token_type"), AcceleratorTestConstants.BEARER)
    }

    @Test
    void "OB-839_Validate access token generation with valid MTLS certificate in the context"() {

        clientId = configuration.getAppInfoClientID()
        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(AcceleratorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
                .header(AcceleratorTestConstants.X_WSO2_MUTUAL_CERT, TestUtil.getPublicKeyFromTransportKeyStore())
                .body(generator.getAppAccessTokenJwt(AcceleratorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(AcceleratorTestConstants.TOKEN_ENDPOINT_URL)

        def accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")

        Assert.assertEquals(tokenResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(accessToken)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "expires_in").toString(),
                AcceleratorTestConstants.TOKEN_EXPIRY_TIME)
        Assert.assertNotNull(TestUtil.parseResponseBody(tokenResponse, "scope"))
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "token_type"), AcceleratorTestConstants.BEARER)
    }

    @Test
    void "OB-480_Validate token request without MTLS certificate in the header and context"() {

        clientId = configuration.getAppInfoClientID()
        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = getAccessTokenRequestWithoutCertInContext()
                .body(generator.getAppAccessTokenJwt(AcceleratorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(AcceleratorTestConstants.TOKEN_ENDPOINT_URL)

        Assert.assertEquals(tokenResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, AcceleratorTestConstants.ERROR),
                AcceleratorTestConstants.INVALID_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, AcceleratorTestConstants.ERROR_DESCRIPTION),
                "Transport certificate not found in the request")
    }

    @Test
    void "OB-481_Validate token request with invalid MTLS certificate in the header"() {


        String keystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "berlin-certs","eidas-qwac.jks")
        String alias = "1"
        String password = "wso2carbon"

        clientId = configuration.getAppInfoClientID()
        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(AcceleratorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
                .header(AcceleratorTestConstants.X_WSO2_MUTUAL_CERT,
                        OBKeyStore.getPublicKeyFromKeyStore(keystoreLocation, password, alias))
                .body(generator.getAppAccessTokenJwt(AcceleratorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(AcceleratorTestConstants.TOKEN_ENDPOINT_URL)

        Assert.assertEquals(tokenResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, AcceleratorTestConstants.ERROR),
                AcceleratorTestConstants.INVALID_CLIENT)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, AcceleratorTestConstants.ERROR_DESCRIPTION),
                "Invalid transport certificate. Certificate passed through the request not valid")
    }

    @Test
    void "OB-482_Validate token request with expired MTLS certificate in the header"() {

        String keystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "expired-certs", "signing-keystore", "signing.jks")
        String alias = "tpp4-sig"
        String password = "wso2carbon"

        clientId = configuration.getAppInfoClientID()
        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(AcceleratorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
                .header(AcceleratorTestConstants.X_WSO2_MUTUAL_CERT,
                        OBKeyStore.getPublicKeyFromKeyStore(keystoreLocation, password, alias))
                .body(generator.getAppAccessTokenJwt(AcceleratorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(AcceleratorTestConstants.TOKEN_ENDPOINT_URL)

        Assert.assertEquals(tokenResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, AcceleratorTestConstants.ERROR),
                AcceleratorTestConstants.INVALID_CLIENT)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, AcceleratorTestConstants.ERROR_DESCRIPTION),
                "Invalid transport certificate. Certificate passed through the request not valid")
    }

    @Test
    void "OB-483_Validate token request when the request header and client_certificate_header configs are differ"() {

        clientId = configuration.getAppInfoClientID()
        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(AcceleratorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
                .header("mutual-auth-cert", TestUtil.getPublicKeyFromTransportKeyStore())
                .body(generator.getAppAccessTokenJwt(AcceleratorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(AcceleratorTestConstants.TOKEN_ENDPOINT_URL)

        Assert.assertEquals(tokenResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, AcceleratorTestConstants.ERROR),
                AcceleratorTestConstants.INVALID_REQUEST)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, AcceleratorTestConstants.ERROR_DESCRIPTION),
                "Transport certificate not found in the request")
    }

    @Test
    void "OB-558_Validate token request with valid MTLS certificate in the header and the context"() {

        clientId = configuration.getAppInfoClientID()
        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(AcceleratorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
                .header(AcceleratorTestConstants.X_WSO2_MUTUAL_CERT, TestUtil.getPublicKeyFromTransportKeyStore())
                .body(generator.getAppAccessTokenJwt(AcceleratorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(AcceleratorTestConstants.TOKEN_ENDPOINT_URL)

        accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")

        Assert.assertEquals(tokenResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(accessToken)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "expires_in").toString(),
                AcceleratorTestConstants.TOKEN_EXPIRY_TIME)
        Assert.assertNotNull(TestUtil.parseResponseBody(tokenResponse, "scope"))
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "token_type"), AcceleratorTestConstants.BEARER)
    }

    @Test
    void "OB-559_Validate token request with invalid TLS cert in the header and valid cert in the context" () {

        String keystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "expired-certs", "signing-keystore", "signing.jks")
        String alias = "tpp4-sig"
        String password = "wso2carbon"

        clientId = configuration.getAppInfoClientID()
        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(AcceleratorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
                .header(AcceleratorTestConstants.X_WSO2_MUTUAL_CERT,
                        OBKeyStore.getPublicKeyFromKeyStore(keystoreLocation, password, alias))
                .body(generator.getAppAccessTokenJwt(AcceleratorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(AcceleratorTestConstants.TOKEN_ENDPOINT_URL)

        def accessToken = TestUtil.parseResponseBody(tokenResponse, "access_token")
        Assert.assertEquals(tokenResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(accessToken)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "expires_in").toString(),
                AcceleratorTestConstants.TOKEN_EXPIRY_TIME)
        Assert.assertNotNull(TestUtil.parseResponseBody(tokenResponse, "scope"))
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "token_type"), AcceleratorTestConstants.BEARER)
    }

    @Test (expectedExceptions = SSLHandshakeException.class)
    void "OB-560_Validate token request with valid TLS cert in the header and invalid cert in the context" () {

        String keystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "expired-certs", "signing-keystore", "signing.jks")
        String alias = "tpp4-sig"
        String password = "wso2carbon"

        clientId = configuration.getAppInfoClientID()
        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = FSRestAsRequestBuilder.buildRequest(keystoreLocation, password)
                .contentType(AcceleratorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
                .header(AcceleratorTestConstants.X_WSO2_MUTUAL_CERT,
                        OBKeyStore.getPublicKeyFromKeyStore(keystoreLocation, password, alias))
                .body(generator.getAppAccessTokenJwt(AcceleratorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(AcceleratorTestConstants.TOKEN_ENDPOINT_URL)

        Assert.assertEquals(tokenResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_400)
    }

    //Test Scenarios for Token Request when client_transport_cert_as_header_enabled=false
//    enable the below test case after configuring client_transport_cert_as_header_enabled=false in deployment.toml
    @Test (enabled = false)
    void "OB-561_Token request with valid MTLS certificate in header when client_transport_cert_as_header disabled"() {

        clientId = configuration.getAppInfoClientID()
        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(AcceleratorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
                .header(AcceleratorTestConstants.X_WSO2_MUTUAL_CERT, TestUtil.getPublicKeyFromTransportKeyStore())
                .body(generator.getAppAccessTokenJwt(AcceleratorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(AcceleratorTestConstants.TOKEN_ENDPOINT_URL)

        Assert.assertEquals(tokenResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_400)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, AcceleratorTestConstants.ERROR),
                AcceleratorTestConstants.CERTIFICATE_NOT_FOUND)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, AcceleratorTestConstants.ERROR_DESCRIPTION),
                "Transport certificate not found in the request")
    }

    //enable the below test case after configuring client_transport_cert_as_header_enabled=false in deployment.toml
    @Test (enabled = false)
    void "OB-562_Validate token request with valid MTLS certificate in the context when client_transport_cert_as_header disabled"() {

        clientId = configuration.getAppInfoClientID()
        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(AcceleratorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
                .header(AcceleratorTestConstants.X_WSO2_MUTUAL_CERT, TestUtil.getPublicKeyFromTransportKeyStore())
                .body(generator.getAppAccessTokenJwt(AcceleratorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(AcceleratorTestConstants.TOKEN_ENDPOINT_URL)

        Assert.assertEquals(tokenResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(accessToken)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "expires_in").toString(),
                AcceleratorTestConstants.TOKEN_EXPIRY_TIME)
        Assert.assertNotNull(TestUtil.parseResponseBody(tokenResponse, "scope"))
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "token_type"), AcceleratorTestConstants.BEARER)
    }

    //enable the below test case after configuring client_transport_cert_as_header_enabled=false in deployment.toml
    @Test (enabled = false)
    void "OB-563_Validate token request with valid MTLS cert in header and context when server config is disabled"() {

        clientId = configuration.getAppInfoClientID()
        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = getAccessTokenRequestWithoutCertInContext()
                .body(generator.getAppAccessTokenJwt(AcceleratorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(AcceleratorTestConstants.TOKEN_ENDPOINT_URL)

        Assert.assertEquals(tokenResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(tokenResponse)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "expires_in").toString(),
                AcceleratorTestConstants.TOKEN_EXPIRY_TIME)
        Assert.assertNotNull(TestUtil.parseResponseBody(tokenResponse, "scope"))
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "token_type"), AcceleratorTestConstants.BEARER)
    }

    //enable the below test case after configuring client_transport_cert_as_header_enabled=false in deployment.toml
    @Test (enabled = false)
    void "OB-564_Token request with invalid TLS cert in header and valid cert in context when server config disabled"() {

        String keystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "expired-certs", "signing-keystore", "signing.jks")
        String alias = "tpp4-sig"
        String password = "wso2carbon"

        clientId = configuration.getAppInfoClientID()
        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(AcceleratorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
                .header(AcceleratorTestConstants.X_WSO2_MUTUAL_CERT,
                        OBKeyStore.getPublicKeyFromKeyStore(keystoreLocation, password, alias))
                .body(generator.getAppAccessTokenJwt(AcceleratorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(AcceleratorTestConstants.TOKEN_ENDPOINT_URL)

        def accessToken = TestUtil.parseResponseBody(tokenResponse, AcceleratorTestConstants.ACCESS_TOKEN)
        Assert.assertEquals(tokenResponse.statusCode(), AcceleratorTestConstants.STATUS_CODE_200)
        Assert.assertNotNull(accessToken)
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "expires_in").toString(),
                AcceleratorTestConstants.TOKEN_EXPIRY_TIME)
        Assert.assertNotNull(TestUtil.parseResponseBody(tokenResponse, "scope"))
        Assert.assertEquals(TestUtil.parseResponseBody(tokenResponse, "token_type"), AcceleratorTestConstants.BEARER)
    }

    //enable the below test case after configuring client_transport_cert_as_header_enabled=false in deployment.toml
    @Test (enabled = false, expectedExceptions = SSLHandshakeException.class)
    void "OB-565_Token request with valid TLS cert in header and invalid cert in context when server config disabled" () {

        String keystoreLocation = Paths.get(configuration.getTestArtifactLocation(),
                "expired-certs", "signing-keystore", "signing.jks")
        String alias = "tpp4-sig"
        String password = "wso2carbon"

        clientId = configuration.getAppInfoClientID()
        List<String> scopes = [scope.scopeString]
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        Response tokenResponse = FSRestAsRequestBuilder.buildRequest(keystoreLocation, password)
                .contentType(AcceleratorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
                .header(AcceleratorTestConstants.X_WSO2_MUTUAL_CERT,
                        OBKeyStore.getPublicKeyFromKeyStore(keystoreLocation, password, alias))
                .body(generator.getAppAccessTokenJwt(AcceleratorTestConstants.TLS_AUTH_METHOD, clientId))
                .post(AcceleratorTestConstants.TOKEN_ENDPOINT_URL)
    }
}
