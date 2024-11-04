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
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.event.notifications.service.realtime.service.RealtimeEventNotificationLoaderService;
import org.wso2.financial.services.accelerator.event.notifications.service.realtime.util.activator.PeriodicalEventNotificationConsumerJobActivator;

/**
 * The Component class for activating event notification osgi service.
 */
@Component(
    name = "org.wso2.financial.services.accelerator.event.notifications.service.internal.EventNotificationComponent",
    immediate = true)
public class EventNotificationComponent {
    private static Log log = LogFactory.getLog(EventNotificationComponent.class);

    @Activate
    protected void activate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Event Notification Service Component Activated");
        }

        // Check if realtime event notification enabled
        if (FinancialServicesConfigParser.getInstance().isRealtimeEventNotificationEnabled()) {
            /*
             * Initialize the blocking queue for storing the realtime event notifications
             * Initialize the quartz job for consuming the realtime event notifications
             * Initialize the thread for producing the open state realtime event notifications
             */
            new Thread(new RealtimeEventNotificationLoaderService()).start();
            new PeriodicalEventNotificationConsumerJobActivator().activate();
        }
    }
}
