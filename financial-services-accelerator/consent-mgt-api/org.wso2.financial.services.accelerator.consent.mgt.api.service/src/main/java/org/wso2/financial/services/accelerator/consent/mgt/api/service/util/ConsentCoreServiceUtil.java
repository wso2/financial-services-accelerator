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

package org.wso2.financial.services.accelerator.consent.mgt.api.service.util;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.ConsentCoreDAO;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.constants.ConsentError;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.exceptions.ConsentDataInsertionException;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.exceptions.ConsentDataRetrievalException;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.exceptions.ConsentMgtException;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.models.ConsentAttributes;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.models.ConsentHistoryResource;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.models.ConsentMappingResource;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.models.ConsentStatusAuditRecord;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.util.DatabaseUtils;
import org.wso2.financial.services.accelerator.consent.mgt.api.service.constants.ConsentCoreServiceConstants;
import org.wso2.financial.services.accelerator.consent.mgt.api.service.impl.ConsentCoreServiceImpl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Consent Core Service Util.
 */
public class ConsentCoreServiceUtil {

    private static final Log log = LogFactory.getLog(ConsentCoreServiceUtil.class);

    private ConsentCoreServiceUtil() {

    }
    /**
     * Create an authorizable consent with audit record.
     *
     * @param consentCoreDAO         Consent core DAO
     * @param detailedConsentResource        Consent resource
     * @return DetailedConsentResource
     * @throws ConsentDataInsertionException Consent data insertion exception
     * @throws ConsentMgtException           Consent management exception
     */
    public static DetailedConsentResource createConsentWithAuditRecord(ConsentCoreDAO consentCoreDAO,
                                                                       DetailedConsentResource detailedConsentResource)
            throws ConsentDataInsertionException, ConsentMgtException {

        boolean isConsentAttributesStored = false;
        AuthorizationResource storedAuthorizationResource;

        try (Connection connection = DatabaseUtils.getDBConnection()) {

            // Create consent
            if (log.isDebugEnabled()) {
                log.debug(("Creating the consent for ID:" + detailedConsentResource.getConsentId())
                        .replaceAll("[\r\n]", ""));
            }

            if (System.currentTimeMillis() / 1000 > detailedConsentResource.getExpiryTime()) {
                throw new ConsentMgtException(ConsentError.INVALID_CONSENT_EXPIRY_TIME);
            }

            ConsentResource storedConsentResource = consentCoreDAO.storeConsentResource(connection,
                    detailedConsentResource);
            String consentId = storedConsentResource.getConsentId();

            // Store consent attributes if available
            if (MapUtils.isNotEmpty(detailedConsentResource.getConsentAttributes())) {
                ConsentAttributes consentAttributes = new ConsentAttributes(consentId,
                        detailedConsentResource.getConsentAttributes());

                if (log.isDebugEnabled()) {
                    log.debug(String.format("Storing consent attributes for the consent of ID: %s", consentAttributes
                            .getConsentId().replaceAll("[\r\n]", "")));
                }
                isConsentAttributesStored = consentCoreDAO.storeConsentAttributes(connection,
                        consentAttributes);
                if (isConsentAttributesStored) {
                    detailedConsentResource.setConsentAttributes(detailedConsentResource.getConsentAttributes());
                }
            }

            // Store authorization resource if available
            List<AuthorizationResource> authorizationResources =
                    detailedConsentResource.getAuthorizationResources();

            if (authorizationResources != null && !authorizationResources.isEmpty()) {

                if (log.isDebugEnabled()) {
                    log.debug(("Storing authorization resource for consent of ID: " +
                            detailedConsentResource.getConsentId()
                    ).replaceAll("[\r\n]", ""));
                }
                List<AuthorizationResource> storedAuthorizationResources = consentCoreDAO.
                        storeBulkAuthorizationResources(connection, consentId, authorizationResources);
                detailedConsentResource.setAuthorizationResources(
                        storedAuthorizationResources);

           }
            // TODO : handle history
            DatabaseUtils.commitTransaction(connection);
            return detailedConsentResource;
        } catch (SQLException e) {
            log.error(ConsentError.DETAILED_CONSENT_RESOURCE_INSERTION_ERROR.getMessage().replaceAll("[\r\n]",
                    ""), e);
            throw new ConsentMgtException(ConsentError.DETAILED_CONSENT_RESOURCE_INSERTION_ERROR);

        }
    }

