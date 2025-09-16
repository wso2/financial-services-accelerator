/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.bfsi.test.framework.request_builder

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.RSASSASigner
import org.json.JSONObject
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.wso2.bfsi.test.framework.configuration.CommonConfigurationService
import org.wso2.bfsi.test.framework.exception.TestFrameworkException
import org.wso2.bfsi.test.framework.keystore.KeyStore

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.security.Key
import java.security.KeyFactory
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.Security
import java.security.UnrecoverableEntryException
import java.security.UnrecoverableKeyException
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.spec.PKCS8EncodedKeySpec

/**
 * Class for get Signed objects
 */
class SignedObject {

    private CommonConfigurationService configuration
    private String signingAlgorithm = null

    SignedObject() {
        configuration = new CommonConfigurationService()
    }

    /**
     * Set signing algorithm
     * @param algorithm
     */
    void setSigningAlgorithm(String algorithm) {
        this.signingAlgorithm = algorithm
    }

    /**
     * Get signing algorithm for methods. IF signing algorithm is null, provide algorithm in configuration
     * @return
     */
    String getSigningAlgorithm() {
        if (signingAlgorithm == null) {
            signingAlgorithm = configuration.getCommonSigningAlgorithm()
        }
        return this.signingAlgorithm
    }

    /**
     *  Get signed basic Client Assertion jwt
     * @param clientId
     * @return
     * @throws TestFrameworkException
     */
    String getJwt(String clientId = null) throws TestFrameworkException {

        JSONObject clientAssertion = new JSONRequestGenerator().addIssuer(clientId)
                .addSubject(clientId).addAudience().addExpireDate().addIssuedAt().addJti().getJsonObject()


        Key signingKey = KeyStore.getApplicationSigningKey()
        Certificate certificate = KeyStore.getApplicationCertificate()
        String thumbprint = KeyStore.getJwkThumbPrintForSHA1(certificate)

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.parse(getSigningAlgorithm()))
                .keyID(thumbprint).type(JOSEObjectType.JWT).build();

        JWSSigner signer = new RSASSASigner((PrivateKey) signingKey);

        JWSObject jwsObject = new JWSObject(header, new Payload(clientAssertion.toString()));

        jwsObject.sign(signer);

