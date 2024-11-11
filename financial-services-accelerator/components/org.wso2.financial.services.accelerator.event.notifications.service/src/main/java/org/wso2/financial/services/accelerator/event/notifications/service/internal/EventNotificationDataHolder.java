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

package org.wso2.financial.services.accelerator.event.notifications.service.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigurationService;
import org.wso2.financial.services.accelerator.event.notifications.service.model.RealtimeEventNotification;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Data holder for Open Banking Event Notifications.
 */
public class EventNotificationDataHolder {
    private static Log log = LogFactory.getLog(EventNotificationDataHolder.class);
    private static volatile EventNotificationDataHolder instance;
    private volatile LinkedBlockingQueue<RealtimeEventNotification> realtimeEventNotificationQueue;
    private FinancialServicesConfigurationService configService;

    private EventNotificationDataHolder() {
        this.realtimeEventNotificationQueue = new LinkedBlockingQueue<>();
    }

    /**
     * Return a singleton instance of the data holder.
     *
     * @return A singleton instance of the data holder
     */
    public static synchronized EventNotificationDataHolder getInstance() {
        if (instance == null) {
            synchronized (EventNotificationDataHolder.class) {
                if (instance == null) {
                    instance = new EventNotificationDataHolder();
                }
            }
        }
        return instance;
    }

    public LinkedBlockingQueue<RealtimeEventNotification> getRealtimeEventNotificationQueue() {
        return realtimeEventNotificationQueue;
    }

    public void setRealtimeEventNotificationQueue(LinkedBlockingQueue<RealtimeEventNotification> queue) {
        this.realtimeEventNotificationQueue = queue;
    }
}
