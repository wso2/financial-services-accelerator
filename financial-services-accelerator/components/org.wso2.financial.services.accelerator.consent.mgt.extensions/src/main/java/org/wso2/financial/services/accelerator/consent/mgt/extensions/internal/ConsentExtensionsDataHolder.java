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
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigurationService;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.builder.ConsentAdminBuilder;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.builder.ConsentStepsBuilder;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionExporter;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.builder.ConsentManageBuilder;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.builder.ConsentValidateBuilder;
import org.wso2.financial.services.accelerator.consent.mgt.service.ConsentCoreService;

import java.security.KeyStore;

/**
 * Contains Data holder class for consent extensions.
 */
public class ConsentExtensionsDataHolder {

    private static Log log = LogFactory.getLog(ConsentExtensionsDataHolder.class);
    private static volatile ConsentExtensionsDataHolder instance;
    private FinancialServicesConfigurationService configurationService;
    private ConsentCoreService consentCoreService;
    private ConsentStepsBuilder consentStepsBuilder;
    private ConsentManageBuilder consentManageBuilder;
    private ConsentValidateBuilder consentValidateBuilder;
    private ConsentAdminBuilder consentAdminBuilder;
    private KeyStore trustStore = null;

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

    public FinancialServicesConfigurationService getConfigurationService() {

        return configurationService;
    }

    public void setConfigurationService(FinancialServicesConfigurationService configurationService) {

        this.configurationService = configurationService;

        if (configurationService != null) {
            ConsentStepsBuilder consentStepsBuilder = new ConsentStepsBuilder();
            this.setConsentStepsBuilder(consentStepsBuilder);
            ConsentExtensionExporter.setConsentStepsBuilder(consentStepsBuilder);

            ConsentManageBuilder consentManageBuilder = new ConsentManageBuilder();
            this.setConsentManageBuilder(consentManageBuilder);
            ConsentExtensionExporter.setConsentManageBuilder(consentManageBuilder);

            ConsentValidateBuilder consentValidateBuilder = new ConsentValidateBuilder();
            this.setConsentValidateBuilder(consentValidateBuilder);
            ConsentExtensionExporter.setConsentValidateBuilder(consentValidateBuilder);

            ConsentAdminBuilder consentAdminBuilder = new ConsentAdminBuilder();
            consentAdminBuilder.build();
            this.setConsentAdminBuilder(consentAdminBuilder);
            ConsentExtensionExporter.setConsentAdminBuilder(consentAdminBuilder);
        }
    }

    public ConsentManageBuilder getConsentManageBuilder() {
        return consentManageBuilder;
    }

    public void setConsentManageBuilder(ConsentManageBuilder consentManageBuilder) {
        this.consentManageBuilder = consentManageBuilder;
    }

    public ConsentStepsBuilder getConsentStepsBuilder() {
        return consentStepsBuilder;
    }

    public void setConsentStepsBuilder(ConsentStepsBuilder consentStepsBuilder) {
        this.consentStepsBuilder = consentStepsBuilder;
    }

    public ConsentValidateBuilder getConsentValidateBuilder() {
        return consentValidateBuilder;
    }

    public void setConsentValidateBuilder(ConsentValidateBuilder consentValidateBuilder) {
        this.consentValidateBuilder = consentValidateBuilder;
    }

    public ConsentAdminBuilder getConsentAdminBuilder() {
        return consentAdminBuilder;
    }

    public void setConsentAdminBuilder(ConsentAdminBuilder consentAdminBuilder) {
        this.consentAdminBuilder = consentAdminBuilder;
    }

    public ConsentCoreService getConsentCoreService() {
        return consentCoreService;
    }

    public void setConsentCoreService(ConsentCoreService consentCoreService) {
        this.consentCoreService = consentCoreService;
    }

    public KeyStore getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(KeyStore trustStore) {
        this.trustStore = trustStore;
    }
}
