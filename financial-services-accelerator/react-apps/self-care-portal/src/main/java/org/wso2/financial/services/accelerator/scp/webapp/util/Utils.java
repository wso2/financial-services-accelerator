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

package org.wso2.financial.services.accelerator.scp.webapp.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.encoder.Encode;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.common.util.HTTPClientUtils;
import org.wso2.financial.services.accelerator.scp.webapp.exception.TokenGenerationException;
import org.wso2.financial.services.accelerator.scp.webapp.model.SelfCarePortalError;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Utils
 * <p>
 * Contains utility methods used by self-care portal web application
 */
public class Utils {
    private static final Log LOG = LogFactory.getLog(Utils.class);

    private Utils() {
        // hiding constructor
    }

    public static JSONObject sendRequest(HttpUriRequest request)
            throws TokenGenerationException {

        LOG.debug(String.format("Sending request to %s", request.getURI().toString().replaceAll("\n\r", "")));
        String responseStr = null;
        try (CloseableHttpResponse response = HTTPClientUtils.getHttpsClient().execute(request)) {
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                responseStr = EntityUtils.toString(responseEntity);
            }
            if ((response.getStatusLine().getStatusCode() / 100) != 2) {
                if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    LOG.debug(String.format("Received unauthorized(401) response. body: %s",
                            responseStr.replaceAll("\n\r", "")));
                    throw new TokenGenerationException("Received unauthorized Response: " + responseStr);
                }
            } else {
                // received success (200 range) response
                JSONObject responseJson;
                if (StringUtils.isNotEmpty(responseStr)) {
                    responseJson = new JSONObject(responseStr);
                } else {
                    responseJson = new JSONObject();
                }
                responseJson.put("res_status_code", response.getStatusLine().getStatusCode());
                return responseJson;
            }

        } catch (IOException e) {
            LOG.error("Exception occurred while forwarding request. Caused by, ", e);
        }
        throw new TokenGenerationException("Unexpected response received for the request. path: " +
                request.getURI() + " response:" + responseStr);
    }

    public static JSONObject sendTokenRequest(HttpPost tokenReq) throws TokenGenerationException {
        try (CloseableHttpResponse response = HTTPClientUtils.getHttpsClient().execute(tokenReq)) {
            String responseStr = EntityUtils.toString(response.getEntity());

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // received success response
                return new JSONObject(responseStr);
            }
            LOG.error(String.format("Invalid response received for token request. Error: %s",
                    responseStr.replaceAll("\n\r", "")));
        } catch (IOException e) {
            LOG.error("Exception occurred while sending token request. Caused by, ", e);
        } catch (JSONException e) {
            LOG.error("Exception occurred while processing token response. Caused by, ", e);
        }
        throw new TokenGenerationException("Invalid response received for token request");
    }

    public static HttpUriRequest getHttpUriRequest(String isBaseUrl, String requestMethod, String requestURI,
                                                   String queryParams) {

        final String uri = isBaseUrl + requestURI.replaceFirst(Constants.PREFIX_CONSENT_MANAGER,
                Constants.PREFIX_OB_CONSENT) + "?" + queryParams;

        switch (requestMethod) {
            case HttpDelete.METHOD_NAME:
                return new HttpDelete(uri);
            default:
                return new HttpGet(uri);
        }
    }

    public static Optional<Cookie> getCookieFromRequest(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .findFirst();
    }

    @Generated(message = "Ignoring since method do not contain a logic")
    public static String formatDateToEncodedString(LocalDateTime localDateTime) {
        return Base64.getEncoder().encodeToString(localDateTime
                .format(DateTimeFormatter.ofPattern(Constants.SCP_TOKEN_VALIDITY_DATE_FORMAT))
                .getBytes(StandardCharsets.UTF_8));
    }

    @Generated(message = "Ignoring since method do not contain a logic")
    public static LocalDateTime parseEncodedStringToDate(String dateStr) {
        String tokenCreatedTime = new String(Base64.getDecoder().decode(dateStr), StandardCharsets.UTF_8);
        return LocalDateTime.parse(tokenCreatedTime,
                DateTimeFormatter.ofPattern(Constants.SCP_TOKEN_VALIDITY_DATE_FORMAT));
    }

    @Generated(message = "Ignoring since method do not contain a logic")
    public static void returnResponse(HttpServletResponse resp, int statusCode, JSONObject payload) {
        try {
            LOG.debug(String.format("Returning response to frontend. payload: %s",
                    payload.toString().replaceAll("\n\r", "")));
            // returning  response
            resp.setContentType(ContentType.APPLICATION_JSON.getMimeType());
            resp.setStatus(statusCode);
            PrintWriter out = resp.getWriter();
//            out.print(payload);
            out.write(Encode.forHtml(payload.toString()));
            out.flush();
        } catch (IOException e) {
            LOG.error("Exception occurred while returning response. Caused by, ", e);
        }
    }

    @SuppressFBWarnings("FORMAT_STRING_MANIPULATION")
    // Suppressed content - final String errorUrl = String.format(errRedirectUrlFormat, errorMsg, errorDesc);
    // Suppression reason - False Positive : These fields are internally generated.
    // Suppressed warning count - 1
    @Generated(message = "Ignoring since method do not contain a logic")
    public static void sendErrorToFrontend(SelfCarePortalError error, String errRedirectUrlFormat,
                                           HttpServletResponse resp) {
        LOG.debug("Sending error to frontend.");
        try {
            final String errorMsg = Base64.getUrlEncoder()
                    .encodeToString(error.getMessage().getBytes(StandardCharsets.UTF_8)).replaceAll("\n\r", "");
            final String errorDesc = Base64.getUrlEncoder()
                    .encodeToString((error.getDescription()).getBytes(StandardCharsets.UTF_8)).replaceAll("\n\r", "");

            final String errorUrl = String.format(errRedirectUrlFormat, errorMsg, errorDesc);
            resp.sendRedirect(errorUrl);
        } catch (IOException ex) {
            LOG.error("Exception occurred while redirecting to frontend. Caused by, ", ex);
        }
    }

    /**
     * Retrieve consent management portal related configurations from webapp properties or OB configs.
     */
    public static String getParameter(String requiredParameter) {
        String parameter;
        try {
            InputStream configurations = Utils.class.getClassLoader()
                    .getResourceAsStream(Constants.CONFIG_FILE_NAME);
            Properties configurationProperties = new Properties();
            configurationProperties.load(configurations);
            Boolean isConfiguredInWebapp = Boolean.parseBoolean(
                    configurationProperties.getProperty(Constants.LOCATION_OF_CREDENTIALS));
            if (!isConfiguredInWebapp) {
                parameter = (String) FinancialServicesConfigParser.getInstance().getConfiguration()
                        .get(requiredParameter);
            } else {
                parameter = configurationProperties.getProperty(requiredParameter);
            }
        } catch (IOException e) {
            LOG.error("Error occurred while reading the webapp properties file. Therefore using OB configurations.");
            parameter = (String) FinancialServicesConfigParser.getInstance().getConfiguration()
                    .get(requiredParameter);
        }
        return parameter;
    }

    /**
     * Method to retrieve application details
     */
    public static JSONObject sendApplicationRetrievalRequest() {

//        HttpGet tokenReq = new HttpGet(Utils.getParameter(Constants.IS_BASE_URL) + Constants.PATH_APP_RETRIEVAL);
        HttpGet tokenReq;
        try {
            URIBuilder uriBuilder = new URIBuilder(Utils.getParameter(Constants.IS_BASE_URL) +
                    Constants.PATH_APP_RETRIEVAL);
            tokenReq = new HttpGet(uriBuilder.build().toString());
        } catch (URISyntaxException e) {
            LOG.error("Error building URI for application retrieval request", e);
            throw new RuntimeException(e);
        }
        // generating basic authorization
        String adminUsername =  FinancialServicesConfigParser.getInstance().getAdminUsername();
        String adminPassword = FinancialServicesConfigParser.getInstance().getAdminPassword();
        final String auth = adminUsername + ":" + adminPassword;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        // add request headers
        tokenReq.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);
        tokenReq.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        try (CloseableHttpResponse response = HTTPClientUtils.getHttpsClient().execute(tokenReq)) {
            String responseStr = EntityUtils.toString(response.getEntity());

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // received success response
                return new JSONObject(responseStr);
            }
            LOG.error(String.format("Invalid response received for token request. Error: %s",
                    responseStr.replaceAll("\n\r", "")));
        } catch (IOException e) {
            LOG.error("Exception occurred while sending token request. Caused by, ", e);
        } catch (JSONException e) {
            LOG.error("Exception occurred while processing token response. Caused by, ", e);
        }
        return null;
    }

    /**
     * Method to retrieve application client name and logo url
     * @return Map
     */
    public static Map<String, String> getAppClientNameMap() {
        // Retrieving application details to get client name.
        Map<String, String> appClientNameMap = new HashMap<>();
        JSONObject applicationDetails = sendApplicationRetrievalRequest();
        if (applicationDetails != null) {
            for (Object application : applicationDetails.getJSONArray(Constants.APPLICATIONS)) {
                JSONObject applicationJson = (JSONObject) application;
                JSONObject configs = applicationJson.getJSONObject(Constants.ADVANCED_CONFIGURATIONS);
                JSONArray spData = configs.getJSONArray(Constants.ADDITIONAL_SP_PROPERTIES);
                String clientName = null;
                for (Object spDataItem : spData) {
                    JSONObject spDataItemJson = (JSONObject) spDataItem;
                    if (spDataItemJson.getString(Constants.NAME).equals(Utils.getParameter(Constants.APP_NAME))) {
                        String softwareClientName = spDataItemJson.getString(Constants.VALUE);
                        if (org.apache.commons.lang.StringUtils.isNotEmpty(softwareClientName)) {
                            clientName = softwareClientName;
                        }
                    }
                }
                if (clientName == null) {
                    clientName = applicationJson.getString(Constants.NAME);
                }
                appClientNameMap.put(applicationJson.getString(Constants.CLIENT_ID_CC), clientName);
            }
        }
        return appClientNameMap;
    }

    /**
     * Method to retrieve application logo url
     * @return Map
     */
    public static Map<String, String> getAppLogoUrlMap() {
        // Retrieving application details to get client name and logo url.
        Map<String, String> appLogoUrlMap = new HashMap<>();
        JSONObject applicationDetails = sendApplicationRetrievalRequest();
        if (applicationDetails != null) {
            for (Object application : applicationDetails.getJSONArray(Constants.APPLICATIONS)) {
                JSONObject applicationJson = (JSONObject) application;
                JSONObject configs = applicationJson.getJSONObject(Constants.ADVANCED_CONFIGURATIONS);
                JSONArray spData = configs.getJSONArray(Constants.ADDITIONAL_SP_PROPERTIES);
                String logoUrl = null;
                for (Object spDataItem : spData) {
                    JSONObject spDataItemJson = (JSONObject) spDataItem;
                    if (spDataItemJson.getString(Constants.NAME).equals(Utils.getParameter(Constants.APP_LOGO_URL))) {
                        logoUrl = spDataItemJson.getString(Constants.VALUE);
                    }
                }
                appLogoUrlMap.put(applicationJson.getString(Constants.CLIENT_ID_CC), logoUrl);
            }
        }
        return appLogoUrlMap;
    }

}
