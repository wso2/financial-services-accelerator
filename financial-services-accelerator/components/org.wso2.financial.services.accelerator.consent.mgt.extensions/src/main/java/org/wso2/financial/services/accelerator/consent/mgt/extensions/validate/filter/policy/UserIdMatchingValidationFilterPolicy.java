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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.policy.FSPolicyExecutionException;
import org.wso2.financial.services.accelerator.common.policy.filter.FSFilterPolicy;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.filter.policy.utils.ConsentValidateFilterPolicyUtils;

import java.util.ArrayList;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * Consent status validation filter policy.
 */
public class UserIdMatchingValidationFilterPolicy extends FSFilterPolicy {

    private static final Log log = LogFactory.getLog(UserIdMatchingValidationFilterPolicy.class);

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

            //User Validation
            String userIdFromToken = FinancialServicesUtils
                    .resolveUsernameFromUserId(validatePayload.getString("userId"));
            boolean userIdMatching = false;
            ArrayList<AuthorizationResource> authResources = consent.getAuthorizationResources();
            for (AuthorizationResource resource : authResources) {
                if (StringUtils.isNotEmpty(userIdFromToken) && userIdFromToken.equals(resource.getUserID())) {
                    userIdMatching = true;
                    break;
                }
            }

            if (!userIdMatching) {
                log.error("User bound to the token does not have access to the given consent");
                throw new FSPolicyExecutionException(HttpServletResponse.SC_BAD_REQUEST,
                        "consent_validation_failure",
                        "User bound to the token does not have access to the given consent");
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
