/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.common.validator.annotation;

import org.wso2.financial.services.accelerator.common.validator.impl.MandatoryParameterValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to check required fields.
 */
@Target(ElementType.TYPE)
@Repeatable(RequiredParameters.class)
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {MandatoryParameterValidator.class})
public @interface RequiredParameter {

    String message() default "Mandatory parameter missing";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String param();

}
