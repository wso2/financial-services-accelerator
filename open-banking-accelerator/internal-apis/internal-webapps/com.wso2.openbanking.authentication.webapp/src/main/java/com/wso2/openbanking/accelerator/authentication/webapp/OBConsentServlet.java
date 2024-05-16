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

package com.wso2.openbanking.accelerator.authentication.webapp;

import com.wso2.openbanking.accelerator.authentication.webapp.util.Constants;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.consent.extensions.authservlet.impl.ConsentMgrAuthServletImpl;
import com.wso2.openbanking.accelerator.consent.extensions.authservlet.impl.ISDefaultAuthServletImpl;
import com.wso2.openbanking.accelerator.consent.extensions.authservlet.model.OBAuthServletInterface;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.wso2.openbanking.accelerator.consent.extensions.authservlet.impl.util.Utils.i18n;

/**
 * The servlet responsible for displaying the consent details in the auth UI flow.
 */
public class OBConsentServlet extends HttpServlet {

    static OBAuthServletInterface obAuthServletTK;
    private static final long serialVersionUID = 6106269076132678046L;
    private static Logger log = LoggerFactory.getLogger(OBConsentServlet.class);
    private static final String BUNDLE = "com.wso2.openbanking.authentication.webapp.i18n";

    @SuppressFBWarnings({"REQUESTDISPATCHER_FILE_DISCLOSURE", "TRUST_BOUNDARY_VIOLATION"})
    // Suppressed content - obAuthServlet.getJSPPath()
    // Suppression reason - False Positive : JSP path is hard coded and does not accept any user inputs, therefore it
    //                      can be trusted
    // Suppressed content - Encode.forJava(sessionDataKey)
    // Suppression reason - False positive : sessionDataKey is encoded for Java which escapes untrusted characters
    // Suppressed warning count - 2
    @Override
    public void doGet(HttpServletRequest originalRequest, HttpServletResponse response)
            throws IOException, ServletException {
        HttpServletRequest request = originalRequest;
        setAuthExtension();

        // get consent data
        String sessionDataKey = request.getParameter("sessionDataKeyConsent");
        HttpResponse consentDataResponse = getConsentDataWithKey(sessionDataKey, getServletContext());
        JSONObject dataSet = new JSONObject();
        log.debug("HTTP response for consent retrieval" + consentDataResponse.toString());
        try {
            if (consentDataResponse.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_MOVED_TEMP &&
                    consentDataResponse.getLastHeader("Location") != null) {
                response.sendRedirect(consentDataResponse.getLastHeader("Location").getValue());
                return;
            } else {
                String retrievalResponse = IOUtils.toString(consentDataResponse.getEntity().getContent(),
                        String.valueOf(StandardCharsets.UTF_8));
                JSONObject data = new JSONObject(retrievalResponse);
                String errorResponse = getErrorResponseForRedirectURL(data);
                if (data.has(Constants.REDIRECT_URI) && StringUtils.isNotEmpty(errorResponse)) {
                    URI errorURI = new URI(data.get(Constants.REDIRECT_URI).toString().concat(errorResponse));
                    response.sendRedirect(errorURI.toString());
                    return;
                } else {
                    dataSet = createConsentDataset(data, consentDataResponse.getStatusLine().getStatusCode());
                }
            }
        } catch (IOException e) {
            dataSet.put(Constants.IS_ERROR, "Exception occurred while retrieving consent data");
        } catch (URISyntaxException e) {
            dataSet.put(Constants.IS_ERROR, "Error while constructing URI for redirection");

        }
        if (dataSet.has(Constants.IS_ERROR)) {
            String isError = (String) dataSet.get(Constants.IS_ERROR);
            request.getSession().invalidate();
            response.sendRedirect("retry.do?status=Error&statusMsg=" + isError);
            return;
        }

        // set variables to session
        HttpSession session = request.getSession();

        session.setAttribute(Constants.SESSION_DATA_KEY_CONSENT, Encode.forJava(sessionDataKey));
        session.setAttribute("displayScopes",
                Boolean.parseBoolean(getServletContext().getInitParameter("displayScopes")));

        // set strings to request
        ResourceBundle resourceBundle = getResourceBundle(request.getLocale());

        originalRequest.setAttribute("privacyDescription", i18n(resourceBundle,
                "privacy.policy.privacy.short.description.approving"));
        originalRequest.setAttribute("privacyGeneral", i18n(resourceBundle, "privacy.policy.general"));

        // bottom.jsp
        originalRequest.setAttribute("ok", i18n(resourceBundle, "ok"));
        originalRequest.setAttribute("requestedScopes", i18n(resourceBundle, "requested.scopes"));

        originalRequest.setAttribute("app", dataSet.getString("application"));

        // Get servlet extension
        OBAuthServletInterface obAuthServlet;
        if (Constants.DEFAULT.equals(dataSet.getString("type"))) {
            // get default auth servlet extension
            obAuthServlet = new ISDefaultAuthServletImpl();
        } else if (Constants.CONSENT_MGT.equals(dataSet.getString("type"))) {
            // get consent manager auth servlet extension
            obAuthServlet = new ConsentMgrAuthServletImpl();
        } else {
            // get auth servlet toolkit implementation
            if (obAuthServletTK == null) {
                request.getSession().invalidate();
                response.sendRedirect("retry.do?status=Error&statusMsg=Error while processing request");
                log.error("Unable to find OB auth servlet extension implementation. Returning error.");
                return;
            }
            obAuthServlet = obAuthServletTK;
        }

        Map<String, Object> updatedValues;

        updatedValues = obAuthServlet.updateRequestAttribute(request, dataSet, resourceBundle);
        updatedValues.forEach(originalRequest::setAttribute);

        // update session
        updatedValues = obAuthServlet.updateSessionAttribute(request, dataSet, resourceBundle);
        updatedValues.forEach(originalRequest.getSession()::setAttribute);

        // dispatch
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher(obAuthServlet.getJSPPath());
        dispatcher.forward(originalRequest, response);

    }

