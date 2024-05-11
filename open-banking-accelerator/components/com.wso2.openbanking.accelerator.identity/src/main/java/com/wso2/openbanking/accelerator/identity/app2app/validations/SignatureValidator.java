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
import com.wso2.openbanking.accelerator.identity.app2app.model.Secret;
import com.wso2.openbanking.accelerator.identity.app2app.utils.App2AppAuthUtils;
import com.wso2.openbanking.accelerator.identity.app2app.validations.annotations.ValidateSignature;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authenticator.push.device.handler.exception.PushDeviceHandlerClientException;
import org.wso2.carbon.identity.application.authenticator.push.device.handler.exception.PushDeviceHandlerServerException;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator class for validating the signature of a JWT.
 */
// TODO: change the name of this implementation
public class SignatureValidator implements ConstraintValidator<ValidateSignature, Secret> {
    private static final Log log = LogFactory.getLog(SignatureValidator.class);
    private String algorithm;
    @Override
    public void initialize(ValidateSignature validateSignature) {

        this.algorithm = validateSignature.algorithm();

    }

    @Override
    public boolean isValid(Secret secret, ConstraintValidatorContext constraintValidatorContext) {

        try {

            SignedJWT signedJWT = secret.getSignedJWT();
            String deviceID = secret.getDeviceId();
            AuthenticatedUser authenticatedUser = secret.getAuthenticatedUser();
            UserRealm userRealm = App2AppAuthUtils.getUserRealm(authenticatedUser);
            String userID = App2AppAuthUtils.getUserIdFromUsername(authenticatedUser.getUserName(), userRealm);
            String publicKey = App2AppAuthUtils.getPublicKey(deviceID, userID);

            if (!JWTUtils.validateJWTSignature(signedJWT, publicKey, algorithm)) {
                log.error("Signature can't be verified with registered public key.");
                return false;
            }
        } catch (UserStoreException e) {
            log.error("Error while creating authenticated user.", e);
            return false;
        } catch (PushDeviceHandlerServerException e) {
            log.error("Error occurred push device handler service.", e);
            return false;
        } catch (PushDeviceHandlerClientException e) {
            log.error("Push Device handler client.", e);
            return false;
        } catch (NoSuchAlgorithmException e) {
            log.error("No such algorithm found -" + algorithm + ".", e);
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

