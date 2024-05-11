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
import com.wso2.openbanking.accelerator.identity.app2app.App2AppAuthenticatorConstants;
import com.wso2.openbanking.accelerator.identity.app2app.exception.SecretValidationException;
import com.wso2.openbanking.accelerator.identity.app2app.validations.annotations.ValidateJTI;
import com.wso2.openbanking.accelerator.identity.app2app.validations.annotations.ValidateSignature;
import com.wso2.openbanking.accelerator.identity.app2app.validations.annotations.ValidateTimeliness;
import com.wso2.openbanking.accelerator.identity.app2app.validations.validationgroups.RequiredParamChecks;
import com.wso2.openbanking.accelerator.identity.app2app.validations.validationgroups.ValidityChecks;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.text.ParseException;
import java.util.Date;


/**
 * Model class for App2App Auth AppAuthValidationJWT.
 */
@ValidateJTI(groups = ValidityChecks.class)
@ValidateSignature(algorithm = App2AppAuthenticatorConstants.SIGNING_ALGORITHM, groups = ValidityChecks.class)
@ValidateTimeliness(groups = ValidityChecks.class)
public class AppAuthValidationJWT {
    @SerializedName("did")
    private String deviceId;
    @SerializedName("loginHint")
    private String loginHint;
    @SerializedName("exp")
    private Date expirationTime;
    @SerializedName("nbf")
    private Date notValidBefore;
    @SerializedName("jti")
    private String jti;
    @SerializedName("ist")
    private Date issuedTime;
    private SignedJWT signedJWT;
    private JWTClaimsSet jwtClaimsSet;
    private AuthenticatedUser authenticatedUser;

    public AppAuthValidationJWT(SignedJWT signedJWT) throws SecretValidationException {

        try {
            this.signedJWT = signedJWT;
            this.jwtClaimsSet = signedJWT.getJWTClaimsSet();
            this.expirationTime = jwtClaimsSet.getExpirationTime();
            this.notValidBefore = jwtClaimsSet.getNotBeforeTime();
            this.issuedTime = jwtClaimsSet.getIssueTime();
            this.jti = jwtClaimsSet.getJWTID();
            this.deviceId = getClaim(jwtClaimsSet,App2AppAuthenticatorConstants.DEVICE_IDENTIFIER);
            this.loginHint = getClaim(jwtClaimsSet,App2AppAuthenticatorConstants.LOGIN_HINT);
        } catch (IllegalArgumentException | ParseException e) {
            throw new SecretValidationException(e.getMessage());
        }

    }

    @NotBlank(message = "Required Parameter did cannot be null or empty.", groups = RequiredParamChecks.class)
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    @NotBlank(message = "Required Parameter loginHint cannot be null or empty.", groups = RequiredParamChecks.class)
    public String getLoginHint() {
        return loginHint;
    }
    public void setLoginHint(String loginHint) {
        this.loginHint = loginHint;
    }
    @NotNull(message = "Required Parameter exp cannot be null.", groups = RequiredParamChecks.class)
    public Date getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }
    @NotNull(message = "Required Parameter nbf cannot be null.", groups = RequiredParamChecks.class)
    public Date getNotValidBefore() {
        return notValidBefore;
    }

    public void setNotValidBefore(Date notValidBefore) {
        this.notValidBefore = notValidBefore;
    }
    @NotBlank(message = "Required Parameter jti cannot be null or empty.", groups = RequiredParamChecks.class)
    public String getJti() {
        return jwtClaimsSet.getJWTID();
    }

    public void setJti(String jti) {
        this.jti = jti;
    }
    @NotNull(message = "Required Parameter iat cannot be null.", groups = RequiredParamChecks.class)
    public Date getIssuedTime() {
        return issuedTime;
    }

    public void setIssuedTime(Date issuedAt) {
        this.issuedTime = issuedAt;
    }
    @NotNull(message = "Required Parameter signedJWT cannot be null.", groups = RequiredParamChecks.class)
    public SignedJWT getSignedJWT() {
        return signedJWT;
    }

    public void setSignedJWT(SignedJWT signedJWT) {
        this.signedJWT = signedJWT;
    }

    public AuthenticatedUser getAuthenticatedUser() {
        return authenticatedUser;
    }

    public void setAuthenticatedUser(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    @NotNull(message = "Required Parameter jwtClaimsSet cannot be null.", groups = RequiredParamChecks.class)
    public JWTClaimsSet getJwtClaimsSet() {
        return jwtClaimsSet;
    }

    public void setJwtClaimsSet(JWTClaimsSet jwtClaimsSet) {
        this.jwtClaimsSet = jwtClaimsSet;
    }

    /**
     * Retrieves the value of the specified claim from the provided JWTClaimsSet.
     *
     * @param jwtClaimsSet the JWTClaimsSet from which to retrieve the claim value
     * @param claim the name of the claim to retrieve
     * @param <T> the type of the claim value
     * @return the value of the specified claim, or null if the claim is not present
     */
    private <T> T getClaim(JWTClaimsSet jwtClaimsSet , String claim) {

        Object claimObj = jwtClaimsSet.getClaim(claim);
        return (T) claimObj;

    }
}

