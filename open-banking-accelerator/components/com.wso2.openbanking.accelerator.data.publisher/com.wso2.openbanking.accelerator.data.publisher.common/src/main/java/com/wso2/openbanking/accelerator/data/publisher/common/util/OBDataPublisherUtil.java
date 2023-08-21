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

package com.wso2.openbanking.accelerator.data.publisher.common.util;

import com.wso2.openbanking.accelerator.data.publisher.common.DataPublisherPool;
import com.wso2.openbanking.accelerator.data.publisher.common.EventQueue;
import com.wso2.openbanking.accelerator.data.publisher.common.OpenBankingDataPublisher;
import com.wso2.openbanking.accelerator.data.publisher.common.constants.DataPublishingConstants;
import com.wso2.openbanking.accelerator.data.publisher.common.internal.OBAnalyticsDataHolder;
import com.wso2.openbanking.accelerator.data.publisher.common.model.OBAnalyticsEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * Utility class for Data Publishing.
 */
public class OBDataPublisherUtil {

    private static final Log log = LogFactory.getLog(OBDataPublisherUtil.class);

    public static OpenBankingDataPublisher getDataPublisherInstance() {

        DataPublisherPool<OpenBankingDataPublisher> pool =
                OBAnalyticsDataHolder.getInstance().getDataPublisherPool();
        try {
            return pool.borrowObject();
        } catch (Exception e) {
            log.error("Error while receiving Thrift Data Publisher from the pool.");
        }
        return null;
    }

    public static void releaseDataPublishingInstance(OpenBankingDataPublisher instance) {

        OBAnalyticsDataHolder.getInstance().getDataPublisherPool().returnObject(instance);
        log.debug("Data publishing instance released to the pool");
    }

    /**
     * Util method to publish OB analytics data.
     * This method will put received data to an event queue and take care of asynchronous data publishing.
     */
    public static void publishData(String streamName, String streamVersion, Map<String, Object> analyticsData) {

        if (Boolean.parseBoolean((String) OBAnalyticsDataHolder.getInstance().getConfigurationMap()
                .get(DataPublishingConstants.DATA_PUBLISHING_ENABLED))) {

            EventQueue eventQueue = OBAnalyticsDataHolder.getInstance().getEventQueue();
            if (!(eventQueue == null)) {
                OBAnalyticsEvent event = new OBAnalyticsEvent(streamName, streamVersion, analyticsData);
                eventQueue.put(event);
            } else {
                log.error("Unable to get the event queue. Data publishing may be disabled.");
            }
        } else {
            log.debug("Data publishing is disabled. Failed to obtain a data publisher instance.");
        }
    }

}
