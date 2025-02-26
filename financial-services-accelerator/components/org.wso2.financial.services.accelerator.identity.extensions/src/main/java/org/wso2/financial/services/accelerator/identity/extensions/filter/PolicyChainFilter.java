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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesYamlConfigParser;
import org.wso2.financial.services.accelerator.common.policy.FSPolicy;
import org.wso2.financial.services.accelerator.identity.extensions.filter.policy.FSFilterPolicy;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Filter to engage financial services policy chain.
 */
public class PolicyChainFilter implements Filter {

    private static final Log log = LogFactory.getLog(PolicyChainFilter.class);
    private String apiName;

    public void init(FilterConfig filterConfig) {
        apiName = filterConfig.getInitParameter("apiName");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        String path = ((HttpServletRequest) servletRequest).getRequestURI();
        String operation = ((HttpServletRequest) servletRequest).getMethod();

        log.info("Engaging policy chain for the token endpoint request");
        List<FSPolicy> requestPolicies = FinancialServicesYamlConfigParser
                .getPolicies(apiName, path, operation, "request-flow");
        for (FSPolicy policy : requestPolicies) {
            FSFilterPolicy filterPolicy = (FSFilterPolicy) policy;
            filterPolicy.processRequest(servletRequest, policy.getPropertyMap());
        }

        filterChain.doFilter(servletRequest, servletResponse);

        log.info("Engaging policy chain for the token endpoint response");
        List<FSPolicy> responsePolicies = FinancialServicesYamlConfigParser
                .getPolicies(apiName, path, operation, "response-flow");
        for (FSPolicy policy : responsePolicies) {
            FSFilterPolicy filterPolicy = (FSFilterPolicy) policy;
            filterPolicy.processResponse(servletResponse, policy.getPropertyMap());
        }
    }

    @Override
    public void destroy() {

    }
}
