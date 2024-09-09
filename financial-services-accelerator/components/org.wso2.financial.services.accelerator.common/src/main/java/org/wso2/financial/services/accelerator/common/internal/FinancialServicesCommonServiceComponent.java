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

package org.wso2.financial.services.accelerator.common.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigurationService;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigurationServiceImpl;

/**
 * Method to register Financial Services common OSGi Services.
 */
@Component(
        name = "org.wso2.financial.services.accelerator.common",
        immediate = true
)
public class FinancialServicesCommonServiceComponent {

    private static final Log log = LogFactory.getLog(FinancialServicesCommonServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        FinancialServicesConfigurationService openBankingConfigurationService
                = new FinancialServicesConfigurationServiceImpl();
        context.getBundleContext().registerService(FinancialServicesConfigurationService.class.getName(),
                openBankingConfigurationService, null);
        context.getBundleContext().registerService(ApplicationManagementService.class,
                ApplicationManagementService.getInstance(), null);

        log.debug("Financial Services common component is activated successfully");
    }

    @Reference(
            name = "ApplicationManagementService",
            service = ApplicationManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetApplicationManagementService"
    )
    protected void setApplicationManagementService(ApplicationManagementService mgtService) {

        FinancialServicesCommonDataHolder.getInstance().setApplicationManagementService(mgtService);
    }

    protected void unsetApplicationManagementService(ApplicationManagementService mgtService) {

        FinancialServicesCommonDataHolder.getInstance().setApplicationManagementService(null);
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        log.debug("Financial Services common component is deactivated");
    }
}
