/**
* Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
* <p>
* WSO2 LLC. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
* <p>
*     http://www.apache.org/licenses/LICENSE-2.0
* <p>
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.financial.services.accelerator.authentication.endpoint.util.AuthenticationUtils;
import org.wso2.financial.services.accelerator.authentication.endpoint.util.Constants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.util.ConsentAuthorizeConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authservlet.FSAuthServletInterface;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authservlet.impl.ConsentMgrAuthServletImpl;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authservlet.impl.ISDefaultAuthServletImpl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.wso2.financial.services.accelerator.consent.mgt.extensions.authservlet.utils.Utils.i18n;

/**
 * The servlet responsible for displaying the consent details in the auth UI
 * flow.
 */
public class FSConsentServlet extends HttpServlet {

    static FSAuthServletInterface fsAuthServletTK;
    private static final long serialVersionUID = 6106269076132678046L;
    private static Logger log = LoggerFactory.getLogger(FSConsentServlet.class);

    @SuppressFBWarnings({ "REQUESTDISPATCHER_FILE_DISCLOSURE", "TRUST_BOUNDARY_VIOLATION" })
    // Suppressed content - obAuthServlet.getJSPPath()
    // Suppression reason - False Positive : JSP path is hard coded and does not
    // accept any user inputs, therefore it
    // can be trusted
    // Suppressed content - Encode.forJava(sessionDataKey)
    // Suppression reason - False positive : sessionDataKey is encoded for Java
    // which escapes untrusted characters
    // Suppressed warning count - 2
    @Override
    public void doGet(HttpServletRequest originalRequest, HttpServletResponse response)
            throws IOException, ServletException {

        HttpServletRequest request = originalRequest;

        // get consent data
        String sessionDataKey = request.getParameter(Constants.SESSION_DATA_KEY_CONSENT);

        // validating session data key format
        try {
            UUID.fromString(sessionDataKey);
        } catch (IllegalArgumentException e) {
            log.error("Invalid session data key", e);
            request.getSession().invalidate();
            response.sendRedirect("retry.do?status=Error&statusMsg=Invalid session data key");
            return;
        }

        fsAuthServletTK = AuthenticationUtils.getAuthExtension();

        HttpResponse consentDataResponse = getConsentDataWithKey(sessionDataKey, getServletContext());
        JSONObject dataSet = new JSONObject();
        log.debug("HTTP response for consent retrieval" + consentDataResponse.toString());
        try {
            if (consentDataResponse.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_MOVED_TEMP &&
                    consentDataResponse.getLastHeader(Constants.LOCATION) != null) {
                response.sendRedirect(consentDataResponse.getLastHeader(Constants.LOCATION).getValue());
                return;
            } else {
                String retrievalResponse = IOUtils.toString(consentDataResponse.getEntity().getContent(),
                        String.valueOf(StandardCharsets.UTF_8));
                JSONObject data = new JSONObject(retrievalResponse);
                String errorResponse = AuthenticationUtils.getErrorResponseForRedirectURL(data);
                if (data.has(Constants.REDIRECT_URI) && StringUtils.isNotEmpty(errorResponse)) {
                    URI errorURI = new URI(data.get(Constants.REDIRECT_URI).toString().concat(errorResponse));
                    response.sendRedirect(errorURI.toString());
                    return;
                } else {
                    dataSet = createConsentDataset(data, consentDataResponse.getStatusLine().getStatusCode());
                }
            }
        } catch (IOException e) {
            log.error("Exception occurred while retrieving consent data", e);
            dataSet.put(Constants.IS_ERROR, "Exception occurred while retrieving consent data");
        } catch (URISyntaxException e) {
            log.error("Error while constructing URI for redirection", e);
            dataSet.put(Constants.IS_ERROR, "Error while constructing URI for redirection");
        } catch (JSONException e) {
            log.error("Error while parsing the response", e);
            dataSet.put(Constants.IS_ERROR, "Error while parsing the response");
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
        session.setAttribute(Constants.DISPLAY_SCOPES,
                Boolean.parseBoolean(getServletContext().getInitParameter(Constants.DISPLAY_SCOPES)));

        // set strings to request
        ResourceBundle resourceBundle = AuthenticationUtils.getResourceBundle(request.getLocale());

        originalRequest.setAttribute(Constants.PRIVACY_DESCRIPTION, i18n(resourceBundle,
                Constants.PRIVACY_DESCRIPTION_KEY));
        originalRequest.setAttribute(Constants.PRIVACY_GENERAL, i18n(resourceBundle, Constants.PRIVACY_GENERAL_KEY));

        // bottom.jsp
        originalRequest.setAttribute(Constants.OK, i18n(resourceBundle, Constants.OK));
        originalRequest.setAttribute(Constants.REQUESTED_SCOPES, i18n(resourceBundle, Constants.REQUESTED_SCOPES_KEY));

        originalRequest.setAttribute(Constants.APP, dataSet.getString(Constants.APPLICATION));

        // get auth servlet toolkit implementation

        // Get servlet extension
        FSAuthServletInterface fsAuthServlet;
        if (Constants.IS_DEFAULT.equals(dataSet.getString(Constants.TYPE))) {
            // get default auth servlet extension
            fsAuthServlet = new ISDefaultAuthServletImpl();
        } else if (Constants.CONSENT_MGT.equals(dataSet.getString(Constants.TYPE))) {
            // get consent manager auth servlet extension
            fsAuthServlet = new ConsentMgrAuthServletImpl();
        } else {
            // get auth servlet toolkit implementation
            if (fsAuthServletTK == null) {
                request.getSession().invalidate();
                response.sendRedirect("retry.do?status=Error&statusMsg=Error while processing request");
                log.error("Unable to find FS auth servlet extension implementation. Returning error.");
                return;
            }
            fsAuthServlet = fsAuthServletTK;
        }

        Map<String, Object> updatedValues;

        updatedValues = fsAuthServlet.updateRequestAttribute(request, dataSet, resourceBundle);
        updatedValues.forEach(originalRequest::setAttribute);

        // update session
        updatedValues = fsAuthServlet.updateSessionAttribute(request, dataSet, resourceBundle);
        updatedValues.forEach(originalRequest.getSession()::setAttribute);

        // If the account selection is to be handled separately, forward to the account selection JSP
        Object consentDataObj = dataSet.get(ConsentAuthorizeConstants.CONSENT_DATA);
        if (consentDataObj instanceof JSONObject) {
            JSONObject consentData = (JSONObject) consentDataObj;
            if (consentData.has(ConsentAuthorizeConstants.HANDLE_ACCOUNT_SELECTION_SEPARATELY)) {
                boolean handleAccountSelectionSeparately = consentData
                        .optBoolean(ConsentAuthorizeConstants.HANDLE_ACCOUNT_SELECTION_SEPARATELY, false);

                if (handleAccountSelectionSeparately) {
                    log.debug("Handling account selection separately, forwarding to account selection page");

                    // dispatch
                    RequestDispatcher dispatcher = this.getServletContext()
                            .getRequestDispatcher("/fs_default_account_selection.jsp");
                    dispatcher.forward(originalRequest, response);
                    return;
                } else {
                    log.debug("Handling consent in default flow, forwarding to default consent page");

                    // dispatch
                    RequestDispatcher dispatcher = this.getServletContext()
                            .getRequestDispatcher("/fs_default.jsp");
                    dispatcher.forward(originalRequest, response);
                    return;
                }
            }
        }

        // dispatch
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher(fsAuthServlet.getJSPPath());
        dispatcher.forward(originalRequest, response);
    }

    /**
     * Retrieve consent data with the session data key.
     * 
     * @param sessionDataKeyConsent session data key
     * @param servletContext        servlet context
     * @return HTTP response
     * @throws IOException if an error occurs while retrieving consent data
     */
    HttpResponse getConsentDataWithKey(String sessionDataKeyConsent, ServletContext servletContext) throws IOException {

        String retrievalBaseURL = servletContext.getInitParameter(Constants.RETRIEVAL_BASE_URL);
        String retrieveUrl = (retrievalBaseURL.endsWith(Constants.SLASH)) ? retrievalBaseURL + sessionDataKeyConsent
                : retrievalBaseURL + Constants.SLASH + sessionDataKeyConsent;

        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpGet dataRequest = new HttpGet(retrieveUrl);
        dataRequest.addHeader(Constants.AUTHORIZATION, Constants.BASIC +
                AuthenticationUtils.getConsentApiCredentials());

        return client.execute(dataRequest);

    }

    /**
     * Create consent data from the response of the consent retrieval.
     * 
     * @param consentResponse consent response from retrieval
     * @param statusCode      status code of the response
     * @return consent data JSON object
     * @throws IOException if an error occurs while creating the consent data
     */
    JSONObject createConsentDataset(JSONObject consentResponse, int statusCode) throws IOException {

        JSONObject errorObject = new JSONObject();
        if (statusCode != HttpURLConnection.HTTP_OK) {
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                if (consentResponse.has(Constants.DESCRIPTION)) {
                    errorObject.put(Constants.IS_ERROR, consentResponse.get(Constants.DESCRIPTION));
                }
            } else {
                errorObject.put(Constants.IS_ERROR, "Retrieving consent data failed");
            }
            return errorObject;
        } else {
            return consentResponse;
        }
    }
}
