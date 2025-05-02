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

package org.wso2.financial.services.accelerator.consent.mgt.service.impl;

import net.minidev.json.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
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
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentFile;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentHistoryResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentMappingResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentStatusAuditRecord;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.persistence.ConsentStoreInitializer;
import org.wso2.financial.services.accelerator.consent.mgt.dao.util.DatabaseUtils;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;
import org.wso2.financial.services.accelerator.consent.mgt.service.constants.ConsentCoreServiceConstants;
import org.wso2.financial.services.accelerator.consent.mgt.service.util.ConsentCoreServiceUtil;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.Response;


/**
 * Consent core service implementation.
 */
public class ConsentCoreServiceImpl implements ConsentCoreService {

    private static final Log log = LogFactory.getLog(ConsentCoreServiceImpl.class);

    private static ConsentCoreServiceImpl consentCoreService = null;

    public static ConsentCoreServiceImpl getInstance() {
        if (consentCoreService == null) {
            consentCoreService = new ConsentCoreServiceImpl();
        }
        return consentCoreService;
    }

    @Override
    public DetailedConsentResource createAuthorizableConsentWithBulkAuth(ConsentResource consentResource,
                                                                         ArrayList<AuthorizationResource>
                                                                                 authorizationResources) throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentResource.getClientID()) || StringUtils.isBlank(consentResource.getReceipt()) ||
                StringUtils.isBlank(consentResource.getConsentType()) ||
                StringUtils.isBlank(consentResource.getCurrentStatus())) {

            log.error(ConsentCoreServiceConstants.CANNOT_PROCEED_WITH_CONSENT_CREATION);

            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants.CANNOT_PROCEED_WITH_CONSENT_CREATION);
        }

        if (authorizationResources != null) {

            for (AuthorizationResource authorizationResource : authorizationResources) {
                if (StringUtils.isBlank(authorizationResource.getAuthorizationStatus()) ||
                        StringUtils.isBlank(authorizationResource.getAuthorizationType()) ||
                        StringUtils.isBlank(authorizationResource.getUserID())) {
                    log.error(ConsentCoreServiceConstants.CANNOT_PROCEED_WITH_IMPLICIT_AUTH);
                    throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                            ConsentCoreServiceConstants.CANNOT_PROCEED_WITH_IMPLICIT_AUTH);
                }
            }

        }

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            try {
                ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
                DetailedConsentResource detailedConsentResource = ConsentCoreServiceUtil
                        .createAuthorizableConsentWithAuditRecordWithBulkAuthResources(connection,
                                consentCoreDAO, consentResource,
                                authorizationResources);
                DatabaseUtils.commitTransaction(connection);
                return detailedConsentResource;
            } catch (ConsentDataInsertionException e) {
                log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG,
                        e);
            } catch (ConsentDataRetrievalException e) {
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }



    @Override
    public ConsentResource getConsent(String consentID, boolean withAttributes) throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentID)) {
            log.error(ConsentCoreServiceConstants.CONSENT_ID_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.CONSENT_ID_MISSING_ERROR_MSG);
        }

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                ConsentResource retrievedConsentResource;

                // Get consent attributes if needed
                if (!withAttributes) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Retrieving consent for consent ID: %s",
                                consentID.replaceAll("[\r\n]", "")));
                    }
                    retrievedConsentResource = consentCoreDAO.getConsentResource(connection, consentID);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Retrieving consent with consent attributes for consent ID: %s",
                                consentID.replaceAll("[\r\n]", "")));
                    }
                    retrievedConsentResource = consentCoreDAO.getConsentResourceWithAttributes(connection, consentID);
                }


                // Commit transactions
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return retrievedConsentResource;
            } catch (ConsentDataRetrievalException e) {
                if (e.getMessage().equals(ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG)) {
                    throw new ConsentMgtException(Response.Status.NOT_FOUND,
                            ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG, e);
                }

                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }

    @Override
    public DetailedConsentResource getDetailedConsent(String consentID) throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentID)) {
            log.error(ConsentCoreServiceConstants.CONSENT_ID_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants.CONSENT_ID_MISSING_ERROR_MSG);
        }

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {

                // Retrieve the detailed consent resource
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Retrieving detailed consent for consent ID: %s",
                            consentID.replaceAll("[\r\n]", "")));
                }
                DetailedConsentResource retrievedDetailedConsentResource = consentCoreDAO
                        .getDetailedConsentResource(connection, consentID);


                // Commit transactions
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return retrievedDetailedConsentResource;
            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                if (e.getMessage() != null) {
                    if (e.getMessage().equals(ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG)) {
                        throw new ConsentMgtException(Response.Status.NOT_FOUND,
                                ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG, e);
                    }
                }

                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }

    @Override
    public DetailedConsentResource getConsentWithAuthorizationResources(String consentID) throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentID)) {
            log.error(ConsentCoreServiceConstants.CONSENT_ID_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants.CONSENT_ID_MISSING_ERROR_MSG);
        }

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {

                // Retrieve the detailed consent resource
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Retrieving detailed consent for consent ID: %s",
                            consentID.replaceAll("[\r\n]", "")));
                }
                DetailedConsentResource retrievedDetailedConsentResource = consentCoreDAO
                        .getConsentResourceWithAuthorizationResources(connection, consentID);

                // Commit transactions
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return retrievedDetailedConsentResource;
            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                if (e.getMessage() != null) {
                    if (e.getMessage().equals(ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG)) {
                        throw new ConsentMgtException(Response.Status.NOT_FOUND,
                                ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG, e);
                    }
                }

                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }

    @Override
    public boolean createConsentFile(ConsentFile consentFileResource, String newConsentStatus, String userID,
                                     String applicableStatusToFileUpload)
            throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentFileResource.getConsentID()) ||
                StringUtils.isBlank(consentFileResource.getConsentFile())) {

            log.error(ConsentCoreServiceConstants.CONSENT_FILE_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants.CONSENT_FILE_MISSING_ERROR_MSG);
        }

        String consentID = consentFileResource.getConsentID();

        if (StringUtils.isBlank(newConsentStatus) || StringUtils.isBlank(applicableStatusToFileUpload)) {
            log.error(ConsentCoreServiceConstants.NEW_CONSENT_STATUS_OR_APPLICABLE_STATUS_MISSING_ERROR);
            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants
                            .NEW_CONSENT_STATUS_OR_APPLICABLE_STATUS_MISSING_ERROR);
        }

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                // Get the existing consent to validate status
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Retrieving the consent for ID: %s to validate status",
                            consentID.replaceAll("[\r\n]", "")));
                }
                ConsentResource existingConsentResource = consentCoreDAO.getConsentResource(connection, consentID);

                String existingConsentStatus = existingConsentResource.getCurrentStatus();

                // Validate status of the consent
                if (!applicableStatusToFileUpload.equals(existingConsentResource.getCurrentStatus())) {
                    log.error(ConsentCoreServiceConstants.CONSENT_INVALID_STATUS_ERROR_MSG);
                    throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                            ConsentCoreServiceConstants.CONSENT_INVALID_STATUS_ERROR_MSG);
                }

                // Store the consent file
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Creating the consent file for the consent of ID: %s",
                            consentID.replaceAll("[\r\n]", "")));
                }
                consentCoreDAO.storeConsentFile(connection, consentFileResource);

                // Update consent status with new status
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Updating the status of the consent for ID: %s",
                            consentID.replaceAll("[\r\n]", "")));
                }
                consentCoreDAO.updateConsentStatus(connection, consentID, newConsentStatus);

                // Create audit record and execute state change listener
                HashMap<String, Object> consentDataMap = new HashMap<>();
                consentDataMap.put(ConsentCoreServiceConstants.CONSENT_RESOURCE, existingConsentResource);
