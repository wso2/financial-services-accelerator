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
 * AccountMetadataDBQueriesMySQLImpl
 * Contains the MySQL queries used by the AccountMetadataDAOImpl.
 */
public class AccountMetadataDBQueriesMySQLImpl implements AccountMetadataDBQueries {

    /**
     * {@inheritDoc}
     */
    public String getStoreAccountMetadataPreparedStatement() {

        return "INSERT INTO OB_ACCOUNT_METADATA (ACCOUNT_ID, USER_ID, METADATA_KEY, METADATA_VALUE, " +
                "LAST_UPDATED_TIMESTAMP) VALUES (?, ?, ?, ?, ?)";
    }

    /**
     * {@inheritDoc}
     */
    public String getUpdateAccountMetadataPreparedStatement() {

        return "UPDATE OB_ACCOUNT_METADATA SET METADATA_VALUE = ?, LAST_UPDATED_TIMESTAMP = ? WHERE ACCOUNT_ID = ? " +
                "AND USER_ID = ? AND METADATA_KEY = ?";
    }

    /**
     * {@inheritDoc}
     */
    public String getRetrieveAccountMetadataPreparedStatement() {

        return "SELECT METADATA_KEY, METADATA_VALUE FROM OB_ACCOUNT_METADATA WHERE ACCOUNT_ID = ? AND USER_ID = ?";

    }

    /**
     * {@inheritDoc}
     */
    public String getRetrieveMetadataByAccountIdAndKeyPreparedStatement() {

        return "SELECT USER_ID, METADATA_VALUE FROM OB_ACCOUNT_METADATA WHERE ACCOUNT_ID = ? AND METADATA_KEY = ?";

    }

    /**
     * {@inheritDoc}
     */
    public String getRetrieveAccountMetadataByKeyPreparedStatement() {

        return "SELECT METADATA_VALUE FROM OB_ACCOUNT_METADATA WHERE ACCOUNT_ID = ? AND USER_ID = ? AND " +
                "METADATA_KEY = ?";

    }

    /**
     * {@inheritDoc}
     */
    public String getDeleteAccountMetadataPreparedStatement() {

        return "DELETE FROM OB_ACCOUNT_METADATA WHERE ACCOUNT_ID = ? AND USER_ID = ?";

    }

    /**
     * {@inheritDoc}
     */
    public String getDeleteAccountMetadataByKeyPreparedStatement() {

        return "DELETE FROM OB_ACCOUNT_METADATA WHERE ACCOUNT_ID = ? AND USER_ID  = ? AND METADATA_KEY = ?";

    }

    /**
     * {@inheritDoc}
     */
    public String getDeleteAccountMetadataByKeyForAllUsersPreparedStatement() {

        return "DELETE FROM OB_ACCOUNT_METADATA WHERE ACCOUNT_ID = ? AND METADATA_KEY = ?";

    }
}
