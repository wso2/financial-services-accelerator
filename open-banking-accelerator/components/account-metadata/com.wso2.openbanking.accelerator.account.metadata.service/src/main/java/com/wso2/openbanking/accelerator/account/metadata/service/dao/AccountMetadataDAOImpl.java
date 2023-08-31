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

package com.wso2.openbanking.accelerator.account.metadata.service.dao;

import com.wso2.openbanking.accelerator.account.metadata.service.dao.queries.AccountMetadataDBQueries;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.DatabaseUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of AccountMetadataDAO.
 */
public class AccountMetadataDAOImpl implements AccountMetadataDAO {

    private static final Log log = LogFactory.getLog(AccountMetadataDAOImpl.class);
    private static final String KEY = "METADATA_KEY";
    private static final String VALUE = "METADATA_VALUE";
    private static final String USER_ID = "USER_ID";

    //Error messages
    private static final String DB_CONNECTION_NULL_ERROR = "Database connection is null.";
    private static final String ACCOUNT_ID_USER_ID_MISSING_ERROR = "Account Id or User Id is not provided";
    private static final String ACCOUNT_METADATA_MISSING_ERROR = "Metadata key or Metadata value is not provided";
    private static final String ERROR_WHILE_DELETING_METADATA = "Error occurred while deleting account metadata.";


    AccountMetadataDBQueries sqlStatements;

