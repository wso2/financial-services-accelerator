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

import com.nimbusds.jwt.SignedJWT;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.identity.token.util.TokenFilterException;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonHelper;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.ParseException;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Filter validator to check if the client assertion is signed with the registered algorithm.
 */
public class SignatureAlgorithmEnforcementValidator implements OBIdentityFilterValidator {

    private static final Log log = LogFactory.getLog(SignatureAlgorithmEnforcementValidator.class);

    @Override
    public void validate(ServletRequest request, String clientId) throws TokenFilterException {

        if (request instanceof HttpServletRequest) {
            String signedObject = request.getParameter(IdentityCommonConstants.OAUTH_JWT_ASSERTION);
            if (StringUtils.isNotEmpty(signedObject)) {
                validateInboundSignatureAlgorithm(getRequestSigningAlgorithm(signedObject),
                        getRegisteredSigningAlgorithm(clientId));
            }
        }
    }

    /**
     * Checks if the incoming signed request is signed with the registered algorithms during service provider creation.
     *
     * @param requestSigningAlgorithm    the algorithm of signed message
     * @param registeredSigningAlgorithm the algorithm registered during client authentication
     */
    public void validateInboundSignatureAlgorithm(String requestSigningAlgorithm, String registeredSigningAlgorithm)
            throws TokenFilterException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Validating request algorithm %s against registered algorithm %s.",
                    requestSigningAlgorithm, registeredSigningAlgorithm));
        }
        if (registeredSigningAlgorithm.equals(IdentityCommonConstants.NOT_APPLICABLE)) {
            return;
        }
        if (!(StringUtils.isNotEmpty(requestSigningAlgorithm) &&
                requestSigningAlgorithm.equals(registeredSigningAlgorithm))) {
            throw new TokenFilterException(HttpServletResponse.SC_UNAUTHORIZED, IdentityCommonConstants
                    .OAUTH2_INVALID_CLIENT_MESSAGE, "Registered algorithm does not match with the token " +
                    "signed algorithm");
        }
    }

    @Generated(message = "Ignoring because it requires a service call")
    public String getRegisteredSigningAlgorithm(String clientId) throws TokenFilterException {

        try {
            if (!(StringUtils.isNotEmpty(new IdentityCommonHelper().getCertificateContent(clientId))
                    && IdentityCommonUtil.getRegulatoryFromSPMetaData(clientId))) {
                return new IdentityCommonHelper().getAppPropertyFromSPMetaData(clientId,
                        IdentityCommonConstants.TOKEN_ENDPOINT_AUTH_SIGNING_ALG);
            }
        } catch (OpenBankingException e) {
            throw new TokenFilterException(HttpServletResponse.SC_UNAUTHORIZED, IdentityCommonConstants
                    .OAUTH2_INVALID_REQUEST_MESSAGE, "Token signing algorithm not registered", e);
        }
        return IdentityCommonConstants.NOT_APPLICABLE;
    }

    public String getRequestSigningAlgorithm(String signedObject) throws TokenFilterException {
        //retrieve algorithm from the signed JWT
        try {
            SignedJWT signedJWT = SignedJWT.parse(signedObject);
            return signedJWT.getHeader().getAlgorithm().getName();
        } catch (ParseException e) {
            throw new TokenFilterException(HttpServletResponse.SC_BAD_REQUEST, IdentityCommonConstants
                    .OAUTH2_INVALID_CLIENT_MESSAGE, "Error occurred while parsing the signed assertion", e);
        }
    }
}
