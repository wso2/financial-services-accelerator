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
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.identity.token.util.TokenFilterException;
import com.wso2.openbanking.accelerator.identity.util.ClientAuthenticatorEnum;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonHelper;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Validates whether the registered client authentication method is invoked.
 */
public class ClientAuthenticatorValidator implements OBIdentityFilterValidator {

    private static final Log log = LogFactory.getLog(ClientAuthenticatorValidator.class);

    @Override
    public void validate(ServletRequest request, String clientId) throws TokenFilterException, ServletException {

        if (request instanceof HttpServletRequest) {
            String registeredClientAuthMethod = retrieveRegisteredAuthMethod(clientId);

            if (registeredClientAuthMethod.equals(IdentityCommonConstants.NOT_APPLICABLE)) {
                return;
            }

            // There can be multiple registered client auth methods
            if (!(registeredClientAuthMethod.contains(retrieveRequestAuthMethod(request)))) {
                throw new TokenFilterException(HttpServletResponse.SC_BAD_REQUEST, IdentityCommonConstants
                        .OAUTH2_INVALID_REQUEST_MESSAGE, "Request does not follow the registered token endpoint auth " +
                        "method " + registeredClientAuthMethod);
            }
        } else {
            throw new ServletException("Error occurred during request validation, passed request is not a " +
                    "HttpServletRequest");
        }
    }

    /**
     * Get the authentication method that matches the request.
     *
     * @param request servlet request
     * @return authentication method
     */
    @Generated(message = "Excluding from code coverage because a the actual implementation test cases are coverd")
    public String retrieveRequestAuthMethod(ServletRequest request) throws TokenFilterException {

        try {
            if (isPrivateKeyJWTAuthentication(request)) {
                log.debug("Validating request with JWT client authentication method");
                return ClientAuthenticatorEnum.PRIVATE_KEY_JWT.toString();
            } else if (new IdentityCommonHelper().isMTLSAuthentication(request)) {
                log.debug("Validating request with MTLS client authentication method");
                return ClientAuthenticatorEnum.TLS_CLIENT_AUTH.toString();
            }
            return "INVALID_AUTH";
        } catch (OpenBankingException e) {
            throw new TokenFilterException(HttpServletResponse.SC_UNAUTHORIZED, IdentityCommonConstants
                    .OAUTH2_INVALID_REQUEST_MESSAGE, e.getMessage());
        }
    }

    /**
     * Validate whether the request follows the private key jwt authentication pattern.
     *
     * @param request servlet request
     * @return whether request fallows PKJWT pattern
     */
    public boolean isPrivateKeyJWTAuthentication(ServletRequest request) {

        String oauthJWTAssertionType = request.getParameter(IdentityCommonConstants.OAUTH_JWT_ASSERTION_TYPE);
        String oauthJWTAssertion = request.getParameter(IdentityCommonConstants.OAUTH_JWT_ASSERTION);
        return IdentityCommonConstants.OAUTH_JWT_BEARER_GRANT_TYPE.equals(oauthJWTAssertionType) &&
                StringUtils.isNotEmpty(oauthJWTAssertion);
    }

    /**
     * Retrieve client authentication method from sp metadata.
     *
     * @param clientId auth client ID
     * @return the value of the client authentication method registered
     * @throws TokenFilterException
     */
    @Generated(message = "Excluding from code coverage because a service call is required for the method")
    public String retrieveRegisteredAuthMethod(String clientId) throws TokenFilterException {

        try {
            if (!(StringUtils.isNotEmpty(new IdentityCommonHelper().getCertificateContent(clientId))
                    && IdentityCommonUtil.getRegulatoryFromSPMetaData(clientId))) {
                return new IdentityCommonHelper().getAppPropertyFromSPMetaData(clientId,
                        IdentityCommonConstants.TOKEN_ENDPOINT_AUTH_METHOD);
            }
        } catch (OpenBankingException e) {
            throw new TokenFilterException(HttpServletResponse.SC_UNAUTHORIZED, IdentityCommonConstants
                    .OAUTH2_INVALID_REQUEST_MESSAGE, "Client authentication method not registered", e);
        }
        return IdentityCommonConstants.NOT_APPLICABLE;
    }
}
