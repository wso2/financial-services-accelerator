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

package com.wso2.openbanking.accelerator.gateway.executor.revocation;

import com.wso2.openbanking.accelerator.common.exception.CertificateValidationException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.HTTPClientUtils;
import com.wso2.openbanking.accelerator.gateway.executor.model.RevocationStatus;
import com.wso2.openbanking.accelerator.gateway.executor.util.CertificateValidationUtils;
import com.wso2.openbanking.accelerator.gateway.internal.TPPCertValidatorDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This is used to verify whether a certificate is revoked or not by using the Certificate Revocation List published
 * by the CA.
 */
public class CRLValidator implements RevocationValidator {

    private static final Log log = LogFactory.getLog(CRLValidator.class);

    private final int retryCount;
    private static int httpConnectTimeout;
    private static int httpConnectionRequestTimeout;
    private static int httpSocketTimeout;

    public CRLValidator(int retryCount) {

        this.retryCount = retryCount;
    }


    public CRLValidator(int retryCount, int httpConnectTimeout, int httpConnectionRequestTimeout,
                         int httpSocketTimeout) {

        this.retryCount = retryCount;
        CRLValidator.httpConnectTimeout = httpConnectTimeout;
        CRLValidator.httpConnectionRequestTimeout = httpConnectionRequestTimeout;
        CRLValidator.httpSocketTimeout = httpSocketTimeout;
    }

    /**
     * Extracts all CRL distribution point URLs from the "CRL Distribution Point" extension in a X.509 certificate.
     * If CRL distribution point extension or CRL Urls are unavailable, throw an exception.
     *
     * @param cert X509 certificate
     * @return List of CRL Urls in the certificate
     * @throws CertificateValidationException certificateValidationException
     */
    public static List<String> getCRLUrls(X509Certificate cert) throws CertificateValidationException {

        List<String> crlUrls;
        byte[] crlDPExtensionValue = getCRLDPExtensionValue(cert);
        if (crlDPExtensionValue == null) {
            throw new CertificateValidationException("Certificate with serial num:" + cert.getSerialNumber()
                    + " doesn't have CRL Distribution points");
        }
        CRLDistPoint distPoint = getCrlDistPoint(crlDPExtensionValue);
        crlUrls = getCrlUrlsFromDistPoint(distPoint);

        if (crlUrls.isEmpty()) {
            throw new CertificateValidationException("Cannot get CRL urls from certificate with serial num:" +
                    cert.getSerialNumber());
        }
        return crlUrls;
    }

    /**
     * Get revocation status of a certificate using CRL Url.
     *
     * @param peerCert                          peer certificate
     * @param retryCount                        retry count to connect to CRL Url and get the CRL
     * @param crlUrls                           List of CRL Urls
     * @param certificateRevocationProxyEnabled whether certificate revocation proxy enabled in the config
     * @param certificateRevocationProxyHost    certificate revocation proxy host
     * @param certificateRevocationProxyPort    certificate revocation proxy port
     * @return Revocation status of the certificate
     * @throws CertificateValidationException certificateValidationException
     */
    public static RevocationStatus getCRLRevocationStatus(X509Certificate peerCert, X509Certificate issuerCert,
                                                          int retryCount, List<String> crlUrls,
                                                          boolean certificateRevocationProxyEnabled,
                                                          String certificateRevocationProxyHost,
                                                          int certificateRevocationProxyPort)
            throws CertificateValidationException {

        // Check with distributions points in the list one by one. if one fails go to the other.
        for (String crlUrl : crlUrls) {
            if (log.isDebugEnabled()) {
                log.debug("Trying to get CRL for URL: " + crlUrl);
            }
            X509CRL x509CRL = downloadCRLFromWeb(crlUrl, retryCount, peerCert, issuerCert,
                    certificateRevocationProxyEnabled, certificateRevocationProxyHost, certificateRevocationProxyPort);
            if (x509CRL != null) {
                return getRevocationStatusFromCRL(x509CRL, peerCert);
            }
        }
        throw new CertificateValidationException("Cannot check revocation status with the certificate");
    }

    /**
     * ****************************************
     * Util methods for CRL Validation.
     * ****************************************
     */

    private static boolean isValidX509Crl(X509CRL x509CRL, X509Certificate peerCert, X509Certificate issuerCert)
            throws CertificateValidationException {

        Date currentDate = CertificateValidationUtils.getNewDate();
        Date nextUpdate = x509CRL.getNextUpdate();
        boolean isValid = false;

        if (isValidX509CRLFromIssuer(x509CRL, peerCert, issuerCert)) {
            isValid = isValidX509CRLFromNextUpdate(x509CRL, currentDate, nextUpdate);
        }
        return isValid;
    }

