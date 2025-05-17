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
package org.wso2.financial.services.accelerator.consent.mgt.dao.constants;

/**
 * Enum representing standardized error codes used throughout the Consent Management component.
 *
 * <p>Each error code is associated with a unique identifier, a short message for client-facing
 * communication, and a detailed description to aid in troubleshooting and development.</p>
 *
 * <p>Error codes are organized by functional area with the following prefixes:</p>
 * <ul>
 *     <li><strong>CMGT_10000</strong>: Payload validation errors</li>
 *     <li><strong>CMGT_1xxx0</strong>: BAD_REQUEST Status</li>
 *     <li><strong>CMGT_1xxx1</strong>: NOT_FOUND Status</li>
 *     <li><strong>CMGT_10001 – 10025</strong>: Errors related to Consent resources in database level</li>
 *     <li><strong>CMGT_10025 – 10050</strong>: Errors related to Consent resources in dao level</li>
 *     <li><strong>CMGT_10050 – 10075</strong>: Errors related to Consent resources in service level</li>
 *     <li><strong>CMGT_10075 – 10099</strong>: Errors related to Consent resources in api level</li>
 *     <li><strong>CMGT_11000 – 11025</strong>: Consent attribute-specific errors in database level</li>
 *     <li><strong>CMGT_11025 – 11050</strong>: Consent attribute-specific errors in dao level</li>
 *     <li><strong>CMGT_11050 – 11075</strong>: Consent attribute-specific errors in service level</li>
 *     <li><strong>CMGT_11075 – 11099</strong>: Consent attribute-specific errors in api level</li>
 *     <li><strong>CMGT_12000 – 12025</strong>: Authorization resource-specific errors in database level</li>
 *     <li><strong>CMGT_12025 – 12050</strong>: Authorization resource-specific errors in dao level</li>
 *     <li><strong>CMGT_12050 – 12075</strong>: Authorization resource-specific errors in service level</li>
 *     <li><strong>CMGT_12075 – 12099</strong>: Authorization resource-specific errors in api level</li>
 *     <li><strong>CMGT_13000 – 13025</strong>: Detailed Consent resource-specific errors in database level</li>
 *     <li><strong>CMGT_13025 – 13050</strong>: Detailed Consent resource-specific errors in dao level</li>
 *     <li><strong>CMGT_13050 – 13075</strong>: Detailed Consent resource-specific errors in service level</li>
 *     <li><strong>CMGT_13075 – 13099</strong>: Detailed Consent resource-specific errors in api level</li>
 *     <li><strong>CMGT_14000 – 14025</strong>: Status audit-specific errors in database level</li>
 *     <li><strong>CMGT_14025 – 14050</strong>: Status audit-specific errors in dao level</li>
 *     <li><strong>CMGT_14050 – 14075</strong>: Status audit-specific errors in service level</li>
 *     <li><strong>CMGT_14075 – 14099</strong>: Status audit-specific errors in api level</li>
 * </ul>
 *
 * <p>This classification enables consistent error handling and simplifies debugging by separating
 * application-level issues, database-related problems, and client-side validation failures.</p>
 */
public enum ConsentError {

    // payload validation (1)

    PAYLOAD_SCHEMA_VALIDATION_ERROR(
            "CMGT_10000",
            "Invalid request payload",
            "Occurs when the request payload does not match the expected schema."),

    // Consent Resource (10)

    // database level
    CONSENT_NOT_FOUND(
            "CMGT_10001",
            "Consent resource not found",
            "The requested consent does not exist in the system" +
            "."),

    // dao level
    CONSENT_INSERTION_ERROR(
    "CMGT_10025",
    "Failed to insert consent resource",
    "Occurs when consent creation logic fails at the service level."),
    CONSENT_INSERTION_ERROR_IN_DATABASE(
    "CMGT_10026",
    "Failed to persist consent resource to database",
    "Occurs during database operation while storing a new consent."),
    CONSENT_RETRIEVAL_ERROR(
            "CMGT_10027",
            "Unable to retrieve consent resource",
            "General error when fetching a consent from the service."
    ),
    CONSENT_RETRIEVAL_ERROR_IN_DATABASE(
            "CMGT_10028",
            "Database error retrieving consent resource",
            "Occurs due to failure at the database layer while fetching a consent."
    ),

