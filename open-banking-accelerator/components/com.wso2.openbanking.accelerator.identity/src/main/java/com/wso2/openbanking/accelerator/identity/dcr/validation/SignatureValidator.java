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

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.SignedJWT;
import com.wso2.openbanking.accelerator.common.identity.IdentityConstants;
import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.common.util.OpenBankingUtils;
import com.wso2.openbanking.accelerator.identity.dcr.validation.annotation.ValidateSignature;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.text.ParseException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator class for signature validation of SSA.
 */
public class SignatureValidator implements ConstraintValidator<ValidateSignature, Object> {

    private static final Log log = LogFactory.getLog(SignatureValidator.class);

    private String softwareStatementPath;
    private String ssaBodyPath;

    @Override
    public void initialize(ValidateSignature validateSignature) {

        this.softwareStatementPath = validateSignature.ssa();
        this.ssaBodyPath = validateSignature.ssaBody();
    }

    @Override
    public boolean isValid(Object registrationRequest,
                           ConstraintValidatorContext constraintValidatorContext) {

        try {
            String softwareStatement = BeanUtils.getProperty(registrationRequest, softwareStatementPath);
            if (StringUtils.isEmpty(softwareStatement)) {
                return true;
            }
            SignedJWT signedJWT = SignedJWT.parse(softwareStatement);
            String jwtString = signedJWT.getParsedString();
            String alg = signedJWT.getHeader().getAlgorithm().getName();
            String softwareEnvironmentFromSSA = OpenBankingUtils.getSoftwareEnvironmentFromSSA(jwtString);
            String jwksURL;

            if (IdentityConstants.PRODUCTION.equals(softwareEnvironmentFromSSA)) {
                // validate the signature against production jwks
                jwksURL = IdentityExtensionsDataHolder.getInstance().getConfigurationMap()
                        .get(DCRCommonConstants.DCR_JWKS_ENDPOINT_PRODUCTION).toString();
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Validating the signature from Production JwksUrl %s",
                            jwksURL.replaceAll("[\r\n]", "")));
                }
            } else {
                // else validate the signature against sandbox jwks
                jwksURL = IdentityExtensionsDataHolder.getInstance().getConfigurationMap()
                        .get(DCRCommonConstants.DCR_JWKS_ENDPOINT_SANDBOX).toString();
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Validating the signature from Sandbox JwksUrl %s",
                            jwksURL.replaceAll("[\r\n]", "")));
                }
            }
            return isValidateJWTSignature(jwksURL, jwtString, alg);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("Error while resolving validation fields", e);
        } catch (ParseException e) {
            log.error("Error while parsing the JWT string", e);
        }
        return false;
    }

    private boolean isValidateJWTSignature(String jwksURL, String jwtString, String alg) {

        try {
            return JWTUtils.validateJWTSignature(jwtString, jwksURL, alg);
        } catch (ParseException e) {
            log.error("Error while parsing the JWT string", e);
        } catch (JOSEException | BadJOSEException | MalformedURLException e) {
            log.error("Error occurred while validating the signature", e);
        }
        return false;
    }
}

