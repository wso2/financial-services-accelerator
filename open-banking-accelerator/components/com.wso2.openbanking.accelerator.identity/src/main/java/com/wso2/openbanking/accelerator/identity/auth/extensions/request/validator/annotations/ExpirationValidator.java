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

package com.wso2.openbanking.accelerator.identity.auth.extensions.request.validator.annotations;

import com.wso2.openbanking.accelerator.identity.push.auth.extension.request.validator.constants.PushAuthRequestConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Date;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * To validate if the expiration claim provided is valid.
 */
public class ExpirationValidator implements ConstraintValidator<ValidExpiration, Object> {

    private String expirationXPath;
    private static Log log = LogFactory.getLog(ExpirationValidator.class);

    @Override
    public void initialize(ValidExpiration constraintAnnotation) {

        this.expirationXPath = constraintAnnotation.expiration();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {

        String errorMessage;
        try {
            final String expClaimInDateTimeFormat = BeanUtils.getProperty(object, expirationXPath);

            if (StringUtils.isNotBlank(expClaimInDateTimeFormat)) {
                Date expirationDate = IdentityCommonUtil.parseStringToDate(expClaimInDateTimeFormat);
                long timeStampSkewMillis = OAuthServerConfiguration.getInstance().getTimeStampSkewInSeconds() * 1000;
                long expirationTimeInMillis = expirationDate.getTime();
                long currentTimeInMillis = System.currentTimeMillis();
                // exp parameter should not be over 1 hour in the future.
                if ((expirationTimeInMillis - (currentTimeInMillis + timeStampSkewMillis)) >
                        PushAuthRequestConstants.ONE_HOUR_IN_MILLIS) {
                    errorMessage = "exp parameter in the request object is over 1 hour in the future";
                    log.debug(errorMessage);
                    IdentityCommonUtil.setCustomErrorMessage(constraintValidatorContext, errorMessage);
                    return false;
                }
                // exp parameter should not be in the past.
                if ((currentTimeInMillis + timeStampSkewMillis) > expirationTimeInMillis) {
                    errorMessage = "Request object expired";
                    log.debug(errorMessage);
                    IdentityCommonUtil.setCustomErrorMessage(constraintValidatorContext, errorMessage);
                    return false;
                }
            } else {
                errorMessage = "exp parameter is missing in the request object";
                log.debug(errorMessage);
                IdentityCommonUtil.setCustomErrorMessage(constraintValidatorContext,
                        errorMessage);
                return false;
            }

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException exception) {
            log.error("Error while resolving validation fields", exception);
            return false;
        } catch (ParseException exception) {
            log.error("Error while parsing exp value", exception);
            return false;
        }

        return true;
    }

}
