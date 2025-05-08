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

package org.wso2.financial.services.accelerator.consent.mgt.service;

import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentMgtException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentAttributes;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentFile;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentHistoryResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentStatusAuditRecord;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Consent core service interface.
 */
public interface ConsentCoreService {


    /**
     * This method is used to create an authorizable consent. The following functionality contains in this method.
     * 1. Creates a consent resource
     * 2. If available, stores consent attributes
     * 3. Create an audit record for consent creation
     * 4. If isImplicitAuth parameter is true, creates an authorization resource
     *
     * @param consentResource   consent resource
     * @param authorizationResources auth resources
     * @return returns DetailedConsentResource
     * @throws ConsentMgtException thrown if any error occur in the process
     */
    DetailedConsentResource createConsent(ConsentResource consentResource,
                                                                  ArrayList<AuthorizationResource>
                                                                          authorizationResources
                                                                 )
            throws
            ConsentMgtException,
            org.wso2.financial.services.accelerator.consent.mgt.service.exception.ConsentMgtException;


    /**
     * This method is used to get a consent with or without consent attributes. The following functionality contains in
     * this method.
     *
     * 1. Get existing consent for status validation
     * 2. Optionally gets consent attributes according to the value of withConsentAttributes flag
     * 3. Check whether the retrieved consent involves a file
     *
     * @param consentId               ID of the consent
     * @param withConsentAttributes   flag to determine the consent should be retrieved with attributes or not
     * @return returns ConsentResource
     * @throws ConsentMgtException thrown if any error occur in the process
     */
    ConsentResource getConsent(String consentId, boolean withConsentAttributes)
            throws
            ConsentMgtException;

    /**
     * This method is used to get a detailed consent for the provided consent ID. The detailed consent includes
     * following data if exist in addition to consent resource specific data.
     *
     * 1. Relative consent authorization data
     * 2. Relative consent account mapping data
     * 3. Relative consent attributes
     *
     * @param consentId      ID of the consent
     * @return a detailed consent resource
     * @throws ConsentMgtException thrown if any error occur in the process
     */
    DetailedConsentResource getDetailedConsent(String consentId, String orgInfo) throws
            ConsentMgtException;

    /**
     * This method is used to create a consent file. The following functionality contains in this method.
     *
     * 1. Get the existing consent to validate the status according to the attribute "applicableStatusToFileUpload"
     * 2. Create the consent file
     * 3. Update the consent status
     * 4. Create an audit record for consent update
     *
     * @param consentFileResource            consent file resource
     * @param newConsentStatus               new cbindUserAccountsToConsentonsent status
     * @param userId                         user ID (optional)
     * @param applicableStatusToFileUpload   status that the consent should have to upload a file
     * @return true if transaction is a success, throws an exception otherwise
     * @throws ConsentMgtException thrown if any error occur in the process
     */
    boolean createConsentFile(ConsentFile consentFileResource, String newConsentStatus, String userId,
                              String applicableStatusToFileUpload)
            throws
            ConsentMgtException;

    /**
     * This method is used to retrieve the consent file using the related consent ID.
     *
     * @param consentId     consent ID
     * @return the consent file resource
     * @throws ConsentMgtException thrown if an error occurs
     */
    ConsentFile getConsentFile(String consentId) throws
            ConsentMgtException;

    /**
     * This method is used to create an authorization for a consent.
     *
     * @param authorizationResource     authorization resource
     * @return returns AuthorizationResource
     * @throws ConsentMgtException thrown if any error occurs in the process
     */
    AuthorizationResource createConsentAuthorization(AuthorizationResource authorizationResource)
            throws
            ConsentMgtException;

    /**
     * This method is to retrieve an authorization resource using a given authorization ID.
     *
     * @param authorizationId   authorization ID
     * @return an authorization resource
     * @throws ConsentMgtException thrown if an error occurs in the process
     */
    AuthorizationResource getAuthorizationResource(String authorizationId, String orgID) throws
            ConsentMgtException;

