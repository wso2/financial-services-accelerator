/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.consent.mgt.service.constants;

/**
 * Consent Core Service Constants.
 */
public class ConsentCoreServiceConstants {

    public static final String TEST = "test123v3";
    public static final String CONSENT_RESOURCE = "ConsentResource";
    public static final String DETAILED_CONSENT_RESOURCE = "DetailedConsentResource";
    public static final String CONSENT_AMENDMENT_HISTORY_RESOURCE = "ConsentAmendmentHistory";
    public static final String CONSENT_AMENDMENT_TIME = "ConsentAmendmentTime";
    public static final String ACTIVE_MAPPING_STATUS = "active";
    public static final String INACTIVE_MAPPING_STATUS = "inactive";
    public static final String RECEIPT = "RECEIPT";
    public static final String EXPIRY_TIME = "EXPIRY_TIME";
    public static final String UPDATED_TIME = "UPDATED_TIME";
    public static final String CURRENT_STATUS = "CURRENT_STATUS";
    public static final String MAPPING_STATUS = "MAPPING_STATUS";
    public static final String AUTHORIZATION_STATUS = "AUTHORIZATION_STATUS";
    public static final String CONSENT_AMENDED_STATUS = "amended";
    public static final String TYPE_CONSENT_BASIC_DATA = "ConsentData";
    public static final String TYPE_CONSENT_ATTRIBUTES_DATA = "ConsentAttributesData";
    public static final String TYPE_CONSENT_MAPPING_DATA = "ConsentMappingData";
    public static final String TYPE_CONSENT_AUTH_RESOURCE_DATA = "ConsentAuthResourceData";
    public static final String CREATE_CONSENT_REASON = "Create consent";
    public static final String CREATE_EXCLUSIVE_AUTH_CONSENT_REASON = "Create exclusive authorization consent";
    public static final String CONSENT_FILE_UPLOAD_REASON = "Upload consent file";
    public static final String USER_ACCOUNTS_BINDING_REASON = "Bind user accounts to consent";
    public static final String CONSENT_REVOKE_REASON = "Revoke the consent";
    public static final String CONSENT_REAUTHORIZE_REASON = "Reauthorize consent";
    public static final String CONSENT_AMEND_REASON = "Amend consent";
    public static final String DEFAULT_PERMISSION_VALUE = "n/a";
    public static final String ADDITIONAL_AUTHORIZATION_RESOURCES = "AdditionalAuthorizationResources";
    public static final String ADDITIONAL_MAPPING_RESOURCES = "AdditionalMappingResources";
    public static final String AMENDMENT_REASON_CONSENT_AMENDMENT_FLOW = "ConsentAmendmentFlow";
    public static final String AMENDMENT_REASON_CONSENT_REVOCATION = "ConsentRevocation";
    public static final String CONSENT_REVOKE_STATUS = "revoked";
    // Error Constants
    public static final String ORGANIZATION_MISMATCH_ERROR_MSG =
            "OrgInfo does not match, please provide the correct OrgInfo";
    public static final String DATA_INSERTION_ROLLBACK_ERROR_MSG = "Error occurred while inserting data. Rolling" +
            " back the transaction";
    public static final String DATABASE_CONNECTION_CLOSE_LOG_MSG = "Closing database connection";
    public static final String TRANSACTION_COMMITTED_LOG_MSG = "Transaction committed";
    public static final String CANNOT_PROCEED_WITH_CONSENT_CREATION = "Cannot proceed since client ID, receipt, " +
            "consent type or consent status is missing.";
    public static final String CANNOT_PROCEED_WITH_IMPLICIT_AUTH = "Cannot proceed with implicit authorization" +
            " creation without authorizationStatus or authorizationType or userId provided";
    public static final String CREATE_EXCLUSIVE_CONSENT_MANDATORY_PARAMETER_MISSING_ERROR = "One or more of following" +
            " data are missing (Client ID, receipt, consent type, consent status, auth status, auth type, applicable " +
            "existing consent status, new existing consent status, new current consent status), cannot proceed";
    public static final String DATA_RETRIEVE_ERROR_MSG = "Error occurred while retrieving data, cannot find the " +
            "requested data with the given parameters";
    public static final String DATA_UPDATE_ROLLBACK_ERROR_MSG = "Error occurred while updating consent data. Rolling " +
            "back the transaction";
    public static final String NEW_CONSENT_STATUS_OR_APPLICABLE_STATUS_MISSING_ERROR = "New consent status or " +
            "applicable status for file upload is missing. Cannot proceed";

    public static final String ATTRIBUTE_UPDATE_ERROR_MSG = "Error occurred while updating consent attributes. " +
            "Rolling back the transaction";
    public static final String DATA_DELETE_ROLLBACK_ERROR_MSG = "Error occurred while deleting data. Rolling " +
            "back the transaction";
    public static final String CONSENT_ATTRIBUTES_DELETE_ERROR_MSG = "Error occurred while deleting consent " +
            "attributes in the database";

