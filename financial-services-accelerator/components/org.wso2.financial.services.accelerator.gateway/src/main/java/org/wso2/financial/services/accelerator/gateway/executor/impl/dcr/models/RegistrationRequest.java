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

package org.wso2.financial.services.accelerator.gateway.executor.impl.dcr.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Model class for dcr request containing common attributes.
 */
public class RegistrationRequest {

    @SerializedName("iat")
    private long iat;

    @SerializedName("exp")
    private long exp;

    @SerializedName("aud")
    private String aud;

    @SerializedName("iss")
    private String issuer;

    @SerializedName("token_endpoint_auth_method")
    private String tokenEndPointAuthMethod;

    @SerializedName("jwks_uri")
    private String jwksURI;

    @SerializedName("grant_types")
    private List<String> grantTypes;

    @SerializedName("software_statement")
    private String softwareStatement;

    @SerializedName("id_token_signed_response_alg")
    private String idTokenSignedResponseAlg;

    @SerializedName("redirect_uris")
    private List<String> redirectUris;

    @SerializedName("token_endpoint_auth_signing_alg")
    private String tokenEndPointAuthSigningAlg;

    @SerializedName("response_types")
    private List<String> responseTypes;

    @SerializedName("software_id")
    private String softwareId;

    @SerializedName("scope")
    private String scope;

    @SerializedName("application_type")
    private String applicationType;

    @SerializedName("jti")
    private String jti;

    @SerializedName("id_token_encrypted_response_alg")
    private String idTokenEncryptionResponseAlg;

    @SerializedName("id_token_encrypted_response_enc")
    private String idTokenEncryptionResponseEnc;

    @SerializedName("request_object_signing_alg")
    private String requestObjectSigningAlg;

    @SerializedName("tls_client_auth_subject_dn")
    private String tlsClientAuthSubjectDn;

    @SerializedName("backchannel_token_delivery_mode")
    private String backchannelTokenDeliveryMode;

    @SerializedName("backchannel_authentication_request_signing_alg")
    private String backchannelAuthenticationRequestSigningAlg;

    @SerializedName("backchannel_client_notification_endpoint")
    private String backchannelClientNotificationEndpoint;

    @SerializedName("backchannel_user_code_parameter_supported")
    private boolean backchannelUserCodeParameterSupported;

    @SerializedName("client_id")
    private String clientId;

    @SerializedName("client_secret")
    private String clientSecret;

    @SerializedName("client_name")
    private String clientName;

    @SerializedName("token_type_extension")
    private String tokenType;

    @SerializedName("require_signed_request_object")
    private boolean requireSignedReqObj;

    @SerializedName("tls_client_certificate_bound_access_tokens")
    private boolean tlsClientCertificateBoundAccessToken;

    public boolean getBackchannelUserCodeParameterSupported() {

        return backchannelUserCodeParameterSupported;
    }

    public void setBackchannelUserCodeParameterSupported(boolean backchannelUserCodeParameterSupported) {

        this.backchannelUserCodeParameterSupported = backchannelUserCodeParameterSupported;
    }

    public String getBackchannelClientNotificationEndpoint() {

        return backchannelClientNotificationEndpoint;
    }

    public void setBackchannelClientNotificationEndpoint(String backchannelClientNotificationEndpoint) {

        this.backchannelClientNotificationEndpoint = backchannelClientNotificationEndpoint;
    }

    public String getBackchannelAuthenticationRequestSigningAlg() {

        return backchannelAuthenticationRequestSigningAlg;
    }

    public void setBackchannelAuthenticationRequestSigningAlg(String backchannelAuthenticationRequestSigningAlg) {

        this.backchannelAuthenticationRequestSigningAlg = backchannelAuthenticationRequestSigningAlg;
    }

    public String getBackchannelTokenDeliveryMode() {

        return backchannelTokenDeliveryMode;
    }

    public void setBackchannelTokenDeliveryMode(String backchannelTokenDeliveryMode) {

        this.backchannelTokenDeliveryMode = backchannelTokenDeliveryMode;
    }

