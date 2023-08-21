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

package com.wso2.openbanking.accelerator.ciba;

import com.wso2.openbanking.accelerator.identity.token.util.TokenFilterException;
import com.wso2.openbanking.accelerator.identity.token.validators.SignatureAlgorithmEnforcementValidator;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;

import javax.servlet.http.HttpServletResponse;

/**
 * CIBA Signature Algorithm Enforcer class
 */
public class OBCIBASignatureAlgorithmEnforcementValidator extends SignatureAlgorithmEnforcementValidator {

    /**
     * CIBA and FAPI related validations for Signature Algorithm.
     * @param requestSigningAlgorithm    the algorithm of signed message
     * @param registeredSigningAlgorithm the algorithm registered during client authentication
     * @throws TokenFilterException
     */
    @Override
    public void validateInboundSignatureAlgorithm(String requestSigningAlgorithm, String registeredSigningAlgorithm)
            throws TokenFilterException {

        super.validateInboundSignatureAlgorithm(requestSigningAlgorithm, registeredSigningAlgorithm);
        if (OAuthServerConfiguration.getInstance().isFapiCiba()) {
            if (!(IdentityCommonConstants.ALG_ES256.equals(requestSigningAlgorithm) ||
                    IdentityCommonConstants.ALG_PS256.equals(requestSigningAlgorithm))) {
                String message = "FAPI unsupported signing algorithm " + requestSigningAlgorithm
                        + " used to sign the JWT";
                throw new TokenFilterException(HttpServletResponse.SC_UNAUTHORIZED, IdentityCommonConstants
                        .OAUTH2_INVALID_CLIENT_MESSAGE, message);
            }
        }
    }
}
