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
 * This class is used for handling open banking retention data (if enabled) persistence in the JDBC Store.
 * During the server start-up, it checks whether the database is created, It reads the data source properties
 * from the open-banking.xml. This is implemented as a singleton. An instance of this class can be obtained through
 * JDBCRetentionDataPersistenceManager.getInstance() method.
 */
public class JDBCRetentionDataPersistenceManager {

    private static volatile JDBCRetentionDataPersistenceManager instance;
    private static volatile DataSource dataSource;
    private static Log log = LogFactory.getLog(JDBCRetentionDataPersistenceManager.class);

    private JDBCRetentionDataPersistenceManager() {

        initDataSource();
    }

    /**
     * Get an instance of the JDBCRetentionDataPersistenceManager. It implements a double checked locking initialization
     *
     * @return JDBCRetentionDataPersistenceManager instance
     */
    public static synchronized JDBCRetentionDataPersistenceManager getInstance() {

        if (instance == null) {
            synchronized (JDBCRetentionDataPersistenceManager.class) {
                if (instance == null) {
                    instance = new JDBCRetentionDataPersistenceManager();
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
        synchronized (JDBCRetentionDataPersistenceManager.class) {
            try {
                String dataSourceName = OpenBankingConfigParser.getInstance().getRetentionDataSourceName();
                if (StringUtils.isNotBlank(dataSourceName)) {
                    Context context = new InitialContext();
                    dataSource = (DataSource) context.lookup(dataSourceName);
                } else {
                    throw new ConsentManagementRuntimeException("Persistence Manager configuration for " +
                            "retention datasource is not available in open-banking.xml file. Terminating the " +
                            "JDBC retention data persistence manager initialization.");
                }
            } catch (NamingException e) {
                throw new ConsentManagementRuntimeException("Error when looking up the Consent Retention " +
                        "Data Source.", e);
            }
        }
    }

    /**
     * Returns an database connection for retention data source.
     *
     * @return Database connection.
     * @throws ConsentManagementRuntimeException Exception occurred when getting the data source.
     */
    public Connection getDBConnection() throws ConsentManagementRuntimeException {

        try {
            Connection dbConnection = dataSource.getConnection();
            dbConnection.setAutoCommit(false);
            log.debug("Returning database connection for retention data source");
            return dbConnection;
        } catch (SQLException e) {
            throw new ConsentManagementRuntimeException("Error when getting a database connection object from the " +
                    "retention data source.", e);
        }
    }

    /**
     * Returns retention data source.
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
