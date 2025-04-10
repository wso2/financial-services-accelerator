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

package org.wso2.financial.services.accelerator.consent.mgt.dao.models;

/**
 * Model for the consent file.
 */
public class ConsentFile {

    private String consentID;
    private String consentFile;

    public ConsentFile() {

    }

    public ConsentFile(String consentID, String consentFile) {
        this.consentID = consentID;
        this.consentFile = consentFile;
    }

    public String getConsentID() {

        return consentID;
    }

    public void setConsentID(String consentID) {

        this.consentID = consentID;
    }

    public String getConsentFile() {

        return consentFile;
    }

    public void setConsentFile(String consentFile) {

        this.consentFile = consentFile;
    }
}
