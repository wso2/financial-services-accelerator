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

package org.wso2.financial.services.accelerator.test.framework.request_builder

import org.wso2.bfsi.test.framework.request_builder.JSONRequestGenerator
import org.wso2.bfsi.test.framework.request_builder.PayloadGenerator
import org.wso2.bfsi.test.framework.request_builder.SignedObject
import io.restassured.RestAssured
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.json.JSONObject
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants
import org.wso2.financial.services.accelerator.test.framework.utility.FSRestAsRequestBuilder
import org.wso2.financial.services.accelerator.test.framework.utility.TestUtil

import java.nio.charset.Charset

/**
 * Client Accelerator Request Builder Class.
 */
class TokenRequestBuilder {

    private static Logger log = LogManager.getLogger(TokenRequestBuilder.class.getName())
    private static ConfigurationService configuration = new ConfigurationService()

    /**
     * Method to get application access token
     * @param authMethodType
     * @param scopes
     * @param clientId
     * @return
     */
    static Response getApplicationAccessTokenResponse(String authMethodType, List<String> scopes, String clientId) {
        JWTGenerator acceleratorJWTGenerator = new JWTGenerator()
        acceleratorJWTGenerator.setScopes(scopes)
        String jwt = acceleratorJWTGenerator.getAppAccessTokenJwt(authMethodType, clientId)

        RestAssured.baseURI = configuration.getISServerUrl()
        Response response
        if (ConnectorTestConstants.TLS_AUTH_METHOD == authMethodType) {
            response = FSRestAsRequestBuilder.buildRequest()
                    .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                    .header(ConnectorTestConstants.X_WSO2_MUTUAL_CERT, TestUtil.getPublicKeyFromTransportKeyStore())
                    .body(jwt)
                    .post(ConnectorTestConstants.TOKEN_ENDPOINT)
        } else {
            response = FSRestAsRequestBuilder.buildRequest()
                    .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                    .body(jwt)
                    .post(ConnectorTestConstants.TOKEN_ENDPOINT)
        }

        return response
    }

    /**
     * Method to get application access token
     * @param authMethodType
     * @param scopes
     * @param clientId
     * @return
     */
    static Response getApplicationAccessTokenResponseWithCustomExp(String authMethodType, List<String> scopes,
                                                                   String clientId, long exp) {
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        //Adding Client Assertion for other Auth Method types
        JSONObject clientAssertion = new JSONRequestGenerator().addIssuer(clientId)
                .addSubject(clientId).addAudience().addExpireDate(exp).addIssuedAt().addJti().getJsonObject()

        String payload = generator.getSignedRequestObject(clientAssertion.toString())
        String accessTokenJWT = new PayloadGenerator().addGrantType().addScopes(scopes).addClientAsType()
                .addClientAssertion(payload).addRedirectUri().addClientID(clientId).getPayload()

        RestAssured.baseURI = configuration.getISServerUrl()
        Response response = FSRestAsRequestBuilder.buildRequest()
                .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .body(accessTokenJWT)
                .post(ConnectorTestConstants.TOKEN_ENDPOINT)

        return response
    }

    /**
     * Method to get application access token
     * @param authMethodType
     * @param scopes
     * @param clientId
     * @return
     */
    static Response getApplicationAccessTokenResponseWithoutAssertion(String authMethodType, List<String> scopes,
                                                                      String clientId) {
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        String accessTokenJWT = new PayloadGenerator().addGrantType().addScopes(scopes).addClientAsType()
                .addRedirectUri().addClientID(clientId).getPayload()

        RestAssured.baseURI = configuration.getISServerUrl()
        Response response
        if (ConnectorTestConstants.TLS_AUTH_METHOD == authMethodType) {
            response = FSRestAsRequestBuilder.buildRequest()
                    .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                    .header(ConnectorTestConstants.X_WSO2_MUTUAL_CERT, TestUtil.getPublicKeyFromTransportKeyStore())
                    .body(accessTokenJWT)
                    .post(ConnectorTestConstants.TOKEN_ENDPOINT)
        } else {
            response = FSRestAsRequestBuilder.buildRequest()
                    .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                    .body(accessTokenJWT)
                    .post(ConnectorTestConstants.TOKEN_ENDPOINT)
        }

        return response
    }

