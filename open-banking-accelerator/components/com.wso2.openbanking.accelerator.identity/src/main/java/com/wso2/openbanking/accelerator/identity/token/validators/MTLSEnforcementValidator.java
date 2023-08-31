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

package com.wso2.openbanking.accelerator.identity.token.validators;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.CertificateUtils;
import com.wso2.openbanking.accelerator.identity.token.util.TokenFilterException;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Mutual TLS Enforcement Filter Validator.
 * Enforces whether the Request is sent with MTLS cert as a header.
 */
public class MTLSEnforcementValidator implements OBIdentityFilterValidator {

    private static final Log log = LogFactory.getLog(MTLSEnforcementValidator.class);

    @Override
    public void validate(ServletRequest request, String clientId) throws TokenFilterException, ServletException {

        if (request instanceof HttpServletRequest) {

            HttpServletRequest servletRequest = (HttpServletRequest) request;
            String x509Certificate = servletRequest.getHeader(IdentityCommonUtil.getMTLSAuthHeader());
            try {
                if (!(StringUtils.isNotEmpty(x509Certificate) &&
                        CertificateUtils.parseCertificate(x509Certificate) != null)) {
                    throw new TokenFilterException(HttpServletResponse.SC_BAD_REQUEST, IdentityCommonConstants
                            .OAUTH2_INVALID_REQUEST_MESSAGE, "Transport certificate not passed through the " +
                            "request or the certificate is not valid");
                }
            } catch (TokenFilterException e) {
                throw new TokenFilterException(e.getErrorCode(), e.getMessage(), e.getErrorDescription());
            } catch (OpenBankingException e) {
                throw new TokenFilterException(HttpServletResponse.SC_BAD_REQUEST, IdentityCommonConstants
                        .OAUTH2_INVALID_CLIENT_MESSAGE, "Invalid transport certificate. " +
                        e.getMessage(), e);
            }
        }
    }
}
