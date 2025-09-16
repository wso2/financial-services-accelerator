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

package org.wso2.openbanking.test.framework.request_builder

import org.wso2.bfsi.test.framework.exception.TestFrameworkException
import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.RSASSASigner
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.json.JSONObject
import org.wso2.openbanking.test.framework.configuration.OBConfigurationService
import org.wso2.openbanking.test.framework.keystore.OBKeyStore

import java.security.Key
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.Security
import java.security.UnrecoverableEntryException
import java.security.cert.Certificate
import java.security.cert.CertificateException

/**
 * Class for get Signed objects
 */
class SignedObject {

    private OBConfigurationService configuration
    private String signingAlgorithm = null

    SignedObject() {
        configuration = new OBConfigurationService()
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


        Key signingKey = OBKeyStore.getApplicationSigningKey()
        Certificate certificate = OBKeyStore.getApplicationCertificate()
        String thumbprint = OBKeyStore.getJwkThumbPrintForSHA1(certificate)

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

        Key signingKey = OBKeyStore.getApplicationSigningKey()
        Certificate certificate = OBKeyStore.getApplicationCertificate()
        String thumbprint = OBKeyStore.getJwkThumbPrintForSHA1(certificate)

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.parse(getSigningAlgorithm()))
                .keyID(thumbprint).type(JOSEObjectType.JWT).build();

        JWSSigner signer = new RSASSASigner((PrivateKey) signingKey);

        JWSObject jwsObject = new JWSObject(header, new Payload(claims));

        jwsObject.sign(signer);

        return jwsObject.serialize()

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

            KeyStore keyStore = OBKeyStore.getKeyStore(appKeystoreLocation, appKeystorePassword)
            Certificate certificate = OBKeyStore.getCertificate(keyStore, appKeystoreAlias, appKeystorePassword)
            Key signingKey = OBKeyStore.getSigningKey(appKeystoreLocation, appKeystorePassword, appKeystoreAlias)

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.parse(signingAlg)).
                    keyID(OBKeyStore.getJwkThumbPrintForSHA1(certificate)).build();
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

}