    /**
     * This method is used to search authorization resources for a given input parameter. Both consent ID and
     * user ID are optional. If both are null, all authorization resources will be returned.
     *
     * @param consentId     consent ID
     * @param userId        user ID
     * @return a list of authorization resources
     * @throws ConsentMgtException thrown if any error occurs in the process
     */
    ArrayList<AuthorizationResource> searchAuthorizations(String consentId, String userId)
            throws
            ConsentMgtException;

    /**
     * This method is used to search authorization resources for a given consent ID.
     *
     * @param consentId     consent ID
     * @return a list of authorization resources
     * @throws ConsentMgtException thrown if any error occurs in the process
     */
    ArrayList<AuthorizationResource> searchAuthorizations(String consentId)
            throws
            ConsentMgtException;

    /***
     * this method is used to update the whole authorization resource
     *
     * @param authorizationResource authorization resource
     * @return updated authorization resource
     * @throws ConsentMgtException thrown if any error occurs in the process
     */
    AuthorizationResource updateAuthorizationResource(String authorizationId,
                                                      AuthorizationResource authorizationResource,
                                                      String orgID)
            throws
            ConsentMgtException;


    /**
     * This method is used to search authorization resources for a userId.
     *
     * @param userId    user ID
     * @return a list of authorization resources
     * @throws ConsentMgtException thrown if any error occurs in the process
     */
    ArrayList<AuthorizationResource> searchAuthorizationsForUser(String userId)
            throws
            ConsentMgtException;

    /**
     * This method is used to update the status of an authorization resource by providing the authorization Id and
     * the new authorization status.
     *
     * @param authorizationId           the authorization Id of the authorization resource need to be updated
     * @param newAuthorizationStatus    the new authorization resource
     * @return the updated authorization resource
     * @throws ConsentMgtException thrown if any error occur while updating
     */
    AuthorizationResource updateAuthorizationStatus(String authorizationId, String newAuthorizationStatus, String orgID)
            throws
            ConsentMgtException;

    /**
     * This method is used to update the user of an authorization resource by providing the authorization ID and
     * the user ID.
     *
     * @param authorizationId   the authorization ID of the authorization resource that needs to be updated
     * @param userId            the user of the authorization resource
     * @return the updated authorization resource
     * @throws ConsentMgtException thrown if any error occurs while updating
     */
    AuthorizationResource updateAuthorizationUser(String authorizationId, String userId, String orgID)
            throws
            ConsentMgtException;


    /**
     * This metho is used to delete an authorization resource using the given authorization ID.
     * @param authorizationId   authorization ID
     * @return true if the deletion is successful
     * @throws ConsentMgtException thrown if any error occurs in the process
     */
    boolean deleteAuthorizationResource(String authorizationId)
            throws
            ConsentMgtException;

    /**
     * This method is used to update status of the consent for a given consentId and userId.
     * @param consentId         consent ID
     * @param newConsentStatus  new consent status
     * @return the updated consent resource
     * @throws ConsentMgtException thrown if any error occurs in the process
     */
    DetailedConsentResource updateConsentStatus(String consentId, String newConsentStatus)
            throws
            ConsentMgtException;

    /**
     * This method is used to update status of the consent for a given consentId and userId.
     * @param consentId         consent ID
     * @param newConsentStatus  new consent status
     * @param userId            user ID
     * @param  reason            reason
     * @throws ConsentMgtException thrown if any error occurs in the process
     */
    void updateConsentStatusWithImplicitReasonAndUserId(String consentId,
                                                        String newConsentStatus, String userId,
                                                        String reason, String orgID)
            throws
            ConsentMgtException;


    /**
     * This method is used to update status of the consent for a given clientId and userId.
     * @param clientId
     * @param status
     * @param reason
     * @param userId
     */
    void bulkUpdateConsentStatus(String orgID, String clientId, String status, String reason, String userId,
                                 String consentType, ArrayList<String> applicableExistingStatus)
            throws
            ConsentMgtException;




    /**
     * This method is used to deactivate account bindings of provided account mapping IDs.
     *
     * @param accountMappingIDs     list of account mapping IDs to be deactivated
     * @return true is deactivation is a success, false otherwise
     * @throws ConsentMgtException thrown if any error occurs
     */
    boolean deactivateAccountMappings(ArrayList<String> accountMappingIDs) throws
            ConsentMgtException;



