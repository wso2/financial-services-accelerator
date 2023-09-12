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

package com.wso2.openbanking.accelerator.event.notifications.service.persistence;

import com.wso2.openbanking.accelerator.common.persistence.JDBCPersistenceManager;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.event.notifications.service.dao.EventSubscriptionDAO;
import com.wso2.openbanking.accelerator.event.notifications.service.dao.EventSubscriptionDAOImpl;
import com.wso2.openbanking.accelerator.event.notifications.service.dao.EventSubscriptionSqlStatements;
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Initializer Class for EventSubscription Service DB.
 */
@Generated(message = "Datastore initializer classes")
public class EventSubscriptionStoreInitializer {

    private static Log log = LogFactory.getLog(EventSubscriptionStoreInitializer.class);
    private static final String MYSQL = "MySQL";
    private static final String POSTGRE = "PostgreSQL";
    private static final String MSSQL = "Microsoft";
    private static final String ORACLE = "Oracle";
    private static final String H2 = "h2";

    public static EventSubscriptionDAO initializeSubscriptionDAO() throws OBEventNotificationException {

        EventSubscriptionDAO eventSubscriptionDao;
        try (Connection connection = JDBCPersistenceManager.getInstance().getDBConnection()) {
            String driverName = connection.getMetaData().getDriverName();

            if (driverName.contains(MYSQL) || driverName.contains(H2)) {
                eventSubscriptionDao = new EventSubscriptionDAOImpl(new EventSubscriptionSqlStatements());
            } else if (driverName.contains(POSTGRE)) {
                eventSubscriptionDao = new EventSubscriptionDAOImpl(new EventSubscriptionSqlStatements());
            } else if (driverName.contains(MSSQL)) {
                eventSubscriptionDao = new EventSubscriptionDAOImpl(new EventSubscriptionSqlStatements());
            } else if (driverName.contains(ORACLE)) {
                eventSubscriptionDao = new EventSubscriptionDAOImpl(new EventSubscriptionSqlStatements());
            } else {
                throw new OBEventNotificationException("Unhandled DB driver: " + driverName + " detected");
            }
        } catch (SQLException e) {
            throw new OBEventNotificationException("Error while getting the database connection : ", e);
        }

        return eventSubscriptionDao;
    }

    public static EventSubscriptionDAO getEventSubscriptionDao() throws OBEventNotificationException {

        return initializeSubscriptionDAO();
    }
}
