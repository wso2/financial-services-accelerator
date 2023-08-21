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


package com.wso2.openbanking.accelerator.consent.extensions.authorize.impl.handler.retrieval;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.ErrorConstants;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.utils.ConsentRetrievalUtil;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionUtils;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentServiceUtil;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class to handle Payment Consent data retrieval for Authorize.
 */
public class PaymentConsentRetrievalHandler implements ConsentRetrievalHandler {

    private static final Log log = LogFactory.getLog(PaymentConsentRetrievalHandler.class);


    /**
     * Method defined to retrieve the Payment consent related data in the authorization flow to send them to the.
     * consent page to get PSU consent
     *
     * @param consentResource Consent Resource parameter containing consent related information retrieved from database
     * @return
     * @throws ConsentException
     */
    @Override
    public JSONArray getConsentDataSet(ConsentResource consentResource) {

        try {
            String receiptString = consentResource.getReceipt();
            Object receiptJSON = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(receiptString);

            //Checking whether the request body is in JSON format
            if (!(receiptJSON instanceof JSONObject)) {
                log.error(ErrorConstants.NOT_JSON_OBJECT_ERROR);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, ErrorConstants.NOT_JSON_OBJECT_ERROR);
            }

            //Checking whether the consent status is valid
            if (!consentResource.getCurrentStatus().equals(ConsentExtensionConstants.AWAITING_AUTH_STATUS)) {
                log.error(ErrorConstants.STATE_INVALID_ERROR);
                //Currently throwing error as 400 response. Developer also have the option of appending a field IS_ERROR
                // to the jsonObject and showing it to the user in the webapp. If so, the IS_ERROR have to be checked in
                // any later steps.
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.STATE_INVALID_ERROR);
            }

            JSONArray consentDataJSON = new JSONArray();
            JSONObject receipt = (JSONObject) receiptJSON;
            JSONObject data = (JSONObject) receipt.get(ConsentExtensionConstants.DATA);
            JSONObject initiation = (JSONObject) data.get(ConsentExtensionConstants.INITIATION);

