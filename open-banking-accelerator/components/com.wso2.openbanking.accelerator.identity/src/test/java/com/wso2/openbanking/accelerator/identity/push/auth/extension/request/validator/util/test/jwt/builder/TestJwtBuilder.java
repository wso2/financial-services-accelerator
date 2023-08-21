/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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
package com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.util.test.jwt.builder;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.util.test.jwt.builder.constants.TestJwtBuilderConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Builder class to Automate the process of building a JWT for Testing purposes.
 *
 */
public class TestJwtBuilder {
    private static String issuer = "wHKH6jd5YRJtG_CXSLVfcStMfOAa";
    private static String responseType = "code id_token";
    private static String codeChallengeMethod =  "S256";
    private static String nonce = "n-jBXhOmOKCB";
    private static String invalidNonce = null;
    private static String clientId = "wHKH6jd5YRJtG_CXSLVfcStMfOAa";
    private static String audience = "https://localhost:9443/oauth2/token";
    private static String scope = "bank:accounts.basic:read bank:transactions:read " +
            "common:customer.detail:read openid";
    private static String redirectUri = "https://www.google.com/redirects/redirect1";
    private static String state = "0pN0NBTHcv";
    private static String codeChallenge = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM";
    private static String kid = "W_TcnQVcHAy20q8zCMcdByrootw";
    private static String alias = "wso2carbon";
    private static String keyPassword = "wso2carbon";
    private static String acrValue = "urn:cds.au:cdr:2";
    private static String invalidParameter = "invalidParameter";
    private static boolean acrEssential = true;
    private static int sharingDuration = 7776000;
    private static int expiryPeriod = 3600;
    private static int notBeforePeriod = 3600;
    private static String keyStorePath = "src/test/resources";

    private TestJwtBuilder() {
    }

    /**
     * This method is used to get a valid signed JWT with signature algorithm PS256.
     *
     * @return String
     * @throws Exception if an error occurs
     */
    public static String getValidSignedJWT() throws Exception {
        JWTClaimsSet claimsSet = getValidJWTClaimsSetBuilder().build();
        SignedJWT signedJWT = getSignedJWT(JWSAlgorithm.PS256, claimsSet);
        return signedJWT.serialize();
    }

    /**
     * This method is used to get a valid encrypted JWT with signature algorithm PS256,
     * encryption algorithm RSA-OAEP-256 and encryption method A256GCM.
     *
     * @return String
     * @throws Exception if an error occurs
     */
    public static String getValidEncryptedJWT() throws Exception {
        JWTClaimsSet claimsSet = getValidJWTClaimsSetBuilder().build();
        SignedJWT signedJWT = getSignedJWT(JWSAlgorithm.PS256 , claimsSet);
        return getEncryptedJWT(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM, signedJWT).serialize();
    }

    /**
     * This method is used to get a invalid JWT signed with unsupported signature algorithm.
     * ex: RS256
     *
     * @return String
     * @throws Exception if an error occurs
     */
    public static String getInvalidJWTWithUnsupportedAlgorithm() throws Exception {
        JWTClaimsSet claimsSet = getValidJWTClaimsSetBuilder().build();
        SignedJWT signedJWT = getSignedJWT(JWSAlgorithm.RS256, claimsSet);
        return signedJWT.serialize();
    }

    /**
     * This method is used to get a invalid JWT with invalid nonce value.
     * ex: null
     *
     * @return String
     * @throws Exception if an error occurs
     */
    public static String getInvalidJWTWithUnsupportedNonce() throws Exception {

        JWTClaimsSet.Builder builder = getValidJWTClaimsSetBuilder();
        JWTClaimsSet invalidClaimSet = builder.claim(TestJwtBuilderConstants.NONCE, invalidNonce).build();
        SignedJWT signedJWT = getSignedJWT(JWSAlgorithm.PS256, invalidClaimSet);
        return signedJWT.serialize();
    }

    /**
     * This method is used to get an invalid JWT with unsupported claims value.
     *
     * @return String
     * @throws Exception if an error occurs
     */
    public static String getInvalidJWTWithUnsupportedClaims() throws Exception {
        Map<String, Object> invalidClaimsMap = getValidClaimsMap();
        JWTClaimsSet.Builder builder = getValidJWTClaimsSetBuilder();
        JWTClaimsSet invalidClaimSet = builder
                .claim(TestJwtBuilderConstants.CLAIMS, invalidClaimsMap)
                .claim(TestJwtBuilderConstants.REQUEST, invalidParameter)
                .build();
        SignedJWT signedJWT = getSignedJWT(JWSAlgorithm.PS256, invalidClaimSet);
        return signedJWT.serialize();
    }

    /**
     * This method is used to get an invalid JWT with exp claim over 60 minutes in the future.
     *
     * @return String
     * @throws Exception if an error occurs
     */
    public static String getInvalidJWTWithExpClaimOver60Min() throws Exception {
        JWTClaimsSet.Builder builder = getValidJWTClaimsSetBuilder();
        JWTClaimsSet invalidClaimSet = builder
                .expirationTime(Date.from(Instant.now().plusSeconds(expiryPeriod + 1500)))
                .build();
        SignedJWT signedJWT = getSignedJWT(JWSAlgorithm.PS256, invalidClaimSet);
        return signedJWT.serialize();
    }

