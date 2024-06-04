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

package com.wso2.openbanking.accelerator.event.notifications.service.dao;

import com.wso2.openbanking.accelerator.common.util.DatabaseUtil;
import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationTestConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.model.EventSubscription;
import com.wso2.openbanking.accelerator.event.notifications.service.utils.EventNotificationTestUtils;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Test class for EventSubscriptionDAOImpl.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest(DatabaseUtil.class)
public class EventSubscriptionDAOImplTests extends PowerMockTestCase {
    private static Connection mockedConnection;
    private PreparedStatement mockedPreparedStatement;

    EventSubscriptionDAOImpl eventSubscriptionDAOImpl = new EventSubscriptionDAOImpl(
            new EventSubscriptionSqlStatements());

    @BeforeMethod
    public void mock() throws OBEventNotificationException {
        mockedConnection = Mockito.mock(Connection.class);
        mockedPreparedStatement = Mockito.mock(PreparedStatement.class);
        PowerMockito.mockStatic(DatabaseUtil.class);
        PowerMockito.when(DatabaseUtil.getDBConnection()).thenReturn(mockedConnection);
    }

    @Test
    public void testStoreEventSubscription() throws OBEventNotificationException, SQLException {
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeUpdate()).thenReturn(1);
        EventSubscription sampleEventSubscription = EventNotificationTestUtils.getSampleEventSubscription();

        EventSubscription result = eventSubscriptionDAOImpl.storeEventSubscription(mockedConnection,
                sampleEventSubscription);

