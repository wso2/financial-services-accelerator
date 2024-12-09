/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.event.notifications.service.persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.common.persistence.JDBCPersistenceManager;
import org.wso2.financial.services.accelerator.event.notifications.service.dao.EventNotificationDAO;
import org.wso2.financial.services.accelerator.event.notifications.service.dao.EventNotificationDAOImpl;
import org.wso2.financial.services.accelerator.event.notifications.service.dao.EventSubscriptionDAO;
import org.wso2.financial.services.accelerator.event.notifications.service.dao.EventSubscriptionDAOImpl;
import org.wso2.financial.services.accelerator.event.notifications.service.dao.PostgreSqlEventNotificationDAOImpl;
import org.wso2.financial.services.accelerator.event.notifications.service.dao.PostgreSqlEventSubscriptionDAOImpl;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.queries.EventNotificationSqlStatements;
import org.wso2.financial.services.accelerator.event.notifications.service.queries.EventSubscriptionSqlStatements;
import org.wso2.financial.services.accelerator.event.notifications.service.queries.MSSQLEventNotificationSqlStatements;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Initializer Class for Event Notification Service DB.
 */
public class EventNotificationStoreInitializer {

    private static Log log = LogFactory.getLog(EventNotificationStoreInitializer.class);
    private static final String MYSQL = "MySQL";
    private static final String POSTGRE = "PostgreSQL";
    private static final String MSSQL = "Microsoft";
    private static final String ORACLE = "Oracle";
    private static final String H2 = "h2";

    public static EventNotificationDAO initializeEventNotificationDAO() throws FSEventNotificationException {

        EventNotificationDAO eventNotificationDAO;
        try (Connection connection = JDBCPersistenceManager.getInstance().getDBConnection()) {
            String driverName = connection.getMetaData().getDriverName();

            if (driverName.contains(MYSQL) || driverName.contains(H2)) {
                eventNotificationDAO = new EventNotificationDAOImpl(new EventNotificationSqlStatements());
            } else if (driverName.contains(POSTGRE)) {
                eventNotificationDAO = new PostgreSqlEventNotificationDAOImpl(new EventNotificationSqlStatements());
            } else if (driverName.contains(MSSQL)) {
                eventNotificationDAO = new EventNotificationDAOImpl(new MSSQLEventNotificationSqlStatements());
            } else if (driverName.contains(ORACLE)) {
                eventNotificationDAO = new EventNotificationDAOImpl(new EventNotificationSqlStatements());
            } else {
                throw new FSEventNotificationException("Unhandled DB driver: " + driverName + " detected");
            }

        } catch (SQLException e) {
            throw new FSEventNotificationException("Error while getting the database connection : ", e);
        }
        return eventNotificationDAO;
    }

    public static EventNotificationDAO getEventNotificationDAO() throws FSEventNotificationException {

        return initializeEventNotificationDAO();
    }

    public static EventSubscriptionDAO initializeSubscriptionDAO() throws FSEventNotificationException {

        EventSubscriptionDAO eventSubscriptionDao;
        try (Connection connection = JDBCPersistenceManager.getInstance().getDBConnection()) {
            String driverName = connection.getMetaData().getDriverName();

            if (driverName.contains(MYSQL) || driverName.contains(H2)) {
                eventSubscriptionDao = new EventSubscriptionDAOImpl(new EventSubscriptionSqlStatements());
            } else if (driverName.contains(POSTGRE)) {
                eventSubscriptionDao = new PostgreSqlEventSubscriptionDAOImpl(new EventSubscriptionSqlStatements());
            } else if (driverName.contains(MSSQL)) {
                eventSubscriptionDao = new EventSubscriptionDAOImpl(new EventSubscriptionSqlStatements());
            } else if (driverName.contains(ORACLE)) {
                eventSubscriptionDao = new EventSubscriptionDAOImpl(new EventSubscriptionSqlStatements());
            } else {
                throw new FSEventNotificationException("Unhandled DB driver: " + driverName + " detected");
            }
        } catch (SQLException e) {
            throw new FSEventNotificationException("Error while getting the database connection : ", e);
        }

        return eventSubscriptionDao;
    }

    public static EventSubscriptionDAO getEventSubscriptionDAO() throws FSEventNotificationException {

        return initializeSubscriptionDAO();
    }
}
