/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.identity.extensions.dcr.validators;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigParser;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Validator class for validating the required parameters.
 */
public class RequiredParamsValidator {

    private static final Log log = LogFactory.getLog(RequiredParamsValidator.class);

    public static void validate(Map<String, Object> requestParameterMap, Map<String, Object> ssaParams)
            throws FinancialServicesException {

        Map<String, Map<String, Object>> dcrConfigs = FinancialServicesConfigParser.getInstance().getDCRParamsConfig();

        for (Map.Entry<String, Map<String, Object>> paramConfig : dcrConfigs.entrySet()) {
            //convert first letter to lowercase in DCR registration config parameters
            String camelCaseConfigParam = convertFirstLetterToLowerCase(paramConfig.getKey());
            Map additionalAttributes = (Map) requestParameterMap.get("additionalAttributes");
            //check whether required parameters are available in the request as expected
            if (Boolean.parseBoolean(paramConfig.getValue().get("Required").toString())) {
                if (!requestParameterMap.containsKey(camelCaseConfigParam) &&
                        !additionalAttributes.containsKey(paramConfig.getValue().get("Key"))) {
                    throw new FinancialServicesException("Required parameter " + camelCaseConfigParam +
                                    " cannot be null");
                }

                Object value;
                if (requestParameterMap.containsKey(camelCaseConfigParam)) {
                    value = requestParameterMap.get(camelCaseConfigParam);
                } else {
                    value = additionalAttributes.get(paramConfig.getValue().get("Key"));
                }
                //validate list type required parameters
                if (value instanceof List) {
                    List param = (List) value;
                    if (param.isEmpty()) {
                        throw  new FinancialServicesException("Required parameter " + camelCaseConfigParam +
                                " cannot be empty");
                    }
                }
                //validate string type required parameters
                if (value instanceof String) {
                    String param = (String) value;
                    if (StringUtils.isBlank(param)) {
                        throw new FinancialServicesException("Required parameter " + camelCaseConfigParam +
                                " cannot be empty");
                    }
                }
            }
            Object value;
            if (requestParameterMap.containsKey(camelCaseConfigParam)) {
                value = requestParameterMap.get(camelCaseConfigParam);
            } else {
                value = additionalAttributes.get(paramConfig.getValue().get("Key"));
            }
            //checks whether <AllowedValues> tag is set in config and is not empty.
            if (paramConfig.getValue().get("AllowedValues") != null && value != null) {
                //checks whether allowed values configurations contain any empty values
                if (!((List) paramConfig.getValue().get("AllowedValues")).contains("")) {
                    //validate against allowed values provided in config
                    List allowedList = (List) paramConfig.getValue().get("AllowedValues");
                    if (!allowedList.isEmpty()) {
                        //validate array type parameters
                        if (value instanceof List) {
                            List<String> params = (ArrayList<String>) value;
                            for (Object paramObject : params) {
                                if (paramObject instanceof String) {
                                    String param = (String) paramObject;
                                    if (!allowedList.contains(param)) {
                                        throw new FinancialServicesException("Invalid " + camelCaseConfigParam +
                                                " provided");
                                    }
                                }
                            }
                        }
                        //validate string type parameters
                        if (value instanceof String) {
                            String param = (String) value;
                            //check scope validation since request is sending a space separated scopes list
                            if (camelCaseConfigParam.equals("scope")) {
                                List<String> scopeList = Arrays.asList(param.split(" "));
                                for (String scope : scopeList) {
                                    if (!allowedList.contains(scope)) {
                                        throw new FinancialServicesException("Invalid " + camelCaseConfigParam +
                                                " provided");
                                    }
                                }
                            } else if (!allowedList.contains(param)) {
                                throw new FinancialServicesException("Invalid " + camelCaseConfigParam + " provided");
                            }
                        }
                    }
                }
            }
        }
    }

    private static String convertFirstLetterToLowerCase(String configParameterValue) {
        return configParameterValue.substring(0, 1).toLowerCase(Locale.ENGLISH) + configParameterValue.substring(1);
    }
}
