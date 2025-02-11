/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.identity.extensions.dcr.validators;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.oauth.dcr.bean.ApplicationRegistrationRequest;
import org.wso2.carbon.identity.oauth.dcr.bean.ApplicationUpdateRequest;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;

import java.util.Map;

/**
 * Validator class for validating he token endpoint authentication signing algorithm is available and not empty when
 * token endpoint authentication method is private ket jwt.
 */
public class TokenEndpointAuthSigningAlgValidator implements DynamicClientRegistrationValidator {

    private static final Log log = LogFactory.getLog(TokenEndpointAuthSigningAlgValidator.class);
    private static final String AUTH_METHOD_PRIVATE_KEY_JWT = "private_key_jwt";

    @Override
    public void validatePost(ApplicationRegistrationRequest applicationRegistrationRequest,
                             Map<String, Object> ssaParams) throws FinancialServicesException {

        String tokenEndpointAuthMethod = applicationRegistrationRequest.getTokenEndpointAuthMethod();
        String tokenEndpointAuthSigningAlg = applicationRegistrationRequest.getTokenEndpointAuthSignatureAlgorithm();
        validateTokenEPAuthSigningAlg(tokenEndpointAuthMethod, tokenEndpointAuthSigningAlg);
    }

    @Override
    public void validateGet(Map<String, String> ssaParams) throws FinancialServicesException {

    }

    @Override
    public void validateUpdate(ApplicationUpdateRequest applicationUpdateRequest, Map<String, Object> ssaParams,
                               ServiceProviderProperty[] serviceProviderProperties) throws FinancialServicesException {


        String tokenEndpointAuthMethod = applicationUpdateRequest.getTokenEndpointAuthMethod();
        String tokenEndpointAuthSigningAlg = applicationUpdateRequest.getTokenEndpointAuthSignatureAlgorithm();
        validateTokenEPAuthSigningAlg(tokenEndpointAuthMethod, tokenEndpointAuthSigningAlg);
    }

    /**
     * Validate whether the token endpoint authentication signing algorithm is available and not empty when
     * token endpoint authentication method is private ket jwt.
     *
     * @param tokenEndpointAuthMethod        Token endpoint authentication method
     * @param tokenEndpointAuthSigningAlg    Token endpoint authentication signing algorithm
     * @throws FinancialServicesException When the issuer is not equal to the software id
     */
    private static void validateTokenEPAuthSigningAlg(String tokenEndpointAuthMethod,
                                          String tokenEndpointAuthSigningAlg) throws FinancialServicesException {

        // token_endpoint_auth_signing_alg must be specified if token_endpoint_auth_method is private_key_jwt.
        if (StringUtils.isNotEmpty(tokenEndpointAuthMethod) &&
                StringUtils.equals(tokenEndpointAuthMethod, AUTH_METHOD_PRIVATE_KEY_JWT) &&
                StringUtils.isBlank(tokenEndpointAuthSigningAlg)) {
            log.error("Token endpoint auth signing alg must be specified if token_endpoint_auth_method is " +
                    "private_key_jwt.");
            throw new FinancialServicesException("Token endpoint auth signing alg must be specified if " +
                    "token_endpoint_auth_method is private_key_jwt.");
        }
    }

}
