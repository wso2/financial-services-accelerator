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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.common;

/**
 * Constant class for consent extension module.
 */
public class ConsentExtensionConstants {

    //Common Constants
    public static final String ACCOUNTS = "accounts";
    public static final String PAYMENTS = "payments";
    public static final String FUNDS_CONFIRMATIONS = "fundsconfirmations";
    public static final String DEFAULT = "default";
    public static final String ACCOUNT_CONSENT_PATH = "account-access-consents";
    public static final String COF_CONSENT_PATH = "funds-confirmation-consents";
    public static final String PAYMENT_CONSENT_PATH = "payment-consents";
    public static final String CONSENT_DATA = "consentData";
    public static final String TITLE = "title";
    public static final String CONSENT_ID = "ConsentId";
    public static final String ACCOUNT_ID = "account_id";
    public static final String DATA = "Data";
    public static final String CONSENT_TYPE = "consent_type";
    public static final String AWAIT_AUTHORISE_STATUS = "AwaitingAuthorisation";
    public static final String AWAIT_UPLOAD_STATUS = "AwaitingUpload";
    public static final String AUTHORIZED_STATUS = "Authorised";
    public static final String REVOKED_STATUS = "Revoked";
    public static final String REJECTED_STATUS = "Rejected";
    public static final String CREATED_STATUS = "Created";
    public static final String DEFAULT_AUTH_TYPE = "authorisation";
    public static final String PERMISSIONS = "Permissions";
    public static final String EXPIRATION_DATE = "ExpirationDateTime";
    public static final String EXPIRATION_DATE_TITLE = "Expiration Date Time";
    public static final String TRANSACTION_FROM_DATE = "TransactionFromDateTime";
    public static final String TRANSACTION_FROM_DATE_TITLE = "Transaction From Date Time";
    public static final String TRANSACTION_TO_DATE = "TransactionToDateTime";
    public static final String TRANSACTION_TO_DATE_TITLE = "Transaction To Date Time";
    public static final String INITIATION = "Initiation";
    public static final String PAYMENT_TYPE_TITLE = "Payment Type";
    public static final String CURRENCY_OF_TRANSFER = "CurrencyOfTransfer";
    public static final String CURRENCY_OF_TRANSFER_TITLE = "Currency of Transfer";
    public static final String INTERNATIONAL_PAYMENTS = "International Payments";
    public static final String DOMESTIC_PAYMENTS = "Domestic Payments";
    public static final String END_TO_END_IDENTIFICATION = "EndToEndIdentification";
    public static final String END_TO_END_IDENTIFICATION_TITLE = "End to End Identification";
    public static final String INSTRUCTION_IDENTIFICATION = "InstructionIdentification";
    public static final String INSTRUCTION_IDENTIFICATION_TITLE = "Instruction Identification";
    public static final String DEBTOR_ACC = "DebtorAccount";
    public static final String DEBTOR_ACC_TITLE = "Debtor Account";
    public static final String CREDITOR_ACC = "CreditorAccount";
    public static final String CREDITOR_ACC_TITLE = "Creditor Account";
    public static final String SCHEME_NAME = "SchemeName";
    public static final String SCHEME_NAME_TITLE = "Scheme Name";
    public static final String IDENTIFICATION = "Identification";
    public static final String IDENTIFICATION_TITLE = "Identification";
    public static final String NAME = "Name";
    public static final String NAME_TITLE = "Name";
    public static final String SECONDARY_IDENTIFICATION = "SecondaryIdentification";
    public static final String SECONDARY_IDENTIFICATION_TITLE = "Secondary Identification";
    public static final String OPEN_ENDED_AUTHORIZATION = "Open Ended Authorisation Requested";
    public static final String INSTRUCTED_AMOUNT = "InstructedAmount";
    public static final String INSTRUCTED_AMOUNT_TITLE = "Instructed Amount";
    public static final String CURRENCY = "Currency";
    public static final String CURRENCY_TITLE = "Currency";
    public static final String AMOUNT = "Amount";
    public static final String AMOUNT_TITLE = "Amount";
    public static final String COMMON_AUTH_ID = "commonAuthId";

    //Consent Auth Servlet Constants
    public static final String DEBTOR_ACCOUNT_ID = "AccountId";
    public static final String AUTH_ACCOUNT_ID = "account_id";
    public static final String DATA_REQUESTED = "data_requested";
    public static final String ACCOUNT_DATA = "account_data";
    public static final String SELECTED_ACCOUNT = "selectedAccount";
    public static final String DISPLAY_NAME = "display_name";
    public static final String PAYMENT_ACCOUNT = "paymentAccount";
    public static final String COF_ACCOUNT = "cofAccount";
    public static final String PRIMARY = "primary";
    public static final String ACCOUNT_IDS = "accountIds";
    public static final String ERROR = "error";
    public static final String ERROR_CODE = "code";
    public static final String ERROR_MSG = "message";
    public static final String OPERATION = "operation";
    public static final String ERROR_DESCRIPTION = "description";
    public static final String STATE = "state";
    public static final String REDIRECT_URI = "redirect_uri";

