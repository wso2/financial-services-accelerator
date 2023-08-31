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

package com.wso2.openbanking.accelerator.gateway.executor.impl.mtls.cert.validation.executor;

import com.wso2.openbanking.accelerator.common.error.OpenBankingErrorCodes;
import com.wso2.openbanking.accelerator.common.exception.CertificateValidationException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.gateway.cache.CertificateRevocationCache;
import com.wso2.openbanking.accelerator.gateway.cache.GatewayCacheKey;
import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OpenBankingExecutorError;
import com.wso2.openbanking.accelerator.gateway.executor.service.CertValidationService;
import com.wso2.openbanking.accelerator.gateway.executor.util.CertificateValidationUtils;
import com.wso2.openbanking.accelerator.gateway.internal.TPPCertValidatorDataHolder;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;

/**
 * This executor will be used to validate the certificate revocation (CRL and OCSP validation) of the client
 * certificate during a mutual tls session. The immediate issuer of the client certificate must be present in the
 * truststore to continue with the validation.
 */
public class CertRevocationValidationExecutor implements OpenBankingGatewayExecutor {

    private static final Log LOG = LogFactory.getLog(CertRevocationValidationExecutor.class);

    @Generated(message = "Ignoring since all cases are covered from other unit tests")
    @Override
    public void preProcessRequest(OBAPIRequestContext obapiRequestContext) {

        LOG.info("Starting certificate revocation validation process");

        // Skip the executor if previous executors failed.
        if (obapiRequestContext.isError()) {
            return;
        }

        try {
            Certificate[] clientCerts = obapiRequestContext.getClientCertsLatest();
            // enforcement executor validates the certificate presence
            if (clientCerts != null && clientCerts.length > 0) {
                Optional<X509Certificate> transportCert =
                        CertificateValidationUtils.convertCertToX509Cert(clientCerts[0]);

                if (!transportCert.isPresent()) {
                    LOG.error("Invalid mutual TLS request. Client certificate is invalid");
                    OpenBankingExecutorError error = new OpenBankingExecutorError(
                            OpenBankingErrorCodes.INVALID_MTLS_CERT_CODE,
                            "Invalid mutual TLS request. Client certificate is invalid",
                            "", OpenBankingErrorCodes.UNAUTHORIZED_CODE);

                    CertificateValidationUtils.handleExecutorErrors(error, obapiRequestContext);
                } else {
                    X509Certificate transportCertificate = transportCert.get();
                    if (CertificateValidationUtils.isExpired(transportCertificate)) {
                        LOG.error("Certificate with the serial number " +
                                transportCertificate.getSerialNumber() + " issued by the CA " +
                                transportCertificate.getIssuerDN().toString() + " is expired");
                        OpenBankingExecutorError error = new OpenBankingExecutorError(
                                OpenBankingErrorCodes.EXPIRED_MTLS_CERT_CODE,
                                "Invalid mutual TLS request. Client certificate is expired",
                                "Certificate with the serial number " +
                                        transportCertificate.getSerialNumber() + " issued by the CA " +
                                        transportCertificate.getIssuerDN().toString() + " is expired",
                                OpenBankingErrorCodes.UNAUTHORIZED_CODE);

                        CertificateValidationUtils.handleExecutorErrors(error, obapiRequestContext);
                    } else {
                        LOG.debug("Client certificate expiry validation completed successfully");
                        if (isCertRevoked(transportCertificate)) {
                            LOG.error("Invalid mutual TLS request. Client certificate is revoked");
                            OpenBankingExecutorError error = new OpenBankingExecutorError(
                                    OpenBankingErrorCodes.REVOKED_MTLS_CERT_CODE,
                                    "Invalid mutual TLS request. Client certificate is revoked",
                                    "", OpenBankingErrorCodes.UNAUTHORIZED_CODE);

                            CertificateValidationUtils.handleExecutorErrors(error, obapiRequestContext);
                        } else {
                            LOG.debug("Certificate revocation validation success");
                        }
                    }
                }
            }
        } catch (CertificateValidationException e) {
            LOG.error("Unable to validate the client certificate, caused by ", e);

            //catch errors and set to context
            CertificateValidationUtils.handleExecutorErrors(e, obapiRequestContext);
        } catch (CertificateEncodingException e) {
            LOG.error("Unable to generate the client certificate thumbprint, caused by ", e);
            OpenBankingExecutorError error = new OpenBankingExecutorError(
                    OpenBankingErrorCodes.INVALID_MTLS_CERT_CODE,
                    "Unable to generate the client certificate thumbprint",
                    "", OpenBankingErrorCodes.UNAUTHORIZED_CODE);

            //catch errors and set to context
            CertificateValidationUtils.handleExecutorErrors(error, obapiRequestContext);
        } catch (CertificateException e) {
            String errorMsg = "Error occurred while converting the client certificate to X509Certificate ";
            LOG.error(errorMsg, e);
            OpenBankingExecutorError error = new OpenBankingExecutorError(
                    OpenBankingErrorCodes.INVALID_MTLS_CERT_CODE, errorMsg,
                    e.getMessage(), OpenBankingErrorCodes.UNAUTHORIZED_CODE);
            CertificateValidationUtils.handleExecutorErrors(error, obapiRequestContext);
        }
    }

