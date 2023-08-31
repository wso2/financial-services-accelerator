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

import com.wso2.openbanking.accelerator.gateway.internal.GatewayDataHolder;
import org.wso2.am.analytics.publisher.exception.MetricCreationException;
import org.wso2.am.analytics.publisher.reporter.AbstractMetricReporter;
import org.wso2.am.analytics.publisher.reporter.CounterMetric;
import org.wso2.am.analytics.publisher.reporter.MetricSchema;
import org.wso2.am.analytics.publisher.reporter.TimerMetric;
import org.wso2.am.analytics.publisher.reporter.cloud.DefaultAnalyticsMetricReporter;

import java.util.Map;

/**
 * OB Analytics Metric Reporter class.
 */
public class OBAnalyticsMetricReporter extends AbstractMetricReporter {

    private DefaultAnalyticsMetricReporter defaultAnalyticsMetricReporter;

    public OBAnalyticsMetricReporter(Map<String, String> properties) throws MetricCreationException {

        super(properties);
        if (GatewayDataHolder.getInstance().isAPIMAnalyticsEnabled()) {
            defaultAnalyticsMetricReporter = new DefaultAnalyticsMetricReporter(properties);
        }
    }

    @Override
    protected void validateConfigProperties(Map<String, String> map) throws MetricCreationException {

    }

    @Override
    protected CounterMetric createCounter(String name, MetricSchema schema) throws MetricCreationException {

        CounterMetric counterMetric = null;
        if (GatewayDataHolder.getInstance().isAPIMAnalyticsEnabled()) {
            counterMetric = defaultAnalyticsMetricReporter.createCounterMetric(name, schema);
        }
        return new OBCounterMetric(name, counterMetric, schema);
    }

    @Override
    protected TimerMetric createTimer(String s) {

        return null;
    }

}
