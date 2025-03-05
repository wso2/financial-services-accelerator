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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.filter.policy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.policy.FSPolicyExecutionException;
import org.wso2.financial.services.accelerator.common.policy.filter.FSFilterPolicy;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.filter.policy.utils.ConsentValidateFilterPolicyUtils;

import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * Consent status validation filter policy.
 */
public class ConsentStatusValidationFilterPolicy extends FSFilterPolicy {

    private static final Log log = LogFactory.getLog(ConsentStatusValidationFilterPolicy.class);

    @Override
    public void processRequest(ServletRequest servletRequest, Map<String, Object> propertyMap)
            throws FSPolicyExecutionException {

        try {
            JSONObject validatePayload = (JSONObject) servletRequest.getAttribute("decodedPayload");
            DetailedConsentResource consent = ConsentValidateFilterPolicyUtils.getConsentResource(servletRequest,
                    validatePayload);
            if (consent == null) {
                throw new FSPolicyExecutionException(HttpServletResponse.SC_NOT_FOUND,
                        "consent_not_found", "Consent not found");
            }

            if (propertyMap.containsKey("applicable_status")) {
                String applicableStatus = (String) propertyMap.get("applicable_status");
                if (applicableStatus != null && !applicableStatus.isEmpty()) {
                    if (!applicableStatus.equals(consent.getCurrentStatus())) {
                        log.error("Consent is not in the correct state");
                        throw new FSPolicyExecutionException(HttpServletResponse.SC_BAD_REQUEST,
                                "consent_status_invalid", "Consent is not in the correct state");
                    }
                }
            }
        } catch (ConsentManagementException e) {
            log.error(e.getMessage().replaceAll("[\n\r]", ""));
            throw new FSPolicyExecutionException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "consent_retrieval_failure", e.getMessage(), e);
        }
    }

    @Override
    public void processResponse(ServletResponse servletResponse, Map<String, Object> propertyMap)
            throws FSPolicyExecutionException {

    }
}