            // Rejecting consent if cut off time is elapsed and the policy is REJECT
            // Updating the consent status to "Reject" if the above condition is true
            if (ConsentExtensionUtils.shouldSubmissionRequestBeRejected(ConsentExtensionUtils
                    .convertToISO8601(consentResource.getCreatedTime()))) {
                boolean success = ConsentServiceUtil.getConsentService()
                        .revokeConsent(consentResource.getConsentID(), ConsentExtensionConstants.REJECTED_STATUS);
                if (!success) {
                    log.error(ErrorConstants.AUTH_TOKEN_REVOKE_ERROR);
                    throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                            ErrorConstants.AUTH_TOKEN_REVOKE_ERROR);
                }
                log.error(ErrorConstants.AUTH_CUT_OFF_DATE_ELAPSED);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.AUTH_CUT_OFF_DATE_ELAPSED);
            }

            consentDataJSON = populateSinglePaymentData(initiation, consentDataJSON);
            //Adding Debtor Account
            ConsentRetrievalUtil.populateDebtorAccount(initiation, consentDataJSON);

            //Adding Creditor Account
            ConsentRetrievalUtil.populateCreditorAccount(initiation, consentDataJSON);

            return consentDataJSON;
        } catch (ParseException | ConsentManagementException e) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, ErrorConstants.CONSENT_RETRIEVAL_ERROR);
        }
    }

    /**
     * Populate Domestic and international Payment Details.
     *
     * @param data        Initiation request from th request
     * @param consentDataJSON   Consent information
     */
    private static JSONArray populateSinglePaymentData(JSONObject data, JSONArray consentDataJSON) {

        JSONArray paymentTypeArray = new JSONArray();
        JSONObject jsonElementPaymentType = new JSONObject();

        if (data.containsKey(ConsentExtensionConstants.CURRENCY_OF_TRANSFER)) {
            //For International Payments
            //Adding Payment Type
            paymentTypeArray.add(ConsentExtensionConstants.INTERNATIONAL_PAYMENTS);

            jsonElementPaymentType.appendField(ConsentExtensionConstants.TITLE,
                    ConsentExtensionConstants.PAYMENT_TYPE_TITLE);
            jsonElementPaymentType.appendField(ConsentExtensionConstants.DATA_SIMPLE, paymentTypeArray);
            consentDataJSON.add(jsonElementPaymentType);

            //Adding Currency Of Transfer
            JSONArray currencyTransferArray = new JSONArray();
            currencyTransferArray.add(data.getAsString(ConsentExtensionConstants.CURRENCY_OF_TRANSFER));

            JSONObject jsonElementCurTransfer = new JSONObject();
            jsonElementCurTransfer.appendField(ConsentExtensionConstants.TITLE,
                    ConsentExtensionConstants.CURRENCY_OF_TRANSFER_TITLE);
            jsonElementCurTransfer.appendField(ConsentExtensionConstants.DATA_SIMPLE, currencyTransferArray);
            consentDataJSON.add(jsonElementCurTransfer);
        } else {
            //Adding Payment Type
            paymentTypeArray.add(ConsentExtensionConstants.DOMESTIC_PAYMENTS);

            jsonElementPaymentType.appendField(ConsentExtensionConstants.TITLE,
                    ConsentExtensionConstants.PAYMENT_TYPE_TITLE);
            jsonElementPaymentType.appendField(ConsentExtensionConstants.DATA_SIMPLE, paymentTypeArray);
            consentDataJSON.add(jsonElementPaymentType);
        }

        //Adding InstructionIdentification
        JSONArray identificationArray = new JSONArray();
        identificationArray.add(data.getAsString(ConsentExtensionConstants.INSTRUCTION_IDENTIFICATION));

        JSONObject jsonElementIdentification = new JSONObject();
        jsonElementIdentification.appendField(ConsentExtensionConstants.TITLE,
                ConsentExtensionConstants.INSTRUCTION_IDENTIFICATION_TITLE);
        jsonElementIdentification.appendField(ConsentExtensionConstants.DATA_SIMPLE, identificationArray);
        consentDataJSON.add(jsonElementIdentification);

        //Adding EndToEndIdentification
        JSONArray endToEndIdentificationArray = new JSONArray();
        endToEndIdentificationArray
                .add(data.getAsString(ConsentExtensionConstants.END_TO_END_IDENTIFICATION));

        JSONObject jsonElementEndToEndIdentification = new JSONObject();
        jsonElementEndToEndIdentification.appendField(ConsentExtensionConstants.TITLE,
                ConsentExtensionConstants.END_TO_END_IDENTIFICATION_TITLE);
        jsonElementEndToEndIdentification.appendField(ConsentExtensionConstants.DATA_SIMPLE,
                endToEndIdentificationArray);
        consentDataJSON.add(jsonElementEndToEndIdentification);

        //Adding InstructedAmount
        JSONObject instructedAmount = (JSONObject) data.get(ConsentExtensionConstants.INSTRUCTED_AMOUNT);
        JSONArray instructedAmountArray = new JSONArray();

        if (instructedAmount.getAsString(ConsentExtensionConstants.AMOUNT_TITLE) != null) {
            instructedAmountArray.add(ConsentExtensionConstants.AMOUNT_TITLE + " : " +
                    instructedAmount.getAsString(ConsentExtensionConstants.AMOUNT));
        }

        if (instructedAmount.getAsString(ConsentExtensionConstants.CURRENCY) != null) {
            instructedAmountArray.add(ConsentExtensionConstants.CURRENCY_TITLE + " : " +
                    instructedAmount.getAsString(ConsentExtensionConstants.CURRENCY));
        }

        JSONObject jsonElementInstructedAmount = new JSONObject();
        jsonElementInstructedAmount.appendField(ConsentExtensionConstants.TITLE,
                ConsentExtensionConstants.INSTRUCTED_AMOUNT_TITLE);
        jsonElementInstructedAmount.appendField(ConsentExtensionConstants.DATA_SIMPLE, instructedAmountArray);
        consentDataJSON.add(jsonElementInstructedAmount);

        return consentDataJSON;
    }

}

