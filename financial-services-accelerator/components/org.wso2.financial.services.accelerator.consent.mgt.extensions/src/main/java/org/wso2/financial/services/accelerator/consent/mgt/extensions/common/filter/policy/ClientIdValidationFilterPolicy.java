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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.common.filter.policy;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.financial.services.accelerator.common.policy.FSPolicyExecutionException;
import org.wso2.financial.services.accelerator.common.policy.filter.FSFilterPolicy;

import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Client Id Validation Filter Policy.
 */
public class ClientIdValidationFilterPolicy extends FSFilterPolicy {

    private static final String CLIENT_ID_HEADER = "x-wso2-client-id";

    @Override
    @SuppressFBWarnings({"SERVLET_HEADER"})
    // Suppressed content - Endpoint
    // Suppression reason - False Positive : This endpoint is secured with access control lists in the configuration
    // Suppressed content - request.getHeader()
    // Suppression reason - False Positive : Header is properly validated to ensure no special characters are passed
    // Suppressed warning count - 1
    public void processRequest(ServletRequest servletRequest, Map<String, Object> propertyMap)
            throws FSPolicyExecutionException {

        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            String clientId = httpServletRequest.getHeader(CLIENT_ID_HEADER);
            if (clientId == null || clientId.isEmpty()) {
                throw new FSPolicyExecutionException(HttpServletResponse.SC_BAD_REQUEST, "invalid_header",
                        "Client ID missing in the request.");
            }

            validateClientId(clientId);
        }
    }

    @Override
    public void processResponse(ServletResponse servletResponse, Map<String, Object> propertyMap)
            throws FSPolicyExecutionException {

    }

    private void validateClientId(String clientId) throws FSPolicyExecutionException {

        if (StringUtils.isNotEmpty(clientId)) {
            Optional<ServiceProvider> serviceProvider;
            try {
                serviceProvider = Optional.ofNullable(OAuth2Util.getServiceProvider(clientId));
                if (!serviceProvider.isPresent()) {
                    throw new FSPolicyExecutionException(HttpServletResponse.SC_BAD_REQUEST, "invalid_header",
                            "Invalid mandatory parameter x-wso2-client-id.");
                }
            } catch (IdentityOAuth2Exception e) {
                throw new FSPolicyExecutionException(HttpServletResponse.SC_BAD_REQUEST, "invalid_header",
                        "Invalid mandatory parameter x-wso2-client-id.", e);
            }
        }
    }
}
