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
import com.wso2.openbanking.accelerator.event.notifications.service.dao.AggregatedPollingDAO;
import com.wso2.openbanking.accelerator.event.notifications.service.exceptions.OBEventNotificationException;
import com.wso2.openbanking.accelerator.event.notifications.service.internal.EventNotificationComponent;
import com.wso2.openbanking.accelerator.event.notifications.service.persistence.EventPollingStoreInitializer;
import com.wso2.openbanking.accelerator.event.notifications.service.util.EventNotificationServiceUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Map;

/**
 * This method is used to send the HTTP requests to the TPP provided callback URL.
 * Exponential backoff and Circuit breaker based retry policy is used to retry failed POST requests.
 */
public class RealtimeEventNotificationSenderService implements Runnable {

    private static final Log log = LogFactory.getLog(EventNotificationComponent.class);

    private static final OpenBankingConfigParser configParser = OpenBankingConfigParser.getInstance();
    private static final int MAX_RETRIES = configParser.getRealtimeEventNotificationMaxRetries();
    private static final int INITIAL_BACKOFF_TIME_IN_SECONDS =
            configParser.getRealtimeEventNotificationInitialBackoffTimeInSeconds();
    private static final String BACKOFF_FUNCTION = configParser.getRealtimeEventNotificationBackoffFunction();
    private static final int CIRCUIT_BREAKER_OPEN_TIMEOUT_IN_SECONDS =
            configParser.getRealtimeEventNotificationCircuitBreakerOpenTimeoutInSeconds();
    private static final int TIMEOUT_IN_SECONDS = configParser.getRealtimeEventNotificationTimeoutInSeconds();

    private RealtimeEventNotificationRequestGenerator httpRequestGenerator;
    private String notificationId;
    private String callbackUrl;
    private String payloadJson;
    private int maxRetryCount = configParser.getRealtimeEventNotificationMaxRetries() + 1;

    public RealtimeEventNotificationSenderService(String callbackUrl, String payloadJson,
                                                  String notificationId) {

        this.httpRequestGenerator = EventNotificationServiceUtil.getRealtimeEventNotificationRequestGenerator();
        this.notificationId = notificationId;
        this.callbackUrl = callbackUrl;
        this.payloadJson = payloadJson;
    }

    public void run() {
        try {
            postWithRetry();
        } catch (OBEventNotificationException e) {
            log.error("Failed to send the Real-time event notification with notificationId: "
                    + notificationId, e);
        }
    }

    /**
     * This method is used to send the HTTP requests to the TPP provided callback URL.
     * Exponential backoff and Circuit breaker based retry policy is used to retry failed POST requests.
     *
     * @throws OBEventNotificationException
     */
    private void postWithRetry() throws OBEventNotificationException {
        AggregatedPollingDAO aggregatedPollingDAO =
                EventPollingStoreInitializer.getAggregatedPollingDAO();
        int retryCount = 0;
        long backoffTimeMs = INITIAL_BACKOFF_TIME_IN_SECONDS * 1000L;
        boolean circuitBreakerOpen = false;
        LocalTime startTime = LocalTime.now();

        while (retryCount <= MAX_RETRIES && !circuitBreakerOpen) {
            try {
                // This if closure will execute only if the initial POST request is failed.
                // This includes the retry policy and will execute according to the configurations.
                if (retryCount > 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("HTTP request Retry #" + retryCount + " - waiting for "
                                + backoffTimeMs + " ms before trying again");
                    }
                    Thread.sleep(backoffTimeMs);

                    switch (BACKOFF_FUNCTION) {
                        case "CONSTANT":
                            // Backoff time will not be changed
                            // Retries will happen in constant time frames
                            break;
                        case "LINEAR":
                            // Backoff time will be doubled after each retry
                            // nextWaitingTime = 2 x previousWaitingTime
                            backoffTimeMs *= 2;
                            break;
                        case "EX":
                            // Backoff time will be increased exponentially
                            // nextWaitingTime = startWaitingTime x e^(retryCount)
                            backoffTimeMs = (long)
                                    (INITIAL_BACKOFF_TIME_IN_SECONDS
                                            * 1000 * Math.exp(retryCount));
                            break;
                        default:
                            log.error("Invalid backoff function for the realtime event notification retry policy: "
                                    + BACKOFF_FUNCTION);
                            throw new IllegalArgumentException(
                                    "Invalid backoff function for the realtime event notification retry policy: "
                                            + BACKOFF_FUNCTION);
                    }
                }

                HttpPost httpPost = new HttpPost(URI.create(callbackUrl));

                for (Map.Entry<String, String> entry : httpRequestGenerator.getAdditionalHeaders().entrySet()) {
                    String headerName = entry.getKey();
                    String headerValue = entry.getValue();
                    httpPost.setHeader(headerName, headerValue);
                }

                httpPost.setEntity(new StringEntity(payloadJson, ContentType.APPLICATION_JSON));
                RequestConfig requestConfig = RequestConfig.custom()
                        .setConnectTimeout(TIMEOUT_IN_SECONDS * 1000)
                        .setConnectionRequestTimeout(TIMEOUT_IN_SECONDS * 1000)
                        .setSocketTimeout(TIMEOUT_IN_SECONDS * 1000)
                        .build();
                httpPost.setConfig(requestConfig);

                try (CloseableHttpResponse response = HTTPClientUtils.getHttpsClient().execute(httpPost)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == HttpStatus.SC_ACCEPTED) {
                        if (log.isDebugEnabled()) {
                            log.debug("Real-time event notification with notificationId: " + notificationId
                                    + " sent successfully");
                        }
                        aggregatedPollingDAO.updateNotificationStatusById(notificationId,
                                EventNotificationConstants.ACK);
                        return;
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Real-time event notification with notificationId: " + notificationId
                                    + " sent failed with status code: " + statusCode);
                        }
                    }
                } catch (OpenBankingException e) {
                    log.error("Failed to initialize the HTTP client for the realtime event notification", e);
                }
            } catch (IOException | InterruptedException e) {
                log.error("Real-time event notification with notificationId: " + notificationId
                        + " sent failed" + e);
            }

            // Circuit breaker will be opened if the retrying time exceeds the configured circuit breaker timeout.
            if (Duration.between(startTime, LocalTime.now()).toMillis()
                    > CIRCUIT_BREAKER_OPEN_TIMEOUT_IN_SECONDS * 1000) {
                circuitBreakerOpen = true;
                if (log.isDebugEnabled()) {
                    log.debug("Circuit breaker open for the realtime event notification with notificationId: "
                            + notificationId);
                }
            }
            retryCount++;
        }

        // If the circuit breaker is opened or the maximum retry count is exceeded,
        // the notification status will be updated as ERROR.
        aggregatedPollingDAO.updateNotificationStatusById(notificationId, EventNotificationConstants.ERROR);

    }
}
