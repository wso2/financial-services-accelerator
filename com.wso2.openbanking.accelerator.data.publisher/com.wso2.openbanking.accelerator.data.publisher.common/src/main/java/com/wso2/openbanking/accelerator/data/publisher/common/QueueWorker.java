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

package com.wso2.openbanking.accelerator.data.publisher.common;

import com.wso2.openbanking.accelerator.data.publisher.common.model.OBAnalyticsEvent;
import com.wso2.openbanking.accelerator.data.publisher.common.util.OBDataPublisherUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Queue worker implementation for publish events in queue.
 */
public class QueueWorker implements Runnable {

    private BlockingQueue<OBAnalyticsEvent> eventQueue;
    private ExecutorService executorService;
    private static final Log log = LogFactory.getLog(QueueWorker.class);

    public QueueWorker(BlockingQueue<OBAnalyticsEvent> queue, ExecutorService executorService) {

        this.eventQueue = queue;
        this.executorService = executorService;
    }

    public void run() {

        ThreadPoolExecutor threadPoolExecutor = ((ThreadPoolExecutor) executorService);

        do {
            OBAnalyticsEvent event = eventQueue.poll();
            if (event != null) {
                FinancialServicesDataPublisher dataPublisher = OBDataPublisherUtil.getDataPublisherInstance();
                if (dataPublisher != null) {
                    dataPublisher.publish(event.getStreamName(), event.getStreamVersion(), event.getAnalyticsData());
                    OBDataPublisherUtil.releaseDataPublishingInstance(dataPublisher);
                }
            }
        } while (threadPoolExecutor.getActiveCount() == 1 && eventQueue.size() != 0);
    }

}
