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
package com.wso2.openbanking.accelerator.common.util;

/**
 * Error Constant Class.
 */
public class ErrorConstants {

    //Error Response Structure constants
    public static final String CODE = "Code";
    public static final String ID = "Id";
    public static final String ERRORS = "Errors";
    public static final String PATH = "Path";
    public static final String URL = "Url";
    public static final String ERROR = "error";

    //Low level textual error code
    public static final String FIELD_INVALID = "OB.Field.Invalid";
    public static final String FIELD_MISSING = "OB.Field.Missing";
    public static final String RESOURCE_INVALID_FORMAT = "OB.Resource.InvalidFormat";
    public static final String UNSUPPORTED_LOCAL_INSTRUMENTS = "OB.Unsupported.LocalInstrument";
    public static final String PATH_REQUEST_BODY = "Payload.Body";
    public static final String PATH_INSTRUCTED_AMOUNT = "Data.Initiation.InstructedAmount";
    public static final String PATH_CREDIT_ACCOUNT = "Data.Initiation.CreditorAccount";
    public static final String PATH_LOCAL_INSTRUMENT = "Data.Initiation.LocalInstrument";
    public static final String PATH_DEBTOR_ACCOUNT_NAME = "Data.Initiation.DebtorAccount.Name";
    public static final String PATH_DEBTOR_ACCOUNT_IDENTIFICATION = "Data.Initiation.DebtorAccount.Identification";
    public static final String PATH_DEBTOR_ACCOUNT_SCHEME = "Data.Initiation.DebtorAccount.SchemeName";
    public static final String PATH_CREDIT_ACCOUNT_SEC_IDENTIFICATION = "Data.Initiation.CreditorAccount" +
            ".SecondaryIdentification";
    public static final String PATH_CREDIT_ACCOUNT_NAME = "Data.Initiation.CreditorAccount.Name";

    public static final String PATH_CREDIT_ACCOUNT_IDENTIFICATION = "Data.Initiation.CreditorAccount.Identification";
    public static final String PATH_CREDIT_ACCOUNT_SCHEME = "Data.Initiation.CreditorAccount.SchemeName";

