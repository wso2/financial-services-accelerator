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

package com.wso2.openbanking.accelerator.event.notifications.service.realtime.service;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.HTTPClientUtils;
import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.constants.EventNotificationTestConstants;
import com.wso2.openbanking.accelerator.event.notifications.service.dao.AggregatedPollingDAOImpl;
import com.wso2.openbanking.accelerator.event.notifications.service.persistence.EventPollingStoreInitializer;
import com.wso2.openbanking.accelerator.event.notifications.service.util.EventNotificationServiceUtil;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;

/**
 * Test class for RealtimeEventNotificationSenderService.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({OpenBankingConfigParser.class, HTTPClientUtils.class, CloseableHttpClient.class, HttpPost.class,
        AggregatedPollingDAOImpl.class, EventPollingStoreInitializer.class, CloseableHttpResponse.class,
        StatusLine.class, EventNotificationServiceUtil.class, DefaultRealtimeEventNotificationRequestGenerator.class})
public class RealtimeEventNotificationSenderServiceTests extends PowerMockTestCase {
    private static final int MAX_RETRIES = 1;
    private static final int INITIAL_BACKOFF_TIME_IN_SECONDS = 1;
    private static final String BACKOFF_FUNCTION = "EX";
    private static final int CIRCUIT_BREAKER_OPEN_TIMEOUT_IN_SECONDS = 30;
    private static final int TIMEOUT_IN_SECONDS = 1;

    @BeforeClass
    public void initTest() {
        OpenBankingConfigParser configParser = Mockito.mock(OpenBankingConfigParser.class);
        Mockito.when(configParser.getRealtimeEventNotificationMaxRetries()).thenReturn(MAX_RETRIES);
        Mockito.when(configParser.getRealtimeEventNotificationInitialBackoffTimeInSeconds())
                .thenReturn(INITIAL_BACKOFF_TIME_IN_SECONDS);
        Mockito.when(configParser.getRealtimeEventNotificationBackoffFunction()).thenReturn(BACKOFF_FUNCTION);
        Mockito.when(configParser.getRealtimeEventNotificationCircuitBreakerOpenTimeoutInSeconds())
                .thenReturn(CIRCUIT_BREAKER_OPEN_TIMEOUT_IN_SECONDS);
        Mockito.when(configParser.getRealtimeEventNotificationTimeoutInSeconds()).thenReturn(TIMEOUT_IN_SECONDS);
        PowerMockito.mockStatic(OpenBankingConfigParser.class);
        PowerMockito.when(OpenBankingConfigParser.getInstance()).thenReturn(configParser);
    }

    @Test
    public void testRunBad() throws OpenBankingException, IOException {
        AggregatedPollingDAOImpl aggregatedPollingDAOMock = Mockito.mock(AggregatedPollingDAOImpl.class);
        Mockito.when(aggregatedPollingDAOMock.updateNotificationStatusById(EventNotificationTestConstants
                .SAMPLE_NOTIFICATION_ID, EventNotificationConstants.ACK)).thenReturn(true);
        PowerMockito.mockStatic(EventPollingStoreInitializer.class);
        PowerMockito.when(EventPollingStoreInitializer.getAggregatedPollingDAO()).thenReturn(aggregatedPollingDAOMock);

        RealtimeEventNotificationRequestGenerator mockRequestGenerator =
                Mockito.mock(DefaultRealtimeEventNotificationRequestGenerator.class);
        Map<String, String> mockHeaders = new HashMap<>();
        Mockito.when(mockRequestGenerator.getAdditionalHeaders()).thenReturn(mockHeaders);
        PowerMockito.mockStatic(EventNotificationServiceUtil.class);
        PowerMockito.when(EventNotificationServiceUtil.getRealtimeEventNotificationRequestGenerator())
                .thenReturn(mockRequestGenerator);

        CloseableHttpResponse mockResponse = Mockito.mock(CloseableHttpResponse.class);
        StatusLine mockSL = Mockito.mock(StatusLine.class);
        Mockito.when(mockResponse.getStatusLine()).thenReturn(mockSL);
        Mockito.when(mockSL.getStatusCode()).thenReturn(HttpStatus.SC_BAD_REQUEST);

        CloseableHttpClient httpClientMock = Mockito.mock(CloseableHttpClient.class);
        doReturn(mockResponse).when(httpClientMock).execute(any());
        PowerMockito.mockStatic(HTTPClientUtils.class);
        PowerMockito.when(HTTPClientUtils.getHttpsClientInstance()).thenReturn(httpClientMock);

        new Thread(new RealtimeEventNotificationSenderService(EventNotificationTestConstants.SAMPLE_CALLBACK_URL,
                EventNotificationTestConstants.SAMPLE_NOTIFICATION_PAYLOAD,
                EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID)).start();
    }

    @Test
    public void testRun() throws OpenBankingException, IOException {
        AggregatedPollingDAOImpl aggregatedPollingDAOMock = Mockito.mock(AggregatedPollingDAOImpl.class);
        Mockito.when(aggregatedPollingDAOMock.updateNotificationStatusById(EventNotificationTestConstants
                .SAMPLE_NOTIFICATION_ID, EventNotificationConstants.ACK)).thenReturn(true);
        PowerMockito.mockStatic(EventPollingStoreInitializer.class);
        PowerMockito.when(EventPollingStoreInitializer.getAggregatedPollingDAO()).thenReturn(aggregatedPollingDAOMock);

        RealtimeEventNotificationRequestGenerator mockRequestGenerator =
                Mockito.mock(DefaultRealtimeEventNotificationRequestGenerator.class);
        Map<String, String> mockHeaders = new HashMap<>();
        Mockito.when(mockRequestGenerator.getAdditionalHeaders()).thenReturn(mockHeaders);
        PowerMockito.mockStatic(EventNotificationServiceUtil.class);
        PowerMockito.when(EventNotificationServiceUtil.getRealtimeEventNotificationRequestGenerator())
                .thenReturn(mockRequestGenerator);

        CloseableHttpResponse mockResponse = Mockito.mock(CloseableHttpResponse.class);
        StatusLine mockSL = Mockito.mock(StatusLine.class);
        Mockito.when(mockResponse.getStatusLine()).thenReturn(mockSL);
        Mockito.when(mockSL.getStatusCode()).thenReturn(HttpStatus.SC_OK);

        CloseableHttpClient httpClientMock = Mockito.mock(CloseableHttpClient.class);
        doReturn(mockResponse).when(httpClientMock).execute(any());
        PowerMockito.mockStatic(HTTPClientUtils.class);
        PowerMockito.when(HTTPClientUtils.getHttpsClientInstance()).thenReturn(httpClientMock);

        new Thread(new RealtimeEventNotificationSenderService(EventNotificationTestConstants.SAMPLE_CALLBACK_URL,
                EventNotificationTestConstants.SAMPLE_NOTIFICATION_PAYLOAD,
                EventNotificationTestConstants.SAMPLE_NOTIFICATION_ID)).start();
    }
}
