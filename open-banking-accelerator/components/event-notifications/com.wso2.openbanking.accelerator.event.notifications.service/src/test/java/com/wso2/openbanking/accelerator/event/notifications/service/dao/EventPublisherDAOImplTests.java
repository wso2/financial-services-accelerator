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
import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationTestConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.utils.EventNotificationTestUtils;
import net.minidev.json.parser.ParseException;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;
/**
 * Test class for EventPublisherDAOImpl.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest(DatabaseUtil.class)
public class EventPublisherDAOImplTests extends PowerMockTestCase {

    private static Connection mockedConnection;
    private static Connection mockedExceptionConnection;
    private PreparedStatement mockedPreparedStatement;

    EventPublisherDAOImpl eventPublisherDAOImpl = new EventPublisherDAOImpl(new NotificationPublisherSqlStatements());

    @BeforeClass
    public void initTest() throws Exception {

        mockedConnection = Mockito.mock(Connection.class);
        mockedExceptionConnection = Mockito.mock(Connection.class);
        mockedPreparedStatement = Mockito.mock(PreparedStatement.class);
    }

    @Test
    public void testPersistEventNotification() throws SQLException, ParseException, OBEventNotificationException {

        PowerMockito.mockStatic(DatabaseUtil.class);
        int[] noOfRows = new int[5];
        PowerMockito.when(DatabaseUtil.getDBConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockedPreparedStatement.executeBatch()).thenReturn(noOfRows);

        String notificationId = eventPublisherDAOImpl.persistEventNotification(mockedConnection,
                EventNotificationTestUtils.getSampleNotificationDTO(),
                EventNotificationTestUtils.getSampleEventList());

        Assert.assertEquals(notificationId, EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);
    }

    @Test(expectedExceptions = OBEventNotificationException.class)
    public void testPersistEventNotificationDBError() throws SQLException,
            ParseException, OBEventNotificationException {

        PowerMockito.mockStatic(DatabaseUtil.class);
        int[] noOfRows = new int[5];
        PowerMockito.when(DatabaseUtil.getDBConnection()).thenReturn(mockedExceptionConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
        when(mockedPreparedStatement.executeUpdate()).thenThrow(new SQLException());
        //when(mockedPreparedStatement.executeBatch()).thenReturn(noOfRows);

        String notificationId = eventPublisherDAOImpl.persistEventNotification(mockedConnection,
                EventNotificationTestUtils.getSampleNotificationDTO(),
                EventNotificationTestUtils.getSampleEventList());

        Assert.assertEquals(notificationId, EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);
    }
}
