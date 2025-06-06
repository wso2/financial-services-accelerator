/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.financial.services.accelerator.consent.mgt.extensions.authorize.model;

import java.util.Map;

/**
 * Wrapper for pre consent authorize flow external response data.
 */
public class ExternalAPIPreConsentAuthorizeResponseDTO {

    private Map<String, Object> consentData;
    private Map<String, Object> consumerData;
    private Map<String, Object> metadata;

    public Map<String, Object> getConsentData() {
        return consentData;
    }

    public void setConsentData(Map<String, Object> consentData) {
        this.consentData = consentData;
    }

    public Map<String, Object> getConsumerData() {
        return consumerData;
    }

    public void setConsumerData(Map<String, Object> consumerData) {
        this.consumerData = consumerData;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
