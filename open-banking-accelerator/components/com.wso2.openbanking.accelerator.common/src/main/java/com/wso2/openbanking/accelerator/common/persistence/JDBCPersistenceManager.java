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

package com.wso2.openbanking.accelerator.common.persistence;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementRuntimeException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * This class is used for handling Open banking consent data persistence in the JDBC Store. During the server
 * start-up, it checks whether the database is created, if not it creates one. It reads the data source properties
 * from the open-banking.xml. This is implemented as a singleton. An instance of this class can be obtained through
 * JDBCPersistenceManager.getInstance() method.
 */
public class JDBCPersistenceManager {

    private static volatile JDBCPersistenceManager instance;
    private static volatile DataSource dataSource;
    private static Log log = LogFactory.getLog(JDBCPersistenceManager.class);

    private JDBCPersistenceManager() {

        initDataSource();
    }

    /**
     * Get an instance of the JDBCPersistenceManager. It implements a double checked locking initialization.
     *
     * @return JDBCPersistenceManager instance
     */
    public static synchronized JDBCPersistenceManager getInstance() {
        if (instance == null) {
            synchronized (JDBCPersistenceManager.class) {
                if (instance == null) {
                    instance = new JDBCPersistenceManager();
                }
            }
        }
        return instance;
    }

    /**
     * Initialize the data source.
     */
    @SuppressFBWarnings("LDAP_INJECTION")
    // Suppressed content - context.lookup(dataSourceName)
    // Suppression reason - False Positive : Since the dataSourceName is taken from the deployment.toml, it can be
    //                      trusted
    // Suppressed warning count - 1
    private void initDataSource() {

        if (dataSource != null) {
            return;
        }
        synchronized (JDBCPersistenceManager.class) {
            try {
                String dataSourceName = OpenBankingConfigParser.getInstance().getDataSourceName();
                if (StringUtils.isNotBlank(dataSourceName)) {
                    Context context = new InitialContext();
                    dataSource = (DataSource) context.lookup(dataSourceName);
                } else {
                    throw new ConsentManagementRuntimeException("Persistence Manager configuration for Open Banking " +
                            "is not available in open-banking.xml file. Terminating the JDBC persistence manager " +
                            "initialization.");
                }
            } catch (NamingException e) {
                throw new ConsentManagementRuntimeException("Error when looking up the Consent Management Data Source.",
                        e);
            }
        }
    }

    /**
     * Returns an database connection for Consent Management data source.
     *
     * @return Database connection.
     * @throws ConsentManagementRuntimeException Exception occurred when getting the data source.
     */
    public Connection getDBConnection() throws ConsentManagementRuntimeException {

        try {
            Connection dbConnection = dataSource.getConnection();
            dbConnection.setAutoCommit(false);
            log.debug("Returning database connection for Consent Management data source");
            return dbConnection;
        } catch (SQLException e) {
            throw new ConsentManagementRuntimeException("Error when getting a database connection object from the " +
                    "consent management data source.", e);
        }
    }

    /**
     * Returns Consent Management data source.
     *
     * @return Data source.
     */
    public DataSource getDataSource() {

        return dataSource;
    }

    /**
     * Revoke the transaction when catch then sql transaction errors.
     *
     * @param dbConnection database connection.
     */
    public void rollbackTransaction(Connection dbConnection) {

        try {
            if (dbConnection != null) {
                dbConnection.rollback();
            }
        } catch (SQLException e) {
            log.error("An error occurred while rolling back transactions. ", e);
        }
    }

    /**
     * Commit the transaction.
     *
     * @param dbConnection database connection.
     */
    public void commitTransaction(Connection dbConnection) {

        try {
            if (dbConnection != null) {
                dbConnection.commit();
            }
        } catch (SQLException e) {
            log.error("An error occurred while commit transactions. ", e);
        }
    }
}