    /**
     *
     * @param consentId
     * @return
     * @throws ConsentMgtException
     */
    boolean deleteConsent(String consentId)
            throws
            ConsentMgtException;

    /**
     * This method is used to revoke a consent. The following functionality contains in this method.
     *
     * 1. Get existing consent for status validation
     * 2. Update existing consent status
     * 3. Create an audit record for consent update
     * 4. Update account mapping status as inactive
     *
     * @param consentId             ID of the consent
     * @param revokedConsentStatus  the status of the consent after revoked
     * @return true is the transaction is a success, throws an exception otherwise
     * @throws ConsentMgtException thrown if any error occur in the process
     */


    boolean revokeConsent(String consentId, String revokedConsentStatus)
            throws
            ConsentMgtException;

    /**
     * This method is used to revoke a consent by adding a revoke reason.
     * The following functionality contains in this method.
     *
     * 1. Get existing consent for status validation
     * 2. Update existing consent status
     * 3. Create an audit record for consent update
     * 4. Update account mapping status as inactive
     *
     * @param consentId             ID of the consent
     * @param revokedConsentStatus  the status of the consent after revoked
     * @param revokedReason         the reason for consent revocation
     * @return true is the transaction is a success, throws an exception otherwise
     * @throws ConsentMgtException thrown if any error occur in the process
     */
    boolean revokeConsentWithReason(String consentId, String revokedConsentStatus, String revokedReason)
            throws
            ConsentMgtException;

    /**
     * This method is used to revoke a consent. The following functionality contains in this method.
     *
     * 1. Get existing consent for status validation
     * 2. Update existing consent status
     * 3. Create an audit record for consent update
     * 4. Update account mapping status as inactive
     *
     * @param consentId             ID of the consent
     * @param revokedConsentStatus  the status of the consent after revoked
     * @param userId                user ID
     * @return true is the transaction is a success, throws an exception otherwise
     * @throws ConsentMgtException thrown if any error occur in the process
     */
    boolean revokeConsent(String consentId, String revokedConsentStatus, String userId)
            throws
            ConsentMgtException;

    /**
     * This method is used to revoke a consent by adding a revoke reason.
     * The following functionality contains in this method.
     *
     * 1. Get existing consent for status validation
     * 2. Update existing consent status
     * 3. Create an audit record for consent update
     * 4. Update account mapping status as inactive
     *
     * @param consentId             ID of the consent
     * @param revokedConsentStatus  the status of the consent after revoked
     * @param userId                user ID
     * @param revokedReason         the reason for consent revocation
     * @return true is the transaction is a success, throws an exception otherwise
     * @throws ConsentMgtException thrown if any error occur in the process
     */
    boolean revokeConsentWithReason(String consentId, String revokedConsentStatus, String userId, String revokedReason)
            throws
            ConsentMgtException;

    /**
     * This method is used to revoke a consent. The following functionality contains in this method.
     *
     * 1. Get existing consent for status validation
     * 2. Update existing consent status
     * 3. Create an audit record for consent update
     * 4. Update account mapping status as inactive
     * 5. Revoke tokens related to the consent if the flag 'shouldRevokeTokens' is true
     *
     * @param consentId             ID of the consent
     * @param revokedConsentStatus  the status of the consent after revoked
     * @param userId                user ID
     * @param shouldRevokeTokens the check to revoke tokens or not when revoking consent
     * @return true is the transaction is a success, throws an exception otherwise
     * @throws ConsentMgtException thrown if any error occur in the process
     */
    boolean revokeConsent(String consentId, String revokedConsentStatus, String userId, boolean shouldRevokeTokens)
            throws
            ConsentMgtException;

