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

package org.wso2.financial.services.accelerator.identity.extensions.client.registration.dcr.attribute.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.oauth.dcr.bean.ApplicationRegistrationRequest;
import org.wso2.carbon.identity.oauth.dcr.bean.ApplicationUpdateRequest;
import org.wso2.carbon.identity.oauth.dcr.exception.DCRMClientException;
import org.wso2.carbon.identity.oauth.dcr.handler.AdditionalAttributeFilter;
import org.wso2.financial.services.accelerator.common.config.FinancialServicesConfigurationService;
import org.wso2.financial.services.accelerator.common.constant.FinancialServicesConstants;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceRequest;
import org.wso2.financial.services.accelerator.common.extension.model.ExternalServiceResponse;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.extension.model.StatusEnum;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.identity.extensions.client.registration.dcr.extension.FSAbstractDCRExtension;
import org.wso2.financial.services.accelerator.identity.extensions.client.registration.dcr.validators.DynamicClientRegistrationValidator;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByKey;

/**
 * Additional attribute filter for financial services accelerator.
 */
public class FSAdditionalAttributeFilter implements AdditionalAttributeFilter {

    private static final FinancialServicesConfigurationService configurationService =
            IdentityExtensionsDataHolder.getInstance().getConfigurationService();
    private static final List<DynamicClientRegistrationValidator> enabledDcrValidators =
            getEnabledDcrValidators();
    private static final Log log = LogFactory.getLog(FSAdditionalAttributeFilter.class);