    /**
     * Method to get application access token
     * @param authMethodType
     * @param scopes
     * @param clientId
     * @return
     */
    static Response getApplicationAccessTokenResponseWithoutClientId(String authMethod, List<String> scopes) {
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        String accessTokenJWT

        if(authMethod == ConnectorTestConstants.TLS_AUTH_METHOD){
            accessTokenJWT = new PayloadGenerator().addGrantType().addScopes(scopes)
                    .addRedirectUri().getPayload()
        } else {
            //Adding Client Assertion for other Auth Method types
            JSONObject clientAssertion = new JSONRequestGenerator().addAudience().addExpireDate().addIssuedAt().addJti().getJsonObject()

            String payload = generator.getSignedRequestObject(clientAssertion.toString())
            accessTokenJWT = new PayloadGenerator().addGrantType().addScopes(scopes).addClientAsType()
                    .addClientAssertion(payload).addRedirectUri().getPayload()
        }

        RestAssured.baseURI = configuration.getISServerUrl()
        Response response = FSRestAsRequestBuilder.buildRequest()
                .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .body(accessTokenJWT)
                .post(ConnectorTestConstants.TOKEN_ENDPOINT)

        return response
    }

    /**
     * Method to get application access token
     * @param authMethodType
     * @param scopes
     * @param clientId
     * @return
     */
    static Response getApplicationAccessTokenTLSWithAndAssertion(List<String> scopes, String clientId) {
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        JSONObject clientAssertion = new JSONRequestGenerator().addIssuer(clientId)
                .addSubject(clientId).addAudience().addExpireDate().addIssuedAt().addJti().getJsonObject()

        String payload = generator.getSignedRequestObject(clientAssertion.toString())
        String accessTokenJWT = new PayloadGenerator().addGrantType().addScopes(scopes).addClientAsType()
                .addClientID(clientId).addClientAssertion(payload).addRedirectUri().getPayload()

        RestAssured.baseURI = configuration.getISServerUrl()
        Response response = FSRestAsRequestBuilder.buildRequest()
                .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .body(accessTokenJWT)
                .post(ConnectorTestConstants.TOKEN_ENDPOINT)

        return response
    }

    /**
     * Method to get application access token
     * @param authMethodType
     * @param scopes
     * @param clientId
     * @return
     */
    static Response getApplicationAccessTokenResponseWithCertAndAssertion(String authMethodType, List<String> scopes, String clientId) {
        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        JSONObject clientAssertion = new JSONRequestGenerator().addIssuer(clientId)
                .addSubject(clientId).addAudience().addExpireDate().addIssuedAt().addJti().getJsonObject()

        String payload = generator.getSignedRequestObject(clientAssertion.toString())
        String accessTokenJWT = new PayloadGenerator().addGrantType().addScopes(scopes).addClientAsType()
                .addClientID(clientId).addClientAssertion(payload).addRedirectUri().getPayload()

        RestAssured.baseURI = configuration.getISServerUrl()
        Response response = FSRestAsRequestBuilder.buildRequest()
                .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(ConnectorTestConstants.X_WSO2_MUTUAL_CERT, TestUtil.getPublicKeyFromTransportKeyStore())
                .body(accessTokenJWT)
                .post(ConnectorTestConstants.TOKEN_ENDPOINT)

        return response
    }

    /**
     * Method to get User Access Token from Authorization Code
     * * @param authMethodType
     * @param scopes
     * @param clientId
     * @param code
     * @return
     */
    static String getUserAccessToken(String authMethodType, List<String> scopes, String clientId, String code) {
        JWTGenerator acceleratorJWTGenerator = new JWTGenerator()
        acceleratorJWTGenerator.setScopes(scopes)
        String jwt = acceleratorJWTGenerator.getUserAccessTokenJwt(authMethodType, clientId, code)

        RestAssured.baseURI = configuration.getISServerUrl()
        Response response = FSRestAsRequestBuilder.buildRequest()
                .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(ConnectorTestConstants.X_WSO2_MUTUAL_CERT, TestUtil.getPublicKeyFromTransportKeyStore())
                .body(jwt)
                .post(ConnectorTestConstants.TOKEN_ENDPOINT)

        def accessToken = TestUtil.parseResponseBody(response, "access_token")
        log.info("Got user access token $accessToken")

        return accessToken
    }