    public static final String CONSENT_ID_MISSING_ERROR_MSG = "Consent ID is missing, cannot proceed";
    public static final String CONSENT_FILE_MISSING_ERROR_MSG = "Consent ID or Consent File content is missing. " +
            "Cannot proceed.";
    public static final String CONSENT_INVALID_STATUS_ERROR_MSG = "The consent is not in required state to proceed";
    public static final String AUTH_DETAILS_MISSING_ERROR_MSG = "Consent ID, authorization type, user ID " +
            "or authorization status is missing, cannot proceed";
    public static final String AUTH_ID_MISSING_ERROR_MSG = "Authorization ID is missing, cannot proceed";
    public static final String AUTH_RESOURCE_SEARCH_ERROR_MSG = "Error occurred while searching authorization" +
            " resources";
    public static final String AUTH_STATUS_MISSING_ERROR_MSG = "Authorization ID or newAuthorizationStatus" +
            " is missing. Cannot proceed.";
    public static final String AUTH_USER_ID_MISSING_ERROR_MSG = "Authorization ID or user ID is missing. " +
            "Cannot proceed.";
    public static final String USER_BIND_DETAILS_MISSING_ERROR_MSG = "Consent ID, client ID, consent type, user ID," +
            " authorization ID, new authorization status or new consent status is missing, cannot proceed.";
    public static final String ACC_ID_PERMISSION_DETAILS_MISSING_ERROR = "Account IDs and relative permissions" +
            " are not present, cannot proceed";
    public static final String CONSENT_UPDATE_DETAILS_MISSING_ERROR = "Consent ID, userId or newConsentStatus" +
            " is missing. Cannot proceed.";
    public static final String ACC_MAPPING_DETAILS_MISSING_ERROR = "Authorization ID, accountID/permission map" +
            " is not found, cannot proceed";
    public static final String ACC_MAPPING_ID_MISSING_ERROR_MSG = "Account mapping IDs are not provided, " +
            "cannot proceed";
    public static final String CONSENT_STATUS_MISSING_ERROR_MSG = "New consent status is missing," +
            " cannot proceed";

    public static final String USER_ID_MISSING_ERROR_MSG = "User ID is required for token revocation, cannot proceed";
    public static final String USER_ID_MISMATCH_ERROR_MSG = "Requested UserID %s and Consent UserID  %s do not match," +
            " cannot proceed.";
    public static final String REVOKE_DETAILS_MISSING_ERROR_MSG = "Client ID, new consent status, consent type," +
            " user ID or applicable consent status to revoke is missing, cannot proceed";
    public static final String RE_AUTH_DETAILS_MISSING_ERROR_MSG = "Consent ID, auth ID, user ID, account permissions" +
            " map, applicable consent status, new consent status or current consent status is not present," +
            " cannot proceed";
    public static final String RE_AUTH_RESOURCE_DETAILS_MISSING_ERROR = "Consent ID, user ID, account" +
            " permissions map, current consent status, new consent status, new existing auth status, " +
            "new auth status or new auth type is not present, cannot proceed";
    public static final String CONSENT_ATTRIBUTES_MISSING_ERROR_MSG = "ConsentID or consentAttributes is missing," +
            " cannot proceed";
    public static final String CONSENT_ATTRIBUTE_KEYS_MISSING_ERROR_MSG = "Consent ID or consent attributes keys" +
            " are missing, cannot proceed";
    public static final String CONSENT_ATTRIBUTE_NAME_MISSING_ERROR_MSG = "Attribute name is not provided," +
            " cannot proceed";
    public static final String ATTRIBUTE_NAME_VALUE_MISSING_ERROR_MSG = "Attribute name or value is not provided," +
            " cannot proceed";
    public static final String ATTRIBUTE_MAP_MISSING_ERROR_MSG = "Consent ID or attributes map is not provided," +
            " cannot proceed";
    public static final String ATTRIBUTE_LIST_MISSING_ERROR_MSG = "Consent ID or attributes list is not provided," +
            " cannot proceed";
    public static final String AUDIT_RECORD_SEARCH_ERROR_MSG = "Error occurred while searching audit records";
    public static final String AMEND_DETAILS_MISSING_ERROR_MSG = "Consent ID or detailed consent resource or " +
            "amendment reason or amended timestamp in consent history resource is empty/zero";
    public static final String DETAIL_CONSENT_SEARCH_ERROR_MSG = "Error occurred while searching detailed consents";
    public static final String CONSENT_DATA_MISSING_ERROR_MSG = "Consent ID or both consent receipt and consent" +
            " validity period are not provided, cannot proceed";
    public static final String CONSENT_EXPIRY_TIME_MISSING_ERROR = " Expriy Time empty, cannot proceed";

    public static final String CONSENT_EXPIRY_TIME_BEFORE_CURRENT_TIMESTAMP_ERROR = " Expiry Time is before the " +
            "current timestatmp, please consider a time in the future ";

    public static final String CONSENT_ALREADY_REVOKED = "Consent is already revoked, cannot proceed";

    public static final String DETAILED_CONSENT_DATA_MISSING_ERROR_MSG = "Auth ID, user ID, account permissions map," +
            " new consent status or new consent attributes is not present, cannot proceed";
    public static final String DATA_DELETION_ROLLBACK_ERROR_MSG = "Error occurred while deleting data. Rolling" +
            " back the transaction";
}
