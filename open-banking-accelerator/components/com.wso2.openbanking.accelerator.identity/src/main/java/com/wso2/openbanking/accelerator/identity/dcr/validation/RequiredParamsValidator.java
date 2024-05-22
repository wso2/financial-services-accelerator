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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wso2.openbanking.accelerator.identity.dcr.model.RegistrationRequest;
import com.wso2.openbanking.accelerator.identity.dcr.validation.annotation.ValidateRequiredParams;
import com.wso2.openbanking.accelerator.identity.internal.IdentityExtensionsDataHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


/**
 * Validator class for validating the required parameters.
 */
public class RequiredParamsValidator implements ConstraintValidator<ValidateRequiredParams, Object> {

    private static final Log log = LogFactory.getLog(RequiredParamsValidator.class);

    private static ObjectMapper objMapper = new ObjectMapper();

    @Override
    public boolean isValid(Object registrationRequestObject, ConstraintValidatorContext constraintValidatorContext) {

        RegistrationRequest registrationRequest = (RegistrationRequest) registrationRequestObject;

        Map<String, Object> requestParameterMap = objMapper.convertValue(registrationRequest, Map.class);

        Map<String, Map<String, Object>> dcrConfigs = IdentityExtensionsDataHolder.getInstance()
                .getDcrRegistrationConfigMap();

        for (Map.Entry<String, Map<String, Object>> paramConfig : dcrConfigs.entrySet()) {
            //convert first letter to lowercase in DCR registration config parameters
            String camelCaseConfigParam = convertFirstLetterToLowerCase(paramConfig.getKey());
            //check whether required parameters are available in the request as expected
            if (DCRCommonConstants.DCR_REGISTRATION_PARAM_REQUIRED_TRUE
                    .equalsIgnoreCase((String) paramConfig.getValue()
                            .get(DCRCommonConstants.DCR_REGISTRATION_PARAM_REQUIRED))) {
                if (requestParameterMap.get(camelCaseConfigParam) == null) {
                    constraintValidatorContext.disableDefaultConstraintViolation();
                    constraintValidatorContext
                            .buildConstraintViolationWithTemplate("Required parameter " + camelCaseConfigParam +
                                    " cannot be null:" + DCRCommonConstants.INVALID_META_DATA)
                            .addConstraintViolation();
                    return false;
                }
                //validate list type required parameters
                if (requestParameterMap.get(camelCaseConfigParam) instanceof List) {
                    List param = (List) requestParameterMap.get(camelCaseConfigParam);
                    if (param.isEmpty()) {
                        constraintValidatorContext.disableDefaultConstraintViolation();
                        constraintValidatorContext
                                .buildConstraintViolationWithTemplate("Required parameter " + camelCaseConfigParam +
                                        " cannot be empty:" + DCRCommonConstants.INVALID_META_DATA)
                                .addConstraintViolation();
                        return false;
                    }
                }
                //validate string type required parameters
                if (requestParameterMap.get(camelCaseConfigParam) instanceof String) {
                    String param = (String) requestParameterMap.get(camelCaseConfigParam);
                    if (StringUtils.isBlank(param)) {
                        constraintValidatorContext.disableDefaultConstraintViolation();
                        constraintValidatorContext
                                .buildConstraintViolationWithTemplate("Required parameter " + camelCaseConfigParam +
                                        " cannot be empty:" + DCRCommonConstants.INVALID_META_DATA)
                                .addConstraintViolation();
                        return false;
                    }
                }
            }
            //checks whether <AllowedValues> tag is set in config and is not empty.
            if (paramConfig.getValue().get(DCRCommonConstants.DCR_REGISTRATION_PARAM_ALLOWED_VALUES) != null &&
                    requestParameterMap.get(camelCaseConfigParam) != null) {
                //checks whether allowed values configurations contain any empty values
                if (!((List) paramConfig.getValue().get(DCRCommonConstants.DCR_REGISTRATION_PARAM_ALLOWED_VALUES))
                        .contains("")) {
                    //validate against allowed values provided in config
                    List allowedList = (List) paramConfig.getValue()
                            .get(DCRCommonConstants.DCR_REGISTRATION_PARAM_ALLOWED_VALUES);
                    //validate array type parameters
                    if (requestParameterMap.get(camelCaseConfigParam) instanceof List) {
                        List<String> params = (ArrayList<String>) requestParameterMap.get(camelCaseConfigParam);
                        for (Object paramObject : params) {
                            if (paramObject instanceof String) {
                                String param = (String) paramObject;
                                if (!allowedList.contains(param)) {
                                    constraintValidatorContext.disableDefaultConstraintViolation();
                                    constraintValidatorContext
                                            .buildConstraintViolationWithTemplate("Invalid " +
                                                    camelCaseConfigParam + " provided:" +
                                                    DCRCommonConstants.INVALID_META_DATA).addConstraintViolation();
                                    return false;
                                }
                            }

                        }
                    }
                    //validate string type parameters
                    if (requestParameterMap.get(camelCaseConfigParam) instanceof String) {
                        String param = (String) requestParameterMap.get(camelCaseConfigParam);
                        //check scope validation since request is sending a space separated scopes list
                        if (camelCaseConfigParam.equalsIgnoreCase(DCRCommonConstants.DCR_REGISTRATION_PARAM_SCOPE)) {
                            List<String> scopeList = Arrays.asList(param.split(" "));
                            for (String scope : scopeList) {
                                if (!allowedList.contains(scope)) {
                                    constraintValidatorContext
                                            .buildConstraintViolationWithTemplate("Invalid " +
                                                    camelCaseConfigParam + " provided:" +
                                                    DCRCommonConstants.INVALID_META_DATA).addConstraintViolation();
                                    return false;
                                }
                            }
                        } else if (!allowedList.contains(param)) {
                            constraintValidatorContext.disableDefaultConstraintViolation();
                            constraintValidatorContext
                                    .buildConstraintViolationWithTemplate("Invalid " +
                                            camelCaseConfigParam + " provided:" +
                                            DCRCommonConstants.INVALID_META_DATA).addConstraintViolation();
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private String convertFirstLetterToLowerCase(String configParameterValue) {
        return configParameterValue.substring(0, 1).toLowerCase(Locale.ENGLISH) + configParameterValue.substring(1);
    }
}
