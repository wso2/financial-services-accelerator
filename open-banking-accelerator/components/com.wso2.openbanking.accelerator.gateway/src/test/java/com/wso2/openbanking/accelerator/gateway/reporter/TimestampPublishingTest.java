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

package com.wso2.openbanking.accelerator.gateway.reporter;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.am.analytics.publisher.exception.MetricReportingException;
import org.wso2.am.analytics.publisher.reporter.MetricEventBuilder;
import org.wso2.am.analytics.publisher.reporter.cloud.DefaultResponseMetricEventBuilder;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Test class for timestamp publishing.
 */
public class TimestampPublishingTest {

    private static final String CORRELATION_ID = "correlationId";
    private static final String REQUEST_TIMESTAMP = "requestTimestamp";
    private static final String BACKEND_LATENCY = "backendLatency";
    private static final String REQUEST_MEDIATION_LATENCY = "requestMediationLatency";
    private static final String RESPONSE_LATENCY = "responseLatency";
    private static final String RESPONSE_MEDIATION_LATENCY = "responseMediationLatency";
    private final Timestamp currentTimestamp = new Timestamp(new Date().getTime());

    @Test
    public void createMetricReporter() throws MetricReportingException {

        MetricEventBuilder metricEventBuilder = new MockEventBuilder();

        metricEventBuilder.addAttribute(CORRELATION_ID, UUID.randomUUID());
        metricEventBuilder.addAttribute(REQUEST_TIMESTAMP, currentTimestamp);
        metricEventBuilder.addAttribute(BACKEND_LATENCY, 10);
        metricEventBuilder.addAttribute(REQUEST_MEDIATION_LATENCY, 20);
        metricEventBuilder.addAttribute(RESPONSE_LATENCY, 30);
        metricEventBuilder.addAttribute(RESPONSE_MEDIATION_LATENCY, 40);

        OBTimestampPublisher obTimestampPublisher = new MockOBTimestampPublisher(metricEventBuilder);
        obTimestampPublisher.run();
    }

    private void validateData(Map<String, Object> analyticsData) {
        Assert.assertTrue(analyticsData.get(REQUEST_TIMESTAMP).equals(currentTimestamp));
        Assert.assertTrue(analyticsData.get(BACKEND_LATENCY).equals(10));
        Assert.assertTrue(analyticsData.get(REQUEST_MEDIATION_LATENCY).equals(20));
        Assert.assertTrue(analyticsData.get(RESPONSE_LATENCY).equals(30));
        Assert.assertTrue(analyticsData.get(RESPONSE_MEDIATION_LATENCY).equals(40));
    }

    class MockEventBuilder extends DefaultResponseMetricEventBuilder {
        @Override
        public boolean validate() throws MetricReportingException {
            return true;
        }

        @Override
        protected Map<String, Object> buildEvent() {
            this.eventMap.put("eventType", "response");
            String userAgentHeader = (String) this.eventMap.remove("userAgentHeader");
            return this.eventMap;
        }
    }

    class MockOBTimestampPublisher extends OBTimestampPublisher {

        public MockOBTimestampPublisher(MetricEventBuilder builder) {

            super(builder);
        }

        @Override
        protected void publishLatencyData(Map<String, Object> analyticsData) {
            // validate
            validateData(analyticsData);
        }
    }
}
