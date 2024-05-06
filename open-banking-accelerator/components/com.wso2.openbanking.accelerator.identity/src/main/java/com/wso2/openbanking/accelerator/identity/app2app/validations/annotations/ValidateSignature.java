package com.wso2.openbanking.accelerator.identity.app2app.validations.annotations;

import com.wso2.openbanking.accelerator.identity.dcr.validation.AlgorithmValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {AlgorithmValidator.class})
public @interface ValidateSignature {
    String message() default "Signature validation Failed.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
    String algorithm();
}
