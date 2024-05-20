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
import com.wso2.openbanking.accelerator.identity.common.annotations.validationgroups.MandatoryChecks;
import com.wso2.openbanking.accelerator.identity.common.annotations.validationgroups.SignatureCheck;
import com.wso2.openbanking.accelerator.identity.common.annotations.validationgroups.ValidityChecks;

import java.text.ParseException;
import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Model class for App2App Auth DeviceVerificationToken.
 */
@ValidateJTI(groups = ValidityChecks.class)
@ValidateSignature(groups = SignatureCheck.class)
@ValidateExpiry(groups = ValidityChecks.class)
@ValidateNBF(groups = ValidityChecks.class)
public class DeviceVerificationToken {

    @SerializedName(DeviceVerificationTokenConstants.DEVICE_IDENTIFIER)
    private String deviceId;
    @SerializedName(DeviceVerificationTokenConstants.LOGIN_HINT)
    private String loginHint;
    @SerializedName(DeviceVerificationTokenConstants.EXPIRY_TIME)
    private Date expirationTime;
    @SerializedName(DeviceVerificationTokenConstants.NOT_VALID_BEFORE)
    private Date notValidBefore;
    @SerializedName(DeviceVerificationTokenConstants.JWT_ID)
    private String jti;
    @SerializedName(DeviceVerificationTokenConstants.ISSUED_TIME)
    private Date issuedTime;
    private SignedJWT signedJWT;
    private String publicKey;

    public DeviceVerificationToken(SignedJWT signedJWT)
            throws ParseException {

        this.signedJWT = signedJWT;
        JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
        this.expirationTime = jwtClaimsSet.getExpirationTime();
        this.notValidBefore = jwtClaimsSet.getNotBeforeTime();
        this.issuedTime = jwtClaimsSet.getIssueTime();
        this.jti = jwtClaimsSet.getJWTID();
        this.deviceId = getClaim(jwtClaimsSet, DeviceVerificationTokenConstants.DEVICE_IDENTIFIER);
        this.loginHint = getClaim(jwtClaimsSet, DeviceVerificationTokenConstants.LOGIN_HINT);

    }

    @NotBlank(message = "Required parameter did cannot be null or empty.", groups = MandatoryChecks.class)
    public String getDeviceId() {

        return deviceId;
    }

    @NotBlank(message = "Required parameter loginHint cannot be null or empty.", groups = MandatoryChecks.class)
    public String getLoginHint() {

        return loginHint;
    }

    @NotNull(message = "Required parameter exp cannot be null.", groups = MandatoryChecks.class)
    public Date getExpirationTime() {

        return expirationTime;
    }

    @NotNull(message = "Required parameter nbf cannot be null.", groups = MandatoryChecks.class)
    public Date getNotValidBefore() {

        return notValidBefore;
    }

    @NotBlank(message = "Required parameter jti cannot be null or empty.", groups = MandatoryChecks.class)
    public String getJti() {

        return jti;
    }

    @NotNull(message = "Required parameter iat cannot be null.", groups = MandatoryChecks.class)
    public Date getIssuedTime() {

        return issuedTime;
    }

    @NotNull(message = "Required parameter signedJWT cannot be null.", groups = MandatoryChecks.class)
    public SignedJWT getSignedJWT() {

        return signedJWT;
    }

    public void setSignedJWT(SignedJWT signedJWT) {

        this.signedJWT = signedJWT;
    }

    @NotBlank(message = "Required parameter public key cannot be null or empty.", groups = MandatoryChecks.class)
    public String getPublicKey() {

        return publicKey;
    }

    public void setPublicKey(String publicKey) {

        this.publicKey = publicKey;
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

