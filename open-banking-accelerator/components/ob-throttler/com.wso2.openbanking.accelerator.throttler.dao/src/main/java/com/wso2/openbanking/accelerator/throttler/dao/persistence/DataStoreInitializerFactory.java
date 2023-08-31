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
package com.wso2.openbanking.accelerator.throttler.dao.persistence;

import com.wso2.openbanking.accelerator.common.exception.OBThrottlerException;
import com.wso2.openbanking.accelerator.common.persistence.JDBCPersistenceManager;
import com.wso2.openbanking.accelerator.throttler.dao.OBThrottlerDAO;
import com.wso2.openbanking.accelerator.throttler.dao.impl.OBThrottlerDAOImpl;
import com.wso2.openbanking.accelerator.throttler.dao.queries.OBThrottlerSQLStatements;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Data Store Initializer Factory class.
 */
public class DataStoreInitializerFactory {

    private static final Log log = LogFactory.getLog(DataStoreInitializerFactory.class);

    public OBThrottlerDAO initializeDataStore() throws OBThrottlerException {

        try (Connection connection = JDBCPersistenceManager.getInstance().getDBConnection()) {
            String driverName = connection.getMetaData().getDriverName();

            if (log.isDebugEnabled()) {
                log.debug("Initiated OBThrottlerDAO with " + driverName.replaceAll("[\r\n]", ""));
            }
            // returning default queries for all database types
            return new OBThrottlerDAOImpl(new OBThrottlerSQLStatements());

        } catch (SQLException e) {
            log.error(String.format("Error while getting the database connection. %s", e).replaceAll("[\r\n]", ""));
            throw new OBThrottlerException("Error while getting the database connection : ", e);
        }
    }
}
