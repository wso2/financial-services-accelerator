/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.consent.mgt.service;

import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentMgtException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentAttributes;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentFile;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentHistoryResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentMappingResource;
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
     * @param userID            user ID is optional and used to create the audit record
     * @param authStatus        authorization status
     * @param authType          authorization type (eg. authorization, cancellation)
     * @param isImplicitAuth    flag to determine whether authorization is implicit or not
     * @return returns DetailedConsentResource
     * @throws ConsentMgtException thrown if any error occur in the process
     */
    DetailedConsentResource createAuthorizableConsent(ConsentResource consentResource, String userID,
                                                      String authStatus, String authType, boolean isImplicitAuth)
            throws
            ConsentMgtException;

    /**
     * This method is used to create an authorizable consent. The following functionality contains in this method.
     * 1. Creates a consent resource
     * 2. If available, stores consent attributes
     * 3. Create an audit record for consent creation
     * 4. If isImplicitAuth parameter is true, creates an authorization resource
     *
     * @param consentResource   consent resource
     * @param authorizationResources auth resources
     * @param isImplicitAuth    flag to determine whether authorization is implicit or not
     * @return returns DetailedConsentResource
     * @throws ConsentMgtException thrown if any error occur in the process
     */
    DetailedConsentResource createAuthorizableConsentWithBulkAuth(ConsentResource consentResource,
                                                                  ArrayList<AuthorizationResource>
                                                                          authorizationResources,
                                                                  boolean isImplicitAuth)
            throws
            ConsentMgtException,
            org.wso2.financial.services.accelerator.consent.mgt.service.exception.ConsentMgtException;

    /**
     * This method is used to create an exclusive consent. The following functionality contains in this method.
     *
     * 1. Update existing consent statuses as necessary and deactivate their account mappings
     * 2. Create audit records for necessary consent updates
     * 3. Create a new authorizable consent
     *
     * @param consentResource consent resource
     * @param userID                            user ID
     * @param authStatus                        authorization status
     * @param authType                          authorization type
     * @param applicableExistingConsentsStatus  applicable status for existing consents to be updated
     * @param newExistingConsentStatus          new status that the updated consents should be
     * @param isImplicitAuth                    flag to determine whether authorization is implicit or not
     * @return returns DetailedConsentResource
     * @throws ConsentMgtException thrown if any error occur in the process
     */
    DetailedConsentResource createExclusiveConsent(ConsentResource consentResource, String userID, String authStatus,
                                                   String authType, String applicableExistingConsentsStatus,
                                                   String newExistingConsentStatus, boolean isImplicitAuth)
            throws
            ConsentMgtException;

    /**
     * This method is used to get a consent with or without consent attributes. The following functionality contains in
     * this method.
     *
     * 1. Get existing consent for status validation
     * 2. Optionally gets consent attributes according to the value of withConsentAttributes flag
     * 3. Check whether the retrieved consent involves a file
     *
     * @param consentID               ID of the consent
     * @param withConsentAttributes   flag to determine the consent should be retrieved with attributes or not
     * @return returns ConsentResource
     * @throws ConsentMgtException thrown if any error occur in the process
     */
    ConsentResource getConsent(String consentID, boolean withConsentAttributes)
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
     * @param consentID      ID of the consent
     * @return a detailed consent resource
     * @throws ConsentMgtException thrown if any error occur in the process
     */
    DetailedConsentResource getDetailedConsent(String consentID) throws
            ConsentMgtException;

    /**
     * This method is used to get a detailed consent for the provided consent ID. The detailed consent includes
     * following data if exist in addition to consent resource specific data.
     *
     * 1. Relative consent authorization data
     * 2. Relative consent account mapping data
     * 3. Relative consent attributes
     *
     * @param consentID      ID of the consent
     * @return a detailed consent resource
     * @throws ConsentMgtException thrown if any error occur in the process
     */
    DetailedConsentResource getConsentWithAuthorizationResources(String consentID) throws
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
     * @param newConsentStatus               new consent status
     * @param userID                         user ID (optional)
     * @param applicableStatusToFileUpload   status that the consent should have to upload a file
     * @return true if transaction is a success, throws an exception otherwise
     * @throws ConsentMgtException thrown if any error occur in the process
     */
    boolean createConsentFile(ConsentFile consentFileResource, String newConsentStatus, String userID,
                              String applicableStatusToFileUpload)
            throws
            ConsentMgtException;

    /**
     * This method is used to retrieve the consent file using the related consent ID.
     *
     * @param consentID     consent ID
     * @return the consent file resource
     * @throws ConsentMgtException thrown if an error occurs
     */
    ConsentFile getConsentFile(String consentID) throws
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
     * @param authorizationID   authorization ID
     * @return an authorization resource
     * @throws ConsentMgtException thrown if an error occurs in the process
     */
    AuthorizationResource getAuthorizationResource(String authorizationID, String orgID) throws
            ConsentMgtException;

    /**
     * This method is used to search authorization resources for a given input parameter. Both consent ID and
     * user ID are optional. If both are null, all authorization resources will be returned.
     *
     * @param consentID     consent ID
     * @param userID        user ID
     * @return a list of authorization resources
     * @throws ConsentMgtException thrown if any error occurs in the process
     */
    ArrayList<AuthorizationResource> searchAuthorizations(String consentID, String userID)
            throws
            ConsentMgtException;

    /**
     * This method is used to search authorization resources for a given consent ID.
     *
     * @param consentID     consent ID
     * @return a list of authorization resources
     * @throws ConsentMgtException thrown if any error occurs in the process
     */
    ArrayList<AuthorizationResource> searchAuthorizations(String consentID)
            throws
            ConsentMgtException;

    /**
     * This method is used to search authorization resources for a userId.
     *
     * @param userID    user ID
     * @return a list of authorization resources
     * @throws ConsentMgtException thrown if any error occurs in the process
     */
    ArrayList<AuthorizationResource> searchAuthorizationsForUser(String userID)
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
     * @param authorizationID   the authorization ID of the authorization resource that needs to be updated
     * @param userID            the user of the authorization resource
     * @return the updated authorization resource
     * @throws ConsentMgtException thrown if any error occurs while updating
     */
    AuthorizationResource updateAuthorizationUser(String authorizationID, String userID, String orgID)
            throws
            ConsentMgtException;

    /**
     * This method is used to bind user and accounts to the consent.
     *
     * @param consentResource consent resource
     * @param userID                        user ID
     * @param authID                        ID of the authorization resource
     * @param accountIDsMapWithPermissions  account IDs list with relevant permissions
     * @param newAuthStatus                 new authorization status
     * @param newCurrentConsentStatus       the new status of the current consent
     * @return true if all operations are successful
     * @throws ConsentMgtException thrown if an error occurs in the process
     */
    boolean bindUserAccountsToConsent(ConsentResource consentResource, String userID, String authID,
                                      Map<String, ArrayList<String>> accountIDsMapWithPermissions, String newAuthStatus,
                                      String newCurrentConsentStatus) throws
            ConsentMgtException;

    /**
     * This method is used to bind user and accounts to the consent where permissions for each account is not relevant.
     *
     * @param consentResource consent resource
     * @param userID                    user ID
     * @param authID                    ID of the authorization resource
     * @param accountIDs                account IDs list
     * @param newAuthStatus             new authorization status
     * @param newCurrentConsentStatus   the new status of the current consent
     * @return true if all operations are successful
     * @throws ConsentMgtException thrown if an error occurs in the process
     */
     boolean bindUserAccountsToConsent(ConsentResource consentResource, String userID,
                                             String authID, ArrayList<String> accountIDs,
                                             String newAuthStatus,
                                             String newCurrentConsentStatus)
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
     * This method is used to create account ID and permission mappings for the relevant authorized user. A map is
     * used to represent permissions related to each accountID.
     *
     * @param authID                        authorization ID
     * @param accountIDsMapWithPermissions  account IDs with relative permissions
     * @return returns the list of created consent mapping resources
     * @throws ConsentMgtException thrown if any error occurs
     */
    ArrayList<ConsentMappingResource> createConsentAccountMappings(String authID, Map<String, ArrayList<String>>
                                                                           accountIDsMapWithPermissions)
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
     * This method is used to update the status of account bindings of provided account mapping IDs.
     *
     * @param accountMappingIDs     list of account mapping IDs to be updated
     * @param newMappingStatus      new mapping status
     * @return true is the transaction is a success, throws an exception otherwise
     * @throws ConsentMgtException thrown if any error occurs
     */
    boolean updateAccountMappingStatus(ArrayList<String> accountMappingIDs, String newMappingStatus) throws
            ConsentMgtException;

    /**
     * This method is used to revoke a consent. The following functionality contains in this method.
     *
     * 1. Get existing consent for status validation
     * 2. Update existing consent status
     * 3. Create an audit record for consent update
     * 4. Update account mapping status as inactive
     *
     * @param consentID             ID of the consent
     * @param revokedConsentStatus  the status of the consent after revoked
     * @return true is the transaction is a success, throws an exception otherwise
     * @throws ConsentMgtException thrown if any error occur in the process
     */
    boolean revokeConsent(String consentID, String revokedConsentStatus)
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
     * @param consentID             ID of the consent
     * @param revokedConsentStatus  the status of the consent after revoked
     * @param revokedReason         the reason for consent revocation
     * @return true is the transaction is a success, throws an exception otherwise
     * @throws ConsentMgtException thrown if any error occur in the process
     */
    boolean revokeConsentWithReason(String consentID, String revokedConsentStatus, String revokedReason)
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
     * @param consentID             ID of the consent
     * @param revokedConsentStatus  the status of the consent after revoked
     * @param userID                user ID
     * @return true is the transaction is a success, throws an exception otherwise
     * @throws ConsentMgtException thrown if any error occur in the process
     */
    boolean revokeConsent(String consentID, String revokedConsentStatus, String userID)
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
     * @param consentID             ID of the consent
     * @param revokedConsentStatus  the status of the consent after revoked
     * @param userID                user ID
     * @param revokedReason         the reason for consent revocation
     * @return true is the transaction is a success, throws an exception otherwise
     * @throws ConsentMgtException thrown if any error occur in the process
     */
    boolean revokeConsentWithReason(String consentID, String revokedConsentStatus, String userID, String revokedReason)
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
     * @param consentID             ID of the consent
     * @param revokedConsentStatus  the status of the consent after revoked
     * @param userID                user ID
     * @param shouldRevokeTokens the check to revoke tokens or not when revoking consent
     * @return true is the transaction is a success, throws an exception otherwise
     * @throws ConsentMgtException thrown if any error occur in the process
     */
    boolean revokeConsent(String consentID, String revokedConsentStatus, String userID, boolean shouldRevokeTokens)
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
     * @param consentID             ID of the consent
     * @param revokedConsentStatus  the status of the consent after revoked
     * @param userID                user ID
     * @param shouldRevokeTokens    the check to revoke tokens or not when revoking consent
     * @param revokedReason         the reason for consent revocation
     * @return true is the transaction is a success, throws an exception otherwise
     * @throws ConsentMgtException thrown if any error occur in the process
     */
    boolean revokeConsentWithReason(String consentID, String revokedConsentStatus, String userID,
                                    boolean shouldRevokeTokens, String revokedReason)
            throws
            ConsentMgtException;

    /**
     * This method is used to revoke existing consents for the given clientID, userID, consent type and status
     * combination. Also revokes the tokens related to the consents which are revoked if the flag
     * 'shouldRevokeTokens' is true.
     *
     * @param clientID                  ID of the client
     * @param userID                    ID of the user
     * @param consentType               consent type
     * @param applicableStatusToRevoke  the status that a consent should have for revoking
     * @param revokedConsentStatus      the status should be updated the consent with after revoking
     * @param shouldRevokeTokens        the check to revoke tokens or not when revoking consent
     * @return returns true if successful
     * @throws ConsentMgtException thrown if an error occurs in the process
     */
    boolean revokeExistingApplicableConsents(String clientID, String userID, String consentType,
                                             String applicableStatusToRevoke, String revokedConsentStatus,
                                             boolean shouldRevokeTokens)
            throws
            ConsentMgtException;

    /**
     * This method is used in consent re-authorization scenarios to update the account mappings according to the
     * additional/removed accounts from the new  authorization. Also, the consent status is updated with a provided
     * status. Also, can be used to amend accounts.
     *
     * @param consentID                         consent ID
     * @param authID                            authorization ID
     * @param userID                            user ID for creating the audit record
     * @param accountIDsMapWithPermissions      accounts IDs with relative permissions
     * @param currentConsentStatus              current status of the consent for creating audit record
     * @param newConsentStatus                  new consent status after re-authorization
     * @return true if all operations are successful
     * @throws ConsentMgtException thrown if any error occurs in the process
     */
    boolean reAuthorizeExistingAuthResource(String consentID, String authID, String userID, Map<String,
            ArrayList<String>> accountIDsMapWithPermissions, String currentConsentStatus, String newConsentStatus)
            throws
            ConsentMgtException;

    /**
     * This method is used in consent re-authorization scenarios to update the account mappings according to the
     * additional/removed accounts from the new  authorization. A new authorization resource will be created when
     * re authorizing using this method. Existing authorizations will be updated with a provided status. Also, the
     * consent status is updated with a provided status. Also, can be used to amend accounts.
     *
     * @param consentID                     consent ID
     * @param userID                        user ID
     * @param accountIDsMapWithPermissions  account IDs with relative permissions
     * @param currentConsentStatus          current status of the consent for creating audit record
     * @param newConsentStatus              new consent status after re-authorization
     * @param newExistingAuthStatus         new status of the existing authorizations
     * @param newAuthStatus                 new status of the new authorization
     * @param newAuthType                   new authorization type
     * @return true if all operations are successful
     * @throws ConsentMgtException thrown if any error occurs in the process
     */
    boolean reAuthorizeConsentWithNewAuthResource(String consentID, String userID, Map<String,
            ArrayList<String>> accountIDsMapWithPermissions, String currentConsentStatus, String newConsentStatus,
                                                  String newExistingAuthStatus, String newAuthStatus,
                                                  String newAuthType)
            throws
            ConsentMgtException;


    /**
     * This method is used to store consent attributes related to a particular consent.
     *
     * @param consentID             consent ID
     * @param consentAttributes     consent attribute key and values map
     * @return a consent attributes resource
     * @throws ConsentMgtException thrown if an error occurs in the process
     */
    boolean storeConsentAttributes(String consentID, Map<String, String> consentAttributes)
            throws
            ConsentMgtException;

    /**
     * This method is used to get consent attributes for a provided attribute keys list related to a particular consent.
     *
     * @param consentID             consent ID
     * @param consentAttributeKeys  consent attribute keys list
     * @return a consent attributes resource
     * @throws ConsentMgtException thrown if an error occurs in the process
     */
    ConsentAttributes getConsentAttributes(String consentID, ArrayList<String> consentAttributeKeys)
            throws
            ConsentMgtException;

    /**
     * This method is used to get consent attributes related to a particular consent.
     *
     * @param consentID     consent ID
     * @return a consent attributes resource
     * @throws ConsentMgtException thrown if an error occurs in the process
     */
    ConsentAttributes getConsentAttributes(String consentID) throws
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
     * @param consentID             consent ID
     * @param consentAttributes     attributes to update
     * @return updated consent attributes
     * @throws ConsentMgtException thrown if an error occurs in the process
     */
    ConsentAttributes updateConsentAttributes(String consentID, Map<String, String> consentAttributes)
            throws
            ConsentMgtException;

    /**
     * This method is used to delete the provided consent attributes for a particular consent.
     *
     * @param consentID             consent ID
     * @param attributeKeysList     attributes to delete
     * @return true if deletion is successful
     * @throws ConsentMgtException thrown if an error occurs in the process
     */
    boolean deleteConsentAttributes(String consentID, ArrayList<String> attributeKeysList)
            throws
            ConsentMgtException;

    /**
     * This method is used to search audit records. Useful for auditing purposes. All the input parameters are
     * optional. If all parameters are null, all the audit records will be returned.
     *
     * @param consentID         consent ID
     * @param status            status of the audit records needed
     * @param actionBy          user who performed the status change
     * @param fromTime          from time
     * @param toTime            to time
     * @param statusAuditID     ID of a specific audit record that need to be searched
     * @return a list of consent status audit records
     * @throws ConsentMgtException thrown if an error occurs
     */
    ArrayList<ConsentStatusAuditRecord> searchConsentStatusAuditRecords(String consentID, String status,
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
     * @param consentID                 consent ID
     * @param consentHistoryResource    detailed consent resource and other history parameters of the previous consent
     * @param currentConsentResource    detailed consent resource of the current (new) consent
     * @return true if all operations are successful
     * @throws ConsentMgtException thrown if any error occurs in the process
     */
    boolean storeConsentAmendmentHistory(String consentID, ConsentHistoryResource consentHistoryResource,
                                         DetailedConsentResource currentConsentResource)
            throws
            ConsentMgtException;

    /**
     * This method is used to retrieve consent amendment history for a given consentId. Consent ID is mandatory.
     *
     * @param consentID     consent ID
     * @return a map of consent history resources
     * @throws ConsentMgtException thrown if any error occurs in the process
     */
    Map<String, ConsentHistoryResource> getConsentAmendmentHistoryData(
                                                                       List<String> statusAuditRecordIds,
                                                                       String consentID)
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
     * @param consentID                     consent ID
     * @param consentReceipt                new consent receipt
     * @param consentValidityTime           new consent validity time
     * @param authID                        authorization ID
     * @param accountIDsMapWithPermissions  accounts IDs with relative permissions
     * @param newConsentStatus              new consent status
     * @param consentAttributes             new consent attributes key and values map
     * @param userID                        user ID to create audit record
     * @param additionalAmendmentData       A Data Map to pass any additional data that needs to be amended in the
     *                                     consent
     * @return the updated detailed consent resource
     * @throws ConsentMgtException thrown if any error occurs in the process
     */
    DetailedConsentResource amendDetailedConsent(String consentID, String consentReceipt, Long consentValidityTime,
                                                 String authID, Map<String,
                                                 ArrayList<String>> accountIDsMapWithPermissions,
                                                 String newConsentStatus, Map<String, String> consentAttributes,
                                                 String userID, Map<String, Object> additionalAmendmentData)
            throws
            ConsentMgtException;

    /**
     *
     * @param orgId
     * @param consentID
     * @param consentReceipt
     * @param consentValidityTime
     * @param reAuthorizationResources
     * @param newConsentStatus
     * @param consentAttributes
     * @param userID
     * @param newAuthResources
     * @return
     * @throws ConsentMgtException
     */
    DetailedConsentResource amendDetailedConsentWithBulkAuthResource(String orgId, String consentID,
                                                                     String consentReceipt,
                                                                     Long consentValidityTime,
                                                                     ArrayList<AuthorizationResource>
                                                                             reAuthorizationResources,

                                                                     String newConsentStatus,
                                                                     Map<String, String> consentAttributes,
                                                                     String userID,
                                                                     ArrayList<AuthorizationResource> newAuthResources)
                throws
            ConsentMgtException;


}
