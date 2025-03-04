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

package org.wso2.financial.services.accelerator.common.policy.filter.chain;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesYamlConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.policy.FSPolicy;
import org.wso2.financial.services.accelerator.common.policy.FSPolicyExecutionException;
import org.wso2.financial.services.accelerator.common.policy.FilterPolicyRequestWrapper;
import org.wso2.financial.services.accelerator.common.policy.filter.FSFilterPolicy;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

/**
 * Filter to engage financial services filter policy chain to consent APIs.
 */
public class ConsentPolicyChainFilter implements Filter {

    private static final Log log = LogFactory.getLog(ConsentPolicyChainFilter.class);
    private String apiName;

    public void init(FilterConfig filterConfig) {
        apiName = filterConfig.getInitParameter("apiName");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        if (servletRequest instanceof HttpServletRequest) {
            String path = ((HttpServletRequest) servletRequest).getRequestURI();
            String operation = ((HttpServletRequest) servletRequest).getMethod();
            FilterPolicyRequestWrapper cachedRequest =
                    new FilterPolicyRequestWrapper((HttpServletRequest) servletRequest);

            log.info("Engaging policy chain for the consent endpoint request");
            List<FSPolicy> requestPolicies = FinancialServicesYamlConfigParser
                    .getPolicies(apiName, path, operation, "request-flow");

            try {
                for (FSPolicy policy : requestPolicies) {
                    FSFilterPolicy filterPolicy = (FSFilterPolicy) policy;
                    filterPolicy.processRequest(cachedRequest, policy.getPropertyMap());
                }
            } catch (FSPolicyExecutionException e) {
                log.error("Error occurred while processing consent request policies.", e);
                handleValidationFailure((HttpServletResponse) servletResponse, e.getStatusCode(), e.getErrorCode(),
                        e.getMessage());
                return;
            }

            filterChain.doFilter(cachedRequest, servletResponse);

            log.info("Engaging policy chain for the consent endpoint response");
            List<FSPolicy> responsePolicies = FinancialServicesYamlConfigParser
                    .getPolicies(apiName, path, operation, "response-flow");
            try {
                for (FSPolicy policy : responsePolicies) {
                    FSFilterPolicy filterPolicy = (FSFilterPolicy) policy;
                    filterPolicy.processResponse(servletResponse, policy.getPropertyMap());
                }
            } catch (FSPolicyExecutionException e) {
                log.error("Error occurred while processing consent response policies.", e);
                handleValidationFailure((HttpServletResponse) servletResponse, e.getStatusCode(), e.getErrorCode(),
                        e.getMessage());
                return;
            }
        }
    }

    @Override
    public void destroy() {

    }

    /**
     * Respond when there is a failure in filter validation.
     *
     * @param response     HTTP servlet response object
     * @param status       HTTP status code
     * @param error        error
     * @param errorMessage error description
     * @throws IOException if an error occurs while writing to the output stream
     */
    protected void handleValidationFailure(HttpServletResponse response, int status, String error, String errorMessage)
            throws IOException {

        JSONObject errorJSON = new JSONObject();
        errorJSON.put(FinancialServicesConstants.OAUTH_ERROR, error);
        errorJSON.put(FinancialServicesConstants.OAUTH_ERROR_DESCRIPTION, errorMessage);

        try (OutputStream outputStream = response.getOutputStream()) {
            response.setStatus(status);
            response.setContentType(MediaType.APPLICATION_JSON);
            outputStream.write(errorJSON.toString().getBytes(StandardCharsets.UTF_8));
        }
    }
}
