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

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
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
 * To validate if the not before claim provided is valid.
 */
public class NbfExpClaimsValidator implements ConstraintValidator<ValidNbfExpClaims, Object> {

    private static Log log = LogFactory.getLog(NbfExpClaimsValidator.class);
    private String notBeforeXPath;
    private String expirationXPath;

    @Override
    public void initialize(ValidNbfExpClaims constraintAnnotation) {

        this.notBeforeXPath = constraintAnnotation.notBefore();
        this.expirationXPath = constraintAnnotation.expiration();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {

        String errorMessage;
        try {
            final String nbfClaimInDateTimeFormat = BeanUtils.getProperty(object, notBeforeXPath);
            final String expClaimInDateTimeFormat = BeanUtils.getProperty(object, expirationXPath);

            if (StringUtils.isNotBlank(nbfClaimInDateTimeFormat)) {
                Date notBeforeDate = IdentityCommonUtil.parseStringToDate(nbfClaimInDateTimeFormat);
                long timeStampSkewMillis = OAuthServerConfiguration.getInstance().getTimeStampSkewInSeconds() * 1000;
                long notBeforeTimeInMillis = notBeforeDate.getTime();
                long currentTimeInMillis = System.currentTimeMillis();
                // request object should be used on or after nbf value.
                if ((currentTimeInMillis + timeStampSkewMillis) < notBeforeTimeInMillis) {
                    // Sync error msg to match with product IS.
                    errorMessage = "Error occurred while validating request object signature using jwks endpoint: " +
                            "request object is not valid yet.";
                    log.debug(errorMessage);
                    IdentityCommonUtil.setCustomErrorMessage(constraintValidatorContext, errorMessage);
                    return false;
                }
                // nbf parameter should not be over 1 hour in the past.
                if (((currentTimeInMillis + timeStampSkewMillis) - notBeforeTimeInMillis) >
                        PushAuthRequestConstants.ONE_HOUR_IN_MILLIS) {
                    errorMessage = "nbf parameter in the request object is over 1 hour in the past";
                    log.debug(errorMessage);
                    IdentityCommonUtil.setCustomErrorMessage(constraintValidatorContext, errorMessage);
                    return false;
                }

                // exp time should not be older than 1 hour from nbf time.
                Date expirationDate = IdentityCommonUtil.parseStringToDate(expClaimInDateTimeFormat);
                long expirationTimeInMillis = expirationDate.getTime();
                if ((expirationTimeInMillis - notBeforeTimeInMillis) > PushAuthRequestConstants.ONE_HOUR_IN_MILLIS) {
                    errorMessage = "Request Object expiry time is too far in the future than not before time.";
                    log.debug(errorMessage);
                    IdentityCommonUtil.setCustomErrorMessage(constraintValidatorContext, errorMessage);
                    return false;
                }

            } else {
                if (!OpenBankingConfigParser.getInstance().isNbfClaimMandatory()) {
                    // This config added to preserve backward compatibility when moving from FAPI ID2 to FAPI 1 Advance.
                    return true;
                }
                errorMessage = "nbf parameter is missing in the request object";
                log.debug(errorMessage);
                IdentityCommonUtil.setCustomErrorMessage(constraintValidatorContext,
                        errorMessage);
                return false;
            }

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException exception) {
            log.error("Error while resolving validation fields", exception);
            return false;
        } catch (ParseException exception) {
            log.error("Error while parsing nbf value", exception);
            return false;
        }

        return true;
    }

}
