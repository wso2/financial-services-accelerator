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

package com.wso2.openbanking.accelerator.identity.clientauth;

import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.oauth2.bean.OAuthClientAuthnContext;
import org.wso2.carbon.identity.oauth2.client.authentication.OAuthClientAuthnException;
import org.wso2.carbon.identity.oauth2.token.handler.clientauth.mutualtls.MutualTLSClientAuthenticator;
import org.wso2.carbon.identity.oauth2.token.handler.clientauth.mutualtls.utils.MutualTLSUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * OpenBanking Mutual TLS Client Authenticator.
 */
public class OBMutualTLSClientAuthenticator extends MutualTLSClientAuthenticator {

    private static Log log = LogFactory.getLog(OBMutualTLSClientAuthenticator.class);

    @Override
    public boolean canAuthenticate(HttpServletRequest request, Map<String, List> bodyParams,
                                   OAuthClientAuthnContext oAuthClientAuthnContext) {

        // Look for client assertion in request parameters.
        String clientAssertion = request.getParameter(IdentityCommonConstants.OAUTH_JWT_ASSERTION);
        if (StringUtils.isNotEmpty(clientAssertion)) {
            log.debug("Request cannot be handled by OBMutualTLSClientAuthenticator");
            return false;
        }

        // Look for client assertion in bodyParams map.
        if (bodyParams != null) {
            List<?> clientAssertionList = bodyParams.get(IdentityCommonConstants.OAUTH_JWT_ASSERTION);
            if (clientAssertionList != null && !clientAssertionList.isEmpty() && clientAssertionList.get(0) != null) {
                String bodyParamsClientAssertion = clientAssertionList.get(0).toString();
                if (StringUtils.isNotEmpty(bodyParamsClientAssertion)) {
                    log.debug("Client assertion found in body parameters. Request cannot be handled by " +
                            "OBMutualTLSClientAuthenticator.");
                    return false;
                }
            }
        }

        return super.canAuthenticate(request, bodyParams, oAuthClientAuthnContext);
    }

    @Override
    public URL getJWKSEndpointOfSP(ServiceProvider serviceProvider, String clientID) throws OAuthClientAuthnException {

        String jwksUri = MutualTLSUtil.getPropertyValue(serviceProvider, IdentityCommonUtil.getJWKURITransportCert());
        if (StringUtils.isEmpty(jwksUri)) {
            throw new OAuthClientAuthnException("jwks endpoint not configured for the service provider for client ID: "
                    + clientID, "server_error");
        } else {
            try {
                URL url = new URL(jwksUri);
                if (log.isDebugEnabled()) {
                    log.debug("Configured JWKS URI found: " + jwksUri);
                }

                return url;
            } catch (MalformedURLException var6) {
                throw new OAuthClientAuthnException("URL might be malformed " + clientID, "server_error", var6);
            }
        }
    }
}
