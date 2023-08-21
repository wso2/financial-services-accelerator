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

package com.wso2.openbanking.accelerator.common.internal;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigurationService;
import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigurationServiceImpl;
import com.wso2.openbanking.accelerator.common.event.executor.OBEventQueue;
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

/**
 * Method to register Open Banking common OSGi Services.
 */
@Component(
        name = "com.wso2.open.banking.common",
        immediate = true
)
public class OpenBankingCommonServiceComponent {

    private static final Log log = LogFactory.getLog(OpenBankingCommonServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        OpenBankingConfigurationService openBankingConfigurationService
                = new OpenBankingConfigurationServiceImpl();
        OpenBankingCommonDataHolder openBankingCommonDataHolder = OpenBankingCommonDataHolder.getInstance();
        context.getBundleContext().registerService(OpenBankingConfigurationService.class.getName(),
                openBankingConfigurationService, null);
        context.getBundleContext().registerService(OBEventQueue.class.getName(),
                openBankingCommonDataHolder.getOBEventQueue(), null);
        context.getBundleContext().registerService(ApplicationManagementService.class,
                ApplicationManagementService.getInstance(), null);

        log.debug("Open banking common component is activated successfully");
    }

    @Reference(
            name = "ApplicationManagementService",
            service = ApplicationManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetApplicationManagementService"
    )
    protected void setApplicationManagementService(ApplicationManagementService mgtService) {

        OpenBankingCommonDataHolder.getInstance().setApplicationManagementService(mgtService);
    }

    protected void unsetApplicationManagementService(ApplicationManagementService mgtService) {

        OpenBankingCommonDataHolder.getInstance().setApplicationManagementService(null);
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        log.debug("Open banking common component is deactivated");
    }
}
