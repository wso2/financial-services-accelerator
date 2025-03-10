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

package org.wso2.financial.services.accelerator.test.framework.request_builder

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
import org.wso2.bfsi.test.framework.exception.TestFrameworkException
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.json.JSONObject
import org.testng.Reporter
import org.wso2.openbanking.test.framework.keystore.OBKeyStore
import org.wso2.openbanking.test.framework.request_builder.JSONRequestGenerator
import org.wso2.openbanking.test.framework.request_builder.PayloadGenerator
import org.wso2.openbanking.test.framework.request_builder.SignedObject
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService
import org.wso2.financial.services.accelerator.test.framework.constant.AcceleratorTestConstants

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

        Certificate certificate = OBKeyStore.getApplicationCertificate()
        String thumbprint = OBKeyStore.getJwkThumbPrintForSHA1(certificate)
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.parse(getSigningAlgorithm()))
                .keyID(thumbprint).type(JOSEObjectType.JWT).build()
        Key signingKey = OBKeyStore.getApplicationSigningKey()

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

        Certificate certificate = OBKeyStore.getApplicationCertificate()
        String thumbprint = OBKeyStore.getJwkThumbPrintForSHA1(certificate)
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.parse("RS256"))
                .keyID(thumbprint).type(JOSEObjectType.JWT).build()
        Key signingKey = OBKeyStore.getApplicationSigningKey()

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
                                   String state = AcceleratorTestConstants.STATE_PARAMETER,
                                   String nonce = AcceleratorTestConstants.NONCE_PARAMETER,
                                   String response_type = AcceleratorTestConstants.RESPONSE_TYPE_PARAMETER,
                                   String audienceValue = acceleratorConfiguration.getConsentAudienceValue(),
                                   String redirectUrl = acceleratorConfiguration.getAppDCRRedirectUri()) {

        def expiryDate = Instant.now().plus(58, ChronoUnit.MINUTES)
        def notBefore = Instant.now()

        JSONObject critString = new ArrayList<String>() {
            {
                add(AcceleratorTestConstants.B64_PARAMETER)
                add(AcceleratorTestConstants.CRIT_IAT_URL)
                add(AcceleratorTestConstants.CRIT_ISS_URL)
                add(AcceleratorTestConstants.CRIT_TAN_URL)
            }
        }

        JSONObject acr = new JSONObject().put(AcceleratorTestConstants.ESSENTIAL, true).put(AcceleratorTestConstants.VALUES, new ArrayList<String>() {
            {
                add(AcceleratorTestConstants.ACR_CA_URL)
                add(AcceleratorTestConstants.ACR_SCA_URL)
            }
        })

        JSONObject openbankingIntentString = new JSONObject().put(AcceleratorTestConstants.VALUE_PARAMETER, consentId).put(AcceleratorTestConstants.ESSENTIAL, true)
        JSONObject claimsString = new JSONObject().put(
                AcceleratorTestConstants.ID_TOKEN_PARAMETER,
                new JSONObject().put(AcceleratorTestConstants.ACR_PARAMETER, acr)
                        .put(AcceleratorTestConstants.INTENT_ID_PARAMETER, openbankingIntentString)
        )
                .put(AcceleratorTestConstants.USER_INFO_PARAMETER, new JSONObject().put(AcceleratorTestConstants.INTENT_ID_PARAMETER, openbankingIntentString))

        String claims = new JSONRequestGenerator()
                .addCustomJson(AcceleratorTestConstants.CRIT_PARAMETER, critString)
                .addAudience(audienceValue)
                .addScope(scopeString)
                .addExpireDate(expiryDate.getEpochSecond().toLong())
                .addCustomJson(AcceleratorTestConstants.CLAIMS_PARAMETER, claimsString)
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
                                              String state = AcceleratorTestConstants.STATE_PARAMETER,
                                              String nonce = AcceleratorTestConstants.NONCE_PARAMETER,
                                              String response_type = AcceleratorTestConstants.RESPONSE_TYPE_PARAMETER,
                                              String audienceValue = AcceleratorTestConstants.AUD_PARAMETER,
                                              String redirectUrl = AcceleratorTestConstants.REDIRECT_URI_PARAMETER,
                                              String appKeystoreLocation, String appKeystorePassword,
                                              String appKeystoreAlias) {

        def expiryDate = Instant.now().plus(58, ChronoUnit.MINUTES)
        def notBefore = Instant.now()

        JSONObject critString = new ArrayList<String>() {
            {
                add(AcceleratorTestConstants.B64_PARAMETER)
                add(AcceleratorTestConstants.CRIT_IAT_URL)
                add(AcceleratorTestConstants.CRIT_ISS_URL)
                add(AcceleratorTestConstants.CRIT_TAN_URL)
            }
        }

        JSONObject acr = new JSONObject().put(AcceleratorTestConstants.ESSENTIAL, true).put(AcceleratorTestConstants.VALUES, new ArrayList<String>() {
            {
                add(AcceleratorTestConstants.ACR_CA_URL)
                add(AcceleratorTestConstants.ACR_SCA_URL)
            }
        })

        JSONObject openbankingIntentString = new JSONObject().put(AcceleratorTestConstants.VALUE_PARAMETER, consentId).put(AcceleratorTestConstants.ESSENTIAL, true)
        JSONObject claimsString = new JSONObject().put(
                AcceleratorTestConstants.ID_TOKEN_PARAMETER,
                new JSONObject().put(AcceleratorTestConstants.ACR_PARAMETER, acr)
                        .put(AcceleratorTestConstants.INTENT_ID_PARAMETER, openbankingIntentString)
        )
                .put(AcceleratorTestConstants.USER_INFO_PARAMETER, new JSONObject().put(AcceleratorTestConstants.INTENT_ID_PARAMETER, openbankingIntentString))

        String claims = new JSONRequestGenerator()
                .addCustomJson(AcceleratorTestConstants.CRIT_PARAMETER, critString)
                .addAudience()
                .addScope(scopeString)
                .addExpireDate(expiryDate.getEpochSecond().toLong())
                .addCustomJson(AcceleratorTestConstants.CLAIMS_PARAMETER, claimsString)
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
                                                      String state = AcceleratorTestConstants.STATE_PARAMETER,
                                                      String nonce = AcceleratorTestConstants.NONCE_PARAMETER,
                                                      String response_type = AcceleratorTestConstants.RESPONSE_TYPE_PARAMETER,
                                                      String audienceValue = AcceleratorTestConstants.AUD_PARAMETER,
                                                      String redirectUrl = AcceleratorTestConstants.REDIRECT_URI_PARAMETER) {

        def expiryDate = Instant.now().plus(58, ChronoUnit.MINUTES)
        def notBefore = Instant.now()

        JSONObject critString = new ArrayList<String>() {
            {
                add(AcceleratorTestConstants.B64_PARAMETER)
                add(AcceleratorTestConstants.CRIT_IAT_URL)
                add(AcceleratorTestConstants.CRIT_ISS_URL)
                add(AcceleratorTestConstants.CRIT_TAN_URL)
            }
        }

        JSONObject acr = new JSONObject().put(AcceleratorTestConstants.ESSENTIAL, true).put(AcceleratorTestConstants.VALUES, new ArrayList<String>() {
            {
                add(AcceleratorTestConstants.ACR_CA_URL)
                add(AcceleratorTestConstants.ACR_SCA_URL)
            }
        })

        JSONObject openbankingIntentString = new JSONObject().put(AcceleratorTestConstants.VALUE_PARAMETER, consentId).put(AcceleratorTestConstants.ESSENTIAL, true)
        JSONObject claimsString = new JSONObject().put(
                AcceleratorTestConstants.ID_TOKEN_PARAMETER,
                new JSONObject().put(AcceleratorTestConstants.ACR_PARAMETER, acr)
                        .put(AcceleratorTestConstants.INTENT_ID_PARAMETER, openbankingIntentString)
        )
                .put(AcceleratorTestConstants.USER_INFO_PARAMETER, new JSONObject().put(AcceleratorTestConstants.INTENT_ID_PARAMETER, openbankingIntentString))

        String claims = new JSONRequestGenerator()
                .addCustomJson(AcceleratorTestConstants.CRIT_PARAMETER, critString)
                .addAudience()
                .addScope(scopeString)
                .addExpireDate(expiryDate.getEpochSecond().toLong())
                .addCustomJson(AcceleratorTestConstants.CLAIMS_PARAMETER, claimsString)
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

        if(authMethodType == AcceleratorTestConstants.TLS_AUTH_METHOD){
            String accessTokenJWT = new PayloadGenerator().addGrantType().addScopes(scopesList).addClientID()
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
        if(authMethodType == AcceleratorTestConstants.TLS_AUTH_METHOD){
            String accessTokenJWT = new PayloadGenerator().addGrantType(AcceleratorTestConstants.AUTH_CODE).addCode(code)
                    .addScopes(scopesList).addClientID().addRedirectUri().getPayload()
            return accessTokenJWT
        }

        //Adding Client Assertion for other Auth Method types
        else{
            JSONObject clientAssertion = new JSONRequestGenerator().addIssuer(clientId)
                    .addSubject(clientId).addAudience().addExpireDate().addIssuedAt().addJti().getJsonObject()

            String payload = getSignedRequestObject(clientAssertion.toString())
            String accessTokenJWT = new PayloadGenerator().addGrantType(AcceleratorTestConstants.AUTH_CODE).addCode(code)
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

        if (authMethodType == AcceleratorTestConstants.TLS_AUTH_METHOD) {
            String accessTokenPayload = new PayloadGenerator()
                    .addGrantType(AcceleratorTestConstants.PASSWORD)
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
                    .addGrantType(AcceleratorTestConstants.PASSWORD)
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

        if (authMethodType == AcceleratorTestConstants.TLS_AUTH_METHOD) {
            String accessTokenPayload = new PayloadGenerator()
                    .addGrantType(AcceleratorTestConstants.REFRESH_TOKEN)
                    .addScopes(scopesList)
                    .addClientID()
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
                    .addGrantType(AcceleratorTestConstants.REFRESH_TOKEN)
                    .addScopes(scopesList)
                    .addClientAsType()
                    .addClientAssertion(payload)
                    .addRedirectUri()
                    .addRefreshToken(refreshToken)
                    .getPayload()
            return accessTokenPayload
        }
    }
}
