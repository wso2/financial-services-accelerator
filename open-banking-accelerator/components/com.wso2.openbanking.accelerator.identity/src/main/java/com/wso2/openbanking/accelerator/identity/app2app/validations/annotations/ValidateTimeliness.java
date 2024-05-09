package com.wso2.openbanking.accelerator.identity.app2app.validations.annotations;

import com.wso2.openbanking.accelerator.identity.app2app.validations.JwtTokenTimelinessValidator;

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
@Constraint(validatedBy = {JwtTokenTimelinessValidator.class})
public @interface ValidateTimeliness {
    String message() default "JWT token contains invalid time claims.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
