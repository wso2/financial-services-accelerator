/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.consent.mgt.extensions.event.executor;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.event.executor.FSEventExecutor;
import org.wso2.financial.services.accelerator.common.event.executor.model.FSEvent;
import org.wso2.financial.services.accelerator.common.exception.ConsentManagementException;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.AuthorizationResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.ConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.dao.models.DetailedConsentResource;
import org.wso2.financial.services.accelerator.consent.mgt.service.impl.ConsentCoreServiceImpl;
import org.wso2.financial.services.accelerator.data.publisher.constants.DataPublishingConstants;
import org.wso2.financial.services.accelerator.data.publisher.util.FSDataPublisherUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * CDS event executor implementation to execute consent state change related events.
 */
public class ConsentLCEventExecutor implements FSEventExecutor {

    private static final Log log = LogFactory.getLog(org.wso2.financial.services.accelerator.consent.mgt.extensions.
            event.executor.ConsentLCEventExecutor.class);
    private ConsentCoreServiceImpl consentCoreService = new ConsentCoreServiceImpl();
    private static final String CONSENT_ID = "ConsentId";
    private static final String CONSENT_DATA_MAP = "ConsentDataMap";
    private static final String CONSENT_RESOURCE = "ConsentResource";
    private static final String DETAILED_CONSENT_RESOURCE = "DetailedConsentResource";
    private static final String REQUEST_URI_KEY = "requestUriKey";

    private static final String CONSENT_ID_KEY = "consentId";
    private static final String CONSENT_DETAILS_KEY = "consentDetails";
    private static final String CURRENT_STATUS_KEY = "currentStatus";
    private static final String PREVIOUS_STATUS_KEY = "previousStatus";
    public static final  String AUTH_RESOURCE_TYPE_PRIMARY = "primary_member";

    private static final Cache<String, Boolean> publishedEventIdentifierCache =
            CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).maximumSize(20).build();

    @Override
    public void processEvent(FSEvent fsEvent) {

        Map<String, Object> eventData = fsEvent.getEventData();


        HashMap<String, Object> consentDataMap = (HashMap<String, Object>) eventData
                .get(CONSENT_DATA_MAP);
        ConsentResource consentResource = (ConsentResource) consentDataMap
                .get(CONSENT_RESOURCE);
        DetailedConsentResource detailedConsentResource = (DetailedConsentResource) consentDataMap
                .get(DETAILED_CONSENT_RESOURCE);

        log.debug("Publishing consent data for metrics.");

        String consentId = (String) eventData.get(CONSENT_ID);
        String primaryUserId;
        try {
            primaryUserId = getPrimaryUserForConsent(detailedConsentResource, consentId);
        } catch (ConsentManagementException e) {
            log.error("Error while trying to retrieve consent data", e);
            return;
        }

        if (StringUtils.isBlank(primaryUserId)) {
            return;
        }

        HashMap<String, Object> consentData = new HashMap<>();
        String eventType = fsEvent.getEventType();
        consentData.put(CONSENT_ID_KEY, consentId);
        consentData.put(CONSENT_DETAILS_KEY, detailedConsentResource);
        consentData.put(CURRENT_STATUS_KEY, fsEvent.getEventType());
        consentData.put(PREVIOUS_STATUS_KEY, consentResource.getCurrentStatus());

        String eventIdentifier = getEventIdentifier(consentResource, detailedConsentResource, eventType);
        if (eventIdentifier != null) {
            synchronized (publishedEventIdentifierCache) {
                if (publishedEventIdentifierCache.getIfPresent(eventIdentifier) != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Skipping authorisation data publishing for event identifier: " +
                                eventIdentifier.replaceAll("[\r\n]", "") +
                                " as it has already been published.");
                    }
                    return;
                }
                publishedEventIdentifierCache.put(eventIdentifier, Boolean.TRUE);
            }
        }
        //publish consent lifecycle data
        publishReportingData(consentData);
    }

    /**
     * Publish reporting data related to consent lifecycle state changes
     *
     * @param requestData  data map to publish
     */
    private void publishReportingData(Map<String, Object> requestData) {

        if (Boolean.parseBoolean((String) FinancialServicesConfigParser.getInstance().getConfiguration()
                .get(DataPublishingConstants.DATA_PUBLISHING_ENABLED))) {
            FSDataPublisherUtil.publishData("ConsentLCEventStream", "1.0.0", requestData);

        } else {
            log.debug("Data publishing is disabled");
        }
    }

    private String getPrimaryUserForConsent(DetailedConsentResource detailedConsentResource, String consentId)
            throws ConsentManagementException {

        String primaryUser = null;
        if (detailedConsentResource == null) {
            detailedConsentResource = this.consentCoreService.getDetailedConsent(consentId);
        }

        ArrayList<AuthorizationResource> authorizationResources = detailedConsentResource.getAuthorizationResources();
        for (AuthorizationResource authorizationResource : authorizationResources) {
            if (AUTH_RESOURCE_TYPE_PRIMARY
                    .equals(authorizationResource.getAuthorizationType())) {
                primaryUser = authorizationResource.getUserID();
            }
        }
        return primaryUser;
    }


    /**
     * Method to get the issued from the current time.
     *
     * @param currentTime Current time in milliseconds (unix timestamp format)
     * @return issued time in seconds (unix timestamp format)
     */
    protected static long getIatFromCurrentTime(long currentTime) {

        return currentTime / 1000;
    }

    /**
     * Method to get the expiry time when the current time is given.
     *
     * @param currentTime Current time in milliseconds (unix timestamp format)
     * @return expiry time in seconds (unix timestamp format)
     */
    protected static long getExpFromCurrentTime(long currentTime) {
        // (current time + 5 minutes) is the expiry time.
        return (currentTime / 1000) + 300;
    }

    private String getEventIdentifier(ConsentResource consentResource, DetailedConsentResource detailedConsentResource,
                                      String eventType) {

        Map<String, String> consentAttributes = null;
        if (consentResource != null) {
            consentAttributes = consentResource.getConsentAttributes();
        } else if (detailedConsentResource != null) {
            consentAttributes = detailedConsentResource.getConsentAttributes();
        }
        return consentAttributes != null ? (consentAttributes.get(REQUEST_URI_KEY) + ":" + eventType) : null;
    }


}
