/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.scp.webapp.servlet;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.scp.webapp.exception.TokenGenerationException;
import org.wso2.financial.services.accelerator.scp.webapp.model.SelfCarePortalError;
import org.wso2.financial.services.accelerator.scp.webapp.service.OAuthService;
import org.wso2.financial.services.accelerator.scp.webapp.util.Constants;
import org.wso2.financial.services.accelerator.scp.webapp.util.Utils;

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
    private static final String ERROR = "error";
    private static final String ERROR_DESCRIPTION = "error_description";
    private static final String LOGOUT_DENY_ERROR_DESCRIPTION = "End User denied the logout request";

    @Generated(message = "Ignoring since all cases are covered from other unit tests")
    @Override
    @SuppressFBWarnings("SERVLET_PARAMETER")
    // Suppressed content - req.getParameter(CODE)
    // Suppression reason - False Positive : These endpoints are secured with access control
    // as defined in the IS deployment.toml file
    // Suppressed warning count - 1
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        final String iamBaseUrl = Utils.getParameter(Constants.IS_BASE_URL).replaceAll("\n\r", "");
        try {
            final String code = req.getParameter(CODE).replaceAll("\n\r", "");
            if (StringUtils.isNotEmpty(code)) {
                LOG.debug(String.format("Authorization callback request received with code: %s", code));
            }

            String redirectUrl = iamBaseUrl + "/consentmgr";

            OAuthService oAuthService = OAuthService.getInstance();
            if (StringUtils.isEmpty(code)) {
                LOG.debug("Logout callback request received. Invalidating cookies.");
                oAuthService.removeAllCookiesFromRequest(req, resp);
            } else {
                LOG.debug("Authorization callback request received");
                final String clientKey = Utils.getParameter(Constants.CONFIGURED_CLIENT_ID);
                final String clientSecret = Utils.getParameter(Constants.CONFIGURED_CLIENT_SECRET);

                JSONObject tokenResponse = oAuthService
                        .sendAccessTokenRequest(iamBaseUrl, clientKey, clientSecret, code);
                // add cookies to response
                oAuthService.generateCookiesFromTokens(tokenResponse, req, resp);
            }
            if ("access_denied".equals(req.getParameter(ERROR).replaceAll("\n\r", ""))
                    && !LOGOUT_DENY_ERROR_DESCRIPTION.equals
                    (req.getParameter(ERROR_DESCRIPTION).replaceAll("\n\r", ""))) {
                LOG.debug(String.format("User denied the consent. Error: %s Error Description: %s",
                        req.getParameter(ERROR).replaceAll("\n\r", ""),
                        req.getParameter(ERROR_DESCRIPTION).replaceAll("\n\r", "")));
                SelfCarePortalError error = new SelfCarePortalError(
                        req.getParameter(ERROR).replaceAll("\n\r", ""),
                        req.getParameter(ERROR_DESCRIPTION).replaceAll("\n\r", ""));
                final String errorUrlFormat = iamBaseUrl + "/consentmgr/error?message=%s&description=%s";
                Utils.sendErrorToFrontend(error, errorUrlFormat, resp);
                return;
            }
            LOG.debug(String.format("Redirecting to frontend application: %s",
                    redirectUrl.replaceAll("\n\r", "")));
            resp.sendRedirect(redirectUrl);
        } catch (TokenGenerationException | IOException e) {
            LOG.error("Exception occurred while processing authorization callback request. Caused by, ", e);
            // sending error to frontend
            SelfCarePortalError error = new SelfCarePortalError("Authentication Failed!",
                    "Something went wrong during the authentication process. Please try signing in again.");
            final String errorUrlFormat = iamBaseUrl + "/consentmgr/error?message=%s&description=%s";
            Utils.sendErrorToFrontend(error, errorUrlFormat, resp);
        }
    }
}