    CONSENT_UPDATE_ERROR(
            "CMGT_10032",
            "Unable to update consent resource",
            "Occurs when attempting to modify an existing consent fails."
    ),
    CONSENT_UPDATE_ERROR_IN_DATABASE(
            "CMGT_10033",
            "Unable to update consent resource",
            "Occurs when attempting to modify an existing consent fails."
    ),
    CONSENT_SEARCH_ERROR(
            "CMGT_10034",
            "Failed to search for consent resources",
            "An error occurred during the search operation on consents."
    ),

    CONSENT_SEARCH_ERROR_IN_DATABASE(
            "CMGT_10035",
            "Database error while searching for consent resources",
            "Occurs when the system fails to search for consents in the database."
    ),
    CONSENT_STATUS_UPDATE_ERROR(
            "CMGT_10036",
            "Unable to update consent status",
            "Occurs when the system fails to update the status of a consent."
    ),
    CONSENT_STATUS_UPDATE_ERROR_IN_DATABASE(
            "CMGT_10037",
            "Unable to update consent status",
            "Occurs when the system fails to update the status of a consent in database."
    ),
    CONSENT_EXPIRY_TIME_UPDATE_ERROR(
            "CMGT_10038",
            "Unable to update consent validity time",
            "Occurs when the system fails to update the validity time of a consent."
    ),
    CONSENT_EXPIRY_TIME_UPDATE_ERROR_IN_DATABASE(
            "CMGT_10042",
            "Unable to update consent validity time",
            "Occurs when the system fails to update the validity time of a consent in database."
    ),

    CONSENT_DELETE_ERROR(
            "CMGT_10043",
            "Unable to delete consent resource",
            "Occurs when the system fails to remove a consent."
    ),
    CONSENT_DELETE_ERROR_IN_DATABASE(
            "CMGT_10044",
            "Database error while deleting consent resource",
            "Occurs when the system fails to delete a consent from the database."
    ),

    CONSENT_REVOKE_ERROR(
            "CMGT_10045",
            "Unable to revoke consent resource",
            "Occurs when the system fails to revoke a consent."
    ),

    // service level
    CONSENT_ALREADY_REVOKED_ERROR(
            "CMGT_10059",
            "Consent already revoked",
            "Occurs when a consent is already revoked and an attempt is made to revoke it again or to update it."
    ),

    INVALID_CONSENT_EXPIRY_TIME(
            "CMGT_10060",
            "Invalid consent expiry time",
            "Please ensure that the expiry time is in the future."
    ),

    // Consent Attribute (11)
    // database level
    CONSENT_ATTRIBUTES_STORE_ERROR(
            "CMGT_11001",
            "Failed to store consent attributes",
            "A generic error occurred while saving consent attribute data."
    ),

    // dao level
    // TODO: not yet implemented
    CONSENT_ATTRIBUTES_STORE_ERROR_DATABASE(
            "CMGT_11001",
            "Database error while storing consent attributes",
            "Occurs when database insertion for consent attributes fails."
    ),
    ATTRIBUTE_NOT_FOUND(
            "CMGT_11101",
            "Consent attribute not found",
            "The requested attribute is not associated with any consent."
    ),
    ATTRIBUTE_UPDATE_ERROR(
            "CMGT_11201",
            "Failed to update consent attribute",
            "Occurs when an update operation on a consent attribute fails."
    ),
    ATTRIBUTE_DELETE_ERROR(
            "CMGT_11301",
            "Failed to delete consent attribute",
            "Triggered when an attribute deletion fails due to system or database errors."
    ),
    ATTRIBUTE_VALIDATION_ERROR(
            "CMGT_11401",
            "Invalid consent attribute data",
            "The provided attribute data is either missing required fields or violates constraints."
    ),

    // Authorization Resource (12)
    // database level
    AUTHORIZATION_RESOURCE_NOT_FOUND(
            "CMGT_12101",
            "Authorization resource not found",
            "The requested authorization record could not be located."
    ),

