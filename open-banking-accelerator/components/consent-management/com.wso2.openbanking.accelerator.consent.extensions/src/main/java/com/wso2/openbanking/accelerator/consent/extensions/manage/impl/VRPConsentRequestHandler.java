/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.wso2.openbanking.accelerator.consent.extensions.manage.impl;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.ErrorConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentServiceUtil;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.extensions.manage.validator.VRPConsentRequestValidator;
import com.wso2.openbanking.accelerator.consent.extensions.util.ConsentManageUtil;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

import static com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants.AUTH_TYPE_AUTHORIZATION;
import static com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants.CREATED_STATUS;

/**
 * Consent Manage request handler class for VRP Payment Request Validation.
 */
public class VRPConsentRequestHandler implements ConsentManageRequestHandler {

    private static final Log log = LogFactory.getLog(VRPConsentRequestHandler.class);


    /**
     * This method is responsible for processing a Variable Recurring Payment Consent Manage POST request.
     * It validates the payment  request, checks for the existence of an idempotency key.
     *
     * @param consentManageData Object
     */
    @Override
    public void handleConsentManagePost(ConsentManageData consentManageData) {

        try {
            Object request = consentManageData.getPayload();

            JSONObject validationResponse = VRPConsentRequestValidator.validateVRPPayload(request);

            if (!(Boolean.parseBoolean(validationResponse.getAsString(ConsentExtensionConstants.IS_VALID)))) {
                log.error(validationResponse.get(ConsentExtensionConstants.ERRORS));
                throw new ConsentException((ResponseStatus) validationResponse
                        .get(ConsentExtensionConstants.HTTP_CODE),
                        String.valueOf(validationResponse.get(ConsentExtensionConstants.ERRORS)));
            }

            if (StringUtils.isEmpty(consentManageData.getHeaders()
                    .get(ConsentExtensionConstants.X_IDEMPOTENCY_KEY))) {
                log.error(ErrorConstants.IDEMPOTENCY_KEY_NOT_FOUND);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.IDEMPOTENCY_KEY_NOT_FOUND);
            }

            //Handle payment initiation flows
            handlePaymentPost(consentManageData, request);

        } catch (ConsentManagementException e) {
            log.error("Error occurred while handling the initiation request", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    ErrorConstants.VRP_INITIATION_HANDLE_ERROR);
        }
    }

    /**
     * This method is responsible for handling the GET request for retrieving consent initiation details.
     * It validates the consent ID, checks if the consent exists,verifies if the consent belongs to the
     * client making the request.
     *
     * @param consentManageData Object
     */
    @Override
    public void handleConsentManageGet(ConsentManageData consentManageData) {

        String consentId = consentManageData.getRequestPath().split("/")[1];
        if (ConsentManageUtil.isConsentIdValid(consentId)) {
            try {
                ConsentResource consent = ConsentServiceUtil.getConsentService().getConsent(consentId,
                        false);
                // Check whether the client id is matching
                if (!consent.getClientID().equals(consentManageData.getClientId())) {
                    // Throws the error if the client Ids mismatch
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("ClientIds missmatch. " +
                                        "Retrieved client id: %s, ConsentmanageData client id: %s",
                                consent.getClientID(), consentManageData.getClientId()));
                    }

                    throw new ConsentException(ResponseStatus.BAD_REQUEST,
                            "Invalid client id passed");
                }

                JSONObject receiptJSON = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).
                        parse(consent.getReceipt());
                consentManageData.setResponsePayload(ConsentManageUtil
                        .getInitiationRetrievalResponse(receiptJSON, consent, consentManageData,
                                ConsentExtensionConstants.VRP));
                consentManageData.setResponseStatus(ResponseStatus.OK);
            } catch (ConsentManagementException | ParseException e) {
                log.error(ErrorConstants.INVALID_CLIENT_ID_MATCH, e);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        ErrorConstants.VRP_INITIATION_RETRIEVAL_ERROR);
            }
        } else {
            log.error(ErrorConstants.INVALID_CONSENT_ID);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.INVALID_CONSENT_ID);
        }
    }

    /**
     * Handles the DELETE request for revoking or deleting a consent.
     *
     * @param consentManageData Object containing request details
     */
    @Override
    public void handleConsentManageDelete(ConsentManageData consentManageData) {

        ConsentManageUtil.handleConsentManageDelete(consentManageData);
    }

    /**
     * Method to handle Variable Recurring Payment POST requests.
     * This private method processes Variable Recurring Payment POST requests, creating a new consent
     * based on the provided request payload. It performs the following actions:
     * - Creates a DetailedConsentResource representing the consent initiation.
     * - Stores consent attributes, including the idempotency key.
     * - Constructs the response payload containing initiation details and sets appropriate headers.
     * - Sets the response status to Created.
     *
     * @param consentManageData Object containing request details, including client ID, request payload, headers.
     * @param request Object
     */
    public void handlePaymentPost(ConsentManageData consentManageData, Object request)
            throws ConsentManagementException {

        if (request instanceof JSONObject) {
            JSONObject requestObject = (JSONObject) request;

            // Create a ConsentResource representing the requested consent
            ConsentResource requestedConsent = new ConsentResource(consentManageData.getClientId(),
                    requestObject.toJSONString(), ConsentExtensionConstants.VRP,
                    ConsentExtensionConstants.AWAITING_AUTH_STATUS);

            // Create the consent
            DetailedConsentResource createdConsent = ConsentServiceUtil.getConsentService()
                    .createAuthorizableConsent(requestedConsent, null,
                            CREATED_STATUS, AUTH_TYPE_AUTHORIZATION, true);

            //Set consent attributes for storing
            Map<String, String> consentAttributes = new HashMap<>();
            consentAttributes.put(ConsentExtensionConstants.IDEMPOTENCY_KEY, consentManageData.getHeaders()
                    .get(ConsentExtensionConstants.X_IDEMPOTENCY_KEY));

            consentManageData.setResponsePayload(ConsentManageUtil.getInitiationResponse(requestObject, createdConsent,
                    consentManageData, ConsentExtensionConstants.VRP));

            //Set Control Parameters as consent attributes to store
            JSONObject controlParameters = (JSONObject) ((JSONObject) ((JSONObject) consentManageData.getPayload())
                    .get(ConsentExtensionConstants.DATA)).get(ConsentExtensionConstants.CONTROL_PARAMETERS);

            consentAttributes.put(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT,
                    ((JSONObject) (controlParameters)
                            .get(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT))
                            .get(ConsentExtensionConstants.AMOUNT).toString());

            consentAttributes.put(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT_CURRENCY,
                    ((JSONObject) (controlParameters)
                            .get(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT))
                            .get(ConsentExtensionConstants.CURRENCY).toString());

            consentAttributes.put(ConsentExtensionConstants.PERIOD_ALIGNMENT, ((JSONObject) ((JSONArray)
                    (controlParameters).get(ConsentExtensionConstants.PERIODIC_LIMITS)).get(0))
                    .get(ConsentExtensionConstants.PERIOD_ALIGNMENT).toString());
            //TODO: Improve the logic of storing the PERIODIC_LIMITS and rest of VRP parameters

            consentAttributes.put(ConsentExtensionConstants.PERIOD_TYPE,
                    ((JSONObject) ((JSONArray) (controlParameters)
                    .get(ConsentExtensionConstants.PERIODIC_LIMITS)).get(0)).get(ConsentExtensionConstants.PERIOD_TYPE)
                    .toString());

            consentAttributes.put(ConsentExtensionConstants.PERIOD_AMOUNT_LIMIT, ((JSONObject)
                    ((JSONArray) (controlParameters).get(ConsentExtensionConstants.PERIODIC_LIMITS)).get(0))
                    .get(ConsentExtensionConstants.PERIOD_AMOUNT_LIMIT).toString());

            consentAttributes.put(ConsentExtensionConstants.PERIOD_LIMIT_CURRENCY, ((JSONObject)
                    ((JSONArray) (controlParameters).get(ConsentExtensionConstants.PERIODIC_LIMITS)).get(0))
                    .get(ConsentExtensionConstants.CURRENCY).toString());

            //Store consent attributes
            ConsentServiceUtil.getConsentService().storeConsentAttributes(createdConsent.getConsentID(),
                    consentAttributes);

            // Get request headers
            Map<String, String> headers = consentManageData.getHeaders();

            consentManageData.setResponseHeader(ConsentExtensionConstants.X_IDEMPOTENCY_KEY,
                    headers.get(ConsentExtensionConstants.X_IDEMPOTENCY_KEY));
            consentManageData.setResponseStatus(ResponseStatus.CREATED);

        } else {
            log.error("Invalid request type. Expected JSONObject.");
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    ErrorConstants.PAYLOAD_FORMAT_ERROR);
        }
    }
}
