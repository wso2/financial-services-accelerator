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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.builder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigurationService;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.ConsentValidator;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.impl.ConsentValidatorServiceExtension;

import java.util.List;
import java.util.Map;

/**
 * Builder class for consent validator.
 */
public class ConsentValidateBuilder {

    private static final Log log = LogFactory.getLog(ConsentValidateBuilder.class);
    private ConsentValidator consentValidator = null;
    private String requestSignatureAlias = null;
    private String serviceEndpointBaseUrl = null;

    public ConsentValidateBuilder() {
        build();
    }

    private void build() {

        FinancialServicesConfigurationService configurationService = ConsentExtensionsDataHolder.getInstance()
                .getConfigurationService();
        Map<String, Object> configs =  configurationService.getConfigurations();
        Map<String, Object> serviceExtensionConfigs =  configurationService.getServiceExtensionConfigs();
        List<ServiceExtensionTypeEnum> supportedServiceTypes = (List<ServiceExtensionTypeEnum>)
                serviceExtensionConfigs.get(FinancialServicesConstants.SERVICE_EXTENSIONS_ENDPOINT_TYPE);

        if ((boolean) serviceExtensionConfigs.get(FinancialServicesConstants.SERVICE_EXTENSIONS_ENDPOINT_ENABLED) &&
                supportedServiceTypes.contains(ServiceExtensionTypeEnum.CONSENT_VALIDATION)) {
            log.debug("Service extensions endpoint is enabled. Loading configurations from service extensions.");
            consentValidator = new ConsentValidatorServiceExtension();
            serviceEndpointBaseUrl = (String) serviceExtensionConfigs
                    .get(FinancialServicesConstants.SERVICE_EXTENSIONS_ENDPOINT_BASE_URL);
        } else {
            String handlerConfig = (String)  configs.get(FinancialServicesConstants.CONSENT_VALIDATOR);
            consentValidator = FinancialServicesUtils.getClassInstanceFromFQN(handlerConfig, ConsentValidator.class);
        }
        requestSignatureAlias = (String) configs.get(FinancialServicesConstants.SIGNATURE_ALIAS);
        log.debug("Validate handler loaded successfully");
    }

    public ConsentValidator getConsentValidator() {
        return consentValidator;
    }

    public String getRequestSignatureAlias() {
        return requestSignatureAlias;
    }
}
