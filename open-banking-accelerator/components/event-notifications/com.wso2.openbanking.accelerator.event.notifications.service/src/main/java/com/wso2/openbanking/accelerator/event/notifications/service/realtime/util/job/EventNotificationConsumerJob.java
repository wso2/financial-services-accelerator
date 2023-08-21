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

package com.wso2.openbanking.accelerator.event.notifications.service.realtime.util.job;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.event.notifications.service.internal.EventNotificationDataHolder;
import com.wso2.openbanking.accelerator.event.notifications.service.realtime.model.RealtimeEventNotification;
import com.wso2.openbanking.accelerator.event.notifications.service.realtime.service.RealtimeEventNotificationSenderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Scheduled Task to send realtime event notifications to callback Urls.
 * This task is scheduled to run periodically.
 * This task consumes all the notifications in the queue and send them to the callback urls.
 */
@Generated(message = "Excluding from code coverage")
@DisallowConcurrentExecution
public class EventNotificationConsumerJob implements Job {

    private static final Log log = LogFactory.getLog(EventNotificationConsumerJob.class);
    private static final int THREAD_POOL_SIZE = OpenBankingConfigParser
            .getInstance().getEventNotificationThreadpoolSize();

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        ArrayList<RealtimeEventNotification> notifications = consumeNotifications();
        // send notifications to the callback urls
        int threads = Math.min(notifications.size(), THREAD_POOL_SIZE);
        int threadPoolSize = Math.max(threads, 2);

        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        for (RealtimeEventNotification notification : notifications) {
            String callbackUrl = notification.getCallbackUrl();
            String payload = notification.getJsonPayload();
            Runnable worker = new RealtimeEventNotificationSenderService(callbackUrl,
                    payload, notification.getNotificationId());
            executor.execute(worker);
        }

        executor.shutdown();
        while (!executor.isTerminated()) { }
    }

    private static ArrayList<RealtimeEventNotification> consumeNotifications() {

        LinkedBlockingQueue<RealtimeEventNotification> queue = EventNotificationDataHolder.getInstance()
                .getRealtimeEventNotificationQueue();
        ArrayList<RealtimeEventNotification> notifications = new ArrayList<>();

        // consume all notifications in the queue
        int key = 0;
        while (!queue.isEmpty() && key < THREAD_POOL_SIZE) {
            key++;
            try {
                RealtimeEventNotification notification = queue.take();
                notifications.add(notification);
            } catch (InterruptedException ex) {
                log.error("Error while consuming notifications from the event notification queue", ex);
            }
        }
        return notifications;
    }
}
