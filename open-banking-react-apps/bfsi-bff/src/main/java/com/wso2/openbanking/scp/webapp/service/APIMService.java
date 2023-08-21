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

package com.wso2.openbanking.scp.webapp.service;

import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.scp.webapp.exception.TokenGenerationException;
import com.wso2.openbanking.scp.webapp.util.Constants;
import com.wso2.openbanking.scp.webapp.util.Utils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONObject;
import org.wso2.carbon.databridge.commons.exception.SessionTimeoutException;

import java.io.Serializable;
import java.text.ParseException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.wso2.openbanking.scp.webapp.util.Constants.PART_ONE;
import static com.wso2.openbanking.scp.webapp.util.Constants.PART_TWO;

/**
 * APIMService.
 * <p>
 * Contains methods to process requests that are forwarding to APIM.
 */
public class APIMService implements Serializable {
    private static final long serialVersionUID = -1968486857447834419L;
    private static final Log LOG = LogFactory.getLog(APIMService.class);

    public Optional<String> constructAccessTokenFromCookies(HttpServletRequest req) {
        Optional<Cookie> cookieAccessTokenPart2 = Utils
                .getCookieFromRequest(req, Constants.ACCESS_TOKEN_COOKIE_NAME + PART_TWO);

        if (cookieAccessTokenPart2.isPresent()) {
            // access token part 2 is present as a cookie
            final String accessTokenPart1 = req.getHeader(HttpHeaders.AUTHORIZATION);


            if (StringUtils.isNotEmpty(accessTokenPart1)) {
                // access token part 1 is present in the request
                String reqAccessTokenPart1 = accessTokenPart1.replace("Bearer ", "");

                Optional<Cookie> cookieAccessTokenPart1 = Utils
                        .getCookieFromRequest(req, Constants.ACCESS_TOKEN_COOKIE_NAME + PART_ONE);

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
                .getCookieFromRequest(req, Constants.REFRESH_TOKEN_COOKIE_NAME + PART_ONE);

        Optional<Cookie> refreshTokenPart2 = Utils
                .getCookieFromRequest(req, Constants.REFRESH_TOKEN_COOKIE_NAME + PART_TWO);

        if (refreshTokenPart1.isPresent() && refreshTokenPart2.isPresent()) {
            return Optional.of(refreshTokenPart1.get().getValue() + refreshTokenPart2.get().getValue());
        }
        return Optional.empty();
    }

    public Optional<String> constructIdTokenFromCookies(HttpServletRequest req) {
        Optional<Cookie> idTokenPart1 = Utils
                .getCookieFromRequest(req, Constants.ID_TOKEN_COOKIE_NAME + PART_ONE);

        Optional<Cookie> idTokenPart2 = Utils
                .getCookieFromRequest(req, Constants.ID_TOKEN_COOKIE_NAME + PART_TWO);

        if (idTokenPart1.isPresent() && idTokenPart2.isPresent()) {
            return Optional.of(idTokenPart1.get().getValue() + idTokenPart2.get().getValue());
        }
        return Optional.empty();
    }

    public void forwardRequest(HttpServletResponse resp, HttpUriRequest httpRequest, Map<String, String> headers)
            throws TokenGenerationException {
        // adding headers to request
        headers.forEach(httpRequest::addHeader);

        JSONObject responseJson = Utils.sendRequest(httpRequest);
        int statusCode = responseJson.optInt("res_status_code", 200);
        responseJson.remove("res_status_code");

        // returning  response
        Utils.returnResponse(resp, statusCode, responseJson);

    }

    public boolean isAccessTokenExpired(HttpServletRequest req) throws SessionTimeoutException {
        Optional<String> optAccessToken = constructAccessTokenFromCookies(req);
        try {
            if (optAccessToken.isPresent()) {
                net.minidev.json.JSONObject tokenBody = JWTUtils.decodeRequestJWT(optAccessToken.get(), "body");
                final Number tokenExp = tokenBody.getAsNumber("exp");
                return Instant.now().getEpochSecond() > tokenExp.longValue();
            }
            // cookie not found in the request
            LOG.debug(String.format("Invalid request received. %s cookie is missing",
                    Constants.ACCESS_TOKEN_COOKIE_NAME));
        } catch (IllegalArgumentException e) {
            LOG.error(String.format("Invalid request received. %s cookie is invalid",
                    Constants.ACCESS_TOKEN_COOKIE_NAME), e);
        } catch (ParseException e) {
            LOG.error(String.format("Invalid request received. Cannot parse %s cookie",
                    Constants.ACCESS_TOKEN_COOKIE_NAME), e);
        }
        throw new SessionTimeoutException(String.format
                ("Invalid request received. %s cookie is invalid", Constants.ACCESS_TOKEN_COOKIE_NAME));
    }
}
