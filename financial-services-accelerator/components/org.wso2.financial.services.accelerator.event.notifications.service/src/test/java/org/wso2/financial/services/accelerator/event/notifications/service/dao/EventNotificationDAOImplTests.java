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

package org.wso2.financial.services.accelerator.event.notifications.service.dao;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.util.DatabaseUtils;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationTestConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.model.Notification;
import org.wso2.financial.services.accelerator.event.notifications.service.model.NotificationEvent;
import org.wso2.financial.services.accelerator.event.notifications.service.queries.EventNotificationSqlStatements;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationTestUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;

/**
 * Test class for EventNotificationDAOImpl.
 */
public class EventNotificationDAOImplTests {

    private static final String DB_NAME = "CONSENT_DB";
    private static Connection mockedConnection;
    private static Connection mockedExceptionConnection;
    private PreparedStatement mockedPreparedStatement;
    private MockedStatic<DatabaseUtils> databaseUtilMockedStatic;

    EventNotificationDAOImpl eventNotificationDAOImpl = new EventNotificationDAOImpl(
            new EventNotificationSqlStatements());

    @BeforeClass
    public void initTest() throws Exception {

        EventNotificationTestUtils.initializeDataSource(DB_NAME,
                EventNotificationTestUtils.getFilePath("dbScripts/h2.sql"));
        mockedConnection = Mockito.mock(Connection.class);
        mockedExceptionConnection = Mockito.mock(Connection.class);
        mockedPreparedStatement = Mockito.mock(PreparedStatement.class);

        databaseUtilMockedStatic = Mockito.mockStatic(DatabaseUtils.class);
        databaseUtilMockedStatic.when(DatabaseUtils::getDBConnection).thenReturn(mockedConnection);
    }

    @AfterClass
    public void tearDown() {
        databaseUtilMockedStatic.close();
    }

    @Test
    public void testPersistEventNotification() throws SQLException, FSEventNotificationException {

        String notificationIdReceived;
        String notificationIdSent = UUID.randomUUID().toString();
        try (Connection connection = EventNotificationTestUtils.getConnection(DB_NAME)) {
            notificationIdReceived = eventNotificationDAOImpl.persistEventNotification(connection,
                    EventNotificationTestUtils.getSampleNotificationDTO(notificationIdSent),
                    EventNotificationTestUtils.getSampleEventList());
        }

        Assert.assertEquals(notificationIdReceived, notificationIdSent);
    }

    @Test(expectedExceptions = FSEventNotificationException.class)
    public void testPersistEventNotificationDBError() throws SQLException, FSEventNotificationException {

        Mockito.when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        Mockito.when(mockedPreparedStatement.executeUpdate()).thenThrow(new SQLException());

        String notificationIdSent = UUID.randomUUID().toString();
        String notificationIdReceived = eventNotificationDAOImpl.persistEventNotification(mockedConnection,
                    EventNotificationTestUtils.getSampleNotificationDTO(notificationIdSent),
                    EventNotificationTestUtils.getSampleEventList());

        Assert.assertNotNull(notificationIdReceived, notificationIdSent);
    }

    @Test
    public void testGetNotificationsByStatus() throws SQLException, FSEventNotificationException {

        List<Notification> eventsList;
        String notificationIdSent = UUID.randomUUID().toString();
        try (Connection connection = EventNotificationTestUtils.getConnection(DB_NAME)) {
            eventNotificationDAOImpl.persistEventNotification(connection,
                    EventNotificationTestUtils.getSampleNotificationDTO(notificationIdSent),
                    EventNotificationTestUtils.getSampleEventList());
            eventsList = eventNotificationDAOImpl.getNotificationsByStatus(connection, "OPEN");
        }
        Assert.assertNotNull(eventsList);
    }

    @Test
    public void testGetEventsByNotificationID() throws FSEventNotificationException, SQLException {

        List<NotificationEvent> eventsList;
        String notificationIdSent = UUID.randomUUID().toString();
        try (Connection connection = EventNotificationTestUtils.getConnection(DB_NAME)) {
            eventNotificationDAOImpl.persistEventNotification(connection,
                    EventNotificationTestUtils.getSampleNotificationDTO(notificationIdSent),
                    EventNotificationTestUtils.getSampleEventList());
            eventsList = eventNotificationDAOImpl.getEventsByNotificationID(connection,
                    notificationIdSent);
        }
        Assert.assertNotNull(eventsList);
    }

    @Test
    public void testGetNotificationsByClientIdAndStatus() throws SQLException, FSEventNotificationException {

        List<Notification> eventsList;

        String notificationIdSent = UUID.randomUUID().toString();
        try (Connection connection = EventNotificationTestUtils.getConnection(DB_NAME)) {
            eventNotificationDAOImpl.persistEventNotification(connection,
                    EventNotificationTestUtils.getSampleNotificationDTO(notificationIdSent),
                    EventNotificationTestUtils.getSampleEventList());
            eventsList = eventNotificationDAOImpl.getNotificationsByClientIdAndStatus(connection,
                    EventNotificationTestConstants.SAMPLE_CLIENT_ID, "OPEN", 5);
        }

        Assert.assertNotNull(eventsList);
    }

