/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.authentication.endpoint;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.financial.services.accelerator.authentication.endpoint.util.AuthenticationUtils;
import org.wso2.financial.services.accelerator.authentication.endpoint.util.Constants;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authservlet.FSAuthServletInterface;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The servlet responsible for the confirm page in auth web flow.
 */
public class FSConsentConfirmServlet extends HttpServlet {

    static FSAuthServletInterface fsAuthServletTK;
    private static final long serialVersionUID = 6106269597832678046L;
    private static Logger log = LoggerFactory.getLogger(FSConsentConfirmServlet.class);

    @SuppressFBWarnings("COOKIE_USAGE")
    // Suppressed content - browserCookies.put(cookie.getName(), cookie.getValue())
    // Suppression reason - False Positive : The cookie values are only read and
    // here. No sensitive info is added to
    // the cookie in this step.
    // Suppressed warning count - 1
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        fsAuthServletTK = AuthenticationUtils.getAuthExtension();

        HttpSession session = request.getSession();
        Map<String, String> metadata = new HashMap<>();
        Map<String, String> browserCookies = new HashMap<>();
        JSONObject consentData = new JSONObject();

        // retrieve commonAuthId to be stored for co-relation of consent Id and access
        // token issued
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            browserCookies.put(cookie.getName(), cookie.getValue());
        }
        consentData.put(Constants.COOKIES, browserCookies);

        // Add authorisationId if available
        String authorisationId = request.getParameter(Constants.AUTH_ID);
        if (StringUtils.isNotEmpty(authorisationId)) {
            metadata.put(Constants.AUTH_ID, authorisationId);
        }

        consentData.put(Constants.TYPE, request.getParameter(Constants.TYPE));
        consentData.put(Constants.APPROVAL, request.getParameter(Constants.CONSENT));
        consentData.put(Constants.USER_ID, session.getAttribute(Constants.USER_NAME));

        // add TK data
        if (fsAuthServletTK != null) {
            Map<String, String> updatedMetadata = fsAuthServletTK.updateConsentMetaData(request);
            if (updatedMetadata != null) {
                metadata.putAll(updatedMetadata);
            }

            Map<String, Object> updatedConsentData = fsAuthServletTK.updateConsentData(request);
            if (updatedConsentData != null) {
                updatedConsentData.forEach(consentData::put);
            }
        }

        consentData.put(Constants.METADATA, metadata);

        String sessionDataKey = request.getParameter(Constants.SESSION_DATA_KEY_CONSENT);

        // validating session data key format
        try {
            UUID.fromString(sessionDataKey);
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID", e);
            session.invalidate();
            response.sendRedirect("retry.do?status=Error&statusMsg=Invalid UUID");
            return;
        }

        String redirectURL = persistConsentData(
                consentData, sessionDataKey, getServletContext());

        // Invoke authorize flow
        if (redirectURL != null) {
            response.sendRedirect(redirectURL);

        } else {
            session.invalidate();
            response.sendRedirect("retry.do?status=Error&statusMsg=Error while persisting consent");
        }

    }

    @Generated(message = "Contains the tested code of HTTPClient")
    String persistConsentData(JSONObject consentData, String sessionDataKey, ServletContext servletContext) {

        String persistenceBaseURL = servletContext.getInitParameter(Constants.PERSISTENCE_BASE_URL);
        String persistenceUrl = persistenceBaseURL + Constants.SLASH + sessionDataKey;

        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpPatch dataRequest = new HttpPatch(persistenceUrl);
            dataRequest.addHeader(Constants.ACCEPT, Constants.JSON);
            dataRequest.addHeader(Constants.AUTHORIZATION, Constants.BASIC +
                    AuthenticationUtils.getConsentApiCredentials());
            StringEntity body = new StringEntity(consentData.toString(), ContentType.APPLICATION_JSON);
            dataRequest.setEntity(body);
            HttpResponse dataResponse = client.execute(dataRequest);

            if (dataResponse.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_MOVED_TEMP &&
                    dataResponse.getLastHeader(Constants.LOCATION) != null) {
                return dataResponse.getLastHeader(Constants.LOCATION).getValue();
            } else {
                String retrievalResponse = IOUtils.toString(dataResponse.getEntity().getContent(),
                        String.valueOf(StandardCharsets.UTF_8));
                JSONObject data = new JSONObject(retrievalResponse);
                String errorResponse = AuthenticationUtils.getErrorResponseForRedirectURL(data);
                if (data.has(Constants.REDIRECT_URI) && StringUtils.isNotEmpty(errorResponse)) {
                    URI errorURI = new URI(data.get(Constants.REDIRECT_URI).toString().concat(errorResponse));
                    return errorURI.toString();
                } else {
                    return null;
                }
            }
        } catch (IOException | JSONException | URISyntaxException e) {
            log.error("Exception while calling persistence endpoint", e);
            return null;
        }
    }
}
