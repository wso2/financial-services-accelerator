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

package com.wso2.openbanking.accelerator.common.config;

import java.util.List;
import java.util.Map;

/**
 * Interface to expose Configurations as an OSGi Service.
 */
public interface OpenBankingConfigurationService {

    public Map<String, Object> getConfigurations();

    public Map<String, Map<Integer, String>> getExecutors();

    public Map<String, Map<Integer, String>> getDataPublishingStreams();

    public Map<String, Map<String, Object>> getDataPublishingValidationMap();

    public Map<String, Map<String, Object>> getDCRRegistrationConfigurations();

    public Map<String, Map<Integer, String>> getAuthorizeSteps();

    public Map<String, List<String>> getAllowedScopes();

    public Map<String, List<String>> getAllowedAPIs();

    public Map<String, String> getAuthenticationWorkers();

}
