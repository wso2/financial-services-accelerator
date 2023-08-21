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
import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationTestConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.dao.EventPublisherDAO;
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.persistence.EventPublisherStoreInitializer;
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

import static org.mockito.Matchers.anyObject;
import static org.powermock.api.mockito.PowerMockito.when;
/**
 * Tests for Default event notification validations.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({DatabaseUtil.class, EventPublisherStoreInitializer.class, OpenBankingConfigParser.class})
public class EventCreationServiceTests extends PowerMockTestCase {

    private static Connection mockedConnection;

    private static EventPublisherDAO mockedEventPublisherDAO;

    @BeforeClass
    public void initTest() throws Exception {

        mockedConnection = Mockito.mock(Connection.class);
    }

    @BeforeMethod
    public void mock() throws ConsentManagementException, OBEventNotificationException {

        PowerMockito.mockStatic(DatabaseUtil.class);
        PowerMockito.when(DatabaseUtil.getDBConnection()).thenReturn(mockedConnection);
    }

    @Test
    public void testPublishOBEventNotification() throws OBEventNotificationException {

        mockedEventPublisherDAO = Mockito.mock(EventPublisherDAO.class);
        OpenBankingConfigParser openBankingConfigParserMock = Mockito.mock(OpenBankingConfigParser.class);
        Mockito.when(openBankingConfigParserMock.isRealtimeEventNotificationEnabled()).thenReturn(false);
        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(openBankingConfigParserMock);
        PowerMockito.mockStatic(EventPublisherStoreInitializer.class);
        PowerMockito.when(EventPublisherStoreInitializer.getEventCreationDao()).thenReturn(
                mockedEventPublisherDAO);
        when(mockedEventPublisherDAO.persistEventNotification(anyObject(), anyObject(), anyObject())).thenReturn(
                EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);

        EventCreationService eventCreationService = new EventCreationService();
        String notificationId = eventCreationService.publishOBEventNotification(
                EventNotificationTestUtils.getNotificationCreationDTO());

        Assert.assertEquals(EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID, notificationId);
    }

    @Test(expectedExceptions = OBEventNotificationException.class)
    public void testEventPublisherException() throws OBEventNotificationException {

        mockedEventPublisherDAO = Mockito.mock(EventPublisherDAO.class);
        PowerMockito.mockStatic(EventPublisherStoreInitializer.class);
        PowerMockito.when(EventPublisherStoreInitializer.getEventCreationDao()).thenReturn(
                mockedEventPublisherDAO);
        when(mockedEventPublisherDAO.persistEventNotification(anyObject(), anyObject(), anyObject())).thenThrow(
                OBEventNotificationException.class);

        EventCreationService eventCreationService = new EventCreationService();
        String notificationId = eventCreationService.publishOBEventNotification(
                EventNotificationTestUtils.getNotificationCreationDTO());
    }
}
