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

package com.wso2.openbanking.accelerator.identity.dcr.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class for dcr response containing common attributes.
 */
public class RegistrationResponse {

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @SerializedName("registration_access_token")
    protected String token = null;

    @SerializedName("client_id")
    protected String clientId = null;

    @SerializedName("client_id_issued_at")
    protected String clientIdIssuedAt  = null;

    @SerializedName("redirect_uris")
    protected List<String> redirectUris = new ArrayList<>();

    @SerializedName("grant_types")
    protected List<String> grantTypes = new ArrayList<>();

    @SerializedName("response_types")
    protected List<String> responseTypes = new ArrayList<>();

    @SerializedName("application_type")
    protected String applicationType = null;

    @SerializedName("id_token_signed_response_alg")
    protected String idTokenSignedResponseAlg = null;

    @SerializedName("request_object_signing_alg")
    protected String requestObjectSigningAlg = null;

    @SerializedName("scope")
    protected String scope = null;

    @SerializedName("software_id")
    protected String softwareId = null;

    @SerializedName("token_endpoint_auth_method")
    protected String tokenEndpointAuthMethod = null;

    @SerializedName("registration_client_uri")
    protected String registrationClientURI = null;

    @SerializedName("software_statement")
    protected String softwareStatement = null;

    public String getSoftwareStatement() {

        return softwareStatement;
    }

    public void setSoftwareStatement(String softwareStatement) {

        this.softwareStatement = softwareStatement;
    }

    public String getTokenEndpointAuthMethod() {

        return tokenEndpointAuthMethod;
    }

    public void setTokenEndpointAuthMethod(String tokenEndpointAuthMethod) {

        this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
    }

    public List<String> getResponseTypes() {

        return responseTypes;
    }

    public void setResponseTypes(List<String> responseTypes) {

        this.responseTypes = responseTypes;
    }


    public String getClientIdIssuedAt() {

        return clientIdIssuedAt;
    }

    public void setClientIdIssuedAt(String clientIdIssuedAt) {

        this.clientIdIssuedAt = clientIdIssuedAt;
    }

    public String getClientId() {

        return clientId;
    }

    public void setClientId(String clientId) {

        this.clientId = clientId;
    }

    public List<String> getRedirectUris() {

        return redirectUris;
    }

    public void setRedirectUris(List<String> redirectUris) {

        this.redirectUris = redirectUris;
    }

    public List<String> getGrantTypes() {

        return grantTypes;
    }

    public void setGrantTypes(List<String> grantTypes) {

        this.grantTypes = grantTypes;
    }

    public String getApplicationType() {

        return applicationType;
    }

    public void setApplicationType(String applicationType) {

        this.applicationType = applicationType;
    }

    public String getIdTokenSignedResponseAlg() {

        return idTokenSignedResponseAlg;
    }

    public void setIdTokenSignedResponseAlg(String idTokenSignedResponseAlg) {

        this.idTokenSignedResponseAlg = idTokenSignedResponseAlg;
    }

    public String getRequestObjectSigningAlg() {

        return requestObjectSigningAlg;
    }

    public void setRequestObjectSigningAlg(String requestObjectSigningAlg) {

        this.requestObjectSigningAlg = requestObjectSigningAlg;
    }

    public String getScope() {

        return scope;
    }

    public void setScope(String scope) {

        this.scope = scope;
    }

    public String getSoftwareId() {

        return softwareId;
    }

    public void setSoftwareId(String softwareId) {

        this.softwareId = softwareId;
    }

    public String getRegistrationClientURI() {
        return registrationClientURI;
    }

    public void setRegistrationClientURI(String registrationClientURI) {
        this.registrationClientURI = registrationClientURI;
    }
}
