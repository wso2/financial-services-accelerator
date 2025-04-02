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
import org.wso2.financial.services.accelerator.event.notifications.service.model.EventSubscription;
import org.wso2.financial.services.accelerator.event.notifications.service.queries.EventSubscriptionSqlStatements;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationTestUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;


/**
 * Test class for PostgreSqlEventSubscriptionDAOImpl.
 */
public class PostgreSqlEventSubscriptionDAOImplTests {

    private static Connection mockedConnection;
    private PreparedStatement mockedPreparedStatement;
    private MockedStatic<DatabaseUtils> databaseUtilMockedStatic;

    PostgreSqlEventSubscriptionDAOImpl eventSubscriptionDAOImpl = new PostgreSqlEventSubscriptionDAOImpl(
            new EventSubscriptionSqlStatements());

    @BeforeClass
    public void initTest() {

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
    public void testStoreEventSubscription() throws FSEventNotificationException, SQLException {

        Mockito.when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        Mockito.when(mockedPreparedStatement.executeUpdate()).thenReturn(1);
        EventSubscription sampleEventSubscription = EventNotificationTestUtils.getSampleEventSubscription();

        EventSubscription result = eventSubscriptionDAOImpl.storeEventSubscription(mockedConnection,
                sampleEventSubscription);

        Assert.assertNotNull(sampleEventSubscription.getSubscriptionId());
        Assert.assertNotNull(sampleEventSubscription.getTimeStamp());
        Assert.assertEquals("CREATED", result.getStatus());
    }

    @Test(expectedExceptions = FSEventNotificationException.class)
    public void testStoreEventSubscriptionDBError() throws FSEventNotificationException, SQLException {

        Mockito.when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        Mockito.when(mockedPreparedStatement.executeUpdate()).thenThrow(new SQLException());

        eventSubscriptionDAOImpl.storeEventSubscription(mockedConnection,
                EventNotificationTestUtils.getSampleEventSubscription());
    }

    @Test
    public void testStoreSubscribedEventTypes() throws FSEventNotificationException, SQLException {

        Mockito.when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        Mockito.when(mockedPreparedStatement.executeBatch()).thenReturn(new int[]{1, 1, 1});
        List<String> sampleEventTypes = EventNotificationTestUtils.getSampleStoredEventTypes();

        List<String> result = eventSubscriptionDAOImpl.storeSubscribedEventTypes(mockedConnection,
                EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1, sampleEventTypes);

        Assert.assertEquals(sampleEventTypes, result);
    }

    @Test
    public void testStoreSubscribedEventTypesFailure() throws SQLException {

        Mockito.when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        Mockito.when(mockedPreparedStatement.executeBatch()).thenReturn(new int[]{0, 1, 1});
        List<String> sampleEventTypes = EventNotificationTestUtils.getSampleStoredEventTypes();

        Assert.assertThrows(FSEventNotificationException.class, () -> eventSubscriptionDAOImpl.
                storeSubscribedEventTypes(mockedConnection, EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1,
                        sampleEventTypes));
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

        Mockito.when(mockedConnection.prepareStatement(anyString(), anyInt(), anyInt()))
                .thenReturn(mockedPreparedStatement);
        ResultSet mockedResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
        Mockito.when(mockedResultSet.next()).thenReturn(true, true, true, false);
        Mockito.when(mockedResultSet.getString(EventNotificationConstants.EVENT_TYPE)).
                thenReturn(EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1,
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1,
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_2,
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_2);

        EventSubscription result = eventSubscriptionDAOImpl.getEventSubscriptionBySubscriptionId(
                mockedConnection, EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);
        List<String> eventTypes = result.getEventTypes();
        Assert.assertTrue(eventTypes.contains(EventNotificationTestConstants.
                SAMPLE_NOTIFICATION_EVENT_TYPE_1));
    }

    @Test
    public void testGetEventSubscriptionBySubscriptionIdNotFound() throws SQLException {

        Mockito.when(mockedConnection.prepareStatement(anyString(), anyInt(), anyInt()))
                .thenReturn(mockedPreparedStatement);
        ResultSet mockedResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
        Mockito.when(mockedResultSet.next()).thenReturn(false);

        Assert.assertThrows(FSEventNotificationException.class, () -> eventSubscriptionDAOImpl.
                getEventSubscriptionBySubscriptionId(mockedConnection,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1));
    }

    @Test
    public void testGetEventSubscriptionBySubscriptionIdDBError() throws SQLException {

        Mockito.when(mockedConnection.prepareStatement(anyString(), anyInt(), anyInt()))
                .thenReturn(mockedPreparedStatement);
        Mockito.when(mockedPreparedStatement.executeQuery()).thenThrow(new SQLException());

        Assert.assertThrows(FSEventNotificationException.class, () -> eventSubscriptionDAOImpl.
                getEventSubscriptionBySubscriptionId(mockedConnection,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1));
    }

    @Test
    public void testGetEventSubscriptionsByClientId() throws FSEventNotificationException, SQLException {

        Mockito.when(mockedConnection.prepareStatement(anyString(), anyInt(), anyInt()))
                .thenReturn(mockedPreparedStatement);
        ResultSet mockedResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
        Mockito.when(mockedResultSet.isBeforeFirst()).thenReturn(true);
        Mockito.when(mockedResultSet.next()).thenReturn(true, true, true, true, true, true, true, false);
        Mockito.when(mockedResultSet.getString(EventNotificationConstants.SUBSCRIPTION_ID)).
                thenReturn(EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_2,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_2,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_2,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_2);
        Mockito.when(mockedResultSet.getString(EventNotificationConstants.EVENT_TYPE)).
                thenReturn(EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1,
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1,
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_2,
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_2,
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1,
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1,
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_2,
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_2);

        List<EventSubscription> eventSubscriptions = eventSubscriptionDAOImpl.getEventSubscriptionsByClientId(
                mockedConnection, EventNotificationTestConstants.SAMPLE_CLIENT_ID);

        Assert.assertNotNull(eventSubscriptions);
        Assert.assertEquals(2, eventSubscriptions.size()); // We expect one EventSubscription object

        EventSubscription subscription = eventSubscriptions.get(0);
        Assert.assertNotNull(subscription.getEventTypes());
        Assert.assertEquals(subscription.getEventTypes().size(), 2);
        Assert.assertTrue(subscription.getEventTypes().contains(EventNotificationTestConstants.
                SAMPLE_NOTIFICATION_EVENT_TYPE_1));
        Assert.assertTrue(subscription.getEventTypes().contains(EventNotificationTestConstants.
                SAMPLE_NOTIFICATION_EVENT_TYPE_2));

        EventSubscription subscription2 = eventSubscriptions.get(1);
        Assert.assertNotNull(subscription2.getEventTypes());
        Assert.assertEquals(subscription2.getEventTypes().size(), 2);
        Assert.assertTrue(subscription2.getEventTypes().contains(EventNotificationTestConstants.
                SAMPLE_NOTIFICATION_EVENT_TYPE_1));
        Assert.assertTrue(subscription2.getEventTypes().contains(EventNotificationTestConstants.
                SAMPLE_NOTIFICATION_EVENT_TYPE_2));
    }

    @Test
    public void testGetEventSubscriptionsByClientIdNoSubscriptions() throws FSEventNotificationException,
    SQLException {

        Mockito.when(mockedConnection.prepareStatement(anyString(), anyInt(), anyInt()))
                .thenReturn(mockedPreparedStatement);
        ResultSet mockedResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
        Mockito.when(mockedResultSet.isBeforeFirst()).thenReturn(false);

        List<EventSubscription> eventSubscriptions = eventSubscriptionDAOImpl.getEventSubscriptionsByClientId(
                mockedConnection, EventNotificationTestConstants.SAMPLE_CLIENT_ID);

        Assert.assertNotNull(eventSubscriptions);
        Assert.assertTrue(eventSubscriptions.isEmpty()); // We expect an empty list since no data was found
    }

    @Test
    public void testGetEventSubscriptionsByEventType() throws FSEventNotificationException, SQLException {

        Mockito.when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        ResultSet mockedResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
        Mockito.when(mockedResultSet.isBeforeFirst()).thenReturn(true);
        Mockito.when(mockedResultSet.next()).thenReturn(true, true, true, true, true, true, true, false);
        Mockito.when(mockedResultSet.getString(EventNotificationConstants.SUBSCRIPTION_ID)).
                thenReturn(EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_2,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_2,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_2,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_2);
        Mockito.when(mockedResultSet.getString(EventNotificationConstants.EVENT_TYPE)).
                thenReturn(EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1,
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1,
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_2,
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_2,
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1,
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1,
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_2,
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_2);

        List<EventSubscription> eventSubscriptions = eventSubscriptionDAOImpl.
                getEventSubscriptionsByEventType(mockedConnection,
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1);

        Assert.assertEquals(2, eventSubscriptions.size());

        EventSubscription subscription1 = eventSubscriptions.get(0);
        Assert.assertEquals(EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1,
        subscription1.getSubscriptionId());
        Assert.assertEquals(subscription1.getEventTypes().size(), 2);
        Assert.assertEquals(EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1,
                subscription1.getEventTypes().get(0));

        EventSubscription subscription2 = eventSubscriptions.get(1);
        Assert.assertEquals(EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_2,
        subscription2.getSubscriptionId());
        Assert.assertEquals(subscription2.getEventTypes().size(), 2);
        Assert.assertEquals(EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1,
                subscription2.getEventTypes().get(0));
    }

    @Test
    public void testGetEventSubscriptionsByEventTypeNoSubscriptions() throws FSEventNotificationException,
            SQLException {

        Mockito.when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        ResultSet mockedResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
        Mockito.when(mockedResultSet.isBeforeFirst()).thenReturn(false);

        List<EventSubscription> eventSubscriptions = eventSubscriptionDAOImpl.
                getEventSubscriptionsByEventType(mockedConnection,
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1);

        Assert.assertEquals(0, eventSubscriptions.size());
    }

    @Test(expectedExceptions = FSEventNotificationException.class)
    public void testGetEventSubscriptionsByEventType_SQLException() throws FSEventNotificationException,
            SQLException {

        // Mock the behavior of the PreparedStatement and ResultSet
        Mockito.when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        ResultSet mockedResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
        Mockito.when(mockedResultSet.isBeforeFirst()).thenThrow(new SQLException());

        // Call the method under test (expecting an exception to be thrown)
        eventSubscriptionDAOImpl.getEventSubscriptionsByEventType(mockedConnection,
                EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1);
    }

    @Test
    public void testUpdateEventSubscription() throws FSEventNotificationException, SQLException {

        Mockito.when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        Mockito.when(mockedPreparedStatement.executeUpdate()).thenReturn(1);

        Boolean isUpdated = eventSubscriptionDAOImpl.updateEventSubscription(mockedConnection,
                EventNotificationTestUtils.getSampleEventSubscriptionToBeUpdated(
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1));
        Assert.assertTrue(isUpdated);
    }

    @Test
    public void testUpdateEventSubscriptionFailed() throws FSEventNotificationException, SQLException {

        Mockito.when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        Mockito.when(mockedPreparedStatement.executeUpdate()).thenReturn(0);

        Boolean isUpdated = eventSubscriptionDAOImpl.updateEventSubscription(mockedConnection,
                EventNotificationTestUtils.getSampleEventSubscriptionToBeUpdated(
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1));
        Assert.assertFalse(isUpdated);
    }

    @Test
    public void testUpdateEventSubscriptionDBError() throws SQLException {

        Mockito.when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        Mockito.when(mockedPreparedStatement.executeUpdate()).thenThrow(new SQLException());

        Assert.assertThrows(FSEventNotificationException.class,
                () -> eventSubscriptionDAOImpl.updateEventSubscription(mockedConnection,
                        EventNotificationTestUtils.getSampleEventSubscriptionToBeUpdated(
                                EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1)));
    }

    @Test
    public void testDeleteEventSubscription() throws FSEventNotificationException, SQLException {

        Mockito.when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        Mockito.when(mockedPreparedStatement.executeUpdate()).thenReturn(1);

        Boolean isDeleted = eventSubscriptionDAOImpl.deleteEventSubscription(mockedConnection,
                EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);

        Assert.assertTrue(isDeleted);
    }

    @Test
    public void testDeleteEventSubscriptionFails() throws FSEventNotificationException, SQLException {

        Mockito.when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        Mockito.when(mockedPreparedStatement.executeUpdate()).thenReturn(0);

        Boolean isDeleted = eventSubscriptionDAOImpl.deleteEventSubscription(mockedConnection,
                EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);

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

        Mockito.when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        Mockito.when(mockedPreparedStatement.executeUpdate()).thenReturn(1);

        boolean isDeleted = eventSubscriptionDAOImpl.deleteSubscribedEventTypes(mockedConnection,
                EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);

        Assert.assertTrue(isDeleted);
    }

    @Test
    public void testDeleteSubscribedEventTypesFails() throws FSEventNotificationException, SQLException {

        Mockito.when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        Mockito.when(mockedPreparedStatement.executeUpdate()).thenReturn(0);

        boolean isDeleted = eventSubscriptionDAOImpl.deleteSubscribedEventTypes(mockedConnection,
                EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);

        Assert.assertFalse(isDeleted);
    }

    @Test(expectedExceptions = FSEventNotificationException.class)
    public void testDeleteSubscribedEventTypesDBError() throws FSEventNotificationException, SQLException {

        Mockito.when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        Mockito.when(mockedPreparedStatement.executeUpdate()).thenThrow(new SQLException());

        eventSubscriptionDAOImpl.deleteSubscribedEventTypes(mockedConnection,
                EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);
    }
}

