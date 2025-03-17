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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.ConsentValidator;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.model.ConsentValidateData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.model.ConsentValidateRequest;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.model.ConsentValidationResult;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * Consent validator default implementation.
 */
public class ConsentValidatorServiceExtension implements ConsentValidator {

    private static final Log log = LogFactory.getLog(ConsentValidatorServiceExtension.class);

    @Override
    public void validate(ConsentValidateData consentValidateData, ConsentValidationResult consentValidationResult)
            throws ConsentException {

        if (consentValidateData.getComprehensiveConsent() == null ||
                consentValidateData.getComprehensiveConsent().getReceipt() == null) {
            log.error("Consent Details cannot be found");
            consentValidationResult.setValid(false);
            consentValidationResult.setErrorMessage("Consent Details cannot be found");
            consentValidationResult.setErrorCode(ResponseStatus.BAD_REQUEST.getReasonPhrase());
            consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        JSONObject receiptJSON;
        try {
            receiptJSON = new JSONObject(consentValidateData.getComprehensiveConsent().getReceipt());
        } catch (JSONException e) {
            log.error(e.getMessage().replaceAll("[\n\r]", ""));
            consentValidationResult.setValid(false);
            consentValidationResult.setErrorMessage(e.getMessage().replaceAll("[\n\r]", ""));
            consentValidationResult.setErrorCode(ResponseStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
            consentValidationResult.setHttpCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        //User Validation
        String userIdFromToken = FinancialServicesUtils.resolveUsernameFromUserId(consentValidateData.getUserId());
        boolean userIdMatching = false;
        ArrayList<AuthorizationResource> authResources = consentValidateData.getComprehensiveConsent()
                .getAuthorizationResources();
        for (AuthorizationResource resource : authResources) {
            if (userIdFromToken.equals(resource.getUserID())) {
                userIdMatching = true;
                break;
            }
        }

        if (!userIdMatching) {
            log.error("Invalid User Id");
            consentValidationResult.setValid(false);
            consentValidationResult.setErrorMessage("Invalid User Id");
            consentValidationResult.setErrorCode(ResponseStatus.BAD_REQUEST.getReasonPhrase());
            consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        String clientIdFromToken = consentValidateData.getClientId();
        String clientIdFromConsent = consentValidateData.getComprehensiveConsent().getClientID();
        if (clientIdFromToken == null || !clientIdFromToken.equals(clientIdFromConsent)) {
            log.error("Invalid Client Id");
            consentValidationResult.setValid(false);
            consentValidationResult.setErrorMessage("Invalid Client Id");
            consentValidationResult.setErrorCode(ResponseStatus.FORBIDDEN.getReasonPhrase());
            consentValidationResult.setHttpCode(HttpStatus.SC_FORBIDDEN);
            return;
        }

        JSONObject response = ServiceExtensionUtils.invokeExternalServiceCall(
                getConsentValidateServiceRequest(consentValidateData),
                ServiceExtensionTypeEnum.CONSENT_VALIDATION);

        if ("SUCCESS".equals(response.getString("actionStatus"))) {
            consentValidationResult.setValid(true);
        } else {
            consentValidationResult.setValid(false);
            consentValidationResult.setErrorMessage(response.getString("errorDescription"));
            consentValidationResult.setErrorCode(response.getString("errorMessage"));
            consentValidationResult.setHttpCode(HttpStatus.SC_BAD_REQUEST);
        }
        consentValidationResult.setValid(true);

    }

    private ExternalServiceRequest getConsentValidateServiceRequest(ConsentValidateData consentValidateData) {

        Map<String, String> additionalParams = Map.of("consent_type",
                consentValidateData.getComprehensiveConsent().getConsentType());
        ConsentValidateRequest request = new ConsentValidateRequest(consentValidateData.getConsentId(),
                new JSONObject(consentValidateData.getComprehensiveConsent()),
                constructDataPayload(consentValidateData), additionalParams);

        return new ExternalServiceRequest(UUID.randomUUID().toString(), request, "validate");
    }

    private JSONObject constructDataPayload(ConsentValidateData consentValidateData) {
        JSONObject dataPayload = new JSONObject();
        dataPayload.put("headers", consentValidateData.getHeaders());
        dataPayload.put("consentId", consentValidateData.getConsentId());
        dataPayload.put("clientId", consentValidateData.getClientId());
        dataPayload.put("resourceParams", consentValidateData.getResourceParams());
        dataPayload.put("userId", consentValidateData.getUserId());
        dataPayload.put("electedResource", consentValidateData.getRequestPath());
        if (consentValidateData.getPayload() != null) {
            dataPayload.put("body", consentValidateData.getPayload());
        }
        return dataPayload;
    }
}
