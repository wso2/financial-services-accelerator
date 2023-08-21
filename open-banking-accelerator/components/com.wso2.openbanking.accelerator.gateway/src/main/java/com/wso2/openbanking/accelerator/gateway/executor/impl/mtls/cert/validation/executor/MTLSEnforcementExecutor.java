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
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.gateway.executor.core.OpenBankingGatewayExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OpenBankingExecutorError;
import com.wso2.openbanking.accelerator.gateway.executor.util.CertificateValidationUtils;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Optional;

/**
 * Mutual TLS Enforcement Executor
 * Enforces whether the Request is sent with MTLS cert as a header.
 */
public class MTLSEnforcementExecutor implements OpenBankingGatewayExecutor {

    private static final Log LOG = LogFactory.getLog(MTLSEnforcementExecutor.class);

    @Generated(message = "Ignoring since all cases are covered from other unit tests")
    @Override
    public void preProcessRequest(OBAPIRequestContext obapiRequestContext) {

        LOG.info("Starting mutual TLS enforcement process");

        // Skip the executor if previous executors failed.
        if (obapiRequestContext.isError()) {
            return;
        }

        Certificate[] clientCerts = obapiRequestContext.getClientCertsLatest();
        if (clientCerts != null && clientCerts.length > 0) {
            Optional<X509Certificate> transportCert = Optional.empty();
            try {
                transportCert = CertificateValidationUtils.convertCertToX509Cert(clientCerts[0]);
            } catch (CertificateException e) {
                String errorMsg = "Error occurred while converting the client certificate to X509Certificate ";
                LOG.error(errorMsg, e);
                OpenBankingExecutorError error = new OpenBankingExecutorError(
                        OpenBankingErrorCodes.INVALID_MTLS_CERT_CODE, errorMsg,
                        e.getMessage(), OpenBankingErrorCodes.UNAUTHORIZED_CODE);
                CertificateValidationUtils.handleExecutorErrors(error, obapiRequestContext);
            }

            if (transportCert.isPresent()) {
                LOG.debug("Mutual TLS enforcement success");
            } else {
                LOG.error(GatewayConstants.CLIENT_CERTIFICATE_INVALID);
                OpenBankingExecutorError error = new OpenBankingExecutorError(
                        OpenBankingErrorCodes.INVALID_MTLS_CERT_CODE, GatewayConstants.INVALID_CLIENT,
                        GatewayConstants.CLIENT_CERTIFICATE_INVALID, OpenBankingErrorCodes.UNAUTHORIZED_CODE);

                CertificateValidationUtils.handleExecutorErrors(error, obapiRequestContext);
            }
        } else {
            LOG.error(GatewayConstants.CLIENT_CERTIFICATE_MISSING);
            OpenBankingExecutorError error = new OpenBankingExecutorError(
                    OpenBankingErrorCodes.MISSING_MTLS_CERT_CODE, GatewayConstants.INVALID_CLIENT,
                    GatewayConstants.CLIENT_CERTIFICATE_MISSING, OpenBankingErrorCodes.UNAUTHORIZED_CODE);

            CertificateValidationUtils.handleExecutorErrors(error, obapiRequestContext);
        }

    }

    @Override
    public void preProcessResponse(OBAPIResponseContext obapiResponseContext) {
        // Do not need to handle the response
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
