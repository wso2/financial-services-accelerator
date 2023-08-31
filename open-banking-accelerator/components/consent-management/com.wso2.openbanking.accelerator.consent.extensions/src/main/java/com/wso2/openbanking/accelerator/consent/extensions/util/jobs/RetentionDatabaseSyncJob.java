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


package com.wso2.openbanking.accelerator.consent.extensions.util.jobs;

import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Scheduled Task to read and sync the temporary retention data in consent tables to retention database.
 * 1) Read the consents in temporary retention tables in consennt DB.
 * 2) Insert each consent data to retention database.
 * 3) Delete the consent data from temporary retention tables.
 */
@DisallowConcurrentExecution
public class RetentionDatabaseSyncJob implements Job {

    private static Log log = LogFactory.getLog(RetentionDatabaseSyncJob.class);

    /**
     * Method used to enforce sync the temporary retention data.
     *
     * @param jobExecutionContext
     * @throws JobExecutionException
     */
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        try {
            syncRetentionDatabase();
        } catch (ConsentManagementException e) {
            log.error("Error occurred while retention database syncing", e);
        }
    }

    /**
     * Method to sync the temporary retention data.
     */
    public static void syncRetentionDatabase() throws ConsentManagementException {

        log.debug("Retention database syncing scheduled task is executing.");
        ConsentExtensionsDataHolder.getInstance().getConsentCoreService().syncRetentionDatabaseWithPurgedConsent();
        log.debug("Retention database syncing scheduled task is finished.");
    }

}