    /**
     * This method is used to revoke a consent. The following functionality contains in this method.
     *
     * 1. Get existing consent for status validation
     * 2. Update existing consent status
     * 3. Create an audit record for consent update
     * 4. Update account mapping status as inactive
     * 5. Revoke tokens related to the consent if the flag 'shouldRevokeTokens' is true
     *
     * @param consentId             ID of the consent
     * @param revokedConsentStatus  the status of the consent after revoked
     * @param userId                user ID
     * @param shouldRevokeTokens    the check to revoke tokens or not when revoking consent
     * @param revokedReason         the reason for consent revocation
     * @return true is the transaction is a success, throws an exception otherwise
     * @throws ConsentMgtException thrown if any error occur in the process
     */
    boolean revokeConsentWithReason(String consentId, String revokedConsentStatus, String userId,
                                    boolean shouldRevokeTokens, String revokedReason)
            throws
            ConsentMgtException;

    /**
     * This method is used to revoke existing consents for the given clientId, userId, consent type and status
     * combination. Also revokes the tokens related to the consents which are revoked if the flag
     * 'shouldRevokeTokens' is true.
     *
     * @param clientId                  ID of the client
     * @param userId                    ID of the user
     * @param consentType               consent type
     * @param applicableStatusToRevoke  the status that a consent should have for revoking
     * @param revokedConsentStatus      the status should be updated the consent with after revoking
     * @param shouldRevokeTokens        the check to revoke tokens or not when revoking consent
     * @return returns true if successful
     * @throws ConsentMgtException thrown if an error occurs in the process
     */
    boolean revokeExistingApplicableConsents(String clientId, String userId, String consentType,
                                             String applicableStatusToRevoke, String revokedConsentStatus,
                                             boolean shouldRevokeTokens)
            throws
            ConsentMgtException;

    /**
     * This method is used in consent re-authorization scenarios to update the account mappings according to the
     * additional/removed accounts from the new  authorization. Also, the consent status is updated with a provided
     * status. Also, can be used to amend accounts.
     *
     * @param consentId                         consent ID
     * @param authID                            authorization ID
     * @param userId                            user ID for creating the audit record
     * @param accountIDsMapWithPermissions      accounts IDs with relative permissions
     * @param currentConsentStatus              current status of the consent for creating audit record
     * @param newConsentStatus                  new consent status after re-authorization
     * @return true if all operations are successful
     * @throws ConsentMgtException thrown if any error occurs in the process
     */
    boolean reAuthorizeExistingAuthResource(String consentId, String authID, String userId, Map<String,
            ArrayList<String>> accountIDsMapWithPermissions, String currentConsentStatus, String newConsentStatus)
            throws
            ConsentMgtException;

    /**
     * This method is used in consent re-authorization scenarios to update the account mappings according to the
     * additional/removed accounts from the new  authorization. A new authorization resource will be created when
     * re authorizing using this method. Existing authorizations will be updated with a provided status. Also, the
     * consent status is updated with a provided status. Also, can be used to amend accounts.
     *
     * @param consentId                     consent ID
     * @param userId                        user ID
     * @param accountIDsMapWithPermissions  account IDs with relative permissions
     * @param currentConsentStatus          current status of the consent for creating audit record
     * @param newConsentStatus              new consent status after re-authorization
     * @param newExistingAuthStatus         new status of the existing authorizations
     * @param newAuthStatus                 new status of the new authorization
     * @param newAuthType                   new authorization type
     * @return true if all operations are successful
     * @throws ConsentMgtException thrown if any error occurs in the process
     */
    boolean reAuthorizeConsentWithNewAuthResource(String consentId, String userId, Map<String,
                                                          ArrayList<String>> accountIDsMapWithPermissions,
                                                  String currentConsentStatus, String newConsentStatus,
                                                  String newExistingAuthStatus, String newAuthStatus,
                                                  String newAuthType)
            throws
            ConsentMgtException;


    /**
     * This method is used to store consent attributes related to a particular consent.
     *
     * @param consentId             consent ID
     * @param consentAttributes     consent attribute key and values map
     * @return a consent attributes resource
     * @throws ConsentMgtException thrown if an error occurs in the process
     */
    boolean storeConsentAttributes(String consentId, Map<String, String> consentAttributes)
            throws
            ConsentMgtException;

