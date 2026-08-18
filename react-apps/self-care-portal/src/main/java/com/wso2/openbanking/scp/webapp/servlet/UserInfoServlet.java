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
import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.scp.webapp.model.SCPError;
import com.wso2.openbanking.scp.webapp.service.APIMService;
import com.wso2.openbanking.scp.webapp.service.OAuthService;
import com.wso2.openbanking.scp.webapp.util.Constants;
import com.wso2.openbanking.scp.webapp.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Optional;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * UserInfoServlet
 * <p>
 * Decodes the id token server-side (from its HttpOnly cookie halves) and exposes only the claims the
 * frontend needs (email, role) as JSON. This keeps the id token itself out of reach of frontend JavaScript.
 */
@WebServlet(name = "UserInfoServlet", urlPatterns = {"/scp/userinfo"})
public class UserInfoServlet extends HttpServlet {

    private static final long serialVersionUID = 3902649563217544213L;
    private static final Log LOG = LogFactory.getLog(UserInfoServlet.class);
    private final APIMService apimService = new APIMService();

    @Generated(message = "Ignoring since all cases are covered from other unit tests")
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        Optional<String> optIdToken = apimService.constructIdTokenFromCookies(req);

        if (!optIdToken.isPresent()) {
            LOG.debug("Id token cookies are missing from the request. Returning unauthenticated response.");
            Utils.returnResponse(resp, HttpStatus.SC_UNAUTHORIZED, buildUnauthenticatedResponse());
            return;
        }

        try {
            net.minidev.json.JSONObject idTokenBody = JWTUtils.decodeRequestJWT(optIdToken.get(), "body");

            JSONObject userInfo = new JSONObject();
            userInfo.put("isLogged", true);
            userInfo.put("email", idTokenBody.getAsString(Constants.CLAIM_SUB));
            userInfo.put("role", idTokenBody.getAsString(Constants.CLAIM_USER_ROLE));

            Utils.returnResponse(resp, HttpStatus.SC_OK, userInfo);
        } catch (ParseException e) {
            LOG.error("Exception occurred while decoding id token. Caused by, ", e);
            OAuthService.getInstance().removeAllCookiesFromRequest(req, resp);
            Utils.returnResponse(resp, HttpStatus.SC_UNAUTHORIZED, buildUnauthenticatedResponse());
        }
    }

    private JSONObject buildUnauthenticatedResponse() {
        SCPError error = new SCPError("Authentication Error!",
                "User session is invalid or has expired. Please sign in again.");
        return new JSONObject(error);
    }
}
