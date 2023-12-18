/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.wso2.openbanking.accelerator.consent.extensions.util;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.ErrorConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionConstants;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionUtils;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import net.minidev.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Consent manage util class for accelerator.
 */
public class ConsentManageUtil {
    private static final Log log = LogFactory.getLog(ConsentManageUtil.class);
    private static final OpenBankingConfigParser parser = OpenBankingConfigParser.getInstance();

    /**
     * Check whether valid Data object is provided.
     *
     * @param requestbody Data object in initiation payload
     * @return whether the Data object is valid
     */
    public static JSONObject validateInitiationDataBody(JSONObject requestbody) {
        JSONObject validationResponse = new JSONObject();

        if (!requestbody.containsKey(ConsentExtensionConstants.DATA) || !(requestbody.
                get(ConsentExtensionConstants.DATA)
                instanceof JSONObject) || ((JSONObject) requestbody.get(ConsentExtensionConstants.DATA)).isEmpty()) {
            log.error(ErrorConstants.PAYLOAD_FORMAT_ERROR);
            return ConsentManageUtil.getValidationResponse(ErrorConstants.RESOURCE_INVALID_FORMAT,
                    ErrorConstants.PAYLOAD_FORMAT_ERROR, ErrorConstants.PATH_REQUEST_BODY);
        }

        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
        return validationResponse;
    }


    /**
     * Method to construct the consent manage validation response.
     *
     * @param errorCode    Error Code
     * @param errorMessage Error Message
     * @param errorPath    Error Path
     * @return
     */
    public static JSONObject getValidationResponse(String errorCode, String errorMessage, String errorPath) {
        JSONObject validationResponse = new JSONObject();

        validationResponse.put(ConsentExtensionConstants.IS_VALID, false);
        validationResponse.put(ConsentExtensionConstants.HTTP_CODE, ResponseStatus.BAD_REQUEST);
        validationResponse.put(ConsentExtensionConstants.ERRORS, errorMessage);
        return validationResponse;
    }

    /**
     * Method to validate debtor account.
     *
     * @param debtorAccount Debtor Account object
     * @return
     */
    public static JSONObject validateDebtorAccount(JSONObject debtorAccount) {

        JSONObject validationResponse = new JSONObject();
        //Check Debtor Account Scheme name exists
        if (!debtorAccount.containsKey(ConsentExtensionConstants.SCHEME_NAME) ||
                StringUtils.isEmpty(debtorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME))) {
            log.error(ErrorConstants.MISSING_DEBTOR_ACC_SCHEME_NAME);
            validationResponse.put(ConsentExtensionConstants.IS_VALID, false);
            validationResponse.put(ConsentExtensionConstants.HTTP_CODE, ResponseStatus.BAD_REQUEST);
            validationResponse.put(ConsentExtensionConstants.ERRORS, ErrorConstants.MISSING_DEBTOR_ACC_SCHEME_NAME);

            return validationResponse;
        }

        //Validate Debtor Account Scheme name Length
        if (debtorAccount.containsKey(ConsentExtensionConstants.SCHEME_NAME) &&
                !ConsentManageUtil.validateDebtorAccSchemeNameLength(debtorAccount
                        .getAsString(ConsentExtensionConstants.SCHEME_NAME))) {
            log.error(ErrorConstants.INVALID_DEBTOR_ACC_SCHEME_NAME_LENGTH);
            validationResponse.put(ConsentExtensionConstants.IS_VALID, false);
            validationResponse.put(ConsentExtensionConstants.HTTP_CODE, ResponseStatus.BAD_REQUEST);
            validationResponse.put(ConsentExtensionConstants.ERRORS,
                    ErrorConstants.INVALID_DEBTOR_ACC_SCHEME_NAME_LENGTH);

            return validationResponse;
        }

        //Validate Debtor Account Scheme name
        if (debtorAccount.containsKey(ConsentExtensionConstants.SCHEME_NAME) &&
                (!(debtorAccount.get(ConsentExtensionConstants.SCHEME_NAME) instanceof String) ||
                        !ConsentManageUtil.isDebtorAccSchemeNameValid(debtorAccount
                                .getAsString(ConsentExtensionConstants.SCHEME_NAME)))) {
            log.error(ErrorConstants.INVALID_DEBTOR_ACC_SCHEME_NAME);
            validationResponse.put(ConsentExtensionConstants.IS_VALID, false);
            validationResponse.put(ConsentExtensionConstants.HTTP_CODE, ResponseStatus.BAD_REQUEST);
            validationResponse.put(ConsentExtensionConstants.ERRORS, ErrorConstants.INVALID_DEBTOR_ACC_SCHEME_NAME);

            return validationResponse;
        }

