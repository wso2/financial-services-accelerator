/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.wso2.openbanking.accelerator.consent.mgt.dao.impl;

import com.wso2.openbanking.accelerator.consent.mgt.dao.ConsentCoreDAO;
import com.wso2.openbanking.accelerator.consent.mgt.dao.constants.ConsentMgtDAOConstants;
import com.wso2.openbanking.accelerator.consent.mgt.dao.exceptions.OBConsentDataDeletionException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.exceptions.OBConsentDataInsertionException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.exceptions.OBConsentDataRetrievalException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.exceptions.OBConsentDataUpdationException;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentAttributes;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentFile;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentHistoryResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentStatusAuditRecord;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.queries.ConsentMgtCommonDBQueries;
import com.wso2.openbanking.accelerator.consent.mgt.dao.utils.ConsentDAOUtils;
import net.minidev.json.JSONValue;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.wso2.openbanking.accelerator.consent.mgt.dao.constants.ConsentMgtDAOConstants.SESSION_DATA_KEY;

/**
 * This class only implements the data access methods for the consent management accelerator. It implements all the
 * methods defined in the ConsentCoreDAO interface and is only responsible for reading and writing data from/to the
 * database. The incoming data are pre-validated in the upper service layer. Therefore, no validations are done in
 * this layer.
 */
public class ConsentCoreDAOImpl implements ConsentCoreDAO {

    private static Log log = LogFactory.getLog(ConsentCoreDAOImpl.class);
    private static final String GROUP_BY_SEPARATOR = "\\|\\|";
    ConsentMgtCommonDBQueries sqlStatements;
    //Numbers are assigned to each consent DB table & used as the reference for each table when storing CA history
    static final Map<String, String> TABLES_MAP = new HashMap<String, String>() {
        {
            put(ConsentMgtDAOConstants.TABLE_OB_CONSENT, "01");
            put(ConsentMgtDAOConstants.TABLE_OB_CONSENT_AUTH_RESOURCE, "02");
            put(ConsentMgtDAOConstants.TABLE_OB_CONSENT_MAPPING, "03");
            put(ConsentMgtDAOConstants.TABLE_OB_CONSENT_ATTRIBUTE, "04");
            put(ConsentMgtDAOConstants.TABLE_OB_CONSENT_FILE, "05");
        }
    };
    static final Map<String, String> COLUMNS_MAP = new HashMap<String, String>() {
        {
            put(ConsentMgtDAOConstants.CONSENT_IDS, "CONSENT_ID");
            put(ConsentMgtDAOConstants.CLIENT_IDS, "CLIENT_ID");
            put(ConsentMgtDAOConstants.CONSENT_TYPES, "CONSENT_TYPE");
            put(ConsentMgtDAOConstants.CONSENT_STATUSES, "CURRENT_STATUS");
            put(ConsentMgtDAOConstants.USER_IDS, "OCAR.USER_ID");
        }
    };

    public ConsentCoreDAOImpl(ConsentMgtCommonDBQueries sqlStatements) {

        this.sqlStatements = sqlStatements;
    }

    @Override
    public ConsentResource storeConsentResource(Connection connection, ConsentResource consentResource)
            throws OBConsentDataInsertionException {

        int result;
        String consentID = "";
        if (StringUtils.isEmpty(consentResource.getConsentID())) {
            consentID = UUID.randomUUID().toString();
        } else {
            consentID = consentResource.getConsentID();
        }
        // Unix time in seconds
        long createdTime;
        if (consentResource.getCreatedTime() == 0) {
            createdTime = System.currentTimeMillis() / 1000;
        } else {
            createdTime = consentResource.getCreatedTime();
        }
        long updatedTime;
        if (consentResource.getUpdatedTime() == 0) {
            updatedTime = System.currentTimeMillis() / 1000;
        } else {
            updatedTime = consentResource.getUpdatedTime();
        }
        String storeConsentPrepStatement = sqlStatements.getStoreConsentPreparedStatement();

        try (PreparedStatement storeConsentPreparedStmt = connection.prepareStatement(storeConsentPrepStatement)) {

            log.debug("Setting parameters to prepared statement to store consent resource");

            storeConsentPreparedStmt.setString(1, consentID);
            storeConsentPreparedStmt.setString(2, consentResource.getReceipt());
            storeConsentPreparedStmt.setLong(3, createdTime);
            storeConsentPreparedStmt.setLong(4, updatedTime);
            storeConsentPreparedStmt.setString(5, consentResource.getClientID());
            storeConsentPreparedStmt.setString(6, consentResource.getConsentType());
            storeConsentPreparedStmt.setString(7, consentResource.getCurrentStatus());
            storeConsentPreparedStmt.setLong(8, consentResource.getConsentFrequency());
            storeConsentPreparedStmt.setLong(9, consentResource.getValidityPeriod());
            storeConsentPreparedStmt.setBoolean(10, consentResource.isRecurringIndicator());

            // with result, we can determine whether the insertion was successful or not
            result = storeConsentPreparedStmt.executeUpdate();
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_RESOURCE_STORE_ERROR_MSG, e);
            throw new OBConsentDataInsertionException(ConsentMgtDAOConstants.CONSENT_RESOURCE_STORE_ERROR_MSG, e);
        }