//                ConsentCoreServiceUtil.postStateChange(connection, consentCoreDAO, consentID, userID,
//                newConsentStatus,
//                        existingConsentStatus, ConsentCoreServiceConstants.CONSENT_FILE_UPLOAD_REASON,
//                        existingConsentResource.getClientID(), consentDataMap);

                // Commit transaction
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return true;
            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentMgtException(Response.Status.NOT_FOUND,
                        ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            } catch (ConsentDataInsertionException e) {
                log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
            } catch (ConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }

    @Override
    public ConsentFile getConsentFile(String consentId) throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentId)) {
            log.error(ConsentCoreServiceConstants.CONSENT_ID_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants.CONSENT_ID_MISSING_ERROR_MSG);
        }

        Connection connection = DatabaseUtils.getDBConnection();

        ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();

        try {
            //Retrieve consent status audit records.
            return consentCoreDAO.getConsentFile(connection, consentId);

        } catch (ConsentDataRetrievalException e) {
            log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }

    @Override
    public AuthorizationResource createConsentAuthorization(AuthorizationResource authorizationResource)
            throws
            ConsentMgtException {

        if (authorizationResource == null || StringUtils.isBlank(authorizationResource.getConsentID()) ||
                StringUtils.isBlank(authorizationResource.getAuthorizationType()) ||
                StringUtils.isBlank(authorizationResource.getAuthorizationStatus())) {

            log.error(ConsentCoreServiceConstants.AUTH_DETAILS_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants.AUTH_DETAILS_MISSING_ERROR_MSG);
        }

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                // Create authorization resource
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Creating authorization resource for the consent of ID: %s",
                            authorizationResource.getConsentID().replaceAll("[\r\n]", "")));
                }
                AuthorizationResource storedAuthorizationResource =
                        consentCoreDAO.storeAuthorizationResource(connection, authorizationResource);

                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return storedAuthorizationResource;
            } catch (ConsentDataInsertionException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(Response.Status.NOT_FOUND,
                        ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG,
                        e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }

    @Override
    public AuthorizationResource getAuthorizationResource(String authorizationID, String orgID) throws
            ConsentMgtException {

        if (StringUtils.isBlank(authorizationID)) {
            log.error(ConsentCoreServiceConstants.AUTH_ID_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants.AUTH_ID_MISSING_ERROR_MSG);
        }

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                AuthorizationResource retrievedAuthorizationResource;

                // Get consent file
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Retrieving authorization resource for authorization ID: %s",
                            authorizationID.replaceAll("[\r\n]", "")));
                }
                retrievedAuthorizationResource = consentCoreDAO.getAuthorizationResource(connection, authorizationID,
                        orgID);

                // Commit transactions
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return retrievedAuthorizationResource;
            } catch (ConsentDataRetrievalException e) {
                if (e.getMessage().equals(ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG)) {
                    throw new ConsentMgtException(Response.Status.NOT_FOUND, e.getMessage());
                }

                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentMgtException(Response.Status.NOT_FOUND,
                        ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG,
                        e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }

    @Override
    public ArrayList<AuthorizationResource> searchAuthorizations(String consentID)
            throws
            ConsentMgtException {
        return searchAuthorizations(consentID, null);
    }

    @Override
    public ArrayList<AuthorizationResource> searchAuthorizationsForUser(String userID)
            throws
            ConsentMgtException {
        return searchAuthorizations(null, userID);
    }

    @Override
    public ArrayList<AuthorizationResource> searchAuthorizations(String consentID, String userID)
            throws
            ConsentMgtException {

        ArrayList<AuthorizationResource> authorizationResources;
        Connection connection = DatabaseUtils.getDBConnection();

        try {
            try {
                ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();

                log.debug("Searching authorization resources");
                authorizationResources = consentCoreDAO.searchConsentAuthorizations(connection, consentID, userID);

            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.AUTH_RESOURCE_SEARCH_ERROR_MSG, e);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.AUTH_RESOURCE_SEARCH_ERROR_MSG, e);
            }

            // Commit transactions
            DatabaseUtils.commitTransaction(connection);
            log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
        return authorizationResources;
    }

    @Override
    public AuthorizationResource updateAuthorizationStatus(String authorizationId, String newAuthorizationStatus,
                                                           String orgID)
            throws
            ConsentMgtException {

        if (StringUtils.isBlank(authorizationId) || StringUtils.isBlank(newAuthorizationStatus)) {

            log.error(ConsentCoreServiceConstants.AUTH_STATUS_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants.AUTH_STATUS_MISSING_ERROR_MSG);
        }

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                // Update authorization status with new status
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Updating the status of the authorization for ID: %s",
                            authorizationId.replaceAll("[\r\n]", "")));
                }
                consentCoreDAO.updateAuthorizationStatus(connection, authorizationId, newAuthorizationStatus);
                AuthorizationResource updatedAuthorizationResource = consentCoreDAO
                        .getAuthorizationResource(connection, authorizationId, orgID);

                // Commit transaction
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return updatedAuthorizationResource;
            } catch (ConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }

    @Override
    public AuthorizationResource updateAuthorizationUser(String authorizationID, String userID, String orgID)
            throws
            ConsentMgtException {

        if (StringUtils.isBlank(authorizationID) || StringUtils.isBlank(userID)) {

            log.error(ConsentCoreServiceConstants.AUTH_USER_ID_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants.AUTH_USER_ID_MISSING_ERROR_MSG);
        }

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                // Updating the authorized user
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Updating the status of the user for authorization ID: %s",
                            authorizationID.replaceAll("[\r\n]", "")));
                }
                consentCoreDAO.updateAuthorizationUser(connection, authorizationID, userID);
                AuthorizationResource updatedAuthResource = consentCoreDAO
                        .getAuthorizationResource(connection, authorizationID, orgID);

                // Commit transaction
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return updatedAuthResource;
            } catch (ConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }

    @Override
    public boolean bindUserAccountsToConsent(ConsentResource consentResource, String userID,
                                             String authID, ArrayList<String> accountIDs,
                                             String newAuthStatus, String newCurrentConsentStatus)
            throws
            ConsentMgtException {

        Map<String, ArrayList<String>> accountIDsMapWithPermissions = new HashMap<>();
        ArrayList<String> permissionsDefault = new ArrayList<>();
        permissionsDefault.add(ConsentCoreServiceConstants.DEFAULT_PERMISSION_VALUE);

        for (String accountId : accountIDs) {
            accountIDsMapWithPermissions.put(accountId, permissionsDefault);
        }

        return bindUserAccountsToConsent(consentResource, userID, authID, accountIDsMapWithPermissions, newAuthStatus,
                newCurrentConsentStatus);
    }

    @Override
    public boolean bindUserAccountsToConsent(ConsentResource consentResource, String userID,
                                             String authID, Map<String, ArrayList<String>> accountIDsMapWithPermissions,
                                             String newAuthStatus, String newCurrentConsentStatus)
            throws
            ConsentMgtException {

        String consentID = consentResource.getConsentID();
        String clientID = consentResource.getClientID();
        String consentType = consentResource.getConsentType();

        if (StringUtils.isBlank(consentID) || StringUtils.isBlank(clientID) || StringUtils.isBlank(consentType)
                || StringUtils.isBlank(userID) || StringUtils.isBlank(authID) || StringUtils.isBlank(newAuthStatus)
                || StringUtils.isBlank(newCurrentConsentStatus)) {
            log.error(ConsentCoreServiceConstants.USER_BIND_DETAILS_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants.USER_BIND_DETAILS_MISSING_ERROR_MSG);
        }

        if (MapUtils.isEmpty(accountIDsMapWithPermissions)) {
            log.error(ConsentCoreServiceConstants.ACC_ID_PERMISSION_DETAILS_MISSING_ERROR);
            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants.ACC_ID_PERMISSION_DETAILS_MISSING_ERROR);
        }

        Connection connection = DatabaseUtils.getDBConnection();
        try {
            try {
                ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();

                // Update authorization resource of current consent
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Update authorization status and authorization user for current " +
                            "consent ID : %s", consentID.replaceAll("[\r\n]", "")));
                }
                consentCoreDAO.updateAuthorizationUser(connection, authID, userID);
                consentCoreDAO.updateAuthorizationStatus(connection, authID, newAuthStatus);

                // Create account mappings for current consent
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Creating account mappings for current consent ID: %s",
                            consentID.replaceAll("[\r\n]", "")));
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
                    log.debug(String.format("Update the status of the current consent ID: %s",
                            consentID.replaceAll("[\r\n]", "")));
                }
                consentCoreDAO.updateConsentStatus(connection, consentID, newCurrentConsentStatus);

                // Create audit record for the consent status update and execute the state change listener
