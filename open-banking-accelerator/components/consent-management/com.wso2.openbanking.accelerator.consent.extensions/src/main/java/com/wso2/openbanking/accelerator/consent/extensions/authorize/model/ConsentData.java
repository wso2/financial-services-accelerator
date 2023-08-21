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

package com.wso2.openbanking.accelerator.consent.extensions.authorize.model;

import com.wso2.openbanking.accelerator.consent.mgt.dao.models.AuthorizationResource;
import com.wso2.openbanking.accelerator.consent.mgt.dao.models.ConsentResource;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Data wrapper for consent retrieve flow data.
 *
 */
public class ConsentData implements Serializable {

    private static final long serialVersionUID = -568639746792395972L;
    private String sessionDataKey;
    private String userId;
    private String spQueryParams;
    private String scopeString;
    private String application;
    private String consentId;
    private String clientId;
    private Boolean regulatory;
    private Map<String, String> requestHeaders;
    private ConsentResource consentResource;
    private AuthorizationResource authResource;
    private Map<String, Object> metaDataMap;
    private Map<String, Serializable> sensitiveDataMap;
    private URI redirectURI;
    private String state;

    //Mandatory parameter to be set by extension. This will be considered "DEFAULT" if not set.
    private String type;

    public ConsentData(String sessionDataKey, String userId, String spQueryParams, String scopeString,
                       String application, Map<String, String> requestHeaders) {

        this.sessionDataKey = sessionDataKey;
        this.userId = userId;
        this.spQueryParams = spQueryParams;
        this.scopeString = scopeString;
        this.application = application;
        this.requestHeaders = requestHeaders;
        this.metaDataMap = new HashMap<>();
    }

    public String getSessionDataKey() {
        return sessionDataKey;
    }

    public void setSessionDataKey(String sessionDataKey) {
        this.sessionDataKey = sessionDataKey;
    }

    public String getUserId() {

        return userId;
    }

    public void setUserId(String userId) {

        this.userId = userId;
    }

    public String getApplication() {

        return application;
    }

    public void setApplication(String application) {

        this.application = application;
    }

    public String getScopeString() {

        return scopeString;
    }

    public void setScopeString(String scopeString) {

        this.scopeString = scopeString;
    }

    public String getSpQueryParams() {

        return spQueryParams;
    }

    public void setSpQueryParams(String spQueryParams) {

        this.spQueryParams = spQueryParams;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ConsentResource getConsentResource() {
        return consentResource;
    }

    public void setConsentResource(ConsentResource consentResource) {
        this.consentResource = consentResource;
    }

    public String getConsentId() {
        return consentId;
    }

    public void setConsentId(String consentId) {
        this.consentId = consentId;
    }

    public AuthorizationResource getAuthResource() {
        return authResource;
    }

    public void setAuthResource(AuthorizationResource authResource) {
        this.authResource = authResource;
    }

    public Boolean isRegulatory() {
        return regulatory;
    }

    public void setRegulatory(Boolean regulatory) {
        this.regulatory = regulatory;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Map<String, Object> getMetaDataMap() {
        return metaDataMap;
    }

    public void setMetaDataMap(Map<String, Object> metaDataMap) {
        this.metaDataMap = metaDataMap;
    }

    public void addData(String key, Object value) {

        this.metaDataMap.put(key, value);
    }

    public void addAllData(Map<String, Object> data) {

        this.metaDataMap.putAll(data);
    }

    public URI getRedirectURI() {
        return redirectURI;
    }

    public void setRedirectURI(URI redirectURI) {
        this.redirectURI = redirectURI;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Map<String, Serializable> getSensitiveDataMap() {
        return sensitiveDataMap;
    }

    public void setSensitiveDataMap(Map<String, Serializable> sensitiveDataMap) {
        this.sensitiveDataMap = sensitiveDataMap;
    }
}
