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
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * Consent expiry validation filter policy.
 */
public class ConsentExpiryValidationFilterPolicy extends FSFilterPolicy {

    private static final Log log = LogFactory.getLog(ConsentExpiryValidationFilterPolicy.class);

    @Override
    public void processRequest(ServletRequest servletRequest, Map<String, Object> propertyMap)
            throws FSPolicyExecutionException {

        ConsentCoreService consentCoreService = ConsentExtensionsDataHolder.getInstance().getConsentCoreService();
        try {
            JSONObject validatePayload = (JSONObject) servletRequest.getAttribute("decodedPayload");
            DetailedConsentResource consent = consentCoreService.getDetailedConsent(
                    validatePayload.getString("consentId"));
            if (consent == null) {
                throw new FSPolicyExecutionException(HttpServletResponse.SC_NOT_FOUND,
                        "consent_not_found", "Consent not found");
            }
            servletRequest.setAttribute("consent", consent);
            String expDateParamName = propertyMap.get("expiry_date_param").toString();
            JSONObject receiptObj  = new JSONObject(consent.getReceipt());

            if (ConsentExtensionUtils.pathExists(receiptObj, expDateParamName)) {
                String dateVal = (String) ConsentExtensionUtils.retrieveValueFromJSONObject(receiptObj,
                        expDateParamName);
                if (isConsentExpired(dateVal)) {
                    throw new FSPolicyExecutionException(HttpServletResponse.SC_BAD_REQUEST, "invalid_request",
                            "Provided consent is expired");
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

    /**
     * Method to check the consent expiration.
     *
     * @param expDateVal   Expiration date value
     * @return boolean     True if consent is expired
     * @throws ConsentException Consent Exception with error details
     */
    private static boolean isConsentExpired(String expDateVal) throws ConsentException {

        if (expDateVal != null && !expDateVal.isEmpty()) {
            try {
                OffsetDateTime expDate = OffsetDateTime.parse(expDateVal);
                return OffsetDateTime.now().isAfter(expDate);
            } catch (DateTimeParseException e) {
                log.error(String.format("Error occurred while parsing the expiration date : %s",
                        expDateVal.replaceAll("[\n\r]", "")));
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        "Error occurred while parsing the expiration date");
            }
        } else {
            return false;
        }
    }
}
