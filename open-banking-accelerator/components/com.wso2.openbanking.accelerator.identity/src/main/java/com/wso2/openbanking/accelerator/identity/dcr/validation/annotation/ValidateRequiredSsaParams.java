/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */
package com.wso2.openbanking.accelerator.identity.dcr.validation.annotation;

import com.wso2.openbanking.accelerator.identity.dcr.validation.RequiredSsaParamsValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation class for mandatory parameter validation in SSA
 */
@Target(ElementType.TYPE)
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {RequiredSsaParamsValidator.class})
public @interface ValidateRequiredSsaParams {

    String message() default "Missing mandatory parameter from SSA";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String ssa() default "ssa";
}
