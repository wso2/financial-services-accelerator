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

package com.wso2.openbanking.accelerator.gateway.internal;

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
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;

/**
 * Service class for executor core.
 */
@Component(
        name = "com.wso2.open.banking.common",
        immediate = true
)
public class GatewayServiceComponent {

    private static final Log log = LogFactory.getLog(GatewayServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        log.debug("Open banking gateway component is activated ");
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        log.debug("Open banking gateway component is deactivated ");
    }

    @Reference(
            service = OpenBankingConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigService"
    )
    public void setConfigService(OpenBankingConfigurationService openBankingConfigurationService) {

        GatewayDataHolder.getInstance().setOpenBankingConfigurationService(openBankingConfigurationService);
    }

    public void unsetConfigService(OpenBankingConfigurationService openBankingConfigurationService) {

        GatewayDataHolder.getInstance().setOpenBankingConfigurationService(null);
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
