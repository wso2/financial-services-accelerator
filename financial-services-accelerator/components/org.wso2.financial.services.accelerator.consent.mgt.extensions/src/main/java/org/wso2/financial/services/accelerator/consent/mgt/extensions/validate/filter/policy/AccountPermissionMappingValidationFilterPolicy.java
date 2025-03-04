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
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.common.policy.FSPolicyExecutionException;
import org.wso2.financial.services.accelerator.common.policy.filter.FSFilterPolicy;
import org.wso2.financial.services.accelerator.common.policy.utils.FilterPolicyUtils;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.filter.policy.utils.ConsentValidateFilterPolicyUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * Account Permission validation filter policy.
 * Validate whether the consent has the required permission to access the requested resource
 */
public class AccountPermissionMappingValidationFilterPolicy extends FSFilterPolicy {

    private static final Log log = LogFactory.getLog(AccountPermissionMappingValidationFilterPolicy.class);

    @Override
    public void processRequest(ServletRequest servletRequest, Map<String, Object> propertyMap)
            throws FSPolicyExecutionException {

        try {
            JSONObject validatePayload = (JSONObject) servletRequest.getAttribute("decodedPayload");
            String electedResource = validatePayload.getString("electedResource");
            DetailedConsentResource consent = ConsentValidateFilterPolicyUtils.getConsentResource(servletRequest,
                    validatePayload);
            Map<String, String> resourcePermissionMappings = (Map<String, String>)
                    propertyMap.get("resource_permission_mappings");
            String permissionPath = (String) propertyMap.get("path_to_permissions");
            JSONObject consentReceipt = new JSONObject(consent.getReceipt());
            AtomicBoolean hasAllowedPermissions = new AtomicBoolean(true);
            if (FilterPolicyUtils.pathExists(consentReceipt, permissionPath)) {
                JSONArray permissions = (JSONArray) FilterPolicyUtils.retrieveValueFromJSONObject(consentReceipt,
                        permissionPath);
                resourcePermissionMappings.forEach((resource, allowedPermissions) -> {
                    String[] resourcePaths = resource.split("/|");
                    String[] allowedPermissionsList = allowedPermissions.split("/|");
                    if (Arrays.stream(resourcePaths).anyMatch(electedResource::matches)) {
                        for (String permission : allowedPermissionsList) {
                            if (!permissions.toList().contains(permission)) {
                                hasAllowedPermissions.set(false);
                                break;
                            }
                        }
                    }
                });
            }
            if (!hasAllowedPermissions.get()) {
                throw new FSPolicyExecutionException(HttpServletResponse.SC_FORBIDDEN, "forbidden",
                        "Consent does not have the required permissions to access the requested resource");
            }
        } catch (ConsentManagementException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void processResponse(ServletResponse servletResponse, Map<String, Object> propertyMap)
            throws FSPolicyExecutionException {

    }
}
