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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.Response;


/**
 * Consent core service implementation.
 */
@SuppressFBWarnings("CRLF_INJECTION_LOGS")
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
                    .createConsentWithAuditRecord(consentCoreDAO, consentResource, authorizationResources);
        } catch (ConsentDataInsertionException e) {
            log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG);
            throw new ConsentMgtException(e.getConsentError());
        } catch (ConsentDataRetrievalException e) {
            log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            throw new ConsentMgtException(e.getConsentError());
        }

    }


    @Override
    public DetailedConsentResource getDetailedConsent(String consentId, String orgInfo) throws
            ConsentMgtException {

        try (Connection connection = DatabaseUtils.getDBConnection()) {
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
                throw new ConsentMgtException(e.getConsentError());
            }
        } catch (SQLException e) {
            log.error(ConsentError.DETAILED_CONSENT_RETRIEVAL_ERROR.getMessage(), e);
            throw new ConsentMgtException(ConsentError.DETAILED_CONSENT_RETRIEVAL_ERROR);
        }
    }


    @Override
    public ArrayList<DetailedConsentResource> searchDetailedConsents(String orgInfo, ArrayList<String> consentIDs,
                                                                     ArrayList<String> clientIDs,
                                                                     ArrayList<String> consentTypes,
                                                                     ArrayList<String> consentStatuses,
                                                                     ArrayList<String> userIDs, Long fromTime,
                                                                     Long toTime,
                                                                     Integer limit, Integer offset) throws
            ConsentMgtException {

        // Input parameters except limit and offset are not validated since they are validated in the DAO method
        ArrayList<DetailedConsentResource> detailedConsentResources;


        try (Connection connection = DatabaseUtils.getDBConnection()) {
            try {
                ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();

                log.debug("Searching detailed consents");
                detailedConsentResources = consentCoreDAO.searchConsents(connection, orgInfo, consentIDs, clientIDs,
                        consentTypes, consentStatuses, userIDs, fromTime, toTime, limit, offset);

            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DETAIL_CONSENT_SEARCH_ERROR_MSG, e);
                throw new ConsentMgtException(ConsentError.CONSENT_SEARCH_ERROR);
            }
            // Commit transactions
            DatabaseUtils.commitTransaction(connection);
            log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
        } catch (SQLException e) {
            log.error(ConsentError.CONSENT_SEARCH_ERROR.getMessage(), e);
            throw new ConsentMgtException(ConsentError.CONSENT_SEARCH_ERROR);
        }
        return detailedConsentResources;
    }


    @Override
    public boolean updateConsentExpiryTime(String consentId, long consentExpiryTime, String orgInfo)
            throws
            ConsentMgtException {


        try (Connection connection = DatabaseUtils.getDBConnection()) {
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
            throw new ConsentMgtException(e.getConsentError());
        } catch (ConsentDataRetrievalException e) {
            log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            throw new ConsentMgtException(e.getConsentError());

        } catch (SQLException e) {
            log.error(ConsentError.CONSENT_EXPIRY_TIME_UPDATE_ERROR.getMessage(), e);
            throw new ConsentMgtException(ConsentError.CONSENT_EXPIRY_TIME_UPDATE_ERROR);
        }

        return true;

    }


    @Override
    public void updateConsentStatus(String consentId, String newConsentStatus,
                                    String reason, String userId, String orgInfo) throws
            ConsentMgtException {

        try (Connection connection = DatabaseUtils.getDBConnection()) {
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

                    throw new ConsentMgtException(ConsentError.CONSENT_ALREADY_REVOKED_ERROR);
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

                throw new ConsentMgtException(e.getConsentError());
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
        } catch (SQLException e) {
            log.error(ConsentError.CONSENT_STATUS_UPDATE_ERROR.getMessage(), e);
            throw new ConsentMgtException(ConsentError.CONSENT_STATUS_UPDATE_ERROR);
        }
    }


    @Override
    public void bulkUpdateConsentStatus(String orgInfo, String clientId, String status, String reason, String userId,
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
        try (Connection connection = DatabaseUtils.getDBConnection()) {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                detailedConsentResources = consentCoreDAO.searchConsents(connection, orgInfo, new ArrayList<>(),
                        clientIds,
                        consentTypes, applicableExistingStatus, null, null,
                        null, null, null);

            } catch (ConsentDataRetrievalException e) {
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        } catch (SQLException e) {
            log.error(ConsentError.CONSENT_UPDATE_ERROR.getMessage(), e);
            throw new ConsentMgtException(ConsentError.CONSENT_UPDATE_ERROR);
        }

        if (detailedConsentResources.isEmpty()) {
            log.error(ConsentError.CONSENT_NOT_FOUND.getMessage());
            throw new ConsentMgtException(ConsentError.CONSENT_NOT_FOUND);
        }
        for (DetailedConsentResource consent : detailedConsentResources) {
            updateConsentStatus(consent.getConsentId(), status, reason, userId, orgInfo);

        }


    }


    @Override
    public boolean deleteConsent(String consentId) throws
            ConsentMgtException {


        try (Connection connection = DatabaseUtils.getDBConnection()) {
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
        } catch (ConsentDataDeletionException e) {
            log.error(ConsentCoreServiceConstants.DATA_DELETION_ROLLBACK_ERROR_MSG, e);
            throw new ConsentMgtException(e.getConsentError());
        } catch (SQLException e) {
            log.error(ConsentError.CONSENT_DELETE_ERROR.getMessage(), e);
            throw new ConsentMgtException(ConsentError.CONSENT_DELETE_ERROR);
        }
    }


    @Override
    public boolean revokeConsent(String consentId, String orgInfo, String actionBy, String revokedReason)
            throws
            ConsentMgtException {


        try (Connection connection = DatabaseUtils.getDBConnection()) {
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

                    throw new ConsentMgtException(ConsentError.CONSENT_ALREADY_REVOKED_ERROR);
                }

                // Update consent status as revoked
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Updating the status of the consent of ID: %s",
                            consentId.replaceAll("[\r\n]", "")));
                }


                consentCoreDAO.updateConsentStatus(connection, consentId,
                        ConsentCoreServiceConstants.CONSENT_REVOKE_STATUS);

                // TODO : store history and status Audit

                //Commit transaction
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(e.getConsentError());
            } catch (ConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(
                        e.getConsentError());
            }
        } catch (SQLException e) {
            log.error(ConsentError.CONSENT_REVOKE_ERROR.getMessage(), e);
            throw new ConsentMgtException(ConsentError.CONSENT_REVOKE_ERROR);
        }
        return true;
    }


    @Override
    public AuthorizationResource createConsentAuthorization(AuthorizationResource authorizationResource)
            throws
            ConsentMgtException {

        try (Connection connection = DatabaseUtils.getDBConnection()) {
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
                log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(e.getConsentError());
            } catch (ConsentDataRetrievalException e) {
                DatabaseUtils.rollbackTransaction(connection);
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentMgtException(e.getConsentError());

            }
        } catch (SQLException e) {
            log.error(ConsentError.AUTHORIZATION_RESOURCE_INSERTION_ERROR.getMessage(), e);
            throw new ConsentMgtException(ConsentError.AUTHORIZATION_RESOURCE_INSERTION_ERROR);
        }
    }

    @Override
    public AuthorizationResource getAuthorizationResource(String authorizationId, String orgInfo) throws
            ConsentMgtException {

        try (Connection connection = DatabaseUtils.getDBConnection()) {

            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                AuthorizationResource retrievedAuthorizationResource;

                if (log.isDebugEnabled()) {
                    log.debug(String.format("Retrieving authorization resource for authorization ID: %s",
                            authorizationId.replaceAll("[\r\n]", "")));
                }
                retrievedAuthorizationResource = consentCoreDAO.getAuthorizationResource(connection, authorizationId,
                        orgInfo);

                // Commit transactions
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return retrievedAuthorizationResource;
            } catch (ConsentDataRetrievalException e) {
                throw new ConsentMgtException(e.getConsentError());

            }
        } catch (SQLException e) {
            log.error(ConsentError.AUTHORIZATION_RESOURCE_RETRIEVAL_ERROR.getMessage(), e);
            throw new ConsentMgtException(ConsentError.AUTHORIZATION_RESOURCE_RETRIEVAL_ERROR);
        }
    }


    @Override
    public void updateAuthorizationResource(String authorizationId,
                                            AuthorizationResource authorizationResource,
                                            String orgInfo)
            throws
            ConsentMgtException {

        try (Connection connection = DatabaseUtils.getDBConnection()) {

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
                throw new ConsentMgtException(e.getConsentError());

            }
        } catch (SQLException e) {
            log.error(ConsentError.AUTHORIZATION_RESOURCE_UPDATE_ERROR.getMessage(), e);
            throw new ConsentMgtException(ConsentError.AUTHORIZATION_RESOURCE_UPDATE_ERROR);
        }
    }

    @Override
    public boolean deleteAuthorizationResource(String authorizationId) throws
            ConsentMgtException {

        try (Connection connection = DatabaseUtils.getDBConnection()) {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
            try {
                // Delete authorization resource
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Deleting the authorization resource for ID: %s",
                            authorizationId.replaceAll("[\r\n]", "")));
                }
                // check if the authorization resource is already deleted
                consentCoreDAO.getAuthorizationResource(connection,
                        authorizationId, null);

                consentCoreDAO.deleteAuthorizationResource(connection, authorizationId);

                // Commit transaction
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return true;
            } catch (ConsentDataDeletionException e) {
                log.error(ConsentCoreServiceConstants.DATA_DELETE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(e.getConsentError());

            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentMgtException(e.getConsentError());

            }

        } catch (SQLException e) {
            log.error(ConsentError.DATABASE_CONNECTION_ERROR.getMessage(), e);
            throw new ConsentMgtException(ConsentError.DATABASE_CONNECTION_ERROR);
        }
    }


    //TODO : not yet implemented and tested
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


        try (Connection connection = DatabaseUtils.getDBConnection()) {
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
        } catch (SQLException e) {
            log.error(ConsentError.DATABASE_CONNECTION_ERROR.getMessage(), e);
            throw new ConsentMgtException(ConsentError.DATABASE_CONNECTION_ERROR);
        }

        return isConsentAttributesStored;
    }

    //TODO : not yet implemented and tested
    @Override
    public ConsentAttributes getConsentAttributes(String consentId, ArrayList<String> consentAttributeKeys)
            throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentId) || CollectionUtils.isEmpty(consentAttributeKeys)) {
            log.error(ConsentCoreServiceConstants.CONSENT_ATTRIBUTE_KEYS_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.CONSENT_ATTRIBUTE_KEYS_MISSING_ERROR_MSG);
        }


        try (Connection connection = DatabaseUtils.getDBConnection()) {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();

            try {
                //TODO : pass orgInfo
                ConsentResource retrievedConsentResource = consentCoreDAO.getConsentResource(connection, consentId,
                        null);
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
                //TODO : pass orgInfo
                retrievedConsentAttributes = consentCoreDAO.getConsentAttributes(connection, consentId, null,
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
        } catch (SQLException e) {
            log.error(ConsentError.DATABASE_CONNECTION_ERROR.getMessage(), e);
            throw new ConsentMgtException(ConsentError.DATABASE_CONNECTION_ERROR);
        }
    }

    //TODO : not yet implemented and tested
    @Override
    public ConsentAttributes getConsentAttributes(String consentId) throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentId)) {
            log.error(ConsentCoreServiceConstants.CONSENT_ID_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.CONSENT_ID_MISSING_ERROR_MSG);
        }


        try (Connection connection = DatabaseUtils.getDBConnection()) {
            ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();

            try {
                //TODO : pass orgInfo
                ConsentResource retrievedConsentResource = consentCoreDAO.getConsentResource(connection, consentId,
                        null);
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
                //TODO : pass orgInfo
                retrievedConsentAttributes = consentCoreDAO.getConsentAttributes(connection, consentId, null);

                // Commit transactions
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return retrievedConsentAttributes;
            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                        ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            }
        } catch (SQLException e) {
            log.error(ConsentError.DATABASE_CONNECTION_ERROR.getMessage(), e);
            throw new ConsentMgtException(ConsentError.DATABASE_CONNECTION_ERROR);
        }
    }


    //TODO : not yet implemented and tested
    @Override
    public ConsentAttributes updateConsentAttributes(String consentId, Map<String, String> consentAttributes)
            throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentId) || consentAttributes == null || consentAttributes.isEmpty()) {
            log.error(ConsentCoreServiceConstants.ATTRIBUTE_MAP_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.BAD_REQUEST,
                    ConsentCoreServiceConstants.ATTRIBUTE_MAP_MISSING_ERROR_MSG);
        }


        try (Connection connection = DatabaseUtils.getDBConnection()) {
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
                //TODO : pass orgInfo
                updatedAttributes = consentCoreDAO.getConsentAttributes(connection, consentId, null);

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
        } catch (SQLException e) {
            log.error(ConsentError.DATABASE_CONNECTION_ERROR.getMessage(), e);
            throw new ConsentMgtException(ConsentError.DATABASE_CONNECTION_ERROR);
        }

    }

    //TODO : not yet implemented and tested
    @Override
    public boolean deleteConsentAttributes(String consentId, ArrayList<String> attributeKeysList)
            throws
            ConsentMgtException {

        if (StringUtils.isBlank(consentId) || CollectionUtils.isEmpty(attributeKeysList)) {
            log.error(ConsentCoreServiceConstants.ATTRIBUTE_LIST_MISSING_ERROR_MSG);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.ATTRIBUTE_LIST_MISSING_ERROR_MSG);
        }


        try (Connection connection = DatabaseUtils.getDBConnection()) {
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
        } catch (SQLException e) {
            log.error(ConsentError.DATABASE_CONNECTION_ERROR.getMessage(), e);
            throw new ConsentMgtException(ConsentError.DATABASE_CONNECTION_ERROR);
        }
    }

    //TODO : not yet implemented and tested
    @Override
    public ArrayList<ConsentStatusAuditRecord> searchConsentStatusAuditRecords(String consentId, String status,
                                                                               String actionBy, Long fromTime,
                                                                               Long toTime, String statusAuditID)
            throws
            ConsentMgtException {

        ArrayList<ConsentStatusAuditRecord> auditRecords;

        try (Connection connection = DatabaseUtils.getDBConnection()) {
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
        } catch (SQLException e) {
            log.error(ConsentError.DATABASE_CONNECTION_ERROR.getMessage(), e);
            throw new ConsentMgtException(ConsentError.DATABASE_CONNECTION_ERROR);
        }
        return auditRecords;
    }

    //TODO : not yet implemented and tested
    @Override
    public ArrayList<ConsentStatusAuditRecord> getConsentStatusAuditRecords(ArrayList<String> consentIDs,
                                                                            Integer limit, Integer offset)
            throws
            ConsentMgtException {


        ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();

        try (Connection connection = DatabaseUtils.getDBConnection()) {
            //Retrieve consent status audit records.
            return consentCoreDAO.getConsentStatusAuditRecordsByConsentId(connection, consentIDs, limit, offset);

        } catch (ConsentDataRetrievalException e) {
            log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
        } catch (SQLException e) {
            log.error(ConsentError.DATABASE_CONNECTION_ERROR.getMessage(), e);
            throw new ConsentMgtException(ConsentError.DATABASE_CONNECTION_ERROR);
        }
    }

    //TODO : not yet implemented and tested
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

        ConsentCoreDAO consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
        try (Connection connection = DatabaseUtils.getDBConnection()) {
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

            log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
            return true;
        } catch (ConsentDataInsertionException | ConsentDataRetrievalException e) {
            log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
            throw new ConsentMgtException(Response.Status.INTERNAL_SERVER_ERROR,
                    ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
        } catch (SQLException e) {
            log.error(ConsentError.DATABASE_CONNECTION_ERROR.getMessage(), e);
            throw new ConsentMgtException(ConsentError.DATABASE_CONNECTION_ERROR);
        }
    }



}
