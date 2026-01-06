package org.wso2.financial.services.accelerator.data.publisher;

import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.financial.services.accelerator.data.publisher.model.FSAnalyticsEvent;

import java.util.concurrent.ExecutorService;

public class EventQueueTest {
    private EventQueue eventQueue;
    private ExecutorService mockExecutor;

    @BeforeMethod
    public void setUp() {
        eventQueue = new EventQueue(1, 1);
    }

    @Test
    public void testPutEventSuccessfully() {
        FSAnalyticsEvent event = Mockito.mock(FSAnalyticsEvent.class);
        eventQueue.put(event);
        // No exception means success
    }

    @Test
    public void testQueueFullDropsEvents() {
        FSAnalyticsEvent event1 = Mockito.mock(FSAnalyticsEvent.class);
        FSAnalyticsEvent event2 = Mockito.mock(FSAnalyticsEvent.class);
        eventQueue.put(event1);
        eventQueue.put(event2); // Should be dropped, no exception thrown
    }

    @Test
    public void testFinalizeShutsDownExecutor() throws Throwable {
        EventQueue queue = new EventQueue(1, 1);
        queue.finalize(); // Should call shutdown on executor
        // No exception means success
    }
}
