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

import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.identity.dcr.validation.annotation.ValidateAlgorithm;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator class for validating the allowed algorithms.
 */
public class AlgorithmValidator implements ConstraintValidator<ValidateAlgorithm, Object> {

    private String idTokenSigningAlgPath;
    private String requestObjectSigningAlgPath;
    private String tokenAuthSignignAlgPath;
    private static Log log = LogFactory.getLog(AlgorithmValidator.class);

    @Override
    public void initialize(ValidateAlgorithm validateAlgorithm) {

        this.idTokenSigningAlgPath = validateAlgorithm.idTokenAlg();
        this.requestObjectSigningAlgPath = validateAlgorithm.reqObjAlg();
        this.tokenAuthSignignAlgPath = validateAlgorithm.tokenAuthAlg();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {

        List<String> allowedAlgorithmsList = new ArrayList<>();
        Object allowedAlgorithms = IdentityExtensionsDataHolder.getInstance()
                .getConfigurationMap().get(OpenBankingConstants.SIGNATURE_ALGORITHMS);
        if (allowedAlgorithms instanceof List) {
            allowedAlgorithmsList = (List<String>) allowedAlgorithms;
        } else {
            allowedAlgorithmsList.add(allowedAlgorithms.toString());
        }
        String requestedIdTokenSigningAlg = null;
        String requestedRequestObjSignignAlg = null;
        String requestedTokenAuthSigningAlg = null;
        try {
            requestedIdTokenSigningAlg = BeanUtils.getProperty(object, idTokenSigningAlgPath);
            requestedRequestObjSignignAlg = BeanUtils.getProperty(object, requestObjectSigningAlgPath);
            requestedTokenAuthSigningAlg = BeanUtils.getProperty(object, tokenAuthSignignAlgPath);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("Error while resolving validation fields", e);
            return false;
        }
        if (StringUtils.isNotEmpty(requestedIdTokenSigningAlg) &&
                !allowedAlgorithmsList.contains(requestedIdTokenSigningAlg)) {
            return false;
        }
        if (StringUtils.isNotEmpty(requestedRequestObjSignignAlg) &&
                !allowedAlgorithmsList.contains(requestedRequestObjSignignAlg)) {
            return false;
        }
        if (StringUtils.isNotEmpty(requestedTokenAuthSigningAlg) &&
                !allowedAlgorithmsList.contains(requestedTokenAuthSigningAlg)) {
            return false;
        }
        return true;
    }
}
