/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.openbanking.accelerator.consent.extensions.common.idempotency;

import com.wso2.openbanking.accelerator.consent.mgt.dao.models.DetailedConsentResource;

/**
 * Class to hold idempotency validation result.
 */
public class IdempotencyValidationResult {

    private boolean isIdempotent;
    private boolean isValid;
    private DetailedConsentResource consent;
    private String consentId;

    public IdempotencyValidationResult() {
    }

    public IdempotencyValidationResult(boolean isIdempotent, boolean isValid, DetailedConsentResource consent,
                                       String consentId) {
        this.isIdempotent = isIdempotent;
        this.isValid = isValid;
        this.consent = consent;
        this.consentId = consentId;
    }

    public boolean isIdempotent() {
        return isIdempotent;
    }

    public void setIsIdempotent(boolean isIdempotent) {
        this.isIdempotent = isIdempotent;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public DetailedConsentResource getConsent() {
        return consent;
    }

    public void setConsent(DetailedConsentResource consent) {
        this.consent = consent;
    }

    public String getConsentId() {
        return consentId;
    }

    public void setConsentID(String consentId) {
        this.consentId = consentId;
    }
}
