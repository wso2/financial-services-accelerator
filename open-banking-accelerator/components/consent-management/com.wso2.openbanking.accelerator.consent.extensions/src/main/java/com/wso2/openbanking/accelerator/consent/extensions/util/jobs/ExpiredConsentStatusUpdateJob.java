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

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import com.wso2.openbanking.accelerator.consent.mgt.dao.constants.ConsentMgtDAOConstants;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentHistoryResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.constants.ConsentCoreServiceConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;

/**
 * Scheduled Task to read and update expired consents in the DB
 * 1) Read the consents which has a expiry time attribute from the DB.
 * 2) Check if expired, and collect expired consents
 * 3) Update the expired statues in DB
 * 4) Notify state change to relevant handler.
 */
@DisallowConcurrentExecution
public class ExpiredConsentStatusUpdateJob implements Job {

    private static Log log = LogFactory.getLog(ExpiredConsentStatusUpdateJob.class);
    private static final String expiredConsentStatus =
            OpenBankingConfigParser.getInstance().getStatusWordingForExpiredConsents();
    private static final String expirationEligibleConsentStatuses =
            OpenBankingConfigParser.getInstance().getEligibleStatusesForConsentExpiry();

    /**
     * Method used to enforce periodic statues update of consents.
     *
     * @param jobExecutionContext  Job Execution Context
     * @throws JobExecutionException  if an error occurs while executing the job
     */
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            updateExpiredStatues();
        } catch (ConsentManagementException e) {
            log.error("Error occurred while updating status for expired consents", e);
        }

    }

    /**
     * Method to update statues of consents.
     * @throws ConsentManagementException if an error occurs while updating the consent status
     */
    public static void updateExpiredStatues() throws ConsentManagementException {

        log.debug("Expired Consent Status Update Scheduled Task is executing.");
        // get consents which has a expiry time attribute
        ArrayList<DetailedConsentResource> consentsEligibleForExpiration =
                ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                        .getConsentsEligibleForExpiration(expirationEligibleConsentStatuses);
        // filter out expired consents and change the status of expired consents
        for (DetailedConsentResource consentResource : consentsEligibleForExpiration) {
            if (isExpired(consentResource)) {
                String updatedConsentId = updateConsentExpiredStatus(consentResource);
                if (log.isDebugEnabled()) {
                    log.debug("Expired status updated for consent : " + updatedConsentId);
                }
            }
        }
        log.debug("Expired Consent Status Update Scheduled Task is finished.");
    }

    /**
     * Check if the consents is expired based on the consent attribute value.
     *
     * @param detailedConsentResource
     * @return
     */
    private static boolean isExpired(DetailedConsentResource detailedConsentResource) {

        Map<String, String> consentAttributes = detailedConsentResource.getConsentAttributes();
        if (consentAttributes.containsKey(ConsentMgtDAOConstants.CONSENT_EXPIRY_TIME_ATTRIBUTE)) {
            // Read the UTC expiry timestamp in long
            long expiryTimestamp = Long.parseLong(
                    consentAttributes.get(ConsentMgtDAOConstants.CONSENT_EXPIRY_TIME_ATTRIBUTE));
            // Compare with current UTC timestamp in long
            Instant instant = Instant.now();
            long currentTimeStampSeconds = instant.getEpochSecond();
            if (currentTimeStampSeconds >= expiryTimestamp) {
                log.info("Consent " + detailedConsentResource.getConsentID() + " is identified as expired based on the "
                        + "given consent expiration time : " + expiryTimestamp);
                return true;
            }
        }
        return false;
    }

    /**
     * Update the expired consents in DB.
     *
     * @param detailedConsentResource
     * @return
     */
    private static String updateConsentExpiredStatus(DetailedConsentResource detailedConsentResource) {

        try {
            ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                    .updateConsentStatus(detailedConsentResource.getConsentID(), expiredConsentStatus);

            //since the consent status is changed during the consent expiration, the previous status will be saved
            //in the consent history to properly back-track the previous status held in the consent
            storeConsentStateChangeInConsentHistory(detailedConsentResource);

        } catch (ConsentManagementException e) {
            log.error("Error occurred while updating status for consentId : " +
                    detailedConsentResource.getConsentID(), e);
        }
        return detailedConsentResource.getConsentID();
    }

    private static void storeConsentStateChangeInConsentHistory(DetailedConsentResource detailedConsentResource)
            throws ConsentManagementException {

        if (OpenBankingConfigParser.getInstance().isConsentAmendmentHistoryEnabled()) {

            ConsentCoreService consentCoreService = ConsentExtensionsDataHolder.getInstance().getConsentCoreService();
            ConsentHistoryResource consentHistoryResource = new ConsentHistoryResource();
            consentHistoryResource.setTimestamp(System.currentTimeMillis() / 1000);
            consentHistoryResource.setReason(ConsentCoreServiceConstants.AMENDMENT_REASON_CONSENT_EXPIRATION);
            consentHistoryResource.setDetailedConsentResource(detailedConsentResource);

            consentCoreService.storeConsentAmendmentHistory(detailedConsentResource.getConsentID(),
                    consentHistoryResource, null);
        }
    }

}
