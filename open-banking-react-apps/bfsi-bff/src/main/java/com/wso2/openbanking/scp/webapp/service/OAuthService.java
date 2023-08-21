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
import com.wso2.openbanking.scp.webapp.model.CookieAttributes;
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
                .addParameter(Constants.RESPONSE_TYPE, Constants.OAUTH_CODE)
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
        
        int tokenExpiry = Constants.DEFAULT_COOKIE_MAX_AGE;
        if (tokens.has(Constants.EXPIRES_IN)) {
            tokenExpiry = tokens.getInt(Constants.EXPIRES_IN);
        }

        final CookieAttributes accessTokenAttributes = 
            new CookieAttributes(Constants.ACCESS_TOKEN_COOKIE_NAME, true, tokenExpiry);
        final CookieAttributes idTokenAttributes = 
            new CookieAttributes(Constants.ID_TOKEN_COOKIE_NAME, false, tokenExpiry);
        final CookieAttributes refreshTokenAttributes = 
            new CookieAttributes(Constants.REFRESH_TOKEN_COOKIE_NAME, true, tokenExpiry);

        // adding tokens as cookies
        addCookiesToResponse(req, resp, accessToken, accessTokenAttributes);
        addCookiesToResponse(req, resp, idToken, idTokenAttributes);
        addCookiesToResponse(req, resp, refreshToken, refreshTokenAttributes);
    }

    private void addCookieToResponse(HttpServletRequest req, HttpServletResponse resp, CookieAttributes attributes) {
        Optional<Cookie> optCookie = getCookieFromRequest(req, attributes.getName());
        Cookie cookie;
        if (optCookie.isPresent()) {
            cookie = optCookie.get();
            cookie.setValue(attributes.getValue());
        } else {
            cookie = new Cookie(attributes.getName(), attributes.getValue());
        }
        cookie.setSecure(attributes.isSecure());
        cookie.setMaxAge(attributes.getMaxAge());
        cookie.setPath(attributes.getPath());
        cookie.setHttpOnly(attributes.isHttpOnly());

        resp.addCookie(cookie);
    }

    /**
     * Add part 1 cookie and part 2 cookie to HttpServletResponse.
     *
     * @param req Http Servlet Request
     * @param resp Http Servlet Response
     * @param token Token to be added as cookies
     * @param attributes an object of cookie attributes
     */
    private void addCookiesToResponse(HttpServletRequest req, HttpServletResponse resp, String token,
                                      CookieAttributes attributes) {
        if (StringUtils.isNotEmpty(token)) {
            final int tokenLength = token.length();
            final String tokenPart1 = token.substring(0, tokenLength / 2);
            final String tokenPart2 = token.substring(tokenLength / 2, tokenLength);

            CookieAttributes tokenPart1Attributes = this.generateTokenAttributes("_P1", tokenPart1, attributes);
            tokenPart1Attributes.setHttpOnly(false);
            addCookieToResponse(req, resp, tokenPart1Attributes);
            
            CookieAttributes tokenPart2Attributes = this.generateTokenAttributes("_P2", tokenPart2, attributes);
            addCookieToResponse(req, resp, tokenPart2Attributes);
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
                    cookie.setPath(Constants.DEFAULT_BASE_PATH);
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

    public String generateLogoutUrl(String iamBaseUrl, String idToken) throws URISyntaxException {
        URI logoutUrl = new URIBuilder(iamBaseUrl)
                .setPath(Constants.PATH_LOGOUT)
                .addParameter(Constants.ID_TOKEN_HINT, idToken)
                .addParameter(Constants.POST_LOGOUT_REDIRECT_URI, iamBaseUrl + Constants.PATH_CALLBACK)
                .build();

        return logoutUrl.toString();
    }

    private CookieAttributes generateTokenAttributes(String namePart, String tokenValue, 
        CookieAttributes commonAttributes) {
        
        CookieAttributes tokenAttributes = new CookieAttributes(commonAttributes.getName() + namePart, tokenValue);
        tokenAttributes.setSecure(commonAttributes.isSecure());
        tokenAttributes.setMaxAge(commonAttributes.getMaxAge());
        tokenAttributes.setPath(commonAttributes.getPath());
        tokenAttributes.setHttpOnly(commonAttributes.isHttpOnly());

        return tokenAttributes;
    }
}