    HttpResponse getConsentDataWithKey(String sessionDataKeyConsent, ServletContext servletContext) throws IOException {

        String retrievalBaseURL = servletContext.getInitParameter("retrievalBaseURL");
        String retrieveUrl = (retrievalBaseURL.endsWith("/")) ? retrievalBaseURL + sessionDataKeyConsent :
                retrievalBaseURL + "/" + sessionDataKeyConsent;

        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpGet dataRequest = new HttpGet(retrieveUrl);
        dataRequest.addHeader("Authorization", "Basic " + getConsentApiCredentials());
        HttpResponse dataResponse = client.execute(dataRequest);

        return dataResponse;

    }

    JSONObject createConsentDataset(JSONObject consentResponse, int statusCode) throws IOException {

        JSONObject errorObject = new JSONObject();
        if (statusCode != HttpURLConnection.HTTP_OK) {
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                if (consentResponse.has("description")) {
                    errorObject.put(Constants.IS_ERROR, consentResponse.get("description"));
                }
            } else {
                errorObject.put(Constants.IS_ERROR, "Retrieving consent data failed");
            }
            return errorObject;
        } else {
            return consentResponse;
        }
    }

    /**
     * Retrieve the config.
     */
    void setAuthExtension() {

        try {
            obAuthServletTK = (OBAuthServletInterface) Class.forName(OpenBankingConfigParser.getInstance().
                    getAuthServletExtension()).getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException |
                InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            log.error("Webapp extension not found", e);
        }
    }

    @Generated(message = "Encapsulated method for unit test")
    ResourceBundle getResourceBundle(Locale locale) {

        return ResourceBundle.getBundle(BUNDLE, locale);
    }

    /**
     * @param data error response received from consent data retrieval endpoint
     * @return formatted error response to be send to call back uri
     */
    String getErrorResponseForRedirectURL(JSONObject data) {

        String errorResponse = "";
        try {
            if (data.has(Constants.ERROR)) {
                errorResponse = errorResponse.concat(Constants.ERROR_URI_FRAGMENT)
                        .concat(URLEncoder.encode(data.get(Constants.ERROR).toString(),
                                StandardCharsets.UTF_8.toString()));
            }
            if (data.has(Constants.ERROR_DESCRIPTION)) {
                errorResponse = errorResponse.concat(Constants.ERROR_DESCRIPTION_PARAMETER)
                        .concat(URLEncoder.encode(data.get(Constants.ERROR_DESCRIPTION).toString(),
                                StandardCharsets.UTF_8.toString()));
            }
            if (data.has(Constants.STATE)) {
                errorResponse = errorResponse.concat(Constants.STATE_PARAMETER)
                        .concat(URLEncoder.encode(data.get(Constants.STATE).toString(),
                                StandardCharsets.UTF_8.toString()));
            }

        } catch (UnsupportedEncodingException e) {
            log.error("Error while building error response", e);
        }
        return errorResponse;
    }

    /**
     * Retrieve admin credentials in Base64 format from webapp properties or OB configs.
     */
    static String getConsentApiCredentials () {
        String username, password;
        try {
            InputStream configurations = OBConsentConfirmServlet.class.getClassLoader()
                    .getResourceAsStream(Constants.CONFIG_FILE_NAME);
            Properties configurationProperties = new Properties();
            configurationProperties.load(configurations);
            Boolean isConfiguredInWebapp = Boolean.parseBoolean(
                    configurationProperties.getProperty(Constants.LOCATION_OF_CREDENTIALS));
            if (!isConfiguredInWebapp) {
                username = (String) OpenBankingConfigParser.getInstance().getConfiguration()
                        .get(Constants.USERNAME_IN_OB_CONFIGS);
                password = (String) OpenBankingConfigParser.getInstance().getConfiguration()
                        .get(Constants.PASSWORD_IN_OB_CONFIGS);
            } else {
                username = configurationProperties.getProperty(Constants.USERNAME_IN_WEBAPP_CONFIGS);
                password = configurationProperties.getProperty(Constants.PASSWORD_IN_WEBAPP_CONFIGS);
            }
        } catch (IOException | NullPointerException e) {
            log.error("Error occurred while reading the webapp properties file. Therefore using OB configurations.");
            username = (String) OpenBankingConfigParser.getInstance().getConfiguration()
                    .get(Constants.USERNAME_IN_OB_CONFIGS);
            password = (String) OpenBankingConfigParser.getInstance().getConfiguration()
                    .get(Constants.PASSWORD_IN_OB_CONFIGS);
        }
        return Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
    }

}