    @Override
    public void preProcessResponse(OBAPIResponseContext obapiResponseContext) {
        // Do not need to handle the response
    }

    /**
     * Checks the certificate validity of a given certificate. For this validation, the immediate issuer
     * of the peer certificate must be present in the trust store.
     * JSONObject jsonObject;
     *
     * @param peerCertificate peer certificate
     * @return validity of the certificate
     * @throws CertificateValidationException when an error occurs while validating the certificate
     */
    private boolean isCertRevoked(X509Certificate peerCertificate)
            throws CertificateValidationException, CertificateEncodingException {

        // Initializing certificate cache and cache key
        CertificateRevocationCache certificateRevocationCache = CertificateRevocationCache.getInstance();
        // Generating the certificate thumbprint to use as cache key
        String certificateValidationCacheKeyStr = DigestUtils.sha256Hex(peerCertificate.getEncoded());
        GatewayCacheKey certificateValidationCacheKey =
                GatewayCacheKey.of(certificateValidationCacheKeyStr);

        // Executing certificate revocation process or retrieve last status from cache
        if (certificateRevocationCache.getFromCache(certificateValidationCacheKey) != null) {
            // previous result is present in cache, return result
            return !certificateRevocationCache.getFromCache(certificateValidationCacheKey);
        } else {
            final boolean result = isCertRevocationSuccess(peerCertificate);
            if (result) {
                // Adding result to cache
                certificateRevocationCache.addToCache(certificateValidationCacheKey, true);
                return false;
            }
        }
        return true;
    }

    private boolean isCertRevocationSuccess(X509Certificate peerCertificate) {

        TPPCertValidatorDataHolder tppCertValidatorDataHolder = TPPCertValidatorDataHolder.getInstance();

        Integer certificateRevocationValidationRetryCount =
                tppCertValidatorDataHolder.getCertificateRevocationValidationRetryCount();

        int connectTimeout = tppCertValidatorDataHolder.getConnectTimeout();
        int connectionRequestTimeout = tppCertValidatorDataHolder.getConnectionRequestTimeout();
        int socketTimeout = tppCertValidatorDataHolder.getSocketTimeout();

        boolean isValid;
        // Check certificate revocation status.
        if (tppCertValidatorDataHolder.isCertificateRevocationValidationEnabled()) {
            LOG.debug("Client certificate revocation validation is enabled");

            // Skip certificate revocation validation if the certificate is self-signed.
            if (peerCertificate.getSubjectDN().getName().equals(peerCertificate.getIssuerDN().getName())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Client certificate is self signed. Hence, excluding the certificate revocation" +
                            " validation");
                }
                return true;
            }

            /*
             *  Skip certificate revocation validation if the certificate issuer is listed to exclude from
             *  revocation validation in open-banking.xml under
             *  CertificateManagement.RevocationValidationExcludedIssuers configuration.
             *
             *  This option can be used to skip certificate revocation validation for certificates which have been
             *  issued by a trusted locally generated CA.
             */
            List<String> revocationValidationExcludedIssuers =
                    tppCertValidatorDataHolder.getCertificateRevocationValidationExcludedIssuers();
            if (revocationValidationExcludedIssuers.contains(peerCertificate.getIssuerDN().getName())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("The issuer of the client certificate has been configured to exclude from " +
                            "certificate revocation validation. Hence, excluding the certificate " +
                            "revocation validation");
                }
                return true;
            }

            // Get issuer certificate from the truststore to continue with the certificate validation.
            X509Certificate issuerCertificate;
            try {
                issuerCertificate = CertificateValidationUtils
                        .getIssuerCertificateFromTruststore(peerCertificate);
            } catch (CertificateValidationException e) {
                LOG.error("Issuer certificate retrieving failed for client certificate with" +
                        " serial number " + peerCertificate.getSerialNumber() + " issued by the CA " +
                        peerCertificate.getIssuerDN().toString(), e);
                return false;
            }

            isValid = CertValidationService.getInstance().verify(peerCertificate, issuerCertificate,
                    certificateRevocationValidationRetryCount, connectTimeout, connectionRequestTimeout, socketTimeout);
        } else {
            isValid = true;
        }

        LOG.debug("Stored certificate validation status in cache");

        return isValid;
    }

    /**
     * Method to handle post response.
     *
     * @param obapiResponseContext OB response context object
     */
    @Override
    public void postProcessResponse(OBAPIResponseContext obapiResponseContext) {

    }

    /**
     * Method to handle post request.
     *
     * @param obapiRequestContext OB request context object
     */
    @Override
    public void postProcessRequest(OBAPIRequestContext obapiRequestContext) {

    }
}
