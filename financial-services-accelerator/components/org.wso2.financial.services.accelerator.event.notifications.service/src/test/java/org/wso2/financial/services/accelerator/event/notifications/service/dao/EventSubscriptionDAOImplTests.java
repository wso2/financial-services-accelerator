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

package org.wso2.financial.services.accelerator.event.notifications.service.dao;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.util.DatabaseUtils;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationTestConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.model.EventSubscription;
import org.wso2.financial.services.accelerator.event.notifications.service.queries.EventSubscriptionSqlStatements;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationTestUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;


/**
 * Test class for EventSubscriptionDAOImpl.
 */
public class EventSubscriptionDAOImplTests {

    private static final String DB_NAME = "CONSENT_DB";
    private static Connection mockedConnection;
    private PreparedStatement mockedPreparedStatement;
    private MockedStatic<DatabaseUtils> databaseUtilMockedStatic;

    private final EventSubscriptionDAOImpl eventSubscriptionDAOImpl = new EventSubscriptionDAOImpl(
            new EventSubscriptionSqlStatements());
    private final EventSubscription sampleEventSubscription = EventNotificationTestUtils.getSampleEventSubscription();
    private final List<String> sampleEventTypes = EventNotificationTestUtils.getSampleStoredEventTypes();

    @BeforeClass
    public void initTest() throws Exception {
        EventNotificationTestUtils.initializeDataSource(DB_NAME,
                EventNotificationTestUtils.getFilePath("dbScripts/h2.sql"));

        mockedConnection = Mockito.mock(Connection.class);
        mockedPreparedStatement = Mockito.mock(PreparedStatement.class);

        databaseUtilMockedStatic = Mockito.mockStatic(DatabaseUtils.class);
        databaseUtilMockedStatic.when(DatabaseUtils::getDBConnection).thenReturn(mockedConnection);
    }

    @AfterClass
    public void tearDown() {
        databaseUtilMockedStatic.close();
    }

    @Test
    public void testStoreEventSubscription() throws SQLException, FSEventNotificationException {

        EventSubscription result;
        try (Connection connection = EventNotificationTestUtils.getConnection(DB_NAME)) {
            result = eventSubscriptionDAOImpl.storeEventSubscription(connection,
                    sampleEventSubscription);
        }

        Assert.assertNotNull(sampleEventSubscription.getSubscriptionId());
        Assert.assertNotNull(sampleEventSubscription.getTimeStamp());
        Assert.assertEquals("CREATED", result.getStatus());
    }

//    @Test(expectedExceptions = FSEventNotificationException.class)
//    public void testStoreEventSubscriptionDBError() throws SQLException, FSEventNotificationException {
//
//        Mockito.when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
//        Mockito.when(mockedPreparedStatement.executeUpdate()).thenThrow(new SQLException());
//
//        eventSubscriptionDAOImpl.storeEventSubscription(mockedConnection,
//                EventNotificationTestUtils.getSampleEventSubscription());
//    }

    @Test
    public void testStoreSubscribedEventTypes() throws FSEventNotificationException, SQLException {

        List<String> result;
        try (Connection connection = EventNotificationTestUtils.getConnection(DB_NAME)) {
            EventSubscription subscription = eventSubscriptionDAOImpl.storeEventSubscription(connection,
                    EventNotificationTestUtils.getSampleEventSubscription());
            result = eventSubscriptionDAOImpl.storeSubscribedEventTypes(connection,
                    subscription.getSubscriptionId(), sampleEventTypes);
        }

        Assert.assertEquals(sampleEventTypes, result);
    }

    @Test(expectedExceptions = FSEventNotificationException.class)
    public void testStoreSubscribedEventTypesFailure() throws SQLException, FSEventNotificationException {

        try (Connection connection = EventNotificationTestUtils.getConnection(DB_NAME)) {
            eventSubscriptionDAOImpl.storeSubscribedEventTypes(connection,
                    EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1, sampleEventTypes);
        }
    }

    @Test
    public void testStoreSubscribedEventTypesDBError() throws SQLException {
        Mockito.when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        Mockito.when(mockedPreparedStatement.executeBatch()).thenThrow(new SQLException());
        List<String> sampleEventTypes = EventNotificationTestUtils.getSampleStoredEventTypes();

        Assert.assertThrows(FSEventNotificationException.class, () -> eventSubscriptionDAOImpl.
                storeSubscribedEventTypes(mockedConnection, EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1,
                        sampleEventTypes));
    }