//                HashMap<String, Object> consentDataMap = new HashMap<>();
//                consentDataMap.put(ConsentCoreServiceConstants.CONSENT_AMENDMENT_HISTORY_RESOURCE, consentResource);
//                ConsentCoreServiceUtil.postStateChange(connection, consentCoreDAO, consentID, userID,
//                        newCurrentConsentStatus, consentResource.getCurrentStatus(),
//                        ConsentCoreServiceConstants.USER_ACCOUNTS_BINDING_REASON, clientID, consentDataMap);

                // Commit transactions
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return true;
            } catch (ConsentDataInsertionException e) {
                log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
            } catch (ConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }

    @Override
    public DetailedConsentResource updateConsentStatus(String consentId, String newConsentStatus)
            throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentId) || StringUtils.isBlank(newConsentStatus)) {

            log.error(ConsentCoreServiceConstants.CONSENT_UPDATE_DETAILS_MISSING_ERROR);
            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants.CONSENT_UPDATE_DETAILS_MISSING_ERROR);
        }

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                // Get the existing consent to validate status
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Retrieving the consent for ID: %s to validate status",
                            consentId.replaceAll("[\r\n]", "")));
                }

                // Update consent status with new status
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Updating the status of the consent for ID: %s",
                            consentId.replaceAll("[\r\n]", "")));
                }

                consentCoreDAO.updateConsentStatus(connection, consentId, newConsentStatus);
                DetailedConsentResource existingConsentResource = consentCoreDAO
                        .getDetailedConsentResource(connection, consentId);
                String existingConsentStatus = existingConsentResource.getCurrentStatus();
                ArrayList<AuthorizationResource> authResources = existingConsentResource.getAuthorizationResources();

                // Previous consent status is not added in reason because it can be null
                String auditMessage = "Consent status updated to " + newConsentStatus;
                for (AuthorizationResource authResource : authResources) {
                    // Create an audit record execute state change listener
                    HashMap<String, Object> consentDataMap = new HashMap<>();
                    consentDataMap.put(ConsentCoreServiceConstants.DETAILED_CONSENT_RESOURCE, existingConsentResource);
                    ConsentCoreServiceUtil.postStateChange(connection, consentCoreDAO, consentId,
                            authResource.getUserID(), newConsentStatus, existingConsentStatus, auditMessage,
                            existingConsentResource.getClientID(), consentDataMap);
                }

                // Commit transaction
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);

                return existingConsentResource;
            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            } catch (ConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            } catch (ConsentDataInsertionException e) {
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }

    @Override
    public void updateConsentStatusWithImplicitReasonAndUserId(String consentId,
                                                               String newConsentStatus,
                                                               String reason, String userID, String orgID) throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentId) || StringUtils.isBlank(newConsentStatus) || StringUtils.isBlank(userID)) {

            log.error(ConsentCoreServiceConstants.CONSENT_UPDATE_DETAILS_MISSING_ERROR);
            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants.CONSENT_UPDATE_DETAILS_MISSING_ERROR);
        }

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                // Get the existing consent to validate status
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Retrieving the consent for ID: %s to validate status",
                            consentId.replaceAll("[\r\n]", "")));
                }

                // Update consent status with new status
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Updating the status of the consent for ID: %s",
                            consentId.replaceAll("[\r\n]", "")));
                }
                DetailedConsentResource existingConsentResource = consentCoreDAO
                        .getDetailedConsentResource(connection, consentId);


                if (!ConsentCoreServiceUtil.validateOrgInfo(orgID, existingConsentResource.getOrgID())) {
                    log.error("OrgInfo does not match");
                    throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                            "OrgInfo does not match, please provide the correct OrgInfo");
                }
                String previousConsentStatus = existingConsentResource.getCurrentStatus();
                if (previousConsentStatus.equals("revoked")) {

                    throw  new ConsentMgtException(Response.Status.BAD_REQUEST, "consent already revoked");
                }

                //
                consentCoreDAO.updateConsentStatus(connection, consentId, newConsentStatus);
                String existingConsentStatus = existingConsentResource.getCurrentStatus();
                ArrayList<AuthorizationResource> authResources = existingConsentResource.getAuthorizationResources();

                // Previous consent status is not added in reason because it can be null
                String auditMessage = "Consent status updated to " + newConsentStatus;

                //TODO: it correct?

                // Create an audit record execute state change listener
                HashMap<String, Object> consentDataMap = new HashMap<>();
                consentDataMap.put(ConsentCoreServiceConstants.CONSENT_AMENDMENT_HISTORY_RESOURCE,
                        existingConsentResource);
                DetailedConsentResource newConsentResource = existingConsentResource.clone();
                newConsentResource.setCurrentStatus(newConsentStatus);

                consentDataMap.put(ConsentCoreServiceConstants.DETAILED_CONSENT_RESOURCE, newConsentResource);
                ConsentCoreServiceUtil.postStateChange(connection, consentCoreDAO, consentId,
                        userID, newConsentStatus, existingConsentStatus, auditMessage,
                        existingConsentResource.getClientID(), consentDataMap);


                // Commit transaction
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                existingConsentResource.setCurrentStatus(newConsentStatus);

            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                if (e.getMessage().equals(ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG)) {
                    throw new ConsentMgtException(Response.Status.NOT_FOUND,
                            ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG, e);
                }
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            } catch (ConsentDataInsertionException e) {
                log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
            } catch (ConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }

    @Override
    public void bulkUpdateConsentStatus(String orgID, String clientId, String status, String reason, String userId,
                                        String consentType, ArrayList<String> applicableExistingStatus) throws
            ConsentMgtException {


        ArrayList<String> clientIds = new ArrayList<>();
        if (clientId != null) {
            clientIds.add(clientId);
        }
        ArrayList<String> userIds = new ArrayList<>();
        if (userId != null) {
            userIds.add(userId);
        }
        ArrayList<String> consentTypes = new ArrayList<>();

        if (consentType != null) {
            consentTypes.add(consentType);
        }
        ArrayList<DetailedConsentResource> detailedConsentResources;


        // get consents by client id and update status
        Connection connection = DatabaseUtils.getDBConnection();
        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                detailedConsentResources = consentCoreDAO.searchConsents(connection, orgID, new ArrayList<>(),
                        clientIds,
                        consentTypes, applicableExistingStatus, null, null,
                        null, null, null);

            } catch (ConsentDataRetrievalException e) {
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }

        if (detailedConsentResources.isEmpty()) {
            throw new ConsentMgtException(Response.Status.NOT_FOUND,
                    ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG);
        }
        for (DetailedConsentResource consent : detailedConsentResources) {
            updateConsentStatusWithImplicitReasonAndUserId(consent.getConsentID(), status, reason, userId, orgID);

        }


    }


    @Override
    public ArrayList<ConsentMappingResource> createConsentAccountMappings(String authID, Map<String,
            ArrayList<String>> accountIDsMapWithPermissions) throws
            ConsentMgtException {

        if (StringUtils.isBlank(authID) || MapUtils.isEmpty(accountIDsMapWithPermissions)) {
            log.error(ConsentCoreServiceConstants.ACC_MAPPING_DETAILS_MISSING_ERROR);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.ACC_MAPPING_DETAILS_MISSING_ERROR);
        }

        ArrayList<ConsentMappingResource> storedConsentMappingResources = new ArrayList<>();
        Connection connection = DatabaseUtils.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                // Create account mapping resources
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Creating consent account mapping resources for authorization ID: %s",
                            authID.replaceAll("[\r\n]", "")));
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
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return storedConsentMappingResources;
            } catch (ConsentDataInsertionException e) {
                log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }

    @Override
    public boolean deactivateAccountMappings(ArrayList<String> accountMappingIDs) throws
            ConsentMgtException {

        if (accountMappingIDs.isEmpty()) {
            log.error(ConsentCoreServiceConstants.ACC_MAPPING_ID_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.ACC_MAPPING_ID_MISSING_ERROR_MSG);
        }

        Connection connection = DatabaseUtils.getDBConnection();
        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                // Deactivate account mapping resources
                log.debug("Deactivating consent account mapping resources for given mapping IDs");

                consentCoreDAO.updateConsentMappingStatus(connection, accountMappingIDs,
                        ConsentCoreServiceConstants.INACTIVE_MAPPING_STATUS);

                // Commit transaction
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return true;
            } catch (ConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }

    @Override
    public boolean updateAccountMappingStatus(ArrayList<String> accountMappingIDs,
                                              String newMappingStatus)
            throws
            ConsentMgtException {

        if (accountMappingIDs.isEmpty()) {
            log.error(ConsentCoreServiceConstants.ACC_MAPPING_ID_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.ACC_MAPPING_ID_MISSING_ERROR_MSG);
        }

        Connection connection = DatabaseUtils.getDBConnection();
        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                // update account mapping resources
                log.debug("Deactivating consent account mapping resources for given mapping IDs");

                consentCoreDAO.updateConsentMappingStatus(connection, accountMappingIDs,
                        newMappingStatus);

                // Commit transaction
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return true;
            } catch (ConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }


    @Override
    public boolean deleteConsent(String consentID) throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentID)) {
            log.error(ConsentCoreServiceConstants.CONSENT_ID_MISSING_ERROR_MSG);
            return false;
        }

        Connection connection = DatabaseUtils.getDBConnection();
        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            // Delete consent
            if (log.isDebugEnabled()) {
                log.debug(String.format("Deleting the consent for ID: %s",
                        consentID.replaceAll("[\r\n]", "")));
            }
            consentCoreDAO.deleteConsent(connection, consentID);

            // Commit transaction
            DatabaseUtils.commitTransaction(connection);
            log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
            return true;
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }


    @Override
    public boolean revokeConsent(String consentID, String revokedConsentStatus)
            throws
            ConsentMgtException {
        return revokeConsentWithReason(consentID, revokedConsentStatus, null, true,
                ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);
    }

    @Override
    public boolean revokeConsentWithReason(String consentID, String revokedConsentStatus, String revokedReason)
            throws
            ConsentMgtException {
        return revokeConsentWithReason(consentID, revokedConsentStatus, null, true,
                revokedReason);
    }

    @Override
    public boolean revokeConsent(String consentID, String revokedConsentStatus, String userID)
            throws
            ConsentMgtException {
        return revokeConsentWithReason(consentID, revokedConsentStatus, userID, true,
                ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);
    }

    @Override
    public boolean revokeConsentWithReason(String consentID, String revokedConsentStatus, String userID,
                                           String revokedReason)
            throws
            ConsentMgtException {
        return revokeConsentWithReason(consentID, revokedConsentStatus, userID, true, revokedReason);
    }

    @Override
    public boolean revokeConsent(String consentID, String revokedConsentStatus, String userID,
                                 boolean shouldRevokeTokens)
            throws
            ConsentMgtException {
        return revokeConsentWithReason(consentID, revokedConsentStatus, userID, shouldRevokeTokens,
                ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);
    }

    @Override
    public boolean revokeConsentWithReason(String consentID, String revokedConsentStatus, String userID,
                                           boolean shouldRevokeTokens, String revokedReason)
            throws
            ConsentMgtException {



        if (StringUtils.isBlank(consentID) || StringUtils.isBlank(revokedConsentStatus)) {
            log.error(ConsentCoreServiceConstants.CONSENT_STATUS_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.CONSENT_STATUS_MISSING_ERROR_MSG);
        }

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                // Get existing detailed consent
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Retrieving existing consent of ID: %s for status validation",
                            consentID.replaceAll("[\r\n]", "")));
                }
                DetailedConsentResource retrievedDetailedConsentResource = consentCoreDAO
                        .getDetailedConsentResource(connection, consentID);
                String previousConsentStatus = retrievedDetailedConsentResource.getCurrentStatus();
                if (previousConsentStatus.equals("revoked")) {

                    throw  new ConsentMgtException(Response.Status.BAD_REQUEST, "consent already revoked");
                }

                // Update consent status as revoked
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Updating the status of the consent of ID: %s",
                            consentID.replaceAll("[\r\n]", "")));
                }
                consentCoreDAO.updateConsentStatus(connection, consentID, revokedConsentStatus);

                if (shouldRevokeTokens) {
                    // Extract userId from authorizationResources
                    ArrayList<AuthorizationResource> authorizationResources = retrievedDetailedConsentResource
                            .getAuthorizationResources();
                    //TODO : check this
                    String consentUserID = userID;
                    if (authorizationResources != null && !authorizationResources.isEmpty()) {
                        consentUserID = authorizationResources.get(0).getUserID();
                    }

                    if (StringUtils.isBlank(consentUserID)) {
                        log.error(ConsentCoreServiceConstants.USER_ID_MISSING_ERROR_MSG);
                        throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                                ConsentCoreServiceConstants.USER_ID_MISSING_ERROR_MSG);
                    }

