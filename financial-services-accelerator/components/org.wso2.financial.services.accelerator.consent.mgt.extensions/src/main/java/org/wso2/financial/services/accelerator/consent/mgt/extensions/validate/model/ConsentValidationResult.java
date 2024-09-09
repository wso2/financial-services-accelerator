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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.validate.model;

import org.json.JSONObject;

/**
 * Data wrapper for the result of consent validation.
 */
public class ConsentValidationResult {

    private boolean isValid = false;
    private JSONObject modifiedPayload = null;
    private JSONObject consentInformation = new JSONObject();
    /**
     * errorCode, errorMessage and httpCode have to be set in error/invalid scenarios.
     */
    private String errorCode = null;
    private String errorMessage = null;
    private int httpCode = 0;

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public JSONObject getModifiedPayload() {
        return modifiedPayload;
    }

    public void setModifiedPayload(JSONObject modifiedPayload) {
        this.modifiedPayload = modifiedPayload;
    }

    public JSONObject getConsentInformation() {
        return consentInformation;
    }

    public void setConsentInformation(JSONObject consentInformation) {
        this.consentInformation = consentInformation;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

    public JSONObject generatePayload() {
        JSONObject payload = new JSONObject();
        payload.put("isValid", isValid);
        if (modifiedPayload != null) {
            payload.put("modifiedPayload", modifiedPayload);
        }
        if (errorCode != null && errorMessage != null && httpCode != 0) {
            payload.put("errorCode", errorCode);
            payload.put("errorMessage", errorMessage);
            payload.put("httpCode", httpCode);
        }
        return payload;
    }
}