    /**
     * Method to get User Access Token from Authorization Code
     * * @param authMethodType
     * @param scopes
     * @param clientId
     * @param code
     * @return
     */
    static Response getUserAccessTokenResponse(String authMethodType, List<String> scopes, String clientId, String code,
                                               boolean isCodeVerifierRequired = false) {
        JWTGenerator acceleratorJWTGenerator = new JWTGenerator()
        acceleratorJWTGenerator.setScopes(scopes)
        String jwt

        if(isCodeVerifierRequired) {
            jwt = acceleratorJWTGenerator.getUserAccessTokenJwtWithCodeVerifier(authMethodType, clientId, code)
        } else {
            jwt = acceleratorJWTGenerator.getUserAccessTokenJwt(authMethodType, clientId, code)
        }

        RestAssured.baseURI = configuration.getISServerUrl()
        Response response
        if (ConnectorTestConstants.TLS_AUTH_METHOD == authMethodType) {
            response = FSRestAsRequestBuilder.buildRequest()
                    .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                    .header(ConnectorTestConstants.X_WSO2_MUTUAL_CERT, TestUtil.getPublicKeyFromTransportKeyStore())
                    .body(jwt)
                    .post(ConnectorTestConstants.TOKEN_ENDPOINT)
        } else {
            response = FSRestAsRequestBuilder.buildRequest()
                    .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                    .body(jwt)
                    .post(ConnectorTestConstants.TOKEN_ENDPOINT)
        }
        return response
    }

    /**
     * Method to get User Access Token from Authorization Code
     * * @param authMethodType
     * @param scopes
     * @param clientId
     * @param code
     * @return
     */
    static Response getTokenIntrospectionResponse(String accessToken) {

        def authToken = "${configuration.getUserKeyManagerAdminName()}:${configuration.getUserKeyManagerAdminPWD()}"
        def basicHeader = "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"

        def payload = "token=$accessToken"

        RestAssured.baseURI = configuration.getISServerUrl()
        Response response = FSRestAsRequestBuilder.buildRequest()
                .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(ConnectorTestConstants.X_WSO2_MUTUAL_CERT, TestUtil.getPublicKeyFromTransportKeyStore())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${basicHeader}")
                .body(payload)
                .post(ConnectorTestConstants.INTROSPECTION_ENDPOINT)

        return response
    }

    static Response getPasswordGrantAccessToken(List<String> scopes, String clientId, String username,
                                                String password, String authMethod = ConnectorTestConstants.TLS_AUTH_METHOD) {

        Response tokenResponse

        def authToken = "${configuration.getAppInfoClientID()}:${configuration.getAppInfoClientSecret()}"
        def basicHeader = "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"


        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        String payload = generator.getPasswordAccessTokenPayload(authMethod, clientId, username, password)

        RestAssured.baseURI = configuration.getISServerUrl()
        if (ConnectorTestConstants.TLS_AUTH_METHOD == authMethod) {
            tokenResponse = FSRestAsRequestBuilder.buildRequest()
                    .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                    .header(ConnectorTestConstants.X_WSO2_MUTUAL_CERT, TestUtil.getPublicKeyFromTransportKeyStore())
                    .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${basicHeader}")
                    .body(payload)
                    .post(ConnectorTestConstants.TOKEN_ENDPOINT)
        } else {
            tokenResponse = FSRestAsRequestBuilder.buildRequest()
                    .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                    .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${basicHeader}")
                    .body(payload)
                    .post(ConnectorTestConstants.TOKEN_ENDPOINT)
        }

        return tokenResponse
    }

    static Response getRefreshGrantTokenResponse(List<String> scopes, String clientId, String refreshToken,
                                                 String authMethod = ConnectorTestConstants.TLS_AUTH_METHOD) {

        Response tokenResponse

        def authToken = "${configuration.getAppInfoClientID()}:${configuration.getAppInfoClientSecret()}"
        def basicHeader = "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"

        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        String payload = generator.getRefreshAccessTokenPayload(authMethod, clientId, refreshToken)

        RestAssured.baseURI = configuration.getISServerUrl()
        if (ConnectorTestConstants.TLS_AUTH_METHOD == authMethod) {
            tokenResponse = FSRestAsRequestBuilder.buildRequest()
                    .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                    .header(ConnectorTestConstants.X_WSO2_MUTUAL_CERT, TestUtil.getPublicKeyFromTransportKeyStore())
                    .body(payload)
                    .post(ConnectorTestConstants.TOKEN_ENDPOINT)
        } else {
            tokenResponse = FSRestAsRequestBuilder.buildRequest()
                    .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                    .body(payload)
                    .post(ConnectorTestConstants.TOKEN_ENDPOINT)
        }

        return tokenResponse
    }

    /**
     * Get Base Access Token Request without TLS cert in the context.
     * @return access token request
     */
    static RequestSpecification getAccessTokenRequestWithoutCertInContext() {

        return FSRestAsRequestBuilder.buildBasicRequestWithoutTlsContext()
                .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .baseUri(configuration.getISServerUrl())
    }

