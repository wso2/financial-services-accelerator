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

package com.wso2.openbanking.accelerator.identity.auth.extensions.authz.request;

import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.utils.OAuthUtils;
import org.apache.oltu.oauth2.common.validators.OAuthValidator;
import org.json.JSONObject;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import static com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants.OAUTH2_INVALID_REQUEST_MESSAGE;
import static com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants.REQUEST;
import static com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants.REQUEST_URI;

/**
 * OB OAuth 2 authorization request for request_uri support.
 */
public class OBOAuthAuthzRequest extends OAuthAuthzRequest {

    private static final Log log = LogFactory.getLog(OBOAuthAuthzRequest.class);

    public OBOAuthAuthzRequest(HttpServletRequest request) throws OAuthSystemException, OAuthProblemException {

        super(request);
    }

    @Override
    protected OAuthValidator<HttpServletRequest> initValidator() throws OAuthProblemException, OAuthSystemException {

        String responseTypeValue = getParam(OAuth.OAUTH_RESPONSE_TYPE);

        // Check if request object reference is present.
        if (OAuthUtils.isEmpty(responseTypeValue) && request.getParameterMap().containsKey(REQUEST_URI)) {
            responseTypeValue = IdentityCommonUtil.decodeRequestObjectAndGetKey(request, OAuth.OAUTH_RESPONSE_TYPE);
        }
        if (OAuthUtils.isEmpty(responseTypeValue)) {
            throw IdentityCommonUtil.handleOAuthProblemException(OAUTH2_INVALID_REQUEST_MESSAGE,
                    "Missing response_type parameter value", getState());
        }

        Class<? extends OAuthValidator<HttpServletRequest>> oauthValidatorClass = OAuthServerConfiguration.getInstance()
                .getSupportedResponseTypeValidators().get(responseTypeValue);

        if (oauthValidatorClass == null) {
            if (log.isDebugEnabled()) {

                //Do not change this log format as these logs use by external applications
                log.debug("Unsupported Response Type : " + responseTypeValue +
                        " for client id : " + getClientId());
            }
            throw IdentityCommonUtil.handleOAuthProblemException(OAUTH2_INVALID_REQUEST_MESSAGE,
                    "Invalid response_type parameter value", getState());
        }

        return OAuthUtils.instantiateClass(oauthValidatorClass);
    }

    @Override
    public Set<String> getScopes() {

        if (request.getParameterMap().containsKey(REQUEST_URI) && request.getParameter(REQUEST_URI) != null) {
            try {
                return OAuthUtils.decodeScopes(IdentityCommonUtil
                        .decodeRequestObjectAndGetKey(request, OAuth.OAUTH_SCOPE));
            } catch (OAuthProblemException e) {
                log.error("Invalid request URI", e);
                return null;
            }
        } else {
            return super.getScopes();
        }
    }

    @Override
    public String getResponseType() {

        if (request.getParameterMap().containsKey(REQUEST_URI) && request.getParameter(REQUEST_URI) != null) {
            try {
                return IdentityCommonUtil.decodeRequestObjectAndGetKey(request, OAuth.OAUTH_RESPONSE_TYPE);
            } catch (OAuthProblemException e) {
                log.error("Invalid request URI", e);
                return null;
            }
        } else {
            return super.getResponseType();
        }

    }

    @Override
    public String getState() {

        if (request.getParameterMap().containsKey(REQUEST_URI) && request.getParameter(REQUEST_URI) != null) {
            try {
                return IdentityCommonUtil.decodeRequestObjectAndGetKey(request, OAuth.OAUTH_STATE);
            } catch (OAuthProblemException e) {
                log.error("Invalid request URI", e);
                return null;
            }
        } else {

            //retrieve state value if present inside request body.
            if (StringUtils.isNotBlank(getParam(REQUEST))) {
                byte[] requestObject;
                try {
                    requestObject = Base64.getDecoder().decode(getParam(REQUEST).split("\\.")[1]);
                } catch (IllegalArgumentException e) {

                    // Decode if the requestObject is base64-url encoded.
                    requestObject = Base64.getUrlDecoder().decode(getParam(REQUEST).split("\\.")[1]);
                }
                JSONObject requestObjectJson = new JSONObject(new String(requestObject, StandardCharsets.UTF_8));
                return requestObjectJson.has(OAuth.OAUTH_STATE) ? requestObjectJson.getString(OAuth.OAUTH_STATE) : null;
            }
            return null;
        }
    }

}
