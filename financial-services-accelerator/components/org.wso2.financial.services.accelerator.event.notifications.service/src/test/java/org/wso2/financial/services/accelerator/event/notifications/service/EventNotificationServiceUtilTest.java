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

import org.json.JSONObject;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationTestConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.realtime.service.RealtimeEventNotificationRequestGenerator;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationServiceUtil;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;

/**
 * Test class for EventNotificationServiceUtil.
 */
public class EventNotificationServiceUtilTest {

    private MockedStatic<FinancialServicesConfigParser> configParserMockedStatic;

    @BeforeClass
    public void initTest() {
        configParserMockedStatic = Mockito.mockStatic(FinancialServicesConfigParser.class);

        Map<String, Object> configs = new HashMap<String, Object>();
        configs.put(FinancialServicesConstants.REALTIME_EVENT_NOTIFICATION_ENABLED, false);
        configs.put(FinancialServicesConstants.REQUIRE_SUBSCRIPTION_TO_POLL, false);
        FinancialServicesConfigParser configParserMock = Mockito.mock(FinancialServicesConfigParser.class);
        Mockito.doReturn(configs).when(configParserMock).getConfiguration();
        Mockito.doReturn("org.wso2.financial.services.accelerator.event.notifications.service" +
                ".DefaultEventNotificationGenerator").when(configParserMock).getEventNotificationGenerator();
        Mockito.doReturn("org.wso2.financial.services.accelerator.event.notifications.service." +
                        "realtime.service.DefaultRealtimeEventNotificationRequestGenerator")
                .when(configParserMock).getRealtimeEventNotificationRequestGenerator();
        configParserMockedStatic.when(FinancialServicesConfigParser::getInstance).thenReturn(configParserMock);

    }

    @AfterClass
    public void tearDown() {
        configParserMockedStatic.close();
    }

    @Test
    public void testGetEventNotificationGenerator() {
        EventNotificationGenerator eventNotificationGenerator = EventNotificationServiceUtil
                .getEventNotificationGenerator();
        Assert.assertNotNull(eventNotificationGenerator);
    }

    @Test
    public void testGetRealtimeEventNotificationRequestGenerator() {
        RealtimeEventNotificationRequestGenerator eventNotificationGenerator = EventNotificationServiceUtil
                .getRealtimeEventNotificationRequestGenerator();
        Assert.assertNotNull(eventNotificationGenerator);
    }

    @Test
    public void testGetErrorDTO() {
        JSONObject error = EventNotificationServiceUtil.getErrorDTO("Invalid request", "Invalid request payload");
        Assert.assertTrue(error.has(EventNotificationConstants.ERROR_FIELD));
        Assert.assertTrue(error.has(EventNotificationConstants.ERROR_DESCRIPTION_FIELD));
    }

    @Test
    public void testIsSubscriptionExist() throws Exception {
        EventSubscriptionService eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);
        Mockito.when(eventSubscriptionService.getEventSubscriptionsByClientId(any()))
                .thenReturn(EventNotificationTestUtils.getSampleStoredEventSubscriptions());

        Assert.assertTrue(EventNotificationServiceUtil.isSubscriptionExist(eventSubscriptionService,
                EventNotificationTestConstants.SAMPLE_CLIENT_ID));
    }
}
