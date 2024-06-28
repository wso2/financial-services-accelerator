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

package com.wso2.openbanking.scp.webapp.servlet;

import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.scp.webapp.exception.TokenGenerationException;
import com.wso2.openbanking.scp.webapp.model.SCPError;
import com.wso2.openbanking.scp.webapp.service.OAuthService;
import com.wso2.openbanking.scp.webapp.util.Constants;
import com.wso2.openbanking.scp.webapp.util.Utils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The OAuthCallbackServlet is responsible for handling oauth2 authorization callback requests.
 */
@WebServlet(name = "OAuthCallbackServlet", urlPatterns = "/scp_oauth2_callback")
public class OAuthCallbackServlet extends HttpServlet {

    private static final long serialVersionUID = -1253188744670051774L;
    private static final Log LOG = LogFactory.getLog(OAuthCallbackServlet.class);
    private static final String CODE = "code";

    @Generated(message = "Ignoring since all cases are covered from other unit tests")
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        final String iamBaseUrl = Utils.getParameter(Constants.IS_BASE_URL);
        try {
            final String code = req.getParameter(CODE);

            String redirectUrl = iamBaseUrl + "/consentmgr";

            OAuthService oAuthService = OAuthService.getInstance();
            if (StringUtils.isEmpty(code)) {
                LOG.debug("Logout callback request received. Invalidating cookies.");
                oAuthService.removeAllCookiesFromRequest(req, resp);
                redirectUrl  += "/logout";
            } else {
                LOG.debug("Authorization callback request received");
                final String clientKey = Utils.getParameter(Constants.CONFIGURED_CLIENT_ID);
                final String clientSecret = Utils.getParameter(Constants.CONFIGURED_CLIENT_SECRET);

                JSONObject tokenResponse = oAuthService
                        .sendAccessTokenRequest(iamBaseUrl, clientKey, clientSecret, code);
                // add cookies to response
                oAuthService.generateCookiesFromTokens(tokenResponse, req, resp);
            }
            LOG.debug("Redirecting to frontend application: " + redirectUrl);
            resp.sendRedirect(redirectUrl);
        } catch (TokenGenerationException | IOException e) {
            LOG.error("Exception occurred while processing authorization callback request. Caused by, ", e);
            // sending error to frontend
            SCPError error = new SCPError("Authentication Failed!",
                    "Something went wrong during the authentication process. Please try signing in again.");
            final String errorUrlFormat = iamBaseUrl + "/consentmgr/error?message=%s&description=%s";
            Utils.sendErrorToFrontend(error, errorUrlFormat, resp);
        }
    }
}