    public static final String ERROR_URI_FRAGMENT = "#error=";
    public static final String ERROR_DESCRIPTION_PARAMETER = "&error_description=";
    public static final String STATE_PARAMETER = "&state=";

    //Consent Admin Handler Constants
    public static final String CONSENT_IDS = "consentIds";
    public static final String CLIENT_IDS = "clientIds";
    public static final String CONSENT_TYPES = "consentTypes";
    public static final String CONSENT_STATUSES = "consentStatuses";
    public static final String USER_IDS = "userIds";
    public static final String FROM_TIME = "fromTime";
    public static final String TO_TIME = "toTime";
    public static final String LIMIT = "limit";
    public static final String OFFSET = "offset";
    public static final String COUNT = "count";
    public static final String TOTAL = "total";
    public static final String METADATA = "metadata";
    public static final String HISTORY_ID = "historyId";
    public static final String AMENDED_REASON = "amendedReason";
    public static final String AMENDED_TIME = "amendedTime";
    public static final String CURRENT_CONSENT = "currentConsent";
    public static final String AMENDMENT_HISTORY = "consentAmendmentHistory";
    public static final String AMENDMENT_COUNT = "amendmentCount";
    public static final String STATUS_AUDIT_ID = "statusAuditId";
    public static final String ACTION_TIME = "actionTime";
    public static final String REASON = "reason";
    public static final String ACTION_BY = "actionBy";
    public static final String PREVIOUS_STATUS = "previousStatus";
    public static final String CONSENT_FILE = "consentFile";
    public static final String CONSENT_REVOKE_FROM_DASHBOARD_REASON = "Revoke the consent from dashboard";
    public static final String CLAIMS = "claims";
    public static final String USER_INFO = "userinfo";
    public static final String ID_TOKEN = "id_token";
    public static final String OB_INTENT_ID = "openbanking_intent_id";
    public static final String VALUE = "value";
    public static final String CREATION_DATE_TIME = "CreationDateTime";
    public static final String STATUS_UPDATE_DATE_TIME = "StatusUpdateDateTime";
    public static final String STATUS = "Status";
    public static final String RECEIPT = "receipt";
    public static final String CLIENT_ID = "clientId";
    public static final String CURRENT_STATUS = "currentStatus";
    public static final String CONSENT_FREQUENCY = "consentFrequency";
    public static final String VALIDITY_PERIOD = "validityPeriod";
    public static final String CREATED_TIMESTAMP = "createdTimestamp";
    public static final String UPDATED_TIMESTAMP = "updatedTimestamp";
    public static final String RECURRING_INDICATOR = "recurringIndicator";
    public static final String CONSENT_ATTRIBUTES = "consentAttributes";
    public static final String RESOURCE = "resource";
    public static final String AUTH_ID = "authorizationId";
    public static final String CC_CONSENT_ID = "consentId";
    public static final String USER_ID = "userId";
    public static final String USER_ID_PARAM = "userID";
    public static final String AUTH_STATUS = "authorizationStatus";
    public static final String AUTH_TYPE = "authorizationType";
    public static final String UPDATE_TIME = "updatedTime";
    public static final String AUTH_RESOURCES = "authorizationResources";
    public static final String MAPPING_ID = "mappingId";
    public static final String PERMISSION = "permission";
    public static final String MAPPING_STATUS = "mappingStatus";
    public static final String MAPPING_RESOURCES = "consentMappingResources";

    //Consent Authorize Constants
    public static final String IS_ERROR = "isError";
    public static final String TYPE = "type";
    public static final String APPLICATION = "application";
    public static final String SENSITIVE_DATA_MAP = "sensitiveDataMap";
    public static final String LOGGED_IN_USER = "loggedInUser";
    public static final String SP_QUERY_PARAMS = "spQueryParams";
    public static final String SCOPES = "scopeString";
    public static final String REQUEST_HEADERS = "requestHeaders";
    public static final String REQUEST_URI = "redirectURI";
    public static final String META_DATA = "metaDataMap";
    public static final String RESOURCE_PATH = "ResourcePath";
    public static final String CONSENT_RESOURCE = "consentResource";
    public static final String AUTH_RESOURCE = "authResource";
    public static final String REGULATORY = "regulatory";
    public static final String HAS_APPROVED_ALWAYS = "hasApprovedAlways";
    public static final String USER = "user";
    public static final String SESSION_DATA_KEY = "sessionDataKey";
    public static final String SESSION_DATA_KEY_CONSENT = "sessionDataKeyConsent";
    public static final String CONSENT = "consent";
    public static final String LOCATION = "Location";
    public static final int STATUS_FOUND = 302;
    public static final String APPROVAL = "approval";
    public static final String COOKIES = "cookies";
    public static final String OPENID_SCOPE = "openid";
    public static final String PERSIST_PAYLOAD = "persistPayload";

    // Consent Validate Constants
    public static final String HEADERS = "headers";
    public static final String BODY = "body";
    public static final String ELECTED_RESOURCE = "electedResource";
    public static final String RESOURCE_PARAMS = "resourceParams";
    public static final String ADDITIONAL_CONSENT_INFO = "additionalConsentInfo";
    public static final String CONSENT_INFO = "consentInformation";
}
