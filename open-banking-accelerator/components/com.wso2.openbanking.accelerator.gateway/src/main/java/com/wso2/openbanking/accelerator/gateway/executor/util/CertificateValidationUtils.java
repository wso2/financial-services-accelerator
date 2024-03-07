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

package com.wso2.openbanking.accelerator.gateway.executor.util;

import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.error.OpenBankingErrorCodes;
import com.wso2.openbanking.accelerator.common.exception.CertificateValidationException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OpenBankingExecutorError;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Optional;

/**
 * Utility class containing certificate util methods.
 */
public class CertificateValidationUtils {

    public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    public static final String END_CERT = "-----END CERTIFICATE-----";
    public static final String X509_CERT_INSTANCE_NAME = "X.509";
    public static final String HTTP_CONTENT_TYPE = "Content-Type";
    public static final String HTTP_CONTENT_TYPE_OCSP = "application/ocsp-request";
    public static final String HTTP_ACCEPT = "Accept";
    public static final String HTTP_ACCEPT_OCSP = "application/ocsp-response";
    public static final String CONTENT_TYPE = "application/json";
    public static final String TRUSTSTORE_LOCATION_CONF_KEY = "Security.TrustStore.Location";
    public static final String TRUSTSTORE_PASS_CONF_KEY = "Security.TrustStore.Password";
    private static final Log LOG = LogFactory.getLog(CertificateValidationUtils.class);
    private static KeyStore trustStore = null;

    private CertificateValidationUtils() {
        // Adding a private constructor to hide the implicit public one.
    }

    /**
     * @deprecated use com.wso2.openbanking.accelerator.common.util.CertificateUtils.isExpired() instead
     */
    @Deprecated
    public static boolean isExpired(X509Certificate peerCertificate) {
        try {
            peerCertificate.checkValidity();
        } catch (CertificateException e) {
            LOG.error("Certificate with the serial number " +
                    peerCertificate.getSerialNumber() + " issued by the CA " +
                    peerCertificate.getIssuerDN().toString() + " is expired. Caused by, " + e.getMessage());
            return true;
        }
        return false;
    }

    /**
     * Get issuer certificate from the truststore.
     *
     * @param peerCertificate peer certificate
     * @return certificate issuer of the peer certificate
     * @throws CertificateValidationException when unable to validate the certificate
     */
    public static X509Certificate getIssuerCertificateFromTruststore(X509Certificate peerCertificate)
            throws CertificateValidationException {

        KeyStore loadedTrustStore = getTrustStore();
        if (loadedTrustStore == null) {
            throw new CertificateValidationException("Client truststore has not been initialized");
        }

        return retrieveCertificateFromTruststore(peerCertificate, loadedTrustStore);

    }

    /**
     * Get the truststore. This methods needs to be synchronized with the loadTrustStore() method
     *
     * @return instance of the truststore
     */
    public static synchronized KeyStore getTrustStore() {
        return trustStore;
    }

