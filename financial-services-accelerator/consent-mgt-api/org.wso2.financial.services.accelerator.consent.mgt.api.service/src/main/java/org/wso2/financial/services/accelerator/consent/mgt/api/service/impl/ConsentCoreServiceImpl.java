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

package org.wso2.financial.services.accelerator.consent.mgt.api.service.impl;

import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.ConsentCoreDAO;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.constants.ConsentError;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.exceptions.ConsentDataDeletionException;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.exceptions.ConsentDataInsertionException;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.exceptions.ConsentDataRetrievalException;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.exceptions.ConsentDataUpdationException;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.exceptions.ConsentMgtException;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.models.ConsentAttributes;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.models.ConsentHistoryResource;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.models.ConsentStatusAuditRecord;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.persistence.ConsentStoreInitializer;
import org.wso2.financial.services.accelerator.consent.mgt.api.dao.util.DatabaseUtils;
import org.wso2.financial.services.accelerator.consent.mgt.api.service.ConsentCoreService;
import org.wso2.financial.services.accelerator.consent.mgt.api.service.constants.ConsentCoreServiceConstants;
import org.wso2.financial.services.accelerator.consent.mgt.api.service.util.ConsentCoreServiceUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Consent core service implementation.
 */
public class ConsentCoreServiceImpl implements ConsentCoreService {

    private static final Log log = LogFactory.getLog(ConsentCoreServiceImpl.class);

    private static volatile ConsentCoreServiceImpl instance;
    private final ConsentCoreDAO consentCoreDAO;

    public static ConsentCoreServiceImpl getInstance() throws ConsentMgtException {
        if (instance == null) {
            synchronized (ConsentCoreServiceImpl.class) {
                if (instance == null) {
                    instance = new ConsentCoreServiceImpl();
                }
            }
        }
        return instance;
    }

    private ConsentCoreServiceImpl() throws ConsentMgtException {
        this.consentCoreDAO = ConsentStoreInitializer.getInitializedConsentCoreDAOImpl();
    }

