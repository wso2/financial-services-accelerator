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
package com.wso2.openbanking.accelerator.consent.extensions.common;

/**
 * Constant class for consent extension module.
 */
public class ConsentExtensionConstants {

    public static final String ERROR_URI_FRAGMENT = "#error=";
    public static final String ERROR_DESCRIPTION_PARAMETER = "&error_description=";
    public static final String STATE_PARAMETER = "&state=";
    public static final String PRESERVE_CONSENT = "Consent.PreserveConsentLink";
    public static final String SENSITIVE_DATA_MAP = "sensitiveDataMap";
    public static final String LOGGED_IN_USER = "loggedInUser";
    public static final String SP_QUERY_PARAMS = "spQueryParams";
    public static final String SCOPES = "scopeString";
    public static final String APPLICATION = "application";
    public static final String REQUEST_HEADERS = "requestHeaders";
    public static final String REQUEST_URI = "redirectURI";
    public static final String USERID = "userId";
    public static final String CONSENT_ID = "ConsentId";
    public static final String CONSENT_ID_VALIDATION = "ConsentId";
    public static final String CLIENT_ID = "clientId";
    public static final String REGULATORY = "regulatory";
    public static final String CONSENT_RESOURCE = "consentResource";
    public static final String AUTH_RESOURCE = "authResource";
    public static final String META_DATA = "metaDataMap";
    public static final String TYPE = "type";
    public static final String X_IDEMPOTENCY_KEY = "x-idempotency-key";
    public static final String IS_VALID = "isValid";
    public static final String HTTP_CODE = "httpCode";
    public static final String ERRORS = "errors";
    public static final String PAYMENTS = "payments";
    public static final String VRP = "vrp";