    /**
     * Get certificate from truststore.
     *
     * @param peerCertificate  peer certificate
     * @param loadedTrustStore truststore
     * @return certificate retrieved from truststore
     * @throws CertificateValidationException when unable to validate the certificate
     */
    public static X509Certificate retrieveCertificateFromTruststore(
            X509Certificate peerCertificate, KeyStore loadedTrustStore) throws CertificateValidationException {

        Enumeration enumeration;
        java.security.cert.X509Certificate certificate;
        try {
            // Get aliases of all the certificates in the truststore.
            enumeration = loadedTrustStore.aliases();
        } catch (KeyStoreException e) {
            throw new CertificateValidationException("Error while retrieving aliases from keystore", e);
        }

        // As there is no any specific way to query the issuer certificate from the truststore, public keys of all the
        // certificates in the truststore are validated against the signature of the peer certificate to identify the
        // issuer.
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                String alias = null;
                try {
                    alias = (String) enumeration.nextElement();
                    certificate = (java.security.cert.X509Certificate) loadedTrustStore.getCertificate(alias);
                } catch (KeyStoreException e) {
                    throw new CertificateValidationException("Unable to read the certificate from truststore with " +
                            "the alias: " + alias, e);
                }
                try {
                    peerCertificate.verify(certificate.getPublicKey());
                    LOG.debug("Valid issuer certificate found in the client truststore");
                    return certificate;
                } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException |
                         NoSuchProviderException | SignatureException e) {
                    // Unable to verify the signature. Check with the next certificate.
                }
            }
        } else {
            throw new CertificateValidationException("Unable to read the certificate aliases from the truststore");
        }
        throw new CertificateValidationException("Unable to find the immediate issuer from the truststore of the " +
                "certificate with the serial number " + peerCertificate.getSerialNumber() + " issued by the CA " +
                peerCertificate.getIssuerDN().toString());
    }

    /**
     * Loads the Truststore.
     *
     * @param trustStorePassword truststore password
     */
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    // Suppressed content - Files.newInputStream(Paths.get(trustStorePath)))
    // Suppression reason - False Positive : trustStorePath is obtained from deployment.toml. So it can be marked
    //                      as a trusted filepath
    // Suppressed warning count - 1
    @Generated(message = "Ignoring because ServerConfiguration cannot be mocked")
    public static synchronized void loadTrustStore(char[] trustStorePassword)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {

        String trustStorePath = ServerConfiguration.getInstance()
                .getFirstProperty(CertificateValidationUtils.TRUSTSTORE_LOCATION_CONF_KEY);
        try (InputStream inputStream = Files.newInputStream(Paths.get(trustStorePath))) {
            trustStore = KeyStore.getInstance(OpenBankingConstants.TRUSTSTORE_CONF_TYPE_DEFAULT);
            trustStore.load(inputStream, trustStorePassword);
        }
    }


    /**
     * Loads the Truststore.
     *
     * This method is deprecated as it allows custom absolute file paths which could result in
     * path traversal attacks. Do not use this method unless the custom path is trusted.
     * @param trustStorePath     truststore path
     * @param trustStorePassword truststore password
     */
    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    // Suppressed content - dataHolder.getKeyStoreLocation()
    // Suppression reason - False Positive : Keystore location is obtained from deployment.toml. So it can be marked
    //                      as a trusted filepath
    // Suppressed warning count - 1
    @Deprecated
    public static synchronized void loadTrustStore(String trustStorePath, char[] trustStorePassword)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {

        try (InputStream inputStream = Files.newInputStream(Paths.get(trustStorePath))) {
            trustStore = KeyStore.getInstance(OpenBankingConstants.TRUSTSTORE_CONF_TYPE_DEFAULT);
            trustStore.load(inputStream, trustStorePassword);
        }
    }

    public static void handleExecutorErrors(CertificateValidationException e
            , OBAPIRequestContext obapiRequestContext) {
        OpenBankingExecutorError error = new OpenBankingExecutorError(e.getErrorCode(), e.getMessage(),
                e.getErrorPayload(), OpenBankingErrorCodes.UNAUTHORIZED_CODE);
        handleExecutorErrors(error, obapiRequestContext);
    }

    public static void handleExecutorErrors(OpenBankingExecutorError error
            , OBAPIRequestContext obapiRequestContext) {
        ArrayList<OpenBankingExecutorError> executorErrors = obapiRequestContext.getErrors();
        executorErrors.add(error);

        obapiRequestContext.setError(true);
        obapiRequestContext.setErrors(executorErrors);
    }

    /**
     * Convert javax.security.cert.X509Certificate to java.security.cert.X509Certificate.
     *
     * @param cert the certificate to be converted
     * @return java.security.cert.X509Certificate type certificate
     * @deprecated use convertCert(javax.security.cert.X509Certificate cert) method instead.
     */
    @Deprecated
    public static Optional<java.security.cert.X509Certificate> convert(javax.security.cert.X509Certificate cert) {

        try {
            return convertCert(cert);
        } catch (CertificateException e) {
            // Not logging the errors again as it is done in the convertCert method
            return Optional.empty();
        }
    }

    /**
     * Convert javax.security.cert.X509Certificate to java.security.cert.X509Certificate
     * This method will also handle the exceptions that could occur in the process of converting the certificate.
     * Will be having the above convert method as well as it is a public method and will deprecate it gradually.
     *
     * @param cert the certificate to be converted
     * @return java.security.cert.X509Certificate type certificate
     * @throws CertificateException
     */
    public static Optional<java.security.cert.X509Certificate> convertCert(javax.security.cert.X509Certificate cert)
            throws CertificateException {

        if (cert != null) {
            try {
                byte[] encoded = cert.getEncoded();
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(encoded);
                java.security.cert.CertificateFactory certificateFactory
                        = java.security.cert.CertificateFactory.getInstance(X509_CERT_INSTANCE_NAME);
                return Optional.of((java.security.cert.X509Certificate) certificateFactory.generateCertificate(
                        byteArrayInputStream));
            } catch (javax.security.cert.CertificateEncodingException e) {
                String errorMsg = "Error while decoding the certificate ";
                LOG.error(errorMsg, e);
                throw new CertificateException(errorMsg, e);
            } catch (java.security.cert.CertificateException e) {
                String errorMsg = "Error while generating the certificate ";
                LOG.error(errorMsg, e);
                throw new CertificateException(errorMsg, e);
            }
        } else {
            return Optional.empty();
        }
    }

    /**
     * Converts java.security.cert.Certificate to java.security.cert.X509Certificate.
     *
     * @param certificate java.security.cert.Certificate which needs conversion
     * @return java.security.cert.X509Certificate
     * @throws CertificateException thrown if an error occurs while converting
     */
    public static Optional<X509Certificate> convertCertToX509Cert(Certificate certificate)
            throws CertificateException {

        CertificateFactory cf = CertificateFactory.getInstance(X509_CERT_INSTANCE_NAME);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(certificate.getEncoded());
        return Optional.of((X509Certificate) cf.generateCertificate(byteArrayInputStream));
    }

    public static Date getNewDate() {
        return new Date();
    }
}
