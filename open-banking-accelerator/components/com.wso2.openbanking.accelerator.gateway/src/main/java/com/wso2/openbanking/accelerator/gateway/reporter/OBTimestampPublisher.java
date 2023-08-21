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

import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.data.publisher.common.util.OBDataPublisherUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.am.analytics.publisher.exception.MetricReportingException;
import org.wso2.am.analytics.publisher.reporter.MetricEventBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * OB timestamp publisher worker class.
 */
public class OBTimestampPublisher implements Runnable {

    private MetricEventBuilder builder;
    private static final String CORRELATION_ID = "correlationId";
    private static final String REQUEST_TIMESTAMP = "requestTimestamp";
    private static final String BACKEND_LATENCY = "backendLatency";
    private static final String REQUEST_MEDIATION_LATENCY = "requestMediationLatency";
    private static final String RESPONSE_LATENCY = "responseLatency";
    private static final String RESPONSE_MEDIATION_LATENCY = "responseMediationLatency";
    private static final String API_LATENCY_INPUT_STREAM = "APILatencyInputStream";
    private static final String API_LATENCY_STREAM_VERSION = "1.0.0";
    private static final Log log = LogFactory.getLog(OBTimestampPublisher.class);

    public OBTimestampPublisher(MetricEventBuilder builder) {

        this.builder = builder;
    }

    public void run() {

        try {
            Map<String, Object> eventMap = builder.build();
            Map<String, Object> analyticsData = new HashMap<>();
            analyticsData.put(CORRELATION_ID, eventMap.get(CORRELATION_ID));
            analyticsData.put(REQUEST_TIMESTAMP, eventMap.get(REQUEST_TIMESTAMP));
            analyticsData.put(BACKEND_LATENCY,
                    eventMap.get(BACKEND_LATENCY) != null ? eventMap.get(BACKEND_LATENCY) : 0L);
            analyticsData.put(REQUEST_MEDIATION_LATENCY,
                    eventMap.get(REQUEST_MEDIATION_LATENCY) != null ? eventMap.get(REQUEST_MEDIATION_LATENCY) : 0L);
            analyticsData.put(RESPONSE_LATENCY,
                    eventMap.get(RESPONSE_LATENCY) != null ? eventMap.get(RESPONSE_LATENCY) : 0L);
            analyticsData.put(RESPONSE_MEDIATION_LATENCY, eventMap.get(RESPONSE_MEDIATION_LATENCY) != null ?
                    eventMap.get(RESPONSE_MEDIATION_LATENCY) : 0L);
            publishLatencyData(analyticsData);
        } catch (MetricReportingException e) {
            log.error("Error while collecting latency stats", e);
        }
    }

    @Generated(message = "This method is already covered")
    protected void publishLatencyData(Map<String, Object> analyticsData) {
        OBDataPublisherUtil.publishData(API_LATENCY_INPUT_STREAM, API_LATENCY_STREAM_VERSION, analyticsData);
    }

}
