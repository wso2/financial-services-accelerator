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

package org.wso2.financial.services.accelerator.consent.mgt.dao;

import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentDataDeletionException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentDataInsertionException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentDataRetrievalException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentDataUpdationException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentAttributes;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentFile;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentHistoryResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentStatusAuditRecord;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This interface access the data storage layer to retrieve, store, delete and update consent management related
 * resources.
 */
public interface ConsentCoreDAO {

    /**
     * This method is used to store the consent resource in the database. The request consent resource object must
     * contain all data in it without the consent ID. A consent ID will be generated and set to the response object
     * if the insertion is successful.
     *
     * @param connection        connection object
     * @param consentResource   ConsentResource object with all required data
     * @return returns the consent resource with the consent ID and created time if insertion is successful
     * @throws ConsentDataInsertionException thrown if a database error occur or an insertion failure
     */
    ConsentResource storeConsentResource(Connection connection, ConsentResource consentResource)
            throws
            ConsentDataInsertionException;

    /**
     * This method is used to retrieve a consent resource for the provided consent ID (without associated consent
     * attributes).
     *
     * @param connection    connection object
     * @param consentId     consent ID
     * @return returns the consent resource related to the provided consent ID without consent attributes.
     * @throws ConsentDataRetrievalException thrown if a database error occurs
     */
    ConsentResource getConsentResource(Connection connection, String consentId)
            throws
            ConsentDataRetrievalException;

    /**
     * This method is used to retrieve a consent resource for the provided consent ID with associated attributes.
     *
     * @param connection    connection object
     * @param consentId     consent ID
     * @return returns the consent resource related to the provided consent ID with additional consent attributes
     * @throws ConsentDataRetrievalException thrown if a database error occurs
     */
    ConsentResource getConsentResourceWithAttributes(Connection connection, String consentId)
            throws
            ConsentDataRetrievalException;

    /**
     * This method is used to retrieve a detailed consent resource for the provided consent ID (includes
     * authorization resources, account mapping resources and consent attributes).
     *
     * @param connection                connection object
     * @param consentId                 consent ID
     * @return returns a detailed consent resource related to the provided consent ID
     * @throws ConsentDataRetrievalException thrown if a database error occurs
     */
    DetailedConsentResource getDetailedConsentResource(Connection connection, String consentId)
            throws
            ConsentDataRetrievalException;



    /**
     * This method is used to update the status of a consent resource. The request consent resource object must be
     * set with a consent ID and the new consent status.
     *
     * @param connection        connection object
     * @param consentId         consent ID of the consent needed to be updated
     * @param consentStatus     the new status that should be updated with
     * @throws ConsentDataUpdationException thrown if a database error occur or an update failure
     */
    void updateConsentStatus(Connection connection, String consentId, String consentStatus)
            throws
            ConsentDataUpdationException;

    /**
     * This method is used to update consent receipt.
     *
     * @param connection        connection object
     * @param consentId         ID of the consent to be amended
     * @param consentReceipt    new consent receipt
     * @throws ConsentDataUpdationException thrown if an error occur in the process
     */
    void updateConsentReceipt(Connection connection, String consentId, String consentReceipt)
            throws
            ConsentDataUpdationException;

    /**
     * This method is used to update consent validity time.
     *
     * @param connection    connection object
     * @param consentId     consent ID
     * @param expiryTime  new validity time
     * @throws ConsentDataUpdationException thrown if any error occurs in the process
     */
    void updateConsentExpiryTime(Connection connection, String consentId, long expiryTime)
            throws
            ConsentDataUpdationException;

    /**
     * This method is used to store the authorization resource in the database. The request authorization resource
     * object must contain data all in it without the authorization ID and the updated time. Both of them will be
     * generated and set to the response object if the insertion is successful.
     *
     * @param connection                connection object
     * @param authorizationResource     authorization resource with all required data
     * @return returns the authorization resource with the updated time if insertion is successful
     * @throws ConsentDataInsertionException thrown if a database error occur or an insertion failure
     */
    AuthorizationResource storeAuthorizationResource(Connection connection, AuthorizationResource authorizationResource)
            throws
            ConsentDataInsertionException;

    /**
     * This method is used to retrieve an authorization resource for the provided authorization ID.
     *
     * @param connection        connection object
     * @param authorizationId   authorization ID
     * @return the relevant authorization resource
     * @throws ConsentDataRetrievalException thrown if a database error occurs
     */
    AuthorizationResource getAuthorizationResource(Connection connection, String authorizationId, String orgID)
            throws
            ConsentDataRetrievalException;

