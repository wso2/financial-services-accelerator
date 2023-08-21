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
import com.wso2.openbanking.accelerator.gateway.internal.GatewayDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.am.analytics.publisher.exception.MetricReportingException;
import org.wso2.am.analytics.publisher.reporter.CounterMetric;
import org.wso2.am.analytics.publisher.reporter.MetricEventBuilder;
import org.wso2.am.analytics.publisher.reporter.MetricSchema;
import org.wso2.am.analytics.publisher.reporter.cloud.DefaultChoreoFaultMetricEventBuilder;
import org.wso2.am.analytics.publisher.reporter.cloud.DefaultChoreoResponseMetricEventBuilder;
import org.wso2.am.analytics.publisher.reporter.cloud.DefaultFaultMetricEventBuilder;
import org.wso2.am.analytics.publisher.reporter.cloud.DefaultResponseMetricEventBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * OB Counter metric class to publish am analytics and ob analytics data.
 */
public class OBCounterMetric implements CounterMetric {

    private static final Log log = LogFactory.getLog(OBCounterMetric.class);
    private final ExecutorService obExecutorService;
    private final CounterMetric counterMetric;
    private String name;
    private MetricSchema schema;

    public OBCounterMetric(String name, CounterMetric counterMetric, MetricSchema schema) {

        this.counterMetric = counterMetric;
        this.name = name;
        this.schema = schema;
        int workerThreadCount = Integer.parseInt(GatewayDataHolder.getInstance().getWorkerThreadCount());
        this.obExecutorService = Executors.newFixedThreadPool(workerThreadCount);
    }

    @Override
    public int incrementCount(MetricEventBuilder builder) {

       int status;

        if (GatewayDataHolder.getInstance().isAPIMAnalyticsEnabled()) {
            try {
                status = counterMetric.incrementCount(builder);
            } catch (MetricReportingException e) {
                log.error("Error while publishing APIM analytics", e);
                status = 0;
            }
        } else {
            log.debug("APIM analytics disabled.");
            status = 0;
        }

        // OB data publishing
        if (GatewayDataHolder.getInstance().isOBDataPublishingEnabled()) {
            obExecutorService.submit(new OBTimestampPublisher(builder));
        }
        return status;
    }

    @Override
    public String getName() {

        return this.name;
    }

    @Override
    public MetricSchema getSchema() {

        return this.schema;
    }

    @Override
    @Generated(message = "This is skipped because schema cannot be mocked")
    public MetricEventBuilder getEventBuilder() {

        switch (schema) {
            case RESPONSE:
                return new DefaultResponseMetricEventBuilder();
            case ERROR:
                return new DefaultFaultMetricEventBuilder();
            case CHOREO_RESPONSE:
                return new DefaultChoreoResponseMetricEventBuilder();
            case CHOREO_ERROR:
                return new DefaultChoreoFaultMetricEventBuilder();
            default:
                // will not happen
                return null;
        }
    }
}
