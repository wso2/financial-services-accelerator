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

import java.util.List;

/**
 * Model class containing common attributes for software statement.
 */
public class SoftwareStatementBody {

    @SerializedName("software_environment")
    private String softwareEnvironment;

    @SerializedName("software_id")
    private String softwareId;

    @SerializedName("org_id")
    private String orgId;

    @SerializedName("org_name")
    private String orgName;

    @SerializedName("scope")
    private String scopes;

    @SerializedName(value = "software_client_name" , alternate = "client_name")
    private String clientName;

    @SerializedName(value = "software_redirect_uris", alternate = "redirect_uris")
    private List<String> ssaRedirectURIs;

    @SerializedName(value = "software_jwks_endpoint", alternate = "jwks_uri")
    private String jwksURI;

    @SerializedName("iss")
    private String ssaIssuer;

    public String getSsaIssuer() {

        return ssaIssuer;
    }

    public void setSsaIssuer(String ssaIssuer) {

        this.ssaIssuer = ssaIssuer;
    }

    public String getJwksURI() {

        return jwksURI;
    }

    public void setJwksURI(String jwksURI) {

        this.jwksURI = jwksURI;
    }

    public List<String> getCallbackUris() {

        return ssaRedirectURIs;
    }

    public void setCallbackUris(List<String> redirectURIs) {

        this.ssaRedirectURIs = redirectURIs;
    }

    public String getClientName() {

        return clientName;
    }

    public void setClientName(String clientName) {

        this.clientName = clientName;
    }

    public String getScopes() {

        return scopes;
    }

    public void setScopes(String scopes) {

        this.scopes = scopes;
    }

    public String getSoftwareEnvironment() {

        return softwareEnvironment;
    }

    public void setSoftwareEnvironment(String softwareEnvironment) {

        this.softwareEnvironment = softwareEnvironment;
    }

    public String getSoftwareId() {

        return softwareId;
    }

    public void setSoftwareId(String softwareId) {

        this.softwareId = softwareId;
    }

    public String getOrgId() {

        return orgId;
    }

    public void setOrgId(String orgId) {

        this.orgId = orgId;
    }

    public String getOrgName() {

        return orgName;
    }

    public void setOrgName(String orgName) {

        this.orgName = orgName;
    }
}
