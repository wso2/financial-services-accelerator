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

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;

import java.util.Map;

/**
 * Account Metadata Service.
 * <p>
 * Handles the calls for persisting and retrieving metadata related to accounts.
 */
public interface AccountMetadataService {


    /**
     * Add or update multiple account metadata.
     * If the key already exists for the account-id and user-id combination, the value be updated.
     *
     * @param accountId          - Account ID
     * @param userId             - User ID
     * @param accountMetadataMap - Map containing metadata key and value pairs
     * @return number of records inserted/updated
     * @throws OpenBankingException - OpenBankingException
     */
    int addOrUpdateAccountMetadata(String accountId, String userId, Map<String, String> accountMetadataMap) throws
            OpenBankingException;

    /**
     * Add or update multiple account metadata for account-id where the user id is N/A.
     * If the key already exists for the account-id, the value be updated.
     *
     * @param accountId          - Account ID
     * @param accountMetadataMap - Map containing metadata key and value pairs
     * @return number of records inserted/updated
     * @throws OpenBankingException - OpenBankingException
     */
    int addOrUpdateAccountMetadata(String accountId, Map<String, String> accountMetadataMap) throws
            OpenBankingException;

    /**
     * Add or update account metadata.
     * If the key already exists for the account-id and user-id combination, the value be updated.
     *
     * @param accountId     - Account ID
     * @param userId        - User ID
     * @param metadataKey   - Metadata Key
     * @param metadataValue - Metadata Value
     * @return number of records inserted/updated
     * @throws OpenBankingException - OpenBankingException
     */
    int addOrUpdateAccountMetadata(String accountId, String userId, String metadataKey, String metadataValue) throws
            OpenBankingException;

    /**
     * Add or update metadata for account-id where the user id is N/A.
     * If the key already exists for the account-id, the value be updated.
     *
     * @param accountId     - Account ID
     * @param metadataKey   - Metadata Key
     * @param metadataValue - Metadata Value
     * @return number of records updated
     * @throws OpenBankingException - OpenBankingException
     */
    int addOrUpdateAccountMetadata(String accountId, String metadataKey, String metadataValue) throws
            OpenBankingException;

    /**
     * Get all metadata for an account-id user-id combination.
     *
     * @param accountId - Account ID
     * @param userId    - User ID
     * @return Map of account metadata
     * @throws OpenBankingException - OpenBankingException
     */
    Map<String, String> getAccountMetadataMap(String accountId, String userId) throws
            OpenBankingException;

    /**
     * Get all metadata affecting the account-id regardless of the user-id.
     *
     * @param accountId - Account ID
     * @return Map of account metadata
     * @throws OpenBankingException - OpenBankingException
     */
    Map<String, String> getAccountMetadataMap(String accountId) throws OpenBankingException;

    /**
     * Get users and metadata values for an account-id and key combination.
     *
     * @param accountId - Account ID
     * @param key       - Metadata key
     * @return Map of users and metadata values
     * @throws OpenBankingException - OpenBankingException
     */
    Map<String, String> getUserMetadataForAccountIdAndKey(String accountId, String key) throws
            OpenBankingException;

    /**
     * Get metadata value for an account-id user-id and key combination.
     *
     * @param accountId - Account ID
     * @param userId    - User ID
     * @param key       - Metadata key
     * @return Metadata value
     * @throws OpenBankingException - OpenBankingException
     */
    String getAccountMetadataByKey(String accountId, String userId, String key) throws
            OpenBankingException;

    /**
     * Given the key, get metadata value of the account-id where the user-id is N/A.
     *
     * @param accountId - Account ID
     * @param key       - Metadata key
     * @return Metadata value
     * @throws OpenBankingException - OpenBankingException
     */
    String getAccountMetadataByKey(String accountId, String key) throws OpenBankingException;

    /**
     * Remove all metadata for an account-id user-id combination.
     *
     * @param accountId - Account ID
     * @param userId    - User ID
     * @return number of affected rows
     * @throws OpenBankingException - OpenBankingException
     */
    int removeAccountMetadata(String accountId, String userId) throws OpenBankingException;

    /**
     * Remove all metadata for an account-id where the user-id is N/A.
     *
     * @param accountId - Account ID
     * @return number of affected rows
     * @throws OpenBankingException - OpenBankingException
     */
    int removeAccountMetadata(String accountId) throws OpenBankingException;

    /**
     * Remove metadata for an account-id user-id and key combination.
     *
     * @param accountId - Account ID
     * @param userId    - User ID
     * @param key       - Metadata key
     * @return number of affected rows
     * @throws OpenBankingException - OpenBankingException
     */
    int removeAccountMetadataByKey(String accountId, String userId, String key) throws
            OpenBankingException;

    /**
     * Remove metadata for an account-id and key combination for all user-ids.
     *
     * @param accountId - Account ID
     * @param key       - Metadata key
     * @return number of affected rows
     * @throws OpenBankingException - OpenBankingException
     */
    int removeAccountMetadataByKeyForAllUsers(String accountId, String key) throws
            OpenBankingException;

    /**
     * Given the key, remove metadata affecting the account-id where the user-id is N/A.
     *
     * @param accountId - Account ID
     * @param key       - Metadata key
     * @return number of affected rows
     * @throws OpenBankingException - OpenBankingException
     */
    int removeAccountMetadataByKey(String accountId, String key) throws OpenBankingException;

}