    @Override
    public Map<String, Object> filterDCRRegisterAttributes(ApplicationRegistrationRequest appRegistrationRequest,
                                                           Map<String, Object> ssaParams) throws DCRMClientException {

        // Executing configured DCR validators.
        for (DynamicClientRegistrationValidator validator : enabledDcrValidators) {
            try {
                validator.validatePost(appRegistrationRequest, ssaParams);
            } catch (FinancialServicesException e) {
                log.error(e.getMessage().replaceAll("[\r\n]", ""));
                throw new DCRMClientException(IdentityCommonConstants.INVALID_CLIENT_METADATA, e.getMessage(), e);
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> requestMap = objectMapper.convertValue(appRegistrationRequest, Map.class);

        Map<String, Object> attributesToStore = getCustomAttributesToStore(requestMap, ssaParams, null,
                IdentityCommonConstants.APP_REG_REQUEST, ServiceExtensionTypeEnum.VALIDATE_DCR_CREATE_REQUEST);

        Map<String, Object> filteredAttributes = new HashMap<>();
        Map<String, Object> additionalAttributes = appRegistrationRequest.getAdditionalAttributes();
        // Adding fields from the configuration to be stored as SP metadata
        getResponseAttributeKeys()
                .stream()
                .filter(additionalAttributes::containsKey)
                .forEach((key) -> filteredAttributes.put(key, additionalAttributes.get(key)));

        attributesToStore.keySet().stream()
                .filter(key -> !filteredAttributes.containsKey(key))
                .forEach((key) -> filteredAttributes.put(key, attributesToStore.get(key)));
        return filteredAttributes;
    }

    @Override
    public Map<String, Object> filterDCRUpdateAttributes(ApplicationUpdateRequest applicationUpdateRequest,
                                                         Map<String, Object> ssaParams,
                                                         ServiceProviderProperty[] serviceProviderProperties)
            throws DCRMClientException {

        // Executing configured DCR validators.
        for (DynamicClientRegistrationValidator validator : enabledDcrValidators) {
            try {
                validator.validateUpdate(applicationUpdateRequest, ssaParams, serviceProviderProperties);
            } catch (FinancialServicesException e) {
                log.error(e.getMessage().replaceAll("[\r\n]", ""));
                throw new DCRMClientException(IdentityCommonConstants.INVALID_CLIENT_METADATA, e.getMessage(), e);
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> requestMap = objectMapper.convertValue(applicationUpdateRequest, Map.class);
        List<JSONObject> spProperties = constructSPPropertyList(serviceProviderProperties);

        Map<String, Object> attributesToStore = getCustomAttributesToStore(requestMap, ssaParams, spProperties,
                IdentityCommonConstants.APP_UPDATE_REQUEST, ServiceExtensionTypeEnum.VALIDATE_DCR_UPDATE_REQUEST);


        Map<String, Object> filteredAttributes = new HashMap<>();
        Map<String, Object> additionalAttributes = applicationUpdateRequest.getAdditionalAttributes();
        // Adding fields from the configuration to be stored as SP metadata
        getResponseAttributeKeys()
                .stream()
                .filter(additionalAttributes::containsKey)
                .forEach((key) -> filteredAttributes.put(key, additionalAttributes.get(key)));

        attributesToStore.keySet().stream()
                .filter(key -> !filteredAttributes.containsKey(key))
                .forEach((key) -> filteredAttributes.put(key, attributesToStore.get(key)));
        return filteredAttributes;
    }

    @Override
    public Map<String, Object> processDCRGetAttributes(Map<String, String> ssaParams) throws DCRMClientException {

        // Executing configured DCR validators.
        for (DynamicClientRegistrationValidator validator : enabledDcrValidators) {
            try {
                validator.validateGet(ssaParams);
            } catch (FinancialServicesException e) {
                log.error(e.getMessage().replaceAll("[\r\n]", ""));
                throw new DCRMClientException(IdentityCommonConstants.INVALID_CLIENT_METADATA, e.getMessage(), e);
            }
        }

        // Filtering the attributes to be returned in the response.
        Map<String, Object> filteredAttributes = new HashMap<>();
        filteredAttributes.putAll(ssaParams);
        return filteredAttributes;

    }

    @Override
    public List<String> getResponseAttributeKeys() {

        //  Adding BFSI specific fields to be returned in the response.
        List<String> responseAttributeKeys = getResponseParamsFromConfig();
        responseAttributeKeys.add(IdentityCommonConstants.SOFTWARE_STATEMENT);
        responseAttributeKeys.add(IdentityCommonConstants.SOFTWARE_ID);
        responseAttributeKeys.add(IdentityCommonConstants.SCOPE);
        responseAttributeKeys.add(IdentityCommonConstants.RESPONSE_TYPES);
        responseAttributeKeys.add(IdentityCommonConstants.APPLICATION_TYPE);
        return responseAttributeKeys;
    }

    /**
     * Get the response parameters from the configuration.
     * @return List of response parameters.
     */
    private List<String> getResponseParamsFromConfig() {

        List<String> responseParams = new ArrayList<>();

        Map<String, Map<String, Object>> dcrConfigs = configurationService.getDCRParamsConfig();

        dcrConfigs.forEach((key, value) -> {
            if (Boolean.parseBoolean(value.get(IdentityCommonConstants.INCLUDE_IN_RESPONSE).toString())) {
                responseParams.add(value.get(IdentityCommonConstants.KEY).toString());
            }
        });

        return responseParams;
    }

    /**
     * Get the enabled DCR validators.
     * @return List of enabled DCR validators.
     */
    private static List<DynamicClientRegistrationValidator> getEnabledDcrValidators() {

        Map<String, Map<String, Object>> dcrValidators = configurationService.getDCRValidatorsConfig();

        Map<Integer, DynamicClientRegistrationValidator> validatorMap = new HashMap<>();

        for (Map<String, Object> validator : dcrValidators.values()) {
            if (Boolean.parseBoolean(validator.get(IdentityCommonConstants.ENABLE).toString())) {
                DynamicClientRegistrationValidator dcrValidator = FinancialServicesUtils.getClassInstanceFromFQN(
                                validator.get(IdentityCommonConstants.CLASS).toString(),
                        DynamicClientRegistrationValidator.class);
                validatorMap.put(Integer.parseInt(validator.get(IdentityCommonConstants.PRIORITY).toString()),
                        dcrValidator);
            }
        }

        LinkedHashMap<Integer, DynamicClientRegistrationValidator> priorityMap = validatorMap.entrySet()
                .stream()
                .sorted(comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                        LinkedHashMap::new));

        return priorityMap.keySet().stream()
                .map(priorityMap::get).collect(Collectors.toList());
    }

    private static Map<String, Object> getCustomAttributesToStore(Map<String, Object> appUpdateRequest,
                                                                Map<String, Object> ssaParams,
                                                                List<JSONObject> spProperties, String operation,
                                                                ServiceExtensionTypeEnum serviceExtensionTypeEnum)
                        throws DCRMClientException  {

        if (ServiceExtensionUtils.isInvokeExternalService(serviceExtensionTypeEnum)) {
            try {
                ExternalServiceResponse response = ServiceExtensionUtils.invokeExternalServiceCall(
                        getExternalServiceRequest(appUpdateRequest, ssaParams, spProperties, operation),
                        serviceExtensionTypeEnum);
                if (StatusEnum.SUCCESS.equals(response.getStatus())) {
                    JSONObject attributesToStoreJson = new JSONObject(response.getData().get("attributesToStore")
                            .toString());
                    return attributesToStoreJson.toMap();
                } else {
                    throw new DCRMClientException(response.getErrorMessage(), response.getErrorDescription());
                }
            } catch (FinancialServicesException e) {
                throw new DCRMClientException(IdentityCommonConstants.SERVER_ERROR, e.getMessage());
            }
        } else if (getFSAbstractDCRExtension() != null) {
            try {
                getFSAbstractDCRExtension().validateDCRUpdateAttributes(appUpdateRequest, ssaParams, spProperties);
            } catch (FinancialServicesException e) {
                throw new DCRMClientException(IdentityCommonConstants.INVALID_CLIENT_METADATA, e.getMessage(), e);

            }
        }
        return new HashMap<>();
    }

    private static ExternalServiceRequest getExternalServiceRequest(Map<String, Object> appRequest,
                                                                    Map<String, Object> ssaParams,
                                                                    List<JSONObject> spProperties, String operation) {
        JSONObject data = new JSONObject();
        data.put(operation, appRequest);
        data.put(IdentityCommonConstants.SSA_PARAMS, ssaParams);
        if (spProperties != null) {
            data.put(IdentityCommonConstants.SP_PROPERTIES, spProperties);
        }

        return new ExternalServiceRequest(UUID.randomUUID().toString(), data);
    }

    private static FSAbstractDCRExtension getFSAbstractDCRExtension() {
        Object dcrServiceExtension = configurationService.getConfigurations()
                .get(FinancialServicesConstants.DCR_SERVICE_EXTENSION);
        if (dcrServiceExtension == null) {
            return null;
        } else {
            return FinancialServicesUtils.getClassInstanceFromFQN(dcrServiceExtension.toString(),
                    FSAbstractDCRExtension.class);
        }
    }

    private static List<JSONObject> constructSPPropertyList(ServiceProviderProperty[] serviceProviderProperties) {

        List<JSONObject> spPropertyList = new ArrayList<>();
        for (ServiceProviderProperty property : serviceProviderProperties) {
            JSONObject propertyObject = new JSONObject();
            propertyObject.put("Name", property.getName());
            propertyObject.put("Value", property.getValue());
            propertyObject.put("DisplayName", property.getDisplayName());
            spPropertyList.add(propertyObject);
        }
        return spPropertyList;
    }
}
