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

package org.wso2.bfsi.test.framework.keystore

import org.apache.lucene.queries.spans.SpanNearQuery
import org.wso2.bfsi.test.framework.configuration.CommonConfigurationService
import org.wso2.bfsi.test.framework.constant.Constants
import org.wso2.bfsi.test.framework.exception.TestFrameworkException

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.util.Base64URL
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.security.Key
import java.security.KeyStoreException
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate
import java.security.cert.CertificateEncodingException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey;

/**
 * Class for handle Keystore functions
 */
class KeyStore {

    static Logger log = LogManager.getLogger(KeyStore.class.getName());

    private static CommonConfigurationService obConfiguration = new CommonConfigurationService()

    /**
     *  Provide Keystore instance for given parameters
     * @param keystorePath
     * @param password
     * @return Keystore
     * @throws TestFrameworkException
     */
    static java.security.KeyStore getKeyStore(String keystorePath, String password) throws TestFrameworkException {
        try {
            InputStream inputStream = new FileInputStream(keystorePath)
            java.security.KeyStore keyStore = java.security.KeyStore.getInstance("JKS");
            keyStore.load(inputStream, password.toCharArray());
            return keyStore;
        } catch (IOException e) {
            log.error("Failed to load Keystore file from the location", e)
            throw new TestFrameworkException("Failed to load Keystore file from the location", e)
        } catch (CertificateException e) {
            log.error("Failed to load Certificate from the keystore", e)
            throw new TestFrameworkException("Failed to load Certificate from the keystore", e)
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to identify the Algorithm", e)
            throw new TestFrameworkException("Failed to identify the Algorithm ", e)
        } catch (KeyStoreException e) {
            log.error("Failed to initialize the Keystore ", e)
            throw new TestFrameworkException("Failed to initialize the Keystore ", e)
        }
    }

    /**
     *  Return application keystore
     * @param tppNumber
     * @return
     * @throws TestFrameworkException
     */
    static java.security.KeyStore getApplicationKeyStore(Integer tppNumber = null) throws TestFrameworkException {
        return getKeyStore(obConfiguration.getAppKeyStoreLocation(tppNumber), obConfiguration.getAppKeyStorePWD(tppNumber));
    }

    /**
     *  Return SigningKey for given keystore details
     * @param keystorePath
     * @param password
     * @param alias
     * @return
     * @throws TestFrameworkException
     */
    static Key getSigningKey(String keystorePath, String password, String alias) throws TestFrameworkException {
        java.security.KeyStore keyStore = getKeyStore(keystorePath, password)
        Key signingKey = keyStore.getKey(alias, password.toCharArray())
        return signingKey
    }

    /**
     * Return Application signing key for given application number
     * @param tppNumber
     * @return
     * @throws TestFrameworkException
     */
    static Key getApplicationSigningKey(Integer tppNumber = null) throws TestFrameworkException {
        return getSigningKey(obConfiguration.getAppKeyStoreLocation(tppNumber), obConfiguration.getAppKeyStorePWD(tppNumber)
                , obConfiguration.getAppKeyStoreAlias(tppNumber))
    }

