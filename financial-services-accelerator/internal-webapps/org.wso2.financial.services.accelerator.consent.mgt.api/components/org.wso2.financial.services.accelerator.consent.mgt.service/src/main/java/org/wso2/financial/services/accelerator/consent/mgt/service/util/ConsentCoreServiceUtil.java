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

package org.wso2.financial.services.accelerator.consent.mgt.service.util;

import com.google.gson.Gson;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.consent.mgt.dao.ConsentCoreDAO;
import org.wso2.financial.services.accelerator.consent.mgt.dao.constants.ConsentMgtDAOConstants;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentDataDeletionException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentDataInsertionException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentDataRetrievalException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentDataUpdationException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentMgtException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentAttributes;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentHistoryResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentStatusAuditRecord;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.service.constants.ConsentCoreServiceConstants;
import org.wso2.financial.services.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.Response;


/**
 * Consent Core Service Util.
 */
@SuppressFBWarnings("CRLF_INJECTION_LOGS")
public class ConsentCoreServiceUtil {

    private static final Log log = LogFactory.getLog(ConsentCoreServiceUtil.class);


    /**
     * Create an authorizable consent with audit record.
     *
     * @param connection              Database connection
     * @param consentCoreDAO          Consent core DAO
     * @param consentResource         Consent resource
     * @param authorizationResources  auth resources
     * @return DetailedConsentResource
     * @throws ConsentDataInsertionException Consent data insertion exception
     * @throws ConsentMgtException           Consent management exception
     */
    public static DetailedConsentResource createAuthorizableConsentWithAuditRecordWithBulkAuthResources(
            Connection connection,
            ConsentCoreDAO consentCoreDAO,
            ConsentResource consentResource,
            List<AuthorizationResource> authorizationResources)
            throws
            ConsentDataInsertionException,
            ConsentMgtException,
            ConsentDataRetrievalException {

        boolean isConsentAttributesStored = false;
        AuthorizationResource storedAuthorizationResource = null;

        // Create consent
        if (log.isDebugEnabled()) {
            log.debug(("Creating the consent for ID:" + consentResource.getConsentID())
                    .replaceAll("[\r\n]", ""));
        }
        ConsentResource storedConsentResource = consentCoreDAO.storeConsentResource(connection, consentResource);
        String consentID = storedConsentResource.getConsentID();

        // Store consent attributes if available
        if (MapUtils.isNotEmpty(consentResource.getConsentAttributes())) {
            ConsentAttributes consentAttributes = new ConsentAttributes(consentID,
                    consentResource.getConsentAttributes());

            if (log.isDebugEnabled()) {
                log.debug(String.format("Storing consent attributes for the consent of ID: %s", consentAttributes
                        .getConsentID().replaceAll("[\r\n]", "")));
            }
            isConsentAttributesStored = consentCoreDAO.storeConsentAttributes(connection, consentAttributes);
        }


        //TODO: authorizationResources.get(0).getUserID()
        ///  authorizationResources is null and get the userId

        String userId = authorizationResources != null ?
                !authorizationResources.isEmpty() ? authorizationResources.get(0).getUserID() :
                        consentResource.getClientID() :
                consentResource.getClientID();


        ArrayList<AuthorizationResource> storedAuthorizationResources = new ArrayList<>();
        ArrayList<ConsentMappingResource> storedConsentMappingResources = new ArrayList<>();

        // Create an authorization resource if isImplicitAuth parameter is true
        if (authorizationResources != null) {
            /* Setting userID as null since at this point, there is no userID in this flow. User ID can be
                   updated in authorization flow */


            for (AuthorizationResource authorizationResource : authorizationResources) {
                authorizationResource.setUpdatedTime(System.currentTimeMillis());
                authorizationResource.setConsentID(consentID);


                if (log.isDebugEnabled()) {
                    log.debug(("Storing authorization resource for consent of ID: " + authorizationResource
                            .getConsentID()).replaceAll("[\r\n]", ""));
                }

                storedAuthorizationResource = consentCoreDAO.storeAuthorizationResource(connection,
                        authorizationResource);



                storedAuthorizationResources.add(storedAuthorizationResource);
            }
        }

        DetailedConsentResource detailedConsentResource =
                new DetailedConsentResource(storedConsentResource.getOrgID(), consentID,
                        storedConsentResource.getClientID(), storedConsentResource.getReceipt(),
                        storedConsentResource.getConsentType(), storedConsentResource.getCurrentStatus(),
                         storedConsentResource.getExpiryTime(),
                        storedConsentResource.getCreatedTime(), storedConsentResource.getUpdatedTime(),
                        storedConsentResource.isRecurringIndicator(), consentResource.getConsentAttributes(),
                        new ArrayList<>(),
                        new ArrayList<>());


        if (isConsentAttributesStored) {
            detailedConsentResource.setConsentAttributes(consentResource.getConsentAttributes());
        }
        if (authorizationResources != null) {
            detailedConsentResource.setAuthorizationResources(storedAuthorizationResources);
            detailedConsentResource.setConsentMappingResources(storedConsentMappingResources);
        }
                /* Create audit record, setting previous consent status as null since this is the first time the
           consent is created and execute state change listener */
        HashMap<String, Object> consentDataMap = new HashMap<>();
        consentDataMap.put(ConsentCoreServiceConstants.DETAILED_CONSENT_RESOURCE, detailedConsentResource);
        DetailedConsentResource oldDetailedConsent = new DetailedConsentResource();
        oldDetailedConsent.setConsentAttributes(new HashMap<>());
        oldDetailedConsent.setAuthorizationResources(new ArrayList<>());
        oldDetailedConsent.setConsentMappingResources(new ArrayList<>());
        consentDataMap.put(ConsentCoreServiceConstants.CONSENT_AMENDMENT_HISTORY_RESOURCE, oldDetailedConsent);

        postStateChange(connection, consentCoreDAO, consentID, userId,
                consentResource.getCurrentStatus(),
                null, ConsentCoreServiceConstants.CREATE_CONSENT_REASON,
                consentResource.getClientID(), consentDataMap);
        return detailedConsentResource;
    }

