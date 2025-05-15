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

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.oauth2.sdk.id.Issuer
import com.nimbusds.oauth2.sdk.pkce.CodeChallenge
import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod
import org.json.JSONException
import org.wso2.bfsi.test.framework.exception.TestFrameworkException
import org.wso2.bfsi.test.framework.keystore.KeyStore
import org.wso2.bfsi.test.framework.request_builder.JSONRequestGenerator
import org.wso2.bfsi.test.framework.request_builder.PayloadGenerator
import org.wso2.bfsi.test.framework.request_builder.SignedObject
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.json.JSONObject
import org.testng.Reporter
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.ConnectorTestConstants

import java.security.Key
import java.security.PrivateKey
import java.security.Security
import java.security.cert.Certificate
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Class for generating JWT in Accelerator layer.
 */
class JWTGenerator {

    private ConfigurationService acceleratorConfiguration
    private List<String> scopesList = null // Scopes can be set before generate payload
    private String signingAlgorithm

    JWTGenerator(){
        acceleratorConfiguration = new ConfigurationService()
    }

    void setScopes(List<String> scopes){
        scopesList = scopes
    }

    /**
     * Set signing algorithm
     * @param algorithm
     */
    void setSigningAlgorithm(String algorithm) {
        this.signingAlgorithm = algorithm
    }

    void setKeystore(List<String> scopes){
        scopesList = scopes
    }

    /**
     * Get signing algorithm for methods. IF signing algorithm is null, provide algorithm in configuration
     * @return
     */
    String getSigningAlgorithm() {
        if (signingAlgorithm == null) {
            signingAlgorithm = acceleratorConfiguration.getCommonSigningAlgorithm()
        }
        return this.signingAlgorithm
    }

    /**
     * Get Signed object
     * @param claims
     * @return
     */
     String getSignedRequestObject(String claims) {

        Certificate certificate = KeyStore.getApplicationCertificate()
        String thumbprint = KeyStore.getJwkThumbPrintForSHA1(certificate)
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.parse(getSigningAlgorithm()))
                .keyID(thumbprint).type(JOSEObjectType.JWT).build()
        Key signingKey = KeyStore.getApplicationSigningKey()

