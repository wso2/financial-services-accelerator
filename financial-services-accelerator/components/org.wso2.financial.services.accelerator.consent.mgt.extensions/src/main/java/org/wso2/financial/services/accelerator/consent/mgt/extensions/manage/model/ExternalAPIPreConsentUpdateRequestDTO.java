/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model;

import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.model.ExternalAPIBasicConsentResourceRequestDTO;

import java.util.Map;

/**
 * ExternalAPIPreConsentUpdateRequestDTO.
 */
public class ExternalAPIPreConsentUpdateRequestDTO {

    //Payload can either be a JSONObject or a JSONArray
    private ExternalAPIBasicConsentResourceRequestDTO consentResource;
    private Object consentUpdateData;
    private String consentResourcePath;
    private Map<String, String> requestHeaders;

    public ExternalAPIPreConsentUpdateRequestDTO(ConsentManageData consentManageData,
                                                 ExternalAPIBasicConsentResourceRequestDTO consentResource) {

        this.consentResource = consentResource;
        this.consentUpdateData = consentManageData.getPayload();
        this.consentResourcePath = consentManageData.getRequestPath();
        this.requestHeaders = consentManageData.getAllowedExtensionHeaders();
    }

    public ExternalAPIBasicConsentResourceRequestDTO getConsentResource() {
        return consentResource;
    }

    public void setConsentResource(ExternalAPIBasicConsentResourceRequestDTO consentResource) {
        this.consentResource = consentResource;
    }

    public Object getConsentUpdateData() {
        return consentUpdateData;
    }

    public void setConsentUpdateData(Object consentUpdateData) {
        this.consentUpdateData = consentUpdateData;
    }

    public String getConsentResourcePath() {
        return consentResourcePath;
    }

    public void setConsentResourcePath(String consentResourcePath) {
        this.consentResourcePath = consentResourcePath;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }
}
