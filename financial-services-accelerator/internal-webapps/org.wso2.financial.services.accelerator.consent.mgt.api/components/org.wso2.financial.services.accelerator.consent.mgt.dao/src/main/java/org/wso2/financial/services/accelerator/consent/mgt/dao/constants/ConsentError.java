package org.wso2.financial.services.accelerator.consent.mgt.dao.constants;

/**
 * Enum representing various error codes and messages related to consent management.
 * Each enum constant includes a unique error code, a user-friendly message, and a detailed description.
 */
public enum ConsentError {

    // payload validation (1)

    PAYLOAD_SCHEMA_VALIDATION_ERROR("CMGT_10000", "Invalid request payload", "Occurs when the request payload does " +
            "not match the expected schema."),
    // Consent Resource (10)
    CONSENT_INSERTION_ERROR("CMGT_10001", "Failed to insert consent resource",
            "Occurs when consent creation logic fails at the service level."),
    CONSENT_INSERTION_ERROR_IN_DATABASE("CMGT_10002", "Failed to persist consent resource to database",
            "Occurs during database operation while storing a new consent."),
    CONSENT_ALREADY_REVOKED_ERROR("CMGT_10003", "Consent already revoked",
            "Occurs when a consent is already revoked and an attempt is made to revoke it again or to update it."),
    CONSENT_NOT_FOUND("CMGT_10010", "Consent resource not found", "The requested consent does not exist in the system" +
            "."),
    CONSENT_RETRIEVAL_ERROR("CMGT_10011", "Unable to retrieve consent resource", "General error when fetching a " +
            "consent from the service."),
    CONSENT_RETRIEVAL_ERROR_IN_DATABASE("CMGT_10012", "Database error retrieving consent resource", "Occurs due to " +
            "failure at the database layer while fetching a consent."),

    CONSENT_SEARCH_ERROR_IN_DATABASE("CMGT_10013", "Database error while searching for consent resources",
            "Occurs when the system fails to search for consents in the database."),
    CONSENT_UPDATE_ERROR("CMGT_10020", "Unable to update consent resource", "Occurs when attempting to modify an " +
            "existing consent fails."),
    CONSENT_STATUS_UPDATE_ERROR("CMGT_1021", "Unable to update consent status", "Occurs when the system fails to " +
            "update the status of a consent."),
    CONSENT_STATUS_UPDATE_ERROR_IN_DATABASE("CMGT_1022", "Unable to update consent status", "Occurs when the system " +
            "fails to " +
            "update the status of a consent in database."),
    CONSENT_EXPIRY_TIME_UPDATE_ERROR("CMGT_1023", "Unable to update consent validity time",
            "Occurs when the system fails to update the validity time of a consent."),
    CONSENT_EXPIRY_TIME_UPDATE_ERROR_IN_DATABASE("CMGT_1024", "Unable to update consent validity time",
            "Occurs when the system fails to update the validity time of a consent in database."),
    CONSENT_DELETE_ERROR("CMGT_10301", "Unable to delete consent resource",
            "Occurs when the system fails to remove a consent."),
    CONSENT_VALIDATION_ERROR("CMGT_10401", "Consent data is invalid or missing",
            "Triggered when consent data does not meet validation requirements."),
    CONSENT_PROCESSING_ERROR("CMGT_10901", "Unexpected error during consent processing",
            "A generic error occurred while handling a consent."),
    INVALID_CONSENT_EXPIRY_TIME("CMGT_10901", "Invalid consent expiry time",
            "Please ensure that the expiry time is " + "in the" +
                    " future"),
    CONSENT_SEARCH_ERROR("CMGT_10801", "Failed to search for consent resources",
            "An error occurred during the search operation on consents."),

    // Consent Attribute (11)
    CONSENT_ATTRIBUTES_STORE_ERROR("CMGT_11001", "Failed to store consent attributes",
            "A generic error occurred while saving consent attribute data."),
    CONSENT_ATTRIBUTES_STORE_ERROR_DATABASE("CMGT_11001", "Database error while storing consent attributes",
            "Occurs when database insertion for consent attributes fails."),

