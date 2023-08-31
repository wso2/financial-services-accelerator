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

import com.wso2.openbanking.scp.webapp.exception.TokenGenerationException;
import com.wso2.openbanking.scp.webapp.util.Constants;
import com.wso2.openbanking.scp.webapp.util.Utils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * OAuthService.
 * <p>
 * This specifies service methods to use in oauth2 flow.
 */
public class OAuthService {

    private static OAuthService oauthService;

    private OAuthService() {
        // private constructor
    }

    public static synchronized OAuthService getInstance() {
        if (oauthService == null) {
            oauthService = new OAuthService();
        }
        return oauthService;
    }

    public String generateAuthorizationUrl(String iamBaseUrl, String clientId)
            throws URISyntaxException {
        URI authUri = new URIBuilder(iamBaseUrl)
                .setPath(Constants.PATH_AUTHORIZE)
                .addParameter(Constants.RESPONSE_TYPE, "code")
                .addParameter(Constants.OAUTH_SCOPE, "consentmgt openid consents:read_all consents:read_self")
                .addParameter(Constants.CLIENT_ID, clientId)
                .addParameter(Constants.REDIRECT_URI, iamBaseUrl + Constants.PATH_CALLBACK)
                .build();

        return authUri.toString();
    }

    private JSONObject sendTokenRequest(String iamBaseUrl, String clientKey, String clientSecret, List<NameValuePair>
            params) throws UnsupportedEncodingException, TokenGenerationException {

        HttpPost tokenReq = new HttpPost(iamBaseUrl + Constants.PATH_TOKEN);

        // generating basic authorization
        final String auth = clientKey + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        // add request headers
        tokenReq.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);
        tokenReq.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());

        tokenReq.setEntity(new UrlEncodedFormEntity(params));

        // sending request
        return Utils.sendTokenRequest(tokenReq);
    }

    public JSONObject sendAccessTokenRequest(String iamBaseUrl, String clientKey, String clientSecret, String code)
            throws UnsupportedEncodingException, TokenGenerationException {
        // generate access token request params
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(Constants.CLIENT_ID, clientKey));
        params.add(new BasicNameValuePair(Constants.GRANT_TYPE, "authorization_code"));
        params.add(new BasicNameValuePair(Constants.OAUTH_CODE, code));
        params.add(new BasicNameValuePair(Constants.REDIRECT_URI, iamBaseUrl + Constants.PATH_CALLBACK));

        return sendTokenRequest(iamBaseUrl, clientKey, clientSecret, params);
    }

    public JSONObject sendRefreshTokenRequest(String iamBaseUrl, String clientKey, String clientSecret,
                                              String token, String scopes)
            throws UnsupportedEncodingException, TokenGenerationException {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(Constants.GRANT_TYPE, Constants.REFRESH_TOKEN));
        params.add(new BasicNameValuePair(Constants.REFRESH_TOKEN, token));
        params.add(new BasicNameValuePair(Constants.CLIENT_ID, clientKey));
        params.add(new BasicNameValuePair(Constants.OAUTH_SCOPE, scopes));

        return sendTokenRequest(iamBaseUrl, clientKey, clientSecret, params);
    }

    public void generateCookiesFromTokens(JSONObject tokens, HttpServletRequest req, HttpServletResponse resp) {
        final String accessToken = tokens.getString(Constants.ACCESS_TOKEN);
        final String idToken = tokens.optString(Constants.ID_TOKEN);
        final String refreshToken = tokens.getString(Constants.REFRESH_TOKEN);
        final int tokenExpiry = tokens.getInt(Constants.EXPIRES_IN);

        // adding tokens as cookies
        addCookiesToResponse(req, resp, Constants.ACCESS_TOKEN_COOKIE_NAME, accessToken,
                Constants.DEFAULT_COOKIE_PATH, Constants.DEFAULT_COOKIE_MAX_AGE);
        addCookiesToResponse(req, resp, Constants.ID_TOKEN_COOKIE_NAME, idToken,
                Constants.DEFAULT_COOKIE_PATH, 86400);
        // adding refresh token, expires after 24 hours
        addCookiesToResponse(req, resp, Constants.REFRESH_TOKEN_COOKIE_NAME, refreshToken,
                Constants.DEFAULT_COOKIE_PATH, 86400);

        LocalDateTime accessTokenExpiry = LocalDateTime.now().plusSeconds(tokenExpiry);
        addCookieToResponse(req, resp, Constants.TOKEN_VALIDITY_COOKIE_NAME,
                Utils.formatDateToEncodedString(accessTokenExpiry), Constants.DEFAULT_COOKIE_PATH, 86400);
    }

    private void addCookieToResponse(HttpServletRequest req, HttpServletResponse resp, String cookieName,
                                     String cookieValue, String path, int maxAge) {
        Optional<Cookie> optCookie = getCookieFromRequest(req, cookieName);
        Cookie cookie;
        if (optCookie.isPresent()) {
            cookie = optCookie.get();
            cookie.setValue(cookieValue);
        } else {
            cookie = new Cookie(cookieName, cookieValue);
        }
        cookie.setSecure(true);
        cookie.setMaxAge(maxAge);
        cookie.setPath(path);

        resp.addCookie(cookie);
    }

    private void addCookiesToResponse(HttpServletRequest req, HttpServletResponse resp, String cookieName, String token,
                                      String path, int maxAge) {
        if (StringUtils.isNotEmpty(token)) {
            final int tokenLength = token.length();
            final String tokenPart1 = token.substring(0, tokenLength / 2);
            final String tokenPart2 = token.substring(tokenLength / 2, tokenLength);

            addCookieToResponse(req, resp, cookieName + "_P1", tokenPart1, path, maxAge);
            addCookieToResponse(req, resp, cookieName + "_P2", tokenPart2, path, maxAge);
        }
    }

    public void removeAllCookiesFromRequest(HttpServletRequest req, HttpServletResponse res) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null || cookies.length == 0) {
            return;
        }
        // invalidating all ob self-care-portal cookies
        Arrays.stream(cookies)
                .filter(Objects::nonNull)
                .filter(cookie -> cookie.getName().startsWith(Constants.COOKIE_BASE_NAME))
                .forEach(cookie -> {
                    cookie.setMaxAge(0);
                    cookie.setValue("");
                    cookie.setPath("/consentmgr");
                    res.addCookie(cookie);
                });
    }

    private Optional<Cookie> getCookieFromRequest(HttpServletRequest req, String cookieName) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return Optional.of(cookie);
                }
            }
        }
        return Optional.empty();
    }
}