    public static final String PATH_INVALID = "Request path invalid";
    public static final String PAYLOAD_INVALID = "Consent validation failed due to invalid initiation payload";
    public static final String NOT_JSON_OBJECT_ERROR = "Payload is not a JSON object";
    public static final String PAYLOAD_FORMAT_ERROR = "Request Payload is not in correct JSON format";
    public static final String INVALID_REQ_PAYLOAD = "Invalid request payload";
    public static final String MISSING_DEBTOR_ACC_SCHEME_NAME = "Mandatory parameter Debtor Account Scheme Name does " +
            "not exists";
    public static final String MISSING_DEBTOR_ACC_IDENTIFICATION = "Mandatory parameter Debtor Account Identification" +
            " does not exists";
    public static final String INVALID_DEBTOR_ACC_SCHEME_NAME = "Debtor Account Scheme Name does not match with the" +
            " Scheme Names defined in the specification";
    public static final String INVALID_DEBTOR_ACC_IDENTIFICATION = "Debtor Account Identification should not exceed" +
            " the max length of 256 characters defined in the specification";
    public static final String INVALID_DEBTOR_ACC_NAME = "Debtor Account Name should not exceed the max length of 70" +
            " character defined in the specification";
    public static final String INVALID_DEBTOR_ACC_SEC_IDENTIFICATION = "Debtor Account Secondary Identification" +
            " should not exceed the max length of 34 characters defined in the specification";
    public static final String NO_CONSENT_FOR_CLIENT_ERROR = "No valid consent found for given information";
    public static final String PAYMENT_INITIATION_HANDLE_ERROR = "Error occurred while handling the payment " +
            "initiation request";
    public static final String MSG_ELAPSED_CUT_OFF_DATE_TIME = "{payment-order} consent / resource received after " +
            "CutOffDateTime.";
    public static final String MAX_INSTRUCTED_AMOUNT = "Instructed Amount specified exceed the Maximum Instructed " +
            "Amount of the bank";
    public static final String INVALID_INSTRUCTED_AMOUNT = "Instructed Amount specified should be grater than zero";
    public static final String MSG_MISSING_CREDITOR_ACC = "Mandatory parameter CreditorAccount is missing in the" +
            " payload.";
    public static final String MISSING_CREDITOR_ACC_SCHEME_NAME = "Mandatory parameter Creditor Account Scheme Name" +
            " does not exists";
    public static final String MISSING_CREDITOR_ACC_IDENTIFICATION = "Mandatory parameter Creditor Account " +
            "Identification does not exists";
    public static final String INVALID_CREDITOR_ACC_SCHEME_NAME = "Creditor Account Scheme Name does not match with" +
            " the Scheme Names defined in the specification";
    public static final String INVALID_CREDITOR_ACC_IDENTIFICATION = "Creditor Account Identification should not " +
            "exceed the max length of 256 characters defined in the specification";
    public static final String INVALID_CREDITOR_ACC_NAME = "Creditor Account Name should not exceed the max length" +
            " of 350 character defined in the specification";
    public static final String INVALID_CREDITOR_ACC_SEC_IDENTIFICATION = "Creditor Account Secondary Identification" +
            " should not exceed the max length of 34 characters defined in the specification";
    public static final String INVALID_IDENTIFICATION = "Identification validation for SortCodeNumber Scheme failed.";
    public static final String INVALID_LOCAL_INSTRUMENT = "The given local instrument value is not supported";
    public static final String INVALID_DEBTOR_ACC_SCHEME_NAME_LENGTH = "Debtor Account Scheme Name length does not " +
            "match with the length defined in the specification";
    public static final String INVALID_CREDITOR_ACC_SCHEME_NAME_LENGTH = "Creditor Account Scheme Name length does" +
            " not match with the length defined in the specification";
    public static final String IDEMPOTENCY_KEY_NOT_FOUND = "Idempotency related details should be submitted" +
            " in order to proceed.";
    public static final String MSG_INVALID_DEBTOR_ACC = "Mandatory parameter DebtorAccount object is invalid.";
    public static final String PATH_DEBTOR_ACCOUNT = "Data.Initiation.DebtorAccount";
    public static final String COF_PATH_DEBTOR_ACCOUNT_SCHEME = "Data.DebtorAccount.SchemeName";
    public static final String COF_PATH_DEBTOR_ACCOUNT_IDENTIFICATION = "Data.DebtorAccount.Identification";
    public static final String COF_PATH_DEBTOR_ACCOUNT_NAME = "Data.DebtorAccount.Name";
    public static final String COF_PATH_DEBTOR_ACCOUNT_SECOND_IDENTIFICATION =
            "Data.DebtorAccount.SecondaryIdentification";
    public static final String PATH_CUTOFF_DATE = "Data.CutOffDateTime";
    public static final String RULES_CUTOFF = "OB.Rules.AfterCutOffDateTime";
    public static final String PATH_CONSENT_ID = "Data.Initiation.Consent-id";
    public static final String PATH_DATA = "Data";
    public static final String PATH_INITIATION = "Data.Initiation";
    public static final String PATH_CONTROL_PARAMETERS = "Data.ControlParameters";
    public static final String PATH_RISK = "Data.Risk";
    public static final String PATH_URL = "Data.Url";
    public static final String PATH_EXPIRATION_DATE = "Data.Expiration-Date";
    public static final String MSG_MISSING_DEBTOR_ACC = "Mandatory parameter DebtorAccount is missing in the payload.";
    public static final String REQUEST_OBJ_EXTRACT_ERROR = "Request object cannot be extracted";
    public static final String REQUEST_OBJ_NOT_SIGNED = "request object is not signed JWT";
    public static final String NOT_JSON_PAYLOAD = "Payload is not a JSON object";
    public static final String INTENT_ID_NOT_FOUND = "intent_id not found in request object";
    public static final String REQUEST_OBJ_PARSE_ERROR = "Error while parsing the request object.";
    public static final String STATE_INVALID_ERROR = "Consent not in authorizable state";
    public static final String DATE_PARSE_MSG = "Parsed OffsetDateTime: %s, current OffsetDateTime: %s";
    public static final String EXP_DATE_PARSE_ERROR = "Error occurred while parsing the expiration date. ";

