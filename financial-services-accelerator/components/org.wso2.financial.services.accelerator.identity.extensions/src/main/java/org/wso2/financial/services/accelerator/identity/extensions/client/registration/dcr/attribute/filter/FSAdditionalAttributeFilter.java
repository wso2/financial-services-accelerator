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

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesDCRException;
import org.wso2.financial.services.accelerator.common.exception.FinancialServicesException;
import org.wso2.financial.services.accelerator.common.extension.model.ServiceExtensionTypeEnum;
import org.wso2.financial.services.accelerator.common.util.FinancialServicesUtils;
import org.wso2.financial.services.accelerator.common.util.ServiceExtensionUtils;
import org.wso2.financial.services.accelerator.identity.extensions.client.registration.dcr.extension.FSAbstractDCRExtension;
import org.wso2.financial.services.accelerator.identity.extensions.client.registration.dcr.util.DCRUtils;
import org.wso2.financial.services.accelerator.identity.extensions.client.registration.dcr.validators.DynamicClientRegistrationValidator;
import org.wso2.financial.services.accelerator.identity.extensions.internal.IdentityExtensionsDataHolder;
import org.wso2.financial.services.accelerator.identity.extensions.util.IdentityCommonConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Additional attribute filter for financial services accelerator.
 */
public class FSAdditionalAttributeFilter implements AdditionalAttributeFilter {

    private static final FinancialServicesConfigurationService configurationService =
            IdentityExtensionsDataHolder.getInstance().getConfigurationService();
    private static final Log log = LogFactory.getLog(FSAdditionalAttributeFilter.class);

    @Override
    public Map<String, Object> filterDCRRegisterAttributes(ApplicationRegistrationRequest appRegistrationRequest,
                                                           Map<String, Object> ssaParams) throws DCRMClientException {

        Map<String, Object> filteredAttributes = new HashMap<>();
        if (appRegistrationRequest.getAdditionalAttributes().containsKey(FinancialServicesConstants.REGULATORY)) {
            // Handling DCR requests invoked from Devportal
            Map<String, Object> additionalAttributes = appRegistrationRequest.getAdditionalAttributes();
            filteredAttributes.putAll(additionalAttributes);
        } else {
            // Handling DCR requests invoked from Gateway DCR endpoint
            filteredAttributes = handleGatewayDCRRegisterRequest(appRegistrationRequest, ssaParams);
        }

        return filteredAttributes;
    }

    @Override
    public Map<String, Object> filterDCRUpdateAttributes(ApplicationUpdateRequest applicationUpdateRequest,
                                                         Map<String, Object> ssaParams,
                                                         ServiceProviderProperty[] serviceProviderProperties)
            throws DCRMClientException {

        Map<String, Object> filteredAttributes = new HashMap<>();
        if (applicationUpdateRequest.getAdditionalAttributes().containsKey(FinancialServicesConstants.REGULATORY)) {
            // Handling DCR requests invoked from Devportal
            Map<String, Object> additionalAttributes = applicationUpdateRequest.getAdditionalAttributes();
            filteredAttributes.putAll(additionalAttributes);
        } else {
            // Handling DCR requests invoked from Gateway DCR endpoint
            filteredAttributes = handleGatewayDCRUpdateRequest(applicationUpdateRequest, ssaParams,
                    serviceProviderProperties);
        }

        return filteredAttributes;
    }

    @Override
    public Map<String, Object> processDCRGetAttributes(Map<String, String> ssaParams) throws DCRMClientException {

        // Executing configured DCR validators.
        for (DynamicClientRegistrationValidator validator : DCRUtils.getEnabledDcrValidators()) {
            try {
                validator.validateGet(ssaParams);
            } catch (FinancialServicesDCRException e) {
                log.error(e.getMessage().replaceAll("[\r\n]", ""));
                throw new DCRMClientException(e.getErrorCode(), e.getMessage(), e);
            }
        }

        if (ServiceExtensionUtils.isInvokeExternalService(ServiceExtensionTypeEnum.PRE_PROCESS_CLIENT_RETRIEVAL)) {
            log.debug("Executing external service call to get custom attributes to store");
            return DCRUtils.callExternalServiceForRetrieval(ssaParams);
        }
        // Filtering the attributes to be returned in the response.
        return new HashMap<>(ssaParams);
    }

    @Override
    public List<String> getResponseAttributeKeys() {

        //  Adding BFSI specific fields to be returned in the response.
        Set<String> responseAttributeKeys = DCRUtils.getResponseParamsFromConfig();
        responseAttributeKeys.add(IdentityCommonConstants.SOFTWARE_ID);
        responseAttributeKeys.add(FinancialServicesConstants.SCOPE);
        responseAttributeKeys.add(IdentityCommonConstants.RESPONSE_TYPES);
        responseAttributeKeys.add(IdentityCommonConstants.APPLICATION_TYPE);
        return new ArrayList<>(responseAttributeKeys);
    }

