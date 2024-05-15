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

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.identity.app2app.model.AppAuthValidationJWT;
import com.wso2.openbanking.accelerator.identity.app2app.validations.annotations.ValidateSignature;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator class for validating the signature of a JWT.
 */
public class PublicKeySignatureValidator implements ConstraintValidator<ValidateSignature, AppAuthValidationJWT> {

    private static final Log log = LogFactory.getLog(PublicKeySignatureValidator.class);

    @Override
    public boolean isValid(AppAuthValidationJWT appAuthValidationJWT, ConstraintValidatorContext constraintValidatorContext) {

        try {

            SignedJWT signedJWT = appAuthValidationJWT.getSignedJWT();
            String publicKey = appAuthValidationJWT.getPublicKey();
            String algorithm = appAuthValidationJWT.getSigningAlgorithm();
            if (!JWTUtils.validateJWTSignature(signedJWT, publicKey, algorithm)) {
                log.error("Signature can't be verified with registered public key.");
                return false;
            }

        } catch (NoSuchAlgorithmException e) {
            log.error("No such algorithm found.", e);
            return false;
        } catch (InvalidKeySpecException e) {
            log.error("Invalid key spec.", e);
            return false;
        } catch (JOSEException e) {
            log.error("JOSE exception", e);
            return false;
        }
        return true;

    }
}

