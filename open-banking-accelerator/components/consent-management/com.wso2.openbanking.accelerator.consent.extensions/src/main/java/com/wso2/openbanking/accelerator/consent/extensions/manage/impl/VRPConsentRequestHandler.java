/**
 * Copyright (c) 2021-2022, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.openbanking.accelerator.consent.extensions.manage.impl;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.ErrorConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionUtils;
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
     * Method to handle Variable Recurring Payment Consent Manage Post Request.
     *
     * @param consentManageData Object containing request details
     */
    @Override
    public void handleConsentManagePost(ConsentManageData consentManageData) {

        try {
            //Validate cutoff datetime
            if (ConsentExtensionUtils.shouldInitiationRequestBeRejected()) {
                log.error(ErrorConstants.MSG_ELAPSED_CUT_OFF_DATE_TIME);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.PAYMENT_INITIATION_HANDLE_ERROR);
            }

            //Get the request payload from the ConsentManageData
            Object request = consentManageData.getPayload();
            if (!(request instanceof JSONObject)) {
                log.error(ErrorConstants.PAYLOAD_FORMAT_ERROR);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.INVALID_REQ_PAYLOAD);
            }

            JSONObject requestObject = (JSONObject) request;

            //Set request object to the response
            JSONObject response = requestObject;

            //Check Idempotency key exists
            if (StringUtils.isEmpty(consentManageData.getHeaders()
                    .get(ConsentExtensionConstants.X_IDEMPOTENCY_KEY))) {
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.IDEMPOTENCY_KEY_NOT_FOUND);
            }

            //Handle payment initiation flows
            handlePaymentPost(consentManageData, requestObject, response);

        } catch (ConsentManagementException e) {
            log.error(e.getMessage());
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    ErrorConstants.PAYMENT_INITIATION_HANDLE_ERROR);
        }
        }


    @Override
    public void handleConsentManageGet(ConsentManageData consentManageData) {

        String consentId = consentManageData.getRequestPath().split("/")[1];
        if (ConsentManageUtil.isConsentIdValid(consentId)) {
            try {
                ConsentResource consent = ConsentServiceUtil.getConsentService().getConsent(consentId,
                        false);
                if (consent == null) {
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.RESOURCE_CONSENT_MISMATCH);
                }
                // Check whether the client id is matching
                if (!consent.getClientID().equals(consentManageData.getClientId())) {
                    //Throwing same error as null scenario since client will not be able to identify if consent
                    // exists if consent does not belong to them
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

    @Override
    public void handleConsentManageDelete(ConsentManageData consentManageData) {

        ConsentManageUtil.handleConsentManageDelete(consentManageData);
    }

    /**
     * Method to handle the Variable Recurring Payment POST requests.
     *
     * @param consentManageData Object containing request details
     * @param requestObject     Request payload
     * @param response          Response
     */
    private void handlePaymentPost(ConsentManageData consentManageData, JSONObject requestObject, JSONObject response)
            throws ConsentManagementException {

        DetailedConsentResource createdConsent;

        //Validate Payment Initiation request
        JSONObject validationResponse = VRPConsentRequestValidator.validatePaymentInitiation(requestObject);

        //Throw an error if the initiation payload is not valid
        if (!((boolean) validationResponse.get(ConsentExtensionConstants.IS_VALID))) {

            log.error(ErrorConstants.PAYLOAD_INVALID);
            throw new ConsentException((ResponseStatus) validationResponse
                    .get(ConsentExtensionConstants.HTTP_CODE),
                    String.valueOf(validationResponse.get(ConsentExtensionConstants.ERRORS)));
        }

        ConsentResource requestedConsent = new ConsentResource(consentManageData.getClientId(),
                requestObject.toJSONString(), ConsentExtensionConstants.VRP,
                ConsentExtensionConstants.AWAITING_AUTH_STATUS);

        createdConsent = ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                .createAuthorizableConsent(requestedConsent, null,
                        CREATED_STATUS, AUTH_TYPE_AUTHORIZATION, true);

        //Set consent attributes for storing
        Map<String, String> consentAttributes = new HashMap();
        consentAttributes.put(ConsentExtensionConstants.IDEMPOTENCY_KEY, consentManageData.getHeaders()
                .get(ConsentExtensionConstants.X_IDEMPOTENCY_KEY));
        //Store consent attributes
        ConsentServiceUtil.getConsentService().storeConsentAttributes(createdConsent.getConsentID(),
                consentAttributes);
        consentManageData.setResponsePayload(ConsentManageUtil.getInitiationResponse(response, createdConsent,
                consentManageData, ConsentExtensionConstants.VRP_PAYMENT));

        //Set Control Parameters as consent attributes to store
        JSONObject controlParameters = (JSONObject) ((JSONObject) ((JSONObject) consentManageData.getPayload())
                .get(ConsentExtensionConstants.DATA)).get(ConsentExtensionConstants.CONTROL_PARAMETERS);
        consentAttributes.put(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT, ((JSONObject) (controlParameters)
                .get(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT)).get(ConsentExtensionConstants.AMOUNT)
                .toString());
        consentAttributes.put(ConsentExtensionConstants.PERIOD_TYPE, ((JSONObject) ((JSONArray) (controlParameters)
                .get(ConsentExtensionConstants.PERIODIC_LIMITS)).get(0)).get(ConsentExtensionConstants.PERIOD_TYPE)
                .toString());
        consentAttributes.put(ConsentExtensionConstants.PERIOD_AMOUNT_LIMIT, ((JSONObject)
                ((JSONArray) (controlParameters).get(ConsentExtensionConstants.PERIODIC_LIMITS)).get(0))
                .get(ConsentExtensionConstants.PERIOD_AMOUNT_LIMIT).toString());
        consentAttributes.put(ConsentExtensionConstants.PAID_AMOUNT, "0");
        consentAttributes.put(ConsentExtensionConstants.LAST_PAYMENT_DATE, "0");

        Map<String, String> headers = consentManageData.getHeaders();
        //Setting response headers
        //Setting created time and idempotency to headers to handle idempotency in Gateway
        consentManageData.setResponseHeader(ConsentExtensionConstants.X_IDEMPOTENCY_KEY,
                headers.get(ConsentExtensionConstants.X_IDEMPOTENCY_KEY));
        consentManageData.setResponseStatus(ResponseStatus.CREATED);
    }
}
