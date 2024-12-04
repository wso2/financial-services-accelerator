/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.event.notifications.service.realtime.util.activator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronExpression;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.event.notifications.service.realtime.util.job.EventNotificationConsumerJob;
import org.wso2.financial.services.accelerator.event.notifications.service.realtime.util.scheduler.PeriodicalEventNotificationConsumerJobScheduler;

import java.text.ParseException;
import java.util.Date;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Scheduled Task definition and trigger to perform realtime event notification sending based on the cron string.
 */
@Generated(message = "Excluding from code coverage")
public class PeriodicalEventNotificationConsumerJobActivator {

    private static Log log = LogFactory.getLog(PeriodicalEventNotificationConsumerJobActivator.class);

    public void activate() {
        int cronInSeconds = 60;
        String periodicCronExpression = FinancialServicesConfigParser.getInstance()
                .getRealtimeEventNotificationSchedulerCronExpression().replaceAll("[\r\n]", "");

        try {
            CronExpression cron = new CronExpression(periodicCronExpression);

            Date nextValidTime = cron.getNextValidTimeAfter(new Date());
            Date secondValidTime = cron.getNextValidTimeAfter(nextValidTime);

            cronInSeconds = (int) (secondValidTime.getTime() - nextValidTime.getTime()) / 1000;

        } catch (ParseException e) {
            log.error("Error while parsing the event notification scheduler cron expression : "
                    + periodicCronExpression, e);
        }

        JobDetail job = newJob(EventNotificationConsumerJob.class)
                .withIdentity("RealtimeEventNotificationJob", "group2")
                .build();

        Trigger trigger = newTrigger()
                .withIdentity("periodicalEvenNotificationTrigger", "group2")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(cronInSeconds)
                        .repeatForever())
                .build();

        try {
            Scheduler scheduler = PeriodicalEventNotificationConsumerJobScheduler.getInstance().getScheduler();
            // this check is to remove already stored jobs in clustered mode.
            if (scheduler.checkExists(job.getKey())) {
                scheduler.deleteJob(job.getKey());
            }

            scheduler.scheduleJob(job, trigger);
            log.info("Periodical Realtime Event Notification sender Started with cron : "
                    + periodicCronExpression);
        } catch (SchedulerException e) {
            log.error("Error while starting Periodical Realtime Event Notification sender", e);
        }
    }
}
