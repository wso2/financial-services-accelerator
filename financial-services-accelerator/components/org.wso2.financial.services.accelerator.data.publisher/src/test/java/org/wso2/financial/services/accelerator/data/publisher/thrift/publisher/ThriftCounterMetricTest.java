package org.wso2.financial.services.accelerator.data.publisher.thrift.publisher;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.am.analytics.publisher.exception.MetricCreationException;
import org.wso2.am.analytics.publisher.reporter.MetricEventBuilder;
import org.wso2.am.analytics.publisher.reporter.MetricSchema;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ThriftCounterMetricTest {
    private EventQueue mockQueue;

    @BeforeMethod
    void setUp() {
        mockQueue = mock(EventQueue.class);
    }

    @Test
    void testConstructorAndGetters() throws MetricCreationException {
        ThriftCounterMetric metric = new ThriftCounterMetric("test", MetricSchema.RESPONSE, mockQueue);
        Assert.assertEquals("test", metric.getName());
        Assert.assertEquals(MetricSchema.RESPONSE, metric.getSchema());
    }

    @Test
    void testIncrementCountDelegatesToQueue() throws Exception {
        ThriftCounterMetric metric = new ThriftCounterMetric("test", MetricSchema.RESPONSE, mockQueue);
        MetricEventBuilder builder = mock(MetricEventBuilder.class);
        int result = metric.incrementCount(builder);
        verify(mockQueue, times(1)).put(builder);
        Assert.assertEquals(0, result);
    }


    @Test
    void testGetEventBuilderForErrorSchema() throws MetricCreationException {
        ThriftCounterMetric metric = new ThriftCounterMetric("test", MetricSchema.ERROR, mockQueue);
        MetricEventBuilder builder = metric.getEventBuilder();
        Assert.assertTrue(builder instanceof ThriftMetricEventBuilder);
        Assert.assertEquals(ThriftStream.FAULT_STREAM, ((ThriftMetricEventBuilder) builder).getThriftStream());
    }

    @Test
    void testGetEventBuilderForResponseSchema() throws MetricCreationException {
        ThriftCounterMetric metric = new ThriftCounterMetric("test", MetricSchema.RESPONSE, mockQueue);
        MetricEventBuilder builder = metric.getEventBuilder();
        Assert.assertTrue(builder instanceof ThriftMetricEventBuilder);
        Assert.assertEquals(ThriftStream.REQUEST_STREAM, ((ThriftMetricEventBuilder) builder).getThriftStream());
    }
}

