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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.json.JSONObject;
import org.wso2.carbon.databridge.commons.exception.SessionTimeoutException;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.scp.webapp.exception.TokenGenerationException;
import org.wso2.financial.services.accelerator.scp.webapp.model.SelfCarePortalError;
import org.wso2.financial.services.accelerator.scp.webapp.service.OAuthService;
import org.wso2.financial.services.accelerator.scp.webapp.service.ResourceInterceptorService;
import org.wso2.financial.services.accelerator.scp.webapp.util.Constants;
import org.wso2.financial.services.accelerator.scp.webapp.util.Utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ResourceInterceptorServlet
 * <p>
 * This interrupts the requests, adds auth header, and forward requests to Identity Server,
 * Also this class modifies the response from IS by adding application details.
 */
@WebServlet(name = "ResourceInterceptorServlet", urlPatterns = {"/scp/admin/*"})
public class ResourceInterceptorServlet extends HttpServlet {

    private static final long serialVersionUID = 7385252581004845440L;
    private static final Log LOG = LogFactory.getLog(ResourceInterceptorServlet.class);
    private final ResourceInterceptorService resourceInterceptorService = new ResourceInterceptorService();

    @Generated(message = "Ignoring since all cases are covered from other unit tests")
    @Override
    @SuppressFBWarnings("SERVLET_QUERY_STRING")
    // Suppressed content - req.getQueryString(), req.getRequestURI()
    // Suppression reason - False Positive :These parameters are read only
    // Suppressed warning count - 2
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            LOG.debug(String.format("New request received: %s ? %s", req.getRequestURI().replaceAll("\n\r", ""),
                    req.getQueryString().replaceAll("\n\r", "")));
            if (resourceInterceptorService.isAccessTokenExpired(req)) {
                // access token is expired, refreshing access token
                Optional<String> optRefreshToken = resourceInterceptorService.constructRefreshTokenFromCookies(req);

                if (optRefreshToken.isPresent()) {
                    final OAuthService oAuthService = OAuthService.getInstance();
                    final String iamBaseUrl = Utils.getParameter(Constants.IS_BASE_URL);
                    final String clientKey = Utils.getParameter(Constants.CONFIGURED_CLIENT_ID);
                    final String clientSecret = Utils.getParameter(Constants.CONFIGURED_CLIENT_SECRET);

                    JSONObject tokenResponse = oAuthService.sendRefreshTokenRequest(iamBaseUrl, clientKey,
                            clientSecret, optRefreshToken.get());

                    // add new tokes as cookies to response
                    oAuthService.generateCookiesFromTokens(tokenResponse, req, resp);

                    final String isBaseUrl = Utils.getParameter(Constants.IS_BASE_URL);
                    HttpUriRequest request = Utils
                            .getHttpUriRequest(isBaseUrl, req.getMethod(), req.getRequestURI(), req.getQueryString());

                    // generating header
                    Map<String, String> headers = new HashMap<>();
                    headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + tokenResponse.getString(Constants.ACCESS_TOKEN));
                    headers.put(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());

                    resourceInterceptorService.forwardRequest(resp, request, headers);

                } else {
                    // Invalid request, refresh token missing
                    SelfCarePortalError error = new SelfCarePortalError("Authentication Error!",
                            "Some values are missing from the request. Please try signing in again.");
                    LOG.error("Refresh token is missing from the request. Returning error to frontend, " + error);
                    OAuthService.getInstance().removeAllCookiesFromRequest(req, resp);
                    Utils.returnResponse(resp, HttpStatus.SC_BAD_REQUEST, new JSONObject(error));
                }
            } else {
                // access token is not expired yet
                Optional<String> optAccessToken = resourceInterceptorService.constructAccessTokenFromCookies(req);

                if (optAccessToken.isPresent()) {
                    final String isBaseUrl = Utils.getParameter(Constants.IS_BASE_URL);
                    HttpUriRequest request = Utils
                            .getHttpUriRequest(isBaseUrl, req.getMethod(), req.getRequestURI(), req.getQueryString());

                    // add existing req headers to new request
                    Map<String, String> headers = Collections.list(req.getHeaderNames())
                            .stream()
                            .filter(h -> !HttpHeaders.AUTHORIZATION.equals(h))
                            .collect(Collectors.toMap(h -> h, req::getHeader));

                    // add authorization headers to request
                    headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + optAccessToken.get());
                    headers.put(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());

                    resourceInterceptorService.forwardRequest(resp, request, headers);

                } else {
                    // invalid request, access token missing
                    SelfCarePortalError error = new SelfCarePortalError("Authentication Error!",
                            "Some values are invalid of the request. Please try signing in again.");
                    LOG.error("Requested access token is invalid. Returning error to frontend, " + error);
                    OAuthService.getInstance().removeAllCookiesFromRequest(req, resp);
                    Utils.returnResponse(resp, HttpStatus.SC_BAD_REQUEST, new JSONObject(error));
                }
            }
        } catch (TokenGenerationException | IOException | URISyntaxException e) {
            LOG.error("Exception occurred while processing frontend request. Caused by, ", e);
            SelfCarePortalError error = new SelfCarePortalError("Request Forwarding Error!",
                    "Something went wrong during the authentication process. Please try signing in again.");
            OAuthService.getInstance().removeAllCookiesFromRequest(req, resp);
            Utils.returnResponse(resp, HttpStatus.SC_UNAUTHORIZED, new JSONObject(error));
        } catch (SessionTimeoutException e) {
            LOG.debug("Session timeout exception occurred while processing request. Caused by, ", e);
            OAuthService.getInstance().removeAllCookiesFromRequest(req, resp);
            SelfCarePortalError error = new SelfCarePortalError("Session Has Expired!", "Please try signing in again.");
            Utils.returnResponse(resp, HttpStatus.SC_UNAUTHORIZED, new JSONObject(error));
        }
    }

    @Generated(message = "Ignoring since method contains no logics")
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        doGet(req, resp);
    }

}
