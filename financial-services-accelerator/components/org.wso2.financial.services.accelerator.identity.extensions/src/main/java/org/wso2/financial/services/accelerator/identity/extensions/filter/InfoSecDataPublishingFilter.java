/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.identity.extensions.filter;

import com.nimbusds.jwt.SignedJWT;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.data.publisher.constants.DataPublishingConstants;
import org.wso2.financial.services.accelerator.data.publisher.util.FSDataPublisherUtil;
import org.wso2.financial.services.accelerator.identity.extensions.filter.constants.FilterConstants;

import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Tomcat filter to publish data related to infoSec endpoints.
 * This filter should be added as the first filter of the filter chain
 * as the invocation latency data are calculated within this filter logic
 */
public class InfoSecDataPublishingFilter implements Filter {

    private static final Log LOG = LogFactory.getLog(InfoSecDataPublishingFilter.class);
    private final Map<String, Object> configMap = FinancialServicesConfigParser.getInstance().getConfiguration();
    private final String externalTrafficHeaderName = (String) configMap.get(FilterConstants
            .EXTERNAL_TRAFFIC_HEADER_NAME);
    private final String expectedExternalTrafficHeaderValue = (String) configMap.get(FilterConstants
            .EXTERNAL_TRAFFIC_EXPECTED_VALUE);

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {

            chain.doFilter(request, response);
            // Publish the reporting data before returning the response
            publishReportingData((HttpServletRequest) request, (HttpServletResponse) response);
        }
    }

    /**
     * Publish reporting data related to infoSec endpoints.
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     */
    public void publishReportingData(HttpServletRequest request, HttpServletResponse response) {

        if (Boolean.parseBoolean((String) FinancialServicesConfigParser.getInstance().getConfiguration()
                .get(DataPublishingConstants.DATA_PUBLISHING_ENABLED)) && shouldPublishCurrentRequestData(request)) {

            String messageId = UUID.randomUUID().toString();

            // publish api endpoint invocation data
            Map<String, Object> requestData = generateInvocationDataMap(request, response, messageId);
            FSDataPublisherUtil.publishData("API_DATA_STREAM", "INPUT_STREAM_VERSION", requestData);

        } else {
            LOG.debug("Data publishing is disabled or the request is not an external request. Infosec data " +
                    "publishing skipped.");
        }
    }

    /**
     * Create the APIInvocation data map.
     * @param request   HttpServletRequest
     * @param response  HttpServletResponse
     * @param messageId Unique Id for the request
     * @return requestData Map
     */
    @SuppressFBWarnings("SERVLET_HEADER")
    // Suppressed content - request.getHeader("authorization")
    // Suppression reason - False Positive : These endpoints are secured with access control
    // as defined in the IS deployment.toml file
    // Suppressed warning count - 1
    public Map<String, Object> generateInvocationDataMap(HttpServletRequest request, HttpServletResponse response,
                                                         String messageId) {

        Map<String, Object> requestData = new HashMap<>();
        String contentLength = response.getHeader(FilterConstants.CONTENT_LENGTH);
        String authHeader = request.getHeader("authorization");
        if (authHeader != null) {
            authHeader = authHeader.replaceAll("[\r\n]", "");
            String consentId = null;

            // Get consent id from the access token
            String token = authHeader.split(" ")[1];
            try {
                SignedJWT signedJWT = SignedJWT.parse(token);
                JSONObject jsonObject = signedJWT.getJWTClaimsSet().toJSONObject();
                consentId = (String) jsonObject.get("consent_id");
            } catch (ParseException e) {
                LOG.error("Error while parsing the JWT token", e);
            }
            requestData.put("consentId", consentId);
        }

        requestData.put("clientId", extractClientId(request));
        requestData.put("responseStatusCode", response.getStatus());
        requestData.put("httpMethod", request.getMethod());
        requestData.put("responsePayloadSize", contentLength != null ? Long.parseLong(contentLength) : 0);
        requestData.put("electedResource", request.getRequestURI());
        requestData.put("apiName", request.getContextPath());
        requestData.put("requestTimestamp", Instant.now().getEpochSecond());
        requestData.put("messageId", messageId);
        return requestData;
    }



    /**
     * Extracts the client id from the request parameter or from the assertion.
     *
     * @param request HttpServlet request containing the request data
     * @return clientId
     */
    @SuppressFBWarnings("SERVLET_PARAMETER")
    // Suppressed content - request.getHeader(FilterConstants.OAUTH_JWT_ASSERTION),request.
    // getHeader(FilterConstants.CLIENT_ID)
    // Suppression reason - False Positive : These endpoints are secured with access control
    // as defined in the IS deployment.toml file
    // Suppressed warning count - 2
    private String extractClientId(HttpServletRequest request) {
        String oauthJwtAssertion = request
                .getParameter(FilterConstants.OAUTH_JWT_ASSERTION).replaceAll("[\r\n]", "");
        String clientId = request
                .getParameter(FilterConstants.CLIENT_ID).replaceAll("[\r\n]", "");
        Optional<String> signedObject = Optional.ofNullable(oauthJwtAssertion);
        Optional<String> clientIdAsReqParam = Optional.ofNullable(clientId);
        if (signedObject.isPresent()) {
            SignedJWT signedJWT = null;
            try {
                signedJWT = SignedJWT.parse(signedObject.get());
                return signedJWT.getJWTClaimsSet().getIssuer();
            } catch (ParseException e) {
                LOG.error("Invalid assertion found in the request", e);
            }
        } else if (clientIdAsReqParam.isPresent()) {
            return clientIdAsReqParam.get();
        }
        return null;
    }

    @Override
    public void destroy() {
    }

    /**
     * Check whether data should be published for the current request.
     *
     * @return boolean
     */
    @SuppressFBWarnings("SERVLET_HEADER")
    // Suppressed content - request.getHeader(externalTrafficHeaderName)
    // Suppression reason - False Positive : These endpoints are secured with access control
    // as defined in the IS deployment.toml file
    // Suppressed warning count - 1
    public boolean shouldPublishCurrentRequestData(ServletRequest request) {

        // If the request is internal traffic, no need to publish data
        if (request instanceof HttpServletRequest) {
            return expectedExternalTrafficHeaderValue.equals(
                    ((HttpServletRequest) request).getHeader(externalTrafficHeaderName).replaceAll("[\r\n]",
                            ""));
        }
        return false;
    }
}
