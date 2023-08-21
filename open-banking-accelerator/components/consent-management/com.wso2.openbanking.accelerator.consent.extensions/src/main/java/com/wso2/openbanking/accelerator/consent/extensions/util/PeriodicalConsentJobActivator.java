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


package com.wso2.openbanking.accelerator.consent.extensions.util;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.consent.extensions.util.jobs.ExpiredConsentStatusUpdateJob;
import com.wso2.openbanking.accelerator.consent.extensions.util.jobs.RetentionDatabaseSyncJob;
import com.wso2.openbanking.accelerator.consent.extensions.util.scheduler.PeriodicalConsentJobScheduler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Scheduled Task definition and trigger to perform expired consent status updateJob based on the cron string.
 */
public class PeriodicalConsentJobActivator {

    private static Log log = LogFactory.getLog(PeriodicalConsentJobActivator.class);

    /**
     * activate the scheduler task.
     */
    public void activate() {

        if (OpenBankingConfigParser.getInstance().isConsentExpirationPeriodicalJobEnabled()) {
            JobDetail job = newJob(ExpiredConsentStatusUpdateJob.class)
                    .withIdentity("ConsentStatusUpdateJob", "group1")
                    .build();

            Trigger trigger = newTrigger()
                    .withIdentity("periodicalTrigger", "group1")
                    .withSchedule(CronScheduleBuilder.cronSchedule(
                            OpenBankingConfigParser.getInstance().getConsentExpiryCronExpression()))
                    .build();

            try {
                Scheduler scheduler = PeriodicalConsentJobScheduler.getInstance().getScheduler();
                // this check is to remove already stored jobs in clustered mode.
                if (scheduler.checkExists(job.getKey())) {
                    scheduler.deleteJob(job.getKey());
                }

                scheduler.scheduleJob(job, trigger);
                if (log.isDebugEnabled()) {
                    log.debug("Periodical Consent Status Updater Started with cron : "
                            + OpenBankingConfigParser.getInstance().getConsentExpiryCronExpression());
                }
            } catch (SchedulerException e) {
                log.error("Error while creating and starting Periodical Consent Status Update Scheduled Task", e);
            }
        }

        if (OpenBankingConfigParser.getInstance().isRetentionDataDBSyncEnabled() &&
                OpenBankingConfigParser.getInstance().isConsentDataRetentionEnabled()) {
            JobDetail job = newJob(RetentionDatabaseSyncJob.class)
                    .withIdentity("RetentionDatabaseSyncJob", "group1")
                    .build();

            Trigger trigger = newTrigger()
                    .withIdentity("RetentionDatabaseSyncPeriodicalTrigger", "group1")
                    .withSchedule(CronScheduleBuilder.cronSchedule(
                            OpenBankingConfigParser.getInstance().getRetentionDataDBSyncCronExpression()))
                    .build();

            try {
                Scheduler scheduler = PeriodicalConsentJobScheduler.getInstance().getScheduler();
                // this check is to remove already stored jobs in clustered mode.
                if (scheduler.checkExists(job.getKey())) {
                    scheduler.deleteJob(job.getKey());
                }

                scheduler.scheduleJob(job, trigger);
                if (log.isDebugEnabled()) {
                    log.debug("Retention database sync job started with cron : "
                            + OpenBankingConfigParser.getInstance().getConsentExpiryCronExpression());
                }
            } catch (SchedulerException e) {
                log.error("Error while creating and starting retention database syncing scheduled Task", e);
            }
        }
    }
}
