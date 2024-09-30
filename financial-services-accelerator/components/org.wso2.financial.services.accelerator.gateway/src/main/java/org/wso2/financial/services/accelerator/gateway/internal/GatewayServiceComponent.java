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

package org.wso2.financial.services.accelerator.gateway.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigurationService;

/**
 * Service class for executor core
 */
@Component(
        name = "org.wso2.financial.services.accelerator.gateway.internal.GatewayServiceComponent",
        immediate = true
)
public class GatewayServiceComponent {

    private static final Log log = LogFactory.getLog(GatewayServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        log.debug("Financial services gateway component is activated ");
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        log.debug("Financial services gateway component is deactivated ");
    }

    @Reference(
            service = FinancialServicesConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigService"
    )
    public void setConfigService(FinancialServicesConfigurationService configurationService) {

        GatewayDataHolder.getInstance().setFinancialServicesConfigurationService(configurationService);
    }

    public void unsetConfigService(FinancialServicesConfigurationService configurationService) {

        GatewayDataHolder.getInstance().setFinancialServicesConfigurationService(null);
    }

    @Reference(
            service = APIManagerConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetAPIMConfigs"
    )
    public void setAPIMConfig(APIManagerConfigurationService apManagerConfigurationService) {

        GatewayDataHolder.getInstance().setApiManagerConfiguration(apManagerConfigurationService);
    }

    public void unSetAPIMConfigs(APIManagerConfigurationService apManagerConfigurationService) {

        GatewayDataHolder.getInstance().setApiManagerConfiguration(apManagerConfigurationService);
    }
}
