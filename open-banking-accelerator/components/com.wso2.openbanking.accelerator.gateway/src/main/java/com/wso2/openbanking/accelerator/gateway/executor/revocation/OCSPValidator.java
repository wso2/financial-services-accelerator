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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.bouncycastle.asn1.ASN1IA5String;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.ocsp.OCSPResponseStatus;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.CertificateID;
import org.bouncycastle.cert.ocsp.CertificateStatus;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPReq;
import org.bouncycastle.cert.ocsp.OCSPReqBuilder;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.SingleResp;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * This is used to verify a certificate is revoked or not by using the Online Certificate Status Protocol published
 * by the CA.
 */
public class OCSPValidator implements RevocationValidator {

    private static final Log log = LogFactory.getLog(OCSPValidator.class);

    private static final String BC = "BC";
    private final int retryCount;
    private static int httpConnectTimeout;
    private static int httpConnectionRequestTimeout;
    private static int httpSocketTimeout;

    public OCSPValidator(int retryCount) {

        this.retryCount = retryCount;
    }

    public OCSPValidator(int retryCount, int httpConnectTimeout, int httpConnectionRequestTimeout,
                         int httpSocketTimeout) {

        this.retryCount = retryCount;
        OCSPValidator.httpConnectTimeout = httpConnectTimeout;
        OCSPValidator.httpConnectionRequestTimeout = httpConnectionRequestTimeout;
        OCSPValidator.httpSocketTimeout = httpSocketTimeout;
    }

    /**
     * Authority Information Access (AIA) is a non-critical extension in an X509 Certificate. This contains the
     * URL of the OCSP endpoint if one is available.
     *
     * @param cert is the certificate
     * @return a list of URLs in AIA extension of the certificate which will hopefully contain an OCSP endpoint
     * @throws CertificateValidationException certificateValidationException
     */
    public static List<String> getAIALocations(X509Certificate cert) throws CertificateValidationException {

        List<String> ocspUrlList;
        byte[] aiaExtensionValue = getAiaExtensionValue(cert);
        if (aiaExtensionValue == null) {
            throw new CertificateValidationException("Certificate with serial num: " +
                    cert.getSerialNumber() + " doesn't have Authority Information Access points");
        }
        AuthorityInformationAccess authorityInformationAccess = getAuthorityInformationAccess(aiaExtensionValue);
        ocspUrlList = getOcspUrlsFromAuthorityInfoAccess(authorityInformationAccess);

        if (ocspUrlList.isEmpty()) {
            throw new CertificateValidationException("Cant get OCSP urls from certificate with serial num: " +
                    cert.getSerialNumber());
        }

        return ocspUrlList;
    }

    /**
     * This method generates an OCSP Request to be sent to an OCSP endpoint.
     *
     * @param issuerCert   is the Certificate of the Issuer of the peer certificate we are interested in
     * @param serialNumber of the peer certificate
     * @return generated OCSP request
     * @throws CertificateValidationException certificateRevocationValidationException
     */
    private static OCSPReq generateOCSPRequest(X509Certificate issuerCert, BigInteger serialNumber)
            throws CertificateValidationException {

        // Add provider BC
        Security.addProvider(new BouncyCastleProvider());
        try {

            byte[] issuerCertEnc = issuerCert.getEncoded();
            X509CertificateHolder certificateHolder = new X509CertificateHolder(issuerCertEnc);
            DigestCalculatorProvider digCalcProv = new JcaDigestCalculatorProviderBuilder().setProvider(BC).build();

            // CertID structure is used to uniquely identify certificates that are the subject of
            // an OCSP request or response and has an ASN.1 definition. CertID structure is defined in RFC 2560
            CertificateID id = new CertificateID(digCalcProv.get(CertificateID.HASH_SHA1), certificateHolder,
                    serialNumber);

            // basic request generation with nonce
            OCSPReqBuilder builder = new OCSPReqBuilder();
            builder.addRequest(id);

            // create details for nonce extension. The nonce extension is used to bind a request to a response to
            // prevent replay attacks. As the name implies, the nonce value is something that the client should only
            // use once within a reasonably small period.
            BigInteger nonce = BigInteger.valueOf(System.currentTimeMillis());

            // create the request Extension
            builder.setRequestExtensions(new Extensions(new Extension(OCSPObjectIdentifiers.id_pkix_ocsp_nonce, false,
                    new DEROctetString(nonce.toByteArray()))));

            return builder.build();
        } catch (CertificateEncodingException | IOException | OCSPException | OperatorCreationException e) {
            throw new CertificateValidationException("Cannot generate OSCP Request with the given certificate with " +
                    "serial num: " + serialNumber, e);
        }
    }

