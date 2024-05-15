package com.wso2.openbanking.accelerator.identity.app2app.validations;

import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.identity.app2app.model.AppAuthValidationJWT;
import com.wso2.openbanking.accelerator.identity.app2app.validations.annotations.ValidateExpiry;
import com.wso2.openbanking.accelerator.identity.app2app.validations.annotations.ValidateNBF;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.text.ParseException;
import java.util.Date;

public class ExpiryValidator implements ConstraintValidator<ValidateExpiry, AppAuthValidationJWT> {

    private static final long DEFAULT_TIME_SKEW_IN_SECONDS = 300L;
    @Override
    public boolean isValid(AppAuthValidationJWT appAuthValidationJWT, ConstraintValidatorContext constraintValidatorContext) {

        Date expiryTime = appAuthValidationJWT.getExpirationTime();
        return JWTUtils.validateExpiryTime(expiryTime, DEFAULT_TIME_SKEW_IN_SECONDS);

    }
}
