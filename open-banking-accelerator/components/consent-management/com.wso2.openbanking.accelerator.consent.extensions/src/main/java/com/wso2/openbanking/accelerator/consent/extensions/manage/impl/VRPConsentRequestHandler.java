/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

import com.google.gson.Gson;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.ErrorConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentServiceUtil;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.PeriodicLimit;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wso2.openbanking.accelerator.consent.extensions.common.
        ConsentExtensionConstants.AUTH_TYPE_AUTHORIZATION;
import static com.wso2.openbanking.accelerator.consent.extensions.
        common.ConsentExtensionConstants.CREATED_STATUS;

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
     * @throws ConsentManagementException if an error occurs while creating the consent or storing consent attributes.
     */
    public void handlePaymentPost(ConsentManageData consentManageData, Object request)
            throws ConsentManagementException {

        // Check if the request is a JSONObject
        if (!(request instanceof JSONObject)) {
            log.error("Invalid request type. Expected JSONObject.");
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    ErrorConstants.PAYLOAD_FORMAT_ERROR);
        }

        JSONObject requestObject = (JSONObject) request;

        // Create a ConsentResource representing the requested consent
        ConsentResource requestedConsent = createRequestedConsent(consentManageData, requestObject);

        // Create the consent
        DetailedConsentResource createdConsent = createConsent(requestedConsent);

        // Set consent attributes for storing
        Map<String, String> consentAttributes = createConsentAttributes(consentManageData);

        // Store consent attributes
        ConsentServiceUtil.getConsentService().storeConsentAttributes(createdConsent.getConsentID(),
                consentAttributes);

        // Set response payload and headers
        setResponse(consentManageData, requestObject, createdConsent);
    }

    /**
     * Method to Create a ConsentResource object using the provided ConsentManageData and requestObject.
     *
     * @param consentManageData Object containing request details
     * @param requestObject JSON object representing the request
     * @return ConsentResource object
     */
    private ConsentResource createRequestedConsent(ConsentManageData consentManageData, JSONObject requestObject) {
        return new ConsentResource(consentManageData.getClientId(),
                requestObject.toJSONString(), ConsentExtensionConstants.VRP,
                ConsentExtensionConstants.AWAITING_AUTH_STATUS);
    }

    /**
     * Method to create a DetailedConsentResource object using the provided ConsentResource.
     *
     * @param requestedConsent ConsentResource object
     * @return DetailedConsentResource object
     * @throws ConsentManagementException if an error occurs while creating the consent
     */
    private DetailedConsentResource createConsent(ConsentResource requestedConsent) throws ConsentManagementException {
        return ConsentServiceUtil.getConsentService()
                .createAuthorizableConsent(requestedConsent, null,
                        CREATED_STATUS, AUTH_TYPE_AUTHORIZATION, true);
    }

    /**
     * Method to Create a map of consent attributes using the provided ConsentManageData.
     *
     * @param consentManageData Object containing request details
     * @return Map of consent attributes
     */
    private Map<String, String> createConsentAttributes(ConsentManageData consentManageData) {
        Map<String, String> consentAttributes = new HashMap<>();
        consentAttributes.put(ConsentExtensionConstants.IDEMPOTENCY_KEY, consentManageData.getHeaders()
                .get(ConsentExtensionConstants.X_IDEMPOTENCY_KEY));

        JSONObject controlParameters = getControlParameters(consentManageData);
        JSONArray periodicLimitsArray = (JSONArray) controlParameters.get(ConsentExtensionConstants.PERIODIC_LIMITS);

        List<PeriodicLimit> periodicLimitsList = createPeriodicLimitsList(periodicLimitsArray);

        JSONObject jsonObject = createControlParameters(controlParameters, periodicLimitsList);

        // Convert the JSONObject to a string
        String consentAttributesJson = jsonObject.toJSONString();

        // Add the consentAttributesJson to the consentAttributes
        consentAttributes.put(ConsentExtensionConstants.CONTROL_PARAMETERS, consentAttributesJson);

        return consentAttributes;
    }

    /**
     * Method to retrieve control parameters from the provided ConsentManageData.
     *
     * @param consentManageData Object containing request details
     * @return JSONObject of control parameters
     */
    private JSONObject getControlParameters(ConsentManageData consentManageData) {
        return (JSONObject) ((JSONObject) ((JSONObject) consentManageData.getPayload())
                .get(ConsentExtensionConstants.DATA)).get(ConsentExtensionConstants.CONTROL_PARAMETERS);
    }

    /**
     * Method to create a list of PeriodicLimit objects from the provided JSONArray.
     *
     * @param periodicLimitsArray JSONArray of periodic limits
     * @return List of PeriodicLimit objects
     */
    private List<PeriodicLimit> createPeriodicLimitsList(JSONArray periodicLimitsArray) {
        List<PeriodicLimit> periodicLimitsList = new ArrayList<>();

        for (Object periodicLimit : periodicLimitsArray) {
            JSONObject jsonObject = (JSONObject) periodicLimit;
            String periodType = (String) jsonObject.get(ConsentExtensionConstants.PERIOD_TYPE);
            BigDecimal amount = BigDecimal.valueOf(Double.parseDouble((String) jsonObject.get(ConsentExtensionConstants.
                    AMOUNT)));
            String periodAlignment = (String) jsonObject.get(ConsentExtensionConstants.PERIOD_ALIGNMENT);

            PeriodicLimit periodicLimits = new PeriodicLimit(periodType, amount, periodAlignment);
            periodicLimitsList.add(periodicLimits);
        }

        return periodicLimitsList;
    }

    /**
     * Method to create JSONObject of control parameters using the provided JSONObject and list of PeriodicLimit objects.
     *
     * @param controlParameters JSONObject of control parameters
     * @param periodicLimitsList List of PeriodicLimit objects
     * @return JSONObject of control parameters
     */
    private JSONObject createControlParameters(JSONObject controlParameters, List<PeriodicLimit> periodicLimitsList) {
        Gson gson = new Gson();

        // Get MaximumIndividualAmount from controlParameters
        JSONObject maximumIndividualAmountObject = (JSONObject) controlParameters.
                get(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT);
        double maximumIndividualAmount = Double.parseDouble(maximumIndividualAmountObject
                .get(ConsentExtensionConstants.AMOUNT).toString());

        // Create a new JSONObject
        JSONObject jsonObject = new JSONObject();

        // Add MaximumIndividualAmount to the JSONObject
        jsonObject.put(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT, maximumIndividualAmount);

        // Convert the periodicLimitsList to a JSON string
        String periodicLimitsJson = gson.toJson(periodicLimitsList);

        // Parse the JSON string back to a JSONArray
        JSONArray newPeriodicLimitsArray;
        try {
            newPeriodicLimitsArray = (JSONArray) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(periodicLimitsJson);
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing JSON", e);
        }

        // Add the PeriodicLimits array to the JSONObject
        jsonObject.put(ConsentExtensionConstants.PERIODIC_LIMITS, newPeriodicLimitsArray);

        return jsonObject;
    }

    /**
     * Method to set the response payload, headers, and status for the provided ConsentManageData using the
     * provided requestObject and createdConsent.
     *
     * @param consentManageData Object containing request details
     * @param requestObject JSON object representing the request
     * @param createdConsent DetailedConsentResource object representing the created consent
     */
    private void setResponse(ConsentManageData consentManageData,
                             JSONObject requestObject, DetailedConsentResource createdConsent) {
        consentManageData.setResponsePayload(ConsentManageUtil.getInitiationResponse(requestObject, createdConsent,
                consentManageData, ConsentExtensionConstants.VRP));

        // Get request headers
        Map<String, String> headers = consentManageData.getHeaders();

        consentManageData.setResponseHeader(ConsentExtensionConstants.X_IDEMPOTENCY_KEY,
                headers.get(ConsentExtensionConstants.X_IDEMPOTENCY_KEY));
        consentManageData.setResponseStatus(ResponseStatus.CREATED);
    }
}
