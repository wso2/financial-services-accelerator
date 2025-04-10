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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.model;

import org.json.JSONObject;

/**
 * Consent validate request model.
 */
public class ConsentValidateRequest {
    JSONObject consentData;
    String consentId;
    JSONObject dataRequestPayload;
    String consentType;

    public ConsentValidateRequest(String consentId, JSONObject consentData, JSONObject dataRequestPayload,
                                  String consentType) {
        this.consentData = consentData;
        this.consentId = consentId;
        this.dataRequestPayload = dataRequestPayload;
        this.consentType = consentType;
    }

    public JSONObject getConsentData() {
        return consentData;
    }

    public void setConsentData(JSONObject consentData) {
        this.consentData = consentData;
    }

    public String getConsentId() {
        return consentId;
    }

    public void setConsentId(String consentId) {
        this.consentId = consentId;
    }

    public JSONObject getDataRequestPayload() {
        return dataRequestPayload;
    }

    public void setDataRequestPayload(JSONObject dataRequestPayload) {
        this.dataRequestPayload = dataRequestPayload;
    }

    public String getConsentType() {
        return consentType;
    }

    public void setConsentType(String consentType) {
        this.consentType = consentType;
    }

}
