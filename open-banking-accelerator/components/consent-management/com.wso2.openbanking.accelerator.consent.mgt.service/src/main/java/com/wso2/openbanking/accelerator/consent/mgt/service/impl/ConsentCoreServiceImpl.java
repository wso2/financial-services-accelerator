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

package com.wso2.openbanking.accelerator.consent.mgt.service.impl;

import com.google.gson.Gson;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.DatabaseUtil;
import com.wso2.openbanking.accelerator.common.util.Generated;
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
import com.wso2.openbanking.accelerator.consent.mgt.dao.persistence.ConsentStoreInitializer;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.constants.ConsentCoreServiceConstants;
import com.wso2.openbanking.accelerator.consent.mgt.service.internal.ConsentManagementDataHolder;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.OAuth2Service;
import org.wso2.carbon.identity.oauth2.bean.OAuthClientAuthnContext;
import org.wso2.carbon.identity.oauth2.dao.OAuthTokenPersistenceFactory;
import org.wso2.carbon.identity.oauth2.dto.OAuthRevocationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuthRevocationResponseDTO;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Consent core service implementation.
 */
public class ConsentCoreServiceImpl implements ConsentCoreService {

    private static final Log log = LogFactory.getLog(ConsentCoreServiceImpl.class);

    @Override
    public DetailedConsentResource createAuthorizableConsent(ConsentResource consentResource, String userID,
                                                             String authStatus, String authType,
                                                             boolean isImplicitAuth)
            throws ConsentManagementException {

        if (StringUtils.isBlank(consentResource.getClientID()) || StringUtils.isBlank(consentResource.getReceipt()) ||
                StringUtils.isBlank(consentResource.getConsentType()) ||
                StringUtils.isBlank(consentResource.getCurrentStatus())) {

            log.error("Client ID, receipt, consent type or consent status is missing, cannot proceed");
            throw new ConsentManagementException("Cannot proceed since client ID, receipt, consent type or consent " +
                    "status is missing.");
        }

        if (isImplicitAuth) {
            if (StringUtils.isBlank(authStatus) || StringUtils.isBlank(authType)) {
                log.error("Authorization status and authorization type is not found for implicit " +
                        "authorization creation");
                throw new ConsentManagementException("Cannot proceed with implicit authorization creation " +
                        "without Authorization Status and Authorization Type provided");
            }
        }

        Connection connection = DatabaseUtil.getDBConnection();

        try {
            try {
                ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
                DetailedConsentResource detailedConsentResource = createAuthorizableConesntWithAuditRecord(connection,
                        consentCoreDAO, consentResource, userID, authStatus, authType, isImplicitAuth);
                DatabaseUtil.commitTransaction(connection);
                return detailedConsentResource;
            } catch (OBConsentDataInsertionException e) {
                log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
                DatabaseUtil.rollbackTransaction(connection);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public DetailedConsentResource createExclusiveConsent(ConsentResource consentResource, String userID,
                                                          String authStatus, String authType,
                                                          String applicableExistingConsentsStatus,
                                                          String newExistingConsentStatus,
                                                          boolean isImplicitAuth)
            throws ConsentManagementException {

        if (StringUtils.isBlank(consentResource.getClientID()) || StringUtils.isBlank(consentResource.getReceipt()) ||
                StringUtils.isBlank(consentResource.getConsentType()) ||
                StringUtils.isBlank(consentResource.getCurrentStatus()) || StringUtils.isBlank(userID)
                || StringUtils.isBlank(applicableExistingConsentsStatus)
                || StringUtils.isBlank(newExistingConsentStatus)) {

            log.error(ConsentCoreServiceConstants.CREATE_EXCLUSIVE_CONSENT_MANDATORY_PARAMETER_MISSING_ERROR);
            throw new ConsentManagementException(ConsentCoreServiceConstants
                    .CREATE_EXCLUSIVE_CONSENT_MANDATORY_PARAMETER_MISSING_ERROR);
        }

        if (isImplicitAuth) {
            if (StringUtils.isBlank(authStatus) || StringUtils.isBlank(authType)) {
                log.error("Authorization status and authorization type is not found for implicit " +
                        "authorization creation");
                throw new ConsentManagementException("Cannot proceed with implicit authorization creation " +
                        "without Authorization Status and Authorization Type provided");
            }
        }

        Connection connection = DatabaseUtil.getDBConnection();

        try {
            try {
                ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();

                // Update existing consent statuses and revoke their account mappings
                updateExistingConsentStatusesAndRevokeAccountMappings(connection, consentCoreDAO, consentResource,
                        userID, applicableExistingConsentsStatus, newExistingConsentStatus);

                // Create a new consent, audit record and authorization resource if allowed
                DetailedConsentResource storedDetailedConsentResource =
                        createAuthorizableConesntWithAuditRecord(connection, consentCoreDAO, consentResource, userID,
                                authStatus, authType, isImplicitAuth);

                // Commit the transaction
                DatabaseUtil.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return storedDetailedConsentResource;
            } catch (OBConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG);
            } catch (OBConsentDataInsertionException e) {
                log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
                DatabaseUtil.rollbackTransaction(connection);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
            } catch (OBConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtil.rollbackTransaction(connection);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public boolean createConsentFile(ConsentFile consentFileResource, String newConsentStatus, String userID,
                                     String applicableStatusToFileUpload)
            throws ConsentManagementException {

        if (StringUtils.isBlank(consentFileResource.getConsentID()) ||
                StringUtils.isBlank(consentFileResource.getConsentFile())) {

            log.error("Consent ID or Consent File content is missing. Cannot proceed.");
            throw new ConsentManagementException("Cannot proceed without consent ID and file content.");
        }

        String consentID = consentFileResource.getConsentID();

        if (StringUtils.isBlank(newConsentStatus) || StringUtils.isBlank(applicableStatusToFileUpload)) {
            log.error(ConsentCoreServiceConstants.NEW_CONSENT_STATUS_OR_APPLICABLE_STATUS_MISSING_ERROR);
            throw new ConsentManagementException(ConsentCoreServiceConstants
                    .NEW_CONSENT_STATUS_OR_APPLICABLE_STATUS_MISSING_ERROR);
        }

        Connection connection = DatabaseUtil.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                // Get the existing consent to validate status
                if (log.isDebugEnabled()) {
                    log.debug("Retrieving the consent for ID:" + consentID.replaceAll("[\r\n]", "") +
                            " to validate status");
                }
                ConsentResource existingConsentResource = consentCoreDAO.getConsentResource(connection, consentID);

                String existingConsentStatus = existingConsentResource.getCurrentStatus();

                // Validate status of the consent
                if (!applicableStatusToFileUpload.equalsIgnoreCase(existingConsentResource.getCurrentStatus())) {
                    log.error("The consent is not in required state to proceed");
                    throw new ConsentManagementException("The consent should be in the required state in order to " +
                            "proceed");
                }

                // Store the consent file
                if (log.isDebugEnabled()) {
                    log.debug("Creating the consent file for the consent of ID:" +
                            consentID.replaceAll("[\r\n]", ""));
                }
                consentCoreDAO.storeConsentFile(connection, consentFileResource);

                // Update consent status with new status
                if (log.isDebugEnabled()) {
                    log.debug("Updating the status of the consent for ID:" + consentID.replaceAll("[\r\n]", ""));
                }
                consentCoreDAO.updateConsentStatus(connection, consentID, newConsentStatus);

                // Create audit record and execute state change listener
                HashMap<String, Object> consentDataMap = new HashMap<>();
                consentDataMap.put(ConsentCoreServiceConstants.CONSENT_RESOURCE, existingConsentResource);
                postStateChange(connection, consentCoreDAO, consentID, userID, newConsentStatus,
                        existingConsentStatus, ConsentCoreServiceConstants.CONSENT_FILE_UPLOAD_REASON,
                        existingConsentResource.getClientID(), consentDataMap);

                // Commit transaction
                DatabaseUtil.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return true;
            } catch (OBConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            } catch (OBConsentDataInsertionException e) {
                log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
                DatabaseUtil.rollbackTransaction(connection);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
            } catch (OBConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtil.rollbackTransaction(connection);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public boolean revokeConsent(String consentID, String revokedConsentStatus)
            throws ConsentManagementException {
        return revokeConsentWithReason(consentID, revokedConsentStatus, null, true,
                ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);
    }

    @Override
    public boolean revokeConsentWithReason(String consentID, String revokedConsentStatus, String revokedReason)
            throws ConsentManagementException {
        return revokeConsentWithReason(consentID, revokedConsentStatus, null, true, revokedReason);
    }

    @Override
    public boolean revokeConsent(String consentID, String revokedConsentStatus, String userID)
            throws ConsentManagementException {
        return revokeConsentWithReason(consentID, revokedConsentStatus, userID, true,
                ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);
    }

    @Override
    public boolean revokeConsentWithReason(String consentID, String revokedConsentStatus, String userID,
                                           String revokedReason)
            throws ConsentManagementException {
        return revokeConsentWithReason(consentID, revokedConsentStatus, userID, true, revokedReason);
    }

    @Override
    public boolean revokeConsent(String consentID, String revokedConsentStatus, String userID,
                                 boolean shouldRevokeTokens)
            throws ConsentManagementException {
        return revokeConsentWithReason(consentID, revokedConsentStatus, userID, shouldRevokeTokens,
                ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);
    }


    @Override
    public boolean revokeConsentWithReason(String consentID, String revokedConsentStatus, String userID,
                                           boolean shouldRevokeTokens, String revokedReason)
            throws ConsentManagementException {

        if (StringUtils.isBlank(consentID) || StringUtils.isBlank(revokedConsentStatus)) {
            log.error("Consent ID or new consent status is missing, cannot proceed");
            throw new ConsentManagementException("Consent ID or new consent status is missing, cannot " +
                    "proceed");
        }

        Connection connection = DatabaseUtil.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                // Get existing detailed consent
                if (log.isDebugEnabled()) {
                    log.debug("Retrieving existing consent of ID: " + consentID.replaceAll("[\r\n]", "") +
                            " for status validation");
                }
                DetailedConsentResource retrievedDetailedConsentResource = consentCoreDAO
                        .getDetailedConsentResource(connection, consentID, false);
                String previousConsentStatus = retrievedDetailedConsentResource.getCurrentStatus();

                // Update consent status as revoked
                if (log.isDebugEnabled()) {
                    log.debug("Updating the status of the consent of ID: " + consentID.replaceAll("[\r\n]", ""));
                }
                consentCoreDAO.updateConsentStatus(connection, consentID, revokedConsentStatus);

                if (shouldRevokeTokens) {
                    // Extract userId from authorizationResources
                    ArrayList<AuthorizationResource> authorizationResources = retrievedDetailedConsentResource
                            .getAuthorizationResources();

                    String consentUserID = "";
                    if (authorizationResources != null && !authorizationResources.isEmpty()) {
                        consentUserID = authorizationResources.get(0).getUserID();
                    }

                    if (StringUtils.isBlank(consentUserID)) {
                        log.error("User ID is required for token revocation, cannot proceed");
                        throw new ConsentManagementException("User ID is required for token revocation, cannot " +
                                "proceed");
                    }

                    if (!isValidUserID(userID, consentUserID)) {
                        final String errorMsg = "Requested UserID and Consent UserID do not match, cannot proceed.";
                        log.error(errorMsg + ", request UserID: " + userID.replaceAll("[\r\n]", "") +
                                ", Consent UserID: " + consentUserID.replaceAll("[\r\n]", ""));
                        throw new ConsentManagementException(errorMsg);
                    }
                    revokeTokens(retrievedDetailedConsentResource, consentUserID);
                }

                ArrayList<ConsentMappingResource> consentMappingResources = retrievedDetailedConsentResource
                        .getConsentMappingResources();
                ArrayList<String> mappingIDs = new ArrayList<>();

                if (!consentMappingResources.isEmpty()) {
                    for (ConsentMappingResource resource : consentMappingResources) {
                        mappingIDs.add(resource.getMappingID());
                    }

                    // Update account mapping status as inactive
                    if (log.isDebugEnabled()) {
                        log.debug("Updating the account mappings of consent ID: " +
                                consentID.replaceAll("[\r\n]", "") + " as inactive");
                    }
                    consentCoreDAO.updateConsentMappingStatus(connection, mappingIDs,
                            ConsentCoreServiceConstants.INACTIVE_MAPPING_STATUS);
                }

                HashMap<String, Object> consentDataMap = new HashMap<>();
                // Get detailed consent status after the updates
                DetailedConsentResource newDetailedConsentResource =
                        consentCoreDAO.getDetailedConsentResource(connection, consentID, false);
                consentDataMap.put(ConsentCoreServiceConstants.DETAILED_CONSENT_RESOURCE,
                        newDetailedConsentResource);

                // Pass the previous status consent to persist as consent history
                consentDataMap.put(ConsentCoreServiceConstants.CONSENT_AMENDMENT_HISTORY_RESOURCE,
                        retrievedDetailedConsentResource);
                consentDataMap.put(ConsentCoreServiceConstants.CONSENT_AMENDMENT_TIME, System.currentTimeMillis());

                // Create an audit record execute state change listener
                postStateChange(connection, consentCoreDAO, consentID, userID, revokedConsentStatus,
                        previousConsentStatus, revokedReason,
                        retrievedDetailedConsentResource.getClientID(), consentDataMap);

                //Commit transaction
                DatabaseUtil.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
            } catch (OBConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            } catch (OBConsentDataInsertionException e) {
                log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
                DatabaseUtil.rollbackTransaction(connection);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
            } catch (OBConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtil.rollbackTransaction(connection);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            } catch (IdentityOAuth2Exception e) {
                log.error("Error while revoking tokens for the consent ID: "
                        + consentID.replaceAll("[\r\n]", ""), e);
                throw new ConsentManagementException("Error occurred while revoking tokens for the consent ID: "
                        + consentID);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
        return true;
    }

    @Override
    public boolean revokeExistingApplicableConsents(String clientID, String userID, String consentType,
                                                    String applicableStatusToRevoke,
                                                    String revokedConsentStatus, boolean shouldRevokeTokens)
            throws ConsentManagementException {

        if (StringUtils.isBlank(clientID) || StringUtils.isBlank(revokedConsentStatus) || StringUtils.isBlank(userID)
                || StringUtils.isBlank(applicableStatusToRevoke) || StringUtils.isBlank(consentType)) {
            log.error("Client ID, new consent status, consent type, user ID or applicable consent status to revoke is" +
                    " missing, cannot proceed");
            throw new ConsentManagementException("Client ID, new consent status, consent type, user ID or applicable " +
                    "consent status to revoke is missing, cannot proceed");
        }

        Connection connection = DatabaseUtil.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {

                ArrayList<String> accountMappingIDsList = new ArrayList<>();
                ArrayList<String> clientIDsList = new ArrayList<>();
                clientIDsList.add(clientID);
                ArrayList<String> userIDsList = new ArrayList<>();
                userIDsList.add(userID);
                ArrayList<String> consentTypesList = new ArrayList<>();
                consentTypesList.add(consentType);
                ArrayList<String> consentStatusesList = new ArrayList<>();
                consentStatusesList.add(applicableStatusToRevoke);

                // Get existing consents
                log.debug("Retrieving existing consents");

                // Only parameters needed for the search are provided, others are made null
                ArrayList<DetailedConsentResource> retrievedDetailedConsentResources = consentCoreDAO
                        .searchConsents(connection, null, clientIDsList, consentTypesList,
                                consentStatusesList, userIDsList, null, null, null, null);

                // Revoke existing consents and create audit records
                for (DetailedConsentResource resource : retrievedDetailedConsentResources) {
                    String previousConsentStatus = resource.getCurrentStatus();

                    // Update consent status
                    if (log.isDebugEnabled()) {
                        log.debug("Updating consent status for consent ID: " +
                                resource.getConsentID().replaceAll("[\r\n]", ""));
                    }
                    consentCoreDAO.updateConsentStatus(connection, resource.getConsentID(), revokedConsentStatus);

                    if (shouldRevokeTokens) {
                        revokeTokens(resource, userID);
                    }

                    // Create an audit record for consent update
                    if (log.isDebugEnabled()) {
                        log.debug("Creating audit record for the status change of consent ID: "
                                + resource.getConsentID().replaceAll("[\r\n]", ""));
                    }
                    // Create an audit record execute state change listener
                    HashMap<String, Object> consentDataMap = new HashMap<>();
                    consentDataMap.put(ConsentCoreServiceConstants.DETAILED_CONSENT_RESOURCE, resource);
                    postStateChange(connection, consentCoreDAO, resource.getConsentID(), userID,
                            revokedConsentStatus, previousConsentStatus,
                            ConsentCoreServiceConstants.CONSENT_REVOKE_REASON, resource.getClientID(), consentDataMap);

                    // Extract account mapping IDs for retrieved applicable consents
                    if (log.isDebugEnabled()) {
                        log.debug("Extracting account mapping IDs from consent ID: " +
                                resource.getConsentID().replaceAll("[\r\n]", ""));
                    }
                    for (ConsentMappingResource mappingResource : resource.getConsentMappingResources()) {
                        accountMappingIDsList.add(mappingResource.getMappingID());
                    }
                }

                // Update account mappings as inactive
                log.debug("Deactivating account mappings");
                consentCoreDAO.updateConsentMappingStatus(connection, accountMappingIDsList,
                        ConsentCoreServiceConstants.INACTIVE_MAPPING_STATUS);

                //Commit transaction
                DatabaseUtil.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return true;
            } catch (OBConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            } catch (OBConsentDataInsertionException e) {
                log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
                DatabaseUtil.rollbackTransaction(connection);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
            } catch (OBConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtil.rollbackTransaction(connection);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            } catch (IdentityOAuth2Exception e) {
                log.error("Error while revoking tokens for existing consents", e);
                throw new ConsentManagementException("Error occurred while revoking tokens for existing consents");
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public ConsentResource getConsent(String consentID, boolean withAttributes)
            throws ConsentManagementException {

        if (StringUtils.isBlank(consentID)) {
            log.error("Consent ID is missing, cannot proceed");
            throw new ConsentManagementException("Consent ID is missing, cannot proceed");
        }

        Connection connection = DatabaseUtil.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                ConsentResource retrievedConsentResource;

                // Get consent attributes if needed
                if (!withAttributes) {
                    if (log.isDebugEnabled()) {
                        log.debug("Retrieving consent for consent ID: " + consentID.replaceAll("[\r\n]", ""));
                    }
                    retrievedConsentResource = consentCoreDAO.getConsentResource(connection, consentID);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Retrieving consent with consent attributes for consent ID: " +
                                consentID.replaceAll("[\r\n]", ""));
                    }
                    retrievedConsentResource = consentCoreDAO.getConsentResourceWithAttributes(connection, consentID);
                }

                // Commit transactions
                DatabaseUtil.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return retrievedConsentResource;
            } catch (OBConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public boolean storeConsentAttributes(String consentID, Map<String, String> consentAttributes)
            throws ConsentManagementException {

        boolean isConsentAttributesStored;

        if (StringUtils.isBlank(consentID) || consentAttributes == null || consentAttributes.isEmpty()) {

            log.error("consentID or consentAttributes is missing, cannot proceed");
            throw new ConsentManagementException("Cannot proceed since consentID or consentAttributes is missing.");
        }

        Connection connection = DatabaseUtil.getDBConnection();

        try {
            try {
                ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
                ConsentAttributes consentAttributesObject = new ConsentAttributes();
                consentAttributesObject.setConsentID(consentID);
                consentAttributesObject.setConsentAttributes(consentAttributes);

                if (log.isDebugEnabled()) {
                    log.debug("Storing consent attributes for the consent of ID: " +
                            consentID.replaceAll("[\r\n]", ""));
                }
                isConsentAttributesStored = consentCoreDAO.storeConsentAttributes(connection, consentAttributesObject);
                DatabaseUtil.commitTransaction(connection);
            } catch (OBConsentDataInsertionException e) {
                log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
                DatabaseUtil.rollbackTransaction(connection);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }

        return isConsentAttributesStored;

    }

    @Override
    public ConsentAttributes getConsentAttributes(String consentID, ArrayList<String> consentAttributeKeys)
            throws ConsentManagementException {

        if (StringUtils.isBlank(consentID) || CollectionUtils.isEmpty(consentAttributeKeys)) {
            log.error("Consent ID or consent attributes keys are missing, cannot proceed");
            throw new ConsentManagementException("Consent ID or consent attribute keys are missing, cannot proceed");
        }

        Connection connection = DatabaseUtil.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                ConsentAttributes retrievedConsentAttributes;
                if (log.isDebugEnabled()) {
                    log.debug("Retrieving consent attributes for given keys for consent ID: " +
                            consentID.replaceAll("[\r\n]", ""));
                }
                retrievedConsentAttributes = consentCoreDAO.getConsentAttributes(connection, consentID,
                        consentAttributeKeys);

                // Commit transactions
                DatabaseUtil.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return retrievedConsentAttributes;
            } catch (OBConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public ConsentAttributes getConsentAttributes(String consentID)
            throws ConsentManagementException {

        if (StringUtils.isBlank(consentID)) {
            log.error("Consent ID is missing, cannot proceed");
            throw new ConsentManagementException("Consent ID is missing, cannot proceed");
        }

        Connection connection = DatabaseUtil.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                ConsentAttributes retrievedConsentAttributes;
                if (log.isDebugEnabled()) {
                    log.debug("Retrieving consent attributes for consent ID: " +
                            consentID.replaceAll("[\r\n]", ""));
                }
                retrievedConsentAttributes = consentCoreDAO.getConsentAttributes(connection, consentID);

                // Commit transactions
                DatabaseUtil.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return retrievedConsentAttributes;
            } catch (OBConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public Map<String, String> getConsentAttributesByName(String attributeName) throws ConsentManagementException {

        if (StringUtils.isBlank(attributeName)) {
            log.error("Attribute name is not provided, cannot proceed");
            throw new ConsentManagementException("Attribute name is not provided, cannot proceed");
        }

        Connection connection = DatabaseUtil.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                Map<String, String> retrievedAttributeValuesMap;
                if (log.isDebugEnabled()) {
                    log.debug("Retrieving attribute values for the provided attribute key: " +
                            attributeName.replaceAll("[\r\n]", ""));
                }
                retrievedAttributeValuesMap = consentCoreDAO.getConsentAttributesByName(connection, attributeName);

                // Commit transactions
                DatabaseUtil.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return retrievedAttributeValuesMap;
            } catch (OBConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public ArrayList<String> getConsentIdByConsentAttributeNameAndValue(String attributeName, String attributeValue)
            throws ConsentManagementException {

        if (StringUtils.isBlank(attributeName) || StringUtils.isBlank(attributeValue)) {
            log.error("Attribute name or value is not provided, cannot proceed");
            throw new ConsentManagementException("Attribute name or value is not provided, cannot proceed");
        }

        Connection connection = DatabaseUtil.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                ArrayList<String> retrievedConsentIdList;
                if (log.isDebugEnabled()) {
                    log.debug("Retrieving consent Id for the provided attribute key : " +
                            attributeName.replaceAll("[\r\n]", "") + " and " +
                            "attribute value : " + attributeValue.replaceAll("[\r\n]", ""));
                }
                retrievedConsentIdList = consentCoreDAO.getConsentIdByConsentAttributeNameAndValue(connection,
                        attributeName, attributeValue);

                // Commit transactions
                DatabaseUtil.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return retrievedConsentIdList;
            } catch (OBConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public boolean deleteConsentAttributes(String consentID, ArrayList<String> attributeKeysList)
            throws ConsentManagementException {

        if (StringUtils.isBlank(consentID) || CollectionUtils.isEmpty(attributeKeysList)) {
            log.error("Consent ID or attributes list is not provided, cannot proceed");
            throw new ConsentManagementException("Consent ID or attributes list is not provided, cannot proceed");
        }

        Connection connection = DatabaseUtil.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Deleting attributes for the consent ID: " + consentID.replaceAll("[\r\n]", ""));
                }
                consentCoreDAO.deleteConsentAttributes(connection, consentID, attributeKeysList);

                // Commit transactions
                DatabaseUtil.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return true;
            } catch (OBConsentDataDeletionException e) {
                log.error(ConsentCoreServiceConstants.DATA_DELETE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtil.rollbackTransaction(connection);
                throw new ConsentManagementException(ConsentCoreServiceConstants.CONSENT_ATTRIBUTES_DELETE_ERROR_MSG);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public ConsentFile getConsentFile(String consentID) throws ConsentManagementException {

        if (StringUtils.isBlank(consentID)) {
            log.error("Consent ID is missing, cannot proceed");
            throw new ConsentManagementException("Consent ID is missing, cannot proceed");
        }

        Connection connection = DatabaseUtil.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                ConsentFile retrievedConsentFileResource;

                // Get consent file
                if (log.isDebugEnabled()) {
                    log.debug("Retrieving consent file resource for consent ID: " +
                            consentID.replaceAll("[\r\n]", ""));
                }
                retrievedConsentFileResource = consentCoreDAO.getConsentFile(connection, consentID, false);

                // Commit transactions
                DatabaseUtil.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return retrievedConsentFileResource;
            } catch (OBConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public AuthorizationResource getAuthorizationResource(String authorizationID) throws ConsentManagementException {

        if (StringUtils.isBlank(authorizationID)) {
            log.error("Authorization ID is missing, cannot proceed");
            throw new ConsentManagementException("Authorization ID is missing, cannot proceed");
        }

        Connection connection = DatabaseUtil.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                AuthorizationResource retrievedAuthorizationResource;

                // Get consent file
                if (log.isDebugEnabled()) {
                    log.debug("Retrieving authorization resource for authorization ID: " +
                            authorizationID.replaceAll("[\r\n]", ""));
                }
                retrievedAuthorizationResource = consentCoreDAO.getAuthorizationResource(connection, authorizationID);

                // Commit transactions
                DatabaseUtil.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return retrievedAuthorizationResource;
            } catch (OBConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public ArrayList<ConsentStatusAuditRecord> searchConsentStatusAuditRecords(String consentID, String status,
                                                                               String actionBy, Long fromTime,
                                                                               Long toTime, String statusAuditID)
            throws ConsentManagementException {

        ArrayList<ConsentStatusAuditRecord> auditRecords;
        Connection connection = DatabaseUtil.getDBConnection();

        try {
            try {
                ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();

                log.debug("Searching audit records");
                auditRecords = consentCoreDAO.getConsentStatusAuditRecords(connection, consentID, status, actionBy,
                        fromTime, toTime, statusAuditID, false);

            } catch (OBConsentDataRetrievalException e) {
                log.error("Error occurred while searching audit records");
                throw new ConsentManagementException("Error occurred while searching audit records", e);
            }

            // Commit transactions
            DatabaseUtil.commitTransaction(connection);
            log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
        return auditRecords;
    }

    @Override
    public boolean reAuthorizeExistingAuthResource(String consentID, String authID, String userID,
                                                   Map<String, ArrayList<String>> accountIDsMapWithPermissions,
                                                   String currentConsentStatus, String newConsentStatus)
            throws ConsentManagementException {

        if (StringUtils.isBlank(consentID) || StringUtils.isBlank(authID) || StringUtils.isBlank(userID)
                || MapUtils.isEmpty(accountIDsMapWithPermissions) || StringUtils.isBlank(newConsentStatus)
                || StringUtils.isBlank(currentConsentStatus)) {
            log.error("Consent ID, auth ID, user ID, account permissions map, applicable consent status, new consent " +
                    "status or current consent status is not present, cannot proceed");
            throw new ConsentManagementException("Consent ID, auth ID, user ID, account permissions map, applicable " +
                    "consent status, new consent status or current consent status is not present, cannot proceed");
        }

        Connection connection = DatabaseUtil.getDBConnection();

        try {
            try {
                ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();

                // Get detailed consent to retrieve account mappings
                DetailedConsentResource detailedConsentResource =
                        consentCoreDAO.getDetailedConsentResource(connection, consentID, false);

                // Update accounts if required
                    updateAccounts(connection, consentCoreDAO, authID, accountIDsMapWithPermissions,
                        detailedConsentResource, false);

                // Update consent status
                consentCoreDAO.updateConsentStatus(connection, consentID, newConsentStatus);

                // Create an audit record execute state change listener
                HashMap<String, Object> consentDataMap = new HashMap<>();
                consentDataMap.put(ConsentCoreServiceConstants.DETAILED_CONSENT_RESOURCE, detailedConsentResource);
                postStateChange(connection, consentCoreDAO, consentID, userID, newConsentStatus,
                        currentConsentStatus, ConsentCoreServiceConstants.CONSENT_REAUTHORIZE_REASON,
                        detailedConsentResource.getClientID(), consentDataMap);

                // Commit transactions
                DatabaseUtil.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return true;
            } catch (OBConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            } catch (OBConsentDataInsertionException e) {
                log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
                DatabaseUtil.rollbackTransaction(connection);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
            } catch (OBConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtil.rollbackTransaction(connection);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public boolean reAuthorizeConsentWithNewAuthResource(String consentID, String userID, Map<String,
            ArrayList<String>> accountIDsMapWithPermissions, String currentConsentStatus, String newConsentStatus,
                                                         String newExistingAuthStatus, String newAuthStatus,
                                                         String newAuthType)
            throws ConsentManagementException {

        if (StringUtils.isBlank(consentID) || StringUtils.isBlank(userID)
                || MapUtils.isEmpty(accountIDsMapWithPermissions) || StringUtils.isBlank(newConsentStatus)
                || StringUtils.isBlank(currentConsentStatus) || StringUtils.isBlank(newExistingAuthStatus)
                || StringUtils.isBlank(newAuthStatus) || StringUtils.isBlank(newAuthType)) {
            log.error("Consent ID, user ID, account permissions map, current consent status, new consent " +
                    "status, new existing auth status, new auth status or new auth type is not present, cannot " +
                    "proceed");
            throw new ConsentManagementException("Consent ID, user ID, account permissions map, current consent " +
                    "status, new consent status, new existing auth status, new auth status or new auth type is not " +
                    "present, cannot proceed");
        }

        Connection connection = DatabaseUtil.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {

                // Get authorizations related to current consent to revoke
                ArrayList<AuthorizationResource> authorizationResources =
                        consentCoreDAO.searchConsentAuthorizations(connection, consentID, userID);

                ArrayList<ConsentMappingResource> mappingResourcesToDeactivate
                        = new ArrayList<ConsentMappingResource>();
                for (AuthorizationResource resource : authorizationResources) {
                    // Update existing authorizations
                    consentCoreDAO.updateAuthorizationStatus(connection, resource.getAuthorizationID(),
                            newExistingAuthStatus);
                    mappingResourcesToDeactivate.addAll(consentCoreDAO.getConsentMappingResources(connection,
                            resource.getAuthorizationID()));
                }

                // Deactivate account mappings of old auth resource.
                ArrayList<String> mappingIdsToDeactivate = new ArrayList<>();
                for (ConsentMappingResource resource : mappingResourcesToDeactivate) {
                    mappingIdsToDeactivate.add(resource.getMappingID());
                }
                consentCoreDAO.updateConsentMappingStatus(connection, mappingIdsToDeactivate,
                        ConsentCoreServiceConstants.INACTIVE_MAPPING_STATUS);

                // Create a new authorization resource for the consent
                AuthorizationResource newAuthorizationResource = new AuthorizationResource();
                newAuthorizationResource.setConsentID(consentID);
                newAuthorizationResource.setAuthorizationType(newAuthType);
                newAuthorizationResource.setAuthorizationStatus(newAuthStatus);
                newAuthorizationResource.setUserID(userID);
                consentCoreDAO.storeAuthorizationResource(connection, newAuthorizationResource);

                // Retrieve the detailed consent for obtaining relative account mappings
                DetailedConsentResource detailedConsentResource =
                        consentCoreDAO.getDetailedConsentResource(connection, consentID, false);

                // Update accounts if required
                updateAccounts(connection, consentCoreDAO, newAuthorizationResource.getAuthorizationID(),
                        accountIDsMapWithPermissions, detailedConsentResource, true);

                // Update consent status
                consentCoreDAO.updateConsentStatus(connection, consentID, newConsentStatus);

                // Create an audit record execute state change listener
                HashMap<String, Object> consentDataMap = new HashMap<>();
                consentDataMap.put(ConsentCoreServiceConstants.DETAILED_CONSENT_RESOURCE, detailedConsentResource);
                postStateChange(connection, consentCoreDAO, consentID, userID, newConsentStatus,
                        currentConsentStatus, ConsentCoreServiceConstants.CONSENT_REAUTHORIZE_REASON,
                        detailedConsentResource.getClientID(), consentDataMap);

                // Commit transactions
                DatabaseUtil.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return true;
            } catch (OBConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            } catch (OBConsentDataInsertionException e) {
                log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
                DatabaseUtil.rollbackTransaction(connection);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
            } catch (OBConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtil.rollbackTransaction(connection);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public DetailedConsentResource getDetailedConsent(String consentID) throws ConsentManagementException {

        if (StringUtils.isBlank(consentID)) {
            log.error("Consent ID is missing, cannot proceed");
            throw new ConsentManagementException("Consent ID is missing, cannot proceed");
        }

        Connection connection = DatabaseUtil.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                DetailedConsentResource retrievedDetailedConsentResource;

                // Retrieve the detailed consent resource
                if (log.isDebugEnabled()) {
                    log.debug("Retrieving detailed consent for consent ID: " + consentID.replaceAll("[\r\n]", ""));
                }
                retrievedDetailedConsentResource = consentCoreDAO.getDetailedConsentResource(connection, consentID,
                        false);

                // Commit transactions
                DatabaseUtil.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return retrievedDetailedConsentResource;
            } catch (OBConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public AuthorizationResource createConsentAuthorization(AuthorizationResource authorizationResource)
            throws ConsentManagementException {

        if (StringUtils.isBlank(authorizationResource.getConsentID()) ||
                StringUtils.isBlank(authorizationResource.getAuthorizationType()) ||
                StringUtils.isBlank(authorizationResource.getAuthorizationStatus())) {

            log.error("Consent ID, authorization type, user ID or authorization status is missing, cannot proceed");
            throw new ConsentManagementException("Cannot proceed since consent ID, authorization type, user ID or " +
                    "authorization status is missing");
        }

        Connection connection = DatabaseUtil.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                // Create authorization resource
                if (log.isDebugEnabled()) {
                    log.debug("Creating authorization resource for the consent of ID: " + authorizationResource
                            .getConsentID().replaceAll("[\r\n]", ""));
                }
                AuthorizationResource storedAuthorizationResource =
                        consentCoreDAO.storeAuthorizationResource(connection, authorizationResource);

                DatabaseUtil.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return storedAuthorizationResource;
            } catch (OBConsentDataInsertionException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                DatabaseUtil.rollbackTransaction(connection);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public ArrayList<ConsentMappingResource> createConsentAccountMappings(String authID, Map<String,
            ArrayList<String>> accountIDsMapWithPermissions) throws ConsentManagementException {

        if (StringUtils.isBlank(authID) || MapUtils.isEmpty(accountIDsMapWithPermissions)) {
            log.error("Authorization ID, accountID/permission map is not found, cannot " +
                    "proceed");
            throw new ConsentManagementException("Authorization ID, accountID/permission map " +
                    "is not found, cannot proceed");
        }

        ArrayList<ConsentMappingResource> storedConsentMappingResources = new ArrayList<>();
        Connection connection = DatabaseUtil.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                // Create account mapping resources
                if (log.isDebugEnabled()) {
                    log.debug("Creating consent account mapping resources for authorization ID: " +
                            authID.replaceAll("[\r\n]", ""));
                }
                for (Map.Entry<String, ArrayList<String>> entry : accountIDsMapWithPermissions.entrySet()) {
                    String accountID = entry.getKey();
                    for (String value : entry.getValue()) {
                        ConsentMappingResource consentMappingResource = new ConsentMappingResource();
                        consentMappingResource.setAccountID(accountID);
                        consentMappingResource.setPermission(value);
                        consentMappingResource.setAuthorizationID(authID);
                        consentMappingResource.setMappingStatus(ConsentCoreServiceConstants.ACTIVE_MAPPING_STATUS);
                        storedConsentMappingResources.add(consentCoreDAO.storeConsentMappingResource(connection,
                                consentMappingResource));
                    }
                }

                // Commit transaction
                DatabaseUtil.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return storedConsentMappingResources;
            } catch (OBConsentDataInsertionException e) {
                log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
                DatabaseUtil.rollbackTransaction(connection);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public boolean deactivateAccountMappings(ArrayList<String> accountMappingIDs) throws ConsentManagementException {

        if (accountMappingIDs.isEmpty()) {
            log.error("Account mapping IDs are not provided, cannot proceed");
            throw new ConsentManagementException("Cannot proceed since account mapping IDs are not provided");
        }

        Connection connection = DatabaseUtil.getDBConnection();
        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                // Deactivate account mapping resources
                log.debug("Deactivating consent account mapping resources for given mapping IDs");

                consentCoreDAO.updateConsentMappingStatus(connection, accountMappingIDs,
                        ConsentCoreServiceConstants.INACTIVE_MAPPING_STATUS);

                // Commit transaction
                DatabaseUtil.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return true;
            } catch (OBConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtil.rollbackTransaction(connection);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }
    @Override
    public boolean updateAccountMappingStatus(ArrayList<String> accountMappingIDs, String newMappingStatus) throws
            ConsentManagementException {

        if (accountMappingIDs.isEmpty()) {
            log.error("Account mapping IDs are not provided, cannot proceed");
            throw new ConsentManagementException("Cannot proceed since account mapping IDs are not provided");
        }

        Connection connection = DatabaseUtil.getDBConnection();
        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                // update account mapping resources
                log.debug("Deactivating consent account mapping resources for given mapping IDs");

                consentCoreDAO.updateConsentMappingStatus(connection, accountMappingIDs,
                        newMappingStatus);

                // Commit transaction
                DatabaseUtil.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return true;
            } catch (OBConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtil.rollbackTransaction(connection);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public ArrayList<DetailedConsentResource> searchDetailedConsents(ArrayList<String> consentIDs,
                                                                     ArrayList<String> clientIDs,
                                                                     ArrayList<String> consentTypes,
                                                                     ArrayList<String> consentStatuses,
                                                                     ArrayList<String> userIDs, Long fromTime,
                                                                     Long toTime,
                                                                     Integer limit, Integer offset)
            throws ConsentManagementException {

        // Input parameters except limit and offset are not validated since they are validated in the DAO method
        ArrayList<DetailedConsentResource> detailedConsentResources;

        Connection connection = DatabaseUtil.getDBConnection();

        try {
            try {
                ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();

                log.debug("Searching detailed consents");
                detailedConsentResources = consentCoreDAO.searchConsents(connection, consentIDs, clientIDs,
                        consentTypes, consentStatuses, userIDs, fromTime, toTime, limit, offset);

            } catch (OBConsentDataRetrievalException e) {
                log.error("Error occurred while searching detailed consents", e);
                throw new ConsentManagementException("Error occurred while searching detailed consents", e);
            }

            // Commit transactions
            DatabaseUtil.commitTransaction(connection);
            log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
        return detailedConsentResources;
    }

    @Override
    public ArrayList<DetailedConsentResource> searchDetailedConsents(ArrayList<String> consentIDs,
                                                                     ArrayList<String> clientIDs,
                                                                     ArrayList<String> consentTypes,
                                                                     ArrayList<String> consentStatuses,
                                                                     ArrayList<String> userIDs, Long fromTime,
                                                                     Long toTime, Integer limit, Integer offset,
                                                                     boolean fetchFromRetentionDatabase)
            throws ConsentManagementException {

        // Input parameters except limit and offset are not validated since they are validated in the DAO method
        ArrayList<DetailedConsentResource> detailedConsentResources;

        Connection connection;
        if (fetchFromRetentionDatabase) {
            connection = DatabaseUtil.getRetentionDBConnection();
        } else {
            connection = DatabaseUtil.getDBConnection();
        }

        try {
            try {
                ConsentCoreDAO consentCoreDAO;
                if (fetchFromRetentionDatabase) {
                    consentCoreDAO = ConsentStoreInitializer.getInitializedConsentRetentionDAOImpl();
                } else {
                    consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
                }

                log.debug("Searching detailed consents");
                detailedConsentResources = consentCoreDAO.searchConsents(connection, consentIDs, clientIDs,
                        consentTypes, consentStatuses, userIDs, fromTime, toTime, limit, offset);

            } catch (OBConsentDataRetrievalException e) {
                log.error("Error occurred while searching detailed consents", e);
                throw new ConsentManagementException("Error occurred while searching detailed consents", e);
            }

            // Commit transactions
            DatabaseUtil.commitTransaction(connection);
            log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
        return detailedConsentResources;
    }

    @Override
    public boolean bindUserAccountsToConsent(ConsentResource consentResource, String userID,
                                             String authID, ArrayList<String> accountIDs,
                                             String newAuthStatus,
                                             String newCurrentConsentStatus)
            throws ConsentManagementException {

        Map<String, ArrayList<String>> accountIDsMapWithPermissions = new HashMap<>();
        ArrayList<String> permissionsDefault = new ArrayList<>();
        permissionsDefault.add("n/a");

        for (String accountId : accountIDs) {
            accountIDsMapWithPermissions.put(accountId, permissionsDefault);
        }

        return bindUserAccountsToConsent(consentResource, userID, authID, accountIDsMapWithPermissions, newAuthStatus,
                newCurrentConsentStatus);
    }

    @Override
    public boolean bindUserAccountsToConsent(ConsentResource consentResource, String userID,
                                             String authID, Map<String, ArrayList<String>> accountIDsMapWithPermissions,
                                             String newAuthStatus,
                                             String newCurrentConsentStatus)
            throws ConsentManagementException {

        String consentID = consentResource.getConsentID();
        String clientID = consentResource.getClientID();
        String consentType = consentResource.getConsentType();

        if (StringUtils.isBlank(consentID) || StringUtils.isBlank(clientID) || StringUtils.isBlank(consentType)
                || StringUtils.isBlank(userID) || StringUtils.isBlank(authID) || StringUtils.isBlank(newAuthStatus)
                || StringUtils.isBlank(newCurrentConsentStatus)) {
            log.error("Consent ID, client ID, consent type, user ID, authorization ID, new authorization status or " +
                    "new consent status is " +
                    "missing, cannot proceed.");
            throw new ConsentManagementException("Consent ID, client ID, consent type, user ID, authorization ID, new" +
                    " authorization status or new consent status is missing, " +
                    "cannot proceed");
        }

        if (MapUtils.isEmpty(accountIDsMapWithPermissions)) {
            log.error("Account IDs and relative permissions are not present, cannot proceed");
            throw new ConsentManagementException("Account IDs and relative permissions are not present, cannot " +
                    "proceed");
        }

        Connection connection = DatabaseUtil.getDBConnection();
        try {
            try {
                ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();

                // Update authorization resource of current consent
                if (log.isDebugEnabled()) {
                    log.debug("Update authorization status and authorization user for current consent ID: "
                            + consentID.replaceAll("[\r\n]", ""));
                }
                consentCoreDAO.updateAuthorizationUser(connection, authID, userID);
                consentCoreDAO.updateAuthorizationStatus(connection, authID, newAuthStatus);

                // Create account mappings for current consent
                if (log.isDebugEnabled()) {
                    log.debug("Creating account mappings for current consent ID: " +
                            consentID.replaceAll("[\r\n]", ""));
                }
                for (Map.Entry<String, ArrayList<String>> entry : accountIDsMapWithPermissions.entrySet()) {
                    String accountID = entry.getKey();
                    for (String value : entry.getValue()) {
                        ConsentMappingResource consentMappingResource = new ConsentMappingResource();
                        consentMappingResource.setAccountID(accountID);
                        consentMappingResource.setPermission(value);
                        consentMappingResource.setAuthorizationID(authID);
                        consentMappingResource.setMappingStatus(ConsentCoreServiceConstants.ACTIVE_MAPPING_STATUS);
                        consentCoreDAO.storeConsentMappingResource(connection, consentMappingResource);
                    }
                }

                // Update current consent status
                if (log.isDebugEnabled()) {
                    log.debug("Update the status of the current consent ID: " + consentID.replaceAll("[\r\n]", ""));
                }
                consentCoreDAO.updateConsentStatus(connection, consentID, newCurrentConsentStatus);

                // Create audit record for the consent status update and execute the state change listener
                HashMap<String, Object> consentDataMap = new HashMap<>();
                consentDataMap.put(ConsentCoreServiceConstants.CONSENT_RESOURCE, consentResource);
                postStateChange(connection, consentCoreDAO, consentID, userID, newCurrentConsentStatus,
                        consentResource.getCurrentStatus(), ConsentCoreServiceConstants.USER_ACCOUNTS_BINDING_REASON,
                        clientID, consentDataMap);

                // Commit transactions
                DatabaseUtil.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return true;
            } catch (OBConsentDataInsertionException e) {
                log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
                DatabaseUtil.rollbackTransaction(connection);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
            } catch (OBConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtil.rollbackTransaction(connection);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public ArrayList<AuthorizationResource> searchAuthorizations(String consentID)
            throws ConsentManagementException {
        return searchAuthorizations(consentID, null);
    }

    @Override
    public ArrayList<AuthorizationResource> searchAuthorizationsForUser(String userID)
            throws ConsentManagementException {
        return searchAuthorizations(null, userID);
    }

    @Override
    public ArrayList<AuthorizationResource> searchAuthorizations(String consentID, String userID)
            throws ConsentManagementException {

        ArrayList<AuthorizationResource> authorizationResources;
        Connection connection = DatabaseUtil.getDBConnection();

        try {
            try {
                ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();

                log.debug("Searching authorization resources");
                authorizationResources = consentCoreDAO.searchConsentAuthorizations(connection, consentID, userID);

            } catch (OBConsentDataRetrievalException e) {
                log.error("Error occurred while searching authorization resources", e);
                throw new ConsentManagementException("Error occurred while searching authorization resources", e);
            }

            // Commit transactions
            DatabaseUtil.commitTransaction(connection);
            log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
        return authorizationResources;
    }

    private void createAuditRecord(Connection connection, ConsentCoreDAO consentCoreDAO, String consentID,
                                   String userID, String newConsentStatus, String previousConsentStatus, String reason)
            throws OBConsentDataInsertionException {

        // Create an audit record
        ConsentStatusAuditRecord consentStatusAuditRecord = new ConsentStatusAuditRecord();
        consentStatusAuditRecord.setConsentID(consentID);
        consentStatusAuditRecord.setCurrentStatus(newConsentStatus);
        consentStatusAuditRecord.setReason(reason);
        if (StringUtils.isNotEmpty(userID)) {
            consentStatusAuditRecord.setActionBy(userID);
        } else {
            consentStatusAuditRecord.setActionBy(null);
        }
        consentStatusAuditRecord.setPreviousStatus(previousConsentStatus);

        if (log.isDebugEnabled()) {
            log.debug(("Storing audit record for consent of ID: " +
                    consentStatusAuditRecord.getConsentID()).replaceAll("[\r\n]", ""));
        }
        consentCoreDAO.storeConsentStatusAuditRecord(connection, consentStatusAuditRecord);
    }

    private DetailedConsentResource createAuthorizableConesntWithAuditRecord(Connection connection,
                                                                             ConsentCoreDAO consentCoreDAO,
                                                                             ConsentResource consentResource,
                                                                             String userID, String authStatus,
                                                                             String authType,
                                                                             boolean isImplicitAuthorization)
            throws OBConsentDataInsertionException, ConsentManagementException {

        boolean isConsentAttributesStored = false;
        AuthorizationResource storedAuthorizationResource = null;

        // Create consent
        if (log.isDebugEnabled()) {
            log.debug(("Creating the consent for ID:" + consentResource.getConsentID()).replaceAll("[\r\n]", ""));
        }
        ConsentResource storedConsentResource = consentCoreDAO.storeConsentResource(connection,
                consentResource);
        String consentID = storedConsentResource.getConsentID();

        // Store consent attributes if available
        if (MapUtils.isNotEmpty(consentResource.getConsentAttributes())) {
            ConsentAttributes consentAttributes = new ConsentAttributes();
            consentAttributes.setConsentID(consentID);
            consentAttributes.setConsentAttributes(consentResource.getConsentAttributes());

            if (log.isDebugEnabled()) {
                log.debug("Storing consent attributes for the consent of ID: " + consentAttributes
                        .getConsentID().replaceAll("[\r\n]", ""));
            }
            isConsentAttributesStored = consentCoreDAO.storeConsentAttributes(connection, consentAttributes);
        }

        /* Create audit record, setting previous consent status as null since this is the first time the
           consent is created and execute state change listener */
        HashMap<String, Object> consentDataMap = new HashMap<>();
        consentDataMap.put(ConsentCoreServiceConstants.CONSENT_RESOURCE, consentResource);
        postStateChange(connection, consentCoreDAO, consentID, userID, consentResource.getCurrentStatus(),
                null, ConsentCoreServiceConstants.CREATE_CONSENT_REASON,
                consentResource.getClientID(), consentDataMap);

        // Create an authorization resource if isImplicitAuth parameter is true
        if (isImplicitAuthorization) {
            AuthorizationResource authorizationResource = new AuthorizationResource();
            authorizationResource.setConsentID(consentID);
            authorizationResource.setAuthorizationStatus(authStatus);
            authorizationResource.setAuthorizationType(authType);
            if (StringUtils.isNotBlank(userID)) {
                authorizationResource.setUserID(userID);
            } else {
                /* Setting userID as null since at this point, there is no userID in this flow. User ID can be
                   updated in authorization flow */
                authorizationResource.setUserID(null);
            }

            if (log.isDebugEnabled()) {
                log.debug(("Storing authorization resource for consent of ID: " + authorizationResource
                        .getConsentID()).replaceAll("[\r\n]", ""));
            }

            storedAuthorizationResource = consentCoreDAO.storeAuthorizationResource(connection,
                    authorizationResource);
        }

        DetailedConsentResource detailedConsentResource = new DetailedConsentResource();
        detailedConsentResource.setConsentID(consentID);
        detailedConsentResource.setClientID(storedConsentResource.getClientID());
        detailedConsentResource.setReceipt(storedConsentResource.getReceipt());
        detailedConsentResource.setConsentType(storedConsentResource.getConsentType());
        detailedConsentResource.setCurrentStatus(storedConsentResource.getCurrentStatus());
        detailedConsentResource.setConsentFrequency(storedConsentResource.getConsentFrequency());
        detailedConsentResource.setValidityPeriod(storedConsentResource.getValidityPeriod());
        detailedConsentResource.setCreatedTime(storedConsentResource.getCreatedTime());
        detailedConsentResource.setRecurringIndicator(storedConsentResource.isRecurringIndicator());
        detailedConsentResource.setUpdatedTime(storedConsentResource.getUpdatedTime());

        if (isConsentAttributesStored) {
            detailedConsentResource.setConsentAttributes(consentResource.getConsentAttributes());
        }
        if (isImplicitAuthorization) {
            ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
            authorizationResources.add(storedAuthorizationResource);
            detailedConsentResource.setAuthorizationResources(authorizationResources);
        }
        return detailedConsentResource;
    }

    private void updateExistingConsentStatusesAndRevokeAccountMappings(Connection connection,
                                                                       ConsentCoreDAO consentCoreDAO,
                                                                       ConsentResource consentResource, String userID,
                                                                       String applicableExistingConsentsStatus,
                                                                       String newExistingConsentStatus)
            throws OBConsentDataRetrievalException, OBConsentDataUpdationException, OBConsentDataInsertionException,
            ConsentManagementException {

        ArrayList<String> accountMappingIDsList = new ArrayList<>();
        ArrayList<String> clientIDsList = new ArrayList<>();
        clientIDsList.add(consentResource.getClientID());
        ArrayList<String> userIDsList = new ArrayList<>();
        userIDsList.add(userID);
        ArrayList<String> consentTypesList = new ArrayList<>();
        consentTypesList.add(consentResource.getConsentType());
        ArrayList<String> consentStatusesList = new ArrayList<>();
        consentStatusesList.add(applicableExistingConsentsStatus);

        // Get existing applicable consents
        log.debug("Retrieving existing authorized consents");
        ArrayList<DetailedConsentResource> retrievedExistingAuthorizedConsentsList =
                consentCoreDAO.searchConsents(connection, null, clientIDsList, consentTypesList,
                        consentStatusesList, userIDsList, null, null, null,
                        null);

        for (DetailedConsentResource resource : retrievedExistingAuthorizedConsentsList) {

            String previousConsentStatus = resource.getCurrentStatus();

            // Update existing consents as necessary
            if (log.isDebugEnabled()) {
                log.debug(("Updating existing consent statuses with the new status provided for consent ID: "
                        + resource.getConsentID()).replaceAll("[\r\n]", ""));
            }
            consentCoreDAO.updateConsentStatus(connection, resource.getConsentID(),
                    newExistingConsentStatus);

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
                    ConsentCoreServiceConstants.CREATE_EXCLUSIVE_AUTHORIZATION_CONSENT_REASON, resource.getClientID(),
                    consentDataMap);

            // Extract account mapping IDs for retrieved applicable consents
            if (log.isDebugEnabled()) {
                log.debug(("Extracting account mapping IDs from consent ID: " +
                        resource.getConsentID()).replaceAll("[\r\n]", ""));
            }
            for (ConsentMappingResource mappingResource : resource.getConsentMappingResources()) {
                accountMappingIDsList.add(mappingResource.getMappingID());
            }
        }

        // Update account mappings as inactive
        log.debug("Deactivating account mappings");
        consentCoreDAO.updateConsentMappingStatus(connection, accountMappingIDsList,
                ConsentCoreServiceConstants.INACTIVE_MAPPING_STATUS);
    }

    private void updateAccounts(Connection connection,
                                ConsentCoreDAO consentCoreDAO, String authID,
                                Map<String, ArrayList<String>> accountIDsMapWithPermissions,
                                DetailedConsentResource detailedConsentResource, boolean isNewAuthResource)
            throws OBConsentDataInsertionException, OBConsentDataUpdationException {

        // Get existing consent account mappings
        log.debug("Retrieve existing active account mappings");
        ArrayList<ConsentMappingResource> existingAccountMappings =
                detailedConsentResource.getConsentMappingResources();

        // Determine unique account IDs
        HashSet<String> existingAccountIDs = new HashSet<>();
        for (ConsentMappingResource resource : existingAccountMappings) {
            existingAccountIDs.add(resource.getAccountID());
        }

        ArrayList<String> existingAccountIDsList = new ArrayList<>(existingAccountIDs);

        ArrayList<String> reAuthorizedAccounts = new ArrayList<>();
        for (Map.Entry<String, ArrayList<String>> entry : accountIDsMapWithPermissions.entrySet()) {
            String accountID = entry.getKey();
            reAuthorizedAccounts.add(accountID);
        }

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
                    ConsentMappingResource consentMappingResource = new ConsentMappingResource();
                    consentMappingResource.setAuthorizationID(authID);
                    consentMappingResource.setAccountID(accountID);
                    consentMappingResource.setPermission(permission);
                    consentMappingResource.setMappingStatus(ConsentCoreServiceConstants.ACTIVE_MAPPING_STATUS);
                    consentCoreDAO.storeConsentMappingResource(connection, consentMappingResource);
                }
            }
        }
        if (!accountsToRevoke.isEmpty()) {
            // Update mapping statuses of revoking accounts to inactive
            log.debug("Deactivate unwanted account mappings");
            ArrayList<String> mappingIDsToUpdate = new ArrayList<>();
            for (String accountID : accountsToRevoke) {
                for (ConsentMappingResource resource : existingAccountMappings) {
                    if (accountID.equalsIgnoreCase(resource.getAccountID())) {
                        mappingIDsToUpdate.add(resource.getMappingID());
                    }
                }
            }
            consentCoreDAO.updateConsentMappingStatus(connection, mappingIDsToUpdate,
                    ConsentCoreServiceConstants.INACTIVE_MAPPING_STATUS);
        }
    }

    @Override
    public ConsentResource amendConsentData(String consentID, String consentReceipt, Long consentValidityTime,
                                            String userID)
            throws ConsentManagementException {

        if (StringUtils.isBlank(consentID) ||
                (StringUtils.isBlank(consentReceipt) && (consentValidityTime == null))) {
            log.error("Consent ID or both consent receipt and consent validity period are not provided, cannot " +
                    "proceed");
            throw new ConsentManagementException("Consent ID or both consent receipt and consent validity period are " +
                    "not provided, cannot proceed");
        }

        Connection connection = DatabaseUtil.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                // Update receipt and validity time accordingly
                if (StringUtils.isNotBlank(consentReceipt) && (consentValidityTime != null)) {
                    // update receipt
                    consentCoreDAO.updateConsentReceipt(connection, consentID, consentReceipt);
                    // update validity period
                    consentCoreDAO.updateConsentValidityTime(connection, consentID, consentValidityTime);
                } else {
                    if (StringUtils.isBlank(consentReceipt) && (consentValidityTime != null)) {
                        // update receipt
                        consentCoreDAO.updateConsentValidityTime(connection, consentID, consentValidityTime);
                    } else {
                        // update receipt
                        consentCoreDAO.updateConsentReceipt(connection, consentID, consentReceipt);
                    }
                }

                // Get consent after update
                ConsentResource consentResource = consentCoreDAO.getConsentResource(connection, consentID);

                /* Even if the consent is amended, the status remains same as Authorized. For tracking purposes, an
                 audit record is created as the consent status of "amended". But still the real consent status will
                 remain as it is */
                HashMap<String, Object> consentDataMap = new HashMap<>();
                consentDataMap.put(ConsentCoreServiceConstants.CONSENT_RESOURCE, consentResource);
                postStateChange(connection, consentCoreDAO, consentID, userID,
                        ConsentCoreServiceConstants.CONSENT_AMENDED_STATUS, consentResource.getCurrentStatus(),
                        ConsentCoreServiceConstants.CONSENT_AMEND_REASON, consentResource.getClientID(),
                        consentDataMap);

                // Commit transaction
                DatabaseUtil.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return consentResource;
            } catch (OBConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            } catch (OBConsentDataInsertionException e) {
                log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
                DatabaseUtil.rollbackTransaction(connection);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
            } catch (OBConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtil.rollbackTransaction(connection);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    public DetailedConsentResource amendDetailedConsent(String consentID, String consentReceipt,
                            Long consentValidityTime, String authID,
                            Map<String, ArrayList<String>> accountIDsMapWithPermissions,
                            String newConsentStatus, Map<String, String> consentAttributes, String userID,
                            Map<String, Object> additionalAmendmentData)
            throws ConsentManagementException {

        if (StringUtils.isBlank(consentID) ||
                (StringUtils.isBlank(consentReceipt) && (consentValidityTime == null))) {
            log.error("Consent ID or both consent receipt and consent validity period are not provided, cannot " +
                    "proceed");
            throw new ConsentManagementException("Consent ID or both consent receipt and consent validity period are " +
                    "not provided, cannot proceed");
        }

        if (StringUtils.isBlank(authID) || StringUtils.isBlank(userID)
            || MapUtils.isEmpty(accountIDsMapWithPermissions) || StringUtils.isBlank(newConsentStatus)
                || consentAttributes == null) {
            log.error("Auth ID, user ID, account permissions map, new consent status or new consent attributes " +
                    "is not present, cannot proceed");
            throw new ConsentManagementException("Auth ID, user ID, account permissions map, new consent status or " +
                    "new consent attributes is not present, cannot proceed");
        }

        Connection connection = DatabaseUtil.getDBConnection();
        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            // Retrieve the current detailed consent before the amendment for the consent amendment history persistence
            DetailedConsentResource detailedConsentResource =
                    consentCoreDAO.getDetailedConsentResource(connection, consentID, false);

            // Update receipt and validity time
            if (StringUtils.isNotBlank(consentReceipt)) {
                consentCoreDAO.updateConsentReceipt(connection, consentID, consentReceipt);
            }
            if (consentValidityTime != null) {
                consentCoreDAO.updateConsentValidityTime(connection, consentID, consentValidityTime);
            }

            // Update consent status and record the updated time
            consentCoreDAO.updateConsentStatus(connection, consentID, newConsentStatus);

            // Update accounts if required
            updateAccounts(connection, consentCoreDAO, authID, accountIDsMapWithPermissions,
                    detailedConsentResource, false);

            // Update consent attributes
            updateConsentAttributes(connection, consentCoreDAO, consentID, consentAttributes);

            // Update consent accordingly if additional amendment data passed
            if (!additionalAmendmentData.isEmpty()) {
                processAdditionalConsentAmendmentData(connection, consentCoreDAO, additionalAmendmentData);
            }

            // Get detailed consent status after update
            DetailedConsentResource newDetailedConsentResource =
                    consentCoreDAO.getDetailedConsentResource(connection, consentID, false);

            /* Even if the consent is amended, the status remains same as Authorized. For tracking purposes, an
                 audit record is created as the consent status of "amended". But still the real consent status will
                 remain as it is */
            HashMap<String, Object> consentDataMap = new HashMap<>();
            consentDataMap.put(ConsentCoreServiceConstants.DETAILED_CONSENT_RESOURCE, newDetailedConsentResource);

            // Pass the previous consent to persist as consent amendment history
            consentDataMap.put(ConsentCoreServiceConstants.CONSENT_AMENDMENT_HISTORY_RESOURCE, detailedConsentResource);
            consentDataMap.put(ConsentCoreServiceConstants.CONSENT_AMENDMENT_TIME, System.currentTimeMillis());

            postStateChange(connection, consentCoreDAO, consentID, userID,
                ConsentCoreServiceConstants.CONSENT_AMENDED_STATUS, detailedConsentResource.getCurrentStatus(),
                ConsentCoreServiceConstants.CONSENT_AMEND_REASON, detailedConsentResource.getClientID(),
                consentDataMap);

            // Commit transactions
            DatabaseUtil.commitTransaction(connection);
            log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
            return newDetailedConsentResource;
        } catch (OBConsentDataRetrievalException e) {
            log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
        } catch (OBConsentDataInsertionException e) {
            log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
            DatabaseUtil.rollbackTransaction(connection);
            throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
        } catch (OBConsentDataUpdationException e) {
            log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            DatabaseUtil.rollbackTransaction(connection);
            throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
        } catch (OBConsentDataDeletionException e) {
            log.error(ConsentCoreServiceConstants.DATA_DELETE_ROLLBACK_ERROR_MSG, e);
            DatabaseUtil.rollbackTransaction(connection);
            throw new ConsentManagementException(ConsentCoreServiceConstants.CONSENT_ATTRIBUTES_DELETE_ERROR_MSG);
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    private void updateConsentAttributes(Connection connection, ConsentCoreDAO consentCoreDAO,
                                         String consentID, Map<String, String> consentAttributes)
            throws OBConsentDataDeletionException, OBConsentDataInsertionException {

        // delete existing consent attributes
        if (log.isDebugEnabled()) {
            log.debug("Deleting attributes for the consent ID: " + consentID.replaceAll("[\r\n]", ""));
        }
        consentCoreDAO.deleteConsentAttributes(connection, consentID,
                new ArrayList<>(consentAttributes.keySet()));

        // store new set of consent attributes
        ConsentAttributes consentAttributesObject = new ConsentAttributes();
        consentAttributesObject.setConsentID(consentID);
        consentAttributesObject.setConsentAttributes(consentAttributes);
        if (log.isDebugEnabled()) {
            log.debug("Storing consent attributes for the consent of ID: " + consentID.replaceAll("[\r\n]", ""));
        }
        consentCoreDAO.storeConsentAttributes(connection, consentAttributesObject);
    }

    private void processAdditionalConsentAmendmentData(Connection connection, ConsentCoreDAO consentCoreDAO,
                                                       Map<String, Object> additionalAmendmentData)
            throws ConsentManagementException, OBConsentDataInsertionException {

        Map<String, AuthorizationResource> newAuthResources;
        Map<String, ArrayList<ConsentMappingResource>> newMappingResources;

        if (additionalAmendmentData.containsKey(ConsentCoreServiceConstants.ADDITIONAL_AUTHORIZATION_RESOURCES) &&
                additionalAmendmentData.containsKey(ConsentCoreServiceConstants.ADDITIONAL_MAPPING_RESOURCES)) {

            newAuthResources = (Map<String, AuthorizationResource>) additionalAmendmentData
                    .get(ConsentCoreServiceConstants.ADDITIONAL_AUTHORIZATION_RESOURCES);
            newMappingResources = (Map<String, ArrayList<ConsentMappingResource>>) additionalAmendmentData
                    .get(ConsentCoreServiceConstants.ADDITIONAL_MAPPING_RESOURCES);

            for (Map.Entry<String, AuthorizationResource> authResourceEntry : newAuthResources.entrySet()) {
                String userId = authResourceEntry.getKey();
                AuthorizationResource authResource = authResourceEntry.getValue();

                if (StringUtils.isBlank(authResource.getConsentID()) ||
                        StringUtils.isBlank(authResource.getAuthorizationType()) ||
                        StringUtils.isBlank(authResource.getAuthorizationStatus())) {
                    log.error("Consent ID, authorization type or authorization status is missing, cannot proceed");
                    throw new ConsentManagementException("Cannot proceed since consent ID, authorization type or " +
                            "authorization status is missing");
                }
                // create authorization resource
                AuthorizationResource authorizationResource =
                        consentCoreDAO.storeAuthorizationResource(connection, authResource);
                ArrayList<ConsentMappingResource> mappingResources = newMappingResources.get(userId);

                for (ConsentMappingResource mappingResource : mappingResources) {

                    if (StringUtils.isBlank(mappingResource.getAccountID()) ||
                            StringUtils.isBlank(mappingResource.getMappingStatus())) {
                        log.error("Account ID or Mapping Status is not found, cannot proceed");
                        throw new ConsentManagementException("Account ID or Mapping Status is not found, " +
                                "cannot proceed");
                    }
                    mappingResource.setAuthorizationID(authorizationResource.getAuthorizationID());
                    // create mapping resource
                    consentCoreDAO.storeConsentMappingResource(connection, mappingResource);
                }
            }
        }
    }

    @Override
    public boolean storeConsentAmendmentHistory(String consentID, ConsentHistoryResource consentHistoryResource,
                    DetailedConsentResource detailedCurrentConsent) throws ConsentManagementException {

        if (StringUtils.isBlank(consentID) || consentHistoryResource == null ||
                StringUtils.isBlank(consentHistoryResource.getReason()) || consentHistoryResource.getTimestamp() == 0) {
            log.error("Consent ID or detailed consent resource or amendment reason or amended timestamp " +
                    "in consent history resource is empty/zero");
            throw new ConsentManagementException("Consent ID or detailed consent resource or amendment reason or " +
                    "amended timestamp in consent resource history is empty/zero, cannot proceed");
        }

        String historyID = consentHistoryResource.getHistoryID();
        if (StringUtils.isBlank(historyID)) {
            historyID = String.valueOf(UUID.randomUUID());
        }
        long amendedTimestamp = consentHistoryResource.getTimestamp();
        String amendmentReason = consentHistoryResource.getReason();

        Connection connection = DatabaseUtil.getDBConnection();
        ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
        try {
            if (detailedCurrentConsent == null) {
                detailedCurrentConsent = consentCoreDAO.getDetailedConsentResource(connection, consentID, false);
            }

            DetailedConsentResource detailedHistoryConsent = consentHistoryResource.getDetailedConsentResource();
            // store only the changes in basic consent data to CA history
            JSONObject changedConsentDataJson = getChangedBasicConsentDataJSON(detailedCurrentConsent,
                    detailedHistoryConsent);
            if (!changedConsentDataJson.isEmpty()) {
                consentCoreDAO.storeConsentAmendmentHistory(connection, historyID, amendedTimestamp, consentID,
                        ConsentMgtDAOConstants.TYPE_CONSENT_BASIC_DATA, String.valueOf(changedConsentDataJson),
                        amendmentReason);
            }

            // store only the changes in consent attributes to CA history
            JSONObject changedConsentAttributesJson = getChangedConsentAttributesDataJSON(
                    detailedCurrentConsent.getConsentAttributes(), detailedHistoryConsent.getConsentAttributes());
            if (!changedConsentAttributesJson.isEmpty()) {
                consentCoreDAO.storeConsentAmendmentHistory(connection, historyID, amendedTimestamp,
                        consentID, ConsentMgtDAOConstants.TYPE_CONSENT_ATTRIBUTES_DATA,
                        String.valueOf(changedConsentAttributesJson), amendmentReason);
            }

            // store only the changes in consent mappings to CA history
            Map<String, JSONObject> changedConsentMappingsJsonDataMap =
                    getChangedConsentMappingDataJSONMap(detailedCurrentConsent.getConsentMappingResources(),
                            detailedHistoryConsent.getConsentMappingResources());
            for (Map.Entry<String, JSONObject> changedConsentMapping : changedConsentMappingsJsonDataMap.entrySet()) {
                consentCoreDAO.storeConsentAmendmentHistory(connection, historyID, amendedTimestamp,
                        changedConsentMapping.getKey(), ConsentMgtDAOConstants.TYPE_CONSENT_MAPPING_DATA,
                        String.valueOf(changedConsentMapping.getValue()), amendmentReason);
            }

            // store only the changes in consent Auth Resources to CA history
            Map<String, JSONObject> changedConsentAuthResourcesJsonDataMap =
                    getChangedConsentAuthResourcesDataJSONMap(detailedCurrentConsent.getAuthorizationResources(),
                            detailedHistoryConsent.getAuthorizationResources());
            for (Map.Entry<String, JSONObject> changedConsentAuthResource :
                    changedConsentAuthResourcesJsonDataMap.entrySet()) {
                consentCoreDAO.storeConsentAmendmentHistory(connection, historyID, amendedTimestamp,
                        changedConsentAuthResource.getKey(), ConsentMgtDAOConstants.TYPE_CONSENT_AUTH_RESOURCE_DATA,
                        String.valueOf(changedConsentAuthResource.getValue()), amendmentReason);
            }

            // Commit transactions
            DatabaseUtil.commitTransaction(connection);
            log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
            return true;
        } catch (OBConsentDataRetrievalException e) {
            log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
        } catch (OBConsentDataInsertionException e) {
            log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
            DatabaseUtil.rollbackTransaction(connection);
            throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    private JSONObject getChangedBasicConsentDataJSON(DetailedConsentResource newConsentResource,
                                                      DetailedConsentResource oldConsentResource) {

        JSONObject changedConsentDataJson = new JSONObject();
        if (!newConsentResource.getReceipt().equalsIgnoreCase(oldConsentResource.getReceipt())) {
            changedConsentDataJson.put(ConsentMgtDAOConstants.RECEIPT, oldConsentResource.getReceipt());
        }
        if (newConsentResource.getValidityPeriod() != oldConsentResource.getValidityPeriod()) {
            changedConsentDataJson.put(ConsentMgtDAOConstants.VALIDITY_TIME,
                    String.valueOf(oldConsentResource.getValidityPeriod()));
        }
        if (newConsentResource.getUpdatedTime() != oldConsentResource.getUpdatedTime()) {
            changedConsentDataJson.put(ConsentMgtDAOConstants.UPDATED_TIME,
                    String.valueOf(oldConsentResource.getUpdatedTime()));
        }
        if (!newConsentResource.getCurrentStatus().equalsIgnoreCase(oldConsentResource.getCurrentStatus())) {
            changedConsentDataJson.put(ConsentMgtDAOConstants.CURRENT_STATUS,
                    String.valueOf(oldConsentResource.getCurrentStatus()));
        }
        return changedConsentDataJson;
    }

    private JSONObject getChangedConsentAttributesDataJSON(Map<String, String> newConsentAttributes,
                                                           Map<String, String> oldConsentAttributes) {

        JSONObject changedConsentAttributesJson = new JSONObject();
        for (Map.Entry<String, String> consentAttribute : oldConsentAttributes.entrySet()) {
            String attributeKey = consentAttribute.getKey();
            if (!newConsentAttributes.containsKey(attributeKey)
                    || !newConsentAttributes.get(attributeKey).equalsIgnoreCase(consentAttribute.getValue())) {
                //store only the consent attributes with a changed value to the consent amendment history
                changedConsentAttributesJson.put(attributeKey, consentAttribute.getValue());
            }
        }
        for (Map.Entry<String, String> newConsentAttribute : newConsentAttributes.entrySet()) {
            String attributeKey = newConsentAttribute.getKey();
            if (!oldConsentAttributes.containsKey(newConsentAttribute.getKey())) {
                //store any new consent attribute in current consent to the changedConsentAttributesJson of
                //the immediate past consent amendment history with a null value
                changedConsentAttributesJson.put(attributeKey, null);
            }
        }
        return changedConsentAttributesJson;
    }

    private Map<String, JSONObject> getChangedConsentMappingDataJSONMap(ArrayList<ConsentMappingResource>
                                         newConsentMappings, ArrayList<ConsentMappingResource> oldConsentMappings) {

        Map<String, JSONObject> changedConsentMappingsJsonDataMap = new HashMap<>();
        ArrayList<String> existingConsentMappingIds = new ArrayList<>();
        for (ConsentMappingResource newMapping : newConsentMappings) {
            JSONObject changedConsentMappingJson = new JSONObject();
            for (ConsentMappingResource oldMapping : oldConsentMappings) {
                if (newMapping.getMappingID().equalsIgnoreCase(oldMapping.getMappingID())) {
                    existingConsentMappingIds.add(newMapping.getMappingID());
                    if (!newMapping.getMappingStatus().equalsIgnoreCase(oldMapping.getMappingStatus())) {
                        //store only the mapping-ids with a changed Mapping Status to the consent amendment history
                        changedConsentMappingJson.put(ConsentMgtDAOConstants.MAPPING_STATUS,
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

    private Map<String, JSONObject> getChangedConsentAuthResourcesDataJSONMap(ArrayList<AuthorizationResource>
                            newConsentAuthResources, ArrayList<AuthorizationResource> oldConsentAuthResources) {

        Map<String, JSONObject> changedConsentAuthResourcesJsonDataMap = new HashMap<>();

        ArrayList<String> existingConsentAuthResourceIds = new ArrayList<>();
        for (AuthorizationResource newAuthResource : newConsentAuthResources) {
            for (AuthorizationResource oldAuthResource : oldConsentAuthResources) {
                if (newAuthResource.getAuthorizationID().equalsIgnoreCase(oldAuthResource.getAuthorizationID())) {
                    existingConsentAuthResourceIds.add(newAuthResource.getAuthorizationID());
                    break;
                }
            }
            // store any new authorization-ids in current consent (an Auth Resource not available in previous consent,
            // but newly added in current consent) to the immediate past consent amendment history with 'null' value
            if (!existingConsentAuthResourceIds.contains(newAuthResource.getAuthorizationID())) {
                changedConsentAuthResourcesJsonDataMap.put(newAuthResource.getAuthorizationID(), null);
            }
        }
        return changedConsentAuthResourcesJsonDataMap;
    }

    @Override
    public Map<String, ConsentHistoryResource> getConsentAmendmentHistoryData(String consentID)
            throws ConsentManagementException {

        if (StringUtils.isBlank(consentID)) {
            log.error("Consent ID is empty");
            throw new ConsentManagementException("Consent ID is empty, cannot proceed");
        }

        Connection connection = DatabaseUtil.getDBConnection();
        ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
        try {
            //Retrieve the current detailed consent to build the detailed consent amendment history resources
            DetailedConsentResource currentConsentResource =
                    consentCoreDAO.getDetailedConsentResource(connection, consentID, false);

            Map<String, ConsentHistoryResource> consentAmendmentHistoryRetrievalResult =
                    consentCoreDAO.retrieveConsentAmendmentHistory(connection,
                            getRecordIdListForConsentHistoryRetrieval(currentConsentResource));

            Map<String, ConsentHistoryResource> consentAmendmentHistory = new LinkedHashMap<>();
            if (!consentAmendmentHistoryRetrievalResult.isEmpty()) {
                consentAmendmentHistory = processConsentAmendmentHistoryData(
                        consentAmendmentHistoryRetrievalResult, currentConsentResource);
            }
            return consentAmendmentHistory;
        } catch (OBConsentDataRetrievalException e) {
            log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public boolean syncRetentionDatabaseWithPurgedConsent() throws ConsentManagementException {

        if (!OpenBankingConfigParser.getInstance().isConsentDataRetentionEnabled()) {
            log.error("Consent data retention is not enabled, Hence data sync is not possible at the moment");
            throw new ConsentManagementException("Consent data retention is not enabled, " +
                    "Hence data sync is not possible at the moment");
        }

        Connection consentDBConnection = DatabaseUtil.getDBConnection();
        Connection retentionDBConnection = DatabaseUtil.getRetentionDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            ConsentCoreDAO consentRetentionDAO = ConsentStoreInitializer.getInitializedConsentRetentionDAOImpl();

            // Fetch list of consent_id's to sync from temporary retention tables in consent DB.
            ArrayList<String> listOfConsentIds = consentCoreDAO.getListOfConsentIds(consentDBConnection, true);

            // Fetch consent data from temporary retention tables in consent DB.
            log.debug("Fetching consent data from temporary retention tables in consent DB");
            for (String consentId : listOfConsentIds) {
                Savepoint retentionDBSavepoint = retentionDBConnection.setSavepoint();
                Savepoint consentDBSavepoint = consentDBConnection.setSavepoint();
                try {
                    // Fetching detailed consent.
                    DetailedConsentResource detailedConsent =
                            consentCoreDAO.getDetailedConsentResource(consentDBConnection, consentId, true);
                    ConsentResource consentResource = new ConsentResource(detailedConsent.getConsentID(),
                            detailedConsent.getClientID(), detailedConsent.getReceipt(),
                            detailedConsent.getConsentType(), detailedConsent.getConsentFrequency(),
                            detailedConsent.getValidityPeriod(), detailedConsent.isRecurringIndicator(),
                            detailedConsent.getCurrentStatus(), detailedConsent.getCreatedTime(),
                            detailedConsent.getUpdatedTime());

                    ConsentFile consentFile = null;
                    ArrayList<ConsentStatusAuditRecord> consentStatusAuditRecords = null;
                    try {
                        // Fetching consent file.
                        consentFile = consentCoreDAO.getConsentFile(consentDBConnection, consentId, true);
                    } catch (OBConsentDataRetrievalException e) {
                        log.error(String.format("Error occurred fetching consent file for consent_id : %s , " +
                                "Ignoring this as null consent file for given consent_id",
                                consentId.replaceAll("[\r\n]", "")));
                    }
                    try {
                        // Fetching consent audit records.
                        ArrayList<String> consentIds = new ArrayList<>();
                        consentIds.add(consentId);
                        consentStatusAuditRecords =
                                consentCoreDAO.getConsentStatusAuditRecordsByConsentId(consentDBConnection, consentIds,
                                         null, null, true);
                    } catch (OBConsentDataRetrievalException e) {
                        log.error(String.format("Error occurred fetching consent audit records for consent_id : %s , " +
                                "Ignoring this as null consent audit records for given consent_id",
                                consentId.replaceAll("[\r\n]", "")));
                    }

                    // Inserting to retention datasource
                    ConsentResource insertedConsentResources =
                            consentRetentionDAO.storeConsentResource(retentionDBConnection, consentResource);
                    if (insertedConsentResources == null) {
                        throw new OBConsentDataInsertionException(ConsentCoreServiceConstants.
                                DATA_INSERTION_ROLLBACK_ERROR_MSG + " for consent resource");
                    }
                    for (AuthorizationResource authResource : detailedConsent.getAuthorizationResources()) {
                        if (authResource.getAuthorizationID() != null) {
                            AuthorizationResource storeAuthorizationResource =
                                    consentRetentionDAO.storeAuthorizationResource(retentionDBConnection, authResource);
                            if (storeAuthorizationResource == null) {
                                throw new OBConsentDataInsertionException(ConsentCoreServiceConstants.
                                        DATA_INSERTION_ROLLBACK_ERROR_MSG + " for authorization resources");
                            }
                        }
                    }
                    for (ConsentMappingResource mappingResource : detailedConsent.getConsentMappingResources()) {
                        ConsentMappingResource storeConsentMappingResource =
                                consentRetentionDAO.storeConsentMappingResource(retentionDBConnection, mappingResource);
                        if (storeConsentMappingResource == null) {
                            throw new OBConsentDataInsertionException(ConsentCoreServiceConstants.
                                    DATA_INSERTION_ROLLBACK_ERROR_MSG + " for mapping resources");
                        }
                    }
                    if (!detailedConsent.getConsentAttributes().isEmpty()) {
                        ConsentAttributes consentAttributes = new ConsentAttributes(consentId,
                                detailedConsent.getConsentAttributes());
                        if (!consentRetentionDAO.storeConsentAttributes(retentionDBConnection, consentAttributes)) {
                            throw new OBConsentDataInsertionException(ConsentCoreServiceConstants.
                                    DATA_INSERTION_ROLLBACK_ERROR_MSG + " for consent attributes");
                        }
                    }
                    if (consentFile != null) {
                        if (!consentRetentionDAO.storeConsentFile(retentionDBConnection, consentFile)) {
                            throw new OBConsentDataInsertionException(ConsentCoreServiceConstants.
                                    DATA_INSERTION_ROLLBACK_ERROR_MSG + " for consent file");
                        }
                    }
                    if (consentStatusAuditRecords != null) {
                        for (ConsentStatusAuditRecord auditRecords : consentStatusAuditRecords) {
                            ConsentStatusAuditRecord storeConsentStatusAuditRecord = consentRetentionDAO
                                    .storeConsentStatusAuditRecord(retentionDBConnection, auditRecords);
                            if (storeConsentStatusAuditRecord == null) {
                                throw new OBConsentDataInsertionException(ConsentCoreServiceConstants.
                                        DATA_INSERTION_ROLLBACK_ERROR_MSG + " for consent audit records");
                            }
                        }
                    }

                    // Removing consent data from temporary retention table in consent database
                    boolean consentDeleted = consentCoreDAO.deleteConsentData(consentDBConnection, consentId, true);
                    if (!consentDeleted) {
                        throw new OBConsentDataDeletionException(ConsentCoreServiceConstants.
                                DATA_DELETE_ROLLBACK_ERROR_MSG + " for consent data deletion");
                    }
                    // Commit transactions
                    DatabaseUtil.commitTransaction(retentionDBConnection);
                    DatabaseUtil.commitTransaction(consentDBConnection);
                    log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                } catch (OBConsentDataRetrievalException | OBConsentDataInsertionException |
                        OBConsentDataDeletionException e) {
                    log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
                    consentDBConnection.rollback(consentDBSavepoint);
                    retentionDBConnection.rollback(retentionDBSavepoint);
                }
            }
            return true;
        } catch (OBConsentDataRetrievalException | SQLException e) {
            log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            throw new ConsentManagementException("Error occurred while syncing the retention data in consent " +
                    "database to retention database", e);
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(consentDBConnection);
            DatabaseUtil.closeConnection(retentionDBConnection);
        }
    }

    @Override
    public ArrayList<ConsentStatusAuditRecord> getConsentStatusAuditRecords(ArrayList<String> consentIDs,
                                                                            Integer limit, Integer offset,
                                                                            boolean fetchFromRetentionDatabase)
            throws ConsentManagementException {

        if (!OpenBankingConfigParser.getInstance().isConsentDataRetentionEnabled() && fetchFromRetentionDatabase) {
            log.error("Consent data retention is not enabled.");
            throw new ConsentManagementException("Consent data retention is not enabled.");
        }

        Connection connection;
        if (fetchFromRetentionDatabase) {
            connection = DatabaseUtil.getRetentionDBConnection();
        } else {
            connection = DatabaseUtil.getDBConnection();
        }

        ConsentCoreDAO consentCoreDAO;
        if (fetchFromRetentionDatabase) {
            consentCoreDAO = ConsentStoreInitializer.getInitializedConsentRetentionDAOImpl();
            log.debug("Fetching consent status audit records from retention datasource");
        } else {
            consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
        }

        try {
            //Retrieve consent status audit records.
            return consentCoreDAO.getConsentStatusAuditRecordsByConsentId(connection, consentIDs, limit, offset,
                    false);

        } catch (OBConsentDataRetrievalException e) {
            log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public ConsentFile getConsentFile(String consentId, boolean fetchFromRetentionDatabase)
            throws ConsentManagementException {

        if (StringUtils.isBlank(consentId)) {
            log.error("Consent ID is empty");
            throw new ConsentManagementException("Consent ID is empty, cannot proceed");
        }

        if (!OpenBankingConfigParser.getInstance().isConsentDataRetentionEnabled() && fetchFromRetentionDatabase) {
            log.error("Consent data retention is not enabled.");
            throw new ConsentManagementException("Consent data retention is not enabled.");
        }

        Connection connection;
        if (fetchFromRetentionDatabase) {
            connection = DatabaseUtil.getRetentionDBConnection();
        } else {
            connection = DatabaseUtil.getDBConnection();
        }

        ConsentCoreDAO consentCoreDAO;
        if (fetchFromRetentionDatabase) {
            consentCoreDAO = ConsentStoreInitializer.getInitializedConsentRetentionDAOImpl();
            log.debug("Fetching consent file from retention datasource");
        } else {
            consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
        }

        try {
            //Retrieve consent status audit records.
            return consentCoreDAO.getConsentFile(connection, consentId, false);

        } catch (OBConsentDataRetrievalException e) {
            log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    private List<String> getRecordIdListForConsentHistoryRetrieval(DetailedConsentResource detailedConsentResource) {

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

    private Map<String, ConsentHistoryResource> processConsentAmendmentHistoryData(
                            Map<String, ConsentHistoryResource> consentAmendmentHistoryRetrievalResult,
                            DetailedConsentResource currentConsentResource) throws ConsentManagementException {

        Gson gson = new Gson();
        Map<String, ConsentHistoryResource> consentAmendmentHistoryDataMap = new LinkedHashMap<>();
        DetailedConsentResource detailedConsentHistoryResource = gson.fromJson(gson.toJson(currentConsentResource),
                DetailedConsentResource.class);

        for (Map.Entry<String, ConsentHistoryResource> consentHistoryDataEntry :
                                                consentAmendmentHistoryRetrievalResult.entrySet()) {
            String historyId = consentHistoryDataEntry.getKey();
            ConsentHistoryResource consentHistoryResource = consentHistoryDataEntry.getValue();

            for (Map.Entry<String, Object> consentHistoryDataTypeEntry :
                    consentHistoryResource.getChangedAttributesJsonDataMap().entrySet()) {
                String consentDataType = consentHistoryDataTypeEntry.getKey();
                Object changedAttributes = consentHistoryDataTypeEntry.getValue();

                if (ConsentMgtDAOConstants.TYPE_CONSENT_BASIC_DATA.equalsIgnoreCase(consentDataType)) {
                    JSONObject changedValuesJSON = parseChangedAttributeJsonString(changedAttributes.toString());
                    if (changedValuesJSON.containsKey(ConsentMgtDAOConstants.RECEIPT)) {
                        detailedConsentHistoryResource.setReceipt(
                                (String) changedValuesJSON.get(ConsentMgtDAOConstants.RECEIPT));
                    }
                    if (changedValuesJSON.containsKey(ConsentMgtDAOConstants.VALIDITY_TIME)) {
                        detailedConsentHistoryResource.setValidityPeriod(Long.parseLong((String)
                                changedValuesJSON.get(ConsentMgtDAOConstants.VALIDITY_TIME)));
                    }
                    if (changedValuesJSON.containsKey(ConsentMgtDAOConstants.UPDATED_TIME)) {
                        detailedConsentHistoryResource.setUpdatedTime(Long.parseLong((String)
                                changedValuesJSON.get(ConsentMgtDAOConstants.UPDATED_TIME)));
                    }
                    if (changedValuesJSON.containsKey(ConsentMgtDAOConstants.CURRENT_STATUS)) {
                        detailedConsentHistoryResource.setCurrentStatus((String)
                                changedValuesJSON.get(ConsentMgtDAOConstants.CURRENT_STATUS));
                    }

                } else if (ConsentMgtDAOConstants.TYPE_CONSENT_ATTRIBUTES_DATA.equalsIgnoreCase(consentDataType)) {
                    JSONObject changedValuesJSON = parseChangedAttributeJsonString(changedAttributes.toString());
                    for (Map.Entry<String, Object> attribute : changedValuesJSON.entrySet()) {
                        Object attributeValue = attribute.getValue();
                        if (attributeValue == null) {
                            //Ignore the consent attribute from the consent history if it's value is stored as null
                            detailedConsentHistoryResource.getConsentAttributes().remove(attribute.getKey());
                        } else {
                            detailedConsentHistoryResource.getConsentAttributes().put(attribute.getKey(),
                                    attributeValue.toString());
                        }
                    }

                } else if (ConsentMgtDAOConstants.TYPE_CONSENT_MAPPING_DATA.equalsIgnoreCase(consentDataType)) {
                    Map<String, Object> changedConsentMappingsDataMap = (Map<String, Object>) changedAttributes;
                    ArrayList<ConsentMappingResource> consentMappings =
                            detailedConsentHistoryResource.getConsentMappingResources();
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
                                    changedValuesJSON.get(ConsentMgtDAOConstants.MAPPING_STATUS).toString());
                        }
                        consentMappingsHistory.add(gson.fromJson(gson.toJson(mapping), ConsentMappingResource.class));
                    }
                    detailedConsentHistoryResource.setConsentMappingResources(consentMappingsHistory);

                } else if (ConsentMgtDAOConstants.TYPE_CONSENT_AUTH_RESOURCE_DATA.equalsIgnoreCase(consentDataType)) {
                    Map<String, Object> changedConsentAuthResourceDataMap = (Map<String, Object>) changedAttributes;
                    ArrayList<AuthorizationResource> consentAuthResources = detailedConsentHistoryResource
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
                    detailedConsentHistoryResource.setAuthorizationResources(consentAuthResourceHistory);
                }
            }
            consentHistoryResource.setDetailedConsentResource(gson.fromJson(gson.toJson(detailedConsentHistoryResource),
                    DetailedConsentResource.class));
            consentAmendmentHistoryDataMap.put(historyId, consentHistoryResource);
        }
        return consentAmendmentHistoryDataMap;
    }

    private JSONObject parseChangedAttributeJsonString(String changedAttributes) throws ConsentManagementException {

        Object changedValues;
        try {
            changedValues = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(changedAttributes);
        } catch (ParseException e) {
            throw new ConsentManagementException("Changed Values is not a valid JSON object", e);
        }
        if (changedValues == null) {
            return new JSONObject();
        }
        return (JSONObject) changedValues;

    }

    public void revokeTokens(DetailedConsentResource detailedConsentResource, String userID)
            throws IdentityOAuth2Exception {

        OAuth2Service oAuth2Service = getOAuth2Service();
        String clientId = detailedConsentResource.getClientID();
        String consentId = detailedConsentResource.getConsentID();
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(userID);
        Set<AccessTokenDO> accessTokenDOSet = getAccessTokenDOSet(detailedConsentResource, authenticatedUser);

        String consentIdClaim = OpenBankingConfigParser.getInstance().getConfiguration()
                .get(OpenBankingConstants.CONSENT_ID_CLAIM_NAME).toString();

        if (!accessTokenDOSet.isEmpty()) {
            Set<String> activeTokens = new HashSet<>();
            // Get tokens to revoke to an array
            for (AccessTokenDO accessTokenDO : accessTokenDOSet) {
                // Filter tokens by consent ID claim
                if (Arrays.asList(accessTokenDO.getScope()).contains(consentIdClaim +
                        detailedConsentResource.getConsentID())) {
                    activeTokens.add(accessTokenDO.getAccessToken());
                }
            }

            if (!activeTokens.isEmpty()) {
                // set authorization context details for the given user
                OAuthClientAuthnContext oAuthClientAuthnContext = new OAuthClientAuthnContext();
                oAuthClientAuthnContext.setAuthenticated(true);
                oAuthClientAuthnContext.setClientId(clientId);
                oAuthClientAuthnContext.addParameter(OpenBankingConstants.IS_CONSENT_REVOCATION_FLOW, true);

                // set common properties of token revocation request
                OAuthRevocationRequestDTO revokeRequestDTO = new OAuthRevocationRequestDTO();
                revokeRequestDTO.setOauthClientAuthnContext(oAuthClientAuthnContext);
                revokeRequestDTO.setConsumerKey(clientId);
                revokeRequestDTO.setTokenType(GrantType.REFRESH_TOKEN.toString());

                for (String activeToken : activeTokens) {
                    // set access token to be revoked
                    revokeRequestDTO.setToken(activeToken);
                    OAuthRevocationResponseDTO oAuthRevocationResponseDTO =
                            revokeTokenByClient(oAuth2Service, revokeRequestDTO);

                    if (oAuthRevocationResponseDTO.isError()) {
                        log.error("Error while revoking access token for consent ID: "
                                + consentId.replaceAll("[\r\n]", ""));
                        throw new IdentityOAuth2Exception(
                                String.format("Error while revoking access token for consent ID: %s. Caused by, %s",
                                        consentId, oAuthRevocationResponseDTO.getErrorMsg()));
                    }
                }
            }
        }
    }

    private boolean isValidUserID(String requestUserID, String consentUserID) {
        if (StringUtils.isEmpty(requestUserID)) {
            // userId not present in request query parameters, can use consentUserID to revoke tokens
            return true;
        }
        return requestUserID.equals(consentUserID);
    }

    @Generated(message = "Excluded from code coverage since used for testing purposes")
    OAuth2Service getOAuth2Service() {

        return ConsentManagementDataHolder.getInstance().getOAuth2Service();
    }

    @Generated(message = "Excluded from code coverage since used for testing purposes")
    AuthenticatedUser getAuthenticatedUser(String userID) throws IdentityOAuth2Exception {
        // set domain name
        if (UserCoreUtil.getDomainFromThreadLocal() == null) {
            UserCoreUtil.setDomainInThreadLocal(UserCoreUtil.extractDomainFromName(userID));
        }
        if (OpenBankingConfigParser.getInstance().isPSUFederated()) {
            AuthenticatedUser authenticatedUser =
                    AuthenticatedUser.createFederateAuthenticatedUserFromSubjectIdentifier(userID);
            authenticatedUser.setUserStoreDomain(OAuth2Util.getUserStoreForFederatedUser(authenticatedUser));
            authenticatedUser.setTenantDomain(MultitenantUtils.getTenantDomain(userID));
            authenticatedUser.setFederatedIdPName(OpenBankingConfigParser.getInstance().getFederatedIDPName());
            authenticatedUser.setUserName(MultitenantUtils.getTenantAwareUsername(userID));
            return authenticatedUser;
        } else {
            return AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier(userID);
        }
    }

    @Generated(message = "Excluded from code coverage since used for testing purposes")
    Set<AccessTokenDO> getAccessTokenDOSet(DetailedConsentResource detailedConsentResource,
                                           AuthenticatedUser authenticatedUser) throws IdentityOAuth2Exception {

        return OAuthTokenPersistenceFactory.getInstance().getAccessTokenDAO()
                .getAccessTokens(detailedConsentResource.getClientID(), authenticatedUser,
                        authenticatedUser.getUserStoreDomain(), false);
    }

    @Generated(message = "Excluded from code coverage since used for testing purposes")
    OAuthRevocationResponseDTO revokeTokenByClient(OAuth2Service oAuth2Service,
                                                   OAuthRevocationRequestDTO revocationRequestDTO) {

        return oAuth2Service.revokeTokenByOAuthClient(revocationRequestDTO);
    }

    @Override
    public ConsentResource updateConsentStatus(String consentId, String newConsentStatus)
            throws ConsentManagementException {

        if (StringUtils.isBlank(consentId) || StringUtils.isBlank(newConsentStatus)) {

            log.error("Consent ID, userID or newConsentStatus is missing. Cannot proceed.");
            throw new ConsentManagementException("Cannot proceed without Consent ID, userID or newConsentStatus.");
        }

        Connection connection = DatabaseUtil.getDBConnection();
        ConsentResource updatedConsentResource;

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                // Get the existing consent to validate status
                if (log.isDebugEnabled()) {
                    log.debug("Retrieving the consent for ID:" + consentId.replaceAll("[\r\n]", "")
                            + " to validate status");
                }

                // Update consent status with new status
                if (log.isDebugEnabled()) {
                    log.debug("Updating the status of the consent for ID:" + consentId.replaceAll("[\r\n]", ""));
                }

                DetailedConsentResource existingConsentResource = consentCoreDAO
                        .getDetailedConsentResource(connection, consentId, false);
                String existingConsentStatus = existingConsentResource.getCurrentStatus();
                ArrayList<AuthorizationResource>  authResources = existingConsentResource.getAuthorizationResources();

                updatedConsentResource = consentCoreDAO.updateConsentStatus(connection, consentId, newConsentStatus);

                // Previous consent status is not added in reason because it can be null
                String auditMessage = "Consent status updated to " + newConsentStatus;
                for (AuthorizationResource authResource : authResources) {
                    // Create an audit record execute state change listener
                    HashMap<String, Object> consentDataMap = new HashMap<>();
                    consentDataMap.put(ConsentCoreServiceConstants.DETAILED_CONSENT_RESOURCE, existingConsentResource);
                    postStateChange(connection, consentCoreDAO, consentId, authResource.getUserID(), newConsentStatus,
                            existingConsentStatus, auditMessage, existingConsentResource.getClientID(), consentDataMap);
                }

                // Commit transaction
                DatabaseUtil.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return updatedConsentResource;
            } catch (OBConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            } catch (OBConsentDataInsertionException e) {
                log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
                DatabaseUtil.rollbackTransaction(connection);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
            } catch (OBConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtil.rollbackTransaction(connection);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public ArrayList<DetailedConsentResource> getConsentsEligibleForExpiration(String statusesEligibleForExpiration)
            throws ConsentManagementException {

        Connection connection = DatabaseUtil.getDBConnection();
        ArrayList<DetailedConsentResource> detailedConsentResources;

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                log.debug("Retrieving consents which has expiration time attribute.");
                detailedConsentResources = consentCoreDAO.getExpiringConsents(connection,
                        statusesEligibleForExpiration);
                // Commit transactions
                DatabaseUtil.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return detailedConsentResources;
            } catch (OBConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    private void postStateChange(Connection connection, ConsentCoreDAO consentCoreDAO, String consentID,
                                 String userID, String newConsentStatus, String previousConsentStatus, String reason,
                                 String clientId, Map<String, Object> consentDataMap)
            throws OBConsentDataInsertionException, ConsentManagementException {

        createAuditRecord(connection, consentCoreDAO, consentID, userID, newConsentStatus, previousConsentStatus,
                reason);
        ConsentStateChangeListenerImpl.getInstance().onStateChange(consentID, userID, newConsentStatus,
                previousConsentStatus, reason, clientId, consentDataMap);
    }

    public AuthorizationResource updateAuthorizationStatus(String authorizationId, String newAuthorizationStatus)
            throws ConsentManagementException {

        if (StringUtils.isBlank(authorizationId) || StringUtils.isBlank(newAuthorizationStatus)) {

            log.error("Authorization ID or newAuthorizationStatus is missing. Cannot proceed.");
            throw new ConsentManagementException("Cannot proceed without Authorization ID or newAuthorizationStatus" +
                    ".");
        }

        Connection connection = DatabaseUtil.getDBConnection();
        AuthorizationResource updatedAuthorizationResource;

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                // Update authorization status with new status
                if (log.isDebugEnabled()) {
                    log.debug("Updating the status of the authorization for ID:" +
                            authorizationId.replaceAll("[\r\n]", ""));
                }
                updatedAuthorizationResource = consentCoreDAO.updateAuthorizationStatus(connection, authorizationId,
                        newAuthorizationStatus);

                // Commit transaction
                DatabaseUtil.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return updatedAuthorizationResource;
            } catch (OBConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtil.rollbackTransaction(connection);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

    public void updateAuthorizationUser(String authorizationID, String userID)
            throws ConsentManagementException {

        if (StringUtils.isBlank(authorizationID) || StringUtils.isBlank(userID)) {

            log.error("Authorization ID or user ID is missing. Cannot proceed.");
            throw new ConsentManagementException("Cannot proceed without Authorization ID or UserID.");
        }

        Connection connection = DatabaseUtil.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                // Updating the authorized user
                if (log.isDebugEnabled()) {
                    log.debug("Updating the status of the user for authorization ID:" +
                            authorizationID.replaceAll("[\r\n]", ""));
                }
                consentCoreDAO.updateAuthorizationUser(connection, authorizationID,
                        userID);

                // Commit transaction
                DatabaseUtil.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return;
            } catch (OBConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtil.rollbackTransaction(connection);
                throw new ConsentManagementException(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtil.closeConnection(connection);
        }
    }

}
