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

package org.wso2.financial.services.accelerator.consent.mgt.api.dao.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Model for consent attributes.
 */
public class ConsentAttributes {

    private String consentId;
    private Map<String, Object> consentAttributes;

    public ConsentAttributes() {

    }

    public ConsentAttributes(String consentId, Map<String, Object> consentAttributes) {
        this.consentId = consentId;
        this.consentAttributes = consentAttributes;
    }

    public String getConsentId() {
        return consentId;
    }

    public void setConsentId(String consentId) {
        this.consentId = consentId;
    }

    public Map<String, Object> getConsentAttributes() {
        return consentAttributes;
    }

    public void setConsentAttributes(Map<String, Object> consentAttributes) {
        this.consentAttributes = consentAttributes;
    }

    public void setConsentAttribute(String key, Object value) {
        if (this.consentAttributes == null) {
            this.consentAttributes = new HashMap<>();
        }
        this.consentAttributes.put(key, value);
    }
}
