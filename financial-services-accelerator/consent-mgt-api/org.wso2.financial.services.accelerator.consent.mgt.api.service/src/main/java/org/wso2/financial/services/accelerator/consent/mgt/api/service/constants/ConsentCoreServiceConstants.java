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

package org.wso2.financial.services.accelerator.consent.mgt.api.service.constants;

/**
 * Consent Core Service Constants.
 */
public class ConsentCoreServiceConstants {


    public static final String DETAILED_CONSENT_RESOURCE = "DetailedConsentResource";
    public static final String CONSENT_AMENDMENT_HISTORY_RESOURCE = "ConsentAmendmentHistory";
    public static final String RECEIPT = "RECEIPT";
    public static final String EXPIRY_TIME = "EXPIRY_TIME";
    public static final String UPDATED_TIME = "UPDATED_TIME";
    public static final String CURRENT_STATUS = "CURRENT_STATUS";
    public static final String MAPPING_STATUS = "MAPPING_STATUS";
    public static final String AUTHORIZATION_STATUS = "AUTHORIZATION_STATUS";
    public static final String TYPE_CONSENT_BASIC_DATA = "ConsentData";
    public static final String TYPE_CONSENT_ATTRIBUTES_DATA = "ConsentAttributesData";
    public static final String TYPE_CONSENT_MAPPING_DATA = "ConsentMappingData";
    public static final String TYPE_CONSENT_AUTH_RESOURCE_DATA = "ConsentAuthResourceData";
    public static final String CREATE_CONSENT_REASON = "Create consent";
    public static final String CONSENT_REVOKE_STATUS = "revoked";
    // Error Constants

    public static final String DATA_INSERTION_ROLLBACK_ERROR_MSG =
            "Error occurred while inserting data. Rolling back the transaction";
    public static final String TRANSACTION_COMMITTED_LOG_MSG = "Transaction committed";

    public static final String DATA_RETRIEVE_ERROR_MSG =
            "Error occurred while retrieving data, cannot find the requested data with the given parameters";
    public static final String DATA_UPDATE_ROLLBACK_ERROR_MSG =
            "Error occurred while updating consent data. Rolling back the transaction";


    public static final String ATTRIBUTE_UPDATE_ERROR_MSG =
            "Error occurred while updating consent attributes. Rolling back the transaction";
    public static final String DATA_DELETE_ROLLBACK_ERROR_MSG =
            "Error occurred while deleting data. Rolling back the transaction";
    public static final String CONSENT_ATTRIBUTES_DELETE_ERROR_MSG =
            "Error occurred while deleting consent attributes in the database";

    public static final String CONSENT_ID_MISSING_ERROR_MSG = "Consent ID is missing, cannot proceed";

    public static final String CONSENT_ATTRIBUTES_MISSING_ERROR_MSG =
            "ConsentID or consentAttributes is missing, cannot proceed";
    public static final String CONSENT_ATTRIBUTE_KEYS_MISSING_ERROR_MSG =
            "Consent ID or consent attributes keys are missing, cannot proceed";
    public static final String ATTRIBUTE_MAP_MISSING_ERROR_MSG =
            "Consent ID or attributes map is not provided, cannot proceed";
    public static final String ATTRIBUTE_LIST_MISSING_ERROR_MSG =
            "Consent ID or attributes list is not provided, cannot proceed";

    public static final String AUDIT_RECORD_SEARCH_ERROR_MSG = "Error occurred while searching audit records";
    public static final String AMEND_DETAILS_MISSING_ERROR_MSG =
            "Consent ID or detailed consent resource or amendment reason or amended timestamp in consent history " +
                    "resource is empty/zero";
    public static final String DETAIL_CONSENT_SEARCH_ERROR_MSG = "Error occurred while searching detailed consents";

    public static final String CONSENT_EXPIRY_TIME_BEFORE_CURRENT_TIMESTAMP_ERROR =
            " Expiry Time is before the current timestatmp, please consider a time in the future ";

    public static final String CONSENT_ALREADY_REVOKED = "Consent is already revoked, cannot proceed";

    public static final String DATA_DELETION_ROLLBACK_ERROR_MSG =
            "Error occurred while deleting data. Rolling back the transaction";
}