    @Override
    public DetailedConsentResource createConsent(DetailedConsentResource detailedConsentResource) throws
            ConsentMgtException {

        try {
            return ConsentCoreServiceUtil
                    .createConsentWithAuditRecord(consentCoreDAO, detailedConsentResource);
        } catch (ConsentDataInsertionException e) {
            log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG);
            throw new ConsentMgtException(e.getConsentError());
        }

    }

    @Override
    public DetailedConsentResource getDetailedConsent(String consentId, String orgId) throws
            ConsentMgtException {

        try (Connection connection = DatabaseUtils.getDBConnection()) {
            try {
                // Retrieve the detailed consent resource
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Retrieving detailed consent for consent ID: %s",
                            consentId.replaceAll("[\r\n]", "")));
                }
                DetailedConsentResource retrievedDetailedConsentResource = consentCoreDAO
                        .getDetailedConsentResource(connection, consentId, orgId);

                // Commit transactions
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return retrievedDetailedConsentResource;
            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentMgtException(e.getConsentError());
            }
        } catch (SQLException e) {
            log.error(ConsentError.DETAILED_CONSENT_RETRIEVAL_ERROR.getMessage().replaceAll("[\r\n]",
                    ""), e);
            throw new ConsentMgtException(ConsentError.DETAILED_CONSENT_RETRIEVAL_ERROR);
        }
    }

    @Override
    public List<DetailedConsentResource> searchDetailedConsents(String orgId, List<String> consentIds,
                                                                List<String> clientIDs,
                                                                List<String> consentTypes,
                                                                List<String> consentStatuses,
                                                                List<String> userIDs, Long fromTime,
                                                                     Long toTime,
                                                                     Integer limit, Integer offset) throws
            ConsentMgtException {

        // Input parameters except limit and offset are not validated since they are validated in the DAO method
        List<DetailedConsentResource> detailedConsentResources;

        try (Connection connection = DatabaseUtils.getDBConnection()) {
            try {
                log.debug("Searching detailed consents");
                detailedConsentResources = consentCoreDAO.searchConsents(connection, orgId, consentIds, clientIDs,
                        consentTypes, consentStatuses, userIDs, fromTime, toTime, limit, offset);

            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DETAIL_CONSENT_SEARCH_ERROR_MSG, e);
                throw new ConsentMgtException(ConsentError.CONSENT_SEARCH_ERROR);
            }

        } catch (SQLException e) {
            log.error(ConsentError.CONSENT_SEARCH_ERROR.getMessage().replaceAll("[\r\n]", ""), e);
            throw new ConsentMgtException(ConsentError.CONSENT_SEARCH_ERROR);
        }
        return detailedConsentResources;
    }

    @Override
    public boolean updateConsentExpiryTime(String consentId, long consentExpiryTime, String orgId)
            throws ConsentMgtException {

        try (Connection connection = DatabaseUtils.getDBConnection()) {
            if (System.currentTimeMillis() / 1000 > consentExpiryTime) {
                throw new ConsentMgtException(ConsentError.INVALID_CONSENT_EXPIRY_TIME);
            }

            DetailedConsentResource consentResource = consentCoreDAO.getDetailedConsentResource(connection, consentId,
                    orgId);

            if (consentResource.getCurrentStatus() != null) {
                if (consentResource.getCurrentStatus().equals(ConsentCoreServiceConstants.CONSENT_REVOKE_STATUS)) {
                    throw new ConsentMgtException(ConsentError.CONSENT_ALREADY_REVOKED_ERROR);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug(String.format("Updating the expiry time of the consent for ID: %s",
                        consentId.replaceAll("[\r\n]", "")));
            }
            consentCoreDAO.updateConsentExpiryTime(connection, consentId, consentExpiryTime);

            // Commit transaction
            DatabaseUtils.commitTransaction(connection);
            log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
        } catch (ConsentDataUpdationException e) {
            log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
            throw new ConsentMgtException(e.getConsentError());
        } catch (ConsentDataRetrievalException e) {
            log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            throw new ConsentMgtException(e.getConsentError());

        } catch (SQLException e) {
            log.error(ConsentError.CONSENT_EXPIRY_TIME_UPDATE_ERROR.getMessage().replaceAll("[\r\n]",
                    ""), e);
            throw new ConsentMgtException(ConsentError.CONSENT_EXPIRY_TIME_UPDATE_ERROR);
        }

        return true;

    }

    @Override
    public void updateConsentStatus(String consentId, String newConsentStatus,
                                    String reason, String userId, String orgId) throws
            ConsentMgtException {

        try (Connection connection = DatabaseUtils.getDBConnection()) {
            try {
                // Get the existing consent to validate status
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Retrieving the consent for ID: %s to validate status",
                            consentId.replaceAll("[\r\n]", "")));
                }
                DetailedConsentResource existingConsentResource = consentCoreDAO
                        .getDetailedConsentResource(connection, consentId, orgId);

                // Update consent status with new status
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Updating the status of the consent for ID: %s",
                            consentId.replaceAll("[\r\n]", "")));
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

            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentMgtException(e.getConsentError());
            } catch (ConsentDataInsertionException e) {
                log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(e.getConsentError());
            } catch (ConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(e.getConsentError());
            }
        } catch (SQLException e) {
            log.error(ConsentError.CONSENT_STATUS_UPDATE_ERROR.getMessage().replaceAll("[\r\n]", ""), e);
            throw new ConsentMgtException(ConsentError.CONSENT_STATUS_UPDATE_ERROR);
        }
    }

    @Override
    public void consentStatusBulkUpdate(String orgId, String clientId, String status, String reason, String userId,
                                        String consentType, List<String> applicableExistingStatus) throws
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
        List<DetailedConsentResource> detailedConsentResources;

        // get consents by client id and update status
        try (Connection connection = DatabaseUtils.getDBConnection()) {
            try {
                detailedConsentResources = consentCoreDAO.searchConsents(connection, orgId, new ArrayList<>(),
                        clientIds,
                        consentTypes, applicableExistingStatus, null, null,
                        null, null, null);

                if (detailedConsentResources != null) {
                    if (detailedConsentResources.isEmpty()) {
                        log.error(ConsentError.CONSENT_NOT_FOUND.getMessage().replaceAll("[\r\n]", ""));
                        throw new ConsentMgtException(ConsentError.CONSENT_NOT_FOUND);
                    }
                }
                // extract list of consentIds
                List<String> consentIds = detailedConsentResources.stream().
                        map(DetailedConsentResource::getConsentId).collect(Collectors.toList());

                // Update consent status
                consentCoreDAO.bulkConsentStatusUpdate(connection, consentIds, status, orgId);
                DatabaseUtils.commitTransaction(connection);

                //TODO : handle history
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentError.DETAILED_CONSENT_RETRIEVAL_ERROR.getMessage().replaceAll("[\r\n]", ""), e);
                throw new ConsentMgtException(ConsentError.DETAILED_CONSENT_RETRIEVAL_ERROR);
            } catch (ConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(e.getConsentError());
            }
        } catch (SQLException e) {
            log.error(ConsentError.CONSENT_UPDATE_ERROR.getMessage().replaceAll("[\r\n]", ""), e);
            throw new ConsentMgtException(ConsentError.CONSENT_UPDATE_ERROR);
        }

    }

    @Override
    public boolean deleteConsent(String consentId, String orgId) throws
            ConsentMgtException {

        try (Connection connection = DatabaseUtils.getDBConnection()) {
            // Delete consent
            if (log.isDebugEnabled()) {
                log.debug(String.format("Deleting the consent for ID: %s",
                        consentId.replaceAll("[\r\n]", "")));
            }
            consentCoreDAO.deleteConsent(connection, consentId, orgId);

            // Commit transaction
            DatabaseUtils.commitTransaction(connection);
            log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
            return true;
        } catch (ConsentDataDeletionException e) {
            log.error(ConsentCoreServiceConstants.DATA_DELETION_ROLLBACK_ERROR_MSG, e);
            throw new ConsentMgtException(e.getConsentError());
        } catch (SQLException e) {
            log.error(ConsentError.CONSENT_DELETE_ERROR.getMessage().replaceAll("[\r\n]", ""), e);
            throw new ConsentMgtException(ConsentError.CONSENT_DELETE_ERROR);
        }
    }

    @Override
    public boolean revokeConsent(String consentId, String orgId, String actionBy, String revokedReason)
            throws ConsentMgtException {

        try (Connection connection = DatabaseUtils.getDBConnection()) {
            try {
                // Get existing detailed consent
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Retrieving existing consent of ID: %s for status validation",
                            consentId.replaceAll("[\r\n]", "")));
                }
                DetailedConsentResource retrievedDetailedConsentResource = consentCoreDAO
                        .getDetailedConsentResource(connection, consentId, orgId);

                String previousConsentStatus = retrievedDetailedConsentResource.getCurrentStatus();
                if (previousConsentStatus != null) {
                    if (previousConsentStatus.equals(ConsentCoreServiceConstants.CONSENT_REVOKE_STATUS)) {
                        throw new ConsentMgtException(ConsentError.CONSENT_ALREADY_REVOKED_ERROR);
                    }
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
            log.error(ConsentError.CONSENT_REVOKE_ERROR.getMessage().replaceAll("[\r\n]", ""), e);
            throw new ConsentMgtException(ConsentError.CONSENT_REVOKE_ERROR);
        }
        return true;
    }

    @Override
    public List<AuthorizationResource> createConsentAuthorizations(String consentId,
                                                                   List<AuthorizationResource>
                                                                           authorizationResources)
            throws ConsentMgtException {

        try (Connection connection = DatabaseUtils.getDBConnection()) {
            try {
                // Create authorization resource
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Creating authorization resource for the consent of ID: %s",
                            consentId.replaceAll("[\r\n]", "")));
                }

                // check wheather the consent is already revoked
                DetailedConsentResource consentResource = consentCoreDAO
                        .getDetailedConsentResource(connection, consentId);
                if (consentResource.getCurrentStatus().equals(ConsentCoreServiceConstants.CONSENT_REVOKE_STATUS)) {
                    throw new ConsentMgtException(ConsentError.CONSENT_ALREADY_REVOKED_ERROR);
                }
                List<AuthorizationResource> storedAuthorizationResource =
                        consentCoreDAO.storeBulkAuthorizationResources(connection, consentId, authorizationResources);

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
            log.error(ConsentError.AUTHORIZATION_RESOURCE_INSERTION_ERROR.getMessage().replaceAll("[\r\n]", ""), e);
            throw new ConsentMgtException(ConsentError.AUTHORIZATION_RESOURCE_INSERTION_ERROR);
        }
    }

    @Override
    public AuthorizationResource getAuthorizationResource(String authorizationId, String orgId) throws
            ConsentMgtException {

        try (Connection connection = DatabaseUtils.getDBConnection()) {
            try {
                AuthorizationResource retrievedAuthorizationResource;

                if (log.isDebugEnabled()) {
                    log.debug(String.format("Retrieving authorization resource for authorization ID: %s",
                            authorizationId.replaceAll("[\r\n]", "")));
                }
                retrievedAuthorizationResource = consentCoreDAO.getAuthorizationResource(connection, authorizationId,
                        orgId);

                // Commit transactions
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return retrievedAuthorizationResource;
            } catch (ConsentDataRetrievalException e) {
                throw new ConsentMgtException(e.getConsentError());

            }
        } catch (SQLException e) {
            log.error(ConsentError.AUTHORIZATION_RESOURCE_RETRIEVAL_ERROR.getMessage().replaceAll("[\r\n]", ""), e);
            throw new ConsentMgtException(ConsentError.AUTHORIZATION_RESOURCE_RETRIEVAL_ERROR);
        }
    }

    @Override
    public void updateAuthorizationResource(String authorizationId, AuthorizationResource authorizationResource,
                                            String orgId) throws
            ConsentMgtException {

        try (Connection connection = DatabaseUtils.getDBConnection()) {
            try {
                // Update authorization resource
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Updating the authorization resource for ID: %s",
                            authorizationId.replaceAll("[\r\n]", "")));
                }
                consentCoreDAO.updateAuthorizationResource(connection, authorizationId, authorizationResource);

                // Commit transaction
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
            } catch (ConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.DATA_UPDATE_ROLLBACK_ERROR_MSG, e);
                DatabaseUtils.rollbackTransaction(connection);
                throw new ConsentMgtException(e.getConsentError());

            }
        } catch (SQLException e) {
            log.error(ConsentError.AUTHORIZATION_RESOURCE_UPDATE_ERROR.getMessage().replaceAll("[\r\n]", ""), e);
            throw new ConsentMgtException(ConsentError.AUTHORIZATION_RESOURCE_UPDATE_ERROR);
        }
    }

    @Override
    public boolean deleteAuthorizationResource(String authorizationId, String orgId) throws
            ConsentMgtException {

        try (Connection connection = DatabaseUtils.getDBConnection()) {
            try {

                // check if the authorization resource exists and validate the orgId
                consentCoreDAO.getAuthorizationResource(connection, authorizationId, orgId);

                // Delete authorization resource
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Deleting the authorization resource for ID: %s",
                            authorizationId.replaceAll("[\r\n]", "")));
                }
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
            log.error(ConsentError.DATABASE_CONNECTION_ERROR.getMessage().replaceAll("[\r\n]", ""), e);
            throw new ConsentMgtException(ConsentError.DATABASE_CONNECTION_ERROR);
        }
    }

    //TODO : not yet implemented and tested
    @Override
    public boolean storeConsentAttributes(String consentId, Map<String, Object> consentAttributes)
            throws  ConsentMgtException {

        boolean isConsentAttributesStored;
        try (Connection connection = DatabaseUtils.getDBConnection()) {
            try {
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
                throw new ConsentMgtException(e.getConsentError());
            }
        } catch (SQLException e) {
            log.error(ConsentError.DATABASE_CONNECTION_ERROR.getMessage().replaceAll("[\r\n]", ""), e);
            throw new ConsentMgtException(ConsentError.DATABASE_CONNECTION_ERROR);
        }

        return isConsentAttributesStored;
    }

    //TODO : not yet implemented and tested
    @Override
    public ConsentAttributes getConsentAttributes(String consentId, String orgId, List<String> consentAttributeKeys)
            throws ConsentMgtException {


        try (Connection connection = DatabaseUtils.getDBConnection()) {
            try {
                ConsentResource retrievedConsentResource = consentCoreDAO.getConsentResource(connection, consentId,
                        orgId);
                if (retrievedConsentResource == null) {
                    String errorMessage = String.format("Consent ID  : %s is not available in the database",
                            consentId.replaceAll("[\r\n]", ""));
                    log.error(errorMessage);
                    throw new ConsentMgtException(ConsentError.CONSENT_NOT_FOUND);
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
                retrievedConsentAttributes = consentCoreDAO.getConsentAttributes(connection, consentId, orgId,
                        consentAttributeKeys);

                // Commit transactions
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return retrievedConsentAttributes;
            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentMgtException(e.getConsentError());
            }
        } catch (SQLException e) {
            log.error(ConsentError.DATABASE_CONNECTION_ERROR.getMessage().replaceAll("[\r\n]", ""), e);
            throw new ConsentMgtException(ConsentError.DATABASE_CONNECTION_ERROR);
        }
    }

    //TODO : not yet implemented and tested
    @Override
    public ConsentAttributes getConsentAttributes(String consentId, String orgId) throws ConsentMgtException {

        try (Connection connection = DatabaseUtils.getDBConnection()) {
            try {
                ConsentResource retrievedConsentResource = consentCoreDAO.getConsentResource(connection, consentId,
                        orgId);
                if (retrievedConsentResource == null) {
                    String errorMessage = String.format("Consent ID  : %s is not available in the database",
                            consentId.replaceAll("[\r\n]", ""));
                    log.error(errorMessage);
                    throw new ConsentMgtException(ConsentError.CONSENT_NOT_FOUND);
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
                retrievedConsentAttributes = consentCoreDAO.getConsentAttributes(connection, consentId, orgId);

                // Commit transactions
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return retrievedConsentAttributes;
            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentMgtException(e.getConsentError());
            }
        } catch (SQLException e) {
            log.error(ConsentError.DATABASE_CONNECTION_ERROR.getMessage().replaceAll("[\r\n]", ""), e);
            throw new ConsentMgtException(ConsentError.DATABASE_CONNECTION_ERROR);
        }
    }

    //TODO : not yet implemented and tested
    @Override
    public ConsentAttributes updateConsentAttributes(String consentId, Map<String, Object> consentAttributes)
            throws ConsentMgtException {

        try (Connection connection = DatabaseUtils.getDBConnection()) {
            try {
                ConsentAttributes updatedAttributes;
                if (log.isDebugEnabled()) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Updating the attributes of the consent for ID: %s",
                                consentId.replaceAll("[\r\n]", "")));
                    }
                }
                consentCoreDAO.updateConsentAttributes(connection, consentId, consentAttributes);
                //TODO : pass orgId
                updatedAttributes = consentCoreDAO.getConsentAttributes(connection, consentId, null);

                // Commit transactions
                DatabaseUtils.commitTransaction(connection);
                log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
                return updatedAttributes;
            } catch (ConsentDataUpdationException e) {
                log.error(ConsentCoreServiceConstants.ATTRIBUTE_UPDATE_ERROR_MSG, e);
                throw new ConsentMgtException(e.getConsentError());
            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
                throw new ConsentMgtException(e.getConsentError());
            }
        } catch (SQLException e) {
            log.error(ConsentError.DATABASE_CONNECTION_ERROR.getMessage().replaceAll("[\r\n]", ""), e);
            throw new ConsentMgtException(ConsentError.DATABASE_CONNECTION_ERROR);
        }

    }

    //TODO : not yet implemented and tested
    @Override
    public boolean deleteConsentAttributes(String consentId, List<String> attributeKeysList)
            throws ConsentMgtException {

        try (Connection connection = DatabaseUtils.getDBConnection()) {
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
                throw new ConsentMgtException(e.getConsentError());
            }
        } catch (SQLException e) {
            log.error(ConsentError.DATABASE_CONNECTION_ERROR.getMessage().replaceAll("[\r\n]", ""), e);
            throw new ConsentMgtException(ConsentError.DATABASE_CONNECTION_ERROR);
        }
    }

    //TODO : not yet implemented and tested
    @Override
    public ArrayList<ConsentStatusAuditRecord> searchConsentStatusAuditRecords(String consentId, String status,
                                                                               String actionBy, Long fromTime,
                                                                               Long toTime, String statusAuditId)
            throws ConsentMgtException {

        ArrayList<ConsentStatusAuditRecord> auditRecords;

        try (Connection connection = DatabaseUtils.getDBConnection()) {
            try {
                log.debug("Searching audit records");
                auditRecords = consentCoreDAO.getConsentStatusAuditRecords(connection, consentId, status, actionBy,
                        fromTime, toTime, statusAuditId);

            } catch (ConsentDataRetrievalException e) {
                log.error(ConsentCoreServiceConstants.AUDIT_RECORD_SEARCH_ERROR_MSG, e);
                throw new ConsentMgtException(e.getConsentError());
            }

            // Commit transactions
            DatabaseUtils.commitTransaction(connection);
            log.debug(ConsentCoreServiceConstants.TRANSACTION_COMMITTED_LOG_MSG);
        } catch (SQLException e) {
            log.error(ConsentError.DATABASE_CONNECTION_ERROR.getMessage().replaceAll("[\r\n]", ""), e);
            throw new ConsentMgtException(ConsentError.DATABASE_CONNECTION_ERROR);
        }
        return auditRecords;
    }

    //TODO : not yet implemented and tested
    @Override
    public List<ConsentStatusAuditRecord> getConsentStatusAuditRecords(List<String> consentIds,
                                                                            Integer limit, Integer offset)
            throws ConsentMgtException {
        try (Connection connection = DatabaseUtils.getDBConnection()) {
            //Retrieve consent status audit records.
            return consentCoreDAO.getConsentStatusAuditRecordsByConsentId(connection, consentIds, limit, offset);

        } catch (ConsentDataRetrievalException e) {
            log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG, e);
            throw new ConsentMgtException(e.getConsentError());
        } catch (SQLException e) {
            log.error(ConsentError.DATABASE_CONNECTION_ERROR.getMessage().replaceAll("[\r\n]", ""), e);
            throw new ConsentMgtException(ConsentError.DATABASE_CONNECTION_ERROR);
        }
    }

    //TODO : not yet implemented and tested
    @Override
    public boolean storeConsentAmendmentHistory(String statusAuditRecordId,
                                                ConsentHistoryResource consentHistoryResource,
                                                DetailedConsentResource detailedCurrentConsent)
            throws ConsentMgtException {

        if (StringUtils.isBlank(statusAuditRecordId) || consentHistoryResource == null ||
                StringUtils.isBlank(consentHistoryResource.getReason()) ||
                consentHistoryResource.getTimestamp() == 0) {
            log.error(ConsentCoreServiceConstants.AMEND_DETAILS_MISSING_ERROR_MSG);
            throw new ConsentMgtException(ConsentError.AMENDMENT_DETAILS_MISSING);
        }

        String historyID = consentHistoryResource.getHistoryID();
        if (StringUtils.isBlank(historyID)) {
            historyID = String.valueOf(UUID.randomUUID());
        }
        long amendedTimestamp = consentHistoryResource.getTimestamp();
        String amendmentReason = consentHistoryResource.getReason();
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

            // store only the changes in consent Auth Resources to CA history
            Map<String, JSONObject> changedConsentAuthResourcesJsonDataMap = ConsentCoreServiceUtil
                    .getChangedConsentAuthResourcesDataJSONMap(detailedCurrentConsent.getAuthorizationResources(),
                            (ArrayList<AuthorizationResource>) detailedHistoryConsent.getAuthorizationResources());
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
        } catch (ConsentDataInsertionException  e) {
            log.error(ConsentCoreServiceConstants.DATA_INSERTION_ROLLBACK_ERROR_MSG, e);
            throw new ConsentMgtException(e.getConsentError());
        } catch (ConsentDataRetrievalException e) {
            log.error(ConsentCoreServiceConstants.DATA_RETRIEVE_ERROR_MSG);
            throw new ConsentMgtException(e.getConsentError());
        } catch (SQLException e) {
            log.error(ConsentError.DATABASE_CONNECTION_ERROR.getMessage().replaceAll("[\r\n]", ""), e);
            throw new ConsentMgtException(ConsentError.DATABASE_CONNECTION_ERROR);
        }
    }

}
