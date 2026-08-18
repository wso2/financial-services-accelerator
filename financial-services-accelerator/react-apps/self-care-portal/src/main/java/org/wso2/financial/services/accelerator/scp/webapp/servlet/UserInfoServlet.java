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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.util.JWTUtils;
import org.wso2.financial.services.accelerator.scp.webapp.model.SelfCarePortalError;
import org.wso2.financial.services.accelerator.scp.webapp.service.ResourceInterceptorService;
import org.wso2.financial.services.accelerator.scp.webapp.util.Utils;

import java.text.ParseException;
import java.util.Optional;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * UserInfoServlet.
 * <p>
 * Decodes the id_token (kept server-side only, in httpOnly cookies) and returns just the
 * email/role claims the frontend needs for display, so the frontend never has to read or
 * decode the raw id_token itself.
 */
@WebServlet(name = "UserInfoServlet", urlPatterns = "/scp/userinfo")
public class UserInfoServlet extends HttpServlet {

    private static final long serialVersionUID = 1938471023984710293L;
    private static final Log LOG = LogFactory.getLog(UserInfoServlet.class);
    private final ResourceInterceptorService resourceInterceptorService = new ResourceInterceptorService();
    private static final String EMAIL = "email";
    private static final String ROLE = "role";
    private static final String SUB_CLAIM = "sub";
    private static final String ROLE_CLAIM = "user_role";

    @Override
    @SuppressFBWarnings("SERVLET_HEADER")
    // Suppressed content - req.getHeader(HttpHeaders.AUTHORIZATION)
    // Suppression reason - False Positive: header is read-only; used only to check that the
    //                      caller holds a session, the value itself is never trusted or echoed.
    // Suppressed warning count - 1
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        if (req.getHeader(HttpHeaders.AUTHORIZATION) == null) {
            SelfCarePortalError error = new SelfCarePortalError("Authentication Error!",
                    "Please try signing in again.");
            Utils.returnResponse(resp, HttpStatus.SC_UNAUTHORIZED, new JSONObject(error));
            return;
        }

        Optional<String> optIdToken = resourceInterceptorService.constructIdTokenFromCookies(req);
        if (!optIdToken.isPresent()) {
            SelfCarePortalError error = new SelfCarePortalError("Authentication Error!",
                    "Please try signing in again.");
            Utils.returnResponse(resp, HttpStatus.SC_UNAUTHORIZED, new JSONObject(error));
            return;
        }

        try {
            String tokenBody = JWTUtils.decodeRequestJWT(optIdToken.get(), "body");
            JSONObject tokenBodyObj = new JSONObject(tokenBody);

            JSONObject userInfo = new JSONObject();
            userInfo.put(EMAIL, tokenBodyObj.optString(SUB_CLAIM, ""));
            userInfo.put(ROLE, tokenBodyObj.optString(ROLE_CLAIM, ""));
            Utils.returnResponse(resp, HttpStatus.SC_OK, userInfo);
        } catch (ParseException e) {
            LOG.error("Failed to parse id_token while resolving user info. Caused by, ", e);
            SelfCarePortalError error = new SelfCarePortalError("Authentication Error!",
                    "Please try signing in again.");
            Utils.returnResponse(resp, HttpStatus.SC_UNAUTHORIZED, new JSONObject(error));
        }
    }
}
