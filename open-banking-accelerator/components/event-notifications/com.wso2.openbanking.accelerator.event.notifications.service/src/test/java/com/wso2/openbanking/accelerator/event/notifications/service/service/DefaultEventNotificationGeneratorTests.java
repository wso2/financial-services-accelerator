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
import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationTestConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.dto.NotificationDTO;
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.model.Notification;
import com.wso2.openbanking.accelerator.event.notifications.service.utils.EventNotificationTestUtils;
import net.minidev.json.parser.ParseException;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
/**
 * Test class for DefaultEventNotificationGenerator.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({OpenBankingConfigParser.class})
public class DefaultEventNotificationGeneratorTests {

    @BeforeMethod
    public void mock() {

       EventNotificationTestUtils.mockConfigParser();
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @Test
    public void testGenerateEventNotificationBody() throws ParseException, OBEventNotificationException {

        NotificationDTO notificationDAO = new NotificationDTO();
        notificationDAO.setClientId(EventNotificationTestConstants.SAMPLE_CLIENT_ID);
        notificationDAO.setNotificationId(EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);

        DefaultEventNotificationGenerator defaultEventNotificationGenerator = new DefaultEventNotificationGenerator();

        Notification notification  = defaultEventNotificationGenerator.generateEventNotificationBody(notificationDAO,
                EventNotificationTestUtils.getSampleNotificationsList());

        Assert.assertEquals(notification.getAud(), EventNotificationTestConstants.SAMPLE_CLIENT_ID);
        Assert.assertEquals(notification.getJti(), EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID);
        Assert.assertNotNull(notification.getEvents().get(EventNotificationTestConstants.
                SAMPLE_NOTIFICATION_EVENT_TYPE_1));
    }
}
