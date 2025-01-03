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

package com.wso2.openbanking.accelerator.consent.extensions.internal;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigurationService;
import com.wso2.openbanking.accelerator.consent.extensions.ciba.authenticator.weblink.CIBAWebLinkAuthenticator;
import com.wso2.openbanking.accelerator.consent.extensions.ciba.authenticator.weblink.notification.CIBAWebLinkNotificationHandler;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionExporter;
import com.wso2.openbanking.accelerator.consent.extensions.util.PeriodicalConsentJobActivator;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * The Component class for activating consent extensions osgi service.
 */
@Component(
        name = "com.wso2.openbanking.accelerator.consent.extensions.internal.ConsentExtensionsComponent",
        immediate = true)
public class ConsentExtensionsComponent {
    private static Log log = LogFactory.getLog(ConsentExtensionsComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        context.getBundleContext().registerService(ConsentExtensionExporter.class.getName(),
                ConsentExtensionExporter.getInstance(), null);
        if (log.isDebugEnabled()) {
            log.debug("Consent extensions are registered successfully.");
        }
        new PeriodicalConsentJobActivator().activate();
        if (log.isDebugEnabled()) {
            log.debug("Periodical Consent Status Updater Started");
        }
        CIBAWebLinkAuthenticator cibaWebLinkAuthenticator = new CIBAWebLinkAuthenticator();
        context.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                cibaWebLinkAuthenticator, null);
        context.getBundleContext().registerService(AbstractEventHandler.class.getName(),
                new CIBAWebLinkNotificationHandler(), null);
        if (log.isDebugEnabled()) {
            log.debug("CIBA Push authenticator bundle is activated");
        }

    }

    @Reference(
            service = OpenBankingConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigService"
    )
    public void setConfigService(OpenBankingConfigurationService openBankingConfigurationService) {

        ConsentExtensionsDataHolder.getInstance().setOpenBankingConfigurationService(openBankingConfigurationService);
    }

    public void unsetConfigService(OpenBankingConfigurationService openBankingConfigurationService) {

        ConsentExtensionsDataHolder.getInstance().setOpenBankingConfigurationService(openBankingConfigurationService);

    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        log.debug("Open banking Consent Extensions component is deactivated");
    }

    @Reference(
            service = com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService.class,
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

    @Reference(
            name = "EventMgtService",
            service = IdentityEventService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdentityEventService"
    )
    protected void setIdentityEventService(IdentityEventService eventService) {
        ConsentExtensionsDataHolder.getInstance().setIdentityEventService(eventService);
    }

    protected void unsetIdentityEventService(IdentityEventService eventService) {

        ConsentExtensionsDataHolder.getInstance().setIdentityEventService(null);
    }

    @Reference(
            name = "realm.service",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService"
    )
    protected void setRealmService(RealmService realmService) {

        log.debug("Setting the Realm Service");
        ConsentExtensionsDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        log.debug("UnSetting the Realm Service");
        ConsentExtensionsDataHolder.getInstance().setRealmService(null);
    }
}
