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

package com.wso2.openbanking.accelerator.data.publisher.common.internal;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigurationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Service class for Open Banking Data Publishing Component.
 */
@Component(
        name = "com.wso2.openbanking.accelerator.data.publisher.common.internal.OBAnalyticsServiceComponent",
        immediate = true
)
public class OBAnalyticsServiceComponent {

    private static final Log log = LogFactory.getLog(OBAnalyticsServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        OBAnalyticsDataHolder.getInstance().initializePool();
        OBAnalyticsDataHolder.getInstance().initializeEventQueue();
        log.debug("Open banking data publishing component is activated ");
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        OBAnalyticsDataHolder.getInstance().closePool();
        log.debug("Open banking data publishing component is deactivated ");
    }

    @Reference(
            service = OpenBankingConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigService"
    )
    public void setConfigService(OpenBankingConfigurationService openBankingConfigurationService) {

        OBAnalyticsDataHolder.getInstance().setOpenBankingConfigurationService(openBankingConfigurationService);
    }

    public void unsetConfigService(OpenBankingConfigurationService openBankingConfigurationService) {

        OBAnalyticsDataHolder.getInstance().setOpenBankingConfigurationService(openBankingConfigurationService);
    }
}