        return jwsObject.serialize()

    }

    /**
     * Get Signed Request object for given claims
     * @param claims
     * @return
     * @throws TestFrameworkException
     */
    String getSignedRequest(String claims) throws TestFrameworkException {

        Key signingKey = KeyStore.getApplicationSigningKey()
        Certificate certificate = KeyStore.getApplicationCertificate()
        String thumbprint = KeyStore.getJwkThumbPrintForSHA1(certificate)

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.parse(getSigningAlgorithm()))
                .keyID(thumbprint).type(JOSEObjectType.JWT).build();

        JWSSigner signer = new RSASSASigner((PrivateKey) signingKey);

        JWSObject jwsObject = new JWSObject(header, new Payload(claims));

        jwsObject.sign(signer);

        return jwsObject.serialize()

    }

    /**
     * Get Signed Request object for given claims
     * @param claims
     * @return
     * @throws TestFrameworkException
     */
    String getSignedRequestWithTruststore(String claims) throws TestFrameworkException {

        try (FileInputStream is = new FileInputStream(configuration.getTransportTruststoreLocation())) {
            java.security.KeyStore keystore = java.security.KeyStore.getInstance(java.security.KeyStore.getDefaultType());
            keystore.load(is, configuration.getTransportTruststorePWD().toCharArray());
            Key signingKey = keystore.getKey("iamvalidate", configuration.getTransportTruststorePWD().toCharArray());
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.parse(getSigningAlgorithm()))
                    .type(JOSEObjectType.JWT).build();

            JWSSigner signer = new RSASSASigner((PrivateKey) signingKey);

            JWSObject jwsObject = new JWSObject(header, new Payload(claims));

            jwsObject.sign(signer);

            return jwsObject.serialize()
        } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            log.error("Error occurred while retrieving private key from keystore ", e);
        }
    }

    /**
     * Generate a sign JWT for request object with defined certificates.
     *
     * @param claims claims
     * @param signingAlg signing algorithm
     * @param appKeystoreLocation Application Keystore Location
     * @param appKeystorePassword Application Keystore Password
     * @param appKeystoreAlias Application Keystore Alias
     * @return signed JWT
     */
    static String getSignedRequestObjectWithDefinedCert(String claims, String signingAlg, String appKeystoreLocation,
                                                        String appKeystorePassword, String appKeystoreAlias)
            throws TestFrameworkException {
        try {

            java.security.KeyStore keyStore = KeyStore.getKeyStore(appKeystoreLocation, appKeystorePassword)
            Certificate certificate = KeyStore.getCertificate(keyStore, appKeystoreAlias, appKeystorePassword)
            Key signingKey = KeyStore.getSigningKey(appKeystoreLocation, appKeystorePassword, appKeystoreAlias)

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.parse(signingAlg)).
                    keyID(KeyStore.getJwkThumbPrintForSHA1(certificate)).build();
            Payload payload = new Payload(claims);

            JWSSigner signer = new RSASSASigner((PrivateKey) signingKey);

            Security.addProvider(new BouncyCastleProvider());
            JWSObject jwsObject = new JWSObject(header, new Payload(payload.toString()));
            jwsObject.sign(signer);
            return jwsObject.serialize();

        } catch (IOException e) {
            throw new TestFrameworkException("Failed to load Keystore file from the location", e);
        } catch (CertificateException e) {
            throw new TestFrameworkException("Failed to load Certificate from the keystore", e);
        } catch (NoSuchAlgorithmException e) {
            throw new TestFrameworkException("Failed to identify the Algorithm ", e);
        } catch (KeyStoreException e) {
            throw new TestFrameworkException("Failed to initialize the Keystore ", e);
        } catch (UnrecoverableEntryException e) {
            throw new TestFrameworkException("Error occurred while retrieving values from KeyStore ", e);
        } catch (JOSEException e) {
            throw new TestFrameworkException("Failed to sign the object ", e);
        }
    }

    /**
     * Generate a sign JWT for request object with defined PEM certificate.
     * @param claims
     * @param signingAlg
     * @param certLocation
     * @return
     * @throws TestFrameworkException
     */
    static String getSignedRequestObjectWithDefinedPemCert(String claims, String signingAlg, String certLocation)
            throws TestFrameworkException {
        try {

            // Load RSA private key
            String key = new String(Files.readAllBytes(Paths.get(certLocation)), StandardCharsets.UTF_8);
            key = key.replaceAll("-----BEGIN (.*)-----", "")
                    .replaceAll("-----END (.*)-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded)

            PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(spec)

            // Create signer
            JWSSigner signer = new RSASSASigner(privateKey)
            Security.addProvider(new BouncyCastleProvider())

            // Create JWS header
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.parse(signingAlg))
                    .type(JOSEObjectType.JWT)
                    .build()

            Payload payload = new Payload(claims)

            // Create the JWS object
            JWSObject jwsObject = new JWSObject(header, new Payload(payload.toString()))
            jwsObject.sign(signer)
            return jwsObject.serialize()

        } catch (IOException e) {
            throw new TestFrameworkException("Failed to load Keystore file from the location", e);
        } catch (CertificateException e) {
            throw new TestFrameworkException("Failed to load Certificate from the keystore", e);
        } catch (NoSuchAlgorithmException e) {
            throw new TestFrameworkException("Failed to identify the Algorithm ", e);
        } catch (KeyStoreException e) {
            throw new TestFrameworkException("Failed to initialize the Keystore ", e);
        } catch (UnrecoverableEntryException e) {
            throw new TestFrameworkException("Error occurred while retrieving values from KeyStore ", e);
        } catch (JOSEException e) {
            throw new TestFrameworkException("Failed to sign the object ", e);
        }
    }

}