    @Test
    public void testGetEventSubscriptionBySubscriptionId() throws FSEventNotificationException, SQLException {

        EventSubscription result;
        try (Connection connection = EventNotificationTestUtils.getConnection(DB_NAME)) {
            EventSubscription subscription = eventSubscriptionDAOImpl.storeEventSubscription(connection,
                    EventNotificationTestUtils.getSampleEventSubscription());
            eventSubscriptionDAOImpl.storeSubscribedEventTypes(connection,
                    subscription.getSubscriptionId(), sampleEventTypes);
            result = eventSubscriptionDAOImpl.getEventSubscriptionBySubscriptionId(
                    connection, subscription.getSubscriptionId());
        }

        List<String> eventTypes = result.getEventTypes();
        Assert.assertTrue(eventTypes.contains(EventNotificationTestConstants.
                SAMPLE_NOTIFICATION_EVENT_TYPE_1));
    }

    @Test(expectedExceptions = FSEventNotificationException.class)
    public void testGetEventSubscriptionBySubscriptionIdNotFound() throws SQLException, FSEventNotificationException {

        try (Connection connection = EventNotificationTestUtils.getConnection(DB_NAME)) {
            eventSubscriptionDAOImpl.getEventSubscriptionBySubscriptionId(
                    connection, EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);
        }
    }

    @Test
    public void testGetEventSubscriptionBySubscriptionIdDBError() throws SQLException {
        Mockito.when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        Mockito.when(mockedPreparedStatement.executeQuery()).thenThrow(new SQLException());

        Assert.assertThrows(FSEventNotificationException.class, () -> eventSubscriptionDAOImpl.
                getEventSubscriptionBySubscriptionId(mockedConnection,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1));
    }

    @Test
    public void testGetEventSubscriptionsByClientId() throws FSEventNotificationException, SQLException {

        List<EventSubscription> eventSubscriptions;
        try (Connection connection = EventNotificationTestUtils.getConnection(DB_NAME)) {
            eventSubscriptions = eventSubscriptionDAOImpl.getEventSubscriptionsByClientId(
                    connection, EventNotificationTestConstants.SAMPLE_CLIENT_ID);
        }

        Assert.assertNotNull(eventSubscriptions);
        Assert.assertEquals(eventSubscriptions.size(), 2); // We expect one EventSubscription object
    }

    @Test
    public void testGetEventSubscriptionsByClientIdNoSubscriptions()
            throws FSEventNotificationException, SQLException {

        List<EventSubscription> eventSubscriptions;
        try (Connection connection = EventNotificationTestUtils.getConnection(DB_NAME)) {
            eventSubscriptions = eventSubscriptionDAOImpl.getEventSubscriptionsByClientId(connection, "TestClientID");
        }

        Assert.assertNotNull(eventSubscriptions);
        Assert.assertTrue(eventSubscriptions.isEmpty()); // We expect an empty list since no data was found
    }

    @Test
    public void testGetEventSubscriptionsByEventType() throws FSEventNotificationException, SQLException {

        List<EventSubscription> eventSubscriptions;
        try (Connection connection = EventNotificationTestUtils.getConnection(DB_NAME)) {
            eventSubscriptions = eventSubscriptionDAOImpl.getEventSubscriptionsByEventType(connection,
                            EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1);
        }

        Assert.assertEquals(eventSubscriptions.size(), 1);

        EventSubscription subscription1 = eventSubscriptions.get(0);
        Assert.assertNotNull(subscription1.getSubscriptionId());
        Assert.assertEquals(subscription1.getEventTypes().size(), 2);
        Assert.assertTrue(subscription1.getEventTypes()
                .contains(EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1));
    }

    @Test
    public void testGetEventSubscriptionsByEventTypeNoSubscriptions() throws FSEventNotificationException,
            SQLException {

        List<EventSubscription> eventSubscriptions;
        try (Connection connection = EventNotificationTestUtils.getConnection(DB_NAME)) {
            eventSubscriptions = eventSubscriptionDAOImpl.getEventSubscriptionsByEventType(connection,
                    "sample_event_type");
        }

        Assert.assertEquals(eventSubscriptions.size(), 0);
    }

//    @Test(priority= 13, expectedExceptions = FSEventNotificationException.class)
//    public void testGetEventSubscriptionsByEventType_SQLException() throws FSEventNotificationException,
//            SQLException {
//        // Mock the behavior of the PreparedStatement
//        Mockito.when(mockedConnection.prepareStatement(anyString(), any(), any())).thenThrow(new SQLException());
//
//        // Call the method under test (expecting an exception to be thrown)
//        eventSubscriptionDAOImpl.getEventSubscriptionsByEventType(mockedConnection,
//                EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1);
//    }