    /**
     * This method is used to retrieve an authorization resource for the provided consent ID.
     * @param connection
     * @param authorizationId
     * @param authorizationResource
     * @return the relevant authorization resource
     * @throws ConsentDataUpdationException
     */
    AuthorizationResource updateAuthorizationResource(Connection connection, String authorizationId,
                                                      AuthorizationResource authorizationResource)
            throws
            ConsentDataUpdationException;

    // delete authorization resource
    /**
     * This method is used to delete an authorization resource for the provided authorization ID.
     *
     * @param connection        connection object
     * @param authorizationId   authorization ID
     * @throws ConsentDataDeletionException thrown if a database error occurs
     */
    void deleteAuthorizationResource(Connection connection, String authorizationId)
            throws
            ConsentDataDeletionException;
////
    /**
     * This method is used to update a given authorization object. The status of the authorization resource provided
     * will be updated with the new status.
     *
     * @param connection                connection object
     * @param authorizationId           authorization ID of the resource needed to be updated
     * @param newAuthorizationStatus    the new authorization status that should be updated with
     * @throws ConsentDataUpdationException thrown if a database error occur or an update failure
     */
    void updateAuthorizationStatus(Connection connection, String authorizationId, String newAuthorizationStatus)
            throws
            ConsentDataUpdationException;

    /**
     * This method is used for updating the user of a given authorization resource. The user ID of the authorization
     * resource provided will be updated with the new user ID.
     *
     * @param connection        connection object
     * @param authorizationId   authorization ID of the resource needed to be updated
     * @param userId            the new user ID that should be updated with
     * @throws ConsentDataUpdationException thrown if a database error occur or an update failure
     */
    void updateAuthorizationUser(Connection connection, String authorizationId, String userId)
            throws
            ConsentDataUpdationException;


    /**
     * This method is used to retrieve consent mapping resources for a given authorization ID.
     *
     * @param connection        connection object
     * @param authorizationId   authorization ID
     * @return a list of all consent mapping resources for the given authorization ID
     * @throws ConsentDataRetrievalException thrown if a database error occurs
     */
    ArrayList<ConsentMappingResource> getConsentMappingResources(Connection connection, String authorizationId)
            throws
            ConsentDataRetrievalException;

    /**
     * This method is used to update given consent mapping resources. All the mapping resources of provided mapping
     * IDs will be updated with the new mapping status provided.
     *
     * @param connection        connection object
     * @param mappingIDs        a list of mapping IDs that needed to be updated
     * @param mappingStatus     the new mapping status that should be updated with
     * @throws ConsentDataUpdationException thrown if a database error occur or an update failure
     */
    void updateConsentMappingStatus(Connection connection, ArrayList<String> mappingIDs,
                                    String mappingStatus)
            throws
            ConsentDataUpdationException;

    /**
     * This method is used to store the consent attributes in the database. The request consent attributes object
     * must be set with a consent ID and consent attribute map.
     * must be set with a consent ID and consent attribute map.
     *
     * @param connection            connection object
     * @param consentAttributes     consent attributes object with consent ID and attributes map
     * @return returns true if insertion is successful
     * @throws ConsentDataInsertionException thrown if a database error occur or an insertion failure
     */
    boolean storeConsentAttributes(Connection connection, ConsentAttributes consentAttributes)
            throws
            ConsentDataInsertionException;

    /**
     * This method is used to retrieve all the consent attributes from the database for the given consent ID.
     *
     * @param connection    connection object
     * @param consentId     consent ID
     * @return returns the consent attributes that matches the provided consentId and consent attribute keys
     * @throws ConsentDataRetrievalException thrown if a database error occurs
     */
    ConsentAttributes getConsentAttributes(Connection connection, String consentId)
            throws
            ConsentDataRetrievalException;

    /**
     * This method is used to retrieve the consent attributes from the database for given attribute keys.
     *
     * @param connection            connection object
     * @param consentId             consent ID
     * @param consentAttributeKeys  the keys of the consent attributes that need to be retrieved
     * @return returns the consent attributes that matches the provided consentId and consent attribute keys
     * @throws ConsentDataRetrievalException thrown if a database error occurs
     */
    ConsentAttributes getConsentAttributes(Connection connection, String consentId,
                                           ArrayList<String> consentAttributeKeys)
            throws
            ConsentDataRetrievalException;