    /**
     * Get revocation status of a certificate using OCSP Url.
     *
     * @param peerCert                          peer certificate
     * @param issuerCert                        issuer certificate of peer
     * @param retryCount                        retry count to connect to OCSP Url and get the OCSP response
     * @param locations                         AIA locations
     * @param certificateRevocationProxyEnabled whether certificate revocation proxy enabled in the config
     * @param certificateRevocationProxyHost    certificate revocation proxy host
     * @param certificateRevocationProxyPort    certificate revocation proxy port
     * @return Revocation status of the certificate
     * @throws CertificateValidationException certificateValidationException
     */
    public static RevocationStatus getOCSPRevocationStatus(X509Certificate peerCert, X509Certificate issuerCert,
                                                           int retryCount, List<String> locations,
                                                           boolean certificateRevocationProxyEnabled,
                                                           String certificateRevocationProxyHost,
                                                           int certificateRevocationProxyPort)
            throws CertificateValidationException {

        OCSPReq request = generateOCSPRequest(issuerCert, peerCert.getSerialNumber());
        for (String serviceUrl : locations) {
            SingleResp[] responses;
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Trying to get OCSP Response from : " + serviceUrl);
                }
                OCSPResp ocspResponse = getOCSPResponse(serviceUrl, request, retryCount,
                        certificateRevocationProxyEnabled, certificateRevocationProxyHost,
                        certificateRevocationProxyPort);
                if (OCSPResponseStatus.SUCCESSFUL != ocspResponse.getStatus()) {
                    log.debug("OCSP Response is not successfully received.");
                    continue;
                }

                BasicOCSPResp basicResponse = (BasicOCSPResp) ocspResponse.getResponseObject();
                responses = (basicResponse == null) ? null : basicResponse.getResponses();
            } catch (OCSPException | CertificateValidationException e) {
                // On any error, consider the other AIA locations as well.
                log.debug("Certificate revocation check failed due to an exception", e);
                continue;
            }

