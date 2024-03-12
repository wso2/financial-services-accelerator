/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
                            "\\r\\n|\\r|\\n|%20", "");
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
     * @param initiation      Initiation object from the request
     * @param consentDataJSON Consent information object
     */
    public static void populateDebtorAccount(JSONObject initiation, JSONArray consentDataJSON) {
        if (initiation.get(ConsentExtensionConstants.DEBTOR_ACC) != null) {
            JSONObject debtorAccount = (JSONObject) initiation.get(ConsentExtensionConstants.DEBTOR_ACC);
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
            jsonElementDebtor.appendField(StringUtils.lowerCase(ConsentExtensionConstants.DATA), debtorAccountArray);
            consentDataJSON.add(jsonElementDebtor);
        }
    }


    /**
     * Method to add debtor account details to consent data to send it to the consent page.
     *
     * @param initiation
     * @param consentDataJSON
     */
    public static void populateCreditorAccount(JSONObject initiation, JSONArray consentDataJSON) {
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
            jsonElementCreditor.appendField(StringUtils.lowerCase(ConsentExtensionConstants.DATA),
                    creditorAccountArray);
            consentDataJSON.add(jsonElementCreditor);
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
         * Method that consists the implementation for the validation of  payload and the consent,
         * this method also invokes the relevant methods to populate data for each flow.
         *
         * @param consentResource Consent Resource parameter containing consent related information retrieved
         *                        from database
         * @return ConsentDataJson array
         */
        public static JSONArray getConsentData(ConsentResource consentResource)  throws ConsentException {

            JSONArray consentDataJSON = new JSONArray();
            try {

                String receiptString = consentResource.getReceipt();
                Object receiptJSON = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(receiptString);

                // Checking whether the request body is in JSON format
                if (!(receiptJSON instanceof JSONObject)) {
                    log.error(ErrorConstants.NOT_JSON_OBJECT_ERROR);
                    throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                            ErrorConstants.NOT_JSON_OBJECT_ERROR);
                }

                if (!consentResource.getCurrentStatus().equals(ConsentExtensionConstants.AWAITING_AUTH_STATUS)) {
                    log.error(ErrorConstants.STATE_INVALID_ERROR);
                    // Currently throwing an error as a 400 response.
                    // Developers have the option of appending a field IS_ERROR to the jsonObject
                    // and showing it to the user in the webapp.If so,the IS_ERROR has to be checked in any later steps.
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.STATE_INVALID_ERROR);
                }

                JSONObject receipt = (JSONObject) receiptJSON;

                // Checks if 'data' object is present in the receipt
                if (receipt.containsKey(ConsentExtensionConstants.DATA)) {
                    JSONObject data = (JSONObject) receipt.get(ConsentExtensionConstants.DATA);

                    String type = consentResource.getConsentType();
                    switch (type) {
                        case ConsentExtensionConstants.ACCOUNTS:
                            populateAccountData(data, consentDataJSON);
                            break;
                        case ConsentExtensionConstants.PAYMENTS:
                            populatePaymentData(data, consentDataJSON);
                            break;
                        case ConsentExtensionConstants.FUNDSCONFIRMATIONS:
                            populateCofData(data, consentDataJSON);
                            break;
                        case ConsentExtensionConstants.VRP:
                            populateVRPData(data, consentDataJSON);
                            break;
                        default:
                            break;
                    }
                } else {
                    log.error(ErrorConstants.DATA_OBJECT_MISSING_ERROR);
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.DATA_OBJECT_MISSING_ERROR);
                }

            } catch (ParseException e) {
                log.error(ErrorConstants.CONSENT_RETRIEVAL_ERROR, e);
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        ErrorConstants.CONSENT_RETRIEVAL_ERROR);
            }

            return consentDataJSON;
        }


        /**
         * Populate Domestic and international Payment Details.
         *
         * @param data            data request from the request
         * @param consentDataJSON Consent information
         */
        private static void populatePaymentData(JSONObject data, JSONArray consentDataJSON) {

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
                    jsonElementPaymentType.appendField(StringUtils.lowerCase(ConsentExtensionConstants.DATA),
                            paymentTypeArray);
                    consentDataJSON.add(jsonElementPaymentType);

                    //Adding Currency Of Transfer
                    JSONArray currencyTransferArray = new JSONArray();
                    currencyTransferArray.add(initiation.getAsString(ConsentExtensionConstants.CURRENCY_OF_TRANSFER));

                    JSONObject jsonElementCurTransfer = new JSONObject();
                    jsonElementCurTransfer.appendField(ConsentExtensionConstants.TITLE,
                            ConsentExtensionConstants.CURRENCY_OF_TRANSFER_TITLE);
                    jsonElementCurTransfer.appendField(StringUtils.lowerCase(ConsentExtensionConstants.DATA),
                            currencyTransferArray);
                    consentDataJSON.add(jsonElementCurTransfer);
                } else {
                    //Adding Payment Type
                    paymentTypeArray.add(ConsentExtensionConstants.DOMESTIC_PAYMENTS);

                    jsonElementPaymentType.appendField(ConsentExtensionConstants.TITLE,
                            ConsentExtensionConstants.PAYMENT_TYPE_TITLE);
                    jsonElementPaymentType.appendField(StringUtils.lowerCase(ConsentExtensionConstants.DATA),
                            paymentTypeArray);
                    consentDataJSON.add(jsonElementPaymentType);
                }

                //Adding InstructionIdentification
                JSONArray identificationArray = new JSONArray();
                identificationArray.add(initiation.getAsString(ConsentExtensionConstants.INSTRUCTION_IDENTIFICATION));

                JSONObject jsonElementIdentification = new JSONObject();
                jsonElementIdentification.appendField(ConsentExtensionConstants.TITLE,
                        ConsentExtensionConstants.INSTRUCTION_IDENTIFICATION_TITLE);
                jsonElementIdentification.appendField(StringUtils.lowerCase(ConsentExtensionConstants.DATA),
                        identificationArray);
                consentDataJSON.add(jsonElementIdentification);

                //Adding EndToEndIdentification
                JSONArray endToEndIdentificationArray = new JSONArray();
                endToEndIdentificationArray
                        .add(initiation.getAsString(ConsentExtensionConstants.END_TO_END_IDENTIFICATION));

                JSONObject jsonElementEndToEndIdentification = new JSONObject();
                jsonElementEndToEndIdentification.appendField(ConsentExtensionConstants.TITLE,
                        ConsentExtensionConstants.END_TO_END_IDENTIFICATION_TITLE);
                jsonElementEndToEndIdentification.appendField(StringUtils.lowerCase(ConsentExtensionConstants.DATA),
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
                jsonElementInstructedAmount.appendField(StringUtils.lowerCase(ConsentExtensionConstants.DATA),
                        instructedAmountArray);
                consentDataJSON.add(jsonElementInstructedAmount);

                // Adding Debtor Account
                populateDebtorAccount(initiation, consentDataJSON);
                // Adding Creditor Account
                populateCreditorAccount(initiation, consentDataJSON);

            }

        }

        /**
         * Populate account Details.
         *
         * @param data            data request from the request
         * @param consentDataJSON Consent information
         */
        private static void populateAccountData(JSONObject data, JSONArray consentDataJSON) {

            //Adding Permissions
            JSONArray permissions = (JSONArray) data.get(ConsentExtensionConstants.PERMISSIONS);
            if (permissions != null) {
                JSONObject jsonElementPermissions = new JSONObject();
                jsonElementPermissions.appendField(ConsentExtensionConstants.TITLE,
                        ConsentExtensionConstants.PERMISSIONS);
                jsonElementPermissions.appendField(StringUtils.lowerCase(ConsentExtensionConstants.DATA),
                        permissions);
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
                jsonElementExpiry.appendField(StringUtils.lowerCase(ConsentExtensionConstants.DATA),
                        expiryArray);
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
                jsonElementFromDateTime.appendField(StringUtils.lowerCase(ConsentExtensionConstants.DATA),
                        fromDateTimeArray);
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
                jsonElementToDateTime.appendField(StringUtils.lowerCase(ConsentExtensionConstants.DATA),
                        toDateTimeArray);
                consentDataJSON.add(jsonElementToDateTime);
            }

        }

        /**
         * Populate funds confirmation Details.
         *
         * @param initiation      data from the request
         * @param consentDataJSON Consent information
         */
        private static void populateCofData(JSONObject initiation, JSONArray consentDataJSON) {

            //Adding Expiration Date Time
            if (initiation.getAsString(ConsentExtensionConstants.EXPIRATION_DATE) != null) {

                if (!ConsentRetrievalUtil
                        .validateExpiryDateTime(initiation.getAsString(ConsentExtensionConstants.EXPIRATION_DATE))) {
                    log.error(ErrorConstants.CONSENT_EXPIRED);
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.CONSENT_EXPIRED);
                }

                String expiry = initiation.getAsString(ConsentExtensionConstants.EXPIRATION_DATE);
                JSONArray expiryArray = new JSONArray();
                expiryArray.add(expiry);

                JSONObject jsonElementExpiry = new JSONObject();
                jsonElementExpiry.appendField(ConsentExtensionConstants.TITLE,
                        ConsentExtensionConstants.EXPIRATION_DATE_TITLE);
                jsonElementExpiry.appendField(StringUtils.lowerCase(ConsentExtensionConstants.DATA), expiryArray);
                consentDataJSON.add(jsonElementExpiry);
            } else {
                JSONArray expiryArray = new JSONArray();
                expiryArray.add(ConsentExtensionConstants.OPEN_ENDED_AUTHORIZATION);

                JSONObject jsonElementExpiry = new JSONObject();
                jsonElementExpiry.appendField(ConsentExtensionConstants.TITLE,
                        ConsentExtensionConstants.EXPIRATION_DATE_TITLE);
                jsonElementExpiry.appendField(StringUtils.lowerCase(ConsentExtensionConstants.DATA), expiryArray);
                consentDataJSON.add(jsonElementExpiry);
            }

            if (initiation.get(ConsentExtensionConstants.DEBTOR_ACC) != null) {
                //Adding Debtor Account
                populateDebtorAccount(initiation, consentDataJSON);
            }
        }


        /**
         * Populate VRP Details.
         *
         * @param data            Control Parameters from the request
         * @param consentDataJSON Consent information object
         */
        private static void populateVRPData(JSONObject data, JSONArray consentDataJSON) {

            if (data.containsKey(ConsentExtensionConstants.CONTROL_PARAMETERS)) {

                JSONObject controlParameters = (JSONObject) data.
                        get(ConsentExtensionConstants.CONTROL_PARAMETERS);

                //Adding Payment Type
                JSONArray paymentTypeArray = new JSONArray();
                JSONObject jsonElementPaymentType = new JSONObject();
                paymentTypeArray.add(ConsentExtensionConstants.DOMESTIC_VRP);
                jsonElementPaymentType.appendField(ConsentExtensionConstants.TITLE,
                        ConsentExtensionConstants.PAYMENT_TYPE_TITLE);
                jsonElementPaymentType.appendField(StringUtils.lowerCase(ConsentExtensionConstants.DATA),
                        paymentTypeArray);
                consentDataJSON.add(jsonElementPaymentType);

                String validToDateTime = controlParameters.getAsString(ConsentExtensionConstants.VALID_TO_DATE_TIME);
                if (validToDateTime != null) {
                    // Constructing jsonElementValidToDataTime
                    JSONObject jsonElementValidToDateTime = new JSONObject();
                    jsonElementValidToDateTime.appendField(ConsentExtensionConstants.TITLE,
                            ConsentExtensionConstants.CONTROL_PARAMETER_VALID_TO_DATE_TITLE);
                    JSONArray dateControlParameterArray = new JSONArray();
                    dateControlParameterArray.add((controlParameters).
                            get(ConsentExtensionConstants.VALID_TO_DATE_TIME));
                    jsonElementValidToDateTime.appendField(StringUtils.lowerCase(ConsentExtensionConstants.DATA),
                            dateControlParameterArray);

                    consentDataJSON.add(jsonElementValidToDateTime);
                }

                String validFromDateTime = controlParameters.getAsString
                        (ConsentExtensionConstants.VALID_FROM_DATE_TIME);
                if (validFromDateTime != null) {
                    // Constructing jsonElementValidFromDataTime
                    JSONObject jsonElementValidFromDateTime = new JSONObject();
                    jsonElementValidFromDateTime.appendField(ConsentExtensionConstants.TITLE,
                            ConsentExtensionConstants.CONTROL_PARAMETER_VALID_FROM_DATE_TITLE);
                    JSONArray dateTimeControlParameterArray = new JSONArray();
                    dateTimeControlParameterArray.add((controlParameters).
                            get(ConsentExtensionConstants.VALID_FROM_DATE_TIME));
                    jsonElementValidFromDateTime.appendField(StringUtils.lowerCase(ConsentExtensionConstants.DATA),
                            dateTimeControlParameterArray);
                    consentDataJSON.add(jsonElementValidFromDateTime);
                }

                Object maxAmount = controlParameters.get(ConsentExtensionConstants.MAXIMUM_INDIVIDUAL_AMOUNT);

                if (maxAmount instanceof JSONObject) {
                        JSONObject jsonElementControlParameter = new JSONObject();
                        jsonElementControlParameter.appendField(ConsentExtensionConstants.TITLE,
                                ConsentExtensionConstants.CONTROL_PARAMETER_MAX_INDIVIDUAL_AMOUNT_TITLE);
                        JSONArray controlParameterArray = new JSONArray();

                        JSONObject maximumIndividualAmount = (JSONObject) maxAmount;

                        String formattedAmount = String.format("%s %s",
                                maximumIndividualAmount.getAsString(ConsentExtensionConstants.CURRENCY),
                                maximumIndividualAmount.getAsString(ConsentExtensionConstants.AMOUNT));
                        controlParameterArray.add(formattedAmount);
                        jsonElementControlParameter.appendField(StringUtils.lowerCase(ConsentExtensionConstants.DATA),
                                controlParameterArray);

                        consentDataJSON.add(jsonElementControlParameter);
                } else {
                    log.error(ErrorConstants.MAX_AMOUNT_NOT_JSON_OBJECT_ERROR);
                    throw new ConsentException(ResponseStatus.BAD_REQUEST,
                            ErrorConstants.MAX_AMOUNT_NOT_JSON_OBJECT_ERROR);
                }

                Object periodicLimit = controlParameters.get(ConsentExtensionConstants.PERIODIC_LIMITS);

                if (periodicLimit instanceof JSONArray) {
                    JSONArray periodicLimitsArrays = (JSONArray) periodicLimit;

                    for (Object periodicLimitObject : periodicLimitsArrays) {
                        if (periodicLimitObject instanceof JSONObject) {
                            JSONObject jsonObject = (JSONObject) periodicLimitObject;

                            Object periodAlignmentObject = jsonObject.get(ConsentExtensionConstants.PERIOD_ALIGNMENT);

                           if (periodAlignmentObject instanceof String) {
                                // Constructing jsonElementPeriodAlignment
                                JSONObject jsonElementPeriodAlignment = new JSONObject();
                                jsonElementPeriodAlignment.appendField(ConsentExtensionConstants.TITLE,
                                        ConsentExtensionConstants.CONTROL_PARAMETER_PERIOD_ALIGNMENT_TITLE);

                                JSONArray periodAlignmentArray = new JSONArray();
                                periodAlignmentArray.add(periodAlignmentObject);

                                jsonElementPeriodAlignment.appendField(StringUtils.
                                                lowerCase(ConsentExtensionConstants.DATA), periodAlignmentArray);
                                consentDataJSON.add(jsonElementPeriodAlignment);
                           } else {
                               log.error(ErrorConstants.PERIOD_ALIGNMENT_NOT_STRING_ERROR);
                               throw new ConsentException(ResponseStatus.BAD_REQUEST,
                                       ErrorConstants.PERIOD_ALIGNMENT_NOT_STRING_ERROR);
                           }

                            Object periodTypeObject = jsonObject.get(ConsentExtensionConstants.PERIOD_TYPE);

                           if (periodTypeObject instanceof String) {

                                JSONObject jsonElementPeriodType = new JSONObject();
                                jsonElementPeriodType.appendField(ConsentExtensionConstants.TITLE,
                                        ConsentExtensionConstants.CONTROL_PARAMETER_PERIOD_TYPE_TITLE);

                                JSONArray periodTypeArray = new JSONArray();
                                periodTypeArray.add(periodTypeObject);

                                jsonElementPeriodType.appendField(StringUtils.lowerCase(ConsentExtensionConstants.DATA),
                                        periodTypeArray);

                                consentDataJSON.add(jsonElementPeriodType);

                            } else {
                               log.error(ErrorConstants.PERIOD_TYPE_NOT_STRING_ERROR);
                               throw new ConsentException(ResponseStatus.BAD_REQUEST,
                                       ErrorConstants.PERIOD_TYPE_NOT_STRING_ERROR);
                           }
                           // Constructing jsonElementPeriodicLimitsAmountCurrency - periodicLimits amount and currency
                            Object amount = jsonObject.get(ConsentExtensionConstants.AMOUNT);
                            Object currency = jsonObject.get(ConsentExtensionConstants.CURRENCY);

                        if (amount instanceof String && currency instanceof String) {
                            String periodTypeString = (String) periodTypeObject;

                            JSONObject jsonElementPeriodicLimitsAmountCurrency = new JSONObject();
                            jsonElementPeriodicLimitsAmountCurrency.appendField(ConsentExtensionConstants.TITLE,
                                    ConsentExtensionConstants.CONTROL_PARAMETER_AMOUNT_TITLE +
                                            periodTypeString);

                            JSONArray periodicLimitsArray = new JSONArray();

                            String amountString = (String) amount;
                            String currencyString = (String) currency;

                            String formattedPeriodicAmount = String.format("%s %s", currencyString, amountString);
                            periodicLimitsArray.add(formattedPeriodicAmount);

                            jsonElementPeriodicLimitsAmountCurrency.appendField(StringUtils.
                                            lowerCase(ConsentExtensionConstants.DATA), periodicLimitsArray);
                            consentDataJSON.add(jsonElementPeriodicLimitsAmountCurrency);

                        } else {
                            log.error(ErrorConstants.NOT_STRING_ERROR);
                            throw new ConsentException(ResponseStatus.BAD_REQUEST,
                                    ErrorConstants.NOT_STRING_ERROR);
                        }
                    }
                }
            } else {
                log.error(ErrorConstants.NOT_JSON_ARRAY_ERROR);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.NOT_JSON_ARRAY_ERROR);
            }
        } else {
                log.error(ErrorConstants.CONTROL_PARAMETERS_MISSING_ERROR);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ErrorConstants.CONTROL_PARAMETERS_MISSING_ERROR);
        }
    }
}


