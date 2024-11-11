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
import com.wso2.openbanking.accelerator.consent.extensions.admin.builder.ConsentAdminBuilder;
import com.wso2.openbanking.accelerator.consent.extensions.authorize.builder.ConsentStepsBuilder;
import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentExtensionExporter;
import com.wso2.openbanking.accelerator.consent.extensions.manage.builder.ConsentManageBuilder;
import com.wso2.openbanking.accelerator.consent.extensions.validate.builder.ConsentValidateBuilder;
import com.wso2.openbanking.accelerator.consent.mgt.service.ConsentCoreService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Contains Data holder class for consent extensions.
 */
public class ConsentExtensionsDataHolder {

    private static Log log = LogFactory.getLog(ConsentExtensionsDataHolder.class);
    private static volatile ConsentExtensionsDataHolder instance;
    private OpenBankingConfigurationService openBankingConfigurationService;
    private ConsentCoreService consentCoreService;
    private RealmService realmService;
    private ConsentStepsBuilder consentStepsBuilder;
    private ConsentAdminBuilder consentAdminBuilder;
    private ConsentManageBuilder consentManageBuilder;
    private ConsentValidateBuilder consentValidateBuilder;
    private IdentityEventService identityEventService;

    // Prevent instantiation
    private ConsentExtensionsDataHolder() {}

    /**
     * Return a singleton instance of the data holder.
     *
     * @return A singleton instance of the data holder
     */
    public static synchronized ConsentExtensionsDataHolder getInstance() {
        if (instance == null) {
            synchronized (ConsentExtensionsDataHolder.class) {
                if (instance == null) {
                    instance = new ConsentExtensionsDataHolder();
                }
            }
        }
        return instance;
    }

    public OpenBankingConfigurationService getOpenBankingConfigurationService() {

        return openBankingConfigurationService;
    }

    public void setOpenBankingConfigurationService(
            OpenBankingConfigurationService openBankingConfigurationService) {

        this.openBankingConfigurationService = openBankingConfigurationService;

        ConsentStepsBuilder consentStepsBuilder = new ConsentStepsBuilder();
        consentStepsBuilder.build();
        this.setConsentStepsBuilder(consentStepsBuilder);
        ConsentExtensionExporter.setConsentStepsBuilder(consentStepsBuilder);

        ConsentAdminBuilder consentAdminBuilder = new ConsentAdminBuilder();
        consentAdminBuilder.build();
        this.setConsentAdminBuilder(consentAdminBuilder);
        ConsentExtensionExporter.setConsentAdminBuilder(consentAdminBuilder);

        ConsentManageBuilder consentManageBuilder = new ConsentManageBuilder();
        consentManageBuilder.build();
        this.setConsentManageBuilder(consentManageBuilder);
        ConsentExtensionExporter.setConsentManageBuilder(consentManageBuilder);

        ConsentValidateBuilder consentValidateBuilder = new ConsentValidateBuilder();
        consentValidateBuilder.build();
        this.setConsentValidateBuilder(consentValidateBuilder);
        ConsentExtensionExporter.setConsentValidateBuilder(consentValidateBuilder);
    }

    public ConsentStepsBuilder getConsentStepsBuilder() {
        return consentStepsBuilder;
    }

    public void setConsentStepsBuilder(ConsentStepsBuilder consentStepsBuilder) {
        this.consentStepsBuilder = consentStepsBuilder;
    }

    public ConsentAdminBuilder getConsentAdminBuilder() {
        return consentAdminBuilder;
    }

    public void setConsentAdminBuilder(ConsentAdminBuilder consentAdminBuilder) {
        this.consentAdminBuilder = consentAdminBuilder;
    }

    public ConsentManageBuilder getConsentManageBuilder() {
        return consentManageBuilder;
    }

    public void setConsentManageBuilder(ConsentManageBuilder consentManageBuilder) {
        this.consentManageBuilder = consentManageBuilder;
    }

    public ConsentValidateBuilder getConsentValidateBuilder() {
        return consentValidateBuilder;
    }

    public void setConsentValidateBuilder(ConsentValidateBuilder consentValidateBuilder) {
        this.consentValidateBuilder = consentValidateBuilder;
    }

    public ConsentCoreService getConsentCoreService() {
        return consentCoreService;
    }

    public void setConsentCoreService(ConsentCoreService consentCoreService) {
        this.consentCoreService = consentCoreService;
    }

    public IdentityEventService getIdentityEventService() {
        return identityEventService;
    }

    public void setIdentityEventService(IdentityEventService identityEventService) {
        this.identityEventService = identityEventService;
    }

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }
}
