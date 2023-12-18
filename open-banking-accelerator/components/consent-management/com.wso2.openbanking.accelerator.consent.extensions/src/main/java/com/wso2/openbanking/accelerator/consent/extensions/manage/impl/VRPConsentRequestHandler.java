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
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
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
     * Method to handle Variable Recurring Payment Consent Manage POST Request.
     * This method is responsible for processing a Variable Recurring Payment Consent Manage POST request.
     * It validates the payment  request, checks for the existence of an idempotency key,
     * and then delegates the handling to the specific payment initiation flows.
     *
     * @param consentManageData Object
     * @return
     */
    @Override
    public void handleConsentManagePost(ConsentManageData consentManageData) {

        try {
            //Get the request payload from the ConsentManageData
            Object request = consentManageData.getPayload();

//            JSONObject response = (JSONObject) request;
            //Validate Payment Initiation request
            JSONObject validationResponse = VRPConsentRequestValidator.validateVRPPayload(request);

            //Throw an error if the initiation payload is not valid
            if (!((boolean) validationResponse.get(ConsentExtensionConstants.IS_VALID))) {
                log.error(ErrorConstants.INVALID_INITIATION_PAYLOAD);
                throw new ConsentException((ResponseStatus) validationResponse
                        .get(ConsentExtensionConstants.HTTP_CODE),
                        String.valueOf(validationResponse.get(ConsentExtensionConstants.ERRORS)));
            }

            //Check Idempotency key exists
            if (StringUtils.isEmpty(consentManageData.getHeaders()
                    .get(ConsentExtensionConstants.X_IDEMPOTENCY_KEY))) {
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.IDEMPOTENCY_KEY_NOT_FOUND);
            }

            //Handle payment initiation flows
            handlePaymentPost(consentManageData, request);

        } catch (ConsentManagementException e) {
            log.error("Error occurred while handling the initiation request", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    ErrorConstants.PAYMENT_INITIATION_HANDLE_ERROR);
        }
    }


    /**
     * Method to handle Variable Recurring Payment Consent Manage POST Request.
     * This method is responsible for processing a Variable Recurring Payment Consent Manage POST request.
     * It validates the payment initiation request, checks for the existence of an idempotency key,
     * and then delegates the handling to the specific payment initiation flows.
     *
     * @param consentManageData Object
     *
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
                    // Throwing same error as null scenario since client will not be able to identify if consent
                    // exists if consent does not belong to them
                    log.error(ErrorConstants.INVALID_CLIENT_ID_MATCH);
                    throw new ConsentException(ResponseStatus.BAD_REQUEST,
                            ErrorConstants.NO_CONSENT_FOR_CLIENT_ERROR);
                }
                JSONObject receiptJSON = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).
                        parse(consent.getReceipt());
                consentManageData.setResponsePayload(ConsentManageUtil
                        .getInitiationRetrievalResponse(receiptJSON, consent, consentManageData,
                                ConsentExtensionConstants.VRP));
                consentManageData.setResponseStatus(ResponseStatus.OK);
            } catch (ConsentManagementException | ParseException e) {
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        ErrorConstants.ACC_INITIATION_RETRIEVAL_ERROR);
            }
        } else {
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.INVALID_CONSENT_ID);
        }
    }

    /**
     * Method to handle Variable Recurring Payment Consent Manage GET Request.
     * This method retrieves and processes information related to a Variable Recurring Payment consent
     * based on the provided consent ID. It validates the consent ID, checks if the consent exists,
     * verifies if the consent belongs to the client making the request, and constructs a response payload
     * containing relevant initiation retrieval details. The response status is set accordingly.
     *
     * @param consentManageData Object
     *
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
     * @param consentManageData Object containing request details, including client ID, request payload, headers,
     *                          and other relevant information.
     */
    public void handlePaymentPost(ConsentManageData consentManageData, Object request)
            throws ConsentManagementException {

        // Variable to store the created consent
        DetailedConsentResource createdConsent;

        JSONObject requestObject = (JSONObject) request;

        // Create a ConsentResource representing the requested consent
        ConsentResource requestedConsent = new ConsentResource(consentManageData.getClientId(),
                requestObject.toJSONString(), ConsentExtensionConstants.VRP,
                ConsentExtensionConstants.AWAITING_AUTH_STATUS);

        // Create the consent
        createdConsent = ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                .createAuthorizableConsent(requestedConsent, null,
                        CREATED_STATUS, AUTH_TYPE_AUTHORIZATION, true);

        //Set consent attributes for storing
        Map<String, String> consentAttributes = new HashMap<>();
        consentAttributes.put(ConsentExtensionConstants.IDEMPOTENCY_KEY, consentManageData.getHeaders()
                .get(ConsentExtensionConstants.X_IDEMPOTENCY_KEY));

        //Store consent attributes
        ConsentServiceUtil.getConsentService().storeConsentAttributes(createdConsent.getConsentID(),
                consentAttributes);
//        consentManageData.setResponsePayload(ConsentManageUtil.getInitiationResponse(requestObject, createdConsent,
//                consentManageData, ConsentExtensionConstants.VRP_PAYMENT));
        JSONObject response = (JSONObject) request;
        consentManageData.setResponsePayload(ConsentManageUtil.getInitiationResponse(response, createdConsent,
                consentManageData, ConsentExtensionConstants.VRP));

        //Set Control Parameters as consent attributes to store
        JSONObject controlParameters = (JSONObject) ((JSONObject) ((JSONObject) consentManageData.getPayload())
                .get(ConsentExtensionConstants.DATA)).get(ConsentExtensionConstants.CONTROL_PARAMETERS);

        consentAttributes.put(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT,
                ((JSONObject) (controlParameters)
                .get(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT)).get(ConsentExtensionConstants.AMOUNT)
                .toString());
//        consentAttributes.put(ConsentExtensionConstants.PERIOD_ALIGNMENT, ((JSONObject) ((JSONArray)
//                (controlParameters).get(ConsentExtensionConstants.PERIODIC_LIMITS)).get(0))
//                .get(ConsentExtensionConstants.PERIOD_ALIGNMENT).toString());
        consentAttributes.put(ConsentExtensionConstants.PERIOD_TYPE, ((JSONObject) ((JSONArray) (controlParameters)
                .get(ConsentExtensionConstants.PERIODIC_LIMITS)).get(0)).get(ConsentExtensionConstants.PERIOD_TYPE)
                .toString());
        consentAttributes.put(ConsentExtensionConstants.PERIOD_AMOUNT_LIMIT, ((JSONObject)
                ((JSONArray) (controlParameters).get(ConsentExtensionConstants.PERIODIC_LIMITS)).get(0))
                .get(ConsentExtensionConstants.PERIOD_AMOUNT_LIMIT).toString());

        // Get request headers
        Map<String, String> headers = consentManageData.getHeaders();

        //Setting response headers
        //Setting created time and idempotency to headers to handle idempotency in Gateway
        consentManageData.setResponseHeader(ConsentExtensionConstants.X_IDEMPOTENCY_KEY,
                headers.get(ConsentExtensionConstants.X_IDEMPOTENCY_KEY));
        consentManageData.setResponseStatus(ResponseStatus.CREATED);
    }
}
