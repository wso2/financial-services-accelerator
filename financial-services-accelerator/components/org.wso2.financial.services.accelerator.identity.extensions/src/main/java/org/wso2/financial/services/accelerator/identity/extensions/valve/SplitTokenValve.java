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

package org.wso2.financial.services.accelerator.identity.extensions.valve;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.tomcat.ext.valves.CarbonTomcatValve;
import org.wso2.carbon.tomcat.ext.valves.CompositeValve;
import org.wso2.financial.services.accelerator.common.util.Generated;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * Tomcat valve that reconstructs a Bearer token split across two cookies.
 *
 * Registered via TomcatValveContainer so it runs inside CompositeValve, before
 * AuthenticationValve validates the token. On each request to /consentmgr/scp/*
 * the valve reads P1 from the Authorization header and both parts from cookies,
 * performs a CSRF check (header P1 must equal cookie P1), joins them, and writes
 * the full token back into the Coyote request MimeHeaders before continuing.
 * Requests without split-token cookies pass through unchanged.
 */
public class SplitTokenValve extends CarbonTomcatValve {

    private static final Log LOG = LogFactory.getLog(SplitTokenValve.class);

    static final String SCP_PATH_PREFIX = "/consentmgr/scp/";
    static final String PART1_COOKIE_NAME = "OB_SCP_AT_P1";
    static final String PART2_COOKIE_NAME = "OB_SCP_AT_P2";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Generated(message = "invoke() wires Catalina internals; covered by reconstructToken() unit tests")
    @Override
    public void invoke(Request request, Response response, CompositeValve compositeValve) {

        String uri = request.getRequestURI();

        if (uri == null || !uri.contains(SCP_PATH_PREFIX)) {
            compositeValve.continueInvocation(request, response);
            return;
        }

        String fullToken = reconstructToken(request.getRequest());

        if (fullToken == null) {
            LOG.debug("SplitTokenValve: token parts not present or CSRF check failed for "
                    + uri.replaceAll("[\r\n]", "") + ". Passing through.");
            compositeValve.continueInvocation(request, response);
            return;
        }

        LOG.debug("SplitTokenValve: reconstructed full token for " + uri.replaceAll("[\r\n]", ""));
        request.getCoyoteRequest().getMimeHeaders()
                .setValue(AUTHORIZATION_HEADER).setString(BEARER_PREFIX + fullToken);
        compositeValve.continueInvocation(request, response);
    }

    /**
     * Reads P1 from the Authorization header and both parts from cookies.
     * Validates header P1 == cookie P1 as a CSRF guard before joining.
     *
     * @return full token string, or null if any piece is missing or CSRF check fails
     */
    @SuppressFBWarnings({"COOKIE_USAGE", "SERVLET_HEADER"})
    String reconstructToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }

        String headerP1 = authHeader.substring(BEARER_PREFIX.length());
        if (headerP1.isEmpty()) {
            return null;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        String cookieP1 = null;
        String cookieP2 = null;
        for (Cookie cookie : cookies) {
            if (PART1_COOKIE_NAME.equals(cookie.getName())) {
                cookieP1 = cookie.getValue();
            } else if (PART2_COOKIE_NAME.equals(cookie.getName())) {
                cookieP2 = cookie.getValue();
            }
        }

        if (cookieP1 == null || cookieP2 == null) {
            return null;
        }

        if (!cookieP1.equals(headerP1)) {
            LOG.warn("SplitTokenValve: Authorization header P1 does not match "
                    + PART1_COOKIE_NAME + " cookie. Possible token substitution attempt.");
            return null;
        }

        return headerP1 + cookieP2;
    }
}
