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

package com.wso2.openbanking.accelerator.account.metadata.service.dao.queries;

/**
 * AccountMetadataDBQueries
 * Contains the queries used by the AccountMetadataDAOImpl.
 */
public interface AccountMetadataDBQueries {

    /**
     * Returns the query to store account metadata.
     *
     * @return String
     */
    String getStoreAccountMetadataPreparedStatement();

    /**
     * Returns the query to update account metadata.
     *
     * @return String
     */
    String getUpdateAccountMetadataPreparedStatement();

    /**
     * Returns the query to retrieve account metadata.
     *
     * @return String
     */
    String getRetrieveAccountMetadataPreparedStatement();

    /**
     * Returns the query to retrieve user-ids and metadata values when
     * account-id and metadata-key is given.
     *
     * @return String
     */
    String getRetrieveMetadataByAccountIdAndKeyPreparedStatement();

    /**
     * Returns the query to retrieve the account metadata by key.
     *
     * @return String
     */
    String getRetrieveAccountMetadataByKeyPreparedStatement();

    /**
     * Returns the query to delete the account metadata.
     *
     * @return String
     */
    String getDeleteAccountMetadataPreparedStatement();

    /**
     * Returns the query to delete the account metadata by key.
     *
     * @return String
     */
    String getDeleteAccountMetadataByKeyPreparedStatement();

    /**
     * Returns the query to delete the account metadata by key for al users.
     *
     * @return String
     */
    String getDeleteAccountMetadataByKeyForAllUsersPreparedStatement();
}
