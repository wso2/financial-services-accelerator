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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.json.JSONObject;
import org.wso2.carbon.databridge.commons.exception.SessionTimeoutException;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.common.util.JWTUtils;
import org.wso2.financial.services.accelerator.scp.webapp.exception.TokenGenerationException;
import org.wso2.financial.services.accelerator.scp.webapp.model.SelfCarePortalError;
import org.wso2.financial.services.accelerator.scp.webapp.service.OAuthService;
import org.wso2.financial.services.accelerator.scp.webapp.service.ResourceInterceptorService;
import org.wso2.financial.services.accelerator.scp.webapp.util.Constants;
import org.wso2.financial.services.accelerator.scp.webapp.util.Utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ResourceInterceptorServlet
 * <p>
 * This interrupts the requests, validates user permissions, and forwards requests to Identity Server
 * using admin Basic Auth credentials. Permission logic: CCO users (consents:read_all scope) can
 * access all admin paths; non-CCO users can only access /search and /revoke when the userId param
 * matches their token subject.
 */
@WebServlet(name = "ResourceInterceptorServlet", urlPatterns = {"/scp/admin/*"})
public class ResourceInterceptorServlet extends HttpServlet {

    private static final long serialVersionUID = 7385252581004845440L;
    private static final Log LOG = LogFactory.getLog(ResourceInterceptorServlet.class);
    private static final String CCO_SCOPE = "consents:read_all";
    private static final String SUPER_TENANT_DOMAIN = "carbon.super";
    private final ResourceInterceptorService resourceInterceptorService = new ResourceInterceptorService();

    @Generated(message = "Ignoring since all cases are covered from other unit tests")
    @Override
    @SuppressFBWarnings({"SERVLET_QUERY_STRING", "IMPROPER_UNICODE"})
    // Suppressed content - req.getQueryString(), req.getRequestURI()
    // Suppression reason - False Positive: These parameters are read only
    // Suppressed warning count - 2
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            LOG.debug(String.format("New request received: %s ? %s",
                    req.getRequestURI().replaceAll(FinancialServicesConstants.SANITIZING_CHARACTERS, ""),
                    req.getQueryString().replaceAll(FinancialServicesConstants.SANITIZING_CHARACTERS, "")));
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

                    // add new tokens as cookies to response
                    oAuthService.generateCookiesFromTokens(tokenResponse, req, resp);

                    String accessToken = tokenResponse.getString(Constants.ACCESS_TOKEN);
                    if (!validateUserPermissions(req, accessToken, resp)) {
                        return;
                    }

                    final String isBaseUrl = Utils.getParameter(Constants.IS_BASE_URL);
                    HttpUriRequest request = Utils
                            .getHttpUriRequest(isBaseUrl, req.getMethod(), req.getRequestURI(), req.getQueryString());

                    Map<String, String> headers = new HashMap<>();
                    headers.put(HttpHeaders.AUTHORIZATION, "Basic " + getAdminBasicAuth());
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
                    String accessToken = optAccessToken.get();
                    if (!validateUserPermissions(req, accessToken, resp)) {
                        return;
                    }

                    final String isBaseUrl = Utils.getParameter(Constants.IS_BASE_URL);
                    HttpUriRequest request = Utils
                            .getHttpUriRequest(isBaseUrl, req.getMethod(), req.getRequestURI(), req.getQueryString());

                    Map<String, String> headers = new HashMap<>();
                    headers.put(HttpHeaders.AUTHORIZATION, "Basic " + getAdminBasicAuth());
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

    /**
     * Validates whether the authenticated user is permitted to access the requested admin path.
     * CCO users (consents:read_all scope) may access all admin paths. Non-CCO users may only
     * access /search and /revoke when the userId query parameter matches their token subject.
     *
     * @return true if permitted, false if a 401/403 error response has already been written
     */
    boolean validateUserPermissions(HttpServletRequest req, String accessToken, HttpServletResponse resp) {
        try {
            String tokenBody = JWTUtils.decodeRequestJWT(accessToken, "body");
            JSONObject tokenBodyObj = new JSONObject(tokenBody);
            String tokenScopes = tokenBodyObj.getString("scope");

            if (isCustomerCareOfficer(tokenScopes)) {
                return true;
            }

            // Non-CCO users: only /search and /revoke are allowed, with userId self-match
            String pathInfo = req.getPathInfo();
            String userId = null;
            if ("/search".equals(pathInfo)) {
                String[] userIds = req.getParameterValues("userIds");
                userId = (userIds != null && userIds.length > 0) ? userIds[0] : null;
            } else if ("/revoke".equals(pathInfo)) {
                userId = req.getParameter("userId");
            } else {
                LOG.warn("Non-CCO user attempted to access restricted admin path: " +
                        String.valueOf(pathInfo).replaceAll(FinancialServicesConstants.SANITIZING_CHARACTERS, ""));
                SelfCarePortalError error = new SelfCarePortalError("Forbidden!",
                        "Insufficient permissions to access this resource.");
                Utils.returnResponse(resp, HttpStatus.SC_FORBIDDEN, new JSONObject(error));
                return false;
            }

            String tokenSub = getUserNameWithTenantDomain(tokenBodyObj.getString("sub"));
            String normalizedUserId = StringUtils.isEmpty(userId) ? "" : getUserNameWithTenantDomain(userId);
            if (StringUtils.isEmpty(normalizedUserId) || !normalizedUserId.equals(tokenSub)) {
                LOG.error("UserId and token subject do not match for non-CCO user.");
                SelfCarePortalError error = new SelfCarePortalError("Unauthorized!",
                        "UserId and token subject do not match.");
                Utils.returnResponse(resp, HttpStatus.SC_UNAUTHORIZED, new JSONObject(error));
                return false;
            }
            return true;
        } catch (ParseException e) {
            LOG.error("Failed to parse access token for permission validation. Caused by, ", e);
            SelfCarePortalError error = new SelfCarePortalError("Authentication Error!",
                    "Failed to validate user permissions.");
            Utils.returnResponse(resp, HttpStatus.SC_UNAUTHORIZED, new JSONObject(error));
            return false;
        }
    }

    boolean isCustomerCareOfficer(String scopes) {
        return StringUtils.isNotEmpty(scopes) && scopes.contains(CCO_SCOPE);
    }

    String getUserNameWithTenantDomain(String userName) {
        if (userName.endsWith(SUPER_TENANT_DOMAIN)) {
            return userName;
        }
        return userName + "@" + SUPER_TENANT_DOMAIN;
    }

    String getAdminBasicAuth() {
        FinancialServicesConfigParser configParser = FinancialServicesConfigParser.getInstance();
        String auth = configParser.getAdminUsername() + ":" + configParser.getAdminPassword();
        return Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    }

}