    /**
    /**
     * Update existing consent statuses and revoke their account mappings.
     *
     * @param connection                       Database connection
     * @param consentCoreDAO                   Consent core DAO
     * @param consentResource                  Consent resource
     * @param userID                           User ID
     * @param applicableExistingConsentsStatus Applicable existing consents status
     * @param newExistingConsentStatus         New existing consent status
     * @throws ConsentDataRetrievalException If an error occurs when retrieving existing consents
     * @throws ConsentDataUpdationException  If an error occurs when updating existing consents
     * @throws ConsentDataInsertionException If an error occurs when inserting data
     * @throws ConsentMgtException           Consent management exception
     */
    public static void updateExistingConsentStatusesAndRevokeAccountMappings(Connection connection,
                                                                             ConsentCoreDAO consentCoreDAO,
                                                                             ConsentResource consentResource,
                                                                             String userID,
                                                                             String applicableExistingConsentsStatus,
                                                                             String newExistingConsentStatus)
            throws
            ConsentDataRetrievalException,
            ConsentDataUpdationException,
            ConsentDataInsertionException,
            ConsentMgtException {

        ArrayList<String> accountMappingIDsList = new ArrayList<>();

        ArrayList<String> clientIDsList = constructArrayList(consentResource.getClientID());
        ArrayList<String> userIDsList = constructArrayList(userID);
        ArrayList<String> consentTypesList = constructArrayList(consentResource.getConsentType());
        ArrayList<String> consentStatusesList = constructArrayList(applicableExistingConsentsStatus);

        // Get existing applicable consents
        log.debug("Retrieving existing authorized consents");
        ArrayList<DetailedConsentResource> retrievedExistingAuthorizedConsentsList =
                consentCoreDAO.searchConsents(connection, null, null, clientIDsList, consentTypesList,
                        consentStatusesList, userIDsList, null, null, null,
                        null);

        for (DetailedConsentResource resource : retrievedExistingAuthorizedConsentsList) {

            String previousConsentStatus = resource.getCurrentStatus();

            // Update existing consents as necessary
            if (log.isDebugEnabled()) {
                log.debug(("Updating existing consent statuses with the new status provided for consent ID: "
                        + resource.getConsentID()).replaceAll("[\r\n]", ""));
            }
            consentCoreDAO.updateConsentStatus(connection, resource.getConsentID(), newExistingConsentStatus);

            // Create audit record for each consent update
            if (log.isDebugEnabled()) {
                log.debug(("Creating audit record for the consent update of consent ID: "
                        + resource.getConsentID()).replaceAll("[\r\n]", ""));
            }
            // Create an audit record execute state change listener
            HashMap<String, Object> consentDataMap = new HashMap<>();
            consentDataMap.put(ConsentCoreServiceConstants.DETAILED_CONSENT_RESOURCE, resource);
            postStateChange(connection, consentCoreDAO, resource.getConsentID(), userID,
                    newExistingConsentStatus, previousConsentStatus,
                    ConsentCoreServiceConstants.CREATE_EXCLUSIVE_AUTH_CONSENT_REASON, resource.getClientID(),
                    consentDataMap);

            // Extract account mapping IDs for retrieved applicable consents
            if (log.isDebugEnabled()) {
                log.debug(("Extracting account mapping IDs from consent ID: " +
                        resource.getConsentID()).replaceAll("[\r\n]", ""));
            }
            resource.getConsentMappingResources().forEach(mappingResource ->
                    accountMappingIDsList.add(mappingResource.getMappingID()));
        }

        // Update account mappings as inactive
        log.debug("Deactivating account mappings");
        consentCoreDAO.updateConsentMappingStatus(connection, accountMappingIDsList,
                ConsentCoreServiceConstants.INACTIVE_MAPPING_STATUS);
    }

