/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.policy.FSPolicyExecutionException;
import org.wso2.financial.services.accelerator.common.policy.filter.FSFilterPolicy;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.filter.policy.utils.ConsentFilterPolicyUtils;


import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Future Date Validation Policy.
 */
public class FutureDateValidationFilterPolicy extends FSFilterPolicy {

    private static final Log log = LogFactory.getLog(FutureDateValidationFilterPolicy.class);

    @Override
    public void processRequest(ServletRequest servletRequest, Map<String, Object> propertyMap)
            throws FSPolicyExecutionException {

        try {
            String payload = ConsentFilterPolicyUtils.getStringPayload((HttpServletRequest) servletRequest);
            JSONObject payloadObj = new JSONObject(payload);

            Object applicableParams = propertyMap.get("applicable_params");

            if (applicableParams instanceof String) {
                String applicableParam = (String) applicableParams;
                if (ConsentExtensionUtils.pathExists(payloadObj, applicableParam)) {
                    String dateVal = (String) ConsentExtensionUtils.retrieveValueFromJSONObject(payloadObj,
                            applicableParam);
                    if (!isFutureDate(dateVal)) {
                        String errorMsg = String.format("The %S value has to be a future date", applicableParam);
                        throw new FSPolicyExecutionException(HttpServletResponse.SC_BAD_REQUEST, "invalid_date",
                                errorMsg);
                    }
                }
            } else if (applicableParams instanceof List) {
                List<String> applicableParamList = (List<String>) applicableParams;
                for (String applicableParam : applicableParamList) {
                    if (ConsentExtensionUtils.pathExists(payloadObj, applicableParam)) {
                        String dateVal = (String) ConsentExtensionUtils.retrieveValueFromJSONObject(payloadObj,
                                applicableParam);
                        if (!isFutureDate(dateVal)) {
                            String errorMsg = String.format("The %S value has to be a future date", applicableParam);
                            throw new FSPolicyExecutionException(HttpServletResponse.SC_BAD_REQUEST, "invalid_date",
                                    errorMsg);
                        }
                    }
                }
            }
        } catch (FinancialServicesException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void processResponse(ServletResponse servletResponse, Map<String, Object> propertyMap)
            throws FSPolicyExecutionException {

    }

    /**
     * Check if the provided date is a future date.
     *
     * @param dateVal date value
     * @return true if the date is a future date
     */
    private static boolean isFutureDate(String dateVal) {

        if (dateVal == null) {
            return true;
        }
        try {
            OffsetDateTime providedDate = OffsetDateTime.parse(dateVal);
            OffsetDateTime currDate = OffsetDateTime.now(providedDate.getOffset());

            if (log.isDebugEnabled()) {
                log.debug(String.format("Provided date is: %s current date is: %s", providedDate, currDate));
            }
            return providedDate.isAfter(currDate);
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