    /**
     * Return thumbprint for given certificate and algorithm
     * @param certificate
     * @param algorithm
     * @return
     * @throws TestFrameworkException
     */
    static String getJwkThumb(Certificate certificate, String algorithm) throws TestFrameworkException {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            ByteArrayInputStream bais = new ByteArrayInputStream(certificate.getEncoded());
            X509Certificate x509 = (X509Certificate) cf.generateCertificate(bais);
            Base64URL jwkThumbprint = RSAKey.parse(x509).computeThumbprint(algorithm);
            return jwkThumbprint.toString();
        } catch (CertificateException | JOSEException e) {
            throw new TestFrameworkException("Error occurred while generating" + algorithm + " JWK thumbprint", e);
        }
    }

    /**
     * Return application thumbprint for SHA-1 algorithm
     * @param certificate
     * @return
     * @throws TestFrameworkException
     */
    static String getJwkThumbPrintForSHA1(Certificate certificate) throws TestFrameworkException {
        return getJwkThumb(certificate, Constants.ALGORITHM_SHA_1)
    }

    /**
     * Return application thumbprint for SHA-256 algorithm
     * @param certificate
     * @return
     * @throws TestFrameworkException
     */
    static String getJwkThumbPrintForSHA256(Certificate certificate) throws TestFrameworkException {
        return getJwkThumb(certificate, Constants.ALGORITHM_SHA_256)
    }

    /**
     * Return thumbprint
     * @param certificate
     * @return
     * @throws TestFrameworkException
     */
    static String getThumbPrint(Certificate certificate) throws TestFrameworkException {

        MessageDigest digestValue;
        try {
            digestValue = MessageDigest.getInstance("SHA-256");
            byte[] der = certificate.getEncoded();
            digestValue.update(der);
            byte[] digestInBytes = digestValue.digest();

            return hexify(digestInBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new TestFrameworkException("Failed to identify the Algorithm ", e);
        } catch (CertificateEncodingException e) {
            throw new TestFrameworkException("Failed to identify the Certificate encoding ", e);
        }

    }

    /**
     *  Return certificate for given keystore details
     * @param keyStore
     * @param alias
     * @param password
     * @return
     * @throws TestFrameworkException
     */
    static Certificate getCertificate(java.security.KeyStore keyStore, String alias, String password) throws TestFrameworkException {
        try {
            java.security.KeyStore.PrivateKeyEntry pkEntry = (java.security.KeyStore.PrivateKeyEntry) keyStore.getEntry(
                    alias, new java.security.KeyStore.PasswordProtection(password.toCharArray()))
            Certificate certificate = pkEntry.getCertificate();
            return certificate
        } catch (CertificateException e) {
            throw new TestFrameworkException("Error occurred while generating certificate", e);
        }
    }

    /**
     * Return application certificate for given application number
     * @param tppNumber
     * @return
     * @throws TestFrameworkException
     */
    static Certificate getApplicationCertificate(Integer tppNumber = null) throws TestFrameworkException {
        java.security.KeyStore keyStore = getApplicationKeyStore(tppNumber)
        return getCertificate(keyStore, obConfiguration.getAppKeyStoreAlias(tppNumber)
                , obConfiguration.getAppKeyStorePWD(tppNumber))
    }

    /**
     * Returns the key id of the Application certificate.
     *
     * @return keyId
     */
    static String getApplicationCertificateKeyId() {

        X509Certificate certificate = (X509Certificate) Objects
                .requireNonNull(getApplicationCertificate());
        try {
            RSAKey rsaKey = RSAKey.parse(certificate);

            RSAKey key = new RSAKey.Builder(rsaKey)
                    .keyUse(KeyUse.ENCRYPTION)
                    .keyIDFromThumbprint()
                    .build()
            return key.getKeyID();
        } catch (JOSEException e) {
            log.error("Error occurred while parsing Certificate ", e);
        }

        return null;
    }

    /**
     * Returns the subject DN for the application certificate.
     *
     * @return subject DN
     */
    static String getApplicationCertificateSubjectDn() {

        X509Certificate certificate = (X509Certificate) getApplicationCertificate();

        if (certificate != null && certificate.getSubjectDN() != null) {
            return certificate.getSubjectDN().getName();
        }

        return null;
    }


    /**
     * get kid from certificate in JWKs endpoint.
     *
     * @param certificate certificate
     * @return x5t
     */
    static String getKidOfJwksCertificate(X509Certificate certificate) {

        String x5t = "";
        RSAPublicKey rsaPublicKey = (RSAPublicKey) certificate.getPublicKey();
        JWK jwk = new SpanNearQuery.Builder(rsaPublicKey).keyID(UUID.randomUUID().toString()).build();
        try {
            x5t = jwk.computeThumbprint("SHA-1").toString();
        } catch (JOSEException e) {
            log.error("Error while computing thumbprint", e);
        }
        return x5t;
    }

    /**
     * Get Public Key From the Specified Keystore.
     *
     * @param keystoreLocation keystore location
     * @param keystorePassword keystore password
     * @param keystoreAlias alias
     * @return public key
     * @throws TestFrameworkException exception
     */
    static String getPublicKeyFromKeyStore(String keystoreLocation, String keystorePassword
                                           , String keystoreAlias) throws TestFrameworkException {

        try {
            java.security.KeyStore keyStore = getKeyStore(keystoreLocation, keystorePassword)
            // Get certificate of public key
            Certificate cert = getCertificate(keyStore, keystoreAlias, keystorePassword)
            // Get public key
            return Base64.getEncoder().encodeToString(cert.getPublicKey().getEncoded());
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            throw new TestFrameworkException("Error occurred while retrieving values from KeyStore ", e);
        }
    }

    /**
     * Provide Application Transport Keystore
     * @return
     * @throws TestFrameworkException
     */
    static getTransportKeyStore() throws TestFrameworkException {
        return getKeyStore(obConfiguration.getAppTransportKeyStoreLocation(), obConfiguration.getAppTransportKeyStorePWD())
    }

}

