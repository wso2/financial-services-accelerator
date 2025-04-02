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
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationTestConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.dao.EventSubscriptionDAO;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.model.EventSubscription;
import org.wso2.financial.services.accelerator.event.notifications.service.persistence.EventNotificationStoreInitializer;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationServiceUtil;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationTestUtils;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * This is to test the Event Subscription Service.
 */
public class EventSubscriptionServiceTests {
    private static EventSubscriptionDAO mockedEventSubscriptionDAO;
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
    public void testCreateEventSubscription() throws FSEventNotificationException {

        mockedEventSubscriptionDAO = Mockito.mock(EventSubscriptionDAO.class);
        Mockito.when(mockedEventSubscriptionDAO.storeEventSubscription(any(), any()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscription());
        Mockito.when(mockedEventSubscriptionDAO.storeSubscribedEventTypes(any(), anyString(), any()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventTypes());

        eventStoreInitializerMockedStatic.when(EventNotificationStoreInitializer::getEventSubscriptionDAO)
                .thenReturn(mockedEventSubscriptionDAO);

        EventSubscriptionService eventSubscriptionService = new EventSubscriptionService();
        EventSubscription sampleEventSubscription = EventNotificationTestUtils.getSampleStoredEventSubscription();

        EventSubscription result = eventSubscriptionService.createEventSubscription(
                EventNotificationTestUtils.getSampleEventSubscription());

        Assert.assertNotNull(sampleEventSubscription.getSubscriptionId()); // Check that subscriptionId is not null
        Assert.assertNotNull(sampleEventSubscription.getTimeStamp()); // Check that timeStamp is not null
        Assert.assertEquals(EventNotificationConstants.CREATED, result.getStatus());
        Assert.assertNotNull(result.getEventTypes());
        Assert.assertTrue(result.getEventTypes().contains(EventNotificationTestConstants.
                SAMPLE_NOTIFICATION_EVENT_TYPE_1));
        Assert.assertTrue(result.getEventTypes().contains(EventNotificationTestConstants.
                SAMPLE_NOTIFICATION_EVENT_TYPE_2));

    }

    @Test(expectedExceptions = FSEventNotificationException.class)
    public void testCreateEventSubscriptionWithoutClientID() throws FSEventNotificationException {

        EventSubscriptionService eventSubscriptionService = new EventSubscriptionService();
        EventSubscription sampleEventSubscription = new EventSubscription();

        eventSubscriptionService.createEventSubscription(sampleEventSubscription);
    }

    @Test(expectedExceptions = FSEventNotificationException.class)
    public void testCreateEventSubscriptionWithoutRequestData() throws FSEventNotificationException {

        EventSubscriptionService eventSubscriptionService = new EventSubscriptionService();
        EventSubscription sampleEventSubscription = new EventSubscription();
        sampleEventSubscription.setClientId(EventNotificationTestConstants.SAMPLE_CLIENT_ID);

        eventSubscriptionService.createEventSubscription(sampleEventSubscription);
    }

    @Test(expectedExceptions = FSEventNotificationException.class)
    public void testCreateEventSubscriptionWithoutStatus() throws FSEventNotificationException {

        EventSubscriptionService eventSubscriptionService = new EventSubscriptionService();
        EventSubscription sampleEventSubscription = new EventSubscription();
        sampleEventSubscription.setClientId(EventNotificationTestConstants.SAMPLE_CLIENT_ID);
        sampleEventSubscription.setRequestData(EventNotificationTestConstants.SUBSCRIPTION_PAYLOAD);

        eventSubscriptionService.createEventSubscription(sampleEventSubscription);
    }

    @Test(expectedExceptions = FSEventNotificationException.class)
    public void testCreateEventSubscriptionDaoError() throws FSEventNotificationException {

        mockedEventSubscriptionDAO = Mockito.mock(EventSubscriptionDAO.class);
        Mockito.when(mockedEventSubscriptionDAO.storeEventSubscription(any(), any()))
                .thenThrow(FSEventNotificationException.class);
        Mockito.when(mockedEventSubscriptionDAO.storeSubscribedEventTypes(any(), anyString(), any()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventTypes());

        eventStoreInitializerMockedStatic.when(EventNotificationStoreInitializer::getEventSubscriptionDAO)
                .thenReturn(mockedEventSubscriptionDAO);

        EventSubscriptionService eventSubscriptionService = new EventSubscriptionService();
        eventSubscriptionService.createEventSubscription(EventNotificationTestUtils.getSampleEventSubscription());

    }

    @Test
    public void testGetEventSubscriptionBySubscriptionId() throws FSEventNotificationException {

        mockedEventSubscriptionDAO = Mockito.mock(EventSubscriptionDAO.class);
        Mockito.when(mockedEventSubscriptionDAO.getEventSubscriptionBySubscriptionId(any(), anyString()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscription());

        eventStoreInitializerMockedStatic.when(EventNotificationStoreInitializer::getEventSubscriptionDAO)
                .thenReturn(mockedEventSubscriptionDAO);

        EventSubscriptionService eventSubscriptionService = new EventSubscriptionService();

        EventSubscription result = eventSubscriptionService.
                getEventSubscriptionBySubscriptionId(EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);

        Assert.assertNotNull(result);
    }

    @Test(expectedExceptions = FSEventNotificationException.class)
    public void testGetEventSubscriptionBySubscriptionIdWithoutSubscriptionID() throws FSEventNotificationException {

        mockedEventSubscriptionDAO = Mockito.mock(EventSubscriptionDAO.class);
        Mockito.when(mockedEventSubscriptionDAO.getEventSubscriptionBySubscriptionId(any(), anyString()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscription());

        eventStoreInitializerMockedStatic.when(EventNotificationStoreInitializer::getEventSubscriptionDAO)
                .thenReturn(mockedEventSubscriptionDAO);

        EventSubscriptionService eventSubscriptionService = new EventSubscriptionService();

        EventSubscription result = eventSubscriptionService.getEventSubscriptionBySubscriptionId(null);

        Assert.assertNotNull(result);
    }

    @Test(expectedExceptions = FSEventNotificationException.class)
    public void testGetEventSubscriptionBySubscriptionIdDaoError() throws FSEventNotificationException {

        mockedEventSubscriptionDAO = Mockito.mock(EventSubscriptionDAO.class);
        Mockito.when(mockedEventSubscriptionDAO.getEventSubscriptionBySubscriptionId(any(), anyString()))
                .thenThrow(FSEventNotificationException.class);

        eventStoreInitializerMockedStatic.when(EventNotificationStoreInitializer::getEventSubscriptionDAO)
                .thenReturn(mockedEventSubscriptionDAO);

        EventSubscriptionService eventSubscriptionService = new EventSubscriptionService();

        EventSubscription result = eventSubscriptionService.
                getEventSubscriptionBySubscriptionId(EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);
    }

    @Test
    public void testGetEventSubscriptionsByClientId() throws FSEventNotificationException {

        mockedEventSubscriptionDAO = Mockito.mock(EventSubscriptionDAO.class);
        Mockito.when(mockedEventSubscriptionDAO.getEventSubscriptionsByClientId(any(), anyString()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscriptions());

        eventStoreInitializerMockedStatic.when(EventNotificationStoreInitializer::getEventSubscriptionDAO)
                .thenReturn(mockedEventSubscriptionDAO);

        EventSubscriptionService eventSubscriptionService = new EventSubscriptionService();

        List<EventSubscription> result = eventSubscriptionService.
                getEventSubscriptionsByClientId(EventNotificationTestConstants.SAMPLE_CLIENT_ID);

        Assert.assertEquals(result.size(), 2);
    }

    @Test(expectedExceptions = FSEventNotificationException.class)
    public void testGetEventSubscriptionsByClientIdWithoutClientId() throws FSEventNotificationException {

        mockedEventSubscriptionDAO = Mockito.mock(EventSubscriptionDAO.class);
        Mockito.when(mockedEventSubscriptionDAO.getEventSubscriptionsByClientId(any(), anyString()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscriptions());

        eventStoreInitializerMockedStatic.when(EventNotificationStoreInitializer::getEventSubscriptionDAO)
                .thenReturn(mockedEventSubscriptionDAO);

        EventSubscriptionService eventSubscriptionService = new EventSubscriptionService();

        List<EventSubscription> result = eventSubscriptionService.getEventSubscriptionsByClientId(null);
    }

    @Test(expectedExceptions = FSEventNotificationException.class)
    public void testGetEventSubscriptionsByClientIdDaoError() throws FSEventNotificationException {

        mockedEventSubscriptionDAO = Mockito.mock(EventSubscriptionDAO.class);
        Mockito.when(mockedEventSubscriptionDAO.getEventSubscriptionsByClientId(any(), anyString()))
                .thenThrow(FSEventNotificationException.class);

        eventStoreInitializerMockedStatic.when(EventNotificationStoreInitializer::getEventSubscriptionDAO)
                .thenReturn(mockedEventSubscriptionDAO);

        EventSubscriptionService eventSubscriptionService = new EventSubscriptionService();

        List<EventSubscription> result = eventSubscriptionService.
                getEventSubscriptionsByClientId(EventNotificationTestConstants.SAMPLE_CLIENT_ID);
    }

    @Test
    public void testGetEventSubscriptionsByEventType() throws FSEventNotificationException {

        mockedEventSubscriptionDAO = Mockito.mock(EventSubscriptionDAO.class);
        Mockito.when(mockedEventSubscriptionDAO.getEventSubscriptionsByEventType(any(), anyString()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscriptions());

        eventStoreInitializerMockedStatic.when(EventNotificationStoreInitializer::getEventSubscriptionDAO)
                .thenReturn(mockedEventSubscriptionDAO);

        EventSubscriptionService eventSubscriptionService = new EventSubscriptionService();

        List<EventSubscription> result = eventSubscriptionService.getEventSubscriptionsByEventType(
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1);

        Assert.assertEquals(2, result.size());
    }

    @Test(expectedExceptions = FSEventNotificationException.class)
    public void testGetEventSubscriptionsByEventTypeWithoutEventType() throws FSEventNotificationException {

        mockedEventSubscriptionDAO = Mockito.mock(EventSubscriptionDAO.class);
        Mockito.when(mockedEventSubscriptionDAO.getEventSubscriptionsByEventType(any(), anyString()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscriptions());

        eventStoreInitializerMockedStatic.when(EventNotificationStoreInitializer::getEventSubscriptionDAO)
                .thenReturn(mockedEventSubscriptionDAO);

        EventSubscriptionService eventSubscriptionService = new EventSubscriptionService();

        eventSubscriptionService.getEventSubscriptionsByEventType(null);
    }

    @Test(expectedExceptions = FSEventNotificationException.class)
    public void testGetEventSubscriptionsByEventTypeDaoError() throws FSEventNotificationException {

        mockedEventSubscriptionDAO = Mockito.mock(EventSubscriptionDAO.class);
        Mockito.when(mockedEventSubscriptionDAO.getEventSubscriptionsByEventType(any(), anyString()))
                .thenThrow(FSEventNotificationException.class);

        eventStoreInitializerMockedStatic.when(EventNotificationStoreInitializer::getEventSubscriptionDAO)
                .thenReturn(mockedEventSubscriptionDAO);

        EventSubscriptionService eventSubscriptionService = new EventSubscriptionService();

        eventSubscriptionService.getEventSubscriptionsByEventType(
                EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1);
    }

    @Test
    public void testUpdateEventSubscription() throws FSEventNotificationException {

        mockedEventSubscriptionDAO = Mockito.mock(EventSubscriptionDAO.class);
        Mockito.when(mockedEventSubscriptionDAO.getEventSubscriptionBySubscriptionId(any(), anyString()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscription());
        Mockito.when(mockedEventSubscriptionDAO.updateEventSubscription(any(), any())).thenReturn(true);
        Mockito.when(mockedEventSubscriptionDAO.deleteSubscribedEventTypes(any(), any())).thenReturn(true);
        Mockito.when(mockedEventSubscriptionDAO.storeSubscribedEventTypes(any(), anyString(), any()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventTypes());

        eventStoreInitializerMockedStatic.when(EventNotificationStoreInitializer::getEventSubscriptionDAO)
                .thenReturn(mockedEventSubscriptionDAO);

        EventSubscriptionService eventSubscriptionService = new EventSubscriptionService();

        Boolean result = eventSubscriptionService.updateEventSubscription(EventNotificationTestUtils
                    .getSampleEventSubscriptionToBeUpdated(EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1));

        Assert.assertTrue(result);
    }

    @Test(expectedExceptions = FSEventNotificationException.class)
    public void testUpdateEventSubscriptionWithoutSubscriptionID() throws FSEventNotificationException {

        mockedEventSubscriptionDAO = Mockito.mock(EventSubscriptionDAO.class);
        Mockito.when(mockedEventSubscriptionDAO.getEventSubscriptionBySubscriptionId(any(), anyString()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscription());
        Mockito.when(mockedEventSubscriptionDAO.updateEventSubscription(any(), any())).thenReturn(true);
        Mockito.when(mockedEventSubscriptionDAO.deleteSubscribedEventTypes(any(), any())).thenReturn(true);
        Mockito.when(mockedEventSubscriptionDAO.storeSubscribedEventTypes(any(), anyString(), any()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventTypes());

        eventStoreInitializerMockedStatic.when(EventNotificationStoreInitializer::getEventSubscriptionDAO)
                .thenReturn(mockedEventSubscriptionDAO);

        EventSubscriptionService eventSubscriptionService = new EventSubscriptionService();
        EventSubscription eventSubscription = new EventSubscription();

        eventSubscriptionService.updateEventSubscription(eventSubscription);
    }

    @Test(expectedExceptions = FSEventNotificationException.class)
    public void testUpdateEventSubscriptionWithoutClientId() throws FSEventNotificationException {

        mockedEventSubscriptionDAO = Mockito.mock(EventSubscriptionDAO.class);
        Mockito.when(mockedEventSubscriptionDAO.getEventSubscriptionBySubscriptionId(any(), anyString()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscription());
        Mockito.when(mockedEventSubscriptionDAO.updateEventSubscription(any(), any())).thenReturn(true);
        Mockito.when(mockedEventSubscriptionDAO.deleteSubscribedEventTypes(any(), any())).thenReturn(true);
        Mockito.when(mockedEventSubscriptionDAO.storeSubscribedEventTypes(any(), anyString(), any()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventTypes());

        eventStoreInitializerMockedStatic.when(EventNotificationStoreInitializer::getEventSubscriptionDAO)
                .thenReturn(mockedEventSubscriptionDAO);

        EventSubscriptionService eventSubscriptionService = new EventSubscriptionService();
        EventSubscription eventSubscription = new EventSubscription();
        eventSubscription.setSubscriptionId(EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);

        eventSubscriptionService.updateEventSubscription(eventSubscription);
    }

    @Test
    public void testDeleteEventSubscription() throws FSEventNotificationException {

        mockedEventSubscriptionDAO = Mockito.mock(EventSubscriptionDAO.class);
        Mockito.when(mockedEventSubscriptionDAO.deleteEventSubscription(any(), anyString())).thenReturn(true);

        eventStoreInitializerMockedStatic.when(EventNotificationStoreInitializer::getEventSubscriptionDAO)
                .thenReturn(mockedEventSubscriptionDAO);

        EventSubscriptionService eventSubscriptionService = new EventSubscriptionService();

        Boolean result = eventSubscriptionService.
                deleteEventSubscription(EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);

        Assert.assertTrue(result);
    }
}
