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

package org.wso2.financial.services.accelerator.event.notifications.service.realtime;

import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.util.HTTPClientUtils;
import org.wso2.financial.services.accelerator.event.notifications.service.DefaultEventNotificationGenerator;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.constants.EventNotificationTestConstants;
import org.wso2.financial.services.accelerator.event.notifications.service.dao.EventNotificationDAOImpl;
import org.wso2.financial.services.accelerator.event.notifications.service.exception.FSEventNotificationException;
import org.wso2.financial.services.accelerator.event.notifications.service.internal.EventNotificationDataHolder;
import org.wso2.financial.services.accelerator.event.notifications.service.model.RealtimeEventNotification;
import org.wso2.financial.services.accelerator.event.notifications.service.persistence.EventNotificationStoreInitializer;
import org.wso2.financial.services.accelerator.event.notifications.service.realtime.service.DefaultRealtimeEventNotificationRequestGenerator;
import org.wso2.financial.services.accelerator.event.notifications.service.realtime.service.RealtimeEventNotificationSenderService;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationServiceUtil;
import org.wso2.financial.services.accelerator.event.notifications.service.util.EventNotificationTestUtils;

import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

/**
 * Test class for RealtimeEventNotificationSenderService.
 */
public class RealtimeEventNotificationSenderServiceTests {

    private static final int MAX_RETRIES = 1;
    private static final int INITIAL_BACKOFF_TIME_IN_SECONDS = 1;
    private static final String BACKOFF_FUNCTION = "EX";
    private static final int CIRCUIT_BREAKER_OPEN_TIMEOUT_IN_SECONDS = 30;
    private static final int TIMEOUT_IN_SECONDS = 1;

    MockedStatic<EventNotificationServiceUtil> notificationServiceUtilMockedStatic;
    MockedStatic<EventNotificationDataHolder> eventNotificationDataHolderMockedStatic;
    MockedStatic<EventNotificationStoreInitializer> notificationStoreInitializerMockedStatic;
    MockedStatic<FinancialServicesConfigParser> financialServicesConfigParserMockedStatic;
    MockedStatic<HTTPClientUtils> httpClientUtilsMockedStatic;
    DefaultEventNotificationGenerator mockedEventNotificationGenerator;
    DefaultRealtimeEventNotificationRequestGenerator mockedRealtimeEventNotificationRequestGenerator;
    LinkedBlockingQueue<RealtimeEventNotification> eventQueue;

    @BeforeClass
    public void initTest() throws FSEventNotificationException, IOException {

        mockedEventNotificationGenerator = Mockito.mock(DefaultEventNotificationGenerator.class);
        mockedRealtimeEventNotificationRequestGenerator =
                Mockito.mock(DefaultRealtimeEventNotificationRequestGenerator.class);
        Mockito.doReturn(new HashMap<>()).when(mockedRealtimeEventNotificationRequestGenerator).getAdditionalHeaders();

        notificationServiceUtilMockedStatic = Mockito.mockStatic(EventNotificationServiceUtil.class);
        notificationServiceUtilMockedStatic.when(EventNotificationServiceUtil::getEventNotificationGenerator)
                .thenReturn(mockedEventNotificationGenerator);
        notificationServiceUtilMockedStatic.when(EventNotificationServiceUtil::getEventNotificationGenerator)
                .thenReturn(mockedRealtimeEventNotificationRequestGenerator);

        eventQueue = new LinkedBlockingQueue<>();
        EventNotificationDataHolder eventNotificationDataHolder = Mockito.mock(EventNotificationDataHolder.class);
        eventNotificationDataHolder.setRealtimeEventNotificationQueue(eventQueue);

        eventNotificationDataHolderMockedStatic = Mockito.mockStatic(EventNotificationDataHolder.class);
        eventNotificationDataHolderMockedStatic.when(EventNotificationDataHolder::getInstance)
                .thenReturn(eventNotificationDataHolder);

        Connection connection = Mockito.mock(Connection.class);
        EventNotificationDAOImpl mockDAOImpl = Mockito.mock(EventNotificationDAOImpl.class);
        Mockito.doReturn(EventNotificationTestUtils.getNotificationList()).when(mockDAOImpl).getNotificationsByStatus(
                connection, EventNotificationConstants.OPEN);
        Mockito.doReturn(true).when(mockDAOImpl).updateNotificationStatusById(connection,
                EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID, EventNotificationConstants.ACK);

        notificationStoreInitializerMockedStatic = Mockito.mockStatic(EventNotificationStoreInitializer.class);
        notificationStoreInitializerMockedStatic.when(EventNotificationStoreInitializer::getEventNotificationDAO)
                .thenReturn(mockDAOImpl);

        FinancialServicesConfigParser configParser = Mockito.mock(FinancialServicesConfigParser.class);
        Mockito.when(configParser.getRealtimeEventNotificationMaxRetries()).thenReturn(MAX_RETRIES);
        Mockito.when(configParser.getRealtimeEventNotificationInitialBackoffTimeInSeconds())
                .thenReturn(INITIAL_BACKOFF_TIME_IN_SECONDS);
        Mockito.when(configParser.getRealtimeEventNotificationBackoffFunction()).thenReturn(BACKOFF_FUNCTION);
        Mockito.when(configParser.getRealtimeEventNotificationCircuitBreakerOpenTimeoutInSeconds())
                .thenReturn(CIRCUIT_BREAKER_OPEN_TIMEOUT_IN_SECONDS);
        Mockito.when(configParser.getRealtimeEventNotificationTimeoutInSeconds()).thenReturn(TIMEOUT_IN_SECONDS);

        financialServicesConfigParserMockedStatic = Mockito.mockStatic(FinancialServicesConfigParser.class);
        financialServicesConfigParserMockedStatic.when(FinancialServicesConfigParser::getInstance)
                .thenReturn(configParser);

        CloseableHttpResponse mockResponse = Mockito.mock(CloseableHttpResponse.class);
        StatusLine mockSL = Mockito.mock(StatusLine.class);
        Mockito.when(mockResponse.getStatusLine()).thenReturn(mockSL);
        Mockito.when(mockSL.getStatusCode()).thenReturn(HttpStatus.SC_BAD_REQUEST);

        CloseableHttpClient httpClientMock = Mockito.mock(CloseableHttpClient.class);
        Mockito.doReturn(mockResponse).when(httpClientMock).execute(any());
        httpClientUtilsMockedStatic = Mockito.mockStatic(HTTPClientUtils.class);
        httpClientUtilsMockedStatic.when(() -> HTTPClientUtils.getHttpsClient(anyInt(), anyInt()))
                .thenReturn(httpClientMock);

    }

    @AfterClass
    public void tearDown() {
        notificationServiceUtilMockedStatic.close();
        eventNotificationDataHolderMockedStatic.close();
        notificationStoreInitializerMockedStatic.close();
        financialServicesConfigParserMockedStatic.close();
    }

    @Test
    public void testRunBad() {

        new Thread(new RealtimeEventNotificationSenderService(EventNotificationTestConstants.SAMPLE_CALLBACK_URL,
                EventNotificationTestConstants.SAMPLE_NOTIFICATION_PAYLOAD,
                EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID)).start();
    }

    @Test
    public void testRun() {

        new Thread(new RealtimeEventNotificationSenderService(EventNotificationTestConstants.SAMPLE_CALLBACK_URL,
                EventNotificationTestConstants.SAMPLE_NOTIFICATION_PAYLOAD,
                EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID)).start();
    }
}
