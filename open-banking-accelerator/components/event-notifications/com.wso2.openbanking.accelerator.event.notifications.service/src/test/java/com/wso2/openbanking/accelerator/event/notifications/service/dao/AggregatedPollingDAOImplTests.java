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

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.DatabaseUtil;
import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationTestConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.dto.NotificationDTO;
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.model.NotificationError;
import com.wso2.openbanking.accelerator.event.notifications.service.model.NotificationEvent;
import com.wso2.openbanking.accelerator.event.notifications.service.utils.EventNotificationTestUtils;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;
/**
 * Test class for AggregatedPollingDAOImpl.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest(DatabaseUtil.class)
public class AggregatedPollingDAOImplTests extends PowerMockTestCase {

    private static Connection mockedConnection;
    private static Connection mockedExceptionConnection;
    private PreparedStatement mockedPreparedStatement;


    AggregatedPollingDAOImpl aggregatedPollingDAOImpl = new AggregatedPollingDAOImpl(
            new NotificationPollingSqlStatements());

    @BeforeClass
    public void initTest() throws Exception {

        mockedConnection = Mockito.mock(Connection.class);
        mockedExceptionConnection = Mockito.mock(Connection.class);
        mockedPreparedStatement = Mockito.mock(PreparedStatement.class);
    }

    @BeforeMethod
    public void mock() throws ConsentManagementException {

        PowerMockito.mockStatic(DatabaseUtil.class);
        PowerMockito.when(DatabaseUtil.getDBConnection()).thenReturn(mockedConnection);
    }

    @Test
    public void testGetNotificationsByStatus() throws OBEventNotificationException, SQLException {
        ResultSet mockedResultSet = Mockito.mock(ResultSet.class);
        when(mockedResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(mockedResultSet.isBeforeFirst()).thenReturn(true);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
        when(mockedResultSet.getString(EventNotificationConstants.NOTIFICATION_ID)).thenReturn(
                EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);
        when(mockedResultSet.getString(EventNotificationConstants.CLIENT_ID)).thenReturn(
                EventNotificationTestConstants.SAMPLE_CLIENT_ID);
        when(mockedResultSet.getString(EventNotificationConstants.RESOURCE_ID)).thenReturn(
                EventNotificationTestConstants.SAMPLE_RESOURCE_ID);
        when(mockedResultSet.getString(EventNotificationConstants.STATUS)).thenReturn("OPEN");
        when(mockedResultSet.getTimestamp(EventNotificationConstants.UPDATED_TIMESTAMP)).thenReturn(
                new Timestamp(System.currentTimeMillis()));

        List<NotificationDTO> eventsList = aggregatedPollingDAOImpl.getNotificationsByStatus("OPEN");
        Assert.assertEquals(EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID,
                eventsList.get(0).getNotificationId());
    }

    @Test
    public void testGetEventsByNotificationID() throws OBEventNotificationException, SQLException, IOException {

        ResultSet mockedResultSet = Mockito.mock(ResultSet.class);
        when(mockedResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(mockedResultSet.isBeforeFirst()).thenReturn(true);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
        when(mockedResultSet.getString(EventNotificationConstants.NOTIFICATION_ID)).thenReturn(
                EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);
        when(mockedResultSet.getString(EventNotificationConstants.EVENT_TYPE)).thenReturn(
                EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1);
        when(mockedResultSet.getString(EventNotificationConstants.EVENT_INFO)).thenReturn(
                EventNotificationTestUtils.getSampleEventInformation().toString());

        List<NotificationEvent> eventsList = aggregatedPollingDAOImpl.getEventsByNotificationID(
                EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);
        Assert.assertEquals(EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID,
                eventsList.get(0).getNotificationId());

    }

    @Test
    public void testGetNotificationsByClientIdAndStatus() throws SQLException, OBEventNotificationException {

        ResultSet mockedResultSet = Mockito.mock(ResultSet.class);
        when(mockedResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(mockedResultSet.isBeforeFirst()).thenReturn(true);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
        when(mockedResultSet.getString(EventNotificationConstants.NOTIFICATION_ID)).thenReturn(
                EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);
        when(mockedResultSet.getString(EventNotificationConstants.CLIENT_ID)).thenReturn(
                EventNotificationTestConstants.SAMPLE_CLIENT_ID);
        when(mockedResultSet.getString(EventNotificationConstants.RESOURCE_ID)).thenReturn(
                EventNotificationTestConstants.SAMPLE_RESOURCE_ID);
        when(mockedResultSet.getString(EventNotificationConstants.STATUS)).thenReturn("OPEN");
        when(mockedResultSet.getTimestamp(EventNotificationConstants.UPDATED_TIMESTAMP)).thenReturn(
                new Timestamp(System.currentTimeMillis()));

        List<NotificationDTO> eventsList = aggregatedPollingDAOImpl.getNotificationsByClientIdAndStatus(
                EventNotificationTestConstants.SAMPLE_CLIENT_ID, "OPEN", 5);
        Assert.assertEquals(EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID,
                eventsList.get(0).getNotificationId());
    }

    @Test
    public void testGetEventsStatus() throws SQLException, IOException, OBEventNotificationException {

        ResultSet mockedResultSet = Mockito.mock(ResultSet.class);
        when(mockedResultSet.next()).thenReturn(true);
        when(mockedResultSet.getString("STATUS")).thenReturn(EventNotificationConstants.OPEN);
        when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);

        boolean status = aggregatedPollingDAOImpl.getNotificationStatus(EventNotificationTestConstants.
                SAMPLE_NOTIFICATION_ID);

        Assert.assertTrue(status);

    }

    @Test
    public void testStoreErrorNotifications() throws SQLException, OBEventNotificationException {

        PowerMockito.mockStatic(DatabaseUtil.class);
        PowerMockito.when(DatabaseUtil.getDBConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeUpdate()).thenReturn(1);

        Map<String, NotificationError> errors = aggregatedPollingDAOImpl.
                storeErrorNotification(EventNotificationTestUtils.getNotificationError());

        Assert.assertTrue(errors.containsKey(EventNotificationTestConstants.SAMPLE_ERROR_NOTIFICATION_ID));
    }

    @Test
    public void testGetEventsStatusACK() throws SQLException, IOException, OBEventNotificationException {

        ResultSet mockedResultSet = Mockito.mock(ResultSet.class);
        when(mockedResultSet.next()).thenReturn(true);
        when(mockedResultSet.getString("STATUS")).thenReturn(EventNotificationConstants.ACK);
        when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);

        boolean status = aggregatedPollingDAOImpl.getNotificationStatus(EventNotificationTestConstants.
                SAMPLE_NOTIFICATION_ID);

        Assert.assertFalse(status);

    }

    @Test(expectedExceptions = OBEventNotificationException.class)
    public void testGetEventsStatusDBError() throws SQLException, OBEventNotificationException {

        PowerMockito.mockStatic(DatabaseUtil.class);
        PowerMockito.when(DatabaseUtil.getDBConnection()).thenReturn(mockedExceptionConnection);
        ResultSet mockedResultSet = Mockito.mock(ResultSet.class);
        when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
        when(mockedExceptionConnection.prepareStatement(anyString())).thenThrow(new SQLException());

        boolean status = aggregatedPollingDAOImpl.getNotificationStatus(EventNotificationTestConstants.
                SAMPLE_NOTIFICATION_ID);
    }

    @Test
    public void testGetNotificationCount() throws SQLException, OBEventNotificationException {

        ResultSet mockedResultSet = Mockito.mock(ResultSet.class);
        when(mockedResultSet.next()).thenReturn(true);
        when(mockedResultSet.getInt("NOTIFICATION_COUNT")).thenReturn(4);
        when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);

        int eventCount = aggregatedPollingDAOImpl.getNotificationCountByClientIdAndStatus(
                EventNotificationTestConstants.SAMPLE_CLIENT_ID, EventNotificationConstants.OPEN);

        Assert.assertEquals(eventCount, 4);
    }

    @Test
    public void testGetNotificationCountNoEvents() throws SQLException, OBEventNotificationException {

        ResultSet mockedResultSet = Mockito.mock(ResultSet.class);
        when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);

        int eventCount = aggregatedPollingDAOImpl.getNotificationCountByClientIdAndStatus(
                EventNotificationTestConstants.SAMPLE_CLIENT_ID, EventNotificationConstants.OPEN);

        Assert.assertEquals(eventCount, 0);
    }

    @Test
    public void testUpdateNotificationStatusById() throws SQLException, OBEventNotificationException {

        PowerMockito.mockStatic(DatabaseUtil.class);
        PowerMockito.when(DatabaseUtil.getDBConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeUpdate()).thenReturn(1);

        Boolean updatedStatus = aggregatedPollingDAOImpl.updateNotificationStatusById(
                EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID, "ACK");

        Assert.assertTrue(updatedStatus);
    }

    @Test
    public void testUpdateNotificationStatusByIdError() throws SQLException, OBEventNotificationException {

        PowerMockito.mockStatic(DatabaseUtil.class);
        PowerMockito.when(DatabaseUtil.getDBConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeUpdate()).thenReturn(0);

        Boolean updatedStatus = aggregatedPollingDAOImpl.updateNotificationStatusById(
                EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID, "ACK");

        Assert.assertFalse(updatedStatus);
    }
}