    private static boolean isValidX509CRLFromIssuer(X509CRL x509CRL, X509Certificate peerCert,
                                                    X509Certificate issuerCert)
            throws CertificateValidationException {

        if (!peerCert.getIssuerDN().equals(x509CRL.getIssuerDN())) {
            throw new CertificateValidationException("X509 CRL is not valid. Issuer DN in the peer " +
                    "certificate: " + peerCert.getIssuerDN() + " does not match with the Issuer DN in the X509 CRL: " +
                    x509CRL.getIssuerDN());
        }

        // Verify the signature of the CRL.
        try {
            x509CRL.verify(issuerCert.getPublicKey());
            return true;
        } catch (CRLException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException |
                SignatureException e) {
            throw new CertificateValidationException("CRL signature cannot be verified", e);
        }
    }

    private static boolean isValidX509CRLFromNextUpdate(X509CRL x509CRL, Date currentDate, Date nextUpdate)
            throws CertificateValidationException {

        if (nextUpdate != null) {
            if (log.isDebugEnabled()) {
                log.debug("Validating the next update date: " + nextUpdate.toString() + " with the current date: " +
                        currentDate.toString());
            }
            if (currentDate.before(x509CRL.getNextUpdate())) {
                return true;
            } else {
                throw new CertificateValidationException("X509 CRL is not valid. Next update date: " +
                        nextUpdate.toString() + " is before the current date: " + currentDate.toString());
            }
        } else {
            log.debug("Couldn't validate the X509 CRL, next update date is not available.");
        }
        return false;
    }

