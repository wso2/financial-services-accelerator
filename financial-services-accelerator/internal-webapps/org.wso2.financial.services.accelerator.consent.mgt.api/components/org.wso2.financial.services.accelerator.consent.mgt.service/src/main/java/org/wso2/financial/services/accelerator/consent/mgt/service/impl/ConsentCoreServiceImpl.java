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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.consent.mgt.dao.ConsentCoreDAO;
import org.wso2.financial.services.accelerator.consent.mgt.dao.constants.ConsentError;
import org.wso2.financial.services.accelerator.consent.mgt.dao.constants.ConsentMgtDAOConstants;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentDataDeletionException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentDataInsertionException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentDataRetrievalException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentDataUpdationException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentMgtException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentAttributes;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentHistoryResource;
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
    public DetailedConsentResource createConsent(ConsentResource consentResource,
                                                 ArrayList<AuthorizationResource>
                                                         authorizationResources) throws
            ConsentMgtException {

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            return ConsentCoreServiceUtil
                    .createConsentWithAuditRecord(
                            consentCoreDAO, consentResource,
                            authorizationResources);
        } catch (ConsentDataInsertionException e) {
            log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    e.getConsentError());
        } catch (ConsentDataRetrievalException e) {
            log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.NOT_FOUND,
                    e.getConsentError());
        }

    }


    @Override
    public ConsentResource getConsent(String consentId, boolean withAttributes) throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentId)) {
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
                                consentId.replaceAll("[\r\n]", "")));
                    }
                    retrievedConsentResource = consentCoreDAO.getConsentResource(connection, consentId);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Retrieving consent with consent attributes for consent ID: %s",
                                consentId.replaceAll("[\r\n]", "")));
                    }
                    retrievedConsentResource = consentCoreDAO.getConsentResourceWithAttributes(connection, consentId);
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
    public DetailedConsentResource getDetailedConsent(String consentId, String orgInfo) throws
            ConsentMgtException {

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {

                // Retrieve the detailed consent resource
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Retrieving detailed consent for consent ID: %s",
                            consentId.replaceAll("[\r\n]", "")));
                }
                DetailedConsentResource retrievedDetailedConsentResource = consentCoreDAO
                        .getDetailedConsentResource(connection, consentId, orgInfo);


                // Commit transactions
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return retrievedDetailedConsentResource;
            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentMgtException(Response.Status.NOT_FOUND,
                        e.getConsentError());
            }
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

            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DETAIL_CONSENT_SEARCH_ERROR_MSG, e);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentError.CONSENT_SEARCH_ERROR, e);
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
    public boolean updateConsentExpiryTime(String consentId, long consentExpiryTime, String orgInfo)
            throws
            ConsentMgtException {

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();


            if (System.currentTimeMillis() / 1000 > consentExpiryTime) {

                throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                        ConsentCoreServiceConstants.CONSENT_EXPIRY_TIME_BEFORE_CURRENT_TIMESTAMP_ERROR);
            }

            DetailedConsentResource consentResource = consentCoreDAO.getDetailedConsentResource(connection, consentId,
                    orgInfo);

            if (consentResource.getCurrentStatus().equals("revoked")) {

                throw new ConsentMgtException(Response.Status.CONFLICT,
                        ConsentError.CONSENT_ALREADY_REVOKED_ERROR);
            }

            consentCoreDAO.updateConsentExpiryTime(connection, consentId, consentExpiryTime);

        } catch (ConsentDataUpdationException e) {
            log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG);
        } catch (ConsentDataRetrievalException e) {
            log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
        }

        return true;

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
                            authResource.getUserId(), newConsentStatus, existingConsentStatus, auditMessage,
                            existingConsentResource.getClientId(), consentDataMap);
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
    public void updateConsentStatus(String consentId, String newConsentStatus,
                                    String reason, String userId, String orgInfo) throws
            ConsentMgtException {

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
                        .getDetailedConsentResource(connection, consentId, orgInfo);

                String previousConsentStatus = existingConsentResource.getCurrentStatus();
                if (previousConsentStatus.equals("revoked")) {

                    throw new ConsentMgtException(Response.Status.CONFLICT,
                            ConsentCoreServiceConstants.CONSENT_ALREADY_REVOKED);
                }
                consentCoreDAO.updateConsentStatus(connection, consentId, newConsentStatus);


                //TODO: store history and audit record

                String existingConsentStatus = existingConsentResource.getCurrentStatus();
                // Previous consent status is not added in reason because it can be null
                String auditMessage = "Consent status updated to " + newConsentStatus;


                // Create an audit record execute state change listener
                HashMap<String, Object> consentDataMap = new HashMap<>();
                consentDataMap.put(ConsentCoreServiceConstants.CONSENT_AMENDMENT_HISTORY_RESOURCE,
                        existingConsentResource);
                DetailedConsentResource newConsentResource = existingConsentResource.clone();
                newConsentResource.setCurrentStatus(newConsentStatus);

                consentDataMap.put(ConsentCoreServiceConstants.DETAILED_CONSENT_RESOURCE, newConsentResource);
                ConsentCoreServiceUtil.postStateChange(connection, consentCoreDAO, consentId,
                        userId, newConsentStatus, existingConsentStatus, auditMessage,
                        existingConsentResource.getClientId(), consentDataMap);


                // Commit transaction
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                existingConsentResource.setCurrentStatus(newConsentStatus);

            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);

                throw new ConsentMgtException(Response.Status.NOT_FOUND, e);
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
            updateConsentStatus(consent.getConsentId(), status, reason, userId, orgID);

        }


    }


    @Override
    public boolean deleteConsent(String consentId) throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentId)) {
            log.error(ConsentCoreServiceConstants.CONSENT_ID_MISSING_ERROR_MSG);
            return false;
        }

        Connection connection = DatabaseUtils.getDBConnection();
        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            // Delete consent
            if (log.isDebugEnabled()) {
                log.debug(String.format("Deleting the consent for ID: %s",
                        consentId.replaceAll("[\r\n]", "")));
            }
            consentCoreDAO.deleteConsent(connection, consentId);

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
    public boolean revokeConsent(String consentId, String orgInfo, String actionBy, String revokedReason)
            throws
            ConsentMgtException {


        Connection connection = DatabaseUtils.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                // Get existing detailed consent
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Retrieving existing consent of ID: %s for status validation",
                            consentId.replaceAll("[\r\n]", "")));
                }
                DetailedConsentResource retrievedDetailedConsentResource = consentCoreDAO
                        .getDetailedConsentResource(connection, consentId, orgInfo);
                String previousConsentStatus = retrievedDetailedConsentResource.getCurrentStatus();

                if (previousConsentStatus.equals(ConsentCoreServiceConstants.CONSENT_REVOKE_STATUS)) {

                    throw new ConsentMgtException(Response.Status.CONFLICT, ConsentError.CONSENT_ALREADY_REVOKED_ERROR);
                }

                // Update consent status as revoked
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Updating the status of the consent of ID: %s",
                            consentId.replaceAll("[\r\n]", "")));
                }


                consentCoreDAO.updateConsentStatus(connection, consentId,
                        ConsentCoreServiceConstants.CONSENT_REVOKE_STATUS);

                // TODO : store history and status Audit
                HashMap<String, Object> consentDataMap = new HashMap<>();
                // Get detailed consent status after the updates
                DetailedConsentResource newDetailedConsentResource =
                        consentCoreDAO.getDetailedConsentResource(connection, consentId);
                consentDataMap.put(ConsentCoreServiceConstants.DETAILED_CONSENT_RESOURCE,
                        newDetailedConsentResource);

                // Pass the previous status consent to persist as consent history
                consentDataMap.put(ConsentCoreServiceConstants.CONSENT_AMENDMENT_HISTORY_RESOURCE,
                        retrievedDetailedConsentResource);
                consentDataMap.put(ConsentCoreServiceConstants.CONSENT_AMENDMENT_TIME, System.currentTimeMillis());

                if (actionBy == null) {
                    if (!retrievedDetailedConsentResource.getAuthorizationResources().isEmpty()) {
                        actionBy = retrievedDetailedConsentResource.getAuthorizationResources().get(0).getUserId();

                    } else {
                        throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                                "please provide a userId");
                    }
                }
                // Create an audit record execute state change listener
                ConsentCoreServiceUtil.postStateChange(connection, consentCoreDAO, consentId, actionBy,
                        ConsentCoreServiceConstants.CONSENT_REVOKE_STATUS, previousConsentStatus, revokedReason,
                        retrievedDetailedConsentResource.getClientId(), consentDataMap);

                //Commit transaction
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(Response.Status.NOT_FOUND,
                        e.getConsentError());
            } catch (ConsentDataInsertionException e) {
                log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        e.getConsentError());
            } catch (ConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        e.getConsentError());
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
        return true;
    }



    @Override
    public AuthorizationResource createConsentAuthorization(AuthorizationResource authorizationResource)
            throws
            ConsentMgtException {

        if (authorizationResource == null || StringUtils.isBlank(authorizationResource.getConsentId()) ||
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
                            authorizationResource.getConsentId().replaceAll("[\r\n]", "")));
                }

                // check wheather the consent is already revoked
                DetailedConsentResource consentResource = consentCoreDAO
                        .getDetailedConsentResource(connection, authorizationResource.getConsentId());
                if (consentResource.getCurrentStatus().equals("revoked")) {
                    throw new ConsentMgtException(Response.Status.CONFLICT,
                            ConsentCoreServiceConstants.CONSENT_ALREADY_REVOKED);
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
            } catch (ConsentDataRetrievalException e) {
                if (e.getMessage().equals(ConsentMgtDAOConstants.NO_RECORDS_FOUND_ERROR_MSG)) {
                    throw new ConsentMgtException(Response.Status.NOT_FOUND, e.getMessage());
                } else {
                    log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                    throw new ConsentMgtException(Response.Status.NOT_FOUND,
                            ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG,
                            e);
                }
            }
        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }

    @Override
    public AuthorizationResource getAuthorizationResource(String authorizationId, String orgID) throws
            ConsentMgtException {

        if (StringUtils.isBlank(authorizationId)) {
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
                            authorizationId.replaceAll("[\r\n]", "")));
                }
                retrievedAuthorizationResource = consentCoreDAO.getAuthorizationResource(connection, authorizationId,
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
    public void updateAuthorizationResource(String authorizationId,
                                            AuthorizationResource authorizationResource,
                                            String orgID)
            throws
            ConsentMgtException {

        if (StringUtils.isBlank(authorizationId)) {

            log.error(ConsentCoreServiceConstants.AUTH_ID_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants.AUTH_ID_MISSING_ERROR_MSG);
        }

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                // Update authorization resource
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Updating the authorization resource for ID: %s",
                            authorizationId.replaceAll("[\r\n]", "")));
                }
                AuthorizationResource updatedAuthorizationResource =
                        consentCoreDAO.updateAuthorizationResource(connection,
                                authorizationId,
                                authorizationResource);

                // Commit transaction
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
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

    ;

    @Override
    public boolean deleteAuthorizationResource(String authorizationId) throws
            ConsentMgtException {
        if (StringUtils.isBlank(authorizationId)) {
            log.error(ConsentCoreServiceConstants.AUTH_ID_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants.AUTH_ID_MISSING_ERROR_MSG);
        }

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                // Delete authorization resource
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Deleting the authorization resource for ID: %s",
                            authorizationId.replaceAll("[\r\n]", "")));
                }
                // check if the authorization resource is already deleted
                AuthorizationResource authorizationResource = consentCoreDAO.getAuthorizationResource(connection,
                        authorizationId, null);

                consentCoreDAO.deleteAuthorizationResource(connection, authorizationId);

                // Commit transaction
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return true;
            } catch (ConsentDataDeletionException e) {
                log.error(ConsentCoreServiceConstants.DATA_DELETE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_DELETE_ROLLBACK_ERROR_MSG, e);
            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentMgtException(Response.Status.NOT_FOUND,
                        ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            }

        } finally {
            log.debug(ConsentCoreServiceConstants.DATABASE_CONNECTION_CLOSE_LOG_MSG);
            DatabaseUtils.closeConnection(connection);
        }
    }


    @Override
    public boolean storeConsentAttributes(String consentId, Map<String, String> consentAttributes)
            throws
            ConsentMgtException {

        boolean isConsentAttributesStored;

        if (StringUtils.isBlank(consentId) || consentAttributes == null || consentAttributes.isEmpty()) {

            log.error(ConsentCoreServiceConstants.CONSENT_ATTRIBUTES_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants.CONSENT_ATTRIBUTES_MISSING_ERROR_MSG);
        }

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            try {
                ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
                ConsentAttributes consentAttributesObject = new ConsentAttributes();
                consentAttributesObject.setConsentId(consentId);
                consentAttributesObject.setConsentAttributes(consentAttributes);

                if (log.isDebugEnabled()) {
                    log.debug(String.format("Storing consent attributes for the consent of ID: %s",
                            consentId.replaceAll("[\r\n]", "")));
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
    public ConsentAttributes getConsentAttributes(String consentId, ArrayList<String> consentAttributeKeys)
            throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentId) || CollectionUtils.isEmpty(consentAttributeKeys)) {
            log.error(ConsentCoreServiceConstants.CONSENT_ATTRIBUTE_KEYS_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.CONSENT_ATTRIBUTE_KEYS_MISSING_ERROR_MSG);
        }

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();

            try {
                ConsentResource retrievedConsentResource = consentCoreDAO.getConsentResource(connection, consentId);
                if (retrievedConsentResource == null) {
                    String errorMessage = String.format("Consent ID  : %s is not available in the database",
                            consentId.replaceAll("[\r\n]", ""));
                    log.error(errorMessage);
                    throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                            errorMessage);
                }
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Consent ID  : %s is available in the database",
                            consentId.replaceAll("[\r\n]", "")));
                }
                ConsentAttributes retrievedConsentAttributes;
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Retrieving consent attributes for given keys for consent ID: %s",
                            consentId.replaceAll("[\r\n]", "")));
                }
                retrievedConsentAttributes = consentCoreDAO.getConsentAttributes(connection, consentId,
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
    public ConsentAttributes getConsentAttributes(String consentId) throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentId)) {
            log.error(ConsentCoreServiceConstants.CONSENT_ID_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.CONSENT_ID_MISSING_ERROR_MSG);
        }

        Connection connection = DatabaseUtils.getDBConnection();

        try {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();

            try {
                ConsentResource retrievedConsentResource = consentCoreDAO.getConsentResource(connection, consentId);
                if (retrievedConsentResource == null) {
                    String errorMessage = String.format("Consent ID  : %s is not available in the database",
                            consentId.replaceAll("[\r\n]", ""));
                    log.error(errorMessage);
                    throw new ConsentMgtException(Response.Status.BAD_REQUEST, errorMessage);
                }
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Consent ID  : %s is available in the database",
                            consentId.replaceAll("[\r\n]", "")));
                }

                ConsentAttributes retrievedConsentAttributes;
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Retrieving consent attributes for consent ID: %s",
                            consentId.replaceAll("[\r\n]", "")));
                }
                retrievedConsentAttributes = consentCoreDAO.getConsentAttributes(connection, consentId);

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
    public ConsentAttributes updateConsentAttributes(String consentId, Map<String, String> consentAttributes)
            throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentId) || consentAttributes == null || consentAttributes.isEmpty()) {
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
                                consentId.replaceAll("[\r\n]", "")));
                    }
                }
                consentCoreDAO.updateConsentAttributes(connection, consentId, consentAttributes);
                updatedAttributes = consentCoreDAO.getConsentAttributes(connection, consentId);

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
    public boolean deleteConsentAttributes(String consentId, ArrayList<String> attributeKeysList)
            throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentId) || CollectionUtils.isEmpty(attributeKeysList)) {
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
                            consentId.replaceAll("[\r\n]", "")));
                }
                consentCoreDAO.deleteConsentAttributes(connection, consentId, attributeKeysList);

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
    public ArrayList<ConsentStatusAuditRecord> searchConsentStatusAuditRecords(String consentId, String status,
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
                auditRecords = consentCoreDAO.getConsentStatusAuditRecords(connection, consentId, status, actionBy,
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
                        detailedCurrentConsent.getConsentId(),
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
                            detailedCurrentConsent.getConsentId(),
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
                                                                              String consentId)
            throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentId)) {
            log.error(ConsentCoreServiceConstants.CONSENT_ID_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.CONSENT_ID_MISSING_ERROR_MSG);
        }

        Connection connection = DatabaseUtils.getDBConnection();
        ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
        try {
            //Retrieve the current detailed consent to build the detailed consent amendment history resources
            DetailedConsentResource currentConsentResource =
                    consentCoreDAO.getDetailedConsentResource(connection, consentId);

            Map<String, ConsentHistoryResource> consentAmendmentHistoryRetrievalResult =
                    consentCoreDAO.retrieveConsentAmendmentHistory(connection,
                            statusAuditRecordIds, consentId);

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



}
