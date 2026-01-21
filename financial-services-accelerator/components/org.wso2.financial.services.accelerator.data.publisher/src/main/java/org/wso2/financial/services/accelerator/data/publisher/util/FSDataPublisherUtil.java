/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.data.publisher.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.data.publisher.DataPublisherPool;
import org.wso2.financial.services.accelerator.data.publisher.EventQueue;
import org.wso2.financial.services.accelerator.data.publisher.FinancialServicesDataPublisher;
import org.wso2.financial.services.accelerator.data.publisher.constants.DataPublishingConstants;
import org.wso2.financial.services.accelerator.data.publisher.internal.FSAnalyticsDataHolder;
import org.wso2.financial.services.accelerator.data.publisher.model.FSAnalyticsEvent;


import java.util.Map;

/**
 * Utility class for Data Publishing.
 */
public class FSDataPublisherUtil {

    private static final Log log = LogFactory.getLog(FSDataPublisherUtil.class);

    public static FinancialServicesDataPublisher getDataPublisherInstance() {

        DataPublisherPool<FinancialServicesDataPublisher> pool =
                FSAnalyticsDataHolder.getInstance().getDataPublisherPool();
        try {
            return pool.borrowObject();
        } catch (Exception e) {
            log.error("Error while receiving Thrift Data Publisher from the pool.");
        }
        return null;
    }

    public static void releaseDataPublishingInstance(FinancialServicesDataPublisher instance) {

        FSAnalyticsDataHolder.getInstance().getDataPublisherPool().returnObject(instance);
        log.debug("Data publishing instance released to the pool");
    }

    /**
     * Util method to publish OB analytics data.
     * This method will put received data to an event queue and take care of asynchronous data publishing.
     */
    public static void publishData(String streamName, String streamVersion, Map<String, Object> analyticsData) {

        // Analytics data will be added to the OB analytics logfile for processing if ELK is configured for the server.
        if (Boolean.parseBoolean((String) FinancialServicesConfigParser.getInstance().getConfiguration()
                .get(DataPublishingConstants.ELK_ANALYTICS_ENABLED))) {
            try {
                LogsPublisherUtil.addAnalyticsLogs(DataPublishingConstants.LOG_FILE_NAME, streamName,
                        streamVersion, analyticsData);
            } catch (FinancialServicesException e) {
                log.error("Error occurred while writing analytics logs", e);
            }
        }

        if (Boolean.parseBoolean((String) FSAnalyticsDataHolder.getInstance().getConfigurationMap()
                .get(DataPublishingConstants.DATA_PUBLISHING_ENABLED))) {

            EventQueue eventQueue = FSAnalyticsDataHolder.getInstance().getEventQueue();
            if (!(eventQueue == null)) {
                FSAnalyticsEvent event = new FSAnalyticsEvent(streamName, streamVersion, analyticsData);
                eventQueue.put(event);
            } else {
                log.error("Unable to get the event queue. Data publishing may be disabled.");
            }
        } else {
            log.debug("Data publishing is disabled. Failed to obtain a data publisher instance.");
        }
    }

}
