/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.job.activator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.util.Generated;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.job.ExpiredConsentStatusUpdateJob;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.job.scheduler.PeriodicalConsentJobScheduler;

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
    @Generated(message = "Ignoring since method contains no unit testable logics")
    public void activate() {

        if (FinancialServicesConfigParser.getInstance().isConsentExpirationPeriodicalJobEnabled()) {
            JobDetail job = newJob(ExpiredConsentStatusUpdateJob.class)
                    .withIdentity("ConsentStatusUpdateJob", "group1")
                    .build();

            Trigger trigger = newTrigger()
                    .withIdentity("periodicalTrigger", "group1")
                    .withSchedule(CronScheduleBuilder.cronSchedule(
                            FinancialServicesConfigParser.getInstance().getConsentExpiryCronExpression()))
                    .build();

            try {
                Scheduler scheduler = PeriodicalConsentJobScheduler.getInstance().getScheduler();
                // this check is to remove already stored jobs in clustered mode.
                if (scheduler.checkExists(job.getKey())) {
                    scheduler.deleteJob(job.getKey());
                }

                scheduler.scheduleJob(job, trigger);
                if (log.isDebugEnabled()) {
                    String debug = "Periodical Consent Status Updater Started with cron : "
                            + FinancialServicesConfigParser.getInstance().getConsentExpiryCronExpression();
                    log.debug(debug.replaceAll("[\r\n]", ""));
                }
            } catch (SchedulerException e) {
                log.error("Error while creating and starting Periodical Consent Status Update Scheduled Task", e);
            }
        }
    }
}