    public AccountMetadataDAOImpl(AccountMetadataDBQueries sqlStatements) {
        this.sqlStatements = sqlStatements;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int storeAccountMetadata(Connection dbConnection, String accountId, String userId,
                                    String metadataKey, String metadataValue) throws OpenBankingException {

        int noOfRows;

        if (StringUtils.isBlank(accountId) || StringUtils.isBlank(userId)) {
            log.error(ACCOUNT_ID_USER_ID_MISSING_ERROR);
            throw new OpenBankingException(ACCOUNT_ID_USER_ID_MISSING_ERROR);
        }
        if (StringUtils.isBlank(metadataKey) || StringUtils.isBlank(metadataValue)) {
            log.error(ACCOUNT_METADATA_MISSING_ERROR);
            throw new OpenBankingException(ACCOUNT_METADATA_MISSING_ERROR);
        }
        if (dbConnection == null) {
            log.error(DB_CONNECTION_NULL_ERROR);
            throw new OpenBankingException(DB_CONNECTION_NULL_ERROR);
        }

        try {
            String storeAttributeSqlStatement = sqlStatements.getStoreAccountMetadataPreparedStatement();
            Savepoint savepoint = dbConnection.setSavepoint();
            log.debug("Storing account metadata data in the database for account-id " + accountId + " and " +
                    "user-id " + userId);
            try (PreparedStatement prepStmt = dbConnection.prepareStatement(storeAttributeSqlStatement)) {
                prepStmt.setString(1, accountId);
                prepStmt.setString(2, userId);
                prepStmt.setString(3, metadataKey);
                prepStmt.setString(4, metadataValue);
                prepStmt.setTimestamp(5, new Timestamp(new Date().getTime()));

                if (log.isDebugEnabled()) {
                    log.debug("Added data for the key " + metadataKey + " and value " + metadataValue +
                            " to be inserted to the database.");
                }
                noOfRows = prepStmt.executeUpdate();
                if (noOfRows > 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("The query affected " + noOfRows + " rows.");
                        log.debug("Stored attributes for account-id" + accountId + " and user-id " + userId +
                                " in the database.");
                    }
                    dbConnection.commit();
                } else {
                    dbConnection.rollback(savepoint);
                    log.error("Error occurred while inserting account metadata data. Any changes occurred " +
                            "during the failed transaction are rolled back.");
                    throw new OpenBankingException("Error occurred while inserting account metadata " +
                            "data.");
                }
            } catch (SQLException e) {
                dbConnection.rollback(savepoint);
                log.error("Error occurred while inserting account metadata data.", e);
                throw new OpenBankingException("Error occurred while inserting account metadata " +
                        "data.", e);
            }
        } catch (SQLException e) {
            log.error("Error occurred while interacting with the database connection.", e);
            throw new OpenBankingException("Error occurred while interacting with the database connection", e);
        } finally {
            DatabaseUtil.closeConnection(dbConnection);
        }
        return noOfRows;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int updateAccountMetadata(Connection dbConnection, String accountId, String userId,
                                     String metadataKey, String metadataValue) throws OpenBankingException {

        int noOfRows;

        if (StringUtils.isBlank(accountId) || StringUtils.isBlank(userId)) {
            log.error(ACCOUNT_ID_USER_ID_MISSING_ERROR);
            throw new OpenBankingException(ACCOUNT_ID_USER_ID_MISSING_ERROR);
        }
        if (StringUtils.isBlank(metadataKey) || StringUtils.isBlank(metadataValue)) {
            log.error(ACCOUNT_METADATA_MISSING_ERROR);
            throw new OpenBankingException(ACCOUNT_METADATA_MISSING_ERROR);
        }
        if (dbConnection == null) {
            log.error(DB_CONNECTION_NULL_ERROR);
            throw new OpenBankingException(DB_CONNECTION_NULL_ERROR);
        }

        try {
            String storeAttributeSqlStatement = sqlStatements.getUpdateAccountMetadataPreparedStatement();
            Savepoint savepoint = dbConnection.setSavepoint();
            log.debug("Storing account metadata data in the database for account-id " + accountId + " and " +
                    "user-id " + userId);
            try (PreparedStatement prepStmt = dbConnection.prepareStatement(storeAttributeSqlStatement)) {
                prepStmt.setString(1, metadataValue);
                prepStmt.setTimestamp(2, new Timestamp(new Date().getTime()));
                prepStmt.setString(3, accountId);
                prepStmt.setString(4, userId);
                prepStmt.setString(5, metadataKey);
                prepStmt.executeUpdate();
                if (log.isDebugEnabled()) {
                    log.debug("Added data for the key " + metadataKey + " and value" + metadataValue +
                            " to be updated in the database.");
                }
                noOfRows = prepStmt.executeUpdate();
                if (noOfRows > 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("The query affected " + noOfRows + " rows.");
                        log.debug("Updated attributes for account-id" + accountId + " and user-id " + userId +
                                " in the database.");
                    }
                } else {
                    log.info("No rows were affected in the transaction. No change was made to existing account " +
                            "metadata.");
                }
                dbConnection.commit();
            } catch (SQLException e) {
                dbConnection.rollback(savepoint);
                log.error("Error occurred while updating account metadata.", e);
                throw new OpenBankingException("Error occurred while inserting account metadata.", e);
            }
        } catch (SQLException e) {
            log.error("Error occurred while interacting with the database connection.", e);
            throw new OpenBankingException("Error occurred while interacting with the database connection", e);
        } finally {
            DatabaseUtil.closeConnection(dbConnection);
        }
        return noOfRows;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getAccountMetadataMap(Connection dbConnection, String accountId, String userId) throws
            OpenBankingException {

        if (StringUtils.isBlank(accountId) || StringUtils.isBlank(userId)) {
            log.error(ACCOUNT_ID_USER_ID_MISSING_ERROR);
            throw new OpenBankingException(ACCOUNT_ID_USER_ID_MISSING_ERROR);
        }
        if (dbConnection == null) {
            log.error(DB_CONNECTION_NULL_ERROR);
            throw new OpenBankingException(DB_CONNECTION_NULL_ERROR);
        }
        HashMap<String, String> attributesMap = new HashMap<>();
        final String retrieveAttributeSqlStatement = sqlStatements.getRetrieveAccountMetadataPreparedStatement();

        if (log.isDebugEnabled()) {
            log.debug("Retrieving account metadata for account-id " + accountId + " and user-id " +
                    userId);
        }
        try (PreparedStatement prepStmt = dbConnection.prepareStatement(retrieveAttributeSqlStatement)) {
            prepStmt.setString(1, accountId);
            prepStmt.setString(2, userId);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    attributesMap.put(rs.getString(KEY), rs.getString(VALUE));
                    if (log.isDebugEnabled()) {
                        log.debug("Added attribute with key " + rs.getString(KEY) + " and value " +
                                rs.getString(VALUE) + "to the map.");
                    }
                }
            } catch (SQLException e) {
                log.error("Error occurred while reading retrieved result set for account-id " + accountId +
                        "and user-id " + userId, e);
                throw new OpenBankingException("Error occurred while reading retrieved result set for " +
                        "account-id " + accountId + " and user-id " + userId, e);
            }
        } catch (SQLException e) {
            log.error("Error occurred while retrieving account metadata from database for account-id " +
                    accountId + " and user-id " + userId, e);
            throw new OpenBankingException("Error occurred while retrieving account metadata " +
                    "from database for account-id " + accountId + " and user-id " + userId, e);
        } finally {
            DatabaseUtil.closeConnection(dbConnection);
        }
        return attributesMap;
    }

    @Override
    public Map<String, String> getMetadataForAccountIdAndKey(Connection dbConnection, String accountId, String key)
            throws OpenBankingException {

        if (StringUtils.isBlank(accountId) || StringUtils.isBlank(key)) {
            log.error("AccountId or Key not found in the request");
            throw new OpenBankingException("AccountId and Key should be submitted in order to proceed.");
        }
        if (dbConnection == null) {
            log.error(DB_CONNECTION_NULL_ERROR);
            throw new OpenBankingException(DB_CONNECTION_NULL_ERROR);
        }
        HashMap<String, String> attributesMap = new HashMap<>();
        final String retrieveAttributeSqlStatement = sqlStatements.
                getRetrieveMetadataByAccountIdAndKeyPreparedStatement();

        if (log.isDebugEnabled()) {
            log.debug("Retrieving account metadata for account-id " + accountId + " and metadata-key " +
                    key);
        }
        try (PreparedStatement prepStmt = dbConnection.prepareStatement(retrieveAttributeSqlStatement)) {
            prepStmt.setString(1, accountId);
            prepStmt.setString(2, key);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    attributesMap.put(rs.getString(USER_ID), rs.getString(VALUE));
                    if (log.isDebugEnabled()) {
                        log.debug("Added attribute with user-id " + rs.getString(USER_ID) + " and value " +
                                rs.getString(VALUE) + "to the map.");
                    }
                }
            } catch (SQLException e) {
                log.error("Error occurred while reading retrieved result set for account-id " + accountId +
                        "and key " + key, e);
                throw new OpenBankingException("Error occurred while reading retrieved result set for " +
                        "account-id " + accountId + " and key " + key, e);
            }
        } catch (SQLException e) {
            log.error("Error occurred while retrieving account metadata from database for account-id " +
                    accountId + " and key " + key, e);
            throw new OpenBankingException("Error occurred while retrieving account metadata " +
                    "from database for account-id " + accountId + " and key " + key, e);
        } finally {
            DatabaseUtil.closeConnection(dbConnection);
        }
        return attributesMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAccountMetadataByKey(Connection dbConnection, String accountId, String userId, String key) throws
            OpenBankingException {

        if (StringUtils.isBlank(accountId) || StringUtils.isBlank(userId) || StringUtils.isBlank(key)) {
            log.error("AccountId, UserId or Key not found in the request");
            throw new OpenBankingException("AccountId, UserId and Key should be submitted in order to " +
                    "proceed.");
        }
        if (dbConnection == null) {
            log.error(DB_CONNECTION_NULL_ERROR);
            throw new OpenBankingException(DB_CONNECTION_NULL_ERROR);
        }
        String attributeValue = null;
        final String retrieveAttributeSqlStatement = sqlStatements.
                getRetrieveAccountMetadataByKeyPreparedStatement();

        if (log.isDebugEnabled()) {
            log.debug("Retrieving account metadata for account-id " + accountId + " and user-id " + userId);
        }
        try (PreparedStatement prepStmt = dbConnection.prepareStatement(retrieveAttributeSqlStatement)) {
            prepStmt.setString(1, accountId);
            prepStmt.setString(2, userId);
            prepStmt.setString(3, key);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    attributeValue = rs.getString(VALUE);
                    if (log.isDebugEnabled()) {
                        log.debug("Retrieved attribute with key " + key + " and value " + attributeValue);
                    }
                }
                dbConnection.commit();
            } catch (SQLException e) {
                log.error("Error occurred while reading retrieved result set for account-id " + accountId +
                        " and user-id " + userId, e);
                throw new OpenBankingException("Error occurred while reading retrieved result set for " +
                        "account-id " + accountId + " and user-id " + userId, e);
            }
        } catch (SQLException e) {
            log.error("Error occurred while retrieving account metadata from database for account-id " +
                    accountId + " and user-id " + userId, e);
            throw new OpenBankingException("Error occurred while retrieving account metadata " +
                    "from database for account-id " + accountId + " and user-id " + userId, e);
        } finally {
            DatabaseUtil.closeConnection(dbConnection);
        }
        return attributeValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int deleteAccountMetadata(Connection dbConnection, String accountId, String userId)
            throws OpenBankingException {

        int noOfRows;
        if (StringUtils.isBlank(accountId) || StringUtils.isBlank(userId)) {
            log.error(ACCOUNT_ID_USER_ID_MISSING_ERROR);
            throw new OpenBankingException(ACCOUNT_ID_USER_ID_MISSING_ERROR);
        }
        if (dbConnection == null) {
            log.error(DB_CONNECTION_NULL_ERROR);
            throw new OpenBankingException(DB_CONNECTION_NULL_ERROR);
        }
        try {
            String deleteAttributeSqlStatement = sqlStatements.getDeleteAccountMetadataPreparedStatement();
            dbConnection.setAutoCommit(false);
            Savepoint savepoint = dbConnection.setSavepoint();

            try (PreparedStatement prepStmt = dbConnection.prepareStatement(deleteAttributeSqlStatement)) {
                prepStmt.setString(1, accountId);
                prepStmt.setString(2, userId);
                noOfRows = prepStmt.executeUpdate();
                if (noOfRows >= 0) {
                    dbConnection.commit();
                    if (log.isDebugEnabled()) {
                        log.debug("Deleted " + noOfRows + " account metadata for account-id " + accountId +
                                "and user-id " + userId);
                    }
                } else {
                    dbConnection.rollback(savepoint);
                    log.error(ERROR_WHILE_DELETING_METADATA + "Any changes occurred " +
                            "during the failed transaction are rolled back.");
                    throw new OpenBankingException(ERROR_WHILE_DELETING_METADATA);
                }
            } catch (SQLException e) {
                dbConnection.rollback(savepoint);
                log.error(ERROR_WHILE_DELETING_METADATA, e);
                throw new OpenBankingException(ERROR_WHILE_DELETING_METADATA, e);
            }
        } catch (SQLException e) {
            log.error("Error occurred while interacting with the database connection.", e);
            throw new OpenBankingException("Error occurred while interacting with the database connection", e);
        } finally {
            DatabaseUtil.closeConnection(dbConnection);
        }
        return noOfRows;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int deleteAccountMetadataByKey(Connection dbConnection, String accountId, String userId, String key) throws
            OpenBankingException {

        if (StringUtils.isBlank(accountId) || StringUtils.isBlank(userId) || StringUtils.isBlank(key)) {
            log.error("AccountId, UserId or Key not found in the request");
            throw new OpenBankingException("AccountId, UserId and Key should be submitted in order to " +
                    "proceed.");
        }
        if (dbConnection == null) {
            log.error(DB_CONNECTION_NULL_ERROR);
            throw new OpenBankingException(DB_CONNECTION_NULL_ERROR);
        }
        int noOfRows;
        try {
            String deleteAttributeSqlStatement = sqlStatements.getDeleteAccountMetadataByKeyPreparedStatement();
            Savepoint savepoint = dbConnection.setSavepoint();

            try (PreparedStatement prepStmt = dbConnection.prepareStatement(deleteAttributeSqlStatement)) {
                prepStmt.setString(1, accountId);
                prepStmt.setString(2, userId);
                prepStmt.setString(3, key);
                noOfRows = prepStmt.executeUpdate();
                if (noOfRows >= 0) {
                    dbConnection.commit();
                    if (log.isDebugEnabled()) {
                        log.debug("Deleted account metadata for account-id " + accountId + "and user-id " +
                                userId + "for the key " + key);
                    }
                } else {
                    dbConnection.rollback(savepoint);
                    log.error(ERROR_WHILE_DELETING_METADATA + "Any changes occurred " +
                            "during the failed transaction are rolled back.");
                    throw new OpenBankingException(ERROR_WHILE_DELETING_METADATA);
                }
            } catch (SQLException e) {
                dbConnection.rollback(savepoint);
                log.error("Error occurred while deleting account metadata data.", e);
                throw new OpenBankingException("Error occurred while deleting account metadata data.", e);
            }
        } catch (SQLException e) {
            log.error("Error occurred while interacting with the database connection.", e);
            throw new OpenBankingException("Error occurred while interacting with the database connection", e);
        } finally {
            DatabaseUtil.closeConnection(dbConnection);
        }
        return noOfRows;
    }

    @Override
    public int deleteAccountMetadataByKeyForAllUsers(Connection dbConnection, String accountId, String key) throws
            OpenBankingException {

        int noOfRows;
        if (StringUtils.isBlank(accountId) || StringUtils.isBlank(key)) {
            log.error("AccountId or Key not found in the request");
            throw new OpenBankingException("AccountId and Key should be submitted in order to " +
                    "proceed.");
        }
        if (dbConnection == null) {
            log.error(DB_CONNECTION_NULL_ERROR);
            throw new OpenBankingException(DB_CONNECTION_NULL_ERROR);
        }
        try {
            String deleteAttributeSqlStatement = sqlStatements.
                    getDeleteAccountMetadataByKeyForAllUsersPreparedStatement();
            Savepoint savepoint = dbConnection.setSavepoint();

            try (PreparedStatement prepStmt = dbConnection.prepareStatement(deleteAttributeSqlStatement)) {
                prepStmt.setString(1, accountId);
                prepStmt.setString(2, key);
                noOfRows = prepStmt.executeUpdate();
                if (noOfRows >= 0) {
                    dbConnection.commit();
                    if (log.isDebugEnabled()) {
                        log.debug("Deleted account metadata for account-id " + accountId + "for the key " + key);
                    }
                } else {
                    dbConnection.rollback(savepoint);
                    log.error(ERROR_WHILE_DELETING_METADATA + "Any changes occurred " +
                            "during the failed transaction are rolled back.");
                    throw new OpenBankingException(ERROR_WHILE_DELETING_METADATA);
                }
            } catch (SQLException e) {
                dbConnection.rollback(savepoint);
                log.error("Error occurred while deleting account metadata data.", e);
                throw new OpenBankingException("Error occurred while deleting account metadata data.", e);
            }
        } catch (SQLException e) {
            log.error("Error occurred while interacting with the database connection.", e);
            throw new OpenBankingException("Error occurred while interacting with the database connection", e);
        } finally {
            DatabaseUtil.closeConnection(dbConnection);
        }
        return noOfRows;
    }
}
