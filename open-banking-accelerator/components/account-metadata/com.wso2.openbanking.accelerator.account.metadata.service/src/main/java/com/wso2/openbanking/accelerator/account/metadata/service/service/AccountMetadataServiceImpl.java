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

package com.wso2.openbanking.accelerator.account.metadata.service.service;

import com.wso2.openbanking.accelerator.account.metadata.service.dao.AccountMetadataDAO;
import com.wso2.openbanking.accelerator.account.metadata.service.dao.AccountMetadataDAOImpl;
import com.wso2.openbanking.accelerator.account.metadata.service.dao.queries.AccountMetadataDBQueriesMySQLImpl;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.DatabaseUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.util.Map;

/**
 * Implementation of AccountMetadataService.
 */
public class AccountMetadataServiceImpl implements AccountMetadataService {

    private static final Log log = LogFactory.getLog(AccountMetadataServiceImpl.class);
    private static final String NOT_APPLICABLE = "N/A";
    private static AccountMetadataServiceImpl instance = null;
    AccountMetadataDAO accountMetadataDAO = new AccountMetadataDAOImpl(
            new AccountMetadataDBQueriesMySQLImpl());

    // private constructor
    private AccountMetadataServiceImpl() {
    }

    /**
     * @return AccountMetadataServiceImpl instance
     */
    public static synchronized AccountMetadataServiceImpl getInstance() {

        if (instance == null) {
            instance = new AccountMetadataServiceImpl();
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int addOrUpdateAccountMetadata(String accountId, String userId, Map<String, String> accountMetadataMap)
            throws OpenBankingException {

        if (StringUtils.isBlank(accountId) || StringUtils.isBlank(userId)) {
            log.error("Account Id or User Id is not provided.");
            throw new OpenBankingException("Account Id or User Id is not provided.");
        }
        if (accountMetadataMap == null || accountMetadataMap.isEmpty()) {
            log.error("Account metadata is not present.");
            throw new OpenBankingException("Account metadata is not present");
        }
        int noOfRecords = 0;
        // Add all entries in the accountMetadataMap to the database
        for (Map.Entry<String, String> accountMetadata : accountMetadataMap.entrySet()) {
            addOrUpdateAccountMetadata(accountId, userId, accountMetadata.getKey(), accountMetadata.getValue());
            noOfRecords++;
        }
        return noOfRecords;
    }

    @Override
    public int addOrUpdateAccountMetadata(String accountId, Map<String, String> accountMetadataMap)
            throws OpenBankingException {
        return addOrUpdateAccountMetadata(accountId, NOT_APPLICABLE, accountMetadataMap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int addOrUpdateAccountMetadata(String accountId, String userId, String metadataKey, String metadataValue)
            throws OpenBankingException {

        if (StringUtils.isBlank(accountId) || StringUtils.isBlank(userId)) {
            log.error("Account Id or User Id is not provided.");
            throw new OpenBankingException("Account Id or User Id is not provided.");
        }
        if (StringUtils.isBlank(metadataKey) || StringUtils.isBlank(metadataValue)) {
            log.error("Account metadata is not present.");
            throw new OpenBankingException("Account metadata is not present");
        }
        Connection dbConnection = DatabaseUtil.getDBConnection();
        // Check if the record is already present in the database.
        if (getAccountMetadataByKey(accountId, userId, metadataKey) == null) {
            // Add the record
            return accountMetadataDAO.storeAccountMetadata(dbConnection, accountId, userId, metadataKey, metadataValue);
        } else {
            // Update the record
            return accountMetadataDAO.updateAccountMetadata(dbConnection, accountId, userId, metadataKey,
                    metadataValue);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int addOrUpdateAccountMetadata(String accountId, String metadataKey, String metadataValue)
            throws OpenBankingException {
        return addOrUpdateAccountMetadata(accountId, NOT_APPLICABLE, metadataKey, metadataValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getAccountMetadataMap(String accountId, String userId)
            throws OpenBankingException {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(accountId)) {
            log.error("Account Id or User Id is not provided.");
            throw new OpenBankingException("Account Id or User Id is not provided.");
        }
        Connection dbConnection = DatabaseUtil.getDBConnection();
        return accountMetadataDAO.getAccountMetadataMap(dbConnection, accountId, userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getAccountMetadataMap(String accountId) throws OpenBankingException {
        return getAccountMetadataMap(accountId, NOT_APPLICABLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getUserMetadataForAccountIdAndKey(String accountId, String key)
            throws OpenBankingException {
        if (StringUtils.isBlank(accountId) || StringUtils.isBlank(key)) {
            log.error("Account Id or Key is not provided.");
            throw new OpenBankingException("Account Id or Key is not provided.");
        }
        Connection dbConnection = DatabaseUtil.getDBConnection();
        return accountMetadataDAO.getMetadataForAccountIdAndKey(dbConnection, accountId, key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAccountMetadataByKey(String accountId, String userId, String key)
            throws OpenBankingException {
        if (StringUtils.isBlank(accountId) || StringUtils.isBlank(userId) || StringUtils.isBlank(key)) {
            log.error("Account Id, User Id or Key is not provided.");
            throw new OpenBankingException("Account Id, User Id or Key is not provided.");
        }
        Connection dbConnection = DatabaseUtil.getDBConnection();
        return accountMetadataDAO.getAccountMetadataByKey(dbConnection, accountId, userId, key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAccountMetadataByKey(String accountId, String key) throws OpenBankingException {
        return getAccountMetadataByKey(accountId, NOT_APPLICABLE, key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAccountMetadata(String accountId, String userId) throws OpenBankingException {
        if (StringUtils.isBlank(accountId) || StringUtils.isBlank(userId)) {
            log.error("Account Id or User Id is not provided.");
            throw new OpenBankingException("Account Id or User Id is not provided.");
        }
        Connection dbConnection = DatabaseUtil.getDBConnection();
        return accountMetadataDAO.deleteAccountMetadata(dbConnection, accountId, userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAccountMetadata(String accountId) throws OpenBankingException {
        return removeAccountMetadata(accountId, NOT_APPLICABLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAccountMetadataByKey(String accountId, String userId, String key) throws
            OpenBankingException {
        if (StringUtils.isBlank(accountId) || StringUtils.isBlank(userId) || StringUtils.isBlank(key)) {
            log.error("Account Id, User Id or Key is not provided.");
            throw new OpenBankingException("Account Id, User Id or Key is not provided.");
        }
        Connection dbConnection = DatabaseUtil.getDBConnection();
        return accountMetadataDAO.deleteAccountMetadataByKey(dbConnection, accountId, userId, key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAccountMetadataByKeyForAllUsers(String accountId, String key) throws
            OpenBankingException {
        if (StringUtils.isBlank(accountId) || StringUtils.isBlank(key)) {
            log.error("Account Id or Key is not provided.");
            throw new OpenBankingException("Account Id or Key is not provided.");
        }
        Connection dbConnection = DatabaseUtil.getDBConnection();
        return accountMetadataDAO.deleteAccountMetadataByKeyForAllUsers(dbConnection, accountId, key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int removeAccountMetadataByKey(String accountId, String key) throws OpenBankingException {
        return removeAccountMetadataByKey(accountId, NOT_APPLICABLE, key);
    }

}