//                    if (!ConsentCoreServiceUtil.isValidUserID(userID, consentUserID)) {
//                        final String errorMsg = String.format(ConsentCoreServiceConstants.USER_ID_MISMATCH_ERROR_MSG,
//                                userID.replaceAll("[\r\n]", ""),
//                                consentUserID.replaceAll("[\r\n]", ""));
//                        log.error(errorMsg);
//                        throw new ConsentMgtException(errorMsg);
//                    }
//                    TokenRevocationUtil.revokeTokens(retrievedDetailedConsentResource, consentUserID);
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
                        log.debug(String.format("Updating the account mappings of consent ID: %s as inactive",
                                consentID.replaceAll("[\r\n]", "")));
                    }
                    consentCoreDAO.updateConsentMappingStatus(connection, mappingIDs,
                            ConsentCoreServiceConstants.INACTIVE_MAPPING_STATUS);
                }

                HashMap<String, Object> consentDataMap = new HashMap<>();
                // Get detailed consent status after the updates
                DetailedConsentResource newDetailedConsentResource =
                        consentCoreDAO.getDetailedConsentResource(connection, consentID);
                consentDataMap.put(ConsentCoreServiceConstants.DETAILED_CONSENT_RESOURCE,
                        newDetailedConsentResource);

                // Pass the previous status consent to persist as consent history
                consentDataMap.put(ConsentCoreServiceConstants.CONSENT_AMENDMENT_HISTORY_RESOURCE,
                        retrievedDetailedConsentResource);
                consentDataMap.put(ConsentCoreServiceConstants.CONSENT_AMENDMENT_TIME, System.currentTimeMillis());

                if (userID == null) {
                    if (!retrievedDetailedConsentResource.getAuthorizationResources().isEmpty()) {
                        userID = retrievedDetailedConsentResource.getAuthorizationResources().get(0).getUserID();

                    } else {
                        throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                                "please provide a userId");
                    }
                }
                // Create an audit record execute state change listener
                ConsentCoreServiceUtil.postStateChange(connection, consentCoreDAO, consentID, userID,
                        revokedConsentStatus, previousConsentStatus, revokedReason,
                        retrievedDetailedConsentResource.getClientID(), consentDataMap);

                //Commit transaction
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG,
                        e);
            } catch (ConsentDataInsertionException e) {
                log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
            } catch (ConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
        return true;
    }

    @Override
    public boolean revokeExistingApplicableConsents(String clientID, String userID, String consentType,
                                                    String applicableStatusToRevoke,
                                                    String revokedConsentStatus, boolean shouldRevokeTokens)
            throws
            ConsentMgtException {

        if (StringUtils.isBlank(clientID) || StringUtils.isBlank(revokedConsentStatus) || StringUtils.isBlank(userID)
                || StringUtils.isBlank(applicableStatusToRevoke) || StringUtils.isBlank(consentType)) {
            log.error(ConsentCoreServiceConstants.REVOKE_DETAILS_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants.REVOKE_DETAILS_MISSING_ERROR_MSG);
        }

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {

                ArrayList<String> accountMappingIDsList = new ArrayList<>();
                ArrayList<String> clientIDsList = ConsentCoreServiceUtil.constructArrayList(clientID);
                ArrayList<String> userIDsList = ConsentCoreServiceUtil.constructArrayList(userID);
                ArrayList<String> consentTypesList = ConsentCoreServiceUtil.constructArrayList(consentType);
                ArrayList<String> consentStatusesList = ConsentCoreServiceUtil
                        .constructArrayList(applicableStatusToRevoke);

                // Get existing consents
                log.debug("Retrieving existing consents");

                // Only parameters needed for the search are provided, others are made null
                ArrayList<DetailedConsentResource> retrievedDetailedConsentResources = consentCoreDAO
                        .searchConsents(connection, null, null, clientIDsList, consentTypesList,
                                consentStatusesList, userIDsList, null, null, null, null);

                // Revoke existing consents and create audit records
                for (DetailedConsentResource resource : retrievedDetailedConsentResources) {
                    String previousConsentStatus = resource.getCurrentStatus();

                    // Update consent status
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Updating consent status for consent ID: %s",
                                resource.getConsentID().replaceAll("[\r\n]", "")));
                    }
                    consentCoreDAO.updateConsentStatus(connection, resource.getConsentID(), revokedConsentStatus);

                    if (shouldRevokeTokens) {
//                        TokenRevocationUtil.revokeTokens(resource, userID);
                    }

                    // Create an audit record for consent update
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Creating audit record for the status change of consent ID: %s",
                                resource.getConsentID().replaceAll("[\r\n]", "")));
                    }
                    // Create an audit record execute state change listener
                    HashMap<String, Object> consentDataMap = new HashMap<>();
                    consentDataMap.put(ConsentCoreServiceConstants.DETAILED_CONSENT_RESOURCE, resource);
