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

package org.wso2.financial.services.accelerator.consent.mgt.dao.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.consent.mgt.dao.ConsentCoreDAO;
import org.wso2.financial.services.accelerator.consent.mgt.dao.constants.ConsentMgtDAOConstants;
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
import org.wso2.financial.services.accelerator.consent.mgt.dao.queries.ConsentMgtCommonDBQueries;
import org.wso2.financial.services.accelerator.consent.mgt.dao.util.ConsentManagementDAOUtil;

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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class only implements the data access methods for the consent management accelerator. It implements all the
 * methods defined in the ConsentCoreDAO interface and is only responsible for reading and writing data from/to the
 * database. The incoming data are pre-validated in the upper service layer. Therefore, no validations are done in
 * this layer.
 */
public class ConsentCoreDAOImpl implements ConsentCoreDAO {

    private static final Log log = LogFactory.getLog(ConsentCoreDAOImpl.class);
    private static final String GROUP_BY_SEPARATOR = "\\|\\|";
    ConsentMgtCommonDBQueries sqlStatements;

    public ConsentCoreDAOImpl(ConsentMgtCommonDBQueries sqlStatements) {

        this.sqlStatements = sqlStatements;
    }

    @Override
    public ConsentResource storeConsentResource(Connection connection, ConsentResource consentResource)
            throws
            ConsentDataInsertionException {

        String consentID = StringUtils.isBlank(consentResource.getConsentID()) ? UUID.randomUUID().toString() :
                consentResource.getConsentID();
        // Unix time in seconds
        long currentTimestamp = System.currentTimeMillis() / 1000;
        long createdTime = consentResource.getCreatedTime() == 0 ? currentTimestamp : consentResource.getCreatedTime();
        long updatedTime = consentResource.getUpdatedTime() == 0 ? currentTimestamp : consentResource.getUpdatedTime();

        String storeConsentPrepStatement = sqlStatements.getStoreConsentPreparedStatement();

        try (PreparedStatement storeConsentPreparedStmt = connection.prepareStatement(storeConsentPrepStatement)) {

            log.debug("Setting parameters to prepared statement to store consent resource");
            storeConsentPreparedStmt.setString(1, consentResource.getOrgID());

            storeConsentPreparedStmt.setString(2, consentID);
            storeConsentPreparedStmt.setString(3, consentResource.getReceipt());
            storeConsentPreparedStmt.setLong(4, createdTime);
            storeConsentPreparedStmt.setLong(5, updatedTime);
            storeConsentPreparedStmt.setString(6, consentResource.getClientID());
            storeConsentPreparedStmt.setString(7, consentResource.getConsentType());
            storeConsentPreparedStmt.setString(8, consentResource.getCurrentStatus());
            storeConsentPreparedStmt.setLong(9, consentResource.getConsentFrequency());
            storeConsentPreparedStmt.setLong(10, consentResource.getValidityPeriod());
            storeConsentPreparedStmt.setBoolean(11, consentResource.isRecurringIndicator());

            // with result, we can determine whether the insertion was successful or not
            int result = storeConsentPreparedStmt.executeUpdate();

            // Confirm that the data are inserted successfully
            if (result > 0) {
                log.debug("Stored the consent resource successfully");
                consentResource.setConsentID(consentID);
                consentResource.setCreatedTime(createdTime);
                consentResource.setUpdatedTime(createdTime);
                return consentResource;
            } else {
                log.error(ConsentMgtDAOConstants.FAILED_TO_STORE_ERROR_MSG);
                throw new ConsentDataInsertionException(ConsentMgtDAOConstants.FAILED_TO_STORE_ERROR_MSG);
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_RESOURCE_STORE_ERROR_MSG, e);
            throw new ConsentDataInsertionException(ConsentMgtDAOConstants.CONSENT_RESOURCE_STORE_ERROR_MSG, e);
        }
    }

