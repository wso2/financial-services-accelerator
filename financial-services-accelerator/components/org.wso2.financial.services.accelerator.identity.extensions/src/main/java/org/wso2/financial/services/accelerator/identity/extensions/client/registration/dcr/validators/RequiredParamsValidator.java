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

package org.wso2.financial.services.accelerator.identity.extensions.client.registration.dcr.validators;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.oauth.dcr.bean.ApplicationRegistrationRequest;
import org.wso2.carbon.identity.oauth.dcr.bean.ApplicationUpdateRequest;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesDCRException;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonConstants;

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
                             Map<String, Object> ssaParams) throws FinancialServicesDCRException {

        Map<String, Object> requestParameterMap = gson.fromJson(gson.toJson(applicationRegistrationRequest), Map.class);
        validateRequiredAttributes(requestParameterMap, ssaParams);
    }

    @Override
    public void validateGet(Map<String, String> ssaParams) throws FinancialServicesDCRException {

    }

    @Override
    public void validateUpdate(ApplicationUpdateRequest applicationUpdateRequest, Map<String, Object> ssaParams,
                               ServiceProviderProperty[] serviceProviderProperties)
            throws FinancialServicesDCRException {


        Map<String, Object> requestParameterMap = gson.fromJson(gson.toJson(applicationUpdateRequest), Map.class);
        validateRequiredAttributes(requestParameterMap, ssaParams);
    }

    /**
     * Validate the required parameters.
     */
    private static void validateRequiredAttributes(Map<String, Object> requestParamMap, Map<String, Object> ssaParams)
            throws FinancialServicesDCRException {

        Map<String, Map<String, Object>> dcrConfigs = IdentityExtensionsDataHolder.getInstance()
                .getConfigurationService().getDCRParamsConfig();

        for (Map.Entry<String, Map<String, Object>> paramConfig : dcrConfigs.entrySet()) {
            //convert first letter to lowercase in DCR registration config parameters
            String camelCaseConfigParam = convertFirstLetterToLowerCase(paramConfig.getKey());
            //check whether required parameters are available in the request as expected
            validateRequiredAttributesPresence(requestParamMap, camelCaseConfigParam, paramConfig);
            //validate against allowed values provided in config
            validatedAllowedValues(requestParamMap, camelCaseConfigParam, paramConfig);
        }
    }

    /**
     * Convert the first letter of the parameter to lowercase.
     */
    private static String convertFirstLetterToLowerCase(String configParameterValue) {
        return configParameterValue.substring(0, 1).toLowerCase(Locale.ENGLISH) + configParameterValue.substring(1);
    }

    /**
     * Validate the required attributes presence.
     *
     * @param requestParamMap  Request parameter map
     * @param camelCaseConfig  Camel case config
     * @param paramConfig      Parameter config
     * @throws FinancialServicesDCRException  when the required attribute is not present
     */
    private static void validateRequiredAttributesPresence(Map<String, Object> requestParamMap, String camelCaseConfig,
                                                           Map.Entry<String, Map<String, Object>> paramConfig)
            throws FinancialServicesDCRException {

        Map additionalAttributes = (Map) requestParamMap.get(IdentityCommonConstants.ADDITIONAL_ATTRIBUTES);

        if (Boolean.parseBoolean(paramConfig.getValue().get(IdentityCommonConstants.REQUIRED).toString())) {

            //check whether required parameters are available in the request or in additional attributes as expected
            containsRequiredParam(requestParamMap, camelCaseConfig, additionalAttributes, paramConfig);

            Object value;
            if (requestParamMap.containsKey(camelCaseConfig)) {
                value = requestParamMap.get(camelCaseConfig);
            } else {
                value = additionalAttributes.get(paramConfig.getValue().get(IdentityCommonConstants.KEY));
            }
            //validate list type required parameters
            if (value instanceof List) {
                List param = (List) value;
                if (param.isEmpty()) {
                    String errorMessage = String.format("Required parameter %s cannot be empty",
                            camelCaseConfig.replaceAll("[\r\n]", ""));
                    log.debug(errorMessage);
                    throw  new FinancialServicesDCRException(IdentityCommonConstants.INVALID_CLIENT_METADATA,
                            errorMessage);
                }

                boolean isAnyEmpty = param.stream().anyMatch(Objects::isNull);
                if (isAnyEmpty) {
                    String errorMessage = String.format("Required parameter %s cannot be empty",
                            camelCaseConfig.replaceAll("[\r\n]", ""));
                    log.debug(errorMessage);
                    throw new FinancialServicesDCRException(IdentityCommonConstants.INVALID_CLIENT_METADATA,
                            errorMessage);
                }

            }
            //validate string type required parameters
            if (value instanceof String) {
                String param = (String) value;
                if (StringUtils.isBlank(param)) {
                    String errorMessage = String.format("Required parameter %s cannot be empty",
                            camelCaseConfig.replaceAll("[\r\n]", ""));
                    log.debug(errorMessage);
                    if (IdentityCommonConstants.SOFTWARE_STATEMENT_CC.equals(camelCaseConfig)) {
                        throw new FinancialServicesDCRException(IdentityCommonConstants.INVALID_SOFTWARE_STATEMENT,
                                errorMessage);
                    }
                    throw new FinancialServicesDCRException(IdentityCommonConstants.INVALID_CLIENT_METADATA,
                            errorMessage);
                }
            }
        }
    }

    /**
     * Validate the allowed values.
     *
     * @param requestParamMap        Request parameter map
     * @param camelCaseConfigParam   Camel case config parameter
     * @param paramConfig            Parameter config
     * @throws FinancialServicesDCRException  when the allowed values are not valid
     */
    private static void validatedAllowedValues(Map<String, Object> requestParamMap, String camelCaseConfigParam,
                                               Map.Entry<String, Map<String, Object>> paramConfig)
            throws FinancialServicesDCRException {

        Map additionalAttributes = (Map) requestParamMap.get(IdentityCommonConstants.ADDITIONAL_ATTRIBUTES);
        Object value;
        if (requestParamMap.containsKey(camelCaseConfigParam)) {
            value = requestParamMap.get(camelCaseConfigParam);
        } else {
            value = additionalAttributes.get(paramConfig.getValue().get(IdentityCommonConstants.KEY));
        }
        //checks whether <AllowedValues> tag is set in config and is not empty.
        if (paramConfig.getValue().get(IdentityCommonConstants.ALLOWED_VALUES) != null && value != null) {
            //checks whether allowed values configurations contain any empty values
            if (!((List) paramConfig.getValue().get(IdentityCommonConstants.ALLOWED_VALUES)).contains("")) {
                //validate against allowed values provided in config
                List allowedList = (List) paramConfig.getValue().get(IdentityCommonConstants.ALLOWED_VALUES);
                if (!allowedList.isEmpty()) {
                    //validate array type parameters
                    if (value instanceof List) {
                        checkAllowedValuesInList(value, camelCaseConfigParam, allowedList);
                    }
                    //validate string type parameters
                    if (value instanceof String) {
                        String param = (String) value;
                        checkAllowedValuesInString(param, camelCaseConfigParam, allowedList);
                    }
                }
            }
        }
    }

    /**
     * Check whether the required parameter is present in the request.
     * @param requestParamMap        Request parameter map
     * @param camelCaseConfig        Camel case config
     * @param additionalAttributes   Additional attributes
     * @param paramConfig            Parameter config
     * @throws FinancialServicesDCRException when the required parameter is not present
     */
    private static void containsRequiredParam(Map<String, Object> requestParamMap, String camelCaseConfig,
                                  Map<String, String> additionalAttributes,
                                  Map.Entry<String, Map<String, Object>> paramConfig)
            throws FinancialServicesDCRException {

        String attributeKey = (String) paramConfig.getValue().get(IdentityCommonConstants.KEY);
        if (!requestParamMap.containsKey(camelCaseConfig) &&
                !additionalAttributes.containsKey(attributeKey)) {
            String errorMessage = String.format("Required parameter %s not found in the request",
                    camelCaseConfig.replaceAll("[\r\n]", ""));
            log.debug(errorMessage);
            if (IdentityCommonConstants.SOFTWARE_STATEMENT.equals(attributeKey)) {
                throw new FinancialServicesDCRException(IdentityCommonConstants.INVALID_SOFTWARE_STATEMENT,
                        errorMessage);
            }
            throw new FinancialServicesDCRException(IdentityCommonConstants.INVALID_CLIENT_METADATA,
                    errorMessage);
        }
    }

    /**
     * Check whether the allowed values are in the list.
     *
     * @param value                  object containing values
     * @param camelCaseConfigParam   Camel case config parameter
     * @param allowedList            Allowed list
     * @throws FinancialServicesDCRException
     */
    private static void checkAllowedValuesInList(Object value, String camelCaseConfigParam, List allowedList)
            throws FinancialServicesDCRException {
        List<String> params = (ArrayList<String>) value;
        for (Object paramObject : params) {
            if (paramObject instanceof String) {
                String param = (String) paramObject;
                if (!allowedList.contains(param)) {
                    String errorMessage = String.format("Invalid %s provided",
                            camelCaseConfigParam.replaceAll("[\r\n]", ""));
                    log.debug(errorMessage);
                    throw new FinancialServicesDCRException(IdentityCommonConstants.INVALID_CLIENT_METADATA,
                            errorMessage);
                }
            }
        }
    }

    /**
     * Check whether the allowed values are in the string.
     *
     * @param param                 parameter
     * @param camelCaseConfigParam  Camel case config parameter
     * @param allowedList           Allowed list
     * @throws FinancialServicesDCRException
     */
    private static void checkAllowedValuesInString(String param, String camelCaseConfigParam, List allowedList)
            throws FinancialServicesDCRException {

        //check scope validation since request is sending a space separated scopes list
        if (camelCaseConfigParam.equals(IdentityCommonConstants.SCOPE)) {
            List<String> scopeList = Arrays.asList(param.split(" "));
            for (String scope : scopeList) {
                if (!allowedList.contains(scope)) {
                    String errorMessage = String.format("Invalid %s provided",
                            camelCaseConfigParam.replaceAll("[\r\n]", ""));
                    log.debug(errorMessage);
                    throw new FinancialServicesDCRException(IdentityCommonConstants.INVALID_CLIENT_METADATA,
                            errorMessage);
                }
            }
        } else if (!allowedList.contains(param)) {
            String errorMessage = String.format("Invalid %s provided",
                    camelCaseConfigParam.replaceAll("[\r\n]", ""));
            log.debug(errorMessage);
            throw new FinancialServicesDCRException(IdentityCommonConstants.INVALID_CLIENT_METADATA,
                    errorMessage);
        }
    }
}