    @Test
    public void testGetEventsStatus() throws SQLException, FSEventNotificationException {

        boolean status;
        String notificationIdSent = UUID.randomUUID().toString();
        try (Connection connection = EventNotificationTestUtils.getConnection(DB_NAME)) {
            eventNotificationDAOImpl.persistEventNotification(connection,
                    EventNotificationTestUtils.getSampleNotificationDTO(notificationIdSent),
                    EventNotificationTestUtils.getSampleEventList());
            status = eventNotificationDAOImpl.getNotificationStatus(connection, notificationIdSent);
        }

        Assert.assertTrue(status);

    }

    @Test
    public void testStoreErrorNotifications() throws SQLException, FSEventNotificationException {

        String notificationIdSent = UUID.randomUUID().toString();
        try (Connection connection = EventNotificationTestUtils.getConnection(DB_NAME)) {
            eventNotificationDAOImpl.persistEventNotification(connection,
                    EventNotificationTestUtils.getSampleNotificationDTO(notificationIdSent),
                    EventNotificationTestUtils.getSampleEventList());
            eventNotificationDAOImpl.storeErrorNotification(connection,
                    EventNotificationTestUtils.getNotificationError(notificationIdSent));
        }
    }

    @Test
    public void testGetEventsStatusACK() throws SQLException, FSEventNotificationException {

        boolean status;
        String notificationIdSent = UUID.randomUUID().toString();
        try (Connection connection = EventNotificationTestUtils.getConnection(DB_NAME)) {
            eventNotificationDAOImpl.persistEventNotification(connection,
                    EventNotificationTestUtils.getSampleNotificationDTO(notificationIdSent),
                    EventNotificationTestUtils.getSampleEventList());
            eventNotificationDAOImpl.updateNotificationStatusById(connection, notificationIdSent, "ACK");
            status = eventNotificationDAOImpl.getNotificationStatus(connection, notificationIdSent);
        }

        Assert.assertFalse(status);
    }

    @Test(expectedExceptions = FSEventNotificationException.class)
    public void testGetEventsStatusDBError() throws SQLException, FSEventNotificationException {

        ResultSet mockedResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
        Mockito.when(mockedExceptionConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        eventNotificationDAOImpl.getNotificationStatus(mockedExceptionConnection,
                EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);
    }

    @Test
    public void testGetNotificationCount() throws SQLException, FSEventNotificationException {

        int eventCount;
        String notificationIdSent = UUID.randomUUID().toString();
        try (Connection connection = EventNotificationTestUtils.getConnection(DB_NAME)) {
            eventNotificationDAOImpl.persistEventNotification(connection,
                    EventNotificationTestUtils.getSampleNotificationDTO(notificationIdSent),
                    EventNotificationTestUtils.getSampleEventList());
            eventCount = eventNotificationDAOImpl.getNotificationCountByClientIdAndStatus(connection,
                    EventNotificationTestConstants.SAMPLE_CLIENT_ID, EventNotificationConstants.OPEN);
        }
        Assert.assertTrue(eventCount > 0);
    }

    @Test
    public void testGetNotificationCountNoEvents() throws SQLException, FSEventNotificationException {

        int eventCount;
        String notificationIdSent = UUID.randomUUID().toString();
        try (Connection connection = EventNotificationTestUtils.getConnection(DB_NAME)) {
            eventNotificationDAOImpl.persistEventNotification(connection,
                    EventNotificationTestUtils.getSampleNotificationDTO(notificationIdSent),
                    EventNotificationTestUtils.getSampleEventList());
            eventCount = eventNotificationDAOImpl.getNotificationCountByClientIdAndStatus(connection,
                    "jvchvhVVDCDVWYEQCUQsytcldqh", EventNotificationConstants.OPEN);
        }
        Assert.assertEquals(eventCount, 0);
    }

    @Test
    public void testUpdateNotificationStatusById() throws SQLException, FSEventNotificationException {

        String notificationIdSent = UUID.randomUUID().toString();
        try (Connection connection = EventNotificationTestUtils.getConnection(DB_NAME)) {
            eventNotificationDAOImpl.persistEventNotification(connection,
                    EventNotificationTestUtils.getSampleNotificationDTO(notificationIdSent),
                    EventNotificationTestUtils.getSampleEventList());
            eventNotificationDAOImpl.updateNotificationStatusById(connection, notificationIdSent, "ACK");
        }
    }

    @Test(expectedExceptions = FSEventNotificationException.class)
    public void testUpdateNotificationStatusByIdError() throws SQLException, FSEventNotificationException {

        try (Connection connection = EventNotificationTestUtils.getConnection(DB_NAME)) {
            eventNotificationDAOImpl.updateNotificationStatusById(connection,
                    EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID, "ACK");
        }
    }

}
