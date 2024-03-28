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

package com.wso2.openbanking.accelerator.event.notifications.service.internal;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigurationService;
import com.wso2.openbanking.accelerator.event.notifications.service.realtime.service.RealtimeEventNotificationLoaderService;
import com.wso2.openbanking.accelerator.event.notifications.service.realtime.util.
        activator.PeriodicalEventNotificationConsumerJobActivator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.oauth2.OAuth2Service;

/**
 * The Component class for activating event notification osgi service.
 */
@Component(
        name = "com.wso2.openbanking.accelerator.event.notifications.service.internal.EventNotificationComponent",
        immediate = true)
public class EventNotificationComponent {
    private static Log log = LogFactory.getLog(EventNotificationComponent.class);

    @Activate
    protected void activate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Event Notification Service Component Activated");
        }

        // Check if realtime event notification enabled
        if (OpenBankingConfigParser.getInstance().isRealtimeEventNotificationEnabled()) {
            /*
             * Initialize the blocking queue for storing the realtime event notifications
             * Initialize the quartz job for consuming the realtime event notifications
             * Initialize the thread for producing the open state realtime event notifications
             */
            new Thread(new RealtimeEventNotificationLoaderService()).start();
            new PeriodicalEventNotificationConsumerJobActivator().activate();
        }
    }

    /**
     * Setters for the descendent OSGI services of the EventNotificationComponent.
     * This is added to run the EventNotification OSGI component after the Common module
     * @param openBankingConfigurationService OpenBankingConfigurationService
     */
    @Reference(
            service = OpenBankingConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigService"
    )
    public void setConfigService(OpenBankingConfigurationService openBankingConfigurationService) {
        EventNotificationDataHolder.getInstance().setOpenBankingConfigurationService(openBankingConfigurationService);
    }

    public void unsetConfigService(OpenBankingConfigurationService openBankingConfigurationService) {
        EventNotificationDataHolder.getInstance().setOpenBankingConfigurationService(null);
    }

    /**
     * Setters for the descendent OSGI services of the EventNotificationComponent.
     * This is added to run the EventNotification OSGI component after the OAuth2Service
     */
    @Reference(
            service = OAuth2Service.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOAuth2Service"
    )

    /**
     * Setters for the descendent OSGI services of the EventNotificationComponent.
     * @param oAuth2Service OAuth2Service
     */
    public void setOAuth2Service(OAuth2Service oAuth2Service) {
    }

    public void unsetOAuth2Service(OAuth2Service oAuth2Service) {
    }
}