    /**
     * Method to create an audit record in post consent state change.
     *
     * @param connection            Database connection
     * @param consentCoreDAO        Consent core DAO
     * @param consentID             Consent ID
     * @param userID                User ID
     * @param newConsentStatus      New consent status
     * @param previousConsentStatus Previous consent status
     * @param reason                Reason for the status change
     * @param clientId              Client ID
     * @param consentDataMap        Consent data map
     * @throws ConsentDataInsertionException If an error occurs when storing the audit record
     * @throws ConsentMgtException           Consent management exception
     */
    public static void postStateChange(Connection connection, ConsentCoreDAO consentCoreDAO, String consentID,
                                       String userID, String newConsentStatus, String previousConsentStatus,
                                       String reason, String clientId, Map<String, Object> consentDataMap)
            throws
            ConsentDataInsertionException,
            ConsentMgtException,
            ConsentDataRetrievalException {

        ConsentStatusAuditRecord consentStatusAuditRecord = createAuditRecord(connection, consentCoreDAO, consentID,
                userID,
                newConsentStatus,
                previousConsentStatus,
                reason);

        DetailedConsentResource detailedCurrentConsent = (DetailedConsentResource)
                consentDataMap.get(ConsentCoreServiceConstants.DETAILED_CONSENT_RESOURCE);
        DetailedConsentResource detailedHistoryConsent = (DetailedConsentResource)
                consentDataMap.get(ConsentCoreServiceConstants.CONSENT_AMENDMENT_HISTORY_RESOURCE);

        if (detailedCurrentConsent == null) {
            // get the current consent details
            detailedCurrentConsent = consentCoreDAO.getDetailedConsentResource(connection, consentID);
        }

        ConsentHistoryResource consentHistoryResource = new ConsentHistoryResource();
        consentHistoryResource.setDetailedConsentResource(detailedHistoryConsent);
        consentHistoryResource.setReason(reason);
        consentHistoryResource.setTimestamp(System.currentTimeMillis());

        ConsentCoreServiceImpl consentCoreService = new ConsentCoreServiceImpl();
        boolean result = consentCoreService.storeConsentAmendmentHistory(
                consentStatusAuditRecord.getStatusAuditID(),
                consentHistoryResource,
                detailedCurrentConsent);

        if (result) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Consent Amendment History of consentID: %s persisted successfully.",
                        consentID));
            }
        } else {
            log.error(String.format("Failed to persist Consent Amendment History of consentID : %s. ",
                    consentID));
        }


    }

    /**
     * Create an audit record for the consent status change.
     *
     * @param connection            database connection
     * @param consentCoreDAO        consent core DAO
     * @param consentID             consent ID
     * @param userID                user ID
     * @param newConsentStatus      new consent status
     * @param previousConsentStatus previous consent status
     * @param reason                reason for the status change
     * @return
     * @throws ConsentDataInsertionException thrown if an error occurs when storing the audit record
     */
    public static ConsentStatusAuditRecord createAuditRecord(Connection connection, ConsentCoreDAO consentCoreDAO,
                                                             String consentID,
                                                             String userID, String newConsentStatus,
                                                             String previousConsentStatus,
                                                             String reason) throws
            ConsentDataInsertionException {

        // Create an audit record
        String actionBy = StringUtils.isNotEmpty(userID) ? userID : null;
        ConsentStatusAuditRecord consentStatusAuditRecord = new ConsentStatusAuditRecord(consentID, newConsentStatus,
                0, reason, actionBy, previousConsentStatus);

        consentStatusAuditRecord.setStatusAuditID(String.valueOf(UUID.randomUUID()));
        if (log.isDebugEnabled()) {
            log.debug(("Storing audit record for consent of ID: " +
                    consentStatusAuditRecord.getConsentID()).replaceAll("[\r\n]", ""));
        }
        return consentCoreDAO.storeConsentStatusAuditRecord(connection, consentStatusAuditRecord);
    }


    /**
     * Construct an ArrayList with a single field.
     *
     * @param field Field to be added to the ArrayList
     * @return ArrayList with a single field
     */
    public static ArrayList<String> constructArrayList(String field) {
        return new ArrayList<String>() {
            {
                add(field);
            }
        };
    }

    /**
     * Method to update the accounts of a consent.
     *
     * @param connection                   Database connection
     * @param consentCoreDAO               Consent core DAO
     * @param authID                       Authorization ID
     * @param accountIDsMapWithPermissions Account IDs map with permissions
     * @param detailedConsentResource      Detailed consent resource
     * @param isNewAuthResource            Is new authorization resource
     * @throws ConsentDataInsertionException If an error occurs when inserting data
     * @throws ConsentDataUpdationException  If an error occurs when updating data
     */
    public static void updateAccounts(Connection connection, ConsentCoreDAO consentCoreDAO, String authID,
                                      Map<String, ArrayList<String>> accountIDsMapWithPermissions,
                                      DetailedConsentResource detailedConsentResource, boolean isNewAuthResource)
            throws
            ConsentDataInsertionException,
            ConsentDataUpdationException {

        // Get existing consent account mappings
        log.debug("Retrieve existing active account mappings");
        ArrayList<ConsentMappingResource> existingAccountMappings =
                detailedConsentResource.getConsentMappingResources();

        // Determine unique account IDs
        HashSet<String> existingAccountIDs = new HashSet<>();
        existingAccountMappings.forEach(mapping -> existingAccountIDs.add(
                mapping.getAccountID()));

        ArrayList<String> existingAccountIDsList = new ArrayList<>(existingAccountIDs);

        ArrayList<String> reAuthorizedAccounts = new ArrayList<>();
        accountIDsMapWithPermissions.forEach((accountID, permissions) ->
                reAuthorizedAccounts.add(accountID));

        // Determine whether the account should be removed or added
        ArrayList<String> accountsToRevoke = new ArrayList<>(existingAccountIDsList);
        accountsToRevoke.removeAll(reAuthorizedAccounts);

        ArrayList<String> accountsToAdd = new ArrayList<>(reAuthorizedAccounts);

        if (isNewAuthResource) {
            ArrayList<String> commonAccountsFromReAuth = new ArrayList<>(existingAccountIDs);
            commonAccountsFromReAuth.retainAll(accountsToAdd);
            accountsToAdd.removeAll(existingAccountIDs);
            accountsToAdd.addAll(commonAccountsFromReAuth);
        } else {
            accountsToAdd.removeAll(existingAccountIDs);
        }

        if (!accountsToAdd.isEmpty()) {
            // Store accounts as consent account mappings
            log.debug("Add extra accounts as account mappings");
            for (String accountID : accountsToAdd) {
                ArrayList<String> permissions = accountIDsMapWithPermissions.get(accountID);
                for (String permission : permissions) {
                    ConsentMappingResource consentMappingResource = new ConsentMappingResource(
                            authID, accountID, permission, ConsentCoreServiceConstants.ACTIVE_MAPPING_STATUS);
                    ConsentMappingResource storedConsentMappingResource =
                            consentCoreDAO.storeConsentMappingResource(connection,
                                    consentMappingResource);
                    existingAccountMappings.add(storedConsentMappingResource);
                }
            }
        }
        if (!accountsToRevoke.isEmpty()) {
            // Update mapping statuses of revoking accounts to inactive
            log.debug("Deactivate unwanted account mappings");
            ArrayList<String> mappingIDsToUpdate = new ArrayList<>();
            for (String accountID : accountsToRevoke) {
                existingAccountMappings.stream()
                        .filter(resource -> accountID.equals(resource.getAccountID()))
                        .forEach(resource -> mappingIDsToUpdate.add(resource.getMappingID()));
            }
            consentCoreDAO.updateConsentMappingStatus(connection, mappingIDsToUpdate,
                    ConsentCoreServiceConstants.INACTIVE_MAPPING_STATUS);
        }
    }

    /**
     * Method to update the consent attributes.
     *
     * @param connection        Database connection
     * @param consentCoreDAO    Consent core DAO
     * @param consentID         Consent ID
     * @param consentAttributes Consent attributes
     * @throws ConsentDataInsertionException If an error occurs when inserting data
     * @throws ConsentDataDeletionException  If an error occurs when deleting data
     */
    public static void updateConsentAttributes(Connection connection, ConsentCoreDAO consentCoreDAO,
                                               String consentID, Map<String, String> consentAttributes)
            throws
            ConsentDataInsertionException,
            ConsentDataDeletionException {

        // delete existing consent attributes
        if (log.isDebugEnabled()) {
            log.debug(String.format("Deleting attributes for the consent ID: %s",
                    consentID.replaceAll("[\r\n]", "")));
        }
        consentCoreDAO.deleteConsentAttributes(connection, consentID, new ArrayList<>(consentAttributes.keySet()));

        // store new set of consent attributes
        ConsentAttributes consentAttributesObject = new ConsentAttributes(consentID, consentAttributes);
        if (log.isDebugEnabled()) {
            log.debug(String.format("Storing consent attributes for the consent of ID: %s",
                    consentID.replaceAll("[\r\n]", "")));
        }
        consentCoreDAO.storeConsentAttributes(connection, consentAttributesObject);
    }

    /**
     * Method to get the changed values from consent amendment compared to the original consent.
     *
     * @param newConsentResource New Consent Resource after the amendment
     * @param oldConsentResource Existing Consent Resource
     * @return JSON object with the changed values
     */
    public static JSONObject getChangedBasicConsentDataJSON(DetailedConsentResource newConsentResource,
                                                            DetailedConsentResource oldConsentResource) {

        JSONObject changedConsentDataJson = new JSONObject();
        if (!newConsentResource.getReceipt().equals(oldConsentResource.getReceipt())) {
            changedConsentDataJson.put(ConsentCoreServiceConstants.RECEIPT, oldConsentResource.getReceipt());
        }
        if (newConsentResource.getExpiryTime() != oldConsentResource.getExpiryTime()) {
            changedConsentDataJson.put(ConsentCoreServiceConstants.EXPIRY_TIME,
                    String.valueOf(oldConsentResource.getExpiryTime()));
        }
        if (newConsentResource.getUpdatedTime() != oldConsentResource.getUpdatedTime()) {
            changedConsentDataJson.put(ConsentCoreServiceConstants.UPDATED_TIME,
                    String.valueOf(oldConsentResource.getUpdatedTime()));
        }
        if (!newConsentResource.getCurrentStatus().equals(oldConsentResource.getCurrentStatus())) {
            changedConsentDataJson.put(ConsentCoreServiceConstants.CURRENT_STATUS,
                    String.valueOf(oldConsentResource.getCurrentStatus()));
        }
        return changedConsentDataJson;
    }

    /**
     * Method to get the changed consent attribute values from consent amendment compared to the original consent.
     *
     * @param newConsentAttributes New consent attributes after the amendment
     * @param oldConsentAttributes Existing consent attributes
     * @return JSON object with the changed consent attributes
     */
    public static JSONObject getChangedConsentAttributesDataJSON(Map<String, String> newConsentAttributes,
                                                                 Map<String, String> oldConsentAttributes) {

        JSONObject changedConsentAttributesJson = new JSONObject();

        oldConsentAttributes.entrySet().stream()
                .filter(oldConsentAttribute -> !newConsentAttributes.containsKey(
                        oldConsentAttribute.getKey()))
                .forEach(oldConsentAttribute -> {
                    //store any removed consent attribute in current consent to the changedConsentAttributesJson of
                    //the immediate past consent amendment history with a null value
                    changedConsentAttributesJson.put(oldConsentAttribute.getKey(), oldConsentAttribute.getValue());
                });

        newConsentAttributes.entrySet().stream().filter(newConsentAttribute -> !oldConsentAttributes
                .containsKey(newConsentAttribute.getKey())).forEach(newConsentAttribute -> {
            //store any new consent attribute in current consent to the changedConsentAttributesJson of
            //the immediate past consent amendment history with a null value
            changedConsentAttributesJson.put(newConsentAttribute.getKey(), newConsentAttribute.getValue());
        });

        // get same keys in old and new consent attributes
        for (Map.Entry<String, String> newConsentAttribute : newConsentAttributes.entrySet()) {
            if (oldConsentAttributes.containsKey(newConsentAttribute.getKey())) {
                if (!newConsentAttribute.getValue().equals(oldConsentAttributes.get(newConsentAttribute.getKey()))) {
                    //store only the consent attributes with a changed value to the consent amendment history
                    changedConsentAttributesJson.put(newConsentAttribute.getKey(),
                            oldConsentAttributes.get(newConsentAttribute.getKey()));
                }
            }
        }
        return changedConsentAttributesJson;
    }

    /**
     * Method to get the changed consent mappings from consent amendment compared to the original consent.
     *
     * @param newConsentMappings New consent mappings after the amendment
     * @param oldConsentMappings Existing consent mappings
     * @return JSON object with the changed consent mappings
     */
    public static Map<String, JSONObject> getChangedConsentMappingDataJSONMap(ArrayList<ConsentMappingResource>
                                                                                      newConsentMappings,
                                                                              ArrayList<ConsentMappingResource>
                                                                                      oldConsentMappings) {

        Map<String, JSONObject> changedConsentMappingsJsonDataMap = new HashMap<>();
        ArrayList<String> existingConsentMappingIds = new ArrayList<>();
        for (ConsentMappingResource newMapping : newConsentMappings) {
            JSONObject changedConsentMappingJson = new JSONObject();
            for (ConsentMappingResource oldMapping : oldConsentMappings) {
                if (newMapping.getMappingID().equals(oldMapping.getMappingID())) {
                    existingConsentMappingIds.add(newMapping.getMappingID());
                    if (!newMapping.getMappingStatus().equals(oldMapping.getMappingStatus())) {
                        //store only the mapping-ids with a changed Mapping Status to the consent amendment history
                        changedConsentMappingJson.put(ConsentCoreServiceConstants.MAPPING_STATUS,
                                oldMapping.getMappingStatus());
                    }
                    break;
                }
            }
            if (!changedConsentMappingJson.isEmpty()) {
                changedConsentMappingsJsonDataMap.put(newMapping.getMappingID(), changedConsentMappingJson);
            }
            // store any new mapping-ids in current consent to the immediate past consent amendment history with
            // 'null' value
            if (!existingConsentMappingIds.contains(newMapping.getMappingID())) {
                changedConsentMappingsJsonDataMap.put(newMapping.getMappingID(), null);
            }
        }
        return changedConsentMappingsJsonDataMap;
    }

    /**
     * Method to get the changed consent auth resources from consent amendment compared to the original consent.
     *
     * @param newConsentAuthResources New consent auth resources after the amendment
     * @param oldConsentAuthResources Existing auth resources
     * @return JSON object with the changed consent mappings
     */
    public static Map<String, JSONObject> getChangedConsentAuthResourcesDataJSONMap(ArrayList<AuthorizationResource>
                                                                                            newConsentAuthResources,
                                                                                    ArrayList<AuthorizationResource>
                                                                                            oldConsentAuthResources) {

        Map<String, JSONObject> changedConsentAuthResourcesJsonDataMap = new HashMap<>();

        ArrayList<String> existingConsentAuthResourceIds = new ArrayList<>();
        for (AuthorizationResource newAuthResource : newConsentAuthResources) {
            oldConsentAuthResources.stream()
                    .filter(oldAuthResource -> newAuthResource.getAuthorizationID().
                            equals(oldAuthResource.getAuthorizationID()))
                    .forEach(oldAuthResource ->
                            existingConsentAuthResourceIds.add(newAuthResource.getAuthorizationID()));

            // store any new authorization-ids in current consent (an Auth Resource not available in previous consent,
            // but newly added in current consent) to the immediate past consent amendment history with 'null' value
            if (!existingConsentAuthResourceIds.contains(newAuthResource.getAuthorizationID())) {
                changedConsentAuthResourcesJsonDataMap.put(newAuthResource.getAuthorizationID(), null);
            }

            // compare old auth resrouce status with new auth resource status
            JSONObject changedConsentAuthResourceJson = new JSONObject();
            for (AuthorizationResource oldAuthResource : oldConsentAuthResources) {
                if (newAuthResource.getAuthorizationID().equals(oldAuthResource.getAuthorizationID())) {
                    if (!newAuthResource.getAuthorizationStatus().equals(
                            oldAuthResource.getAuthorizationStatus())) {
                        //store only the auth resources with a changed status to the consent amendment history
                        changedConsentAuthResourceJson.put(ConsentCoreServiceConstants.AUTHORIZATION_STATUS,
                                oldAuthResource.getAuthorizationStatus());
                        changedConsentAuthResourcesJsonDataMap.put(newAuthResource.getAuthorizationID(),
                                changedConsentAuthResourceJson);

                    }
                    break;
                }
            }

        }
        return changedConsentAuthResourcesJsonDataMap;
    }

    /**
     * Method to get the consent mapping id and consent auth resource id as a list for consent history retrieval.
     *
     * @param detailedConsentResource Changed attribute JSON string
     * @return List of record IDs
     */
    public static List<String> getRecordIdListForConsentHistoryRetrieval(
            DetailedConsentResource detailedConsentResource) {

        List<String> recordIdsList = new ArrayList<>();
        recordIdsList.add(detailedConsentResource.getConsentID());

        for (ConsentMappingResource mappingResource : detailedConsentResource.getConsentMappingResources()) {
            recordIdsList.add(mappingResource.getMappingID());
        }
        for (AuthorizationResource authResource : detailedConsentResource.getAuthorizationResources()) {
            recordIdsList.add(authResource.getAuthorizationID());
        }
        return recordIdsList;
    }

    /**
     * Method to process the consent amendment history data.
     *
     * @param consentAmendmentHistoryRetrievalResult Consent amendment history retrieval result
     * @param currentConsentResource                 Current consent resource
     * @return Consent amendment history data map
     * @throws ConsentMgtException Consent management exception
     */
    public static Map<String, ConsentHistoryResource> processConsentAmendmentHistoryData(
            Map<String, ConsentHistoryResource> consentAmendmentHistoryRetrievalResult,
            DetailedConsentResource currentConsentResource) throws
            ConsentMgtException {

        Gson gson = new Gson();
        Map<String, ConsentHistoryResource> consentAmendmentHistoryDataMap = new LinkedHashMap<>();

        for (Map.Entry<String, ConsentHistoryResource> consentHistoryDataEntry :
                consentAmendmentHistoryRetrievalResult.entrySet()) {
            String historyId = consentHistoryDataEntry.getKey();
            ConsentHistoryResource consentHistoryResource = consentHistoryDataEntry.getValue();
            consentHistoryResource.setDetailedConsentResource(currentConsentResource.clone());

            for (Map.Entry<String, Object> consentHistoryDataTypeEntry :
                    consentHistoryResource.getChangedAttributesJsonDataMap().entrySet()) {

                String consentDataType = consentHistoryDataTypeEntry.getKey();
                Object changedAttributes = consentHistoryDataTypeEntry.getValue();

                if (ConsentCoreServiceConstants.TYPE_CONSENT_BASIC_DATA.equals(consentDataType)) {
                    JSONObject changedValuesJSON = parseChangedAttributeJsonString(changedAttributes.toString());
                    if (changedValuesJSON.containsKey(ConsentCoreServiceConstants.RECEIPT)) {
                        currentConsentResource.setReceipt(
                                (String) changedValuesJSON.get(ConsentCoreServiceConstants.RECEIPT));
                    }
                    if (changedValuesJSON.containsKey(ConsentCoreServiceConstants.EXPIRY_TIME)) {
                        currentConsentResource.setExpiryTime(Long.parseLong((String)
                                changedValuesJSON.get(ConsentCoreServiceConstants.EXPIRY_TIME)));
                    }
                    if (changedValuesJSON.containsKey(ConsentCoreServiceConstants.UPDATED_TIME)) {
                        currentConsentResource.setUpdatedTime(Long.parseLong((String)
                                changedValuesJSON.get(ConsentCoreServiceConstants.UPDATED_TIME)));
                    }
                    if (changedValuesJSON.containsKey(ConsentCoreServiceConstants.CURRENT_STATUS)) {
                        currentConsentResource.setCurrentStatus((String)
                                changedValuesJSON.get(ConsentCoreServiceConstants.CURRENT_STATUS));
                    }

                } else if (ConsentCoreServiceConstants.TYPE_CONSENT_ATTRIBUTES_DATA.equals(consentDataType)) {
                    JSONObject changedValuesJSON = parseChangedAttributeJsonString(changedAttributes.toString());
                    for (Map.Entry<String, Object> attribute : changedValuesJSON.entrySet()) {
                        Object attributeValue = attribute.getValue();
                        if (attributeValue == null) {
                            //Ignore the consent attribute from the consent history if it's value is stored as null
                            currentConsentResource.getConsentAttributes().remove(attribute.getKey());
                        } else {
                            Map<String, String> consentAttributes = currentConsentResource.getConsentAttributes();
                            consentAttributes.put(attribute.getKey(),
                                    attributeValue.toString());
                            currentConsentResource.setConsentAttributes(consentAttributes);


                        }
                    }

                } else if (ConsentCoreServiceConstants.TYPE_CONSENT_MAPPING_DATA.equals(consentDataType)) {
                    Map<String, Object> changedConsentMappingsDataMap = (Map<String, Object>) changedAttributes;
                    ArrayList<ConsentMappingResource> consentMappings =
                            currentConsentResource.getConsentMappingResources();
                    ArrayList<ConsentMappingResource> consentMappingsHistory = new ArrayList<>();
                    for (ConsentMappingResource mapping : consentMappings) {
                        String mappingID = mapping.getMappingID();
                        if (changedConsentMappingsDataMap.containsKey(mappingID)) {
                            JSONObject changedValuesJSON = parseChangedAttributeJsonString(
                                    changedConsentMappingsDataMap.get(mappingID).toString());
                            if (changedValuesJSON.isEmpty()) {
                                //Skip setting the mapping to consent history if the value is null
                                continue;
                            }
                            //set the value available in the history as the mapping status
                            mapping.setMappingStatus(
                                    changedValuesJSON.get(ConsentCoreServiceConstants.MAPPING_STATUS).toString());
                        }
                        consentMappingsHistory.add(gson.fromJson(gson.toJson(mapping), ConsentMappingResource.class));
                    }
                    currentConsentResource.setConsentMappingResources(consentMappingsHistory);

                } else if (ConsentCoreServiceConstants.TYPE_CONSENT_AUTH_RESOURCE_DATA.equals(consentDataType)) {
                    Map<String, Object> changedConsentAuthResourceDataMap = (Map<String, Object>) changedAttributes;
                    ArrayList<AuthorizationResource> consentAuthResources = currentConsentResource
                            .getAuthorizationResources();
                    ArrayList<AuthorizationResource> consentAuthResourceHistory = new ArrayList<>();
                    for (AuthorizationResource authResource : consentAuthResources) {
                        String authID = authResource.getAuthorizationID();
                        if (changedConsentAuthResourceDataMap.containsKey(authID)) {
                            JSONObject changedValuesJSON = parseChangedAttributeJsonString(
                                    changedConsentAuthResourceDataMap.get(authID).toString());
                            if (changedValuesJSON.isEmpty()) {
                                //Skip setting the auth resource to consent history if the value is null
                                continue;
                            }
                        }
                        consentAuthResourceHistory.add(gson.fromJson(gson.toJson(authResource),
                                AuthorizationResource.class));
                    }
                    currentConsentResource.setAuthorizationResources(consentAuthResourceHistory);
                }
            }
            consentAmendmentHistoryDataMap.put(historyId, consentHistoryResource);
        }
        return consentAmendmentHistoryDataMap;
    }


    /**
     * Method to parse the changed attribute JSON string to a JSON Object.
     *
     * @param changedAttributes Changed attribute JSON string
     * @return JSON object with the changed attributes
     * @throws ConsentMgtException If there is an error while parsing the JSON String
     */
    static JSONObject parseChangedAttributeJsonString(String changedAttributes)
            throws
            ConsentMgtException {

        Object changedValues;
        try {
            changedValues = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(changedAttributes);
        } catch (ParseException e) {
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    "Changed Values is not a valid JSON object", e);
        }
        if (changedValues == null) {
            return new JSONObject();
        }
        return (JSONObject) changedValues;

    }

    /**
     * Method to process the additional consent amendment data.
     *
     * @param connection                Database connection
     * @param consentCoreDAO            Consent core DAO
     * @param newAuthorizationResources new Authorization resources
     * @throws ConsentMgtException           If an error occurs when processing the additional amendment data
     * @throws ConsentDataInsertionException If an error occurs when inserting data
     */
    public static void processAdditionalConsentAmendmentData(Connection connection, ConsentCoreDAO consentCoreDAO,
                                                             ArrayList<AuthorizationResource>
                                                                     newAuthorizationResources,
                                                             List<ConsentMappingResource>
                                                                     updatedConsentMappingResources)
            throws
            ConsentMgtException,
            ConsentDataInsertionException {
        for (AuthorizationResource authResource : newAuthorizationResources) {


            if (StringUtils.isBlank(authResource.getConsentID()) ||
                    StringUtils.isBlank(authResource.getAuthorizationType()) ||
                    StringUtils.isBlank(authResource.getAuthorizationStatus())) {
                log.error("Consent ID, authorization type or authorization status is missing, cannot proceed");
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        "Cannot proceed since consent ID, authorization type or " +
                                "authorization status is missing");
            }
            // create authorization resource
            AuthorizationResource authorizationResource =
                    consentCoreDAO.storeAuthorizationResource(connection, authResource);


        }
    }

    public static boolean validateOrgInfo(String headerOrg, String orgId) throws
            ConsentMgtException {

        if (headerOrg == null) {
            headerOrg = ConsentMgtDAOConstants.DEFAULT_ORG;
        }
        return headerOrg.equals(orgId);
    }
}