//                    ConsentCoreServiceUtil.postStateChange(connection, consentCoreDAO, resource.getConsentID(),
//                    userID,
//                            revokedConsentStatus, previousConsentStatus,
//                            ConsentCoreServiceConstants.CONSENT_REVOKE_REASON, resource.getClientID(),
//                            consentDataMap);

                    // Extract account mapping IDs for retrieved applicable consents
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Extracting account mapping IDs from consent ID: %s",
                                resource.getConsentID().replaceAll("[\r\n]", "")));
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
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return true;
            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            } catch (ConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }

    @Override
    public boolean reAuthorizeExistingAuthResource(String consentID, String authID, String userID,
                                                   Map<String, ArrayList<String>> accountIDsMapWithPermissions,
                                                   String currentConsentStatus, String newConsentStatus)
            throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentID) || StringUtils.isBlank(authID) || StringUtils.isBlank(userID)
                || MapUtils.isEmpty(accountIDsMapWithPermissions) || StringUtils.isBlank(newConsentStatus)
                || StringUtils.isBlank(currentConsentStatus)) {
            log.error(ConsentCoreServiceConstants.RE_AUTH_DETAILS_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants.RE_AUTH_DETAILS_MISSING_ERROR_MSG);
        }

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            try {
                ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();

                // Get detailed consent to retrieve account mappings
                DetailedConsentResource detailedConsentResource =
                        consentCoreDAO.getDetailedConsentResource(connection, consentID);

//                // Update accounts if required
//                ConsentCoreServiceUtil.updateAccounts(connection, consentCoreDAO, authID,
//                accountIDsMapWithPermissions,
//                        detailedConsentResource, false);

                // Update consent status
                consentCoreDAO.updateConsentStatus(connection, consentID, newConsentStatus);

                // Create an audit record execute state change listener
                HashMap<String, Object> consentDataMap = new HashMap<>();
                consentDataMap.put(ConsentCoreServiceConstants.DETAILED_CONSENT_RESOURCE, detailedConsentResource);
//                ConsentCoreServiceUtil.postStateChange(connection, consentCoreDAO, consentID, userID,
//                newConsentStatus,
//                        currentConsentStatus, ConsentCoreServiceConstants.CONSENT_REAUTHORIZE_REASON,
//                        detailedConsentResource.getClientID(), consentDataMap);

                // Commit transactions
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return true;
            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            } catch (ConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }

    @Override
    public boolean reAuthorizeConsentWithNewAuthResource(String consentID, String userID, Map<String,
                                                                 ArrayList<String>> accountIDsMapWithPermissions,
                                                         String currentConsentStatus, String newConsentStatus,
                                                         String newExistingAuthStatus, String newAuthStatus,
                                                         String newAuthType)
            throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentID) || StringUtils.isBlank(userID)
                || MapUtils.isEmpty(accountIDsMapWithPermissions) || StringUtils.isBlank(newConsentStatus)
                || StringUtils.isBlank(currentConsentStatus) || StringUtils.isBlank(newExistingAuthStatus)
                || StringUtils.isBlank(newAuthStatus) || StringUtils.isBlank(newAuthType)) {
            log.error(ConsentCoreServiceConstants.RE_AUTH_RESOURCE_DETAILS_MISSING_ERROR);
            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants.RE_AUTH_RESOURCE_DETAILS_MISSING_ERROR);
        }

        Connection connection = DatabaseUtils.getDBConnection();

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
                mappingResourcesToDeactivate.forEach(resource ->
                        mappingIdsToDeactivate.add(resource.getMappingID()));
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
                        consentCoreDAO.getDetailedConsentResource(connection, consentID);

                // Update accounts if required
//                ConsentCoreServiceUtil.updateAccounts(connection, consentCoreDAO,
//                        newAuthorizationResource.getAuthorizationID(), accountIDsMapWithPermissions,
//                        detailedConsentResource, true);

                // Update consent status
                consentCoreDAO.updateConsentStatus(connection, consentID, newConsentStatus);

                // Create an audit record execute state change listener
                HashMap<String, Object> consentDataMap = new HashMap<>();
                consentDataMap.put(ConsentCoreServiceConstants.DETAILED_CONSENT_RESOURCE, detailedConsentResource);
