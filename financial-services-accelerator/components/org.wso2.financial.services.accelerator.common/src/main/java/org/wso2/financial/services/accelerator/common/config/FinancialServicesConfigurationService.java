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

import java.util.List;
import java.util.Map;

/**
 * Interface to expose Configurations as an OSGi Service.
 */
public interface FinancialServicesConfigurationService {

    public Map<String, Object> getConfigurations();

    public Map<String, Object> getServiceExtensionConfigs();

    public Map<String, Map<Integer, String>> getExecutors();

    public Map<String, Map<Integer, String>> getAuthorizeSteps();

    public Map<String, Map<String, Object>> getDCRValidatorsConfig();

    public Map<String, Map<String, Object>> getDCRParamsConfig();

    public List<String> getDCRResponseParameters();

    public Map<String, Map<String, String>> getKeyManagerConfigs();

}