    /**
     * This method is used to retrieve consent attribute using the attribute name.
     *
     * @param connection        connection object
     * @param attributeName     attribute name
     * @return a map with the consent ID and the related attribute value
     * @throws ConsentDataRetrievalException thrown if a database error occurs
     */
    Map<String, String> getConsentAttributesByName(Connection connection, String attributeName)
            throws
            ConsentDataRetrievalException;

    /**
     * This method is used to retrieve consent id using the attribute name and value.
     *
     * @param connection        connection object
     * @param attributeName     attribute name
     * @param attributeValue    attribute value
     * @return Consent ID related to the given attribute key and value
     * @throws ConsentDataRetrievalException  `thrown if a database error occurs
     */
    ArrayList<String> getConsentIdByConsentAttributeNameAndValue(Connection connection, String attributeName,
                                                                 String attributeValue)
            throws
            ConsentDataRetrievalException;

    /**
     * This method is used to update a given list of consent attributes.
     *
     * @param connection             connection object
     * @param consentId              consent ID
     * @param consentAttributes      a map of attributes that should be updated
     * @throws ConsentDataUpdationException thrown if a database error occurs
     */
    void updateConsentAttributes(Connection connection, String consentId, Map<String, String> consentAttributes)
            throws
            ConsentDataUpdationException;

    /**
     * This method is used to delete a given list of consent attributes.
     *
     * @param connection                connection object
     * @param consentId                 consent ID
     * @param consentAttributeKeys      a list of attribute keys that should be deleted
     * @return true if the deletion is successful
     * @throws ConsentDataDeletionException thrown if a database error occurs
     */
    boolean deleteConsentAttributes(Connection connection, String consentId, ArrayList<String> consentAttributeKeys)
            throws
            ConsentDataDeletionException;

    /**
     * This method is used to store the consent file in the database. The request consent file object must be set
     * with a consent ID and the file that needed to be stored.
     *
     * @param connection            connection object
     * @param consentFileResource   consent file resource with consent ID and the file content
     * @return returns true if insertion is successful
     * @throws ConsentDataInsertionException thrown if a database error occur or an insertion failure
     */
    boolean storeConsentFile(Connection connection, ConsentFile consentFileResource)
            throws
            ConsentDataInsertionException;

    /**
     * This method is used to retrieve the consent file from the database.
     *
     * @param connection                connection object
     * @param consentId                 consent ID of the file needed to be retrieved
     * @return returns the requested consent file resource
     * @throws ConsentDataRetrievalException thrown if a database error occur or an retrieval failure
     */
    ConsentFile getConsentFile(Connection connection, String consentId)
            throws
            ConsentDataRetrievalException;

    /**
     * This method is used to search detailed consents for the given lists of parameters. The search will be
     * performed according to the provided input. Any list can contain any number of elements. The conjunctive result
     * will be returned. If all lists are passed as null, all the consents related to other search parameters will be
     * returned. "fromTime" and "toTime" are also optional. "limit" and "offset" are optional combined. If all
     * parameters are null, all the consents will be returned.
     *
     * @param connection        connection object
     * @param consentIDs        consent IDs optional list
     * @param clientIDs         client IDs optional list
     * @param consentTypes      consent types optional list
     * @param consentStatuses   consent statuses optional list
     * @param userIDs           user IDs optional list
     * @param fromTime          from time
     * @param toTime            to time
     * @param limit             limit
     * @param offset            offset
     * @return a list of detailed consent resources according to the provided parameters or the list of all consents
     * if all parameters are null
     * @throws ConsentDataRetrievalException thrown if any error occur
     */
    ArrayList<DetailedConsentResource> searchConsents(Connection connection, String orgID, ArrayList<String> consentIDs,
                                                      ArrayList<String> clientIDs, ArrayList<String> consentTypes,
                                                      ArrayList<String> consentStatuses, ArrayList<String> userIDs,
                                                      Long fromTime, Long toTime, Integer limit, Integer offset)
            throws
            ConsentDataRetrievalException;


    /**
     * This method is used to search authorization resources using following optional parameters. If all the input
     * parameters are null, all the relevant authorization resources will be returned.
     * 1. Consent ID
     * 2. User ID
     *
     * @param connection    connection object
     * @param consentId     consent ID (optional)
     * @param userId        user ID (optional)
     * @return a list of authorization resources
     * @throws ConsentDataRetrievalException thrown if an error occurs in the process
     */
    ArrayList<AuthorizationResource> searchConsentAuthorizations(Connection connection, String consentId, String userId)
            throws
            ConsentDataRetrievalException;