//                ConsentCoreServiceUtil.postStateChange(connection, consentCoreDAO, consentID, userID,
//                newConsentStatus,
//                        currentConsentStatus, ConsentCoreServiceConstants.CONSENT_REAUTHORIZE_REASON,
//                        detailedConsentResource.getClientID(), consentDataMap);

                // Commit transactions
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return true;
            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            } catch (ConsentDataInsertionException e) {
                log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
            } catch (ConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }

    @Override
    public boolean storeConsentAttributes(String consentID, Map<String, String> consentAttributes)
            throws
            ConsentMgtException {

        boolean isConsentAttributesStored;

        if (StringUtils.isBlank(consentID) || consentAttributes == null || consentAttributes.isEmpty()) {

            log.error(ConsentCoreServiceConstants.CONSENT_ATTRIBUTES_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants.CONSENT_ATTRIBUTES_MISSING_ERROR_MSG);
        }

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            try {
                ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
                ConsentAttributes consentAttributesObject = new ConsentAttributes();
                consentAttributesObject.setConsentID(consentID);
                consentAttributesObject.setConsentAttributes(consentAttributes);

                if (log.isDebugEnabled()) {
                    log.debug(String.format("Storing consent attributes for the consent of ID: %s",
                            consentID.replaceAll("[\r\n]", "")));
                }
                isConsentAttributesStored = consentCoreDAO.storeConsentAttributes(connection, consentAttributesObject);
                DatabaseUtils.commitTransaction(connection);
            } catch (ConsentDataInsertionException e) {
                log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }

        return isConsentAttributesStored;
    }

    @Override
    public ConsentAttributes getConsentAttributes(String consentID, ArrayList<String> consentAttributeKeys)
            throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentID) || CollectionUtils.isEmpty(consentAttributeKeys)) {
            log.error(ConsentCoreServiceConstants.CONSENT_ATTRIBUTE_KEYS_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.CONSENT_ATTRIBUTE_KEYS_MISSING_ERROR_MSG);
        }

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();

            try {
                ConsentResource retrievedConsentResource = consentCoreDAO.getConsentResource(connection, consentID);
                if (retrievedConsentResource == null) {
                    String errorMessage = String.format("Consent ID  : %s is not available in the database",
                            consentID.replaceAll("[\r\n]", ""));
                    log.error(errorMessage);
                    throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                            errorMessage);
                }
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Consent ID  : %s is available in the database",
                            consentID.replaceAll("[\r\n]", "")));
                }
                ConsentAttributes retrievedConsentAttributes;
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Retrieving consent attributes for given keys for consent ID: %s",
                            consentID.replaceAll("[\r\n]", "")));
                }
                retrievedConsentAttributes = consentCoreDAO.getConsentAttributes(connection, consentID,
                        consentAttributeKeys);

                // Commit transactions
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return retrievedConsentAttributes;
            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }

    @Override
    public ConsentAttributes getConsentAttributes(String consentID) throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentID)) {
            log.error(ConsentCoreServiceConstants.CONSENT_ID_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.CONSENT_ID_MISSING_ERROR_MSG);
        }

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();

            try {
                ConsentResource retrievedConsentResource = consentCoreDAO.getConsentResource(connection, consentID);
                if (retrievedConsentResource == null) {
                    String errorMessage = String.format("Consent ID  : %s is not available in the database",
                            consentID.replaceAll("[\r\n]", ""));
                    log.error(errorMessage);
                    throw new ConsentMgtException(Response.Status.BAD_REQUEST, errorMessage);
                }
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Consent ID  : %s is available in the database",
                            consentID.replaceAll("[\r\n]", "")));
                }

                ConsentAttributes retrievedConsentAttributes;
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Retrieving consent attributes for consent ID: %s",
                            consentID.replaceAll("[\r\n]", "")));
                }
                retrievedConsentAttributes = consentCoreDAO.getConsentAttributes(connection, consentID);

                // Commit transactions
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return retrievedConsentAttributes;
            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }

    @Override
    public Map<String, String> getConsentAttributesByName(String attributeName) throws
            ConsentMgtException {

        if (StringUtils.isBlank(attributeName)) {
            log.error(ConsentCoreServiceConstants.CONSENT_ATTRIBUTE_NAME_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants.CONSENT_ATTRIBUTE_NAME_MISSING_ERROR_MSG);
        }

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                Map<String, String> retrievedAttributeValuesMap;
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Retrieving attribute values for the provided attribute key: %s",
                            attributeName.replaceAll("[\r\n]", "")));
                }
                retrievedAttributeValuesMap = consentCoreDAO.getConsentAttributesByName(connection, attributeName);

                // Commit transactions
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return retrievedAttributeValuesMap;
            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }

    @Override
    public ArrayList<String> getConsentIdByConsentAttributeNameAndValue(String attributeName, String attributeValue)
            throws
            ConsentMgtException {

        if (StringUtils.isBlank(attributeName) || StringUtils.isBlank(attributeValue)) {
            log.error(ConsentCoreServiceConstants.ATTRIBUTE_NAME_VALUE_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants.ATTRIBUTE_NAME_VALUE_MISSING_ERROR_MSG);
        }

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                ArrayList<String> retrievedConsentIdList;
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Retrieving consent Id for the provided attribute key : %s and " +
                                    "attribute value : %s", attributeName.replaceAll("[\r\n]", ""),
                            attributeValue.replaceAll("[\r\n]", "")));
                }
                retrievedConsentIdList = consentCoreDAO.getConsentIdByConsentAttributeNameAndValue(connection,
                        attributeName, attributeValue);

                // Commit transactions
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return retrievedConsentIdList;
            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }

    @Override
    public ConsentAttributes updateConsentAttributes(String consentID, Map<String, String> consentAttributes)
            throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentID) || consentAttributes == null || consentAttributes.isEmpty()) {
            log.error(ConsentCoreServiceConstants.ATTRIBUTE_MAP_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants.ATTRIBUTE_MAP_MISSING_ERROR_MSG);
        }

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                ConsentAttributes updatedAttributes;
                if (log.isDebugEnabled()) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Updating the attributes of the consent for ID: %s",
                                consentID.replaceAll("[\r\n]", "")));
                    }
                }
                consentCoreDAO.updateConsentAttributes(connection, consentID, consentAttributes);
                updatedAttributes = consentCoreDAO.getConsentAttributes(connection, consentID);

                // Commit transactions
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return updatedAttributes;
            } catch (ConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.ATTRIBUTE_UPDATE_ERROR_MSG, e);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.ATTRIBUTE_UPDATE_ERROR_MSG, e);
            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }

    }

    @Override
    public boolean deleteConsentAttributes(String consentID, ArrayList<String> attributeKeysList)
            throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentID) || CollectionUtils.isEmpty(attributeKeysList)) {
            log.error(ConsentCoreServiceConstants.ATTRIBUTE_LIST_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.ATTRIBUTE_LIST_MISSING_ERROR_MSG);
        }

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Deleting attributes for the consent ID: %s",
                            consentID.replaceAll("[\r\n]", "")));
                }
                consentCoreDAO.deleteConsentAttributes(connection, consentID, attributeKeysList);

                // Commit transactions
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return true;
            } catch (ConsentDataDeletionException e) {
                log.error(ConsentCoreServiceConstants.DATA_DELETE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.CONSENT_ATTRIBUTES_DELETE_ERROR_MSG);
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }

    @Override
    public ArrayList<ConsentStatusAuditRecord> searchConsentStatusAuditRecords(String consentID, String status,
                                                                               String actionBy, Long fromTime,
                                                                               Long toTime, String statusAuditID)
            throws
            ConsentMgtException {

        ArrayList<ConsentStatusAuditRecord> auditRecords;
        Connection connection = DatabaseUtils.getDBConnection();

        try {
            try {
                ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();

                log.debug("Searching audit records");
                auditRecords = consentCoreDAO.getConsentStatusAuditRecords(connection, consentID, status, actionBy,
                        fromTime, toTime, statusAuditID);

            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.AUDIT_RECORD_SEARCH_ERROR_MSG, e);
                if (e.getMessage() != null) {
                    if (e.getMessage().contains(ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG)) {
                        throw new ConsentMgtException(Response.Status.BAD_REQUEST, e);
                    }
                }

                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.AUDIT_RECORD_SEARCH_ERROR_MSG, e);
            }

            // Commit transactions
            DatabaseUtils.commitTransaction(connection);
            log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
        return auditRecords;
    }

    @Override
    public ArrayList<ConsentStatusAuditRecord> getConsentStatusAuditRecords(ArrayList<String> consentIDs,
                                                                            Integer limit, Integer offset)
            throws
            ConsentMgtException {

        Connection connection = DatabaseUtils.getDBConnection();

        ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();

        try {
            //Retrieve consent status audit records.
            return consentCoreDAO.getConsentStatusAuditRecordsByConsentId(connection, consentIDs, limit, offset);

        } catch (ConsentDataRetrievalException e) {
            log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }

    @Override
    public boolean storeConsentAmendmentHistory(String statusAuditRecordId,
                                                ConsentHistoryResource consentHistoryResource,
                                                DetailedConsentResource detailedCurrentConsent)
            throws
            ConsentMgtException {

        if (StringUtils.isBlank(statusAuditRecordId) || consentHistoryResource == null ||
                StringUtils.isBlank(consentHistoryResource.getReason()) ||
                consentHistoryResource.getTimestamp() == 0) {
            log.error(ConsentCoreServiceConstants.AMEND_DETAILS_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants.AMEND_DETAILS_MISSING_ERROR_MSG);
        }

        String historyID = consentHistoryResource.getHistoryID();
        if (StringUtils.isBlank(historyID)) {
            historyID = String.valueOf(UUID.randomUUID());
        }
        long amendedTimestamp = consentHistoryResource.getTimestamp();
        String amendmentReason = consentHistoryResource.getReason();

        Connection connection = DatabaseUtils.getDBConnection();
        ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
        try {
            if (detailedCurrentConsent == null) {
                detailedCurrentConsent = consentCoreDAO.getDetailedConsentResource(connection, statusAuditRecordId);
            }

            DetailedConsentResource detailedHistoryConsent = consentHistoryResource.getDetailedConsentResource();
            // store only the changes in basic consent data to CA history

            JSONObject changedConsentDataJson = ConsentCoreServiceUtil
                    .getChangedBasicConsentDataJSON(detailedCurrentConsent, detailedHistoryConsent);
            if (!changedConsentDataJson.isEmpty()) {
                consentCoreDAO.storeConsentAmendmentHistory(connection, historyID, amendedTimestamp,
                        statusAuditRecordId,
                        detailedCurrentConsent.getConsentID(),
                        ConsentCoreServiceConstants.TYPE_CONSENT_BASIC_DATA, String.valueOf(changedConsentDataJson),
                        amendmentReason);
            }

            if (detailedCurrentConsent.getConsentAttributes() != null) {
                // store only the changes in consent attributes to CA history
                JSONObject changedConsentAttributesJson = ConsentCoreServiceUtil.getChangedConsentAttributesDataJSON(
                        detailedCurrentConsent.getConsentAttributes(), detailedHistoryConsent.getConsentAttributes());
                if (!changedConsentAttributesJson.isEmpty()) {
                    consentCoreDAO.storeConsentAmendmentHistory(connection, historyID, amendedTimestamp,
                            statusAuditRecordId,
                            detailedCurrentConsent.getConsentID(),
                            ConsentCoreServiceConstants.TYPE_CONSENT_ATTRIBUTES_DATA,
                            String.valueOf(changedConsentAttributesJson), amendmentReason);
                }
            }


            if (detailedCurrentConsent.getConsentMappingResources() != null) {
                // store only the changes in consent mappings to CA history
                Map<String, JSONObject> changedConsentMappingsJsonDataMap = ConsentCoreServiceUtil
                        .getChangedConsentMappingDataJSONMap(detailedCurrentConsent.getConsentMappingResources(),
                                detailedHistoryConsent.getConsentMappingResources());
                for (Map.Entry<String, JSONObject> changedConsentMapping :
                        changedConsentMappingsJsonDataMap.entrySet()) {
                    consentCoreDAO.storeConsentAmendmentHistory(connection, historyID, amendedTimestamp,
                            statusAuditRecordId, changedConsentMapping.getKey(),
                            ConsentCoreServiceConstants.TYPE_CONSENT_MAPPING_DATA,
                            String.valueOf(changedConsentMapping.getValue()), amendmentReason);
                }
            }


            // store only the changes in consent Auth Resources to CA history
            Map<String, JSONObject> changedConsentAuthResourcesJsonDataMap = ConsentCoreServiceUtil
                    .getChangedConsentAuthResourcesDataJSONMap(detailedCurrentConsent.getAuthorizationResources(),
                            detailedHistoryConsent.getAuthorizationResources());
            for (Map.Entry<String, JSONObject> changedConsentAuthResource :
                    changedConsentAuthResourcesJsonDataMap.entrySet()) {
                consentCoreDAO.storeConsentAmendmentHistory(connection, historyID, amendedTimestamp,
                        statusAuditRecordId,
                        changedConsentAuthResource.getKey(),
                        ConsentCoreServiceConstants.TYPE_CONSENT_AUTH_RESOURCE_DATA,
                        String.valueOf(changedConsentAuthResource.getValue()), amendmentReason);
            }

            // Commit transactions
            DatabaseUtils.commitTransaction(connection);
            log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
            return true;
        } catch (ConsentDataInsertionException | ConsentDataRetrievalException e) {
            log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
            DatabaseUtils.rollbackTransaction(connection);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }

    @Override
    public Map<String, ConsentHistoryResource> getConsentAmendmentHistoryData(List<String> statusAuditRecordIds,
                                                                              String consentID)
            throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentID)) {
            log.error(ConsentCoreServiceConstants.CONSENT_ID_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.CONSENT_ID_MISSING_ERROR_MSG);
        }

        Connection connection = DatabaseUtils.getDBConnection();
        ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
        try {
            //Retrieve the current detailed consent to build the detailed consent amendment history resources
            DetailedConsentResource currentConsentResource =
                    consentCoreDAO.getDetailedConsentResource(connection, consentID);

            Map<String, ConsentHistoryResource> consentAmendmentHistoryRetrievalResult =
                    consentCoreDAO.retrieveConsentAmendmentHistory(connection,
                            statusAuditRecordIds, consentID);

            Map<String, ConsentHistoryResource> consentAmendmentHistory = new LinkedHashMap<>();
            if (!consentAmendmentHistoryRetrievalResult.isEmpty()) {
                consentAmendmentHistory = ConsentCoreServiceUtil.processConsentAmendmentHistoryData(
                        consentAmendmentHistoryRetrievalResult, currentConsentResource);
            }
            return consentAmendmentHistory;
        } catch (ConsentDataRetrievalException e) {
            log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }

    @Override
    public ArrayList<DetailedConsentResource> searchDetailedConsents(String orgID, ArrayList<String> consentIDs,
                                                                     ArrayList<String> clientIDs,
                                                                     ArrayList<String> consentTypes,
                                                                     ArrayList<String> consentStatuses,
                                                                     ArrayList<String> userIDs, Long fromTime,
                                                                     Long toTime,
                                                                     Integer limit, Integer offset) throws
            ConsentMgtException {

        // Input parameters except limit and offset are not validated since they are validated in the DAO method
        ArrayList<DetailedConsentResource> detailedConsentResources;

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            try {
                ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();

                log.debug("Searching detailed consents");
                detailedConsentResources = consentCoreDAO.searchConsents(connection, orgID, consentIDs, clientIDs,
                        consentTypes, consentStatuses, userIDs, fromTime, toTime, limit, offset);

            } catch (ConsentDataRetrievalException | ConsentMgtException e) {
                log.error(ConsentCoreServiceConstants.DETAIL_CONSENT_SEARCH_ERROR_MSG, e);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DETAIL_CONSENT_SEARCH_ERROR_MSG, e);
            }

            // Commit transactions
            DatabaseUtils.commitTransaction(connection);
            log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
        return detailedConsentResources;
    }


    @Override
    public DetailedConsentResource amendDetailedConsent(String consentID, String consentReceipt,
                                                        Long consentExpiryTime, String authID,
                                                        Map<String, ArrayList<String>> accountIDsMapWithPermissions,
                                                        String newConsentStatus, Map<String, String> consentAttributes,
                                                        String userID, Map<String, Object> additionalAmendmentData)
            throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentID) ||
                (StringUtils.isBlank(consentReceipt) && (consentExpiryTime == null))) {
            log.error(ConsentCoreServiceConstants.CONSENT_DATA_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants.CONSENT_DATA_MISSING_ERROR_MSG);
        }

        if (StringUtils.isBlank(authID) || StringUtils.isBlank(userID)
                || MapUtils.isEmpty(accountIDsMapWithPermissions) || StringUtils.isBlank(newConsentStatus)
                || consentAttributes == null) {
            log.error(ConsentCoreServiceConstants.DETAILED_CONSENT_DATA_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants.DETAILED_CONSENT_DATA_MISSING_ERROR_MSG);
        }

        Connection connection = DatabaseUtils.getDBConnection();
        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            // Retrieve the current detailed consent before the amendment for the consent amendment history persistence
            DetailedConsentResource detailedConsentResource =
                    consentCoreDAO.getDetailedConsentResource(connection, consentID);

            // Update receipt and validity time
            if (StringUtils.isNotBlank(consentReceipt)) {
                consentCoreDAO.updateConsentReceipt(connection, consentID, consentReceipt);
            }
            if (consentExpiryTime != null) {
                consentCoreDAO.updateConsentExpiryTime(connection, consentID, consentExpiryTime);
            }

            // Update consent status and record the updated time
            consentCoreDAO.updateConsentStatus(connection, consentID, newConsentStatus);

