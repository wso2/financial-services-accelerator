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

package org.wso2.financial.services.accelerator.identity.extensions.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

/**
 * Tomcat-level filter registered via [[tomcat.filter]] in deployment.toml.
 *
 * The SCP splits the access token across two cookies (OB_SCP_AT_P1, OB_SCP_AT_P2) and
 * sends only P1 in the Authorization header. This filter reassembles the full token and
 * rewrites the Authorization header on the request before IS's AuthenticationValve
 * validates it, allowing [[resource.access_control]] secure="true" to work correctly
 * for /consentmgr/scp/* paths.
 *
 * Requests to paths outside /consentmgr/scp/ are passed through unchanged.
 */
public class SelfCarePortalTokenFilter implements Filter {

    private static final Log LOG = LogFactory.getLog(SelfCarePortalTokenFilter.class);

    private static final String SCP_API_PATH = "/consentmgr/scp/";
    private static final String AT_P1_COOKIE = "OB_SCP_AT_P1";
    private static final String AT_P2_COOKIE = "OB_SCP_AT_P2";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOG.info("SelfCarePortalTokenFilter initialized.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();

        if (requestURI == null || !requestURI.contains(SCP_API_PATH)) {
            chain.doFilter(request, response);
            return;
        }

        String safeURI = requestURI.replaceAll("[\r\n]", "");
        String fullToken = reconstructAccessToken(httpRequest);

        if (fullToken == null) {
            // token reconstruction failed — pass through; IS AuthenticationValve will reject
            LOG.debug("SelfCarePortalTokenFilter: could not reconstruct access token for " + safeURI +
                    ". Passing through for IS to handle.");
            chain.doFilter(request, response);
            return;
        }

        LOG.debug("SelfCarePortalTokenFilter: reconstructed full access token for " + safeURI);
        chain.doFilter(new TokenReplacedRequest(httpRequest, BEARER_PREFIX + fullToken), response);
    }

    @Override
    public void destroy() {
    }

    /**
     * Reads OB_SCP_AT_P1 from the Authorization header and OB_SCP_AT_P2 from cookies.
     * Validates that the P1 in the header matches the P1 cookie (CSRF guard) before joining.
     *
     * @return full access token string, or null if reconstruction is not possible
     */
    private String reconstructAccessToken(HttpServletRequest request) {
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
            if (AT_P1_COOKIE.equals(cookie.getName())) {
                cookieP1 = cookie.getValue();
            } else if (AT_P2_COOKIE.equals(cookie.getName())) {
                cookieP2 = cookie.getValue();
            }
        }

        if (cookieP1 == null || cookieP2 == null) {
            return null;
        }

        // Validate header P1 matches cookie P1 to prevent token substitution attacks
        if (!cookieP1.equals(headerP1)) {
            LOG.warn("SelfCarePortalTokenFilter: Authorization header P1 does not match OB_SCP_AT_P1 cookie. " +
                    "Possible token substitution attempt.");
            return null;
        }

        return headerP1 + cookieP2;
    }

    /**
     * Request wrapper that overrides the Authorization header with the reconstructed full token.
     */
    private static class TokenReplacedRequest extends HttpServletRequestWrapper {

        private final String replacedAuthorizationValue;

        TokenReplacedRequest(HttpServletRequest request, String authorizationValue) {
            super(request);
            this.replacedAuthorizationValue = authorizationValue;
        }

        @Override
        public String getHeader(String name) {
            if (AUTHORIZATION_HEADER.equalsIgnoreCase(name)) {
                return replacedAuthorizationValue;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (AUTHORIZATION_HEADER.equalsIgnoreCase(name)) {
                return Collections.enumeration(Collections.singletonList(replacedAuthorizationValue));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Set<String> names = new HashSet<>(Collections.list(super.getHeaderNames()));
            names.add(AUTHORIZATION_HEADER);
            return Collections.enumeration(names);
        }
    }
}