    @Override
    public ConsentResource getConsentResource(Connection connection, String consentID)
            throws
            ConsentDataRetrievalException {

        String getConsentResourcePrepStatement = sqlStatements.getGetConsentPreparedStatement();

        try (PreparedStatement getConsentResourcePreparedStmt =
                     connection.prepareStatement(getConsentResourcePrepStatement)) {

            log.debug("Setting parameters to prepared statement to retrieve consent resource");

            getConsentResourcePreparedStmt.setString(1, consentID);

            try (ResultSet resultSet = getConsentResourcePreparedStmt.executeQuery()) {
                if (resultSet.next()) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Retrieved the consent resource from FS_CONSENT table for consent ID" +
                                " : %s", consentID.replaceAll("[\r\n]", "")));
                    }
                    return ConsentManagementDAOUtil.setDataToConsentResource(resultSet);
                } else {
                    log.error(String.format("No records are found for consent ID : %S",
                            consentID.replaceAll("[\r\n]", "")));
                    throw new ConsentDataRetrievalException(ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG);
                }
            } catch (SQLException e) {
                String errorMessage = String.format("Error occurred while retrieving consent resource for " +
                        "consent ID : %s", consentID.replaceAll("[\r\n]", ""));
                log.error(errorMessage, e);
                throw new ConsentDataRetrievalException(errorMessage, e);
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_RESOURCE_RETRIEVE_ERROR_MSG, e);
            throw new ConsentDataRetrievalException(ConsentMgtDAOConstants.CONSENT_RESOURCE_RETRIEVE_ERROR_MSG, e);
        }
    }

    @Override
    public ConsentResource getConsentResourceWithAttributes(Connection connection, String consentID)
            throws
            ConsentDataRetrievalException {

        String getConsentResourcePrepStatement = sqlStatements.getGetConsentWithConsentAttributesPreparedStatement();

        try (PreparedStatement getConsentResourcePreparedStmt =
                     connection.prepareStatement(getConsentResourcePrepStatement, ResultSet.TYPE_SCROLL_INSENSITIVE,
                             ResultSet.CONCUR_READ_ONLY)) {

            log.debug("Setting parameters to prepared statement to retrieve consent resource with consent " +
                    "attributes");

            getConsentResourcePreparedStmt.setString(1, consentID);

            try (ResultSet resultSet = getConsentResourcePreparedStmt.executeQuery()) {
                if (resultSet.next()) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Retrieved the consent resource with consent attributes for " +
                                "consent ID : %s ", consentID.replaceAll("[\r\n]", "")));
                    }
                    return ConsentManagementDAOUtil.setDataToConsentResourceWithAttributes(resultSet);
                } else {
                    log.error(String.format("No records are found for consent ID : %s",
                            consentID.replaceAll("[\r\n]", "")));
                    throw new ConsentDataRetrievalException(ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG);
                }
            } catch (SQLException e) {
                String errorMessage = String.format("Error occurred while retrieving consent " +
                        "resource with consent attributes for consent ID : %s", consentID.replaceAll("[\r\n]", ""));
                log.error(errorMessage, e);
                throw new ConsentDataRetrievalException(errorMessage, e);
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_WITH_ATTRIBUTES_RETRIEVE_ERROR_MSG, e);
            throw new ConsentDataRetrievalException(ConsentMgtDAOConstants.CONSENT_WITH_ATTRIBUTES_RETRIEVE_ERROR_MSG,
                    e);
        }
    }

    @Override
    public DetailedConsentResource getDetailedConsentResource(Connection connection, String consentID)
            throws
            ConsentDataRetrievalException {

        String getDetailedConsentResourcePrepStatement = sqlStatements.getGetDetailedConsentPreparedStatement();

        try (PreparedStatement getDetailedConsentResourcePreparedStmt = connection
                .prepareStatement(getDetailedConsentResourcePrepStatement)) {

            log.debug("Setting parameters to prepared statement to retrieve detailed consent resource");

            getDetailedConsentResourcePreparedStmt.setString(1, consentID);

            try (ResultSet resultSet = getDetailedConsentResourcePreparedStmt.executeQuery()) {
                if (resultSet.isBeforeFirst()) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Retrieved the detailed consent resource for consent ID : %s",
                                consentID.replaceAll("[\r\n]", "")));
                    }
                    return ConsentManagementDAOUtil.setDataToDetailedConsentResource(resultSet);
                } else {
                    log.error(String.format("No records are found for consent ID : %s",
                            consentID.replaceAll("[\r\n]", "")));
                    throw new ConsentDataRetrievalException(ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG);
                }
            } catch (SQLException e) {
                log.error("Error occurred while reading detailed consent resource", e);
                throw new ConsentDataRetrievalException(String.format("Error occurred while retrieving " +
                        "detailed consent resource for consent ID : %s", consentID), e);
            }
        } catch (SQLException | JsonProcessingException e) {
            log.error(ConsentMgtDAOConstants.DETAILED_CONSENT_RESOURCE_RETRIEVE_ERROR_MSG, e);
            throw new ConsentDataRetrievalException(ConsentMgtDAOConstants
                    .DETAILED_CONSENT_RESOURCE_RETRIEVE_ERROR_MSG, e);
        }
    }

    @Override
    public DetailedConsentResource getConsentResourceWithAuthorizationResources(Connection connection, String consentID)
            throws
            ConsentDataRetrievalException {

        String getConsentResourceWithAuthorizationResourcesPreparedStatemen = sqlStatements.
                getGetConsentResourceWithAuthorizationResourcesPreparedStatement();

        try (PreparedStatement getConsentResourceWithAuthorizationResourcesPreparedStmt = connection
                .prepareStatement(getConsentResourceWithAuthorizationResourcesPreparedStatemen)) {

            log.debug("Setting parameters to prepared statement to retrieve detailed consent resource");

            getConsentResourceWithAuthorizationResourcesPreparedStmt.setString(1, consentID);

            try (ResultSet resultSet = getConsentResourceWithAuthorizationResourcesPreparedStmt.executeQuery()) {
                if (resultSet.isBeforeFirst()) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Retrieved the detailed consent resource for consent ID : %s",
                                consentID.replaceAll("[\r\n]", "")));
                    }
                    return ConsentManagementDAOUtil.setDataToConsentResourceWithAuthorizationResource(resultSet);
                } else {
                    log.error(String.format("No records are found for consent ID : %s",
                            consentID.replaceAll("[\r\n]", "")));
                    throw new ConsentDataRetrievalException(ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG);
                }
            } catch (SQLException e) {
                log.error("Error occurred while reading detailed consent resource", e);
                throw new ConsentDataRetrievalException(String.format("Error occurred while retrieving " +
                        "detailed consent resource for consent ID : %s", consentID), e);
            }
        } catch (SQLException | JsonProcessingException e) {
            log.error(ConsentMgtDAOConstants.DETAILED_CONSENT_RESOURCE_RETRIEVE_ERROR_MSG, e);
            throw new ConsentDataRetrievalException(ConsentMgtDAOConstants
                    .DETAILED_CONSENT_RESOURCE_RETRIEVE_ERROR_MSG, e);
        }
    }


    @Override
    public void updateConsentStatus(Connection connection, String consentID, String newConsentStatus)
            throws
            ConsentDataUpdationException {

        long updatedTime = System.currentTimeMillis() / 1000;
        String updateConsentStatusPrepStatement = sqlStatements.getUpdateConsentStatusPreparedStatement();

        try (PreparedStatement updateConsentStatusPreparedStmt =
                     connection.prepareStatement(updateConsentStatusPrepStatement)) {

            log.debug("Setting parameters to prepared statement to update consent status");

            updateConsentStatusPreparedStmt.setString(1, newConsentStatus);
            updateConsentStatusPreparedStmt.setLong(2, updatedTime);
            updateConsentStatusPreparedStmt.setString(3, consentID);

            // with result, we can determine whether the updating was successful or not
            int result = updateConsentStatusPreparedStmt.executeUpdate();
            // Confirm that the data are updated successfully
            if (result > 0) {
                log.debug("Updated the consent status successfully");
            } else {
                throw new ConsentDataUpdationException("Failed to update consent status properly.");
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_STATUS_UPDATE_ERROR_MSG, e);
            throw new ConsentDataUpdationException(ConsentMgtDAOConstants.CONSENT_STATUS_UPDATE_ERROR_MSG, e);
        }
    }

    @Override
    public void updateConsentReceipt(Connection connection, String consentID, String consentReceipt)
            throws
            ConsentDataUpdationException {

        String updateConsentReceiptPrepStatement = sqlStatements.getUpdateConsentReceiptPreparedStatement();

        try (PreparedStatement updateConsentReceiptPreparedStmt =
                     connection.prepareStatement(updateConsentReceiptPrepStatement)) {

            log.debug("Setting parameters to prepared statement to update consent receipt");

            updateConsentReceiptPreparedStmt.setString(1, consentReceipt);
            updateConsentReceiptPreparedStmt.setString(2, consentID);

            // with result, we can determine whether the updating was successful or not
            int result = updateConsentReceiptPreparedStmt.executeUpdate();
            // Confirm that the data are updated successfully
            if (result > 0) {
                log.debug("Updated the consent receipt successfully");
            } else {
                throw new ConsentDataUpdationException("Failed to update consent receipt properly.");
            }
        } catch (SQLException e) {
            String errorMessage = String.format("Error while updating consent receipt for consent ID: %s", consentID);
            log.error(errorMessage.replaceAll("[\r\n]", ""), e);
            throw new ConsentDataUpdationException(errorMessage, e);
        }
    }

    @Override
    public void updateConsentValidityTime(Connection connection, String consentID, long validityTime)
            throws
            ConsentDataUpdationException {

        String updateConsentReceiptPrepStatement = sqlStatements.getUpdateConsentValidityTimePreparedStatement();
        long updatedTime = System.currentTimeMillis() / 1000;

        try (PreparedStatement updateConsentValidityTimePreparedStmt =
                     connection.prepareStatement(updateConsentReceiptPrepStatement)) {

            log.debug("Setting parameters to prepared statement to update consent receipt");

            updateConsentValidityTimePreparedStmt.setLong(1, validityTime);
            updateConsentValidityTimePreparedStmt.setLong(2, updatedTime);
            updateConsentValidityTimePreparedStmt.setString(3, consentID);

            // with result, we can determine whether the updating was successful or not
            int result = updateConsentValidityTimePreparedStmt.executeUpdate();
            // Confirm that the data are updated successfully
            if (result > 0) {
                log.debug("Updated the consent validity time successfully");
            } else {
                throw new ConsentDataUpdationException("Failed to update consent validity time properly.");
            }
        } catch (SQLException e) {
            String errorMessage = String.format("Error while updating consent validity time for consent ID: %s",
                    consentID);
            log.error(errorMessage.replaceAll("[\r\n]", ""), e);
            throw new ConsentDataUpdationException(errorMessage, e);
        }
    }

    @Override
    public AuthorizationResource storeAuthorizationResource(Connection connection,
                                                            AuthorizationResource authorizationResource)
            throws
            ConsentDataInsertionException {

        String authorizationID = StringUtils.isEmpty(authorizationResource.getAuthorizationID()) ?
                UUID.randomUUID().toString() : authorizationResource.getAuthorizationID();
        // Unix time in seconds
        long updatedTime = authorizationResource.getUpdatedTime() == 0 ? System.currentTimeMillis() / 1000 :
                authorizationResource.getUpdatedTime();

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
            int result = storeAuthorizationPreparedStmt.executeUpdate();

            // Confirm that the data are inserted successfully
            if (result > 0) {
                log.debug("Stored the authorization resource successfully");
                authorizationResource.setAuthorizationID(authorizationID);
                authorizationResource.setUpdatedTime(updatedTime);
                return authorizationResource;
            } else {
                throw new ConsentDataInsertionException("Failed to store authorization resource data properly.");
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.AUTHORIZATION_RESOURCE_STORE_ERROR_MSG, e);
            throw new ConsentDataInsertionException(ConsentMgtDAOConstants.AUTHORIZATION_RESOURCE_STORE_ERROR_MSG, e);
        }
    }

    @Override
    public AuthorizationResource getAuthorizationResource(Connection connection, String authorizationID, String orgID)
            throws
            ConsentDataRetrievalException {

        String getAuthorizationResourcePrepStatement = sqlStatements.getGetAuthorizationResourcePreparedStatement();

        try (PreparedStatement getConsentResourcePreparedStmt =
                     connection.prepareStatement(getAuthorizationResourcePrepStatement)) {

            log.debug("Setting parameters to prepared statement to retrieve consent authorization resource");

            getConsentResourcePreparedStmt.setString(1, authorizationID);
            getConsentResourcePreparedStmt.setString(2, orgID);

            try (ResultSet resultSet = getConsentResourcePreparedStmt.executeQuery()) {
                if (resultSet.next()) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Retrieved the consent authorization resource for authorization ID" +
                                " : %s", authorizationID.replaceAll("[\r\n]", "")));
                    }
                    return ConsentManagementDAOUtil.setAuthorizationDataWithConsentMapping(resultSet,
                            ConsentMgtDAOConstants.UPDATED_TIME);
                } else {
                    log.error(String.format("No records are found for authorization ID : %s",
                            authorizationID.replaceAll("[\r\n]", "")));
                    throw new ConsentDataRetrievalException(String.format("No records are found for " +
                                    "authorization ID : %s",
                            authorizationID.replaceAll("[\r\n]", "")));
                }
            } catch (SQLException e) {
                String errorMessage = String.format("Error occurred while retrieving consent authorization " +
                        "resource for authorization ID : %s", authorizationID.replaceAll("[\r\n]",
                        ""));
                log.error(errorMessage, e);
                throw new ConsentDataRetrievalException(errorMessage, e);
            }
        } catch (SQLException | JsonProcessingException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_AUTHORIZATION_RESOURCE_RETRIEVE_ERROR_MSG, e);
            throw new ConsentDataRetrievalException(ConsentMgtDAOConstants.
                    CONSENT_AUTHORIZATION_RESOURCE_RETRIEVE_ERROR_MSG, e);
        }
    }

    @Override
    public void updateAuthorizationStatus(Connection connection, String authorizationID, String newAuthorizationStatus)
            throws
            ConsentDataUpdationException {

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
            int result = updateAuthorizationStatusPreparedStmt.executeUpdate();

            // Confirm that the data are updated successfully
            if (result > 0) {
                log.debug("Updated the authorization status successfully");
            } else {
                throw new ConsentDataUpdationException("Failed to update consent status properly.");
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_AUTHORIZATION_STATUS_UPDATE_ERROR_MSG, e);
            throw new ConsentDataUpdationException(ConsentMgtDAOConstants
                    .CONSENT_AUTHORIZATION_STATUS_UPDATE_ERROR_MSG, e);
        }
    }

    @Override
    public void updateAuthorizationUser(Connection connection, String authorizationID, String userID)
            throws
            ConsentDataUpdationException {
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
            int result = updateAuthorizationUserPreparedStmt.executeUpdate();

            // Confirm that the data are updated successfully
            if (result > 0) {
                log.debug("Updated the authorization user successfully");
            } else {
                throw new ConsentDataUpdationException("Failed to update authorization user properly.");
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_AUTHORIZATION_USER_UPDATE_ERROR_MSG, e);
            throw new ConsentDataUpdationException(ConsentMgtDAOConstants.CONSENT_AUTHORIZATION_USER_UPDATE_ERROR_MSG,
                    e);
        }
    }

    @Override
    public ConsentMappingResource storeConsentMappingResource(Connection connection,
                                                              ConsentMappingResource consentMappingResource)
            throws
            ConsentDataInsertionException {

        String consentMappingID = StringUtils.isEmpty(consentMappingResource.getMappingID()) ?
                UUID.randomUUID().toString() : consentMappingResource.getMappingID();

        String storeConsentMappingPrepStatement = sqlStatements.getStoreConsentMappingPreparedStatement();

        try (PreparedStatement storeConsentMappingPreparedStmt =
                     connection.prepareStatement(storeConsentMappingPrepStatement)) {

            log.debug("Setting parameters to prepared statement to store consent mapping resource");

            storeConsentMappingPreparedStmt.setString(1, consentMappingID);
            storeConsentMappingPreparedStmt.setString(2, consentMappingResource.getAuthorizationID());
            storeConsentMappingPreparedStmt.setString(3, consentMappingResource.getResource().toString());
            storeConsentMappingPreparedStmt.setString(4, consentMappingResource.getMappingStatus());


            // with result, we can determine whether the insertion was successful or not
            int result = storeConsentMappingPreparedStmt.executeUpdate();

            // Confirm that the data are inserted successfully
            if (result > 0) {
                log.debug("Stored the consent mapping resource successfully");
                consentMappingResource.setMappingID(consentMappingID);
                return consentMappingResource;
            } else {
                throw new ConsentDataInsertionException("Failed to store consent mapping resource data properly.");
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_MAPPING_RESOURCE_STORE_ERROR_MSG, e);
            throw new ConsentDataInsertionException(ConsentMgtDAOConstants.CONSENT_MAPPING_RESOURCE_STORE_ERROR_MSG,
                    e);
        }
    }

    @Override
    public ArrayList<ConsentMappingResource> getConsentMappingResources(Connection connection, String authorizationID)
            throws
            ConsentDataRetrievalException {

        ArrayList<ConsentMappingResource> retrievedConsentMappingResources = new ArrayList<>();
        String getMappingResourcePrepStatement = sqlStatements.getGetConsentMappingResourcesPreparedStatement();

        try (PreparedStatement getConsentMappingResourcePreparedStmt =
                     connection.prepareStatement(getMappingResourcePrepStatement)) {

            log.debug("Setting parameters to prepared statement to retrieve consent mapping resources");

            getConsentMappingResourcePreparedStmt.setString(1, authorizationID);

            try (ResultSet resultSet = getConsentMappingResourcePreparedStmt.executeQuery()) {
                if (resultSet.isBeforeFirst()) {
                    while (resultSet.next()) {
                        retrievedConsentMappingResources.add(ConsentManagementDAOUtil
                                .getConsentMappingResourceWithData(resultSet));
                    }
                } else {
                    log.debug(String.format("No records are found for authorization ID : %s",
                            authorizationID.replaceAll("[\r\n]", "")));
                }
            } catch (SQLException e) {
                log.error("Error occurred while reading consent mapping resources", e);
                throw new ConsentDataRetrievalException(String.format("Error occurred while retrieving consent " +
                        "mapping resources for authorization ID : %s", authorizationID), e);
            }

            if (log.isDebugEnabled()) {
                log.debug(String.format("Retrieved the consent mapping resources for authorization ID : %s",
                        authorizationID.replaceAll("[\r\n]", "")));
            }
        } catch (SQLException | JsonProcessingException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_MAPPING_RETRIEVE_ERROR_MSG, e);
            throw new ConsentDataRetrievalException(ConsentMgtDAOConstants.CONSENT_MAPPING_RETRIEVE_ERROR_MSG, e);
        }
        return retrievedConsentMappingResources;
    }

    @Override
    public void updateConsentMappingStatus(Connection connection, ArrayList<String> mappingIDs, String mappingStatus)
            throws
            ConsentDataUpdationException {

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
            throw new ConsentDataUpdationException(ConsentMgtDAOConstants.CONSENT_MAPPING_STATUS_UPDATE_ERROR_MSG, e);
        }

        // An empty array or an array with value -3 means the batch execution is failed
        if (result.length != 0 && IntStream.of(result).noneMatch(value -> value == -3)) {
            log.debug("Updated the consent mapping statuses of matching records successfully");
        } else {
            throw new ConsentDataUpdationException("Failed to update consent mapping status properly.");
        }
    }

    @Override
    public boolean storeConsentAttributes(Connection connection, ConsentAttributes consentAttributes)
            throws
            ConsentDataInsertionException {

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
            throw new ConsentDataInsertionException(ConsentMgtDAOConstants.CONSENT_ATTRIBUTES_STORE_ERROR_MSG, e);
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
            throw new ConsentDataInsertionException("Failed to store consent attribute data properly.");
        }
    }

    @Override
    public ConsentAttributes getConsentAttributes(Connection connection, String consentID)
            throws
            ConsentDataRetrievalException {

        Map<String, String> retrievedConsentAttributesMap = new HashMap<>();
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
                } else {
                    log.debug(String.format("No records are found for consent ID : %s",
                            consentID.replaceAll("[\r\n]", "")));
                }
            } catch (SQLException e) {
                log.error("Error occurred while reading consent attributes", e);
                throw new ConsentDataRetrievalException(String.format("Error occurred while retrieving consent " +
                        "attributes for consent ID : %s", consentID), e);
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_ATTRIBUTES_RETRIEVE_ERROR_MSG, e);
            throw new ConsentDataRetrievalException(ConsentMgtDAOConstants.CONSENT_ATTRIBUTES_RETRIEVE_ERROR_MSG, e);
        }

        ConsentAttributes retrievedConsentAttributesResource = new ConsentAttributes();
        retrievedConsentAttributesResource.setConsentID(consentID);
        retrievedConsentAttributesResource.setConsentAttributes(retrievedConsentAttributesMap);
        return retrievedConsentAttributesResource;
    }

    @Override
    public ConsentAttributes getConsentAttributes(Connection connection, String consentID,
                                                  ArrayList<String> consentAttributeKeys)
            throws
            ConsentDataRetrievalException {

        Map<String, String> retrievedConsentAttributesMap = new HashMap<>();
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
                } else {
                    log.error(String.format("No records are found for consent ID : %s and consent attribute keys",
                            consentID.replaceAll("[\r\n]", "")));
                }
            } catch (SQLException e) {
                String errorMessage = String.format("Error occurred while retrieving consent attributes for " +
                        "consent ID : %s and provided consent attributes", consentID.replaceAll("[\r\n]", ""));
                log.error(errorMessage, e);
                throw new ConsentDataRetrievalException(errorMessage, e);
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_ATTRIBUTES_KEYS_RETRIEVE_ERROR_MSG, e);
            throw new ConsentDataRetrievalException(ConsentMgtDAOConstants.CONSENT_ATTRIBUTES_KEYS_RETRIEVE_ERROR_MSG,
                    e);
        }

        ConsentAttributes retrievedConsentAttributesResource = new ConsentAttributes();
        retrievedConsentAttributesResource.setConsentID(consentID);
        retrievedConsentAttributesResource.setConsentAttributes(retrievedConsentAttributesMap);
        return retrievedConsentAttributesResource;
    }

    @Override
    public Map<String, String> getConsentAttributesByName(Connection connection, String attributeName)
            throws
            ConsentDataRetrievalException {

        Map<String, String> retrievedConsentAttributesMap = new HashMap<>();
        String getConsentAttributesByNamePrepStatement = sqlStatements.getGetConsentAttributesByNamePreparedStatement();

        try (PreparedStatement getConsentAttributesByNamePreparedStmt =
                     connection.prepareStatement(getConsentAttributesByNamePrepStatement)) {

            if (log.isDebugEnabled()) {
                log.debug(String.format("Setting parameters to prepared statement to retrieve consent attributes" +
                        " for the provided key: %s", attributeName.replaceAll("[\r\n]", "")));
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
                String errorMessage = String.format("Error occurred while retrieving consent attributes for " +
                        "attribute key: %s", attributeName);
                log.error(errorMessage.replaceAll("[\r\n]", ""), e);
                throw new ConsentDataRetrievalException(errorMessage, e);
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_ATTRIBUTES_RETRIEVE_ERROR_MSG, e);
            throw new ConsentDataRetrievalException(ConsentMgtDAOConstants.CONSENT_ATTRIBUTES_RETRIEVE_ERROR_MSG, e);
        }
        return retrievedConsentAttributesMap;
    }

    @Override
    public ArrayList<String> getConsentIdByConsentAttributeNameAndValue(Connection connection, String attributeName,
                                                                        String attributeValue)
            throws
            ConsentDataRetrievalException {

        ArrayList<String> retrievedConsentIdList = new ArrayList<>();
        String getConsentIdByConsentAttributeNameAndValuePrepStatement = sqlStatements
                .getConsentIdByConsentAttributeNameAndValuePreparedStatement();

        try (PreparedStatement getConsentAttributesByNamePreparedStmt =
                     connection.prepareStatement(getConsentIdByConsentAttributeNameAndValuePrepStatement)) {

            if (log.isDebugEnabled()) {
                log.debug(String.format("Setting parameters to prepared statement to retrieve consent id for the" +
                                " provided key: %s and value: %s", attributeName.replaceAll("[\r\n]", ""),
                        attributeValue.replaceAll("[\r\n]", "")));
            }
            getConsentAttributesByNamePreparedStmt.setString(1, attributeName);
            getConsentAttributesByNamePreparedStmt.setString(2, attributeValue);

            try (ResultSet resultSet = getConsentAttributesByNamePreparedStmt.executeQuery()) {
                if (resultSet.isBeforeFirst()) {
                    while (resultSet.next()) {
                        retrievedConsentIdList.add(resultSet.getString(ConsentMgtDAOConstants.CONSENT_ID));
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("No records are found for the provided attribute key : %s " +
                                        "and value: %s", attributeName.replaceAll("[\r\n]", ""),
                                attributeValue.replaceAll("[\r\n]", "")));
                    }
                }
            } catch (SQLException e) {
                String errorMessage = String.format("Error occurred while retrieving consent " +
                        "attributes for attribute key: %s  and value: %s", attributeName, attributeValue);
                log.error(errorMessage.replaceAll("[\r\n]", ""), e);
                throw new ConsentDataRetrievalException(errorMessage, e);
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_ID_RETRIEVE_ERROR_MSG);
            throw new ConsentDataRetrievalException(ConsentMgtDAOConstants.CONSENT_ID_RETRIEVE_ERROR_MSG, e);
        }
        return retrievedConsentIdList;
    }

    @Override
    public void updateConsentAttributes(Connection connection, String consentID, Map<String, String> consentAttributes)
            throws
            ConsentDataUpdationException {

        int[] result;
        String updateConsentAttributesPrepStatement = sqlStatements.getUpdateConsentAttributesPreparedStatement();

        try (PreparedStatement updateConsentAttributesPreparedStmt =
                     connection.prepareStatement(updateConsentAttributesPrepStatement)) {

            for (Map.Entry<String, String> entry : consentAttributes.entrySet()) {
                updateConsentAttributesPreparedStmt.setString(1, entry.getValue());
                updateConsentAttributesPreparedStmt.setString(2, consentID);
                updateConsentAttributesPreparedStmt.setString(3, entry.getKey());
                updateConsentAttributesPreparedStmt.addBatch();
            }

            // with result, we can determine whether the updating was successful or not
            result = updateConsentAttributesPreparedStmt.executeBatch();
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_ATTRIBUTES_UPDATE_ERROR_MSG, e);
            throw new ConsentDataUpdationException(ConsentMgtDAOConstants.CONSENT_ATTRIBUTES_UPDATE_ERROR_MSG, e);
        }

        /*
           An empty array or an array with value -3 means the batch execution is failed.
           If an array contains value -2, it means the command completed successfully but the number of rows affected
           are unknown. Therefore, only checking for the existence of -3.
        */
        if (result.length != 0 && IntStream.of(result).noneMatch(value -> value == -3)) {
            log.debug("Updated the consent attributes successfully");
        } else {
            throw new ConsentDataUpdationException("Failed to update consent attribute data properly.");
        }
    }

    @Override
    public boolean deleteConsentAttributes(Connection connection, String consentID,
                                           ArrayList<String> consentAttributeKeys)
            throws
            ConsentDataDeletionException {

        int[] result;
        String deleteConsentAttributePrepStatement = sqlStatements.getDeleteConsentAttributePreparedStatement();

        try (PreparedStatement deleteConsentAttributesPreparedStmt =
                     connection.prepareStatement(deleteConsentAttributePrepStatement)) {

            log.debug("Setting parameters to prepared statement to delete the provided consent attributes");

            for (String key : consentAttributeKeys) {
                deleteConsentAttributesPreparedStmt.setString(1, consentID);
                deleteConsentAttributesPreparedStmt.setString(2, key);
                deleteConsentAttributesPreparedStmt.addBatch();
            }

            result = deleteConsentAttributesPreparedStmt.executeBatch();
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_ATTRIBUTES_DELETE_ERROR_MSG, e);
            throw new ConsentDataDeletionException(ConsentMgtDAOConstants.CONSENT_ATTRIBUTES_DELETE_ERROR_MSG, e);
        }

        if (result.length != 0 && IntStream.of(result).noneMatch(value -> value == -3)) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Deleted the consent attribute of key %s",
                        consentAttributeKeys.toString().replaceAll("[\r\n]", "")));
            }
            return true;
        } else {
            throw new ConsentDataDeletionException("Failed to delete consent attribute properly.");
        }
    }

    @Override
    public boolean storeConsentFile(Connection connection, ConsentFile consentFileResource)
            throws
            ConsentDataInsertionException {

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
            throw new ConsentDataInsertionException(ConsentMgtDAOConstants.CONSENT_FILE_STORE_ERROR_MSG, e);
        }

        // Confirm that the data are inserted successfully
        if (result > 0) {
            log.debug("Stored the consent file resource successfully");
            return true;
        } else {
            throw new ConsentDataInsertionException("Failed to store consent file resource data properly.");
        }
    }

    @Override
    public ConsentFile getConsentFile(Connection connection, String consentID)
            throws
            ConsentDataRetrievalException {

        ConsentFile receivedConsentFileResource = new ConsentFile();
        String getConsentFilePrepStatement = sqlStatements.getGetConsentFileResourcePreparedStatement();

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
                    log.error(String.format("No records are found for consent ID : %s",
                            consentID.replaceAll("[\r\n]", "")));
                    throw new ConsentDataRetrievalException(ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG);
                }
            } catch (SQLException e) {
                log.error("Error occurred while reading consent file resource");
                throw new ConsentDataRetrievalException(String.format("Error occurred while retrieving consent file" +
                        " resource for consent ID : %s", consentID.replaceAll("[\r\n]", "")), e);
            }

            if (log.isDebugEnabled()) {
                log.debug(String.format("Retrieved the consent file resource for consent ID : %s",
                        consentID.replaceAll("[\r\n]", "")));
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_FILE_RETRIEVE_ERROR_MSG, e);
            throw new ConsentDataRetrievalException(ConsentMgtDAOConstants.CONSENT_FILE_RETRIEVE_ERROR_MSG, e);
        }
        return receivedConsentFileResource;
    }

    @Override
    @SuppressFBWarnings("SQL_INJECTION_JDBC")
    // Suppressed content - connection.prepareStatement(searchConsentsPreparedStatement,
    //                  ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)
    // Suppression reason - False Positive : Cannot bind variables separately as the query is complex
    // Suppressed warning count - 1


    public ArrayList<DetailedConsentResource> searchConsents(Connection connection,
                                                             String orgID, ArrayList<String> consentIDs,
                                                             ArrayList<String> clientIDs,
                                                             ArrayList<String> consentTypes,
                                                             ArrayList<String> consentStatuses,
                                                             ArrayList<String> userIDs, Long fromTime, Long toTime,
                                                             Integer limit, Integer offset)
            throws
            ConsentDataRetrievalException {

        boolean shouldLimit = true;
        boolean shouldOffset = true;
        int parameterIndex = 0;
        Map<String, ArrayList<String>> applicableConditionsMap = new HashMap<>();


        if (orgID == null) {
            orgID = ConsentMgtDAOConstants.DEFAULT_ORG;
        }
        ArrayList<String> orgIDs = new ArrayList<>();
        orgIDs.add(orgID);
        validateAndSetSearchConditions(orgIDs, applicableConditionsMap, consentIDs, clientIDs, consentTypes,
                consentStatuses);

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
                ConsentManagementDAOUtil.constructConsentSearchPreparedStatement(applicableConditionsMap);

        String userIDFilterCondition = "";
        Map<String, ArrayList<String>> userIdMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(userIDs)) {
            userIdMap.put(ConsentMgtDAOConstants.COLUMNS_MAP.get(ConsentMgtDAOConstants.USER_IDS), userIDs);
            userIDFilterCondition = ConsentManagementDAOUtil.constructUserIdListFilterCondition(userIdMap);
        }

        String searchConsentsPreparedStatement =
                sqlStatements.getSearchConsentsPreparedStatement(constructedConditions, shouldLimit, shouldOffset,
                        userIDFilterCondition);

        try (PreparedStatement searchConsentsPreparedStmt =
                     connection.prepareStatement(searchConsentsPreparedStatement, ResultSet.TYPE_SCROLL_INSENSITIVE,
                             ResultSet.CONCUR_UPDATABLE)) {

            /* Since we don't know the order of the set condition clauses, have to determine the order of them to set
               the actual values to the  prepared statement */
            Map<Integer, ArrayList<String>> orderedParamsMap = ConsentManagementDAOUtil.determineOrderOfParamsToSet(
                    constructedConditions, applicableConditionsMap, ConsentMgtDAOConstants.COLUMNS_MAP);

            log.debug("Setting parameters to prepared statement to search consents");

            parameterIndex = ConsentManagementDAOUtil.setDynamicConsentSearchParameters(searchConsentsPreparedStmt,
                    orderedParamsMap, ++parameterIndex);
            parameterIndex = parameterIndex - 1;

            //determine order of user Ids to set
            if (CollectionUtils.isNotEmpty(userIDs)) {
                Map<Integer, ArrayList<String>> orderedUserIdsMap = ConsentManagementDAOUtil
                        .determineOrderOfParamsToSet(userIDFilterCondition, userIdMap,
                                ConsentMgtDAOConstants.COLUMNS_MAP);
                parameterIndex = ConsentManagementDAOUtil.setDynamicConsentSearchParameters(searchConsentsPreparedStmt,
                        orderedUserIdsMap, ++parameterIndex);
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
                    int resultSetSize = ConsentManagementDAOUtil.getResultSetSize(resultSet);
                    detailedConsentResources = constructDetailedConsentsSearchResult(resultSet, resultSetSize);
                }
                return detailedConsentResources;
            } catch (SQLException e) {
                log.error("Error occurred while searching detailed consent resources", e);
                throw new ConsentDataRetrievalException("Error occurred while searching detailed " +
                        "consent resources", e);
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_SEARCH_ERROR_MSG, e);
            throw new ConsentDataRetrievalException(ConsentMgtDAOConstants.CONSENT_SEARCH_ERROR_MSG);
        }
    }

    @Override
    @SuppressFBWarnings("SQL_INJECTION_JDBC")
    // Suppressed content - connection.prepareStatement(searchAuthorizationResourcesPrepStatement)
    // Suppression reason - False Positive : Cannot bind variables separately as the query is complex
    // Suppressed warning count - 1
    public ArrayList<AuthorizationResource> searchConsentAuthorizations(Connection connection, String consentID,
                                                                        String userID)
            throws
            ConsentDataRetrievalException {

        ArrayList<AuthorizationResource> retrievedAuthorizationResources = new ArrayList<>();
        Map<String, String> conditions = new HashMap<>();
        if (StringUtils.trimToNull(consentID) != null) {
            conditions.put("CONSENT_ID", consentID);
        }
        if (StringUtils.trimToNull(userID) != null) {
            conditions.put("USER_ID", userID);
        }
        String whereClause = ConsentManagementDAOUtil.constructAuthSearchPreparedStatement(conditions);
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
                    throw new ConsentDataRetrievalException(ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG);
                }
            } catch (SQLException e) {
                log.error("Error occurred while searching authorization resources", e);
                throw new ConsentDataRetrievalException(ConsentMgtDAOConstants
                        .CONSENT_AUTHORIZATION_RESOURCE_RETRIEVE_ERROR_MSG, e);
            }
            log.debug("Retrieved the authorization resources successfully");
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_AUTHORIZATION_RESOURCE_RETRIEVE_ERROR_MSG, e);
            throw new ConsentDataRetrievalException(ConsentMgtDAOConstants
                    .CONSENT_AUTHORIZATION_RESOURCE_RETRIEVE_ERROR_MSG, e);
        }
        return retrievedAuthorizationResources;
    }

    @Override
    public ConsentStatusAuditRecord storeConsentStatusAuditRecord(Connection connection,
                                                                  ConsentStatusAuditRecord consentStatusAuditRecord)
            throws
            ConsentDataInsertionException {

        int result;
        String statusAuditID = StringUtils.isEmpty(consentStatusAuditRecord.getStatusAuditID()) ?
                UUID.randomUUID().toString() : consentStatusAuditRecord.getStatusAuditID();
        // Unix time in seconds
        long actionTime = (consentStatusAuditRecord.getActionTime() == 0) ? System.currentTimeMillis() :
                consentStatusAuditRecord.getActionTime();

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
            throw new ConsentDataInsertionException(ConsentMgtDAOConstants.AUDIT_RECORD_STORE_ERROR_MSG, e);
        }

        // Confirm that the data are inserted successfully
        if (result > 0) {
            log.debug("Stored the consent status audit record successfully");
            consentStatusAuditRecord.setStatusAuditID(statusAuditID);
            consentStatusAuditRecord.setActionTime(actionTime);
            return consentStatusAuditRecord;
        } else {
            throw new ConsentDataInsertionException("Failed to store consent status audit record data properly.");
        }
    }

    @Override
    public ArrayList<ConsentStatusAuditRecord> getConsentStatusAuditRecords(Connection connection, String consentID,
                                                                            String currentStatus, String actionBy,
                                                                            Long fromTime, Long toTime,
                                                                            String statusAuditID)
            throws
            ConsentDataRetrievalException {

        ArrayList<ConsentStatusAuditRecord> retrievedAuditRecords = new ArrayList<>();
        String getConsentStatusAuditRecordsPrepStatement =
                sqlStatements.getGetConsentStatusAuditRecordsPreparedStatement();

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
                    throw new ConsentDataRetrievalException(ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG);
                }
            } catch (SQLException e) {
                log.error("Error occurred while reading consent status audit records", e);
                throw new ConsentDataRetrievalException(ConsentMgtDAOConstants.AUDIT_RECORDS_RETRIEVE_ERROR_MSG, e);
            }

            log.debug("Retrieved the consent status audit records successfully");

        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.AUDIT_RECORDS_RETRIEVE_ERROR_MSG, e);
            throw new ConsentDataRetrievalException(ConsentMgtDAOConstants.AUDIT_RECORDS_RETRIEVE_ERROR_MSG, e);
        }
        return retrievedAuditRecords;
    }

    @Override
    @SuppressFBWarnings("SQL_INJECTION_JDBC")
    // Suppressed content - connection.prepareStatement(getConsentStatusAuditRecordsPrepStatement)
    // Suppression reason - False Positive : Cannot bind variables separately as the query is complex
    // Suppressed warning count - 1
    public ArrayList<ConsentStatusAuditRecord> getConsentStatusAuditRecordsByConsentId(Connection connection,
                                                                                       ArrayList<String> consentIDs,
                                                                                       Integer limit, Integer offset)
            throws
            ConsentDataRetrievalException {

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
                ConsentManagementDAOUtil.constructConsentAuditRecordSearchPreparedStatement(consentIDs);

        String getConsentStatusAuditRecordsPrepStatement =
                sqlStatements.getConsentStatusAuditRecordsByConsentIdsPreparedStatement(constructedConditions,
                        shouldLimit, shouldOffset);

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
                throw new ConsentDataRetrievalException(ConsentMgtDAOConstants.AUDIT_RECORDS_RETRIEVE_ERROR_MSG, e);
            }

            log.debug("Retrieved the consent status audit records successfully");

        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.AUDIT_RECORDS_RETRIEVE_ERROR_MSG, e);
            throw new ConsentDataRetrievalException(ConsentMgtDAOConstants.AUDIT_RECORDS_RETRIEVE_ERROR_MSG, e);
        }
        return retrievedAuditRecords;
    }

    @Override
    public boolean storeConsentAmendmentHistory(Connection connection, String historyID, long timestamp,
                                                String statusAuditRecordID, String recordID, String consentDataType,
                                                String changedAttributesJsonString, String amendmentReason)
            throws
            ConsentDataInsertionException {

        String tableID = ConsentManagementDAOUtil.generateConsentTableId(consentDataType.replaceAll("[\r\n]", ""));


        int result;
        String insertConsentHistoryPrepStatement = sqlStatements.getInsertConsentHistoryPreparedStatement();

        try (PreparedStatement insertConsentHistoryPreparedStmt =
                     connection.prepareStatement(insertConsentHistoryPrepStatement)) {

            if (log.isDebugEnabled()) {
                log.debug(String.format("Setting parameters to prepared statement to store consent amendment history " +
                        "of %s", consentDataType.replaceAll("[\r\n]", "")));
            }

            insertConsentHistoryPreparedStmt.setString(1, tableID);
            insertConsentHistoryPreparedStmt.setString(2, statusAuditRecordID);
            insertConsentHistoryPreparedStmt.setString(3, recordID);
            insertConsentHistoryPreparedStmt.setString(4, historyID);
            insertConsentHistoryPreparedStmt.setString(5, changedAttributesJsonString);
            insertConsentHistoryPreparedStmt.setString(6, amendmentReason);
            insertConsentHistoryPreparedStmt.setLong(7, timestamp);

            // with result, we can determine whether the updating was successful or not
            result = insertConsentHistoryPreparedStmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error while storing consent amendment history", e);
            throw new ConsentDataInsertionException(String.format("Error while storing consent amendment history of" +
                    " %s for record ID: %s", consentDataType, recordID), e);
        }

        // Confirm that the data are inserted successfully
        if (result > 0) {
            return true;
        } else {
            log.error("Failed to store consent amendment history data.");
            throw new ConsentDataInsertionException("Failed to store consent amendment history data properly.");
        }
    }


    @Override
    @SuppressFBWarnings("SQL_INJECTION_JDBC")
    // Suppressed content - connection.prepareStatement(getConsentHistoryPrepStatement)
    // Suppression reason - False Positive : Cannot bind variables separately as the query is complex
    // Suppressed warning count - 1
    public Map<String, ConsentHistoryResource> retrieveConsentAmendmentHistory(Connection connection,
                                                                               List<String> recordIDsList,
                                                                               String consentID) throws
            ConsentDataRetrievalException {

        String whereClause = ConsentManagementDAOUtil.constructConsentHistoryPreparedStatement(recordIDsList.size());
        String getConsentHistoryPrepStatement = sqlStatements.getGetConsentHistoryPreparedStatement(whereClause);

        try (PreparedStatement getConsentHistoryPreparedStmt =
                     connection.prepareStatement(getConsentHistoryPrepStatement)) {

            log.debug("Setting parameters to prepared statement to retrieve consent history data");

            for (int count = 1; count <= recordIDsList.size(); count++) {
                getConsentHistoryPreparedStmt.setString(count, recordIDsList.get(count - 1));
            }


            try (ResultSet resultSet = getConsentHistoryPreparedStmt.executeQuery()) {
                if (resultSet.isBeforeFirst()) {
                    return ConsentManagementDAOUtil.constructConsentHistoryRetrievalResult(consentID, resultSet);
                } else {
                    log.error(String.format("No records are found for consent ID : %s",
                            consentID.replaceAll("[\r\n]", "")));
                    return new HashMap<>();
                }
            } catch (SQLException e) {
                log.error("Error occurred while reading consent amendment history", e);
                throw new ConsentDataRetrievalException(String.format("Error occurred while retrieving consent " +
                        "amendment history for consent ID : %s", consentID), e);
            }
        } catch (SQLException e) {
            log.error(ConsentMgtDAOConstants.CONSENT_AMENDMENT_HISTORY_RETRIEVE_ERROR_MSG, e);
            throw new ConsentDataRetrievalException(
                    ConsentMgtDAOConstants.CONSENT_AMENDMENT_HISTORY_RETRIEVE_ERROR_MSG, e);
        }
    }

    @Override
    @SuppressFBWarnings("SQL_INJECTION_JDBC")
    // Suppressed content - connection.prepareStatement(expiringConsentStatement)
    // Suppression reason - False Positive : Cannot bind variables separately as the query is complex
    // Suppressed warning count - 1
    public ArrayList<DetailedConsentResource> getExpiringConsents(Connection connection, String orgID,
                                                                  String statusesEligibleForExpiration)
            throws
            ConsentDataRetrievalException {

        List<String> statusesEligibleForExpirationList = Arrays.stream(statusesEligibleForExpiration
                        .split(",")).filter(status -> !status.isEmpty())
                .collect(Collectors.toList());

        String statusesEligibleForExpirationCondition = ConsentManagementDAOUtil
                .constructStatusesEligibleForExpirationCondition(statusesEligibleForExpirationList);
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
                    return searchConsents(connection, null, consentIdList, null, null, null,
                            null, null, null, null, null);
                } else {
                    return new ArrayList<>();
                }

            } catch (SQLException e) {
                log.error("Error occurred while searching consents eligible for expiration", e);
                throw new ConsentDataRetrievalException("Error occurred while searching consents" +
                        " eligible for expiration", e);
            }
        } catch (SQLException e) {
            log.error("Error while searching consents eligible for expiration", e);
            throw new ConsentDataRetrievalException("Error while updating searching consents eligible for" +
                    " expiration", e);
        }
    }

    @Override
    public void deleteConsent(Connection connection, String consentID) {

        List<String> deleteStatements = sqlStatements.getDeleteConsentCascadeStatements();

        try {
            connection.setAutoCommit(false); // Begin transaction

            for (String query : deleteStatements) {
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setString(1, consentID);
                    int result = ps.executeUpdate();
                    log.debug("Executed delete query, affected rows: " + result);
                }
            }

            connection.commit();
            log.debug(String.format("Successfully deleted consent and related records for ID: %s",
                    consentID.replaceAll("[\r\n]", "")));

        } catch (SQLException e) {
            try {
                connection.rollback();
                log.error("Transaction rolled back due to error while deleting consent", e);
            } catch (SQLException rollbackEx) {
                log.error("Error during rollback after failed consent deletion", rollbackEx);
            }
        } finally {
            try {
                connection.setAutoCommit(true); // Reset autocommit
            } catch (SQLException e) {
                log.error("Failed to reset autocommit to true", e);
            }
        }
    }

    void validateAndSetSearchConditions(ArrayList<String> orgIDs,
                                        Map<String, ArrayList<String>> applicableConditionsMap,
                                        ArrayList<String> consentIDs, ArrayList<String> clientIDs,
                                        ArrayList<String> consentTypes, ArrayList<String> consentStatuses) {

        log.debug("Validate applicable search conditions");

        if (CollectionUtils.isNotEmpty(consentIDs)) {
            applicableConditionsMap.put(ConsentMgtDAOConstants.COLUMNS_MAP.get(ConsentMgtDAOConstants.CONSENT_IDS),
                    consentIDs);
        }
        if (CollectionUtils.isNotEmpty(clientIDs)) {
            applicableConditionsMap.put(ConsentMgtDAOConstants.COLUMNS_MAP.get(ConsentMgtDAOConstants.CLIENT_IDS),
                    clientIDs);
        }
        if (CollectionUtils.isNotEmpty(consentTypes)) {
            applicableConditionsMap.put(ConsentMgtDAOConstants.COLUMNS_MAP.get(ConsentMgtDAOConstants.CONSENT_TYPES),
                    consentTypes);
        }
        if (CollectionUtils.isNotEmpty(consentStatuses)) {
            applicableConditionsMap.put(ConsentMgtDAOConstants.COLUMNS_MAP.get(ConsentMgtDAOConstants.CONSENT_STATUSES),
                    consentStatuses);
        }
        if (CollectionUtils.isNotEmpty(orgIDs)) {
            applicableConditionsMap.put(ConsentMgtDAOConstants.COLUMNS_MAP.get(ConsentMgtDAOConstants.ORG_ID),
                    orgIDs);
        }
    }

    ArrayList<DetailedConsentResource> constructDetailedConsentsSearchResult(ResultSet resultSet, int resultSetSize)
            throws
            SQLException {

        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();

        while (resultSet.next()) {

            Map<String, String> consentAttributesMap = new HashMap<>();
            ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();
            ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
            DetailedConsentResource detailedConsentResource = ConsentManagementDAOUtil
                    .setConsentDataToDetailedConsentResource(resultSet);

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

    protected void setAuthorizationDataInResponseForGroupedQuery(ArrayList<AuthorizationResource>
                                                                         authorizationResources,
                                                                 ResultSet resultSet, String consentId)
            throws
            SQLException {

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
                                                          ResultSet resultSet) throws
            SQLException {

        //identify duplicate mappingIds
        Set<String> mappingIdSet = new HashSet<>();

        // fetch values from group_concat
        String[] authIds = resultSet.getString(ConsentMgtDAOConstants.AUTH_MAPPING_ID) != null ?
                resultSet.getString(ConsentMgtDAOConstants.AUTH_MAPPING_ID).split(GROUP_BY_SEPARATOR) : null;
        String[] mappingIds = resultSet.getString(ConsentMgtDAOConstants.MAPPING_ID) != null ?
                resultSet.getString(ConsentMgtDAOConstants.MAPPING_ID).split(GROUP_BY_SEPARATOR) : null;

        String[] mappingStatues = resultSet.getString(ConsentMgtDAOConstants.MAPPING_STATUS) != null ?
                resultSet.getString(ConsentMgtDAOConstants.MAPPING_STATUS).split(GROUP_BY_SEPARATOR) : null;
        String[] resources = resultSet.getString(ConsentMgtDAOConstants.RESOURCE) != null ?
                resultSet.getString(ConsentMgtDAOConstants.RESOURCE).split(GROUP_BY_SEPARATOR) : null;

        for (int index = 0; index < (mappingIds != null ? mappingIds.length : 0); index++) {
            if (!mappingIdSet.contains(mappingIds[index])) {
                ConsentMappingResource consentMappingResource = new ConsentMappingResource();
                if (authIds != null && authIds.length > index) {
                    consentMappingResource.setAuthorizationID(authIds[index]);
                }
                consentMappingResource.setMappingID(mappingIds[index]);

                if (mappingStatues != null && mappingStatues.length > index) {
                    consentMappingResource.setMappingStatus(mappingStatues[index]);
                }
                if (resources != null && resources.length > index) {
                    consentMappingResource.setPermission(resources[index]);
                }
                consentMappingResources.add(consentMappingResource);
                mappingIdSet.add(mappingIds[index]);
            }
        }
    }
}
