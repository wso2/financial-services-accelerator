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

package org.wso2.openbanking.test.framework.utility

import org.wso2.bfsi.test.framework.exception.TestFrameworkException
import org.wso2.openbanking.test.framework.configuration.OBConfigurationService
import org.apache.http.conn.ssl.SSLSocketFactory
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.UnrecoverableKeyException
import java.security.cert.CertificateException

/**
 * Creates an SSL socket factory for MTLS requests.
 */
class SSLSocketFactoryCreator {

    OBConfigurationService configuration

    SSLSocketFactoryCreator() {
        configuration = new OBConfigurationService()
    }
    /**
     * Create SSL socket factory.
     *
     * @return an SSLSocketFactory implementation
     * @throws TestFrameworkException when an error occurs while loading the keystore and truststore
     */
    SSLSocketFactory create(Integer tppNumber = null) throws TestFrameworkException {
        try {
            FileInputStream keystoreLocation = new FileInputStream(new File(configuration.getAppTransportKeyStoreLocation(tppNumber)));
            FileInputStream truststoreLocation = new FileInputStream(new File(configuration.getTransportTruststoreLocation()))

            KeyStore keyStore = KeyStore.getInstance(configuration.getAppTransportKeyStoreType(tppNumber));
            keyStore.load(keystoreLocation, configuration.getAppTransportKeyStorePWD(tppNumber).toCharArray());
            KeyStore trustStore = KeyStore.getInstance(configuration.getTransportTruststoreType());
            trustStore.load(truststoreLocation, configuration.getTransportTruststorePWD().toCharArray());

            // Manually create a new socketfactory and pass in the required values.
            return new SSLSocketFactory(keyStore, configuration.getAppTransportKeyStorePWD(tppNumber), trustStore);
        }
        catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | KeyManagementException | UnrecoverableKeyException | IOException e) {
            throw new TestFrameworkException("Unable to load the transport keystore and truststore", e);
        }
    }

    /**
     * Create SSL socket factory for given keystore file path, keystore password and tppNumber
     * tpp number for select application
     *
     * @param keystoreFilePath keystore file path.
     * @param keystorePassword keystore password.
     * @return an SSLSocketFactory implementation.
     * @throws TestFrameworkException when an error occurs while loading the keystore and truststore.
     */
    SSLSocketFactory creatWithCustomKeystore(String keystoreFilePath, String keystorePassword, int tppNumber) throws TestFrameworkException {
        try {

            FileInputStream keystoreLocation = new FileInputStream(new File(keystoreFilePath));
            FileInputStream truststoreLocation = new FileInputStream(new File(configuration.getTransportTruststoreLocation()))

            KeyStore keyStore = KeyStore.getInstance(configuration.getAppTransportKeyStoreType(tppNumber));
            keyStore.load(keystoreLocation, keystorePassword.toCharArray());
            KeyStore trustStore = KeyStore.getInstance(configuration.getTransportTruststoreType());
            trustStore.load(truststoreLocation, configuration.getTransportTruststorePWD().toCharArray());

            // Manually create a new socket factory and pass in the required values.
            return new SSLSocketFactory(keyStore, keystorePassword, trustStore);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | KeyManagementException | UnrecoverableKeyException | IOException e) {
            throw new TestFrameworkException("Unable to load the transport keystore and truststore", e);
        }
    }

}

