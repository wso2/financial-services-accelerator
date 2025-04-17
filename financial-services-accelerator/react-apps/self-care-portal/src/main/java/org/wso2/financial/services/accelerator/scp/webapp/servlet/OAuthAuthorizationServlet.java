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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.scp.webapp.model.SCPError;
import org.wso2.financial.services.accelerator.scp.webapp.service.OAuthService;
import org.wso2.financial.services.accelerator.scp.webapp.util.Constants;
import org.wso2.financial.services.accelerator.scp.webapp.util.Utils;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The OAuthAuthorizationServlet is responsible for handling oauth2 authorization flow and token flow for
 * Self Care Portal app.
 */
@WebServlet(name = "OAuthAuthorizationServlet", urlPatterns = "/scp_oauth2_authorize")
public class OAuthAuthorizationServlet extends HttpServlet {

    private static final long serialVersionUID = 6935866958152624870L;
    private static final Log LOG = LogFactory.getLog(OAuthAuthorizationServlet.class);

    @Generated(message = "Ignoring since all cases are covered from other unit tests")
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        LOG.debug("Authorization request received");

        try {
            final String iamBaseUrl = Utils.getParameter(Constants.IS_BASE_URL);
            final String clientKey = Utils.getParameter(Constants.CONFIGURED_CLIENT_ID);

            final String authUrl = OAuthService.getInstance().generateAuthorizationUrl(iamBaseUrl, clientKey);

            LOG.debug("Redirecting to: " + authUrl);
            resp.sendRedirect(authUrl);
        } catch (URISyntaxException | IOException e) {
            LOG.error("Exception occurred while redirecting to authorization url. caused by,", e);
            // sending error to frontend
            SCPError error = new SCPError("Authentication Failed!",
                    "Something went wrong during the authentication process. Please try signing in again.");
            final String iamBaseUrl = Utils.getParameter(Constants.IS_BASE_URL);
            final String errorUrlFormat = iamBaseUrl + "/consentmgr/error?message=%s&description=%s";
            Utils.sendErrorToFrontend(error, errorUrlFormat, resp);
        }
    }
}