    /**
     * This method is used to get an invalid JWT without an exp claim.
     *
     * @return String
     * @throws Exception if an error occurs
     */
    public static String getInvalidJWTWithoutExpClaim() throws Exception {
        JWTClaimsSet.Builder builder = getValidJWTClaimsSetBuilder();
        JWTClaimsSet invalidClaimSet = builder
                .expirationTime(null)
                .build();
        SignedJWT signedJWT = getSignedJWT(JWSAlgorithm.PS256, invalidClaimSet);
        return signedJWT.serialize();
    }

    /**
     * This method is used to get an invalid JWT without the code challenge.
     *
     * @return String
     * @throws Exception if an error occurs
     */
    public static String getInvalidJWTWithoutCodeChallenge() throws Exception {

        JWTClaimsSet.Builder builder = getValidJWTClaimsSetBuilder();
        JWTClaimsSet invalidClaimSet = builder
                .claim(TestJwtBuilderConstants.CODE_CHALLENGE, null)
                .build();
        SignedJWT signedJWT = getSignedJWT(JWSAlgorithm.PS256, invalidClaimSet);
        return signedJWT.serialize();
    }

    /**
     * This method is used to get an invalid JWT without the code challenge method.
     *
     * @return String
     * @throws Exception if an error occurs
     */
    public static String getInvalidJWTWithoutCodeChallengeMethod() throws Exception {

        JWTClaimsSet.Builder builder = getValidJWTClaimsSetBuilder();
        JWTClaimsSet invalidClaimSet = builder
                .claim(TestJwtBuilderConstants.CODE_CHALLENGE_METHOD, null)
                .build();
        SignedJWT signedJWT = getSignedJWT(JWSAlgorithm.PS256, invalidClaimSet);
        return signedJWT.serialize();
    }

    /**
     * This method is used to get an invalid JWT without the response type.
     *
     * @return String
     * @throws Exception if an error occurs
     */
    public static String getInvalidJWTWithoutResponseType() throws Exception {

        JWTClaimsSet.Builder builder = getValidJWTClaimsSetBuilder();
        JWTClaimsSet invalidClaimSet = builder
                .claim(TestJwtBuilderConstants.RESPONSE_TYPE, null)
                .build();
        SignedJWT signedJWT = getSignedJWT(JWSAlgorithm.PS256, invalidClaimSet);
        return signedJWT.serialize();
    }

    /**
     * This method is used to get an invalid JWT without nbf claim.
     *
     * @return String
     * @throws Exception if an error occurs
     */
    public static String getInvalidJWTWithoutNbfClaim() throws Exception {
        JWTClaimsSet.Builder builder = getValidJWTClaimsSetBuilder();
        JWTClaimsSet invalidClaimSet = builder
                .notBeforeTime(null)
                .build();
        SignedJWT signedJWT = getSignedJWT(JWSAlgorithm.PS256, invalidClaimSet);
        return signedJWT.serialize();
    }

    /**
     * This method is used to get an invalid JWT with nbf claim over 60 minutes in the past.
     *
     * @return String
     * @throws Exception if an error occurs
     */
    public static String getInvalidJWTWithNbfClaimOver60Min() throws Exception {
        JWTClaimsSet.Builder builder = getValidJWTClaimsSetBuilder();
        JWTClaimsSet invalidClaimSet = builder
                .notBeforeTime(Date.from(Instant.now().minusSeconds(notBeforePeriod + 1500)))
                .build();
        SignedJWT signedJWT = getSignedJWT(JWSAlgorithm.PS256, invalidClaimSet);
        return signedJWT.serialize();
    }

    /**
     * This method is used to get a valid JWTClaimsSet.Builder Object.
     *
     * @return JWTClaimsSet.Builder
     */
    private static  JWTClaimsSet.Builder getValidJWTClaimsSetBuilder() {
        Map<String, Object> claimsMap = getValidClaimsMap();

        return new JWTClaimsSet.Builder()
                .issuer(issuer)
                .audience(audience)
                .expirationTime(Date.from(Instant.now().plusSeconds(expiryPeriod)))
                .notBeforeTime(Date.from(Instant.now()))
                .claim(TestJwtBuilderConstants.RESPONSE_TYPE, responseType)
                .claim(TestJwtBuilderConstants.CODE_CHALLENGE_METHOD, codeChallengeMethod)
                .claim(TestJwtBuilderConstants.NONCE, nonce)
                .claim(TestJwtBuilderConstants.CLIENT_ID, clientId)
                .claim(TestJwtBuilderConstants.REDIRECT_URI, redirectUri)
                .claim(TestJwtBuilderConstants.SCOPE, scope)
                .claim(TestJwtBuilderConstants.STATE, state)
                .claim(TestJwtBuilderConstants.CLAIMS, claimsMap)
                .claim(TestJwtBuilderConstants.CODE_CHALLENGE, codeChallenge);
    }


