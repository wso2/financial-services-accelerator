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

package com.wso2.openbanking.accelerator.common.util;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingRuntimeException;
import com.wso2.openbanking.accelerator.common.persistence.JDBCPersistenceManager;
import com.wso2.openbanking.accelerator.common.persistence.JDBCRetentionDataPersistenceManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Utility class for database operations.
 */
public class DatabaseUtil {

    private static final Log log = LogFactory.getLog(DatabaseUtil.class);

    /**
     * Get a database connection instance from the Consent Management Persistence Manager.
     *
     * @return Database Connection
     * @throws OpenBankingRuntimeException Error when getting a database connection to Consent Management database
     */
    public static Connection getDBConnection() throws OpenBankingRuntimeException {

        return JDBCPersistenceManager.getInstance().getDBConnection();
    }

    /**
     * Get a database connection instance from the Retention Data Persistence Manager.
     *
     * @return Database Connection
     * @throws OpenBankingRuntimeException Error when getting a database connection to retention database
     */
    public static Connection getRetentionDBConnection() throws OpenBankingRuntimeException {

        return JDBCRetentionDataPersistenceManager.getInstance().getDBConnection();
    }

    public static void closeConnection(Connection dbConnection) {

        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                log.error("Database error. Could not close statement. Continuing with others. - "
                        + e.getMessage().replaceAll("[\r\n]", ""), e);
            }
        }
    }

    public static void rollbackTransaction(Connection dbConnection) {

        JDBCPersistenceManager.getInstance().rollbackTransaction(dbConnection);
    }

    public static void commitTransaction(Connection dbConnection) {

        JDBCPersistenceManager.getInstance().commitTransaction(dbConnection);
    }
}
