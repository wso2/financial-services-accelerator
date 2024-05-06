package com.wso2.openbanking.accelerator.identity.app2app.validations;

import com.wso2.openbanking.accelerator.identity.app2app.model.Secret;
import com.wso2.openbanking.accelerator.identity.app2app.validations.annotations.ValidateSignature;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class SignatureValidator implements ConstraintValidator<ValidateSignature, Secret> {
    private String algorithm;
    @Override
    public void initialize(ValidateSignature validateSignature) {
        this.algorithm = validateSignature.algorithm();
    }

    @Override
    public boolean isValid(Secret secret, ConstraintValidatorContext constraintValidatorContext) {
        return false;
    }
}
