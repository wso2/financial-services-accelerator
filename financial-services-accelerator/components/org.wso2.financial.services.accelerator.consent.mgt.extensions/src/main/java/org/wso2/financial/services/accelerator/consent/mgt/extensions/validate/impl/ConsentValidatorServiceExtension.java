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
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.extension.model.StatusEnum;
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
import java.util.UUID;

/**
 * Consent validator default implementation with external service call.
 */
public class ConsentValidatorServiceExtension implements ConsentValidator {

    private static final Log log = LogFactory.getLog(ConsentValidatorServiceExtension.class);
    private static final String HEADERS = "headers";
    private static final String CONSENT_ID = "consentId";
    private static final String CLIENT_ID = "clientId";
    private static final String RESOURCE_PARAMS = "resourceParams";
    private static final String USER_ID = "userId";
    private static final String ELECTED_RESOURCE = "electedResource";
    private static final String BODY = "body";

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

        // Invoking external validation service configured
        ExternalServiceResponse response = null;
        try {
            response = ServiceExtensionUtils.invokeExternalServiceCall(
                    getConsentValidateServiceRequest(consentValidateData),
                    ServiceExtensionTypeEnum.CONSENT_VALIDATION);
            if (StatusEnum.SUCCESS.equals(response.getStatus())) {
                consentValidationResult.setValid(true);
            } else {
                consentValidationResult.setValid(false);
                consentValidationResult.setErrorMessage(response.getErrorDescription());
                consentValidationResult.setErrorCode(response.getErrorMessage());
                consentValidationResult.setHttpCode(Integer.parseInt(response.getErrorCode()));
            }
        } catch (FinancialServicesException e) {
            consentValidationResult.setValid(false);
            consentValidationResult.setErrorMessage(e.getMessage());
            consentValidationResult.setErrorCode("Error occurred while invoking the external service");
            consentValidationResult.setHttpCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Construct the service request to be sent to the external service.
     *
     * @param consentValidateData  Consent validation data
     * @return  External service request
     */
    private ExternalServiceRequest getConsentValidateServiceRequest(ConsentValidateData consentValidateData) {

        ConsentValidateRequest request = new ConsentValidateRequest(consentValidateData.getConsentId(),
                new JSONObject(consentValidateData.getComprehensiveConsent()),
                constructDataPayload(consentValidateData),
                consentValidateData.getComprehensiveConsent().getConsentType());

        return new ExternalServiceRequest(UUID.randomUUID().toString(), new JSONObject(request));
    }

    /**
     * Construct the data payload to be sent to the external service.
     *
     * @param consentValidateData  Consent validation data
     * @return  Data payload
     */
    private JSONObject constructDataPayload(ConsentValidateData consentValidateData) {
        JSONObject dataPayload = new JSONObject();
        dataPayload.put(HEADERS, consentValidateData.getHeaders());
        dataPayload.put(CONSENT_ID, consentValidateData.getConsentId());
        dataPayload.put(CLIENT_ID, consentValidateData.getClientId());
        dataPayload.put(RESOURCE_PARAMS, consentValidateData.getResourceParams());
        dataPayload.put(USER_ID, consentValidateData.getUserId());
        dataPayload.put(ELECTED_RESOURCE, consentValidateData.getRequestPath());
        if (consentValidateData.getPayload() != null) {
            dataPayload.put(BODY, consentValidateData.getPayload());
        }
        return dataPayload;
    }
}
