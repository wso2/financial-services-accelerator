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

package com.wso2.openbanking.accelerator.identity.auth.extensions.request.validator.annotations;

import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonConstants;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonHelper;
import com.wso2.openbanking.accelerator.identity.util.IdentityCommonUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


/**
 * To validate if the signing algorithm used to sign request object
 * is the same as the algorithm given during registration.
 */
public class SigningAlgorithmValidator implements ConstraintValidator<ValidSigningAlgorithm, Object> {

    private String algorithmXpath;
    private String clientIdXPath;
    private static Log log = LogFactory.getLog(SigningAlgorithmValidator.class);

    @Override
    public void initialize(ValidSigningAlgorithm constraintAnnotation) {

        this.algorithmXpath = constraintAnnotation.algorithm();
        this.clientIdXPath = constraintAnnotation.clientId();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {

        try {
            final String algorithm = new PropertyUtilsBean().getProperty(object, algorithmXpath).toString();
            final String clientId = BeanUtils.getProperty(object, clientIdXPath);

            return algorithmValidate(algorithm, clientId);

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | NestedNullException e) {
            log.error("Error while resolving validation fields", e);
            return false;
        }
    }

     boolean algorithmValidate(String requestedAlgo, String clientId) {

        try {
            if (!(StringUtils.isNotEmpty(new IdentityCommonHelper().getCertificateContent(clientId))
                    && IdentityCommonUtil.getRegulatoryFromSPMetaData(clientId))) {
                String registeredAlgo = new IdentityCommonHelper().getAppPropertyFromSPMetaData(
                        clientId, IdentityCommonConstants.REQUEST_OBJECT_SIGNING_ALG);

                return requestedAlgo.equals(registeredAlgo);
            } else {
                return true;
            }
        } catch (OpenBankingException e) {
            log.error("Error while getting signing SP metadata", e);
        }

        return false;
    }
}
