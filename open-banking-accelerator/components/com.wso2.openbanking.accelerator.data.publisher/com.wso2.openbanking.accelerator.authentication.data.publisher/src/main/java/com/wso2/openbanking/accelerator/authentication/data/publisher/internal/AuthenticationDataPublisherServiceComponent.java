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

package com.wso2.openbanking.accelerator.authentication.data.publisher.internal;

import com.wso2.openbanking.accelerator.authentication.data.publisher.service.AuthenticationDataPublisherService;
import com.wso2.openbanking.accelerator.authentication.data.publisher.service.AuthenticationDataPublisherServiceImpl;
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
import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistry;

/**
 * Method to register authentication data publisher OSGi Services.
 */
@Component(
        name = "com.wso2.open.banking.authentication.data.publisher",
        immediate = true
)
public class AuthenticationDataPublisherServiceComponent {

    private AuthenticationDataPublisherServiceImpl authenticationDataPublisherService;
    private JsFunctionRegistry jsFunctionRegistry;
    private static final Log log = LogFactory.getLog(AuthenticationDataPublisherServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            authenticationDataPublisherService = new AuthenticationDataPublisherServiceImpl();

            jsFunctionRegistry.register(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER, "publishAuthData",
                    (AuthenticationDataPublisherService) authenticationDataPublisherService::authDataExtractor);
        } catch (Throwable e) {
            log.error("Custom adaptive authentication function for data publishing activation failed", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Authentication Data Publisher component is activated successfully.");
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        if (jsFunctionRegistry != null) {
            jsFunctionRegistry.deRegister(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER, "publishAuthData");
        }
        if (log.isDebugEnabled()) {
            log.debug("Authentication Data Publisher component is deactivated.");
        }
    }

    @Reference(
            service = JsFunctionRegistry.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetJsFunctionRegistry"
    )
    public void setJsFunctionRegistry(JsFunctionRegistry jsFunctionRegistry) {

        this.jsFunctionRegistry = jsFunctionRegistry;
    }
    public void unsetJsFunctionRegistry(JsFunctionRegistry jsFunctionRegistry) {
        this.jsFunctionRegistry = null;
    }

    @Reference(
            service = OpenBankingConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigService"
    )
    public void setConfigService(OpenBankingConfigurationService openBankingConfigurationService) {

        AuthenticationDataPublisherDataHolder.getInstance()
                .setOpenBankingConfigurationService(openBankingConfigurationService);
    }
    public void unsetConfigService(OpenBankingConfigurationService openBankingConfigurationService) {

        AuthenticationDataPublisherDataHolder.getInstance()
                .setOpenBankingConfigurationService(openBankingConfigurationService);
    }
}
