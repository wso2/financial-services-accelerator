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
import com.wso2.openbanking.scp.webapp.model.SCPError;
import com.wso2.openbanking.scp.webapp.service.APIMService;
import com.wso2.openbanking.scp.webapp.service.OAuthService;
import com.wso2.openbanking.scp.webapp.util.Constants;
import com.wso2.openbanking.scp.webapp.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * OAuthLogoutServlet
 * <p>
 * Responsible for handling oauth2 logout flow for Self Care Portal app.
 */
@WebServlet(name = "OAuthLogoutServlet", urlPatterns = "/scp_oauth2_logout")
public class OAuthLogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 7935866958152624870L;
    private static final Log LOG = LogFactory.getLog(OAuthLogoutServlet.class);
    private final APIMService apimService = new APIMService();

    @Generated(message = "Ignoring since all cases are covered from other unit tests")
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        LOG.debug("Logout request received");

        try {
            Optional<String> optIdToken = apimService.constructIdTokenFromCookies(req);
            final String iamBaseUrl = Utils.getParameter(Constants.IS_BASE_URL);
            final String logoutUrl = OAuthService.getInstance().generateLogoutUrl(iamBaseUrl, optIdToken.get());
            OAuthService.getInstance().removeAllCookiesFromRequest(req, resp);
            LOG.debug("Redirecting to: " + logoutUrl);
            resp.sendRedirect(logoutUrl);
        } catch (URISyntaxException | IOException e) {
            LOG.error("Exception occurred while redirecting to logout url. caused by,", e);
            // sending error to frontend
            SCPError error = new SCPError("Authentication Failed!",
                    "Something went wrong during the authentication process. Please try signing in again.");
            final String iamBaseUrl = Utils.getParameter(Constants.IS_BASE_URL);
            final String errorUrlFormat = iamBaseUrl + "/consentmgr/error?message=%s&description=%s";
            Utils.sendErrorToFrontend(error, errorUrlFormat, resp);
        }
    }
}
