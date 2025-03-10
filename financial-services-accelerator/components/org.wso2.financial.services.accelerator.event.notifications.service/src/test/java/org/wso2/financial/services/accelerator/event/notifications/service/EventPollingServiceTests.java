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

package org.wso2.financial.services.accelerator.event.notifications.service;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.util.DatabaseUtils;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.dao.EventNotificationDAO;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.model.AggregatedPollingResponse;
import org.wso2.financial.services.accelerator.event.notifications.service.persistence.EventNotificationStoreInitializer;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationServiceUtil;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationTestUtils;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Test class for EventPollingService.
 */
public class EventPollingServiceTests {
    private static EventNotificationDAO mockedEventNotificationDao;
    private MockedStatic<FinancialServicesConfigParser> configParserMockedStatic;
    private MockedStatic<EventNotificationStoreInitializer> eventStoreInitializerMockedStatic;
    private MockedStatic<DatabaseUtils> databaseUtilMockedStatic;
    private MockedStatic<EventNotificationServiceUtil> eventNotificationUtilMockedStatic;

    @BeforeClass
    public void initTest() {
        Connection mockedConnection = Mockito.mock(Connection.class);
        EventNotificationGenerator mockedEventNotificationGenerator = Mockito.mock(EventNotificationGenerator.class);

        configParserMockedStatic = Mockito.mockStatic(FinancialServicesConfigParser.class);
        eventStoreInitializerMockedStatic = Mockito.mockStatic(EventNotificationStoreInitializer.class);
        databaseUtilMockedStatic = Mockito.mockStatic(DatabaseUtils.class);
        eventNotificationUtilMockedStatic = Mockito.mockStatic(EventNotificationServiceUtil.class);

        Map<String, Object> configs = new HashMap<String, Object>();
        configs.put(FinancialServicesConstants.REALTIME_EVENT_NOTIFICATION_ENABLED, false);
        FinancialServicesConfigParser configParserMock = Mockito.mock(FinancialServicesConfigParser.class);
        Mockito.doReturn(configs).when(configParserMock).getConfiguration();
        configParserMockedStatic.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        eventNotificationUtilMockedStatic.when(EventNotificationServiceUtil::getEventNotificationGenerator)
                .thenReturn(mockedEventNotificationGenerator);
        databaseUtilMockedStatic.when(DatabaseUtils::getDBConnection).thenReturn(mockedConnection);
    }

    @AfterClass
    public void tearDown() {
        configParserMockedStatic.close();
        eventStoreInitializerMockedStatic.close();
        databaseUtilMockedStatic.close();
        eventNotificationUtilMockedStatic.close();
    }

    @Test
    public void testPollEventsNoNotifications() throws FSEventNotificationException {

        mockedEventNotificationDao = Mockito.mock(EventNotificationDAO.class);
        Mockito.when(mockedEventNotificationDao.getNotificationStatus(any(), anyString())).thenReturn(true);

        eventStoreInitializerMockedStatic.when(EventNotificationStoreInitializer::getEventNotificationDAO)
                .thenReturn(mockedEventNotificationDao);

        EventPollingService eventPollingService = new EventPollingService();

        AggregatedPollingResponse aggregatedPollingResponse = eventPollingService.pollEvents(
                EventNotificationTestUtils.getEventPollingDTO());

        Assert.assertEquals(aggregatedPollingResponse.getStatus(), EventNotificationConstants.NOT_FOUND);
    }

    @Test
    public void testPollNotifications() throws FSEventNotificationException {

        mockedEventNotificationDao = Mockito.mock(EventNotificationDAO.class);
        Mockito.when(mockedEventNotificationDao.getNotificationStatus(any(), anyString())).thenReturn(true);
        Mockito.when(mockedEventNotificationDao.getNotificationsByClientIdAndStatus(any(), anyString(), anyString(),
                anyInt())).thenReturn(EventNotificationTestUtils.getSampleSavedTestNotification());

        eventStoreInitializerMockedStatic.when(EventNotificationStoreInitializer::getEventNotificationDAO)
                .thenReturn(mockedEventNotificationDao);

        EventPollingService eventPollingService = new EventPollingService();

        AggregatedPollingResponse aggregatedPollingResponse = eventPollingService.pollEvents(
                EventNotificationTestUtils.getEventPollingDTO());

        Assert.assertEquals(aggregatedPollingResponse.getStatus(), EventNotificationConstants.OK);
    }
}
