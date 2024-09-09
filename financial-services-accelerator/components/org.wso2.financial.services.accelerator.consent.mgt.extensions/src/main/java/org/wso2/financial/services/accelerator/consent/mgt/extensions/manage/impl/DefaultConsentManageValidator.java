/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionConstants;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.ConsentManageValidator;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model.ConsentPayloadValidationResult;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.utils.ConsentManageUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Payload validator class.
 */
public class DefaultConsentManageValidator implements ConsentManageValidator {

    private static final Log log = LogFactory.getLog(DefaultConsentManageValidator.class);

    private static final List<String> validPermissions = Arrays.asList(
            "ReadAccountsBasic",
            "ReadAccountsDetail",
            "ReadTransactionsDetail",
            "ReadBalances");

    @Override
    public ConsentPayloadValidationResult validateRequestPayload(JSONObject requestPayload, String consentType) {
        switch (consentType) {
            case ConsentExtensionConstants.ACCOUNTS:
                return validateAccountRequestPayload(requestPayload);
            case ConsentExtensionConstants.FUNDS_CONFIRMATIONS:
                return validateCOFRequestPayload(requestPayload);
            case ConsentExtensionConstants.PAYMENTS:
                return validatePaymentRequestPayload(requestPayload);
            default:
                return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST, "invalid_consent_type",
                        "Invalid consent type");
        }
    }

    /**
     * Method to validate account initiation request.
     *
     * @param initiation      Initiation Object
     * @return ConsentPayloadValidationResult     Validation Result
     */
    public static ConsentPayloadValidationResult validateAccountRequestPayload(JSONObject initiation) {
        ConsentPayloadValidationResult dataValidationResult = validateDataObjInRequestBody(initiation);
        if (!(boolean) dataValidationResult.isValid()) {
            return dataValidationResult;
        }

        JSONObject data = initiation.getJSONObject("Data");

        if (!data.has("Permissions") || !(data.get("Permissions") instanceof JSONArray)) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Permissions are not in correct format");
        }

        JSONArray permissions = data.getJSONArray("Permissions");
        for (int i = 0; i < permissions.length(); i++) {
            Object permission = permissions.get(i);
            if (!(permission instanceof String)) {
                return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                        ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                        "Permissions should be string values");
            }
            String permissionString = (String) permission;
            if (!validPermissions.contains(permissionString)) {
                return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                        ResponseStatus.BAD_REQUEST.getReasonPhrase(), "Permissions are invalid");
            }
        }

        if (!data.has("ExpirationDateTime") || !(data.get("ExpirationDateTime") instanceof String)) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(), "ExpirationDateTime is invalid");
        }

        if (!ConsentManageUtils.isConsentExpirationTimeValid(data.getString("ExpirationDateTime"))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "ExpirationDateTime should be a future time");
        }

        if (!data.has("TransactionFromDateTime") || !(data.get("TransactionFromDateTime") instanceof String)) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(), "TransactionFromDateTime is invalid");
        }

        if (!data.has("TransactionToDateTime") || !(data.get("TransactionToDateTime") instanceof String)) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(), "TransactionToDateTime is invalid");
        }

        if (!ConsentManageUtils.isTransactionFromToTimeValid(data.getString("TransactionFromDateTime"),
                data.getString("TransactionToDateTime"))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "TransactionToDateTime should be after TransactionFromDateTime");
        }

        return new ConsentPayloadValidationResult(true);
    }

    /**
     * Method to validate COF initiation request.
     *
     * @param initiation      Initiation Object
     * @return ConsentPayloadValidationResult     Validation Result
     */
    public static ConsentPayloadValidationResult validateCOFRequestPayload(JSONObject initiation) {
        //Check request body is valid and not empty
        ConsentPayloadValidationResult dataValidationResult = validateDataObjInRequestBody(initiation);
        if (!(boolean) dataValidationResult.isValid()) {
            return dataValidationResult;
        }

        JSONObject data = initiation.getJSONObject(ConsentExtensionConstants.DATA);

        //Validate json payload expirationDateTime is a future date
        if (data.has(ConsentExtensionConstants.EXPIRATION_DATE) && !ConsentManageUtils
                .isConsentExpirationTimeValid(data.getString(ConsentExtensionConstants.EXPIRATION_DATE))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "ExpirationDateTime should be after TransactionFromDateTime");
        }

        if (data.has(ConsentExtensionConstants.DEBTOR_ACC)) {

            Object debtorAccountObj = data.get(ConsentExtensionConstants.DEBTOR_ACC);
            //Check whether debtor account is a JsonObject
            if (!(debtorAccountObj instanceof JSONObject)) {
                return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                        ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                        "Debtor Account is not in correct format");
            }

            JSONObject debtorAccount = data.getJSONObject(ConsentExtensionConstants.DEBTOR_ACC);
            //Check whether debtor account is not empty
            if (debtorAccount == null) {
                return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                        ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                        "Data object is not in correct format");
            }

        } else {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Debtor Account should be present in the request");
        }
        return new ConsentPayloadValidationResult(true);
    }

    /**
     * Method to validate payment initiation request.
     *
     * @param request      Initiation Object
     * @return ConsentPayloadValidationResult     Validation Result
     */
    public static ConsentPayloadValidationResult validatePaymentRequestPayload(JSONObject request) {

        //Check request body is valid and not empty
        ConsentPayloadValidationResult dataValidationResult = validateDataObjInRequestBody(request);
        if (!(boolean) dataValidationResult.isValid()) {
            return dataValidationResult;
        }
        JSONObject data = request.getJSONObject("Data");

        //Check request body is valid and not empty
        ConsentPayloadValidationResult validationResult = validateInitiationObjInRequestBody(data);
        if (!(boolean) validationResult.isValid()) {
            return validationResult;
        }
        JSONObject initiation = data.getJSONObject("Initiation");

        ConsentPayloadValidationResult initiationValidationResult =
                validatePaymentInitiationPayload(initiation);
        if (!(boolean) initiationValidationResult.isValid()) {
            return initiationValidationResult;
        }

        return new ConsentPayloadValidationResult(true);
    }

    /**
     * Check whether valid Data object is provided.
     *
     * @param requestBody Data object in initiation payload
     * @return whether the Data object is valid
     */
    public static ConsentPayloadValidationResult validateDataObjInRequestBody(JSONObject requestBody) {

        if (!requestBody.has("Data") || !(requestBody.get("Data")
                instanceof JSONObject) || (requestBody.get("Data")) == null) {
            log.error("Invalid request payload");
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(), "Invalid request payload");
        }

        return new ConsentPayloadValidationResult(true);
    }

    /**
     * Check whether valid Initiation object is provided.
     *
     * @param requestBody Initiation object in initiation payload
     * @return whether the Data object is valid
     */
    public static ConsentPayloadValidationResult validateInitiationObjInRequestBody(JSONObject requestBody) {

        if (!requestBody.has("Initiation") || !(requestBody.get("Initiation")
                instanceof JSONObject) || (requestBody.get("Initiation")) == null) {
            log.error("Invalid request payload");
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(), "Invalid request payload");
        }

        return new ConsentPayloadValidationResult(true);
    }

    /**
     * Method to validate payment initiation payload.
     *
     * @param initiation  Initiation Object of the request
     * @return JSONObject Validation Response
     */
    public static ConsentPayloadValidationResult validatePaymentInitiationPayload(JSONObject initiation) {

        //Validate DebtorAccount
        if (initiation.has(ConsentExtensionConstants.DEBTOR_ACC)) {
            JSONObject debtorAccount = initiation.getJSONObject(ConsentExtensionConstants.DEBTOR_ACC);
            ConsentPayloadValidationResult validationResult = validateDebtorAccount(debtorAccount);
            if (!(boolean) validationResult.isValid()) {
                return validationResult;
            }
        }

        //Validate CreditorAccount
        if (initiation.has(ConsentExtensionConstants.CREDITOR_ACC)) {
            JSONObject creditorAccount = initiation.getJSONObject(ConsentExtensionConstants.CREDITOR_ACC);
            ConsentPayloadValidationResult validationResult = validateCreditorAccount(creditorAccount);

            if (!(boolean) validationResult.isValid()) {
                return validationResult;
            }
        } else {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Creditor account should be present in the request");
        }

        //Validate Local Instrument
        if (initiation.has("LocalInstrument") && !ConsentManageUtils
                .validateLocalInstrument(initiation.getString("LocalInstrument"))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Invalid Local Instrument value found");
        }

        JSONObject instructedAmount = initiation.getJSONObject(ConsentExtensionConstants.INSTRUCTED_AMOUNT);
        if (Double.parseDouble(instructedAmount.getString(ConsentExtensionConstants.AMOUNT)) < 1) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Invalid Instructed Amount value found");
        }

        if (!ConsentManageUtils.validateMaxInstructedAmount(
                instructedAmount.getString(ConsentExtensionConstants.AMOUNT))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Instructed Amount value exceeds the maximum limit");
        }
        return new ConsentPayloadValidationResult(true);
    }

    /**
     * Method to validate debtor account.
     *
     * @param debtorAccount Debtor Account object
     * @return ConsentPayloadValidationResult Validation response
     */
    private static ConsentPayloadValidationResult validateDebtorAccount(JSONObject debtorAccount) {

        //Check Debtor Account Scheme name exists
        if (!debtorAccount.has(ConsentExtensionConstants.SCHEME_NAME) ||
                StringUtils.isEmpty(debtorAccount.getString(ConsentExtensionConstants.SCHEME_NAME))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Debtor Account Scheme Name should be present in the request");
        }

        //Validate Debtor Account Scheme name
        if (debtorAccount.has(ConsentExtensionConstants.SCHEME_NAME) &&
                (debtorAccount.getString(ConsentExtensionConstants.SCHEME_NAME) == null ||
                        !ConsentManageUtils.isDebtorAccSchemeNameValid(debtorAccount
                                .getString(ConsentExtensionConstants.SCHEME_NAME)))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Debtor Account Scheme Name is not in the correct format");
        }

        //Check Debtor Account Identification existing
        if (!debtorAccount.has(ConsentExtensionConstants.IDENTIFICATION) ||
                StringUtils.isEmpty(debtorAccount.getString(ConsentExtensionConstants.IDENTIFICATION))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Debtor Account Identification should be present in the request");
        }

        //Validate Debtor Account Identification
        if (debtorAccount.has(ConsentExtensionConstants.IDENTIFICATION) &&
                (debtorAccount.getString(ConsentExtensionConstants.IDENTIFICATION) == null ||
                        !ConsentManageUtils.isDebtorAccIdentificationValid(debtorAccount
                                .getString(ConsentExtensionConstants.IDENTIFICATION)))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Debtor Account Identification is not in the correct format");
        }

        //Validate Debtor Account Name
        if (debtorAccount.has(ConsentExtensionConstants.NAME) &&
                (debtorAccount.getString(ConsentExtensionConstants.NAME) == null ||
                        !ConsentManageUtils.isDebtorAccNameValid(debtorAccount
                                .getString(ConsentExtensionConstants.NAME)))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Debtor Account Name is not in the correct format");
        }

        //Validate Debtor Account Secondary Identification
        if (debtorAccount.has(ConsentExtensionConstants.SECONDARY_IDENTIFICATION) &&
                (debtorAccount.getString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION) == null ||
                        !ConsentManageUtils.isDebtorAccSecondaryIdentificationValid(debtorAccount
                                .getString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION)))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Debtor Account Secondary Identification is not in the correct format");
        }

        return new ConsentPayloadValidationResult(true);
    }

    /**
     * Method to validate debtor account.
     *
     * @param creditorAccount Creditor Account object
     * @return ConsentPayloadValidationResult Validation response
     */
    private static ConsentPayloadValidationResult validateCreditorAccount(JSONObject creditorAccount) {

        //Check Debtor Account Scheme name exists
        if (!creditorAccount.has(ConsentExtensionConstants.SCHEME_NAME) ||
                StringUtils.isEmpty(creditorAccount.getString(ConsentExtensionConstants.SCHEME_NAME))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Creditor Account Scheme Name should be present in the request");
        }

        //Validate Debtor Account Scheme name
        if (creditorAccount.has(ConsentExtensionConstants.SCHEME_NAME) &&
                (creditorAccount.getString(ConsentExtensionConstants.SCHEME_NAME) == null ||
                        !ConsentManageUtils.isDebtorAccSchemeNameValid(creditorAccount
                                .getString(ConsentExtensionConstants.SCHEME_NAME)))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Creditor Account Scheme Name is not in the correct format");
        }

        //Check Debtor Account Identification existing
        if (!creditorAccount.has(ConsentExtensionConstants.IDENTIFICATION) ||
                StringUtils.isEmpty(creditorAccount.getString(ConsentExtensionConstants.IDENTIFICATION))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Creditor Account Identification should be present in the request");
        }

        //Validate Debtor Account Identification
        if (creditorAccount.has(ConsentExtensionConstants.IDENTIFICATION) &&
                (creditorAccount.getString(ConsentExtensionConstants.IDENTIFICATION) == null ||
                        !ConsentManageUtils.isDebtorAccIdentificationValid(creditorAccount
                                .getString(ConsentExtensionConstants.IDENTIFICATION)))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Creditor Account Identification is not in the correct format");
        }

        //Validate Debtor Account Name
        if (creditorAccount.has(ConsentExtensionConstants.NAME) &&
                (creditorAccount.getString(ConsentExtensionConstants.NAME) == null ||
                        !ConsentManageUtils.isDebtorAccNameValid(creditorAccount
                                .getString(ConsentExtensionConstants.NAME)))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Creditor Account Name is not in the correct format");
        }

        //Validate Debtor Account Secondary Identification
        if (creditorAccount.has(ConsentExtensionConstants.SECONDARY_IDENTIFICATION) &&
                (creditorAccount.getString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION) == null ||
                        !ConsentManageUtils.isDebtorAccSecondaryIdentificationValid(creditorAccount
                                .getString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION)))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Creditor Account Secondary Identification is not in the correct format");
        }

        return new ConsentPayloadValidationResult(true);
    }
}