    /**
     * This method is used to store the consent status audit record in the database. The request consent status audit
     * record object must contain all the data in it without the status audit ID and actionTime. They will be generated
     * and set to the response object if the insertion is successful.
     *
     * @param connection connection object
     * @param consentStatusAuditRecord consent status audit record with all required data
     * @return returns the consent status audit record if the insertion is successful
     * @throws ConsentDataInsertionException thrown if a database error occur or an insertion failure
     */
    ConsentStatusAuditRecord storeConsentStatusAuditRecord(Connection connection,
                                                           ConsentStatusAuditRecord consentStatusAuditRecord)
            throws
            ConsentDataInsertionException;


    /**
     * This method is used to retrieve consent status audit records. It queries the consent audit records by the
     * parameter that is provided. All parameters are optional. If no parameters are provided, all the records will
     * be queried.
     *
     * @param connection connection object
     * @param consentId consent ID
     * @param currentStatus current status of the consent
     * @param actionBy the user who performed the status update
     * @param fromTime lower bound of the time of the needed records
     * @param toTime upper bound of the time of the needed records
     * @param statusAuditID status audit ID
     * @return a list of retrieved audit records that matches the provided parameters
     * @throws ConsentDataRetrievalException thrown if a database error occurs
     */
    ArrayList<ConsentStatusAuditRecord> getConsentStatusAuditRecords(Connection connection, String consentId,
                                                                     String currentStatus, String actionBy,
                                                                     Long fromTime, Long toTime,
                                                                     String statusAuditID)
            throws
            ConsentDataRetrievalException;

    /**
     * This method is used to retrieve a list of consent status audit records by consent_ids.
     *
     * @param connection connection object
     * @param consentIDs consentIDs
     * @return returns a list of consent status audit records.
     * @throws ConsentDataRetrievalException thrown if a database error occurs
     */
    ArrayList<ConsentStatusAuditRecord> getConsentStatusAuditRecordsByConsentId(Connection connection,
                                                                                ArrayList<String> consentIDs,
                                                                                Integer limit, Integer offset)
            throws
            ConsentDataRetrievalException;

    /**
     * This method is used to store the changed attribute values of the consent into consent history when an
     * amendment happens to the consent.
     *
     * @param connection connection object
     * @param historyID An identifier for consent history uniquely assigned per consent amendment
     * @param timestamp The timestamp at which the consent amendment happened
     * @param recordID Identifier for each history record (can be ConsentID or MappingID)
     * @param statusAuditRecordId The status audit record ID of the consent
     * @param consentDataType The consent data type stored in each history record (can be ConsentData,
     *                        ConsentAttributesData or ConsentMappingData)
     * @param changedAttributesJsonString The key-value pair json string that represents the changes
     *                          relevant to each history record
     * @param amendmentReason A string that indicates the reason that caused the amendment of the consent
     * @return true if insertion successful
     * @throws ConsentDataInsertionException thrown if any error occurs in the process
     */
    boolean storeConsentAmendmentHistory(Connection connection, String historyID, long timestamp,
                                         String statusAuditRecordId, String recordID,
                                         String consentDataType, String changedAttributesJsonString,
                                         String amendmentReason)
            throws
            ConsentDataInsertionException;

    /**
     * This method is used to retrieve consent amendment history for a given consentId provided with its mappingIDs,
     * AuthorizationIDs.
     *
     * @param connection connection object
     * @param recordIDsList the list of recordIDs relevant to the consent (includes consentId, MappingIDs, AuthIDs)
     * @return a comprehensive map of consent history data
     * @throws ConsentDataRetrievalException thrown if any error occurs in the process
     */
    Map<String, ConsentHistoryResource> retrieveConsentAmendmentHistory(Connection connection,
                                                                        List<String> recordIDsList, String consentId)
            throws
            ConsentDataRetrievalException;

    /**
     * This method is used to fetch consents which has a expiring time as a consent attribute
     * (eligible for expiration).
     * @throws ConsentDataRetrievalException thrown if any error occurs in the process
     */
    ArrayList<DetailedConsentResource> getExpiringConsents(Connection connection, String orgID,
                                                           String statusesEligibleForExpiration)
            throws
            ConsentDataRetrievalException;

    /**
     *
     * @param connection
     * @param consentId
     */
    void deleteConsent(Connection connection, String consentId);
}