//            // Update accounts if required
//            ConsentCoreServiceUtil.updateAccounts(connection, consentCoreDAO, authID, accountIDsMapWithPermissions,
//                    detailedConsentResource, false);

            // Update consent attributes
            ConsentCoreServiceUtil.updateConsentAttributes(connection, consentCoreDAO, consentID, consentAttributes);

            // Update consent accordingly if additional amendment data passed
//            if (!additionalAmendmentData.isEmpty()) {
//                ConsentCoreServiceUtil.processAdditionalConsentAmendmentData(connection, consentCoreDAO,
//                        additionalAmendmentData);
//            }

            // Get detailed consent status after update
            DetailedConsentResource newDetailedConsentResource =
                    consentCoreDAO.getDetailedConsentResource(connection, consentID);

            /* Even if the consent is amended, the status remains same as Authorized. For tracking purposes, an
                 audit record is created as the consent status of "amended". But still the real consent status will
                 remain as it is */
            HashMap<String, Object> consentDataMap = new HashMap<>();
            consentDataMap.put(ConsentCoreServiceConstants.DETAILED_CONSENT_RESOURCE, newDetailedConsentResource);

            // Pass the previous consent to persist as consent amendment history
            consentDataMap.put(ConsentCoreServiceConstants.CONSENT_AMENDMENT_HISTORY_RESOURCE, detailedConsentResource);
            consentDataMap.put(ConsentCoreServiceConstants.CONSENT_AMENDMENT_TIME, System.currentTimeMillis());

            ConsentCoreServiceUtil.postStateChange(connection, consentCoreDAO, consentID, userID,
                    ConsentCoreServiceConstants.CONSENT_AMENDED_STATUS, detailedConsentResource.getCurrentStatus(),
                    ConsentCoreServiceConstants.CONSENT_AMEND_REASON, detailedConsentResource.getClientID(),
                    consentDataMap);


            log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
            return newDetailedConsentResource;
        } catch (ConsentDataRetrievalException e) {
            log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
        } catch (ConsentDataInsertionException e) {
            log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
            DatabaseUtils.rollbackTransaction(connection);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
        } catch (ConsentDataUpdationException e) {
            log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            DatabaseUtils.rollbackTransaction(connection);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
        } catch (ConsentDataDeletionException e) {
            log.error(ConsentCoreServiceConstants.DATA_DELETE_ROLLBACK_ERROR_MSG, e);
            DatabaseUtils.rollbackTransaction(connection);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.CONSENT_ATTRIBUTES_DELETE_ERROR_MSG);
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }

    //
    @Override
    public DetailedConsentResource amendDetailedConsentWithBulkAuthResource(String orgId, String consentID,
                                                                            String consentReceipt,
                                                                            Long consentExpiryTime,
                                                                            ArrayList<AuthorizationResource>
                                                                                    reAuthorizationResources,

                                                                            String newConsentStatus,
                                                                            Map<String, String> consentAttributes,
                                                                            String userID,
                                                                            ArrayList<AuthorizationResource>
                                                                                    newAuthResources)
            throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentID) ||
                (StringUtils.isBlank(consentReceipt) && (consentExpiryTime == null))) {
            log.error(ConsentCoreServiceConstants.CONSENT_DATA_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants.CONSENT_DATA_MISSING_ERROR_MSG);
        }

        for (AuthorizationResource authorizationResource : reAuthorizationResources) {
            if (StringUtils.isBlank(authorizationResource.getAuthorizationID()) ||
                    StringUtils.isBlank(authorizationResource.getAuthorizationStatus()) ||
                    StringUtils.isBlank(authorizationResource.getAuthorizationType()) ||
                    StringUtils.isBlank(authorizationResource.getUserID()) || StringUtils.isBlank(newConsentStatus)) {
                log.error(ConsentCoreServiceConstants.DETAILED_CONSENT_DATA_MISSING_ERROR_MSG);
                throw new
                        ConsentMgtException(Response.Status.BAD_REQUEST,
                        ConsentCoreServiceConstants.DETAILED_CONSENT_DATA_MISSING_ERROR_MSG);
            }
        }


        Connection connection = DatabaseUtils.getDBConnection();
        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            // Retrieve the current detailed consent before the amendment for the consent amendment history persistence
            DetailedConsentResource detailedConsentResource =
                    consentCoreDAO.getDetailedConsentResource(connection, consentID);

            if (!ConsentCoreServiceUtil.validateOrgInfo(orgId, detailedConsentResource.getOrgID())) {
                log.error("OrgInfo does not match");
                throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                        "OrgInfo does not match, please provide the correct OrgInfo");
            }

            // Update receipt and validity time
            if (StringUtils.isNotBlank(consentReceipt)) {
                consentCoreDAO.updateConsentReceipt(connection, consentID, consentReceipt);
            }
            if (consentExpiryTime != null) {
                consentCoreDAO.updateConsentExpiryTime(connection, consentID, consentExpiryTime);
            }

            // Update consent status and record the updated time
            consentCoreDAO.updateConsentStatus(connection, consentID, newConsentStatus);

            ArrayList<ConsentMappingResource> updatedConsentMappingResources = new ArrayList<>();

            // iterate through the authresources and get the persomins for each account and call updateAccounts
            for (AuthorizationResource authorizationResource : reAuthorizationResources) {


                AuthorizationResource existingAuthorizationResource =
                        consentCoreDAO.getAuthorizationResource(connection,
                                authorizationResource.getAuthorizationID(), orgId);

                //validate consentId
                if (!consentID.equals(existingAuthorizationResource.getConsentID())) {
                    throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                            String.format("Consent ID %s does not match with the consent ID %s of the " +
                                            "authorization ID %s", consentID,
                                    existingAuthorizationResource.getConsentID(),
                                    authorizationResource.getAuthorizationID()));
                }


                consentCoreDAO.updateAuthorizationStatus(connection, authorizationResource.getAuthorizationID(),
                        authorizationResource.getAuthorizationStatus());

                consentCoreDAO.updateAuthorizationUser(connection, authorizationResource.getAuthorizationID(),
                        authorizationResource.getUserID());

                Map<String, ArrayList<String>> accountIDsMapWithPermissions = new HashMap<>();

                ArrayList<ConsentMappingResource> exitingConsentMappingResources = consentCoreDAO
                        .getConsentMappingResources(connection, authorizationResource.getAuthorizationID());
                ArrayList<String> newConsentResourceIds = new ArrayList<>();


                // deactivating removed resources
                ArrayList<String> inactiveMappings = new ArrayList<>();
                for (ConsentMappingResource consentMappingResource : exitingConsentMappingResources) {
                    if (!newConsentResourceIds.contains(consentMappingResource.getMappingID())) {
                        consentMappingResource.setMappingStatus("inactive");
                        inactiveMappings.add(consentMappingResource.getMappingID());
                        updatedConsentMappingResources.add(consentMappingResource);
                    }
                }
                if (!inactiveMappings.isEmpty()) {
                    consentCoreDAO.updateConsentMappingStatus(connection, inactiveMappings, "inactive");

                }

            }

            // Update consent attributes
            if (!consentAttributes.isEmpty()) {
                ConsentCoreServiceUtil.updateConsentAttributes(connection, consentCoreDAO, consentID,
                        consentAttributes);

            }

            // Update consent accordingly if additional amendment data passed
            if (!newAuthResources.isEmpty()) {
                ConsentCoreServiceUtil.processAdditionalConsentAmendmentData(connection, consentCoreDAO,
                        newAuthResources, updatedConsentMappingResources);
            }

            // Get detailed consent status after update
            DetailedConsentResource newDetailedConsentResource =
                    consentCoreDAO.getDetailedConsentResource(connection, consentID);

            newDetailedConsentResource.setConsentMappingResources(updatedConsentMappingResources);
            newDetailedConsentResource.setReceipt(consentReceipt);




            /* Even if the consent is amended, the status remains same as Authorized. For tracking purposes, an
                 audit record is created as the consent status of "amended". But still the real consent status will
                 remain as it is */
            HashMap<String, Object> consentDataMap = new HashMap<>();
            consentDataMap.put(ConsentCoreServiceConstants.DETAILED_CONSENT_RESOURCE, newDetailedConsentResource);

            // Pass the previous consent to persist as consent amendment history
            consentDataMap.put(ConsentCoreServiceConstants.CONSENT_AMENDMENT_HISTORY_RESOURCE, detailedConsentResource);
            consentDataMap.put(ConsentCoreServiceConstants.CONSENT_AMENDMENT_TIME, System.currentTimeMillis());

            //TODO : what is this userId? what value to pass?
            if (userID == null) {
                userID = detailedConsentResource.getClientID();
            }
            ConsentCoreServiceUtil.postStateChange(connection, consentCoreDAO, consentID, userID,
                    ConsentCoreServiceConstants.CONSENT_AMENDED_STATUS, detailedConsentResource.getCurrentStatus(),
                    ConsentCoreServiceConstants.CONSENT_AMEND_REASON, detailedConsentResource.getClientID(),
                    consentDataMap);

            // Commit transactions
            DatabaseUtils.commitTransaction(connection);
            log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
            return newDetailedConsentResource;
        } catch (ConsentDataRetrievalException e) {
            log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            throw new ConsentMgtException(Response.Status.NOT_FOUND,
                    e.getMessage(),
                    e);
        } catch (ConsentDataInsertionException e) {
            log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
            DatabaseUtils.rollbackTransaction(connection);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
        } catch (ConsentDataUpdationException e) {
            log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            DatabaseUtils.rollbackTransaction(connection);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
        } catch (ConsentDataDeletionException e) {
            log.error(ConsentCoreServiceConstants.DATA_DELETE_ROLLBACK_ERROR_MSG, e);
            DatabaseUtils.rollbackTransaction(connection);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.CONSENT_ATTRIBUTES_DELETE_ERROR_MSG);
        } catch (ConsentMgtException e) {
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }


    }

    //                 consentCoreDAO.updateConsentExpiryTime(connection, consentID, consentExpiryTime);
    // update ConsentexpiryTime
    public boolean updateConsentExpiryTime( String consentId, long consentExpiryTime)
    throws  ConsentMgtException
    {

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();


            if( System.currentTimeMillis()/1000 > consentExpiryTime  ){

                throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                        ConsentCoreServiceConstants.CONSENT_EXPIRY_TIME_BEFORE_CURRENT_TIMESTAMP_ERROR);
            }

            consentCoreDAO.updateConsentExpiryTime(connection, consentId, consentExpiryTime);

        } catch (ConsentDataUpdationException  e ){

            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                     ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG);
        }

        return true;

    }


}
