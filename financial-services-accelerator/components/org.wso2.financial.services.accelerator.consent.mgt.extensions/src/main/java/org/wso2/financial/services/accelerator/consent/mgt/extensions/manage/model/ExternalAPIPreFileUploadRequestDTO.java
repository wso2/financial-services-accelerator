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
package org.wso2.financial.services.accelerator.consent.mgt.extensions.manage.model;

import org.json.JSONObject;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.model.ExternalAPIConsentResourceRequestDTO;

import java.util.Map;

/**
 * ExternalAPIValidateFileUploadRequestDTO
 */
public class ExternalAPIPreFileUploadRequestDTO {

    private String consentId;
    private ExternalAPIConsentResourceRequestDTO consentResource;
    private String fileContent;
    private String consentResourcePath;
    private Map<String, String> requestHeaders;

    public ExternalAPIPreFileUploadRequestDTO(ExternalAPIConsentResourceRequestDTO consentResource,
                                              ConsentManageData consentManageData) {

        this.consentId = consentResource.getId();
        this.consentResource = consentResource;
        this.fileContent = consentManageData.getPayload().toString();
        this.consentResourcePath = consentManageData.getRequestPath();
        this.requestHeaders = consentManageData.getAllowedExtensionHeaders();

    }

    public String getConsentId() {
        return consentId;
    }

    public void setConsentId(String consentId) {
        this.consentId = consentId;
    }

    public ExternalAPIConsentResourceRequestDTO getConsentResource() {
        return consentResource;
    }

    public void setConsentResource(
            ExternalAPIConsentResourceRequestDTO consentResource) {
        this.consentResource = consentResource;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
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

    /**
     * Convert the dto to a JSON object with correct consent resource format.
     *
     * @return JSON object
     */
    public JSONObject toJson() {

        JSONObject dtoJson = new JSONObject(this);
        JSONObject consentResourceJson = new JSONObject(consentResource);
        dtoJson.put("consentResource", consentResourceJson);
        return dtoJson;
    }
}
