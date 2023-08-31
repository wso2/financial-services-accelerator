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

package com.wso2.openbanking.accelerator.runtime.identity.authn.filter;

import com.wso2.openbanking.accelerator.identity.common.IdentityServiceExporter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.cxf.message.Message;
import org.wso2.carbon.identity.oauth.client.authn.filter.OAuthClientAuthenticatorProxy;
import org.wso2.carbon.identity.oauth.common.OAuth2ErrorCodes;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth2.bean.OAuthClientAuthnContext;
import org.wso2.carbon.identity.oauth2.client.authentication.OAuthClientAuthnService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * JAX-RS interceptor which intercepts requests. This interceptor will act as a proxy for OAuth2 Client Authenticators.
 * This will pick correct authenticator which can handle OAuth client authentication and engage it.
 */
public class OBOAuthClientAuthenticatorProxy extends OAuthClientAuthenticatorProxy {

    private static final Log log = LogFactory.getLog(OBOAuthClientAuthenticatorProxy.class);
    private static final String HTTP_REQUEST = "HTTP.REQUEST";
    private OAuthClientAuthnService oAuthClientAuthnService;

    /**
     * Handles the incoming JAX-RS message for the purpose of OAuth2 client authentication.
     *
     * @param message JAX-RS message
     */
    @Override
    public void handleMessage(Message message) {

        Map<String, List> bodyContentParams = getContentParams(message);
        HttpServletRequest request = ((HttpServletRequest) message.get(HTTP_REQUEST));
        if (oAuthClientAuthnService == null) {
            oAuthClientAuthnService = IdentityServiceExporter.getOAuthClientAuthnService();
        }
        OAuthClientAuthnContext oAuthClientAuthnContext = oAuthClientAuthnService.authenticateClient(request,
                bodyContentParams);
        if (!oAuthClientAuthnContext.isPreviousAuthenticatorEngaged()) {
            oAuthClientAuthnContext.setErrorCode(OAuth2ErrorCodes.INVALID_CLIENT);
            oAuthClientAuthnContext.setErrorMessage("Unsupported client authentication mechanism");
        }
        setContextToRequest(request, oAuthClientAuthnContext);
    }

    /**
     * Retrieve body content as a String, List map.
     *
     * @param message JAX-RS incoming message
     * @return Body parameter of the incoming request message
     */
    protected Map<String, List> getContentParams(Message message) {

        Map<String, List> contentMap = new HashMap<>();
        List contentList = message.getContent(List.class);
        contentList.forEach(item -> {
            if (item instanceof MetadataMap) {
                MetadataMap metadataMap = (MetadataMap) item;
                metadataMap.forEach((key, value) -> {
                    if (key instanceof String && value instanceof List) {
                        contentMap.put((String) key, (List) value);
                    }
                });
            }
        });
        return contentMap;
    }

    /**
     * Set client authentication context to the request.
     *
     * @param request                 - Request
     * @param oAuthClientAuthnContext - Context
     */
    private void setContextToRequest(HttpServletRequest request, OAuthClientAuthnContext oAuthClientAuthnContext) {

        log.debug("Setting OAuth client authentication context to request");
        request.setAttribute(OAuthConstants.CLIENT_AUTHN_CONTEXT,
                oAuthClientAuthnContext);
    }

}