    /**
     * Method to create an audit record in post consent state change.
     *
     * @param connection            Database connection
     * @param consentCoreDAO        Consent core DAO
     * @param consentId             Consent ID
     * @param userId                User ID
     * @param newConsentStatus      New consent status
     * @param previousConsentStatus Previous consent status
     * @param reason                Reason for the status change
     * @param clientId              Client ID
     * @param consentDataMap        Consent data map
     * @throws ConsentDataInsertionException If an error occurs when storing the audit record
     * @throws ConsentMgtException           Consent management exception
     */
    public static void postStateChange(Connection connection, ConsentCoreDAO consentCoreDAO, String consentId,
                                       String userId, String newConsentStatus, String previousConsentStatus,
                                       String reason, String clientId, Map<String, Object> consentDataMap)
            throws ConsentDataInsertionException, ConsentMgtException, ConsentDataRetrievalException {

        ConsentStatusAuditRecord consentStatusAuditRecord = createAuditRecord(connection, consentCoreDAO, consentId,
                userId,
                newConsentStatus,
                previousConsentStatus,
                reason);

        DetailedConsentResource detailedCurrentConsent = (DetailedConsentResource)
                consentDataMap.get(ConsentCoreServiceConstants.DETAILED_CONSENT_RESOURCE);
        DetailedConsentResource detailedHistoryConsent = (DetailedConsentResource)
                consentDataMap.get(ConsentCoreServiceConstants.CONSENT_AMENDMENT_HISTORY_RESOURCE);

        if (detailedCurrentConsent == null) {
            // get the current consent details
            detailedCurrentConsent = consentCoreDAO.getDetailedConsentResource(connection, consentId);
        }

        ConsentHistoryResource consentHistoryResource = new ConsentHistoryResource();
        consentHistoryResource.setDetailedConsentResource(detailedHistoryConsent);
        consentHistoryResource.setReason(reason);
        consentHistoryResource.setTimestamp(System.currentTimeMillis());

        ConsentCoreServiceImpl consentCoreService = ConsentCoreServiceImpl.getInstance();
        boolean result = consentCoreService.storeConsentAmendmentHistory(
                consentStatusAuditRecord.getStatusAuditID(),
                consentHistoryResource,
                detailedCurrentConsent);

        if (result) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Consent Amendment History of consentId: %s persisted successfully.",
                        consentId).replaceAll("[\r\n]", ""));
            }
        } else {
            log.error(String.format("Failed to persist Consent Amendment History of consentId : %s. ",
                    consentId).replaceAll("[\r\n]", ""));
        }

    }

    /**
     * Create an audit record for the consent status change.
     *
     * @param connection            database connection
     * @param consentCoreDAO        consent core DAO
     * @param consentId             consent ID
     * @param userId                user ID
     * @param newConsentStatus      new consent status
     * @param previousConsentStatus previous consent status
     * @param reason                reason for the status change
     * @return
     * @throws ConsentDataInsertionException thrown if an error occurs when storing the audit record
     */
    public static ConsentStatusAuditRecord createAuditRecord(Connection connection, ConsentCoreDAO consentCoreDAO,
                                                             String consentId,
                                                             String userId, String newConsentStatus,
                                                             String previousConsentStatus,
                                                             String reason) throws ConsentDataInsertionException {

        // Create an audit record
        String actionBy = StringUtils.isNotEmpty(userId) ? userId : null;
        ConsentStatusAuditRecord consentStatusAuditRecord = new ConsentStatusAuditRecord(consentId, newConsentStatus,
                0, reason, actionBy, previousConsentStatus);

        consentStatusAuditRecord.setStatusAuditID(String.valueOf(UUID.randomUUID()));
        if (log.isDebugEnabled()) {
            log.debug(("Storing audit record for consent of ID: " +
                    consentStatusAuditRecord.getConsentId()).replaceAll("[\r\n]", ""));
        }
        return consentCoreDAO.storeConsentStatusAuditRecord(connection, consentStatusAuditRecord);
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
    public static JSONObject getChangedConsentAttributesDataJSON(Map<String, Object> newConsentAttributes,
                                                                 Map<String, Object> oldConsentAttributes) {

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
        for (Map.Entry<String, Object> newConsentAttribute : newConsentAttributes.entrySet()) {
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
                if (newMapping.getMappingId().equals(oldMapping.getMappingId())) {
                    existingConsentMappingIds.add(newMapping.getMappingId());
                    if (!newMapping.getMappingStatus().equals(oldMapping.getMappingStatus())) {
                        //store only the mapping-ids with a changed Mapping Status to the consent amendment history
                        changedConsentMappingJson.put(ConsentCoreServiceConstants.MAPPING_STATUS,
                                oldMapping.getMappingStatus());
                    }
                    break;
                }
            }
            if (!changedConsentMappingJson.isEmpty()) {
                changedConsentMappingsJsonDataMap.put(newMapping.getMappingId(), changedConsentMappingJson);
            }
            // store any new mapping-ids in current consent to the immediate past consent amendment history with
            // 'null' value
            if (!existingConsentMappingIds.contains(newMapping.getMappingId())) {
                changedConsentMappingsJsonDataMap.put(newMapping.getMappingId(), null);
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
    public static Map<String, JSONObject> getChangedConsentAuthResourcesDataJSONMap(List<AuthorizationResource>
                                                                                            newConsentAuthResources,
                                                                                    ArrayList<AuthorizationResource>
                                                                                            oldConsentAuthResources) {

        Map<String, JSONObject> changedConsentAuthResourcesJsonDataMap = new HashMap<>();

        ArrayList<String> existingConsentAuthResourceIds = new ArrayList<>();
        for (AuthorizationResource newAuthResource : newConsentAuthResources) {
            oldConsentAuthResources.stream()
                    .filter(oldAuthResource -> newAuthResource.getAuthorizationId().
                            equals(oldAuthResource.getAuthorizationId()))
                    .forEach(oldAuthResource ->
                            existingConsentAuthResourceIds.add(newAuthResource.getAuthorizationId()));

            // store any new authorization-ids in current consent (an Auth Resource not available in previous consent,
            // but newly added in current consent) to the immediate past consent amendment history with 'null' value
            if (!existingConsentAuthResourceIds.contains(newAuthResource.getAuthorizationId())) {
                changedConsentAuthResourcesJsonDataMap.put(newAuthResource.getAuthorizationId(), null);
            }

            // compare old auth resrouce status with new auth resource status
            JSONObject changedConsentAuthResourceJson = new JSONObject();
            for (AuthorizationResource oldAuthResource : oldConsentAuthResources) {
                if (newAuthResource.getAuthorizationId().equals(oldAuthResource.getAuthorizationId())) {
                    if (!newAuthResource.getAuthorizationStatus().equals(
                            oldAuthResource.getAuthorizationStatus())) {
                        //store only the auth resources with a changed status to the consent amendment history
                        changedConsentAuthResourceJson.put(ConsentCoreServiceConstants.AUTHORIZATION_STATUS,
                                oldAuthResource.getAuthorizationStatus());
                        changedConsentAuthResourcesJsonDataMap.put(newAuthResource.getAuthorizationId(),
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
        recordIdsList.add(detailedConsentResource.getConsentId());

        for (AuthorizationResource authResource : detailedConsentResource.getAuthorizationResources()) {
            recordIdsList.add(authResource.getAuthorizationId());
        }
        return recordIdsList;
    }


    /**
     * Method to parse the changed attribute JSON string to a JSON Object.
     *
     * @param changedAttributes Changed attribute JSON string
     * @return JSON object with the changed attributes
     * @throws ConsentMgtException If there is an error while parsing the JSON String
     */
    static JSONObject parseChangedAttributeJsonString(String changedAttributes)
            throws ConsentMgtException {

        Object changedValues;
        try {
            changedValues = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(changedAttributes);
        } catch (ParseException e) {
            throw new ConsentMgtException(ConsentError.JSON_PROCESSING_ERROR);
        }
        if (changedValues == null) {
            return new JSONObject();
        }
        return (JSONObject) changedValues;

    }



}
