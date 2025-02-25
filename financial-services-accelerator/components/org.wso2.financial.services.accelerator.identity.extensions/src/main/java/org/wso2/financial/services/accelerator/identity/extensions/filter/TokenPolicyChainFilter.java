/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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
package org.wso2.financial.services.accelerator.identity.extensions.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesYamlConfigParser;
import org.wso2.financial.services.accelerator.identity.extensions.policy.FinancialServicesPolicy;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Filter to engage financial services policy chain for the token endpoint.
 */
public class TokenPolicyChainFilter implements Filter {

    private static final Log log = LogFactory.getLog(TokenPolicyChainFilter.class);
    private final List<FinancialServicesPolicy> requestFlowPolicies = new ArrayList<>();
    private final List<FinancialServicesPolicy> executionFlowPolicies = new ArrayList<>();
    private final List<FinancialServicesPolicy> responseFlowPolicies = new ArrayList<>();

    public void init(FilterConfig filterConfig) {
        Map<String, Object> configMap = FinancialServicesYamlConfigParser.parseConfig();
        initPolicyClasses(configMap);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        log.info("Engaging policy chain for the token endpoint request");
        for (FinancialServicesPolicy policy : requestFlowPolicies) {
            policy.processRequest(servletRequest, policy.getPropertyMap());
        }

        filterChain.doFilter(servletRequest, servletResponse);

        log.info("Engaging policy chain for the token endpoint response");
        for (FinancialServicesPolicy policy : responseFlowPolicies) {
            policy.processResponse(servletResponse, policy.getPropertyMap());
        }
    }

    private void initPolicyClasses(Map<String, Object> configMap) {

        List<Map<String, Object>> apis = (List<Map<String, Object>>) configMap.get("apis");
        for (Map<String, Object> api : apis) {
            if ("TokenAPI".equals(api.get("name"))) {
                Map<String, Object> paths = (Map<String, Object>) api.get("paths");
                Map<String, Object> tokenPath = (Map<String, Object>) paths.get("/token");

                if (tokenPath != null) {
                    try {
                        extractPolicyClasses(tokenPath, "request-flow", requestFlowPolicies);
                        extractPolicyClasses(tokenPath, "execution-flow", executionFlowPolicies);
                        extractPolicyClasses(tokenPath, "response-flow", responseFlowPolicies);
                    } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
                             IllegalAccessException | InvocationTargetException e) {
                        log.error("Error while extracting policy classes", e);
                    }
                }
            }
        }
    }

    private static void extractPolicyClasses(Map<String, Object> tokenPath, String flowType,
                                             List<FinancialServicesPolicy> policiesList) throws ClassNotFoundException,
            NoSuchMethodException,
            InvocationTargetException, InstantiationException, IllegalAccessException {

        Map<String, Object> flow = (Map<String, Object>) tokenPath.get("post");
        if (flow != null) {
            Map<String, Object> specificFlow = (Map<String, Object>) flow.get(flowType);
            if (specificFlow != null) {
                List<Map<String, Object>> policies = (List<Map<String, Object>>) specificFlow.get("policies");
                if (policies != null) {
                    for (Map<String, Object> policy : policies) {
                        Map<String, Object> parameters = (Map<String, Object>) policy.get("parameters");
                        String className = (String) policy.get("class");
                        if (parameters != null && className != null) {
                            Class<?> policyClass = Class.forName(className);
                            Constructor<?> constructor = policyClass.getConstructor();
                            FinancialServicesPolicy policyInstance = (FinancialServicesPolicy)
                                    constructor.newInstance();
                            policyInstance.setPropertyMap(parameters);
                            policiesList.add(policyInstance);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void destroy() {

    }
}
