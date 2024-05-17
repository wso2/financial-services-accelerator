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

import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.identity.app2app.model.DeviceVerificationToken;
import com.wso2.openbanking.accelerator.identity.app2app.validations.annotations.ValidateExpiry;

import java.util.Date;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator class for validating expiry of a device verification token..
 */
public class ExpiryValidator implements ConstraintValidator<ValidateExpiry, DeviceVerificationToken> {

    private static final long DEFAULT_TIME_SKEW_IN_SECONDS = 300L;

    @Override
    public boolean isValid(DeviceVerificationToken deviceVerificationToken,
                           ConstraintValidatorContext constraintValidatorContext) {

        Date expiryTime = deviceVerificationToken.getExpirationTime();
        return JWTUtils.isValidExpiryTime(expiryTime, DEFAULT_TIME_SKEW_IN_SECONDS);
    }
}

