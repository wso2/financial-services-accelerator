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
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.am.analytics.publisher.exception.MetricCreationException;
import org.wso2.am.analytics.publisher.exception.MetricReportingException;
import org.wso2.am.analytics.publisher.reporter.CounterMetric;

import java.util.HashMap;
import java.util.Map;

/**
 * Test for Open Banking analytics metric reporter.
 */
public class OBAnalyticsMetricReporterTest {

    private OBAnalyticsMetricReporter obMetricReporter;
    private GatewayDataHolder dataHolder;

    @Test
    public void createOBMetricReporter() throws MetricCreationException {

        dataHolder = GatewayDataHolder.getInstance();
        dataHolder.setWorkerThreadCount("1");
        dataHolder.setOBDataPublishingEnabled("true");
        obMetricReporter = new OBAnalyticsMetricReporter(new HashMap<>());
        obMetricReporter.createTimer("testTimer");
    }

    @Test(dependsOnMethods = "createOBMetricReporter")
    public void createCounterMetric() throws MetricCreationException, MetricReportingException {

        CounterMetric counterMetric = obMetricReporter.createCounter("testCounter", Mockito.any());
        Assert.assertEquals(counterMetric.getName(), "testCounter");
        counterMetric.incrementCount(Mockito.any());
        counterMetric.getSchema();
    }

    @Test(dependsOnMethods = "createOBMetricReporter")
    public void createReporterForAPIMAnalytics() throws MetricCreationException {

        dataHolder.setAPIMAnalyticsEnabled("true");
        Map<String, String> prop = new HashMap<>();
        prop.put("auth.api.token", "testToken");
        prop.put("auth.api.url", "testUrl");
        new OBAnalyticsMetricReporter(prop);
    }
}
