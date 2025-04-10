/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package  org.wso2.financial.services.accelerator.consent.mgt.dao.persistence;

import org.wso2.financial.services.accelerator.consent.mgt.dao.ConsentCoreDAO;
import org.wso2.financial.services.accelerator.consent.mgt.dao.exceptions.ConsentMgtException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.impl.ConsentCoreDAOImpl;
import org.wso2.financial.services.accelerator.consent.mgt.dao.impl.MssqlConsentCoreDAOImpl;
import org.wso2.financial.services.accelerator.consent.mgt.dao.impl.OracleConsentCoreDAOImpl;
import org.wso2.financial.services.accelerator.consent.mgt.dao.queries.ConsentMgtCommonDBQueries;
import org.wso2.financial.services.accelerator.consent.mgt.dao.queries.ConsentMgtMssqlDBQueries;
import org.wso2.financial.services.accelerator.consent.mgt.dao.queries.ConsentMgtOracleDBQueries;
import org.wso2.financial.services.accelerator.consent.mgt.dao.queries.ConsentMgtPostgresDBQueries;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class handles consent DAO layer initiation with the relevant SQL statements per database types.
 */
public class ConsentStoreInitializer {

    private static final String MYSQL = "MySQL";
    private static final String H2 = "H2";
    private static final String MICROSOFT = "Microsoft";
    private static final String MS_SQL = "MSSQL";
    private static final String POSTGRE = "PostgreSQL";
    private static final String ORACLE = "Oracle";
    private static ConsentCoreDAO consentCoreDAO = null;

    /**
     * Return the DAO implementation initialized for the relevant database type.
     *
     * @return the dao implementation
     * @throws ConsentMgtException thrown if an error occurs when getting the database connection
     */
    public static synchronized ConsentCoreDAO getInitializedConsentCoreDAOImpl() throws
            ConsentMgtException {

        if (consentCoreDAO == null) {
            consentCoreDAO = getDaoInstance();
        }
        return consentCoreDAO;
    }

    private static ConsentCoreDAO getDaoInstance()
            throws
            ConsentMgtException {

        try {
            Connection connection = JDBCPersistenceManager.getInstance().getDBConnection();
            String driverName = connection.getMetaData().getDriverName();

            ConsentCoreDAO dao;
            if (driverName.contains(MYSQL)) {
                dao = new ConsentCoreDAOImpl(new ConsentMgtCommonDBQueries());
            } else if (driverName.contains(H2)) {
                dao = new ConsentCoreDAOImpl(new ConsentMgtCommonDBQueries());
            } else if (driverName.contains(MS_SQL) || driverName.contains(MICROSOFT)) {
                dao = new MssqlConsentCoreDAOImpl(new ConsentMgtMssqlDBQueries());
            } else if (driverName.contains(POSTGRE)) {
                dao = new ConsentCoreDAOImpl(new ConsentMgtPostgresDBQueries());
            } else if (driverName.contains(ORACLE)) {
                dao = new OracleConsentCoreDAOImpl(new ConsentMgtOracleDBQueries());
            } else {
                throw new ConsentMgtException("Unhandled DB driver: " + driverName + " detected : ");
            }
            return dao;
        } catch (SQLException e) {
            throw new ConsentMgtException("Error while getting the database connection : ", e);
        }
    }
}
