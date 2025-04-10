/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.util.JWTUtils;
import com.wso2.openbanking.accelerator.identity.dcr.validation.annotation.ValidateRequiredSsaParams;
import net.minidev.json.JSONObject;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator class for mandatory parameter validation in SSA
 */
public class RequiredSsaParamsValidator implements ConstraintValidator<ValidateRequiredSsaParams, Object> {

    private static final Log log = LogFactory.getLog(RequiredSsaParamsValidator.class);

    private String softwareStatementPath;

    @Override
    public void initialize(ValidateRequiredSsaParams validateMandatorySSAParams) {

        this.softwareStatementPath = validateMandatorySSAParams.ssa();
    }

    @Override
    public boolean isValid(Object registrationRequest, ConstraintValidatorContext constraintValidatorContext) {

        try {
            String softwareStatement = BeanUtils.getProperty(registrationRequest, softwareStatementPath);
            JSONObject softwareStatementJsonObject = JWTUtils.decodeRequestJWT(softwareStatement, "body");
            Map<String, Object> configuration = OpenBankingConfigParser.getInstance().getConfiguration();
            Object mandatorySsaParameterObjects = configuration.get(OpenBankingConstants.DCR_SSA_MANDATORY_PARAMETERS);
            List<String> mandatorySsaParameters = new ArrayList<>();
            if (mandatorySsaParameterObjects instanceof ArrayList) {
                mandatorySsaParameters = (ArrayList<String>) mandatorySsaParameterObjects;
            } else if (mandatorySsaParameterObjects instanceof String) {
                mandatorySsaParameters.add((String) mandatorySsaParameterObjects);
            }

            for (String parameter : mandatorySsaParameters) {
                if (softwareStatementJsonObject.containsKey(parameter)) {

                    Object ssaValue = softwareStatementJsonObject.get(parameter);
                    if (ssaValue == null) {
                        constraintValidatorContext.disableDefaultConstraintViolation();
                        constraintValidatorContext
                                .buildConstraintViolationWithTemplate("Required SSA parameter `" + parameter +
                                        "` is null:" + DCRCommonConstants.INVALID_META_DATA)
                                .addConstraintViolation();
                        return false;
                    } else if (ssaValue instanceof String && StringUtils.isBlank(ssaValue.toString())) {
                        constraintValidatorContext.disableDefaultConstraintViolation();
                        constraintValidatorContext
                                .buildConstraintViolationWithTemplate("Required SSA parameter `" + parameter +
                                        "` is blank:" + DCRCommonConstants.INVALID_META_DATA)
                                .addConstraintViolation();
                        return false;
                    } else if ((ssaValue instanceof ArrayList && ((ArrayList<?>) ssaValue).isEmpty()) ||
                            (ssaValue instanceof JSONObject && ((JSONObject) ssaValue).isEmpty())) {
                        constraintValidatorContext.disableDefaultConstraintViolation();
                        constraintValidatorContext
                                .buildConstraintViolationWithTemplate("Required SSA parameter `" + parameter +
                                        "` is empty:" + DCRCommonConstants.INVALID_META_DATA)
                                .addConstraintViolation();
                        return false;
                    }
                } else {
                    constraintValidatorContext.disableDefaultConstraintViolation();
                    constraintValidatorContext
                            .buildConstraintViolationWithTemplate("Required SSA parameter `" + parameter +
                                    "` not found:" + DCRCommonConstants.INVALID_META_DATA)
                            .addConstraintViolation();
                    return false;
                }
            }

            return true;

        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            log.error("Error while resolving validation fields", e);
        } catch (ParseException e) {
            log.error("Error while parsing the softwareStatement", e);
        }
        return false;
    }
}