    /**
     * This method is used to get consent attributes for a provided attribute keys list related to a particular consent.
     *
     * @param consentId             consent ID
     * @param consentAttributeKeys  consent attribute keys list
     * @return a consent attributes resource
     * @throws ConsentMgtException thrown if an error occurs in the process
     */
    ConsentAttributes getConsentAttributes(String consentId, ArrayList<String> consentAttributeKeys)
            throws
            ConsentMgtException;

    /**
     * This method is used to get consent attributes related to a particular consent.
     *
     * @param consentId     consent ID
     * @return a consent attributes resource
     * @throws ConsentMgtException thrown if an error occurs in the process
     */
    ConsentAttributes getConsentAttributes(String consentId) throws
            ConsentMgtException;

    /**
     * This method is used to get consent attributes for a provided attribute name.
     *
     * @param attributeName     attribute name
     * @return a map with related consent ID and the attribute values
     * @throws ConsentMgtException thrown if an error occurs in the process
     */
    Map<String, String> getConsentAttributesByName(String attributeName) throws
            ConsentMgtException;


    /**
     * This method is used to get consent attributes for a provided attribute name and attribute value.
     *
     * @param attributeName     attribute name
     * @param attributeValue    attribute value
     * @return Consent ID related to the given attribute key and value
     * @throws ConsentMgtException thrown if an error occurs in the process
     */
    ArrayList<String> getConsentIdByConsentAttributeNameAndValue(String attributeName, String attributeValue)
            throws
            ConsentMgtException;

    /**
     * This method is used to delete the provided consent attributes for a particular consent.
     *
     * @param consentId             consent ID
     * @param consentAttributes     attributes to update
     * @return updated consent attributes
     * @throws ConsentMgtException thrown if an error occurs in the process
     */
    ConsentAttributes updateConsentAttributes(String consentId, Map<String, String> consentAttributes)
            throws
            ConsentMgtException;

    /**
     * This method is used to delete the provided consent attributes for a particular consent.
     *
     * @param consentId             consent ID
     * @param attributeKeysList     attributes to delete
     * @return true if deletion is successful
     * @throws ConsentMgtException thrown if an error occurs in the process
     */
    boolean deleteConsentAttributes(String consentId, ArrayList<String> attributeKeysList)
            throws
            ConsentMgtException;

    /**
     * This method is used to search audit records. Useful for auditing purposes. All the input parameters are
     * optional. If all parameters are null, all the audit records will be returned.
     *
     * @param consentId         consent ID
     * @param status            status of the audit records needed
     * @param actionBy          user who performed the status change
     * @param fromTime          from time
     * @param toTime            to time
     * @param statusAuditID     ID of a specific audit record that need to be searched
     * @return a list of consent status audit records
     * @throws ConsentMgtException thrown if an error occurs
     */
    ArrayList<ConsentStatusAuditRecord> searchConsentStatusAuditRecords(String consentId, String status,
                                                                        String actionBy, Long fromTime, Long toTime,
                                                                        String statusAuditID)
            throws
            ConsentMgtException;

    /**
     * This method is used to retrieve a list of consent status audit records by consent_id.
     *
     * @param consentIDs    list of consentIDs (optional)
     * @param limit         limit
     * @param offset        offset
     * @return returns a list of consent status audit records.
     * @throws ConsentMgtException thrown if a database error occurs
     */
    ArrayList<ConsentStatusAuditRecord> getConsentStatusAuditRecords(ArrayList<String> consentIDs, Integer limit,
                                                                     Integer offset)
            throws
            ConsentMgtException;

    /**
     * This method is used to store the details of the previous consent when an consent amendment happens.
     * The consent ID is mandatory. The detailed consent resource for the previous consent and the amendedTimestamp
     * is mandatory to be set in the ConsentHistoryResource.
     *
     * @param consentId                 consent ID
     * @param consentHistoryResource    detailed consent resource and other history parameters of the previous consent
     * @param currentConsentResource    detailed consent resource of the current (new) consent
     * @return true if all operations are successful
     * @throws ConsentMgtException thrown if any error occurs in the process
     */
    boolean storeConsentAmendmentHistory(String consentId, ConsentHistoryResource consentHistoryResource,
                                         DetailedConsentResource currentConsentResource)
            throws
            ConsentMgtException;

