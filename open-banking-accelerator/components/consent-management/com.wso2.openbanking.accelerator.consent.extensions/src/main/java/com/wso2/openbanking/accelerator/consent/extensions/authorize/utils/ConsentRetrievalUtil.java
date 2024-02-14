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


package com.wso2.openbanking.accelerator.consent.extensions.authorize.utils;


import com.wso2.openbanking.accelerator.common.util.ErrorConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;

/**
 * Util class for Consent authorize implementation.
 */
public class ConsentRetrievalUtil {

    private static final Log log = LogFactory.getLog(ConsentRetrievalUtil.class);

    /**
     * Method to extract request object from query params.
     *
     * @param spQueryParams
     * @return
     */
    public static String extractRequestObject(String spQueryParams) {

        if (StringUtils.isNotBlank(spQueryParams)) {
            String requestObject = null;
            String[] spQueries = spQueryParams.split("&");
            for (String param : spQueries) {
                if (param.contains("request=")) {
                    requestObject = (param.substring("request=".length())).replaceAll(
                            "\\r\\n|\\r|\\n|\\%20", "");
                }
            }
            if (requestObject != null) {
                return requestObject;
            }
        }
        throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, ErrorConstants.REQUEST_OBJ_EXTRACT_ERROR);
    }

    /**
     * Method to validate the request object and extract consent ID.
     *
     * @param requestObject
     * @return
     */
    public static String extractConsentId(String requestObject) {

        String consentId = null;
        try {
            // validate request object and get the payload
            String requestObjectPayload;
            String[] jwtTokenValues = requestObject.split("\\.");
            if (jwtTokenValues.length == ConsentExtensionConstants.NUMBER_OF_PARTS_IN_JWS) {
                requestObjectPayload = new String(Base64.getUrlDecoder().decode(jwtTokenValues[1]),
                        StandardCharsets.UTF_8);
            } else {
                log.error(ErrorConstants.REQUEST_OBJ_NOT_SIGNED);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.REQUEST_OBJ_NOT_SIGNED);
            }
            Object payload = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(requestObjectPayload);
            if (!(payload instanceof JSONObject)) {
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.NOT_JSON_PAYLOAD);
            }
            JSONObject jsonObject = (JSONObject) payload;

            // get consent id from the request object
            if (jsonObject.containsKey(ConsentExtensionConstants.CLAIMS)) {
                JSONObject claims = (JSONObject) jsonObject.get(ConsentExtensionConstants.CLAIMS);
                for (String claim : ConsentExtensionConstants.CLAIM_FIELDS) {
                    if (claims.containsKey(claim)) {
                        JSONObject claimObject = (JSONObject) claims.get(claim);
                        if (claimObject.containsKey(ConsentExtensionConstants.OPENBANKING_INTENT_ID)) {
                            JSONObject intentObject = (JSONObject) claimObject
                                    .get(ConsentExtensionConstants.OPENBANKING_INTENT_ID);
                            if (intentObject.containsKey(ConsentExtensionConstants.VALUE)) {
                                consentId = (String) intentObject.get(ConsentExtensionConstants.VALUE);
                                break;
                            }
                        }
                    }
                }
            }

            if (consentId == null) {
                log.error(ErrorConstants.INTENT_ID_NOT_FOUND);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.INTENT_ID_NOT_FOUND);
            }
            return consentId;

        } catch (ParseException e) {
            log.error(ErrorConstants.REQUEST_OBJ_PARSE_ERROR, e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, ErrorConstants.REQUEST_OBJ_PARSE_ERROR);
        }
    }


    /**
     * Check if the expiry date time of the consent has elapsed.
     *
     * @param expiryDate The expiry date/time of consent
     * @return boolean result of validation
     */
    public static boolean validateExpiryDateTime(String expiryDate) throws ConsentException {

        try {
            OffsetDateTime expDate = OffsetDateTime.parse(expiryDate);
            if (log.isDebugEnabled()) {
                log.debug(String.format(ErrorConstants.DATE_PARSE_MSG, expDate, OffsetDateTime.now()));
            }
            return OffsetDateTime.now().isBefore(expDate);
        } catch (DateTimeParseException e) {
            log.error(ErrorConstants.EXP_DATE_PARSE_ERROR, e);
            throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.ACC_CONSENT_RETRIEVAL_ERROR);
        }
    }


    /**
     * Method to add debtor account details to consent data to send it to the consent page.
     *
     * @param data     Initiation object from the request
     * @param consentDataJSON Consent information object
     */
    public static void populateDebtorAccount(JSONObject data, JSONArray consentDataJSON) {
        if (consentDataJSON == null) {
            // Initialize consentDataJSON if it's null
            consentDataJSON = new JSONArray();
        }

        if (data.containsKey(ConsentExtensionConstants.INITIATION)) {
            JSONObject initiation = (JSONObject) data.get(ConsentExtensionConstants.INITIATION);

            // Check if initiation contains the debtor account information
            if (initiation.containsKey(ConsentExtensionConstants.DEBTOR_ACC)) {
                JSONObject debtorAccount = (JSONObject) initiation.get(ConsentExtensionConstants.DEBTOR_ACC);
                JSONArray debtorAccountArray = new JSONArray();

                // Adding Debtor Account Scheme Name
                if (debtorAccount.containsKey(ConsentExtensionConstants.SCHEME_NAME)) {
                    debtorAccountArray.add(ConsentExtensionConstants.SCHEME_NAME_TITLE + " : " +
                            debtorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME));
                }

                // Adding Debtor Account Identification
                if (debtorAccount.containsKey(ConsentExtensionConstants.IDENTIFICATION)) {
                    debtorAccountArray.add(ConsentExtensionConstants.IDENTIFICATION_TITLE + " : " +
                            debtorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION));
                }

                // Adding Debtor Account Name
                if (debtorAccount.containsKey(ConsentExtensionConstants.NAME)) {
                    debtorAccountArray.add(ConsentExtensionConstants.NAME_TITLE + " : " +
                            debtorAccount.getAsString(ConsentExtensionConstants.NAME));
                }

                // Adding Debtor Account Secondary Identification
                if (debtorAccount.containsKey(ConsentExtensionConstants.SECONDARY_IDENTIFICATION)) {
                    debtorAccountArray.add(ConsentExtensionConstants.SECONDARY_IDENTIFICATION_TITLE + " : " +
                            debtorAccount.getAsString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION));
                }

                JSONObject jsonElementDebtor = new JSONObject();
                jsonElementDebtor.appendField(ConsentExtensionConstants.TITLE,
                        ConsentExtensionConstants.DEBTOR_ACC_TITLE);
                jsonElementDebtor.appendField(ConsentExtensionConstants.DATA_SIMPLE, debtorAccountArray);
                consentDataJSON.add(jsonElementDebtor);
            }
        }
    }

    /**
     * Method to add debtor account details to consent data to send it to the consent page.
     *
     * @param data
     * @param consentDataJSON
     */
    public static void populateCreditorAccount(JSONObject data, JSONArray consentDataJSON) {

        if (consentDataJSON == null) {
            consentDataJSON = new JSONArray();
        }

        if (data.containsKey(ConsentExtensionConstants.INITIATION)) {
            JSONObject initiation = (JSONObject) data.get(ConsentExtensionConstants.INITIATION);

            if (initiation.get(ConsentExtensionConstants.CREDITOR_ACC) != null) {
                JSONObject creditorAccount = (JSONObject) initiation.get(ConsentExtensionConstants.CREDITOR_ACC);
                JSONArray creditorAccountArray = new JSONArray();
                //Adding Debtor Account Scheme Name
                if (creditorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME) != null) {
                    creditorAccountArray.add(ConsentExtensionConstants.SCHEME_NAME_TITLE + " : " +
                            creditorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME));
                }
                //Adding Debtor Account Identification
                if (creditorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION) != null) {
                    creditorAccountArray.add(ConsentExtensionConstants.IDENTIFICATION_TITLE + " : " +
                            creditorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION));
                }
                //Adding Debtor Account Name
                if (creditorAccount.getAsString(ConsentExtensionConstants.NAME) != null) {
                    creditorAccountArray.add(ConsentExtensionConstants.NAME_TITLE + " : " +
                            creditorAccount.getAsString(ConsentExtensionConstants.NAME));
                }
                //Adding Debtor Account Secondary Identification
                if (creditorAccount.getAsString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION) != null) {
                    creditorAccountArray.add(ConsentExtensionConstants.SECONDARY_IDENTIFICATION_TITLE + " : " +
                            creditorAccount.getAsString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION));
                }

                JSONObject jsonElementCreditor = new JSONObject();
                jsonElementCreditor.appendField(ConsentExtensionConstants.TITLE,
                        ConsentExtensionConstants.CREDITOR_ACC_TITLE);
                jsonElementCreditor.appendField(ConsentExtensionConstants.DATA_SIMPLE, creditorAccountArray);
                consentDataJSON.add(jsonElementCreditor);
            }
        }
    }

    /**
     * Method to append Dummy data for Account ID. Ideally should be separate step calling accounts service
     *
     * @return accountsJSON
     */
    public static JSONArray appendDummyAccountID() {

        JSONArray accountsJSON = new JSONArray();
        JSONObject accountOne = new JSONObject();
        accountOne.appendField("account_id", "12345");
        accountOne.appendField("display_name", "Salary Saver Account");

        accountsJSON.add(accountOne);

        JSONObject accountTwo = new JSONObject();
        accountTwo.appendField("account_id", "67890");
        accountTwo.appendField("account_id", "67890");
        accountTwo.appendField("display_name", "Max Bonus Account");

        accountsJSON.add(accountTwo);

        return accountsJSON;

    }

    /**
     * Method that invokes the relevant methods to populate data for each flow.
     *
     * @param consentResource Consent Resource parameter containing consent related information retrieved from database
     * @return ConsentDataJson array
     */
    public static JSONArray getConsentData(ConsentResource consentResource) throws ParseException {

        JSONArray consentDataJSON = getConsentRetrievalDataSet(consentResource);
        String receiptString = consentResource.getReceipt();
        Object receiptJSON = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(receiptString);

        JSONObject receipt = (JSONObject) receiptJSON;
        JSONObject data = (JSONObject) receipt.get(ConsentExtensionConstants.DATA);
        JSONObject controlParameters = (JSONObject) data.get(ConsentExtensionConstants.CONTROL_PARAMETERS);


        String type = consentResource.getConsentType();
        switch (type) {
            case ConsentExtensionConstants.ACCOUNTS:
                consentDataJSON = populateAccountData(data, consentDataJSON);
                break;
            case ConsentExtensionConstants.PAYMENTS:
               consentDataJSON = populateSinglePaymentData(data, consentDataJSON);
                // Adding Debtor Account
                ConsentRetrievalUtil.populateDebtorAccount(data, consentDataJSON);
                // Adding Creditor Account
                ConsentRetrievalUtil.populateCreditorAccount(data, consentDataJSON);
                break;
            case ConsentExtensionConstants.FUNDSCONFIRMATIONS:
                consentDataJSON = populateCofData(data, consentDataJSON);
                break;
            case ConsentExtensionConstants.VRP:
                consentDataJSON = populateVRData(controlParameters, consentDataJSON);
                break;
            default:
                break;
        }
        return consentDataJSON;

    }

    /**
     * Method defined to retrieve the Payment consent related data in the authorization flow to send them to the
     * consent page to get PSU consent.
     *
     * @param consentResource Consent Resource parameter containing consent related information retrieved from database
     */
    public static JSONArray getConsentRetrievalDataSet(ConsentResource consentResource) {

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
        } catch (ParseException e) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, ErrorConstants.CONSENT_RETRIEVAL_ERROR);
        }
        return null;

    }

    /**
     * Populate Domestic and international Payment Details.
     *
     * @param data       data request from the request
     * @param consentDataJSON   Consent information
     */
    private static JSONArray populateSinglePaymentData(JSONObject data, JSONArray consentDataJSON) {

        if (consentDataJSON == null) {
            // Initialize consentDataJSON if it's null
            consentDataJSON = new JSONArray();
        }

        JSONArray paymentTypeArray = new JSONArray();
        JSONObject jsonElementPaymentType = new JSONObject();

        if (data.containsKey(ConsentExtensionConstants.INITIATION)) {
            JSONObject initiation = (JSONObject) data.get(ConsentExtensionConstants.INITIATION);

            if (initiation.containsKey(ConsentExtensionConstants.CURRENCY_OF_TRANSFER)) {
                //For International Payments
                //Adding Payment Type
                paymentTypeArray.add(ConsentExtensionConstants.INTERNATIONAL_PAYMENTS);

                jsonElementPaymentType.appendField(ConsentExtensionConstants.TITLE,
                        ConsentExtensionConstants.PAYMENT_TYPE_TITLE);
                jsonElementPaymentType.appendField(ConsentExtensionConstants.DATA_SIMPLE, paymentTypeArray);
                consentDataJSON.add(jsonElementPaymentType);

                //Adding Currency Of Transfer
                JSONArray currencyTransferArray = new JSONArray();
                currencyTransferArray.add(initiation.getAsString(ConsentExtensionConstants.CURRENCY_OF_TRANSFER));

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
            identificationArray.add(initiation.getAsString(ConsentExtensionConstants.INSTRUCTION_IDENTIFICATION));

            JSONObject jsonElementIdentification = new JSONObject();
            jsonElementIdentification.appendField(ConsentExtensionConstants.TITLE,
                    ConsentExtensionConstants.INSTRUCTION_IDENTIFICATION_TITLE);
            jsonElementIdentification.appendField(ConsentExtensionConstants.DATA_SIMPLE, identificationArray);
            consentDataJSON.add(jsonElementIdentification);

            //Adding EndToEndIdentification
            JSONArray endToEndIdentificationArray = new JSONArray();
            endToEndIdentificationArray
                    .add(initiation.getAsString(ConsentExtensionConstants.END_TO_END_IDENTIFICATION));

            JSONObject jsonElementEndToEndIdentification = new JSONObject();
            jsonElementEndToEndIdentification.appendField(ConsentExtensionConstants.TITLE,
                    ConsentExtensionConstants.END_TO_END_IDENTIFICATION_TITLE);
            jsonElementEndToEndIdentification.appendField(ConsentExtensionConstants.DATA_SIMPLE,
                    endToEndIdentificationArray);
            consentDataJSON.add(jsonElementEndToEndIdentification);

            //Adding InstructedAmount
            JSONObject instructedAmount = (JSONObject) initiation.get(ConsentExtensionConstants.INSTRUCTED_AMOUNT);
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
        }

        return consentDataJSON;

    }

    /**
     * Populate account Details.
     *
     * @param data        data request from the request
     * @param consentDataJSON   Consent information
     */
    private static JSONArray populateAccountData(JSONObject data, JSONArray consentDataJSON) {

        if (consentDataJSON == null) {
            consentDataJSON = new JSONArray();
        }

        //Adding Permissions
        JSONArray permissions = (JSONArray) data.get(ConsentExtensionConstants.PERMISSIONS);
        if (permissions != null) {
            JSONObject jsonElementPermissions = new JSONObject();
            jsonElementPermissions.appendField(ConsentExtensionConstants.TITLE,
                    ConsentExtensionConstants.PERMISSIONS);
            jsonElementPermissions.appendField(ConsentExtensionConstants.DATA_SIMPLE, permissions);
            consentDataJSON.add(jsonElementPermissions);
        }

        //Adding Expiration Date Time
        String expirationDate = data.getAsString(ConsentExtensionConstants.EXPIRATION_DATE);
        if (expirationDate != null) {
            if (!ConsentRetrievalUtil.validateExpiryDateTime(expirationDate)) {
                log.error(ErrorConstants.CONSENT_EXPIRED);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.CONSENT_EXPIRED);
            }
            JSONArray expiryArray = new JSONArray();
            expiryArray.add(expirationDate);

            JSONObject jsonElementExpiry = new JSONObject();
            jsonElementExpiry.appendField(ConsentExtensionConstants.TITLE,
                    ConsentExtensionConstants.EXPIRATION_DATE_TITLE);
            jsonElementExpiry.appendField(ConsentExtensionConstants.DATA_SIMPLE, expiryArray);
            consentDataJSON.add(jsonElementExpiry);
        }

        //Adding Transaction From Date Time
        String fromDateTime = data.getAsString(ConsentExtensionConstants.TRANSACTION_FROM_DATE);
        if (fromDateTime != null) {
            JSONArray fromDateTimeArray = new JSONArray();
            fromDateTimeArray.add(fromDateTime);

            JSONObject jsonElementFromDateTime = new JSONObject();
            jsonElementFromDateTime.appendField(ConsentExtensionConstants.TITLE,
                    ConsentExtensionConstants.TRANSACTION_FROM_DATE_TITLE);
            jsonElementFromDateTime.appendField(ConsentExtensionConstants.DATA_SIMPLE, fromDateTimeArray);
            consentDataJSON.add(jsonElementFromDateTime);
        }

        //Adding Transaction To Date Time
        String toDateTime = data.getAsString(ConsentExtensionConstants.TRANSACTION_TO_DATE);
        if (toDateTime != null) {
            JSONArray toDateTimeArray = new JSONArray();
            toDateTimeArray.add(toDateTime);

            JSONObject jsonElementToDateTime = new JSONObject();
            jsonElementToDateTime.appendField(ConsentExtensionConstants.TITLE,
                    ConsentExtensionConstants.TRANSACTION_TO_DATE_TITLE);
            jsonElementToDateTime.appendField(ConsentExtensionConstants.DATA_SIMPLE, toDateTimeArray);
            consentDataJSON.add(jsonElementToDateTime);
        }

        return consentDataJSON;

    }

    /**
     * Populate funds confirmation Details.
     *
     * @param data        data request from the request
     * @param consentDataJSON   Consent information
     */
    private static JSONArray populateCofData(JSONObject data, JSONArray consentDataJSON) {

        if (consentDataJSON == null) {
            consentDataJSON = new JSONArray();
        }

        //Adding Expiration Date Time
        if (data.getAsString(ConsentExtensionConstants.EXPIRATION_DATE) != null) {

            if (!ConsentRetrievalUtil
                    .validateExpiryDateTime(data.getAsString(ConsentExtensionConstants.EXPIRATION_DATE))) {
                log.error(ErrorConstants.CONSENT_EXPIRED);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.CONSENT_EXPIRED);
            }

            String expiry = data.getAsString(ConsentExtensionConstants.EXPIRATION_DATE);
            JSONArray expiryArray = new JSONArray();
            expiryArray.add(expiry);

            JSONObject jsonElementExpiry = new JSONObject();
            jsonElementExpiry.appendField(ConsentExtensionConstants.TITLE,
                    ConsentExtensionConstants.EXPIRATION_DATE_TITLE);
            jsonElementExpiry.appendField(ConsentExtensionConstants.DATA_SIMPLE, expiryArray);
            consentDataJSON.add(jsonElementExpiry);
        } else {
            JSONArray expiryArray = new JSONArray();
            expiryArray.add(ConsentExtensionConstants.OPEN_ENDED_AUTHORIZATION);

            JSONObject jsonElementExpiry = new JSONObject();
            jsonElementExpiry.appendField(ConsentExtensionConstants.TITLE,
                    ConsentExtensionConstants.EXPIRATION_DATE_TITLE);
            jsonElementExpiry.appendField(ConsentExtensionConstants.DATA_SIMPLE, expiryArray);
            consentDataJSON.add(jsonElementExpiry);
        }

        //Adding Debtor Account
        if (data.get(ConsentExtensionConstants.DEBTOR_ACC) != null) {
            JSONObject debtorAccount = (JSONObject) data.get(ConsentExtensionConstants.DEBTOR_ACC);
            JSONArray debtorAccountArray = new JSONArray();
            //Adding Debtor Account Scheme Name
            if (debtorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME) != null) {
                debtorAccountArray.add(ConsentExtensionConstants.SCHEME_NAME_TITLE + " : " +
                        debtorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME));
            }
            //Adding Debtor Account Identification
            if (debtorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION) != null) {
                debtorAccountArray.add(ConsentExtensionConstants.IDENTIFICATION_TITLE + " : " +
                        debtorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION));
            }
            //Adding Debtor Account Name
            if (debtorAccount.getAsString(ConsentExtensionConstants.NAME) != null) {
                debtorAccountArray.add(ConsentExtensionConstants.NAME_TITLE + " : " +
                        debtorAccount.getAsString(ConsentExtensionConstants.NAME));
            }
            //Adding Debtor Account Secondary Identification
            if (debtorAccount.getAsString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION) != null) {
                debtorAccountArray.add(ConsentExtensionConstants.SECONDARY_IDENTIFICATION_TITLE + " : " +
                        debtorAccount.getAsString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION));
            }

            JSONObject jsonElementDebtor = new JSONObject();
            jsonElementDebtor.appendField(ConsentExtensionConstants.TITLE,
                    ConsentExtensionConstants.DEBTOR_ACC_TITLE);
            jsonElementDebtor.appendField(ConsentExtensionConstants.DATA_SIMPLE, debtorAccountArray);
            consentDataJSON.add(jsonElementDebtor);
        }

        return consentDataJSON;

    }


    /**
     * Populate VRP Details.
     *
     * @param controlParameters Control Parameters from the request
     * @param consentDataJSON   Consent information object
     */
    private static JSONArray populateVRData(JSONObject controlParameters, JSONArray consentDataJSON) {

        if (consentDataJSON == null) {
            consentDataJSON = new JSONArray();
        }

        JSONArray paymentTypeArray = new JSONArray();
        JSONObject jsonElementPaymentType = new JSONObject();

        //Adding Payment Type
        paymentTypeArray.add(ConsentExtensionConstants.DOMESTIC_VRP);
        jsonElementPaymentType.appendField(ConsentExtensionConstants.TITLE,
                ConsentExtensionConstants.PAYMENT_TYPE_TITLE);
        jsonElementPaymentType.appendField(ConsentExtensionConstants.DATA_SIMPLE, paymentTypeArray);
        consentDataJSON.add(jsonElementPaymentType);

        // Constructing jsonElementValidToDataTime
        JSONObject jsonElementValidToDateTime = new JSONObject();
        jsonElementValidToDateTime.appendField(ConsentExtensionConstants.TITLE,
                ConsentExtensionConstants.CONTROL_PARAMETER_VALID_TO_DATE_TITLE);
        JSONArray dateControlParameterArray = new JSONArray();
        dateControlParameterArray.add((controlParameters).
                get(ConsentExtensionConstants.VALID_TO_DATE_TIME));
        jsonElementValidToDateTime.appendField(ConsentExtensionConstants.DATA_SIMPLE, dateControlParameterArray);

        // Constructing jsonElementValidFromDataTime
        JSONObject jsonElementValidFromDateTime = new JSONObject();
        jsonElementValidFromDateTime.appendField(ConsentExtensionConstants.TITLE,
                ConsentExtensionConstants.CONTROL_PARAMETER_VALID_FROM_DATE_TITLE);
        JSONArray dateTimeControlParameterArray = new JSONArray();
        dateTimeControlParameterArray.add((controlParameters).
                get(ConsentExtensionConstants.VALID_FROM_DATE_TIME));
        jsonElementValidFromDateTime.appendField(ConsentExtensionConstants.DATA_SIMPLE, dateTimeControlParameterArray);

        // Constructing jsonElementControlParameter
        JSONObject jsonElementControlParameter = new JSONObject();
        jsonElementControlParameter.appendField(ConsentExtensionConstants.TITLE,
                ConsentExtensionConstants.CONTROL_PARAMETER_MAX_INDIVIDUAL_AMOUNT_TITLE);
        JSONArray controlParameterArray = new JSONArray();

        String formattedAmount = String.format("%s %s",
                ((JSONObject) controlParameters.get(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT))
                        .get(ConsentExtensionConstants.CURRENCY),
                ((JSONObject) controlParameters.get(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT))
                        .get(ConsentExtensionConstants.AMOUNT));
        controlParameterArray.add(formattedAmount);
        jsonElementControlParameter.appendField(ConsentExtensionConstants.DATA_SIMPLE, controlParameterArray);

        // Constructing jsonElementPeriodAlignment
        JSONObject jsonElementPeriodAlignment = new JSONObject();
        jsonElementPeriodAlignment.appendField(ConsentExtensionConstants.TITLE,
                ConsentExtensionConstants.CONTROL_PARAMETER_PERIOD_ALIGNMENT_TITLE);
        String periodAlignment = (String) ((JSONObject) ((JSONArray) controlParameters
                .get(ConsentExtensionConstants.PERIODIC_LIMITS)).get(0))
                .get(ConsentExtensionConstants.PERIOD_ALIGNMENT);
        JSONArray periodAlignmentArray = new JSONArray();
        periodAlignmentArray.add(periodAlignment);
        jsonElementPeriodAlignment.appendField(ConsentExtensionConstants.DATA_SIMPLE, periodAlignmentArray);

        // Constructing jsonElementPeriodType
        JSONObject jsonElementPeriodType = new JSONObject();
        jsonElementPeriodType.appendField(ConsentExtensionConstants.TITLE,
                ConsentExtensionConstants.CONTROL_PARAMETER_PERIOD_TYPE_TITLE);
        String periodType = (String) ((JSONObject) ((JSONArray) controlParameters
                .get(ConsentExtensionConstants.PERIODIC_LIMITS)).get(0)).get(ConsentExtensionConstants.PERIOD_TYPE);
        JSONArray periodTypeArray = new JSONArray();
        periodTypeArray.add(periodType);
        jsonElementPeriodType.appendField(ConsentExtensionConstants.DATA_SIMPLE, periodTypeArray);

        // Constructing jsonElementPeriodicLimits
        JSONObject jsonElementPeriodicLimits = new JSONObject();
        jsonElementPeriodicLimits.appendField(ConsentExtensionConstants.TITLE,
                ConsentExtensionConstants.CONTROL_PARAMETER_AMOUNT_TITLE + periodType);
        JSONArray periodicLimitsArray = new JSONArray();

        JSONObject periodicLimitsObject = (JSONObject) ((JSONArray) controlParameters
                .get(ConsentExtensionConstants.PERIODIC_LIMITS)).get(0);

        String currency = (String) periodicLimitsObject.get(ConsentExtensionConstants.CURRENCY);
        String amount = (String) periodicLimitsObject.get(ConsentExtensionConstants.AMOUNT);

        // Concatenate currency and amount in the desired order
        String formattedPeriodicAmount = String.format("%s %s", currency, amount);

        periodicLimitsArray.add(formattedPeriodicAmount);

        jsonElementPeriodicLimits.appendField(ConsentExtensionConstants.DATA_SIMPLE, periodicLimitsArray);

        // Adding constructed JSON objects to consentDataJSON
        consentDataJSON.add(jsonElementControlParameter);
        consentDataJSON.add(jsonElementPeriodAlignment);
        consentDataJSON.add(jsonElementPeriodicLimits);
        consentDataJSON.add(jsonElementPeriodType);
        consentDataJSON.add(jsonElementValidToDateTime);
        consentDataJSON.add(jsonElementValidFromDateTime);

        return consentDataJSON;

    }
}

