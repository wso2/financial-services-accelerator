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

package org.wso2.financial.services.accelerator.common.event.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.common.event.executor.model.FSEvent;
import org.wso2.financial.services.accelerator.common.internal.FinancialServicesCommonDataHolder;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * Open Banking Queue worker implementation to execute events in queue.
 */
public class FSQueueWorker implements Runnable {

    private BlockingQueue<FSEvent> eventQueue;
    private ExecutorService executorService;
    private static final Log log = LogFactory.getLog(org.wso2.financial.services.accelerator.common.event.
            executor.FSQueueWorker.class);

    protected FSQueueWorker(BlockingQueue<FSEvent> queue, ExecutorService executorService) {

        this.eventQueue = queue;
        this.executorService = executorService;
    }

    @Override
    public void run() {

        ThreadPoolExecutor threadPoolExecutor = ((ThreadPoolExecutor) executorService);

        do {
            FSEvent event = eventQueue.poll();
            if (event != null) {
                Map<Integer, String> fsEventExecutors = FinancialServicesCommonDataHolder.getInstance().
                        getFSEventExecutors();
                List<FSEventExecutor> executorList = fsEventExecutors.keySet().stream()
                        .map(integer -> (FSEventExecutor) FinancialServicesUtils
                                .getClassInstanceFromFQN(fsEventExecutors.get(integer))).collect(Collectors.toList());
                for (FSEventExecutor obEventExecutor : executorList) {
                    obEventExecutor.processEvent(event);
                }
            } else {
                log.error("OB Event is null");
            }
        } while (threadPoolExecutor.getActiveCount() == 1 && eventQueue.size() != 0);
    }
}
