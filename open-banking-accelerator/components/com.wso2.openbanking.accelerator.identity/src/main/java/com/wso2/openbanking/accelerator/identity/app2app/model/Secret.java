package com.wso2.openbanking.accelerator.identity.app2app.model;

import com.google.gson.annotations.SerializedName;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.identity.app2app.App2AppAuthenticatorConstants;
import com.wso2.openbanking.accelerator.identity.app2app.exception.SecretValidationException;
import com.wso2.openbanking.accelerator.identity.app2app.validations.annotations.ValidateJTI;
import com.wso2.openbanking.accelerator.identity.app2app.validations.annotations.ValidateSignature;
import com.wso2.openbanking.accelerator.identity.app2app.validations.annotations.ValidateTimeliness;
import com.wso2.openbanking.accelerator.identity.app2app.validations.validationgroups.RequiredParamChecks;
import com.wso2.openbanking.accelerator.identity.app2app.validations.validationgroups.ValidityChecks;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

import java.text.ParseException;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


/**
 * Model class for App2App Auth Secret.
 */
@ValidateJTI(groups = ValidityChecks.class)
@ValidateSignature(algorithm = App2AppAuthenticatorConstants.SIGNING_ALGORITHM, groups = ValidityChecks.class)
@ValidateTimeliness(groups = ValidityChecks.class)
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
    private String publicKey;
    private AuthenticatedUser authenticatedUser;

    public Secret(String jwtString) throws SecretValidationException {

        try {
            this.signedJWT = JWTUtils.getSignedJWT(jwtString);
            this.jwtClaimsSet = JWTUtils.getJWTClaimsSet(this.signedJWT);
            this.expirationTime = jwtClaimsSet.getExpirationTime();
            this.notValidBefore = jwtClaimsSet.getNotBeforeTime();
            this.issuedTime = jwtClaimsSet.getIssueTime();
            this.jti = jwtClaimsSet.getJWTID();
            this.deviceId = JWTUtils.getClaim(jwtClaimsSet, App2AppAuthenticatorConstants.DEVICE_IDENTIFIER);
            this.loginHint = JWTUtils.getClaim(jwtClaimsSet, App2AppAuthenticatorConstants.LOGIN_HINT);
        } catch (ParseException e) {
            throw new SecretValidationException("Error while parsing JWT.", e);
        } catch (IllegalArgumentException e) {
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
    @NotNull(message = "Required Parameter exp cannot be null or empty.", groups = RequiredParamChecks.class)
    public Date getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }
    @NotNull(message = "Required Parameter nbf cannot be null or empty.", groups = RequiredParamChecks.class)
    public Date getNotValidBefore() {
        return notValidBefore;
    }

    public void setNotValidBefore(Date notValidBefore) {
        this.notValidBefore = notValidBefore;
    }
    @NotBlank(message = "Required Parameter jti cannot be null or empty.", groups = RequiredParamChecks.class)
    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }
    @NotNull(message = "Required Parameter iat cannot be null or empty.", groups = RequiredParamChecks.class)
    public Date getIssuedTime() {
        return issuedTime;
    }

    public void setIssuedTime(Date issuedAt) {
        this.issuedTime = issuedAt;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
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

    public JWTClaimsSet getJwtClaimsSet() {
        return jwtClaimsSet;
    }

    public void setJwtClaimsSet(JWTClaimsSet jwtClaimsSet) {
        this.jwtClaimsSet = jwtClaimsSet;
    }
}