    // dao level
    AUTHORIZATION_RESOURCE_INSERTION_ERROR(
            "CMGT_12002",
            "Failed to store authorization resource",
            "A general error occurred while saving authorization data."
    ),
    AUTHORIZATION_RESOURCE_INSERTION_ERROR_IN_DATABASE(
            "CMGT_12003",
            "Database error while storing authorization resource",
            "Database insert operation failed for the authorization resource."
    ),
    AUTHORIZATION_RESOURCE_RETRIEVAL_ERROR(
            "CMGT_12004",
            "Authorization resource not found",
            "The requested authorization resource could not be found."
    ),
    AUTHORIZATION_RESOURCE_RETRIEVAL_ERROR_IN_DATABASE(
            "CMGT_12005",
            "Database error retrieving authorization resource",
            "Database fetch operation failed for the authorization resource."
    ),
    AUTHORIZATION_RESOURCE_UPDATE_ERROR(
            "CMGT_12006",
            "Failed to update authorization resource",
            "Occurs when a system or database error prevents updating authorization data."
    ),
    AUTHORIZATION_RESOURCE_UPDATE_ERROR_IN_DATABASE(
            "CMGT_12007",
            "Database error while updating authorization resource",
            "Database update operation failed for the authorization resource."
    ),
    AUTHORIZATION_RESOURCE_DELETE_ERROR(
            "CMGT_12308",
            "Failed to delete authorization resource",
            "Occurs when deletion of the authorization data fails."
    ),
    AUTHORIZATION_RESOURCE_DELETE_ERROR_IN_DATABASE(
            "CMGT_12308",
            "Failed to delete authorization resource",
            "Occurs when deletion of the authorization data fails."
    ),

    // Detailed Consent Resource (13)
    // database level
    DETAILED_CONSENT_NOT_FOUND(
            "CMGT_13001",
            "Detailed consent resource not found",
            "The system could not find a detailed consent resource for the given identifier."
    ),

    // dao level
    DETAILED_CONSENT_RESOURCE_INSERTION_ERROR(
            "CMGT_13002",
            "Failed to store detailed consent resource",
            "A general failure occurred while saving a detailed consent record."
    ),
    DETAILED_CONSENT_RESOURCE_INSERTION_ERROR_IN_DATABASE(
            "CMGT_13003",
            "Database error while storing detailed consent resource",
            "Failed to insert a detailed consent record into the database."
    ),
    DETAILED_CONSENT_RETRIEVAL_ERROR(
            "CMGT_13004",
            "Unable to retrieve detailed consent resource",
            "A general error occurred while fetching a detailed consent."
    ),
    DETAILED_CONSENT_RETRIEVAL_ERROR_IN_DATABASE(
            "CMGT_13005",
            "Database error retrieving detailed consent resource",
            "Database fetch operation failed for a detailed consent."
    ),
    DETAILED_CONSENT_UPDATE_ERROR(
            "CMGT_13006",
            "Failed to update detailed consent resource",
            "Occurs when update operation on a detailed consent fails."
    ),

    // status audit (14)
    STATUS_AUDIT_INSERTION_ERROR(
            "CMGT_14002",
            "Failed to store status audit",
            "A general error occurred while saving status audit data."
    ),
    STATUS_AUDIT_INSERTION_ERROR_IN_DATABASE(
            "CMGT_14003",
            "Database error while storing status audit",
            "Database insert operation failed for the status audit."
    ),

    // consent history
    CONSENT_HISTORY_RETRIEVAL_ERROR(
            "CMGT_15100",
            "Failed to retrieve consent history",
            "A general error occurred while fetching consent history."
    ),

    DATABASE_CONNECTION_ERROR(
            "CMGT_20002",
            "Database connection failure",
            "Unable to connect to the database services."
    ),
    // General
    UNKNOWN_ERROR(
            "CMGT_99998",
            "An unknown error occurred",
            "An unexpected exception or system error was encountered."
    );


    private final String code;
    private final String message;
    private final String description;


    ConsentError(String code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return code + ": " + message;
    }
}
