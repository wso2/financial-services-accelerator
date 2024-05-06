package com.wso2.openbanking.accelerator.identity.app2app.model;

import com.google.gson.annotations.SerializedName;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.identity.app2app.App2AppAuthenticatorConstants;
import com.wso2.openbanking.accelerator.identity.app2app.exception.SecretValidationException;

import javax.validation.constraints.NotBlank;
import java.text.ParseException;
import java.util.Date;

/**
 * Model class for App2App Auth Secret
 */
public class Secret {
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

    public Secret(String jwtString) throws SecretValidationException {
        try {
            this.signedJWT = JWTUtils.getSignedJWT(jwtString);
            this.jwtClaimsSet = JWTUtils.getJWTClaimsSet(this.signedJWT);
            this.expirationTime = jwtClaimsSet.getExpirationTime();
            this.notValidBefore = jwtClaimsSet.getNotBeforeTime();
            this.issuedTime = jwtClaimsSet.getIssueTime();
            this.jti = JWTUtils.getClaim(jwtClaimsSet, App2AppAuthenticatorConstants.JTI);
            this.deviceId = JWTUtils.getClaim(jwtClaimsSet,App2AppAuthenticatorConstants.DEVICE_IDENTIFIER);
            this.loginHint = JWTUtils.getClaim(jwtClaimsSet,App2AppAuthenticatorConstants.LOGIN_HINT);
        } catch (ParseException e) {
            throw new SecretValidationException("Error while parsing JWT.",e);
        } catch (IllegalArgumentException e){
            throw new SecretValidationException(e.getMessage());
        }
    }

    @NotBlank(message = "Required Parameter did cannot be null or empty.")
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    @NotBlank(message = "Required Parameter loginHint cannot be null or empty.")
    public String getLoginHint() {
        return loginHint;
    }
    public void setLoginHint(String loginHint) {
        this.loginHint = loginHint;
    }
    @NotBlank(message = "Required Parameter exp cannot be null or empty.")
    public Date getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }
    @NotBlank(message = "Required Parameter nbf cannot be null or empty.")
    public Date getNotValidBefore() {
        return notValidBefore;
    }

    public void setNotValidBefore(Date notValidBefore) {
        this.notValidBefore = notValidBefore;
    }
    @NotBlank(message = "Required Parameter jti cannot be null or empty.")
    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }
    @NotBlank(message = "Required Parameter iat cannot be null or empty.")
    public Date getIssuedAt() {
        return issuedTime;
    }

    public void setIssuedAt(Date issuedAt) {
        this.issuedTime = issuedAt;
    }
}
