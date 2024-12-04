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
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationTestConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.dao.EventNotificationDAO;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.persistence.EventNotificationStoreInitializer;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationTestUtils;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;


/**
 * Tests for Default event notification validations.
 */
public class EventCreationServiceTests {

    private static EventNotificationDAO mockedEventNotificationDAO;
    private MockedStatic<FinancialServicesConfigParser> configParserMockedStatic;
    private MockedStatic<EventNotificationStoreInitializer> eventStoreInitializerMockedStatic;
    private MockedStatic<DatabaseUtils> databaseUtilMockedStatic;

    @BeforeClass
    public void initTest() {

        Connection mockedConnection = Mockito.mock(Connection.class);

        configParserMockedStatic = Mockito.mockStatic(FinancialServicesConfigParser.class);
        eventStoreInitializerMockedStatic = Mockito.mockStatic(EventNotificationStoreInitializer.class);
        databaseUtilMockedStatic = Mockito.mockStatic(DatabaseUtils.class);

        Map<String, Object> configs = new HashMap<String, Object>();
        configs.put(FinancialServicesConstants.REALTIME_EVENT_NOTIFICATION_ENABLED, false);
        FinancialServicesConfigParser configParserMock = Mockito.mock(FinancialServicesConfigParser.class);
        Mockito.doReturn(configs).when(configParserMock).getConfiguration();
        configParserMockedStatic.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

        databaseUtilMockedStatic.when(DatabaseUtils::getDBConnection).thenReturn(mockedConnection);

    }

    @AfterClass
    public void tearDown() {
        configParserMockedStatic.close();
        eventStoreInitializerMockedStatic.close();
        databaseUtilMockedStatic.close();
    }

    @Test
    public void testPublishOBEventNotification() throws FSEventNotificationException {

        mockedEventNotificationDAO = Mockito.mock(EventNotificationDAO.class);
        Mockito.when(mockedEventNotificationDAO.persistEventNotification(any(), any(), any())).thenReturn(
                EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);

        eventStoreInitializerMockedStatic.when(EventNotificationStoreInitializer::getEventNotificationDAO)
                .thenReturn(mockedEventNotificationDAO);

        EventCreationService eventCreationService = new EventCreationService();
        String notificationId = eventCreationService.publishEventNotification(
                EventNotificationTestUtils.getNotificationCreationDTO());

        Assert.assertEquals(EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID, notificationId);
    }

    @Test(expectedExceptions = FSEventNotificationException.class)
    public void testEventPublisherException() throws FSEventNotificationException {

        mockedEventNotificationDAO = Mockito.mock(EventNotificationDAO.class);
        Mockito.when(mockedEventNotificationDAO.persistEventNotification(any(), any(), any())).thenThrow(
                FSEventNotificationException.class);

        eventStoreInitializerMockedStatic.when(EventNotificationStoreInitializer::getEventNotificationDAO)
                .thenReturn(mockedEventNotificationDAO);

        EventCreationService eventCreationService = new EventCreationService();
        String notificationId = eventCreationService.publishEventNotification(
                EventNotificationTestUtils.getNotificationCreationDTO());
        Assert.assertNotNull(notificationId);
    }
}
