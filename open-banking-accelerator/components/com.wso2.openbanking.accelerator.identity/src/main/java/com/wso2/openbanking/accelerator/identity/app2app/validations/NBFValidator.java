package com.wso2.openbanking.accelerator.identity.app2app.validations;

import com.nimbusds.jwt.SignedJWT;
import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.identity.app2app.model.AppAuthValidationJWT;
import com.wso2.openbanking.accelerator.identity.app2app.validations.annotations.ValidateNBF;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.text.ParseException;
import java.util.Date;

public class NBFValidator implements ConstraintValidator<ValidateNBF, AppAuthValidationJWT> {

    private static final long DEFAULT_TIME_SKEW_IN_SECONDS = 300L;

    private static final Log log = LogFactory.getLog(NBFValidator.class);
    @Override
    public boolean isValid(AppAuthValidationJWT appAuthValidationJWT, ConstraintValidatorContext constraintValidatorContext) {

        Date notValidBefore = appAuthValidationJWT.getNotValidBefore();
        return JWTUtils.validateNotValidBefore(notValidBefore, DEFAULT_TIME_SKEW_IN_SECONDS);

    }
}
