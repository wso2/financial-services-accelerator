/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.keymanager.dto;

import org.json.JSONObject;

import java.util.Map;

/**
 * DTO for external API application update request.
 */
public class ExternalAPIApplicationUpdateRequestDTO {

    Map<String, Object> appData;
    Map<String, String> additionalProperties;
    JSONObject existingAppData;

    public ExternalAPIApplicationUpdateRequestDTO(Map<String, Object> appData,
                                                  Map<String, String> additionalProperties,
                                                  JSONObject existingAppData) {
        this.appData = appData;
        this.additionalProperties = additionalProperties;
        this.existingAppData = existingAppData;
    }

    public Map<String, Object> getAppData() {
        return appData;
    }

    public void setAppData(Map<String, Object> appData) {
        this.appData = appData;
    }

    public Map<String, String> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, String> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public JSONObject getExistingAppData() {
        return existingAppData;
    }

    public void setExistingAppData(JSONObject existingAppData) {
        this.existingAppData = existingAppData;
    }
}
