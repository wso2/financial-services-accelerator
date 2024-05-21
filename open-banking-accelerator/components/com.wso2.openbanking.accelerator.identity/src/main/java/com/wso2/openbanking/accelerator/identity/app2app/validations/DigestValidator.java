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

import com.wso2.openbanking.accelerator.identity.app2app.model.DeviceVerificationToken;
import com.wso2.openbanking.accelerator.identity.app2app.validations.annotations.ValidateDigest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator class for validating digest of a device verification token.
 */
public class DigestValidator implements ConstraintValidator<ValidateDigest, DeviceVerificationToken> {

    private static final Log log = LogFactory.getLog(DigestValidator.class);

    /**
     * Checks if the given device verification token is valid based on its digest.
     *
     * @param deviceVerificationToken The device verification token to be validated.
     * @param constraintValidatorContext The context in which the validation is performed.
     * @return true if the token is valid, false otherwise.
     */
    @Override
    public boolean isValid(DeviceVerificationToken deviceVerificationToken,
                           ConstraintValidatorContext constraintValidatorContext) {

        String requestObject = deviceVerificationToken.getRequestObject();
        String digest = deviceVerificationToken.getDigest();
        return validateDigest(digest, requestObject);
    }

    /**
     * Validating the digest of the request.
     *
     * @param digestHeader digest header sent with the request
     * @param request the request JWT String
     * @return return true if the digest validation is a success, false otherwise
     */
    protected boolean validateDigest(String digestHeader, String request) {

        if (StringUtils.isBlank(request)) {
            //If the request is null nothing to validate.
            return true;
        } else if (StringUtils.isBlank(digestHeader)) {
            //If request is not empty and digest us empty validation fails.
            return false;
        }

        try {
            String[] digestAttribute = digestHeader.split("=", 2);
            if (digestAttribute.length != 2) {
                log.error("Invalid digest header.");
                return false;
            }
            String digestAlgorithm = digestAttribute[0].trim();
            String digestValue = digestAttribute[1].trim();
            MessageDigest messageDigest = MessageDigest.getInstance(digestAlgorithm);
            byte[] digestHash = messageDigest.digest(request.getBytes(StandardCharsets.UTF_8));
            String generatedDigest = Base64.getEncoder()
                    .encodeToString(digestHash);

            if (generatedDigest.equals(digestValue)) {
                return true;
            }

        } catch (NoSuchAlgorithmException e) {
            log.error("Invalid algorithm.", e);
            return false;
        }

        return false;
    }
}
