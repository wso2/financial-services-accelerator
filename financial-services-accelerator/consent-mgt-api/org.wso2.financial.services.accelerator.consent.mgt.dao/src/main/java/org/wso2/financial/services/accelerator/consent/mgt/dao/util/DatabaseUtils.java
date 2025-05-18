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

package org.wso2.financial.services.accelerator.consent.mgt.dao.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentMgtException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.persistence.JDBCPersistenceManager;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Utility class for database operations.
 */
@SuppressFBWarnings("CRLF_INJECTION_LOGS")
public class DatabaseUtils {

    private static final Log log = LogFactory.getLog(DatabaseUtils.class);

    /**
     * Get a database connection instance from the Consent Management Persistence Manager.
     * @return Database Connection
     * @throws ConsentMgtException Error when getting a database connection to Consent Management database
     */
    public static Connection getDBConnection() throws
            ConsentMgtException,
            SQLException {

        return JDBCPersistenceManager.getInstance().getDBConnection();
    }

    public static void closeConnection(Connection dbConnection) throws
            SQLException {

        if (dbConnection != null && !dbConnection.isClosed()) {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                log.error("Database error. Could not close statement. Continuing with others. - "

                        + e.getMessage().replaceAll("[\r\n]", ""), e);
            }
        }
    }

    public static void rollbackTransaction(Connection dbConnection) throws
            ConsentMgtException {

        JDBCPersistenceManager.getInstance().rollbackTransaction(dbConnection);
    }

    public static void commitTransaction(Connection dbConnection) throws
            ConsentMgtException {

        JDBCPersistenceManager.getInstance().commitTransaction(dbConnection);
    }
}