    public static final String ACC_CONSENT_RETRIEVAL_ERROR = "Error occurred while retrieving the account initiation" +
            " request details";
    public static final String CONSENT_EXPIRED = "Provided consent is expired";
    public static final String CONSENT_RETRIEVAL_ERROR = "Exception occurred while getting consent data";
    public static final String AUTH_CUT_OFF_DATE_ELAPSED = "Cut off time has elapsed";
    public static final String AUTH_TOKEN_REVOKE_ERROR = "Cutoff date time elapsed. Error while revoking the consent.";
    public static final String ACCOUNT_ID_NOT_FOUND_ERROR = "Account IDs not available in persist request";
    public static final String ACCOUNT_ID_FORMAT_ERROR = "Account IDs format error in persist request";
    public static final String RESOURCE_CONSENT_MISMATCH = "OB.Resource.ConsentMismatch";
    public static final String INVALID_USER_ID = "Token received does not bound to the authorized user.:"
            + ErrorConstants.PATH_ACCESS_TOKEN;
    public static final String PATH_ACCESS_TOKEN = "Header.AccessToken";
    public static final String MSG_INVALID_CLIENT_ID = "The client Id related the consent does not match with the " +
            "client id bound to token:" +  ErrorConstants.PATH_CLIENT_ID;
    public static final String PATH_CLIENT_ID = "Header.Client-id";
    public static final String UNEXPECTED_ERROR = "OB.UnexpectedError";
    public static final String INVALID_CONSENT_TYPE = "Invalid Consent Type found in the request";
    public static final String ACCOUNT_CONSENT_STATE_INVALID = "Account validation failed due to invalid consent" +
            " state. :" + ErrorConstants.PATH_STATUS;
    public static final String PATH_STATUS = "Payload.Status";
    public static final String RESOURCE_INVALID_CONSENT_STATUS = "OB.Resource.InvalidConsentStatus";
    public static final String INSTRUCTION_IDENTIFICATION_MISMATCH = "Instruction Identification does not match:"
            + ErrorConstants.PATH_INSTRUCTION_IDENTIFICATION;
    public static final String PATH_INSTRUCTION_IDENTIFICATION = "Data.Initiation.InstructionIdentification";
    public static final String END_TO_END_IDENTIFICATION_MISMATCH = "End to End Identification does not match:"
            + ErrorConstants.PATH_ENDTOEND_IDENTIFICATION;
    public static final String PATH_ENDTOEND_IDENTIFICATION = "Data.Initiation.EndToEndIdentification";
    public static final String END_TO_END_IDENTIFICATION_NOT_FOUND = "End to End Identification isn't present in " +
            "the request or in the consent:" + ErrorConstants.PATH_ENDTOEND_IDENTIFICATION;
    public static final String INSTRUCTED_AMOUNT_AMOUNT_MISMATCH = "Instructed Amount Amount does not match the " +
            "initiated amount:" + ErrorConstants.PATH_INSTRUCTED_AMOUNT_AMOUNT;
    public static final String PATH_INSTRUCTED_AMOUNT_AMOUNT = "Data.Initiation.InstructedAmount.Amount";
    public static final String INSTRUCTED_AMOUNT_AMOUNT_NOT_FOUND = "Instructed Amount  Amount isn't present in the " +
            "payload:" + ErrorConstants.PATH_INSTRUCTED_AMOUNT;
    public static final String INSTRUCTED_AMOUNT_CURRENCY_MISMATCH = "Instructed Amount currency does not match the " +
            "initiated amount or currency:" + ErrorConstants.PATH_INSTRUCTED_AMOUNT_CURRENCY;
    public static final String PATH_INSTRUCTED_AMOUNT_CURRENCY = "Data.Initiation.InstructedAmount.Currency";
    public static final String INSTRUCTED_AMOUNT_CURRENCY_NOT_FOUND = "Instructed Amount Currency isn't present in " +
            "the payload:" + ErrorConstants.PATH_INSTRUCTED_AMOUNT;
    public static final String INSTRUCTED_AMOUNT_NOT_FOUND = "Instructed Amount isn't present in the payload:" +
            ErrorConstants.PATH_INSTRUCTED_AMOUNT;
    public static final String CREDITOR_ACC_SCHEME_NAME_MISMATCH = "Creditor Accounts Scheme does not match:" +
            ErrorConstants.PATH_CREDIT_ACCOUNT_SCHEME;
    public static final String CREDITOR_ACC_SCHEME_NAME_NOT_FOUND = "Creditor Accounts Scheme isn't present in the" +
            " request or in the consent.:" + ErrorConstants.PATH_CREDIT_ACCOUNT_SCHEME;
    public static final String CREDITOR_ACC_IDENTIFICATION_MISMATCH = "Creditor Account Identification does not match:"
            + ErrorConstants.PATH_CREDIT_ACCOUNT_IDENTIFICATION;
    public static final String CREDITOR_ACC_IDENTIFICATION_NOT_FOUND = "Creditor Account Identification isn't " +
            "present in the request or in the consent.:" + ErrorConstants.PATH_CREDIT_ACCOUNT_IDENTIFICATION;
    public static final String CREDITOR_ACC_NAME_MISMATCH = "Creditor Account Name does not match:" +
            ErrorConstants.PATH_CREDIT_ACCOUNT_NAME;
    public static final String CREDITOR_ACC_SEC_IDENTIFICATION_MISMATCH = "Creditor Account Secondary Identification" +
            " does not match:" + ErrorConstants.PATH_CREDIT_ACCOUNT_SEC_IDENTIFICATION;

