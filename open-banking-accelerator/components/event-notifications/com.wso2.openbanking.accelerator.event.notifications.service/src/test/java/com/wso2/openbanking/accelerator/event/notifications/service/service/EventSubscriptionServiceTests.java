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
import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationTestConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.dao.EventSubscriptionDAO;
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.model.EventSubscription;
import com.wso2.openbanking.accelerator.event.notifications.service.persistence.EventSubscriptionStoreInitializer;
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
import java.util.List;

/**
 * This is to test the Event Subscription Service.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({DatabaseUtil.class, EventSubscriptionStoreInitializer.class, EventNotificationServiceUtil.class,
        OpenBankingConfigParser.class})
public class EventSubscriptionServiceTests extends PowerMockTestCase {
    private static Connection mockedConnection;
    private static EventSubscriptionDAO mockedEventSubscriptionDAO;

    @BeforeClass
    public void initTest() {

        mockedConnection = Mockito.mock(Connection.class);
    }

    @BeforeMethod
    public void mock() throws ConsentManagementException, OBEventNotificationException {

        PowerMockito.mockStatic(DatabaseUtil.class);
        PowerMockito.when(DatabaseUtil.getDBConnection()).thenReturn(mockedConnection);
        EventNotificationTestUtils.mockConfigParser();

        mockedEventSubscriptionDAO = Mockito.mock(EventSubscriptionDAO.class);

        PowerMockito.mockStatic(EventSubscriptionStoreInitializer.class);
        PowerMockito.when(EventSubscriptionStoreInitializer.getEventSubscriptionDao()).
                thenReturn(mockedEventSubscriptionDAO);
    }

    @Test
    public void testCreateEventSubscription() throws OBEventNotificationException {
        PowerMockito.when(mockedEventSubscriptionDAO.storeEventSubscription(Mockito.anyObject(), Mockito.any())).
                thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscription());
        PowerMockito.when(mockedEventSubscriptionDAO.storeSubscribedEventTypes(Mockito.anyObject(), Mockito.anyString(),
                Mockito.any())).thenReturn(EventNotificationTestUtils.getSampleStoredEventTypes());

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

    @Test
    public void testGetEventSubscriptionBySubscriptionId() throws OBEventNotificationException {
        PowerMockito.when(mockedEventSubscriptionDAO.getEventSubscriptionBySubscriptionId(Mockito.anyObject(),
                Mockito.anyString())).thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscription());

        EventSubscriptionService eventSubscriptionService = new EventSubscriptionService();

        EventSubscription result = eventSubscriptionService.
                getEventSubscriptionBySubscriptionId(EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);

        Assert.assertNotNull(result);
    }

    @Test
    public void testGetEventSubscriptionsByClientId() throws OBEventNotificationException {
        PowerMockito.when(mockedEventSubscriptionDAO.getEventSubscriptionsByClientId(Mockito.anyObject(),
                Mockito.anyString())).thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscriptions());

        EventSubscriptionService eventSubscriptionService = new EventSubscriptionService();

        List<EventSubscription> result = eventSubscriptionService.
                getEventSubscriptionsByClientId(EventNotificationTestConstants.SAMPLE_CLIENT_ID);

        Assert.assertEquals(result.size(), 2);
    }

    @Test
    public void testGetEventSubscriptionsByClientIdAndEventType() throws OBEventNotificationException {
        PowerMockito.when(mockedEventSubscriptionDAO.getEventSubscriptionsByEventType(Mockito.anyObject(),
                        Mockito.anyString())).
                thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscriptions());

        EventSubscriptionService eventSubscriptionService = new EventSubscriptionService();

        List<EventSubscription> result = eventSubscriptionService.
                getEventSubscriptionsByClientIdAndEventType(
                        EventNotificationTestConstants.SAMPLE_NOTIFICATION_EVENT_TYPE_1);

        Assert.assertEquals(2, result.size());
    }

    @Test
    public void testUpdateEventSubscription() throws OBEventNotificationException {
        PowerMockito.when(mockedEventSubscriptionDAO.getEventSubscriptionBySubscriptionId(Mockito.anyObject(),
                Mockito.anyString())).thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscription());
        PowerMockito.when(mockedEventSubscriptionDAO.updateEventSubscription(Mockito.anyObject(), Mockito.any())).
                thenReturn(true);
        PowerMockito.when(mockedEventSubscriptionDAO.deleteSubscribedEventTypes(Mockito.anyObject(),
                Mockito.anyString())).thenReturn(true);
        PowerMockito.when(mockedEventSubscriptionDAO.storeSubscribedEventTypes(Mockito.anyObject(), Mockito.anyString(),
                Mockito.any())).thenReturn(EventNotificationTestUtils.getSampleStoredEventTypes());

        EventSubscriptionService eventSubscriptionService = new EventSubscriptionService();

        Boolean result = eventSubscriptionService.
                updateEventSubscription(EventNotificationTestUtils.getSampleEventSubscriptionToBeUpdated());

        Assert.assertTrue(result);
    }

    @Test
    public void testDeleteEventSubscription() throws OBEventNotificationException {
        PowerMockito.when(mockedEventSubscriptionDAO.deleteEventSubscription(Mockito.anyObject(), Mockito.anyString())).
                thenReturn(true);

        EventSubscriptionService eventSubscriptionService = new EventSubscriptionService();

        Boolean result = eventSubscriptionService.
                deleteEventSubscription(EventNotificationTestConstants.SAMPLE_SUBSCRIPTION_ID_1);

        Assert.assertTrue(result);
    }
}
