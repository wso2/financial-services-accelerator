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

package com.wso2.openbanking.scp.webapp.servlet;

import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.scp.webapp.exception.TokenGenerationException;
import com.wso2.openbanking.scp.webapp.service.APIMService;
import com.wso2.openbanking.scp.webapp.util.Constants;
import com.wso2.openbanking.scp.webapp.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * IsReqHandlerServlet.
 * <p>
 * This interrupts the requests, adds auth header, and forward requests to IS.
 */
@WebServlet(name = "IsReqHandlerServlet", urlPatterns = {"/ca/admin"})
public class IsReqHandlerServlet extends HttpServlet {

    private static final long serialVersionUID = 7385252581004845448L;
    private static final Log LOG = LogFactory.getLog(IsReqHandlerServlet.class);
    private final APIMService apimService = new APIMService();

    @Generated(message = "Ignoring since all cases are covered from other unit tests")
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            LOG.debug("New request received: " + req.getRequestURI() + "?" + req.getQueryString());
            String sessionDataKeyConsent = req.getParameter("sessionDataKeyConsent");

            final String isBaseUrl = Utils.getParameter(Constants.IS_BASE_URL);
            HttpUriRequest request = Utils
                    .commonHttpUriRequest(isBaseUrl, Constants.PATH_IS_CONSENT_RETRIEVE, req.getMethod(), null,
                            sessionDataKeyConsent, null);

            // generating header
            Map<String, String> headers = new HashMap<>();
            headers.put(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
            headers.put(HttpHeaders.AUTHORIZATION, "Basic " + Utils.getConsentApiCredentials());

            apimService.forwardRequest(resp, request, headers);
        } catch (TokenGenerationException e) {
            LOG.error("Exception occurred while processing frontend request. Caused by, ", e);
        }

    }

    @Generated(message = "Ignoring since all cases are covered from other unit tests")
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            LOG.debug("New request received: " + req.getRequestURI() + "?" + req.getQueryString());
            String sessionDataKeyConsent = req.getParameter("sessionDataKeyConsent");

            HttpEntity postData = new StringEntity(Utils.getPayload(req), ContentType.APPLICATION_JSON);
            final String isBaseUrl = Utils.getParameter(Constants.IS_BASE_URL);
            HttpUriRequest request = Utils
                    .commonHttpUriRequest(isBaseUrl, Constants.PATH_IS_CONSENT_PERSIST, req.getMethod(), null,
                            sessionDataKeyConsent, postData);

            // generating header
            Map<String, String> headers = new HashMap<>();
            headers.put(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
            headers.put(HttpHeaders.AUTHORIZATION, "Basic " + Utils.getConsentApiCredentials());

            apimService.forwardRequest(resp, request, headers);
        } catch (TokenGenerationException | IOException e) {
            LOG.error("Exception occurred while processing frontend request. Caused by, ", e);
        }

    }

}
