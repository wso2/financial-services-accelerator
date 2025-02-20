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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationTestConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.model.Notification;
import org.wso2.financial.services.accelerator.event.notifications.service.model.NotificationResponse;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationTestUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Test class for DefaultEventNotificationGenerator.
 */
public class DefaultEventNotificationGeneratorTests {

    private MockedStatic<FinancialServicesConfigParser> configParserMockedStatic;

    @BeforeMethod
    public void mock() {

        configParserMockedStatic = Mockito.mockStatic(FinancialServicesConfigParser.class);

        Map<String, Object> configs = new HashMap<String, Object>();
        configs.put(FinancialServicesConstants.REALTIME_EVENT_NOTIFICATION_ENABLED, false);
        FinancialServicesConfigParser configParserMock = Mockito.mock(FinancialServicesConfigParser.class);
        Mockito.doReturn(configs).when(configParserMock).getConfiguration();
        configParserMockedStatic.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);
    }

    @AfterClass
    public void tearDown() {
        configParserMockedStatic.close();
    }

    @Test
    public void testGenerateEventNotificationBody() throws FSEventNotificationException {

        Notification notification = new Notification();
        notification.setClientId(EventNotificationTestConstants.SAMPLE_CLIENT_ID);
        notification.setNotificationId(EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);

        DefaultEventNotificationGenerator defaultEventNotificationGenerator = new DefaultEventNotificationGenerator();

        NotificationResponse notificationResponse  = defaultEventNotificationGenerator
                .generateEventNotificationBody(notification, EventNotificationTestUtils.getSampleNotificationsList());

        Assert.assertEquals(notificationResponse.getAud(), EventNotificationTestConstants.SAMPLE_CLIENT_ID);
        Assert.assertEquals(notificationResponse.getJti(), EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);
        Assert.assertNotNull(notificationResponse.getEvents().get(EventNotificationTestConstants.
                SAMPLE_NOTIFICATION_EVENT_TYPE_1));
    }
}