    public static final String DATA = "Data";
    public static final String INITIATION = "Initiation";
    public static final String STATUS = "Status";
    public static final String STATUS_UPDATE_TIME = "StatusUpdateDateTime";
    public static final String CREATION_DATE_TIME = "CreationDateTime";
    public static final String FUNDSCONFIRMATIONS = "fundsconfirmations";
    public static final String SCHEME_NAME = "SchemeName";
    public static final String IDENTIFICATION = "Identification";
    public static final String NAME = "Name";
    public static final String SECONDARY_IDENTIFICATION = "SecondaryIdentification";
    public static final String OB_SORT_CODE_ACCOUNT_NUMBER = "OB.SortCodeAccountNumber";
    public static final String SORT_CODE_ACCOUNT_NUMBER = "SortCodeAccountNumber";
    public static final int ACCOUNT_IDENTIFICATION_LENGTH = 14;
    public static final String SORT_CODE_PATTERN = "^[0-9]{6}[0-9]{8}$";
    public static final String CUSTOM_LOCAL_INSTRUMENT_VALUES = "Consent.CustomLocalInstrumentValues";
    public static final String AMOUNT = "Amount";
    public static final String CREDITOR_ACC = "CreditorAccount";
    public static final String DEBTOR_ACC = "DebtorAccount";
    public static final String INSTRUCTED_AMOUNT = "InstructedAmount";
    public static final String LOCAL_INSTRUMENT = "LocalInstrument";
    public static final String ACCOUNT_CONSENT_GET_PATH = "account-access-consents";
    public static final String ACCOUNT_CONSENT_DELETE_PATH = "account-access-consents/";
    public static final String UUID_REGEX =
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
    public static final String INTERACTION_ID_HEADER = "x-fapi-interaction-id";
    public static final String PERMISSIONS = "Permissions";
    public static final String COF_CONSENT_PATH = "funds-confirmation-consents";
    public static final String PAYMENT_CONSENT_PATH = "payment-consents";
    public static final String CONSENT_KEY = "OauthConsentKey";
    public static final String REQUEST_KEY = "AuthRequestKey";
    public static final String CUTOFF_DATE_ENABLED = "Consent.PaymentRestrictions.CutOffDateTime.Enabled";
    public static final String MAX_INSTRUCTED_AMOUNT = "Consent.PaymentRestrictions" +
            ".MaximumInstructedAmount";
    public static final String DAILY_CUTOFF = "Consent.PaymentRestrictions.CutOffDateTime" +
            ".DailyCutOffTime";
    public static final String REJECT = "REJECT";
    public static final String CUTOFF_DATE_POLICY = "Consent.PaymentRestrictions.CutOffDateTime" +
            ".CutOffDateTimePolicy";
    public static final String ACCEPT = "ACCEPT";
    public static final String ZONE_ID = "ZoneId";
    public static final String IS_ERROR = "isError";
    public static final String ACCOUNTS = "accounts";
    public static final String CONSENT_DATA = "consentData";
    public static final String TITLE = "title";
    public static final String DATA_SIMPLE = "data";
    public static final String DEBTOR_ACCOUNT_ID = "AccountId";
    public static final String ACCOUNT_ID = "account_id";
    public static final String DATA_REQUESTED = "data_requested";
    public static final String PAYMENT_ACCOUNT = "paymentAccount";
    public static final String COF_ACCOUNT = "cofAccount";
    public static final String AWAITING_AUTH_STATUS = "awaitingAuthorisation";
    public static final String CUT_OFF_DATE_TIME = "CutOffDateTime";
    public static final String IDEMPOTENCY_KEY = "IdempotencyKey";
    public static final int NUMBER_OF_PARTS_IN_JWS = 3;
    public static final String CLAIMS = "claims";
    public static final String[] CLAIM_FIELDS = new String[]{"userinfo", "id_token"};
    public static final String OPENBANKING_INTENT_ID = "openbanking_intent_id";
    public static final String VALUE = "value";
    public static final String AUTHORIZED_STATUS = "authorised";
    public static final String EXPIRATION_DATE = "ExpirationDateTime";
    public static final String EXPIRATION_DATE_TITLE = "Expiration Date Time";
    public static final String INSTRUCTED_AMOUNT_TITLE = "Instructed Amount";
    public static final String CURRENCY_TITLE = "Currency";
    public static final String CURRENCY = "Currency";
    public static final String AMOUNT_TITLE = "Amount";
    public static final String END_TO_END_IDENTIFICATION_TITLE = "End to End Identification";
    public static final String END_TO_END_IDENTIFICATION = "EndToEndIdentification";
    public static final String INSTRUCTION_IDENTIFICATION_TITLE = "Instruction Identification";
    public static final String INSTRUCTION_IDENTIFICATION = "InstructionIdentification";
    public static final String REJECTED_STATUS = "rejected";
    public static final String OPEN_ENDED_AUTHORIZATION = "Open Ended Authorization Requested";
    public static final String DEBTOR_ACC_TITLE = "Debtor Account";
    public static final String SCHEME_NAME_TITLE = "Scheme Name";
    public static final String IDENTIFICATION_TITLE = "Identification";
    public static final String NAME_TITLE = "Name";
    public static final String SECONDARY_IDENTIFICATION_TITLE = "Secondary Identification";
    public static final String CREDITOR_ACC_TITLE = "Creditor Account";
    public static final String CONSENT_TYPE = "consent_type";
    public static final String TRANSACTION_FROM_DATE = "TransactionFromDateTime";
    public static final String TRANSACTION_TO_DATE = "TransactionToDateTime";
    public static final String TRANSACTION_FROM_DATE_TITLE = "Transaction From Date Time";
    public static final String TRANSACTION_TO_DATE_TITLE = "Transaction To Date Time";
    public static final String PAYMENT_TYPE_TITLE = "Payment Type";
    public static final String CURRENCY_OF_TRANSFER_TITLE = "Currency of Transfer";
    public static final String CURRENCY_OF_TRANSFER = "CurrencyOfTransfer";
    public static final String INTERNATIONAL_PAYMENTS = "International Payments";
    public static final String DOMESTIC_PAYMENTS = "Domestic Payments";
    public static final String CREATED_STATUS = "created";
    public static final String IS_VALID_PAYLOAD = "isValidPayload";
    public static final String ERROR_CODE = "errorCode";
    public static final String ERROR_MESSAGE = "errorMessage";
    public static final String RISK = "Risk";
    public static final String COF_CONSENT_INITIATION_PATH = "/funds-confirmation-consents";
    public static final String COF_CONSENT_CONSENT_ID_PATH = "/funds-confirmation-consents/{ConsentId}";
    public static final String COF_SUBMISSION_PATH = "/funds-confirmations";
    public static final String ACCOUNT_ID_LIST = "AccountIds";
    public static final String CREATION_TIME = "CreationDateTime";
    public static final String LINKS = "Links";
    public static final String SELF = "Self";
    public static final String META = "Meta";
    public static final String ACCOUNTS_SELF_LINK = "Consent.AccountAPIURL";
    public static final String PAYMENT_SELF_LINK = "Consent.PaymentAPIURL";
    public static final String COF_SELF_LINK = "Consent.FundsConfirmationAPIURL";
    public static final String VRP_SELF_LINK = "Consent.VRPAPIURL";
    public static final String REVOKED_STATUS = "revoked";
    public static final String DISPLAY_NAME = "display_name";
    public static final String ACCOUNT_DATA = "account_data";
    public static final String SELECTED_ACCOUNT = "selectedAccount";
    public static final String PAYMENT_COF_PATH = "funds-confirmation";
    public static final String AWAITING_UPLOAD_STATUS = "awaitingUpload";
    public static final String OB_REVOKED_STATUS = "Revoked";
    public static final String OB_REJECTED_STATUS = "Rejected";
    public static final String OB_AUTHORIZED_STATUS = "Authorised";
    public static final String OB_AWAITING_AUTH_STATUS = "AwaitingAuthorisation";
    public static final String OB_AWAITING_UPLOAD_STATUS = "AwaitingUpload";

