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

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.oauth.dcr.bean.ApplicationRegistrationRequest;
import org.wso2.carbon.identity.oauth.dcr.bean.ApplicationUpdateRequest;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Validator class for validating the required parameters.
 */
public class RequiredParamsValidator implements DynamicClientRegistrationValidator {

    private static final Log log = LogFactory.getLog(RequiredParamsValidator.class);
    private static final Gson gson = new Gson();

    @Override
    public void validatePost(ApplicationRegistrationRequest applicationRegistrationRequest,
                             Map<String, Object> ssaParams) throws FinancialServicesException {

        Map<String, Object> requestParameterMap = gson.fromJson(gson.toJson(applicationRegistrationRequest), Map.class);
        validateRequiredAttributes(requestParameterMap, ssaParams);
    }

    @Override
    public void validateGet(Map<String, String> ssaParams) throws FinancialServicesException {

    }

    @Override
    public void validateUpdate(ApplicationUpdateRequest applicationUpdateRequest, Map<String, Object> ssaParams,
                               ServiceProviderProperty[] serviceProviderProperties) throws FinancialServicesException {


        Map<String, Object> requestParameterMap = gson.fromJson(gson.toJson(applicationUpdateRequest), Map.class);
        validateRequiredAttributes(requestParameterMap, ssaParams);
    }

    /**
     * Validate the required parameters.
     */
    private static void validateRequiredAttributes(Map<String, Object> requestParamMap, Map<String, Object> ssaParams)
            throws FinancialServicesException {

        Map<String, Map<String, Object>> dcrConfigs = IdentityExtensionsDataHolder.getInstance()
                .getConfigurationService().getDCRParamsConfig();

        for (Map.Entry<String, Map<String, Object>> paramConfig : dcrConfigs.entrySet()) {
            //convert first letter to lowercase in DCR registration config parameters
            String camelCaseConfigParam = convertFirstLetterToLowerCase(paramConfig.getKey());
            Map additionalAttributes = (Map) requestParamMap.get("additionalAttributes");
            //check whether required parameters are available in the request as expected
            if (Boolean.parseBoolean(paramConfig.getValue().get("Required").toString())) {
                if (!requestParamMap.containsKey(camelCaseConfigParam) &&
                        !additionalAttributes.containsKey(paramConfig.getValue().get("Key"))) {
                    String errorMessage = String.format("Required parameter %s not found in the request",
                            camelCaseConfigParam.replaceAll("[\r\n]", ""));
                    log.error(errorMessage);
                    throw new FinancialServicesException(errorMessage);
                }

                Object value;
                if (requestParamMap.containsKey(camelCaseConfigParam)) {
                    value = requestParamMap.get(camelCaseConfigParam);
                } else {
                    value = additionalAttributes.get(paramConfig.getValue().get("Key"));
                }
                //validate list type required parameters
                if (value instanceof List) {
                    List param = (List) value;
                    if (param.isEmpty()) {
                        String errorMessage = String.format("Required parameter %s cannot be empty",
                                camelCaseConfigParam.replaceAll("[\r\n]", ""));
                        log.error(errorMessage);
                        throw  new FinancialServicesException(errorMessage);
                    }

                    boolean isAnyEmpty = param.stream().anyMatch(Objects::isNull);
                    if (isAnyEmpty) {
                        String errorMessage = String.format("Required parameter %s cannot be empty",
                                camelCaseConfigParam.replaceAll("[\r\n]", ""));
                        log.error(errorMessage);
                        throw new FinancialServicesException(errorMessage);
                    }

                }
                //validate string type required parameters
                if (value instanceof String) {
                    String param = (String) value;
                    if (StringUtils.isBlank(param)) {
                        String errorMessage = String.format("Required parameter %s cannot be empty",
                                camelCaseConfigParam.replaceAll("[\r\n]", ""));
                        log.error(errorMessage);
                        throw new FinancialServicesException(errorMessage);
                    }
                }
            }
            Object value;
            if (requestParamMap.containsKey(camelCaseConfigParam)) {
                value = requestParamMap.get(camelCaseConfigParam);
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
                                        String errorMessage = String.format("Invalid %s provided",
                                                camelCaseConfigParam.replaceAll("[\r\n]", ""));
                                        log.error(errorMessage);
                                        throw new FinancialServicesException(errorMessage);
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
                                        String errorMessage = String.format("Invalid %s provided",
                                                camelCaseConfigParam.replaceAll("[\r\n]", ""));
                                        log.error(errorMessage);
                                        throw new FinancialServicesException(errorMessage);
                                    }
                                }
                            } else if (!allowedList.contains(param)) {
                                String errorMessage = String.format("Invalid %s provided",
                                        camelCaseConfigParam.replaceAll("[\r\n]", ""));
                                log.error(errorMessage);
                                throw new FinancialServicesException(errorMessage);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Convert the first letter of the parameter to lowercase.
     */
    private static String convertFirstLetterToLowerCase(String configParameterValue) {
        return configParameterValue.substring(0, 1).toLowerCase(Locale.ENGLISH) + configParameterValue.substring(1);
    }
}
