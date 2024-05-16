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

package com.wso2.openbanking.accelerator.identity.app2app.model;

import com.google.gson.annotations.SerializedName;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.wso2.openbanking.accelerator.identity.app2app.validations.annotations.ValidateExpiry;
import com.wso2.openbanking.accelerator.identity.app2app.validations.annotations.ValidateJTI;
import com.wso2.openbanking.accelerator.identity.app2app.validations.annotations.ValidateNBF;
import com.wso2.openbanking.accelerator.identity.app2app.validations.annotations.ValidateSignature;
import com.wso2.openbanking.accelerator.identity.common.annotations.validationgroups.RequiredParamChecks;
import com.wso2.openbanking.accelerator.identity.common.annotations.validationgroups.ValidityChecks;

import java.text.ParseException;
import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Model class for App2App Auth AppAuthValidationJWT.
 */
@ValidateJTI(groups = ValidityChecks.class)
@ValidateSignature(groups = ValidityChecks.class)
@ValidateExpiry(groups = ValidityChecks.class)
@ValidateNBF(groups = ValidityChecks.class)
public class AppAuthValidationJWT {

    @SerializedName(AppAuthValidationJWTConstants.DEVICE_IDENTIFIER)
    private String deviceId;
    @SerializedName(AppAuthValidationJWTConstants.LOGIN_HINT)
    private String loginHint;
    @SerializedName(AppAuthValidationJWTConstants.EXPIRY_TIME)
    private Date expirationTime;
    @SerializedName(AppAuthValidationJWTConstants.NOT_VALID_BEFORE)
    private Date notValidBefore;
    @SerializedName(AppAuthValidationJWTConstants.JWT_ID)
    private String jti;
    @SerializedName(AppAuthValidationJWTConstants.ISSUED_TIME)
    private Date issuedTime;
    private SignedJWT signedJWT;
    private String publicKey;
    private String signingAlgorithm;

    public AppAuthValidationJWT(SignedJWT signedJWT)
            throws ParseException {

        this.signedJWT = signedJWT;
        JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
        this.expirationTime = jwtClaimsSet.getExpirationTime();
        this.notValidBefore = jwtClaimsSet.getNotBeforeTime();
        this.issuedTime = jwtClaimsSet.getIssueTime();
        this.jti = jwtClaimsSet.getJWTID();
        this.deviceId = getClaim(jwtClaimsSet, AppAuthValidationJWTConstants.DEVICE_IDENTIFIER);
        this.loginHint = getClaim(jwtClaimsSet, AppAuthValidationJWTConstants.LOGIN_HINT);

    }

    @NotBlank(message = "Required parameter did cannot be null or empty.", groups = RequiredParamChecks.class)
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    @NotBlank(message = "Required parameter loginHint cannot be null or empty.", groups = RequiredParamChecks.class)
    public String getLoginHint() {
        return loginHint;
    }
    public void setLoginHint(String loginHint) {
        this.loginHint = loginHint;
    }
    @NotNull(message = "Required parameter exp cannot be null.", groups = RequiredParamChecks.class)
    public Date getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }
    @NotNull(message = "Required parameter nbf cannot be null.", groups = RequiredParamChecks.class)
    public Date getNotValidBefore() {
        return notValidBefore;
    }

    public void setNotValidBefore(Date notValidBefore) {
        this.notValidBefore = notValidBefore;
    }
    @NotBlank(message = "Required parameter jti cannot be null or empty.", groups = RequiredParamChecks.class)
    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }
    @NotNull(message = "Required parameter iat cannot be null.", groups = RequiredParamChecks.class)
    public Date getIssuedTime() {
        return issuedTime;
    }

    public void setIssuedTime(Date issuedAt) {
        this.issuedTime = issuedAt;
    }
    @NotNull(message = "Required parameter signedJWT cannot be null.", groups = RequiredParamChecks.class)
    public SignedJWT getSignedJWT() {
        return signedJWT;
    }

    public void setSignedJWT(SignedJWT signedJWT) {
        this.signedJWT = signedJWT;
    }

    @NotBlank(message = "Required parameter public key cannot be null or empty.", groups = RequiredParamChecks.class)
    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @NotBlank(message = "Required parameter signing algorithm cannot be null or empty.",
            groups = RequiredParamChecks.class)
    public String getSigningAlgorithm() {
        return signingAlgorithm;
    }

    public void setSigningAlgorithm(String signingAlgorithm) {
        this.signingAlgorithm = signingAlgorithm;
    }

    /**
     * Retrieves the value of the specified claim from the provided JWTClaimsSet.
     *
     * @param jwtClaimsSet the JWTClaimsSet from which to retrieve the claim value
     * @param claim the name of the claim to retrieve
     * @return the value of the specified claim, or null if the claim is not present
     */
    private String getClaim(JWTClaimsSet jwtClaimsSet , String claim) {

        Object claimObj = jwtClaimsSet.getClaim(claim);
        return (String) claimObj;

    }
}

