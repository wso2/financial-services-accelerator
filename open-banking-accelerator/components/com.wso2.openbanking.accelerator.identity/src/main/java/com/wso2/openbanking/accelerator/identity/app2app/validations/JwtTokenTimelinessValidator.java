package com.wso2.openbanking.accelerator.identity.app2app.validations;

import com.nimbusds.jwt.JWTClaimsSet;
import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.identity.app2app.model.Secret;
import com.wso2.openbanking.accelerator.identity.app2app.validations.annotations.ValidateTimeliness;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class JwtTokenTimelinessValidator implements ConstraintValidator<ValidateTimeliness, Secret> {
    private static final Log log = LogFactory.getLog(JwtTokenTimelinessValidator.class);
    @Override
    public boolean isValid(Secret secret, ConstraintValidatorContext constraintValidatorContext) {
        JWTClaimsSet jwtClaimsSet = secret.getJwtClaimsSet();
        if (!JWTUtils.validateExpiryTime(jwtClaimsSet)){
            log.error("JWT Expired.");
            return false;
        }
        if (!JWTUtils.validateNotValidBefore(jwtClaimsSet)){
            log.error("JWT is not active.");
            return false;
        }
        return true;
    }
}
