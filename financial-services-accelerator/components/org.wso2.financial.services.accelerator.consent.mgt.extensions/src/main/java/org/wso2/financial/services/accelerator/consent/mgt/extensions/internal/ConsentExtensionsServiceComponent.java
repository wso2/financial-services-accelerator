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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionExporter;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;

/**
 * The Component class for activating consent extensions osgi service.
 */
@Component(
    name = "org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsServiceComponent",
    immediate = true)
public class ConsentExtensionsServiceComponent {

    private static final Log log = LogFactory.getLog(ConsentExtensionsServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {
        log.debug("Consent Extensions component is activated");

        context.getBundleContext().registerService(ConsentExtensionExporter.class.getName(),
                ConsentExtensionExporter.getInstance(), null);
        log.debug("Consent extensions are registered successfully.");
    }

    @Reference(
            service = FinancialServicesConfigParser.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigService"
    )
    public void setConfigService(FinancialServicesConfigParser configurationService) {

        ConsentExtensionsDataHolder.getInstance().setConfigurationService(configurationService);
    }

    public void unsetConfigService(FinancialServicesConfigParser configurationService) {

        ConsentExtensionsDataHolder.getInstance().setConfigurationService(null);

    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        log.debug("Consent Extensions component is deactivated");
    }

    @Reference(
            service = org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConsentCoreService"
    )
    public void setConsentCoreService(ConsentCoreService consentCoreService) {

        log.debug("Setting the Consent Core Service");
        ConsentExtensionsDataHolder.getInstance().setConsentCoreService(consentCoreService);
    }

    public void unsetConsentCoreService(ConsentCoreService consentCoreService) {

        log.debug("UnSetting the Consent Core Service");
        ConsentExtensionsDataHolder.getInstance().setConsentCoreService(null);

    }
}