    /**
     * This method is used to retrieve consent amendment history for a given consentId. Consent ID is mandatory.
     *
     * @param consentId     consent ID
     * @return a map of consent history resources
     * @throws ConsentMgtException thrown if any error occurs in the process
     */
    Map<String, ConsentHistoryResource> getConsentAmendmentHistoryData(
            List<String> statusAuditRecordIds,
            String consentId)
            throws
            ConsentMgtException;

    /**
     * This method is used to search detailed consents for the given lists of parameters. Following optional lists
     * can be passed to retrieve detailed consents. The search will be performed according to the provided input. Any
     * list can contain any number of elements. The conjunctive result will be returned. If all lists are passed as
     * null, all the consents related to other search parameters will be returned.
     *
     * 1. A list of consent IDs
     * 2. A list of client IDs
     * 3. A list of consent types
     * 4. A list of consent statuses
     * 5. A list of user IDs
     *
     * (All above lists are optional)
     *
     * @param consentIDs        consent IDs optional list
     * @param clientIDs         client IDs optional list
     * @param consentTypes      consent types optional list
     * @param consentStatuses   consent statuses optional list
     * @param userIDs           user IDs optional list
     * @param fromTime          from time
     * @param toTime            to time
     * @param limit             limit
     * @param offset            offset
     * @return a list of detailed consent resources according to the provided parameters
     * @throws ConsentMgtException thrown if any error occur
     */
    ArrayList<DetailedConsentResource> searchDetailedConsents(String orgID, ArrayList<String> consentIDs,
                                                              ArrayList<String> clientIDs,
                                                              ArrayList<String> consentTypes,
                                                              ArrayList<String> consentStatuses,
                                                              ArrayList<String> userIDs, Long fromTime, Long toTime,
                                                              Integer limit, Integer offset)
            throws
            ConsentMgtException;


    /**
     * This method is used to amend the selected properties of the entire detailed consent. The consent ID is mandatory.
     * One of consent receipt or validity period must be provided.
     * An audit record is created to indicate that the consent is
     * amended. But the consent status won't be changed (since when an authorized consent is amended, the status
     * remains the same)
     *
     * @param consentId                     consent ID
     * @param consentReceipt                new consent receipt
     * @param consentExpiryTime           new consent validity time
     * @param authID                        authorization ID
     * @param accountIDsMapWithPermissions  accounts IDs with relative permissions
     * @param newConsentStatus              new consent status
     * @param consentAttributes             new consent attributes key and values map
     * @param userId                        user ID to create audit record
     * @param additionalAmendmentData       A Data Map to pass any additional data that needs to be amended in the
     *                                     consent
     * @return the updated detailed consent resource
     * @throws ConsentMgtException thrown if any error occurs in the process
     */
    DetailedConsentResource amendDetailedConsent(String consentId, String consentReceipt, Long consentExpiryTime,
                                                 String authID, Map<String,
                    ArrayList<String>> accountIDsMapWithPermissions,
                                                 String newConsentStatus, Map<String, String> consentAttributes,
                                                 String userId, Map<String, Object> additionalAmendmentData)
            throws
            ConsentMgtException;

    /**
     *
     * @param orgInfo
     * @param consentId
     * @param consentReceipt
     * @param consentExpiryTime
     * @param reAuthorizationResources
     * @param newConsentStatus
     * @param consentAttributes
     * @param userId
     * @param newAuthResources
     * @return
     * @throws ConsentMgtException
     */
    DetailedConsentResource amendDetailedConsentWithBulkAuthResource(String orgInfo, String consentId,
                                                                     String consentReceipt,
                                                                     Long consentExpiryTime,
                                                                     ArrayList<AuthorizationResource>
                                                                             reAuthorizationResources,

                                                                     String newConsentStatus,
                                                                     Map<String, String> consentAttributes,
                                                                     String userId,
                                                                     ArrayList<AuthorizationResource> newAuthResources)
            throws
            ConsentMgtException;

    /**
     * this method is used to update the expiry time of the consent
     */
    boolean updateConsentExpiryTime(String consentId, long consentExpiryTime, String orgInfo)
            throws
            ConsentMgtException;

}
