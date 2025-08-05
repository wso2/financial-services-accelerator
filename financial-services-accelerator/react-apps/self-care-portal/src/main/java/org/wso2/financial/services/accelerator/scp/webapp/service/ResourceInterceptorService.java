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

package org.wso2.financial.services.accelerator.scp.webapp.service;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
import org.wso2.carbon.databridge.commons.exception.SessionTimeoutException;
import org.wso2.financial.services.accelerator.scp.webapp.exception.TokenGenerationException;
import org.wso2.financial.services.accelerator.scp.webapp.util.Constants;
import org.wso2.financial.services.accelerator.scp.webapp.util.Utils;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;

/**
 * ResourceInterceptorService
 * <p>
 * Contains methods to process requests that are forwarding to IS
 */
public class ResourceInterceptorService implements Serializable {
    private static final long serialVersionUID = -1968486857447834419L;
    private static final Log LOG = LogFactory.getLog(ResourceInterceptorService.class);

    @SuppressFBWarnings({"COOKIE_USAGE", "SERVLET_HEADER"})
    // Suppressed content - cookieAccessTokenPart1.get().getValue().equals(reqAccessTokenPart1)
    // Suppressed content - req.getHeader(HttpHeaders.AUTHORIZATION)
    // Suppression reason - False Positive : The cookie values are only read and here. No sensitive info is added to
    //                      the cookie in this step.
    // Suppressed warning count - 2
    public Optional<String> constructAccessTokenFromCookies(HttpServletRequest req) {
        Optional<Cookie> cookieAccessTokenPart2 = Utils
                .getCookieFromRequest(req, Constants.ACCESS_TOKEN_COOKIE_NAME + "_P2");

        if (cookieAccessTokenPart2.isPresent()) {
            // access token part 2 is present as a cookie
            final String accessTokenPart1 = req.getHeader(HttpHeaders.AUTHORIZATION).replaceAll("\n\r", "");


            if (StringUtils.isNotEmpty(accessTokenPart1)) {
                // access token part 1 is present in the request
                String reqAccessTokenPart1 = accessTokenPart1.replace("Bearer ", "");

                Optional<Cookie> cookieAccessTokenPart1 = Utils
                        .getCookieFromRequest(req, Constants.ACCESS_TOKEN_COOKIE_NAME + "_P1");

                if (cookieAccessTokenPart1.isPresent()
                        && cookieAccessTokenPart1.get().getValue().equals(reqAccessTokenPart1)) {
                    // access token part 1 is present in the cookie and its equal to request access token part 1
                    return Optional.of(reqAccessTokenPart1 + cookieAccessTokenPart2.get().getValue());
                }
            }
        }
        return Optional.empty();
    }

    @SuppressFBWarnings("COOKIE_USAGE")
    // Suppressed content - cookie.getName()
    // Suppression reason - False Positive : The cookie values are only read and here. No sensitive info is added to
    //                      the cookie in this step.
    // Suppressed warning count - 1
    public Optional<String> constructRefreshTokenFromCookies(HttpServletRequest req) {
        Optional<Cookie> refreshTokenPart1 = Utils
                .getCookieFromRequest(req, Constants.REFRESH_TOKEN_COOKIE_NAME + "_P1");

        Optional<Cookie> refreshTokenPart2 = Utils
                .getCookieFromRequest(req, Constants.REFRESH_TOKEN_COOKIE_NAME + "_P2");

        if (refreshTokenPart1.isPresent() && refreshTokenPart2.isPresent()) {
            return Optional.of(refreshTokenPart1.get().getValue() + refreshTokenPart2.get().getValue());
        }
        return Optional.empty();
    }