    public static final String DEBTOR_ACC_SCHEME_NAME_MISMATCH = "Debtor Account Scheme name does not " +
            "match:" + ErrorConstants.PATH_DEBTOR_ACCOUNT_SCHEME;
    public static final String DEBTOR_ACC_SCHEME_NAME_NOT_FOUND = "Debtor Account Scheme name isn't present in the " +
            "request or in the consent:" + ErrorConstants.PATH_DEBTOR_ACCOUNT_SCHEME;
    public static final String DEBTOR_ACC_IDENTIFICATION_MISMATCH = "Debtor Account Identification does " +
            "not match:" + ErrorConstants.PATH_DEBTOR_ACCOUNT_IDENTIFICATION;
    public static final String DEBTOR_ACC_IDENTIFICATION_NOT_FOUND = "Debtor Account Identification isn't present " +
            "in the request or in the consent:" + ErrorConstants.PATH_DEBTOR_ACCOUNT_IDENTIFICATION;
    public static final String DEBTOR_ACC_NAME_MISMATCH = "Debtor Account Name does not match:" +
            ErrorConstants.PATH_DEBTOR_ACCOUNT_NAME;
    public static final String DEBTOR_ACC_SEC_IDENTIFICATION_MISMATCH = "Debtor Account Secondary Identification" +
            " does not match:" + ErrorConstants.PATH_DEBTOR_ACCOUNT_SECOND_IDENTIFICATION;
    public static final String PATH_DEBTOR_ACCOUNT_SECOND_IDENTIFICATION =
            "Data.Initiation.DebtorAccount.SecondaryIdentification";
    public static final String CREDITOR_ACC_NOT_FOUND = "Creditor Account isn't present in the request.:" +
            ErrorConstants.PATH_CREDIT_ACCOUNT;
    public static final String DEBTOR_ACC_MISMATCH = "Debtor Account isn't present in the request " +
            "or in the consent:" + ErrorConstants.PATH_DEBTOR_ACCOUNT;
    public static final String LOCAL_INSTRUMENT_MISMATCH = "Local Instrument Does Not Match:" +
            ErrorConstants.PATH_LOCAL_INSTRUMENT;
    public static final String TOKEN_REVOKE_ERROR = "Token revocation unsuccessful. :" +
            ErrorConstants.PATH_CUTOFF_DATE;
    public static final String CUT_OFF_DATE_ELAPSED = "Cut off time has elapsed :" +
            ErrorConstants.PATH_CUTOFF_DATE;
    public static final String MSG_INVALID_CONSENT_ID = "The requested consent-Id does not match with the consent-Id" +
            " bound to token:" +  ErrorConstants.PATH_CONSENT_ID;
    public static final String PAYMENT_CONSENT_STATE_INVALID = "Payment validation failed due to invalid consent" +
            " state.:" + ErrorConstants.PATH_STATUS;
    public static final String DATA_NOT_FOUND = "Data is not found or empty in the request.:" +
            ErrorConstants.PATH_DATA;
    public static final String INITIATION_NOT_FOUND = "Initiation is not found or empty in the request.:" +
            ErrorConstants.PATH_INITIATION;

