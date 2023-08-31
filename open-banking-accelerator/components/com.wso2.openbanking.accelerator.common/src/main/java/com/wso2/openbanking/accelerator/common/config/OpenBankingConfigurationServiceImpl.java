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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;

/**
 * Implementation of Open Banking Configuration Service.
 */
public class OpenBankingConfigurationServiceImpl implements OpenBankingConfigurationService {

    private static final OpenBankingConfigParser openBankingConfigParser = OpenBankingConfigParser.getInstance();
    private static final Log log = LogFactory.getLog(OpenBankingConfigurationServiceImpl.class);

    @Override
    public Map<String, Object> getConfigurations() {

        return openBankingConfigParser.getConfiguration();
    }

    @Override
    public Map<String, Map<Integer, String>> getExecutors() {

        return openBankingConfigParser.getOpenBankingExecutors();
    }

    @Override
    public Map<String, Map<Integer, String>> getDataPublishingStreams() {

        return openBankingConfigParser.getDataPublishingStreams();
    }

    @Override
    public Map<String, Map<String, Object>> getDataPublishingValidationMap() {

        return openBankingConfigParser.getDataPublishingValidationMap();
    }

    @Override
    public Map<String, Map<String, Object>> getDCRRegistrationConfigurations() {

        return openBankingConfigParser.getOpenBankingDCRRegistrationParams();
    }

    @Override
    public Map<String, Map<Integer, String>> getAuthorizeSteps() {

        return openBankingConfigParser.getConsentAuthorizeSteps();
    }

    @Override
    public Map<String, List<String>> getAllowedScopes() {
        return openBankingConfigParser.getAllowedScopes();
    }

    @Override
    public Map<String, List<String>> getAllowedAPIs() {
        return openBankingConfigParser.getAllowedAPIs();
    }

    @Override
    public Map<String, String> getAuthenticationWorkers() {
        return openBankingConfigParser.getAuthWorkerConfig();
    }


}