    //VRP Constants
    public static final String VRP_CONSENT_PATH = "domestic-vrp-consents";
    public static final String VRP_PAYMENT = "vrp-payment";
    public static final String PAID_AMOUNT = "paid-amount";
    public static final String LAST_PAYMENT_DATE = "last-payment-date";
    public static final String AUTH_TYPE_AUTHORIZATION = "authorization";
    public static final String CONTROL_PARAMETERS = "ControlParameters";
    public static final String MAXIMUM_INDIVIDUAL_AMOUNT = "MaximumIndividualAmount";
    public static final String MAXIMUM_INDIVIDUAL_AMOUNT_CURRENCY = "MaximumIndividualAmount.Amount.Currency";
    public static final String PERIODIC_LIMITS = "PeriodicLimits";
    public static final String PERIOD_AMOUNT_LIMIT = "Amount";
    public static final String PERIOD_LIMIT_CURRENCY = "PeriodicLimits.Currency";

    //vrp period alignment
    public static final String PERIOD_ALIGNMENT = "PeriodAlignment";

    // vrp periodic alignment types
    public static final String CONSENT = "Consent";
    public static final String CALENDAR = "Calendar";

    //vrp periodicLimits
    public static final String PERIOD_TYPE = "PeriodType";

    //vrp periodic types
    public static final String DAY = "Day";
    public static final String WEEK = "Week";
    public static final String FORTNIGHT = "Fortnight";
    public static final String MONTH = "Month";
    public static final String HALF_YEAR = "Half-year";
    public static final String YEAR = "Year";
    public static final String VALID_TO_DATE_TIME = "ValidToDateTime";
    public static final String VALID_FROM_DATE_TIME = "ValidFromDateTime";
    public static final String VRP_RESPONSE_PROCESS_PATH = "vrp-response-process";

    // vrp authorization flow constants
    public static final String DOMESTIC_VRP = "Domestic VRP";
    public static final String CONTROL_PARAMETER_MAX_INDIVIDUAL_AMOUNT_TITLE = "Maximum amount per payment";
    public static final String CONTROL_PARAMETER_VALID_TO_DATE_TITLE = "Valid to date and time";
    public static final String CONTROL_PARAMETER_PERIOD_ALIGNMENT_TITLE = "Period Alignment";
    public static final String CONTROL_PARAMETER_PERIOD_TYPE_TITLE = "Period Type";
    public static final Object CONTROL_PARAMETER_AMOUNT_TITLE = "Maximum payment amount per ";
    public static final String VRP_ACCOUNT = "vrpAccount";
    public static final Object CONTROL_PARAMETER_VALID_FROM_DATE_TITLE = "Valid from date and time";
}
