package org.wso2.financial.services.accelerator.data.publisher.thrift.publisher;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.am.analytics.publisher.exception.MetricCreationException;

import java.util.HashMap;
import java.util.Map;


public class ThriftMetricReporterTest {
    private Map<String, String> getValidProperties() {
        Map<String, String> props = new HashMap<>();
        props.put(Constants.THRIFT_ENDPOINT_RECEIVER_URL_SET, "tcp://localhost:7611");
        props.put(Constants.THRIFT_ENDPOINT_AUTH_URL_SET, "ssl://localhost:7711");
        props.put(Constants.THRIFT_ENDPOINT_USERNAME, "admin");
        props.put(Constants.THRIFT_ENDPOINT_PASSWORD, "admin");
        return props;
    }

    @Test
    void testCreateTimerReturnsNull() throws MetricCreationException {
        org.wso2.financial.services.accelerator.data.publisher.thrift.publisher.ThriftMetricReporter reporter =
                new org.wso2.financial.services.accelerator.data.publisher.thrift.publisher.
                        ThriftMetricReporter(getValidProperties());
        Assert.assertNull(reporter.createTimer("testTimer"));
    }
}