    public String getTlsClientAuthSubjectDn() {

        return tlsClientAuthSubjectDn;
    }

    public void setTlsClientAuthSubjectDn(String tlsClientAuthSubjectDn) {

        this.tlsClientAuthSubjectDn = tlsClientAuthSubjectDn;
    }

    public String getRequestObjectSigningAlg() {

        return requestObjectSigningAlg;
    }

    public void setRequestObjectSigningAlg(String requestObjectSigningAlg) {

        this.requestObjectSigningAlg = requestObjectSigningAlg;
    }

    public String getIdTokenEncryptionResponseEnc() {

        return idTokenEncryptionResponseEnc;
    }

    public void setIdTokenEncryptionResponseEnc(String idTokenEncryptionResponseEnc) {

        this.idTokenEncryptionResponseEnc = idTokenEncryptionResponseEnc;
    }

    public String getIdTokenEncryptionResponseAlg() {

        return idTokenEncryptionResponseAlg;
    }

    public void setIdTokenEncryptionResponseAlg(String idTokenEncryptionResponseAlg) {

        this.idTokenEncryptionResponseAlg = idTokenEncryptionResponseAlg;
    }

    public String getApplicationType() {

        return applicationType;
    }

    public void setApplicationType(String applicationType) {

        this.applicationType = applicationType;
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

    public List<String> getResponseTypes() {

        return responseTypes;
    }

    public void setResponseTypes(List<String> responseTypes) {

        this.responseTypes = responseTypes;
    }

    public String getTokenEndPointAuthSigningAlg() {

        return tokenEndPointAuthSigningAlg;
    }

    public void setTokenEndPointAuthSigningAlg(String tokenEndPointAuthSigningAlg) {

        this.tokenEndPointAuthSigningAlg = tokenEndPointAuthSigningAlg;
    }

    public List<String> getCallbackUris() {

        return redirectUris;
    }

    public void setCallbackUris(List<String> redirectUris) {

        this.redirectUris = redirectUris;
    }

    public String getIssuer() {

        return issuer;
    }

    public void setIssuer(String issuer) {

        this.issuer = issuer;
    }

    public String getTokenEndPointAuthentication() {

        return tokenEndPointAuthMethod;
    }

    public void setTokenEndPointAuthentication(String tokenEndPointAuthMethod) {

        this.tokenEndPointAuthMethod = tokenEndPointAuthMethod;
    }

    public List<String> getGrantTypes() {

        return grantTypes;
    }

    public void setGrantTypes(List<String> grantTypes) {

        this.grantTypes = grantTypes;
    }

    public String getSoftwareStatement() {

        return softwareStatement;
    }

    public void setSoftwareStatement(String softwareStatement) {

        this.softwareStatement = softwareStatement;
    }

    public String getIdTokenSignedResponseAlg() {

        return idTokenSignedResponseAlg;
    }

    public void setIdTokenSignedResponseAlg(String idTokenSignedResponseAlg) {

        this.idTokenSignedResponseAlg = idTokenSignedResponseAlg;
    }

    public String getAudience() {

        return aud;
    }
    public void setAudience(String aud) {

        this.aud = aud;
    }

    public String getJti() {

        return jti;
    }

    public void setJti(String jti) {

        this.jti = jti;
    }

    public String getJwksURI() {
        return jwksURI;
    }

    public void setJwksURI(String jwksURI) {
        this.jwksURI = jwksURI;
    }

    public long getIat() {
        return iat;
    }

    public void setIat(long iat) {
        this.iat = iat;
    }

    public long getExp() {
        return exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public boolean isRequireSignedReqObj() {
        return requireSignedReqObj;
    }

    public void setRequireSignedReqObj(boolean requireSignedReqObj) {
        this.requireSignedReqObj = requireSignedReqObj;
    }

    public boolean isTlsClientCertBoundAccessTokens() {
        return tlsClientCertificateBoundAccessToken;
    }

    public void setTlsClientCertBoundAccessTokens(boolean tlsClientCert) {
        this.tlsClientCertificateBoundAccessToken = tlsClientCert;
    }
}
