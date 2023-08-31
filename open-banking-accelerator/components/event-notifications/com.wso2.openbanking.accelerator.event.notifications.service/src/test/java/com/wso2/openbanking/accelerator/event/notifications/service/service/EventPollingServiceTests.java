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

package com.wso2.openbanking.accelerator.event.notifications.service.service;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.common.util.DatabaseUtil;
import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.dao.AggregatedPollingDAO;
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.model.AggregatedPollingResponse;
import com.wso2.openbanking.accelerator.event.notifications.service.persistence.EventPollingStoreInitializer;
import com.wso2.openbanking.accelerator.event.notifications.service.util.EventNotificationServiceUtil;
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

import java.sql.Connection;
/**
 * Test class for EventPollingService.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({DatabaseUtil.class, EventPollingStoreInitializer.class, EventNotificationServiceUtil.class,
        OpenBankingConfigParser.class})
public class EventPollingServiceTests extends PowerMockTestCase {
    private static Connection mockedConnection;
    private static AggregatedPollingDAO mockedAggregatedPollingDAO;
    private static EventNotificationGenerator mockedEventNotificationGenerator;

    @BeforeClass
    public void initTest() throws Exception {

        mockedConnection = Mockito.mock(Connection.class);
    }

    @BeforeMethod
    public void mock() throws ConsentManagementException, OBEventNotificationException {

        PowerMockito.mockStatic(DatabaseUtil.class);
        PowerMockito.when(DatabaseUtil.getDBConnection()).thenReturn(mockedConnection);
        EventNotificationTestUtils.mockConfigParser();
    }

    @Test
    public void testPollEventsNoNotifications() throws OBEventNotificationException {

        mockedAggregatedPollingDAO = Mockito.mock(AggregatedPollingDAO.class);
        mockedEventNotificationGenerator = Mockito.mock(EventNotificationGenerator.class);

        PowerMockito.mockStatic(EventPollingStoreInitializer.class);
        PowerMockito.when(EventPollingStoreInitializer.getAggregatedPollingDAO()).thenReturn(
                mockedAggregatedPollingDAO);
        PowerMockito.mockStatic(EventNotificationServiceUtil.class);
        PowerMockito.when(EventNotificationServiceUtil.getEventNotificationGenerator()).thenReturn(
                mockedEventNotificationGenerator);
        PowerMockito.when(mockedAggregatedPollingDAO.getNotificationStatus(Mockito.anyString())).thenReturn(true);

        EventPollingService eventPollingService = new EventPollingService();

        AggregatedPollingResponse aggregatedPollingResponse = eventPollingService.pollEvents(
                EventNotificationTestUtils.getEventPollingDTO());

        Assert.assertEquals(aggregatedPollingResponse.getStatus(), EventNotificationConstants.NOT_FOUND);
    }

    @Test
    public void testPollNotifications() throws OBEventNotificationException {

        mockedAggregatedPollingDAO = Mockito.mock(AggregatedPollingDAO.class);
        mockedEventNotificationGenerator = Mockito.mock(EventNotificationGenerator.class);

        PowerMockito.mockStatic(EventPollingStoreInitializer.class);
        PowerMockito.when(EventPollingStoreInitializer.getAggregatedPollingDAO()).thenReturn(
                mockedAggregatedPollingDAO);
        PowerMockito.mockStatic(EventNotificationServiceUtil.class);
        PowerMockito.when(EventNotificationServiceUtil.getEventNotificationGenerator()).thenReturn(
                mockedEventNotificationGenerator);
        PowerMockito.when(mockedAggregatedPollingDAO.getNotificationsByClientIdAndStatus(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyInt())).thenReturn(
                EventNotificationTestUtils.getSampleSavedTestNotification());
        PowerMockito.when(mockedAggregatedPollingDAO.getNotificationStatus(Mockito.anyString())).thenReturn(true);

        EventPollingService eventPollingService = new EventPollingService();

        AggregatedPollingResponse aggregatedPollingResponse = eventPollingService.pollEvents(
                EventNotificationTestUtils.getEventPollingDTO());

        Assert.assertEquals(aggregatedPollingResponse.getStatus(), EventNotificationConstants.OK);
    }
}
