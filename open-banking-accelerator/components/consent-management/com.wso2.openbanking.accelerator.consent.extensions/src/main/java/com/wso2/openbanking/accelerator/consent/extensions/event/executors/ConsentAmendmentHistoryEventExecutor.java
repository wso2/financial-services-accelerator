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

package com.wso2.openbanking.accelerator.consent.extensions.event.executors;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.event.executor.OBEventExecutor;
import com.wso2.openbanking.accelerator.common.event.executor.model.OBEvent;
import com.wso2.openbanking.accelerator.common.exception.ConsentManagementException;
import com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsDataHolder;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentHistoryResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import com.wso2.openbanking.accelerator.consent.mgt.service.constants.ConsentCoreServiceConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * Open banking event executor for Consent Amendment History Asynchronous Persistence.
 */
public class ConsentAmendmentHistoryEventExecutor implements OBEventExecutor {

    private static final Log log = LogFactory.getLog(ConsentAmendmentHistoryEventExecutor.class);

    @Override
    public void processEvent(OBEvent obEvent) {

        String eventType = obEvent.getEventType();
        if (OpenBankingConfigParser.getInstance().isConsentAmendmentHistoryEnabled() &&
                (ConsentCoreServiceConstants.CONSENT_AMENDED_STATUS.equalsIgnoreCase(eventType) ||
                        OpenBankingConstants.DEFAULT_STATUS_FOR_REVOKED_CONSENTS.equalsIgnoreCase(eventType))) {

            ConsentCoreService consentCoreService = ConsentExtensionsDataHolder.getInstance().getConsentCoreService();
            try {
                Map<String, Object> eventData = obEvent.getEventData();
                String consentID = eventData.get("ConsentId").toString();

                Map<String, Object> consentDataMap = (Map<String, Object>) eventData.get("ConsentDataMap");
                DetailedConsentResource detailedCurrentConsent = (DetailedConsentResource)
                        consentDataMap.get(ConsentCoreServiceConstants.DETAILED_CONSENT_RESOURCE);
                DetailedConsentResource detailedHistoryConsent = (DetailedConsentResource)
                        consentDataMap.get(ConsentCoreServiceConstants.CONSENT_AMENDMENT_HISTORY_RESOURCE);
                long amendedTimestamp  = (long)
                        consentDataMap.get(ConsentCoreServiceConstants.CONSENT_AMENDMENT_TIME) / 1000;

                String amendmentReason;
                if (ConsentCoreServiceConstants.CONSENT_AMENDED_STATUS.equalsIgnoreCase(eventType)) {
                    amendmentReason = ConsentCoreServiceConstants.AMENDMENT_REASON_CONSENT_AMENDMENT_FLOW;
                } else {
                    amendmentReason = ConsentCoreServiceConstants.AMENDMENT_REASON_CONSENT_REVOCATION;
                }
                ConsentHistoryResource consentHistoryResource = new ConsentHistoryResource();
                consentHistoryResource.setDetailedConsentResource(detailedHistoryConsent);
                consentHistoryResource.setReason(amendmentReason);
                consentHistoryResource.setTimestamp(amendedTimestamp);

                boolean result = consentCoreService.storeConsentAmendmentHistory(consentID, consentHistoryResource,
                        detailedCurrentConsent);

                if (result) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Consent Amendment History of consentID: %s persisted successfully.",
                                consentID));
                    }
                } else {
                    log.error(String.format("Failed to persist Consent Amendment History of consentID : %s. " +
                            consentID));
                }
            } catch (ConsentManagementException e) {
                log.error("An error occurred while persisting consent amendment history data.", e);
            }
        }
    }
}