        Assert.assertNotNull(sampleEventSubscription.getSubscriptionId());
        Assert.assertNotNull(sampleEventSubscription.getTimeStamp());
        Assert.assertEquals("CREATED", result.getStatus());
    }

    @Test(expectedExceptions = OBEventNotificationException.class)
    public void testStoreEventSubscriptionDBError() throws OBEventNotificationException, SQLException {
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeUpdate()).thenThrow(new SQLException());

        eventSubscriptionDAOImpl.storeEventSubscription(mockedConnection,
                EventNotificationTestUtils.getSampleEventSubscription());
    }

    @Test
    public void testStoreSubscribedEventTypes() throws OBEventNotificationException, SQLException {
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeBatch()).thenReturn(new int[]{1, 1, 1});
        List<String> sampleEventTypes = EventNotificationTestUtils.getSampleStoredEventTypes();

        List<String> result = eventSubscriptionDAOImpl.storeSubscribedEventTypes(mockedConnection,
                EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1, sampleEventTypes);

        Assert.assertEquals(sampleEventTypes, result);
    }

    @Test
    public void testStoreSubscribedEventTypesFailure() throws SQLException {
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeBatch()).thenReturn(new int[]{0, 1, 1});
        List<String> sampleEventTypes = EventNotificationTestUtils.getSampleStoredEventTypes();

        Assert.assertThrows(OBEventNotificationException.class, () -> eventSubscriptionDAOImpl.
                storeSubscribedEventTypes(mockedConnection, EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1,
                        sampleEventTypes));
    }

    @Test
    public void testStoreSubscribedEventTypesDBError() throws SQLException {
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeBatch()).thenThrow(new SQLException());
        List<String> sampleEventTypes = EventNotificationTestUtils.getSampleStoredEventTypes();

        Assert.assertThrows(OBEventNotificationException.class, () -> eventSubscriptionDAOImpl.
                storeSubscribedEventTypes(mockedConnection, EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1,
                        sampleEventTypes));
    }

    @Test
    public void testGetEventSubscriptionBySubscriptionId() throws OBEventNotificationException, SQLException {
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        ResultSet mockedResultSet = Mockito.mock(ResultSet.class);
        when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
        when(mockedResultSet.next()).thenReturn(true, true, true, false);
        when(mockedResultSet.getString(EventNotificationConstants.EVENT_TYPE)).
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
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        ResultSet mockedResultSet = Mockito.mock(ResultSet.class);
        when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
        when(mockedResultSet.next()).thenReturn(false);

        Assert.assertThrows(OBEventNotificationException.class, () -> eventSubscriptionDAOImpl.
                getEventSubscriptionBySubscriptionId(mockedConnection,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1));
    }

    @Test
    public void testGetEventSubscriptionBySubscriptionIdDBError() throws SQLException {
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeQuery()).thenThrow(new SQLException());

        Assert.assertThrows(OBEventNotificationException.class, () -> eventSubscriptionDAOImpl.
                getEventSubscriptionBySubscriptionId(mockedConnection,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1));
    }

    @Test
    public void testGetEventSubscriptionsByClientId() throws OBEventNotificationException, SQLException {
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        ResultSet mockedResultSet = Mockito.mock(ResultSet.class);
        when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
        when(mockedResultSet.isBeforeFirst()).thenReturn(true);
        when(mockedResultSet.next()).thenReturn(true, true, true, true, true, true, true, false);
        when(mockedResultSet.getString(EventNotificationConstants.SUBSCRIPTION_ID)).
                thenReturn(EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_2,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_2,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_2,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_2);
        when(mockedResultSet.getString(EventNotificationConstants.EVENT_TYPE)).
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
    public void testGetEventSubscriptionsByClientIdNoSubscriptions() throws OBEventNotificationException, SQLException {
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        ResultSet mockedResultSet = Mockito.mock(ResultSet.class);
        when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
        when(mockedResultSet.isBeforeFirst()).thenReturn(false);

        List<EventSubscription> eventSubscriptions = eventSubscriptionDAOImpl.getEventSubscriptionsByClientId(
                mockedConnection, EventNotificationTestConstants.SAMPLE_CLIENT_ID);

        Assert.assertNotNull(eventSubscriptions);
        Assert.assertTrue(eventSubscriptions.isEmpty()); // We expect an empty list since no data was found
    }

    @Test
    public void testGetEventSubscriptionsByEventType() throws OBEventNotificationException, SQLException {
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        ResultSet mockedResultSet = Mockito.mock(ResultSet.class);
        when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
        when(mockedResultSet.isBeforeFirst()).thenReturn(true);
        when(mockedResultSet.next()).thenReturn(true, true, true, true, true, true, true, false);
        when(mockedResultSet.getString(EventNotificationConstants.SUBSCRIPTION_ID)).
                thenReturn(EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_2,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_2,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_2,
                        EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_2);
        when(mockedResultSet.getString(EventNotificationConstants.EVENT_TYPE)).
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
        Assert.assertEquals(EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1, subscription1.getSubscriptionId());
        Assert.assertEquals(subscription1.getEventTypes().size(), 2);
        Assert.assertEquals(EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1,
                subscription1.getEventTypes().get(0));

        EventSubscription subscription2 = eventSubscriptions.get(1);
        Assert.assertEquals(EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_2, subscription2.getSubscriptionId());
        Assert.assertEquals(subscription2.getEventTypes().size(), 2);
        Assert.assertEquals(EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1,
                subscription2.getEventTypes().get(0));
    }

    @Test
    public void testGetEventSubscriptionsByEventTypeNoSubscriptions() throws OBEventNotificationException,
            SQLException {
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        ResultSet mockedResultSet = Mockito.mock(ResultSet.class);
        when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
        when(mockedResultSet.isBeforeFirst()).thenReturn(false);

        List<EventSubscription> eventSubscriptions = eventSubscriptionDAOImpl.
                getEventSubscriptionsByEventType(mockedConnection,
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1);

        Assert.assertEquals(0, eventSubscriptions.size());
    }

    @Test(expectedExceptions = OBEventNotificationException.class)
    public void testGetEventSubscriptionsByEventType_SQLException() throws OBEventNotificationException,
            SQLException {
        // Mock the behavior of the PreparedStatement and ResultSet
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        ResultSet mockedResultSet = Mockito.mock(ResultSet.class);
        when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
        when(mockedResultSet.isBeforeFirst()).thenThrow(new SQLException());

        // Call the method under test (expecting an exception to be thrown)
        eventSubscriptionDAOImpl.getEventSubscriptionsByEventType(mockedConnection,
                EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1);
    }

    @Test
    public void testUpdateEventSubscription() throws OBEventNotificationException, SQLException {
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeUpdate()).thenReturn(1);

        Boolean isUpdated = eventSubscriptionDAOImpl.updateEventSubscription(mockedConnection,
                EventNotificationTestUtils.getSampleEventSubscriptionToBeUpdated());
        Assert.assertTrue(isUpdated);
    }

    @Test
    public void testUpdateEventSubscriptionFailed() throws OBEventNotificationException, SQLException {
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeUpdate()).thenReturn(0);

        Boolean isUpdated = eventSubscriptionDAOImpl.updateEventSubscription(mockedConnection,
                EventNotificationTestUtils.getSampleEventSubscriptionToBeUpdated());
        Assert.assertFalse(isUpdated);
    }

    @Test
    public void testUpdateEventSubscriptionDBError() throws SQLException {
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeUpdate()).thenThrow(new SQLException());

        Assert.assertThrows(OBEventNotificationException.class,
                () -> eventSubscriptionDAOImpl.updateEventSubscription(mockedConnection,
                        EventNotificationTestUtils.getSampleEventSubscriptionToBeUpdated()));
    }

    @Test
    public void testDeleteEventSubscription() throws OBEventNotificationException, SQLException {
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeUpdate()).thenReturn(1);

        Boolean isDeleted = eventSubscriptionDAOImpl.deleteEventSubscription(mockedConnection,
                EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);

        Assert.assertTrue(isDeleted);
    }

    @Test
    public void testDeleteEventSubscriptionFails() throws OBEventNotificationException, SQLException {
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeUpdate()).thenReturn(0);

        Boolean isDeleted = eventSubscriptionDAOImpl.deleteEventSubscription(mockedConnection,
                EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);

        Assert.assertFalse(isDeleted);
    }

    @Test(expectedExceptions = OBEventNotificationException.class)
    public void testDeleteEventSubscriptionDBError() throws OBEventNotificationException, SQLException {
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeUpdate()).thenThrow(new SQLException());

        eventSubscriptionDAOImpl.deleteEventSubscription(mockedConnection,
                EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);
    }

    @Test
    public void testDeleteSubscribedEventTypes() throws OBEventNotificationException, SQLException {
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeUpdate()).thenReturn(1);

        boolean isDeleted = eventSubscriptionDAOImpl.deleteSubscribedEventTypes(mockedConnection,
                EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);

        Assert.assertTrue(isDeleted);
    }

    @Test
    public void testDeleteSubscribedEventTypesFails() throws OBEventNotificationException, SQLException {
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeUpdate()).thenReturn(0);

        boolean isDeleted = eventSubscriptionDAOImpl.deleteSubscribedEventTypes(mockedConnection,
                EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);

        Assert.assertFalse(isDeleted);
    }

    @Test(expectedExceptions = OBEventNotificationException.class)
    public void testDeleteSubscribedEventTypesDBError() throws OBEventNotificationException, SQLException {
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeUpdate()).thenThrow(new SQLException());

        eventSubscriptionDAOImpl.deleteSubscribedEventTypes(mockedConnection,
                EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);
    }
}

