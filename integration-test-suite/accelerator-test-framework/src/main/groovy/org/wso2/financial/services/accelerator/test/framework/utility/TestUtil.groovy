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

package org.wso2.financial.services.accelerator.test.framework.utility

import com.fasterxml.uuid.Generators
import org.apache.http.conn.ssl.SSLSocketFactory
import org.wso2.bfsi.test.framework.exception.TestFrameworkException
import org.wso2.openbanking.test.framework.utility.OBTestUtil
import org.wso2.openbanking.test.framework.utility.SSLSocketFactoryCreator
import org.wso2.financial.services.accelerator.test.framework.configuration.ConfigurationService

import java.nio.charset.Charset
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.Certificate
import java.security.cert.CertificateException

/**
 * Accelerator layer class to contain utilities.
 */
class TestUtil extends OBTestUtil{

    static ConfigurationService configurationService = new ConfigurationService()

    /**
     * Get Basic Auth Header.
     * @param client
     * @param password
     * @return encoded basic auth header
     */
    static String getBasicAuthHeader(String client, String password) {

        def authToken = "${client}:${password}"
        return Base64.encoder.encodeToString(authToken.getBytes(Charset.defaultCharset()))
    }

    /**
     * Generate random UUID.
     *
     * @return generated random UUID
     */
    static String generateUUID(){
        UUID uuid = Generators.timeBasedGenerator().generate()
        return uuid.toString()
    }

    /**
     * Get the error description when user denies a Consent
     * @param url
     */
    static String getErrorDescriptionFromUrlWhenDenied(String url) {
        try {
            return url.split("error_description=")[1].split("&")[0]
        } catch (Exception e) {
            log.error("User+denied+the+consent", e)
        }
        return null
    }

    /**
     * Get Public Key from Transport Keystore.
     *
     * @return public key
     * @throws org.wso2.bfsi.test.framework.exception.TestFrameworkException exception
     */
    public static String getPublicKeyFromTransportKeyStore() throws TestFrameworkException {
        ConfigurationService configurationService = new ConfigurationService();

        try (InputStream inputStream = new FileInputStream(
                configurationService.getAppTransportKeyStoreLocation())) {
            KeyStore keyStore = KeyStore.getInstance("JKS");

            String keystorePassword = configurationService.getAppTransportKeyStorePWD();
            keyStore.load(inputStream, keystorePassword.toCharArray());

            String keystoreAlias = configurationService.getTransportKeystoreAlias();
            // Get certificate of public key
            Certificate cert = keyStore.getCertificate(keystoreAlias);

            // Get public key
            return Base64.getEncoder().encodeToString(cert.getEncoded());

        } catch (IOException e) {
            throw new TestFrameworkException("Failed to load Keystore file from the location", e);
        } catch (CertificateException e) {
            throw new TestFrameworkException("Failed to load Certificate from the keystore", e);
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            throw new TestFrameworkException("Error occurred while retrieving values from KeyStore ", e);
        }
    }

    /**
     * Extract Error Description From URL
     *
     * @param url
     * @return Error Description
     */
    static String getOAuthErrorCodeFromUrl(String url) {
        return url.split("oauth2_error.do?")[1].split("&")[0].replaceAll("\\+", " ");
    }

    /**
     * Extract Error Message From URL
     *
     * @param url
     * @return Error Description
     */
    static String getOAuthErrorMsgFromUrl(String url) {

        return url.split("oauth2_error.do?")[1].split("&")[1].replaceAll("\\+", " ");
    }

    /**
     * Get request with request context.
     * @param keystoreLocation keystore file path.
     * @param keystorePassword keystore password.
     * @return sslSocketFactory.
     */
    public static SSLSocketFactory getSslSocketFactory(String keystoreLocation, String keystorePassword) {

        if (configurationService.appTransportMLTSEnable) {
            try {
                SSLSocketFactoryCreator sslSocketFactoryCreator = new SSLSocketFactoryCreator();
                sslSocketFactory = sslSocketFactoryCreator.creatWithCustomKeystore(keystoreLocation, keystorePassword, 0);

                // Skip hostname verification.
                sslSocketFactory.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory
                        .ALLOW_ALL_HOSTNAME_VERIFIER);
            } catch (TestFrameworkException e) {
                log.error("Unable to create the SSL socket factory", e);
            }
        }
        return sslSocketFactory;
    }

    /**
     * Extract Authorisation code from redirect URL of hybriod flow response.
     *
     * @param codeUrl redirection url.
     * @return authorisation code.
     */
    static String getHybridCodeFromUrl(String codeUrl) {
        return codeUrl.split("#")[1].split("&")[1].split("code")[1].substring(1)
    }

}
