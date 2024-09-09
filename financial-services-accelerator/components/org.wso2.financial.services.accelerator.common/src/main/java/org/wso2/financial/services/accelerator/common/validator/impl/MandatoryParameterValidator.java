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

package org.wso2.financial.services.accelerator.common.validator.impl;

import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.common.validator.annotation.RequiredParameter;

import java.lang.reflect.InvocationTargetException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * To validate if a mandatory parameter is in the object.
 */
public class MandatoryParameterValidator implements ConstraintValidator<RequiredParameter, Object> {

    private String paramXPath;
    private static Log log = LogFactory.getLog(MandatoryParameterValidator.class);

    @Override
    public void initialize(RequiredParameter constraintAnnotation) {
        this.paramXPath = constraintAnnotation.param();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {

        try {

            final Object parameterValue = new PropertyUtilsBean().getProperty(object, paramXPath);

            if (parameterValue instanceof Integer) {
                return (Integer) parameterValue != 0;

            } else if (parameterValue instanceof Boolean) {
                return (Boolean) parameterValue;

            } else {
                return parameterValue != null;
            }

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | NestedNullException e) {
            log.error("Mandatory parameter missing", e);
            return false;
        }

    }
}
