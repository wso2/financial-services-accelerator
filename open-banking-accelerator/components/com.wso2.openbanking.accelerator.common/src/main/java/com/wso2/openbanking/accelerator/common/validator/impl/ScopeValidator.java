/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.wso2.openbanking.accelerator.common.validator.impl;

import com.wso2.openbanking.accelerator.common.validator.annotation.ValidScopeFormat;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * To validate if the scope is in the correct format.
 */
public class ScopeValidator implements ConstraintValidator<ValidScopeFormat, Object> {

    private String scopeXPath;
    private static Log log = LogFactory.getLog(ScopeValidator.class);

    @Override
    public void initialize(ValidScopeFormat constraintAnnotation) {
        this.scopeXPath = constraintAnnotation.scope();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {

        try {
            final Object scope = new PropertyUtilsBean().getProperty(object, scopeXPath);

            return scopeValidate(scope);

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | NestedNullException e) {
            log.error("Error while resolving validation fields", e);
            return false;
        }
    }

    boolean scopeValidate(Object scope) {

        if (scope instanceof String) {
            ArrayList<String> scopes = new ArrayList<>(Arrays.asList(
                    ((String) scope).replaceAll("\\s+", " ").trim().split(" ")));

            return scopes.contains("openid");
        }

        return false;
    }
}
