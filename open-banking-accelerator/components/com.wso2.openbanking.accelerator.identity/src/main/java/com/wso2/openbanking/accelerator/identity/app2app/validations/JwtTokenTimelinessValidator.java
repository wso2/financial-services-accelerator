/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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
package com.wso2.openbanking.accelerator.identity.app2app.validations;

import com.nimbusds.jwt.JWTClaimsSet;
import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.identity.app2app.model.Secret;
import com.wso2.openbanking.accelerator.identity.app2app.validations.annotations.ValidateTimeliness;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator class for validating the timeliness of a JWT.
 * Validates the expiry.
 * Validates the nbf.
 */
public class JwtTokenTimelinessValidator implements ConstraintValidator<ValidateTimeliness, Secret> {
    private static final Log log = LogFactory.getLog(JwtTokenTimelinessValidator.class);
    @Override
    public boolean isValid(Secret secret, ConstraintValidatorContext constraintValidatorContext) {

        JWTClaimsSet jwtClaimsSet = secret.getJwtClaimsSet();

        //Validating the exp of the JWT
        if (!JWTUtils.validateExpiryTime(jwtClaimsSet)) {
            log.error("JWT Expired.");
            return false;
        }

        //Validating the nbf of the JWT
        if (!JWTUtils.validateNotValidBefore(jwtClaimsSet)) {
            log.error("JWT is not active.");
            return false;
        }

        return true;
    }
}
