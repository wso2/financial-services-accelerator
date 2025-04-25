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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.databridge.commons.exception.SessionTimeoutException;
import org.wso2.financial.services.accelerator.scp.webapp.exception.TokenGenerationException;
import org.wso2.financial.services.accelerator.scp.webapp.util.Constants;
import org.wso2.financial.services.accelerator.scp.webapp.util.Utils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;

/**
 * APIMService
 * <p>
 * Contains methods to process requests that are forwarding to APIM
 */
public class APIMService implements Serializable {
    private static final long serialVersionUID = -1968486857447834419L;
    private static final Log LOG = LogFactory.getLog(APIMService.class);

    public Optional<String> constructAccessTokenFromCookies(HttpServletRequest req) {
        Optional<Cookie> cookieAccessTokenPart2 = Utils
                .getCookieFromRequest(req, Constants.ACCESS_TOKEN_COOKIE_NAME + "_P2");

        if (cookieAccessTokenPart2.isPresent()) {
            // access token part 2 is present as a cookie
            final String accessTokenPart1 = req.getHeader(HttpHeaders.AUTHORIZATION);


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
            throws TokenGenerationException {
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
            // Retrieving application details to get client name and logo url.
            JSONObject applicationDetails = Utils.sendApplicationRetrievalRequest();
            if (applicationDetails != null) {
                Map<String, String> appClientNameMap = new HashMap<>();
                Map<String, String> appLogoUrlMap = new HashMap<>();
                for (Object application : applicationDetails.getJSONArray(Constants.APPLICATIONS)) {
                    JSONObject applicationJson = (JSONObject) application;
                    JSONObject configs = applicationJson.getJSONObject(Constants.ADVANCED_CONFIGURATIONS);
                    JSONArray spData = configs.getJSONArray(Constants.ADDITIONAL_SP_PROPERTIES);
                    String clientName = null;
                    String logoUrl = null;
                    for (Object spDatum : spData) {
                        JSONObject spDatumJson = (JSONObject) spDatum;
                        if (spDatumJson.getString(Constants.NAME).equalsIgnoreCase(
                                Utils.getParameter(Constants.APP_NAME))) {
                            String softwareClientName = spDatumJson.getString(Constants.VALUE);
                            if (StringUtils.isNotEmpty(softwareClientName)) {
                                clientName = softwareClientName;
                            }
                        }
                        if (spDatumJson.getString(Constants.NAME).equalsIgnoreCase(
                                Utils.getParameter(Constants.APP_LOGO_URL))) {
                            logoUrl = spDatumJson.getString(Constants.VALUE);
                        }
                    }
                    if (clientName == null) {
                        clientName = applicationJson.getString(Constants.NAME);
                    }
                    appClientNameMap.put(applicationJson.getString(Constants.CLIENT_ID_CC), clientName);
                    appLogoUrlMap.put(applicationJson.getString(Constants.CLIENT_ID_CC), logoUrl);
                }

                if (responseJson.has(Constants.DATA)) {
                    for (Object dataElement : responseJson.getJSONArray(Constants.DATA)) {
                        JSONObject dataElementJson = (JSONObject) dataElement;
                        String clientId = dataElementJson.optString(Constants.CLIENT_ID_CC);
                        if (clientId != null && appClientNameMap.containsKey(clientId)) {
                            dataElementJson.put(Constants.SOFTWARE_CLIENT_NAME, appClientNameMap.get(clientId));
                            dataElementJson.put(Constants.LOGO_URL, appLogoUrlMap.get(clientId));
                        }
                    }
                }
            }
        }
        // returning  response
        Utils.returnResponse(resp, statusCode, responseJson);

    }

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
