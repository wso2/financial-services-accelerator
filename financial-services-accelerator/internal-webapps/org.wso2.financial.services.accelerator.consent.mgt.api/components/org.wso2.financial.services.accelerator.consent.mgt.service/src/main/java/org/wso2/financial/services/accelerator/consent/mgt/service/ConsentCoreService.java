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
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentHistoryResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentStatusAuditRecord;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;


import java.sql.SQLException;
import java.util.ArrayList;
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
    DetailedConsentResource createConsent(ConsentResource consentResource, ArrayList<AuthorizationResource>
                                                  authorizationResources) throws ConsentMgtException;

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
    DetailedConsentResource getDetailedConsent(String consentId, String orgInfo) throws ConsentMgtException,
            SQLException;

    /**
     * This method is used to create an authorization for a consent.
     *
     * @param authorizationResource     authorization resource
     * @return returns AuthorizationResource
     * @throws ConsentMgtException thrown if any error occurs in the process
     */
    AuthorizationResource createConsentAuthorization(AuthorizationResource authorizationResource)
            throws ConsentMgtException;

    /**
     * This method is to retrieve an authorization resource using a given authorization ID.
     *
     * @param authorizationId   authorization ID
     * @return an authorization resource
     * @throws ConsentMgtException thrown if an error occurs in the process
     */
    AuthorizationResource getAuthorizationResource(String authorizationId, String orgInfo) throws
            ConsentMgtException;

    /***
     * this method is used to update the whole authorization resource
     *
     * @param authorizationResource authorization resource
     * @throws ConsentMgtException thrown if any error occurs in the process
     */
    void updateAuthorizationResource(String authorizationId,
                                     AuthorizationResource authorizationResource,
                                     String orgInfo)
            throws  ConsentMgtException;

    /**
     * This metho is used to delete an authorization resource using the given authorization ID.
     * @param authorizationId   authorization ID
     * @return true if the deletion is successful
     * @throws ConsentMgtException thrown if any error occurs in the process
     */
    boolean deleteAuthorizationResource(String authorizationId)
            throws ConsentMgtException;

    /**
     * This method is used to update status of the consent for a given consentId and userId.
     * @param consentId         consent ID
     * @param newConsentStatus  new consent status
     * @param userId            user ID
     * @param  reason            reason
     * @throws ConsentMgtException thrown if any error occurs in the process
     */
    void updateConsentStatus(String consentId,
                                                        String newConsentStatus, String userId,
                                                        String reason, String orgInfo)
            throws ConsentMgtException;

    /**
     * This method is used to update status of the consent for a given clientId and userId.
     * @param clientId
     * @param status
     * @param reason
     * @param userId
     */
    void bulkUpdateConsentStatus(String orgInfo, String clientId, String status, String reason, String userId,
                                 String consentType, ArrayList<String> applicableExistingStatus)
            throws ConsentMgtException;

    /**
     *
     * @param consentId
     * @return
     * @throws ConsentMgtException
     */
    boolean deleteConsent(String consentId)
            throws ConsentMgtException;

    /**
     * This method is used to revoke a consent. The following functionality contains in this method.
     * @param consentId         Id of the consent
     * @param orgInfo           orgInfo for validation
     * @param actionBy          action by userId
     * @param revokedReason     reason for revoking the consent
     * @return                  boolean status
     * @throws ConsentMgtException thrown if any exception occured in the process
     */
        boolean revokeConsent(String consentId, String orgInfo, String actionBy, String revokedReason)
            throws ConsentMgtException;


    /**
     * This method is used to store consent attributes related to a particular consent.
     *
     * @param consentId             consent ID
     * @param consentAttributes     consent attribute key and values map
     * @return a consent attributes resource
     * @throws ConsentMgtException thrown if an error occurs in the process
     */
    boolean storeConsentAttributes(String consentId, Map<String, String> consentAttributes)
            throws ConsentMgtException;

    /**
     * This method is used to get consent attributes for a provided attribute keys list related to a particular consent.
     *
     * @param consentId             consent ID
     * @param consentAttributeKeys  consent attribute keys list
     * @return a consent attributes resource
     * @throws ConsentMgtException thrown if an error occurs in the process
     */
    ConsentAttributes getConsentAttributes(String consentId, ArrayList<String> consentAttributeKeys)
            throws ConsentMgtException;

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
     * This method is used to delete the provided consent attributes for a particular consent.
     *
     * @param consentId             consent ID
     * @param consentAttributes     attributes to update
     * @return updated consent attributes
     * @throws ConsentMgtException thrown if an error occurs in the process
     */
    ConsentAttributes updateConsentAttributes(String consentId, Map<String, String> consentAttributes)
            throws ConsentMgtException;

    /**
     * This method is used to delete the provided consent attributes for a particular consent.
     *
     * @param consentId             consent ID
     * @param attributeKeysList     attributes to delete
     * @return true if deletion is successful
     * @throws ConsentMgtException thrown if an error occurs in the process
     */
    boolean deleteConsentAttributes(String consentId, ArrayList<String> attributeKeysList)
            throws  ConsentMgtException;

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
            throws ConsentMgtException;

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
            throws  ConsentMgtException;

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
            throws ConsentMgtException;


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
    ArrayList<DetailedConsentResource> searchDetailedConsents(String orgInfo, ArrayList<String> consentIDs,
                                                              ArrayList<String> clientIDs,
                                                              ArrayList<String> consentTypes,
                                                              ArrayList<String> consentStatuses,
                                                              ArrayList<String> userIDs, Long fromTime, Long toTime,
                                                              Integer limit, Integer offset)
            throws ConsentMgtException;


    /**
     * this method is used to update the expiry time of the consent
     */
    boolean updateConsentExpiryTime(String consentId, long consentExpiryTime, String orgInfo)
            throws ConsentMgtException;

}