            if (responses != null && responses.length == 1) {
                return getRevocationStatusFromOCSP(responses[0]);
            }
        }
        throw new CertificateValidationException("Cant get Revocation Status from OCSP using any of the OCSP Urls " +
                "for certificate with serial num:" + peerCert.getSerialNumber());
    }

    private static List<String> getOcspUrlsFromAuthorityInfoAccess(AuthorityInformationAccess
                                                                           authorityInformationAccess) {

        List<String> ocspUrlList = new ArrayList<>();
        AccessDescription[] accessDescriptions;
        if (authorityInformationAccess != null) {
            accessDescriptions = authorityInformationAccess.getAccessDescriptions();
            for (AccessDescription accessDescription : accessDescriptions) {

                GeneralName gn = accessDescription.getAccessLocation();
                if (gn.getTagNo() == GeneralName.uniformResourceIdentifier) {
                    ASN1IA5String str = ASN1IA5String.getInstance(gn.getName());
                    String accessLocation = str.getString();
                    ocspUrlList.add(accessLocation);
                }
            }
        }
        return ocspUrlList;
    }

    private static AuthorityInformationAccess getAuthorityInformationAccess(byte[] aiaExtensionValue)
            throws CertificateValidationException {

        AuthorityInformationAccess authorityInformationAccess;
        try (ASN1InputStream asn1InputStream =
                     new ASN1InputStream(((DEROctetString)
                             (new ASN1InputStream(new ByteArrayInputStream(aiaExtensionValue)).readObject()))
                             .getOctets())) {
            authorityInformationAccess = AuthorityInformationAccess.getInstance(asn1InputStream.readObject());
        } catch (IOException e) {
            throw new CertificateValidationException("Cannot read certificate to get OSCP urls", e);
        }
        return authorityInformationAccess;
    }

    private static byte[] getAiaExtensionValue(X509Certificate cert) {

        //Gets the DER-encoded OCTET string for the extension value for Authority information access Points
        return cert.getExtensionValue(Extension.authorityInfoAccess.getId());
    }

    /**
     * Gets an ASN.1 encoded OCSP response (as defined in RFC 2560) from the given service URL. Currently supports
     * only HTTP.
     *
     * @param serviceUrl                        URL of the OCSP endpoint.
     * @param request                           an OCSP request object.
     * @param certificateRevocationProxyEnabled whether certificate revocation proxy enabled in the config
     * @param certificateRevocationProxyHost    certificate revocation proxy host
     * @param certificateRevocationProxyPort    certificate revocation proxy port
     * @return OCSP response encoded in ASN.1 structure.
     * @throws CertificateValidationException certificateValidationException
     */
    private static OCSPResp getOCSPResponse(String serviceUrl, OCSPReq request, int retryCount,
                                            boolean certificateRevocationProxyEnabled,
                                            String certificateRevocationProxyHost,
                                            int certificateRevocationProxyPort)
            throws CertificateValidationException {

        OCSPResp ocspResp = null;
        if (log.isDebugEnabled()) {
            log.debug("Certificate revocation check proxy enabled: " + certificateRevocationProxyEnabled);
        }
        HttpPost httpPost = new HttpPost(serviceUrl);

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
            httpPost.setConfig(config);
            log.debug("Setting certificate revocation proxy finished.");
        }

        // adding request timeout configurations
        RequestConfig timeoutRequestConfig;
        if (httpPost.getConfig() == null) {
            httpPost.setConfig(RequestConfig.custom().build());
        }
        timeoutRequestConfig = RequestConfig.copy(httpPost.getConfig())
                .setConnectTimeout(httpConnectTimeout)
                .setConnectionRequestTimeout(httpConnectionRequestTimeout)
                .setSocketTimeout(httpSocketTimeout)
                .build();
        httpPost.setConfig(timeoutRequestConfig);

        if (log.isDebugEnabled()) {
            log.debug("OCSP request timeout configurations: " + "httpConnectTimeout: " + httpConnectTimeout +
                    ", httpConnectionRequestTimeout: " + httpConnectionRequestTimeout + ", httpSocketTimeout: " +
                    httpSocketTimeout);
        }

        try (CloseableHttpResponse httpResponse = HTTPClientUtils.getHttpsClient().execute(httpPost)) {

            setRequestProperties(request.getEncoded(), httpPost);
            //Check errors in response, if response status code is not 200 (success) range, throws exception
            // eg: if response code is 200 (success) or 201 (accepted) return true,
            //     if response code is 404 (not found) or 500 throw exception
            if (httpResponse.getStatusLine().getStatusCode() / 100 != 2) {
                throw new CertificateValidationException("Error getting ocsp response." +
                        "Response code is " + httpResponse.getStatusLine().getStatusCode());
            }
            InputStream in = httpResponse.getEntity().getContent();
            ocspResp = new OCSPResp(in);
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug("Certificate revocation check failed due to an exception", e);
            }
            if (retryCount == 0) {
                throw new CertificateValidationException("Cannot get ocspResponse from url: "
                        + serviceUrl, e);
            } else {
                log.info("Cant reach URI: " + serviceUrl + ". Retrying to connect - attempt " + retryCount);
                return getOCSPResponse(serviceUrl, request, --retryCount, certificateRevocationProxyEnabled,
                        certificateRevocationProxyHost, certificateRevocationProxyPort);
            }
        } catch (OpenBankingException e) {
            throw new CertificateValidationException("Error when creating http client.", e);
        }
        return ocspResp;
    }

    private static void setRequestProperties(byte[] message, HttpPost httpPost) {

        httpPost.addHeader(CertificateValidationUtils.HTTP_CONTENT_TYPE,
                CertificateValidationUtils.HTTP_CONTENT_TYPE_OCSP);
        httpPost.addHeader(CertificateValidationUtils.HTTP_ACCEPT,
                CertificateValidationUtils.HTTP_ACCEPT_OCSP);

        httpPost.setEntity(new ByteArrayEntity(message,
                ContentType.create(CertificateValidationUtils.CONTENT_TYPE)));
    }

    private static RevocationStatus getRevocationStatusFromOCSP(SingleResp resp)
            throws CertificateValidationException {

        Object status = resp.getCertStatus();
        if (status == CertificateStatus.GOOD) {
            return RevocationStatus.GOOD;
        } else if (status instanceof org.bouncycastle.cert.ocsp.RevokedStatus) {
            return RevocationStatus.REVOKED;
        } else if (status instanceof org.bouncycastle.cert.ocsp.UnknownStatus) {
            return RevocationStatus.UNKNOWN;
        }
        throw new CertificateValidationException("Cant recognize Certificate Status");
    }

    /**
     * Check revocation status (Good, Revoked, Unknown) of the peer certificate.
     *
     * @param peerCert   peer certificate
     * @param issuerCert issuer certificate of the peer
     * @return revocation status of the peer certificate
     * @throws CertificateValidationException certificateValidationException
     */
    @Override
    public RevocationStatus checkRevocationStatus(X509Certificate peerCert, X509Certificate issuerCert)
            throws CertificateValidationException {

        if (issuerCert == null) {
            throw new CertificateValidationException("Issuer Certificate is not available for " +
                    "OCSP validation");
        }
        List<String> locations = getAIALocations(peerCert);
        if (log.isDebugEnabled()) {
            log.debug("Peer certificate AIA locations: " + locations);
        }
        TPPCertValidatorDataHolder tppCertValidatorDataHolder = TPPCertValidatorDataHolder.getInstance();

        final boolean isCertificateRevocationProxyEnabled = tppCertValidatorDataHolder
                .isCertificateRevocationProxyEnabled();
        final int certificateRevocationProxyPort = tppCertValidatorDataHolder
                .getCertificateRevocationProxyPort();
        final String certificateRevocationProxyHost = tppCertValidatorDataHolder
                .getCertificateRevocationProxyHost();

        return getOCSPRevocationStatus(peerCert, issuerCert, retryCount, locations, isCertificateRevocationProxyEnabled
                , certificateRevocationProxyHost, certificateRevocationProxyPort);
    }

    @Override
    public int getRetryCount() {

        return retryCount;
    }
}
