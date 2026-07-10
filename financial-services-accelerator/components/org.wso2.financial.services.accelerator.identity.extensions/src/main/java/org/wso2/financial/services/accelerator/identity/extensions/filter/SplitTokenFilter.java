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

/**
 * Generic Tomcat filter that reconstructs a Bearer token split across two cookies.
 *
 * Configured via [[tomcat.filter]] init_params in deployment.toml:
 *   tokenPart1CookieName — name of the cookie carrying part 1 (also sent in Authorization header)
 *   tokenPart2CookieName — name of the HttpOnly cookie carrying part 2
 *
 * On each request the filter checks for a Bearer Authorization header whose value matches the
 * part-1 cookie (CSRF guard). If both cookies are present and the check passes, the full token
 * (P1 + P2) is placed back in the Authorization header before the request continues down the
 * filter chain. If any piece is missing the request is passed through unchanged — downstream
 * authentication will reject it if a valid token was required.
 *
 * URL patterns to protect are declared via [[tomcat.filter_mapping]] in deployment.toml;
 * no path logic lives inside this filter.
 */
public class SplitTokenFilter implements Filter {

    private static final Log LOG = LogFactory.getLog(SplitTokenFilter.class);

    static final String INIT_PARAM_PART1_COOKIE = "tokenPart1CookieName";
    static final String INIT_PARAM_PART2_COOKIE = "tokenPart2CookieName";

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private String part1CookieName;
    private String part2CookieName;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        part1CookieName = filterConfig.getInitParameter(INIT_PARAM_PART1_COOKIE);
        part2CookieName = filterConfig.getInitParameter(INIT_PARAM_PART2_COOKIE);

        if (part1CookieName == null || part2CookieName == null) {
            throw new ServletException("SplitTokenFilter requires init-params '" + INIT_PARAM_PART1_COOKIE
                    + "' and '" + INIT_PARAM_PART2_COOKIE + "'");
        }
        LOG.info("SplitTokenFilter initialized. P1 cookie: " + part1CookieName.replaceAll("[\r\n]", "")
                + ", P2 cookie: " + part2CookieName.replaceAll("[\r\n]", ""));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String fullToken = reconstructToken(httpRequest);

        if (fullToken == null) {
            LOG.debug("SplitTokenFilter: token parts not present or CSRF check failed for "
                    + httpRequest.getRequestURI().replaceAll("[\r\n]", "") + ". Passing through.");
            chain.doFilter(request, response);
            return;
        }

        LOG.debug("SplitTokenFilter: reconstructed full token for "
                + httpRequest.getRequestURI().replaceAll("[\r\n]", ""));
        chain.doFilter(new TokenReplacedRequest(httpRequest, BEARER_PREFIX + fullToken), response);
    }

    @Override
    public void destroy() {
    }

    /**
     * Reads the part-1 value from the Authorization header and both parts from cookies.
     * Validates header P1 == cookie P1 as a CSRF guard before joining.
     *
     * @return full token string, or null if any piece is missing or the CSRF check fails
     */
    private String reconstructToken(HttpServletRequest request) {
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
            if (part1CookieName.equals(cookie.getName())) {
                cookieP1 = cookie.getValue();
            } else if (part2CookieName.equals(cookie.getName())) {
                cookieP2 = cookie.getValue();
            }
        }

        if (cookieP1 == null || cookieP2 == null) {
            return null;
        }

        if (!cookieP1.equals(headerP1)) {
            LOG.warn("SplitTokenFilter: Authorization header P1 does not match "
                    + part1CookieName.replaceAll("[\r\n]", "") + " cookie. Possible token substitution attempt.");
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
