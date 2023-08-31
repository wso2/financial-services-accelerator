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

package com.wso2.openbanking.accelerator.event.notifications.service.realtime.util.scheduler;

import com.wso2.openbanking.accelerator.common.util.Generated;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Periodic realtime event notification job scheduler class.
 * This class initialize the scheduler and schedule configured jobs and triggers.
 */
@Generated(message = "Excluding from code coverage")
public class PeriodicalEventNotificationConsumerJobScheduler {
    private static volatile PeriodicalEventNotificationConsumerJobScheduler instance;
    private static volatile Scheduler scheduler;
    private static Log log = LogFactory.getLog(PeriodicalEventNotificationConsumerJobScheduler.class);

    private PeriodicalEventNotificationConsumerJobScheduler() {
        initScheduler();
    }

    public static synchronized PeriodicalEventNotificationConsumerJobScheduler getInstance() {

        if (instance == null) {
            synchronized (PeriodicalEventNotificationConsumerJobScheduler.class) {
                if (instance == null) {
                    instance = new PeriodicalEventNotificationConsumerJobScheduler();
                }
            }
        }
        return instance;
    }

    private void initScheduler() {

        if (instance != null) {
            return;
        }
        synchronized (PeriodicalEventNotificationConsumerJobScheduler.class) {
            try {
                scheduler = StdSchedulerFactory.getDefaultScheduler();
                scheduler.start();
            } catch (SchedulerException e) {
                log.error("Exception while initializing the Real-time Event notification scheduler", e);
            }
        }
    }

    /**
     * Returns the scheduler.
     *
     * @return Scheduler scheduler.
     */
    public Scheduler getScheduler() {
        return scheduler;
    }
}