    /**
     * This method is used to get a valid claims value as a map object.
     *
     * @return Map<String, Object>
     */
    private static Map<String, Object> getValidClaimsMap() {
        // Create a new HashMap object to represent the claims
        Map<String, Object> claimsMap = new HashMap<>();

        // Add the sharing_duration key-value pair to the claims map
        claimsMap.put(TestJwtBuilderConstants.SHARING_DURATION, sharingDuration);

        // Create a new HashMap object to represent the id_token object
        Map<String, Object> idToken = new HashMap<>();

        // Create a new HashMap object to represent the acr object
        Map<String, Object> acr = new HashMap<>();
        acr.put(TestJwtBuilderConstants.ACR_VALUE, acrValue);
        acr.put(TestJwtBuilderConstants.ACR_ESSENTIAL, acrEssential);

        // Add the acr map to the id_token map
        idToken.put(TestJwtBuilderConstants.ACR, acr);

        // Add the id_token map to the claims map
        claimsMap.put(TestJwtBuilderConstants.ID_TOKEN, idToken);
        return claimsMap;
    }

    /**
     * This method is used to get a valid JWT signed with supported signature algorithm.
     * ex: PS256
     *
     * @param jwsAlgorithm JWSAlgorithm Value
     * @param claimsSet JWTClaimsSet Object
     * @return SignedJWT
     * @throws Exception if an error occurs
     */
    private static SignedJWT getSignedJWT(JWSAlgorithm jwsAlgorithm , JWTClaimsSet claimsSet) throws Exception {
        RSAPrivateKey privateKey = getPrivateKeyFromKeyStore();
        JWSHeader header = new JWSHeader.Builder(jwsAlgorithm)
                .keyID(kid)
                .type(JOSEObjectType.JWT)
                .build();
        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        JWSSigner signer = new RSASSASigner(privateKey);
        signer.getJCAContext().setProvider(BouncyCastleProviderSingleton.getInstance());
        signedJWT.sign(signer);
        return signedJWT;
    }

    /**
     * This method is used to get a valid JWT encrypted with supported encryption algorithm and encryption method.
     * ex: RSA-OAEP-256
     *
     * @param jweAlgorithm JWEAlgorithm Value
     * @param encryptionMethod EncryptionMethod Value
     * @param signedJWT SignedJWT Object
     * @return JWEObject
     * @throws Exception if an error occurs
     */
    private static JWEObject  getEncryptedJWT(JWEAlgorithm jweAlgorithm ,
                                              EncryptionMethod encryptionMethod, SignedJWT signedJWT) throws Exception {
        RSAPublicKey publicKey = getPublicKeyFromKeyStore();
        JWEHeader jweHeader = new JWEHeader.Builder(jweAlgorithm, encryptionMethod)
                .build();
        JWEObject jweObject = new JWEObject(jweHeader, new Payload(signedJWT));
        JWEEncrypter encrypter = new RSAEncrypter(publicKey);
        encrypter.getJCAContext().setProvider(BouncyCastleProviderSingleton.getInstance());
        jweObject.encrypt(encrypter);
        return jweObject;
    }

    /**
     * This method is used to get the private key from the keystore.
     *
     * @return RSAPrivateKey
     * @throws Exception if an error occurs
     */
    private static RSAPrivateKey getPrivateKeyFromKeyStore() throws Exception {
        KeyStore keyStore = getKeyStore();
        KeyStore.PasswordProtection keyProtection = new KeyStore.PasswordProtection(keyPassword.toCharArray());
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, keyProtection);
        return (RSAPrivateKey) privateKeyEntry.getPrivateKey();
    }

    /**
     * This method is used to get the public key from the keystore.
     *
     * @return RSAPublicKey
     * @throws Exception if an error occurs
     */
    private static RSAPublicKey getPublicKeyFromKeyStore() throws Exception {
        KeyStore keyStore = getKeyStore();
        KeyStore.PasswordProtection keyProtection = new KeyStore.PasswordProtection(keyPassword.toCharArray());
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, keyProtection);
        java.security.cert.Certificate[] certChain = privateKeyEntry.getCertificateChain();
        Certificate cert = certChain[0];
        return (RSAPublicKey) cert.getPublicKey();
    }

    /**
     * This method is used to get the keystore.
     *
     * @return KeyStore
     * @throws Exception if an error occurs
     */
    private static KeyStore getKeyStore() throws Exception  {
        File file = new File(keyStorePath);
        String absolutePathForTestResources = file.getAbsolutePath();
        String absolutePathForKeyStore = absolutePathForTestResources + "/wso2carbon.jks";
        String[] pathParts = absolutePathForKeyStore.split("/");
        String platformAbsolutePathForKeyStore = String.join(File.separator, pathParts);
        InputStream keystoreFile = new FileInputStream(platformAbsolutePathForKeyStore);
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(keystoreFile, keyPassword.toCharArray());
        keystoreFile.close();
        return keyStore;
    }
}