    /**
     * Handle the DCR register request for Gateway DCR endpoint.
     *
     * @param appRegistrationRequest   Application Registration Request
     * @param ssaParams                SSA Params
     * @return  Filtered attributes to be stored
     * @throws DCRMClientException    DCRM Client Exception
     */
    private Map<String, Object> handleGatewayDCRRegisterRequest(ApplicationRegistrationRequest appRegistrationRequest,
                                       Map<String, Object> ssaParams) throws DCRMClientException {
        // Executing configured DCR validators.
        for (DynamicClientRegistrationValidator validator : DCRUtils.getEnabledDcrValidators()) {
            try {
                validator.validatePost(appRegistrationRequest, ssaParams);
            } catch (FinancialServicesDCRException e) {
                log.error(e.getMessage().replaceAll("[\r\n]", ""));
                throw new DCRMClientException(e.getErrorCode(), e.getMessage(), e);
            }
        }

        DCRUtils.validateRequireRequestObject(appRegistrationRequest.isRequireSignedRequestObject());

        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> attributesToStore = new HashMap<>();
        try {
            JSONObject appRequestObj = new JSONObject(objectMapper.writeValueAsString(appRegistrationRequest));

            if (ServiceExtensionUtils.isInvokeExternalService(ServiceExtensionTypeEnum.PRE_PROCESS_CLIENT_CREATION)) {
                log.debug("Executing external service call to get custom attributes to store");
                attributesToStore = DCRUtils.callExternalService(appRequestObj, ssaParams, null,
                        ServiceExtensionTypeEnum.PRE_PROCESS_CLIENT_CREATION);
            } else if (getFSDCRExtension() != null) {
                log.debug("Executing custom DCR extension to get custom attributes to store");
                return getFSDCRExtension().validateDCRRegisterAttributes(appRequestObj, ssaParams);
            }
        } catch (JsonProcessingException e) {
            throw new DCRMClientException(IdentityCommonConstants.SERVER_ERROR, e.getMessage(), e);
        } catch (FinancialServicesException e) {
            throw new DCRMClientException(IdentityCommonConstants.INVALID_CLIENT_METADATA, e.getMessage(), e);
        }

        Map<String, Object> requestMap = objectMapper.convertValue(appRegistrationRequest, Map.class);
        Map<String, Object> filteredAttributes = new HashMap<>(ssaParams);
        Map<String, Object> additionalAttributes = appRegistrationRequest.getAdditionalAttributes();
        // Adding fields from the configuration to be stored as SP metadata
        if (appRegistrationRequest.getSoftwareStatement() != null) {
            filteredAttributes.put(IdentityCommonConstants.SOFTWARE_STATEMENT,
                    appRegistrationRequest.getSoftwareStatement());
        }
        //Storing client name to use in consent mgr app
        if (appRegistrationRequest.getClientName() != null) {
            filteredAttributes.put(IdentityCommonConstants.CLIENT_NAME,
                    appRegistrationRequest.getClientName());
        }
        getResponseAttributeKeys()
                .stream()
                .filter(additionalAttributes::containsKey)
                .filter(key -> !filteredAttributes.containsKey(key))
                .forEach((key) -> filteredAttributes.put(key, additionalAttributes.get(key)));

        getResponseAttributeKeys()
                .stream()
                .filter(requestMap::containsKey)
                .filter(key -> !filteredAttributes.containsKey(key))
                .forEach((key) -> filteredAttributes.put(key, requestMap.get(key)));


        Map<String, Object> finalAttributesToStore = attributesToStore;
        attributesToStore.keySet().stream()
                .filter(key -> !filteredAttributes.containsKey(key))
                .forEach((key) -> filteredAttributes.put(key, finalAttributesToStore.get(key)));
        //Setting the software statement to null to avoid sending software statement twice in the response.
        appRegistrationRequest.setSoftwareStatement(null);

        // Setting the regulatory field to true to indicate that the request is from a regulatory client.
        filteredAttributes.put(FinancialServicesConstants.REGULATORY, true);
        // Adding SP property to identify create request. Will be removed when setting up authenticators.
        filteredAttributes.put("AppCreateRequest", "true");
        return filteredAttributes;
    }

