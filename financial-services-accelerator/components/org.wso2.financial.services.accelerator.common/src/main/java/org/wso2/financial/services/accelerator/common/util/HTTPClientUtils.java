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

package org.wso2.financial.services.accelerator.common.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.internal.FinancialServicesCommonDataHolder;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;

/**
 * HTTP Client Utility methods.
 */
public class HTTPClientUtils {

    public static final String ALLOW_ALL = "AllowAll";
    public static final String STRICT = "Strict";
    public static final String HOST_NAME_VERIFIER = "httpclient.hostnameVerifier";
    private static final String[] SUPPORTED_HTTP_PROTOCOLS = {"TLSv1.2"};
    private static final Log log = LogFactory.getLog(DatabaseUtils.class);

    private HTTPClientUtils() {
        // Prevent instantiation
    }

    /**
     * Get closeable https client.
     *
     * @return Closeable https client
     */
    @Generated(message = "Ignoring since method contains no logics")
    public static CloseableHttpClient getHttpsClient() {

        return HttpsClientHolder.INSTANCE;
    }

    private static class HttpsClientHolder {
        private static final CloseableHttpClient INSTANCE;

        static {

            INSTANCE = HttpClients.custom()
                    .setConnectionManager(FinancialServicesCommonDataHolder.getInstance().getConnectionManager())
                    .build();

            // Optional: Add shutdown hook to clean up properly
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    INSTANCE.close();
                } catch (IOException e) {
                    log.error("Error shutting down HttpClient", e);
                }
            }));

            log.debug("HTTPS Client initialized with singleton pattern.");
        }
    }

    /**
     * create a SSL Connection Socket Factory.
     *
     * @return SSLConnectionSocketFactory
     * @throws FinancialServicesException when failed to create SSLConnectionSocketFactory
     */
    @Generated(message = "Ignoring because ServerConfiguration cannot be mocked")
    public static SSLConnectionSocketFactory createSSLConnectionSocketFactory()
            throws FinancialServicesException {

        KeyStore trustStore = null;

        trustStore = loadKeyStore(
                ServerConfiguration.getInstance().getFirstProperty("Security.TrustStore.Location"),
                ServerConfiguration.getInstance().getFirstProperty("Security.TrustStore.Password"));

        // Trust own CA and all self-signed certs
        SSLContext sslcontext = null;
        try {
            sslcontext = SSLContexts.custom().loadTrustMaterial(trustStore, new TrustSelfSignedStrategy()).build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new FinancialServicesException("Unable to create the ssl context", e);
        }

        // Allow TLSv1 protocol only
        return new SSLConnectionSocketFactory(sslcontext, SUPPORTED_HTTP_PROTOCOLS,
                null, getX509HostnameVerifier());

    }

    /**
     * Load the keystore when the location and password is provided.
     *
     * @param keyStoreLocation Location of the keystore
     * @param keyStorePassword Keystore password
     * @return Keystore as an object
     * @throws FinancialServicesException when failed to load Keystore from given details
     */
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    // Suppressed content - new FileInputStream(keyStoreLocation)
    // Suppression reason - False Positive : Keystore location is obtained from deployment.toml. So it can be marked
    //                      as a trusted filepath
    // Suppressed warning count - 1
    public static KeyStore loadKeyStore(String keyStoreLocation, String keyStorePassword)
            throws FinancialServicesException {

        KeyStore keyStore;

        try (FileInputStream inputStream = new FileInputStream(keyStoreLocation)) {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(inputStream, keyStorePassword.toCharArray());
            return keyStore;
        } catch (KeyStoreException e) {
            throw new FinancialServicesException("Error while retrieving aliases from keystore", e);
        } catch (IOException | CertificateException | NoSuchAlgorithmException e) {
            throw new FinancialServicesException("Error while loading keystore", e);
        }
    }

    /**
     * Get the Hostname Verifier property in set in system properties.
     *
     * @return X509HostnameVerifier
     */
    public static X509HostnameVerifier getX509HostnameVerifier() {

        String hostnameVerifierOption = System.getProperty(HOST_NAME_VERIFIER);
        X509HostnameVerifier hostnameVerifier;

        if (ALLOW_ALL.equals(hostnameVerifierOption)) {
            hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
        } else if (STRICT.equals(hostnameVerifierOption)) {
            hostnameVerifier = SSLSocketFactory.STRICT_HOSTNAME_VERIFIER;
        } else {
            hostnameVerifier = SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER;
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Proceeding with %s : %s", HOST_NAME_VERIFIER,
                    hostnameVerifierOption));
        }
        return hostnameVerifier;

    }

}
