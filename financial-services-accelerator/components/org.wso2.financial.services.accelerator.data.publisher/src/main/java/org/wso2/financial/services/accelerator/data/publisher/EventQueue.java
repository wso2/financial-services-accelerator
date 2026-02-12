/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.data.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.data.publisher.model.FSAnalyticsEvent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * Event queue wrapper class wrapping the ArrayBlockingQueue.
 */
public class EventQueue {

    private static final Log log = LogFactory.getLog(EventQueue.class);
    private final BlockingQueue<FSAnalyticsEvent> eventQueue;
    private final ExecutorService publisherExecutorService;

    public EventQueue(int queueSize, int workerThreadCount) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing EventQueue with queueSize: " + queueSize + " and workerThreadCount: " +
                    workerThreadCount);
        }
        // Note : Using a fixed worker thread pool and a bounded queue to control the load on the server
        publisherExecutorService = Executors.newFixedThreadPool(workerThreadCount);
        eventQueue = new ArrayBlockingQueue<>(queueSize);
    }

    public void put(FSAnalyticsEvent fsAnalyticsEvent) {
        log.debug("Attempting to add analytics event to queue");
        try {
            if (eventQueue.offer(fsAnalyticsEvent)) {
                publisherExecutorService.submit(new QueueWorker(eventQueue, publisherExecutorService));
            } else {
                log.error("Event queue is full. Starting to drop OB analytics events.");
            }
        } catch (RejectedExecutionException e) {
            log.warn("Task submission failed. Task queue might be full", e);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        log.debug("Shutting down publisher executor service");
        publisherExecutorService.shutdown();
        super.finalize();
    }
}