        JWSObject jwsObject = new JWSObject(header, new Payload(claims))
        JWSSigner signer = new RSASSASigner((PrivateKey) signingKey)
        Security.addProvider(new BouncyCastleProvider())
        jwsObject.sign(signer)
        return jwsObject.serialize()
    }

    /**
     * Get Signed object
     * @param claims
     * @return
     */
    String getSignedRequestObjectWithCustomAlgorithm(String claims) {

        Certificate certificate = KeyStore.getApplicationCertificate()
        String thumbprint = KeyStore.getJwkThumbPrintForSHA1(certificate)
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.parse("RS256"))
                .keyID(thumbprint).type(JOSEObjectType.JWT).build()
        Key signingKey = KeyStore.getApplicationSigningKey()

        JWSObject jwsObject = new JWSObject(header, new Payload(claims))
        JWSSigner signer = new RSASSASigner((PrivateKey) signingKey)
        Security.addProvider(new BouncyCastleProvider())
        jwsObject.sign(signer)
        return jwsObject.serialize()
    }

    /**
     * Return signed JWT for Authorization request
     * @param scopeString
     * @param clientID
     * @param iss
     * @param state
     * @param nonce
     * @param response_type
     * @param audienceValue
     * @param redirectUrl
     * @return signed jwt
     */
    JWT getSignedAuthRequestObject(String scopeString,
                                   ClientID clientId,
                                   Issuer iss,
                                   String consentId,
                                   String state = ConnectorTestConstants.STATE_PARAMETER,
                                   String nonce = ConnectorTestConstants.NONCE_PARAMETER,
                                   String response_type = ConnectorTestConstants.RESPONSE_TYPE_PARAMETER,
                                   String audienceValue = acceleratorConfiguration.getConsentAudienceValue(),
                                   String redirectUrl = acceleratorConfiguration.getAppDCRRedirectUri()) {

        def expiryDate = Instant.now().plus(58, ChronoUnit.MINUTES)
        def notBefore = Instant.now()

        JSONObject critString = new ArrayList<String>() {
            {
                add(ConnectorTestConstants.B64_PARAMETER)
                add(ConnectorTestConstants.CRIT_IAT_URL)
                add(ConnectorTestConstants.CRIT_ISS_URL)
                add(ConnectorTestConstants.CRIT_TAN_URL)
            }
        }

        JSONObject acr = new JSONObject().put(ConnectorTestConstants.ESSENTIAL, true).put(ConnectorTestConstants.VALUES, new ArrayList<String>() {
            {
                add(ConnectorTestConstants.ACR_CA_URL)
                add(ConnectorTestConstants.ACR_SCA_URL)
            }
        })

        JSONObject openbankingIntentString = new JSONObject().put(ConnectorTestConstants.VALUE_PARAMETER, consentId).put(ConnectorTestConstants.ESSENTIAL, true)
        JSONObject claimsString = new JSONObject().put(
                ConnectorTestConstants.ID_TOKEN_PARAMETER,
                new JSONObject().put(ConnectorTestConstants.ACR_PARAMETER, acr)
                        .put(ConnectorTestConstants.INTENT_ID_PARAMETER, openbankingIntentString)
        )
                .put(ConnectorTestConstants.USER_INFO_PARAMETER, new JSONObject().put(ConnectorTestConstants.INTENT_ID_PARAMETER, openbankingIntentString))

        String claims = new JSONRequestGenerator()
                .addCustomJson(ConnectorTestConstants.CRIT_PARAMETER, critString)
                .addAudience(audienceValue)
                .addScope(scopeString)
                .addExpireDate(expiryDate.getEpochSecond().toLong())
                .addCustomJson(ConnectorTestConstants.CLAIMS_PARAMETER, claimsString)
                .addIssuer(iss.toString())
                .addResponseType()
                .addRedirectURI()
                .addState(UUID.randomUUID().toString())
                .addNonce()
                .addClientID(clientId.toString())
                .addCustomValue("nbf", notBefore.getEpochSecond().toLong())
                .getJsonObject().toString()

        String payload = getSignedRequestObject(claims)

        Reporter.log("Authorisation Request Object")
        Reporter.log("JWS Payload ${new Payload(claims).toString()}")

        return SignedJWT.parse(payload)
    }

    /**
     * Return signed JWT for Authorization request
     * @param scopeString
     * @param clientID
     * @param iss
     * @param state
     * @param nonce
     * @param response_type
     * @param audienceValue
     * @param redirectUrl
     * @return signed jwt
     */
    JWT getSignedRequestObjectWithDefinedCert(String scopeString,
                                              ClientID clientId,
                                              Issuer iss,
                                              String consentId,
                                              String state = ConnectorTestConstants.STATE_PARAMETER,
                                              String nonce = ConnectorTestConstants.NONCE_PARAMETER,
                                              String response_type = ConnectorTestConstants.RESPONSE_TYPE_PARAMETER,
                                              String audienceValue = ConnectorTestConstants.AUD_PARAMETER,
                                              String redirectUrl = ConnectorTestConstants.REDIRECT_URI_PARAMETER,
                                              String appKeystoreLocation, String appKeystorePassword,
                                              String appKeystoreAlias) {

        def expiryDate = Instant.now().plus(58, ChronoUnit.MINUTES)
        def notBefore = Instant.now()

        JSONObject critString = new ArrayList<String>() {
            {
                add(ConnectorTestConstants.B64_PARAMETER)
                add(ConnectorTestConstants.CRIT_IAT_URL)
                add(ConnectorTestConstants.CRIT_ISS_URL)
                add(ConnectorTestConstants.CRIT_TAN_URL)
            }
        }

        JSONObject acr = new JSONObject().put(ConnectorTestConstants.ESSENTIAL, true).put(ConnectorTestConstants.VALUES, new ArrayList<String>() {
            {
                add(ConnectorTestConstants.ACR_CA_URL)
                add(ConnectorTestConstants.ACR_SCA_URL)
            }
        })

        JSONObject openbankingIntentString = new JSONObject().put(ConnectorTestConstants.VALUE_PARAMETER, consentId).put(ConnectorTestConstants.ESSENTIAL, true)
        JSONObject claimsString = new JSONObject().put(
                ConnectorTestConstants.ID_TOKEN_PARAMETER,
                new JSONObject().put(ConnectorTestConstants.ACR_PARAMETER, acr)
                        .put(ConnectorTestConstants.INTENT_ID_PARAMETER, openbankingIntentString)
        )
                .put(ConnectorTestConstants.USER_INFO_PARAMETER, new JSONObject().put(ConnectorTestConstants.INTENT_ID_PARAMETER, openbankingIntentString))

        String claims = new JSONRequestGenerator()
                .addCustomJson(ConnectorTestConstants.CRIT_PARAMETER, critString)
                .addAudience()
                .addScope(scopeString)
                .addExpireDate(expiryDate.getEpochSecond().toLong())
                .addCustomJson(ConnectorTestConstants.CLAIMS_PARAMETER, claimsString)
                .addIssuer(iss.toString())
                .addResponseType()
                .addRedirectURI()
                .addState(UUID.randomUUID().toString())
                .addNonce()
                .addClientID(clientId.toString())
                .addCustomValue("nbf", notBefore.getEpochSecond().toLong())
                .getJsonObject().toString()

        String payload = getSignedRequestObject(claims)

        Reporter.log("Authorisation Request Object")
        Reporter.log("JWS Payload ${new Payload(claims).toString()}")

        return SignedJWT.parse(SignedObject.getSignedRequestObjectWithDefinedCert(claims,
                (new ConfigurationService()).commonSigningAlgorithm, appKeystoreLocation,
                appKeystorePassword, appKeystoreAlias))
    }

    JWT getSignedAuthRequestObjectWithCustomAlgorithm(String scopeString,
                                                      ClientID clientId,
                                                      Issuer iss,
                                                      String consentId,
                                                      String state = ConnectorTestConstants.STATE_PARAMETER,
                                                      String nonce = ConnectorTestConstants.NONCE_PARAMETER,
                                                      String response_type = ConnectorTestConstants.RESPONSE_TYPE_PARAMETER,
                                                      String audienceValue = ConnectorTestConstants.AUD_PARAMETER,
                                                      String redirectUrl = ConnectorTestConstants.REDIRECT_URI_PARAMETER) {

        def expiryDate = Instant.now().plus(58, ChronoUnit.MINUTES)
        def notBefore = Instant.now()

        JSONObject critString = new ArrayList<String>() {
            {
                add(ConnectorTestConstants.B64_PARAMETER)
                add(ConnectorTestConstants.CRIT_IAT_URL)
                add(ConnectorTestConstants.CRIT_ISS_URL)
                add(ConnectorTestConstants.CRIT_TAN_URL)
            }
        }

        JSONObject acr = new JSONObject().put(ConnectorTestConstants.ESSENTIAL, true).put(ConnectorTestConstants.VALUES, new ArrayList<String>() {
            {
                add(ConnectorTestConstants.ACR_CA_URL)
                add(ConnectorTestConstants.ACR_SCA_URL)
            }
        })

        JSONObject openbankingIntentString = new JSONObject().put(ConnectorTestConstants.VALUE_PARAMETER, consentId).put(ConnectorTestConstants.ESSENTIAL, true)
        JSONObject claimsString = new JSONObject().put(
                ConnectorTestConstants.ID_TOKEN_PARAMETER,
                new JSONObject().put(ConnectorTestConstants.ACR_PARAMETER, acr)
                        .put(ConnectorTestConstants.INTENT_ID_PARAMETER, openbankingIntentString)
        )
                .put(ConnectorTestConstants.USER_INFO_PARAMETER, new JSONObject().put(ConnectorTestConstants.INTENT_ID_PARAMETER, openbankingIntentString))

        String claims = new JSONRequestGenerator()
                .addCustomJson(ConnectorTestConstants.CRIT_PARAMETER, critString)
                .addAudience()
                .addScope(scopeString)
                .addExpireDate(expiryDate.getEpochSecond().toLong())
                .addCustomJson(ConnectorTestConstants.CLAIMS_PARAMETER, claimsString)
                .addIssuer(iss.toString())
                .addResponseType()
                .addRedirectURI()
                .addState(UUID.randomUUID().toString())
                .addNonce()
                .addClientID(clientId.toString())
                .addCustomValue("nbf", notBefore.getEpochSecond().toLong())
                .getJsonObject().toString()

        String payload = getSignedRequestObjectWithCustomAlgorithm(claims)

        Reporter.log("Authorisation Request Object")
        Reporter.log("JWS Payload ${new Payload(claims).toString()}")

        return SignedJWT.parse(payload)
    }

    /**
     * Return JWT for application access token generation
     * @param clientId
     * @return
     * @throws org.wso2.bfsi.test.framework.exception.TestFrameworkException
     */
    String getAppAccessTokenJwt(String authMethodType, String clientId = null) throws TestFrameworkException {

        if(authMethodType == ConnectorTestConstants.TLS_AUTH_METHOD){
            String accessTokenJWT = new PayloadGenerator().addGrantType().addScopes(scopesList).addClientID(clientId)
                    .addRedirectUri().getPayload()
            return accessTokenJWT
        } else {
            //Adding Client Assertion for other Auth Method types
            JSONObject clientAssertion = new JSONRequestGenerator().addIssuer(clientId)
                    .addSubject(clientId).addAudience().addExpireDate().addIssuedAt().addJti().getJsonObject()

            String payload = getSignedRequestObject(clientAssertion.toString())
            String accessTokenJWT = new PayloadGenerator().addGrantType().addScopes(scopesList).addClientAsType()
                    .addClientID(clientId).addClientAssertion(payload).addRedirectUri().getPayload()
            return accessTokenJWT
        }
    }

    /**
     * Return JWT for user access token generation
     * @param code
     * @return
     * @throws TestFrameworkException
     */
    String getUserAccessTokenJwt(String authMethodType, String clientId = null, String code = "")
            throws TestFrameworkException {
        if(authMethodType == ConnectorTestConstants.TLS_AUTH_METHOD){
            String accessTokenJWT = new PayloadGenerator().addGrantType(ConnectorTestConstants.AUTH_CODE).addCode(code)
                    .addScopes(scopesList).addClientID(clientId).addRedirectUri().getPayload()
            return accessTokenJWT
        }

        //Adding Client Assertion for other Auth Method types
        else{
            JSONObject clientAssertion = new JSONRequestGenerator().addIssuer(clientId)
                    .addSubject(clientId).addAudience().addExpireDate().addIssuedAt().addJti().getJsonObject()

            String payload = getSignedRequestObject(clientAssertion.toString())
            String accessTokenJWT = new PayloadGenerator().addGrantType(ConnectorTestConstants.AUTH_CODE).addCode(code)
                    .addScopes(scopesList).addClientAsType().addClientAssertion(payload).addRedirectUri().getPayload()
            return accessTokenJWT
        }
    }

    /**
     * Return JWT for user access token generation
     * @param authMethodType
     * @param clientId
     * @param username
     * @param password
     * @return
     * @throws TestFrameworkException
     */
    String getPasswordAccessTokenPayload(String authMethodType, String clientId, String username, String password)
            throws TestFrameworkException {

        if (authMethodType == ConnectorTestConstants.TLS_AUTH_METHOD) {
            String accessTokenPayload = new PayloadGenerator()
                    .addGrantType(ConnectorTestConstants.PASSWORD)
                    .addScopes(scopesList)
                    .addClientID()
                    .addRedirectUri()
                    .addUserName(username)
                    .addPassword(password)
                    .getPayload()
            return accessTokenPayload
        }

        //Adding Client Assertion for other Auth Method types
        else{
            JSONObject clientAssertion = new JSONRequestGenerator()
                    .addIssuer(clientId)
                    .addSubject(clientId)
                    .addAudience()
                    .addExpireDate()
                    .addIssuedAt()
                    .addJti()
                    .getJsonObject()

            String payload = getSignedRequestObject(clientAssertion.toString())
            String accessTokenPayload = new PayloadGenerator()
                    .addGrantType(ConnectorTestConstants.PASSWORD)
                    .addScopes(scopesList)
                    .addClientAsType()
                    .addClientAssertion(payload)
                    .addRedirectUri()
                    .addUserName(username)
                    .addPassword(password)
                    .getPayload()
            return accessTokenPayload
        }
    }

    /**
     * Return payload for refresh token generation
     * @param authMethodType
     * @param clientId
     * @return
     * @throws TestFrameworkException
     */
    String getRefreshAccessTokenPayload(String authMethodType, String clientId, String refreshToken)
            throws TestFrameworkException {

        if (authMethodType == ConnectorTestConstants.TLS_AUTH_METHOD) {
            String accessTokenPayload = new PayloadGenerator()
                    .addGrantType(ConnectorTestConstants.REFRESH_TOKEN)
                    .addScopes(scopesList)
                    .addClientID(clientId)
                    .addRedirectUri()
                    .addRefreshToken(refreshToken)
                    .getPayload()
            return accessTokenPayload
        }

        //Adding Client Assertion for other Auth Method types
        else{
            JSONObject clientAssertion = new JSONRequestGenerator()
                    .addIssuer(clientId)
                    .addSubject(clientId)
                    .addAudience()
                    .addExpireDate()
                    .addIssuedAt()
                    .addJti()
                    .getJsonObject()

            String payload = getSignedRequestObject(clientAssertion.toString())
            String accessTokenPayload = new PayloadGenerator()
                    .addGrantType(ConnectorTestConstants.REFRESH_TOKEN)
                    .addScopes(scopesList)
                    .addClientAsType()
                    .addClientAssertion(payload)
                    .addRedirectUri()
                    .addRefreshToken(refreshToken)
                    .getPayload()
            return accessTokenPayload
        }
    }

    /**
     * Return JWT for Authorization request without consent id
     * @param scopeString
     * @param clientId
     * @param iss
     * @param state
     * @param nonce
     * @param response_type
     * @param audienceValue
     * @param redirectUrl
     * @return
     */
    JWT getSignedAuthRequestObjectWithoutConsentId(String scopeString,
                                                   ClientID clientId,
                                                   Issuer iss,
                                                   String state = ConnectorTestConstants.STATE_PARAMETER,
                                                   String nonce = ConnectorTestConstants.NONCE_PARAMETER,
                                                   String response_type = ConnectorTestConstants.RESPONSE_TYPE_PARAMETER,
                                                   String audienceValue = acceleratorConfiguration.getConsentAudienceValue(),
                                                   String redirectUrl = acceleratorConfiguration.getAppDCRRedirectUri()) {

        def expiryDate = Instant.now().plus(58, ChronoUnit.MINUTES)
        def notBefore = Instant.now()

        JSONObject critString = new ArrayList<String>() {
            {
                add(ConnectorTestConstants.B64_PARAMETER)
                add(ConnectorTestConstants.CRIT_IAT_URL)
                add(ConnectorTestConstants.CRIT_ISS_URL)
                add(ConnectorTestConstants.CRIT_TAN_URL)
            }
        }

        JSONObject acr = new JSONObject().put(ConnectorTestConstants.ESSENTIAL, true).put(ConnectorTestConstants.VALUES, new ArrayList<String>() {
            {
                add(ConnectorTestConstants.ACR_CA_URL)
                add(ConnectorTestConstants.ACR_SCA_URL)
            }
        })

        JSONObject claimsString = new JSONObject().put(
                ConnectorTestConstants.ID_TOKEN_PARAMETER,
                new JSONObject().put(ConnectorTestConstants.ACR_PARAMETER, acr)
        )

        String claims = new JSONRequestGenerator()
                .addCustomJson(ConnectorTestConstants.CRIT_PARAMETER, critString)
                .addAudience(audienceValue)
                .addScope(scopeString)
                .addExpireDate(expiryDate.getEpochSecond().toLong())
                .addCustomJson(ConnectorTestConstants.CLAIMS_PARAMETER, claimsString)
                .addIssuer(iss.toString())
                .addResponseType()
                .addRedirectURI()
                .addState(UUID.randomUUID().toString())
                .addNonce()
                .addClientID(clientId.toString())
                .addCustomValue("nbf", notBefore.getEpochSecond().toLong())
                .getJsonObject().toString()

        String payload = getSignedRequestObject(claims)

        Reporter.log("Authorisation Request Object")
        Reporter.log("JWS Payload ${new Payload(claims).toString()}")

        return SignedJWT.parse(payload)
    }

    /**
     * Return payload for refresh token generation
     * @param authMethodType
     * @param refreshToken
     * @return
     * @throws TestFrameworkException
     */
    String getRefreshAccessTokenPayloadWithoutClientId(String refreshToken)
            throws TestFrameworkException {

        String accessTokenPayload = new PayloadGenerator()
                .addGrantType(ConnectorTestConstants.REFRESH_TOKEN)
                .addScopes(scopesList)
                .addRedirectUri()
                .addRefreshToken(refreshToken)
                .getPayload()
        return accessTokenPayload
    }

    /**
     * Get Client Assertion.
     * @param clientId - Client ID
     * @return clientAssertion - Client Assertion
     */
    String getClientAssertionJwt(String clientId=null) {
        JSONObject clientAssertion = new JSONRequestGenerator().addIssuer(clientId)
                .addSubject(clientId).addAudience().addExpireDate().addIssuedAt().addJti().getJsonObject()

        String payload = getSignedRequestObject(clientAssertion.toString())
        return payload
    }

    /**
     * Get Client Assertion without IAT.
     * @param clientId
     * @return
     */
    String getClientAssertionJwtWithoutIAT(String clientId=null) {
        JSONObject clientAssertion = new JSONRequestGenerator().addIssuer(clientId)
                .addSubject(clientId).addAudience().addExpireDate().addJti().getJsonObject()

        String payload = getSignedRequestObject(clientAssertion.toString())
        return payload
    }

    /**
     * Get Client Assertion with customized Issuer and Audience
     * @param issuer Issuer
     * @param audience Audience
     * @return jwt
     */
    String getClientAssertionJwt(String issuer, String audience) {
        JSONObject clientAssertion = new JSONRequestGenerator().addIssuer(issuer)
                .addSubject(issuer).addAudience(audience).addExpireDate().addIssuedAt().addJti().getJsonObject()

        String payload = getSignedRequestObject(clientAssertion.toString())
        return payload
    }

    /**
     * Get Request Object for Pushed Authorisation Request.
     * @param scopeString
     * @param consentId
     * @param redirect_uri
     * @param clientId
     * @param responseType
     * @param isStateRequired
     * @param state
     * @param responseMode
     * @param expiryDate
     * @param notBefore
     * @param codeChallengeMethod
     * @return
     */
    JWT getRequestObjectClaim(String scopeString, String consentId, String redirect_uri, String clientId, String responseType,
                              boolean isStateRequired = true, String state,
                              Instant expiryDate = Instant.now().plus(1, ChronoUnit.HOURS),
                              Instant notBefore = Instant.now(), CodeChallengeMethod codeChallengeMethod = CodeChallengeMethod.S256) {
        String claims

        //Generate Code Challenge
        CodeChallenge codeChallenge = CodeChallenge.compute(codeChallengeMethod, ConnectorTestConstants.CODE_VERIFIER)
        String codeChallengeValue = codeChallenge.getValue()

        //Define additional claims
        JSONObject acr = new JSONObject().put(ConnectorTestConstants.ESSENTIAL, true).put(ConnectorTestConstants.VALUES, new ArrayList<String>() {
            {
                add(ConnectorTestConstants.ACR_CA_URL)
                add(ConnectorTestConstants.ACR_SCA_URL)
            }
        })
        JSONObject openbankingIntentString = new JSONObject().put(ConnectorTestConstants.VALUE_PARAMETER, consentId).put(ConnectorTestConstants.ESSENTIAL, true)
        JSONObject authTimeString = new JSONObject().put("essential", true)
        JSONObject maxAgeString = new JSONObject().put("essential", true).put("max_age", 86400)
        JSONObject userInfoString = new JSONObject().put("name", null).put("given_name", null).put("family_name", null).put("updated_at", Instant.now())
        JSONObject claimsString = new JSONObject().put("id_token", new JSONObject().put("acr", acr).put("auth_time", authTimeString).put(ConnectorTestConstants.INTENT_ID_PARAMETER, openbankingIntentString))
                .put(ConnectorTestConstants.USER_INFO_PARAMETER, new JSONObject().put(ConnectorTestConstants.INTENT_ID_PARAMETER, openbankingIntentString))


        if (isStateRequired) {
            claims = new JSONRequestGenerator()
                    .addAudience()
                    .addResponseType(responseType)
                    .addExpireDate(expiryDate.getEpochSecond().toLong())
                    .addClientID(clientId)
                    .addIssuer(clientId)
                    .addRedirectURI(redirect_uri)
                    .addScope(scopeString)
                    .addState(state)
                    .addNonce()
                    .addCustomValue("max_age", maxAgeString)
                    .addCustomValue("nbf", notBefore.getEpochSecond().toLong())
                    .addCustomJson("claims", claimsString)
                    .addCustomValue("userinfo", userInfoString)
                    .addCustomValue("code_challenge_method", codeChallengeMethod)
                    .addCustomValue("code_challenge", codeChallengeValue)
                    .getJsonObject().toString()
        } else {
            claims = new JSONRequestGenerator()
                    .addAudience()
                    .addResponseType(responseType)
                    .addExpireDate(expiryDate.getEpochSecond().toLong())
                    .addClientID(clientId)
                    .addIssuer(clientId)
                    .addRedirectURI(redirect_uri)
                    .addScope(scopeString)
                    .addNonce()
                    .addCustomValue("max_age", maxAgeString)
                    .addCustomValue("nbf", notBefore.getEpochSecond().toLong())
                    .addCustomJson("claims", claimsString)
                    .addCustomValue("userinfo", userInfoString)
                    .addCustomValue("code_challenge_method", codeChallengeMethod)
                    .addCustomValue("code_challenge", codeChallengeValue)
                    .getJsonObject().toString()
        }

        String payload = getSignedRequestObject(claims)

        Reporter.log("Authorisation Request Object")
        Reporter.log("JWS Payload ${new Payload(claims).toString()}")

        return SignedJWT.parse(payload)
    }

    /**
     * Return JWT for user access token generation with Code Verifier
     * @param authMethodType
     * @param clientId
     * @param code
     * @return
     * @throws TestFrameworkException
     */
    String getUserAccessTokenJwtWithCodeVerifier(String authMethodType, String clientId = null, String code = "")
            throws TestFrameworkException {
        if(authMethodType == ConnectorTestConstants.TLS_AUTH_METHOD){
            String accessTokenJWT = new PayloadGenerator().addGrantType(ConnectorTestConstants.AUTH_CODE).addCode(code)
                    .addScopes(scopesList).addClientID(clientId).addRedirectUri()
                    .addCodeVerifier(ConnectorTestConstants.CODE_VERIFIER.getValue()).getPayload()
            return accessTokenJWT
        }

        //Adding Client Assertion for other Auth Method types
        else{
            JSONObject clientAssertion = new JSONRequestGenerator().addIssuer(clientId)
                    .addSubject(clientId).addAudience().addExpireDate().addIssuedAt().addJti().getJsonObject()

            String payload = getSignedRequestObject(clientAssertion.toString())
            String accessTokenJWT = new PayloadGenerator().addGrantType(ConnectorTestConstants.AUTH_CODE).addCode(code)
                    .addScopes(scopesList).addClientAsType().addClientAssertion(payload).addRedirectUri()
                    .addCodeVerifier(ConnectorTestConstants.CODE_VERIFIER.getValue()).getPayload()
            return accessTokenJWT
        }
    }

    /**
     * Remove claims from request object
     * @param claims
     * @param nodeToBeRemoved
     * @return
     */
    static String removeClaimsFromRequestObject(String claims, String nodeToBeRemoved) {

        // Parse the JSON payload
        ObjectMapper objectMapper = new ObjectMapper()
        JsonNode rootNode = objectMapper.readTree(claims)

        // Remove elements from the JSON payload
        if (rootNode instanceof ObjectNode) {
            ObjectNode objectNode = (ObjectNode) rootNode
            objectNode.remove(nodeToBeRemoved)
        }

        // Convert the modified JSON back to a string
        String modifiedJsonPayload = objectMapper.writeValueAsString(rootNode)
        System.out.println(modifiedJsonPayload)

        return modifiedJsonPayload
    }

    /**
     * Get Request Object for Pushed Authorisation Request.
     * @param scopeString
     * @param consentId
     * @param redirect_uri
     * @param clientId
     * @param responseType
     * @param isStateRequired
     * @param state
     * @param expiryDate
     * @param notBefore
     * @param codeChallengeMethod
     * @return
     */
    String getRequestObjectClaimString(String scopeString, String consentId, String redirect_uri, String clientId, String responseType,
                              boolean isStateRequired = true, String state,
                              Instant expiryDate = Instant.now().plus(1, ChronoUnit.HOURS),
                              Instant notBefore = Instant.now(), CodeChallengeMethod codeChallengeMethod = CodeChallengeMethod.S256) {
        String claims

        //Generate Code Challenge
        CodeChallenge codeChallenge = CodeChallenge.compute(codeChallengeMethod, ConnectorTestConstants.CODE_VERIFIER)
        String codeChallengeValue = codeChallenge.getValue()

        //Define additional claims
        JSONObject acr = new JSONObject().put(ConnectorTestConstants.ESSENTIAL, true).put(ConnectorTestConstants.VALUES, new ArrayList<String>() {
            {
                add(ConnectorTestConstants.ACR_CA_URL)
                add(ConnectorTestConstants.ACR_SCA_URL)
            }
        })
        JSONObject openbankingIntentString = new JSONObject().put(ConnectorTestConstants.VALUE_PARAMETER, consentId).put(ConnectorTestConstants.ESSENTIAL, true)
        JSONObject authTimeString = new JSONObject().put("essential", true)
        JSONObject maxAgeString = new JSONObject().put("essential", true).put("max_age", 86400)
        JSONObject userInfoString = new JSONObject().put("name", null).put("given_name", null).put("family_name", null).put("updated_at", Instant.now())
        JSONObject claimsString = new JSONObject().put("id_token", new JSONObject().put("acr", acr).put("auth_time", authTimeString).put(ConnectorTestConstants.INTENT_ID_PARAMETER, openbankingIntentString))
                .put(ConnectorTestConstants.USER_INFO_PARAMETER, new JSONObject().put(ConnectorTestConstants.INTENT_ID_PARAMETER, openbankingIntentString))


        if (isStateRequired) {
            claims = new JSONRequestGenerator()
                    .addAudience()
                    .addResponseType(responseType)
                    .addExpireDate(expiryDate.getEpochSecond().toLong())
                    .addClientID(clientId)
                    .addIssuer(clientId)
                    .addRedirectURI(redirect_uri)
                    .addScope(scopeString)
                    .addState(state)
                    .addNonce()
                    .addCustomValue("max_age", maxAgeString)
                    .addCustomValue("nbf", notBefore.getEpochSecond().toLong())
                    .addCustomJson("claims", claimsString)
                    .addCustomValue("userinfo", userInfoString)
                    .addCustomValue("code_challenge_method", codeChallengeMethod)
                    .addCustomValue("code_challenge", codeChallengeValue)
                    .getJsonObject().toString()
        } else {
            claims = new JSONRequestGenerator()
                    .addAudience()
                    .addResponseType(responseType)
                    .addExpireDate(expiryDate.getEpochSecond().toLong())
                    .addClientID(clientId)
                    .addIssuer(clientId)
                    .addRedirectURI(redirect_uri)
                    .addScope(scopeString)
                    .addNonce()
                    .addCustomValue("max_age", maxAgeString)
                    .addCustomValue("nbf", notBefore.getEpochSecond().toLong())
                    .addCustomJson("claims", claimsString)
                    .addCustomValue("userinfo", userInfoString)
                    .addCustomValue("code_challenge_method", codeChallengeMethod)
                    .addCustomValue("code_challenge", codeChallengeValue)
                    .getJsonObject().toString()
        }

        return claims
    }

    /**
     * Add claims to request object
     * @param claims
     * @param updatedClaim
     * @param nodeToBeAdded
     * @return
     */
    static String addClaimsFromRequestObject(String claims, String updatedClaim, String nodeToBeAdded) {

        JSONObject payload = new JSONObject(claims)
        try {
            payload.put(updatedClaim, nodeToBeAdded)
        } catch (JSONException e) {
            e.printStackTrace()
        }
        return payload.toString()
    }
}
