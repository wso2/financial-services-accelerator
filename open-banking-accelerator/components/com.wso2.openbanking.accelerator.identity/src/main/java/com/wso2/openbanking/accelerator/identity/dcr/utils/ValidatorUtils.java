/**
 * Copyright (c) 2023-2024, WSO2 LLC. (https://www.wso2.com).
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
package com.wso2.openbanking.accelerator.identity.dcr.utils;

import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.common.validator.OpenBankingValidator;
import com.wso2.openbanking.accelerator.identity.common.annotations.validationorder.ValidationOrder;
import com.wso2.openbanking.accelerator.identity.dcr.exception.DCRValidationException;
import com.wso2.openbanking.accelerator.identity.dcr.model.RegistrationRequest;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.OAuth2Service;
import org.wso2.carbon.identity.oauth2.bean.OAuthClientAuthnContext;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.model.HttpRequestHeader;

/**
 * Util class for validation logic implementation.
 */
public class ValidatorUtils {

    private static final Log log = LogFactory.getLog(ValidatorUtils.class);

    public static void getValidationViolations(RegistrationRequest registrationRequest)
            throws DCRValidationException {

        String error = OpenBankingValidator.getInstance().getFirstViolation(registrationRequest, ValidationOrder.class);
        if (error != null) {
            String[] errors = error.split(":");
            throw new DCRValidationException(errors[1], errors[0]);
        }

    }

    /**
     * Create client credentials grant access token with PK JWT for DCR.
     * @param clientId client ID
     * @param tlsCert  transport certificate
     * @return String  access token
     */
    @Generated(message = "Excluding from code coverage since it requires a service call")
    public static String generateAccessToken(String clientId, String tlsCert) {
        OAuth2AccessTokenReqDTO tokenReqDTO = new OAuth2AccessTokenReqDTO();
        OAuthClientAuthnContext oauthClientAuthnContext = new OAuthClientAuthnContext();
        oauthClientAuthnContext.setClientId(clientId);
        oauthClientAuthnContext.addAuthenticator(IdentityCommonConstants.PRIVATE_KEY);
        oauthClientAuthnContext.setAuthenticated(true);

        tokenReqDTO.setoAuthClientAuthnContext(oauthClientAuthnContext);
        tokenReqDTO.setGrantType(IdentityCommonConstants.CLIENT_CREDENTIALS);
        tokenReqDTO.setClientId(clientId);

        String[] scopes = new String[2];

        //add the appropriate scopes
        scopes[0] = IdentityCommonUtil.getDCRScope();
        scopes[1] = IdentityCommonConstants.OPENID_SCOPE;
        tokenReqDTO.setScope(scopes);
        tokenReqDTO.setTenantDomain(IdentityCommonConstants.CARBON_SUPER);

        // set the tls cert as a header to bind the cnf value to the token
        HttpRequestHeader[] requestHeaders = new HttpRequestHeader[1];
        requestHeaders[0] = new HttpRequestHeader(IdentityCommonUtil.getMTLSAuthHeader(), tlsCert);
        tokenReqDTO.setHttpRequestHeaders(requestHeaders);

        tokenReqDTO.addAuthenticationMethodReference(IdentityCommonConstants.CLIENT_CREDENTIALS);

        OAuth2Service oAuth2Service = new OAuth2Service();
        OAuth2AccessTokenRespDTO tokenRespDTO = oAuth2Service.issueAccessToken(tokenReqDTO);

        return tokenRespDTO.getAccessToken();
    }

    /**
     * Get Registration Client URI.
     * @return String Registration client URI
     */
    public static String getRegistrationClientURI() {
        return String.valueOf(IdentityExtensionsDataHolder.getInstance()
                .getConfigurationMap().getOrDefault(IdentityCommonConstants.DCR_REGISTRATION_CLIENT_URI,
                        IdentityCommonConstants.DEFAULT_REGISTRATION_CLIENT_URI));
    }
}