        // Confirm that the data are inserted successfully
        if (result > 0) {
            log.debug("Stored the consent resource successfully");
            consentResource.setConsentID(consentID);
            consentResource.setCreatedTime(createdTime);
            consentResource.setUpdatedTime(createdTime);
            return consentResource;
        } else {
            throw new OBConsentDataInsertionException("Failed to store consent data properly.");
        }
    }

    @Override
    public AuthorizationResource storeAuthorizationResource(Connection connection,
                                                            AuthorizationResource authorizationResource)
            throws OBConsentDataInsertionException {

        int result;
        if (authorizationResource == null) {
            throw new OBConsentDataInsertionException("Failed to store authorization resource due to null value.");
        }
        String authorizationID = UUID.randomUUID().toString();
        if (!StringUtils.isEmpty(authorizationResource.getAuthorizationID())) {
            authorizationID = authorizationResource.getAuthorizationID();
        }
        // Unix time in seconds
        long updatedTime = System.currentTimeMillis() / 1000;
        if (authorizationResource.getUpdatedTime() != 0) {
            updatedTime = authorizationResource.getUpdatedTime();
        }
        String storeAuthorizationPrepStatement = sqlStatements.getStoreAuthorizationPreparedStatement();

        try (PreparedStatement storeAuthorizationPreparedStmt =
                     connection.prepareStatement(storeAuthorizationPrepStatement)) {

            log.debug("Setting parameters to prepared statement to store authorization resource");

            storeAuthorizationPreparedStmt.setString(1, authorizationID);
            storeAuthorizationPreparedStmt.setString(2, authorizationResource.getConsentID());
            storeAuthorizationPreparedStmt.setString(3, authorizationResource.getAuthorizationType());
            storeAuthorizationPreparedStmt.setString(4, authorizationResource.getUserID());
            storeAuthorizationPreparedStmt.setString(5, authorizationResource.getAuthorizationStatus());
            storeAuthorizationPreparedStmt.setLong(6, updatedTime);

            // with result, we can determine whether the insertion was successful or not
            result = storeAuthorizationPreparedStmt.executeUpdate();
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.AUTHORIZATION_RESOURCE_STORE_ERROR_MSG, e);
            throw new OBConsentDataInsertionException(ConsentMgtDAOConstants.AUTHORIZATION_RESOURCE_STORE_ERROR_MSG, e);
        }

        // Confirm that the data are inserted successfully
        if (result > 0) {
            log.debug("Stored the authorization resource successfully");
            authorizationResource.setAuthorizationID(authorizationID);
            authorizationResource.setUpdatedTime(updatedTime);
            return authorizationResource;
        } else {
            throw new OBConsentDataInsertionException("Failed to store authorization resource data properly.");
        }
    }

    @Override
    public ConsentMappingResource storeConsentMappingResource(Connection connection,
                                                              ConsentMappingResource consentMappingResource)
            throws OBConsentDataInsertionException {

        int result;
        if (consentMappingResource == null) {
            throw new OBConsentDataInsertionException("Failed to store consent mapping resource due to null value.");
        }
        String consentMappingID = UUID.randomUUID().toString();
        if (!StringUtils.isEmpty(consentMappingResource.getMappingID())) {
            consentMappingID = consentMappingResource.getMappingID();
        }
        String storeConsentMappingPrepStatement = sqlStatements.getStoreConsentMappingPreparedStatement();

        try (PreparedStatement storeConsentMappingPreparedStmt =
                     connection.prepareStatement(storeConsentMappingPrepStatement)) {

            log.debug("Setting parameters to prepared statement to store consent mapping resource");

            storeConsentMappingPreparedStmt.setString(1, consentMappingID);
            storeConsentMappingPreparedStmt.setString(2, consentMappingResource.getAuthorizationID());
            storeConsentMappingPreparedStmt.setString(3, consentMappingResource.getAccountID());
            storeConsentMappingPreparedStmt.setString(4, consentMappingResource.getPermission());
            storeConsentMappingPreparedStmt.setString(5, consentMappingResource.getMappingStatus());

            // with result, we can determine whether the insertion was successful or not
            result = storeConsentMappingPreparedStmt.executeUpdate();
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_MAPPING_RESOURCE_STORE_ERROR_MSG, e);
            throw new OBConsentDataInsertionException(ConsentMgtDAOConstants.CONSENT_MAPPING_RESOURCE_STORE_ERROR_MSG,
                    e);
        }

        // Confirm that the data are inserted successfully
        if (result > 0) {
            log.debug("Stored the consent mapping resource successfully");
            consentMappingResource.setMappingID(consentMappingID);
            return consentMappingResource;
        } else {
            throw new OBConsentDataInsertionException("Failed to store consent mapping resource data properly.");
        }
    }

    @Override
    public ConsentStatusAuditRecord storeConsentStatusAuditRecord(Connection connection,
                                                                  ConsentStatusAuditRecord consentStatusAuditRecord)
            throws OBConsentDataInsertionException {

        int result;
        if (consentStatusAuditRecord == null) {
            throw new OBConsentDataInsertionException("Failed to store consent audit record due to null value.");
        }
        String statusAuditID = UUID.randomUUID().toString();
        if (!StringUtils.isEmpty(consentStatusAuditRecord.getStatusAuditID())) {
            statusAuditID = consentStatusAuditRecord.getStatusAuditID();
        }
        // Unix time in seconds
        long actionTime = System.currentTimeMillis() / 1000;
        if (consentStatusAuditRecord.getActionTime() != 0) {
            actionTime = consentStatusAuditRecord.getActionTime();
        }
        String storeConsentStatusAuditRecordPrepStatement =
                sqlStatements.getStoreConsentStatusAuditRecordPreparedStatement();

        try (PreparedStatement storeConsentStatusAuditRecordPreparedStmt =
                     connection.prepareStatement(storeConsentStatusAuditRecordPrepStatement)) {

            log.debug("Setting parameters to prepared statement to store consent audit record");

            storeConsentStatusAuditRecordPreparedStmt.setString(1, statusAuditID);
            storeConsentStatusAuditRecordPreparedStmt.setString(2, consentStatusAuditRecord
                    .getConsentID());
            storeConsentStatusAuditRecordPreparedStmt.setString(3, consentStatusAuditRecord
                    .getCurrentStatus());
            storeConsentStatusAuditRecordPreparedStmt.setLong(4, actionTime);
            storeConsentStatusAuditRecordPreparedStmt.setString(5, consentStatusAuditRecord.getReason());
            storeConsentStatusAuditRecordPreparedStmt.setString(6, consentStatusAuditRecord
                    .getActionBy());
            storeConsentStatusAuditRecordPreparedStmt.setString(7, consentStatusAuditRecord
                    .getPreviousStatus());

            // with result, we can determine whether the insertion was successful or not
            result = storeConsentStatusAuditRecordPreparedStmt.executeUpdate();
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.AUDIT_RECORD_STORE_ERROR_MSG, e);
            throw new OBConsentDataInsertionException(ConsentMgtDAOConstants.AUDIT_RECORD_STORE_ERROR_MSG, e);
        }

        // Confirm that the data are inserted successfully
        if (result > 0) {
            log.debug("Stored the consent status audit record successfully");
            consentStatusAuditRecord.setStatusAuditID(statusAuditID);
            consentStatusAuditRecord.setActionTime(actionTime);
            return consentStatusAuditRecord;
        } else {
            throw new OBConsentDataInsertionException("Failed to store consent status audit record data properly.");
        }
    }

    @Override
    public boolean storeConsentAttributes(Connection connection, ConsentAttributes consentAttributes)
            throws OBConsentDataInsertionException {

        int[] result;
        String storeConsentAttributesPrepStatement = sqlStatements.getStoreConsentAttributesPreparedStatement();
        Map<String, String> consentAttributesMap = consentAttributes.getConsentAttributes();

        try (PreparedStatement storeConsentAttributesPreparedStmt =
                     connection.prepareStatement(storeConsentAttributesPrepStatement)) {

            for (Map.Entry<String, String> entry : consentAttributesMap.entrySet()) {
                storeConsentAttributesPreparedStmt.setString(1, consentAttributes.getConsentID());
                storeConsentAttributesPreparedStmt.setString(2, entry.getKey());
                storeConsentAttributesPreparedStmt.setString(3, entry.getValue());
                storeConsentAttributesPreparedStmt.addBatch();
            }

            // with result, we can determine whether the updating was successful or not
            result = storeConsentAttributesPreparedStmt.executeBatch();
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_ATTRIBUTES_STORE_ERROR_MSG, e);
            throw new OBConsentDataInsertionException(ConsentMgtDAOConstants.CONSENT_ATTRIBUTES_STORE_ERROR_MSG, e);
        }

        /*
           An empty array or an array with value -3 means the batch execution is failed.
           If an array contains value -2, it means the command completed successfully but the number of rows affected
           are unknown. Therefore, only checking for the existence of -3.
        */
        if (result.length != 0 && IntStream.of(result).noneMatch(value -> value == -3)) {
            log.debug("Stored the consent attributes successfully");
            return true;
        } else {
            throw new OBConsentDataInsertionException("Failed to store consent attribute data properly.");
        }
    }

    @Override
    public boolean storeConsentFile(Connection connection, ConsentFile consentFileResource)
            throws OBConsentDataInsertionException {

        int result;
        String storeConsentMappingPrepStatement = sqlStatements.getStoreConsentFilePreparedStatement();

        try (PreparedStatement storeConsentFilePreparedStmt =
                     connection.prepareStatement(storeConsentMappingPrepStatement)) {

            log.debug("Setting parameters to prepared statement to store consent file resource");

            storeConsentFilePreparedStmt.setString(1, consentFileResource.getConsentID());
            storeConsentFilePreparedStmt.setString(2, consentFileResource.getConsentFile());

            // with result, we can determine whether the insertion was successful or not
            result = storeConsentFilePreparedStmt.executeUpdate();
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_FILE_STORE_ERROR_MSG, e);
            throw new OBConsentDataInsertionException(ConsentMgtDAOConstants.CONSENT_FILE_STORE_ERROR_MSG, e);
        }

        // Confirm that the data are inserted successfully
        if (result > 0) {
            log.debug("Stored the consent file resource successfully");
            return true;
        } else {
            throw new OBConsentDataInsertionException("Failed to store consent file resource data properly.");
        }
    }

    @Override
    public ConsentResource updateConsentStatus(Connection connection, String consentID, String newConsentStatus)
            throws OBConsentDataUpdationException {

        int result;
        long updatedTime = System.currentTimeMillis() / 1000;
        ConsentResource consentResource = new ConsentResource();
        String updateConsentStatusPrepStatement = sqlStatements.getUpdateConsentStatusPreparedStatement();

        try (PreparedStatement updateConsentStatusPreparedStmt =
                     connection.prepareStatement(updateConsentStatusPrepStatement)) {

            log.debug("Setting parameters to prepared statement to update consent status");

            updateConsentStatusPreparedStmt.setString(1, newConsentStatus);
            updateConsentStatusPreparedStmt.setLong(2, updatedTime);
            updateConsentStatusPreparedStmt.setString(3, consentID);

            // with result, we can determine whether the updating was successful or not
            result = updateConsentStatusPreparedStmt.executeUpdate();
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_STATUS_UPDATE_ERROR_MSG, e);
            throw new OBConsentDataUpdationException(ConsentMgtDAOConstants.CONSENT_STATUS_UPDATE_ERROR_MSG, e);
        }

        // Confirm that the data are updated successfully
        if (result > 0) {
            log.debug("Updated the consent status successfully");
            consentResource.setConsentID(consentID);
            consentResource.setCurrentStatus(newConsentStatus);
            return consentResource;
        } else {
            throw new OBConsentDataUpdationException("Failed to update consent status properly.");
        }
    }

    @Override
    public boolean updateConsentMappingStatus(Connection connection, ArrayList<String> mappingIDs, String mappingStatus)
            throws OBConsentDataUpdationException {

        int[] result;
        String updateConsentMappingStatusPrepStatement = sqlStatements.getUpdateConsentMappingStatusPreparedStatement();

        try (PreparedStatement updateConsentMappingStatusPreparedStmt =
                     connection.prepareStatement(updateConsentMappingStatusPrepStatement)) {

            log.debug("Setting parameters to prepared statement to update consent mapping status");

            for (String mappingID : mappingIDs) {
                updateConsentMappingStatusPreparedStmt.setString(1, mappingStatus);
                updateConsentMappingStatusPreparedStmt.setString(2, mappingID);
                updateConsentMappingStatusPreparedStmt.addBatch();
            }

            // with result, we can determine whether the updating was successful or not
            result = updateConsentMappingStatusPreparedStmt.executeBatch();
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_MAPPING_STATUS_UPDATE_ERROR_MSG, e);
            throw new OBConsentDataUpdationException(ConsentMgtDAOConstants.CONSENT_MAPPING_STATUS_UPDATE_ERROR_MSG, e);
        }

        // An empty array or an array with value -3 means the batch execution is failed
        if (result.length != 0 && IntStream.of(result).noneMatch(value -> value == -3)) {
            log.debug("Updated the consent mapping statuses of matching records successfully");
            return true;
        } else {
            throw new OBConsentDataUpdationException("Failed to update consent mapping status properly.");
        }
    }

    @Override
    public AuthorizationResource updateAuthorizationStatus(Connection connection, String authorizationID,
                                                           String newAuthorizationStatus)
            throws OBConsentDataUpdationException {

        int result;

        // Unix time in seconds
        long updatedTime = System.currentTimeMillis() / 1000;
        String updateAuthorizationStatusPrepStatement = sqlStatements.getUpdateAuthorizationStatusPreparedStatement();

        try (PreparedStatement updateAuthorizationStatusPreparedStmt =
                     connection.prepareStatement(updateAuthorizationStatusPrepStatement)) {

            log.debug("Setting parameters to prepared statement to update authorization status");

            updateAuthorizationStatusPreparedStmt.setString(1, newAuthorizationStatus);
            updateAuthorizationStatusPreparedStmt.setLong(2, updatedTime);
            updateAuthorizationStatusPreparedStmt.setString(3, authorizationID);

            // with result, we can determine whether the updating was successful or not
            result = updateAuthorizationStatusPreparedStmt.executeUpdate();
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_AUTHORIZATION_STATUS_UPDATE_ERROR_MSG, e);
            throw new OBConsentDataUpdationException(ConsentMgtDAOConstants
                    .CONSENT_AUTHORIZATION_STATUS_UPDATE_ERROR_MSG, e);
        }

        // Confirm that the data are updated successfully
        if (result > 0) {
            log.debug("Updated the authorization status successfully");
            AuthorizationResource authorizationResource = new AuthorizationResource();

            authorizationResource.setAuthorizationStatus(newAuthorizationStatus);
            authorizationResource.setAuthorizationID(authorizationID);
            authorizationResource.setUpdatedTime(updatedTime);
            return authorizationResource;
        } else {
            throw new OBConsentDataUpdationException("Failed to update consent status properly.");
        }
    }

    @Override
    public AuthorizationResource updateAuthorizationUser(Connection connection, String authorizationID, String userID)
            throws OBConsentDataUpdationException {

        int result;

        // Unix time in seconds
        long updatedTime = System.currentTimeMillis() / 1000;
        String updateAuthorizationUserPrepStatement = sqlStatements.getUpdateAuthorizationUserPreparedStatement();

        try (PreparedStatement updateAuthorizationUserPreparedStmt =
                     connection.prepareStatement(updateAuthorizationUserPrepStatement)) {

            log.debug("Setting parameters to prepared statement to update authorization user");

            updateAuthorizationUserPreparedStmt.setString(1, userID);
            updateAuthorizationUserPreparedStmt.setLong(2, updatedTime);
            updateAuthorizationUserPreparedStmt.setString(3, authorizationID);

            // with result, we can determine whether the updating was successful or not
            result = updateAuthorizationUserPreparedStmt.executeUpdate();
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_AUTHORIZATION_USER_UPDATE_ERROR_MSG, e);
            throw new OBConsentDataUpdationException(ConsentMgtDAOConstants.CONSENT_AUTHORIZATION_USER_UPDATE_ERROR_MSG,
                    e);
        }

        // Confirm that the data are updated successfully
        if (result > 0) {
            log.debug("Updated the authorization user successfully");
            AuthorizationResource authorizationResource = new AuthorizationResource();

            authorizationResource.setUserID(userID);
            authorizationResource.setAuthorizationID(authorizationID);
            authorizationResource.setUpdatedTime(updatedTime);
            return authorizationResource;
        } else {
            throw new OBConsentDataUpdationException("Failed to update authorization user properly.");
        }
    }

    @Override
    public ConsentFile getConsentFile(Connection connection, String consentID, boolean fetchFromRetentionTables)
            throws OBConsentDataRetrievalException {

        ConsentFile receivedConsentFileResource = new ConsentFile();
        String getConsentFilePrepStatement = sqlStatements.getGetConsentFileResourcePreparedStatement(
                fetchFromRetentionTables);

        try (PreparedStatement getConsentFileResourcePreparedStmt =
                     connection.prepareStatement(getConsentFilePrepStatement)) {

            log.debug("Setting parameters to prepared statement to retrieve consent file resource");

            getConsentFileResourcePreparedStmt.setString(1, consentID);

            try (ResultSet resultSet = getConsentFileResourcePreparedStmt.executeQuery()) {
                if (resultSet.next()) {
                    String storedConsentID = resultSet.getString(ConsentMgtDAOConstants.CONSENT_ID);
                    String consentFile = resultSet.getString(ConsentMgtDAOConstants.CONSENT_FILE);

                    receivedConsentFileResource.setConsentID(storedConsentID);
                    receivedConsentFileResource.setConsentFile(consentFile);
                } else {
                    log.error("No records are found for consent ID :" + consentID);
                    throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG);
                }
            } catch (SQLException e) {
                log.error("Error occurred while reading consent file resource");
                throw new OBConsentDataRetrievalException(String.format("Error occurred while retrieving consent file" +
                        " resource for consent ID : %s", consentID), e);
            }

            if (log.isDebugEnabled()) {
                log.debug("Retrieved the consent file resource for consent ID : " + consentID);
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_FILE_RETRIEVE_ERROR_MSG, e);
            throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.CONSENT_FILE_RETRIEVE_ERROR_MSG, e);
        }
        return receivedConsentFileResource;
    }

    @Override
    public ConsentAttributes getConsentAttributes(Connection connection, String consentID,
                                                  ArrayList<String> consentAttributeKeys)
            throws OBConsentDataRetrievalException {

        Map<String, String> retrievedConsentAttributesMap = new HashMap<>();
        ConsentAttributes retrievedConsentAttributesResource;
        String getConsentAttributesPrepStatement = sqlStatements.getGetConsentAttributesPreparedStatement();

        try (PreparedStatement getConsentAttributesPreparedStmt =
                     connection.prepareStatement(getConsentAttributesPrepStatement)) {

            log.debug("Setting parameters to prepared statement to retrieve consent attributes");

            getConsentAttributesPreparedStmt.setString(1, consentID);

            try (ResultSet resultSet = getConsentAttributesPreparedStmt.executeQuery()) {
                if (resultSet.isBeforeFirst()) {
                    while (resultSet.next()) {
                        String attributeKey = resultSet.getString(ConsentMgtDAOConstants.ATT_KEY);
                        String attributeValue = resultSet.getString(ConsentMgtDAOConstants.ATT_VALUE);

                        // Filter the needed attributes
                        if (consentAttributeKeys.contains(attributeKey)) {
                            retrievedConsentAttributesMap.put(attributeKey, attributeValue);
                            if (retrievedConsentAttributesMap.size() == consentAttributeKeys.size()) {
                                break;
                            }
                        }
                    }
                    retrievedConsentAttributesResource = new ConsentAttributes();
                    retrievedConsentAttributesResource.setConsentID(consentID);
                    retrievedConsentAttributesResource.setConsentAttributes(retrievedConsentAttributesMap);
                } else {
                    log.error("No records are found for consent ID : " + consentID + " and consent attribute keys");
                    throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG);
                }
            } catch (SQLException e) {
                log.error("Error occurred while reading consent attributes", e);
                throw new OBConsentDataRetrievalException(String.format("Error occurred while retrieving consent " +
                        "attributes for consent ID : %s and provided consent attributes", consentID), e);
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_ATTRIBUTES_RETRIEVE_ERROR_MSG, e);
            throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.CONSENT_ATTRIBUTES_RETRIEVE_ERROR_MSG, e);
        }
        return retrievedConsentAttributesResource;
    }

    @Override
    public ConsentAttributes getConsentAttributes(Connection connection, String consentID)
            throws OBConsentDataRetrievalException {

        Map<String, String> retrievedConsentAttributesMap = new HashMap<>();
        ConsentAttributes retrievedConsentAttributesResource;
        String getConsentAttributesPrepStatement = sqlStatements.getGetConsentAttributesPreparedStatement();

        try (PreparedStatement getConsentAttributesPreparedStmt =
                     connection.prepareStatement(getConsentAttributesPrepStatement)) {

            log.debug("Setting parameters to prepared statement to retrieve consent attributes");

            getConsentAttributesPreparedStmt.setString(1, consentID);

            try (ResultSet resultSet = getConsentAttributesPreparedStmt.executeQuery()) {
                if (resultSet.isBeforeFirst()) {
                    while (resultSet.next()) {
                        retrievedConsentAttributesMap.put(resultSet.getString(ConsentMgtDAOConstants.ATT_KEY),
                                resultSet.getString(ConsentMgtDAOConstants.ATT_VALUE));
                    }
                    retrievedConsentAttributesResource = new ConsentAttributes();
                    retrievedConsentAttributesResource.setConsentID(consentID);
                    retrievedConsentAttributesResource.setConsentAttributes(retrievedConsentAttributesMap);
                } else {
                    log.error("No records are found for consent ID :" + consentID);
                    throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG);
                }
            } catch (SQLException e) {
                log.error("Error occurred while reading consent attributes", e);
                throw new OBConsentDataRetrievalException(String.format("Error occurred while retrieving consent " +
                        "attributes for consent ID : %s", consentID), e);
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_ATTRIBUTES_RETRIEVE_ERROR_MSG, e);
            throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.CONSENT_ATTRIBUTES_RETRIEVE_ERROR_MSG, e);
        }
        return retrievedConsentAttributesResource;
    }

    @Override
    public Map<String, String> getConsentAttributesByName(Connection connection, String attributeName)
            throws OBConsentDataRetrievalException {

        Map<String, String> retrievedConsentAttributesMap = new HashMap<>();
        String getConsentAttributesByNamePrepStatement = sqlStatements.getGetConsentAttributesByNamePreparedStatement();

        try (PreparedStatement getConsentAttributesByNamePreparedStmt =
                     connection.prepareStatement(getConsentAttributesByNamePrepStatement)) {

            if (log.isDebugEnabled()) {
                log.debug("Setting parameters to prepared statement to retrieve consent attributes for the provided " +
                        "key: " + attributeName);
            }
            getConsentAttributesByNamePreparedStmt.setString(1, attributeName);

            try (ResultSet resultSet = getConsentAttributesByNamePreparedStmt.executeQuery()) {
                if (resultSet.isBeforeFirst()) {
                    while (resultSet.next()) {
                        retrievedConsentAttributesMap.put(resultSet.getString(ConsentMgtDAOConstants.CONSENT_ID),
                                resultSet.getString(ConsentMgtDAOConstants.ATT_VALUE));
                    }
                }
            } catch (SQLException e) {
                log.error("Error occurred while reading consent attributes for the given key: "
                        + attributeName, e);
                throw new OBConsentDataRetrievalException(String.format("Error occurred while retrieving consent " +
                        "attributes for attribute key: %s", attributeName), e);
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_ATTRIBUTES_RETRIEVE_ERROR_MSG, e);
            throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.CONSENT_ATTRIBUTES_RETRIEVE_ERROR_MSG, e);
        }
        return retrievedConsentAttributesMap;
    }

    @Override
    public ArrayList<String> getConsentIdByConsentAttributeNameAndValue(Connection connection, String attributeName,
                                                                        String attributeValue)
            throws OBConsentDataRetrievalException {

        ArrayList<String> retrievedConsentIdList = new ArrayList<>();
        String getConsentIdByConsentAttributeNameAndValuePrepStatement = sqlStatements
                .getConsentIdByConsentAttributeNameAndValuePreparedStatement();

        try (PreparedStatement getConsentAttributesByNamePreparedStmt =
                     connection.prepareStatement(getConsentIdByConsentAttributeNameAndValuePrepStatement)) {

            if (log.isDebugEnabled()) {
                log.debug("Setting parameters to prepared statement to retrieve consent id for the provided " +
                        "key: " + attributeName + " and value: " + attributeValue);
            }
            getConsentAttributesByNamePreparedStmt.setString(1, attributeName);
            getConsentAttributesByNamePreparedStmt.setString(2, attributeValue);

            try (ResultSet resultSet = getConsentAttributesByNamePreparedStmt.executeQuery()) {
                if (resultSet.isBeforeFirst()) {
                    while (resultSet.next()) {
                        retrievedConsentIdList.add(resultSet.getString(ConsentMgtDAOConstants.CONSENT_ID));
                    }
                } else {
                    log.error("No records are found for the provided attribute key  :" + attributeName +
                            " and value: " + attributeValue);
                    throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG);
                }
            } catch (SQLException e) {
                log.error("Error occurred while reading consent attributes for the given key: " + attributeName +
                        " and value: " + attributeValue);
                throw new OBConsentDataRetrievalException(String.format("Error occurred while retrieving consent " +
                        "attributes for attribute key: %s  and value: %s", attributeName, attributeValue), e);
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_ID_RETRIEVE_ERROR_MSG);
            throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.CONSENT_ID_RETRIEVE_ERROR_MSG, e);
        }
        return retrievedConsentIdList;
    }

    @Override
    public ConsentResource getConsentResource(Connection connection, String consentID)
            throws OBConsentDataRetrievalException {

        ConsentResource retrievedConsentResource = new ConsentResource();

        String getConsentResourcePrepStatement = sqlStatements.getGetConsentPreparedStatement();

        try (PreparedStatement getConsentResourcePreparedStmt =
                     connection.prepareStatement(getConsentResourcePrepStatement)) {

            log.debug("Setting parameters to prepared statement to retrieve consent resource");

            getConsentResourcePreparedStmt.setString(1, consentID);

            try (ResultSet resultSet = getConsentResourcePreparedStmt.executeQuery()) {
                if (resultSet.next()) {
                    setDataToConsentResource(resultSet, retrievedConsentResource);
                } else {
                    log.error("No records are found for consent ID :" + consentID);
                    throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG);
                }
            } catch (SQLException e) {
                log.error("Error occurred while reading consent resource", e);
                throw new OBConsentDataRetrievalException(String.format("Error occurred while retrieving consent " +
                        "resource for consent ID : %s", consentID), e);
            }

            if (log.isDebugEnabled()) {
                log.debug("Retrieved the consent resource from OB_CONSENT table for consent ID : " + consentID);
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_RESOURCE_RETRIEVE_ERROR_MSG, e);
            throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.CONSENT_RESOURCE_RETRIEVE_ERROR_MSG, e);
        }
        return retrievedConsentResource;
    }

    @Override
    public DetailedConsentResource getDetailedConsentResource(Connection connection, String consentID,
                                                              boolean fetchFromRetentionTables)
            throws OBConsentDataRetrievalException {

        DetailedConsentResource retrievedDetailedConsentResource = new DetailedConsentResource();

        String getDetailedConsentResourcePrepStatement = sqlStatements.getGetDetailedConsentPreparedStatement(
                fetchFromRetentionTables);

        try (PreparedStatement getDetailedConsentResourcePreparedStmt = connection
                .prepareStatement(getDetailedConsentResourcePrepStatement)) {

            log.debug("Setting parameters to prepared statement to retrieve detailed consent resource");

            getDetailedConsentResourcePreparedStmt.setString(1, consentID);

            try (ResultSet resultSet = getDetailedConsentResourcePreparedStmt.executeQuery()) {
                if (resultSet.isBeforeFirst()) {
                    setDataToDetailedConsentResource(resultSet, retrievedDetailedConsentResource);
                } else {
                    log.error("No records are found for consent ID :" + consentID);
                    throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG);
                }
            } catch (SQLException e) {
                log.error("Error occurred while reading detailed consent resource", e);
                throw new OBConsentDataRetrievalException(String.format("Error occurred while retrieving " +
                        "detailed consent resource for consent ID : %s", consentID), e);
            }

            if (log.isDebugEnabled()) {
                log.debug("Retrieved the detailed consent resource for consent ID : " +
                        consentID);
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.DETAILED_CONSENT_RESOURCE_RETRIEVE_ERROR_MSG, e);
            throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants
                    .DETAILED_CONSENT_RESOURCE_RETRIEVE_ERROR_MSG, e);
        }
        return retrievedDetailedConsentResource;
    }

    @Override
    public ConsentResource getConsentResourceWithAttributes(Connection connection, String consentID)
            throws OBConsentDataRetrievalException {

        Map<String, String> retrievedConsentAttributeMap = new HashMap<>();
        ConsentResource retrievedConsentResource = new ConsentResource();

        String getConsentResourcePrepStatement = sqlStatements.getGetConsentWithConsentAttributesPreparedStatement();

        try (PreparedStatement getConsentResourcePreparedStmt =
                     connection.prepareStatement(getConsentResourcePrepStatement, ResultSet.TYPE_SCROLL_INSENSITIVE,
                             ResultSet.CONCUR_READ_ONLY)) {

            log.debug("Setting parameters to prepared statement to retrieve consent resource with consent attributes");

            getConsentResourcePreparedStmt.setString(1, consentID);

            try (ResultSet resultSet = getConsentResourcePreparedStmt.executeQuery()) {
                if (resultSet.next()) {
                    setDataToConsentResource(resultSet, retrievedConsentResource);

                    // Point the cursor to the beginning of the result set to read attributes
                    resultSet.beforeFirst();
                    while (resultSet.next()) {
                        retrievedConsentAttributeMap.put(resultSet.getString(ConsentMgtDAOConstants.ATT_KEY),
                                resultSet.getString(ConsentMgtDAOConstants.ATT_VALUE));
                    }
                    retrievedConsentResource.setConsentAttributes(retrievedConsentAttributeMap);
                } else {
                    log.error("No records are found for consent ID :" + consentID);
                    throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG);
                }
            } catch (SQLException e) {
                log.error("Error occurred while reading consent resource with consent attributes", e);
                throw new OBConsentDataRetrievalException(String.format("Error occurred while retrieving consent " +
                        "resource with consent attributes for consent ID : %s", consentID), e);
            }

            if (log.isDebugEnabled()) {
                log.debug("Retrieved the consent resource with consent attributes for consent ID : " + consentID);
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_ATTRIBUTES_RETRIEVE_ERROR_MSG, e);
            throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.CONSENT_ATTRIBUTES_RETRIEVE_ERROR_MSG, e);
        }
        return retrievedConsentResource;
    }

    @Override
    public AuthorizationResource getAuthorizationResource(Connection connection, String authorizationID)
            throws OBConsentDataRetrievalException {

        AuthorizationResource retrievedAuthorizationResource = new AuthorizationResource();
        String getAuthorizationResourcePrepStatement = sqlStatements.getGetAuthorizationResourcePreparedStatement();

        try (PreparedStatement getConsentResourcePreparedStmt =
                     connection.prepareStatement(getAuthorizationResourcePrepStatement)) {

            log.debug("Setting parameters to prepared statement to retrieve consent authorization resource");

            getConsentResourcePreparedStmt.setString(1, authorizationID);

            try (ResultSet resultSet = getConsentResourcePreparedStmt.executeQuery()) {
                if (resultSet.next()) {
                    setAuthorizationData(retrievedAuthorizationResource, resultSet);
                } else {
                    log.error("No records are found for authorization ID :" + authorizationID);
                    throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG);
                }
            } catch (SQLException e) {
                log.error("Error occurred while reading consent authorization resource", e);
                throw new OBConsentDataRetrievalException(String.format("Error occurred while retrieving consent " +
                        "authorization resource for authorization ID : %s", authorizationID), e);
            }

            if (log.isDebugEnabled()) {
                log.debug("Retrieved the consent authorization resource for authorization ID : " + authorizationID);
            }

        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_AUTHORIZATION_RESOURCE_RETRIEVE_ERROR_MSG, e);
            throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.
                    CONSENT_AUTHORIZATION_RESOURCE_RETRIEVE_ERROR_MSG, e);
        }
        return retrievedAuthorizationResource;
    }

    @Override
    public ArrayList<ConsentStatusAuditRecord> getConsentStatusAuditRecords(Connection connection, String consentID,
                                                                            String currentStatus, String actionBy,
                                                                            Long fromTime, Long toTime,
                                                                            String statusAuditID,
                                                                            boolean fetchFromRetentionTables)
            throws OBConsentDataRetrievalException {

        ArrayList<ConsentStatusAuditRecord> retrievedAuditRecords = new ArrayList<>();
        String getConsentStatusAuditRecordsPrepStatement =
                sqlStatements.getGetConsentStatusAuditRecordsPreparedStatement(fetchFromRetentionTables);

        try (PreparedStatement getConsentStatusAuditRecordPreparedStmt =
                     connection.prepareStatement(getConsentStatusAuditRecordsPrepStatement)) {

            if (log.isDebugEnabled()) {
                log.debug("Setting parameters to prepared statement to retrieve consent status audit records");
            }

            // consentID
            if (StringUtils.trimToNull(consentID) != null) {
                getConsentStatusAuditRecordPreparedStmt.setString(1, consentID);
            } else {
                getConsentStatusAuditRecordPreparedStmt.setNull(1, Types.VARCHAR);
            }

            // currentStatus
            if (StringUtils.trimToNull(currentStatus) != null) {
                getConsentStatusAuditRecordPreparedStmt.setString(2, currentStatus);
            } else {
                getConsentStatusAuditRecordPreparedStmt.setNull(2, Types.VARCHAR);
            }

            // actionBy
            if (StringUtils.trimToNull(actionBy) != null) {
                getConsentStatusAuditRecordPreparedStmt.setString(3, actionBy);
            } else {
                getConsentStatusAuditRecordPreparedStmt.setNull(3, Types.VARCHAR);
            }

            // statusAuditID
            if (StringUtils.trimToNull(statusAuditID) != null) {
                getConsentStatusAuditRecordPreparedStmt.setString(4, statusAuditID);
            } else {
                getConsentStatusAuditRecordPreparedStmt.setNull(4, Types.VARCHAR);
            }

            // fromTime
            if (fromTime != null) {
                getConsentStatusAuditRecordPreparedStmt.setLong(5, fromTime);
            } else {
                getConsentStatusAuditRecordPreparedStmt.setNull(5, Types.BIGINT);
            }

            // toTime
            if (toTime != null) {
                getConsentStatusAuditRecordPreparedStmt.setLong(6, toTime);
            } else {
                getConsentStatusAuditRecordPreparedStmt.setNull(6, Types.BIGINT);
            }

            try (ResultSet resultSet = getConsentStatusAuditRecordPreparedStmt.executeQuery()) {
                if (resultSet.isBeforeFirst()) {
                    while (resultSet.next()) {
                        ConsentStatusAuditRecord consentStatusAuditRecord = new ConsentStatusAuditRecord();
                        consentStatusAuditRecord
                                .setStatusAuditID(resultSet.getString(ConsentMgtDAOConstants.STATUS_AUDIT_ID));
                        consentStatusAuditRecord.setConsentID(resultSet.getString(ConsentMgtDAOConstants.CONSENT_ID));
                        consentStatusAuditRecord
                                .setCurrentStatus(resultSet.getString(ConsentMgtDAOConstants.CURRENT_STATUS));
                        consentStatusAuditRecord.setActionBy(resultSet.getString(ConsentMgtDAOConstants.ACTION_BY));
                        consentStatusAuditRecord.setActionTime(resultSet.getLong(ConsentMgtDAOConstants.ACTION_TIME));
                        consentStatusAuditRecord.setReason(resultSet.getString(ConsentMgtDAOConstants.REASON));
                        consentStatusAuditRecord
                                .setPreviousStatus(resultSet.getString(ConsentMgtDAOConstants.PREVIOUS_STATUS));
                        retrievedAuditRecords.add(consentStatusAuditRecord);
                    }
                } else {
                    log.error("No records are found for the provided inputs");
                    throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG);
                }
            } catch (SQLException e) {
                log.error("Error occurred while reading consent status audit records", e);
                throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.AUDIT_RECORDS_RETRIEVE_ERROR_MSG, e);
            }

            log.debug("Retrieved the consent status audit records successfully");

        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.AUDIT_RECORDS_RETRIEVE_ERROR_MSG, e);
            throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.AUDIT_RECORDS_RETRIEVE_ERROR_MSG, e);
        }
        return retrievedAuditRecords;
    }

    @Override
    public ArrayList<ConsentMappingResource> getConsentMappingResources(Connection connection, String authorizationID)
            throws OBConsentDataRetrievalException {

        ArrayList<ConsentMappingResource> retrievedConsentMappingResources = new ArrayList<>();
        String getMappingResourcePrepStatement = sqlStatements.getGetConsentMappingResourcesPreparedStatement();

        try (PreparedStatement getConsentMappingResourcePreparedStmt =
                     connection.prepareStatement(getMappingResourcePrepStatement)) {

            log.debug("Setting parameters to prepared statement to retrieve consent mapping resources");

            getConsentMappingResourcePreparedStmt.setString(1, authorizationID);

            try (ResultSet resultSet = getConsentMappingResourcePreparedStmt.executeQuery()) {
                if (resultSet.isBeforeFirst()) {
                    while (resultSet.next()) {
                        retrievedConsentMappingResources.add(getConsentMappingResourceWithData(resultSet));
                    }
                } else {
                    log.error("No records are found for authorization ID : " + authorizationID);
                    throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG);
                }
            } catch (SQLException e) {
                log.error("Error occurred while reading consent mapping resources", e);
                throw new OBConsentDataRetrievalException(String.format("Error occurred while retrieving consent " +
                        "mapping resources for authorization ID : %s", authorizationID), e);
            }

            if (log.isDebugEnabled()) {
                log.debug("Retrieved the consent mapping resources for authorization ID : " + authorizationID);
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_MAPPING_RETRIEVE_ERROR_MSG, e);
            throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.CONSENT_MAPPING_RETRIEVE_ERROR_MSG, e);
        }
        return retrievedConsentMappingResources;
    }

    @Override
    public ArrayList<ConsentMappingResource> getConsentMappingResources(Connection connection, String authorizationID,
                                                                        String mappingStatus)
            throws OBConsentDataRetrievalException {

        ArrayList<ConsentMappingResource> retrievedConsentMappingResources = new ArrayList<>();
        String getMappingResourcePrepStatement
                = sqlStatements.getGetConsentMappingResourcesForStatusPreparedStatement();

        try (PreparedStatement getConsentMappingResourcePreparedStmt =
                     connection.prepareStatement(getMappingResourcePrepStatement)) {

            log.debug("Setting parameters to prepared statement to retrieve consent mapping resources");

            getConsentMappingResourcePreparedStmt.setString(1, authorizationID);
            getConsentMappingResourcePreparedStmt.setString(2, mappingStatus);

            try (ResultSet resultSet = getConsentMappingResourcePreparedStmt.executeQuery()) {
                if (resultSet.isBeforeFirst()) {
                    while (resultSet.next()) {
                        retrievedConsentMappingResources.add(getConsentMappingResourceWithData(resultSet));
                    }
                } else {
                    log.error("No records are found for authorization ID : " + authorizationID + " and mapping " +
                            "status " + mappingStatus);
                    throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG);
                }
            } catch (SQLException e) {
                log.error("Error occurred while reading consent mapping resources", e);
                throw new OBConsentDataRetrievalException(String.format("Error occurred while retrieving consent " +
                                "mapping resources for authorization ID : %s and mapping status : %s", authorizationID,
                        mappingStatus), e);
            }

            if (log.isDebugEnabled()) {
                log.debug("Retrieved the consent mapping resources for authorization ID : " + authorizationID + " and" +
                        " mapping status : " + mappingStatus);
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_MAPPING_RETRIEVE_ERROR_MSG, e);
            throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.CONSENT_MAPPING_RETRIEVE_ERROR_MSG, e);
        }
        return retrievedConsentMappingResources;
    }

    @Override
    public boolean deleteConsentAttributes(Connection connection, String consentID,
                                           ArrayList<String> consentAttributeKeys)
            throws OBConsentDataDeletionException {

        int[] result;
        String deleteConsentAttributePrepStatement = sqlStatements.getDeleteConsentAttributePreparedStatement();

        try (PreparedStatement deleteConsentAttributesPreparedStmt =
                     connection.prepareStatement(deleteConsentAttributePrepStatement)) {

            if (log.isDebugEnabled()) {
                log.debug("Setting parameters to prepared statement to delete the provided consent attributes");
            }

            for (String key : consentAttributeKeys) {
                deleteConsentAttributesPreparedStmt.setString(1, consentID);
                deleteConsentAttributesPreparedStmt.setString(2, key);
                deleteConsentAttributesPreparedStmt.addBatch();
            }

            result = deleteConsentAttributesPreparedStmt.executeBatch();
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_ATTRIBUTES_DELETE_ERROR_MSG, e);
            throw new OBConsentDataDeletionException(ConsentMgtDAOConstants.CONSENT_ATTRIBUTES_DELETE_ERROR_MSG, e);
        }

        if (result.length != 0 && IntStream.of(result).noneMatch(value -> value == -3)) {
            if (log.isDebugEnabled()) {
                log.debug("Deleted the consent attribute of key " + consentAttributeKeys);
            }
            return true;
        } else {
            throw new OBConsentDataDeletionException("Failed to delete consent attribute properly.");
        }
    }

    @Override
    public ArrayList<DetailedConsentResource> searchConsents(Connection connection, ArrayList<String> consentIDs,
                                                             ArrayList<String> clientIDs,
                                                             ArrayList<String> consentTypes,
                                                             ArrayList<String> consentStatuses,
                                                             ArrayList<String> userIDs, Long fromTime, Long toTime,
                                                             Integer limit, Integer offset)
            throws OBConsentDataRetrievalException {

        boolean shouldLimit = true;
        boolean shouldOffset = true;
        int parameterIndex = 0;
        Map<String, ArrayList> applicableConditionsMap = new HashMap<>();

        validateAndSetSearchConditions(applicableConditionsMap, consentIDs, clientIDs, consentTypes, consentStatuses);

        // Don't limit if either of limit or offset is null
        if (limit == null) {
            shouldLimit = false;
        }
        if (offset == null) {
            shouldOffset = false;
        }

        // logic to set the prepared statement
        log.debug("Constructing the prepared statement");
        String constructedConditions =
                ConsentDAOUtils.constructConsentSearchPreparedStatement(applicableConditionsMap);

        String userIDFilterCondition = "";
        Map<String, ArrayList> userIdMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(userIDs)) {
            userIdMap.put(COLUMNS_MAP.get(ConsentMgtDAOConstants.USER_IDS), userIDs);
            userIDFilterCondition = ConsentDAOUtils.constructUserIdListFilterCondition(userIdMap);
        }

        String searchConsentsPreparedStatement =
                sqlStatements.getSearchConsentsPreparedStatement(constructedConditions, shouldLimit, shouldOffset,
                        userIDFilterCondition);

        try (PreparedStatement searchConsentsPreparedStmt =
                     connection.prepareStatement(searchConsentsPreparedStatement, ResultSet.TYPE_SCROLL_INSENSITIVE,
                             ResultSet.CONCUR_UPDATABLE)) {

            /* Since we don't know the order of the set condition clauses, have to determine the order of them to set
               the actual values to the  prepared statement */
            Map<Integer, ArrayList> orderedParamsMap = ConsentDAOUtils
                    .determineOrderOfParamsToSet(constructedConditions, applicableConditionsMap, COLUMNS_MAP);

            log.debug("Setting parameters to prepared statement to search consents");

            parameterIndex = setDynamicConsentSearchParameters(searchConsentsPreparedStmt, orderedParamsMap,
                    ++parameterIndex);
            parameterIndex = parameterIndex - 1;

            //determine order of user Ids to set
            if (CollectionUtils.isNotEmpty(userIDs)) {
                Map<Integer, ArrayList> orderedUserIdsMap = ConsentDAOUtils
                        .determineOrderOfParamsToSet(userIDFilterCondition, userIdMap, COLUMNS_MAP);
                parameterIndex = setDynamicConsentSearchParameters(searchConsentsPreparedStmt, orderedUserIdsMap,
                        ++parameterIndex);
                parameterIndex = parameterIndex - 1;
            }

            if (fromTime != null) {
                searchConsentsPreparedStmt.setLong(++parameterIndex, fromTime);
            } else {
                searchConsentsPreparedStmt.setNull(++parameterIndex, Types.BIGINT);
            }

            if (toTime != null) {
                searchConsentsPreparedStmt.setLong(++parameterIndex, toTime);
            } else {
                searchConsentsPreparedStmt.setNull(++parameterIndex, Types.BIGINT);
            }

            if (limit != null && offset != null) {
                searchConsentsPreparedStmt.setInt(++parameterIndex, limit);
                searchConsentsPreparedStmt.setInt(++parameterIndex, offset);
            } else if (limit != null) {
                searchConsentsPreparedStmt.setInt(++parameterIndex, limit);
            }
            ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();

            try (ResultSet resultSet = searchConsentsPreparedStmt.executeQuery()) {
                if (resultSet.isBeforeFirst()) {
                    int resultSetSize = getResultSetSize(resultSet);
                    detailedConsentResources = constructDetailedConsentsSearchResult(resultSet, resultSetSize);
                }
                return detailedConsentResources;
            } catch (SQLException e) {
                log.error("Error occurred while searching detailed consent resources", e);
                throw new OBConsentDataRetrievalException("Error occurred while searching detailed " +
                        "consent resources", e);
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_SEARCH_ERROR_MSG, e);
            throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.CONSENT_SEARCH_ERROR_MSG);
        }
    }

    @Override
    public ArrayList<AuthorizationResource> searchConsentAuthorizations(Connection connection, String consentID,
                                                                        String userID)
            throws OBConsentDataRetrievalException {

        ArrayList<AuthorizationResource> retrievedAuthorizationResources = new ArrayList<>();
        Map<String, String> conditions = new HashMap<>();
        if (StringUtils.trimToNull(consentID) != null) {
            conditions.put("CONSENT_ID", consentID);
        }
        if (StringUtils.trimToNull(userID) != null) {
            conditions.put("USER_ID", userID);
        }
        String whereClause = ConsentDAOUtils.constructAuthSearchPreparedStatement(conditions);
        String searchAuthorizationResourcesPrepStatement =
                sqlStatements.getSearchAuthorizationResourcesPreparedStatement(whereClause);

        try (PreparedStatement getSearchAuthorizationResourcesPreparedStmt =
                     connection.prepareStatement(searchAuthorizationResourcesPrepStatement)) {

            if (log.isDebugEnabled()) {
                log.debug("Setting parameters to prepared statement to search authorization resources");
            }

            Iterator<Map.Entry<String, String>> conditionIterator = conditions.entrySet().iterator();

            for (int count = 1; count <= conditions.size(); count++) {
                getSearchAuthorizationResourcesPreparedStmt.setString(count, conditionIterator.next().getValue());
            }

            try (ResultSet resultSet = getSearchAuthorizationResourcesPreparedStmt.executeQuery()) {
                if (resultSet.isBeforeFirst()) {
                    while (resultSet.next()) {
                        AuthorizationResource authorizationResource = new AuthorizationResource();
                        authorizationResource
                                .setAuthorizationID(resultSet.getString(ConsentMgtDAOConstants.AUTH_ID));
                        authorizationResource.setConsentID(resultSet.getString(ConsentMgtDAOConstants.CONSENT_ID));
                        authorizationResource
                                .setUserID(resultSet.getString(ConsentMgtDAOConstants.USER_ID));
                        authorizationResource.setAuthorizationStatus(resultSet
                                .getString(ConsentMgtDAOConstants.AUTH_STATUS));
                        authorizationResource
                                .setAuthorizationType(resultSet.getString(ConsentMgtDAOConstants.AUTH_TYPE));
                        authorizationResource.setUpdatedTime(resultSet.getLong(ConsentMgtDAOConstants.UPDATED_TIME));
                        retrievedAuthorizationResources.add(authorizationResource);
                    }
                } else {
                    log.error("No records are found for the provided inputs");
                    throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG);
                }
            } catch (SQLException e) {
                log.error("Error occurred while searching authorization resources", e);
                throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants
                        .CONSENT_AUTHORIZATION_RESOURCE_RETRIEVE_ERROR_MSG, e);
            }
            log.debug("Retrieved the authorization resources successfully");
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_AUTHORIZATION_RESOURCE_RETRIEVE_ERROR_MSG, e);
            throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants
                    .CONSENT_AUTHORIZATION_RESOURCE_RETRIEVE_ERROR_MSG, e);
        }
        return retrievedAuthorizationResources;
    }

    /**
     * Set data from the result set to ConsentResource object.
     *
     * @param resultSet       result set
     * @param consentResource consent resource
     * @throws SQLException thrown if an error occurs when getting data from the result set
     */
    private void setDataToConsentResource(ResultSet resultSet, ConsentResource consentResource) throws SQLException {

        consentResource.setConsentID(resultSet.getString(ConsentMgtDAOConstants.CONSENT_ID));
        consentResource.setReceipt(resultSet.getString(ConsentMgtDAOConstants.RECEIPT));
        consentResource.setCreatedTime(resultSet.getLong(ConsentMgtDAOConstants.CREATED_TIME));
        consentResource.setUpdatedTime(resultSet.getLong(ConsentMgtDAOConstants.UPDATED_TIME));
        consentResource.setClientID(resultSet.getString(ConsentMgtDAOConstants.CLIENT_ID));
        consentResource.setConsentType(resultSet.getString(ConsentMgtDAOConstants.CONSENT_TYPE));
        consentResource.
                setCurrentStatus(resultSet.getString(ConsentMgtDAOConstants.CURRENT_STATUS));
        consentResource.setConsentFrequency(resultSet
                .getInt(ConsentMgtDAOConstants.CONSENT_FREQUENCY));
        consentResource.setValidityPeriod(resultSet.getLong(ConsentMgtDAOConstants.VALIDITY_TIME));
        consentResource.setRecurringIndicator(resultSet.getBoolean(
                ConsentMgtDAOConstants.RECURRING_INDICATOR));
    }

    /**
     * Set data from the result set to DetaildConsentResource object.
     *
     * @param resultSet               result set
     * @param detailedConsentResource consent resource
     * @throws SQLException thrown if an error occurs when getting data from the result set
     */
    private void setDataToDetailedConsentResource(ResultSet resultSet, DetailedConsentResource detailedConsentResource)
            throws SQLException {

        Map<String, String> consentAttributesMap = new HashMap<>();
        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();
        ArrayList<String> authIds = new ArrayList<>();
        ArrayList<String> consentMappingIds = new ArrayList<>();

        while (resultSet.next()) {
            // Set data related to the consent resource
            setConsentDataToDetailedConsentResource(resultSet, detailedConsentResource);

            // Set data related to consent attributes
            if (StringUtils.isNotBlank(resultSet.getString(ConsentMgtDAOConstants.ATT_KEY))) {
                String attributeValue = resultSet.getString(ConsentMgtDAOConstants.ATT_VALUE);

                // skip adding all temporary session data to consent attributes
                if (!(JSONValue.isValidJson(attributeValue) && attributeValue.contains(SESSION_DATA_KEY))) {
                    consentAttributesMap.put(resultSet.getString(ConsentMgtDAOConstants.ATT_KEY),
                            attributeValue);
                }
            }

            // Set data related to authorization resources
            if (authIds.isEmpty()) {
                AuthorizationResource authorizationResource = new AuthorizationResource();
                authorizationResource.setAuthorizationID(resultSet.getString(ConsentMgtDAOConstants.AUTH_ID));
                authorizationResource.setConsentID(resultSet.getString(ConsentMgtDAOConstants.CONSENT_ID));
                authorizationResource.setAuthorizationStatus(resultSet.getString(ConsentMgtDAOConstants.AUTH_STATUS));
                authorizationResource.setAuthorizationType(resultSet.getString(ConsentMgtDAOConstants.AUTH_TYPE));
                authorizationResource.setUserID(resultSet.getString(ConsentMgtDAOConstants.USER_ID));
                authorizationResource.setUpdatedTime(resultSet.getLong(ConsentMgtDAOConstants.AUTH_UPDATED_TIME));
                authorizationResources.add(authorizationResource);
                authIds.add(authorizationResource.getAuthorizationID());
            } else {
                if (!authIds.contains(resultSet.getString(ConsentMgtDAOConstants.AUTH_ID))) {
                    AuthorizationResource authorizationResource = new AuthorizationResource();
                    authorizationResource.setAuthorizationID(resultSet.getString(ConsentMgtDAOConstants.AUTH_ID));
                    authorizationResource.setConsentID(resultSet.getString(ConsentMgtDAOConstants.CONSENT_ID));
                    authorizationResource
                            .setAuthorizationStatus(resultSet.getString(ConsentMgtDAOConstants.AUTH_STATUS));
                    authorizationResource.setAuthorizationType(resultSet.getString(ConsentMgtDAOConstants.AUTH_TYPE));
                    authorizationResource.setUserID(resultSet.getString(ConsentMgtDAOConstants.USER_ID));
                    authorizationResource.setUpdatedTime(resultSet.getLong(ConsentMgtDAOConstants.AUTH_UPDATED_TIME));
                    authorizationResources.add(authorizationResource);
                    authIds.add(authorizationResource.getAuthorizationID());
                }
            }

            // Set data related to consent account mappings
            // Check whether consentMappingIds is empty and result set consists a mapping id since at this moment
            //  there can be a situation where an auth resource is created and mapping resource is not created
            if (consentMappingIds.isEmpty() && resultSet.getString(ConsentMgtDAOConstants.MAPPING_ID) != null) {
                ConsentMappingResource consentMappingResource = new ConsentMappingResource();
                consentMappingResource.setAuthorizationID(resultSet.getString(ConsentMgtDAOConstants.AUTH_ID));
                consentMappingResource.setAccountID(resultSet.getString(ConsentMgtDAOConstants.ACCOUNT_ID));
                consentMappingResource.setMappingID(resultSet.getString(ConsentMgtDAOConstants.MAPPING_ID));
                consentMappingResource.setMappingStatus(resultSet.getString(ConsentMgtDAOConstants.MAPPING_STATUS));
                consentMappingResource.setPermission(resultSet.getString(ConsentMgtDAOConstants.PERMISSION));
                consentMappingResources.add(consentMappingResource);
                consentMappingIds.add(consentMappingResource.getMappingID());
            } else {
                // Check whether result set consists a mapping id since at this moment, there can be a situation
                //  where an auth resource is created and mapping resource is not created
                if (!consentMappingIds.contains(resultSet.getString(ConsentMgtDAOConstants.MAPPING_ID)) &&
                        resultSet.getString(ConsentMgtDAOConstants.MAPPING_ID) != null) {
                    ConsentMappingResource consentMappingResource = new ConsentMappingResource();
                    consentMappingResource.setAuthorizationID(resultSet.getString(ConsentMgtDAOConstants.AUTH_ID));
                    consentMappingResource.setAccountID(resultSet.getString(ConsentMgtDAOConstants.ACCOUNT_ID));
                    consentMappingResource.setMappingID(resultSet.getString(ConsentMgtDAOConstants.MAPPING_ID));
                    consentMappingResource.setMappingStatus(resultSet.getString(ConsentMgtDAOConstants.MAPPING_STATUS));
                    consentMappingResource.setPermission(resultSet.getString(ConsentMgtDAOConstants.PERMISSION));
                    consentMappingResources.add(consentMappingResource);
                    consentMappingIds.add(consentMappingResource.getMappingID());
                }
            }
        }

        // Set consent attributes, auth resources and account mappings to detailed consent resource
        detailedConsentResource.setConsentAttributes(consentAttributesMap);
        detailedConsentResource.setAuthorizationResources(authorizationResources);
        detailedConsentResource.setConsentMappingResources(consentMappingResources);
    }

    void setConsentDataToDetailedConsentResource(ResultSet resultSet,
                                                 DetailedConsentResource detailedConsentResource)
            throws SQLException {

        detailedConsentResource.setConsentID(resultSet.getString(ConsentMgtDAOConstants.CONSENT_ID));
        detailedConsentResource.setClientID(resultSet.getString(ConsentMgtDAOConstants.CLIENT_ID));
        detailedConsentResource.setReceipt(resultSet.getString(ConsentMgtDAOConstants.RECEIPT));
        detailedConsentResource.setCreatedTime(resultSet.getLong(ConsentMgtDAOConstants.CONSENT_CREATED_TIME));
        detailedConsentResource.setUpdatedTime(resultSet.getLong(ConsentMgtDAOConstants.CONSENT_UPDATED_TIME));
        detailedConsentResource.setConsentType(resultSet.getString(ConsentMgtDAOConstants.CONSENT_TYPE));
        detailedConsentResource.setCurrentStatus(resultSet.getString(ConsentMgtDAOConstants.CURRENT_STATUS));
        detailedConsentResource.setConsentFrequency(resultSet.getInt(ConsentMgtDAOConstants.CONSENT_FREQUENCY));
        detailedConsentResource.setValidityPeriod(resultSet.getLong(ConsentMgtDAOConstants.VALIDITY_TIME));
        detailedConsentResource.setRecurringIndicator(resultSet
                .getBoolean(ConsentMgtDAOConstants.RECURRING_INDICATOR));
    }

    /**
     * Return a consent mapping resource with data set from the result set.
     *
     * @param resultSet result set
     * @return a consent mapping resource
     * @throws SQLException thrown if an error occurs when getting data from the result set
     */
    private ConsentMappingResource getConsentMappingResourceWithData(ResultSet resultSet) throws SQLException {

        ConsentMappingResource consentMappingResource = new ConsentMappingResource();
        consentMappingResource.setAuthorizationID(resultSet.getString(ConsentMgtDAOConstants.AUTH_ID));
        consentMappingResource.setMappingID(resultSet.getString(ConsentMgtDAOConstants.MAPPING_ID));
        consentMappingResource.setAccountID(resultSet.getString(ConsentMgtDAOConstants.ACCOUNT_ID));
        consentMappingResource.setPermission(resultSet.getString(ConsentMgtDAOConstants.PERMISSION));
        consentMappingResource.setMappingStatus(resultSet.getString(ConsentMgtDAOConstants.MAPPING_STATUS));

        return consentMappingResource;
    }

    /**
     * Sets search parameters to dynamically constructed prepared statement. The outer loop is used to iterate the
     * different AND clauses and the inner loop is to iterate the number of placeholders of the current AND clause.
     *
     * @param preparedStatement dynamically constructed prepared statement
     * @param orderedParamsMap  map with ordered AND conditions
     * @param parameterIndex    index which the parameter should be set
     * @return the final parameter index
     * @throws SQLException thrown if an error occurs in the process
     */
    int setDynamicConsentSearchParameters(PreparedStatement preparedStatement, Map<Integer, ArrayList> orderedParamsMap,
                                          int parameterIndex)
            throws SQLException {

        for (Map.Entry<Integer, ArrayList> entry : orderedParamsMap.entrySet()) {
            for (int valueIndex = 0; valueIndex < entry.getValue().size(); valueIndex++) {
                preparedStatement.setString(parameterIndex, ((String) entry.getValue().get(valueIndex)).trim());
                parameterIndex++;
            }
        }
        return parameterIndex;
    }

    int getResultSetSize(ResultSet resultSet) throws SQLException {

        resultSet.last();
        int resultSetSize = resultSet.getRow();

        // Point result set back before first
        resultSet.beforeFirst();
        return resultSetSize;
    }

    void setAuthorizationData(AuthorizationResource authorizationResource, ResultSet resultSet)
            throws SQLException {

        authorizationResource.setAuthorizationID(resultSet
                .getString(ConsentMgtDAOConstants.AUTH_ID));
        authorizationResource.setConsentID(resultSet.getString(ConsentMgtDAOConstants.CONSENT_ID));
        authorizationResource.setAuthorizationType(resultSet
                .getString(ConsentMgtDAOConstants.AUTH_TYPE));
        authorizationResource.setAuthorizationStatus(resultSet
                .getString(ConsentMgtDAOConstants.AUTH_STATUS));
        authorizationResource.setUpdatedTime(resultSet
                .getLong(ConsentMgtDAOConstants.UPDATED_TIME));
        authorizationResource.setUserID(resultSet.getString(ConsentMgtDAOConstants.USER_ID));
    }

    protected void setAuthorizationDataInResponseForGroupedQuery(ArrayList<AuthorizationResource>
                                                                         authorizationResources,
                                                                 ResultSet resultSet, String consentId)
            throws SQLException {

        //identify duplicate auth data
        Set<String> authIdSet = new HashSet<>();

        // fetch values from group_concat
        String[] authIds = resultSet.getString(ConsentMgtDAOConstants.AUTH_ID) != null ?
                resultSet.getString(ConsentMgtDAOConstants.AUTH_ID).split(GROUP_BY_SEPARATOR) : null;
        String[] authTypes = resultSet.getString(ConsentMgtDAOConstants.AUTH_TYPE) != null ?
                resultSet.getString(ConsentMgtDAOConstants.AUTH_TYPE).split(GROUP_BY_SEPARATOR) : null;
        String[] authStatues = resultSet.getString(ConsentMgtDAOConstants.AUTH_STATUS) != null ?
                resultSet.getString(ConsentMgtDAOConstants.AUTH_STATUS).split(GROUP_BY_SEPARATOR) : null;
        String[] updatedTimes = resultSet.getString(ConsentMgtDAOConstants.UPDATED_TIME) != null ?
                resultSet.getString(ConsentMgtDAOConstants.UPDATED_TIME).split(GROUP_BY_SEPARATOR) : null;
        String[] userIds = resultSet.getString(ConsentMgtDAOConstants.USER_ID) != null ?
                resultSet.getString(ConsentMgtDAOConstants.USER_ID).split(GROUP_BY_SEPARATOR) : null;

        for (int index = 0; index < (authIds != null ? authIds.length : 0); index++) {
            if (!authIdSet.contains(authIds[index])) {
                AuthorizationResource authorizationResource = new AuthorizationResource();
                authIdSet.add(authIds[index]);
                authorizationResource.setAuthorizationID(authIds[index]);
                authorizationResource.setConsentID(consentId);
                if (authTypes != null && authTypes.length > index) {
                    authorizationResource.setAuthorizationType(authTypes[index]);
                }
                if (authStatues != null && authStatues.length > index) {
                    authorizationResource.setAuthorizationStatus(authStatues[index]);
                }
                if (updatedTimes != null && updatedTimes.length > index) {
                    authorizationResource.setUpdatedTime(Long.parseLong(updatedTimes[index]));
                }
                if (userIds != null && userIds.length > index) {
                    authorizationResource.setUserID(userIds[index]);
                }
                authorizationResources.add(authorizationResource);
            }
        }

    }

    protected void setAccountConsentMappingDataInResponse(ArrayList<ConsentMappingResource> consentMappingResources,
                                                ResultSet resultSet) throws SQLException {

        //identify duplicate mappingIds
        Set<String> mappingIdSet = new HashSet<>();

        // fetch values from group_concat
        String[] authIds = resultSet.getString(ConsentMgtDAOConstants.AUTH_MAPPING_ID) != null ?
                resultSet.getString(ConsentMgtDAOConstants.AUTH_MAPPING_ID).split(GROUP_BY_SEPARATOR) : null;
        String[] mappingIds = resultSet.getString(ConsentMgtDAOConstants.MAPPING_ID) != null ?
                resultSet.getString(ConsentMgtDAOConstants.MAPPING_ID).split(GROUP_BY_SEPARATOR) : null;
        String[] accountIds = resultSet.getString(ConsentMgtDAOConstants.ACCOUNT_ID) != null ?
                resultSet.getString(ConsentMgtDAOConstants.ACCOUNT_ID).split(GROUP_BY_SEPARATOR) : null;
        String[] mappingStatues = resultSet.getString(ConsentMgtDAOConstants.MAPPING_STATUS) != null ?
                resultSet.getString(ConsentMgtDAOConstants.MAPPING_STATUS).split(GROUP_BY_SEPARATOR) : null;
        String[] permissions = resultSet.getString(ConsentMgtDAOConstants.PERMISSION) != null ?
                resultSet.getString(ConsentMgtDAOConstants.PERMISSION).split(GROUP_BY_SEPARATOR) : null;

        for (int index = 0; index < (mappingIds != null ? mappingIds.length : 0); index++) {
            if (!mappingIdSet.contains(mappingIds[index])) {
                ConsentMappingResource consentMappingResource = new ConsentMappingResource();
                if (authIds != null && authIds.length > index) {
                    consentMappingResource.setAuthorizationID(authIds[index]);
                }
                consentMappingResource.setMappingID(mappingIds[index]);
                if (accountIds != null && accountIds.length > index) {
                    consentMappingResource.setAccountID(accountIds[index]);
                }
                if (mappingStatues != null && mappingStatues.length > index) {
                    consentMappingResource.setMappingStatus(mappingStatues[index]);
                }
                if (permissions != null && permissions.length > index) {
                    consentMappingResource.setPermission(permissions[index]);
                }
                consentMappingResources.add(consentMappingResource);
                mappingIdSet.add(mappingIds[index]);
            }
        }

    }

    void validateAndSetSearchConditions(Map<String, ArrayList> applicableConditionsMap, ArrayList<String> consentIDs,
                                        ArrayList<String> clientIDs,
                                        ArrayList<String> consentTypes,
                                        ArrayList<String> consentStatuses) {

        log.debug("Validate applicable search conditions");

        if (CollectionUtils.isNotEmpty(consentIDs)) {
            applicableConditionsMap.put(COLUMNS_MAP.get(ConsentMgtDAOConstants.CONSENT_IDS), consentIDs);
        }
        if (CollectionUtils.isNotEmpty(clientIDs)) {
            applicableConditionsMap.put(COLUMNS_MAP.get(ConsentMgtDAOConstants.CLIENT_IDS), clientIDs);
        }
        if (CollectionUtils.isNotEmpty(consentTypes)) {
            applicableConditionsMap.put(COLUMNS_MAP.get(ConsentMgtDAOConstants.CONSENT_TYPES), consentTypes);
        }
        if (CollectionUtils.isNotEmpty(consentStatuses)) {
            applicableConditionsMap.put(COLUMNS_MAP.get(ConsentMgtDAOConstants.CONSENT_STATUSES), consentStatuses);
        }
    }

    ArrayList<DetailedConsentResource> constructDetailedConsentsSearchResult(ResultSet resultSet, int resultSetSize)
            throws SQLException {

        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();

        while (resultSet.next()) {

            Map<String, String> consentAttributesMap = new HashMap<>();
            ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();
            ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
            DetailedConsentResource detailedConsentResource = new DetailedConsentResource();

            setConsentDataToDetailedConsentResource(resultSet, detailedConsentResource);

            // Set consent attributes to map if available
            if (resultSet.getString(ConsentMgtDAOConstants.ATT_KEY) != null &&
                    StringUtils.isNotBlank(resultSet.getString(ConsentMgtDAOConstants.ATT_KEY))
                    && StringUtils.isNotBlank(resultSet.getString(ConsentMgtDAOConstants.ATT_VALUE))) {
                // fetch attribute keys and values from group_concat
                String[] attributeKeys = resultSet.getString(ConsentMgtDAOConstants.ATT_KEY).split(GROUP_BY_SEPARATOR);
                String[] attributeValues = resultSet
                        .getString(ConsentMgtDAOConstants.ATT_VALUE).split(GROUP_BY_SEPARATOR);
                // check if all attribute keys has values
                if (attributeKeys.length == attributeValues.length) {
                    for (int index = 0; index < attributeKeys.length; index++) {
                        consentAttributesMap.put(attributeKeys[index], attributeValues[index]);
                    }
                }
            }
            // Set authorization data
            setAuthorizationDataInResponseForGroupedQuery(authorizationResources, resultSet,
                    detailedConsentResource.getConsentID());
            // Set consent account mapping data if available
            setAccountConsentMappingDataInResponse(consentMappingResources, resultSet);

            detailedConsentResource.setConsentAttributes(consentAttributesMap);
            detailedConsentResource.setAuthorizationResources(authorizationResources);
            detailedConsentResource.setConsentMappingResources(consentMappingResources);

            detailedConsentResources.add(detailedConsentResource);

        }
        return detailedConsentResources;
    }

    @Override
    public boolean updateConsentReceipt(Connection connection, String consentID, String consentReceipt)
            throws OBConsentDataUpdationException {

        int result;
        String updateConsentReceiptPrepStatement = sqlStatements.getUpdateConsentReceiptPreparedStatement();

        try (PreparedStatement updateConsentReceiptPreparedStmt =
                     connection.prepareStatement(updateConsentReceiptPrepStatement)) {

            log.debug("Setting parameters to prepared statement to update consent receipt");

            updateConsentReceiptPreparedStmt.setString(1, consentReceipt);
            updateConsentReceiptPreparedStmt.setString(2, consentID);

            // with result, we can determine whether the updating was successful or not
            result = updateConsentReceiptPreparedStmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error while updating consent receipt", e);
            throw new OBConsentDataUpdationException("Error while updating consent receipt for consent ID: "
                    + consentID, e);
        }

        // Confirm that the data are updated successfully
        if (result > 0) {
            return true;
        } else {
            throw new OBConsentDataUpdationException("Failed to update consent receipt properly.");
        }
    }

    @Override
    public boolean updateConsentValidityTime(Connection connection, String consentID, long validityTime)
            throws OBConsentDataUpdationException {

        int result;
        String updateConsentReceiptPrepStatement = sqlStatements.getUpdateConsentValidityTimePreparedStatement();
        long updatedTime = System.currentTimeMillis() / 1000;

        try (PreparedStatement updateConsentValidityTimePreparedStmt =
                     connection.prepareStatement(updateConsentReceiptPrepStatement)) {

            log.debug("Setting parameters to prepared statement to update consent receipt");

            updateConsentValidityTimePreparedStmt.setLong(1, validityTime);
            updateConsentValidityTimePreparedStmt.setLong(2, updatedTime);
            updateConsentValidityTimePreparedStmt.setString(3, consentID);

            // with result, we can determine whether the updating was successful or not
            result = updateConsentValidityTimePreparedStmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error while updating consent validity time", e);
            throw new OBConsentDataUpdationException("Error while updating consent validity time for consent ID: "
                    + consentID, e);
        }

        // Confirm that the data are updated successfully
        if (result > 0) {
            return true;
        } else {
            throw new OBConsentDataUpdationException("Failed to update consent validity time properly.");
        }
    }

    @Override
    public boolean storeConsentAmendmentHistory(Connection connection, String historyID, long timestamp,
                    String recordID, String consentDataType, String changedAttributesJsonString, String amendmentReason)
            throws OBConsentDataInsertionException {

        String tableID = generateConsentTableId(consentDataType);

        int result;
        String insertConsentHistoryPrepStatement = sqlStatements.getInsertConsentHistoryPreparedStatement();

        try (PreparedStatement insertConsentHistoryPreparedStmt =
                     connection.prepareStatement(insertConsentHistoryPrepStatement)) {

            if (log.isDebugEnabled()) {
                log.debug(String.format("Setting parameters to prepared statement to store consent amendment history " +
                                "of %s", consentDataType));
            }

            insertConsentHistoryPreparedStmt.setString(1, tableID);
            insertConsentHistoryPreparedStmt.setString(2, recordID);
            insertConsentHistoryPreparedStmt.setString(3, historyID);
            insertConsentHistoryPreparedStmt.setString(4, changedAttributesJsonString);
            insertConsentHistoryPreparedStmt.setString(5, amendmentReason);
            insertConsentHistoryPreparedStmt.setLong(6, timestamp);

            // with result, we can determine whether the updating was successful or not
            result = insertConsentHistoryPreparedStmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error while storing consent amendment history", e);
            throw new OBConsentDataInsertionException(String.format("Error while storing consent amendment history of" +
                    " %s for record ID: %s", consentDataType, recordID), e);
        }

        // Confirm that the data are inserted successfully
        if (result > 0) {
            return true;
        } else {
            log.error("Failed to store consent amendment history data.");
            throw new OBConsentDataInsertionException("Failed to store consent amendment history data properly.");
        }
    }

    @Override
    public Map<String, ConsentHistoryResource> retrieveConsentAmendmentHistory(Connection connection,
                           List<String> recordIDsList) throws OBConsentDataRetrievalException {

        String whereClause = ConsentDAOUtils.constructConsentHistoryPreparedStatement(recordIDsList.size());
        String getConsentHistoryPrepStatement = sqlStatements.getGetConsentHistoryPreparedStatement(whereClause);

        try (PreparedStatement getConsentHistoryPreparedStmt =
                     connection.prepareStatement(getConsentHistoryPrepStatement)) {

            log.debug("Setting parameters to prepared statement to retrieve consent history data");

            for (int count = 1; count <= recordIDsList.size(); count++) {
                getConsentHistoryPreparedStmt.setString(count, recordIDsList.get(count - 1));
            }

            String consentID = recordIDsList.get(0);
            try (ResultSet resultSet = getConsentHistoryPreparedStmt.executeQuery()) {
                if (resultSet.isBeforeFirst()) {
                    return constructConsentHistoryRetrievalResult(consentID, resultSet);
                } else {
                    log.error("No records are found for consent ID : " + consentID);
                    return new HashMap<>();
                }
            } catch (SQLException e) {
                log.error("Error occurred while reading consent amendment history", e);
                throw new OBConsentDataRetrievalException(String.format("Error occurred while retrieving consent " +
                        "amendment history for consent ID : %s", consentID), e);
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_AMENDMENT_HISTORY_RETRIEVE_ERROR_MSG, e);
            throw new OBConsentDataRetrievalException(
                    ConsentMgtDAOConstants.CONSENT_AMENDMENT_HISTORY_RETRIEVE_ERROR_MSG, e);
        }
    }

    /**
     * construct a data map that includes the changed attributes of each consent amendment history entry and.
     * return a map of ConsentHistoryResources including this changed attributes data map
     *
     * @param consentId consent Id
     * @param resultSet result set
     * @return  a map of ConsentHistoryResources
     * @throws SQLException thrown if an error occurs when getting data from the result set
     */
    private Map<String, ConsentHistoryResource> constructConsentHistoryRetrievalResult(String consentId,
                                                                                       ResultSet resultSet)
            throws SQLException {

        Map<String, ConsentHistoryResource> consentAmendmentHistoryDataMap = new LinkedHashMap<>();

        while (resultSet.next()) {
            String tableID = resultSet.getString(ConsentMgtDAOConstants.TABLE_ID);
            String recordID = resultSet.getString(ConsentMgtDAOConstants.RECORD_ID);
            String historyId = resultSet.getString(ConsentMgtDAOConstants.HISTORY_ID);
            String changedAttributesString = resultSet.getString(ConsentMgtDAOConstants.CHANGED_VALUES);
            String amendmentReason = resultSet.getString(ConsentMgtDAOConstants.REASON);
            Long timestamp = resultSet.getLong(ConsentMgtDAOConstants.EFFECTIVE_TIMESTAMP);

            ConsentHistoryResource consentHistoryResource;
            Map<String, Object> changedAttributesJsonDataMap;
            if (consentAmendmentHistoryDataMap.containsKey(historyId)) {
                consentHistoryResource = consentAmendmentHistoryDataMap.get(historyId);
            } else {
                consentHistoryResource = new ConsentHistoryResource(consentId, historyId);
                consentHistoryResource.setTimestamp(timestamp);
                consentHistoryResource.setReason(amendmentReason);
            }

            changedAttributesJsonDataMap = consentHistoryResource.getChangedAttributesJsonDataMap();

            if (TABLES_MAP.get(ConsentMgtDAOConstants.TABLE_OB_CONSENT).equalsIgnoreCase(tableID)) {
                changedAttributesJsonDataMap.put(ConsentMgtDAOConstants.TYPE_CONSENT_BASIC_DATA,
                        changedAttributesString);
            } else if (TABLES_MAP.get(ConsentMgtDAOConstants.TABLE_OB_CONSENT_ATTRIBUTE).equalsIgnoreCase(tableID)) {
                changedAttributesJsonDataMap.put(ConsentMgtDAOConstants.TYPE_CONSENT_ATTRIBUTES_DATA,
                        changedAttributesString);
            } else if (TABLES_MAP.get(ConsentMgtDAOConstants.TABLE_OB_CONSENT_AUTH_RESOURCE)
                    .equalsIgnoreCase(tableID)) {
                Map<String, Object> consentAuthResources;
                if (changedAttributesJsonDataMap.containsKey(ConsentMgtDAOConstants.TYPE_CONSENT_AUTH_RESOURCE_DATA)) {
                    consentAuthResources = (Map<String, Object>) changedAttributesJsonDataMap
                            .get(ConsentMgtDAOConstants.TYPE_CONSENT_AUTH_RESOURCE_DATA);
                } else {
                    consentAuthResources = new HashMap<>();
                }
                consentAuthResources.put(recordID, changedAttributesString);
                changedAttributesJsonDataMap.put(ConsentMgtDAOConstants.TYPE_CONSENT_AUTH_RESOURCE_DATA,
                        consentAuthResources);
            } else if (TABLES_MAP.get(ConsentMgtDAOConstants.TABLE_OB_CONSENT_MAPPING).equalsIgnoreCase(tableID)) {
                Map<String, Object> consentMappingResources;
                if (changedAttributesJsonDataMap.containsKey(ConsentMgtDAOConstants.TYPE_CONSENT_MAPPING_DATA)) {
                    consentMappingResources = (Map<String, Object>) changedAttributesJsonDataMap
                            .get(ConsentMgtDAOConstants.TYPE_CONSENT_MAPPING_DATA);
                } else {
                    consentMappingResources = new HashMap<>();
                }
                consentMappingResources.put(recordID, changedAttributesString);
                changedAttributesJsonDataMap.put(ConsentMgtDAOConstants.TYPE_CONSENT_MAPPING_DATA,
                        consentMappingResources);
            } else {
                log.error(String.format("The retrieved tableId : %s has no corresponding consent data type to be" +
                        " matched", tableID));
            }
            consentHistoryResource.setChangedAttributesJsonDataMap(changedAttributesJsonDataMap);
            consentAmendmentHistoryDataMap.put(historyId, consentHistoryResource);
        }
        return consentAmendmentHistoryDataMap;
    }

    public ArrayList<DetailedConsentResource> getExpiringConsents(Connection connection,
                                                                  String statusesEligibleForExpiration)
            throws OBConsentDataRetrievalException {

        List<String> statusesEligibleForExpirationList = Arrays.asList(statusesEligibleForExpiration.split(","))
                .stream().filter(status -> !status.isEmpty())
                .collect(Collectors.toList());

        String statusesEligibleForExpirationCondition = ConsentDAOUtils.constructStatusesEligibleForExpirationCondition(
                statusesEligibleForExpirationList);
        String expiringConsentStatement = sqlStatements.getSearchExpiringConsentPreparedStatement(
                statusesEligibleForExpirationCondition);

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(expiringConsentStatement)) {

            log.debug("Setting parameters to prepared statement to fetch consents eligible for expiration");

            ArrayList<String> consentIdList = new ArrayList<>();

            // populate prepared statement
            int parameterIndex = 0;
            preparedStatement.setString(++parameterIndex, ConsentMgtDAOConstants.CONSENT_EXPIRY_TIME_ATTRIBUTE);
            for (String status : statusesEligibleForExpirationList) {
                preparedStatement.setString(++parameterIndex, status);
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.isBeforeFirst()) {
                    while (resultSet.next()) {
                        consentIdList.add(resultSet.getString(ConsentMgtDAOConstants.CONSENT_ID));
                    }
                } else {
                    log.debug("No consents found for expiration check eligibility.");
                }
                if (!consentIdList.isEmpty()) {
                    return searchConsents(connection, consentIdList, null, null, null,
                            null, null, null, null, null);
                } else {
                    return new ArrayList<>();
                }

            } catch (SQLException e) {
                log.error("Error occurred while searching consents eligible for expiration", e);
                throw new OBConsentDataRetrievalException("Error occurred while searching consents" +
                        " eligible for expiration", e);
            }
        } catch (SQLException e) {
            log.error("Error while searching consents eligible for expiration", e);
            throw new OBConsentDataRetrievalException("Error while updating searching consents eligible for" +
                    " expiration", e);
        }
    }

    @Override
    public boolean deleteConsentData(Connection connection, String consentID, boolean executeOnRetentionTables)
            throws OBConsentDataDeletionException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Deleting consent details for consent_id : %s", consentID));
        }

        int results;

        String deleteConsentAttributePrepStatement = sqlStatements
                .getDeleteConsentAttributeByConsentIdPreparedStatement(executeOnRetentionTables);
        String deleteConsentFilePrepStatement = sqlStatements
                .getDeleteConsentFileResourcePreparedStatement(executeOnRetentionTables);
        String deleteConsentMappingPrepStatement = sqlStatements
                .getDeleteConsentMappingByAuthIdPreparedStatement(executeOnRetentionTables);
        String deleteConsentAuthResourcePrepStatement = sqlStatements
                .getDeleteAuthorizationResourcePreparedStatement(executeOnRetentionTables);
        String deleteConsentStatusAuditRecordPrepStatement = sqlStatements
                .getDeleteConsentStatusAuditRecordsPreparedStatement(executeOnRetentionTables);
        String deleteConsentResourcePrepStatement = sqlStatements
                .getDeleteConsentPreparedStatement(executeOnRetentionTables);

        try (PreparedStatement deleteConsentAttributesPreparedStmt =
                     connection.prepareStatement(deleteConsentAttributePrepStatement);
             PreparedStatement deleteConsentFilePreparedStmt =
                     connection.prepareStatement(deleteConsentFilePrepStatement);
             PreparedStatement deleteConsentMappingPreparedStmt =
                     connection.prepareStatement(deleteConsentMappingPrepStatement);
             PreparedStatement deleteConsentAuthResourcePreparedStmt =
                     connection.prepareStatement(deleteConsentAuthResourcePrepStatement);
             PreparedStatement deleteConsentStatusAuditPreparedStmt =
                     connection.prepareStatement(deleteConsentStatusAuditRecordPrepStatement);
             PreparedStatement deleteConsentResourcePreparedStmt =
                     connection.prepareStatement(deleteConsentResourcePrepStatement)) {

            // deleting consent attributes.
            log.debug("Setting parameters to prepared statement to delete consent attributes");
            deleteConsentAttributesPreparedStmt.setString(1, consentID);
            deleteConsentAttributesPreparedStmt.executeUpdate();

            // deleting consent file.
            log.debug("Setting parameters to prepared statement to delete consent files");
            deleteConsentFilePreparedStmt.setString(1, consentID);
            deleteConsentFilePreparedStmt.executeUpdate();

            // deleting consent mappings
            log.debug("Setting parameters to prepared statement to delete consent mappings");
            deleteConsentMappingPreparedStmt.setString(1, consentID);
            deleteConsentMappingPreparedStmt.executeUpdate();

            // deleting consent auth resource.
            log.debug("Setting parameters to prepared statement to delete consent auth resource");
            deleteConsentAuthResourcePreparedStmt.setString(1, consentID);
            deleteConsentAuthResourcePreparedStmt.executeUpdate();

            // deleting consent status audit.
            log.debug("Setting parameters to prepared statement to delete consent files");
            deleteConsentStatusAuditPreparedStmt.setString(1, consentID);
            deleteConsentStatusAuditPreparedStmt.executeUpdate();

            // deleting consent resource.
            log.debug("Setting parameters to prepared statement to delete consent resource");
            deleteConsentResourcePreparedStmt.setString(1, consentID);
            results = deleteConsentResourcePreparedStmt.executeUpdate();

            return results > 0;
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_DATA_DELETE_ERROR_MSG, e);
            throw new OBConsentDataDeletionException(ConsentMgtDAOConstants.CONSENT_DATA_DELETE_ERROR_MSG, e);
        }
    }

    @Override
    public ArrayList<String> getListOfConsentIds(Connection connection, boolean fetchFromRetentionTable)
            throws OBConsentDataRetrievalException {

        String getConsentIdsPrepStatement =
                sqlStatements.getListOfConsentIdsPreparedStatement(fetchFromRetentionTable);
        ArrayList<String> consentIDs = new ArrayList<>();

        try (PreparedStatement getConsentIdsPreparedStmt =
                     connection.prepareStatement(getConsentIdsPrepStatement)) {

            try (ResultSet resultSet = getConsentIdsPreparedStmt.executeQuery()) {
                while (resultSet.next()) {
                    consentIDs.add(resultSet.getString(ConsentMgtDAOConstants.CONSENT_ID));
                }
            } catch (SQLException e) {
                log.error("Error occurred while reading consent_id list", e);
                throw new OBConsentDataRetrievalException("Error occurred while retrieving consent consent IDs list",
                        e);
            }

            if (log.isDebugEnabled()) {
                log.debug("Retrieved the consent id list from consent table");
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_RESOURCE_RETRIEVE_ERROR_MSG, e);
            throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.CONSENT_RESOURCE_RETRIEVE_ERROR_MSG, e);
        }
        return consentIDs;
    }

    @Override
    public ArrayList<ConsentStatusAuditRecord> getConsentStatusAuditRecordsByConsentId(Connection connection,
                                                                                       ArrayList<String> consentIDs,
                                                                                       Integer limit, Integer offset,
                                                                                       boolean fetchFromRetentionTable)
            throws OBConsentDataRetrievalException {

        boolean shouldLimit = true;
        boolean shouldOffset = true;
        int parameterIndex = 0;

        // Don't limit if either of limit or offset is null
        if (limit == null) {
            shouldLimit = false;
        }
        if (offset == null) {
            shouldOffset = false;
        }

        ArrayList<ConsentStatusAuditRecord> retrievedAuditRecords = new ArrayList<>();
        String constructedConditions =
                ConsentDAOUtils.constructConsentAuditRecordSearchPreparedStatement(consentIDs);

        String getConsentStatusAuditRecordsPrepStatement =
                sqlStatements.getConsentStatusAuditRecordsByConsentIdsPreparedStatement(constructedConditions,
                        shouldLimit, shouldOffset, fetchFromRetentionTable);

        try (PreparedStatement getConsentStatusAuditRecordPreparedStmt =
                     connection.prepareStatement(getConsentStatusAuditRecordsPrepStatement)) {

            log.debug("Setting parameters to prepared statement to retrieve consent status audit records");
            if (!CollectionUtils.isEmpty(consentIDs)) {
                for (String consentId : consentIDs) {
                    parameterIndex++;
                    getConsentStatusAuditRecordPreparedStmt.setString(parameterIndex, consentId);
                }
            }
            if (limit != null && offset != null) {
                getConsentStatusAuditRecordPreparedStmt.setInt(++parameterIndex,
                        sqlStatements.isLimitBeforeThanOffset() ? limit : offset);
                getConsentStatusAuditRecordPreparedStmt.setInt(++parameterIndex,
                        sqlStatements.isLimitBeforeThanOffset() ? offset : limit);
            } else if (limit != null) {
                getConsentStatusAuditRecordPreparedStmt.setInt(++parameterIndex, limit);
            }

            try (ResultSet resultSet = getConsentStatusAuditRecordPreparedStmt.executeQuery()) {
                if (resultSet.isBeforeFirst()) {
                    while (resultSet.next()) {
                        ConsentStatusAuditRecord consentStatusAuditRecord = new ConsentStatusAuditRecord();
                        consentStatusAuditRecord
                                .setStatusAuditID(resultSet.getString(ConsentMgtDAOConstants.STATUS_AUDIT_ID));
                        consentStatusAuditRecord.setConsentID(resultSet.getString(ConsentMgtDAOConstants.CONSENT_ID));
                        consentStatusAuditRecord
                                .setCurrentStatus(resultSet.getString(ConsentMgtDAOConstants.CURRENT_STATUS));
                        consentStatusAuditRecord.setActionBy(resultSet.getString(ConsentMgtDAOConstants.ACTION_BY));
                        consentStatusAuditRecord.setActionTime(resultSet.getLong(ConsentMgtDAOConstants.ACTION_TIME));
                        consentStatusAuditRecord.setReason(resultSet.getString(ConsentMgtDAOConstants.REASON));
                        consentStatusAuditRecord
                                .setPreviousStatus(resultSet.getString(ConsentMgtDAOConstants.PREVIOUS_STATUS));
                        retrievedAuditRecords.add(consentStatusAuditRecord);
                    }
                }
            } catch (SQLException e) {
                log.error("Error occurred while reading consent status audit records", e);
                throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.AUDIT_RECORDS_RETRIEVE_ERROR_MSG, e);
            }

            log.debug("Retrieved the consent status audit records successfully");

        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.AUDIT_RECORDS_RETRIEVE_ERROR_MSG, e);
            throw new OBConsentDataRetrievalException(ConsentMgtDAOConstants.AUDIT_RECORDS_RETRIEVE_ERROR_MSG, e);
        }
        return retrievedAuditRecords;
    }

    /**
     * Generate the tableID based on the type of the consent data record to be stored in consent history table.
     *
     * @param consentDataType A predefined consent data category based on each consent database table
     * @return A identifier assigned for the relevant consent database table
     */
    private String generateConsentTableId(String consentDataType) throws OBConsentDataInsertionException {

        String tableId;
        if (ConsentMgtDAOConstants.TYPE_CONSENT_BASIC_DATA.equalsIgnoreCase(consentDataType)) {
            tableId = TABLES_MAP.get(ConsentMgtDAOConstants.TABLE_OB_CONSENT);
        } else if (ConsentMgtDAOConstants.TYPE_CONSENT_AUTH_RESOURCE_DATA.equalsIgnoreCase(consentDataType)) {
            tableId = TABLES_MAP.get(ConsentMgtDAOConstants.TABLE_OB_CONSENT_AUTH_RESOURCE);
        } else if (ConsentMgtDAOConstants.TYPE_CONSENT_ATTRIBUTES_DATA.equalsIgnoreCase(consentDataType)) {
            tableId = TABLES_MAP.get(ConsentMgtDAOConstants.TABLE_OB_CONSENT_ATTRIBUTE);
        } else if (ConsentMgtDAOConstants.TYPE_CONSENT_MAPPING_DATA.equalsIgnoreCase(consentDataType)) {
            tableId = TABLES_MAP.get(ConsentMgtDAOConstants.TABLE_OB_CONSENT_MAPPING);
        } else {
            log.error(String.format("Can not find a table matching to the provided consentDataType : %s",
                    consentDataType));
            throw new OBConsentDataInsertionException("Error occurred while preparing to store consent amendment " +
                    "history data. Invalid consentDataType provided");
        }
        return tableId;
    }
}
