/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.scp.webapp.service.OAuthService;
import org.wso2.financial.services.accelerator.scp.webapp.service.ResourceInterceptorService;
import org.wso2.financial.services.accelerator.scp.webapp.util.Constants;
import org.wso2.financial.services.accelerator.scp.webapp.util.Utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * OAuthLogoutServlet.
 * <p>
 * Builds the IS logout URL (with id_token_hint) server-side, since the id_token is only ever
 * kept in httpOnly cookies and is never readable by frontend JS. The frontend simply navigates
 * here to start logout; IS redirects back to OAuthCallbackServlet, which clears the cookies.
 */
@WebServlet(name = "OAuthLogoutServlet", urlPatterns = "/scp_oauth2_logout")
public class OAuthLogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 4837201938471029384L;
    private static final Log LOG = LogFactory.getLog(OAuthLogoutServlet.class);
    private final ResourceInterceptorService resourceInterceptorService = new ResourceInterceptorService();

    @Generated(message = "Ignoring since all cases are covered from other unit tests")
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        final String iamBaseUrl = Utils.getParameter(Constants.IS_BASE_URL)
                .replaceAll(FinancialServicesConstants.SANITIZING_CHARACTERS, "");
        try {
            Optional<String> optIdToken = resourceInterceptorService.constructIdTokenFromCookies(req);
            String logoutUrl = OAuthService.getInstance()
                    .generateLogoutUrl(iamBaseUrl, optIdToken.orElse(null));
            resp.sendRedirect(logoutUrl);
        } catch (URISyntaxException | IOException e) {
            LOG.error("Exception occurred while building the logout redirect. Caused by, ", e);
        }
    }
}