    public static final String RISK_MISMATCH = "RISK Does Not Match.:" + ErrorConstants.PATH_RISK;
    public static final String RISK_NOT_FOUND = "RISK is not found or empty in the request.:" +
            ErrorConstants.PATH_RISK;

    public static final String INVALID_URI_ERROR = "Path requested is invalid. :" + ErrorConstants.PATH_URL;
    public static final String COF_CONSENT_STATE_INVALID = "Confirmation of Funds validation failed due to invalid" +
            " consent state.:" + ErrorConstants.PATH_STATUS;
    public static final String CONSENT_EXPIRED_ERROR = "Provided consent is expired. :"
            + ErrorConstants.PATH_EXPIRATION_DATE;
    public static final String MSG_MISSING_CLIENT_ID = "Missing mandatory parameter x-wso2-client-id.";
    public static final String RESOURCE_NOT_FOUND = "OB.Resource.NotFound";
    public static final String ACC_INITIATION_RETRIEVAL_ERROR = "Error occurred while handling the account initiation" +
            " retrieval request";
    public static final String INVALID_CONSENT_ID = "Invalid Consent Id found in the request";
    public static final String CONSENT_ID_NOT_FOUND = "Consent ID not available in consent data";
    public static final String FIELD_INVALID_DATE = "OB.Field.InvalidDate";
    public static final String EXPIRED_DATE_ERROR = "The ExpirationDateTime value has to be a future date.";
    // vrp
    public static final String MAXIMUM_INDIVIDUAL_AMOUNT_IS_MISSING = "Mandatory parameter MaximumIndividualAmount" +
            " Amount is missing in the payload.";
    public static final String MAXIMUM_INDIVIDUAL_AMOUNT_CURRENCY_IS_MISSING = "Mandatory parameter" +
            "Currency in MaximumIndividualAmount is missing in the payload";
    public static final String INVALID_AMOUNT = "Parameter in the payload for " +
            "Amount" + "is missing in the payload or its null or not a string";
    public static final String INVALID_CURRENCY = "Mandatory parameter " +
            "Currency is missing in the payload or its null or not a string";
    public static final String INVALID_PERIOD_ALIGNMENT = "Invalid value for period alignment in PeriodicLimits";
    public static final String MISSING_PERIOD_TYPE = "Mandatory parameter " +
            "period type is missing in the payload";
    public static final String INVALID_VALID_TO_DATE = "Valid to Date specified in the request is invalid";

    // new error constants
    public static final String INVALID_PARAMETER = "Parameter passed in is null , " +
            "empty or not a JSONObject";
    public static final String INVALID_DATE_TIME_FORMAT = "Date and Time  is not in correct JSON " +
            "ISO-8601 date-time format";
    public static final String INVALID_PARAMETER_PERIODIC_LIMITS = "Parameter passed in is null , " +
            "empty or not a JSONArray";
    public static final String MISSING_PERIOD_LIMITS = "Mandatory parameter " +
            "periodic limits is missing in the payload";

    public static final String PATH_VALID_TO_DATE = "Data.ControlParameters.ValidToDateTime";
    public static final String PATH_VALID_FROM_DATE = "Data.ControlParameters.ValidFromDateTime";
    public static final String PATH_MAXIMUM_INDIVIDUAL_AMOUNT = "Data.ControlParameters.MaximumIndividualAmount";
    public static final String PATH_MAXIMUM_INDIVIDUAL_AMOUNT_AMOUNT = "Data.ControlParameters." +
            "MaximumIndividualAmount.Amount";
    public static final String PATH_MAXIMUM_INDIVIDUAL_AMOUNT_CURRENCY = "Data.ControlParameters." +
            "MaximumIndividualAmount.Currency";
    public static final String PATH_PERIOD_LIMIT = "Data.ControlParameters.PeriodicLimits";
    public static final String PATH_PERIOD_LIMIT_AMOUNT = "Data.ControlParameters.PeriodicLimits.Amount";
    public static final String PATH_PERIOD_LIMIT_CURRENCY = "Data.ControlParameters.PeriodicLimits.Currency";
    public static final String PATH_PERIOD_TYPE = "Data.ControlParameters.PeriodicLimits.PeriodType";
    public static final String PATH_PERIOD_ALIGNMENT = "Data.ControlParameters.PeriodLimits.PeriodAlignment";
}