    private static X509CRL downloadCRLFromWeb(String crlURL, int retryCount, X509Certificate peerCert,
                                              X509Certificate issuerCert, boolean certificateRevocationProxyEnabled,
                                              String certificateRevocationProxyHost, int certificateRevocationProxyPort)
            throws CertificateValidationException {

        X509CRL x509CRL = null;
        if (log.isDebugEnabled()) {
            log.debug("Certificate revocation check proxy enabled: " + certificateRevocationProxyEnabled);
        }

        HttpGet httpGet = new HttpGet(crlURL);
        if (certificateRevocationProxyEnabled) {
            log.debug("Setting certificate revocation proxy started.");
            if (certificateRevocationProxyHost == null || certificateRevocationProxyHost.trim().isEmpty()) {
                String message = "Certificate revocation proxy server host is not configured. Please do set the " +
                        "'CertificateManagement -> CertificateRevocationProxy -> ProxyHost' file";
                log.error(message);
                throw new CertificateValidationException(message);
            }
            if (log.isDebugEnabled()) {
                log.debug("Certificate revocation proxy: " + certificateRevocationProxyHost + ":" +
                        certificateRevocationProxyPort);
            }
            HttpHost proxy = new HttpHost(certificateRevocationProxyHost, certificateRevocationProxyPort);
            RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
            httpGet.setConfig(config);
            log.debug("Setting certificate revocation proxy finished.");
        }

        // adding request timeout configurations
        RequestConfig timeoutRequestConfig;
        if (httpGet.getConfig() == null) {
            httpGet.setConfig(RequestConfig.custom().build());
        }
        timeoutRequestConfig = RequestConfig.copy(httpGet.getConfig())
                .setConnectTimeout(httpConnectTimeout)
                .setConnectionRequestTimeout(httpConnectionRequestTimeout)
                .setSocketTimeout(httpSocketTimeout)
                .build();
        httpGet.setConfig(timeoutRequestConfig);
        if (log.isDebugEnabled()) {
            log.debug("CRL request timeout configurations: " + "httpConnectTimeout: " + httpConnectTimeout +
                    ", httpConnectionRequestTimeout: " + httpConnectionRequestTimeout + ", httpSocketTimeout: " +
                    httpSocketTimeout);
        }


        try (CloseableHttpResponse httpResponse = HTTPClientUtils.getHttpsClient().execute(httpGet)) {
            //Check errors in response:
            if (httpResponse.getStatusLine().getStatusCode() / 100 != 2) {
                throw new CertificateValidationException("Error getting crl response." +
                        "Response code is " + httpResponse.getStatusLine().getStatusCode());
            }
            InputStream in = httpResponse.getEntity().getContent();

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509CRL x509CRLDownloaded = (X509CRL) cf.generateCRL(in);
            in.close();
            if (log.isDebugEnabled()) {
                log.debug("CRL is downloaded from CRL Url: " + crlURL);
            }

            if (isValidX509Crl(x509CRLDownloaded, peerCert, issuerCert)) {
                x509CRL = x509CRLDownloaded;
            }
        } catch (MalformedURLException e) {
            throw new CertificateValidationException("CRL Url is malformed", e);
        } catch (IOException e) {
            if (retryCount == 0) {
                throw new CertificateValidationException("Cant reach the CRL Url: " + crlURL, e);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Cant reach CRL Url: " + crlURL + ". Retrying to connect - attempt " + retryCount);
                }
                return downloadCRLFromWeb(crlURL, --retryCount, peerCert, issuerCert,
                        certificateRevocationProxyEnabled, certificateRevocationProxyHost,
                        certificateRevocationProxyPort);
            }
        } catch (CertificateException e) {
            throw new CertificateValidationException("Error when generating certificate factory.", e);
        } catch (CRLException e) {
            throw new CertificateValidationException("Cannot generate X509CRL from the stream data", e);
        } catch (OpenBankingException e) {
            throw new CertificateValidationException("Error when creating http client.", e);
        }
        return x509CRL;
    }

    private static RevocationStatus getRevocationStatusFromCRL(X509CRL x509CRL, X509Certificate peerCert) {

        if (x509CRL.isRevoked(peerCert)) {
            return RevocationStatus.REVOKED;
        } else {
            return RevocationStatus.GOOD;
        }
    }

    private static byte[] getCRLDPExtensionValue(X509Certificate cert) {

        //DER-encoded octet string of the extension value for CRLDistributionPoints identified by the passed-in oid
        return cert.getExtensionValue(Extension.cRLDistributionPoints.getId());
    }

    private static CRLDistPoint getCrlDistPoint(byte[] crlDPExtensionValue) throws CertificateValidationException {

        //crlDPExtensionValue is encoded in ASN.1 format
        //DER (Distinguished Encoding Rules) is one of ASN.1 encoding rules defined in ITU-T X.690, 2002, specification.
        //ASN.1 encoding rules can be used to encode any data object into a binary file. Read the object in octets.
        CRLDistPoint distPoint;
        try (ASN1InputStream crlDPEx = new ASN1InputStream(crlDPExtensionValue);
             ASN1InputStream asn1InOctets =
                     new ASN1InputStream(((DEROctetString) (crlDPEx).readObject()).getOctets())) {
            //Get Input stream in octets
            ASN1Primitive crlDERObject = asn1InOctets.readObject();
            distPoint = CRLDistPoint.getInstance(crlDERObject);
        } catch (IOException e) {
            throw new CertificateValidationException("Cannot read certificate to get CRL urls", e);
        }
        return distPoint;
    }

    private static List<String> getCrlUrlsFromDistPoint(CRLDistPoint distPoint) {

        List<String> crlUrls = new ArrayList<>();
        //Loop through ASN1Encodable DistributionPoints
        for (DistributionPoint dp : distPoint.getDistributionPoints()) {
            //get ASN1Encodable DistributionPointName
            DistributionPointName dpn = dp.getDistributionPoint();
            if (dpn != null && dpn.getType() == DistributionPointName.FULL_NAME) {
                //Create ASN1Encodable General Names
                GeneralName[] genNames = GeneralNames.getInstance(dpn.getName()).getNames();
                // Look for a URI
                for (GeneralName genName : genNames) {
                    if (genName.getTagNo() == GeneralName.uniformResourceIdentifier) {
                        //DERIA5String contains an ascii string.
                        //A IA5String is a restricted character string type in the ASN.1 notation
                        String url = DERIA5String.getInstance(genName.getName()).getString().trim();
                        crlUrls.add(url);
                    }
                }
            }
        }
        return crlUrls;
    }

    /**
     * Checks revocation status (Good, Revoked) of the peer certificate.
     *
     * @param peerCert   peer certificate
     * @param issuerCert issuer certificate of the peer
     * @return revocation status of the peer certificate
     * @throws CertificateValidationException certificateValidationException
     */
    @Override
    public RevocationStatus checkRevocationStatus(X509Certificate peerCert, X509Certificate issuerCert)
            throws CertificateValidationException {

        TPPCertValidatorDataHolder tppCertValidatorDataHolder = TPPCertValidatorDataHolder.getInstance();

        final boolean isCertificateRevocationProxyEnabled = tppCertValidatorDataHolder
                .isCertificateRevocationProxyEnabled();
        final int certificateRevocationProxyPort = tppCertValidatorDataHolder
                .getCertificateRevocationProxyPort();
        final String certificateRevocationProxyHost = tppCertValidatorDataHolder
                .getCertificateRevocationProxyHost();

        List<String> crlUrls = getCRLUrls(peerCert);
        return getCRLRevocationStatus(peerCert, issuerCert, retryCount, crlUrls, isCertificateRevocationProxyEnabled,
                certificateRevocationProxyHost, certificateRevocationProxyPort);
    }

    @Override
    public int getRetryCount() {

        return retryCount;
    }
}
