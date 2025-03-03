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

package org.wso2.financial.services.accelerator.common.policy.filter.common;

import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.policy.FSPolicyExecutionException;
import org.wso2.financial.services.accelerator.common.policy.filter.FSFilterPolicy;
import org.wso2.financial.services.accelerator.common.policy.utils.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.policy.utils.FilterPolicyUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * External Validation Filter Policy.
 */
public class ExternalValidationFilterPolicy extends FSFilterPolicy {

    @Override
    public void processRequest(ServletRequest servletRequest, Map<String, Object> propertyMap)
            throws FSPolicyExecutionException {

        if (servletRequest instanceof HttpServletRequest) {
            try {
                String requestPayload = FilterPolicyUtils.getStringPayload((HttpServletRequest) servletRequest);
                JSONObject requestPayloadObj = new JSONObject(requestPayload);

                ExternalServiceRequest externalServiceRequest = getExternalServiceRequest(requestPayloadObj,
                        propertyMap.get("service_type").toString());

                String externalServicesPayload = (new JSONObject(externalServiceRequest)).toString();

                JSONObject externalServiceResponse = FilterPolicyUtils.invokeExternalServiceCall(propertyMap,
                        externalServicesPayload);

                servletRequest.setAttribute("externalServiceResponse", externalServiceResponse.toString());
            } catch (FinancialServicesException e) {
                throw new FSPolicyExecutionException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "internal_server_error", "Error occurred while processing the request", e);
            }
        }

    }

    @Override
    public void processResponse(ServletResponse servletResponse, Map<String, Object> propertyMap)
            throws FSPolicyExecutionException {

    }

    private ExternalServiceRequest getExternalServiceRequest(JSONObject requestPayloadObj, String serviceType)
            throws FSPolicyExecutionException {

        switch (serviceType) {
            case "consent":
                return getExternalServiceRequestForConsent(requestPayloadObj);
            default:
                throw new FSPolicyExecutionException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "internal_server_error", "Invalid service type");
        }
    }

    private ExternalServiceRequest getExternalServiceRequestForConsent(JSONObject requestPayloadObj) {

        ExternalServiceRequest.EventRequest eventRequest =
                new ExternalServiceRequest.EventRequest(requestPayloadObj, new ArrayList<>(), new ArrayList<>());
        ExternalServiceRequest.Event event = new ExternalServiceRequest.Event(eventRequest);

        return new ExternalServiceRequest(UUID.randomUUID().toString(), event, "validate");
    }
}