    public void forwardRequest(HttpServletResponse resp, HttpUriRequest httpRequest, Map<String, String> headers)
            throws TokenGenerationException, URISyntaxException {

        if (HttpMethod.GET.equals(httpRequest.getMethod())) {
            // Replacing software id with client id before sending the request to the backend
            URI originalUri = httpRequest.getURI();
            String queryParams = originalUri.getQuery();
            if (queryParams != null && queryParams.contains("clientIds")) {
                String clientIds = "";
                Map<String, String> appClientNameMap = Utils.getAppClientNameMap();
                String[] queryParamsArray = queryParams.split("&");
                for (String queryParam : queryParamsArray) {
                    if (queryParam.startsWith("clientIds")) {
                        String[] softwareIds = queryParam.split("=")[1].split(",");
                        for (String softwareId : softwareIds) {
                            for (Map.Entry<String, String> entry : appClientNameMap.entrySet()) {
                                if (entry.getValue().equals(softwareId)) {
                                    clientIds = clientIds.concat(entry.getKey() + ",");
                                }
                            }
                        }
                    }
                }
                // Rebuild URI with new query parameters
                URI newUri = new URIBuilder(originalUri)
                        .setParameter("clientIds", clientIds)
                        .build();
                httpRequest = new HttpGet(newUri);
            }
        }

        // adding headers to request
        headers.forEach(httpRequest::addHeader);

        // Updating the host header to apim host
        String hostname = httpRequest.getURI().getHost();
        httpRequest.removeHeaders(HttpHeaders.HOST);
        httpRequest.addHeader(HttpHeaders.HOST, hostname);

        JSONObject responseJson = Utils.sendRequest(httpRequest);
        int statusCode = responseJson.optInt(Constants.RESPONSE_STATUS_CODE, 200);
        responseJson.remove(Constants.RESPONSE_STATUS_CODE);

        if (HttpMethod.GET.equals(httpRequest.getMethod())) {

            Map<String, String> appClientNameMap = Utils.getAppClientNameMap();
            Map<String, String> appLogoUrlMap = Utils.getAppLogoUrlMap();

            if (responseJson.has(Constants.DATA)) {
                for (Object dataElement : responseJson.getJSONArray(Constants.DATA)) {
                    JSONObject dataElementJson = (JSONObject) dataElement;
                    String clientId = dataElementJson.optString(Constants.CLIENT_ID_CC);
                    if (clientId != null && appClientNameMap.containsKey(clientId)) {
                        dataElementJson.put(Constants.SOFTWARE_CLIENT_NAME, appClientNameMap.get(clientId));
                        dataElementJson.put(Constants.LOGO_URL, appLogoUrlMap.get(clientId));
                    } else {
                        // For deleted applications
                        dataElementJson.put(Constants.SOFTWARE_CLIENT_NAME, clientId);
                    }
                }
            }
        }
        // returning  response
        Utils.returnResponse(resp, statusCode, responseJson);

    }

    @SuppressFBWarnings("COOKIE_USAGE")
    // Suppressed content - cookie.getName()
    // Suppression reason - False Positive : The cookie values are only read and here. No sensitive info is added to
    //                      the cookie in this step.
    // Suppressed warning count - 1
    public boolean isAccessTokenExpired(HttpServletRequest req) throws SessionTimeoutException {
        Optional<Cookie> optValidityCookie = Utils
                .getCookieFromRequest(req, Constants.TOKEN_VALIDITY_COOKIE_NAME);
        try {
            if (optValidityCookie.isPresent()) {
                Cookie validityCookie = optValidityCookie.get();
                LocalDateTime tokenExpiry = Utils.parseEncodedStringToDate(validityCookie.getValue());

                return LocalDateTime.now().isAfter(tokenExpiry);
            }
            // cookie not found in the request
            LOG.debug(String.format("Invalid request received. %s cookie is missing",
                    Constants.TOKEN_VALIDITY_COOKIE_NAME));
        } catch (IllegalArgumentException e) {
            LOG.error(String.format("Invalid request received. %s cookie is invalid",
                    Constants.TOKEN_VALIDITY_COOKIE_NAME));
        }
        throw new SessionTimeoutException(String.format
                ("Invalid request received. %s cookie is missing", Constants.TOKEN_VALIDITY_COOKIE_NAME));
    }
}
