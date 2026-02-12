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

package org.wso2.financial.services.accelerator.data.publisher.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigurationService;


/**
 * Service class for Open Banking Data Publishing Component.
 */
@Component(
        name = "org.wso2.financial.services.accelerator.data.publisher.common.internal.OBAnalyticsServiceComponent",
        immediate = true
)
public class FSAnalyticsServiceComponent {

    private static final Log log = LogFactory.getLog(FSAnalyticsServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        FSAnalyticsDataHolder.getInstance().initializePool();
        FSAnalyticsDataHolder.getInstance().initializeEventQueue();
        log.debug("Open banking data publishing component is activated ");
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        FSAnalyticsDataHolder.getInstance().closePool();
        log.debug("Open banking data publishing component is deactivated ");
    }

    @Reference(
            service = FinancialServicesConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigService"
    )
    public void setConfigService(FinancialServicesConfigurationService financialServicesConfigurationService) {

        FSAnalyticsDataHolder.getInstance().
                setFinancialServicesConfigurationService(financialServicesConfigurationService);
    }

    public void unsetConfigService(FinancialServicesConfigurationService financialServicesConfigurationService) {

        FSAnalyticsDataHolder.getInstance().
                setFinancialServicesConfigurationService(financialServicesConfigurationService);
    }
}
