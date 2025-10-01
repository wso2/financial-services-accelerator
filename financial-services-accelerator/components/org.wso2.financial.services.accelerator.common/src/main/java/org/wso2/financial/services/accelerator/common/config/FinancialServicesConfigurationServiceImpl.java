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

package org.wso2.financial.services.accelerator.common.config;

import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of Financial Services Configuration Service.
 */
public class FinancialServicesConfigurationServiceImpl implements FinancialServicesConfigurationService {

    private static final FinancialServicesConfigParser configParser = FinancialServicesConfigParser.getInstance();

    @Override
    public Map<String, Object> getConfigurations() {

        return configParser.getConfiguration();
    }

    public Map<String, Object> getServiceExtensionConfigs() {

        Map<String, Object> serviceExtensionConfigs = new HashMap<String, Object>();
        serviceExtensionConfigs.put(FinancialServicesConstants.SERVICE_EXTENSIONS_ENDPOINT_ENABLED,
                configParser.isServiceExtensionsEndpointEnabled());
        serviceExtensionConfigs.put(FinancialServicesConstants.SERVICE_EXTENSIONS_ENDPOINT_BASE_URL,
                configParser.getServiceExtensionsEndpointBaseUrl());
        serviceExtensionConfigs.put(FinancialServicesConstants.SERVICE_EXTENSIONS_EXTENSION,
                configParser.getServiceExtensionTypes());
        return serviceExtensionConfigs;
    }

    @Override
    public Map<String, Map<Integer, String>> getExecutors() {

        return configParser.getFinancialServicesExecutors();
    }

    @Override
    public Map<String, Map<Integer, String>> getAuthorizeSteps() {

        return configParser.getConsentAuthorizeSteps();
    }

    @Override
    public Map<String, Map<Integer, String>> getDataPublishingStreams() {

        return configParser.getDataPublishingStreams();
    }

    @Override
    public Map<String, Map<String, Object>> getDataPublishingValidationMap() {

        return configParser.getDataPublishingValidationMap();
    }

    @Override
    public Map<String, Map<String, Object>> getDCRParamsConfig() {

        return configParser.getDCRParamsConfig();
    }

    @Override
    public Map<String, Map<String, Object>> getDCRValidatorsConfig() {

        return configParser.getDCRValidatorsConfig();
    }

    @Override
    public List<String> getDCRResponseParameters() {

        return configParser.getDCRResponseParameters();
    }

    public Map<String, Map<String, String>> getKeyManagerConfigs() {

        return configParser.getKeyManagerAdditionalProperties();
    }
}
