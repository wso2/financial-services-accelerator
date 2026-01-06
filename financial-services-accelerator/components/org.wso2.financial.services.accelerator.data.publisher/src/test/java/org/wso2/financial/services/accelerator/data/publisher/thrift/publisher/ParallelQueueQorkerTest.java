package org.wso2.financial.services.accelerator.data.publisher.thrift.publisher;

import org.apache.commons.pool.ObjectPool;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.am.analytics.publisher.exception.MetricReportingException;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ParallelQueueQorkerTest {
    private BlockingQueue<ThriftMetricEventBuilder> queue;
    private ObjectPool pool;
    private ThriftClient client;

    @BeforeMethod
    void setUp() {
        queue = new ArrayBlockingQueue<>(2);
        pool = mock(ObjectPool.class);
        client = mock(ThriftClient.class);
    }

    @Test
    void testPublishesEventSuccessfully() throws Exception {
        ThriftMetricEventBuilder event = mock(ThriftMetricEventBuilder.class);
        queue.put(event);
        when(pool.borrowObject()).thenReturn(client);

        ParallelQueueWorker worker = new ParallelQueueWorker((BlockingQueue) queue, pool);

        Thread t = new Thread(() -> {
            // Run only one iteration for test
            try {
                worker.run();
            } catch (Exception ignored) {
            }
        });
        t.start();
        Thread.sleep(100); // Let the worker process
        t.interrupt();
        verify(pool, atLeastOnce()).borrowObject();
        verify(client, atLeastOnce()).publish(event);
        verify(pool, atLeastOnce()).returnObject(client);
    }

    @Test
    void testNullThriftClientLogsError() throws Exception {
        ThriftMetricEventBuilder event = mock(ThriftMetricEventBuilder.class);
        queue.put(event);
        when(pool.borrowObject()).thenReturn(null);

        ParallelQueueWorker worker = new ParallelQueueWorker((BlockingQueue) queue, pool);

        Thread t = new Thread(() -> {
            try {
                worker.run();
            } catch (Exception ignored) {
            }
        });
        t.start();
        Thread.sleep(100);
        t.interrupt();

        verify(pool, atLeastOnce()).borrowObject();
        verify(pool, atLeastOnce()).returnObject(null);
    }

    @Test
    void testMetricReportingExceptionFaultStream() throws Exception {
        ThriftMetricEventBuilder event = mock(ThriftMetricEventBuilder.class);
        when(event.getThriftStream()).thenReturn(ThriftStream.FAULT_STREAM);
        doThrow(new MetricReportingException("fail")).when(client).publish(event);
        queue.put(event);
        when(pool.borrowObject()).thenReturn(client);

        ParallelQueueWorker worker = new ParallelQueueWorker((BlockingQueue) queue, pool);

        Thread t = new Thread(() -> {
            try {
                worker.run();
            } catch (Exception ignored) {
            }
        });
        t.start();
        Thread.sleep(100);
        t.interrupt();

        verify(client, atLeastOnce()).publish(event);
        verify(pool, atLeastOnce()).returnObject(client);
    }

    @Test
    void testInterruptedExceptionHandling() throws Exception {
        BlockingQueue<ThriftMetricEventBuilder> spyQueue = spy(queue);
        doThrow(new InterruptedException("interrupted")).when(spyQueue).take();

        ParallelQueueWorker worker = new ParallelQueueWorker((BlockingQueue) spyQueue, pool);

        Thread t = new Thread(() -> {
            try {
                worker.run();
            } catch (Exception ignored) {
            }
        });
        t.start();
        Thread.sleep(100);
        t.interrupt();

        verify(spyQueue, atLeastOnce()).take();
    }

    @Test
    void testExceptionOnReturnObject() throws Exception {
        ThriftMetricEventBuilder event = mock(ThriftMetricEventBuilder.class);
        queue.put(event);
        when(pool.borrowObject()).thenReturn(client);
        doThrow(new RuntimeException("return fail")).when(pool).returnObject(client);

        ParallelQueueWorker worker = new ParallelQueueWorker((BlockingQueue) queue, pool);

        Thread t = new Thread(() -> {
            try {
                worker.run();
            } catch (Exception ignored) {
            }
        });
        t.start();
        Thread.sleep(100);
        t.interrupt();

        verify(pool, atLeastOnce()).returnObject(client);
    }
}




