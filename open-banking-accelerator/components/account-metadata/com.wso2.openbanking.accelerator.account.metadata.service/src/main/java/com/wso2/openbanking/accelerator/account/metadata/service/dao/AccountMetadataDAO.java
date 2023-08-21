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

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;

import java.sql.Connection;
import java.util.Map;

/**
 * AccountMetadataDAO
 * <p>
 * Contains the methods to store, retrieve and delete account
 * metadata in the database.
 */
public interface AccountMetadataDAO {

    /**
     * Store account metadata.
     *
     * @param accountId     - Account ID
     * @param userId        - User ID
     * @param metadataKey   - Metadata key
     * @param metadataValue - Metadata value
     * @return number of rows affected
     * @throws OpenBankingException - OpenBankingException
     */
    int storeAccountMetadata(Connection dbConnection, String accountId, String userId, String metadataKey,
                             String metadataValue) throws OpenBankingException;

    /**
     * Store or update account metadata.
     * If the key already exists for the account-id and user-id combination, the value be updated.
     *
     * @param accountId          - Account ID
     * @param userId             - User ID
     * @param metadataKey   - Metadata key
     * @param metadataValue - Metadata value
     * @return number of rows affected
     * @throws OpenBankingException - OpenBankingException
     */
    int updateAccountMetadata(Connection dbConnection, String accountId, String userId, String metadataKey,
                              String metadataValue) throws OpenBankingException;

    /**
     * Retrieve account metadata for a given user-id and account-id combination.
     *
     * @param accountId - Account ID
     * @param userId    - User ID
     * @return Map of account metadata
     * @throws OpenBankingException - OpenBankingException
     */
    Map<String, String> getAccountMetadataMap(Connection dbConnection, String accountId, String userId)
            throws OpenBankingException;

    /**
     * Retrieve account metadata for a given account-id and key combination.
     *
     * @param accountId - Account ID
     * @param key       - Attribute key
     * @return Map of user-id and attribute value
     * @throws OpenBankingException - OpenBankingException
     */
    Map<String, String> getMetadataForAccountIdAndKey(Connection dbConnection, String accountId, String key)
            throws OpenBankingException;

    /**
     * Retrieve the value for the given account-id, user-id and key combination.
     *
     * @param accountId - Account ID
     * @param userId    - User ID
     * @param key       - Key
     * @return Attribute value
     * @throws OpenBankingException - OpenBankingException
     */
    String getAccountMetadataByKey(Connection dbConnection, String accountId, String userId, String key)
            throws OpenBankingException;

    /**
     * Delete all account metadata for a given user-id and account-id combination.
     *
     * @param accountId - Account ID
     * @param userId    - User ID
     * @return number of rows affected
     * @throws OpenBankingException - OpenBankingException
     */
    int deleteAccountMetadata(Connection dbConnection, String accountId, String userId) throws OpenBankingException;

    /**
     * Delete account metadata for a given user-id, account-id and key combination.
     *
     * @param accountId - Account ID
     * @param userId    - User ID
     * @param key       - Key
     * @return number of rows affected
     * @throws OpenBankingException - OpenBankingException
     */
    int deleteAccountMetadataByKey(Connection dbConnection, String accountId, String userId, String key) throws
            OpenBankingException;

    /**
     * Delete all account metadata for a given account-id and key combination.
     *
     * @param accountId - Account ID
     * @param key       - Key
     * @return number of rows affected
     * @throws OpenBankingException - OpenBankingException
     */
    int deleteAccountMetadataByKeyForAllUsers(Connection dbConnection, String accountId, String key) throws
            OpenBankingException;

}
