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

package  org.wso2.financial.services.accelerator.common.event.executor;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.common.event.executor.model.FSEvent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;


/**
 * Open Banking event queue wrapper class wrapping the ArrayBlockingQueue.
 */
public class FSEventQueue {

    private static final Log log = LogFactory.getLog(org.wso2.financial.services.accelerator.common.event.
            executor.FSEventQueue.class);
    private final BlockingQueue<FSEvent> eventQueue;
    private final ExecutorService executorService;

    public FSEventQueue(int queueSize, int workerThreadCount) {

        // Note : Using a fixed worker thread pool and a bounded queue to control the load on the server
        executorService = Executors.newFixedThreadPool(workerThreadCount);
        eventQueue = new ArrayBlockingQueue<>(queueSize);
    }

    public void put(FSEvent obEvent) {

        try {
            if (eventQueue.offer(obEvent)) {
                executorService.submit(new FSQueueWorker(eventQueue, executorService));
            } else {
                log.error("Event queue is full. Starting to drop events.");
            }
        } catch (RejectedExecutionException e) {
            log.warn("Task submission failed. Task queue might be full", e);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        executorService.shutdown();
        super.finalize();
    }
}