    /**
     * Handle the DCR update request for Gateway DCR endpoint.
     *
     * @param applicationUpdateRequest    Application Update Request
     * @param ssaParams                   SSA Params
     * @param serviceProviderProperties   Service Provider Properties
     * @return Filtered attributes to be stored
     * @throws DCRMClientException      DCRM Client Exception
     */
    private Map<String, Object> handleGatewayDCRUpdateRequest(ApplicationUpdateRequest applicationUpdateRequest,
                                                       Map<String, Object> ssaParams,
                                                       ServiceProviderProperty[] serviceProviderProperties)
            throws DCRMClientException {

        // Executing configured DCR validators.
        for (DynamicClientRegistrationValidator validator : DCRUtils.getEnabledDcrValidators()) {
            try {
                validator.validateUpdate(applicationUpdateRequest, ssaParams, serviceProviderProperties);
            } catch (FinancialServicesDCRException e) {
                log.error(e.getMessage().replaceAll("[\r\n]", ""));
                throw new DCRMClientException(e.getErrorCode(), e.getMessage(), e);
            }
        }

        DCRUtils.validateRequireRequestObject(applicationUpdateRequest.isRequireSignedRequestObject());

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> requestMap = objectMapper.convertValue(applicationUpdateRequest, Map.class);
        List<JSONObject> spProperties = DCRUtils.constructSPPropertyList(serviceProviderProperties);

        Map<String, Object> attributesToStore = new HashMap<>();
        try {
            JSONObject appRequestObj = new JSONObject(objectMapper.writeValueAsString(applicationUpdateRequest));

            if (ServiceExtensionUtils.isInvokeExternalService(ServiceExtensionTypeEnum.PRE_PROCESS_CLIENT_UPDATE)) {
                log.debug("Executing external service call to get custom attributes to store");
                attributesToStore = DCRUtils.callExternalService(appRequestObj, ssaParams, spProperties,
                        ServiceExtensionTypeEnum.PRE_PROCESS_CLIENT_UPDATE);
            } else if (getFSDCRExtension() != null) {
                log.debug("Executing custom DCR extension to get custom attributes to store");
                return getFSDCRExtension().validateDCRUpdateAttributes(appRequestObj, ssaParams, spProperties);
            }
        } catch (JsonProcessingException e) {
            throw new DCRMClientException(IdentityCommonConstants.SERVER_ERROR, e.getMessage(), e);
        } catch (FinancialServicesException e) {
            throw new DCRMClientException(IdentityCommonConstants.INVALID_CLIENT_METADATA, e.getMessage(), e);
        }

        Map<String, Object> filteredAttributes = new HashMap<>(ssaParams);
        Map<String, Object> additionalAttributes = applicationUpdateRequest.getAdditionalAttributes();
        // Adding fields from the configuration to be stored as SP metadata
        if (applicationUpdateRequest.getSoftwareStatement() != null) {
            filteredAttributes.put(IdentityCommonConstants.SOFTWARE_STATEMENT,
                    applicationUpdateRequest.getSoftwareStatement());
        }
        getResponseAttributeKeys()
                .stream()
                .filter(additionalAttributes::containsKey)
                .filter(key -> !filteredAttributes.containsKey(key))
                .forEach((key) -> filteredAttributes.put(key, additionalAttributes.get(key)));

        getResponseAttributeKeys()
                .stream()
                .filter(requestMap::containsKey)
                .filter(key -> !filteredAttributes.containsKey(key))
                .forEach((key) -> filteredAttributes.put(key, requestMap.get(key)));

        Map<String, Object> finalAttributesToStore = attributesToStore;
        attributesToStore.keySet().stream()
                .filter(key -> !filteredAttributes.containsKey(key))
                .forEach((key) -> filteredAttributes.put(key, finalAttributesToStore.get(key)));
        //Setting the software statement to null to avoid sending software statement twice in the response.
        applicationUpdateRequest.setSoftwareStatement(null);

        // Adding SP property to identify update request. Will be removed when updating authenticators.
        filteredAttributes.put("AppCreateRequest", "false");
        //add ssa issuer as a custom attribute to be stored
        filteredAttributes.put("ssaIssuer", ssaParams.get("iss").toString());

        return filteredAttributes;
    }

    /**
     * Get the FSAbstractDCRExtension instance.
     * @return FSAbstractDCRExtension instance.
     */
    private static FSAbstractDCRExtension getFSDCRExtension() {
        Object dcrServiceExtension = configurationService.getConfigurations()
                .get(FinancialServicesConstants.DCR_SERVICE_EXTENSION);
        if (dcrServiceExtension == null) {
            return null;
        } else {
            return FinancialServicesUtils.getClassInstanceFromFQN(dcrServiceExtension.toString(),
                    FSAbstractDCRExtension.class);
        }
    }
}