    /**
     * Token Revocation Request.
     * @param accessToken
     */
    static Response doTokenRevocation(String accessToken, String clientId) {

        def payload  = "token=$accessToken&token_type_hint=access_token" +
                "&client_id=$clientId"

        Response response = FSRestAsRequestBuilder.buildRequest()
                .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .body(payload)
                .baseUri(configuration.getISServerUrl())
                .post(ConnectorTestConstants.OAUTH2_REVOKE_ENDPOINT)

        return response
    }

    /**
     * Method to get User access token with defined certs.
     * @param signingAlgorithm
     * @param appKeystoreLocation
     * @param appKeystorePassword
     * @param appKeystoreAlias
     * @param scopes
     * @param clientId
     * @param code
     * @return
     */
    static Response getUserAccessTokenWithDefinedCert(String signingAlgorithm, String appKeystoreLocation,
                                                       String appKeystorePassword, String appKeystoreAlias,
                                                       List<ConnectorTestConstants.ApiScope> scopes, String clientId,
                                                      String code) {

        SignedObject signedObject = new SignedObject()

        JSONObject clientAssertion = new JSONRequestGenerator().addIssuer(clientId)
                .addSubject(clientId).addAudience().addExpireDate().addIssuedAt().addJti().getJsonObject()

        String payload = signedObject.getSignedRequestObjectWithDefinedCert(clientAssertion.toString(), signingAlgorithm,
                appKeystoreLocation, appKeystorePassword, appKeystoreAlias)

        String accessTokenJWT = new PayloadGenerator().addGrantType(ConnectorTestConstants.AUTH_CODE).addCode(code)
                .addScopes(scopes).addClientAsType().addClientAssertion(payload).addRedirectUri().getPayload()

        RestAssured.baseURI = configuration.getISServerUrl()
        Response response
        response = FSRestAsRequestBuilder.buildRequest()
                    .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                    .body(accessTokenJWT)
                    .post(ConnectorTestConstants.TOKEN_ENDPOINT)

        return response
    }

    public static String getScopeString(List<ConnectorTestConstants.ApiScope> scopes) {
        return "${ConnectorTestConstants.ApiScope.OPEN_ID.scopeString} " +
                "${String.join(" ", scopes.collect({ it.scopeString }))}"
    }

    /**
     * Method to get application access token with defined JTI.
     * @param scopes
     * @param clientId
     * @param jti
     * @return
     */
    static Response getApplicationAccessTokenResponseWithDefinedJti(List <ConnectorTestConstants.ApiScope> scopeString,
                                                                    String clientId, String jti) {

        List<String> scopes = scopeString.collect { it.scopeString }.toList()

        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        //Adding Client Assertion for other Auth Method types
        JSONObject clientAssertion = new JSONRequestGenerator().addIssuer(clientId)
                .addSubject(clientId).addAudience().addIssuedAt().addExpireDate().addJti(jti).getJsonObject()

        String payload = generator.getSignedRequestObject(clientAssertion.toString())
        String accessTokenJWT = new PayloadGenerator().addGrantType().addScopes(scopes).addClientAsType()
                .addClientAssertion(payload).addRedirectUri().addClientID(clientId).getPayload()

        RestAssured.baseURI = configuration.getISServerUrl()
        Response response = FSRestAsRequestBuilder.buildRequest()
                .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .body(accessTokenJWT)
                .post(ConnectorTestConstants.TOKEN_ENDPOINT)

        return response
    }

    /**
     * Method to get refresh token response without client id
     * @param scopes
     * @param refreshToken
     * @return
     */
    static Response getRefreshGrantTokenResponseWithoutClientId(List<ConnectorTestConstants.ApiScope> scopeString,
                                                                String refreshToken) {

        Response tokenResponse
        List<String> scopes = scopeString.stream().map { it.scopeString }.toList()

        def authToken = "${configuration.getAppInfoClientID()}:${configuration.getAppInfoClientSecret()}"
        def basicHeader = "Basic ${Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))}"

        JWTGenerator generator = new JWTGenerator()
        generator.setScopes(scopes)
        String payload = generator.getRefreshAccessTokenPayloadWithoutClientId(refreshToken)

        RestAssured.baseURI = configuration.getISServerUrl()
        tokenResponse = FSRestAsRequestBuilder.buildRequest()
                .contentType(ConnectorTestConstants.ACCESS_TOKEN_CONTENT_TYPE)
                .header(ConnectorTestConstants.X_WSO2_MUTUAL_CERT, TestUtil.getPublicKeyFromTransportKeyStore())
                .header(ConnectorTestConstants.AUTHORIZATION_HEADER, "${basicHeader}")
                .body(payload)
                .post(ConnectorTestConstants.TOKEN_ENDPOINT)

        return tokenResponse
    }
}
