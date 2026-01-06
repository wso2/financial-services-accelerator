package org.wso2.financial.services.accelerator.data.publisher.thrift.publisher;

import org.apache.commons.pool.ObjectPool;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.am.analytics.publisher.reporter.MetricEventBuilder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class EventQueueTest {
    private ObjectPool mockPool;

    @BeforeMethod
    void setUp() {
        mockPool = Mockito.mock(ObjectPool.class);
    }

    @Test
    void testPutEventSuccessfully() {
        org.wso2.financial.services.accelerator.data.publisher.thrift.publisher.EventQueue queue = new
                org.wso2.financial.services.accelerator.data.publisher.thrift.publisher.EventQueue
                (2, 1, mockPool);
        MetricEventBuilder builder = Mockito.mock(MetricEventBuilder.class);
        queue.put(builder);
        // No exception means success
    }

    @Test
    void testQueueFullDropsEvents() {
        org.wso2.financial.services.accelerator.data.publisher.thrift.publisher.EventQueue queue = new
                org.wso2.financial.services.accelerator.data.publisher.thrift.publisher.EventQueue(1,
                1, mockPool);
        MetricEventBuilder builder1 = Mockito.mock(MetricEventBuilder.class);
        MetricEventBuilder builder2 = Mockito.mock(MetricEventBuilder.class);
        queue.put(builder1);
        queue.put(builder2); // Should be dropped
        // No exception means handled gracefully
    }

    @Test
    void testConcurrentPut() throws InterruptedException {
        int queueSize = 5;
        EventQueue queue = new EventQueue(queueSize, 2, mockPool);
        CountDownLatch latch = new CountDownLatch(queueSize + 2);
        for (int i = 0; i < queueSize + 2; i++) {
            new Thread(() -> {
                queue.put(Mockito.mock(MetricEventBuilder.class));
                latch.countDown();
            }).start();
        }
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }
}
