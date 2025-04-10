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
import com.wso2.openbanking.accelerator.identity.common.annotations.validationgroups.AttributeChecks;
import com.wso2.openbanking.accelerator.identity.common.annotations.validationgroups.MandatoryChecks;
import com.wso2.openbanking.accelerator.identity.common.annotations.validationgroups.MandatorySsaChecks;
import com.wso2.openbanking.accelerator.identity.common.annotations.validationgroups.SignatureCheck;
import com.wso2.openbanking.accelerator.identity.dcr.validation.DCRCommonConstants;
import com.wso2.openbanking.accelerator.identity.dcr.validation.annotation.ValidateAlgorithm;
import com.wso2.openbanking.accelerator.identity.dcr.validation.annotation.ValidateIssuer;
import com.wso2.openbanking.accelerator.identity.dcr.validation.annotation.ValidateRequiredParams;
import com.wso2.openbanking.accelerator.identity.dcr.validation.annotation.ValidateRequiredSsaParams;
import com.wso2.openbanking.accelerator.identity.dcr.validation.annotation.ValidateSignature;

import java.util.List;
import java.util.Map;

/**
 * Model class for dcr registration request.
 */
@ValidateRequiredParams(message = "Required parameters cannot be null or empty:" + DCRCommonConstants.INVALID_META_DATA,
        groups = MandatoryChecks.class)
@ValidateIssuer(issuerProperty = "issuer", ssa = "softwareStatement",
        message = "Invalid issuer:" + DCRCommonConstants.INVALID_META_DATA, groups = AttributeChecks.class)
@ValidateSignature(ssaBody = "softwareStatementBody", ssa = "softwareStatement", message = "Invalid signature for SSA:"
        + DCRCommonConstants.INVALID_SSA, groups = SignatureCheck.class)
@ValidateRequiredSsaParams(ssa = "softwareStatement", message = "Missing mandatory parameters in SSA:"
        + DCRCommonConstants.INVALID_SSA, groups = MandatorySsaChecks.class)
@ValidateAlgorithm(idTokenAlg = "idTokenSignedResponseAlg", reqObjAlg = "requestObjectSigningAlg",
        tokenAuthAlg = "tokenEndPointAuthSigningAlg",
        message = "Invalid signing algorithm sent:" + DCRCommonConstants.INVALID_META_DATA,
        groups = AttributeChecks.class)
public class RegistrationRequest {

    @SerializedName("iat")
    private String iat;

    @SerializedName("exp")
    private String exp;

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

    private SoftwareStatementBody softwareStatementBody;

    private Map<String, Object> requestParameters;

    private Map<String, Object> ssaParameters;

    public Map<String, Object> getSsaParameters() {

        return ssaParameters;
    }

    public void setSsaParameters(Map<String, Object> ssaParameters) {

        this.ssaParameters = ssaParameters;
    }

    public Map<String, Object> getRequestParameters() {

        return requestParameters;
    }

    public void setRequestParameters(Map<String, Object> requestParameters) {

        this.requestParameters = requestParameters;
    }

    public SoftwareStatementBody getSoftwareStatementBody() {

        return softwareStatementBody;
    }

    public void setSoftwareStatementBody(SoftwareStatementBody softwareStatementBody) {

        this.softwareStatementBody = softwareStatementBody;
    }

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

    public String getIat() {
        return iat;
    }

    public void setIat(String iat) {
        this.iat = iat;
    }

    public String getExp() {
        return exp;
    }

    public void setExp(String exp) {
        this.exp = exp;
    }
}