    @Test
    public void testUpdateEventSubscription() throws FSEventNotificationException, SQLException {

        Boolean isUpdated;
        try (Connection connection = EventNotificationTestUtils.getConnection(DB_NAME)) {
            EventSubscription result = eventSubscriptionDAOImpl.storeEventSubscription(connection,
                    sampleEventSubscription);
            eventSubscriptionDAOImpl.storeSubscribedEventTypes(connection,
                    result.getSubscriptionId(), sampleEventTypes);
            isUpdated = eventSubscriptionDAOImpl.updateEventSubscription(connection,
                    EventNotificationTestUtils.getSampleEventSubscriptionToBeUpdated(result.getSubscriptionId()));
        }

        Assert.assertTrue(isUpdated);
    }

    @Test
    public void testUpdateEventSubscriptionFailed() throws FSEventNotificationException, SQLException {

        Boolean isUpdated;
        try (Connection connection = EventNotificationTestUtils.getConnection(DB_NAME)) {
            isUpdated = eventSubscriptionDAOImpl.updateEventSubscription(connection,
                    EventNotificationTestUtils.getSampleEventSubscriptionToBeUpdated(
                            EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1));
        }

        Assert.assertFalse(isUpdated);
    }

//    @Test(priority=16, expectedExceptions = FSEventNotificationException.class)
//    public void testUpdateEventSubscriptionDBError() throws SQLException, FSEventNotificationException {
//
//        ResultSet mockedResultSet = Mockito.mock(ResultSet.class);
//        Mockito.when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
//        Mockito.when(mockedConnection.prepareStatement(anyString())).thenThrow(new SQLException());
//
//        eventSubscriptionDAOImpl.updateEventSubscription(mockedConnection,
//                        EventNotificationTestUtils.getSampleEventSubscriptionToBeUpdated(
//                                EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1));
//    }

    @Test
    public void testDeleteEventSubscription() throws FSEventNotificationException, SQLException {

        Boolean isDeleted;
        try (Connection connection = EventNotificationTestUtils.getConnection(DB_NAME)) {
            EventSubscription result = eventSubscriptionDAOImpl.storeEventSubscription(connection,
                    sampleEventSubscription);
            isDeleted = eventSubscriptionDAOImpl.deleteEventSubscription(connection, result.getSubscriptionId());
        }

        Assert.assertTrue(isDeleted);
    }

    @Test
    public void testDeleteEventSubscriptionFails() throws FSEventNotificationException, SQLException {

        Boolean isDeleted;
        try (Connection connection = EventNotificationTestUtils.getConnection(DB_NAME)) {
            isDeleted = eventSubscriptionDAOImpl.deleteEventSubscription(connection,
                    EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);
        }

        Assert.assertFalse(isDeleted);
    }

    @Test(expectedExceptions = FSEventNotificationException.class)
    public void testDeleteEventSubscriptionDBError() throws FSEventNotificationException, SQLException {
        Mockito.when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        Mockito.when(mockedPreparedStatement.executeUpdate()).thenThrow(new SQLException());

        eventSubscriptionDAOImpl.deleteEventSubscription(mockedConnection,
                EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);
    }

    @Test
    public void testDeleteSubscribedEventTypes() throws FSEventNotificationException, SQLException {

        boolean isDeleted;
        try (Connection connection = EventNotificationTestUtils.getConnection(DB_NAME)) {
            EventSubscription result = eventSubscriptionDAOImpl.storeEventSubscription(connection,
                    sampleEventSubscription);
            eventSubscriptionDAOImpl.storeSubscribedEventTypes(connection,
                    result.getSubscriptionId(), sampleEventTypes);
            isDeleted = eventSubscriptionDAOImpl.deleteSubscribedEventTypes(connection,
                    result.getSubscriptionId());
        }

        Assert.assertTrue(isDeleted);
    }

    @Test
    public void testDeleteSubscribedEventTypesFails() throws FSEventNotificationException, SQLException {

        boolean isDeleted;
        try (Connection connection = EventNotificationTestUtils.getConnection(DB_NAME)) {
            isDeleted = eventSubscriptionDAOImpl.deleteSubscribedEventTypes(connection,
                    EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);
        }

        Assert.assertFalse(isDeleted);
    }

//    @Test(expectedExceptions = FSEventNotificationException.class)
//    public void testDeleteSubscribedEventTypesDBError() throws FSEventNotificationException, SQLException {
//        Mockito.when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
//        Mockito.when(mockedPreparedStatement.executeUpdate()).thenThrow(new SQLException());
//
//        eventSubscriptionDAOImpl.deleteSubscribedEventTypes(mockedConnection,
//                EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);
//    }
}

