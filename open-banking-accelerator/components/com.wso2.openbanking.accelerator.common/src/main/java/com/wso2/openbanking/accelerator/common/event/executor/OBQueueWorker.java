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

package com.wso2.openbanking.accelerator.common.event.executor;

import com.wso2.openbanking.accelerator.common.event.executor.model.OBEvent;
import com.wso2.openbanking.accelerator.common.internal.OpenBankingCommonDataHolder;
import com.wso2.openbanking.accelerator.common.util.OpenBankingUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * Open Banking Queue worker implementation to execute events in queue.
 */
public class OBQueueWorker implements Runnable {

    private BlockingQueue<OBEvent> eventQueue;
    private ExecutorService executorService;
    private static final Log log = LogFactory.getLog(OBQueueWorker.class);

    public OBQueueWorker(BlockingQueue<OBEvent> queue, ExecutorService executorService) {

        this.eventQueue = queue;
        this.executorService = executorService;
    }

    @Override
    public void run() {

        ThreadPoolExecutor threadPoolExecutor = ((ThreadPoolExecutor) executorService);

        do {
            OBEvent event = eventQueue.poll();
            if (event != null) {
                Map<Integer, String> obEventExecutors = OpenBankingCommonDataHolder.getInstance().getOBEventExecutors();
                List<OBEventExecutor> executorList = obEventExecutors.keySet().stream()
                        .map(integer -> (OBEventExecutor) OpenBankingUtils
                                .getClassInstanceFromFQN(obEventExecutors.get(integer))).collect(Collectors.toList());
                for (OBEventExecutor obEventExecutor : executorList) {
                    obEventExecutor.processEvent(event);
                }
            } else {
                log.error("OB Event is null");
            }
        } while (threadPoolExecutor.getActiveCount() == 1 && eventQueue.size() != 0);
    }
}
