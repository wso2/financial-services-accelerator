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
package com.wso2.openbanking.accelerator.identity.dcr.validation;

import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.identity.dcr.validation.annotation.ValidateIssuer;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator class for validating the issuer of the registration request.
 */
public class IssuerValidator implements ConstraintValidator<ValidateIssuer, Object> {

    private static final Log log = LogFactory.getLog(IssuerValidator.class);

    private String issuerPath;
    private String ssaPath;

    @Override
    public void initialize(ValidateIssuer validateIssuer) {

        this.issuerPath = validateIssuer.issuerProperty();
        this.ssaPath = validateIssuer.ssa();
    }

    @Override
    public boolean isValid(Object registrationRequest,
                           ConstraintValidatorContext constraintValidatorContext) {

        try {
            String issuer = BeanUtils.getProperty(registrationRequest, issuerPath);
            String softwareStatement = BeanUtils.getProperty(registrationRequest, ssaPath);
            if (issuer != null && softwareStatement != null) {
                String softwareId = JWTUtils.decodeRequestJWT(softwareStatement, "body")
                        .getAsString(DCRCommonConstants.SOFTWARE_ID);
                if (softwareId != null && softwareId.equals(issuer)) {
                    return true;
                }

            } else {
                return true;
            }

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("Error while resolving validation fields", e);
        } catch (ParseException e) {
            log.error("Error while parsing the softwareStatement", e);
        }

        return false;
    }
}
