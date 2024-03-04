/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.accelerator.identity.token.validators;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.CertificateUtils;
import com.wso2.openbanking.accelerator.identity.token.util.TokenFilterException;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.cert.X509Certificate;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * MTLS Certificate Validator.
 * Validates the expiry status of the certificate.
 */
public class MTLSCertificateValidator implements OBIdentityFilterValidator {

    private static final Log log = LogFactory.getLog(MTLSCertificateValidator.class);

    @Override
    public void validate(ServletRequest request, String clientId) throws TokenFilterException, ServletException {

        HttpServletRequest servletRequest = (HttpServletRequest) request;
        String mtlsCertificate = servletRequest.getHeader(IdentityCommonUtil.getMTLSAuthHeader());
        // MTLSEnforcementValidator validates the presence of the certificate.
        if (mtlsCertificate != null) {
            try {
                X509Certificate x509Certificate = CertificateUtils.parseCertificate(mtlsCertificate);

                if (CertificateUtils.isExpired(x509Certificate)) {
                    log.error("Certificate with the serial number " +
                            x509Certificate.getSerialNumber() + " issued by the CA " +
                            x509Certificate.getIssuerDN().toString() + " is expired");
                    throw new TokenFilterException(HttpServletResponse.SC_UNAUTHORIZED,
                            "Invalid mutual TLS request. Client certificate is expired",
                            "Certificate with the serial number " + x509Certificate.getSerialNumber() +
                                    " issued by the CA " + x509Certificate.getIssuerDN().toString() + " is expired");
                }
                log.debug("Client certificate expiry validation completed successfully");
            } catch (OpenBankingException e) {
                log.error("Invalid mutual TLS request. Client certificate is invalid", e);
                throw new TokenFilterException(HttpServletResponse.SC_UNAUTHORIZED, IdentityCommonConstants
                        .OAUTH2_INVALID_CLIENT_MESSAGE, e.getMessage());
            }
        }
    }
}