        //Check Debtor Account Identification existing
        if (!debtorAccount.containsKey(ConsentExtensionConstants.IDENTIFICATION) ||
                StringUtils.isEmpty(debtorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION))) {
            log.error(ErrorConstants.MISSING_DEBTOR_ACC_IDENTIFICATION);
            validationResponse.put(ConsentExtensionConstants.IS_VALID, false);
            validationResponse.put(ConsentExtensionConstants.HTTP_CODE, ResponseStatus.BAD_REQUEST);
            validationResponse.put(ConsentExtensionConstants.ERRORS, ErrorConstants.MISSING_DEBTOR_ACC_IDENTIFICATION);

            return validationResponse;
        }

        //Validate Debtor Account Identification
        if (debtorAccount.containsKey(ConsentExtensionConstants.IDENTIFICATION) &&
                (!(debtorAccount.get(ConsentExtensionConstants.IDENTIFICATION) instanceof String) ||
                        !ConsentManageUtil.isDebtorAccIdentificationValid(debtorAccount
                                .getAsString(ConsentExtensionConstants.IDENTIFICATION)))) {
            log.error(ErrorConstants.INVALID_DEBTOR_ACC_IDENTIFICATION);
            validationResponse.put(ConsentExtensionConstants.IS_VALID, false);
            validationResponse.put(ConsentExtensionConstants.HTTP_CODE, ResponseStatus.BAD_REQUEST);
            validationResponse.put(ConsentExtensionConstants.ERRORS, ErrorConstants.MISSING_DEBTOR_ACC_IDENTIFICATION);

            return validationResponse;
        }

        //Validate Debtor Account Name
        if (debtorAccount.containsKey(ConsentExtensionConstants.NAME) &&
                (!(debtorAccount.get(ConsentExtensionConstants.NAME) instanceof String) ||
                        !ConsentManageUtil.isDebtorAccNameValid(debtorAccount
                                .getAsString(ConsentExtensionConstants.NAME)))) {
            log.error(ErrorConstants.INVALID_DEBTOR_ACC_NAME);
            validationResponse.put(ConsentExtensionConstants.IS_VALID, false);
            validationResponse.put(ConsentExtensionConstants.HTTP_CODE, ResponseStatus.BAD_REQUEST);
            validationResponse.put(ConsentExtensionConstants.ERRORS, ErrorConstants.INVALID_DEBTOR_ACC_NAME);

            return validationResponse;
        }

        //Validate Debtor Account Secondary Identification
        if (debtorAccount.containsKey(ConsentExtensionConstants.SECONDARY_IDENTIFICATION) &&
                (!(debtorAccount.get(ConsentExtensionConstants.SECONDARY_IDENTIFICATION) instanceof String) ||
                        !ConsentManageUtil.isDebtorAccSecondaryIdentificationValid(debtorAccount
                                .getAsString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION)))) {
            log.error(ErrorConstants.INVALID_DEBTOR_ACC_SEC_IDENTIFICATION);
            validationResponse.put(ConsentExtensionConstants.IS_VALID, false);
            validationResponse.put(ConsentExtensionConstants.HTTP_CODE, ResponseStatus.BAD_REQUEST);
            validationResponse.put(ConsentExtensionConstants.ERRORS,
                    ErrorConstants.INVALID_DEBTOR_ACC_SEC_IDENTIFICATION);

            return validationResponse;
        }

        //Validate Sort Code number scheme
        String schemeName = debtorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME);
        String identification = debtorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION);
        if (!checkSortCodeSchemeNameAndIdentificationValidity(schemeName, identification)) {
            log.error(ErrorConstants.INVALID_IDENTIFICATION);
            validationResponse.put(ConsentExtensionConstants.IS_VALID, false);
            validationResponse.put(ConsentExtensionConstants.HTTP_CODE, ResponseStatus.BAD_REQUEST);
            validationResponse.put(ConsentExtensionConstants.ERRORS, ErrorConstants.INVALID_IDENTIFICATION);

            return validationResponse;
        }
        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);

        return validationResponse;
    }

    /**
     * Validate creditor account.
     *
     * @param creditorAccount Creditor Account object
     * @return
     */
    public static JSONObject validateCreditorAccount(JSONObject creditorAccount) {

        JSONObject validationResponse = new JSONObject();
        //Check Creditor Account Scheme name exists
        if (!creditorAccount.containsKey(ConsentExtensionConstants.SCHEME_NAME) ||
                StringUtils.isEmpty(creditorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME))) {
            log.error(ErrorConstants.MISSING_CREDITOR_ACC_SCHEME_NAME);
            validationResponse.put(ConsentExtensionConstants.IS_VALID, false);
            validationResponse.put(ConsentExtensionConstants.HTTP_CODE, ResponseStatus.BAD_REQUEST);
            validationResponse.put(ConsentExtensionConstants.ERRORS, ErrorConstants.MISSING_CREDITOR_ACC_SCHEME_NAME);
            return validationResponse;
        }

        //Validate Creditor Account Scheme name Length
        if (creditorAccount.containsKey(ConsentExtensionConstants.SCHEME_NAME) &&
                !ConsentManageUtil.validateDebtorAccSchemeNameLength(creditorAccount
                        .getAsString(ConsentExtensionConstants.SCHEME_NAME))) {
            log.error(ErrorConstants.INVALID_CREDITOR_ACC_SCHEME_NAME_LENGTH);
            validationResponse.put(ConsentExtensionConstants.IS_VALID, false);
            validationResponse.put(ConsentExtensionConstants.HTTP_CODE, ResponseStatus.BAD_REQUEST);
            validationResponse.put(ConsentExtensionConstants.ERRORS,
                    ErrorConstants.INVALID_CREDITOR_ACC_SCHEME_NAME_LENGTH);
            return validationResponse;
        }

        //Validate Creditor Account Scheme name
        if (creditorAccount.containsKey(ConsentExtensionConstants.SCHEME_NAME) &&
                (!(creditorAccount.get(ConsentExtensionConstants.SCHEME_NAME) instanceof String) ||
                        !ConsentManageUtil.isDebtorAccSchemeNameValid(creditorAccount
                                .getAsString(ConsentExtensionConstants.SCHEME_NAME)))) {
            log.error(ErrorConstants.INVALID_CREDITOR_ACC_SCHEME_NAME);
            validationResponse.put(ConsentExtensionConstants.IS_VALID, false);
            validationResponse.put(ConsentExtensionConstants.HTTP_CODE, ResponseStatus.BAD_REQUEST);
            validationResponse.put(ConsentExtensionConstants.ERRORS, ErrorConstants.INVALID_CREDITOR_ACC_SCHEME_NAME);
            return validationResponse;
        }

        //Check Creditor Account Identification existing
        if (!creditorAccount.containsKey(ConsentExtensionConstants.IDENTIFICATION) ||
                StringUtils.isEmpty(creditorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION))) {
            log.error(ErrorConstants.MISSING_CREDITOR_ACC_IDENTIFICATION);
            validationResponse.put(ConsentExtensionConstants.IS_VALID, false);
            validationResponse.put(ConsentExtensionConstants.HTTP_CODE, ResponseStatus.BAD_REQUEST);
            validationResponse.put(ConsentExtensionConstants.ERRORS,
                    ErrorConstants.MISSING_CREDITOR_ACC_IDENTIFICATION);
            return validationResponse;
        }

        //Validate Creditor Account Identification
        if (creditorAccount.containsKey(ConsentExtensionConstants.IDENTIFICATION) &&
                (!(creditorAccount.get(ConsentExtensionConstants.IDENTIFICATION) instanceof String) ||
                        !ConsentManageUtil.isDebtorAccIdentificationValid(creditorAccount
                                .getAsString(ConsentExtensionConstants.IDENTIFICATION)))) {
            log.error(ErrorConstants.INVALID_CREDITOR_ACC_IDENTIFICATION);
            validationResponse.put(ConsentExtensionConstants.IS_VALID, false);
            validationResponse.put(ConsentExtensionConstants.HTTP_CODE, ResponseStatus.BAD_REQUEST);
            validationResponse.put(ConsentExtensionConstants.ERRORS,
                    ErrorConstants.INVALID_CREDITOR_ACC_IDENTIFICATION);
            return validationResponse;
        }

        //Validate Creditor Account Name
        if (creditorAccount.containsKey(ConsentExtensionConstants.NAME) &&
                (!(creditorAccount.get(ConsentExtensionConstants.NAME) instanceof String) ||
                        !ConsentManageUtil.isDebtorAccNameValid(creditorAccount
                                .getAsString(ConsentExtensionConstants.NAME)))) {
            log.error(ErrorConstants.INVALID_CREDITOR_ACC_NAME);
            validationResponse.put(ConsentExtensionConstants.IS_VALID, false);
            validationResponse.put(ConsentExtensionConstants.HTTP_CODE, ResponseStatus.BAD_REQUEST);
            validationResponse.put(ConsentExtensionConstants.ERRORS, ErrorConstants.INVALID_CREDITOR_ACC_NAME);
            return validationResponse;
        }

        //Validate Creditor Account Secondary Identification
        if (creditorAccount.containsKey(ConsentExtensionConstants.SECONDARY_IDENTIFICATION) &&
                (!(creditorAccount.get(ConsentExtensionConstants.SECONDARY_IDENTIFICATION) instanceof String) ||
                        !ConsentManageUtil.isDebtorAccSecondaryIdentificationValid(creditorAccount
                                .getAsString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION)))) {
            log.error(ErrorConstants.INVALID_CREDITOR_ACC_SEC_IDENTIFICATION);
            validationResponse.put(ConsentExtensionConstants.IS_VALID, false);
            validationResponse.put(ConsentExtensionConstants.HTTP_CODE, ResponseStatus.BAD_REQUEST);
            validationResponse.put(ConsentExtensionConstants.ERRORS,
                    ErrorConstants.INVALID_CREDITOR_ACC_SEC_IDENTIFICATION);
            return validationResponse;
        }

        //Validate Sort Code number scheme
        String schemeName = creditorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME);
        String identification = creditorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION);
        if (!checkSortCodeSchemeNameAndIdentificationValidity(schemeName, identification)) {
            log.error(ErrorConstants.INVALID_IDENTIFICATION);
            validationResponse.put(ConsentExtensionConstants.IS_VALID, false);
            validationResponse.put(ConsentExtensionConstants.HTTP_CODE, ResponseStatus.BAD_REQUEST);
            validationResponse.put(ConsentExtensionConstants.ERRORS, ErrorConstants.INVALID_IDENTIFICATION);
            return validationResponse;
        }

        validationResponse.put(ConsentExtensionConstants.IS_VALID, true);
        return validationResponse;
    }


    /**
     * Method to handle the Payment/cof Consent Delete requests.
     *
     * @param consentManageData Object containing request details
     */
    public static void handleConsentManageDelete(ConsentManageData consentManageData) {

        String consentId = consentManageData.getRequestPath().split("/")[1];
        Boolean shouldRevokeTokens;
        if (ConsentManageUtil.isConsentIdValid(consentId)) {
            try {
                ConsentResource consentResource = ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                        .getConsent(consentId, false);

                if (!consentResource.getClientID().equals(consentManageData.getClientId())) {
                    //Throwing this error in a generic manner since client will not be able to identify if consent
                    // exists if consent does not belong to them
                    throw new ConsentException(ResponseStatus.BAD_REQUEST,
                            ErrorConstants.NO_CONSENT_FOR_CLIENT_ERROR);
                }

                if (ConsentExtensionConstants.REVOKED_STATUS.equals(consentResource.getCurrentStatus())) {
                    throw new ConsentException(ResponseStatus.BAD_REQUEST,
                            "Consent already in revoked state");
                }

                //Revoke tokens related to the consent if the flag 'shouldRevokeTokens' is true.
                shouldRevokeTokens = ConsentExtensionConstants.AUTHORIZED_STATUS
                        .equals(consentResource.getCurrentStatus());

                boolean success = ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                        .revokeConsent(consentId, ConsentExtensionConstants.REVOKED_STATUS, null,
                                shouldRevokeTokens);
                if (!success) {
                    throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                            "Token revocation unsuccessful");
                }
                consentManageData.setResponseStatus(ResponseStatus.NO_CONTENT);
            } catch (ConsentManagementException e) {
                log.error(e.getMessage());
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        } else {
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Consent ID invalid");
        }
    }

    /**
     * Utility class to check whether the Debtor Account Scheme name length.
     *
     * @param debtorAccSchemeName Debtor Account Scheme Name
     * @return
     */
    public static boolean validateDebtorAccSchemeNameLength(String debtorAccSchemeName) {
        if (log.isDebugEnabled()) {
            log.debug("debtorAccSchemeName: " + debtorAccSchemeName);
        }

        return (debtorAccSchemeName.length() <= 256);
    }

    /**
     * Utility class to check whether the Debtor Account Scheme name matches with Enum values.
     *
     * @param debtorAccSchemeName Debtor Account Scheme Name
     * @return
     */
    public static boolean isDebtorAccSchemeNameValid(String debtorAccSchemeName) {
        if (log.isDebugEnabled()) {
            log.debug("debtorAccSchemeName: " + debtorAccSchemeName);
        }

        EnumSet<DebtorAccountSchemeNameEnum> set = EnumSet.allOf(DebtorAccountSchemeNameEnum.class);
        boolean result = set.contains(DebtorAccountSchemeNameEnum.fromValue(debtorAccSchemeName));
        if (log.isDebugEnabled()) {
            log.debug("Result: " + result);
        }
        return result;
    }

    /**
     * Utility class to check whether the Debtor Account Identification is valid.
     *
     * @param debtorAccIdentification Debtor Account Identification
     * @return
     */
    public static boolean isDebtorAccIdentificationValid(String debtorAccIdentification) {
        if (log.isDebugEnabled()) {
            log.debug("debtorAccIdentification: " + debtorAccIdentification);
        }

        return (debtorAccIdentification.length() <= 256);
    }

    /**
     * Utility class to check whether the Debtor Account Name is valid.
     *
     * @param debtorAccName Debtor Account Name
     * @return
     */
    public static boolean isDebtorAccNameValid(String debtorAccName) {
        if (log.isDebugEnabled()) {
            log.debug("debtorAccName: " + debtorAccName);
        }

        return (debtorAccName.length() <= 350);
    }

    /**
     * Utility class to check whether the Debtor AccountSecondary Identification is valid.
     *
     * @param debtorAccSecondaryIdentification Debtor Account Secondary Identification
     * @return
     */
    public static boolean isDebtorAccSecondaryIdentificationValid(String debtorAccSecondaryIdentification) {
        if (log.isDebugEnabled()) {
            log.debug("debtorAccSecondaryIdentification: " + debtorAccSecondaryIdentification);
        }

        return (debtorAccSecondaryIdentification.length() <= 34);
    }

    /**
     * Utility class to check whether the SortCode SchemeName and Identification is valid.
     *
     * @param schemeName     Scheme name
     * @param identification Identification
     * @return
     */
    private static boolean checkSortCodeSchemeNameAndIdentificationValidity(String schemeName, String identification) {

        boolean isValid = true;
        if ((ConsentExtensionConstants.OB_SORT_CODE_ACCOUNT_NUMBER.equals(schemeName)
                || ConsentExtensionConstants.SORT_CODE_ACCOUNT_NUMBER.equals(schemeName)) &&
                (StringUtils.isNotEmpty(identification) &&
                        !(identification.length() == ConsentExtensionConstants.ACCOUNT_IDENTIFICATION_LENGTH &&
                                identification.matches(ConsentExtensionConstants.SORT_CODE_PATTERN)))) {
            isValid = false;
        }
        return isValid;
    }

    /**
     * Check whether the local instrument is supported.
     *
     * @param localInstrument Local Instrument value to validate
     */
    public static boolean validateLocalInstrument(String localInstrument) {
        ArrayList<String> defaultLocalInstrumentList = new ArrayList<>(Arrays.asList(
                "OB.BACS",
                "OB.BalanceTransfer", "OB.CHAPS", "OB.Euro1", "OB.FPS", "OB.Link",
                "OB.MoneyTransfer", "OB.Paym", "OB.SEPACreditTransfer",
                "OB.SEPAInstantCreditTransfer", "OB.SWIFT", "OB.Target2"));

        String customValues = (String) parser.getConfiguration().get(
                ConsentExtensionConstants.CUSTOM_LOCAL_INSTRUMENT_VALUES);
        if (customValues != null) {

            String[] customLocalInstrumentList = customValues.split("\\|");
            defaultLocalInstrumentList.addAll(Arrays.asList(customLocalInstrumentList));
        }
        return defaultLocalInstrumentList.contains(localInstrument);

    }

    /**
     * Check whether the amount is higher that the max instructed amount allowed by the bank.
     *
     * @param instructedAmount Instructed Amount to validate
     */
    public static boolean validateMaxInstructedAmount(String instructedAmount) {
        //This is a mandatory configuration in open-banking.xml. Hence can't be null.
        String maxInstructedAmount = (String) parser.getConfiguration().get(
                ConsentExtensionConstants.MAX_INSTRUCTED_AMOUNT);
        return Double.parseDouble(instructedAmount) <= Double.parseDouble(maxInstructedAmount);

    }

    /**
     * Method to construct Initiation response.
     *
     * @param response       Response of the request
     * @param createdConsent Consent response received from service layer
     * @return
     */
    public static JSONObject getInitiationResponse(JSONObject response, DetailedConsentResource createdConsent,
                                                   ConsentManageData consentManageData, String type) {
        JSONObject dataObject = (JSONObject) response.get(ConsentExtensionConstants.DATA);
        dataObject.appendField(ConsentExtensionConstants.CONSENT_ID, createdConsent.getConsentID());
        dataObject.appendField("CreationDateTime", convertEpochDateTime(createdConsent.getCreatedTime()));
        dataObject.appendField("StatusUpdateDateTime", convertEpochDateTime(createdConsent.getUpdatedTime()));
        dataObject.appendField(ConsentExtensionConstants.STATUS,
                ConsentExtensionUtils.getConsentStatus(createdConsent.getCurrentStatus()));
        if (type.equals(ConsentExtensionConstants.PAYMENTS) &&
                ConsentExtensionUtils.isRequestAcceptedPastElapsedTime()) {
            dataObject.appendField(ConsentExtensionConstants.CUT_OFF_DATE_TIME, ConsentExtensionUtils
                    .constructDateTime(0L, (String) parser.getConfiguration()
                            .get(OpenBankingConstants.DAILY_CUTOFF)));
        }

        //add self link
        JSONObject links = new JSONObject();
        links.put(ConsentExtensionConstants.SELF,
                constructSelfLink(createdConsent.getConsentID(), consentManageData, type));
        response.appendField(ConsentExtensionConstants.LINKS, links);

        response.appendField(ConsentExtensionConstants.META, new JSONObject());

        response.remove(ConsentExtensionConstants.DATA);
        response.appendField(ConsentExtensionConstants.DATA, dataObject);

        return response;
    }


    /**
     * Method to construct Retrieval Initiation response.
     *
     * @param receiptJSON Initiation of the request
     * @param consent     Consent response received from service layer
     * @return
     */
    public static JSONObject getInitiationRetrievalResponse(JSONObject receiptJSON, ConsentResource consent,
                                                            ConsentManageData consentManageData, String type) {

        JSONObject dataObject = (JSONObject) receiptJSON.get(ConsentExtensionConstants.DATA);
        dataObject.appendField(ConsentExtensionConstants.CONSENT_ID, consent.getConsentID());
        dataObject.appendField(ConsentExtensionConstants.STATUS, consent.getCurrentStatus());
        dataObject.appendField(ConsentExtensionConstants.STATUS_UPDATE_TIME,
                ConsentExtensionUtils.convertToISO8601(consent.getUpdatedTime()));
        dataObject.appendField(ConsentExtensionConstants.CREATION_TIME,
                ConsentExtensionUtils.convertToISO8601(consent.getCreatedTime()));

        receiptJSON.remove(ConsentExtensionConstants.DATA);
        receiptJSON.appendField(ConsentExtensionConstants.DATA, dataObject);

        JSONObject links = new JSONObject();
        links.put(ConsentExtensionConstants.SELF,
                constructSelfLink(consent.getConsentID(), consentManageData, type));
        receiptJSON.appendField(ConsentExtensionConstants.LINKS, links);

        receiptJSON.appendField(ConsentExtensionConstants.META, new JSONObject());

        return receiptJSON;
    }

    private static String convertEpochDateTime(long epochTime) {

        int nanoOfSecond = 0;
        ZoneOffset offset = ZoneOffset.UTC;
        LocalDateTime ldt = LocalDateTime.ofEpochSecond(epochTime, nanoOfSecond, offset);
        return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").format(ldt);
    }

    /**
     * Method to construct the self link.
     *
     * @param consentId         Consent ID
     * @param consentManageData Request Details recieved
     * @param type              ConsentType
     * @return
     */
    public static String constructSelfLink(String consentId, ConsentManageData consentManageData, String type) {

        String baseUrl = "";
        if (ConsentExtensionConstants.ACCOUNTS.equals(type)) {
            baseUrl = (String) parser.getConfiguration().get(
                    ConsentExtensionConstants.ACCOUNTS_SELF_LINK);
        } else if (ConsentExtensionConstants.PAYMENTS.equals(type)) {
            baseUrl = (String) parser.getConfiguration().get(
                    ConsentExtensionConstants.PAYMENT_SELF_LINK);
        } else if (ConsentExtensionConstants.FUNDSCONFIRMATIONS.equals(type)) {
            baseUrl = (String) parser.getConfiguration().get(
                    ConsentExtensionConstants.COF_SELF_LINK);
        } else if (ConsentExtensionConstants.VRP.equals(type)) {
            baseUrl = (String) parser.getConfiguration().get(
                    ConsentExtensionConstants.VRP_SELF_LINK);
        }

        String requestPath = consentManageData.getRequestPath();
        return baseUrl.replaceFirst("\\{version}", "3.1") + requestPath + "/" + consentId;
    }

    /**
     * Validate the consent ID.
     *
     * @param consentId Consent Id to validate
     * @return
     */
    public static boolean isConsentIdValid(String consentId) {
        return (consentId.length() == 36 && Pattern.matches(ConsentExtensionConstants.UUID_REGEX, consentId));
    }

    /**
     * Validate Expiration Date Time.
     *
     * @param expDateVal Expiration Date Time
     * @return
     */
    public static boolean isConsentExpirationTimeValid(String expDateVal) {
        if (expDateVal == null) {
            return true;
        }
        try {
            OffsetDateTime expDate = OffsetDateTime.parse(expDateVal);
            OffsetDateTime currDate = OffsetDateTime.now(expDate.getOffset());

            return expDate.compareTo(currDate) > 0;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    /**
     * validate the maximum amount in the payload  in VRP.
     *
     * @param currency Currency
     *
     */
//    public static boolean validateCurrency(String currency) {
//
//        if (currency != null
//                && currency.containsKey(ConsentExtensionConstants.CURRENCY)) {
//            Object currencyValue = currency.get(ConsentExtensionConstants.CURRENCY);
//
//            if (currencyValue instanceof String) {
//                return true;
//            } else {
//                return false;
//            }
//        } else {
//            return false;
//        }
//    }
//


    /**
     * validate the periodiclimits in the payload  in VRP.
     * @param limit
     * @return
     */
    public static boolean validatePeriodicAlignment(JSONObject limit) {
        String periodAlignment = (String) limit.get(ConsentExtensionConstants.PERIOD_ALIGNMENT);

        return (ConsentExtensionConstants.CONSENT.equals(periodAlignment) ||
                ConsentExtensionConstants.CALENDER.equals(periodAlignment));
    }

    /**
     * method to validate periodic type in VRP.
     * @param periodiclimit periodic type
     * @return
     */
    public static boolean validatePeriodicType(JSONObject periodiclimit) {
        String periodType = (String) periodiclimit.get(ConsentExtensionConstants.PERIOD_TYPE);

        List<String> periodTypes = Arrays.asList(ConsentExtensionConstants.DAY,
                ConsentExtensionConstants.WEEK, ConsentExtensionConstants.FORTNIGHT,
                ConsentExtensionConstants.MONTH, ConsentExtensionConstants.HALF_YEAR,
                ConsentExtensionConstants.YEAR);

        return (periodTypes.contains(periodType));
    }

}