    ATTRIBUTE_NOT_FOUND("CMGT_11101", "Consent attribute not found",
            "The requested attribute is not associated with any consent."),
    ATTRIBUTE_UPDATE_ERROR("CMGT_11201", "Failed to update consent attribute",
            "Occurs when an update operation on a consent attribute fails."),
    ATTRIBUTE_DELETE_ERROR("CMGT_11301", "Failed to delete consent attribute",
            "Triggered when an attribute deletion fails due to system or database errors."),
    ATTRIBUTE_VALIDATION_ERROR("CMGT_11401", "Invalid consent attribute data",
            "The provided attribute data is either missing required fields or violates constraints."),

    // Authorization Resource (12)


            AUTHORIZATION_RESOURCE_INSERTION_ERROR("CMGT_12001", "Failed to store authorization resource",
            "A general error occurred while saving authorization data."),
    AUTHORIZATION_RESOURCE_INSERTION_ERROR_IN_DATABASE("CMGT_12002",
            "Database error while storing authorization resource",
            "Database insert operation failed for the authorization resource."),


    AUTHORIZATION_RESOURCE_RETRIEVAL_ERROR("CMGT_12100", "Authorization resource not found",
            "The requested authorization resource could not be found."),
    AUTHORIZATION_RESOURCE_RETRIEVAL_ERROR_IN_DATABASE(
            "CMGT_12101", "Database error retrieving authorization resource",
            "Database fetch operation failed for the authorization resource."),

    AUTHORIZATION_RESOURCE_NOT_FOUND("CMGT_12103", "Authorization resource not found",
            "The requested authorization record could not be located."),
    AUTHORIZATION_RESOURCE_UPDATE_ERROR("CMGT_12201", "Failed to update authorization resource",
            "Occurs when a system or database error prevents updating authorization data."),
    AUTHORIZATION_RESOURCE_DELETE_ERROR("CMGT_12301", "Failed to delete authorization resource",
            "Occurs when deletion of the authorization data fails."),
    AUTHORIZATION_RESOURCE_VALIDATION_ERROR("CMGT_12401", "Invalid authorization resource data",
            "Validation failed for one or more fields in the authorization resource."),

    // Detailed Consent Resource (13)
    DETAILED_CONSENT_RESOURCE_INSERTION_ERROR("CMGT_13001", "Failed to store detailed consent resource",
            "A general failure occurred while saving a detailed consent record."),
    DETAILED_CONSENT_RESOURCE_INSERTION_ERROR_IN_DATABASE("CMGT_13001",
            "Database error while storing detailed consent resource",
            "Failed to insert a detailed consent record into the database."),
    DETAILED_CONSENT_NOT_FOUND("CMGT_13101", "Detailed consent resource not found",
            "The system could not find a detailed consent resource for the given identifier."),
    DETAILED_CONSENT_RETRIEVAL_ERROR("CMGT_13101", "Unable to retrieve detailed consent resource",
            "A general error occurred while fetching a detailed consent."),
    DETAILED_CONSENT_RETRIEVAL_ERROR_IN_DATABASE("CMGT_13102", "Database error retrieving detailed consent resource",
            "Database fetch operation failed for a detailed consent."),
    DETAILED_CONSENT_UPDATE_ERROR("CMGT_13201", "Failed to update detailed consent resource",
            "Occurs when update operation on a detailed consent fails."),

    // status audit (14)
    STATUS_AUDIT_INSERTION_ERROR("CMGT_14000", "Failed to store status audit",
            "A general error occurred while saving status audit data."),
    STATUS_AUDIT_INSERTION_ERROR_IN_DATABASE("CMGT_14001", "Database error while storing status audit",
            "Database insert operation failed for the status audit."),


    // consent history
    CONSENT_HISTORY_RETRIEVAL_ERROR("CMGT_15100", "Failed to retrieve consent history",
            "A general error occurred while fetching consent history."),

    // General
    UNKNOWN_ERROR("CMGT_99999", "An unknown error occurred",
            "An unexpected exception or system error was encountered.");


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
